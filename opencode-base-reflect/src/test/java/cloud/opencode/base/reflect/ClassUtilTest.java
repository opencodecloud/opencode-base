package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ClassUtilTest Tests
 * ClassUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("ClassUtil 测试")
class ClassUtilTest {

    @BeforeEach
    void setUp() {
        ClassUtil.clearCache();
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = ClassUtil.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("forName方法测试")
    class ForNameTests {

        @Test
        @DisplayName("加载存在的类")
        void testForNameExisting() {
            Class<?> clazz = ClassUtil.forName("java.lang.String");
            assertThat(clazz).isEqualTo(String.class);
        }

        @Test
        @DisplayName("加载的类会被缓存")
        void testForNameCached() {
            Class<?> clazz1 = ClassUtil.forName("java.lang.String");
            Class<?> clazz2 = ClassUtil.forName("java.lang.String");
            assertThat(clazz1).isSameAs(clazz2);
        }

        @Test
        @DisplayName("加载不存在的类抛出异常")
        void testForNameNotFound() {
            assertThatThrownBy(() -> ClassUtil.forName("com.nonexistent.Class"))
                    .isInstanceOf(OpenReflectException.class);
        }

        @Test
        @DisplayName("使用类加载器加载")
        void testForNameWithClassLoader() {
            Class<?> clazz = ClassUtil.forName("java.lang.Integer", Thread.currentThread().getContextClassLoader());
            assertThat(clazz).isEqualTo(Integer.class);
        }
    }

    @Nested
    @DisplayName("forNameSafe方法测试")
    class ForNameSafeTests {

        @Test
        @DisplayName("安全加载存在的类")
        void testForNameSafeExisting() {
            Optional<Class<?>> result = ClassUtil.forNameSafe("java.lang.String");
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("安全加载不存在的类返回空")
        void testForNameSafeNotFound() {
            Optional<Class<?>> result = ClassUtil.forNameSafe("com.nonexistent.Class");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("exists方法测试")
    class ExistsTests {

        @Test
        @DisplayName("检查存在的类")
        void testExistsTrue() {
            assertThat(ClassUtil.exists("java.lang.String")).isTrue();
        }

        @Test
        @DisplayName("检查不存在的类")
        void testExistsFalse() {
            assertThat(ClassUtil.exists("com.nonexistent.Class")).isFalse();
        }
    }

    @Nested
    @DisplayName("getAllSuperclasses方法测试")
    class GetAllSuperclassesTests {

        @Test
        @DisplayName("获取所有父类")
        void testGetAllSuperclasses() {
            List<Class<?>> superclasses = ClassUtil.getAllSuperclasses(Integer.class);
            assertThat(superclasses).contains(Number.class);
            assertThat(superclasses).doesNotContain(Object.class);
        }
    }

    @Nested
    @DisplayName("getAllInterfaces方法测试")
    class GetAllInterfacesTests {

        @Test
        @DisplayName("获取所有接口")
        void testGetAllInterfaces() {
            List<Class<?>> interfaces = ClassUtil.getAllInterfaces(Integer.class);
            assertThat(interfaces).contains(Comparable.class, Serializable.class);
        }

        @Test
        @DisplayName("接口结果被缓存")
        void testGetAllInterfacesCached() {
            List<Class<?>> interfaces1 = ClassUtil.getAllInterfaces(Integer.class);
            List<Class<?>> interfaces2 = ClassUtil.getAllInterfaces(Integer.class);
            assertThat(interfaces1).isSameAs(interfaces2);
        }
    }

    @Nested
    @DisplayName("getClassHierarchy方法测试")
    class GetClassHierarchyTests {

        @Test
        @DisplayName("获取完整类层次结构")
        void testGetClassHierarchy() {
            List<Class<?>> hierarchy = ClassUtil.getClassHierarchy(Integer.class);
            assertThat(hierarchy).contains(Integer.class, Number.class);
        }

        @Test
        @DisplayName("层次结构被缓存")
        void testGetClassHierarchyCached() {
            List<Class<?>> hierarchy1 = ClassUtil.getClassHierarchy(Integer.class);
            List<Class<?>> hierarchy2 = ClassUtil.getClassHierarchy(Integer.class);
            assertThat(hierarchy1).isSameAs(hierarchy2);
        }
    }

    @Nested
    @DisplayName("isPrimitive方法测试")
    class IsPrimitiveTests {

        @Test
        @DisplayName("检查原始类型")
        void testIsPrimitive() {
            assertThat(ClassUtil.isPrimitive(int.class)).isTrue();
            assertThat(ClassUtil.isPrimitive(Integer.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isWrapper方法测试")
    class IsWrapperTests {

        @Test
        @DisplayName("检查包装类型")
        void testIsWrapper() {
            assertThat(ClassUtil.isWrapper(Integer.class)).isTrue();
            assertThat(ClassUtil.isWrapper(int.class)).isFalse();
            assertThat(ClassUtil.isWrapper(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isPrimitiveOrWrapper方法测试")
    class IsPrimitiveOrWrapperTests {

        @Test
        @DisplayName("检查原始或包装类型")
        void testIsPrimitiveOrWrapper() {
            assertThat(ClassUtil.isPrimitiveOrWrapper(int.class)).isTrue();
            assertThat(ClassUtil.isPrimitiveOrWrapper(Integer.class)).isTrue();
            assertThat(ClassUtil.isPrimitiveOrWrapper(String.class)).isFalse();
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
            assertThat(ClassUtil.isRecord(TestRecord.class)).isTrue();
            assertThat(ClassUtil.isRecord(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSealed方法测试")
    class IsSealedTests {

        @Test
        @DisplayName("检查密封类")
        void testIsSealed() {
            assertThat(ClassUtil.isSealed(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isEnum方法测试")
    class IsEnumTests {

        @Test
        @DisplayName("检查枚举类型")
        void testIsEnum() {
            assertThat(ClassUtil.isEnum(Thread.State.class)).isTrue();
            assertThat(ClassUtil.isEnum(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isArray方法测试")
    class IsArrayTests {

        @Test
        @DisplayName("检查数组类型")
        void testIsArray() {
            assertThat(ClassUtil.isArray(String[].class)).isTrue();
            assertThat(ClassUtil.isArray(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isInterface方法测试")
    class IsInterfaceTests {

        @Test
        @DisplayName("检查接口类型")
        void testIsInterface() {
            assertThat(ClassUtil.isInterface(Runnable.class)).isTrue();
            assertThat(ClassUtil.isInterface(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAbstract方法测试")
    class IsAbstractTests {

        @Test
        @DisplayName("检查抽象类型")
        void testIsAbstract() {
            assertThat(ClassUtil.isAbstract(Number.class)).isTrue();
            assertThat(ClassUtil.isAbstract(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isFinal方法测试")
    class IsFinalTests {

        @Test
        @DisplayName("检查final类型")
        void testIsFinal() {
            assertThat(ClassUtil.isFinal(String.class)).isTrue();
            assertThat(ClassUtil.isFinal(Object.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAnonymous方法测试")
    class IsAnonymousTests {

        @Test
        @DisplayName("检查匿名类")
        void testIsAnonymous() {
            Runnable anonymous = new Runnable() {
                @Override
                public void run() {
                }
            };
            assertThat(ClassUtil.isAnonymous(anonymous.getClass())).isTrue();
            assertThat(ClassUtil.isAnonymous(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isInnerClass方法测试")
    class IsInnerClassTests {

        @Test
        @DisplayName("检查内部类")
        void testIsInnerClass() {
            assertThat(ClassUtil.isInnerClass(java.util.Map.Entry.class)).isTrue();
            assertThat(ClassUtil.isInnerClass(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isFunctionalInterface方法测试")
    class IsFunctionalInterfaceTests {

        @Test
        @DisplayName("检查函数式接口")
        void testIsFunctionalInterface() {
            assertThat(ClassUtil.isFunctionalInterface(Runnable.class)).isTrue();
            assertThat(ClassUtil.isFunctionalInterface(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("primitiveToWrapper方法测试")
    class PrimitiveToWrapperTests {

        @Test
        @DisplayName("原始类型转包装类型")
        void testPrimitiveToWrapper() {
            assertThat(ClassUtil.primitiveToWrapper(int.class)).isEqualTo(Integer.class);
            assertThat(ClassUtil.primitiveToWrapper(boolean.class)).isEqualTo(Boolean.class);
        }

        @Test
        @DisplayName("非原始类型返回原类型")
        void testPrimitiveToWrapperNonPrimitive() {
            assertThat(ClassUtil.primitiveToWrapper(String.class)).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("wrapperToPrimitive方法测试")
    class WrapperToPrimitiveTests {

        @Test
        @DisplayName("包装类型转原始类型")
        void testWrapperToPrimitive() {
            assertThat(ClassUtil.wrapperToPrimitive(Integer.class)).isEqualTo(int.class);
            assertThat(ClassUtil.wrapperToPrimitive(Boolean.class)).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("非包装类型返回null")
        void testWrapperToPrimitiveNonWrapper() {
            assertThat(ClassUtil.wrapperToPrimitive(String.class)).isNull();
        }
    }

    @Nested
    @DisplayName("getComponentType方法测试")
    class GetComponentTypeTests {

        @Test
        @DisplayName("获取数组组件类型")
        void testGetComponentType() {
            assertThat(ClassUtil.getComponentType(String[].class)).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getArrayClass方法测试")
    class GetArrayClassTests {

        @Test
        @DisplayName("获取数组类型")
        void testGetArrayClass() {
            assertThat(ClassUtil.getArrayClass(String.class)).isEqualTo(String[].class);
        }
    }

    @Nested
    @DisplayName("getSimpleName方法测试")
    class GetSimpleNameTests {

        @Test
        @DisplayName("获取简单名称")
        void testGetSimpleName() {
            assertThat(ClassUtil.getSimpleName(String.class)).isEqualTo("String");
        }

        @Test
        @DisplayName("数组类型的简单名称")
        void testGetSimpleNameArray() {
            assertThat(ClassUtil.getSimpleName(String[].class)).isEqualTo("String[]");
        }
    }

    @Nested
    @DisplayName("getCanonicalNameOrName方法测试")
    class GetCanonicalNameOrNameTests {

        @Test
        @DisplayName("获取规范名称")
        void testGetCanonicalNameOrName() {
            assertThat(ClassUtil.getCanonicalNameOrName(String.class)).isEqualTo("java.lang.String");
        }
    }

    @Nested
    @DisplayName("getPackageName方法测试")
    class GetPackageNameTests {

        @Test
        @DisplayName("获取包名")
        void testGetPackageName() {
            assertThat(ClassUtil.getPackageName(String.class)).isEqualTo("java.lang");
        }
    }

    @Nested
    @DisplayName("isSamePackage方法测试")
    class IsSamePackageTests {

        @Test
        @DisplayName("检查同一包")
        void testIsSamePackage() {
            assertThat(ClassUtil.isSamePackage(String.class, Integer.class)).isTrue();
            assertThat(ClassUtil.isSamePackage(String.class, java.util.List.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAssignableFrom方法测试")
    class IsAssignableFromTests {

        @Test
        @DisplayName("检查类型赋值兼容")
        void testIsAssignableFrom() {
            assertThat(ClassUtil.isAssignableFrom(Object.class, String.class)).isTrue();
            assertThat(ClassUtil.isAssignableFrom(String.class, Object.class)).isFalse();
        }

        @Test
        @DisplayName("检查原始和包装类型兼容")
        void testIsAssignableFromPrimitiveWrapper() {
            assertThat(ClassUtil.isAssignableFrom(int.class, Integer.class)).isTrue();
            assertThat(ClassUtil.isAssignableFrom(Integer.class, int.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("getCommonSuperclass方法测试")
    class GetCommonSuperclassTests {

        @Test
        @DisplayName("获取公共父类")
        void testGetCommonSuperclass() {
            assertThat(ClassUtil.getCommonSuperclass(Integer.class, Double.class)).isEqualTo(Number.class);
            assertThat(ClassUtil.getCommonSuperclass(String.class, Integer.class)).isEqualTo(Object.class);
        }

        @Test
        @DisplayName("其中一个是另一个的父类")
        void testGetCommonSuperclassDirectAncestor() {
            assertThat(ClassUtil.getCommonSuperclass(Object.class, String.class)).isEqualTo(Object.class);
            assertThat(ClassUtil.getCommonSuperclass(String.class, Object.class)).isEqualTo(Object.class);
        }
    }

    @Nested
    @DisplayName("clearCache方法测试")
    class ClearCacheTests {

        @Test
        @DisplayName("清除所有缓存")
        void testClearAllCache() {
            ClassUtil.forName("java.lang.String");
            ClassUtil.getAllInterfaces(String.class);
            ClassUtil.getClassHierarchy(String.class);

            ClassUtil.clearCache();
            // Should not throw, just verify clearCache works
        }

        @Test
        @DisplayName("清除特定类的缓存")
        void testClearCacheForClass() {
            ClassUtil.getAllInterfaces(String.class);
            ClassUtil.clearCache(String.class);
            // Should not throw
        }
    }
}
