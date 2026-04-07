package cloud.opencode.base.core.exception;

import cloud.opencode.base.core.func.CheckedRunnable;
import cloud.opencode.base.core.func.CheckedSupplier;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Exception Utility - Exception chain analysis and handling utilities
 * 异常工具类 - 异常链分析和处理工具
 *
 * <p>Provides exception chain analysis, stack trace handling and safe execution wrappers.</p>
 * <p>提供异常链分析、堆栈跟踪处理和安全执行包装器等功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Root cause extraction (getRootCause) - 根本原因提取</li>
 *   <li>Stack trace to string (getStackTrace) - 堆栈跟踪转字符串</li>
 *   <li>Exception chain traversal (getCausalChain) - 异常链遍历</li>
 *   <li>Checked exception wrapping (wrapAndThrow) - 受检异常包装</li>
 *   <li>Sneaky throw (bypass compilation check) - 静默抛出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Throwable root = ExceptionUtil.getRootCause(exception);
 * String stackTrace = ExceptionUtil.getStackTrace(exception);
 * List<Throwable> chain = ExceptionUtil.getCausalChain(exception);
 * ExceptionUtil.sneakyThrow(new IOException("test"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class ExceptionUtil {

    private ExceptionUtil() {
        // 工具类不可实例化
    }

    /**
     * Gets the root cause of the exception chain.
     * 获取异常的根本原因。
     *
     * <p>Traverses the cause chain and returns the deepest exception (one with no cause).</p>
     * <p>遍历异常链，返回最底层的异常（没有 cause 的异常）。</p>
     *
     * @param throwable the exception to analyze | 待分析的异常
     * @return the root cause, or null if the input is null | 根本原因，如果输入为 null 则返回 null
     */
    public static Throwable getRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        java.util.Set<Throwable> seen = java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<>());
        Throwable cause = throwable;
        seen.add(cause);
        while (cause.getCause() != null && seen.add(cause.getCause())) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Gets the full stack trace as a string.
     * 获取异常的堆栈跟踪字符串。
     *
     * @param throwable the exception to convert | 待转换的异常
     * @return the stack trace string, or empty string if input is null | 堆栈跟踪字符串，如果输入为 null 则返回空字符串
     */
    public static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Gets the complete causal chain as a list.
     * 获取异常的因果链。
     *
     * <p>Returns all exceptions from the given throwable to the root cause.</p>
     * <p>返回从当前异常到根本原因的所有异常列表。</p>
     *
     * @param throwable the exception to traverse | 待遍历的异常
     * @return unmodifiable list of the causal chain, or empty list if input is null |
     *         异常链的不可修改列表（从当前异常到根本原因），如果输入为 null 则返回空列表
     */
    public static List<Throwable> getCausalChain(Throwable throwable) {
        if (throwable == null) {
            return Collections.emptyList();
        }

        List<Throwable> chain = new ArrayList<>();
        java.util.Set<Throwable> seen = java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<>());
        Throwable current = throwable;

        while (current != null && seen.add(current)) {
            chain.add(current);
            current = current.getCause();
        }

        return Collections.unmodifiableList(chain);
    }

    /**
     * Unwraps a nested exception by returning its cause.
     * 解包嵌套异常，返回其原因。
     *
     * <p>If the exception wraps another (e.g., RuntimeException wrapping IOException),
     * returns the wrapped exception. If there is no cause, returns the original.</p>
     * <p>如果异常是包装类型（如 RuntimeException 包装了 IOException），
     * 则返回被包装的异常。如果没有 cause，返回原异常。</p>
     *
     * @param throwable the exception to unwrap | 待解包的异常
     * @return the cause, or the original exception if no cause exists | 原因异常，如果没有 cause 则返回原异常
     */
    public static Throwable unwrap(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        Throwable cause = throwable.getCause();
        return cause != null ? cause : throwable;
    }

    /**
     * Unwraps an exception of a specific type from the causal chain.
     * 从异常链中解包特定类型的异常。
     *
     * <p>Traverses the causal chain and returns the first exception matching the given type.</p>
     * <p>遍历异常链，找到并返回指定类型的异常。</p>
     *
     * @param throwable     the exception to search from | 起始异常
     * @param exceptionType the target exception type to find | 目标异常类型
     * @param <T>           the exception type | 异常类型
     * @return the found exception, or null if not found | 找到的异常，如果未找到则返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T unwrap(Throwable throwable, Class<T> exceptionType) {
        if (throwable == null || exceptionType == null) {
            return null;
        }

        Throwable current = throwable;
        while (current != null) {
            if (exceptionType.isInstance(current)) {
                return (T) current;
            }
            Throwable cause = current.getCause();
            if (cause == current) {
                break;
            }
            current = cause;
        }
        return null;
    }

    /**
     * Wraps a checked exception as a runtime exception and throws it.
     * 包装受检异常为运行时异常并抛出。
     *
     * <p>Executes the given runnable. If a checked exception is thrown, it is wrapped
     * in an {@link OpenException}. RuntimeExceptions pass through unwrapped.</p>
     * <p>执行可能抛出受检异常的代码，如果发生受检异常则包装为 {@link OpenException}。
     * RuntimeException 直接抛出不包装。</p>
     *
     * @param runnable the code block that may throw checked exceptions | 可能抛出受检异常的代码块
     * @throws OpenException if a checked exception occurs | 如果发生受检异常
     */
    public static void wrapAndThrow(CheckedRunnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenException("Core", "WRAPPED_EXCEPTION", e.getMessage(), e);
        }
    }

    /**
     * Wraps a checked exception as a runtime exception and returns the result.
     * 包装受检异常为运行时异常并返回结果。
     *
     * <p>Executes the given supplier. If a checked exception is thrown, it is wrapped
     * in an {@link OpenException}. RuntimeExceptions pass through unwrapped.</p>
     * <p>执行可能抛出受检异常的代码块并返回结果。如果发生受检异常则包装为 {@link OpenException}。</p>
     *
     * @param supplier the code block that may throw checked exceptions | 可能抛出受检异常的代码块
     * @param <T>      the return type | 返回值类型
     * @return the execution result | 执行结果
     * @throws OpenException if a checked exception occurs | 如果发生受检异常
     */
    public static <T> T wrapAndReturn(CheckedSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenException("Core", "WRAPPED_EXCEPTION", e.getMessage(), e);
        }
    }

    /**
     * Silently throws a checked exception
     * 静默抛出受检异常
     * <p>
     * 利用泛型擦除机制，将受检异常作为运行时异常抛出，
     * 而不需要在方法签名中声明。
     *
     * <p><strong>警告</strong></p>
     * 谨慎使用此方法，因为它绕过了编译器的异常检查。
     * 仅在确实需要抛出原始受检异常时使用。
     *
     * @param throwable the exception to throw | 要抛出的异常
     * @param <T>       the disguised exception type | 伪装的异常类型
     * @return never returns, declared only to satisfy return type requirements |
     *         永不返回，仅用于满足返回类型要求
     * @throws T always thrown | 实际抛出的异常
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException sneakyThrow(Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * Checks if the causal chain contains an exception of the given type.
     * 判断异常链中是否包含指定类型的异常。
     *
     * @param throwable     the exception to check | 待检查的异常
     * @param exceptionType the target exception type | 目标异常类型
     * @return true if the chain contains the type | 如果异常链包含该类型则返回 true
     */
    public static boolean contains(Throwable throwable, Class<? extends Throwable> exceptionType) {
        return unwrap(throwable, exceptionType) != null;
    }

    /**
     * Finds the first exception of the given type in the causal chain.
     * 在异常链中查找第一个指定类型的异常。
     *
     * @param throwable     the exception to search from | 起始异常
     * @param exceptionType the type to find | 目标异常类型
     * @param <T>           the exception type | 异常类型
     * @return Optional containing the found exception, or empty if not found |
     *         包含找到的异常的 Optional，未找到则为空
     */
    public static <T extends Throwable> Optional<T> findCause(Throwable throwable, Class<T> exceptionType) {
        return Optional.ofNullable(unwrap(throwable, exceptionType));
    }

    /**
     * Checks if the throwable itself or any of its causes is of the given type.
     * 检查异常本身或其原因链中是否存在指定类型的异常。
     *
     * <p>Semantically equivalent to {@link #contains(Throwable, Class)} with a
     * more descriptive name.</p>
     * <p>语义上等同于 {@link #contains(Throwable, Class)}，但名称更具描述性。</p>
     *
     * @param throwable     the exception to check | 待检查的异常
     * @param exceptionType the type to look for | 目标异常类型
     * @return true if the throwable or any cause matches | 如果异常或任何原因匹配则返回 true
     */
    public static boolean isOrCausedBy(Throwable throwable, Class<? extends Throwable> exceptionType) {
        return contains(throwable, exceptionType);
    }

    /**
     * Gets the exception message, falling back to the class name if null.
     * 获取异常消息，如果为 null 则返回异常类名。
     *
     * @param throwable the exception | 异常
     * @return the message or class name | 异常消息或类名
     */
    public static String getMessage(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        String message = throwable.getMessage();
        return message != null ? message : throwable.getClass().getName();
    }

    /**
     * Gets the root cause message.
     * 获取根本原因的消息。
     *
     * @param throwable the exception | 异常
     * @return the root cause message | 根本原因的消息
     */
    public static String getRootCauseMessage(Throwable throwable) {
        return getMessage(getRootCause(throwable));
    }

    /**
     * Deprecated bridge interface for backward compatibility
     * 已弃用的桥接接口，用于向后兼容
     *
     * <p>Use {@link cloud.opencode.base.core.func.CheckedRunnable} instead.</p>
     * <p>请使用 {@link cloud.opencode.base.core.func.CheckedRunnable} 替代。</p>
     *
     * @deprecated since 1.0.3, use {@link cloud.opencode.base.core.func.CheckedRunnable} instead.
     *             Will be removed in a future version.
     *             自 1.0.3 起弃用，请使用 {@link cloud.opencode.base.core.func.CheckedRunnable} 替代，
     *             将在未来版本中移除。
     */
    @Deprecated(since = "1.0.3", forRemoval = true)
    @FunctionalInterface
    public interface CheckedRunnable extends cloud.opencode.base.core.func.CheckedRunnable {
    }

    /**
     * Deprecated bridge interface for backward compatibility
     * 已弃用的桥接接口，用于向后兼容
     *
     * <p>Use {@link cloud.opencode.base.core.func.CheckedSupplier} instead.</p>
     * <p>请使用 {@link cloud.opencode.base.core.func.CheckedSupplier} 替代。</p>
     *
     * @param <T> return type - 返回值类型
     * @deprecated since 1.0.3, use {@link cloud.opencode.base.core.func.CheckedSupplier} instead.
     *             Will be removed in a future version.
     *             自 1.0.3 起弃用，请使用 {@link cloud.opencode.base.core.func.CheckedSupplier} 替代，
     *             将在未来版本中移除。
     */
    @Deprecated(since = "1.0.3", forRemoval = true)
    @FunctionalInterface
    public interface CheckedSupplier<T> extends cloud.opencode.base.core.func.CheckedSupplier<T> {
    }

}
