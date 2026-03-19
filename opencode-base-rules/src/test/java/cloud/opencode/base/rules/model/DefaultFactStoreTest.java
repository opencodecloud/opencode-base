package cloud.opencode.base.rules.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultFactStore Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("DefaultFactStore Tests")
class DefaultFactStoreTest {

    private DefaultFactStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultFactStore();
    }

    @Nested
    @DisplayName("Add Operations Tests")
    class AddOperationsTests {

        @Test
        @DisplayName("add(Object) should store typed fact")
        void addShouldStoreTypedFact() {
            store.add("test-string");
            assertThat(store.get(String.class)).contains("test-string");
        }

        @Test
        @DisplayName("add(Object) should ignore null")
        void addShouldIgnoreNull() {
            store.add(null);
            assertThat(store.size()).isZero();
        }

        @Test
        @DisplayName("add(String, Object) should store named fact")
        void addNamedShouldStoreNamedFact() {
            store.add("myFact", "value");
            assertThat(store.get("myFact")).isEqualTo("value");
        }

        @Test
        @DisplayName("add(String, Object) should ignore null name")
        void addNamedShouldIgnoreNullName() {
            store.add(null, "value");
            assertThat(store.size()).isZero();
        }

        @Test
        @DisplayName("add(String, Object) should ignore null fact")
        void addNamedShouldIgnoreNullFact() {
            store.add("name", null);
            assertThat(store.size()).isZero();
        }
    }

    @Nested
    @DisplayName("Get Operations Tests")
    class GetOperationsTests {

        @Test
        @DisplayName("get(Class) should return first fact of type")
        void getByClassShouldReturnFirstFactOfType() {
            store.add("first");
            store.add("second");
            store.add(100);

            Optional<String> result = store.get(String.class);
            assertThat(result).contains("first");
        }

        @Test
        @DisplayName("get(Class) should return empty for missing type")
        void getByClassShouldReturnEmptyForMissingType() {
            store.add("string");
            assertThat(store.get(Integer.class)).isEmpty();
        }

        @Test
        @DisplayName("get(String) should return named fact")
        void getByNameShouldReturnNamedFact() {
            store.add("name", "value");
            assertThat(store.get("name")).isEqualTo("value");
        }

        @Test
        @DisplayName("get(String) should return null for missing name")
        void getByNameShouldReturnNullForMissingName() {
            assertThat(store.get("missing")).isNull();
        }

        @Test
        @DisplayName("getAll(Class) should return all facts of type")
        void getAllShouldReturnAllFactsOfType() {
            store.add("first");
            store.add("second");
            store.add(100);
            store.add("third");

            List<String> strings = store.getAll(String.class);
            assertThat(strings).containsExactly("first", "second", "third");
        }

        @Test
        @DisplayName("getAll(Class) should return empty list for missing type")
        void getAllShouldReturnEmptyListForMissingType() {
            store.add("string");
            assertThat(store.getAll(Double.class)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Contains Operations Tests")
    class ContainsOperationsTests {

        @Test
        @DisplayName("contains(String) should return true for existing name")
        void containsByNameShouldReturnTrueForExistingName() {
            store.add("name", "value");
            assertThat(store.contains("name")).isTrue();
        }

        @Test
        @DisplayName("contains(String) should return false for missing name")
        void containsByNameShouldReturnFalseForMissingName() {
            assertThat(store.contains("missing")).isFalse();
        }

        @Test
        @DisplayName("contains(Class) should return true for existing type")
        void containsByClassShouldReturnTrueForExistingType() {
            store.add("string");
            assertThat(store.contains(String.class)).isTrue();
        }

        @Test
        @DisplayName("contains(Class) should return false for missing type")
        void containsByClassShouldReturnFalseForMissingType() {
            store.add("string");
            assertThat(store.contains(Integer.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Remove Operations Tests")
    class RemoveOperationsTests {

        @Test
        @DisplayName("remove(String) should remove and return named fact")
        void removeShouldRemoveAndReturnNamedFact() {
            store.add("name", "value");
            Object removed = store.remove("name");

            assertThat(removed).isEqualTo("value");
            assertThat(store.contains("name")).isFalse();
        }

        @Test
        @DisplayName("remove(String) should return null for missing name")
        void removeShouldReturnNullForMissingName() {
            assertThat(store.remove("missing")).isNull();
        }

        @Test
        @DisplayName("removeAll(Class) should remove and return all facts of type")
        void removeAllShouldRemoveAndReturnAllFactsOfType() {
            store.add("first");
            store.add("second");
            store.add(100);

            List<String> removed = store.removeAll(String.class);
            assertThat(removed).containsExactly("first", "second");
            assertThat(store.contains(String.class)).isFalse();
            assertThat(store.contains(Integer.class)).isTrue();
        }

        @Test
        @DisplayName("removeAll(Class) should return empty list for missing type")
        void removeAllShouldReturnEmptyListForMissingType() {
            store.add("string");
            assertThat(store.removeAll(Double.class)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Clear and Size Tests")
    class ClearAndSizeTests {

        @Test
        @DisplayName("size() should return total count")
        void sizeShouldReturnTotalCount() {
            store.add("typed");
            store.add("name1", "value1");
            store.add("name2", "value2");

            assertThat(store.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("clear() should remove all facts")
        void clearShouldRemoveAllFacts() {
            store.add("typed");
            store.add("name", "value");
            store.clear();

            assertThat(store.size()).isZero();
        }
    }

    @Nested
    @DisplayName("Type Hierarchy Tests")
    class TypeHierarchyTests {

        @Test
        @DisplayName("get(Class) should work with parent types")
        void getShouldWorkWithParentTypes() {
            store.add("string-value");
            assertThat(store.get(CharSequence.class)).contains("string-value");
        }

        @Test
        @DisplayName("getAll(Class) should work with parent types")
        void getAllShouldWorkWithParentTypes() {
            store.add(100);
            store.add(200L);

            List<Number> numbers = store.getAll(Number.class);
            assertThat(numbers).hasSize(2);
        }

        @Test
        @DisplayName("contains(Class) should work with parent types")
        void containsShouldWorkWithParentTypes() {
            store.add("string");
            assertThat(store.contains(CharSequence.class)).isTrue();
        }
    }
}
