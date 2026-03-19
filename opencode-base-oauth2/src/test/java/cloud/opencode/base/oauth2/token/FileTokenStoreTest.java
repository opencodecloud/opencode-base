package cloud.opencode.base.oauth2.token;

import cloud.opencode.base.oauth2.OAuth2Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * FileTokenStoreTest Tests
 * FileTokenStoreTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("FileTokenStore 测试")
class FileTokenStoreTest {

    @TempDir
    Path tempDir;

    private FileTokenStore store;

    @BeforeEach
    void setUp() {
        store = new FileTokenStore(tempDir);
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建store并初始化目录")
        void testConstructorCreatesDirectory() {
            Path newDir = tempDir.resolve("tokens");
            FileTokenStore newStore = new FileTokenStore(newDir);

            assertThat(newDir).exists();
            assertThat(newDir).isDirectory();
        }
    }

    @Nested
    @DisplayName("save方法测试")
    class SaveTests {

        @Test
        @DisplayName("save存储token到文件")
        void testSave() {
            OAuth2Token token = createToken("access123", "refresh456");
            store.save("user1", token);

            assertThat(store.exists("user1")).isTrue();
        }

        @Test
        @DisplayName("save所有字段")
        void testSaveAllFields() {
            Instant now = Instant.now();
            Instant expires = now.plusSeconds(3600);

            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .tokenType("Bearer")
                    .refreshToken("refresh456")
                    .idToken("id789")
                    .scopeString("openid profile")
                    .issuedAt(now)
                    .expiresAt(expires)
                    .build();

            store.save("user1", token);
            Optional<OAuth2Token> loaded = store.load("user1");

            assertThat(loaded).isPresent();
            OAuth2Token loadedToken = loaded.get();
            assertThat(loadedToken.accessToken()).isEqualTo("access123");
            assertThat(loadedToken.tokenType()).isEqualTo("Bearer");
            assertThat(loadedToken.refreshToken()).isEqualTo("refresh456");
            assertThat(loadedToken.idToken()).isEqualTo("id789");
            assertThat(loadedToken.scopes()).contains("openid", "profile");
        }

        @Test
        @DisplayName("save null key抛出异常")
        void testSaveNullKey() {
            assertThatThrownBy(() -> store.save(null, createToken("access", null)))
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
            OAuth2Token token = createToken("access123", "refresh456");
            store.save("user1", token);

            Optional<OAuth2Token> loaded = store.load("user1");

            assertThat(loaded).isPresent();
            assertThat(loaded.get().accessToken()).isEqualTo("access123");
            assertThat(loaded.get().refreshToken()).isEqualTo("refresh456");
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
        @DisplayName("delete删除token文件")
        void testDelete() {
            store.save("user1", createToken("access", null));
            store.delete("user1");

            assertThat(store.exists("user1")).isFalse();
            assertThat(store.load("user1")).isEmpty();
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
        @DisplayName("deleteAll删除所有token文件")
        void testDeleteAll() {
            store.save("user1", createToken("access1", null));
            store.save("user2", createToken("access2", null));
            store.save("user3", createToken("access3", null));

            store.deleteAll();

            assertThat(store.keys()).isEmpty();
        }
    }

    @Nested
    @DisplayName("keys方法测试")
    class KeysTests {

        @Test
        @DisplayName("keys返回所有存储的键")
        void testKeys() {
            store.save("user1", createToken("access1", null));
            store.save("user2", createToken("access2", null));

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
    @DisplayName("文件名清理测试")
    class SanitizeTests {

        @Test
        @DisplayName("特殊字符被替换")
        void testSanitizeSpecialCharacters() {
            store.save("user@example.com", createToken("access", null));

            // 应该能正常工作，key被清理
            assertThat(store.exists("user@example.com")).isTrue();
        }

        @Test
        @DisplayName("包含路径分隔符的key被清理")
        void testSanitizePathSeparators() {
            store.save("path/to/user", createToken("access", null));

            // 应该能正常工作，key被清理
            Set<String> keys = store.keys();
            assertThat(keys).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("持久化测试")
    class PersistenceTests {

        @Test
        @DisplayName("重新创建store后能加载token")
        void testPersistence() {
            store.save("user1", createToken("access123", "refresh456"));

            // 创建新的store实例
            FileTokenStore newStore = new FileTokenStore(tempDir);
            Optional<OAuth2Token> loaded = newStore.load("user1");

            assertThat(loaded).isPresent();
            assertThat(loaded.get().accessToken()).isEqualTo("access123");
        }
    }

    private OAuth2Token createToken(String accessToken, String refreshToken) {
        OAuth2Token.Builder builder = OAuth2Token.builder()
                .accessToken(accessToken)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));

        if (refreshToken != null) {
            builder.refreshToken(refreshToken);
        }

        return builder.build();
    }
}
