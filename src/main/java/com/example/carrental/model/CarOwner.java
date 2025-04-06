package com.example.carrental.model;

import lombok.Data;
import javax.persistence.Entity;

@Data
@Entity
public class CarOwner extends Person {
    
    @Override
    public void login() {
        // Implementation for car owner login
    }
    
    @Override
    public void register() {
        // Implementation for car owner registration
    }
    
    @Override
    public void logout() {
        // Implementation for car owner logout
    }
    
    public void uploadCarDetails() {
        // Implementation for uploading car details
    }
    
    public void updateCarDetails() {
        // Implementation for updating car details
    }
    
    public void removeCar() {
        // Implementation for removing car
    }
} 