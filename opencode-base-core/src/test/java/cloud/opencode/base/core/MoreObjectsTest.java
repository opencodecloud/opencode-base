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

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for MoreObjects class
 * MoreObjects 类的全面测试
 *
 * @author Test
 * @since JDK 25, opencode-base-core V1.0.0
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("MoreObjects Tests")
class MoreObjectsTest {

    // ==================== firstNonNull Tests ====================

    @Nested
    @DisplayName("firstNonNull() Tests")
    class FirstNonNullTests {

        @Test
        @DisplayName("First is non-null")
        void firstNonNull_FirstIsNonNull() {
            String result = MoreObjects.firstNonNull("first", "second");
            assertEquals("first", result);
        }

        @Test
        @DisplayName("First is null, second is non-null")
        void firstNonNull_FirstIsNull() {
            String result = MoreObjects.firstNonNull(null, "second");
            assertEquals("second", result);
        }

        @Test
        @DisplayName("Both are null")
        void firstNonNull_BothNull() {
            String result = MoreObjects.firstNonNull(null, null);
            assertNull(result);
        }

        @Test
        @DisplayName("Both are non-null, returns first")
        void firstNonNull_BothNonNull() {
            Integer result = MoreObjects.firstNonNull(1, 2);
            assertEquals(1, result);
        }

        @Test
        @DisplayName("Works with different types")
        void firstNonNull_DifferentTypes() {
            Object result = MoreObjects.firstNonNull(null, 42);
            assertEquals(42, result);
        }
    }

    // ==================== toStringHelper Factory Tests ====================

    @Nested
    @DisplayName("toStringHelper Factory Methods")
    class ToStringHelperFactoryTests {

        @Test
        @DisplayName("toStringHelper with Object")
        void toStringHelperWithObject() {
            String result = MoreObjects.toStringHelper(new TestClass())
                    .add("name", "test")
                    .toString();
            assertEquals("TestClass{name=test}", result);
        }

        @Test
        @DisplayName("toStringHelper with Class")
        void toStringHelperWithClass() {
            String result = MoreObjects.toStringHelper(String.class)
                    .add("value", "test")
                    .toString();
            assertEquals("String{value=test}", result);
        }

        @Test
        @DisplayName("toStringHelper with String")
        void toStringHelperWithString() {
            String result = MoreObjects.toStringHelper("CustomName")
                    .add("key", "value")
                    .toString();
            assertEquals("CustomName{key=value}", result);
        }

        @Test
        @DisplayName("toStringHelper with empty values")
        void toStringHelperEmpty() {
            String result = MoreObjects.toStringHelper("Empty").toString();
            assertEquals("Empty{}", result);
        }

        // Test helper class
        private static class TestClass {}
    }

    // ==================== ToStringHelper add() Tests ====================

    @Nested
    @DisplayName("ToStringHelper add() Methods")
    class AddMethodsTests {

        @Test
        @DisplayName("add with Object value")
        void addObject() {
            String result = MoreObjects.toStringHelper("Test")
                    .add("name", "Alice")
                    .toString();
            assertEquals("Test{name=Alice}", result);
        }

        @Test
        @DisplayName("add with null Object value")
        void addNullObject() {
            String result = MoreObjects.toStringHelper("Test")
                    .add("name", (Object) null)
                    .toString();
            assertEquals("Test{name=null}", result);
        }

        @Test
        @DisplayName("add with boolean value")
        void addBoolean() {
            String result = MoreObjects.toStringHelper("Test")
                    .add("active", true)
                    .add("deleted", false)
                    .toString();
            assertEquals("Test{active=true, deleted=false}", result);
        }

        @Test
        @DisplayName("add with char value")
        void addChar() {
            String result = MoreObjects.toStringHelper("Test")
                    .add("grade", 'A')
                    .toString();
            assertEquals("Test{grade=A}", result);
        }

        @Test
        @DisplayName("add with int value")
        void addInt() {
            String result = MoreObjects.toStringHelper("Test")
                    .add("count", 42)
                    .toString();
            assertEquals("Test{count=42}", result);
        }

        @Test
        @DisplayName("add with long value")
        void addLong() {
            String result = MoreObjects.toStringHelper("Test")
                    .add("bigNumber", 9876543210L)
                    .toString();
            assertEquals("Test{bigNumber=9876543210}", result);
        }

