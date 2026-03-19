package cloud.opencode.base.pool.factory;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * PooledObjectStateTest Tests
 * PooledObjectStateTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("PooledObjectState 测试")
class PooledObjectStateTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValueTests {

        @Test
        @DisplayName("IDLE状态存在")
        void testIdleExists() {
            assertThat(PooledObjectState.IDLE).isNotNull();
        }

        @Test
        @DisplayName("ALLOCATED状态存在")
        void testAllocatedExists() {
            assertThat(PooledObjectState.ALLOCATED).isNotNull();
        }

        @Test
        @DisplayName("EVICTION状态存在")
        void testEvictionExists() {
            assertThat(PooledObjectState.EVICTION).isNotNull();
        }

        @Test
        @DisplayName("VALIDATION状态存在")
        void testValidationExists() {
            assertThat(PooledObjectState.VALIDATION).isNotNull();
        }

        @Test
        @DisplayName("INVALID状态存在")
        void testInvalidExists() {
            assertThat(PooledObjectState.INVALID).isNotNull();
        }

        @Test
        @DisplayName("RETURNING状态存在")
        void testReturningExists() {
            assertThat(PooledObjectState.RETURNING).isNotNull();
        }

        @Test
        @DisplayName("ABANDONED状态存在")
        void testAbandonedExists() {
            assertThat(PooledObjectState.ABANDONED).isNotNull();
        }
    }

    @Nested
    @DisplayName("枚举标准方法测试")
    class EnumStandardMethodTests {

        @Test
        @DisplayName("values返回所有状态")
        void testValues() {
            PooledObjectState[] values = PooledObjectState.values();

            assertThat(values).hasSize(7);
            assertThat(values).contains(
                    PooledObjectState.IDLE,
                    PooledObjectState.ALLOCATED,
                    PooledObjectState.EVICTION,
                    PooledObjectState.VALIDATION,
                    PooledObjectState.INVALID,
                    PooledObjectState.RETURNING,
                    PooledObjectState.ABANDONED
            );
        }

        @Test
        @DisplayName("valueOf返回正确的枚举值")
        void testValueOf() {
            assertThat(PooledObjectState.valueOf("IDLE"))
                    .isEqualTo(PooledObjectState.IDLE);
            assertThat(PooledObjectState.valueOf("ALLOCATED"))
                    .isEqualTo(PooledObjectState.ALLOCATED);
        }

        @Test
        @DisplayName("name返回枚举名称")
        void testName() {
            assertThat(PooledObjectState.IDLE.name()).isEqualTo("IDLE");
            assertThat(PooledObjectState.ALLOCATED.name()).isEqualTo("ALLOCATED");
        }

        @Test
        @DisplayName("ordinal返回枚举序号")
        void testOrdinal() {
            assertThat(PooledObjectState.IDLE.ordinal()).isZero();
        }
    }
}
