# Rules 组件方案

## 1. 组件概述

`opencode-base-rules` 模块提供轻量级、高性能的业务规则引擎，零外部依赖，适合微服务和轻量级项目。

**核心特性：**
- 业务规则 DSL（流式 API 定义规则）
- 决策表支持（代码构建，支持多种命中策略）
- 规则优先级与冲突解决
- 规则分组与编排
- 事实管理（FactStore）
- 规则执行监听与审计
- SPI 扩展（RuleProvider / ActionProvider）

**模块依赖：**
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-rules</artifactId>
    <version>${version}</version>
</dependency>
```

---

## 2. 包结构

```
cloud.opencode.base.rules
├── OpenRules.java                   # 门面入口类
├── Rule.java                        # 规则接口
├── RuleEngine.java                  # 规则引擎接口
├── RuleContext.java                 # 规则上下文
├── RuleResult.java                  # 执行结果（record）
│
├── model/                           # 模型定义
│   ├── Action.java                 # 动作接口
│   ├── Condition.java              # 条件接口
│   ├── FactStore.java              # 事实存储接口
│   ├── DefaultFactStore.java       # 事实存储默认实现
│   └── RuleGroup.java              # 规则分组
│
├── dsl/                             # 规则 DSL 构建器
│   ├── RuleBuilder.java            # 规则构建器
│   ├── RuleGroupBuilder.java       # 规则组构建器
│   ├── RuleEngineBuilder.java      # 引擎构建器
│   └── DecisionTableBuilder.java   # 决策表构建器
│
├── engine/                          # 引擎实现
│   ├── DefaultRule.java            # 默认规则实现
│   └── DefaultRuleEngine.java      # 默认规则引擎
│
├── condition/                       # 条件实现
│   ├── PredicateCondition.java     # 谓词条件
│   └── CompositeCondition.java     # 组合条件（AND/OR/NOT）
│
├── action/                          # 动作实现
│   ├── ConsumerAction.java         # Consumer 动作
│   └── CompositeAction.java        # 组合动作
│
├── conflict/                        # 冲突解决
│   ├── ConflictResolver.java       # 冲突解决器接口
│   ├── PriorityConflictResolver.java  # 优先级策略
│   └── OrderConflictResolver.java     # 顺序策略
│
├── decision/                        # 决策表
│   ├── DecisionTable.java          # 决策表接口
│   ├── SimpleDecisionTable.java    # 简单决策表实现
│   ├── DecisionResult.java         # 决策结果（record）
│   └── HitPolicy.java             # 命中策略枚举
│
├── listener/                        # 监听器
│   ├── RuleListener.java           # 规则监听器接口
│   └── LoggingRuleListener.java    # 日志监听器
│
├── exception/                       # 异常
│   └── OpenRulesException.java     # 规则引擎异常
│
└── spi/                             # SPI 扩展
    ├── RuleProvider.java           # 规则提供者
    └── ActionProvider.java         # 动作提供者
```

---

## 3. 核心 API

### 3.1 OpenRules

> 规则引擎门面入口类，提供规则定义、引擎创建、决策表、条件/动作构建等统一 API。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static RuleBuilder rule(String name)` | 创建命名规则构建器 |
| `static RuleBuilder rule()` | 创建匿名规则构建器 |
| `static RuleGroupBuilder group(String name)` | 创建规则组构建器 |
| `static RuleEngineBuilder engine()` | 创建规则引擎构建器 |
| `static RuleEngine defaultEngine()` | 创建默认规则引擎 |
| `static RuleEngine engineWith(Rule... rules)` | 用指定规则创建引擎 |
| `static RuleEngine engineWith(RuleGroup group)` | 用规则组创建引擎 |
| `static DecisionTableBuilder decisionTable()` | 创建决策表构建器 |
| `static DecisionTableBuilder decisionTable(String name)` | 创建命名决策表构建器 |
| `static RuleContext context()` | 创建空规则上下文 |
| `static RuleContext contextOf(Object... keyValues)` | 从键值对创建上下文 |
| `static Condition condition(Predicate<RuleContext> predicate)` | 创建谓词条件 |
| `static Condition alwaysTrue()` | 创建永真条件 |
| `static Condition alwaysFalse()` | 创建永假条件 |
| `static Action action(Consumer<RuleContext> consumer)` | 创建 Consumer 动作 |
| `static Action noOp()` | 创建空动作 |
| `static ConflictResolver priorityResolver()` | 优先级冲突解决器 |
| `static ConflictResolver orderResolver()` | 顺序冲突解决器 |
| `static RuleListener loggingListener()` | 创建日志监听器 |
| `static String version()` | 获取版本号 |
| `static String info()` | 获取组件信息 |

