package cloud.opencode.base.functional.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenMatchException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("OpenMatchException 测试")
class OpenMatchExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用消息构造")
        void testMessageConstructor() {
            OpenMatchException ex = new OpenMatchException("test message");

            assertThat(ex.getMessage()).contains("test message");
            assertThat(ex.unmatchedValue()).isNull();
        }

        @Test
        @DisplayName("使用消息和未匹配值构造")
        void testMessageAndValueConstructor() {
            Object value = "test value";
            OpenMatchException ex = new OpenMatchException("no match", value);

            assertThat(ex.getMessage()).contains("no match");
            assertThat(ex.unmatchedValue()).isEqualTo(value);
        }

        @Test
        @DisplayName("使用错误码、消息和未匹配值构造")
        void testErrorCodeMessageAndValueConstructor() {
            Object value = 42;
            OpenMatchException ex = new OpenMatchException("MATCH_001", "no match", value);

            assertThat(ex.getMessage()).contains("no match");
            assertThat(ex.unmatchedValue()).isEqualTo(value);
        }

        @Test
        @DisplayName("null未匹配值")
        void testNullUnmatchedValue() {
            OpenMatchException ex = new OpenMatchException("no match", null);

            assertThat(ex.unmatchedValue()).isNull();
        }
    }

    @Nested
    @DisplayName("unmatchedValue() 测试")
    class UnmatchedValueTests {

        @Test
        @DisplayName("返回未匹配的值")
        void testUnmatchedValue() {
            Integer value = 42;
            OpenMatchException ex = new OpenMatchException("no match", value);

            assertThat(ex.unmatchedValue()).isEqualTo(42);
        }

        @Test
        @DisplayName("无值时返回null")
        void testNullWhenNoValue() {
            OpenMatchException ex = new OpenMatchException("error");

            assertThat(ex.unmatchedValue()).isNull();
        }
    }

    @Nested
    @DisplayName("noMatch() 工厂方法测试")
    class NoMatchTests {

        @Test
        @DisplayName("创建无匹配异常")
        void testNoMatch() {
            OpenMatchException ex = OpenMatchException.noMatch("test");

            assertThat(ex.getMessage()).contains("No pattern matched");
            assertThat(ex.unmatchedValue()).isEqualTo("test");
        }

        @Test
        @DisplayName("包含值类型信息")
        void testContainsTypeInfo() {
            OpenMatchException ex = OpenMatchException.noMatch(123);

            assertThat(ex.getMessage()).contains("Integer");
        }

        @Test
        @DisplayName("处理null值")
        void testNoMatchWithNull() {
            OpenMatchException ex = OpenMatchException.noMatch(null);

            assertThat(ex.getMessage()).contains("null");
            assertThat(ex.unmatchedValue()).isNull();
        }
    }

    @Nested
    @DisplayName("exhaustive() 工厂方法测试")
    class ExhaustiveTests {

        @Test
        @DisplayName("创建非穷尽匹配异常")
        void testExhaustive() {
            OpenMatchException ex = OpenMatchException.exhaustive("Triangle", Shape.class);

            assertThat(ex.getMessage()).contains("Shape");
            assertThat(ex.getMessage()).contains("Triangle");
            assertThat(ex.getMessage()).contains("exhaustive");
        }

        @Test
        @DisplayName("保存未匹配值")
        void testExhaustiveStoresValue() {
            Object value = "Circle";
            OpenMatchException ex = OpenMatchException.exhaustive(value, Shape.class);

            assertThat(ex.unmatchedValue()).isEqualTo(value);
        }
    }

    // 测试用的内部接口
    interface Shape {}

    @Nested
    @DisplayName("guardFailed() 工厂方法测试")
    class GuardFailedTests {

        @Test
        @DisplayName("创建守卫失败异常")
        void testGuardFailed() {
            OpenMatchException ex = OpenMatchException.guardFailed(-5);

            assertThat(ex.getMessage()).contains("Guard condition failed");
            assertThat(ex.getMessage()).contains("-5");
            assertThat(ex.unmatchedValue()).isEqualTo(-5);
        }
    }

    @Nested
    @DisplayName("typeMismatch() 工厂方法测试")
    class TypeMismatchTests {

        @Test
        @DisplayName("创建类型不匹配异常")
        void testTypeMismatch() {
            OpenMatchException ex = OpenMatchException.typeMismatch("hello", Integer.class);

            assertThat(ex.getMessage()).contains("String");
            assertThat(ex.getMessage()).contains("Integer");
            assertThat(ex.unmatchedValue()).isEqualTo("hello");
        }

        @Test
        @DisplayName("处理null值的类型不匹配")
        void testTypeMismatchWithNull() {
            OpenMatchException ex = OpenMatchException.typeMismatch(null, String.class);

            assertThat(ex.getMessage()).contains("null");
            assertThat(ex.getMessage()).contains("String");
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承自OpenFunctionalException")
        void testExtendsOpenFunctionalException() {
            OpenMatchException ex = new OpenMatchException("test");

            assertThat(ex).isInstanceOf(OpenFunctionalException.class);
        }

        @Test
        @DisplayName("继承自RuntimeException")
        void testExtendsRuntimeException() {
            OpenMatchException ex = new OpenMatchException("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以作为异常抛出")
        void testCanBeThrown() {
            assertThatThrownBy(() -> {
                throw OpenMatchException.noMatch("value");
            }).isInstanceOf(OpenMatchException.class);
        }
    }
}
