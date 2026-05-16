# 第 3 课：Spring Boot Web 层测试

## 本课目标

这一课开始学习 Spring Boot 测试，先从 Controller 层开始。

本节重点：

- 理解 `@WebMvcTest` 的作用
- 理解 `MockMvc` 如何模拟 HTTP 请求
- 理解 `@MockBean` 如何替换 Controller 依赖的 Service
- 使用 `jsonPath` 断言 JSON 响应内容
- 理解 Controller 测试和普通 Service 单元测试的区别

## 为什么先测 Controller

Controller 是 HTTP 请求进入后端应用的入口。

普通 Service 单元测试主要验证 Java 方法逻辑，例如：

```text
调用 orderService.createOrder(...)
断言返回值或异常
```

Controller 测试更接近真实接口调用，例如：

```text
GET /api/orders/o-1001
断言 HTTP 状态码是 200
断言响应 JSON 字段正确
```

所以 Controller 测试关注的是：

- URL 是否正确
- HTTP 方法是否正确
- 路径参数是否能绑定
- 返回状态码是否正确
- JSON 响应结构是否正确
- Controller 是否正确调用 Service

## `@WebMvcTest`

示例：

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
}
```

含义：

```text
只加载 Spring MVC Web 层相关组件，并重点测试 OrderController。
```

它不会启动完整应用，也不会加载所有 Service、Repository、数据库配置。

这和 `@SpringBootTest` 不同：

```text
@WebMvcTest     更轻，只测 Web 层
@SpringBootTest 更重，启动更完整的 Spring Boot 上下文
```

## `MockMvc`

`MockMvc` 用来在测试里模拟 HTTP 请求，不需要真的启动一个服务器端口。

示例：

```java
@Autowired
private MockMvc mockMvc;
```

发送请求：

```java
mockMvc.perform(get("/api/orders/o-1001"))
        .andExpect(status().isOk());
```

含义：

```text
模拟一次 GET /api/orders/o-1001 请求，并断言响应状态码是 200。
```

## `@MockBean`

`OrderController` 依赖 `OrderQueryService`：

```text
OrderController -> OrderQueryService
```

在 `@WebMvcTest` 中，Spring 只重点加载 Web 层，不会自动准备完整 Service 逻辑。

所以测试里使用：

```java
@MockBean
private OrderQueryService orderQueryService;
```

含义：

```text
把 OrderQueryService 注册成 Spring 容器里的 Mock 对象。
```

这样 Controller 启动时需要的 `OrderQueryService` 依赖就有了，并且测试可以控制它返回什么。

## 完整示例

测试类：

```text
src/test/java/com/example/testinglab/order/OrderControllerTest.java
```

核心代码：

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderQueryService orderQueryService;

    @Test
    void shouldGetOrderById() throws Exception {
        Order order = new Order("o-1001", "Java Testing Book", 2, new BigDecimal("119.80"));

        when(orderQueryService.findById("o-1001")).thenReturn(order);

        mockMvc.perform(get("/api/orders/o-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("o-1001"))
                .andExpect(jsonPath("$.productName").value("Java Testing Book"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(119.80));

        verify(orderQueryService).findById("o-1001");
    }
}
```

## 代码执行流程

这段测试的执行顺序可以理解为：

```text
1. @WebMvcTest 创建一个只包含 Web 层的 Spring 测试环境
2. @MockBean 创建一个假的 OrderQueryService，并放进 Spring 容器
3. when(...).thenReturn(...) 指定 Service 被调用时返回什么订单
4. mockMvc.perform(...) 模拟浏览器或客户端发起 HTTP GET 请求
5. Spring MVC 找到 OrderController.getOrder(...)
6. Controller 调用 Mock 出来的 OrderQueryService
7. Controller 把 Order 转换成 OrderResponse
8. Spring 把 OrderResponse 序列化成 JSON
9. andExpect(...) 断言 HTTP 状态码和 JSON 字段
10. verify(...) 验证 Controller 确实调用了 Service
```

## `jsonPath`

`jsonPath` 用来从 JSON 响应里取字段并断言。

例如响应 JSON 是：

