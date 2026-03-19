package cloud.opencode.base.web;

/**
 * Common Result Code
 * 通用响应码
 *
 * <p>Predefined common result codes for API responses.</p>
 * <p>API响应的预定义通用响应码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Comprehensive predefined result codes - 全面的预定义响应码</li>
 *   <li>HTTP status code mapping - HTTP 状态码映射</li>
 *   <li>Bilingual messages (English + Chinese) - 双语消息（英文 + 中文）</li>
 *   <li>Fast code lookup via static cache - 通过静态缓存快速查找代码</li>
 * </ul>
 *
 * <p><strong>Code Format | 编码格式:</strong></p>
 * <ul>
 *   <li>Success: 00000 - 成功</li>
 *   <li>Client Error: A0xxx - 客户端错误</li>
 *   <li>Server Error: B0xxx - 服务端错误</li>
 *   <li>Third-party Error: C0xxx - 第三方错误</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use in Result creation
 * Result<?> result = Result.fail(CommonResultCode.NOT_FOUND);
 *
 * // Lookup by code string
 * CommonResultCode code = CommonResultCode.fromCode("A0404");
 *
 * // Get HTTP status
 * int status = CommonResultCode.UNAUTHORIZED.getHttpStatus(); // 401
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum, immutable) - 线程安全: 是（枚举，不可变）</li>
 *   <li>Null-safe: Yes (fromCode returns null for unknown codes) - 空值安全: 是（fromCode 对未知代码返回 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public enum CommonResultCode implements ResultCode {

    // === Success (00xxx) ===
    SUCCESS("00000", "Success | 成功", 200),
    CREATED("00001", "Created | 创建成功", 201),
    UPDATED("00002", "Updated | 更新成功", 200),
    DELETED("00003", "Deleted | 删除成功", 200),
    ACCEPTED("00004", "Accepted | 已接受", 202),
    NO_CONTENT("00005", "No Content | 无内容", 204),
    PARTIAL_CONTENT("00006", "Partial Content | 部分内容", 206),
    QUEUED("00007", "Queued | 已排队", 202),
    PROCESSING("00008", "Processing | 处理中", 202),

    // === Client Errors (A0xxx) ===
    BAD_REQUEST("A0400", "Bad Request | 请求错误", 400),
    UNAUTHORIZED("A0401", "Unauthorized | 未授权", 401),
    FORBIDDEN("A0403", "Forbidden | 禁止访问", 403),
    NOT_FOUND("A0404", "Not Found | 资源不存在", 404),
    METHOD_NOT_ALLOWED("A0405", "Method Not Allowed | 方法不允许", 405),
    NOT_ACCEPTABLE("A0406", "Not Acceptable | 不可接受", 406),
    REQUEST_TIMEOUT("A0408", "Request Timeout | 请求超时", 408),
    CONFLICT("A0409", "Conflict | 冲突", 409),
    GONE("A0410", "Gone | 资源已删除", 410),
    PAYLOAD_TOO_LARGE("A0413", "Payload Too Large | 请求体过大", 413),
    URI_TOO_LONG("A0414", "URI Too Long | URI过长", 414),
    UNSUPPORTED_MEDIA_TYPE("A0415", "Unsupported Media Type | 不支持的媒体类型", 415),
    VALIDATION_ERROR("A0422", "Validation Error | 验证错误", 422),
    LOCKED("A0423", "Locked | 资源已锁定", 423),
    TOO_MANY_REQUESTS("A0429", "Too Many Requests | 请求过多", 429),
    HEADER_TOO_LARGE("A0431", "Header Too Large | 请求头过大", 431),

    // === Parameter Errors (A1xxx) ===
    PARAM_MISSING("A1001", "Parameter Missing | 参数缺失", 400),
    PARAM_INVALID("A1002", "Parameter Invalid | 参数无效", 400),
    PARAM_TYPE_ERROR("A1003", "Parameter Type Error | 参数类型错误", 400),
    PARAM_FORMAT_ERROR("A1004", "Parameter Format Error | 参数格式错误", 400),
    PARAM_RANGE_ERROR("A1005", "Parameter Out of Range | 参数超出范围", 400),
    PARAM_LENGTH_ERROR("A1006", "Parameter Length Error | 参数长度错误", 400),
    PARAM_PATTERN_ERROR("A1007", "Parameter Pattern Mismatch | 参数格式不匹配", 400),
    PARAM_DEPENDENCY_ERROR("A1008", "Parameter Dependency Error | 参数依赖错误", 400),

    // === Auth Errors (A2xxx) ===
    TOKEN_EXPIRED("A2001", "Token Expired | 令牌过期", 401),
    TOKEN_INVALID("A2002", "Token Invalid | 令牌无效", 401),
    PERMISSION_DENIED("A2003", "Permission Denied | 权限不足", 403),
    ACCOUNT_LOCKED("A2004", "Account Locked | 账户已锁定", 403),
    ACCOUNT_DISABLED("A2005", "Account Disabled | 账户已禁用", 403),
    ACCOUNT_NOT_VERIFIED("A2006", "Account Not Verified | 账户未验证", 403),
    SESSION_EXPIRED("A2007", "Session Expired | 会话过期", 401),
    CREDENTIALS_INVALID("A2008", "Invalid Credentials | 凭证无效", 401),
    MFA_REQUIRED("A2009", "MFA Required | 需要多因素认证", 401),
    MFA_INVALID("A2010", "MFA Code Invalid | 多因素认证码无效", 401),
    IP_BLOCKED("A2011", "IP Blocked | IP已被阻止", 403),
    DEVICE_NOT_TRUSTED("A2012", "Device Not Trusted | 设备未信任", 403),

    // === File/Upload Errors (A3xxx) ===
    FILE_NOT_FOUND("A3001", "File Not Found | 文件不存在", 404),
    FILE_TYPE_NOT_ALLOWED("A3002", "File Type Not Allowed | 文件类型不允许", 400),
    FILE_TOO_LARGE("A3003", "File Too Large | 文件过大", 413),
    FILE_EMPTY("A3004", "File Empty | 文件为空", 400),
    FILE_UPLOAD_FAILED("A3005", "File Upload Failed | 文件上传失败", 500),
    FILE_CORRUPTED("A3006", "File Corrupted | 文件损坏", 400),
    FILE_VIRUS_DETECTED("A3007", "Virus Detected | 检测到病毒", 400),

    // === Rate Limit Errors (A4xxx) ===
    RATE_LIMIT_EXCEEDED("A4001", "Rate Limit Exceeded | 超出速率限制", 429),
    QUOTA_EXCEEDED("A4002", "Quota Exceeded | 超出配额", 429),
    CONCURRENT_LIMIT("A4003", "Concurrent Request Limit | 并发请求限制", 429),
    DAILY_LIMIT_EXCEEDED("A4004", "Daily Limit Exceeded | 超出日限制", 429),

    // === Server Errors (B0xxx) ===
    INTERNAL_ERROR("B0500", "Internal Server Error | 服务器内部错误", 500),
    NOT_IMPLEMENTED("B0501", "Not Implemented | 未实现", 501),
    BAD_GATEWAY("B0502", "Bad Gateway | 网关错误", 502),
    SERVICE_UNAVAILABLE("B0503", "Service Unavailable | 服务不可用", 503),
    GATEWAY_TIMEOUT("B0504", "Gateway Timeout | 网关超时", 504),
    BANDWIDTH_LIMIT("B0509", "Bandwidth Limit Exceeded | 带宽限制", 509),

    // === Business Errors (B1xxx) ===
    BUSINESS_ERROR("B1001", "Business Error | 业务错误", 500),
    DATA_NOT_FOUND("B1002", "Data Not Found | 数据不存在", 404),
    DATA_DUPLICATE("B1003", "Data Duplicate | 数据重复", 409),
    OPERATION_FAILED("B1004", "Operation Failed | 操作失败", 500),
    INSUFFICIENT_BALANCE("B1005", "Insufficient Balance | 余额不足", 400),
    ORDER_EXPIRED("B1006", "Order Expired | 订单过期", 400),
    INVENTORY_INSUFFICIENT("B1007", "Inventory Insufficient | 库存不足", 400),
    TRANSACTION_FAILED("B1008", "Transaction Failed | 交易失败", 500),
    WORKFLOW_ERROR("B1009", "Workflow Error | 工作流错误", 500),
    STATE_TRANSITION_ERROR("B1010", "Invalid State Transition | 状态转换错误", 400),

    // === Database Errors (B2xxx) ===
    DATABASE_ERROR("B2001", "Database Error | 数据库错误", 500),
    DATABASE_TIMEOUT("B2002", "Database Timeout | 数据库超时", 504),
    DATABASE_CONNECTION_FAILED("B2003", "Database Connection Failed | 数据库连接失败", 503),
    CONSTRAINT_VIOLATION("B2004", "Constraint Violation | 约束冲突", 409),
    DEADLOCK_DETECTED("B2005", "Deadlock Detected | 检测到死锁", 409),
    DATA_INTEGRITY_ERROR("B2006", "Data Integrity Error | 数据完整性错误", 500),

    // === Concurrency Errors (B3xxx) ===
    OPTIMISTIC_LOCK_ERROR("B3001", "Optimistic Lock Error | 乐观锁错误", 409),
    RESOURCE_BUSY("B3002", "Resource Busy | 资源忙", 409),
    VERSION_CONFLICT("B3003", "Version Conflict | 版本冲突", 409),

    // === Third-party Errors (C0xxx) ===
    THIRD_PARTY_ERROR("C0001", "Third Party Error | 第三方错误", 502),
    REMOTE_SERVICE_ERROR("C0002", "Remote Service Error | 远程服务错误", 502),
    NETWORK_ERROR("C0003", "Network Error | 网络错误", 502),
    EXTERNAL_API_ERROR("C0004", "External API Error | 外部API错误", 502),
    EXTERNAL_TIMEOUT("C0005", "External Service Timeout | 外部服务超时", 504),
    EXTERNAL_UNAVAILABLE("C0006", "External Service Unavailable | 外部服务不可用", 503),

    // === Payment Errors (C1xxx) ===
    PAYMENT_FAILED("C1001", "Payment Failed | 支付失败", 402),
    PAYMENT_DECLINED("C1002", "Payment Declined | 支付被拒绝", 402),
    PAYMENT_EXPIRED("C1003", "Payment Expired | 支付过期", 410),
    REFUND_FAILED("C1004", "Refund Failed | 退款失败", 500),

    // === Messaging Errors (C2xxx) ===
    SMS_SEND_FAILED("C2001", "SMS Send Failed | 短信发送失败", 502),
    EMAIL_SEND_FAILED("C2002", "Email Send Failed | 邮件发送失败", 502),
    PUSH_NOTIFICATION_FAILED("C2003", "Push Notification Failed | 推送失败", 502);

    private final String code;
    private final String message;
    private final int httpStatus;

    CommonResultCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    // === Static lookup cache ===
    private static final java.util.Map<String, CommonResultCode> CODE_MAP;

    static {
        java.util.Map<String, CommonResultCode> map = new java.util.HashMap<>();
        for (CommonResultCode value : values()) {
            map.put(value.code, value);
        }
        CODE_MAP = java.util.Collections.unmodifiableMap(map);
    }

    /**
     * Find result code by code string
     * 根据code字符串查找响应码
     *
     * @param code the code | 响应码
     * @return result code or null | 响应码或null
     */
    public static CommonResultCode fromCode(String code) {
        return CODE_MAP.get(code);
    }

    /**
     * Find result code by code string with default
     * 根据code字符串查找响应码（带默认值）
     *
     * @param code the code | 响应码
     * @param defaultCode the default code | 默认响应码
     * @return result code | 响应码
     */
    public static CommonResultCode fromCode(String code, CommonResultCode defaultCode) {
        CommonResultCode result = CODE_MAP.get(code);
        return result != null ? result : defaultCode;
    }
}
