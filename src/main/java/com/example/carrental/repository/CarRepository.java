package com.example.carrental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.carrental.model.Car;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    @Query("SELECT c FROM Car c WHERE c.availabilityStatus = true AND c.carId IN (SELECT MIN(c2.carId) FROM Car c2 GROUP BY c2.make, c2.model, c2.owner)")
    List<Car> findByAvailabilityStatusTrue();
    
    @Query("SELECT c FROM Car c WHERE c.owner = ?1 AND c.carId IN (SELECT MIN(c2.carId) FROM Car c2 WHERE c2.owner = ?1 GROUP BY c2.make, c2.model)")
    List<Car> findByOwner(String owner);
} 