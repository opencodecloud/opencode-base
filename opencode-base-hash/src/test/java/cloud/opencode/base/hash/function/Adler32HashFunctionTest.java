package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.HashFunction;
import cloud.opencode.base.hash.Hasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.zip.Adler32;

import static org.assertj.core.api.Assertions.*;

/**
 * Adler32HashFunction tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.3
 */
@DisplayName("Adler32HashFunction Tests")
class Adler32HashFunctionTest {

    /**
     * Computes reference Adler-32 using java.util.zip.Adler32 directly
     */
    private static int referenceAdler32(byte[] data) {
        Adler32 adler = new Adler32();
        adler.update(data);
        return (int) adler.getValue();
    }

    @Nested
    @DisplayName("Known Input Tests")
    class KnownInputTests {

        @Test
        @DisplayName("should match java.util.zip.Adler32 for ASCII input")
        void testMatchesJdkAdler32() {
            byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);
            HashFunction hf = Adler32HashFunction.adler32();

            HashCode hash = hf.hashBytes(data);

            assertThat(hash.asInt()).isEqualTo(referenceAdler32(data));
        }

        @Test
        @DisplayName("should match java.util.zip.Adler32 for binary data")
        void testBinaryData() {
            byte[] data = new byte[]{0, 1, 2, 3, 127, (byte) 128, (byte) 255};
            HashFunction hf = Adler32HashFunction.adler32();

            HashCode hash = hf.hashBytes(data);

            assertThat(hash.asInt()).isEqualTo(referenceAdler32(data));
        }

        @Test
        @DisplayName("should match java.util.zip.Adler32 for unicode input")
        void testUnicodeInput() {
            byte[] data = "你好世界".getBytes(StandardCharsets.UTF_8);
            HashFunction hf = Adler32HashFunction.adler32();

            HashCode hash = hf.hashBytes(data);

            assertThat(hash.asInt()).isEqualTo(referenceAdler32(data));
        }

        @Test
        @DisplayName("should have 32 bits output")
        void testBits() {
            HashFunction hf = Adler32HashFunction.adler32();
            assertThat(hf.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("should have correct name")
        void testName() {
            HashFunction hf = Adler32HashFunction.adler32();
            assertThat(hf.name()).isEqualTo("adler32");
        }
    }

    @Nested
    @DisplayName("Empty Input Tests")
    class EmptyInputTests {

        @Test
        @DisplayName("should handle empty byte array")
        void testEmptyBytes() {
            HashFunction hf = Adler32HashFunction.adler32();

            HashCode hash = hf.hashBytes(new byte[0]);

            assertThat(hash.asInt()).isEqualTo(referenceAdler32(new byte[0]));
            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("should handle empty string")
        void testEmptyString() {
            HashFunction hf = Adler32HashFunction.adler32();

            HashCode hash = hf.hashUtf8("");

            assertThat(hash.asInt()).isEqualTo(referenceAdler32(new byte[0]));
        }
    }

    @Nested
    @DisplayName("Streaming Hasher Tests")
    class StreamingHasherTests {

        @Test
        @DisplayName("streaming should match direct hash")
        void testStreamingMatchesDirect() {
            byte[] data = "streaming test data".getBytes(StandardCharsets.UTF_8);
            HashFunction hf = Adler32HashFunction.adler32();

            HashCode direct = hf.hashBytes(data);

            Hasher hasher = hf.newHasher();
            hasher.putBytes(data);
            HashCode streamed = hasher.hash();

            assertThat(streamed).isEqualTo(direct);
        }

        @Test
        @DisplayName("byte-by-byte streaming should match direct hash")
        void testByteByByte() {
            byte[] data = "byte by byte".getBytes(StandardCharsets.UTF_8);
            HashFunction hf = Adler32HashFunction.adler32();

            HashCode direct = hf.hashBytes(data);

            Hasher hasher = hf.newHasher();
            for (byte b : data) {
                hasher.putByte(b);
            }
            HashCode streamed = hasher.hash();

            assertThat(streamed).isEqualTo(direct);
        }

        @Test
        @DisplayName("putInt streaming should work")
        void testPutInt() {
            HashFunction hf = Adler32HashFunction.adler32();
            Hasher hasher = hf.newHasher();

            hasher.putInt(42);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putLong streaming should work")
        void testPutLong() {
            HashFunction hf = Adler32HashFunction.adler32();
            Hasher hasher = hf.newHasher();

            hasher.putLong(123456789L);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putUtf8 streaming should work")
        void testPutUtf8() {
            HashFunction hf = Adler32HashFunction.adler32();
            Hasher hasher = hf.newHasher();

            hasher.putUtf8("Hello");
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("Consistency Tests")
    class ConsistencyTests {

        @Test
        @DisplayName("same input should always produce same output")
        void testConsistency() {
            HashFunction hf = Adler32HashFunction.adler32();
            byte[] data = "consistency check".getBytes(StandardCharsets.UTF_8);

            HashCode h1 = hf.hashBytes(data);
            HashCode h2 = hf.hashBytes(data);
            HashCode h3 = hf.hashBytes(data);

            assertThat(h1).isEqualTo(h2);
            assertThat(h2).isEqualTo(h3);
        }

        @Test
        @DisplayName("different inputs should produce different outputs")
        void testDifferentInputs() {
            HashFunction hf = Adler32HashFunction.adler32();

            HashCode h1 = hf.hashUtf8("foo");
            HashCode h2 = hf.hashUtf8("bar");

            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("hashBytes with offset and length should work correctly")
        void testHashBytesWithOffset() {
            HashFunction hf = Adler32HashFunction.adler32();
            byte[] full = "Hello, World!".getBytes(StandardCharsets.UTF_8);
            byte[] sub = "World".getBytes(StandardCharsets.UTF_8);

            HashCode fromFull = hf.hashBytes(full, 7, 5);
            HashCode fromSub = hf.hashBytes(sub);

            assertThat(fromFull).isEqualTo(fromSub);
        }

        @Test
        @DisplayName("toString format should be name[bits]")
        void testToString() {
            HashFunction hf = Adler32HashFunction.adler32();
            assertThat(hf.toString()).isEqualTo("adler32[32]");
        }
    }
}
