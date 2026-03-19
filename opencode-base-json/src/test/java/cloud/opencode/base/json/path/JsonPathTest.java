package cloud.opencode.base.json.path;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonPath 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonPath 测试")
class JsonPathTest {

    @Nested
    @DisplayName("compile方法测试")
    class CompileTests {

        @Test
        @DisplayName("编译简单路径")
        void testCompileSimple() {
            JsonPath path = JsonPath.compile("$.name");

            assertThat(path.getExpression()).isEqualTo("$.name");
        }

        @Test
        @DisplayName("编译嵌套路径")
        void testCompileNested() {
            JsonPath path = JsonPath.compile("$.user.name");

            assertThat(path.getExpression()).isEqualTo("$.user.name");
        }

        @Test
        @DisplayName("不以$开头抛出异常")
        void testCompileInvalidNoRoot() {
            assertThatThrownBy(() -> JsonPath.compile("name"))
                .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("null抛出异常")
        void testCompileNull() {
            assertThatThrownBy(() -> JsonPath.compile(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("read方法测试")
    class ReadTests {

        @Test
        @DisplayName("读取简单属性")
        void testReadSimpleProperty() {
            JsonNode root = JsonNode.object().put("name", "John");

            List<JsonNode> results = JsonPath.read(root, "$.name");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).asString()).isEqualTo("John");
        }

        @Test
        @DisplayName("读取嵌套属性")
        void testReadNestedProperty() {
            JsonNode user = JsonNode.object().put("name", "John");
            JsonNode root = JsonNode.object().put("user", user);

            List<JsonNode> results = JsonPath.read(root, "$.user.name");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).asString()).isEqualTo("John");
        }

        @Test
        @DisplayName("读取数组元素")
        void testReadArrayElement() {
            JsonNode array = JsonNode.array().add("a").add("b").add("c");
            JsonNode root = JsonNode.object().put("items", array);

            List<JsonNode> results = JsonPath.read(root, "$.items[1]");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).asString()).isEqualTo("b");
        }

