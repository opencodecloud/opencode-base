package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenClassTest Tests
 * OpenClassTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("OpenClass 测试")
class OpenClassTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = OpenClass.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("forName方法测试")
    class ForNameTests {

        @Test
        @DisplayName("加载存在的类")
        void testForNameExisting() {
            Class<?> clazz = OpenClass.forName("java.lang.String");
            assertThat(clazz).isEqualTo(String.class);
        }

        @Test
        @DisplayName("加载原始类型")
        void testForNamePrimitive() {
            assertThat(OpenClass.forName("int")).isEqualTo(int.class);
            assertThat(OpenClass.forName("boolean")).isEqualTo(boolean.class);
            assertThat(OpenClass.forName("void")).isEqualTo(void.class);
        }

        @Test
        @DisplayName("加载不存在的类抛出异常")
        void testForNameNotFound() {
            assertThatThrownBy(() -> OpenClass.forName("com.nonexistent.Class"))
                    .isInstanceOf(OpenReflectException.class);
        }

        @Test
        @DisplayName("null类名抛出异常")
        void testForNameNull() {
            assertThatThrownBy(() -> OpenClass.forName(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("使用指定类加载器加载")
        void testForNameWithClassLoader() {
            Class<?> clazz = OpenClass.forName("java.lang.Integer", Thread.currentThread().getContextClassLoader());
            assertThat(clazz).isEqualTo(Integer.class);
        }
    }

    @Nested
    @DisplayName("forNameWithoutInit方法测试")
    class ForNameWithoutInitTests {

        @Test
        @DisplayName("加载类但不初始化")
        void testForNameWithoutInit() {
            Class<?> clazz = OpenClass.forNameWithoutInit("java.lang.String");
            assertThat(clazz).isEqualTo(String.class);
        }

        @Test
        @DisplayName("加载不存在的类抛出异常")
        void testForNameWithoutInitNotFound() {
            assertThatThrownBy(() -> OpenClass.forNameWithoutInit("com.nonexistent.Class"))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("forNameSafe方法测试")
    class ForNameSafeTests {

        @Test
        @DisplayName("安全加载存在的类")
        void testForNameSafeExisting() {
            Optional<Class<?>> result = OpenClass.forNameSafe("java.lang.String");
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("安全加载不存在的类返回空")
        void testForNameSafeNotFound() {
            Optional<Class<?>> result = OpenClass.forNameSafe("com.nonexistent.Class");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("exists方法测试")
    class ExistsTests {

        @Test
        @DisplayName("检查存在的类")
        void testExistsTrue() {
            assertThat(OpenClass.exists("java.lang.String")).isTrue();
        }

        @Test
        @DisplayName("检查不存在的类")
        void testExistsFalse() {
            assertThat(OpenClass.exists("com.nonexistent.Class")).isFalse();
        }
    }

    @Nested
    @DisplayName("getPrimitiveClass方法测试")
    class GetPrimitiveClassTests {

        @Test
        @DisplayName("获取原始类型")
        void testGetPrimitiveClass() {
            assertThat(OpenClass.getPrimitiveClass("int")).isEqualTo(int.class);
            assertThat(OpenClass.getPrimitiveClass("boolean")).isEqualTo(boolean.class);
            assertThat(OpenClass.getPrimitiveClass("char")).isEqualTo(char.class);
        }

        @Test
        @DisplayName("获取不存在的原始类型返回null")
        void testGetPrimitiveClassNotFound() {
            assertThat(OpenClass.getPrimitiveClass("string")).isNull();
        }
    }

    @Nested
    @DisplayName("getShortClassName方法测试")
    class GetShortClassNameTests {

        @Test
        @DisplayName("获取简短类名")
        void testGetShortClassName() {
            assertThat(OpenClass.getShortClassName(String.class)).isEqualTo("String");
        }

        @Test
        @DisplayName("获取null类返回空字符串")
        void testGetShortClassNameNull() {
            assertThat(OpenClass.getShortClassName(null)).isEmpty();
        }

        @Test
        @DisplayName("获取内部类简短名")
        void testGetShortClassNameInner() {
            assertThat(OpenClass.getShortClassName(java.util.Map.Entry.class)).contains("Entry");
        }
    }

    @Nested
    @DisplayName("getSimpleName方法测试")
    class GetSimpleNameTests {

        @Test
        @DisplayName("获取简单名称")
        void testGetSimpleName() {
            assertThat(OpenClass.getSimpleName(String.class)).isEqualTo("String");
        }
    }

    @Nested
    @DisplayName("getPackageName方法测试")
    class GetPackageNameTests {

        @Test
        @DisplayName("获取包名")
        void testGetPackageName() {
            assertThat(OpenClass.getPackageName(String.class)).isEqualTo("java.lang");
        }
    }

    @Nested
    @DisplayName("getCanonicalName方法测试")
    class GetCanonicalNameTests {

        @Test
        @DisplayName("获取规范名")
        void testGetCanonicalName() {
            assertThat(OpenClass.getCanonicalName(String.class)).isEqualTo("java.lang.String");
        }
    }

    @Nested
    @DisplayName("getClassLocation方法测试")
    class GetClassLocationTests {

        @Test
        @DisplayName("获取类位置")
        void testGetClassLocation() {
            Optional<URL> result = OpenClass.getClassLocation(String.class);
            // JDK classes might not have a location in some environments
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("isPrimitive方法测试")
    class IsPrimitiveTests {

        @Test
        @DisplayName("检查原始类型")
        void testIsPrimitive() {
            assertThat(OpenClass.isPrimitive(int.class)).isTrue();
            assertThat(OpenClass.isPrimitive(Integer.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isWrapper方法测试")
    class IsWrapperTests {

        @Test
        @DisplayName("检查包装类型")
        void testIsWrapper() {
            assertThat(OpenClass.isWrapper(Integer.class)).isTrue();
            assertThat(OpenClass.isWrapper(int.class)).isFalse();
            assertThat(OpenClass.isWrapper(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isPrimitiveOrWrapper方法测试")
    class IsPrimitiveOrWrapperTests {

        @Test
        @DisplayName("检查原始或包装类型")
        void testIsPrimitiveOrWrapper() {
            assertThat(OpenClass.isPrimitiveOrWrapper(int.class)).isTrue();
            assertThat(OpenClass.isPrimitiveOrWrapper(Integer.class)).isTrue();
            assertThat(OpenClass.isPrimitiveOrWrapper(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isArray方法测试")
    class IsArrayTests {

        @Test
        @DisplayName("检查数组类型")
        void testIsArray() {
            assertThat(OpenClass.isArray(String[].class)).isTrue();
            assertThat(OpenClass.isArray(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isEnum方法测试")
    class IsEnumTests {

        @Test
        @DisplayName("检查枚举类型")
        void testIsEnum() {
            assertThat(OpenClass.isEnum(Thread.State.class)).isTrue();
            assertThat(OpenClass.isEnum(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAnnotation方法测试")
    class IsAnnotationTests {

        @Test
        @DisplayName("检查注解类型")
        void testIsAnnotation() {
            assertThat(OpenClass.isAnnotation(Override.class)).isTrue();
            assertThat(OpenClass.isAnnotation(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isInterface方法测试")
    class IsInterfaceTests {

        @Test
        @DisplayName("检查接口类型")
        void testIsInterface() {
            assertThat(OpenClass.isInterface(Runnable.class)).isTrue();
            assertThat(OpenClass.isInterface(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAbstract方法测试")
    class IsAbstractTests {

        @Test
        @DisplayName("检查抽象类型")
        void testIsAbstract() {
            assertThat(OpenClass.isAbstract(Number.class)).isTrue();
            assertThat(OpenClass.isAbstract(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isFinal方法测试")
    class IsFinalTests {

        @Test
        @DisplayName("检查final类型")
        void testIsFinal() {
            assertThat(OpenClass.isFinal(String.class)).isTrue();
            assertThat(OpenClass.isFinal(Object.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isInnerClass方法测试")
    class IsInnerClassTests {

        @Test
        @DisplayName("检查内部类")
        void testIsInnerClass() {
            assertThat(OpenClass.isInnerClass(java.util.Map.Entry.class)).isTrue();
            assertThat(OpenClass.isInnerClass(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAnonymousClass方法测试")
    class IsAnonymousClassTests {

        @Test
        @DisplayName("检查匿名类")
        void testIsAnonymousClass() {
            Runnable anonymous = new Runnable() {
                @Override
                public void run() {
                }
            };
            assertThat(OpenClass.isAnonymousClass(anonymous.getClass())).isTrue();
            assertThat(OpenClass.isAnonymousClass(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isRecord方法测试")
    class IsRecordTests {

        @Test
        @DisplayName("检查Record类型")
        void testIsRecord() {
            record TestRecord(String name) {
            }
            assertThat(OpenClass.isRecord(TestRecord.class)).isTrue();
            assertThat(OpenClass.isRecord(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSealed方法测试")
    class IsSealedTests {

        @Test
        @DisplayName("检查密封类型")
        void testIsSealed() {
            // String is not sealed
            assertThat(OpenClass.isSealed(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isFunctionalInterface方法测试")
    class IsFunctionalInterfaceTests {

        @Test
        @DisplayName("检查函数式接口")
        void testIsFunctionalInterface() {
            assertThat(OpenClass.isFunctionalInterface(Runnable.class)).isTrue();
            assertThat(OpenClass.isFunctionalInterface(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAssignable方法测试")
    class IsAssignableTests {

        @Test
        @DisplayName("检查类型赋值兼容")
        void testIsAssignable() {
            assertThat(OpenClass.isAssignable(Object.class, String.class)).isTrue();
            assertThat(OpenClass.isAssignable(String.class, Object.class)).isFalse();
        }

        @Test
        @DisplayName("检查原始类型和包装类型兼容")
        void testIsAssignableAutoboxing() {
            assertThat(OpenClass.isAssignable(int.class, Integer.class, true)).isTrue();
            assertThat(OpenClass.isAssignable(Integer.class, int.class, true)).isTrue();
            assertThat(OpenClass.isAssignable(int.class, Integer.class, false)).isFalse();
        }
    }

    @Nested
    @DisplayName("getAllSuperclasses方法测试")
    class GetAllSuperclassesTests {

        @Test
        @DisplayName("获取所有父类")
        void testGetAllSuperclasses() {
            List<Class<?>> superclasses = OpenClass.getAllSuperclasses(Integer.class);
            assertThat(superclasses).contains(Number.class);
            assertThat(superclasses).doesNotContain(Object.class);
        }

        @Test
        @DisplayName("Object没有父类")
        void testGetAllSuperclassesObject() {
            List<Class<?>> superclasses = OpenClass.getAllSuperclasses(Object.class);
            assertThat(superclasses).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAllInterfaces方法测试")
    class GetAllInterfacesTests {

        @Test
        @DisplayName("获取所有接口")
        void testGetAllInterfaces() {
            List<Class<?>> interfaces = OpenClass.getAllInterfaces(Integer.class);
            assertThat(interfaces).contains(Comparable.class, Serializable.class);
        }
    }

    @Nested
    @DisplayName("getClassHierarchy方法测试")
    class GetClassHierarchyTests {

        @Test
        @DisplayName("获取类层次结构")
        void testGetClassHierarchy() {
            List<Class<?>> hierarchy = OpenClass.getClassHierarchy(Integer.class);
            assertThat(hierarchy).contains(Integer.class, Number.class, Object.class);
        }
    }

    @Nested
    @DisplayName("primitiveToWrapper方法测试")
    class PrimitiveToWrapperTests {

        @Test
        @DisplayName("原始类型转包装类型")
        void testPrimitiveToWrapper() {
            assertThat(OpenClass.primitiveToWrapper(int.class)).isEqualTo(Integer.class);
            assertThat(OpenClass.primitiveToWrapper(boolean.class)).isEqualTo(Boolean.class);
        }

        @Test
        @DisplayName("非原始类型返回原类型")
        void testPrimitiveToWrapperNonPrimitive() {
            assertThat(OpenClass.primitiveToWrapper(String.class)).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("wrapperToPrimitive方法测试")
    class WrapperToPrimitiveTests {

        @Test
        @DisplayName("包装类型转原始类型")
        void testWrapperToPrimitive() {
            assertThat(OpenClass.wrapperToPrimitive(Integer.class)).isEqualTo(int.class);
            assertThat(OpenClass.wrapperToPrimitive(Boolean.class)).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("非包装类型返回原类型")
        void testWrapperToPrimitiveNonWrapper() {
            assertThat(OpenClass.wrapperToPrimitive(String.class)).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getComponentType方法测试")
    class GetComponentTypeTests {

        @Test
        @DisplayName("获取数组组件类型")
        void testGetComponentType() {
            assertThat(OpenClass.getComponentType(String[].class)).isEqualTo(String.class);
            assertThat(OpenClass.getComponentType(int[].class)).isEqualTo(int.class);
        }
    }

    @Nested
    @DisplayName("getArrayClass方法测试")
    class GetArrayClassTests {

        @Test
        @DisplayName("获取数组类型")
        void testGetArrayClass() {
            assertThat(OpenClass.getArrayClass(String.class)).isEqualTo(String[].class);
            assertThat(OpenClass.getArrayClass(int.class)).isEqualTo(int[].class);
        }
    }

    @Nested
    @DisplayName("isInstantiable方法测试")
    class IsInstantiableTests {

        @Test
        @DisplayName("检查可实例化类")
        void testIsInstantiable() {
            assertThat(OpenClass.isInstantiable(Object.class)).isTrue();
            assertThat(OpenClass.isInstantiable(Number.class)).isFalse();
            assertThat(OpenClass.isInstantiable(Runnable.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("hasDefaultConstructor方法测试")
    class HasDefaultConstructorTests {

        @Test
        @DisplayName("检查有默认构造函数")
        void testHasDefaultConstructor() {
            assertThat(OpenClass.hasDefaultConstructor(Object.class)).isTrue();
        }

        @Test
        @DisplayName("检查无默认构造函数")
        void testNoDefaultConstructor() {
            assertThat(OpenClass.hasDefaultConstructor(Integer.class)).isFalse();
        }
    }
}
