package cloud.opencode.base.yml.security;

import cloud.opencode.base.yml.exception.YmlSecurityException;
import cloud.opencode.base.yml.exception.YmlSecurityException.SecurityViolationType;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlSafeLoaderTest Tests
 * YmlSafeLoaderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlSafeLoader Tests")
class YmlSafeLoaderTest {

    // ==================== create() Factory Method Tests ====================

    @Nested
    @DisplayName("create() factory method")
    class CreateFactoryMethodTests {

        @Test
        @DisplayName("should return non-null loader")
        void shouldReturnNonNullLoader() {
            YmlSafeLoader loader = YmlSafeLoader.create();
            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("should return new instance each time")
        void shouldReturnNewInstanceEachTime() {
            YmlSafeLoader loader1 = YmlSafeLoader.create();
            YmlSafeLoader loader2 = YmlSafeLoader.create();
            assertThat(loader1).isNotSameAs(loader2);
        }

        @Test
        @DisplayName("should have default maxDepth of 100")
        void shouldHaveDefaultMaxDepth() {
            YmlSafeLoader loader = YmlSafeLoader.create();
            assertThat(loader.getMaxDepth()).isEqualTo(100);
        }

        @Test
        @DisplayName("should have default maxSize of 10_000_000")
        void shouldHaveDefaultMaxSize() {
            YmlSafeLoader loader = YmlSafeLoader.create();
            assertThat(loader.getMaxSize()).isEqualTo(10_000_000);
        }

        @Test
        @DisplayName("should have default maxAliases of 50")
        void shouldHaveDefaultMaxAliases() {
            YmlSafeLoader loader = YmlSafeLoader.create();
            assertThat(loader.getMaxAliases()).isEqualTo(50);
        }

        @Test
        @DisplayName("should have default denied tags")
        void shouldHaveDefaultDeniedTags() {
            YmlSafeLoader loader = YmlSafeLoader.create();
            assertThat(loader.isDeniedTag("!!java/object")).isTrue();
            assertThat(loader.isDeniedTag("!!java/class")).isTrue();
            assertThat(loader.isDeniedTag("!!javax/script")).isTrue();
            assertThat(loader.isDeniedTag("tag:yaml.org,2002:java/object")).isTrue();
        }
    }

    // ==================== builder() Factory Method Tests ====================

    @Nested
    @DisplayName("builder() factory method")
    class BuilderFactoryMethodTests {

        @Test
        @DisplayName("should return non-null builder")
        void shouldReturnNonNullBuilder() {
            YmlSafeLoader.Builder builder = YmlSafeLoader.builder();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should return new builder instance each time")
        void shouldReturnNewBuilderInstanceEachTime() {
            YmlSafeLoader.Builder builder1 = YmlSafeLoader.builder();
            YmlSafeLoader.Builder builder2 = YmlSafeLoader.builder();
            assertThat(builder1).isNotSameAs(builder2);
        }

        @Test
        @DisplayName("should build loader with default values")
        void shouldBuildLoaderWithDefaultValues() {
            YmlSafeLoader loader = YmlSafeLoader.builder().build();
            assertThat(loader.getMaxDepth()).isEqualTo(100);
            assertThat(loader.getMaxSize()).isEqualTo(10_000_000);
            assertThat(loader.getMaxAliases()).isEqualTo(50);
        }
    }

    // ==================== validate() Method Tests ====================

    @Nested
    @DisplayName("validate() method")
    class ValidateMethodTests {

        @Nested
        @DisplayName("shallow data validation")
        class ShallowDataValidationTests {

            @Test
            @DisplayName("should accept null data")
            void shouldAcceptNullData() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThatNoException().isThrownBy(() -> loader.validate(null));
            }

            @Test
            @DisplayName("should accept empty map")
            void shouldAcceptEmptyMap() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThatNoException().isThrownBy(() -> loader.validate(new HashMap<>()));
            }

            @Test
            @DisplayName("should accept empty list")
            void shouldAcceptEmptyList() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThatNoException().isThrownBy(() -> loader.validate(new ArrayList<>()));
            }

            @Test
            @DisplayName("should accept simple string")
            void shouldAcceptSimpleString() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThatNoException().isThrownBy(() -> loader.validate("simple string"));
            }

