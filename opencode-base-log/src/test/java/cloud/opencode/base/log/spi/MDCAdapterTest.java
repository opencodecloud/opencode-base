package cloud.opencode.base.log.spi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * MDCAdapter 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("MDCAdapter 接口测试")
class MDCAdapterTest {

    @Nested
    @DisplayName("接口定义测试")
    class InterfaceDefinitionTests {

        @Test
        @DisplayName("MDCAdapter是接口")
        void testIsInterface() {
            assertThat(MDCAdapter.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("定义了put方法")
        void testPutMethod() throws NoSuchMethodException {
            assertThat(MDCAdapter.class.getMethod("put", String.class, String.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了get方法")
        void testGetMethod() throws NoSuchMethodException {
            assertThat(MDCAdapter.class.getMethod("get", String.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了remove方法")
        void testRemoveMethod() throws NoSuchMethodException {
            assertThat(MDCAdapter.class.getMethod("remove", String.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了clear方法")
        void testClearMethod() throws NoSuchMethodException {
            assertThat(MDCAdapter.class.getMethod("clear")).isNotNull();
        }

        @Test
        @DisplayName("定义了getCopyOfContextMap方法")
        void testGetCopyOfContextMapMethod() throws NoSuchMethodException {
            assertThat(MDCAdapter.class.getMethod("getCopyOfContextMap")).isNotNull();
        }

        @Test
        @DisplayName("定义了setContextMap方法")
        void testSetContextMapMethod() throws NoSuchMethodException {
            assertThat(MDCAdapter.class.getMethod("setContextMap", Map.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("实现测试")
    class ImplementationTests {

        private MDCAdapter createAdapter() {
            DefaultLogProvider provider = new DefaultLogProvider();
            return provider.getMDCAdapter();
        }

        @Test
        @DisplayName("put和get方法")
        void testPutGet() {
            MDCAdapter adapter = createAdapter();

            adapter.put("key", "value");
            assertThat(adapter.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("remove方法")
        void testRemove() {
            MDCAdapter adapter = createAdapter();

            adapter.put("key", "value");
            adapter.remove("key");
            assertThat(adapter.get("key")).isNull();
        }

        @Test
        @DisplayName("clear方法")
        void testClear() {
            MDCAdapter adapter = createAdapter();

            adapter.put("key1", "value1");
            adapter.put("key2", "value2");
            adapter.clear();

            assertThat(adapter.get("key1")).isNull();
            assertThat(adapter.get("key2")).isNull();
        }

        @Test
        @DisplayName("getCopyOfContextMap返回副本")
        void testGetCopyOfContextMap() {
            MDCAdapter adapter = createAdapter();

            adapter.put("key", "value");
            Map<String, String> copy = adapter.getCopyOfContextMap();

            assertThat(copy).containsEntry("key", "value");

            // Verify it's a copy
            copy.put("newKey", "newValue");
            assertThat(adapter.get("newKey")).isNull();
        }

        @Test
        @DisplayName("setContextMap设置上下文")
        void testSetContextMap() {
            MDCAdapter adapter = createAdapter();

            Map<String, String> map = new HashMap<>();
            map.put("key1", "value1");
            map.put("key2", "value2");

            adapter.setContextMap(map);

            assertThat(adapter.get("key1")).isEqualTo("value1");
            assertThat(adapter.get("key2")).isEqualTo("value2");
        }

        @Test
        @DisplayName("setContextMap(null)清空上下文")
        void testSetContextMapNull() {
            MDCAdapter adapter = createAdapter();

            adapter.put("key", "value");
            adapter.setContextMap(null);

            assertThat(adapter.get("key")).isNull();
        }
    }
}
