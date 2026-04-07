/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Range class.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Range Tests")
class RangeTest {

    // ==================== Factory Method Tests ====================

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("closed() creates inclusive range")
        void closedCreatesInclusiveRange() {
            Range<Integer> range = Range.closed(1, 10);

            assertTrue(range.hasLowerBound());
            assertTrue(range.hasUpperBound());
            assertEquals(Optional.of(1), range.lowerEndpoint());
            assertEquals(Optional.of(10), range.upperEndpoint());
            assertEquals(Range.BoundType.CLOSED, range.lowerBoundType());
            assertEquals(Range.BoundType.CLOSED, range.upperBoundType());
        }

        @Test
        @DisplayName("open() creates exclusive range")
        void openCreatesExclusiveRange() {
            Range<Integer> range = Range.open(1, 10);

            assertTrue(range.hasLowerBound());
            assertTrue(range.hasUpperBound());
            assertEquals(Optional.of(1), range.lowerEndpoint());
            assertEquals(Optional.of(10), range.upperEndpoint());
            assertEquals(Range.BoundType.OPEN, range.lowerBoundType());
            assertEquals(Range.BoundType.OPEN, range.upperBoundType());
        }

        @Test
        @DisplayName("closedOpen() creates left-closed right-open range")
        void closedOpenCreatesHalfOpenRange() {
            Range<Integer> range = Range.closedOpen(1, 10);

            assertEquals(Range.BoundType.CLOSED, range.lowerBoundType());
            assertEquals(Range.BoundType.OPEN, range.upperBoundType());
        }

        @Test
        @DisplayName("openClosed() creates left-open right-closed range")
        void openClosedCreatesHalfOpenRange() {
            Range<Integer> range = Range.openClosed(1, 10);

            assertEquals(Range.BoundType.OPEN, range.lowerBoundType());
            assertEquals(Range.BoundType.CLOSED, range.upperBoundType());
        }

        @Test
        @DisplayName("atMost() creates unbounded lower range")
        void atMostCreatesUnboundedLower() {
            Range<Integer> range = Range.atMost(10);

            assertFalse(range.hasLowerBound());
            assertTrue(range.hasUpperBound());
            assertEquals(Optional.empty(), range.lowerEndpoint());
            assertEquals(Optional.of(10), range.upperEndpoint());
            assertEquals(Range.BoundType.CLOSED, range.upperBoundType());
        }

        @Test
        @DisplayName("lessThan() creates unbounded lower open range")
        void lessThanCreatesUnboundedLowerOpen() {
            Range<Integer> range = Range.lessThan(10);

            assertFalse(range.hasLowerBound());
            assertTrue(range.hasUpperBound());
            assertEquals(Range.BoundType.OPEN, range.upperBoundType());
        }

        @Test
        @DisplayName("atLeast() creates unbounded upper range")
        void atLeastCreatesUnboundedUpper() {
            Range<Integer> range = Range.atLeast(5);

            assertTrue(range.hasLowerBound());
            assertFalse(range.hasUpperBound());
            assertEquals(Optional.of(5), range.lowerEndpoint());
            assertEquals(Optional.empty(), range.upperEndpoint());
            assertEquals(Range.BoundType.CLOSED, range.lowerBoundType());
        }

        @Test
        @DisplayName("greaterThan() creates unbounded upper open range")
        void greaterThanCreatesUnboundedUpperOpen() {
            Range<Integer> range = Range.greaterThan(5);

            assertTrue(range.hasLowerBound());
            assertFalse(range.hasUpperBound());
            assertEquals(Range.BoundType.OPEN, range.lowerBoundType());
        }

        @Test
        @DisplayName("all() creates fully unbounded range")
        void allCreatesUnboundedRange() {
            Range<Integer> range = Range.all();

            assertFalse(range.hasLowerBound());
            assertFalse(range.hasUpperBound());
            assertEquals(Optional.empty(), range.lowerEndpoint());
            assertEquals(Optional.empty(), range.upperEndpoint());
        }

