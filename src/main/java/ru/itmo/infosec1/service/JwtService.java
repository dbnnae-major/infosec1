package ru.itmo.infosec1.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;

@Service
public final class JwtService {
    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final String issuer;
    private final long lifetimeSeconds;

    public JwtService(
            @Value("${app.jwt.secret-base64}") String encodedSecret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.lifetime-seconds}") long lifetimeSeconds) {
        byte[] secret;
        try {
            secret = Base64.getDecoder().decode(encodedSecret);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("JWT_SECRET_BASE64 must contain a Base64-encoded random key", e);
        }
        if (secret.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET_BASE64 must encode at least 32 bytes");
        }
        if (lifetimeSeconds <= 0) {
            throw new IllegalArgumentException("JWT lifetime must be positive");
        }
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.lifetimeSeconds = lifetimeSeconds;
        this.verifier = JWT.require(algorithm).withIssuer(issuer).build();
    }

    public String createToken(String username) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(username)
                .withIssuedAt(now)
                .withExpiresAt(now.plusSeconds(lifetimeSeconds))
                .sign(algorithm);
    }

    public String verifiedUsername(String token) {
        return verifier.verify(token).getSubject();
    }

    public long getLifetimeSeconds() {
        return lifetimeSeconds;
    }
}
