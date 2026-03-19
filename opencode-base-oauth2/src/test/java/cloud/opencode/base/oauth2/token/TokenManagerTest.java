package cloud.opencode.base.oauth2.token;

import cloud.opencode.base.oauth2.OAuth2Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

/**
 * TokenManagerTest Tests
 * TokenManagerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("TokenManager 测试")
class TokenManagerTest {

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodsTests {

        @Test
        @DisplayName("所有方法在接口中声明")
        void testInterfaceMethods() {
            // 验证接口包含所有必需的方法
            assertThat(TokenManager.class.isInterface()).isTrue();

            assertThatCode(() -> {
                TokenManager.class.getMethod("store", String.class, OAuth2Token.class);
                TokenManager.class.getMethod("get", String.class);
                TokenManager.class.getMethod("getValidToken", String.class);
                TokenManager.class.getMethod("getOrObtain", String.class, Supplier.class);
                TokenManager.class.getMethod("exists", String.class);
                TokenManager.class.getMethod("hasValidToken", String.class);
                TokenManager.class.getMethod("remove", String.class);
                TokenManager.class.getMethod("clear");
                TokenManager.class.getMethod("size");
                TokenManager.class.getMethod("close");
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("继承AutoCloseable")
        void testExtendsAutoCloseable() {
            assertThat(AutoCloseable.class.isAssignableFrom(TokenManager.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("模拟实现测试")
    class MockImplementationTests {

        @Test
        @DisplayName("基本操作")
        void testBasicOperations() {
            TokenManager manager = createMockTokenManager();

            OAuth2Token token = createToken();
            manager.store("user1", token);

            assertThat(manager.exists("user1")).isTrue();
            assertThat(manager.get("user1")).isPresent();
            assertThat(manager.size()).isEqualTo(1);

            manager.remove("user1");
            assertThat(manager.exists("user1")).isFalse();
        }

        @Test
        @DisplayName("getOrObtain - 已存在token")
        void testGetOrObtainExisting() {
            TokenManager manager = createMockTokenManager();
            OAuth2Token existingToken = createToken();
            manager.store("user1", existingToken);

            OAuth2Token result = manager.getOrObtain("user1", () -> {
                throw new RuntimeException("Should not be called");
            });

            assertThat(result).isEqualTo(existingToken);
        }

        @Test
        @DisplayName("clear清除所有token")
        void testClear() {
            TokenManager manager = createMockTokenManager();
            manager.store("user1", createToken());
            manager.store("user2", createToken());

            manager.clear();

            assertThat(manager.size()).isZero();
        }
    }

    private OAuth2Token createToken() {
        return OAuth2Token.builder()
                .accessToken("access123")
                .expiresIn(3600)
                .build();
    }

    private TokenManager createMockTokenManager() {
        return new TokenManager() {
            private final java.util.Map<String, OAuth2Token> tokens = new java.util.concurrent.ConcurrentHashMap<>();

            @Override
            public void store(String key, OAuth2Token token) {
                tokens.put(key, token);
            }

            @Override
            public Optional<OAuth2Token> get(String key) {
                return Optional.ofNullable(tokens.get(key));
            }

            @Override
            public OAuth2Token getValidToken(String key) {
                return tokens.get(key);
            }

            @Override
            public OAuth2Token getOrObtain(String key, Supplier<OAuth2Token> tokenSupplier) {
                return tokens.computeIfAbsent(key, k -> tokenSupplier.get());
            }

            @Override
            public boolean exists(String key) {
                return tokens.containsKey(key);
            }

            @Override
            public boolean hasValidToken(String key) {
                OAuth2Token token = tokens.get(key);
                return token != null && !token.isExpired();
            }

            @Override
            public void remove(String key) {
                tokens.remove(key);
            }

            @Override
            public void clear() {
                tokens.clear();
            }

            @Override
            public int size() {
                return tokens.size();
            }

            @Override
            public void close() {
                tokens.clear();
            }
        };
    }
}
