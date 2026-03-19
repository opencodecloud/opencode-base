package cloud.opencode.base.web.spi;

import cloud.opencode.base.web.Result;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ResultCustomizerTest Tests
 * ResultCustomizerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("ResultCustomizer Tests")
class ResultCustomizerTest {

    @Nested
    @DisplayName("Interface Default Methods Tests")
    class InterfaceDefaultMethodsTests {

        @Test
        @DisplayName("getOrder should return 0 by default")
        void getOrderShouldReturn0ByDefault() {
            ResultCustomizer customizer = new TestResultCustomizer();

            assertThat(customizer.getOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("shouldApply should return true by default")
        void shouldApplyShouldReturnTrueByDefault() {
            ResultCustomizer customizer = new TestResultCustomizer();
            Result<String> result = Result.ok("test");

            assertThat(customizer.shouldApply(result)).isTrue();
        }
    }

    @Nested
    @DisplayName("Customize Tests")
    class CustomizeTests {

        @Test
        @DisplayName("customize should modify result")
        void customizeShouldModifyResult() {
            ResultCustomizer customizer = new ResultCustomizer() {
                @Override
                public <T> Result<T> customize(Result<T> result) {
                    return result.withMessage("Customized: " + result.message());
                }
            };

            Result<String> original = Result.ok("data");
            Result<String> customized = customizer.customize(original);

            assertThat(customized.message()).startsWith("Customized:");
        }

        @Test
        @DisplayName("customizer with trace ID should add trace ID")
        void customizerWithTraceIdShouldAddTraceId() {
            ResultCustomizer customizer = new ResultCustomizer() {
                @Override
                public <T> Result<T> customize(Result<T> result) {
                    return result.withTraceId("custom-trace-id");
                }
            };

            Result<String> original = Result.ok("data");
            Result<String> customized = customizer.customize(original);

            assertThat(customized.traceId()).isEqualTo("custom-trace-id");
        }
    }

    @Nested
    @DisplayName("Conditional Application Tests")
    class ConditionalApplicationTests {

        @Test
        @DisplayName("shouldApply can filter by success status")
        void shouldApplyCanFilterBySuccessStatus() {
            ResultCustomizer successOnly = new ResultCustomizer() {
                @Override
                public <T> Result<T> customize(Result<T> result) {
                    return result;
                }

                @Override
                public boolean shouldApply(Result<?> result) {
                    return result.success();
                }
            };

            assertThat(successOnly.shouldApply(Result.ok())).isTrue();
            assertThat(successOnly.shouldApply(Result.fail("E001", "Error"))).isFalse();
        }

        @Test
        @DisplayName("shouldApply can filter by code")
        void shouldApplyCanFilterByCode() {
            ResultCustomizer codeFilter = new ResultCustomizer() {
                @Override
                public <T> Result<T> customize(Result<T> result) {
                    return result;
                }

                @Override
                public boolean shouldApply(Result<?> result) {
                    return result.code().startsWith("A");
                }
            };

            assertThat(codeFilter.shouldApply(Result.fail("A0400", "Bad Request"))).isTrue();
            assertThat(codeFilter.shouldApply(Result.fail("B0500", "Server Error"))).isFalse();
        }
    }

    @Nested
    @DisplayName("Order Tests")
    class OrderTests {

        @Test
        @DisplayName("customizer with custom order should return that order")
        void customizerWithCustomOrderShouldReturnThatOrder() {
            ResultCustomizer customizer = new ResultCustomizer() {
                @Override
                public <T> Result<T> customize(Result<T> result) {
                    return result;
                }

                @Override
                public int getOrder() {
                    return 50;
                }
            };

            assertThat(customizer.getOrder()).isEqualTo(50);
        }
    }

    private static class TestResultCustomizer implements ResultCustomizer {
        @Override
        public <T> Result<T> customize(Result<T> result) {
            return result;
        }
    }
}
