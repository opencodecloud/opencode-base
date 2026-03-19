package cloud.opencode.base.oauth2.token;

import cloud.opencode.base.oauth2.OAuth2Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * InMemoryTokenStoreTest Tests
 * InMemoryTokenStoreTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("InMemoryTokenStore 测试")
class InMemoryTokenStoreTest {

    private InMemoryTokenStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryTokenStore();
    }

    @Nested
    @DisplayName("save方法测试")
    class SaveTests {

        @Test
        @DisplayName("save存储token")
        void testSave() {
            OAuth2Token token = createToken("access123");
            store.save("user1", token);

            assertThat(store.exists("user1")).isTrue();
        }

        @Test
        @DisplayName("save覆盖已存在的token")
        void testSaveOverwrite() {
            OAuth2Token token1 = createToken("access1");
            OAuth2Token token2 = createToken("access2");

            store.save("user1", token1);
            store.save("user1", token2);

            Optional<OAuth2Token> loaded = store.load("user1");
            assertThat(loaded).isPresent();
            assertThat(loaded.get().accessToken()).isEqualTo("access2");
        }

        @Test
        @DisplayName("save null key抛出异常")
        void testSaveNullKey() {
            assertThatThrownBy(() -> store.save(null, createToken("access")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("save null token抛出异常")
        void testSaveNullToken() {
            assertThatThrownBy(() -> store.save("key", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("load方法测试")
    class LoadTests {

        @Test
        @DisplayName("load返回存储的token")
        void testLoad() {
            OAuth2Token token = createToken("access123");
            store.save("user1", token);

            Optional<OAuth2Token> loaded = store.load("user1");
            assertThat(loaded).isPresent();
            assertThat(loaded.get().accessToken()).isEqualTo("access123");
        }

        @Test
        @DisplayName("load不存在的key返回empty")
        void testLoadNotFound() {
            Optional<OAuth2Token> loaded = store.load("nonexistent");
            assertThat(loaded).isEmpty();
        }

        @Test
        @DisplayName("load null key返回empty")
        void testLoadNullKey() {
            Optional<OAuth2Token> loaded = store.load(null);
            assertThat(loaded).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete方法测试")
    class DeleteTests {

        @Test
        @DisplayName("delete移除token")
        void testDelete() {
            store.save("user1", createToken("access"));
            store.delete("user1");

            assertThat(store.exists("user1")).isFalse();
        }

        @Test
        @DisplayName("delete不存在的key不抛异常")
        void testDeleteNonexistent() {
            assertThatCode(() -> store.delete("nonexistent"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("delete null key不抛异常")
        void testDeleteNullKey() {
            assertThatCode(() -> store.delete(null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("deleteAll方法测试")
    class DeleteAllTests {

        @Test
        @DisplayName("deleteAll清除所有token")
        void testDeleteAll() {
            store.save("user1", createToken("access1"));
            store.save("user2", createToken("access2"));
            store.save("user3", createToken("access3"));

            store.deleteAll();

            assertThat(store.size()).isZero();
            assertThat(store.keys()).isEmpty();
        }
    }

    @Nested
    @DisplayName("exists方法测试")
    class ExistsTests {

        @Test
        @DisplayName("exists - 存在")
        void testExistsTrue() {
            store.save("user1", createToken("access"));
            assertThat(store.exists("user1")).isTrue();
        }

        @Test
        @DisplayName("exists - 不存在")
        void testExistsFalse() {
            assertThat(store.exists("nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("keys方法测试")
    class KeysTests {

        @Test
        @DisplayName("keys返回所有键")
        void testKeys() {
            store.save("user1", createToken("access1"));
            store.save("user2", createToken("access2"));

            Set<String> keys = store.keys();
            assertThat(keys).containsExactlyInAnyOrder("user1", "user2");
        }

        @Test
        @DisplayName("keys空store返回空集合")
        void testKeysEmpty() {
            Set<String> keys = store.keys();
            assertThat(keys).isEmpty();
        }
    }

    @Nested
    @DisplayName("size方法测试")
    class SizeTests {

        @Test
        @DisplayName("size返回正确数量")
        void testSize() {
            assertThat(store.size()).isZero();

            store.save("user1", createToken("access1"));
            assertThat(store.size()).isEqualTo(1);

            store.save("user2", createToken("access2"));
            assertThat(store.size()).isEqualTo(2);

            store.delete("user1");
            assertThat(store.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("removeExpired方法测试")
    class RemoveExpiredTests {

        @Test
        @DisplayName("removeExpired移除过期的token")
        void testRemoveExpired() {
            OAuth2Token expiredToken = OAuth2Token.builder()
                    .accessToken("expired")
                    .expiresAt(Instant.now().minusSeconds(60))
                    .build();
            OAuth2Token validToken = OAuth2Token.builder()
                    .accessToken("valid")
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            store.save("expired", expiredToken);
            store.save("valid", validToken);

            int removed = store.removeExpired();

            assertThat(removed).isEqualTo(1);
            assertThat(store.exists("expired")).isFalse();
            assertThat(store.exists("valid")).isTrue();
        }

        @Test
        @DisplayName("removeExpired返回移除的数量")
        void testRemoveExpiredCount() {
            OAuth2Token expired1 = OAuth2Token.builder()
                    .accessToken("expired1")
                    .expiresAt(Instant.now().minusSeconds(60))
                    .build();
            OAuth2Token expired2 = OAuth2Token.builder()
                    .accessToken("expired2")
                    .expiresAt(Instant.now().minusSeconds(120))
                    .build();

            store.save("expired1", expired1);
            store.save("expired2", expired2);

            int removed = store.removeExpired();
            assertThat(removed).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("线程安全测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("并发操作安全")
        void testConcurrentOperations() throws InterruptedException {
            int threadCount = 10;
            int operationsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            String key = "thread" + threadId + "-key" + j;
                            store.save(key, createToken("access" + j));
                            store.load(key);
                            if (j % 2 == 0) {
                                store.delete(key);
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            // 验证没有异常发生，store仍然可用
            assertThatCode(() -> {
                store.save("final", createToken("final"));
                store.load("final");
                store.keys();
                store.size();
            }).doesNotThrowAnyException();
        }
    }

    private OAuth2Token createToken(String accessToken) {
        return OAuth2Token.builder()
                .accessToken(accessToken)
                .build();
    }
}
