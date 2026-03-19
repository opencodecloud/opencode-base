package cloud.opencode.base.core.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
     * Gets
     * 获取异常的根本原因
     * <p>
     * 遍历异常链，返回最底层的异常（没有 cause 的异常）。
     *
     * @param throwable the exception | 异常
     * @return the result | 根本原因，如果输入为 null 则返回 null
     */
    public static Throwable getRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Gets
     * 获取异常的堆栈跟踪字符串
     *
     * @param throwable the exception | 异常
     * @return the result | 堆栈跟踪字符串，如果输入为 null 则返回空字符串
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
     * Gets
     * 获取异常的因果链
     * <p>
     * 返回从当前异常到根本原因的所有异常列表。
     *
     * @param throwable the exception | 异常
     * @return the result | 异常链列表（从当前异常到根本原因），如果输入为 null 则返回空列表
     */
    public static List<Throwable> getCausalChain(Throwable throwable) {
        if (throwable == null) {
            return Collections.emptyList();
        }

        List<Throwable> chain = new ArrayList<>();
        Throwable current = throwable;

        while (current != null) {
            chain.add(current);
            Throwable cause = current.getCause();
            // 防止循环引用
            if (cause == current) {
                break;
            }
            current = cause;
        }

        return Collections.unmodifiableList(chain);
    }

    /**
     * Unwraps a nested exception
     * 解包嵌套异常
     * <p>
     * 如果异常是包装类型（如 RuntimeException 包装了 IOException），
     * 则返回被包装的异常。
     *
     * @param throwable the exception | 异常
     * @return the result | 解包后的异常，如果没有 cause 则返回原异常
     */
    public static Throwable unwrap(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        Throwable cause = throwable.getCause();
        return cause != null ? cause : throwable;
    }

    /**
     * Unwraps an exception of a specific type
     * 解包特定类型的异常
     * <p>
     * 遍历异常链，找到并返回指定类型的异常。
     *
     * @param throwable the exception | 异常
     * @param exceptionType the value | 目标异常类型
     * @param <T> the value | 异常类型
     * @return the result | 找到的异常，如果未找到则返回 null
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
     * Wraps
     * 包装受检异常为运行时异常并抛出
     * <p>
     * 执行可能抛出受检异常的代码，如果发生异常则包装为 {@link OpenException}。
     *
     * @param runnable the value | 可能抛出受检异常的代码块
     * @throws OpenException if the condition is not met | 如果发生异常
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
     * Wraps
     * 包装受检异常为运行时异常并返回结果
     *
     * @param supplier the value | 可能抛出受检异常的代码块
     * @param <T> the value | 返回值类型
     * @return the result | 执行结果
     * @throws OpenException if the condition is not met | 如果发生异常
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
     * <h3>警告</h3>
     * 谨慎使用此方法，因为它绕过了编译器的异常检查。
     * 仅在确实需要抛出原始受检异常时使用。
     *
     * @param throwable the value | 要抛出的异常
     * @param <T> the value | 伪装的异常类型
     * @return the result | 永不返回，仅用于满足返回类型要求
     * @throws T if the condition is not met | 实际抛出的异常
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException sneakyThrow(Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * Checks
     * 判断异常链中是否包含指定类型的异常
     *
     * @param throwable the exception | 异常
     * @param exceptionType the value | 目标异常类型
     * @return the result | 如果包含则返回 true
     */
    public static boolean contains(Throwable throwable, Class<? extends Throwable> exceptionType) {
        return unwrap(throwable, exceptionType) != null;
    }

    /**
     * Gets
     * 获取异常消息，如果为 null 则返回异常类名
     *
     * @param throwable the exception | 异常
     * @return the result | 异常消息或类名
     */
    public static String getMessage(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        String message = throwable.getMessage();
        return message != null ? message : throwable.getClass().getName();
    }

    /**
     * Gets
     * 获取根本原因的消息
     *
     * @param throwable the exception | 异常
     * @return the result | 根本原因的消息
     */
    public static String getRootCauseMessage(Throwable throwable) {
        return getMessage(getRootCause(throwable));
    }

    /**
     * A Runnable that may throw checked exceptions
     * 可抛出受检异常的 Runnable
     */
    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }

    /**
     * A Supplier that may throw checked exceptions
     * 可抛出受检异常的 Supplier
     *
     * @param <T> the value | 返回值类型
     */
    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}
