# OpenCode Base Rules

**轻量级业务规则引擎，支持 DSL、决策表、执行追踪、评分引擎和冲突解决，适用于 Java 25+**

`opencode-base-rules` 是一个轻量级但功能强大的业务规则引擎，提供流式 DSL 定义规则、决策表引擎、多种触发模式、冲突解决策略、执行追踪、规则校验、指标采集、评分引擎等功能。

## 功能特性

### 核心功能
- **流式规则 DSL**：使用可读的构建器模式定义规则（when/then）
- **规则引擎**：注册、管理和执行业务规则
- **多种触发模式**：触发全部、触发首个、持续触发直到停止（推理）
- **规则分组**：将规则组织为逻辑组
- **规则上下文**：用于在规则之间传递数据的类型化事实存储
- **终止规则**：标记规则为 terminal，触发时停止引擎执行
- **停止条件**：定义自定义谓词动态停止规则执行

### 类型安全 (V1.0.3)
- **TypedKey\<T\>**：编译时类型安全的事实和变量访问键
- **消除 ClassCastException**：访问事实时无需手动转换

### 执行追踪 (V1.0.3)
- **ExecutionTrace**：完整的结构化执行审计轨迹
- **RuleTrace**：每条规则的追踪，包含条件结果、耗时和错误信息
- **TracingRuleListener**：即插即用的追踪监听器
- **fireAndTrace()**：一步调用触发规则并获取执行轨迹

### 规则解释 (V1.0.3)
- **RuleExplainer**：从执行轨迹生成人类可读的解释
- **Explanation**：结构化的摘要和详细说明

### 规则校验 (V1.0.3)
- **RuleValidator**：运行前的规则集静态分析
- **检测**：重复名称、空名称、空条件、负优先级

### 规则指标 (V1.0.3)
- **RuleMetrics**：线程安全的按规则指标采集（评估次数、触发次数、耗时）
- **MetricsListener**：自动采集指标的即插即用监听器
- **MetricsSnapshot**：按规则的时间点指标快照

### 评分引擎 (V1.0.3)
- **ScoringRule**：产出数值分数的规则
- **ScoringEngine**：评估并聚合分数，支持可配置策略
- **聚合策略**：求和(SUM)、加权求和(WEIGHTED_SUM)、最大值(MAX)、平均值(AVERAGE)

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

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-rules</artifactId>
    <version>1.0.3</version>
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

### 类型安全事实访问
```java
import cloud.opencode.base.rules.key.TypedKey;

// 定义类型化键
TypedKey<String> CUSTOMER_TYPE = OpenRules.key("customerType", String.class);
TypedKey<Double> AMOUNT = OpenRules.key("amount", Double.class);
TypedKey<Double> DISCOUNT = OpenRules.key("discount", Double.class);

// 类型安全访问 — 无需强制转换
RuleContext context = RuleContext.create();
context.put(CUSTOMER_TYPE, "VIP");
context.put(AMOUNT, 1500.0);

String type = context.get(CUSTOMER_TYPE);   // String — 编译器检查
Double amount = context.get(AMOUNT);         // Double — 编译器检查
```

### 执行追踪
```java
import cloud.opencode.base.rules.trace.*;

// 触发规则并获取完整执行轨迹
ExecutionTrace trace = engine.fireAndTrace(context);

System.out.println("触发: " + trace.firedCount());
System.out.println("耗时: " + trace.totalDuration().toMillis() + "ms");

for (RuleTrace rt : trace.ruleTraces()) {
    System.out.println(rt.ruleName() + " → " +
        (rt.hasFired() ? "已触发" : "已跳过") +
        " (" + rt.duration().toMillis() + "ms)");
}
```

### 规则解释
```java
import cloud.opencode.base.rules.explain.*;

ExecutionTrace trace = engine.fireAndTrace(context);
Explanation explanation = RuleExplainer.explain(trace);

System.out.println(explanation.summary());
// "Evaluated 3 rules: 1 fired, 2 skipped, 0 failed in 5ms"

for (RuleExplanation detail : explanation.details()) {
    System.out.println(detail.reason());
}
```

### 终止规则和停止条件
```java
// 终止规则 — 触发时停止引擎
Rule stopRule = OpenRules.rule("stop-rule")
    .when(ctx -> ctx.get("amount", 0.0) > 10000)
    .then(ctx -> ctx.setResult("blocked", true))
    .terminal()
    .build();

// 引擎停止条件
RuleEngine engine = OpenRules.engine()
    .register(rule1, rule2, rule3)
    .haltWhen(ctx -> ctx.contains("done"))
    .build();
```

