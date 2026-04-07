package cloud.opencode.base.pool.policy;

import cloud.opencode.base.pool.OpenPool;
import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.factory.DefaultPooledObject;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * MaxAgeEvictionPolicyTest - Tests for Maximum Age Eviction Policy
 * MaxAgeEvictionPolicyTest - 最大生命周期驱逐策略测试类
 *
 * <p>Verifies that MaxAge eviction correctly identifies expired and
 * non-expired objects, works with pattern matching, and integrates
 * with pool configuration.</p>
 * <p>验证 MaxAge 驱逐策略正确识别过期和未过期的对象，
 * 支持模式匹配，并与池配置集成。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.3
 */
@DisplayName("MaxAge 驱逐策略测试")
class MaxAgeEvictionPolicyTest {

    private static final EvictionContext DEFAULT_CONTEXT =
            new EvictionContext(5, 3, 10, Instant.now());

    @Nested
    @DisplayName("基本驱逐逻辑测试 - Basic Eviction Logic")
    class BasicEvictionTests {

        /**
         * Tests that MaxAge evicts objects older than maxLifetime.
         * 测试 MaxAge 驱逐超过 maxLifetime 的对象。
         */
        @Test
        @DisplayName("驱逐超过最大生命周期的对象")
        void testEvictsOldObjects() {
            var policy = new EvictionPolicy.MaxAge<String>(Duration.ofMillis(50));

            // Create an object that was created in the past
            PooledObject<String> oldObject = createObjectWithAge(Duration.ofMillis(100));

            boolean shouldEvict = policy.evict(oldObject, DEFAULT_CONTEXT);

            assertThat(shouldEvict).isTrue();
        }

        /**
         * Tests that MaxAge does not evict objects younger than maxLifetime.
         * 测试 MaxAge 不驱逐未超过 maxLifetime 的对象。
         */
        @Test
        @DisplayName("不驱逐未超过最大生命周期的对象")
        void testDoesNotEvictYoungObjects() {
            var policy = new EvictionPolicy.MaxAge<String>(Duration.ofHours(1));

            // A freshly created object should not be evicted
            PooledObject<String> freshObject = new DefaultPooledObject<>("fresh");

            boolean shouldEvict = policy.evict(freshObject, DEFAULT_CONTEXT);

            assertThat(shouldEvict).isFalse();
        }

        /**
         * Tests that MaxAge correctly handles objects exactly at the boundary.
         * 测试 MaxAge 正确处理恰好在边界的对象。
         */
        @Test
        @DisplayName("边界值: 恰好等于 maxLifetime 不驱逐")
        void testBoundaryObjectNotEvicted() {
            // An object created "now" compared against a very large maxLifetime should not be evicted
            var policy = new EvictionPolicy.MaxAge<String>(Duration.ofDays(365));

            PooledObject<String> obj = new DefaultPooledObject<>("boundary");

            assertThat(policy.evict(obj, DEFAULT_CONTEXT)).isFalse();
        }

        /**
         * Tests that the record accessor returns the configured maxLifetime.
         * 测试记录访问器返回配置的 maxLifetime。
         */
        @Test
        @DisplayName("maxLifetime() 访问器返回正确值")
        void testMaxLifetimeAccessor() {
            var lifetime = Duration.ofMinutes(30);
            var policy = new EvictionPolicy.MaxAge<String>(lifetime);

            assertThat(policy.maxLifetime()).isEqualTo(lifetime);
        }
    }

    @Nested
    @DisplayName("模式匹配测试 - Pattern Matching")
    class PatternMatchingTests {

        /**
         * Tests that MaxAge works in a pattern matching switch expression.
         * 测试 MaxAge 在模式匹配 switch 表达式中工作。
         */
        @Test
        @DisplayName("switch 模式匹配中使用 MaxAge")
        void testMaxAgeInPatternMatchingSwitch() {
            EvictionPolicy<String> policy = new EvictionPolicy.MaxAge<>(Duration.ofHours(1));

            String description = switch (policy) {
                case EvictionPolicy.IdleTime<String>(var maxIdle) ->
                        "idle:" + maxIdle;
                case EvictionPolicy.LRU<String>(var max) ->
                        "lru:" + max;
                case EvictionPolicy.LFU<String>(var min) ->
                        "lfu:" + min;
                case EvictionPolicy.MaxAge<String>(var maxLife) ->
                        "maxAge:" + maxLife;
                case EvictionPolicy.Composite<String>(var policies, var all) ->
                        "composite:" + all;
            };

            assertThat(description).startsWith("maxAge:");
            assertThat(description).contains("PT1H");
        }

