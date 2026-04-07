package cloud.opencode.base.test.mock;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;

/**
 * Invocation - Record of a method invocation on a mock
 * 调用记录 - Mock上方法调用的记录
 *
 * <p>Immutable record of a method invocation including method, arguments, and timestamp.</p>
 * <p>方法调用的不可变记录，包括方法、参数和时间戳。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable invocation recording - 不可变调用记录</li>
 *   <li>Argument matching support - 参数匹配支持</li>
 *   <li>Method name and type introspection - 方法名称和类型内省</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Invocation inv = Invocation.of(method, args);
 * String name = inv.methodName();
 * boolean matches = inv.argsMatch("value1", 42);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (handles null args) - 空值安全: 是（处理空参数）</li>
 * </ul>
 *
 * @param method    the invoked method | 调用的方法
 * @param args      the arguments | 参数
 * @param timestamp the invocation timestamp | 调用时间戳
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public record Invocation(Method method, Object[] args, Instant timestamp) {

    /**
     * Compact constructor — defensively copies args array.
     * 紧凑构造器 — 防御性复制参数数组。
     */
    public Invocation {
        args = args != null ? args.clone() : null;
    }

    /**
     * Returns a defensive copy of the arguments array.
     * 返回参数数组的防御性副本。
     *
     * @return args copy or null | 参数副本或 null
     */
    @Override
    public Object[] args() {
        return args != null ? args.clone() : null;
    }

    /**
     * Creates an invocation with current timestamp.
     * 使用当前时间戳创建调用记录。
     *
     * @param method the method | 方法
     * @param args   the arguments | 参数
     * @return the invocation | 调用记录
     */
    public static Invocation of(Method method, Object[] args) {
        return new Invocation(method, args, Instant.now());
    }

    /**
     * Gets the method name.
     * 获取方法名。
     *
     * @return the method name | 方法名
     */
    public String methodName() {
        return method.getName();
    }

    /**
     * Gets the return type.
     * 获取返回类型。
     *
     * @return the return type | 返回类型
     */
    public Class<?> returnType() {
        return method.getReturnType();
    }

    /**
     * Gets the parameter types.
     * 获取参数类型。
     *
     * @return the parameter types | 参数类型
     */
    public Class<?>[] parameterTypes() {
        return method.getParameterTypes();
    }

    /**
     * Checks if arguments match.
     * 检查参数是否匹配。
     *
     * @param expectedArgs the expected arguments | 期望参数
     * @return true if match | 如果匹配返回 true
     */
    public boolean argsMatch(Object... expectedArgs) {
        // Treat null and empty array as equivalent (both mean no arguments)
        Object[] actual = (args == null || args.length == 0) ? null : args;
        Object[] expected = (expectedArgs == null || expectedArgs.length == 0) ? null : expectedArgs;
        return Arrays.deepEquals(actual, expected);
    }

    /**
     * Checks if this invocation is for the specified method name.
     * 检查此调用是否为指定方法名。
     *
     * @param name the method name | 方法名
     * @return true if match | 如果匹配返回 true
     */
    public boolean isMethod(String name) {
        return method.getName().equals(name);
    }

    @Override
    public String toString() {
        String argsStr = args != null ? Arrays.toString(args) : "[]";
        return method.getName() + argsStr + " at " + timestamp;
    }
}
