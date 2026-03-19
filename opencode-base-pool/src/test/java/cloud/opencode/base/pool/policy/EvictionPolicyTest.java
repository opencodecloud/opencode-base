package cloud.opencode.base.pool.policy;

import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.factory.DefaultPooledObject;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * EvictionPolicyTest Tests
 * EvictionPolicyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("EvictionPolicy 测试")
class EvictionPolicyTest {

    private EvictionContext createContext(int idle, int active, int maxTotal) {
        return new EvictionContext(idle, active, maxTotal, Instant.now());
    }

    @Nested
    @DisplayName("IdleTime策略测试")
    class IdleTimeTests {

        @Test
        @DisplayName("空闲时间超过阈值时驱逐")
        void testEvictWhenIdleTimeExceeded() throws InterruptedException {
            EvictionPolicy<String> policy = new EvictionPolicy.IdleTime<>(Duration.ofMillis(50));
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            pooled.markReturned();

            Thread.sleep(100);

            EvictionContext context = createContext(1, 0, 10);
            boolean shouldEvict = policy.evict(pooled, context);

            assertThat(shouldEvict).isTrue();
        }

        @Test
        @DisplayName("空闲时间未超过阈值时不驱逐")
        void testNoEvictWhenIdleTimeNotExceeded() {
            EvictionPolicy<String> policy = new EvictionPolicy.IdleTime<>(Duration.ofMinutes(30));
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            pooled.markReturned();

            EvictionContext context = createContext(1, 0, 10);
            boolean shouldEvict = policy.evict(pooled, context);

            assertThat(shouldEvict).isFalse();
        }

        @Test
        @DisplayName("maxIdleTime返回配置值")
        void testMaxIdleTimeAccessor() {
            Duration maxIdle = Duration.ofMinutes(15);
            EvictionPolicy.IdleTime<String> policy = new EvictionPolicy.IdleTime<>(maxIdle);

            assertThat(policy.maxIdleTime()).isEqualTo(maxIdle);
        }
    }

    @Nested
    @DisplayName("LRU策略测试")
    class LruTests {

        @Test
        @DisplayName("空闲数超过maxObjects时驱逐")
        void testEvictWhenOverMaxObjects() {
            EvictionPolicy<String> policy = new EvictionPolicy.LRU<>(5);
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");

            EvictionContext context = createContext(10, 2, 20); // idle > 5
            boolean shouldEvict = policy.evict(pooled, context);

            assertThat(shouldEvict).isTrue();
        }

        @Test
        @DisplayName("空闲数未超过maxObjects时不驱逐")
        void testNoEvictWhenUnderMaxObjects() {
            EvictionPolicy<String> policy = new EvictionPolicy.LRU<>(5);
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");

            EvictionContext context = createContext(3, 2, 20); // idle < 5
            boolean shouldEvict = policy.evict(pooled, context);

            assertThat(shouldEvict).isFalse();
        }

