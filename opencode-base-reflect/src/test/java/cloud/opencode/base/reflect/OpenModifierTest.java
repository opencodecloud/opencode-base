package cloud.opencode.base.reflect;

import org.junit.jupiter.api.*;

import java.lang.reflect.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenModifierTest Tests
 * OpenModifierTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("OpenModifier 测试")
class OpenModifierTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = OpenModifier.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("isPublic方法测试")
    class IsPublicTests {

        @Test
        @DisplayName("检查public修饰符")
        void testIsPublicModifiers() {
            assertThat(OpenModifier.isPublic(Modifier.PUBLIC)).isTrue();
            assertThat(OpenModifier.isPublic(Modifier.PRIVATE)).isFalse();
            assertThat(OpenModifier.isPublic(0)).isFalse();
        }

        @Test
        @DisplayName("检查public成员")
        void testIsPublicMember() throws Exception {
            Method publicMethod = String.class.getMethod("length");
            Field privateField = String.class.getDeclaredField("value");

            assertThat(OpenModifier.isPublic(publicMethod)).isTrue();
            assertThat(OpenModifier.isPublic(privateField)).isFalse();
        }

        @Test
        @DisplayName("检查public类")
        void testIsPublicClass() {
            assertThat(OpenModifier.isPublic(String.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("isPrivate方法测试")
    class IsPrivateTests {

        @Test
        @DisplayName("检查private修饰符")
        void testIsPrivateModifiers() {
            assertThat(OpenModifier.isPrivate(Modifier.PRIVATE)).isTrue();
            assertThat(OpenModifier.isPrivate(Modifier.PUBLIC)).isFalse();
        }

        @Test
        @DisplayName("检查private成员")
        void testIsPrivateMember() throws Exception {
            Field privateField = String.class.getDeclaredField("value");
            Method publicMethod = String.class.getMethod("length");

            assertThat(OpenModifier.isPrivate(privateField)).isTrue();
            assertThat(OpenModifier.isPrivate(publicMethod)).isFalse();
        }

        @Test
        @DisplayName("检查private类")
        void testIsPrivateClass() {
            assertThat(OpenModifier.isPrivate(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isProtected方法测试")
    class IsProtectedTests {

        @Test
        @DisplayName("检查protected修饰符")
        void testIsProtectedModifiers() {
            assertThat(OpenModifier.isProtected(Modifier.PROTECTED)).isTrue();
            assertThat(OpenModifier.isProtected(Modifier.PUBLIC)).isFalse();
        }

        @Test
        @DisplayName("检查protected成员")
        void testIsProtectedMember() throws Exception {
            Method cloneMethod = Object.class.getDeclaredMethod("clone");
            assertThat(OpenModifier.isProtected(cloneMethod)).isTrue();
        }

        @Test
        @DisplayName("检查protected类")
        void testIsProtectedClass() {
            assertThat(OpenModifier.isProtected(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isPackagePrivate方法测试")
    class IsPackagePrivateTests {

        @Test
        @DisplayName("检查包私有修饰符")
        void testIsPackagePrivateModifiers() {
            assertThat(OpenModifier.isPackagePrivate(0)).isTrue();
            assertThat(OpenModifier.isPackagePrivate(Modifier.PUBLIC)).isFalse();
            assertThat(OpenModifier.isPackagePrivate(Modifier.PRIVATE)).isFalse();
            assertThat(OpenModifier.isPackagePrivate(Modifier.PROTECTED)).isFalse();
        }

        @Test
        @DisplayName("检查包私有成员")
        void testIsPackagePrivateMember() throws Exception {
            // String has private fields, not package-private
            Method publicMethod = String.class.getMethod("length");
            assertThat(OpenModifier.isPackagePrivate(publicMethod)).isFalse();
        }

        @Test
        @DisplayName("检查包私有类")
        void testIsPackagePrivateClass() {
            assertThat(OpenModifier.isPackagePrivate(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isStatic方法测试")
    class IsStaticTests {

        @Test
        @DisplayName("检查static修饰符")
        void testIsStaticModifiers() {
            assertThat(OpenModifier.isStatic(Modifier.STATIC)).isTrue();
            assertThat(OpenModifier.isStatic(0)).isFalse();
        }

        @Test
        @DisplayName("检查static成员")
        void testIsStaticMember() throws Exception {
            Field typeField = String.class.getDeclaredField("CASE_INSENSITIVE_ORDER");
            Method lengthMethod = String.class.getMethod("length");

            assertThat(OpenModifier.isStatic(typeField)).isTrue();
            assertThat(OpenModifier.isStatic(lengthMethod)).isFalse();
        }
    }

    @Nested
    @DisplayName("isFinal方法测试")
    class IsFinalTests {

        @Test
        @DisplayName("检查final修饰符")
        void testIsFinalModifiers() {
            assertThat(OpenModifier.isFinal(Modifier.FINAL)).isTrue();
            assertThat(OpenModifier.isFinal(0)).isFalse();
        }

        @Test
        @DisplayName("检查final成员")
        void testIsFinalMember() throws Exception {
            Field valueField = String.class.getDeclaredField("value");
            assertThat(OpenModifier.isFinal(valueField)).isTrue();
        }

        @Test
        @DisplayName("检查final类")
        void testIsFinalClass() {
            assertThat(OpenModifier.isFinal(String.class)).isTrue();
            assertThat(OpenModifier.isFinal(Object.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAbstract方法测试")
    class IsAbstractTests {

        @Test
        @DisplayName("检查abstract修饰符")
        void testIsAbstractModifiers() {
            assertThat(OpenModifier.isAbstract(Modifier.ABSTRACT)).isTrue();
            assertThat(OpenModifier.isAbstract(0)).isFalse();
        }

        @Test
        @DisplayName("检查abstract类")
        void testIsAbstractClass() {
            assertThat(OpenModifier.isAbstract(Number.class)).isTrue();
            assertThat(OpenModifier.isAbstract(String.class)).isFalse();
        }

        @Test
        @DisplayName("检查abstract方法")
        void testIsAbstractMethod() throws Exception {
            Method abstractMethod = Number.class.getDeclaredMethod("intValue");
            assertThat(OpenModifier.isAbstract(abstractMethod)).isTrue();
        }
    }

    @Nested
    @DisplayName("isSynchronized方法测试")
    class IsSynchronizedTests {

        @Test
        @DisplayName("检查synchronized修饰符")
        void testIsSynchronizedModifiers() {
            assertThat(OpenModifier.isSynchronized(Modifier.SYNCHRONIZED)).isTrue();
            assertThat(OpenModifier.isSynchronized(0)).isFalse();
        }

        @Test
        @DisplayName("检查synchronized方法")
        void testIsSynchronizedMethod() throws Exception {
            Method waitMethod = Object.class.getDeclaredMethod("wait", long.class);
            // wait method is not synchronized
            assertThat(OpenModifier.isSynchronized(waitMethod)).isFalse();
        }
    }

    @Nested
    @DisplayName("isVolatile方法测试")
    class IsVolatileTests {

        @Test
        @DisplayName("检查volatile修饰符")
        void testIsVolatileModifiers() {
            assertThat(OpenModifier.isVolatile(Modifier.VOLATILE)).isTrue();
            assertThat(OpenModifier.isVolatile(0)).isFalse();
        }

        @Test
        @DisplayName("检查volatile字段")
        void testIsVolatileField() throws Exception {
            // String doesn't have volatile fields
            Field valueField = String.class.getDeclaredField("value");
            assertThat(OpenModifier.isVolatile(valueField)).isFalse();
        }
    }

    @Nested
    @DisplayName("isTransient方法测试")
    class IsTransientTests {

        @Test
        @DisplayName("检查transient修饰符")
        void testIsTransientModifiers() {
            assertThat(OpenModifier.isTransient(Modifier.TRANSIENT)).isTrue();
            assertThat(OpenModifier.isTransient(0)).isFalse();
        }

        @Test
        @DisplayName("检查transient字段")
        void testIsTransientField() throws Exception {
            Field valueField = String.class.getDeclaredField("value");
            assertThat(OpenModifier.isTransient(valueField)).isFalse();
        }
    }

    @Nested
    @DisplayName("isNative方法测试")
    class IsNativeTests {

        @Test
        @DisplayName("检查native修饰符")
        void testIsNativeModifiers() {
            assertThat(OpenModifier.isNative(Modifier.NATIVE)).isTrue();
            assertThat(OpenModifier.isNative(0)).isFalse();
        }

        @Test
        @DisplayName("检查native方法")
        void testIsNativeMethod() throws Exception {
            Method hashCodeMethod = Object.class.getDeclaredMethod("hashCode");
            assertThat(OpenModifier.isNative(hashCodeMethod)).isTrue();
        }
    }

    @Nested
    @DisplayName("isInterface方法测试")
    class IsInterfaceTests {

        @Test
        @DisplayName("检查interface修饰符")
        void testIsInterfaceModifiers() {
            assertThat(OpenModifier.isInterface(Modifier.INTERFACE)).isTrue();
            assertThat(OpenModifier.isInterface(0)).isFalse();
        }
    }

    @Nested
    @DisplayName("isStrict方法测试")
    class IsStrictTests {

        @Test
        @DisplayName("检查strictfp修饰符")
        void testIsStrictModifiers() {
            // strictfp is effectively ignored in modern Java
            assertThat(OpenModifier.isStrict(Modifier.STRICT)).isTrue();
            assertThat(OpenModifier.isStrict(0)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSynthetic方法测试")
    class IsSyntheticTests {

        @Test
        @DisplayName("检查synthetic方法")
        void testIsSyntheticMethod() throws Exception {
            Method lengthMethod = String.class.getMethod("length");
            assertThat(OpenModifier.isSynthetic(lengthMethod)).isFalse();
        }

        @Test
        @DisplayName("检查synthetic字段")
        void testIsSyntheticField() throws Exception {
            Field valueField = String.class.getDeclaredField("value");
            assertThat(OpenModifier.isSynthetic(valueField)).isFalse();
        }

        @Test
        @DisplayName("检查synthetic类")
        void testIsSyntheticClass() {
            assertThat(OpenModifier.isSynthetic(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("修饰符转字符串")
        void testToString() {
            String result = OpenModifier.toString(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
            assertThat(result).contains("public");
            assertThat(result).contains("static");
            assertThat(result).contains("final");
        }

        @Test
        @DisplayName("无修饰符转空字符串")
        void testToStringEmpty() {
            assertThat(OpenModifier.toString(0)).isEmpty();
        }
    }

    @Nested
    @DisplayName("toList方法测试")
    class ToListTests {

        @Test
        @DisplayName("修饰符转列表")
        void testToList() {
            List<String> result = OpenModifier.toList(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
            assertThat(result).contains("public", "static", "final");
        }

        @Test
        @DisplayName("无修饰符转空列表")
        void testToListEmpty() {
            assertThat(OpenModifier.toList(0)).isEmpty();
        }
    }

    @Nested
    @DisplayName("AccessLevel枚举测试")
    class AccessLevelTests {

        @Test
        @DisplayName("枚举值存在")
        void testEnumValues() {
            assertThat(OpenModifier.AccessLevel.values()).containsExactly(
                    OpenModifier.AccessLevel.PUBLIC,
                    OpenModifier.AccessLevel.PROTECTED,
                    OpenModifier.AccessLevel.PACKAGE_PRIVATE,
                    OpenModifier.AccessLevel.PRIVATE
            );
        }
    }

    @Nested
    @DisplayName("getAccessLevel方法测试")
    class GetAccessLevelTests {

        @Test
        @DisplayName("从修饰符获取访问级别")
        void testGetAccessLevelFromModifiers() {
            assertThat(OpenModifier.getAccessLevel(Modifier.PUBLIC)).isEqualTo(OpenModifier.AccessLevel.PUBLIC);
            assertThat(OpenModifier.getAccessLevel(Modifier.PROTECTED)).isEqualTo(OpenModifier.AccessLevel.PROTECTED);
            assertThat(OpenModifier.getAccessLevel(Modifier.PRIVATE)).isEqualTo(OpenModifier.AccessLevel.PRIVATE);
            assertThat(OpenModifier.getAccessLevel(0)).isEqualTo(OpenModifier.AccessLevel.PACKAGE_PRIVATE);
        }

        @Test
        @DisplayName("从成员获取访问级别")
        void testGetAccessLevelFromMember() throws Exception {
            Method publicMethod = String.class.getMethod("length");
            assertThat(OpenModifier.getAccessLevel(publicMethod)).isEqualTo(OpenModifier.AccessLevel.PUBLIC);
        }

        @Test
        @DisplayName("从类获取访问级别")
        void testGetAccessLevelFromClass() {
            assertThat(OpenModifier.getAccessLevel(String.class)).isEqualTo(OpenModifier.AccessLevel.PUBLIC);
        }
    }

    @Nested
    @DisplayName("isAccessAtLeast方法测试")
    class IsAccessAtLeastTests {

        @Test
        @DisplayName("检查最低访问级别")
        void testIsAccessAtLeast() {
            assertThat(OpenModifier.isAccessAtLeast(Modifier.PUBLIC, OpenModifier.AccessLevel.PROTECTED)).isTrue();
            assertThat(OpenModifier.isAccessAtLeast(Modifier.PUBLIC, OpenModifier.AccessLevel.PUBLIC)).isTrue();
            assertThat(OpenModifier.isAccessAtLeast(Modifier.PRIVATE, OpenModifier.AccessLevel.PUBLIC)).isFalse();
        }
    }

    @Nested
    @DisplayName("isOverridable方法测试")
    class IsOverridableTests {

        @Test
        @DisplayName("检查方法可被重写")
        void testIsOverridable() throws Exception {
            Method toStringMethod = Object.class.getMethod("toString");
            Method equalsMethod = Object.class.getMethod("equals", Object.class);

            assertThat(OpenModifier.isOverridable(toStringMethod)).isTrue();
            assertThat(OpenModifier.isOverridable(equalsMethod)).isTrue();
        }

        @Test
        @DisplayName("final方法不可被重写")
        void testFinalMethodNotOverridable() throws Exception {
            Method getClassMethod = Object.class.getMethod("getClass");
            // wait(long) is also final in Object class
            Method waitMethod = Object.class.getDeclaredMethod("wait", long.class);
            assertThat(OpenModifier.isOverridable(getClassMethod)).isFalse();
            assertThat(OpenModifier.isOverridable(waitMethod)).isFalse();
        }
    }

    @Nested
    @DisplayName("isExtendable方法测试")
    class IsExtendableTests {

        @Test
        @DisplayName("检查类可被继承")
        void testIsExtendable() {
            assertThat(OpenModifier.isExtendable(Object.class)).isTrue();
            assertThat(OpenModifier.isExtendable(Number.class)).isTrue();
        }

        @Test
        @DisplayName("final类不可被继承")
        void testFinalClassNotExtendable() {
            assertThat(OpenModifier.isExtendable(String.class)).isFalse();
        }

        @Test
        @DisplayName("接口不可被继承")
        void testInterfaceNotExtendable() {
            assertThat(OpenModifier.isExtendable(Runnable.class)).isFalse();
        }

        @Test
        @DisplayName("数组不可被继承")
        void testArrayNotExtendable() {
            assertThat(OpenModifier.isExtendable(String[].class)).isFalse();
        }

        @Test
        @DisplayName("原始类型不可被继承")
        void testPrimitiveNotExtendable() {
            assertThat(OpenModifier.isExtendable(int.class)).isFalse();
        }
    }
}
