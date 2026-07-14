package in.advohq.service;

import in.advohq.config.AdvoHqProperties;
import in.advohq.domain.User;
import in.advohq.dto.AuthResponse;
import in.advohq.dto.ChangePasswordRequest;
import in.advohq.dto.LoginRequest;
import in.advohq.dto.RegisterRequest;
import in.advohq.dto.UserResponse;
import in.advohq.exception.ConflictException;
import in.advohq.exception.NotFoundException;
import in.advohq.repo.UserRepository;
import in.advohq.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final long expirationMs;

    public AuthService(UserRepository users, PasswordEncoder encoder,
                       JwtService jwt, AdvoHqProperties props) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.expirationMs = props.security().jwt().expirationMs();
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (users.existsByUsernameIgnoreCase(req.username())) {
            throw new ConflictException("Username '" + req.username() + "' is already taken");
        }
        User u = new User();
        u.setUsername(req.username().trim());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setFullName(req.fullName().trim());
        u.setDisplayName(req.fullName().trim());
        u.setPhone(req.phone());
        u.setEmail(req.email());
        u = users.save(u);
        return issue(u);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User u = users.findByUsernameIgnoreCase(req.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return issue(u);
    }

    @Transactional
    public AuthResponse changePassword(UUID userId, ChangePasswordRequest req) {
        User u = users.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!encoder.matches(req.currentPassword(), u.getPasswordHash())) {
            // IllegalArgumentException → 400 with this message, so the client can
            // tell "wrong current password" apart from an expired session (401).
            throw new IllegalArgumentException("Current password is incorrect");
        }
        u.setPasswordHash(encoder.encode(req.newPassword()));
        // Invalidate every previously issued token, then hand back a fresh one
        // so the session that changed the password stays signed in.
        u.setTokenVersion(u.getTokenVersion() + 1);
        u = users.save(u);
        return issue(u);
    }

    private AuthResponse issue(User u) {
        String token = jwt.generateToken(u.getId(), u.getUsername(), u.getTokenVersion());
        return AuthResponse.of(token, expirationMs, UserResponse.from(u));
    }
}
