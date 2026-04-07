package cloud.opencode.base.core.page;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Sort}.
 */
@DisplayName("Sort Tests")
class SortTest {

    @Nested
    @DisplayName("Direction")
    class DirectionTests {

        @Test
        @DisplayName("reverse() returns opposite direction")
        void reverseReturnsOpposite() {
            assertThat(Sort.Direction.ASC.reverse()).isEqualTo(Sort.Direction.DESC);
            assertThat(Sort.Direction.DESC.reverse()).isEqualTo(Sort.Direction.ASC);
        }

        @Test
        @DisplayName("isAscending() returns correct value")
        void isAscending() {
            assertThat(Sort.Direction.ASC.isAscending()).isTrue();
            assertThat(Sort.Direction.DESC.isAscending()).isFalse();
        }
    }

    @Nested
    @DisplayName("Order")
    class OrderTests {

        @Test
        @DisplayName("asc() creates ascending order")
        void ascCreatesAscOrder() {
            Sort.Order order = Sort.Order.asc("name");
            assertThat(order.property()).isEqualTo("name");
            assertThat(order.direction()).isEqualTo(Sort.Direction.ASC);
        }

        @Test
        @DisplayName("desc() creates descending order")
        void descCreatesDescOrder() {
            Sort.Order order = Sort.Order.desc("age");
            assertThat(order.property()).isEqualTo("age");
            assertThat(order.direction()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("reversed() returns order with opposite direction")
        void reversedReturnsOpposite() {
            Sort.Order asc = Sort.Order.asc("name");
            Sort.Order reversed = asc.reversed();
            assertThat(reversed.property()).isEqualTo("name");
            assertThat(reversed.direction()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("toSql() renders SQL fragment")
        void toSqlRendersSqlFragment() {
            assertThat(Sort.Order.asc("name").toSql()).isEqualTo("name ASC");
            assertThat(Sort.Order.desc("age").toSql()).isEqualTo("age DESC");
        }

        @Test
        @DisplayName("equals and hashCode follow record semantics")
        void equalsAndHashCode() {
            Sort.Order o1 = Sort.Order.asc("name");
            Sort.Order o2 = new Sort.Order("name", Sort.Direction.ASC);
            assertThat(o1).isEqualTo(o2);
            assertThat(o1.hashCode()).isEqualTo(o2.hashCode());
        }
    }

    @Nested
    @DisplayName("Static factory: by(String)")
    class ByStringTests {

        @Test
        @DisplayName("creates ascending sort for single property")
        void createsSingleAscSort() {
            Sort sort = Sort.by("name");
            assertThat(sort.getOrders()).hasSize(1);
            assertThat(sort.getOrders().getFirst().property()).isEqualTo("name");
            assertThat(sort.getOrders().getFirst().direction()).isEqualTo(Sort.Direction.ASC);
        }
    }

    @Nested
    @DisplayName("Static factory: by(Direction, String)")
    class ByDirectionStringTests {

        @Test
        @DisplayName("creates sort with given direction")
        void createsSortWithDirection() {
            Sort sort = Sort.by(Sort.Direction.DESC, "age");
            assertThat(sort.getOrders()).hasSize(1);
            assertThat(sort.getOrders().getFirst().direction()).isEqualTo(Sort.Direction.DESC);
        }
    }

    @Nested
    @DisplayName("Static factory: by(Direction, String...)")
    class ByDirectionVarargsTests {

        @Test
        @DisplayName("creates sort for multiple properties with same direction")
        void createsMultiPropertySort() {
            Sort sort = Sort.by(Sort.Direction.ASC, "name", "age", "email");
            assertThat(sort.getOrders()).hasSize(3);
            assertThat(sort.getOrders()).allMatch(o -> o.direction() == Sort.Direction.ASC);
            assertThat(sort.getOrders()).extracting(Sort.Order::property)
                    .containsExactly("name", "age", "email");
        }
    }

    @Nested
    @DisplayName("Static factory: by(Order...)")
    class ByOrderVarargsTests {

        @Test
        @DisplayName("creates sort from multiple orders")
        void createsFromOrders() {
            Sort sort = Sort.by(Sort.Order.asc("name"), Sort.Order.desc("age"));
            assertThat(sort.getOrders()).hasSize(2);
            assertThat(sort.getOrders().get(0).direction()).isEqualTo(Sort.Direction.ASC);
            assertThat(sort.getOrders().get(1).direction()).isEqualTo(Sort.Direction.DESC);
        }
    }

    @Nested
    @DisplayName("Static factory: by(List<Order>)")
    class ByOrderListTests {

        @Test
        @DisplayName("creates sort from list of orders")
        void createsFromList() {
            List<Sort.Order> orders = List.of(Sort.Order.asc("x"), Sort.Order.desc("y"));
            Sort sort = Sort.by(orders);
            assertThat(sort.getOrders()).hasSize(2);
        }

        @Test
        @DisplayName("empty list produces unsorted")
        void emptyListProducesUnsorted() {
            Sort sort = Sort.by(List.of());
            assertThat(sort.isUnsorted()).isTrue();
        }
    }

    @Nested
    @DisplayName("unsorted()")
    class UnsortedTests {

        @Test
        @DisplayName("returns UNSORTED singleton")
        void returnsSingleton() {
            assertThat(Sort.unsorted()).isSameAs(Sort.UNSORTED);
            assertThat(Sort.unsorted().isUnsorted()).isTrue();
            assertThat(Sort.unsorted().getOrders()).isEmpty();
        }
    }

    @Nested
    @DisplayName("isUnsorted()")
    class IsUnsortedTests {

        @Test
        @DisplayName("returns true when no orders")
        void trueWhenNoOrders() {
            assertThat(Sort.unsorted().isUnsorted()).isTrue();
        }

        @Test
        @DisplayName("returns false when orders present")
        void falseWhenOrdersPresent() {
            assertThat(Sort.by("name").isUnsorted()).isFalse();
        }
    }

    @Nested
    @DisplayName("and()")
    class AndTests {

        @Test
        @DisplayName("merges two sorts")
        void mergesTwoSorts() {
            Sort sort1 = Sort.by("name");
            Sort sort2 = Sort.by(Sort.Direction.DESC, "age");
            Sort merged = sort1.and(sort2);
            assertThat(merged.getOrders()).hasSize(2);
            assertThat(merged.getOrders().get(0).property()).isEqualTo("name");
            assertThat(merged.getOrders().get(1).property()).isEqualTo("age");
        }

        @Test
        @DisplayName("merging with unsorted returns original orders")
        void mergingWithUnsorted() {
            Sort sort = Sort.by("name");
            Sort merged = sort.and(Sort.unsorted());
            assertThat(merged.getOrders()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("toSql()")
    class ToSqlTests {

        @Test
        @DisplayName("renders single order as SQL")
        void singleOrder() {
            assertThat(Sort.by("name").toSql()).isEqualTo("name ASC");
        }

        @Test
        @DisplayName("renders multiple orders as comma-separated SQL")
        void multipleOrders() {
            Sort sort = Sort.by(Sort.Order.asc("name"), Sort.Order.desc("age"));
            assertThat(sort.toSql()).isEqualTo("name ASC, age DESC");
        }

        @Test
        @DisplayName("unsorted returns empty string")
        void unsortedReturnsEmpty() {
            assertThat(Sort.unsorted().toSql()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTests {

        @Test
        @DisplayName("unsorted returns Sort.UNSORTED")
        void unsortedString() {
            assertThat(Sort.unsorted().toString()).isEqualTo("Sort.UNSORTED");
        }

        @Test
        @DisplayName("sorted returns Sort[...] with SQL")
        void sortedString() {
            assertThat(Sort.by("name").toString()).isEqualTo("Sort[name ASC]");
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equal sorts are equal")
        void equalSorts() {
            Sort s1 = Sort.by("name");
            Sort s2 = Sort.by("name");
            assertThat(s1).isEqualTo(s2);
            assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
        }

        @Test
        @DisplayName("different sorts are not equal")
        void differentSorts() {
            Sort s1 = Sort.by("name");
            Sort s2 = Sort.by("age");
            assertThat(s1).isNotEqualTo(s2);
        }

        @Test
        @DisplayName("same instance is equal")
        void sameInstance() {
            Sort s = Sort.by("name");
            assertThat(s).isEqualTo(s);
        }

        @Test
        @DisplayName("not equal to non-Sort")
        void notEqualToOtherType() {
            assertThat(Sort.by("name")).isNotEqualTo("name");
        }
    }

    @Nested
    @DisplayName("getOrders() immutability")
    class ImmutabilityTests {

        @Test
        @DisplayName("returned list is unmodifiable")
        void unmodifiableList() {
            Sort sort = Sort.by("name");
            List<Sort.Order> orders = sort.getOrders();
            org.junit.jupiter.api.Assertions.assertThrows(
                    UnsupportedOperationException.class,
                    () -> orders.add(Sort.Order.asc("x"))
            );
        }
    }
}
