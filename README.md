# Java Testing Lab

这是一个配合 Java 自动化测试学习使用的实践项目。

项目会随着学习进度逐步增加业务代码、测试代码、工具配置和学习文档。目标不是只看概念，而是每学一个测试知识点，都在真实 Java 代码里写出来、运行起来、理解清楚。

## 当前学习阶段

项目当前主要围绕 Java 自动化测试展开，包括：

- JUnit 5
- AssertJ
- Mockito
- Spring Boot 测试
- MyBatis Plus 数据层测试
- Maven 测试命令

## 环境要求

- Java 17
- Maven 3.9.x

## 运行测试

在项目根目录执行：

```bash
mvn test
```

## 项目结构

```text
src/main/java
```

业务代码目录。

```text
src/test/java
```

测试代码目录。

当前业务代码采用轻量 DDD / 整洁架构分层：

```text
com.example.testinglab
├── common.error
│   ├── ErrorResponse
│   └── GlobalExceptionHandler
├── order
│   ├── domain
│   │   ├── Order
│   │   ├── OrderCalculator
│   │   ├── OrderNotFoundException
│   │   └── OrderRepository
│   ├── application
│   │   ├── OrderService
│   │   ├── OrderQueryService
│   │   └── OrderCommandService
│   ├── infrastructure.persistence
│   │   ├── OrderDO
│   │   ├── OrderMapper
│   │   └── OrderRepositoryImpl
│   └── interfaces.rest
│       ├── OrderController
│       ├── OrderResponse
│       └── CreateOrderRequest
├── product.domain
│   ├── Product
│   └── ProductRepository
└── notification
    ├── domain
    │   └── MessageSender
    └── application
        └── OrderNotificationService
```

分层含义：

- `domain`：业务实体、领域规则、领域异常、Repository 接口
- `application`：应用服务和用例编排
- `infrastructure.persistence`：数据库对象、MyBatis Plus Mapper、Repository 实现
- `interfaces.rest`：HTTP Controller、请求 DTO、响应 DTO
- `common.error`：通用错误响应和全局异常处理

```text
src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker
```

Mockito 测试配置。当前设置为：

```text
mock-maker-subclass
```

用于避免当前 JDK 环境下 Mockito inline mock maker 挂载 Byte Buddy agent 失败。

## 主要业务示例

当前项目以订单创建流程作为测试练习场景。

核心业务类：

- `OrderCalculator`：计算订单金额
- `OrderService`：创建订单
- `OrderRepository`：订单保存接口
- `ProductRepository`：商品查询接口

`OrderService.createOrder(productId, quantity)` 的流程：

```text
1. 查询商品
2. 商品不存在则抛出异常
3. 库存不足则抛出异常
4. 计算订单总价
5. 创建订单对象
6. 保存订单
7. 返回订单
```

## 学习文档

- [Agent 协作说明](AGENTS.md)
- [学习计划](docs/learning-plan.md)
- [学习方式与改进约定](docs/learning-method.md)
- [第 1 课：JUnit 5 单元测试入门](docs/lesson-01-junit5.md)
- [第 2 课：Mockito 与 Service 层单元测试](docs/lesson-02-mockito.md)
- [第 3 课：Spring Boot Web 层测试](docs/lesson-03-spring-boot-test.md)
- [第 4 课：MyBatis Plus 数据层测试](docs/lesson-04-mybatis-plus-data-test.md)
- [当前学习进度交接](docs/session-progress.md)

当前学习进度、环境细节、测试状态和下一步任务记录在：

```text
docs/session-progress.md
```

切换设备或切换 AI 会话时，优先阅读这份进度文件。

## 后续计划

后续会继续补充：

- Mockito 进阶
- Spring Boot 测试
- 集成测试
- Testcontainers
- API 自动化测试
- 属性测试与 Fuzzing
