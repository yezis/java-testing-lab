# Java 自动化测试学习进度交接

更新时间：2026-05-16 19:32 CST

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

Maven：

```text
Apache Maven 3.9.15
/Users/yezi/.local/apache-maven/apache-maven-3.9.15
```

当前命令行环境：

```text
JAVA_HOME=/Users/yezi/.local/jdks/jdk-17.0.19+10/Contents/Home
java=/Users/yezi/.local/jdks/jdk-17.0.19+10/Contents/Home/bin/java
mvn=/Users/yezi/.local/apache-maven/apache-maven-3.9.15/bin/mvn
```

注意：当前 Java 17 安装在隐藏目录 `.local` 下。IDE 如果不能直接选择隐藏目录，可以用“输入路径”或“显示隐藏文件”的方式选择上面的 JDK 路径。

最近一次测试命令：

```bash
mvn test
```

最近一次测试结果：

```text
Tests run: 29, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 主要文件

学习计划：

```text
docs/learning-plan.md
```

学习方式与改进约定：

```text
docs/learning-method.md
```

JUnit 5 笔记：

```text
docs/lesson-01-junit5.md
```

Mockito 笔记：

```text
docs/lesson-02-mockito.md
```

Spring Boot 测试笔记：

```text
docs/lesson-03-spring-boot-test.md
```

MyBatis Plus 数据层测试笔记：

```text
docs/lesson-04-mybatis-plus-data-test.md
```

当前进度交接：

```text
docs/session-progress.md
```

Agent 协作说明：

```text
AGENTS.md
```

## 业务代码

当前项目已重构为轻量 DDD / 整洁架构分层。

通用错误处理：

```text
src/main/java/com/example/testinglab/common/error/ErrorResponse.java
src/main/java/com/example/testinglab/common/error/GlobalExceptionHandler.java
```

订单 domain：

```text
src/main/java/com/example/testinglab/order/domain/Order.java
src/main/java/com/example/testinglab/order/domain/OrderCalculator.java
src/main/java/com/example/testinglab/order/domain/OrderNotFoundException.java
src/main/java/com/example/testinglab/order/domain/OrderRepository.java
```

订单 application：

```text
src/main/java/com/example/testinglab/order/application/OrderService.java
src/main/java/com/example/testinglab/order/application/OrderQueryService.java
src/main/java/com/example/testinglab/order/application/OrderCommandService.java
```

订单 interfaces.rest：

```text
src/main/java/com/example/testinglab/order/interfaces/rest/OrderController.java
src/main/java/com/example/testinglab/order/interfaces/rest/OrderResponse.java
src/main/java/com/example/testinglab/order/interfaces/rest/CreateOrderRequest.java
```

订单 infrastructure.persistence：

```text
src/main/java/com/example/testinglab/order/infrastructure/persistence/OrderDO.java
src/main/java/com/example/testinglab/order/infrastructure/persistence/OrderMapper.java
src/main/java/com/example/testinglab/order/infrastructure/persistence/OrderRepositoryImpl.java
```

商品 domain：

```text
src/main/java/com/example/testinglab/product/domain/Product.java
src/main/java/com/example/testinglab/product/domain/ProductRepository.java
```

通知：

```text
src/main/java/com/example/testinglab/notification/domain/MessageSender.java
src/main/java/com/example/testinglab/notification/application/OrderNotificationService.java
```

## 测试代码

```text
src/test/java/com/example/testinglab/order/domain/OrderCalculatorTest.java
src/test/java/com/example/testinglab/order/domain/OrderCalculatorSpyTest.java
src/test/java/com/example/testinglab/order/application/OrderServiceTest.java
src/test/java/com/example/testinglab/order/interfaces/rest/OrderControllerTest.java
src/test/java/com/example/testinglab/order/interfaces/rest/OrderControllerSpringBootTest.java
src/test/java/com/example/testinglab/order/interfaces/rest/OrderControllerRandomPortTest.java
src/test/java/com/example/testinglab/order/infrastructure/persistence/OrderRepositoryImplTest.java
src/test/java/com/example/testinglab/notification/application/OrderNotificationServiceTest.java
src/test/java/com/example/testinglab/common/config/ApplicationInfoPropertiesTest.java
```

数据层测试资源：

```text
src/test/resources/application-test.yml
src/test/resources/schema.sql
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

