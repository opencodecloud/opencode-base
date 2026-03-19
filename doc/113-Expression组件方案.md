# Expression 组件方案

## 1. 组件概述

### 1.1 设计目标

`opencode-base-expression` 模块提供轻量级、类型安全的表达式引擎。

**核心特性：**
- 算术运算（+、-、*、/、%、**）
- 比较运算（==、!=、>、<、>=、<=）
- 逻辑运算（&&、||、!）
- 三元运算（? :）
- 属性访问（user.name、items[0]）
- 方法调用（str.toUpperCase()）
- 空安全导航（user?.address?.city）
- 集合操作（过滤、投影）
- 内置函数（字符串、数学、集合、日期、类型）
- 沙箱执行（安全策略、白名单/黑名单控制）
- 表达式编译缓存和 AST 优化

**设计原则：**
- **零外部依赖**：不依赖 Spring SpEL、JEXL、MVEL 等
- **类型安全**：编译时类型检查，运行时类型推断
- **高性能**：表达式编译缓存，AST 常量折叠/短路优化
- **安全优先**：沙箱执行，防止代码注入

### 1.2 架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                         应用层                                   │
│        (配置求值 / 规则引擎 / 模板渲染 / 条件校验)               │
└─────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│   表达式 API    │   │   内置函数      │   │   安全沙箱      │
│                 │   │                 │   │                 │
│ Expression      │   │ StringFunctions │   │ Sandbox         │
│ ExpressionParser│   │ MathFunctions   │   │ DefaultSandbox  │
│ OpenExpression  │   │ CollectionFuncs │   │ SecurityPolicy  │
│ CompiledExpr    │   │ DateFunctions   │   │ AllowList       │
│                 │   │ TypeFunctions   │   │ SandboxException│
└─────────────────┘   └─────────────────┘   └─────────────────┘
        │                       │                       │
        └───────────────────────┼───────────────────────┘
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    AST 与求值引擎                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │  Tokenizer   │  │   Parser    │  │      AstEvaluator      │  │
│  │  (词法分析)  │  │  (语法分析)  │  │      (求值器)          │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                      AST 节点类型 (sealed)                   │ │
│  │  Literal | BinaryOp | UnaryOp | TernaryOp | PropertyAccess │ │
│  │  IndexAccess | MethodCall | FunctionCall | Identifier      │ │
│  │  ListLiteral | CollectionFilter | CollectionProject        │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    编译优化                                   │ │
│  │  ExpressionCompiler | ExpressionCache | Optimizer           │ │
│  │  CompiledExpressionCache                                    │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   SPI 扩展点                                      │
│  ┌───────────────┐  ┌───────────────┐  ┌─────────────────────┐  │
│  │FunctionProvider│  │TypeConverter  │  │  PropertyAccessor   │  │
│  └───────────────┘  └───────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 模块依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-expression</artifactId>
    <version>${version}</version>
</dependency>
```

```
expression 模块依赖:
├── opencode-base-core (必需，提供 Preconditions、OpenException)
└── 无其他外部依赖

依赖 expression 的组件:
├── config → expression (配置值动态计算)
├── validation → expression (条件校验表达式)
├── feature → expression (功能开关条件)
├── string/template → expression (模板表达式)
└── tasker → expression (任务条件判断)
```

---

## 2. 包结构

```
cloud.opencode.base.expression
├── OpenExpression.java              # 门面入口类
├── Expression.java                  # 表达式接口
├── ExpressionParser.java            # 解析器接口
├── OpenExpressionException.java     # 异常类
│
├── ast/                             # 抽象语法树 (sealed)
│   ├── Node.java                   # AST 节点接口 (sealed interface)
│   ├── LiteralNode.java            # 字面量（数字、字符串、布尔、null）
│   ├── IdentifierNode.java         # 标识符（变量名）
│   ├── BinaryOpNode.java           # 二元运算（+、-、*、/、==、&&等）
│   ├── UnaryOpNode.java            # 一元运算（!、-）
│   ├── TernaryOpNode.java          # 三元运算（? :）
│   ├── PropertyAccessNode.java     # 属性访问（user.name，支持空安全）
│   ├── IndexAccessNode.java        # 索引访问（list[0]，支持空安全）
│   ├── MethodCallNode.java         # 方法调用（str.toUpperCase()，支持空安全）
│   ├── FunctionCallNode.java       # 函数调用（upper(str)）
│   ├── ListLiteralNode.java        # 列表字面量
│   ├── CollectionFilterNode.java   # 集合过滤（users.?[age>18]）
│   └── CollectionProjectNode.java  # 集合投影（users.![name]）
│
├── parser/                          # 解析器
│   ├── Tokenizer.java              # 词法分析器
│   ├── Token.java                  # 词法单元 (record)
│   ├── TokenType.java              # 词法类型枚举
│   ├── Parser.java                 # 语法分析器
│   └── ParserException.java        # 解析异常（含位置信息）
│
├── eval/                            # 求值器
│   ├── Evaluator.java              # 求值器接口
│   ├── AstEvaluator.java           # AST 遍历求值器
│   ├── OperatorEvaluator.java      # 运算符求值（含溢出保护）
│   └── TypeCoercion.java           # 类型强制转换
│
├── context/                         # 上下文
│   ├── EvaluationContext.java      # 求值上下文接口
│   ├── StandardContext.java        # 标准上下文（最全功能）
│   ├── MapContext.java             # Map 包装上下文
│   ├── BeanContext.java            # Bean 包装上下文
│   └── ChainedContext.java         # 链式上下文（父子多层查找）
│
├── function/                        # 内置函数
│   ├── Function.java               # 函数接口
│   ├── FunctionRegistry.java       # 函数注册表（全局/局部）
│   ├── StringFunctions.java        # 字符串函数
│   ├── MathFunctions.java          # 数学函数
│   ├── CollectionFunctions.java    # 集合函数
│   ├── DateFunctions.java          # 日期函数
│   └── TypeFunctions.java          # 类型函数
│
├── compiler/                        # 编译优化
│   ├── ExpressionCompiler.java     # 表达式编译器（缓存+优化）
│   ├── CompiledExpression.java     # 编译后表达式
│   ├── CompiledExpressionCache.java # LRU 编译缓存
│   ├── ExpressionCache.java        # 表达式缓存
│   └── Optimizer.java              # AST 优化器（常量折叠/短路/死码消除）
│
├── sandbox/                         # 安全沙箱
│   ├── Sandbox.java                # 沙箱接口
│   ├── DefaultSandbox.java         # 默认沙箱（permissive/restrictive/standard）
│   ├── SecurityPolicy.java         # 安全策略 (record)
│   ├── AllowList.java              # 白名单（类/方法/属性）
│   └── SandboxException.java       # 沙箱违规异常
│
└── spi/                             # SPI 扩展
    ├── FunctionProvider.java       # 函数提供者 SPI
    ├── TypeConverter.java          # 类型转换器 SPI
    └── PropertyAccessor.java       # 属性访问器 SPI
