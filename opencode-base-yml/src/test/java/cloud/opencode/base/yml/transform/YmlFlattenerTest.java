package cloud.opencode.base.yml.transform;

import cloud.opencode.base.yml.exception.OpenYmlException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("YmlFlattener")
class YmlFlattenerTest {

    @Nested
    @DisplayName("Flatten")
    class Flatten {

        @Test
        @DisplayName("simple flat map stays the same")
        void simpleFlatMap() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", "app");
            data.put("version", 1);

            Map<String, Object> result = YmlFlattener.flatten(data);

            assertThat(result).containsEntry("name", "app");
            assertThat(result).containsEntry("version", 1);
        }

        @Test
        @DisplayName("nested map flattened to dot-notation")
        void nestedMapFlattened() {
            Map<String, Object> data = Map.of(
                    "server", Map.of("port", 8080, "host", "localhost"));

            Map<String, Object> result = YmlFlattener.flatten(data);

            assertThat(result).containsEntry("server.port", 8080);
            assertThat(result).containsEntry("server.host", "localhost");
        }

        @Test
        @DisplayName("deeply nested map")
        void deeplyNestedMap() {
            Map<String, Object> data = Map.of(
                    "a", Map.of("b", Map.of("c", Map.of("d", 42))));

            Map<String, Object> result = YmlFlattener.flatten(data);

            assertThat(result).hasSize(1);
            assertThat(result).containsEntry("a.b.c.d", 42);
        }

        @Test
        @DisplayName("list flattened with index notation")
        void listWithIndexNotation() {
            Map<String, Object> data = Map.of("items", List.of("a", "b", "c"));

            Map<String, Object> result = YmlFlattener.flatten(data);

            assertThat(result).hasSize(3);
            assertThat(result).containsEntry("items[0]", "a");
            assertThat(result).containsEntry("items[1]", "b");
            assertThat(result).containsEntry("items[2]", "c");
        }

        @Test
        @DisplayName("list of maps flattened correctly")
        void listOfMaps() {
            Map<String, Object> data = Map.of("users",
                    List.of(
                            Map.of("name", "Alice", "age", 30),
                            Map.of("name", "Bob", "age", 25)));

            Map<String, Object> result = YmlFlattener.flatten(data);

            assertThat(result).containsEntry("users[0].name", "Alice");
            assertThat(result).containsEntry("users[0].age", 30);
            assertThat(result).containsEntry("users[1].name", "Bob");
            assertThat(result).containsEntry("users[1].age", 25);
        }

        @Test
        @DisplayName("mixed nested structures")
        void mixedNested() {
            Map<String, Object> data = Map.of(
                    "app", Map.of(
                            "name", "demo",
                            "tags", List.of("web", "api")));

            Map<String, Object> result = YmlFlattener.flatten(data);

            assertThat(result).containsEntry("app.name", "demo");
            assertThat(result).containsEntry("app.tags[0]", "web");
            assertThat(result).containsEntry("app.tags[1]", "api");
        }

        @Test
        @DisplayName("null values preserved")
        void nullValuesPreserved() {
            Map<String, Object> data = new HashMap<>();
            data.put("key", null);
            data.put("other", "value");

            Map<String, Object> result = YmlFlattener.flatten(data);

            assertThat(result).containsEntry("key", null);
            assertThat(result).containsEntry("other", "value");
        }

        @Test
        @DisplayName("empty map becomes leaf value")
        void emptyMapBecomesLeaf() {
            Map<String, Object> data = Map.of("empty", Collections.emptyMap());

            Map<String, Object> result = YmlFlattener.flatten(data);

            assertThat(result).hasSize(1);
            assertThat(result).containsEntry("empty", Collections.emptyMap());
        }

        @Test
        @DisplayName("empty list becomes leaf value")
        void emptyListBecomesLeaf() {
            Map<String, Object> data = Map.of("empty", Collections.emptyList());

            Map<String, Object> result = YmlFlattener.flatten(data);

            assertThat(result).hasSize(1);
            assertThat(result).containsEntry("empty", Collections.emptyList());
        }

