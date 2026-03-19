package cloud.opencode.base.web.crypto;

import cloud.opencode.base.web.Result;
import org.junit.jupiter.api.*;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AbstractResultEncryptor 抽象基类测试")
class AbstractResultEncryptorTest {

    private TestEncryptor encryptor;

    @BeforeEach
    void setup() {
        encryptor = new TestEncryptor();
    }

    @Nested
    @DisplayName("serializeData方法测试")
    class SerializeDataTests {

        @Test
        @DisplayName("序列化null返回null字符串")
        void testSerializeNull() {
            assertThat(encryptor.testSerialize(null)).isEqualTo("null");
        }

        @Test
        @DisplayName("序列化String带引号")
        void testSerializeString() {
            String result = encryptor.testSerialize("hello");
            assertThat(result).isEqualTo("\"hello\"");
        }

        @Test
        @DisplayName("序列化数字不带引号")
        void testSerializeNumber() {
            assertThat(encryptor.testSerialize(42)).isEqualTo("42");
            assertThat(encryptor.testSerialize(3.14)).isEqualTo("3.14");
        }

        @Test
        @DisplayName("序列化布尔值不带引号")
        void testSerializeBoolean() {
            assertThat(encryptor.testSerialize(true)).isEqualTo("true");
            assertThat(encryptor.testSerialize(false)).isEqualTo("false");
        }
    }

    @Nested
    @DisplayName("deserializeData方法测试")
    class DeserializeDataTests {

        @Test
        @DisplayName("反序列化null字符串返回null")
        void testDeserializeNull() {
            assertThat(encryptor.testDeserialize("null", String.class)).isNull();
            assertThat(encryptor.testDeserialize(null, String.class)).isNull();
        }

        @Test
        @DisplayName("反序列化带引号的String")
        void testDeserializeString() {
            String result = encryptor.testDeserialize("\"hello\"", String.class);
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("反序列化Integer")
        void testDeserializeInteger() {
            Integer result = encryptor.testDeserialize("42", Integer.class);
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("反序列化Long")
        void testDeserializeLong() {
            Long result = encryptor.testDeserialize("123456789", Long.class);
            assertThat(result).isEqualTo(123456789L);
        }

        @Test
        @DisplayName("反序列化Boolean")
        void testDeserializeBoolean() {
            Boolean result = encryptor.testDeserialize("true", Boolean.class);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("反序列化Double")
        void testDeserializeDouble() {
            Double result = encryptor.testDeserialize("3.14", Double.class);
            assertThat(result).isEqualTo(3.14);
        }
    }

    @Nested
    @DisplayName("encrypt和decrypt方法测试")
    class EncryptDecryptTests {

        @Test
        @DisplayName("encrypt加密Result")
        void testEncrypt() {
            Result<String> result = new Result<>("00000", "Success", "hello", true, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(result);
            assertThat(encrypted).isNotNull();
            assertThat(encrypted.code()).isEqualTo("00000");
            assertThat(encrypted.algorithm()).isEqualTo("TEST");
            assertThat(encrypted.encryptedData()).isNotNull();
        }

        @Test
        @DisplayName("encrypt处理null数据")
        void testEncryptNullData() {
            Result<String> result = new Result<>("00000", "Success", null, true, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(result);
            assertThat(encrypted).isNotNull();
        }

        @Test
        @DisplayName("decrypt解密EncryptedResult")
        void testDecrypt() {
            Result<String> original = new Result<>("00000", "Success", "hello", true, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(original);
            Result<String> decrypted = encryptor.decrypt(encrypted, String.class);
            assertThat(decrypted).isNotNull();
            assertThat(decrypted.data()).isEqualTo("hello");
        }
    }

    /**
     * Test implementation that simply reverses bytes (no real encryption).
      *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
    static class TestEncryptor extends AbstractResultEncryptor {

        @Override
        protected byte[] doEncrypt(byte[] data) {
            byte[] result = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                result[i] = data[data.length - 1 - i];
            }
            return result;
        }

        @Override
        protected byte[] doDecrypt(byte[] data) {
            // Reverse is self-inverse
            return doEncrypt(data);
        }

        @Override
        public String getAlgorithm() {
            return "TEST";
        }

        // Expose protected methods for testing
        public String testSerialize(Object data) {
            return serializeData(data);
        }

        public <T> T testDeserialize(String json, Class<T> type) {
            return deserializeData(json, type);
        }
    }
}
