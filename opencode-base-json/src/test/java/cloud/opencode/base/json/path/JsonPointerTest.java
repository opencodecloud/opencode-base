package cloud.opencode.base.json.path;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonPointer 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonPointer 测试")
class JsonPointerTest {

    @Nested
    @DisplayName("parse方法测试")
    class ParseTests {

        @Test
        @DisplayName("解析空字符串返回ROOT")
        void testParseEmpty() {
            JsonPointer pointer = JsonPointer.parse("");

            assertThat(pointer).isSameAs(JsonPointer.ROOT);
            assertThat(pointer.isRoot()).isTrue();
        }

        @Test
        @DisplayName("解析单层路径")
        void testParseSingleLevel() {
            JsonPointer pointer = JsonPointer.parse("/foo");

            assertThat(pointer.getTokens()).containsExactly("foo");
            assertThat(pointer.toString()).isEqualTo("/foo");
        }

        @Test
        @DisplayName("解析多层路径")
        void testParseMultiLevel() {
            JsonPointer pointer = JsonPointer.parse("/foo/bar/baz");

            assertThat(pointer.getTokens()).containsExactly("foo", "bar", "baz");
        }

        @Test
        @DisplayName("解析数组索引路径")
        void testParseArrayIndex() {
            JsonPointer pointer = JsonPointer.parse("/foo/0/bar");

            assertThat(pointer.getTokens()).containsExactly("foo", "0", "bar");
        }

        @Test
        @DisplayName("解析转义的斜杠")
        void testParseEscapedSlash() {
            JsonPointer pointer = JsonPointer.parse("/a~1b");

            assertThat(pointer.getTokens()).containsExactly("a/b");
        }

        @Test
        @DisplayName("解析转义的波浪号")
        void testParseEscapedTilde() {
            JsonPointer pointer = JsonPointer.parse("/m~0n");

            assertThat(pointer.getTokens()).containsExactly("m~n");
        }

