package cloud.opencode.base.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * TypeReference 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("TypeReference 测试")
class TypeReferenceTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建简单类型引用")
        void testSimpleTypeReference() {
            TypeReference<String> ref = new TypeReference<String>() {};

            assertThat(ref.getType()).isEqualTo(String.class);
            assertThat(ref.getRawType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("创建泛型List类型引用")
        void testGenericListTypeReference() {
            TypeReference<List<String>> ref = new TypeReference<List<String>>() {};

            assertThat(ref.getType()).isInstanceOf(ParameterizedType.class);
            assertThat(ref.getRawType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("创建泛型Map类型引用")
        void testGenericMapTypeReference() {
            TypeReference<Map<String, Integer>> ref = new TypeReference<Map<String, Integer>>() {};

            assertThat(ref.getType()).isInstanceOf(ParameterizedType.class);
            assertThat(ref.getRawType()).isEqualTo(Map.class);
        }

        @Test
        @DisplayName("创建嵌套泛型类型引用")
        void testNestedGenericTypeReference() {
            TypeReference<Map<String, List<Integer>>> ref = new TypeReference<Map<String, List<Integer>>>() {};

            assertThat(ref.getType()).isInstanceOf(ParameterizedType.class);
            assertThat(ref.getRawType()).isEqualTo(Map.class);
        }
    }

    @Nested
    @DisplayName("of(Class)方法测试")
    class OfClassTests {

        @Test
        @DisplayName("从Class创建TypeReference")
        void testOfClass() {
            TypeReference<String> ref = TypeReference.of(String.class);

            assertThat(ref.getType()).isEqualTo(String.class);
            assertThat(ref.getRawType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("null Class抛出异常")
        void testOfNullClass() {
            assertThatThrownBy(() -> TypeReference.of((Class<?>) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("of(Type)方法测试")
    class OfTypeTests {

        @Test
        @DisplayName("从Type创建TypeReference")
        void testOfType() {
            Type type = String.class;
            TypeReference<String> ref = TypeReference.of(type);

            assertThat(ref.getType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("从ParameterizedType创建TypeReference")
        void testOfParameterizedType() {
            TypeReference<List<String>> original = new TypeReference<List<String>>() {};
            Type type = original.getType();

            TypeReference<?> ref = TypeReference.of(type);

            assertThat(ref.getType()).isEqualTo(type);
            assertThat(ref.getRawType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("null Type抛出异常")
        void testOfNullType() {
            assertThatThrownBy(() -> TypeReference.of((Type) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("isParameterized方法测试")
    class IsParameterizedTests {

        @Test
        @DisplayName("简单类型返回false")
        void testSimpleTypeNotParameterized() {
            TypeReference<String> ref = new TypeReference<String>() {};

            assertThat(ref.isParameterized()).isFalse();
        }

        @Test
        @DisplayName("泛型类型返回true")
        void testGenericTypeIsParameterized() {
            TypeReference<List<String>> ref = new TypeReference<List<String>>() {};

            assertThat(ref.isParameterized()).isTrue();
        }
    }

    @Nested
    @DisplayName("getTypeArguments方法测试")
    class GetTypeArgumentsTests {

        @Test
        @DisplayName("简单类型返回空数组")
        void testSimpleTypeNoArguments() {
            TypeReference<String> ref = new TypeReference<String>() {};

            assertThat(ref.getTypeArguments()).isEmpty();
        }

        @Test
        @DisplayName("泛型List返回一个类型参数")
        void testListTypeArguments() {
            TypeReference<List<String>> ref = new TypeReference<List<String>>() {};

            Type[] args = ref.getTypeArguments();
            assertThat(args).hasSize(1);
            assertThat(args[0]).isEqualTo(String.class);
        }

        @Test
        @DisplayName("泛型Map返回两个类型参数")
        void testMapTypeArguments() {
            TypeReference<Map<String, Integer>> ref = new TypeReference<Map<String, Integer>>() {};

            Type[] args = ref.getTypeArguments();
            assertThat(args).hasSize(2);
            assertThat(args[0]).isEqualTo(String.class);
            assertThat(args[1]).isEqualTo(Integer.class);
        }
    }

    @Nested
    @DisplayName("equals和hashCode方法测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同类型相等")
        void testSameTypeEquals() {
            TypeReference<String> ref1 = new TypeReference<String>() {};
            TypeReference<String> ref2 = new TypeReference<String>() {};

            assertThat(ref1).isEqualTo(ref2);
            assertThat(ref1.hashCode()).isEqualTo(ref2.hashCode());
        }

        @Test
        @DisplayName("不同类型不相等")
        void testDifferentTypesNotEqual() {
            TypeReference<String> ref1 = new TypeReference<String>() {};
            TypeReference<Integer> ref2 = new TypeReference<Integer>() {};

            assertThat(ref1).isNotEqualTo(ref2);
        }

        @Test
        @DisplayName("相同泛型类型相等")
        void testSameGenericTypeEquals() {
            TypeReference<List<String>> ref1 = new TypeReference<List<String>>() {};
            TypeReference<List<String>> ref2 = new TypeReference<List<String>>() {};

            assertThat(ref1).isEqualTo(ref2);
            assertThat(ref1.hashCode()).isEqualTo(ref2.hashCode());
        }

        @Test
        @DisplayName("不同泛型参数不相等")
        void testDifferentGenericParamsNotEqual() {
            TypeReference<List<String>> ref1 = new TypeReference<List<String>>() {};
            TypeReference<List<Integer>> ref2 = new TypeReference<List<Integer>>() {};

            assertThat(ref1).isNotEqualTo(ref2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            TypeReference<String> ref = new TypeReference<String>() {};

            assertThat(ref).isEqualTo(ref);
        }

        @Test
        @DisplayName("与null不相等")
        void testNotEqualsNull() {
            TypeReference<String> ref = new TypeReference<String>() {};

            assertThat(ref).isNotEqualTo(null);
        }

        @Test
        @DisplayName("与其他类型不相等")
        void testNotEqualsOtherType() {
            TypeReference<String> ref = new TypeReference<String>() {};

            assertThat(ref).isNotEqualTo("String");
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("简单类型toString")
        void testSimpleTypeToString() {
            TypeReference<String> ref = new TypeReference<String>() {};

            assertThat(ref.toString()).contains("TypeReference");
            assertThat(ref.toString()).contains("String");
        }

        @Test
        @DisplayName("泛型类型toString")
        void testGenericTypeToString() {
            TypeReference<List<String>> ref = new TypeReference<List<String>>() {};

            assertThat(ref.toString()).contains("TypeReference");
            assertThat(ref.toString()).contains("List");
        }
    }

    @Nested
    @DisplayName("SimpleTypeReference测试")
    class SimpleTypeReferenceTests {

        @Test
        @DisplayName("of(Class)创建的引用工作正常")
        void testSimpleTypeReferenceFromClass() {
            TypeReference<Integer> ref = TypeReference.of(Integer.class);

            assertThat(ref.getType()).isEqualTo(Integer.class);
            assertThat(ref.getRawType()).isEqualTo(Integer.class);
            assertThat(ref.isParameterized()).isFalse();
            assertThat(ref.getTypeArguments()).isEmpty();
        }
    }
}
