package cloud.opencode.base.yml.merge;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for MergeStrategy enum
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("MergeStrategy Tests")
class MergeStrategyTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("should have OVERRIDE strategy")
        void shouldHaveOverrideStrategy() {
            assertThat(MergeStrategy.OVERRIDE).isNotNull();
            assertThat(MergeStrategy.OVERRIDE.name()).isEqualTo("OVERRIDE");
        }

        @Test
        @DisplayName("should have KEEP_FIRST strategy")
        void shouldHaveKeepFirstStrategy() {
            assertThat(MergeStrategy.KEEP_FIRST).isNotNull();
            assertThat(MergeStrategy.KEEP_FIRST.name()).isEqualTo("KEEP_FIRST");
        }

        @Test
        @DisplayName("should have DEEP_MERGE strategy")
        void shouldHaveDeepMergeStrategy() {
            assertThat(MergeStrategy.DEEP_MERGE).isNotNull();
            assertThat(MergeStrategy.DEEP_MERGE.name()).isEqualTo("DEEP_MERGE");
        }

        @Test
        @DisplayName("should have APPEND_LISTS strategy")
        void shouldHaveAppendListsStrategy() {
            assertThat(MergeStrategy.APPEND_LISTS).isNotNull();
            assertThat(MergeStrategy.APPEND_LISTS.name()).isEqualTo("APPEND_LISTS");
        }

        @Test
        @DisplayName("should have MERGE_LISTS_UNIQUE strategy")
        void shouldHaveMergeListsUniqueStrategy() {
            assertThat(MergeStrategy.MERGE_LISTS_UNIQUE).isNotNull();
            assertThat(MergeStrategy.MERGE_LISTS_UNIQUE.name()).isEqualTo("MERGE_LISTS_UNIQUE");
        }

        @Test
        @DisplayName("should have FAIL_ON_CONFLICT strategy")
        void shouldHaveFailOnConflictStrategy() {
            assertThat(MergeStrategy.FAIL_ON_CONFLICT).isNotNull();
            assertThat(MergeStrategy.FAIL_ON_CONFLICT.name()).isEqualTo("FAIL_ON_CONFLICT");
        }

        @Test
        @DisplayName("should have exactly six strategies")
        void shouldHaveExactlySixStrategies() {
            assertThat(MergeStrategy.values()).hasSize(6);
        }
    }

    @Nested
    @DisplayName("Ordinal Tests")
    class OrdinalTests {

        @Test
        @DisplayName("OVERRIDE should have ordinal 0")
        void overrideShouldHaveOrdinalZero() {
            assertThat(MergeStrategy.OVERRIDE.ordinal()).isEqualTo(0);
        }

        @Test
        @DisplayName("KEEP_FIRST should have ordinal 1")
        void keepFirstShouldHaveOrdinalOne() {
            assertThat(MergeStrategy.KEEP_FIRST.ordinal()).isEqualTo(1);
        }

        @Test
        @DisplayName("DEEP_MERGE should have ordinal 2")
        void deepMergeShouldHaveOrdinalTwo() {
            assertThat(MergeStrategy.DEEP_MERGE.ordinal()).isEqualTo(2);
        }

        @Test
        @DisplayName("APPEND_LISTS should have ordinal 3")
        void appendListsShouldHaveOrdinalThree() {
            assertThat(MergeStrategy.APPEND_LISTS.ordinal()).isEqualTo(3);
        }

        @Test
        @DisplayName("MERGE_LISTS_UNIQUE should have ordinal 4")
        void mergeListsUniqueShouldHaveOrdinalFour() {
            assertThat(MergeStrategy.MERGE_LISTS_UNIQUE.ordinal()).isEqualTo(4);
        }

        @Test
        @DisplayName("FAIL_ON_CONFLICT should have ordinal 5")
        void failOnConflictShouldHaveOrdinalFive() {
            assertThat(MergeStrategy.FAIL_ON_CONFLICT.ordinal()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("ValueOf Tests")
    class ValueOfTests {

        @Test
        @DisplayName("valueOf should return OVERRIDE for 'OVERRIDE'")
        void valueOfShouldReturnOverride() {
            assertThat(MergeStrategy.valueOf("OVERRIDE")).isEqualTo(MergeStrategy.OVERRIDE);
        }

        @Test
        @DisplayName("valueOf should return KEEP_FIRST for 'KEEP_FIRST'")
        void valueOfShouldReturnKeepFirst() {
            assertThat(MergeStrategy.valueOf("KEEP_FIRST")).isEqualTo(MergeStrategy.KEEP_FIRST);
        }

        @Test
        @DisplayName("valueOf should return DEEP_MERGE for 'DEEP_MERGE'")
        void valueOfShouldReturnDeepMerge() {
            assertThat(MergeStrategy.valueOf("DEEP_MERGE")).isEqualTo(MergeStrategy.DEEP_MERGE);
        }

        @Test
        @DisplayName("valueOf should return APPEND_LISTS for 'APPEND_LISTS'")
        void valueOfShouldReturnAppendLists() {
            assertThat(MergeStrategy.valueOf("APPEND_LISTS")).isEqualTo(MergeStrategy.APPEND_LISTS);
        }

        @Test
        @DisplayName("valueOf should return MERGE_LISTS_UNIQUE for 'MERGE_LISTS_UNIQUE'")
        void valueOfShouldReturnMergeListsUnique() {
            assertThat(MergeStrategy.valueOf("MERGE_LISTS_UNIQUE")).isEqualTo(MergeStrategy.MERGE_LISTS_UNIQUE);
        }

        @Test
        @DisplayName("valueOf should return FAIL_ON_CONFLICT for 'FAIL_ON_CONFLICT'")
        void valueOfShouldReturnFailOnConflict() {
            assertThat(MergeStrategy.valueOf("FAIL_ON_CONFLICT")).isEqualTo(MergeStrategy.FAIL_ON_CONFLICT);
        }

        @Test
        @DisplayName("valueOf should throw for invalid name")
        void valueOfShouldThrowForInvalidName() {
            assertThatThrownBy(() -> MergeStrategy.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("valueOf should throw for null")
        void valueOfShouldThrowForNull() {
            assertThatThrownBy(() -> MergeStrategy.valueOf(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Values Array Tests")
    class ValuesArrayTests {

        @Test
        @DisplayName("values should return all strategies in order")
        void valuesShouldReturnAllStrategiesInOrder() {
            MergeStrategy[] strategies = MergeStrategy.values();

            assertThat(strategies).containsExactly(
                MergeStrategy.OVERRIDE,
                MergeStrategy.KEEP_FIRST,
                MergeStrategy.DEEP_MERGE,
                MergeStrategy.APPEND_LISTS,
                MergeStrategy.MERGE_LISTS_UNIQUE,
                MergeStrategy.FAIL_ON_CONFLICT
            );
        }

        @Test
        @DisplayName("values should return new array each time")
        void valuesShouldReturnNewArrayEachTime() {
            MergeStrategy[] first = MergeStrategy.values();
            MergeStrategy[] second = MergeStrategy.values();

            assertThat(first).isNotSameAs(second);
            assertThat(first).containsExactly(second);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should return strategy name")
        void toStringShouldReturnStrategyName() {
            assertThat(MergeStrategy.OVERRIDE.toString()).isEqualTo("OVERRIDE");
            assertThat(MergeStrategy.KEEP_FIRST.toString()).isEqualTo("KEEP_FIRST");
            assertThat(MergeStrategy.DEEP_MERGE.toString()).isEqualTo("DEEP_MERGE");
            assertThat(MergeStrategy.APPEND_LISTS.toString()).isEqualTo("APPEND_LISTS");
            assertThat(MergeStrategy.MERGE_LISTS_UNIQUE.toString()).isEqualTo("MERGE_LISTS_UNIQUE");
            assertThat(MergeStrategy.FAIL_ON_CONFLICT.toString()).isEqualTo("FAIL_ON_CONFLICT");
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("same strategy should be equal")
        void sameStrategyShouldBeEqual() {
            assertThat(MergeStrategy.DEEP_MERGE).isEqualTo(MergeStrategy.DEEP_MERGE);
        }

        @Test
        @DisplayName("different strategies should not be equal")
        void differentStrategiesShouldNotBeEqual() {
            assertThat(MergeStrategy.OVERRIDE).isNotEqualTo(MergeStrategy.KEEP_FIRST);
            assertThat(MergeStrategy.DEEP_MERGE).isNotEqualTo(MergeStrategy.APPEND_LISTS);
        }

        @Test
        @DisplayName("strategy should not equal null")
        void strategyShouldNotEqualNull() {
            assertThat(MergeStrategy.OVERRIDE).isNotEqualTo(null);
        }

        @Test
        @DisplayName("strategy should not equal other objects")
        void strategyShouldNotEqualOtherObjects() {
            assertThat(MergeStrategy.OVERRIDE).isNotEqualTo("OVERRIDE");
            assertThat(MergeStrategy.OVERRIDE).isNotEqualTo(0);
        }
    }

    @Nested
    @DisplayName("HashCode Tests")
    class HashCodeTests {

        @Test
        @DisplayName("same strategy should have same hashCode")
        void sameStrategyShouldHaveSameHashCode() {
            assertThat(MergeStrategy.DEEP_MERGE.hashCode())
                .isEqualTo(MergeStrategy.DEEP_MERGE.hashCode());
        }

        @Test
        @DisplayName("different strategies should have different hashCodes")
        void differentStrategiesShouldHaveDifferentHashCodes() {
            assertThat(MergeStrategy.OVERRIDE.hashCode())
                .isNotEqualTo(MergeStrategy.KEEP_FIRST.hashCode());
        }
    }

    @Nested
    @DisplayName("Enum Class Tests")
    class EnumClassTests {

        @Test
        @DisplayName("should be an Enum")
        void shouldBeAnEnum() {
            assertThat(MergeStrategy.DEEP_MERGE).isInstanceOf(Enum.class);
        }

        @Test
        @DisplayName("declaringClass should be MergeStrategy")
        void declaringClassShouldBeMergeStrategy() {
            assertThat(MergeStrategy.OVERRIDE.getDeclaringClass()).isEqualTo(MergeStrategy.class);
        }
    }
}
