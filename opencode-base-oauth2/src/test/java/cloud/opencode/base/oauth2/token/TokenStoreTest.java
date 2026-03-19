package cloud.opencode.base.oauth2.token;

import cloud.opencode.base.oauth2.OAuth2Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * TokenStoreTest Tests
 * TokenStoreTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("TokenStore 测试")
class TokenStoreTest {

    @Nested
    @DisplayName("接口默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("exists默认实现")
        void testExistsDefault() {
            TokenStore store = new TestTokenStore();
            store.save("key1", createToken());

            assertThat(store.exists("key1")).isTrue();
            assertThat(store.exists("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("size默认实现")
        void testSizeDefault() {
            TokenStore store = new TestTokenStore();
            assertThat(store.size()).isZero();

            store.save("key1", createToken());
            assertThat(store.size()).isEqualTo(1);

            store.save("key2", createToken());
            assertThat(store.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("基本操作测试")
    class BasicOperationsTests {

        @Test
        @DisplayName("save和load")
        void testSaveAndLoad() {
            TokenStore store = new TestTokenStore();
            OAuth2Token token = createToken();

            store.save("user1", token);
            Optional<OAuth2Token> loaded = store.load("user1");

            assertThat(loaded).isPresent();
            assertThat(loaded.get().accessToken()).isEqualTo(token.accessToken());
        }

        @Test
        @DisplayName("delete")
        void testDelete() {
            TokenStore store = new TestTokenStore();
            store.save("user1", createToken());

            store.delete("user1");
            assertThat(store.load("user1")).isEmpty();
        }

        @Test
        @DisplayName("deleteAll")
        void testDeleteAll() {
            TokenStore store = new TestTokenStore();
            store.save("user1", createToken());
            store.save("user2", createToken());

            store.deleteAll();
            assertThat(store.keys()).isEmpty();
        }

        @Test
        @DisplayName("keys")
        void testKeys() {
            TokenStore store = new TestTokenStore();
            store.save("user1", createToken());
            store.save("user2", createToken());

            Set<String> keys = store.keys();
            assertThat(keys).containsExactlyInAnyOrder("user1", "user2");
        }
    }

    private OAuth2Token createToken() {
        return OAuth2Token.builder()
                .accessToken("access123")
                .build();
    }

    // 简单的测试实现
    private static class TestTokenStore implements TokenStore {
        private final java.util.Map<String, OAuth2Token> tokens = new java.util.concurrent.ConcurrentHashMap<>();

        @Override
        public void save(String key, OAuth2Token token) {
            tokens.put(key, token);
        }

        @Override
        public Optional<OAuth2Token> load(String key) {
            return Optional.ofNullable(tokens.get(key));
        }

        @Override
        public void delete(String key) {
            tokens.remove(key);
        }

        @Override
        public void deleteAll() {
            tokens.clear();
        }

        @Override
        public Set<String> keys() {
            return Set.copyOf(tokens.keySet());
        }
    }
}
