package com.example.carrental.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.carrental.model.Booking;
import com.example.carrental.model.Car;
import com.example.carrental.model.User;
import com.example.carrental.service.BookingService;
import com.example.carrental.service.CarService;
import com.example.carrental.service.CustomUserDetailsService;
import com.example.carrental.service.CustomUserDetailsService.CustomUserDetails;

@Controller
@RequestMapping("/bookings")
public class BookingController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CarService carService;

    @Autowired
    private CustomUserDetailsService userService;

    @GetMapping("/create/{carId}")
    public String showBookingForm(@PathVariable Long carId, Model model) {
        logger.info("Showing booking form for car ID: {}", carId);
        try {
            Car car = carService.getCarById(carId);
            if (car == null) {
                logger.error("Car not found with ID: {}", carId);
                return "redirect:/cars?error=Car+not+found";
            }
            logger.info("Found car: {} {}", car.getMake(), car.getModel());
            model.addAttribute("car", car);
            model.addAttribute("booking", new Booking());
            return "bookings/form";
        } catch (Exception e) {
            logger.error("Error showing booking form for car ID {}: {}", carId, e.getMessage(), e);
            return "redirect:/cars?error=Error+loading+booking+form";
        }
    }

    @GetMapping("/confirm/{bookingId}")
    public String showConfirmation(@PathVariable Long bookingId,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        logger.info("Showing confirmation page for booking ID: {}", bookingId);

        if (userDetails == null) {
            logger.error("User not authenticated");
            redirectAttributes.addFlashAttribute("error", "Please log in to view booking confirmation");
            return "redirect:/login";
        }

        try {
            // Get the booking
            Booking booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                logger.error("Booking not found with ID: {}", bookingId);
                redirectAttributes.addFlashAttribute("error", "Booking not found");
                return "redirect:/bookings/my-bookings";
            }

            // Verify the booking belongs to the current user
            User user = userService.getUserByEmail(userDetails.getUsername());
            if (!booking.getUser().equals(user)) {
                logger.error("Unauthorized access to booking ID: {}", bookingId);
                redirectAttributes.addFlashAttribute("error", "Unauthorized to view this booking");
                return "redirect:/bookings/my-bookings";
            }

            model.addAttribute("booking", booking);
            return "bookings/confirm";

        } catch (Exception e) {
            logger.error("Error showing confirmation for booking ID {}: {}", bookingId, e.getMessage());
            logger.error("Full stack trace:", e);
            redirectAttributes.addFlashAttribute("error", "Error loading booking confirmation");
            return "redirect:/bookings/my-bookings";
        }
    }

    @PostMapping("/create")
    public String createBooking(@RequestParam("car") Long carId,
                              @RequestParam("startDate") String startDateStr,
                              @RequestParam("endDate") String endDateStr,
                              @RequestParam(value = "notes", required = false) String notes,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        logger.info("Creating booking for car ID: {} from {} to {}", carId, startDateStr, endDateStr);
        
        // Check authentication
        if (userDetails == null) {
            logger.error("User not authenticated");
            redirectAttributes.addFlashAttribute("error", "Please log in to create a booking");
            return "redirect:/login";
        }

        // Validate input parameters
        if (carId == null) {
            logger.error("Car ID is null");
            redirectAttributes.addFlashAttribute("error", "Invalid car selection");
            return "redirect:/cars";
        }

        if (startDateStr == null || startDateStr.trim().isEmpty() || 
            endDateStr == null || endDateStr.trim().isEmpty()) {
            logger.error("Start date or end date is empty");
            redirectAttributes.addFlashAttribute("error", "Please select both start and end dates");
            return "redirect:/bookings/create/" + carId;
        }

        try {
            // Parse dates with explicit error handling
            LocalDate startDate;
            LocalDate endDate;
            try {
                startDate = LocalDate.parse(startDateStr);
                endDate = LocalDate.parse(endDateStr);
            } catch (DateTimeParseException e) {
                logger.error("Error parsing dates: {} - {}", startDateStr, endDateStr);
                redirectAttributes.addFlashAttribute("error", "Invalid date format. Please use YYYY-MM-DD format.");
                return "redirect:/bookings/create/" + carId;
            }

            // Validate dates
            LocalDate today = LocalDate.now();
            if (startDate.isBefore(today)) {
                logger.error("Start date {} is before today {}", startDate, today);
                redirectAttributes.addFlashAttribute("error", "Start date cannot be in the past");
                return "redirect:/bookings/create/" + carId;
            }

            if (endDate.isBefore(startDate)) {
                logger.error("End date {} is before start date {}", endDate, startDate);
                redirectAttributes.addFlashAttribute("error", "End date cannot be before start date");
                return "redirect:/bookings/create/" + carId;
            }

            // Get the car with explicit error handling
            Car car;
            try {
                car = carService.getCarById(carId);
                if (car == null) {
                    throw new RuntimeException("Car not found");
                }
            } catch (Exception e) {
                logger.error("Error fetching car with ID {}: {}", carId, e.getMessage());
                redirectAttributes.addFlashAttribute("error", "Selected car is not available");
                return "redirect:/cars";
            }

            // Verify car availability
            if (!car.isAvailabilityStatus()) {
                logger.error("Car {} is not available", carId);
                redirectAttributes.addFlashAttribute("error", "Selected car is not available for booking");
                return "redirect:/cars";
            }

            // Get the user with explicit error handling
            User user;
            try {
                user = userService.getUserByEmail(userDetails.getUsername());
                if (user == null) {
                    throw new RuntimeException("User not found");
                }
            } catch (Exception e) {
                logger.error("Error fetching user: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("error", "Error retrieving user information");
                return "redirect:/login";
            }

            // Create and populate booking object
            Booking booking = new Booking();
            booking.setCar(car);
            booking.setUser(user);
            booking.setStartDate(startDate);
            booking.setEndDate(endDate);
            booking.setNotes(notes != null ? notes.trim() : null);
            booking.setStatus("PENDING");

            // Calculate total price
            try {
                booking.calculateTotalPrice();
                if (booking.getTotalPrice() == null || booking.getTotalPrice() <= 0) {
                    throw new IllegalStateException("Invalid total price calculated");
                }
            } catch (Exception e) {
                logger.error("Error calculating total price: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("error", "Error calculating booking price");
                return "redirect:/bookings/create/" + carId;
            }

            // Save the booking with explicit error handling
            try {
                Booking savedBooking = bookingService.createBooking(booking);
                logger.info("Booking created successfully with ID: {}", savedBooking.getId());
                return "redirect:/bookings/confirm/" + savedBooking.getId();
            } catch (Exception e) {
                logger.error("Error saving booking: {}", e.getMessage());
                logger.error("Booking save error:", e);
                redirectAttributes.addFlashAttribute("error", "Error creating booking: " + e.getMessage());
                return "redirect:/bookings/create/" + carId;
            }
        } catch (Exception e) {
            logger.error("Unexpected error in createBooking: {}", e.getMessage());
            logger.error("Full stack trace:", e);
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred");
            return "redirect:/cars";
        }
    }

    @GetMapping("/my-bookings")
    public String showMyBookings(@AuthenticationPrincipal CustomUserDetails userDetails, 
                               Model model, 
                               RedirectAttributes redirectAttributes) {
        logger.info("Accessing /my-bookings endpoint");
        
        // Check authentication
        if (userDetails == null) {
            logger.error("User not authenticated");
            redirectAttributes.addFlashAttribute("error", "Please log in to view your bookings");
            return "redirect:/login";
        }

        try {
            // Get user
            User user = userService.getUserByEmail(userDetails.getUsername());
            if (user == null) {
                logger.error("No user found for email: {}", userDetails.getUsername());
                redirectAttributes.addFlashAttribute("error", "User account not found");
                return "redirect:/login";
            }

            // Fetch bookings
            List<Booking> bookings = bookingService.getBookingsByUser(user);
            logger.info("Retrieved {} bookings for user {}", 
                bookings != null ? bookings.size() : 0, 
                userDetails.getUsername());

            // Add bookings to model (empty list if null)
            model.addAttribute("bookings", bookings != null ? bookings : List.of());
            
            return "bookings/my-bookings";
            
        } catch (Exception e) {
            logger.error("Error in showMyBookings: {}", e.getMessage());
            logger.error("Full stack trace:", e);
            model.addAttribute("bookings", List.of());
            model.addAttribute("error", "An error occurred while loading your bookings");
            return "bookings/my-bookings";
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, 
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        logger.info("Cancelling booking ID: {}", id);
        try {
            // Get the booking
            Booking booking = bookingService.getBookingById(id);
            if (booking == null) {
                throw new RuntimeException("Booking not found");
            }

            // Verify the booking belongs to the current user
            User user = userService.getUserByEmail(userDetails.getUsername());
            if (!booking.getUser().equals(user)) {
                throw new RuntimeException("Unauthorized to cancel this booking");
            }

            // Cancel the booking
            bookingService.cancelBooking(id);
            logger.info("Booking cancelled successfully");
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully!");
        } catch (Exception e) {
            logger.error("Error cancelling booking ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error cancelling booking: " + e.getMessage());
        }
        return "redirect:/bookings/my-bookings";
    }

    @GetMapping("/update-statuses")
    public String updateBookingStatuses(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        logger.info("Updating booking statuses");
        
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to view your bookings");
            return "redirect:/login";
        }
        
        try {
            bookingService.updateBookingStatuses();
            redirectAttributes.addFlashAttribute("success", "Booking statuses updated successfully!");
        } catch (Exception e) {
            logger.error("Error updating booking statuses: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error updating booking statuses: " + e.getMessage());
        }
        
        return "redirect:/bookings/my-bookings";
    }
    
    @PostMapping("/complete/{id}")
    public String completeBooking(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        logger.info("Manually completing booking ID: {}", id);
        
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to manage your bookings");
            return "redirect:/login";
        }
        
        try {
            // Get the booking
            Booking booking = bookingService.getBookingById(id);
            if (booking == null) {
                throw new RuntimeException("Booking not found");
            }
            
            // Verify the booking belongs to the current user
            User user = userService.getUserByEmail(userDetails.getUsername());
            if (!booking.getUser().equals(user)) {
                throw new RuntimeException("Unauthorized to complete this booking");
            }
            
            // Don't allow completing cancelled bookings
            if ("CANCELLED".equals(booking.getStatus())) {
                throw new RuntimeException("Cannot complete a cancelled booking");
            }
            
            // Update booking status
            booking.setStatus("COMPLETED");
            bookingService.updateBooking(booking);
            
            // Make car available again
            Car car = booking.getCar();
            car.setAvailabilityStatus(true);
            carService.updateCar(car);
            
            redirectAttributes.addFlashAttribute("success", "Booking marked as completed! You can now leave feedback.");
        } catch (Exception e) {
            logger.error("Error completing booking ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error completing booking: " + e.getMessage());
        }
        
        return "redirect:/bookings/my-bookings";
    }
} 