```json
{
  "orderId": "o-1001",
  "productName": "Java Testing Book",
  "quantity": 2,
  "totalAmount": 119.80
}
```

那么：

```java
jsonPath("$.productName").value("Java Testing Book")
```

含义：

```text
取 JSON 根对象下的 productName 字段，断言它等于 Java Testing Book。
```

常见写法：

```text
$.orderId       根对象下的 orderId
$.productName  根对象下的 productName
$.quantity     根对象下的 quantity
```

## `@PathVariable` 的注意点

Controller 中建议显式写出路径变量名：

```java
@PathVariable("orderId") String orderId
```

不要只写：

```java
@PathVariable String orderId
```

原因：

```text
如果编译时没有保留 Java 方法参数名，Spring 可能无法知道路径里的 {orderId} 应该绑定到哪个参数。
```

本项目已经在 `OrderController` 中显式写了路径变量名。

## Controller 异常场景：返回 404

接口测试不能只测成功场景，还要测失败场景。

例如：

```text
GET /api/orders/o-404
```

如果订单不存在，接口不应该返回 `200`，也不应该因为未处理异常返回 `500`。

更合理的结果是：

```text
404 Not Found
```

## 第一种方式：`@ResponseStatus`

一种简单方式是在异常类上标注 `@ResponseStatus`：

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrderNotFoundException extends RuntimeException {
}
```

这种方式适合很简单的场景。

但它有一个限制：

```text
它主要定义 HTTP 状态码，不方便统一定义错误响应 JSON。
```

真实项目里通常希望错误响应格式稳定，例如：

```json
{
  "code": "ORDER_NOT_FOUND",
  "message": "order not found: o-404"
}
```

因此本项目继续引入统一异常处理。

## 统一错误响应

本项目新增了错误响应结构：

```text
src/main/java/com/example/testinglab/common/ErrorResponse.java
```

```java
public record ErrorResponse(
        String code,
        String message
) {
}
```

它表示接口错误时返回的 JSON 格式：

```json
{
  "code": "ORDER_NOT_FOUND",
  "message": "order not found: o-404"
}
```

## `@RestControllerAdvice`

本项目新增了全局异常处理器：

```text
src/main/java/com/example/testinglab/common/GlobalExceptionHandler.java
```

核心代码：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleOrderNotFound(OrderNotFoundException exception) {
        return new ErrorResponse("ORDER_NOT_FOUND", exception.getMessage());
    }
}
```

含义：

```text
@RestControllerAdvice：
定义一个全局 Controller 异常处理器。

@ExceptionHandler(OrderNotFoundException.class)：
当 Controller 调用链路中抛出 OrderNotFoundException 时，进入这个方法。

@ResponseStatus(HttpStatus.NOT_FOUND)：
这个异常处理方法返回 HTTP 404。

return new ErrorResponse(...)：
响应体返回统一 JSON。
```

这样 Controller 不需要写 `try-catch`：

```java
@GetMapping("/{orderId}")
public OrderResponse getOrder(@PathVariable("orderId") String orderId) {
    return OrderResponse.from(orderQueryService.findById(orderId));
}
```

如果 `orderQueryService.findById(orderId)` 抛出 `OrderNotFoundException`，Spring MVC 会交给 `GlobalExceptionHandler.handleOrderNotFound(...)` 处理。

## 404 场景测试

测试代码：

```java
@Test
void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
    when(orderQueryService.findById("o-404")).thenThrow(new OrderNotFoundException("o-404"));

    mockMvc.perform(get("/api/orders/o-404"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("order not found: o-404"));

    verify(orderQueryService).findById("o-404");
}
```

这段测试的含义：

```text
1. 准备 Mock：当查询 o-404 时，Service 抛出 OrderNotFoundException
2. 使用 MockMvc 请求 GET /api/orders/o-404
3. 断言 HTTP 状态码是 404
4. 断言错误响应 JSON 里的 code 和 message
5. 验证 Controller 确实调用了 orderQueryService.findById("o-404")
```

## 异常处理执行流程

当请求：

```text
GET /api/orders/o-404
```

执行流程是：

