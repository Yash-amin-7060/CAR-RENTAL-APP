package com.example.carrental.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.carrental.model.Car;
import com.example.carrental.repository.CarRepository;

@Service
public class CarService {
    
    private static final Logger logger = LoggerFactory.getLogger(CarService.class);
    
    @Autowired
    private CarRepository carRepository;
    
    public List<Car> getAllCars() {
        logger.debug("Fetching all cars without duplicates");
        return removeDuplicatesFromList(carRepository.findAll());
    }
    
    public List<Car> getAllAvailableCars() {
        logger.debug("Fetching all available cars without duplicates");
        return removeDuplicatesFromList(carRepository.findByAvailabilityStatusTrue());
    }

    private List<Car> removeDuplicatesFromList(List<Car> cars) {
        Map<String, Car> uniqueCars = new HashMap<>();
        
        // Keep only one instance of each car (make + model + owner combination)
        for (Car car : cars) {
            String key = car.getMake() + "|" + car.getModel() + "|" + car.getOwner();
            if (!uniqueCars.containsKey(key) || car.getCarId() < uniqueCars.get(key).getCarId()) {
                uniqueCars.put(key, car);
            }
        }
        
        return new ArrayList<>(uniqueCars.values());
    }
    
    public List<Car> getCarsByOwner(String owner) {
        logger.debug("Fetching cars for owner: {}", owner);
        return carRepository.findByOwner(owner);
    }
    
    public Car addCar(Car car) {
        if (car == null) {
            logger.error("Cannot add null car");
            throw new IllegalArgumentException("Car cannot be null");
        }
        logger.debug("Adding new car: {} {}", car.getMake(), car.getModel());
        return carRepository.save(car);
    }
    
    public void deleteCar(Long id) {
        if (id == null) {
            logger.error("Cannot delete car with null ID");
            throw new IllegalArgumentException("Car ID cannot be null");
        }
        logger.debug("Deleting car with ID: {}", id);
        carRepository.deleteById(id);
    }
    
    public Car updateCar(Car car) {
        if (car == null || car.getCarId() == null) {
            logger.error("Cannot update null car or car without ID");
            throw new IllegalArgumentException("Car and car ID cannot be null");
        }
        logger.debug("Updating car with ID: {}", car.getCarId());
        return carRepository.save(car);
    }
    
    public Car getCarById(Long id) {
        if (id == null) {
            logger.error("Cannot get car with null ID");
            throw new IllegalArgumentException("Car ID cannot be null");
        }
        logger.debug("Fetching car with ID: {}", id);
        Optional<Car> car = carRepository.findById(id);
        if (car.isEmpty()) {
            logger.warn("No car found with ID: {}", id);
            return null;
        }
        return car.get();
    }
    
    public Car saveCar(Car car) {
        if (car == null) {
            logger.error("Cannot save null car");
            throw new IllegalArgumentException("Car cannot be null");
        }
        logger.debug("Saving car: {} {}", car.getMake(), car.getModel());
        return carRepository.save(car);
    }

    @Transactional
    public void resetAllCarsAvailability() {
        logger.info("Resetting availability status for all cars");
        try {
            List<Car> cars = carRepository.findAll();
            for (Car car : cars) {
                if (!car.isAvailabilityStatus()) {
                    car.setAvailabilityStatus(true);
                    carRepository.save(car);
                }
            }
            logger.info("Successfully reset availability status for {} cars", cars.size());
        } catch (Exception e) {
            logger.error("Error resetting car availability status: {}", e.getMessage());
            throw new RuntimeException("Failed to reset car availability status", e);
        }
    }

    @Transactional
    public void ensureAllCarsVisible() {
        logger.info("Ensuring all cars are visible");
        try {
            List<Car> cars = carRepository.findAll();
            boolean updatesNeeded = false;
            
            for (Car car : cars) {
                if (!car.isAvailabilityStatus()) {
                    car.setAvailabilityStatus(true);
                    updatesNeeded = true;
                }
            }
            
            if (updatesNeeded) {
                carRepository.saveAll(cars);
                logger.info("Updated visibility status for cars that were not visible");
            } else {
                logger.info("All cars are already visible");
            }
        } catch (Exception e) {
            logger.error("Error ensuring cars visibility: {}", e.getMessage());
            throw new RuntimeException("Failed to ensure cars visibility", e);
        }
    }

    @Transactional
    public void removeDuplicateCars() {
        logger.info("Starting duplicate car removal process");
        try {
            List<Car> allCars = carRepository.findAll();
            Map<String, Car> uniqueCars = new HashMap<>();
            List<Car> duplicatesToRemove = new java.util.ArrayList<>();

            // First pass: identify duplicates and keep the one with lowest ID
            for (Car car : allCars) {
                String key = car.getMake() + "|" + car.getModel() + "|" + car.getOwner();
                if (uniqueCars.containsKey(key)) {
                    Car existing = uniqueCars.get(key);
                    if (car.getCarId() < existing.getCarId()) {
                        // If current car has lower ID, mark the existing one for removal
                        duplicatesToRemove.add(existing);
                        uniqueCars.put(key, car);
                    } else {
                        // Current car has higher ID, mark it for removal
                        duplicatesToRemove.add(car);
                    }
                } else {
                    uniqueCars.put(key, car);
                }
            }

            // Delete all duplicates
            for (Car duplicate : duplicatesToRemove) {
                logger.info("Removing duplicate car: {} {} (ID: {})", 
                    duplicate.getMake(), duplicate.getModel(), duplicate.getCarId());
                carRepository.delete(duplicate);
            }

            logger.info("Successfully removed {} duplicate cars", duplicatesToRemove.size());
        } catch (Exception e) {
            logger.error("Error removing duplicate cars: {}", e.getMessage());
            throw new RuntimeException("Failed to remove duplicate cars", e);
        }
    }
} 