        @Test
        @DisplayName("add with float value")
        void addFloat() {
            String result = MoreObjects.toStringHelper("Test")
                    .add("ratio", 3.14f)
                    .toString();
            assertEquals("Test{ratio=3.14}", result);
        }

        @Test
        @DisplayName("add with double value")
        void addDouble() {
            String result = MoreObjects.toStringHelper("Test")
                    .add("pi", 3.14159)
                    .toString();
            assertEquals("Test{pi=3.14159}", result);
        }

        @Test
        @DisplayName("add multiple values")
        void addMultipleValues() {
            String result = MoreObjects.toStringHelper("Person")
                    .add("name", "John")
                    .add("age", 30)
                    .add("active", true)
                    .toString();
            assertEquals("Person{name=John, age=30, active=true}", result);
        }
    }

    // ==================== ToStringHelper addValue() Tests ====================

    @Nested
    @DisplayName("ToStringHelper addValue() Methods")
    class AddValueMethodsTests {

        @Test
        @DisplayName("addValue with Object")
        void addValueObject() {
            String result = MoreObjects.toStringHelper("Point")
                    .addValue("x")
                    .addValue("y")
                    .toString();
            assertEquals("Point{x, y}", result);
        }

        @Test
        @DisplayName("addValue with null Object")
        void addValueNullObject() {
            String result = MoreObjects.toStringHelper("Test")
                    .addValue((Object) null)
                    .toString();
            assertEquals("Test{null}", result);
        }

        @Test
        @DisplayName("addValue with boolean")
        void addValueBoolean() {
            String result = MoreObjects.toStringHelper("Test")
                    .addValue(true)
                    .addValue(false)
                    .toString();
            assertEquals("Test{true, false}", result);
        }

        @Test
        @DisplayName("addValue with char")
        void addValueChar() {
            String result = MoreObjects.toStringHelper("Test")
                    .addValue('X')
                    .toString();
            assertEquals("Test{X}", result);
        }

        @Test
        @DisplayName("addValue with int")
        void addValueInt() {
            String result = MoreObjects.toStringHelper("Coords")
                    .addValue(10)
                    .addValue(20)
                    .toString();
            assertEquals("Coords{10, 20}", result);
        }

        @Test
        @DisplayName("addValue with long")
        void addValueLong() {
            String result = MoreObjects.toStringHelper("Test")
                    .addValue(123456789012345L)
                    .toString();
            assertEquals("Test{123456789012345}", result);
        }

        @Test
        @DisplayName("addValue with float")
        void addValueFloat() {
            String result = MoreObjects.toStringHelper("Test")
                    .addValue(1.5f)
                    .toString();
            assertEquals("Test{1.5}", result);
        }

        @Test
        @DisplayName("addValue with double")
        void addValueDouble() {
            String result = MoreObjects.toStringHelper("Test")
                    .addValue(2.71828)
                    .toString();
            assertEquals("Test{2.71828}", result);
        }
    }

    // ==================== ToStringHelper omitNullValues() Tests ====================

    @Nested
    @DisplayName("ToStringHelper omitNullValues() Tests")
    class OmitNullValuesTests {

        @Test
        @DisplayName("omitNullValues removes null named values")
        void omitNullNamedValues() {
            String result = MoreObjects.toStringHelper("Test")
                    .omitNullValues()
                    .add("name", "Alice")
                    .add("nickname", null)
                    .add("age", 25)
                    .toString();
            assertEquals("Test{name=Alice, age=25}", result);
        }

        @Test
        @DisplayName("omitNullValues removes null unnamed values")
        void omitNullUnnamedValues() {
            String result = MoreObjects.toStringHelper("Test")
                    .omitNullValues()
                    .addValue("value1")
                    .addValue(null)
                    .addValue("value2")
                    .toString();
            assertEquals("Test{value1, value2}", result);
        }

        @Test
        @DisplayName("omitNullValues with all nulls")
        void omitNullAllNulls() {
            String result = MoreObjects.toStringHelper("Test")
                    .omitNullValues()
                    .add("a", null)
                    .add("b", null)
                    .toString();
            assertEquals("Test{}", result);
        }

        @Test
        @DisplayName("omitNullValues keeps non-null values")
        void omitNullKeepsNonNull() {
            String result = MoreObjects.toStringHelper("Test")
                    .omitNullValues()
                    .add("value", "present")
                    .toString();
            assertEquals("Test{value=present}", result);
        }

