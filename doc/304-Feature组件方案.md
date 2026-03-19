# Feature 组件方案

## 1. 组件概述

### 1.1 设计目标

`opencode-base-feature` 模块提供功能开关（Feature Toggle / Feature Flag）能力，支持灰度发布、A/B 测试、动态配置和审计日志。

**核心特性：**
- 功能开关管理（注册、查询、启用、禁用、删除）
- 多种启用策略（百分比灰度、用户白名单、日期范围、租户感知、一致性哈希、表达式、组合策略）
- A/B 测试支持（VariantRouter 变体路由器）
- 方法级功能门控（FeatureProxy 动态代理 + @FeatureToggle 注解）
- 可插拔存储（内存、文件、LRU、Redis、缓存装饰器）
- 变更监听与审计日志
- 安全功能管理（权限控制 + 审计）

### 1.2 架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                        Application Layer                         │
│                    (业务代码检查功能开关)                          │
└─────────────────────────────────────────────────────────────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    ▼                           ▼
           ┌───────────────┐           ┌───────────────┐
           │  isEnabled()  │           │  register()   │
           │   检查功能     │           │   注册功能     │
           └───────────────┘           └───────────────┘
                    │                           │
                    ▼                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Facade Layer                             │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                     OpenFeature                           │    │
│  │   (入口类：功能注册/检查/动态更新/监听器管理)               │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                                  │
          ┌───────────────────────┼───────────────────────┐
          ▼                       ▼                       ▼
┌───────────────┐        ┌───────────────┐        ┌───────────────┐
│  Model Layer  │        │Strategy Layer │        │  Proxy Layer  │
│ Feature       │        │ EnableStrategy│        │ FeatureProxy  │
│ FeatureContext│        │ AlwaysOn/Off  │        │ VariantRouter │
│               │        │ Percentage    │        │ @FeatureToggle│
│               │        │ UserList      │        │ @FeatureVariant│
│               │        │ DateRange     │        └───────────────┘
│               │        │ Composite     │
│               │        │ TenantAware   │
│               │        │ Consistent%   │
│               │        │ Expression    │
└───────────────┘        └───────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                          Store Layer                             │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐  │
│  │InMemoryFeatureStore│  │ FileFeatureStore │  │LruFeatureStore│  │
│  └──────────────────┘  └──────────────────┘  └──────────────┘  │
│  ┌──────────────────┐  ┌──────────────────┐                    │
│  │RedisFeatureStore │  │CachedFeatureStore│                    │
│  └──────────────────┘  └──────────────────┘                    │
└─────────────────────────────────────────────────────────────────┘
                                  │
              ┌───────────────────┼───────────────────┐
              ▼                   ▼                   ▼
┌───────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  Listener Layer   │  │  Security Layer  │  │   Audit Layer    │
│ FeatureListener   │  │SecureFeatureMgr  │  │FeatureAuditEvent │
│                   │  │ AuditLogger      │  │FileAuditLogger   │
│                   │  │                  │  │MetricsListener   │
└───────────────────┘  └──────────────────┘  └──────────────────┘
```

### 1.3 模块依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-feature</artifactId>
    <version>${version}</version>
</dependency>
```

---

## 2. 包结构

```
cloud.opencode.base.feature
├── OpenFeature.java              # 功能管理器（门面入口类）
├── Feature.java                  # 功能定义（record）
├── FeatureContext.java           # 功能上下文（record）
│
├── strategy/                     # 启用策略
│   ├── EnableStrategy.java                # 策略接口
│   ├── AlwaysOnStrategy.java              # 始终启用
│   ├── AlwaysOffStrategy.java             # 始终禁用
│   ├── PercentageStrategy.java            # 百分比灰度
│   ├── ConsistentPercentageStrategy.java  # 一致性哈希百分比
│   ├── UserListStrategy.java              # 用户白名单
│   ├── DateRangeStrategy.java             # 日期范围
│   ├── TenantAwareStrategy.java           # 租户感知策略
│   ├── CompositeStrategy.java             # 组合策略（AND/OR）
│   └── ExpressionStrategy.java            # 表达式策略（可选模块委托）
│
├── store/                        # 存储
│   ├── FeatureStore.java         # 存储接口
│   ├── InMemoryFeatureStore.java # 内存存储
│   ├── FileFeatureStore.java     # 文件存储（properties 格式）
│   ├── LruFeatureStore.java      # LRU 缓存存储
│   ├── RedisFeatureStore.java    # Redis 分布式存储
│   └── CachedFeatureStore.java   # 缓存装饰器（可选 Cache 模块委托）
│
├── proxy/                        # 代理
│   ├── FeatureProxy.java         # 功能门控动态代理
│   └── VariantRouter.java        # A/B 测试变体路由器
│
├── annotation/                   # 注解
│   ├── FeatureToggle.java        # 功能开关注解
│   └── FeatureVariant.java       # 功能变体注解
│
├── listener/                     # 监听器
│   └── FeatureListener.java      # 功能变更监听器（函数式接口）
│
├── security/                     # 安全
│   ├── SecureFeatureManager.java # 安全功能管理器
│   └── AuditLogger.java          # 审计日志接口
│
├── audit/                        # 审计
│   ├── FeatureAuditEvent.java    # 审计事件（record）
│   ├── FileAuditLogger.java      # 文件审计日志
│   └── MetricsFeatureListener.java # 指标监听器
│
└── exception/                    # 异常
    ├── FeatureException.java            # 功能异常基类
    ├── FeatureNotFoundException.java    # 功能不存在
    ├── FeatureConfigException.java      # 配置异常
    ├── FeatureStoreException.java       # 存储异常
    ├── FeatureSecurityException.java    # 安全异常
    └── FeatureErrorCode.java            # 错误码枚举
```

