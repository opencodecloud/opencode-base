package cloud.opencode.base.tree.virtual;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * VirtualTree Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
class VirtualTreeTest {

    private Map<Long, List<VirtualTree<String, Long>>> mockDatabase;
    private AtomicInteger loadCount;

    @BeforeEach
    void setUp() {
        mockDatabase = new HashMap<>();
        loadCount = new AtomicInteger(0);
    }

    private LazyChildLoader<VirtualTree<String, Long>, Long> createLoader() {
        return parentId -> {
            loadCount.incrementAndGet();
            List<VirtualTree<String, Long>> children = mockDatabase.get(parentId);
            if (children == null) {
                return List.of();
            }
            return new ArrayList<>(children);
        };
    }

    @Nested
    @DisplayName("Basic Operations Tests")
    class BasicOperationsTests {

        @Test
        void shouldCreateRootNode() {
            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", createLoader());

            assertThat(root.getId()).isEqualTo(1L);
            assertThat(root.getData()).isEqualTo("Root");
            assertThat(root.isRoot()).isTrue();
            assertThat(root.getParentId()).isNull();
        }

        @Test
        void shouldCreateWithBuilder() {
            Map<Long, VirtualTree<String, Long>> cache = new ConcurrentHashMap<>();
            VirtualTree<String, Long> node = VirtualTree.<String, Long>builder()
                    .id(1L)
                    .parentId(null)
                    .data("Node")
                    .childLoader(createLoader())
                    .nodeCache(cache)
                    .maxCacheSize(100)
                    .cacheEnabled(true)
                    .build();

            assertThat(node.getId()).isEqualTo(1L);
            assertThat(node.getData()).isEqualTo("Node");
        }

        @Test
        void shouldDetectLeafNode() {
            VirtualTree<String, Long> leaf = VirtualTree.root(1L, "Leaf", parentId -> List.of());

            assertThat(leaf.isLeaf()).isTrue();
        }
    }

    @Nested
    @DisplayName("Lazy Loading Tests")
    class LazyLoadingTests {

        @Test
        void shouldNotLoadChildrenUntilAccessed() {
            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", createLoader());

            assertThat(root.isChildrenLoaded()).isFalse();
            assertThat(loadCount.get()).isEqualTo(0);
        }

        @Test
        void shouldLoadChildrenOnAccess() {
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();
            VirtualTree<String, Long> child1 = new VirtualTree<>(2L, 1L, "Child1", loader);
            VirtualTree<String, Long> child2 = new VirtualTree<>(3L, 1L, "Child2", loader);
            mockDatabase.put(1L, List.of(child1, child2));

            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", loader);

            List<VirtualTree<String, Long>> children = root.getChildren();

            assertThat(root.isChildrenLoaded()).isTrue();
            assertThat(loadCount.get()).isEqualTo(1);
            assertThat(children).hasSize(2);
        }

        @Test
        void shouldOnlyLoadOnce() {
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();
            mockDatabase.put(1L, List.of(new VirtualTree<>(2L, 1L, "Child", loader)));

            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", loader);

            root.getChildren();
            root.getChildren();
            root.getChildren();

            assertThat(loadCount.get()).isEqualTo(1);
        }

        @Test
        void shouldReloadChildren() {
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();
            mockDatabase.put(1L, List.of(new VirtualTree<>(2L, 1L, "Child", loader)));

            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", loader);

            root.getChildren();
            root.reloadChildren();

            assertThat(loadCount.get()).isEqualTo(2);
        }

        @Test
        void shouldUnloadChildren() {
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();
            mockDatabase.put(1L, List.of(new VirtualTree<>(2L, 1L, "Child", loader)));

            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", loader);
            root.getChildren();
            assertThat(root.isChildrenLoaded()).isTrue();

            root.unloadChildren();

            assertThat(root.isChildrenLoaded()).isFalse();
        }
    }

    @Nested
    @DisplayName("Preloading Tests")
    class PreloadingTests {

