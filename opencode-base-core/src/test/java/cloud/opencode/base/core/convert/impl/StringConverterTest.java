package cloud.opencode.base.core.convert.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * StringConverter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("StringConverter 测试")
class StringConverterTest {

    @Nested
    @DisplayName("基本类型转换测试")
    class BasicTypeConversionTests {

        @Test
        @DisplayName("数字转字符串")
        void testNumberToString() {
            StringConverter converter = StringConverter.getInstance();

            assertThat(converter.convert(123)).isEqualTo("123");
            assertThat(converter.convert(123L)).isEqualTo("123");
            assertThat(converter.convert(3.14)).isEqualTo("3.14");
            assertThat(converter.convert(3.14f)).isEqualTo("3.14");
        }

        @Test
        @DisplayName("布尔转字符串")
        void testBooleanToString() {
            StringConverter converter = StringConverter.getInstance();

            assertThat(converter.convert(true)).isEqualTo("true");
            assertThat(converter.convert(false)).isEqualTo("false");
        }

        @Test
        @DisplayName("字符转字符串")
        void testCharacterToString() {
            StringConverter converter = StringConverter.getInstance();

            assertThat(converter.convert('A')).isEqualTo("A");
            assertThat(converter.convert('中')).isEqualTo("中");
        }

        @Test
        @DisplayName("字符串直接返回")
        void testStringReturned() {
            StringConverter converter = StringConverter.getInstance();

            String original = "hello";
            assertThat(converter.convert(original)).isSameAs(original);
        }

        @Test
        @DisplayName("CharSequence 转字符串")
        void testCharSequenceToString() {
            StringConverter converter = StringConverter.getInstance();

            StringBuilder sb = new StringBuilder("hello");
            assertThat(converter.convert(sb)).isEqualTo("hello");

            StringBuffer buf = new StringBuffer("world");
            assertThat(converter.convert(buf)).isEqualTo("world");
        }
    }

    @Nested
    @DisplayName("枚举转换测试")
    class EnumConversionTests {

        @Test
        @DisplayName("枚举转字符串")
        void testEnumToString() {
            StringConverter converter = StringConverter.getInstance();

            assertThat(converter.convert(Thread.State.RUNNABLE)).isEqualTo("RUNNABLE");
            assertThat(converter.convert(Thread.State.WAITING)).isEqualTo("WAITING");
        }
    }

    @Nested
    @DisplayName("字节数组转换测试")
    class ByteArrayConversionTests {

