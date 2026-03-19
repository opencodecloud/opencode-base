package cloud.opencode.base.json.patch;

import cloud.opencode.base.json.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonMergePatch 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonMergePatch 测试")
class JsonMergePatchTest {

    @Nested
    @DisplayName("of方法测试")
    class OfMethodTests {

        @Test
        @DisplayName("从JsonNode创建补丁")
        void testOfJsonNode() {
            JsonNode patch = JsonNode.object().put("name", "John");

            JsonMergePatch mergePatch = JsonMergePatch.of(patch);

            assertThat(mergePatch).isNotNull();
            assertThat(mergePatch.getPatch()).isEqualTo(patch);
        }

        @Test
        @DisplayName("null补丁抛出异常")
        void testOfNullPatch() {
            assertThatThrownBy(() -> JsonMergePatch.of(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("builder方法测试")
    class BuilderTests {

        @Test
        @DisplayName("创建构建器")
        void testBuilder() {
            JsonMergePatch.Builder builder = JsonMergePatch.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("设置JsonNode值")
        void testSetJsonNode() {
            JsonMergePatch patch = JsonMergePatch.builder()
                .set("user", JsonNode.object().put("name", "John"))
                .build();

            assertThat(patch.getPatch().has("user")).isTrue();
        }

        @Test
        @DisplayName("设置字符串值")
        void testSetString() {
            JsonMergePatch patch = JsonMergePatch.builder()
                .set("name", "John")
                .build();

            assertThat(patch.getPatch().get("name").asString()).isEqualTo("John");
        }

        @Test
        @DisplayName("设置数字值")
        void testSetNumber() {
            JsonMergePatch patch = JsonMergePatch.builder()
                .set("age", 30)
                .build();

            assertThat(patch.getPatch().get("age").asInt()).isEqualTo(30);
        }

        @Test
        @DisplayName("设置布尔值")
        void testSetBoolean() {
            JsonMergePatch patch = JsonMergePatch.builder()
                .set("active", true)
                .build();

            assertThat(patch.getPatch().get("active").asBoolean()).isTrue();
        }

        @Test
        @DisplayName("移除属性")
        void testRemove() {
            JsonMergePatch patch = JsonMergePatch.builder()
                .remove("age")
                .build();

            assertThat(patch.getPatch().get("age").isNull()).isTrue();
        }

        @Test
        @DisplayName("链式操作")
        void testChainedOperations() {
            JsonMergePatch patch = JsonMergePatch.builder()
                .set("name", "Jane")
                .set("age", 25)
                .remove("city")
                .set("email", "jane@example.com")
                .build();

            assertThat(patch.getPatch().get("name").asString()).isEqualTo("Jane");
            assertThat(patch.getPatch().get("age").asInt()).isEqualTo(25);
            assertThat(patch.getPatch().get("city").isNull()).isTrue();
            assertThat(patch.getPatch().get("email").asString()).isEqualTo("jane@example.com");
        }

        @Test
        @DisplayName("构建并应用")
        void testBuildAndApply() {
            JsonNode target = JsonNode.object().put("name", "John").put("age", 30);

            JsonNode result = JsonMergePatch.builder()
                .set("name", "Jane")
                .apply(target);

            assertThat(result.get("name").asString()).isEqualTo("Jane");
            assertThat(result.get("age").asInt()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("apply静态方法测试")
    class ApplyStaticMethodTests {

        @Test
        @DisplayName("应用简单补丁")
        void testApplySimplePatch() {
            JsonNode target = JsonNode.object().put("name", "John");
            JsonNode patch = JsonNode.object().put("name", "Jane");

            JsonNode result = JsonMergePatch.apply(target, patch);

            assertThat(result.get("name").asString()).isEqualTo("Jane");
        }

        @Test
        @DisplayName("添加新属性")
        void testApplyAddProperty() {
            JsonNode target = JsonNode.object().put("name", "John");
            JsonNode patch = JsonNode.object().put("age", 30);

            JsonNode result = JsonMergePatch.apply(target, patch);

            assertThat(result.get("name").asString()).isEqualTo("John");
            assertThat(result.get("age").asInt()).isEqualTo(30);
        }

        @Test
        @DisplayName("移除属性")
        void testApplyRemoveProperty() {
            JsonNode target = JsonNode.object().put("name", "John").put("age", 30);
            JsonNode patch = JsonNode.object().putNull("age");

            JsonNode result = JsonMergePatch.apply(target, patch);

            assertThat(result.has("name")).isTrue();
            assertThat(result.has("age")).isFalse();
        }
    }

    @Nested
    @DisplayName("apply实例方法测试")
    class ApplyInstanceMethodTests {

        @Test
        @DisplayName("更新属性值")
        void testApplyUpdateProperty() {
            JsonNode target = JsonNode.object().put("name", "John").put("age", 30);
            JsonMergePatch patch = JsonMergePatch.of(
                JsonNode.object().put("age", 31)
            );

            JsonNode result = patch.apply(target);

            assertThat(result.get("age").asInt()).isEqualTo(31);
        }

        @Test
        @DisplayName("合并嵌套对象")
        void testApplyMergeNestedObject() {
            JsonNode targetAddress = JsonNode.object()
                .put("city", "NYC")
                .put("zip", "10001");
            JsonNode target = JsonNode.object()
                .put("name", "John")
                .put("address", targetAddress);

            JsonNode patchAddress = JsonNode.object().put("city", "LA");
            JsonNode patch = JsonNode.object().put("address", patchAddress);

            JsonNode result = JsonMergePatch.apply(target, patch);

            assertThat(result.get("address").get("city").asString()).isEqualTo("LA");
            assertThat(result.get("address").get("zip").asString()).isEqualTo("10001");
        }

        @Test
        @DisplayName("非对象补丁替换整个文档")
        void testApplyNonObjectPatchReplacesTarget() {
            JsonNode target = JsonNode.object().put("name", "John");
            JsonNode patch = JsonNode.of("replaced");

            JsonNode result = JsonMergePatch.apply(target, patch);

            assertThat(result.isString()).isTrue();
            assertThat(result.asString()).isEqualTo("replaced");
        }

        @Test
        @DisplayName("目标非对象时从空对象开始")
        void testApplyTargetNotObject() {
            JsonNode target = JsonNode.of("old value");
            JsonNode patch = JsonNode.object().put("name", "John");

            JsonNode result = JsonMergePatch.apply(target, patch);

            assertThat(result.isObject()).isTrue();
            assertThat(result.get("name").asString()).isEqualTo("John");
        }

        @Test
        @DisplayName("数组补丁替换数组")
        void testApplyArrayPatch() {
            JsonNode target = JsonNode.array().add(1).add(2);
            JsonNode patch = JsonNode.array().add(3).add(4);

            JsonNode result = JsonMergePatch.apply(target, patch);

            assertThat(result.isArray()).isTrue();
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.get(0).asInt()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("diff方法测试")
    class DiffMethodTests {

        @Test
        @DisplayName("相同文档生成空补丁")
        void testDiffIdenticalDocuments() {
            JsonNode doc = JsonNode.object().put("name", "John");

            JsonMergePatch diff = JsonMergePatch.diff(doc, doc);

            assertThat(diff.getPatch().isEmpty()).isTrue();
        }

        @Test
        @DisplayName("检测添加的属性")
        void testDiffAddedProperty() {
            JsonNode source = JsonNode.object().put("name", "John");
            JsonNode target = JsonNode.object().put("name", "John").put("age", 30);

            JsonMergePatch diff = JsonMergePatch.diff(source, target);

            assertThat(diff.getPatch().get("age").asInt()).isEqualTo(30);
        }

        @Test
        @DisplayName("检测移除的属性")
        void testDiffRemovedProperty() {
            JsonNode source = JsonNode.object().put("name", "John").put("age", 30);
            JsonNode target = JsonNode.object().put("name", "John");

            JsonMergePatch diff = JsonMergePatch.diff(source, target);

            assertThat(diff.getPatch().get("age").isNull()).isTrue();
        }

        @Test
        @DisplayName("检测更改的属性")
        void testDiffChangedProperty() {
            JsonNode source = JsonNode.object().put("name", "John");
            JsonNode target = JsonNode.object().put("name", "Jane");

            JsonMergePatch diff = JsonMergePatch.diff(source, target);

            assertThat(diff.getPatch().get("name").asString()).isEqualTo("Jane");
        }

        @Test
        @DisplayName("嵌套对象差异")
        void testDiffNestedObject() {
            JsonNode sourceAddress = JsonNode.object().put("city", "NYC");
            JsonNode source = JsonNode.object().put("address", sourceAddress);

            JsonNode targetAddress = JsonNode.object().put("city", "LA");
            JsonNode target = JsonNode.object().put("address", targetAddress);

            JsonMergePatch diff = JsonMergePatch.diff(source, target);

            assertThat(diff.getPatch().get("address").get("city").asString()).isEqualTo("LA");
        }

        @Test
        @DisplayName("目标非对象时返回整个目标")
        void testDiffTargetNotObject() {
            JsonNode source = JsonNode.object().put("name", "John");
            JsonNode target = JsonNode.of("value");

            JsonMergePatch diff = JsonMergePatch.diff(source, target);

            assertThat(diff.getPatch().asString()).isEqualTo("value");
        }

        @Test
        @DisplayName("源非对象时返回整个目标")
        void testDiffSourceNotObject() {
            JsonNode source = JsonNode.of("old");
            JsonNode target = JsonNode.object().put("name", "John");

            JsonMergePatch diff = JsonMergePatch.diff(source, target);

            assertThat(diff.getPatch().get("name").asString()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("深拷贝测试")
    class DeepCopyTests {

        @Test
        @DisplayName("补丁应用不修改原始对象")
        void testApplyDoesNotModifyOriginal() {
            JsonNode target = JsonNode.object().put("name", "John");
            JsonNode patch = JsonNode.object().put("name", "Jane");

            JsonMergePatch.apply(target, patch);

            assertThat(target.get("name").asString()).isEqualTo("John");
        }

        @Test
        @DisplayName("嵌套对象深拷贝")
        void testDeepCopyNestedObjects() {
            JsonNode.ObjectNode inner = JsonNode.object().put("x", 1);
            JsonNode target = JsonNode.object().put("inner", inner);
            JsonNode patch = JsonNode.object().put("other", "value");

            JsonNode result = JsonMergePatch.apply(target, patch);

            // Modify original
            inner.put("x", 2);

            // Result should not be affected
            assertThat(result.get("inner").get("x").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("数组深拷贝")
        void testDeepCopyArrays() {
            JsonNode array = JsonNode.array().add(1).add(2);
            JsonNode target = JsonNode.object().put("nums", array);
            JsonNode patch = JsonNode.object();

            JsonNode result = JsonMergePatch.apply(target, patch);

            assertThat(result.get("nums").size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getPatch方法测试")
    class GetPatchTests {

        @Test
        @DisplayName("返回补丁内容")
        void testGetPatch() {
            JsonNode patchNode = JsonNode.object().put("name", "test");
            JsonMergePatch patch = JsonMergePatch.of(patchNode);

            assertThat(patch.getPatch()).isEqualTo(patchNode);
        }
    }

    @Nested
    @DisplayName("RFC 7396 规范测试")
    class Rfc7396Tests {

        @Test
        @DisplayName("RFC示例1 - 替换成员")
        void testRfcExample1() {
            JsonNode target = JsonNode.object()
                .put("a", "b")
                .put("c", JsonNode.object().put("d", "e").put("f", "g"));
            JsonNode patch = JsonNode.object()
                .put("a", "z")
                .put("c", JsonNode.object().putNull("f"));

            JsonNode result = JsonMergePatch.apply(target, patch);

            assertThat(result.get("a").asString()).isEqualTo("z");
            assertThat(result.get("c").get("d").asString()).isEqualTo("e");
            assertThat(result.get("c").has("f")).isFalse();
        }

        @Test
        @DisplayName("RFC示例2 - 完整替换")
        void testRfcExample2() {
            JsonNode target = JsonNode.object()
                .put("title", "Hello!")
                .put("author", JsonNode.object().put("givenName", "John"));
            JsonNode patch = JsonNode.object()
                .put("title", "Goodbye!")
                .put("author", JsonNode.object().put("familyName", "Doe"));

            JsonNode result = JsonMergePatch.apply(target, patch);

            assertThat(result.get("title").asString()).isEqualTo("Goodbye!");
            assertThat(result.get("author").get("familyName").asString()).isEqualTo("Doe");
            // Note: givenName should still exist in a merge patch scenario for objects
        }
    }
}
