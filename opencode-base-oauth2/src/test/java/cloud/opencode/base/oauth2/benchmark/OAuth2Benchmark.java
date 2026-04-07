package cloud.opencode.base.oauth2.benchmark;

import cloud.opencode.base.oauth2.internal.JsonParser;
import cloud.opencode.base.oauth2.oidc.JwtClaims;
import cloud.opencode.base.oauth2.pkce.PkceChallenge;
import cloud.opencode.base.oauth2.security.StateParameter;
import cloud.opencode.base.oauth2.token.InMemoryTokenStore;
import cloud.opencode.base.oauth2.OAuth2Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance Benchmarks for opencode-base-oauth2
 * opencode-base-oauth2 性能基准测试
 *
 * <p>Measures throughput and latency of core OAuth2 operations using warmup + measurement
 * iterations with nanoTime-based timing. Covers hot paths identified during code review:
 * JWT parsing, JSON field extraction, PKCE generation, state parameter, discovery cache,
 * token store, and token lifecycle checks.</p>
 * <p>使用预热+测量迭代和 nanoTime 计时测量核心 OAuth2 操作的吞吐量和延迟。
 * 覆盖代码审查中识别的热路径：JWT 解析、JSON 字段提取、PKCE 生成、state 参数、
 * 令牌存储和令牌生命周期检查。</p>
 *
 * <p><strong>Run:</strong> {@code mvn test -pl opencode-base-oauth2 -Dgroups=benchmark}</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
@DisplayName("opencode-base-oauth2 性能基准测试")
@Tag("benchmark")
class OAuth2Benchmark {

    // ==================== Fixtures ====================

    /**
     * Minimal token-endpoint response (~80 chars).
     * 最小令牌端点响应（约 80 字符）。
     */
    private static final String TOKEN_RESPONSE_SMALL =
            "{\"access_token\":\"abc123\",\"token_type\":\"Bearer\",\"expires_in\":3600}";

    /**
     * Full token-endpoint response with all optional fields (~260 chars).
     * 完整令牌端点响应，包含所有可选字段（约 260 字符）。
     */
    private static final String TOKEN_RESPONSE_FULL =
            "{\"access_token\":\"eyJhbGciOiJSUzI1NiJ9.payload.sig\"," +
            "\"token_type\":\"Bearer\"," +
            "\"expires_in\":3600," +
            "\"refresh_token\":\"rt_xyz_refresh_token_value\"," +
            "\"scope\":\"openid email profile\"," +
            "\"id_token\":\"eyJhbGciOiJSUzI1NiJ9.payload.sig\"," +
            "\"jti\":\"unique-jwt-id-12345\"}";

    /**
     * Typical OIDC discovery response (~600 chars).
     * 典型 OIDC 发现响应（约 600 字符）。
     */
    private static final String DISCOVERY_RESPONSE =
            "{\"issuer\":\"https://accounts.google.com\"," +
            "\"authorization_endpoint\":\"https://accounts.google.com/o/oauth2/v2/auth\"," +
            "\"token_endpoint\":\"https://oauth2.googleapis.com/token\"," +
            "\"userinfo_endpoint\":\"https://openidconnect.googleapis.com/v1/userinfo\"," +
            "\"jwks_uri\":\"https://www.googleapis.com/oauth2/v3/certs\"," +
            "\"response_types_supported\":[\"code\",\"token\",\"id_token\"]," +
            "\"subject_types_supported\":[\"public\"]," +
            "\"id_token_signing_alg_values_supported\":[\"RS256\"]," +
            "\"scopes_supported\":[\"openid\",\"email\",\"profile\"]," +
            "\"token_endpoint_auth_methods_supported\":[\"client_secret_post\",\"client_secret_basic\"]," +
            "\"code_challenge_methods_supported\":[\"plain\",\"S256\"]," +
            "\"grant_types_supported\":[\"authorization_code\",\"refresh_token\"," +
            "\"urn:ietf:params:oauth:grant-type:device_code\"]}";

