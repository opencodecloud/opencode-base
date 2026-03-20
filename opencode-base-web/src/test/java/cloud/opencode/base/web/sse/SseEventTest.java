package cloud.opencode.base.web.sse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SseEvent")
class SseEventTest {

    @Nested
    @DisplayName("of(String data)")
    class OfData {

        @Test
        @DisplayName("should create event with data and default event type")
        void shouldCreateWithDataAndDefaultEvent() {
            SseEvent event = SseEvent.of("hello");
            assertThat(event.data()).isEqualTo("hello");
            assertThat(event.event()).isEqualTo(SseEvent.DEFAULT_EVENT);
            assertThat(event.id()).isNull();
            assertThat(event.retry()).isNull();
        }

        @Test
        @DisplayName("should accept null data")
        void shouldAcceptNullData() {
            SseEvent event = SseEvent.of((String) null);
            assertThat(event.data()).isNull();
        }
    }

    @Nested
    @DisplayName("of(String event, String data)")
    class OfEventData {

        @Test
        @DisplayName("should create event with event type and data")
        void shouldCreateWithEventAndData() {
            SseEvent event = SseEvent.of("update", "payload");
            assertThat(event.event()).isEqualTo("update");
            assertThat(event.data()).isEqualTo("payload");
            assertThat(event.id()).isNull();
        }
    }

    @Nested
    @DisplayName("of(String id, String event, String data)")
    class OfIdEventData {

        @Test
        @DisplayName("should create event with id, event type, and data")
        void shouldCreateWithAllFields() {
            SseEvent event = SseEvent.of("42", "update", "payload");
            assertThat(event.id()).isEqualTo("42");
            assertThat(event.event()).isEqualTo("update");
            assertThat(event.data()).isEqualTo("payload");
            assertThat(event.retry()).isNull();
        }
    }

    @Nested
    @DisplayName("eventOrDefault()")
    class EventOrDefault {

        @Test
        @DisplayName("should return event when set")
        void shouldReturnEventWhenSet() {
            SseEvent event = SseEvent.of("custom", "data");
            assertThat(event.eventOrDefault()).isEqualTo("custom");
        }

        @Test
        @DisplayName("should return default when event is null")
        void shouldReturnDefaultWhenNull() {
            SseEvent event = new SseEvent(null, null, "data", null);
            assertThat(event.eventOrDefault()).isEqualTo(SseEvent.DEFAULT_EVENT);
        }
    }

    @Nested
    @DisplayName("getId()")
    class GetId {

        @Test
        @DisplayName("should return present optional when id is set")
        void shouldReturnPresentOptional() {
            SseEvent event = SseEvent.of("123", "msg", "data");
            assertThat(event.getId()).isPresent().contains("123");
        }

