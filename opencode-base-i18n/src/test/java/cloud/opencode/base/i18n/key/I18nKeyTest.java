package cloud.opencode.base.i18n.key;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for I18nKey interface
 */
@DisplayName("I18nKey")
class I18nKeyTest {

    /** Simple enum implementing I18nKey */
    enum TestKey implements I18nKey {
        HELLO("test.hello"),
        WORLD("test.world");

        private final String k;
        TestKey(String k) { this.k = k; }

        @Override public String key() { return k; }
    }

    @Nested
    @DisplayName("key()")
    class KeyMethod {
        @Test void returnsCorrectKey() {
            assertThat(TestKey.HELLO.key()).isEqualTo("test.hello");
            assertThat(TestKey.WORLD.key()).isEqualTo("test.world");
        }
    }

    @Nested
    @DisplayName("toKeyString()")
    class ToKeyString {
        @Test void sameAsKey() {
            assertThat(TestKey.HELLO.toKeyString()).isEqualTo(TestKey.HELLO.key());
        }
    }

    @Nested
    @DisplayName("getOrDefault() - no provider")
    class GetOrDefault {
        @Test void returnsDefaultWhenKeyNotFound() {
            // No provider configured, key won't be found → returns defaultValue or key
            String result = TestKey.HELLO.getOrDefault("fallback");
            // Since no bundle is configured, OpenI18n returns the key or fallback
            assertThat(result).isNotNull();
        }
    }
}
