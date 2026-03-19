package cloud.opencode.base.json.patch;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonPatch 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonPatch 测试")
class JsonPatchTest {

    @Nested
    @DisplayName("builder方法测试")
    class BuilderTests {

        @Test
        @DisplayName("创建空补丁")
        void testEmptyPatch() {
            JsonPatch patch = JsonPatch.builder().build();

            assertThat(patch.size()).isEqualTo(0);
            assertThat(patch.getOperations()).isEmpty();
        }

        @Test
        @DisplayName("添加add操作")
        void testAddOperation() {
            JsonPatch patch = JsonPatch.builder()
                .add("/name", JsonNode.of("John"))
                .build();

            assertThat(patch.size()).isEqualTo(1);
            assertThat(patch.getOperations().get(0).op()).isEqualTo(JsonPatch.Operation.ADD);
        }

        @Test
        @DisplayName("添加remove操作")
        void testRemoveOperation() {
            JsonPatch patch = JsonPatch.builder()
                .remove("/age")
                .build();

            assertThat(patch.getOperations().get(0).op()).isEqualTo(JsonPatch.Operation.REMOVE);
        }

        @Test
        @DisplayName("添加replace操作")
        void testReplaceOperation() {
            JsonPatch patch = JsonPatch.builder()
                .replace("/name", JsonNode.of("Jane"))
                .build();

            assertThat(patch.getOperations().get(0).op()).isEqualTo(JsonPatch.Operation.REPLACE);
        }

        @Test
        @DisplayName("添加move操作")
        void testMoveOperation() {
            JsonPatch patch = JsonPatch.builder()
                .move("/old", "/new")
                .build();

            assertThat(patch.getOperations().get(0).op()).isEqualTo(JsonPatch.Operation.MOVE);
        }

        @Test
        @DisplayName("添加copy操作")
        void testCopyOperation() {
            JsonPatch patch = JsonPatch.builder()
                .copy("/source", "/target")
                .build();

            assertThat(patch.getOperations().get(0).op()).isEqualTo(JsonPatch.Operation.COPY);
        }

        @Test
        @DisplayName("添加test操作")
        void testTestOperation() {
            JsonPatch patch = JsonPatch.builder()
                .test("/name", JsonNode.of("John"))
                .build();

            assertThat(patch.getOperations().get(0).op()).isEqualTo(JsonPatch.Operation.TEST);
        }

