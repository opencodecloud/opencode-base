package cloud.opencode.base.test.fixture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Fixture Registry
 * 夹具注册表
 *
 * <p>Registry for managing test fixtures.</p>
 * <p>管理测试夹具的注册表。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Test fixture registration and lookup - 测试夹具注册和查找</li>
 *   <li>Type-safe fixture management - 类型安全的夹具管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FixtureRegistry registry = new FixtureRegistry();
 * registry.register("user", () -> new User("test"));
 * User user = registry.get("user", User.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (concurrent data structures) - 线程安全: 是（并发数据结构）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class FixtureRegistry {

    private static final Map<String, TestFixture<?>> fixtures = new ConcurrentHashMap<>();

    private FixtureRegistry() {
        // Utility class
    }

    /**
     * Register a fixture
     * 注册夹具
     *
     * @param fixture the fixture | 夹具
     * @param <T> the data type | 数据类型
     */
    public static <T> void register(TestFixture<T> fixture) {
        fixtures.put(fixture.getName(), fixture);
    }

    /**
     * Register a simple fixture
     * 注册简单夹具
     *
     * @param name the fixture name | 夹具名称
     * @param supplier the data supplier | 数据供应者
     * @param <T> the data type | 数据类型
     */
    public static <T> void register(String name, Supplier<T> supplier) {
        fixtures.put(name, new TestFixture<>(name, supplier));
    }

    /**
     * Get a fixture by name
     * 按名称获取夹具
     *
     * @param name the fixture name | 夹具名称
     * @param <T> the data type | 数据类型
     * @return the fixture | 夹具
     */
    @SuppressWarnings("unchecked")
    public static <T> TestFixture<T> get(String name) {
        return (TestFixture<T>) fixtures.get(name);
    }

    /**
     * Get fixture data by name
     * 按名称获取夹具数据
     *
     * @param name the fixture name | 夹具名称
     * @param <T> the data type | 数据类型
     * @return the fixture data | 夹具数据
     */
    @SuppressWarnings("unchecked")
    public static <T> T getData(String name) {
        TestFixture<T> fixture = (TestFixture<T>) fixtures.get(name);
        return fixture != null ? fixture.get() : null;
    }

    /**
     * Check if fixture exists
     * 检查夹具是否存在
     *
     * @param name the fixture name | 夹具名称
     * @return true if exists | 如果存在返回true
     */
    public static boolean exists(String name) {
        return fixtures.containsKey(name);
    }

    /**
     * Unregister a fixture
     * 取消注册夹具
     *
     * @param name the fixture name | 夹具名称
     */
    public static void unregister(String name) {
        TestFixture<?> fixture = fixtures.remove(name);
        if (fixture != null) {
            fixture.tearDown();
        }
    }

    /**
     * Reset all fixtures
     * 重置所有夹具
     */
    public static void resetAll() {
        fixtures.values().forEach(TestFixture::reset);
    }

    /**
     * Tear down all fixtures
     * 拆除所有夹具
     */
    public static void tearDownAll() {
        fixtures.values().forEach(TestFixture::tearDown);
    }

    /**
     * Clear all fixtures
     * 清除所有夹具
     */
    public static void clear() {
        tearDownAll();
        fixtures.clear();
    }

    /**
     * Get fixture count
     * 获取夹具数量
     *
     * @return the count | 数量
     */
    public static int size() {
        return fixtures.size();
    }
}
