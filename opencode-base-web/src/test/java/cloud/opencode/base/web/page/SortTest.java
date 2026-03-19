package cloud.opencode.base.web.page;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SortTest Tests
 * SortTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("Sort Tests")
class SortTest {

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("unsorted should create empty sort")
        void unsortedShouldCreateEmptySort() {
            Sort sort = Sort.unsorted();

            assertThat(sort.orders()).isEmpty();
            assertThat(sort.isUnsorted()).isTrue();
        }

        @Test
        @DisplayName("asc should create ascending sort")
        void ascShouldCreateAscendingSort() {
            Sort sort = Sort.asc("name");

            assertThat(sort.orders()).hasSize(1);
            assertThat(sort.getFirst().property()).isEqualTo("name");
            assertThat(sort.getFirst().isAscending()).isTrue();
        }

        @Test
        @DisplayName("desc should create descending sort")
        void descShouldCreateDescendingSort() {
            Sort sort = Sort.desc("createdAt");

            assertThat(sort.orders()).hasSize(1);
            assertThat(sort.getFirst().property()).isEqualTo("createdAt");
            assertThat(sort.getFirst().isDescending()).isTrue();
        }

        @Test
        @DisplayName("by with varargs should create sort with multiple orders")
        void byWithVarargsShouldCreateSortWithMultipleOrders() {
            Sort sort = Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.desc("createdAt")
            );