**示例：**
```java
// 定义规则
Rule discountRule = OpenRules.rule("VIP折扣")
    .priority(1)
    .when(ctx -> "VIP".equals(ctx.get("customerType")))
    .then(ctx -> ctx.setResult("discount", 0.1))
    .build();

// 快速创建引擎并执行
RuleEngine engine = OpenRules.engineWith(discountRule);
RuleContext context = OpenRules.contextOf("customerType", "VIP", "amount", 2000.0);
RuleResult result = engine.fire(context);
```

---

### 3.2 Rule

> 规则接口，业务规则的核心抽象，支持条件评估、动作执行、优先级排序和分组。实现 `Comparable<Rule>`。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String getName()` | 获取规则唯一名称 |
| `String getDescription()` | 获取规则描述 |
| `int getPriority()` | 获取优先级（数值越小越高） |
| `String getGroup()` | 获取所属分组 |
| `boolean isEnabled()` | 规则是否启用 |
| `boolean evaluate(RuleContext context)` | 评估规则条件 |
| `void execute(RuleContext context)` | 执行规则动作 |
| `Condition getCondition()` | 获取条件对象 |
| `Action getAction()` | 获取动作对象 |

**常量：**
- `int DEFAULT_PRIORITY = 1000` — 默认优先级

**示例：**
```java
Rule rule = OpenRules.rule("discount-rule")
    .when(ctx -> ctx.<Double>get("amount") > 1000)
    .then(ctx -> ctx.setResult("discount", 0.1))
    .build();

if (rule.evaluate(context)) {
    rule.execute(context);
}
```

---

### 3.3 RuleContext

> 规则上下文，管理规则执行期间的事实（Facts）和变量，支持结果存储。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static RuleContext create()` | 创建空上下文 |
| `static RuleContext of(Map<String, Object> values)` | 从 Map 创建上下文 |
| `static RuleContext of(Object... keyValues)` | 从键值对创建上下文 |
| `static RuleContext withFacts(Object... facts)` | 从事实对象创建上下文 |
| `RuleContext addFact(Object fact)` | 添加事实对象 |
| `RuleContext addFact(String name, Object fact)` | 添加命名事实 |
| `RuleContext put(String key, Object value)` | 设置变量值 |
| `<T> T get(String key)` | 获取变量值 |
| `<T> T get(String key, T defaultValue)` | 获取变量值（带默认值） |
| `<T> Optional<T> getFact(Class<T> type)` | 按类型获取事实 |
| `<T> List<T> getFacts(Class<T> type)` | 获取指定类型所有事实 |
| `boolean contains(String key)` | 检查变量是否存在 |
| `RuleContext setResult(String key, Object value)` | 设置执行结果 |
| `<T> T getResult(String key)` | 获取执行结果 |
| `<T> T getResult(String key, T defaultValue)` | 获取结果（带默认值） |
| `Map<String, Object> getResults()` | 获取所有结果 |
| `Map<String, Object> getVariables()` | 获取所有变量 |
| `FactStore facts()` | 获取事实存储 |
| `RuleContext clearResults()` | 清空结果 |
| `RuleContext clearVariables()` | 清空变量 |

**示例：**
```java
RuleContext context = RuleContext.create()
    .put("customerType", "VIP")
    .put("orderAmount", 2000.0)
    .addFact(new Order("ORD-001", 2000.0));

String type = context.get("customerType");
Optional<Order> order = context.getFact(Order.class);
```

