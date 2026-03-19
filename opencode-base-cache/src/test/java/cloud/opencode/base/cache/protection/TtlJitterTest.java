package cloud.opencode.base.cache.protection;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link TtlJitter}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("TtlJitter TTL抖动测试")
class TtlJitterTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryTests {

        @Test
        @DisplayName("of() 应使用给定的抖动范围创建实例")
        void ofShouldCreateWithGivenRange() {
            Duration range = Duration.ofSeconds(10);

            TtlJitter jitter = TtlJitter.of(range);

            assertThat(jitter).isNotNull();
            assertThat(jitter.getJitterRange()).isEqualTo(range);
        }

        @Test
        @DisplayName("of() 应接受零值抖动范围")
        void ofShouldAcceptZeroRange() {
            TtlJitter jitter = TtlJitter.of(Duration.ZERO);

            assertThat(jitter.getJitterRange()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("of() 当参数为 null 时应抛出 IllegalArgumentException")
        void ofShouldThrowOnNull() {
            assertThatThrownBy(() -> TtlJitter.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("of() 当参数为负值时应抛出 IllegalArgumentException")
        void ofShouldThrowOnNegative() {
            assertThatThrownBy(() -> TtlJitter.of(Duration.ofSeconds(-1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative");
        }

        @Test
        @DisplayName("defaults() 应创建30秒抖动范围的实例")
        void defaultsShouldCreate30SecondRange() {
            TtlJitter jitter = TtlJitter.defaults();

            assertThat(jitter).isNotNull();
            assertThat(jitter.getJitterRange()).isEqualTo(Duration.ofSeconds(30));
        }
    }

    @Nested
    @DisplayName("apply() 随机抖动测试")
    class ApplyTests {

        @Test
        @DisplayName("apply() 应在 [baseTtl, baseTtl + jitterRange] 范围内返回结果")
        void applyShouldReturnValueWithinRange() {
            Duration range = Duration.ofSeconds(10);
            Duration baseTtl = Duration.ofMinutes(5);
            TtlJitter jitter = TtlJitter.of(range);

            for (int i = 0; i < 100; i++) {
                Duration result = jitter.apply(baseTtl);

                assertThat(result).isGreaterThanOrEqualTo(baseTtl);
                assertThat(result).isLessThanOrEqualTo(baseTtl.plus(range));
            }
        }

        @Test
        @DisplayName("apply() 多次调用应产生不同的结果（随机性验证）")
        void applyShouldProduceVariedResults() {
            Duration range = Duration.ofSeconds(30);
            Duration baseTtl = Duration.ofMinutes(10);
            TtlJitter jitter = TtlJitter.of(range);

            Set<Duration> results = new HashSet<>();
            for (int i = 0; i < 50; i++) {
                results.add(jitter.apply(baseTtl));
            }

            // With 30s range and 50 trials, we should get multiple distinct values
            assertThat(results.size()).isGreaterThan(1);
        }

        @Test
        @DisplayName("apply() 当 baseTtl 为 null 时应返回 null")
        void applyShouldReturnNullForNullBaseTtl() {
            TtlJitter jitter = TtlJitter.of(Duration.ofSeconds(10));

            Duration result = jitter.apply(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("apply() 当 baseTtl 为零时应返回零")
        void applyShouldReturnZeroForZeroBaseTtl() {
            TtlJitter jitter = TtlJitter.of(Duration.ofSeconds(10));

            Duration result = jitter.apply(Duration.ZERO);

            assertThat(result).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("apply() 当 jitterRange 为零时应返回原始 baseTtl")
        void applyShouldReturnExactBaseTtlWhenRangeIsZero() {
            TtlJitter jitter = TtlJitter.of(Duration.ZERO);
            Duration baseTtl = Duration.ofMinutes(5);

            Duration result = jitter.apply(baseTtl);

            assertThat(result).isEqualTo(baseTtl);
        }
    }

    @Nested
    @DisplayName("stagger() 确定性错开测试")
    class StaggerTests {

        @Test
        @DisplayName("stagger() 应产生确定性的递增偏移")
        void staggerShouldProduceDeterministicOffsets() {
            Duration range = Duration.ofSeconds(10);
            Duration baseTtl = Duration.ofMinutes(5);
            TtlJitter jitter = TtlJitter.of(range);
            int batchSize = 5;

            Duration prev = Duration.ZERO;
            for (int i = 0; i < batchSize; i++) {
                Duration result = jitter.stagger(baseTtl, batchSize, i);
                assertThat(result).isGreaterThanOrEqualTo(baseTtl);
                assertThat(result).isGreaterThanOrEqualTo(prev);
                prev = result;
            }
        }

        @Test
        @DisplayName("stagger() index=0 应返回原始 baseTtl")
        void staggerIndexZeroShouldReturnBaseTtl() {
            TtlJitter jitter = TtlJitter.of(Duration.ofSeconds(10));
            Duration baseTtl = Duration.ofMinutes(5);

            Duration result = jitter.stagger(baseTtl, 10, 0);

            assertThat(result).isEqualTo(baseTtl);
        }

        @Test
        @DisplayName("stagger() 应按公式 baseTtl + (jitterRange/batchSize * index) 计算")
        void staggerShouldFollowFormula() {
            Duration range = Duration.ofSeconds(10);
            Duration baseTtl = Duration.ofMinutes(5);
            TtlJitter jitter = TtlJitter.of(range);
            int batchSize = 4;

            // staggerStep = 10000ms / 4 = 2500ms
            long staggerStep = range.toMillis() / batchSize;

            for (int i = 0; i < batchSize; i++) {
                Duration expected = baseTtl.plusMillis(staggerStep * i);
                Duration result = jitter.stagger(baseTtl, batchSize, i);
                assertThat(result).isEqualTo(expected);
            }
        }

        @Test
        @DisplayName("stagger() 当 batchSize <= 1 时应返回原始 baseTtl")
        void staggerShouldReturnBaseTtlForSingleBatch() {
            TtlJitter jitter = TtlJitter.of(Duration.ofSeconds(10));
            Duration baseTtl = Duration.ofMinutes(5);

            assertThat(jitter.stagger(baseTtl, 1, 0)).isEqualTo(baseTtl);
            assertThat(jitter.stagger(baseTtl, 0, 0)).isEqualTo(baseTtl);
            assertThat(jitter.stagger(baseTtl, -1, 0)).isEqualTo(baseTtl);
        }

        @Test
        @DisplayName("stagger() 当 baseTtl 为 null 时应返回 null")
        void staggerShouldReturnNullForNullBaseTtl() {
            TtlJitter jitter = TtlJitter.of(Duration.ofSeconds(10));

            Duration result = jitter.stagger(null, 10, 5);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("stagger() 相同参数多次调用应返回相同结果")
        void staggerShouldBeDeterministic() {
            TtlJitter jitter = TtlJitter.of(Duration.ofSeconds(10));
            Duration baseTtl = Duration.ofMinutes(5);

            Duration first = jitter.stagger(baseTtl, 10, 3);
            Duration second = jitter.stagger(baseTtl, 10, 3);

            assertThat(first).isEqualTo(second);
        }
    }

    @Nested
    @DisplayName("mutexRefresh() 互斥刷新测试")
    class MutexRefreshTests {

        @Test
        @DisplayName("mutexRefresh() 锁可用时应执行刷新操作并返回新值")
        void mutexRefreshShouldReturnFreshValueWhenLockAvailable() {
            TtlJitter jitter = TtlJitter.of(Duration.ofSeconds(10));

            String result = jitter.mutexRefresh(() -> "fresh-value", "stale-value");

            assertThat(result).isEqualTo("fresh-value");
        }

        @Test
        @DisplayName("mutexRefresh() 应支持返回 null 的刷新操作")
        void mutexRefreshShouldHandleNullRefreshResult() {
            TtlJitter jitter = TtlJitter.of(Duration.ofSeconds(10));

            String result = jitter.mutexRefresh(() -> null, "stale");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("mutexRefresh() 多次调用无竞争时都应返回新值")
        void mutexRefreshShouldReturnFreshValueOnRepeatedCalls() {
            TtlJitter jitter = TtlJitter.of(Duration.ofSeconds(10));

            for (int i = 0; i < 5; i++) {
                int index = i;
                String result = jitter.mutexRefresh(() -> "value-" + index, "stale");
                assertThat(result).isEqualTo("value-" + index);
            }
        }
    }

    @Nested
    @DisplayName("getJitterRange() 配置读取测试")
    class GetJitterRangeTests {

        @Test
        @DisplayName("getJitterRange() 应返回构造时传入的值")
        void getJitterRangeShouldReturnConfiguredValue() {
            Duration range = Duration.ofMinutes(2);
            TtlJitter jitter = TtlJitter.of(range);

            assertThat(jitter.getJitterRange()).isEqualTo(range);
        }

        @Test
        @DisplayName("getJitterRange() defaults() 应返回30秒")
        void getJitterRangeForDefaultsShouldReturn30Seconds() {
            TtlJitter jitter = TtlJitter.defaults();

            assertThat(jitter.getJitterRange()).isEqualTo(Duration.ofSeconds(30));
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString() 应包含 jitterRange 信息")
        void toStringShouldContainJitterRange() {
            TtlJitter jitter = TtlJitter.of(Duration.ofSeconds(15));

            String str = jitter.toString();

            assertThat(str).contains("TtlJitter");
            assertThat(str).contains("jitterRange");
        }
    }
}
