package com.example.testinglab.order.interfaces.rest;

import com.example.testinglab.order.application.OrderCommandService;
import com.example.testinglab.order.application.OrderQueryService;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;

    public OrderController(OrderQueryService orderQueryService, OrderCommandService orderCommandService) {
        this.orderQueryService = orderQueryService;
        this.orderCommandService = orderCommandService;
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            @PathVariable("orderId")
            @Pattern(regexp = "o-\\d+", message = "orderId must match pattern o-{number}")
            String orderId
    ) {
        return OrderResponse.from(orderQueryService.findById(orderId));
    }

    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return OrderResponse.from(orderCommandService.createOrder(request.productId(), request.quantity()));
    }
}
