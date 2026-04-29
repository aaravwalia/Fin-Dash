// File: PolicyServer/src/main/java/.../service/PolicyService.java
package com.example.PolicyServer.service;

import com.example.PolicyServer.model.User;
import com.example.PolicyServer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // CRITICAL IMPORT
import java.util.Optional;

@Service
public class PolicyService {

    @Autowired
    private UserRepository userRepository;

    private static final String FIXED_USER_PHONE = "1234567890"; 

    /**
     * Retrieves the policy details (READ operation). 
     * FIX: @Transactional(readOnly = true) guarantees read consistency.
     */
    @Transactional(readOnly = true) 
    public Optional<String> getPolicyDetails(String accessToken) {
        
        if (accessToken == null || accessToken.isEmpty()) {
            return Optional.empty(); 
        }

        // Simulates finding the user based on the valid token (using the fixed phone number)
        Optional<User> userOptional = userRepository.findByPhoneNumber(FIXED_USER_PHONE);

        if (userOptional.isPresent()) {
            return Optional.of(userOptional.get().getPolicyDetails());
        }
        
        return Optional.empty();
    }
    
    /**
     * Sets the account status (WRITE/UPDATE operation). 
     * FIX: @Transactional guarantees the write operation is safely committed.
     */
    @Transactional 
    public boolean setAccountStatus(String targetPhone, String newStatus) {
        
        Optional<User> userOptional = userRepository.findByPhoneNumber(targetPhone);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            if ("FROZEN".equalsIgnoreCase(newStatus) || "ACTIVE".equalsIgnoreCase(newStatus)) {
                user.setAccountStatus(newStatus.toUpperCase());
                userRepository.save(user); // This requires an active transaction
                return true;
            }
        }
        return false;
    }
}