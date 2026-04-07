package cloud.opencode.base.oauth2.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonParser Tests
 * JsonParser 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
@DisplayName("JsonParser 测试")
class JsonParserTest {

    @Nested
    @DisplayName("getString 测试")
    class GetStringTests {

        @Test
        @DisplayName("提取简单字符串字段")
        void testSimpleString() {
            String json = """
                    {"access_token": "abc123", "token_type": "Bearer"}""";
            assertThat(JsonParser.getString(json, "access_token")).isEqualTo("abc123");
            assertThat(JsonParser.getString(json, "token_type")).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("字段不存在返回null")
        void testFieldNotFound() {
            String json = """
                    {"access_token": "abc123"}""";
            assertThat(JsonParser.getString(json, "refresh_token")).isNull();
        }

        @Test
        @DisplayName("null值返回null")
        void testNullValue() {
            String json = """
                    {"access_token": null}""";
            assertThat(JsonParser.getString(json, "access_token")).isNull();
        }

        @Test
        @DisplayName("null JSON返回null")
        void testNullJson() {
            assertThat(JsonParser.getString(null, "field")).isNull();
        }

        @Test
        @DisplayName("空JSON返回null")
        void testEmptyJson() {
            assertThat(JsonParser.getString("", "field")).isNull();
        }

        @Test
        @DisplayName("处理转义引号")
        void testEscapedQuotes() {
            String json = """
                    {"message": "say \\"hello\\""}""";
            assertThat(JsonParser.getString(json, "message")).isEqualTo("say \"hello\"");
        }

        @Test
        @DisplayName("处理转义字符")
        void testEscapeSequences() {
            String json = """
                    {"message": "line1\\nline2\\ttab"}""";
            assertThat(JsonParser.getString(json, "message")).isEqualTo("line1\nline2\ttab");
        }

        @Test
        @DisplayName("处理Unicode转义")
        void testUnicodeEscape() {
            String json = """
                    {"name": "\\u0041\\u0042\\u0043"}""";
            assertThat(JsonParser.getString(json, "name")).isEqualTo("ABC");
        }

        @Test
        @DisplayName("不匹配部分字段名")
        void testNoPartialMatch() {
            String json = """
                    {"custom_access_token": "wrong", "access_token": "correct"}""";
            assertThat(JsonParser.getString(json, "access_token")).isEqualTo("correct");
        }

        @Test
        @DisplayName("非字符串值返回null")
        void testNonStringValue() {
            String json = """
                    {"count": 42}""";
            assertThat(JsonParser.getString(json, "count")).isNull();
        }
    }

    @Nested
    @DisplayName("getLong 测试")
    class GetLongTests {

        @Test
        @DisplayName("提取长整型字段")
        void testSimpleLong() {
            String json = """
                    {"expires_in": 3600}""";
            assertThat(JsonParser.getLong(json, "expires_in")).isEqualTo(3600L);
        }

        @Test
        @DisplayName("提取负数")
        void testNegativeLong() {
            String json = """
                    {"offset": -100}""";
            assertThat(JsonParser.getLong(json, "offset")).isEqualTo(-100L);
        }

        @Test
        @DisplayName("字段不存在返回null")
        void testFieldNotFound() {
            String json = """
                    {"expires_in": 3600}""";
            assertThat(JsonParser.getLong(json, "interval")).isNull();
        }

        @Test
        @DisplayName("null值返回null")
        void testNullValue() {
            String json = """
                    {"expires_in": null}""";
            assertThat(JsonParser.getLong(json, "expires_in")).isNull();
        }

        @Test
        @DisplayName("null JSON返回null")
        void testNullJson() {
            assertThat(JsonParser.getLong(null, "field")).isNull();
        }

        @Test
        @DisplayName("空JSON返回null")
        void testEmptyJson() {
            assertThat(JsonParser.getLong("", "field")).isNull();
        }
    }

    @Nested
    @DisplayName("getBoolean 测试")
    class GetBooleanTests {

        @Test
        @DisplayName("提取true值")
        void testTrueValue() {
            String json = """
                    {"active": true}""";
            assertThat(JsonParser.getBoolean(json, "active")).isTrue();
        }

        @Test
        @DisplayName("提取false值")
        void testFalseValue() {
            String json = """
                    {"active": false}""";
            assertThat(JsonParser.getBoolean(json, "active")).isFalse();
        }

