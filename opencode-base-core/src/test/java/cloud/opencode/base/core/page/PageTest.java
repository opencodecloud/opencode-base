package cloud.opencode.base.core.page;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link Page}.
 */
@DisplayName("Page Tests")
class PageTest {

    @Nested
    @DisplayName("Constructor validation")
    class ConstructorValidationTests {

        @Test
        @DisplayName("rejects current < 1")
        void rejectsCurrentLessThanOne() {
            assertThatThrownBy(() -> new Page<>(0, 10, 0, List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("current must >= 1");

            assertThatThrownBy(() -> new Page<>(-5, 10, 0, List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("current must >= 1");
        }

        @Test
        @DisplayName("rejects size < 1")
        void rejectsSizeLessThanOne() {
            assertThatThrownBy(() -> new Page<>(1, 0, 0, List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("size must >= 1");

            assertThatThrownBy(() -> new Page<>(1, -3, 0, List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("size must >= 1");
        }

        @Test
        @DisplayName("rejects total < 0")
        void rejectsTotalLessThanZero() {
            assertThatThrownBy(() -> new Page<>(1, 10, -1, List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("total must >= 0");
        }

        @Test
        @DisplayName("rejects null records")
        void rejectsNullRecords() {
            assertThatThrownBy(() -> new Page<>(1, 10, 0, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("records must not be null");
        }

        @Test
        @DisplayName("valid construction succeeds")
        void validConstruction() {
            Page<String> page = new Page<>(2, 15, 100, List.of("a", "b"));
            assertThat(page.current()).isEqualTo(2);
            assertThat(page.size()).isEqualTo(15);
            assertThat(page.total()).isEqualTo(100);
            assertThat(page.records()).containsExactly("a", "b");
        }
    }

    @Nested
    @DisplayName("Defensive copy")
    class DefensiveCopyTests {

        @Test
        @DisplayName("modifying source list does not affect Page")
        void sourceListModificationDoesNotAffectPage() {
            List<String> source = new ArrayList<>(List.of("a", "b", "c"));
            Page<String> page = new Page<>(1, 10, 3, source);

            source.add("d");
            source.remove(0);

            assertThat(page.records()).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("records list is unmodifiable")
        void recordsListIsUnmodifiable() {
            Page<String> page = new Page<>(1, 10, 3, List.of("a", "b", "c"));

            assertThatThrownBy(() -> page.records().add("d"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("of() factory")
    class OfTests {

        @Test
        @DisplayName("creates page with given values")
        void createsPage() {
            Page<Integer> page = Page.of(2, 15, 50, List.of(1, 2, 3));
            assertThat(page.current()).isEqualTo(2);
            assertThat(page.size()).isEqualTo(15);
            assertThat(page.total()).isEqualTo(50);
            assertThat(page.records()).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("empty() factory")
    class EmptyTests {

        @Test
        @DisplayName("creates empty page with size")
        void createsEmptyPage() {
            Page<String> page = Page.empty(20);
            assertThat(page.current()).isEqualTo(1);
            assertThat(page.size()).isEqualTo(20);
            assertThat(page.total()).isZero();
            assertThat(page.records()).isEmpty();
        }
    }

    @Nested
    @DisplayName("pages()")
    class PagesTests {

        @Test
        @DisplayName("returns 0 when total is 0")
        void zeroTotal() {
            Page<String> page = Page.of(1, 10, 0, List.of());
            assertThat(page.pages()).isZero();
        }

        @Test
        @DisplayName("computes correctly with exact division")
        void exactDivision() {
            Page<String> page = Page.of(1, 10, 100, List.of());
            assertThat(page.pages()).isEqualTo(10);
        }

        @Test
        @DisplayName("computes correctly with remainder")
        void withRemainder() {
            Page<String> page = Page.of(1, 10, 101, List.of());
            assertThat(page.pages()).isEqualTo(11);
        }

        @Test
        @DisplayName("single item with size 1")
        void singleItem() {
            Page<String> page = Page.of(1, 1, 1, List.of("a"));
            assertThat(page.pages()).isEqualTo(1);
        }

        @Test
        @DisplayName("off-by-one: total equals size")
        void totalEqualsSize() {
            Page<String> page = Page.of(1, 5, 5, List.of());
            assertThat(page.pages()).isEqualTo(1);
        }

        @Test
        @DisplayName("off-by-one: total is size + 1")
        void totalIsSizePlusOne() {
            Page<String> page = Page.of(1, 5, 6, List.of());
            assertThat(page.pages()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("hasNext()")
    class HasNextTests {

        @Test
        @DisplayName("returns true when more pages exist")
        void trueWhenMorePages() {
            Page<String> page = Page.of(1, 10, 25, List.of());
            assertThat(page.hasNext()).isTrue();
        }

        @Test
        @DisplayName("returns false on last page")
        void falseOnLastPage() {
            Page<String> page = Page.of(3, 10, 25, List.of());
            assertThat(page.hasNext()).isFalse();
        }

        @Test
        @DisplayName("returns false when no records")
        void falseWhenEmpty() {
            Page<String> page = Page.of(1, 10, 0, List.of());
            assertThat(page.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasPrevious()")
    class HasPreviousTests {

        @Test
        @DisplayName("returns false on first page")
        void falseOnFirst() {
            Page<String> page = Page.of(1, 10, 50, List.of());
            assertThat(page.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("returns true on subsequent pages")
        void trueOnSubsequent() {
            Page<String> page = Page.of(2, 10, 50, List.of());
            assertThat(page.hasPrevious()).isTrue();
        }
    }

    @Nested
    @DisplayName("offset()")
    class OffsetTests {

        @Test
        @DisplayName("computes offset correctly")
        void computesOffset() {
            assertThat(Page.of(1, 10, 0, List.of()).offset()).isZero();
            assertThat(Page.of(2, 10, 20, List.of()).offset()).isEqualTo(10);
            assertThat(Page.of(3, 25, 100, List.of()).offset()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("map()")
    class MapTests {

        @Test
        @DisplayName("maps records and preserves metadata")
        void mapsRecords() {
            Page<String> page = Page.of(2, 10, 50, List.of("1", "2", "3"));
            Page<Integer> mapped = page.map(Integer::parseInt);

            assertThat(mapped.current()).isEqualTo(2);
            assertThat(mapped.size()).isEqualTo(10);
            assertThat(mapped.total()).isEqualTo(50);
            assertThat(mapped.records()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("rejects null mapper")
        void rejectsNullMapper() {
            Page<String> page = Page.of(1, 10, 0, List.of());
            assertThatThrownBy(() -> page.map(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("mapper must not be null");
        }

        @Test
        @DisplayName("maps empty page to empty page")
        void mapsEmptyPage() {
            Page<String> page = Page.empty(10);
            Page<Integer> mapped = page.map(Integer::parseInt);
            assertThat(mapped.records()).isEmpty();
            assertThat(mapped.total()).isZero();
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equal pages are equal")
        void equalPages() {
            Page<String> a = Page.of(1, 10, 5, List.of("a", "b"));
            Page<String> b = Page.of(1, 10, 5, List.of("a", "b"));
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("different pages are not equal")
        void differentPages() {
            Page<String> a = Page.of(1, 10, 5, List.of("a"));
            Page<String> b = Page.of(2, 10, 5, List.of("a"));
            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTests {

        @Test
        @DisplayName("includes all fields")
        void includesAllFields() {
            Page<String> page = Page.of(2, 10, 50, List.of("a", "b"));
            String str = page.toString();
            assertThat(str).contains("current=2", "size=10", "total=50", "pages=5", "records=2");
        }
    }
}
