/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.test;

import java.lang.ScopedValue.CallableOp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Test Context - ScopedValue-based Test Execution Context
 * 测试上下文 - 基于 ScopedValue 的测试执行上下文
 *
 * <p>Uses JDK 25 ScopedValue for efficient context propagation in test execution.
 * Provides thread-safe variable storage, lifecycle hooks, and metadata management.</p>
 * <p>使用 JDK 25 ScopedValue 实现测试执行中高效的上下文传播。
 * 提供线程安全的变量存储、生命周期钩子和元数据管理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ScopedValue-based context propagation - 基于ScopedValue的上下文传播</li>
 *   <li>Thread-safe variable and attribute storage - 线程安全的变量和属性存储</li>
 *   <li>Lifecycle hooks (success/failure callbacks) - 生命周期钩子（成功/失败回调）</li>
 *   <li>Test timing and status tracking - 测试计时和状态跟踪</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * // Create new test context
 * TestContext context = TestContext.create("myTestName");
 *
 * // Register callbacks
 * context.onSuccess(ctx -> System.out.println("Test passed!"));
 * context.onFailure((ctx, ex) -> System.out.println("Test failed: " + ex));
 *
 * // Run test within context
 * TestContext.run(context, () -> {
 *     // Access current context
 *     TestContext current = TestContext.current().orElseThrow();
 *     current.setVariable("key", "value");
 *     current.setAttribute("component", "UserService");
 *
 *     // Perform test assertions...
 *     return result;
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap for variables/attributes, synchronized callbacks) - 线程安全: 是（变量/属性使用ConcurrentHashMap，回调使用synchronized）</li>
 *   <li>Null-safe: Yes (validates non-null inputs) - 空值安全: 是（验证非空输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class TestContext {

    /**
     * Current test context (ScopedValue)
     * 当前测试上下文 (ScopedValue)
     */
    public static final ScopedValue<TestContext> CURRENT = ScopedValue.newInstance();

    private final String testName;
    private final Instant startTime;
    private final Map<String, Object> variables;
    private final Map<String, Object> attributes;
    private final List<Consumer<TestContext>> successCallbacks;
    private final List<FailureCallback> failureCallbacks;

    /**
     * Failure callback functional interface
     * 失败回调函数式接口
     */
    @FunctionalInterface
    public interface FailureCallback {
        /**
         * Called when test fails
         * 测试失败时调用
         *
         * @param context   the test context | 测试上下文
         * @param throwable the exception that caused failure | 导致失败的异常
         */
        void onFailure(TestContext context, Throwable throwable);
    }

    private TestContext(String testName) {
        this.testName = Objects.requireNonNull(testName, "testName must not be null");
        this.startTime = Instant.now();
        this.variables = new ConcurrentHashMap<>();
        this.attributes = new ConcurrentHashMap<>();
        this.successCallbacks = new ArrayList<>();
        this.failureCallbacks = new ArrayList<>();
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a new test context with test name
     * 使用测试名称创建新的测试上下文
     *
     * @param testName the test name | 测试名称
     * @return new test context | 新的测试上下文
     */
    public static TestContext create(String testName) {
        return new TestContext(testName);
    }

    /**
     * Creates a new test context with default name
     * 使用默认名称创建新的测试上下文
     *
     * @return new test context | 新的测试上下文
     */
    public static TestContext create() {
        return new TestContext("test-" + System.currentTimeMillis());
    }

    // ==================== Context Execution | 上下文执行 ====================

    /**
     * Runs a callable within this context
     * 在此上下文中运行可调用对象
     *
     * @param context the test context | 测试上下文
     * @param task    the task to run | 要运行的任务
     * @param <T>     return type | 返回类型
     * @param <X>     exception type | 异常类型
     * @return task result | 任务结果
     * @throws X if task throws | 如果任务抛出异常
     */
    public static <T, X extends Throwable> T run(TestContext context, CallableOp<T, X> task) throws X {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(task, "task must not be null");

        context.beforeTest();
        try {
            T result = ScopedValue.where(CURRENT, context).call(task);
            context.afterTestSuccess();
            return result;
        } catch (Throwable t) {
            context.afterTestFailure(t);
            throw t;
        }
    }

    /**
     * Runs a runnable within this context
     * 在此上下文中运行可运行对象
     *
     * @param context the test context | 测试上下文
     * @param task    the task to run | 要运行的任务
     */
    public static void run(TestContext context, Runnable task) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(task, "task must not be null");

        context.beforeTest();
        try {
            ScopedValue.where(CURRENT, context).run(task);
            context.afterTestSuccess();
        } catch (Throwable t) {
            context.afterTestFailure(t);
            throw t;
        }
    }

    // ==================== Current Context | 当前上下文 ====================

    /**
     * Gets the current test context
     * 获取当前测试上下文
     *
     * @return current context or empty | 当前上下文或空
     */
    public static Optional<TestContext> current() {
        return CURRENT.isBound() ? Optional.of(CURRENT.get()) : Optional.empty();
    }

    /**
     * Gets the current context or creates a new one
     * 获取当前上下文或创建新的
     *
     * @return current or new context | 当前或新的上下文
     */
    public static TestContext currentOrCreate() {
        return current().orElseGet(TestContext::create);
    }

    /**
     * Gets the current context or creates one with specified name
     * 获取当前上下文或使用指定名称创建新的
     *
     * @param testName the test name for new context | 新上下文的测试名称
     * @return current or new context | 当前或新的上下文
     */
    public static TestContext currentOrCreate(String testName) {
        return current().orElseGet(() -> create(testName));
    }

    // ==================== Variables | 变量 ====================

    /**
     * Sets a variable in the context
     * 在上下文中设置变量
     *
     * @param key   the variable key | 变量键
     * @param value the variable value | 变量值
     * @return this context for chaining | 此上下文用于链式调用
     */
    public TestContext setVariable(String key, Object value) {
        Objects.requireNonNull(key, "key must not be null");
        variables.put(key, value);
        return this;
    }

    /**
     * Gets a variable from the context
     * 从上下文中获取变量
     *
     * @param key the variable key | 变量键
     * @param <T> the expected type | 预期类型
     * @return the variable value or empty | 变量值或空
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getVariable(String key) {
        return Optional.ofNullable((T) variables.get(key));
    }

    /**
     * Gets a variable with default value
     * 获取变量，带默认值
     *
     * @param key          the variable key | 变量键
     * @param defaultValue the default value | 默认值
     * @param <T>          the expected type | 预期类型
     * @return the variable value or default | 变量值或默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key, T defaultValue) {
        return (T) variables.getOrDefault(key, defaultValue);
    }

    /**
     * Checks if a variable exists
     * 检查变量是否存在
     *
     * @param key the variable key | 变量键
     * @return true if variable exists | 如果变量存在返回 true
     */
    public boolean hasVariable(String key) {
        return variables.containsKey(key);
    }

    /**
     * Removes a variable from the context
     * 从上下文中移除变量
     *
     * @param key the variable key | 变量键
     * @return this context for chaining | 此上下文用于链式调用
     */
    public TestContext removeVariable(String key) {
        variables.remove(key);
        return this;
    }

    /**
     * Gets all variables as unmodifiable map
     * 获取所有变量（不可修改的映射）
     *
     * @return all variables | 所有变量
     */
    public Map<String, Object> variables() {
        return Map.copyOf(variables);
    }

    // ==================== Attributes | 属性 ====================

    /**
     * Sets an attribute (metadata) in the context
     * 在上下文中设置属性（元数据）
     *
     * @param key   the attribute key | 属性键
     * @param value the attribute value | 属性值
     * @return this context for chaining | 此上下文用于链式调用
     */
    public TestContext setAttribute(String key, Object value) {
        Objects.requireNonNull(key, "key must not be null");
        attributes.put(key, value);
        return this;
    }

    /**
     * Gets an attribute from the context
     * 从上下文中获取属性
     *
     * @param key the attribute key | 属性键
     * @param <T> the expected type | 预期类型
     * @return the attribute value or empty | 属性值或空
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAttribute(String key) {
        return Optional.ofNullable((T) attributes.get(key));
    }

    /**
     * Gets an attribute with default value
     * 获取属性，带默认值
     *
     * @param key          the attribute key | 属性键
     * @param defaultValue the default value | 默认值
     * @param <T>          the expected type | 预期类型
     * @return the attribute value or default | 属性值或默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }

    /**
     * Checks if an attribute exists
     * 检查属性是否存在
     *
     * @param key the attribute key | 属性键
     * @return true if attribute exists | 如果属性存在返回 true
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * Gets all attributes as unmodifiable map
     * 获取所有属性（不可修改的映射）
     *
     * @return all attributes | 所有属性
     */
    public Map<String, Object> attributes() {
        return Map.copyOf(attributes);
    }

    // ==================== Callbacks | 回调 ====================

    /**
     * Registers a success callback
     * 注册成功回调
     *
     * @param callback the callback to invoke on success | 成功时调用的回调
     * @return this context for chaining | 此上下文用于链式调用
     */
    public TestContext onSuccess(Consumer<TestContext> callback) {
        Objects.requireNonNull(callback, "callback must not be null");
        synchronized (successCallbacks) {
            successCallbacks.add(callback);
        }
        return this;
    }

    /**
     * Registers a failure callback
     * 注册失败回调
     *
     * @param callback the callback to invoke on failure | 失败时调用的回调
     * @return this context for chaining | 此上下文用于链式调用
     */
    public TestContext onFailure(FailureCallback callback) {
        Objects.requireNonNull(callback, "callback must not be null");
        synchronized (failureCallbacks) {
            failureCallbacks.add(callback);
        }
        return this;
    }

    // ==================== Lifecycle Hooks | 生命周期钩子 ====================

    /**
     * Called before test execution (internal)
     * 测试执行前调用（内部）
     */
    private void beforeTest() {
        setAttribute("_status", "running");
    }

    /**
     * Called after successful test execution (internal)
     * 成功测试执行后调用（内部）
     */
    private void afterTestSuccess() {
        setAttribute("_status", "success");
        setAttribute("_endTime", Instant.now());

        List<Consumer<TestContext>> callbacks;
        synchronized (successCallbacks) {
            callbacks = new ArrayList<>(successCallbacks);
        }
        for (Consumer<TestContext> callback : callbacks) {
            try {
                callback.accept(this);
            } catch (Exception e) {
                // Log but don't propagate callback exceptions
            }
        }
    }

    /**
     * Called after failed test execution (internal)
     * 失败测试执行后调用（内部）
     *
     * @param throwable the exception that caused failure | 导致失败的异常
     */
    private void afterTestFailure(Throwable throwable) {
        setAttribute("_status", "failure");
        setAttribute("_endTime", Instant.now());
        setAttribute("_exception", throwable);

        List<FailureCallback> callbacks;
        synchronized (failureCallbacks) {
            callbacks = new ArrayList<>(failureCallbacks);
        }
        for (FailureCallback callback : callbacks) {
            try {
                callback.onFailure(this, throwable);
            } catch (Exception e) {
                // Log but don't propagate callback exceptions
            }
        }
    }

    // ==================== Getters | 获取器 ====================

    /**
     * Gets the test name
     * 获取测试名称
     *
     * @return test name | 测试名称
     */
    public String testName() {
        return testName;
    }

    /**
     * Gets the start time
     * 获取开始时间
     *
     * @return start time | 开始时间
     */
    public Instant startTime() {
        return startTime;
    }

    /**
     * Gets the end time if test has completed
     * 获取结束时间（如果测试已完成）
     *
     * @return end time or empty | 结束时间或空
     */
    public Optional<Instant> endTime() {
        return getAttribute("_endTime");
    }

    /**
     * Gets the test duration
     * 获取测试持续时间
     *
     * @return duration from start to now or end time | 从开始到现在或结束时间的持续时间
     */
    public Duration duration() {
        Instant end = endTime().orElse(Instant.now());
        return Duration.between(startTime, end);
    }

    /**
     * Gets the test status
     * 获取测试状态
     *
     * @return test status (pending, running, success, failure) | 测试状态
     */
    public String status() {
        return getAttribute("_status", "pending");
    }

    /**
     * Checks if test passed
     * 检查测试是否通过
     *
     * @return true if test succeeded | 如果测试成功返回 true
     */
    public boolean isPassed() {
        return "success".equals(status());
    }

    /**
     * Checks if test failed
     * 检查测试是否失败
     *
     * @return true if test failed | 如果测试失败返回 true
     */
    public boolean isFailed() {
        return "failure".equals(status());
    }

    /**
     * Gets the failure exception if test failed
     * 获取失败异常（如果测试失败）
     *
     * @return the exception or empty | 异常或空
     */
    public Optional<Throwable> exception() {
        return getAttribute("_exception");
    }

    @Override
    public String toString() {
        return "TestContext[testName=" + testName + ", status=" + status() +
                ", duration=" + duration().toMillis() + "ms]";
    }
}
