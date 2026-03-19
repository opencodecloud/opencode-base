package cloud.opencode.base.id.snowflake;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * IpBasedAssignerTest Tests
 * IpBasedAssignerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("IpBasedAssigner IP分配器测试")
class IpBasedAssignerTest {

    @Nested
    @DisplayName("创建测试")
    class CreateTests {

        @Test
        @DisplayName("create返回非空实例")
        void testCreate() {
            IpBasedAssigner assigner = IpBasedAssigner.create();
            assertThat(assigner).isNotNull();
        }
    }

    @Nested
    @DisplayName("ID分配测试")
    class AssignTests {

        @Test
        @DisplayName("workerId在有效范围内")
        void testWorkerIdRange() {
            IpBasedAssigner assigner = IpBasedAssigner.create();
            long workerId = assigner.assignWorkerId();
            assertThat(workerId).isBetween(0L, 31L);
        }

        @Test
        @DisplayName("datacenterId在有效范围内")
        void testDatacenterIdRange() {
            IpBasedAssigner assigner = IpBasedAssigner.create();
            long datacenterId = assigner.assignDatacenterId();
            assertThat(datacenterId).isBetween(0L, 31L);
        }

        @Test
        @DisplayName("多次调用返回相同值")
        void testConsistentValues() {
            IpBasedAssigner assigner = IpBasedAssigner.create();
            long workerId1 = assigner.assignWorkerId();
            long workerId2 = assigner.assignWorkerId();
            assertThat(workerId1).isEqualTo(workerId2);
        }
    }

    @Nested
    @DisplayName("策略名称测试")
    class StrategyNameTests {

        @Test
        @DisplayName("getStrategyName返回IP-Based")
        void testStrategyName() {
            IpBasedAssigner assigner = IpBasedAssigner.create();
            assertThat(assigner.getStrategyName()).isEqualTo("IP-Based");
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现WorkerIdAssigner接口")
        void testImplementsInterface() {
            IpBasedAssigner assigner = IpBasedAssigner.create();
            assertThat(assigner).isInstanceOf(WorkerIdAssigner.class);
        }
    }
}
