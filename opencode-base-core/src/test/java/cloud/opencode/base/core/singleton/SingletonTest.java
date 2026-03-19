package cloud.opencode.base.core.singleton;

import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Singleton 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Singleton 测试")
class SingletonTest {

    @BeforeEach
    void setUp() {
        Singleton.clear();
    }

    @AfterEach
    void tearDown() {
        Singleton.clear();
    }

    @Nested
    @DisplayName("按类型操作测试")
    class TypeBasedTests {

        @Test
        @DisplayName("get 获取已注册实例")
        void testGetRegistered() {
            TestService service = new TestService("test");
            Singleton.register(TestService.class, service);

            TestService result = Singleton.get(TestService.class);
            assertThat(result).isSameAs(service);
        }

        @Test
        @DisplayName("get 未注册返回 null")
        void testGetNotRegistered() {
            TestService result = Singleton.get(TestService.class);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("get 带 Supplier 延迟创建")
        void testGetWithSupplier() {
            TestService result = Singleton.get(TestService.class, () -> new TestService("lazy"));
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("lazy");
        }

        @Test
        @DisplayName("get 带 Supplier 已存在不重新创建")
        void testGetWithSupplierExisting() {
            TestService existing = new TestService("existing");
            Singleton.register(TestService.class, existing);

            AtomicInteger createCount = new AtomicInteger(0);
            TestService result = Singleton.get(TestService.class, () -> {
                createCount.incrementAndGet();
                return new TestService("new");
            });

            assertThat(result).isSameAs(existing);
            assertThat(createCount.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("register 注册实例")
        void testRegister() {
            TestService service = new TestService("test");
            Singleton.register(TestService.class, service);

            assertThat(Singleton.get(TestService.class)).isSameAs(service);
        }

        @Test
        @DisplayName("register 覆盖已有实例")
        void testRegisterOverwrite() {
            TestService first = new TestService("first");
            TestService second = new TestService("second");

            Singleton.register(TestService.class, first);
            Singleton.register(TestService.class, second);

            assertThat(Singleton.get(TestService.class)).isSameAs(second);
        }

        @Test
        @DisplayName("registerIfAbsent 不存在时注册")
        void testRegisterIfAbsentNew() {
            TestService service = new TestService("test");
            TestService result = Singleton.registerIfAbsent(TestService.class, service);

            assertThat(result).isNull();
            assertThat(Singleton.get(TestService.class)).isSameAs(service);
        }

        @Test
        @DisplayName("registerIfAbsent 已存在时不覆盖")
        void testRegisterIfAbsentExisting() {
            TestService existing = new TestService("existing");
            TestService newOne = new TestService("new");

            Singleton.register(TestService.class, existing);
            TestService result = Singleton.registerIfAbsent(TestService.class, newOne);

            assertThat(result).isSameAs(existing);
            assertThat(Singleton.get(TestService.class)).isSameAs(existing);
        }

        @Test
        @DisplayName("remove 移除实例")
        void testRemove() {
            TestService service = new TestService("test");
            Singleton.register(TestService.class, service);

            Singleton.remove(TestService.class);
            assertThat(Singleton.get(TestService.class)).isNull();
        }

        @Test
        @DisplayName("contains 检查存在")
        void testContains() {
            assertThat(Singleton.contains(TestService.class)).isFalse();

            Singleton.register(TestService.class, new TestService("test"));
            assertThat(Singleton.contains(TestService.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("按名称操作测试")
    class NameBasedTests {

        @Test
        @DisplayName("get 按名称获取")
        void testGetByName() {
            TestService service = new TestService("test");
            Singleton.register("myService", service);

            TestService result = Singleton.get("myService");
            assertThat(result).isSameAs(service);
        }

        @Test
        @DisplayName("get 按名称未注册返回 null")
        void testGetByNameNotRegistered() {
            TestService result = Singleton.get("nonExistent");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("get 按名称带 Supplier")
        void testGetByNameWithSupplier() {
            TestService result = Singleton.get("myService", () -> new TestService("lazy"));
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("lazy");
        }

        @Test
        @DisplayName("register 按名称注册")
        void testRegisterByName() {
            TestService service = new TestService("test");
            Singleton.register("myService", service);

            assertThat(Singleton.<TestService>get("myService")).isSameAs(service);
        }

        @Test
        @DisplayName("registerIfAbsent 按名称")
        void testRegisterIfAbsentByName() {
            TestService first = new TestService("first");
            TestService second = new TestService("second");

            Singleton.registerIfAbsent("myService", first);
            Singleton.registerIfAbsent("myService", second);

            assertThat(Singleton.<TestService>get("myService")).isSameAs(first);
        }

        @Test
        @DisplayName("remove 按名称移除")
        void testRemoveByName() {
            Singleton.register("myService", new TestService("test"));

            Singleton.remove("myService");
            assertThat((Object) Singleton.get("myService")).isNull();
        }

        @Test
        @DisplayName("contains 按名称检查")
        void testContainsByName() {
            assertThat(Singleton.contains("myService")).isFalse();

            Singleton.register("myService", new TestService("test"));
            assertThat(Singleton.contains("myService")).isTrue();
        }
    }

    @Nested
    @DisplayName("管理方法测试")
    class ManagementTests {

        @Test
        @DisplayName("clear 清除所有")
        void testClear() {
            Singleton.register(TestService.class, new TestService("type"));
            Singleton.register("named", new TestService("named"));

            Singleton.clear();

            assertThat((Object) Singleton.get(TestService.class)).isNull();
            assertThat((Object) Singleton.get("named")).isNull();
        }

        @Test
        @DisplayName("size 类型实例数量")
        void testSize() {
            assertThat(Singleton.size()).isEqualTo(0);

            Singleton.register(TestService.class, new TestService("1"));
            assertThat(Singleton.size()).isEqualTo(1);

            Singleton.register(String.class, "test");
            assertThat(Singleton.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("namedSize 命名实例数量")
        void testNamedSize() {
            assertThat(Singleton.namedSize()).isEqualTo(0);

            Singleton.register("service1", new TestService("1"));
            assertThat(Singleton.namedSize()).isEqualTo(1);

            Singleton.register("service2", new TestService("2"));
            assertThat(Singleton.namedSize()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("线程安全测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("多线程并发注册")
        void testConcurrentRegister() throws InterruptedException {
            int threadCount = 10;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                new Thread(() -> {
                    try {
                        startLatch.await();
                        Singleton.register("service" + index, new TestService("service" + index));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            endLatch.await();

            assertThat(Singleton.namedSize()).isEqualTo(threadCount);
        }

        @Test
        @DisplayName("多线程 computeIfAbsent")
        void testConcurrentComputeIfAbsent() throws InterruptedException {
            int threadCount = 10;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);
            AtomicInteger createCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        Singleton.get(TestService.class, () -> {
                            createCount.incrementAndGet();
                            return new TestService("singleton");
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            endLatch.await();

            // 只创建一次
            assertThat(createCount.get()).isEqualTo(1);
            assertThat(Singleton.get(TestService.class).getName()).isEqualTo("singleton");
        }
    }

    @Nested
    @DisplayName("不同类型测试")
    class DifferentTypesTests {

        @Test
        @DisplayName("多种类型独立存储")
        void testMultipleTypes() {
            Singleton.register(String.class, "string");
            Singleton.register(Integer.class, 123);
            Singleton.register(TestService.class, new TestService("service"));

            assertThat(Singleton.get(String.class)).isEqualTo("string");
            assertThat(Singleton.get(Integer.class)).isEqualTo(123);
            assertThat(Singleton.get(TestService.class).getName()).isEqualTo("service");
        }

        @Test
        @DisplayName("同名不同类型独立")
        void testSameNameDifferentTypes() {
            Singleton.register("config", "stringConfig");
            Singleton.register(String.class, "typeConfig");

            assertThat(Singleton.<String>get("config")).isEqualTo("stringConfig");
            assertThat(Singleton.get(String.class)).isEqualTo("typeConfig");
        }
    }

    static class TestService {
        private final String name;

        TestService(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
