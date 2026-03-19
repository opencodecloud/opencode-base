package cloud.opencode.base.web.page;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * PageRequestTest Tests
 * PageRequestTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("PageRequest Tests")
class PageRequestTest {

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("of should create page request with page and size")
        void ofShouldCreatePageRequestWithPageAndSize() {
            PageRequest request = PageRequest.of(1, 20);

            assertThat(request.page()).isEqualTo(1);
            assertThat(request.size()).isEqualTo(20);
            assertThat(request.sort().isUnsorted()).isTrue();
        }

        @Test
        @DisplayName("of should create page request with sort")
        void ofShouldCreatePageRequestWithSort() {
            Sort sort = Sort.asc("name");

            PageRequest request = PageRequest.of(1, 20, sort);

            assertThat(request.sort()).isEqualTo(sort);
        }

        @Test
        @DisplayName("of should create page request with sort string")
        void ofShouldCreatePageRequestWithSortString() {
            PageRequest request = PageRequest.of(1, 20, "createdAt", "desc");

            assertThat(request.hasSort()).isTrue();
            assertThat(request.sort().getFirst().property()).isEqualTo("createdAt");
            assertThat(request.sort().getFirst().isDescending()).isTrue();
        }

        @Test
        @DisplayName("of should create unsorted request for blank sort field")
        void ofShouldCreateUnsortedRequestForBlankSortField() {
            PageRequest request = PageRequest.of(1, 20, "", "asc");

            assertThat(request.hasSort()).isFalse();
        }

        @Test
        @DisplayName("of should default to ascending sort")
        void ofShouldDefaultToAscendingSort() {
            PageRequest request = PageRequest.of(1, 20, "name", "invalid");

            assertThat(request.sort().getFirst().isAscending()).isTrue();
        }

