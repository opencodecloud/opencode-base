package cloud.opencode.base.yml.merge;

import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for YmlMerger utility class
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlMerger Tests")
class YmlMergerTest {

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("constructor should throw AssertionError")
        void constructorShouldThrowAssertionError() throws Exception {
            Constructor<YmlMerger> constructor = YmlMerger.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(AssertionError.class)
                .hasRootCauseMessage("Utility class - do not instantiate");
        }
    }

    @Nested
    @DisplayName("Merge with Default Strategy Tests")
    class MergeDefaultStrategyTests {

        @Test
        @DisplayName("merge should use DEEP_MERGE as default strategy")
        void mergeShouldUseDeepMergeAsDefault() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("nested", new LinkedHashMap<>(Map.of("a", "1")));

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("nested", new LinkedHashMap<>(Map.of("b", "2")));

            Map<String, Object> result = YmlMerger.merge(base, overlay);

            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) result.get("nested");
            assertThat(nested).containsEntry("a", "1");
            assertThat(nested).containsEntry("b", "2");
        }

        @Test
        @DisplayName("merge should combine non-overlapping keys")
        void mergeShouldCombineNonOverlappingKeys() {
            Map<String, Object> base = Map.of("a", "1");
            Map<String, Object> overlay = Map.of("b", "2");

            Map<String, Object> result = YmlMerger.merge(base, overlay);

            assertThat(result).containsEntry("a", "1");
            assertThat(result).containsEntry("b", "2");
        }

        @Test
        @DisplayName("merge should override scalar values")
        void mergeShouldOverrideScalarValues() {
            Map<String, Object> base = Map.of("key", "base-value");
            Map<String, Object> overlay = Map.of("key", "overlay-value");

            Map<String, Object> result = YmlMerger.merge(base, overlay);

            assertThat(result).containsEntry("key", "overlay-value");
        }
    }

    @Nested
    @DisplayName("Null Handling Tests")
    class NullHandlingTests {

        @Test
        @DisplayName("merge with null base should return copy of overlay")
        void mergeWithNullBaseShouldReturnCopyOfOverlay() {
            Map<String, Object> overlay = Map.of("key", "value");

            Map<String, Object> result = YmlMerger.merge(null, overlay);

            assertThat(result).containsEntry("key", "value");
            assertThat(result).isNotSameAs(overlay);
        }

        @Test
        @DisplayName("merge with null overlay should return copy of base")
        void mergeWithNullOverlayShouldReturnCopyOfBase() {
            Map<String, Object> base = Map.of("key", "value");

            Map<String, Object> result = YmlMerger.merge(base, null);

            assertThat(result).containsEntry("key", "value");
            assertThat(result).isNotSameAs(base);
        }

        @Test
        @DisplayName("merge with both null should return empty map")
        void mergeWithBothNullShouldReturnEmptyMap() {
            Map<String, Object> result = YmlMerger.merge(null, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("merge should keep base value when overlay has null value (all strategies)")
        void mergeShouldKeepBaseValueWhenOverlayHasNullValue() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("key", "value");

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("key", null);

            // All strategies treat null overlay values the same - keep base value
            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.OVERRIDE);

            assertThat(result).containsKey("key");
            assertThat(result.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("merge should keep base value when overlay value is null")
        void mergeShouldKeepBaseValueWhenOverlayValueIsNull() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("key", "base-value");

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("key", null);

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.DEEP_MERGE);

            assertThat(result).containsEntry("key", "base-value");
        }

        @Test
        @DisplayName("merge should use overlay value when base value is null")
        void mergeShouldUseOverlayValueWhenBaseValueIsNull() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("key", null);

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("key", "overlay-value");

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.DEEP_MERGE);

            assertThat(result).containsEntry("key", "overlay-value");
        }
    }

    @Nested
    @DisplayName("OVERRIDE Strategy Tests")
    class OverrideStrategyTests {

        @Test
        @DisplayName("OVERRIDE should replace base value with overlay value")
        void overrideShouldReplaceBaseWithOverlay() {
            Map<String, Object> base = Map.of("key", "base");
            Map<String, Object> overlay = Map.of("key", "overlay");

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.OVERRIDE);

            assertThat(result).containsEntry("key", "overlay");
        }

        @Test
        @DisplayName("OVERRIDE should replace nested maps entirely")
        void overrideShouldReplaceNestedMapsEntirely() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("nested", new LinkedHashMap<>(Map.of("a", "1", "b", "2")));

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("nested", new LinkedHashMap<>(Map.of("c", "3")));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.OVERRIDE);

            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) result.get("nested");
            assertThat(nested).containsOnlyKeys("c");
            assertThat(nested).containsEntry("c", "3");
        }

        @Test
        @DisplayName("OVERRIDE should replace lists entirely")
        void overrideShouldReplaceListsEntirely() {
            Map<String, Object> base = Map.of("list", List.of("a", "b"));
            Map<String, Object> overlay = Map.of("list", List.of("c"));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.OVERRIDE);

            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) result.get("list");
            assertThat(list).containsExactly("c");
        }

        @Test
        @DisplayName("OVERRIDE should preserve keys not in overlay")
        void overrideShouldPreserveKeysNotInOverlay() {
            Map<String, Object> base = Map.of("a", "1", "b", "2");
            Map<String, Object> overlay = Map.of("b", "modified");

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.OVERRIDE);

            assertThat(result).containsEntry("a", "1");
            assertThat(result).containsEntry("b", "modified");
        }
    }

    @Nested
    @DisplayName("KEEP_FIRST Strategy Tests")
    class KeepFirstStrategyTests {

        @Test
        @DisplayName("KEEP_FIRST should preserve base value over overlay")
        void keepFirstShouldPreserveBaseValue() {
            Map<String, Object> base = Map.of("key", "base");
            Map<String, Object> overlay = Map.of("key", "overlay");

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.KEEP_FIRST);

            assertThat(result).containsEntry("key", "base");
        }

        @Test
        @DisplayName("KEEP_FIRST should add keys from overlay not in base")
        void keepFirstShouldAddNewKeysFromOverlay() {
            Map<String, Object> base = Map.of("a", "1");
            Map<String, Object> overlay = Map.of("a", "modified", "b", "2");

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.KEEP_FIRST);

            assertThat(result).containsEntry("a", "1");
            assertThat(result).containsEntry("b", "2");
        }

        @Test
        @DisplayName("KEEP_FIRST should preserve base nested maps")
        void keepFirstShouldPreserveBaseNestedMaps() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("nested", new LinkedHashMap<>(Map.of("a", "base-value")));

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("nested", new LinkedHashMap<>(Map.of("a", "overlay-value")));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.KEEP_FIRST);

            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) result.get("nested");
            assertThat(nested).containsEntry("a", "base-value");
        }

        @Test
        @DisplayName("KEEP_FIRST should preserve base lists")
        void keepFirstShouldPreserveBaseLists() {
            Map<String, Object> base = Map.of("list", List.of("a", "b"));
            Map<String, Object> overlay = Map.of("list", List.of("c", "d"));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.KEEP_FIRST);

            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) result.get("list");
            assertThat(list).containsExactly("a", "b");
        }
    }

    @Nested
    @DisplayName("DEEP_MERGE Strategy Tests")
    class DeepMergeStrategyTests {

        @Test
        @DisplayName("DEEP_MERGE should recursively merge nested maps")
        void deepMergeShouldRecursivelyMergeNestedMaps() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("level1", new LinkedHashMap<>(Map.of("a", "1", "b", "2")));

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("level1", new LinkedHashMap<>(Map.of("b", "modified", "c", "3")));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.DEEP_MERGE);

            @SuppressWarnings("unchecked")
            Map<String, Object> level1 = (Map<String, Object>) result.get("level1");
            assertThat(level1).containsEntry("a", "1");
            assertThat(level1).containsEntry("b", "modified");
            assertThat(level1).containsEntry("c", "3");
        }

        @Test
        @DisplayName("DEEP_MERGE should merge deeply nested structures")
        void deepMergeShouldMergeDeeplyNestedStructures() {
            Map<String, Object> base = new LinkedHashMap<>();
            Map<String, Object> level2Base = new LinkedHashMap<>(Map.of("deep", "base-deep"));
            Map<String, Object> level1Base = new LinkedHashMap<>(Map.of("level2", level2Base));
            base.put("level1", level1Base);

            Map<String, Object> overlay = new LinkedHashMap<>();
            Map<String, Object> level2Overlay = new LinkedHashMap<>(Map.of("extra", "overlay-extra"));
            Map<String, Object> level1Overlay = new LinkedHashMap<>(Map.of("level2", level2Overlay));
            overlay.put("level1", level1Overlay);

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.DEEP_MERGE);

            @SuppressWarnings("unchecked")
            Map<String, Object> level1 = (Map<String, Object>) result.get("level1");
            @SuppressWarnings("unchecked")
            Map<String, Object> level2 = (Map<String, Object>) level1.get("level2");

            assertThat(level2).containsEntry("deep", "base-deep");
            assertThat(level2).containsEntry("extra", "overlay-extra");
        }

        @Test
        @DisplayName("DEEP_MERGE should replace non-map values with overlay")
        void deepMergeShouldReplaceNonMapValues() {
            Map<String, Object> base = Map.of("scalar", "base-value");
            Map<String, Object> overlay = Map.of("scalar", "overlay-value");

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.DEEP_MERGE);

            assertThat(result).containsEntry("scalar", "overlay-value");
        }

        @Test
        @DisplayName("DEEP_MERGE should replace lists with overlay")
        void deepMergeShouldReplaceListsWithOverlay() {
            Map<String, Object> base = Map.of("list", List.of("a", "b"));
            Map<String, Object> overlay = Map.of("list", List.of("c"));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.DEEP_MERGE);

            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) result.get("list");
            assertThat(list).containsExactly("c");
        }

        @Test
        @DisplayName("DEEP_MERGE should handle mixed types by using overlay")
        void deepMergeShouldHandleMixedTypes() {
            Map<String, Object> base = Map.of("key", "string-value");
            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("key", new LinkedHashMap<>(Map.of("nested", "value")));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.DEEP_MERGE);

            assertThat(result.get("key")).isInstanceOf(Map.class);
        }
    }

    @Nested
    @DisplayName("APPEND_LISTS Strategy Tests")
    class AppendListsStrategyTests {

        @Test
        @DisplayName("APPEND_LISTS should append overlay list to base list")
        void appendListsShouldAppendLists() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("list", new ArrayList<>(List.of("a", "b")));

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("list", new ArrayList<>(List.of("c", "d")));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.APPEND_LISTS);

            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) result.get("list");
            assertThat(list).containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("APPEND_LISTS should allow duplicates")
        void appendListsShouldAllowDuplicates() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("list", new ArrayList<>(List.of("a", "b")));

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("list", new ArrayList<>(List.of("b", "c")));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.APPEND_LISTS);

            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) result.get("list");
            assertThat(list).containsExactly("a", "b", "b", "c");
        }

        @Test
        @DisplayName("APPEND_LISTS should deep merge nested maps")
        void appendListsShouldDeepMergeNestedMaps() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("nested", new LinkedHashMap<>(Map.of("a", "1")));

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("nested", new LinkedHashMap<>(Map.of("b", "2")));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.APPEND_LISTS);

            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) result.get("nested");
            assertThat(nested).containsEntry("a", "1");
            assertThat(nested).containsEntry("b", "2");
        }

        @Test
        @DisplayName("APPEND_LISTS should replace non-list values with overlay")
        void appendListsShouldReplaceNonListValues() {
            Map<String, Object> base = Map.of("key", "base");
            Map<String, Object> overlay = Map.of("key", "overlay");

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.APPEND_LISTS);

            assertThat(result).containsEntry("key", "overlay");
        }

        @Test
        @DisplayName("APPEND_LISTS should handle nested list within maps")
        void appendListsShouldHandleNestedListsWithinMaps() {
            Map<String, Object> base = new LinkedHashMap<>();
            Map<String, Object> innerBase = new LinkedHashMap<>();
            innerBase.put("items", new ArrayList<>(List.of("x", "y")));
            base.put("config", innerBase);

            Map<String, Object> overlay = new LinkedHashMap<>();
            Map<String, Object> innerOverlay = new LinkedHashMap<>();
            innerOverlay.put("items", new ArrayList<>(List.of("z")));
            overlay.put("config", innerOverlay);

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.APPEND_LISTS);

            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) result.get("config");
            @SuppressWarnings("unchecked")
            List<String> items = (List<String>) config.get("items");
            assertThat(items).containsExactly("x", "y", "z");
        }
    }

    @Nested
    @DisplayName("MERGE_LISTS_UNIQUE Strategy Tests")
    class MergeListsUniqueStrategyTests {

        @Test
        @DisplayName("MERGE_LISTS_UNIQUE should merge lists removing duplicates")
        void mergeListsUniqueShouldRemoveDuplicates() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("list", new ArrayList<>(List.of("a", "b")));

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("list", new ArrayList<>(List.of("b", "c")));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.MERGE_LISTS_UNIQUE);

            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) result.get("list");
            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("MERGE_LISTS_UNIQUE should preserve order (base first, then overlay)")
        void mergeListsUniqueShouldPreserveOrder() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("list", new ArrayList<>(List.of("c", "a")));

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("list", new ArrayList<>(List.of("b", "a")));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.MERGE_LISTS_UNIQUE);

            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) result.get("list");
            assertThat(list).containsExactly("c", "a", "b");
        }

        @Test
        @DisplayName("MERGE_LISTS_UNIQUE should deep merge nested maps")
        void mergeListsUniqueShouldDeepMergeNestedMaps() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("nested", new LinkedHashMap<>(Map.of("a", "1")));

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("nested", new LinkedHashMap<>(Map.of("b", "2")));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.MERGE_LISTS_UNIQUE);

            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) result.get("nested");
            assertThat(nested).containsEntry("a", "1");
            assertThat(nested).containsEntry("b", "2");
        }

        @Test
        @DisplayName("MERGE_LISTS_UNIQUE should replace non-list values with overlay")
        void mergeListsUniqueShouldReplaceNonListValues() {
            Map<String, Object> base = Map.of("key", "base");
            Map<String, Object> overlay = Map.of("key", "overlay");

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.MERGE_LISTS_UNIQUE);

            assertThat(result).containsEntry("key", "overlay");
        }

        @Test
        @DisplayName("MERGE_LISTS_UNIQUE should handle integer lists")
        void mergeListsUniqueShouldHandleIntegerLists() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("numbers", new ArrayList<>(List.of(1, 2, 3)));

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("numbers", new ArrayList<>(List.of(3, 4, 5)));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.MERGE_LISTS_UNIQUE);

            @SuppressWarnings("unchecked")
            List<Integer> numbers = (List<Integer>) result.get("numbers");
            assertThat(numbers).containsExactly(1, 2, 3, 4, 5);
        }
    }

    @Nested
    @DisplayName("FAIL_ON_CONFLICT Strategy Tests")
    class FailOnConflictStrategyTests {

        @Test
        @DisplayName("FAIL_ON_CONFLICT should allow identical values")
        void failOnConflictShouldAllowIdenticalValues() {
            Map<String, Object> base = Map.of("key", "same-value");
            Map<String, Object> overlay = Map.of("key", "same-value");

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.FAIL_ON_CONFLICT);

            assertThat(result).containsEntry("key", "same-value");
        }

        @Test
        @DisplayName("FAIL_ON_CONFLICT should throw on conflicting scalar values")
        void failOnConflictShouldThrowOnConflictingScalars() {
            Map<String, Object> base = Map.of("key", "base-value");
            Map<String, Object> overlay = Map.of("key", "overlay-value");

            assertThatThrownBy(() -> YmlMerger.merge(base, overlay, MergeStrategy.FAIL_ON_CONFLICT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Merge conflict")
                .hasMessageContaining("base-value")
                .hasMessageContaining("overlay-value");
        }

        @Test
        @DisplayName("FAIL_ON_CONFLICT should allow non-overlapping keys")
        void failOnConflictShouldAllowNonOverlappingKeys() {
            Map<String, Object> base = Map.of("a", "1");
            Map<String, Object> overlay = Map.of("b", "2");

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.FAIL_ON_CONFLICT);

            assertThat(result).containsEntry("a", "1");
            assertThat(result).containsEntry("b", "2");
        }

        @Test
        @DisplayName("FAIL_ON_CONFLICT should throw on conflicting lists")
        void failOnConflictShouldThrowOnConflictingLists() {
            Map<String, Object> base = Map.of("list", List.of("a"));
            Map<String, Object> overlay = Map.of("list", List.of("b"));

            assertThatThrownBy(() -> YmlMerger.merge(base, overlay, MergeStrategy.FAIL_ON_CONFLICT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Merge conflict");
        }

        @Test
        @DisplayName("FAIL_ON_CONFLICT should allow identical lists")
        void failOnConflictShouldAllowIdenticalLists() {
            Map<String, Object> base = Map.of("list", List.of("a", "b"));
            Map<String, Object> overlay = Map.of("list", List.of("a", "b"));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.FAIL_ON_CONFLICT);

            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) result.get("list");
            assertThat(list).containsExactly("a", "b");
        }

        @Test
        @DisplayName("FAIL_ON_CONFLICT should throw on conflicting nested maps")
        void failOnConflictShouldThrowOnConflictingNestedMaps() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("nested", new LinkedHashMap<>(Map.of("key", "base")));

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("nested", new LinkedHashMap<>(Map.of("key", "overlay")));

            assertThatThrownBy(() -> YmlMerger.merge(base, overlay, MergeStrategy.FAIL_ON_CONFLICT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Merge conflict");
        }

        @Test
        @DisplayName("FAIL_ON_CONFLICT should throw on type mismatch")
        void failOnConflictShouldThrowOnTypeMismatch() {
            Map<String, Object> base = Map.of("key", "string");
            Map<String, Object> overlay = Map.of("key", 123);

            assertThatThrownBy(() -> YmlMerger.merge(base, overlay, MergeStrategy.FAIL_ON_CONFLICT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Merge conflict");
        }
    }

    @Nested
    @DisplayName("MergeAll Varargs Tests")
    class MergeAllVarargsTests {

        @Test
        @DisplayName("mergeAll with no maps should return empty map")
        void mergeAllWithNoMapsShouldReturnEmptyMap() {
            Map<String, Object> result = YmlMerger.mergeAll();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("mergeAll with single map should return copy of map")
        void mergeAllWithSingleMapShouldReturnCopy() {
            Map<String, Object> map = Map.of("key", "value");

            Map<String, Object> result = YmlMerger.mergeAll(map);

            assertThat(result).containsEntry("key", "value");
            assertThat(result).isNotSameAs(map);
        }

        @Test
        @DisplayName("mergeAll should merge multiple maps")
        void mergeAllShouldMergeMultipleMaps() {
            Map<String, Object> map1 = Map.of("a", "1");
            Map<String, Object> map2 = Map.of("b", "2");
            Map<String, Object> map3 = Map.of("c", "3");

            Map<String, Object> result = YmlMerger.mergeAll(map1, map2, map3);

            assertThat(result).containsEntry("a", "1");
            assertThat(result).containsEntry("b", "2");
            assertThat(result).containsEntry("c", "3");
        }

        @Test
        @DisplayName("mergeAll should apply merges in order (later wins)")
        void mergeAllShouldApplyMergesInOrder() {
            Map<String, Object> map1 = Map.of("key", "first");
            Map<String, Object> map2 = Map.of("key", "second");
            Map<String, Object> map3 = Map.of("key", "third");

            Map<String, Object> result = YmlMerger.mergeAll(map1, map2, map3);

            assertThat(result).containsEntry("key", "third");
        }

        @Test
        @DisplayName("mergeAll with null array should return empty map")
        void mergeAllWithNullArrayShouldReturnEmptyMap() {
            Map<String, Object>[] nullArray = null;

            Map<String, Object> result = YmlMerger.mergeAll(nullArray);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("mergeAll should handle null elements in array")
        void mergeAllShouldHandleNullElementsInArray() {
            Map<String, Object> map1 = Map.of("a", "1");
            Map<String, Object> map2 = null;
            Map<String, Object> map3 = Map.of("c", "3");

            @SuppressWarnings("unchecked")
            Map<String, Object> result = YmlMerger.mergeAll(map1, map2, map3);

            assertThat(result).containsEntry("a", "1");
            assertThat(result).containsEntry("c", "3");
        }
    }

    @Nested
    @DisplayName("MergeAll Varargs with Strategy Tests")
    class MergeAllVarargsWithStrategyTests {

        @Test
        @DisplayName("mergeAll with strategy should use specified strategy")
        void mergeAllWithStrategyShouldUseSpecifiedStrategy() {
            Map<String, Object> map1 = Map.of("key", "first");
            Map<String, Object> map2 = Map.of("key", "second");

            Map<String, Object> result = YmlMerger.mergeAll(MergeStrategy.KEEP_FIRST, map1, map2);

            assertThat(result).containsEntry("key", "first");
        }

        @Test
        @DisplayName("mergeAll with APPEND_LISTS should append all lists")
        void mergeAllWithAppendListsShouldAppendAllLists() {
            Map<String, Object> map1 = new LinkedHashMap<>();
            map1.put("list", new ArrayList<>(List.of("a")));

            Map<String, Object> map2 = new LinkedHashMap<>();
            map2.put("list", new ArrayList<>(List.of("b")));

            Map<String, Object> map3 = new LinkedHashMap<>();
            map3.put("list", new ArrayList<>(List.of("c")));

            Map<String, Object> result = YmlMerger.mergeAll(MergeStrategy.APPEND_LISTS, map1, map2, map3);

            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) result.get("list");
            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("mergeAll with null array and strategy should return empty map")
        void mergeAllWithNullArrayAndStrategyShouldReturnEmptyMap() {
            Map<String, Object>[] nullArray = null;

            Map<String, Object> result = YmlMerger.mergeAll(MergeStrategy.DEEP_MERGE, nullArray);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("mergeAll with empty array and strategy should return empty map")
        void mergeAllWithEmptyArrayAndStrategyShouldReturnEmptyMap() {
            @SuppressWarnings("unchecked")
            Map<String, Object>[] emptyArray = new Map[0];

            Map<String, Object> result = YmlMerger.mergeAll(MergeStrategy.OVERRIDE, emptyArray);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("MergeAll List Tests")
    class MergeAllListTests {

        @Test
        @DisplayName("mergeAll with empty list should return empty map")
        void mergeAllWithEmptyListShouldReturnEmptyMap() {
            List<Map<String, Object>> emptyList = List.of();

            Map<String, Object> result = YmlMerger.mergeAll(emptyList);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("mergeAll with null list should return empty map")
        void mergeAllWithNullListShouldReturnEmptyMap() {
            List<Map<String, Object>> nullList = null;

            Map<String, Object> result = YmlMerger.mergeAll(nullList);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("mergeAll with list should merge all maps")
        void mergeAllWithListShouldMergeAllMaps() {
            List<Map<String, Object>> maps = List.of(
                Map.of("a", "1"),
                Map.of("b", "2"),
                Map.of("c", "3")
            );

            Map<String, Object> result = YmlMerger.mergeAll(maps);

            assertThat(result).containsEntry("a", "1");
            assertThat(result).containsEntry("b", "2");
            assertThat(result).containsEntry("c", "3");
        }

        @Test
        @DisplayName("mergeAll with list should use default DEEP_MERGE strategy")
        void mergeAllWithListShouldUseDefaultDeepMergeStrategy() {
            Map<String, Object> map1 = new LinkedHashMap<>();
            map1.put("nested", new LinkedHashMap<>(Map.of("a", "1")));

            Map<String, Object> map2 = new LinkedHashMap<>();
            map2.put("nested", new LinkedHashMap<>(Map.of("b", "2")));

            List<Map<String, Object>> maps = List.of(map1, map2);

            Map<String, Object> result = YmlMerger.mergeAll(maps);

            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) result.get("nested");
            assertThat(nested).containsEntry("a", "1");
            assertThat(nested).containsEntry("b", "2");
        }
    }

    @Nested
    @DisplayName("MergeAll List with Strategy Tests")
    class MergeAllListWithStrategyTests {

        @Test
        @DisplayName("mergeAll with list and strategy should use specified strategy")
        void mergeAllWithListAndStrategyShouldUseSpecifiedStrategy() {
            Map<String, Object> map1 = Map.of("key", "first");
            Map<String, Object> map2 = Map.of("key", "second");
            List<Map<String, Object>> maps = List.of(map1, map2);

            Map<String, Object> result = YmlMerger.mergeAll(maps, MergeStrategy.KEEP_FIRST);

            assertThat(result).containsEntry("key", "first");
        }

        @Test
        @DisplayName("mergeAll with null list and strategy should return empty map")
        void mergeAllWithNullListAndStrategyShouldReturnEmptyMap() {
            Map<String, Object> result = YmlMerger.mergeAll(null, MergeStrategy.OVERRIDE);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("mergeAll with empty list and strategy should return empty map")
        void mergeAllWithEmptyListAndStrategyShouldReturnEmptyMap() {
            List<Map<String, Object>> emptyList = List.of();

            Map<String, Object> result = YmlMerger.mergeAll(emptyList, MergeStrategy.DEEP_MERGE);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("mergeAll list with MERGE_LISTS_UNIQUE should deduplicate lists")
        void mergeAllListWithMergeListsUniqueShouldDeduplicateLists() {
            Map<String, Object> map1 = new LinkedHashMap<>();
            map1.put("items", new ArrayList<>(List.of("a", "b")));

            Map<String, Object> map2 = new LinkedHashMap<>();
            map2.put("items", new ArrayList<>(List.of("b", "c")));

            Map<String, Object> map3 = new LinkedHashMap<>();
            map3.put("items", new ArrayList<>(List.of("c", "d")));

            List<Map<String, Object>> maps = List.of(map1, map2, map3);

            Map<String, Object> result = YmlMerger.mergeAll(maps, MergeStrategy.MERGE_LISTS_UNIQUE);

            @SuppressWarnings("unchecked")
            List<String> items = (List<String>) result.get("items");
            assertThat(items).containsExactly("a", "b", "c", "d");
        }
    }

    @Nested
    @DisplayName("Nested Map Merging Tests")
    class NestedMapMergingTests {

        @Test
        @DisplayName("should handle three levels of nesting")
        void shouldHandleThreeLevelsOfNesting() {
            Map<String, Object> base = new LinkedHashMap<>();
            Map<String, Object> level2Base = new LinkedHashMap<>();
            Map<String, Object> level3Base = new LinkedHashMap<>();
            level3Base.put("deep", "base-deep");
            level2Base.put("level3", level3Base);
            base.put("level1", new LinkedHashMap<>(Map.of("level2", level2Base)));

            Map<String, Object> overlay = new LinkedHashMap<>();
            Map<String, Object> level2Overlay = new LinkedHashMap<>();
            Map<String, Object> level3Overlay = new LinkedHashMap<>();
            level3Overlay.put("extra", "overlay-extra");
            level2Overlay.put("level3", level3Overlay);
            overlay.put("level1", new LinkedHashMap<>(Map.of("level2", level2Overlay)));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.DEEP_MERGE);

            @SuppressWarnings("unchecked")
            Map<String, Object> level1 = (Map<String, Object>) result.get("level1");
            @SuppressWarnings("unchecked")
            Map<String, Object> level2 = (Map<String, Object>) level1.get("level2");
            @SuppressWarnings("unchecked")
            Map<String, Object> level3 = (Map<String, Object>) level2.get("level3");

            assertThat(level3).containsEntry("deep", "base-deep");
            assertThat(level3).containsEntry("extra", "overlay-extra");
        }

        @Test
        @DisplayName("should preserve sibling maps during deep merge")
        void shouldPreserveSiblingMapsDuringDeepMerge() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("config1", new LinkedHashMap<>(Map.of("setting", "value1")));
            base.put("config2", new LinkedHashMap<>(Map.of("setting", "value2")));

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("config1", new LinkedHashMap<>(Map.of("newSetting", "newValue")));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.DEEP_MERGE);

            @SuppressWarnings("unchecked")
            Map<String, Object> config1 = (Map<String, Object>) result.get("config1");
            @SuppressWarnings("unchecked")
            Map<String, Object> config2 = (Map<String, Object>) result.get("config2");

            assertThat(config1).containsEntry("setting", "value1");
            assertThat(config1).containsEntry("newSetting", "newValue");
            assertThat(config2).containsEntry("setting", "value2");
        }

        @Test
        @DisplayName("should handle map becoming scalar in overlay")
        void shouldHandleMapBecomingScalarInOverlay() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("config", new LinkedHashMap<>(Map.of("nested", "value")));

            Map<String, Object> overlay = Map.of("config", "simple-string");

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.OVERRIDE);

            assertThat(result.get("config")).isEqualTo("simple-string");
        }

        @Test
        @DisplayName("should handle scalar becoming map in overlay")
        void shouldHandleScalarBecomingMapInOverlay() {
            Map<String, Object> base = Map.of("config", "simple-string");

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("config", new LinkedHashMap<>(Map.of("nested", "value")));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.OVERRIDE);

            assertThat(result.get("config")).isInstanceOf(Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) result.get("config");
            assertThat(config).containsEntry("nested", "value");
        }
    }

    @Nested
    @DisplayName("Result Map Behavior Tests")
    class ResultMapBehaviorTests {

        @Test
        @DisplayName("merge should return LinkedHashMap preserving order")
        void mergeShouldReturnLinkedHashMapPreservingOrder() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("z", "last");
            base.put("a", "first");

            Map<String, Object> overlay = new LinkedHashMap<>();

            Map<String, Object> result = YmlMerger.merge(base, overlay);

            assertThat(result).isInstanceOf(LinkedHashMap.class);
            assertThat(new ArrayList<>(result.keySet())).containsExactly("z", "a");
        }

        @Test
        @DisplayName("merge should return new map not modifying inputs")
        void mergeShouldReturnNewMapNotModifyingInputs() {
            Map<String, Object> base = new LinkedHashMap<>(Map.of("a", "1"));
            Map<String, Object> overlay = new LinkedHashMap<>(Map.of("b", "2"));

            Map<String, Object> result = YmlMerger.merge(base, overlay);
            result.put("c", "3");

            assertThat(base).doesNotContainKey("b");
            assertThat(base).doesNotContainKey("c");
            assertThat(overlay).doesNotContainKey("a");
            assertThat(overlay).doesNotContainKey("c");
        }

        @Test
        @DisplayName("merge result should be mutable")
        void mergeResultShouldBeMutable() {
            Map<String, Object> base = Map.of("a", "1");
            Map<String, Object> overlay = Map.of("b", "2");

            Map<String, Object> result = YmlMerger.merge(base, overlay);

            assertThatCode(() -> result.put("c", "3")).doesNotThrowAnyException();
            assertThat(result).containsEntry("c", "3");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("merge should handle empty base map")
        void mergeShouldHandleEmptyBaseMap() {
            Map<String, Object> base = Map.of();
            Map<String, Object> overlay = Map.of("key", "value");

            Map<String, Object> result = YmlMerger.merge(base, overlay);

            assertThat(result).containsEntry("key", "value");
        }

        @Test
        @DisplayName("merge should handle empty overlay map")
        void mergeShouldHandleEmptyOverlayMap() {
            Map<String, Object> base = Map.of("key", "value");
            Map<String, Object> overlay = Map.of();

            Map<String, Object> result = YmlMerger.merge(base, overlay);

            assertThat(result).containsEntry("key", "value");
        }

        @Test
        @DisplayName("merge should handle both empty maps")
        void mergeShouldHandleBothEmptyMaps() {
            Map<String, Object> base = Map.of();
            Map<String, Object> overlay = Map.of();

            Map<String, Object> result = YmlMerger.merge(base, overlay);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("merge should handle map with empty nested map")
        void mergeShouldHandleMapWithEmptyNestedMap() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("nested", new LinkedHashMap<>());

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("nested", new LinkedHashMap<>(Map.of("key", "value")));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.DEEP_MERGE);

            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) result.get("nested");
            assertThat(nested).containsEntry("key", "value");
        }

        @Test
        @DisplayName("merge should handle map with empty list")
        void mergeShouldHandleMapWithEmptyList() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("list", new ArrayList<>());

            Map<String, Object> overlay = new LinkedHashMap<>();
            overlay.put("list", new ArrayList<>(List.of("item")));

            Map<String, Object> result = YmlMerger.merge(base, overlay, MergeStrategy.APPEND_LISTS);

            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) result.get("list");
            assertThat(list).containsExactly("item");
        }

        @Test
        @DisplayName("merge should handle various value types")
        void mergeShouldHandleVariousValueTypes() {
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("string", "text");
            base.put("integer", 42);
            base.put("double", 3.14);
            base.put("boolean", true);
            base.put("list", List.of("a", "b"));
            base.put("map", Map.of("key", "value"));

            Map<String, Object> overlay = new LinkedHashMap<>();

            Map<String, Object> result = YmlMerger.merge(base, overlay);

            assertThat(result).containsEntry("string", "text");
            assertThat(result).containsEntry("integer", 42);
            assertThat(result).containsEntry("double", 3.14);
            assertThat(result).containsEntry("boolean", true);
            assertThat(result.get("list")).isInstanceOf(List.class);
            assertThat(result.get("map")).isInstanceOf(Map.class);
        }
    }
}
