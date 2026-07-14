package in.advohq.security;

import in.advohq.config.AdvoHqProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and validates signed JWT access tokens.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(AdvoHqProperties props) {
        byte[] secret = Base64.getDecoder().decode(props.security().jwt().secret());
        this.key = Keys.hmacShaKeyFor(secret);
        this.expirationMs = props.security().jwt().expirationMs();
    }

    /** Generate a token whose subject is the user id, carrying the username and token version as claims. */
    public String generateToken(UUID userId, String username, int tokenVersion) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("ver", tokenVersion)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parse(token).getSubject());
    }

    public String extractUsername(String token) {
        return parse(token).get("username", String.class);
    }

    /** Tokens issued before versioning have no "ver" claim; treat them as version 0. */
    public int extractTokenVersion(String token) {
        Integer ver = parse(token).get("ver", Integer.class);
        return ver == null ? 0 : ver;
    }

    /** @return true if the token is well-formed, correctly signed and unexpired. */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
