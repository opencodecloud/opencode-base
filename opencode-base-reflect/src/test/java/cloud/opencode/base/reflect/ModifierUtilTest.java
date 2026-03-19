package cloud.opencode.base.reflect;

import org.junit.jupiter.api.*;

import java.lang.reflect.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ModifierUtilTest Tests
 * ModifierUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("ModifierUtil 测试")
class ModifierUtilTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = ModifierUtil.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("isPublic方法测试")
    class IsPublicTests {

        @Test
        @DisplayName("检查public修饰符")
        void testIsPublicModifiers() {
            assertThat(ModifierUtil.isPublic(Modifier.PUBLIC)).isTrue();
            assertThat(ModifierUtil.isPublic(Modifier.PRIVATE)).isFalse();
        }

        @Test
        @DisplayName("检查public成员")
        void testIsPublicMember() throws Exception {
            Method publicMethod = String.class.getMethod("length");
            assertThat(ModifierUtil.isPublic(publicMethod)).isTrue();
        }

        @Test
        @DisplayName("检查public类")
        void testIsPublicClass() {
            assertThat(ModifierUtil.isPublic(String.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("isPrivate方法测试")
    class IsPrivateTests {

        @Test
        @DisplayName("检查private修饰符")
        void testIsPrivateModifiers() {
            assertThat(ModifierUtil.isPrivate(Modifier.PRIVATE)).isTrue();
            assertThat(ModifierUtil.isPrivate(Modifier.PUBLIC)).isFalse();
        }

        @Test
        @DisplayName("检查private成员")
        void testIsPrivateMember() throws Exception {
            Field privateField = String.class.getDeclaredField("value");
            assertThat(ModifierUtil.isPrivate(privateField)).isTrue();
        }
    }

    @Nested
    @DisplayName("isProtected方法测试")
    class IsProtectedTests {

        @Test
        @DisplayName("检查protected修饰符")
        void testIsProtectedModifiers() {
            assertThat(ModifierUtil.isProtected(Modifier.PROTECTED)).isTrue();
            assertThat(ModifierUtil.isProtected(Modifier.PUBLIC)).isFalse();
        }

        @Test
        @DisplayName("检查protected成员")
        void testIsProtectedMember() throws Exception {
            Method cloneMethod = Object.class.getDeclaredMethod("clone");
            assertThat(ModifierUtil.isProtected(cloneMethod)).isTrue();
        }
    }

    @Nested
    @DisplayName("isPackagePrivate方法测试")
    class IsPackagePrivateTests {

        @Test
        @DisplayName("检查包私有修饰符")
        void testIsPackagePrivateModifiers() {
            assertThat(ModifierUtil.isPackagePrivate(0)).isTrue();
            assertThat(ModifierUtil.isPackagePrivate(Modifier.PUBLIC)).isFalse();
        }

        @Test
        @DisplayName("检查包私有成员")
        void testIsPackagePrivateMember() throws Exception {
            Method publicMethod = String.class.getMethod("length");
            assertThat(ModifierUtil.isPackagePrivate(publicMethod)).isFalse();
        }
    }

    @Nested
    @DisplayName("isStatic方法测试")
    class IsStaticTests {

        @Test
        @DisplayName("检查static修饰符")
        void testIsStaticModifiers() {
            assertThat(ModifierUtil.isStatic(Modifier.STATIC)).isTrue();
            assertThat(ModifierUtil.isStatic(0)).isFalse();
        }

        @Test
        @DisplayName("检查static成员")
        void testIsStaticMember() throws Exception {
            Field staticField = String.class.getDeclaredField("CASE_INSENSITIVE_ORDER");
            assertThat(ModifierUtil.isStatic(staticField)).isTrue();
        }
    }

    @Nested
    @DisplayName("isFinal方法测试")
    class IsFinalTests {

        @Test
        @DisplayName("检查final修饰符")
        void testIsFinalModifiers() {
            assertThat(ModifierUtil.isFinal(Modifier.FINAL)).isTrue();
            assertThat(ModifierUtil.isFinal(0)).isFalse();
        }

        @Test
        @DisplayName("检查final成员")
        void testIsFinalMember() throws Exception {
            Field valueField = String.class.getDeclaredField("value");
            assertThat(ModifierUtil.isFinal(valueField)).isTrue();
        }

        @Test
        @DisplayName("检查final类")
        void testIsFinalClass() {
            assertThat(ModifierUtil.isFinal(String.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("isAbstract方法测试")
    class IsAbstractTests {

        @Test
        @DisplayName("检查abstract修饰符")
        void testIsAbstractModifiers() {
            assertThat(ModifierUtil.isAbstract(Modifier.ABSTRACT)).isTrue();
            assertThat(ModifierUtil.isAbstract(0)).isFalse();
        }

        @Test
        @DisplayName("检查abstract类")
        void testIsAbstractClass() {
            assertThat(ModifierUtil.isAbstract(Number.class)).isTrue();
            assertThat(ModifierUtil.isAbstract(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isTransient方法测试")
    class IsTransientTests {

        @Test
        @DisplayName("检查transient修饰符")
        void testIsTransientModifiers() {
            assertThat(ModifierUtil.isTransient(Modifier.TRANSIENT)).isTrue();
            assertThat(ModifierUtil.isTransient(0)).isFalse();
        }
    }

    @Nested
    @DisplayName("isVolatile方法测试")
    class IsVolatileTests {

        @Test
        @DisplayName("检查volatile修饰符")
        void testIsVolatileModifiers() {
            assertThat(ModifierUtil.isVolatile(Modifier.VOLATILE)).isTrue();
            assertThat(ModifierUtil.isVolatile(0)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSynchronized方法测试")
    class IsSynchronizedTests {

        @Test
        @DisplayName("检查synchronized修饰符")
        void testIsSynchronizedModifiers() {
            assertThat(ModifierUtil.isSynchronized(Modifier.SYNCHRONIZED)).isTrue();
            assertThat(ModifierUtil.isSynchronized(0)).isFalse();
        }
    }

    @Nested
    @DisplayName("isNative方法测试")
    class IsNativeTests {

        @Test
        @DisplayName("检查native修饰符")
        void testIsNativeModifiers() {
            assertThat(ModifierUtil.isNative(Modifier.NATIVE)).isTrue();
            assertThat(ModifierUtil.isNative(0)).isFalse();
        }
    }

    @Nested
    @DisplayName("isStrict方法测试")
    class IsStrictTests {

        @Test
        @DisplayName("检查strictfp修饰符")
        void testIsStrictModifiers() {
            assertThat(ModifierUtil.isStrict(Modifier.STRICT)).isTrue();
            assertThat(ModifierUtil.isStrict(0)).isFalse();
        }
    }

    @Nested
    @DisplayName("hasAll方法测试")
    class HasAllTests {

        @Test
        @DisplayName("检查是否包含所有修饰符")
        void testHasAll() {
            int modifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
            assertThat(ModifierUtil.hasAll(modifiers, Modifier.PUBLIC | Modifier.STATIC)).isTrue();
            assertThat(ModifierUtil.hasAll(modifiers, Modifier.PUBLIC | Modifier.ABSTRACT)).isFalse();
        }
    }

    @Nested
    @DisplayName("hasAny方法测试")
    class HasAnyTests {

        @Test
        @DisplayName("检查是否包含任一修饰符")
        void testHasAny() {
            int modifiers = Modifier.PUBLIC | Modifier.FINAL;
            assertThat(ModifierUtil.hasAny(modifiers, Modifier.PUBLIC | Modifier.STATIC)).isTrue();
            assertThat(ModifierUtil.hasAny(modifiers, Modifier.ABSTRACT | Modifier.STATIC)).isFalse();
        }
    }

    @Nested
    @DisplayName("hasNone方法测试")
    class HasNoneTests {

        @Test
        @DisplayName("检查是否不包含任何修饰符")
        void testHasNone() {
            int modifiers = Modifier.PUBLIC | Modifier.FINAL;
            assertThat(ModifierUtil.hasNone(modifiers, Modifier.ABSTRACT | Modifier.STATIC)).isTrue();
            assertThat(ModifierUtil.hasNone(modifiers, Modifier.PUBLIC | Modifier.STATIC)).isFalse();
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("修饰符转字符串")
        void testToString() {
            String result = ModifierUtil.toString(Modifier.PUBLIC | Modifier.STATIC);
            assertThat(result).contains("public");
            assertThat(result).contains("static");
        }
    }

    @Nested
    @DisplayName("getAccessLevelName方法测试")
    class GetAccessLevelNameTests {

        @Test
        @DisplayName("获取访问级别名称")
        void testGetAccessLevelName() {
            assertThat(ModifierUtil.getAccessLevelName(Modifier.PUBLIC)).isEqualTo("public");
            assertThat(ModifierUtil.getAccessLevelName(Modifier.PRIVATE)).isEqualTo("private");
            assertThat(ModifierUtil.getAccessLevelName(Modifier.PROTECTED)).isEqualTo("protected");
            assertThat(ModifierUtil.getAccessLevelName(0)).isEqualTo("package-private");
        }
    }

    @Nested
    @DisplayName("修饰符常量方法测试")
    class ModifierConstantsTests {

        @Test
        @DisplayName("publicModifier返回正确值")
        void testPublicModifier() {
            assertThat(ModifierUtil.publicModifier()).isEqualTo(Modifier.PUBLIC);
        }

        @Test
        @DisplayName("privateModifier返回正确值")
        void testPrivateModifier() {
            assertThat(ModifierUtil.privateModifier()).isEqualTo(Modifier.PRIVATE);
        }

        @Test
        @DisplayName("protectedModifier返回正确值")
        void testProtectedModifier() {
            assertThat(ModifierUtil.protectedModifier()).isEqualTo(Modifier.PROTECTED);
        }

        @Test
        @DisplayName("staticModifier返回正确值")
        void testStaticModifier() {
            assertThat(ModifierUtil.staticModifier()).isEqualTo(Modifier.STATIC);
        }

        @Test
        @DisplayName("finalModifier返回正确值")
        void testFinalModifier() {
            assertThat(ModifierUtil.finalModifier()).isEqualTo(Modifier.FINAL);
        }

        @Test
        @DisplayName("abstractModifier返回正确值")
        void testAbstractModifier() {
            assertThat(ModifierUtil.abstractModifier()).isEqualTo(Modifier.ABSTRACT);
        }

        @Test
        @DisplayName("transientModifier返回正确值")
        void testTransientModifier() {
            assertThat(ModifierUtil.transientModifier()).isEqualTo(Modifier.TRANSIENT);
        }

        @Test
        @DisplayName("volatileModifier返回正确值")
        void testVolatileModifier() {
            assertThat(ModifierUtil.volatileModifier()).isEqualTo(Modifier.VOLATILE);
        }

        @Test
        @DisplayName("synchronizedModifier返回正确值")
        void testSynchronizedModifier() {
            assertThat(ModifierUtil.synchronizedModifier()).isEqualTo(Modifier.SYNCHRONIZED);
        }

        @Test
        @DisplayName("nativeModifier返回正确值")
        void testNativeModifier() {
            assertThat(ModifierUtil.nativeModifier()).isEqualTo(Modifier.NATIVE);
        }
    }
}
