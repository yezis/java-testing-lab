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

## 下一步练习

建议继续学习多条件查询，例如：

```text
根据 productName 和最小 totalAmount 查询订单
```

会用到：

```text
eq
ge
selectList
```
