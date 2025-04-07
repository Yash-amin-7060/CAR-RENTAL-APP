package com.example.carrental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.carrental.model.Car;
import com.example.carrental.service.CarService;

@Controller
@RequestMapping("/cars")
public class CarController {
    
    @Autowired
    private CarService carService;
    
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
} 