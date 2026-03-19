package cloud.opencode.base.json.diff;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.patch.JsonPatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonDiff 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonDiff 测试")
class JsonDiffTest {

    @Nested
    @DisplayName("diff方法测试")
    class DiffMethodTests {

        @Test
        @DisplayName("相同文档无差异")
        void testIdenticalDocuments() {
            JsonNode doc = JsonNode.object().put("name", "John").put("age", 30);

            JsonDiff.DiffResult result = JsonDiff.diff(doc, doc);

            assertThat(result.isIdentical()).isTrue();
            assertThat(result.hasDifferences()).isFalse();
            assertThat(result.getDifferenceCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("检测添加的属性")
        void testAddedProperty() {
            JsonNode source = JsonNode.object().put("name", "John");
            JsonNode target = JsonNode.object().put("name", "John").put("age", 30);

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            assertThat(result.hasDifferences()).isTrue();
            List<JsonDiff.Difference> added = result.getDifferencesByType(JsonDiff.DiffType.ADDED);
            assertThat(added).hasSize(1);
            assertThat(added.get(0).path()).isEqualTo("/age");
        }

        @Test
        @DisplayName("检测移除的属性")
        void testRemovedProperty() {
            JsonNode source = JsonNode.object().put("name", "John").put("age", 30);
            JsonNode target = JsonNode.object().put("name", "John");

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            List<JsonDiff.Difference> removed = result.getDifferencesByType(JsonDiff.DiffType.REMOVED);
            assertThat(removed).hasSize(1);
            assertThat(removed.get(0).path()).isEqualTo("/age");
        }

        @Test
        @DisplayName("检测更改的值")
        void testChangedValue() {
            JsonNode source = JsonNode.object().put("name", "John");
            JsonNode target = JsonNode.object().put("name", "Jane");

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            List<JsonDiff.Difference> changed = result.getDifferencesByType(JsonDiff.DiffType.CHANGED);
            assertThat(changed).hasSize(1);
            assertThat(changed.get(0).path()).isEqualTo("/name");
        }

        @Test
        @DisplayName("检测类型变化")
        void testTypeChanged() {
            JsonNode source = JsonNode.object().put("value", "100");
            JsonNode target = JsonNode.object().put("value", 100);

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            List<JsonDiff.Difference> typeChanged = result.getDifferencesByType(JsonDiff.DiffType.TYPE_CHANGED);
            assertThat(typeChanged).hasSize(1);
        }

        @Test
        @DisplayName("比较嵌套对象")
        void testNestedObjects() {
            JsonNode sourceUser = JsonNode.object().put("name", "John");
            JsonNode source = JsonNode.object().put("user", sourceUser);

            JsonNode targetUser = JsonNode.object().put("name", "Jane");
            JsonNode target = JsonNode.object().put("user", targetUser);

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            assertThat(result.hasDifferences()).isTrue();
            assertThat(result.differences().get(0).path()).isEqualTo("/user/name");
        }

        @Test
        @DisplayName("比较数组")
        void testArrays() {
            JsonNode sourceArray = JsonNode.array().add(1).add(2);
            JsonNode source = JsonNode.object().put("nums", sourceArray);

            JsonNode targetArray = JsonNode.array().add(1).add(3);
            JsonNode target = JsonNode.object().put("nums", targetArray);

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            assertThat(result.hasDifferences()).isTrue();
        }

        @Test
        @DisplayName("比较不同长度数组")
        void testDifferentLengthArrays() {
            JsonNode sourceArray = JsonNode.array().add(1).add(2);
            JsonNode source = JsonNode.object().put("nums", sourceArray);

            JsonNode targetArray = JsonNode.array().add(1).add(2).add(3);
            JsonNode target = JsonNode.object().put("nums", targetArray);

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            List<JsonDiff.Difference> added = result.getDifferencesByType(JsonDiff.DiffType.ADDED);
            assertThat(added).hasSize(1);
            assertThat(added.get(0).path()).isEqualTo("/nums/2");
        }

        @Test
        @DisplayName("null到非null变化")
        void testNullToNonNull() {
            JsonNode source = JsonNode.object().putNull("value");
            JsonNode target = JsonNode.object().put("value", "test");

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            assertThat(result.hasDifferences()).isTrue();
        }

        @Test
        @DisplayName("非null到null变化")
        void testNonNullToNull() {
            JsonNode source = JsonNode.object().put("value", "test");
            JsonNode target = JsonNode.object().putNull("value");

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            assertThat(result.hasDifferences()).isTrue();
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsMethodTests {

        @Test
        @DisplayName("相同文档返回true")
        void testEqualsTrue() {
            JsonNode doc1 = JsonNode.object().put("name", "test");
            JsonNode doc2 = JsonNode.object().put("name", "test");

            assertThat(JsonDiff.equals(doc1, doc2)).isTrue();
        }

        @Test
        @DisplayName("不同文档返回false")
        void testEqualsFalse() {
            JsonNode doc1 = JsonNode.object().put("name", "test1");
            JsonNode doc2 = JsonNode.object().put("name", "test2");

            assertThat(JsonDiff.equals(doc1, doc2)).isFalse();
        }
    }

    @Nested
    @DisplayName("DiffResult测试")
    class DiffResultTests {

        @Test
        @DisplayName("isIdentical相同文档返回true")
        void testIsIdentical() {
            JsonNode doc = JsonNode.object().put("a", 1);

            JsonDiff.DiffResult result = JsonDiff.diff(doc, doc);

            assertThat(result.isIdentical()).isTrue();
        }

        @Test
        @DisplayName("hasDifferences有差异返回true")
        void testHasDifferences() {
            JsonNode source = JsonNode.object().put("a", 1);
            JsonNode target = JsonNode.object().put("a", 2);

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            assertThat(result.hasDifferences()).isTrue();
        }

        @Test
        @DisplayName("getDifferenceCount返回正确数量")
        void testGetDifferenceCount() {
            JsonNode source = JsonNode.object().put("a", 1).put("b", 2);
            JsonNode target = JsonNode.object().put("a", 10).put("c", 3);

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            assertThat(result.getDifferenceCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("getDifferencesByType过滤正确")
        void testGetDifferencesByType() {
            JsonNode source = JsonNode.object().put("name", "old");
            JsonNode target = JsonNode.object().put("name", "new").put("age", 25);

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            assertThat(result.getDifferencesByType(JsonDiff.DiffType.CHANGED)).hasSize(1);
            assertThat(result.getDifferencesByType(JsonDiff.DiffType.ADDED)).hasSize(1);
        }

        @Test
        @DisplayName("toPatch生成JSON Patch")
        void testToPatch() {
            JsonNode source = JsonNode.object().put("name", "John");
            JsonNode target = JsonNode.object().put("name", "Jane");

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);
            JsonPatch patch = result.toPatch();

            assertThat(patch).isNotNull();
            assertThat(patch.size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("getSummary返回摘要")
        void testGetSummary() {
            JsonNode source = JsonNode.object().put("a", 1);
            JsonNode target = JsonNode.object().put("a", 2).put("b", 3);

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            String summary = result.getSummary();
            assertThat(summary).contains("Differences:");
        }

        @Test
        @DisplayName("getSummary相同文档")
        void testGetSummaryIdentical() {
            JsonNode doc = JsonNode.object().put("a", 1);

            JsonDiff.DiffResult result = JsonDiff.diff(doc, doc);

            assertThat(result.getSummary()).contains("identical");
        }

        @Test
        @DisplayName("source和target可访问")
        void testSourceAndTarget() {
            JsonNode source = JsonNode.object().put("a", 1);
            JsonNode target = JsonNode.object().put("b", 2);

            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            assertThat(result.source()).isEqualTo(source);
            assertThat(result.target()).isEqualTo(target);
        }
    }

    @Nested
    @DisplayName("DiffType枚举测试")
    class DiffTypeTests {

        @Test
        @DisplayName("所有差异类型存在")
        void testAllDiffTypes() {
            assertThat(JsonDiff.DiffType.values())
                .containsExactlyInAnyOrder(
                    JsonDiff.DiffType.ADDED,
                    JsonDiff.DiffType.REMOVED,
                    JsonDiff.DiffType.CHANGED,
                    JsonDiff.DiffType.TYPE_CHANGED
                );
        }
    }

    @Nested
    @DisplayName("Difference记录测试")
    class DifferenceTests {

        @Test
        @DisplayName("创建Difference")
        void testCreateDifference() {
            JsonNode source = JsonNode.of("old");
            JsonNode target = JsonNode.of("new");

            JsonDiff.Difference diff = new JsonDiff.Difference(
                JsonDiff.DiffType.CHANGED, "/path", source, target);

            assertThat(diff.type()).isEqualTo(JsonDiff.DiffType.CHANGED);
            assertThat(diff.path()).isEqualTo("/path");
            assertThat(diff.sourceValue()).isEqualTo(source);
            assertThat(diff.targetValue()).isEqualTo(target);
        }

        @Test
        @DisplayName("ADDED的toString")
        void testToStringAdded() {
            JsonDiff.Difference diff = new JsonDiff.Difference(
                JsonDiff.DiffType.ADDED, "/name", null, JsonNode.of("John"));

            String str = diff.toString();
            assertThat(str).contains("ADDED");
            assertThat(str).contains("/name");
        }

        @Test
        @DisplayName("REMOVED的toString")
        void testToStringRemoved() {
            JsonDiff.Difference diff = new JsonDiff.Difference(
                JsonDiff.DiffType.REMOVED, "/age", JsonNode.of(30), null);

            String str = diff.toString();
            assertThat(str).contains("REMOVED");
            assertThat(str).contains("/age");
        }

        @Test
        @DisplayName("CHANGED的toString")
        void testToStringChanged() {
            JsonDiff.Difference diff = new JsonDiff.Difference(
                JsonDiff.DiffType.CHANGED, "/value", JsonNode.of("old"), JsonNode.of("new"));

            String str = diff.toString();
            assertThat(str).contains("CHANGED");
            assertThat(str).contains("->");
        }

        @Test
        @DisplayName("TYPE_CHANGED的toString")
        void testToStringTypeChanged() {
            JsonDiff.Difference diff = new JsonDiff.Difference(
                JsonDiff.DiffType.TYPE_CHANGED, "/value", JsonNode.of("100"), JsonNode.of(100));

            String str = diff.toString();
            assertThat(str).contains("TYPE_CHANGED");
        }
    }
}