        @Test
        @DisplayName("不以/开头抛出异常")
        void testParseInvalidNoSlash() {
            assertThatThrownBy(() -> JsonPointer.parse("foo"))
                .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("null抛出异常")
        void testParseNull() {
            assertThatThrownBy(() -> JsonPointer.parse(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("of方法测试")
    class OfTests {

        @Test
        @DisplayName("从tokens创建指针")
        void testOfTokens() {
            JsonPointer pointer = JsonPointer.of("foo", "bar");

            assertThat(pointer.getTokens()).containsExactly("foo", "bar");
            assertThat(pointer.toString()).isEqualTo("/foo/bar");
        }

        @Test
        @DisplayName("空tokens返回ROOT")
        void testOfEmptyTokens() {
            JsonPointer pointer = JsonPointer.of();

            assertThat(pointer).isSameAs(JsonPointer.ROOT);
        }

        @Test
        @DisplayName("null tokens返回ROOT")
        void testOfNullTokens() {
            JsonPointer pointer = JsonPointer.of((String[]) null);

            assertThat(pointer).isSameAs(JsonPointer.ROOT);
        }

        @Test
        @DisplayName("从List创建指针")
        void testOfList() {
            JsonPointer pointer = JsonPointer.of(List.of("a", "b", "c"));

            assertThat(pointer.getTokens()).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("tokens包含特殊字符时正确转义")
        void testOfWithSpecialChars() {
            JsonPointer pointer = JsonPointer.of("a/b", "m~n");

            assertThat(pointer.toString()).contains("~1").contains("~0");
        }
    }

    @Nested
    @DisplayName("evaluate方法测试")
    class EvaluateTests {

        @Test
        @DisplayName("ROOT返回根节点")
        void testEvaluateRoot() {
            JsonNode root = JsonNode.object().put("name", "test");

            JsonNode result = JsonPointer.ROOT.evaluate(root);

            assertThat(result).isEqualTo(root);
        }

        @Test
        @DisplayName("访问对象属性")
        void testEvaluateObjectProperty() {
            JsonNode root = JsonNode.object().put("name", "John");

            JsonNode result = JsonPointer.parse("/name").evaluate(root);

            assertThat(result.asString()).isEqualTo("John");
        }

        @Test
        @DisplayName("访问嵌套属性")
        void testEvaluateNestedProperty() {
            JsonNode user = JsonNode.object().put("name", "John");
            JsonNode root = JsonNode.object().put("user", user);

            JsonNode result = JsonPointer.parse("/user/name").evaluate(root);

            assertThat(result.asString()).isEqualTo("John");
        }

        @Test
        @DisplayName("访问数组元素")
        void testEvaluateArrayElement() {
            JsonNode array = JsonNode.array().add("a").add("b").add("c");
            JsonNode root = JsonNode.object().put("items", array);

            JsonNode result = JsonPointer.parse("/items/1").evaluate(root);

            assertThat(result.asString()).isEqualTo("b");
        }

        @Test
        @DisplayName("路径不存在抛出异常")
        void testEvaluatePathNotFound() {
            JsonNode root = JsonNode.object().put("name", "test");

            assertThatThrownBy(() -> JsonPointer.parse("/missing").evaluate(root))
                .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("无效数组索引抛出异常")
        void testEvaluateInvalidArrayIndex() {
            JsonNode array = JsonNode.array().add("a");
            JsonNode root = JsonNode.object().put("items", array);

            assertThatThrownBy(() -> JsonPointer.parse("/items/abc").evaluate(root))
                .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("null根节点抛出异常")
        void testEvaluateNullRoot() {
            assertThatThrownBy(() -> JsonPointer.parse("/foo").evaluate(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("evaluateOrNull方法测试")
    class EvaluateOrNullTests {

        @Test
        @DisplayName("路径存在返回节点")
        void testEvaluateOrNullExists() {
            JsonNode root = JsonNode.object().put("name", "test");

            JsonNode result = JsonPointer.parse("/name").evaluateOrNull(root);

            assertThat(result).isNotNull();
            assertThat(result.asString()).isEqualTo("test");
        }

        @Test
        @DisplayName("路径不存在返回null")
        void testEvaluateOrNullNotExists() {
            JsonNode root = JsonNode.object().put("name", "test");

            JsonNode result = JsonPointer.parse("/missing").evaluateOrNull(root);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null根节点返回null")
        void testEvaluateOrNullNullRoot() {
            JsonNode result = JsonPointer.parse("/foo").evaluateOrNull(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("exists方法测试")
    class ExistsTests {

        @Test
        @DisplayName("路径存在返回true")
        void testExistsTrue() {
            JsonNode root = JsonNode.object().put("name", "test");

            assertThat(JsonPointer.parse("/name").exists(root)).isTrue();
        }

        @Test
        @DisplayName("路径不存在返回false")
        void testExistsFalse() {
            JsonNode root = JsonNode.object().put("name", "test");

            assertThat(JsonPointer.parse("/missing").exists(root)).isFalse();
        }
    }

    @Nested
    @DisplayName("parent方法测试")
    class ParentTests {

        @Test
        @DisplayName("ROOT的parent是ROOT")
        void testParentOfRoot() {
            assertThat(JsonPointer.ROOT.parent()).isSameAs(JsonPointer.ROOT);
        }

        @Test
        @DisplayName("获取父指针")
        void testParent() {
            JsonPointer pointer = JsonPointer.parse("/foo/bar/baz");

            JsonPointer parent = pointer.parent();

            assertThat(parent.toString()).isEqualTo("/foo/bar");
        }

        @Test
        @DisplayName("单层路径的parent是ROOT")
        void testParentOfSingleLevel() {
            JsonPointer pointer = JsonPointer.parse("/foo");

            assertThat(pointer.parent()).isSameAs(JsonPointer.ROOT);
        }
    }

    @Nested
    @DisplayName("append方法测试")
    class AppendTests {

        @Test
        @DisplayName("追加属性名")
        void testAppendProperty() {
            JsonPointer pointer = JsonPointer.parse("/foo");

            JsonPointer appended = pointer.append("bar");

            assertThat(appended.toString()).isEqualTo("/foo/bar");
        }

        @Test
        @DisplayName("追加数组索引")
        void testAppendIndex() {
            JsonPointer pointer = JsonPointer.parse("/items");

            JsonPointer appended = pointer.append(0);

            assertThat(appended.toString()).isEqualTo("/items/0");
        }

        @Test
        @DisplayName("从ROOT追加")
        void testAppendFromRoot() {
            JsonPointer appended = JsonPointer.ROOT.append("foo");

            assertThat(appended.toString()).isEqualTo("/foo");
        }
    }

    @Nested
    @DisplayName("getLastToken方法测试")
    class GetLastTokenTests {

        @Test
        @DisplayName("ROOT返回null")
        void testLastTokenOfRoot() {
            assertThat(JsonPointer.ROOT.getLastToken()).isNull();
        }

        @Test
        @DisplayName("获取最后一个token")
        void testLastToken() {
            JsonPointer pointer = JsonPointer.parse("/foo/bar");

            assertThat(pointer.getLastToken()).isEqualTo("bar");
        }
    }

    @Nested
    @DisplayName("depth方法测试")
    class DepthTests {

        @Test
        @DisplayName("ROOT深度为0")
        void testDepthOfRoot() {
            assertThat(JsonPointer.ROOT.depth()).isEqualTo(0);
        }

        @Test
        @DisplayName("计算正确深度")
        void testDepth() {
            assertThat(JsonPointer.parse("/a").depth()).isEqualTo(1);
            assertThat(JsonPointer.parse("/a/b").depth()).isEqualTo(2);
            assertThat(JsonPointer.parse("/a/b/c").depth()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("isRoot方法测试")
    class IsRootTests {

        @Test
        @DisplayName("ROOT返回true")
        void testIsRootTrue() {
            assertThat(JsonPointer.ROOT.isRoot()).isTrue();
            assertThat(JsonPointer.parse("").isRoot()).isTrue();
        }

        @Test
        @DisplayName("非ROOT返回false")
        void testIsRootFalse() {
            assertThat(JsonPointer.parse("/foo").isRoot()).isFalse();
        }
    }

    @Nested
    @DisplayName("equals和hashCode方法测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同指针相等")
        void testEquals() {
            JsonPointer p1 = JsonPointer.parse("/foo/bar");
            JsonPointer p2 = JsonPointer.parse("/foo/bar");

            assertThat(p1).isEqualTo(p2);
            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }

        @Test
        @DisplayName("不同指针不相等")
        void testNotEquals() {
            JsonPointer p1 = JsonPointer.parse("/foo");
            JsonPointer p2 = JsonPointer.parse("/bar");

            assertThat(p1).isNotEqualTo(p2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            JsonPointer p = JsonPointer.parse("/foo");

            assertThat(p).isEqualTo(p);
        }

        @Test
        @DisplayName("与null不相等")
        void testNotEqualsNull() {
            JsonPointer p = JsonPointer.parse("/foo");

            assertThat(p).isNotEqualTo(null);
        }

        @Test
        @DisplayName("与其他类型不相等")
        void testNotEqualsOtherType() {
            JsonPointer p = JsonPointer.parse("/foo");

            assertThat(p).isNotEqualTo("/foo");
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("返回指针字符串")
        void testToString() {
            JsonPointer pointer = JsonPointer.parse("/foo/bar");

            assertThat(pointer.toString()).isEqualTo("/foo/bar");
        }

        @Test
        @DisplayName("ROOT返回空字符串")
        void testToStringRoot() {
            assertThat(JsonPointer.ROOT.toString()).isEmpty();
        }
    }

    @Nested
    @DisplayName("ROOT常量测试")
    class RootConstantTests {

        @Test
        @DisplayName("ROOT是单例")
        void testRootSingleton() {
            assertThat(JsonPointer.ROOT).isSameAs(JsonPointer.parse(""));
        }

        @Test
        @DisplayName("ROOT属性正确")
        void testRootProperties() {
            assertThat(JsonPointer.ROOT.isRoot()).isTrue();
            assertThat(JsonPointer.ROOT.depth()).isEqualTo(0);
            assertThat(JsonPointer.ROOT.getTokens()).isEmpty();
        }
    }
}
