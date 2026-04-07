package cloud.opencode.base.yml.diff;

import cloud.opencode.base.yml.YmlDocument;
import cloud.opencode.base.yml.exception.OpenYmlException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("YmlDiff")
class YmlDiffTest {

    @Nested
    @DisplayName("Diff Maps")
    class DiffMaps {

        @Test
        @DisplayName("identical maps produce no diffs")
        void identicalMapsNoDiffs() {
            Map<String, Object> data = Map.of("key", "value", "num", 42);

            List<DiffEntry> diffs = YmlDiff.diff(data, data);

            assertThat(diffs).isEmpty();
        }

        @Test
        @DisplayName("added key detected")
        void addedKeyDetected() {
            Map<String, Object> base = Map.of("a", 1);
            Map<String, Object> other = Map.of("a", 1, "b", 2);

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(1);
            assertThat(diffs.getFirst().type()).isEqualTo(DiffType.ADDED);
            assertThat(diffs.getFirst().path()).isEqualTo("b");
            assertThat(diffs.getFirst().newValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("removed key detected")
        void removedKeyDetected() {
            Map<String, Object> base = Map.of("a", 1, "b", 2);
            Map<String, Object> other = Map.of("a", 1);

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(1);
            assertThat(diffs.getFirst().type()).isEqualTo(DiffType.REMOVED);
            assertThat(diffs.getFirst().path()).isEqualTo("b");
            assertThat(diffs.getFirst().oldValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("modified value detected")
        void modifiedValueDetected() {
            Map<String, Object> base = Map.of("port", 8080);
            Map<String, Object> other = Map.of("port", 9090);

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(1);
            assertThat(diffs.getFirst().type()).isEqualTo(DiffType.MODIFIED);
            assertThat(diffs.getFirst().path()).isEqualTo("port");
            assertThat(diffs.getFirst().oldValue()).isEqualTo(8080);
            assertThat(diffs.getFirst().newValue()).isEqualTo(9090);
        }

        @Test
        @DisplayName("nested map differences use dot-notation paths")
        void nestedMapDotNotation() {
            Map<String, Object> base = Map.of("server", Map.of("port", 8080, "host", "localhost"));
            Map<String, Object> other = Map.of("server", Map.of("port", 9090, "host", "localhost"));

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(1);
            assertThat(diffs.getFirst().path()).isEqualTo("server.port");
        }

        @Test
        @DisplayName("deeply nested changes detected")
        void deeplyNestedChanges() {
            Map<String, Object> base = Map.of("a", Map.of("b", Map.of("c", Map.of("d", 1))));
            Map<String, Object> other = Map.of("a", Map.of("b", Map.of("c", Map.of("d", 2))));

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(1);
            assertThat(diffs.getFirst().path()).isEqualTo("a.b.c.d");
            assertThat(diffs.getFirst().oldValue()).isEqualTo(1);
            assertThat(diffs.getFirst().newValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("list element differences use index notation")
        void listDiffsUseIndexNotation() {
            Map<String, Object> base = Map.of("items", List.of("a", "b", "c"));
            Map<String, Object> other = Map.of("items", List.of("a", "x", "c"));

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(1);
            assertThat(diffs.getFirst().path()).isEqualTo("items[1]");
            assertThat(diffs.getFirst().oldValue()).isEqualTo("b");
            assertThat(diffs.getFirst().newValue()).isEqualTo("x");
        }

        @Test
        @DisplayName("list with added elements")
        void listWithAddedElements() {
            Map<String, Object> base = Map.of("items", List.of("a"));
            Map<String, Object> other = Map.of("items", List.of("a", "b", "c"));

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(2);
            assertThat(diffs).extracting(DiffEntry::type)
                    .containsOnly(DiffType.ADDED);
            assertThat(diffs).extracting(DiffEntry::path)
                    .containsExactly("items[1]", "items[2]");
        }

        @Test
        @DisplayName("list with removed elements")
        void listWithRemovedElements() {
            Map<String, Object> base = Map.of("items", List.of("a", "b", "c"));
            Map<String, Object> other = Map.of("items", List.of("a"));

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(2);
            assertThat(diffs).extracting(DiffEntry::type)
                    .containsOnly(DiffType.REMOVED);
            assertThat(diffs).extracting(DiffEntry::path)
                    .containsExactly("items[1]", "items[2]");
        }

        @Test
        @DisplayName("nested map within list")
        void nestedMapWithinList() {
            Map<String, Object> base = Map.of("items",
                    List.of(Map.of("name", "Alice")));
            Map<String, Object> other = Map.of("items",
                    List.of(Map.of("name", "Bob")));

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(1);
            assertThat(diffs.getFirst().path()).isEqualTo("items[0].name");
        }

        @Test
        @DisplayName("empty maps have no diffs")
        void emptyMaps() {
            List<DiffEntry> diffs = YmlDiff.diff(
                    Collections.emptyMap(), Collections.emptyMap());

            assertThat(diffs).isEmpty();
        }

        @Test
        @DisplayName("mixed add, remove, and modify")
        void mixedChanges() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("keep", "same");
            base.put("modify", "old");
            base.put("remove", "gone");

            Map<String, Object> other = new LinkedHashMap<>();
            other.put("keep", "same");
            other.put("modify", "new");
            other.put("add", "fresh");

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(3);
            assertThat(diffs).extracting(DiffEntry::type)
                    .containsExactlyInAnyOrder(DiffType.REMOVED, DiffType.MODIFIED, DiffType.ADDED);
        }

        @Test
        @DisplayName("value changed from scalar to map")
        void scalarToMapChange() {
            Map<String, Object> base = Map.of("server", "simple");
            Map<String, Object> other = Map.of("server", Map.of("port", 8080));

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(1);
            assertThat(diffs.getFirst().type()).isEqualTo(DiffType.MODIFIED);
            assertThat(diffs.getFirst().path()).isEqualTo("server");
        }

        @Test
        @DisplayName("value changed from map to scalar")
        void mapToScalarChange() {
            Map<String, Object> base = Map.of("server", Map.of("port", 8080));
            Map<String, Object> other = Map.of("server", "simple");

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(1);
            assertThat(diffs.getFirst().type()).isEqualTo(DiffType.MODIFIED);
        }

        @Test
        @DisplayName("null value to non-null detected as modified")
        void nullToNonNull() {
            Map<String, Object> base = new HashMap<>();
            base.put("key", null);

            Map<String, Object> other = Map.of("key", "value");

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(1);
            assertThat(diffs.getFirst().type()).isEqualTo(DiffType.MODIFIED);
            assertThat(diffs.getFirst().oldValue()).isNull();
            assertThat(diffs.getFirst().newValue()).isEqualTo("value");
        }

        @Test
        @DisplayName("non-null to null detected as modified")
        void nonNullToNull() {
            Map<String, Object> base = Map.of("key", "value");

            Map<String, Object> other = new HashMap<>();
            other.put("key", null);

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(1);
            assertThat(diffs.getFirst().type()).isEqualTo(DiffType.MODIFIED);
        }
    }

    @Nested
    @DisplayName("Diff Documents")
    class DiffDocuments {

        @Test
        @DisplayName("document diff delegates to map diff")
        void documentDiffWorks() {
            YmlDocument base = YmlDocument.of(Map.of("port", 8080));
            YmlDocument other = YmlDocument.of(Map.of("port", 9090));

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(1);
            assertThat(diffs.getFirst().path()).isEqualTo("port");
        }

        @Test
        @DisplayName("identical documents have no diffs")
        void identicalDocuments() {
            YmlDocument doc = YmlDocument.of(Map.of("a", 1));

            List<DiffEntry> diffs = YmlDiff.diff(doc, doc);

            assertThat(diffs).isEmpty();
        }

        @Test
        @DisplayName("empty documents have no diffs")
        void emptyDocuments() {
            List<DiffEntry> diffs = YmlDiff.diff(YmlDocument.empty(), YmlDocument.empty());

            assertThat(diffs).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("null base treated as empty (all ADDED)")
        void nullBaseTreatedAsEmpty() {
            Map<String, Object> other = Map.of("a", 1, "b", 2);

            List<DiffEntry> diffs = YmlDiff.diff((Map<String, Object>) null, other);

            assertThat(diffs).hasSize(2);
            assertThat(diffs).extracting(DiffEntry::type)
                    .containsOnly(DiffType.ADDED);
        }

        @Test
        @DisplayName("null other treated as empty (all REMOVED)")
        void nullOtherTreatedAsEmpty() {
            Map<String, Object> base = Map.of("a", 1, "b", 2);

            List<DiffEntry> diffs = YmlDiff.diff(base, (Map<String, Object>) null);

            assertThat(diffs).hasSize(2);
            assertThat(diffs).extracting(DiffEntry::type)
                    .containsOnly(DiffType.REMOVED);
        }

        @Test
        @DisplayName("both null produces no diffs")
        void bothNullNoDiffs() {
            List<DiffEntry> diffs = YmlDiff.diff(
                    (Map<String, Object>) null, (Map<String, Object>) null);

            assertThat(diffs).isEmpty();
        }

        @Test
        @DisplayName("null documents treated as empty")
        void nullDocumentsTreatedAsEmpty() {
            List<DiffEntry> diffs = YmlDiff.diff(
                    (YmlDocument) null, (YmlDocument) null);

            assertThat(diffs).isEmpty();
        }

        @Test
        @DisplayName("deep nesting beyond limit throws OpenYmlException")
        void deepNestingThrows() {
            // Build a structure deeper than MAX_DEPTH (50)
            Map<String, Object> base = new HashMap<>();
            Map<String, Object> other = new HashMap<>();

            Map<String, Object> currentBase = base;
            Map<String, Object> currentOther = other;
            for (int i = 0; i < 55; i++) {
                Map<String, Object> nextBase = new HashMap<>();
                Map<String, Object> nextOther = new HashMap<>();
                currentBase.put("level", nextBase);
                currentOther.put("level", nextOther);
                currentBase = nextBase;
                currentOther = nextOther;
            }
            currentBase.put("value", 1);
            currentOther.put("value", 2);

            assertThatThrownBy(() -> YmlDiff.diff(base, other))
                    .isInstanceOf(OpenYmlException.class)
                    .hasMessageContaining("maximum depth");
        }

        @Test
        @DisplayName("result list is unmodifiable")
        void resultIsUnmodifiable() {
            List<DiffEntry> diffs = YmlDiff.diff(Map.of("a", 1), Map.of("a", 2));

            assertThatThrownBy(() -> diffs.add(DiffEntry.added("x", 1)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("both values null for same key produces no diff")
        void bothValuesNullNoDiff() {
            Map<String, Object> base = new HashMap<>();
            base.put("key", null);
            Map<String, Object> other = new HashMap<>();
            other.put("key", null);

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).isEmpty();
        }

        @Test
        @DisplayName("empty list vs non-empty list")
        void emptyVsNonEmptyList() {
            Map<String, Object> base = Map.of("items", Collections.emptyList());
            Map<String, Object> other = Map.of("items", List.of("a", "b"));

            List<DiffEntry> diffs = YmlDiff.diff(base, other);

            assertThat(diffs).hasSize(2);
            assertThat(diffs).extracting(DiffEntry::type)
                    .containsOnly(DiffType.ADDED);
        }
    }
}
