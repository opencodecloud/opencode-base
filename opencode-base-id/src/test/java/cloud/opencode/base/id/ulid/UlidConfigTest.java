package cloud.opencode.base.id.ulid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * UlidConfig 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("UlidConfig 测试")
class UlidConfigTest {

    @Nested
    @DisplayName("默认配置测试")
    class DefaultConfigTests {

        @Test
        @DisplayName("默认配置存在")
        void testDefaultConfig() {
            UlidConfig config = UlidConfig.DEFAULT;

            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("默认配置是单调的")
        void testDefaultIsMonotonic() {
            assertThat(UlidConfig.DEFAULT.monotonic()).isTrue();
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("defaultConfig方法")
        void testDefaultConfigMethod() {
            UlidConfig config = UlidConfig.defaultConfig();

            assertThat(config).isNotNull();
            assertThat(config).isSameAs(UlidConfig.DEFAULT);
        }

        @Test
        @DisplayName("withMonotonic方法")
        void testWithMonotonic() {
            UlidConfig config = UlidConfig.withMonotonic();

            assertThat(config).isNotNull();
            assertThat(config.monotonic()).isTrue();
        }

        @Test
        @DisplayName("nonMonotonic方法")
        void testNonMonotonic() {
            UlidConfig config = UlidConfig.nonMonotonic();

            assertThat(config).isNotNull();
            assertThat(config.monotonic()).isFalse();
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodTests {

        @Test
        @DisplayName("构造函数")
        void testConstructor() {
            UlidConfig config = new UlidConfig(true);

            assertThat(config.monotonic()).isTrue();
        }

        @Test
        @DisplayName("equals方法")
        void testEquals() {
            UlidConfig config1 = new UlidConfig(true);
            UlidConfig config2 = new UlidConfig(true);
            UlidConfig config3 = new UlidConfig(false);

            assertThat(config1).isEqualTo(config2);
            assertThat(config1).isNotEqualTo(config3);
        }

        @Test
        @DisplayName("hashCode方法")
        void testHashCode() {
            UlidConfig config1 = new UlidConfig(true);
            UlidConfig config2 = new UlidConfig(true);

            assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
        }

        @Test
        @DisplayName("toString方法")
        void testToString() {
            UlidConfig config = new UlidConfig(true);

            assertThat(config.toString()).contains("monotonic");
        }
    }
}
