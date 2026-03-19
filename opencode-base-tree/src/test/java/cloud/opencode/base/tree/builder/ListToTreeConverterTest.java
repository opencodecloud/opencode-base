package cloud.opencode.base.tree.builder;

import cloud.opencode.base.tree.DefaultTreeNode;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ListToTreeConverterTest Tests
 * ListToTreeConverterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("ListToTreeConverter Tests")
class ListToTreeConverterTest {

    @Nested
    @DisplayName("Convert Tests")
    class ConvertTests {

        @Test
        @DisplayName("convert should create tree from flat list")
        void convertShouldCreateTreeFromFlatList() {
            List<DefaultTreeNode<Long>> nodes = Arrays.asList(
                new DefaultTreeNode<>(1L, 0L, "Root"),
                new DefaultTreeNode<>(2L, 1L, "Child1"),
                new DefaultTreeNode<>(3L, 1L, "Child2"),
                new DefaultTreeNode<>(4L, 2L, "Grandchild")
            );

            List<DefaultTreeNode<Long>> roots = ListToTreeConverter.convert(nodes);

            assertThat(roots).hasSize(1);
            assertThat(roots.get(0).getId()).isEqualTo(1L);
            assertThat(roots.get(0).getChildren()).hasSize(2);
        }

        @Test
        @DisplayName("convert with rootId should use specified root")
        void convertWithRootIdShouldUseSpecifiedRoot() {
            List<DefaultTreeNode<Long>> nodes = Arrays.asList(
                new DefaultTreeNode<>(1L, 10L, "Root"),
                new DefaultTreeNode<>(2L, 1L, "Child")
            );

            List<DefaultTreeNode<Long>> roots = ListToTreeConverter.convert(nodes, 10L);

            assertThat(roots).hasSize(1);
            assertThat(roots.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("convert should handle empty list")
        void convertShouldHandleEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = Collections.emptyList();
            List<DefaultTreeNode<Long>> roots = ListToTreeConverter.convert(emptyList);

            assertThat(roots).isEmpty();
        }

        @Test
        @DisplayName("convert should handle null list")
        void convertShouldHandleNullList() {
            List<DefaultTreeNode<Long>> roots = ListToTreeConverter.convert(null);

            assertThat(roots).isEmpty();
        }

        @Test
        @DisplayName("convert should handle multiple roots")
        void convertShouldHandleMultipleRoots() {
            List<DefaultTreeNode<Long>> nodes = Arrays.asList(
                new DefaultTreeNode<>(1L, 0L, "Root1"),
                new DefaultTreeNode<>(2L, 0L, "Root2")
            );

            List<DefaultTreeNode<Long>> roots = ListToTreeConverter.convert(nodes);

            assertThat(roots).hasSize(2);
        }
    }

    @Nested
    @DisplayName("ConvertSorted Tests")
    class ConvertSortedTests {

        @Test
        @DisplayName("convertSorted should sort children by comparator")
        void convertSortedShouldSortChildrenByComparator() {
            List<DefaultTreeNode<Long>> nodes = Arrays.asList(
                new DefaultTreeNode<>(1L, 0L, "Root"),
                new DefaultTreeNode<>(3L, 1L, "C"),
                new DefaultTreeNode<>(2L, 1L, "A"),
                new DefaultTreeNode<>(4L, 1L, "B")
            );

            List<DefaultTreeNode<Long>> roots = ListToTreeConverter.convertSorted(
                nodes, 0L, Comparator.comparing(DefaultTreeNode::getName));

            assertThat(roots.get(0).getChildren().get(0).getName()).isEqualTo("A");
            assertThat(roots.get(0).getChildren().get(1).getName()).isEqualTo("B");
            assertThat(roots.get(0).getChildren().get(2).getName()).isEqualTo("C");
        }
    }

    @Nested
    @DisplayName("Convert With Extractors Tests")
    class ConvertWithExtractorsTests {

        @Test
        @DisplayName("convert with extractors should transform items")
        void convertWithExtractorsShouldTransformItems() {
            record SimpleItem(Long id, Long parentId, String name) {}

            List<SimpleItem> items = List.of(
                new SimpleItem(1L, 0L, "Root"),
                new SimpleItem(2L, 1L, "Child")
            );

            List<DefaultTreeNode<Long>> roots = ListToTreeConverter.convert(
                items,
                SimpleItem::id,
                SimpleItem::parentId,
                item -> new DefaultTreeNode<>(item.id(), item.parentId(), item.name())
            );

            assertThat(roots).hasSize(1);
            assertThat(roots.get(0).getName()).isEqualTo("Root");
        }

        @Test
        @DisplayName("convert with extractors should handle empty list")
        void convertWithExtractorsShouldHandleEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = Collections.emptyList();
            List<DefaultTreeNode<Long>> roots = ListToTreeConverter.convert(
                emptyList,
                DefaultTreeNode::getId,
                DefaultTreeNode::getParentId,
                n -> n
            );

            assertThat(roots).isEmpty();
        }
    }

    @Nested
    @DisplayName("Root Detection Tests")
    class RootDetectionTests {

        @Test
        @DisplayName("should treat null parentId as root")
        void shouldTreatNullParentIdAsRoot() {
            List<DefaultTreeNode<Long>> nodes = List.of(
                new DefaultTreeNode<>(1L, null, "Root")
            );

            List<DefaultTreeNode<Long>> roots = ListToTreeConverter.convert(nodes);

            assertThat(roots).hasSize(1);
        }

        @Test
        @DisplayName("should treat 0 parentId as root for Long")
        void shouldTreat0ParentIdAsRootForLong() {
            List<DefaultTreeNode<Long>> nodes = List.of(
                new DefaultTreeNode<>(1L, 0L, "Root")
            );

            List<DefaultTreeNode<Long>> roots = ListToTreeConverter.convert(nodes);

            assertThat(roots).hasSize(1);
        }

        @Test
        @DisplayName("should treat empty string parentId as root")
        void shouldTreatEmptyStringParentIdAsRoot() {
            List<DefaultTreeNode<String>> nodes = List.of(
                new DefaultTreeNode<>("node1", "", "Root")
            );

            List<DefaultTreeNode<String>> roots = ListToTreeConverter.convert(nodes);

            assertThat(roots).hasSize(1);
        }

        @Test
        @DisplayName("should treat '0' string parentId as root")
        void shouldTreatZeroStringParentIdAsRoot() {
            List<DefaultTreeNode<String>> nodes = List.of(
                new DefaultTreeNode<>("node1", "0", "Root")
            );

            List<DefaultTreeNode<String>> roots = ListToTreeConverter.convert(nodes);

            assertThat(roots).hasSize(1);
        }
    }
}
