package cloud.opencode.base.i18n.select;

import cloud.opencode.base.i18n.exception.OpenI18nException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for SelectFormatter
 */
@DisplayName("SelectFormatter")
class SelectFormatterTest {

    private final SelectFormatter fmt = new SelectFormatter();

    @Nested
    @DisplayName("Gender select")
    class GenderSelect {
        private static final String PATTERN = "male{He} female{She} other{They}";

        @Test void male()   { assertThat(fmt.format(PATTERN, "male")).isEqualTo("He"); }
        @Test void female() { assertThat(fmt.format(PATTERN, "female")).isEqualTo("She"); }
        @Test void unknown(){ assertThat(fmt.format(PATTERN, "nonbinary")).isEqualTo("They"); }
    }

    @Nested
    @DisplayName("Other fallback")
    class OtherFallback {
        @Test
        void fallsBackToOtherWhenNoMatch() {
            String result = fmt.format("cat{meow} dog{woof} other{silence}", "fish");
            assertThat(result).isEqualTo("silence");
        }
    }

    @Nested
    @DisplayName("Nested braces in branch")
    class NestedBraces {
        @Test
        void branchCanContainBraces() {
            String result = fmt.format("a{{nested}} other{plain}", "a");
            assertThat(result).isEqualTo("{nested}");
        }
    }

    @Nested
    @DisplayName("Error cases")
    class ErrorCases {
        @Test
        void noOtherBranchThrows() {
            assertThatExceptionOfType(OpenI18nException.class)
                    .isThrownBy(() -> fmt.format("cat{meow}", "dog"));
        }

        @Test
        void nullPatternThrows() {
            assertThatNullPointerException().isThrownBy(() -> fmt.format(null, "value"));
        }

        @Test
        void nullValueThrows() {
            assertThatNullPointerException().isThrownBy(() -> fmt.format("other{ok}", null));
        }
    }

    @Nested
    @DisplayName("Cache management")
    class Cache {
        @Test
        void clearCacheWorks() {
            fmt.format("other{ok}", "x");
            assertThatCode(() -> fmt.clearCache()).doesNotThrowAnyException();
            assertThat(fmt.format("other{ok}", "x")).isEqualTo("ok");
        }
    }
}
