package cloud.opencode.base.web;

import cloud.opencode.base.web.page.PageInfo;
import cloud.opencode.base.web.page.PageResult;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ResultsTest Tests
 * ResultsTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("Results Tests")
class ResultsTest {

    @Nested
    @DisplayName("Success Results Tests")
    class SuccessResultsTests {

        @Test
        @DisplayName("ok should create success result without data")
        void okShouldCreateSuccessResultWithoutData() {
            Result<String> result = Results.ok();

            assertThat(result.success()).isTrue();
            assertThat(result.code()).isEqualTo(CommonResultCode.SUCCESS.getCode());
            assertThat(result.message()).isEqualTo("Success");
            assertThat(result.data()).isNull();
        }

        @Test
        @DisplayName("ok with data should create success result with data")
        void okWithDataShouldCreateSuccessResultWithData() {
            Result<String> result = Results.ok("test data");

            assertThat(result.success()).isTrue();
            assertThat(result.data()).isEqualTo("test data");
        }

        @Test
        @DisplayName("ok with message and data should create success result")
        void okWithMessageAndDataShouldCreateSuccessResult() {
            Result<Integer> result = Results.ok("Operation successful", 42);

            assertThat(result.success()).isTrue();
            assertThat(result.message()).isEqualTo("Operation successful");
            assertThat(result.data()).isEqualTo(42);
        }

        @Test
        @DisplayName("ok with result code should create success result")
        void okWithResultCodeShouldCreateSuccessResult() {
            Result<String> result = Results.ok(CommonResultCode.CREATED, "new item");

            assertThat(result.success()).isTrue();
            assertThat(result.code()).isEqualTo(CommonResultCode.CREATED.getCode());
            assertThat(result.message()).isEqualTo(CommonResultCode.CREATED.getMessage());
            assertThat(result.data()).isEqualTo("new item");
        }
    }

    @Nested
    @DisplayName("Failure Results Tests")
    class FailureResultsTests {

        @Test
        @DisplayName("fail with code and message should create failure result")
        void failWithCodeAndMessageShouldCreateFailureResult() {
            Result<String> result = Results.fail("E001", "Error message");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo("E001");
            assertThat(result.message()).isEqualTo("Error message");
            assertThat(result.data()).isNull();
        }

        @Test
        @DisplayName("fail with result code should create failure result")
        void failWithResultCodeShouldCreateFailureResult() {
            Result<String> result = Results.fail(CommonResultCode.BAD_REQUEST);

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(CommonResultCode.BAD_REQUEST.getCode());
            assertThat(result.message()).isEqualTo(CommonResultCode.BAD_REQUEST.getMessage());
        }

        @Test
        @DisplayName("fail with result code and custom message should create failure result")
        void failWithResultCodeAndCustomMessageShouldCreateFailureResult() {
            Result<String> result = Results.fail(CommonResultCode.VALIDATION_ERROR, "Custom error");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(CommonResultCode.VALIDATION_ERROR.getCode());
            assertThat(result.message()).isEqualTo("Custom error");
        }

        @Test
        @DisplayName("fail with exception should create failure result")
        void failWithExceptionShouldCreateFailureResult() {
            Result<String> result = Results.fail(new RuntimeException("Test exception"));

            assertThat(result.success()).isFalse();
            assertThat(result.message()).contains("Test exception");
        }

        @Test
        @DisplayName("fail with code, message and data should create failure result")
        void failWithCodeMessageAndDataShouldCreateFailureResult() {
            Result<String> result = Results.fail("E001", "Error", "error data");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo("E001");
            assertThat(result.data()).isEqualTo("error data");
        }
    }

    @Nested
    @DisplayName("Page Results Tests")
    class PageResultsTests {

