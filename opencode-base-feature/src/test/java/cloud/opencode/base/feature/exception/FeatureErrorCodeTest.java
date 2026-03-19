package cloud.opencode.base.feature.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureErrorCode 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("FeatureErrorCode 测试")
class FeatureErrorCodeTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValueTests {

        @Test
        @DisplayName("UNKNOWN错误码为0")
        void testUnknown() {
            assertThat(FeatureErrorCode.UNKNOWN.getCode()).isZero();
            assertThat(FeatureErrorCode.UNKNOWN.getMessage()).isEqualTo("Unknown error");
            assertThat(FeatureErrorCode.UNKNOWN.getMessageZh()).isEqualTo("未知错误");
        }

        @Test
        @DisplayName("NOT_FOUND错误码为1001")
        void testNotFound() {
            assertThat(FeatureErrorCode.NOT_FOUND.getCode()).isEqualTo(1001);
            assertThat(FeatureErrorCode.NOT_FOUND.getMessage()).isEqualTo("Feature not found");
            assertThat(FeatureErrorCode.NOT_FOUND.getMessageZh()).isEqualTo("功能不存在");
        }

        @Test
        @DisplayName("ALREADY_EXISTS错误码为1002")
        void testAlreadyExists() {
            assertThat(FeatureErrorCode.ALREADY_EXISTS.getCode()).isEqualTo(1002);
        }

        @Test
        @DisplayName("INVALID_KEY错误码为1003")
        void testInvalidKey() {
            assertThat(FeatureErrorCode.INVALID_KEY.getCode()).isEqualTo(1003);
        }

        @Test
        @DisplayName("配置错误在2xxx范围")
        void testConfigErrorsIn2xxxRange() {
            assertThat(FeatureErrorCode.INVALID_STRATEGY.getCode()).isBetween(2000, 2999);
            assertThat(FeatureErrorCode.INVALID_CONTEXT.getCode()).isBetween(2000, 2999);
            assertThat(FeatureErrorCode.INVALID_CONFIG.getCode()).isBetween(2000, 2999);
        }

        @Test
        @DisplayName("存储错误在3xxx范围")
        void testStoreErrorsIn3xxxRange() {
            assertThat(FeatureErrorCode.STORE_ERROR.getCode()).isBetween(3000, 3999);
            assertThat(FeatureErrorCode.PERSIST_FAILED.getCode()).isBetween(3000, 3999);
            assertThat(FeatureErrorCode.LOAD_FAILED.getCode()).isBetween(3000, 3999);
        }

        @Test
        @DisplayName("安全错误在4xxx范围")
        void testSecurityErrorsIn4xxxRange() {
            assertThat(FeatureErrorCode.UNAUTHORIZED.getCode()).isBetween(4000, 4999);
            assertThat(FeatureErrorCode.AUDIT_FAILED.getCode()).isBetween(4000, 4999);
            assertThat(FeatureErrorCode.SECURITY_VIOLATION.getCode()).isBetween(4000, 4999);
        }
    }

    @Nested
    @DisplayName("getDescription() 测试")
    class GetDescriptionTests {

        @Test
        @DisplayName("返回格式化描述")
        void testGetDescription() {
            String desc = FeatureErrorCode.NOT_FOUND.getDescription();

            assertThat(desc).isEqualTo("1001: Feature not found");
        }
    }

    @Nested
    @DisplayName("所有枚举值测试")
    class AllValuesTests {

        @Test
        @DisplayName("所有值都有非空消息")
        void testAllValuesHaveMessages() {
            for (FeatureErrorCode code : FeatureErrorCode.values()) {
                assertThat(code.getMessage()).isNotNull().isNotEmpty();
                assertThat(code.getMessageZh()).isNotNull().isNotEmpty();
            }
        }

        @Test
        @DisplayName("枚举值数量")
        void testValueCount() {
            assertThat(FeatureErrorCode.values()).hasSize(13);
        }
    }
}
