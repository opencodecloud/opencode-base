package cloud.opencode.base.oauth2.token;

import cloud.opencode.base.oauth2.OAuth2Config;
import cloud.opencode.base.oauth2.OAuth2Token;
import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import cloud.opencode.base.oauth2.http.OAuth2HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultTokenManager Tests
 * DefaultTokenManager 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
@DisplayName("DefaultTokenManager 测试")
class DefaultTokenManagerTest {

    private InMemoryTokenStore tokenStore;
    private DefaultTokenManager manager;

    /**
     * Stub TokenRefresher that counts refresh calls and returns a configured token.
     * Uses a real OAuth2Config and OAuth2HttpClient (unused for actual HTTP).
     */
    private static final class StubTokenRefresher extends TokenRefresher {
        private final AtomicInteger refreshCount = new AtomicInteger(0);
        private volatile OAuth2Token tokenToReturn;
        private volatile OAuth2Exception exceptionToThrow;
        private volatile long delayMillis;

        StubTokenRefresher() {
            super(OAuth2Config.builder()
                            .clientId("test-client")
                            .clientSecret("test-secret")
                            .tokenEndpoint("https://auth.example.com/token")
                            .build(),
                    new OAuth2HttpClient(),
                    Duration.ofMinutes(5));
        }

        void setTokenToReturn(OAuth2Token token) {
            this.tokenToReturn = token;
        }

        void setExceptionToThrow(OAuth2Exception ex) {
            this.exceptionToThrow = ex;
        }

        void setDelayMillis(long millis) {
            this.delayMillis = millis;
        }

        int getRefreshCount() {
            return refreshCount.get();
        }

        @Override
        public boolean needsRefresh(OAuth2Token token) {
            if (token == null) return false;
            return token.isExpiringSoon(Duration.ofMinutes(5)) && token.hasRefreshToken();
        }

        @Override
        public OAuth2Token refresh(OAuth2Token token) {
            refreshCount.incrementAndGet();
            if (delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
            return tokenToReturn != null ? tokenToReturn : token;
        }
    }

    private static OAuth2Token createValidToken() {
        return OAuth2Token.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-456")
                .expiresAt(Instant.now().plus(Duration.ofHours(1)))
                .build();
    }

    private static OAuth2Token createExpiredToken() {
        return OAuth2Token.builder()
                .accessToken("expired-token")
                .refreshToken("refresh-token-789")
                .expiresAt(Instant.now().minus(Duration.ofHours(1)))
                .build();
    }

    private static OAuth2Token createExpiringSoonToken() {
        return OAuth2Token.builder()
                .accessToken("expiring-soon-token")
                .refreshToken("refresh-token-abc")
                .expiresAt(Instant.now().plus(Duration.ofMinutes(2)))
                .build();
    }

    @BeforeEach
    void setUp() {
        tokenStore = new InMemoryTokenStore();
    }

