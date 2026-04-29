// File: PolicyServer/src/main/java/com/example/PolicyServer/model/User.java
package com.example.PolicyServer.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "users") 
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;
    
    @Column(columnDefinition = "JSON") 
    private String policyDetails; 

    private String otpSecret;
    private LocalDateTime lastOtpTime;
    
    // --- NEW FIELDS ---
    private String role; // Stores USER or ADMIN
    private String accountStatus; // Stores ACTIVE or FROZEN
    // ------------------
    
    public User() {} 
    
    // --- FULL GETTERS AND SETTERS (REQUIRED) ---
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPolicyDetails() { return policyDetails; }
    public void setPolicyDetails(String policyDetails) { this.policyDetails = policyDetails; }

    public String getOtpSecret() { return otpSecret; }
    public void setOtpSecret(String otpSecret) { this.otpSecret = otpSecret; }

    public LocalDateTime getLastOtpTime() { return lastOtpTime; }
    public void setLastOtpTime(LocalDateTime lastOtpTime) { this.lastOtpTime = lastOtpTime; }
    
    // --- ROLE AND STATUS METHODS ---
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    // The method Maven was looking for:
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { 
        this.accountStatus = accountStatus; 
    }
}