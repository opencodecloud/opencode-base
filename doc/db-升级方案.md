# opencode-base-db 全面兼容 MyBatis / MyBatis-Plus 升级方案

## 1. 背景与目标

### 当前状态
`opencode-base-db` 有 55 个 Java 文件，是一个轻量级 JDBC/ORM-lite 框架，提供 SQL Builder、JdbcTemplate、EntityMeta、事务管理、连接池、方言等能力。

### 目标状态
升级 `opencode-base-db`，使其成为 MyBatis 和 MyBatis-Plus 的 **drop-in replacement**。项目仅需将 Maven 依赖从 `mybatis` / `mybatis-plus` 替换为 `opencode-base-db` 相关模块即可正常编译运行。

需新增 ~210 个类/接口，提供完整的 `org.apache.ibatis.*` 和 `com.baomidou.mybatisplus.*` API surface。

---

## 2. 架构方案：新建两个兼容模块

```
opencode-base-db-mybatisplus
    -> opencode-base-db-mybatis
        -> opencode-base-db          (核心引擎)
        -> opencode-base-xml         (XML mapper 解析)
        -> opencode-base-expression  (动态 SQL 表达式，替代 OGNL)
        -> opencode-base-reflect     (Mapper 代理、Lambda 列名提取)
    -> opencode-base-reflect
```

| 模块 | 包名 | 用途 |
|------|------|------|
| `opencode-base-db`（现有） | `cloud.opencode.base.db.*` | 核心引擎，按需增强 |
| `opencode-base-db-mybatis`（**新建**） | `org.apache.ibatis.*` | MyBatis 完整 API surface |
| `opencode-base-db-mybatisplus`（**新建**） | `com.baomidou.mybatisplus.*` | MyBatis-Plus API surface |

### 为何分模块而非单模块
- JPMS 不允许 split package，各模块需独立 `module-info.java`
- 用户按需引入（仅用 MyBatis 的不需要 Plus 依赖）
- 与原生 MyBatis/MyBatis-Plus 的依赖关系一致

---

## 3. 复用已有模块的关键能力

| 已有模块 | 复用点 |
|---------|--------|
| `opencode-base-db` -> `JdbcTemplate` | SqlSession 的所有 SQL 执行最终委托此类 |
| `opencode-base-db` -> `EntityMeta` | BaseMapper 的实体元数据解析基础 |
| `opencode-base-db` -> `Condition/Select/Insert/Update/Delete` | Wrapper -> SQL 转换 |
| `opencode-base-db` -> `TransactionManager` | MyBatis Transaction 接口的底层实现 |
| `opencode-base-db` -> `SimpleConnectionPool` | PooledDataSource 的底层实现 |
| `opencode-base-db` -> `Dialect` | 分页 SQL、方言特定语法 |
| `opencode-base-xml` -> `DomParser/XPathQuery` | 解析 mybatis-config.xml 和 mapper.xml |
| `opencode-base-expression` -> `OpenExpression.eval()` | 替代 OGNL，评估 `<if test="...">` |
| `opencode-base-reflect` -> `ProxyFactory` | 创建 Mapper 接口的动态代理 |
| `opencode-base-reflect` -> `SerializedLambdaWrapper` | LambdaQueryWrapper 从 `User::getName` 提取列名 |
| `opencode-base-cache` | L1/L2 缓存实现 |

---

## 4. 分阶段实施计划

### Phase 1: MyBatis 核心 API（~60 类）

#### 1.1 模块脚手架
- 新建 `opencode-base-db-mybatis/pom.xml`、`module-info.java`

#### 1.2 Configuration 体系（~12 类）
- `Configuration`、`Environment`、`MappedStatement`、`BoundSql`
- `ResultMap`/`ResultMapping`、`ParameterMap`/`ParameterMapping`
- `SqlCommandType`、`StatementType`、`RowBounds`、`ResultHandler`/`ResultContext`

#### 1.3 SqlSession / Factory / Builder（~8 类）
- `SqlSession`（接口）、`SqlSessionFactory`、`SqlSessionFactoryBuilder`
- `DefaultSqlSession`（委托 `JdbcTemplate`）、`DefaultSqlSessionFactory`
- `SqlSessionManager`、`ExecutorType`、`TransactionIsolationLevel`

#### 1.4 Mapper 代理系统（~6 类）
- `MapperRegistry`、`MapperProxy`（使用 `ProxyFactory`）
- `MapperProxyFactory`、`MapperMethod`

