package cloud.opencode.base.config.advanced;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigListener;
import cloud.opencode.base.config.OpenConfigException;

import cloud.opencode.base.config.converter.impl.DurationConverter;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Encrypted Configuration Processor
 * 加密配置处理器
 *
 * <p>Provides transparent decryption of encrypted configuration values.
 * Values prefixed with "ENC(" and suffixed with ")" are automatically decrypted.</p>
 * <p>提供加密配置值的透明解密。以"ENC("开头和")"结尾的值会自动解密。</p>
 *
 * <p><strong>Encrypted Format | 加密格式:</strong></p>
 * <pre>
 * database.password=ENC(base64EncodedEncryptedValue)
 * </pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SecretKey key = loadSecretKey();
 * Config config = EncryptedConfigProcessor.createEncryptedConfig(
 *     OpenConfig.getGlobal(), key);
 * String password = config.getString("database.password"); // Auto-decrypted
 * }</pre>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core EncryptedConfigProcessor functionality - EncryptedConfigProcessor核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) per encrypt/decrypt where n is plaintext length - 时间复杂度: 每次加密/解密 O(n)，n 为明文长度</li>
 *   <li>Space complexity: O(n) for cipher buffers - 空间复杂度: O(n) 密文缓冲区</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class EncryptedConfigProcessor {

    private static final String ENCRYPTED_PREFIX = "ENC(";
    private static final String ENCRYPTED_SUFFIX = ")";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public static Config createEncryptedConfig(Config source, SecretKey key) {
        return new EncryptedConfig(source, key);
    }

    /**
     * Encrypts a plaintext value using AES/GCM with a random IV.
     * 使用带随机 IV 的 AES/GCM 加密明文值。
     *
     * <p>The IV is prepended to the ciphertext before Base64 encoding.</p>
     * <p>IV 在 Base64 编码之前被添加到密文前面。</p>
     *
     * @param plaintext the plaintext to encrypt | 要加密的明文
     * @param key       the secret key | 密钥
     * @return the encrypted value wrapped in ENC() format | 以 ENC() 格式包装的加密值
     */
    public static String encrypt(String plaintext, SecretKey key) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to ciphertext
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(combined) + ENCRYPTED_SUFFIX;
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    private static class EncryptedConfig implements Config {
        private static final DurationConverter DURATION_CONVERTER = new DurationConverter();
        private final Config delegate;
        private final SecretKey key;

        EncryptedConfig(Config delegate, SecretKey key) {
            this.delegate = delegate;
            this.key = key;
        }

        @Override
        public String getString(String key) {
            String value = delegate.getString(key);
            return decryptIfNeeded(value);
        }

        @Override
        public String getString(String key, String defaultValue) {
            String value = delegate.getString(key, defaultValue);
            return value != null ? decryptIfNeeded(value) : null;
        }

        private String decryptIfNeeded(String value) {
            if (value != null && value.startsWith(ENCRYPTED_PREFIX) && value.endsWith(ENCRYPTED_SUFFIX)) {
                String encrypted = value.substring(ENCRYPTED_PREFIX.length(), value.length() - ENCRYPTED_SUFFIX.length());
                return decrypt(encrypted);
            }
            return value;
        }

        private String decrypt(String encrypted) {
            try {
                byte[] decoded = Base64.getDecoder().decode(encrypted);
                if (decoded.length < GCM_IV_LENGTH) {
                    throw new IllegalArgumentException("Encrypted data too short to contain IV");
                }

                // Extract IV from first 12 bytes
                byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH);
                byte[] ciphertext = Arrays.copyOfRange(decoded, GCM_IV_LENGTH, decoded.length);

                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                cipher.init(Cipher.DECRYPT_MODE, key, spec);
                return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw OpenConfigException.decryptionFailed(e);
            }
        }

        // Numeric/boolean/duration delegate methods - decrypt first, then parse
        @Override public int getInt(String key) { return Integer.parseInt(getString(key)); }
        @Override public int getInt(String key, int defaultValue) {
            try { return Integer.parseInt(getString(key)); } catch (Exception e) { return defaultValue; }
        }
        @Override public long getLong(String key) { return Long.parseLong(getString(key)); }
        @Override public long getLong(String key, long defaultValue) {
            try { return Long.parseLong(getString(key)); } catch (Exception e) { return defaultValue; }
        }
        @Override public double getDouble(String key) { return Double.parseDouble(getString(key)); }
        @Override public double getDouble(String key, double defaultValue) {
            try { return Double.parseDouble(getString(key)); } catch (Exception e) { return defaultValue; }
        }
        @Override public boolean getBoolean(String key) { return Boolean.parseBoolean(getString(key)); }
        @Override public boolean getBoolean(String key, boolean defaultValue) {
            if (!hasKey(key)) return defaultValue;
            return Boolean.parseBoolean(getString(key));
        }
        @Override public Duration getDuration(String key) { return DURATION_CONVERTER.convert(getString(key)); }
        @Override public Duration getDuration(String key, Duration defaultValue) {
            try { return DURATION_CONVERTER.convert(getString(key)); } catch (Exception e) { return defaultValue; }
        }
        @Override @SuppressWarnings("unchecked")
        public <T> T get(String key, Class<T> type) {
            // Resolve through getString() first to decrypt, then convert
            if (type == String.class) {
                return (T) getString(key);
            }
            return delegate.get(key, type);
        }
        @Override @SuppressWarnings("unchecked")
        public <T> T get(String key, Class<T> type, T defaultValue) {
            try {
                if (type == String.class) {
                    return (T) getString(key);
                }
                return delegate.get(key, type, defaultValue);
            } catch (Exception e) { return defaultValue; }
        }
        @Override public <T> List<T> getList(String key, Class<T> elementType) { return delegate.getList(key, elementType); }
        @Override public <K, V> Map<K, V> getMap(String key, Class<K> keyType, Class<V> valueType) { return delegate.getMap(key, keyType, valueType); }
        @Override public Optional<String> getOptional(String key) {
            try { return Optional.ofNullable(getString(key)); } catch (Exception e) { return Optional.empty(); }
        }
        @Override public <T> Optional<T> getOptional(String key, Class<T> type) {
            try {
                return Optional.ofNullable(get(key, type));
            } catch (Exception e) { return Optional.empty(); }
        }
        @Override public Config getSubConfig(String prefix) { return delegate.getSubConfig(prefix); }
        @Override public Map<String, String> getByPrefix(String prefix) { return delegate.getByPrefix(prefix); }
        @Override public boolean hasKey(String key) { return delegate.hasKey(key); }
        @Override public Set<String> getKeys() { return delegate.getKeys(); }
        @Override public void addListener(ConfigListener listener) { delegate.addListener(listener); }
        @Override public void addListener(String key, ConfigListener listener) { delegate.addListener(key, listener); }
        @Override public void removeListener(ConfigListener listener) { delegate.removeListener(listener); }
        @Override public <T> T bind(String prefix, Class<T> type) { return delegate.bind(prefix, type); }
        @Override public <T> void bindTo(String prefix, T target) { delegate.bindTo(prefix, target); }
    }
}
