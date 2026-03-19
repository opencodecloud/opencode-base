package cloud.opencode.base.test.fixture;

import java.util.function.Supplier;

/**
 * Test Fixture
 * 测试夹具
 *
 * <p>A reusable test fixture that can be set up and torn down.</p>
 * <p>可重用的测试夹具，可以设置和拆除。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Reusable test data fixtures - 可重用的测试数据夹具</li>
 *   <li>Setup and teardown lifecycle - 设置和拆卸生命周期</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TestFixture<User> fixture = TestFixture.of(() -> new User("test"));
 * User user = fixture.get();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 *
 * @param <T> the fixture data type | 夹具数据类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public class TestFixture<T> {

    private final String name;
    private final Supplier<T> setup;
    private final java.util.function.Consumer<T> teardown;
    private T data;
    private boolean initialized;

    /**
     * Create test fixture
     * 创建测试夹具
     *
     * @param name the fixture name | 夹具名称
     * @param setup the setup function | 设置函数
     */
    public TestFixture(String name, Supplier<T> setup) {
        this(name, setup, null);
    }

    /**
     * Create test fixture with teardown
     * 创建带拆除的测试夹具
     *
     * @param name the fixture name | 夹具名称
     * @param setup the setup function | 设置函数
     * @param teardown the teardown function | 拆除函数
     */
    public TestFixture(String name, Supplier<T> setup, java.util.function.Consumer<T> teardown) {
        this.name = name;
        this.setup = setup;
        this.teardown = teardown;
        this.initialized = false;
    }

    /**
     * Create fixture builder
     * 创建夹具构建器
     *
     * @param name the fixture name | 夹具名称
     * @param <T> the data type | 数据类型
     * @return the builder | 构建器
     */
    public static <T> Builder<T> builder(String name) {
        return new Builder<>(name);
    }

    /**
     * Get fixture name
     * 获取夹具名称
     *
     * @return the name | 名称
     */
    public String getName() {
        return name;
    }

    /**
     * Set up the fixture
     * 设置夹具
     *
     * @return the fixture data | 夹具数据
     */
    public T setUp() {
        if (!initialized) {
            data = setup.get();
            initialized = true;
        }
        return data;
    }

    /**
     * Get the fixture data (alias for setUp)
     * 获取夹具数据（setUp的别名）
     *
     * @return the fixture data | 夹具数据
     */
    public T get() {
        return setUp();
    }

    /**
     * Tear down the fixture
     * 拆除夹具
     */
    public void tearDown() {
        if (initialized && teardown != null) {
            teardown.accept(data);
        }
        data = null;
        initialized = false;
    }

    /**
     * Check if initialized
     * 检查是否已初始化
     *
     * @return true if initialized | 如果已初始化返回true
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Reset the fixture
     * 重置夹具
     */
    public void reset() {
        tearDown();
    }

    /**
     * Builder for TestFixture
     * TestFixture的构建器
     *
     * @param <T> the data type | 数据类型
     */
    public static class Builder<T> {
        private final String name;
        private Supplier<T> setup;
        private java.util.function.Consumer<T> teardown;

        private Builder(String name) {
            this.name = name;
        }

        /**
         * Set the setup function
         * 设置setup函数
         *
         * @param setup the setup function | 设置函数
         * @return this builder | 此构建器
         */
        public Builder<T> setup(Supplier<T> setup) {
            this.setup = setup;
            return this;
        }

        /**
         * Set the teardown function
         * 设置teardown函数
         *
         * @param teardown the teardown function | 拆除函数
         * @return this builder | 此构建器
         */
        public Builder<T> teardown(java.util.function.Consumer<T> teardown) {
            this.teardown = teardown;
            return this;
        }

        /**
         * Build the fixture
         * 构建夹具
         *
         * @return the fixture | 夹具
         */
        public TestFixture<T> build() {
            if (setup == null) {
                throw new IllegalStateException("Setup function is required");
            }
            return new TestFixture<>(name, setup, teardown);
        }
    }
}