---

### 3.4 RuleEngine

> 规则引擎接口，负责规则的注册、管理和执行，支持多种触发模式。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `RuleEngine register(Rule... rules)` | 注册规则 |
| `RuleEngine register(RuleGroup group)` | 注册规则组 |
| `RuleEngine unregister(String ruleName)` | 移除规则 |
| `RuleResult fire(RuleContext context)` | 执行所有匹配的规则 |
| `RuleResult fire(RuleContext context, String group)` | 执行指定分组的规则 |
| `RuleResult fireFirst(RuleContext context)` | 只执行第一个匹配的规则 |
| `RuleResult fireUntilHalt(RuleContext context)` | 循环执行直到无规则触发 |
| `List<Rule> getRules()` | 获取所有已注册规则 |
| `List<Rule> getRules(String group)` | 获取指定分组规则 |
| `Rule getRule(String name)` | 按名称获取规则 |
| `boolean hasRule(String name)` | 检查规则是否存在 |
| `int getRuleCount()` | 获取规则数量 |
| `RuleEngine addListener(RuleListener listener)` | 添加监听器 |
| `RuleEngine removeListener(RuleListener listener)` | 移除监听器 |
| `RuleEngine setConflictResolver(ConflictResolver resolver)` | 设置冲突解决策略 |
| `void clear()` | 清空所有规则 |

**示例：**
```java
RuleEngine engine = OpenRules.engine()
    .register(rule1, rule2)
    .setConflictResolver(OpenRules.priorityResolver())
    .addListener(OpenRules.loggingListener())
    .build();

RuleResult result = engine.fire(context);
RuleResult firstResult = engine.fireFirst(context);
RuleResult groupResult = engine.fire(context, "discount-rules");
```

---

### 3.5 RuleResult

> 规则执行结果，不可变 record 类型，包含执行状态、触发/跳过/失败的规则列表、结果和耗时。

**Record 组件：**
- `boolean success` — 是否成功
- `List<String> firedRules` — 已触发规则
- `List<String> skippedRules` — 已跳过规则
- `List<String> failedRules` — 失败规则
- `Map<String, Object> results` — 执行结果
- `Duration executionTime` — 执行耗时
- `List<RuleError> errors` — 错误列表

**主要方法：**

| 方法 | 描述 |
|------|------|
| `int firedCount()` | 触发规则数量 |
| `int skippedCount()` | 跳过规则数量 |
| `int failedCount()` | 失败规则数量 |
| `boolean hasFired()` | 是否有规则被触发 |
| `boolean wasFired(String ruleName)` | 检查指定规则是否被触发 |
| `boolean wasSkipped(String ruleName)` | 检查指定规则是否被跳过 |
| `boolean hasFailed(String ruleName)` | 检查指定规则是否失败 |
| `<T> T getResult(String key)` | 获取指定结果 |
| `<T> T getResult(String key, T defaultValue)` | 获取结果（带默认值） |
| `boolean hasResult(String key)` | 检查结果是否存在 |
| `static Builder successBuilder()` | 创建成功结果构建器 |
| `static Builder failure()` | 创建失败结果构建器 |

**嵌套类型 RuleError：**

| 方法 | 描述 |
|------|------|
| `static RuleError of(String ruleName, String message)` | 创建规则错误 |
| `static RuleError of(String ruleName, String message, Throwable cause)` | 创建带异常的规则错误 |

**示例：**
```java
RuleResult result = engine.fire(context);

if (result.hasFired()) {
    Double discount = result.getResult("discount");
    System.out.println("触发了 " + result.firedCount() + " 条规则");
    System.out.println("折扣: " + discount);
}

if (result.failedCount() > 0) {
    result.errors().forEach(e ->
        System.err.println("规则 " + e.ruleName() + " 失败: " + e.message()));
}
```

---

### 3.6 RuleBuilder

