package cloud.opencode.base.graph.algorithm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * UnionFind 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
@DisplayName("UnionFind 测试")
class UnionFindTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建空并查集")
        void testEmptyCollection() {
            UnionFind<String> uf = new UnionFind<>(List.of());

            assertThat(uf.componentCount()).isEqualTo(0);
            assertThat(uf.components()).isEmpty();
        }

        @Test
        @DisplayName("创建单元素并查集")
        void testSingleElement() {
            UnionFind<String> uf = new UnionFind<>(List.of("A"));

            assertThat(uf.componentCount()).isEqualTo(1);
            assertThat(uf.find("A")).isEqualTo("A");
        }

        @Test
        @DisplayName("null集合抛出异常")
        void testNullCollection() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new UnionFind<>(null))
                    .withMessageContaining("null");
        }

        @Test
        @DisplayName("包含null元素抛出异常")
        void testNullElement() {
            List<String> elements = new java.util.ArrayList<>();
            elements.add("A");
            elements.add(null);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new UnionFind<>(elements))
                    .withMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("find测试")
    class FindTests {

        @Test
        @DisplayName("单元素find返回自身")
        void testFindSingleton() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B", "C"));

            assertThat(uf.find("A")).isEqualTo("A");
            assertThat(uf.find("B")).isEqualTo("B");
        }

        @Test
        @DisplayName("路径压缩：多次find返回相同代表")
        void testPathCompression() {
            UnionFind<Integer> uf = new UnionFind<>(List.of(1, 2, 3, 4, 5));
            uf.union(1, 2);
            uf.union(2, 3);
            uf.union(3, 4);
            uf.union(4, 5);

            // All should have the same representative after find
            Integer root = uf.find(5);
            assertThat(uf.find(1)).isEqualTo(root);
            assertThat(uf.find(2)).isEqualTo(root);
            assertThat(uf.find(3)).isEqualTo(root);
            assertThat(uf.find(4)).isEqualTo(root);
            assertThat(uf.find(5)).isEqualTo(root);
        }

        @Test
        @DisplayName("null元素find抛出异常")
        void testFindNull() {
            UnionFind<String> uf = new UnionFind<>(List.of("A"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> uf.find(null));
        }

        @Test
        @DisplayName("未知元素find抛出异常")
        void testFindUnknown() {
            UnionFind<String> uf = new UnionFind<>(List.of("A"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> uf.find("Z"))
                    .withMessageContaining("Z");
        }
    }

    @Nested
    @DisplayName("union测试")
    class UnionTests {

        @Test
        @DisplayName("合并两个不同集合返回true")
        void testUnionDifferentSets() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B"));

            assertThat(uf.union("A", "B")).isTrue();
            assertThat(uf.connected("A", "B")).isTrue();
        }

        @Test
        @DisplayName("合并相同集合返回false（自合并）")
        void testSelfUnion() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B"));

            assertThat(uf.union("A", "A")).isFalse();
        }

        @Test
        @DisplayName("重复合并返回false")
        void testDuplicateUnion() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B"));

            assertThat(uf.union("A", "B")).isTrue();
            assertThat(uf.union("A", "B")).isFalse();
            assertThat(uf.union("B", "A")).isFalse();
        }

        @Test
        @DisplayName("按秩合并保持树平衡")
        void testUnionByRank() {
            UnionFind<Integer> uf = new UnionFind<>(List.of(1, 2, 3, 4));

            // Create two pairs
            uf.union(1, 2); // rank of root becomes 1
            uf.union(3, 4); // rank of root becomes 1

            // Union the two pairs
            uf.union(1, 3);

            // All four should be connected
            assertThat(uf.connected(1, 2)).isTrue();
            assertThat(uf.connected(1, 3)).isTrue();
            assertThat(uf.connected(1, 4)).isTrue();
            assertThat(uf.connected(2, 4)).isTrue();
            assertThat(uf.componentCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("null元素union抛出异常")
        void testUnionNull() {
            UnionFind<String> uf = new UnionFind<>(List.of("A"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> uf.union(null, "A"));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> uf.union("A", null));
        }

        @Test
        @DisplayName("未知元素union抛出异常")
        void testUnionUnknown() {
            UnionFind<String> uf = new UnionFind<>(List.of("A"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> uf.union("A", "Z"));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> uf.union("Z", "A"));
        }
    }

    @Nested
    @DisplayName("connected测试")
    class ConnectedTests {

        @Test
        @DisplayName("初始状态各元素不连通")
        void testInitiallyDisconnected() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B", "C"));

            assertThat(uf.connected("A", "B")).isFalse();
            assertThat(uf.connected("B", "C")).isFalse();
            assertThat(uf.connected("A", "C")).isFalse();
        }

        @Test
        @DisplayName("自身连通")
        void testSelfConnected() {
            UnionFind<String> uf = new UnionFind<>(List.of("A"));

            assertThat(uf.connected("A", "A")).isTrue();
        }

        @Test
        @DisplayName("传递连通")
        void testTransitiveConnection() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B", "C"));

            uf.union("A", "B");
            uf.union("B", "C");

            assertThat(uf.connected("A", "C")).isTrue();
        }
    }

    @Nested
    @DisplayName("componentCount测试")
    class ComponentCountTests {

        @Test
        @DisplayName("初始分量数等于元素数")
        void testInitialCount() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B", "C", "D"));

            assertThat(uf.componentCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("合并后分量数减少")
        void testCountAfterUnion() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B", "C", "D"));

            uf.union("A", "B");
            assertThat(uf.componentCount()).isEqualTo(3);

            uf.union("C", "D");
            assertThat(uf.componentCount()).isEqualTo(2);

            uf.union("A", "C");
            assertThat(uf.componentCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("重复合并不减少分量数")
        void testCountDuplicateUnion() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B"));

            uf.union("A", "B");
            assertThat(uf.componentCount()).isEqualTo(1);

            uf.union("A", "B");
            assertThat(uf.componentCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("componentOf测试")
    class ComponentOfTests {

        @Test
        @DisplayName("单元素分量")
        void testSingletonComponent() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B"));

            assertThat(uf.componentOf("A")).containsExactly("A");
        }

        @Test
        @DisplayName("合并后的分量")
        void testMergedComponent() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B", "C", "D"));

            uf.union("A", "B");
            uf.union("B", "C");

            assertThat(uf.componentOf("A")).containsExactlyInAnyOrder("A", "B", "C");
            assertThat(uf.componentOf("B")).containsExactlyInAnyOrder("A", "B", "C");
            assertThat(uf.componentOf("C")).containsExactlyInAnyOrder("A", "B", "C");
            assertThat(uf.componentOf("D")).containsExactly("D");
        }

        @Test
        @DisplayName("null元素componentOf抛出异常")
        void testComponentOfNull() {
            UnionFind<String> uf = new UnionFind<>(List.of("A"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> uf.componentOf(null));
        }

        @Test
        @DisplayName("未知元素componentOf抛出异常")
        void testComponentOfUnknown() {
            UnionFind<String> uf = new UnionFind<>(List.of("A"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> uf.componentOf("Z"));
        }
    }

    @Nested
    @DisplayName("components测试")
    class ComponentsTests {

        @Test
        @DisplayName("初始状态每个元素一个分量")
        void testInitialComponents() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B", "C"));

            List<Set<String>> components = uf.components();
            assertThat(components).hasSize(3);
            assertThat(components).containsExactlyInAnyOrder(
                    Set.of("A"), Set.of("B"), Set.of("C")
            );
        }

        @Test
        @DisplayName("所有元素合并为一个分量")
        void testAllInOneComponent() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B", "C"));

            uf.union("A", "B");
            uf.union("B", "C");

            List<Set<String>> components = uf.components();
            assertThat(components).hasSize(1);
            assertThat(components.getFirst()).containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("多个分量")
        void testMultipleComponents() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B", "C", "D", "E"));

            uf.union("A", "B");
            uf.union("C", "D");

            List<Set<String>> components = uf.components();
            assertThat(components).hasSize(3);
            assertThat(components).containsExactlyInAnyOrder(
                    Set.of("A", "B"), Set.of("C", "D"), Set.of("E")
            );
        }

        @Test
        @DisplayName("返回不可修改的列表和集合")
        void testImmutableResult() {
            UnionFind<String> uf = new UnionFind<>(List.of("A", "B"));
            uf.union("A", "B");

            List<Set<String>> components = uf.components();
            assertThatThrownBy(() -> components.add(Set.of("X")))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> components.getFirst().add("X"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Integer类型测试")
    class IntegerTypeTests {

        @Test
        @DisplayName("整数类型并查集")
        void testIntegerUnionFind() {
            UnionFind<Integer> uf = new UnionFind<>(List.of(1, 2, 3, 4, 5));

            uf.union(1, 2);
            uf.union(3, 4);
            uf.union(4, 5);

            assertThat(uf.connected(1, 2)).isTrue();
            assertThat(uf.connected(3, 5)).isTrue();
            assertThat(uf.connected(1, 3)).isFalse();
            assertThat(uf.componentCount()).isEqualTo(2);
        }
    }
}
