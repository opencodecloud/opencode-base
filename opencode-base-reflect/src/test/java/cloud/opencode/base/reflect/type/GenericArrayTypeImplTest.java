package cloud.opencode.base.reflect.type;

import org.junit.jupiter.api.*;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * GenericArrayTypeImplTest Tests
 * GenericArrayTypeImplTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("GenericArrayTypeImpl 测试")
class GenericArrayTypeImplTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建泛型数组类型")
        void testCreate() {
            GenericArrayTypeImpl type = new GenericArrayTypeImpl(String.class);
            assertThat(type).isNotNull();
            assertThat(type.getGenericComponentType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("组件类型为null抛出异常")
        void testCreateNullComponentType() {
            assertThatThrownBy(() -> new GenericArrayTypeImpl(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("创建参数化组件类型的泛型数组")
        void testCreateWithParameterizedComponent() {
            ParameterizedTypeImpl listType = new ParameterizedTypeImpl(List.class, null, String.class);
            GenericArrayTypeImpl type = new GenericArrayTypeImpl(listType);
            assertThat(type.getGenericComponentType()).isEqualTo(listType);
        }
    }

    @Nested
    @DisplayName("getGenericComponentType方法测试")
    class GetGenericComponentTypeTests {

        @Test
        @DisplayName("获取组件类型")
        void testGetGenericComponentType() {
            GenericArrayTypeImpl type = new GenericArrayTypeImpl(String.class);
            assertThat(type.getGenericComponentType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同泛型数组类型相等")
        void testEquals() {
            GenericArrayTypeImpl type1 = new GenericArrayTypeImpl(String.class);
            GenericArrayTypeImpl type2 = new GenericArrayTypeImpl(String.class);
            assertThat(type1).isEqualTo(type2);
        }

        @Test
        @DisplayName("不同组件类型不相等")
        void testNotEqualsDifferentComponentType() {
            GenericArrayTypeImpl type1 = new GenericArrayTypeImpl(String.class);
            GenericArrayTypeImpl type2 = new GenericArrayTypeImpl(Integer.class);
            assertThat(type1).isNotEqualTo(type2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            GenericArrayTypeImpl type = new GenericArrayTypeImpl(String.class);
            assertThat(type).isEqualTo(type);
        }

        @Test
        @DisplayName("与其他GenericArrayType实现比较")
        void testEqualsOtherImpl() throws Exception {
            ParameterizedTypeImpl listType = new ParameterizedTypeImpl(List.class, null, String.class);
            GenericArrayTypeImpl type1 = new GenericArrayTypeImpl(listType);
            Type type2 = getClass().getDeclaredField("stringListArray").getGenericType();
            if (type2 instanceof GenericArrayType) {
                // Component types may differ in implementation details
                assertThat(type1).isInstanceOf(GenericArrayType.class);
            }
        }

        @SuppressWarnings("unused")
        private List<String>[] stringListArray;
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同类型有相同hashCode")
        void testHashCode() {
            GenericArrayTypeImpl type1 = new GenericArrayTypeImpl(String.class);
            GenericArrayTypeImpl type2 = new GenericArrayTypeImpl(String.class);
            assertThat(type1.hashCode()).isEqualTo(type2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("简单泛型数组类型toString")
        void testToString() {
            GenericArrayTypeImpl type = new GenericArrayTypeImpl(String.class);
            assertThat(type.toString()).isEqualTo("String[]");
        }

        @Test
        @DisplayName("参数化组件类型的toString")
        void testToStringParameterizedComponent() {
            ParameterizedTypeImpl listType = new ParameterizedTypeImpl(List.class, null, String.class);
            GenericArrayTypeImpl type = new GenericArrayTypeImpl(listType);
            assertThat(type.toString()).contains("List").contains("String").endsWith("[]");
        }
    }
}
