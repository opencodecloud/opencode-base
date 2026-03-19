package cloud.opencode.base.i18n.formatter;

import cloud.opencode.base.i18n.spi.MessageFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * NamedParameterFormatter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("NamedParameterFormatter 测试")
class NamedParameterFormatterTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("默认构造方法")
        void testDefaultConstructor() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            assertThat(formatter).isNotNull();
        }

        @Test
        @DisplayName("指定keepMissingParams")
        void testKeepMissingParamsConstructor() {
            NamedParameterFormatter formatter = new NamedParameterFormatter(true);

            String result = formatter.format("Hello ${name}!", Locale.ENGLISH, Map.of());
            assertThat(result).isEqualTo("Hello ${name}!");
        }

        @Test
        @DisplayName("指定完整参数")
        void testFullConstructor() {
            NamedParameterFormatter formatter = new NamedParameterFormatter(true, "[", "]");

            // keepMissingParams=true时，缺失参数会保留为自定义前后缀格式
            // 必须传入非空Map才会触发参数替换逻辑
            String result = formatter.format("Hello ${name}!", Locale.ENGLISH, Map.of("other", "value"));
            assertThat(result).isEqualTo("Hello [name]!");
        }
    }

    @Nested
    @DisplayName("format方法测试（位置参数）")
    class FormatWithArgsTests {

        @Test
        @DisplayName("格式化位置参数")
        void testFormatPositional() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.format("Hello ${0}!", Locale.ENGLISH, "World");

            assertThat(result).isEqualTo("Hello World!");
        }

        @Test
        @DisplayName("格式化多个位置参数")
        void testFormatMultiplePositional() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.format("${0} is ${1} years old", Locale.ENGLISH, "John", 25);

            assertThat(result).isEqualTo("John is 25 years old");
        }

        @Test
        @DisplayName("模板为null时返回null")
        void testFormatNullTemplate() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.format(null, Locale.ENGLISH, "arg");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("参数为null时返回原模板")
        void testFormatNullArgs() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.format("Hello", Locale.ENGLISH, (Object[]) null);

            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("空参数时返回原模板")
        void testFormatEmptyArgs() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.format("Hello", Locale.ENGLISH);

            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("null参数值替换为空字符串")
        void testFormatNullArgValue() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.format("Hello ${0}!", Locale.ENGLISH, (Object) null);

            assertThat(result).isEqualTo("Hello !");
        }
    }

    @Nested
    @DisplayName("format方法测试（命名参数）")
    class FormatWithMapTests {

        @Test
        @DisplayName("格式化命名参数")
        void testFormatNamed() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.format("Hello, ${name}!", Locale.ENGLISH, Map.of("name", "Alice"));

            assertThat(result).isEqualTo("Hello, Alice!");
        }

        @Test
        @DisplayName("格式化多个命名参数")
        void testFormatMultipleNamed() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.format(
                    "${name} has ${count} items",
                    Locale.ENGLISH,
                    Map.of("name", "Bob", "count", 5)
            );

            assertThat(result).isEqualTo("Bob has 5 items");
        }

        @Test
        @DisplayName("缺失参数移除")
        void testFormatMissingParam() {
            NamedParameterFormatter formatter = new NamedParameterFormatter(false);

            // 必须传入非空Map才会触发参数替换逻辑
            String result = formatter.format("Hello ${name}!", Locale.ENGLISH, Map.of("other", "value"));

            assertThat(result).isEqualTo("Hello !");
        }

        @Test
        @DisplayName("保留缺失参数")
        void testFormatKeepMissingParam() {
            NamedParameterFormatter formatter = new NamedParameterFormatter(true);

            // 必须传入非空Map才会触发参数替换逻辑
            String result = formatter.format("Hello ${name}!", Locale.ENGLISH, Map.of("other", "value"));

            assertThat(result).isEqualTo("Hello ${name}!");
        }

        @Test
        @DisplayName("模板为null时返回null")
        void testFormatNullTemplateMap() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.format(null, Locale.ENGLISH, Map.of("name", "Alice"));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Map为null时返回原模板")
        void testFormatNullMap() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.format("Hello", Locale.ENGLISH, (Map<String, Object>) null);

            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("空Map时返回原模板")
        void testFormatEmptyMap() {
            NamedParameterFormatter formatter = new NamedParameterFormatter(false);

            String result = formatter.format("Hello World", Locale.ENGLISH, Map.of());

            assertThat(result).isEqualTo("Hello World");
        }
    }

    @Nested
    @DisplayName("formatNested方法测试")
    class FormatNestedTests {

        @Test
        @DisplayName("嵌套参数格式化")
        void testFormatNested() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.formatNested(
                    "${greeting}",
                    Map.of("greeting", "Hello ${name}", "name", "World"),
                    2
            );

            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("深度限制")
        void testFormatNestedDepthLimit() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.formatNested(
                    "${a}",
                    Map.of("a", "${b}", "b", "${c}", "c", "value"),
                    2
            );

            assertThat(result).isEqualTo("${c}");
        }

        @Test
        @DisplayName("null模板返回null")
        void testFormatNestedNullTemplate() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.formatNested(null, Map.of("name", "Alice"), 2);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null参数返回原模板")
        void testFormatNestedNullParams() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.formatNested("Hello", null, 2);

            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("深度为0返回原模板")
        void testFormatNestedZeroDepth() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            String result = formatter.formatNested("${name}", Map.of("name", "Alice"), 0);

            assertThat(result).isEqualTo("${name}");
        }
    }

    @Nested
    @DisplayName("containsParameters方法测试")
    class ContainsParametersTests {

        @Test
        @DisplayName("包含参数返回true")
        void testContainsParameters() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            assertThat(formatter.containsParameters("Hello ${name}!")).isTrue();
        }

        @Test
        @DisplayName("不包含参数返回false")
        void testNoParameters() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            assertThat(formatter.containsParameters("Hello World!")).isFalse();
        }

        @Test
        @DisplayName("null模板返回false")
        void testNullTemplate() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            assertThat(formatter.containsParameters(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("extractParameterNames方法测试")
    class ExtractParameterNamesTests {

        @Test
        @DisplayName("提取参数名")
        void testExtractParameterNames() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            Set<String> names = formatter.extractParameterNames("${name} has ${count} items");

            assertThat(names).containsExactlyInAnyOrder("name", "count");
        }

        @Test
        @DisplayName("无参数返回空集合")
        void testExtractNoParameters() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            Set<String> names = formatter.extractParameterNames("Hello World");

            assertThat(names).isEmpty();
        }

        @Test
        @DisplayName("null模板返回空集合")
        void testExtractFromNull() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            Set<String> names = formatter.extractParameterNames(null);

            assertThat(names).isEmpty();
        }

        @Test
        @DisplayName("保持顺序")
        void testExtractPreservesOrder() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            Set<String> names = formatter.extractParameterNames("${a} ${b} ${c}");

            assertThat(names).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("clearCache方法测试")
    class ClearCacheTests {

        @Test
        @DisplayName("清除缓存不抛出异常")
        void testClearCache() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            assertThatCode(() -> formatter.clearCache()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现MessageFormatter接口")
        void testImplementsInterface() {
            NamedParameterFormatter formatter = new NamedParameterFormatter();

            assertThat(formatter).isInstanceOf(MessageFormatter.class);
        }
    }
}
