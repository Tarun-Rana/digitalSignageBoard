package com.demo.fileUpload.controller;

import com.demo.fileUpload.model.Order;
import com.demo.fileUpload.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    @GetMapping("/getOrder/{id}")
    public Order getOrder(@PathVariable String id){
        return orderService.getSequence(id);
    }

}