### 规则校验
```java
import cloud.opencode.base.rules.validation.*;

ValidationReport report = OpenRules.validate(engine.getRules());
if (!report.isValid()) {
    report.errors().forEach(issue ->
        System.err.println(issue.type() + ": " + issue.message()));
}
```

### 规则指标
```java
import cloud.opencode.base.rules.metric.*;

RuleMetrics metrics = OpenRules.metrics();
MetricsListener listener = OpenRules.metricsListener(metrics);

engine.addListener(listener);

// 执行若干次后...
MetricsSnapshot snapshot = metrics.getSnapshot("discount-rule");
System.out.println("评估次数: " + snapshot.evaluationCount());
System.out.println("触发率: " + snapshot.fireRate());
System.out.println("平均耗时: " + snapshot.avgDurationNanos() + "ns");
```

### 评分引擎
```java
import cloud.opencode.base.rules.score.*;

// 创建评分规则
ScoringRule creditScore = new ScoringRule() {
    public String getName() { return "credit-check"; }
    public String getDescription() { return "信用评分检查"; }
    public int getPriority() { return 1; }
    public boolean evaluate(RuleContext ctx) { return true; }
    public void execute(RuleContext ctx) {}
    public double score(RuleContext ctx) { return 0.8; }
    public double weight() { return 0.5; }
};

ScoreResult result = ScoringEngine.score(context,
    List.of(creditScore), AggregationStrategy.WEIGHTED_SUM);

System.out.println("总分: " + result.totalScore());
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
| `Rule` | 表示业务规则的接口（条件 + 动作 + 终止） |
| `RuleEngine` | 注册、管理和执行规则的接口 |
| `RuleContext` | 支持 TypedKey 的类型化事实存储 |
| `RuleResult` | 包含已触发规则和统计信息的执行结果 |

### 键 (`cloud.opencode.base.rules.key`)
| 类 | 说明 |
|----|------|
| `TypedKey<T>` | 编译时类型检查的事实访问键记录 |

### 追踪 (`cloud.opencode.base.rules.trace`)
| 类 | 说明 |
|----|------|
| `ExecutionTrace` | 完整的执行追踪记录，包含每条规则的详细信息 |
| `RuleTrace` | 单条规则评估追踪记录 |
| `TracingRuleListener` | 收集执行追踪数据的监听器 |

### 解释 (`cloud.opencode.base.rules.explain`)
| 类 | 说明 |
|----|------|
| `RuleExplainer` | 从执行轨迹生成人类可读解释 |
| `Explanation` | 结构化解释记录，包含摘要和详情 |
| `RuleExplanation` | 每条规则的解释记录 |

### 校验 (`cloud.opencode.base.rules.validation`)
| 类 | 说明 |
|----|------|
| `RuleValidator` | 静态规则集验证器 |
| `ValidationReport` | 包含问题列表的验证结果 |
| `ValidationIssue` | 单个验证问题，包含严重级别和类型 |

### 指标 (`cloud.opencode.base.rules.metric`)
| 类 | 说明 |
|----|------|
| `RuleMetrics` | 线程安全的按规则指标收集器 |
| `MetricsSnapshot` | 时间点指标快照记录 |
| `MetricsListener` | 从规则执行中采集指标的监听器 |

### 评分 (`cloud.opencode.base.rules.score`)
| 类 | 说明 |
|----|------|
| `ScoringRule` | 产出数值分数的规则接口 |
| `ScoringEngine` | 评估评分规则并聚合结果 |
| `ScoreResult` | 包含每条规则分数和总分的评分结果 |
| `AggregationStrategy` | 聚合策略枚举：SUM、WEIGHTED_SUM、MAX、AVERAGE |

### 引擎 (`cloud.opencode.base.rules.engine`)
| 类 | 说明 |
|----|------|
| `DefaultRuleEngine` | 支持终止/停止的线程安全 RuleEngine 默认实现 |
| `DefaultRule` | 带终止标志的不可变 Rule 默认实现 |

### 模型 (`cloud.opencode.base.rules.model`)
| 类 | 说明 |
|----|------|
| `Condition` | 规则条件的函数式接口 |
| `Action` | 规则动作的函数式接口 |
| `RuleGroup` | 相关规则的命名组 |
| `FactStore` | 支持 TypedKey 的事实存储和检索接口 |
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
| `RuleBuilder` | 支持终止标志的规则流式构建器 |
| `RuleEngineBuilder` | 支持 haltWhen 的规则引擎流式构建器 |
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
