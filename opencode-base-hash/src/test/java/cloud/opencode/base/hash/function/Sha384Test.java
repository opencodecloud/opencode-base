package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SHA-384 哈希函数测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("SHA-384 测试")
class Sha384Test {

    private final MessageDigestHashFunction sha384 = MessageDigestHashFunction.sha384();

    @Nested
    @DisplayName("已知向量测试")
    class KnownVectorTest {

        @Test
        @DisplayName("空字符串SHA-384")
        void testEmptyString() {
            // Known SHA-384 of empty string
            String expected = "38b060a751ac96384cd9327eb1b1e36a21fdb71114be07434c0cc7bf63f6e1da274edebfe76f65fbd51ad2f14898b95b";
            HashCode hash = sha384.hashUtf8("");

            assertThat(hash.toHex()).isEqualTo(expected);
        }

        @Test
        @DisplayName("\"abc\"的SHA-384")
        void testAbc() {
            String expected = "cb00753f45a35e8bb5a03d699ac65007272c32ab0eded1631a8b605a43ff5bed8086072ba1e7cc2358baeca134c825a7";
            HashCode hash = sha384.hashUtf8("abc");

            assertThat(hash.toHex()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("属性测试")
    class PropertyTest {

        @Test
        @DisplayName("bits()返回384")
        void testBits() {
            assertThat(sha384.bits()).isEqualTo(384);
        }

        @Test
        @DisplayName("name()返回SHA-384")
        void testName() {
            assertThat(sha384.name()).isEqualTo("SHA-384");
        }
    }
}
