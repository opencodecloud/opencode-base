package cloud.opencode.base.web;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.web.context.RequestContext;
import cloud.opencode.base.web.context.RequestContextHolder;
import cloud.opencode.base.web.context.UserContext;
import cloud.opencode.base.web.page.PageRequest;
import cloud.opencode.base.web.page.PageResult;
import cloud.opencode.base.web.page.Sort;
import cloud.opencode.base.web.util.WebUtil;

import java.util.List;
import java.util.Map;

/**
 * Open Web
 * 开放Web
 *
 * <p>Main facade for web utilities.</p>
 * <p>Web工具的主要门面。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Result shortcuts - 响应快捷方法</li>
 *   <li>Page shortcuts - 分页快捷方法</li>
 *   <li>Context management - 上下文管理</li>
 *   <li>URL encoding - URL编码</li>
 *   <li>Validation - 验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Success / failure results
 * Result<User> ok   = OpenWeb.ok(user);
 * Result<?>    fail = OpenWeb.fail(CommonResultCode.NOT_FOUND);
 *
 * // Paging
 * PageResult<User> page = OpenWeb.page(users, 1, 10, 100).data();
 *
 * // Request context
 * RequestContext ctx = OpenWeb.getContext();
 * String userId = OpenWeb.getUserId();
 *
 * // URL helpers
 * String encoded = OpenWeb.urlEncode("hello world");
 * boolean validEmail = OpenWeb.isValidEmail("user@example.com");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Partial (delegates to underlying utilities) - 空值安全: 部分（委托给底层工具）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class OpenWeb {

    private OpenWeb() {
        // Utility class
    }

    // === Result Shortcuts ===

    /**
     * Create success result
     * 创建成功结果
     *
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> ok() {
        return Result.ok();
    }

    /**
     * Create success result with data
     * 创建带数据的成功结果
     *
     * @param data the data | 数据
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> ok(T data) {
        return Result.ok(data);
    }

    /**
     * Create success result with message and data
     * 创建带消息和数据的成功结果
     *
     * @param message the message | 消息
     * @param data the data | 数据
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> ok(String message, T data) {
        return Result.ok(message, data);
    }

    /**
     * Create failure result
     * 创建失败结果
     *
     * @param message the message | 消息
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> fail(String message) {
        return Result.fail(CommonResultCode.BUSINESS_ERROR, message);
    }

    /**
     * Create failure result with code
     * 创建带代码的失败结果
     *
     * @param code the code | 代码
     * @param message the message | 消息
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> fail(String code, String message) {
        return Result.fail(code, message);
    }

    /**
     * Create failure result with result code
     * 使用响应码创建失败结果
     *
     * @param resultCode the result code | 响应码
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return Result.fail(resultCode);
    }

    /**
     * Create failure result from exception
     * 从异常创建失败结果
     *
     * @param throwable the exception | 异常
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> fail(Throwable throwable) {
        return Result.fail(throwable);
    }

    // === Page Shortcuts ===

    /**
     * Create page result
     * 创建分页结果
     *
     * @param items the items | 项列表
     * @param page the page number | 页码
     * @param size the page size | 页大小
     * @param total the total count | 总数
     * @param <T> the item type | 项类型
     * @return the page result | 分页结果
     */
    public static <T> PageResult<T> page(List<T> items, int page, int size, long total) {
        return PageResult.of(items, total, page, size);
    }

    /**
     * Create page request
     * 创建分页请求
     *
     * @param page the page number | 页码
     * @param size the page size | 页大小
     * @return the page request | 分页请求
     */
    public static PageRequest pageRequest(int page, int size) {
        return PageRequest.of(page, size);
    }

    /**
     * Create page request with sort
     * 创建带排序的分页请求
     *
     * @param page the page number | 页码
     * @param size the page size | 页大小
     * @param sortBy the sort field | 排序字段
     * @param sortOrder the sort order (asc/desc) | 排序顺序
     * @return the page request | 分页请求
     */
    public static PageRequest pageRequest(int page, int size, String sortBy, String sortOrder) {
        return PageRequest.of(page, size, sortBy, sortOrder);
    }

    /**
     * Create page request with sort
     * 创建带排序的分页请求
     *
     * @param page the page number | 页码
     * @param size the page size | 页大小
     * @param sort the sort criteria | 排序条件
     * @return the page request | 分页请求
     */
    public static PageRequest pageRequest(int page, int size, Sort sort) {
        return PageRequest.of(page, size, sort);
    }

    // === Context Shortcuts ===

    /**
     * Get current request context
     * 获取当前请求上下文
     *
     * @return the request context or null | 请求上下文或null
     */
    public static RequestContext getContext() {
        return RequestContextHolder.getContext();
    }

    /**
     * Set current request context
     * 设置当前请求上下文
     *
     * @param context the request context | 请求上下文
     */
    public static void setContext(RequestContext context) {
        RequestContextHolder.setContext(context);
    }

    /**
     * Clear current request context
     * 清除当前请求上下文
     */
    public static void clearContext() {
        RequestContextHolder.clear();
    }

    /**
     * Get current user context
     * 获取当前用户上下文
     *
     * @return the user context or null | 用户上下文或null
     */
    public static UserContext getUser() {
        return RequestContextHolder.getUser();
    }

    /**
     * Get current user ID
     * 获取当前用户ID
     *
     * @return the user ID or null | 用户ID或null
     */
    public static String getUserId() {
        return RequestContextHolder.getUserId();
    }

    /**
     * Get current trace ID
     * 获取当前追踪ID
     *
     * @return the trace ID or null | 追踪ID或null
     */
    public static String getTraceId() {
        return RequestContextHolder.getTraceId();
    }

    // === URL Encoding ===

    /**
     * URL encode
     * URL编码
     *
     * @param value the value to encode | 要编码的值
     * @return the encoded value | 编码后的值
     */
    public static String urlEncode(String value) {
        return WebUtil.urlEncode(value);
    }

    /**
     * URL decode
     * URL解码
     *
     * @param value the value to decode | 要解码的值
     * @return the decoded value | 解码后的值
     */
    public static String urlDecode(String value) {
        return WebUtil.urlDecode(value);
    }

    // === Base64 ===

    /**
     * Base64 encode
     * Base64编码
     *
     * @param value the value to encode | 要编码的值
     * @return the encoded value | 编码后的值
     */
    public static String base64Encode(String value) {
        return OpenBase64.encode(value);
    }

    /**
     * Base64 decode
     * Base64解码
     *
     * @param value the value to decode | 要解码的值
     * @return the decoded value | 解码后的值
     */
    public static String base64Decode(String value) {
        return OpenBase64.decodeToString(value);
    }

    /**
     * Base64 URL encode
     * Base64 URL编码
     *
     * @param value the value to encode | 要编码的值
     * @return the encoded value | 编码后的值
     */
    public static String base64UrlEncode(String value) {
        return OpenBase64.encodeUrlSafe(value);
    }

    /**
     * Base64 URL decode
     * Base64 URL解码
     *
     * @param value the value to decode | 要解码的值
     * @return the decoded value | 解码后的值
     */
    public static String base64UrlDecode(String value) {
        return OpenBase64.decodeUrlSafeToString(value);
    }

    // === Query String ===

    /**
     * Parse query string
     * 解析查询字符串
     *
     * @param queryString the query string | 查询字符串
     * @return the parameters map | 参数映射
     */
    public static Map<String, String> parseQuery(String queryString) {
        return WebUtil.parseQueryString(queryString);
    }

    /**
     * Build query string
     * 构建查询字符串
     *
     * @param params the parameters | 参数
     * @return the query string | 查询字符串
     */
    public static String buildQuery(Map<String, String> params) {
        return WebUtil.buildQueryString(params);
    }

    // === Validation ===

    /**
     * Check if valid IP
     * 检查是否有效IP
     *
     * @param ip the IP to check | 要检查的IP
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValidIp(String ip) {
        return WebUtil.isValidIp(ip);
    }

    /**
     * Check if valid email
     * 检查是否有效邮箱
     *
     * @param email the email to check | 要检查的邮箱
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValidEmail(String email) {
        return WebUtil.isValidEmail(email);
    }

    /**
     * Check if valid URL
     * 检查是否有效URL
     *
     * @param url the URL to check | 要检查的URL
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValidUrl(String url) {
        return WebUtil.isValidUrl(url);
    }

    /**
     * Check if private IP
     * 检查是否私有IP
     *
     * @param ip the IP to check | 要检查的IP
     * @return true if private | 如果是私有IP返回true
     */
    public static boolean isPrivateIp(String ip) {
        return WebUtil.isPrivateIp(ip);
    }
}
