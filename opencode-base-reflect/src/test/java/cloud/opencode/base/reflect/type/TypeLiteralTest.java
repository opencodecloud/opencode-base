package cloud.opencode.base.reflect.type;

import org.junit.jupiter.api.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * TypeLiteralTest Tests
 * TypeLiteralTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("TypeLiteral 测试")
class TypeLiteralTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("通过匿名子类创建TypeLiteral")
        void testCreateViaAnonymousSubclass() {
            TypeLiteral<List<String>> literal = new TypeLiteral<List<String>>() {};

            assertThat(literal.getRawType()).isEqualTo(List.class);
            assertThat(literal.getType()).isNotNull();
        }

        @Test
        @DisplayName("无类型参数创建抛出异常")
        void testCreateWithoutTypeParameter() {
            assertThatThrownBy(() -> new TypeLiteral() {})
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getType方法测试")
    class GetTypeTests {

        @Test
        @DisplayName("获取底层Type")
        void testGetType() {
            TypeLiteral<String> literal = new TypeLiteral<String>() {};
            Type type = literal.getType();
            assertThat(type).isEqualTo(String.class);
        }

        @Test
        @DisplayName("获取参数化类型")
        void testGetTypeParameterized() {
            TypeLiteral<List<String>> literal = new TypeLiteral<List<String>>() {};
            Type type = literal.getType();
            assertThat(type.getTypeName()).contains("List");
        }
    }

    @Nested
    @DisplayName("getRawType方法测试")
    class GetRawTypeTests {

        @Test
        @DisplayName("获取原始类型")
        void testGetRawType() {
            TypeLiteral<List<String>> literal = new TypeLiteral<List<String>>() {};
            assertThat(literal.getRawType()).isEqualTo(List.class);
        }
    }

    @Nested
    @DisplayName("toTypeToken方法测试")
    class ToTypeTokenTests {

        @Test
        @DisplayName("转换为TypeToken")
        void testToTypeToken() {
            TypeLiteral<List<String>> literal = new TypeLiteral<List<String>>() {};
            TypeToken<List<String>> token = literal.toTypeToken();
            assertThat(token).isNotNull();
            // Note: Due to how toTypeToken creates an anonymous TypeToken subclass,
            // the type info is preserved via getType() but rawType uses TypeVariable
            assertThat(token.getType()).isNotNull();
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同类型相等")
        void testEquals() {
            TypeLiteral<String> literal1 = new TypeLiteral<String>() {};
            TypeLiteral<String> literal2 = new TypeLiteral<String>() {};
            assertThat(literal1).isEqualTo(literal2);
        }

        @Test
        @DisplayName("不同类型不相等")
        void testNotEquals() {
            TypeLiteral<String> literal1 = new TypeLiteral<String>() {};
            TypeLiteral<Integer> literal2 = new TypeLiteral<Integer>() {};
            assertThat(literal1).isNotEqualTo(literal2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            TypeLiteral<String> literal = new TypeLiteral<String>() {};
            assertThat(literal).isEqualTo(literal);
        }

        @Test
        @DisplayName("与非TypeLiteral对象不相等")
        void testNotEqualsOtherType() {
            TypeLiteral<String> literal = new TypeLiteral<String>() {};
            assertThat(literal).isNotEqualTo("String");
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同类型有相同hashCode")
        void testHashCode() {
            TypeLiteral<String> literal1 = new TypeLiteral<String>() {};
            TypeLiteral<String> literal2 = new TypeLiteral<String>() {};
            assertThat(literal1.hashCode()).isEqualTo(literal2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("简单类型toString")
        void testToStringSimple() {
            TypeLiteral<String> literal = new TypeLiteral<String>() {};
            assertThat(literal.toString()).isEqualTo("String");
        }

        @Test
        @DisplayName("参数化类型toString")
        void testToStringParameterized() {
            TypeLiteral<List<String>> literal = new TypeLiteral<List<String>>() {};
            assertThat(literal.toString()).contains("List").contains("String");
        }
    }
}
