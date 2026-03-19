package cloud.opencode.base.log.spi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.assertj.core.api.Assertions.*;

/**
 * NDCAdapter 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("NDCAdapter 接口测试")
class NDCAdapterTest {

    @Nested
    @DisplayName("接口定义测试")
    class InterfaceDefinitionTests {

        @Test
        @DisplayName("NDCAdapter是接口")
        void testIsInterface() {
            assertThat(NDCAdapter.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("定义了push方法")
        void testPushMethod() throws NoSuchMethodException {
            assertThat(NDCAdapter.class.getMethod("push", String.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了pop方法")
        void testPopMethod() throws NoSuchMethodException {
            assertThat(NDCAdapter.class.getMethod("pop")).isNotNull();
        }

        @Test
        @DisplayName("定义了peek方法")
        void testPeekMethod() throws NoSuchMethodException {
            assertThat(NDCAdapter.class.getMethod("peek")).isNotNull();
        }

        @Test
        @DisplayName("定义了clear方法")
        void testClearMethod() throws NoSuchMethodException {
            assertThat(NDCAdapter.class.getMethod("clear")).isNotNull();
        }

        @Test
        @DisplayName("定义了getDepth方法")
        void testGetDepthMethod() throws NoSuchMethodException {
            assertThat(NDCAdapter.class.getMethod("getDepth")).isNotNull();
        }

        @Test
        @DisplayName("定义了setMaxDepth方法")
        void testSetMaxDepthMethod() throws NoSuchMethodException {
            assertThat(NDCAdapter.class.getMethod("setMaxDepth", int.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了getCopyOfStack方法")
        void testGetCopyOfStackMethod() throws NoSuchMethodException {
            assertThat(NDCAdapter.class.getMethod("getCopyOfStack")).isNotNull();
        }

        @Test
        @DisplayName("定义了setStack方法")
        void testSetStackMethod() throws NoSuchMethodException {
            assertThat(NDCAdapter.class.getMethod("setStack", Deque.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("实现测试")
    class ImplementationTests {

        private NDCAdapter createAdapter() {
            DefaultLogProvider provider = new DefaultLogProvider();
            return provider.getNDCAdapter();
        }

        @Test
        @DisplayName("push和pop方法")
        void testPushPop() {
            NDCAdapter adapter = createAdapter();

            adapter.push("msg1");
            adapter.push("msg2");

            assertThat(adapter.pop()).isEqualTo("msg2");
            assertThat(adapter.pop()).isEqualTo("msg1");
        }

        @Test
        @DisplayName("peek方法")
        void testPeek() {
            NDCAdapter adapter = createAdapter();

            adapter.push("msg1");
            assertThat(adapter.peek()).isEqualTo("msg1");
            assertThat(adapter.peek()).isEqualTo("msg1"); // Still there
        }

        @Test
        @DisplayName("peek空栈返回null")
        void testPeekEmpty() {
            NDCAdapter adapter = createAdapter();
            assertThat(adapter.peek()).isNull();
        }

        @Test
        @DisplayName("pop空栈返回null")
        void testPopEmpty() {
            NDCAdapter adapter = createAdapter();
            assertThat(adapter.pop()).isNull();
        }

        @Test
        @DisplayName("clear方法")
        void testClear() {
            NDCAdapter adapter = createAdapter();

            adapter.push("msg1");
            adapter.push("msg2");
            adapter.clear();

            assertThat(adapter.getDepth()).isZero();
        }

        @Test
        @DisplayName("getDepth方法")
        void testGetDepth() {
            NDCAdapter adapter = createAdapter();

            assertThat(adapter.getDepth()).isZero();
            adapter.push("msg1");
            assertThat(adapter.getDepth()).isEqualTo(1);
            adapter.push("msg2");
            assertThat(adapter.getDepth()).isEqualTo(2);
        }

        @Test
        @DisplayName("setMaxDepth限制栈深度")
        void testSetMaxDepth() {
            NDCAdapter adapter = createAdapter();

            adapter.setMaxDepth(2);
            adapter.push("msg1");
            adapter.push("msg2");
            adapter.push("msg3"); // Should be ignored

            assertThat(adapter.getDepth()).isEqualTo(2);
        }

        @Test
        @DisplayName("getCopyOfStack返回副本")
        void testGetCopyOfStack() {
            NDCAdapter adapter = createAdapter();

            adapter.push("msg1");
            adapter.push("msg2");

            Deque<String> copy = adapter.getCopyOfStack();
            assertThat(copy).hasSize(2);

            // Verify it's a copy
            copy.push("msg3");
            assertThat(adapter.getDepth()).isEqualTo(2);
        }

        @Test
        @DisplayName("setStack设置栈")
        void testSetStack() {
            NDCAdapter adapter = createAdapter();

            Deque<String> stack = new ArrayDeque<>();
            stack.push("msg1");
            stack.push("msg2");

            adapter.setStack(stack);

            assertThat(adapter.getDepth()).isEqualTo(2);
            assertThat(adapter.pop()).isEqualTo("msg2");
        }

        @Test
        @DisplayName("setStack(null)清空栈")
        void testSetStackNull() {
            NDCAdapter adapter = createAdapter();

            adapter.push("msg1");
            adapter.setStack(null);

            assertThat(adapter.getDepth()).isZero();
        }
    }
}