---

## 3. 核心 API

### 3.1 Feature -- 功能定义

不可变 record，包含功能开关的全部元数据。

```java
public record Feature(
    String key,                    // 功能键（唯一标识）
    String name,                   // 功能名称
    String description,            // 描述
    boolean defaultEnabled,        // 默认是否启用
    EnableStrategy strategy,       // 启用策略
    Map<String, Object> metadata,  // 元数据
    Instant createdAt,             // 创建时间
    Instant updatedAt              // 更新时间
) { ... }
```

**主要方法：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `builder(String key)` | `key` - 功能键 | `Builder` | 静态工厂，创建构建器 |
| `isEnabled()` | 无 | `boolean` | 使用空上下文检查是否启用 |
| `isEnabled(FeatureContext context)` | `context` - 功能上下文 | `boolean` | 使用指定上下文检查是否启用 |
| `getMetadata(String key)` | `key` - 元数据键 | `<T> T` | 获取元数据值 |
| `getMetadata(String key, T defaultValue)` | `key` - 元数据键, `defaultValue` - 默认值 | `<T> T` | 获取元数据值，不存在时返回默认值 |
| `withStrategy(EnableStrategy newStrategy)` | `newStrategy` - 新策略 | `Feature` | 返回使用新策略的 Feature 副本 |

**Builder 方法：**

| 方法 | 参数 | 说明 |
|------|------|------|
| `name(String name)` | 功能名称 | 设置名称 |
| `description(String desc)` | 描述 | 设置描述 |
| `defaultEnabled(boolean enabled)` | 默认启用 | 设置默认启用状态 |
| `strategy(EnableStrategy strategy)` | 策略 | 设置启用策略 |
| `alwaysOn()` | 无 | 设为始终启用 |
| `alwaysOff()` | 无 | 设为始终禁用 |
| `percentage(int percent)` | 百分比 0-100 | 设为百分比灰度 |
| `forUsers(String... userIds)` | 用户 ID 列表 | 设为用户白名单 |
| `forUsers(Set<String> userIds)` | 用户 ID 集合 | 设为用户白名单 |
| `metadata(String key, Object value)` | 键值对 | 添加元数据 |
| `metadata(Map<String, Object> metadata)` | 元数据 Map | 批量设置元数据 |
| `build()` | 无 | 构建 Feature |

**代码示例：**

```java
// 始终启用
Feature alwaysOn = Feature.builder("new-ui")
    .name("新版 UI")
    .alwaysOn()
    .build();

// 百分比灰度
Feature grayRelease = Feature.builder("dark-mode")
    .name("深色模式")
    .percentage(10)
    .metadata("owner", "ui-team")
    .build();

// 用户白名单
Feature betaFeature = Feature.builder("beta-feature")
    .name("Beta 功能")
    .forUsers("user1", "user2", "user3")
    .build();

// 检查是否启用
boolean enabled = alwaysOn.isEnabled();
boolean enabledForUser = grayRelease.isEnabled(FeatureContext.ofUser("user-123"));
```

### 3.2 FeatureContext -- 功能上下文

不可变 record，封装功能检查时的上下文信息。

```java
public record FeatureContext(
    String userId,                   // 用户 ID
    String tenantId,                 // 租户 ID
    Map<String, Object> attributes   // 自定义属性
) { ... }
```

