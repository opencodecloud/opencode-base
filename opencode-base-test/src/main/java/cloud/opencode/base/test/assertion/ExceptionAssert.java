package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Exception Assert - Fluent assertions for exceptions
 * 异常断言 - 异常的流式断言
 *
 * <p>Provides comprehensive assertion methods for exception testing.</p>
 * <p>为异常测试提供全面的断言方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Assert exception type, message, and cause - 断言异常类型、消息和原因</li>
 *   <li>Assert code does not throw - 断言代码不抛出异常</li>
 *   <li>Root cause inspection - 根因检查</li>
 *   <li>Fluent chaining API - 流式链式API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ExceptionAssert.assertThatThrownBy(() -> {
 *         throw new IllegalArgumentException("Invalid input");
 *     })
 *     .isInstanceOf(IllegalArgumentException.class)
 *     .hasMessage("Invalid input")
 *     .hasNoCause();
 *
 * ExceptionAssert.assertThatCode(() -> {
 *         // safe code
 *     })
 *     .doesNotThrowAnyException();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not designed for concurrent use) - 线程安全: 否（非设计用于并发使用）</li>
 *   <li>Null-safe: Yes (handles null throwable gracefully) - 空值安全: 是（优雅处理空异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class ExceptionAssert {

    private final Throwable throwable;
    private final ThrowableRunner runner;

    private ExceptionAssert(Throwable throwable, ThrowableRunner runner) {
        this.throwable = throwable;
        this.runner = runner;
    }

    /**
     * Functional interface for code that may throw.
     * 可能抛出异常的代码的函数式接口。
     */
    @FunctionalInterface
    public interface ThrowableRunner {
        void run() throws Throwable;
    }

    /**
     * Creates assertion expecting exception to be thrown.
     * 创建期望抛出异常的断言。
     *
     * @param runner the code to execute | 要执行的代码
     * @return the assertion | 断言
     */
    public static ExceptionAssert assertThatThrownBy(ThrowableRunner runner) {
        try {
            runner.run();
            throw new AssertionException("Expected exception to be thrown but nothing was thrown");
        } catch (AssertionException e) {
            throw e;
        } catch (Throwable t) {
            return new ExceptionAssert(t, runner);
        }
    }

    /**
     * Creates assertion for exception.
     * 为异常创建断言。
     *
     * @param throwable the throwable | 异常
     * @return the assertion | 断言
     */
    public static ExceptionAssert assertThat(Throwable throwable) {
        return new ExceptionAssert(throwable, null);
    }

    /**
     * Creates assertion for code that should not throw.
     * 创建不应抛出异常的代码的断言。
     *
     * @param runner the code to execute | 要执行的代码
     * @return the assertion | 断言
     */
    public static ExceptionAssert assertThatCode(ThrowableRunner runner) {
        return new ExceptionAssert(null, runner);
    }

    /**
     * Asserts that no exception is thrown.
     * 断言不抛出异常。
     *
     * @return this | 此对象
     */
    public ExceptionAssert doesNotThrowAnyException() {
        if (runner != null) {
            try {
                runner.run();
            } catch (Throwable t) {
                throw new AssertionException("Expected no exception but got: " + t.getClass().getName() + ": " + t.getMessage(), t);
            }
        }
        if (throwable != null) {
            throw new AssertionException("Expected no exception but had: " + throwable.getClass().getName());
        }
        return this;
    }

    /**
     * Asserts that exception is instance of.
     * 断言异常是指定类型。
     *
     * @param expectedType the expected type | 期望类型
     * @return this | 此对象
     */
    public ExceptionAssert isInstanceOf(Class<? extends Throwable> expectedType) {
        if (throwable == null) {
            throw new AssertionException("Expected exception of type " + expectedType.getName() + " but none was thrown");
        }
        if (!expectedType.isInstance(throwable)) {
            throw new AssertionException("Expected " + expectedType.getName() + " but was " + throwable.getClass().getName());
        }
        return this;
    }

    /**
     * Asserts that exception is exactly of type.
     * 断言异常恰好是指定类型。
     *
     * @param expectedType the expected type | 期望类型
     * @return this | 此对象
     */
    public ExceptionAssert isExactlyInstanceOf(Class<? extends Throwable> expectedType) {
        if (throwable == null) {
            throw new AssertionException("Expected exception of type " + expectedType.getName() + " but none was thrown");
        }
        if (throwable.getClass() != expectedType) {
            throw new AssertionException("Expected exactly " + expectedType.getName() + " but was " + throwable.getClass().getName());
        }
        return this;
    }

    /**
     * Asserts that exception has message.
     * 断言异常有指定消息。
     *
     * @param expectedMessage the expected message | 期望消息
     * @return this | 此对象
     */
    public ExceptionAssert hasMessage(String expectedMessage) {
        if (throwable == null) {
            throw new AssertionException("No exception to check message");
        }
        if (!Objects.equals(throwable.getMessage(), expectedMessage)) {
            throw new AssertionException("Expected message '" + expectedMessage + "' but was '" + throwable.getMessage() + "'");
        }
        return this;
    }

    /**
     * Asserts that exception message contains.
     * 断言异常消息包含。
     *
     * @param substring the substring | 子字符串
     * @return this | 此对象
     */
    public ExceptionAssert hasMessageContaining(String substring) {
        if (throwable == null) {
            throw new AssertionException("No exception to check message");
        }
        String message = throwable.getMessage();
        if (message == null || !message.contains(substring)) {
            throw new AssertionException("Expected message containing '" + substring + "' but was '" + message + "'");
        }
        return this;
    }

    /**
     * Asserts that exception message starts with.
     * 断言异常消息以...开始。
     *
     * @param prefix the prefix | 前缀
     * @return this | 此对象
     */
    public ExceptionAssert hasMessageStartingWith(String prefix) {
        if (throwable == null) {
            throw new AssertionException("No exception to check message");
        }
        String message = throwable.getMessage();
        if (message == null || !message.startsWith(prefix)) {
            throw new AssertionException("Expected message starting with '" + prefix + "' but was '" + message + "'");
        }
        return this;
    }

    /**
     * Asserts that exception message ends with.
     * 断言异常消息以...结束。
     *
     * @param suffix the suffix | 后缀
     * @return this | 此对象
     */
    public ExceptionAssert hasMessageEndingWith(String suffix) {
        if (throwable == null) {
            throw new AssertionException("No exception to check message");
        }
        String message = throwable.getMessage();
        if (message == null || !message.endsWith(suffix)) {
            throw new AssertionException("Expected message ending with '" + suffix + "' but was '" + message + "'");
        }
        return this;
    }

    /**
     * Asserts that exception has cause.
     * 断言异常有原因。
     *
     * @return this | 此对象
     */
    public ExceptionAssert hasCause() {
        if (throwable == null) {
            throw new AssertionException("No exception to check cause");
        }
        if (throwable.getCause() == null) {
            throw new AssertionException("Expected cause but was null");
        }
        return this;
    }

    /**
     * Asserts that exception has no cause.
     * 断言异常没有原因。
     *
     * @return this | 此对象
     */
    public ExceptionAssert hasNoCause() {
        if (throwable == null) {
            throw new AssertionException("No exception to check cause");
        }
        if (throwable.getCause() != null) {
            throw new AssertionException("Expected no cause but had: " + throwable.getCause().getClass().getName());
        }
        return this;
    }

    /**
     * Asserts that exception has cause of type.
     * 断言异常有指定类型的原因。
     *
     * @param causeType the cause type | 原因类型
     * @return this | 此对象
     */
    public ExceptionAssert hasCauseInstanceOf(Class<? extends Throwable> causeType) {
        if (throwable == null) {
            throw new AssertionException("No exception to check cause");
        }
        Throwable cause = throwable.getCause();
        if (cause == null) {
            throw new AssertionException("Expected cause of type " + causeType.getName() + " but was null");
        }
        if (!causeType.isInstance(cause)) {
            throw new AssertionException("Expected cause of type " + causeType.getName() + " but was " + cause.getClass().getName());
        }
        return this;
    }

    /**
     * Asserts that exception has root cause of type.
     * 断言异常有指定类型的根原因。
     *
     * @param rootCauseType the root cause type | 根原因类型
     * @return this | 此对象
     */
    public ExceptionAssert hasRootCauseInstanceOf(Class<? extends Throwable> rootCauseType) {
        if (throwable == null) {
            throw new AssertionException("No exception to check root cause");
        }
        Throwable rootCause = getRootCause(throwable);
        if (!rootCauseType.isInstance(rootCause)) {
            throw new AssertionException("Expected root cause of type " + rootCauseType.getName() + " but was " + rootCause.getClass().getName());
        }
        return this;
    }

    /**
     * Gets the thrown exception for further inspection.
     * 获取抛出的异常以进行进一步检查。
     *
     * @param <T> the expected type | 期望类型
     * @return the throwable | 异常
     */
    @SuppressWarnings("unchecked")
    public <T extends Throwable> T getThrowable() {
        return (T) throwable;
    }

    private Throwable getRootCause(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root;
    }
}
