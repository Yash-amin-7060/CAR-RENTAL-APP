package com.example.carrental.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.carrental.model.Car;
import com.example.carrental.model.Feedback;
import com.example.carrental.service.CarService;
import com.example.carrental.service.FeedbackService;

@Controller
@RequestMapping("/cars")
public class CarController {
    
    @Autowired
    private CarService carService;
    
    @Autowired
    private FeedbackService feedbackService;
    
    @GetMapping
    public String listCars(Model model) {
        model.addAttribute("cars", carService.getAllCars());
        return "cars/list";
    }
    
    @GetMapping("/{id}")
    public String showCar(@PathVariable Long id, Model model) {
        Car car = carService.getCarById(id);
        if (car == null) {
            return "redirect:/cars?error=Car+not+found";
        }
        model.addAttribute("car", car);
        return "cars/show";
    }
    
    @GetMapping("/{id}/feedback")
    public String showFeedback(@PathVariable Long id, Model model) {
        Car car = carService.getCarById(id);
        if (car == null) {
            return "redirect:/cars?error=Car+not+found";
        }
        
        List<Feedback> feedbacks = feedbackService.getFeedbacksByCarId(id);
        double averageRating = 0.0;
        
        if (!feedbacks.isEmpty()) {
            averageRating = feedbacks.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);
        }
        
        model.addAttribute("car", car);
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("averageRating", averageRating);
        
        return "cars/feedback";
    }
} 