**主要方法：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `empty()` | 无 | `FeatureContext` | 创建空上下文 |
| `ofUser(String userId)` | 用户 ID | `FeatureContext` | 创建包含用户 ID 的上下文 |
| `ofTenant(String tenantId)` | 租户 ID | `FeatureContext` | 创建包含租户 ID 的上下文 |
| `of(String userId, String tenantId)` | 用户 ID, 租户 ID | `FeatureContext` | 创建包含用户和租户 ID 的上下文 |
| `builder()` | 无 | `Builder` | 创建构建器 |
| `getAttribute(String key)` | 属性键 | `<T> T` | 获取属性值 |
| `getAttribute(String key, T defaultValue)` | 属性键, 默认值 | `<T> T` | 获取属性值，不存在时返回默认值 |
| `hasUserId()` | 无 | `boolean` | 是否包含用户 ID |
| `hasTenantId()` | 无 | `boolean` | 是否包含租户 ID |

**代码示例：**

```java
// 空上下文
FeatureContext empty = FeatureContext.empty();

// 用户上下文
FeatureContext userCtx = FeatureContext.ofUser("user-123");

// 完整上下文
FeatureContext ctx = FeatureContext.builder()
    .userId("user-123")
    .tenantId("tenant-456")
    .attribute("role", "admin")
    .attribute("age", 25)
    .build();

String role = ctx.getAttribute("role"); // "admin"
```

### 3.3 OpenFeature -- 门面入口类

功能管理器，提供功能注册、检查、动态更新和监听器管理。支持单例模式和自定义实例。

**主要方法：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `getInstance()` | 无 | `OpenFeature` | 获取全局单例 |
| `create(FeatureStore store)` | 存储实现 | `OpenFeature` | 创建自定义实例 |
| `resetInstance()` | 无 | `void` | 重置全局单例 |
| `register(Feature feature)` | 功能定义 | `void` | 注册功能 |
| `registerAll(Feature... features)` | 多个功能 | `void` | 批量注册 |
| `isEnabled(String key)` | 功能键 | `boolean` | 检查功能是否启用 |
| `isEnabled(String key, FeatureContext ctx)` | 功能键, 上下文 | `boolean` | 带上下文检查 |
| `isEnabledForUser(String key, String userId)` | 功能键, 用户 ID | `boolean` | 检查对某用户是否启用 |
| `ifEnabled(String key, Runnable action)` | 功能键, 动作 | `void` | 条件执行 |
| `ifEnabled(String key, FeatureContext ctx, Runnable action)` | 功能键, 上下文, 动作 | `void` | 带上下文条件执行 |
| `ifEnabled(String key, Supplier<T> enabled, Supplier<T> disabled)` | 功能键, 启用回调, 禁用回调 | `<T> T` | 条件返回值 |
| `ifEnabled(String key, FeatureContext ctx, Supplier<T> enabled, Supplier<T> disabled)` | 功能键, 上下文, 启用回调, 禁用回调 | `<T> T` | 带上下文条件返回值 |
| `get(String key)` | 功能键 | `Optional<Feature>` | 获取功能定义 |
| `getOrThrow(String key)` | 功能键 | `Feature` | 获取功能，不存在则抛异常 |
| `enable(String key)` | 功能键 | `void` | 启用功能 |
| `disable(String key)` | 功能键 | `void` | 禁用功能 |
| `updateStrategy(String key, EnableStrategy strategy)` | 功能键, 新策略 | `void` | 更新策略 |
| `delete(String key)` | 功能键 | `boolean` | 删除功能 |
| `getAllKeys()` | 无 | `Set<String>` | 获取所有功能键 |
| `getAll()` | 无 | `Map<String, Feature>` | 获取所有功能 |
| `exists(String key)` | 功能键 | `boolean` | 检查功能是否存在 |
| `size()` | 无 | `int` | 获取功能数量 |
| `clear()` | 无 | `void` | 清空所有功能 |
| `addListener(FeatureListener listener)` | 监听器 | `void` | 添加变更监听器 |
| `removeListener(FeatureListener listener)` | 监听器 | `void` | 移除监听器 |
| `getStore()` | 无 | `FeatureStore` | 获取存储实现 |
| `setStore(FeatureStore store)` | 存储实现 | `void` | 设置存储实现 |

**代码示例：**

