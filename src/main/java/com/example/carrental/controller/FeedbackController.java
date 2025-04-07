package com.example.carrental.controller;

import com.example.carrental.model.*;
import com.example.carrental.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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