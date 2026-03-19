package cloud.opencode.base.reflect.scan;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ClassInfoTest Tests
 * ClassInfoTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("ClassInfo 测试")
class ClassInfoTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("从资源名创建ClassInfo")
        void testCreate() {
            ClassInfo classInfo = new ClassInfo("java/lang/String.class", ClassLoader.getSystemClassLoader());
            assertThat(classInfo).isNotNull();
            assertThat(classInfo.getClassName()).isEqualTo("java.lang.String");
        }

        @Test
        @DisplayName("处理不带.class后缀的资源名")
        void testCreateWithoutSuffix() {
            ClassInfo classInfo = new ClassInfo("java/lang/String", ClassLoader.getSystemClassLoader());
            assertThat(classInfo.getClassName()).isEqualTo("java.lang.String");
        }
    }

    @Nested
    @DisplayName("fromClassName静态方法测试")
    class FromClassNameTests {

        @Test
        @DisplayName("从类名创建ClassInfo")
        void testFromClassName() {
            ClassInfo classInfo = ClassInfo.fromClassName("java.lang.String", ClassLoader.getSystemClassLoader());
            assertThat(classInfo.getClassName()).isEqualTo("java.lang.String");
        }
    }

    @Nested
    @DisplayName("fromClass静态方法测试")
    class FromClassTests {

        @Test
        @DisplayName("从Class对象创建ClassInfo")
        void testFromClass() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo.getClassName()).isEqualTo("cloud.opencode.base.reflect.scan.ClassInfoTest");
            assertThat(classInfo.isLoaded()).isTrue();
        }
    }

    @Nested
    @DisplayName("getClassName方法测试")
    class GetClassNameTests {

        @Test
        @DisplayName("获取完全限定类名")
        void testGetClassName() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo.getClassName()).isEqualTo("cloud.opencode.base.reflect.scan.ClassInfoTest");
        }
    }

    @Nested
    @DisplayName("getSimpleName方法测试")
    class GetSimpleNameTests {

        @Test
        @DisplayName("获取简单类名")
        void testGetSimpleName() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo.getSimpleName()).isEqualTo("ClassInfoTest");
        }

        @Test
        @DisplayName("无包类的简单名")
        void testGetSimpleNameNoPackage() {
            ClassInfo classInfo = new ClassInfo("SimpleClass.class", ClassLoader.getSystemClassLoader());
            assertThat(classInfo.getSimpleName()).isEqualTo("SimpleClass");
        }
    }

    @Nested
    @DisplayName("getPackageName方法测试")
    class GetPackageNameTests {

        @Test
        @DisplayName("获取包名")
        void testGetPackageName() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo.getPackageName()).isEqualTo("cloud.opencode.base.reflect.scan");
        }

        @Test
        @DisplayName("无包类返回空")
        void testGetPackageNameEmpty() {
            ClassInfo classInfo = new ClassInfo("SimpleClass.class", ClassLoader.getSystemClassLoader());
            assertThat(classInfo.getPackageName()).isEmpty();
        }
    }

    @Nested
    @DisplayName("load方法测试")
    class LoadTests {

        @Test
        @DisplayName("加载类")
        void testLoad() {
            ClassInfo classInfo = ClassInfo.fromClassName("java.lang.String", ClassLoader.getSystemClassLoader());
            Class<?> loaded = classInfo.load();
            assertThat(loaded).isEqualTo(String.class);
        }

        @Test
        @DisplayName("重复加载返回同一实例")
        void testLoadCached() {
            ClassInfo classInfo = ClassInfo.fromClassName("java.lang.String", ClassLoader.getSystemClassLoader());
            Class<?> first = classInfo.load();
            Class<?> second = classInfo.load();
            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("加载不存在的类抛出异常")
        void testLoadNotFound() {
            ClassInfo classInfo = ClassInfo.fromClassName("com.nonexistent.Class", ClassLoader.getSystemClassLoader());
            assertThatThrownBy(classInfo::load).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("isLoaded方法测试")
    class IsLoadedTests {

        @Test
        @DisplayName("未加载返回false")
        void testIsLoadedFalse() {
            ClassInfo classInfo = ClassInfo.fromClassName("java.lang.String", ClassLoader.getSystemClassLoader());
            assertThat(classInfo.isLoaded()).isFalse();
        }

        @Test
        @DisplayName("已加载返回true")
        void testIsLoadedTrue() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo.isLoaded()).isTrue();
        }
    }

    @Nested
    @DisplayName("isInnerClass方法测试")
    class IsInnerClassTests {

        @Test
        @DisplayName("内部类返回true")
        void testIsInnerClassTrue() {
            ClassInfo classInfo = ClassInfo.fromClassName("cloud.opencode.base.reflect.scan.ClassInfoTest$ConstructorTests", ClassLoader.getSystemClassLoader());
            assertThat(classInfo.isInnerClass()).isTrue();
        }

        @Test
        @DisplayName("非内部类返回false")
        void testIsInnerClassFalse() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo.isInnerClass()).isFalse();
        }
    }

    @Nested
    @DisplayName("isAnonymousClass方法测试")
    class IsAnonymousClassTests {

        @Test
        @DisplayName("匿名类返回true")
        void testIsAnonymousClassTrue() {
            // Test with a known anonymous class pattern
            ClassInfo classInfo = ClassInfo.fromClassName("cloud.opencode.base.reflect.scan.ClassInfoTest$1", ClassLoader.getSystemClassLoader());
            assertThat(classInfo.isAnonymousClass()).isTrue();
        }

        @Test
        @DisplayName("非匿名类返回false")
        void testIsAnonymousClassFalse() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo.isAnonymousClass()).isFalse();
        }
    }

    @Nested
    @DisplayName("getOuterClassName方法测试")
    class GetOuterClassNameTests {

        @Test
        @DisplayName("内部类获取外部类名")
        void testGetOuterClassName() {
            ClassInfo classInfo = ClassInfo.fromClassName("cloud.opencode.base.reflect.scan.ClassInfoTest$ConstructorTests", ClassLoader.getSystemClassLoader());
            assertThat(classInfo.getOuterClassName()).isEqualTo("cloud.opencode.base.reflect.scan.ClassInfoTest");
        }

        @Test
        @DisplayName("非内部类返回null")
        void testGetOuterClassNameNull() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo.getOuterClassName()).isNull();
        }
    }

    @Nested
    @DisplayName("isInPackage方法测试")
    class IsInPackageTests {

        @Test
        @DisplayName("在指定包中返回true")
        void testIsInPackageTrue() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo.isInPackage("cloud.opencode.base.reflect.scan")).isTrue();
        }

        @Test
        @DisplayName("不在指定包中返回false")
        void testIsInPackageFalse() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo.isInPackage("java.util")).isFalse();
        }
    }

    @Nested
    @DisplayName("isInPackageOrSubpackage方法测试")
    class IsInPackageOrSubpackageTests {

        @Test
        @DisplayName("在指定包中返回true")
        void testIsInPackageOrSubpackageExact() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo.isInPackageOrSubpackage("cloud.opencode.base.reflect.scan")).isTrue();
        }

        @Test
        @DisplayName("在父包中返回true")
        void testIsInPackageOrSubpackageParent() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo.isInPackageOrSubpackage("cloud.opencode.base.reflect")).isTrue();
        }

        @Test
        @DisplayName("不在包或子包中返回false")
        void testIsInPackageOrSubpackageFalse() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo.isInPackageOrSubpackage("javax")).isFalse();
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同类名相等")
        void testEquals() {
            ClassInfo c1 = ClassInfo.fromClass(ClassInfoTest.class);
            ClassInfo c2 = ClassInfo.fromClassName("cloud.opencode.base.reflect.scan.ClassInfoTest", ClassLoader.getSystemClassLoader());
            assertThat(c1).isEqualTo(c2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo).isEqualTo(classInfo);
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同类名有相同hashCode")
        void testHashCode() {
            ClassInfo c1 = ClassInfo.fromClass(ClassInfoTest.class);
            ClassInfo c2 = ClassInfo.fromClassName("cloud.opencode.base.reflect.scan.ClassInfoTest", ClassLoader.getSystemClassLoader());
            assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含类名")
        void testToString() {
            ClassInfo classInfo = ClassInfo.fromClass(ClassInfoTest.class);
            assertThat(classInfo.toString()).contains("ClassInfo");
            assertThat(classInfo.toString()).contains("cloud.opencode.base.reflect.scan.ClassInfoTest");
        }
    }
}
