package com.school.homework.controller;

import com.school.homework.entity.AppUser;
import com.school.homework.entity.AppUser.Role;
import com.school.homework.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String email = body.get("email");
        String password = body.get("password");
        String role = body.get("role");

        if (name == null || email == null || password == null || role == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "All fields required"));
        }
        if (userRepo.existsByEmail(email)) {
            return ResponseEntity.status(409).body(Map.of("error", "Email already registered"));
        }

        AppUser user = new AppUser(name, email, encoder.encode(password), Role.valueOf(role.toUpperCase()));
        if (body.containsKey("className")) user.setClassName(body.get("className"));
        if (body.containsKey("studentEmail")) user.setStudentEmail(body.get("studentEmail"));
        userRepo.save(user);

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        AppUser user = userRepo.findByEmail(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not found"));

        return ResponseEntity.ok(Map.of(
            "id", user.getId(), "name", user.getName(), "email", user.getEmail(),
            "role", user.getRole().name(), "className", user.getClassName() != null ? user.getClassName() : "",
            "studentEmail", user.getStudentEmail() != null ? user.getStudentEmail() : ""
        ));
    }
}