#### 1.5 注解（~20 个）
- `@Select`/`@Insert`/`@Update`/`@Delete`、`@Param`、`@Results`/`@Result`
- `@SelectProvider`/`@InsertProvider`/`@UpdateProvider`/`@DeleteProvider`
- `@Mapper`、`@Options`、`@SelectKey`、`@CacheNamespace` 等

#### 1.6 TypeHandler 体系（~20 类）
- `TypeHandler<T>`、`BaseTypeHandler<T>`、`TypeHandlerRegistry`、`JdbcType`
- 30+ 内置类型处理器、`TypeAliasRegistry`

---

### Phase 2: 动态 SQL + XML Mapper 解析（~35 类）

#### 2.1 动态 SQL 节点（~15 类）
- `SqlNode`、`DynamicContext`
- `IfSqlNode`（用 `OpenExpression.eval()` 评估 test 条件）
- `ChooseSqlNode`、`WhereSqlNode`、`SetSqlNode`、`TrimSqlNode`
- `ForEachSqlNode`、`MixedSqlNode`、`BindSqlNode`

#### 2.2 SQL Source（~5 类）
- `SqlSource`、`SqlSourceBuilder`（`#{param}` -> `?` 占位符转换）
- `RawSqlSource`、`DynamicSqlSource`、`DefaultParameterHandler`

#### 2.3 XML Mapper 解析（~10 类）
- `XMLConfigBuilder`（用 `DomParser` + `XPathQuery` 解析 mybatis-config.xml）
- `XMLMapperBuilder`、`XMLStatementBuilder`、`XMLIncludeTransformer`
- `MapperBuilderAssistant`、`MapperAnnotationBuilder`

---

### Phase 3: Executor、Plugin、Cache（~25 类）

#### 3.1 Executor 层级（~8 类）
- `Executor`、`SimpleExecutor`/`ReuseExecutor`/`BatchExecutor`（委托 `JdbcTemplate`）
- `CachingExecutor`、`CacheKey`

#### 3.2 Handler（~6 类）
- `StatementHandler`、`PreparedStatementHandler`
- `ResultSetHandler`（用 `BeanMapper`/`RecordMapper`）、`ParameterHandler`

#### 3.3 Plugin/Interceptor（~5 类）
- `Interceptor`、`Invocation`、`Plugin.wrap()`（用 `ProxyFactory`）
- `@Intercepts`、`@Signature`

#### 3.4 Cache（~6 类）
- `Cache` 接口、`PerpetualCache`
- LRU/FIFO/Soft/Weak 装饰器（委托 `opencode-base-cache`）

---

### Phase 4: Transaction + DataSource 桥接（~10 类）

- `Transaction`/`JdbcTransaction`/`ManagedTransaction`（包装 `TransactionManager`）
- `PooledDataSource`（包装 `SimpleConnectionPool`）、`UnpooledDataSource`

---

### Phase 5: MyBatis-Plus BaseMapper + Wrapper（~40 类）

#### 5.1 模块脚手架
- 新建 `opencode-base-db-mybatisplus/pom.xml`、`module-info.java`

#### 5.2 Plus 注解（~10 个）
- `@TableName`、`@TableId`（`IdType`）、`@TableField`（`FieldFill`）
- `@TableLogic`、`@Version`、`@EnumValue`、`@OrderBy`

#### 5.3 BaseMapper<T>（18 个方法）
- `insert`、`deleteById`、`deleteBatchIds`、`delete(Wrapper)`
- `updateById`、`update(T, Wrapper)`
- `selectById`、`selectBatchIds`、`selectOne(Wrapper)`、`selectList(Wrapper)`
- `selectCount(Wrapper)`、`selectPage(Page, Wrapper)` 等
- 实现：通过 `EntityMeta` + SQL Builder 动态构建 SQL

#### 5.4 Wrapper 体系（~12 类）
- `QueryWrapper<T>`（字段名条件）-> 内部转换为 `Condition`
- `LambdaQueryWrapper<T>`（用 `SerializedLambdaWrapper.getPropertyName()` 从 `User::getName` 提取列名）
- `UpdateWrapper<T>`、`LambdaUpdateWrapper<T>`
- 链式方法：`eq`/`ne`/`gt`/`ge`/`lt`/`le`/`between`/`like`/`in`/`isNull`/`orderByAsc`/`groupBy` 等

#### 5.5 分页（~5 类）
- `IPage<T>`、`Page<T>`、`PaginationInnerInterceptor`（用 `Dialect.getLimitSql()`）

#### 5.6 IService<T>（~3 类）
- `IService<T>`（~30 个方法）、`ServiceImpl<M, T>`（委托 BaseMapper）

