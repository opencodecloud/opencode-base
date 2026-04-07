package cloud.opencode.base.test.http;

import cloud.opencode.base.test.exception.AssertionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Request Verification — Fluent API for verifying recorded HTTP requests.
 * 请求验证 — 用于验证已录制 HTTP 请求的流式 API。
 *
 * <p>Provides a WireMock-style verification builder that filters recorded requests
 * by matcher predicates and asserts call counts, headers, and body content.</p>
 * <p>提供类似 WireMock 风格的验证构建器，通过匹配器谓词过滤已录制请求，
 * 并断言调用次数、请求头和请求体内容。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Filter recorded requests by method and path - 按方法和路径过滤已录制请求</li>
 *   <li>Assert exact, at-least, at-most call counts - 断言精确、至少、至多调用次数</li>
 *   <li>Assert request body content - 断言请求体内容</li>
 *   <li>Assert request header values - 断言请求头值</li>
 *   <li>Fluent chaining for multiple assertions - 流式链式多断言</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Verify GET /api/users was called exactly 2 times
 * server.verify().that(RequestMatcher.get("/api/users")).wasCalled(2);
 *
 * // Verify POST with specific body content
 * server.verify()
 *     .that(RequestMatcher.post("/api/users"))
 *     .wasCalled()
 *     .withBodyContaining("\"name\"");
 *
 * // Verify a request was never made
 * server.verify().that(RequestMatcher.delete("/api/admin")).wasNeverCalled();
 *
 * // Verify header was sent
 * server.verify()
 *     .that(RequestMatcher.get("/api/data"))
 *     .withHeader("authorization", "Bearer token123");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (intended for single-threaded test assertions) -
 *       线程安全: 否（设计用于单线程测试断言）</li>
 *   <li>Null-safe: Yes (null arguments throw NullPointerException) -
 *       空值安全: 是（null 参数抛出 NullPointerException）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see TestHttpServer#verify()
 * @see RequestMatcher
 * @see RecordedRequest
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.3
 */
public final class RequestVerification {

    private final List<RecordedRequest> allRequests;
    private List<RecordedRequest> matchedRequests;

    /**
     * Creates a new request verification against the given recorded requests.
     * 针对给定的已录制请求创建新的请求验证。
     *
     * @param requests the list of all recorded requests | 所有已录制请求的列表
     */
    RequestVerification(List<RecordedRequest> requests) {
        Objects.requireNonNull(requests, "requests must not be null");
        this.allRequests = List.copyOf(requests);
        this.matchedRequests = this.allRequests;
    }

    /**
     * Filters recorded requests by the given matcher.
     * Calling this method replaces any previously filtered results.
     * 按给定匹配器过滤已录制请求。调用此方法会替换之前过滤的结果。
     *
     * @param matcher the request matcher predicate | 请求匹配器谓词
     * @return this verification for fluent chaining | 当前验证实例（支持链式调用）
     * @throws NullPointerException if matcher is null | 如果 matcher 为 null
     */
    public RequestVerification that(RequestMatcher matcher) {
        Objects.requireNonNull(matcher, "matcher must not be null");
        List<RecordedRequest> filtered = new ArrayList<>();
        for (RecordedRequest request : allRequests) {
            if (matcher.matches(request)) {
                filtered.add(request);
            }
        }
        this.matchedRequests = Collections.unmodifiableList(filtered);
        return this;
    }

    /**
     * Asserts that the matched requests were called at least once.
     * 断言匹配的请求至少被调用了一次。
     *
     * @return this verification for fluent chaining | 当前验证实例（支持链式调用）
     * @throws AssertionException if no matching requests were recorded |
     *                            如果没有录制到匹配的请求
     */
    public RequestVerification wasCalled() {
        if (matchedRequests.isEmpty()) {
            throw AssertionException.failed(
                    "Expected at least 1 matching request, but found 0");
        }
        return this;
    }

    /**
     * Asserts that the matched requests were called exactly the specified number of times.
     * 断言匹配的请求恰好被调用了指定的次数。
     *
     * @param times the exact expected call count | 期望的精确调用次数
     * @return this verification for fluent chaining | 当前验证实例（支持链式调用）
     * @throws IllegalArgumentException if times is negative | 如果 times 为负数
     * @throws AssertionException       if actual count does not equal expected |
     *                                  如果实际次数不等于期望次数
     */
    public RequestVerification wasCalled(int times) {
        if (times < 0) {
            throw new IllegalArgumentException("times must not be negative: " + times);
        }
        int actual = matchedRequests.size();
        if (actual != times) {
            throw AssertionException.failed(
                    "Expected exactly " + times + " matching request(s), but found " + actual);
        }
        return this;
    }

    /**
     * Asserts that the matched requests were called at least the specified number of times.
     * 断言匹配的请求至少被调用了指定的次数。
     *
     * @param times the minimum expected call count | 期望的最少调用次数
     * @return this verification for fluent chaining | 当前验证实例（支持链式调用）
     * @throws IllegalArgumentException if times is negative | 如果 times 为负数
     * @throws AssertionException       if actual count is less than expected |
     *                                  如果实际次数少于期望次数
     */
    public RequestVerification wasCalledAtLeast(int times) {
        if (times < 0) {
            throw new IllegalArgumentException("times must not be negative: " + times);
        }
        int actual = matchedRequests.size();
        if (actual < times) {
            throw AssertionException.failed(
                    "Expected at least " + times + " matching request(s), but found " + actual);
        }
        return this;
    }

