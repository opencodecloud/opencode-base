package cloud.opencode.base.i18n.plural;

import cloud.opencode.base.i18n.exception.OpenI18nException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for PluralFormatter
 */
@DisplayName("PluralFormatter")
class PluralFormatterTest {

    private final PluralFormatter fmt = new PluralFormatter();

    @Nested
    @DisplayName("English plural")
    class EnglishPlural {
        @Test
        void singularMatchesOne() {
            String result = fmt.selectBranch("one{1 file} other{# files}", 1L, Locale.ENGLISH);
            assertThat(result).isEqualTo("1 file");
        }

        @Test
        void pluralMatchesOther() {
            String result = fmt.selectBranch("one{1 file} other{# files}", 5L, Locale.ENGLISH);
            assertThat(result).isEqualTo("5 files");
        }

        @Test
        void hashIsReplacedWithCount() {
            String result = fmt.selectBranch("one{1 item} other{# items}", 42L, Locale.ENGLISH);
            assertThat(result).isEqualTo("42 items");
        }
    }

    @Nested
    @DisplayName("Exact match (=N)")
    class ExactMatch {
        @Test
        void exactZeroTakesPriorityOverCategory() {
            String result = fmt.selectBranch("=0{no files} one{1 file} other{# files}",
                    0L, Locale.ENGLISH);
            assertThat(result).isEqualTo("no files");
        }

        @Test
        void exactOneTakesPriorityOverCategory() {
            String result = fmt.selectBranch("=1{just one} other{# items}", 1L, Locale.ENGLISH);
            assertThat(result).isEqualTo("just one");
        }

        @Test
        void fallsBackToCategoryWhenNoExactMatch() {
            String result = fmt.selectBranch("=0{none} one{single} other{# many}", 2L, Locale.ENGLISH);
            assertThat(result).isEqualTo("2 many");
        }
    }

    @Nested
    @DisplayName("Russian plural (one/few/many)")
    class RussianPlural {
        private final Locale RU = Locale.of("ru");

        @Test void one()  { assertThat(fmt.selectBranch("one{# файл} few{# файла} many{# файлов} other{# файла}", 1L, RU)).isEqualTo("1 файл"); }
        @Test void few()  { assertThat(fmt.selectBranch("one{# файл} few{# файла} many{# файлов} other{# файла}", 2L, RU)).isEqualTo("2 файла"); }
        @Test void many() { assertThat(fmt.selectBranch("one{# файл} few{# файла} many{# файлов} other{# файла}", 5L, RU)).isEqualTo("5 файлов"); }
        @Test void one21(){ assertThat(fmt.selectBranch("one{# файл} few{# файла} many{# файлов} other{# файла}", 21L, RU)).isEqualTo("21 файл"); }
    }

    @Nested
    @DisplayName("Nested braces in branch content")
    class NestedBraces {
        @Test
        void branchCanContainBraces() {
            String result = fmt.selectBranch("one{1 {item}} other{# {items}}", 1L, Locale.ENGLISH);
            assertThat(result).isEqualTo("1 {item}");
        }
    }

    @Nested
    @DisplayName("Error cases")
    class ErrorCases {
        @Test
        void noOtherBranchThrows() {
            assertThatExceptionOfType(OpenI18nException.class)
                    .isThrownBy(() -> fmt.selectBranch("one{singular}", 5L, Locale.ENGLISH));
        }

        @Test
        void invalidPatternThrows() {
            assertThatExceptionOfType(OpenI18nException.class)
                    .isThrownBy(() -> fmt.selectBranch("one{unclosed", 1L, Locale.ENGLISH));
        }
    }

    @Nested
    @DisplayName("Cache management")
    class Cache {
        @Test
        void clearCacheWorks() {
            fmt.selectBranch("one{a} other{b}", 1L, Locale.ENGLISH);
            assertThatCode(() -> fmt.clearCache()).doesNotThrowAnyException();
            // Should still work after clear
            assertThat(fmt.selectBranch("one{a} other{b}", 1L, Locale.ENGLISH)).isEqualTo("a");
        }
    }
}
