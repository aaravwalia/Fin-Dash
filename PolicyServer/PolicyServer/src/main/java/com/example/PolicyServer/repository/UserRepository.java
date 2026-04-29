// File: PolicyServer/.../repository/UserRepository.java
package com.example.PolicyServer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.example.PolicyServer.model.User; // New import for Lock annotation

import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * FINAL SECURITY FIX FOR STALE DATA: 
     * Uses the @Lock annotation to force Hibernate to bypass the cache 
     * and retrieve the latest account status (ACTIVE/FROZEN) directly from MySQL.
     */
    // Use LockModeType.PESSIMISTIC_READ or read-only hint to force immediate database check
    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<User> findByPhoneNumber(String phoneNumber);
}