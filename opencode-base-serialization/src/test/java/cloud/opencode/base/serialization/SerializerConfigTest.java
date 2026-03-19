package cloud.opencode.base.serialization;

import cloud.opencode.base.serialization.compress.CompressionAlgorithm;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SerializerConfigTest Tests
 * SerializerConfigTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
@DisplayName("SerializerConfig Tests")
class SerializerConfigTest {

    @Nested
    @DisplayName("Default Configuration Tests")
    class DefaultConfigurationTests {

        @Test
        @DisplayName("defaults() should return DEFAULT instance")
        void defaultsShouldReturnDefaultInstance() {
            SerializerConfig config = SerializerConfig.defaults();

            assertThat(config).isSameAs(SerializerConfig.DEFAULT);
        }

        @Test
        @DisplayName("DEFAULT should have expected values")
        void defaultShouldHaveExpectedValues() {
            SerializerConfig config = SerializerConfig.DEFAULT;

            assertThat(config.isIncludeTypeInfo()).isFalse();
            assertThat(config.isCompressionEnabled()).isFalse();
            assertThat(config.getCompressionAlgorithm()).isEqualTo(CompressionAlgorithm.GZIP);
            assertThat(config.getCompressionThreshold()).isEqualTo(1024);
            assertThat(config.isFailOnUnknownProperties()).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder() should create new builder")
        void builderShouldCreateNewBuilder() {
            SerializerConfig.Builder builder = SerializerConfig.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("builder should build with all settings")
        void builderShouldBuildWithAllSettings() {
            SerializerConfig config = SerializerConfig.builder()
                    .includeTypeInfo(true)
                    .enableCompression(true)
                    .compressionAlgorithm(CompressionAlgorithm.LZ4)
                    .compressionThreshold(2048)
                    .failOnUnknownProperties(true)
                    .build();

            assertThat(config.isIncludeTypeInfo()).isTrue();
            assertThat(config.isCompressionEnabled()).isTrue();
            assertThat(config.getCompressionAlgorithm()).isEqualTo(CompressionAlgorithm.LZ4);
            assertThat(config.getCompressionThreshold()).isEqualTo(2048);
            assertThat(config.isFailOnUnknownProperties()).isTrue();
        }

        @Test
        @DisplayName("compressionAlgorithm should reject null")
        void compressionAlgorithmShouldRejectNull() {
            assertThatThrownBy(() -> SerializerConfig.builder().compressionAlgorithm(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Algorithm");
        }

        @Test
        @DisplayName("compressionThreshold should reject negative values")
        void compressionThresholdShouldRejectNegative() {
            assertThatThrownBy(() -> SerializerConfig.builder().compressionThreshold(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("non-negative");
        }

        @Test
        @DisplayName("compressionThreshold should accept zero")
        void compressionThresholdShouldAcceptZero() {
            SerializerConfig config = SerializerConfig.builder()
                    .compressionThreshold(0)
                    .build();

            assertThat(config.getCompressionThreshold()).isZero();
        }
    }

    @Nested
    @DisplayName("toBuilder Tests")
    class ToBuilderTests {

        @Test
        @DisplayName("toBuilder should create builder with current values")
        void toBuilderShouldCreateBuilderWithCurrentValues() {
            SerializerConfig original = SerializerConfig.builder()
                    .includeTypeInfo(true)
                    .enableCompression(true)
                    .compressionAlgorithm(CompressionAlgorithm.SNAPPY)
                    .compressionThreshold(4096)
                    .failOnUnknownProperties(true)
                    .build();

            SerializerConfig copy = original.toBuilder().build();

            assertThat(copy).isEqualTo(original);
            assertThat(copy).isNotSameAs(original);
        }

        @Test
        @DisplayName("toBuilder should allow modification")
        void toBuilderShouldAllowModification() {
            SerializerConfig original = SerializerConfig.builder()
                    .includeTypeInfo(true)
                    .build();

            SerializerConfig modified = original.toBuilder()
                    .includeTypeInfo(false)
                    .enableCompression(true)
                    .build();

            assertThat(modified.isIncludeTypeInfo()).isFalse();
            assertThat(modified.isCompressionEnabled()).isTrue();
            assertThat(original.isIncludeTypeInfo()).isTrue();
        }
    }

    @Nested
    @DisplayName("equals and hashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            SerializerConfig config = SerializerConfig.builder().build();

            assertThat(config).isEqualTo(config);
        }

        @Test
        @DisplayName("Should be equal to another with same values")
        void shouldBeEqualToAnotherWithSameValues() {
            SerializerConfig config1 = SerializerConfig.builder()
                    .includeTypeInfo(true)
                    .enableCompression(true)
                    .compressionAlgorithm(CompressionAlgorithm.GZIP)
                    .compressionThreshold(1024)
                    .failOnUnknownProperties(true)
                    .build();

            SerializerConfig config2 = SerializerConfig.builder()
                    .includeTypeInfo(true)
                    .enableCompression(true)
                    .compressionAlgorithm(CompressionAlgorithm.GZIP)
                    .compressionThreshold(1024)
                    .failOnUnknownProperties(true)
                    .build();

            assertThat(config1).isEqualTo(config2);
            assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to different configuration")
        void shouldNotBeEqualToDifferentConfiguration() {
            SerializerConfig config1 = SerializerConfig.builder()
                    .includeTypeInfo(true)
                    .build();

            SerializerConfig config2 = SerializerConfig.builder()
                    .includeTypeInfo(false)
                    .build();

            assertThat(config1).isNotEqualTo(config2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            SerializerConfig config = SerializerConfig.builder().build();

            assertThat(config).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            SerializerConfig config = SerializerConfig.builder().build();

            assertThat(config).isNotEqualTo("string");
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should contain all field values")
        void toStringShouldContainAllFieldValues() {
            SerializerConfig config = SerializerConfig.builder()
                    .includeTypeInfo(true)
                    .enableCompression(true)
                    .compressionAlgorithm(CompressionAlgorithm.ZSTD)
                    .compressionThreshold(512)
                    .failOnUnknownProperties(true)
                    .build();

            String str = config.toString();

            assertThat(str).contains("SerializerConfig");
            assertThat(str).contains("includeTypeInfo=true");
            assertThat(str).contains("compressionEnabled=true");
            assertThat(str).contains("compressionAlgorithm=ZSTD");
            assertThat(str).contains("compressionThreshold=512");
            assertThat(str).contains("failOnUnknownProperties=true");
        }
    }
}
