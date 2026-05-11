# Java 自动化测试学习进度交接

更新时间：2026-05-11

## 项目位置

```text
/Users/yezi/IdeaProjects/java-testing-lab
```

这是一个 Maven Java 17 项目，用来通过实际代码学习 Java 自动化测试。

## 本机环境

Java 17：

```text
/Users/yezi/.local/jdks/jdk-17.0.19+10/Contents/Home
```

IDEA 方便选择的 Java 17 软链接：

```text
/Users/yezi/Java17/Contents/Home
```

Maven：

```text
/Users/yezi/.local/apache-maven/apache-maven-3.9.15
```

运行测试：

```bash
mvn test
```

最近一次测试结果：

```text
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 主要文件

学习计划：

```text
docs/learning-plan.md
```

JUnit 5 笔记：

```text
docs/lesson-01-junit5.md
```

Mockito 笔记：

```text
docs/lesson-02-mockito.md
```

学习方式与改进约定：

```text
docs/learning-method.md
```

当前进度交接：

```text
docs/session-progress.md
```

业务代码：

```text
src/main/java/com/example/testinglab/order/OrderCalculator.java
src/main/java/com/example/testinglab/order/Order.java
src/main/java/com/example/testinglab/order/OrderService.java
src/main/java/com/example/testinglab/order/OrderRepository.java
src/main/java/com/example/testinglab/product/Product.java
src/main/java/com/example/testinglab/product/ProductRepository.java
```

测试代码：

```text
src/test/java/com/example/testinglab/order/OrderCalculatorTest.java
src/test/java/com/example/testinglab/order/OrderServiceTest.java
```

Mockito 环境配置：

```text
src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker
```

内容：

```text
mock-maker-subclass
```

这个配置用于避免当前 JDK 环境下 Mockito inline mock maker 挂载 Byte Buddy agent 失败。

## 已学习内容

### JUnit 5

已经学习并在项目中实践：

- `@Test`
- AssertJ `assertThat`
- AssertJ `assertThatThrownBy`
- `@ParameterizedTest`
- `@ValueSource`
- `@CsvSource`
- 正常场景测试
- 异常场景测试
- 参数化测试

### Mockito

已经学习并在 `OrderServiceTest` 中实践：

- `@ExtendWith(MockitoExtension.class)`
- `@Mock`
- `@InjectMocks`
- `when(...).thenReturn(...)`
- `verify(...)`
- `verify(..., never())`
- `verifyNoInteractions(...)` 的含义
- 参数匹配器：`any()`、`anyInt()`、`eq(...)`
- matcher 规则：同一次方法调用中，只要一个参数使用 matcher，其他参数也要使用 matcher
- `ArgumentCaptor`
- `thenAnswer(...)`
- `thenThrow(...)`
- `times(1)`
- `InOrder`
- `doAnswer(...).when(...)`
- `doThrow(...).when(...)`

## 当前业务代码含义

`OrderService.createOrder(productId, quantity)` 的流程：

```text
1. 根据 productId 查询商品
2. 商品不存在时抛出 IllegalArgumentException("product not found")
3. 库存不足时抛出 IllegalArgumentException("product stock is not enough")
4. 调用 OrderCalculator.calculateTotal(...) 计算总价
5. 创建 Order 对象
6. 调用 OrderRepository.save(order) 保存订单
7. 返回保存后的订单
```

## 当前测试覆盖

`OrderCalculatorTest` 覆盖：

- 正常金额计算
- 数量为 0
- 价格为 null
- 数量为负数
- 参数化测试

`OrderServiceTest` 覆盖：

- 商品存在且库存足够时，可以创建订单
- 创建订单成功时，会保存订单
- 使用 `ArgumentCaptor` 验证保存的订单内容
- 使用 `InOrder` 验证调用顺序：先查商品，再计算金额，最后保存订单
- 商品不存在时，抛出 `product not found`
- 商品库存不足时，抛出 `product stock is not enough`
- 异常场景下不调用 `OrderCalculator.calculateTotal(...)`
- 保存订单失败时，抛出 `save order failed`
- 保存成功时使用 `doAnswer(...)` 模拟 Repository 返回传入的订单
- 保存失败时使用 `doThrow(...)` 模拟 Repository 抛异常

## 当前代码状态

当前 `OrderServiceTest` 中：

- `ProductRepository` 是 `@Mock`
- `OrderCalculator` 仍然是 `@Mock`
- `OrderRepository` 是 `@Mock`
- 已经开始学习 `Spy`，但还没有正式把 `OrderCalculator` 改成 `@Spy`

当前测试代码里还有两段旧写法注释，建议后续清理：

```java
//        when(orderRepository.save(any(Order.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
```

```java
//        when(orderRepository.save(any(Order.class))).thenThrow(new IllegalStateException("save order failed"));
```

## 下一步学习任务

下一步继续 Mockito 的 `Spy`：

1. 在 `OrderServiceTest` 中导入：

```java
import org.mockito.Spy;
```

2. 把：

```java
@Mock
private OrderCalculator orderCalculator;
```

改成：

```java
@Spy
private OrderCalculator orderCalculator = new OrderCalculator();
```

3. 在成功创建订单测试中删除金额计算 Stub：

```java
when(orderCalculator.calculateTotal(any(), eq(2)))
        .thenReturn(new BigDecimal("119.80"));
```

原因：`Spy` 默认会调用 `OrderCalculator` 的真实计算逻辑，`59.90 * 2` 本来就是 `119.80`。

4. 在保存失败测试中，把金额计算 Stub 改成更适合 Spy 的写法：

```java
doReturn(new BigDecimal("119.80"))
        .when(orderCalculator)
        .calculateTotal(any(), eq(2));
```

5. 运行：

```bash
mvn test
```

预期仍然全部通过。

## 后续学习路线

Mockito 后续还可以继续学习：

- `Spy` 深入
- `void` 方法测试
- strict stubbing
- 常见 Mockito 错误
- BDDMockito
- static mock
- constructor mock
- deep stubs
- lenient stubbing

之后进入：

- Spring Boot 测试
- 集成测试
- Testcontainers
- API 自动化测试
- 属性测试与 Fuzzing
  - jqwik
  - `@Property`
  - `@ForAll`
  - 生成器
  - shrinking
  - Jazzer / JQF 概念
