# 第 4 课：MyBatis Plus 数据层测试

## 本课目标

这一课开始学习数据层测试。当前项目的数据访问方案采用 MyBatis Plus，并保留 Repository 层：

```text
Service -> Repository -> Mapper -> Database
```

本阶段重点不是让 Service 直接依赖 MyBatis Plus Mapper，而是让基础设施层负责把领域对象保存到数据库。

## 为什么仍然保留 Repository

`Mapper` 是 MyBatis Plus 提供的数据访问接口，关注的是数据库表和 SQL 操作。

`Repository` 是业务层看到的持久化接口，关注的是领域对象的保存和查询。

当前项目中：

```text
OrderService 依赖 OrderRepository
OrderRepositoryImpl 依赖 OrderMapper
OrderMapper 操作 orders 表
```

这样做的好处：

- `application` 层不需要知道 MyBatis Plus
- `domain` 层只定义 Repository 接口
- `infrastructure.persistence` 层负责数据库细节
- 后续替换数据库实现时，业务代码改动更少

## 当前数据测试环境

项目已加入：

```text
mybatis-plus-spring-boot3-starter
H2
```

测试环境使用 H2 内存数据库，不依赖本机真实 MySQL。

测试配置：

```text
src/test/resources/application-test.yml
```

建表脚本：

```text
src/test/resources/schema.sql
```

数据表：

```sql
CREATE TABLE orders (
    product_id VARCHAR(64) PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL
);
```

## 生产代码结构

数据层相关代码在：

```text
src/main/java/com/example/testinglab/order/infrastructure/persistence
```

核心类：

```text
OrderDO
OrderMapper
OrderRepositoryImpl
```

含义：

- `OrderDO`：数据库表对应的数据对象，带有 MyBatis Plus 表映射注解
- `OrderMapper`：继承 `BaseMapper<OrderDO>`，获得 `insert`、`selectById` 等基础数据库操作
- `OrderRepositoryImpl`：实现领域层的 `OrderRepository`，负责 `Order` 与 `OrderDO` 之间的转换，并通过 `OrderMapper` 访问数据库

## 第一个 Repository 集成测试

当前已有测试：

```text
src/test/java/com/example/testinglab/order/infrastructure/persistence/OrderRepositoryImplTest.java
```

测试目标：

```text
调用 OrderRepository.save(order)
实际通过 MyBatis Plus 写入 H2 数据库
再通过 OrderMapper.selectById(...) 查询数据库记录
断言数据库中的字段正确
```

这个测试验证的是：

```text
Repository 实现 -> Mapper -> 数据库表
```

它不是纯单元测试，而是数据层集成测试。

## 常用注解

`@SpringBootTest`

```text
启动完整 Spring Boot 测试上下文，让 Repository、Mapper、DataSource 都成为真实 Bean。
```

`@ActiveProfiles("test")`

```text
使用 application-test.yml 中的测试数据库配置。
```

`@Transactional`

```text
测试方法结束后回滚数据库改动，避免测试数据互相影响。
```

## 后续学习顺序

数据层测试建议按这个顺序推进：

```text
1. Mapper 基础测试：insert / selectById
2. Repository 测试：领域对象和数据库对象转换
3. 查询方法测试：按业务字段查询
4. 异常与约束测试：主键重复、非空字段、非法数据
5. 测试数据隔离：@Transactional、schema.sql、data.sql
6. Testcontainers：使用真实 MySQL 容器替代 H2
```

## 已完成练习

已完成 `OrderMapperTest`。

测试目标：

```text
直接测试 OrderMapper.insert(...) 和 OrderMapper.selectById(...)
确认 MyBatis Plus Mapper 能把 OrderDO 正确写入并查询出来
```

建议测试类：

```text
src/test/java/com/example/testinglab/order/infrastructure/persistence/OrderMapperTest.java
```

建议测试方法：

```text
shouldInsertAndSelectOrderDO()
```

关键 API：

```text
@SpringBootTest
@ActiveProfiles("test")
@Transactional
orderMapper.insert(orderDO)
orderMapper.selectById("p-3001")
assertThat(...)
```

已完成 `OrderRepositoryImplTest` 中的查询测试。

测试目标：

```text
1. 根据 productName 查询到匹配订单列表
2. 数据库中存在干扰数据时，只返回符合条件的数据
3. 查询不到数据时返回空 List
```

已学习的 MyBatis Plus 条件查询 API：

```java
LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
queryWrapper.eq(OrderDO::getProductName, productName);
orderMapper.selectList(queryWrapper);
```

含义：

```text
根据 OrderDO 的 productName 字段生成等值查询条件。
```

## 条件更新

