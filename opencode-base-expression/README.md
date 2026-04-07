# OpenCode Base Expression

**Lightweight expression engine for Java 25+**

`opencode-base-expression` is a powerful yet lightweight expression evaluation engine supporting arithmetic, comparison, logical operators, property access, method calls, collection operations, and sandboxed execution.

## Features

### Core Features
- **Arithmetic Operators**: `+`, `-`, `*`, `/`, `%` with automatic type promotion
- **Comparison Operators**: `==`, `!=`, `>`, `<`, `>=`, `<=`
- **Logical Operators**: `&&`, `||`, `!`, ternary `? :`
- **Property Access**: Dot notation `user.name`, index access `list[0]`
- **Method Calls**: `str.toUpperCase()`, `list.size()`
- **Collection Operations**: Filtering `list.?[age > 18]`, projection `list.![name]`
- **Built-in Functions**: Math, String, Date, Collection, and Type functions

### Advanced Features
- **Expression Caching**: LRU cache for parsed expressions
- **Sandbox Execution**: Configurable security policies (standard, restrictive, permissive)
- **Type Coercion**: Automatic type conversion with explicit cast support
- **AST Compilation**: Expression compilation and optimization
- **SPI Extension**: Pluggable function providers, property accessors, and type converters
- **Multiple Contexts**: Map context, bean context, chained context

## What's New in V1.0.3

### New Operators
- **Elvis Operator** (`?:`): Null-coalescing operator returns the left operand if non-null, otherwise the right operand
- **In Operator** (`in`): Membership test against a collection
- **Between Operator** (`between`): Inclusive range test
- **Bitwise Operators** (`&`, `|`, `^`, `~`, `<<`, `>>`): Full bitwise operation support

### New Expression Types
- **Lambda Expressions**: First-class lambda support for use with collection functions
- **Map Literals**: Inline map construction with `#{'key': value}` syntax
- **String Interpolation**: Template-style string construction within expressions

### New APIs
- **Expression Visitor** (`ExpressionVisitor<T>`): Generic visitor interface for AST traversal and transformation
- **Variable Extraction** (`VariableExtractor`): Extract referenced variable names from expressions
- **Evaluation Listener** (`EvaluationListener`): Before/after/error hooks for debugging, profiling, and tracing
- **Arithmetic Mode** (`ArithmeticMode`): Switch between standard and BigDecimal precision for financial calculations
- **Expression Template** (`ExpressionTemplate`): Mix literal text with `${expression}` placeholders

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-expression</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.expression.OpenExpression;

// Simple evaluation
Object result = OpenExpression.eval("1 + 2 * 3");  // 7

// With variables
Object result = OpenExpression.eval("name + ' World'", Map.of("name", "Hello"));  // "Hello World"

// With root object
Object result = OpenExpression.eval("user.name", myUser);

// Type conversion
int result = OpenExpression.eval("100 / 3", Integer.class);  // 33
```

### Advanced Usage

```java
// Reusable parser
ExpressionParser parser = OpenExpression.parser();
Expression expr = parser.parseExpression("price * quantity");

// Evaluate with context
StandardContext ctx = OpenExpression.contextBuilder()
    .rootObject(order)
    .sandbox(OpenExpression.standardSandbox())
    .build();
Object result = expr.getValue(ctx);

// Validate expression
boolean valid = OpenExpression.isValid("1 + 2");  // true

// Register custom functions
OpenExpression.functions().register("double", args -> (int) args[0] * 2);
```

### Sandbox Execution

```java
// Standard sandbox (recommended for most use cases)
Sandbox sandbox = OpenExpression.standardSandbox();

// Restrictive sandbox (minimal permissions)
Sandbox sandbox = OpenExpression.restrictiveSandbox();

// Permissive sandbox (for trusted expressions)
Sandbox sandbox = OpenExpression.permissiveSandbox();

StandardContext ctx = OpenExpression.contextBuilder()
    .sandbox(sandbox)
    .build();
