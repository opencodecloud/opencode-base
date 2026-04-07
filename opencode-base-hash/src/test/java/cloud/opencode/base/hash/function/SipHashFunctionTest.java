package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.HashFunction;
import cloud.opencode.base.hash.Hasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SipHashFunction 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.3
 */
@DisplayName("SipHashFunction 测试")
class SipHashFunctionTest {

    @Nested
    @DisplayName("基本测试")
    class BasicTest {

        @Test
        @DisplayName("已知测试向量: SipHash-2-4 canonical vector")
        void testKnownVector() {
            // From the SipHash paper: key = 00 01 02 ... 0f, input = 00 01 02 ... 0e
            // Expected output: 0xa129ca6149be45e5
            long k0 = 0x0706050403020100L;
            long k1 = 0x0f0e0d0c0b0a0908L;
            HashFunction hf = SipHashFunction.sipHash24(k0, k1);

            byte[] input = new byte[15];
            for (int i = 0; i < 15; i++) {
                input[i] = (byte) i;
            }

            HashCode hash = hf.hashBytes(input);
            assertThat(hash.asLong()).isEqualTo(0xa129ca6149be45e5L);
        }

        @Test
        @DisplayName("空输入哈希")
        void testEmptyInput() {
            HashFunction hf = SipHashFunction.sipHash24();

            HashCode hash = hf.hashBytes(new byte[0]);

            assertThat(hash.bits()).isEqualTo(64);
            // Empty input should produce a valid, deterministic hash
            HashCode hash2 = hf.hashBytes(new byte[0]);
            assertThat(hash).isEqualTo(hash2);
        }

        @Test
        @DisplayName("相同输入产生相同输出（确定性）")
        void testDeterminism() {
            HashFunction hf = SipHashFunction.sipHash24(123L, 456L);

            HashCode h1 = hf.hashUtf8("deterministic test");
            HashCode h2 = hf.hashUtf8("deterministic test");

            assertThat(h1).isEqualTo(h2);
            assertThat(h1.asLong()).isEqualTo(h2.asLong());
        }