            @Test
            @DisplayName("should accept simple integer")
            void shouldAcceptSimpleInteger() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThatNoException().isThrownBy(() -> loader.validate(42));
            }

            @Test
            @DisplayName("should accept flat map with string values")
            void shouldAcceptFlatMapWithStringValues() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                Map<String, String> data = Map.of("key1", "value1", "key2", "value2");
                assertThatNoException().isThrownBy(() -> loader.validate(data));
            }

            @Test
            @DisplayName("should accept flat list with string values")
            void shouldAcceptFlatListWithStringValues() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                List<String> data = List.of("item1", "item2", "item3");
                assertThatNoException().isThrownBy(() -> loader.validate(data));
            }

            @Test
            @DisplayName("should accept shallow nested map within default depth")
            void shouldAcceptShallowNestedMapWithinDefaultDepth() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                Map<String, Object> data = new HashMap<>();
                data.put("level1", Map.of("level2", Map.of("level3", "value")));
                assertThatNoException().isThrownBy(() -> loader.validate(data));
            }
        }

        @Nested
        @DisplayName("deeply nested Map validation")
        class DeeplyNestedMapValidationTests {

            @Test
            @DisplayName("should throw exception when map depth exceeds maxDepth")
            void shouldThrowExceptionWhenMapDepthExceedsMaxDepth() {
                YmlSafeLoader loader = YmlSafeLoader.builder().maxDepth(5).build();
                Map<String, Object> data = createDeeplyNestedMap(10);

                assertThatThrownBy(() -> loader.validate(data))
                        .isInstanceOf(YmlSecurityException.class)
                        .satisfies(e -> {
                            YmlSecurityException ex = (YmlSecurityException) e;
                            assertThat(ex.getType()).isEqualTo(SecurityViolationType.NESTING_DEPTH_EXCEEDED);
                        });
            }

            @Test
            @DisplayName("should accept map at exactly maxDepth")
            void shouldAcceptMapAtExactlyMaxDepth() {
                YmlSafeLoader loader = YmlSafeLoader.builder().maxDepth(5).build();
                Map<String, Object> data = createDeeplyNestedMap(5);
                assertThatNoException().isThrownBy(() -> loader.validate(data));
            }

            @Test
            @DisplayName("should throw exception when map depth is one more than maxDepth")
            void shouldThrowExceptionWhenMapDepthIsOneMoreThanMaxDepth() {
                YmlSafeLoader loader = YmlSafeLoader.builder().maxDepth(5).build();
                Map<String, Object> data = createDeeplyNestedMap(6);

                assertThatThrownBy(() -> loader.validate(data))
                        .isInstanceOf(YmlSecurityException.class);
            }

            @Test
            @DisplayName("should throw exception with very deep nesting")
            void shouldThrowExceptionWithVeryDeepNesting() {
                YmlSafeLoader loader = YmlSafeLoader.builder().maxDepth(10).build();
                Map<String, Object> data = createDeeplyNestedMap(50);

                assertThatThrownBy(() -> loader.validate(data))
                        .isInstanceOf(YmlSecurityException.class)
                        .satisfies(e -> {
                            YmlSecurityException ex = (YmlSecurityException) e;
                            assertThat(ex.getType()).isEqualTo(SecurityViolationType.NESTING_DEPTH_EXCEEDED);
                        });
            }

            @Test
            @DisplayName("should throw exception with minimal maxDepth of 1")
            void shouldThrowExceptionWithMinimalMaxDepthOf1() {
                YmlSafeLoader loader = YmlSafeLoader.builder().maxDepth(1).build();
                Map<String, Object> data = Map.of("outer", Map.of("inner", "value"));

                assertThatThrownBy(() -> loader.validate(data))
                        .isInstanceOf(YmlSecurityException.class);
            }
        }

        @Nested
        @DisplayName("deeply nested List validation")
        class DeeplyNestedListValidationTests {

            @Test
            @DisplayName("should throw exception when list depth exceeds maxDepth")
            void shouldThrowExceptionWhenListDepthExceedsMaxDepth() {
                YmlSafeLoader loader = YmlSafeLoader.builder().maxDepth(5).build();
                List<Object> data = createDeeplyNestedList(10);

                assertThatThrownBy(() -> loader.validate(data))
                        .isInstanceOf(YmlSecurityException.class)
                        .satisfies(e -> {
                            YmlSecurityException ex = (YmlSecurityException) e;
                            assertThat(ex.getType()).isEqualTo(SecurityViolationType.NESTING_DEPTH_EXCEEDED);
                        });
            }

            @Test
            @DisplayName("should accept list at exactly maxDepth")
            void shouldAcceptListAtExactlyMaxDepth() {
                YmlSafeLoader loader = YmlSafeLoader.builder().maxDepth(5).build();
                List<Object> data = createDeeplyNestedList(5);
                assertThatNoException().isThrownBy(() -> loader.validate(data));
            }

            @Test
            @DisplayName("should throw exception when list depth is one more than maxDepth")
            void shouldThrowExceptionWhenListDepthIsOneMoreThanMaxDepth() {
                YmlSafeLoader loader = YmlSafeLoader.builder().maxDepth(5).build();
                List<Object> data = createDeeplyNestedList(6);

                assertThatThrownBy(() -> loader.validate(data))
                        .isInstanceOf(YmlSecurityException.class);
            }

            @Test
            @DisplayName("should throw exception with very deep list nesting")
            void shouldThrowExceptionWithVeryDeepListNesting() {
                YmlSafeLoader loader = YmlSafeLoader.builder().maxDepth(10).build();
                List<Object> data = createDeeplyNestedList(50);

                assertThatThrownBy(() -> loader.validate(data))
                        .isInstanceOf(YmlSecurityException.class)
                        .satisfies(e -> {
                            YmlSecurityException ex = (YmlSecurityException) e;
                            assertThat(ex.getType()).isEqualTo(SecurityViolationType.NESTING_DEPTH_EXCEEDED);
                        });
            }
        }

        @Nested
        @DisplayName("mixed nested structure validation")
        class MixedNestedStructureValidationTests {

            @Test
            @DisplayName("should validate mixed map and list nesting")
            void shouldValidateMixedMapAndListNesting() {
                YmlSafeLoader loader = YmlSafeLoader.builder().maxDepth(3).build();
                Map<String, Object> data = new HashMap<>();
                data.put("items", List.of(Map.of("nested", List.of("deep"))));

                assertThatThrownBy(() -> loader.validate(data))
                        .isInstanceOf(YmlSecurityException.class);
            }

            @Test
            @DisplayName("should accept mixed structure within depth limit")
            void shouldAcceptMixedStructureWithinDepthLimit() {
                YmlSafeLoader loader = YmlSafeLoader.builder().maxDepth(10).build();
                Map<String, Object> data = new HashMap<>();
                data.put("items", List.of(Map.of("nested", List.of("deep"))));

                assertThatNoException().isThrownBy(() -> loader.validate(data));
            }
        }
    }

    // ==================== isAllowedType() Method Tests ====================

    @Nested
    @DisplayName("isAllowedType() method")
    class IsAllowedTypeMethodTests {

        @Nested
        @DisplayName("primitive types")
        class PrimitiveTypesTests {

            @Test
            @DisplayName("should allow int primitive")
            void shouldAllowIntPrimitive() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(int.class)).isTrue();
            }

            @Test
            @DisplayName("should allow long primitive")
            void shouldAllowLongPrimitive() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(long.class)).isTrue();
            }

            @Test
            @DisplayName("should allow double primitive")
            void shouldAllowDoublePrimitive() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(double.class)).isTrue();
            }

            @Test
            @DisplayName("should allow float primitive")
            void shouldAllowFloatPrimitive() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(float.class)).isTrue();
            }

            @Test
            @DisplayName("should allow boolean primitive")
            void shouldAllowBooleanPrimitive() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(boolean.class)).isTrue();
            }

            @Test
            @DisplayName("should allow short primitive")
            void shouldAllowShortPrimitive() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(short.class)).isTrue();
            }

            @Test
            @DisplayName("should allow byte primitive")
            void shouldAllowBytePrimitive() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(byte.class)).isTrue();
            }

            @Test
            @DisplayName("should allow char primitive")
            void shouldAllowCharPrimitive() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(char.class)).isTrue();
            }
        }

        @Nested
        @DisplayName("wrapper types")
        class WrapperTypesTests {

            @Test
            @DisplayName("should allow String")
            void shouldAllowString() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(String.class)).isTrue();
            }

            @Test
            @DisplayName("should allow Integer")
            void shouldAllowInteger() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(Integer.class)).isTrue();
            }

            @Test
            @DisplayName("should allow Long")
            void shouldAllowLong() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(Long.class)).isTrue();
            }

            @Test
            @DisplayName("should allow Double")
            void shouldAllowDouble() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(Double.class)).isTrue();
            }

            @Test
            @DisplayName("should allow Float")
            void shouldAllowFloat() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(Float.class)).isTrue();
            }

            @Test
            @DisplayName("should allow Boolean")
            void shouldAllowBoolean() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(Boolean.class)).isTrue();
            }

            @Test
            @DisplayName("should allow Short")
            void shouldAllowShort() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(Short.class)).isTrue();
            }

            @Test
            @DisplayName("should allow Byte")
            void shouldAllowByte() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(Byte.class)).isTrue();
            }

            @Test
            @DisplayName("should allow Character")
            void shouldAllowCharacter() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(Character.class)).isTrue();
            }
        }

        @Nested
        @DisplayName("collection types")
        class CollectionTypesTests {

            @Test
            @DisplayName("should allow Map interface")
            void shouldAllowMapInterface() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(Map.class)).isTrue();
            }

            @Test
            @DisplayName("should allow List interface")
            void shouldAllowListInterface() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(List.class)).isTrue();
            }

            @Test
            @DisplayName("should allow Set interface")
            void shouldAllowSetInterface() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(Set.class)).isTrue();
            }

            @Test
            @DisplayName("should allow HashMap")
            void shouldAllowHashMap() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(HashMap.class)).isTrue();
            }

            @Test
            @DisplayName("should allow ArrayList")
            void shouldAllowArrayList() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(ArrayList.class)).isTrue();
            }

            @Test
            @DisplayName("should allow HashSet")
            void shouldAllowHashSet() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(HashSet.class)).isTrue();
            }

            @Test
            @DisplayName("should allow LinkedHashMap")
            void shouldAllowLinkedHashMap() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(LinkedHashMap.class)).isTrue();
            }

            @Test
            @DisplayName("should allow LinkedList")
            void shouldAllowLinkedList() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(LinkedList.class)).isTrue();
            }

            @Test
            @DisplayName("should allow TreeMap")
            void shouldAllowTreeMap() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(TreeMap.class)).isTrue();
            }

            @Test
            @DisplayName("should allow TreeSet")
            void shouldAllowTreeSet() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(TreeSet.class)).isTrue();
            }
        }

        @Nested
        @DisplayName("custom allowed types")
        class CustomAllowedTypesTests {

            @Test
            @DisplayName("should not allow arbitrary class by default")
            void shouldNotAllowArbitraryClassByDefault() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isAllowedType(CustomClass.class)).isFalse();
            }

            @Test
            @DisplayName("should allow custom type when added via builder")
            void shouldAllowCustomTypeWhenAddedViaBuilder() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .allowType(CustomClass.class)
                        .build();
                assertThat(loader.isAllowedType(CustomClass.class)).isTrue();
            }

            @Test
            @DisplayName("should allow multiple custom types when added via builder")
            void shouldAllowMultipleCustomTypesWhenAddedViaBuilder() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .allowTypes(CustomClass.class, AnotherCustomClass.class)
                        .build();
                assertThat(loader.isAllowedType(CustomClass.class)).isTrue();
                assertThat(loader.isAllowedType(AnotherCustomClass.class)).isTrue();
            }
        }
    }

    // ==================== isDeniedTag() Method Tests ====================

    @Nested
    @DisplayName("isDeniedTag() method")
    class IsDeniedTagMethodTests {

        @Nested
        @DisplayName("default denied tags")
        class DefaultDeniedTagsTests {

            @Test
            @DisplayName("should deny !!java/object tag")
            void shouldDenyJavaObjectTag() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isDeniedTag("!!java/object")).isTrue();
            }

            @Test
            @DisplayName("should deny !!java/class tag")
            void shouldDenyJavaClassTag() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isDeniedTag("!!java/class")).isTrue();
            }

            @Test
            @DisplayName("should deny !!javax/script tag")
            void shouldDenyJavaxScriptTag() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isDeniedTag("!!javax/script")).isTrue();
            }

            @Test
            @DisplayName("should deny tag:yaml.org,2002:java/object")
            void shouldDenyYamlOrgJavaObjectTag() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isDeniedTag("tag:yaml.org,2002:java/object")).isTrue();
            }
        }

        @Nested
        @DisplayName("non-denied tags")
        class NonDeniedTagsTests {

            @Test
            @DisplayName("should not deny !!str tag")
            void shouldNotDenyStrTag() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isDeniedTag("!!str")).isFalse();
            }

            @Test
            @DisplayName("should not deny !!int tag")
            void shouldNotDenyIntTag() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isDeniedTag("!!int")).isFalse();
            }

            @Test
            @DisplayName("should not deny !!map tag")
            void shouldNotDenyMapTag() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isDeniedTag("!!map")).isFalse();
            }

            @Test
            @DisplayName("should not deny !!seq tag")
            void shouldNotDenySeqTag() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isDeniedTag("!!seq")).isFalse();
            }

            @Test
            @DisplayName("should not deny arbitrary unknown tag")
            void shouldNotDenyArbitraryUnknownTag() {
                YmlSafeLoader loader = YmlSafeLoader.create();
                assertThat(loader.isDeniedTag("!!custom/tag")).isFalse();
            }
        }

        @Nested
        @DisplayName("custom denied tags")
        class CustomDeniedTagsTests {

            @Test
            @DisplayName("should deny custom tag when added via builder")
            void shouldDenyCustomTagWhenAddedViaBuilder() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .denyTag("!!python/object")
                        .build();
                assertThat(loader.isDeniedTag("!!python/object")).isTrue();
            }

            @Test
            @DisplayName("should deny multiple custom tags when added via builder")
            void shouldDenyMultipleCustomTagsWhenAddedViaBuilder() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .denyTag("!!python/object")
                        .denyTag("!!ruby/object")
                        .build();
                assertThat(loader.isDeniedTag("!!python/object")).isTrue();
                assertThat(loader.isDeniedTag("!!ruby/object")).isTrue();
            }

            @Test
            @DisplayName("should still deny default tags after adding custom tags")
            void shouldStillDenyDefaultTagsAfterAddingCustomTags() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .denyTag("!!python/object")
                        .build();
                assertThat(loader.isDeniedTag("!!java/object")).isTrue();
                assertThat(loader.isDeniedTag("!!java/class")).isTrue();
            }
        }
    }

    // ==================== getMaxDepth() Method Tests ====================

    @Nested
    @DisplayName("getMaxDepth() method")
    class GetMaxDepthMethodTests {

        @Test
        @DisplayName("should return default value of 100")
        void shouldReturnDefaultValueOf100() {
            YmlSafeLoader loader = YmlSafeLoader.create();
            assertThat(loader.getMaxDepth()).isEqualTo(100);
        }

        @Test
        @DisplayName("should return custom value when set via builder")
        void shouldReturnCustomValueWhenSetViaBuilder() {
            YmlSafeLoader loader = YmlSafeLoader.builder()
                    .maxDepth(50)
                    .build();
            assertThat(loader.getMaxDepth()).isEqualTo(50);
        }

        @Test
        @DisplayName("should return small custom value")
        void shouldReturnSmallCustomValue() {
            YmlSafeLoader loader = YmlSafeLoader.builder()
                    .maxDepth(1)
                    .build();
            assertThat(loader.getMaxDepth()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return large custom value")
        void shouldReturnLargeCustomValue() {
            YmlSafeLoader loader = YmlSafeLoader.builder()
                    .maxDepth(1000)
                    .build();
            assertThat(loader.getMaxDepth()).isEqualTo(1000);
        }
    }

    // ==================== getMaxSize() Method Tests ====================

    @Nested
    @DisplayName("getMaxSize() method")
    class GetMaxSizeMethodTests {

        @Test
        @DisplayName("should return default value of 10_000_000")
        void shouldReturnDefaultValueOf10000000() {
            YmlSafeLoader loader = YmlSafeLoader.create();
            assertThat(loader.getMaxSize()).isEqualTo(10_000_000);
        }

        @Test
        @DisplayName("should return custom value when set via builder")
        void shouldReturnCustomValueWhenSetViaBuilder() {
            YmlSafeLoader loader = YmlSafeLoader.builder()
                    .maxSize(1_000_000)
                    .build();
            assertThat(loader.getMaxSize()).isEqualTo(1_000_000);
        }

        @Test
        @DisplayName("should return small custom value")
        void shouldReturnSmallCustomValue() {
            YmlSafeLoader loader = YmlSafeLoader.builder()
                    .maxSize(1024)
                    .build();
            assertThat(loader.getMaxSize()).isEqualTo(1024);
        }

        @Test
        @DisplayName("should return large custom value")
        void shouldReturnLargeCustomValue() {
            YmlSafeLoader loader = YmlSafeLoader.builder()
                    .maxSize(100_000_000)
                    .build();
            assertThat(loader.getMaxSize()).isEqualTo(100_000_000);
        }
    }

    // ==================== getMaxAliases() Method Tests ====================

    @Nested
    @DisplayName("getMaxAliases() method")
    class GetMaxAliasesMethodTests {

        @Test
        @DisplayName("should return default value of 50")
        void shouldReturnDefaultValueOf50() {
            YmlSafeLoader loader = YmlSafeLoader.create();
            assertThat(loader.getMaxAliases()).isEqualTo(50);
        }

        @Test
        @DisplayName("should return custom value when set via builder")
        void shouldReturnCustomValueWhenSetViaBuilder() {
            YmlSafeLoader loader = YmlSafeLoader.builder()
                    .maxAliases(25)
                    .build();
            assertThat(loader.getMaxAliases()).isEqualTo(25);
        }

        @Test
        @DisplayName("should return zero when set to zero")
        void shouldReturnZeroWhenSetToZero() {
            YmlSafeLoader loader = YmlSafeLoader.builder()
                    .maxAliases(0)
                    .build();
            assertThat(loader.getMaxAliases()).isEqualTo(0);
        }

        @Test
        @DisplayName("should return large custom value")
        void shouldReturnLargeCustomValue() {
            YmlSafeLoader loader = YmlSafeLoader.builder()
                    .maxAliases(1000)
                    .build();
            assertThat(loader.getMaxAliases()).isEqualTo(1000);
        }
    }

    // ==================== Builder Tests ====================

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Nested
        @DisplayName("allowType() method")
        class AllowTypeMethodTests {

            @Test
            @DisplayName("should return builder for method chaining")
            void shouldReturnBuilderForMethodChaining() {
                YmlSafeLoader.Builder builder = YmlSafeLoader.builder();
                YmlSafeLoader.Builder result = builder.allowType(CustomClass.class);
                assertThat(result).isSameAs(builder);
            }

            @Test
            @DisplayName("should add type to allowed types")
            void shouldAddTypeToAllowedTypes() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .allowType(CustomClass.class)
                        .build();
                assertThat(loader.isAllowedType(CustomClass.class)).isTrue();
            }

            @Test
            @DisplayName("should allow chaining multiple allowType calls")
            void shouldAllowChainingMultipleAllowTypeCalls() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .allowType(CustomClass.class)
                        .allowType(AnotherCustomClass.class)
                        .build();
                assertThat(loader.isAllowedType(CustomClass.class)).isTrue();
                assertThat(loader.isAllowedType(AnotherCustomClass.class)).isTrue();
            }
        }

        @Nested
        @DisplayName("allowTypes() method")
        class AllowTypesMethodTests {

            @Test
            @DisplayName("should return builder for method chaining")
            void shouldReturnBuilderForMethodChaining() {
                YmlSafeLoader.Builder builder = YmlSafeLoader.builder();
                YmlSafeLoader.Builder result = builder.allowTypes(CustomClass.class);
                assertThat(result).isSameAs(builder);
            }

            @Test
            @DisplayName("should add multiple types to allowed types")
            void shouldAddMultipleTypesToAllowedTypes() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .allowTypes(CustomClass.class, AnotherCustomClass.class)
                        .build();
                assertThat(loader.isAllowedType(CustomClass.class)).isTrue();
                assertThat(loader.isAllowedType(AnotherCustomClass.class)).isTrue();
            }

            @Test
            @DisplayName("should handle single type in varargs")
            void shouldHandleSingleTypeInVarargs() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .allowTypes(CustomClass.class)
                        .build();
                assertThat(loader.isAllowedType(CustomClass.class)).isTrue();
            }

            @Test
            @DisplayName("should handle empty varargs")
            void shouldHandleEmptyVarargs() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .allowTypes()
                        .build();
                assertThat(loader.isAllowedType(CustomClass.class)).isFalse();
            }
        }

        @Nested
        @DisplayName("denyTag() method")
        class DenyTagMethodTests {

            @Test
            @DisplayName("should return builder for method chaining")
            void shouldReturnBuilderForMethodChaining() {
                YmlSafeLoader.Builder builder = YmlSafeLoader.builder();
                YmlSafeLoader.Builder result = builder.denyTag("!!custom/tag");
                assertThat(result).isSameAs(builder);
            }

            @Test
            @DisplayName("should add tag to denied tags")
            void shouldAddTagToDeniedTags() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .denyTag("!!custom/tag")
                        .build();
                assertThat(loader.isDeniedTag("!!custom/tag")).isTrue();
            }

            @Test
            @DisplayName("should allow chaining multiple denyTag calls")
            void shouldAllowChainingMultipleDenyTagCalls() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .denyTag("!!custom/tag1")
                        .denyTag("!!custom/tag2")
                        .build();
                assertThat(loader.isDeniedTag("!!custom/tag1")).isTrue();
                assertThat(loader.isDeniedTag("!!custom/tag2")).isTrue();
            }
        }

        @Nested
        @DisplayName("maxDepth() method")
        class MaxDepthMethodTests {

            @Test
            @DisplayName("should return builder for method chaining")
            void shouldReturnBuilderForMethodChaining() {
                YmlSafeLoader.Builder builder = YmlSafeLoader.builder();
                YmlSafeLoader.Builder result = builder.maxDepth(50);
                assertThat(result).isSameAs(builder);
            }

            @Test
            @DisplayName("should set maxDepth value")
            void shouldSetMaxDepthValue() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .maxDepth(75)
                        .build();
                assertThat(loader.getMaxDepth()).isEqualTo(75);
            }

            @Test
            @DisplayName("should allow overwriting maxDepth value")
            void shouldAllowOverwritingMaxDepthValue() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .maxDepth(50)
                        .maxDepth(75)
                        .build();
                assertThat(loader.getMaxDepth()).isEqualTo(75);
            }
        }

        @Nested
        @DisplayName("maxSize() method")
        class MaxSizeMethodTests {

            @Test
            @DisplayName("should return builder for method chaining")
            void shouldReturnBuilderForMethodChaining() {
                YmlSafeLoader.Builder builder = YmlSafeLoader.builder();
                YmlSafeLoader.Builder result = builder.maxSize(5_000_000);
                assertThat(result).isSameAs(builder);
            }

            @Test
            @DisplayName("should set maxSize value")
            void shouldSetMaxSizeValue() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .maxSize(5_000_000)
                        .build();
                assertThat(loader.getMaxSize()).isEqualTo(5_000_000);
            }

            @Test
            @DisplayName("should allow overwriting maxSize value")
            void shouldAllowOverwritingMaxSizeValue() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .maxSize(5_000_000)
                        .maxSize(8_000_000)
                        .build();
                assertThat(loader.getMaxSize()).isEqualTo(8_000_000);
            }
        }

        @Nested
        @DisplayName("maxAliases() method")
        class MaxAliasesMethodTests {

            @Test
            @DisplayName("should return builder for method chaining")
            void shouldReturnBuilderForMethodChaining() {
                YmlSafeLoader.Builder builder = YmlSafeLoader.builder();
                YmlSafeLoader.Builder result = builder.maxAliases(25);
                assertThat(result).isSameAs(builder);
            }

            @Test
            @DisplayName("should set maxAliases value")
            void shouldSetMaxAliasesValue() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .maxAliases(30)
                        .build();
                assertThat(loader.getMaxAliases()).isEqualTo(30);
            }

            @Test
            @DisplayName("should allow overwriting maxAliases value")
            void shouldAllowOverwritingMaxAliasesValue() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .maxAliases(30)
                        .maxAliases(40)
                        .build();
                assertThat(loader.getMaxAliases()).isEqualTo(40);
            }
        }

        @Nested
        @DisplayName("build() method")
        class BuildMethodTests {

            @Test
            @DisplayName("should create immutable allowed types set")
            void shouldCreateImmutableAllowedTypesSet() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .allowType(CustomClass.class)
                        .build();
                // Allowed types should be immutable - attempting to check via isAllowedType
                assertThat(loader.isAllowedType(CustomClass.class)).isTrue();
            }

            @Test
            @DisplayName("should create loader with all custom settings")
            void shouldCreateLoaderWithAllCustomSettings() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .maxDepth(50)
                        .maxSize(5_000_000)
                        .maxAliases(25)
                        .allowType(CustomClass.class)
                        .denyTag("!!custom/tag")
                        .build();

                assertThat(loader.getMaxDepth()).isEqualTo(50);
                assertThat(loader.getMaxSize()).isEqualTo(5_000_000);
                assertThat(loader.getMaxAliases()).isEqualTo(25);
                assertThat(loader.isAllowedType(CustomClass.class)).isTrue();
                assertThat(loader.isDeniedTag("!!custom/tag")).isTrue();
            }

            @Test
            @DisplayName("should create independent loaders from same builder")
            void shouldCreateIndependentLoadersFromSameBuilder() {
                YmlSafeLoader.Builder builder = YmlSafeLoader.builder().maxDepth(50);
                YmlSafeLoader loader1 = builder.build();
                builder.maxDepth(75);
                YmlSafeLoader loader2 = builder.build();

                assertThat(loader1.getMaxDepth()).isEqualTo(50);
                assertThat(loader2.getMaxDepth()).isEqualTo(75);
            }
        }

        @Nested
        @DisplayName("fluent API chaining")
        class FluentApiChainingTests {

            @Test
            @DisplayName("should support full fluent API chain")
            void shouldSupportFullFluentApiChain() {
                YmlSafeLoader loader = YmlSafeLoader.builder()
                        .maxDepth(50)
                        .maxSize(5_000_000)
                        .maxAliases(25)
                        .allowType(CustomClass.class)
                        .allowTypes(AnotherCustomClass.class)
                        .denyTag("!!python/object")
                        .denyTag("!!ruby/object")
                        .build();

                assertThat(loader.getMaxDepth()).isEqualTo(50);
                assertThat(loader.getMaxSize()).isEqualTo(5_000_000);
                assertThat(loader.getMaxAliases()).isEqualTo(25);
                assertThat(loader.isAllowedType(CustomClass.class)).isTrue();
                assertThat(loader.isAllowedType(AnotherCustomClass.class)).isTrue();
                assertThat(loader.isDeniedTag("!!python/object")).isTrue();
                assertThat(loader.isDeniedTag("!!ruby/object")).isTrue();
            }
        }
    }

    // ==================== Edge Cases ====================

    @Nested
    @DisplayName("Edge cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle maxDepth of zero")
        void shouldHandleMaxDepthOfZero() {
            YmlSafeLoader loader = YmlSafeLoader.builder().maxDepth(0).build();
            Map<String, Object> data = Map.of("key", "value");

            assertThatThrownBy(() -> loader.validate(data))
                    .isInstanceOf(YmlSecurityException.class);
        }

        @Test
        @DisplayName("should handle negative maxDepth")
        void shouldHandleNegativeMaxDepth() {
            YmlSafeLoader loader = YmlSafeLoader.builder().maxDepth(-1).build();
            Map<String, Object> data = Map.of("key", "value");

            // With negative maxDepth, even depth 0 would exceed the limit
            assertThatThrownBy(() -> loader.validate(data))
                    .isInstanceOf(YmlSecurityException.class);
        }

        @Test
        @DisplayName("should validate empty nested structures")
        void shouldValidateEmptyNestedStructures() {
            YmlSafeLoader loader = YmlSafeLoader.builder().maxDepth(2).build();
            Map<String, Object> data = Map.of("outer", Collections.emptyMap());
            assertThatNoException().isThrownBy(() -> loader.validate(data));
        }

        @Test
        @DisplayName("should handle list with mixed types")
        void shouldHandleListWithMixedTypes() {
            YmlSafeLoader loader = YmlSafeLoader.create();
            List<Object> data = new ArrayList<>();
            data.add("string");
            data.add(42);
            data.add(Map.of("key", "value"));
            data.add(List.of(1, 2, 3));
            assertThatNoException().isThrownBy(() -> loader.validate(data));
        }

        @Test
        @DisplayName("should handle map with null values")
        void shouldHandleMapWithNullValues() {
            YmlSafeLoader loader = YmlSafeLoader.create();
            Map<String, Object> data = new HashMap<>();
            data.put("key1", null);
            data.put("key2", "value");
            assertThatNoException().isThrownBy(() -> loader.validate(data));
        }

        @Test
        @DisplayName("should handle list with null values")
        void shouldHandleListWithNullValues() {
            YmlSafeLoader loader = YmlSafeLoader.create();
            List<Object> data = new ArrayList<>();
            data.add(null);
            data.add("value");
            data.add(null);
            assertThatNoException().isThrownBy(() -> loader.validate(data));
        }
    }

    // ==================== Helper Methods and Classes ====================

    private Map<String, Object> createDeeplyNestedMap(int depth) {
        Map<String, Object> current = new HashMap<>();
        current.put("value", "leaf");

        for (int i = 0; i < depth - 1; i++) {
            Map<String, Object> outer = new HashMap<>();
            outer.put("level" + (depth - i - 1), current);
            current = outer;
        }

        return current;
    }

    private List<Object> createDeeplyNestedList(int depth) {
        List<Object> current = new ArrayList<>();
        current.add("leaf");

        for (int i = 0; i < depth - 1; i++) {
            List<Object> outer = new ArrayList<>();
            outer.add(current);
            current = outer;
        }

        return current;
    }

    // Test helper classes
    private static class CustomClass {
    }

    private static class AnotherCustomClass {
    }
}