> 规则构建器，提供流式 DSL 风格的规则定义。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `RuleBuilder(String name)` | 构造命名构建器 |
| `RuleBuilder()` | 构造匿名构建器 |
| `static RuleBuilder rule(String name)` | 静态工厂方法 |
| `RuleBuilder description(String description)` | 设置描述 |
| `RuleBuilder priority(int priority)` | 设置优先级 |
| `RuleBuilder group(String group)` | 设置分组 |
| `RuleBuilder enabled(boolean enabled)` | 设置是否启用 |
| `RuleBuilder when(Predicate<RuleContext> predicate)` | 设置条件（谓词） |
| `RuleBuilder when(Condition condition)` | 设置条件（Condition） |
| `RuleBuilder and(Predicate<RuleContext> predicate)` | 添加 AND 条件 |
| `RuleBuilder and(Condition condition)` | 添加 AND 条件（Condition） |
| `RuleBuilder then(Consumer<RuleContext> action)` | 设置动作 |
| `RuleBuilder then(Action action)` | 设置动作（Action） |
| `RuleBuilder andThen(Consumer<RuleContext> action)` | 追加动作 |
| `RuleBuilder andThen(Action action)` | 追加动作（Action） |
| `Rule build()` | 构建规则 |

**示例：**
```java
Rule rule = RuleBuilder.rule("VIP折扣")
    .description("VIP用户订单满1000打9折")
    .priority(1)
    .group("discount")
    .when(ctx -> "VIP".equals(ctx.get("customerType")))
    .and(ctx -> ctx.<Double>get("orderAmount") >= 1000)
    .then(ctx -> {
        double amount = ctx.get("orderAmount");
        ctx.setResult("discount", amount * 0.1);
    })
    .andThen(ctx -> ctx.setResult("applied", true))
    .build();
```

---

### 3.7 RuleEngineBuilder

> 规则引擎构建器，用于流式配置和创建规则引擎。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `RuleEngineBuilder register(Rule... rules)` | 注册规则 |
| `RuleEngineBuilder register(RuleGroup group)` | 注册规则组 |
| `RuleEngineBuilder setConflictResolver(ConflictResolver resolver)` | 设置冲突解决策略 |
| `RuleEngineBuilder addListener(RuleListener listener)` | 添加监听器 |
| `RuleEngine build()` | 构建引擎 |

**示例：**
```java
RuleEngine engine = OpenRules.engine()
    .register(rule1, rule2, rule3)
    .setConflictResolver(OpenRules.priorityResolver())
    .addListener(OpenRules.loggingListener())
    .build();
```

---

### 3.8 RuleGroupBuilder

> 规则组构建器，用于流式创建规则分组。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `RuleGroupBuilder(String name)` | 构造方法 |
| `static RuleGroupBuilder group(String name)` | 静态工厂方法 |
| `RuleGroupBuilder description(String description)` | 设置描述 |
| `RuleGroupBuilder priority(int priority)` | 设置优先级 |
| `RuleGroupBuilder addRule(Rule rule)` | 添加单条规则 |
| `RuleGroupBuilder addRules(Rule... rules)` | 添加多条规则 |
| `RuleGroupBuilder addRules(List<Rule> rules)` | 添加规则列表 |
| `RuleGroup build()` | 构建规则组 |

**示例：**
```java
RuleGroup group = RuleGroupBuilder.group("风控规则")
    .description("订单风控检查")
    .priority(1)
    .addRule(blacklistRule)
    .addRules(amountRule, newUserRule)
    .build();
```

---

### 3.9 RuleGroup