```

### Elvis Operator (Null Coalescing)

```java
// Returns left side if non-null, otherwise the right side
Object result = OpenExpression.eval("name ?: 'default'", Map.of());  // "default"
Object result = OpenExpression.eval("name ?: 'default'", Map.of("name", "Jon"));  // "Jon"

// Chain multiple elvis operators
Object result = OpenExpression.eval("a ?: b ?: 'fallback'", Map.of("b", "found"));  // "found"
```

### In Operator (Membership Test)

```java
// Check if a value is contained in a set
Object result = OpenExpression.eval("x in {1, 2, 3}", Map.of("x", 2));  // true
Object result = OpenExpression.eval("x in {1, 2, 3}", Map.of("x", 5));  // false

// Works with string collections
Object result = OpenExpression.eval(
    "status in {'active', 'pending'}",
    Map.of("status", "active")
);  // true
```

### Between Operator (Range Test)

```java
// Inclusive range test: lower <= value <= upper
Object result = OpenExpression.eval("age between 18 and 65", Map.of("age", 25));  // true
Object result = OpenExpression.eval("age between 18 and 65", Map.of("age", 10));  // false

// Boundary values are inclusive
Object result = OpenExpression.eval("x between 1 and 10", Map.of("x", 1));   // true
Object result = OpenExpression.eval("x between 1 and 10", Map.of("x", 10));  // true
```

### Bitwise Operators

```java
// AND, OR, XOR
Object result = OpenExpression.eval("flags & 0x0F", Map.of("flags", 0xFF));  // 15
Object result = OpenExpression.eval("a | b", Map.of("a", 0x0F, "b", 0xF0));  // 255
Object result = OpenExpression.eval("a ^ b", Map.of("a", 0xFF, "b", 0x0F));  // 240

// NOT (bitwise complement)
Object result = OpenExpression.eval("~0", Map.of());  // -1

// Shift operators
Object result = OpenExpression.eval("1 << 3", Map.of());   // 8
Object result = OpenExpression.eval("16 >> 2", Map.of());   // 4
```

### Lambda Expressions

```java
// Lambda with collection functions
Object result = OpenExpression.eval(
    "filter({1,2,3,4,5}, x -> x > 3)", Map.of()
);  // [4, 5]

Object result = OpenExpression.eval(
    "map({1,2,3}, x -> x * 2)", Map.of()
);  // [2, 4, 6]
```

### Map Literals

```java
// Create maps inline with #{} syntax
Object result = OpenExpression.eval("#{'name': 'Jon', 'age': 30}");
// Returns Map with {name=Jon, age=30}

// Use variables in map values
Object result = OpenExpression.eval(
    "#{'greeting': name + ' World'}",
    Map.of("name", "Hello")
);
// Returns Map with {greeting=Hello World}
```

### String Interpolation (Expression Template)

```java
// Simple variable substitution
String result = ExpressionTemplate.render(
    "Hello, ${name}!",
    Map.of("name", "World")
);
// "Hello, World!"

// Expression within template
String result = ExpressionTemplate.render(
    "Total: ${price * quantity}",
    Map.of("price", 9.99, "quantity", 3)
);
// "Total: 29.97"

// Also available via OpenExpression facade
String result = OpenExpression.render(
    "${firstName} ${lastName}",
    Map.of("firstName", "John", "lastName", "Doe")
);
// "John Doe"

// Escape with backslash
String result = ExpressionTemplate.render("Use \\${var} syntax", Map.of());
// "Use ${var} syntax"
```

### Expression Visitor API

```java
import cloud.opencode.base.expression.ExpressionVisitor;
import cloud.opencode.base.expression.ast.*;

