package com.example.carrental.model;

import lombok.Data;
import javax.persistence.Entity;

@Data
@Entity
public class SUV extends Car {
    private double offRoadCapability;
    private double towingCapacity;
    
    public void offRoadCapability() {
        // Implementation for off-road capability
    }
    
    public void towingCapacity() {
        // Implementation for towing capacity
    }
    
    public void showType() {
        // Implementation for showing car type
    }
} 