package cloud.opencode.base.cache.spi;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CacheSerializerTest Tests
 * CacheSerializerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheSerializer 接口测试")
class CacheSerializerTest {

    @Nested
    @DisplayName("string序列化器测试")
    class StringSerializerTests {

        @Test
        @DisplayName("序列化和反序列化字符串")
        void testStringRoundTrip() {
            CacheSerializer<String> serializer = CacheSerializer.string();

            byte[] bytes = serializer.serialize("hello");
            String result = serializer.deserialize(bytes);

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("UTF-8编码正确处理中文")
        void testUtf8Chinese() {
            CacheSerializer<String> serializer = CacheSerializer.string();

            byte[] bytes = serializer.serialize("你好世界");
            String result = serializer.deserialize(bytes);

            assertThat(result).isEqualTo("你好世界");
        }
    }

    @Nested
    @DisplayName("identity序列化器测试")
    class IdentitySerializerTests {

        @Test
        @DisplayName("字节数组透传")
        void testIdentity() {
            CacheSerializer<byte[]> serializer = CacheSerializer.identity();
            byte[] input = {1, 2, 3};

            byte[] result = serializer.serialize(input);

            assertThat(result).isEqualTo(input);
        }

        @Test
        @DisplayName("反序列化返回原始字节")
        void testDeserialize() {
            CacheSerializer<byte[]> serializer = CacheSerializer.identity();
            byte[] input = {4, 5, 6};

            byte[] result = serializer.deserialize(input);

            assertThat(result).isEqualTo(input);
        }
    }

    @Nested
    @DisplayName("JDK序列化器测试")
    class JdkSerializerTests {

        @Test
        @DisplayName("序列化和反序列化Serializable对象")
        void testJdkRoundTrip() {
            CacheSerializer<String> serializer = CacheSerializer.jdk();

            byte[] bytes = serializer.serialize("test");
            String result = serializer.deserialize(bytes);

            assertThat(result).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("compressed序列化器测试")
    class CompressedSerializerTests {

        @Test
        @DisplayName("压缩序列化和解压反序列化")
        void testCompressedRoundTrip() {
            CacheSerializer<String> base = CacheSerializer.string();
            CacheSerializer<String> compressed = CacheSerializer.compressed(base);

            byte[] bytes = compressed.serialize("hello world hello world hello world");
            String result = compressed.deserialize(bytes);

            assertThat(result).isEqualTo("hello world hello world hello world");
        }
    }

    @Nested
    @DisplayName("estimateSize默认方法测试")
    class EstimateSizeTests {

        @Test
        @DisplayName("estimateSize返回序列化后的字节长度")
        void testEstimateSize() {
            CacheSerializer<String> serializer = CacheSerializer.string();

            long size = serializer.estimateSize("hello");

            assertThat(size).isEqualTo(5);
        }
    }
}
