package cloud.opencode.base.id.snowflake;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * SnowflakeBuilder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("SnowflakeBuilder 测试")
class SnowflakeBuilderTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("默认构造方法")
        void testConstructor() {
            SnowflakeBuilder builder = new SnowflakeBuilder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("SnowflakeGenerator.builder方法")
        void testStaticBuilder() {
            SnowflakeBuilder builder = SnowflakeGenerator.builder();

            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("配置方法测试")
    class ConfigurationTests {

        @Test
        @DisplayName("设置workerId")
        void testWorkerId() {
            SnowflakeGenerator gen = new SnowflakeBuilder()
                    .workerId(10)
                    .build();

            assertThat(gen.getWorkerId()).isEqualTo(10);
        }

        @Test
        @DisplayName("设置datacenterId")
        void testDatacenterId() {
            SnowflakeGenerator gen = new SnowflakeBuilder()
                    .datacenterId(15)
                    .build();

            assertThat(gen.getDatacenterId()).isEqualTo(15);
        }

        @Test
        @DisplayName("设置epochMillis")
        void testEpochMillis() {
            SnowflakeGenerator gen = new SnowflakeBuilder()
                    .epochMillis(1609459200000L)
                    .build();

            assertThat(gen.getEpoch()).isEqualTo(1609459200000L);
        }

        @Test
        @DisplayName("设置epoch Instant")
        void testEpochInstant() {
            Instant epoch = Instant.ofEpochMilli(1609459200000L);
            SnowflakeGenerator gen = new SnowflakeBuilder()
                    .epoch(epoch)
                    .build();

            assertThat(gen.getEpoch()).isEqualTo(epoch.toEpochMilli());
        }

        @Test
        @DisplayName("设置时钟回拨策略")
        void testClockBackwardStrategy() {
            SnowflakeGenerator gen = new SnowflakeBuilder()
                    .clockBackwardStrategy(ThrowException.getInstance())
                    .build();

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("设置workerIdAssigner")
        void testWorkerIdAssigner() {
            SnowflakeGenerator gen = new SnowflakeBuilder()
                    .workerIdAssigner(RandomAssigner.create())
                    .build();

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("设置timestampBits")
        void testTimestampBits() {
            SnowflakeGenerator gen = new SnowflakeBuilder()
                    .timestampBits(41)
                    .build();

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("设置datacenterBits")
        void testDatacenterBits() {
            SnowflakeGenerator gen = new SnowflakeBuilder()
                    .datacenterBits(5)
                    .build();

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("设置workerBits")
        void testWorkerBits() {
            SnowflakeGenerator gen = new SnowflakeBuilder()
                    .workerBits(5)
                    .build();

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("设置sequenceBits")
        void testSequenceBits() {
            SnowflakeGenerator gen = new SnowflakeBuilder()
                    .sequenceBits(12)
                    .build();

            assertThat(gen).isNotNull();
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentTests {

        @Test
        @DisplayName("链式配置")
        void testFluentConfiguration() {
            SnowflakeGenerator gen = new SnowflakeBuilder()
                    .workerId(1)
                    .datacenterId(2)
                    .epochMillis(1609459200000L)
                    .clockBackwardStrategy(Wait.ofSeconds(5))
                    .build();

            assertThat(gen).isNotNull();
            assertThat(gen.getWorkerId()).isEqualTo(1);
            assertThat(gen.getDatacenterId()).isEqualTo(2);
        }

        @Test
        @DisplayName("完整配置")
        void testFullConfiguration() {
            SnowflakeGenerator gen = new SnowflakeBuilder()
                    .workerId(5)
                    .datacenterId(10)
                    .epochMillis(1609459200000L)
                    .timestampBits(41)
                    .datacenterBits(5)
                    .workerBits(5)
                    .sequenceBits(12)
                    .clockBackwardStrategy(ThrowException.getInstance())
                    .build();

            assertThat(gen).isNotNull();
            assertThat(gen.getWorkerId()).isEqualTo(5);
            assertThat(gen.getDatacenterId()).isEqualTo(10);
            assertThat(gen.generate()).isPositive();
        }
    }

    @Nested
    @DisplayName("验证测试")
    class ValidationTests {

        @Test
        @DisplayName("无效workerId抛出异常")
        void testInvalidWorkerId() {
            assertThatThrownBy(() -> new SnowflakeBuilder()
                    .workerId(100)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("无效datacenterId抛出异常")
        void testInvalidDatacenterId() {
            assertThatThrownBy(() -> new SnowflakeBuilder()
                    .datacenterId(100)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负数workerId抛出异常")
        void testNegativeWorkerId() {
            assertThatThrownBy(() -> new SnowflakeBuilder()
                    .workerId(-1)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负数datacenterId抛出异常")
        void testNegativeDatacenterId() {
            assertThatThrownBy(() -> new SnowflakeBuilder()
                    .datacenterId(-1)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("生成器功能测试")
    class GeneratorTests {

        @Test
        @DisplayName("构建后可以生成ID")
        void testGenerateAfterBuild() {
            SnowflakeGenerator gen = new SnowflakeBuilder()
                    .workerId(1)
                    .datacenterId(1)
                    .build();

            long id = gen.generate();

            assertThat(id).isPositive();
        }

        @Test
        @DisplayName("生成多个唯一ID")
        void testGenerateUniqueIds() {
            SnowflakeGenerator gen = new SnowflakeBuilder().build();

            long id1 = gen.generate();
            long id2 = gen.generate();
            long id3 = gen.generate();

            assertThat(id1).isNotEqualTo(id2);
            assertThat(id2).isNotEqualTo(id3);
        }
    }
}