        @Test
        @DisplayName("should return empty optional when id is null")
        void shouldReturnEmptyOptional() {
            SseEvent event = SseEvent.of("data");
            assertThat(event.getId()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getRetry()")
    class GetRetry {

        @Test
        @DisplayName("should return present optional when retry is set")
        void shouldReturnPresentOptional() {
            SseEvent event = SseEvent.builder().retry(3000).build();
            assertThat(event.getRetry()).isPresent().contains(3000L);
        }

        @Test
        @DisplayName("should return empty optional when retry is null")
        void shouldReturnEmptyOptional() {
            SseEvent event = SseEvent.of("data");
            assertThat(event.getRetry()).isEmpty();
        }
    }

    @Nested
    @DisplayName("isMessage()")
    class IsMessage {

        @Test
        @DisplayName("should return true for default event type")
        void shouldReturnTrueForDefault() {
            assertThat(SseEvent.of("data").isMessage()).isTrue();
        }

        @Test
        @DisplayName("should return true when event is null")
        void shouldReturnTrueWhenNull() {
            SseEvent event = new SseEvent(null, null, "data", null);
            assertThat(event.isMessage()).isTrue();
        }

        @Test
        @DisplayName("should return false for custom event type")
        void shouldReturnFalseForCustom() {
            assertThat(SseEvent.of("update", "data").isMessage()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasData()")
    class HasData {

        @Test
        @DisplayName("should return true when data is present")
        void shouldReturnTrueWhenPresent() {
            assertThat(SseEvent.of("data").hasData()).isTrue();
        }

        @Test
        @DisplayName("should return false when data is null")
        void shouldReturnFalseWhenNull() {
            SseEvent event = new SseEvent(null, null, null, null);
            assertThat(event.hasData()).isFalse();
        }

        @Test
        @DisplayName("should return false when data is empty")
        void shouldReturnFalseWhenEmpty() {
            assertThat(SseEvent.of("").hasData()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasId()")
    class HasId {

        @Test
        @DisplayName("should return true when id is present and non-empty")
        void shouldReturnTrue() {
            assertThat(SseEvent.of("1", "msg", "data").hasId()).isTrue();
        }

        @Test
        @DisplayName("should return false when id is null")
        void shouldReturnFalseWhenNull() {
            assertThat(SseEvent.of("data").hasId()).isFalse();
        }

        @Test
        @DisplayName("should return false when id is empty")
        void shouldReturnFalseWhenEmpty() {
            SseEvent event = SseEvent.of("", "msg", "data");
            assertThat(event.hasId()).isFalse();
        }
    }

    @Nested
    @DisplayName("toDebugString()")
    class ToDebugString {

        @Test
        @DisplayName("should include all fields")
        void shouldIncludeAllFields() {
            SseEvent event = SseEvent.builder()
                    .id("42").event("update").data("payload").retry(5000).build();
            String debug = event.toDebugString();
            assertThat(debug).contains("id=42", "event=update", "data=payload", "retry=5000");
        }

        @Test
        @DisplayName("should truncate long data")
        void shouldTruncateLongData() {
            String longData = "x".repeat(100);
            SseEvent event = SseEvent.of(longData);
            String debug = event.toDebugString();
            assertThat(debug).contains("...");
            assertThat(debug).doesNotContain("x".repeat(100));
        }

        @Test
        @DisplayName("should not truncate short data")
        void shouldNotTruncateShortData() {
            SseEvent event = SseEvent.of("short");
            assertThat(event.toDebugString()).contains("data=short");
            assertThat(event.toDebugString()).doesNotContain("...");
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTest {

        @Test
        @DisplayName("should build event with all fields")
        void shouldBuildWithAllFields() {
            SseEvent event = SseEvent.builder()
                    .id("1").event("test").data("hello").retry(1000).build();
            assertThat(event.id()).isEqualTo("1");
            assertThat(event.event()).isEqualTo("test");
            assertThat(event.data()).isEqualTo("hello");
            assertThat(event.retry()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("appendData should concatenate with newline")
        void appendDataShouldConcatenate() {
            SseEvent event = SseEvent.builder()
                    .appendData("line1").appendData("line2").build();
            assertThat(event.data()).isEqualTo("line1\nline2");
        }

        @Test
        @DisplayName("appendData with null initial data should just set data")
        void appendDataWithNullInitial() {
            SseEvent event = SseEvent.builder().appendData("first").build();
            assertThat(event.data()).isEqualTo("first");
        }

        @Test
        @DisplayName("hasContent should return true when any field is set")
        void hasContentShouldReturnTrue() {
            SseEvent.Builder builder = SseEvent.builder().data("x");
            assertThat(builder.hasContent()).isTrue();
        }

        @Test
        @DisplayName("hasContent should return false for empty builder")
        void hasContentShouldReturnFalse() {
            assertThat(SseEvent.builder().hasContent()).isFalse();
        }

        @Test
        @DisplayName("reset should clear all fields")
        void resetShouldClearAll() {
            SseEvent.Builder builder = SseEvent.builder()
                    .id("1").event("e").data("d").retry(100);
            builder.reset();
            assertThat(builder.hasContent()).isFalse();
            SseEvent event = builder.build();
            assertThat(event.id()).isNull();
            assertThat(event.event()).isNull();
            assertThat(event.data()).isNull();
            assertThat(event.retry()).isNull();
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTest {

        @Test
        @DisplayName("should delegate to toDebugString")
        void shouldDelegateToDebugString() {
            SseEvent event = SseEvent.of("data");
            assertThat(event.toString()).isEqualTo(event.toDebugString());
        }
    }
}