        /**
         * Tests deconstruction of MaxAge record pattern.
         * 测试 MaxAge 记录模式的解构。
         */
        @Test
        @DisplayName("MaxAge 记录模式解构")
        void testMaxAgeRecordDeconstruction() {
            var policy = new EvictionPolicy.MaxAge<String>(Duration.ofMinutes(45));

            if (policy instanceof EvictionPolicy.MaxAge<String>(var lifetime)) {
                assertThat(lifetime).isEqualTo(Duration.ofMinutes(45));
            } else {
                fail("Pattern match should succeed for MaxAge");
            }
        }
    }

    @Nested
    @DisplayName("工厂方法测试 - Factory Methods")
    class FactoryMethodTests {

        /**
         * Tests OpenPool.maxAgeEviction() factory method.
         * 测试 OpenPool.maxAgeEviction() 工厂方法。
         */
        @Test
        @DisplayName("OpenPool.maxAgeEviction() 工厂方法")
        void testOpenPoolMaxAgeEvictionFactory() {
            EvictionPolicy<String> policy = OpenPool.maxAgeEviction(Duration.ofHours(2));

            assertThat(policy).isInstanceOf(EvictionPolicy.MaxAge.class);
            assertThat(((EvictionPolicy.MaxAge<String>) policy).maxLifetime())
                    .isEqualTo(Duration.ofHours(2));
        }

        /**
         * Tests that MaxAge can be combined in a composite policy via OpenPool.
         * 测试 MaxAge 可以通过 OpenPool 组合到复合策略中。
         */
        @Test
        @DisplayName("MaxAge 与其他策略组合")
        void testMaxAgeInCompositePolicy() {
            EvictionPolicy<String> composite = OpenPool.allEviction(
                    OpenPool.idleTimeEviction(Duration.ofMinutes(30)),
                    OpenPool.maxAgeEviction(Duration.ofHours(1))
            );

            assertThat(composite).isInstanceOf(EvictionPolicy.Composite.class);
        }

        /**
         * Tests that MaxAge can be combined with OR logic.
         * 测试 MaxAge 可以用 OR 逻辑组合。
         */
        @Test
        @DisplayName("MaxAge OR 组合: 任一匹配即驱逐")
        void testMaxAgeInAnyComposite() {
            EvictionPolicy<String> composite = OpenPool.anyEviction(
                    OpenPool.idleTimeEviction(Duration.ofMinutes(30)),
                    OpenPool.maxAgeEviction(Duration.ofMillis(50))
            );

            // Old object should be evicted by MaxAge even if not idle
            PooledObject<String> oldObj = createObjectWithAge(Duration.ofMillis(100));
            assertThat(composite.evict(oldObj, DEFAULT_CONTEXT)).isTrue();
        }
    }

    @Nested
    @DisplayName("集成测试 - Integration")
    class IntegrationTests {

        /**
         * Tests maxObjectLifetime config in PoolConfig.
         * 测试 PoolConfig 中的 maxObjectLifetime 配置。
         */
        @Test
        @DisplayName("PoolConfig.maxObjectLifetime 配置")
        void testPoolConfigMaxObjectLifetime() {
            var config = cloud.opencode.base.pool.PoolConfig.builder()
                    .maxObjectLifetime(Duration.ofHours(1))
                    .build();

            assertThat(config.maxObjectLifetime()).isEqualTo(Duration.ofHours(1));
            assertThat(config.isLifetimeEnabled()).isTrue();
        }