```

---

## 3. 核心 API

### 3.1 OpenExpression - 门面入口

`OpenExpression` 是表达式引擎的主入口，提供快捷求值、解析、缓存管理、上下文创建、沙箱获取等功能。

```java
public final class OpenExpression {

    // ==================== 快捷求值 ====================

    /** 求值表达式（无上下文） */
    public static Object eval(String expression);

    /** 求值表达式（Map 变量） */
    public static Object eval(String expression, Map<String, Object> variables);

    /** 求值表达式（Bean 根对象） */
    public static Object eval(String expression, Object rootObject);

    /** 求值表达式（自定义上下文） */
    public static Object eval(String expression, EvaluationContext context);

    /** 求值表达式（带类型转换，无上下文） */
    public static <T> T eval(String expression, Class<T> targetType);

    /** 求值表达式（Map 变量 + 类型转换） */
    public static <T> T eval(String expression, Map<String, Object> variables, Class<T> targetType);

    /** 求值表达式（自定义上下文 + 类型转换） */
    public static <T> T eval(String expression, EvaluationContext context, Class<T> targetType);

    // ==================== 解析 ====================

    /** 获取默认解析器（缓存共享） */
    public static ExpressionParser parser();

    /** 创建新解析器实例 */
    public static ExpressionParser newParser();

    /** 解析表达式（使用缓存） */
    public static Expression parse(String expression);

    // ==================== 函数注册表 ====================

    /** 获取全局函数注册表 */
    public static FunctionRegistry functions();

    // ==================== 沙箱 ====================

    /** 获取标准沙箱 */
    public static Sandbox standardSandbox();

    /** 获取严格沙箱 */
    public static Sandbox restrictiveSandbox();

    /** 获取宽松沙箱 */
    public static Sandbox permissiveSandbox();

    // ==================== 上下文 ====================

    /** 创建空标准上下文 */
    public static StandardContext context();

    /** 创建带根对象的标准上下文 */
    public static StandardContext context(Object rootObject);

    /** 获取上下文构建器 */
    public static StandardContext.Builder contextBuilder();

    // ==================== 验证与缓存 ====================

    /** 验证表达式语法是否有效 */
    public static boolean isValid(String expression);

    /** 清除表达式缓存 */
    public static void clearCache();

    /** 获取缓存大小 */
    public static int cacheSize();
}
```

**使用示例：**

```java
// 简单求值
Object result = OpenExpression.eval("1 + 2 * 3");  // 7

// 变量求值
Object result = OpenExpression.eval("name + ' World'", Map.of("name", "Hello"));
// "Hello World"

// Bean 属性访问
Object result = OpenExpression.eval("user.name", myUser);

// 带类型转换
int sum = OpenExpression.eval("a + b", Map.of("a", 10, "b", 20), Integer.class);
// 30

// 预编译复用
ExpressionParser parser = OpenExpression.parser();
Expression expr = parser.parseExpression("price * quantity");

StandardContext ctx = StandardContext.builder()
    .rootObject(order1)
    .build();
Object result1 = expr.getValue(ctx);

ctx.setRootObject(order2);
Object result2 = expr.getValue(ctx);

// 验证表达式
boolean valid = OpenExpression.isValid("1 + 2");     // true
boolean invalid = OpenExpression.isValid("1 + + 2"); // false
```

### 3.2 Expression - 表达式接口

```java
public interface Expression {

    /** 获取原始表达式字符串 */
    String getExpressionString();

    /** 求值（无上下文） */
    Object getValue();

    /** 求值（自定义上下文） */
    Object getValue(EvaluationContext context);

    /** 求值（带类型转换） */
    <T> T getValue(Class<T> targetType);

    /** 求值（上下文 + 类型转换） */
    <T> T getValue(EvaluationContext context, Class<T> targetType);

    /** 求值（Bean 根对象） */
    Object getValue(Object rootObject);

    /** 求值（Bean 根对象 + 类型转换） */
    <T> T getValue(Object rootObject, Class<T> targetType);

    /** 获取返回类型 */
    Class<?> getValueType();

