# OpenCode Base Rules

**Lightweight business rule engine with DSL, decision tables, execution tracing, scoring engine and conflict resolution for Java 25+**

`opencode-base-rules` is a lightweight yet powerful business rule engine that provides a fluent DSL for defining rules, a decision table engine, multiple firing modes, conflict resolution strategies, execution tracing, rule validation, metrics collection, scoring engine and more.

## Features

### Core Features
- **Fluent Rule DSL**: Define rules with a readable builder pattern (when/then)
- **Rule Engine**: Register, manage, and execute business rules
- **Multiple Firing Modes**: Fire all, fire first, fire until halt (inference)
- **Rule Groups**: Organize rules into logical groups
- **Rule Context**: Typed fact store for passing data between rules
- **Terminal Rules**: Mark rules as terminal to stop engine execution when fired
- **Halt Conditions**: Define custom predicates to halt rule execution dynamically

### Type Safety (V1.0.3)
- **TypedKey\<T\>**: Compile-time type-safe keys for fact and variable access
- **Eliminates ClassCastException**: No manual casting needed when accessing facts

### Execution Tracing (V1.0.3)
- **ExecutionTrace**: Full structured audit trail of every rule evaluation
- **RuleTrace**: Per-rule trace with condition result, duration, and error info
- **TracingRuleListener**: Drop-in listener that collects trace data
- **fireAndTrace()**: One-call method to fire rules and get execution trace

### Rule Explanation (V1.0.3)
- **RuleExplainer**: Generate human-readable explanations from execution traces
- **Explanation**: Structured summary with per-rule details

### Rule Validation (V1.0.3)
- **RuleValidator**: Static analysis of rule sets before execution
- **Detects**: Duplicate names, empty names, null conditions, negative priority

### Rule Metrics (V1.0.3)
- **RuleMetrics**: Thread-safe per-rule metrics collection (evaluation count, fire count, duration)
- **MetricsListener**: Drop-in listener that feeds metrics automatically
- **MetricsSnapshot**: Point-in-time snapshot of per-rule statistics

### Scoring Engine (V1.0.3)
- **ScoringRule**: Rules that produce numeric scores
- **ScoringEngine**: Evaluate and aggregate scores with configurable strategies
- **Aggregation Strategies**: SUM, WEIGHTED_SUM, MAX, AVERAGE

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

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-rules</artifactId>
    <version>1.0.3</version>
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

### Type-Safe Fact Access
```java
import cloud.opencode.base.rules.key.TypedKey;

// Define typed keys
TypedKey<String> CUSTOMER_TYPE = OpenRules.key("customerType", String.class);
TypedKey<Double> AMOUNT = OpenRules.key("amount", Double.class);
TypedKey<Double> DISCOUNT = OpenRules.key("discount", Double.class);

// Type-safe access — no casting needed
RuleContext context = RuleContext.create();
context.put(CUSTOMER_TYPE, "VIP");
context.put(AMOUNT, 1500.0);

String type = context.get(CUSTOMER_TYPE);   // String — compiler checked
Double amount = context.get(AMOUNT);         // Double — compiler checked
```

### Execution Tracing
```java
import cloud.opencode.base.rules.trace.*;

// Fire rules and get full execution trace
ExecutionTrace trace = engine.fireAndTrace(context);

System.out.println("Fired: " + trace.firedCount());
System.out.println("Duration: " + trace.totalDuration().toMillis() + "ms");

for (RuleTrace rt : trace.ruleTraces()) {
    System.out.println(rt.ruleName() + " → " +
        (rt.hasFired() ? "FIRED" : "SKIPPED") +
        " (" + rt.duration().toMillis() + "ms)");
}
```

### Rule Explanation
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

### Terminal Rules and Halt Conditions
```java
// Terminal rule — stops engine when fired
Rule stopRule = OpenRules.rule("stop-rule")
    .when(ctx -> ctx.get("amount", 0.0) > 10000)
    .then(ctx -> ctx.setResult("blocked", true))
    .terminal()
    .build();

// Halt condition on engine
RuleEngine engine = OpenRules.engine()
    .register(rule1, rule2, rule3)
    .haltWhen(ctx -> ctx.contains("done"))
    .build();
```