        @Test
        @DisplayName("字段不存在返回null")
        void testFieldNotFound() {
            String json = """
                    {"active": true}""";
            assertThat(JsonParser.getBoolean(json, "enabled")).isNull();
        }

        @Test
        @DisplayName("null值返回null")
        void testNullValue() {
            String json = """
                    {"active": null}""";
            assertThat(JsonParser.getBoolean(json, "active")).isNull();
        }

        @Test
        @DisplayName("null JSON返回null")
        void testNullJson() {
            assertThat(JsonParser.getBoolean(null, "field")).isNull();
        }
    }

    @Nested
    @DisplayName("parseObject 测试")
    class ParseObjectTests {

        @Test
        @DisplayName("解析包含各种类型的对象")
        void testMixedTypes() {
            String json = """
                    {"name": "test", "count": 42, "active": true, "data": null}""";
            Map<String, Object> result = JsonParser.parseObject(json);
            assertThat(result).hasSize(4);
            assertThat(result.get("name")).isEqualTo("test");
            assertThat(result.get("count")).isEqualTo(42L);
            assertThat(result.get("active")).isEqualTo(Boolean.TRUE);
            assertThat(result).containsEntry("data", null);
        }

        @Test
        @DisplayName("解析空对象")
        void testEmptyObject() {
            Map<String, Object> result = JsonParser.parseObject("{}");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null返回空Map")
        void testNullJson() {
            assertThat(JsonParser.parseObject(null)).isEmpty();
        }

        @Test
        @DisplayName("空字符串返回空Map")
        void testEmptyJson() {
            assertThat(JsonParser.parseObject("")).isEmpty();
        }

        @Test
        @DisplayName("非JSON对象返回空Map")
        void testNonObject() {
            assertThat(JsonParser.parseObject("[1,2,3]")).isEmpty();
        }

        @Test
        @DisplayName("解析OAuth2 Token响应")
        void testOAuth2TokenResponse() {
            String json = """
                    {
                      "access_token": "eyJhbGciOiJSUzI1NiJ9",
                      "token_type": "Bearer",
                      "expires_in": 3600,
                      "refresh_token": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4",
                      "scope": "openid profile email"
                    }""";
            Map<String, Object> result = JsonParser.parseObject(json);
            assertThat(result.get("access_token")).isEqualTo("eyJhbGciOiJSUzI1NiJ9");
            assertThat(result.get("token_type")).isEqualTo("Bearer");
            assertThat(result.get("expires_in")).isEqualTo(3600L);
            assertThat(result.get("refresh_token")).isEqualTo("dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4");
            assertThat(result.get("scope")).isEqualTo("openid profile email");
        }

        @Test
        @DisplayName("解析OAuth2错误响应")
        void testOAuth2ErrorResponse() {
            String json = """
                    {"error": "invalid_grant", "error_description": "The authorization code has expired", "error_uri": "https://example.com/errors"}""";
            Map<String, Object> result = JsonParser.parseObject(json);
            assertThat(result.get("error")).isEqualTo("invalid_grant");
            assertThat(result.get("error_description")).isEqualTo("The authorization code has expired");
            assertThat(result.get("error_uri")).isEqualTo("https://example.com/errors");
        }

        @Test
        @DisplayName("解析包含false值的对象")
        void testFalseValue() {
            String json = """
                    {"active": false, "name": "test"}""";
            Map<String, Object> result = JsonParser.parseObject(json);
            assertThat(result.get("active")).isEqualTo(Boolean.FALSE);
            assertThat(result.get("name")).isEqualTo("test");
        }

        @Test
        @DisplayName("解析带有转义字符的字符串")
        void testEscapedStrings() {
            String json = """
                    {"message": "say \\"hello\\"", "path": "a\\\\b"}""";
            Map<String, Object> result = JsonParser.parseObject(json);
            assertThat(result.get("message")).isEqualTo("say \"hello\"");
            assertThat(result.get("path")).isEqualTo("a\\b");
        }

        @Test
        @DisplayName("解析带空格的JSON")
        void testWhitespace() {
            String json = """
                    {
                        "key1"  :  "value1"  ,
                        "key2"  :  123
                    }""";
            Map<String, Object> result = JsonParser.parseObject(json);
            assertThat(result.get("key1")).isEqualTo("value1");
            assertThat(result.get("key2")).isEqualTo(123L);
        }
    }
}
