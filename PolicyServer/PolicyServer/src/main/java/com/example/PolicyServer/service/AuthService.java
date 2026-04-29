package com.example.PolicyServer.service; 

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // CRITICAL: Enables database WRITE operations
import org.springframework.transaction.annotation.Transactional;

import com.example.PolicyServer.model.User;
import com.example.PolicyServer.repository.UserRepository;

@Service 
public class AuthService {

    @Autowired 
    private UserRepository userRepository;

    private static final int OTP_EXPIRY_MINUTES = 5;

    /**
     * Finds user, generates OTP, and SAVES to DB.
     * Requires @Transactional because it updates the database (saves OTP).
     */
    @Transactional 
    public boolean requestOtp(String phoneNumber) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // CRITICAL CHECK 1: Block sending OTP if account is FROZEN
            if ("FROZEN".equalsIgnoreCase(user.getAccountStatus())) {
                System.out.println("--- LOGIN BLOCKED --- Account " + phoneNumber + " is FROZEN. Cannot send OTP.");
                return false; 
            }
            
            String otp = String.format("%06d", new Random().nextInt(1000000));
            
            user.setOtpSecret(otp);
            user.setLastOtpTime(LocalDateTime.now());
            userRepository.save(user); // Database write operation
            
            System.out.println("\n--- SIMULATED SMS SENT ---");
            System.out.println("User: " + phoneNumber + " | Your OTP is: " + otp);
            System.out.println("--------------------------\n");
            
            return true;
        }
        return false;
    }

    /**
     * Verifies OTP against DB data. 
     * Uses readOnly = true because it primarily reads, though it checks before write logic in controllers.
     */
    @Transactional(readOnly = true) 
    public Optional<String> verifyOtp(String phoneNumber, String otp) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // CRITICAL CHECK 2: Block login if account is FROZEN
            // This prevents the user from getting a valid token.
            if ("FROZEN".equalsIgnoreCase(user.getAccountStatus())) {
                 // Return empty Optional, which causes the Controller to fail the login attempt.
                 System.out.println("--- LOGIN BLOCKED --- Account " + phoneNumber + " is FROZEN. Verification denied.");
                 return Optional.empty(); 
            }
            
            // Proceed with normal OTP validation only if ACTIVE
            if (otp.equals(user.getOtpSecret())) {
                LocalDateTime expiryTime = user.getLastOtpTime().plusMinutes(OTP_EXPIRY_MINUTES);
                if (LocalDateTime.now().isBefore(expiryTime)) {
                    
                    String token = java.util.UUID.randomUUID().toString();
                    return Optional.of(token); 
                }
            }
        }
        return Optional.empty(); // Fails if OTP is wrong, expired, or user not found/frozen
    }
}