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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Ordering class.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Ordering Tests")
class OrderingTest {

    // ==================== Factory Method Tests ====================

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("natural() creates natural ordering")
        void naturalCreatesNaturalOrdering() {
            Ordering<String> ordering = Ordering.natural();

            assertTrue(ordering.compare("a", "b") < 0);
            assertTrue(ordering.compare("b", "a") > 0);
            assertEquals(0, ordering.compare("a", "a"));
        }

        @Test
        @DisplayName("natural() works with integers")
        void naturalWorksWithIntegers() {
            Ordering<Integer> ordering = Ordering.natural();

            assertTrue(ordering.compare(1, 2) < 0);
            assertTrue(ordering.compare(2, 1) > 0);
            assertEquals(0, ordering.compare(5, 5));
        }

        @Test
        @DisplayName("from(Comparator) creates ordering from comparator")
        void fromComparatorCreatesOrdering() {
            Comparator<String> comparator = String.CASE_INSENSITIVE_ORDER;
            Ordering<String> ordering = Ordering.from(comparator);

            assertEquals(0, ordering.compare("ABC", "abc"));
            assertTrue(ordering.compare("a", "B") < 0);
        }

        @Test
        @DisplayName("from(Comparator) returns same instance for Ordering")
        void fromComparatorReturnsSameInstanceForOrdering() {
            Ordering<String> original = Ordering.natural();
            Ordering<String> result = Ordering.from(original);

            assertSame(original, result);
        }

        @Test
        @DisplayName("from(Function) creates ordering by key")
        void fromFunctionCreatesOrderingByKey() {
            Ordering<String> ordering = Ordering.from(String::length);

            assertTrue(ordering.compare("a", "bb") < 0);
            assertTrue(ordering.compare("ccc", "dd") > 0);
            assertEquals(0, ordering.compare("ab", "cd"));
        }

        @Test
        @DisplayName("from(Function, Comparator) creates ordering by key with comparator")
        void fromFunctionComparatorCreatesOrdering() {
            Ordering<String> ordering = Ordering.from(
                    s -> s.toUpperCase(),
                    String.CASE_INSENSITIVE_ORDER
            );

            assertEquals(0, ordering.compare("abc", "ABC"));
        }

        @Test
        @DisplayName("allEqual() creates ordering that treats all as equal")
        void allEqualCreatesAllEqualOrdering() {
            Ordering<String> ordering = Ordering.allEqual();

            assertEquals(0, ordering.compare("a", "b"));
            assertEquals(0, ordering.compare("", "zzz"));
            assertEquals(0, ordering.compare(null, "test"));
        }

        @Test
        @DisplayName("explicit(varargs) creates explicit ordering")
        void explicitVarargsCreatesExplicitOrdering() {
            Ordering<String> ordering = Ordering.explicit("c", "b", "a");

            assertTrue(ordering.compare("c", "b") < 0);
            assertTrue(ordering.compare("b", "a") < 0);
            assertTrue(ordering.compare("c", "a") < 0);
        }

        @Test
        @DisplayName("explicit(List) creates explicit ordering")
        void explicitListCreatesExplicitOrdering() {
            Ordering<Integer> ordering = Ordering.explicit(Arrays.asList(3, 1, 2));

            assertTrue(ordering.compare(3, 1) < 0);
            assertTrue(ordering.compare(1, 2) < 0);
            assertTrue(ordering.compare(3, 2) < 0);
        }

        @Test
        @DisplayName("explicit() throws on duplicate value")
        void explicitThrowsOnDuplicateValue() {
            assertThrows(IllegalArgumentException.class, () -> Ordering.explicit("a", "b", "a"));
        }