```text
1. MockMvc 发起 GET 请求
2. Spring MVC 匹配到 OrderController.getOrder(...)
3. Controller 调用 orderQueryService.findById("o-404")
4. Mock 出来的 orderQueryService 抛出 OrderNotFoundException
5. Spring MVC 找到 GlobalExceptionHandler
6. handleOrderNotFound(...) 返回 ErrorResponse
7. Spring 把 ErrorResponse 序列化为 JSON
8. 响应状态码是 404
```

## 请求参数校验：返回 400

Controller 可以直接对路径参数、查询参数、请求体字段做校验。

本项目在 `OrderController` 上启用了方法参数校验：

```java
@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {
}
```

并给 `orderId` 增加了格式规则：

```java
@Pattern(regexp = "o-\\d+", message = "orderId must match pattern o-{number}")
String orderId
```

含义：

```text
orderId 必须是 o-数字 的格式。
```

合法示例：

```text
o-1001
o-404
```

非法示例：

```text
abc
1-123
order-1001
```

如果参数不合法，Spring MVC 会在进入业务逻辑前抛出 `ConstraintViolationException`。

本项目在 `GlobalExceptionHandler` 中把它转换为：

```text
400 Bad Request
```

响应体：

```json
{
  "code": "INVALID_REQUEST",
  "message": "..."
}
```

## 400 场景测试

当前测试类：

```text
src/test/java/com/example/testinglab/order/OrderControllerTest.java
```

新增测试目标：

```text
当请求 GET /api/orders/1-123 时：
1. Spring MVC 校验 orderId 格式
2. 因为 1-123 不符合 o-数字，返回 400
3. JSON 字段 code = INVALID_REQUEST
4. orderQueryService 不应该被调用
```

关键代码：

```java
mockMvc.perform(get("/api/orders/1-123"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

verifyNoInteractions(orderQueryService);
```

这里使用 `verifyNoInteractions(orderQueryService)` 是合理的，因为参数校验发生在 Controller 方法真正执行前。

建议继续补强一个断言：

```java
.andExpect(jsonPath("$.message").value(containsString("orderId must match pattern o-{number}")))
```

这样能确认触发 `400` 的原因确实是 `orderId` 格式错误。

## POST 请求体 JSON 测试

GET 请求通常通过路径参数或查询参数传值。

POST 请求常见做法是通过请求体传 JSON，例如：

```json
{
  "productId": "p-1001",
  "quantity": 2
}
```

Controller 中使用 `@RequestBody` 接收 JSON：

```java
@PostMapping
public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
    return OrderResponse.from(orderCommandService.createOrder(request.productId(), request.quantity()));
}
```

含义：

```text
1. Spring MVC 读取 HTTP 请求体
2. 把 JSON 反序列化成 CreateOrderRequest
3. @Valid 触发请求体字段校验
4. Controller 调用 orderCommandService.createOrder(...)
5. 返回 OrderResponse JSON
```

## 请求 DTO：`CreateOrderRequest`

`CreateOrderRequest` 是请求 DTO，专门表示创建订单接口的入参。

当前使用 Java `record`：

```java
public record CreateOrderRequest(
        @NotBlank(message = "productId must not be blank")
        String productId,

        @Min(value = 1, message = "quantity must be greater than or equal to 1")
        int quantity
) {
}
```

`record` 适合只承载数据的对象。Java 会自动生成构造器、字段访问方法、`equals`、`hashCode` 和 `toString`。

所以取值时使用：

```java
request.productId()
request.quantity()
```

而不是：

```java
request.getProductId()
request.getQuantity()
```

## MockMvc 发送 JSON

POST 测试中使用：

```java
mockMvc.perform(post("/api/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
                {
                    "productId": "p-1001",
                    "quantity": 2
                }
                """))
```

关键点：

```text
post("/api/orders")：
模拟 POST 请求。

contentType(MediaType.APPLICATION_JSON)：
告诉 Spring MVC 请求体是 JSON。

content(...)：
设置 HTTP 请求体内容。
```

完整测试目标：

```text
1. Stub orderCommandService.createOrder("p-1001", 2) 返回一个 Order
2. MockMvc 发起 POST /api/orders
3. 请求体传入 productId 和 quantity
4. 断言状态码是 200
5. 断言响应 JSON 字段正确
6. verify orderCommandService.createOrder("p-1001", 2)
```