    /**
     * Asserts that the matched requests were called at most the specified number of times.
     * 断言匹配的请求最多被调用了指定的次数。
     *
     * @param times the maximum expected call count | 期望的最多调用次数
     * @return this verification for fluent chaining | 当前验证实例（支持链式调用）
     * @throws IllegalArgumentException if times is negative | 如果 times 为负数
     * @throws AssertionException       if actual count exceeds expected |
     *                                  如果实际次数超过期望次数
     */
    public RequestVerification wasCalledAtMost(int times) {
        if (times < 0) {
            throw new IllegalArgumentException("times must not be negative: " + times);
        }
        int actual = matchedRequests.size();
        if (actual > times) {
            throw AssertionException.failed(
                    "Expected at most " + times + " matching request(s), but found " + actual);
        }
        return this;
    }

    /**
     * Asserts that no matching requests were recorded.
     * 断言没有录制到匹配的请求。
     *
     * @return this verification for fluent chaining | 当前验证实例（支持链式调用）
     * @throws AssertionException if any matching requests were recorded |
     *                            如果录制到了匹配的请求
     */
    public RequestVerification wasNeverCalled() {
        int actual = matchedRequests.size();
        if (actual != 0) {
            throw AssertionException.failed(
                    "Expected 0 matching requests, but found " + actual);
        }
        return this;
    }

    /**
     * Asserts that all matched requests have a body equal to the expected body.
     * 断言所有匹配的请求的请求体等于期望的请求体。
     *
     * @param expectedBody the expected body content | 期望的请求体内容
     * @return this verification for fluent chaining | 当前验证实例（支持链式调用）
     * @throws NullPointerException if expectedBody is null | 如果 expectedBody 为 null
     * @throws AssertionException   if no matched requests exist, or any matched request
     *                              body does not equal expectedBody |
     *                              如果没有匹配的请求，或任意匹配请求的请求体不等于期望值
     */
    public RequestVerification withBody(String expectedBody) {
        Objects.requireNonNull(expectedBody, "expectedBody must not be null");
        if (matchedRequests.isEmpty()) {
            throw AssertionException.failed(
                    "Cannot verify body: no matching requests found");
        }
        for (RecordedRequest request : matchedRequests) {
            String actualBody = request.bodyAsString();
            if (actualBody == null || !expectedBody.equals(actualBody)) {
                throw AssertionException.failed(
                        "Expected body \"" + expectedBody
                                + "\" but found \"" + actualBody + "\""
                                + " for " + request.method() + " " + request.path());
            }
        }
        return this;
    }

    /**
     * Asserts that all matched requests have a body containing the expected substring.
     * 断言所有匹配的请求的请求体包含期望的子字符串。
     *
     * @param substring the expected substring | 期望的子字符串
     * @return this verification for fluent chaining | 当前验证实例（支持链式调用）
     * @throws NullPointerException if substring is null | 如果 substring 为 null
     * @throws AssertionException   if no matched requests exist, or any matched request
     *                              body does not contain the substring |
     *                              如果没有匹配的请求，或任意匹配请求的请求体不包含该子字符串
     */
    public RequestVerification withBodyContaining(String substring) {
        Objects.requireNonNull(substring, "substring must not be null");
        if (matchedRequests.isEmpty()) {
            throw AssertionException.failed(
                    "Cannot verify body: no matching requests found");
        }
        for (RecordedRequest request : matchedRequests) {
            String actualBody = request.bodyAsString();
            if (actualBody == null || !actualBody.contains(substring)) {
                throw AssertionException.failed(
                        "Expected body to contain \"" + substring
                                + "\" but body was \"" + actualBody + "\""
                                + " for " + request.method() + " " + request.path());
            }
        }
        return this;
    }

    /**
     * Asserts that all matched requests have the specified header with the expected value.
     * 断言所有匹配的请求包含指定名称和期望值的请求头。
     *
     * @param name  the header name (case-insensitive) | 请求头名称（不区分大小写）
     * @param value the expected header value | 期望的请求头值
     * @return this verification for fluent chaining | 当前验证实例（支持链式调用）
     * @throws NullPointerException if name or value is null | 如果 name 或 value 为 null
     * @throws AssertionException   if no matched requests exist, or any matched request
     *                              does not have the header with the expected value |
     *                              如果没有匹配的请求，或任意匹配请求没有包含期望值的请求头
     */
    public RequestVerification withHeader(String name, String value) {
        Objects.requireNonNull(name, "header name must not be null");
        Objects.requireNonNull(value, "header value must not be null");
        if (matchedRequests.isEmpty()) {
            throw AssertionException.failed(
                    "Cannot verify header: no matching requests found");
        }
        for (RecordedRequest request : matchedRequests) {
            String actualValue = request.header(name);
            if (actualValue == null) {
                throw AssertionException.failed(
                        "Expected header \"" + name + "\" to be \"" + value
                                + "\" but header was absent"
                                + " for " + request.method() + " " + request.path());
            }
            if (!value.equals(actualValue)) {
                throw AssertionException.failed(
                        "Expected header \"" + name + "\" to be \"" + value
                                + "\" but was \"" + actualValue + "\""
                                + " for " + request.method() + " " + request.path());
            }
        }
        return this;
    }

    /**
     * Returns the list of matched requests for custom assertions.
     * 返回匹配的请求列表，用于自定义断言。
     *
     * @return unmodifiable list of matched requests | 不可变的匹配请求列表
     */
    public List<RecordedRequest> getMatchedRequests() {
        return matchedRequests;
    }
}
