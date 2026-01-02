package uth.edu.vn.lms_user_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> publicEndpoint() {
        return ResponseEntity.ok(Map.of(
                "message", "This is a public endpoint",
                "status", "success"
        ));
    }

    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return ResponseEntity.ok(Map.of(
                "message", "This is a protected endpoint - authentication successful!",
                "username", auth.getName(),
                "authorities", auth.getAuthorities(),
                "status", "success"
        ));
    }

    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> adminEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return ResponseEntity.ok(Map.of(
                "message", "This is an admin endpoint - authentication successful!",
                "username", auth.getName(),
                "authorities", auth.getAuthorities(),
                "status", "success"
        ));
    }
}