## 整体阶段进度

```text
阶段 1：单元测试基础 - 已完成
阶段 2：Mock 与可测试代码设计 - 已完成并收束
阶段 3：Spring Boot 测试 - Web 层测试已完成，Profile 已学习
阶段 4：MyBatis Plus 数据层测试 - 环境已搭建，准备开始 Mapper 测试
阶段 5：接口自动化测试 - 未开始
阶段 6：属性测试与 Fuzzing - 未开始
阶段 7：外部服务与真实依赖测试 - 未开始
阶段 8：持续集成与质量度量 - 未开始
阶段 9：UI 自动化测试 - 未开始
```

当前建议：

```text
下一次学习 MyBatis Plus Mapper 测试，建议新增 OrderMapperTest。
不要为了新知识点反复修改已经完成的 Mockito / Spring Boot Web 测试。
新增代码时遵守当前分层：domain、application、interfaces.rest、infrastructure.persistence、common.error。
```

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

已经学习并在项目中实践：

- `@ExtendWith(MockitoExtension.class)`
- `@Mock`
- `@InjectMocks`
- `when(...).thenReturn(...)`
- `verify(...)`
- `verify(..., never())`
- `verifyNoInteractions(...)`
- 参数匹配器：`any()`、`anyInt()`、`eq(...)`
- matcher 规则
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

### Spring Boot 测试

已完成准备：

- `pom.xml` 已新增 `spring-boot-starter-web`
- `pom.xml` 已新增 `spring-boot-starter-test`
- 新增 `TestingLabApplication`
- 新增 `OrderController`
- 新增 `OrderQueryService`
- 新增 `OrderResponse`
- 新增 `OrderNotFoundException`
- 新增 `ErrorResponse`
- 新增 `GlobalExceptionHandler`
- 新增 `OrderControllerTest`
- 已完成第一个 `@WebMvcTest` + `MockMvc` 测试
- 已完成订单不存在时返回 `404 Not Found` 的异常场景测试
- 已完成统一错误响应 JSON 测试
- 已完成路径参数格式校验和 `400 Bad Request` 测试
- 已完成包结构重构：从按实体平铺改为轻量 DDD / 整洁架构分层
- 已完成 POST /api/orders 请求体 JSON 成功场景测试
- 已完成 POST /api/orders 请求体字段校验失败测试
- 已完成 JSON 反序列化失败测试：quantity = "abc" 返回 400
- 已完成 @WebMvcTest 小阶段问答收束
- 已完成第一个 @SpringBootTest + MockMvc 测试
- 已完成 @SpringBootTest + MockMvc 真实 404 异常链路测试
- 已完成 @SpringBootTest + MockMvc 真实 POST 成功链路测试
- 已完成 @SpringBootTest(webEnvironment = RANDOM_PORT) + TestRestTemplate 测试
- 已完成 @ActiveProfiles("test") 和 application-test.yml 基础使用

### MyBatis Plus 数据层测试

已完成环境搭建：

- `pom.xml` 已新增 `mybatis-plus-spring-boot3-starter`
- `pom.xml` 已新增 H2 测试数据库依赖
- `pom.xml` 已通过 Spring Boot BOM 锁定 Spring 相关依赖版本，避免 MyBatis Plus starter 间接带入不匹配的 Spring Boot 版本
- `src/test/resources/application-test.yml` 已配置 H2 内存数据库
- `src/test/resources/schema.sql` 已创建 `orders` 表
- 新增 `OrderDO`
- 新增 `OrderMapper`
- 新增 `OrderRepositoryImpl`
- 新增 `OrderRepositoryImplTest`

当前数据访问结构：

```text
Service -> Repository -> Mapper -> Database
```

下一步建议：

```text
新增 OrderMapperTest，直接测试 MyBatis Plus Mapper 的 insert 和 selectById。
```
- 已完成 @SpringBootTest + MockMvc 阶段问答收束
- 已完成第一个 @SpringBootTest(webEnvironment = RANDOM_PORT) + TestRestTemplate 测试
- 已完成 RANDOM_PORT 真实端口 404 测试
- 已完成三种 Web 测试方式对比收束
- 已完成测试 Profile：@ActiveProfiles("test") 和 application-test.yml

