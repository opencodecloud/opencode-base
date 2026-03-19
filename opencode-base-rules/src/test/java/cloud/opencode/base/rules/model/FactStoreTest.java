package cloud.opencode.base.rules.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * FactStore Interface Tests
 * FactStore 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("FactStore Interface Tests | FactStore 接口测试")
class FactStoreTest {

    private FactStore factStore;

    @BeforeEach
    void setUp() {
        factStore = new SimpleFactStore();
    }

    @Nested
    @DisplayName("Add Tests | 添加测试")
    class AddTests {

        @Test
        @DisplayName("add fact by type | 按类型添加事实")
        void testAddByType() {
            factStore.add(new Person("John", 25));

            assertThat(factStore.contains(Person.class)).isTrue();
        }

        @Test
        @DisplayName("add named fact | 添加命名事实")
        void testAddNamed() {
            factStore.add("customer", new Person("John", 25));

            assertThat(factStore.contains("customer")).isTrue();
        }

        @Test
        @DisplayName("add multiple facts of same type | 添加同类型多个事实")
        void testAddMultipleSameType() {
            factStore.add(new Person("John", 25));
            factStore.add(new Person("Jane", 30));

            List<Person> persons = factStore.getAll(Person.class);
            assertThat(persons).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Get Tests | 获取测试")
    class GetTests {

        @Test
        @DisplayName("get by type returns Optional | 按类型获取返回 Optional")
        void testGetByType() {
            Person person = new Person("John", 25);
            factStore.add(person);

            Optional<Person> result = factStore.get(Person.class);

            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo("John");
        }

        @Test
        @DisplayName("get by type returns empty for missing | 按类型获取对缺失返回空")
        void testGetByTypeMissing() {
            Optional<Person> result = factStore.get(Person.class);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("get by name returns fact | 按名称获取返回事实")
        void testGetByName() {
            factStore.add("myFact", "value");

            Object result = factStore.get("myFact");

            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("get by name returns null for missing | 按名称获取对缺失返回 null")
        void testGetByNameMissing() {
            Object result = factStore.get("nonexistent");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getAll returns all facts of type | getAll 返回该类型所有事实")
        void testGetAll() {
            factStore.add(new Person("John", 25));
            factStore.add(new Person("Jane", 30));
            factStore.add("other");

            List<Person> persons = factStore.getAll(Person.class);

            assertThat(persons).hasSize(2);
            assertThat(persons).extracting(Person::name)
                    .containsExactlyInAnyOrder("John", "Jane");
        }

        @Test
        @DisplayName("getAll returns empty list for missing type | getAll 对缺失类型返回空列表")
        void testGetAllMissing() {
            List<Person> persons = factStore.getAll(Person.class);
            assertThat(persons).isEmpty();
        }
    }

    @Nested
    @DisplayName("Contains Tests | 包含测试")
    class ContainsTests {

        @Test
        @DisplayName("contains by name | 按名称检查包含")
        void testContainsByName() {
            factStore.add("key", "value");

            assertThat(factStore.contains("key")).isTrue();
            assertThat(factStore.contains("other")).isFalse();
        }

        @Test
        @DisplayName("contains by type | 按类型检查包含")
        void testContainsByType() {
            factStore.add(new Person("John", 25));

            assertThat(factStore.contains(Person.class)).isTrue();
            assertThat(factStore.contains(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Remove Tests | 移除测试")
    class RemoveTests {

        @Test
        @DisplayName("remove by name | 按名称移除")
        void testRemoveByName() {
            factStore.add("key", "value");

            Object removed = factStore.remove("key");

            assertThat(removed).isEqualTo("value");
            assertThat(factStore.contains("key")).isFalse();
        }

        @Test
        @DisplayName("remove by name returns null for missing | 按名称移除对缺失返回 null")
        void testRemoveByNameMissing() {
            Object removed = factStore.remove("nonexistent");
            assertThat(removed).isNull();
        }

        @Test
        @DisplayName("removeAll by type | 按类型移除所有")
        void testRemoveAllByType() {
            factStore.add(new Person("John", 25));
            factStore.add(new Person("Jane", 30));

            List<Person> removed = factStore.removeAll(Person.class);

            assertThat(removed).hasSize(2);
            assertThat(factStore.contains(Person.class)).isFalse();
        }

        @Test
        @DisplayName("removeAll returns empty for missing type | removeAll 对缺失类型返回空列表")
        void testRemoveAllMissing() {
            List<Person> removed = factStore.removeAll(Person.class);
            assertThat(removed).isEmpty();
        }
    }

    @Nested
    @DisplayName("Clear Tests | 清除测试")
    class ClearTests {

        @Test
        @DisplayName("clear removes all facts | clear 移除所有事实")
        void testClear() {
            factStore.add(new Person("John", 25));
            factStore.add("key", "value");

            factStore.clear();

            assertThat(factStore.size()).isEqualTo(0);
            assertThat(factStore.contains(Person.class)).isFalse();
            assertThat(factStore.contains("key")).isFalse();
        }
    }

    @Nested
    @DisplayName("Size Tests | 大小测试")
    class SizeTests {

        @Test
        @DisplayName("size returns correct count | size 返回正确数量")
        void testSize() {
            assertThat(factStore.size()).isEqualTo(0);

            factStore.add(new Person("John", 25));
            assertThat(factStore.size()).isEqualTo(1);

            factStore.add("key", "value");
            assertThat(factStore.size()).isEqualTo(2);
        }
    }

    // Helper record
    private record Person(String name, int age) {}

    // Simple implementation for testing
    private static class SimpleFactStore implements FactStore {
        private final java.util.Map<String, Object> namedFacts = new java.util.HashMap<>();
        private final java.util.List<Object> typedFacts = new java.util.ArrayList<>();

        @Override
        public void add(Object fact) {
            typedFacts.add(fact);
        }

        @Override
        public void add(String name, Object fact) {
            namedFacts.put(name, fact);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> get(Class<T> type) {
            return typedFacts.stream()
                    .filter(type::isInstance)
                    .map(f -> (T) f)
                    .findFirst();
        }

        @Override
        public Object get(String name) {
            return namedFacts.get(name);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> List<T> getAll(Class<T> type) {
            return typedFacts.stream()
                    .filter(type::isInstance)
                    .map(f -> (T) f)
                    .toList();
        }

        @Override
        public boolean contains(String name) {
            return namedFacts.containsKey(name);
        }

        @Override
        public boolean contains(Class<?> type) {
            return typedFacts.stream().anyMatch(type::isInstance);
        }

        @Override
        public Object remove(String name) {
            return namedFacts.remove(name);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> List<T> removeAll(Class<T> type) {
            List<T> removed = getAll(type);
            typedFacts.removeIf(type::isInstance);
            return removed;
        }

        @Override
        public void clear() {
            namedFacts.clear();
            typedFacts.clear();
        }

        @Override
        public int size() {
            return namedFacts.size() + typedFacts.size();
        }
    }
}