### Rule Validation
```java
import cloud.opencode.base.rules.validation.*;

ValidationReport report = OpenRules.validate(engine.getRules());
if (!report.isValid()) {
    report.errors().forEach(issue ->
        System.err.println(issue.type() + ": " + issue.message()));
}
```

### Rule Metrics
```java
import cloud.opencode.base.rules.metric.*;

RuleMetrics metrics = OpenRules.metrics();
MetricsListener listener = OpenRules.metricsListener(metrics);

engine.addListener(listener);

// After some executions...
MetricsSnapshot snapshot = metrics.getSnapshot("discount-rule");
System.out.println("Evaluations: " + snapshot.evaluationCount());
System.out.println("Fire rate: " + snapshot.fireRate());
System.out.println("Avg duration: " + snapshot.avgDurationNanos() + "ns");
```

### Scoring Engine
```java
import cloud.opencode.base.rules.score.*;

// Create scoring rules
ScoringRule creditScore = new ScoringRule() {
    public String getName() { return "credit-check"; }
    public String getDescription() { return "Credit score check"; }
    public int getPriority() { return 1; }
    public boolean evaluate(RuleContext ctx) { return true; }
    public void execute(RuleContext ctx) {}
    public double score(RuleContext ctx) { return 0.8; }
    public double weight() { return 0.5; }
};

ScoreResult result = ScoringEngine.score(context,
    List.of(creditScore), AggregationStrategy.WEIGHTED_SUM);

System.out.println("Total score: " + result.totalScore());
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
| `Rule` | Interface representing a business rule (condition + action + terminal) |
| `RuleEngine` | Interface for registering, managing, and executing rules |
| `RuleContext` | Typed fact store with TypedKey support for passing data between rules |
| `RuleResult` | Execution result containing fired rules and statistics |

### Key (`cloud.opencode.base.rules.key`)
| Class | Description |
|-------|-------------|
| `TypedKey<T>` | Type-safe key record for compile-time checked fact access |

### Trace (`cloud.opencode.base.rules.trace`)
| Class | Description |
|-------|-------------|
| `ExecutionTrace` | Complete execution trace record with per-rule details |
| `RuleTrace` | Single rule evaluation trace record |
| `TracingRuleListener` | Listener that collects execution trace data |

### Explain (`cloud.opencode.base.rules.explain`)
| Class | Description |
|-------|-------------|
| `RuleExplainer` | Generates human-readable explanations from execution traces |
| `Explanation` | Structured explanation record with summary and details |
| `RuleExplanation` | Per-rule explanation record |

### Validation (`cloud.opencode.base.rules.validation`)
| Class | Description |
|-------|-------------|
| `RuleValidator` | Static rule set validator |
| `ValidationReport` | Validation result with issues list |
| `ValidationIssue` | Individual validation issue with severity and type |

### Metric (`cloud.opencode.base.rules.metric`)
| Class | Description |
|-------|-------------|
| `RuleMetrics` | Thread-safe per-rule metrics collector |
| `MetricsSnapshot` | Point-in-time metrics snapshot record |
| `MetricsListener` | Listener that feeds metrics from rule execution |

### Score (`cloud.opencode.base.rules.score`)
| Class | Description |
|-------|-------------|
| `ScoringRule` | Rule interface that produces numeric scores |
| `ScoringEngine` | Evaluates scoring rules and aggregates results |
| `ScoreResult` | Scoring result with per-rule scores and total |
| `AggregationStrategy` | Enum: SUM, WEIGHTED_SUM, MAX, AVERAGE |

### Engine (`cloud.opencode.base.rules.engine`)
| Class | Description |
|-------|-------------|
| `DefaultRuleEngine` | Default thread-safe RuleEngine with terminal/halt support |
| `DefaultRule` | Default immutable Rule implementation with terminal flag |

### Model (`cloud.opencode.base.rules.model`)
| Class | Description |
|-------|-------------|
| `Condition` | Functional interface for rule conditions |
| `Action` | Functional interface for rule actions |
| `RuleGroup` | Named group of related rules |
| `FactStore` | Interface for storing and retrieving facts (with TypedKey support) |
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
| `RuleBuilder` | Fluent builder for creating rules (with terminal support) |
| `RuleEngineBuilder` | Fluent builder for configuring rule engines (with haltWhen) |
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
