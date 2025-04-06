package com.example.carrental.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomLogoutSuccessHandler.class);
    
    @Autowired
    private CarService carService;
    
    @Override
    public void onLogoutSuccess(HttpServletRequest request, 
                              HttpServletResponse response, 
                              Authentication authentication) throws IOException, ServletException {
        logger.info("User logged out successfully");
        
        try {
            // Reset all cars' availability
            carService.resetAllCarsAvailability();
            logger.info("Successfully reset all cars' availability status on logout");
            
        } catch (Exception e) {
            logger.error("Error resetting cars' availability on logout: {}", e.getMessage());
            logger.error("Full stack trace:", e);
        }
        
        // Set default target URL and redirect
        setDefaultTargetUrl("/cars");
        super.onLogoutSuccess(request, response, authentication);
    }
} 