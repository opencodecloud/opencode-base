package cloud.opencode.base.deepclone.strategy;

import cloud.opencode.base.deepclone.CloneContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CloneStrategy 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("CloneStrategy 接口测试")
class CloneStrategyTest {

    // Custom strategy implementation
    public static class CustomStrategy implements CloneStrategy {
        @Override
        public String name() {
            return "custom";
        }

        @Override
        public <T> T clone(T original, CloneContext context) {
            return original; // Simple identity clone for testing
        }
    }

    public static class PriorityStrategy implements CloneStrategy {
        private final int priorityValue;

        public PriorityStrategy(int priority) {
            this.priorityValue = priority;
        }

        @Override
        public String name() {
            return "priority-" + priorityValue;
        }

        @Override
        public <T> T clone(T original, CloneContext context) {
            return original;
        }

        @Override
        public int priority() {
            return priorityValue;
        }
    }

    public static class LimitedStrategy implements CloneStrategy {
        @Override
        public String name() {
            return "limited";
        }

        @Override
        public <T> T clone(T original, CloneContext context) {
            return original;
        }

        @Override
        public boolean supports(Class<?> type) {
            return String.class.equals(type);
        }
    }

    @Nested
    @DisplayName("name() 测试")
    class NameTests {

        @Test
        @DisplayName("返回策略名称")
        void testName() {
            CustomStrategy strategy = new CustomStrategy();

            assertThat(strategy.name()).isEqualTo("custom");
        }
    }

    @Nested
    @DisplayName("clone() 测试")
    class CloneTests {

        @Test
        @DisplayName("克隆对象")
        void testClone() {
            CustomStrategy strategy = new CustomStrategy();
            CloneContext context = CloneContext.create();

            String result = strategy.clone("test", context);

            assertThat(result).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("priority() 测试")
    class PriorityTests {

        @Test
        @DisplayName("默认优先级为100")
        void testDefaultPriority() {
            CustomStrategy strategy = new CustomStrategy();

            assertThat(strategy.priority()).isEqualTo(100);
        }

        @Test
        @DisplayName("自定义优先级")
        void testCustomPriority() {
            PriorityStrategy strategy1 = new PriorityStrategy(10);
            PriorityStrategy strategy2 = new PriorityStrategy(50);

            assertThat(strategy1.priority()).isEqualTo(10);
            assertThat(strategy2.priority()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("supports() 测试")
    class SupportsTests {

        @Test
        @DisplayName("默认支持所有类型")
        void testDefaultSupports() {
            CustomStrategy strategy = new CustomStrategy();

            assertThat(strategy.supports(String.class)).isTrue();
            assertThat(strategy.supports(Integer.class)).isTrue();
            assertThat(strategy.supports(Object.class)).isTrue();
        }

        @Test
        @DisplayName("自定义支持检查")
        void testCustomSupports() {
            LimitedStrategy strategy = new LimitedStrategy();

            assertThat(strategy.supports(String.class)).isTrue();
            assertThat(strategy.supports(Integer.class)).isFalse();
        }
    }
}
