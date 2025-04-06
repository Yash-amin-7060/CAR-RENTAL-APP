package com.example.carrental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.carrental.model.Booking;
import com.example.carrental.model.User;

@Repository
@Transactional(readOnly = true)
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.car WHERE b.user = :user ORDER BY b.startDate DESC")
    List<Booking> findByUser(@Param("user") User user);
    
    List<Booking> findByCar_Owner(String owner);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.car WHERE b.status = :status")
    List<Booking> findByStatus(@Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE Booking b SET b.status = 'CANCELLED' WHERE b.status = 'PENDING' AND b.user = :user")
    void cancelPendingBookingsForUser(@Param("user") User user);
} 