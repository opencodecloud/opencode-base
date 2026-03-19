package cloud.opencode.base.expression.sandbox;

import cloud.opencode.base.expression.OpenExpressionException;

/**
 * Sandbox Exception
 * 沙箱异常
 *
 * <p>Thrown when a security violation is detected during expression evaluation.</p>
 * <p>在表达式求值过程中检测到安全违规时抛出。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Typed violation categories via ViolationType enum - 通过ViolationType枚举的类型化违规分类</li>
 *   <li>Violated resource tracking - 被违规资源跟踪</li>
 *   <li>Static factory methods for common violations - 常见违规的静态工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     OpenExpression.eval("Runtime.exec('cmd')", sandboxedCtx);
 * } catch (SandboxException e) {
 *     SandboxException.ViolationType type = e.getViolationType();
 *     String resource = e.getViolatedResource();
 * }
 *
 * // Create specific violations
 * throw SandboxException.classNotAllowed("java.lang.Runtime");
 * throw SandboxException.methodNotAllowed("exec");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable after construction - 线程安全: 是，构造后不可变</li>
 *   <li>Null-safe: Yes, null resource handled gracefully - 空值安全: 是，null资源优雅处理</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class SandboxException extends OpenExpressionException {

    private final ViolationType violationType;
    private final String violatedResource;

    /**
     * Create sandbox exception
     * 创建沙箱异常
     *
     * @param message the error message | 错误消息
     */
    public SandboxException(String message) {
        this(message, ViolationType.UNKNOWN, null);
    }

    /**
     * Create sandbox exception with violation type
     * 创建带违规类型的沙箱异常
     *
     * @param message the error message | 错误消息
     * @param violationType the violation type | 违规类型
     * @param violatedResource the violated resource | 被违规的资源
     */
    public SandboxException(String message, ViolationType violationType, String violatedResource) {
        super("Sandbox violation: " + message);
        this.violationType = violationType;
        this.violatedResource = violatedResource;
    }

    /**
     * Create sandbox exception with cause
     * 创建带原因的沙箱异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public SandboxException(String message, Throwable cause) {
        super("Sandbox violation: " + message, cause);
        this.violationType = ViolationType.UNKNOWN;
        this.violatedResource = null;
    }

    /**
     * Get the violation type
     * 获取违规类型
     *
     * @return the violation type | 违规类型
     */
    public ViolationType getViolationType() {
        return violationType;
    }

    /**
     * Get the violated resource
     * 获取被违规的资源
     *
     * @return the violated resource | 被违规的资源
     */
    public String getViolatedResource() {
        return violatedResource;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Create exception for denied class access
     * 创建拒绝类访问的异常
     *
     * @param className the class name | 类名
     * @return the exception | 异常
     */
    public static SandboxException classNotAllowed(String className) {
        return new SandboxException(
                "Access to class '" + className + "' is not allowed",
                ViolationType.CLASS_ACCESS,
                className
        );
    }

    /**
     * Create exception for denied class access
     * 创建拒绝类访问的异常
     *
     * @param clazz the class | 类
     * @return the exception | 异常
     */
    public static SandboxException classNotAllowed(Class<?> clazz) {
        return classNotAllowed(clazz != null ? clazz.getName() : "null");
    }

    /**
     * Create exception for denied method access
     * 创建拒绝方法访问的异常
     *
     * @param methodName the method name | 方法名
     * @return the exception | 异常
     */
    public static SandboxException methodNotAllowed(String methodName) {
        return new SandboxException(
                "Access to method '" + methodName + "' is not allowed",
                ViolationType.METHOD_CALL,
                methodName
        );
    }

    /**
     * Create exception for denied method access with class context
     * 创建带类上下文的拒绝方法访问异常
     *
     * @param className the class name | 类名
     * @param methodName the method name | 方法名
     * @return the exception | 异常
     */
    public static SandboxException methodNotAllowed(String className, String methodName) {
        String fullName = className + "." + methodName;
        return new SandboxException(
                "Access to method '" + fullName + "' is not allowed",
                ViolationType.METHOD_CALL,
                fullName
        );
    }

    /**
     * Create exception for denied property access
     * 创建拒绝属性访问的异常
     *
     * @param propertyName the property name | 属性名
     * @return the exception | 异常
     */
    public static SandboxException propertyNotAllowed(String propertyName) {
        return new SandboxException(
                "Access to property '" + propertyName + "' is not allowed",
                ViolationType.PROPERTY_ACCESS,
                propertyName
        );
    }

    /**
     * Create exception for denied function access
     * 创建拒绝函数访问的异常
     *
     * @param functionName the function name | 函数名
     * @return the exception | 异常
     */
    public static SandboxException functionNotAllowed(String functionName) {
        return new SandboxException(
                "Access to function '" + functionName + "' is not allowed",
                ViolationType.FUNCTION_CALL,
                functionName
        );
    }

    /**
     * Create exception for execution timeout
     * 创建执行超时异常
     *
     * @param timeoutMs the timeout in milliseconds | 超时毫秒数
     * @return the exception | 异常
     */
    public static SandboxException timeout(long timeoutMs) {
        return new SandboxException(
                "Expression execution timed out after " + timeoutMs + "ms",
                ViolationType.TIMEOUT,
                String.valueOf(timeoutMs)
        );
    }

    /**
     * Create exception for iteration limit exceeded
     * 创建迭代限制超出异常
     *
     * @param maxIterations the max iterations | 最大迭代次数
     * @return the exception | 异常
     */
    public static SandboxException iterationLimitExceeded(int maxIterations) {
        return new SandboxException(
                "Expression execution exceeded maximum iterations: " + maxIterations,
                ViolationType.ITERATION_LIMIT,
                String.valueOf(maxIterations)
        );
    }

    /**
     * Create exception for expression length exceeded
     * 创建表达式长度超出异常
     *
     * @param maxLength the max length | 最大长度
     * @param actualLength the actual length | 实际长度
     * @return the exception | 异常
     */
    public static SandboxException expressionTooLong(int maxLength, int actualLength) {
        return new SandboxException(
                "Expression length " + actualLength + " exceeds maximum " + maxLength,
                ViolationType.EXPRESSION_LENGTH,
                String.valueOf(actualLength)
        );
    }

    /**
     * Create exception for depth limit exceeded
     * 创建深度限制超出异常
     *
     * @param maxDepth the max depth | 最大深度
     * @return the exception | 异常
     */
    public static SandboxException depthLimitExceeded(int maxDepth) {
        return new SandboxException(
                "Expression evaluation exceeded maximum depth: " + maxDepth,
                ViolationType.DEPTH_LIMIT,
                String.valueOf(maxDepth)
        );
    }

    /**
     * Violation Type Enum
     * 违规类型枚举
     */
    public enum ViolationType {
        /**
         * Class access violation
         * 类访问违规
         */
        CLASS_ACCESS,

        /**
         * Method call violation
         * 方法调用违规
         */
        METHOD_CALL,

        /**
         * Property access violation
         * 属性访问违规
         */
        PROPERTY_ACCESS,

        /**
         * Function call violation
         * 函数调用违规
         */
        FUNCTION_CALL,

        /**
         * Execution timeout
         * 执行超时
         */
        TIMEOUT,

        /**
         * Iteration limit exceeded
         * 迭代限制超出
         */
        ITERATION_LIMIT,

        /**
         * Expression too long
         * 表达式过长
         */
        EXPRESSION_LENGTH,

        /**
         * Evaluation depth exceeded
         * 求值深度超出
         */
        DEPTH_LIMIT,

        /**
         * Unknown violation
         * 未知违规
         */
        UNKNOWN
    }
}