            assertThat(sort.orders()).hasSize(2);
        }

        @Test
        @DisplayName("by with list should create sort with orders")
        void byWithListShouldCreateSortWithOrders() {
            List<Sort.Order> orders = List.of(
                Sort.Order.asc("name"),
                Sort.Order.desc("date")
            );

            Sort sort = Sort.by(orders);

            assertThat(sort.orders()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Parse Tests")
    class ParseTests {

        @Test
        @DisplayName("parse should parse simple sort string")
        void parseShouldParseSimpleSortString() {
            Sort sort = Sort.parse("name,asc");

            assertThat(sort.orders()).hasSize(1);
            assertThat(sort.getFirst().property()).isEqualTo("name");
            assertThat(sort.getFirst().isAscending()).isTrue();
        }

        @Test
        @DisplayName("parse should parse descending sort string")
        void parseShouldParseDescendingSortString() {
            Sort sort = Sort.parse("createdAt,desc");

            assertThat(sort.getFirst().property()).isEqualTo("createdAt");
            assertThat(sort.getFirst().isDescending()).isTrue();
        }

        @Test
        @DisplayName("parse should parse multiple sort orders")
        void parseShouldParseMultipleSortOrders() {
            Sort sort = Sort.parse("name,asc;createdAt,desc");

            assertThat(sort.orders()).hasSize(2);
            assertThat(sort.orders().get(0).property()).isEqualTo("name");
            assertThat(sort.orders().get(1).property()).isEqualTo("createdAt");
        }

        @Test
        @DisplayName("parse should default to ascending for missing direction")
        void parseShouldDefaultToAscendingForMissingDirection() {
            Sort sort = Sort.parse("name");

            assertThat(sort.getFirst().isAscending()).isTrue();
        }

        @Test
        @DisplayName("parse should return unsorted for null string")
        void parseShouldReturnUnsortedForNullString() {
            Sort sort = Sort.parse(null);

            assertThat(sort.isUnsorted()).isTrue();
        }

        @Test
        @DisplayName("parse should return unsorted for blank string")
        void parseShouldReturnUnsortedForBlankString() {
            Sort sort = Sort.parse("   ");

            assertThat(sort.isUnsorted()).isTrue();
        }
    }

    @Nested
    @DisplayName("Chaining Methods Tests")
    class ChainingMethodsTests {

        @Test
        @DisplayName("and should add order to sort")
        void andShouldAddOrderToSort() {
            Sort sort = Sort.asc("name").and(Sort.Order.desc("date"));

            assertThat(sort.orders()).hasSize(2);
        }

        @Test
        @DisplayName("andAsc should add ascending order")
        void andAscShouldAddAscendingOrder() {
            Sort sort = Sort.desc("date").andAsc("name");

            assertThat(sort.orders()).hasSize(2);
            assertThat(sort.orders().get(1).isAscending()).isTrue();
        }

        @Test
        @DisplayName("andDesc should add descending order")
        void andDescShouldAddDescendingOrder() {
            Sort sort = Sort.asc("name").andDesc("date");

            assertThat(sort.orders()).hasSize(2);
            assertThat(sort.orders().get(1).isDescending()).isTrue();
        }
    }

    @Nested
    @DisplayName("Query Methods Tests")
    class QueryMethodsTests {

        @Test
        @DisplayName("isSorted should return true for sorted")
        void isSortedShouldReturnTrueForSorted() {
            Sort sort = Sort.asc("name");

            assertThat(sort.isSorted()).isTrue();
        }

        @Test
        @DisplayName("isSorted should return false for unsorted")
        void isSortedShouldReturnFalseForUnsorted() {
            Sort sort = Sort.unsorted();

            assertThat(sort.isSorted()).isFalse();
        }

        @Test
        @DisplayName("isUnsorted should return true for empty sort")
        void isUnsortedShouldReturnTrueForEmptySort() {
            Sort sort = Sort.unsorted();

            assertThat(sort.isUnsorted()).isTrue();
        }

        @Test
        @DisplayName("isUnsorted should return false for sorted")
        void isUnsortedShouldReturnFalseForSorted() {
            Sort sort = Sort.asc("name");

            assertThat(sort.isUnsorted()).isFalse();
        }

        @Test
        @DisplayName("getFirst should return first order")
        void getFirstShouldReturnFirstOrder() {
            Sort sort = Sort.by(Sort.Order.asc("first"), Sort.Order.desc("second"));

            assertThat(sort.getFirst().property()).isEqualTo("first");
        }

        @Test
        @DisplayName("getFirst should return null for unsorted")
        void getFirstShouldReturnNullForUnsorted() {
            Sort sort = Sort.unsorted();

            assertThat(sort.getFirst()).isNull();
        }
    }

    @Nested
    @DisplayName("SQL Conversion Tests")
    class SqlConversionTests {

        @Test
        @DisplayName("toSql should return empty string for unsorted")
        void toSqlShouldReturnEmptyStringForUnsorted() {
            Sort sort = Sort.unsorted();

            assertThat(sort.toSql()).isEmpty();
        }

        @Test
        @DisplayName("toSql should return single order SQL")
        void toSqlShouldReturnSingleOrderSql() {
            Sort sort = Sort.asc("name");

            assertThat(sort.toSql()).isEqualTo("name ASC");
        }

        @Test
        @DisplayName("toSql should return multiple orders SQL")
        void toSqlShouldReturnMultipleOrdersSql() {
            Sort sort = Sort.by(Sort.Order.asc("name"), Sort.Order.desc("date"));

            assertThat(sort.toSql()).isEqualTo("name ASC, date DESC");
        }
    }

    @Nested
    @DisplayName("Order Tests")
    class OrderTests {

        @Test
        @DisplayName("Order.asc should create ascending order")
        void orderAscShouldCreateAscendingOrder() {
            Sort.Order order = Sort.Order.asc("name");

            assertThat(order.property()).isEqualTo("name");
            assertThat(order.direction()).isEqualTo(Sort.Direction.ASC);
            assertThat(order.isAscending()).isTrue();
            assertThat(order.isDescending()).isFalse();
        }

        @Test
        @DisplayName("Order.desc should create descending order")
        void orderDescShouldCreateDescendingOrder() {
            Sort.Order order = Sort.Order.desc("date");

            assertThat(order.property()).isEqualTo("date");
            assertThat(order.direction()).isEqualTo(Sort.Direction.DESC);
            assertThat(order.isAscending()).isFalse();
            assertThat(order.isDescending()).isTrue();
        }
    }

    @Nested
    @DisplayName("Direction Tests")
    class DirectionTests {

        @Test
        @DisplayName("Direction enum should have ASC and DESC values")
        void directionEnumShouldHaveAscAndDescValues() {
            assertThat(Sort.Direction.values()).containsExactly(Sort.Direction.ASC, Sort.Direction.DESC);
        }
    }

    @Nested
    @DisplayName("Compact Constructor Tests")
    class CompactConstructorTests {

        @Test
        @DisplayName("should handle null orders list")
        void shouldHandleNullOrdersList() {
            Sort sort = new Sort(null);

            assertThat(sort.orders()).isEmpty();
        }

        @Test
        @DisplayName("should create immutable orders list")
        void shouldCreateImmutableOrdersList() {
            List<Sort.Order> orders = new java.util.ArrayList<>();
            orders.add(Sort.Order.asc("name"));
            Sort sort = new Sort(orders);

            assertThatThrownBy(() -> sort.orders().add(Sort.Order.desc("date")))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
