/*
 * Copyright 2025 OpenCode Cloud Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.crypto.jwt;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenSignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * JWT Utility - Creates and verifies JSON Web Tokens
 * JWT 工具类 - 创建和验证 JSON Web Token
 *
 * <p>Provides comprehensive JWT support with multiple algorithms including
 * HMAC (HS256/384/512), RSA (RS256/384/512), and ECDSA (ES256/384/512).</p>
 * <p>提供全面的 JWT 支持，包括多种算法：HMAC、RSA 和 ECDSA。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HMAC symmetric signing - HMAC 对称签名</li>
 *   <li>RSA asymmetric signing - RSA 非对称签名</li>
 *   <li>ECDSA asymmetric signing - ECDSA 非对称签名</li>
 *   <li>Claim validation - 声明验证</li>
 *   <li>Expiration checking - 过期检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create JWT with HMAC
 * String token = JwtUtil.builder()
 *     .algorithm(JwtAlgorithm.HS256)
 *     .secret("your-256-bit-secret")
 *     .issuer("auth-service")
 *     .subject("user123")
 *     .expiresIn(Duration.ofHours(1))
 *     .claim("role", "admin")
 *     .sign();
 *
 * // Verify and parse JWT
 * JwtClaims claims = JwtUtil.verify(token, "your-256-bit-secret");
 *
 * // Create JWT with RSA
 * KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
 * String rsaToken = JwtUtil.builder()
 *     .algorithm(JwtAlgorithm.RS256)
 *     .privateKey(keyPair.getPrivate())
 *     .issuer("auth-service")
 *     .subject("user123")
 *     .sign();
 *
 * // Verify RSA JWT
 * JwtClaims rsaClaims = JwtUtil.verify(rsaToken, keyPair.getPublic());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - 时间复杂度: O(n)，n为token长度</li>
 *   <li>Space complexity: O(n) - 空间复杂度: O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.2.0
 */
public final class JwtUtil {

    private static final String COMPONENT = "JWT";
    private static final int MAX_DEPTH = 32;
    private static final int MAX_STRING_LENGTH = 65536;
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private JwtUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // ==================== Builder ====================

