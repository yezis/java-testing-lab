# Java 自动化测试学习计划

## 学习目标

通过一个逐步扩展的 Java 项目，掌握日常后端开发中常用的自动化测试方法、工具和工程实践。

## 学习方式

后续学习按 `docs/learning-method.md` 中的约定进行：

- 每节结束做小复盘
- 测试代码保持干净
- 测试失败时先分析期望值和实际值
- 逐步减少照抄，增加测试设计
- 每个阶段及时收束
- Fuzzing 放在基础测试之后展开

## 阶段 1：单元测试基础

- JUnit 5 基本结构
- 常用断言
- 异常测试
- 参数化测试
- 生命周期注解
- AssertJ 流式断言

## 阶段 2：Mock 与可测试代码设计

- Mockito 基本使用
- Mock、Stub、Spy 的区别
- 验证方法调用
- 测试 Service 层业务逻辑
- 降低代码对外部依赖的耦合

## 阶段 3：Spring Boot 测试

- `@SpringBootTest`
- `@WebMvcTest`
- MockMvc
- Controller 测试
- Repository 测试

## 阶段 4：接口自动化测试

- REST Assured
- JSON 响应断言
- OpenAPI 契约测试
- 测试数据准备与清理

## 阶段 5：属性测试与 Fuzzing

- 随机测试、属性测试、Fuzzing 的区别
- jqwik 基本使用
- `@Property`
- `@ForAll`
- 输入生成器
- 边界值自动探索
- shrinking：失败用例最小化
- JQF / Jazzer 基本概念
- 适合 Fuzzing 的代码类型：解析器、输入校验、序列化、复杂状态转换
- 不适合 Fuzzing 的代码类型：强依赖外部服务、结果高度不确定的流程

## 阶段 6：数据库与外部服务

- H2 适用场景
- Testcontainers
- WireMock
- 数据库迁移脚本测试

## 阶段 7：持续集成与质量度量

- Maven / Gradle 测试配置
- JaCoCo 测试覆盖率
- GitHub Actions / Jenkins
- 测试报告与失败诊断

## 阶段 8：UI 自动化测试

- Selenium
- Playwright Java
- 页面对象模型
- UI 测试稳定性设计