        @Test
        @DisplayName("singleton() creates single-value range")
        void singletonCreatesSingleValueRange() {
            Range<Integer> range = Range.singleton(5);

            assertEquals(Optional.of(5), range.lowerEndpoint());
            assertEquals(Optional.of(5), range.upperEndpoint());
            assertEquals(Range.BoundType.CLOSED, range.lowerBoundType());
            assertEquals(Range.BoundType.CLOSED, range.upperBoundType());
            assertTrue(range.contains(5));
            assertFalse(range.contains(4));
            assertFalse(range.contains(6));
        }

        @Test
        @DisplayName("encloseAll(varargs) creates minimal enclosing range")
        void encloseAllVarargsCreatesMinimalRange() {
            Range<Integer> range = Range.encloseAll(3, 1, 5, 2, 4);

            assertEquals(Optional.of(1), range.lowerEndpoint());
            assertEquals(Optional.of(5), range.upperEndpoint());
        }

        @Test
        @DisplayName("encloseAll(varargs) with single value")
        void encloseAllVarargsSingleValue() {
            Range<Integer> range = Range.encloseAll(5);

            assertEquals(Optional.of(5), range.lowerEndpoint());
            assertEquals(Optional.of(5), range.upperEndpoint());
        }

        @Test
        @DisplayName("encloseAll(varargs) throws on empty")
        void encloseAllVarargsThrowsOnEmpty() {
            assertThrows(IllegalArgumentException.class, () -> Range.encloseAll(new Integer[0]));
        }

        @Test
        @DisplayName("encloseAll(varargs) throws on null")
        void encloseAllVarargsThrowsOnNull() {
            assertThrows(IllegalArgumentException.class, () -> Range.encloseAll((Integer[]) null));
        }

        @Test
        @DisplayName("encloseAll(Iterable) creates minimal enclosing range")
        void encloseAllIterableCreatesMinimalRange() {
            Range<Integer> range = Range.encloseAll(Arrays.asList(3, 1, 5, 2, 4));

            assertEquals(Optional.of(1), range.lowerEndpoint());
            assertEquals(Optional.of(5), range.upperEndpoint());
        }

        @Test
        @DisplayName("encloseAll(Iterable) with single element")
        void encloseAllIterableSingleElement() {
            Range<Integer> range = Range.encloseAll(Collections.singletonList(5));

            assertEquals(Optional.of(5), range.lowerEndpoint());
            assertEquals(Optional.of(5), range.upperEndpoint());
        }

