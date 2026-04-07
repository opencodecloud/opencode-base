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

## V1.0.3 新特性

### 新增运算符
- **Elvis 运算符** (`?:`)：空值合并运算符，左侧非空则返回左侧，否则返回右侧
- **In 运算符** (`in`)：集合成员测试
- **Between 运算符** (`between`)：包含边界的范围测试
- **位运算符** (`&`、`|`、`^`、`~`、`<<`、`>>`)：完整的位运算支持

### 新增表达式类型
- **Lambda 表达式**：一等公民 Lambda 支持，可与集合函数配合使用
- **Map 字面量**：使用 `#{'key': value}` 语法内联构造 Map
- **字符串插值**：表达式内的模板式字符串构造

### 新增 API
- **表达式访问者** (`ExpressionVisitor<T>`)：用于 AST 遍历和转换的泛型访问者接口
- **变量提取** (`VariableExtractor`)：从表达式中提取引用的变量名
- **求值监听器** (`EvaluationListener`)：求值前/后/错误钩子，用于调试、性能分析和追踪
- **算术模式** (`ArithmeticMode`)：在标准精度和 BigDecimal 精度之间切换，适用于金融计算
- **表达式模板** (`ExpressionTemplate`)：将字面文本与 `${expression}` 占位符混合

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-expression</artifactId>
    <version>1.0.3</version>
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

### Elvis 运算符（空值合并）

```java
// 左侧非空则返回左侧，否则返回右侧
Object result = OpenExpression.eval("name ?: 'default'", Map.of());  // "default"
Object result = OpenExpression.eval("name ?: 'default'", Map.of("name", "Jon"));  // "Jon"

// 链式使用多个 Elvis 运算符
Object result = OpenExpression.eval("a ?: b ?: 'fallback'", Map.of("b", "found"));  // "found"
```

### In 运算符（成员测试）

```java
// 检查值是否包含在集合中
Object result = OpenExpression.eval("x in {1, 2, 3}", Map.of("x", 2));  // true
Object result = OpenExpression.eval("x in {1, 2, 3}", Map.of("x", 5));  // false

// 支持字符串集合
Object result = OpenExpression.eval(
    "status in {'active', 'pending'}",
    Map.of("status", "active")
);  // true
```

### Between 运算符（范围测试）

```java
// 包含边界的范围测试：lower <= value <= upper
Object result = OpenExpression.eval("age between 18 and 65", Map.of("age", 25));  // true
Object result = OpenExpression.eval("age between 18 and 65", Map.of("age", 10));  // false

// 边界值是包含的
Object result = OpenExpression.eval("x between 1 and 10", Map.of("x", 1));   // true
Object result = OpenExpression.eval("x between 1 and 10", Map.of("x", 10));  // true
```

### 位运算符

```java
// 与、或、异或
Object result = OpenExpression.eval("flags & 0x0F", Map.of("flags", 0xFF));  // 15
Object result = OpenExpression.eval("a | b", Map.of("a", 0x0F, "b", 0xF0));  // 255
Object result = OpenExpression.eval("a ^ b", Map.of("a", 0xFF, "b", 0x0F));  // 240

// 取反（按位补码）
Object result = OpenExpression.eval("~0", Map.of());  // -1

// 移位运算符
Object result = OpenExpression.eval("1 << 3", Map.of());   // 8
Object result = OpenExpression.eval("16 >> 2", Map.of());   // 4
```

### Lambda 表达式

```java
// Lambda 与集合函数配合使用
Object result = OpenExpression.eval(
    "filter({1,2,3,4,5}, x -> x > 3)", Map.of()
);  // [4, 5]

Object result = OpenExpression.eval(
    "map({1,2,3}, x -> x * 2)", Map.of()
);  // [2, 4, 6]
```

### Map 字面量

```java
// 使用 #{} 语法内联创建 Map
Object result = OpenExpression.eval("#{'name': 'Jon', 'age': 30}");
// 返回 Map：{name=Jon, age=30}

// 在 Map 值中使用变量
Object result = OpenExpression.eval(
    "#{'greeting': name + ' World'}",
    Map.of("name", "Hello")
);
// 返回 Map：{greeting=Hello World}
```

### 字符串插值（表达式模板）

