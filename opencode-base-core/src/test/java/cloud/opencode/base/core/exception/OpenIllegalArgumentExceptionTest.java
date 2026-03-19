package cloud.opencode.base.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenIllegalArgumentException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenIllegalArgumentException 测试")
class OpenIllegalArgumentExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageConstructor() {
            OpenIllegalArgumentException ex = new OpenIllegalArgumentException("Invalid param");

            assertThat(ex.getMessage()).isEqualTo("[Core] (ILLEGAL_ARGUMENT) Invalid param");
            assertThat(ex.getComponent()).isEqualTo("Core");
            assertThat(ex.getErrorCode()).isEqualTo("ILLEGAL_ARGUMENT");
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            Exception cause = new NumberFormatException("Not a number");
            OpenIllegalArgumentException ex = new OpenIllegalArgumentException("Invalid param", cause);

            assertThat(ex.getMessage()).isEqualTo("[Core] (ILLEGAL_ARGUMENT) Invalid param");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("notNull 创建非空异常")
        void testNotNull() {
            OpenIllegalArgumentException ex = OpenIllegalArgumentException.notNull("userId");

            assertThat(ex.getMessage()).contains("userId");
            assertThat(ex.getMessage()).contains("must not be null");
        }

        @Test
        @DisplayName("notEmpty 创建非空异常")
        void testNotEmpty() {
            OpenIllegalArgumentException ex = OpenIllegalArgumentException.notEmpty("name");

            assertThat(ex.getMessage()).contains("name");
            assertThat(ex.getMessage()).contains("must not be empty");
        }

        @Test
        @DisplayName("notBlank 创建非空白异常")
        void testNotBlank() {
            OpenIllegalArgumentException ex = OpenIllegalArgumentException.notBlank("description");

            assertThat(ex.getMessage()).contains("description");
            assertThat(ex.getMessage()).contains("must not be blank");
        }

        @Test
        @DisplayName("positive 创建正数异常")
        void testPositive() {
            OpenIllegalArgumentException ex = OpenIllegalArgumentException.positive("count", -5);

            assertThat(ex.getMessage()).contains("count");
            assertThat(ex.getMessage()).contains("must be positive");
            assertThat(ex.getMessage()).contains("-5");
        }

        @Test
        @DisplayName("nonNegative 创建非负数异常")
        void testNonNegative() {
            OpenIllegalArgumentException ex = OpenIllegalArgumentException.nonNegative("index", -1);

            assertThat(ex.getMessage()).contains("index");
            assertThat(ex.getMessage()).contains("must not be negative");
            assertThat(ex.getMessage()).contains("-1");
        }

        @Test
        @DisplayName("outOfRange 创建超出范围异常")
        void testOutOfRange() {
            OpenIllegalArgumentException ex = OpenIllegalArgumentException.outOfRange("age", 150, 0, 120);

            assertThat(ex.getMessage()).contains("age");
            assertThat(ex.getMessage()).contains("must be between");
            assertThat(ex.getMessage()).contains("0");
            assertThat(ex.getMessage()).contains("120");
            assertThat(ex.getMessage()).contains("150");
        }

        @Test
        @DisplayName("indexOutOfBounds 创建索引越界异常")
        void testIndexOutOfBounds() {
            OpenIllegalArgumentException ex = OpenIllegalArgumentException.indexOutOfBounds(10, 5);

            assertThat(ex.getMessage()).contains("Index: 10");
            assertThat(ex.getMessage()).contains("Size: 5");
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是 OpenException 的子类")
        void testExtendsOpenException() {
            OpenIllegalArgumentException ex = new OpenIllegalArgumentException("Test");
            assertThat(ex).isInstanceOf(OpenException.class);
        }
    }
}
