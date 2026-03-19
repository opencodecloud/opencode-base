package cloud.opencode.base.crypto.rotation;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.key.KeyGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * KeyRotation 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("KeyRotation 测试")
class KeyRotationTest {

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("使用keyGenerator创建KeyRotation")
        void testBuildWithKeyGenerator() {
            KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test-key")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .build();

            assertThat(rotation.getKeyId()).isEqualTo("test-key");
            assertThat(rotation.getCurrentVersion()).isEqualTo(1);
            assertThat(rotation.getCurrentKey()).isNotNull();

            rotation.close();
        }

        @Test
        @DisplayName("使用initialKey创建KeyRotation")
        void testBuildWithInitialKey() {
            SecretKey initialKey = KeyGenerator.generateAesKey(256);
            KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test-key")
                    .initialKey(initialKey)
                    .build();

            assertThat(rotation.getCurrentKey().key()).isEqualTo(initialKey);
            assertThat(rotation.getCurrentVersion()).isEqualTo(1);

            rotation.close();
        }

        @Test
        @DisplayName("没有keyGenerator和initialKey抛出异常")
        void testBuildWithoutKeyGeneratorOrInitialKey() {
            assertThatThrownBy(() -> KeyRotation.<SecretKey>builder()
                    .keyId("test-key")
                    .build())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("keyGenerator or initialKey");
        }

        @Test
        @DisplayName("自定义rotationInterval")
        void testCustomRotationInterval() {
            KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test-key")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .rotationInterval(Duration.ofDays(30))
                    .build();

            assertThat(rotation.getTimeUntilNextRotation()).isLessThanOrEqualTo(Duration.ofDays(30));

            rotation.close();
        }

        @Test
        @DisplayName("自定义maxVersions")
        void testCustomMaxVersions() {
            KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test-key")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .maxVersions(2)
                    .build();

            // 轮换3次，只保留最新的2个版本
            rotation.rotate();
            rotation.rotate();
            rotation.rotate();

            assertThat(rotation.getAvailableVersions()).hasSize(2);

            rotation.close();
        }

