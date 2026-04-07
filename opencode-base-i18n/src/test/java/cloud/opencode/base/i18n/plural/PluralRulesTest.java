package cloud.opencode.base.i18n.plural;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for PluralRules - CLDR plural rule correctness
 */
@DisplayName("PluralRules")
class PluralRulesTest {

    @Nested
    @DisplayName("English (one/other)")
    class English {
        private final PluralRules rules = PluralRules.forLocale(Locale.ENGLISH);

        @Test void one()  { assertThat(rules.select(1L)).isEqualTo(PluralCategory.ONE); }
        @Test void two()  { assertThat(rules.select(2L)).isEqualTo(PluralCategory.OTHER); }
        @Test void zero() { assertThat(rules.select(0L)).isEqualTo(PluralCategory.OTHER); }
        @Test void large(){ assertThat(rules.select(100L)).isEqualTo(PluralCategory.OTHER); }
        @Test void neg()  { assertThat(rules.select(-1L)).isEqualTo(PluralCategory.ONE); }
    }

    @Nested
    @DisplayName("German (one/other)")
    class German {
        private final PluralRules rules = PluralRules.forLocale(Locale.GERMAN);

        @Test void one()   { assertThat(rules.select(1L)).isEqualTo(PluralCategory.ONE); }
        @Test void other() { assertThat(rules.select(2L)).isEqualTo(PluralCategory.OTHER); }
    }

    @Nested
    @DisplayName("French (0,1 → one)")
    class French {
        private final PluralRules rules = PluralRules.forLocale(Locale.FRENCH);

        @Test void zero()  { assertThat(rules.select(0L)).isEqualTo(PluralCategory.ONE); }
        @Test void one()   { assertThat(rules.select(1L)).isEqualTo(PluralCategory.ONE); }
        @Test void two()   { assertThat(rules.select(2L)).isEqualTo(PluralCategory.OTHER); }
        @Test void large() { assertThat(rules.select(100L)).isEqualTo(PluralCategory.OTHER); }
    }

    @Nested
    @DisplayName("Chinese (always other)")
    class Chinese {
        private final PluralRules rules = PluralRules.forLocale(Locale.CHINESE);

        @Test void zero()  { assertThat(rules.select(0L)).isEqualTo(PluralCategory.OTHER); }
        @Test void one()   { assertThat(rules.select(1L)).isEqualTo(PluralCategory.OTHER); }
        @Test void large() { assertThat(rules.select(999L)).isEqualTo(PluralCategory.OTHER); }
    }

    @Nested
    @DisplayName("Japanese (always other)")
    class Japanese {
        @Test void alwaysOther() {
            PluralRules rules = PluralRules.forLocale(Locale.JAPANESE);
            assertThat(rules.select(1L)).isEqualTo(PluralCategory.OTHER);
            assertThat(rules.select(10L)).isEqualTo(PluralCategory.OTHER);
        }
    }

    @Nested
    @DisplayName("Russian (one/few/many)")
    class Russian {
        private final PluralRules rules = PluralRules.forLocale(Locale.of("ru"));

        @Test void one()   { assertThat(rules.select(1L)).isEqualTo(PluralCategory.ONE); }
        @Test void few2()  { assertThat(rules.select(2L)).isEqualTo(PluralCategory.FEW); }
        @Test void few4()  { assertThat(rules.select(4L)).isEqualTo(PluralCategory.FEW); }
        @Test void many5() { assertThat(rules.select(5L)).isEqualTo(PluralCategory.MANY); }
        @Test void many11(){ assertThat(rules.select(11L)).isEqualTo(PluralCategory.MANY); }
        @Test void many12(){ assertThat(rules.select(12L)).isEqualTo(PluralCategory.MANY); }
        @Test void one21() { assertThat(rules.select(21L)).isEqualTo(PluralCategory.ONE); }
        @Test void few22() { assertThat(rules.select(22L)).isEqualTo(PluralCategory.FEW); }
        @Test void many25(){ assertThat(rules.select(25L)).isEqualTo(PluralCategory.MANY); }
        @Test void many100(){ assertThat(rules.select(100L)).isEqualTo(PluralCategory.MANY); }
        @Test void one101(){ assertThat(rules.select(101L)).isEqualTo(PluralCategory.ONE); }
    }

    @Nested
    @DisplayName("Arabic (zero/one/two/few/many/other)")
    class Arabic {
        private final PluralRules rules = PluralRules.forLocale(Locale.of("ar"));

        @Test void zero()  { assertThat(rules.select(0L)).isEqualTo(PluralCategory.ZERO); }
        @Test void one()   { assertThat(rules.select(1L)).isEqualTo(PluralCategory.ONE); }
        @Test void two()   { assertThat(rules.select(2L)).isEqualTo(PluralCategory.TWO); }
        @Test void few3()  { assertThat(rules.select(3L)).isEqualTo(PluralCategory.FEW); }
        @Test void few10() { assertThat(rules.select(10L)).isEqualTo(PluralCategory.FEW); }
        @Test void many11(){ assertThat(rules.select(11L)).isEqualTo(PluralCategory.MANY); }
        @Test void many99(){ assertThat(rules.select(99L)).isEqualTo(PluralCategory.MANY); }
        @Test void other100(){ assertThat(rules.select(100L)).isEqualTo(PluralCategory.OTHER); }
        @Test void other200(){ assertThat(rules.select(200L)).isEqualTo(PluralCategory.OTHER); }
        @Test void few103(){ assertThat(rules.select(103L)).isEqualTo(PluralCategory.FEW); }
    }

    @Nested
    @DisplayName("Welsh (zero/one/two/few/many/other)")
    class Welsh {
        private final PluralRules rules = PluralRules.forLocale(Locale.of("cy"));

        @Test void zero() { assertThat(rules.select(0L)).isEqualTo(PluralCategory.ZERO); }
        @Test void one()  { assertThat(rules.select(1L)).isEqualTo(PluralCategory.ONE); }
        @Test void two()  { assertThat(rules.select(2L)).isEqualTo(PluralCategory.TWO); }
        @Test void few()  { assertThat(rules.select(3L)).isEqualTo(PluralCategory.FEW); }
        @Test void many() { assertThat(rules.select(6L)).isEqualTo(PluralCategory.MANY); }
        @Test void other(){ assertThat(rules.select(7L)).isEqualTo(PluralCategory.OTHER); }
    }

    @Nested
    @DisplayName("null locale → default rules")
    class NullLocale {
        @Test void nullLocaleUsesDefault() {
            PluralRules rules = PluralRules.forLocale(null);
            assertThat(rules.select(1L)).isEqualTo(PluralCategory.ONE);
            assertThat(rules.select(2L)).isEqualTo(PluralCategory.OTHER);
        }
    }

    @Nested
    @DisplayName("decimal select")
    class DecimalSelect {
        @Test void decimalWithFractionsIsOther() {
            PluralRules rules = PluralRules.forLocale(Locale.ENGLISH);
            assertThat(rules.select(1.5, 1)).isEqualTo(PluralCategory.OTHER);
        }
        @Test void decimalWholeNumber() {
            PluralRules rules = PluralRules.forLocale(Locale.ENGLISH);
            assertThat(rules.select(1.0, 0)).isEqualTo(PluralCategory.ONE);
        }
    }
}