这个测试验证的是：

```text
JSON 请求体
-> CreateOrderRequest
-> Controller
-> OrderCommandService
-> OrderResponse JSON
```

## POST 请求体校验失败

`CreateOrderRequest` 中定义了请求体字段校验规则：

```java
public record CreateOrderRequest(
        @NotBlank(message = "productId must not be blank")
        String productId,

        @Min(value = 1, message = "quantity must be greater than or equal to 1")
        int quantity
) {
}
```

当请求体是：

```json
{
  "productId": "p-1001",
  "quantity": 0
}
```

`quantity` 不满足 `@Min(1)`，所以 Spring MVC 会在进入业务逻辑前抛出 `MethodArgumentNotValidException`。

本项目在 `GlobalExceptionHandler` 中处理这个异常，并返回：

```text
400 Bad Request
```

测试目标：

```text
1. MockMvc 发起 POST /api/orders
2. 请求体中 quantity = 0
3. 断言状态码是 400
4. 断言 code = INVALID_REQUEST
5. 断言 message 包含 quantity must be greater than or equal to 1
6. 验证 orderCommandService 没有被调用
```

这里使用：

```java
verifyNoInteractions(orderCommandService);
```

含义：

```text
请求体校验失败时，Controller 不应该进入创建订单的业务逻辑。
```

执行流程：

```text
1. MockMvc 发送 POST JSON
2. Spring MVC 把 JSON 转成 CreateOrderRequest
3. @Valid 触发字段校验
4. quantity = 0 不满足 @Min(1)
5. Spring MVC 抛出 MethodArgumentNotValidException
6. GlobalExceptionHandler 转换为 400 + ErrorResponse
7. orderCommandService 不被调用
```

## JSON 反序列化失败

请求体校验失败和 JSON 反序列化失败不是同一类问题。

例如：

```json
{
  "productId": "p-1001",
  "quantity": "abc"
}
```

这里 `quantity` 在 Java 中是 `int`：

```java
int quantity
```

但 JSON 中传入的是字符串 `"abc"`。

这时 Spring MVC 无法把请求体转换成 `CreateOrderRequest`，所以还没进入 `@Min(1)` 校验阶段，就会抛出：

```text
HttpMessageNotReadableException
```

本项目在 `GlobalExceptionHandler` 中处理它：

```java
@ExceptionHandler(HttpMessageNotReadableException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ErrorResponse handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
    return new ErrorResponse("INVALID_REQUEST", "request body is not readable");
}
```

测试目标：

```text
1. MockMvc 发起 POST /api/orders
2. 请求体中 quantity = "abc"
3. Spring MVC 无法把 "abc" 转成 int
4. 断言状态码是 400
5. 断言 code = INVALID_REQUEST
6. 断言 message = request body is not readable
7. 验证 orderCommandService 没有被调用
```

对比：

```text
quantity = 0
-> JSON 可以转成 int
-> 进入 @Valid
-> 违反 @Min(1)
-> MethodArgumentNotValidException

quantity = "abc"
-> JSON 不能转成 int
-> 无法创建 CreateOrderRequest
-> HttpMessageNotReadableException
```

## 小复盘

### 1. `@WebMvcTest` 解决什么问题？

它用于只启动 Spring MVC Web 层，快速测试 Controller，不需要启动完整应用和数据库。

### 2. 当前项目里哪一行代码用了它？

```text
OrderControllerTest 类上的 @WebMvcTest(OrderController.class)
```

### 3. 如果不用它，测试会变成什么样？

可以用 `@SpringBootTest` 启动完整应用上下文，但测试更重、启动更慢，而且会引入更多和 Controller 无关的配置。

### 4. `@RestControllerAdvice` 解决什么问题？

它把 Controller 层的异常处理集中到一个地方，避免每个 Controller 都写重复的 `try-catch`。

当前项目里：

```text
OrderNotFoundException
-> GlobalExceptionHandler.handleOrderNotFound(...)
-> 404 + ErrorResponse JSON
```

### 5. `@Validated` 和 `@Pattern` 解决什么问题？

它们用于在请求进入业务逻辑前检查参数是否合法。

当前项目里：