---

### Phase 6: MyBatis-Plus 高级特性（~25 类）

- `MybatisPlusInterceptor` + `InnerInterceptor` 链
- `OptimisticLockerInnerInterceptor`（自动递增 `@Version`）
- `TenantLineInnerInterceptor`（自动注入 tenant_id 条件）
- `BlockAttackInnerInterceptor`（阻止无 WHERE 的全表更新/删除）
- `MetaObjectHandler`（自动填充 createTime/updateTime）
- 逻辑删除（`@TableLogic`：DELETE -> UPDATE SET deleted=1）
- `Db` 静态工具类

---

### Phase 7: 收尾（~15 类）

- `MetaObject`/`MetaClass`（包装 `opencode-base-reflect`）
- `Cursor<T>` 流式结果集
- `Log`/`LogFactory` 日志桥接
- `PersistenceException`/`TooManyResultsException`
- 代码生成器（`AutoGenerator`）-- **可选/延期**，非运行时依赖

---

## 5. 关键技术决策

### OGNL 替代
`opencode-base-expression` 已支持 90%+ 实际 OGNL 用法（属性访问、比较、逻辑运算、方法调用）。对于 `@Class@staticMethod` 等 OGNL 特有语法，提供 regex 预处理器转换。

### Mapper 代理
使用 `ProxyFactory.forInterface(UserMapper.class).handler(new MapperProxy(...)).create()`，与 MyBatis 的 JDK 动态代理行为一致。

### Lambda 列名提取
`SerializedLambdaWrapper.from(User::getName).getPropertyName()` -> `"name"` -> 通过命名策略转为 `"user_name"`，完美匹配 MyBatis-Plus 的 `SFunction` 机制。

### XML Mapper 解析
`DomParser.parse(inputStream)` + `XPathQuery` 提取 `<select>`/`<insert>` 等元素，构建 `MappedStatement`。

---

## 6. 风险分析

| 风险 | 级别 | 缓解措施 |
|------|------|----------|
| JPMS split-package 冲突 | 高 | 用户必须完全替换，不能同时存在原始 MyBatis JAR（classpath 模式下无此限制） |
| OGNL 表达式不兼容 | 高 | 覆盖 90%+ 常用场景，文档标注不支持的语法 |
| API 覆盖度不完整 | 高 | 优先实现 80/20 最常用 API，用真实项目验证编译通过 |
| 工作量巨大（~210 类） | 高 | 严格分阶段，每阶段独立可交付、可测试 |

---

## 7. 测试策略

- 每个类配套 JUnit 5 + AssertJ + Mockito 测试（嵌套 `@Nested` 分类）
- H2 内存数据库做集成测试
- 兼容性测试：取 MyBatis/MyBatis-Plus 官方文档示例代码，验证在新 API 下编译通过且行为正确
- 预估新增 ~580 个测试

---

## 8. 产出汇总

| 阶段 | 类数 | 内容 |
|------|------|------|
| Phase 1 | ~60 | MyBatis 核心：SqlSession、Configuration、Mapper 代理、注解、TypeHandler |
| Phase 2 | ~35 | 动态 SQL、XML Mapper 解析、Expression 桥接 |
| Phase 3 | ~25 | Executor、Plugin/Interceptor、Cache |
| Phase 4 | ~10 | Transaction、DataSource 桥接 |
| Phase 5 | ~40 | MyBatis-Plus：BaseMapper、Wrapper、IService、分页 |
| Phase 6 | ~25 | Plus 高级：乐观锁、多租户、逻辑删除、自动填充 |
| Phase 7 | ~15 | 反射桥接、日志、异常、收尾 |
| **合计** | **~210** | **完整 drop-in replacement** |

---

## 9. 迁移指南（用户侧）

### Maven 依赖替换

**替换前（原始 MyBatis）：**
```xml
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.5.x</version>
</dependency>
```

**替换后：**
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-db-mybatis</artifactId>
    <version>1.0.0</version>
</dependency>
```

**替换前（原始 MyBatis-Plus）：**
```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.x</version>
</dependency>
```

**替换后：**
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-db-mybatisplus</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 已知限制
- 不支持 OGNL 特有语法（如 `@java.lang.Math@max(a,b)`），建议改用标准表达式
- 代码生成器（`AutoGenerator`）暂不提供，后续版本补充
- JPMS 模式下不可与原始 MyBatis/MyBatis-Plus JAR 共存
- classpath 模式下无此限制，但建议完全替换避免冲突
