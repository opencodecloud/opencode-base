package cloud.opencode.base.tree.result;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Tree Result
 * 树操作结果
 *
 * <p>A sealed interface representing the result of tree operations.
 * Supports pattern matching with switch expressions for exhaustive handling.</p>
 * <p>表示树操作结果的密封接口。支持使用switch表达式进行模式匹配以实现完整处理。</p>
 *
 * <h2>Usage Examples | 使用示例</h2>
 * <pre>{@code
 * // Pattern matching with switch
 * String message = switch (result) {
 *     case TreeResult.Success(var data) -> "Found: " + data;
 *     case TreeResult.Failure(var msg, var cause) -> "Error: " + msg;
 *     case TreeResult.Empty() -> "No data found";
 *     case TreeResult.Validation(var violations) -> "Invalid: " + violations;
 * };
 *
 * // Functional style
 * result.map(node -> node.getData())
 *       .onSuccess(data -> System.out.println(data))
 *       .onFailure(error -> log.error(error));
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed interface with pattern matching support - 密封接口支持模式匹配</li>
 *   <li>Success, Failure, Empty, and Validation variants - 成功、失败、空和验证变体</li>
 *   <li>Functional operations (map, flatMap, recover) - 函数式操作</li>
 *   <li>Violation tracking with severity levels - 带严重程度级别的违规跟踪</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable records) - 是（不可变记录）</li>
 *   <li>Null-safe: Partial (data can be null in Success) - 部分（Success中数据可为null）</li>
 * </ul>
 * @param <T> the result data type | 结果数据类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public sealed interface TreeResult<T>
        permits TreeResult.Success, TreeResult.Failure, TreeResult.Empty, TreeResult.Validation {

    // ==================== State Query Methods ====================

    /**
     * Check if this result represents a success
     * 检查此结果是否表示成功
     *
     * @return true if success | 如果成功返回true
     */
    default boolean isSuccess() {
        return this instanceof Success<T>;
    }

    /**
     * Check if this result represents a failure
     * 检查此结果是否表示失败
     *
     * @return true if failure | 如果失败返回true
     */
    default boolean isFailed() {
        return this instanceof Failure<T>;
    }

    /**
     * Check if this result represents an empty result
     * 检查此结果是否表示空结果
     *
     * @return true if empty | 如果为空返回true
     */
    default boolean isEmpty() {
        return this instanceof Empty<T>;
    }

    /**
     * Check if this result represents a validation failure
     * 检查此结果是否表示验证失败
     *
     * @return true if validation failure | 如果验证失败返回true
     */
    default boolean isValidation() {
        return this instanceof Validation<T>;
    }

    // ==================== Value Access Methods ====================

    /**
     * Get the value or null if not present
     * 获取值，如果不存在则返回null
     *
     * @return the value or null | 值或null
     */
    default T getOrNull() {
        return switch (this) {
            case Success<T>(var data) -> data;
            case Failure<T> _, Empty<T> _, Validation<T> _ -> null;
        };
    }

    /**
     * Get the value or throw if not present
     * 获取值，如果不存在则抛出异常
     *
     * @return the value | 值
     * @throws NoSuchElementException if no value present | 如果没有值抛出异常
     */
    default T getOrThrow() {
        return switch (this) {
            case Success<T>(var data) -> data;
            case Failure<T>(var message, _) -> throw new NoSuchElementException("Result is failure: " + message);
            case Empty<T> _ -> throw new NoSuchElementException("Result is empty");
            case Validation<T>(var violations) -> throw new NoSuchElementException(
                    "Result is validation failure: " + violations.size() + " violations");
        };
    }

    /**
     * Get the value or throw a custom exception
     * 获取值，如果不存在则抛出自定义异常
     *
     * @param <X> the exception type | 异常类型
     * @param exceptionSupplier the exception supplier | 异常提供者
     * @return the value | 值
     * @throws X if no value present | 如果没有值抛出异常
     */
    default <X extends Throwable> T getOrThrow(Supplier<? extends X> exceptionSupplier) throws X {
        Objects.requireNonNull(exceptionSupplier, "exceptionSupplier must not be null");
        return switch (this) {
            case Success<T>(var data) -> data;
            case Failure<T> _, Empty<T> _, Validation<T> _ -> throw exceptionSupplier.get();
        };
    }

    /**
     * Get the value or a default value
     * 获取值，如果不存在则返回默认值
     *
     * @param defaultValue the default value | 默认值
     * @return the value or default | 值或默认值
     */
    default T getOrElse(T defaultValue) {
        return switch (this) {
            case Success<T>(var data) -> data;
            case Failure<T> _, Empty<T> _, Validation<T> _ -> defaultValue;
        };
    }

    /**
     * Get the value or compute a default value
     * 获取值，如果不存在则计算默认值
     *
     * @param defaultSupplier the default value supplier | 默认值提供者
     * @return the value or computed default | 值或计算的默认值
     */
    default T getOrElseGet(Supplier<? extends T> defaultSupplier) {
        Objects.requireNonNull(defaultSupplier, "defaultSupplier must not be null");
        return switch (this) {
            case Success<T>(var data) -> data;
            case Failure<T> _, Empty<T> _, Validation<T> _ -> defaultSupplier.get();
        };
    }

    // ==================== Transformation Methods ====================

    /**
     * Transform the value if present
     * 如果存在则转换值
     *
     * @param <U> the target type | 目标类型
     * @param mapper the mapping function | 映射函数
     * @return the transformed result | 转换后的结果
     */
    @SuppressWarnings("unchecked")
    default <U> TreeResult<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return switch (this) {
            case Success<T>(var data) -> success(mapper.apply(data));
            case Failure<T>(var message, var cause) -> (TreeResult<U>) failure(message, cause);
            case Empty<T> _ -> empty();
            case Validation<T>(var violations) -> (TreeResult<U>) validation(violations);
        };
    }

    /**
     * Transform the value with a function that returns a TreeResult
     * 使用返回TreeResult的函数转换值
     *
     * @param <U> the target type | 目标类型
     * @param mapper the mapping function | 映射函数
     * @return the transformed result | 转换后的结果
     */
    @SuppressWarnings("unchecked")
    default <U> TreeResult<U> flatMap(Function<? super T, ? extends TreeResult<U>> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return switch (this) {
            case Success<T>(var data) -> mapper.apply(data);
            case Failure<T>(var message, var cause) -> (TreeResult<U>) failure(message, cause);
            case Empty<T> _ -> empty();
            case Validation<T>(var violations) -> (TreeResult<U>) validation(violations);
        };
    }

    /**
     * Recover from failure with an alternative value
     * 从失败中恢复为替代值
     *
     * @param recoveryFunction the recovery function | 恢复函数
     * @return the recovered result | 恢复后的结果
     */
    default TreeResult<T> recover(Function<? super String, ? extends T> recoveryFunction) {
        Objects.requireNonNull(recoveryFunction, "recoveryFunction must not be null");
        return switch (this) {
            case Success<T> s -> s;
            case Failure<T>(var message, _) -> success(recoveryFunction.apply(message));
            case Empty<T> e -> e;
            case Validation<T> v -> v;
        };
    }

    /**
     * Recover from failure with an alternative TreeResult
     * 从失败中恢复为替代TreeResult
     *
     * @param recoveryFunction the recovery function | 恢复函数
     * @return the recovered result | 恢复后的结果
     */
    default TreeResult<T> recoverWith(Function<? super String, ? extends TreeResult<T>> recoveryFunction) {
        Objects.requireNonNull(recoveryFunction, "recoveryFunction must not be null");
        return switch (this) {
            case Success<T> s -> s;
            case Failure<T>(var message, _) -> recoveryFunction.apply(message);
            case Empty<T> e -> e;
            case Validation<T> v -> v;
        };
    }

    // ==================== Callback Methods ====================

    /**
     * Execute action if success
     * 如果成功则执行操作
     *
     * @param action the action to execute | 要执行的操作
     * @return this result for chaining | 此结果用于链式调用
     */
    default TreeResult<T> onSuccess(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action must not be null");
        if (this instanceof Success<T>(var data)) {
            action.accept(data);
        }
        return this;
    }

    /**
     * Execute action if failure
     * 如果失败则执行操作
     *
     * @param action the action to execute with error message | 要执行的操作，带错误消息
     * @return this result for chaining | 此结果用于链式调用
     */
    default TreeResult<T> onFailure(Consumer<? super String> action) {
        Objects.requireNonNull(action, "action must not be null");
        if (this instanceof Failure<T>(var message, _)) {
            action.accept(message);
        }
        return this;
    }

    /**
     * Execute action if empty
     * 如果为空则执行操作
     *
     * @param action the action to execute | 要执行的操作
     * @return this result for chaining | 此结果用于链式调用
     */
    default TreeResult<T> onEmpty(Runnable action) {
        Objects.requireNonNull(action, "action must not be null");
        if (this instanceof Empty<T>) {
            action.run();
        }
        return this;
    }

    /**
     * Execute action if validation failure
     * 如果验证失败则执行操作
     *
     * @param action the action to execute with violations | 要执行的操作，带违规列表
     * @return this result for chaining | 此结果用于链式调用
     */
    default TreeResult<T> onValidation(Consumer<? super List<Violation>> action) {
        Objects.requireNonNull(action, "action must not be null");
        if (this instanceof Validation<T>(var violations)) {
            action.accept(violations);
        }
        return this;
    }

    // ==================== Conversion Methods ====================

    /**
     * Convert to Optional
     * 转换为Optional
     *
     * @return the Optional | Optional对象
     */
    default Optional<T> toOptional() {
        return switch (this) {
            case Success<T>(var data) -> Optional.ofNullable(data);
            case Failure<T> _, Empty<T> _, Validation<T> _ -> Optional.empty();
        };
    }

    /**
     * Fold the result into a single value
     * 将结果折叠为单个值
     *
     * @param <U> the result type | 结果类型
     * @param onSuccess function to apply on success | 成功时应用的函数
     * @param onFailure function to apply on failure | 失败时应用的函数
     * @param onEmpty supplier for empty case | 空情况的提供者
     * @param onValidation function to apply on validation failure | 验证失败时应用的函数
     * @return the folded value | 折叠后的值
     */
    default <U> U fold(
            Function<? super T, ? extends U> onSuccess,
            Function<? super String, ? extends U> onFailure,
            Supplier<? extends U> onEmpty,
            Function<? super List<Violation>, ? extends U> onValidation) {
        Objects.requireNonNull(onSuccess, "onSuccess must not be null");
        Objects.requireNonNull(onFailure, "onFailure must not be null");
        Objects.requireNonNull(onEmpty, "onEmpty must not be null");
        Objects.requireNonNull(onValidation, "onValidation must not be null");
        return switch (this) {
            case Success<T>(var data) -> onSuccess.apply(data);
            case Failure<T>(var message, _) -> onFailure.apply(message);
            case Empty<T> _ -> onEmpty.get();
            case Validation<T>(var violations) -> onValidation.apply(violations);
        };
    }

    // ==================== Static Factory Methods ====================

    /**
     * Create a success result
     * 创建成功结果
     *
     * @param <T> the data type | 数据类型
     * @param data the data | 数据
     * @return the success result | 成功结果
     */
    static <T> TreeResult<T> success(T data) {
        return new Success<>(data);
    }

    /**
     * Create a failure result
     * 创建失败结果
     *
     * @param <T> the data type | 数据类型
     * @param message the error message | 错误消息
     * @return the failure result | 失败结果
     */
    static <T> TreeResult<T> failure(String message) {
        return new Failure<>(message, null);
    }

    /**
     * Create a failure result with cause
     * 创建带原因的失败结果
     *
     * @param <T> the data type | 数据类型
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     * @return the failure result | 失败结果
     */
    static <T> TreeResult<T> failure(String message, Throwable cause) {
        return new Failure<>(message, cause);
    }

    /**
     * Create an empty result
     * 创建空结果
     *
     * @param <T> the data type | 数据类型
     * @return the empty result | 空结果
     */
    static <T> TreeResult<T> empty() {
        return new Empty<>();
    }

    /**
     * Create a validation result
     * 创建验证结果
     *
     * @param <T> the data type | 数据类型
     * @param violations the validation violations | 验证违规列表
     * @return the validation result | 验证结果
     */
    static <T> TreeResult<T> validation(List<Violation> violations) {
        return new Validation<>(violations);
    }

    /**
     * Create a validation result with single violation
     * 创建单个违规的验证结果
     *
     * @param <T> the data type | 数据类型
     * @param violation the validation violation | 验证违规
     * @return the validation result | 验证结果
     */
    static <T> TreeResult<T> validation(Violation violation) {
        Objects.requireNonNull(violation, "violation must not be null");
        return new Validation<>(List.of(violation));
    }

    /**
     * Create a result from Optional
     * 从Optional创建结果
     *
     * @param <T> the data type | 数据类型
     * @param optional the optional | Optional对象
     * @return the result | 结果
     */
    static <T> TreeResult<T> fromOptional(Optional<T> optional) {
        Objects.requireNonNull(optional, "optional must not be null");
        return optional.map(TreeResult::success).orElseGet(TreeResult::empty);
    }

    /**
     * Create a result from nullable value
     * 从可空值创建结果
     *
     * @param <T> the data type | 数据类型
     * @param value the nullable value | 可空值
     * @return the result | 结果
     */
    static <T> TreeResult<T> fromNullable(T value) {
        return value != null ? success(value) : empty();
    }

    /**
     * Create a result from a callable
     * 从Callable创建结果
     *
     * @param <T> the data type | 数据类型
     * @param callable the callable | Callable对象
     * @return the result | 结果
     */
    static <T> TreeResult<T> fromCallable(java.util.concurrent.Callable<T> callable) {
        Objects.requireNonNull(callable, "callable must not be null");
        try {
            return success(callable.call());
        } catch (Exception e) {
            return failure(e.getMessage(), e);
        }
    }

    // ==================== Permitted Implementations ====================

    /**
     * Success Result
     * 成功结果
     *
     * <p>Represents a successful tree operation with data.</p>
     * <p>表示带数据的成功树操作。</p>
     *
     * @param <T> the data type | 数据类型
     * @param data the result data | 结果数据
     * @author Leon Soo
     * @since JDK 25, opencode-base-tree V1.0.0
     */
    record Success<T>(T data) implements TreeResult<T> {

        /**
         * Compact constructor
         * 紧凑构造函数
         */
        public Success {
            // data can be null for void operations
        }

        @Override
        public String toString() {
            return "TreeResult.Success[data=" + data + "]";
        }
    }

    /**
     * Failure Result
     * 失败结果
     *
     * <p>Represents a failed tree operation with error information.</p>
     * <p>表示带错误信息的失败树操作。</p>
     *
     * @param <T> the data type | 数据类型
     * @param message the error message | 错误消息
     * @param cause the cause throwable | 原因异常
     * @author Leon Soo
     * @since JDK 25, opencode-base-tree V1.0.0
     */
    record Failure<T>(String message, Throwable cause) implements TreeResult<T> {

        /**
         * Compact constructor
         * 紧凑构造函数
         */
        public Failure {
            message = message != null ? message : "Unknown error";
        }

        /**
         * Check if has cause
         * 检查是否有原因
         *
         * @return true if has cause | 如果有原因返回true
         */
        public boolean hasCause() {
            return cause != null;
        }

        /**
         * Get the root cause
         * 获取根本原因
         *
         * @return the root cause or null | 根本原因或null
         */
        public Throwable getRootCause() {
            if (cause == null) return null;
            Throwable root = cause;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            return root;
        }

        @Override
        public String toString() {
            return "TreeResult.Failure[message=" + message +
                    (cause != null ? ", cause=" + cause.getClass().getSimpleName() : "") + "]";
        }
    }

    /**
     * Empty Result
     * 空结果
     *
     * <p>Represents an empty result when no data is found.</p>
     * <p>表示未找到数据时的空结果。</p>
     *
     * @param <T> the data type | 数据类型
     * @author Leon Soo
     * @since JDK 25, opencode-base-tree V1.0.0
     */
    record Empty<T>() implements TreeResult<T> {

        @Override
        public String toString() {
            return "TreeResult.Empty[]";
        }
    }

    /**
     * Validation Result
     * 验证结果
     *
     * <p>Represents a validation failure with violation details.</p>
     * <p>表示带违规详情的验证失败。</p>
     *
     * @param <T> the data type | 数据类型
     * @param violations the list of violations | 违规列表
     * @author Leon Soo
     * @since JDK 25, opencode-base-tree V1.0.0
     */
    record Validation<T>(List<Violation> violations) implements TreeResult<T> {

        /**
         * Compact constructor
         * 紧凑构造函数
         */
        public Validation {
            violations = violations != null ? List.copyOf(violations) : List.of();
        }

        /**
         * Get the violation count
         * 获取违规数量
         *
         * @return the violation count | 违规数量
         */
        public int getViolationCount() {
            return violations.size();
        }

        /**
         * Check if has violations
         * 检查是否有违规
         *
         * @return true if has violations | 如果有违规返回true
         */
        public boolean hasViolations() {
            return !violations.isEmpty();
        }

        /**
         * Get violations by severity
         * 按严重程度获取违规
         *
         * @param severity the severity level | 严重程度
         * @return the violations with the severity | 具有该严重程度的违规列表
         */
        public List<Violation> getViolationsBySeverity(Violation.Severity severity) {
            Objects.requireNonNull(severity, "severity must not be null");
            return violations.stream()
                    .filter(v -> v.severity() == severity)
                    .toList();
        }

        /**
         * Get error violations
         * 获取错误违规
         *
         * @return the error violations | 错误违规列表
         */
        public List<Violation> getErrors() {
            return getViolationsBySeverity(Violation.Severity.ERROR);
        }

        /**
         * Get warning violations
         * 获取警告违规
         *
         * @return the warning violations | 警告违规列表
         */
        public List<Violation> getWarnings() {
            return getViolationsBySeverity(Violation.Severity.WARNING);
        }

        /**
         * Get the combined error message
         * 获取组合错误消息
         *
         * @return the combined message | 组合消息
         */
        public String getCombinedMessage() {
            return violations.stream()
                    .map(Violation::message)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("");
        }

        @Override
        public String toString() {
            return "TreeResult.Validation[violations=" + violations.size() + "]";
        }
    }

    // ==================== Violation Record ====================

    /**
     * Violation
     * 违规
     *
     * <p>Represents a validation violation with details about the error.</p>
     * <p>表示带有错误详情的验证违规。</p>
     *
     * @param field the field name that violated | 违规的字段名
     * @param message the violation message | 违规消息
     * @param code the violation code | 违规代码
     * @param severity the violation severity | 违规严重程度
     * @param value the invalid value | 无效值
     * @author Leon Soo
     * @since JDK 25, opencode-base-tree V1.0.0
     */
    record Violation(
            String field,
            String message,
            String code,
            Severity severity,
            Object value
    ) {

        /**
         * Compact constructor
         * 紧凑构造函数
         */
        public Violation {
            Objects.requireNonNull(message, "message must not be null");
            severity = severity != null ? severity : Severity.ERROR;
        }

        /**
         * Create a simple violation
         * 创建简单违规
         *
         * @param message the violation message | 违规消息
         * @return the violation | 违规
         */
        public static Violation of(String message) {
            return new Violation(null, message, null, Severity.ERROR, null);
        }

        /**
         * Create a violation with field
         * 创建带字段的违规
         *
         * @param field the field name | 字段名
         * @param message the violation message | 违规消息
         * @return the violation | 违规
         */
        public static Violation of(String field, String message) {
            return new Violation(field, message, null, Severity.ERROR, null);
        }

        /**
         * Create a violation with field and code
         * 创建带字段和代码的违规
         *
         * @param field the field name | 字段名
         * @param message the violation message | 违规消息
         * @param code the violation code | 违规代码
         * @return the violation | 违规
         */
        public static Violation of(String field, String message, String code) {
            return new Violation(field, message, code, Severity.ERROR, null);
        }

        /**
         * Create an error violation
         * 创建错误违规
         *
         * @param field the field name | 字段名
         * @param message the violation message | 违规消息
         * @return the violation | 违规
         */
        public static Violation error(String field, String message) {
            return new Violation(field, message, null, Severity.ERROR, null);
        }

        /**
         * Create a warning violation
         * 创建警告违规
         *
         * @param field the field name | 字段名
         * @param message the violation message | 违规消息
         * @return the violation | 违规
         */
        public static Violation warning(String field, String message) {
            return new Violation(field, message, null, Severity.WARNING, null);
        }

        /**
         * Create an info violation
         * 创建信息违规
         *
         * @param field the field name | 字段名
         * @param message the violation message | 违规消息
         * @return the violation | 违规
         */
        public static Violation info(String field, String message) {
            return new Violation(field, message, null, Severity.INFO, null);
        }

        /**
         * Create a violation with value
         * 创建带值的违规
         *
         * @param field the field name | 字段名
         * @param message the violation message | 违规消息
         * @param value the invalid value | 无效值
         * @return the violation | 违规
         */
        public static Violation withValue(String field, String message, Object value) {
            return new Violation(field, message, null, Severity.ERROR, value);
        }

        /**
         * Check if this is an error
         * 检查是否为错误
         *
         * @return true if error | 如果为错误返回true
         */
        public boolean isError() {
            return severity == Severity.ERROR;
        }

        /**
         * Check if this is a warning
         * 检查是否为警告
         *
         * @return true if warning | 如果为警告返回true
         */
        public boolean isWarning() {
            return severity == Severity.WARNING;
        }

        /**
         * Check if this is info
         * 检查是否为信息
         *
         * @return true if info | 如果为信息返回true
         */
        public boolean isInfo() {
            return severity == Severity.INFO;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Violation[");
            if (field != null) {
                sb.append("field=").append(field).append(", ");
            }
            sb.append("message=").append(message);
            if (code != null) {
                sb.append(", code=").append(code);
            }
            sb.append(", severity=").append(severity);
            if (value != null) {
                sb.append(", value=").append(value);
            }
            sb.append("]");
            return sb.toString();
        }

        /**
         * Violation Severity
         * 违规严重程度
         *
         * <p>Defines the severity level of a violation.</p>
         * <p>定义违规的严重程度级别。</p>
         *
         * @author Leon Soo
         * @since JDK 25, opencode-base-tree V1.0.0
         */
        public enum Severity {
            /**
             * Error severity - operation should fail
             * 错误严重程度 - 操作应该失败
             */
            ERROR,

            /**
             * Warning severity - operation may continue
             * 警告严重程度 - 操作可以继续
             */
            WARNING,

            /**
             * Info severity - informational only
             * 信息严重程度 - 仅供参考
             */
            INFO
        }
    }
}
