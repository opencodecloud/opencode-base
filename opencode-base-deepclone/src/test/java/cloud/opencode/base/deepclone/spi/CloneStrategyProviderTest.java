package cloud.opencode.base.deepclone.spi;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.strategy.CloneStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CloneStrategyProvider 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("CloneStrategyProvider 接口测试")
class CloneStrategyProviderTest {

    // Test strategy
    public static class TestStrategy implements CloneStrategy {
        @Override
        public String name() {
            return "test-strategy";
        }

        @Override
        public <T> T clone(T original, CloneContext context) {
            return original;
        }
    }

    // Test provider implementation
    public static class TestProvider implements CloneStrategyProvider {
        @Override
        public List<CloneStrategy> getStrategies() {
            return List.of(new TestStrategy());
        }
    }

    // Provider with custom priority
    public static class PriorityProvider implements CloneStrategyProvider {
        private final int priorityValue;

        public PriorityProvider(int priority) {
            this.priorityValue = priority;
        }

        @Override
        public List<CloneStrategy> getStrategies() {
            return List.of(new TestStrategy());
        }

        @Override
        public int priority() {
            return priorityValue;
        }
    }

    // Provider with multiple strategies
    public static class MultiStrategyProvider implements CloneStrategyProvider {
        @Override
        public List<CloneStrategy> getStrategies() {
            return List.of(
                    new TestStrategy(),
                    new CloneStrategy() {
                        @Override
                        public String name() {
                            return "second-strategy";
                        }

                        @Override
                        public <T> T clone(T original, CloneContext context) {
                            return original;
                        }
                    }
            );
        }
    }

    @Nested
    @DisplayName("getStrategies() 测试")
    class GetStrategiesTests {

        @Test
        @DisplayName("返回策略列表")
        void testGetStrategies() {
            TestProvider provider = new TestProvider();

            List<CloneStrategy> strategies = provider.getStrategies();

            assertThat(strategies).hasSize(1);
            assertThat(strategies.get(0).name()).isEqualTo("test-strategy");
        }

        @Test
        @DisplayName("返回多个策略")
        void testGetMultipleStrategies() {
            MultiStrategyProvider provider = new MultiStrategyProvider();

            List<CloneStrategy> strategies = provider.getStrategies();

            assertThat(strategies).hasSize(2);
            assertThat(strategies).extracting(CloneStrategy::name)
                    .containsExactly("test-strategy", "second-strategy");
        }
    }

    @Nested
    @DisplayName("priority() 测试")
    class PriorityTests {

        @Test
        @DisplayName("默认优先级为100")
        void testDefaultPriority() {
            TestProvider provider = new TestProvider();

            assertThat(provider.priority()).isEqualTo(100);
        }

        @Test
        @DisplayName("自定义优先级")
        void testCustomPriority() {
            PriorityProvider provider1 = new PriorityProvider(10);
            PriorityProvider provider2 = new PriorityProvider(50);

            assertThat(provider1.priority()).isEqualTo(10);
            assertThat(provider2.priority()).isEqualTo(50);
        }

        @Test
        @DisplayName("优先级比较")
        void testPriorityComparison() {
            PriorityProvider highPriority = new PriorityProvider(10);
            PriorityProvider lowPriority = new PriorityProvider(200);
            TestProvider defaultPriority = new TestProvider();

            // Lower value = higher priority
            assertThat(highPriority.priority()).isLessThan(defaultPriority.priority());
            assertThat(defaultPriority.priority()).isLessThan(lowPriority.priority());
        }
    }
}