```java
OpenFeature fm = OpenFeature.getInstance();

// 注册功能
fm.registerAll(
    Feature.builder("new-ui").name("新版 UI").alwaysOn().build(),
    Feature.builder("dark-mode").name("深色模式").percentage(10).build(),
    Feature.builder("beta-feature").forUsers("user1", "user2").build()
);

// 简单检查
if (fm.isEnabled("new-ui")) {
    showNewUI();
}

// 带用户上下文
boolean enabled = fm.isEnabledForUser("beta-feature", currentUser.getId());

// 条件执行
fm.ifEnabled("dark-mode", () -> applyDarkTheme());

// 条件返回值
String theme = fm.ifEnabled("dark-mode",
    () -> "dark",
    () -> "light"
);

// 动态更新
fm.disable("problematic-feature");
fm.updateStrategy("new-feature", new PercentageStrategy(50));

// 监听变更
fm.addListener((key, oldValue, newValue) -> {
    log.info("Feature {} changed: {} -> {}", key, oldValue, newValue);
});
```

---

## 4. 启用策略

### 4.1 EnableStrategy -- 策略接口

```java
@FunctionalInterface
public interface EnableStrategy {
    boolean isEnabled(Feature feature, FeatureContext context);
}
```

所有策略实现 `EnableStrategy` 接口。可用 lambda 表达式自定义策略。

### 4.2 内置策略

| 策略类 | 说明 | 关键方法/字段 |
|--------|------|--------------|
| `AlwaysOnStrategy` | 始终启用 | `INSTANCE` 单例，始终返回 `true` |
| `AlwaysOffStrategy` | 始终禁用 | `INSTANCE` 单例，始终返回 `false` |
| `PercentageStrategy` | 百分比灰度 | 构造参数 `percentage` (0-100)；有 userId 时使用哈希保证一致性，无 userId 时随机 |
| `ConsistentPercentageStrategy` | 一致性哈希百分比 | 构造参数 `percentage` + 可选 `salt`；使用 `featureKey:userId:salt` 计算哈希，防止用户通过修改 ID 绕过 |
| `UserListStrategy` | 用户白名单 | 构造参数 `Set<String> allowedUsers`；`isUserAllowed(String)` 查询单个用户 |
| `DateRangeStrategy` | 日期范围 | 构造参数 `Instant startTime, Instant endTime`；`of(LocalDateTime, LocalDateTime)` 工厂方法；`until(Instant)`、`from(Instant)` 单边范围；`hasStarted()`、`hasEnded()` 查询状态 |
| `TenantAwareStrategy` | 租户感知 | 构造参数 `Map<String, Boolean> tenantOverrides` + 可选 `fallbackStrategy`；`hasTenantOverride(String)` 查询租户覆盖 |
| `CompositeStrategy` | 组合策略 | `allOf(EnableStrategy...)` 全部满足、`anyOf(EnableStrategy...)` 任一满足；`getStrategies()`、`isRequireAll()` |
| `ExpressionStrategy` | 表达式策略 | `of(String expression)` / `of(String expression, boolean fallbackValue)`；若 opencode-base-expression 模块可用则委托，否则降级；上下文属性作为变量注入 |

**代码示例：**

```java
// 百分比灰度
Feature feature = Feature.builder("new-feature")
    .strategy(new PercentageStrategy(30))
    .build();

// 一致性哈希百分比（防绕过）
Feature feature = Feature.builder("new-feature")
    .strategy(new ConsistentPercentageStrategy(10, "secret-salt"))
    .build();

// 日期范围
Feature springSale = Feature.builder("spring-sale")
    .strategy(DateRangeStrategy.of(
        LocalDateTime.of(2025, 3, 1, 0, 0),
        LocalDateTime.of(2025, 3, 31, 23, 59)
    ))
    .build();

// 租户感知
Feature premiumFeature = Feature.builder("premium-feature")
    .strategy(new TenantAwareStrategy(
        Map.of("enterprise", true, "trial", false),
        AlwaysOffStrategy.INSTANCE
    ))
    .build();

// 组合策略（AND）
EnableStrategy strategy = CompositeStrategy.allOf(
    new UserListStrategy(Set.of("admin")),
    new DateRangeStrategy(Instant.now(), Instant.now().plusSeconds(3600))
);

// 表达式策略
EnableStrategy exprStrategy = ExpressionStrategy.of("age >= 18 && userType == 'premium'");
FeatureContext ctx = FeatureContext.builder()
    .attribute("age", 25)
    .attribute("userType", "premium")
    .build();
```

---

## 5. 代理与 A/B 测试

### 5.1 FeatureProxy -- 功能门控代理

