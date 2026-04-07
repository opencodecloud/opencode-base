package cloud.opencode.base.i18n.formatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for IcuLikeFormatter
 */
@DisplayName("IcuLikeFormatter")
class IcuLikeFormatterTest {

    private final IcuLikeFormatter fmt = new IcuLikeFormatter();
    private static final Locale EN = Locale.ENGLISH;

    @Nested
    @DisplayName("Simple substitution - named")
    class SimpleNamed {
        @Test void singleParam() {
            assertThat(fmt.format("Hello, {name}!", EN, Map.of("name", "Alice"))).isEqualTo("Hello, Alice!");
        }
        @Test void multipleParams() {
            String r = fmt.format("{greeting}, {name}!", EN, Map.of("greeting", "Hi", "name", "Bob"));
            assertThat(r).isEqualTo("Hi, Bob!");
        }
        @Test void missingParamEmpty() {
            assertThat(fmt.format("{missing}", EN, Map.of())).isEqualTo("");
        }
        @Test void literalText() {
            assertThat(fmt.format("Hello world!", EN, Map.of())).isEqualTo("Hello world!");
        }
    }

    @Nested
    @DisplayName("Simple substitution - positional")
    class SimplePositional {
        @Test void firstParam() {
            assertThat(fmt.format("Hello, {0}!", EN, "Alice")).isEqualTo("Hello, Alice!");
        }
        @Test void multipleParams() {
            assertThat(fmt.format("{0} + {1} = {2}", EN, 1, 2, 3)).isEqualTo("1 + 2 = 3");
        }
    }

    @Nested
    @DisplayName("No single-quote escaping")
    class NoSingleQuoteEscaping {
        @Test void apostropheInText() {
            // In java.text.MessageFormat, you need "It''s". Here it works as-is.
            assertThat(fmt.format("It's {name}'s turn", EN, Map.of("name", "Alice")))
                    .isEqualTo("It's Alice's turn");
        }
        @Test void singleQuoteInLiteral() {
            assertThat(fmt.format("Don't worry", EN, Map.of())).isEqualTo("Don't worry");
        }
    }

    @Nested
    @DisplayName("Escaped braces")
    class EscapedBraces {
        @Test void escapedOpen() {
            assertThat(fmt.format("Use \\{ for literal", EN, Map.of())).isEqualTo("Use { for literal");
        }
        @Test void escapedClose() {
            assertThat(fmt.format("End \\}", EN, Map.of())).isEqualTo("End }");
        }
    }

    @Nested
    @DisplayName("Plural formatting - English")
    class PluralEnglish {
        private static final String TPL = "You have {count, plural, =0{no items} one{# item} other{# items}}.";

        @Test void zero()  { assertThat(fmt.format(TPL, EN, Map.of("count", 0))).isEqualTo("You have no items."); }
        @Test void one()   { assertThat(fmt.format(TPL, EN, Map.of("count", 1))).isEqualTo("You have 1 item."); }
        @Test void five()  { assertThat(fmt.format(TPL, EN, Map.of("count", 5))).isEqualTo("You have 5 items."); }
    }

    @Nested
    @DisplayName("Plural with named param inside branch")
    class PluralWithNamedParam {
        @Test void countInBranch() {
            String tpl = "{count, plural, one{1 file in {folder}} other{{count} files in {folder}}}";
            String r = fmt.format(tpl, EN, Map.of("count", 3, "folder", "Documents"));
            assertThat(r).isEqualTo("3 files in Documents");
        }
    }

    @Nested
    @DisplayName("Select formatting")
    class SelectFormatting {
        @Test void maleGender() {
            String r = fmt.format("{gender, select, male{He} female{She} other{They}} went.", EN,
                    Map.of("gender", "male"));
            assertThat(r).isEqualTo("He went.");
        }
        @Test void unknownFallsBackToOther() {
            String r = fmt.format("{gender, select, male{He} female{She} other{They}} went.", EN,
                    Map.of("gender", "unknown"));
            assertThat(r).isEqualTo("They went.");
        }
    }

    @Nested
    @DisplayName("Number formatting")
    class NumberFormatting {
        @Test void defaultFormat() {
            String r = fmt.format("Amount: {amount, number}", EN, Map.of("amount", 1234.5));
            assertThat(r).contains("1,234.5");
        }
        @Test void customPattern() {
            String r = fmt.format("Price: {price, number, #,##0.00}", EN, Map.of("price", 99.9));
            assertThat(r).isEqualTo("Price: 99.90");
        }
    }

    @Nested
    @DisplayName("Date formatting")
    class DateFormatting {
        @Test void customPattern() {
            String r = fmt.format("Date: {date, date, yyyy-MM-dd}", EN,
                    Map.of("date", LocalDate.of(2025, 1, 15)));
            assertThat(r).isEqualTo("Date: 2025-01-15");
        }
    }

    @Nested
    @DisplayName("Positional plural")
    class PositionalPlural {
        @Test void positionalArgs() {
            String r = fmt.format("{0} has {1, plural, one{# point} other{# points}}.",
                    EN, "Alice", 3);
            assertThat(r).isEqualTo("Alice has 3 points.");
        }
    }

    @Nested
    @DisplayName("Null and empty handling")
    class NullEmpty {
        @Test void nullTemplateReturnsEmpty() {
            assertThat(fmt.format(null, EN, Map.of())).isEmpty();
        }
        @Test void emptyTemplateReturnsEmpty() {
            assertThat(fmt.format("", EN, Map.of())).isEmpty();
        }
        @Test void nullParamsMap() {
            assertThat(fmt.format("Hello!", EN, (Map<String, Object>) null)).isEqualTo("Hello!");
        }
    }

    @Nested
    @DisplayName("Cache management")
    class Cache {
        @Test void clearCacheDoesNotBreakFormatting() {
            fmt.format("Hello, {name}!", EN, Map.of("name", "Test"));
            fmt.clearCache();
            assertThat(fmt.format("Hello, {name}!", EN, Map.of("name", "Test")))
                    .isEqualTo("Hello, Test!");
        }
    }
}
