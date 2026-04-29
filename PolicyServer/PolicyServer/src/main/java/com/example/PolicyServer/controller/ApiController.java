// File: PolicyServer/src/main/java/com/example/PolicyServer/controller/ApiController.java
package com.example.PolicyServer.controller;

import com.example.PolicyServer.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api") 
public class ApiController {

    @Autowired
    private PolicyService policyService;

    // CRITICAL FIX: Changed from 'record' to a simple 'class' with getters/setters 
    // to ensure successful JSON deserialization by the server.
    public static class StatusUpdateRequest {
        private String targetPhone;
        private String newStatus;

        // Default Constructor (REQUIRED for JSON deserialization)
        public StatusUpdateRequest() {}

        // Getters and Setters (REQUIRED for JSON deserialization)
        public String getTargetPhone() { return targetPhone; }
        public void setTargetPhone(String targetPhone) { this.targetPhone = targetPhone; }
        public String getNewStatus() { return newStatus; }
        public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    }

    /**
     * Endpoint 1: Retrieves secure policy details.
     * URL: GET http://localhost:8080/api/details
     */
    @GetMapping("/details")
    public ResponseEntity<?> getDetails(
        @RequestHeader(value = "Authorization") String accessToken) 
    {
        // Fix for common Postman issue: strips "Bearer " prefix if present
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        
        // 1. Initial Security Check
        if (accessToken == null || accessToken.isEmpty()) {
             return ResponseEntity.status(401).body(
                Map.of("error", "Access Denied. Token is missing.")
            );
        }

        // 2. Logic: Call the service to retrieve the policy data
        Optional<String> policyDetailsJson = policyService.getPolicyDetails(accessToken);

        if (policyDetailsJson.isPresent()) {
            // Success: Return the policy JSON string (HTTP 200 OK)
            return ResponseEntity.ok(policyDetailsJson.get());
        }
        
        // Failure: Token may be invalid or user data not found
        return ResponseEntity.status(401).body(
            Map.of("error", "Access Denied. Invalid token or user data not found.")
        );
    }
    
    /**
     * Endpoint 2: Allows an authorized user (Manager/Admin) to change account status.
     * URL: POST http://localhost:8080/api/admin/status
     */
    @PostMapping("/admin/status")
    public ResponseEntity<?> updateAccountStatus(
        @RequestHeader(value = "Authorization") String accessToken, 
        @RequestBody StatusUpdateRequest request) 
    {
        // Token validation check
        if (accessToken == null || accessToken.isEmpty()) {
             return ResponseEntity.status(401).body(
                Map.of("error", "Permission Denied. Token missing.")
            );
        }

        // Call the service to update the status in the database
        boolean success = policyService.setAccountStatus(request.getTargetPhone(), request.getNewStatus());

        if (success) {
            return ResponseEntity.ok(
                Map.of("message", "Account " + request.getTargetPhone() + " status set to " + request.getNewStatus().toUpperCase()));
        }
        
        return ResponseEntity.status(403).body( // 403 Forbidden
            Map.of("error", "Action Failed. Target account not found or status invalid."));
    }
}