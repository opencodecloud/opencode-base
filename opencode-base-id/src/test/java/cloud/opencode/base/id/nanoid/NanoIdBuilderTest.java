package cloud.opencode.base.id.nanoid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.*;

/**
 * NanoIdBuilder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("NanoIdBuilder 测试")
class NanoIdBuilderTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("默认构造方法")
        void testConstructor() {
            NanoIdBuilder builder = new NanoIdBuilder();

            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("配置方法测试")
    class ConfigurationTests {

        @Test
        @DisplayName("设置长度")
        void testSize() {
            NanoIdGenerator gen = new NanoIdBuilder()
                    .size(10)
                    .build();

            assertThat(gen.getSize()).isEqualTo(10);
            assertThat(gen.generate()).hasSize(10);
        }

        @Test
        @DisplayName("设置字母表字符串")
        void testAlphabetString() {
            NanoIdGenerator gen = new NanoIdBuilder()
                    .alphabet("0123456789")
                    .build();

            assertThat(gen.getAlphabet()).isEqualTo("0123456789");
            assertThat(gen.generate()).matches("[0-9]+");
        }

        @Test
        @DisplayName("设置字母表枚举")
        void testAlphabetEnum() {
            NanoIdGenerator gen = new NanoIdBuilder()
                    .alphabet(Alphabet.NUMERIC)
                    .build();

            assertThat(gen.getAlphabet()).isEqualTo(Alphabet.NUMERIC.getChars());
            assertThat(gen.generate()).matches("[0-9]+");
        }

        @Test
        @DisplayName("设置随机源")
        void testRandom() {
            Random random = new Random(12345);
            NanoIdGenerator gen = new NanoIdBuilder()
                    .random(random)
                    .build();

            assertThat(gen).isNotNull();
        }

        @Test
        @DisplayName("使用SecureRandom")
        void testSecureRandom() {
            NanoIdGenerator gen = new NanoIdBuilder()
                    .secureRandom()
                    .build();

            assertThat(gen).isNotNull();
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentTests {

        @Test
        @DisplayName("链式配置")
        void testFluentConfiguration() {
            NanoIdGenerator gen = new NanoIdBuilder()
                    .size(16)
                    .alphabet(Alphabet.ALPHANUMERIC)
                    .secureRandom()
                    .build();

            assertThat(gen).isNotNull();
            assertThat(gen.getSize()).isEqualTo(16);
            assertThat(gen.getAlphabet()).isEqualTo(Alphabet.ALPHANUMERIC.getChars());
        }
    }

    @Nested
    @DisplayName("验证测试")
    class ValidationTests {

        @Test
        @DisplayName("无效长度抛出异常")
        void testInvalidSize() {
            assertThatThrownBy(() -> new NanoIdBuilder().size(0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> new NanoIdBuilder().size(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("无效字母表抛出异常")
        void testInvalidAlphabet() {
            assertThatThrownBy(() -> new NanoIdBuilder().alphabet((String) null))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> new NanoIdBuilder().alphabet(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("字母表过长抛出异常")
        void testAlphabetTooLong() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 257; i++) {
                sb.append((char) i);
            }

            assertThatThrownBy(() -> new NanoIdBuilder().alphabet(sb.toString()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("构建测试")
    class BuildTests {

        @Test
        @DisplayName("默认构建")
        void testDefaultBuild() {
            NanoIdGenerator gen = new NanoIdBuilder().build();

            assertThat(gen).isNotNull();
            assertThat(gen.getSize()).isEqualTo(NanoIdGenerator.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("多次构建不同实例")
        void testMultipleBuild() {
            NanoIdBuilder builder = new NanoIdBuilder();
            NanoIdGenerator gen1 = builder.build();
            NanoIdGenerator gen2 = builder.build();

            assertThat(gen1).isNotSameAs(gen2);
        }
    }
}
