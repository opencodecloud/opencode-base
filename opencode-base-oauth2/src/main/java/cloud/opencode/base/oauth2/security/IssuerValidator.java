package cloud.opencode.base.oauth2.security;

import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Authorization Server Issuer Validator (RFC 9207)
 * 授权服务器颁发者验证器（RFC 9207）
 *
 * <p>Validates the authorization server issuer identifier as defined in RFC 9207.
 * Uses constant-time comparison via {@link MessageDigest#isEqual} to prevent
 * timing side-channel attacks when comparing issuer values.</p>
 * <p>验证 RFC 9207 定义的授权服务器颁发者标识符。使用 {@link MessageDigest#isEqual}
 * 进行恒定时间比较，以防止比较颁发者值时的时序侧信道攻击。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 9207 compliant issuer identification - 符合 RFC 9207 的颁发者识别</li>
 *   <li>Constant-time comparison to prevent timing attacks - 恒定时间比较防止时序攻击</li>
 *   <li>Null-safe with configurable validation - 空值安全，支持可配置验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Validate issuer in authorization response
 * // 验证授权响应中的颁发者
 * IssuerValidator.validate("https://auth.example.com", responseIssuer);
 *
 * // Check without throwing
 * // 不抛异常地检查
 * if (IssuerValidator.matches("https://auth.example.com", responseIssuer)) {
 *     // Issuer is valid
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 *   <li>Uses constant-time comparison (MessageDigest.isEqual) - 使用恒定时间比较</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc9207">RFC 9207 - OAuth 2.0 Authorization Server Issuer Identification</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
public final class IssuerValidator {

    private IssuerValidator() {
        // Utility class - prevent instantiation
    }

    /**
     * Validate that the actual issuer matches the expected issuer.
     * 验证实际颁发者是否匹配预期颁发者。
     *
     * <p>If expectedIssuer is null, validation is skipped (issuer validation not configured).
     * If actualIssuer is null, an exception is thrown.</p>
     * <p>如果 expectedIssuer 为 null，则跳过验证（颁发者验证未配置）。
     * 如果 actualIssuer 为 null，则抛出异常。</p>
     *
     * @param expectedIssuer the expected issuer (null to skip validation) | 预期颁发者（null 跳过验证）
     * @param actualIssuer   the actual issuer from the response | 响应中的实际颁发者
     * @throws OAuth2Exception with ISSUER_MISMATCH if issuers don't match or actualIssuer is null
     *                         | 如果颁发者不匹配或 actualIssuer 为 null 则抛出 ISSUER_MISMATCH
     */
    public static void validate(String expectedIssuer, String actualIssuer) {
        if (expectedIssuer == null) {
            // Issuer validation not configured, skip
            return;
        }

        if (actualIssuer == null) {
            throw new OAuth2Exception(OAuth2ErrorCode.ISSUER_MISMATCH,
                    "Issuer is missing in the response");
        }

        if (!constantTimeEquals(expectedIssuer, actualIssuer)) {
            throw new OAuth2Exception(OAuth2ErrorCode.ISSUER_MISMATCH,
                    "Issuer validation failed");
        }
    }

    /**
     * Check if the actual issuer matches the expected issuer.
     * 检查实际颁发者是否匹配预期颁发者。
     *
     * <p>Uses constant-time comparison to prevent timing attacks.</p>
     * <p>使用恒定时间比较以防止时序攻击。</p>
     *
     * @param expectedIssuer the expected issuer | 预期颁发者
     * @param actualIssuer   the actual issuer | 实际颁发者
     * @return true if both are non-null and equal | 如果两者都非 null 且相等返回 true
     */
    public static boolean matches(String expectedIssuer, String actualIssuer) {
        if (expectedIssuer == null || actualIssuer == null) {
            return false;
        }
        return constantTimeEquals(expectedIssuer, actualIssuer);
    }

    /**
     * Perform constant-time string comparison using MessageDigest.isEqual.
     * 使用 MessageDigest.isEqual 执行恒定时间字符串比较。
     *
     * @param a first string | 第一个字符串
     * @param b second string | 第二个字符串
     * @return true if equal | 相等返回 true
     */
    private static boolean constantTimeEquals(String a, String b) {
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }
}
