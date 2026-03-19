package cloud.opencode.base.json.internal;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.annotation.JsonIgnore;
import cloud.opencode.base.json.annotation.JsonProperty;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * BeanMapperTest Tests
 * BeanMapperTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("BeanMapper 测试")
class BeanMapperTest {

    // ==================== Test Types ====================

    public static class Simple {
        public String name;
        public int age;
        public boolean active;
        public Simple() {}
    }

    public static class WithAnnotations {
        @JsonProperty("full_name")
        public String name;
        @JsonIgnore
        public String password;
        @JsonProperty(required = true)
        public String email;
        public WithAnnotations() {}
    }

    public static class WithCollections {
        public List<String> tags;
        public Map<String, Integer> scores;
        public String[] roles;
        public WithCollections() {}
    }

    public static class WithNested {
        public Simple inner;
        public List<Simple> items;
        public WithNested() {}
    }

    public record SimpleRecord(String name, int age) {}

    public record AnnotatedRecord(
            @JsonProperty("full_name") String name,
            int age
    ) {}

    public enum Status { ACTIVE, INACTIVE }

    public static class WithAllTypes {
        public String str;
        public int intVal;
        public long longVal;
        public double doubleVal;
        public float floatVal;
        public boolean boolVal;
        public short shortVal;
        public byte byteVal;
        public char charVal;
        public BigDecimal decimal;
        public Status status;
        public UUID uuid;
        public LocalDate date;
        public LocalDateTime dateTime;
        public Instant instant;
        public Duration duration;
        public Optional<String> opt;
        public WithAllTypes() {}
    }

    public static class Parent {
        public String parentField;
        public Parent() {}
    }

    public static class Child extends Parent {
        public String childField;
        public Child() {}
    }

    public static class WithReadOnly {
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        public String readOnly;
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        public String writeOnly;
        public String normal;
        public WithReadOnly() {}
    }

    // ==================== toTree Tests ====================

    @Nested
    @DisplayName("toTree 序列化")
    class ToTreeTests {

        @Test
        @DisplayName("null → NullNode")
        void null_value() {
            assertThat(BeanMapper.toTree(null).isNull()).isTrue();
        }

        @Test
        @DisplayName("String → StringNode")
        void string() {
            JsonNode node = BeanMapper.toTree("hello");
            assertThat(node.asString()).isEqualTo("hello");
        }

        @Test
        @DisplayName("primitives → NumberNode/BooleanNode")
        void primitives() {
            assertThat(BeanMapper.toTree(42).asInt()).isEqualTo(42);
            assertThat(BeanMapper.toTree(3.14).asDouble()).isCloseTo(3.14, within(0.001));
            assertThat(BeanMapper.toTree(true).asBoolean()).isTrue();
            assertThat(BeanMapper.toTree(100L).asLong()).isEqualTo(100L);
        }

