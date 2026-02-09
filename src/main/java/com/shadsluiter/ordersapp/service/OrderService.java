package com.shadsluiter.ordersapp.service;

import com.shadsluiter.ordersapp.data.OrderRepository;
import com.shadsluiter.ordersapp.data.OrderRepositoryInterface;
import com.shadsluiter.ordersapp.models.Converters;
import com.shadsluiter.ordersapp.models.OrderEntity;
import com.shadsluiter.ordersapp.models.OrderModel;

import ch.qos.logback.core.pattern.Converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service is a layer between the controller and the repository.
 * Here is where the business logic goes.
 * Does not interact with the database directly, but uses the repository for that.
 */
@Service
public class OrderService {

    private final OrderRepositoryInterface orderRepository; 
     

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderModel> findAll() {
        List<OrderEntity> orderEntities = orderRepository.findAll(); 
        return  Converters.convertToModels(orderEntities);
    }

    public List<OrderModel> findByCustomerid(String customerid) {
        List<OrderEntity> orderEntities = orderRepository.findByCustomerid(Long.valueOf(customerid));
        return Converters.convertToModels(orderEntities);
    }

    public List<OrderModel> findByNotes(String searchString) {
        List<OrderEntity> orderEntities = orderRepository.findByNote(searchString); 
        return Converters.convertToModels(orderEntities);
    }
 

    public OrderModel save(OrderModel order) {
        OrderEntity orderEntity = Converters.orderModelToOrderEntity(order);
        OrderEntity savedOrder = orderRepository.save(orderEntity);
        return Converters.orderEntityToOrderModel(savedOrder);
    }

    public void delete(String id) {
        orderRepository.deleteById(Long.valueOf(id));
    }

    public OrderModel updateOrder(String id, OrderModel order) {
        order.setId(id);
        return save(order);
    }

    public OrderModel findById(String id) {
        OrderEntity orderEntity = orderRepository.findById(Long.valueOf(id));
        return Converters.orderEntityToOrderModel(orderEntity);
    }
 
 


}
