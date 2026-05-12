package com.example.testinglab.order.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderCalculatorSpyTest {

    @Spy
    private OrderCalculator orderCalculator = new OrderCalculator();

    @Test
    void shouldCallRealMethodByDefaultWhenUsingSpy() {
        BigDecimal total = orderCalculator.calculateTotal(new BigDecimal("59.90"), 2);

        assertThat(total).isEqualByComparingTo(new BigDecimal("119.80"));
    }


    @Test
    void shouldUseStubbedValueWhenSpyMethodIsStubbed() {
        doReturn(new BigDecimal("100.00")).when(orderCalculator).calculateTotal(any(), eq(2));

        BigDecimal total = orderCalculator.calculateTotal(new BigDecimal("59.90"), 2);

        assertThat(total).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldThrowExceptionWhenStubbingSpyWithWhenAndMatcher() {
        assertThatThrownBy(() -> when(orderCalculator.calculateTotal(any(), eq(2))).thenReturn(new BigDecimal("100.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unitPrice must not be null");
    }


}