        @Test
        @DisplayName("Enum → StringNode")
        void enumValue() {
            assertThat(BeanMapper.toTree(Status.ACTIVE).asString()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("LocalDate → StringNode")
        void localDate() {
            LocalDate d = LocalDate.of(2026, 3, 15);
            assertThat(BeanMapper.toTree(d).asString()).isEqualTo("2026-03-15");
        }

        @Test
        @DisplayName("UUID → StringNode")
        void uuid() {
            UUID u = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            assertThat(BeanMapper.toTree(u).asString()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        }

        @Test
        @DisplayName("Map → ObjectNode")
        void map() {
            JsonNode node = BeanMapper.toTree(Map.of("a", 1, "b", "two"));
            assertThat(node.isObject()).isTrue();
            assertThat(node.get("a").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("List → ArrayNode")
        void list() {
            JsonNode node = BeanMapper.toTree(List.of(1, 2, 3));
            assertThat(node.isArray()).isTrue();
        }

        @Test
        @DisplayName("Array → ArrayNode")
        void array() {
            JsonNode node = BeanMapper.toTree(new int[]{1, 2, 3});
            assertThat(node.isArray()).isTrue();
        }

        @Test
        @DisplayName("POJO → ObjectNode (with @JsonProperty)")
        void pojo() {
            WithAnnotations obj = new WithAnnotations();
            obj.name = "Alice";
            obj.password = "secret";
            obj.email = "a@b.com";
            JsonNode node = BeanMapper.toTree(obj);
            assertThat(node.get("full_name").asString()).isEqualTo("Alice");
            assertThat(node.get("password")).isNull(); // @JsonIgnore
            assertThat(node.get("email").asString()).isEqualTo("a@b.com");
        }

        @Test
        @DisplayName("POJO 继承字段")
        void inheritance() {
            Child c = new Child();
            c.parentField = "parent";
            c.childField = "child";
            JsonNode node = BeanMapper.toTree(c);
            assertThat(node.get("parentField").asString()).isEqualTo("parent");
            assertThat(node.get("childField").asString()).isEqualTo("child");
        }

        @Test
        @DisplayName("READ_ONLY 字段序列化，WRITE_ONLY 不序列化")
        void access_control() {
            WithReadOnly obj = new WithReadOnly();
            obj.readOnly = "r";
            obj.writeOnly = "w";
            obj.normal = "n";
            JsonNode node = BeanMapper.toTree(obj);
            assertThat(node.get("readOnly").asString()).isEqualTo("r");
            assertThat(node.get("writeOnly")).isNull(); // WRITE_ONLY → not serialized
            assertThat(node.get("normal").asString()).isEqualTo("n");
        }

        @Test
        @DisplayName("Optional → value or null")
        void optional() {
            assertThat(BeanMapper.toTree(Optional.of("yes")).asString()).isEqualTo("yes");
            assertThat(BeanMapper.toTree(Optional.empty()).isNull()).isTrue();
        }
    }

    // ==================== fromTree Tests ====================

    @Nested
    @DisplayName("fromTree 反序列化")
    class FromTreeTests {

        @Test
        @DisplayName("POJO 基本字段")
        void pojo_basic() {
            JsonNode node = JsonNode.object().put("name", "Bob").put("age", 25).put("active", true);
            Simple s = BeanMapper.fromTree(node, Simple.class);
            assertThat(s.name).isEqualTo("Bob");
            assertThat(s.age).isEqualTo(25);
            assertThat(s.active).isTrue();
        }

        @Test
        @DisplayName("POJO with @JsonProperty name mapping")
        void pojo_annotation() {
            JsonNode node = JsonNode.object().put("full_name", "Alice").put("email", "a@b.com");
            WithAnnotations obj = BeanMapper.fromTree(node, WithAnnotations.class);
            assertThat(obj.name).isEqualTo("Alice");
            assertThat(obj.email).isEqualTo("a@b.com");
        }

        @Test
        @DisplayName("required 字段缺失时抛异常")
        void pojo_required_missing() {
            JsonNode node = JsonNode.object().put("full_name", "X");
            assertThatThrownBy(() -> BeanMapper.fromTree(node, WithAnnotations.class))
                    .isInstanceOf(OpenJsonProcessingException.class)
                    .hasMessageContaining("Required");
        }

        @Test
        @DisplayName("嵌套对象")
        void nested() {
            JsonNode inner = JsonNode.object().put("name", "N").put("age", 1);
            JsonNode items = JsonNode.array().add(inner);
            JsonNode node = JsonNode.object().put("inner", inner).put("items", items);
            WithNested n = BeanMapper.fromTree(node, WithNested.class);
            assertThat(n.inner.name).isEqualTo("N");
            assertThat(n.items).hasSize(1);
        }

        @Test
        @DisplayName("集合字段")
        void collections() {
            JsonNode node = JsonNode.object()
                    .put("tags", JsonNode.array().add("a").add("b"))
                    .put("scores", JsonNode.object().put("math", 100))
                    .put("roles", JsonNode.array().add("admin"));
            WithCollections obj = BeanMapper.fromTree(node, WithCollections.class);
            assertThat(obj.tags).containsExactly("a", "b");
            assertThat(obj.scores).containsEntry("math", 100);
        }

        @Test
        @DisplayName("Record 反序列化")
        void record_type() {
            JsonNode node = JsonNode.object().put("name", "R").put("age", 20);
            SimpleRecord r = BeanMapper.fromTree(node, SimpleRecord.class);
            assertThat(r.name()).isEqualTo("R");
            assertThat(r.age()).isEqualTo(20);
        }

        @Test
        @DisplayName("Record with @JsonProperty")
        void record_annotated() {
            JsonNode node = JsonNode.object().put("full_name", "AR").put("age", 30);
            AnnotatedRecord r = BeanMapper.fromTree(node, AnnotatedRecord.class);
            assertThat(r.name()).isEqualTo("AR");
        }

        @Test
        @DisplayName("Enum 反序列化")
        void enum_type() {
            JsonNode node = new JsonNode.StringNode("INACTIVE");
            Status s = BeanMapper.fromTree(node, Status.class);
            assertThat(s).isEqualTo(Status.INACTIVE);
        }

        @Test
        @DisplayName("java.time 反序列化")
        void time_types() {
            LocalDate result = BeanMapper.fromTree(new JsonNode.StringNode("2026-03-15"), LocalDate.class);
            assertThat(result).isEqualTo(LocalDate.of(2026, 3, 15));
        }

        @Test
        @DisplayName("UUID 反序列化")
        void uuid_type() {
            UUID expected = UUID.randomUUID();
            UUID actual = BeanMapper.fromTree(new JsonNode.StringNode(expected.toString()), UUID.class);
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("null/NullNode → null")
        void null_handling() {
            Simple fromNull = BeanMapper.fromTree(null, Simple.class);
            assertThat(fromNull).isNull();
            Simple fromNullNode = BeanMapper.fromTree(JsonNode.nullNode(), Simple.class);
            assertThat(fromNullNode).isNull();
        }

        @Test
        @DisplayName("继承字段反序列化")
        void inheritance() {
            JsonNode node = JsonNode.object().put("parentField", "p").put("childField", "c");
            Child child = BeanMapper.fromTree(node, Child.class);
            assertThat(child.parentField).isEqualTo("p");
            assertThat(child.childField).isEqualTo("c");
        }

        @Test
        @DisplayName("WRITE_ONLY 字段反序列化，READ_ONLY 不反序列化")
        void access_control() {
            JsonNode node = JsonNode.object()
                    .put("readOnly", "r").put("writeOnly", "w").put("normal", "n");
            WithReadOnly obj = BeanMapper.fromTree(node, WithReadOnly.class);
            assertThat(obj.readOnly).isNull(); // READ_ONLY → not deserialized
            assertThat(obj.writeOnly).isEqualTo("w");
            assertThat(obj.normal).isEqualTo("n");
        }

        @Test
        @DisplayName("Map 类型")
        @SuppressWarnings("unchecked")
        void map_type() {
            JsonNode node = JsonNode.object().put("a", 1);
            Map<String, Object> map = BeanMapper.fromTree(node, Map.class);
            assertThat(map).containsKey("a");
        }

        @Test
        @DisplayName("List 类型")
        void list_type() {
            JsonNode node = JsonNode.array().add(1).add(2);
            List<?> list = BeanMapper.fromTree(node, List.class);
            assertThat(list).hasSize(2);
        }

        @Test
        @DisplayName("Object 类型 → untyped")
        void object_type() {
            JsonNode node = JsonNode.object().put("k", "v");
            Object obj = BeanMapper.fromTree(node, Object.class);
            assertThat(obj).isInstanceOf(Map.class);
        }

        @Test
        @DisplayName("数组类型反序列化")
        void array_type() {
            JsonNode node = JsonNode.array().add(1).add(2).add(3);
            int[] arr = BeanMapper.fromTree(node, int[].class);
            assertThat(arr).containsExactly(1, 2, 3);
        }
    }

    // ==================== Round-Trip Tests ====================

    @Nested
    @DisplayName("往返测试")
    class RoundTripTests {

        @Test
        @DisplayName("POJO → JsonNode → POJO")
        void pojo_round_trip() {
            Simple original = new Simple();
            original.name = "test";
            original.age = 42;
            original.active = true;

            JsonNode node = BeanMapper.toTree(original);
            Simple restored = BeanMapper.fromTree(node, Simple.class);

            assertThat(restored.name).isEqualTo(original.name);
            assertThat(restored.age).isEqualTo(original.age);
            assertThat(restored.active).isEqualTo(original.active);
        }

        @Test
        @DisplayName("Record → JsonNode → Record")
        void record_round_trip() {
            SimpleRecord original = new SimpleRecord("RT", 99);
            JsonNode node = BeanMapper.toTree(original);
            SimpleRecord restored = BeanMapper.fromTree(node, SimpleRecord.class);
            assertThat(restored).isEqualTo(original);
        }
    }
}
