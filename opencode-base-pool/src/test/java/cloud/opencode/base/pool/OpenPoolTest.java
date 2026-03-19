package cloud.opencode.base.pool;

import cloud.opencode.base.pool.exception.OpenPoolException;
import cloud.opencode.base.pool.factory.BaseKeyedPooledObjectFactory;
import cloud.opencode.base.pool.factory.BasePooledObjectFactory;
import cloud.opencode.base.pool.factory.KeyedPooledObjectFactory;
import cloud.opencode.base.pool.impl.*;
import cloud.opencode.base.pool.policy.EvictionPolicy;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenPoolTest Tests
 * OpenPoolTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("OpenPool 测试")
class OpenPoolTest {

    private PooledObjectFactory<String> stringFactory;
    private KeyedPooledObjectFactory<String, String> keyedFactory;

    @BeforeEach
    void setUp() {
        stringFactory = new BasePooledObjectFactory<>() {
            @Override
            protected String create() {
                return "test-" + System.nanoTime();
            }
        };

        keyedFactory = new BaseKeyedPooledObjectFactory<>() {
            @Override
            protected String create(String key) {
                return key + "-" + System.nanoTime();
            }
        };
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数抛出异常")
        void testPrivateConstructor() throws Exception {
            var constructor = OpenPool.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                    .hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("createPool方法测试")
    class CreatePoolTests {

        @Test
        @DisplayName("使用默认配置创建池")
        void testCreatePoolDefault() throws Exception {
            ObjectPool<String> pool = OpenPool.createPool(stringFactory);

            assertThat(pool).isInstanceOf(GenericObjectPool.class);

            String obj = pool.borrowObject();
            assertThat(obj).startsWith("test-");
            pool.returnObject(obj);
            pool.close();
        }

        @Test
        @DisplayName("使用自定义配置创建池")
        void testCreatePoolWithConfig() throws Exception {
            PoolConfig config = PoolConfig.builder()
                    .maxTotal(5)
                    .minIdle(1)
                    .build();

            ObjectPool<String> pool = OpenPool.createPool(stringFactory, config);

            assertThat(pool).isInstanceOf(GenericObjectPool.class);
            pool.close();
        }
    }

    @Nested
    @DisplayName("createKeyedPool方法测试")
    class CreateKeyedPoolTests {

        @Test
        @DisplayName("使用默认配置创建键控池")
        void testCreateKeyedPoolDefault() throws Exception {
            KeyedObjectPool<String, String> pool = OpenPool.createKeyedPool(keyedFactory);

            assertThat(pool).isInstanceOf(GenericKeyedObjectPool.class);

            String obj = pool.borrowObject("key1");
            assertThat(obj).startsWith("key1-");
            pool.returnObject("key1", obj);
            pool.close();
        }

        @Test
        @DisplayName("使用自定义配置创建键控池")
        void testCreateKeyedPoolWithConfig() throws Exception {
            PoolConfig config = PoolConfig.builder()
                    .maxTotal(10)
                    .build();

            KeyedObjectPool<String, String> pool = OpenPool.createKeyedPool(keyedFactory, config);

            assertThat(pool).isInstanceOf(GenericKeyedObjectPool.class);
            pool.close();
        }
    }

    @Nested
    @DisplayName("createThreadLocalPool方法测试")
    class CreateThreadLocalPoolTests {

        @Test
        @DisplayName("创建线程本地池")
        void testCreateThreadLocalPool() throws Exception {
            ObjectPool<String> pool = OpenPool.createThreadLocalPool(stringFactory);

            assertThat(pool).isInstanceOf(ThreadLocalPool.class);

            String obj = pool.borrowObject();
            assertThat(obj).startsWith("test-");
            pool.returnObject(obj);
            pool.close();
        }
    }

    @Nested
    @DisplayName("createSoftReferencePool方法测试")
    class CreateSoftReferencePoolTests {

        @Test
        @DisplayName("使用默认配置创建软引用池")
        void testCreateSoftReferencePoolDefault() throws Exception {
            ObjectPool<String> pool = OpenPool.createSoftReferencePool(stringFactory);

            assertThat(pool).isInstanceOf(SoftReferencePool.class);

            String obj = pool.borrowObject();
            assertThat(obj).startsWith("test-");
            pool.returnObject(obj);
            pool.close();
        }

        @Test
        @DisplayName("使用自定义配置创建软引用池")
        void testCreateSoftReferencePoolWithConfig() throws Exception {
            PoolConfig config = PoolConfig.builder()
                    .maxTotal(5)
                    .build();

            ObjectPool<String> pool = OpenPool.createSoftReferencePool(stringFactory, config);

            assertThat(pool).isInstanceOf(SoftReferencePool.class);
            pool.close();
        }
    }

    @Nested
    @DisplayName("createVirtualThreadPool方法测试")
    class CreateVirtualThreadPoolTests {

        @Test
        @DisplayName("使用默认配置创建虚拟线程池")
        void testCreateVirtualThreadPoolDefault() throws Exception {
            ObjectPool<String> pool = OpenPool.createVirtualThreadPool(stringFactory);

            assertThat(pool).isInstanceOf(VirtualThreadPool.class);

            String obj = pool.borrowObject();
            assertThat(obj).startsWith("test-");
            pool.returnObject(obj);
            pool.close();
        }

        @Test
        @DisplayName("使用自定义配置创建虚拟线程池")
        void testCreateVirtualThreadPoolWithConfig() throws Exception {
            PoolConfig config = PoolConfig.builder()
                    .maxTotal(10)
                    .build();

            ObjectPool<String> pool = OpenPool.createVirtualThreadPool(stringFactory, config);

            assertThat(pool).isInstanceOf(VirtualThreadPool.class);
            pool.close();
        }
    }

    @Nested
    @DisplayName("配置方法测试")
    class ConfigMethodTests {

        @Test
        @DisplayName("configBuilder返回构建器")
        void testConfigBuilder() {
            PoolConfig.Builder builder = OpenPool.configBuilder();

            assertThat(builder).isNotNull();

            PoolConfig config = builder.maxTotal(20).build();
            assertThat(config.maxTotal()).isEqualTo(20);
        }

        @Test
        @DisplayName("defaultConfig返回默认配置")
        void testDefaultConfig() {
            PoolConfig config = OpenPool.defaultConfig();

            assertThat(config).isNotNull();
            assertThat(config).isEqualTo(PoolConfig.defaults());
        }
    }

    @Nested
    @DisplayName("驱逐策略工厂方法测试")
    class EvictionPolicyTests {

        @Test
        @DisplayName("idleTimeEviction创建空闲时间策略")
        void testIdleTimeEviction() {
            Duration maxIdleTime = Duration.ofMinutes(30);
            EvictionPolicy<String> policy = OpenPool.idleTimeEviction(maxIdleTime);

            assertThat(policy).isInstanceOf(EvictionPolicy.IdleTime.class);
            assertThat(((EvictionPolicy.IdleTime<String>) policy).maxIdleTime())
                    .isEqualTo(maxIdleTime);
        }

        @Test
        @DisplayName("lruEviction创建LRU策略")
        void testLruEviction() {
            EvictionPolicy<String> policy = OpenPool.lruEviction(10);

            assertThat(policy).isInstanceOf(EvictionPolicy.LRU.class);
            assertThat(((EvictionPolicy.LRU<String>) policy).maxObjects()).isEqualTo(10);
        }

        @Test
        @DisplayName("lfuEviction创建LFU策略")
        void testLfuEviction() {
            EvictionPolicy<String> policy = OpenPool.lfuEviction(5);

            assertThat(policy).isInstanceOf(EvictionPolicy.LFU.class);
            assertThat(((EvictionPolicy.LFU<String>) policy).minBorrowCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("allEviction创建组合策略（全部匹配）")
        void testAllEviction() {
            EvictionPolicy<String> idlePolicy = OpenPool.idleTimeEviction(Duration.ofMinutes(30));
            EvictionPolicy<String> lfuPolicy = OpenPool.lfuEviction(5);

            EvictionPolicy<String> composite = OpenPool.allEviction(idlePolicy, lfuPolicy);

            assertThat(composite).isInstanceOf(EvictionPolicy.Composite.class);
            EvictionPolicy.Composite<String> comp = (EvictionPolicy.Composite<String>) composite;
            assertThat(comp.requireAll()).isTrue();
            assertThat(comp.policies()).hasSize(2);
        }

        @Test
        @DisplayName("anyEviction创建组合策略（任一匹配）")
        void testAnyEviction() {
            EvictionPolicy<String> idlePolicy = OpenPool.idleTimeEviction(Duration.ofMinutes(30));
            EvictionPolicy<String> lruPolicy = OpenPool.lruEviction(10);

            EvictionPolicy<String> composite = OpenPool.anyEviction(idlePolicy, lruPolicy);

            assertThat(composite).isInstanceOf(EvictionPolicy.Composite.class);
            EvictionPolicy.Composite<String> comp = (EvictionPolicy.Composite<String>) composite;
            assertThat(comp.requireAll()).isFalse();
            assertThat(comp.policies()).hasSize(2);
        }
    }
}
