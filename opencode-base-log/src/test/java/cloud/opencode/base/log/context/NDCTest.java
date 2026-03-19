package cloud.opencode.base.log.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.assertj.core.api.Assertions.*;

/**
 * NDC 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("NDC 测试")
class NDCTest {

    @BeforeEach
    void setUp() {
        NDC.clear();
    }

    @AfterEach
    void tearDown() {
        NDC.clear();
    }

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(NDC.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = NDC.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("push/pop方法测试")
    class PushPopTests {

        @Test
        @DisplayName("push和pop基本操作")
        void testPushAndPop() {
            NDC.push("message1");
            NDC.push("message2");

            assertThat(NDC.pop()).isEqualTo("message2");
            assertThat(NDC.pop()).isEqualTo("message1");
        }

        @Test
        @DisplayName("空栈pop返回null")
        void testPopEmpty() {
            assertThat(NDC.pop()).isNull();
        }
    }

    @Nested
    @DisplayName("peek方法测试")
    class PeekTests {

        @Test
        @DisplayName("peek返回栈顶但不移除")
        void testPeek() {
            NDC.push("message");

            assertThat(NDC.peek()).isEqualTo("message");
            assertThat(NDC.peek()).isEqualTo("message");
            assertThat(NDC.getDepth()).isEqualTo(1);
        }

        @Test
        @DisplayName("空栈peek返回null")
        void testPeekEmpty() {
            assertThat(NDC.peek()).isNull();
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清空栈")
        void testClear() {
            NDC.push("message1");
            NDC.push("message2");
            NDC.clear();

            assertThat(NDC.getDepth()).isZero();
            assertThat(NDC.pop()).isNull();
        }
    }

    @Nested
    @DisplayName("getDepth方法测试")
    class GetDepthTests {

        @Test
        @DisplayName("返回栈深度")
        void testGetDepth() {
            assertThat(NDC.getDepth()).isZero();

            NDC.push("message1");
            assertThat(NDC.getDepth()).isEqualTo(1);

            NDC.push("message2");
            assertThat(NDC.getDepth()).isEqualTo(2);

            NDC.pop();
            assertThat(NDC.getDepth()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("setMaxDepth方法测试")
    class SetMaxDepthTests {

        @Test
        @DisplayName("设置最大深度")
        void testSetMaxDepth() {
            assertThatCode(() -> NDC.setMaxDepth(10)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getCopyOfStack方法测试")
    class GetCopyOfStackTests {

        @Test
        @DisplayName("返回栈副本")
        void testGetCopyOfStack() {
            NDC.push("message1");
            NDC.push("message2");

            Deque<String> copy = NDC.getCopyOfStack();

            assertThat(copy).isNotNull();
        }
    }

    @Nested
    @DisplayName("setStack方法测试")
    class SetStackTests {

        @Test
        @DisplayName("设置栈")
        void testSetStack() {
            Deque<String> newStack = new ArrayDeque<>();
            newStack.push("msg1");
            newStack.push("msg2");

            NDC.setStack(newStack);
        }
    }

    @Nested
    @DisplayName("scope方法测试")
    class ScopeTests {

        @Test
        @DisplayName("scope自动pop")
        void testScope() {
            try (NDC.NDCScope scope = NDC.scope("scoped message")) {
                assertThat(NDC.peek()).isEqualTo("scoped message");
            }
            assertThat(NDC.peek()).isNull();
        }

        @Test
        @DisplayName("嵌套scope")
        void testNestedScope() {
            try (NDC.NDCScope scope1 = NDC.scope("outer")) {
                assertThat(NDC.getDepth()).isEqualTo(1);

                try (NDC.NDCScope scope2 = NDC.scope("inner")) {
                    assertThat(NDC.getDepth()).isEqualTo(2);
                    assertThat(NDC.peek()).isEqualTo("inner");
                }

                assertThat(NDC.getDepth()).isEqualTo(1);
                assertThat(NDC.peek()).isEqualTo("outer");
            }

            assertThat(NDC.getDepth()).isZero();
        }
    }

    @Nested
    @DisplayName("NDCScope内部类测试")
    class NDCScopeTests {

        @Test
        @DisplayName("NDCScope是final类")
        void testNDCScopeIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(NDC.NDCScope.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("NDCScope实现AutoCloseable")
        void testNDCScopeImplementsAutoCloseable() {
            assertThat(AutoCloseable.class.isAssignableFrom(NDC.NDCScope.class)).isTrue();
        }
    }
}
