package com.example.carrental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.carrental.model.Feedback;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByCarCarId(Long carId);
    List<Feedback> findByUserPersonId(Integer userId);
    boolean existsByBookingId(Long bookingId);
} 