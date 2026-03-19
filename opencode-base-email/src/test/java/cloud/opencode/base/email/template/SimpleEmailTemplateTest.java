package cloud.opencode.base.email.template;

import cloud.opencode.base.email.exception.EmailTemplateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * SimpleEmailTemplate 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("SimpleEmailTemplate 测试")
class SimpleEmailTemplateTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void cleanUp() {
        SimpleEmailTemplate.clearCache();
    }

    @Nested
    @DisplayName("getInstance() 测试")
    class GetInstanceTests {

        @Test
        @DisplayName("获取单例实例")
        void testGetInstance() {
            SimpleEmailTemplate instance1 = SimpleEmailTemplate.getInstance();
            SimpleEmailTemplate instance2 = SimpleEmailTemplate.getInstance();

            assertThat(instance1).isNotNull();
            assertThat(instance1).isSameAs(instance2);
        }
    }

    @Nested
    @DisplayName("renderTemplate() 测试")
    class RenderTemplateTests {

        @Test
        @DisplayName("渲染null模板返回null")
        void testRenderNullTemplate() {
            String result = SimpleEmailTemplate.renderTemplate(null, Map.of("name", "John"));
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("渲染模板null变量返回原模板")
        void testRenderWithNullVariables() {
            String template = "Hello ${name}!";
            String result = SimpleEmailTemplate.renderTemplate(template, null);
            assertThat(result).isEqualTo(template);
        }

        @Test
        @DisplayName("渲染模板空变量返回原模板")
        void testRenderWithEmptyVariables() {
            String template = "Hello ${name}!";
            String result = SimpleEmailTemplate.renderTemplate(template, Map.of());
            assertThat(result).isEqualTo(template);
        }

        @Test
        @DisplayName("使用${...}模式替换变量")
        void testDollarBracePattern() {
            String template = "Hello ${name}, your order #${orderId} is confirmed.";
            Map<String, Object> variables = Map.of(
                    "name", "John",
                    "orderId", "12345"
            );

            String result = SimpleEmailTemplate.renderTemplate(template, variables);

            assertThat(result).isEqualTo("Hello John, your order #12345 is confirmed.");
        }

        @Test
        @DisplayName("使用{{...}}模式替换变量")
        void testMustachePattern() {
            String template = "Hello {{name}}, welcome to {{site}}!";
            Map<String, Object> variables = Map.of(
                    "name", "Alice",
                    "site", "OpenCode"
            );

            String result = SimpleEmailTemplate.renderTemplate(template, variables);

            assertThat(result).isEqualTo("Hello Alice, welcome to OpenCode!");
        }

        @Test
        @DisplayName("混合使用两种模式")
        void testMixedPatterns() {
            String template = "${greeting} {{name}}, your code is ${code}.";
            Map<String, Object> variables = Map.of(
                    "greeting", "Hi",
                    "name", "Bob",
                    "code", "ABC123"
            );

            String result = SimpleEmailTemplate.renderTemplate(template, variables);

            assertThat(result).isEqualTo("Hi Bob, your code is ABC123.");
        }

        @Test
        @DisplayName("使用默认值")
        void testDefaultValue() {
            String template = "Hello ${name:Guest}, status: ${status:Unknown}";
            Map<String, Object> variables = Map.of("name", "John");

            String result = SimpleEmailTemplate.renderTemplate(template, variables);

            assertThat(result).isEqualTo("Hello John, status: Unknown");
        }

        @Test
        @DisplayName("使用默认值(mustache模式)")
        void testDefaultValueMustache() {
            String template = "{{name:Anonymous}} - {{role:user}}";
            Map<String, Object> variables = Map.of("role", "admin");

            String result = SimpleEmailTemplate.renderTemplate(template, variables);

            assertThat(result).isEqualTo("Anonymous - admin");
        }

        @Test
        @DisplayName("变量未找到且无默认值保留原样")
        void testMissingVariableNoDefault() {
            String template = "Hello ${unknown}!";
            Map<String, Object> variables = Map.of("name", "John");

            String result = SimpleEmailTemplate.renderTemplate(template, variables);

            assertThat(result).isEqualTo("Hello ${unknown}!");
        }

        @Test
        @DisplayName("非字符串变量转换为字符串")
        void testNonStringVariables() {
            String template = "Count: ${count}, Price: ${price}, Active: ${active}";
            Map<String, Object> variables = Map.of(
                    "count", 42,
                    "price", 19.99,
                    "active", true
            );

            String result = SimpleEmailTemplate.renderTemplate(template, variables);

            assertThat(result).isEqualTo("Count: 42, Price: 19.99, Active: true");
        }

        @Test
        @DisplayName("空变量名处理")
        void testEmptyVariableName() {
            // ${} pattern requires at least 1 character, so it won't match
            String template = "Hello ${}!";
            Map<String, Object> variables = new HashMap<>();
            variables.put("", "World");

            String result = SimpleEmailTemplate.renderTemplate(template, variables);

            // Pattern doesn't match empty variable name, template unchanged
            assertThat(result).isEqualTo("Hello ${}!");
        }
    }

    @Nested
    @DisplayName("renderTemplate() HTML转义测试")
    class HtmlEscapingTests {

        @Test
        @DisplayName("HTML转义特殊字符")
        void testHtmlEscaping() {
            String template = "${content}";
            Map<String, Object> variables = Map.of(
                    "content", "<script>alert('xss')</script>"
            );

            String result = SimpleEmailTemplate.renderTemplate(template, variables, true);

            assertThat(result).isEqualTo("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;");
        }

        @Test
        @DisplayName("转义所有HTML特殊字符")
        void testAllHtmlSpecialChars() {
            String template = "${text}";
            Map<String, Object> variables = Map.of(
                    "text", "< > \" ' &"
            );

            String result = SimpleEmailTemplate.renderTemplate(template, variables, true);

            assertThat(result).isEqualTo("&lt; &gt; &quot; &#39; &amp;");
        }

        @Test
        @DisplayName("不启用转义时保留原样")
        void testNoEscaping() {
            String template = "${html}";
            Map<String, Object> variables = Map.of(
                    "html", "<b>Bold</b>"
            );

            String result = SimpleEmailTemplate.renderTemplate(template, variables, false);

            assertThat(result).isEqualTo("<b>Bold</b>");
        }

        @Test
        @DisplayName("默认值不进行HTML转义")
        void testDefaultValueNotEscaped() {
            String template = "${missing:<default>}";

            // Need non-empty map to trigger processing (empty map returns template unchanged)
            String result = SimpleEmailTemplate.renderTemplate(template, Map.of("other", "value"), true);

            assertThat(result).isEqualTo("<default>");
        }
    }

    @Nested
    @DisplayName("render() 实例方法测试")
    class InstanceRenderTests {

        @Test
        @DisplayName("使用实例方法渲染")
        void testInstanceRender() {
            SimpleEmailTemplate template = SimpleEmailTemplate.getInstance();
            String content = "Hello ${name}!";
            Map<String, Object> variables = Map.of("name", "World");

            String result = template.render(content, variables);

            assertThat(result).isEqualTo("Hello World!");
        }
    }

    @Nested
    @DisplayName("escapeHtml() 测试")
    class EscapeHtmlTests {

        @Test
        @DisplayName("转义null返回null")
        void testEscapeNull() {
            assertThat(SimpleEmailTemplate.escapeHtml(null)).isNull();
        }

        @Test
        @DisplayName("转义HTML特殊字符")
        void testEscapeSpecialChars() {
            assertThat(SimpleEmailTemplate.escapeHtml("&")).isEqualTo("&amp;");
            assertThat(SimpleEmailTemplate.escapeHtml("<")).isEqualTo("&lt;");
            assertThat(SimpleEmailTemplate.escapeHtml(">")).isEqualTo("&gt;");
            assertThat(SimpleEmailTemplate.escapeHtml("\"")).isEqualTo("&quot;");
            assertThat(SimpleEmailTemplate.escapeHtml("'")).isEqualTo("&#39;");
        }

        @Test
        @DisplayName("普通文本不变")
        void testNormalText() {
            assertThat(SimpleEmailTemplate.escapeHtml("Hello World")).isEqualTo("Hello World");
        }
    }

    @Nested
    @DisplayName("loadTemplate() 测试")
    class LoadTemplateTests {

        @Test
        @DisplayName("从文件加载模板")
        void testLoadFromFile() throws Exception {
            Path file = tempDir.resolve("template.html");
            Files.writeString(file, "<html>Hello ${name}</html>");

            String content = SimpleEmailTemplate.loadTemplate(file);

            assertThat(content).isEqualTo("<html>Hello ${name}</html>");
        }

        @Test
        @DisplayName("文件不存在抛出异常")
        void testLoadNonExistentFile() {
            Path file = tempDir.resolve("nonexistent.html");

            assertThatThrownBy(() -> SimpleEmailTemplate.loadTemplate(file))
                    .isInstanceOf(EmailTemplateException.class)
                    .hasMessageContaining("Failed to load template");
        }
    }

    @Nested
    @DisplayName("loadTemplateFromClasspath() 测试")
    class LoadTemplateFromClasspathTests {

        @Test
        @DisplayName("资源不存在抛出异常")
        void testLoadNonExistentResource() {
            assertThatThrownBy(() -> SimpleEmailTemplate.loadTemplateFromClasspath("nonexistent.html"))
                    .isInstanceOf(EmailTemplateException.class)
                    .hasMessageContaining("Template not found");
        }

        @Test
        @DisplayName("使用自定义类加载器加载资源不存在抛出异常")
        void testLoadWithCustomClassLoader() {
            ClassLoader classLoader = SimpleEmailTemplateTest.class.getClassLoader();

            assertThatThrownBy(() -> SimpleEmailTemplate.loadTemplateFromClasspath("nonexistent.html", classLoader))
                    .isInstanceOf(EmailTemplateException.class)
                    .hasMessageContaining("Template not found");
        }
    }

    @Nested
    @DisplayName("缓存测试")
    class CacheTests {

        @Test
        @DisplayName("loadTemplateCached() 缓存模板")
        void testCacheFromFile() throws Exception {
            Path file = tempDir.resolve("cached.html");
            Files.writeString(file, "Original content");

            // First load
            String content1 = SimpleEmailTemplate.loadTemplateCached(file);
            assertThat(content1).isEqualTo("Original content");

            // Modify file
            Files.writeString(file, "Modified content");

            // Second load should return cached content
            String content2 = SimpleEmailTemplate.loadTemplateCached(file);
            assertThat(content2).isEqualTo("Original content");

            assertThat(SimpleEmailTemplate.getCacheSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("clearCache() 清除所有缓存")
        void testClearCache() throws Exception {
            Path file1 = tempDir.resolve("file1.html");
            Path file2 = tempDir.resolve("file2.html");
            Files.writeString(file1, "Content 1");
            Files.writeString(file2, "Content 2");

            SimpleEmailTemplate.loadTemplateCached(file1);
            SimpleEmailTemplate.loadTemplateCached(file2);
            assertThat(SimpleEmailTemplate.getCacheSize()).isEqualTo(2);

            SimpleEmailTemplate.clearCache();
            assertThat(SimpleEmailTemplate.getCacheSize()).isEqualTo(0);
        }

        @Test
        @DisplayName("removeFromCache() 移除特定缓存")
        void testRemoveFromCache() throws Exception {
            Path file = tempDir.resolve("toremove.html");
            Files.writeString(file, "Content");

            SimpleEmailTemplate.loadTemplateCached(file);
            assertThat(SimpleEmailTemplate.getCacheSize()).isEqualTo(1);

            SimpleEmailTemplate.removeFromCache(file.toAbsolutePath().toString());
            assertThat(SimpleEmailTemplate.getCacheSize()).isEqualTo(0);
        }

        @Test
        @DisplayName("getCacheSize() 返回缓存大小")
        void testGetCacheSize() throws Exception {
            assertThat(SimpleEmailTemplate.getCacheSize()).isEqualTo(0);

            Path file = tempDir.resolve("size.html");
            Files.writeString(file, "Content");
            SimpleEmailTemplate.loadTemplateCached(file);

            assertThat(SimpleEmailTemplate.getCacheSize()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("createHtmlTemplate() 测试")
    class CreateHtmlTemplateTests {

        @Test
        @DisplayName("创建HTML模板")
        void testCreateHtmlTemplate() {
            String html = SimpleEmailTemplate.createHtmlTemplate("Welcome", "<h1>Hello</h1>");

            assertThat(html).contains("<!DOCTYPE html>");
            assertThat(html).contains("<title>Welcome</title>");
            assertThat(html).contains("<h1>Hello</h1>");
            assertThat(html).contains("<meta charset=\"UTF-8\">");
        }

        @Test
        @DisplayName("标题HTML转义")
        void testTitleEscaping() {
            String html = SimpleEmailTemplate.createHtmlTemplate("<script>alert()</script>", "Body");

            assertThat(html).contains("<title>&lt;script&gt;alert()&lt;/script&gt;</title>");
        }

        @Test
        @DisplayName("正文不转义")
        void testBodyNotEscaped() {
            String html = SimpleEmailTemplate.createHtmlTemplate("Title", "<b>Bold</b>");

            assertThat(html).contains("<b>Bold</b>");
        }
    }

    @Nested
    @DisplayName("特殊字符处理测试")
    class SpecialCharacterTests {

        @Test
        @DisplayName("处理$符号在替换中")
        void testDollarSignInReplacement() {
            String template = "Price: ${price}";
            Map<String, Object> variables = Map.of("price", "$100");

            String result = SimpleEmailTemplate.renderTemplate(template, variables);

            assertThat(result).isEqualTo("Price: $100");
        }

        @Test
        @DisplayName("处理反斜杠在替换中")
        void testBackslashInReplacement() {
            String template = "Path: ${path}";
            Map<String, Object> variables = Map.of("path", "C:\\Users\\John");

            String result = SimpleEmailTemplate.renderTemplate(template, variables);

            assertThat(result).isEqualTo("Path: C:\\Users\\John");
        }

        @Test
        @DisplayName("处理正则特殊字符")
        void testRegexSpecialChars() {
            String template = "${text}";
            Map<String, Object> variables = Map.of("text", "a+b*c?d");

            String result = SimpleEmailTemplate.renderTemplate(template, variables);

            assertThat(result).isEqualTo("a+b*c?d");
        }
    }
}
