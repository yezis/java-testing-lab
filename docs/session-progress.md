# Java 自动化测试学习进度交接

更新时间：2026-05-12

## 项目位置

```text
E:\work_space\java-testing-lab
```

这是一个 Maven Java 17 项目，用来通过实际代码学习 Java 自动化测试。

## 本机环境

Java 17：

```text
C:\Program Files\Java\jdk-17
```

命令行 `java`：

```text
java version "21.0.9"
```

Maven：

```text
Apache Maven 3.8.1
D:\work_software\apache-maven-3.8.1
```

注意：当前 Maven 默认使用的 Java 是 JDK 8：

```text
Java version: 1.8.0_301
C:\Program Files\Java\jdk1.8.0_301\jre
```

直接运行 `mvn test` 会因为项目要求 Java 17 而失败：

```text
Fatal error compiling: 无效的目标发行版: 17
```

当前 Windows 环境下运行测试需要临时指定 Java 17：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn test
```

最近一次测试结果：

```text
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
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
src/main/java/com/example/testinglab/notification/MessageSender.java
src/main/java/com/example/testinglab/notification/OrderNotificationService.java
src/main/java/com/example/testinglab/TestingLabApplication.java
src/main/java/com/example/testinglab/order/OrderController.java
src/main/java/com/example/testinglab/order/OrderQueryService.java
src/main/java/com/example/testinglab/order/OrderResponse.java
```

测试代码：

```text
src/test/java/com/example/testinglab/order/OrderCalculatorTest.java
src/test/java/com/example/testinglab/order/OrderCalculatorSpyTest.java
src/test/java/com/example/testinglab/order/OrderServiceTest.java
src/test/java/com/example/testinglab/notification/OrderNotificationServiceTest.java
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

## 整体阶段进度

```text
阶段 1：单元测试基础 - 已完成
阶段 2：Mock 与可测试代码设计 - 已完成并收束
阶段 3：Spring Boot 测试 - 已开始，业务骨架已准备
阶段 4：接口自动化测试 - 未开始
阶段 5：属性测试与 Fuzzing - 未开始
阶段 6：数据库与外部服务 - 未开始
阶段 7：持续集成与质量度量 - 未开始
阶段 8：UI 自动化测试 - 未开始
```

当前建议：

```text
下一次学习从第一个 @WebMvcTest 开始。
Mockito 高级能力如 BDDMockito、static mock、constructor mock、deep stubs 暂时只做了解，不继续深挖。
```

### Spring Boot 测试

已完成准备：

- `pom.xml` 已新增 `spring-boot-starter-web`
- `pom.xml` 已新增 `spring-boot-starter-test`
- 新增 `TestingLabApplication`
- 新增 `OrderController`
- 新增 `OrderQueryService`
- 新增 `OrderResponse`

当前 Spring Boot 示例接口：

```text
GET /api/orders/{orderId}
```

当前 Controller 行为：

```text
OrderController.getOrder(orderId)
-> 调用 OrderQueryService.findById(orderId)
-> 转换为 OrderResponse
-> 返回 JSON
```

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
- `@Spy`
- `doReturn(...).when(...)`
- `Spy` 默认调用真实方法
- `Spy` 局部 Stub
- `when(spy.method(...)).thenReturn(...)` 可能在 Stub 阶段提前调用真实方法
- `void` 方法测试
- 用 `verify(...)` 验证 `void` 方法调用
- 用 `doThrow(...).when(...)` 模拟 `void` 方法抛异常
- strict stubbing 初步观察
- `PotentialStubbingProblem`
- `UnnecessaryStubbingException`
- `lenient()` 的作用和使用边界
- 常见 Mockito 错误总结

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

`OrderCalculatorSpyTest` 覆盖：

- `Spy` 默认调用 `OrderCalculator.calculateTotal(...)` 的真实方法
- 使用 `doReturn(...).when(...)` 对 `Spy` 进行局部 Stub
- 使用 `when(spy.method(...)).thenReturn(...)` 配合 matcher 时，可能在 Stub 阶段调用真实方法并触发异常

`OrderNotificationServiceTest` 覆盖：

- 调用 `notifyOrderCreated(...)` 时，验证 `messageSender.send(...)` 被调用
- 使用 `doThrow(...).when(...)` 模拟 `messageSender.send(...)` 发送失败
- 使用 `assertThatThrownBy(...)` 验证发送失败异常继续向外抛出

## 当前代码状态

当前 `OrderServiceTest` 中：

- `ProductRepository` 是 `@Mock`
- `OrderCalculator` 仍然是 `@Mock`
- `OrderRepository` 是 `@Mock`
- 不因为学习 `Spy` 而改写已经完成的 `OrderServiceTest`

