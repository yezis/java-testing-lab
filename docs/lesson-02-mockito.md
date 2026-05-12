# 第 2 课：Mockito 与 Service 层单元测试

## 本课目标

掌握 Service 层单元测试中最常用的 Mockito 写法：

- 理解 Mock 的作用
- 使用 `@Mock` 创建模拟对象
- 使用 `@InjectMocks` 创建被测试对象
- 使用 `when(...).thenReturn(...)` 准备依赖返回值
- 使用 `verify(...)` 验证依赖是否被调用
- 理解 `Mock` 和 `Spy` 的区别
- 测试 `void` 方法的调用和异常

## 示例业务

当前新增了一个订单创建服务：

- `src/main/java/com/example/testinglab/order/OrderService.java`
- `src/main/java/com/example/testinglab/order/Order.java`
- `src/main/java/com/example/testinglab/order/OrderRepository.java`
- `src/main/java/com/example/testinglab/product/ProductRepository.java`
- `src/test/java/com/example/testinglab/order/OrderServiceTest.java`

创建订单的业务流程：

1. 根据 `productId` 查询商品
2. 商品不存在时抛出异常
3. 商品库存不足时抛出异常
4. 调用 `OrderCalculator` 计算订单总价
5. 调用 `OrderRepository` 保存订单
6. 返回保存后的 `Order` 对象

## 为什么需要 Mock

`OrderService` 依赖两个对象：

- `ProductRepository`
- `OrderCalculator`
- `OrderRepository`

如果测试 `OrderService` 时真的去访问数据库，测试就会变慢、变复杂，也不再是纯粹的单元测试。

Mockito 可以创建这些依赖对象的替身。测试时我们只关心 `OrderService` 自己的逻辑，依赖对象返回什么由测试代码指定。

