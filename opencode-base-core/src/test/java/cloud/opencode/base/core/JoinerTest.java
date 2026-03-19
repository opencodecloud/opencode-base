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

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Joiner class
 * Joiner 类的全面测试
 *
 * @author Test
 * @since JDK 25, opencode-base-core V1.0.0
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Joiner Tests")
class JoinerTest {

    // ==================== Factory Methods Tests ====================

    @Nested
    @DisplayName("Factory Methods - on(char)")
    class OnCharTests {

        @Test
        @DisplayName("Join with comma separator")
        void joinWithComma() {
            String result = Joiner.on(',').join("a", "b", "c");
            assertEquals("a,b,c", result);
        }

        @Test
        @DisplayName("Join with pipe separator")
        void joinWithPipe() {
            String result = Joiner.on('|').join("x", "y", "z");
            assertEquals("x|y|z", result);
        }

        @Test
        @DisplayName("Join single element")
        void joinSingleElement() {
            String result = Joiner.on(',').join("a");
            assertEquals("a", result);
        }

        @Test
        @DisplayName("Join empty varargs")
        void joinEmptyVarargs() {
            String result = Joiner.on(',').join();
            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Factory Methods - on(String)")
    class OnStringTests {

        @Test
        @DisplayName("Join with string separator")
        void joinWithStringSeparator() {
            String result = Joiner.on(", ").join("a", "b", "c");
            assertEquals("a, b, c", result);
        }

        @Test
        @DisplayName("Join with multi-character separator")
        void joinWithMultiCharSeparator() {
            String result = Joiner.on(" -> ").join("x", "y", "z");
            assertEquals("x -> y -> z", result);
        }

        @Test
        @DisplayName("Join with empty separator")
        void joinWithEmptySeparator() {
            String result = Joiner.on("").join("a", "b", "c");
            assertEquals("abc", result);
        }

        @Test
        @DisplayName("Null separator throws exception")
        void nullSeparator() {
            assertThrows(NullPointerException.class, () -> Joiner.on((String) null));
        }
    }

    // ==================== Configuration Methods Tests ====================

    @Nested
    @DisplayName("Configuration - skipNulls()")
    class SkipNullsTests {

        @Test
        @DisplayName("Skip null values")
        void skipNulls() {
            String result = Joiner.on(',').skipNulls().join("a", null, "b", null, "c");
            assertEquals("a,b,c", result);
        }

        @Test
        @DisplayName("Skip all nulls results in empty")
        void skipAllNulls() {
            String result = Joiner.on(',').skipNulls().join(null, null, null);
            assertEquals("", result);
        }

        @Test
        @DisplayName("Skip nulls with no nulls")
        void skipNullsNoNulls() {
            String result = Joiner.on(',').skipNulls().join("a", "b", "c");
            assertEquals("a,b,c", result);
        }

        @Test
        @DisplayName("Skip nulls at start")
        void skipNullsAtStart() {
            String result = Joiner.on(',').skipNulls().join(null, null, "a", "b");
            assertEquals("a,b", result);
        }

        @Test
        @DisplayName("Skip nulls at end")
        void skipNullsAtEnd() {
            String result = Joiner.on(',').skipNulls().join("a", "b", null, null);
            assertEquals("a,b", result);
        }
    }

    @Nested
    @DisplayName("Configuration - useForNull()")
    class UseForNullTests {

        @Test
        @DisplayName("Replace nulls with text")
        void useForNull() {
            String result = Joiner.on(',').useForNull("N/A").join("a", null, "b");
            assertEquals("a,N/A,b", result);
        }

        @Test
        @DisplayName("Replace all nulls")
        void useForNullAll() {
            String result = Joiner.on(',').useForNull("-").join(null, null, null);
            assertEquals("-,-,-", result);
        }

        @Test
        @DisplayName("Replace null with empty string")
        void useForNullEmpty() {
            String result = Joiner.on(',').useForNull("").join("a", null, "b");
            assertEquals("a,,b", result);
        }

        @Test
        @DisplayName("Null nullText throws exception")
        void nullNullText() {
            assertThrows(NullPointerException.class, () -> Joiner.on(',').useForNull(null));
        }
    }

    @Nested
    @DisplayName("Configuration - withFormatter()")
    class WithFormatterTests {

        @Test
        @DisplayName("Format with uppercase")
        void formatUppercase() {
            String result = Joiner.on(',')
                    .withFormatter(obj -> obj.toString().toUpperCase())
                    .join("a", "b", "c");
            assertEquals("A,B,C", result);
        }

        @Test
        @DisplayName("Format integers with prefix")
        void formatIntegersWithPrefix() {
            String result = Joiner.on(',')
                    .withFormatter(obj -> "#" + obj)
                    .join(1, 2, 3);
            assertEquals("#1,#2,#3", result);
        }

        @Test
        @DisplayName("Format with custom object")
        void formatCustomObject() {
            record Person(String name, int age) {}
            String result = Joiner.on("; ")
                    .withFormatter(obj -> ((Person) obj).name())
                    .join(new Person("Alice", 30), new Person("Bob", 25));
            assertEquals("Alice; Bob", result);
        }
    }

    @Nested
    @DisplayName("Null Handling Without Configuration")
    class NullWithoutConfigTests {

        @Test
        @DisplayName("Null without config throws exception")
        void nullWithoutConfig() {
            Joiner joiner = Joiner.on(',');
            assertThrows(NullPointerException.class, () -> joiner.join("a", null, "b"));
        }

        @Test
        @DisplayName("Null in list throws exception")
        void nullInListThrows() {
            Joiner joiner = Joiner.on(',');
            List<String> list = Arrays.asList("a", null, "b");
            assertThrows(NullPointerException.class, () -> joiner.join(list));
        }
    }

    // ==================== Join Methods Tests ====================

    @Nested
    @DisplayName("Join Methods - join(Object...)")
    class JoinVarargsTests {

        @Test
        @DisplayName("Join varargs")
        void joinVarargs() {
            String result = Joiner.on(',').join("a", "b", "c");
            assertEquals("a,b,c", result);
        }

        @Test
        @DisplayName("Join mixed types")
        void joinMixedTypes() {
            String result = Joiner.on(',').join("a", 1, 2.5, true);
            assertEquals("a,1,2.5,true", result);
        }
    }

    @Nested
    @DisplayName("Join Methods - join(Iterable)")
    class JoinIterableTests {

        @Test
        @DisplayName("Join list")
        void joinList() {
            List<String> list = List.of("a", "b", "c");
            String result = Joiner.on(',').join(list);
            assertEquals("a,b,c", result);
        }

        @Test
        @DisplayName("Join set (order preserved for LinkedHashSet)")
        void joinSet() {
            Set<String> set = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
            String result = Joiner.on(',').join(set);
            assertEquals("a,b,c", result);
        }

        @Test
        @DisplayName("Join empty list")
        void joinEmptyList() {
            String result = Joiner.on(',').join(List.of());
            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Join Methods - join(Iterator)")
    class JoinIteratorTests {

        @Test
        @DisplayName("Join iterator")
        void joinIterator() {
            Iterator<String> iterator = List.of("x", "y", "z").iterator();
            String result = Joiner.on(',').join(iterator);
            assertEquals("x,y,z", result);
        }

        @Test
        @DisplayName("Join empty iterator")
        void joinEmptyIterator() {
            Iterator<String> iterator = Collections.emptyIterator();
            String result = Joiner.on(',').join(iterator);
            assertEquals("", result);
        }
    }

    // ==================== Append Methods Tests ====================

    @Nested
    @DisplayName("Append Methods - appendTo(StringBuilder)")
    class AppendToStringBuilderTests {

        @Test
        @DisplayName("Append to StringBuilder with varargs")
        void appendToStringBuilderVarargs() {
            StringBuilder sb = new StringBuilder("prefix: ");
            Joiner.on(',').appendTo(sb, "a", "b", "c");
            assertEquals("prefix: a,b,c", sb.toString());
        }

        @Test
        @DisplayName("Append to StringBuilder with iterable")
        void appendToStringBuilderIterable() {
            StringBuilder sb = new StringBuilder("items: ");
            Joiner.on(',').appendTo(sb, List.of("x", "y", "z"));
            assertEquals("items: x,y,z", sb.toString());
        }

        @Test
        @DisplayName("Append returns StringBuilder")
        void appendReturnsStringBuilder() {
            StringBuilder sb = new StringBuilder();
            StringBuilder returned = Joiner.on(',').appendTo(sb, "a", "b");
            assertSame(sb, returned);
        }

        @Test
        @DisplayName("Append empty to StringBuilder")
        void appendEmptyToStringBuilder() {
            StringBuilder sb = new StringBuilder("test");
            Joiner.on(',').appendTo(sb);
            assertEquals("test", sb.toString());
        }
    }

    @Nested
    @DisplayName("Append Methods - appendTo(Appendable)")
    class AppendToAppendableTests {

        @Test
        @DisplayName("Append to StringWriter")
        void appendToStringWriter() throws IOException {
            StringWriter writer = new StringWriter();
            writer.write("prefix: ");
            Joiner.on(',').appendTo(writer, "a", "b", "c");
            assertEquals("prefix: a,b,c", writer.toString());
        }

        @Test
        @DisplayName("Append to Appendable with iterable")
        void appendToAppendableIterable() throws IOException {
            StringWriter writer = new StringWriter();
            Joiner.on(',').appendTo(writer, List.of("1", "2", "3"));
            assertEquals("1,2,3", writer.toString());
        }

        @Test
        @DisplayName("Append to Appendable with iterator")
        void appendToAppendableIterator() throws IOException {
            StringWriter writer = new StringWriter();
            Joiner.on(',').appendTo(writer, List.of("a", "b").iterator());
            assertEquals("a,b", writer.toString());
        }

        @Test
        @DisplayName("Null appendable throws exception")
        void nullAppendable() {
            assertThrows(NullPointerException.class,
                () -> Joiner.on(',').appendTo((Appendable) null, "a", "b"));
        }
    }

    // ==================== Map Joiner Tests ====================

    @Nested
    @DisplayName("Map Joiner - withKeyValueSeparator(char)")
    class MapJoinerCharTests {

        @Test
        @DisplayName("Join map with char separator")
        void joinMap() {
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("a", 1);
            map.put("b", 2);
            map.put("c", 3);

            String result = Joiner.on(',').withKeyValueSeparator('=').join(map);
            assertEquals("a=1,b=2,c=3", result);
        }

        @Test
        @DisplayName("Join empty map")
        void joinEmptyMap() {
            String result = Joiner.on(',').withKeyValueSeparator('=').join(Map.of());
            assertEquals("", result);
        }

        @Test
        @DisplayName("Join single entry map")
        void joinSingleEntryMap() {
            String result = Joiner.on(',').withKeyValueSeparator('=').join(Map.of("key", "value"));
            assertEquals("key=value", result);
        }
    }

    @Nested
    @DisplayName("Map Joiner - withKeyValueSeparator(String)")
    class MapJoinerStringTests {

        @Test
        @DisplayName("Join map with string separator")
        void joinMapStringSep() {
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("name", 1);
            map.put("age", 2);

            String result = Joiner.on(" | ").withKeyValueSeparator(" -> ").join(map);
            assertEquals("name -> 1 | age -> 2", result);
        }
    }

    @Nested
    @DisplayName("Map Joiner - join(Iterable<Entry>)")
    class MapJoinerEntriesTests {

        @Test
        @DisplayName("Join entries iterable")
        void joinEntries() {
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("x", 10);
            map.put("y", 20);

            String result = Joiner.on(',').withKeyValueSeparator('=').join(map.entrySet());
            assertEquals("x=10,y=20", result);
        }
    }

    @Nested
    @DisplayName("Map Joiner - skipNullValues()")
    class MapJoinerSkipNullValuesTests {

        @Test
        @DisplayName("Skip null values in map")
        void skipNullValues() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("a", "1");
            map.put("b", null);
            map.put("c", "3");

            String result = Joiner.on(',').withKeyValueSeparator('=')
                    .skipNullValues().join(map);
            assertEquals("a=1,c=3", result);
        }

        @Test
        @DisplayName("Skip all null values")
        void skipAllNullValues() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("a", null);
            map.put("b", null);

            String result = Joiner.on(',').withKeyValueSeparator('=')
                    .skipNullValues().join(map);
            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Map Joiner - useForNull()")
    class MapJoinerUseForNullTests {

        @Test
        @DisplayName("Replace null values in map")
        void useForNullInMap() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("a", "1");
            map.put("b", null);
            map.put("c", "3");

            String result = Joiner.on(',').withKeyValueSeparator('=')
                    .useForNull("N/A").join(map);
            assertEquals("a=1,b=N/A,c=3", result);
        }

        @Test
        @DisplayName("Null useForNull throws exception")
        void nullUseForNull() {
            assertThrows(NullPointerException.class, () ->
                Joiner.on(',').withKeyValueSeparator('=').useForNull(null));
        }
    }

    @Nested
    @DisplayName("Map Joiner - null values without config")
    class MapJoinerNullWithoutConfigTests {

        @Test
        @DisplayName("Null value without config throws exception")
        void nullValueThrows() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("a", "1");
            map.put("b", null);

            Joiner.MapJoiner mapJoiner = Joiner.on(',').withKeyValueSeparator('=');
            assertThrows(NullPointerException.class, () -> mapJoiner.join(map));
        }
    }

    @Nested
    @DisplayName("Map Joiner - appendTo methods")
    class MapJoinerAppendToTests {

        @Test
        @DisplayName("Append map to StringBuilder")
        void appendToStringBuilder() {
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("a", 1);
            map.put("b", 2);

            StringBuilder sb = new StringBuilder("data: ");
            Joiner.on(',').withKeyValueSeparator('=').appendTo(sb, map);
            assertEquals("data: a=1,b=2", sb.toString());
        }

        @Test
        @DisplayName("Append entries to StringBuilder")
        void appendEntriesToStringBuilder() {
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("x", 10);

            StringBuilder sb = new StringBuilder();
            Joiner.on(',').withKeyValueSeparator('=').appendTo(sb, map.entrySet());
            assertEquals("x=10", sb.toString());
        }

        @Test
        @DisplayName("Append map to Appendable")
        void appendToAppendable() throws IOException {
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("a", 1);
            map.put("b", 2);

            StringWriter writer = new StringWriter();
            Joiner.on(',').withKeyValueSeparator('=').appendTo(writer, map);
            assertEquals("a=1,b=2", writer.toString());
        }

        @Test
        @DisplayName("Append entries to Appendable")
        void appendEntriesToAppendable() throws IOException {
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("key", 100);

            StringWriter writer = new StringWriter();
            Joiner.on(',').withKeyValueSeparator('=').appendTo(writer, map.entrySet());
            assertEquals("key=100", writer.toString());
        }
    }

    // ==================== Edge Cases Tests ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Unicode characters")
        void unicodeCharacters() {
            String result = Joiner.on(',').join("你好", "世界", "测试");
            assertEquals("你好,世界,测试", result);
        }

        @Test
        @DisplayName("Unicode separator")
        void unicodeSeparator() {
            String result = Joiner.on("→").join("a", "b", "c");
            assertEquals("a→b→c", result);
        }

        @Test
        @DisplayName("Very long input")
        void veryLongInput() {
            List<String> parts = Collections.nCopies(1000, "x");
            String result = Joiner.on(',').join(parts);
            assertEquals(1999, result.length()); // 1000 "x" + 999 ","
        }

        @Test
        @DisplayName("Empty strings in input")
        void emptyStringsInInput() {
            String result = Joiner.on(',').join("", "a", "", "b", "");
            assertEquals(",a,,b,", result);
        }

        @Test
        @DisplayName("Separator contains special characters")
        void separatorWithSpecialChars() {
            String result = Joiner.on("\n\t").join("a", "b", "c");
            assertEquals("a\n\tb\n\tc", result);
        }
    }

