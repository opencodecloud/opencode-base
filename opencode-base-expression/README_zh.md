# OpenCode Base Expression

**轻量级表达式引擎，适用于 Java 25+**

`opencode-base-expression` 是一个功能强大且轻量级的表达式求值引擎，支持算术、比较、逻辑运算符、属性访问、方法调用、集合操作和沙箱执行。

## 功能特性

### 核心功能
- **算术运算符**：`+`、`-`、`*`、`/`、`%`，支持自动类型提升
- **比较运算符**：`==`、`!=`、`>`、`<`、`>=`、`<=`
- **逻辑运算符**：`&&`、`||`、`!`、三元运算 `? :`
- **属性访问**：点号表示法 `user.name`、索引访问 `list[0]`
- **方法调用**：`str.toUpperCase()`、`list.size()`
- **集合操作**：过滤 `list.?[age > 18]`、投影 `list.![name]`
- **内置函数**：数学、字符串、日期、集合和类型函数

### 高级功能
- **表达式缓存**：基于 LRU 的已解析表达式缓存
- **沙箱执行**：可配置的安全策略（标准、限制性、宽松）
- **类型强制转换**：自动类型转换，支持显式转换
- **AST 编译**：表达式编译和优化
- **SPI 扩展**：可插拔的函数提供者、属性访问器和类型转换器
- **多种上下文**：Map 上下文、Bean 上下文、链式上下文

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-expression</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.expression.OpenExpression;

// 简单求值
Object result = OpenExpression.eval("1 + 2 * 3");  // 7

// 使用变量
Object result = OpenExpression.eval("name + ' World'", Map.of("name", "Hello"));  // "Hello World"

// 使用根对象
Object result = OpenExpression.eval("user.name", myUser);

// 类型转换
int result = OpenExpression.eval("100 / 3", Integer.class);  // 33
```

### 高级用法

```java
// 可复用的解析器
ExpressionParser parser = OpenExpression.parser();
Expression expr = parser.parseExpression("price * quantity");

// 使用上下文求值
StandardContext ctx = OpenExpression.contextBuilder()
    .rootObject(order)
    .sandbox(OpenExpression.standardSandbox())
    .build();
Object result = expr.getValue(ctx);

// 验证表达式
boolean valid = OpenExpression.isValid("1 + 2");  // true

// 注册自定义函数
OpenExpression.functions().register("double", args -> (int) args[0] * 2);
```

### 沙箱执行

```java
// 标准沙箱（推荐用于大多数场景）
Sandbox sandbox = OpenExpression.standardSandbox();

// 限制性沙箱（最小权限）
Sandbox sandbox = OpenExpression.restrictiveSandbox();

// 宽松沙箱（用于受信任的表达式）
Sandbox sandbox = OpenExpression.permissiveSandbox();

StandardContext ctx = OpenExpression.contextBuilder()
    .sandbox(sandbox)
    .build();