## 基础示例

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderCalculator orderCalculator;

    @InjectMocks
    private OrderService orderService;
}
```

含义：

- `@ExtendWith(MockitoExtension.class)`：启用 Mockito 对 JUnit 5 的支持。
- `@Mock`：创建一个模拟对象。
- `@InjectMocks`：创建被测试对象，并把 `@Mock` 标记的依赖注入进去。

## Stub：指定 Mock 返回值

```java
when(productRepository.findById("p-1001")).thenReturn(Optional.of(product));
```

含义：

当测试代码调用：

```java
productRepository.findById("p-1001")
```

Mockito 返回：

```java
Optional.of(product)
```

## Verify：验证依赖调用

```java
verify(productRepository).findById("p-1001");
verify(orderCalculator).calculateTotal(new BigDecimal("59.90"), 2);
```

含义：

验证 `OrderService` 在创建订单过程中确实调用了这些依赖方法。

## 异常场景测试

商品不存在时，`ProductRepository` 返回 `Optional.empty()`，`OrderService` 应该抛出异常：

```java
@Test
void shouldRejectCreateOrderWhenProductDoesNotExist() {
    when(productRepository.findById("p-404")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.createOrder("p-404", 2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("product not found");

    verify(productRepository).findById("p-404");
    verify(orderCalculator, never()).calculateTotal(any(), anyInt());
}
```

库存不足时，`OrderService` 应该在库存校验阶段停止，不应该继续调用金额计算器：

```java
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
```

## `verifyNoInteractions` 与 `never`

`verifyNoInteractions(orderCalculator)` 表示：

```text
orderCalculator 这个 Mock 对象没有任何方法被调用。
```

`verify(orderCalculator, never()).calculateTotal(any(), anyInt())` 表示：

```text
orderCalculator.calculateTotal(...) 这个方法没有被调用。
```

区别：

- `verifyNoInteractions(...)` 是对象级别验证，要求这个 Mock 完全没有交互。
- `verify(..., never())` 是方法级别验证，只要求某个方法没有被调用。

在真实项目中，如果一个依赖对象有多个方法，而测试只关心某一个方法不应被调用，优先使用 `never()`。

## 参数匹配器

Mockito 参数匹配器用于描述“参数怎样才算匹配”。

常见写法：

```java
any()
anyInt()
eq(2)
```

含义：

- `any()`：任意引用类型参数。
- `anyInt()`：任意 `int` 参数。
- `eq(2)`：参数必须等于 `2`。

示例：

```java
when(orderCalculator.calculateTotal(any(), eq(2)))
        .thenReturn(new BigDecimal("119.80"));
```

含义：

```text
只要调用 calculateTotal，并且第二个参数 quantity 等于 2，就返回 119.80。
```

Mockito 有一个重要规则：

```text
如果一个方法调用里有一个参数使用了 matcher，那么这个方法调用里的所有参数都要使用 matcher。
```

错误示例：

```java
verify(orderCalculator).calculateTotal(any(), 2);
```

正确示例：

```java
verify(orderCalculator).calculateTotal(any(), eq(2));
```

或者全部使用精确值：

```java
verify(orderCalculator).calculateTotal(new BigDecimal("59.90"), 2);
```

## `ArgumentCaptor` 参数捕获器

有时只验证“方法被调用了”还不够，还要验证“传进去的对象内容是否正确”。

例如 `OrderService` 创建订单后会调用：

```java
orderRepository.save(order);
```

这时可以用 `ArgumentCaptor` 捕获传给 `save` 方法的 `Order` 对象：

```java
ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
verify(orderRepository).save(orderCaptor.capture());

Order savedOrder = orderCaptor.getValue();
assertThat(savedOrder.getProductId()).isEqualTo("p-1001");
assertThat(savedOrder.getProductName()).isEqualTo("Java Testing Book");
assertThat(savedOrder.getQuantity()).isEqualTo(2);
assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("119.80"));
```

含义：

```text
1. 创建一个 Order 类型的参数捕获器
2. 验证 orderRepository.save(...) 被调用
3. 同时把 save(...) 接收到的参数捕获下来
4. 取出捕获到的 Order
5. 验证这个 Order 的字段是否正确
```

`ArgumentCaptor` 适合这种场景：

- 方法参数是复杂对象
- 不能只用 `eq(...)` 简单比较
- 需要逐个字段验证对象内容
- 需要确认 Service 组装出来的对象是否正确

当前测试里还使用了：

```java
when(orderRepository.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
```

含义：

```text
当保存任意 Order 对象时，直接把传入的那个 Order 原样返回。
```

这是测试 Repository `save` 方法时常见的写法，可以模拟“保存成功后返回订单对象”。

## `thenThrow`：让 Mock 抛出异常

前面我们用过：

```java
when(...).thenReturn(...);
```

它的意思是：当 Mock 的某个方法被调用时，返回指定结果。

`thenThrow(...)` 的意思是：当 Mock 的某个方法被调用时，抛出指定异常。

示例：

```java
when(orderRepository.save(any(Order.class)))
        .thenThrow(new IllegalStateException("save order failed"));
```

含义：

```text
只要 orderRepository.save(...) 被调用，就抛出 IllegalStateException。
```

这个写法常用于测试：

- 数据库保存失败
- 远程接口调用失败
- 缓存服务不可用
- 消息发送失败
- 文件写入失败

也就是说，`thenThrow(...)` 主要用来模拟“依赖对象出问题”的场景。

### 在当前业务里的测试思路

当前 `OrderService.createOrder(...)` 的最后一步是：

```java
return orderRepository.save(order);
```

如果保存订单时 `orderRepository.save(...)` 抛出异常，而 `OrderService` 没有捕获它，那么这个异常会继续向外抛出。

测试代码可以这样写：

```java
@Test
void shouldThrowExceptionWhenSaveOrderFailed() {
    Product product = new Product("p-1001", "Java Testing Book", new BigDecimal("59.90"), 10);

    when(productRepository.findById("p-1001")).thenReturn(Optional.of(product));
    when(orderCalculator.calculateTotal(any(), eq(2)))
            .thenReturn(new BigDecimal("119.80"));
    when(orderRepository.save(any(Order.class)))
            .thenThrow(new IllegalStateException("save order failed"));

    assertThatThrownBy(() -> orderService.createOrder("p-1001", 2))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("save order failed");

    verify(productRepository).findById("p-1001");
    verify(orderCalculator).calculateTotal(new BigDecimal("59.90"), 2);
    verify(orderRepository).save(any(Order.class));
}
```

这个测试验证的是：

```text
1. 商品存在
2. 库存足够
3. 金额计算成功
4. 保存订单失败
5. OrderService 把保存失败的异常继续抛出去
```

注意：这个测试不是在测试 `OrderRepository` 真的会不会失败，而是在测试 `OrderService` 面对“保存失败”时的行为。

## Mockito MockMaker 配置说明

当前项目新增了这个测试资源文件：

```text
src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker
```

文件内容：

```text
mock-maker-subclass
```

原因是 Mockito 5 默认可能使用 inline mock maker。inline mock maker 需要通过 Byte Buddy 动态挂载 Java agent。

在某些 JDK 或本机环境下，这个挂载过程可能失败，错误类似：

```text
Could not initialize inline Byte Buddy mock maker
Could not self-attach to current VM using external process
```

当前项目只是 Mock 普通接口和普通类，不需要 Mock `final` 类、静态方法或构造函数，所以使用更基础的 subclass mock maker 就够了。

这个配置的作用是告诉 Mockito：

```text
使用普通子类方式创建 Mock，不使用 inline Java agent 方式。
```

这属于测试环境配置，不改变业务代码，也不改变测试逻辑。

## 调用次数验证：`times`、`atLeastOnce`、`atMostOnce`

前面我们写过：

```java
verify(productRepository).findById("p-1001");
```

它其实等价于：

```java
verify(productRepository, times(1)).findById("p-1001");
```

也就是说，Mockito 默认验证的是：

```text
这个方法被调用了 1 次。
```

### `times(n)`：精确调用次数

```java
verify(productRepository, times(1)).findById("p-1001");
verify(orderRepository, times(1)).save(any(Order.class));
```

含义：

```text
findById(...) 必须刚好调用 1 次。
save(...) 必须刚好调用 1 次。
```

如果实际调用 0 次、2 次或更多，测试都会失败。

### `never()`：调用 0 次

```java
verify(orderCalculator, never()).calculateTotal(any(), anyInt());
```

`never()` 等价于：

```java
verify(orderCalculator, times(0)).calculateTotal(any(), anyInt());
```

但在表达“不应该调用”时，`never()` 更直观。

### `atLeastOnce()`：至少调用 1 次

```java
verify(productRepository, atLeastOnce()).findById("p-1001");
```

含义：

```text
findById(...) 至少调用过 1 次。
```

它不关心到底是 1 次、2 次还是更多次。

这个写法适合你只关心“确实发生过”，不关心精确次数的场景。

### `atMostOnce()`：最多调用 1 次

```java
verify(orderRepository, atMostOnce()).save(any(Order.class));
```

含义：

```text
save(...) 最多调用 1 次。
```

它允许调用 0 次或 1 次，但不能调用 2 次或更多。

### 什么时候该验证调用次数

不是每个测试都需要写 `times(...)`。

一般建议：

- 默认 `verify(...)` 足够表达时，不必强行写 `times(1)`。
- 当调用次数本身属于业务规则时，才重点验证次数。
- 如果要表达“不应该调用”，优先用 `never()`。
- 如果调用次数只是实现细节，过度验证会让测试变脆。

例如当前 `createOrder(...)` 成功时，订单应该只保存一次：

```java
verify(orderRepository, times(1)).save(any(Order.class));
```

这个验证是合理的，因为重复保存订单可能造成严重业务问题。

但下面这种验证价值就不一定高：

```java
verify(productRepository, times(1)).findById("p-1001");
```

它没有错，不过很多时候普通的 `verify(productRepository).findById("p-1001")` 已经足够。

## `InOrder`：验证调用顺序

有些业务不只关心“某个方法有没有被调用”，还关心“调用顺序是否正确”。

当前 `OrderService.createOrder(...)` 的正常流程是：

```text
1. productRepository.findById(...)
2. orderCalculator.calculateTotal(...)
3. orderRepository.save(...)
```

如果顺序变成先保存订单，再计算金额，业务就明显不合理。

Mockito 可以用 `InOrder` 验证调用顺序：

```java
InOrder inOrder = inOrder(productRepository, orderCalculator, orderRepository);

inOrder.verify(productRepository).findById("p-1001");
inOrder.verify(orderCalculator).calculateTotal(new BigDecimal("59.90"), 2);
inOrder.verify(orderRepository).save(any(Order.class));
```

含义：

```text
1. 创建一个顺序验证器，指定要观察哪些 Mock
2. 按期望顺序写 verify
3. Mockito 会检查这些调用是否真的按这个顺序发生
```

需要的静态导入：

```java
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
```

### `verify` 和 `InOrder.verify` 的区别

普通 `verify(...)` 只验证方法是否被调用：

```java
verify(orderRepository).save(any(Order.class));
```

它不关心这个调用发生在前面还是后面。

`inOrder.verify(...)` 验证的是：

```text
这个调用必须出现在当前顺序位置。
```

例如：

```java
inOrder.verify(productRepository).findById("p-1001");
inOrder.verify(orderRepository).save(any(Order.class));
```

表示：

```text
findById(...) 必须发生在 save(...) 之前。
```

### 什么时候使用 `InOrder`

适合使用：

- 调用顺序本身是业务规则
- 后一步依赖前一步的结果
- 错误顺序会造成明显业务问题
- 需要验证流程编排类代码

不适合滥用：

- 顺序只是当前实现细节
- 调整内部实现顺序不会影响业务结果
- 过度验证会导致测试很脆

当前订单创建流程里，先查商品、再计算金额、最后保存订单，是比较合理的顺序验证场景。

## `doReturn`、`doThrow`、`doAnswer`

Mockito 有两类常见的 Stub 写法。

第一类是我们前面一直在用的：

```java
when(mock.method()).thenReturn(value);
when(mock.method()).thenThrow(exception);
when(mock.method()).thenAnswer(answer);
```

第二类是 `do...when(...)` 写法：

```java
doReturn(value).when(mock).method();
doThrow(exception).when(mock).method();
doAnswer(answer).when(mock).method();
```

两类写法表达的意思接近，只是顺序不同。

### `doReturn`

普通写法：

```java
when(productRepository.findById("p-1001")).thenReturn(Optional.of(product));
```

`doReturn` 写法：

```java
doReturn(Optional.of(product))
        .when(productRepository)
        .findById("p-1001");
```

含义一样：

```text
当 productRepository.findById("p-1001") 被调用时，返回 Optional.of(product)。
```

在普通 Mock 上，两种写法大多数时候都可以。

### `doThrow`

普通写法：

```java
when(orderRepository.save(any(Order.class)))
        .thenThrow(new IllegalStateException("save order failed"));
```

`doThrow` 写法：

```java
doThrow(new IllegalStateException("save order failed"))
        .when(orderRepository)
        .save(any(Order.class));
```

含义一样：

```text
当 orderRepository.save(...) 被调用时，抛出 IllegalStateException。
```

`doThrow` 特别常用于 `void` 方法，因为 `void` 方法不能写在 `when(...)` 里。

例如：

```java
doThrow(new IllegalStateException("send message failed"))
        .when(messageSender)
        .send(any());
```

### `doAnswer`

普通写法：

```java
when(orderRepository.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
```

`doAnswer` 写法：

```java
doAnswer(invocation -> invocation.getArgument(0))
        .when(orderRepository)
        .save(any(Order.class));
```

含义一样：

```text
当 save(...) 被调用时，把传入的第 1 个参数原样返回。
```

### 什么时候用 `when...then...`，什么时候用 `do...when...`

一般建议：

- 普通 Mock 返回值：优先用 `when(...).thenReturn(...)`，可读性更自然。
- 普通 Mock 抛异常：`when(...).thenThrow(...)` 和 `doThrow(...).when(...)` 都可以。
- `void` 方法：使用 `doThrow(...).when(...)` 或 `doAnswer(...).when(...)`。
- `Spy` 对象：经常使用 `doReturn(...).when(...)`，避免真实方法在 Stub 阶段被调用。

当前阶段先记住一句：

```text
普通返回值优先 when...then...；void 方法和 Spy 场景优先 do...when...
```

## `Spy`：部分模拟真实对象

`Mock` 和 `Spy` 都是 Mockito 提供的测试替身，但它们的默认行为不同。

### `Mock` 的默认行为

`Mock` 是一个假的对象。

如果没有 Stub，它的方法不会执行真实逻辑，而是返回默认值：

- 对象类型返回 `null`
- `int` 返回 `0`
- `boolean` 返回 `false`
- 集合可能返回空或 `null`

例如：

```java
@Mock
private OrderCalculator orderCalculator;
```

如果没有写：

```java
when(orderCalculator.calculateTotal(...)).thenReturn(...);
```

那么调用 `calculateTotal(...)` 时不会执行 `OrderCalculator` 里的真实计算逻辑。

### `Spy` 的默认行为

`Spy` 是包在真实对象外面的一层测试替身。

如果没有 Stub，它会调用真实方法。

例如：

```java
@Spy
private OrderCalculator orderCalculator = new OrderCalculator();
```

如果测试中调用：

```java
orderCalculator.calculateTotal(new BigDecimal("59.90"), 2);
```

默认会执行 `OrderCalculator` 的真实计算逻辑。

### `Spy` 适合什么场景

`Spy` 适合“我想用大部分真实行为，只替换其中一小部分”的场景。

常见例子：

- 某个类大部分方法可以真实执行，但其中一个方法依赖当前时间
- 某个类大部分方法可以真实执行，但其中一个方法会访问外部系统
- 想验证真实对象上的某个方法是否被调用

但要注意：`Spy` 比 `Mock` 更容易让测试依赖真实实现细节，所以不要滥用。

### `Spy` 为什么常配合 `doReturn`

对于 `Spy`，下面这种写法有风险：

```java
when(orderCalculator.calculateTotal(new BigDecimal("59.90"), 2))
        .thenReturn(new BigDecimal("100.00"));
```

因为在 `when(...)` 阶段，Spy 可能会先调用一次真实方法。

更推荐写成：

```java
doReturn(new BigDecimal("100.00"))
        .when(orderCalculator)
        .calculateTotal(new BigDecimal("59.90"), 2);
```

`doReturn(...).when(...)` 的好处是：

```text
安排 Stub 时，不会先执行真实方法。
```

这就是前面说的：

```text
Spy 场景优先 do...when...
```

### 当前项目里的练习方式

为了不反复改已经完成的 `OrderServiceTest`，当前项目新增了一个专门的 Spy 练习类：

```text
src/test/java/com/example/testinglab/order/OrderCalculatorSpyTest.java
```

这个类只用于学习 `Spy`，不替换之前已经通过的 Service 层测试。

第一个测试验证：Spy 默认调用真实方法。

```java
@Test
void shouldCallRealMethodByDefaultWhenUsingSpy() {
    BigDecimal total = orderCalculator.calculateTotal(new BigDecimal("59.90"), 2);

    assertThat(total).isEqualByComparingTo(new BigDecimal("119.80"));
}
```

第二个测试验证：Spy 可以局部 Stub。

```java
@Test
void shouldUseStubbedValueWhenSpyMethodIsStubbed() {
    doReturn(new BigDecimal("100.00"))
            .when(orderCalculator)
            .calculateTotal(any(), eq(2));

    BigDecimal total = orderCalculator.calculateTotal(new BigDecimal("59.90"), 2);

    assertThat(total).isEqualByComparingTo(new BigDecimal("100.00"));
}
```

这里真实计算结果本来应该是 `119.80`，但因为测试对 `calculateTotal(...)` 打了 Stub，所以最终返回 `100.00`。

第三个测试验证：对 Spy 使用 `when(...).thenReturn(...)` 时可能提前调用真实方法。

```java
@Test
void shouldThrowExceptionWhenStubbingSpyWithWhenAndMatcher() {
    assertThatThrownBy(() ->
            when(orderCalculator.calculateTotal(any(), eq(2)))
                    .thenReturn(new BigDecimal("100.00"))
    )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("unitPrice must not be null");
}
```

原因是 `any()` 在 Stub 设置阶段会提供 `null`，而 Spy 可能会先执行真实的 `calculateTotal(...)`，于是触发业务代码里的空值校验。

### `Mock` 和 `Spy` 的选择

一般建议：

- 测 Service 时，外部依赖优先用 `Mock`。
- 纯计算类可以真实使用，也可以用 `Spy` 学习部分模拟。
- 如果一个对象会访问数据库、网络、文件、消息队列，通常不要用 `Spy`，优先用 `Mock`。
- 如果发现测试里大量使用 `Spy`，通常说明类的职责可能太复杂，需要重新设计。

## `void` 方法测试

`void` 方法没有返回值，所以测试重点通常不是断言返回结果，而是验证它是否对依赖产生了正确调用。

当前项目新增了一个通知示例：

```text
src/main/java/com/example/testinglab/notification/MessageSender.java
src/main/java/com/example/testinglab/notification/OrderNotificationService.java
src/test/java/com/example/testinglab/notification/OrderNotificationServiceTest.java
```

业务代码：

```java
public void notifyOrderCreated(String orderId) {
    messageSender.send("Order created: " + orderId);
}
```

### 验证 `void` 方法被调用

当 `notifyOrderCreated("o-1001")` 被调用时，测试要验证：

```text
messageSender.send("Order created: o-1001") 被调用
```

关键写法：

```java
verify(messageSender).send("Order created: " + orderId);
```

这里的重点是：

```text
void 方法没有返回值，所以用 verify(...) 验证它是否发生过。
```

如果测试只调用：

```java
orderNotificationService.notifyOrderCreated(orderId);
```

但没有 `verify(...)` 或断言，那么即使业务方法内部什么都不做，测试也可能通过。这种测试覆盖不到真实行为。

### 让 `void` 方法抛异常

普通的 `when(...).thenThrow(...)` 不适合 `void` 方法，因为 `void` 方法不能放进 `when(...)` 里作为返回值表达式。

`void` 方法抛异常要使用：

```java
doThrow(new IllegalStateException("send message failed"))
        .when(messageSender)
        .send("Order created: " + orderId);
```

然后用 AssertJ 断言异常：

```java
assertThatThrownBy(() -> orderNotificationService.notifyOrderCreated(orderId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("send message failed");
```

这个测试验证的是：

```text
1. 通知发送依赖 messageSender 失败
2. OrderNotificationService 没有吞掉异常
3. 异常继续向外抛出
```

### `void` 方法测试总结

常见场景：

- 正常执行：用 `verify(...)` 验证依赖调用。
- 抛出异常：用 `doThrow(...).when(...)` 准备异常，再用 `assertThatThrownBy(...)` 断言。
- 如果还要验证调用次数，可以结合 `times(1)`。

## Strict Stubbing：发现多余或不匹配的 Stub

Mockito 的 `MockitoExtension` 默认会进行较严格的 Stub 检查。它的目标是让测试代码保持干净，尽早发现“写了 Stub 但测试没有真正用到”的问题。

### `PotentialStubbingProblem`

这个错误表示：测试中对某个方法做了 Stub，但业务代码真实调用这个方法时，参数和 Stub 不匹配。

本项目练习中观察到的场景：

```text
Stub：messageSender.send("unused message")
真实调用：messageSender.send("Order created: o-1001")
```

Mockito 报错：

```text
Strict stubbing argument mismatch
```

含义：

```text
同一个方法被调用了，但实际参数和测试准备的 Stub 参数不一致。
```

这通常说明：

- Stub 参数写错了
- 业务代码实际行为和测试预期不一致
- 测试里有复制粘贴留下的错误 Stub

### `UnnecessaryStubbingException`

这个错误表示：测试里写了 Stub，但这个 Stub 对应的方法在测试执行过程中完全没有被调用。

本项目练习中观察到的场景：

```text
Stub：productRepository.findById("unused-product")
测试过程：没有任何代码调用 findById("unused-product")
```

Mockito 报错：

```text
Unnecessary stubbings detected.
Clean & maintainable test code requires zero unnecessary code.
```

含义：

```text
测试里存在多余准备代码，应该删除。
```

### 两类错误的区别

```text
PotentialStubbingProblem：
Stub 了某个方法，但真实调用时参数不匹配。

UnnecessaryStubbingException：
Stub 写了，但对应方法完全没有被调用。
```

### 处理原则

优先做法：

```text
删除没用的 Stub，或把 Stub 参数改成真实业务调用会使用的参数。
```

不优先做法：

```text
一看到 strict stubbing 报错就使用 lenient。
```

`lenient` 可以放宽检查，但学习阶段先不要依赖它。大多数时候，strict stubbing 报错都在提醒测试代码确实不够干净或不够准确。

## `lenient()`：放宽 strict stubbing

`lenient()` 用来告诉 Mockito：这个 Stub 即使没有被用到，也不要因为 strict stubbing 报错。

示例：

```java
lenient().when(productRepository.findById("unused-product"))
        .thenReturn(Optional.empty());
```

如果不加 `lenient()`，这类未使用 Stub 通常会触发：

```text
UnnecessaryStubbingException
```

### 什么时候可以考虑使用

`lenient()` 适合少数共享 setup 场景：

```text
多个测试共用一段初始化 Stub，
但其中某些测试确实不会用到其中一部分 Stub。
```

### 什么时候不该使用

不应该把 `lenient()` 当成让测试通过的快捷方式。

优先处理顺序：

```text
1. 删除无用 Stub。
2. 把 Stub 移到真正需要它的测试方法里。
3. 确实存在共享 setup 的合理原因时，再考虑 lenient()。
```

一句话总结：

```text
lenient() 是 strict stubbing 的例外开关，不是常规写法。
```

## 常见 Mockito 错误总结

### 参数匹配器混用

错误示例：

```java
verify(orderCalculator).calculateTotal(any(), 2);
```

原因：

```text
同一次方法调用里，一个参数用了 matcher，其他参数也必须用 matcher。
```

正确写法：

```java
verify(orderCalculator).calculateTotal(any(), eq(2));
```

也可以完全不用 matcher：

```java
verify(orderCalculator).calculateTotal(new BigDecimal("59.90"), 2);
```

### `PotentialStubbingProblem`

含义：

```text
Stub 了某个方法，但真实调用时参数不匹配。
```

优先检查：

```text
Stub 参数和业务代码真实调用参数是否一致。
```

### `UnnecessaryStubbingException`

含义：

```text
写了 Stub，但测试过程中完全没有用到。
```

优先处理：

```text
删除无用 Stub，或把 Stub 移到真正需要它的测试方法里。
```

### `Spy` 真实方法提前执行

风险写法：

```java
when(orderCalculator.calculateTotal(any(), eq(2)))
        .thenReturn(new BigDecimal("100.00"));
```

原因：

```text
Spy 使用 when(spy.method(...)) 时，可能在 Stub 设置阶段先执行真实方法。
```

推荐写法：

```java
doReturn(new BigDecimal("100.00"))
        .when(orderCalculator)
        .calculateTotal(any(), eq(2));
```

### `Wanted but not invoked`

含义：

```text
verify(...) 期望某个方法被调用，但业务代码实际没有调用。
```

优先检查：

```text
1. 业务代码是否真的调用了这个方法。
2. 调用参数是否和 verify 期望一致。
3. 被验证的 Mock 是否就是业务代码实际使用的依赖对象。
```

## Mockito 基础总复盘

### Mock、Stub、Verify

```text
Mock：模拟依赖对象。
Stub：给 Mock 或 Spy 的方法设置返回值、异常或 Answer。
Verify：验证 Mock 或 Spy 的方法是否按预期被调用。
```

### ArgumentCaptor

```text
当方法参数是复杂对象，且需要检查对象内部字段时使用 ArgumentCaptor。
```

例如当前项目中，`OrderServiceTest` 使用 `ArgumentCaptor<Order>` 捕获传给 `orderRepository.save(...)` 的 `Order` 对象，再验证订单字段是否正确。

### Spy 和 Mock

```text
Spy：默认调用真实方法。
Mock：默认不执行真实逻辑，通常返回 null、0、false 等默认值。
```

Spy 打 Stub 推荐：

```java
doReturn(value)
        .when(spy)
        .method(...);
```

原因：

```text
doReturn(...).when(spy).method(...) 不会在设置 Stub 时调用真实方法。
```

这样可以避免 `when(spy.method(any()))` 在 Stub 阶段提前执行真实方法，导致 `null` 参数触发异常。

### void 方法测试

```text
void 正常调用：用 verify(...) 验证依赖方法被调用。
void 抛异常：用 doThrow(...).when(mock).voidMethod(...) 准备异常，再用 assertThatThrownBy(...) 断言。
```

### strict stubbing

strict stubbing 主要帮助发现两类问题：

```text
1. Stub 参数不匹配：PotentialStubbingProblem
2. Stub 完全没用到：UnnecessaryStubbingException
```

### lenient()

```text
lenient() 可以放宽 strict stubbing。
```

但无用 Stub 应优先删除，或者移动到真正需要它的测试方法里，所以不建议优先使用 `lenient()`。

## 本课已完成内容

当前 `OrderServiceTest` 已经覆盖：

- 商品存在且库存足够时，可以创建订单
- 创建订单成功时，会保存订单
- 使用 `ArgumentCaptor` 验证保存的订单内容
- 商品不存在时，抛出 `product not found`
- 商品库存不足时，抛出 `product stock is not enough`
- 异常场景下不调用 `OrderCalculator.calculateTotal(...)`
- 保存订单失败时，抛出 `save order failed`
- 使用独立的 `OrderCalculatorSpyTest` 学习 `Spy`
- 验证 `Spy` 默认调用真实方法
- 验证 `doReturn(...).when(...)` 可以局部 Stub `Spy`
- 验证 `when(spy.method(...)).thenReturn(...)` 在 matcher 场景下可能提前调用真实方法
- 使用独立的 `OrderNotificationServiceTest` 学习 `void` 方法测试
- 使用 `verify(...)` 验证 `void` 方法调用
- 使用 `doThrow(...).when(...)` 模拟 `void` 方法抛异常
- 观察 strict stubbing 的两类常见错误
- 区分 `PotentialStubbingProblem` 和 `UnnecessaryStubbingException`
- 了解 `lenient()` 的作用和使用边界
- 总结常见 Mockito 错误及排查方向

当前测试运行结果：

```text
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 本课练习

1. 在成功创建订单的测试中，把 `verify(orderRepository).save(...)` 改成 `verify(orderRepository, times(1)).save(...)`。
2. 在保存失败的测试中，也验证 `orderRepository.save(...)` 被调用了 1 次。
3. 尝试把其中一个 `times(1)` 改成 `times(2)`，运行测试，观察 Mockito 的失败提示，然后再改回来。
4. 在成功创建订单的测试中，使用 `InOrder` 验证调用顺序：先查商品，再计算金额，最后保存订单。
5. 把保存失败测试里的 `when(...).thenThrow(...)` 改成 `doThrow(...).when(...)`，观察测试结果是否仍然通过。
6. 把成功创建订单测试里的 `when(...).thenAnswer(...)` 改成 `doAnswer(...).when(...)`，观察测试结果是否仍然通过。
7. 新增独立的 `OrderCalculatorSpyTest`，体验 `Spy` 默认调用真实方法。
8. 在 `OrderCalculatorSpyTest` 中使用 `doReturn(...).when(...)`，体验 `Spy` 的局部 Stub。
9. 对比 `when(spy.method(...)).thenReturn(...)` 和 `doReturn(...).when(spy).method(...)` 的差异。
10. 新增独立的 `OrderNotificationServiceTest`，用 `verify(...)` 验证 `void` 方法调用。
11. 使用 `doThrow(...).when(...)` 模拟 `void` 方法抛异常。
12. 观察 strict stubbing 的 `PotentialStubbingProblem` 和 `UnnecessaryStubbingException`。
13. 了解 `lenient()` 只作为 strict stubbing 的例外开关。
14. 复盘常见 Mockito 错误：matcher 混用、未使用 Stub、Spy 提前调用真实方法、`Wanted but not invoked`。
