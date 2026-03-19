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

package cloud.opencode.base.crypto.rotation;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.key.KeyGenerator;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Key Rotation Manager - Automated key version management and rotation
 * 密钥轮换管理器 - 自动化密钥版本管理和轮换
 *
 * <p>Provides automatic key rotation with version management, graceful retirement
 * of old keys, and support for both symmetric and asymmetric keys.</p>
 * <p>提供自动密钥轮换，包括版本管理、旧密钥优雅退役，以及对称和非对称密钥的支持。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Version-based key management - 基于版本的密钥管理</li>
 *   <li>Automatic rotation scheduling - 自动轮换调度</li>
 *   <li>Graceful key retirement - 密钥优雅退役</li>
 *   <li>Key derivation support - 密钥派生支持</li>
 *   <li>Audit logging - 审计日志</li>
 *   <li>Thread-safe operations - 线程安全操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a key rotation manager
 * KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
 *     .keyId("encryption-key")
 *     .keyGenerator(() -> KeyGenerator.generateAesKey(256))
 *     .rotationInterval(Duration.ofDays(90))
 *     .gracePeriod(Duration.ofDays(7))
 *     .maxVersions(3)
 *     .onRotation(event -> log.info("Key rotated: {}", event))
 *     .build();
 *
 * // Start automatic rotation
 * rotation.startAutoRotation();
 *
 * // Get current key for encryption
 * VersionedKey<SecretKey> current = rotation.getCurrentKey();
 *
 * // Decrypt with specific version
 * SecretKey key = rotation.getKeyByVersion(1).key();
 *
 * // Manual rotation
 * rotation.rotate();
 *
 * // Retire old keys
 * rotation.retireOldVersions();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @param <K> the key type - 密钥类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see VersionedKey
 * @see RotationEvent
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class KeyRotation<K> implements AutoCloseable {

    private static final System.Logger LOGGER = System.getLogger(KeyRotation.class.getName());

    private final String keyId;
    private final Supplier<K> keyGenerator;
    private final Duration rotationInterval;
    private final Duration gracePeriod;
    private final int maxVersions;
    private final List<Consumer<RotationEvent>> listeners;

    private final Map<Long, VersionedKey<K>> keyVersions = new ConcurrentHashMap<>();
    private volatile long currentVersion = 0;
    private volatile Instant lastRotationTime;
    private ScheduledExecutorService scheduler;

    private KeyRotation(Builder<K> builder) {
        this.keyId = builder.keyId;
        this.keyGenerator = builder.keyGenerator;
        this.rotationInterval = builder.rotationInterval;
        this.gracePeriod = builder.gracePeriod;
        this.maxVersions = builder.maxVersions;
        this.listeners = new ArrayList<>(builder.listeners);
        this.lastRotationTime = Instant.now();

        // Generate initial key
        if (builder.initialKey != null) {
            addKey(builder.initialKey);
        } else {
            rotate();
        }
    }

    // ==================== Key Access | 密钥访问 ====================

    /**
     * Gets the current (latest) key for encryption.
     * 获取当前（最新）密钥用于加密。
     *
     * @return the current versioned key - 当前版本化密钥
     */
    public VersionedKey<K> getCurrentKey() {
        return keyVersions.get(currentVersion);
    }

    /**
     * Gets a key by its version number.
     * 根据版本号获取密钥。
     *
     * @param version the version number - 版本号
     * @return the versioned key - 版本化密钥
     * @throws OpenCryptoException if version not found - 如果版本未找到
     */
    public VersionedKey<K> getKeyByVersion(long version) {
        VersionedKey<K> key = keyVersions.get(version);
        if (key == null) {
            throw new OpenCryptoException("Key version not found: " + version);
        }
        return key;
    }

    /**
     * Gets the current version number.
     * 获取当前版本号。
     *
     * @return the current version - 当前版本
     */
    public long getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Gets all available versions.
     * 获取所有可用版本。
     *
     * @return set of version numbers - 版本号集合
     */
    public Set<Long> getAvailableVersions() {
        return Collections.unmodifiableSet(keyVersions.keySet());
    }

    /**
     * Gets the key ID.
     * 获取密钥 ID。
     *
     * @return the key ID - 密钥 ID
     */
    public String getKeyId() {
        return keyId;
    }

    // ==================== Rotation Operations | 轮换操作 ====================

    /**
     * Manually triggers a key rotation.
     * 手动触发密钥轮换。
     *
     * @return the new versioned key - 新的版本化密钥
     */
    public synchronized VersionedKey<K> rotate() {
        LOGGER.log(System.Logger.Level.INFO, "Key rotation started for keyId={0}", keyId);
        K newKey = keyGenerator.get();
        VersionedKey<K> versionedKey = addKey(newKey);
        LOGGER.log(System.Logger.Level.INFO, "Key rotation completed for keyId={0}, newVersion={1}",
                keyId, versionedKey.version());
        return versionedKey;
    }

    /**
     * Adds an existing key as a new version.
     * 将现有密钥添加为新版本。
     *
     * @param key the key to add - 要添加的密钥
     * @return the versioned key - 版本化密钥
     */
    public synchronized VersionedKey<K> addKey(K key) {
        long newVersion = ++currentVersion;
        Instant now = Instant.now();

        VersionedKey<K> versionedKey = new VersionedKey<>(
                keyId,
                newVersion,
                key,
                now,
                KeyStatus.ACTIVE
        );

        keyVersions.put(newVersion, versionedKey);
        lastRotationTime = now;

        // Mark previous version as deprecated
        if (newVersion > 1) {
            markPreviousAsDeprecated(newVersion - 1);
        }

        // Cleanup old versions
        cleanupOldVersions();

        // Notify listeners
        notifyListeners(new RotationEvent(
                keyId,
                RotationEvent.Type.KEY_ROTATED,
                newVersion,
                now
        ));

        return versionedKey;
    }

    /**
     * Imports a key with a specific version.
     * 导入具有特定版本的密钥。
     *
     * @param version the version number - 版本号
     * @param key the key - 密钥
     * @param createdAt the creation time - 创建时间
     */
    public synchronized void importKey(long version, K key, Instant createdAt) {
        VersionedKey<K> versionedKey = new VersionedKey<>(
                keyId,
                version,
                key,
                createdAt,
                version == currentVersion ? KeyStatus.ACTIVE : KeyStatus.DEPRECATED
        );

        keyVersions.put(version, versionedKey);

        if (version > currentVersion) {
            currentVersion = version;
        }
    }

    /**
     * Retires keys older than the grace period.
     * 退役超过宽限期的密钥。
     *
     * @return count of retired keys - 退役的密钥数量
     */
    public synchronized int retireOldVersions() {
        Instant cutoff = Instant.now().minus(gracePeriod);
        int retired = 0;

        for (var entry : keyVersions.entrySet()) {
            if (entry.getKey() < currentVersion) {
                VersionedKey<K> key = entry.getValue();
                if (key.status() == KeyStatus.DEPRECATED &&
                        key.createdAt().isBefore(cutoff)) {

                    keyVersions.put(entry.getKey(), key.withStatus(KeyStatus.RETIRED));
                    retired++;

                    notifyListeners(new RotationEvent(
                            keyId,
                            RotationEvent.Type.KEY_RETIRED,
                            entry.getKey(),
                            Instant.now()
                    ));
                }
            }
        }

        return retired;
    }

    /**
     * Removes retired keys permanently.
     * 永久删除已退役的密钥。
     *
     * @return count of deleted keys - 删除的密钥数量
     */
    public synchronized int deleteRetiredKeys() {
        int deleted = 0;
        Iterator<Map.Entry<Long, VersionedKey<K>>> iter = keyVersions.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<Long, VersionedKey<K>> entry = iter.next();
            if (entry.getValue().status() == KeyStatus.RETIRED) {
                iter.remove();
                deleted++;

                notifyListeners(new RotationEvent(
                        keyId,
                        RotationEvent.Type.KEY_DELETED,
                        entry.getKey(),
                        Instant.now()
                ));
            }
        }

        return deleted;
    }

    // ==================== Auto Rotation | 自动轮换 ====================

    /**
     * Starts automatic key rotation.
     * 启动自动密钥轮换。
     */
    public synchronized void startAutoRotation() {
        if (scheduler != null) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "key-rotation-" + keyId);
            t.setDaemon(true);
            return t;
        });

        long intervalMillis = rotationInterval.toMillis();
        scheduler.scheduleAtFixedRate(
                this::autoRotate,
                intervalMillis,
                intervalMillis,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Stops automatic key rotation.
     * 停止自动密钥轮换。
     */
    public synchronized void stopAutoRotation() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            scheduler = null;
        }
    }

    /**
     * Checks if a rotation is needed based on the interval.
     * 根据间隔检查是否需要轮换。
     *
     * @return true if rotation is needed - 如果需要轮换返回 true
     */
    public boolean isRotationNeeded() {
        return Instant.now().isAfter(lastRotationTime.plus(rotationInterval));
    }

    /**
     * Gets the time until next rotation.
     * 获取到下次轮换的时间。
     *
     * @return duration until next rotation - 到下次轮换的时间
     */
    public Duration getTimeUntilNextRotation() {
        Instant nextRotation = lastRotationTime.plus(rotationInterval);
        Duration remaining = Duration.between(Instant.now(), nextRotation);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    // ==================== Lifecycle | 生命周期 ====================

    @Override
    public void close() {
        stopAutoRotation();
        keyVersions.clear();
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @param <K> the key type - 密钥类型
     * @return the builder - 构建器
     */
    public static <K> Builder<K> builder() {
        return new Builder<>();
    }

    /**
     * Creates a key rotation for AES keys.
     * 为 AES 密钥创建密钥轮换。
     *
     * @param keyId the key ID - 密钥 ID
     * @param keyBits the key size in bits (128, 192, or 256) - 密钥位数
     * @return the key rotation manager - 密钥轮换管理器
     */
    public static KeyRotation<SecretKey> forAes(String keyId, int keyBits) {
        return KeyRotation.<SecretKey>builder()
                .keyId(keyId)
                .keyGenerator(() -> KeyGenerator.generateAesKey(keyBits))
                .build();
    }

    /**
     * Creates a key rotation for RSA key pairs.
     * 为 RSA 密钥对创建密钥轮换。
     *
     * @param keyId the key ID - 密钥 ID
     * @param keyBits the key size in bits - 密钥位数
     * @return the key rotation manager - 密钥轮换管理器
     */
    public static KeyRotation<KeyPair> forRsa(String keyId, int keyBits) {
        return KeyRotation.<KeyPair>builder()
                .keyId(keyId)
                .keyGenerator(() -> KeyGenerator.generateRsaKeyPair(keyBits))
                .build();
    }

    // ==================== Private Methods | 私有方法 ====================

    private void autoRotate() {
        try {
            if (isRotationNeeded()) {
                rotate();
            }
            retireOldVersions();
            deleteRetiredKeys();
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.ERROR, "Auto-rotation failed for keyId={0}: {1}",
                    keyId, e.getMessage());
            notifyListeners(new RotationEvent(
                    keyId,
                    RotationEvent.Type.ROTATION_FAILED,
                    currentVersion,
                    Instant.now()
            ));
        }
    }

    private void markPreviousAsDeprecated(long version) {
        VersionedKey<K> previous = keyVersions.get(version);
        if (previous != null && previous.status() == KeyStatus.ACTIVE) {
            keyVersions.put(version, previous.withStatus(KeyStatus.DEPRECATED));

            notifyListeners(new RotationEvent(
                    keyId,
                    RotationEvent.Type.KEY_DEPRECATED,
                    version,
                    Instant.now()
            ));
        }
    }

    private void cleanupOldVersions() {
        if (keyVersions.size() > maxVersions) {
            List<Long> versions = new ArrayList<>(keyVersions.keySet());
            Collections.sort(versions);

            while (keyVersions.size() > maxVersions) {
                if (versions.isEmpty()) {
                    break;
                }
                Long oldest = versions.removeFirst();
                if (oldest >= currentVersion) {
                    break;
                }
                keyVersions.remove(oldest);
            }
        }
    }

    private void notifyListeners(RotationEvent event) {
        for (Consumer<RotationEvent> listener : listeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Rotation listener threw exception for keyId={0}, event={1}: {2}",
                        keyId, event.type(), e.getMessage());
            }
        }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for KeyRotation.
     * KeyRotation 构建器。
     *
     * @param <K> the key type - 密钥类型
     */
    public static final class Builder<K> {
        private String keyId = "default-key";
        private Supplier<K> keyGenerator;
        private Duration rotationInterval = Duration.ofDays(90);
        private Duration gracePeriod = Duration.ofDays(7);
        private int maxVersions = 5;
        private K initialKey;
        private final List<Consumer<RotationEvent>> listeners = new ArrayList<>();

        private Builder() {}

        public Builder<K> keyId(String keyId) {
            this.keyId = Objects.requireNonNull(keyId);
            return this;
        }

        public Builder<K> keyGenerator(Supplier<K> generator) {
            this.keyGenerator = Objects.requireNonNull(generator);
            return this;
        }

        public Builder<K> rotationInterval(Duration interval) {
            Objects.requireNonNull(interval);
            if (interval.isNegative() || interval.isZero()) {
                throw new IllegalArgumentException("rotationInterval must be positive");
            }
            this.rotationInterval = interval;
            return this;
        }

        public Builder<K> gracePeriod(Duration period) {
            Objects.requireNonNull(period);
            if (period.isNegative()) {
                throw new IllegalArgumentException("gracePeriod must not be negative");
            }
            this.gracePeriod = period;
            return this;
        }

        public Builder<K> maxVersions(int max) {
            if (max < 1) {
                throw new IllegalArgumentException("maxVersions must be at least 1, got: " + max);
            }
            this.maxVersions = max;
            return this;
        }

        public Builder<K> initialKey(K key) {
            this.initialKey = key;
            return this;
        }

        public Builder<K> onRotation(Consumer<RotationEvent> listener) {
            this.listeners.add(Objects.requireNonNull(listener));
            return this;
        }

        public KeyRotation<K> build() {
            if (keyGenerator == null && initialKey == null) {
                throw new IllegalStateException("Either keyGenerator or initialKey must be set");
            }
            return new KeyRotation<>(this);
        }
    }

    // ==================== Nested Types | 嵌套类型 ====================

    /**
     * Status of a key version.
     * 密钥版本的状态。
     */
    public enum KeyStatus {
        /** Key is currently active for encryption */
        ACTIVE,
        /** Key is deprecated, only for decryption */
        DEPRECATED,
        /** Key is retired, should not be used */
        RETIRED
    }

    /**
     * A versioned key with metadata.
     * 带元数据的版本化密钥。
     *
     * @param <K> the key type - 密钥类型
     */
    public record VersionedKey<K>(
            String keyId,
            long version,
            K key,
            Instant createdAt,
            KeyStatus status
    ) {
        public VersionedKey<K> withStatus(KeyStatus newStatus) {
            return new VersionedKey<>(keyId, version, key, createdAt, newStatus);
        }
    }

    /**
     * Event triggered during key rotation.
     * 密钥轮换期间触发的事件。
     */
    public record RotationEvent(
            String keyId,
            Type type,
            long version,
            Instant timestamp
    ) {
        public enum Type {
            KEY_ROTATED,
            KEY_DEPRECATED,
            KEY_RETIRED,
            KEY_DELETED,
            ROTATION_FAILED
        }
    }
}
