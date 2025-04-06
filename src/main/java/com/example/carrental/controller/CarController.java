package com.example.carrental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.carrental.model.Car;
import com.example.carrental.service.CarService;

@Controller
@RequestMapping("/cars")
public class CarController {
    
    @Autowired
    private CarService carService;
    
    @GetMapping
    public String listCars(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            // Show all cars for authenticated users
            model.addAttribute("cars", carService.getAllCars());
        } else {
            // Show only available cars for unauthenticated users
            model.addAttribute("cars", carService.getAllAvailableCars());
        }
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
    
    @GetMapping("/add")
    public String showAddCarForm(Model model) {
        model.addAttribute("car", new Car());
        return "cars/form";
    }
    
    @PostMapping("/add")
    public String addCar(@ModelAttribute Car car, RedirectAttributes redirectAttributes) {
        carService.saveCar(car);
        redirectAttributes.addFlashAttribute("success", "Car added successfully!");
        return "redirect:/cars";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditCarForm(@PathVariable Long id, Model model) {
        Car car = carService.getCarById(id);
        if (car == null) {
            return "redirect:/cars?error=Car+not+found";
        }
        model.addAttribute("car", car);
        return "cars/form";
    }
    
    @PostMapping("/edit/{id}")
    public String updateCar(@PathVariable Long id, @ModelAttribute Car car, RedirectAttributes redirectAttributes) {
        car.setCarId(id);
        carService.saveCar(car);
        redirectAttributes.addFlashAttribute("success", "Car updated successfully!");
        return "redirect:/cars";
    }
    
    @PostMapping("/delete/{id}")
    public String deleteCar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        carService.deleteCar(id);
        redirectAttributes.addFlashAttribute("success", "Car deleted successfully!");
        return "redirect:/cars";
    }

    @PostMapping("/remove-duplicates")
    public String removeDuplicates(RedirectAttributes redirectAttributes) {
        try {
            carService.removeDuplicateCars();
            redirectAttributes.addFlashAttribute("success", "Successfully removed duplicate cars!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove duplicate cars: " + e.getMessage());
        }
        return "redirect:/cars";
    }
} 