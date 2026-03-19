package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenClass 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenClass 测试")
class OpenClassTest {

    // 测试用类和接口
    interface TestInterface<T> {}
    static class TestParent<T> {}
    static class TestChild extends TestParent<String> implements TestInterface<Integer> {}
    static class SimpleClass {}
    record TestRecord(String name, int value) {}

    @Nested
    @DisplayName("类加载测试")
    class ClassLoadingTests {

        @Test
        @DisplayName("getClassLoader")
        void testGetClassLoader() {
            ClassLoader cl = OpenClass.getClassLoader();
            assertThat(cl).isNotNull();
        }

        @Test
        @DisplayName("getClassLoader 指定类")
        void testGetClassLoaderForClass() {
            ClassLoader cl = OpenClass.getClassLoader(String.class);
            assertThat(cl).isNotNull();
        }

        @Test
        @DisplayName("loadClass")
        void testLoadClass() {
            Class<?> clazz = OpenClass.loadClass("java.lang.String");
            assertThat(clazz).isEqualTo(String.class);
        }

        @Test
        @DisplayName("loadClass 不存在抛异常")
        void testLoadClassNotFound() {
            assertThatThrownBy(() -> OpenClass.loadClass("com.nonexistent.Class"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("loadClassSafely")
        void testLoadClassSafely() {
            assertThat(OpenClass.loadClassSafely("java.lang.String")).isEqualTo(String.class);
            assertThat(OpenClass.loadClassSafely("com.nonexistent.Class")).isNull();
        }

        @Test
        @DisplayName("loadClassOptional")
        void testLoadClassOptional() {
            assertThat(OpenClass.loadClassOptional("java.lang.String")).contains(String.class);
            assertThat(OpenClass.loadClassOptional("com.nonexistent.Class")).isEmpty();
        }

        @Test
        @DisplayName("isPresent")
        void testIsPresent() {
            assertThat(OpenClass.isPresent("java.lang.String")).isTrue();
            assertThat(OpenClass.isPresent("com.nonexistent.Class")).isFalse();
        }
    }

    @Nested
    @DisplayName("类型判断测试")
    class TypeCheckTests {

        @Test
        @DisplayName("isPrimitive")
        void testIsPrimitive() {
            assertThat(OpenClass.isPrimitive(int.class)).isTrue();
            assertThat(OpenClass.isPrimitive(boolean.class)).isTrue();
            assertThat(OpenClass.isPrimitive(Integer.class)).isFalse();
            assertThat(OpenClass.isPrimitive(null)).isFalse();
        }

        @Test
        @DisplayName("isPrimitiveWrapper")
        void testIsPrimitiveWrapper() {
            assertThat(OpenClass.isPrimitiveWrapper(Integer.class)).isTrue();
            assertThat(OpenClass.isPrimitiveWrapper(Boolean.class)).isTrue();
            assertThat(OpenClass.isPrimitiveWrapper(int.class)).isFalse();
            assertThat(OpenClass.isPrimitiveWrapper(String.class)).isFalse();
        }

        @Test
        @DisplayName("isPrimitiveOrWrapper")
        void testIsPrimitiveOrWrapper() {
            assertThat(OpenClass.isPrimitiveOrWrapper(int.class)).isTrue();
            assertThat(OpenClass.isPrimitiveOrWrapper(Integer.class)).isTrue();
            assertThat(OpenClass.isPrimitiveOrWrapper(String.class)).isFalse();
        }

        @Test
        @DisplayName("isArray")
        void testIsArray() {
            assertThat(OpenClass.isArray(int[].class)).isTrue();
            assertThat(OpenClass.isArray(String[].class)).isTrue();
            assertThat(OpenClass.isArray(String.class)).isFalse();
            assertThat(OpenClass.isArray(null)).isFalse();
        }

        @Test
        @DisplayName("isCollection")
        void testIsCollection() {
            assertThat(OpenClass.isCollection(List.class)).isTrue();
            assertThat(OpenClass.isCollection(Set.class)).isTrue();
            assertThat(OpenClass.isCollection(Map.class)).isTrue();
            assertThat(OpenClass.isCollection(String.class)).isFalse();
        }

        @Test
        @DisplayName("isInterface")
        void testIsInterface() {
            assertThat(OpenClass.isInterface(List.class)).isTrue();
            assertThat(OpenClass.isInterface(ArrayList.class)).isFalse();
            assertThat(OpenClass.isInterface(null)).isFalse();
        }

        @Test
        @DisplayName("isAbstract")
        void testIsAbstract() {
            assertThat(OpenClass.isAbstract(AbstractList.class)).isTrue();
            assertThat(OpenClass.isAbstract(ArrayList.class)).isFalse();
        }

        @Test
        @DisplayName("isEnum")
        void testIsEnum() {
            assertThat(OpenClass.isEnum(Thread.State.class)).isTrue();
            assertThat(OpenClass.isEnum(String.class)).isFalse();
        }

        @Test
        @DisplayName("isRecord")
        void testIsRecord() {
            assertThat(OpenClass.isRecord(TestRecord.class)).isTrue();
            assertThat(OpenClass.isRecord(String.class)).isFalse();
        }

        @Test
        @DisplayName("isInnerClass")
        void testIsInnerClass() {
            assertThat(OpenClass.isInnerClass(SimpleClass.class)).isFalse();
        }

        @Test
        @DisplayName("isAnonymousClass")
        void testIsAnonymousClass() {
            // Lambda 不是匿名类，需要使用真正的匿名内部类
            Runnable anonymous = new Runnable() { public void run() {} };
            assertThat(OpenClass.isAnonymousClass(anonymous.getClass())).isTrue();
            assertThat(OpenClass.isAnonymousClass(String.class)).isFalse();
        }

        @Test
        @DisplayName("isLambdaClass")
        void testIsLambdaClass() {
            Runnable lambda = () -> {};
            assertThat(OpenClass.isLambdaClass(lambda.getClass())).isTrue();
            assertThat(OpenClass.isLambdaClass(String.class)).isFalse();
        }

        @Test
        @DisplayName("isAssignable")
        void testIsAssignable() {
            assertThat(OpenClass.isAssignable(Object.class, String.class)).isTrue();
            assertThat(OpenClass.isAssignable(String.class, Object.class)).isFalse();
            assertThat(OpenClass.isAssignable(int.class, Integer.class)).isTrue();
            assertThat(OpenClass.isAssignable(Integer.class, int.class)).isTrue();
            assertThat(OpenClass.isAssignable(null, String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("类型转换测试")
    class TypeConversionTests {

        @Test
        @DisplayName("getWrapperClass")
        void testGetWrapperClass() {
            assertThat(OpenClass.getWrapperClass(int.class)).isEqualTo(Integer.class);
            assertThat(OpenClass.getWrapperClass(boolean.class)).isEqualTo(Boolean.class);
            assertThat(OpenClass.getWrapperClass(void.class)).isEqualTo(Void.class);
        }

        @Test
        @DisplayName("getPrimitiveClass")
        void testGetPrimitiveClass() {
            assertThat(OpenClass.getPrimitiveClass(Integer.class)).isEqualTo(int.class);
            assertThat(OpenClass.getPrimitiveClass(Boolean.class)).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("getComponentType")
        void testGetComponentType() {
            assertThat(OpenClass.getComponentType(int[].class)).isEqualTo(int.class);
            assertThat(OpenClass.getComponentType(String[].class)).isEqualTo(String.class);
            assertThat(OpenClass.getComponentType(String.class)).isNull();
        }

        @Test
        @DisplayName("getArrayClass")
        void testGetArrayClass() {
            assertThat(OpenClass.getArrayClass(int.class)).isEqualTo(int[].class);
            assertThat(OpenClass.getArrayClass(String.class)).isEqualTo(String[].class);
        }
    }

    @Nested
    @DisplayName("类名操作测试")
    class ClassNameTests {

        @Test
        @DisplayName("getSimpleName")
        void testGetSimpleName() {
            assertThat(OpenClass.getSimpleName(String.class)).isEqualTo("String");
            assertThat(OpenClass.getSimpleName(null)).isNull();
        }

        @Test
        @DisplayName("getShortName")
        void testGetShortName() {
            assertThat(OpenClass.getShortName(String.class)).isEqualTo("String");
        }

        @Test
        @DisplayName("getFullName")
        void testGetFullName() {
            assertThat(OpenClass.getFullName(String.class)).isEqualTo("java.lang.String");
            assertThat(OpenClass.getFullName(null)).isNull();
        }

        @Test
        @DisplayName("getPackageName")
        void testGetPackageName() {
            assertThat(OpenClass.getPackageName(String.class)).isEqualTo("java.lang");
            assertThat(OpenClass.getPackageName((Class<?>) null)).isNull();
        }

        @Test
        @DisplayName("getPackageName 从字符串")
        void testGetPackageNameFromString() {
            assertThat(OpenClass.getPackageName("java.lang.String")).isEqualTo("java.lang");
            assertThat(OpenClass.getPackageName("String")).isEmpty();
            assertThat(OpenClass.getPackageName((String) null)).isNull();
        }

        @Test
        @DisplayName("classNameToPath")
        void testClassNameToPath() {
            assertThat(OpenClass.classNameToPath("java.lang.String")).isEqualTo("java/lang/String");
            assertThat(OpenClass.classNameToPath(null)).isNull();
        }

        @Test
        @DisplayName("pathToClassName")
        void testPathToClassName() {
            assertThat(OpenClass.pathToClassName("java/lang/String")).isEqualTo("java.lang.String");
            assertThat(OpenClass.pathToClassName(null)).isNull();
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("getSuperClasses")
        void testGetSuperClasses() {
            List<Class<?>> superClasses = OpenClass.getSuperClasses(ArrayList.class);
            assertThat(superClasses).contains(AbstractList.class, AbstractCollection.class);
        }

        @Test
        @DisplayName("getSuperClasses null")
        void testGetSuperClassesNull() {
            assertThat(OpenClass.getSuperClasses(null)).isEmpty();
        }

        @Test
        @DisplayName("getAllInterfaces")
        void testGetAllInterfaces() {
            Set<Class<?>> interfaces = OpenClass.getAllInterfaces(ArrayList.class);
            assertThat(interfaces).contains(List.class, Collection.class, Iterable.class, Serializable.class);
        }

        @Test
        @DisplayName("getAllInterfaces null")
        void testGetAllInterfacesNull() {
            assertThat(OpenClass.getAllInterfaces(null)).isEmpty();
        }

        @Test
        @DisplayName("getAllSuperTypes")
        void testGetAllSuperTypes() {
            Set<Class<?>> superTypes = OpenClass.getAllSuperTypes(ArrayList.class);
            assertThat(superTypes).contains(AbstractList.class, List.class, Collection.class);
        }

        @Test
        @DisplayName("getCommonSuperClass")
        void testGetCommonSuperClass() {
            assertThat(OpenClass.getCommonSuperClass(ArrayList.class, LinkedList.class))
                    .isEqualTo(AbstractList.class);
            assertThat(OpenClass.getCommonSuperClass(String.class, Integer.class))
                    .isEqualTo(Object.class);
            assertThat(OpenClass.getCommonSuperClass(null, String.class))
                    .isEqualTo(Object.class);
        }
    }

    @Nested
    @DisplayName("泛型处理测试")
    class GenericsTests {

        @Test
        @DisplayName("getTypeArguments")
        void testGetTypeArguments() {
            Type[] args = OpenClass.getTypeArguments(List.class);
            assertThat(args).hasSize(1);
        }

        @Test
        @DisplayName("getTypeArguments 无泛型")
        void testGetTypeArgumentsNone() {
            Type[] args = OpenClass.getTypeArguments(String.class);
            assertThat(args).isEmpty();
        }

        @Test
        @DisplayName("getSuperclassTypeArguments")
        void testGetSuperclassTypeArguments() {
            Type[] args = OpenClass.getSuperclassTypeArguments(TestChild.class);
            assertThat(args).hasSize(1);
            assertThat(args[0]).isEqualTo(String.class);
        }

        @Test
        @DisplayName("getInterfaceTypeArguments")
        void testGetInterfaceTypeArguments() {
            Type[] args = OpenClass.getInterfaceTypeArguments(TestChild.class, TestInterface.class);
            assertThat(args).hasSize(1);
            assertThat(args[0]).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("resolveTypeArgument")
        void testResolveTypeArgument() {
            Class<?> resolved = OpenClass.resolveTypeArgument(TestChild.class, TestParent.class);
            assertThat(resolved).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("getDefaultValue")
        void testGetDefaultValue() {
            assertThat(OpenClass.getDefaultValue(int.class)).isEqualTo(0);
            assertThat(OpenClass.getDefaultValue(boolean.class)).isEqualTo(false);
            assertThat(OpenClass.getDefaultValue(double.class)).isEqualTo(0.0);
            assertThat(OpenClass.getDefaultValue(char.class)).isEqualTo('\0');
            assertThat(OpenClass.getDefaultValue(String.class)).isNull();
            assertThat(OpenClass.getDefaultValue((Class<?>) null)).isNull();
        }

        @Test
        @DisplayName("getPrimitiveDefaultValue")
        void testGetPrimitiveDefaultValue() {
            assertThat(OpenClass.getPrimitiveDefaultValue(int.class)).isEqualTo(0);
            assertThat(OpenClass.getPrimitiveDefaultValue(long.class)).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("资源加载测试")
    class ResourceLoadingTests {

        @Test
        @DisplayName("getResource")
        void testGetResource() {
            URL url = OpenClass.getResource("java/lang/String.class");
            assertThat(url).isNotNull();
        }

        @Test
        @DisplayName("getResourceAsStream")
        void testGetResourceAsStream() {
            InputStream is = OpenClass.getResourceAsStream("java/lang/String.class");
            assertThat(is).isNotNull();
        }

        @Test
        @DisplayName("getCodeSourceLocation")
        void testGetCodeSourceLocation() {
            URL location = OpenClass.getCodeSourceLocation(OpenClass.class);
            // 可能为 null（如果没有 CodeSource）
        }
    }

    @Nested
    @DisplayName("实例化测试")
    class InstantiationTests {

        @Test
        @DisplayName("newInstance")
        void testNewInstance() {
            SimpleClass instance = OpenClass.newInstance(SimpleClass.class);
            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("newInstance 无默认构造函数抛异常")
        void testNewInstanceNoDefaultConstructor() {
            assertThatThrownBy(() -> OpenClass.newInstance(Integer.class))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
