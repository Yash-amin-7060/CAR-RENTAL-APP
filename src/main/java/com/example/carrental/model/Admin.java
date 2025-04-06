package com.example.carrental.model;

import lombok.Data;
import javax.persistence.Entity;

@Data
@Entity
public class Admin extends Person {
    
    @Override
    public void login() {
        // Implementation for admin login
    }
    
    @Override
    public void register() {
        // Implementation for admin registration
    }
    
    @Override
    public void logout() {
        // Implementation for admin logout
    }
    
    public void addCarDetails() {
        // Implementation for adding car details
    }
    
    public void editCarDetails() {
        // Implementation for editing car details
    }
    
    public void removeCar() {
        // Implementation for removing a car
    }
    
    public void manageAdminTasks() {
        // Implementation for managing admin tasks
    }
} 