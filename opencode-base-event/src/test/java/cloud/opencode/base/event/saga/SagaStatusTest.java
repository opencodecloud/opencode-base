package cloud.opencode.base.event.saga;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SagaStatus 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("SagaStatus 测试")
class SagaStatusTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValueTests {

        @Test
        @DisplayName("所有状态存在")
        void testAllStatusesExist() {
            assertThat(SagaStatus.values()).contains(
                    SagaStatus.RUNNING,
                    SagaStatus.COMPLETED,
                    SagaStatus.COMPENSATED,
                    SagaStatus.FAILED,
                    SagaStatus.CANCELLED,
                    SagaStatus.PENDING,
                    SagaStatus.PAUSED
            );
        }

        @Test
        @DisplayName("状态数量正确")
        void testStatusCount() {
            assertThat(SagaStatus.values()).hasSize(7);
        }
    }

    @Nested
    @DisplayName("valueOf() 测试")
    class ValueOfTests {

        @Test
        @DisplayName("可通过名称获取")
        void testValueOf() {
            assertThat(SagaStatus.valueOf("RUNNING")).isEqualTo(SagaStatus.RUNNING);
            assertThat(SagaStatus.valueOf("COMPLETED")).isEqualTo(SagaStatus.COMPLETED);
            assertThat(SagaStatus.valueOf("COMPENSATED")).isEqualTo(SagaStatus.COMPENSATED);
            assertThat(SagaStatus.valueOf("FAILED")).isEqualTo(SagaStatus.FAILED);
        }

        @Test
        @DisplayName("无效名称抛出异常")
        void testInvalidValueOf() {
            assertThatThrownBy(() -> SagaStatus.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
