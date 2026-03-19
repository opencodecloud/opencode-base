package cloud.opencode.base.reflect.type;

import org.junit.jupiter.api.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ParameterizedTypeImplTest Tests
 * ParameterizedTypeImplTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("ParameterizedTypeImpl 测试")
class ParameterizedTypeImplTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建参数化类型")
        void testCreate() {
            ParameterizedTypeImpl type = new ParameterizedTypeImpl(List.class, null, String.class);
            assertThat(type).isNotNull();
            assertThat(type.getRawType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("rawType为null抛出异常")
        void testCreateNullRawType() {
            assertThatThrownBy(() -> new ParameterizedTypeImpl(null, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("创建带所有者类型的参数化类型")
        void testCreateWithOwnerType() {
            ParameterizedTypeImpl type = new ParameterizedTypeImpl(Map.Entry.class, Map.class, String.class, Integer.class);
            assertThat(type.getOwnerType()).isEqualTo(Map.class);
        }

        @Test
        @DisplayName("创建无类型参数的参数化类型")
        void testCreateWithoutTypeArgs() {
            ParameterizedTypeImpl type = new ParameterizedTypeImpl(List.class, null);
            assertThat(type.getActualTypeArguments()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getActualTypeArguments方法测试")
    class GetActualTypeArgumentsTests {

        @Test
        @DisplayName("获取实际类型参数")
        void testGetActualTypeArguments() {
            ParameterizedTypeImpl type = new ParameterizedTypeImpl(List.class, null, String.class);
            Type[] args = type.getActualTypeArguments();
            assertThat(args).containsExactly(String.class);
        }

        @Test
        @DisplayName("返回的数组是副本")
        void testGetActualTypeArgumentsReturnsClone() {
            ParameterizedTypeImpl type = new ParameterizedTypeImpl(List.class, null, String.class);
            Type[] args1 = type.getActualTypeArguments();
            Type[] args2 = type.getActualTypeArguments();
            assertThat(args1).isNotSameAs(args2);
        }
    }

    @Nested
    @DisplayName("getRawType方法测试")
    class GetRawTypeTests {

        @Test
        @DisplayName("获取原始类型")
        void testGetRawType() {
            ParameterizedTypeImpl type = new ParameterizedTypeImpl(List.class, null, String.class);
            assertThat(type.getRawType()).isEqualTo(List.class);
        }
    }

    @Nested
    @DisplayName("getOwnerType方法测试")
    class GetOwnerTypeTests {

        @Test
        @DisplayName("获取所有者类型")
        void testGetOwnerType() {
            ParameterizedTypeImpl type = new ParameterizedTypeImpl(Map.Entry.class, Map.class, String.class, Integer.class);
            assertThat(type.getOwnerType()).isEqualTo(Map.class);
        }

        @Test
        @DisplayName("无所有者类型返回null")
        void testGetOwnerTypeNull() {
            ParameterizedTypeImpl type = new ParameterizedTypeImpl(List.class, null, String.class);
            assertThat(type.getOwnerType()).isNull();
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同参数化类型相等")
        void testEquals() {
            ParameterizedTypeImpl type1 = new ParameterizedTypeImpl(List.class, null, String.class);
            ParameterizedTypeImpl type2 = new ParameterizedTypeImpl(List.class, null, String.class);
            assertThat(type1).isEqualTo(type2);
        }

        @Test
        @DisplayName("不同rawType不相等")
        void testNotEqualsDifferentRawType() {
            ParameterizedTypeImpl type1 = new ParameterizedTypeImpl(List.class, null, String.class);
            ParameterizedTypeImpl type2 = new ParameterizedTypeImpl(java.util.Set.class, null, String.class);
            assertThat(type1).isNotEqualTo(type2);
        }

        @Test
        @DisplayName("不同类型参数不相等")
        void testNotEqualsDifferentTypeArgs() {
            ParameterizedTypeImpl type1 = new ParameterizedTypeImpl(List.class, null, String.class);
            ParameterizedTypeImpl type2 = new ParameterizedTypeImpl(List.class, null, Integer.class);
            assertThat(type1).isNotEqualTo(type2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            ParameterizedTypeImpl type = new ParameterizedTypeImpl(List.class, null, String.class);
            assertThat(type).isEqualTo(type);
        }

        @Test
        @DisplayName("与其他ParameterizedType实现比较")
        void testEqualsOtherImpl() throws Exception {
            ParameterizedTypeImpl type1 = new ParameterizedTypeImpl(List.class, null, String.class);
            Type type2 = getClass().getDeclaredField("stringList").getGenericType();
            if (type2 instanceof ParameterizedType) {
                assertThat(type1).isEqualTo(type2);
            }
        }

        @SuppressWarnings("unused")
        private List<String> stringList;
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同类型有相同hashCode")
        void testHashCode() {
            ParameterizedTypeImpl type1 = new ParameterizedTypeImpl(List.class, null, String.class);
            ParameterizedTypeImpl type2 = new ParameterizedTypeImpl(List.class, null, String.class);
            assertThat(type1.hashCode()).isEqualTo(type2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("简单参数化类型toString")
        void testToString() {
            ParameterizedTypeImpl type = new ParameterizedTypeImpl(List.class, null, String.class);
            assertThat(type.toString()).contains("List").contains("String");
        }

        @Test
        @DisplayName("带所有者类型的toString")
        void testToStringWithOwner() {
            ParameterizedTypeImpl type = new ParameterizedTypeImpl(Map.Entry.class, Map.class, String.class, Integer.class);
            assertThat(type.toString()).contains("Map").contains("Entry");
        }

        @Test
        @DisplayName("无类型参数的toString")
        void testToStringNoArgs() {
            ParameterizedTypeImpl type = new ParameterizedTypeImpl(List.class, null);
            assertThat(type.toString()).isEqualTo("List");
        }
    }
}
