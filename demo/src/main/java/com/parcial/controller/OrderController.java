package com.parcial.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.parcial.model.Order;
import com.parcial.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody @Valid Order order) {
        try {
            Order createdOrder = orderService.createOrder(order); // Llama al servicio para crear la orden
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED); // Devuelve la respuesta con el código 201
        } catch (Exception e) {
            // En caso de error, devolver un 500 con el mensaje de error
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(@RequestParam(required = false) String client, @RequestParam(required = false) String status) {
        return new ResponseEntity<>(orderService.getOrders(client, status), HttpStatus.OK);
    }
}
