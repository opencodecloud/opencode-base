# OpenCode Base Rules

**轻量级业务规则引擎，支持 DSL、决策表和冲突解决，适用于 Java 25+**

`opencode-base-rules` 是一个轻量级但功能强大的业务规则引擎，提供流式 DSL 定义规则、决策表引擎、多种触发模式、冲突解决策略和执行监听器。

## 功能特性

### 核心功能
- **流式规则 DSL**：使用可读的构建器模式定义规则（when/then）
- **规则引擎**：注册、管理和执行业务规则
- **多种触发模式**：触发全部、触发首个、持续触发直到停止（推理）
- **规则分组**：将规则组织为逻辑组
- **规则上下文**：用于在规则之间传递数据的类型化事实存储

### 决策表
- **决策表构建器**：流式 API 构建决策表
- **命中策略**：首个、唯一、任意、优先级、收集和规则顺序策略
- **输入/输出列**：类型化的输入条件和输出动作

### 冲突解决
- **优先级解决器**：按优先级执行规则（数字越小 = 优先级越高）
- **顺序解决器**：按注册顺序执行规则
- **自定义解决器**：实现 ConflictResolver 接口

### 扩展性
- **执行监听器**：观察规则评估和执行生命周期
- **日志监听器**：内置的调试日志监听器
- **SPI 支持**：通过 ActionProvider 和 RuleProvider 加载规则
- **条件工厂**：基于谓词的、始终为真的和始终为假的条件
- **动作工厂**：基于 Consumer 的动作和无操作动作

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-rules</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 定义和执行规则
```java
import cloud.opencode.base.rules.*;

// 创建规则
Rule discountRule = OpenRules.rule("discount-rule")
    .description("为 VIP 客户应用折扣")
    .priority(100)
    .when(ctx -> "VIP".equals(ctx.get("customerType")))
    .then(ctx -> ctx.put("discount", 0.15))
    .build();

// 创建和配置引擎
RuleEngine engine = OpenRules.engine()
    .register(discountRule)
    .setConflictResolver(OpenRules.priorityResolver())
    .addListener(OpenRules.loggingListener())
    .build();

// 执行规则
RuleContext context = OpenRules.contextOf("customerType", "VIP", "amount", 1000.0);
RuleResult result = engine.fire(context);

System.out.println("折扣: " + context.get("discount")); // 0.15
```

### 决策表
```java
DecisionTable table = OpenRules.decisionTable("pricing")
    .hitPolicy(HitPolicy.FIRST)
    .input("customerType", String.class)
    .input("amount", Double.class)
    .output("discount", Double.class)
    .row(new Object[]{"VIP", ">= 1000"}, new Object[]{0.15})
    .row(new Object[]{"VIP", "-"}, new Object[]{0.10})
    .row(new Object[]{"-", "-"}, new Object[]{0.0})
    .build();
```

### 规则分组
```java
RuleGroup group = OpenRules.group("discount-rules")
    .add(vipRule)
    .add(seniorRule)
    .add(bulkRule)
    .build();

RuleEngine engine = OpenRules.engineWith(group);
RuleResult result = engine.fire(context, "discount-rules");
```

### 触发模式
```java
// 触发所有匹配的规则
RuleResult result = engine.fire(context);

// 只触发第一个匹配的规则
RuleResult result = engine.fireFirst(context);

// 重复触发规则直到没有更多规则可以触发（推理）
RuleResult result = engine.fireUntilHalt(context);
```

## 类参考

### 根包 (`cloud.opencode.base.rules`)
| 类 | 说明 |
|----|------|
| `OpenRules` | 主门面类，提供规则、引擎和决策表的工厂方法 |
| `Rule` | 表示业务规则的接口（条件 + 动作） |
| `RuleEngine` | 注册、管理和执行规则的接口 |
| `RuleContext` | 在规则之间传递数据的类型化事实存储 |
| `RuleResult` | 包含已触发规则和统计信息的执行结果 |

### 引擎 (`cloud.opencode.base.rules.engine`)
| 类 | 说明 |
|----|------|
| `DefaultRuleEngine` | RuleEngine 的默认实现 |
| `DefaultRule` | Rule 的默认实现 |

### 模型 (`cloud.opencode.base.rules.model`)
| 类 | 说明 |
|----|------|
| `Condition` | 规则条件的函数式接口 |
| `Action` | 规则动作的函数式接口 |
| `RuleGroup` | 相关规则的命名组 |
| `FactStore` | 存储和检索事实的接口 |
| `DefaultFactStore` | FactStore 的默认实现 |

### 条件 (`cloud.opencode.base.rules.condition`)
| 类 | 说明 |
|----|------|
| `PredicateCondition` | 由 Predicate 支持的条件 |
| `CompositeCondition` | 多个条件的 AND/OR 组合 |

### 动作 (`cloud.opencode.base.rules.action`)
| 类 | 说明 |
|----|------|
| `ConsumerAction` | 由 Consumer 支持的动作 |
| `CompositeAction` | 多个动作的顺序组合 |

### 决策 (`cloud.opencode.base.rules.decision`)
| 类 | 说明 |
|----|------|
| `DecisionTable` | 决策表评估接口 |
| `SimpleDecisionTable` | 默认决策表实现 |
| `DecisionResult` | 决策表评估结果 |
| `HitPolicy` | 命中策略枚举（FIRST、UNIQUE、ANY、PRIORITY、COLLECT、RULE_ORDER） |

### DSL (`cloud.opencode.base.rules.dsl`)
| 类 | 说明 |
|----|------|
| `RuleBuilder` | 创建规则的流式构建器 |
| `RuleEngineBuilder` | 配置规则引擎的流式构建器 |
| `RuleGroupBuilder` | 创建规则组的流式构建器 |
| `DecisionTableBuilder` | 创建决策表的流式构建器 |

### 冲突 (`cloud.opencode.base.rules.conflict`)
| 类 | 说明 |
|----|------|
| `ConflictResolver` | 解决规则排序冲突的接口 |
| `PriorityConflictResolver` | 按规则优先级解决 |
| `OrderConflictResolver` | 按注册顺序解决 |

### 监听器 (`cloud.opencode.base.rules.listener`)
| 类 | 说明 |
|----|------|
| `RuleListener` | 观察规则执行生命周期的接口 |
| `LoggingRuleListener` | 记录规则评估和执行日志的内置监听器 |

### SPI (`cloud.opencode.base.rules.spi`)
| 类 | 说明 |
|----|------|
| `RuleProvider` | 从外部源加载规则的 SPI |
| `ActionProvider` | 从外部源加载动作的 SPI |

### 异常 (`cloud.opencode.base.rules.exception`)
| 类 | 说明 |
|----|------|
| `OpenRulesException` | 规则引擎错误的运行时异常 |

## 环境要求

- Java 25+
- 无外部依赖（可选：`opencode-base-expression` 用于基于表达式的条件）

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
