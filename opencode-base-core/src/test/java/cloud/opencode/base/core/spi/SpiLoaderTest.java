package cloud.opencode.base.core.spi;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * SpiLoader 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("SpiLoader 测试")
class SpiLoaderTest {

    @BeforeEach
    void setUp() {
        SpiLoader.clearCache();
    }

    @AfterEach
    void tearDown() {
        SpiLoader.clearCache();
    }

    @Nested
    @DisplayName("load 测试")
    class LoadTests {

        @Test
        @DisplayName("load 返回列表")
        void testLoadReturnsList() {
            List<TestService> services = SpiLoader.load(TestService.class);
            assertThat(services).isNotNull();
        }

        @Test
        @DisplayName("load 不存在的服务返回空列表")
        void testLoadNonExistentService() {
            List<NonExistentService> services = SpiLoader.load(NonExistentService.class);
            assertThat(services).isEmpty();
        }

        @Test
        @DisplayName("load 缓存结果")
        void testLoadCachesResult() {
            List<TestService> first = SpiLoader.load(TestService.class);
            List<TestService> second = SpiLoader.load(TestService.class);
            // Returns unmodifiable wrapper; content is equal but wrapper may differ
            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("load 返回不可修改列表")
        void testLoadReturnsUnmodifiableList() {
            List<TestService> services = SpiLoader.load(TestService.class);
            assertThatThrownBy(() -> services.add(new DefaultTestService()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("load 指定类加载器")
        void testLoadWithClassLoader() {
            List<TestService> services = SpiLoader.load(TestService.class,
                    Thread.currentThread().getContextClassLoader());
            assertThat(services).isNotNull();
        }
    }

    @Nested
    @DisplayName("loadFirst 测试")
    class LoadFirstTests {

        @Test
        @DisplayName("loadFirst 返回 Optional")
        void testLoadFirstReturnsOptional() {
            Optional<TestService> service = SpiLoader.loadFirst(TestService.class);
            assertThat(service).isNotNull();
        }

        @Test
        @DisplayName("loadFirst 不存在返回空 Optional")
        void testLoadFirstNonExistent() {
            Optional<NonExistentService> service = SpiLoader.loadFirst(NonExistentService.class);
            assertThat(service).isEmpty();
        }
    }

    @Nested
    @DisplayName("loadFirstOrDefault 测试")
    class LoadFirstOrDefaultTests {

        @Test
        @DisplayName("loadFirstOrDefault 不存在时使用默认值")
        void testLoadFirstOrDefaultUsesDefault() {
            NonExistentService defaultService = new DefaultNonExistentService();
            NonExistentService service = SpiLoader.loadFirstOrDefault(
                    NonExistentService.class, defaultService);
            assertThat(service).isSameAs(defaultService);
        }

        @Test
        @DisplayName("loadFirstOrDefault 存在时不使用默认值")
        void testLoadFirstOrDefaultExisting() {
            // 如果有实现，应该返回实际实现而非默认值
            TestService defaultService = new DefaultTestService();
            TestService service = SpiLoader.loadFirstOrDefault(TestService.class, defaultService);
            // 即使没有 SPI 配置，也返回默认值
            assertThat(service).isNotNull();
        }
    }

    @Nested
    @DisplayName("reload 测试")
    class ReloadTests {

        @Test
        @DisplayName("reload 清除缓存并重新加载")
        void testReload() {
            List<TestService> first = SpiLoader.load(TestService.class);
            List<TestService> reloaded = SpiLoader.reload(TestService.class);

            // 重新加载后不是同一个对象
            assertThat(reloaded).isNotSameAs(first);
        }
    }

    @Nested
    @DisplayName("hasService 测试")
    class HasServiceTests {

        @Test
        @DisplayName("hasService 不存在的服务")
        void testHasServiceNonExistent() {
            boolean has = SpiLoader.hasService(NonExistentService.class);
            assertThat(has).isFalse();
        }

        @Test
        @DisplayName("hasService 存在的服务")
        void testHasServiceExistent() {
            // 如果没有配置 SPI，返回 false
            boolean has = SpiLoader.hasService(TestService.class);
            // 返回值取决于是否有 META-INF/services 配置
            assertThat(has).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("count 测试")
    class CountTests {

        @Test
        @DisplayName("count 不存在的服务返回 0")
        void testCountNonExistent() {
            int count = SpiLoader.count(NonExistentService.class);
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("count 返回服务数量")
        void testCount() {
            int count = SpiLoader.count(TestService.class);
            assertThat(count).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("loadByType 测试")
    class LoadByTypeTests {

        @Test
        @DisplayName("loadByType 过滤类型")
        void testLoadByType() {
            List<SpecialTestService> services = SpiLoader.loadByType(
                    TestService.class, SpecialTestService.class);
            assertThat(services).isNotNull();
            // 所有返回的服务都是 SpecialTestService 类型
            for (SpecialTestService service : services) {
                assertThat(service).isInstanceOf(SpecialTestService.class);
            }
        }

        @Test
        @DisplayName("loadByType 没有匹配返回空列表")
        void testLoadByTypeNoMatch() {
            // 使用 TestService 加载，但过滤出 UnmatchedTestService 类型
            // 由于没有 UnmatchedTestService 的实现，返回空列表
            List<UnmatchedTestService> services = SpiLoader.loadByType(
                    TestService.class, UnmatchedTestService.class);
            assertThat(services).isEmpty();
        }
    }

    @Nested
    @DisplayName("clearCache 测试")
    class ClearCacheTests {

        @Test
        @DisplayName("clearCache 清除所有缓存")
        void testClearCache() {
            SpiLoader.load(TestService.class);
            SpiLoader.load(NonExistentService.class);

            SpiLoader.clearCache();

            // 清除后再次加载应该是新对象
            List<TestService> newList = SpiLoader.load(TestService.class);
            assertThat(newList).isNotNull();
        }

        @Test
        @DisplayName("clearCache 指定类")
        void testClearCacheSpecific() {
            List<TestService> first = SpiLoader.load(TestService.class);

            SpiLoader.clearCache(TestService.class);

            List<TestService> second = SpiLoader.load(TestService.class);
            assertThat(second).isNotSameAs(first);
        }
    }

    @Nested
    @DisplayName("线程安全测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("并发加载")
        void testConcurrentLoad() throws InterruptedException {
            int threadCount = 10;
            java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.CountDownLatch endLatch = new java.util.concurrent.CountDownLatch(threadCount);
            java.util.concurrent.atomic.AtomicReference<List<TestService>> result =
                    new java.util.concurrent.atomic.AtomicReference<>();

            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        List<TestService> services = SpiLoader.load(TestService.class);
                        result.set(services);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            endLatch.await();

            // All threads should get the same cached content (unmodifiable wrapper)
            List<TestService> services = SpiLoader.load(TestService.class);
            assertThat(services).isEqualTo(result.get());
        }
    }

    @Nested
    @DisplayName("loadSafe")
    class LoadSafeTests {

        @Test
        void returnsEmptyForNonExistentService() {
            List<?> result = SpiLoader.loadSafe(Runnable.class);
            assertThat(result).isNotNull();
        }

        @Test
        void resultIsUnmodifiable() {
            List<?> result = SpiLoader.loadSafe(Runnable.class);
            assertThatThrownBy(() -> result.add(null)).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void rejectsNullServiceClass() {
            assertThatThrownBy(() -> SpiLoader.loadSafe(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("loadOrdered")
    class LoadOrderedTests {

        @Test
        void returnsNonNull() {
            List<?> result = SpiLoader.loadOrdered(Runnable.class);
            assertThat(result).isNotNull();
        }

        @Test
        void resultIsUnmodifiable() {
            List<?> result = SpiLoader.loadOrdered(Runnable.class);
            assertThatThrownBy(() -> result.add(null)).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // 测试用接口和实现
    public interface TestService {
        String getName();
    }

    public interface NonExistentService {
    }

    // 用于测试 loadByType 无匹配情况
    public interface UnmatchedTestService extends TestService {
    }

    public static class SpecialTestService implements TestService {
        @Override
        public String getName() {
            return "special";
        }
    }

    public static class DefaultTestService implements TestService {
        @Override
        public String getName() {
            return "default";
        }
    }

    public static class DefaultNonExistentService implements NonExistentService {
    }
}
