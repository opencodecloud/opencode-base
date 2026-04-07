package cloud.opencode.base.tree.benchmark;

import cloud.opencode.base.tree.DefaultTreeNode;
import cloud.opencode.base.tree.operation.TreeMerger;
import cloud.opencode.base.tree.operation.TreeSorter;
import cloud.opencode.base.tree.operation.TreeStatistics;
import cloud.opencode.base.tree.operation.TreeUtil;
import cloud.opencode.base.tree.path.PathFinder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Performance benchmark for V1.0.3 tree operations.
 * 树操作 V1.0.3 性能基准测试。
 *
 * <p>Uses wall-clock timing to verify operations complete within acceptable bounds
 * on a 10K-node tree. Not a JMH microbenchmark (no JMH dependency), but provides
 * regression guards against O(n^2) or worse degradation.</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.3
 */
@DisplayName("Tree Operation Benchmark Tests")
class TreeOperationBenchmark {

    private static final int NODE_COUNT = 10_000;
    private static final int MAX_CHILDREN = 5;
    private static List<DefaultTreeNode<Long>> roots;
    private static List<DefaultTreeNode<Long>> roots2;

    @BeforeAll
    static void buildTree() {
        roots = buildWideTree(NODE_COUNT, MAX_CHILDREN, 1L);
        roots2 = buildWideTree(NODE_COUNT, MAX_CHILDREN, 100_001L);
    }

    private static List<DefaultTreeNode<Long>> buildWideTree(int nodeCount, int maxChildren, long startId) {
        DefaultTreeNode<Long> root = new DefaultTreeNode<>(startId, 0L, "Root");
        root.setChildren(new ArrayList<>());
        List<DefaultTreeNode<Long>> parentLevel = new ArrayList<>();
        parentLevel.add(root);

        long nextId = startId + 1;
        int created = 1;
        // Build level by level to create a wide, shallow tree (depth ~6 for 10K nodes with 5 children)
        while (created < nodeCount && !parentLevel.isEmpty()) {
            List<DefaultTreeNode<Long>> nextLevel = new ArrayList<>();
            for (DefaultTreeNode<Long> parent : parentLevel) {
                for (int c = 0; c < maxChildren && created < nodeCount; c++) {
                    DefaultTreeNode<Long> child = new DefaultTreeNode<>(nextId++, parent.getId(), "N" + nextId);
                    child.setChildren(new ArrayList<>());
                    parent.getChildren().add(child);
                    nextLevel.add(child);
                    created++;
                }
            }
            parentLevel = nextLevel;
        }
        return new ArrayList<>(List.of(root));
    }

    @Test
    @DisplayName("TreeStatistics.of should complete in < 200ms for 10K nodes")
    void statisticsShouldBefast() {
        long start = System.nanoTime();
        TreeStatistics stats = TreeStatistics.of(roots);
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        assertThat(stats.nodeCount()).isGreaterThan(1000);
        assertThat(elapsed).as("TreeStatistics.of elapsed ms").isLessThan(200);
    }

    @Test
    @DisplayName("TreeSorter.sort should complete in < 500ms for 10K nodes")
    void sortShouldBeFast() {
        // Make mutable copies
        List<DefaultTreeNode<Long>> copy = buildWideTree(NODE_COUNT, MAX_CHILDREN, 1L);

        long start = System.nanoTime();
        TreeSorter.sort(copy, Comparator.comparing(DefaultTreeNode<Long>::getName));
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        assertThat(elapsed).as("TreeSorter.sort elapsed ms").isLessThan(500);
    }

    @Test
    @DisplayName("TreeSorter.isSorted should complete in < 200ms for 10K nodes")
    void isSortedShouldBeFast() {
        long start = System.nanoTime();
        TreeSorter.isSorted(roots, Comparator.comparing(DefaultTreeNode<Long>::getName));
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        assertThat(elapsed).as("TreeSorter.isSorted elapsed ms").isLessThan(200);
    }

    @Test
    @DisplayName("TreeMerger.mergeKeepLeft should complete in < 500ms for 10K nodes")
    void mergeShouldBeFast() {
        long start = System.nanoTime();
        List<DefaultTreeNode<Long>> merged = TreeMerger.mergeKeepLeft(
                new ArrayList<>(roots), new ArrayList<>(roots2));
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        assertThat(merged).isNotEmpty();
        assertThat(elapsed).as("TreeMerger.mergeKeepLeft elapsed ms").isLessThan(500);
    }

    @Test
    @DisplayName("PathFinder.findLowestCommonAncestor should complete in < 200ms")
    void lcaShouldBeFast() {
        // Find two leaf-ish nodes
        List<DefaultTreeNode<Long>> leaves = TreeUtil.getLeaves(roots);
        assertThat(leaves).isNotEmpty();
        Long id1 = leaves.getFirst().getId();
        Long id2 = leaves.get(Math.min(leaves.size() - 1, 100)).getId();

        long start = System.nanoTime();
        var lca = PathFinder.findLowestCommonAncestor(roots, id1, id2);
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        assertThat(lca).isPresent();
        assertThat(elapsed).as("LCA elapsed ms").isLessThan(200);
    }
}