已经新增 `LambdaUpdateWrapper` 示例：

```text
OrderRepository.updateQuantityByProductName(productName, quantity)
```

对应实现：

```java
LambdaUpdateWrapper<OrderDO> updateWrapper = new LambdaUpdateWrapper<>();
updateWrapper.eq(OrderDO::getProductName, productName);
updateWrapper.set(OrderDO::getQuantity, quantity);

return orderMapper.update(updateWrapper);
```

含义：

```text
1. eq 指定要更新哪些记录
2. set 指定要修改哪个字段以及修改后的值
3. update 返回受影响的行数
```

对应测试：

```text
OrderRepositoryImplTest.shouldUpdateQuantityByProductName()
```

这个测试验证：

```text
1. 符合 productName 条件的订单被更新
2. 不符合条件的订单不会被更新
3. 返回值 updatedRows 等于实际更新的记录数
```

## 条件删除

已经新增条件删除示例：

```text
OrderRepository.deleteByProductName(productName)
```

对应实现：

```java
LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
queryWrapper.eq(OrderDO::getProductName, productName);

return orderMapper.delete(queryWrapper);
```

含义：

```text
1. 用 LambdaQueryWrapper 描述要删除哪些记录
2. eq 表示只删除 productName 等于指定值的订单
3. delete 返回受影响的行数
```

对应测试：

```text
OrderRepositoryImplTest.shouldDeleteOrdersByProductName()
```

这个测试验证：

```text
1. 符合 productName 条件的订单被删除
2. 不符合条件的订单仍然保留
3. 返回值 deletedRows 等于实际删除的记录数
```

## 预留但暂不学习的主题

以下 MyBatis Plus 测试主题已经预留，但当前先不展开学习：

```text
1. 字段映射测试：@TableField
2. 自动填充字段测试：MetaObjectHandler、createTime、updateTime
3. 逻辑删除测试：@TableLogic
4. 乐观锁测试：@Version
5. 自定义 SQL 测试：@Select、XML Mapper、多表查询、聚合查询
6. 数据库约束测试：主键重复、非空字段、唯一索引、外键约束
7. 事务测试：@Transactional、业务异常回滚、多步数据库操作一致性
```

后续回到这些主题时，应继续遵守当前约定：

```text
尽量新增独立示例和测试，不反复修改已经完成的测试代码。
```

## Testcontainers + MySQL

当前已开始学习真实数据库测试：

```text
Testcontainers + MySQL
```

它和 H2 内存数据库测试的区别：

```text
H2 测试：
启动快，不依赖 Docker，适合日常快速验证 Repository / Mapper 行为。

Testcontainers + MySQL 测试：
启动真实 MySQL 容器，能发现 H2 无法暴露的 MySQL 方言、字段类型、索引、兼容性问题。
```

项目已新增依赖：

```text
mysql-connector-j
testcontainers-junit-jupiter
testcontainers-mysql
```

项目已新增测试类：

```text
src/test/java/com/example/testinglab/order/infrastructure/persistence/OrderMapperMySqlContainerTest.java
```

核心注解：

```java
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
```

含义：

```text
@SpringBootTest：
启动 Spring Boot 测试上下文。

@Testcontainers(disabledWithoutDocker = true)：
启用 Testcontainers。如果当前机器没有启动 Docker，则跳过该测试，而不是让 mvn test 失败。

@Transactional：
测试结束后回滚测试数据。
```

核心容器定义：

```java
@Container
static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
        .withDatabaseName("testing_lab")
        .withUsername("test")
        .withPassword("test");
```

含义：

```text
测试运行时启动一个 MySQL 8.4 容器。
容器内创建 testing_lab 数据库。
测试使用 test/test 用户连接数据库。
MySQLContainer 会自动处理端口映射、JDBC URL、用户名、密码和基础等待策略。
```

核心动态配置：

```java
@DynamicPropertySource
static void configureMySqlProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mysql::getJdbcUrl);
    registry.add("spring.datasource.username", mysql::getUsername);
    registry.add("spring.datasource.password", mysql::getPassword);
    registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    registry.add("spring.datasource.hikari.initialization-fail-timeout", () -> "60000");
    registry.add("spring.datasource.hikari.connection-timeout", () -> "30000");
    registry.add("spring.sql.init.mode", () -> "always");
}
```

含义：

```text
MySQL 容器每次启动时端口可能不同。
@DynamicPropertySource 会把 MySQLContainer 生成的 JDBC 地址、用户名、密码动态写入 Spring 测试环境。
Spring Boot 再使用这些配置创建 DataSource。
Hikari 连接池增加等待时间，避免 MySQL 刚启动但还没完全接受连接时测试过早失败。
```