        @Test
        @DisplayName("链式添加多个操作")
        void testChainedOperations() {
            JsonPatch patch = JsonPatch.builder()
                .add("/a", JsonNode.of(1))
                .remove("/b")
                .replace("/c", JsonNode.of(2))
                .build();

            assertThat(patch.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("of方法测试")
    class OfMethodTests {

        @Test
        @DisplayName("从操作列表创建补丁")
        void testOfOperations() {
            List<JsonPatch.PatchOperation> ops = List.of(
                new JsonPatch.PatchOperation(JsonPatch.Operation.ADD, "/name", JsonNode.of("test")),
                new JsonPatch.PatchOperation(JsonPatch.Operation.REMOVE, "/age")
            );

            JsonPatch patch = JsonPatch.of(ops);

            assertThat(patch.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("apply方法测试-ADD操作")
    class ApplyAddTests {

        @Test
        @DisplayName("添加新属性")
        void testAddNewProperty() {
            JsonNode target = JsonNode.object().put("name", "John");
            JsonPatch patch = JsonPatch.builder()
                .add("/age", JsonNode.of(30))
                .build();

            JsonNode result = patch.apply(target);

            assertThat(result.get("age").asInt()).isEqualTo(30);
            assertThat(result.get("name").asString()).isEqualTo("John");
        }

        @Test
        @DisplayName("添加到数组末尾")
        void testAddToArrayEnd() {
            JsonNode array = JsonNode.array().add(1).add(2);
            JsonNode target = JsonNode.object().put("nums", array);
            JsonPatch patch = JsonPatch.builder()
                .add("/nums/-", JsonNode.of(3))
                .build();

            JsonNode result = patch.apply(target);

            assertThat(result.get("nums").size()).isEqualTo(3);
            assertThat(result.get("nums").get(2).asInt()).isEqualTo(3);
        }

        @Test
        @DisplayName("添加到数组指定位置")
        void testAddToArrayIndex() {
            JsonNode array = JsonNode.array().add(1).add(3);
            JsonNode target = JsonNode.object().put("nums", array);
            JsonPatch patch = JsonPatch.builder()
                .add("/nums/1", JsonNode.of(2))
                .build();

            JsonNode result = patch.apply(target);

            assertThat(result.get("nums").get(1).asInt()).isEqualTo(2);
        }

        @Test
        @DisplayName("添加根节点替换整个文档")
        void testAddRoot() {
            JsonNode target = JsonNode.object().put("old", "value");
            JsonPatch patch = JsonPatch.builder()
                .add("", JsonNode.object().put("new", "value"))
                .build();

            JsonNode result = patch.apply(target);

            assertThat(result.has("new")).isTrue();
            assertThat(result.has("old")).isFalse();
        }
    }

    @Nested
    @DisplayName("apply方法测试-REMOVE操作")
    class ApplyRemoveTests {

        @Test
        @DisplayName("移除属性")
        void testRemoveProperty() {
            JsonNode target = JsonNode.object().put("name", "John").put("age", 30);
            JsonPatch patch = JsonPatch.builder()
                .remove("/age")
                .build();

            JsonNode result = patch.apply(target);

            assertThat(result.has("age")).isFalse();
            assertThat(result.has("name")).isTrue();
        }

        @Test
        @DisplayName("移除数组元素")
        void testRemoveArrayElement() {
            JsonNode array = JsonNode.array().add(1).add(2).add(3);
            JsonNode target = JsonNode.object().put("nums", array);
            JsonPatch patch = JsonPatch.builder()
                .remove("/nums/1")
                .build();

            JsonNode result = patch.apply(target);

            assertThat(result.get("nums").size()).isEqualTo(2);
        }

        @Test
        @DisplayName("移除不存在的属性抛出异常")
        void testRemoveNonExistent() {
            JsonNode target = JsonNode.object().put("name", "John");
            JsonPatch patch = JsonPatch.builder()
                .remove("/missing")
                .build();

            assertThatThrownBy(() -> patch.apply(target))
                .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("移除根节点抛出异常")
        void testRemoveRoot() {
            JsonNode target = JsonNode.object().put("name", "test");
            JsonPatch patch = JsonPatch.builder()
                .remove("")
                .build();

            assertThatThrownBy(() -> patch.apply(target))
                .isInstanceOf(OpenJsonProcessingException.class);
        }
    }

    @Nested
    @DisplayName("apply方法测试-REPLACE操作")
    class ApplyReplaceTests {

        @Test
        @DisplayName("替换属性值")
        void testReplaceProperty() {
            JsonNode target = JsonNode.object().put("name", "John");
            JsonPatch patch = JsonPatch.builder()
                .replace("/name", JsonNode.of("Jane"))
                .build();

            JsonNode result = patch.apply(target);

            assertThat(result.get("name").asString()).isEqualTo("Jane");
        }

        @Test
        @DisplayName("替换数组元素")
        void testReplaceArrayElement() {
            JsonNode array = JsonNode.array().add(1).add(2).add(3);
            JsonNode target = JsonNode.object().put("nums", array);
            JsonPatch patch = JsonPatch.builder()
                .replace("/nums/1", JsonNode.of(20))
                .build();

            JsonNode result = patch.apply(target);

            assertThat(result.get("nums").get(1).asInt()).isEqualTo(20);
        }

        @Test
        @DisplayName("替换不存在的路径抛出异常")
        void testReplaceNonExistent() {
            JsonNode target = JsonNode.object().put("name", "John");
            JsonPatch patch = JsonPatch.builder()
                .replace("/missing", JsonNode.of("value"))
                .build();

            assertThatThrownBy(() -> patch.apply(target))
                .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("替换根节点")
        void testReplaceRoot() {
            JsonNode target = JsonNode.object().put("old", "value");
            JsonPatch patch = JsonPatch.builder()
                .replace("", JsonNode.object().put("new", "value"))
                .build();

            JsonNode result = patch.apply(target);

            assertThat(result.has("new")).isTrue();
        }
    }

    @Nested
    @DisplayName("apply方法测试-MOVE操作")
    class ApplyMoveTests {

        @Test
        @DisplayName("移动属性")
        void testMoveProperty() {
            JsonNode target = JsonNode.object().put("old_name", "John");
            JsonPatch patch = JsonPatch.builder()
                .move("/old_name", "/new_name")
                .build();

            JsonNode result = patch.apply(target);

            assertThat(result.has("old_name")).isFalse();
            assertThat(result.get("new_name").asString()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("apply方法测试-COPY操作")
    class ApplyCopyTests {

        @Test
        @DisplayName("复制属性")
        void testCopyProperty() {
            JsonNode target = JsonNode.object().put("source", "value");
            JsonPatch patch = JsonPatch.builder()
                .copy("/source", "/target")
                .build();

            JsonNode result = patch.apply(target);

            assertThat(result.get("source").asString()).isEqualTo("value");
            assertThat(result.get("target").asString()).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("apply方法测试-TEST操作")
    class ApplyTestTests {

        @Test
        @DisplayName("测试通过")
        void testTestPass() {
            JsonNode target = JsonNode.object().put("name", "John");
            JsonPatch patch = JsonPatch.builder()
                .test("/name", JsonNode.of("John"))
                .build();

            JsonNode result = patch.apply(target);

            assertThat(result).isEqualTo(target);
        }

        @Test
        @DisplayName("测试失败抛出异常")
        void testTestFail() {
            JsonNode target = JsonNode.object().put("name", "John");
            JsonPatch patch = JsonPatch.builder()
                .test("/name", JsonNode.of("Jane"))
                .build();

            assertThatThrownBy(() -> patch.apply(target))
                .isInstanceOf(OpenJsonProcessingException.class);
        }
    }

    @Nested
    @DisplayName("validate方法测试")
    class ValidateTests {

        @Test
        @DisplayName("有效补丁返回true")
        void testValidateTrue() {
            JsonNode target = JsonNode.object().put("name", "John");
            JsonPatch patch = JsonPatch.builder()
                .replace("/name", JsonNode.of("Jane"))
                .build();

            assertThat(patch.validate(target)).isTrue();
        }

        @Test
        @DisplayName("无效补丁返回false")
        void testValidateFalse() {
            JsonNode target = JsonNode.object().put("name", "John");
            JsonPatch patch = JsonPatch.builder()
                .remove("/missing")
                .build();

            assertThat(patch.validate(target)).isFalse();
        }
    }

    @Nested
    @DisplayName("apply null检查测试")
    class ApplyNullTests {

        @Test
        @DisplayName("null目标抛出异常")
        void testApplyNullTarget() {
            JsonPatch patch = JsonPatch.builder()
                .add("/name", JsonNode.of("test"))
                .build();

            assertThatThrownBy(() -> patch.apply(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("PatchOperation记录测试")
    class PatchOperationTests {

        @Test
        @DisplayName("创建带值的操作")
        void testOperationWithValue() {
            JsonPatch.PatchOperation op = new JsonPatch.PatchOperation(
                JsonPatch.Operation.ADD, "/path", JsonNode.of("value"));

            assertThat(op.op()).isEqualTo(JsonPatch.Operation.ADD);
            assertThat(op.path()).isEqualTo("/path");
            assertThat(op.value().asString()).isEqualTo("value");
            assertThat(op.from()).isNull();
        }

        @Test
        @DisplayName("创建无值操作")
        void testOperationWithoutValue() {
            JsonPatch.PatchOperation op = new JsonPatch.PatchOperation(
                JsonPatch.Operation.REMOVE, "/path");

            assertThat(op.op()).isEqualTo(JsonPatch.Operation.REMOVE);
            assertThat(op.value()).isNull();
        }

        @Test
        @DisplayName("创建带from的操作")
        void testOperationWithFrom() {
            JsonPatch.PatchOperation op = new JsonPatch.PatchOperation(
                JsonPatch.Operation.MOVE, "/target", "/source");

            assertThat(op.from()).isEqualTo("/source");
        }
    }

    @Nested
    @DisplayName("Operation枚举测试")
    class OperationEnumTests {

        @Test
        @DisplayName("所有操作类型存在")
        void testAllOperations() {
            assertThat(JsonPatch.Operation.values())
                .containsExactlyInAnyOrder(
                    JsonPatch.Operation.ADD,
                    JsonPatch.Operation.REMOVE,
                    JsonPatch.Operation.REPLACE,
                    JsonPatch.Operation.MOVE,
                    JsonPatch.Operation.COPY,
                    JsonPatch.Operation.TEST
                );
        }
    }
}
