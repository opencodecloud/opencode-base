package cloud.opencode.base.tree;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TreePrinterTest Tests
 * TreePrinterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreePrinter Tests")
class TreePrinterTest {

    @Nested
    @DisplayName("Print Tests")
    class PrintTests {

        @Test
        @DisplayName("print should format tree with branches")
        void printShouldFormatTreeWithBranches() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "Child1");
            DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "Child2");
            root.setChildren(new ArrayList<>(List.of(child1, child2)));

            String result = TreePrinter.print(List.of(root));

            assertThat(result).contains("Root");
            assertThat(result).contains("Child1");
            assertThat(result).contains("Child2");
        }

        @Test
        @DisplayName("print with formatter should use custom formatter")
        void printWithFormatterShouldUseCustomFormatter() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");

            String result = TreePrinter.print(List.of(root), node -> "ID:" + node.getId());

            assertThat(result).contains("ID:1");
        }

        @Test
        @DisplayName("print should handle empty list")
        void printShouldHandleEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = List.of();

            String result = TreePrinter.print(emptyList);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("print should handle multiple roots")
        void printShouldHandleMultipleRoots() {
            DefaultTreeNode<Long> root1 = new DefaultTreeNode<>(1L, null, "Root1");
            DefaultTreeNode<Long> root2 = new DefaultTreeNode<>(2L, null, "Root2");

            String result = TreePrinter.print(List.of(root1, root2));

            assertThat(result).contains("Root1");
            assertThat(result).contains("Root2");
        }
    }

    @Nested
    @DisplayName("PrintSingle Tests")
    class PrintSingleTests {

        @Test
        @DisplayName("printSingle should print single tree")
        void printSingleShouldPrintSingleTree() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Single");

            String result = TreePrinter.printSingle(root);

            assertThat(result).contains("Single");
        }
    }

    @Nested
    @DisplayName("PrintSimple Tests")
    class PrintSimpleTests {

        @Test
        @DisplayName("printSimple should use indentation")
        void printSimpleShouldUseIndentation() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            root.setChildren(new ArrayList<>(List.of(child)));

            String result = TreePrinter.printSimple(List.of(root), "  ", DefaultTreeNode::getName);

            assertThat(result).contains("Root");
            assertThat(result).contains("  Child");
        }
    }

    @Nested
    @DisplayName("PrintToStream Tests")
    class PrintToStreamTests {

        @Test
        @DisplayName("printToStream should write to output stream")
        void printToStreamShouldWriteToOutputStream() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);

            TreePrinter.printToStream(List.of(root), ps, DefaultTreeNode::getName);

            assertThat(baos.toString()).contains("Root");
        }
    }

    @Nested
    @DisplayName("GetStats Tests")
    class GetStatsTests {

        @Test
        @DisplayName("getStats should return tree statistics")
        void getStatsShouldReturnTreeStatistics() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child1 = new DefaultTreeNode<>(2L, 1L, "Child1");
            DefaultTreeNode<Long> child2 = new DefaultTreeNode<>(3L, 1L, "Child2");
            root.setChildren(new ArrayList<>(List.of(child1, child2)));

            String stats = TreePrinter.getStats(List.of(root));

            assertThat(stats).contains("Nodes: 3");
            assertThat(stats).contains("Leaves: 2");
        }

        @Test
        @DisplayName("getStats should handle empty tree")
        void getStatsShouldHandleEmptyTree() {
            List<DefaultTreeNode<Long>> emptyList = List.of();

            String stats = TreePrinter.getStats(emptyList);

            assertThat(stats).contains("Nodes: 0");
        }
    }
}
