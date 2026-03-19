package cloud.opencode.base.core.reflect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.*;

/**
 * ModifierUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("ModifierUtil 测试")
class ModifierUtilTest {

    // 测试用类
    public static class PublicClass {
        public String publicField;
        private String privateField;
        protected String protectedField;
        String packageField;
        static String staticField;
        final String finalField = "final";
        volatile int volatileField;
        transient String transientField;

        public void publicMethod() {}
        private void privateMethod() {}
        protected void protectedMethod() {}
        void packageMethod() {}
        static void staticMethod() {}
        synchronized void syncMethod() {}
        final void finalMethod() {}
    }

    private static class PrivateClass {}

    protected static class ProtectedClass {}

    static class PackageClass {}

    abstract static class AbstractClass {
        public abstract void abstractMethod();
        public native void nativeMethod();
    }

    interface TestInterface {
        void interfaceMethod();
    }

    final static class FinalClass {}

    @Nested
    @DisplayName("isPublic 测试")
    class IsPublicTests {

        @Test
        @DisplayName("isPublic int modifiers")
        void testIsPublicModifiers() {
            assertThat(ModifierUtil.isPublic(Modifier.PUBLIC)).isTrue();
            assertThat(ModifierUtil.isPublic(Modifier.PRIVATE)).isFalse();
            assertThat(ModifierUtil.isPublic(0)).isFalse();
        }

        @Test
        @DisplayName("isPublic Member")
        void testIsPublicMember() throws Exception {
            Field publicField = PublicClass.class.getDeclaredField("publicField");
            Field privateField = PublicClass.class.getDeclaredField("privateField");

            assertThat(ModifierUtil.isPublic(publicField)).isTrue();
            assertThat(ModifierUtil.isPublic(privateField)).isFalse();
        }

        @Test
        @DisplayName("isPublic Class")
        void testIsPublicClass() {
            assertThat(ModifierUtil.isPublic(PublicClass.class)).isTrue();
            assertThat(ModifierUtil.isPublic(PrivateClass.class)).isFalse();
        }

        @Test
        @DisplayName("isPublic Method")
        void testIsPublicMethod() throws Exception {
            Method publicMethod = PublicClass.class.getDeclaredMethod("publicMethod");
            Method privateMethod = PublicClass.class.getDeclaredMethod("privateMethod");

            assertThat(ModifierUtil.isPublic(publicMethod)).isTrue();
            assertThat(ModifierUtil.isPublic(privateMethod)).isFalse();
        }
    }

    @Nested
    @DisplayName("isPrivate 测试")
    class IsPrivateTests {

        @Test
        @DisplayName("isPrivate int modifiers")
        void testIsPrivateModifiers() {
            assertThat(ModifierUtil.isPrivate(Modifier.PRIVATE)).isTrue();
            assertThat(ModifierUtil.isPrivate(Modifier.PUBLIC)).isFalse();
        }

        @Test
        @DisplayName("isPrivate Member")
        void testIsPrivateMember() throws Exception {
            Field privateField = PublicClass.class.getDeclaredField("privateField");
            Field publicField = PublicClass.class.getDeclaredField("publicField");

            assertThat(ModifierUtil.isPrivate(privateField)).isTrue();
            assertThat(ModifierUtil.isPrivate(publicField)).isFalse();
        }
    }

    @Nested
    @DisplayName("isProtected 测试")
    class IsProtectedTests {

        @Test
        @DisplayName("isProtected int modifiers")
        void testIsProtectedModifiers() {
            assertThat(ModifierUtil.isProtected(Modifier.PROTECTED)).isTrue();
            assertThat(ModifierUtil.isProtected(Modifier.PUBLIC)).isFalse();
        }

        @Test
        @DisplayName("isProtected Member")
        void testIsProtectedMember() throws Exception {
            Field protectedField = PublicClass.class.getDeclaredField("protectedField");
            Field publicField = PublicClass.class.getDeclaredField("publicField");

            assertThat(ModifierUtil.isProtected(protectedField)).isTrue();
            assertThat(ModifierUtil.isProtected(publicField)).isFalse();
        }
    }

    @Nested
    @DisplayName("isStatic 测试")
    class IsStaticTests {

        @Test
        @DisplayName("isStatic int modifiers")
        void testIsStaticModifiers() {
            assertThat(ModifierUtil.isStatic(Modifier.STATIC)).isTrue();
            assertThat(ModifierUtil.isStatic(0)).isFalse();
        }

        @Test
        @DisplayName("isStatic Member - Field")
        void testIsStaticField() throws Exception {
            Field staticField = PublicClass.class.getDeclaredField("staticField");
            Field publicField = PublicClass.class.getDeclaredField("publicField");

            assertThat(ModifierUtil.isStatic(staticField)).isTrue();
            assertThat(ModifierUtil.isStatic(publicField)).isFalse();
        }

        @Test
        @DisplayName("isStatic Member - Method")
        void testIsStaticMethod() throws Exception {
            Method staticMethod = PublicClass.class.getDeclaredMethod("staticMethod");
            Method publicMethod = PublicClass.class.getDeclaredMethod("publicMethod");

            assertThat(ModifierUtil.isStatic(staticMethod)).isTrue();
            assertThat(ModifierUtil.isStatic(publicMethod)).isFalse();
        }
    }

    @Nested
    @DisplayName("isFinal 测试")
    class IsFinalTests {

        @Test
        @DisplayName("isFinal int modifiers")
        void testIsFinalModifiers() {
            assertThat(ModifierUtil.isFinal(Modifier.FINAL)).isTrue();
            assertThat(ModifierUtil.isFinal(0)).isFalse();
        }

        @Test
        @DisplayName("isFinal Member")
        void testIsFinalMember() throws Exception {
            Field finalField = PublicClass.class.getDeclaredField("finalField");
            Field publicField = PublicClass.class.getDeclaredField("publicField");

            assertThat(ModifierUtil.isFinal(finalField)).isTrue();
            assertThat(ModifierUtil.isFinal(publicField)).isFalse();
        }

        @Test
        @DisplayName("isFinal Class")
        void testIsFinalClass() {
            assertThat(ModifierUtil.isFinal(FinalClass.class)).isTrue();
            assertThat(ModifierUtil.isFinal(PublicClass.class)).isFalse();
        }

        @Test
        @DisplayName("isFinal Method")
        void testIsFinalMethod() throws Exception {
            Method finalMethod = PublicClass.class.getDeclaredMethod("finalMethod");
            Method publicMethod = PublicClass.class.getDeclaredMethod("publicMethod");

            assertThat(ModifierUtil.isFinal(finalMethod)).isTrue();
            assertThat(ModifierUtil.isFinal(publicMethod)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSynchronized 测试")
    class IsSynchronizedTests {

        @Test
        @DisplayName("isSynchronized int modifiers")
        void testIsSynchronizedModifiers() {
            assertThat(ModifierUtil.isSynchronized(Modifier.SYNCHRONIZED)).isTrue();
            assertThat(ModifierUtil.isSynchronized(0)).isFalse();
        }
    }

    @Nested
    @DisplayName("isVolatile 测试")
    class IsVolatileTests {

        @Test
        @DisplayName("isVolatile int modifiers")
        void testIsVolatileModifiers() {
            assertThat(ModifierUtil.isVolatile(Modifier.VOLATILE)).isTrue();
            assertThat(ModifierUtil.isVolatile(0)).isFalse();
        }
    }

    @Nested
    @DisplayName("isTransient 测试")
    class IsTransientTests {

        @Test
        @DisplayName("isTransient int modifiers")
        void testIsTransientModifiers() {
            assertThat(ModifierUtil.isTransient(Modifier.TRANSIENT)).isTrue();
            assertThat(ModifierUtil.isTransient(0)).isFalse();
        }
    }

    @Nested
    @DisplayName("isNative 测试")
    class IsNativeTests {

        @Test
        @DisplayName("isNative int modifiers")
        void testIsNativeModifiers() {
            assertThat(ModifierUtil.isNative(Modifier.NATIVE)).isTrue();
            assertThat(ModifierUtil.isNative(0)).isFalse();
        }
    }

    @Nested
    @DisplayName("isInterface 测试")
    class IsInterfaceTests {

        @Test
        @DisplayName("isInterface int modifiers")
        void testIsInterfaceModifiers() {
            assertThat(ModifierUtil.isInterface(Modifier.INTERFACE)).isTrue();
            assertThat(ModifierUtil.isInterface(0)).isFalse();
        }

        @Test
        @DisplayName("isInterface Class")
        void testIsInterfaceClass() {
            assertThat(ModifierUtil.isInterface(TestInterface.class)).isTrue();
            assertThat(ModifierUtil.isInterface(PublicClass.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isAbstract 测试")
    class IsAbstractTests {

        @Test
        @DisplayName("isAbstract int modifiers")
        void testIsAbstractModifiers() {
            assertThat(ModifierUtil.isAbstract(Modifier.ABSTRACT)).isTrue();
            assertThat(ModifierUtil.isAbstract(0)).isFalse();
        }

        @Test
        @DisplayName("isAbstract Class")
        void testIsAbstractClass() {
            assertThat(ModifierUtil.isAbstract(AbstractClass.class)).isTrue();
            assertThat(ModifierUtil.isAbstract(PublicClass.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isStrict 测试")
    class IsStrictTests {

        @Test
        @DisplayName("isStrict int modifiers")
        void testIsStrictModifiers() {
            assertThat(ModifierUtil.isStrict(Modifier.STRICT)).isTrue();
            assertThat(ModifierUtil.isStrict(0)).isFalse();
        }
    }

    @Nested
    @DisplayName("isPackagePrivate 测试")
    class IsPackagePrivateTests {

        @Test
        @DisplayName("isPackagePrivate int modifiers")
        void testIsPackagePrivateModifiers() {
            assertThat(ModifierUtil.isPackagePrivate(0)).isTrue();
            assertThat(ModifierUtil.isPackagePrivate(Modifier.PUBLIC)).isFalse();
            assertThat(ModifierUtil.isPackagePrivate(Modifier.PROTECTED)).isFalse();
            assertThat(ModifierUtil.isPackagePrivate(Modifier.PRIVATE)).isFalse();
        }

        @Test
        @DisplayName("isPackagePrivate Member")
        void testIsPackagePrivateMember() throws Exception {
            Field packageField = PublicClass.class.getDeclaredField("packageField");
            Field publicField = PublicClass.class.getDeclaredField("publicField");

            assertThat(ModifierUtil.isPackagePrivate(packageField)).isTrue();
            assertThat(ModifierUtil.isPackagePrivate(publicField)).isFalse();
        }
    }

    @Nested
    @DisplayName("toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString int modifiers")
        void testToStringModifiers() {
            int modifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
            String result = ModifierUtil.toString(modifiers);

            assertThat(result).contains("public");
            assertThat(result).contains("static");
            assertThat(result).contains("final");
        }

        @Test
        @DisplayName("toString Class")
        void testToStringClass() {
            String result = ModifierUtil.toString(PublicClass.class);
            assertThat(result).contains("public");
            assertThat(result).contains("static");
        }

        @Test
        @DisplayName("toString Member")
        void testToStringMember() throws Exception {
            Field publicField = PublicClass.class.getDeclaredField("publicField");
            String result = ModifierUtil.toString(publicField);
            assertThat(result).contains("public");
        }

        @Test
        @DisplayName("toString 空修饰符")
        void testToStringEmpty() {
            String result = ModifierUtil.toString(0);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("组合修饰符测试")
    class CombinedModifiersTests {

        @Test
        @DisplayName("多个修饰符组合")
        void testCombinedModifiers() {
            int modifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;

            assertThat(ModifierUtil.isPublic(modifiers)).isTrue();
            assertThat(ModifierUtil.isStatic(modifiers)).isTrue();
            assertThat(ModifierUtil.isFinal(modifiers)).isTrue();
            assertThat(ModifierUtil.isPrivate(modifiers)).isFalse();
        }

        @Test
        @DisplayName("实际字段的组合修饰符")
        void testRealFieldCombinedModifiers() throws Exception {
            Field staticField = PublicClass.class.getDeclaredField("staticField");
            int modifiers = staticField.getModifiers();

            assertThat(ModifierUtil.isStatic(modifiers)).isTrue();
            assertThat(ModifierUtil.isPackagePrivate(modifiers)).isTrue();
        }
    }
}
