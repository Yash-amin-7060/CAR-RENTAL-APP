package com.example.carrental.controller;

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
import com.example.carrental.model.Feedback;
import com.example.carrental.model.User;
import com.example.carrental.service.BookingService;
import com.example.carrental.service.CarService;
import com.example.carrental.service.CustomUserDetailsService;
import com.example.carrental.service.FeedbackService;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CarService carService;

    @Autowired
    private CustomUserDetailsService userService;

    @GetMapping("/create/{bookingId}")
    public String showFeedbackForm(@PathVariable Long bookingId,
                                 @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails userDetails,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to leave feedback");
            return "redirect:/login";
        }

        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("error", "Booking not found");
            return "redirect:/bookings/my-bookings";
        }

        if (!booking.getUser().getEmail().equals(userDetails.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized access");
            return "redirect:/bookings/my-bookings";
        }

        if (feedbackService.hasFeedbackForBooking(bookingId)) {
            redirectAttributes.addFlashAttribute("error", "You have already submitted feedback for this booking");
            return "redirect:/bookings/my-bookings";
        }

        // Auto-complete the booking if it's not already completed
        if (!"COMPLETED".equals(booking.getStatus())) {
            booking.setStatus("COMPLETED");
            bookingService.updateBooking(booking);
            
            // Make car available if not already
            Car car = booking.getCar();
            if (!car.isAvailabilityStatus()) {
                car.setAvailabilityStatus(true);
                carService.updateCar(car);
            }
        }

        model.addAttribute("booking", booking);
        model.addAttribute("car", booking.getCar());
        return "feedback/form";
    }

    @PostMapping("/submit")
    public String submitFeedback(@RequestParam("bookingId") Long bookingId,
                               @RequestParam("rating") Integer rating,
                               @RequestParam("comment") String comment,
                               @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            Booking booking = bookingService.getBookingById(bookingId);

            if (booking == null || !booking.getUser().equals(user)) {
                throw new RuntimeException("Invalid booking");
            }

            Feedback feedback = new Feedback();
            feedback.setUser(user);
            feedback.setCar(booking.getCar());
            feedback.setBooking(booking);
            feedback.setRating(rating);
            feedback.setComment(comment);

            feedbackService.createFeedback(feedback);

            redirectAttributes.addFlashAttribute("success", "Thank you for your feedback!");
            return "redirect:/bookings/my-bookings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error submitting feedback: " + e.getMessage());
            return "redirect:/feedback/create/" + bookingId;
        }
    }
} 