```text
OrderController.getOrder(...)
-> orderId 必须符合 o-数字
-> 不符合时返回 400
-> 不调用 orderQueryService
```

### 6. `@RequestBody` 和 `content(...)` 解决什么问题？

`@RequestBody` 让 Controller 可以接收 HTTP 请求体中的 JSON。

`MockMvc.content(...)` 用于在测试里模拟这个 JSON 请求体。

当前项目里：

```text
POST /api/orders
-> JSON 请求体
-> CreateOrderRequest
-> orderCommandService.createOrder(...)
```

### 7. 请求体校验失败时为什么要验证 Service 不被调用？

因为参数校验属于 Web 层入口保护。

非法请求应该在进入业务逻辑前被拒绝：

```text
非法 JSON 字段
-> 400 Bad Request
-> 不调用 application service
```

### 8. `HttpMessageNotReadableException` 解决什么问题？

它表示 HTTP 请求体无法被读取或转换成目标 Java 对象。

当前项目里：

```text
quantity = "abc"
-> 无法转换成 int
-> 返回 400
-> 不调用 orderCommandService
```

## `@WebMvcTest` 阶段收束

`@WebMvcTest` 可以理解为 Controller / Web 层切片测试。

它会加载：

```text
指定的 Controller
Spring MVC 相关组件
MockMvc
JSON 序列化和反序列化
ControllerAdvice，例如 GlobalExceptionHandler
```

它不会加载：

```text
普通 Service Bean
Repository Bean
数据库相关配置
完整 Spring Boot 应用上下文
```

更准确地说，`@WebMvcTest` 不是没有 Spring 上下文，而是只加载 Web 层需要的那一部分 Spring 测试上下文。

### `@MockBean` 和 `@Mock`

`@Mock`：

```text
只在当前测试类中创建 Mockito Mock，不会注册到 Spring 容器。
```

`@MockBean`：

```text
创建 Mockito Mock，并注册到 Spring 测试容器中，用来替换 Controller 依赖的 Bean。
```

因此在 `OrderControllerTest` 中需要：

```java
@MockBean
private OrderQueryService orderQueryService;

@MockBean
private OrderCommandService orderCommandService;
```

否则 `OrderController` 启动时找不到依赖。

### 适合用 `@WebMvcTest` 的场景

当想专门测试 Controller 层时，可以使用 `@WebMvcTest`。

它适合验证：

```text
URL 和 HTTP 方法
路径参数绑定
请求体 JSON 反序列化
参数校验
状态码
响应 JSON
异常响应
Controller 是否调用了正确的 Service
```

它不适合验证：

```text
真实 Service 业务逻辑
Repository 查询
数据库交互
完整业务链路
```

一句话总结：

```text
@WebMvcTest = Controller / Web 层切片测试。
```

## 下一步

继续对比 `@WebMvcTest` 和 `@SpringBootTest`：

```text
@WebMvcTest：只测 Web 层
@SpringBootTest：启动更完整的 Spring 上下文
```

随后进入 `@SpringBootTest + MockMvc`。

## `@SpringBootTest + MockMvc`

`@SpringBootTest + MockMvc` 用来启动更完整的 Spring Boot 测试上下文，但仍然通过 `MockMvc` 在测试进程内部模拟 HTTP 请求。

典型结构：

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerSpringBootTest {

    @Autowired
    private MockMvc mockMvc;
}
```

含义：

```text
@SpringBootTest：
启动完整 Spring Boot 测试上下文。

@AutoConfigureMockMvc：
自动配置 MockMvc。

MockMvc：
不启动真实端口，但可以模拟 HTTP 请求。
```

和 `@WebMvcTest` 的区别：

```text
@WebMvcTest
-> 只加载 Web 层
-> Service 通常用 @MockBean 替代
-> 更轻、更快
-> 适合 Controller 层切片测试