    /** 获取返回类型（自定义上下文） */
    Class<?> getValueType(EvaluationContext context);
}
```

### 3.3 ExpressionParser - 解析器接口

```java
public interface ExpressionParser {

    /** 解析表达式字符串为 Expression 对象 */
    Expression parseExpression(String expressionString);
}
```

### 3.4 EvaluationContext - 求值上下文

```java
public interface EvaluationContext {

    /** 获取根对象 */
    Object getRootObject();

    /** 设置根对象 */
    void setRootObject(Object root);

    /** 获取变量值 */
    Object getVariable(String name);

    /** 设置变量 */
    void setVariable(String name, Object value);

    /** 检查变量是否存在 */
    boolean hasVariable(String name);

    /** 获取所有变量 */
    Map<String, Object> getVariables();

    /** 获取函数注册表 */
    FunctionRegistry getFunctionRegistry();

    /** 获取属性访问器列表 */
    List<PropertyAccessor> getPropertyAccessors();

    /** 获取类型转换器 */
    TypeConverter getTypeConverter();

    /** 获取安全沙箱 */
    Sandbox getSandbox();

    /** 创建子上下文 */
    EvaluationContext createChild();
}
```

---

## 4. 上下文实现

### 4.1 StandardContext - 标准上下文

功能最完整的上下文实现，支持根对象、变量、自定义属性访问器、类型转换器、沙箱。

```java
public class StandardContext implements EvaluationContext {

    public StandardContext();
    public StandardContext(Object rootObject);
    public StandardContext(Object rootObject, FunctionRegistry functionRegistry,
                          List<PropertyAccessor> propertyAccessors, Sandbox sandbox);

    public StandardContext addPropertyAccessor(PropertyAccessor accessor);

    public static Builder builder();

    public static class Builder {
        public Builder rootObject(Object rootObject);
        public Builder functionRegistry(FunctionRegistry registry);
        public Builder addPropertyAccessor(PropertyAccessor accessor);
        public Builder typeConverter(TypeConverter converter);
        public Builder sandbox(Sandbox sandbox);
        public StandardContext build();
    }
}
```

**使用示例：**

```java
StandardContext ctx = StandardContext.builder()
    .rootObject(myBean)
    .sandbox(OpenExpression.standardSandbox())
    .build();

ctx.setVariable("threshold", 100);
Object result = OpenExpression.eval("score > threshold", ctx);
```

### 4.2 MapContext - Map 上下文

以 Map 作为变量来源的轻量级上下文。

```java
public class MapContext implements EvaluationContext {

    public MapContext();
    public MapContext(Map<String, Object> variables);
    public MapContext(Map<String, Object> variables,
                     FunctionRegistry functionRegistry, Sandbox sandbox);

    public static MapContext of(Map<String, Object> map);
    public static Builder builder();

    public static class Builder {
        public Builder variable(String name, Object value);
        public Builder variables(Map<String, Object> vars);
        public Builder functionRegistry(FunctionRegistry registry);
        public Builder sandbox(Sandbox sandbox);
        public MapContext build();
    }
}
```

### 4.3 BeanContext - Bean 上下文

以 Java Bean 作为根对象，Bean 属性可直接在表达式中访问。

```java
public class BeanContext implements EvaluationContext {

    public BeanContext(Object rootObject);
    public BeanContext(Object rootObject, FunctionRegistry functionRegistry, Sandbox sandbox);

    public static BeanContext of(Object bean);
    public static Builder builder();

    public static class Builder {
        public Builder rootObject(Object root);
        public Builder functionRegistry(FunctionRegistry registry);
        public Builder sandbox(Sandbox sandbox);
        public Builder variable(String name, Object value);
        public BeanContext build();
    }
}
```

**使用示例：**

```java
User user = new User("John", 30);
BeanContext ctx = BeanContext.of(user);
Object name = OpenExpression.eval("name", ctx);  // "John"
Object age = OpenExpression.eval("age", ctx);    // 30
```

### 4.4 ChainedContext - 链式上下文

支持父子层级，变量查找从子上下文开始向父上下文逐层查找。

```java
public class ChainedContext implements EvaluationContext {

    public ChainedContext(EvaluationContext parent);
    public ChainedContext(EvaluationContext parent, Object rootObject);

    public EvaluationContext getParent();
    public int getDepth();
    public Map<String, Object> getLocalVariables();

    public static ChainedContext of(EvaluationContext parent);
    public static ChainedContext of(EvaluationContext parent, Object rootObject);

    public static Builder builder(EvaluationContext parent);

    public static class Builder {
        public Builder(EvaluationContext parent);
        public Builder rootObject(Object root);
        public Builder variable(String name, Object value);
        public ChainedContext build();
    }
}
```

**使用示例：**

```java
EvaluationContext parent = new StandardContext();
parent.setVariable("x", 10);

ChainedContext child = ChainedContext.of(parent);
child.setVariable("y", 20);

