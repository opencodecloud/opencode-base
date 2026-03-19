package cloud.opencode.base.expression.compiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CompiledExpressionCache Tests
 * CompiledExpressionCache 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("CompiledExpressionCache Tests | CompiledExpressionCache 测试")
class CompiledExpressionCacheTest {

    private CompiledExpressionCache cache;

    @BeforeEach
    void setup() {
        cache = new CompiledExpressionCache(10);
    }

    @Nested
    @DisplayName("Constructor Tests | 构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor sets size 1000 | 默认构造函数设置大小 1000")
        void testDefaultConstructor() {
            CompiledExpressionCache defaultCache = new CompiledExpressionCache();
            assertThat(defaultCache.maxSize()).isEqualTo(1000);
        }

        @Test
        @DisplayName("constructor with size | 带大小的构造函数")
        void testConstructorWithSize() {
            assertThat(cache.maxSize()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests | 工厂方法测试")
    class FactoryMethodsTests {

        @Test
        @DisplayName("global returns singleton | global 返回单例")
        void testGlobal() {
            CompiledExpressionCache global1 = CompiledExpressionCache.global();
            CompiledExpressionCache global2 = CompiledExpressionCache.global();
            assertThat(global1).isSameAs(global2);
        }

        @Test
        @DisplayName("create creates new instance | create 创建新实例")
        void testCreate() {
            CompiledExpressionCache cache1 = CompiledExpressionCache.create(50);
            CompiledExpressionCache cache2 = CompiledExpressionCache.create(50);
            assertThat(cache1).isNotSameAs(cache2);
            assertThat(cache1.maxSize()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("GetOrCompile Tests | getOrCompile 测试")
    class GetOrCompileTests {

        @Test
        @DisplayName("getOrCompile compiles and caches | getOrCompile 编译并缓存")
        void testGetOrCompileCompilesAndCaches() {
            CompiledExpression expr = cache.getOrCompile("1 + 2", CompiledExpression::compile);
            assertThat(expr).isNotNull();
            assertThat(cache.contains("1 + 2")).isTrue();
        }

        @Test
        @DisplayName("getOrCompile returns cached | getOrCompile 返回缓存")
        void testGetOrCompileReturnsCached() {
            CompiledExpression expr1 = cache.getOrCompile("a + b", CompiledExpression::compile);
            CompiledExpression expr2 = cache.getOrCompile("a + b", CompiledExpression::compile);
            assertThat(expr1).isSameAs(expr2);
        }
    }

    @Nested
    @DisplayName("Get Tests | get 测试")
    class GetTests {

        @Test
        @DisplayName("get returns null for missing | get 对缺失项返回 null")
        void testGetReturnsNull() {
            assertThat(cache.get("not_cached")).isNull();
        }

        @Test
        @DisplayName("get returns cached expression | get 返回缓存的表达式")
        void testGetReturnsCached() {
            cache.getOrCompile("cached", CompiledExpression::compile);
            assertThat(cache.get("cached")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Put Tests | put 测试")
    class PutTests {

        @Test
        @DisplayName("put adds to cache | put 添加到缓存")
        void testPut() {
            CompiledExpression expr = CompiledExpression.compile("x + y");
            cache.put("x + y", expr);
            assertThat(cache.contains("x + y")).isTrue();
        }
    }

    @Nested
    @DisplayName("Contains Tests | contains 测试")
    class ContainsTests {

        @Test
        @DisplayName("contains returns false for missing | contains 对缺失项返回 false")
        void testContainsFalse() {
            assertThat(cache.contains("missing")).isFalse();
        }

        @Test
        @DisplayName("contains returns true for cached | contains 对缓存项返回 true")
        void testContainsTrue() {
            cache.put("present", CompiledExpression.compile("1"));
            assertThat(cache.contains("present")).isTrue();
        }
    }

    @Nested
    @DisplayName("Remove Tests | remove 测试")
    class RemoveTests {

        @Test
        @DisplayName("remove removes from cache | remove 从缓存移除")
        void testRemove() {
            cache.put("to_remove", CompiledExpression.compile("1"));
            cache.remove("to_remove");
            assertThat(cache.contains("to_remove")).isFalse();
        }
    }

    @Nested
    @DisplayName("Clear Tests | clear 测试")
    class ClearTests {

        @Test
        @DisplayName("clear removes all | clear 移除所有")
        void testClear() {
            cache.put("a", CompiledExpression.compile("1"));
            cache.put("b", CompiledExpression.compile("2"));
            cache.clear();
            assertThat(cache.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Size Tests | size 测试")
    class SizeTests {

        @Test
        @DisplayName("size returns current count | size 返回当前数量")
        void testSize() {
            assertThat(cache.size()).isEqualTo(0);
            cache.put("a", CompiledExpression.compile("1"));
            assertThat(cache.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Stats Tests | 统计测试")
    class StatsTests {

        @Test
        @DisplayName("getStats returns stats | getStats 返回统计信息")
        void testGetStats() {
            cache.put("a", CompiledExpression.compile("1"));
            cache.put("b", CompiledExpression.compile("2"));
            CompiledExpressionCache.CacheStats stats = cache.getStats();
            assertThat(stats.size()).isEqualTo(2);
            assertThat(stats.maxSize()).isEqualTo(10);
            assertThat(stats.utilization()).isEqualTo(20.0);
        }

        @Test
        @DisplayName("stats utilization at zero size | 零大小时的使用率")
        void testStatsUtilizationZero() {
            CompiledExpressionCache.CacheStats stats = new CompiledExpressionCache.CacheStats(0, 0);
            assertThat(stats.utilization()).isEqualTo(0.0);
        }
    }
}