        @Test
        @DisplayName("explicit() throws on unknown value")
        void explicitThrowsOnUnknownValue() {
            Ordering<String> ordering = Ordering.explicit("a", "b");
            assertThrows(IllegalArgumentException.class, () -> ordering.compare("a", "c"));
            assertThrows(IllegalArgumentException.class, () -> ordering.compare("c", "a"));
        }
    }

    // ==================== Transformation Tests ====================

    @Nested
    @DisplayName("Transformation Methods")
    class TransformationTests {

        @Test
        @DisplayName("reversed() returns reverse ordering")
        void reversedReturnsReverseOrdering() {
            Ordering<String> natural = Ordering.natural();
            Ordering<String> reversed = natural.reversed();

            assertTrue(reversed.compare("a", "b") > 0);
            assertTrue(reversed.compare("b", "a") < 0);
        }

        @Test
        @DisplayName("reversed().reversed() returns original")
        void doubleReversedReturnsOriginal() {
            Ordering<String> natural = Ordering.natural();
            Ordering<String> doubleReversed = natural.reversed().reversed();

            assertEquals(natural.compare("a", "b"), doubleReversed.compare("a", "b"));
        }

        @Test
        @DisplayName("natural().reversed() returns optimized reverse")
        void naturalReversedReturnsOptimized() {
            Ordering<String> natural = Ordering.natural();
            Ordering<String> reversed = natural.reversed();
            Ordering<String> doubleReversed = reversed.reversed();

            assertSame(natural, doubleReversed);
        }

        @Test
        @DisplayName("nullsFirst() puts nulls before non-nulls")
        void nullsFirstPutsNullsFirst() {
            Ordering<String> ordering = Ordering.<String>natural().nullsFirst();

            assertTrue(ordering.compare(null, "a") < 0);
            assertTrue(ordering.compare("a", null) > 0);
            assertEquals(0, ordering.compare(null, null));
            assertEquals(Ordering.<String>natural().compare("a", "b"), ordering.compare("a", "b"));
        }

        @Test
        @DisplayName("nullsLast() puts nulls after non-nulls")
        void nullsLastPutsNullsLast() {
            Ordering<String> ordering = Ordering.<String>natural().nullsLast();

            assertTrue(ordering.compare(null, "a") > 0);
            assertTrue(ordering.compare("a", null) < 0);
            assertEquals(0, ordering.compare(null, null));
        }

        @Test
        @DisplayName("nullsFirst().nullsLast() switches to nullsLast")
        void nullsFirstNullsLastSwitches() {
            Ordering<String> ordering = Ordering.<String>natural().nullsFirst().nullsLast();

            assertTrue(ordering.compare(null, "a") > 0);
        }

        @Test
        @DisplayName("nullsLast().nullsFirst() switches to nullsFirst")
        void nullsLastNullsFirstSwitches() {
            Ordering<String> ordering = Ordering.<String>natural().nullsLast().nullsFirst();

            assertTrue(ordering.compare(null, "a") < 0);
        }

        @Test
        @DisplayName("thenComparing(Comparator) creates compound ordering")
        void thenComparingComparatorCreatesCompound() {
            Ordering<String> ordering = Ordering.from(String::length)
                    .thenComparing(Ordering.natural());

            // Same length, use natural ordering
            assertTrue(ordering.compare("ab", "cd") < 0);
            assertTrue(ordering.compare("cd", "ab") > 0);

            // Different length, use primary
            assertTrue(ordering.compare("a", "bb") < 0);
        }

        @Test
        @DisplayName("thenComparing(Function) creates compound ordering")
        void thenComparingFunctionCreatesCompound() {
            Ordering<String> ordering = Ordering.from(String::length)
                    .thenComparing(s -> s.charAt(0));

            assertEquals(0, ordering.compare("ab", "ab"));
            assertTrue(ordering.compare("ab", "bb") < 0);
        }

        @Test
        @DisplayName("onResultOf() applies function before comparing")
        void onResultOfAppliesFunction() {
            Ordering<Integer> ordering = Ordering.<String>natural().onResultOf(Object::toString);

            // Compares as strings: "1" < "2" < "9" < "10" (lexicographic)
            assertTrue(ordering.compare(1, 2) < 0);
            assertTrue(ordering.compare(9, 10) > 0); // "9" > "10" lexicographically
        }
    }

    // ==================== Min/Max Tests ====================

    @Nested
    @DisplayName("Min/Max Operations")
    class MinMaxTests {

        @Test
        @DisplayName("min(a, b) returns minimum")
        void minReturnsMinimum() {
            Ordering<Integer> ordering = Ordering.natural();

            assertEquals(1, ordering.min(1, 2));
            assertEquals(1, ordering.min(2, 1));
            assertEquals(5, ordering.min(5, 5));
        }

        @Test
        @DisplayName("min(first, second, rest...) returns minimum")
        void minVarargsReturnsMinimum() {
            Ordering<Integer> ordering = Ordering.natural();

            assertEquals(1, ordering.min(3, 1, 4, 1, 5, 9, 2, 6));
        }

        @Test
        @DisplayName("min(Iterable) returns minimum")
        void minIterableReturnsMinimum() {
            Ordering<Integer> ordering = Ordering.natural();

            assertEquals(1, ordering.min(Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6)));
        }

        @Test
        @DisplayName("min(Iterable) throws on empty")
        void minIterableThrowsOnEmpty() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThrows(NoSuchElementException.class, () -> ordering.min(Collections.emptyList()));
        }

        @Test
        @DisplayName("max(a, b) returns maximum")
        void maxReturnsMaximum() {
            Ordering<Integer> ordering = Ordering.natural();

            assertEquals(2, ordering.max(1, 2));
            assertEquals(2, ordering.max(2, 1));
            assertEquals(5, ordering.max(5, 5));
        }

        @Test
        @DisplayName("max(first, second, rest...) returns maximum")
        void maxVarargsReturnsMaximum() {
            Ordering<Integer> ordering = Ordering.natural();

            assertEquals(9, ordering.max(3, 1, 4, 1, 5, 9, 2, 6));
        }

        @Test
        @DisplayName("max(Iterable) returns maximum")
        void maxIterableReturnsMaximum() {
            Ordering<Integer> ordering = Ordering.natural();

            assertEquals(9, ordering.max(Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6)));
        }

        @Test
        @DisplayName("max(Iterable) throws on empty")
        void maxIterableThrowsOnEmpty() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThrows(NoSuchElementException.class, () -> ordering.max(Collections.emptyList()));
        }
    }

    // ==================== Top K Tests ====================

    @Nested
    @DisplayName("Top K Operations")
    class TopKTests {

        @Test
        @DisplayName("leastOf() returns k smallest")
        void leastOfReturnsKSmallest() {
            Ordering<Integer> ordering = Ordering.natural();
            List<Integer> input = Arrays.asList(5, 3, 8, 1, 9, 2, 7, 4, 6);

            List<Integer> result = ordering.leastOf(input, 3);

            assertEquals(Arrays.asList(1, 2, 3), result);
        }

        @Test
        @DisplayName("leastOf() returns all when k > size")
        void leastOfReturnsAllWhenKGreaterThanSize() {
            Ordering<Integer> ordering = Ordering.natural();
            List<Integer> input = Arrays.asList(3, 1, 2);

            List<Integer> result = ordering.leastOf(input, 10);

            assertEquals(Arrays.asList(1, 2, 3), result);
        }

        @Test
        @DisplayName("leastOf() returns empty for k=0")
        void leastOfReturnsEmptyForK0() {
            Ordering<Integer> ordering = Ordering.natural();

            List<Integer> result = ordering.leastOf(Arrays.asList(1, 2, 3), 0);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("leastOf() throws for negative k")
        void leastOfThrowsForNegativeK() {
            Ordering<Integer> ordering = Ordering.natural();

            assertThrows(IllegalArgumentException.class,
                    () -> ordering.leastOf(Arrays.asList(1, 2, 3), -1));
        }

        @Test
        @DisplayName("greatestOf() returns k greatest")
        void greatestOfReturnsKGreatest() {
            Ordering<Integer> ordering = Ordering.natural();
            List<Integer> input = Arrays.asList(5, 3, 8, 1, 9, 2, 7, 4, 6);

            List<Integer> result = ordering.greatestOf(input, 3);

            assertEquals(Arrays.asList(9, 8, 7), result);
        }
    }

    // ==================== Sorting Tests ====================

    @Nested
    @DisplayName("Sorting Operations")
    class SortingTests {

        @Test
        @DisplayName("sortedCopy() returns sorted list")
        void sortedCopyReturnsSortedList() {
            Ordering<Integer> ordering = Ordering.natural();
            List<Integer> input = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6);

            List<Integer> result = ordering.sortedCopy(input);

            assertEquals(Arrays.asList(1, 1, 2, 3, 4, 5, 6, 9), result);
        }

        @Test
        @DisplayName("sortedCopy() does not modify original")
        void sortedCopyDoesNotModifyOriginal() {
            Ordering<Integer> ordering = Ordering.natural();
            List<Integer> input = new ArrayList<>(Arrays.asList(3, 1, 2));

            ordering.sortedCopy(input);

            assertEquals(Arrays.asList(3, 1, 2), input);
        }

        @Test
        @DisplayName("immutableSortedCopy() returns unmodifiable list")
        void immutableSortedCopyReturnsUnmodifiableList() {
            Ordering<Integer> ordering = Ordering.natural();

            List<Integer> result = ordering.immutableSortedCopy(Arrays.asList(3, 1, 2));

            assertEquals(Arrays.asList(1, 2, 3), result);
            assertThrows(UnsupportedOperationException.class, () -> result.add(4));
        }

        @Test
        @DisplayName("isOrdered() returns true for sorted iterable")
        void isOrderedReturnsTrueForSorted() {
            Ordering<Integer> ordering = Ordering.natural();

            assertTrue(ordering.isOrdered(Arrays.asList(1, 2, 3, 4, 5)));
            assertTrue(ordering.isOrdered(Arrays.asList(1, 1, 2, 3)));
            assertTrue(ordering.isOrdered(Collections.emptyList()));
            assertTrue(ordering.isOrdered(Collections.singletonList(5)));
        }

        @Test
        @DisplayName("isOrdered() returns false for unsorted iterable")
        void isOrderedReturnsFalseForUnsorted() {
            Ordering<Integer> ordering = Ordering.natural();

            assertFalse(ordering.isOrdered(Arrays.asList(1, 3, 2)));
            assertFalse(ordering.isOrdered(Arrays.asList(5, 4, 3, 2, 1)));
        }

        @Test
        @DisplayName("isStrictlyOrdered() returns true for strictly sorted")
        void isStrictlyOrderedReturnsTrueForStrictlySorted() {
            Ordering<Integer> ordering = Ordering.natural();

            assertTrue(ordering.isStrictlyOrdered(Arrays.asList(1, 2, 3, 4, 5)));
            assertTrue(ordering.isStrictlyOrdered(Collections.emptyList()));
            assertTrue(ordering.isStrictlyOrdered(Collections.singletonList(5)));
        }

        @Test
        @DisplayName("isStrictlyOrdered() returns false for duplicates")
        void isStrictlyOrderedReturnsFalseForDuplicates() {
            Ordering<Integer> ordering = Ordering.natural();

            assertFalse(ordering.isStrictlyOrdered(Arrays.asList(1, 1, 2, 3)));
            assertFalse(ordering.isStrictlyOrdered(Arrays.asList(1, 2, 2, 3)));
        }

        @Test
        @DisplayName("isStrictlyOrdered() returns false for unsorted")
        void isStrictlyOrderedReturnsFalseForUnsorted() {
            Ordering<Integer> ordering = Ordering.natural();

            assertFalse(ordering.isStrictlyOrdered(Arrays.asList(1, 3, 2)));
        }
    }

    // ==================== Complex Scenario Tests ====================

    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarioTests {

        @Test
        @DisplayName("Chained transformations work correctly")
        void chainedTransformationsWorkCorrectly() {
            Ordering<String> ordering = Ordering.from(String::length)
                    .reversed()
                    .nullsFirst()
                    .thenComparing(Ordering.natural());

            // Nulls first
            assertTrue(ordering.compare(null, "a") < 0);

            // Longer strings come first (reversed length)
            assertTrue(ordering.compare("aaa", "bb") < 0);

            // Same length, natural order
            assertTrue(ordering.compare("ab", "cd") < 0);
        }

        record Person(String name, int age) {}

        @Test
        @DisplayName("Complex ordering with records")
        void complexOrderingWithRecords() {
            Ordering<Person> ordering = Ordering.from(Person::age)
                    .thenComparing(Ordering.from(Person::name));

            Person alice20 = new Person("Alice", 20);
            Person bob20 = new Person("Bob", 20);
            Person alice30 = new Person("Alice", 30);

            assertTrue(ordering.compare(alice20, alice30) < 0); // Age diff
            assertTrue(ordering.compare(alice20, bob20) < 0);   // Same age, name diff
        }
    }
}