基于 JDK 动态代理，拦截带有 `@FeatureToggle` 注解的方法，根据功能开关状态决定是否执行。

**主要方法：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `create(Class<T> interfaceType, T target)` | 接口类, 目标实现 | `<T> T` | 创建代理（使用全局 OpenFeature） |
| `create(Class<T> interfaceType, T target, OpenFeature features)` | 接口类, 目标实现, 自定义实例 | `<T> T` | 创建代理（使用自定义实例） |
| `builder(Class<T> interfaceType, T target)` | 接口类, 目标实现 | `Builder<T>` | 创建构建器 |

**Builder 方法：**

| 方法 | 参数 | 说明 |
|------|------|------|
| `features(OpenFeature)` | OpenFeature 实例 | 设置 OpenFeature 实例 |
| `contextSupplier(Supplier<FeatureContext>)` | 上下文供应器 | 设置动态上下文供应器 |
| `whenDisabled(DisabledBehavior)` | 禁用行为 | 设置功能禁用时的行为 |
| `build()` | 无 | 构建代理实例 |

**DisabledBehavior 枚举：**

| 值 | 说明 |
|----|------|
| `RETURN_DEFAULT` | 返回默认值（null / 0 / false） |
| `THROW_EXCEPTION` | 抛出 `FeatureDisabledException` |
| `SKIP` | 静默跳过（与 RETURN_DEFAULT 相同，意图更清晰） |

**代码示例：**

```java
// 定义带注解的接口
public interface PaymentService {
    @FeatureToggle("new-payment")
    void processPayment(Order order);

    @FeatureToggle(value = "refund-v2", defaultEnabled = false)
    void processRefund(Order order);
}

// 创建功能门控代理
PaymentService impl = new PaymentServiceImpl();
PaymentService proxy = FeatureProxy.create(PaymentService.class, impl);
proxy.processPayment(order);  // 仅当 "new-payment" 启用时执行

// 使用构建器配置
PaymentService proxy = FeatureProxy.builder(PaymentService.class, impl)
    .contextSupplier(() -> FeatureContext.ofUser(getCurrentUserId()))
    .whenDisabled(FeatureProxy.DisabledBehavior.THROW_EXCEPTION)
    .build();
```

### 5.2 VariantRouter -- A/B 测试变体路由器

将方法调用路由到不同的 A/B 测试变体，支持基于百分比的流量分配和一致性用户路由。

**主要方法：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `builder(String featureKey)` | 功能键 | `Builder<T>` | 创建构建器 |
| `fromAnnotations(String featureKey, T target)` | 功能键, 目标对象 | `VariantRouter<MethodVariant<T>>` | 从 @FeatureVariant 注解创建 |
| `route(FeatureContext context)` | 上下文 | `T` | 根据上下文路由到变体 |
| `route()` | 无 | `T` | 使用空上下文路由 |
| `routeForUser(String userId)` | 用户 ID | `T` | 路由到指定用户的变体 |
| `execute(FeatureContext context, Function<T, R> action)` | 上下文, 动作 | `<R> R` | 路由并执行 |
| `executeVoid(FeatureContext context, Consumer<T> action)` | 上下文, 动作 | `void` | 路由并执行（无返回值） |
| `getVariant(String variantId)` | 变体 ID | `Optional<T>` | 获取指定变体 |
| `getSelectedVariantId(FeatureContext context)` | 上下文 | `String` | 获取选中的变体 ID |
| `getVariantIds()` | 无 | `Set<String>` | 获取所有变体 ID |
| `getFeatureKey()` | 无 | `String` | 获取功能键 |

**Builder 方法：**

| 方法 | 参数 | 说明 |
|------|------|------|
| `variant(String variantId, T impl)` | 变体 ID, 实现 | 添加变体 |
| `variant(String variantId, T impl, int percentage)` | 变体 ID, 实现, 百分比 | 添加带权重的变体 |
| `variant(String variantId, Supplier<T> supplier, int percentage)` | 变体 ID, 供应器, 百分比 | 添加延迟初始化变体 |
| `defaultVariant(String variantId)` | 变体 ID | 设置默认变体 |
| `salt(String salt)` | 盐值 | 设置哈希盐值 |
| `build()` | 无 | 构建路由器 |

**代码示例：**