    /**
     * Creates a new JWT builder.
     * 创建新的 JWT 构建器。
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Verification ====================

    /**
     * Verifies a JWT with HMAC secret and returns claims.
     * 使用 HMAC 密钥验证 JWT 并返回声明。
     *
     * @param token  the JWT string
     * @param secret the HMAC secret
     * @return the verified claims
     * @throws OpenSignatureException if verification fails
     */
    public static JwtClaims verify(String token, String secret) {
        return verify(token, secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Verifies a JWT with HMAC secret bytes and returns claims.
     * 使用 HMAC 密钥字节验证 JWT 并返回声明。
     *
     * @param token  the JWT string
     * @param secret the HMAC secret bytes
     * @return the verified claims
     * @throws OpenSignatureException if verification fails
     */
    public static JwtClaims verify(String token, byte[] secret) {
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(secret, "secret must not be null");

        JwtParts parts = parse(token);
        JwtAlgorithm algorithm = parts.algorithm();

        if (!algorithm.isSymmetric()) {
            throw new OpenSignatureException(COMPONENT,
                    "Algorithm " + algorithm.algorithmName() + " requires public key, not secret");
        }

        byte[] expectedSignature = computeHmac(algorithm, secret, parts.signatureInput());

        if (!MessageDigest.isEqual(expectedSignature, parts.signature())) {
            throw new OpenSignatureException(COMPONENT, "Invalid signature");
        }

        validateClaims(parts.claims());
        return parts.claims();
    }

    /**
     * Verifies a JWT with public key and returns claims.
     * 使用公钥验证 JWT 并返回声明。
     *
     * @param token     the JWT string
     * @param publicKey the public key
     * @return the verified claims
     * @throws OpenSignatureException if verification fails
     */
    public static JwtClaims verify(String token, PublicKey publicKey) {
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(publicKey, "publicKey must not be null");

        JwtParts parts = parse(token);
        JwtAlgorithm algorithm = parts.algorithm();

        if (!algorithm.isAsymmetric()) {
            throw new OpenSignatureException(COMPONENT,
                    "Algorithm " + algorithm.algorithmName() + " requires secret, not public key");
        }

        try {
            Signature sig = Signature.getInstance(algorithm.jcaName());
            sig.initVerify(publicKey);
            sig.update(parts.signatureInput().getBytes(StandardCharsets.UTF_8));

            if (!sig.verify(parts.signature())) {
                throw new OpenSignatureException(COMPONENT, "Invalid signature");
            }

            validateClaims(parts.claims());
            return parts.claims();

        } catch (OpenSignatureException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenSignatureException(COMPONENT,
                    "Signature verification failed", e);
        }
    }

    /**
     * Parses a JWT without verification (unsafe).
     * 解析 JWT 但不验证（不安全）。
     *
     * @param token the JWT string
     * @return the claims (unverified)
     */
    public static JwtClaims parseUnsafe(String token) {
        return parse(token).claims();
    }

    /**
     * Parses JWT parts without verification.
     * 解析 JWT 部分但不验证。
     *
     * @param token the JWT string
     * @return the JWT parts
     */
    public static JwtParts parse(String token) {
        Objects.requireNonNull(token, "token must not be null");

        String[] segments = token.split("\\.");
        if (segments.length != 3) {
            throw new OpenCryptoException(COMPONENT, "parsing",
                    "Invalid JWT format: expected 3 segments, got " + segments.length);
        }

        try {
            String headerJson = new String(URL_DECODER.decode(segments[0]), StandardCharsets.UTF_8);
            String payloadJson = new String(URL_DECODER.decode(segments[1]), StandardCharsets.UTF_8);
            byte[] signature = URL_DECODER.decode(segments[2]);

            Map<String, Object> header = parseJson(headerJson);
            Map<String, Object> payload = parseJson(payloadJson);

            String alg = (String) header.get("alg");
            if (alg == null) {
                throw new OpenCryptoException(COMPONENT, "parsing", "Missing 'alg' in header");
            }

            JwtAlgorithm algorithm = JwtAlgorithm.fromName(alg);
            String signatureInput = segments[0] + "." + segments[1];

            return new JwtParts(algorithm, header, JwtClaims.of(payload), signature, signatureInput);

        } catch (OpenCryptoException | OpenSignatureException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenCryptoException(COMPONENT, "parsing", "Failed to parse JWT", e);
        }
    }

    // ==================== Quick Methods ====================

    /**
     * Creates a quick JWT with HMAC-SHA256.
     * 使用 HMAC-SHA256 快速创建 JWT。
     *
     * @param subject    the subject
     * @param secret     the secret
     * @param expiration the expiration duration
     * @return the JWT string
     */
    public static String quickSign(String subject, String secret, Duration expiration) {
        return builder()
                .algorithm(JwtAlgorithm.HS256)
                .secret(secret)
                .subject(subject)
                .issuedAtNow()
                .expiresIn(expiration)
                .sign();
    }

    /**
     * Creates a quick JWT with claims.
     * 使用声明快速创建 JWT。
     *
     * @param claims the claims
     * @param secret the secret
     * @return the JWT string
     */
    public static String quickSign(JwtClaims claims, String secret) {
        return builder()
                .algorithm(JwtAlgorithm.HS256)
                .secret(secret)
                .claims(claims)
                .sign();
    }

    // ==================== Private Helpers ====================

    private static byte[] computeHmac(JwtAlgorithm algorithm, byte[] secret, String data) {
        try {
            Mac mac = Mac.getInstance(algorithm.jcaName());
            mac.init(new SecretKeySpec(secret, algorithm.jcaName()));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new OpenCryptoException(COMPONENT, "signing",
                    "Failed to compute HMAC", e);
        }
    }

    private static void validateClaims(JwtClaims claims) {
        if (claims.isExpired()) {
            throw new OpenSignatureException(COMPONENT,
                    "Token has expired at " + claims.expiration());
        }
        if (claims.isNotYetValid()) {
            throw new OpenSignatureException(COMPONENT,
                    "Token is not yet valid until " + claims.notBefore());
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseJson(String json) {
        return parseJson(json, 0);
    }

    private static Map<String, Object> parseJson(String json, int currentDepth) {
        if (currentDepth > MAX_DEPTH) {
            throw new OpenCryptoException(COMPONENT, "parsing",
                    "JSON nesting depth exceeds maximum of " + MAX_DEPTH);
        }

        // Simple JSON parser for JWT (handles basic types)
        Map<String, Object> map = new LinkedHashMap<>();
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON object");
        }

        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) {
            return map;
        }

        int depth = 0;
        int start = 0;
        boolean inString = false;
        char prev = 0;

        List<String> pairs = new ArrayList<>();

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && prev != '\\') {
                inString = !inString;
            } else if (!inString) {
                if (c == '{' || c == '[') depth++;
                else if (c == '}' || c == ']') depth--;
                else if (c == ',' && depth == 0) {
                    pairs.add(json.substring(start, i).trim());
                    start = i + 1;
                }
            }
            prev = c;
        }
        pairs.add(json.substring(start).trim());

        for (String pair : pairs) {
            // Find the colon separator outside of strings, not just the first colon
            // (handles keys or values containing colons, e.g., "url":"https://...")
            int colonIdx = findKeyValueSeparator(pair);
            if (colonIdx < 0) continue;

            String key = pair.substring(0, colonIdx).trim();
            String value = pair.substring(colonIdx + 1).trim();

            // Remove quotes from key
            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
            }

            map.put(key, parseJsonValue(value, currentDepth));
        }