> 规则分组，将相关规则组织在一起，支持优先级排序。实现 `Comparable<RuleGroup>`。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String getName()` | 获取组名 |
| `String getDescription()` | 获取描述 |
| `int getPriority()` | 获取优先级 |
| `List<Rule> getRules()` | 获取组内所有规则 |
| `int size()` | 获取规则数量 |
| `boolean isEmpty()` | 是否为空 |
| `static Builder builder(String name)` | 创建构建器 |

---

### 3.10 Condition

> 条件接口，规则条件的核心抽象，支持逻辑组合（and/or/negate）。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `boolean evaluate(RuleContext context)` | 评估条件 |
| `default Condition and(Condition other)` | 逻辑与 |
| `default Condition or(Condition other)` | 逻辑或 |
| `default Condition negate()` | 逻辑非 |

**示例：**
```java
Condition c1 = ctx -> ctx.<Integer>get("age") > 18;
Condition c2 = c1.and(ctx -> ctx.<Boolean>get("verified"));
Condition c3 = c1.or(ctx -> ctx.<Boolean>get("admin"));
Condition c4 = c1.negate();
```

---

### 3.11 Action

> 动作接口，规则动作的核心抽象，支持动作组合。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `void execute(RuleContext context)` | 执行动作 |
| `default Action andThen(Action after)` | 组合后续动作 |

**示例：**
```java
Action action = ctx -> ctx.setResult("discount", 0.1);
Action combined = action.andThen(ctx -> ctx.setResult("applied", true));
```

---

### 3.12 CompositeCondition

> 组合条件，通过逻辑操作符组合多个条件。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `CompositeCondition(Operator operator, List<Condition> conditions)` | 构造方法 |
| `boolean evaluate(RuleContext context)` | 评估组合条件 |
| `Operator getOperator()` | 获取操作符 |
| `List<Condition> getConditions()` | 获取子条件列表 |
| `static CompositeCondition and(Condition... conditions)` | AND 组合 |
| `static CompositeCondition or(Condition... conditions)` | OR 组合 |
| `static CompositeCondition not(Condition condition)` | NOT 取反 |

**枚举 Operator：** `AND`, `OR`, `NOT`

---

### 3.13 PredicateCondition

> 基于 Java Predicate 的条件实现。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `PredicateCondition(Predicate<RuleContext> predicate)` | 构造方法 |
| `boolean evaluate(RuleContext context)` | 评估条件 |
| `static PredicateCondition of(Predicate<RuleContext> predicate)` | 静态工厂方法 |

---

### 3.14 CompositeAction

> 组合动作，顺序执行多个动作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `CompositeAction(List<Action> actions)` | 构造方法 |
| `void execute(RuleContext context)` | 顺序执行所有动作 |
| `List<Action> getActions()` | 获取动作列表 |
| `static CompositeAction of(Action... actions)` | 静态工厂方法 |

---

### 3.15 ConsumerAction

> 基于 Java Consumer 的动作实现。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `ConsumerAction(Consumer<RuleContext> consumer)` | 构造方法 |
| `void execute(RuleContext context)` | 执行动作 |
| `static ConsumerAction of(Consumer<RuleContext> consumer)` | 静态工厂方法 |

---

### 3.16 ConflictResolver

> 冲突解决器接口，决定多规则匹配时的执行顺序。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `List<Rule> resolve(List<Rule> rules)` | 解决规则冲突，返回排序后的规则列表 |

---

### 3.17 PriorityConflictResolver

> 优先级冲突解决器，按规则优先级（数值越小越高）排序。

**常量：**
- `PriorityConflictResolver INSTANCE` — 单例实例

---

### 3.18 OrderConflictResolver

> 顺序冲突解决器，保持规则注册顺序。

**常量：**
- `OrderConflictResolver INSTANCE` — 单例实例

---

### 3.19 DecisionTable

> 决策表接口，以表格形式定义规则。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String getName()` | 获取决策表名称 |
| `HitPolicy getHitPolicy()` | 获取命中策略 |
| `DecisionResult evaluate(RuleContext context)` | 评估决策表 |
| `DecisionResult evaluate(Map<String, Object> inputs)` | 用 Map 评估 |

---

### 3.20 DecisionTableBuilder