Object result = OpenExpression.eval("x + y", child);  // 30
```

---

## 5. AST 节点

所有 AST 节点均为 `record` 类型，由 `sealed interface Node` 统一约束。

### 5.1 Node - 节点接口

```java
public sealed interface Node permits
    LiteralNode, IdentifierNode, BinaryOpNode, UnaryOpNode, TernaryOpNode,
    PropertyAccessNode, IndexAccessNode, MethodCallNode, FunctionCallNode,
    ListLiteralNode, CollectionFilterNode, CollectionProjectNode {

    Object evaluate(EvaluationContext context);
    String toExpressionString();
}
```

### 5.2 节点类型一览

| 节点类型 | 说明 | 表达式示例 |
|---------|------|-----------|
| `LiteralNode` | 字面量 | `42`, `"hello"`, `true`, `null` |
| `IdentifierNode` | 标识符/变量 | `name`, `count` |
| `BinaryOpNode` | 二元运算 | `a + b`, `x > 0`, `a && b` |
| `UnaryOpNode` | 一元运算 | `!flag`, `-value` |
| `TernaryOpNode` | 三元运算 | `x > 0 ? "positive" : "negative"` |
| `PropertyAccessNode` | 属性访问 | `user.name`, `user?.address` |
| `IndexAccessNode` | 索引访问 | `list[0]`, `map["key"]` |
| `MethodCallNode` | 方法调用 | `str.toUpperCase()`, `str?.trim()` |
| `FunctionCallNode` | 函数调用 | `upper(name)`, `abs(-10)` |
| `ListLiteralNode` | 列表字面量 | `[1, 2, 3]` |
| `CollectionFilterNode` | 集合过滤 | `users.?[age>18]`, `users.^[active]`, `users.$[active]` |
| `CollectionProjectNode` | 集合投影 | `users.![name]` |

### 5.3 关键节点详解

```java
// 二元运算：支持 +, -, *, /, %, **, ==, !=, >, <, >=, <=, &&, ||, matches
public record BinaryOpNode(String operator, Node left, Node right) implements Node {
    public static BinaryOpNode of(Node left, String operator, Node right);
}

// 属性访问：支持空安全 ?.
public record PropertyAccessNode(Node target, String property, boolean nullSafe) implements Node {
    public static PropertyAccessNode of(Node target, String property);
    public static PropertyAccessNode nullSafe(Node target, String property);
}

// 索引访问：支持空安全 ?[]
public record IndexAccessNode(Node target, Node index, boolean nullSafe) implements Node {
    public static IndexAccessNode of(Node target, Node index);
    public static IndexAccessNode nullSafe(Node target, Node index);
}

// 方法调用：支持空安全 ?.method()
public record MethodCallNode(Node target, String methodName,
                             List<Node> arguments, boolean nullSafe) implements Node {
    public static MethodCallNode of(Node target, String methodName, List<Node> arguments);
    public static MethodCallNode nullSafe(Node target, String methodName, List<Node> arguments);
}

// 集合过滤：支持 all(.?[])、first(.^[])、last(.$[]) 三种模式
public record CollectionFilterNode(Node target, Node predicate, FilterMode mode) implements Node {
    public enum FilterMode { ALL, FIRST, LAST }
    public static CollectionFilterNode all(Node target, Node predicate);
    public static CollectionFilterNode first(Node target, Node predicate);
    public static CollectionFilterNode last(Node target, Node predicate);
}
```

---

## 6. 求值器

### 6.1 AstEvaluator - AST 求值器

```java
public class AstEvaluator implements Evaluator {

    public static AstEvaluator getInstance();

    /** 求值 AST 节点 */
    public Object evaluate(Node node, EvaluationContext context);

    /** 批量求值 */
    public List<Object> evaluateAll(List<Node> nodes, EvaluationContext context);

    /** 求值为布尔值 */
    public boolean evaluateAsBoolean(Node node, EvaluationContext context);

    /** 求值为数字 */
    public Number evaluateAsNumber(Node node, EvaluationContext context);

    /** 求值为字符串 */
    public String evaluateAsString(Node node, EvaluationContext context);

    /** 带超时求值 */
    public Object evaluateWithTimeout(Node node, EvaluationContext context, long timeoutMs);

    /** 静态便捷方法 */
    public static Object eval(Node node, EvaluationContext context);
}
```

### 6.2 OperatorEvaluator - 运算符求值

提供所有二元和一元运算符的求值实现，包含整数溢出保护（使用 `Math.addExact` 等）。

```java
public final class OperatorEvaluator {

    // 分发方法
    public static Object evaluateBinary(String operator, Object left, Object right);
    public static Object evaluateUnary(String operator, Object operand);

    // 算术运算（含溢出保护）
    public static Object add(Object left, Object right);       // 字符串拼接或数值加
    public static Object subtract(Object left, Object right);
    public static Object multiply(Object left, Object right);
    public static Object divide(Object left, Object right);
    public static Object modulo(Object left, Object right);
    public static Object power(Object left, Object right);

    // 比较运算
    public static boolean equals(Object left, Object right);
    public static boolean notEquals(Object left, Object right);
    public static boolean lessThan(Object left, Object right);
    public static boolean lessThanOrEqual(Object left, Object right);
    public static boolean greaterThan(Object left, Object right);
    public static boolean greaterThanOrEqual(Object left, Object right);

    // 逻辑运算
    public static boolean and(Object left, Object right);
    public static boolean or(Object left, Object right);
    public static boolean not(Object operand);

    // 其他
    public static Object negate(Object operand);
    public static boolean matches(Object left, Object right);   // 正则匹配
}
```

### 6.3 TypeCoercion - 类型强制转换

```java
public final class TypeCoercion {

    public static boolean toBoolean(Object value);
    public static int toInt(Object value);
    public static long toLong(Object value);
    public static double toDouble(Object value);
    public static String toString(Object value);

    public static <T> T convert(Object value, Class<T> targetType);
    public static boolean canConvert(Object value, Class<?> targetType);
}
```

---

## 7. 编译与优化

### 7.1 ExpressionCompiler - 表达式编译器

支持缓存和 AST 优化，可通过 Builder 自定义配置。

```java
public class ExpressionCompiler {

    public ExpressionCompiler();
    public ExpressionCompiler(ExpressionCache cache, Optimizer optimizer, boolean optimizationEnabled);

