package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.HashFunction;
import cloud.opencode.base.hash.Hasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * MessageDigestHashFunction 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("MessageDigestHashFunction 测试")
class MessageDigestHashFunctionTest {

    @Nested
    @DisplayName("MD5测试")
    class Md5Tests {

        @Test
        @DisplayName("创建MD5哈希函数")
        void testMd5() {
            HashFunction hf = MessageDigestHashFunction.md5();

            assertThat(hf.bits()).isEqualTo(128);
            assertThat(hf.name()).containsIgnoringCase("md5");
        }

        @Test
        @DisplayName("哈希一致性")
        void testConsistency() {
            HashFunction hf = MessageDigestHashFunction.md5();

            HashCode h1 = hf.hashUtf8("hello");
            HashCode h2 = hf.hashUtf8("hello");

            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("不同输入产生不同哈希")
        void testDifferentInputs() {
            HashFunction hf = MessageDigestHashFunction.md5();

            HashCode h1 = hf.hashUtf8("foo");
            HashCode h2 = hf.hashUtf8("bar");

            assertThat(h1).isNotEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("SHA-1测试")
    class Sha1Tests {

        @Test
        @DisplayName("创建SHA-1哈希函数")
        void testSha1() {
            HashFunction hf = MessageDigestHashFunction.sha1();

            assertThat(hf.bits()).isEqualTo(160);
            assertThat(hf.name()).containsIgnoringCase("sha");
        }

        @Test
        @DisplayName("哈希一致性")
        void testConsistency() {
            HashFunction hf = MessageDigestHashFunction.sha1();

            HashCode h1 = hf.hashUtf8("hello");
            HashCode h2 = hf.hashUtf8("hello");

            assertThat(h1).isEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("SHA-256测试")
    class Sha256Tests {

        @Test
        @DisplayName("创建SHA-256哈希函数")
        void testSha256() {
            HashFunction hf = MessageDigestHashFunction.sha256();

            assertThat(hf.bits()).isEqualTo(256);
            assertThat(hf.name()).containsIgnoringCase("sha");
        }

        @Test
        @DisplayName("哈希一致性")
        void testConsistency() {
            HashFunction hf = MessageDigestHashFunction.sha256();

            HashCode h1 = hf.hashUtf8("hello");
            HashCode h2 = hf.hashUtf8("hello");

            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("不同输入产生不同哈希")
        void testDifferentInputs() {
            HashFunction hf = MessageDigestHashFunction.sha256();

            HashCode h1 = hf.hashUtf8("test1");
            HashCode h2 = hf.hashUtf8("test2");

            assertThat(h1).isNotEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("SHA-512测试")
    class Sha512Tests {

        @Test
        @DisplayName("创建SHA-512哈希函数")
        void testSha512() {
            HashFunction hf = MessageDigestHashFunction.sha512();

            assertThat(hf.bits()).isEqualTo(512);
            assertThat(hf.name()).containsIgnoringCase("sha");
        }

        @Test
        @DisplayName("哈希一致性")
        void testConsistency() {
            HashFunction hf = MessageDigestHashFunction.sha512();

            HashCode h1 = hf.hashUtf8("hello world");
            HashCode h2 = hf.hashUtf8("hello world");

            assertThat(h1).isEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("SHA3-256测试")
    class Sha3_256Tests {

        @Test
        @DisplayName("创建SHA3-256哈希函数")
        void testSha3_256() {
            HashFunction hf = MessageDigestHashFunction.sha3_256();

            assertThat(hf.bits()).isEqualTo(256);
            assertThat(hf.name()).containsIgnoringCase("sha3");
        }

        @Test
        @DisplayName("哈希一致性")
        void testConsistency() {
            HashFunction hf = MessageDigestHashFunction.sha3_256();

            HashCode h1 = hf.hashUtf8("test");
            HashCode h2 = hf.hashUtf8("test");

            assertThat(h1).isEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("SHA3-512测试")
    class Sha3_512Tests {

        @Test
        @DisplayName("创建SHA3-512哈希函数")
        void testSha3_512() {
            HashFunction hf = MessageDigestHashFunction.sha3_512();

            assertThat(hf.bits()).isEqualTo(512);
            assertThat(hf.name()).containsIgnoringCase("sha3");
        }

        @Test
        @DisplayName("哈希一致性")
        void testConsistency() {
            HashFunction hf = MessageDigestHashFunction.sha3_512();

            HashCode h1 = hf.hashUtf8("data");
            HashCode h2 = hf.hashUtf8("data");

            assertThat(h1).isEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("自定义算法测试")
    class CreateTests {

        @Test
        @DisplayName("创建自定义算法哈希函数")
        void testCreate() {
            HashFunction hf = MessageDigestHashFunction.create("SHA-384", 384);

            assertThat(hf.bits()).isEqualTo(384);
        }

        @Test
        @DisplayName("不支持的算法抛出异常")
        void testUnsupportedAlgorithm() {
            assertThatThrownBy(() -> MessageDigestHashFunction.create("INVALID", 128))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("哈希字节数据测试")
    class HashBytesTests {

        @Test
        @DisplayName("哈希字节数组")
        void testHashBytes() {
            HashFunction hf = MessageDigestHashFunction.sha256();
            byte[] data = "test".getBytes(StandardCharsets.UTF_8);

            HashCode hash = hf.hashBytes(data);

            assertThat(hash.bits()).isEqualTo(256);
        }

        @Test
        @DisplayName("哈希部分字节数组")
        void testHashBytesPartial() {
            HashFunction hf = MessageDigestHashFunction.sha256();
            byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);

            HashCode hash = hf.hashBytes(data, 0, 5);

            assertThat(hash.bits()).isEqualTo(256);
        }

        @Test
        @DisplayName("空字节数组哈希")
        void testEmptyBytes() {
            HashFunction hf = MessageDigestHashFunction.sha256();

            HashCode hash = hf.hashBytes(new byte[0]);

            assertThat(hash.bits()).isEqualTo(256);
        }
    }

    @Nested
    @DisplayName("哈希基本类型测试")
    class HashPrimitivesTests {

        @Test
        @DisplayName("哈希整数")
        void testHashInt() {
            HashFunction hf = MessageDigestHashFunction.sha256();

            HashCode hash = hf.hashInt(42);

            assertThat(hash.bits()).isEqualTo(256);
        }

        @Test
        @DisplayName("哈希长整数")
        void testHashLong() {
            HashFunction hf = MessageDigestHashFunction.sha256();

            HashCode hash = hf.hashLong(123456789L);

            assertThat(hash.bits()).isEqualTo(256);
        }
    }

    @Nested
    @DisplayName("Hasher流式API测试")
    class HasherTests {

        @Test
        @DisplayName("创建Hasher")
        void testNewHasher() {
            HashFunction hf = MessageDigestHashFunction.sha256();
            Hasher hasher = hf.newHasher();

            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("流式添加数据")
        void testStreamingHash() {
            HashFunction hf = MessageDigestHashFunction.sha256();
            Hasher hasher = hf.newHasher();

            hasher.putByte((byte) 1);
            hasher.putInt(42);
            hasher.putLong(123L);
            hasher.putUtf8("test");
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(256);
        }

        @Test
        @DisplayName("putBytes流式哈希")
        void testPutBytes() {
            HashFunction hf = MessageDigestHashFunction.sha512();
            Hasher hasher = hf.newHasher();

            hasher.putBytes(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(512);
        }

        @Test
        @DisplayName("putShort流式哈希")
        void testPutShort() {
            HashFunction hf = MessageDigestHashFunction.md5();
            Hasher hasher = hf.newHasher();

            hasher.putShort((short) 12345);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(128);
        }

        @Test
        @DisplayName("putFloat流式哈希")
        void testPutFloat() {
            HashFunction hf = MessageDigestHashFunction.sha1();
            Hasher hasher = hf.newHasher();

            hasher.putFloat(3.14f);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(160);
        }

        @Test
        @DisplayName("putDouble流式哈希")
        void testPutDouble() {
            HashFunction hf = MessageDigestHashFunction.sha256();
            Hasher hasher = hf.newHasher();

            hasher.putDouble(3.14159);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(256);
        }

        @Test
        @DisplayName("putBoolean流式哈希")
        void testPutBoolean() {
            HashFunction hf = MessageDigestHashFunction.sha256();
            Hasher hasher = hf.newHasher();

            hasher.putBoolean(true);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(256);
        }

        @Test
        @DisplayName("putChar流式哈希")
        void testPutChar() {
            HashFunction hf = MessageDigestHashFunction.sha256();
            Hasher hasher = hf.newHasher();

            hasher.putChar('A');
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(256);
        }
    }

    @Nested
    @DisplayName("不同算法比较测试")
    class CompareAlgorithmsTests {

        @Test
        @DisplayName("不同算法产生不同哈希")
        void testDifferentAlgorithms() {
            String input = "test";

            HashCode md5 = MessageDigestHashFunction.md5().hashUtf8(input);
            HashCode sha1 = MessageDigestHashFunction.sha1().hashUtf8(input);
            HashCode sha256 = MessageDigestHashFunction.sha256().hashUtf8(input);

            assertThat(md5.toHex()).isNotEqualTo(sha1.toHex());
            assertThat(sha1.toHex()).isNotEqualTo(sha256.toHex());
        }

        @Test
        @DisplayName("SHA-256和SHA3-256产生不同哈希")
        void testSha256VsSha3_256() {
            String input = "test";

            HashCode sha256 = MessageDigestHashFunction.sha256().hashUtf8(input);
            HashCode sha3_256 = MessageDigestHashFunction.sha3_256().hashUtf8(input);

            assertThat(sha256.toHex()).isNotEqualTo(sha3_256.toHex());
        }
    }

    @Nested
    @DisplayName("大数据量测试")
    class LargeDataTests {

        @Test
        @DisplayName("哈希大字节数组")
        void testLargeBytes() {
            HashFunction hf = MessageDigestHashFunction.sha256();
            byte[] data = new byte[100000];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            HashCode hash = hf.hashBytes(data);

            assertThat(hash.bits()).isEqualTo(256);
        }

        @Test
        @DisplayName("流式哈希大量数据")
        void testStreamingLargeData() {
            HashFunction hf = MessageDigestHashFunction.sha512();
            Hasher hasher = hf.newHasher();

            for (int i = 0; i < 10000; i++) {
                hasher.putLong(i);
            }
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(512);
        }
    }

    @Nested
    @DisplayName("十六进制输出测试")
    class HexOutputTests {

        @Test
        @DisplayName("MD5十六进制长度为32")
        void testMd5HexLength() {
            HashFunction hf = MessageDigestHashFunction.md5();
            HashCode hash = hf.hashUtf8("test");

            assertThat(hash.toHex()).hasSize(32);
        }

        @Test
        @DisplayName("SHA-256十六进制长度为64")
        void testSha256HexLength() {
            HashFunction hf = MessageDigestHashFunction.sha256();
            HashCode hash = hf.hashUtf8("test");

            assertThat(hash.toHex()).hasSize(64);
        }

        @Test
        @DisplayName("SHA-512十六进制长度为128")
        void testSha512HexLength() {
            HashFunction hf = MessageDigestHashFunction.sha512();
            HashCode hash = hf.hashUtf8("test");

            assertThat(hash.toHex()).hasSize(128);
        }
    }
}
