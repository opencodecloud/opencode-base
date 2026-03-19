package cloud.opencode.base.reflect.type;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TypeTokenTest Tests
 * TypeTokenTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("TypeToken 测试")
class TypeTokenTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("通过匿名子类创建TypeToken")
        void testCreateViaAnonymousSubclass() {
            TypeToken<List<String>> token = new TypeToken<List<String>>() {};

            assertThat(token.getRawType()).isEqualTo(List.class);
            assertThat(token.isParameterized()).isTrue();
        }

        @Test
        @DisplayName("无类型参数创建抛出异常")
        void testCreateWithoutTypeParameter() {
            assertThatThrownBy(() -> new TypeToken() {})
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("of工厂方法测试")
    class OfTests {

        @Test
        @DisplayName("从Class创建TypeToken")
        void testOfClass() {
            TypeToken<String> token = TypeToken.of(String.class);

            assertThat(token.getRawType()).isEqualTo(String.class);
            assertThat(token.getType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("从Type创建TypeToken")
        void testOfType() throws Exception {
            Type listType = getClass().getDeclaredField("stringList").getGenericType();
            TypeToken<?> token = TypeToken.of(listType);

            assertThat(token.getRawType()).isEqualTo(List.class);
        }

        @SuppressWarnings("unused")
        private List<String> stringList;
    }

    @Nested
    @DisplayName("getType方法测试")
    class GetTypeTests {

        @Test
        @DisplayName("获取底层Type")
        void testGetType() {
            TypeToken<String> token = TypeToken.of(String.class);
            assertThat(token.getType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getRawType方法测试")
    class GetRawTypeTests {

        @Test
        @DisplayName("获取原始类型")
        void testGetRawType() {
            TypeToken<List<String>> token = new TypeToken<List<String>>() {};
            assertThat(token.getRawType()).isEqualTo(List.class);
        }
    }

    @Nested
    @DisplayName("getTypeParameter方法测试")
    class GetTypeParameterTests {

        @Test
        @DisplayName("获取类型参数")
        void testGetTypeParameter() {
            TypeToken<List<String>> token = new TypeToken<List<String>>() {};
            TypeToken<?> elementType = token.getTypeParameter(0);

            assertThat(elementType.getRawType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("获取Map的键值类型")
        void testGetTypeParameterMap() {
            TypeToken<Map<String, Integer>> token = new TypeToken<Map<String, Integer>>() {};

            assertThat(token.getTypeParameter(0).getRawType()).isEqualTo(String.class);
            assertThat(token.getTypeParameter(1).getRawType()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("索引越界抛出异常")
        void testGetTypeParameterOutOfBounds() {
            TypeToken<List<String>> token = new TypeToken<List<String>>() {};

            assertThatThrownBy(() -> token.getTypeParameter(5))
                    .isInstanceOf(OpenReflectException.class);
        }

        @Test
        @DisplayName("非参数化类型抛出异常")
        void testGetTypeParameterNonParameterized() {
            TypeToken<String> token = TypeToken.of(String.class);

            assertThatThrownBy(() -> token.getTypeParameter(0))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("getTypeParameters方法测试")
    class GetTypeParametersTests {

        @Test
        @DisplayName("获取所有类型参数")
        void testGetTypeParameters() {
            TypeToken<Map<String, Integer>> token = new TypeToken<Map<String, Integer>>() {};
            List<TypeToken<?>> params = token.getTypeParameters();

            assertThat(params).hasSize(2);
            assertThat(params.get(0).getRawType()).isEqualTo(String.class);
            assertThat(params.get(1).getRawType()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("非参数化类型返回空列表")
        void testGetTypeParametersNonParameterized() {
            TypeToken<String> token = TypeToken.of(String.class);
            List<TypeToken<?>> params = token.getTypeParameters();

            assertThat(params).isEmpty();
        }
    }

    @Nested
    @DisplayName("getComponentType方法测试")
    class GetComponentTypeTests {

        @Test
        @DisplayName("获取数组组件类型")
        void testGetComponentType() {
            TypeToken<String[]> token = TypeToken.of(String[].class);
            TypeToken<?> componentType = token.getComponentType();

            assertThat(componentType.getRawType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("非数组类型返回null")
        void testGetComponentTypeNonArray() {
            TypeToken<String> token = TypeToken.of(String.class);
            assertThat(token.getComponentType()).isNull();
        }
    }

    @Nested
    @DisplayName("isPrimitive方法测试")
    class IsPrimitiveTests {

        @Test
        @DisplayName("检查原始类型")
        void testIsPrimitive() {
            assertThat(TypeToken.of(int.class).isPrimitive()).isTrue();
            assertThat(TypeToken.of(Integer.class).isPrimitive()).isFalse();
        }
    }

    @Nested
    @DisplayName("isArray方法测试")
    class IsArrayTests {

        @Test
        @DisplayName("检查数组类型")
        void testIsArray() {
            assertThat(TypeToken.of(String[].class).isArray()).isTrue();
            assertThat(TypeToken.of(String.class).isArray()).isFalse();
        }
    }

    @Nested
    @DisplayName("isParameterized方法测试")
    class IsParameterizedTests {

        @Test
        @DisplayName("检查参数化类型")
        void testIsParameterized() {
            TypeToken<List<String>> listToken = new TypeToken<List<String>>() {};
            TypeToken<String> stringToken = TypeToken.of(String.class);

            assertThat(listToken.isParameterized()).isTrue();
            assertThat(stringToken.isParameterized()).isFalse();
        }
    }

    @Nested
    @DisplayName("isWildcard方法测试")
    class IsWildcardTests {

        @Test
        @DisplayName("检查非通配符类型")
        void testIsWildcard() {
            TypeToken<String> token = TypeToken.of(String.class);
            assertThat(token.isWildcard()).isFalse();
        }
    }

    @Nested
    @DisplayName("isTypeVariable方法测试")
    class IsTypeVariableTests {

        @Test
        @DisplayName("检查非类型变量")
        void testIsTypeVariable() {
            TypeToken<String> token = TypeToken.of(String.class);
            assertThat(token.isTypeVariable()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSupertypeOf方法测试")
    class IsSupertypeOfTests {

        @Test
        @DisplayName("检查父类型关系")
        void testIsSupertypeOf() {
            TypeToken<Object> objectToken = TypeToken.of(Object.class);
            TypeToken<String> stringToken = TypeToken.of(String.class);

            assertThat(objectToken.isSupertypeOf(stringToken)).isTrue();
            assertThat(stringToken.isSupertypeOf(objectToken)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSubtypeOf方法测试")
    class IsSubtypeOfTests {

        @Test
        @DisplayName("检查子类型关系")
        void testIsSubtypeOf() {
            TypeToken<Object> objectToken = TypeToken.of(Object.class);
            TypeToken<String> stringToken = TypeToken.of(String.class);

            assertThat(stringToken.isSubtypeOf(objectToken)).isTrue();
            assertThat(objectToken.isSubtypeOf(stringToken)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAssignableFrom方法测试")
    class IsAssignableFromTests {

        @Test
        @DisplayName("检查可赋值关系")
        void testIsAssignableFrom() {
            TypeToken<Object> objectToken = TypeToken.of(Object.class);
            TypeToken<String> stringToken = TypeToken.of(String.class);

            assertThat(objectToken.isAssignableFrom(stringToken)).isTrue();
        }
    }

    @Nested
    @DisplayName("wrap方法测试")
    class WrapTests {

        @Test
        @DisplayName("原始类型转包装类型")
        void testWrap() {
            TypeToken<Integer> intToken = TypeToken.of(int.class);
            TypeToken<Integer> wrapped = intToken.wrap();

            assertThat(wrapped.getRawType()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("非原始类型返回自身")
        void testWrapNonPrimitive() {
            TypeToken<String> stringToken = TypeToken.of(String.class);
            TypeToken<String> result = stringToken.wrap();

            assertThat(result).isSameAs(stringToken);
        }
    }

    @Nested
    @DisplayName("unwrap方法测试")
    class UnwrapTests {

        @Test
        @DisplayName("包装类型转原始类型")
        void testUnwrap() {
            TypeToken<Integer> integerToken = TypeToken.of(Integer.class);
            TypeToken<Integer> unwrapped = integerToken.unwrap();

            assertThat(unwrapped.getRawType()).isEqualTo(int.class);
        }

        @Test
        @DisplayName("非包装类型返回自身")
        void testUnwrapNonWrapper() {
            TypeToken<String> stringToken = TypeToken.of(String.class);
            TypeToken<String> result = stringToken.unwrap();

            assertThat(result).isSameAs(stringToken);
        }
    }

    @Nested
    @DisplayName("resolveFieldType方法测试")
    class ResolveFieldTypeTests {

        @Test
        @DisplayName("解析字段类型")
        void testResolveFieldType() throws Exception {
            TypeToken<TestClass> token = TypeToken.of(TestClass.class);
            Field field = TestClass.class.getDeclaredField("name");

            TypeToken<?> fieldType = token.resolveFieldType(field);
            assertThat(fieldType.getRawType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("resolveReturnType方法测试")
    class ResolveReturnTypeTests {

        @Test
        @DisplayName("解析方法返回类型")
        void testResolveReturnType() throws Exception {
            TypeToken<TestClass> token = TypeToken.of(TestClass.class);
            Method method = TestClass.class.getDeclaredMethod("getName");

            TypeToken<?> returnType = token.resolveReturnType(method);
            assertThat(returnType.getRawType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("resolveParameterTypes方法测试")
    class ResolveParameterTypesTests {

        @Test
        @DisplayName("解析方法参数类型")
        void testResolveParameterTypes() throws Exception {
            TypeToken<TestClass> token = TypeToken.of(TestClass.class);
            Method method = TestClass.class.getDeclaredMethod("setName", String.class);

            List<TypeToken<?>> paramTypes = token.resolveParameterTypes(method);
            assertThat(paramTypes).hasSize(1);
            assertThat(paramTypes.get(0).getRawType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("listOf工厂方法测试")
    class ListOfTests {

        @Test
        @DisplayName("创建List TypeToken")
        void testListOfClass() {
            TypeToken<List<String>> token = TypeToken.listOf(String.class);

            assertThat(token.getRawType()).isEqualTo(List.class);
            assertThat(token.getTypeParameter(0).getRawType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("使用TypeToken创建List TypeToken")
        void testListOfTypeToken() {
            TypeToken<List<String>> token = TypeToken.listOf(TypeToken.of(String.class));

            assertThat(token.getRawType()).isEqualTo(List.class);
        }
    }

    @Nested
    @DisplayName("setOf工厂方法测试")
    class SetOfTests {

        @Test
        @DisplayName("创建Set TypeToken")
        void testSetOfClass() {
            TypeToken<Set<Integer>> token = TypeToken.setOf(Integer.class);

            assertThat(token.getRawType()).isEqualTo(Set.class);
            assertThat(token.getTypeParameter(0).getRawType()).isEqualTo(Integer.class);
        }
    }

    @Nested
    @DisplayName("mapOf工厂方法测试")
    class MapOfTests {

        @Test
        @DisplayName("创建Map TypeToken")
        void testMapOfClasses() {
            TypeToken<Map<String, Integer>> token = TypeToken.mapOf(String.class, Integer.class);

            assertThat(token.getRawType()).isEqualTo(Map.class);
            assertThat(token.getTypeParameter(0).getRawType()).isEqualTo(String.class);
            assertThat(token.getTypeParameter(1).getRawType()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("使用TypeToken创建Map TypeToken")
        void testMapOfTypeTokens() {
            TypeToken<Map<String, Integer>> token = TypeToken.mapOf(
                    TypeToken.of(String.class), TypeToken.of(Integer.class));

            assertThat(token.getRawType()).isEqualTo(Map.class);
        }
    }

    @Nested
    @DisplayName("optionalOf工厂方法测试")
    class OptionalOfTests {

        @Test
        @DisplayName("创建Optional TypeToken")
        void testOptionalOfClass() {
            TypeToken<Optional<String>> token = TypeToken.optionalOf(String.class);

            assertThat(token.getRawType()).isEqualTo(Optional.class);
            assertThat(token.getTypeParameter(0).getRawType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同类型相等")
        void testEquals() {
            TypeToken<String> token1 = TypeToken.of(String.class);
            TypeToken<String> token2 = TypeToken.of(String.class);

            assertThat(token1).isEqualTo(token2);
        }

        @Test
        @DisplayName("不同类型不相等")
        void testNotEquals() {
            TypeToken<String> stringToken = TypeToken.of(String.class);
            TypeToken<Integer> integerToken = TypeToken.of(Integer.class);

            assertThat(stringToken).isNotEqualTo(integerToken);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            TypeToken<String> token = TypeToken.of(String.class);
            assertThat(token).isEqualTo(token);
        }

        @Test
        @DisplayName("与非TypeToken对象不相等")
        void testNotEqualsOtherType() {
            TypeToken<String> token = TypeToken.of(String.class);
            assertThat(token).isNotEqualTo("String");
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同类型有相同hashCode")
        void testHashCode() {
            TypeToken<String> token1 = TypeToken.of(String.class);
            TypeToken<String> token2 = TypeToken.of(String.class);

            assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("简单类型toString")
        void testToStringSimple() {
            TypeToken<String> token = TypeToken.of(String.class);
            assertThat(token.toString()).isEqualTo("String");
        }

        @Test
        @DisplayName("参数化类型toString")
        void testToStringParameterized() {
            TypeToken<List<String>> token = new TypeToken<List<String>>() {};
            assertThat(token.toString()).contains("List").contains("String");
        }
    }

    // Test helper class
    @SuppressWarnings("unused")
    static class TestClass {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
