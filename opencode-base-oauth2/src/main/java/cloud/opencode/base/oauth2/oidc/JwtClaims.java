package cloud.opencode.base.oauth2.oidc;

import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * JWT Claims
 * JWT 声明
 *
 * <p>Represents decoded JWT claims from an ID token or access token.</p>
 * <p>表示从 ID 令牌或访问令牌解码的 JWT 声明。</p>
 *
 * <p><strong>Note | 注意:</strong></p>
 * <p>This parser does NOT verify the JWT signature. For security-critical applications,
 * use a proper JWT library with signature verification.</p>
 * <p>此解析器不验证 JWT 签名。对于安全关键的应用程序，请使用具有签名验证的正式 JWT 库。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Parse JWT without verification
 * JwtClaims claims = JwtClaims.parse(idToken);
 *
 * String subject = claims.sub();
 * String issuer = claims.iss();
 * Instant expiry = claims.exp();
 *
 * // Check if expired
 * if (claims.isExpired()) {
 *     // Token is expired
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JWT claims representation for OIDC tokens - OIDC令牌的JWT声明表示</li>
 *   <li>Standard claim accessors (sub, iss, exp, iat) - 标准声明访问器（sub、iss、exp、iat）</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://tools.ietf.org/html/rfc7519">RFC 7519 - JSON Web Token</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public record JwtClaims(
        String iss,
        String sub,
        List<String> aud,
        Instant exp,
        Instant nbf,
        Instant iat,
        String jti,
        String nonce,
        String azp,
        Map<String, Object> claims
) {

    /**
     * Compact constructor
     * 紧凑构造器
     */
    public JwtClaims {
        aud = aud != null ? List.copyOf(aud) : List.of();
        claims = claims != null ? Map.copyOf(claims) : Map.of();
    }

    /**
     * Check if the token is expired
     * 检查令牌是否已过期
     *
     * @return true if expired | 已过期返回 true
     */
    public boolean isExpired() {
        return exp != null && Instant.now().isAfter(exp);
    }

    /**
     * Check if the token is not yet valid
     * 检查令牌是否尚未生效
     *
     * @return true if not yet valid | 尚未生效返回 true
     */
    public boolean isNotYetValid() {
        return nbf != null && Instant.now().isBefore(nbf);
    }

    /**
     * Check if the token is currently valid (not expired and not before nbf)
     * 检查令牌当前是否有效（未过期且不在 nbf 之前）
     *
     * @return true if valid | 有效返回 true
     */
    public boolean isValid() {
        return !isExpired() && !isNotYetValid();
    }

    /**
     * Get a custom claim value
     * 获取自定义声明值
     *
     * @param name the claim name | 声明名称
     * @return the claim value | 声明值
     */
    public Optional<Object> getClaim(String name) {
        return Optional.ofNullable(claims.get(name));
    }

    /**
     * Get a custom claim value as string
     * 获取自定义声明值作为字符串
     *
     * @param name the claim name | 声明名称
     * @return the claim value as string | 声明值作为字符串
     */
    public Optional<String> getClaimAsString(String name) {
        return getClaim(name).map(Object::toString);
    }

    /**
     * Get the first audience
     * 获取第一个受众
     *
     * @return the first audience or null | 第一个受众或 null
     */
    public String audience() {
        return aud.isEmpty() ? null : aud.get(0);
    }

    /**
     * Check if the token has the specified audience
     * 检查令牌是否具有指定的受众
     *
     * @param audience the audience to check | 要检查的受众
     * @return true if has audience | 有受众返回 true
     */
    public boolean hasAudience(String audience) {
        return aud.contains(audience);
    }

    /**
     * Parse a JWT token (without signature verification)
     * 解析 JWT 令牌（不验证签名）
     *
     * @param token the JWT token | JWT 令牌
     * @return the claims | 声明
     * @throws OAuth2Exception if parsing fails | 如果解析失败
     */
    public static JwtClaims parse(String token) {
        if (token == null || token.isBlank()) {
            throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_PARSE_ERROR, "Token is empty");
        }

        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_PARSE_ERROR, "Invalid JWT format");
        }

        try {
            // Decode payload (second part)
            String payload = new String(
                    Base64.getUrlDecoder().decode(padBase64(parts[1])),
                    StandardCharsets.UTF_8
            );

            return parsePayload(payload);
        } catch (IllegalArgumentException e) {
            throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_PARSE_ERROR, "Invalid base64 encoding", e);
        }
    }

    /**
     * Pad base64 string if necessary
     * 如有必要，填充 base64 字符串
     */
    private static String padBase64(String base64) {
        int padding = 4 - (base64.length() % 4);
        if (padding != 4) {
            base64 = base64 + "=".repeat(padding);
        }
        return base64;
    }

    /**
     * Parse JWT payload JSON
     * 解析 JWT 有效载荷 JSON
     */
    private static JwtClaims parsePayload(String json) {
        Map<String, Object> claims = new HashMap<>();
        parseJsonToClaims(json, claims);

        String iss = getStringClaim(claims, "iss");
        String sub = getStringClaim(claims, "sub");
        String jti = getStringClaim(claims, "jti");
        String nonce = getStringClaim(claims, "nonce");
        String azp = getStringClaim(claims, "azp");

        List<String> aud = parseAudience(claims.get("aud"));

        Instant exp = getInstantClaim(claims, "exp");
        Instant nbf = getInstantClaim(claims, "nbf");
        Instant iat = getInstantClaim(claims, "iat");

        return new JwtClaims(iss, sub, aud, exp, nbf, iat, jti, nonce, azp, claims);
    }

    private static List<String> parseAudience(Object aud) {
        if (aud == null) {
            return List.of();
        }
        if (aud instanceof String s) {
            return List.of(s);
        }
        if (aud instanceof List<?> list) {
            return list.stream()
                    .map(Object::toString)
                    .toList();
        }
        return List.of(aud.toString());
    }

    private static void parseJsonToClaims(String json, Map<String, Object> claims) {
        // Reuse the parser from UserInfo
        if (json == null || json.isBlank()) return;

        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return;

        json = json.substring(1, json.length() - 1);

        int pos = 0;
        while (pos < json.length()) {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
            if (pos >= json.length()) break;

            if (json.charAt(pos) != '"') {
                pos++;
                continue;
            }
            int keyStart = pos + 1;
            int keyEnd = json.indexOf('"', keyStart);
            if (keyEnd < 0) break;
            String key = json.substring(keyStart, keyEnd);
            pos = keyEnd + 1;

            while (pos < json.length() && json.charAt(pos) != ':') pos++;
            if (pos >= json.length()) break;
            pos++;

            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
            if (pos >= json.length()) break;

            char ch = json.charAt(pos);
            if (ch == '"') {
                int valueStart = pos + 1;
                int valueEnd = json.indexOf('"', valueStart);
                if (valueEnd < 0) break;
                claims.put(key, json.substring(valueStart, valueEnd));
                pos = valueEnd + 1;
            } else if (ch == '[') {
                // Array - simple parsing for string arrays
                int arrayEnd = json.indexOf(']', pos);
                if (arrayEnd < 0) break;
                String arrayContent = json.substring(pos + 1, arrayEnd);
                List<String> items = new ArrayList<>();
                for (String item : arrayContent.split(",")) {
                    item = item.trim();
                    if (item.startsWith("\"") && item.endsWith("\"")) {
                        items.add(item.substring(1, item.length() - 1));
                    }
                }
                claims.put(key, items);
                pos = arrayEnd + 1;
            } else if (ch == 't' || ch == 'f') {
                if (json.substring(pos).startsWith("true")) {
                    claims.put(key, true);
                    pos += 4;
                } else if (json.substring(pos).startsWith("false")) {
                    claims.put(key, false);
                    pos += 5;
                }
            } else if (ch == 'n') {
                if (json.substring(pos).startsWith("null")) {
                    pos += 4;
                }
            } else if (Character.isDigit(ch) || ch == '-') {
                int valueStart = pos;
                while (pos < json.length() && (Character.isDigit(json.charAt(pos)) ||
                        json.charAt(pos) == '.' || json.charAt(pos) == '-')) {
                    pos++;
                }
                String numStr = json.substring(valueStart, pos);
                try {
                    if (numStr.contains(".")) {
                        claims.put(key, Double.parseDouble(numStr));
                    } else {
                        claims.put(key, Long.parseLong(numStr));
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }

            while (pos < json.length() && json.charAt(pos) != ',') pos++;
            if (pos < json.length()) pos++;
        }
    }

    private static String getStringClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value != null ? value.toString() : null;
    }

    private static Instant getInstantClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        if (value instanceof Number n) {
            return Instant.ofEpochSecond(n.longValue());
        }
        if (value instanceof String s) {
            try {
                return Instant.ofEpochSecond(Long.parseLong(s));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
