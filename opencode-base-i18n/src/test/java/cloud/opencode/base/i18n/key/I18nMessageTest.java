package cloud.opencode.base.i18n.key;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for I18nMessage record
 */
@DisplayName("I18nMessage")
class I18nMessageTest {

    @Nested
    @DisplayName("of(key, locale, formatted)")
    class OfBasic {
        @Test void createsRecord() {
            I18nMessage msg = I18nMessage.of("user.welcome", Locale.ENGLISH, "Hello, Alice!");
            assertThat(msg.key()).isEqualTo("user.welcome");
            assertThat(msg.locale()).isEqualTo(Locale.ENGLISH);
            assertThat(msg.formatted()).isEqualTo("Hello, Alice!");
            assertThat(msg.params()).isEmpty();
        }

        @Test void toStringReturnsFormatted() {
            I18nMessage msg = I18nMessage.of("k", Locale.ENGLISH, "text");
            assertThat(msg.toString()).isEqualTo("text");
        }

        @Test void textAliasForFormatted() {
            I18nMessage msg = I18nMessage.of("k", Locale.ENGLISH, "text");
            assertThat(msg.text()).isEqualTo("text");
        }
    }

    @Nested
    @DisplayName("of(key, locale, formatted, params)")
    class OfWithParams {
        @Test void createsWithParams() {
            Map<String, Object> params = Map.of("name", "Bob");
            I18nMessage msg = I18nMessage.of("greeting", Locale.ENGLISH, "Hi, Bob!", params);
            assertThat(msg.params()).containsEntry("name", "Bob");
            assertThat(msg.hasParams()).isTrue();
        }

        @Test void paramsAreCopied() {
            java.util.HashMap<String, Object> original = new java.util.HashMap<>();
            original.put("x", "1");
            I18nMessage msg = I18nMessage.of("k", Locale.ENGLISH, "t", original);
            original.put("y", "2");  // mutate original
            assertThat(msg.params()).doesNotContainKey("y");
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {
        @Test void nullKeyThrows() {
            assertThatNullPointerException().isThrownBy(
                    () -> I18nMessage.of(null, Locale.ENGLISH, "text"));
        }
        @Test void blankKeyThrows() {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> I18nMessage.of("  ", Locale.ENGLISH, "text"));
        }
        @Test void nullLocaleThrows() {
            assertThatNullPointerException().isThrownBy(
                    () -> I18nMessage.of("key", null, "text"));
        }
        @Test void nullFormattedThrows() {
            assertThatNullPointerException().isThrownBy(
                    () -> I18nMessage.of("key", Locale.ENGLISH, null));
        }
    }

    @Nested
    @DisplayName("resolve()")
    class Resolve {
        @Test void resolvesAndCreatesMessage() {
            // No bundle configured: key returned as-is
            I18nMessage msg = I18nMessage.resolve("some.key", Locale.ENGLISH);
            assertThat(msg.key()).isEqualTo("some.key");
            assertThat(msg.locale()).isEqualTo(Locale.ENGLISH);
            assertThat(msg.formatted()).isNotNull();
        }
    }

    @Nested
    @DisplayName("hasParams()")
    class HasParams {
        @Test void withoutParams() {
            assertThat(I18nMessage.of("k", Locale.ENGLISH, "t").hasParams()).isFalse();
        }
        @Test void withParams() {
            assertThat(I18nMessage.of("k", Locale.ENGLISH, "t", Map.of("a", "b")).hasParams()).isTrue();
        }
    }
}
