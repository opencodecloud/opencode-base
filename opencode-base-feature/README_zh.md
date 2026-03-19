# OpenCode Base Feature

**功能开关管理库，适用于 Java 25+**

`opencode-base-feature` 提供全面的功能开关支持，用于灰度发布、A/B 测试和动态配置。提供可插拔的存储后端、多种发布策略和注解驱动的 API。

## 功能特性

### 核心功能
- **功能注册**：注册、启用、禁用和删除功能开关
- **策略化评估**：可插拔的功能激活决策策略
- **上下文感知**：基于用户、租户和属性的评估
- **监听器支持**：功能状态变更的事件驱动通知
- **可插拔存储**：内存、文件、缓存、LRU 和 Redis 存储

### 发布策略
- **AlwaysOn / AlwaysOff**：静态开/关策略
- **PercentageStrategy**：随机百分比发布
- **ConsistentPercentageStrategy**：确定性哈希百分比发布
- **UserListStrategy**：特定用户白名单
- **TenantAwareStrategy**：租户级功能管理
- **DateRangeStrategy**：基于时间窗口的激活
- **ExpressionStrategy**：基于表达式语言的动态规则
- **CompositeStrategy**：组合多个策略（AND/OR 逻辑）

### 高级功能
- **注解支持**：`@FeatureToggle` 和 `@FeatureVariant` 注解
- **代理路由**：通过 `FeatureProxy` 动态变体路由
- **审计日志**：`FileAuditLogger` 完整审计追踪
- **安全管理**：`SecureFeatureManager` 权限控制
- **指标统计**：`MetricsFeatureListener` 功能使用跟踪

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-feature</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.feature.*;
import cloud.opencode.base.feature.strategy.*;

// 获取单例实例
OpenFeature features = OpenFeature.getInstance();

// 注册功能
Feature darkMode = Feature.builder("dark-mode")
    .name("Dark Mode")
    .description("启用暗色主题")
    .defaultEnabled(false)
    .strategy(new PercentageStrategy(30))
    .build();
features.register(darkMode);

// 检查是否启用
if (features.isEnabled("dark-mode")) {
    // 使用暗色模式
}

// 使用用户上下文检查
FeatureContext ctx = FeatureContext.builder()
    .userId("user123")
    .build();
if (features.isEnabled("dark-mode", ctx)) {
    // 为该用户使用暗色模式
}
```

### 策略示例

```java
// 始终启用
Feature feature = Feature.builder("feature-a").alwaysOn().build();

// 百分比发布
Feature feature = Feature.builder("feature-b").percentage(10).build();

// 用户白名单
Feature feature = Feature.builder("beta")
    .forUsers("user1", "user2", "user3")
    .build();

// 时间范围
Feature feature = Feature.builder("promo")
    .strategy(new DateRangeStrategy(startDate, endDate))
    .build();

// 组合策略
Feature feature = Feature.builder("complex")
    .strategy(CompositeStrategy.allOf(
        new PercentageStrategy(50),
        new UserListStrategy(Set.of("admin"))
    ))
    .build();
```

### 条件执行

```java
// 启用时执行
features.ifEnabled("dark-mode", () -> applyDarkTheme());

// 根据功能状态获取值
String theme = features.ifEnabled("dark-mode",
    () -> "dark",
    () -> "light"
);
```

### 功能存储

```java
// 内存存储（默认）
OpenFeature features = OpenFeature.getInstance();

// 文件持久化
OpenFeature features = OpenFeature.create(new FileFeatureStore(Path.of("features.json")));

// 缓存存储
OpenFeature features = OpenFeature.create(new CachedFeatureStore(backingStore));

// LRU 存储
OpenFeature features = OpenFeature.create(new LruFeatureStore(maxSize));
```

## 类参考

### 根包 (`cloud.opencode.base.feature`)
| 类 | 说明 |
|---|------|
| `OpenFeature` | 功能开关管理的主门面类（单例） |
| `Feature` | 表示功能开关定义的不可变记录 |
| `FeatureContext` | 携带用户、租户和自定义属性的评估上下文 |

### 注解包 (`cloud.opencode.base.feature.annotation`)
| 类 | 说明 |
|---|------|
| `FeatureToggle` | 标记由功能开关保护的方法的注解 |
| `FeatureVariant` | 用于变体特定方法实现的注解 |

### 审计包 (`cloud.opencode.base.feature.audit`)
| 类 | 说明 |
|---|------|
| `FeatureAuditEvent` | 捕获功能状态变更审计数据的记录 |
| `FileAuditLogger` | 将事件写入文件的审计日志器 |
| `MetricsFeatureListener` | 收集功能使用指标的监听器 |

### 异常包 (`cloud.opencode.base.feature.exception`)
| 类 | 说明 |
|---|------|
| `FeatureException` | 功能操作的基础异常 |
| `FeatureConfigException` | 功能配置错误异常 |
| `FeatureNotFoundException` | 功能键未找到异常 |
| `FeatureSecurityException` | 功能安全违规异常 |
| `FeatureStoreException` | 功能存储 I/O 错误异常 |
| `FeatureErrorCode` | 功能错误码枚举 |

### 监听器包 (`cloud.opencode.base.feature.listener`)
| 类 | 说明 |
|---|------|
| `FeatureListener` | 接收功能状态变更通知的接口 |

### 代理包 (`cloud.opencode.base.feature.proxy`)
| 类 | 说明 |
|---|------|
| `FeatureProxy` | 基于功能状态路由调用的动态代理 |
| `VariantRouter` | 基于功能变体路由到不同实现 |

### 安全包 (`cloud.opencode.base.feature.security`)
| 类 | 说明 |
|---|------|
| `AuditLogger` | 功能操作审计日志接口 |
| `SecureFeatureManager` | 带权限访问控制的功能管理器 |

### 存储包 (`cloud.opencode.base.feature.store`)
| 类 | 说明 |
|---|------|
| `FeatureStore` | 功能持久化后端接口 |
| `InMemoryFeatureStore` | 线程安全的内存存储（默认） |
| `FileFeatureStore` | 基于文件的 JSON 持久化存储 |
| `CachedFeatureStore` | 基于任意后端的缓存装饰器 |
| `LruFeatureStore` | LRU 淘汰策略的有界内存存储 |
| `RedisFeatureStore` | 基于 Redis 的分布式存储 |

### 策略包 (`cloud.opencode.base.feature.strategy`)
| 类 | 说明 |
|---|------|
| `EnableStrategy` | 功能激活策略接口 |
| `AlwaysOnStrategy` | 始终返回启用 |
| `AlwaysOffStrategy` | 始终返回禁用 |
| `PercentageStrategy` | 随机百分比发布 |
| `ConsistentPercentageStrategy` | 确定性哈希百分比发布 |
| `UserListStrategy` | 特定用户 ID 白名单 |
| `TenantAwareStrategy` | 租户级功能管理 |
| `DateRangeStrategy` | 基于时间窗口的激活 |
| `ExpressionStrategy` | 使用表达式引擎的动态规则评估 |
| `CompositeStrategy` | 组合多个策略（AND/OR 逻辑） |

## 环境要求

- Java 25+（使用 record、密封接口、虚拟线程）
- 核心功能无外部依赖

## 可选依赖

- `opencode-base-cache` - 用于缓存功能存储
- `opencode-base-expression` - 用于基于表达式的策略

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