```java
// 创建 A/B 测试路由器
CheckoutService variantA = new CheckoutServiceV1();
CheckoutService variantB = new CheckoutServiceV2();

VariantRouter<CheckoutService> router = VariantRouter.<CheckoutService>builder("checkout-flow")
    .variant("A", variantA, 50)  // 50% 流量
    .variant("B", variantB, 50)  // 50% 流量
    .defaultVariant("A")
    .build();

// 根据用户路由
FeatureContext ctx = FeatureContext.ofUser("user123");
CheckoutService service = router.route(ctx);
service.checkout(order);

// 或直接执行
router.execute(ctx, s -> s.checkout(order));
```

---

## 6. 存储层

### 6.1 FeatureStore -- 存储接口

```java
public interface FeatureStore {
    void save(Feature feature);
    Optional<Feature> find(String key);
    List<Feature> findAll();
    boolean delete(String key);
    default boolean exists(String key);  // 默认实现
    default int count();                 // 默认实现
    void clear();
}
```

### 6.2 内置存储实现

| 存储类 | 说明 | 构造参数 | 特点 |
|--------|------|----------|------|
| `InMemoryFeatureStore` | 内存存储 | 无 | 使用 ConcurrentHashMap，线程安全 |
| `FileFeatureStore` | 文件存储 | `Path filePath` | properties 格式文件；`reload()` 方法重新加载 |
| `LruFeatureStore` | LRU 缓存存储 | `int maxSize` | 容量有限，自动淘汰最久未用的功能 |
| `RedisFeatureStore` | Redis 存储 | `Function get, BiConsumer set, Consumer del, Supplier keys, Runnable clearAll, String keyPrefix, Duration ttl` | 分布式场景；`getKeyPrefix()`、`getTtl()` 查询配置 |
| `CachedFeatureStore` | 缓存装饰器 | 通过静态方法 `wrap()` 创建 | 若 opencode-base-cache 模块可用则委托 OpenCache，否则使用内建 Map 缓存 |

**CachedFeatureStore 静态方法：**

| 方法 | 参数 | 说明 |
|------|------|------|
| `wrap(FeatureStore delegate)` | 被装饰的存储 | 使用默认 TTL 包装 |
| `wrap(FeatureStore delegate, Duration ttl)` | 被装饰的存储, TTL | 使用指定 TTL 包装 |
| `wrap(FeatureStore delegate, Duration ttl, int maxSize)` | 被装饰的存储, TTL, 最大缓存数 | 完整配置 |
| `isCacheModuleAvailable()` | 无 | 检查 Cache 模块是否可用 |
| `isUsingOpenCache()` | 无 | 是否正在使用 OpenCache |
| `invalidate(String key)` | 功能键 | 使单个缓存失效 |
| `invalidateAll()` | 无 | 使全部缓存失效 |
| `getDelegate()` | 无 | 获取被装饰的存储 |

**代码示例：**

```java
// 内存存储
OpenFeature fm = OpenFeature.getInstance();
fm.setStore(new InMemoryFeatureStore());

// 文件存储
fm.setStore(new FileFeatureStore(Path.of("features.properties")));

// LRU 存储（最多 100 个功能）
fm.setStore(new LruFeatureStore(100));

// 缓存装饰器
FeatureStore remoteStore = new RedisFeatureStore(...);
FeatureStore cachedStore = CachedFeatureStore.wrap(remoteStore, Duration.ofMinutes(5));
fm.setStore(cachedStore);
```

---

## 7. 安全与审计

### 7.1 SecureFeatureManager -- 安全功能管理器

带权限控制和审计日志的功能管理器。

**主要方法：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `SecureFeatureManager(Set<String> adminUsers, AuditLogger logger)` | 管理员用户集合, 审计日志 | 构造方法 | 使用全局 OpenFeature |
| `SecureFeatureManager(OpenFeature features, Set<String> adminUsers, AuditLogger logger)` | 自定义实例, 管理员, 审计日志 | 构造方法 | 使用自定义 OpenFeature |
| `register(Feature feature, String operatorId)` | 功能, 操作者 ID | `void` | 安全注册功能 |
| `enable(String featureKey, String operatorId)` | 功能键, 操作者 ID | `void` | 安全启用功能 |
| `disable(String featureKey, String operatorId)` | 功能键, 操作者 ID | `void` | 安全禁用功能 |
| `updateStrategy(String featureKey, EnableStrategy strategy, String operatorId)` | 功能键, 新策略, 操作者 ID | `void` | 安全更新策略 |
| `isEnabled(String featureKey)` | 功能键 | `boolean` | 检查是否启用 |
| `isEnabled(String featureKey, FeatureContext ctx)` | 功能键, 上下文 | `boolean` | 带上下文检查 |
| `get(String featureKey)` | 功能键 | `Optional<Feature>` | 获取功能 |
| `isAdmin(String operatorId)` | 操作者 ID | `boolean` | 检查是否为管理员 |

