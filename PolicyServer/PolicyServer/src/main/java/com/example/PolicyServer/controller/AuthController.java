// File: PolicyServer/src/main/java/.../controller/AuthController.java
package com.example.PolicyServer.controller; // <-- UPDATE THIS PACKAGE NAME

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PolicyServer.service.AuthService;

@RestController // Exposes data over the web (REST API)
@RequestMapping("/auth") // All endpoints start with /auth
public class AuthController {

    @Autowired
    private AuthService authService;

    // Inner classes to define the structure of the JSON data coming from the client
    public record PhoneRequest(String phoneNumber) {}
    public record VerifyRequest(String phoneNumber, String otp) {}

    /**
     * Endpoint 1: Initiates the login process (Step 1 of 2FA).
     * URL: POST http://localhost:8080/auth/request-otp
     */
    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody PhoneRequest request) {
        if (authService.requestOtp(request.phoneNumber())) {
            // HTTP 200 OK
            return ResponseEntity.ok(
                Map.of("message", "OTP sent successfully.")
            );
        }
        // HTTP 404 Not Found (User not registered)
        return ResponseEntity.status(404).body(
            Map.of("error", "Phone number not registered.")
        );
    }

    /**
     * Endpoint 2: Verifies the submitted OTP. Returns a token on success.
     * URL: POST http://localhost:8080/auth/verify-otp
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyRequest request) {
        
        Optional<String> token = authService.verifyOtp(request.phoneNumber(), request.otp());
        
        if (token.isPresent()) {
            // HTTP 200 OK with the generated access token
            return ResponseEntity.ok(
                Map.of("message", "Login successful!", "accessToken", token.get())
            );
        }
        // HTTP 401 Unauthorized (Wrong OTP or expired)
        return ResponseEntity.status(401).body(
            Map.of("error", "Verification failed. Invalid or expired OTP.")
        );
    }
}