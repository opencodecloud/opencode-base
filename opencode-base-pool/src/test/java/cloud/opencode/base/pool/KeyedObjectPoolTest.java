package cloud.opencode.base.pool;

import cloud.opencode.base.pool.exception.OpenPoolException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * KeyedObjectPoolTest Tests
 * KeyedObjectPoolTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("KeyedObjectPool 接口测试")
class KeyedObjectPoolTest {

    private TestKeyedPool pool;

    @BeforeEach
    void setup() {
        pool = new TestKeyedPool();
    }

    @Nested
    @DisplayName("execute默认方法测试")
    class ExecuteTests {

        @Test
        @DisplayName("execute借用对象执行后归还")
        void testExecute() {
            String result = pool.execute("key1", (key, obj) -> key + ":" + obj);
            assertThat(result).isEqualTo("key1:obj-key1");
            assertThat(pool.returned).hasSize(1);
        }

        @Test
        @DisplayName("execute抛出异常时仍归还对象")
        void testExecuteWithException() {
            assertThatThrownBy(() -> pool.execute("key1", (key, obj) -> {
                throw new RuntimeException("test");
            })).isInstanceOf(RuntimeException.class);
            assertThat(pool.returned).hasSize(1);
        }
    }

    static class TestKeyedPool implements KeyedObjectPool<String, String> {
        final List<String> returned = new ArrayList<>();

        @Override
        public String borrowObject(String key) throws OpenPoolException {
            return "obj-" + key;
        }

        @Override
        public String borrowObject(String key, Duration timeout) throws OpenPoolException {
            return "obj-" + key;
        }

        @Override
        public void returnObject(String key, String obj) {
            returned.add(key + ":" + obj);
        }

        @Override
        public void invalidateObject(String key, String obj) {}

        @Override
        public int getNumIdle(String key) { return 0; }

        @Override
        public int getNumActive(String key) { return 0; }

        @Override
        public void clear(String key) {}

        @Override
        public void clear() {}

        @Override
        public int getNumKeys() { return 0; }

        @Override
        public void close() {}
    }
}
