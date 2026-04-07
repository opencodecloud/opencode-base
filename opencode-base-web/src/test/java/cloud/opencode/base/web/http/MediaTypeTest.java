package cloud.opencode.base.web.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@DisplayName("MediaType")
class MediaTypeTest {

    @Nested
    @DisplayName("Constants")
    class Constants {

        @Test
        @DisplayName("should define standard media types")
        void shouldDefineStandardTypes() {
            assertThat(MediaType.APPLICATION_JSON.mimeType()).isEqualTo("application/json");
            assertThat(MediaType.APPLICATION_XML.mimeType()).isEqualTo("application/xml");
            assertThat(MediaType.TEXT_PLAIN.mimeType()).isEqualTo("text/plain");
            assertThat(MediaType.TEXT_HTML.mimeType()).isEqualTo("text/html");
            assertThat(MediaType.APPLICATION_OCTET_STREAM.mimeType()).isEqualTo("application/octet-stream");
            assertThat(MediaType.ALL.mimeType()).isEqualTo("*/*");
        }
    }

    @Nested
    @DisplayName("of(String, String)")
    class OfTypeSubtype {

        @Test
        @DisplayName("should create media type")
        void shouldCreate() {
            MediaType mt = MediaType.of("application", "json");
            assertThat(mt.type()).isEqualTo("application");
            assertThat(mt.subtype()).isEqualTo("json");
            assertThat(mt.parameters()).isEmpty();
        }

        @Test
        @DisplayName("should normalize to lowercase")
        void shouldNormalize() {
            MediaType mt = MediaType.of("Application", "JSON");
            assertThat(mt.type()).isEqualTo("application");
            assertThat(mt.subtype()).isEqualTo("json");
        }

