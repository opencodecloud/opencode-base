package cloud.opencode.base.web;

import cloud.opencode.base.web.context.RequestContext;
import cloud.opencode.base.web.context.RequestContextHolder;
import cloud.opencode.base.web.context.UserContext;
import cloud.opencode.base.web.page.PageRequest;
import cloud.opencode.base.web.page.PageResult;
import cloud.opencode.base.web.page.Sort;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenWebTest Tests
 * OpenWebTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("OpenWeb Tests")
class OpenWebTest {

    @BeforeEach
    void setUp() {
        RequestContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Nested
    @DisplayName("Result Shortcuts Tests")
    class ResultShortcutsTests {

        @Test
        @DisplayName("ok should create success result")
        void okShouldCreateSuccessResult() {
            Result<String> result = OpenWeb.ok();

            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("ok with data should create success result with data")
        void okWithDataShouldCreateSuccessResultWithData() {
            Result<String> result = OpenWeb.ok("test data");

            assertThat(result.success()).isTrue();
            assertThat(result.data()).isEqualTo("test data");
        }

        @Test
        @DisplayName("ok with message and data should create success result")
        void okWithMessageAndDataShouldCreateSuccessResult() {
            Result<Integer> result = OpenWeb.ok("Message", 42);

            assertThat(result.success()).isTrue();
            assertThat(result.message()).isEqualTo("Message");
            assertThat(result.data()).isEqualTo(42);
        }

        @Test
        @DisplayName("fail with message should create failure result")
        void failWithMessageShouldCreateFailureResult() {
            Result<String> result = OpenWeb.fail("Error message");

            assertThat(result.success()).isFalse();
            assertThat(result.message()).isEqualTo("Error message");
        }

        @Test
        @DisplayName("fail with code and message should create failure result")
        void failWithCodeAndMessageShouldCreateFailureResult() {
            Result<String> result = OpenWeb.fail("E001", "Error message");

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo("E001");
        }

        @Test
        @DisplayName("fail with result code should create failure result")
        void failWithResultCodeShouldCreateFailureResult() {
            Result<String> result = OpenWeb.fail(CommonResultCode.BAD_REQUEST);

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(CommonResultCode.BAD_REQUEST.getCode());
        }

        @Test
        @DisplayName("fail with exception should create failure result")
        void failWithExceptionShouldCreateFailureResult() {
            Result<String> result = OpenWeb.fail(new RuntimeException("Test error"));

            assertThat(result.success()).isFalse();
        }
    }

    @Nested
    @DisplayName("Page Shortcuts Tests")
    class PageShortcutsTests {

        @Test
        @DisplayName("page should create page result")
        void pageShouldCreatePageResult() {
            List<String> items = List.of("item1", "item2");

            PageResult<String> result = OpenWeb.page(items, 1, 10, 2);

            assertThat(result.items()).containsExactly("item1", "item2");
            assertThat(result.getPage()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTotal()).isEqualTo(2);
        }

        @Test
        @DisplayName("pageRequest should create page request")
        void pageRequestShouldCreatePageRequest() {
            PageRequest request = OpenWeb.pageRequest(1, 20);

            assertThat(request.page()).isEqualTo(1);
            assertThat(request.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("pageRequest with sort should create page request with sort")
        void pageRequestWithSortShouldCreatePageRequestWithSort() {
            PageRequest request = OpenWeb.pageRequest(1, 20, "name", "asc");

            assertThat(request.page()).isEqualTo(1);
            assertThat(request.size()).isEqualTo(20);
            assertThat(request.hasSort()).isTrue();
        }

        @Test
        @DisplayName("pageRequest with Sort object should create page request")
        void pageRequestWithSortObjectShouldCreatePageRequest() {
            Sort sort = Sort.desc("createdAt");

            PageRequest request = OpenWeb.pageRequest(1, 20, sort);

            assertThat(request.sort()).isEqualTo(sort);
        }
    }

    @Nested
    @DisplayName("Context Shortcuts Tests")
    class ContextShortcutsTests {

        @Test
        @DisplayName("getContext should return null when not set")
        void getContextShouldReturnNullWhenNotSet() {
            RequestContext context = OpenWeb.getContext();

            assertThat(context).isNull();
        }

        @Test
        @DisplayName("setContext should set the context")
        void setContextShouldSetTheContext() {
            RequestContext context = RequestContext.of("trace-123");

            OpenWeb.setContext(context);

            assertThat(OpenWeb.getContext()).isEqualTo(context);
        }

        @Test
        @DisplayName("clearContext should clear the context")
        void clearContextShouldClearTheContext() {
            OpenWeb.setContext(RequestContext.of("trace-123"));

            OpenWeb.clearContext();

            assertThat(OpenWeb.getContext()).isNull();
        }

        @Test
        @DisplayName("getUser should return user from context")
        void getUserShouldReturnUserFromContext() {
            UserContext user = UserContext.of("user1", "testuser");
            RequestContext context = RequestContext.builder()
                .traceId("trace-123")
                .user(user)
                .build();
            OpenWeb.setContext(context);

            UserContext result = OpenWeb.getUser();

            assertThat(result).isEqualTo(user);
        }

        @Test
        @DisplayName("getUserId should return user ID from context")
        void getUserIdShouldReturnUserIdFromContext() {
            UserContext user = UserContext.of("user1", "testuser");
            RequestContext context = RequestContext.builder()
                .traceId("trace-123")
                .user(user)
                .build();
            OpenWeb.setContext(context);

            String userId = OpenWeb.getUserId();

            assertThat(userId).isEqualTo("user1");
        }

        @Test
        @DisplayName("getTraceId should return trace ID from context")
        void getTraceIdShouldReturnTraceIdFromContext() {
            RequestContext context = RequestContext.of("trace-123");
            OpenWeb.setContext(context);

            String traceId = OpenWeb.getTraceId();

            assertThat(traceId).isEqualTo("trace-123");
        }
    }

    @Nested
    @DisplayName("URL Encoding Tests")
    class UrlEncodingTests {

        @Test
        @DisplayName("urlEncode should encode URL")
        void urlEncodeShouldEncodeUrl() {
            String result = OpenWeb.urlEncode("hello world");

            assertThat(result).isEqualTo("hello+world");
        }

        @Test
        @DisplayName("urlDecode should decode URL")
        void urlDecodeShouldDecodeUrl() {
            String result = OpenWeb.urlDecode("hello+world");

            assertThat(result).isEqualTo("hello world");
        }
    }

    @Nested
    @DisplayName("Base64 Tests")
    class Base64Tests {

        @Test
        @DisplayName("base64Encode should encode string")
        void base64EncodeShouldEncodeString() {
            String result = OpenWeb.base64Encode("hello");

            assertThat(result).isEqualTo("aGVsbG8=");
        }

        @Test
        @DisplayName("base64Decode should decode string")
        void base64DecodeShouldDecodeString() {
            String result = OpenWeb.base64Decode("aGVsbG8=");

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("base64UrlEncode should encode URL-safe string")
        void base64UrlEncodeShouldEncodeUrlSafeString() {
            String result = OpenWeb.base64UrlEncode("hello");

            assertThat(result).isEqualTo("aGVsbG8");
        }

        @Test
        @DisplayName("base64UrlDecode should decode URL-safe string")
        void base64UrlDecodeShouldDecodeUrlSafeString() {
            String result = OpenWeb.base64UrlDecode("aGVsbG8");

            assertThat(result).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("Query String Tests")
    class QueryStringTests {

        @Test
        @DisplayName("parseQuery should parse query string")
        void parseQueryShouldParseQueryString() {
            var result = OpenWeb.parseQuery("name=test&value=123");

            assertThat(result).containsEntry("name", "test");
            assertThat(result).containsEntry("value", "123");
        }

        @Test
        @DisplayName("buildQuery should build query string")
        void buildQueryShouldBuildQueryString() {
            var params = java.util.Map.of("name", "test", "value", "123");

            String result = OpenWeb.buildQuery(params);

            assertThat(result).contains("name=test");
            assertThat(result).contains("value=123");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("isValidIp should validate IP address")
        void isValidIpShouldValidateIpAddress() {
            assertThat(OpenWeb.isValidIp("192.168.1.1")).isTrue();
            assertThat(OpenWeb.isValidIp("invalid")).isFalse();
        }

        @Test
        @DisplayName("isValidEmail should validate email")
        void isValidEmailShouldValidateEmail() {
            assertThat(OpenWeb.isValidEmail("test@example.com")).isTrue();
            assertThat(OpenWeb.isValidEmail("invalid")).isFalse();
        }

        @Test
        @DisplayName("isValidUrl should validate URL")
        void isValidUrlShouldValidateUrl() {
            assertThat(OpenWeb.isValidUrl("https://example.com")).isTrue();
            assertThat(OpenWeb.isValidUrl("invalid")).isFalse();
        }

        @Test
        @DisplayName("isPrivateIp should check if IP is private")
        void isPrivateIpShouldCheckIfIpIsPrivate() {
            assertThat(OpenWeb.isPrivateIp("192.168.1.1")).isTrue();
            assertThat(OpenWeb.isPrivateIp("8.8.8.8")).isFalse();
        }
    }
}
