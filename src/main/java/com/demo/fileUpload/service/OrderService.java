package com.demo.fileUpload.service;

import com.demo.fileUpload.model.Order;
import com.demo.fileUpload.repository.OrderRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public String setSequence (Order order){
        order.setDate(String.valueOf(LocalDateTime.now()));
        return orderRepository.save(order).getId();

    }

    public List<Order> getAllSequence (){
        return orderRepository.findAll();
    }

    public Order getSequence(String id){
        return orderRepository.findById(id).orElse(null);
    }

}
