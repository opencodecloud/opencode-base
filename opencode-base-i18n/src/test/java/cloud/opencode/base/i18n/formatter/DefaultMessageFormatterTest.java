package cloud.opencode.base.i18n.formatter;

import cloud.opencode.base.i18n.spi.MessageFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultMessageFormatter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("DefaultMessageFormatter 测试")
class DefaultMessageFormatterTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造方法")
        void testDefaultConstructor() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter();

            assertThat(formatter.isCacheEnabled()).isTrue();
            assertThat(formatter.getCacheSize()).isZero();
        }

        @Test
        @DisplayName("指定缓存启用状态")
        void testCacheEnabledConstructor() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter(false);

            assertThat(formatter.isCacheEnabled()).isFalse();
        }

        @Test
        @DisplayName("指定缓存大小")
        void testFullConstructor() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter(true, 500);

            assertThat(formatter.isCacheEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("format方法测试（位置参数）")
    class FormatWithArgsTests {

        @Test
        @DisplayName("格式化简单消息")
        void testFormatSimple() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter();

            String result = formatter.format("Hello, {0}!", Locale.ENGLISH, "World");

            assertThat(result).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("格式化多个参数")
        void testFormatMultipleArgs() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter();

            String result = formatter.format("{0} is {1} years old", Locale.ENGLISH, "John", 25);

            assertThat(result).isEqualTo("John is 25 years old");
        }

        @Test
        @DisplayName("无参数时返回原模板")
        void testFormatNoArgs() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter();

            String result = formatter.format("Hello World", Locale.ENGLISH);

            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("模板为null时返回null")
        void testFormatNullTemplate() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter();

            String result = formatter.format(null, Locale.ENGLISH, "arg");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("参数为null时返回原模板")
        void testFormatNullArgs() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter();

            String result = formatter.format("Hello", Locale.ENGLISH, (Object[]) null);

            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("空参数数组时返回原模板")
        void testFormatEmptyArgs() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter();

            String result = formatter.format("Hello", Locale.ENGLISH);

            assertThat(result).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("format方法测试（命名参数）")
    class FormatWithMapTests {

        @Test
        @DisplayName("使用Map格式化")
        void testFormatWithMap() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter();

            String result = formatter.format("Hello, {0}!", Locale.ENGLISH, Map.of("name", "Alice"));

            assertThat(result).isEqualTo("Hello, Alice!");
        }

        @Test
        @DisplayName("空Map时返回原模板")
        void testFormatWithEmptyMap() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter();

            String result = formatter.format("Hello", Locale.ENGLISH, Map.of());

            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Map为null时返回原模板")
        void testFormatWithNullMap() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter();

            String result = formatter.format("Hello", Locale.ENGLISH, (Map<String, Object>) null);

            assertThat(result).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("缓存测试")
    class CacheTests {

        @Test
        @DisplayName("启用缓存后重复格式化使用缓存")
        void testCachingWorks() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter(true);

            formatter.format("Hello, {0}!", Locale.ENGLISH, "World");
            formatter.format("Hello, {0}!", Locale.ENGLISH, "Java");

            assertThat(formatter.getCacheSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("禁用缓存后不缓存")
        void testCachingDisabled() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter(false);

            formatter.format("Hello, {0}!", Locale.ENGLISH, "World");

            assertThat(formatter.getCacheSize()).isZero();
        }

        @Test
        @DisplayName("清除缓存")
        void testClearCache() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter(true);

            formatter.format("Test {0}", Locale.ENGLISH, "msg");
            assertThat(formatter.getCacheSize()).isPositive();

            formatter.clearCache();
            assertThat(formatter.getCacheSize()).isZero();
        }
    }

    @Nested
    @DisplayName("Locale支持测试")
    class LocaleSupportTests {

        @Test
        @DisplayName("不同Locale产生不同缓存条目")
        void testDifferentLocalesCached() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter(true);

            formatter.format("Test {0}", Locale.ENGLISH, "en");
            formatter.format("Test {0}", Locale.CHINESE, "zh");

            assertThat(formatter.getCacheSize()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现MessageFormatter接口")
        void testImplementsInterface() {
            DefaultMessageFormatter formatter = new DefaultMessageFormatter();

            assertThat(formatter).isInstanceOf(MessageFormatter.class);
        }
    }
}
