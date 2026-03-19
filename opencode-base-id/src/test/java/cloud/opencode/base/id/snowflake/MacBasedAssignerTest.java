package cloud.opencode.base.id.snowflake;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MacBasedAssignerTest Tests
 * MacBasedAssignerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("MacBasedAssigner MAC分配器测试")
class MacBasedAssignerTest {

    @Nested
    @DisplayName("创建测试")
    class CreateTests {

        @Test
        @DisplayName("create返回非空实例")
        void testCreate() {
            MacBasedAssigner assigner = MacBasedAssigner.create();
            assertThat(assigner).isNotNull();
        }
    }

    @Nested
    @DisplayName("ID分配测试")
    class AssignTests {

        @Test
        @DisplayName("workerId在有效范围内")
        void testWorkerIdRange() {
            MacBasedAssigner assigner = MacBasedAssigner.create();
            long workerId = assigner.assignWorkerId();
            assertThat(workerId).isBetween(0L, 31L);
        }

        @Test
        @DisplayName("datacenterId在有效范围内")
        void testDatacenterIdRange() {
            MacBasedAssigner assigner = MacBasedAssigner.create();
            long datacenterId = assigner.assignDatacenterId();
            assertThat(datacenterId).isBetween(0L, 31L);
        }

        @Test
        @DisplayName("多次调用返回相同值")
        void testConsistentValues() {
            MacBasedAssigner assigner = MacBasedAssigner.create();
            long workerId1 = assigner.assignWorkerId();
            long workerId2 = assigner.assignWorkerId();
            assertThat(workerId1).isEqualTo(workerId2);
        }
    }

    @Nested
    @DisplayName("策略名称测试")
    class StrategyNameTests {

        @Test
        @DisplayName("getStrategyName返回MAC-Based")
        void testStrategyName() {
            MacBasedAssigner assigner = MacBasedAssigner.create();
            assertThat(assigner.getStrategyName()).isEqualTo("MAC-Based");
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("实现WorkerIdAssigner接口")
        void testImplementsInterface() {
            MacBasedAssigner assigner = MacBasedAssigner.create();
            assertThat(assigner).isInstanceOf(WorkerIdAssigner.class);
        }
    }
}
