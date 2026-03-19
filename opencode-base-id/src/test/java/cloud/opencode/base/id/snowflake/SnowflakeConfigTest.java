package cloud.opencode.base.id.snowflake;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * SnowflakeConfig 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("SnowflakeConfig 测试")
class SnowflakeConfigTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("defaultConfig工厂方法")
        void testDefaultConfig() {
            SnowflakeConfig config = SnowflakeConfig.defaultConfig();

            assertThat(config).isNotNull();
            assertThat(config.workerId()).isEqualTo(0);
            assertThat(config.datacenterId()).isEqualTo(0);
            assertThat(config.epoch()).isEqualTo(SnowflakeConfig.DEFAULT_EPOCH);
        }

        @Test
        @DisplayName("of工厂方法")
        void testOf() {
            SnowflakeConfig config = SnowflakeConfig.of(5, 10);

            assertThat(config.workerId()).isEqualTo(5);
            assertThat(config.datacenterId()).isEqualTo(10);
            assertThat(config.epoch()).isEqualTo(SnowflakeConfig.DEFAULT_EPOCH);
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("默认epoch")
        void testDefaultEpoch() {
            assertThat(SnowflakeConfig.DEFAULT_EPOCH).isGreaterThan(0);
            assertThat(SnowflakeConfig.DEFAULT_EPOCH).isEqualTo(1609459200000L);
        }

        @Test
        @DisplayName("默认位数")
        void testDefaultBits() {
            assertThat(SnowflakeConfig.DEFAULT_TIMESTAMP_BITS).isEqualTo(41);
            assertThat(SnowflakeConfig.DEFAULT_DATACENTER_BITS).isEqualTo(5);
            assertThat(SnowflakeConfig.DEFAULT_WORKER_BITS).isEqualTo(5);
            assertThat(SnowflakeConfig.DEFAULT_SEQUENCE_BITS).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodTests {

        @Test
        @DisplayName("获取workerId")
        void testWorkerId() {
            SnowflakeConfig config = new SnowflakeConfig(
                    1, 2, SnowflakeConfig.DEFAULT_EPOCH,
                    41, 5, 5, 12
            );

            assertThat(config.workerId()).isEqualTo(1);
        }

        @Test
        @DisplayName("获取datacenterId")
        void testDatacenterId() {
            SnowflakeConfig config = new SnowflakeConfig(
                    1, 2, SnowflakeConfig.DEFAULT_EPOCH,
                    41, 5, 5, 12
            );

            assertThat(config.datacenterId()).isEqualTo(2);
        }

        @Test
        @DisplayName("获取epoch")
        void testEpoch() {
            SnowflakeConfig config = new SnowflakeConfig(
                    1, 2, 1609459200000L,
                    41, 5, 5, 12
            );

            assertThat(config.epoch()).isEqualTo(1609459200000L);
        }

        @Test
        @DisplayName("获取timestampBits")
        void testTimestampBits() {
            SnowflakeConfig config = SnowflakeConfig.defaultConfig();

            assertThat(config.timestampBits()).isEqualTo(41);
        }

        @Test
        @DisplayName("获取datacenterBits")
        void testDatacenterBits() {
            SnowflakeConfig config = SnowflakeConfig.defaultConfig();

            assertThat(config.datacenterBits()).isEqualTo(5);
        }

        @Test
        @DisplayName("获取workerBits")
        void testWorkerBits() {
            SnowflakeConfig config = SnowflakeConfig.defaultConfig();

            assertThat(config.workerBits()).isEqualTo(5);
        }

        @Test
        @DisplayName("获取sequenceBits")
        void testSequenceBits() {
            SnowflakeConfig config = SnowflakeConfig.defaultConfig();

            assertThat(config.sequenceBits()).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("计算方法测试")
    class CalculationTests {

        @Test
        @DisplayName("maxWorkerId方法")
        void testMaxWorkerId() {
            SnowflakeConfig config = SnowflakeConfig.defaultConfig();

            assertThat(config.maxWorkerId()).isEqualTo(31L);
        }

        @Test
        @DisplayName("maxDatacenterId方法")
        void testMaxDatacenterId() {
            SnowflakeConfig config = SnowflakeConfig.defaultConfig();

            assertThat(config.maxDatacenterId()).isEqualTo(31L);
        }

        @Test
        @DisplayName("maxSequence方法")
        void testMaxSequence() {
            SnowflakeConfig config = SnowflakeConfig.defaultConfig();

            assertThat(config.maxSequence()).isEqualTo(4095L);
        }

        @Test
        @DisplayName("epochInstant方法")
        void testEpochInstant() {
            SnowflakeConfig config = SnowflakeConfig.defaultConfig();

            Instant instant = config.epochInstant();

            assertThat(instant).isNotNull();
            assertThat(instant.toEpochMilli()).isEqualTo(SnowflakeConfig.DEFAULT_EPOCH);
        }

        @Test
        @DisplayName("workerIdShift方法")
        void testWorkerIdShift() {
            SnowflakeConfig config = SnowflakeConfig.defaultConfig();

            assertThat(config.workerIdShift()).isEqualTo(12);
        }

        @Test
        @DisplayName("datacenterIdShift方法")
        void testDatacenterIdShift() {
            SnowflakeConfig config = SnowflakeConfig.defaultConfig();

            assertThat(config.datacenterIdShift()).isEqualTo(17);
        }

        @Test
        @DisplayName("timestampShift方法")
        void testTimestampShift() {
            SnowflakeConfig config = SnowflakeConfig.defaultConfig();

            assertThat(config.timestampShift()).isEqualTo(22);
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class ValidationTests {

        @Test
        @DisplayName("validate有效配置")
        void testValidateValid() {
            SnowflakeConfig config = SnowflakeConfig.of(10, 20);

            assertThatCode(() -> config.validate()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validate无效workerId")
        void testValidateInvalidWorkerId() {
            SnowflakeConfig config = new SnowflakeConfig(
                    100, 0, SnowflakeConfig.DEFAULT_EPOCH,
                    41, 5, 5, 12
            );

            assertThatThrownBy(config::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Worker ID");
        }

        @Test
        @DisplayName("validate无效datacenterId")
        void testValidateInvalidDatacenterId() {
            SnowflakeConfig config = new SnowflakeConfig(
                    0, 100, SnowflakeConfig.DEFAULT_EPOCH,
                    41, 5, 5, 12
            );

            assertThatThrownBy(config::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Datacenter ID");
        }

        @Test
        @DisplayName("validate位数不等于63")
        void testValidateInvalidTotalBits() {
            SnowflakeConfig config = new SnowflakeConfig(
                    0, 0, SnowflakeConfig.DEFAULT_EPOCH,
                    40, 5, 5, 12
            );

            assertThatThrownBy(config::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("63");
        }
    }

    @Nested
    @DisplayName("等值测试")
    class EqualsTests {

        @Test
        @DisplayName("相同配置相等")
        void testEquals() {
            SnowflakeConfig config1 = SnowflakeConfig.of(1, 2);
            SnowflakeConfig config2 = SnowflakeConfig.of(1, 2);

            assertThat(config1).isEqualTo(config2);
        }

        @Test
        @DisplayName("不同配置不相等")
        void testNotEquals() {
            SnowflakeConfig config1 = SnowflakeConfig.of(1, 2);
            SnowflakeConfig config2 = SnowflakeConfig.of(3, 4);

            assertThat(config1).isNotEqualTo(config2);
        }

        @Test
        @DisplayName("hashCode一致性")
        void testHashCode() {
            SnowflakeConfig config1 = SnowflakeConfig.of(1, 2);
            SnowflakeConfig config2 = SnowflakeConfig.of(1, 2);

            assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
        }
    }
}