> 决策表构建器，流式 DSL 构建决策表。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `DecisionTableBuilder name(String name)` | 设置名称 |
| `DecisionTableBuilder hitPolicy(HitPolicy hitPolicy)` | 设置命中策略 |
| `DecisionTableBuilder input(String name, Class<?> type)` | 添加输入列（带类型） |
| `DecisionTableBuilder input(String name)` | 添加输入列 |
| `DecisionTableBuilder inputs(String... names)` | 批量添加输入列 |
| `DecisionTableBuilder output(String name, Class<?> type)` | 添加输出列（带类型） |
| `DecisionTableBuilder output(String name)` | 添加输出列 |
| `DecisionTableBuilder outputs(String... names)` | 批量添加输出列 |
| `DecisionTableBuilder row(Object[] conditions, Object[] values)` | 添加规则行 |
| `DecisionTableBuilder addRow(Object... args)` | 添加规则行（扁平参数） |
| `DecisionTable build()` | 构建决策表 |
| `int getRowCount()` | 获取行数 |
| `int getInputColumnCount()` | 获取输入列数 |
| `int getOutputColumnCount()` | 获取输出列数 |

**示例：**
```java
DecisionTable table = OpenRules.decisionTable("年龄分组")
    .hitPolicy(HitPolicy.FIRST)
    .input("age", Integer.class)
    .output("ageGroup", String.class)
    .output("discount", Double.class)
    .row(new Object[]{"< 18"}, new Object[]{"少年", 0.5})
    .row(new Object[]{"18-60"}, new Object[]{"成年", 0.0})
    .row(new Object[]{">= 60"}, new Object[]{"老年", 0.3})
    .build();

DecisionResult result = table.evaluate(context);
if (result.hasMatch()) {
    String group = result.get("ageGroup");
    Double discount = result.get("discount");
}
```

---

### 3.21 DecisionResult

> 决策结果，不可变 record 类型。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `boolean hasMatch()` | 是否有匹配 |
| `<T> T get(String key)` | 获取输出值 |
| `<T> T get(String key, T defaultValue)` | 获取输出值（带默认值） |
| `int matchCount()` | 匹配行数 |
| `static DecisionResult noMatch()` | 创建无匹配结果 |
| `static DecisionResult singleMatch(int rowIndex, Map<String, Object> outputs)` | 创建单匹配结果 |
| `static DecisionResult multipleMatches(List<Integer> matchedRows, List<Map<String, Object>> allOutputs)` | 创建多匹配结果 |

---

### 3.22 HitPolicy

> 命中策略枚举，决定决策表如何处理匹配行。

| 枚举值 | 描述 |
|------|------|
| `UNIQUE` | 唯一匹配，多行匹配时抛异常 |
| `FIRST` | 返回第一个匹配行 |
| `PRIORITY` | 返回优先级最高的匹配行 |
| `ANY` | 返回任意匹配行 |
| `COLLECT` | 返回所有匹配行 |
| `RULE_ORDER` | 按规则定义顺序返回所有匹配 |
| `OUTPUT_ORDER` | 按输出值排序返回所有匹配 |

---

### 3.23 FactStore

> 事实存储接口，管理规则上下文中的事实对象。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `void add(Object fact)` | 添加事实 |
| `void add(String name, Object fact)` | 添加命名事实 |
| `<T> Optional<T> get(Class<T> type)` | 按类型获取事实 |
| `Object get(String name)` | 按名称获取事实 |
| `<T> List<T> getAll(Class<T> type)` | 获取指定类型所有事实 |
| `boolean contains(String name)` | 检查命名事实是否存在 |
| `boolean contains(Class<?> type)` | 检查类型事实是否存在 |
| `Object remove(String name)` | 移除命名事实 |
| `<T> List<T> removeAll(Class<T> type)` | 移除指定类型所有事实 |
| `void clear()` | 清空所有事实 |
| `int size()` | 获取事实数量 |

---

### 3.24 RuleListener

> 规则监听器接口，观察规则执行生命周期事件。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `default void beforeEvaluate(Rule rule, RuleContext context)` | 评估前回调 |
| `default void afterEvaluate(Rule rule, RuleContext context, boolean satisfied)` | 评估后回调 |
| `default void beforeExecute(Rule rule, RuleContext context)` | 执行前回调 |
| `default void afterExecute(Rule rule, RuleContext context)` | 执行后回调 |
| `default void onFailure(Rule rule, RuleContext context, Exception exception)` | 失败回调 |
| `default void onStart(RuleContext context)` | 引擎启动回调 |
| `default void onFinish(RuleContext context, int firedCount, long elapsedMillis)` | 引擎完成回调 |

