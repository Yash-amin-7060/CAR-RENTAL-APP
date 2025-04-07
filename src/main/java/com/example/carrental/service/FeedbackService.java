package com.example.carrental.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.carrental.model.Feedback;
import com.example.carrental.repository.FeedbackRepository;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Transactional
    public Feedback createFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getFeedbacksByCarId(Long carId) {
        return feedbackRepository.findByCarCarId(carId);
    }

    public boolean hasFeedbackForBooking(Long bookingId) {
        return feedbackRepository.existsByBookingId(bookingId);
    }
} 