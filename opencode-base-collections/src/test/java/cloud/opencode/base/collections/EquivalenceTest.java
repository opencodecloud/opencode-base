package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Equivalence 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("Equivalence 测试")
class EquivalenceTest {

    @Nested
    @DisplayName("equals 等价关系测试")
    class EqualsEquivalenceTests {

        @Test
        @DisplayName("equals - 基本等价检查")
        void testEquals() {
            Equivalence<String> eq = Equivalence.equals();

            assertThat(eq.equivalent("a", "a")).isTrue();
            assertThat(eq.equivalent("a", "b")).isFalse();
            assertThat(eq.equivalent("a", new String("a"))).isTrue();
        }

        @Test
        @DisplayName("equals - null 处理")
        void testEqualsNull() {
            Equivalence<String> eq = Equivalence.equals();

            assertThat(eq.equivalent(null, null)).isTrue();
            assertThat(eq.equivalent("a", null)).isFalse();
            assertThat(eq.equivalent(null, "a")).isFalse();
        }

        @Test
        @DisplayName("equals - hash")
        void testEqualsHash() {
            Equivalence<String> eq = Equivalence.equals();

            assertThat(eq.hash("abc")).isEqualTo("abc".hashCode());
            assertThat(eq.hash(null)).isZero();
        }
    }

    @Nested
    @DisplayName("identity 等价关系测试")
    class IdentityEquivalenceTests {

        @Test
        @DisplayName("identity - 同一对象")
        void testIdentitySameObject() {
            Equivalence<String> eq = Equivalence.identity();
            String s = "test";

            assertThat(eq.equivalent(s, s)).isTrue();
        }

        @Test
        @DisplayName("identity - 不同对象")
        void testIdentityDifferentObjects() {
            Equivalence<String> eq = Equivalence.identity();

            assertThat(eq.equivalent(new String("a"), new String("a"))).isFalse();
        }

        @Test
        @DisplayName("identity - null 处理")
        void testIdentityNull() {
            Equivalence<String> eq = Equivalence.identity();

            assertThat(eq.equivalent(null, null)).isTrue();
            assertThat(eq.equivalent("a", null)).isFalse();
            assertThat(eq.equivalent(null, "a")).isFalse();
        }

        @Test
        @DisplayName("identity - hash")
        void testIdentityHash() {
            Equivalence<String> eq = Equivalence.identity();
            String s = "test";

            assertThat(eq.hash(s)).isEqualTo(System.identityHashCode(s));
            assertThat(eq.hash(null)).isZero();
        }
    }

    @Nested
    @DisplayName("from 自定义等价关系测试")
    class FunctionalEquivalenceTests {

        @Test
        @DisplayName("from - 大小写不敏感等价")
        void testCaseInsensitive() {
            Equivalence<String> eq = Equivalence.from(
                    String::equalsIgnoreCase,
                    s -> s.toLowerCase().hashCode()
            );

            assertThat(eq.equivalent("Hello", "hello")).isTrue();
            assertThat(eq.equivalent("Hello", "HELLO")).isTrue();
            assertThat(eq.equivalent("Hello", "World")).isFalse();
        }

        @Test
        @DisplayName("from - hash 一致性")
        void testFunctionalHash() {
            Equivalence<String> eq = Equivalence.from(
                    String::equalsIgnoreCase,
                    s -> s.toLowerCase().hashCode()
            );

            assertThat(eq.hash("Hello")).isEqualTo(eq.hash("HELLO"));
            assertThat(eq.hash("Hello")).isEqualTo(eq.hash("hello"));
        }

        @Test
        @DisplayName("from - null 处理")
        void testFunctionalNull() {
            Equivalence<String> eq = Equivalence.from(
                    String::equalsIgnoreCase,
                    s -> s.toLowerCase().hashCode()
            );

            assertThat(eq.equivalent(null, null)).isTrue();
            assertThat(eq.equivalent("a", null)).isFalse();
            assertThat(eq.hash(null)).isZero();
        }

        @Test
        @DisplayName("from - null 参数抛异常")
        void testFunctionalNullParams() {
            assertThatThrownBy(() -> Equivalence.from(null, String::hashCode))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> Equivalence.from(String::equals, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("from - 按长度等价")
        void testByLength() {
            Equivalence<String> eq = Equivalence.from(
                    (a, b) -> a.length() == b.length(),
                    String::length
            );

            assertThat(eq.equivalent("abc", "xyz")).isTrue();
            assertThat(eq.equivalent("ab", "xyz")).isFalse();
            assertThat(eq.hash("abc")).isEqualTo(eq.hash("xyz"));
        }
    }
}
