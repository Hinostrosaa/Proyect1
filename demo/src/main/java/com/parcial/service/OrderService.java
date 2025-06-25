package com.parcial.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.parcial.model.Order;
import com.parcial.model.Product;
import com.parcial.repository.OrderRepository;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public Order createOrder(Order order) {
        if (order.getProducts() == null || order.getProducts().isEmpty()) {
        throw new IllegalArgumentException("Products cannot be null or empty");
    }
    // Calcular el total de la orden
    double total = order.getProducts().stream()
                        .mapToDouble(Product::getPrice)
                        .sum();
    order.setTotal(total); // Establecer el total en la orden

    // Guardar la orden en la base de datos
    return orderRepository.save(order);
}


    public List<Order> getOrders(String client, String status) {
        if (client != null) {
            return orderRepository.findByClient(client);
        } else if (status != null) {
            return orderRepository.findByStatus(status);
        } else {
            return orderRepository.findAll();
        }
    }
}

