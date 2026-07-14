package in.advohq.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed-window per-IP rate limit for the public auth endpoints so passwords
 * can't be brute-forced. Everything else is JWT-protected and left alone.
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS_PER_WINDOW = 15;
    private static final long WINDOW_MS = 60_000;
    private static final int MAX_TRACKED_IPS = 10_000;   // memory bound

    private record Window(long startMs, AtomicInteger count) {}

    private final Map<String, Window> attempts = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Only login/register need this; CORS preflights must pass untouched.
        return !request.getRequestURI().startsWith("/api/auth/")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        long now = System.currentTimeMillis();
        Window window = attempts.compute(clientIp(request), (ip, current) ->
                (current == null || now - current.startMs() >= WINDOW_MS)
                        ? new Window(now, new AtomicInteger(0))
                        : current);

        if (window.count().incrementAndGet() > MAX_ATTEMPTS_PER_WINDOW) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Too Many Requests\",\"message\":\"Too many attempts. Please wait a minute and try again.\"}");
            return;
        }

        if (attempts.size() > MAX_TRACKED_IPS) {
            pruneExpired(now);
        }
        chain.doFilter(request, response);
    }

    private void pruneExpired(long now) {
        for (Iterator<Map.Entry<String, Window>> it = attempts.entrySet().iterator(); it.hasNext(); ) {
            if (now - it.next().getValue().startMs() >= WINDOW_MS) {
                it.remove();
            }
        }
    }

    /**
     * Resolve the real client IP. Cloudflare fronts Render, so CF-Connecting-IP
     * is authoritative and — unlike X-Forwarded-For — can't be spoofed by the
     * client to rotate past the rate limit (Cloudflare overwrites it). Fall back
     * to XFF, then the socket address, for non-Cloudflare/local setups.
     */
    private String clientIp(HttpServletRequest request) {
        String cf = request.getHeader("CF-Connecting-IP");
        if (cf != null && !cf.isBlank()) {
            return cf.trim();
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
