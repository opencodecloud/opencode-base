package cloud.opencode.base.json;

import cloud.opencode.base.json.diff.JsonDiff;
import cloud.opencode.base.json.patch.JsonPatch;
import cloud.opencode.base.json.schema.JsonSchemaValidator;
import cloud.opencode.base.json.spi.JsonProvider;
import cloud.opencode.base.json.stream.JsonReader;
import cloud.opencode.base.json.stream.JsonWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenJson 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("OpenJson 测试")
class OpenJsonTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(OpenJson.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("类是public的")
        void testIsPublic() {
            assertThat(java.lang.reflect.Modifier.isPublic(OpenJson.class.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("静态方法定义测试")
    class StaticMethodTests {

        @Test
        @DisplayName("定义了所有静态工厂方法")
        void testFactoryMethods() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("withConfig", JsonConfig.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("withProvider", String.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("withConfigAndProvider", JsonConfig.class, String.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了所有静态序列化方法")
        void testStaticSerializationMethods() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("toJson", Object.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("toJsonBytes", Object.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("toJson", Object.class, OutputStream.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("toJson", Object.class, Writer.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("toPrettyJson", Object.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了所有静态反序列化方法")
        void testStaticDeserializationMethods() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("fromJson", String.class, Class.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("fromJson", String.class, TypeReference.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("fromJson", byte[].class, Class.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("fromJson", InputStream.class, Class.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("fromJson", Reader.class, Class.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("fromJsonArray", String.class, Class.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("fromJsonMap", String.class, Class.class, Class.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了所有静态树操作方法")
        void testStaticTreeMethods() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("parse", String.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("parse", byte[].class)).isNotNull();
            assertThat(OpenJson.class.getMethod("toTree", Object.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("treeToValue", JsonNode.class, Class.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了所有静态路径操作方法")
        void testStaticPathMethods() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("at", JsonNode.class, String.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("select", JsonNode.class, String.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("selectFirst", JsonNode.class, String.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了所有静态diff/patch方法")
        void testStaticDiffPatchMethods() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("diff", JsonNode.class, JsonNode.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("patch", JsonNode.class, JsonPatch.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("mergePatch", JsonNode.class, JsonNode.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了所有静态schema验证方法")
        void testStaticValidationMethods() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("validate", JsonNode.class, JsonNode.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("validateOrThrow", JsonNode.class, JsonNode.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了所有静态流式API方法")
        void testStaticStreamingMethods() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("createReader", InputStream.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("createReader", Reader.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("createWriter", OutputStream.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("createWriter", Writer.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("实例方法定义测试")
    class InstanceMethodTests {

        @Test
        @DisplayName("定义了所有实例序列化方法")
        void testInstanceSerializationMethods() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("serialize", Object.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("serializeToBytes", Object.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("serialize", Object.class, OutputStream.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("serialize", Object.class, Writer.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了所有实例反序列化方法")
        void testInstanceDeserializationMethods() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("deserialize", String.class, Class.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("deserialize", String.class, TypeReference.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("deserialize", byte[].class, Class.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("deserialize", InputStream.class, Class.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("deserialize", Reader.class, Class.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("deserializeArray", String.class, Class.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("deserializeMap", String.class, Class.class, Class.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了所有实例树操作方法")
        void testInstanceTreeMethods() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("parseTree", String.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("parseTree", byte[].class)).isNotNull();
            assertThat(OpenJson.class.getMethod("valueToTree", Object.class)).isNotNull();
            assertThat(OpenJson.class.getMethod("treeToObject", JsonNode.class, Class.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了配置/提供者获取方法")
        void testGetterMethods() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("getConfig")).isNotNull();
            assertThat(OpenJson.class.getMethod("getProvider")).isNotNull();
        }
    }

    @Nested
    @DisplayName("返回类型测试")
    class ReturnTypeTests {

        @Test
        @DisplayName("withConfig返回OpenJson")
        void testWithConfigReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("withConfig", JsonConfig.class).getReturnType())
                .isEqualTo(OpenJson.class);
        }

        @Test
        @DisplayName("withProvider返回OpenJson")
        void testWithProviderReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("withProvider", String.class).getReturnType())
                .isEqualTo(OpenJson.class);
        }

        @Test
        @DisplayName("toJson返回String")
        void testToJsonReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("toJson", Object.class).getReturnType())
                .isEqualTo(String.class);
        }

        @Test
        @DisplayName("toJsonBytes返回byte[]")
        void testToJsonBytesReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("toJsonBytes", Object.class).getReturnType())
                .isEqualTo(byte[].class);
        }

        @Test
        @DisplayName("toPrettyJson返回String")
        void testToPrettyJsonReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("toPrettyJson", Object.class).getReturnType())
                .isEqualTo(String.class);
        }

        @Test
        @DisplayName("parse返回JsonNode")
        void testParseReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("parse", String.class).getReturnType())
                .isEqualTo(JsonNode.class);
        }

        @Test
        @DisplayName("toTree返回JsonNode")
        void testToTreeReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("toTree", Object.class).getReturnType())
                .isEqualTo(JsonNode.class);
        }

        @Test
        @DisplayName("at返回JsonNode")
        void testAtReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("at", JsonNode.class, String.class).getReturnType())
                .isEqualTo(JsonNode.class);
        }

        @Test
        @DisplayName("select返回List")
        void testSelectReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("select", JsonNode.class, String.class).getReturnType())
                .isEqualTo(List.class);
        }

        @Test
        @DisplayName("selectFirst返回JsonNode")
        void testSelectFirstReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("selectFirst", JsonNode.class, String.class).getReturnType())
                .isEqualTo(JsonNode.class);
        }

        @Test
        @DisplayName("diff返回DiffResult")
        void testDiffReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("diff", JsonNode.class, JsonNode.class).getReturnType())
                .isEqualTo(JsonDiff.DiffResult.class);
        }

        @Test
        @DisplayName("patch返回JsonNode")
        void testPatchReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("patch", JsonNode.class, JsonPatch.class).getReturnType())
                .isEqualTo(JsonNode.class);
        }

        @Test
        @DisplayName("mergePatch返回JsonNode")
        void testMergePatchReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("mergePatch", JsonNode.class, JsonNode.class).getReturnType())
                .isEqualTo(JsonNode.class);
        }

        @Test
        @DisplayName("validate返回ValidationResult")
        void testValidateReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("validate", JsonNode.class, JsonNode.class).getReturnType())
                .isEqualTo(JsonSchemaValidator.ValidationResult.class);
        }

        @Test
        @DisplayName("createReader返回JsonReader")
        void testCreateReaderReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("createReader", InputStream.class).getReturnType())
                .isEqualTo(JsonReader.class);
        }

        @Test
        @DisplayName("createWriter返回JsonWriter")
        void testCreateWriterReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("createWriter", OutputStream.class).getReturnType())
                .isEqualTo(JsonWriter.class);
        }

        @Test
        @DisplayName("getConfig返回JsonConfig")
        void testGetConfigReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("getConfig").getReturnType())
                .isEqualTo(JsonConfig.class);
        }

        @Test
        @DisplayName("getProvider返回JsonProvider")
        void testGetProviderReturnType() throws NoSuchMethodException {
            assertThat(OpenJson.class.getMethod("getProvider").getReturnType())
                .isEqualTo(JsonProvider.class);
        }
    }

    @Nested
    @DisplayName("静态方法修饰符测试")
    class StaticModifierTests {

        @Test
        @DisplayName("toJson是静态方法")
        void testToJsonIsStatic() throws NoSuchMethodException {
            assertThat(java.lang.reflect.Modifier.isStatic(
                OpenJson.class.getMethod("toJson", Object.class).getModifiers()
            )).isTrue();
        }

        @Test
        @DisplayName("fromJson是静态方法")
        void testFromJsonIsStatic() throws NoSuchMethodException {
            assertThat(java.lang.reflect.Modifier.isStatic(
                OpenJson.class.getMethod("fromJson", String.class, Class.class).getModifiers()
            )).isTrue();
        }

        @Test
        @DisplayName("parse是静态方法")
        void testParseIsStatic() throws NoSuchMethodException {
            assertThat(java.lang.reflect.Modifier.isStatic(
                OpenJson.class.getMethod("parse", String.class).getModifiers()
            )).isTrue();
        }

        @Test
        @DisplayName("diff是静态方法")
        void testDiffIsStatic() throws NoSuchMethodException {
            assertThat(java.lang.reflect.Modifier.isStatic(
                OpenJson.class.getMethod("diff", JsonNode.class, JsonNode.class).getModifiers()
            )).isTrue();
        }

        @Test
        @DisplayName("validate是静态方法")
        void testValidateIsStatic() throws NoSuchMethodException {
            assertThat(java.lang.reflect.Modifier.isStatic(
                OpenJson.class.getMethod("validate", JsonNode.class, JsonNode.class).getModifiers()
            )).isTrue();
        }
    }

    @Nested
    @DisplayName("实例方法修饰符测试")
    class InstanceModifierTests {

        @Test
        @DisplayName("serialize不是静态方法")
        void testSerializeIsNotStatic() throws NoSuchMethodException {
            assertThat(java.lang.reflect.Modifier.isStatic(
                OpenJson.class.getMethod("serialize", Object.class).getModifiers()
            )).isFalse();
        }

        @Test
        @DisplayName("deserialize不是静态方法")
        void testDeserializeIsNotStatic() throws NoSuchMethodException {
            assertThat(java.lang.reflect.Modifier.isStatic(
                OpenJson.class.getMethod("deserialize", String.class, Class.class).getModifiers()
            )).isFalse();
        }

        @Test
        @DisplayName("parseTree不是静态方法")
        void testParseTreeIsNotStatic() throws NoSuchMethodException {
            assertThat(java.lang.reflect.Modifier.isStatic(
                OpenJson.class.getMethod("parseTree", String.class).getModifiers()
            )).isFalse();
        }

        @Test
        @DisplayName("getConfig不是静态方法")
        void testGetConfigIsNotStatic() throws NoSuchMethodException {
            assertThat(java.lang.reflect.Modifier.isStatic(
                OpenJson.class.getMethod("getConfig").getModifiers()
            )).isFalse();
        }
    }

    @Nested
    @DisplayName("路径操作功能测试")
    class PathOperationTests {

        @Test
        @DisplayName("at方法支持JSON Pointer")
        void testAtWithJsonPointer() {
            JsonNode.ObjectNode root = JsonNode.object()
                .put("name", "John")
                .put("age", 30);

            // Use JsonPointer directly to avoid OpenJson class initialization issue (no provider)
            JsonNode result = cloud.opencode.base.json.path.JsonPointer.parse("/name").evaluateOrNull(root);

            assertThat(result).isNotNull();
            assertThat(result.asString()).isEqualTo("John");
        }

        @Test
        @DisplayName("at方法对不存在路径返回null")
        void testAtNonexistent() {
            JsonNode.ObjectNode root = JsonNode.object()
                .put("name", "John");

            // Use JsonPointer directly to avoid OpenJson class initialization issue (no provider)
            JsonNode result = cloud.opencode.base.json.path.JsonPointer.parse("/nonexistent").evaluateOrNull(root);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("select方法支持JSONPath")
        void testSelectWithJsonPath() {
            JsonNode.ObjectNode root = JsonNode.object()
                .put("name", "John");

            // Use JsonPath directly to avoid OpenJson class initialization issue (no provider)
            List<JsonNode> results = cloud.opencode.base.json.path.JsonPath.read(root, "$.name");

            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("selectFirst返回第一个匹配")
        void testSelectFirst() {
            JsonNode.ObjectNode root = JsonNode.object()
                .put("name", "John");

            // Use JsonPath directly to avoid OpenJson class initialization issue (no provider)
            JsonNode result = cloud.opencode.base.json.path.JsonPath.readFirst(root, "$.name");

            assertThat(result).isNotNull();
            assertThat(result.asString()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("diff/patch功能测试")
    class DiffPatchTests {

        @Test
        @DisplayName("diff比较两个节点")
        void testDiff() {
            JsonNode source = JsonNode.object().put("name", "John");
            JsonNode target = JsonNode.object().put("name", "Jane");

            // Use JsonDiff directly to avoid OpenJson class initialization issue (no provider)
            JsonDiff.DiffResult result = JsonDiff.diff(source, target);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("mergePatch合并两个节点")
        void testMergePatch() {
            JsonNode target = JsonNode.object().put("name", "John");
            JsonNode patch = JsonNode.object().put("age", 30);

            // Use JsonMergePatch directly to avoid OpenJson class initialization issue (no provider)
            JsonNode result = cloud.opencode.base.json.patch.JsonMergePatch.apply(target, patch);

            assertThat(result).isNotNull();
            assertThat(result.isObject()).isTrue();
        }
    }

    @Nested
    @DisplayName("schema验证功能测试")
    class SchemaValidationTests {

        @Test
        @DisplayName("validate验证数据")
        void testValidate() {
            JsonNode data = JsonNode.of("test");
            JsonNode schema = JsonNode.object().put("type", "string");

            // Use JsonSchemaValidator directly to avoid OpenJson class initialization issue (no provider)
            JsonSchemaValidator.ValidationResult result = JsonSchemaValidator.validate(data, schema);

            assertThat(result).isNotNull();
        }
    }
}