    // 静态工厂
    public static ExpressionCompiler getDefault();
    public static ExpressionCompiler create();
    public static ExpressionCompiler withoutCache();
    public static ExpressionCompiler withoutOptimization();

    // 编译
    public CompiledExpression compile(String expression);
    public CompiledExpression compileWithoutCache(String expression);
    public CompiledExpression compile(String expression, boolean optimize);

    // 缓存管理
    public boolean isCached(String expression);
    public ExpressionCache getCache();
    public Optimizer getOptimizer();
    public boolean isOptimizationEnabled();
    public void clearCache();

    // Builder
    public static Builder builder();

    public static class Builder {
        public Builder cache(ExpressionCache cache);
        public Builder noCache();
        public Builder cacheSize(int maxSize);
        public Builder optimizer(Optimizer optimizer);
        public Builder optimization(boolean enabled);
        public Builder noOptimization();
        public ExpressionCompiler build();
    }
}
```

### 7.2 CompiledExpression - 编译后表达式

```java
public final class CompiledExpression implements Expression {

    public static CompiledExpression compile(String expression);

    public String getExpressionString();
    public Object getValue();
    public Object getValue(EvaluationContext context);
    public <T> T getValue(Class<T> targetType);
    public <T> T getValue(EvaluationContext context, Class<T> targetType);
    public Object getValue(Object rootObject);
    public <T> T getValue(Object rootObject, Class<T> targetType);
    public Class<?> getValueType();
    public Class<?> getValueType(EvaluationContext context);
    public Node getAst();    // 获取底层 AST
}
```

### 7.3 CompiledExpressionCache - LRU 编译缓存

```java
public class CompiledExpressionCache {

    public CompiledExpressionCache();             // 默认容量
    public CompiledExpressionCache(int maxSize);

    public static CompiledExpressionCache global();
    public static CompiledExpressionCache create(int maxSize);

    public CompiledExpression getOrCompile(String expression,
            java.util.function.Function<String, CompiledExpression> compiler);
    public CompiledExpression get(String expression);
    public void put(String expression, CompiledExpression compiled);
    public boolean contains(String expression);
    public void remove(String expression);
    public void clear();
    public int size();
    public int maxSize();

    public CacheStats getStats();

    public record CacheStats(int size, int maxSize) {
        public double utilization();
    }
}
```

### 7.4 Optimizer - AST 优化器

```java
public class Optimizer {

    public Optimizer();

    /** 执行所有启用的优化 */
    public Node optimize(Node node);

    /** 常量折叠：编译期可确定的表达式直接计算为字面量 */
    public Node foldConstants(Node node);

    /** 短路优化：优化 && / || 的短路求值 */
    public Node optimizeShortCircuit(Node node);

    // 开关控制
    public boolean isConstantFoldingEnabled();
    public Optimizer setConstantFoldingEnabled(boolean enabled);
    public boolean isShortCircuitEnabled();
    public Optimizer setShortCircuitEnabled(boolean enabled);
    public boolean isDeadCodeEliminationEnabled();
    public Optimizer setDeadCodeEliminationEnabled(boolean enabled);

    public static Builder builder();

    public static class Builder {
        public Builder constantFolding(boolean enabled);
        public Builder shortCircuit(boolean enabled);
        public Builder deadCodeElimination(boolean enabled);
        public Builder noOptimizations();
        public Optimizer build();
    }
}
```

**优化示例：**

```java
// 常量折叠：1 + 2 * 3 在编译期直接计算为 7
CompiledExpression expr = CompiledExpression.compile("1 + 2 * 3");

// 短路优化：false && expr 直接返回 false，不求值 expr
CompiledExpression shortCircuit = CompiledExpression.compile("false && expensiveCall()");
```

---

## 8. 内置函数

### 8.1 FunctionRegistry - 函数注册表

```java
public class FunctionRegistry {

    /** 获取全局注册表（含所有内置函数） */
    public static FunctionRegistry getGlobal();

    /** 创建新注册表（含内置函数） */
    public static FunctionRegistry create();

    /** 创建空注册表 */
    public static FunctionRegistry empty();

    public FunctionRegistry register(String name, Function function);
    public FunctionRegistry registerAll(Map<String, Function> funcs);
    public FunctionRegistry unregister(String name);
    public Function get(String name);
    public boolean has(String name);
    public Set<String> getNames();
    public int size();
    public void clear();
}
```

### 8.2 内置函数分类

**StringFunctions - 字符串函数**

通过 `StringFunctions.getFunctions()` 获取函数映射，包含：upper、lower、trim、length、substring、contains、replace、matches、split、concat、format、isEmpty、isNotEmpty、isBlank、defaultIfEmpty 等。

**MathFunctions - 数学函数**

通过 `MathFunctions.getFunctions()` 获取函数映射，包含：abs、round、ceil、floor、max、min、pow、sqrt、random 等。

**CollectionFunctions - 集合函数**

通过 `CollectionFunctions.getFunctions()` 获取函数映射，包含：size、isEmpty、contains、first、last、sum、avg、sort、reverse、distinct、join 等。

**DateFunctions - 日期函数**

通过 `DateFunctions.getFunctions()` 获取函数映射，包含：now、today、parseDate、parseDateTime、formatDate、plusDays、plusHours、daysBetween、year、month、dayOfMonth 等。

**TypeFunctions - 类型函数**

通过 `TypeFunctions.getFunctions()` 获取函数映射，包含类型检查和转换相关函数。

**函数调用示例：**

```java
// 字符串函数
OpenExpression.eval("upper('hello')");                        // "HELLO"
OpenExpression.eval("trim('  hello  ')");                     // "hello"
OpenExpression.eval("length('hello')");                       // 5