        @Test
        @DisplayName("null input returns empty map")
        void nullInput() {
            Map<String, Object> result = YmlFlattener.flatten(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("empty input returns empty map")
        void emptyInput() {
            Map<String, Object> result = YmlFlattener.flatten(Collections.emptyMap());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("result is unmodifiable")
        void resultIsUnmodifiable() {
            Map<String, Object> result = YmlFlattener.flatten(Map.of("a", 1));

            assertThatThrownBy(() -> result.put("b", 2))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("deep nesting beyond limit throws OpenYmlException")
        void deepNestingThrows() {
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> current = data;
            for (int i = 0; i < 55; i++) {
                Map<String, Object> next = new HashMap<>();
                current.put("level", next);
                current = next;
            }
            current.put("value", 1);

            assertThatThrownBy(() -> YmlFlattener.flatten(data))
                    .isInstanceOf(OpenYmlException.class)
                    .hasMessageContaining("maximum depth");
        }
    }

    @Nested
    @DisplayName("Unflatten")
    class Unflatten {

        @Test
        @DisplayName("simple flat keys to nested map")
        void simpleFlatToNested() {
            Map<String, Object> flat = Map.of(
                    "server.port", 8080,
                    "server.host", "localhost");

            Map<String, Object> result = YmlFlattener.unflatten(flat);

            assertThat(result).containsKey("server");
            @SuppressWarnings("unchecked")
            Map<String, Object> server = (Map<String, Object>) result.get("server");
            assertThat(server).containsEntry("port", 8080);
            assertThat(server).containsEntry("host", "localhost");
        }

        @Test
        @DisplayName("deeply nested keys")
        void deeplyNestedKeys() {
            Map<String, Object> flat = Map.of("a.b.c.d", 42);

            Map<String, Object> result = YmlFlattener.unflatten(flat);

            @SuppressWarnings("unchecked")
            Map<String, Object> a = (Map<String, Object>) result.get("a");
            @SuppressWarnings("unchecked")
            Map<String, Object> b = (Map<String, Object>) a.get("b");
            @SuppressWarnings("unchecked")
            Map<String, Object> c = (Map<String, Object>) b.get("c");
            assertThat(c).containsEntry("d", 42);
        }

        @Test
        @DisplayName("array indices create lists")
        void arrayIndicesCreateLists() {
            Map<String, Object> flat = new LinkedHashMap<>();
            flat.put("items[0]", "a");
            flat.put("items[1]", "b");
            flat.put("items[2]", "c");

            Map<String, Object> result = YmlFlattener.unflatten(flat);

            assertThat(result).containsKey("items");
            @SuppressWarnings("unchecked")
            List<Object> items = (List<Object>) result.get("items");
            assertThat(items).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("list of maps unflattened correctly")
        void listOfMapsUnflattened() {
            Map<String, Object> flat = new LinkedHashMap<>();
            flat.put("users[0].name", "Alice");
            flat.put("users[0].age", 30);
            flat.put("users[1].name", "Bob");
            flat.put("users[1].age", 25);

            Map<String, Object> result = YmlFlattener.unflatten(flat);

            @SuppressWarnings("unchecked")
            List<Object> users = (List<Object>) result.get("users");
            assertThat(users).hasSize(2);

            @SuppressWarnings("unchecked")
            Map<String, Object> alice = (Map<String, Object>) users.get(0);
            assertThat(alice).containsEntry("name", "Alice");
            assertThat(alice).containsEntry("age", 30);
        }

        @Test
        @DisplayName("null input returns empty map")
        void nullInput() {
            Map<String, Object> result = YmlFlattener.unflatten(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("empty input returns empty map")
        void emptyInput() {
            Map<String, Object> result = YmlFlattener.unflatten(Collections.emptyMap());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("result is unmodifiable")
        void resultIsUnmodifiable() {
            Map<String, Object> result = YmlFlattener.unflatten(Map.of("a.b", 1));

            assertThatThrownBy(() -> result.put("c", 2))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("null separator throws NullPointerException")
        void nullSeparatorThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> YmlFlattener.unflatten(Map.of("a", 1), null));
        }
    }

    @Nested
    @DisplayName("Round Trip")
    class RoundTrip {

        @Test
        @DisplayName("flatten then unflatten restores simple nested map")
        void simpleRoundTrip() {
            Map<String, Object> original = new LinkedHashMap<>();
            original.put("server", new LinkedHashMap<>(Map.of("port", 8080, "host", "localhost")));
            original.put("app", new LinkedHashMap<>(Map.of("name", "demo")));

            Map<String, Object> flat = YmlFlattener.flatten(original);
            Map<String, Object> restored = YmlFlattener.unflatten(flat);

            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("flatten then unflatten restores deeply nested map")
        void deepRoundTrip() {
            Map<String, Object> original = new LinkedHashMap<>();
            original.put("a", new LinkedHashMap<>(
                    Map.of("b", new LinkedHashMap<>(
                            Map.of("c", 42)))));

            Map<String, Object> flat = YmlFlattener.flatten(original);
            Map<String, Object> restored = YmlFlattener.unflatten(flat);

            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("flatten then unflatten restores list data")
        void listRoundTrip() {
            Map<String, Object> original = new LinkedHashMap<>();
            original.put("items", new ArrayList<>(List.of("a", "b", "c")));

            Map<String, Object> flat = YmlFlattener.flatten(original);
            Map<String, Object> restored = YmlFlattener.unflatten(flat);

            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("flatten then unflatten restores mixed structure")
        void mixedRoundTrip() {
            Map<String, Object> user1 = new LinkedHashMap<>(Map.of("name", "Alice", "age", 30));
            Map<String, Object> user2 = new LinkedHashMap<>(Map.of("name", "Bob", "age", 25));
            Map<String, Object> original = new LinkedHashMap<>();
            original.put("users", new ArrayList<>(List.of(user1, user2)));
            original.put("count", 2);

            Map<String, Object> flat = YmlFlattener.flatten(original);
            Map<String, Object> restored = YmlFlattener.unflatten(flat);

            assertThat(restored).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Custom Separator")
    class CustomSeparator {

        @Test
        @DisplayName("flatten with / separator")
        void flattenWithSlash() {
            Map<String, Object> data = Map.of(
                    "server", Map.of("port", 8080));

            Map<String, Object> result = YmlFlattener.flatten(data, "/");

            assertThat(result).containsEntry("server/port", 8080);
        }

        @Test
        @DisplayName("unflatten with / separator")
        void unflattenWithSlash() {
            Map<String, Object> flat = Map.of("server/port", 8080);

            Map<String, Object> result = YmlFlattener.unflatten(flat, "/");

            @SuppressWarnings("unchecked")
            Map<String, Object> server = (Map<String, Object>) result.get("server");
            assertThat(server).containsEntry("port", 8080);
        }

        @Test
        @DisplayName("round trip with custom separator")
        void roundTripWithCustomSeparator() {
            Map<String, Object> original = new LinkedHashMap<>();
            original.put("a", new LinkedHashMap<>(Map.of("b", new LinkedHashMap<>(Map.of("c", 1)))));

            Map<String, Object> flat = YmlFlattener.flatten(original, "/");
            Map<String, Object> restored = YmlFlattener.unflatten(flat, "/");

            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("flatten with :: separator")
        void flattenWithDoubleColon() {
            Map<String, Object> data = Map.of(
                    "database", Map.of("url", "jdbc:mysql://localhost"));

            Map<String, Object> result = YmlFlattener.flatten(data, "::");

            assertThat(result).containsEntry("database::url", "jdbc:mysql://localhost");
        }

        @Test
        @DisplayName("null separator throws NullPointerException")
        void nullSeparatorThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> YmlFlattener.flatten(Map.of("a", 1), null));
        }
    }
}
