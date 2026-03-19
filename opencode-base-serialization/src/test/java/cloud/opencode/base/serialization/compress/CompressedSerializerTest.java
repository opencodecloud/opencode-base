package cloud.opencode.base.serialization.compress;

import cloud.opencode.base.serialization.Serializer;
import cloud.opencode.base.serialization.TypeReference;
import cloud.opencode.base.serialization.exception.OpenSerializationException;
import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CompressedSerializerTest Tests
 * CompressedSerializerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
@DisplayName("CompressedSerializer Tests")
class CompressedSerializerTest {

    private Serializer mockDelegate;

    @BeforeEach
    void setUp() {
        mockDelegate = mock(Serializer.class);
        when(mockDelegate.getFormat()).thenReturn("mock");
        when(mockDelegate.getMimeType()).thenReturn("application/mock");
        when(mockDelegate.supports(any())).thenReturn(true);
        when(mockDelegate.isTextBased()).thenReturn(true);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor with two args should use default threshold")
        void constructorWithTwoArgsShouldUseDefaultThreshold() {
            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP);

            assertThat(serializer.getThreshold()).isEqualTo(CompressedSerializer.DEFAULT_THRESHOLD);
            assertThat(serializer.getDelegate()).isSameAs(mockDelegate);
            assertThat(serializer.getAlgorithm()).isEqualTo(CompressionAlgorithm.GZIP);
        }

        @Test
        @DisplayName("Constructor with three args should use custom threshold")
        void constructorWithThreeArgsShouldUseCustomThreshold() {
            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP, 2048);