        @Test
        @DisplayName("不同密钥产生不同哈希")
        void testDifferentKeysProduceDifferentHashes() {
            HashFunction hf1 = SipHashFunction.sipHash24(1L, 2L);
            HashFunction hf2 = SipHashFunction.sipHash24(3L, 4L);

            HashCode h1 = hf1.hashUtf8("same input");
            HashCode h2 = hf2.hashUtf8("same input");

            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("不同输入产生不同哈希")
        void testDifferentInputsProduceDifferentHashes() {
            HashFunction hf = SipHashFunction.sipHash24();

            HashCode h1 = hf.hashUtf8("hello");
            HashCode h2 = hf.hashUtf8("world");

            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("多个已知测试向量验证")
        void testAdditionalKnownVectors() {
            // Key: 00 01 02 ... 0f
            long k0 = 0x0706050403020100L;
            long k1 = 0x0f0e0d0c0b0a0908L;
            HashFunction hf = SipHashFunction.sipHash24(k0, k1);

            // input = [] (empty), expected from reference implementation
            HashCode emptyHash = hf.hashBytes(new byte[0]);
            assertThat(emptyHash.asLong()).isEqualTo(0x726fdb47dd0e0e31L);

            // input = [0x00], expected from reference implementation
            HashCode oneByteHash = hf.hashBytes(new byte[]{0x00});
            assertThat(oneByteHash.asLong()).isEqualTo(0x74f839c593dc67fdL);
        }
    }

    @Nested
    @DisplayName("Hasher流式API测试")
    class HasherTest {

        @Test
        @DisplayName("流式哈希与直接hashBytes产生相同结果")
        void testStreamingMatchesDirect() {
            HashFunction hf = SipHashFunction.sipHash24(42L, 99L);
            byte[] data = "Hello SipHash".getBytes(java.nio.charset.StandardCharsets.UTF_8);

            HashCode direct = hf.hashBytes(data);

            Hasher hasher = hf.newHasher();
            hasher.putBytes(data);
            HashCode streamed = hasher.hash();

            assertThat(streamed).isEqualTo(direct);
            assertThat(streamed.asLong()).isEqualTo(direct.asLong());
        }

        @Test
        @DisplayName("逐字节流式与一次性putBytes产生相同结果")
        void testByteByByteMatchesBulk() {
            HashFunction hf = SipHashFunction.sipHash24(7L, 13L);
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

            HashCode bulk = hf.hashBytes(data);

            Hasher hasher = hf.newHasher();
            for (byte b : data) {
                hasher.putByte(b);
            }
            HashCode byteByByte = hasher.hash();

            assertThat(byteByByte).isEqualTo(bulk);
        }

        @Test
        @DisplayName("putInt然后putLong然后hash")
        void testPutIntThenPutLongThenHash() {
            HashFunction hf = SipHashFunction.sipHash24(100L, 200L);
            Hasher hasher = hf.newHasher();

            hasher.putInt(42);
            hasher.putLong(123456789L);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(64);
            assertThat(hash.asLong()).isNotZero();

            // Verify determinism: same sequence produces same result
            Hasher hasher2 = hf.newHasher();
            hasher2.putInt(42);
            hasher2.putLong(123456789L);
            HashCode hash2 = hasher2.hash();

            assertThat(hash).isEqualTo(hash2);
        }

        @Test
        @DisplayName("流式哈希各种类型")
        void testStreamingVariousTypes() {
            HashFunction hf = SipHashFunction.sipHash24();
            Hasher hasher = hf.newHasher();

            hasher.putByte((byte) 1);
            hasher.putShort((short) 2);
            hasher.putInt(3);
            hasher.putLong(4L);
            hasher.putFloat(5.0f);
            hasher.putDouble(6.0);
            hasher.putBoolean(true);
            hasher.putChar('Z');
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("Hasher只能hash一次")
        void testHasherCanOnlyBeUsedOnce() {
            HashFunction hf = SipHashFunction.sipHash24();
            Hasher hasher = hf.newHasher();
            hasher.putInt(1);
            hasher.hash();

            assertThatThrownBy(hasher::hash)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryTest {

        @Test
        @DisplayName("sipHash24()创建有效函数")
        void testSipHash24DefaultCreatesValidFunction() {
            HashFunction hf = SipHashFunction.sipHash24();

            assertThat(hf).isNotNull();
            HashCode hash = hf.hashUtf8("test");
            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("sipHash24(k0, k1)创建有效函数")
        void testSipHash24WithKeyCreatesValidFunction() {
            HashFunction hf = SipHashFunction.sipHash24(0xDEADBEEFL, 0xCAFEBABEL);

            assertThat(hf).isNotNull();
            HashCode hash = hf.hashUtf8("test");
            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("bits()返回64")
        void testBitsReturns64() {
            assertThat(SipHashFunction.sipHash24().bits()).isEqualTo(64);
            assertThat(SipHashFunction.sipHash24(1L, 2L).bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("name()返回SipHash-2-4")
        void testNameReturnsSipHash24() {
            assertThat(SipHashFunction.sipHash24().name()).isEqualTo("SipHash-2-4");
            assertThat(SipHashFunction.sipHash24(1L, 2L).name()).isEqualTo("SipHash-2-4");
        }

        @Test
        @DisplayName("toString()不暴露密钥")
        void testToStringDoesNotExposeKey() {
            String str = SipHashFunction.sipHash24(0xDEADBEEFL, 0xCAFEBABEL).toString();

            assertThat(str).doesNotContain("DEADBEEF");
            assertThat(str).doesNotContain("CAFEBABE");
            assertThat(str).contains("SipHash-2-4");
        }

        @Test
        @DisplayName("hashInt和hashLong")
        void testHashIntAndHashLong() {
            HashFunction hf = SipHashFunction.sipHash24();

            HashCode intHash = hf.hashInt(42);
            HashCode longHash = hf.hashLong(42L);

            assertThat(intHash.bits()).isEqualTo(64);
            assertThat(longHash.bits()).isEqualTo(64);
            // int(42) and long(42) should produce different hashes (different byte lengths)
            assertThat(intHash).isNotEqualTo(longHash);
        }

        @Test
        @DisplayName("newHasher(expectedInputSize)创建有效Hasher")
        void testNewHasherWithExpectedSize() {
            HashFunction hf = SipHashFunction.sipHash24();
            Hasher hasher = hf.newHasher(1024);

            assertThat(hasher).isNotNull();
            hasher.putUtf8("test");
            HashCode hash = hasher.hash();
            assertThat(hash.bits()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTest {

        @Test
        @DisplayName("恰好8字节输入（一个完整块）")
        void testExactlyOneBlock() {
            HashFunction hf = SipHashFunction.sipHash24();
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8};

            HashCode hash = hf.hashBytes(data);
            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("恰好16字节输入（两个完整块）")
        void testExactlyTwoBlocks() {
            HashFunction hf = SipHashFunction.sipHash24();
            byte[] data = new byte[16];
            for (int i = 0; i < 16; i++) {
                data[i] = (byte) i;
            }

            HashCode hash = hf.hashBytes(data);
            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("部分字节数组哈希")
        void testPartialByteArray() {
            HashFunction hf = SipHashFunction.sipHash24();
            byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

            HashCode full = hf.hashBytes(data, 2, 5);
            HashCode sub = hf.hashBytes(new byte[]{2, 3, 4, 5, 6});

            assertThat(full).isEqualTo(sub);
        }

        @Test
        @DisplayName("大数据量测试")
        void testLargeInput() {
            HashFunction hf = SipHashFunction.sipHash24();
            byte[] data = new byte[100000];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            HashCode hash = hf.hashBytes(data);
            assertThat(hash.bits()).isEqualTo(64);

            // Determinism for large inputs
            HashCode hash2 = hf.hashBytes(data);
            assertThat(hash).isEqualTo(hash2);
        }

        @Test
        @DisplayName("每种余数长度(1-7)的输入")
        void testAllRemainderLengths() {
            HashFunction hf = SipHashFunction.sipHash24(0xABL, 0xCDL);

            for (int len = 1; len <= 7; len++) {
                byte[] data = new byte[len];
                for (int i = 0; i < len; i++) {
                    data[i] = (byte) (i + 1);
                }
                HashCode hash = hf.hashBytes(data);
                assertThat(hash.bits()).isEqualTo(64);
            }
        }
    }
}
