package com.demo.fileUpload.controller;

import com.demo.fileUpload.model.Order;
import com.demo.fileUpload.repository.OrderRepository;
import com.demo.fileUpload.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @GetMapping("/all")
    public List<Order> getAllSequences(){
        return orderService.getAllSequence();
    }

    @PostMapping("/save")
    public String saveSequence(@RequestBody Order order){
        return orderService.setSequence(order);
    }

}
