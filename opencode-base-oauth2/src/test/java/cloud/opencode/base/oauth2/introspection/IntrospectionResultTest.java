package cloud.opencode.base.oauth2.introspection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * IntrospectionResult Tests
 * IntrospectionResult 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
@DisplayName("IntrospectionResult 测试")
class IntrospectionResultTest {

    @Nested
    @DisplayName("构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("null claims被替换为空Map")
        void testNullClaimsReplacedWithEmptyMap() {
            IntrospectionResult result = new IntrospectionResult(
                    true, null, null, null, null,
                    null, null, null, null, null, null, null, null
            );

            assertThat(result.claims()).isNotNull();
            assertThat(result.claims()).isEmpty();
        }

        @Test
        @DisplayName("claims进行防御性拷贝")
        void testClaimsDefensivelyCopied() {
            var originalClaims = new java.util.HashMap<String, Object>();
            originalClaims.put("key", "value");

            IntrospectionResult result = new IntrospectionResult(
                    true, null, null, null, null,
                    null, null, null, null, null, null, null, originalClaims
            );

            // Modifying original should not affect the result
            originalClaims.put("key2", "value2");
            assertThat(result.claims()).doesNotContainKey("key2");

            // Result claims should be immutable
            assertThatThrownBy(() -> result.claims().put("new", "val"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("所有字段正确设置")
        void testAllFieldsSet() {
            Instant now = Instant.now();
            Map<String, Object> claims = Map.of("custom", "value");

            IntrospectionResult result = new IntrospectionResult(
                    true, "read write", "client1", "user1", "Bearer",
                    now.plusSeconds(3600), now, now, "sub1", "aud1",
                    "https://issuer.example.com", "jti123", claims
            );

            assertThat(result.active()).isTrue();
            assertThat(result.scope()).isEqualTo("read write");
            assertThat(result.clientId()).isEqualTo("client1");
            assertThat(result.username()).isEqualTo("user1");
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.exp()).isEqualTo(now.plusSeconds(3600));
            assertThat(result.iat()).isEqualTo(now);
            assertThat(result.nbf()).isEqualTo(now);
            assertThat(result.sub()).isEqualTo("sub1");
            assertThat(result.aud()).isEqualTo("aud1");
            assertThat(result.iss()).isEqualTo("https://issuer.example.com");
            assertThat(result.jti()).isEqualTo("jti123");
            assertThat(result.claims()).containsEntry("custom", "value");
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用Builder构建最小结果")
        void testBuildMinimal() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(false)
                    .build();

            assertThat(result.active()).isFalse();
            assertThat(result.scope()).isNull();
            assertThat(result.claims()).isEmpty();
        }

        @Test
        @DisplayName("使用Builder构建完整结果")
        void testBuildFull() {
            Instant now = Instant.now();

            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .scope("read write admin")
                    .clientId("my-client")
                    .username("john")
                    .tokenType("Bearer")
                    .exp(now.plusSeconds(7200))
                    .iat(now)
                    .nbf(now)
                    .sub("user-123")
                    .aud("https://api.example.com")
                    .iss("https://auth.example.com")
                    .jti("token-id-456")
                    .claims(Map.of("extra", "data"))
                    .build();

            assertThat(result.active()).isTrue();
            assertThat(result.scope()).isEqualTo("read write admin");
            assertThat(result.clientId()).isEqualTo("my-client");
            assertThat(result.username()).isEqualTo("john");
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.exp()).isEqualTo(now.plusSeconds(7200));
            assertThat(result.iat()).isEqualTo(now);
            assertThat(result.nbf()).isEqualTo(now);
            assertThat(result.sub()).isEqualTo("user-123");
            assertThat(result.aud()).isEqualTo("https://api.example.com");
            assertThat(result.iss()).isEqualTo("https://auth.example.com");
            assertThat(result.jti()).isEqualTo("token-id-456");
            assertThat(result.claims()).containsEntry("extra", "data");
        }
    }

    @Nested
    @DisplayName("isExpired方法测试")
    class IsExpiredTests {

        @Test
        @DisplayName("未过期的Token")
        void testNotExpired() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .exp(Instant.now().plusSeconds(3600))
                    .build();

            assertThat(result.isExpired()).isFalse();
        }

        @Test
        @DisplayName("已过期的Token")
        void testExpired() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .exp(Instant.now().minusSeconds(3600))
                    .build();

            assertThat(result.isExpired()).isTrue();
        }

