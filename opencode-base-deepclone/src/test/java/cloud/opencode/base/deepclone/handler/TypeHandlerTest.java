package cloud.opencode.base.deepclone.handler;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.Cloner;
import cloud.opencode.base.deepclone.OpenClone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * TypeHandler 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("TypeHandler 接口测试")
class TypeHandlerTest {

    // Custom handler implementation
    public static class StringHandler implements TypeHandler<String> {
        @Override
        public String clone(String original, Cloner cloner, CloneContext context) {
            return original + "-cloned";
        }

        @Override
        public boolean supports(Class<?> type) {
            return String.class.equals(type);
        }
    }

    public static class PriorityHandler implements TypeHandler<Object> {
        private final int priorityValue;

        public PriorityHandler(int priority) {
            this.priorityValue = priority;
        }

        @Override
        public Object clone(Object original, Cloner cloner, CloneContext context) {
            return original;
        }

        @Override
        public boolean supports(Class<?> type) {
            return true;
        }

        @Override
        public int priority() {
            return priorityValue;
        }
    }

    @Nested
    @DisplayName("clone() 测试")
    class CloneTests {

        @Test
        @DisplayName("自定义处理器克隆")
        void testCustomHandlerClone() {
            StringHandler handler = new StringHandler();
            Cloner cloner = OpenClone.getDefaultCloner();
            CloneContext context = CloneContext.create();

            String result = handler.clone("test", cloner, context);

            assertThat(result).isEqualTo("test-cloned");
        }
    }

    @Nested
    @DisplayName("supports() 测试")
    class SupportsTests {

        @Test
        @DisplayName("处理器支持指定类型")
        void testSupports() {
            StringHandler handler = new StringHandler();

            assertThat(handler.supports(String.class)).isTrue();
            assertThat(handler.supports(Integer.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("priority() 测试")
    class PriorityTests {

        @Test
        @DisplayName("默认优先级为100")
        void testDefaultPriority() {
            StringHandler handler = new StringHandler();

            assertThat(handler.priority()).isEqualTo(100);
        }

        @Test
        @DisplayName("自定义优先级")
        void testCustomPriority() {
            PriorityHandler handler1 = new PriorityHandler(10);
            PriorityHandler handler2 = new PriorityHandler(50);

            assertThat(handler1.priority()).isEqualTo(10);
            assertThat(handler2.priority()).isEqualTo(50);
        }
    }
}
