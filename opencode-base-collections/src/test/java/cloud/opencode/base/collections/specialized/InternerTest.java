package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Equivalence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * Interner 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("Interner 测试")
class InternerTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("strong - 创建强驻留器")
        void testStrong() {
            Interner<String> interner = Interner.strong();

            assertThat(interner).isNotNull();
        }

        @Test
        @DisplayName("weak - 创建弱驻留器")
        void testWeak() {
            Interner<String> interner = Interner.weak();

            assertThat(interner).isNotNull();
        }

        @Test
        @DisplayName("newBuilder - 创建构建器")
        void testNewBuilder() {
            Interner.Builder<String> builder = Interner.newBuilder();

            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("强驻留器测试")
    class StrongInternerTests {

        @Test
        @DisplayName("intern - 驻留相同值返回相同实例")
        void testInternSameValue() {
            Interner<String> interner = Interner.strong();

            String a = interner.intern(new String("hello"));
            String b = interner.intern(new String("hello"));

            assertThat(a).isSameAs(b);
        }

        @Test
        @DisplayName("intern - 驻留不同值返回不同实例")
        void testInternDifferentValues() {
            Interner<String> interner = Interner.strong();

            String a = interner.intern("hello");
            String b = interner.intern("world");

            assertThat(a).isNotSameAs(b);
        }

        @Test
        @DisplayName("intern - null参数抛异常")
        void testInternNull() {
            Interner<String> interner = Interner.strong();

            assertThatThrownBy(() -> interner.intern(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("intern - 保留所有对象")
        void testStrongKeepsAll() {
            Interner<String> interner = Interner.strong();

            for (int i = 0; i < 100; i++) {
                interner.intern("value-" + i);
            }

            // 强驻留器应该保留所有对象
            String first = interner.intern("value-0");
            assertThat(first).isEqualTo("value-0");
        }
    }

    @Nested
    @DisplayName("弱驻留器测试")
    class WeakInternerTests {

        @Test
        @DisplayName("intern - 驻留相同值返回相同实例")
        void testWeakInternSameValue() {
            Interner<String> interner = Interner.weak();

            String a = interner.intern("hello");
            String b = interner.intern("hello");

            assertThat(a).isSameAs(b);
        }

        @Test
        @DisplayName("intern - 驻留不同值返回不同实例")
        void testWeakInternDifferentValues() {
            Interner<String> interner = Interner.weak();

            String a = interner.intern("hello");
            String b = interner.intern("world");

            assertThat(a).isNotSameAs(b);
        }
    }

    @Nested
    @DisplayName("构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("equivalence - 设置等价策略")
        void testEquivalence() {
            Interner<String> interner = Interner.<String>newBuilder()
                    .equivalence(Equivalence.from(
                            String::equalsIgnoreCase,
                            s -> s.toLowerCase().hashCode()))
                    .strong()
                    .build();

            String a = interner.intern("Hello");
            String b = interner.intern("HELLO");

            // 使用自定义等价性，应该返回相同实例
            assertThat(a).isSameAs(b);
        }

        @Test
        @DisplayName("concurrencyLevel - 设置并发级别")
        void testConcurrencyLevel() {
            Interner<String> interner = Interner.<String>newBuilder()
                    .concurrencyLevel(16)
                    .strong()
                    .build();

            assertThat(interner).isNotNull();
        }

        @Test
        @DisplayName("concurrencyLevel - 无效值抛异常")
        void testConcurrencyLevelInvalid() {
            assertThatThrownBy(() -> Interner.newBuilder().concurrencyLevel(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("strong - 配置为强模式")
        void testBuilderStrong() {
            Interner<String> interner = Interner.<String>newBuilder()
                    .strong()
                    .build();

            String a = interner.intern(new String("test"));
            String b = interner.intern(new String("test"));

            assertThat(a).isSameAs(b);
        }

        @Test
        @DisplayName("weak - 配置为弱模式")
        void testBuilderWeak() {
            Interner<String> interner = Interner.<String>newBuilder()
                    .weak()
                    .build();

            String a = interner.intern("test");
            String b = interner.intern("test");

            assertThat(a).isSameAs(b);
        }
    }

    @Nested
    @DisplayName("工具方法测试")
    class UtilityMethodsTests {

        @Test
        @DisplayName("asFunction - 返回驻留函数")
        void testAsFunction() {
            Interner<String> interner = Interner.strong();
            Function<String, String> internFn = interner.asFunction();

            String a = internFn.apply(new String("hello"));
            String b = internFn.apply(new String("hello"));

            assertThat(a).isSameAs(b);
        }
    }

    @Nested
    @DisplayName("自定义对象驻留测试")
    class CustomObjectTests {

        record Person(String name, int age) {}

        @Test
        @DisplayName("驻留自定义对象")
        void testInternCustomObject() {
            Interner<Person> interner = Interner.strong();

            Person p1 = interner.intern(new Person("Alice", 30));
            Person p2 = interner.intern(new Person("Alice", 30));

            assertThat(p1).isSameAs(p2);
        }

        @Test
        @DisplayName("不同对象返回不同实例")
        void testInternDifferentCustomObjects() {
            Interner<Person> interner = Interner.strong();

            Person p1 = interner.intern(new Person("Alice", 30));
            Person p2 = interner.intern(new Person("Bob", 25));

            assertThat(p1).isNotSameAs(p2);
        }
    }
}
