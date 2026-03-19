package cloud.opencode.base.pool.policy;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * WaitPolicyTest Tests
 * WaitPolicyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("WaitPolicy 测试")
class WaitPolicyTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValueTests {

        @Test
        @DisplayName("BLOCK策略存在")
        void testBlockExists() {
            assertThat(WaitPolicy.BLOCK).isNotNull();
        }

        @Test
        @DisplayName("FAIL策略存在")
        void testFailExists() {
            assertThat(WaitPolicy.FAIL).isNotNull();
        }

        @Test
        @DisplayName("GROW策略存在")
        void testGrowExists() {
            assertThat(WaitPolicy.GROW).isNotNull();
        }
    }

    @Nested
    @DisplayName("枚举标准方法测试")
    class EnumStandardMethodTests {

        @Test
        @DisplayName("values返回所有策略")
        void testValues() {
            WaitPolicy[] values = WaitPolicy.values();

            assertThat(values).hasSize(3);
            assertThat(values).contains(WaitPolicy.BLOCK, WaitPolicy.FAIL, WaitPolicy.GROW);
        }

        @Test
        @DisplayName("valueOf返回正确的枚举值")
        void testValueOf() {
            assertThat(WaitPolicy.valueOf("BLOCK")).isEqualTo(WaitPolicy.BLOCK);
            assertThat(WaitPolicy.valueOf("FAIL")).isEqualTo(WaitPolicy.FAIL);
            assertThat(WaitPolicy.valueOf("GROW")).isEqualTo(WaitPolicy.GROW);
        }

        @Test
        @DisplayName("name返回枚举名称")
        void testName() {
            assertThat(WaitPolicy.BLOCK.name()).isEqualTo("BLOCK");
            assertThat(WaitPolicy.FAIL.name()).isEqualTo("FAIL");
            assertThat(WaitPolicy.GROW.name()).isEqualTo("GROW");
        }

        @Test
        @DisplayName("ordinal返回枚举序号")
        void testOrdinal() {
            assertThat(WaitPolicy.BLOCK.ordinal()).isZero();
            assertThat(WaitPolicy.FAIL.ordinal()).isEqualTo(1);
            assertThat(WaitPolicy.GROW.ordinal()).isEqualTo(2);
        }
    }
}
