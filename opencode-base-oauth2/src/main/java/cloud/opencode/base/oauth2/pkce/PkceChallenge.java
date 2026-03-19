package cloud.opencode.base.oauth2.pkce;

import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PKCE (Proof Key for Code Exchange) Challenge
 * PKCE 挑战
 *
 * <p>Implements RFC 7636 - Proof Key for Code Exchange by OAuth Public Clients.</p>
 * <p>实现 RFC 7636 - OAuth 公共客户端的代码交换证明密钥。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Secure verifier generation - 安全验证器生成</li>
 *   <li>S256 challenge method - S256 挑战方法</li>
 *   <li>Base64 URL-safe encoding - Base64 URL 安全编码</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Generate PKCE challenge
 * PkceChallenge pkce = PkceChallenge.generate();
 *
 * // Use in authorization request
 * String authUrl = authEndpoint
 *     + "?code_challenge=" + pkce.challenge()
 *     + "&code_challenge_method=" + pkce.method();
 *
 * // Use verifier in token exchange
 * tokenRequest.put("code_verifier", pkce.verifier());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Uses SecureRandom for cryptographic randomness - 使用 SecureRandom 生成加密随机数</li>
 *   <li>SHA-256 for challenge generation - 使用 SHA-256 生成挑战</li>
 *   <li>43 character verifier (recommended minimum) - 43 字符验证器（推荐最小值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://tools.ietf.org/html/rfc7636">RFC 7636</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public record PkceChallenge(String verifier, String challenge, String method) {

    /**
     * PKCE S256 method name
     * PKCE S256 方法名称
     */
    public static final String METHOD_S256 = "S256";

    /**
     * PKCE plain method name (not recommended)
     * PKCE plain 方法名称（不推荐）
     */
    public static final String METHOD_PLAIN = "plain";

    /**
     * Default verifier byte length (generates 43 character verifier)
     * 默认验证器字节长度（生成 43 字符验证器）
     */
    private static final int DEFAULT_VERIFIER_BYTES = 32;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generate a new PKCE challenge with S256 method
     * 使用 S256 方法生成新的 PKCE 挑战
     *
     * @return the PKCE challenge | PKCE 挑战
     * @throws OAuth2Exception if SHA-256 is not available | 如果 SHA-256 不可用
     */
    public static PkceChallenge generate() {
        return generate(DEFAULT_VERIFIER_BYTES);
    }

    /**
     * Generate a new PKCE challenge with custom verifier length
     * 使用自定义验证器长度生成新的 PKCE 挑战
     *
     * @param verifierBytes the number of random bytes (32-96 recommended) | 随机字节数（推荐 32-96）
     * @return the PKCE challenge | PKCE 挑战
     * @throws OAuth2Exception if SHA-256 is not available | 如果 SHA-256 不可用
     * @throws IllegalArgumentException if verifierBytes is less than 32 | 如果 verifierBytes 小于 32
     */
    public static PkceChallenge generate(int verifierBytes) {
        if (verifierBytes < 32) {
            throw new IllegalArgumentException("verifierBytes must be at least 32 for security");
        }

        // Generate random verifier (43-128 characters after base64 encoding)
        byte[] bytes = new byte[verifierBytes];
        SECURE_RANDOM.nextBytes(bytes);
        String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // Calculate SHA-256 challenge
        String challenge = calculateS256Challenge(verifier);

        return new PkceChallenge(verifier, challenge, METHOD_S256);
    }

    /**
     * Create a plain PKCE challenge (not recommended, use S256 instead)
     * 创建 plain PKCE 挑战（不推荐，请使用 S256）
     *
     * @param verifier the verifier | 验证器
     * @return the PKCE challenge | PKCE 挑战
     */
    public static PkceChallenge plain(String verifier) {
        return new PkceChallenge(verifier, verifier, METHOD_PLAIN);
    }

    /**
     * Calculate S256 challenge from verifier
     * 从验证器计算 S256 挑战
     *
     * @param verifier the verifier | 验证器
     * @return the challenge | 挑战
     * @throws OAuth2Exception if SHA-256 is not available | 如果 SHA-256 不可用
     */
    public static String calculateS256Challenge(String verifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new OAuth2Exception(OAuth2ErrorCode.PKCE_ERROR, "SHA-256 not available", e);
        }
    }

    /**
     * Verify that the verifier matches the challenge using constant-time comparison
     * 使用恒定时间比较验证验证器是否匹配挑战
     *
     * @param verifier  the verifier to check | 要检查的验证器
     * @param challenge the expected challenge | 预期的挑战
     * @param method    the challenge method | 挑战方法
     * @return true if valid | 有效返回 true
     */
    public static boolean verify(String verifier, String challenge, String method) {
        if (verifier == null || challenge == null || method == null) {
            return false;
        }

        String calculatedChallenge = switch (method) {
            case METHOD_S256 -> calculateS256Challenge(verifier);
            case METHOD_PLAIN -> verifier;
            default -> null;
        };

        if (calculatedChallenge == null) {
            return false;
        }
        // Use constant-time comparison to prevent timing attacks
        // 使用恒定时间比较以防止时序攻击
        return MessageDigest.isEqual(
                challenge.getBytes(StandardCharsets.UTF_8),
                calculatedChallenge.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Check if this challenge uses S256 method
     * 检查此挑战是否使用 S256 方法
     *
     * @return true if S256 | 如果是 S256 返回 true
     */
    public boolean isS256() {
        return METHOD_S256.equals(method);
    }

    /**
     * Check if this challenge uses plain method
     * 检查此挑战是否使用 plain 方法
     *
     * @return true if plain | 如果是 plain 返回 true
     */
    public boolean isPlain() {
        return METHOD_PLAIN.equals(method);
    }
}
