package cloud.opencode.base.serialization.compress;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CompressionAlgorithmTest Tests
 * CompressionAlgorithmTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
@DisplayName("CompressionAlgorithm Tests")
class CompressionAlgorithmTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("Should have all expected values")
        void shouldHaveAllExpectedValues() {
            assertThat(CompressionAlgorithm.values())
                    .containsExactly(
                            CompressionAlgorithm.NONE,
                            CompressionAlgorithm.GZIP,
                            CompressionAlgorithm.LZ4,
                            CompressionAlgorithm.SNAPPY,
                            CompressionAlgorithm.ZSTD
                    );
        }

        @Test
        @DisplayName("valueOf should return correct enum")
        void valueOfShouldReturnCorrectEnum() {
            assertThat(CompressionAlgorithm.valueOf("NONE")).isEqualTo(CompressionAlgorithm.NONE);
            assertThat(CompressionAlgorithm.valueOf("GZIP")).isEqualTo(CompressionAlgorithm.GZIP);
            assertThat(CompressionAlgorithm.valueOf("LZ4")).isEqualTo(CompressionAlgorithm.LZ4);
            assertThat(CompressionAlgorithm.valueOf("SNAPPY")).isEqualTo(CompressionAlgorithm.SNAPPY);
            assertThat(CompressionAlgorithm.valueOf("ZSTD")).isEqualTo(CompressionAlgorithm.ZSTD);
        }
    }

    @Nested
    @DisplayName("getName Tests")
    class GetNameTests {

        @Test
        @DisplayName("NONE should have name 'none'")
        void noneShouldHaveCorrectName() {
            assertThat(CompressionAlgorithm.NONE.getName()).isEqualTo("none");
        }

        @Test
        @DisplayName("GZIP should have name 'gzip'")
        void gzipShouldHaveCorrectName() {
            assertThat(CompressionAlgorithm.GZIP.getName()).isEqualTo("gzip");
        }

        @Test
        @DisplayName("LZ4 should have name 'lz4'")
        void lz4ShouldHaveCorrectName() {
            assertThat(CompressionAlgorithm.LZ4.getName()).isEqualTo("lz4");
        }

        @Test
        @DisplayName("SNAPPY should have name 'snappy'")
        void snappyShouldHaveCorrectName() {
            assertThat(CompressionAlgorithm.SNAPPY.getName()).isEqualTo("snappy");
        }

        @Test
        @DisplayName("ZSTD should have name 'zstd'")
        void zstdShouldHaveCorrectName() {
            assertThat(CompressionAlgorithm.ZSTD.getName()).isEqualTo("zstd");
        }
    }

    @Nested
    @DisplayName("getId Tests")
    class GetIdTests {

        @Test
        @DisplayName("NONE should have id 0")
        void noneShouldHaveId0() {
            assertThat(CompressionAlgorithm.NONE.getId()).isEqualTo((byte) 0);
        }

        @Test
        @DisplayName("GZIP should have id 1")
        void gzipShouldHaveId1() {
            assertThat(CompressionAlgorithm.GZIP.getId()).isEqualTo((byte) 1);
        }

        @Test
        @DisplayName("LZ4 should have id 2")
        void lz4ShouldHaveId2() {
            assertThat(CompressionAlgorithm.LZ4.getId()).isEqualTo((byte) 2);
        }

        @Test
        @DisplayName("SNAPPY should have id 3")
        void snappyShouldHaveId3() {
            assertThat(CompressionAlgorithm.SNAPPY.getId()).isEqualTo((byte) 3);
        }

        @Test
        @DisplayName("ZSTD should have id 4")
        void zstdShouldHaveId4() {
            assertThat(CompressionAlgorithm.ZSTD.getId()).isEqualTo((byte) 4);
        }

        @Test
        @DisplayName("All IDs should be unique")
        void allIdsShouldBeUnique() {
            var ids = new java.util.HashSet<Byte>();
            for (CompressionAlgorithm alg : CompressionAlgorithm.values()) {
                assertThat(ids.add(alg.getId())).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("isBuiltIn Tests")
    class IsBuiltInTests {

        @Test
        @DisplayName("NONE should not be built-in")
        void noneShouldNotBeBuiltIn() {
            assertThat(CompressionAlgorithm.NONE.isBuiltIn()).isFalse();
        }

        @Test
        @DisplayName("GZIP should be built-in")
        void gzipShouldBeBuiltIn() {
            assertThat(CompressionAlgorithm.GZIP.isBuiltIn()).isTrue();
        }

        @Test
        @DisplayName("LZ4 should not be built-in")
        void lz4ShouldNotBeBuiltIn() {
            assertThat(CompressionAlgorithm.LZ4.isBuiltIn()).isFalse();
        }

        @Test
        @DisplayName("SNAPPY should not be built-in")
        void snappyShouldNotBeBuiltIn() {
            assertThat(CompressionAlgorithm.SNAPPY.isBuiltIn()).isFalse();
        }

        @Test
        @DisplayName("ZSTD should not be built-in")
        void zstdShouldNotBeBuiltIn() {
            assertThat(CompressionAlgorithm.ZSTD.isBuiltIn()).isFalse();
        }
    }

    @Nested
    @DisplayName("isAvailable Tests")
    class IsAvailableTests {

        @Test
        @DisplayName("NONE should always be available")
        void noneShouldAlwaysBeAvailable() {
            assertThat(CompressionAlgorithm.NONE.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("GZIP should always be available (JDK built-in)")
        void gzipShouldAlwaysBeAvailable() {
            assertThat(CompressionAlgorithm.GZIP.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("LZ4 availability depends on classpath")
        void lz4AvailabilityDependsOnClasspath() {
            // Just verify the method doesn't throw
            boolean available = CompressionAlgorithm.LZ4.isAvailable();
            assertThat(available).isIn(true, false);
        }

        @Test
        @DisplayName("SNAPPY availability depends on classpath")
        void snappyAvailabilityDependsOnClasspath() {
            // Just verify the method doesn't throw
            boolean available = CompressionAlgorithm.SNAPPY.isAvailable();
            assertThat(available).isIn(true, false);
        }

        @Test
        @DisplayName("ZSTD availability depends on classpath")
        void zstdAvailabilityDependsOnClasspath() {
            // Just verify the method doesn't throw
            boolean available = CompressionAlgorithm.ZSTD.isAvailable();
            assertThat(available).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("fromId Tests")
    class FromIdTests {

        @Test
        @DisplayName("fromId should return correct algorithm for valid IDs")
        void fromIdShouldReturnCorrectAlgorithm() {
            assertThat(CompressionAlgorithm.fromId((byte) 0)).isEqualTo(CompressionAlgorithm.NONE);
            assertThat(CompressionAlgorithm.fromId((byte) 1)).isEqualTo(CompressionAlgorithm.GZIP);
            assertThat(CompressionAlgorithm.fromId((byte) 2)).isEqualTo(CompressionAlgorithm.LZ4);
            assertThat(CompressionAlgorithm.fromId((byte) 3)).isEqualTo(CompressionAlgorithm.SNAPPY);
            assertThat(CompressionAlgorithm.fromId((byte) 4)).isEqualTo(CompressionAlgorithm.ZSTD);
        }

        @Test
        @DisplayName("fromId should return NONE for unknown ID")
        void fromIdShouldReturnNoneForUnknownId() {
            assertThat(CompressionAlgorithm.fromId((byte) 99)).isEqualTo(CompressionAlgorithm.NONE);
            assertThat(CompressionAlgorithm.fromId((byte) -1)).isEqualTo(CompressionAlgorithm.NONE);
        }
    }

    @Nested
    @DisplayName("fromName Tests")
    class FromNameTests {

        @Test
        @DisplayName("fromName should return correct algorithm for valid names")
        void fromNameShouldReturnCorrectAlgorithm() {
            assertThat(CompressionAlgorithm.fromName("none")).isEqualTo(CompressionAlgorithm.NONE);
            assertThat(CompressionAlgorithm.fromName("gzip")).isEqualTo(CompressionAlgorithm.GZIP);
            assertThat(CompressionAlgorithm.fromName("lz4")).isEqualTo(CompressionAlgorithm.LZ4);
            assertThat(CompressionAlgorithm.fromName("snappy")).isEqualTo(CompressionAlgorithm.SNAPPY);
            assertThat(CompressionAlgorithm.fromName("zstd")).isEqualTo(CompressionAlgorithm.ZSTD);
        }

        @Test
        @DisplayName("fromName should be case-insensitive")
        void fromNameShouldBeCaseInsensitive() {
            assertThat(CompressionAlgorithm.fromName("GZIP")).isEqualTo(CompressionAlgorithm.GZIP);
            assertThat(CompressionAlgorithm.fromName("Gzip")).isEqualTo(CompressionAlgorithm.GZIP);
            assertThat(CompressionAlgorithm.fromName("GZip")).isEqualTo(CompressionAlgorithm.GZIP);
        }

        @Test
        @DisplayName("fromName should return NONE for unknown name")
        void fromNameShouldReturnNoneForUnknownName() {
            assertThat(CompressionAlgorithm.fromName("unknown")).isEqualTo(CompressionAlgorithm.NONE);
            assertThat(CompressionAlgorithm.fromName("bzip2")).isEqualTo(CompressionAlgorithm.NONE);
        }

        @Test
        @DisplayName("fromName should return NONE for null")
        void fromNameShouldReturnNoneForNull() {
            assertThat(CompressionAlgorithm.fromName(null)).isEqualTo(CompressionAlgorithm.NONE);
        }

        @Test
        @DisplayName("fromName should return NONE for empty string")
        void fromNameShouldReturnNoneForEmptyString() {
            assertThat(CompressionAlgorithm.fromName("")).isEqualTo(CompressionAlgorithm.NONE);
        }
    }
}