        @Test
        @DisplayName("should reject null type")
        void shouldRejectNullType() {
            assertThatThrownBy(() -> MediaType.of(null, "json"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject null subtype")
        void shouldRejectNullSubtype() {
            assertThatThrownBy(() -> MediaType.of("text", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject empty type")
        void shouldRejectEmptyType() {
            assertThatThrownBy(() -> MediaType.of("", "json"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject empty subtype")
        void shouldRejectEmptySubtype() {
            assertThatThrownBy(() -> MediaType.of("text", ""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("of(String, String, Map)")
    class OfTypeSubtypeParams {

        @Test
        @DisplayName("should create with parameters")
        void shouldCreateWithParams() {
            MediaType mt = MediaType.of("text", "html", Map.of("charset", "utf-8"));
            assertThat(mt.parameters()).containsEntry("charset", "utf-8");
        }

        @Test
        @DisplayName("should defensively copy parameters")
        void shouldCopyParams() {
            var params = new java.util.HashMap<String, String>();
            params.put("charset", "utf-8");
            MediaType mt = MediaType.of("text", "html", params);
            params.put("q", "0.5");
            assertThat(mt.parameters()).doesNotContainKey("q");
        }
    }

    @Nested
    @DisplayName("parse(String)")
    class ParseSingle {

        @Test
        @DisplayName("should parse simple media type")
        void shouldParseSimple() {
            MediaType mt = MediaType.parse("application/json");
            assertThat(mt.type()).isEqualTo("application");
            assertThat(mt.subtype()).isEqualTo("json");
            assertThat(mt.parameters()).isEmpty();
        }

        @Test
        @DisplayName("should parse with charset parameter")
        void shouldParseWithCharset() {
            MediaType mt = MediaType.parse("text/html; charset=utf-8");
            assertThat(mt.type()).isEqualTo("text");
            assertThat(mt.subtype()).isEqualTo("html");
            assertThat(mt.getCharset()).hasValue("utf-8");
        }

        @Test
        @DisplayName("should parse with multiple parameters")
        void shouldParseMultipleParams() {
            MediaType mt = MediaType.parse("text/plain; charset=utf-8; q=0.8");
            assertThat(mt.getCharset()).hasValue("utf-8");
            assertThat(mt.getQuality()).isCloseTo(0.8, within(0.001));
        }

        @Test
        @DisplayName("should parse with quoted parameter value")
        void shouldParseQuotedParam() {
            MediaType mt = MediaType.parse("text/html; charset=\"utf-8\"");
            assertThat(mt.getCharset()).hasValue("utf-8");
        }

        @Test
        @DisplayName("should parse wildcard")
        void shouldParseWildcard() {
            MediaType mt = MediaType.parse("*/*");
            assertThat(mt.isWildcard()).isTrue();
        }

        @Test
        @DisplayName("should parse wildcard subtype")
        void shouldParseWildcardSubtype() {
            MediaType mt = MediaType.parse("text/*");
            assertThat(mt.isWildcardSubtype()).isTrue();
            assertThat(mt.isWildcard()).isFalse();
        }

        @Test
        @DisplayName("should reject null")
        void shouldRejectNull() {
            assertThatThrownBy(() -> MediaType.parse(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject blank")
        void shouldRejectBlank() {
            assertThatThrownBy(() -> MediaType.parse("  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject missing slash")
        void shouldRejectMissingSlash() {
            assertThatThrownBy(() -> MediaType.parse("applicationjson"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("parseAccept(String)")
    class ParseAccept {

        @Test
        @DisplayName("should parse single type")
        void shouldParseSingle() {
            List<MediaType> types = MediaType.parseAccept("application/json");
            assertThat(types).hasSize(1);
            assertThat(types.getFirst().mimeType()).isEqualTo("application/json");
        }

        @Test
        @DisplayName("should parse multiple types sorted by quality")
        void shouldParseMultipleSortedByQuality() {
            List<MediaType> types = MediaType.parseAccept(
                    "text/html, application/json;q=0.9, text/plain;q=0.5");
            assertThat(types).hasSize(3);
            assertThat(types.get(0).mimeType()).isEqualTo("text/html");
            assertThat(types.get(0).getQuality()).isCloseTo(1.0, within(0.001));
            assertThat(types.get(1).mimeType()).isEqualTo("application/json");
            assertThat(types.get(1).getQuality()).isCloseTo(0.9, within(0.001));
            assertThat(types.get(2).mimeType()).isEqualTo("text/plain");
            assertThat(types.get(2).getQuality()).isCloseTo(0.5, within(0.001));
        }

        @Test
        @DisplayName("should handle equal quality factors preserving order")
        void shouldHandleEqualQuality() {
            List<MediaType> types = MediaType.parseAccept("text/html, application/json");
            assertThat(types).hasSize(2);
            // Both q=1.0, stable sort preserves order
            assertThat(types.get(0).getQuality()).isCloseTo(1.0, within(0.001));
            assertThat(types.get(1).getQuality()).isCloseTo(1.0, within(0.001));
        }

        @Test
        @DisplayName("should reject null")
        void shouldRejectNull() {
            assertThatThrownBy(() -> MediaType.parseAccept(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject blank")
        void shouldRejectBlank() {
            assertThatThrownBy(() -> MediaType.parseAccept(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("result should be unmodifiable")
        void resultShouldBeUnmodifiable() {
            List<MediaType> types = MediaType.parseAccept("text/html");
            assertThatThrownBy(() -> types.add(MediaType.ALL))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("bestMatch")
    class BestMatch {

        @Test
        @DisplayName("should find exact match")
        void shouldFindExactMatch() {
            List<MediaType> acceptable = MediaType.parseAccept("application/json");
            List<MediaType> available = List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML);
            Optional<MediaType> best = MediaType.bestMatch(acceptable, available);
            assertThat(best).isPresent();
            assertThat(best.get().mimeType()).isEqualTo("application/json");
        }

        @Test
        @DisplayName("should prefer higher quality")
        void shouldPreferHigherQuality() {
            List<MediaType> acceptable = MediaType.parseAccept(
                    "text/html;q=0.5, application/json;q=0.9");
            List<MediaType> available = List.of(MediaType.TEXT_HTML, MediaType.APPLICATION_JSON);
            Optional<MediaType> best = MediaType.bestMatch(acceptable, available);
            assertThat(best).isPresent();
            assertThat(best.get().mimeType()).isEqualTo("application/json");
        }

        @Test
        @DisplayName("should match wildcard")
        void shouldMatchWildcard() {
            List<MediaType> acceptable = MediaType.parseAccept("*/*");
            List<MediaType> available = List.of(MediaType.APPLICATION_JSON);
            Optional<MediaType> best = MediaType.bestMatch(acceptable, available);
            assertThat(best).isPresent();
            assertThat(best.get().mimeType()).isEqualTo("application/json");
        }

        @Test
        @DisplayName("should match wildcard subtype")
        void shouldMatchWildcardSubtype() {
            List<MediaType> acceptable = List.of(MediaType.of("text", "*"));
            List<MediaType> available = List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN);
            Optional<MediaType> best = MediaType.bestMatch(acceptable, available);
            assertThat(best).isPresent();
            assertThat(best.get().mimeType()).isEqualTo("text/plain");
        }

        @Test
        @DisplayName("should return empty when no match")
        void shouldReturnEmptyWhenNoMatch() {
            List<MediaType> acceptable = MediaType.parseAccept("application/xml");
            List<MediaType> available = List.of(MediaType.APPLICATION_JSON);
            Optional<MediaType> best = MediaType.bestMatch(acceptable, available);
            assertThat(best).isEmpty();
        }

        @Test
        @DisplayName("should return empty for empty available")
        void shouldReturnEmptyForEmptyAvailable() {
            List<MediaType> acceptable = MediaType.parseAccept("application/json");
            Optional<MediaType> best = MediaType.bestMatch(acceptable, List.of());
            assertThat(best).isEmpty();
        }
    }

    @Nested
    @DisplayName("getQuality")
    class GetQuality {

        @Test
        @DisplayName("should default to 1.0")
        void shouldDefaultToOne() {
            MediaType mt = MediaType.of("text", "html");
            assertThat(mt.getQuality()).isCloseTo(1.0, within(0.001));
        }

        @Test
        @DisplayName("should parse q parameter")
        void shouldParseQ() {
            MediaType mt = MediaType.parse("text/html;q=0.7");
            assertThat(mt.getQuality()).isCloseTo(0.7, within(0.001));
        }

        @Test
        @DisplayName("should clamp negative q to 0")
        void shouldClampNegative() {
            MediaType mt = MediaType.of("text", "html", Map.of("q", "-0.5"));
            assertThat(mt.getQuality()).isCloseTo(0.0, within(0.001));
        }

        @Test
        @DisplayName("should clamp q above 1 to 1")
        void shouldClampAboveOne() {
            MediaType mt = MediaType.of("text", "html", Map.of("q", "1.5"));
            assertThat(mt.getQuality()).isCloseTo(1.0, within(0.001));
        }

        @Test
        @DisplayName("should return 0.0 (deprioritize) for invalid q")
        void shouldDefaultForInvalidQ() {
            MediaType mt = MediaType.of("text", "html", Map.of("q", "abc"));
            assertThat(mt.getQuality()).isCloseTo(0.0, within(0.001));
        }
    }

    @Nested
    @DisplayName("includes")
    class Includes {

        @Test
        @DisplayName("wildcard should include anything")
        void wildcardIncludesAnything() {
            assertThat(MediaType.ALL.includes(MediaType.APPLICATION_JSON)).isTrue();
            assertThat(MediaType.ALL.includes(MediaType.TEXT_HTML)).isTrue();
        }

        @Test
        @DisplayName("wildcard subtype should include same type")
        void wildcardSubtypeIncludesSameType() {
            MediaType textWild = MediaType.of("text", "*");
            assertThat(textWild.includes(MediaType.TEXT_PLAIN)).isTrue();
            assertThat(textWild.includes(MediaType.TEXT_HTML)).isTrue();
            assertThat(textWild.includes(MediaType.APPLICATION_JSON)).isFalse();
        }

        @Test
        @DisplayName("exact type should include itself")
        void exactIncludesItself() {
            assertThat(MediaType.APPLICATION_JSON.includes(MediaType.APPLICATION_JSON)).isTrue();
        }

        @Test
        @DisplayName("should not include different subtype")
        void shouldNotIncludeDifferentSubtype() {
            assertThat(MediaType.APPLICATION_JSON.includes(MediaType.APPLICATION_XML)).isFalse();
        }

        @Test
        @DisplayName("should not include null")
        void shouldNotIncludeNull() {
            assertThat(MediaType.APPLICATION_JSON.includes(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isWildcard / isWildcardSubtype")
    class WildcardTests {

        @Test
        @DisplayName("ALL should be wildcard")
        void allShouldBeWildcard() {
            assertThat(MediaType.ALL.isWildcard()).isTrue();
            assertThat(MediaType.ALL.isWildcardSubtype()).isTrue();
        }

        @Test
        @DisplayName("text/* should be wildcard subtype but not wildcard")
        void textWildShouldBeWildcardSubtype() {
            MediaType mt = MediaType.of("text", "*");
            assertThat(mt.isWildcard()).isFalse();
            assertThat(mt.isWildcardSubtype()).isTrue();
        }

        @Test
        @DisplayName("application/json should not be wildcard")
        void jsonNotWildcard() {
            assertThat(MediaType.APPLICATION_JSON.isWildcard()).isFalse();
            assertThat(MediaType.APPLICATION_JSON.isWildcardSubtype()).isFalse();
        }
    }

    @Nested
    @DisplayName("mimeType")
    class MimeTypeTest {

        @Test
        @DisplayName("should return type/subtype")
        void shouldReturnMimeType() {
            assertThat(MediaType.APPLICATION_JSON.mimeType()).isEqualTo("application/json");
        }
    }

    @Nested
    @DisplayName("getCharset")
    class GetCharsetTest {

        @Test
        @DisplayName("should return charset when present")
        void shouldReturnCharset() {
            MediaType mt = MediaType.parse("text/html; charset=utf-8");
            assertThat(mt.getCharset()).hasValue("utf-8");
        }

        @Test
        @DisplayName("should return empty when no charset")
        void shouldReturnEmptyWhenAbsent() {
            assertThat(MediaType.APPLICATION_JSON.getCharset()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("should format without parameters")
        void shouldFormatWithoutParams() {
            assertThat(MediaType.APPLICATION_JSON.toString()).isEqualTo("application/json");
        }

        @Test
        @DisplayName("should format with parameters")
        void shouldFormatWithParams() {
            MediaType mt = MediaType.parse("text/html; charset=utf-8");
            assertThat(mt.toString()).isEqualTo("text/html; charset=utf-8");
        }
    }

    @Nested
    @DisplayName("Record equality")
    class EqualityTest {

        @Test
        @DisplayName("equal media types should be equal")
        void shouldBeEqual() {
            MediaType a = MediaType.of("application", "json");
            MediaType b = MediaType.of("application", "json");
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("different media types should not be equal")
        void shouldNotBeEqual() {
            assertThat(MediaType.APPLICATION_JSON).isNotEqualTo(MediaType.TEXT_PLAIN);
        }
    }
}
