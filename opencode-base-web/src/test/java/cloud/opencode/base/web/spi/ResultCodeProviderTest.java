package cloud.opencode.base.web.spi;

import cloud.opencode.base.web.CommonResultCode;
import cloud.opencode.base.web.ResultCode;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ResultCodeProviderTest Tests
 * ResultCodeProviderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("ResultCodeProvider Tests")
class ResultCodeProviderTest {

    @Nested
    @DisplayName("Interface Default Methods Tests")
    class InterfaceDefaultMethodsTests {

        @Test
        @DisplayName("getByCode should return result code for existing code")
        void getByCodeShouldReturnResultCodeForExistingCode() {
            ResultCodeProvider provider = new TestResultCodeProvider();

            Optional<ResultCode> result = provider.getByCode("TEST001");

            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("TEST001");
        }

        @Test
        @DisplayName("getByCode should return empty for non-existing code")
        void getByCodeShouldReturnEmptyForNonExistingCode() {
            ResultCodeProvider provider = new TestResultCodeProvider();

            Optional<ResultCode> result = provider.getByCode("UNKNOWN");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getOrder should return 0 by default")
        void getOrderShouldReturn0ByDefault() {
            ResultCodeProvider provider = new TestResultCodeProvider();

            assertThat(provider.getOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("supports should return true for existing code")
        void supportsShouldReturnTrueForExistingCode() {
            ResultCodeProvider provider = new TestResultCodeProvider();

            assertThat(provider.supports("TEST001")).isTrue();
        }

        @Test
        @DisplayName("supports should return false for non-existing code")
        void supportsShouldReturnFalseForNonExistingCode() {
            ResultCodeProvider provider = new TestResultCodeProvider();

            assertThat(provider.supports("UNKNOWN")).isFalse();
        }
    }

    @Nested
    @DisplayName("Custom Implementation Tests")
    class CustomImplementationTests {

        @Test
        @DisplayName("custom provider should return custom result codes")
        void customProviderShouldReturnCustomResultCodes() {
            ResultCodeProvider provider = () -> Arrays.asList(CommonResultCode.values());

            Collection<ResultCode> codes = provider.getResultCodes();

            assertThat(codes).isNotEmpty();
            assertThat(codes).contains(CommonResultCode.SUCCESS, CommonResultCode.BAD_REQUEST);
        }

        @Test
        @DisplayName("custom provider with order should return custom order")
        void customProviderWithOrderShouldReturnCustomOrder() {
            ResultCodeProvider provider = new ResultCodeProvider() {
                @Override
                public Collection<ResultCode> getResultCodes() {
                    return List.of();
                }

                @Override
                public int getOrder() {
                    return 100;
                }
            };

            assertThat(provider.getOrder()).isEqualTo(100);
        }
    }

    private static class TestResultCodeProvider implements ResultCodeProvider {
        private static final List<ResultCode> TEST_CODES = List.of(
            ResultCode.of("TEST001", "Test Error 1", 400),
            ResultCode.of("TEST002", "Test Error 2", 500)
        );

        @Override
        public Collection<ResultCode> getResultCodes() {
            return TEST_CODES;
        }
    }
}