当前测试方法：

```text
shouldInsertAndSelectOrderWithRealMySqlContainer()
```

测试目标：

```text
使用真实 MySQL 容器执行 MyBatis Plus insert 和 selectById。
验证 OrderDO 可以真实写入 MySQL，并从 MySQL 查询回来。
```

Docker 未启动时，执行完整测试会跳过该测试：

```text
Tests run: 50, Failures: 0, Errors: 0, Skipped: 1
BUILD SUCCESS
```

Docker 启动并且 mysql:8.4 镜像存在后，可以单独运行：

```bash
mvn test -Dtest=OrderMapperMySqlContainerTest
```

期望结果：

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 简化的容器数据库测试

Spring Boot 3.1 以后可以配合 `spring-boot-testcontainers` 使用 `@ServiceConnection` 简化容器数据库测试。

项目已新增依赖：

```text
spring-boot-testcontainers
```

项目已新增简化版测试类：

```text
src/test/java/com/example/testinglab/order/infrastructure/persistence/OrderMapperMySqlServiceConnectionTest.java
```

核心代码：

```java
@Container
@ServiceConnection
static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
        .withDatabaseName("testing_lab")
        .withUsername("test")
        .withPassword("test");
```

含义：

```text
@ServiceConnection 会让 Spring Boot 自动识别 MySQLContainer。
Spring Boot 会自动从容器中获取 JDBC URL、用户名、密码，并配置 DataSource。
因此这个测试类不需要再写 @DynamicPropertySource。
```

和上一版对比：

```text
@DynamicPropertySource 版本：
显式把 spring.datasource.url、username、password、driver-class-name 注册到 Spring 测试环境。
适合学习底层连接信息是如何传给 Spring 的。

@ServiceConnection 版本：
由 Spring Boot 自动完成容器连接配置。
测试代码更短，更适合真实项目中常见的写法。
```

简化版测试命令：

```bash
mvn test -Dtest=OrderMapperMySqlServiceConnectionTest
```

已验证结果：

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 抽取 MySQL 容器测试基类

当多个测试类都需要同一个 MySQL 容器配置时，不应该在每个测试类里重复写 `@Container`、`@ServiceConnection` 和 Spring 测试配置。

项目已新增测试基类：

```text
src/test/java/com/example/testinglab/support/MySqlServiceConnectionTestBase.java
```

基类负责：

```text
1. 启动 Spring Boot 测试上下文
2. 启用 Testcontainers
3. 启动 mysql:8.4 容器
4. 使用 @ServiceConnection 自动配置 DataSource
5. 使用 @Transactional 回滚测试数据
```

核心代码：

```java
@SpringBootTest(properties = {
        "spring.sql.init.mode=always",
        "spring.datasource.hikari.initialization-fail-timeout=60000",
        "spring.datasource.hikari.connection-timeout=30000"
})
@Testcontainers(disabledWithoutDocker = true)
@Transactional
public abstract class MySqlServiceConnectionTestBase {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("testing_lab")
            .withUsername("test")
            .withPassword("test");
}
```

具体测试类只需要继承它：

```java
class OrderMapperMySqlServiceConnectionTest extends MySqlServiceConnectionTestBase {
    // 只写当前测试类关心的 Mapper、测试数据和断言
}
```

这样做的好处：

```text
容器配置集中管理。
具体测试类更短，只关注业务验证。
后续新增 Repository / Mapper 的真实 MySQL 测试时，可以直接复用基类。
```

已验证命令：

```bash
mvn test -Dtest=OrderMapperMySqlServiceConnectionTest
```

结果：

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Testcontainers 复用方式补充

抽象基类不是官方唯一写法。它只是当前项目为了减少重复配置采用的一种工程化方式。

常见选择：

```text
1. 直接在测试类声明容器：清晰直观，但多个测试类会重复。
2. 抽象基类：集中管理配置，当前项目正在使用。
3. 接口方式：测试类 implements 容器接口，避免占用继承位置。
4. 测试配置类 + @Import / @ImportTestcontainers：更接近 Spring 配置风格。
```

当前先保留：

```text
OrderMapperMySqlServiceConnectionTest extends MySqlServiceConnectionTestBase
```

后续如需进一步简化，可以把容器声明迁移到测试配置类，再由测试类通过 `@Import` 或 `@ImportTestcontainers` 引入。

## 下一步练习

建议继续学习 Testcontainers + MySQL 的第二个用例，例如：

```text
用真实 MySQL 验证唯一主键冲突或非空约束。
```

会用到：

```text
真实 MySQL 容器
数据库约束
assertThatThrownBy
```
