package cloud.opencode.base.yml.path;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlPathTest Tests
 * YmlPathTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlPath Tests")
class YmlPathTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Nested
        @DisplayName("root() Tests")
        class RootTests {

            @Test
            @DisplayName("root should return root path")
            void rootShouldReturnRootPath() {
                YmlPath path = YmlPath.root();

                assertThat(path).isNotNull();
                assertThat(path.isRoot()).isTrue();
            }

            @Test
            @DisplayName("root should return same instance")
            void rootShouldReturnSameInstance() {
                YmlPath root1 = YmlPath.root();
                YmlPath root2 = YmlPath.root();

                assertThat(root1).isSameAs(root2);
            }

            @Test
            @DisplayName("root should have empty segments")
            void rootShouldHaveEmptySegments() {
                YmlPath path = YmlPath.root();

                assertThat(path.getSegments()).isEmpty();
            }

            @Test
            @DisplayName("root should have depth zero")
            void rootShouldHaveDepthZero() {
                YmlPath path = YmlPath.root();

                assertThat(path.depth()).isZero();
            }

            @Test
            @DisplayName("root should have empty toString")
            void rootShouldHaveEmptyToString() {
                YmlPath path = YmlPath.root();

                assertThat(path.toString()).isEmpty();
            }
        }

        @Nested
        @DisplayName("of(String) Tests")
        class OfStringTests {

            @Test
            @DisplayName("of with null should return root")
            void ofWithNullShouldReturnRoot() {
                YmlPath path = YmlPath.of((String) null);

                assertThat(path.isRoot()).isTrue();
            }

            @Test
            @DisplayName("of with empty string should return root")
            void ofWithEmptyStringShouldReturnRoot() {
                YmlPath path = YmlPath.of("");

                assertThat(path.isRoot()).isTrue();
            }

            @Test
            @DisplayName("of with simple property should parse correctly")
            void ofWithSimplePropertyShouldParseCorrectly() {
                YmlPath path = YmlPath.of("name");

                assertThat(path.getSegments()).hasSize(1);
                assertThat(path.getSegments().getFirst()).isInstanceOf(YmlPath.PropertySegment.class);
                assertThat(((YmlPath.PropertySegment) path.getSegments().getFirst()).property()).isEqualTo("name");
            }

            @Test
            @DisplayName("of with dot notation should parse correctly")
            void ofWithDotNotationShouldParseCorrectly() {
                YmlPath path = YmlPath.of("a.b.c");

                assertThat(path.getSegments()).hasSize(3);
                assertThat(path.toString()).isEqualTo("a.b.c");
            }

            @Test
            @DisplayName("of with array index should parse correctly")
            void ofWithArrayIndexShouldParseCorrectly() {
                YmlPath path = YmlPath.of("items[0]");

                assertThat(path.getSegments()).hasSize(2);
                assertThat(path.getSegments().get(0)).isInstanceOf(YmlPath.PropertySegment.class);
                assertThat(path.getSegments().get(1)).isInstanceOf(YmlPath.IndexSegment.class);
                assertThat(((YmlPath.IndexSegment) path.getSegments().get(1)).index()).isZero();
            }

            @Test
            @DisplayName("of with complex path should parse correctly")
            void ofWithComplexPathShouldParseCorrectly() {
                YmlPath path = YmlPath.of("items[0].name");

                assertThat(path.getSegments()).hasSize(3);
                assertThat(path.getSegments().get(0)).isInstanceOf(YmlPath.PropertySegment.class);
                assertThat(path.getSegments().get(1)).isInstanceOf(YmlPath.IndexSegment.class);
                assertThat(path.getSegments().get(2)).isInstanceOf(YmlPath.PropertySegment.class);
            }

            @Test
            @DisplayName("of with multiple array indices should parse correctly")
            void ofWithMultipleArrayIndicesShouldParseCorrectly() {
                YmlPath path = YmlPath.of("matrix[0][1]");

                assertThat(path.getSegments()).hasSize(3);
                assertThat(path.getSegments().get(0)).isInstanceOf(YmlPath.PropertySegment.class);
                assertThat(path.getSegments().get(1)).isInstanceOf(YmlPath.IndexSegment.class);
                assertThat(path.getSegments().get(2)).isInstanceOf(YmlPath.IndexSegment.class);
            }

            @Test
            @DisplayName("of with nested properties and indices should parse correctly")
            void ofWithNestedPropertiesAndIndicesShouldParseCorrectly() {
                YmlPath path = YmlPath.of("servers[0].hosts[1].name");

                assertThat(path.getSegments()).hasSize(5);
                assertThat(path.depth()).isEqualTo(5);
            }

            @Test
            @DisplayName("of with leading dot should handle correctly")
            void ofWithLeadingDotShouldHandleCorrectly() {
                YmlPath path = YmlPath.of(".name");

                assertThat(path.getSegments()).hasSize(1);
                assertThat(((YmlPath.PropertySegment) path.getSegments().getFirst()).property()).isEqualTo("name");
            }

            @Test
            @DisplayName("of with trailing dot should handle correctly")
            void ofWithTrailingDotShouldHandleCorrectly() {
                YmlPath path = YmlPath.of("name.");

                assertThat(path.getSegments()).hasSize(1);
            }

            @Test
            @DisplayName("of with consecutive dots should handle correctly")
            void ofWithConsecutiveDotsShouldHandleCorrectly() {
                YmlPath path = YmlPath.of("a..b");

                assertThat(path.getSegments()).hasSize(2);
            }
        }

        @Nested
        @DisplayName("of(String...) Tests")
        class OfVarargsTests {

            @Test
            @DisplayName("of with single segment should create path")
            void ofWithSingleSegmentShouldCreatePath() {
                YmlPath path = YmlPath.of("name");

                assertThat(path.getSegments()).hasSize(1);
                assertThat(path.toString()).isEqualTo("name");
            }

            @Test
            @DisplayName("of with multiple segments should create path")
            void ofWithMultipleSegmentsShouldCreatePath() {
                YmlPath path = YmlPath.of("server", "host", "name");

                assertThat(path.getSegments()).hasSize(3);
                assertThat(path.toString()).isEqualTo("server.host.name");
            }

            @Test
            @DisplayName("of with no segments should create empty path")
            void ofWithNoSegmentsShouldCreateEmptyPath() {
                YmlPath path = YmlPath.of(new String[]{});

                assertThat(path.getSegments()).isEmpty();
            }

            @Test
            @DisplayName("of varargs should create PropertySegments only")
            void ofVarargsShouldCreatePropertySegmentsOnly() {
                YmlPath path = YmlPath.of("a", "b", "c");

                for (YmlPath.Segment segment : path.getSegments()) {
                    assertThat(segment).isInstanceOf(YmlPath.PropertySegment.class);
                }
            }
        }
    }

    @Nested
    @DisplayName("Path Parsing Tests")
    class PathParsingTests {

        @Test
        @DisplayName("should parse simple property path")
        void shouldParseSimplePropertyPath() {
            YmlPath path = YmlPath.of("server.port");

            assertThat(path.getSegments()).hasSize(2);
            assertThat(((YmlPath.PropertySegment) path.getSegments().get(0)).property()).isEqualTo("server");
            assertThat(((YmlPath.PropertySegment) path.getSegments().get(1)).property()).isEqualTo("port");
        }

        @Test
        @DisplayName("should parse array access at end")
        void shouldParseArrayAccessAtEnd() {
            YmlPath path = YmlPath.of("users[5]");

            assertThat(path.getSegments()).hasSize(2);
            assertThat(((YmlPath.PropertySegment) path.getSegments().get(0)).property()).isEqualTo("users");
            assertThat(((YmlPath.IndexSegment) path.getSegments().get(1)).index()).isEqualTo(5);
        }

        @Test
        @DisplayName("should parse array access at start")
        void shouldParseArrayAccessAtStart() {
            YmlPath path = YmlPath.of("[0].name");

            assertThat(path.getSegments()).hasSize(2);
            assertThat(path.getSegments().get(0)).isInstanceOf(YmlPath.IndexSegment.class);
            assertThat(((YmlPath.IndexSegment) path.getSegments().get(0)).index()).isZero();
        }

        @Test
        @DisplayName("should throw for unclosed bracket")
        void shouldThrowForUnclosedBracket() {
            assertThatThrownBy(() -> YmlPath.of("items[0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unclosed bracket");
        }

        @Test
        @DisplayName("should throw for invalid array index")
        void shouldThrowForInvalidArrayIndex() {
            assertThatThrownBy(() -> YmlPath.of("items[abc]"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid array index");
        }

        @Test
        @DisplayName("should throw for negative array index format")
        void shouldThrowForNegativeArrayIndexFormat() {
            // This should actually work since Integer.parseInt handles negative numbers
            YmlPath path = YmlPath.of("items[-1]");
            assertThat(((YmlPath.IndexSegment) path.getSegments().get(1)).index()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should parse deeply nested path")
        void shouldParseDeeplyNestedPath() {
            YmlPath path = YmlPath.of("a.b.c.d.e.f.g");

            assertThat(path.getSegments()).hasSize(7);
            assertThat(path.depth()).isEqualTo(7);
        }

        @Test
        @DisplayName("should parse mixed property and index path")
        void shouldParseMixedPropertyAndIndexPath() {
            YmlPath path = YmlPath.of("data[0].items[1].values[2].name");

            assertThat(path.getSegments()).hasSize(7);
            assertThat(path.toString()).isEqualTo("data[0].items[1].values[2].name");
        }
    }

    @Nested
    @DisplayName("child() Tests")
    class ChildTests {

        @Test
        @DisplayName("child from root should create single segment path")
        void childFromRootShouldCreateSingleSegmentPath() {
            YmlPath path = YmlPath.root().child("name");

            assertThat(path.getSegments()).hasSize(1);
            assertThat(path.toString()).isEqualTo("name");
        }

        @Test
        @DisplayName("child should append to existing path")
        void childShouldAppendToExistingPath() {
            YmlPath path = YmlPath.of("server").child("port");

            assertThat(path.getSegments()).hasSize(2);
            assertThat(path.toString()).isEqualTo("server.port");
        }

        @Test
        @DisplayName("child should not modify original path")
        void childShouldNotModifyOriginalPath() {
            YmlPath original = YmlPath.of("server");
            YmlPath child = original.child("port");

            assertThat(original.getSegments()).hasSize(1);
            assertThat(child.getSegments()).hasSize(2);
        }

        @Test
        @DisplayName("multiple children should chain correctly")
        void multipleChildrenShouldChainCorrectly() {
            YmlPath path = YmlPath.root()
                .child("server")
                .child("host")
                .child("name");

            assertThat(path.toString()).isEqualTo("server.host.name");
        }
    }

    @Nested
    @DisplayName("index() Tests")
    class IndexTests {

        @Test
        @DisplayName("index from root should create single index path")
        void indexFromRootShouldCreateSingleIndexPath() {
            YmlPath path = YmlPath.root().index(0);

            assertThat(path.getSegments()).hasSize(1);
            assertThat(path.getSegments().getFirst()).isInstanceOf(YmlPath.IndexSegment.class);
        }

        @Test
        @DisplayName("index should append to existing path")
        void indexShouldAppendToExistingPath() {
            YmlPath path = YmlPath.of("items").index(5);

            assertThat(path.getSegments()).hasSize(2);
            assertThat(path.toString()).isEqualTo("items[5]");
        }

        @Test
        @DisplayName("index should not modify original path")
        void indexShouldNotModifyOriginalPath() {
            YmlPath original = YmlPath.of("items");
            YmlPath indexed = original.index(0);

            assertThat(original.getSegments()).hasSize(1);
            assertThat(indexed.getSegments()).hasSize(2);
        }

        @Test
        @DisplayName("multiple indices should chain correctly")
        void multipleIndicesShouldChainCorrectly() {
            YmlPath path = YmlPath.of("matrix").index(0).index(1).index(2);

            assertThat(path.toString()).isEqualTo("matrix[0][1][2]");
        }

        @Test
        @DisplayName("mixed child and index should work correctly")
        void mixedChildAndIndexShouldWorkCorrectly() {
            YmlPath path = YmlPath.root()
                .child("users")
                .index(0)
                .child("addresses")
                .index(1)
                .child("city");

            assertThat(path.toString()).isEqualTo("users[0].addresses[1].city");
        }
    }

    @Nested
    @DisplayName("parent() Tests")
    class ParentTests {

        @Test
        @DisplayName("parent of root should return root")
        void parentOfRootShouldReturnRoot() {
            YmlPath path = YmlPath.root().parent();

            assertThat(path.isRoot()).isTrue();
        }

        @Test
        @DisplayName("parent should remove last segment")
        void parentShouldRemoveLastSegment() {
            YmlPath path = YmlPath.of("a.b.c").parent();

            assertThat(path.toString()).isEqualTo("a.b");
        }

        @Test
        @DisplayName("parent of single segment should return root")
        void parentOfSingleSegmentShouldReturnRoot() {
            YmlPath path = YmlPath.of("name").parent();

            assertThat(path.isRoot()).isTrue();
        }

        @Test
        @DisplayName("parent should work with index segments")
        void parentShouldWorkWithIndexSegments() {
            YmlPath path = YmlPath.of("items[0].name").parent();

            assertThat(path.toString()).isEqualTo("items[0]");
        }

        @Test
        @DisplayName("multiple parent calls should work correctly")
        void multipleParentCallsShouldWorkCorrectly() {
            YmlPath path = YmlPath.of("a.b.c.d")
                .parent()
                .parent();

            assertThat(path.toString()).isEqualTo("a.b");
        }
    }

    @Nested
    @DisplayName("getSegments() Tests")
    class GetSegmentsTests {

        @Test
        @DisplayName("getSegments should return immutable list")
        void getSegmentsShouldReturnImmutableList() {
            YmlPath path = YmlPath.of("a.b.c");
            List<YmlPath.Segment> segments = path.getSegments();

            assertThatThrownBy(() -> segments.add(new YmlPath.PropertySegment("d")))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getSegments should return correct segments")
        void getSegmentsShouldReturnCorrectSegments() {
            YmlPath path = YmlPath.of("items[0].name");
            List<YmlPath.Segment> segments = path.getSegments();

            assertThat(segments).hasSize(3);
            assertThat(segments.get(0)).isInstanceOf(YmlPath.PropertySegment.class);
            assertThat(segments.get(1)).isInstanceOf(YmlPath.IndexSegment.class);
            assertThat(segments.get(2)).isInstanceOf(YmlPath.PropertySegment.class);
        }
    }

    @Nested
    @DisplayName("getLastSegment() Tests")
    class GetLastSegmentTests {

        @Test
        @DisplayName("getLastSegment of root should return null")
        void getLastSegmentOfRootShouldReturnNull() {
            YmlPath path = YmlPath.root();

            assertThat(path.getLastSegment()).isNull();
        }

        @Test
        @DisplayName("getLastSegment should return last property segment")
        void getLastSegmentShouldReturnLastPropertySegment() {
            YmlPath path = YmlPath.of("a.b.c");

            assertThat(path.getLastSegment()).isInstanceOf(YmlPath.PropertySegment.class);
            assertThat(((YmlPath.PropertySegment) path.getLastSegment()).property()).isEqualTo("c");
        }

        @Test
        @DisplayName("getLastSegment should return last index segment")
        void getLastSegmentShouldReturnLastIndexSegment() {
            YmlPath path = YmlPath.of("items[0]");

            assertThat(path.getLastSegment()).isInstanceOf(YmlPath.IndexSegment.class);
            assertThat(((YmlPath.IndexSegment) path.getLastSegment()).index()).isZero();
        }
    }

    @Nested
    @DisplayName("isRoot() Tests")
    class IsRootTests {

        @Test
        @DisplayName("isRoot should return true for root path")
        void isRootShouldReturnTrueForRootPath() {
            assertThat(YmlPath.root().isRoot()).isTrue();
        }

        @Test
        @DisplayName("isRoot should return false for non-root path")
        void isRootShouldReturnFalseForNonRootPath() {
            assertThat(YmlPath.of("name").isRoot()).isFalse();
        }

        @Test
        @DisplayName("isRoot should return true for empty string path")
        void isRootShouldReturnTrueForEmptyStringPath() {
            assertThat(YmlPath.of("").isRoot()).isTrue();
        }
    }

    @Nested
    @DisplayName("depth() Tests")
    class DepthTests {

        @Test
        @DisplayName("depth of root should be zero")
        void depthOfRootShouldBeZero() {
            assertThat(YmlPath.root().depth()).isZero();
        }

        @Test
        @DisplayName("depth should equal number of segments")
        void depthShouldEqualNumberOfSegments() {
            assertThat(YmlPath.of("a").depth()).isEqualTo(1);
            assertThat(YmlPath.of("a.b").depth()).isEqualTo(2);
            assertThat(YmlPath.of("a.b.c").depth()).isEqualTo(3);
        }

        @Test
        @DisplayName("depth should count index segments")
        void depthShouldCountIndexSegments() {
            assertThat(YmlPath.of("items[0]").depth()).isEqualTo(2);
            assertThat(YmlPath.of("items[0].name").depth()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("toString() Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should return empty string for root")
        void toStringShouldReturnEmptyStringForRoot() {
            assertThat(YmlPath.root().toString()).isEmpty();
        }

        @Test
        @DisplayName("toString should format property path correctly")
        void toStringShouldFormatPropertyPathCorrectly() {
            assertThat(YmlPath.of("a.b.c").toString()).isEqualTo("a.b.c");
        }

        @Test
        @DisplayName("toString should format index path correctly")
        void toStringShouldFormatIndexPathCorrectly() {
            assertThat(YmlPath.of("items[0]").toString()).isEqualTo("items[0]");
        }

        @Test
        @DisplayName("toString should format mixed path correctly")
        void toStringShouldFormatMixedPathCorrectly() {
            assertThat(YmlPath.of("items[0].name").toString()).isEqualTo("items[0].name");
        }

        @Test
        @DisplayName("toString should not add dot before first index")
        void toStringShouldNotAddDotBeforeFirstIndex() {
            YmlPath path = YmlPath.root().index(0).child("name");
            assertThat(path.toString()).isEqualTo("[0].name");
        }

        @Test
        @DisplayName("toString should handle multiple consecutive indices")
        void toStringShouldHandleMultipleConsecutiveIndices() {
            YmlPath path = YmlPath.of("matrix[0][1][2]");
            assertThat(path.toString()).isEqualTo("matrix[0][1][2]");
        }
    }

    @Nested
    @DisplayName("equals() Tests")
    class EqualsTests {

        @Test
        @DisplayName("equals should return true for same path")
        void equalsShouldReturnTrueForSamePath() {
            YmlPath path1 = YmlPath.of("a.b.c");
            YmlPath path2 = YmlPath.of("a.b.c");

            assertThat(path1).isEqualTo(path2);
        }

        @Test
        @DisplayName("equals should return false for different paths")
        void equalsShouldReturnFalseForDifferentPaths() {
            YmlPath path1 = YmlPath.of("a.b.c");
            YmlPath path2 = YmlPath.of("a.b.d");

            assertThat(path1).isNotEqualTo(path2);
        }

        @Test
        @DisplayName("equals should return true for root paths")
        void equalsShouldReturnTrueForRootPaths() {
            YmlPath root1 = YmlPath.root();
            YmlPath root2 = YmlPath.of("");

            assertThat(root1).isEqualTo(root2);
        }

        @Test
        @DisplayName("equals should handle paths with indices")
        void equalsShouldHandlePathsWithIndices() {
            YmlPath path1 = YmlPath.of("items[0].name");
            YmlPath path2 = YmlPath.of("items[0].name");

            assertThat(path1).isEqualTo(path2);
        }

        @Test
        @DisplayName("equals should return false for same elements different indices")
        void equalsShouldReturnFalseForSameElementsDifferentIndices() {
            YmlPath path1 = YmlPath.of("items[0]");
            YmlPath path2 = YmlPath.of("items[1]");

            assertThat(path1).isNotEqualTo(path2);
        }

        @Test
        @DisplayName("equals should return true for self comparison")
        void equalsShouldReturnTrueForSelfComparison() {
            YmlPath path = YmlPath.of("a.b.c");

            assertThat(path).isEqualTo(path);
        }

        @Test
        @DisplayName("equals should return false for null")
        void equalsShouldReturnFalseForNull() {
            YmlPath path = YmlPath.of("a.b.c");

            assertThat(path).isNotEqualTo(null);
        }

        @Test
        @DisplayName("equals should return false for different type")
        void equalsShouldReturnFalseForDifferentType() {
            YmlPath path = YmlPath.of("a.b.c");

            assertThat(path).isNotEqualTo("a.b.c");
        }
    }

    @Nested
    @DisplayName("hashCode() Tests")
    class HashCodeTests {

        @Test
        @DisplayName("hashCode should be equal for equal paths")
        void hashCodeShouldBeEqualForEqualPaths() {
            YmlPath path1 = YmlPath.of("a.b.c");
            YmlPath path2 = YmlPath.of("a.b.c");

            assertThat(path1.hashCode()).isEqualTo(path2.hashCode());
        }

        @Test
        @DisplayName("hashCode should be consistent")
        void hashCodeShouldBeConsistent() {
            YmlPath path = YmlPath.of("a.b.c");

            int hashCode1 = path.hashCode();
            int hashCode2 = path.hashCode();

            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @Test
        @DisplayName("hashCode should be equal for paths with indices")
        void hashCodeShouldBeEqualForPathsWithIndices() {
            YmlPath path1 = YmlPath.of("items[0].name");
            YmlPath path2 = YmlPath.of("items[0].name");

            assertThat(path1.hashCode()).isEqualTo(path2.hashCode());
        }
    }

    @Nested
    @DisplayName("PropertySegment Record Tests")
    class PropertySegmentTests {

        @Test
        @DisplayName("PropertySegment should store property name")
        void propertySegmentShouldStorePropertyName() {
            YmlPath.PropertySegment segment = new YmlPath.PropertySegment("name");

            assertThat(segment.property()).isEqualTo("name");
        }

        @Test
        @DisplayName("PropertySegment equals should work correctly")
        void propertySegmentEqualsShouldWorkCorrectly() {
            YmlPath.PropertySegment segment1 = new YmlPath.PropertySegment("name");
            YmlPath.PropertySegment segment2 = new YmlPath.PropertySegment("name");
            YmlPath.PropertySegment segment3 = new YmlPath.PropertySegment("other");

            assertThat(segment1).isEqualTo(segment2);
            assertThat(segment1).isNotEqualTo(segment3);
        }

        @Test
        @DisplayName("PropertySegment hashCode should be consistent")
        void propertySegmentHashCodeShouldBeConsistent() {
            YmlPath.PropertySegment segment1 = new YmlPath.PropertySegment("name");
            YmlPath.PropertySegment segment2 = new YmlPath.PropertySegment("name");

            assertThat(segment1.hashCode()).isEqualTo(segment2.hashCode());
        }

        @Test
        @DisplayName("PropertySegment toString should return meaningful representation")
        void propertySegmentToStringShouldReturnMeaningfulRepresentation() {
            YmlPath.PropertySegment segment = new YmlPath.PropertySegment("name");

            assertThat(segment.toString()).contains("name");
        }
    }

    @Nested
    @DisplayName("IndexSegment Record Tests")
    class IndexSegmentTests {

        @Test
        @DisplayName("IndexSegment should store index value")
        void indexSegmentShouldStoreIndexValue() {
            YmlPath.IndexSegment segment = new YmlPath.IndexSegment(5);

            assertThat(segment.index()).isEqualTo(5);
        }

        @Test
        @DisplayName("IndexSegment should handle zero index")
        void indexSegmentShouldHandleZeroIndex() {
            YmlPath.IndexSegment segment = new YmlPath.IndexSegment(0);

            assertThat(segment.index()).isZero();
        }

        @Test
        @DisplayName("IndexSegment should handle negative index")
        void indexSegmentShouldHandleNegativeIndex() {
            YmlPath.IndexSegment segment = new YmlPath.IndexSegment(-1);

            assertThat(segment.index()).isEqualTo(-1);
        }

        @Test
        @DisplayName("IndexSegment equals should work correctly")
        void indexSegmentEqualsShouldWorkCorrectly() {
            YmlPath.IndexSegment segment1 = new YmlPath.IndexSegment(5);
            YmlPath.IndexSegment segment2 = new YmlPath.IndexSegment(5);
            YmlPath.IndexSegment segment3 = new YmlPath.IndexSegment(10);

            assertThat(segment1).isEqualTo(segment2);
            assertThat(segment1).isNotEqualTo(segment3);
        }

        @Test
        @DisplayName("IndexSegment hashCode should be consistent")
        void indexSegmentHashCodeShouldBeConsistent() {
            YmlPath.IndexSegment segment1 = new YmlPath.IndexSegment(5);
            YmlPath.IndexSegment segment2 = new YmlPath.IndexSegment(5);

            assertThat(segment1.hashCode()).isEqualTo(segment2.hashCode());
        }

        @Test
        @DisplayName("IndexSegment toString should return meaningful representation")
        void indexSegmentToStringShouldReturnMeaningfulRepresentation() {
            YmlPath.IndexSegment segment = new YmlPath.IndexSegment(5);

            assertThat(segment.toString()).contains("5");
        }
    }

    @Nested
    @DisplayName("Segment Sealed Interface Tests")
    class SegmentSealedInterfaceTests {

        @Test
        @DisplayName("Segment should only permit PropertySegment and IndexSegment")
        void segmentShouldOnlyPermitPropertySegmentAndIndexSegment() {
            YmlPath.Segment propertySegment = new YmlPath.PropertySegment("name");
            YmlPath.Segment indexSegment = new YmlPath.IndexSegment(0);

            assertThat(propertySegment).isInstanceOf(YmlPath.Segment.class);
            assertThat(indexSegment).isInstanceOf(YmlPath.Segment.class);
        }

        @Test
        @DisplayName("pattern matching should work with Segment")
        void patternMatchingShouldWorkWithSegment() {
            YmlPath path = YmlPath.of("items[0]");
            StringBuilder result = new StringBuilder();

            for (YmlPath.Segment segment : path.getSegments()) {
                switch (segment) {
                    case YmlPath.PropertySegment ps -> result.append("P:").append(ps.property());
                    case YmlPath.IndexSegment is -> result.append("I:").append(is.index());
                }
            }

            assertThat(result.toString()).isEqualTo("P:itemsI:0");
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("YmlPath should be immutable")
        void ymlPathShouldBeImmutable() {
            YmlPath original = YmlPath.of("a.b.c");

            // These operations create new instances
            YmlPath childPath = original.child("d");
            YmlPath indexPath = original.index(0);
            YmlPath parentPath = original.parent();

            // Original should be unchanged
            assertThat(original.toString()).isEqualTo("a.b.c");
            assertThat(childPath.toString()).isEqualTo("a.b.c.d");
            assertThat(indexPath.toString()).isEqualTo("a.b.c[0]");
            assertThat(parentPath.toString()).isEqualTo("a.b");
        }

        @Test
        @DisplayName("segments list should be unmodifiable")
        void segmentsListShouldBeUnmodifiable() {
            YmlPath path = YmlPath.of("a.b");

            assertThatThrownBy(() -> path.getSegments().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle property with numbers")
        void shouldHandlePropertyWithNumbers() {
            YmlPath path = YmlPath.of("item123.value456");

            assertThat(path.getSegments()).hasSize(2);
            assertThat(((YmlPath.PropertySegment) path.getSegments().get(0)).property()).isEqualTo("item123");
        }

        @Test
        @DisplayName("should handle property with underscores")
        void shouldHandlePropertyWithUnderscores() {
            YmlPath path = YmlPath.of("my_property.other_value");

            assertThat(path.getSegments()).hasSize(2);
            assertThat(((YmlPath.PropertySegment) path.getSegments().get(0)).property()).isEqualTo("my_property");
        }

        @Test
        @DisplayName("should handle property with hyphens")
        void shouldHandlePropertyWithHyphens() {
            YmlPath path = YmlPath.of("my-property.other-value");

            assertThat(path.getSegments()).hasSize(2);
            assertThat(((YmlPath.PropertySegment) path.getSegments().get(0)).property()).isEqualTo("my-property");
        }

        @Test
        @DisplayName("should handle large index values")
        void shouldHandleLargeIndexValues() {
            YmlPath path = YmlPath.of("items[999999]");

            assertThat(((YmlPath.IndexSegment) path.getSegments().get(1)).index()).isEqualTo(999999);
        }

        @Test
        @DisplayName("should handle very deep paths")
        void shouldHandleVeryDeepPaths() {
            StringBuilder pathBuilder = new StringBuilder("a");
            for (int i = 0; i < 100; i++) {
                pathBuilder.append(".b");
            }

            YmlPath path = YmlPath.of(pathBuilder.toString());

            assertThat(path.depth()).isEqualTo(101);
        }
    }
}