    /**
     * JWT with standard OIDC claims (realistic payload, single string aud).
     * 包含标准 OIDC 声明的 JWT（真实有效载荷，单字符串 aud）。
     */
    private static final String JWT_TOKEN = buildJwt();

    /**
     * JWT with aud as string array.
     * aud 为字符串数组的 JWT。
     */
    private static final String JWT_TOKEN_ARRAY_AUD = buildJwtArrayAud();

    /** Pre-computed verifier (43-char minimum, S256-compatible). 预计算验证器（43 字符）。 */
    private static final String PKCE_VERIFIER_43 =
            "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";

    /** Pre-computed longer verifier (86 chars). 预计算较长验证器（86 字符）。 */
    private static final String PKCE_VERIFIER_86 =
            "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXkdBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1g";

    /** Pre-computed state for validate benchmarks. 预计算 state 用于验证基准。 */
    private static final String PRECOMPUTED_STATE = StateParameter.generate();

    /** Sample token for store/lifecycle benchmarks. 令牌存储和生命周期基准用令牌。 */
    private static final OAuth2Token SAMPLE_TOKEN = OAuth2Token.builder()
            .accessToken("eyJhbGciOiJSUzI1NiJ9.sample.token")
            .tokenType("Bearer")
            .expiresIn(3600)
            .refreshToken("sample_refresh_token")
            .build();

    private static final Duration SIXTY_SECONDS = Duration.ofSeconds(60);

    private static String buildJwt() {
        String header = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9";
        String payloadJson = "{\"iss\":\"https://accounts.google.com\"," +
                "\"sub\":\"1234567890\"," +
                "\"aud\":\"my-client-id\"," +
                "\"exp\":9999999999," +
                "\"iat\":1700000000," +
                "\"nonce\":\"random-nonce-value\"," +
                "\"email\":\"user@example.com\"," +
                "\"name\":\"Test User\"," +
                "\"picture\":\"https://example.com/photo.jpg\"}";
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".fakesig";
    }

    private static String buildJwtArrayAud() {
        String header = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9";
        String payloadJson = "{\"iss\":\"https://auth.example.com\"," +
                "\"sub\":\"user-abc\"," +
                "\"aud\":[\"client-a\",\"client-b\",\"client-c\"]," +
                "\"exp\":9999999999," +
                "\"iat\":1700000000," +
                "\"jti\":\"unique-id-xyz\"}";
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".fakesig";
    }

    // ==================== Benchmark Infrastructure ====================

    private record BenchResult(String name, long ops, long totalNanos) {
        double opsPerMs() { return ops * 1_000_000.0 / totalNanos; }
        double nsPerOp()  { return (double) totalNanos / ops; }

        @Override
        public String toString() {
            return String.format("  %-58s %,8d ops  %,9.0f ops/ms  %,7.1f ns/op",
                    name, ops, opsPerMs(), nsPerOp());
        }
    }

    @FunctionalInterface
    interface BenchAction { void run(); }

    private BenchResult bench(String name, int warmup, int measure, BenchAction action) {
        for (int i = 0; i < warmup; i++) action.run();
        long start = System.nanoTime();
        for (int i = 0; i < measure; i++) action.run();
        return new BenchResult(name, measure, System.nanoTime() - start);
    }

    private static final int WARMUP  = 20_000;
    private static final int MEASURE = 100_000;

    // ==================== Benchmarks ====================

    @Nested
    @DisplayName("JWT 解析基准测试 (JwtClaims.parse)")
    class JwtParseBenchmarks {

