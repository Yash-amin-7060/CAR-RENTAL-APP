package com.example.carrental.model;

import lombok.Data;
import javax.persistence.Entity;

@Data
@Entity
public class Sedan extends Car {
    private int seatingCapacity;
    private double fuelEfficiency;
    
    public void seatingCapacity() {
        // Implementation for seating capacity
    }
    
    public void fuelEfficiency() {
        // Implementation for fuel efficiency
    }
    
    public void showType() {
        // Implementation for showing car type
    }
} 