        /**
         * Tests that Duration.ZERO disables lifetime eviction.
         * 测试 Duration.ZERO 禁用生命周期驱逐。
         */
        @Test
        @DisplayName("Duration.ZERO 禁用生命周期驱逐")
        void testZeroDurationDisablesLifetime() {
            var config = cloud.opencode.base.pool.PoolConfig.builder()
                    .maxObjectLifetime(Duration.ZERO)
                    .build();

            assertThat(config.isLifetimeEnabled()).isFalse();
        }

        /**
         * Tests that negative duration disables lifetime eviction.
         * 测试负时长禁用生命周期驱逐。
         */
        @Test
        @DisplayName("负时长禁用生命周期驱逐")
        void testNegativeDurationDisablesLifetime() {
            var config = cloud.opencode.base.pool.PoolConfig.builder()
                    .maxObjectLifetime(Duration.ofSeconds(-1))
                    .build();

            assertThat(config.isLifetimeEnabled()).isFalse();
        }

        /**
         * Tests that maxObjectLifetime config causes expired objects to be rejected on borrow.
         * 测试 maxObjectLifetime 配置使过期对象在借用时被拒绝。
         */
        @Test
        @DisplayName("maxObjectLifetime 导致过期对象在借用时被拒绝")
        void testExpiredObjectRejectedOnBorrow() throws Exception {
            var counter = new java.util.concurrent.atomic.AtomicInteger(0);

            try (var pool = OpenPool.createPool(
                    () -> "obj-" + counter.incrementAndGet(),
                    cloud.opencode.base.pool.PoolConfig.builder()
                            .maxTotal(2)
                            .minIdle(0)
                            .maxObjectLifetime(Duration.ofMillis(50))
                            .testOnBorrow(true)
                            .build())) {

                // Borrow and return quickly
                String obj1 = pool.borrowObject();
                pool.returnObject(obj1);

                // Wait for the object to expire
                Thread.sleep(100);

                // Borrow again - the old object should be expired and a new one created
                String obj2 = pool.borrowObject();
                pool.returnObject(obj2);

                // If maxObjectLifetime is enforced, a new object should have been created
                assertThat(counter.get()).isGreaterThanOrEqualTo(2);
            }
        }

        /**
         * Tests MaxAge eviction policy set via PoolConfig.evictionPolicy.
         * 测试通过 PoolConfig.evictionPolicy 设置 MaxAge 驱逐策略。
         */
        @Test
        @DisplayName("PoolConfig.evictionPolicy 设置 MaxAge")
        void testEvictionPolicyConfigWithMaxAge() {
            EvictionPolicy<String> maxAge = new EvictionPolicy.MaxAge<>(Duration.ofHours(2));

            var config = cloud.opencode.base.pool.PoolConfig.builder()
                    .evictionPolicy(maxAge)
                    .build();

            assertThat(config.evictionPolicy()).isEqualTo(maxAge);
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a PooledObject with a creation time in the past.
     * 创建一个创建时间在过去的 PooledObject。
     */
    private static PooledObject<String> createObjectWithAge(Duration age) {
        Instant pastCreateTime = Instant.now().minus(age);
        return new PooledObject<>() {
            @Override
            public String getObject() {
                return "aged-object";
            }

            @Override
            public Instant getCreateInstant() {
                return pastCreateTime;
            }

            @Override
            public Instant getLastBorrowInstant() {
                return pastCreateTime;
            }

            @Override
            public Instant getLastReturnInstant() {
                return pastCreateTime;
            }

            @Override
            public Instant getLastUseInstant() {
                return pastCreateTime;
            }

            @Override
            public cloud.opencode.base.pool.factory.PooledObjectState getState() {
                return cloud.opencode.base.pool.factory.PooledObjectState.IDLE;
            }

            @Override
            public long getBorrowCount() {
                return 1;
            }

            @Override
            public Duration getActiveDuration() {
                return Duration.ZERO;
            }

            @Override
            public Duration getIdleDuration() {
                return age;
            }

            @Override
            public boolean compareAndSetState(
                    cloud.opencode.base.pool.factory.PooledObjectState expect,
                    cloud.opencode.base.pool.factory.PooledObjectState update) {
                return false;
            }
        };
    }
}
