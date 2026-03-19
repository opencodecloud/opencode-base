package cloud.opencode.base.id.snowflake;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * RandomAssignerTest Tests
 * RandomAssignerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("RandomAssigner 随机分配器测试")
class RandomAssignerTest {

    @Nested
    @DisplayName("create方法测试")
    class CreateTests {

        @Test
        @DisplayName("create返回非空实例")
        void testCreate() {
            RandomAssigner assigner = RandomAssigner.create();
            assertThat(assigner).isNotNull();
        }

        @Test
        @DisplayName("workerId在有效范围内")
        void testWorkerIdRange() {
            RandomAssigner assigner = RandomAssigner.create();
            assertThat(assigner.assignWorkerId()).isBetween(0L, 31L);
        }

        @Test
        @DisplayName("datacenterId在有效范围内")
        void testDatacenterIdRange() {
            RandomAssigner assigner = RandomAssigner.create();
            assertThat(assigner.assignDatacenterId()).isBetween(0L, 31L);
        }
    }

    @Nested
    @DisplayName("of方法测试")
    class OfTests {

        @Test
        @DisplayName("of创建指定ID的分配器")
        void testOf() {
            RandomAssigner assigner = RandomAssigner.of(5, 10);
            assertThat(assigner.assignWorkerId()).isEqualTo(5);
            assertThat(assigner.assignDatacenterId()).isEqualTo(10);
        }

        @Test
        @DisplayName("of对超出范围的值取模")
        void testOfModulo() {
            RandomAssigner assigner = RandomAssigner.of(100, 200);
            assertThat(assigner.assignWorkerId()).isBetween(0L, 31L);
            assertThat(assigner.assignDatacenterId()).isBetween(0L, 31L);
        }

        @Test
        @DisplayName("of(0, 0)创建值为0的分配器")
        void testOfZero() {
            RandomAssigner assigner = RandomAssigner.of(0, 0);
            assertThat(assigner.assignWorkerId()).isEqualTo(0);
            assertThat(assigner.assignDatacenterId()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("策略名称测试")
    class StrategyNameTests {

        @Test
        @DisplayName("getStrategyName返回Random")
        void testStrategyName() {
            RandomAssigner assigner = RandomAssigner.create();
            assertThat(assigner.getStrategyName()).isEqualTo("Random");
        }
    }

    @Nested
    @DisplayName("一致性测试")
    class ConsistencyTests {

        @Test
        @DisplayName("多次调用assignWorkerId返回相同值")
        void testConsistentWorkerId() {
            RandomAssigner assigner = RandomAssigner.create();
            long first = assigner.assignWorkerId();
            long second = assigner.assignWorkerId();
            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("多次调用assignDatacenterId返回相同值")
        void testConsistentDatacenterId() {
            RandomAssigner assigner = RandomAssigner.create();
            long first = assigner.assignDatacenterId();
            long second = assigner.assignDatacenterId();
            assertThat(first).isEqualTo(second);
        }
    }
}