    @AfterEach
    void tearDown() {
        if (manager != null) {
            manager.close();
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("使用所有参数构建")
        void testBuildFull() {
            StubTokenRefresher refresher = new StubTokenRefresher();
            manager = DefaultTokenManager.builder()
                    .tokenStore(tokenStore)
                    .refresher(refresher)
                    .refreshThreshold(Duration.ofMinutes(10))
                    .build();

            assertThat(manager).isNotNull();
        }

        @Test
        @DisplayName("仅使用 tokenStore 构建（无 refresher）")
        void testBuildWithoutRefresher() {
            manager = DefaultTokenManager.builder()
                    .tokenStore(tokenStore)
                    .build();

            assertThat(manager).isNotNull();
        }

        @Test
        @DisplayName("tokenStore 为 null 抛出异常")
        void testBuildNullTokenStore() {
            assertThatThrownBy(() -> DefaultTokenManager.builder().build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("tokenStore");
        }
    }

    @Nested
    @DisplayName("store() 方法测试")
    class StoreTests {

        @Test
        @DisplayName("store 存储 Token")
        void testStore() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            OAuth2Token token = createValidToken();
            manager.store("user-1", token);

            Optional<OAuth2Token> loaded = manager.get("user-1");
            assertThat(loaded).isPresent();
            assertThat(loaded.get().accessToken()).isEqualTo("access-token-123");
        }

        @Test
        @DisplayName("store null key 抛出异常")
        void testStoreNullKey() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            assertThatThrownBy(() -> manager.store(null, createValidToken()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("store null token 抛出异常")
        void testStoreNullToken() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            assertThatThrownBy(() -> manager.store("key", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("get() 方法测试")
    class GetTests {

        @Test
        @DisplayName("get 返回存储的 Token")
        void testGet() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            OAuth2Token token = createValidToken();
            manager.store("user-1", token);

            Optional<OAuth2Token> result = manager.get("user-1");
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("get 不存在的 key 返回空")
        void testGetNotFound() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            Optional<OAuth2Token> result = manager.get("nonexistent");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("get null key 抛出异常")
        void testGetNullKey() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            assertThatThrownBy(() -> manager.get(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getValidToken() 方法测试")
    class GetValidTokenTests {

        @Test
        @DisplayName("getValidToken 返回有效 Token")
        void testGetValidToken() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            OAuth2Token token = createValidToken();
            manager.store("user-1", token);

            OAuth2Token result = manager.getValidToken("user-1");
            assertThat(result.accessToken()).isEqualTo("access-token-123");
        }

        @Test
        @DisplayName("getValidToken Token 不存在抛出 TOKEN_NOT_FOUND")
        void testGetValidTokenNotFound() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();

            assertThatThrownBy(() -> manager.getValidToken("nonexistent"))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(e -> {
                        OAuth2Exception oae = (OAuth2Exception) e;
                        assertThat(oae.errorCode()).isEqualTo(OAuth2ErrorCode.TOKEN_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("getValidToken 过期且无 refresh token 抛出 TOKEN_EXPIRED")
        void testGetValidTokenExpiredNoRefresh() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("expired")
                    .expiresAt(Instant.now().minus(Duration.ofHours(1)))
                    .build();
            manager.store("user-1", token);

            assertThatThrownBy(() -> manager.getValidToken("user-1"))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(e -> {
                        OAuth2Exception oae = (OAuth2Exception) e;
                        assertThat(oae.errorCode()).isEqualTo(OAuth2ErrorCode.TOKEN_EXPIRED);
                    });
        }

        @Test
        @DisplayName("getValidToken 即将过期时自动刷新")
        void testGetValidTokenRefresh() {
            StubTokenRefresher refresher = new StubTokenRefresher();
            OAuth2Token refreshedToken = OAuth2Token.builder()
                    .accessToken("refreshed-token")
                    .expiresAt(Instant.now().plus(Duration.ofHours(1)))
                    .build();
            refresher.setTokenToReturn(refreshedToken);

            manager = DefaultTokenManager.builder()
                    .tokenStore(tokenStore)
                    .refresher(refresher)
                    .refreshThreshold(Duration.ofMinutes(5))
                    .build();

            manager.store("user-1", createExpiringSoonToken());

            OAuth2Token result = manager.getValidToken("user-1");
            assertThat(result.accessToken()).isEqualTo("refreshed-token");
            assertThat(refresher.getRefreshCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("getValidToken 刷新失败抛出 TOKEN_REFRESH_FAILED")
        void testGetValidTokenRefreshFailed() {
            StubTokenRefresher refresher = new StubTokenRefresher();
            refresher.setExceptionToThrow(
                    new OAuth2Exception(OAuth2ErrorCode.TOKEN_REFRESH_FAILED, "Refresh denied"));

            manager = DefaultTokenManager.builder()
                    .tokenStore(tokenStore)
                    .refresher(refresher)
                    .refreshThreshold(Duration.ofMinutes(5))
                    .build();

            manager.store("user-1", createExpiringSoonToken());

            assertThatThrownBy(() -> manager.getValidToken("user-1"))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(e -> {
                        OAuth2Exception oae = (OAuth2Exception) e;
                        assertThat(oae.errorCode()).isEqualTo(OAuth2ErrorCode.TOKEN_REFRESH_FAILED);
                    });
        }

        @Test
        @DisplayName("getValidToken 无 refresher 且 Token 有效时返回 Token")
        void testGetValidTokenNoRefresherValidToken() {
            manager = DefaultTokenManager.builder()
                    .tokenStore(tokenStore)
                    .build();

            manager.store("user-1", createValidToken());

            OAuth2Token result = manager.getValidToken("user-1");
            assertThat(result.accessToken()).isEqualTo("access-token-123");
        }

        @Test
        @DisplayName("getValidToken 无 refresher 且 Token 过期抛出 TOKEN_EXPIRED")
        void testGetValidTokenNoRefresherExpired() {
            manager = DefaultTokenManager.builder()
                    .tokenStore(tokenStore)
                    .build();

            manager.store("user-1", createExpiredToken());

            assertThatThrownBy(() -> manager.getValidToken("user-1"))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(e -> {
                        OAuth2Exception oae = (OAuth2Exception) e;
                        assertThat(oae.errorCode()).isEqualTo(OAuth2ErrorCode.TOKEN_EXPIRED);
                    });
        }
    }

    @Nested
    @DisplayName("getOrObtain() 方法测试")
    class GetOrObtainTests {

        @Test
        @DisplayName("getOrObtain 已有有效 Token 时返回现有 Token")
        void testGetOrObtainExisting() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            OAuth2Token token = createValidToken();
            manager.store("user-1", token);

            AtomicInteger supplierCalls = new AtomicInteger(0);
            OAuth2Token result = manager.getOrObtain("user-1", () -> {
                supplierCalls.incrementAndGet();
                return createValidToken();
            });

            assertThat(result.accessToken()).isEqualTo("access-token-123");
            assertThat(supplierCalls.get()).isZero();
        }

        @Test
        @DisplayName("getOrObtain 不存在时调用 supplier")
        void testGetOrObtainNew() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            OAuth2Token newToken = createValidToken();

            OAuth2Token result = manager.getOrObtain("user-1", () -> newToken);

            assertThat(result.accessToken()).isEqualTo("access-token-123");
            assertThat(manager.exists("user-1")).isTrue();
        }

        @Test
        @DisplayName("getOrObtain null key 抛出异常")
        void testGetOrObtainNullKey() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            assertThatThrownBy(() -> manager.getOrObtain(null, () -> createValidToken()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getOrObtain null supplier 抛出异常")
        void testGetOrObtainNullSupplier() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            assertThatThrownBy(() -> manager.getOrObtain("key", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getOrObtain supplier 返回 null 抛出异常")
        void testGetOrObtainSupplierReturnsNull() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            assertThatThrownBy(() -> manager.getOrObtain("key", () -> null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("exists() 方法测试")
    class ExistsTests {

        @Test
        @DisplayName("exists 已存储返回 true")
        void testExistsTrue() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            manager.store("user-1", createValidToken());
            assertThat(manager.exists("user-1")).isTrue();
        }

        @Test
        @DisplayName("exists 未存储返回 false")
        void testExistsFalse() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            assertThat(manager.exists("nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("hasValidToken() 方法测试")
    class HasValidTokenTests {

        @Test
        @DisplayName("hasValidToken 有效 Token 返回 true")
        void testHasValidTokenTrue() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            manager.store("user-1", createValidToken());
            assertThat(manager.hasValidToken("user-1")).isTrue();
        }

        @Test
        @DisplayName("hasValidToken 过期 Token 返回 false")
        void testHasValidTokenExpired() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            manager.store("user-1", createExpiredToken());
            assertThat(manager.hasValidToken("user-1")).isFalse();
        }

        @Test
        @DisplayName("hasValidToken 不存在返回 false")
        void testHasValidTokenNotFound() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            assertThat(manager.hasValidToken("nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("remove() 方法测试")
    class RemoveTests {

        @Test
        @DisplayName("remove 删除已存储的 Token")
        void testRemove() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            manager.store("user-1", createValidToken());
            manager.remove("user-1");

            assertThat(manager.exists("user-1")).isFalse();
        }

        @Test
        @DisplayName("remove 不存在的 key 不抛出异常")
        void testRemoveNonexistent() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            assertThatCode(() -> manager.remove("nonexistent")).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("clear() 方法测试")
    class ClearTests {

        @Test
        @DisplayName("clear 删除所有 Token")
        void testClear() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            manager.store("user-1", createValidToken());
            manager.store("user-2", createValidToken());
            manager.clear();

            assertThat(manager.size()).isZero();
        }
    }

    @Nested
    @DisplayName("size() 方法测试")
    class SizeTests {

        @Test
        @DisplayName("size 返回正确的数量")
        void testSize() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            assertThat(manager.size()).isZero();

            manager.store("user-1", createValidToken());
            assertThat(manager.size()).isEqualTo(1);

            manager.store("user-2", createValidToken());
            assertThat(manager.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("close() 方法测试")
    class CloseTests {

        @Test
        @DisplayName("close 后操作抛出 IllegalStateException")
        void testClosePreventsFurtherOps() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            manager.close();

            assertThatThrownBy(() -> manager.store("key", createValidToken()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("close 多次调用不抛异常")
        void testCloseIdempotent() {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            assertThatCode(() -> {
                manager.close();
                manager.close();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("close 关闭 owned refresher")
        void testCloseOwnsRefresher() {
            StubTokenRefresher refresher = new StubTokenRefresher();
            assertThat(refresher.isClosed()).isFalse();

            manager = DefaultTokenManager.builder()
                    .tokenStore(tokenStore)
                    .ownedRefresher(refresher)
                    .build();

            manager.close();

            assertThat(refresher.isClosed()).isTrue();
        }

        @Test
        @DisplayName("close 不关闭外部 refresher")
        void testCloseDoesNotCloseExternalRefresher() {
            StubTokenRefresher refresher = new StubTokenRefresher();
            assertThat(refresher.isClosed()).isFalse();

            manager = DefaultTokenManager.builder()
                    .tokenStore(tokenStore)
                    .refresher(refresher)
                    .build();

            manager.close();

            assertThat(refresher.isClosed()).isFalse();
            refresher.close(); // clean up manually
        }
    }

    @Nested
    @DisplayName("线程安全测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("并发 getValidToken 只触发一次刷新")
        void testConcurrentRefreshDeduplication() throws Exception {
            StubTokenRefresher refresher = new StubTokenRefresher();
            OAuth2Token refreshedToken = OAuth2Token.builder()
                    .accessToken("refreshed-token")
                    .expiresAt(Instant.now().plus(Duration.ofHours(1)))
                    .build();
            refresher.setTokenToReturn(refreshedToken);
            refresher.setDelayMillis(100);

            manager = DefaultTokenManager.builder()
                    .tokenStore(tokenStore)
                    .refresher(refresher)
                    .refreshThreshold(Duration.ofMinutes(5))
                    .build();

            manager.store("user-1", createExpiringSoonToken());

            int threadCount = 10;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        manager.getValidToken("user-1");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            doneLatch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            // Only one refresh should have occurred due to per-key locking
            assertThat(refresher.getRefreshCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("并发 store 和 get 不出错")
        void testConcurrentStoreAndGet() throws Exception {
            manager = DefaultTokenManager.builder().tokenStore(tokenStore).build();
            int threadCount = 20;
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final int idx = i;
                executor.submit(() -> {
                    try {
                        String key = "user-" + idx;
                        manager.store(key, createValidToken());
                        manager.get(key);
                        manager.exists(key);
                        manager.hasValidToken(key);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(manager.size()).isEqualTo(threadCount);
        }
    }
}