        @Test
        @DisplayName("注册onRotation监听器")
        void testOnRotationListener() {
            List<KeyRotation.RotationEvent> events = new ArrayList<>();

            KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test-key")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .onRotation(events::add)
                    .build();

            // 构建时会触发第一次轮换
            assertThat(events).hasSize(1);
            assertThat(events.getFirst().type()).isEqualTo(KeyRotation.RotationEvent.Type.KEY_ROTATED);

            rotation.close();
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("forAes创建AES密钥轮换")
        void testForAes() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.forAes("aes-key", 256)) {
                assertThat(rotation.getKeyId()).isEqualTo("aes-key");
                assertThat(rotation.getCurrentKey().key().getAlgorithm()).isEqualTo("AES");
            }
        }

        @Test
        @DisplayName("forRsa创建RSA密钥轮换")
        void testForRsa() {
            try (KeyRotation<KeyPair> rotation = KeyRotation.forRsa("rsa-key", 2048)) {
                assertThat(rotation.getKeyId()).isEqualTo("rsa-key");
                assertThat(rotation.getCurrentKey().key().getPublic().getAlgorithm()).isEqualTo("RSA");
            }
        }
    }

    @Nested
    @DisplayName("密钥访问测试")
    class KeyAccessTests {

        @Test
        @DisplayName("getCurrentKey返回当前密钥")
        void testGetCurrentKey() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.forAes("test", 256)) {
                KeyRotation.VersionedKey<SecretKey> current = rotation.getCurrentKey();

                assertThat(current).isNotNull();
                assertThat(current.keyId()).isEqualTo("test");
                assertThat(current.version()).isEqualTo(1);
                assertThat(current.status()).isEqualTo(KeyRotation.KeyStatus.ACTIVE);
                assertThat(current.key()).isNotNull();
                assertThat(current.createdAt()).isNotNull();
            }
        }

        @Test
        @DisplayName("getKeyByVersion返回指定版本密钥")
        void testGetKeyByVersion() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.forAes("test", 256)) {
                rotation.rotate();
                rotation.rotate();

                KeyRotation.VersionedKey<SecretKey> v1 = rotation.getKeyByVersion(1);
                KeyRotation.VersionedKey<SecretKey> v2 = rotation.getKeyByVersion(2);
                KeyRotation.VersionedKey<SecretKey> v3 = rotation.getKeyByVersion(3);

                assertThat(v1.version()).isEqualTo(1);
                assertThat(v2.version()).isEqualTo(2);
                assertThat(v3.version()).isEqualTo(3);
            }
        }

        @Test
        @DisplayName("getKeyByVersion对不存在的版本抛出异常")
        void testGetKeyByVersionNotFound() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.forAes("test", 256)) {
                assertThatThrownBy(() -> rotation.getKeyByVersion(999))
                        .isInstanceOf(OpenCryptoException.class)
                        .hasMessageContaining("version not found");
            }
        }

        @Test
        @DisplayName("getCurrentVersion返回当前版本号")
        void testGetCurrentVersion() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.forAes("test", 256)) {
                assertThat(rotation.getCurrentVersion()).isEqualTo(1);

                rotation.rotate();
                assertThat(rotation.getCurrentVersion()).isEqualTo(2);

                rotation.rotate();
                assertThat(rotation.getCurrentVersion()).isEqualTo(3);
            }
        }

        @Test
        @DisplayName("getAvailableVersions返回所有版本")
        void testGetAvailableVersions() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.forAes("test", 256)) {
                rotation.rotate();
                rotation.rotate();

                Set<Long> versions = rotation.getAvailableVersions();
                assertThat(versions).containsExactlyInAnyOrder(1L, 2L, 3L);
            }
        }
    }

    @Nested
    @DisplayName("轮换操作测试")
    class RotationOperationsTests {

        @Test
        @DisplayName("rotate创建新密钥")
        void testRotate() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.forAes("test", 256)) {
                SecretKey original = rotation.getCurrentKey().key();

                KeyRotation.VersionedKey<SecretKey> newKey = rotation.rotate();

                assertThat(newKey.version()).isEqualTo(2);
                assertThat(newKey.key()).isNotEqualTo(original);
                assertThat(rotation.getCurrentKey().key()).isEqualTo(newKey.key());
            }
        }

        @Test
        @DisplayName("rotate将前一个版本标记为DEPRECATED")
        void testRotateDeprecatesPrevious() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.forAes("test", 256)) {
                assertThat(rotation.getKeyByVersion(1).status()).isEqualTo(KeyRotation.KeyStatus.ACTIVE);

                rotation.rotate();

                assertThat(rotation.getKeyByVersion(1).status()).isEqualTo(KeyRotation.KeyStatus.DEPRECATED);
                assertThat(rotation.getKeyByVersion(2).status()).isEqualTo(KeyRotation.KeyStatus.ACTIVE);
            }
        }

        @Test
        @DisplayName("addKey添加现有密钥")
        void testAddKey() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.forAes("test", 256)) {
                SecretKey customKey = new SecretKeySpec(new byte[32], "AES");

                KeyRotation.VersionedKey<SecretKey> added = rotation.addKey(customKey);

                assertThat(added.key()).isEqualTo(customKey);
                assertThat(rotation.getCurrentKey().key()).isEqualTo(customKey);
            }
        }

        @Test
        @DisplayName("importKey导入指定版本密钥")
        void testImportKey() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.forAes("test", 256)) {
                SecretKey importedKey = new SecretKeySpec(new byte[32], "AES");
                Instant importTime = Instant.now().minus(Duration.ofDays(30));

                rotation.importKey(10, importedKey, importTime);

                KeyRotation.VersionedKey<SecretKey> imported = rotation.getKeyByVersion(10);
                assertThat(imported.key()).isEqualTo(importedKey);
                assertThat(imported.createdAt()).isEqualTo(importTime);
                assertThat(rotation.getCurrentVersion()).isEqualTo(10);
            }
        }
    }

    @Nested
    @DisplayName("退役和删除测试")
    class RetireAndDeleteTests {

        @Test
        @DisplayName("retireOldVersions退役超期密钥")
        void testRetireOldVersions() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .gracePeriod(Duration.ZERO) // 立即退役
                    .build()) {

                rotation.rotate();

                int retired = rotation.retireOldVersions();

                assertThat(retired).isEqualTo(1);
                assertThat(rotation.getKeyByVersion(1).status()).isEqualTo(KeyRotation.KeyStatus.RETIRED);
            }
        }

        @Test
        @DisplayName("deleteRetiredKeys删除已退役密钥")
        void testDeleteRetiredKeys() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .gracePeriod(Duration.ZERO)
                    .build()) {

                rotation.rotate();
                rotation.retireOldVersions();

                int deleted = rotation.deleteRetiredKeys();

                assertThat(deleted).isEqualTo(1);
                assertThat(rotation.getAvailableVersions()).containsExactly(2L);
            }
        }
    }

    @Nested
    @DisplayName("自动轮换测试")
    class AutoRotationTests {

        @Test
        @DisplayName("isRotationNeeded检测是否需要轮换")
        void testIsRotationNeeded() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .rotationInterval(Duration.ofMillis(1))
                    .build()) {

                // 刚创建，不需要轮换
                assertThat(rotation.isRotationNeeded()).isFalse();

                // 等待超过间隔
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                assertThat(rotation.isRotationNeeded()).isTrue();
            }
        }

        @Test
        @DisplayName("getTimeUntilNextRotation返回剩余时间")
        void testGetTimeUntilNextRotation() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .rotationInterval(Duration.ofDays(90))
                    .build()) {

                Duration remaining = rotation.getTimeUntilNextRotation();

                assertThat(remaining).isPositive();
                assertThat(remaining).isLessThanOrEqualTo(Duration.ofDays(90));
            }
        }

        @Test
        @DisplayName("startAutoRotation和stopAutoRotation")
        void testStartStopAutoRotation() throws InterruptedException {
            AtomicInteger rotationCount = new AtomicInteger(0);

            try (KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .rotationInterval(Duration.ofMillis(50))
                    .onRotation(event -> {
                        if (event.type() == KeyRotation.RotationEvent.Type.KEY_ROTATED) {
                            rotationCount.incrementAndGet();
                        }
                    })
                    .build()) {

                // 初始轮换
                assertThat(rotationCount.get()).isEqualTo(1);

                rotation.startAutoRotation();

                // 等待足够时间让自动轮换执行
                Thread.sleep(150);

                rotation.stopAutoRotation();

                // 应该有额外的自动轮换
                assertThat(rotationCount.get()).isGreaterThan(1);
            }
        }

        @Test
        @DisplayName("重复调用startAutoRotation无效")
        void testStartAutoRotationIdempotent() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.forAes("test", 256)) {
                rotation.startAutoRotation();
                rotation.startAutoRotation(); // 不应该创建新的调度器
                rotation.stopAutoRotation();
            }
        }
    }

    @Nested
    @DisplayName("事件监听器测试")
    class EventListenerTests {

        @Test
        @DisplayName("监听KEY_ROTATED事件")
        void testKeyRotatedEvent() {
            List<KeyRotation.RotationEvent> events = new ArrayList<>();

            try (KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .onRotation(events::add)
                    .build()) {

                rotation.rotate();
            }

            List<KeyRotation.RotationEvent> rotatedEvents = events.stream()
                    .filter(e -> e.type() == KeyRotation.RotationEvent.Type.KEY_ROTATED)
                    .toList();

            assertThat(rotatedEvents).hasSize(2); // 初始 + 手动轮换
        }

        @Test
        @DisplayName("监听KEY_DEPRECATED事件")
        void testKeyDeprecatedEvent() {
            List<KeyRotation.RotationEvent> events = new ArrayList<>();

            try (KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .onRotation(events::add)
                    .build()) {

                rotation.rotate();
            }

            boolean hasDeprecated = events.stream()
                    .anyMatch(e -> e.type() == KeyRotation.RotationEvent.Type.KEY_DEPRECATED);

            assertThat(hasDeprecated).isTrue();
        }

        @Test
        @DisplayName("监听KEY_RETIRED事件")
        void testKeyRetiredEvent() {
            List<KeyRotation.RotationEvent> events = new ArrayList<>();

            try (KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .gracePeriod(Duration.ZERO)
                    .onRotation(events::add)
                    .build()) {

                rotation.rotate();
                rotation.retireOldVersions();
            }

            boolean hasRetired = events.stream()
                    .anyMatch(e -> e.type() == KeyRotation.RotationEvent.Type.KEY_RETIRED);

            assertThat(hasRetired).isTrue();
        }

        @Test
        @DisplayName("监听KEY_DELETED事件")
        void testKeyDeletedEvent() {
            List<KeyRotation.RotationEvent> events = new ArrayList<>();

            try (KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .gracePeriod(Duration.ZERO)
                    .onRotation(events::add)
                    .build()) {

                rotation.rotate();
                rotation.retireOldVersions();
                rotation.deleteRetiredKeys();
            }

            boolean hasDeleted = events.stream()
                    .anyMatch(e -> e.type() == KeyRotation.RotationEvent.Type.KEY_DELETED);

            assertThat(hasDeleted).isTrue();
        }

        @Test
        @DisplayName("监听器异常不影响轮换")
        void testListenerExceptionDoesNotAffectRotation() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .onRotation(event -> { throw new RuntimeException("Test exception"); })
                    .build()) {

                // 尽管监听器抛出异常，轮换应该正常完成
                assertThat(rotation.getCurrentVersion()).isEqualTo(1);

                rotation.rotate();
                assertThat(rotation.getCurrentVersion()).isEqualTo(2);
            }
        }
    }

    @Nested
    @DisplayName("VersionedKey 测试")
    class VersionedKeyTests {

        @Test
        @DisplayName("withStatus创建新状态的副本")
        void testWithStatus() {
            try (KeyRotation<SecretKey> rotation = KeyRotation.forAes("test", 256)) {
                KeyRotation.VersionedKey<SecretKey> original = rotation.getCurrentKey();
                KeyRotation.VersionedKey<SecretKey> updated = original.withStatus(KeyRotation.KeyStatus.DEPRECATED);

                assertThat(updated.status()).isEqualTo(KeyRotation.KeyStatus.DEPRECATED);
                assertThat(updated.key()).isEqualTo(original.key());
                assertThat(updated.version()).isEqualTo(original.version());
                assertThat(updated.keyId()).isEqualTo(original.keyId());
                assertThat(updated.createdAt()).isEqualTo(original.createdAt());
            }
        }
    }

    @Nested
    @DisplayName("RotationEvent 测试")
    class RotationEventTests {

        @Test
        @DisplayName("RotationEvent包含正确信息")
        void testRotationEventFields() {
            List<KeyRotation.RotationEvent> events = new ArrayList<>();

            try (KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test-key-id")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .onRotation(events::add)
                    .build()) {
                // 仅触发初始轮换
            }

            KeyRotation.RotationEvent event = events.getFirst();
            assertThat(event.keyId()).isEqualTo("test-key-id");
            assertThat(event.type()).isEqualTo(KeyRotation.RotationEvent.Type.KEY_ROTATED);
            assertThat(event.version()).isEqualTo(1);
            assertThat(event.timestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("KeyStatus 测试")
    class KeyStatusTests {

        @Test
        @DisplayName("KeyStatus枚举值")
        void testKeyStatusValues() {
            assertThat(KeyRotation.KeyStatus.values())
                    .containsExactly(
                            KeyRotation.KeyStatus.ACTIVE,
                            KeyRotation.KeyStatus.DEPRECATED,
                            KeyRotation.KeyStatus.RETIRED
                    );
        }
    }

    @Nested
    @DisplayName("生命周期测试")
    class LifecycleTests {

        @Test
        @DisplayName("close停止自动轮换并清理")
        void testClose() {
            KeyRotation<SecretKey> rotation = KeyRotation.forAes("test", 256);
            rotation.startAutoRotation();

            rotation.close();

            // close后应该清空版本
            assertThat(rotation.getAvailableVersions()).isEmpty();
        }
    }

    @Nested
    @DisplayName("并发安全测试")
    class ConcurrencySafetyTests {

        @Test
        @DisplayName("并发轮换是线程安全的")
        void testConcurrentRotation() throws InterruptedException {
            try (KeyRotation<SecretKey> rotation = KeyRotation.<SecretKey>builder()
                    .keyId("test")
                    .keyGenerator(() -> KeyGenerator.generateAesKey(256))
                    .maxVersions(100)
                    .build()) {

                int threadCount = 10;
                CountDownLatch latch = new CountDownLatch(threadCount);

                for (int i = 0; i < threadCount; i++) {
                    new Thread(() -> {
                        try {
                            for (int j = 0; j < 5; j++) {
                                rotation.rotate();
                            }
                        } finally {
                            latch.countDown();
                        }
                    }).start();
                }

                latch.await(10, TimeUnit.SECONDS);

                // 应该有 1 (初始) + 10*5 = 51 个版本，但maxVersions限制为100
                assertThat(rotation.getCurrentVersion()).isEqualTo(51);
            }
        }

        @Test
        @DisplayName("并发读取是安全的")
        void testConcurrentRead() throws InterruptedException {
            try (KeyRotation<SecretKey> rotation = KeyRotation.forAes("test", 256)) {
                rotation.rotate();
                rotation.rotate();

                int threadCount = 10;
                CountDownLatch latch = new CountDownLatch(threadCount);
                AtomicInteger errorCount = new AtomicInteger(0);

                for (int i = 0; i < threadCount; i++) {
                    new Thread(() -> {
                        try {
                            for (int j = 0; j < 100; j++) {
                                rotation.getCurrentKey();
                                rotation.getAvailableVersions();
                                rotation.getCurrentVersion();
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        } finally {
                            latch.countDown();
                        }
                    }).start();
                }

                latch.await(10, TimeUnit.SECONDS);

                assertThat(errorCount.get()).isZero();
            }
        }
    }
}
