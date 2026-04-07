package cloud.opencode.base.i18n.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for BundleValidationResult
 */
@DisplayName("BundleValidationResult")
class BundleValidationResultTest {

    @Nested
    @DisplayName("coverage()")
    class Coverage {
        @Test void fullCoverage() {
            BundleValidationResult r = new BundleValidationResult(
                    Locale.ENGLISH, Locale.FRENCH, Set.of(), Set.of(), 10, 10);
            assertThat(r.coverage()).isEqualTo(1.0);
        }

        @Test void partialCoverage() {
            BundleValidationResult r = new BundleValidationResult(
                    Locale.ENGLISH, Locale.FRENCH, Set.of("a", "b"), Set.of(), 10, 8);
            assertThat(r.coverage()).isEqualTo(0.8);
        }

        @Test void zeroCoverageWhenAllMissing() {
            Set<String> missing = Set.of("a", "b", "c");
            BundleValidationResult r = new BundleValidationResult(
                    Locale.ENGLISH, Locale.FRENCH, missing, Set.of(), 3, 0);
            assertThat(r.coverage()).isEqualTo(0.0);
        }

        @Test void zeroBaseKeyCountReturnsOne() {
            BundleValidationResult r = new BundleValidationResult(
                    Locale.ENGLISH, Locale.FRENCH, Set.of(), Set.of(), 0, 0);
            assertThat(r.coverage()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("isComplete()")
    class IsComplete {
        @Test void completeWhenNoMissingKeys() {
            BundleValidationResult r = new BundleValidationResult(
                    Locale.ENGLISH, Locale.FRENCH, Set.of(), Set.of(), 5, 5);
            assertThat(r.isComplete()).isTrue();
        }

        @Test void incompleteWhenMissingKeys() {
            BundleValidationResult r = new BundleValidationResult(
                    Locale.ENGLISH, Locale.FRENCH, Set.of("missing.key"), Set.of(), 5, 4);
            assertThat(r.isComplete()).isFalse();
        }
    }

    @Nested
    @DisplayName("summary()")
    class Summary {
        @Test void summaryContainsLocales() {
            BundleValidationResult r = new BundleValidationResult(
                    Locale.ENGLISH, Locale.FRENCH, Set.of("k"), Set.of(), 10, 9);
            assertThat(r.summary())
                    .contains("en")
                    .contains("fr")
                    .contains("90.0%")
                    .contains("missing=1");
        }
    }

    @Nested
    @DisplayName("Null safety")
    class NullSafety {
        @Test void nullMissingKeysBecomesEmpty() {
            BundleValidationResult r = new BundleValidationResult(
                    Locale.ENGLISH, Locale.FRENCH, null, null, 5, 5);
            assertThat(r.missingKeys()).isEmpty();
            assertThat(r.extraKeys()).isEmpty();
        }

        @Test void nullBaseLocaleThrows() {
            assertThatNullPointerException().isThrownBy(() ->
                    new BundleValidationResult(null, Locale.FRENCH, Set.of(), Set.of(), 0, 0));
        }
    }
}