        @Test
        @DisplayName("page should create page result with items and page info")
        void pageShouldCreatePageResultWithItemsAndPageInfo() {
            List<String> items = List.of("item1", "item2");
            PageInfo pageInfo = PageInfo.of(1, 10, 2);

            Result<PageResult<String>> result = Results.page(items, pageInfo);

            assertThat(result.success()).isTrue();
            assertThat(result.data().items()).containsExactly("item1", "item2");
            assertThat(result.data().getTotal()).isEqualTo(2);
        }

        @Test
        @DisplayName("page should create page result with items, total, page and size")
        void pageShouldCreatePageResultWithParameters() {
            List<String> items = List.of("item1", "item2", "item3");

            Result<PageResult<String>> result = Results.page(items, 100, 1, 10);

            assertThat(result.success()).isTrue();
            assertThat(result.data().items()).hasSize(3);
            assertThat(result.data().getTotal()).isEqualTo(100);
            assertThat(result.data().getPage()).isEqualTo(1);
            assertThat(result.data().getSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("emptyPage should create empty page result")
        void emptyPageShouldCreateEmptyPageResult() {
            Result<PageResult<String>> result = Results.emptyPage();

            assertThat(result.success()).isTrue();
            assertThat(result.data().items()).isEmpty();
            assertThat(result.data().getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("emptyPage with page and size should create empty page result")
        void emptyPageWithPageAndSizeShouldCreateEmptyPageResult() {
            Result<PageResult<String>> result = Results.emptyPage(2, 20);

            assertThat(result.success()).isTrue();
            assertThat(result.data().items()).isEmpty();
            assertThat(result.data().getPage()).isEqualTo(2);
            assertThat(result.data().getSize()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder should create result with all fields")
        void builderShouldCreateResultWithAllFields() {
            Result<String> result = Results.<String>builder()
                .code("00000")
                .message("Custom message")
                .data("test data")
                .success(true)
                .traceId("trace-123")
                .build();

            assertThat(result.code()).isEqualTo("00000");
            assertThat(result.message()).isEqualTo("Custom message");
            assertThat(result.data()).isEqualTo("test data");
            assertThat(result.success()).isTrue();
            assertThat(result.traceId()).isEqualTo("trace-123");
        }

        @Test
        @DisplayName("builder with result code should set code message and success")
        void builderWithResultCodeShouldSetCodeMessageAndSuccess() {
            Result<String> result = Results.<String>builder()
                .code(CommonResultCode.BAD_REQUEST)
                .build();

            assertThat(result.code()).isEqualTo(CommonResultCode.BAD_REQUEST.getCode());
            assertThat(result.message()).isEqualTo(CommonResultCode.BAD_REQUEST.getMessage());
            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("builder should use default values when not set")
        void builderShouldUseDefaultValuesWhenNotSet() {
            Result<String> result = Results.<String>builder().build();

            assertThat(result.code()).isEqualTo(CommonResultCode.SUCCESS.getCode());
            assertThat(result.message()).isEqualTo("Success");
            assertThat(result.success()).isTrue();
            assertThat(result.traceId()).isNotNull();
        }

        @Test
        @DisplayName("builder should allow custom trace ID")
        void builderShouldAllowCustomTraceId() {
            String customTraceId = "custom-trace-id";

            Result<String> result = Results.<String>builder()
                .traceId(customTraceId)
                .build();

            assertThat(result.traceId()).isEqualTo(customTraceId);
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("results should have timestamp")
        void resultsShouldHaveTimestamp() {
            Result<String> okResult = Results.ok();
            Result<String> failResult = Results.fail("E001", "Error");

            assertThat(okResult.timestamp()).isNotNull();
            assertThat(failResult.timestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Trace ID Tests")
    class TraceIdTests {

        @Test
        @DisplayName("results should have trace ID")
        void resultsShouldHaveTraceId() {
            Result<String> okResult = Results.ok();
            Result<String> failResult = Results.fail("E001", "Error");

            assertThat(okResult.traceId()).isNotNull();
            assertThat(failResult.traceId()).isNotNull();
        }
    }
}
