package cloud.opencode.base.i18n.validation;

import cloud.opencode.base.i18n.spi.MessageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for BundleValidator using a simple manual MessageProvider stub
 */
@DisplayName("BundleValidator")
class BundleValidatorTest {

    /** Simple manual stub for MessageProvider */
    private static MessageProvider stubProvider(Map<Locale, Set<String>> localeKeys) {
        return new MessageProvider() {
            @Override
            public Optional<String> getMessageTemplate(String key, Locale locale) {
                Set<String> keys = localeKeys.getOrDefault(locale, Set.of());
                return keys.contains(key) ? Optional.of(key) : Optional.empty();
            }

            @Override
            public Set<String> getKeys(Locale locale) {
                return localeKeys.getOrDefault(locale, Set.of());
            }

            @Override
            public Set<Locale> getSupportedLocales() {
                return localeKeys.keySet();
            }
        };
    }

    @Nested
    @DisplayName("validate(base, target)")
    class Validate {
        @Test
        void detectsMissingKeys() {
            Map<Locale, Set<String>> data = new LinkedHashMap<>();
            data.put(Locale.ENGLISH, new LinkedHashSet<>(Set.of("a", "b", "c")));
            data.put(Locale.FRENCH,  new LinkedHashSet<>(Set.of("a")));

            BundleValidator validator = new BundleValidator(stubProvider(data));
            BundleValidationResult result = validator.validate(Locale.ENGLISH, Locale.FRENCH);

            assertThat(result.missingKeys()).containsExactlyInAnyOrder("b", "c");
            assertThat(result.extraKeys()).isEmpty();
            assertThat(result.coverage()).isEqualTo(1.0 / 3.0);
        }

        @Test
        void detectsExtraKeys() {
            Map<Locale, Set<String>> data = new LinkedHashMap<>();
            data.put(Locale.ENGLISH, Set.of("a"));
            data.put(Locale.FRENCH,  Set.of("a", "b", "c"));

            BundleValidator validator = new BundleValidator(stubProvider(data));
            BundleValidationResult result = validator.validate(Locale.ENGLISH, Locale.FRENCH);

            assertThat(result.missingKeys()).isEmpty();
            assertThat(result.extraKeys()).containsExactlyInAnyOrder("b", "c");
            assertThat(result.isComplete()).isTrue();
        }

        @Test
        void perfectCoverage() {
            Map<Locale, Set<String>> data = new LinkedHashMap<>();
            Set<String> keys = Set.of("a", "b", "c");
            data.put(Locale.ENGLISH, keys);
            data.put(Locale.FRENCH,  keys);

            BundleValidator validator = new BundleValidator(stubProvider(data));
            assertThat(validator.isComplete(Locale.ENGLISH, Locale.FRENCH)).isTrue();
            assertThat(validator.coverage(Locale.ENGLISH, Locale.FRENCH)).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("validateAll()")
    class ValidateAll {
        @Test
        void validatesAllSupportedLocales() {
            Map<Locale, Set<String>> data = new LinkedHashMap<>();
            data.put(Locale.ENGLISH,  Set.of("a", "b", "c"));
            data.put(Locale.FRENCH,   Set.of("a", "b"));
            data.put(Locale.GERMAN,   Set.of("a", "b", "c"));

            BundleValidator validator = new BundleValidator(stubProvider(data));
            Map<Locale, BundleValidationResult> results = validator.validateAll(Locale.ENGLISH);

            assertThat(results).containsKeys(Locale.FRENCH, Locale.GERMAN);
            assertThat(results).doesNotContainKey(Locale.ENGLISH); // base excluded
            assertThat(results.get(Locale.FRENCH).missingKeys()).containsExactly("c");
            assertThat(results.get(Locale.GERMAN).isComplete()).isTrue();
        }
    }

    @Nested
    @DisplayName("Null handling")
    class NullHandling {
        @Test void nullProviderThrows() {
            assertThatNullPointerException().isThrownBy(() -> new BundleValidator(null));
        }
        @Test void nullBaseLocaleThrows() {
            BundleValidator v = new BundleValidator(stubProvider(Map.of()));
            assertThatNullPointerException().isThrownBy(() -> v.validate(null, Locale.FRENCH));
        }
    }
}
