package cloud.opencode.base.id.uuid;

import cloud.opencode.base.id.exception.OpenIdGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UuidParser")
class UuidParserTest {

    private final UuidParser parser = UuidParser.create();

    @Nested
    @DisplayName("parse() — UUID v7")
    class ParseV7 {

        @Test
        void versionIs7() {
            UUID v7 = UuidV7Generator.create().generate();
            UuidParser.ParsedUuid parsed = parser.parse(v7);
            assertThat(parsed.version()).isEqualTo(7);
        }

        @Test
        void timeOrderedIsTrue() {
            UUID v7 = UuidV7Generator.create().generate();
            assertThat(parser.parse(v7).timeOrdered()).isTrue();
        }

        @Test
        void timestampIsRecent() {
            long before = System.currentTimeMillis();
            UUID v7 = UuidV7Generator.create().generate();
            long after = System.currentTimeMillis();

            Instant ts = parser.parse(v7).timestamp();
            assertThat(ts).isNotNull();
            assertThat(ts.toEpochMilli()).isBetween(before - 5, after + 5);
        }

        @Test
        void isV7ReturnsTrue() {
            UUID v7 = UuidV7Generator.create().generate();
            assertThat(parser.parse(v7).isV7()).isTrue();
        }

        @Test
        void isV4ReturnsFalse() {
            UUID v7 = UuidV7Generator.create().generate();
            assertThat(parser.parse(v7).isV4()).isFalse();
        }

        @Test
        void toShortStringIs32Chars() {
            UUID v7 = UuidV7Generator.create().generate();
            String shortStr = parser.parse(v7).toShortString();
            assertThat(shortStr).hasSize(32).doesNotContain("-");
        }
    }

    @Nested
    @DisplayName("parse() — UUID v4")
    class ParseV4 {

        @Test
        void versionIs4() {
            UUID v4 = UUID.randomUUID();
            UuidParser.ParsedUuid parsed = parser.parse(v4);
            assertThat(parsed.version()).isEqualTo(4);
        }

        @Test
        void timeOrderedIsFalse() {
            UUID v4 = UUID.randomUUID();
            assertThat(parser.parse(v4).timeOrdered()).isFalse();
        }

        @Test
        void timestampIsNull() {
            UUID v4 = UUID.randomUUID();
            assertThat(parser.parse(v4).timestamp()).isNull();
        }

        @Test
        void isV4ReturnsTrue() {
            UUID v4 = UUID.randomUUID();
            assertThat(parser.parse(v4).isV4()).isTrue();
        }
    }

    @Nested
    @DisplayName("extractTimestamp()")
    class ExtractTimestamp {

        @Test
        void v7TimestampExtracted() {
            long before = System.currentTimeMillis();
            UUID v7 = UuidV7Generator.create().generate();
            long after = System.currentTimeMillis();

            Instant ts = parser.extractTimestamp(v7);
            assertThat(ts.toEpochMilli()).isBetween(before - 5, after + 5);
        }

        @Test
        void v4ThrowsException() {
            UUID v4 = UUID.randomUUID();
            assertThatThrownBy(() -> parser.extractTimestamp(v4))
                    .isInstanceOf(OpenIdGenerationException.class);
        }

        @Test
        void nullThrowsException() {
            assertThatThrownBy(() -> parser.extractTimestamp(null))
                    .isInstanceOf(OpenIdGenerationException.class);
        }
    }

    @Nested
    @DisplayName("isValid()")
    class IsValid {

        @Test
        void validV4() {
            assertThat(parser.isValid(UUID.randomUUID())).isTrue();
        }

        @Test
        void validV7() {
            assertThat(parser.isValid(UuidV7Generator.create().generate())).isTrue();
        }

        @Test
        void nullReturnsFalse() {
            assertThat(parser.isValid(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("parse() — null guard")
    class NullGuard {

        @Test
        void nullUuidThrows() {
            assertThatThrownBy(() -> parser.parse(null))
                    .isInstanceOf(OpenIdGenerationException.class);
        }
    }
}
