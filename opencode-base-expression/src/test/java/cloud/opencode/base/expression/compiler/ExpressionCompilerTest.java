package cloud.opencode.base.expression.compiler;

import cloud.opencode.base.expression.context.StandardContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ExpressionCompiler Tests
 * ExpressionCompiler 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("ExpressionCompiler Tests | ExpressionCompiler 测试")
class ExpressionCompilerTest {

    @Nested
    @DisplayName("Compile Tests | 编译测试")
    class CompileTests {

        @Test
        @DisplayName("Compile simple expression | 编译简单表达式")
        void testCompileSimple() {
            ExpressionCompiler compiler = ExpressionCompiler.create();
            CompiledExpression expr = compiler.compile("1 + 2");

            assertThat(expr).isNotNull();
            assertThat(expr.getExpressionString()).isEqualTo("1 + 2");
        }

        @Test
        @DisplayName("Compile and evaluate | 编译并求值")
        void testCompileAndEvaluate() {
            ExpressionCompiler compiler = ExpressionCompiler.create();
            CompiledExpression expr = compiler.compile("x + y");

            StandardContext ctx = new StandardContext();
            ctx.setVariable("x", 10);
            ctx.setVariable("y", 20);

            Object result = expr.getValue(ctx);
            assertThat(result).isEqualTo(30);
        }

        @Test
        @DisplayName("Compile with cache hit | 编译缓存命中")
        void testCompileWithCacheHit() {
            ExpressionCompiler compiler = ExpressionCompiler.create();

            CompiledExpression expr1 = compiler.compile("a + b");
            CompiledExpression expr2 = compiler.compile("a + b");

            assertThat(expr1).isSameAs(expr2);
        }

        @Test
        @DisplayName("Compile without cache | 编译不使用缓存")
        void testCompileWithoutCache() {
            ExpressionCompiler compiler = ExpressionCompiler.withoutCache();

            CompiledExpression expr1 = compiler.compile("a + b");
            CompiledExpression expr2 = compiler.compile("a + b");

            // Without cache, should be different instances
            assertThat(expr1).isNotSameAs(expr2);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("Get default compiler | 获取默认编译器")
        void testGetDefault() {
            ExpressionCompiler compiler = ExpressionCompiler.getDefault();
            assertThat(compiler).isNotNull();
            assertThat(compiler.isOptimizationEnabled()).isTrue();
        }

        @Test
        @DisplayName("Create new compiler | 创建新编译器")
        void testCreate() {
            ExpressionCompiler compiler = ExpressionCompiler.create();
            assertThat(compiler).isNotNull();
        }

        @Test
        @DisplayName("Create without cache | 创建无缓存编译器")
        void testWithoutCache() {
            ExpressionCompiler compiler = ExpressionCompiler.withoutCache();
            assertThat(compiler.getCache()).isNull();
        }

        @Test
        @DisplayName("Create without optimization | 创建无优化编译器")
        void testWithoutOptimization() {
            ExpressionCompiler compiler = ExpressionCompiler.withoutOptimization();
            assertThat(compiler.isOptimizationEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder Tests | 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("Build with custom cache size | 使用自定义缓存大小构建")
        void testBuilderCustomCacheSize() {
            ExpressionCompiler compiler = ExpressionCompiler.builder()
                    .cacheSize(500)
                    .build();

            assertThat(compiler.getCache()).isNotNull();
            assertThat(compiler.getCache().maxSize()).isEqualTo(500);
        }

        @Test
        @DisplayName("Build with no cache | 构建无缓存")
        void testBuilderNoCache() {
            ExpressionCompiler compiler = ExpressionCompiler.builder()
                    .noCache()
                    .build();

            assertThat(compiler.getCache()).isNull();
        }

        @Test
        @DisplayName("Build with no optimization | 构建无优化")
        void testBuilderNoOptimization() {
            ExpressionCompiler compiler = ExpressionCompiler.builder()
                    .noOptimization()
                    .build();

            assertThat(compiler.isOptimizationEnabled()).isFalse();
        }

        @Test
        @DisplayName("Build with custom optimizer | 使用自定义优化器构建")
        void testBuilderCustomOptimizer() {
            Optimizer customOptimizer = Optimizer.builder()
                    .constantFolding(false)
                    .build();

            ExpressionCompiler compiler = ExpressionCompiler.builder()
                    .optimizer(customOptimizer)
                    .build();

            assertThat(compiler.getOptimizer()).isEqualTo(customOptimizer);
        }
    }

    @Nested
    @DisplayName("Cache Operations Tests | 缓存操作测试")
    class CacheOperationsTests {

        @Test
        @DisplayName("Check cached | 检查缓存")
        void testIsCached() {
            ExpressionCompiler compiler = ExpressionCompiler.create();

            assertThat(compiler.isCached("x + y")).isFalse();

            compiler.compile("x + y");

            assertThat(compiler.isCached("x + y")).isTrue();
        }

        @Test
        @DisplayName("Clear cache | 清除缓存")
        void testClearCache() {
            ExpressionCompiler compiler = ExpressionCompiler.create();

            compiler.compile("a + b");
            assertThat(compiler.isCached("a + b")).isTrue();

            compiler.clearCache();
            assertThat(compiler.isCached("a + b")).isFalse();
        }
    }

    @Nested
    @DisplayName("Optimization Tests | 优化测试")
    class OptimizationTests {

        @Test
        @DisplayName("Compile with explicit optimization | 使用显式优化编译")
        void testCompileWithExplicitOptimization() {
            ExpressionCompiler compiler = ExpressionCompiler.create();

            // Compile without optimization
            CompiledExpression expr = compiler.compile("2 + 3", false);
            assertThat(expr).isNotNull();
        }

        @Test
        @DisplayName("Compile without cache | 编译不使用缓存")
        void testCompileWithoutCacheMethod() {
            ExpressionCompiler compiler = ExpressionCompiler.create();

            CompiledExpression expr1 = compiler.compileWithoutCache("a + b");
            CompiledExpression expr2 = compiler.compileWithoutCache("a + b");

            // Should not be same instance even though same expression
            assertThat(expr1).isNotSameAs(expr2);
        }
    }
}
