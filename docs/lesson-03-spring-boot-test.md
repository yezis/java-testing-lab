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

## 下一步

继续学习请求参数校验，例如：

```text
GET /api/orders/{orderId}
orderId 为空或格式不合法时，接口应该返回 400。
```

这会引出新的 Spring Boot 测试知识点：参数校验、`@Valid`、`@Validated` 和 400 响应测试。