    // ==================== Immutability Tests ====================

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Joiner is immutable")
        void joinerIsImmutable() {
            Joiner original = Joiner.on(',');
            Joiner withSkipNulls = original.skipNulls();
            Joiner withUseForNull = original.useForNull("N/A");

            // Original should throw on null
            assertThrows(NullPointerException.class, () -> original.join("a", null));

            // withSkipNulls should skip
            assertEquals("a,b", withSkipNulls.join("a", null, "b"));

            // withUseForNull should replace
            assertEquals("a,N/A,b", withUseForNull.join("a", null, "b"));
        }

        @Test
        @DisplayName("MapJoiner is immutable")
        void mapJoinerIsImmutable() {
            Joiner.MapJoiner original = Joiner.on(',').withKeyValueSeparator('=');
            Joiner.MapJoiner withSkip = original.skipNullValues();
            Joiner.MapJoiner withReplace = original.useForNull("-");

            Map<String, String> map = new LinkedHashMap<>();
            map.put("a", null);

            // Original should throw on null
            assertThrows(NullPointerException.class, () -> original.join(map));

            // withSkip should skip
            assertEquals("", withSkip.join(map));

            // withReplace should replace
            assertEquals("a=-", withReplace.join(map));
        }
    }

    // ==================== Chaining Tests ====================

    @Nested
    @DisplayName("Chaining Tests")
    class ChainingTests {

        @Test
        @DisplayName("Chain skipNulls and formatter")
        void chainSkipNullsAndFormatter() {
            String result = Joiner.on(',')
                    .skipNulls()
                    .withFormatter(Object::toString)
                    .join("a", null, "b");
            assertEquals("a,b", result);
        }

        @Test
        @DisplayName("Chain useForNull and formatter")
        void chainUseForNullAndFormatter() {
            String result = Joiner.on(',')
                    .useForNull("null")
                    .withFormatter(s -> "[" + s + "]")
                    .join("a", null, "b");
            assertEquals("[a],[null],[b]", result);
        }
    }
}
