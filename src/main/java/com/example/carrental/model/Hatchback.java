package com.example.carrental.model;

import lombok.Data;
import javax.persistence.Entity;

@Data
@Entity
public class Hatchback extends Car {
    private double space;
    private double fuelEfficiency;
    
    public void space() {
        // Implementation for space calculation
    }
    
    public void fuelEfficiency() {
        // Implementation for fuel efficiency
    }
    
    public void showType() {
        // Implementation for showing car type
    }
} 