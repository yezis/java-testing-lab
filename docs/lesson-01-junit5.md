# 第 1 课：JUnit 5 单元测试入门

## 本课目标

掌握最基础的 Java 单元测试写法：

- 如何写一个测试类
- 如何使用 `@Test`
- 如何断言返回值
- 如何测试异常
- 如何运行测试

## 示例业务

当前项目中有一个简单的订单金额计算类：

- 商品单价必须大于 0
- 商品数量必须大于 0
- 总价 = 单价 * 数量

对应代码：

- `src/main/java/com/example/testinglab/order/OrderCalculator.java`
- `src/test/java/com/example/testinglab/order/OrderCalculatorTest.java`

## 运行测试

```bash
mvn test
```

## 练习

1. 给 `OrderCalculatorTest` 增加一个测试：当单价为 0 时，应该抛出异常。
2. 给 `OrderCalculatorTest` 增加一个测试：当数量为 0 时，应该抛出异常。
3. 尝试把多个正常金额计算场景改成参数化测试。

## 本课已完成内容

本课已经完成了 `OrderCalculator` 的基础单元测试，覆盖了以下场景：

- 正常计算订单总价
- `unitPrice = null` 时抛出异常
- `unitPrice = 0` 时抛出异常
- `quantity = -1` 时抛出异常
- `quantity = 0` 时抛出异常

最终测试运行结果：

```text
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 普通返回值测试

普通返回值测试的基本结构：

```java
@Test
void shouldCalculateTotalWhenPriceAndQuantityAreValid() {
    BigDecimal total = calculator.calculateTotal(new BigDecimal("19.90"), 3);

    assertThat(total).isEqualByComparingTo(new BigDecimal("59.70"));
}
```

关键点：

- `@Test` 表示这是一个 JUnit 5 测试方法。
- `assertThat(total)` 表示要对 `total` 做断言。
- `isEqualByComparingTo` 适合比较 `BigDecimal` 的数值大小，避免小数位差异导致误判。

## 异常测试

异常测试的基本结构：

```java
@Test
void shouldRejectZeroUnitPrice() {
    assertThatThrownBy(() -> calculator.calculateTotal(new BigDecimal("0"), 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("unitPrice must be greater than zero");
}
```

关键点：

- `assertThatThrownBy` 用来断言某段代码会抛出异常。
- `() -> calculator.calculateTotal(...)` 是 Lambda 表达式，表示把这段代码交给 AssertJ 执行和捕获异常。
- `isInstanceOf` 检查异常类型。
- `hasMessage` 检查异常消息。

## `@ValueSource` 参数化测试

当只有一个输入参数变化时，可以使用 `@ValueSource`。

```java
@ParameterizedTest
@ValueSource(ints = {-1, 0})
void shouldRejectInvalidQuantity(int quantity) {
    assertThatThrownBy(() -> calculator.calculateTotal(new BigDecimal("10"), quantity))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("quantity must be greater than zero");
}
```

这段测试会运行两次：

- 第 1 次：`quantity = -1`
- 第 2 次：`quantity = 0`

## `@CsvSource` 参数化测试

当多个输入参数一起变化时，可以使用 `@CsvSource`。

```java
@ParameterizedTest
@CsvSource({
        "19.90, 3, 59.70",
        "10.00, 2, 20.00",
        "0.99, 5, 4.95"
})
void shouldCalculateTotalWhenPriceAndQuantityAreValid(
        BigDecimal unitPrice,
        int quantity,
        BigDecimal expectedTotal
) {
    BigDecimal total = calculator.calculateTotal(unitPrice, quantity);

    assertThat(total).isEqualByComparingTo(expectedTotal);
}
```

这段测试会运行三次：

- 第 1 次：`unitPrice = 19.90`，`quantity = 3`，`expectedTotal = 59.70`
- 第 2 次：`unitPrice = 10.00`，`quantity = 2`，`expectedTotal = 20.00`
- 第 3 次：`unitPrice = 0.99`，`quantity = 5`，`expectedTotal = 4.95`

## 本课小结

本课掌握了 JUnit 5 单元测试的基础写法：

- 使用 `@Test` 编写普通测试
- 使用 AssertJ 的 `assertThat` 验证正常返回值
- 使用 AssertJ 的 `assertThatThrownBy` 验证异常
- 使用 `@ParameterizedTest` 编写参数化测试
- 使用 `@ValueSource` 传入单个参数
- 使用 `@CsvSource` 传入多列参数

后续课程会在这个项目上继续学习 Mockito，用 Mock 隔离外部依赖并测试更复杂的业务逻辑。