            assertThat(serializer.getThreshold()).isEqualTo(2048);
        }

        @Test
        @DisplayName("Constructor should reject null delegate")
        void constructorShouldRejectNullDelegate() {
            assertThatThrownBy(() -> new CompressedSerializer(null, CompressionAlgorithm.GZIP))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Delegate");
        }

        @Test
        @DisplayName("Constructor should reject null algorithm")
        void constructorShouldRejectNullAlgorithm() {
            assertThatThrownBy(() -> new CompressedSerializer(mockDelegate, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Algorithm");
        }

        @Test
        @DisplayName("Constructor should reject negative threshold")
        void constructorShouldRejectNegativeThreshold() {
            assertThatThrownBy(() -> new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("non-negative");
        }

        @Test
        @DisplayName("Constructor should accept zero threshold")
        void constructorShouldAcceptZeroThreshold() {
            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP, 0);

            assertThat(serializer.getThreshold()).isZero();
        }
    }

    @Nested
    @DisplayName("Serialize Tests")
    class SerializeTests {

        @Test
        @DisplayName("Should not compress data below threshold")
        void shouldNotCompressDataBelowThreshold() {
            byte[] smallData = "small".getBytes(StandardCharsets.UTF_8);
            when(mockDelegate.serialize(any())).thenReturn(smallData);

            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP, 1024);
            byte[] result = serializer.serialize("small");

            // First byte is header (NONE = 0), followed by original data
            assertThat(result[0]).isEqualTo((byte) 0);
            assertThat(result.length).isEqualTo(smallData.length + 1);
        }

        @Test
        @DisplayName("Should compress data above threshold with GZIP")
        void shouldCompressDataAboveThresholdWithGzip() {
            // Create data larger than threshold
            byte[] largeData = new byte[2048];
            java.util.Arrays.fill(largeData, (byte) 'a'); // Highly compressible
            when(mockDelegate.serialize(any())).thenReturn(largeData);

            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP, 1024);
            byte[] result = serializer.serialize("large");

            // First byte is header (GZIP = 1)
            assertThat(result[0]).isEqualTo((byte) 1);
            // Compressed data should be smaller (for compressible content)
            assertThat(result.length).isLessThan(largeData.length);
        }

        @Test
        @DisplayName("Should not compress when algorithm is NONE")
        void shouldNotCompressWhenAlgorithmIsNone() {
            byte[] largeData = new byte[2048];
            when(mockDelegate.serialize(any())).thenReturn(largeData);

            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.NONE, 100);
            byte[] result = serializer.serialize("large");

            // First byte is header (NONE = 0)
            assertThat(result[0]).isEqualTo((byte) 0);
            assertThat(result.length).isEqualTo(largeData.length + 1);
        }
    }

    @Nested
    @DisplayName("Deserialize Tests")
    class DeserializeTests {

        @Test
        @DisplayName("Should deserialize uncompressed data")
        void shouldDeserializeUncompressedData() {
            byte[] originalData = "test".getBytes(StandardCharsets.UTF_8);
            // Create data with NONE header
            byte[] dataWithHeader = new byte[originalData.length + 1];
            dataWithHeader[0] = 0; // NONE
            System.arraycopy(originalData, 0, dataWithHeader, 1, originalData.length);

            when(mockDelegate.deserialize(any(byte[].class), eq(String.class))).thenReturn("test");

            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP);
            String result = serializer.deserialize(dataWithHeader, String.class);

            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("Should throw for empty data")
        void shouldThrowForEmptyData() {
            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP);

            assertThatThrownBy(() -> serializer.deserialize(new byte[0], String.class))
                    .isInstanceOf(OpenSerializationException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("Should deserialize with TypeReference")
        void shouldDeserializeWithTypeReference() {
            byte[] originalData = "[]".getBytes(StandardCharsets.UTF_8);
            byte[] dataWithHeader = new byte[originalData.length + 1];
            dataWithHeader[0] = 0; // NONE
            System.arraycopy(originalData, 0, dataWithHeader, 1, originalData.length);

            TypeReference<List<String>> typeRef = new TypeReference<>() {};
            when(mockDelegate.deserialize(any(byte[].class), eq(typeRef))).thenReturn(List.of());

            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP);
            List<String> result = serializer.deserialize(dataWithHeader, typeRef);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should deserialize with Type")
        void shouldDeserializeWithType() {
            byte[] originalData = "test".getBytes(StandardCharsets.UTF_8);
            byte[] dataWithHeader = new byte[originalData.length + 1];
            dataWithHeader[0] = 0; // NONE
            System.arraycopy(originalData, 0, dataWithHeader, 1, originalData.length);

            Type type = String.class;
            when(mockDelegate.deserialize(any(byte[].class), eq(type))).thenReturn("test");

            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP);
            String result = serializer.deserialize(dataWithHeader, type);

            assertThat(result).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("Round-trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should round-trip with GZIP compression")
        void shouldRoundTripWithGzipCompression() {
            // Create a simple serializer that works with bytes
            Serializer simpleSerializer = new Serializer() {
                @Override
                public byte[] serialize(Object obj) {
                    return obj.toString().getBytes(StandardCharsets.UTF_8);
                }

                @Override
                public <T> T deserialize(byte[] data, Class<T> type) {
                    return type.cast(new String(data, StandardCharsets.UTF_8));
                }

                @Override
                @SuppressWarnings("unchecked")
                public <T> T deserialize(byte[] data, TypeReference<T> typeRef) {
                    return (T) new String(data, StandardCharsets.UTF_8);
                }

                @Override
                @SuppressWarnings("unchecked")
                public <T> T deserialize(byte[] data, Type type) {
                    return (T) new String(data, StandardCharsets.UTF_8);
                }

                @Override
                public String getFormat() {
                    return "simple";
                }
            };

            // Create large compressible data
            String original = "a".repeat(2000);

            var compressed = new CompressedSerializer(simpleSerializer, CompressionAlgorithm.GZIP, 100);
            byte[] serialized = compressed.serialize(original);
            String deserialized = compressed.deserialize(serialized, String.class);

            assertThat(deserialized).isEqualTo(original);
            // Verify compression happened
            assertThat(serialized[0]).isEqualTo((byte) 1); // GZIP header
        }
    }

    @Nested
    @DisplayName("getFormat Tests")
    class GetFormatTests {

        @Test
        @DisplayName("Should return delegate format when algorithm is NONE")
        void shouldReturnDelegateFormatWhenAlgorithmIsNone() {
            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.NONE);

            assertThat(serializer.getFormat()).isEqualTo("mock");
        }

        @Test
        @DisplayName("Should return combined format when compression enabled")
        void shouldReturnCombinedFormatWhenCompressionEnabled() {
            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP);

            assertThat(serializer.getFormat()).isEqualTo("mock+gzip");
        }

        @Test
        @DisplayName("Should return combined format for LZ4")
        void shouldReturnCombinedFormatForLz4() {
            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.LZ4);

            assertThat(serializer.getFormat()).isEqualTo("mock+lz4");
        }
    }

    @Nested
    @DisplayName("Delegate Methods Tests")
    class DelegateMethodsTests {

        @Test
        @DisplayName("getMimeType should delegate")
        void getMimeTypeShouldDelegate() {
            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP);

            assertThat(serializer.getMimeType()).isEqualTo("application/mock");
        }

        @Test
        @DisplayName("supports should delegate")
        void supportsShouldDelegate() {
            when(mockDelegate.supports(String.class)).thenReturn(true);
            when(mockDelegate.supports(Integer.class)).thenReturn(false);

            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP);

            assertThat(serializer.supports(String.class)).isTrue();
            assertThat(serializer.supports(Integer.class)).isFalse();
        }

        @Test
        @DisplayName("isTextBased should always return false")
        void isTextBasedShouldAlwaysReturnFalse() {
            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP);

            // Compressed data is always binary
            assertThat(serializer.isTextBased()).isFalse();
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getDelegate should return delegate")
        void getDelegateShouldReturnDelegate() {
            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP);

            assertThat(serializer.getDelegate()).isSameAs(mockDelegate);
        }

        @Test
        @DisplayName("getAlgorithm should return algorithm")
        void getAlgorithmShouldReturnAlgorithm() {
            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.SNAPPY);

            assertThat(serializer.getAlgorithm()).isEqualTo(CompressionAlgorithm.SNAPPY);
        }

        @Test
        @DisplayName("getThreshold should return threshold")
        void getThresholdShouldReturnThreshold() {
            var serializer = new CompressedSerializer(mockDelegate, CompressionAlgorithm.GZIP, 4096);

            assertThat(serializer.getThreshold()).isEqualTo(4096);
        }
    }

    @Nested
    @DisplayName("DEFAULT_THRESHOLD Tests")
    class DefaultThresholdTests {

        @Test
        @DisplayName("DEFAULT_THRESHOLD should be 1024")
        void defaultThresholdShouldBe1024() {
            assertThat(CompressedSerializer.DEFAULT_THRESHOLD).isEqualTo(1024);
        }
    }
}
