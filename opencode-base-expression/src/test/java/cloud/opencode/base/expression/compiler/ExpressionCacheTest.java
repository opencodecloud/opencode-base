package cloud.opencode.base.expression.compiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ExpressionCache Tests
 * ExpressionCache 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("ExpressionCache Tests | ExpressionCache 测试")
class ExpressionCacheTest {

    private ExpressionCache cache;

    @BeforeEach
    void setup() {
        cache = ExpressionCache.create(10);
    }

    @Nested
    @DisplayName("Factory Methods Tests | 工厂方法测试")
    class FactoryMethodsTests {

        @Test
        @DisplayName("global returns singleton | global 返回单例")
        void testGlobal() {
            ExpressionCache global1 = ExpressionCache.global();
            ExpressionCache global2 = ExpressionCache.global();
            assertThat(global1).isSameAs(global2);
        }

        @Test
        @DisplayName("create creates new instance | create 创建新实例")
        void testCreate() {
            ExpressionCache cache1 = ExpressionCache.create(100);
            ExpressionCache cache2 = ExpressionCache.create(100);
            assertThat(cache1).isNotSameAs(cache2);
            assertThat(cache1.maxSize()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Get Operation Tests | get 操作测试")
    class GetOperationTests {

        @Test
        @DisplayName("get compiles and caches expression | get 编译并缓存表达式")
        void testGetCompilesAndCaches() {
            CompiledExpression expr = cache.get("1 + 2");
            assertThat(expr).isNotNull();
            assertThat(cache.contains("1 + 2")).isTrue();
            assertThat(cache.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("get returns same instance | get 返回相同实例")
        void testGetReturnsSameInstance() {
            CompiledExpression expr1 = cache.get("a + b");
            CompiledExpression expr2 = cache.get("a + b");
            assertThat(expr1).isSameAs(expr2);
        }
    }

    @Nested
    @DisplayName("Put Operation Tests | put 操作测试")
    class PutOperationTests {

        @Test
        @DisplayName("put adds expression to cache | put 将表达式添加到缓存")
        void testPut() {
            CompiledExpression expr = CompiledExpression.compile("x * y");
            cache.put("x * y", expr);
            assertThat(cache.contains("x * y")).isTrue();
            assertThat(cache.get("x * y")).isSameAs(expr);
        }
    }

    @Nested
    @DisplayName("Contains Operation Tests | contains 操作测试")
    class ContainsOperationTests {

        @Test
        @DisplayName("contains returns false for missing | contains 对缺失项返回 false")
        void testContainsFalse() {
            assertThat(cache.contains("not_cached")).isFalse();
        }

        @Test
        @DisplayName("contains returns true for cached | contains 对已缓存项返回 true")
        void testContainsTrue() {
            cache.get("cached_expr");
            assertThat(cache.contains("cached_expr")).isTrue();
        }
    }

    @Nested
    @DisplayName("Remove Operation Tests | remove 操作测试")
    class RemoveOperationTests {

        @Test
        @DisplayName("remove removes from cache | remove 从缓存中移除")
        void testRemove() {
            cache.get("to_remove");
            assertThat(cache.contains("to_remove")).isTrue();
            cache.remove("to_remove");
            assertThat(cache.contains("to_remove")).isFalse();
        }
    }

    @Nested
    @DisplayName("Clear Operation Tests | clear 操作测试")
    class ClearOperationTests {

        @Test
        @DisplayName("clear removes all entries | clear 移除所有条目")
        void testClear() {
            cache.get("expr1");
            cache.get("expr2");
            assertThat(cache.size()).isEqualTo(2);
            cache.clear();
            assertThat(cache.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Size Operation Tests | size 操作测试")
    class SizeOperationTests {

        @Test
        @DisplayName("size returns current size | size 返回当前大小")
        void testSize() {
            assertThat(cache.size()).isEqualTo(0);
            cache.get("expr1");
            assertThat(cache.size()).isEqualTo(1);
            cache.get("expr2");
            assertThat(cache.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("maxSize returns configured max | maxSize 返回配置的最大值")
        void testMaxSize() {
            assertThat(cache.maxSize()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Eviction Tests | 淘汰测试")
    class EvictionTests {

        @Test
        @DisplayName("evicts when at capacity | 达到容量时淘汰")
        void testEvictionAtCapacity() {
            ExpressionCache smallCache = ExpressionCache.create(5);
            for (int i = 0; i < 10; i++) {
                smallCache.get("expr" + i);
            }
            // Should have evicted some entries
            assertThat(smallCache.size()).isLessThanOrEqualTo(5);
        }
    }
}