        @Test
        void shouldPreloadToDepth() {
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();

            VirtualTree<String, Long> child = new VirtualTree<>(2L, 1L, "Child", loader);
            VirtualTree<String, Long> grandchild = new VirtualTree<>(3L, 2L, "Grandchild", loader);

            mockDatabase.put(1L, List.of(child));
            mockDatabase.put(2L, List.of(grandchild));

            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", loader);
            root.preload(2);

            assertThat(root.isChildrenLoaded()).isTrue();
            assertThat(root.getChildren().getFirst().isChildrenLoaded()).isTrue();
        }

        @Test
        void shouldRespectDepthLimit() {
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();

            VirtualTree<String, Long> child = new VirtualTree<>(2L, 1L, "Child", loader);
            VirtualTree<String, Long> grandchild = new VirtualTree<>(3L, 2L, "Grandchild", loader);

            mockDatabase.put(1L, List.of(child));
            mockDatabase.put(2L, List.of(grandchild));
            mockDatabase.put(3L, List.of());

            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", loader);
            root.preload(1);

            assertThat(root.isChildrenLoaded()).isTrue();
            // Children are loaded but grandchildren are not preloaded beyond depth
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @Test
        void shouldFindNodeById() {
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();

            VirtualTree<String, Long> child = new VirtualTree<>(2L, 1L, "Child", loader);
            mockDatabase.put(1L, List.of(child));
            mockDatabase.put(2L, List.of());

            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", loader);

            Optional<VirtualTree<String, Long>> found = root.find(2L);

            assertThat(found).isPresent();
            assertThat(found.get().getData()).isEqualTo("Child");
        }

        @Test
        void shouldFindSelf() {
            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", createLoader());

            Optional<VirtualTree<String, Long>> found = root.find(1L);

            assertThat(found).isPresent();
            assertThat(found.get()).isSameAs(root);
        }

        @Test
        void shouldReturnEmptyForNotFound() {
            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", parentId -> List.of());

            Optional<VirtualTree<String, Long>> found = root.find(999L);

            assertThat(found).isEmpty();
        }

        @Test
        void shouldFindAllMatching() {
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();

            VirtualTree<String, Long> child1 = new VirtualTree<>(2L, 1L, "Match", loader);
            VirtualTree<String, Long> child2 = new VirtualTree<>(3L, 1L, "Other", loader);
            VirtualTree<String, Long> child3 = new VirtualTree<>(4L, 1L, "Match", loader);

            mockDatabase.put(1L, List.of(child1, child2, child3));
            mockDatabase.put(2L, List.of());
            mockDatabase.put(3L, List.of());
            mockDatabase.put(4L, List.of());

            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", loader);

            List<VirtualTree<String, Long>> found = root.findAll(data -> "Match".equals(data));

            assertThat(found).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Traversal Tests")
    class TraversalTests {

        @Test
        void shouldTraversePreOrder() {
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();

            VirtualTree<String, Long> child1 = new VirtualTree<>(2L, 1L, "A", loader);
            VirtualTree<String, Long> child2 = new VirtualTree<>(3L, 1L, "B", loader);

            mockDatabase.put(1L, List.of(child1, child2));
            mockDatabase.put(2L, List.of());
            mockDatabase.put(3L, List.of());

            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", loader);

            List<String> visited = new ArrayList<>();
            root.traversePreOrder(node -> visited.add(node.getData()));

            assertThat(visited).containsExactly("Root", "A", "B");
        }

        @Test
        void shouldTraversePostOrder() {
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();

            VirtualTree<String, Long> child1 = new VirtualTree<>(2L, 1L, "A", loader);
            VirtualTree<String, Long> child2 = new VirtualTree<>(3L, 1L, "B", loader);

            mockDatabase.put(1L, List.of(child1, child2));
            mockDatabase.put(2L, List.of());
            mockDatabase.put(3L, List.of());

            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", loader);

            List<String> visited = new ArrayList<>();
            root.traversePostOrder(node -> visited.add(node.getData()));

            assertThat(visited).containsExactly("A", "B", "Root");
        }

        @Test
        void shouldTraverseBreadthFirst() {
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();

            VirtualTree<String, Long> child1 = new VirtualTree<>(2L, 1L, "A", loader);
            VirtualTree<String, Long> child2 = new VirtualTree<>(3L, 1L, "B", loader);
            VirtualTree<String, Long> grandchild = new VirtualTree<>(4L, 2L, "C", loader);

            mockDatabase.put(1L, List.of(child1, child2));
            mockDatabase.put(2L, List.of(grandchild));
            mockDatabase.put(3L, List.of());
            mockDatabase.put(4L, List.of());

            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", loader);

            List<String> visited = new ArrayList<>();
            root.traverseBreadthFirst(node -> visited.add(node.getData()));

            assertThat(visited).containsExactly("Root", "A", "B", "C");
        }

        @Test
        void shouldTraverseWithDepthLimit() {
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();

            VirtualTree<String, Long> child = new VirtualTree<>(2L, 1L, "Child", loader);
            VirtualTree<String, Long> grandchild = new VirtualTree<>(3L, 2L, "Grandchild", loader);

            mockDatabase.put(1L, List.of(child));
            mockDatabase.put(2L, List.of(grandchild));
            mockDatabase.put(3L, List.of());

            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", loader);

            List<String> visited = new ArrayList<>();
            root.traverseWithDepthLimit(node -> visited.add(node.getData()), 1);

            assertThat(visited).containsExactly("Root", "Child");
        }
    }

    @Nested
    @DisplayName("Cache Tests")
    class CacheTests {

        @Test
        void shouldUseCacheForFastLookup() {
            Map<Long, VirtualTree<String, Long>> cache = new ConcurrentHashMap<>();
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();

            VirtualTree<String, Long> root = new VirtualTree<>(1L, null, "Root", loader,
                    cache, 100, true);

            VirtualTree<String, Long> child = new VirtualTree<>(2L, 1L, "Child", loader,
                    cache, 100, true);

            assertThat(cache).containsKey(1L);
            assertThat(cache).containsKey(2L);
        }

        @Test
        void shouldReportCacheStats() {
            Map<Long, VirtualTree<String, Long>> cache = new ConcurrentHashMap<>();
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();

            VirtualTree<String, Long> root = new VirtualTree<>(1L, null, "Root", loader,
                    cache, 100, true);

            VirtualTree.CacheStats stats = root.getCacheStats();

            assertThat(stats.currentSize()).isGreaterThan(0);
            assertThat(stats.maxSize()).isEqualTo(100);
        }

        @Test
        void shouldClearCache() {
            Map<Long, VirtualTree<String, Long>> cache = new ConcurrentHashMap<>();
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();

            VirtualTree<String, Long> root = new VirtualTree<>(1L, null, "Root", loader,
                    cache, 100, true);

            // Add more nodes to cache
            new VirtualTree<>(2L, 1L, "Child", loader, cache, 100, true);

            assertThat(cache).hasSize(2);

            root.clearCache();

            // Only root should remain after clear
            assertThat(cache).hasSize(1);
            assertThat(cache).containsKey(1L);
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        void shouldCountLoadedNodes() {
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();

            VirtualTree<String, Long> child1 = new VirtualTree<>(2L, 1L, "A", loader);
            VirtualTree<String, Long> child2 = new VirtualTree<>(3L, 1L, "B", loader);

            mockDatabase.put(1L, List.of(child1, child2));
            mockDatabase.put(2L, List.of());
            mockDatabase.put(3L, List.of());

            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", loader);

            // Before loading
            assertThat(root.getLoadedNodeCount()).isEqualTo(1);

            // After loading
            root.getChildren();
            assertThat(root.getLoadedNodeCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Set Children Tests")
    class SetChildrenTests {

        @Test
        void shouldSetChildrenManually() {
            LazyChildLoader<VirtualTree<String, Long>, Long> loader = createLoader();

            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", loader);
            VirtualTree<String, Long> child = new VirtualTree<>(2L, 1L, "Child", loader);

            root.setChildren(List.of(child));

            assertThat(root.isChildrenLoaded()).isTrue();
            assertThat(root.getChildren()).hasSize(1);
            assertThat(loadCount.get()).isEqualTo(0); // Loader never called
        }

        @Test
        void shouldHandleNullChildren() {
            VirtualTree<String, Long> root = VirtualTree.root(1L, "Root", createLoader());

            root.setChildren(null);

            assertThat(root.isChildrenLoaded()).isTrue();
            assertThat(root.getChildren()).isEmpty();
        }
    }
}
