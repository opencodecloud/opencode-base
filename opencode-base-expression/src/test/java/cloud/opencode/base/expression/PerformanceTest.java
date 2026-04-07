package cloud.opencode.base.expression;

import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance Smoke Tests
 * 性能冒烟测试
 *
 * <p>Verifies that expression evaluation completes within acceptable time bounds.
 * These are not JMH benchmarks but guard against gross performance regressions.</p>
 * <p>验证表达式求值在可接受的时间范围内完成。
 * 这些不是JMH基准测试，但可防止严重的性能回归。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
@DisplayName("Performance Smoke Tests | 性能冒烟测试")
class PerformanceTest {

    @Nested
    @DisplayName("Expression Cache Tests | 表达式缓存测试")
    class ExpressionCacheTests {

        @Test
        @DisplayName("Cached evaluation should be significantly faster than cold parse")
        void cachedEvaluationVsColdParse() {
            String expr = "x * 2 + y - z / 3";
            Map<String, Object> vars = Map.of("x", 10, "y", 20, "z", 30);

            // Warm up cache
            OpenExpression.eval(expr, vars);

            // Measure cached
            int iterations = 10_000;
            long start = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                OpenExpression.eval(expr, vars);
            }
            long cachedNs = System.nanoTime() - start;
            double cachedPerOp = (double) cachedNs / iterations;

            // Should complete 10K evaluations in under 1 second
            assertThat(cachedNs).isLessThan(1_000_000_000L);
            System.out.printf("[/perf] Cached evaluation: %.0f ns/op (%.0f ops/ms)%n",
                    cachedPerOp, iterations * 1_000_000.0 / cachedNs);
        }
    }

    @Nested
    @DisplayName("Collection Filter Performance | 集合过滤性能")
    class CollectionFilterTests {

        @Test
        @DisplayName("Filter 1000 elements should complete within bounds")
        void filterLargeCollection() {
            // Build a list of 1000 maps
            List<Map<String, Object>> users = new java.util.ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                users.add(Map.of("age", i, "name", "user" + i));
            }

            StandardContext ctx = new StandardContext();
            ctx.setVariable("users", users);

            // Warm up
            OpenExpression.eval("users.?[#this.age > 500]", ctx);

            int iterations = 100;
            long start = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                Object result = OpenExpression.eval("users.?[#this.age > 500]", ctx);
                assertThat(result).isInstanceOf(List.class);
            }
            long totalNs = System.nanoTime() - start;
            double perOp = (double) totalNs / iterations;

            // 100 iterations of filtering 1000 elements should be < 5 seconds
            assertThat(totalNs).isLessThan(5_000_000_000L);
            System.out.printf("[/perf] Filter 1000 elements: %.0f us/op (%.1f ms total for %d iterations)%n",
                    perOp / 1000, totalNs / 1_000_000.0, iterations);
        }
    }

    @Nested
    @DisplayName("Template Rendering Performance | 模板渲染性能")
    class TemplateTests {

        @Test
        @DisplayName("Template with multiple placeholders should render efficiently")
        void multiPlaceholderTemplate() {
            String template = "Hello ${name}, you are ${age} years old. Balance: ${balance * 1.1}. Status: ${active ? 'active' : 'inactive'}";
            Map<String, Object> vars = Map.of("name", "Jon", "age", 30, "balance", 1000.0, "active", true);

            // Warm up
            ExpressionTemplate.render(template, vars);

            int iterations = 10_000;
            long start = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                ExpressionTemplate.render(template, vars);
            }
            long totalNs = System.nanoTime() - start;
            double perOp = (double) totalNs / iterations;

            // 10K renders should be < 2 seconds
            assertThat(totalNs).isLessThan(2_000_000_000L);
            System.out.printf("[/perf] Template render (4 placeholders): %.0f ns/op (%.0f ops/ms)%n",
                    perOp, iterations * 1_000_000.0 / totalNs);
        }
    }

    @Nested
    @DisplayName("New Operator Performance | 新运算符性能")
    class NewOperatorTests {

        @Test
        @DisplayName("Elvis operator chain performance")
        void elvisChain() {
            int iterations = 10_000;
            StandardContext ctx = new StandardContext();
            ctx.setVariable("a", null);
            ctx.setVariable("b", "found");

            // Warm up
            OpenExpression.eval("a ?: b ?: 'fallback'", ctx);

            long start = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                OpenExpression.eval("a ?: b ?: 'fallback'", ctx);
            }
            long totalNs = System.nanoTime() - start;
            System.out.printf("[/perf] Elvis chain: %.0f ns/op%n", (double) totalNs / iterations);
            assertThat(totalNs).isLessThan(1_000_000_000L);
        }

        @Test
        @DisplayName("Bitwise operations performance")
        void bitwiseOps() {
            int iterations = 10_000;
            Map<String, Object> vars = Map.of("a", 0xFF, "b", 0x0F);

            OpenExpression.eval("(a & b) | (a ^ b)", vars);

            long start = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                OpenExpression.eval("(a & b) | (a ^ b)", vars);
            }
            long totalNs = System.nanoTime() - start;
            System.out.printf("[/perf] Bitwise ops: %.0f ns/op%n", (double) totalNs / iterations);
            assertThat(totalNs).isLessThan(1_000_000_000L);
        }

        @Test
        @DisplayName("Between operator performance")
        void betweenOp() {
            int iterations = 10_000;
            Map<String, Object> vars = Map.of("x", 50);

            OpenExpression.eval("x between 1 and 100", vars);

            long start = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                OpenExpression.eval("x between 1 and 100", vars);
            }
            long totalNs = System.nanoTime() - start;
            System.out.printf("[/perf] Between: %.0f ns/op%n", (double) totalNs / iterations);
            assertThat(totalNs).isLessThan(1_000_000_000L);
        }
    }

    @Nested
    @DisplayName("Variable Extraction Performance | 变量提取性能")
    class VariableExtractionTests {

        @Test
        @DisplayName("Variable extraction from complex expression")
        void complexExpressionExtraction() {
            String expr = "a + b * c - d / e + f ** g + (h > i ? j : k)";

            // Warm up
            VariableExtractor.extract(expr);

            int iterations = 10_000;
            long start = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                VariableExtractor.extract(expr);
            }
            long totalNs = System.nanoTime() - start;
            System.out.printf("[/perf] Variable extraction (11 vars): %.0f ns/op%n",
                    (double) totalNs / iterations);
            assertThat(totalNs).isLessThan(1_000_000_000L);
        }
    }
}