        @Test
        @DisplayName("JWT 解析性能: 单字符串 aud / 数组 aud")
        void jwtParseThroughput() {
            System.out.println("\n══════════ JwtClaims.parse Benchmarks ══════════");

            BenchResult r1 = bench("parse JWT  string-aud (9 claims)",
                    WARMUP, MEASURE, () -> JwtClaims.parse(JWT_TOKEN));
            BenchResult r2 = bench("parse JWT  array-aud  (3 elements)",
                    WARMUP, MEASURE, () -> JwtClaims.parse(JWT_TOKEN_ARRAY_AUD));

            System.out.println(r1);
            System.out.println(r2);
            System.out.println();

            // Correctness spot-check
            JwtClaims claims = JwtClaims.parse(JWT_TOKEN);
            assertThat(claims.iss()).isEqualTo("https://accounts.google.com");
            assertThat(claims.sub()).isEqualTo("1234567890");
            assertThat(claims.aud()).containsExactly("my-client-id");
            assertThat(r1.opsPerMs()).isGreaterThan(0);
        }

        @Test
        @DisplayName("isExpired / isValid 检查性能")
        void jwtValidityCheckThroughput() {
            System.out.println("\n══════════ JwtClaims validity-check Benchmarks ══════════");

            JwtClaims claims = JwtClaims.parse(JWT_TOKEN);

            BenchResult r1 = bench("isExpired()  (far-future exp)", WARMUP, MEASURE, claims::isExpired);
            BenchResult r2 = bench("isValid()    (far-future exp)", WARMUP, MEASURE, claims::isValid);

            System.out.println(r1);
            System.out.println(r2);
            System.out.println();

            assertThat(claims.isExpired()).isFalse();
            assertThat(r1.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("JsonParser 字段提取基准测试")
    class JsonParserBenchmarks {

        @Test
        @DisplayName("getString / getLong 单字段提取性能")
        void fieldExtractionThroughput() {
            System.out.println("\n══════════ JsonParser field-extraction Benchmarks ══════════");

            // getString — first field (fast path)
            BenchResult r1 = bench("getString  access_token  (small, first field)",
                    WARMUP, MEASURE, () -> JsonParser.getString(TOKEN_RESPONSE_SMALL, "access_token"));
            BenchResult r2 = bench("getString  access_token  (full,  first field)",
                    WARMUP, MEASURE, () -> JsonParser.getString(TOKEN_RESPONSE_FULL,  "access_token"));

            // getLong — middle field
            BenchResult r3 = bench("getLong    expires_in    (small)",
                    WARMUP, MEASURE, () -> JsonParser.getLong(TOKEN_RESPONSE_SMALL, "expires_in"));
            BenchResult r4 = bench("getLong    expires_in    (full)",
                    WARMUP, MEASURE, () -> JsonParser.getLong(TOKEN_RESPONSE_FULL, "expires_in"));

            // getString — last field (full scan)
            BenchResult r5 = bench("getString  jti           (full,  last field)",
                    WARMUP, MEASURE, () -> JsonParser.getString(TOKEN_RESPONSE_FULL, "jti"));

            // getString — missing field (full scan + negative)
            BenchResult r6 = bench("getString  [missing]     (full,  not found)",
                    WARMUP, MEASURE, () -> JsonParser.getString(TOKEN_RESPONSE_FULL, "not_present"));

            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println(r4);
            System.out.println(r5);
            System.out.println(r6);
            System.out.println();

            assertThat(JsonParser.getString(TOKEN_RESPONSE_SMALL, "access_token")).isEqualTo("abc123");
            assertThat(JsonParser.getLong(TOKEN_RESPONSE_SMALL, "expires_in")).isEqualTo(3600L);
            assertThat(r1.opsPerMs()).isGreaterThan(0);
        }

        @Test
        @DisplayName("parseObject 完整对象解析性能")
        void parseObjectThroughput() {
            System.out.println("\n══════════ JsonParser.parseObject Benchmarks ══════════");

            BenchResult r1 = bench("parseObject  token-small  (~80 chars)",
                    WARMUP, MEASURE, () -> JsonParser.parseObject(TOKEN_RESPONSE_SMALL));
            BenchResult r2 = bench("parseObject  token-full   (~260 chars)",
                    WARMUP, MEASURE, () -> JsonParser.parseObject(TOKEN_RESPONSE_FULL));
            BenchResult r3 = bench("parseObject  discovery    (~600 chars)",
                    WARMUP / 2, MEASURE, () -> JsonParser.parseObject(DISCOVERY_RESPONSE));

            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println();

            Map<String, Object> parsed = JsonParser.parseObject(TOKEN_RESPONSE_SMALL);
            assertThat(parsed.get("access_token")).isEqualTo("abc123");
            assertThat(parsed.get("expires_in")).isEqualTo(3600L);
            assertThat(r1.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("PKCE 生成基准测试")
    class PkceBenchmarks {

        @Test
        @DisplayName("PKCE generate() + S256 挑战计算性能")
        void pkceGenerationThroughput() {
            System.out.println("\n══════════ PKCE Benchmarks ══════════");

            // Full generate: SecureRandom verifier + SHA-256 challenge
            BenchResult r1 = bench("PkceChallenge.generate()              (full round-trip)",
                    WARMUP, MEASURE, PkceChallenge::generate);

            // SHA-256 only (ThreadLocal digest) — the hot path during verification
            BenchResult r2 = bench("calculateS256Challenge(43-char verifier)",
                    WARMUP, MEASURE, () -> PkceChallenge.calculateS256Challenge(PKCE_VERIFIER_43));
            BenchResult r3 = bench("calculateS256Challenge(86-char verifier)",
                    WARMUP, MEASURE, () -> PkceChallenge.calculateS256Challenge(PKCE_VERIFIER_86));

            // Verify: challenge matches verifier (used on token exchange)
            String challenge43 = PkceChallenge.calculateS256Challenge(PKCE_VERIFIER_43);
            BenchResult r4 = bench("PkceChallenge.verify()                (S256 match)",
                    WARMUP, MEASURE,
                    () -> PkceChallenge.verify(PKCE_VERIFIER_43, challenge43, PkceChallenge.METHOD_S256));

            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println(r4);
            System.out.println();

            // Correctness: challenge = base64url(SHA-256(verifier)), always 43 chars
            assertThat(challenge43).hasSize(43);
            assertThat(PkceChallenge.verify(PKCE_VERIFIER_43, challenge43, PkceChallenge.METHOD_S256)).isTrue();
            assertThat(r2.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("State 参数基准测试")
    class StateParameterBenchmarks {

        @Test
        @DisplayName("state 生成 + 常量时间验证性能")
        void stateGenerationValidationThroughput() {
            System.out.println("\n══════════ StateParameter Benchmarks ══════════");

            // Generate: SecureRandom (32 bytes) + base64url encode
            BenchResult r1 = bench("StateParameter.generate()       (32-byte CSPRNG)",
                    WARMUP, MEASURE, StateParameter::generate);

            // Validate: MessageDigest.isEqual — constant time regardless of match/mismatch
            BenchResult r2 = bench("StateParameter.validate()       (match → true)",
                    WARMUP, MEASURE,
                    () -> StateParameter.validate(PRECOMPUTED_STATE, PRECOMPUTED_STATE));

            BenchResult r3 = bench("StateParameter.validate()       (mismatch → false)",
                    WARMUP, MEASURE,
                    () -> StateParameter.validate(PRECOMPUTED_STATE, "wrong-state-value-xyz"));

            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println();

            assertThat(StateParameter.validate(PRECOMPUTED_STATE, PRECOMPUTED_STATE)).isTrue();
            assertThat(StateParameter.validate(PRECOMPUTED_STATE, "wrong")).isFalse();
            assertThat(r1.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("令牌存储基准测试 (InMemoryTokenStore)")
    class TokenStoreBenchmarks {

        @Test
        @DisplayName("save / load / exists 操作性能")
        void tokenStoreOperationThroughput() {
            System.out.println("\n══════════ InMemoryTokenStore Benchmarks ══════════");

            InMemoryTokenStore store = new InMemoryTokenStore();

            // Pre-populate keys for load/exists benchmarks
            for (int i = 0; i < 100; i++) {
                store.save("user-" + i, SAMPLE_TOKEN);
            }

            // save: ConcurrentHashMap put (overwrite existing key)
            BenchResult r1 = bench("save(key, token)              (rotating 100 keys)",
                    WARMUP, MEASURE, new BenchAction() {
                        int i = 0;
                        @Override public void run() {
                            store.save("bench-" + (i++ % 100), SAMPLE_TOKEN);
                        }
                    });

            // load: ConcurrentHashMap get + Optional wrap
            BenchResult r2 = bench("load(key)                     (hit)",
                    WARMUP, MEASURE, new BenchAction() {
                        int i = 0;
                        @Override public void run() {
                            store.load("user-" + (i++ % 100));
                        }
                    });

            // load: miss — returns Optional.empty()
            BenchResult r3 = bench("load(key)                     (miss)",
                    WARMUP, MEASURE, () -> store.load("nonexistent-key-xyz"));

            // exists: just containsKey check
            BenchResult r4 = bench("exists(key)                   (hit)",
                    WARMUP, MEASURE, new BenchAction() {
                        int i = 0;
                        @Override public void run() {
                            store.exists("user-" + (i++ % 100));
                        }
                    });

            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println(r4);
            System.out.println();

            assertThat(store.exists("user-0")).isTrue();
            assertThat(store.load("user-0")).isPresent();
            assertThat(store.load("nonexistent")).isEmpty();
            assertThat(r2.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("OAuth2Token 构建与生命周期基准测试")
    class OAuth2TokenBenchmarks {

        @Test
        @DisplayName("OAuth2Token.builder() 构建性能 + 生命周期检查")
        void tokenBuilderThroughput() {
            System.out.println("\n══════════ OAuth2Token Benchmarks ══════════");

            // Builder: create immutable token record (object allocation)
            BenchResult r1 = bench("OAuth2Token.builder().build()  (full fields)",
                    WARMUP, MEASURE,
                    () -> OAuth2Token.builder()
                            .accessToken("eyJhbGciOiJSUzI1NiJ9.payload.sig")
                            .tokenType("Bearer")
                            .expiresIn(3600)
                            .refreshToken("refresh_token_value")
                            .build());

            // isExpired: compare expiresAt with Instant.now()
            BenchResult r2 = bench("token.isExpired()              (not expired)",
                    WARMUP, MEASURE, SAMPLE_TOKEN::isExpired);

            // isExpiringSoon: Duration comparison
            BenchResult r3 = bench("token.isExpiringSoon(60s)      (not expiring)",
                    WARMUP, MEASURE, () -> SAMPLE_TOKEN.isExpiringSoon(SIXTY_SECONDS));

            // hasRefreshToken / hasIdToken: null check
            BenchResult r4 = bench("token.hasRefreshToken()        (true)",
                    WARMUP, MEASURE, SAMPLE_TOKEN::hasRefreshToken);

            System.out.println(r1);
            System.out.println(r2);
            System.out.println(r3);
            System.out.println(r4);
            System.out.println();

            assertThat(SAMPLE_TOKEN.accessToken()).isEqualTo("eyJhbGciOiJSUzI1NiJ9.sample.token");
            assertThat(SAMPLE_TOKEN.isExpired()).isFalse();
            assertThat(SAMPLE_TOKEN.hasRefreshToken()).isTrue();
            assertThat(r1.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("综合吞吐量报告")
    class SummaryBenchmarks {

        @Test
        @DisplayName("全量热路径吞吐量汇总")
        void fullSummary() {
            System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║       opencode-base-oauth2  V1.0.3 — Performance Benchmark Report             ║");
            System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝\n");

            System.out.println("── JwtClaims.parse ───────────────────────────────────────────────────────────────");
            print(bench("parse JWT string-aud (9 claims)",  WARMUP, MEASURE,
                    () -> JwtClaims.parse(JWT_TOKEN)));
            print(bench("parse JWT array-aud  (3 elem)",    WARMUP, MEASURE,
                    () -> JwtClaims.parse(JWT_TOKEN_ARRAY_AUD)));

            System.out.println("── JsonParser ────────────────────────────────────────────────────────────────────");
            print(bench("getString access_token  (small)",  WARMUP, MEASURE,
                    () -> JsonParser.getString(TOKEN_RESPONSE_SMALL, "access_token")));
            print(bench("getString access_token  (full)",   WARMUP, MEASURE,
                    () -> JsonParser.getString(TOKEN_RESPONSE_FULL, "access_token")));
            print(bench("getLong   expires_in    (small)",  WARMUP, MEASURE,
                    () -> JsonParser.getLong(TOKEN_RESPONSE_SMALL, "expires_in")));
            print(bench("parseObject token-small (~80 chars)",   WARMUP, MEASURE,
                    () -> JsonParser.parseObject(TOKEN_RESPONSE_SMALL)));
            print(bench("parseObject discovery  (~600 chars)",   WARMUP / 2, MEASURE,
                    () -> JsonParser.parseObject(DISCOVERY_RESPONSE)));

            System.out.println("── PKCE ──────────────────────────────────────────────────────────────────────────");
            print(bench("generate()          (SecureRandom + SHA-256)",  WARMUP, MEASURE,
                    PkceChallenge::generate));
            print(bench("calculateS256Challenge (43-char verifier)",     WARMUP, MEASURE,
                    () -> PkceChallenge.calculateS256Challenge(PKCE_VERIFIER_43)));

            System.out.println("── StateParameter ────────────────────────────────────────────────────────────────");
            print(bench("generate()          (32-byte CSPRNG)",          WARMUP, MEASURE,
                    StateParameter::generate));
            print(bench("validate()          match → true",              WARMUP, MEASURE,
                    () -> StateParameter.validate(PRECOMPUTED_STATE, PRECOMPUTED_STATE)));
            print(bench("validate()          mismatch → false",          WARMUP, MEASURE,
                    () -> StateParameter.validate(PRECOMPUTED_STATE, "wrong")));

            System.out.println("── InMemoryTokenStore ────────────────────────────────────────────────────────────");
            InMemoryTokenStore store = new InMemoryTokenStore();
            store.save("bench-user", SAMPLE_TOKEN);
            print(bench("save(key, token)",                              WARMUP, MEASURE,
                    () -> store.save("bench-user", SAMPLE_TOKEN)));
            print(bench("load(key)  hit",                               WARMUP, MEASURE,
                    () -> store.load("bench-user")));
            print(bench("load(key)  miss",                              WARMUP, MEASURE,
                    () -> store.load("no-such-key")));

            System.out.println("── OAuth2Token ───────────────────────────────────────────────────────────────────");
            print(bench("builder().build() (4 fields)",                 WARMUP, MEASURE,
                    () -> OAuth2Token.builder()
                            .accessToken("tok").tokenType("Bearer").expiresIn(3600).build()));
            print(bench("isExpired()        (not expired)",             WARMUP, MEASURE,
                    SAMPLE_TOKEN::isExpired));
            print(bench("isExpiringSoon(60s)",                          WARMUP, MEASURE,
                    () -> SAMPLE_TOKEN.isExpiringSoon(SIXTY_SECONDS)));

            System.out.println("\n──────────────────────────────────────────────────────────────────────────────────");
            System.out.println("  JVM: " + System.getProperty("java.vm.version") +
                    "  OS: " + System.getProperty("os.name"));
            System.out.println();
        }

        private void print(BenchResult r) { System.out.println(r); }
    }
}