@SpringBootTest + MockMvc
-> 加载更完整的 Spring 上下文
-> Controller、Service、ControllerAdvice 等都是真实 Bean
-> 更重、更慢
-> 适合轻量集成测试
```

当前新增测试类：

```text
src/test/java/com/example/testinglab/order/interfaces/rest/OrderControllerSpringBootTest.java
```

第一个测试目标：

```text
1. 不使用 @MockBean
2. 请求 GET /api/orders/o-1001
3. 使用真实 OrderController 和 OrderQueryService
4. 断言状态码是 200
5. 断言响应 JSON 字段正确
```

这验证的是：

```text
真实 Spring Bean 是否能协作完成一次接口请求。
```

### 真实 Bean 的 404 异常链路

`@SpringBootTest + MockMvc` 也适合验证真实 Bean 之间的异常处理协作。

当前真实业务行为：

```java
if ("o-404".equals(orderId)) {
    throw new OrderNotFoundException(orderId);
}
```

测试请求：

```text
GET /api/orders/o-404
```

完整链路：

```text
MockMvc
-> OrderController
-> OrderQueryService
-> OrderNotFoundException
-> GlobalExceptionHandler
-> 404 + ErrorResponse JSON
```

这个测试和 `@WebMvcTest` 中的 404 测试有一个关键区别：

```text
@WebMvcTest：
OrderQueryService 是 @MockBean，异常由测试 Stub 出来。

@SpringBootTest + MockMvc：
OrderQueryService 是真实 Bean，异常来自真实业务代码。
```

### 真实 Bean 的 POST 请求链路

`@SpringBootTest + MockMvc` 也可以测试 POST 请求在完整 Spring 上下文中的执行结果。

请求：

```json
{
  "productId": "p-1001",
  "quantity": 2
}
```

会经过真实链路：

```text
MockMvc
-> OrderController
-> CreateOrderRequest
-> OrderCommandService
-> OrderResponse
-> JSON
```

当前真实 `OrderCommandService` 行为：

```java
public Order createOrder(String productId, int quantity) {
    return new Order(productId, "Java Testing Book", quantity, new BigDecimal("119.80"));
}
```

所以响应中的 `orderId` 是：

```text
p-1001
```

这和 `@WebMvcTest` 中的 POST 成功测试不同：

```text
@WebMvcTest：
响应由 Mock 的 orderCommandService 决定。

@SpringBootTest + MockMvc：
响应由真实 OrderCommandService 决定。
```

这个差异说明：

```text
@WebMvcTest 更适合只验证 Controller 如何处理 Web 输入输出。
@SpringBootTest + MockMvc 更适合验证 Spring Bean 组合起来后的真实行为。
```

## `@SpringBootTest + MockMvc` 阶段收束

默认情况下：

```java
@SpringBootTest
```

等价于使用 Mock Web 环境：

```text
SpringBootTest.WebEnvironment.MOCK
```

也就是说：

```text
它会启动完整 Spring Boot 测试上下文，
但不会监听真实 HTTP 端口。
```

如果需要模拟 HTTP 请求，需要配合：

```java
@AutoConfigureMockMvc
```

并注入：

```java
MockMvc
```

本阶段核心理解：

```text
@SpringBootTest + MockMvc
-> 完整 Spring 上下文
-> 不启动真实端口
-> 使用 MockMvc 在测试进程内部执行 HTTP 请求
-> 适合测试多个真实 Spring Bean 的协作
```

和 `@WebMvcTest` 的使用边界：

```text
@WebMvcTest：
适合 Controller / Web 层切片测试。
Service 通常用 @MockBean 替代。
重点验证 URL、参数绑定、请求体、校验、状态码、响应 JSON、异常响应。

@SpringBootTest + MockMvc：
适合轻量集成测试。
Controller、Service、ControllerAdvice 等使用真实 Bean。
重点验证完整 Spring 容器中的 Bean 装配和协作。
```

和真实 HTTP 调用的区别：

```text
@SpringBootTest + MockMvc：
不启动真实端口，请求在测试进程内部执行。

真实 HTTP 客户端，例如 Postman / REST Assured：
应用需要监听真实端口，请求通过真实 HTTP 网络调用进入应用。
```

## `RANDOM_PORT` 真实端口测试

下一步可以学习：

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
```

它会启动真实 Web Server，并随机分配一个可用端口。

适合测试：

```text
真实 HTTP 请求
真实端口监听
更接近外部客户端访问方式
```

这类测试比 `MockMvc` 更接近接口自动化测试。
