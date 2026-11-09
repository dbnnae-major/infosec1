package ru.itmo.infosec1.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.infosec1.dto.ApiError;
import ru.itmo.infosec1.dto.LoginRequest;
import ru.itmo.infosec1.dto.RegisterRequest;
import ru.itmo.infosec1.dto.TokenResponse;
import ru.itmo.infosec1.model.AppUser;
import ru.itmo.infosec1.repository.UserRepository;
import ru.itmo.infosec1.service.JwtService;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder passwords, JwtService jwt) {
        this.users = users;
        this.passwords = passwords;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        String username = request.username().trim();
        if (username.length() < 3) {
            return ResponseEntity.badRequest().body(new ApiError("VALIDATION_ERROR", "Username must have at least 3 non-space characters"));
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            return ResponseEntity.badRequest().body(new ApiError("VALIDATION_ERROR", "Password must not exceed 72 UTF-8 bytes"));
        }
        if (users.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiError("USERNAME_TAKEN", "Username already exists"));
        }
        users.save(new AppUser(username, passwords.encode(request.password())));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        AppUser user = users.findByUsername(request.username().trim()).orElse(null);
        if (user == null || !passwords.matches(request.password(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError("INVALID_CREDENTIALS", "Invalid username or password"));
        }
        return ResponseEntity.ok(new TokenResponse(jwt.createToken(user.getUsername()),
                "Bearer", jwt.getLifetimeSeconds()));
    }
}
