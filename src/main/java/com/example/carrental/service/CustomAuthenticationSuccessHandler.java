package com.example.carrental.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.carrental.model.User;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private CarService carService;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        logger.info("User {} successfully authenticated", authentication.getName());
        
        try {
            // Get the logged-in user
            User user = userDetailsService.getUserByEmail(authentication.getName());
            
            // Reset user's bookings
            bookingService.resetUserBookings(user);
            logger.info("Successfully reset bookings for user {}", user.getEmail());
            
            // Reset all cars' availability
            carService.resetAllCarsAvailability();
            logger.info("Successfully reset all cars' availability status");
            
            // Add login_success parameter to the redirect URL
            String targetUrl = determineTargetUrl(request, response);
            if (targetUrl.contains("?")) {
                targetUrl += "&login_success";
            } else {
                targetUrl += "?login_success";
            }
            
            // Redirect to the target URL
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            logger.error("Error resetting bookings on login: {}", e.getMessage());
            logger.error("Full stack trace:", e);
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
    
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        String targetUrl = super.determineTargetUrl(request, response);
        return targetUrl.equals("/") ? "/cars" : targetUrl;
    }
} 