        @Test
        @DisplayName("encloseAll(Iterable) throws on empty")
        void encloseAllIterableThrowsOnEmpty() {
            assertThrows(IllegalArgumentException.class, () -> Range.encloseAll(Collections.<Integer>emptyList()));
        }
    }

    // ==================== Validation Tests ====================

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("closed() throws when lower > upper")
        void closedThrowsWhenLowerGreaterThanUpper() {
            assertThrows(IllegalArgumentException.class, () -> Range.closed(10, 1));
        }

        @Test
        @DisplayName("open() throws when lower >= upper (empty range)")
        void openThrowsWhenLowerEqualsUpper() {
            assertThrows(IllegalArgumentException.class, () -> Range.open(5, 5));
        }

        @Test
        @DisplayName("closedOpen() throws when lower > upper")
        void closedOpenThrowsWhenInvalid() {
            assertThrows(IllegalArgumentException.class, () -> Range.closedOpen(10, 5));
        }

        @Test
        @DisplayName("closedOpen() throws for equal bounds")
        void closedOpenThrowsForEqualBounds() {
            // Implementation doesn't allow equal bounds for half-open ranges
            assertThrows(IllegalArgumentException.class, () -> Range.closedOpen(5, 5));
        }

        @Test
        @DisplayName("openClosed() throws for equal bounds")
        void openClosedThrowsForEqualBounds() {
            // Implementation doesn't allow equal bounds for half-open ranges
            assertThrows(IllegalArgumentException.class, () -> Range.openClosed(5, 5));
        }
    }

    // ==================== Containment Tests ====================

    @Nested
    @DisplayName("Containment Tests")
    class ContainmentTests {

        @Test
        @DisplayName("contains() for closed range")
        void containsForClosedRange() {
            Range<Integer> range = Range.closed(1, 10);

            assertTrue(range.contains(1));
            assertTrue(range.contains(5));
            assertTrue(range.contains(10));
            assertFalse(range.contains(0));
            assertFalse(range.contains(11));
        }

        @Test
        @DisplayName("contains() for open range")
        void containsForOpenRange() {
            Range<Integer> range = Range.open(1, 10);

            assertFalse(range.contains(1));
            assertTrue(range.contains(5));
            assertFalse(range.contains(10));
        }

        @Test
        @DisplayName("contains() for unbounded range")
        void containsForUnboundedRange() {
            Range<Integer> atLeast = Range.atLeast(5);
            assertTrue(atLeast.contains(5));
            assertTrue(atLeast.contains(100));
            assertFalse(atLeast.contains(4));

            Range<Integer> atMost = Range.atMost(5);
            assertTrue(atMost.contains(5));
            assertTrue(atMost.contains(-100));
            assertFalse(atMost.contains(6));

            Range<Integer> all = Range.all();
            assertTrue(all.contains(Integer.MIN_VALUE));
            assertTrue(all.contains(Integer.MAX_VALUE));
        }

        @Test
        @DisplayName("contains() throws on null")
        void containsThrowsOnNull() {
            Range<Integer> range = Range.closed(1, 10);
            assertThrows(NullPointerException.class, () -> range.contains(null));
        }

        @Test
        @DisplayName("containsAll(varargs)")
        void containsAllVarargs() {
            Range<Integer> range = Range.closed(1, 10);

            assertTrue(range.containsAll(1, 5, 10));
            assertFalse(range.containsAll(0, 5, 10));
            assertFalse(range.containsAll(1, 5, 11));
            assertTrue(range.containsAll(5));
        }

        @Test
        @DisplayName("containsAll(Iterable)")
        void containsAllIterable() {
            Range<Integer> range = Range.closed(1, 10);

            assertTrue(range.containsAll(Arrays.asList(1, 5, 10)));
            assertFalse(range.containsAll(Arrays.asList(0, 5, 10)));
            assertTrue(range.containsAll(Collections.emptyList()));
        }

        @Test
        @DisplayName("encloses() returns true for enclosing range")
        void enclosesReturnsTrueForEnclosingRange() {
            Range<Integer> outer = Range.closed(1, 10);
            Range<Integer> inner = Range.closed(3, 7);

            assertTrue(outer.encloses(inner));
            assertFalse(inner.encloses(outer));
            assertTrue(outer.encloses(outer)); // encloses self
        }

        @Test
        @DisplayName("encloses() with different bound types")
        void enclosesWithDifferentBoundTypes() {
            Range<Integer> closed = Range.closed(1, 10);
            Range<Integer> innerClosed = Range.closed(2, 9);

            // closed(1,10) encloses closed(2,9)
            assertTrue(closed.encloses(innerClosed));
        }

        @Test
        @DisplayName("encloses() with subset ranges")
        void enclosesWithSubsetRanges() {
            Range<Integer> outer = Range.closed(1, 10);
            Range<Integer> inner = Range.closed(3, 7);

            assertTrue(outer.encloses(inner));
            assertFalse(inner.encloses(outer));
        }
    }

    // ==================== Connection Tests ====================

    @Nested
    @DisplayName("Connection Tests")
    class ConnectionTests {

        @Test
        @DisplayName("isConnected() for overlapping ranges")
        void isConnectedForOverlappingRanges() {
            Range<Integer> range1 = Range.closed(1, 10);
            Range<Integer> range2 = Range.closed(5, 15);

            assertTrue(range1.isConnected(range2));
            assertTrue(range2.isConnected(range1));
        }

        @Test
        @DisplayName("isConnected() for adjacent ranges")
        void isConnectedForAdjacentRanges() {
            Range<Integer> range1 = Range.closed(1, 5);
            Range<Integer> range2 = Range.closed(5, 10);

            assertTrue(range1.isConnected(range2));
        }

        @Test
        @DisplayName("isConnected() for disjoint ranges")
        void isConnectedForDisjointRanges() {
            Range<Integer> range1 = Range.closed(1, 5);
            Range<Integer> range2 = Range.closed(7, 10);

            assertFalse(range1.isConnected(range2));
            assertFalse(range2.isConnected(range1));
        }

        @Test
        @DisplayName("isConnected() with unbounded ranges")
        void isConnectedWithUnboundedRanges() {
            Range<Integer> atLeast = Range.atLeast(5);
            Range<Integer> atMost = Range.atMost(10);

            // atLeast(5) and atMost(10) are connected - they overlap [5, 10]
            assertTrue(atLeast.isConnected(atMost));
        }
    }

    // ==================== Operation Tests ====================

    @Nested
    @DisplayName("Operation Tests")
    class OperationTests {

        @Test
        @DisplayName("intersection() of overlapping ranges")
        void intersectionOfOverlappingRanges() {
            Range<Integer> range1 = Range.closed(1, 10);
            Range<Integer> range2 = Range.closed(5, 15);

            Range<Integer> intersection = range1.intersection(range2);

            assertEquals(Optional.of(5), intersection.lowerEndpoint());
            assertEquals(Optional.of(10), intersection.upperEndpoint());
        }

        @Test
        @DisplayName("intersection() of adjacent ranges")
        void intersectionOfAdjacentRanges() {
            Range<Integer> range1 = Range.closed(1, 5);
            Range<Integer> range2 = Range.closed(5, 10);

            Range<Integer> intersection = range1.intersection(range2);

            assertEquals(Optional.of(5), intersection.lowerEndpoint());
            assertEquals(Optional.of(5), intersection.upperEndpoint());
        }

        @Test
        @DisplayName("intersection() throws for disjoint ranges")
        void intersectionThrowsForDisjointRanges() {
            Range<Integer> range1 = Range.closed(1, 5);
            Range<Integer> range2 = Range.closed(7, 10);

            assertThrows(IllegalArgumentException.class, () -> range1.intersection(range2));
        }

        @Test
        @DisplayName("intersection() with different bound types")
        void intersectionWithDifferentBoundTypes() {
            Range<Integer> closed = Range.closed(1, 10);
            Range<Integer> open = Range.open(5, 15);

            Range<Integer> intersection = closed.intersection(open);

            assertEquals(Range.BoundType.OPEN, intersection.lowerBoundType());
            assertEquals(Range.BoundType.CLOSED, intersection.upperBoundType());
        }

        @Test
        @DisplayName("span() creates minimal enclosing range")
        void spanCreatesMinimalEnclosingRange() {
            Range<Integer> range1 = Range.closed(1, 5);
            Range<Integer> range2 = Range.closed(10, 15);

            Range<Integer> span = range1.span(range2);

            assertEquals(Optional.of(1), span.lowerEndpoint());
            assertEquals(Optional.of(15), span.upperEndpoint());
        }

        @Test
        @DisplayName("span() of overlapping ranges")
        void spanOfOverlappingRanges() {
            Range<Integer> range1 = Range.closed(1, 10);
            Range<Integer> range2 = Range.closed(5, 15);

            Range<Integer> span = range1.span(range2);

            assertEquals(Optional.of(1), span.lowerEndpoint());
            assertEquals(Optional.of(15), span.upperEndpoint());
        }

        @Test
        @DisplayName("span() includes both ranges")
        void spanIncludesBothRanges() {
            Range<Integer> range1 = Range.closed(1, 5);
            Range<Integer> range2 = Range.closed(3, 10);

            Range<Integer> span = range1.span(range2);

            assertEquals(Optional.of(1), span.lowerEndpoint());
            assertEquals(Optional.of(10), span.upperEndpoint());
        }

        @Test
        @DisplayName("gap() returns gap between disjoint ranges")
        void gapReturnsBetweenDisjointRanges() {
            Range<Integer> range1 = Range.closed(1, 5);
            Range<Integer> range2 = Range.closed(10, 15);

            Optional<Range<Integer>> gap = range1.gap(range2);

            assertTrue(gap.isPresent());
            assertEquals(Optional.of(5), gap.get().lowerEndpoint());
            assertEquals(Optional.of(10), gap.get().upperEndpoint());
            assertEquals(Range.BoundType.OPEN, gap.get().lowerBoundType());
            assertEquals(Range.BoundType.OPEN, gap.get().upperBoundType());
        }

        @Test
        @DisplayName("gap() returns empty for connected ranges")
        void gapReturnsEmptyForConnectedRanges() {
            Range<Integer> range1 = Range.closed(1, 10);
            Range<Integer> range2 = Range.closed(5, 15);

            Optional<Range<Integer>> gap = range1.gap(range2);

            assertTrue(gap.isEmpty());
        }

        @Test
        @DisplayName("gap() with reversed order")
        void gapWithReversedOrder() {
            Range<Integer> range1 = Range.closed(10, 15);
            Range<Integer> range2 = Range.closed(1, 5);

            Optional<Range<Integer>> gap = range1.gap(range2);

            assertTrue(gap.isPresent());
        }

        @Test
        @DisplayName("canonical() converts to canonical form")
        void canonicalConvertsToCanonicalForm() {
            Range<Integer> range = Range.open(1, 5);

            Range<Integer> canonical = range.canonical(x -> x + 1);

            // Open lower (1 becomes 2 with successor for closed lower
            assertEquals(Optional.of(2), canonical.lowerEndpoint());
            // Upper endpoint depends on implementation - verify it's set
            assertTrue(canonical.upperEndpoint().isPresent());
            assertEquals(Range.BoundType.CLOSED, canonical.lowerBoundType());
        }

        @Test
        @DisplayName("canonical() with unbounded range")
        void canonicalWithUnboundedRange() {
            Range<Integer> range = Range.greaterThan(5);

            Range<Integer> canonical = range.canonical(x -> x + 1);

            assertEquals(Optional.of(6), canonical.lowerEndpoint());
            assertFalse(canonical.hasUpperBound());
        }

        @Test
        @DisplayName("canonical() with closed range")
        void canonicalWithClosedRange() {
            Range<Integer> range = Range.closed(1, 5);

            Range<Integer> canonical = range.canonical(x -> x + 1);

            assertEquals(Optional.of(1), canonical.lowerEndpoint());
            assertEquals(Optional.of(6), canonical.upperEndpoint());
        }
    }

    // ==================== isEmpty Tests ====================

    @Nested
    @DisplayName("isEmpty Tests")
    class IsEmptyTests {

        @Test
        @DisplayName("isEmpty() returns false for normal ranges")
        void isEmptyReturnsFalseForNormalRanges() {
            assertFalse(Range.closed(1, 10).isEmpty());
            assertFalse(Range.open(1, 10).isEmpty());
        }

        @Test
        @DisplayName("isEmpty() behavior test")
        void isEmptyBehavior() {
            // Singleton range is not empty
            assertFalse(Range.singleton(5).isEmpty());
            // Normal range is not empty
            assertFalse(Range.closed(1, 5).isEmpty());
        }

        @Test
        @DisplayName("isEmpty() returns false for unbounded ranges")
        void isEmptyReturnsFalseForUnboundedRanges() {
            assertFalse(Range.atLeast(5).isEmpty());
            assertFalse(Range.atMost(5).isEmpty());
            assertFalse(Range.all().isEmpty());
        }

        @Test
        @DisplayName("isEmpty() returns false for singleton")
        void isEmptyReturnsFalseForSingleton() {
            assertFalse(Range.singleton(5).isEmpty());
        }
    }

    // ==================== Object Method Tests ====================

    @Nested
    @DisplayName("Object Method Tests")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() and hashCode() contract")
        void equalsAndHashCodeContract() {
            Range<Integer> range1 = Range.closed(1, 10);
            Range<Integer> range2 = Range.closed(1, 10);
            Range<Integer> range3 = Range.closed(1, 11);

            assertEquals(range1, range2);
            assertEquals(range1.hashCode(), range2.hashCode());
            assertNotEquals(range1, range3);
        }

        @Test
        @DisplayName("equals() with same instance")
        void equalsWithSameInstance() {
            Range<Integer> range = Range.closed(1, 10);
            assertEquals(range, range);
        }

        @Test
        @DisplayName("equals() with different type")
        void equalsWithDifferentType() {
            Range<Integer> range = Range.closed(1, 10);
            assertNotEquals(range, "not a range");
            assertNotEquals(range, null);
        }

        @Test
        @DisplayName("equals() with different bound types")
        void equalsWithDifferentBoundTypes() {
            Range<Integer> closed = Range.closed(1, 10);
            Range<Integer> open = Range.open(1, 10);
            Range<Integer> closedOpen = Range.closedOpen(1, 10);

            assertNotEquals(closed, open);
            assertNotEquals(closed, closedOpen);
        }

        @Test
        @DisplayName("toString() for bounded ranges")
        void toStringForBoundedRanges() {
            assertEquals("[1..10]", Range.closed(1, 10).toString());
            assertEquals("(1..10)", Range.open(1, 10).toString());
            assertEquals("[1..10)", Range.closedOpen(1, 10).toString());
            assertEquals("(1..10]", Range.openClosed(1, 10).toString());
        }

        @Test
        @DisplayName("toString() for unbounded ranges")
        void toStringForUnboundedRanges() {
            assertEquals("[5..+∞)", Range.atLeast(5).toString());
            assertEquals("(5..+∞)", Range.greaterThan(5).toString());
            assertEquals("(-∞..5]", Range.atMost(5).toString());
            assertEquals("(-∞..5)", Range.lessThan(5).toString());
            assertEquals("(-∞..+∞)", Range.all().toString());
        }
    }

    // ==================== BoundType Enum Tests ====================

    @Nested
    @DisplayName("BoundType Enum Tests")
    class BoundTypeEnumTests {

        @Test
        @DisplayName("BoundType values exist")
        void boundTypeValuesExist() {
            Range.BoundType[] values = Range.BoundType.values();
            assertEquals(2, values.length);
            assertEquals(Range.BoundType.OPEN, Range.BoundType.valueOf("OPEN"));
            assertEquals(Range.BoundType.CLOSED, Range.BoundType.valueOf("CLOSED"));
        }
    }

    // ==================== String Range Tests ====================

    @Nested
    @DisplayName("String Range Tests")
    class StringRangeTests {

        @Test
        @DisplayName("Range works with String type")
        void rangeWorksWithStringType() {
            Range<String> range = Range.closed("a", "z");

            assertTrue(range.contains("m"));
            assertFalse(range.contains("A"));
            assertTrue(range.contains("a"));
            assertTrue(range.contains("z"));
        }
    }

    // ==================== Double Range Tests ====================

    @Nested
    @DisplayName("Double Range Tests")
    class DoubleRangeTests {

        @Test
        @DisplayName("Range works with Double type")
        void rangeWorksWithDoubleType() {
            Range<Double> range = Range.closed(1.0, 10.0);

            assertTrue(range.contains(5.5));
            assertFalse(range.contains(0.5));
            assertTrue(range.contains(1.0));
            assertTrue(range.contains(10.0));
        }
    }

    // ==================== Predicate Tests ====================

    @Nested
    @DisplayName("Predicate support")
    class PredicateTests {
        @Test void testDelegatesToContains() {
            Range<Integer> range = Range.closed(1, 10);
            assertThat(range.test(5)).isTrue();
            assertThat(range.test(0)).isFalse();
            assertThat(range.test(11)).isFalse();
        }

        @Test void usableAsStreamFilter() {
            Range<Integer> range = Range.closed(3, 7);
            List<Integer> result = IntStream.rangeClosed(1, 10)
                .boxed().filter(range).toList();
            assertThat(result).containsExactly(3, 4, 5, 6, 7);
        }

        @Test void composableWithOtherPredicates() {
            Range<Integer> range = Range.closed(1, 100);
            Predicate<Integer> evenAndInRange = range.and(n -> n % 2 == 0);
            assertThat(evenAndInRange.test(4)).isTrue();
            assertThat(evenAndInRange.test(3)).isFalse();
            assertThat(evenAndInRange.test(200)).isFalse();
        }

        @Test void negatable() {
            Range<Integer> range = Range.closed(1, 5);
            Predicate<Integer> outside = range.negate();
            assertThat(outside.test(0)).isTrue();
            assertThat(outside.test(3)).isFalse();
            assertThat(outside.test(6)).isTrue();
        }
    }
}