当前新增了独立的 `OrderCalculatorSpyTest`：

```java
@Spy
private OrderCalculator orderCalculator = new OrderCalculator();
```

这份测试类用于学习 `Spy`，不替换已有的 Service 层测试。

后续可以做一次测试代码小清理：整理 import 顺序、减少多余空行。旧测试中如有临时注释代码，也可以在不改变行为的前提下清理。

## 最近复盘结论

Mockito 基础概念已经完成一次口头复盘：

```text
1. Mock 是对依赖对象的模拟，例如 OrderNotificationServiceTest 中的 MessageSender。
2. Stub 是给 Mock 或 Spy 设置虚拟行为，例如 doThrow(...).when(messageSender).send(...).
3. verify(...) 用来验证 Mock 或 Spy 上某个方法是否按预期被调用。
4. void 方法没有返回值，所以常通过 verify(...) 验证它是否产生正确的依赖调用。
5. Spy 默认可以调用真实方法；Mock 默认不执行真实逻辑。
6. doThrow(...).when(...) 典型适用于 void 方法抛异常，也常见于 doXXX().when() 这类 Spy / void 场景。
```

需要继续注意：

```text
Mock 不是任何时候都必须 Stub。
如果测试只关心 void 方法是否被调用，可以只 verify，不 Stub。
当测试依赖返回值或后续业务流程时，通常需要 Stub。
```

## 学习协作偏好

后续学习默认采用下面方式：

```text
1. 业务代码、接口、小型示例类由 AI 自动生成。
2. 测试代码优先由学习者自己编写。
3. 学习新知识时，AI 不默认给出完整测试代码。
4. AI 优先给测试目标、关键 API、建议类名、建议方法名、变量名和必要提示。
5. 只有在学习者卡住、setup 比较机械、或精确写法很重要时，才提供完整测试代码。
```

目的：

```text
节约业务代码准备时间，同时保留测试编写练习。
```

已新增 `void` 方法测试示例业务代码：

```text
src/main/java/com/example/testinglab/notification/MessageSender.java
src/main/java/com/example/testinglab/notification/OrderNotificationService.java
```

已新增测试：

```text
src/test/java/com/example/testinglab/notification/OrderNotificationServiceTest.java
```

## 下一步学习任务

Mockito 基础已完成并收束，下一步进入 Spring Boot 测试。

```text
目标：理解 Mockito 如何发现“写了 Stub 但实际没有用到”的测试问题。
```

已经观察过两个受控失败场景：

```text
1. PotentialStubbingProblem
   - Stub 了 messageSender.send("unused message")
   - 真实调用是 messageSender.send("Order created: o-1001")
   - 含义：同一个方法被调用了，但参数和 Stub 不匹配

2. UnnecessaryStubbingException
   - Stub 了 productRepository.findById("unused-product")
   - 测试过程中完全没有调用这个 Stub
   - 含义：测试里存在多余 Stub，应删除无用准备代码
```

当前已完成：

```text
1. 删除临时的 shouldShowUnnecessaryStubbingError 失败测试。
2. 运行 mvn test，恢复 Tests run: 16, Failures: 0, Errors: 0, Skipped: 0。
3. 已把 strict stubbing 总结写入 docs/lesson-02-mockito.md。
```

已继续完成：

```text
1. 已了解 lenient()：它能放宽 strict stubbing，但不要优先使用。
2. 已总结常见 Mockito 错误：
   - matcher 混用
   - PotentialStubbingProblem
   - UnnecessaryStubbingException
   - Spy 真实方法提前执行
   - Wanted but not invoked
3. 已把 lenient() 和常见错误总结写入 docs/lesson-02-mockito.md。
4. 已把 Mockito 基础总复盘写入 docs/lesson-02-mockito.md，方便后续复习。
```

下一步建议：

```text
1. 新增 OrderControllerTest。
2. 使用 @WebMvcTest(OrderController.class)。
3. 注入 MockMvc。
4. 使用 @MockBean OrderQueryService 替换 Spring 容器中的 Service。
5. 测试 GET /api/orders/o-1001 返回 200。
6. 验证 JSON 字段：orderId、productName、quantity、totalAmount。
```

建议测试信息：

```text
测试类：src/test/java/com/example/testinglab/order/OrderControllerTest.java
测试方法：shouldReturnOrderWhenOrderExists
关键 API：@WebMvcTest、MockMvc、@MockBean、mockMvc.perform(get(...))、status().isOk()、jsonPath(...)
注意：当前 Spring Boot 版本是 3.3.5，先使用 @MockBean；Spring Boot 3.4+ 才推荐 @MockitoBean。
```

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
