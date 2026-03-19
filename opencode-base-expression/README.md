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

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-expression</artifactId>
    <version>1.0.0</version>
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

## Class Reference

### Root Package (`cloud.opencode.base.expression`)
| Class | Description |
|-------|-------------|
| `OpenExpression` | Main entry point facade with static convenience methods |
| `Expression` | Interface representing a parsed expression that can be evaluated |
| `ExpressionParser` | Interface for parsing expression strings into Expression objects |
| `OpenExpressionException` | Base exception for expression evaluation errors |

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
