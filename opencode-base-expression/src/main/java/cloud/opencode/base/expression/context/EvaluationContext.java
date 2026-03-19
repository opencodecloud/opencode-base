package cloud.opencode.base.expression.context;

import cloud.opencode.base.expression.function.FunctionRegistry;
import cloud.opencode.base.expression.sandbox.Sandbox;
import cloud.opencode.base.expression.spi.PropertyAccessor;
import cloud.opencode.base.expression.spi.TypeConverter;

import java.util.List;
import java.util.Map;

/**
 * Evaluation Context Interface
 * 求值上下文接口
 *
 * <p>Provides the runtime environment for expression evaluation including
 * variables, functions, property accessors, and security sandbox.</p>
 * <p>提供表达式求值的运行时环境，包括变量、函数、属性访问器和安全沙箱。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Variable management (get, set, check) - 变量管理（获取、设置、检查）</li>
 *   <li>Root object binding for property access - 根对象绑定用于属性访问</li>
 *   <li>Function registry for built-in and custom functions - 函数注册表，支持内置和自定义函数</li>
 *   <li>Property accessor SPI for custom types - 属性访问器SPI用于自定义类型</li>
 *   <li>Security sandbox integration - 安全沙箱集成</li>
 *   <li>Child context creation for scoped evaluation - 子上下文创建用于作用域求值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EvaluationContext ctx = new StandardContext();
 * ctx.setVariable("x", 10);
 * ctx.setVariable("y", 20);
 * Object result = OpenExpression.eval("x + y", ctx);  // 30
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public interface EvaluationContext {

    /**
     * Get the root object
     * 获取根对象
     *
     * @return the root object | 根对象
     */
    Object getRootObject();

    /**
     * Set the root object
     * 设置根对象
     *
     * @param root the root object | 根对象
     */
    void setRootObject(Object root);

    /**
     * Get variable value
     * 获取变量值
     *
     * @param name the variable name | 变量名
     * @return the variable value | 变量值
     */
    Object getVariable(String name);

    /**
     * Set variable value
     * 设置变量值
     *
     * @param name the variable name | 变量名
     * @param value the variable value | 变量值
     */
    void setVariable(String name, Object value);

    /**
     * Check if variable exists
     * 检查变量是否存在
     *
     * @param name the variable name | 变量名
     * @return true if exists | 如果存在返回true
     */
    boolean hasVariable(String name);

    /**
     * Get all variables
     * 获取所有变量
     *
     * @return the variable map | 变量映射
     */
    Map<String, Object> getVariables();

    /**
     * Get function registry
     * 获取函数注册表
     *
     * @return the function registry | 函数注册表
     */
    FunctionRegistry getFunctionRegistry();

    /**
     * Get property accessors
     * 获取属性访问器
     *
     * @return the property accessor list | 属性访问器列表
     */
    List<PropertyAccessor> getPropertyAccessors();

    /**
     * Get type converter
     * 获取类型转换器
     *
     * @return the type converter | 类型转换器
     */
    TypeConverter getTypeConverter();

    /**
     * Get security sandbox
     * 获取安全沙箱
     *
     * @return the sandbox, or null if not configured | 沙箱，如果未配置则返回null
     */
    Sandbox getSandbox();

    /**
     * Create child context
     * 创建子上下文
     *
     * <p>Creates a child context that inherits from this context but can have
     * its own variable bindings. Used for collection operations.</p>
     * <p>创建继承自此上下文但可以有自己变量绑定的子上下文。用于集合操作。</p>
     *
     * @return the child context | 子上下文
     */
    EvaluationContext createChild();
}
