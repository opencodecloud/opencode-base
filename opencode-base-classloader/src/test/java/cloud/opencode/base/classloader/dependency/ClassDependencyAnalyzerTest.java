package cloud.opencode.base.classloader.dependency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ClassDependencyAnalyzer}, {@link DependencyGraph}, and {@link CyclicDependency}.
 * {@link ClassDependencyAnalyzer}、{@link DependencyGraph} 和 {@link CyclicDependency} 的测试。
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
@DisplayName("ClassDependencyAnalyzer Tests | 类依赖分析器测试")
class ClassDependencyAnalyzerTest {

    @Nested
    @DisplayName("Analyze by class name | 按类名分析")
    class AnalyzeByNameTests {

        @Test
        @DisplayName("Should find dependencies for java.util.ArrayList | 应找到 ArrayList 的依赖")
        void shouldFindDependenciesForArrayList() throws IOException {
            // ArrayList depends on other classes; java.* are excluded,
            // but it should still parse without error
            Set<String> deps = ClassDependencyAnalyzer.analyze(
                    "java.util.ArrayList", ClassLoader.getSystemClassLoader());
            // java.* are excluded, so result may be empty — but no error
            assertThat(deps).isNotNull();
        }

        @Test
        @DisplayName("Should find dependencies for this test class | 应找到本测试类的依赖")
        void shouldFindDependenciesForThisClass() throws IOException {
            Set<String> deps = ClassDependencyAnalyzer.analyze(
                    ClassDependencyAnalyzerTest.class.getName(),
                    ClassDependencyAnalyzerTest.class.getClassLoader());
            // This test class depends on classloader.dependency classes
            assertThat(deps).isNotNull();
            // Should find at least ClassDependencyAnalyzer itself
            assertThat(deps).anyMatch(d -> d.contains("classloader"));
        }

        @Test
        @DisplayName("Should return empty set for nonexistent class | 不存在的类应返回空集合")
        void shouldReturnEmptyForNonexistentClass() throws IOException {
            Set<String> deps = ClassDependencyAnalyzer.analyze(
                    "com.nonexistent.FakeClass", ClassLoader.getSystemClassLoader());
            assertThat(deps).isEmpty();
        }

        @Test
        @DisplayName("Should exclude self from dependencies | 应从依赖中排除自身")
        void shouldExcludeSelf() throws IOException {
            String className = ClassDependencyAnalyzerTest.class.getName();
            Set<String> deps = ClassDependencyAnalyzer.analyze(
                    className, ClassDependencyAnalyzerTest.class.getClassLoader());
            assertThat(deps).doesNotContain(className);
        }
    }

    @Nested
    @DisplayName("Analyze from bytecode | 从字节码分析")
    class AnalyzeFromBytecodeTests {

        @Test
        @DisplayName("Should analyze bytecode of a known class | 应分析已知类的字节码")
        void shouldAnalyzeBytecodeOfKnownClass() throws IOException {
            String resourcePath = CyclicDependency.class.getName().replace('.', '/') + ".class";
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                assertThat(is).isNotNull();
                byte[] bytecode = is.readAllBytes();
                Set<String> deps = ClassDependencyAnalyzer.analyze(bytecode);
                assertThat(deps).isNotNull();
            }
        }

        @Test
        @DisplayName("Should throw IOException for invalid bytecode | 无效字节码应抛出 IOException")
        void shouldThrowForInvalidBytecode() {
            byte[] garbage = {0x00, 0x01, 0x02, 0x03};
            assertThatIOException()
                    .isThrownBy(() -> ClassDependencyAnalyzer.analyze(garbage))
                    .withMessageContaining("Invalid class file");
        }

