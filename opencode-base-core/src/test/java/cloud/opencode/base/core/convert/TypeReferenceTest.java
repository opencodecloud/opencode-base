package cloud.opencode.base.core.convert;

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
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("TypeReference 测试")
class TypeReferenceTest {

    @Nested
    @DisplayName("基本类型测试")
    class BasicTypeTests {

        @Test
        @DisplayName("简单类型")
        void testSimpleType() {
            TypeReference<String> ref = new TypeReference<String>() {};
            assertThat(ref.getType()).isEqualTo(String.class);
            assertThat(ref.getRawType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("Integer 类型")
        void testIntegerType() {
            TypeReference<Integer> ref = new TypeReference<Integer>() {};
            assertThat(ref.getType()).isEqualTo(Integer.class);
            assertThat(ref.getRawType()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("Long 类型")
        void testLongType() {
            TypeReference<Long> ref = new TypeReference<Long>() {};
            assertThat(ref.getType()).isEqualTo(Long.class);
            assertThat(ref.getRawType()).isEqualTo(Long.class);
        }
    }

    @Nested
    @DisplayName("泛型类型测试")
    class GenericTypeTests {

        @Test
        @DisplayName("List<String> 类型")
        void testListStringType() {
            TypeReference<List<String>> ref = new TypeReference<List<String>>() {};

            Type type = ref.getType();
            assertThat(type).isInstanceOf(ParameterizedType.class);

            ParameterizedType pt = (ParameterizedType) type;
            assertThat(pt.getRawType()).isEqualTo(List.class);
            assertThat(pt.getActualTypeArguments()).containsExactly(String.class);

            assertThat(ref.getRawType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("List<Integer> 类型")
        void testListIntegerType() {
            TypeReference<List<Integer>> ref = new TypeReference<List<Integer>>() {};

            Type type = ref.getType();
            assertThat(type).isInstanceOf(ParameterizedType.class);

            ParameterizedType pt = (ParameterizedType) type;
            assertThat(pt.getActualTypeArguments()).containsExactly(Integer.class);

            assertThat(ref.getRawType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Map<String, Integer> 类型")
        void testMapType() {
            TypeReference<Map<String, Integer>> ref = new TypeReference<Map<String, Integer>>() {};

            Type type = ref.getType();
            assertThat(type).isInstanceOf(ParameterizedType.class);

            ParameterizedType pt = (ParameterizedType) type;
            assertThat(pt.getRawType()).isEqualTo(Map.class);
            assertThat(pt.getActualTypeArguments()).containsExactly(String.class, Integer.class);

            assertThat(ref.getRawType()).isEqualTo(Map.class);
        }

        @Test
        @DisplayName("嵌套泛型 List<List<String>>")
        void testNestedGenericType() {
            TypeReference<List<List<String>>> ref = new TypeReference<List<List<String>>>() {};

            Type type = ref.getType();
            assertThat(type).isInstanceOf(ParameterizedType.class);

            ParameterizedType pt = (ParameterizedType) type;
            assertThat(pt.getRawType()).isEqualTo(List.class);

            Type[] args = pt.getActualTypeArguments();
            assertThat(args).hasSize(1);
            assertThat(args[0]).isInstanceOf(ParameterizedType.class);

            ParameterizedType innerPt = (ParameterizedType) args[0];
            assertThat(innerPt.getRawType()).isEqualTo(List.class);
            assertThat(innerPt.getActualTypeArguments()).containsExactly(String.class);
        }
    }

    @Nested
    @DisplayName("toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("简单类型 toString")
        void testSimpleTypeToString() {
            TypeReference<String> ref = new TypeReference<String>() {};
            assertThat(ref.toString()).isEqualTo("java.lang.String");
        }

        @Test
        @DisplayName("泛型类型 toString")
        void testGenericTypeToString() {
            TypeReference<List<String>> ref = new TypeReference<List<String>>() {};
            assertThat(ref.toString()).contains("java.util.List");
            assertThat(ref.toString()).contains("java.lang.String");
        }

        @Test
        @DisplayName("Map 类型 toString")
        void testMapTypeToString() {
            TypeReference<Map<String, Integer>> ref = new TypeReference<Map<String, Integer>>() {};
            String str = ref.toString();
            assertThat(str).contains("java.util.Map");
            assertThat(str).contains("java.lang.String");
            assertThat(str).contains("java.lang.Integer");
        }
    }

    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("非参数化类型抛异常")
        void testNonParameterizedTypeThrowsException() {
            // 直接实例化 TypeReference 而不是创建匿名子类会导致错误
            // 但由于 TypeReference 是抽象类，无法直接实例化
            // 这个测试验证当子类不是参数化的时候会抛出异常
            class NonParameterizedTypeReference extends TypeReference {
                // 这是一个非参数化的子类
            }

            assertThatThrownBy(() -> new NonParameterizedTypeReference())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must be parameterized");
        }
    }

    @Nested
    @DisplayName("与 Convert 集成测试")
    class IntegrationWithConvertTests {

        @Test
        @DisplayName("使用 TypeReference 进行转换")
        void testConversionWithTypeReference() {
            TypeReference<Integer> intRef = new TypeReference<Integer>() {};
            Integer result = Convert.convert("123", intRef);
            assertThat(result).isEqualTo(123);
        }

        @Test
        @DisplayName("使用 TypeReference 转换 null")
        void testConversionWithTypeReferenceNull() {
            TypeReference<String> strRef = new TypeReference<String>() {};
            String result = Convert.convert(null, strRef);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("数组类型测试")
    class ArrayTypeTests {

        @Test
        @DisplayName("String[] 类型")
        void testStringArrayType() {
            TypeReference<String[]> ref = new TypeReference<String[]>() {};
            assertThat(ref.getType()).isEqualTo(String[].class);
            assertThat(ref.getRawType()).isEqualTo(String[].class);
        }

        @Test
        @DisplayName("int[] 类型")
        void testIntArrayType() {
            TypeReference<int[]> ref = new TypeReference<int[]>() {};
            assertThat(ref.getType()).isEqualTo(int[].class);
            assertThat(ref.getRawType()).isEqualTo(int[].class);
        }
    }
}
