package cloud.opencode.base.crypto.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JwtClaims}.
 * JwtClaims单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("JwtClaims Tests / JwtClaims测试")
class JwtClaimsTest {

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("builder()创建构建器")
        void testBuilderFactory() {
            JwtClaims.Builder builder = JwtClaims.builder();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("of(Map)从映射创建")
        void testOfFactory() {
            Map<String, Object> map = Map.of("sub", "user123", "iss", "auth-service");
            JwtClaims claims = JwtClaims.of(map);

            assertThat(claims).isNotNull();
            assertThat(claims.subject()).isEqualTo("user123");
            assertThat(claims.issuer()).isEqualTo("auth-service");
        }

        @Test
        @DisplayName("empty()创建空声明")
        void testEmptyFactory() {
            JwtClaims claims = JwtClaims.empty();

            assertThat(claims).isNotNull();
            assertThat(claims.asMap()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Builder Tests / 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("issuer()设置签发者")
        void testIssuer() {
            JwtClaims claims = JwtClaims.builder()
                    .issuer("auth-service")
                    .build();

            assertThat(claims.issuer()).isEqualTo("auth-service");
        }

        @Test
        @DisplayName("subject()设置主题")
        void testSubject() {
            JwtClaims claims = JwtClaims.builder()
                    .subject("user123")
                    .build();

            assertThat(claims.subject()).isEqualTo("user123");
        }

        @Test
        @DisplayName("audience(String)设置单值受众")
        void testAudienceSingle() {
            JwtClaims claims = JwtClaims.builder()
                    .audience("api-service")
                    .build();

            assertThat(claims.audience()).containsExactly("api-service");
        }

        @Test
        @DisplayName("audience(List)设置多值受众")
        void testAudienceMultiple() {
            JwtClaims claims = JwtClaims.builder()
                    .audience(List.of("api1", "api2", "api3"))
                    .build();

            assertThat(claims.audience()).containsExactly("api1", "api2", "api3");
        }

        @Test
        @DisplayName("expiration()设置过期时间")
        void testExpiration() {
            Instant exp = Instant.now().plusSeconds(3600);
            JwtClaims claims = JwtClaims.builder()
                    .expiration(exp)
                    .build();

            assertThat(claims.expiration()).isNotNull();
            // Compare epoch seconds since claims store epoch seconds
            assertThat(claims.expiration().getEpochSecond()).isEqualTo(exp.getEpochSecond());
        }

        @Test
        @DisplayName("expiresIn()设置相对过期时间")
        void testExpiresIn() {
            Instant before = Instant.now();
            JwtClaims claims = JwtClaims.builder()
                    .expiresIn(Duration.ofHours(1))
                    .build();
            Instant after = Instant.now();

            assertThat(claims.expiration()).isNotNull();
            assertThat(claims.expiration().getEpochSecond())
                    .isGreaterThanOrEqualTo(before.plusSeconds(3600).getEpochSecond());
            assertThat(claims.expiration().getEpochSecond())
                    .isLessThanOrEqualTo(after.plusSeconds(3600).getEpochSecond());
        }

        @Test
        @DisplayName("notBefore()设置生效时间")
        void testNotBefore() {
            Instant nbf = Instant.now().plusSeconds(300);
            JwtClaims claims = JwtClaims.builder()
                    .notBefore(nbf)
                    .build();

            assertThat(claims.notBefore()).isNotNull();
            assertThat(claims.notBefore().getEpochSecond()).isEqualTo(nbf.getEpochSecond());
        }

        @Test
        @DisplayName("issuedAt()设置签发时间")
        void testIssuedAt() {
            Instant iat = Instant.now();
            JwtClaims claims = JwtClaims.builder()
                    .issuedAt(iat)
                    .build();

            assertThat(claims.issuedAt()).isNotNull();
            assertThat(claims.issuedAt().getEpochSecond()).isEqualTo(iat.getEpochSecond());
        }

        @Test
        @DisplayName("issuedAtNow()设置当前签发时间")
        void testIssuedAtNow() {
            Instant before = Instant.now();
            JwtClaims claims = JwtClaims.builder()
                    .issuedAtNow()
                    .build();
            Instant after = Instant.now();

            assertThat(claims.issuedAt()).isNotNull();
            assertThat(claims.issuedAt().getEpochSecond())
                    .isGreaterThanOrEqualTo(before.getEpochSecond());
            assertThat(claims.issuedAt().getEpochSecond())
                    .isLessThanOrEqualTo(after.getEpochSecond());
        }

        @Test
        @DisplayName("jwtId()设置JWT ID")
        void testJwtId() {
            JwtClaims claims = JwtClaims.builder()
                    .jwtId("unique-id-123")
                    .build();

            assertThat(claims.jwtId()).isEqualTo("unique-id-123");
        }

        @Test
        @DisplayName("generateJwtId()生成随机JWT ID")
        void testGenerateJwtId() {
            JwtClaims claims1 = JwtClaims.builder().generateJwtId().build();
            JwtClaims claims2 = JwtClaims.builder().generateJwtId().build();

            assertThat(claims1.jwtId()).isNotNull().isNotEmpty();
            assertThat(claims2.jwtId()).isNotNull().isNotEmpty();
            assertThat(claims1.jwtId()).isNotEqualTo(claims2.jwtId());
        }

        @Test
        @DisplayName("claim()设置自定义声明")
        void testCustomClaim() {
            JwtClaims claims = JwtClaims.builder()
                    .claim("role", "admin")
                    .claim("permissions", List.of("read", "write"))
                    .build();

            assertThat(claims.getString("role")).isEqualTo("admin");
            assertThat(claims.<String>getList("permissions")).containsExactly("read", "write");
        }

        @Test
        @DisplayName("claims(Map)批量设置声明")
        void testClaimsMap() {
            Map<String, Object> customClaims = Map.of(
                    "role", "user",
                    "level", 5
            );
            JwtClaims claims = JwtClaims.builder()
                    .claims(customClaims)
                    .build();

            assertThat(claims.getString("role")).isEqualTo("user");
            assertThat(claims.getInt("level")).isEqualTo(5);
        }

        @Test
        @DisplayName("链式调用构建完整声明")
        void testChainedBuilder() {
            JwtClaims claims = JwtClaims.builder()
                    .issuer("auth-service")
                    .subject("user123")
                    .audience("api-service")
                    .issuedAtNow()
                    .expiresIn(Duration.ofHours(1))
                    .generateJwtId()
                    .claim("role", "admin")
                    .build();

            assertThat(claims.issuer()).isEqualTo("auth-service");
            assertThat(claims.subject()).isEqualTo("user123");
            assertThat(claims.audience()).containsExactly("api-service");
            assertThat(claims.issuedAt()).isNotNull();
            assertThat(claims.expiration()).isNotNull();
            assertThat(claims.jwtId()).isNotNull();
            assertThat(claims.getString("role")).isEqualTo("admin");
        }
    }

    @Nested
    @DisplayName("Getter Tests / 获取器测试")
    class GetterTests {

        @Test
        @DisplayName("get()返回原始值")
        void testGet() {
            JwtClaims claims = JwtClaims.of(Map.of("key", 123));
            assertThat(claims.get("key")).isEqualTo(123);
        }

        @Test
        @DisplayName("getString()返回字符串值")
        void testGetString() {
            JwtClaims claims = JwtClaims.of(Map.of("key", "value"));
            assertThat(claims.getString("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("getString()非字符串转换为字符串")
        void testGetStringConverts() {
            JwtClaims claims = JwtClaims.of(Map.of("key", 123));
            assertThat(claims.getString("key")).isEqualTo("123");
        }

        @Test
        @DisplayName("getInt()返回整数值")
        void testGetInt() {
            JwtClaims claims = JwtClaims.of(Map.of("key", 123));
            assertThat(claims.getInt("key")).isEqualTo(123);
        }

        @Test
        @DisplayName("getInt()从字符串解析")
        void testGetIntFromString() {
            JwtClaims claims = JwtClaims.of(Map.of("key", "456"));
            assertThat(claims.getInt("key")).isEqualTo(456);
        }

        @Test
        @DisplayName("getLong()返回长整数值")
        void testGetLong() {
            JwtClaims claims = JwtClaims.of(Map.of("key", 9999999999L));
            assertThat(claims.getLong("key")).isEqualTo(9999999999L);
        }

        @Test
        @DisplayName("getBoolean()返回布尔值")
        void testGetBoolean() {
            JwtClaims claims = JwtClaims.of(Map.of("flag", true));
            assertThat(claims.getBoolean("flag")).isTrue();
        }

        @Test
        @DisplayName("getBoolean()从字符串解析")
        void testGetBooleanFromString() {
            JwtClaims claims = JwtClaims.of(Map.of("flag", "true"));
            assertThat(claims.getBoolean("flag")).isTrue();
        }

        @Test
        @DisplayName("getInstant()从epoch秒获取")
        void testGetInstant() {
            long epochSecond = Instant.now().getEpochSecond();
            JwtClaims claims = JwtClaims.of(Map.of("time", epochSecond));
            assertThat(claims.getInstant("time").getEpochSecond()).isEqualTo(epochSecond);
        }

        @Test
        @DisplayName("getList()返回列表值")
        void testGetList() {
            JwtClaims claims = JwtClaims.of(Map.of("items", List.of("a", "b", "c")));
            assertThat(claims.<String>getList("items")).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("缺失值返回null")
        void testMissingValueReturnsNull() {
            JwtClaims claims = JwtClaims.empty();

            assertThat(claims.get("missing")).isNull();
            assertThat(claims.getString("missing")).isNull();
            assertThat(claims.getInt("missing")).isNull();
            assertThat(claims.getLong("missing")).isNull();
            assertThat(claims.getBoolean("missing")).isNull();
            assertThat(claims.getInstant("missing")).isNull();
            assertThat(claims.<String>getList("missing")).isNull();
        }
    }

    @Nested
    @DisplayName("Utility Method Tests / 工具方法测试")
    class UtilityMethodTests {

        @Test
        @DisplayName("contains()检查声明存在")
        void testContains() {
            JwtClaims claims = JwtClaims.builder()
                    .subject("user")
                    .build();

            assertThat(claims.contains(JwtClaims.SUBJECT)).isTrue();
            assertThat(claims.contains("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("names()返回所有声明名称")
        void testNames() {
            JwtClaims claims = JwtClaims.builder()
                    .issuer("iss")
                    .subject("sub")
                    .claim("custom", "value")
                    .build();

            assertThat(claims.names()).containsExactlyInAnyOrder("iss", "sub", "custom");
        }

        @Test
        @DisplayName("asMap()返回不可变映射")
        void testAsMap() {
            JwtClaims claims = JwtClaims.builder()
                    .subject("user")
                    .build();

            Map<String, Object> map = claims.asMap();
            assertThat(map).containsEntry("sub", "user");

            // Verify map is unmodifiable
            assertThatThrownBy(() -> map.put("new", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Expiration Tests / 过期测试")
    class ExpirationTests {

        @Test
        @DisplayName("isExpired()检测已过期")
        void testIsExpired() {
            JwtClaims expired = JwtClaims.builder()
                    .expiration(Instant.now().minusSeconds(60))
                    .build();

            assertThat(expired.isExpired()).isTrue();
        }

        @Test
        @DisplayName("isExpired()检测未过期")
        void testIsNotExpired() {
            JwtClaims notExpired = JwtClaims.builder()
                    .expiration(Instant.now().plusSeconds(3600))
                    .build();

            assertThat(notExpired.isExpired()).isFalse();
        }

        @Test
        @DisplayName("isExpired()无过期时间返回false")
        void testIsExpiredNoExpiration() {
            JwtClaims noExp = JwtClaims.empty();
            assertThat(noExp.isExpired()).isFalse();
        }

        @Test
        @DisplayName("isNotYetValid()检测尚未生效")
        void testIsNotYetValid() {
            JwtClaims notYetValid = JwtClaims.builder()
                    .notBefore(Instant.now().plusSeconds(3600))
                    .build();

            assertThat(notYetValid.isNotYetValid()).isTrue();
        }

        @Test
        @DisplayName("isNotYetValid()检测已生效")
        void testIsAlreadyValid() {
            JwtClaims valid = JwtClaims.builder()
                    .notBefore(Instant.now().minusSeconds(60))
                    .build();

            assertThat(valid.isNotYetValid()).isFalse();
        }

        @Test
        @DisplayName("isNotYetValid()无nbf返回false")
        void testIsNotYetValidNoNbf() {
            JwtClaims noNbf = JwtClaims.empty();
            assertThat(noNbf.isNotYetValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests / 相等性和哈希测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同声明相等")
        void testEqualClaims() {
            JwtClaims claims1 = JwtClaims.builder()
                    .subject("user")
                    .issuer("auth")
                    .build();
            JwtClaims claims2 = JwtClaims.builder()
                    .subject("user")
                    .issuer("auth")
                    .build();

            assertThat(claims1).isEqualTo(claims2);
            assertThat(claims1.hashCode()).isEqualTo(claims2.hashCode());
        }

        @Test
        @DisplayName("不同声明不相等")
        void testUnequalClaims() {
            JwtClaims claims1 = JwtClaims.builder()
                    .subject("user1")
                    .build();
            JwtClaims claims2 = JwtClaims.builder()
                    .subject("user2")
                    .build();

            assertThat(claims1).isNotEqualTo(claims2);
        }
    }

    @Nested
    @DisplayName("Constants Tests / 常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("标准声明名称常量正确")
        void testStandardClaimConstants() {
            assertThat(JwtClaims.ISSUER).isEqualTo("iss");
            assertThat(JwtClaims.SUBJECT).isEqualTo("sub");
            assertThat(JwtClaims.AUDIENCE).isEqualTo("aud");
            assertThat(JwtClaims.EXPIRATION).isEqualTo("exp");
            assertThat(JwtClaims.NOT_BEFORE).isEqualTo("nbf");
            assertThat(JwtClaims.ISSUED_AT).isEqualTo("iat");
            assertThat(JwtClaims.JWT_ID).isEqualTo("jti");
        }
    }

    @Nested
    @DisplayName("ToString Tests / 字符串表示测试")
    class ToStringTests {

        @Test
        @DisplayName("toString()包含声明信息")
        void testToString() {
            JwtClaims claims = JwtClaims.builder()
                    .subject("user")
                    .build();

            String str = claims.toString();
            assertThat(str).contains("JwtClaims");
            assertThat(str).contains("sub");
            assertThat(str).contains("user");
        }
    }
}
