package cloud.opencode.base.yml.spi;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link YmlFeature} enum.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlFeature Tests")
class YmlFeatureTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("ALLOW_DUPLICATE_KEYS should exist")
        void allowDuplicateKeysShouldExist() {
            assertThat(YmlFeature.ALLOW_DUPLICATE_KEYS).isNotNull();
            assertThat(YmlFeature.valueOf("ALLOW_DUPLICATE_KEYS")).isEqualTo(YmlFeature.ALLOW_DUPLICATE_KEYS);
        }

        @Test
        @DisplayName("ALLOW_RECURSIVE_KEYS should exist")
        void allowRecursiveKeysShouldExist() {
            assertThat(YmlFeature.ALLOW_RECURSIVE_KEYS).isNotNull();
            assertThat(YmlFeature.valueOf("ALLOW_RECURSIVE_KEYS")).isEqualTo(YmlFeature.ALLOW_RECURSIVE_KEYS);
        }

        @Test
        @DisplayName("SAFE_MODE should exist")
        void safeModeShouldExist() {
            assertThat(YmlFeature.SAFE_MODE).isNotNull();
            assertThat(YmlFeature.valueOf("SAFE_MODE")).isEqualTo(YmlFeature.SAFE_MODE);
        }

        @Test
        @DisplayName("BLOCK_STYLE should exist")
        void blockStyleShouldExist() {
            assertThat(YmlFeature.BLOCK_STYLE).isNotNull();
            assertThat(YmlFeature.valueOf("BLOCK_STYLE")).isEqualTo(YmlFeature.BLOCK_STYLE);
        }

        @Test
        @DisplayName("FLOW_STYLE should exist")
        void flowStyleShouldExist() {
            assertThat(YmlFeature.FLOW_STYLE).isNotNull();
            assertThat(YmlFeature.valueOf("FLOW_STYLE")).isEqualTo(YmlFeature.FLOW_STYLE);
        }

        @Test
        @DisplayName("PRESERVE_COMMENTS should exist")
        void preserveCommentsShouldExist() {
            assertThat(YmlFeature.PRESERVE_COMMENTS).isNotNull();
            assertThat(YmlFeature.valueOf("PRESERVE_COMMENTS")).isEqualTo(YmlFeature.PRESERVE_COMMENTS);
        }

        @Test
        @DisplayName("YAML_1_2 should exist")
        void yaml12ShouldExist() {
            assertThat(YmlFeature.YAML_1_2).isNotNull();
            assertThat(YmlFeature.valueOf("YAML_1_2")).isEqualTo(YmlFeature.YAML_1_2);
        }

        @Test
        @DisplayName("ANCHORS_ALIASES should exist")
        void anchorsAliasesShouldExist() {
            assertThat(YmlFeature.ANCHORS_ALIASES).isNotNull();
            assertThat(YmlFeature.valueOf("ANCHORS_ALIASES")).isEqualTo(YmlFeature.ANCHORS_ALIASES);
        }

        @Test
        @DisplayName("CUSTOM_TAGS should exist")
        void customTagsShouldExist() {
            assertThat(YmlFeature.CUSTOM_TAGS).isNotNull();
            assertThat(YmlFeature.valueOf("CUSTOM_TAGS")).isEqualTo(YmlFeature.CUSTOM_TAGS);
        }

        @Test
        @DisplayName("MULTI_DOCUMENT should exist")
        void multiDocumentShouldExist() {
            assertThat(YmlFeature.MULTI_DOCUMENT).isNotNull();
            assertThat(YmlFeature.valueOf("MULTI_DOCUMENT")).isEqualTo(YmlFeature.MULTI_DOCUMENT);
        }

        @Test
        @DisplayName("PRETTY_PRINT should exist")
        void prettyPrintShouldExist() {
            assertThat(YmlFeature.PRETTY_PRINT).isNotNull();
            assertThat(YmlFeature.valueOf("PRETTY_PRINT")).isEqualTo(YmlFeature.PRETTY_PRINT);
        }

        @Test
        @DisplayName("STRICT_TYPES should exist")
        void strictTypesShouldExist() {
            assertThat(YmlFeature.STRICT_TYPES).isNotNull();
            assertThat(YmlFeature.valueOf("STRICT_TYPES")).isEqualTo(YmlFeature.STRICT_TYPES);
        }
    }

    @Nested
    @DisplayName("Enum Standard Methods Tests")
    class EnumStandardMethodsTests {

        @Test
        @DisplayName("values should return all enum values")
        void valuesShouldReturnAllEnumValues() {
            YmlFeature[] values = YmlFeature.values();

            assertThat(values).hasSize(12);
            assertThat(values).containsExactly(
                YmlFeature.ALLOW_DUPLICATE_KEYS,
                YmlFeature.ALLOW_RECURSIVE_KEYS,
                YmlFeature.SAFE_MODE,
                YmlFeature.BLOCK_STYLE,
                YmlFeature.FLOW_STYLE,
                YmlFeature.PRESERVE_COMMENTS,
                YmlFeature.YAML_1_2,
                YmlFeature.ANCHORS_ALIASES,
                YmlFeature.CUSTOM_TAGS,
                YmlFeature.MULTI_DOCUMENT,
                YmlFeature.PRETTY_PRINT,
                YmlFeature.STRICT_TYPES
            );
        }

        @Test
        @DisplayName("valueOf should throw exception for invalid name")
        void valueOfShouldThrowExceptionForInvalidName() {
            assertThatThrownBy(() -> YmlFeature.valueOf("INVALID_FEATURE"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("valueOf should be case sensitive")
        void valueOfShouldBeCaseSensitive() {
            assertThatThrownBy(() -> YmlFeature.valueOf("safe_mode"))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> YmlFeature.valueOf("Safe_Mode"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("name should return enum constant name")
        void nameShouldReturnEnumConstantName() {
            assertThat(YmlFeature.SAFE_MODE.name()).isEqualTo("SAFE_MODE");
            assertThat(YmlFeature.ALLOW_DUPLICATE_KEYS.name()).isEqualTo("ALLOW_DUPLICATE_KEYS");
            assertThat(YmlFeature.YAML_1_2.name()).isEqualTo("YAML_1_2");
        }

        @Test
        @DisplayName("ordinal should return correct position")
        void ordinalShouldReturnCorrectPosition() {
            assertThat(YmlFeature.ALLOW_DUPLICATE_KEYS.ordinal()).isZero();
            assertThat(YmlFeature.ALLOW_RECURSIVE_KEYS.ordinal()).isEqualTo(1);
            assertThat(YmlFeature.SAFE_MODE.ordinal()).isEqualTo(2);
            assertThat(YmlFeature.STRICT_TYPES.ordinal()).isEqualTo(11);
        }

        @Test
        @DisplayName("toString should return enum name")
        void toStringShouldReturnEnumName() {
            assertThat(YmlFeature.SAFE_MODE.toString()).isEqualTo("SAFE_MODE");
            assertThat(YmlFeature.BLOCK_STYLE.toString()).isEqualTo("BLOCK_STYLE");
        }
    }

    @Nested
    @DisplayName("Feature Categories Tests")
    class FeatureCategoriesTests {

        @Test
        @DisplayName("security related features should be available")
        void securityRelatedFeaturesShouldBeAvailable() {
            // Features related to security
            assertThat(YmlFeature.SAFE_MODE).isNotNull();
            assertThat(YmlFeature.STRICT_TYPES).isNotNull();
        }

        @Test
        @DisplayName("key handling features should be available")
        void keyHandlingFeaturesShouldBeAvailable() {
            // Features related to key handling
            assertThat(YmlFeature.ALLOW_DUPLICATE_KEYS).isNotNull();
            assertThat(YmlFeature.ALLOW_RECURSIVE_KEYS).isNotNull();
        }

        @Test
        @DisplayName("output style features should be available")
        void outputStyleFeaturesShouldBeAvailable() {
            // Features related to output style
            assertThat(YmlFeature.BLOCK_STYLE).isNotNull();
            assertThat(YmlFeature.FLOW_STYLE).isNotNull();
            assertThat(YmlFeature.PRETTY_PRINT).isNotNull();
        }

        @Test
        @DisplayName("parsing features should be available")
        void parsingFeaturesShouldBeAvailable() {
            // Features related to parsing
            assertThat(YmlFeature.PRESERVE_COMMENTS).isNotNull();
            assertThat(YmlFeature.MULTI_DOCUMENT).isNotNull();
        }

        @Test
        @DisplayName("YAML spec features should be available")
        void yamlSpecFeaturesShouldBeAvailable() {
            // Features related to YAML specification
            assertThat(YmlFeature.YAML_1_2).isNotNull();
            assertThat(YmlFeature.ANCHORS_ALIASES).isNotNull();
            assertThat(YmlFeature.CUSTOM_TAGS).isNotNull();
        }
    }

    @Nested
    @DisplayName("Enum Comparison Tests")
    class EnumComparisonTests {

        @Test
        @DisplayName("same enum values should be equal")
        void sameEnumValuesShouldBeEqual() {
            assertThat(YmlFeature.SAFE_MODE).isEqualTo(YmlFeature.SAFE_MODE);
            assertThat(YmlFeature.BLOCK_STYLE).isEqualTo(YmlFeature.BLOCK_STYLE);
        }

        @Test
        @DisplayName("different enum values should not be equal")
        void differentEnumValuesShouldNotBeEqual() {
            assertThat(YmlFeature.SAFE_MODE).isNotEqualTo(YmlFeature.BLOCK_STYLE);
            assertThat(YmlFeature.FLOW_STYLE).isNotEqualTo(YmlFeature.BLOCK_STYLE);
        }

        @Test
        @DisplayName("same enum values should have same hashCode")
        void sameEnumValuesShouldHaveSameHashCode() {
            assertThat(YmlFeature.SAFE_MODE.hashCode()).isEqualTo(YmlFeature.SAFE_MODE.hashCode());
        }

        @Test
        @DisplayName("compareTo should work correctly")
        void compareToShouldWorkCorrectly() {
            assertThat(YmlFeature.ALLOW_DUPLICATE_KEYS.compareTo(YmlFeature.SAFE_MODE)).isNegative();
            assertThat(YmlFeature.SAFE_MODE.compareTo(YmlFeature.ALLOW_DUPLICATE_KEYS)).isPositive();
            assertThat(YmlFeature.SAFE_MODE.compareTo(YmlFeature.SAFE_MODE)).isZero();
        }
    }

    @Nested
    @DisplayName("Enum Identity Tests")
    class EnumIdentityTests {

        @Test
        @DisplayName("enum constants should be singletons")
        void enumConstantsShouldBeSingletons() {
            YmlFeature feature1 = YmlFeature.SAFE_MODE;
            YmlFeature feature2 = YmlFeature.valueOf("SAFE_MODE");

            assertThat(feature1).isSameAs(feature2);
        }

        @Test
        @DisplayName("enum constants should support identity comparison")
        void enumConstantsShouldSupportIdentityComparison() {
            assertThat(YmlFeature.SAFE_MODE == YmlFeature.SAFE_MODE).isTrue();
            assertThat(YmlFeature.SAFE_MODE == YmlFeature.BLOCK_STYLE).isFalse();
        }
    }

    @Nested
    @DisplayName("Collection Usage Tests")
    class CollectionUsageTests {

        @Test
        @DisplayName("enum values should work in Set")
        void enumValuesShouldWorkInSet() {
            java.util.Set<YmlFeature> features = java.util.EnumSet.of(
                YmlFeature.SAFE_MODE,
                YmlFeature.PRETTY_PRINT,
                YmlFeature.BLOCK_STYLE
            );

            assertThat(features).hasSize(3);
            assertThat(features).contains(YmlFeature.SAFE_MODE);
            assertThat(features).contains(YmlFeature.PRETTY_PRINT);
            assertThat(features).contains(YmlFeature.BLOCK_STYLE);
            assertThat(features).doesNotContain(YmlFeature.FLOW_STYLE);
        }

        @Test
        @DisplayName("enum values should work in Map")
        void enumValuesShouldWorkInMap() {
            java.util.Map<YmlFeature, Boolean> featureMap = new java.util.EnumMap<>(YmlFeature.class);
            featureMap.put(YmlFeature.SAFE_MODE, true);
            featureMap.put(YmlFeature.ALLOW_DUPLICATE_KEYS, false);

            assertThat(featureMap.get(YmlFeature.SAFE_MODE)).isTrue();
            assertThat(featureMap.get(YmlFeature.ALLOW_DUPLICATE_KEYS)).isFalse();
            assertThat(featureMap.get(YmlFeature.BLOCK_STYLE)).isNull();
        }

        @Test
        @DisplayName("EnumSet.allOf should contain all features")
        void enumSetAllOfShouldContainAllFeatures() {
            java.util.EnumSet<YmlFeature> allFeatures = java.util.EnumSet.allOf(YmlFeature.class);

            assertThat(allFeatures).hasSize(12);
            assertThat(allFeatures).containsAll(java.util.List.of(YmlFeature.values()));
        }

        @Test
        @DisplayName("EnumSet.noneOf should be empty")
        void enumSetNoneOfShouldBeEmpty() {
            java.util.EnumSet<YmlFeature> noFeatures = java.util.EnumSet.noneOf(YmlFeature.class);

            assertThat(noFeatures).isEmpty();
        }
    }
}