        @Test
        @DisplayName("读取不存在的路径返回空列表")
        void testReadNonExistentPath() {
            JsonNode root = JsonNode.object().put("name", "test");

            List<JsonNode> results = JsonPath.read(root, "$.missing");

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("readFirst方法测试")
    class ReadFirstTests {

        @Test
        @DisplayName("返回第一个匹配")
        void testReadFirst() {
            JsonNode root = JsonNode.object().put("name", "John");

            JsonNode result = JsonPath.readFirst(root, "$.name");

            assertThat(result).isNotNull();
            assertThat(result.asString()).isEqualTo("John");
        }

        @Test
        @DisplayName("无匹配返回null")
        void testReadFirstNoMatch() {
            JsonNode root = JsonNode.object().put("name", "test");

            JsonNode result = JsonPath.readFirst(root, "$.missing");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("exists方法测试")
    class ExistsTests {

        @Test
        @DisplayName("存在返回true")
        void testExistsTrue() {
            JsonNode root = JsonNode.object().put("name", "test");

            assertThat(JsonPath.exists(root, "$.name")).isTrue();
        }

        @Test
        @DisplayName("不存在返回false")
        void testExistsFalse() {
            JsonNode root = JsonNode.object().put("name", "test");

            assertThat(JsonPath.exists(root, "$.missing")).isFalse();
        }
    }

    @Nested
    @DisplayName("通配符测试")
    class WildcardTests {

        @Test
        @DisplayName("对象通配符")
        void testObjectWildcard() {
            JsonNode root = JsonNode.object()
                .put("a", "1")
                .put("b", "2")
                .put("c", "3");

            List<JsonNode> results = JsonPath.read(root, "$.*");

            assertThat(results).hasSize(3);
        }

        @Test
        @DisplayName("数组通配符")
        void testArrayWildcard() {
            JsonNode array = JsonNode.array().add("x").add("y").add("z");
            JsonNode root = JsonNode.object().put("items", array);

            List<JsonNode> results = JsonPath.read(root, "$.items[*]");

            assertThat(results).hasSize(3);
        }
    }

    @Nested
    @DisplayName("数组切片测试")
    class ArraySliceTests {

        @Test
        @DisplayName("基本切片")
        void testBasicSlice() {
            JsonNode array = JsonNode.array().add(0).add(1).add(2).add(3).add(4);
            JsonNode root = JsonNode.object().put("nums", array);

            List<JsonNode> results = JsonPath.read(root, "$.nums[1:3]");

            assertThat(results).hasSize(2);
            assertThat(results.get(0).asInt()).isEqualTo(1);
            assertThat(results.get(1).asInt()).isEqualTo(2);
        }

        @Test
        @DisplayName("开放起始切片")
        void testOpenStartSlice() {
            JsonNode array = JsonNode.array().add(0).add(1).add(2);
            JsonNode root = JsonNode.object().put("nums", array);

            List<JsonNode> results = JsonPath.read(root, "$.nums[:2]");

            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("开放结束切片")
        void testOpenEndSlice() {
            JsonNode array = JsonNode.array().add(0).add(1).add(2);
            JsonNode root = JsonNode.object().put("nums", array);

            List<JsonNode> results = JsonPath.read(root, "$.nums[1:]");

            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("括号表示法测试")
    class BracketNotationTests {

        @Test
        @DisplayName("单引号属性名")
        void testSingleQuoteProperty() {
            JsonNode root = JsonNode.object().put("user-name", "John");

            List<JsonNode> results = JsonPath.read(root, "$['user-name']");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).asString()).isEqualTo("John");
        }

        @Test
        @DisplayName("双引号属性名")
        void testDoubleQuoteProperty() {
            JsonNode root = JsonNode.object().put("user.name", "John");

            List<JsonNode> results = JsonPath.read(root, "$[\"user.name\"]");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).asString()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("递归下降测试")
    class RecursiveDescentTests {

        @Test
        @DisplayName("递归查找属性")
        void testRecursiveDescent() {
            JsonNode inner = JsonNode.object().put("name", "inner");
            JsonNode outer = JsonNode.object()
                .put("name", "outer")
                .put("child", inner);

            List<JsonNode> results = JsonPath.read(outer, "$..name");

            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("递归查找嵌套数组")
        void testRecursiveDescentInArray() {
            JsonNode item1 = JsonNode.object().put("id", 1);
            JsonNode item2 = JsonNode.object().put("id", 2);
            JsonNode array = JsonNode.array().add(item1).add(item2);
            JsonNode root = JsonNode.object().put("items", array);

            List<JsonNode> results = JsonPath.read(root, "$..id");

            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("过滤表达式测试")
    class FilterExpressionTests {

        @Test
        @DisplayName("数值比较过滤")
        void testNumericFilter() {
            JsonNode item1 = JsonNode.object().put("price", 10);
            JsonNode item2 = JsonNode.object().put("price", 20);
            JsonNode item3 = JsonNode.object().put("price", 30);
            JsonNode array = JsonNode.array().add(item1).add(item2).add(item3);
            JsonNode root = JsonNode.object().put("items", array);

            List<JsonNode> results = JsonPath.read(root, "$.items[?(@.price < 25)]");

            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("等于比较过滤")
        void testEqualityFilter() {
            JsonNode item1 = JsonNode.object().put("status", "active");
            JsonNode item2 = JsonNode.object().put("status", "inactive");
            JsonNode array = JsonNode.array().add(item1).add(item2);
            JsonNode root = JsonNode.object().put("items", array);

            List<JsonNode> results = JsonPath.read(root, "$.items[?(@.status == 'active')]");

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("不等于比较过滤")
        void testNotEqualFilter() {
            JsonNode item1 = JsonNode.object().put("type", "A");
            JsonNode item2 = JsonNode.object().put("type", "B");
            JsonNode array = JsonNode.array().add(item1).add(item2);
            JsonNode root = JsonNode.object().put("items", array);

            List<JsonNode> results = JsonPath.read(root, "$.items[?(@.type != 'A')]");

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("布尔值过滤")
        void testBooleanFilter() {
            JsonNode item1 = JsonNode.object().put("active", true);
            JsonNode item2 = JsonNode.object().put("active", false);
            JsonNode array = JsonNode.array().add(item1).add(item2);
            JsonNode root = JsonNode.object().put("items", array);

            List<JsonNode> results = JsonPath.read(root, "$.items[?(@.active == true)]");

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("存在性过滤")
        void testExistenceFilter() {
            JsonNode item1 = JsonNode.object().put("name", "A");
            JsonNode item2 = JsonNode.object().put("id", 1);
            JsonNode array = JsonNode.array().add(item1).add(item2);
            JsonNode root = JsonNode.object().put("items", array);

            List<JsonNode> results = JsonPath.read(root, "$.items[?(@.name)]");

            assertThat(results).hasSize(1);
        }
    }

    @Nested
    @DisplayName("evaluate方法测试")
    class EvaluateTests {

        @Test
        @DisplayName("null根节点抛出异常")
        void testEvaluateNullRoot() {
            JsonPath path = JsonPath.compile("$.name");

            assertThatThrownBy(() -> path.evaluate(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("evaluateFirst返回第一个结果")
        void testEvaluateFirst() {
            JsonNode root = JsonNode.object().put("name", "test");
            JsonPath path = JsonPath.compile("$.name");

            JsonNode result = path.evaluateFirst(root);

            assertThat(result).isNotNull();
            assertThat(result.asString()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("返回表达式")
        void testToString() {
            JsonPath path = JsonPath.compile("$.user.name");

            assertThat(path.toString()).isEqualTo("$.user.name");
        }
    }

    @Nested
    @DisplayName("getExpression方法测试")
    class GetExpressionTests {

        @Test
        @DisplayName("返回原始表达式")
        void testGetExpression() {
            String expr = "$.items[*].name";
            JsonPath path = JsonPath.compile(expr);

            assertThat(path.getExpression()).isEqualTo(expr);
        }
    }
}
