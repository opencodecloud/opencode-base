package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.EvaluationContext;
import cloud.opencode.base.expression.sandbox.Sandbox;
import cloud.opencode.base.expression.sandbox.SandboxException;
import cloud.opencode.base.expression.spi.PropertyAccessor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Property Access Node
 * 属性访问节点
 *
 * <p>Represents property access: object.property, object?.property (null-safe)</p>
 * <p>表示属性访问：object.property, object?.property（空安全）</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JavaBean getter access (getXxx, isXxx) - JavaBean getter访问</li>
 *   <li>Record accessor method support - Record访问器方法支持</li>
 *   <li>Map key access - Map键访问</li>
 *   <li>Public field access - 公共字段访问</li>
 *   <li>Null-safe navigation (?.) - 空安全导航</li>
 *   <li>Custom PropertyAccessor SPI support - 自定义PropertyAccessor SPI支持</li>
 *   <li>Sandbox-controlled property access - 沙箱控制的属性访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // user.name
 * Node prop = PropertyAccessNode.of(userNode, "name");
 * Object name = prop.evaluate(ctx);
 *
 * // user?.address - null-safe
 * Node safe = PropertyAccessNode.nullSafe(userNode, "address");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: Optional via nullSafe mode - 空值安全: 通过nullSafe模式可选</li>
 *   <li>Only public fields and methods accessible - 仅可访问公共字段和方法</li>
 * </ul>
 *
 * @param target the target object node | 目标对象节点
 * @param property the property name | 属性名
 * @param nullSafe whether to use null-safe access | 是否使用空安全访问
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public record PropertyAccessNode(Node target, String property, boolean nullSafe) implements Node {

    public PropertyAccessNode {
        Objects.requireNonNull(target, "target cannot be null");
        Objects.requireNonNull(property, "property cannot be null");
    }

    /**
     * Create standard property access
     * 创建标准属性访问
     *
     * @param target the target node | 目标节点
     * @param property the property name | 属性名
     * @return the property access node | 属性访问节点
     */
    public static PropertyAccessNode of(Node target, String property) {
        return new PropertyAccessNode(target, property, false);
    }

    /**
     * Create null-safe property access
     * 创建空安全属性访问
     *
     * @param target the target node | 目标节点
     * @param property the property name | 属性名
     * @return the null-safe property access node | 空安全属性访问节点
     */
    public static PropertyAccessNode nullSafe(Node target, String property) {
        return new PropertyAccessNode(target, property, true);
    }

    /**
     * Create property access with null-safe option
     * 创建带空安全选项的属性访问
     *
     * @param target the target node | 目标节点
     * @param property the property name | 属性名
     * @param nullSafe whether to use null-safe access | 是否使用空安全访问
     * @return the property access node | 属性访问节点
     */
    public static PropertyAccessNode of(Node target, String property, boolean nullSafe) {
        return new PropertyAccessNode(target, property, nullSafe);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        Object targetObj = target.evaluate(context);

        if (targetObj == null) {
            if (nullSafe) {
                return null;
            }
            throw OpenExpressionException.nullPointer("property access on null: " + property);
        }

        return getPropertyValue(targetObj, property, context);
    }

    /**
     * Get property value from object
     * 从对象获取属性值
     *
     * @param target the target object | 目标对象
     * @param property the property name | 属性名
     * @param context the evaluation context | 求值上下文
     * @return the property value | 属性值
     */
    public static Object getPropertyValue(Object target, String property, EvaluationContext context) {
        // Check sandbox permissions before any property access
        Sandbox sandbox = context.getSandbox();
        if (sandbox != null && !sandbox.isPropertyAllowed(target, property)) {
            throw SandboxException.propertyNotAllowed(property);
        }

        // Try custom property accessors first
        List<PropertyAccessor> accessors = context.getPropertyAccessors();
        for (PropertyAccessor accessor : accessors) {
            if (accessor.canRead(target, property)) {
                try {
                    return accessor.read(target, property);
                } catch (SandboxException e) {
                    throw e;
                } catch (Exception e) {
                    // Try next accessor
                }
            }
        }

        // Handle Map
        if (target instanceof Map<?, ?> map) {
            return map.get(property);
        }

        // Handle record accessor methods
        Class<?> clazz = target.getClass();

        // Try getter method: getProperty()
        String getterName = "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
        try {
            Method getter = clazz.getMethod(getterName);
            return getter.invoke(target);
        } catch (NoSuchMethodException e) {
            // Try record accessor or isProperty() for booleans
        } catch (Exception e) {
            throw OpenExpressionException.evaluationError("Failed to invoke " + getterName, e);
        }

        // Try isProperty() for boolean
        String isGetterName = "is" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
        try {
            Method isGetter = clazz.getMethod(isGetterName);
            return isGetter.invoke(target);
        } catch (NoSuchMethodException e) {
            // Try record accessor
        } catch (Exception e) {
            throw OpenExpressionException.evaluationError("Failed to invoke " + isGetterName, e);
        }

        // Try record accessor: property()
        try {
            Method accessor = clazz.getMethod(property);
            return accessor.invoke(target);
        } catch (NoSuchMethodException e) {
            // Try field access
        } catch (Exception e) {
            throw OpenExpressionException.evaluationError("Failed to invoke " + property + "()", e);
        }

        // Try direct field access - only allow public fields
        try {
            Field field = clazz.getDeclaredField(property);
            if (!Modifier.isPublic(field.getModifiers())) {
                throw OpenExpressionException.propertyNotFound(property, clazz);
            }
            return field.get(target);
        } catch (NoSuchFieldException e) {
            throw OpenExpressionException.propertyNotFound(property, clazz);
        } catch (OpenExpressionException e) {
            throw e;
        } catch (Exception e) {
            throw OpenExpressionException.evaluationError("Failed to access field " + property, e);
        }
    }

    @Override
    public String toExpressionString() {
        return target.toExpressionString() + (nullSafe ? "?." : ".") + property;
    }
}
