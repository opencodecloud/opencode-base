package cloud.opencode.base.core.page;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Page}.
 */
@DisplayName("Page Tests")
class PageTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor uses page 1, size 10")
        void defaultConstructor() {
            Page<String> page = new Page<>();
            assertThat(page.getCurrent()).isEqualTo(1);
            assertThat(page.getSize()).isEqualTo(10);
            assertThat(page.getTotal()).isZero();
            assertThat(page.getRecords()).isEmpty();
        }

        @Test
        @DisplayName("parameterized constructor sets values")
        void parameterizedConstructor() {
            Page<String> page = new Page<>(3, 25);
            assertThat(page.getCurrent()).isEqualTo(3);
            assertThat(page.getSize()).isEqualTo(25);
        }

        @Test
        @DisplayName("constructor clamps current to minimum 1")
        void constructorClampsCurrentMin() {
            Page<String> page = new Page<>(0, 10);
            assertThat(page.getCurrent()).isEqualTo(1);

            Page<String> page2 = new Page<>(-5, 10);
            assertThat(page2.getCurrent()).isEqualTo(1);
        }

        @Test
        @DisplayName("constructor clamps size to minimum 1")
        void constructorClampsSizeMin() {
            Page<String> page = new Page<>(1, 0);
            assertThat(page.getSize()).isEqualTo(1);

            Page<String> page2 = new Page<>(1, -3);
            assertThat(page2.getSize()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("of() factory")
    class OfTests {

        @Test
        @DisplayName("creates page with given values")
        void createsPage() {
            Page<Integer> page = Page.of(2, 15);
            assertThat(page.getCurrent()).isEqualTo(2);
            assertThat(page.getSize()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("Setters")
    class SetterTests {

        @Test
        @DisplayName("setCurrent clamps to minimum 1 and returns this")
        void setCurrentClamped() {
            Page<String> page = new Page<>();
            Page<String> result = page.setCurrent(5);
            assertThat(result).isSameAs(page);
            assertThat(page.getCurrent()).isEqualTo(5);

            page.setCurrent(0);
            assertThat(page.getCurrent()).isEqualTo(1);
        }

        @Test
        @DisplayName("setSize clamps to minimum 1 and returns this")
        void setSizeClamped() {
            Page<String> page = new Page<>();
            page.setSize(0);
            assertThat(page.getSize()).isEqualTo(1);
            page.setSize(20);
            assertThat(page.getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("setTotal clamps to minimum 0 and returns this")
        void setTotalClamped() {
            Page<String> page = new Page<>();
            page.setTotal(-1);
            assertThat(page.getTotal()).isZero();
            page.setTotal(100);
            assertThat(page.getTotal()).isEqualTo(100);
        }

        @Test
        @DisplayName("setRecords with null defaults to empty list")
        void setRecordsNull() {
            Page<String> page = new Page<>();
            page.setRecords(null);
            assertThat(page.getRecords()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("setRecords stores the list")
        void setRecordsStoresList() {
            Page<String> page = new Page<>();
            List<String> items = List.of("a", "b", "c");
            page.setRecords(items);
            assertThat(page.getRecords()).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("getPages()")
    class GetPagesTests {

        @Test
        @DisplayName("computes total pages correctly with exact division")
        void exactDivision() {
            Page<String> page = Page.of(1, 10);
            page.setTotal(100);
            assertThat(page.getPages()).isEqualTo(10);
        }

        @Test
        @DisplayName("computes total pages with remainder")
        void withRemainder() {
            Page<String> page = Page.of(1, 10);
            page.setTotal(101);
            assertThat(page.getPages()).isEqualTo(11);
        }

        @Test
        @DisplayName("zero total returns 0 pages")
        void zeroTotal() {
            Page<String> page = Page.of(1, 10);
            assertThat(page.getPages()).isZero();
        }

        @Test
        @DisplayName("single item with size 1 returns 1 page")
        void singleItem() {
            Page<String> page = Page.of(1, 1);
            page.setTotal(1);
            assertThat(page.getPages()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("hasNext()")
    class HasNextTests {

        @Test
        @DisplayName("returns true when more pages exist")
        void trueWhenMorePages() {
            Page<String> page = Page.of(1, 10);
            page.setTotal(25);
            assertThat(page.hasNext()).isTrue();
        }

        @Test
        @DisplayName("returns false on last page")
        void falseOnLastPage() {
            Page<String> page = Page.of(3, 10);
            page.setTotal(25);
            assertThat(page.hasNext()).isFalse();
        }

        @Test
        @DisplayName("returns false when no records")
        void falseWhenEmpty() {
            Page<String> page = Page.of(1, 10);
            assertThat(page.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasPrevious()")
    class HasPreviousTests {

        @Test
        @DisplayName("returns false on first page")
        void falseOnFirst() {
            Page<String> page = Page.of(1, 10);
            assertThat(page.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("returns true on subsequent pages")
        void trueOnSubsequent() {
            Page<String> page = Page.of(2, 10);
            assertThat(page.hasPrevious()).isTrue();
        }
    }

    @Nested
    @DisplayName("getOffset()")
    class GetOffsetTests {

        @Test
        @DisplayName("computes offset correctly")
        void computesOffset() {
            assertThat(Page.of(1, 10).getOffset()).isZero();
            assertThat(Page.of(2, 10).getOffset()).isEqualTo(10);
            assertThat(Page.of(3, 25).getOffset()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTests {

        @Test
        @DisplayName("includes all fields")
        void includesAllFields() {
            Page<String> page = Page.of(2, 10);
            page.setTotal(50);
            page.setRecords(List.of("a", "b"));
            String str = page.toString();
            assertThat(str).contains("current=2", "size=10", "total=50", "pages=5", "records=2");
        }
    }
}
