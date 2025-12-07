package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.data.model.Order;
import com.mb.livedataservice.data.model.OrderItem;
import com.mb.livedataservice.data.repository.OrderItemRepository;
import com.mb.livedataservice.data.repository.OrderRepository;
import com.mb.livedataservice.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/customer/{customerName}")
    public List<Order> getOrdersByCustomer(@PathVariable String customerName) {
        return orderRepository.findByCustomerName(customerName);
    }

    @GetMapping("/recent")
    public List<Order> getRecentOrdersByStatus(@RequestParam OrderStatus status,
                                               @RequestParam LocalDateTime since) {
        return orderRepository.findByStatusAndOrderDateAfter(status, since);
    }

    @GetMapping("/by-coffee")
    public List<Order> getOrdersByCoffee(@RequestParam String coffeeName) {
        return orderRepository.findOrdersByCoffeeName(coffeeName);
    }

    @GetMapping("/{orderId}/items")
    public List<OrderItem> getOrderItems(@PathVariable Long orderId) {
        return orderItemRepository.findOrderItemsWithCoffeeDetails(orderId);
    }
}