        @Test
        @DisplayName("Without omitNullValues, null is shown")
        void withoutOmitNullValuesShowsNull() {
            String result = MoreObjects.toStringHelper("Test")
                    .add("name", null)
                    .toString();
            assertEquals("Test{name=null}", result);
        }

        @Test
        @DisplayName("omitNullValues returns same helper for chaining")
        void omitNullValuesChaining() {
            MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("Test");
            assertSame(helper, helper.omitNullValues());
        }
    }

    // ==================== Array Formatting Tests ====================

    @Nested
    @DisplayName("Array Formatting Tests")
    class ArrayFormattingTests {

        @Test
        @DisplayName("Format Object array")
        void formatObjectArray() {
            String[] arr = {"a", "b", "c"};
            String result = MoreObjects.toStringHelper("Test")
                    .add("arr", arr)
                    .toString();
            assertEquals("Test{arr=[a, b, c]}", result);
        }

        @Test
        @DisplayName("Format nested Object array")
        void formatNestedObjectArray() {
            String[][] arr = {{"a", "b"}, {"c", "d"}};
            String result = MoreObjects.toStringHelper("Test")
                    .add("arr", arr)
                    .toString();
            assertEquals("Test{arr=[[a, b], [c, d]]}", result);
        }

        @Test
        @DisplayName("Format int array")
        void formatIntArray() {
            int[] arr = {1, 2, 3};
            String result = MoreObjects.toStringHelper("Test")
                    .add("arr", arr)
                    .toString();
            assertEquals("Test{arr=[1, 2, 3]}", result);
        }

        @Test
        @DisplayName("Format long array")
        void formatLongArray() {
            long[] arr = {10L, 20L, 30L};
            String result = MoreObjects.toStringHelper("Test")
                    .add("arr", arr)
                    .toString();
            assertEquals("Test{arr=[10, 20, 30]}", result);
        }

        @Test
        @DisplayName("Format double array")
        void formatDoubleArray() {
            double[] arr = {1.1, 2.2, 3.3};
            String result = MoreObjects.toStringHelper("Test")
                    .add("arr", arr)
                    .toString();
            assertEquals("Test{arr=[1.1, 2.2, 3.3]}", result);
        }

        @Test
        @DisplayName("Format float array")
        void formatFloatArray() {
            float[] arr = {1.0f, 2.0f, 3.0f};
            String result = MoreObjects.toStringHelper("Test")
                    .add("arr", arr)
                    .toString();
            assertEquals("Test{arr=[1.0, 2.0, 3.0]}", result);
        }

        @Test
        @DisplayName("Format boolean array")
        void formatBooleanArray() {
            boolean[] arr = {true, false, true};
            String result = MoreObjects.toStringHelper("Test")
                    .add("arr", arr)
                    .toString();
            assertEquals("Test{arr=[true, false, true]}", result);
        }

        @Test
        @DisplayName("Format byte array")
        void formatByteArray() {
            byte[] arr = {1, 2, 3};
            String result = MoreObjects.toStringHelper("Test")
                    .add("arr", arr)
                    .toString();
            assertEquals("Test{arr=[1, 2, 3]}", result);
        }

        @Test
        @DisplayName("Format char array")
        void formatCharArray() {
            char[] arr = {'a', 'b', 'c'};
            String result = MoreObjects.toStringHelper("Test")
                    .add("arr", arr)
                    .toString();
            assertEquals("Test{arr=[a, b, c]}", result);
        }

        @Test
        @DisplayName("Format short array")
        void formatShortArray() {
            short[] arr = {1, 2, 3};
            String result = MoreObjects.toStringHelper("Test")
                    .add("arr", arr)
                    .toString();
            assertEquals("Test{arr=[1, 2, 3]}", result);
        }

        @Test
        @DisplayName("Format empty array")
        void formatEmptyArray() {
            int[] arr = {};
            String result = MoreObjects.toStringHelper("Test")
                    .add("arr", arr)
                    .toString();
            assertEquals("Test{arr=[]}", result);
        }
    }

    // ==================== Mixed Named and Unnamed Values Tests ====================

    @Nested
    @DisplayName("Mixed Values Tests")
    class MixedValuesTests {

        @Test
        @DisplayName("Mix named and unnamed values")
        void mixNamedAndUnnamed() {
            String result = MoreObjects.toStringHelper("Test")
                    .add("name", "Alice")
                    .addValue(42)
                    .add("active", true)
                    .addValue("extra")
                    .toString();
            assertEquals("Test{name=Alice, 42, active=true, extra}", result);
        }