```java
// 简单变量替换
String result = ExpressionTemplate.render(
    "Hello, ${name}!",
    Map.of("name", "World")
);
// "Hello, World!"

// 模板内的表达式求值
String result = ExpressionTemplate.render(
    "Total: ${price * quantity}",
    Map.of("price", 9.99, "quantity", 3)
);
// "Total: 29.97"

// 也可通过 OpenExpression 门面调用
String result = OpenExpression.render(
    "${firstName} ${lastName}",
    Map.of("firstName", "John", "lastName", "Doe")
);
// "John Doe"

// 使用反斜杠转义
String result = ExpressionTemplate.render("Use \\${var} syntax", Map.of());
// "Use ${var} syntax"
```

### 表达式访问者 API

```java
import cloud.opencode.base.expression.ExpressionVisitor;
import cloud.opencode.base.expression.ast.*;

// 创建将 AST 转换为字符串表示的访问者
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

    // ... 为所有节点类型实现其他 visit 方法
};
```

### 变量提取

```java
// 从表达式中提取变量名
Set<String> vars = VariableExtractor.extract("x + y * 2");
// vars = [x, y]

Set<String> vars = VariableExtractor.extract(
    "user.name == 'John' && age > 18"
);
// vars = [user, age]

// 也可通过 OpenExpression 门面调用
Set<String> vars = OpenExpression.extractVariables("a + b");
// vars = [a, b]

// 特殊标识符（#root、#this、true、false、null）会被排除
Set<String> vars = VariableExtractor.extract("#root.name + true");
// vars = []
```

### 求值监听器

```java
// 创建用于调试的日志监听器
EvaluationListener logger = new EvaluationListener() {
    @Override
    public void beforeEvaluate(Node node, EvaluationContext context) {
        System.out.println("正在求值: " + node.toExpressionString());
    }

    @Override
    public void afterEvaluate(Node node, EvaluationContext context, Object result) {
        System.out.println("结果: " + result);
    }

    @Override
    public void onError(Node node, EvaluationContext context, Exception error) {
        System.err.println("错误: " + error.getMessage());
    }
};

// 组合多个监听器（异常隔离）
EvaluationListener combined = EvaluationListener.composite(logger, timer);

// 空操作监听器（零开销）
EvaluationListener noop = EvaluationListener.noOp();
```

### 算术模式（BigDecimal 精度）

> **注意**：`ArithmeticMode` 在 V1.0.3 中定义为未来 BigDecimal 求值支持的基础。当前枚举可用于配置标记，但求值引擎尚未根据模式切换算术行为。完整的 BigDecimal 集成计划在 V1.0.4 中实现。

```java
// ArithmeticMode 枚举值
ArithmeticMode standard = ArithmeticMode.STANDARD;     // 默认：int/long/double
ArithmeticMode precise = ArithmeticMode.BIG_DECIMAL;   // 计划中：精确十进制算术
```

## 类参考

### 根包 (`cloud.opencode.base.expression`)
| 类 | 说明 |
|---|------|
| `OpenExpression` | 主入口门面类，提供静态便捷方法 |
| `Expression` | 表示可求值的已解析表达式的接口 |
| `ExpressionParser` | 将表达式字符串解析为 Expression 对象的接口 |
| `OpenExpressionException` | 表达式求值错误的基础异常 |
| `ExpressionVisitor<T>` | 用于 AST 遍历的泛型访问者接口（V1.0.3） |
| `VariableExtractor` | 从表达式中提取引用的变量名（V1.0.3） |
| `EvaluationListener` | 求值前/后/错误监控钩子（V1.0.3） |
| `ArithmeticMode` | 标准与 BigDecimal 算术精度模式枚举（V1.0.3） |
| `ExpressionTemplate` | 将文本与 `${expression}` 占位符混合的模板引擎（V1.0.3） |

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
| `ElvisNode` | Elvis（空值合并）节点（`a ?: b`）（V1.0.3） |
| `InNode` | 成员测试节点（`x in {1,2,3}`）（V1.0.3） |
| `BetweenNode` | 范围测试节点（`x between 1 and 10`）（V1.0.3） |
| `BitwiseOpNode` | 位运算节点（`a & b`、`a \| b`）（V1.0.3） |
| `LambdaNode` | Lambda 表达式节点（`x -> x + 1`）（V1.0.3） |
| `MapLiteralNode` | Map 字面量节点（`#{'key': value}`）（V1.0.3） |
| `StringInterpolationNode` | 字符串插值节点（V1.0.3） |

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