// 数学函数
OpenExpression.eval("abs(-10)");                              // 10
OpenExpression.eval("round(3.7)");                            // 4
OpenExpression.eval("max(10, 20)");                           // 20

// 集合函数
OpenExpression.eval("size(list)", Map.of("list", List.of(1,2,3)));  // 3
OpenExpression.eval("sum(nums)", Map.of("nums", List.of(1,2,3)));   // 6.0
OpenExpression.eval("join(names, ', ')",
    Map.of("names", List.of("a","b","c")));                         // "a, b, c"

// 日期函数
OpenExpression.eval("year(today())");                         // 2026
```

---

## 9. 安全沙箱

### 9.1 Sandbox - 沙箱接口

```java
public interface Sandbox {

    /** 检查类是否允许访问 */
    boolean isClassAllowed(Class<?> clazz);

    /** 检查方法是否允许调用 */
    boolean isMethodAllowed(Object target, Method method);

    /** 检查属性是否允许访问 */
    boolean isPropertyAllowed(Object target, String property);

    /** 获取最大表达式长度 */
    int getMaxExpressionLength();

    /** 获取最大求值深度 */
    int getMaxEvaluationDepth();

    /** 获取最大求值时间（毫秒） */
    long getMaxEvaluationTime();
}
```

### 9.2 DefaultSandbox - 默认沙箱

提供三种预设安全级别。

```java
public class DefaultSandbox implements Sandbox {

    /** 宽松模式：允许大部分操作 */
    public static DefaultSandbox permissive();

    /** 严格模式：仅允许白名单操作 */
    public static DefaultSandbox restrictive();

    /** 标准模式：平衡安全与便利 */
    public static DefaultSandbox standard();

    public static Builder builder();

    public static class Builder {
        public Builder addAllowedClass(String className);
        public Builder addAllowedClass(Class<?> clazz);
        public Builder addDeniedClass(String className);
        public Builder addDeniedClass(Class<?> clazz);
        public Builder addAllowedPackage(String packageName);
        public Builder addDeniedPackage(String packageName);
        public Builder addAllowedMethod(String methodName);
        public Builder addDeniedMethod(String methodName);
        public Builder maxExpressionLength(int length);
        public Builder maxEvaluationDepth(int depth);
        public Builder maxEvaluationTime(long timeMs);
        public Builder allowAllByDefault(boolean allow);
        public DefaultSandbox build();
    }
}
```

### 9.3 SecurityPolicy - 安全策略

```java
public record SecurityPolicy(
    Set<Class<?>> allowedClasses,
    Set<String> deniedClasses,
    Set<String> allowedMethods,
    Set<String> deniedMethods,
    Set<String> allowedFunctions,
    Set<String> deniedFunctions,
    long timeout,
    int maxIterations,
    int maxExpressionLength
) {
    /** 严格策略 */
    public static SecurityPolicy strict();

    /** 宽松策略 */
    public static SecurityPolicy lenient();

    public static Builder builder();

    public boolean isClassAllowed(Class<?> clazz);
    public boolean isMethodAllowed(String methodName);
    public boolean isFunctionAllowed(String functionName);

    public static class Builder {
        public Builder allowClass(Class<?>... classes);
        public Builder denyClass(String... classNames);
        public Builder allowMethod(String... methods);
        public Builder denyMethod(String... methods);
        public Builder allowFunction(String... functions);
        public Builder denyFunction(String... functions);
        public Builder timeout(long millis);
        public Builder maxIterations(int max);
        public Builder maxExpressionLength(int max);
        public SecurityPolicy build();
    }
}
```

### 9.4 AllowList - 白名单

```java
public class AllowList {

    public boolean isClassAllowed(String className);
    public boolean isClassAllowed(Class<?> clazz);
    public boolean isMethodAllowed(String methodName);
    public boolean isPropertyAllowed(String propertyName);

    public Set<String> getAllowedClasses();
    public Set<String> getAllowedMethods();
    public Set<String> getAllowedProperties();

    public static AllowList empty();
    public static AllowList allowAll();
    public static Builder builder();

    public static class Builder {
        public Builder allowClass(String className);
        public Builder allowClass(Class<?> clazz);
        public Builder allowClasses(String... classNames);
        public Builder allowMethod(String methodName);
        public Builder allowMethods(String... methodNames);
        public Builder allowProperty(String propertyName);
        public Builder allowProperties(String... propertyNames);
        public Builder allowAllByDefault(boolean allow);
        public AllowList build();
    }
}
```

### 9.5 SandboxException - 沙箱异常

```java
public class SandboxException extends OpenExpressionException {

    public enum ViolationType {
        CLASS_NOT_ALLOWED, METHOD_NOT_ALLOWED, PROPERTY_NOT_ALLOWED,
        FUNCTION_NOT_ALLOWED, TIMEOUT, ITERATION_LIMIT, EXPRESSION_TOO_LONG,
        DEPTH_LIMIT
    }

    public ViolationType getViolationType();
    public String getViolatedResource();