当前 Spring Boot 示例接口：

```text
GET /api/orders/{orderId}
POST /api/orders
```

当前 Controller 行为：

```text
OrderController.getOrder(orderId)
-> 调用 OrderQueryService.findById(orderId)
-> 找到订单：转换为 OrderResponse 并返回 JSON
-> 订单不存在：抛出 OrderNotFoundException，由 GlobalExceptionHandler 转换为 404 和错误 JSON
```

下一步练习：

```text
学习数据层测试前的准备，或继续补充 Profile 使用边界。
```

三种 Web 测试方式总结：

```text
@WebMvcTest：
Web 层切片上下文，不启动真实端口，使用 MockMvc，常配 @MockBean。

@SpringBootTest + MockMvc：
完整 Spring Boot 测试上下文，不启动真实端口，使用 MockMvc，验证真实 Bean 协作。

@SpringBootTest(RANDOM_PORT) + TestRestTemplate：
完整 Spring Boot 测试上下文，启动真实随机端口，使用真实 HTTP 客户端访问接口。
```

测试 Profile 已完成的测试目标：

```text
1. 新增 application.yml 默认配置：display-name = Java Testing Lab
2. 新增 application-test.yml 测试配置：display-name = Java Testing Lab Test
3. 新增 ApplicationInfoProperties 绑定 testing-lab.application
4. 使用 @SpringBootTest 启动 Spring 上下文
5. 使用 @ActiveProfiles("test") 激活 test profile
6. 注入 ApplicationInfoProperties
7. 断言 displayName = Java Testing Lab Test
```

@SpringBootTest + MockMvc 阶段总结：

```text
@SpringBootTest 启动完整 Spring Boot 测试上下文。
默认 WebEnvironment 是 MOCK，不监听真实 HTTP 端口。
@AutoConfigureMockMvc 自动配置 MockMvc。
MockMvc 在测试进程内部模拟 HTTP 请求。
它适合验证 Controller、Service、ControllerAdvice 等多个真实 Spring Bean 的协作。
它和 @WebMvcTest 最大区别是：@WebMvcTest 只加载 Web 层切片，Service 通常用 @MockBean；@SpringBootTest 加载更完整的应用上下文，使用真实 Bean。
```

RANDOM_PORT 已完成的测试目标：

```text
当请求 GET /api/orders/o-1001 时：
1. 使用 @SpringBootTest(webEnvironment = RANDOM_PORT) 启动真实随机端口
2. 使用 @LocalServerPort 获取端口
3. 使用 TestRestTemplate 发起真实 HTTP 请求
4. 响应类型使用 OrderResponse.class
5. 断言响应字段 orderId、productName、quantity、totalAmount
```

RANDOM_PORT 已完成的 404 测试目标：

```text
当请求 GET /api/orders/o-404 时：
1. 使用真实随机端口和 TestRestTemplate
2. 使用 getForEntity 获取状态码和响应体
3. 断言 HTTP 状态码是 404
4. 断言响应体 code = ORDER_NOT_FOUND
5. 断言响应体 message = order not found: o-404
```

@WebMvcTest 阶段总结：

```text
@WebMvcTest 是 Controller / Web 层切片测试。
它加载 Controller、Spring MVC、MockMvc、JSON 序列化/反序列化、ControllerAdvice。
它不加载普通 Service、Repository、数据库配置和完整 Spring Boot 应用上下文。
Controller 依赖的 Service 通常用 @MockBean 替代。
它适合验证 URL、HTTP 方法、参数绑定、请求体、参数校验、状态码、响应 JSON 和异常响应。
```

@SpringBootTest + MockMvc 已完成的测试目标：

```text
当请求 GET /api/orders/o-1001 时：
1. 使用 @SpringBootTest 启动完整 Spring Boot 测试上下文
2. 使用 @AutoConfigureMockMvc 注入 MockMvc
3. 不使用 @MockBean
4. 通过真实 OrderController 调用真实 OrderQueryService
5. 断言 HTTP 状态码是 200
6. 断言响应 JSON 字段 orderId、productName、quantity、totalAmount
```

