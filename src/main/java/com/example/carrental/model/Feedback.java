package com.example.carrental.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer feedbackId;
    
    private String renter;
    private int rating;
    private String comments;
    
    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;
    
    public void submitFeedback() {
        // Implementation for submitting feedback
    }
    
    public void viewFeedback() {
        // Implementation for viewing feedback
    }
} 