// Create a visitor that converts AST to string representation
ExpressionVisitor<String> printer = new ExpressionVisitor<>() {
    @Override
    public String visit(LiteralNode node) {
        return String.valueOf(node.value());
    }

    @Override
    public String visit(BinaryOpNode node) {
        return "(" + visit(node.left()) + " " + node.operator()
            + " " + visit(node.right()) + ")";
    }

    @Override
    public String visit(IdentifierNode node) {
        return node.name();
    }

    // ... implement other visit methods for all node types
};
```

### Variable Extraction

```java
// Extract variable names from an expression
Set<String> vars = VariableExtractor.extract("x + y * 2");
// vars = [x, y]

Set<String> vars = VariableExtractor.extract(
    "user.name == 'John' && age > 18"
);
// vars = [user, age]

// Also available via OpenExpression facade
Set<String> vars = OpenExpression.extractVariables("a + b");
// vars = [a, b]

// Special identifiers (#root, #this, true, false, null) are excluded
Set<String> vars = VariableExtractor.extract("#root.name + true");
// vars = []
```

### Evaluation Listener

```java
// Create a logging listener for debugging
EvaluationListener logger = new EvaluationListener() {
    @Override
    public void beforeEvaluate(Node node, EvaluationContext context) {
        System.out.println("Evaluating: " + node.toExpressionString());
    }

    @Override
    public void afterEvaluate(Node node, EvaluationContext context, Object result) {
        System.out.println("Result: " + result);
    }

    @Override
    public void onError(Node node, EvaluationContext context, Exception error) {
        System.err.println("Error: " + error.getMessage());
    }
};

// Combine multiple listeners (exception-isolated)
EvaluationListener combined = EvaluationListener.composite(logger, timer);

// No-op listener (zero overhead)
EvaluationListener noop = EvaluationListener.noOp();
```

### Arithmetic Mode (BigDecimal Precision)

> **Note**: `ArithmeticMode` is defined in V1.0.3 as a foundation for future BigDecimal evaluation support. Currently the enum can be used for configuration tagging, but the evaluation engine does not yet switch arithmetic behavior based on the mode. Full BigDecimal integration is planned for V1.0.4.

```java
// ArithmeticMode enum values
ArithmeticMode standard = ArithmeticMode.STANDARD;     // Default: int/long/double
ArithmeticMode precise = ArithmeticMode.BIG_DECIMAL;   // Planned: exact decimal arithmetic
```

## Class Reference

### Root Package (`cloud.opencode.base.expression`)
| Class | Description |
|-------|-------------|
| `OpenExpression` | Main entry point facade with static convenience methods |
| `Expression` | Interface representing a parsed expression that can be evaluated |
| `ExpressionParser` | Interface for parsing expression strings into Expression objects |
| `OpenExpressionException` | Base exception for expression evaluation errors |
| `ExpressionVisitor<T>` | Generic visitor interface for AST traversal (V1.0.3) |
| `VariableExtractor` | Extract referenced variable names from expressions (V1.0.3) |
| `EvaluationListener` | Before/after/error hooks for evaluation monitoring (V1.0.3) |
| `ArithmeticMode` | Enum for standard vs BigDecimal arithmetic precision (V1.0.3) |
| `ExpressionTemplate` | Template engine mixing text with `${expression}` placeholders (V1.0.3) |

### AST Package (`cloud.opencode.base.expression.ast`)
| Class | Description |
|-------|-------------|
| `Node` | Base interface for all AST nodes |
| `BinaryOpNode` | Binary operation node (e.g., `a + b`) |
| `UnaryOpNode` | Unary operation node (e.g., `-a`, `!b`) |
| `TernaryOpNode` | Ternary conditional node (`a ? b : c`) |
| `LiteralNode` | Literal value node (numbers, strings, booleans) |
| `IdentifierNode` | Variable identifier reference node |
| `PropertyAccessNode` | Property access node (`obj.property`) |
| `IndexAccessNode` | Index access node (`list[0]`) |
| `MethodCallNode` | Method invocation node (`obj.method()`) |
| `FunctionCallNode` | Function call node (`fn(args)`) |
| `ListLiteralNode` | List literal node (`{1, 2, 3}`) |
| `CollectionFilterNode` | Collection filter node (`list.?[predicate]`) |
| `CollectionProjectNode` | Collection projection node (`list.![expr]`) |
| `ElvisNode` | Elvis (null-coalescing) node (`a ?: b`) (V1.0.3) |
| `InNode` | Membership test node (`x in {1,2,3}`) (V1.0.3) |
| `BetweenNode` | Range test node (`x between 1 and 10`) (V1.0.3) |
| `BitwiseOpNode` | Bitwise operation node (`a & b`, `a \| b`) (V1.0.3) |
| `LambdaNode` | Lambda expression node (`x -> x + 1`) (V1.0.3) |
| `MapLiteralNode` | Map literal node (`#{'key': value}`) (V1.0.3) |
| `StringInterpolationNode` | String interpolation node (V1.0.3) |

