package cloud.opencode.base.web.page;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PageResultTest Tests
 * PageResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("PageResult Tests")
class PageResultTest {

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("of with items and page info should create page result")
        void ofWithItemsAndPageInfoShouldCreatePageResult() {
            List<String> items = List.of("a", "b", "c");
            PageInfo pageInfo = PageInfo.of(1, 10, 3);

            PageResult<String> result = PageResult.of(items, pageInfo);

            assertThat(result.items()).containsExactly("a", "b", "c");
            assertThat(result.pageInfo()).isEqualTo(pageInfo);
        }

        @Test
        @DisplayName("of with items, total, page and size should create page result")
        void ofWithItemsTotalPageAndSizeShouldCreatePageResult() {
            List<String> items = List.of("a", "b");

            PageResult<String> result = PageResult.of(items, 100, 1, 10);

            assertThat(result.items()).hasSize(2);
            assertThat(result.getTotal()).isEqualTo(100);
            assertThat(result.getPage()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("of with page request should create page result")
        void ofWithPageRequestShouldCreatePageResult() {
            List<String> items = List.of("a", "b");
            PageRequest request = PageRequest.of(2, 20);

            PageResult<String> result = PageResult.of(items, 100, request);

            assertThat(result.getPage()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("empty should create empty page result")
        void emptyShouldCreateEmptyPageResult() {
            PageResult<String> result = PageResult.empty();

            assertThat(result.items()).isEmpty();
            assertThat(result.getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("empty with page and size should create empty page result")
        void emptyWithPageAndSizeShouldCreateEmptyPageResult() {
            PageResult<String> result = PageResult.empty(3, 20);

            assertThat(result.items()).isEmpty();
            assertThat(result.getPage()).isEqualTo(3);
            assertThat(result.getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("single should create single item result")
        void singleShouldCreateSingleItemResult() {
            PageResult<String> result = PageResult.single("item");

            assertThat(result.items()).containsExactly("item");
            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getPage()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("single with null should return empty result")
        void singleWithNullShouldReturnEmptyResult() {
            PageResult<String> result = PageResult.single((String) null);

            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("single with list should take first item")
        void singleWithListShouldTakeFirstItem() {
            List<String> list = List.of("first", "second", "third");

            PageResult<String> result = PageResult.single(list);

            assertThat(result.items()).containsExactly("first");
        }

        @Test
        @DisplayName("single with empty list should return empty result")
        void singleWithEmptyListShouldReturnEmptyResult() {
            PageResult<String> result = PageResult.single(List.of());

            assertThat(result.items()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Compact Constructor Tests")
    class CompactConstructorTests {

        @Test
        @DisplayName("should handle null items")
        void shouldHandleNullItems() {
            PageResult<String> result = new PageResult<>(null, PageInfo.of(1, 10, 0));

            assertThat(result.items()).isEmpty();
        }

        @Test
        @DisplayName("should handle null page info")
        void shouldHandleNullPageInfo() {
            PageResult<String> result = new PageResult<>(List.of("a"), null);

            assertThat(result.pageInfo()).isNotNull();
        }

        @Test
        @DisplayName("should create immutable items list")
        void shouldCreateImmutableItemsList() {
            List<String> items = new java.util.ArrayList<>();
            items.add("a");
            PageResult<String> result = new PageResult<>(items, PageInfo.of(1, 10, 1));

            assertThatThrownBy(() -> result.items().add("b"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Delegate Methods Tests")
    class DelegateMethodsTests {

        @Test
        @DisplayName("getPage should return page from page info")
        void getPageShouldReturnPageFromPageInfo() {
            PageResult<String> result = PageResult.of(List.of("a"), 10, 3, 5);

            assertThat(result.getPage()).isEqualTo(3);
        }

        @Test
        @DisplayName("getSize should return size from page info")
        void getSizeShouldReturnSizeFromPageInfo() {
            PageResult<String> result = PageResult.of(List.of("a"), 10, 1, 20);

            assertThat(result.getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("getTotal should return total from page info")
        void getTotalShouldReturnTotalFromPageInfo() {
            PageResult<String> result = PageResult.of(List.of("a"), 100, 1, 10);

            assertThat(result.getTotal()).isEqualTo(100);
        }

        @Test
        @DisplayName("getTotalPages should return total pages from page info")
        void getTotalPagesShouldReturnTotalPagesFromPageInfo() {
            PageResult<String> result = PageResult.of(List.of("a"), 100, 1, 10);

            assertThat(result.getTotalPages()).isEqualTo(10);
        }

        @Test
        @DisplayName("hasNext should delegate to page info")
        void hasNextShouldDelegateToPageInfo() {
            PageResult<String> result = PageResult.of(List.of("a"), 100, 1, 10);

            assertThat(result.hasNext()).isTrue();
        }

        @Test
        @DisplayName("hasPrevious should delegate to page info")
        void hasPreviousShouldDelegateToPageInfo() {
            PageResult<String> result = PageResult.of(List.of("a"), 100, 2, 10);

            assertThat(result.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("isFirst should delegate to page info")
        void isFirstShouldDelegateToPageInfo() {
            PageResult<String> result = PageResult.of(List.of("a"), 100, 1, 10);

            assertThat(result.isFirst()).isTrue();
        }

        @Test
        @DisplayName("isLast should delegate to page info")
        void isLastShouldDelegateToPageInfo() {
            PageResult<String> result = PageResult.of(List.of("a"), 10, 1, 10);

            assertThat(result.isLast()).isTrue();
        }
    }

    @Nested
    @DisplayName("Item Access Tests")
    class ItemAccessTests {

        @Test
        @DisplayName("isEmpty should return true for empty items")
        void isEmptyShouldReturnTrueForEmptyItems() {
            PageResult<String> result = PageResult.empty();

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty should return false for non-empty items")
        void isEmptyShouldReturnFalseForNonEmptyItems() {
            PageResult<String> result = PageResult.of(List.of("a"), 1, 1, 10);

            assertThat(result.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("getCount should return item count")
        void getCountShouldReturnItemCount() {
            PageResult<String> result = PageResult.of(List.of("a", "b", "c"), 100, 1, 10);

            assertThat(result.getCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("getFirst should return first item")
        void getFirstShouldReturnFirstItem() {
            PageResult<String> result = PageResult.of(List.of("first", "second"), 2, 1, 10);

            assertThat(result.getFirst()).isEqualTo("first");
        }

        @Test
        @DisplayName("getFirst should return null for empty result")
        void getFirstShouldReturnNullForEmptyResult() {
            PageResult<String> result = PageResult.empty();

            assertThat(result.getFirst()).isNull();
        }

        @Test
        @DisplayName("getLast should return last item")
        void getLastShouldReturnLastItem() {
            PageResult<String> result = PageResult.of(List.of("first", "second", "last"), 3, 1, 10);

            assertThat(result.getLast()).isEqualTo("last");
        }

        @Test
        @DisplayName("getLast should return null for empty result")
        void getLastShouldReturnNullForEmptyResult() {
            PageResult<String> result = PageResult.empty();

            assertThat(result.getLast()).isNull();
        }
    }

    @Nested
    @DisplayName("Map Operations Tests")
    class MapOperationsTests {

        @Test
        @DisplayName("map should transform items")
        void mapShouldTransformItems() {
            PageResult<Integer> result = PageResult.of(List.of(1, 2, 3), 3, 1, 10);

            PageResult<String> mapped = result.map(i -> "Value: " + i);

            assertThat(mapped.items()).containsExactly("Value: 1", "Value: 2", "Value: 3");
            assertThat(mapped.getTotal()).isEqualTo(3);
        }

        @Test
        @DisplayName("mapList should transform entire list")
        void mapListShouldTransformEntireList() {
            PageResult<Integer> result = PageResult.of(List.of(1, 2, 3), 3, 1, 10);

            PageResult<Integer> mapped = result.mapList(list -> list.stream()
                .filter(i -> i > 1)
                .toList());

            assertThat(mapped.items()).containsExactly(2, 3);
        }
    }
}
