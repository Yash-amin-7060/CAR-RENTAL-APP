package com.example.carrental.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.carrental.model.Booking;
import com.example.carrental.model.Car;
import com.example.carrental.model.User;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.repository.CarRepository;

@Service
public class BookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private CarRepository carRepository;
    
    @Autowired
    private CarService carService;
    
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking createBooking(Booking booking) {
        logger.info("Creating new booking");
        
        validateBooking(booking);
        
        // Get car from database with explicit error handling
        Car car = getAndValidateCar(booking.getCar().getCarId());
        
        // Calculate total price with validation
        calculateAndValidatePrice(booking);
        
        // Update car availability in the same transaction
        try {
            logger.info("Updating car availability status to false for car ID: {}", car.getCarId());
            car.setAvailabilityStatus(false);
            carRepository.save(car);
        } catch (Exception e) {
            logger.error("Error updating car availability: {}", e.getMessage());
            throw new RuntimeException("Error updating car availability: " + e.getMessage());
        }
        
        // Set booking status
        booking.setStatus("PENDING");
        
        // Save booking with explicit error handling
        try {
            logger.info("Saving booking to database");
            Booking savedBooking = bookingRepository.save(booking);
            logger.info("Booking saved successfully with ID: {}", savedBooking.getId());
            return savedBooking;
        } catch (Exception e) {
            logger.error("Error saving booking: {}", e.getMessage());
            logger.error("Full stack trace:", e);
            
            // Rollback car availability status
            rollbackCarAvailability(car);
            
            throw new RuntimeException("Failed to save booking: " + e.getMessage());
        }
    }

    private void validateBooking(Booking booking) {
        if (booking == null) {
            logger.error("Booking object is null");
            throw new IllegalArgumentException("Booking cannot be null");
        }
        
        if (booking.getCar() == null || booking.getCar().getCarId() == null) {
            logger.error("Car information is missing or invalid");
            throw new IllegalArgumentException("Valid car information is required");
        }
        
        if (booking.getUser() == null || booking.getUser().getPersonId() == null) {
            logger.error("User information is missing or invalid");
            throw new IllegalArgumentException("Valid user information is required");
        }
        
        if (booking.getStartDate() == null || booking.getEndDate() == null) {
            logger.error("Booking dates are missing");
            throw new IllegalArgumentException("Both start and end dates are required");
        }
        
        if (booking.getEndDate().isBefore(booking.getStartDate())) {
            logger.error("End date {} is before start date {}", booking.getEndDate(), booking.getStartDate());
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }

    private Car getAndValidateCar(Long carId) {
        try {
            Car car = carRepository.findById(carId)
                    .orElseThrow(() -> {
                        logger.error("Car not found with ID: {}", carId);
                        return new RuntimeException("Car not found");
                    });
            
            if (!car.isAvailabilityStatus()) {
                logger.error("Car {} {} (ID: {}) is not available", car.getMake(), car.getModel(), car.getCarId());
                throw new RuntimeException("Selected car is not available for booking");
            }
            
            return car;
        } catch (Exception e) {
            logger.error("Error fetching car: {}", e.getMessage());
            throw new RuntimeException("Error fetching car details: " + e.getMessage());
        }
    }

    private void calculateAndValidatePrice(Booking booking) {
        if (booking.getTotalPrice() == null) {
            booking.calculateTotalPrice();
        }
        
        if (booking.getTotalPrice() == null || booking.getTotalPrice() <= 0) {
            logger.error("Invalid total price calculated: {}", booking.getTotalPrice());
            throw new IllegalStateException("Invalid total price for booking");
        }
    }

    private void rollbackCarAvailability(Car car) {
        try {
            logger.info("Rolling back car availability status for car ID: {}", car.getCarId());
            car.setAvailabilityStatus(true);
            carRepository.save(car);
        } catch (Exception rollbackEx) {
            logger.error("Error rolling back car availability: {}", rollbackEx.getMessage());
            // Continue with the original exception
        }
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUser(User user) {
        if (user == null) {
            logger.error("Cannot get bookings for null user");
            throw new IllegalArgumentException("User cannot be null");
        }
        
        logger.info("Fetching bookings for user: {}", user.getEmail());
        try {
            List<Booking> bookings = bookingRepository.findByUser(user);
            if (bookings == null) {
                return List.of(); // Return empty list instead of null
            }
            logger.info("Found {} bookings for user {}", bookings.size(), user.getEmail());
            return bookings;
        } catch (Exception e) {
            logger.error("Error fetching bookings for user {}: {}", user.getEmail(), e.getMessage());
            logger.error("Full stack trace:", e);
            return List.of(); // Return empty list on error
        }
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!"PENDING".equals(booking.getStatus())) {
            throw new RuntimeException("Only pending bookings can be cancelled");
        }
        
        // Update car availability
        Car car = booking.getCar();
        car.setAvailabilityStatus(true);
        carRepository.save(car);
        
        // Update booking status
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        
        logger.info("Successfully cancelled booking ID: {} for car: {} {}", 
            id, car.getMake(), car.getModel());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void resetUserBookings(User user) {
        if (user == null) {
            logger.error("Cannot reset bookings for null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        logger.info("Resetting bookings for user: {}", user.getEmail());
        try {
            // First, get all pending bookings for the user
            List<Booking> pendingBookings = bookingRepository.findByUser(user).stream()
                .filter(b -> "PENDING".equals(b.getStatus()))
                .toList();

            // Reset car availability for all pending bookings
            for (Booking booking : pendingBookings) {
                Car car = booking.getCar();
                if (car != null) {
                    car.setAvailabilityStatus(true);
                    carRepository.save(car);
                    logger.info("Reset availability for car {} {} (ID: {})", 
                        car.getMake(), car.getModel(), car.getCarId());
                }
            }

            // Cancel all pending bookings in one operation
            bookingRepository.cancelPendingBookingsForUser(user);
            
            // Reset all cars' availability
            carService.resetAllCarsAvailability();
            
            logger.info("Successfully reset {} bookings for user {}", 
                pendingBookings.size(), user.getEmail());
        } catch (Exception e) {
            logger.error("Error resetting bookings for user {}: {}", user.getEmail(), e.getMessage());
            logger.error("Full stack trace:", e);
            throw new RuntimeException("Failed to reset user bookings: " + e.getMessage(), e);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void resetAllPendingBookings() {
        logger.info("Resetting all pending bookings");
        try {
            List<Booking> pendingBookings = bookingRepository.findByStatus("PENDING");
            
            for (Booking booking : pendingBookings) {
                // Reset car availability
                Car car = booking.getCar();
                car.setAvailabilityStatus(true);
                carRepository.save(car);
                
                // Cancel the booking
                booking.setStatus("CANCELLED");
                bookingRepository.save(booking);
                
                logger.info("Reset booking {} for car {} {}", 
                    booking.getId(), car.getMake(), car.getModel());
            }
            
            // Reset all cars' availability
            carService.resetAllCarsAvailability();
            
            logger.info("Successfully reset all pending bookings");
        } catch (Exception e) {
            logger.error("Error resetting pending bookings: {}", e.getMessage());
            throw new RuntimeException("Failed to reset pending bookings", e);
        }
    }
} 