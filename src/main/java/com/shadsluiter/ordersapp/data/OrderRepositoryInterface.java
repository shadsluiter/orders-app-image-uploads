package com.shadsluiter.ordersapp.data;

import com.shadsluiter.ordersapp.models.OrderEntity;

import java.util.List;


/**
 * Interface for Order repository.
 * Defines CRUD operations for OrderEntity.
 * Implemented by OrderRepository class.
 */
public interface OrderRepositoryInterface {

    List<OrderEntity> findByCustomerid(Long customerid);
    List<OrderEntity> findAll();
    void deleteById(Long id);
    OrderEntity save(OrderEntity order);
    OrderEntity findById(Long id);
    boolean existsById(Long id);
    long count();
    void delete(OrderEntity order);
    void deleteAll();
    void deleteAll(Iterable<? extends OrderEntity> orders); 
    List<OrderEntity> saveAll(Iterable<OrderEntity> orders);
    List<OrderEntity> findByNote(String name);

}