### 7.2 AuditLogger -- 审计日志接口

```java
public interface AuditLogger {
    void log(FeatureAuditEvent event);
}
```

### 7.3 FeatureAuditEvent -- 审计事件

```java
public record FeatureAuditEvent(
    String featureKey,    // 功能键
    String operatorId,    // 操作者 ID
    String action,        // 操作类型：ENABLE, DISABLE, REGISTER, UPDATE_STRATEGY
    boolean oldValue,     // 旧状态
    boolean newValue,     // 新状态
    Instant timestamp     // 时间戳
) {
    public boolean isStateChanged();  // 状态是否变更
    public String toLogString();      // 日志格式字符串
}
```

### 7.4 FileAuditLogger -- 文件审计日志

| 方法 | 参数 | 说明 |
|------|------|------|
| `FileAuditLogger(Path logFile)` | 日志文件路径 | 构造方法 |
| `log(FeatureAuditEvent event)` | 审计事件 | 写入日志文件 |
| `getLogFile()` | 无 | 获取日志文件路径 |

### 7.5 MetricsFeatureListener -- 指标监听器

实现 `FeatureListener` 接口，记录功能变更的统计指标。

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `getTotalChanges()` | 无 | `long` | 获取总变更次数 |
| `getEnableCount(String key)` | 功能键 | `long` | 获取启用次数 |
| `getDisableCount(String key)` | 功能键 | `long` | 获取禁用次数 |
| `getAllEnableCounts()` | 无 | `Map<String, Long>` | 获取所有启用计数 |
| `getAllDisableCounts()` | 无 | `Map<String, Long>` | 获取所有禁用计数 |
| `reset()` | 无 | `void` | 重置所有计数 |

**代码示例：**

```java
// 安全管理器
SecureFeatureManager manager = new SecureFeatureManager(
    Set.of("admin-user"),
    new FileAuditLogger(Path.of("audit.log"))
);

// 只有管理员能操作
manager.enable("premium-feature", "admin-user");   // 成功
manager.enable("premium-feature", "regular-user");  // 抛出 FeatureSecurityException

// 指标监听
MetricsFeatureListener metrics = new MetricsFeatureListener();
OpenFeature.getInstance().addListener(metrics);
// ... 后续查询
long total = metrics.getTotalChanges();
long enableCount = metrics.getEnableCount("dark-mode");
```

---

## 8. 注解

### 8.1 @FeatureToggle

标注在方法或类上，配合 `FeatureProxy` 实现方法级功能门控。

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface FeatureToggle {
    String value();                  // 功能键
    boolean defaultEnabled() default false;  // 默认启用状态
}
```

### 8.2 @FeatureVariant

标注在方法上，配合 `VariantRouter.fromAnnotations()` 实现 A/B 测试。

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FeatureVariant {
    String value();           // 变体 ID
    int percentage() default 0;  // 百分比
}
```

---

## 9. 监听器

### 9.1 FeatureListener -- 功能变更监听器

函数式接口，监听功能开关状态变更。

```java
@FunctionalInterface
public interface FeatureListener {
    void onFeatureChanged(String key, boolean oldValue, boolean newValue);
}
```

**代码示例：**

```java
OpenFeature.getInstance().addListener((key, oldValue, newValue) -> {
    log.info("Feature {} changed: {} -> {}", key, oldValue, newValue);
    // 可在此触发缓存刷新、通知推送等
});
```

---

## 10. 异常体系

### 10.1 异常层次结构

```
RuntimeException
└── FeatureException                      # 功能开关异常基类
    ├── FeatureNotFoundException          # 功能不存在
    ├── FeatureConfigException            # 配置异常
    ├── FeatureStoreException             # 存储异常
    └── FeatureSecurityException          # 安全异常
```

### 10.2 FeatureException -- 异常基类

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getFeatureKey()` | `String` | 获取相关的功能键 |
| `getErrorCode()` | `FeatureErrorCode` | 获取错误码 |

**构造方法：**
- `FeatureException(String message)`
- `FeatureException(String message, Throwable cause)`
- `FeatureException(String message, FeatureErrorCode errorCode)`
- `FeatureException(String message, Throwable cause, String featureKey, FeatureErrorCode errorCode)`

### 10.3 FeatureErrorCode -- 错误码枚举

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getCode()` | `int` | 获取错误码 |
| `getMessage()` | `String` | 获取英文描述 |
| `getMessageZh()` | `String` | 获取中文描述 |
| `getDescription()` | `String` | 获取详细描述 |