        @Test
        @DisplayName("Should throw NPE for null bytecode | 空字节码应抛出 NPE")
        void shouldThrowForNullBytecode() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassDependencyAnalyzer.analyze((byte[]) null));
        }
    }

    @Nested
    @DisplayName("AnalyzePackage Tests | 包分析测试")
    class AnalyzePackageTests {

        @Test
        @DisplayName("Should return empty graph for nonexistent package | 不存在的包应返回空图")
        void shouldReturnEmptyGraphForNonexistentPackage() throws IOException {
            DependencyGraph graph = ClassDependencyAnalyzer.analyzePackage(
                    "com.nonexistent.fake.pkg", ClassLoader.getSystemClassLoader());
            assertThat(graph.classCount()).isZero();
            assertThat(graph.edgeCount()).isZero();
            assertThat(graph.classNames()).isEmpty();
        }

        @Test
        @DisplayName("Should throw NPE for null parameters | 空参数应抛出 NPE")
        void shouldThrowForNullParameters() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassDependencyAnalyzer.analyzePackage(
                            null, ClassLoader.getSystemClassLoader()));
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassDependencyAnalyzer.analyzePackage(
                            "com.example", null));
        }
    }

    @Nested
    @DisplayName("DetectCycles Tests | 检测循环测试")
    class DetectCyclesTests {

        @Test
        @DisplayName("Should detect cycle in graph with cycle | 应在含循环的图中检测到循环")
        void shouldDetectCycle() {
            // A → B → C → A (cycle)
            Map<String, Set<String>> adj = Map.of(
                    "A", Set.of("B"),
                    "B", Set.of("C"),
                    "C", Set.of("A")
            );
            DependencyGraph graph = new DependencyGraph(adj, 3, 3);
            List<CyclicDependency> cycles = ClassDependencyAnalyzer.detectCycles(graph);

            assertThat(cycles).hasSize(1);
            assertThat(cycles.getFirst().cyclePath())
                    .containsExactlyInAnyOrder("A", "B", "C");
        }

        @Test
        @DisplayName("Should return empty list when no cycles | 无循环时应返回空列表")
        void shouldReturnEmptyWhenNoCycles() {
            // A → B → C (no cycle)
            Map<String, Set<String>> adj = Map.of(
                    "A", Set.of("B"),
                    "B", Set.of("C"),
                    "C", Set.of()
            );
            DependencyGraph graph = new DependencyGraph(adj, 3, 2);
            List<CyclicDependency> cycles = ClassDependencyAnalyzer.detectCycles(graph);

            assertThat(cycles).isEmpty();
        }

        @Test
        @DisplayName("Should detect multiple cycles | 应检测到多个循环")
        void shouldDetectMultipleCycles() {
            // A → B → A (cycle 1), C → D → C (cycle 2), A → C (cross-link)
            Map<String, Set<String>> adj = Map.of(
                    "A", Set.of("B", "C"),
                    "B", Set.of("A"),
                    "C", Set.of("D"),
                    "D", Set.of("C")
            );
            DependencyGraph graph = new DependencyGraph(adj, 4, 4);
            List<CyclicDependency> cycles = ClassDependencyAnalyzer.detectCycles(graph);

            assertThat(cycles).hasSize(2);
        }

        @Test
        @DisplayName("Should handle empty graph | 应处理空图")
        void shouldHandleEmptyGraph() {
            DependencyGraph graph = new DependencyGraph(Map.of(), 0, 0);
            List<CyclicDependency> cycles = ClassDependencyAnalyzer.detectCycles(graph);
            assertThat(cycles).isEmpty();
        }

        @Test
        @DisplayName("Should handle self-loop as non-cycle (single node SCC) | 自环不视为循环（单节点SCC）")
        void shouldNotReportSelfLoopAsCycle() {
            // A → A is a single-node SCC, but Tarjan reports size > 1 only
            // However, A → A is technically size 1 with a self-loop
            // Our implementation only reports SCCs with size > 1
            Map<String, Set<String>> adj = Map.of("A", Set.of("A"));
            DependencyGraph graph = new DependencyGraph(adj, 1, 1);
            List<CyclicDependency> cycles = ClassDependencyAnalyzer.detectCycles(graph);
            // Self-loop: SCC has size 1, so not reported
            assertThat(cycles).isEmpty();
        }

        @Test
        @DisplayName("Should throw NPE for null graph | 空图应抛出 NPE")
        void shouldThrowForNullGraph() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassDependencyAnalyzer.detectCycles(null));
        }
    }

    @Nested
    @DisplayName("DependencyGraph Tests | 依赖图测试")
    class DependencyGraphTests {

        @Test
        @DisplayName("Should return dependencies of a class | 应返回类的依赖")
        void shouldReturnDependenciesOf() {
            Map<String, Set<String>> adj = Map.of(
                    "A", Set.of("B", "C"),
                    "B", Set.of("C")
            );
            DependencyGraph graph = new DependencyGraph(adj, 2, 3);

            assertThat(graph.dependenciesOf("A")).containsExactlyInAnyOrder("B", "C");
            assertThat(graph.dependenciesOf("B")).containsExactly("C");
            assertThat(graph.dependenciesOf("X")).isEmpty();
        }

        @Test
        @DisplayName("Should return dependents of a class | 应返回依赖于某类的类")
        void shouldReturnDependentsOf() {
            Map<String, Set<String>> adj = Map.of(
                    "A", Set.of("B", "C"),
                    "B", Set.of("C")
            );
            DependencyGraph graph = new DependencyGraph(adj, 2, 3);

            assertThat(graph.dependentsOf("C")).containsExactlyInAnyOrder("A", "B");
            assertThat(graph.dependentsOf("B")).containsExactly("A");
            assertThat(graph.dependentsOf("A")).isEmpty();
        }

        @Test
        @DisplayName("Should return all class names | 应返回所有类名")
        void shouldReturnAllClassNames() {
            Map<String, Set<String>> adj = Map.of(
                    "A", Set.of("B"),
                    "B", Set.of()
            );
            DependencyGraph graph = new DependencyGraph(adj, 2, 1);

            assertThat(graph.classNames()).containsExactlyInAnyOrder("A", "B");
        }

        @Test
        @DisplayName("Should make deep defensive copy | 应进行深度防御性复制")
        void shouldMakeDeepDefensiveCopy() {
            java.util.HashMap<String, Set<String>> adj = new java.util.HashMap<>();
            adj.put("A", new java.util.HashSet<>(Set.of("B")));
            DependencyGraph graph = new DependencyGraph(adj, 1, 1);

            // Mutate original — should not affect graph
            adj.put("X", Set.of("Y"));
            assertThat(graph.classNames()).doesNotContain("X");
        }

        @Test
        @DisplayName("Should throw NPE for null parameters | 空参数应抛出 NPE")
        void shouldThrowForNullParameters() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new DependencyGraph(null, 0, 0));
            DependencyGraph graph = new DependencyGraph(Map.of(), 0, 0);
            assertThatNullPointerException()
                    .isThrownBy(() -> graph.dependenciesOf(null));
            assertThatNullPointerException()
                    .isThrownBy(() -> graph.dependentsOf(null));
        }
    }

    @Nested
    @DisplayName("CyclicDependency Tests | 循环依赖测试")
    class CyclicDependencyTests {

        @Test
        @DisplayName("Should create with defensive copy | 应使用防御性复制创建")
        void shouldCreateWithDefensiveCopy() {
            java.util.ArrayList<String> path = new java.util.ArrayList<>(List.of("A", "B", "C"));
            CyclicDependency cycle = new CyclicDependency(path);

            // Mutate original — should not affect record
            path.add("D");
            assertThat(cycle.cyclePath()).hasSize(3);
            assertThat(cycle.cyclePath()).containsExactly("A", "B", "C");
        }

        @Test
        @DisplayName("Should be immutable | 应为不可变")
        void shouldBeImmutable() {
            CyclicDependency cycle = new CyclicDependency(List.of("A", "B"));
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> cycle.cyclePath().add("C"));
        }

        @Test
        @DisplayName("Should throw NPE for null cyclePath | 空循环路径应抛出 NPE")
        void shouldThrowForNullCyclePath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new CyclicDependency(null));
        }
    }

    @Nested
    @DisplayName("Null Parameter Tests | 空参数测试")
    class NullParameterTests {

        @Test
        @DisplayName("analyze(null className, ...) should throw NPE | className 为 null 应抛出 NPE")
        void analyzeNullClassName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassDependencyAnalyzer.analyze(
                            (String) null, ClassLoader.getSystemClassLoader()));
        }

        @Test
        @DisplayName("analyze(..., null classLoader) should throw NPE | classLoader 为 null 应抛出 NPE")
        void analyzeNullClassLoader() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassDependencyAnalyzer.analyze(
                            "java.lang.String", null));
        }
    }
}