        return map;
    }

    /**
     * Finds the colon that separates a JSON key from its value, skipping colons inside strings.
     * For example, in {@code "url":"https://x.com"}, returns the index of the colon between
     * the closing quote of the key and the opening quote of the value.
     *
     * @param pair a JSON key:value pair string
     * @return the index of the separator colon, or -1 if not found
     */
    private static int findKeyValueSeparator(String pair) {
        boolean inString = false;
        char prev = 0;
        for (int i = 0; i < pair.length(); i++) {
            char c = pair.charAt(i);
            if (c == '"' && prev != '\\') {
                inString = !inString;
            } else if (c == ':' && !inString) {
                return i;
            }
            prev = c;
        }
        return -1;
    }

    private static Object parseJsonValue(String value, int currentDepth) {
        value = value.trim();

        if (value.equals("null")) return null;
        if (value.equals("true")) return true;
        if (value.equals("false")) return false;

        if (value.startsWith("\"") && value.endsWith("\"")) {
            String str = value.substring(1, value.length() - 1);
            if (str.length() > MAX_STRING_LENGTH) {
                throw new OpenCryptoException(COMPONENT, "parsing",
                        "JSON string length exceeds maximum of " + MAX_STRING_LENGTH);
            }
            return unescapeJson(str);
        }

        if (value.startsWith("[") && value.endsWith("]")) {
            return parseJsonArray(value, currentDepth + 1);
        }

        if (value.startsWith("{") && value.endsWith("}")) {
            return parseJson(value, currentDepth + 1);
        }

        // Number
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            }
            long l = Long.parseLong(value);
            if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                return (int) l;
            }
            return l;
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private static List<Object> parseJsonArray(String json, int currentDepth) {
        if (currentDepth > MAX_DEPTH) {
            throw new OpenCryptoException(COMPONENT, "parsing",
                    "JSON nesting depth exceeds maximum of " + MAX_DEPTH);
        }

        List<Object> list = new ArrayList<>();
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return list;

        int depth = 0;
        int start = 0;
        boolean inString = false;
        char prev = 0;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && prev != '\\') {
                inString = !inString;
            } else if (!inString) {
                if (c == '{' || c == '[') depth++;
                else if (c == '}' || c == ']') depth--;
                else if (c == ',' && depth == 0) {
                    list.add(parseJsonValue(json.substring(start, i).trim(), currentDepth));
                    start = i + 1;
                }
            }
            prev = c;
        }
        list.add(parseJsonValue(json.substring(start).trim(), currentDepth));

        return list;
    }

    private static String unescapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (i + 4 < s.length()) {
                            String hex = s.substring(i + 1, i + 5);
                            sb.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        }
                    }
                    default -> sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ==================== JWT Parts Record ====================

    /**
     * JWT Parts - Holds parsed JWT components
     * JWT 部分 - 保存解析后的 JWT 组件
     */
    public record JwtParts(
            JwtAlgorithm algorithm,
            Map<String, Object> header,
            JwtClaims claims,
            byte[] signature,
            String signatureInput
    ) {
    }

    // ==================== Builder ====================

    /**
     * JWT Builder - Fluent builder for creating JWTs
     * JWT 构建器 - 用于创建 JWT 的流式构建器
     */
    public static final class Builder {
        private JwtAlgorithm algorithm = JwtAlgorithm.HS256;
        private byte[] secret;
        private PrivateKey privateKey;
        private final Map<String, Object> header = new LinkedHashMap<>();
        private final JwtClaims.Builder claimsBuilder = JwtClaims.builder();

        private Builder() {
            header.put("typ", "JWT");
        }

        /**
         * Sets the signing algorithm.
         * 设置签名算法。
         *
         * @param algorithm the algorithm
         * @return this builder
         */
        public Builder algorithm(JwtAlgorithm algorithm) {
            this.algorithm = Objects.requireNonNull(algorithm);
            return this;
        }

        /**
         * Sets the HMAC secret.
         * 设置 HMAC 密钥。
         *
         * @param secret the secret string
         * @return this builder
         */
        public Builder secret(String secret) {
            return secret(secret.getBytes(StandardCharsets.UTF_8));
        }

        /**
         * Sets the HMAC secret bytes.
         * 设置 HMAC 密钥字节。
         *
         * @param secret the secret bytes
         * @return this builder
         */
        public Builder secret(byte[] secret) {
            this.secret = Objects.requireNonNull(secret);
            return this;
        }

        /**
         * Sets the private key for asymmetric signing.
         * 设置用于非对称签名的私钥。
         *
         * @param privateKey the private key
         * @return this builder
         */
        public Builder privateKey(PrivateKey privateKey) {
            this.privateKey = Objects.requireNonNull(privateKey);
            return this;
        }

        /**
         * Adds a header claim.
         * 添加头部声明。
         *
         * @param name  the claim name
         * @param value the claim value
         * @return this builder
         */
        public Builder header(String name, Object value) {
            header.put(name, value);
            return this;
        }

        /**
         * Sets the issuer claim.
         * 设置签发者声明。
         *
         * @param issuer the issuer
         * @return this builder
         */
        public Builder issuer(String issuer) {
            claimsBuilder.issuer(issuer);
            return this;
        }

        /**
         * Sets the subject claim.
         * 设置主题声明。
         *
         * @param subject the subject
         * @return this builder
         */
        public Builder subject(String subject) {
            claimsBuilder.subject(subject);
            return this;
        }

        /**
         * Sets the audience claim.
         * 设置受众声明。
         *
         * @param audience the audience
         * @return this builder
         */
        public Builder audience(String audience) {
            claimsBuilder.audience(audience);
            return this;
        }

        /**
         * Sets the audience claim with multiple values.
         * 设置多值受众声明。
         *
         * @param audiences the audiences
         * @return this builder
         */
        public Builder audience(List<String> audiences) {
            claimsBuilder.audience(audiences);
            return this;
        }

        /**
         * Sets the expiration time.
         * 设置过期时间。
         *
         * @param expiration the expiration instant
         * @return this builder
         */
        public Builder expiration(Instant expiration) {
            claimsBuilder.expiration(expiration);
            return this;
        }

        /**
         * Sets the expiration time relative to now.
         * 设置相对于当前时间的过期时间。
         *
         * @param duration the duration from now
         * @return this builder
         */
        public Builder expiresIn(Duration duration) {
            claimsBuilder.expiresIn(duration);
            return this;
        }

        /**
         * Sets the not-before time.
         * 设置生效时间。
         *
         * @param notBefore the not-before instant
         * @return this builder
         */
        public Builder notBefore(Instant notBefore) {
            claimsBuilder.notBefore(notBefore);
            return this;
        }

        /**
         * Sets the issued-at time to now.
         * 设置签发时间为当前时间。
         *
         * @return this builder
         */
        public Builder issuedAtNow() {
            claimsBuilder.issuedAtNow();
            return this;
        }

        /**
         * Sets the issued-at time.
         * 设置签发时间。
         *
         * @param issuedAt the issued-at instant
         * @return this builder
         */
        public Builder issuedAt(Instant issuedAt) {
            claimsBuilder.issuedAt(issuedAt);
            return this;
        }

        /**
         * Generates a random JWT ID.
         * 生成随机 JWT ID。
         *
         * @return this builder
         */
        public Builder generateJwtId() {
            claimsBuilder.generateJwtId();
            return this;
        }

        /**
         * Sets a custom claim.
         * 设置自定义声明。
         *
         * @param name  the claim name
         * @param value the claim value
         * @return this builder
         */
        public Builder claim(String name, Object value) {
            claimsBuilder.claim(name, value);
            return this;
        }

        /**
         * Sets multiple claims.
         * 设置多个声明。
         *
         * @param claims the claims
         * @return this builder
         */
        public Builder claims(JwtClaims claims) {
            claimsBuilder.claims(claims.asMap());
            return this;
        }

        /**
         * Sets multiple claims from a map.
         * 从映射设置多个声明。
         *
         * @param claims the claims map
         * @return this builder
         */
        public Builder claims(Map<String, Object> claims) {
            claimsBuilder.claims(claims);
            return this;
        }

        /**
         * Signs and returns the JWT string.
         * 签名并返回 JWT 字符串。
         *
         * @return the JWT string
         * @throws OpenCryptoException if signing fails
         */
        public String sign() {
            header.put("alg", algorithm.algorithmName());

            String headerJson = toJson(header);
            String payloadJson = toJson(claimsBuilder.build().asMap());

            String headerBase64 = URL_ENCODER.encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadBase64 = URL_ENCODER.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

            String signatureInput = headerBase64 + "." + payloadBase64;
            byte[] signature;

            if (algorithm.isSymmetric()) {
                if (secret == null) {
                    throw new IllegalStateException("Secret must be set for symmetric algorithm");
                }
                signature = computeHmac(algorithm, secret, signatureInput);
            } else {
                if (privateKey == null) {
                    throw new IllegalStateException("Private key must be set for asymmetric algorithm");
                }
                signature = signWithPrivateKey(algorithm, privateKey, signatureInput);
            }

            String signatureBase64 = URL_ENCODER.encodeToString(signature);
            return signatureInput + "." + signatureBase64;
        }

        private byte[] signWithPrivateKey(JwtAlgorithm algorithm, PrivateKey privateKey, String data) {
            try {
                Signature sig = Signature.getInstance(algorithm.jcaName());
                sig.initSign(privateKey);
                sig.update(data.getBytes(StandardCharsets.UTF_8));
                return sig.sign();
            } catch (Exception e) {
                throw new OpenCryptoException(COMPONENT, "signing",
                        "Failed to sign with private key", e);
            }
        }

        private String toJson(Map<String, Object> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
                sb.append(valueToJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }

        private String valueToJson(Object value) {
            if (value == null) return "null";
            if (value instanceof Boolean || value instanceof Number) return value.toString();
            if (value instanceof String s) return "\"" + escapeJson(s) + "\"";
            if (value instanceof List<?> list) {
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                for (Object item : list) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append(valueToJson(item));
                }
                sb.append("]");
                return sb.toString();
            }
            if (value instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) map;
                return toJson(m);
            }
            return "\"" + escapeJson(value.toString()) + "\"";
        }

        private String escapeJson(String s) {
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '"' -> sb.append("\\\"");
                    case '\\' -> sb.append("\\\\");
                    case '\b' -> sb.append("\\b");
                    case '\f' -> sb.append("\\f");
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    default -> {
                        if (c < ' ') {
                            sb.append(String.format("\\u%04x", (int) c));
                        } else {
                            sb.append(c);
                        }
                    }
                }
            }
            return sb.toString();
        }
    }
}