        @Test
        @DisplayName("Only unnamed values")
        void onlyUnnamed() {
            String result = MoreObjects.toStringHelper("Tuple")
                    .addValue(1)
                    .addValue(2)
                    .addValue(3)
                    .toString();
            assertEquals("Tuple{1, 2, 3}", result);
        }
    }

    // ==================== Edge Cases Tests ====================

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Empty class name")
        void emptyClassName() {
            String result = MoreObjects.toStringHelper("")
                    .add("x", 1)
                    .toString();
            assertEquals("{x=1}", result);
        }

        @Test
        @DisplayName("Special characters in values")
        void specialCharactersInValues() {
            String result = MoreObjects.toStringHelper("Test")
                    .add("text", "hello\nworld")
                    .add("path", "C:\\temp")
                    .toString();
            assertEquals("Test{text=hello\nworld, path=C:\\temp}", result);
        }

        @Test
        @DisplayName("Unicode characters")
        void unicodeCharacters() {
            String result = MoreObjects.toStringHelper("Test")
                    .add("chinese", "你好")
                    .add("emoji", "😀")
                    .toString();
            assertEquals("Test{chinese=你好, emoji=😀}", result);
        }

        @Test
        @DisplayName("Very long value")
        void veryLongValue() {
            String longValue = "x".repeat(1000);
            String result = MoreObjects.toStringHelper("Test")
                    .add("long", longValue)
                    .toString();
            assertTrue(result.contains(longValue));
        }

        @Test
        @DisplayName("Custom object toString")
        void customObjectToString() {
            record Custom(int x, int y) {
                @Override
                public String toString() {
                    return "(" + x + "," + y + ")";
                }
            }

            String result = MoreObjects.toStringHelper("Test")
                    .add("point", new Custom(10, 20))
                    .toString();
            assertEquals("Test{point=(10,20)}", result);
        }

        @Test
        @DisplayName("Empty name in add")
        void emptyNameInAdd() {
            String result = MoreObjects.toStringHelper("Test")
                    .add("", "value")
                    .toString();
            assertEquals("Test{=value}", result);
        }
    }

    // ==================== Chaining Tests ====================

    @Nested
    @DisplayName("Chaining Tests")
    class ChainingTests {

        @Test
        @DisplayName("All methods return helper for chaining")
        void allMethodsChaining() {
            MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("Test");

            assertSame(helper, helper.omitNullValues());
            assertSame(helper, helper.add("a", "b"));
            assertSame(helper, helper.add("bool", true));
            assertSame(helper, helper.add("char", 'c'));
            assertSame(helper, helper.add("int", 1));
            assertSame(helper, helper.add("long", 1L));
            assertSame(helper, helper.add("float", 1.0f));
            assertSame(helper, helper.add("double", 1.0));
            assertSame(helper, helper.addValue("value"));
            assertSame(helper, helper.addValue(true));
            assertSame(helper, helper.addValue('x'));
            assertSame(helper, helper.addValue(42));
            assertSame(helper, helper.addValue(42L));
            assertSame(helper, helper.addValue(1.5f));
            assertSame(helper, helper.addValue(2.5));
        }

        @Test
        @DisplayName("Fluent API usage")
        void fluentApiUsage() {
            String result = MoreObjects.toStringHelper("User")
                    .omitNullValues()
                    .add("id", 1)
                    .add("name", "Alice")
                    .add("email", null)
                    .add("active", true)
                    .add("score", 98.5)
                    .toString();

            assertEquals("User{id=1, name=Alice, active=true, score=98.5}", result);
        }
    }

    // ==================== toString() Multiple Calls Tests ====================

    @Nested
    @DisplayName("Multiple toString() Calls Tests")
    class MultipleToStringCallsTests {

        @Test
        @DisplayName("toString can be called multiple times")
        void toStringMultipleCalls() {
            MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("Test")
                    .add("x", 1);

            String first = helper.toString();
            String second = helper.toString();

            assertEquals(first, second);
        }

        @Test
        @DisplayName("Adding after toString affects next call")
        void addingAfterToString() {
            MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("Test")
                    .add("x", 1);

            String first = helper.toString();
            helper.add("y", 2);
            String second = helper.toString();

            assertEquals("Test{x=1}", first);
            assertEquals("Test{x=1, y=2}", second);
        }
    }

}
