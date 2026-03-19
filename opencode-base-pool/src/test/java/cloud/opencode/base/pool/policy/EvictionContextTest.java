package cloud.opencode.base.pool.policy;

import org.junit.jupiter.api.*;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * EvictionContextTest Tests
 * EvictionContextTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("EvictionContext 测试")
class EvictionContextTest {

    @Nested
    @DisplayName("Record组件测试")
    class RecordComponentTests {

        @Test
        @DisplayName("currentIdleCount返回空闲数")
        void testCurrentIdleCount() {
            EvictionContext ctx = new EvictionContext(5, 3, 10, Instant.now());
            assertThat(ctx.currentIdleCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("currentActiveCount返回活跃数")
        void testCurrentActiveCount() {
            EvictionContext ctx = new EvictionContext(5, 3, 10, Instant.now());
            assertThat(ctx.currentActiveCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("maxTotal返回最大总数")
        void testMaxTotal() {
            EvictionContext ctx = new EvictionContext(5, 3, 10, Instant.now());
            assertThat(ctx.maxTotal()).isEqualTo(10);
        }

        @Test
        @DisplayName("evictionTime返回驱逐时间")
        void testEvictionTime() {
            Instant now = Instant.now();
            EvictionContext ctx = new EvictionContext(5, 3, 10, now);
            assertThat(ctx.evictionTime()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("totalCount方法测试")
    class TotalCountTests {

        @Test
        @DisplayName("totalCount返回空闲数加活跃数")
        void testTotalCount() {
            EvictionContext ctx = new EvictionContext(5, 3, 10, Instant.now());
            assertThat(ctx.totalCount()).isEqualTo(8);
        }

        @Test
        @DisplayName("totalCount处理零值")
        void testTotalCountZero() {
            EvictionContext ctx = new EvictionContext(0, 0, 10, Instant.now());
            assertThat(ctx.totalCount()).isZero();
        }
    }

    @Nested
    @DisplayName("isAtCapacity方法测试")
    class IsAtCapacityTests {

        @Test
        @DisplayName("总数等于最大值时返回true")
        void testAtCapacity() {
            EvictionContext ctx = new EvictionContext(5, 5, 10, Instant.now());
            assertThat(ctx.isAtCapacity()).isTrue();
        }

        @Test
        @DisplayName("总数超过最大值时返回true")
        void testOverCapacity() {
            EvictionContext ctx = new EvictionContext(6, 5, 10, Instant.now());
            assertThat(ctx.isAtCapacity()).isTrue();
        }

        @Test
        @DisplayName("总数小于最大值时返回false")
        void testUnderCapacity() {
            EvictionContext ctx = new EvictionContext(3, 3, 10, Instant.now());
            assertThat(ctx.isAtCapacity()).isFalse();
        }
    }

    @Nested
    @DisplayName("idleRatio方法测试")
    class IdleRatioTests {

        @Test
        @DisplayName("计算正确的空闲比率")
        void testIdleRatio() {
            EvictionContext ctx = new EvictionContext(5, 5, 10, Instant.now());
            assertThat(ctx.idleRatio()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("全空闲时返回1.0")
        void testIdleRatioAllIdle() {
            EvictionContext ctx = new EvictionContext(10, 0, 10, Instant.now());
            assertThat(ctx.idleRatio()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("全活跃时返回0.0")
        void testIdleRatioAllActive() {
            EvictionContext ctx = new EvictionContext(0, 10, 10, Instant.now());
            assertThat(ctx.idleRatio()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("总数为零时返回0.0")
        void testIdleRatioZeroTotal() {
            EvictionContext ctx = new EvictionContext(0, 0, 10, Instant.now());
            assertThat(ctx.idleRatio()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Record标准方法测试")
    class RecordStandardMethodTests {

        @Test
        @DisplayName("equals比较相同值返回true")
        void testEquals() {
            Instant now = Instant.now();
            EvictionContext ctx1 = new EvictionContext(5, 3, 10, now);
            EvictionContext ctx2 = new EvictionContext(5, 3, 10, now);

            assertThat(ctx1).isEqualTo(ctx2);
        }

        @Test
        @DisplayName("hashCode相同值返回相同结果")
        void testHashCode() {
            Instant now = Instant.now();
            EvictionContext ctx1 = new EvictionContext(5, 3, 10, now);
            EvictionContext ctx2 = new EvictionContext(5, 3, 10, now);

            assertThat(ctx1.hashCode()).isEqualTo(ctx2.hashCode());
        }

        @Test
        @DisplayName("toString返回字符串表示")
        void testToString() {
            EvictionContext ctx = new EvictionContext(5, 3, 10, Instant.now());
            String str = ctx.toString();

            assertThat(str).contains("EvictionContext");
            assertThat(str).contains("5");
            assertThat(str).contains("3");
            assertThat(str).contains("10");
        }
    }
}
