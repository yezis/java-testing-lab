# 第 2 课：Mockito 与 Service 层单元测试

## 本课目标

掌握 Service 层单元测试中最常用的 Mockito 写法：

- 理解 Mock 的作用
- 使用 `@Mock` 创建模拟对象
- 使用 `@InjectMocks` 创建被测试对象
- 使用 `when(...).thenReturn(...)` 准备依赖返回值
- 使用 `verify(...)` 验证依赖是否被调用

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

### 当前项目里的练习思路

当前 `OrderServiceTest` 里 `OrderCalculator` 是 Mock：

```java
@Mock
private OrderCalculator orderCalculator;
```

下一步可以把它改成 Spy：

```java
@Spy
private OrderCalculator orderCalculator = new OrderCalculator();
```

然后成功创建订单的测试就不一定需要 Stub 金额计算：

```java
when(orderCalculator.calculateTotal(any(), eq(2)))
        .thenReturn(new BigDecimal("119.80"));
```

因为 Spy 默认会调用 `OrderCalculator` 的真实计算逻辑。

不过异常测试里如果仍然不希望真实计算，或者想强行指定返回值，可以用：

```java
doReturn(new BigDecimal("119.80"))
        .when(orderCalculator)
        .calculateTotal(any(), eq(2));
```

### `Mock` 和 `Spy` 的选择

一般建议：

- 测 Service 时，外部依赖优先用 `Mock`。
- 纯计算类可以真实使用，也可以用 `Spy` 学习部分模拟。
- 如果一个对象会访问数据库、网络、文件、消息队列，通常不要用 `Spy`，优先用 `Mock`。
- 如果发现测试里大量使用 `Spy`，通常说明类的职责可能太复杂，需要重新设计。

## 本课已完成内容

当前 `OrderServiceTest` 已经覆盖：

- 商品存在且库存足够时，可以创建订单
- 创建订单成功时，会保存订单
- 使用 `ArgumentCaptor` 验证保存的订单内容
- 商品不存在时，抛出 `product not found`
- 商品库存不足时，抛出 `product stock is not enough`
- 异常场景下不调用 `OrderCalculator.calculateTotal(...)`
- 保存订单失败时，抛出 `save order failed`

当前测试运行结果：

```text
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 本课练习

1. 在成功创建订单的测试中，把 `verify(orderRepository).save(...)` 改成 `verify(orderRepository, times(1)).save(...)`。
2. 在保存失败的测试中，也验证 `orderRepository.save(...)` 被调用了 1 次。
3. 尝试把其中一个 `times(1)` 改成 `times(2)`，运行测试，观察 Mockito 的失败提示，然后再改回来。
4. 在成功创建订单的测试中，使用 `InOrder` 验证调用顺序：先查商品，再计算金额，最后保存订单。
5. 把保存失败测试里的 `when(...).thenThrow(...)` 改成 `doThrow(...).when(...)`，观察测试结果是否仍然通过。
6. 把成功创建订单测试里的 `when(...).thenAnswer(...)` 改成 `doAnswer(...).when(...)`，观察测试结果是否仍然通过。
7. 把 `OrderCalculator` 从 `@Mock` 改成 `@Spy`，体验真实方法默认会被调用。
