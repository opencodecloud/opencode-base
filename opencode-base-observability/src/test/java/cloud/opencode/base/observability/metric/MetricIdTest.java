package cloud.opencode.base.observability.metric;

import cloud.opencode.base.observability.exception.ObservabilityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link MetricId}.
 */
@DisplayName("MetricId")
class MetricIdTest {

    @Nested
    @DisplayName("of() factory method")
    class OfFactory {

        @Test
        @DisplayName("should create id with name only")
        void shouldCreateWithNameOnly() {
            MetricId id = MetricId.of("requests");
            assertThat(id.name()).isEqualTo("requests");
            assertThat(id.tags()).isEmpty();
        }

        @Test
        @DisplayName("should create id with name and tags")
        void shouldCreateWithNameAndTags() {
            MetricId id = MetricId.of("requests", Tag.of("env", "prod"), Tag.of("app", "web"));
            assertThat(id.name()).isEqualTo("requests");
            assertThat(id.tags()).hasSize(2);
        }

        @Test
        @DisplayName("should handle null tags array")
        void shouldHandleNullTagsArray() {
            MetricId id = MetricId.of("requests", (Tag[]) null);
            assertThat(id.tags()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tag sorting")
    class TagSorting {

        @Test
        @DisplayName("should sort tags by key for consistent equals")
        void shouldSortTagsByKey() {
            MetricId id1 = MetricId.of("m", Tag.of("z", "1"), Tag.of("a", "2"));
            MetricId id2 = MetricId.of("m", Tag.of("a", "2"), Tag.of("z", "1"));
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
            // Verify sorted order
            assertThat(id1.tags().get(0).key()).isEqualTo("a");
            assertThat(id1.tags().get(1).key()).isEqualTo("z");
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should throw on null name")
        void shouldThrowOnNullName() {
            assertThatThrownBy(() -> MetricId.of(null))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("Metric name must not be null or blank");
        }

        @Test
        @DisplayName("should throw on blank name")
        void shouldThrowOnBlankName() {
            assertThatThrownBy(() -> MetricId.of("  "))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("Metric name must not be null or blank");
        }

        @Test
        @DisplayName("should throw on empty name")
        void shouldThrowOnEmptyName() {
            assertThatThrownBy(() -> MetricId.of(""))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("Metric name must not be null or blank");
        }

        @Test
        @DisplayName("name 含换行符应抛出异常（防注入）")
        void shouldThrowOnLineBreakInName() {
            assertThatThrownBy(() -> MetricId.of("http.requests\nfake"))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("line-break");
            assertThatThrownBy(() -> MetricId.of("http.requests\rfake"))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("line-break");
        }
    }

    @Nested
    @DisplayName("Defensive copy")
    class DefensiveCopy {

        @Test
        @DisplayName("should not be affected by modification of original list")
        void shouldDefensivelyCopyTags() {
            ArrayList<Tag> mutableTags = new ArrayList<>();
            mutableTags.add(Tag.of("env", "prod"));
            MetricId id = new MetricId("requests", mutableTags);
            mutableTags.add(Tag.of("extra", "val"));
            assertThat(id.tags()).hasSize(1);
        }

        @Test
        @DisplayName("tags list should be immutable")
        void shouldReturnImmutableTags() {
            MetricId id = MetricId.of("m", Tag.of("k", "v"));
            assertThatThrownBy(() -> id.tags().add(Tag.of("x", "y")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Null tags in constructor")
    class NullTags {

        @Test
        @DisplayName("should treat null tags as empty list")
        void shouldTreatNullTagsAsEmpty() {
            MetricId id = new MetricId("requests", null);
            assertThat(id.tags()).isEmpty();
        }
    }
}
