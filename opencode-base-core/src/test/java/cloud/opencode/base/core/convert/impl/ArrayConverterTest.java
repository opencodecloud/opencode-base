package cloud.opencode.base.core.convert.impl;

import cloud.opencode.base.core.convert.Converter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ArrayConverter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("ArrayConverter 测试")
class ArrayConverterTest {

    @Nested
    @DisplayName("原始类型数组转换测试")
    class PrimitiveArrayConversionTests {

        @Test
        @DisplayName("intArrayConverter 字符串")
        void testIntArrayFromString() {
            Converter<int[]> converter = ArrayConverter.intArrayConverter();

            assertThat(converter.convert("1,2,3")).containsExactly(1, 2, 3);
            assertThat(converter.convert("10")).containsExactly(10);
            assertThat(converter.convert("")).isEmpty();
        }

        @Test
        @DisplayName("intArrayConverter 集合")
        void testIntArrayFromCollection() {
            Converter<int[]> converter = ArrayConverter.intArrayConverter();

            assertThat(converter.convert(List.of(1, 2, 3))).containsExactly(1, 2, 3);
            assertThat(converter.convert(Set.of(1))).containsExactly(1);
        }

        @Test
        @DisplayName("intArrayConverter 数组")
        void testIntArrayFromArray() {
            Converter<int[]> converter = ArrayConverter.intArrayConverter();

            int[] original = {1, 2, 3};
            assertThat(converter.convert(original)).containsExactly(1, 2, 3);

            Integer[] boxed = {1, 2, 3};
            assertThat(converter.convert(boxed)).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("longArrayConverter")
        void testLongArray() {
            Converter<long[]> converter = ArrayConverter.longArrayConverter();

            assertThat(converter.convert("1,2,3")).containsExactly(1L, 2L, 3L);
            assertThat(converter.convert(List.of(1L, 2L, 3L))).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("doubleArrayConverter")
        void testDoubleArray() {
            Converter<double[]> converter = ArrayConverter.doubleArrayConverter();

            assertThat(converter.convert("1.1,2.2,3.3")).containsExactly(1.1, 2.2, 3.3);
            assertThat(converter.convert(List.of(1.5, 2.5))).containsExactly(1.5, 2.5);
        }

        @Test
        @DisplayName("floatArrayConverter")
        void testFloatArray() {
            Converter<float[]> converter = ArrayConverter.floatArrayConverter();

            assertThat(converter.convert("1.1,2.2")).containsExactly(1.1f, 2.2f);
            assertThat(converter.convert(List.of(3.14f))).containsExactly(3.14f);
        }

        @Test
        @DisplayName("booleanArrayConverter")
        void testBooleanArray() {
            Converter<boolean[]> converter = ArrayConverter.booleanArrayConverter();

            assertThat(converter.convert("true,false,true")).containsExactly(true, false, true);
            assertThat(converter.convert(List.of(true, false))).containsExactly(true, false);
        }

        @Test
        @DisplayName("byteArrayConverter")
        void testByteArray() {
            Converter<byte[]> converter = ArrayConverter.byteArrayConverter();

            assertThat(converter.convert("1,2,3")).containsExactly((byte) 1, (byte) 2, (byte) 3);
            assertThat(converter.convert(List.of((byte) 10, (byte) 20))).containsExactly((byte) 10, (byte) 20);
        }

        @Test
        @DisplayName("shortArrayConverter")
        void testShortArray() {
            Converter<short[]> converter = ArrayConverter.shortArrayConverter();

            assertThat(converter.convert("100,200,300")).containsExactly((short) 100, (short) 200, (short) 300);
        }

        @Test
        @DisplayName("charArrayConverter 字符串")
        void testCharArrayFromString() {
            Converter<char[]> converter = ArrayConverter.charArrayConverter();

            assertThat(converter.convert("Hello")).containsExactly('H', 'e', 'l', 'l', 'o');
            assertThat(converter.convert("ABC")).containsExactly('A', 'B', 'C');
        }

        @Test
        @DisplayName("charArrayConverter CharSequence")
        void testCharArrayFromCharSequence() {
            Converter<char[]> converter = ArrayConverter.charArrayConverter();

            StringBuilder sb = new StringBuilder("World");
            assertThat(converter.convert(sb)).containsExactly('W', 'o', 'r', 'l', 'd');
        }

        @Test
        @DisplayName("charArrayConverter 已是 char[]")
        void testCharArrayFromCharArray() {
            Converter<char[]> converter = ArrayConverter.charArrayConverter();

            char[] original = {'a', 'b', 'c'};
            assertThat(converter.convert(original)).isSameAs(original);
        }
    }

    @Nested
    @DisplayName("对象数组转换测试")
    class ObjectArrayConversionTests {

        @Test
        @DisplayName("stringArrayConverter 字符串")
        void testStringArrayFromString() {
            Converter<String[]> converter = ArrayConverter.stringArrayConverter();

            assertThat(converter.convert("a,b,c")).containsExactly("a", "b", "c");
            assertThat(converter.convert("hello")).containsExactly("hello");
        }

        @Test
        @DisplayName("stringArrayConverter 集合")
        void testStringArrayFromCollection() {
            Converter<String[]> converter = ArrayConverter.stringArrayConverter();

            assertThat(converter.convert(List.of("x", "y", "z"))).containsExactly("x", "y", "z");
        }

        @Test
        @DisplayName("stringArrayConverter 数组")
        void testStringArrayFromArray() {
            Converter<String[]> converter = ArrayConverter.stringArrayConverter();

            String[] original = {"a", "b"};
            assertThat(converter.convert(original)).isSameAs(original);
        }

        @Test
        @DisplayName("integerArrayConverter")
        void testIntegerArray() {
            Converter<Integer[]> converter = ArrayConverter.integerArrayConverter();

            assertThat(converter.convert("1,2,3")).containsExactly(1, 2, 3);
            assertThat(converter.convert(List.of(10, 20))).containsExactly(10, 20);
        }

        @Test
        @DisplayName("longObjArrayConverter")
        void testLongObjArray() {
            Converter<Long[]> converter = ArrayConverter.longObjArrayConverter();

            assertThat(converter.convert("1,2,3")).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("doubleObjArrayConverter")
        void testDoubleObjArray() {
            Converter<Double[]> converter = ArrayConverter.doubleObjArrayConverter();

            assertThat(converter.convert("1.1,2.2")).containsExactly(1.1, 2.2);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of Class")
        void testOfClass() {
            Converter<String[]> converter = ArrayConverter.of(String.class);

            assertThat(converter.convert("a,b,c")).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("of Class and separator")
        void testOfClassAndSeparator() {
            Converter<String[]> converter = ArrayConverter.of(String.class, ";");

            assertThat(converter.convert("a;b;c")).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("自定义分隔符")
        void testCustomSeparator() {
            Converter<Integer[]> converter = ArrayConverter.of(Integer.class, "|");

            assertThat(converter.convert("1|2|3")).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("Iterable 和 Iterator 转换测试")
    class IterableIteratorTests {

        @Test
        @DisplayName("Iterable 转数组")
        void testIterableToArray() {
            Converter<String[]> converter = ArrayConverter.stringArrayConverter();

            Iterable<String> iterable = () -> List.of("a", "b", "c").iterator();
            assertThat(converter.convert(iterable)).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Iterator 转数组")
        void testIteratorToArray() {
            Converter<String[]> converter = ArrayConverter.stringArrayConverter();

            Iterator<String> iterator = List.of("x", "y").iterator();
            assertThat(converter.convert(iterator)).containsExactly("x", "y");
        }
    }

    @Nested
    @DisplayName("单个对象转换测试")
    class SingleObjectTests {

        @Test
        @DisplayName("单个对象转单元素数组")
        void testSingleObjectToArray() {
            Converter<String[]> converter = ArrayConverter.stringArrayConverter();

            assertThat(converter.convert(123)).containsExactly("123");
        }

        @Test
        @DisplayName("单个数字转数组")
        void testSingleNumberToArray() {
            Converter<Integer[]> converter = ArrayConverter.integerArrayConverter();

            assertThat(converter.convert(42)).containsExactly(42);
        }
    }

    @Nested
    @DisplayName("null 和默认值处理测试")
    class NullAndDefaultHandlingTests {

        @Test
        @DisplayName("null 返回默认值")
        void testNullReturnsDefault() {
            Converter<int[]> converter = ArrayConverter.intArrayConverter();

            int[] defaultArr = {0};
            assertThat(converter.convert(null, defaultArr)).isSameAs(defaultArr);
        }

        @Test
        @DisplayName("null 无默认值返回 null")
        void testNullWithoutDefaultReturnsNull() {
            Converter<int[]> converter = ArrayConverter.intArrayConverter();

            assertThat(converter.convert(null)).isNull();
        }

        @Test
        @DisplayName("空字符串返回空数组")
        void testEmptyStringReturnsEmptyArray() {
            Converter<int[]> converter = ArrayConverter.intArrayConverter();

            assertThat(converter.convert("", new int[0])).isEmpty();
        }

        @Test
        @DisplayName("空格字符串处理")
        void testWhitespaceStringHandling() {
            Converter<int[]> converter = ArrayConverter.intArrayConverter();

            assertThat(converter.convert("   ", new int[0])).isEmpty();
        }
    }

    @Nested
    @DisplayName("带空格的字符串处理测试")
    class WhitespaceHandlingTests {

        @Test
        @DisplayName("字符串元素带空格自动 trim")
        void testStringElementsTrimmed() {
            Converter<String[]> converter = ArrayConverter.stringArrayConverter();

            assertThat(converter.convert(" a , b , c ")).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("数字字符串元素带空格自动 trim")
        void testNumericElementsTrimmed() {
            Converter<int[]> converter = ArrayConverter.intArrayConverter();

            assertThat(converter.convert(" 1 , 2 , 3 ")).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("类型转换测试")
    class TypeConversionTests {

        @Test
        @DisplayName("数组类型转换")
        void testArrayTypeConversion() {
            Converter<Integer[]> converter = ArrayConverter.integerArrayConverter();

            // 从 int[] 转换为 Integer[]
            int[] intArray = {1, 2, 3};
            assertThat(converter.convert(intArray)).containsExactly(1, 2, 3);

            // 从 String[] 转换为 Integer[]
            String[] strArray = {"1", "2", "3"};
            assertThat(converter.convert(strArray)).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("混合类型集合转换")
        void testMixedTypeCollectionConversion() {
            Converter<String[]> converter = ArrayConverter.stringArrayConverter();

            List<Object> mixedList = List.of(1, "two", 3.0);
            assertThat(converter.convert(mixedList)).containsExactly("1", "two", "3.0");
        }

        @Test
        @DisplayName("包含 null 元素的数组")
        void testArrayWithNullElements() {
            Converter<String[]> converter = ArrayConverter.stringArrayConverter();

            List<String> listWithNull = new ArrayList<>();
            listWithNull.add("a");
            listWithNull.add(null);
            listWithNull.add("c");

            String[] result = converter.convert(listWithNull);
            assertThat(result[0]).isEqualTo("a");
            assertThat(result[1]).isNull();
            assertThat(result[2]).isEqualTo("c");
        }
    }

    @Nested
    @DisplayName("兼容类型数组测试")
    class CompatibleTypeArrayTests {

        @Test
        @DisplayName("已是兼容类型数组直接返回")
        void testCompatibleArrayReturned() {
            Converter<String[]> converter = ArrayConverter.stringArrayConverter();

            String[] original = {"a", "b", "c"};
            assertThat(converter.convert(original)).isSameAs(original);
        }

        @Test
        @DisplayName("Integer[] 转 Integer[]")
        void testIntegerArrayToIntegerArray() {
            Converter<Integer[]> converter = ArrayConverter.integerArrayConverter();

            Integer[] original = {1, 2, 3};
            assertThat(converter.convert(original)).isSameAs(original);
        }
    }
}
