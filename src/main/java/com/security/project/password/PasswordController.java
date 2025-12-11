package com.security.project.password;

import com.security.project.password.PasswordResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password")
public class PasswordController {

    private final PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    /**
     * Generate a secure password.
     * GET /api/password?length=16
     * length is optional, default 16, must be between 8 and 128.
     */
    @GetMapping
    public ResponseEntity<PasswordResponse> generate(@RequestParam(defaultValue = "10") int length) {
        String pw = passwordService.generate(length);
        return ResponseEntity.ok(new PasswordResponse(pw));
    }
}