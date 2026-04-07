package cloud.opencode.base.functional.monad;

import cloud.opencode.base.functional.exception.OpenFunctionalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for enhanced Validation methods: mapError, peek, getOrElseThrow, toTry, toOption, stream.
 */
@DisplayName("Validation Enhanced Methods")
class ValidationEnhancedTest {

    // ==================== mapError ====================

    @Nested
    @DisplayName("mapError")
    class MapErrorTest {

        @Test
        @DisplayName("Valid.mapError returns same Valid unchanged")
        void validMapErrorReturnsSameValid() {
            Validation<String, Integer> valid = Validation.valid(42);
            Validation<Integer, Integer> result = valid.mapError(String::length);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getValue()).hasValue(42);
        }

        @Test
        @DisplayName("Invalid.mapError transforms each error")
        void invalidMapErrorTransformsErrors() {
            Validation<String, Integer> invalid = Validation.invalid(List.of("abc", "de"));
            Validation<Integer, Integer> result = invalid.mapError(String::length);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).containsExactly(3, 2);
        }

        @Test
        @DisplayName("Invalid.mapError with single error")
        void invalidMapErrorSingleError() {
            Validation<String, Integer> invalid = Validation.invalid("error");
            Validation<String, Integer> result = invalid.mapError(String::toUpperCase);

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).containsExactly("ERROR");
        }

        @Test
        @DisplayName("Invalid.mapError to different type")
        void invalidMapErrorToDifferentType() {
            Validation<String, Integer> invalid = Validation.invalid("fail");

            record ErrorCode(int code, String msg) {}
            Validation<ErrorCode, Integer> result = invalid.mapError(s -> new ErrorCode(400, s));

            assertThat(result.isInvalid()).isTrue();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().getFirst().code()).isEqualTo(400);
            assertThat(result.getErrors().getFirst().msg()).isEqualTo("fail");
        }
    }

    // ==================== peek ====================

    @Nested
    @DisplayName("peek")
    class PeekTest {

        @Test
        @DisplayName("Valid.peek executes action and returns same instance")
        void validPeekExecutesAction() {
            Validation<String, Integer> valid = Validation.valid(42);
            int[] holder = {0};

            Validation<String, Integer> result = valid.peek(v -> holder[0] = v);

            assertThat(result).isSameAs(valid);
            assertThat(holder[0]).isEqualTo(42);
        }

        @Test
        @DisplayName("Invalid.peek does not execute action")
        void invalidPeekSkipsAction() {
            Validation<String, Integer> invalid = Validation.invalid("err");
            int[] holder = {0};

            Validation<String, Integer> result = invalid.peek(v -> holder[0] = v);

            assertThat(result).isSameAs(invalid);
            assertThat(holder[0]).isEqualTo(0);
        }

        @Test
        @DisplayName("peek with null action throws NullPointerException")
        void peekNullActionThrows() {
            Validation<String, Integer> valid = Validation.valid(42);

            assertThatNullPointerException()
                    .isThrownBy(() -> valid.peek(null))
                    .withMessageContaining("action must not be null");
        }

        @Test
        @DisplayName("peek chains correctly")
        void peekChainsCorrectly() {
            StringBuilder sb = new StringBuilder();

            String result = Validation.<String, String>valid("hello")
                    .peek(v -> sb.append("peeked:").append(v))
                    .map(String::toUpperCase)
                    .getValue()
                    .orElse("");

            assertThat(sb.toString()).isEqualTo("peeked:hello");
            assertThat(result).isEqualTo("HELLO");
        }
    }

    // ==================== getOrElseThrow ====================

    @Nested
    @DisplayName("getOrElseThrow")
    class GetOrElseThrowTest {

        @Test
        @DisplayName("Valid.getOrElseThrow returns value")
        void validReturnsValue() {
            Validation<String, Integer> valid = Validation.valid(42);

            int result = valid.getOrElseThrow(
                    errors -> new IllegalArgumentException("fail: " + errors));

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("Valid with null value returns null")
        void validWithNullReturnsNull() {
            Validation<String, String> valid = Validation.valid(null);

            String result = valid.getOrElseThrow(
                    errors -> new IllegalArgumentException("fail"));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Invalid.getOrElseThrow throws mapped exception")
        void invalidThrowsMappedException() {
            Validation<String, Integer> invalid = Validation.invalid(List.of("err1", "err2"));

            assertThatThrownBy(() -> invalid.getOrElseThrow(
                    errors -> new IllegalArgumentException("Errors: " + errors)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("err1")
                    .hasMessageContaining("err2");
        }

        @Test
        @DisplayName("getOrElseThrow with null mapper throws NullPointerException")
        void nullMapperThrows() {
            Validation<String, Integer> valid = Validation.valid(42);

            assertThatNullPointerException()
                    .isThrownBy(() -> valid.getOrElseThrow(null))
                    .withMessageContaining("exceptionMapper must not be null");
        }
    }

    // ==================== toTry ====================

    @Nested
    @DisplayName("toTry")
    class ToTryTest {

        @Test
        @DisplayName("Valid.toTry returns Try.success")
        void validToTryReturnsSuccess() {
            Validation<String, Integer> valid = Validation.valid(42);

            Try<Integer> result = valid.toTry();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("Valid with null value returns Try.success(null)")
        void validNullToTryReturnsSuccessNull() {
            Validation<String, String> valid = Validation.valid(null);

            Try<String> result = valid.toTry();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isNull();
        }

        @Test
        @DisplayName("Invalid.toTry returns Try.failure with OpenFunctionalException")
        void invalidToTryReturnsFailure() {
            Validation<String, Integer> invalid = Validation.invalid(List.of("err1", "err2"));

            Try<Integer> result = invalid.toTry();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause()).isPresent();
            assertThat(result.getCause().get())
                    .isInstanceOf(OpenFunctionalException.class)
                    .hasMessageContaining("err1")
                    .hasMessageContaining("err2");
        }
    }

    // ==================== toOption ====================

    @Nested
    @DisplayName("toOption")
    class ToOptionTest {

        @Test
        @DisplayName("Valid.toOption returns Option.some")
        void validToOptionReturnsSome() {
            Validation<String, Integer> valid = Validation.valid(42);

            Option<Integer> result = valid.toOption();

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("Valid with null value returns Option.none")
        void validNullToOptionReturnsNone() {
            Validation<String, String> valid = Validation.valid(null);

            Option<String> result = valid.toOption();

            // Option.of(null) returns none
            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("Invalid.toOption returns Option.none")
        void invalidToOptionReturnsNone() {
            Validation<String, Integer> invalid = Validation.invalid("err");

            Option<Integer> result = invalid.toOption();

            assertThat(result.isNone()).isTrue();
        }
    }

    // ==================== stream ====================

    @Nested
    @DisplayName("stream")
    class StreamTest {

        @Test
        @DisplayName("Valid.stream returns single-element stream")
        void validStreamReturnsSingleElement() {
            Validation<String, Integer> valid = Validation.valid(42);

            Stream<Integer> result = valid.stream();

            assertThat(result.toList()).containsExactly(42);
        }

        @Test
        @DisplayName("Valid with null value returns empty stream")
        void validNullStreamReturnsEmpty() {
            Validation<String, String> valid = Validation.valid(null);

            Stream<String> result = valid.stream();

            assertThat(result.toList()).isEmpty();
        }

        @Test
        @DisplayName("Invalid.stream returns empty stream")
        void invalidStreamReturnsEmpty() {
            Validation<String, Integer> invalid = Validation.invalid("err");

            Stream<Integer> result = invalid.stream();

            assertThat(result.toList()).isEmpty();
        }

        @Test
        @DisplayName("stream integrates with Stream API")
        void streamIntegratesWithStreamApi() {
            List<Validation<String, Integer>> validations = List.of(
                    Validation.valid(1),
                    Validation.invalid("err"),
                    Validation.valid(2),
                    Validation.valid(3)
            );

            List<Integer> values = validations.stream()
                    .flatMap(Validation::stream)
                    .toList();

            assertThat(values).containsExactly(1, 2, 3);
        }
    }

    // ==================== Integration ====================

    @Nested
    @DisplayName("Integration")
    class IntegrationTest {

        @Test
        @DisplayName("mapError then toTry on Invalid")
        void mapErrorThenToTry() {
            Validation<String, Integer> invalid = Validation.invalid("bad input");

            Try<Integer> result = invalid
                    .mapError(String::toUpperCase)
                    .toTry();

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("peek then toOption on Valid")
        void peekThenToOption() {
            boolean[] peeked = {false};

            Option<Integer> result = Validation.<String, Integer>valid(42)
                    .peek(v -> peeked[0] = true)
                    .toOption();

            assertThat(peeked[0]).isTrue();
            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("full chain: map, peek, mapError, stream")
        void fullChain() {
            Validation<String, String> valid = Validation.valid("hello");

            List<String> result = valid
                    .map(String::toUpperCase)
                    .peek(v -> assertThat(v).isEqualTo("HELLO"))
                    .stream()
                    .toList();

            assertThat(result).containsExactly("HELLO");
        }
    }
}
