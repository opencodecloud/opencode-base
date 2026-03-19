package cloud.opencode.base.i18n.formatter;

import cloud.opencode.base.i18n.spi.MessageFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * TemplateFormatter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("TemplateFormatter 测试")
class TemplateFormatterTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造方法")
        void testDefaultConstructor() {
            TemplateFormatter formatter = new TemplateFormatter();

            assertThat(formatter).isNotNull();
            assertThat(formatter.getDateFormatCacheSize()).isZero();
            assertThat(formatter.getNumberFormatCacheSize()).isZero();
        }
    }

    @Nested
    @DisplayName("format方法测试（位置参数）")
    class FormatWithArgsTests {

        @Test
        @DisplayName("格式化位置参数")
        void testFormatPositional() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format("Hello ${0}!", Locale.ENGLISH, "World");

            assertThat(result).isEqualTo("Hello World!");
        }

        @Test
        @DisplayName("格式化多个位置参数")
        void testFormatMultiplePositional() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format("${0} is ${1}", Locale.ENGLISH, "John", 25);

            assertThat(result).isEqualTo("John is 25");
        }

        @Test
        @DisplayName("模板为null时返回null")
        void testFormatNullTemplate() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format(null, Locale.ENGLISH, "arg");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("参数为null时返回原模板")
        void testFormatNullArgs() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format("Hello", Locale.ENGLISH, (Object[]) null);

            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("空参数时返回原模板")
        void testFormatEmptyArgs() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format("Hello", Locale.ENGLISH);

            assertThat(result).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("format方法测试（命名参数）")
    class FormatWithMapTests {

        @Test
        @DisplayName("格式化简单命名参数")
        void testFormatNamed() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format(
                    "Hello, ${name}!",
                    Locale.ENGLISH,
                    Map.of("name", "Alice")
            );

            assertThat(result).isEqualTo("Hello, Alice!");
        }

        @Test
        @DisplayName("缺失参数返回空字符串")
        void testFormatMissingParam() {
            TemplateFormatter formatter = new TemplateFormatter();

            // 必须传入非空Map才会触发参数替换逻辑
            String result = formatter.format(
                    "Hello ${name}!",
                    Locale.ENGLISH,
                    Map.of("other", "value")
            );

            assertThat(result).isEqualTo("Hello !");
        }

        @Test
        @DisplayName("模板为null时返回null")
        void testFormatNullTemplateMap() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format(null, Locale.ENGLISH, Map.of("name", "Alice"));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Map为null时返回原模板")
        void testFormatNullMap() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format("Hello", Locale.ENGLISH, (Map<String, Object>) null);

            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("空Map时返回原模板")
        void testFormatEmptyMap() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format("Hello World", Locale.ENGLISH, Map.of());

            assertThat(result).isEqualTo("Hello World");
        }
    }

    @Nested
    @DisplayName("类型指定符测试")
    class TypeSpecifierTests {

        @Test
        @DisplayName("upper转换为大写")
        void testUpperCase() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format(
                    "${name:upper}",
                    Locale.ENGLISH,
                    Map.of("name", "hello")
            );

            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("lower转换为小写")
        void testLowerCase() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format(
                    "${name:lower}",
                    Locale.ENGLISH,
                    Map.of("name", "HELLO")
            );

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("string类型")
        void testStringType() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format(
                    "${value:string}",
                    Locale.ENGLISH,
                    Map.of("value", 123)
            );

            assertThat(result).isEqualTo("123");
        }

        @Test
        @DisplayName("trim类型")
        void testTrimType() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format(
                    "${text:trim}",
                    Locale.ENGLISH,
                    Map.of("text", "  hello  ")
            );

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("length类型")
        void testLengthType() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format(
                    "${text:length}",
                    Locale.ENGLISH,
                    Map.of("text", "hello")
            );

            assertThat(result).isEqualTo("5");
        }

        @Test
        @DisplayName("未知类型返回原值")
        void testUnknownType() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format(
                    "${value:unknown}",
                    Locale.ENGLISH,
                    Map.of("value", "test")
            );

            assertThat(result).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("数字格式化测试")
    class NumberFormattingTests {

        @Test
        @DisplayName("格式化数字")
        void testFormatNumber() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format(
                    "${price:number}",
                    Locale.US,
                    Map.of("price", 1234.5)
            );

            assertThat(result).contains("1,234.5");
        }

        @Test
        @DisplayName("格式化数字带模式")
        void testFormatNumberWithPattern() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format(
                    "${price:number:0.00}",
                    Locale.US,
                    Map.of("price", 99.5)
            );

            assertThat(result).isEqualTo("99.50");
        }

        @Test
        @DisplayName("字符串数字格式化")
        void testFormatStringNumber() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format(
                    "${value:number}",
                    Locale.US,
                    Map.of("value", "123.45")
            );

            assertThat(result).contains("123.45");
        }

        @Test
        @DisplayName("无效数字返回原值")
        void testFormatInvalidNumber() {
            TemplateFormatter formatter = new TemplateFormatter();

            String result = formatter.format(
                    "${value:number}",
                    Locale.US,
                    Map.of("value", "not-a-number")
            );

            assertThat(result).isEqualTo("not-a-number");
        }
    }

    @Nested
    @DisplayName("日期格式化测试")
    class DateFormattingTests {

        @Test
        @DisplayName("格式化LocalDate")
        void testFormatLocalDate() {
            TemplateFormatter formatter = new TemplateFormatter();
            LocalDate date = LocalDate.of(2024, 1, 15);

            String result = formatter.format(
                    "${date:date:yyyy-MM-dd}",
                    Locale.US,
                    Map.of("date", date)
            );

            assertThat(result).isEqualTo("2024-01-15");
        }

        @Test
        @DisplayName("格式化LocalDate默认模式")
        void testFormatLocalDateDefault() {
            TemplateFormatter formatter = new TemplateFormatter();
            LocalDate date = LocalDate.of(2024, 1, 15);

            String result = formatter.format(
                    "${date:date}",
                    Locale.US,
                    Map.of("date", date)
            );

            assertThat(result).isEqualTo("2024-01-15");
        }

        @Test
        @DisplayName("格式化Date对象")
        void testFormatDateObject() {
            TemplateFormatter formatter = new TemplateFormatter();
            @SuppressWarnings("deprecation")
            Date date = new Date(124, 0, 15); // 2024-01-15

            String result = formatter.format(
                    "${date:date:yyyy-MM-dd}",
                    Locale.US,
                    Map.of("date", date)
            );

            assertThat(result).isEqualTo("2024-01-15");
        }
    }

    @Nested
    @DisplayName("时间格式化测试")
    class TimeFormattingTests {

        @Test
        @DisplayName("格式化LocalTime")
        void testFormatLocalTime() {
            TemplateFormatter formatter = new TemplateFormatter();
            LocalTime time = LocalTime.of(14, 30, 0);

            String result = formatter.format(
                    "${time:time:HH:mm}",
                    Locale.US,
                    Map.of("time", time)
            );

            assertThat(result).isEqualTo("14:30");
        }

        @Test
        @DisplayName("格式化LocalTime默认模式")
        void testFormatLocalTimeDefault() {
            TemplateFormatter formatter = new TemplateFormatter();
            LocalTime time = LocalTime.of(14, 30, 45);

            String result = formatter.format(
                    "${time:time}",
                    Locale.US,
                    Map.of("time", time)
            );

            assertThat(result).isEqualTo("14:30:45");
        }
    }

    @Nested
    @DisplayName("缓存测试")
    class CacheTests {

        @Test
        @DisplayName("缓存日期格式")
        void testDateFormatCache() {
            TemplateFormatter formatter = new TemplateFormatter();
            LocalDate date = LocalDate.of(2024, 1, 15);

            formatter.format("${date:date:yyyy-MM-dd}", Locale.US, Map.of("date", date));
            formatter.format("${date:date:yyyy-MM-dd}", Locale.US, Map.of("date", date));

            assertThat(formatter.getDateFormatCacheSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("缓存数字格式")
        void testNumberFormatCache() {
            TemplateFormatter formatter = new TemplateFormatter();

            formatter.format("${num:number:0.00}", Locale.US, Map.of("num", 1.5));
            formatter.format("${num:number:0.00}", Locale.US, Map.of("num", 2.5));

            assertThat(formatter.getNumberFormatCacheSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("清除缓存")
        void testClearCache() {
            TemplateFormatter formatter = new TemplateFormatter();
            LocalDate date = LocalDate.of(2024, 1, 15);

            formatter.format("${date:date:yyyy-MM-dd}", Locale.US, Map.of("date", date));
            formatter.format("${num:number:0.00}", Locale.US, Map.of("num", 1.5));

            formatter.clearCache();

            assertThat(formatter.getDateFormatCacheSize()).isZero();
            assertThat(formatter.getNumberFormatCacheSize()).isZero();
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现MessageFormatter接口")
        void testImplementsInterface() {
            TemplateFormatter formatter = new TemplateFormatter();

            assertThat(formatter).isInstanceOf(MessageFormatter.class);
        }
    }
}