    // 工厂方法
    public static SandboxException classNotAllowed(String className);
    public static SandboxException classNotAllowed(Class<?> clazz);
    public static SandboxException methodNotAllowed(String methodName);
    public static SandboxException methodNotAllowed(String className, String methodName);
    public static SandboxException propertyNotAllowed(String propertyName);
    public static SandboxException functionNotAllowed(String functionName);
    public static SandboxException timeout(long timeoutMs);
    public static SandboxException iterationLimitExceeded(int maxIterations);
    public static SandboxException expressionTooLong(int maxLength, int actualLength);
    public static SandboxException depthLimitExceeded(int maxDepth);
}
```

**沙箱使用示例：**

```java
// 创建严格沙箱
Sandbox sandbox = DefaultSandbox.builder()
    .addAllowedClass(String.class)
    .addAllowedClass(Integer.class)
    .addAllowedClass(List.class)
    .addDeniedMethod("getClass")
    .addDeniedMethod("wait")
    .addDeniedMethod("notify")
    .maxExpressionLength(10000)
    .maxEvaluationDepth(100)
    .maxEvaluationTime(5000)
    .build();

// 使用沙箱上下文执行不可信表达式
StandardContext ctx = StandardContext.builder()
    .sandbox(sandbox)
    .build();
ctx.setVariable("data", untrustedData);

try {
    Object result = OpenExpression.eval(untrustedExpression, ctx);
} catch (SandboxException e) {
    System.err.println("安全违规: " + e.getViolationType());
}
```

---

## 10. 解析器

### 10.1 Tokenizer - 词法分析器

```java
public class Tokenizer {

    public Tokenizer(String expression);

    /** 将表达式字符串分解为 Token 列表 */
    public List<Token> tokenize();

    /** 静态便捷方法 */
    public static List<Token> tokenize(String expression);
}
```

### 10.2 Token - 词法单元

```java
public record Token(TokenType type, Object value, int position, int length) {

    public static Token of(TokenType type, int position);
    public static Token of(TokenType type, int position, int length);
    public static Token of(TokenType type, Object value, int position, int length);

    public boolean is(TokenType type);
    public boolean isAny(TokenType... types);
    public String stringValue();
    public Number numberValue();
}
```

### 10.3 Parser - 语法分析器

```java
public class Parser {

    public Parser(List<Token> tokens);

    /** 解析为 AST */
    public Node parse();

    /** 静态便捷方法：字符串 -> AST */
    public static Node parse(String expression);
}
```

### 10.4 ParserException - 解析异常

```java
public class ParserException extends OpenExpressionException {

    public enum ErrorType {
        UNEXPECTED_TOKEN, EXPECTED_TOKEN, UNEXPECTED_END,
        INVALID_NUMBER, UNTERMINATED_STRING, INVALID_ESCAPE,
        UNBALANCED_PARENTHESES
    }

    public String getExpression();
    public int getLine();
    public int getColumn();
    public ErrorType getErrorType();

    // 工厂方法
    public static ParserException unexpectedToken(String token, int position);
    public static ParserException unexpectedToken(String token, String expression, int position);
    public static ParserException expectedToken(String expected, String actual, int position);
    public static ParserException unexpectedEnd(String expression);
    public static ParserException invalidNumber(String value, int position);
    public static ParserException unterminatedString(int position);
    public static ParserException invalidEscapeSequence(String sequence, int position);
    public static ParserException unbalancedParentheses(int position);
}
```

---

## 11. 异常处理

```java
public class OpenExpressionException extends OpenException {

    public OpenExpressionException(String message);
    public OpenExpressionException(String message, Throwable cause);
    public OpenExpressionException(String message, String expression, int position);

    public String getExpression();
    public int getPosition();

    // 工厂方法
    public static OpenExpressionException parseError(String message, String expression, int position);
    public static OpenExpressionException parseError(String message, int position);
    public static OpenExpressionException evaluationError(String message, Throwable cause);
    public static OpenExpressionException evaluationError(String message);
    public static OpenExpressionException typeError(String expected, Object actual);
    public static OpenExpressionException propertyNotFound(String property, Class<?> type);
    public static OpenExpressionException methodNotFound(String method, Class<?> type);
    public static OpenExpressionException functionNotFound(String function);
    public static OpenExpressionException securityViolation(String message);
    public static OpenExpressionException timeout(long millis);
    public static OpenExpressionException divisionByZero();
    public static OpenExpressionException nullPointer(String context);
}
```

---

## 12. SPI 扩展点

### 12.1 FunctionProvider - 函数提供者

通过 SPI 机制扩展内置函数。

```java
public interface FunctionProvider {

    /** 获取提供的函数映射 */
    Map<String, Function> getFunctions();

    /** 获取优先级（数字越小优先级越高） */
    default int getPriority() { return 100; }
}
```

注册方式：在 `META-INF/services/cloud.opencode.base.expression.spi.FunctionProvider` 文件中声明实现类。

### 12.2 PropertyAccessor - 属性访问器

自定义属性读写逻辑。

```java
public interface PropertyAccessor {

    /** 获取可处理的目标类型 */
    Class<?>[] getSpecificTargetClasses();

    /** 检查是否可读指定属性 */
    boolean canRead(Object target, String name);

    /** 读取属性值 */
    Object read(Object target, String name);

    /** 检查是否可写（默认 false） */
    default boolean canWrite(Object target, String name) { return false; }

    /** 写入属性值 */
    default void write(Object target, String name, Object value) {
        throw new UnsupportedOperationException();
    }
}
```

### 12.3 TypeConverter - 类型转换器

自定义类型转换逻辑。

```java
public interface TypeConverter {