---

### 3.25 LoggingRuleListener

> 日志监听器，将规则执行事件输出到日志。

---

### 3.26 OpenRulesException

> 规则引擎基础异常，继承自 `OpenException`。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `OpenRulesException(String message)` | 指定消息构造 |
| `OpenRulesException(String message, Throwable cause)` | 指定消息和原因构造 |
| `OpenRulesException(RuleErrorType errorType, String message)` | 指定错误类型构造 |
| `OpenRulesException(String message, String ruleName, RuleErrorType errorType)` | 指定规则名和类型构造 |
| `String ruleName()` | 获取关联的规则名 |
| `RuleErrorType errorType()` | 获取错误类型 |

**枚举 RuleErrorType：** 定义规则引擎的错误类型分类。

---

### 3.27 RuleProvider (SPI)

> 规则提供者 SPI，支持从不同来源（数据库、远程服务等）加载规则。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `List<Rule> getRules()` | 获取所有规则 |
| `List<Rule> getRules(String group)` | 获取指定分组规则 |
| `boolean hasUpdates()` | 检查是否有更新 |
| `String getVersion()` | 获取规则版本 |

---

### 3.28 ActionProvider (SPI)

> 动作提供者 SPI，支持动态发现和加载动作实现。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Action getAction(String name)` | 按名称获取动作 |
| `List<String> getActionNames()` | 获取所有动作名称 |
| `boolean hasAction(String name)` | 检查动作是否存在 |

---

## 4. 使用示例

### 4.1 基础规则定义与执行

```java
// 定义折扣规则
Rule vipRule = OpenRules.rule("VIP折扣")
    .priority(1)
    .group("discount")
    .when(ctx -> "VIP".equals(ctx.get("customerType")))
    .and(ctx -> ctx.<Double>get("orderAmount") >= 1000)
    .then(ctx -> {
        double amount = ctx.get("orderAmount");
        ctx.setResult("discount", amount * 0.1);
        ctx.setResult("finalAmount", amount * 0.9);
    })
    .build();

// 创建引擎并执行
RuleEngine engine = OpenRules.engine()
    .register(vipRule)
    .setConflictResolver(OpenRules.priorityResolver())
    .addListener(OpenRules.loggingListener())
    .build();

RuleContext context = RuleContext.create()
    .put("customerType", "VIP")
    .put("orderAmount", 2000.0);

RuleResult result = engine.fire(context);
Double discount = result.getResult("discount"); // 200.0
```

### 4.2 规则分组与编排

```java
// 风控规则组
RuleGroup riskGroup = OpenRules.group("风控规则")
    .priority(1)
    .addRule(OpenRules.rule("黑名单检查")
        .when(ctx -> isBlacklisted(ctx.get("userId")))
        .then(ctx -> ctx.setResult("riskLevel", "HIGH"))
        .build())
    .addRule(OpenRules.rule("异常金额检查")
        .when(ctx -> ctx.<Double>get("amount") > 100000)
        .then(ctx -> ctx.setResult("riskLevel", "MEDIUM"))
        .build())
    .build();

// 注册并按分组执行
RuleEngine engine = OpenRules.engine().register(riskGroup).build();
RuleResult riskResult = engine.fire(context, "风控规则");
```

### 4.3 决策表

```java
DecisionTable table = OpenRules.decisionTable("定价规则")
    .hitPolicy(HitPolicy.FIRST)
    .input("customerType")
    .input("orderAmount")
    .output("discount")
    .output("freeShipping")
    .addRow("VIP", ">= 10000", 0.20, true)
    .addRow("VIP", ">= 1000", 0.15, true)
    .addRow("NORMAL", ">= 5000", 0.08, true)
    .addRow("NORMAL", ">= 1000", 0.05, false)
    .build();

DecisionResult decision = table.evaluate(context);
if (decision.hasMatch()) {
    Double discount = decision.get("discount");
    Boolean freeShipping = decision.get("freeShipping");
}
```

---

*文档更新日期：2026-02-27*