### Compiler Package (`cloud.opencode.base.expression.compiler`)
| Class | Description |
|-------|-------------|
| `CompiledExpression` | Pre-compiled expression for faster evaluation |
| `CompiledExpressionCache` | Cache for compiled expressions |
| `ExpressionCache` | Generic expression caching interface |
| `ExpressionCompiler` | Compiles AST into optimized form |
| `Optimizer` | AST optimization (constant folding, dead code elimination) |

### Context Package (`cloud.opencode.base.expression.context`)
| Class | Description |
|-------|-------------|
| `EvaluationContext` | Interface for expression evaluation context |
| `StandardContext` | Full-featured context with variables, root object, and sandbox |
| `MapContext` | Simple map-backed context for variable lookup |
| `BeanContext` | Bean-based context using reflection for property access |
| `ChainedContext` | Composite context that delegates to multiple contexts |

### Eval Package (`cloud.opencode.base.expression.eval`)
| Class | Description |
|-------|-------------|
| `Evaluator` | Expression evaluation interface |
| `AstEvaluator` | AST tree-walking evaluator |
| `OperatorEvaluator` | Arithmetic and comparison operator evaluation with overflow protection |
| `TypeCoercion` | Automatic type conversion between expression types |

### Function Package (`cloud.opencode.base.expression.function`)
| Class | Description |
|-------|-------------|
| `Function` | Functional interface for custom expression functions |
| `FunctionRegistry` | Global and local function registration and lookup |
| `MathFunctions` | Built-in math functions (`abs`, `ceil`, `floor`, `round`, `min`, `max`) |
| `StringFunctions` | Built-in string functions (`length`, `substring`, `trim`, `upper`, `lower`) |
| `DateFunctions` | Built-in date functions (`now`, `today`, `format`) |
| `CollectionFunctions` | Built-in collection functions (`size`, `contains`, `sort`, `filter`) |
| `TypeFunctions` | Built-in type functions (`typeof`, `instanceof`, `cast`) |

### Parser Package (`cloud.opencode.base.expression.parser`)
| Class | Description |
|-------|-------------|
| `Parser` | Recursive descent parser that produces AST nodes |
| `Tokenizer` | Lexical analyzer that tokenizes expression strings |
| `Token` | Token record representing a lexical unit |
| `TokenType` | Enumeration of all token types |
| `ParserException` | Exception thrown for syntax errors during parsing |

### Sandbox Package (`cloud.opencode.base.expression.sandbox`)
| Class | Description |
|-------|-------------|
| `Sandbox` | Interface for expression execution security constraints |
| `DefaultSandbox` | Default sandbox with standard, restrictive, and permissive modes |
| `AllowList` | Configurable class and method allow list |
| `SecurityPolicy` | Security policy configuration for sandbox |
| `SandboxException` | Exception thrown when sandbox security policy is violated |

### SPI Package (`cloud.opencode.base.expression.spi`)
| Class | Description |
|-------|-------------|
| `FunctionProvider` | SPI for pluggable function providers |
| `PropertyAccessor` | SPI for custom property access strategies |
| `TypeConverter` | SPI for custom type conversion |

## Requirements

- Java 25+ (uses records, sealed interfaces, pattern matching)
- No external dependencies for core functionality

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
