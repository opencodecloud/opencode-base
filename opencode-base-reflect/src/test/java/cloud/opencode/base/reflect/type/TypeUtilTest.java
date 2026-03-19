package cloud.opencode.base.reflect.type;

import org.junit.jupiter.api.*;

import java.lang.reflect.*;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * TypeUtilTest Tests
 * TypeUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("TypeUtil 测试")
class TypeUtilTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = TypeUtil.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getRawType方法测试")
    class GetRawTypeTests {

        @Test
        @DisplayName("从Class获取原始类型")
        void testGetRawTypeFromClass() {
            assertThat(TypeUtil.getRawType(String.class)).isEqualTo(String.class);
        }

        @Test
        @DisplayName("从ParameterizedType获取原始类型")
        void testGetRawTypeFromParameterizedType() throws Exception {
            Type listType = getClass().getDeclaredField("stringList").getGenericType();
            assertThat(TypeUtil.getRawType(listType)).isEqualTo(List.class);
        }

        @Test
        @DisplayName("从GenericArrayType获取原始类型")
        void testGetRawTypeFromGenericArrayType() throws Exception {
            Type arrayType = getClass().getDeclaredField("stringListArray").getGenericType();
            Class<?> rawType = TypeUtil.getRawType(arrayType);
            assertThat(rawType.isArray()).isTrue();
        }

        @Test
        @DisplayName("从TypeVariable获取Object")
        void testGetRawTypeFromTypeVariable() throws Exception {
            TypeVariable<?>[] typeParams = GenericClass.class.getTypeParameters();
            assertThat(TypeUtil.getRawType(typeParams[0])).isEqualTo(Object.class);
        }

        @Test
        @DisplayName("从WildcardType获取上界类型")
        void testGetRawTypeFromWildcardType() throws Exception {
            Type wildcardType = getClass().getDeclaredField("wildcardList").getGenericType();
            if (wildcardType instanceof ParameterizedType pt) {
                Type arg = pt.getActualTypeArguments()[0];
                if (arg instanceof WildcardType) {
                    assertThat(TypeUtil.getRawType(arg)).isEqualTo(Number.class);
                }
            }
        }

        @SuppressWarnings("unused")
        private List<String> stringList;
        @SuppressWarnings("unused")
        private List<String>[] stringListArray;
        @SuppressWarnings("unused")
        private List<? extends Number> wildcardList;
    }

    @Nested
    @DisplayName("isPrimitive方法测试")
    class IsPrimitiveTests {

        @Test
        @DisplayName("检查原始类型")
        void testIsPrimitive() {
            assertThat(TypeUtil.isPrimitive(int.class)).isTrue();
            assertThat(TypeUtil.isPrimitive(Integer.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isWrapper方法测试")
    class IsWrapperTests {

        @Test
        @DisplayName("检查包装类型")
        void testIsWrapper() {
            assertThat(TypeUtil.isWrapper(Integer.class)).isTrue();
            assertThat(TypeUtil.isWrapper(int.class)).isFalse();
            assertThat(TypeUtil.isWrapper(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("wrap方法测试")
    class WrapTests {

        @Test
        @DisplayName("原始类型转包装类型")
        void testWrap() {
            assertThat(TypeUtil.wrap(int.class)).isEqualTo(Integer.class);
            assertThat(TypeUtil.wrap(boolean.class)).isEqualTo(Boolean.class);
            assertThat(TypeUtil.wrap(char.class)).isEqualTo(Character.class);
            assertThat(TypeUtil.wrap(byte.class)).isEqualTo(Byte.class);
            assertThat(TypeUtil.wrap(short.class)).isEqualTo(Short.class);
            assertThat(TypeUtil.wrap(long.class)).isEqualTo(Long.class);
            assertThat(TypeUtil.wrap(float.class)).isEqualTo(Float.class);
            assertThat(TypeUtil.wrap(double.class)).isEqualTo(Double.class);
            assertThat(TypeUtil.wrap(void.class)).isEqualTo(Void.class);
        }

        @Test
        @DisplayName("非原始类型返回原类型")
        void testWrapNonPrimitive() {
            assertThat(TypeUtil.wrap(String.class)).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("unwrap方法测试")
    class UnwrapTests {

        @Test
        @DisplayName("包装类型转原始类型")
        void testUnwrap() {
            assertThat(TypeUtil.unwrap(Integer.class)).isEqualTo(int.class);
            assertThat(TypeUtil.unwrap(Boolean.class)).isEqualTo(boolean.class);
            assertThat(TypeUtil.unwrap(Character.class)).isEqualTo(char.class);
        }

        @Test
        @DisplayName("非包装类型返回原类型")
        void testUnwrapNonWrapper() {
            assertThat(TypeUtil.unwrap(String.class)).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("isAssignableFrom方法测试")
    class IsAssignableFromTests {

        @Test
        @DisplayName("相同类型可赋值")
        void testIsAssignableFromSame() {
            assertThat(TypeUtil.isAssignableFrom(String.class, String.class)).isTrue();
        }

        @Test
        @DisplayName("父子类型可赋值")
        void testIsAssignableFromSuperSubtype() {
            assertThat(TypeUtil.isAssignableFrom(Object.class, String.class)).isTrue();
            assertThat(TypeUtil.isAssignableFrom(String.class, Object.class)).isFalse();
        }

        @Test
        @DisplayName("参数化类型可赋值检查")
        void testIsAssignableFromParameterized() throws Exception {
            Type listStringType = getClass().getDeclaredField("stringList").getGenericType();
            Type listObjectType = getClass().getDeclaredField("objectList").getGenericType();

            // List<String> is not assignable to List<Object> due to invariance
            assertThat(TypeUtil.isAssignableFrom(listObjectType, listStringType)).isFalse();
        }

        @SuppressWarnings("unused")
        private List<String> stringList;
        @SuppressWarnings("unused")
        private List<Object> objectList;
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("Class转字符串")
        void testToStringClass() {
            assertThat(TypeUtil.toString(String.class)).isEqualTo("String");
        }

        @Test
        @DisplayName("ParameterizedType转字符串")
        void testToStringParameterized() throws Exception {
            Type type = getClass().getDeclaredField("stringList").getGenericType();
            String result = TypeUtil.toString(type);
            assertThat(result).contains("List").contains("String");
        }

        @Test
        @DisplayName("GenericArrayType转字符串")
        void testToStringGenericArray() throws Exception {
            Type type = getClass().getDeclaredField("stringListArray").getGenericType();
            String result = TypeUtil.toString(type);
            assertThat(result).contains("[]");
        }

        @Test
        @DisplayName("WildcardType转字符串")
        void testToStringWildcard() throws Exception {
            Type type = getClass().getDeclaredField("wildcardExtends").getGenericType();
            if (type instanceof ParameterizedType pt) {
                Type arg = pt.getActualTypeArguments()[0];
                String result = TypeUtil.toString(arg);
                assertThat(result).contains("?").contains("extends");
            }
        }

        @Test
        @DisplayName("WildcardType super转字符串")
        void testToStringWildcardSuper() throws Exception {
            Type type = getClass().getDeclaredField("wildcardSuper").getGenericType();
            if (type instanceof ParameterizedType pt) {
                Type arg = pt.getActualTypeArguments()[0];
                String result = TypeUtil.toString(arg);
                assertThat(result).contains("?").contains("super");
            }
        }

        @Test
        @DisplayName("TypeVariable转字符串")
        void testToStringTypeVariable() {
            TypeVariable<?>[] params = GenericClass.class.getTypeParameters();
            String result = TypeUtil.toString(params[0]);
            assertThat(result).isEqualTo("T");
        }

        @SuppressWarnings("unused")
        private List<String> stringList;
        @SuppressWarnings("unused")
        private List<String>[] stringListArray;
        @SuppressWarnings("unused")
        private List<? extends Number> wildcardExtends;
        @SuppressWarnings("unused")
        private List<? super Integer> wildcardSuper;
    }

    @Nested
    @DisplayName("getTypeParameters方法测试")
    class GetTypeParametersTests {

        @Test
        @DisplayName("获取类的类型参数")
        void testGetTypeParameters() {
            TypeVariable<?>[] params = TypeUtil.getTypeParameters(GenericClass.class);
            assertThat(params).hasSize(1);
            assertThat(params[0].getName()).isEqualTo("T");
        }

        @Test
        @DisplayName("非泛型类返回空数组")
        void testGetTypeParametersNonGeneric() {
            TypeVariable<?>[] params = TypeUtil.getTypeParameters(String.class);
            assertThat(params).isEmpty();
        }
    }

    @Nested
    @DisplayName("getActualTypeArguments方法测试")
    class GetActualTypeArgumentsTests {

        @Test
        @DisplayName("从参数化类型获取实际类型参数")
        void testGetActualTypeArguments() throws Exception {
            Type type = getClass().getDeclaredField("stringList").getGenericType();
            Type[] args = TypeUtil.getActualTypeArguments(type);

            assertThat(args).hasSize(1);
            assertThat(args[0]).isEqualTo(String.class);
        }

        @Test
        @DisplayName("非参数化类型返回空数组")
        void testGetActualTypeArgumentsNonParameterized() {
            Type[] args = TypeUtil.getActualTypeArguments(String.class);
            assertThat(args).isEmpty();
        }

        @SuppressWarnings("unused")
        private List<String> stringList;
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同Class相等")
        void testEqualsClass() {
            assertThat(TypeUtil.equals(String.class, String.class)).isTrue();
            assertThat(TypeUtil.equals(String.class, Integer.class)).isFalse();
        }

        @Test
        @DisplayName("相同ParameterizedType相等")
        void testEqualsParameterized() throws Exception {
            Type type1 = EqualsTestClass.class.getDeclaredField("stringList1").getGenericType();
            Type type2 = EqualsTestClass.class.getDeclaredField("stringList2").getGenericType();

            assertThat(TypeUtil.equals(type1, type2)).isTrue();
        }

        @Test
        @DisplayName("不同参数的ParameterizedType不相等")
        void testNotEqualsParameterized() throws Exception {
            Type type1 = EqualsTestClass.class.getDeclaredField("stringList1").getGenericType();
            Type type2 = EqualsTestClass.class.getDeclaredField("integerList").getGenericType();

            assertThat(TypeUtil.equals(type1, type2)).isFalse();
        }

        @Test
        @DisplayName("null处理")
        void testEqualsNull() {
            assertThat(TypeUtil.equals(String.class, null)).isFalse();
            assertThat(TypeUtil.equals(null, String.class)).isFalse();
            // Both null returns true (null == null)
            assertThat(TypeUtil.equals(null, null)).isTrue();
        }

        @Test
        @DisplayName("同一对象相等")
        void testEqualsSame() {
            assertThat(TypeUtil.equals(String.class, String.class)).isTrue();
        }
    }

    // Test helper classes
    static class GenericClass<T> {
    }

    @SuppressWarnings("unused")
    static class EqualsTestClass {
        List<String> stringList1;
        List<String> stringList2;
        List<Integer> integerList;
    }
}
