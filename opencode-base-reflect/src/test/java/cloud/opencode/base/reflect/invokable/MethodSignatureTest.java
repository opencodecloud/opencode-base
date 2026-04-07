package cloud.opencode.base.reflect.invokable;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * MethodSignature Tests
 * MethodSignature 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.3
 */
@DisplayName("MethodSignature 测试")
class MethodSignatureTest {

    // ==================== Test helpers | 测试辅助 ====================

    @SuppressWarnings("unused")
    static class Parent {
        public Number compute(String input) { return 0; }
        public Object getValue() { return null; }
        public void process(int a, String b) {}
    }

    @SuppressWarnings("unused")
    static class Child extends Parent {
        @Override
        public Integer compute(String input) { return 1; }
        @Override
        public String getValue() { return ""; }
    }

    @SuppressWarnings("unused")
    static class Unrelated {
        public void compute(int x) {}
        public void process(String b, int a) {}
    }

    // ==================== Factory Method Tests | 工厂方法测试 ====================

    @Nested
    @DisplayName("of(Method) 工厂方法测试")
    class OfMethodTests {

        @Test
        @DisplayName("从Method创建签名")
        void testOfMethod() throws Exception {
            Method method = String.class.getMethod("substring", int.class, int.class);
            MethodSignature sig = MethodSignature.of(method);

            assertThat(sig.getName()).isEqualTo("substring");
            assertThat(sig.getParameterTypes()).containsExactly(int.class, int.class);
            assertThat(sig.getReturnType()).isEqualTo(String.class);
            assertThat(sig.getParameterCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("无参方法签名")
        void testOfMethodNoParams() throws Exception {
            Method method = String.class.getMethod("length");
            MethodSignature sig = MethodSignature.of(method);

            assertThat(sig.getName()).isEqualTo("length");
            assertThat(sig.getParameterTypes()).isEmpty();
            assertThat(sig.getReturnType()).isEqualTo(int.class);
            assertThat(sig.getParameterCount()).isZero();
        }

        @Test
        @DisplayName("null方法抛出异常")
        void testOfNullMethod() {
            assertThatNullPointerException()
                    .isThrownBy(() -> MethodSignature.of((Method) null));
        }
    }

    @Nested
    @DisplayName("of(String, Class...) 工厂方法测试")
    class OfNameParamsTests {

        @Test
        @DisplayName("从名称和参数类型创建签名")
        void testOfNameAndParams() {
            MethodSignature sig = MethodSignature.of("foo", String.class, int.class);

            assertThat(sig.getName()).isEqualTo("foo");
            assertThat(sig.getParameterTypes()).containsExactly(String.class, int.class);
            assertThat(sig.getReturnType()).isNull();
            assertThat(sig.getParameterCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("无参数类型")
        void testOfNameNoParams() {
            MethodSignature sig = MethodSignature.of("bar");

            assertThat(sig.getName()).isEqualTo("bar");
            assertThat(sig.getParameterTypes()).isEmpty();
            assertThat(sig.getParameterCount()).isZero();
        }

        @Test
        @DisplayName("null名称抛出异常")
        void testOfNullName() {
            assertThatThrownBy(() -> MethodSignature.of((String) null))
                    .isInstanceOf(OpenReflectException.class);
        }

        @Test
        @DisplayName("空名称抛出异常")
        void testOfEmptyName() {
            assertThatThrownBy(() -> MethodSignature.of(""))
                    .isInstanceOf(OpenReflectException.class);
        }

        @Test
        @DisplayName("参数类型中包含null抛出异常")
        void testOfNullParameterType() {
            Class<?>[] paramTypes = {String.class, null, int.class};
            assertThatThrownBy(() -> MethodSignature.of("foo", paramTypes))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("withReturnType(String, Class, Class...) 工厂方法测试")
    class WithReturnTypeTests {

        @Test
        @DisplayName("从名称、返回类型和参数类型创建签名")
        void testWithReturnTypeAndParams() {
            MethodSignature sig = MethodSignature.withReturnType("foo", String.class, int.class, double.class);

            assertThat(sig.getName()).isEqualTo("foo");
            assertThat(sig.getReturnType()).isEqualTo(String.class);
            assertThat(sig.getParameterTypes()).containsExactly(int.class, double.class);
        }

        @Test
        @DisplayName("返回类型为void")
        void testWithVoidReturn() {
            MethodSignature sig = MethodSignature.withReturnType("doSomething", void.class);

            assertThat(sig.getReturnType()).isEqualTo(void.class);
            assertThat(sig.getParameterTypes()).isEmpty();
        }
    }

    // ==================== Matching Tests | 匹配测试 ====================

    @Nested
    @DisplayName("matches(Method) 匹配测试")
    class MatchesMethodTests {

        @Test
        @DisplayName("匹配成功 - 名称和参数类型相同")
        void testMatchesPositive() throws Exception {
            Method method = Parent.class.getMethod("process", int.class, String.class);
            MethodSignature sig = MethodSignature.of("process", int.class, String.class);

            assertThat(sig.matches(method)).isTrue();
        }

        @Test
        @DisplayName("匹配失败 - 名称不同")
        void testMatchesDifferentName() throws Exception {
            Method method = String.class.getMethod("length");
            MethodSignature sig = MethodSignature.of("size");

            assertThat(sig.matches(method)).isFalse();
        }

        @Test
        @DisplayName("匹配失败 - 参数类型不同")
        void testMatchesDifferentParams() throws Exception {
            Method method = Parent.class.getMethod("compute", String.class);
            MethodSignature sig = MethodSignature.of("compute", int.class);

            assertThat(sig.matches(method)).isFalse();
        }

        @Test
        @DisplayName("匹配null方法返回false")
        void testMatchesNullMethod() {
            MethodSignature sig = MethodSignature.of("foo");
            assertThat(sig.matches((Method) null)).isFalse();
        }

        @Test
        @DisplayName("不同返回类型但名称和参数相同仍然匹配")
        void testMatchesIgnoresReturnType() throws Exception {
            Method method = Parent.class.getMethod("compute", String.class);
            // Signature with different return type
            MethodSignature sig = MethodSignature.withReturnType("compute", void.class, String.class);

            assertThat(sig.matches(method)).isTrue();
        }
    }

    @Nested
    @DisplayName("matches(MethodSignature) 匹配测试")
    class MatchesSignatureTests {

        @Test
        @DisplayName("相同签名匹配成功")
        void testMatchesPositive() {
            MethodSignature sig1 = MethodSignature.of("foo", String.class, int.class);
            MethodSignature sig2 = MethodSignature.of("foo", String.class, int.class);

            assertThat(sig1.matches(sig2)).isTrue();
        }

        @Test
        @DisplayName("不同签名匹配失败")
        void testMatchesNegative() {
            MethodSignature sig1 = MethodSignature.of("foo", String.class);
            MethodSignature sig2 = MethodSignature.of("bar", String.class);

            assertThat(sig1.matches(sig2)).isFalse();
        }

        @Test
        @DisplayName("null签名返回false")
        void testMatchesNull() {
            MethodSignature sig = MethodSignature.of("foo");
            assertThat(sig.matches((MethodSignature) null)).isFalse();
        }
    }

    // ==================== Override Detection Tests | 覆盖检测测试 ====================

    @Nested
    @DisplayName("isOverrideOf(Method) 覆盖检测测试")
    class IsOverrideOfTests {

        @Test
        @DisplayName("协变返回类型 - Integer覆盖Number")
        void testIsOverrideOfCovariantReturn() throws Exception {
            Method superMethod = Parent.class.getMethod("compute", String.class);
            MethodSignature childSig = MethodSignature.of(
                    Child.class.getMethod("compute", String.class));

            assertThat(childSig.isOverrideOf(superMethod)).isTrue();
        }

        @Test
        @DisplayName("协变返回类型 - String覆盖Object")
        void testIsOverrideOfStringOverridesObject() throws Exception {
            Method superMethod = Parent.class.getMethod("getValue");
            MethodSignature childSig = MethodSignature.of(
                    Child.class.getMethod("getValue"));

            assertThat(childSig.isOverrideOf(superMethod)).isTrue();
        }

        @Test
        @DisplayName("名称不匹配 - 非覆盖")
        void testIsOverrideOfDifferentName() throws Exception {
            Method superMethod = Parent.class.getMethod("compute", String.class);
            MethodSignature sig = MethodSignature.withReturnType("differentName", Number.class, String.class);

            assertThat(sig.isOverrideOf(superMethod)).isFalse();
        }

        @Test
        @DisplayName("参数不匹配 - 非覆盖")
        void testIsOverrideOfDifferentParams() throws Exception {
            Method superMethod = Parent.class.getMethod("compute", String.class);
            MethodSignature sig = MethodSignature.withReturnType("compute", Number.class, int.class);

            assertThat(sig.isOverrideOf(superMethod)).isFalse();
        }

        @Test
        @DisplayName("返回类型不协变 - 非覆盖")
        void testIsOverrideOfNonCovariantReturn() throws Exception {
            Method superMethod = Parent.class.getMethod("compute", String.class);
            // String is not assignable to Number
            MethodSignature sig = MethodSignature.withReturnType("compute", String.class, String.class);

            assertThat(sig.isOverrideOf(superMethod)).isFalse();
        }

        @Test
        @DisplayName("无返回类型的签名 - 只检查名称和参数")
        void testIsOverrideOfNoReturnType() throws Exception {
            Method superMethod = Parent.class.getMethod("compute", String.class);
            MethodSignature sig = MethodSignature.of("compute", String.class);

            assertThat(sig.isOverrideOf(superMethod)).isTrue();
        }

        @Test
        @DisplayName("null父方法返回false")
        void testIsOverrideOfNull() {
            MethodSignature sig = MethodSignature.of("foo");
            assertThat(sig.isOverrideOf(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isOverrideCompatible(MethodSignature) 覆盖兼容测试")
    class IsOverrideCompatibleTests {

        @Test
        @DisplayName("协变返回类型兼容")
        void testIsOverrideCompatibleCovariant() {
            MethodSignature superSig = MethodSignature.withReturnType("compute", Number.class, String.class);
            MethodSignature childSig = MethodSignature.withReturnType("compute", Integer.class, String.class);

            assertThat(childSig.isOverrideCompatible(superSig)).isTrue();
        }

        @Test
        @DisplayName("相同返回类型兼容")
        void testIsOverrideCompatibleSameReturn() {
            MethodSignature sig1 = MethodSignature.withReturnType("foo", String.class, int.class);
            MethodSignature sig2 = MethodSignature.withReturnType("foo", String.class, int.class);

            assertThat(sig1.isOverrideCompatible(sig2)).isTrue();
        }

        @Test
        @DisplayName("父签名无返回类型 - 兼容")
        void testIsOverrideCompatibleSuperNoReturn() {
            MethodSignature superSig = MethodSignature.of("foo", int.class);
            MethodSignature childSig = MethodSignature.withReturnType("foo", String.class, int.class);

            assertThat(childSig.isOverrideCompatible(superSig)).isTrue();
        }

        @Test
        @DisplayName("非协变返回类型 - 不兼容")
        void testIsOverrideCompatibleNonCovariant() {
            MethodSignature superSig = MethodSignature.withReturnType("compute", Integer.class, String.class);
            MethodSignature childSig = MethodSignature.withReturnType("compute", String.class, String.class);

            assertThat(childSig.isOverrideCompatible(superSig)).isFalse();
        }

        @Test
        @DisplayName("null签名返回false")
        void testIsOverrideCompatibleNull() {
            MethodSignature sig = MethodSignature.of("foo");
            assertThat(sig.isOverrideCompatible(null)).isFalse();
        }
    }

    // ==================== Descriptor Tests | 描述符测试 ====================

    @Nested
    @DisplayName("toDescriptor() JVM描述符测试")
    class ToDescriptorTests {

        @Test
        @DisplayName("基本类型参数和void返回")
        void testDescriptorPrimitivesAndVoid() {
            MethodSignature sig = MethodSignature.withReturnType("foo", void.class, int.class, boolean.class);
            assertThat(sig.toDescriptor()).isEqualTo("(IZ)V");
        }

        @Test
        @DisplayName("所有基本类型")
        void testDescriptorAllPrimitives() {
            MethodSignature sig = MethodSignature.withReturnType("bar", long.class,
                    boolean.class, byte.class, char.class, short.class,
                    int.class, long.class, float.class, double.class);
            assertThat(sig.toDescriptor()).isEqualTo("(ZBCSIJFD)J");
        }

        @Test
        @DisplayName("对象类型参数")
        void testDescriptorObjectType() {
            MethodSignature sig = MethodSignature.withReturnType("baz", String.class, String.class, int.class);
            assertThat(sig.toDescriptor()).isEqualTo("(Ljava/lang/String;I)Ljava/lang/String;");
        }

        @Test
        @DisplayName("数组类型参数")
        void testDescriptorArrayType() {
            MethodSignature sig = MethodSignature.withReturnType("arr", void.class, int[].class, String[].class);
            assertThat(sig.toDescriptor()).isEqualTo("([I[Ljava/lang/String;)V");
        }

        @Test
        @DisplayName("多维数组类型")
        void testDescriptorMultiDimArray() {
            MethodSignature sig = MethodSignature.withReturnType("multi", int[][].class);
            assertThat(sig.toDescriptor()).isEqualTo("()[[I");
        }

        @Test
        @DisplayName("无参无返回值")
        void testDescriptorNoParamsNoReturn() {
            MethodSignature sig = MethodSignature.of("empty");
            assertThat(sig.toDescriptor()).isEqualTo("()V");
        }

        @Test
        @DisplayName("未指定返回类型默认为void")
        void testDescriptorNullReturnDefaultsVoid() {
            MethodSignature sig = MethodSignature.of("test", String.class);
            // returnType is null (no return type specified), should default to void
            assertThat(sig.toDescriptor()).isEqualTo("(Ljava/lang/String;)V");
        }
    }

    // ==================== Readable String Tests | 可读字符串测试 ====================

    @Nested
    @DisplayName("toReadableString() 可读字符串测试")
    class ToReadableStringTests {

        @Test
        @DisplayName("有参数有返回类型")
        void testReadableStringWithParamsAndReturn() {
            MethodSignature sig = MethodSignature.withReturnType("foo", String.class, int.class, double.class);
            assertThat(sig.toReadableString()).isEqualTo("foo(int, double): String");
        }

        @Test
        @DisplayName("无参void返回")
        void testReadableStringNoParams() {
            MethodSignature sig = MethodSignature.withReturnType("bar", void.class);
            assertThat(sig.toReadableString()).isEqualTo("bar(): void");
        }

        @Test
        @DisplayName("未指定返回类型显示void")
        void testReadableStringNullReturn() {
            MethodSignature sig = MethodSignature.of("baz", String.class);
            assertThat(sig.toReadableString()).isEqualTo("baz(String): void");
        }

        @Test
        @DisplayName("数组类型参数")
        void testReadableStringArrayParam() {
            MethodSignature sig = MethodSignature.withReturnType("arr", void.class, int[].class);
            assertThat(sig.toReadableString()).isEqualTo("arr(int[]): void");
        }
    }

    // ==================== Equals/HashCode Tests | 相等/哈希码测试 ====================

    @Nested
    @DisplayName("equals/hashCode 测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同名称和参数类型相等")
        void testEqualsSameNameAndParams() {
            MethodSignature sig1 = MethodSignature.of("foo", String.class, int.class);
            MethodSignature sig2 = MethodSignature.of("foo", String.class, int.class);

            assertThat(sig1).isEqualTo(sig2);
            assertThat(sig1.hashCode()).isEqualTo(sig2.hashCode());
        }

        @Test
        @DisplayName("不同返回类型仍然相等 - Java重载规则")
        void testEqualsDifferentReturnTypeStillEqual() {
            MethodSignature sig1 = MethodSignature.withReturnType("foo", String.class, int.class);
            MethodSignature sig2 = MethodSignature.withReturnType("foo", Integer.class, int.class);

            assertThat(sig1).isEqualTo(sig2);
            assertThat(sig1.hashCode()).isEqualTo(sig2.hashCode());
        }

        @Test
        @DisplayName("不同名称不相等")
        void testNotEqualsDifferentName() {
            MethodSignature sig1 = MethodSignature.of("foo");
            MethodSignature sig2 = MethodSignature.of("bar");

            assertThat(sig1).isNotEqualTo(sig2);
        }

        @Test
        @DisplayName("不同参数类型不相等")
        void testNotEqualsDifferentParams() {
            MethodSignature sig1 = MethodSignature.of("foo", String.class);
            MethodSignature sig2 = MethodSignature.of("foo", int.class);

            assertThat(sig1).isNotEqualTo(sig2);
        }

        @Test
        @DisplayName("参数顺序不同不相等 - foo(int,String) ≠ foo(String,int)")
        void testParameterOrderMatters() {
            MethodSignature sig1 = MethodSignature.of("foo", int.class, String.class);
            MethodSignature sig2 = MethodSignature.of("foo", String.class, int.class);

            assertThat(sig1).isNotEqualTo(sig2);
        }

        @Test
        @DisplayName("与null不相等")
        void testNotEqualsNull() {
            MethodSignature sig = MethodSignature.of("foo");
            assertThat(sig).isNotEqualTo(null);
        }

        @Test
        @DisplayName("与其他类型不相等")
        void testNotEqualsOtherType() {
            MethodSignature sig = MethodSignature.of("foo");
            assertThat(sig).isNotEqualTo("foo");
        }

        @Test
        @DisplayName("自身相等")
        void testEqualsSelf() {
            MethodSignature sig = MethodSignature.of("foo", int.class);
            assertThat(sig).isEqualTo(sig);
        }
    }

    // ==================== toString Tests | toString测试 ====================

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString返回可读字符串")
        void testToString() {
            MethodSignature sig = MethodSignature.withReturnType("compute", Number.class, String.class);
            assertThat(sig.toString()).isEqualTo("compute(String): Number");
        }
    }

    // ==================== Defensive Copy Tests | 防御性复制测试 ====================

    @Nested
    @DisplayName("防御性复制测试")
    class DefensiveCopyTests {

        @Test
        @DisplayName("getParameterTypes返回防御性复制")
        void testGetParameterTypesDefensiveCopy() {
            MethodSignature sig = MethodSignature.of("foo", String.class, int.class);
            Class<?>[] params = sig.getParameterTypes();
            params[0] = Integer.class; // modify the returned array

            // Original should not be affected
            assertThat(sig.getParameterTypes()).containsExactly(String.class, int.class);
        }
    }
}