    /** 检查是否可转换 */
    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    /** 执行转换 */
    <T> T convert(Object source, Class<T> targetType);
}
```

---

## 13. 表达式语法规范

### 13.1 字面量

| 类型 | 语法 | 示例 |
|------|------|------|
| 整数 | 数字 | `123`, `-456`, `0` |
| 浮点数 | 数字.数字 | `3.14`, `-0.5` |
| 字符串 | '...' 或 "..." | `'hello'`, `"world"` |
| 布尔 | true / false | `true`, `false` |
| 空值 | null | `null` |
| 列表 | [a, b, c] | `[1, 2, 3]` |

### 13.2 运算符优先级

| 优先级 | 运算符 | 结合性 | 说明 |
|--------|--------|--------|------|
| 1 | `()` `[]` `.` `?.` | 左 | 分组、索引、属性访问 |
| 2 | `!` `-`(一元) | 右 | 逻辑非、负号 |
| 3 | `**` | 右 | 幂运算 |
| 4 | `*` `/` `%` | 左 | 乘、除、取模 |
| 5 | `+` `-` | 左 | 加、减 |
| 6 | `>` `>=` `<` `<=` | 左 | 比较 |
| 7 | `==` `!=` | 左 | 相等性 |
| 8 | `matches` | 左 | 正则匹配 |
| 9 | `&&` | 左 | 逻辑与 |
| 10 | `\|\|` | 左 | 逻辑或 |
| 11 | `?:` | 右 | 三元运算 |

### 13.3 属性与索引访问

```java
// 点号访问
user.name
user.address.city

// 空安全访问
user?.address?.city  // 如果任一为 null，返回 null

// 索引访问
list[0]
map['key']
map["key"]

// 空安全索引
list?[0]

// 组合
users[0].name
config['database'].host
```

### 13.4 方法调用

```java
// 无参方法
str.toUpperCase()
list.size()

// 带参方法
str.substring(0, 5)

// 链式调用
str.trim().toUpperCase().substring(0, 5)

// 空安全调用
str?.toUpperCase()
```

### 13.5 集合操作

```java
// 过滤（Selection）
users.?[age > 18]           // 所有满足条件的
users.^[age > 18]           // 第一个满足条件的
users.$[age > 18]           // 最后一个满足条件的

// 投影（Projection）
users.![name]               // 所有用户的 name 列表

// 组合
users.?[age > 18].![name]   // 年龄大于18的用户名字列表
```

---

## 14. 使用示例

### 14.1 基础运算

```java
// 算术
OpenExpression.eval("1 + 2 * 3");           // 7
OpenExpression.eval("2 ** 10");             // 1024
OpenExpression.eval("10 % 3");              // 1

// 字符串拼接
OpenExpression.eval("'Hello' + ' ' + 'World'"); // "Hello World"

// 比较
OpenExpression.eval("10 > 5");              // true
OpenExpression.eval("'abc' == 'abc'");      // true
```

### 14.2 变量与 Bean 访问

```java
// Map 变量
Map<String, Object> vars = Map.of("name", "Alice", "age", 25);
String greeting = OpenExpression.eval(
    "name + ' is ' + age + ' years old'", vars, String.class);
// "Alice is 25 years old"

// Bean 属性
User user = new User("Bob", 30, new Address("Shanghai"));
String city = OpenExpression.eval("address.city", user, String.class);
// "Shanghai"

// 空安全
User noAddr = new User("Charlie", 25, null);
Object safeCity = OpenExpression.eval("address?.city", noAddr);
// null (不抛 NullPointerException)
```

### 14.3 条件表达式

```java
// 三元运算
String status = OpenExpression.eval(
    "age >= 18 ? 'Adult' : 'Minor'",
    Map.of("age", 20),
    String.class);
// "Adult"

// 逻辑组合
boolean eligible = OpenExpression.eval(
    "age >= 18 && status == 'ACTIVE' && !banned",
    Map.of("age", 25, "status", "ACTIVE", "banned", false),
    Boolean.class);
// true
```

### 14.4 集合操作

```java
List<Map<String, Object>> users = List.of(
    Map.of("name", "Alice", "age", 25, "active", true),
    Map.of("name", "Bob", "age", 17, "active", true),
    Map.of("name", "Charlie", "age", 30, "active", false)
);

// 过滤成年用户
List<?> adults = OpenExpression.eval(
    "users.?[age >= 18]",
    Map.of("users", users),
    List.class);
// [Alice(25), Charlie(30)]

// 获取活跃用户名字
List<?> activeNames = OpenExpression.eval(
    "users.?[active == true].![name]",
    Map.of("users", users),
    List.class);
// ["Alice", "Bob"]
```

### 14.5 自定义函数注册

```java
// 注册函数到全局注册表
FunctionRegistry registry = OpenExpression.functions();
registry.register("mask", args -> {
    String str = (String) args[0];
    int keep = args.length > 1 ? ((Number) args[1]).intValue() : 4;
    if (str == null || str.length() <= keep) return str;
    return "*".repeat(str.length() - keep) + str.substring(str.length() - keep);
});

// 使用
String masked = OpenExpression.eval(
    "mask(phone, 4)",
    Map.of("phone", "13812345678"),
    String.class);
// "*******5678"
```

### 14.6 预编译与缓存

```java
// 编译器配置
ExpressionCompiler compiler = ExpressionCompiler.builder()
    .cacheSize(5000)
    .optimization(true)
    .build();

// 编译
CompiledExpression expr = compiler.compile("price * quantity * (1 - discount)");

// 复用（无需重新解析）
MapContext ctx1 = MapContext.of(Map.of("price", 100, "quantity", 5, "discount", 0.1));
Object result1 = expr.getValue(ctx1);  // 450.0

MapContext ctx2 = MapContext.of(Map.of("price", 200, "quantity", 3, "discount", 0.2));
Object result2 = expr.getValue(ctx2);  // 480.0
```

---

*文档更新日期：2026-02-27*
