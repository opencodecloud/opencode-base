package cloud.opencode.base.json.adapter;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.stream.JsonReader;
import cloud.opencode.base.json.stream.JsonWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonTypeAdapter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonTypeAdapter 测试")
class JsonTypeAdapterTest {

    @Nested
    @DisplayName("基本接口测试")
    class BasicInterfaceTests {

        @Test
        @DisplayName("实现接口")
        void testImplementInterface() {
            JsonTypeAdapter<String> adapter = new JsonTypeAdapter<>() {
                @Override
                public Class<String> getType() {
                    return String.class;
                }

                @Override
                public JsonNode toJson(String value) {
                    return JsonNode.of(value);
                }

                @Override
                public String fromJson(JsonNode node) {
                    return node.asString();
                }
            };

            assertThat(adapter.getType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("序列化和反序列化")
        void testSerializeDeserialize() {
            JsonTypeAdapter<Integer> adapter = new JsonTypeAdapter<>() {
                @Override
                public Class<Integer> getType() {
                    return Integer.class;
                }

                @Override
                public JsonNode toJson(Integer value) {
                    return JsonNode.of(value);
                }

                @Override
                public Integer fromJson(JsonNode node) {
                    return node.asInt();
                }
            };

            JsonNode json = adapter.toJson(42);
            Integer result = adapter.fromJson(json);

            assertThat(json.asInt()).isEqualTo(42);
            assertThat(result).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("getGenericType默认返回getType")
        void testGetGenericTypeDefault() {
            JsonTypeAdapter<String> adapter = createSimpleAdapter(String.class);

            Type genericType = adapter.getGenericType();

            assertThat(genericType).isEqualTo(String.class);
        }

        @Test
        @DisplayName("supportsStreaming默认返回false")
        void testSupportsStreamingDefault() {
            JsonTypeAdapter<String> adapter = createSimpleAdapter(String.class);

            assertThat(adapter.supportsStreaming()).isFalse();
        }

        @Test
        @DisplayName("handlesNull默认返回false")
        void testHandlesNullDefault() {
            JsonTypeAdapter<String> adapter = createSimpleAdapter(String.class);

            assertThat(adapter.handlesNull()).isFalse();
        }

        @Test
        @DisplayName("write默认抛出异常")
        void testWriteDefault() {
            JsonTypeAdapter<String> adapter = createSimpleAdapter(String.class);

            assertThatThrownBy(() -> adapter.write(null, "test"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Streaming write not supported");
        }

        @Test
        @DisplayName("read默认抛出异常")
        void testReadDefault() {
            JsonTypeAdapter<String> adapter = createSimpleAdapter(String.class);

            assertThatThrownBy(() -> adapter.read(null))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Streaming read not supported");
        }
    }

    @Nested
    @DisplayName("自定义默认方法测试")
    class CustomDefaultMethodTests {

        @Test
        @DisplayName("重写handlesNull")
        void testOverrideHandlesNull() {
            JsonTypeAdapter<String> adapter = new JsonTypeAdapter<>() {
                @Override
                public Class<String> getType() {
                    return String.class;
                }

                @Override
                public JsonNode toJson(String value) {
                    return value == null ? JsonNode.of("NULL") : JsonNode.of(value);
                }

                @Override
                public String fromJson(JsonNode node) {
                    return "NULL".equals(node.asString()) ? null : node.asString();
                }

                @Override
                public boolean handlesNull() {
                    return true;
                }
            };

            assertThat(adapter.handlesNull()).isTrue();
        }

        @Test
        @DisplayName("重写supportsStreaming")
        void testOverrideSupportsStreaming() {
            JsonTypeAdapter<String> adapter = new JsonTypeAdapter<>() {
                @Override
                public Class<String> getType() {
                    return String.class;
                }

                @Override
                public JsonNode toJson(String value) {
                    return JsonNode.of(value);
                }

                @Override
                public String fromJson(JsonNode node) {
                    return node.asString();
                }

                @Override
                public boolean supportsStreaming() {
                    return true;
                }

                @Override
                public void write(JsonWriter writer, String value) {
                    // Custom implementation
                }

                @Override
                public String read(JsonReader reader) {
                    return "read";
                }
            };

            assertThat(adapter.supportsStreaming()).isTrue();
        }

        @Test
        @DisplayName("重写getGenericType")
        void testOverrideGetGenericType() {
            class SpecialType {}
            Type customType = SpecialType.class;

            JsonTypeAdapter<String> adapter = new JsonTypeAdapter<>() {
                @Override
                public Class<String> getType() {
                    return String.class;
                }

                @Override
                public Type getGenericType() {
                    return customType;
                }

                @Override
                public JsonNode toJson(String value) {
                    return JsonNode.of(value);
                }

                @Override
                public String fromJson(JsonNode node) {
                    return node.asString();
                }
            };

            assertThat(adapter.getGenericType()).isEqualTo(customType);
        }
    }

    @Nested
    @DisplayName("of工厂方法测试")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("创建适配器")
        void testOf() {
            JsonTypeAdapter<Integer> adapter = JsonTypeAdapter.of(
                Integer.class,
                JsonNode::of,
                JsonNode::asInt
            );

            assertThat(adapter.getType()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("使用of创建的适配器序列化")
        void testOfSerialize() {
            JsonTypeAdapter<Integer> adapter = JsonTypeAdapter.of(
                Integer.class,
                JsonNode::of,
                JsonNode::asInt
            );

            JsonNode result = adapter.toJson(42);

            assertThat(result.asInt()).isEqualTo(42);
        }

        @Test
        @DisplayName("使用of创建的适配器反序列化")
        void testOfDeserialize() {
            JsonTypeAdapter<Integer> adapter = JsonTypeAdapter.of(
                Integer.class,
                JsonNode::of,
                JsonNode::asInt
            );

            Integer result = adapter.fromJson(JsonNode.of(42));

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("自定义序列化逻辑")
        void testOfCustomLogic() {
            JsonTypeAdapter<String> adapter = JsonTypeAdapter.of(
                String.class,
                s -> JsonNode.of(s.toUpperCase()),
                n -> n.asString().toLowerCase()
            );

            JsonNode json = adapter.toJson("hello");
            String result = adapter.fromJson(json);

            assertThat(json.asString()).isEqualTo("HELLO");
            assertThat(result).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("ofString工厂方法测试")
    class OfStringFactoryMethodTests {

        @Test
        @DisplayName("创建字符串适配器")
        void testOfString() {
            JsonTypeAdapter<Integer> adapter = JsonTypeAdapter.ofString(
                Integer.class,
                Object::toString,
                Integer::parseInt
            );

            assertThat(adapter.getType()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("序列化为字符串")
        void testOfStringSerialize() {
            JsonTypeAdapter<Integer> adapter = JsonTypeAdapter.ofString(
                Integer.class,
                Object::toString,
                Integer::parseInt
            );

            JsonNode result = adapter.toJson(42);

            assertThat(result.isString()).isTrue();
            assertThat(result.asString()).isEqualTo("42");
        }

        @Test
        @DisplayName("从字符串反序列化")
        void testOfStringDeserialize() {
            JsonTypeAdapter<Integer> adapter = JsonTypeAdapter.ofString(
                Integer.class,
                Object::toString,
                Integer::parseInt
            );

            Integer result = adapter.fromJson(JsonNode.of("42"));

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("枚举类型适配")
        void testOfStringEnum() {
            JsonTypeAdapter<TestEnum> adapter = JsonTypeAdapter.ofString(
                TestEnum.class,
                Enum::name,
                TestEnum::valueOf
            );

            JsonNode json = adapter.toJson(TestEnum.VALUE_A);
            TestEnum result = adapter.fromJson(json);

            assertThat(json.asString()).isEqualTo("VALUE_A");
            assertThat(result).isEqualTo(TestEnum.VALUE_A);
        }

        @Test
        @DisplayName("自定义格式")
        void testOfStringCustomFormat() {
            // Custom: serialize as "VALUE-X" format
            JsonTypeAdapter<Integer> adapter = JsonTypeAdapter.ofString(
                Integer.class,
                i -> "VALUE-" + i,
                s -> Integer.parseInt(s.substring(6))
            );

            JsonNode json = adapter.toJson(42);
            Integer result = adapter.fromJson(json);

            assertThat(json.asString()).isEqualTo("VALUE-42");
            assertThat(result).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("复杂类型适配器测试")
    class ComplexTypeAdapterTests {

        @Test
        @DisplayName("对象类型适配器")
        void testObjectAdapter() {
            JsonTypeAdapter<Person> adapter = new JsonTypeAdapter<>() {
                @Override
                public Class<Person> getType() {
                    return Person.class;
                }

                @Override
                public JsonNode toJson(Person value) {
                    return JsonNode.object()
                        .put("name", value.name)
                        .put("age", value.age);
                }

                @Override
                public Person fromJson(JsonNode node) {
                    return new Person(
                        node.get("name").asString(),
                        node.get("age").asInt()
                    );
                }
            };

            Person person = new Person("John", 30);
            JsonNode json = adapter.toJson(person);
            Person result = adapter.fromJson(json);

            assertThat(json.get("name").asString()).isEqualTo("John");
            assertThat(json.get("age").asInt()).isEqualTo(30);
            assertThat(result.name).isEqualTo("John");
            assertThat(result.age).isEqualTo(30);
        }

        @Test
        @DisplayName("嵌套对象适配器")
        void testNestedObjectAdapter() {
            JsonTypeAdapter<Address> addressAdapter = new JsonTypeAdapter<>() {
                @Override
                public Class<Address> getType() {
                    return Address.class;
                }

                @Override
                public JsonNode toJson(Address value) {
                    return JsonNode.object()
                        .put("city", value.city)
                        .put("zip", value.zip);
                }

                @Override
                public Address fromJson(JsonNode node) {
                    return new Address(
                        node.get("city").asString(),
                        node.get("zip").asString()
                    );
                }
            };

            Address address = new Address("NYC", "10001");
            JsonNode json = addressAdapter.toJson(address);
            Address result = addressAdapter.fromJson(json);

            assertThat(result.city).isEqualTo("NYC");
            assertThat(result.zip).isEqualTo("10001");
        }
    }

    // Helper methods and classes
    private <T> JsonTypeAdapter<T> createSimpleAdapter(Class<T> type) {
        return new JsonTypeAdapter<>() {
            @Override
            public Class<T> getType() {
                return type;
            }

            @Override
            public JsonNode toJson(T value) {
                return JsonNode.nullNode();
            }

            @Override
            public T fromJson(JsonNode node) {
                return null;
            }
        };
    }

    enum TestEnum {
        VALUE_A, VALUE_B
    }

    record Person(String name, int age) {}

    record Address(String city, String zip) {}
}
