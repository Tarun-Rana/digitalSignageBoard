package com.demo.fileUpload.repository;

import com.demo.fileUpload.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order,String> {
}
