# OpenCode Base Rules

**Lightweight business rule engine with DSL, decision tables and conflict resolution for Java 25+**

`opencode-base-rules` is a lightweight yet powerful business rule engine that provides a fluent DSL for defining rules, a decision table engine, multiple firing modes, conflict resolution strategies, and execution listeners.

## Features

### Core Features
- **Fluent Rule DSL**: Define rules with a readable builder pattern (when/then)
- **Rule Engine**: Register, manage, and execute business rules
- **Multiple Firing Modes**: Fire all, fire first, fire until halt (inference)
- **Rule Groups**: Organize rules into logical groups
- **Rule Context**: Typed fact store for passing data between rules

### Decision Tables
- **Decision Table Builder**: Fluent API for building decision tables
- **Hit Policies**: First, unique, any, priority, collect, and rule-order policies
- **Input/Output Columns**: Typed input conditions and output actions

### Conflict Resolution
- **Priority Resolver**: Execute rules by priority (lower number = higher priority)
- **Order Resolver**: Execute rules in registration order
- **Custom Resolvers**: Implement the ConflictResolver interface

### Extensibility
- **Execution Listeners**: Observe rule evaluation and execution lifecycle
- **Logging Listener**: Built-in logging listener for debugging
- **SPI Support**: ActionProvider and RuleProvider for service-based rule loading
- **Condition Factories**: Predicate-based, always-true, and always-false conditions
- **Action Factories**: Consumer-based and no-op actions

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-rules</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Define and Execute Rules
```java
import cloud.opencode.base.rules.*;

// Create a rule
Rule discountRule = OpenRules.rule("discount-rule")
    .description("Apply discount for VIP customers")
    .priority(100)
    .when(ctx -> "VIP".equals(ctx.get("customerType")))
    .then(ctx -> ctx.put("discount", 0.15))
    .build();

// Create and configure engine
RuleEngine engine = OpenRules.engine()
    .register(discountRule)
    .setConflictResolver(OpenRules.priorityResolver())
    .addListener(OpenRules.loggingListener())
    .build();

// Execute rules
RuleContext context = OpenRules.contextOf("customerType", "VIP", "amount", 1000.0);
RuleResult result = engine.fire(context);

System.out.println("Discount: " + context.get("discount")); // 0.15
```

### Decision Tables
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

### Rule Groups
```java
RuleGroup group = OpenRules.group("discount-rules")
    .add(vipRule)
    .add(seniorRule)
    .add(bulkRule)
    .build();

RuleEngine engine = OpenRules.engineWith(group);
RuleResult result = engine.fire(context, "discount-rules");
```

### Firing Modes
```java
// Fire all matching rules
RuleResult result = engine.fire(context);

// Fire only the first matching rule
RuleResult result = engine.fireFirst(context);

// Fire rules repeatedly until no more can fire (inference)
RuleResult result = engine.fireUntilHalt(context);
```

## Class Reference

### Root Package (`cloud.opencode.base.rules`)
| Class | Description |
|-------|-------------|
| `OpenRules` | Main facade with factory methods for rules, engines, and decision tables |
| `Rule` | Interface representing a business rule (condition + action) |
| `RuleEngine` | Interface for registering, managing, and executing rules |
| `RuleContext` | Typed fact store for passing data between rules |
| `RuleResult` | Execution result containing fired rules and statistics |

### Engine (`cloud.opencode.base.rules.engine`)
| Class | Description |
|-------|-------------|
| `DefaultRuleEngine` | Default implementation of RuleEngine |
| `DefaultRule` | Default implementation of Rule |

### Model (`cloud.opencode.base.rules.model`)
| Class | Description |
|-------|-------------|
| `Condition` | Functional interface for rule conditions |
| `Action` | Functional interface for rule actions |
| `RuleGroup` | Named group of related rules |
| `FactStore` | Interface for storing and retrieving facts |
| `DefaultFactStore` | Default implementation of FactStore |

### Condition (`cloud.opencode.base.rules.condition`)
| Class | Description |
|-------|-------------|
| `PredicateCondition` | Condition backed by a Predicate |
| `CompositeCondition` | AND/OR composition of multiple conditions |

### Action (`cloud.opencode.base.rules.action`)
| Class | Description |
|-------|-------------|
| `ConsumerAction` | Action backed by a Consumer |
| `CompositeAction` | Sequential composition of multiple actions |

### Decision (`cloud.opencode.base.rules.decision`)
| Class | Description |
|-------|-------------|
| `DecisionTable` | Interface for decision table evaluation |
| `SimpleDecisionTable` | Default decision table implementation |
| `DecisionResult` | Result of a decision table evaluation |
| `HitPolicy` | Enum of hit policies (FIRST, UNIQUE, ANY, PRIORITY, COLLECT, RULE_ORDER) |

### DSL (`cloud.opencode.base.rules.dsl`)
| Class | Description |
|-------|-------------|
| `RuleBuilder` | Fluent builder for creating rules |
| `RuleEngineBuilder` | Fluent builder for configuring rule engines |
| `RuleGroupBuilder` | Fluent builder for creating rule groups |
| `DecisionTableBuilder` | Fluent builder for creating decision tables |

### Conflict (`cloud.opencode.base.rules.conflict`)
| Class | Description |
|-------|-------------|
| `ConflictResolver` | Interface for resolving rule ordering conflicts |
| `PriorityConflictResolver` | Resolves by rule priority |
| `OrderConflictResolver` | Resolves by registration order |

### Listener (`cloud.opencode.base.rules.listener`)
| Class | Description |
|-------|-------------|
| `RuleListener` | Interface for observing rule execution lifecycle |
| `LoggingRuleListener` | Built-in listener that logs rule evaluation and execution |

### SPI (`cloud.opencode.base.rules.spi`)
| Class | Description |
|-------|-------------|
| `RuleProvider` | SPI for loading rules from external sources |
| `ActionProvider` | SPI for loading actions from external sources |

### Exception (`cloud.opencode.base.rules.exception`)
| Class | Description |
|-------|-------------|
| `OpenRulesException` | Runtime exception for rule engine errors |

## Requirements

- Java 25+
- No external dependencies (optional: `opencode-base-expression` for expression-based conditions)

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
