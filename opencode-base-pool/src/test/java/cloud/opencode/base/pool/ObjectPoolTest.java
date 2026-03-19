package cloud.opencode.base.pool;

import cloud.opencode.base.pool.exception.OpenPoolException;
import cloud.opencode.base.pool.metrics.PoolMetrics;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ObjectPool 接口测试")
class ObjectPoolTest {

    private TestObjectPool pool;

    @BeforeEach
    void setup() {
        pool = new TestObjectPool();
    }

    @Nested
    @DisplayName("execute(Function)默认方法测试")
    class ExecuteFunctionTests {

        @Test
        @DisplayName("execute借用对象执行后归还")
        void testExecuteFunction() {
            String result = pool.execute(obj -> "result-" + obj);
            assertThat(result).isEqualTo("result-test-object");
            assertThat(pool.returned).contains("test-object");
        }

        @Test
        @DisplayName("execute抛出异常时仍归还对象")
        void testExecuteFunctionWithException() {
            assertThatThrownBy(() -> pool.execute((Function<String, String>) obj -> {
                throw new RuntimeException("test error");
            })).isInstanceOf(RuntimeException.class);
            assertThat(pool.returned).contains("test-object");
        }
    }

    @Nested
    @DisplayName("execute(Consumer)默认方法测试")
    class ExecuteConsumerTests {

        @Test
        @DisplayName("execute(Consumer)借用并归还")
        void testExecuteConsumer() {
            List<String> collected = new ArrayList<>();
            pool.execute((Consumer<String>) collected::add);
            assertThat(collected).containsExactly("test-object");
            assertThat(pool.returned).contains("test-object");
        }

        @Test
        @DisplayName("execute(Consumer)抛出异常时仍归还")
        void testExecuteConsumerWithException() {
            assertThatThrownBy(() -> pool.execute((Consumer<String>) obj -> {
                throw new RuntimeException("test");
            })).isInstanceOf(RuntimeException.class);
            assertThat(pool.returned).contains("test-object");
        }
    }

    /**
     * Minimal test implementation of ObjectPool
      *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
    static class TestObjectPool implements ObjectPool<String> {
        final List<String> returned = new ArrayList<>();

        @Override
        public String borrowObject() throws OpenPoolException {
            return "test-object";
        }

        @Override
        public String borrowObject(Duration timeout) throws OpenPoolException {
            return "test-object";
        }

        @Override
        public void returnObject(String obj) {
            returned.add(obj);
        }

        @Override
        public void invalidateObject(String obj) {}

        @Override
        public void addObject() throws OpenPoolException {}

        @Override
        public int getNumIdle() { return 0; }

        @Override
        public int getNumActive() { return 0; }

        @Override
        public void clear() {}

        @Override
        public PoolMetrics getMetrics() { return null; }

        @Override
        public void close() {}
    }
}
