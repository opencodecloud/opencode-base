/**
 * OpenCode Base Expression Module
 * OpenCode 基础表达式引擎模块
 *
 * <p>Provides a lightweight, type-safe expression engine with comprehensive operators,
 * property access, method calls, collection operations, and sandbox execution.</p>
 * <p>提供轻量级、类型安全的表达式引擎，支持完整的运算符、属性访问、方法调用、集合操作和沙箱执行。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>OpenExpression - Expression engine facade - 表达式引擎门面</li>
 *   <li>Arithmetic operators (+, -, *, /, %, **) - 算术运算符</li>
 *   <li>Comparison operators ({@code ==, !=, >, <, >=, <=}) - 比较运算符</li>
 *   <li>Logical operators ({@code &&, ||, !}) - 逻辑运算符</li>
 *   <li>Ternary operator (? :) - 三元运算符</li>
 *   <li>Property access (user.name, list[0]) - 属性访问</li>
 *   <li>Null-safe navigation (user?.address?.city) - 空安全导航</li>
 *   <li>Method calls (str.toUpperCase()) - 方法调用</li>
 *   <li>Collection filtering (users.?[age>18]) - 集合过滤</li>
 *   <li>Built-in functions (string, math, collection, date) - 内置函数</li>
 *   <li>Sandbox execution with security policies - 沙箱执行与安全策略</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-expression V1.0.0
 */
module cloud.opencode.base.expression {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Optional dependency for enhanced property access
    requires static cloud.opencode.base.reflect;

    // Export public API packages
    exports cloud.opencode.base.expression;
    exports cloud.opencode.base.expression.ast;
    exports cloud.opencode.base.expression.parser;
    exports cloud.opencode.base.expression.eval;
    exports cloud.opencode.base.expression.context;
    exports cloud.opencode.base.expression.function;
    exports cloud.opencode.base.expression.sandbox;
    exports cloud.opencode.base.expression.compiler;
    exports cloud.opencode.base.expression.spi;

    // SPI service loading
    uses cloud.opencode.base.expression.spi.FunctionProvider;
    uses cloud.opencode.base.expression.spi.PropertyAccessor;
    uses cloud.opencode.base.expression.spi.TypeConverter;
}
