package cloud.opencode.base.id.snowflake;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * WorkerIdAssigner 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("WorkerIdAssigner 测试")
class WorkerIdAssignerTest {

    @Nested
    @DisplayName("IpBasedAssigner测试")
    class IpBasedAssignerTests {

        @Test
        @DisplayName("创建分配器")
        void testCreate() {
            IpBasedAssigner assigner = IpBasedAssigner.create();

            assertThat(assigner).isNotNull();
        }

        @Test
        @DisplayName("分配workerId在有效范围内")
        void testAssignWorkerId() {
            IpBasedAssigner assigner = IpBasedAssigner.create();

            long workerId = assigner.assignWorkerId();

            assertThat(workerId).isBetween(0L, 31L);
        }

        @Test
        @DisplayName("分配datacenterId在有效范围内")
        void testAssignDatacenterId() {
            IpBasedAssigner assigner = IpBasedAssigner.create();

            long datacenterId = assigner.assignDatacenterId();

            assertThat(datacenterId).isBetween(0L, 31L);
        }

        @Test
        @DisplayName("获取策略名称")
        void testGetStrategyName() {
            IpBasedAssigner assigner = IpBasedAssigner.create();

            assertThat(assigner.getStrategyName()).isEqualTo("IP-Based");
        }

        @Test
        @DisplayName("多次调用返回相同值")
        void testConsistentValues() {
            IpBasedAssigner assigner = IpBasedAssigner.create();

            long workerId1 = assigner.assignWorkerId();
            long workerId2 = assigner.assignWorkerId();
            long datacenterId1 = assigner.assignDatacenterId();
            long datacenterId2 = assigner.assignDatacenterId();

            assertThat(workerId1).isEqualTo(workerId2);
            assertThat(datacenterId1).isEqualTo(datacenterId2);
        }
    }

    @Nested
    @DisplayName("MacBasedAssigner测试")
    class MacBasedAssignerTests {

        @Test
        @DisplayName("创建分配器")
        void testCreate() {
            MacBasedAssigner assigner = MacBasedAssigner.create();

            assertThat(assigner).isNotNull();
        }

        @Test
        @DisplayName("分配workerId在有效范围内")
        void testAssignWorkerId() {
            MacBasedAssigner assigner = MacBasedAssigner.create();

            long workerId = assigner.assignWorkerId();

            assertThat(workerId).isBetween(0L, 31L);
        }

        @Test
        @DisplayName("分配datacenterId在有效范围内")
        void testAssignDatacenterId() {
            MacBasedAssigner assigner = MacBasedAssigner.create();

            long datacenterId = assigner.assignDatacenterId();

            assertThat(datacenterId).isBetween(0L, 31L);
        }

        @Test
        @DisplayName("获取策略名称")
        void testGetStrategyName() {
            MacBasedAssigner assigner = MacBasedAssigner.create();

            assertThat(assigner.getStrategyName()).isEqualTo("MAC-Based");
        }
    }

    @Nested
    @DisplayName("RandomAssigner测试")
    class RandomAssignerTests {

        @Test
        @DisplayName("创建随机分配器")
        void testCreate() {
            RandomAssigner assigner = RandomAssigner.create();

            assertThat(assigner).isNotNull();
        }

        @Test
        @DisplayName("使用固定ID创建")
        void testOf() {
            RandomAssigner assigner = RandomAssigner.of(10, 20);

            assertThat(assigner.assignWorkerId()).isEqualTo(10);
            assertThat(assigner.assignDatacenterId()).isEqualTo(20);
        }

        @Test
        @DisplayName("分配workerId在有效范围内")
        void testAssignWorkerId() {
            RandomAssigner assigner = RandomAssigner.create();

            long workerId = assigner.assignWorkerId();

            assertThat(workerId).isBetween(0L, 31L);
        }

        @Test
        @DisplayName("分配datacenterId在有效范围内")
        void testAssignDatacenterId() {
            RandomAssigner assigner = RandomAssigner.create();

            long datacenterId = assigner.assignDatacenterId();

            assertThat(datacenterId).isBetween(0L, 31L);
        }

        @Test
        @DisplayName("获取策略名称")
        void testGetStrategyName() {
            RandomAssigner assigner = RandomAssigner.create();

            assertThat(assigner.getStrategyName()).isEqualTo("Random");
        }

        @Test
        @DisplayName("ID取模处理")
        void testModuloHandling() {
            RandomAssigner assigner = RandomAssigner.of(100, 200);

            assertThat(assigner.assignWorkerId()).isBetween(0L, 31L);
            assertThat(assigner.assignDatacenterId()).isBetween(0L, 31L);
        }
    }

    @Nested
    @DisplayName("接口默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("getStrategyName默认实现")
        void testDefaultStrategyName() {
            WorkerIdAssigner assigner = new WorkerIdAssigner() {
                @Override
                public long assignWorkerId() {
                    return 0;
                }

                @Override
                public long assignDatacenterId() {
                    return 0;
                }
            };

            // 对于匿名类，getClass().getSimpleName()返回空字符串
            assertThat(assigner.getStrategyName()).isNotNull();
        }
    }
}
