package cloud.opencode.base.functional.monad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for enhanced Option methods
 * Option 增强方法测试
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("Option Enhanced Methods | Option 增强方法测试")
class OptionEnhancedTest {

    @Nested
    @DisplayName("contains")
    class ContainsTests {

        @Test
        @DisplayName("Some with matching value returns true")
        void someMatching() {
            assertThat(Option.some("hello").contains("hello")).isTrue();
        }

        @Test
        @DisplayName("Some with different value returns false")
        void someDifferent() {
            assertThat(Option.some("hello").contains("world")).isFalse();
        }

        @Test
        @DisplayName("None returns false")
        void noneReturnsFalse() {
            assertThat(Option.<String>none().contains("hello")).isFalse();
        }
    }

    @Nested
    @DisplayName("exists")
    class ExistsTests {

        @Test
        @DisplayName("Some with matching predicate returns true")
        void someMatching() {
            assertThat(Option.some(42).exists(v -> v > 40)).isTrue();
        }

        @Test
        @DisplayName("Some with non-matching predicate returns false")
        void someNonMatching() {
            assertThat(Option.some(42).exists(v -> v > 100)).isFalse();
        }

        @Test
        @DisplayName("None returns false")
        void noneReturnsFalse() {
            assertThat(Option.<Integer>none().exists(v -> true)).isFalse();
        }
    }

    @Nested
    @DisplayName("forAll")
    class ForAllTests {

        @Test
        @DisplayName("Some with matching predicate returns true")
        void someMatching() {
            assertThat(Option.some(42).forAll(v -> v > 40)).isTrue();
        }

        @Test
        @DisplayName("Some with non-matching predicate returns false")
        void someNonMatching() {
            assertThat(Option.some(42).forAll(v -> v > 100)).isFalse();
        }

        @Test
        @DisplayName("None returns true (vacuously)")
        void noneReturnsTrue() {
            assertThat(Option.<Integer>none().forAll(v -> false)).isTrue();
        }
    }

    @Nested
    @DisplayName("toTry")
    class ToTryTests {

        @Test
        @DisplayName("Some returns Success")
        void someReturnsSuccess() {
            Try<Integer> result = Option.some(42).toTry(() -> new NoSuchElementException("missing"));
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("None returns Failure with supplied exception")
        void noneReturnsFailure() {
            IOException expected = new IOException("missing");
            Try<Integer> result = Option.<Integer>none().toTry(() -> expected);
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause()).hasValue(expected);
        }
    }

    @Nested
    @DisplayName("stream")
    class StreamTests {

        @Test
        @DisplayName("Some returns single-element stream")
        void someStream() {
            List<Integer> list = Option.some(42).stream().toList();
            assertThat(list).containsExactly(42);
        }

        @Test
        @DisplayName("None returns empty stream")
        void noneStream() {
            List<Integer> list = Option.<Integer>none().stream().toList();
            assertThat(list).isEmpty();
        }
    }

    @Nested
    @DisplayName("zip")
    class ZipTests {

        @Test
        @DisplayName("Both Some returns combined Some")
        void bothSome() {
            Option<String> result = Option.some(1).zip(Option.some("a"), (i, s) -> i + s);
            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo("1a");
        }

        @Test
        @DisplayName("First None returns None")
        void firstNone() {
            Option<String> result = Option.<Integer>none().zip(Option.some("a"), (i, s) -> i + s);
            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("Second None returns None")
        void secondNone() {
            Option<String> result = Option.some(1).zip(Option.<String>none(), (i, s) -> i + s);
            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("Both None returns None")
        void bothNone() {
            Option<String> result = Option.<Integer>none().zip(Option.<String>none(), (i, s) -> i + s);
            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("Zipper returning null produces None")
        void zipperReturnsNull() {
            Option<String> result = Option.some(1).zip(Option.some("a"), (i, s) -> null);
            assertThat(result.isNone()).isTrue();
        }
    }

    @Nested
    @DisplayName("toValidation")
    class ToValidationTests {

        @Test
        @DisplayName("Some returns Valid")
        void someReturnsValid() {
            Validation<String, Integer> v = Option.some(42).toValidation("missing");
            assertThat(v.isValid()).isTrue();
            assertThat(v.getValue()).hasValue(42);
        }

        @Test
        @DisplayName("None returns Invalid with error")
        void noneReturnsInvalid() {
            Validation<String, Integer> v = Option.<Integer>none().toValidation("missing");
            assertThat(v.isInvalid()).isTrue();
            assertThat(v.getErrors()).containsExactly("missing");
        }
    }
}