---

## 11. 线程安全

| 类 | 线程安全 | 说明 |
|----|----------|------|
| `OpenFeature` | 安全 | 内部使用 ConcurrentHashMap + CopyOnWriteArrayList |
| `Feature` | 安全 | 不可变 Record |
| `FeatureContext` | 安全 | 不可变 Record |
| `Feature.Builder` | 不安全 | 构建器模式，非线程共享 |
| `AlwaysOnStrategy` / `AlwaysOffStrategy` | 安全 | 无状态单例 |
| `PercentageStrategy` | 安全 | 无共享可变状态 |
| `ConsistentPercentageStrategy` | 安全 | 无共享可变状态 |
| `UserListStrategy` | 安全 | 使用不可变 Set |
| `InMemoryFeatureStore` | 安全 | 使用 ConcurrentHashMap |
| `LruFeatureStore` | 安全 | 使用 synchronizedMap |
| `MetricsFeatureListener` | 安全 | 使用 AtomicLong / ConcurrentHashMap |

---

## 12. 使用示例

### 12.1 完整集成示例

```java
// 1. 初始化
OpenFeature fm = OpenFeature.getInstance();
fm.setStore(new InMemoryFeatureStore());

// 2. 注册功能
fm.registerAll(
    Feature.builder("new-ui").name("新版 UI").alwaysOn().build(),
    Feature.builder("dark-mode").name("深色模式").percentage(10).build(),
    Feature.builder("beta-feature").forUsers("user1", "user2").build(),
    Feature.builder("spring-sale")
        .name("春季促销")
        .strategy(DateRangeStrategy.of(
            LocalDateTime.of(2025, 3, 1, 0, 0),
            LocalDateTime.of(2025, 3, 31, 23, 59)
        ))
        .build(),
    Feature.builder("premium-feature")
        .strategy(new TenantAwareStrategy(
            Map.of("enterprise", true, "trial", false),
            AlwaysOffStrategy.INSTANCE
        ))
        .build()
);

// 3. 检查功能
boolean newUI = fm.isEnabled("new-ui");
boolean darkMode = fm.isEnabledForUser("dark-mode", "user-123");

// 4. 条件执行
fm.ifEnabled("new-ui", () -> renderNewUI());
String result = fm.ifEnabled("dark-mode",
    () -> "dark-theme.css",
    () -> "light-theme.css"
);

// 5. A/B 测试
VariantRouter<Renderer> router = VariantRouter.<Renderer>builder("checkout-flow")
    .variant("A", new RendererV1(), 70)
    .variant("B", new RendererV2(), 30)
    .build();
Renderer renderer = router.routeForUser("user-123");

// 6. 动态代理
PaymentService proxy = FeatureProxy.builder(PaymentService.class, new PaymentServiceImpl())
    .contextSupplier(() -> FeatureContext.ofUser(getCurrentUserId()))
    .build();

// 7. 安全管理
SecureFeatureManager secureManager = new SecureFeatureManager(
    Set.of("admin"), new FileAuditLogger(Path.of("audit.log"))
);
secureManager.enable("premium-feature", "admin");
```

### 12.2 从配置文件加载

```java
// 使用 FileFeatureStore
OpenFeature.getInstance().setStore(
    new FileFeatureStore(Path.of("features.properties"))
);

// 使用缓存装饰器包装远程存储
FeatureStore cached = CachedFeatureStore.wrap(remoteStore, Duration.ofMinutes(5));
OpenFeature.getInstance().setStore(cached);
```

### 12.3 分组与元数据

```java
// 通过命名约定分组
fm.register(Feature.builder("payment.new-gateway").build());
fm.register(Feature.builder("payment.retry-logic").build());

// 按分组禁用
fm.getAll().entrySet().stream()
    .filter(e -> e.getKey().startsWith("payment."))
    .forEach(e -> fm.disable(e.getKey()));

// 使用元数据
Feature feature = Feature.builder("feature-a")
    .metadata("group", "experiment-1")
    .metadata("owner", "team-a")
    .build();
String owner = feature.getMetadata("owner"); // "team-a"
```

---

## 13. 版本信息

| 属性 | 值 |
|------|-----|
| 模块名 | opencode-base-feature |
| 最低 JDK | 25 |
| 核心依赖 | 无（可选集成 opencode-base-cache、opencode-base-expression） |
