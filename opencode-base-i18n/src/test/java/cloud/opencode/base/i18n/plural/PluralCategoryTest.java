package cloud.opencode.base.i18n.plural;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for PluralCategory
 */
@DisplayName("PluralCategory")
class PluralCategoryTest {

    @Nested
    @DisplayName("keyword()")
    class Keyword {
        @Test void zero()  { assertThat(PluralCategory.ZERO.keyword()).isEqualTo("zero"); }
        @Test void one()   { assertThat(PluralCategory.ONE.keyword()).isEqualTo("one"); }
        @Test void two()   { assertThat(PluralCategory.TWO.keyword()).isEqualTo("two"); }
        @Test void few()   { assertThat(PluralCategory.FEW.keyword()).isEqualTo("few"); }
        @Test void many()  { assertThat(PluralCategory.MANY.keyword()).isEqualTo("many"); }
        @Test void other() { assertThat(PluralCategory.OTHER.keyword()).isEqualTo("other"); }
    }

    @Nested
    @DisplayName("fromKeyword()")
    class FromKeyword {
        @Test void parsesZero()    { assertThat(PluralCategory.fromKeyword("zero")).isEqualTo(PluralCategory.ZERO); }
        @Test void parsesOne()     { assertThat(PluralCategory.fromKeyword("one")).isEqualTo(PluralCategory.ONE); }
        @Test void parsesOther()   { assertThat(PluralCategory.fromKeyword("other")).isEqualTo(PluralCategory.OTHER); }
        @Test void caseInsensitive() { assertThat(PluralCategory.fromKeyword("FEW")).isEqualTo(PluralCategory.FEW); }
        @Test void withWhitespace()  { assertThat(PluralCategory.fromKeyword(" many ")).isEqualTo(PluralCategory.MANY); }
        @Test void unknownThrows()   { assertThatIllegalArgumentException().isThrownBy(() -> PluralCategory.fromKeyword("bogus")); }
        @Test void nullThrows()      { assertThatNullPointerException().isThrownBy(() -> PluralCategory.fromKeyword(null)); }
    }
}
