package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.EvaluationContext;
import cloud.opencode.base.expression.sandbox.Sandbox;
import cloud.opencode.base.expression.sandbox.SandboxException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Method Call Node
 * 方法调用节点
 *
 * <p>Represents method calls: str.toUpperCase(), list.size()</p>
 * <p>表示方法调用：str.toUpperCase(), list.size()</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Reflective method invocation on target objects - 对目标对象的反射方法调用</li>
 *   <li>Null-safe method call (?.) support - 空安全方法调用(?.)支持</li>
 *   <li>Varargs method support - 可变参数方法支持</li>
 *   <li>Sandbox-controlled method access - 沙箱控制的方法访问</li>
 *   <li>Primitive type auto-boxing - 原始类型自动装箱</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // str.toUpperCase()
 * Node call = MethodCallNode.of(strNode, "toUpperCase", List.of());
 * Object result = call.evaluate(ctx);
 *
 * // obj?.method() - null-safe
 * Node safe = MethodCallNode.nullSafe(objNode, "method", List.of());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: Optional via nullSafe mode - 空值安全: 通过nullSafe模式可选</li>
 *   <li>Sandbox enforcement on method calls - 方法调用的沙箱强制执行</li>
 *   <li>Only public methods accessible - 仅可访问公共方法</li>
 * </ul>
 *
 * @param target the target object node | 目标对象节点
 * @param methodName the method name | 方法名
 * @param arguments the method arguments | 方法参数
 * @param nullSafe whether to use null-safe access | 是否使用空安全访问
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public record MethodCallNode(
    Node target,
    String methodName,
    List<Node> arguments,
    boolean nullSafe
) implements Node {

    public MethodCallNode {
        Objects.requireNonNull(target, "target cannot be null");
        Objects.requireNonNull(methodName, "methodName cannot be null");
        arguments = arguments != null ? List.copyOf(arguments) : List.of();
    }

    /**
     * Create method call node
     * 创建方法调用节点
     *
     * @param target the target node | 目标节点
     * @param methodName the method name | 方法名
     * @param arguments the arguments | 参数
     * @return the method call node | 方法调用节点
     */
    public static MethodCallNode of(Node target, String methodName, List<Node> arguments) {
        return new MethodCallNode(target, methodName, arguments, false);
    }

    /**
     * Create null-safe method call node
     * 创建空安全方法调用节点
     *
     * @param target the target node | 目标节点
     * @param methodName the method name | 方法名
     * @param arguments the arguments | 参数
     * @return the null-safe method call node | 空安全方法调用节点
     */
    public static MethodCallNode nullSafe(Node target, String methodName, List<Node> arguments) {
        return new MethodCallNode(target, methodName, arguments, true);
    }

    /**
     * Create method call node with null-safe option
     * 创建带空安全选项的方法调用节点
     *
     * @param target the target node | 目标节点
     * @param methodName the method name | 方法名
     * @param arguments the arguments | 参数
     * @param nullSafe whether to use null-safe access | 是否使用空安全访问
     * @return the method call node | 方法调用节点
     */
    public static MethodCallNode of(Node target, String methodName, List<Node> arguments, boolean nullSafe) {
        return new MethodCallNode(target, methodName, arguments, nullSafe);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        Object targetObj = target.evaluate(context);

        if (targetObj == null) {
            if (nullSafe) {
                return null;
            }
            throw OpenExpressionException.nullPointer("method call on null: " + methodName);
        }

        // Evaluate arguments
        Object[] args = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            args[i] = arguments.get(i).evaluate(context);
        }

        // Find and invoke method
        return invokeMethod(context, targetObj, methodName, args);
    }

    private Object invokeMethod(EvaluationContext context, Object target, String methodName, Object[] args) {
        Class<?> clazz = target.getClass();
        // Only use getMethods() which returns public methods
        Method[] methods = clazz.getMethods();
        Sandbox sandbox = context.getSandbox();

        // Find matching method
        for (Method method : methods) {
            if (method.getName().equals(methodName) && isCompatible(method, args)) {
                validateMethodAccess(sandbox, target, method);
                try {
                    // Only call setAccessible on public methods (for performance in
                    // cross-module scenarios), never to bypass access control
                    if (Modifier.isPublic(method.getModifiers())) {
                        method.setAccessible(true);
                    }
                    return method.invoke(target, args);
                } catch (Exception e) {
                    throw OpenExpressionException.evaluationError(
                        "Failed to invoke method " + methodName + ": " + e.getMessage(), e);
                }
            }
        }

        // Try varargs methods
        for (Method method : methods) {
            if (method.getName().equals(methodName) && method.isVarArgs()) {
                try {
                    Object[] varArgs = prepareVarArgs(method, args);
                    if (varArgs != null) {
                        validateMethodAccess(sandbox, target, method);
                        if (Modifier.isPublic(method.getModifiers())) {
                            method.setAccessible(true);
                        }
                        return method.invoke(target, varArgs);
                    }
                } catch (SandboxException e) {
                    throw e;
                } catch (Exception e) {
                    // Try next method
                }
            }
        }

        throw OpenExpressionException.methodNotFound(methodName, clazz);
    }

    private void validateMethodAccess(Sandbox sandbox, Object target, Method method) {
        if (sandbox != null && !sandbox.isMethodAllowed(target, method)) {
            throw SandboxException.methodNotAllowed(
                target.getClass().getName(), method.getName());
        }
    }

    private boolean isCompatible(Method method, Object[] args) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != args.length) {
            return false;
        }

        for (int i = 0; i < paramTypes.length; i++) {
            if (args[i] == null) {
                if (paramTypes[i].isPrimitive()) {
                    return false;
                }
                continue;
            }
            if (!isAssignable(paramTypes[i], args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    private boolean isAssignable(Class<?> paramType, Class<?> argType) {
        if (paramType.isAssignableFrom(argType)) {
            return true;
        }

        // Handle primitive type boxing
        if (paramType.isPrimitive()) {
            return switch (paramType.getName()) {
                case "int" -> argType == Integer.class;
                case "long" -> argType == Long.class || argType == Integer.class;
                case "double" -> argType == Double.class || argType == Float.class ||
                                 argType == Long.class || argType == Integer.class;
                case "float" -> argType == Float.class || argType == Integer.class;
                case "boolean" -> argType == Boolean.class;
                case "char" -> argType == Character.class;
                case "byte" -> argType == Byte.class;
                case "short" -> argType == Short.class || argType == Byte.class;
                default -> false;
            };
        }

        return false;
    }

    private Object[] prepareVarArgs(Method method, Object[] args) {
        Class<?>[] paramTypes = method.getParameterTypes();
        int fixedCount = paramTypes.length - 1;

        if (args.length < fixedCount) {
            return null;
        }

        // Check fixed parameters
        for (int i = 0; i < fixedCount; i++) {
            if (args[i] != null && !isAssignable(paramTypes[i], args[i].getClass())) {
                return null;
            }
        }

        // Create varargs array
        Class<?> varArgType = paramTypes[fixedCount].getComponentType();
        int varArgCount = args.length - fixedCount;
        Object varArgs = java.lang.reflect.Array.newInstance(varArgType, varArgCount);

        for (int i = 0; i < varArgCount; i++) {
            Object arg = args[fixedCount + i];
            if (arg != null && !isAssignable(varArgType, arg.getClass())) {
                return null;
            }
            java.lang.reflect.Array.set(varArgs, i, arg);
        }

        Object[] result = new Object[paramTypes.length];
        System.arraycopy(args, 0, result, 0, fixedCount);
        result[fixedCount] = varArgs;
        return result;
    }

    @Override
    public String toExpressionString() {
        StringBuilder sb = new StringBuilder();
        sb.append(target.toExpressionString());
        sb.append(nullSafe ? "?." : ".");
        sb.append(methodName);
        sb.append("(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(arguments.get(i).toExpressionString());
        }
        sb.append(")");
        return sb.toString();
    }
}
