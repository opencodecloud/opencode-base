package cloud.opencode.base.string.desensitize.handler;

import org.junit.jupiter.api.*;

import java.lang.reflect.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CollectionHandlerTest Tests
 * CollectionHandlerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("CollectionHandler Tests")
class CollectionHandlerTest {

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            Constructor<CollectionHandler> constructor = CollectionHandler.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class);
        }
    }

    @Nested
    @DisplayName("List Handle Tests")
    class ListHandleTests {

        @Test
        @DisplayName("Should mask all list elements")
        void shouldMaskAllListElements() {
            List<String> input = List.of("secret1", "secret2", "secret3");
            List<?> result = CollectionHandler.handle(input);
            assertThat(result).hasSize(3);
            assertThat(result.get(0)).isEqualTo("***");
            assertThat(result.get(1)).isEqualTo("***");
            assertThat(result.get(2)).isEqualTo("***");
        }

        @Test
        @DisplayName("Should return null for null list")
        void shouldReturnNullForNullList() {
            assertThat(CollectionHandler.handle((List<?>) null)).isNull();
        }

        @Test
        @DisplayName("Should return empty list for empty list input")
        void shouldReturnEmptyListForEmptyListInput() {
            List<?> result = CollectionHandler.handle(List.of());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should mask list with mixed types")
        void shouldMaskListWithMixedTypes() {
            List<Object> input = new ArrayList<>();
            input.add("string");
            input.add(123);
            input.add(true);
            List<?> result = CollectionHandler.handle(input);
            assertThat(result).hasSize(3);
            assertThat(result.get(0)).isEqualTo("***");
            assertThat(result.get(1)).isEqualTo("***");
            assertThat(result.get(2)).isEqualTo("***");
        }
    }

    @Nested
    @DisplayName("Set Handle Tests")
    class SetHandleTests {

        @Test
        @DisplayName("Should mask all set elements")
        void shouldMaskAllSetElements() {
            Set<String> input = Set.of("secret1", "secret2", "secret3");
            Set<?> result = CollectionHandler.handle(input);
            assertThat(result).hasSize(1); // All become "***" which deduplicates to 1
            assertThat(result.iterator().next()).isEqualTo("***");
        }

        @Test
        @DisplayName("Should return null for null set")
        void shouldReturnNullForNullSet() {
            assertThat(CollectionHandler.handle((Set<?>) null)).isNull();
        }

        @Test
        @DisplayName("Should return empty set for empty set input")
        void shouldReturnEmptySetForEmptySetInput() {
            Set<?> result = CollectionHandler.handle(Set.of());
            assertThat(result).isEmpty();
        }
    }
}
