package com.example.testinglab.order;

import com.example.testinglab.product.Product;
import com.example.testinglab.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderCalculator orderCalculator;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderWhenProductExistsAndStockIsEnough() {
        Product product = new Product("p-1001", "Java Testing Book", new BigDecimal("59.90"), 10);

        when(productRepository.findById("p-1001")).thenReturn(Optional.of(product));
        when(orderCalculator.calculateTotal(any(), eq(2)))
                .thenReturn(new BigDecimal("119.80"));
//        when(orderRepository.save(any(Order.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));

        doAnswer(invocation -> invocation.getArgument(0))
                .when(orderRepository)
                .save(any(Order.class));


        Order order = orderService.createOrder("p-1001", 2);

        assertThat(order.getProductId()).isEqualTo("p-1001");
        assertThat(order.getProductName()).isEqualTo("Java Testing Book");
        assertThat(order.getQuantity()).isEqualTo(2);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("119.80"));

        InOrder inOrder = inOrder(productRepository, orderCalculator, orderRepository);

        inOrder.verify(productRepository).findById("p-1001");
        inOrder.verify(orderCalculator).calculateTotal(new BigDecimal("59.90"), 2);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        inOrder.verify(orderRepository, times(1)).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getProductId()).isEqualTo("p-1001");
        assertThat(savedOrder.getProductName()).isEqualTo("Java Testing Book");
        assertThat(savedOrder.getQuantity()).isEqualTo(2);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("119.80"));
    }


    @Test
    void shouldRejectCreateOrderWhenProductDoesNotExist() {
        when(productRepository.findById("p-404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder("p-404", 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("product not found");

        verify(productRepository).findById("p-404");
        verify(orderCalculator, never()).calculateTotal(any(), anyInt());
    }

    @Test
    void shouldRejectCreateOrderWhenProductStockIsNotEnough() {
        Product product = new Product("p-1001", "Java Testing Book", new BigDecimal("59.90"), 1);

        when(productRepository.findById("p-1001")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder("p-1001", 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("product stock is not enough");

        verify(productRepository).findById("p-1001");
        verify(orderCalculator, never()).calculateTotal(any(), anyInt());
    }

    @Test
    void shouldThrowExceptionWhenSaveOrderFailed() {
        Product product = new Product("p-1001", "Java Testing Book", new BigDecimal("59.90"), 10);

        when(productRepository.findById("p-1001")).thenReturn(Optional.of(product));
        when(orderCalculator.calculateTotal(any(), eq(2))).thenReturn(new BigDecimal("119.80"));
//        when(orderRepository.save(any(Order.class))).thenThrow(new IllegalStateException("save order failed"));
        doThrow(new IllegalStateException("save order failed")).when(orderRepository).save(any(Order.class));

        assertThatThrownBy(() -> orderService.createOrder("p-1001", 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("save order failed");

        verify(productRepository).findById("p-1001");
        verify(orderCalculator).calculateTotal(new BigDecimal("59.90"), 2);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

}