```

## 类参考

### 根包 (`cloud.opencode.base.expression`)
| 类 | 说明 |
|---|------|
| `OpenExpression` | 主入口门面类，提供静态便捷方法 |
| `Expression` | 表示可求值的已解析表达式的接口 |
| `ExpressionParser` | 将表达式字符串解析为 Expression 对象的接口 |
| `OpenExpressionException` | 表达式求值错误的基础异常 |

### AST 包 (`cloud.opencode.base.expression.ast`)
| 类 | 说明 |
|---|------|
| `Node` | 所有 AST 节点的基础接口 |
| `BinaryOpNode` | 二元运算节点（如 `a + b`） |
| `UnaryOpNode` | 一元运算节点（如 `-a`、`!b`） |
| `TernaryOpNode` | 三元条件节点（`a ? b : c`） |
| `LiteralNode` | 字面量值节点（数字、字符串、布尔值） |
| `IdentifierNode` | 变量标识符引用节点 |
| `PropertyAccessNode` | 属性访问节点（`obj.property`） |
| `IndexAccessNode` | 索引访问节点（`list[0]`） |
| `MethodCallNode` | 方法调用节点（`obj.method()`） |
| `FunctionCallNode` | 函数调用节点（`fn(args)`） |
| `ListLiteralNode` | 列表字面量节点（`{1, 2, 3}`） |
| `CollectionFilterNode` | 集合过滤节点（`list.?[predicate]`） |
| `CollectionProjectNode` | 集合投影节点（`list.![expr]`） |

### 编译器包 (`cloud.opencode.base.expression.compiler`)
| 类 | 说明 |
|---|------|
| `CompiledExpression` | 预编译表达式，求值更快 |
| `CompiledExpressionCache` | 编译表达式缓存 |
| `ExpressionCache` | 通用表达式缓存接口 |
| `ExpressionCompiler` | 将 AST 编译为优化形式 |
| `Optimizer` | AST 优化（常量折叠、死代码消除） |

### 上下文包 (`cloud.opencode.base.expression.context`)
| 类 | 说明 |
|---|------|
| `EvaluationContext` | 表达式求值上下文接口 |
| `StandardContext` | 功能完整的上下文，支持变量、根对象和沙箱 |
| `MapContext` | 基于 Map 的简单上下文，用于变量查找 |
| `BeanContext` | 基于 Bean 的上下文，使用反射进行属性访问 |
| `ChainedContext` | 组合上下文，委托给多个上下文 |

### 求值包 (`cloud.opencode.base.expression.eval`)
| 类 | 说明 |
|---|------|
| `Evaluator` | 表达式求值接口 |
| `AstEvaluator` | AST 树遍历求值器 |
| `OperatorEvaluator` | 算术和比较运算符求值，带溢出保护 |
| `TypeCoercion` | 表达式类型之间的自动类型转换 |

### 函数包 (`cloud.opencode.base.expression.function`)
| 类 | 说明 |
|---|------|
| `Function` | 自定义表达式函数的函数接口 |
| `FunctionRegistry` | 全局和局部函数注册与查找 |
| `MathFunctions` | 内置数学函数（`abs`、`ceil`、`floor`、`round`、`min`、`max`） |
| `StringFunctions` | 内置字符串函数（`length`、`substring`、`trim`、`upper`、`lower`） |
| `DateFunctions` | 内置日期函数（`now`、`today`、`format`） |
| `CollectionFunctions` | 内置集合函数（`size`、`contains`、`sort`、`filter`） |
| `TypeFunctions` | 内置类型函数（`typeof`、`instanceof`、`cast`） |

### 解析器包 (`cloud.opencode.base.expression.parser`)
| 类 | 说明 |
|---|------|
| `Parser` | 递归下降解析器，生成 AST 节点 |
| `Tokenizer` | 词法分析器，将表达式字符串分词 |
| `Token` | 表示词法单元的 Token 记录 |
| `TokenType` | 所有 Token 类型的枚举 |
| `ParserException` | 解析过程中语法错误抛出的异常 |

### 沙箱包 (`cloud.opencode.base.expression.sandbox`)
| 类 | 说明 |
|---|------|
| `Sandbox` | 表达式执行安全约束接口 |
| `DefaultSandbox` | 默认沙箱，提供标准、限制性和宽松模式 |
| `AllowList` | 可配置的类和方法白名单 |
| `SecurityPolicy` | 沙箱安全策略配置 |
| `SandboxException` | 违反沙箱安全策略时抛出的异常 |

### SPI 包 (`cloud.opencode.base.expression.spi`)
| 类 | 说明 |
|---|------|
| `FunctionProvider` | 可插拔函数提供者的 SPI |
| `PropertyAccessor` | 自定义属性访问策略的 SPI |
| `TypeConverter` | 自定义类型转换的 SPI |

## 环境要求

- Java 25+（使用 record、密封接口、模式匹配）
- 核心功能无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