        @Test
        @DisplayName("字节数组转字符串 UTF-8")
        void testByteArrayToStringUtf8() {
            StringConverter converter = StringConverter.getInstance();

            byte[] bytes = "Hello World".getBytes(StandardCharsets.UTF_8);
            assertThat(converter.convert(bytes)).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("字节数组转字符串 指定字符集")
        void testByteArrayToStringWithCharset() {
            StringConverter converter = StringConverter.of(StandardCharsets.ISO_8859_1);

            byte[] bytes = "Hello".getBytes(StandardCharsets.ISO_8859_1);
            assertThat(converter.convert(bytes)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("char 数组转字符串")
        void testCharArrayToString() {
            StringConverter converter = StringConverter.getInstance();

            char[] chars = {'H', 'e', 'l', 'l', 'o'};
            assertThat(converter.convert(chars)).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("日期时间转换测试")
    class DateTimeConversionTests {

        @Test
        @DisplayName("LocalDateTime 转字符串")
        void testLocalDateTimeToString() {
            StringConverter converter = StringConverter.getInstance();

            LocalDateTime ldt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            String result = converter.convert(ldt);
            assertThat(result).contains("2024-01-15");
            assertThat(result).contains("10:30:00");
        }

        @Test
        @DisplayName("LocalDate 转字符串")
        void testLocalDateToString() {
            StringConverter converter = StringConverter.getInstance();

            LocalDate ld = LocalDate.of(2024, 1, 15);
            assertThat(converter.convert(ld)).isEqualTo("2024-01-15");
        }

        @Test
        @DisplayName("LocalTime 转字符串")
        void testLocalTimeToString() {
            StringConverter converter = StringConverter.getInstance();

            LocalTime lt = LocalTime.of(10, 30, 45);
            assertThat(converter.convert(lt)).isEqualTo("10:30:45");
        }

        @Test
        @DisplayName("Date 转字符串")
        void testDateToString() {
            StringConverter converter = StringConverter.getInstance();

            Date date = new Date();
            String result = converter.convert(date);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Calendar 转字符串")
        void testCalendarToString() {
            StringConverter converter = StringConverter.getInstance();

            Calendar cal = Calendar.getInstance();
            String result = converter.convert(cal);
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("数组转换测试")
    class ArrayConversionTests {

        @Test
        @DisplayName("int 数组转字符串")
        void testIntArrayToString() {
            StringConverter converter = StringConverter.getInstance();

            int[] arr = {1, 2, 3, 4, 5};
            assertThat(converter.convert(arr)).isEqualTo("1,2,3,4,5");
        }

        @Test
        @DisplayName("对象数组转字符串")
        void testObjectArrayToString() {
            StringConverter converter = StringConverter.getInstance();

            String[] arr = {"a", "b", "c"};
            assertThat(converter.convert(arr)).isEqualTo("a,b,c");

            Integer[] nums = {1, 2, 3};
            assertThat(converter.convert(nums)).isEqualTo("1,2,3");
        }

        @Test
        @DisplayName("空数组转字符串")
        void testEmptyArrayToString() {
            StringConverter converter = StringConverter.getInstance();

            assertThat(converter.convert(new int[0])).isEmpty();
            assertThat(converter.convert(new String[0])).isEmpty();
        }

        @Test
        @DisplayName("包含 null 的数组")
        void testArrayWithNulls() {
            StringConverter converter = StringConverter.getInstance();

            String[] arr = {"a", null, "c"};
            assertThat(converter.convert(arr)).isEqualTo("a,,c");
        }

        @Test
        @DisplayName("自定义分隔符")
        void testCustomSeparator() {
            StringConverter converter = StringConverter.of(";");

            int[] arr = {1, 2, 3};
            assertThat(converter.convert(arr)).isEqualTo("1;2;3");

            String[] strs = {"a", "b", "c"};
            assertThat(converter.convert(strs)).isEqualTo("a;b;c");
        }
    }

    @Nested
    @DisplayName("集合转换测试")
    class CollectionConversionTests {

        @Test
        @DisplayName("List 转字符串")
        void testListToString() {
            StringConverter converter = StringConverter.getInstance();

            List<Integer> list = List.of(1, 2, 3);
            assertThat(converter.convert(list)).isEqualTo("1,2,3");
        }

        @Test
        @DisplayName("Set 转字符串")
        void testSetToString() {
            StringConverter converter = StringConverter.getInstance();

            Set<String> set = new LinkedHashSet<>(List.of("a", "b", "c"));
            assertThat(converter.convert(set)).isEqualTo("a,b,c");
        }

        @Test
        @DisplayName("空集合转字符串")
        void testEmptyCollectionToString() {
            StringConverter converter = StringConverter.getInstance();

            assertThat(converter.convert(List.of())).isEmpty();
            assertThat(converter.convert(Set.of())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Map 转换测试")
    class MapConversionTests {

        @Test
        @DisplayName("Map 转字符串")
        void testMapToString() {
            StringConverter converter = StringConverter.getInstance();

            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("a", 1);
            map.put("b", 2);

            String result = converter.convert(map);
            assertThat(result).contains("a=1");
            assertThat(result).contains("b=2");
            assertThat(result).startsWith("{");
            assertThat(result).endsWith("}");
        }

        @Test
        @DisplayName("空 Map 转字符串")
        void testEmptyMapToString() {
            StringConverter converter = StringConverter.getInstance();

            assertThat(converter.convert(Map.of())).isEqualTo("{}");
        }

        @Test
        @DisplayName("Map 包含 null")
        void testMapWithNulls() {
            StringConverter converter = StringConverter.getInstance();

            Map<String, String> map = new HashMap<>();
            map.put("key", null);
            map.put(null, "value");

            String result = converter.convert(map);
            assertThat(result).contains("null");
        }
    }

    @Nested
    @DisplayName("Optional 转换测试")
    class OptionalConversionTests {

        @Test
        @DisplayName("Optional 有值转字符串")
        void testOptionalWithValue() {
            StringConverter converter = StringConverter.getInstance();

            Optional<Integer> opt = Optional.of(123);
            assertThat(converter.convert(opt)).isEqualTo("123");
        }

        @Test
        @DisplayName("Optional 空转字符串")
        void testOptionalEmpty() {
            StringConverter converter = StringConverter.getInstance();

            Optional<Integer> opt = Optional.empty();
            assertThat(converter.convert(opt)).isNull();
        }
    }

    @Nested
    @DisplayName("Reader 转换测试")
    class ReaderConversionTests {

        @Test
        @DisplayName("Reader 转字符串")
        void testReaderToString() {
            StringConverter converter = StringConverter.getInstance();

            StringReader reader = new StringReader("Hello World");
            assertThat(converter.convert(reader)).isEqualTo("Hello World");
        }
    }

    @Nested
    @DisplayName("null 和默认值处理测试")
    class NullAndDefaultHandlingTests {

        @Test
        @DisplayName("null 返回默认值")
        void testNullReturnsDefault() {
            StringConverter converter = StringConverter.getInstance();

            assertThat(converter.convert(null, "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("null 无默认值返回 null")
        void testNullWithoutDefaultReturnsNull() {
            StringConverter converter = StringConverter.getInstance();

            assertThat(converter.convert(null)).isNull();
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("getInstance 单例")
        void testGetInstance() {
            StringConverter instance1 = StringConverter.getInstance();
            StringConverter instance2 = StringConverter.getInstance();
            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("of Charset")
        void testOfCharset() {
            StringConverter converter = StringConverter.of(StandardCharsets.ISO_8859_1);
            assertThat(converter).isNotNull();
        }

        @Test
        @DisplayName("of separator")
        void testOfSeparator() {
            StringConverter converter = StringConverter.of(";");
            assertThat(converter.convert(new int[]{1, 2, 3})).isEqualTo("1;2;3");
        }

        @Test
        @DisplayName("of Charset and separator")
        void testOfCharsetAndSeparator() {
            StringConverter converter = StringConverter.of(StandardCharsets.UTF_8, "|");
            assertThat(converter.convert(new int[]{1, 2, 3})).isEqualTo("1|2|3");
        }
    }

    @Nested
    @DisplayName("对象 toString 测试")
    class ObjectToStringTests {

        @Test
        @DisplayName("自定义对象使用 toString")
        void testCustomObjectUsesToString() {
            StringConverter converter = StringConverter.getInstance();

            class CustomObject {
                @Override
                public String toString() {
                    return "CustomObject[value=123]";
                }
            }

            assertThat(converter.convert(new CustomObject())).isEqualTo("CustomObject[value=123]");
        }
    }
}