@SpringBootTest + MockMvc 已完成的 404 测试目标：

```text
当请求 GET /api/orders/o-404 时：
1. 不使用 @MockBean
2. 通过真实 OrderController 调用真实 OrderQueryService
3. OrderQueryService 抛出 OrderNotFoundException
4. GlobalExceptionHandler 处理异常
5. 断言 HTTP 状态码是 404
6. 断言 JSON 字段 code = ORDER_NOT_FOUND
7. 断言 JSON 字段 message = order not found: o-404
```

@SpringBootTest + MockMvc 已完成的 POST 测试目标：

```text
当请求 POST /api/orders 时：
1. 不使用 @MockBean
2. 请求体 JSON 包含 productId = p-1001、quantity = 2
3. 通过真实 OrderController 调用真实 OrderCommandService
4. 断言 HTTP 状态码是 200
5. 断言响应 JSON 字段 orderId = p-1001
6. 断言响应 JSON 字段 productName、quantity、totalAmount
```

已完成的测试目标：

```text
当请求 GET /api/orders/o-1001 时：
1. Mock OrderQueryService.findById("o-1001") 返回一个 Order
2. MockMvc 发起 GET 请求
3. 断言 HTTP 状态码是 200
4. 断言 JSON 字段 productName、quantity、totalAmount 正确
5. 验证 orderQueryService.findById("o-1001") 被调用
```

已完成的异常测试目标：

```text
当请求 GET /api/orders/o-404 时：
1. Mock OrderQueryService.findById("o-404") 抛出 OrderNotFoundException
2. MockMvc 发起 GET 请求
3. 断言 HTTP 状态码是 404
4. 断言 JSON 字段 code = ORDER_NOT_FOUND
5. 断言 JSON 字段 message = order not found: o-404
6. 验证 orderQueryService.findById("o-404") 被调用
```

已完成的参数校验测试目标：

```text
当请求 GET /api/orders/1-123 时：
1. orderId 不符合 o-数字 格式
2. MockMvc 发起 GET 请求
3. 断言 HTTP 状态码是 400
4. 断言 JSON 字段 code = INVALID_REQUEST
5. 验证 orderQueryService 没有任何交互
```

已完成的 POST 请求体测试目标：

```text
当请求 POST /api/orders 时：
1. 请求体 JSON 包含 productId = p-1001、quantity = 2
2. Mock OrderCommandService.createOrder("p-1001", 2) 返回一个 Order
3. MockMvc 发起 POST 请求并设置 Content-Type = application/json
4. 断言 HTTP 状态码是 200
5. 断言响应 JSON 字段 orderId、productName、quantity、totalAmount
6. 验证 orderCommandService.createOrder("p-1001", 2) 被调用
```

已完成的 POST 请求体校验失败测试目标：

```text
当请求 POST /api/orders 且 quantity = 0 时：
1. MockMvc 发起 POST 请求并设置 Content-Type = application/json
2. 请求体 JSON 包含 productId = p-1001、quantity = 0
3. @Valid 校验 CreateOrderRequest
4. quantity 不满足 @Min(1)
5. 断言 HTTP 状态码是 400
6. 断言 JSON 字段 code = INVALID_REQUEST
7. 断言 JSON 字段 message 包含 quantity must be greater than or equal to 1
8. 验证 orderCommandService 没有任何交互
```

已完成的 JSON 反序列化失败测试目标：

```text
当请求 POST /api/orders 且 quantity = "abc" 时：
1. MockMvc 发起 POST 请求并设置 Content-Type = application/json
2. 请求体 JSON 中 quantity 是字符串，无法转换为 int
3. Spring MVC 抛出 HttpMessageNotReadableException
4. GlobalExceptionHandler 转换为 400 + ErrorResponse
5. 断言 HTTP 状态码是 400
6. 断言 JSON 字段 code = INVALID_REQUEST
7. 断言 JSON 字段 message 包含 request body is not readable
8. 验证 orderCommandService 没有任何交互
```

本节遇到并修复的问题：

```text
OrderController 中 @PathVariable 原本没有显式写变量名。
MockMvc 测试运行时，Spring 无法通过反射获取参数名，导致路径变量绑定失败。
已修改为 @PathVariable("orderId") String orderId。
```
