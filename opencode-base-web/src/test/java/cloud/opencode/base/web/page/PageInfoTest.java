package cloud.opencode.base.web.page;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * PageInfoTest Tests
 * PageInfoTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("PageInfo Tests")
class PageInfoTest {

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("of should create page info with calculated total pages")
        void ofShouldCreatePageInfoWithCalculatedTotalPages() {
            PageInfo pageInfo = PageInfo.of(1, 10, 95);

            assertThat(pageInfo.page()).isEqualTo(1);
            assertThat(pageInfo.size()).isEqualTo(10);
            assertThat(pageInfo.total()).isEqualTo(95);
            assertThat(pageInfo.totalPages()).isEqualTo(10);
        }

        @Test
        @DisplayName("from should create page info from page request")
        void fromShouldCreatePageInfoFromPageRequest() {
            PageRequest request = PageRequest.of(2, 20);

            PageInfo pageInfo = PageInfo.from(request, 100);

            assertThat(pageInfo.page()).isEqualTo(2);
            assertThat(pageInfo.size()).isEqualTo(20);
            assertThat(pageInfo.total()).isEqualTo(100);
        }

        @Test
        @DisplayName("empty should create empty page info")
        void emptyShouldCreateEmptyPageInfo() {
            PageInfo pageInfo = PageInfo.empty();

            assertThat(pageInfo.page()).isEqualTo(1);
            assertThat(pageInfo.size()).isEqualTo(10);
            assertThat(pageInfo.total()).isEqualTo(0);
            assertThat(pageInfo.totalPages()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Compact Constructor Tests")
    class CompactConstructorTests {

        @Test
        @DisplayName("should normalize negative page to 1")
        void shouldNormalizeNegativePageTo1() {
            PageInfo pageInfo = new PageInfo(-5, 10, 100, 0);

            assertThat(pageInfo.page()).isEqualTo(1);
        }

        @Test
        @DisplayName("should normalize zero page to 1")
        void shouldNormalizeZeroPageTo1() {
            PageInfo pageInfo = new PageInfo(0, 10, 100, 0);

            assertThat(pageInfo.page()).isEqualTo(1);
        }

        @Test
        @DisplayName("should normalize negative size to 1")
        void shouldNormalizeNegativeSizeTo1() {
            PageInfo pageInfo = new PageInfo(1, -10, 100, 0);

            assertThat(pageInfo.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("should normalize negative total to 0")
        void shouldNormalizeNegativeTotalTo0() {
            PageInfo pageInfo = new PageInfo(1, 10, -50, 0);

            assertThat(pageInfo.total()).isEqualTo(0);
        }

        @Test
        @DisplayName("should calculate total pages correctly")
        void shouldCalculateTotalPagesCorrectly() {
            PageInfo pageInfo = new PageInfo(1, 10, 95, 0);

            assertThat(pageInfo.totalPages()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Navigation Methods Tests")
    class NavigationMethodsTests {

        @Test
        @DisplayName("hasNext should return true when not on last page")
        void hasNextShouldReturnTrueWhenNotOnLastPage() {
            PageInfo pageInfo = PageInfo.of(1, 10, 50);

            assertThat(pageInfo.hasNext()).isTrue();
        }

        @Test
        @DisplayName("hasNext should return false when on last page")
        void hasNextShouldReturnFalseWhenOnLastPage() {
            PageInfo pageInfo = PageInfo.of(5, 10, 50);

            assertThat(pageInfo.hasNext()).isFalse();
        }

        @Test
        @DisplayName("hasPrevious should return true when not on first page")
        void hasPreviousShouldReturnTrueWhenNotOnFirstPage() {
            PageInfo pageInfo = PageInfo.of(2, 10, 50);

            assertThat(pageInfo.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("hasPrevious should return false when on first page")
        void hasPreviousShouldReturnFalseWhenOnFirstPage() {
            PageInfo pageInfo = PageInfo.of(1, 10, 50);

            assertThat(pageInfo.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("isFirst should return true for first page")
        void isFirstShouldReturnTrueForFirstPage() {
            PageInfo pageInfo = PageInfo.of(1, 10, 50);

            assertThat(pageInfo.isFirst()).isTrue();
        }

        @Test
        @DisplayName("isFirst should return false for non-first page")
        void isFirstShouldReturnFalseForNonFirstPage() {
            PageInfo pageInfo = PageInfo.of(2, 10, 50);

            assertThat(pageInfo.isFirst()).isFalse();
        }

        @Test
        @DisplayName("isLast should return true for last page")
        void isLastShouldReturnTrueForLastPage() {
            PageInfo pageInfo = PageInfo.of(5, 10, 50);

            assertThat(pageInfo.isLast()).isTrue();
        }

        @Test
        @DisplayName("isLast should return false for non-last page")
        void isLastShouldReturnFalseForNonLastPage() {
            PageInfo pageInfo = PageInfo.of(4, 10, 50);

            assertThat(pageInfo.isLast()).isFalse();
        }
    }

    @Nested
    @DisplayName("Empty Check Tests")
    class EmptyCheckTests {

        @Test
        @DisplayName("isEmpty should return true when total is 0")
        void isEmptyShouldReturnTrueWhenTotalIs0() {
            PageInfo pageInfo = PageInfo.of(1, 10, 0);

            assertThat(pageInfo.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty should return false when total is greater than 0")
        void isEmptyShouldReturnFalseWhenTotalIsGreaterThan0() {
            PageInfo pageInfo = PageInfo.of(1, 10, 50);

            assertThat(pageInfo.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("Offset Calculation Tests")
    class OffsetCalculationTests {

        @Test
        @DisplayName("getOffset should return correct offset for first page")
        void getOffsetShouldReturnCorrectOffsetForFirstPage() {
            PageInfo pageInfo = PageInfo.of(1, 10, 50);

            assertThat(pageInfo.getOffset()).isEqualTo(0);
        }

        @Test
        @DisplayName("getOffset should return correct offset for subsequent pages")
        void getOffsetShouldReturnCorrectOffsetForSubsequentPages() {
            PageInfo pageInfo = PageInfo.of(3, 10, 50);

            assertThat(pageInfo.getOffset()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Page Number Methods Tests")
    class PageNumberMethodsTests {

        @Test
        @DisplayName("getNextPage should return next page number")
        void getNextPageShouldReturnNextPageNumber() {
            PageInfo pageInfo = PageInfo.of(3, 10, 100);

            assertThat(pageInfo.getNextPage()).isEqualTo(4);
        }

        @Test
        @DisplayName("getNextPage should return current page when on last page")
        void getNextPageShouldReturnCurrentPageWhenOnLastPage() {
            PageInfo pageInfo = PageInfo.of(10, 10, 100);

            assertThat(pageInfo.getNextPage()).isEqualTo(10);
        }

        @Test
        @DisplayName("getPreviousPage should return previous page number")
        void getPreviousPageShouldReturnPreviousPageNumber() {
            PageInfo pageInfo = PageInfo.of(3, 10, 100);

            assertThat(pageInfo.getPreviousPage()).isEqualTo(2);
        }

        @Test
        @DisplayName("getPreviousPage should return current page when on first page")
        void getPreviousPageShouldReturnCurrentPageWhenOnFirstPage() {
            PageInfo pageInfo = PageInfo.of(1, 10, 100);

            assertThat(pageInfo.getPreviousPage()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("End Offset Tests")
    class EndOffsetTests {

        @Test
        @DisplayName("getEndOffset should return correct end offset")
        void getEndOffsetShouldReturnCorrectEndOffset() {
            PageInfo pageInfo = PageInfo.of(1, 10, 100);

            assertThat(pageInfo.getEndOffset()).isEqualTo(10);
        }

        @Test
        @DisplayName("getEndOffset should not exceed total")
        void getEndOffsetShouldNotExceedTotal() {
            PageInfo pageInfo = PageInfo.of(10, 10, 95);

            assertThat(pageInfo.getEndOffset()).isEqualTo(95);
        }
    }

    @Nested
    @DisplayName("Current Page Size Tests")
    class CurrentPageSizeTests {

        @Test
        @DisplayName("getCurrentPageSize should return full page size for full page")
        void getCurrentPageSizeShouldReturnFullPageSizeForFullPage() {
            PageInfo pageInfo = PageInfo.of(1, 10, 100);

            assertThat(pageInfo.getCurrentPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("getCurrentPageSize should return remaining items for last page")
        void getCurrentPageSizeShouldReturnRemainingItemsForLastPage() {
            PageInfo pageInfo = PageInfo.of(10, 10, 95);

            assertThat(pageInfo.getCurrentPageSize()).isEqualTo(5);
        }
    }
}