        @Test
        @DisplayName("defaultRequest should create request with default values")
        void defaultRequestShouldCreateRequestWithDefaultValues() {
            PageRequest request = PageRequest.defaultRequest();

            assertThat(request.page()).isEqualTo(1);
            assertThat(request.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("first should create first page request")
        void firstShouldCreateFirstPageRequest() {
            PageRequest request = PageRequest.first(25);

            assertThat(request.page()).isEqualTo(1);
            assertThat(request.size()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("Compact Constructor Tests")
    class CompactConstructorTests {

        @Test
        @DisplayName("should normalize negative page to 1")
        void shouldNormalizeNegativePageTo1() {
            PageRequest request = new PageRequest(-5, 10, null);

            assertThat(request.page()).isEqualTo(1);
        }

        @Test
        @DisplayName("should normalize zero page to 1")
        void shouldNormalizeZeroPageTo1() {
            PageRequest request = new PageRequest(0, 10, null);

            assertThat(request.page()).isEqualTo(1);
        }

        @Test
        @DisplayName("should normalize negative size to 1")
        void shouldNormalizeNegativeSizeTo1() {
            PageRequest request = new PageRequest(1, -10, null);

            assertThat(request.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("should cap size at max value")
        void shouldCapSizeAtMaxValue() {
            PageRequest request = new PageRequest(1, 2000, null);

            assertThat(request.size()).isEqualTo(1000);
        }

        @Test
        @DisplayName("should use unsorted for null sort")
        void shouldUseUnsortedForNullSort() {
            PageRequest request = new PageRequest(1, 10, null);

            assertThat(request.sort()).isNotNull();
            assertThat(request.sort().isUnsorted()).isTrue();
        }
    }

    @Nested
    @DisplayName("Offset Calculation Tests")
    class OffsetCalculationTests {

        @Test
        @DisplayName("getOffset should return 0 for first page")
        void getOffsetShouldReturn0ForFirstPage() {
            PageRequest request = PageRequest.of(1, 10);

            assertThat(request.getOffset()).isEqualTo(0);
        }

        @Test
        @DisplayName("getOffset should calculate correct offset")
        void getOffsetShouldCalculateCorrectOffset() {
            PageRequest request = PageRequest.of(3, 10);

            assertThat(request.getOffset()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Sort Check Tests")
    class SortCheckTests {

        @Test
        @DisplayName("hasSort should return true for sorted request")
        void hasSortShouldReturnTrueForSortedRequest() {
            PageRequest request = PageRequest.of(1, 10, Sort.asc("name"));

            assertThat(request.hasSort()).isTrue();
        }

        @Test
        @DisplayName("hasSort should return false for unsorted request")
        void hasSortShouldReturnFalseForUnsortedRequest() {
            PageRequest request = PageRequest.of(1, 10);

            assertThat(request.hasSort()).isFalse();
        }
    }

    @Nested
    @DisplayName("Navigation Methods Tests")
    class NavigationMethodsTests {

        @Test
        @DisplayName("next should return next page request")
        void nextShouldReturnNextPageRequest() {
            PageRequest request = PageRequest.of(3, 10, Sort.asc("name"));

            PageRequest next = request.next();

            assertThat(next.page()).isEqualTo(4);
            assertThat(next.size()).isEqualTo(10);
            assertThat(next.sort()).isEqualTo(request.sort());
        }

        @Test
        @DisplayName("previous should return previous page request")
        void previousShouldReturnPreviousPageRequest() {
            PageRequest request = PageRequest.of(3, 10);

            PageRequest previous = request.previous();

            assertThat(previous.page()).isEqualTo(2);
        }

        @Test
        @DisplayName("previous should not go below page 1")
        void previousShouldNotGoBelowPage1() {
            PageRequest request = PageRequest.of(1, 10);

            PageRequest previous = request.previous();

            assertThat(previous.page()).isEqualTo(1);
        }

        @Test
        @DisplayName("first should return first page request")
        void firstMethodShouldReturnFirstPageRequest() {
            PageRequest request = PageRequest.of(5, 20, Sort.desc("date"));

            PageRequest first = request.first();

            assertThat(first.page()).isEqualTo(1);
            assertThat(first.size()).isEqualTo(20);
            assertThat(first.sort()).isEqualTo(request.sort());
        }
    }

    @Nested
    @DisplayName("With Methods Tests")
    class WithMethodsTests {

        @Test
        @DisplayName("withPage should create request with different page")
        void withPageShouldCreateRequestWithDifferentPage() {
            PageRequest request = PageRequest.of(1, 10);

            PageRequest newRequest = request.withPage(5);

            assertThat(newRequest.page()).isEqualTo(5);
            assertThat(newRequest.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("withSize should create request with different size")
        void withSizeShouldCreateRequestWithDifferentSize() {
            PageRequest request = PageRequest.of(1, 10);

            PageRequest newRequest = request.withSize(50);

            assertThat(newRequest.page()).isEqualTo(1);
            assertThat(newRequest.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("withSort should create request with different sort")
        void withSortShouldCreateRequestWithDifferentSort() {
            PageRequest request = PageRequest.of(1, 10);
            Sort newSort = Sort.desc("createdAt");

            PageRequest newRequest = request.withSort(newSort);

            assertThat(newRequest.sort()).isEqualTo(newSort);
        }

        @Test
        @DisplayName("sortAsc should create request with ascending sort")
        void sortAscShouldCreateRequestWithAscendingSort() {
            PageRequest request = PageRequest.of(1, 10);

            PageRequest newRequest = request.sortAsc("name");

            assertThat(newRequest.sort().getFirst().property()).isEqualTo("name");
            assertThat(newRequest.sort().getFirst().isAscending()).isTrue();
        }

        @Test
        @DisplayName("sortDesc should create request with descending sort")
        void sortDescShouldCreateRequestWithDescendingSort() {
            PageRequest request = PageRequest.of(1, 10);

            PageRequest newRequest = request.sortDesc("date");

            assertThat(newRequest.sort().getFirst().property()).isEqualTo("date");
            assertThat(newRequest.sort().getFirst().isDescending()).isTrue();
        }
    }
}
