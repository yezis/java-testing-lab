package com.example.testinglab.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderCalculatorTest {

    private final OrderCalculator calculator = new OrderCalculator();

    @ParameterizedTest
    @CsvSource({
            "19.9, 3, 59.7",
            "10, 2, 20",
            "0.99, 5, 4.95"
    })
    void shouldCalculateTotalWhenPriceAndQuantityAreValid(BigDecimal unitPrice, int quantity, BigDecimal expectedTotal) {
        BigDecimal total = calculator.calculateTotal(unitPrice, quantity);

        assertThat(total).isEqualByComparingTo(expectedTotal);
    }

    @Test
    void shouldRejectNullUnitPrice() {
        assertThatThrownBy(() -> calculator.calculateTotal(null, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unitPrice must not be null");
    }

    @Test
    void shouldRejectZeroUnitPrice() {
        assertThatThrownBy(() -> calculator.calculateTotal(new BigDecimal("0"), 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unitPrice must be greater than zero");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void shouldRejectInvalidQuantity(int quantity) {
        assertThatThrownBy(() -> calculator.calculateTotal(new BigDecimal("10"), quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity must be greater than zero");
    }

}