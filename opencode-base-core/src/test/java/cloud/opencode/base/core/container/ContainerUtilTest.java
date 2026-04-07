package cloud.opencode.base.core.container;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ContainerUtil}.
 */
@DisplayName("ContainerUtil Tests")
class ContainerUtilTest {

    @Nested
    @DisplayName("size()")
    class SizeTests {

        @Test
        @DisplayName("null returns 0")
        void nullReturnsZero() {
            assertThat(ContainerUtil.size(null)).isZero();
        }

        @Test
        @DisplayName("Collection size")
        void collectionSize() {
            assertThat(ContainerUtil.size(List.of(1, 2, 3))).isEqualTo(3);
            assertThat(ContainerUtil.size(List.of())).isZero();
            assertThat(ContainerUtil.size(Set.of("a", "b"))).isEqualTo(2);
        }

        @Test
        @DisplayName("Map size")
        void mapSize() {
            assertThat(ContainerUtil.size(Map.of("a", 1, "b", 2))).isEqualTo(2);
            assertThat(ContainerUtil.size(Map.of())).isZero();
        }

        @Test
        @DisplayName("CharSequence length")
        void charSequenceLength() {
            assertThat(ContainerUtil.size("hello")).isEqualTo(5);
            assertThat(ContainerUtil.size("")).isZero();
            assertThat(ContainerUtil.size(new StringBuilder("abc"))).isEqualTo(3);
        }

        @Test
        @DisplayName("Array length")
        void arrayLength() {
            assertThat(ContainerUtil.size(new int[]{1, 2, 3})).isEqualTo(3);
            assertThat(ContainerUtil.size(new String[]{"a"})).isEqualTo(1);
            assertThat(ContainerUtil.size(new Object[0])).isZero();
        }

        @Test
        @DisplayName("Optional size")
        void optionalSize() {
            assertThat(ContainerUtil.size(Optional.of("x"))).isEqualTo(1);
            assertThat(ContainerUtil.size(Optional.empty())).isZero();
        }

        @Test
        @DisplayName("unsupported type returns -1")
        void unsupportedReturnsNegative() {
            assertThat(ContainerUtil.size(42)).isEqualTo(-1);
            assertThat(ContainerUtil.size(new Object())).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("isEmpty()")
    class IsEmptyTests {

        @Test
        @DisplayName("null is empty")
        void nullIsEmpty() {
            assertThat(ContainerUtil.isEmpty(null)).isTrue();
        }

        @Test
        @DisplayName("empty Collection is empty")
        void emptyCollection() {
            assertThat(ContainerUtil.isEmpty(List.of())).isTrue();
            assertThat(ContainerUtil.isEmpty(List.of(1))).isFalse();
        }

        @Test
        @DisplayName("empty Map is empty")
        void emptyMap() {
            assertThat(ContainerUtil.isEmpty(Map.of())).isTrue();
            assertThat(ContainerUtil.isEmpty(Map.of("k", "v"))).isFalse();
        }

        @Test
        @DisplayName("empty CharSequence is empty")
        void emptyCharSequence() {
            assertThat(ContainerUtil.isEmpty("")).isTrue();
            assertThat(ContainerUtil.isEmpty("x")).isFalse();
        }

        @Test
        @DisplayName("empty array is empty")
        void emptyArray() {
            assertThat(ContainerUtil.isEmpty(new int[0])).isTrue();
            assertThat(ContainerUtil.isEmpty(new int[]{1})).isFalse();
        }

        @Test
        @DisplayName("empty Optional is empty")
        void emptyOptional() {
            assertThat(ContainerUtil.isEmpty(Optional.empty())).isTrue();
            assertThat(ContainerUtil.isEmpty(Optional.of("a"))).isFalse();
        }

        @Test
        @DisplayName("unsupported type is treated as empty")
        void unsupportedIsEmpty() {
            assertThat(ContainerUtil.isEmpty(42)).isTrue();
        }
    }

    @Nested
    @DisplayName("isNotEmpty()")
    class IsNotEmptyTests {

        @Test
        @DisplayName("null is not not-empty")
        void nullIsNotNotEmpty() {
            assertThat(ContainerUtil.isNotEmpty(null)).isFalse();
        }

        @Test
        @DisplayName("non-empty collection is not-empty")
        void nonEmptyCollection() {
            assertThat(ContainerUtil.isNotEmpty(List.of(1))).isTrue();
        }

        @Test
        @DisplayName("empty collection is not not-empty")
        void emptyCollectionIsNotNotEmpty() {
            assertThat(ContainerUtil.isNotEmpty(List.of())).isFalse();
        }

        @Test
        @DisplayName("non-empty string is not-empty")
        void nonEmptyString() {
            assertThat(ContainerUtil.isNotEmpty("abc")).isTrue();
        }

        @Test
        @DisplayName("non-empty map is not-empty")
        void nonEmptyMap() {
            assertThat(ContainerUtil.isNotEmpty(Map.of("a", 1))).isTrue();
        }

        @Test
        @DisplayName("non-empty array is not-empty")
        void nonEmptyArray() {
            assertThat(ContainerUtil.isNotEmpty(new String[]{"a"})).isTrue();
        }

        @Test
        @DisplayName("present Optional is not-empty")
        void presentOptional() {
            assertThat(ContainerUtil.isNotEmpty(Optional.of("x"))).isTrue();
        }
    }
}
