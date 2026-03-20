package cloud.opencode.base.core.page;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link PageRequest}.
 */
@DisplayName("PageRequest Tests")
class PageRequestTest {

    @Nested
    @DisplayName("Constructor validation")
    class ConstructorTests {

        @Test
        @DisplayName("throws on page < 1")
        void throwsOnInvalidPage() {
            assertThatThrownBy(() -> new PageRequest(0, 10, Sort.unsorted()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page must be >= 1");
        }

        @Test
        @DisplayName("throws on size < 1")
        void throwsOnInvalidSize() {
            assertThatThrownBy(() -> new PageRequest(1, 0, Sort.unsorted()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Size must be >= 1");
        }

        @Test
        @DisplayName("null sort defaults to unsorted")
        void nullSortDefaults() {
            PageRequest req = new PageRequest(1, 10, null);
            assertThat(req.sort()).isEqualTo(Sort.unsorted());
            assertThat(req.sort().isUnsorted()).isTrue();
        }

        @Test
        @DisplayName("valid parameters are accepted")
        void validParameters() {
            PageRequest req = new PageRequest(2, 20, Sort.by("name"));
            assertThat(req.page()).isEqualTo(2);
            assertThat(req.size()).isEqualTo(20);
            assertThat(req.sort().isUnsorted()).isFalse();
        }
    }

    @Nested
    @DisplayName("of(page, size)")
    class OfPageSizeTests {

        @Test
        @DisplayName("creates request with unsorted")
        void createsWithUnsorted() {
            PageRequest req = PageRequest.of(1, 10);
            assertThat(req.page()).isEqualTo(1);
            assertThat(req.size()).isEqualTo(10);
            assertThat(req.sort().isUnsorted()).isTrue();
        }
    }

    @Nested
    @DisplayName("of(page, size, sort)")
    class OfPageSizeSortTests {

        @Test
        @DisplayName("creates request with sort")
        void createsWithSort() {
            Sort sort = Sort.by("name");
            PageRequest req = PageRequest.of(3, 15, sort);
            assertThat(req.page()).isEqualTo(3);
            assertThat(req.size()).isEqualTo(15);
            assertThat(req.sort()).isEqualTo(sort);
        }
    }

    @Nested
    @DisplayName("ofSize()")
    class OfSizeTests {

        @Test
        @DisplayName("creates first page with given size")
        void createsFirstPage() {
            PageRequest req = PageRequest.ofSize(25);
            assertThat(req.page()).isEqualTo(1);
            assertThat(req.size()).isEqualTo(25);
            assertThat(req.sort().isUnsorted()).isTrue();
        }
    }

    @Nested
    @DisplayName("getOffset()")
    class GetOffsetTests {

        @Test
        @DisplayName("computes offset from page and size")
        void computesOffset() {
            assertThat(PageRequest.of(1, 10).getOffset()).isZero();
            assertThat(PageRequest.of(2, 10).getOffset()).isEqualTo(10);
            assertThat(PageRequest.of(5, 20).getOffset()).isEqualTo(80);
        }
    }

    @Nested
    @DisplayName("isFirst()")
    class IsFirstTests {

        @Test
        @DisplayName("returns true for page 1")
        void trueForPage1() {
            assertThat(PageRequest.of(1, 10).isFirst()).isTrue();
        }

        @Test
        @DisplayName("returns false for page > 1")
        void falseForLaterPages() {
            assertThat(PageRequest.of(2, 10).isFirst()).isFalse();
        }
    }

    @Nested
    @DisplayName("next()")
    class NextTests {

        @Test
        @DisplayName("increments page by 1 and preserves sort")
        void incrementsPage() {
            Sort sort = Sort.by("name");
            PageRequest req = PageRequest.of(1, 10, sort);
            PageRequest next = req.next();
            assertThat(next.page()).isEqualTo(2);
            assertThat(next.size()).isEqualTo(10);
            assertThat(next.sort()).isEqualTo(sort);
        }
    }

    @Nested
    @DisplayName("previous()")
    class PreviousTests {

        @Test
        @DisplayName("decrements page when not first")
        void decrementsPage() {
            PageRequest req = PageRequest.of(3, 10);
            PageRequest prev = req.previous();
            assertThat(prev.page()).isEqualTo(2);
        }

        @Test
        @DisplayName("returns same instance when already first")
        void returnsSameWhenFirst() {
            PageRequest req = PageRequest.of(1, 10);
            PageRequest prev = req.previous();
            assertThat(prev).isSameAs(req);
        }
    }

    @Nested
    @DisplayName("first()")
    class FirstTests {

        @Test
        @DisplayName("returns page 1 request")
        void returnsFirstPage() {
            PageRequest req = PageRequest.of(5, 10, Sort.by("name"));
            PageRequest first = req.first();
            assertThat(first.page()).isEqualTo(1);
            assertThat(first.size()).isEqualTo(10);
            assertThat(first.sort()).isEqualTo(Sort.by("name"));
        }

        @Test
        @DisplayName("returns same instance when already first")
        void returnsSameWhenFirst() {
            PageRequest req = PageRequest.of(1, 10);
            assertThat(req.first()).isSameAs(req);
        }
    }

    @Nested
    @DisplayName("withSort()")
    class WithSortTests {

        @Test
        @DisplayName("returns new request with different sort")
        void changesSort() {
            PageRequest req = PageRequest.of(3, 10);
            Sort newSort = Sort.by(Sort.Direction.DESC, "age");
            PageRequest result = req.withSort(newSort);
            assertThat(result.page()).isEqualTo(3);
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.sort()).isEqualTo(newSort);
        }
    }

    @Nested
    @DisplayName("withPage()")
    class WithPageTests {

        @Test
        @DisplayName("returns new request with different page")
        void changesPage() {
            Sort sort = Sort.by("name");
            PageRequest req = PageRequest.of(1, 10, sort);
            PageRequest result = req.withPage(7);
            assertThat(result.page()).isEqualTo(7);
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.sort()).isEqualTo(sort);
        }
    }

    @Nested
    @DisplayName("toPage()")
    class ToPageTests {

        @Test
        @DisplayName("creates a Page with matching current and size")
        void createsPage() {
            PageRequest req = PageRequest.of(3, 15);
            Page<String> page = req.toPage();
            assertThat(page.getCurrent()).isEqualTo(3);
            assertThat(page.getSize()).isEqualTo(15);
            assertThat(page.getTotal()).isZero();
            assertThat(page.getRecords()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTests {

        @Test
        @DisplayName("includes page and size")
        void includesBasicFields() {
            String str = PageRequest.of(2, 10).toString();
            assertThat(str).contains("page=2", "size=10");
        }

        @Test
        @DisplayName("omits sort when unsorted")
        void omitsSortWhenUnsorted() {
            String str = PageRequest.of(1, 10).toString();
            assertThat(str).doesNotContain("sort=");
        }

        @Test
        @DisplayName("includes sort when sorted")
        void includesSortWhenSorted() {
            String str = PageRequest.of(1, 10, Sort.by("name")).toString();
            assertThat(str).contains("sort=");
        }
    }

    @Nested
    @DisplayName("Record equality")
    class EqualityTests {

        @Test
        @DisplayName("equal records are equal")
        void equalRecords() {
            PageRequest r1 = PageRequest.of(1, 10);
            PageRequest r2 = PageRequest.of(1, 10);
            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("different records are not equal")
        void differentRecords() {
            assertThat(PageRequest.of(1, 10)).isNotEqualTo(PageRequest.of(2, 10));
        }
    }
}
