package cloud.opencode.base.reflect;

import org.junit.jupiter.api.*;

import java.lang.reflect.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ReflectUtilTest Tests
 * ReflectUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("ReflectUtil 测试")
class ReflectUtilTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = ReflectUtil.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("setAccessible方法测试")
    class SetAccessibleTests {

        @Test
        @DisplayName("设置字段可访问性")
        void testSetAccessibleField() throws Exception {
            Field field = TestClass.class.getDeclaredField("privateField");
            TestClass target = new TestClass();
            ReflectUtil.setAccessible(field, target);
            assertThat(field.canAccess(target)).isTrue();
        }

        @Test
        @DisplayName("设置方法可访问性")
        void testSetAccessibleMethod() throws Exception {
            Method method = TestClass.class.getDeclaredMethod("privateMethod");
            TestClass target = new TestClass();
            ReflectUtil.setAccessible(method, target);
            assertThat(method.canAccess(target)).isTrue();
        }

        @Test
        @DisplayName("null参数返回null")
        void testSetAccessibleNull() {
            assertThat(ReflectUtil.setAccessible((Field) null)).isNull();
        }

        @Test
        @DisplayName("带目标对象设置可访问性")
        void testSetAccessibleWithTarget() throws Exception {
            Method method = TestClass.class.getDeclaredMethod("privateMethod");
            TestClass target = new TestClass();
            ReflectUtil.setAccessible(method, target);
            assertThat(method.canAccess(target)).isTrue();
        }
    }

    @Nested
    @DisplayName("isAccessible方法测试")
    class IsAccessibleTests {

        @Test
        @DisplayName("检查可访问性")
        void testIsAccessible() throws Exception {
            Method publicMethod = TestClass.class.getDeclaredMethod("publicMethod");
            Method privateMethod = TestClass.class.getDeclaredMethod("privateMethod");
            TestClass target = new TestClass();

            assertThat(ReflectUtil.isAccessible(publicMethod, target)).isTrue();
            assertThat(ReflectUtil.isAccessible(privateMethod, target)).isFalse();
        }
    }

    @Nested
    @DisplayName("Member操作测试")
    class MemberOperationsTests {

        @Test
        @DisplayName("获取声明类")
        void testGetDeclaringClass() throws Exception {
            Field field = TestClass.class.getDeclaredField("privateField");
            assertThat(ReflectUtil.getDeclaringClass(field)).isEqualTo(TestClass.class);
        }

        @Test
        @DisplayName("获取成员名称")
        void testGetName() throws Exception {
            Field field = TestClass.class.getDeclaredField("privateField");
            assertThat(ReflectUtil.getName(field)).isEqualTo("privateField");
        }

        @Test
        @DisplayName("获取成员修饰符")
        void testGetModifiers() throws Exception {
            Field field = TestClass.class.getDeclaredField("privateField");
            assertThat(Modifier.isPrivate(ReflectUtil.getModifiers(field))).isTrue();
        }
    }

    @Nested
    @DisplayName("类型解析测试")
    class TypeResolutionTests {

        @Test
        @DisplayName("解析方法返回类型")
        void testResolveReturnType() throws Exception {
            Method method = StringList.class.getDeclaredMethod("get", int.class);
            Type resolved = ReflectUtil.resolveReturnType(method, StringList.class);
            assertThat(resolved).isEqualTo(String.class);
        }

        @Test
        @DisplayName("解析参数类型")
        void testResolveParameterTypes() throws Exception {
            // Note: The declared method has Object parameter due to type erasure
            // The resolved type depends on whether bridge method or actual method is found
            Method method = StringList.class.getDeclaredMethod("add", Object.class);
            Type[] resolved = ReflectUtil.resolveParameterTypes(method, StringList.class);
            assertThat(resolved).hasSize(1);
            // Method parameter is erased to Object in bytecode
            assertThat(resolved[0]).isEqualTo(Object.class);
        }

        @Test
        @DisplayName("解析Class类型")
        void testResolveTypeClass() {
            Type resolved = ReflectUtil.resolveType(String.class, Object.class);
            assertThat(resolved).isEqualTo(String.class);
        }

        @Test
        @DisplayName("解析参数化类型")
        void testResolveParameterizedType() throws Exception {
            Field field = GenericFieldClass.class.getDeclaredField("list");
            Type resolved = ReflectUtil.resolveType(field.getGenericType(), GenericFieldClass.class);
            assertThat(resolved).isInstanceOf(ParameterizedType.class);
        }
    }

    @Nested
    @DisplayName("数组操作测试")
    class ArrayOperationsTests {

        @Test
        @DisplayName("创建一维数组")
        void testNewArray() {
            Object array = ReflectUtil.newArray(String.class, 5);
            assertThat(array).isInstanceOf(String[].class);
            assertThat(((String[]) array).length).isEqualTo(5);
        }

        @Test
        @DisplayName("创建多维数组")
        void testNewMultiDimensionalArray() {
            Object array = ReflectUtil.newArray(String.class, 3, 4);
            assertThat(array).isInstanceOf(String[][].class);
            assertThat(((String[][]) array).length).isEqualTo(3);
            assertThat(((String[][]) array)[0].length).isEqualTo(4);
        }

        @Test
        @DisplayName("获取数组长度")
        void testGetArrayLength() {
            String[] array = {"a", "b", "c"};
            assertThat(ReflectUtil.getArrayLength(array)).isEqualTo(3);
        }

        @Test
        @DisplayName("获取数组元素")
        void testGetArrayElement() {
            String[] array = {"a", "b", "c"};
            assertThat(ReflectUtil.getArrayElement(array, 1)).isEqualTo("b");
        }

        @Test
        @DisplayName("设置数组元素")
        void testSetArrayElement() {
            String[] array = {"a", "b", "c"};
            ReflectUtil.setArrayElement(array, 1, "x");
            assertThat(array[1]).isEqualTo("x");
        }
    }

    @Nested
    @DisplayName("异常包装测试")
    class ExceptionWrappingTests {

        @Test
        @DisplayName("解包InvocationTargetException")
        void testUnwrapInvocationTargetException() {
            Exception cause = new RuntimeException("test");
            InvocationTargetException ite = new InvocationTargetException(cause);

            assertThat(ReflectUtil.unwrapInvocationTargetException(ite)).isSameAs(cause);
        }

        @Test
        @DisplayName("非InvocationTargetException返回原异常")
        void testUnwrapNonInvocationTargetException() {
            RuntimeException ex = new RuntimeException("test");
            assertThat(ReflectUtil.unwrapInvocationTargetException(ex)).isSameAs(ex);
        }

        @Test
        @DisplayName("无cause的InvocationTargetException返回自身")
        void testUnwrapInvocationTargetExceptionNoCause() {
            InvocationTargetException ite = new InvocationTargetException(null);
            assertThat(ReflectUtil.unwrapInvocationTargetException(ite)).isSameAs(ite);
        }

        @Test
        @DisplayName("sneakyThrow抛出异常")
        void testSneakyThrow() {
            Exception ex = new Exception("test");
            assertThatThrownBy(() -> ReflectUtil.sneakyThrow(ex))
                    .isSameAs(ex);
        }
    }

    @Nested
    @DisplayName("签名匹配测试")
    class SignatureMatchingTests {

        @Test
        @DisplayName("参数类型匹配")
        void testIsAssignableArrays() {
            Class<?>[] types = {String.class, Integer.class};
            Object[] args = {"test", 42};

            assertThat(ReflectUtil.isAssignable(types, args)).isTrue();
        }

        @Test
        @DisplayName("参数数量不匹配")
        void testIsAssignableArraysLengthMismatch() {
            Class<?>[] types = {String.class};
            Object[] args = {"test", 42};

            assertThat(ReflectUtil.isAssignable(types, args)).isFalse();
        }

        @Test
        @DisplayName("null参数匹配非原始类型")
        void testIsAssignableNullArg() {
            Class<?>[] types = {String.class};
            Object[] args = {null};

            assertThat(ReflectUtil.isAssignable(types, args)).isTrue();
        }

        @Test
        @DisplayName("null参数不匹配原始类型")
        void testIsAssignableNullArgPrimitive() {
            Class<?>[] types = {int.class};
            Object[] args = {null};

            assertThat(ReflectUtil.isAssignable(types, args)).isFalse();
        }

        @Test
        @DisplayName("类型可赋值检查")
        void testIsAssignableTypes() {
            assertThat(ReflectUtil.isAssignable(Object.class, String.class)).isTrue();
            assertThat(ReflectUtil.isAssignable(String.class, Object.class)).isFalse();
        }

        @Test
        @DisplayName("原始类型拓宽")
        void testIsAssignablePrimitiveWidening() {
            assertThat(ReflectUtil.isAssignable(double.class, int.class)).isTrue();
            assertThat(ReflectUtil.isAssignable(long.class, int.class)).isTrue();
            assertThat(ReflectUtil.isAssignable(float.class, int.class)).isTrue();
            assertThat(ReflectUtil.isAssignable(int.class, short.class)).isTrue();
            assertThat(ReflectUtil.isAssignable(short.class, byte.class)).isTrue();
        }

        @Test
        @DisplayName("包装类型到原始类型")
        void testIsAssignableWrapperToPrimitive() {
            assertThat(ReflectUtil.isAssignable(int.class, Integer.class)).isTrue();
        }

        @Test
        @DisplayName("原始类型到包装类型")
        void testIsAssignablePrimitiveToWrapper() {
            assertThat(ReflectUtil.isAssignable(Integer.class, int.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("类型转换测试")
    class TypeConversionTests {

        @Test
        @DisplayName("原始类型转包装类型")
        void testPrimitiveToWrapper() {
            assertThat(ReflectUtil.primitiveToWrapper(int.class)).isEqualTo(Integer.class);
            assertThat(ReflectUtil.primitiveToWrapper(boolean.class)).isEqualTo(Boolean.class);
            assertThat(ReflectUtil.primitiveToWrapper(char.class)).isEqualTo(Character.class);
            assertThat(ReflectUtil.primitiveToWrapper(byte.class)).isEqualTo(Byte.class);
            assertThat(ReflectUtil.primitiveToWrapper(short.class)).isEqualTo(Short.class);
            assertThat(ReflectUtil.primitiveToWrapper(long.class)).isEqualTo(Long.class);
            assertThat(ReflectUtil.primitiveToWrapper(float.class)).isEqualTo(Float.class);
            assertThat(ReflectUtil.primitiveToWrapper(double.class)).isEqualTo(Double.class);
            assertThat(ReflectUtil.primitiveToWrapper(void.class)).isEqualTo(Void.class);
        }

        @Test
        @DisplayName("非原始类型返回原类型")
        void testPrimitiveToWrapperNonPrimitive() {
            assertThat(ReflectUtil.primitiveToWrapper(String.class)).isEqualTo(String.class);
        }

        @Test
        @DisplayName("包装类型转原始类型")
        void testWrapperToPrimitive() {
            assertThat(ReflectUtil.wrapperToPrimitive(Integer.class)).isEqualTo(int.class);
            assertThat(ReflectUtil.wrapperToPrimitive(Boolean.class)).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("非包装类型返回null")
        void testWrapperToPrimitiveNonWrapper() {
            assertThat(ReflectUtil.wrapperToPrimitive(String.class)).isNull();
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("获取原始类型默认值")
        void testGetDefaultValuePrimitive() {
            assertThat(ReflectUtil.getDefaultValue(boolean.class)).isEqualTo(false);
            assertThat(ReflectUtil.getDefaultValue(byte.class)).isEqualTo((byte) 0);
            assertThat(ReflectUtil.getDefaultValue(char.class)).isEqualTo('\0');
            assertThat(ReflectUtil.getDefaultValue(short.class)).isEqualTo((short) 0);
            assertThat(ReflectUtil.getDefaultValue(int.class)).isEqualTo(0);
            assertThat(ReflectUtil.getDefaultValue(long.class)).isEqualTo(0L);
            assertThat(ReflectUtil.getDefaultValue(float.class)).isEqualTo(0.0f);
            assertThat(ReflectUtil.getDefaultValue(double.class)).isEqualTo(0.0d);
        }

        @Test
        @DisplayName("获取非原始类型默认值")
        void testGetDefaultValueNonPrimitive() {
            assertThat(ReflectUtil.getDefaultValue(String.class)).isNull();
            assertThat(ReflectUtil.getDefaultValue(Integer.class)).isNull();
        }
    }

    // Test helper classes
    @SuppressWarnings("unused")
    static class TestClass {
        private String privateField;

        private void privateMethod() {
        }

        public void publicMethod() {
        }
    }

    static abstract class GenericList<T> {
        public abstract T get(int index);

        public abstract void add(T element);
    }

    static class StringList extends GenericList<String> {
        @Override
        public String get(int index) {
            return null;
        }

        @Override
        public void add(String element) {
        }
    }

    @SuppressWarnings("unused")
    static class GenericFieldClass {
        List<String> list;
    }
}