        @Test
        @DisplayName("exp为null时不视为过期")
        void testNullExpNotExpired() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .build();

            assertThat(result.isExpired()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasScope方法测试")
    class HasScopeTests {

        @Test
        @DisplayName("包含指定scope")
        void testHasScope() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .scope("read write admin")
                    .build();

            assertThat(result.hasScope("read")).isTrue();
            assertThat(result.hasScope("write")).isTrue();
            assertThat(result.hasScope("admin")).isTrue();
        }

        @Test
        @DisplayName("不包含指定scope")
        void testDoesNotHaveScope() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .scope("read write")
                    .build();

            assertThat(result.hasScope("admin")).isFalse();
            assertThat(result.hasScope("delete")).isFalse();
        }

        @Test
        @DisplayName("scope为null时返回false")
        void testNullScope() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .build();

            assertThat(result.hasScope("read")).isFalse();
        }

        @Test
        @DisplayName("scope为空白时返回false")
        void testBlankScope() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .scope("  ")
                    .build();

            assertThat(result.hasScope("read")).isFalse();
        }

        @Test
        @DisplayName("null参数抛出NullPointerException")
        void testNullRequiredScope() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .scope("read")
                    .build();

            assertThatThrownBy(() -> result.hasScope(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("scopes方法测试")
    class ScopesTests {

        @Test
        @DisplayName("解析多个scope")
        void testMultipleScopes() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .scope("read write admin")
                    .build();

            Set<String> scopes = result.scopes();
            assertThat(scopes).containsExactlyInAnyOrder("read", "write", "admin");
        }

        @Test
        @DisplayName("解析单个scope")
        void testSingleScope() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .scope("read")
                    .build();

            assertThat(result.scopes()).containsExactly("read");
        }

        @Test
        @DisplayName("scope为null返回空集合")
        void testNullScopeReturnsEmptySet() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .build();

            assertThat(result.scopes()).isEmpty();
        }

        @Test
        @DisplayName("scope为空白返回空集合")
        void testBlankScopeReturnsEmptySet() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .scope("   ")
                    .build();

            assertThat(result.scopes()).isEmpty();
        }

        @Test
        @DisplayName("返回的集合不可修改")
        void testScopesUnmodifiable() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .scope("read write")
                    .build();

            assertThatThrownBy(() -> result.scopes().add("admin"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals和hashCode")
        void testEqualsAndHashCode() {
            Instant exp = Instant.ofEpochSecond(1700000000);

            IntrospectionResult r1 = IntrospectionResult.builder()
                    .active(true)
                    .scope("read")
                    .exp(exp)
                    .build();
            IntrospectionResult r2 = IntrospectionResult.builder()
                    .active(true)
                    .scope("read")
                    .exp(exp)
                    .build();

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("不同结果不相等")
        void testNotEqual() {
            IntrospectionResult r1 = IntrospectionResult.builder()
                    .active(true)
                    .build();
            IntrospectionResult r2 = IntrospectionResult.builder()
                    .active(false)
                    .build();

            assertThat(r1).isNotEqualTo(r2);
        }

        @Test
        @DisplayName("toString包含关键信息")
        void testToString() {
            IntrospectionResult result = IntrospectionResult.builder()
                    .active(true)
                    .scope("read")
                    .clientId("my-client")
                    .build();

            String str = result.toString();
            assertThat(str).contains("true");
            assertThat(str).contains("read");
            assertThat(str).contains("my-client");
        }
    }
}