        @Test
        @DisplayName("maxObjects返回配置值")
        void testMaxObjectsAccessor() {
            EvictionPolicy.LRU<String> policy = new EvictionPolicy.LRU<>(10);

            assertThat(policy.maxObjects()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("LFU策略测试")
    class LfuTests {

        @Test
        @DisplayName("借用次数少于阈值时驱逐")
        void testEvictWhenBorrowCountLow() {
            EvictionPolicy<String> policy = new EvictionPolicy.LFU<>(5);
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            // borrowCount = 0

            EvictionContext context = createContext(5, 0, 10);
            boolean shouldEvict = policy.evict(pooled, context);

            assertThat(shouldEvict).isTrue();
        }

        @Test
        @DisplayName("借用次数达到阈值时不驱逐")
        void testNoEvictWhenBorrowCountHigh() {
            EvictionPolicy<String> policy = new EvictionPolicy.LFU<>(3);
            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");

            // Simulate multiple borrows
            pooled.markBorrowed();
            pooled.markBorrowed();
            pooled.markBorrowed();

            EvictionContext context = createContext(5, 0, 10);
            boolean shouldEvict = policy.evict(pooled, context);

            assertThat(shouldEvict).isFalse();
        }

        @Test
        @DisplayName("minBorrowCount返回配置值")
        void testMinBorrowCountAccessor() {
            EvictionPolicy.LFU<String> policy = new EvictionPolicy.LFU<>(7);

            assertThat(policy.minBorrowCount()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("Composite策略测试")
    class CompositeTests {

        @Test
        @DisplayName("requireAll为true时所有策略都匹配才驱逐")
        void testAllMatch() {
            EvictionPolicy<String> idlePolicy = new EvictionPolicy.IdleTime<>(Duration.ofMillis(1));
            EvictionPolicy<String> lfuPolicy = new EvictionPolicy.LFU<>(5);

            EvictionPolicy<String> composite = new EvictionPolicy.Composite<>(
                    List.of(idlePolicy, lfuPolicy), true);

            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            pooled.markReturned();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            EvictionContext context = createContext(5, 0, 10);

            // Both conditions met: idle time > 1ms and borrowCount (0) < 5
            boolean shouldEvict = composite.evict(pooled, context);

            assertThat(shouldEvict).isTrue();
        }

        @Test
        @DisplayName("requireAll为true时部分策略不匹配则不驱逐")
        void testPartialMatchWithRequireAll() {
            EvictionPolicy<String> idlePolicy = new EvictionPolicy.IdleTime<>(Duration.ofHours(1));
            EvictionPolicy<String> lfuPolicy = new EvictionPolicy.LFU<>(5);

            EvictionPolicy<String> composite = new EvictionPolicy.Composite<>(
                    List.of(idlePolicy, lfuPolicy), true);

            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            pooled.markReturned();

            EvictionContext context = createContext(5, 0, 10);

            // Only LFU matches, IdleTime doesn't
            boolean shouldEvict = composite.evict(pooled, context);

            assertThat(shouldEvict).isFalse();
        }

        @Test
        @DisplayName("requireAll为false时任一策略匹配即驱逐")
        void testAnyMatch() {
            EvictionPolicy<String> idlePolicy = new EvictionPolicy.IdleTime<>(Duration.ofHours(1));
            EvictionPolicy<String> lfuPolicy = new EvictionPolicy.LFU<>(5);

            EvictionPolicy<String> composite = new EvictionPolicy.Composite<>(
                    List.of(idlePolicy, lfuPolicy), false);

            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            pooled.markReturned();

            EvictionContext context = createContext(5, 0, 10);

            // LFU matches (borrowCount 0 < 5)
            boolean shouldEvict = composite.evict(pooled, context);

            assertThat(shouldEvict).isTrue();
        }

        @Test
        @DisplayName("requireAll为false时无策略匹配则不驱逐")
        void testNoMatchWithRequireAny() {
            EvictionPolicy<String> idlePolicy = new EvictionPolicy.IdleTime<>(Duration.ofHours(1));
            EvictionPolicy<String> lfuPolicy = new EvictionPolicy.LFU<>(0); // borrowCount >= 0 always true, so won't evict

            EvictionPolicy<String> composite = new EvictionPolicy.Composite<>(
                    List.of(idlePolicy, lfuPolicy), false);

            DefaultPooledObject<String> pooled = new DefaultPooledObject<>("test");
            pooled.markReturned();

            EvictionContext context = createContext(5, 0, 10);

            // Neither matches
            boolean shouldEvict = composite.evict(pooled, context);

            assertThat(shouldEvict).isFalse();
        }

        @Test
        @DisplayName("policies和requireAll返回配置值")
        void testAccessors() {
            EvictionPolicy<String> policy1 = new EvictionPolicy.IdleTime<>(Duration.ofMinutes(5));
            EvictionPolicy<String> policy2 = new EvictionPolicy.LRU<>(10);
            List<EvictionPolicy<String>> policies = List.of(policy1, policy2);

            EvictionPolicy.Composite<String> composite = new EvictionPolicy.Composite<>(policies, true);

            assertThat(composite.policies()).hasSize(2);
            assertThat(composite.requireAll()).isTrue();
        }
    }

    @Nested
    @DisplayName("密封类型测试")
    class SealedTypeTests {

        @Test
        @DisplayName("所有实现类型可用于模式匹配")
        void testPatternMatching() {
            EvictionPolicy<String> idleTime = new EvictionPolicy.IdleTime<>(Duration.ofMinutes(5));
            EvictionPolicy<String> lru = new EvictionPolicy.LRU<>(10);
            EvictionPolicy<String> lfu = new EvictionPolicy.LFU<>(5);
            EvictionPolicy<String> composite = new EvictionPolicy.Composite<>(List.of(idleTime), true);

            assertThat(describePolicy(idleTime)).contains("IdleTime");
            assertThat(describePolicy(lru)).contains("LRU");
            assertThat(describePolicy(lfu)).contains("LFU");
            assertThat(describePolicy(composite)).contains("Composite");
        }

        private String describePolicy(EvictionPolicy<String> policy) {
            return switch (policy) {
                case EvictionPolicy.IdleTime<String> idleTime -> "IdleTime: " + idleTime.maxIdleTime();
                case EvictionPolicy.LRU<String> lru -> "LRU: " + lru.maxObjects();
                case EvictionPolicy.LFU<String> lfu -> "LFU: " + lfu.minBorrowCount();
                case EvictionPolicy.Composite<String> comp -> "Composite: " + comp.policies().size();
            };
        }
    }
}
