package cloud.opencode.base.web.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ETag")
class ETagTest {

    @Nested
    @DisplayName("strong(String)")
    class StrongFactory {

        @Test
        @DisplayName("should create strong ETag")
        void shouldCreateStrong() {
            ETag etag = ETag.strong("abc123");
            assertThat(etag.value()).isEqualTo("abc123");
            assertThat(etag.weak()).isFalse();
        }

        @Test
        @DisplayName("should reject null value")
        void shouldRejectNull() {
            assertThatThrownBy(() -> ETag.strong(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject empty value")
        void shouldRejectEmpty() {
            assertThatThrownBy(() -> ETag.strong(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("weak(String)")
    class WeakFactory {

        @Test
        @DisplayName("should create weak ETag")
        void shouldCreateWeak() {
            ETag etag = ETag.weak("abc123");
            assertThat(etag.value()).isEqualTo("abc123");
            assertThat(etag.weak()).isTrue();
        }
    }

    @Nested
    @DisplayName("fromContent(byte[])")
    class FromContentBytes {

        @Test
        @DisplayName("should generate SHA-256 based ETag")
        void shouldGenerateSha256() throws Exception {
            byte[] content = "Hello, World!".getBytes(StandardCharsets.UTF_8);
            ETag etag = ETag.fromContent(content);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String expected = HexFormat.of().formatHex(md.digest(content));

            assertThat(etag.value()).isEqualTo(expected);
            assertThat(etag.weak()).isFalse();
        }

        @Test
        @DisplayName("should produce consistent results")
        void shouldBeConsistent() {
            byte[] content = "test data".getBytes(StandardCharsets.UTF_8);
            ETag a = ETag.fromContent(content);
            ETag b = ETag.fromContent(content);
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("should produce different results for different content")
        void shouldDifferForDifferentContent() {
            ETag a = ETag.fromContent("hello".getBytes(StandardCharsets.UTF_8));
            ETag b = ETag.fromContent("world".getBytes(StandardCharsets.UTF_8));
            assertThat(a.value()).isNotEqualTo(b.value());
        }

        @Test
        @DisplayName("should reject null content")
        void shouldRejectNull() {
            assertThatThrownBy(() -> ETag.fromContent((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("fromContent(String)")
    class FromContentString {

        @Test
        @DisplayName("should generate SHA-256 based ETag from string")
        void shouldGenerateFromString() {
            ETag fromString = ETag.fromContent("Hello");
            ETag fromBytes = ETag.fromContent("Hello".getBytes(StandardCharsets.UTF_8));
            assertThat(fromString).isEqualTo(fromBytes);
        }

        @Test
        @DisplayName("should reject null string")
        void shouldRejectNull() {
            assertThatThrownBy(() -> ETag.fromContent((String) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("parse(String)")
    class ParseTest {

        @Test
        @DisplayName("should parse strong ETag")
        void shouldParseStrong() {
            ETag etag = ETag.parse("\"abc123\"");
            assertThat(etag.value()).isEqualTo("abc123");
            assertThat(etag.weak()).isFalse();
        }

        @Test
        @DisplayName("should parse weak ETag with W/ prefix")
        void shouldParseWeak() {
            ETag etag = ETag.parse("W/\"abc123\"");
            assertThat(etag.value()).isEqualTo("abc123");
            assertThat(etag.weak()).isTrue();
        }

        @Test
        @DisplayName("should reject lowercase w/ prefix per RFC 7232")
        void shouldRejectLowercaseWeak() {
            assertThatThrownBy(() -> ETag.parse("w/\"abc123\""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject null")
        void shouldRejectNull() {
            assertThatThrownBy(() -> ETag.parse(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject blank")
        void shouldRejectBlank() {
            assertThatThrownBy(() -> ETag.parse("  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject missing quotes")
        void shouldRejectMissingQuotes() {
            assertThatThrownBy(() -> ETag.parse("abc123"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject single quote")
        void shouldRejectSingleQuote() {
            assertThatThrownBy(() -> ETag.parse("\"abc123"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("headerValue")
    class HeaderValueTest {

        @Test
        @DisplayName("should format strong ETag")
        void shouldFormatStrong() {
            ETag etag = ETag.strong("abc");
            assertThat(etag.headerValue()).isEqualTo("\"abc\"");
        }

        @Test
        @DisplayName("should format weak ETag")
        void shouldFormatWeak() {
            ETag etag = ETag.weak("abc");
            assertThat(etag.headerValue()).isEqualTo("W/\"abc\"");
        }
    }

    @Nested
    @DisplayName("matches(String)")
    class MatchesTest {

        @Test
        @DisplayName("should match wildcard *")
        void shouldMatchWildcard() {
            ETag etag = ETag.strong("abc");
            assertThat(etag.matches("*")).isTrue();
        }

        @Test
        @DisplayName("should match single value")
        void shouldMatchSingleValue() {
            ETag etag = ETag.strong("abc");
            assertThat(etag.matches("\"abc\"")).isTrue();
        }

        @Test
        @DisplayName("should match in comma-separated list")
        void shouldMatchInList() {
            ETag etag = ETag.strong("def");
            assertThat(etag.matches("\"abc\", \"def\", \"ghi\"")).isTrue();
        }

        @Test
        @DisplayName("should match weak ETag in list")
        void shouldMatchWeakInList() {
            ETag etag = ETag.strong("abc");
            assertThat(etag.matches("W/\"abc\"")).isTrue();
        }

        @Test
        @DisplayName("should not match different value")
        void shouldNotMatchDifferent() {
            ETag etag = ETag.strong("abc");
            assertThat(etag.matches("\"xyz\"")).isFalse();
        }

        @Test
        @DisplayName("should not match null")
        void shouldNotMatchNull() {
            ETag etag = ETag.strong("abc");
            assertThat(etag.matches(null)).isFalse();
        }

        @Test
        @DisplayName("should not match blank")
        void shouldNotMatchBlank() {
            ETag etag = ETag.strong("abc");
            assertThat(etag.matches("  ")).isFalse();
        }

        @Test
        @DisplayName("should skip malformed entries in list")
        void shouldSkipMalformed() {
            ETag etag = ETag.strong("abc");
            assertThat(etag.matches("bad, \"abc\"")).isTrue();
        }

        @Test
        @DisplayName("weak ETag should match by value")
        void weakEtagShouldMatchByValue() {
            ETag etag = ETag.weak("abc");
            assertThat(etag.matches("\"abc\"")).isTrue();
            assertThat(etag.matches("W/\"abc\"")).isTrue();
        }
    }

    @Nested
    @DisplayName("strongMatches")
    class StrongMatchesTest {

        @Test
        @DisplayName("should match when both strong and equal")
        void shouldMatchBothStrong() {
            ETag a = ETag.strong("abc");
            ETag b = ETag.strong("abc");
            assertThat(a.strongMatches(b)).isTrue();
        }

        @Test
        @DisplayName("should not match when this is weak")
        void shouldNotMatchWhenThisWeak() {
            ETag a = ETag.weak("abc");
            ETag b = ETag.strong("abc");
            assertThat(a.strongMatches(b)).isFalse();
        }

        @Test
        @DisplayName("should not match when other is weak")
        void shouldNotMatchWhenOtherWeak() {
            ETag a = ETag.strong("abc");
            ETag b = ETag.weak("abc");
            assertThat(a.strongMatches(b)).isFalse();
        }

        @Test
        @DisplayName("should not match when both weak")
        void shouldNotMatchBothWeak() {
            ETag a = ETag.weak("abc");
            ETag b = ETag.weak("abc");
            assertThat(a.strongMatches(b)).isFalse();
        }

        @Test
        @DisplayName("should not match different values")
        void shouldNotMatchDifferent() {
            ETag a = ETag.strong("abc");
            ETag b = ETag.strong("def");
            assertThat(a.strongMatches(b)).isFalse();
        }

        @Test
        @DisplayName("should not match null")
        void shouldNotMatchNull() {
            ETag a = ETag.strong("abc");
            assertThat(a.strongMatches(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("weakMatches")
    class WeakMatchesTest {

        @Test
        @DisplayName("should match same value regardless of weakness")
        void shouldMatchSameValue() {
            ETag strong = ETag.strong("abc");
            ETag weak = ETag.weak("abc");
            assertThat(strong.weakMatches(weak)).isTrue();
            assertThat(weak.weakMatches(strong)).isTrue();
        }

        @Test
        @DisplayName("should match both strong with same value")
        void shouldMatchBothStrong() {
            ETag a = ETag.strong("abc");
            ETag b = ETag.strong("abc");
            assertThat(a.weakMatches(b)).isTrue();
        }

        @Test
        @DisplayName("should match both weak with same value")
        void shouldMatchBothWeak() {
            ETag a = ETag.weak("abc");
            ETag b = ETag.weak("abc");
            assertThat(a.weakMatches(b)).isTrue();
        }

        @Test
        @DisplayName("should not match different values")
        void shouldNotMatchDifferent() {
            ETag a = ETag.strong("abc");
            ETag b = ETag.strong("def");
            assertThat(a.weakMatches(b)).isFalse();
        }

        @Test
        @DisplayName("should not match null")
        void shouldNotMatchNull() {
            ETag a = ETag.strong("abc");
            assertThat(a.weakMatches(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("should return headerValue for strong")
        void shouldReturnHeaderValueStrong() {
            ETag etag = ETag.strong("abc");
            assertThat(etag.toString()).isEqualTo("\"abc\"");
        }

        @Test
        @DisplayName("should return headerValue for weak")
        void shouldReturnHeaderValueWeak() {
            ETag etag = ETag.weak("abc");
            assertThat(etag.toString()).isEqualTo("W/\"abc\"");
        }
    }

    @Nested
    @DisplayName("Record equality")
    class EqualityTest {

        @Test
        @DisplayName("same strong ETags should be equal")
        void sameStrongShouldBeEqual() {
            ETag a = ETag.strong("abc");
            ETag b = ETag.strong("abc");
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("same weak ETags should be equal")
        void sameWeakShouldBeEqual() {
            ETag a = ETag.weak("abc");
            ETag b = ETag.weak("abc");
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("strong and weak with same value should not be equal")
        void strongAndWeakShouldNotBeEqual() {
            ETag a = ETag.strong("abc");
            ETag b = ETag.weak("abc");
            assertThat(a).isNotEqualTo(b);
        }
    }
}
