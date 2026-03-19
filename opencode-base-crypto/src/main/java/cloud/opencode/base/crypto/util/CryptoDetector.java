/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.crypto.util;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * CryptoDetector - Cryptographic Data Detection Utility
 * CryptoDetector - 加密数据检测工具
 *
 * <p>Provides heuristic methods for detecting cryptographic patterns in data,
 * including encrypted content, encoded data, keys, and cryptographic artifacts.</p>
 * <p>提供用于检测数据中加密模式的启发式方法，包括加密内容、编码数据、密钥和加密相关元素。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if data appears encrypted
 * boolean encrypted = CryptoDetector.looksEncrypted(data);
 *
 * // Detect encoding type
 * Optional<EncodingType> encoding = CryptoDetector.detectEncoding(text);
 *
 * // Check for high entropy (random-looking data)
 * double entropy = CryptoDetector.calculateEntropy(data);
 * boolean highEntropy = entropy > 7.5; // Close to maximum of 8
 *
 * // Detect key format
 * Optional<KeyFormat> keyFormat = CryptoDetector.detectKeyFormat(pemString);
 *
 * // Detect hash format
 * Optional<HashFormat> hashFormat = CryptoDetector.detectHashFormat(hashString);
 *
 * // Analyze cryptographic properties
 * CryptoAnalysis analysis = CryptoDetector.analyze(data);
 * System.out.println("Entropy: " + analysis.entropy());
 * System.out.println("Looks encrypted: " + analysis.looksEncrypted());
 * }</pre>
 *
 * <p><strong>Important Notes | 重要说明:</strong></p>
 * <ul>
 *   <li>Detection is heuristic-based and not 100% accurate - 检测基于启发式方法，不是 100% 准确</li>
 *   <li>High entropy alone doesn't prove encryption - 高熵本身并不能证明是加密的</li>
 *   <li>Compressed data also has high entropy - 压缩数据也具有高熵</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Entropy calculation and analysis - 熵计算和分析</li>
 *   <li>Encryption, encoding, key, and hash format detection - 加密、编码、密钥和哈希格式检测</li>
 *   <li>Comprehensive cryptographic data analysis - 综合加密数据分析</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class CryptoDetector {

    private CryptoDetector() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // ==================== Entropy Calculation | 熵计算 ====================

    /**
     * Calculate Shannon entropy of byte data.
     * 计算字节数据的香农熵。
     *
     * <p>Entropy ranges from 0 (completely uniform) to 8 (maximum randomness).
     * Encrypted data typically has entropy > 7.5.</p>
     * <p>熵范围从 0（完全均匀）到 8（最大随机性）。加密数据通常熵 > 7.5。</p>
     *
     * @param data the data to analyze | 要分析的数据
     * @return entropy value (0-8 for byte data) | 熵值（字节数据为 0-8）
     * @throws IllegalArgumentException if data is null or empty | 如果数据为 null 或空
     */
    public static double calculateEntropy(byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data must not be null or empty");
        }

        // Count byte frequencies
        int[] frequencies = new int[256];
        for (byte b : data) {
            frequencies[b & 0xFF]++;
        }

        // Calculate entropy
        double entropy = 0.0;
        double length = data.length;

        for (int freq : frequencies) {
            if (freq > 0) {
                double probability = freq / length;
                entropy -= probability * (Math.log(probability) / Math.log(2));
            }
        }

        return entropy;
    }

    /**
     * Calculate Shannon entropy of string data.
     * 计算字符串数据的香农熵。
     *
     * @param text the text to analyze | 要分析的文本
     * @return entropy value | 熵值
     * @throws IllegalArgumentException if text is null or empty | 如果文本为 null 或空
     */
    public static double calculateEntropy(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Text must not be null or empty");
        }
        return calculateEntropy(text.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== Encryption Detection | 加密检测 ====================

    /**
     * Heuristically determine if data appears to be encrypted.
     * 启发式判断数据是否看起来被加密。
     *
     * <p>Uses multiple heuristics including entropy analysis, byte distribution,
     * and pattern detection.</p>
     * <p>使用多种启发式方法，包括熵分析、字节分布和模式检测。</p>
     *
     * @param data the data to analyze | 要分析的数据
     * @return true if data appears encrypted | 如果数据看起来被加密则返回 true
     */
    public static boolean looksEncrypted(byte[] data) {
        if (data == null || data.length < 16) {
            return false;
        }

        // Check entropy
        double entropy = calculateEntropy(data);
        if (entropy < 7.0) {
            return false;
        }

        // Check byte distribution
        if (!hasUniformByteDistribution(data, 0.15)) {
            return false;
        }

        // Check for text patterns (encrypted data shouldn't have readable text)
        if (hasSignificantTextContent(data)) {
            return false;
        }

        return true;
    }

    /**
     * Check if byte distribution is approximately uniform.
     * 检查字节分布是否大致均匀。
     *
     * @param data      the data to analyze | 要分析的数据
     * @param tolerance acceptable deviation from uniform (0.0-1.0) | 与均匀分布的可接受偏差 (0.0-1.0)
     * @return true if distribution is uniform within tolerance | 如果分布在容差范围内均匀则返回 true
     */
    public static boolean hasUniformByteDistribution(byte[] data, double tolerance) {
        if (data == null || data.length < 256) {
            return false;
        }

        int[] frequencies = new int[256];
        for (byte b : data) {
            frequencies[b & 0xFF]++;
        }

        double expected = data.length / 256.0;
        double allowedDeviation = expected * tolerance;

        for (int freq : frequencies) {
            if (Math.abs(freq - expected) > allowedDeviation * 2) {
                // Allow some bytes to be completely missing in smaller samples
                if (freq > expected + allowedDeviation * 3) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check if data contains significant readable text content.
     * 检查数据是否包含大量可读文本内容。
     *
     * @param data the data to analyze | 要分析的数据
     * @return true if contains significant text | 如果包含大量文本则返回 true
     */
    public static boolean hasSignificantTextContent(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }

        int printableCount = 0;
        int consecutivePrintable = 0;
        int maxConsecutive = 0;

        for (byte b : data) {
            int unsigned = b & 0xFF;
            // ASCII printable characters: 0x20-0x7E
            if (unsigned >= 0x20 && unsigned <= 0x7E) {
                printableCount++;
                consecutivePrintable++;
                maxConsecutive = Math.max(maxConsecutive, consecutivePrintable);
            } else {
                consecutivePrintable = 0;
            }
        }

        double printableRatio = (double) printableCount / data.length;

        // If more than 70% printable or has long consecutive printable sequences
        return printableRatio > 0.7 || maxConsecutive > 20;
    }

    // ==================== Encoding Detection | 编码检测 ====================

    /**
     * Encoding types that can be detected.
     * 可检测的编码类型。
     */
    public enum EncodingType {
        /** Base64 standard encoding | Base64 标准编码 */
        BASE64,
        /** Base64 URL-safe encoding | Base64 URL安全编码 */
        BASE64_URL,
        /** Hexadecimal encoding | 十六进制编码 */
        HEX,
        /** PEM format (Base64 with headers) | PEM 格式 (带头部的 Base64) */
        PEM,
        /** ASCII Armor (PGP) | ASCII Armor (PGP) */
        ASCII_ARMOR,
        /** Unknown or plain text | 未知或纯文本 */
        UNKNOWN
    }

    private static final Pattern BASE64_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+/]+=*$");
    private static final Pattern BASE64_URL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9_-]+=*$");
    private static final Pattern HEX_PATTERN = Pattern.compile(
            "^[0-9A-Fa-f]+$");
    private static final Pattern PEM_PATTERN = Pattern.compile(
            "-----BEGIN [A-Z0-9 ]+-----");
    private static final Pattern ASCII_ARMOR_PATTERN = Pattern.compile(
            "-----BEGIN PGP [A-Z ]+-----");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * Detect the encoding type of a string.
     * 检测字符串的编码类型。
     *
     * @param text the text to analyze | 要分析的文本
     * @return the detected encoding type | 检测到的编码类型
     */
    public static EncodingType detectEncoding(String text) {
        if (text == null || text.isEmpty()) {
            return EncodingType.UNKNOWN;
        }

        String trimmed = text.trim();

        // Check PEM format first (has specific headers)
        if (PEM_PATTERN.matcher(trimmed).find()) {
            return EncodingType.PEM;
        }

        // Check ASCII Armor (PGP)
        if (ASCII_ARMOR_PATTERN.matcher(trimmed).find()) {
            return EncodingType.ASCII_ARMOR;
        }

        // Remove whitespace for pattern matching
        String noWhitespace = WHITESPACE_PATTERN.matcher(trimmed).replaceAll("");

        // Check hex
        if (noWhitespace.length() >= 2 && noWhitespace.length() % 2 == 0) {
            if (HEX_PATTERN.matcher(noWhitespace).matches()) {
                // Could be hex, but also check if it might be Base64
                if (!containsBase64SpecificChars(noWhitespace)) {
                    return EncodingType.HEX;
                }
            }
        }

        // Check Base64 URL
        if (BASE64_URL_PATTERN.matcher(noWhitespace).matches() && noWhitespace.length() >= 4) {
            if (noWhitespace.contains("-") || noWhitespace.contains("_")) {
                return EncodingType.BASE64_URL;
            }
        }

        // Check Base64 standard
        if (BASE64_PATTERN.matcher(noWhitespace).matches() && noWhitespace.length() >= 4) {
            // Verify it's valid Base64 length
            if (noWhitespace.length() % 4 == 0 || noWhitespace.endsWith("=")) {
                return EncodingType.BASE64;
            }
        }

        return EncodingType.UNKNOWN;
    }

    private static boolean containsBase64SpecificChars(String text) {
        for (char c : text.toCharArray()) {
            if (c == '+' || c == '/' || c == '=' ||
                    (c >= 'G' && c <= 'Z') || (c >= 'g' && c <= 'z')) {
                return true;
            }
        }
        return false;
    }

    // ==================== Key Format Detection | 密钥格式检测 ====================

    /**
     * Key formats that can be detected.
     * 可检测的密钥格式。
     */
    public enum KeyFormat {
        /** PEM RSA Private Key | PEM RSA 私钥 */
        PEM_RSA_PRIVATE,
        /** PEM RSA Public Key | PEM RSA 公钥 */
        PEM_RSA_PUBLIC,
        /** PEM EC Private Key | PEM EC 私钥 */
        PEM_EC_PRIVATE,
        /** PEM EC Public Key | PEM EC 公钥 */
        PEM_EC_PUBLIC,
        /** PEM X.509 Certificate | PEM X.509 证书 */
        PEM_CERTIFICATE,
        /** PEM PKCS#8 Private Key | PEM PKCS#8 私钥 */
        PEM_PKCS8_PRIVATE,
        /** PEM PKCS#8 Encrypted Private Key | PEM PKCS#8 加密私钥 */
        PEM_PKCS8_ENCRYPTED,
        /** PEM Public Key (generic) | PEM 公钥 (通用) */
        PEM_PUBLIC_KEY,
        /** OpenSSH Public Key | OpenSSH 公钥 */
        OPENSSH_PUBLIC,
        /** OpenSSH Private Key | OpenSSH 私钥 */
        OPENSSH_PRIVATE,
        /** JWK (JSON Web Key) | JWK (JSON Web Key) */
        JWK,
        /** Unknown format | 未知格式 */
        UNKNOWN
    }

    private static final Map<String, KeyFormat> PEM_HEADERS = Map.ofEntries(
            Map.entry("-----BEGIN RSA PRIVATE KEY-----", KeyFormat.PEM_RSA_PRIVATE),
            Map.entry("-----BEGIN RSA PUBLIC KEY-----", KeyFormat.PEM_RSA_PUBLIC),
            Map.entry("-----BEGIN EC PRIVATE KEY-----", KeyFormat.PEM_EC_PRIVATE),
            Map.entry("-----BEGIN EC PUBLIC KEY-----", KeyFormat.PEM_EC_PUBLIC),
            Map.entry("-----BEGIN CERTIFICATE-----", KeyFormat.PEM_CERTIFICATE),
            Map.entry("-----BEGIN PRIVATE KEY-----", KeyFormat.PEM_PKCS8_PRIVATE),
            Map.entry("-----BEGIN ENCRYPTED PRIVATE KEY-----", KeyFormat.PEM_PKCS8_ENCRYPTED),
            Map.entry("-----BEGIN PUBLIC KEY-----", KeyFormat.PEM_PUBLIC_KEY),
            Map.entry("-----BEGIN OPENSSH PRIVATE KEY-----", KeyFormat.OPENSSH_PRIVATE)
    );

    /**
     * Detect the key format from a string.
     * 从字符串检测密钥格式。
     *
     * @param keyData the key data to analyze | 要分析的密钥数据
     * @return the detected key format | 检测到的密钥格式
     */
    public static KeyFormat detectKeyFormat(String keyData) {
        if (keyData == null || keyData.isEmpty()) {
            return KeyFormat.UNKNOWN;
        }

        String trimmed = keyData.trim();

        // Check PEM headers
        for (Map.Entry<String, KeyFormat> entry : PEM_HEADERS.entrySet()) {
            if (trimmed.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Check OpenSSH public key format
        if (trimmed.startsWith("ssh-rsa ") ||
                trimmed.startsWith("ssh-ed25519 ") ||
                trimmed.startsWith("ecdsa-sha2-")) {
            return KeyFormat.OPENSSH_PUBLIC;
        }

        // Check JWK format
        if (trimmed.startsWith("{") && trimmed.contains("\"kty\"")) {
            return KeyFormat.JWK;
        }

        return KeyFormat.UNKNOWN;
    }

    // ==================== Hash Format Detection | 哈希格式检测 ====================

    /**
     * Hash formats that can be detected.
     * 可检测的哈希格式。
     */
    public enum HashFormat {
        /** MD5 (32 hex chars) | MD5 (32 个十六进制字符) */
        MD5,
        /** SHA-1 (40 hex chars) | SHA-1 (40 个十六进制字符) */
        SHA1,
        /** SHA-256 (64 hex chars) | SHA-256 (64 个十六进制字符) */
        SHA256,
        /** SHA-384 (96 hex chars) | SHA-384 (96 个十六进制字符) */
        SHA384,
        /** SHA-512 (128 hex chars) | SHA-512 (128 个十六进制字符) */
        SHA512,
        /** SHA3-256 (64 hex chars) | SHA3-256 (64 个十六进制字符) */
        SHA3_256,
        /** SHA3-512 (128 hex chars) | SHA3-512 (128 个十六进制字符) */
        SHA3_512,
        /** BCrypt hash | BCrypt 哈希 */
        BCRYPT,
        /** SCrypt hash | SCrypt 哈希 */
        SCRYPT,
        /** Argon2 hash | Argon2 哈希 */
        ARGON2,
        /** PBKDF2 hash | PBKDF2 哈希 */
        PBKDF2,
        /** Unknown format | 未知格式 */
        UNKNOWN
    }

    /**
     * Detect the hash format from a string.
     * 从字符串检测哈希格式。
     *
     * @param hashString the hash string to analyze | 要分析的哈希字符串
     * @return the detected hash format | 检测到的哈希格式
     */
    public static HashFormat detectHashFormat(String hashString) {
        if (hashString == null || hashString.isEmpty()) {
            return HashFormat.UNKNOWN;
        }

        String trimmed = hashString.trim();

        // Check password hash formats first
        if (trimmed.startsWith("$2a$") || trimmed.startsWith("$2b$") || trimmed.startsWith("$2y$")) {
            return HashFormat.BCRYPT;
        }

        if (trimmed.startsWith("$argon2i$") || trimmed.startsWith("$argon2d$") || trimmed.startsWith("$argon2id$")) {
            return HashFormat.ARGON2;
        }

        if (trimmed.startsWith("$scrypt$") || trimmed.startsWith("$s0$")) {
            return HashFormat.SCRYPT;
        }

        if (trimmed.startsWith("$pbkdf2")) {
            return HashFormat.PBKDF2;
        }

        // Check hex hash formats
        if (HEX_PATTERN.matcher(trimmed).matches()) {
            return switch (trimmed.length()) {
                case 32 -> HashFormat.MD5;
                case 40 -> HashFormat.SHA1;
                case 64 -> HashFormat.SHA256; // or SHA3-256
                case 96 -> HashFormat.SHA384;
                case 128 -> HashFormat.SHA512; // or SHA3-512
                default -> HashFormat.UNKNOWN;
            };
        }

        return HashFormat.UNKNOWN;
    }

    // ==================== Comprehensive Analysis | 综合分析 ====================

    /**
     * Result of cryptographic analysis.
     * 加密分析结果。
     *
     * @param entropy             Shannon entropy value | 香农熵值
     * @param looksEncrypted      whether data appears encrypted | 数据是否看起来被加密
     * @param looksCompressed     whether data appears compressed | 数据是否看起来被压缩
     * @param hasTextContent      whether data contains readable text | 数据是否包含可读文本
     * @param uniformDistribution whether byte distribution is uniform | 字节分布是否均匀
     * @param detectedEncoding    detected encoding type | 检测到的编码类型
     * @param detectedKeyFormat   detected key format (if applicable) | 检测到的密钥格式（如果适用）
     * @param detectedHashFormat  detected hash format (if applicable) | 检测到的哈希格式（如果适用）
     */
    public record CryptoAnalysis(
            double entropy,
            boolean looksEncrypted,
            boolean looksCompressed,
            boolean hasTextContent,
            boolean uniformDistribution,
            EncodingType detectedEncoding,
            KeyFormat detectedKeyFormat,
            HashFormat detectedHashFormat
    ) {
        /**
         * Returns true if the data appears to be cryptographic in nature.
         * 如果数据看起来具有加密性质则返回 true。
         *
         * @return true if cryptographic patterns detected | 如果检测到加密模式则返回 true
         */
        public boolean isCryptographic() {
            return looksEncrypted ||
                    detectedKeyFormat != KeyFormat.UNKNOWN ||
                    detectedHashFormat != HashFormat.UNKNOWN ||
                    detectedEncoding == EncodingType.PEM ||
                    detectedEncoding == EncodingType.ASCII_ARMOR;
        }
    }

    /**
     * Perform comprehensive cryptographic analysis on byte data.
     * 对字节数据执行综合加密分析。
     *
     * @param data the data to analyze | 要分析的数据
     * @return analysis result | 分析结果
     */
    public static CryptoAnalysis analyze(byte[] data) {
        if (data == null || data.length == 0) {
            return new CryptoAnalysis(
                    0, false, false, false, false,
                    EncodingType.UNKNOWN, KeyFormat.UNKNOWN, HashFormat.UNKNOWN
            );
        }

        double entropy = calculateEntropy(data);
        boolean encrypted = looksEncrypted(data);
        boolean hasText = hasSignificantTextContent(data);
        boolean uniform = hasUniformByteDistribution(data, 0.15);

        // High entropy but has text patterns might indicate compression
        boolean compressed = entropy > 7.0 && !encrypted && !hasText && !uniform;

        // Try to detect formats from string representation
        String text = new String(data, StandardCharsets.UTF_8);
        EncodingType encoding = detectEncoding(text);
        KeyFormat keyFormat = detectKeyFormat(text);
        HashFormat hashFormat = detectHashFormat(text);

        return new CryptoAnalysis(
                entropy, encrypted, compressed, hasText, uniform,
                encoding, keyFormat, hashFormat
        );
    }

    /**
     * Perform comprehensive cryptographic analysis on text data.
     * 对文本数据执行综合加密分析。
     *
     * @param text the text to analyze | 要分析的文本
     * @return analysis result | 分析结果
     */
    public static CryptoAnalysis analyze(String text) {
        if (text == null || text.isEmpty()) {
            return new CryptoAnalysis(
                    0, false, false, false, false,
                    EncodingType.UNKNOWN, KeyFormat.UNKNOWN, HashFormat.UNKNOWN
            );
        }
        return analyze(text.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Check if a string looks like a cryptographic key or secret.
     * 检查字符串是否看起来像加密密钥或秘密。
     *
     * @param text the text to check | 要检查的文本
     * @return true if looks like a secret | 如果看起来像秘密则返回 true
     */
    public static boolean looksLikeSecret(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        // Check if it's a known key format
        KeyFormat format = detectKeyFormat(text);
        if (format != KeyFormat.UNKNOWN) {
            return true;
        }

        // Check if it's high entropy encoded data
        EncodingType encoding = detectEncoding(text);
        if (encoding == EncodingType.BASE64 || encoding == EncodingType.BASE64_URL) {
            try {
                byte[] decoded = java.util.Base64.getDecoder().decode(
                        WHITESPACE_PATTERN.matcher(text).replaceAll(""));
                return decoded.length >= 16 && calculateEntropy(decoded) > 6.0;
            } catch (IllegalArgumentException e) {
                // Invalid Base64
            }
        }

        if (encoding == EncodingType.HEX) {
            String cleaned = WHITESPACE_PATTERN.matcher(text).replaceAll("");
            if (cleaned.length() >= 32) {
                // 32 hex chars = 16 bytes minimum for most keys
                return true;
            }
        }

        return false;
    }

    /**
     * Estimate the security strength in bits based on entropy.
     * 根据熵估计安全强度（以位为单位）。
     *
     * @param data the data to analyze | 要分析的数据
     * @return estimated security strength in bits | 估计的安全强度（位）
     */
    public static int estimateSecurityStrength(byte[] data) {
        if (data == null || data.length == 0) {
            return 0;
        }

        double entropy = calculateEntropy(data);
        // Security strength = entropy per byte * number of bytes
        return (int) (entropy * data.length);
    }
}
