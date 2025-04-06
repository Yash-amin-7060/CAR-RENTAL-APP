package com.example.carrental.model;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@Data
@Entity
@Table(name = "users")
public class User extends Person {
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String role = "USER"; // Default role
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @PrePersist
    @PreUpdate
    public void updateFullName() {
        if (firstName != null && lastName != null) {
            super.setName(firstName.trim() + " " + lastName.trim());
        }
    }
    
    // Getters and Setters
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        // Remove ROLE_ prefix if it exists
        role = role.replaceAll("^ROLE_", "");
        
        // Add ROLE_ prefix
        this.role = "ROLE_" + role;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        updateFullName();
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
        updateFullName();
    }
    
    @Override
    public String getName() {
        if (firstName != null && lastName != null) {
            return firstName.trim() + " " + lastName.trim();
        }
        return super.getName();
    }
    
    @Override
    public void setName(String name) {
        super.setName(name);
        if (name != null) {
            String[] parts = name.trim().split("\\s+", 2);
            this.firstName = parts[0];
            this.lastName = parts.length > 1 ? parts[1] : "";
        }
    }
    
    @Override
    public void login() {
        // Implementation for user login
    }
    
    @Override
    public void register() {
        // Implementation for user registration
    }
    
    @Override
    public void logout() {
        // Implementation for user logout
    }
    
    public void viewCarList() {
        // Implementation for viewing car list
    }
} 