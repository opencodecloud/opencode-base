package cloud.opencode.base.yml.security;

import cloud.opencode.base.yml.YmlConfig;
import cloud.opencode.base.yml.exception.YmlSecurityException;
import cloud.opencode.base.yml.exception.YmlSecurityException.SecurityViolationType;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlSecurityTest Tests
 * YmlSecurityTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlSecurity Tests")
class YmlSecurityTest {

    // ==================== Constants Tests ====================

    @Nested
    @DisplayName("DEFAULT_MAX_DOCUMENT_SIZE constant")
    class DefaultMaxDocumentSizeTests {

        @Test
        @DisplayName("should be 10 MB")
        void shouldBe10Mb() {
            assertThat(YmlSecurity.DEFAULT_MAX_DOCUMENT_SIZE).isEqualTo(10 * 1024 * 1024);
        }

        @Test
        @DisplayName("should be 10485760 bytes exactly")
        void shouldBe10485760BytesExactly() {
            assertThat(YmlSecurity.DEFAULT_MAX_DOCUMENT_SIZE).isEqualTo(10_485_760L);
        }

        @Test
        @DisplayName("should be positive")
        void shouldBePositive() {
            assertThat(YmlSecurity.DEFAULT_MAX_DOCUMENT_SIZE).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("DEFAULT_MAX_ALIASES constant")
    class DefaultMaxAliasesTests {

        @Test
        @DisplayName("should be 50")
        void shouldBe50() {
            assertThat(YmlSecurity.DEFAULT_MAX_ALIASES).isEqualTo(50);
        }

        @Test
        @DisplayName("should be positive")
        void shouldBePositive() {
            assertThat(YmlSecurity.DEFAULT_MAX_ALIASES).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("DEFAULT_MAX_DEPTH constant")
    class DefaultMaxDepthTests {

        @Test
        @DisplayName("should be 50")
        void shouldBe50() {
            assertThat(YmlSecurity.DEFAULT_MAX_DEPTH).isEqualTo(50);
        }

        @Test
        @DisplayName("should be positive")
        void shouldBePositive() {
            assertThat(YmlSecurity.DEFAULT_MAX_DEPTH).isGreaterThan(0);
        }
    }

    // ==================== validate() Tests ====================

    @Nested
    @DisplayName("validate method with default limits")
    class ValidateDefaultTests {

        @Test
        @DisplayName("should accept null content")
        void shouldAcceptNullContent() {
            assertThatNoException().isThrownBy(() -> YmlSecurity.validate(null));
        }

        @Test
        @DisplayName("should accept empty content")
        void shouldAcceptEmptyContent() {
            assertThatNoException().isThrownBy(() -> YmlSecurity.validate(""));
        }

        @Test
        @DisplayName("should accept simple valid YAML")
        void shouldAcceptSimpleValidYaml() {
            String yaml = """
                name: test
                value: 123
                """;
            assertThatNoException().isThrownBy(() -> YmlSecurity.validate(yaml));
        }

        @Test
        @DisplayName("should accept valid YAML with reasonable aliases")
        void shouldAcceptValidYamlWithReasonableAliases() {
            String yaml = """
                defaults: &defaults
                  adapter: postgres
                  host: localhost

                development:
                  database: dev
                  <<: *defaults

                production:
                  database: prod
                  <<: *defaults
                """;
            assertThatNoException().isThrownBy(() -> YmlSecurity.validate(yaml));
        }

        @Test
        @DisplayName("should accept complex nested YAML")
        void shouldAcceptComplexNestedYaml() {
            String yaml = """
                server:
                  port: 8080
                  ssl:
                    enabled: true
                    keyStore: /path/to/keystore
                database:
                  primary:
                    url: jdbc:mysql://localhost/db
                  replica:
                    url: jdbc:mysql://replica/db
                """;
            assertThatNoException().isThrownBy(() -> YmlSecurity.validate(yaml));
        }

        @Test
        @DisplayName("should reject content exceeding default size limit")
        void shouldRejectContentExceedingDefaultSizeLimit() {
            // Create content larger than 10 MB
            String largeContent = "x".repeat((int) (YmlSecurity.DEFAULT_MAX_DOCUMENT_SIZE + 1));

            assertThatThrownBy(() -> YmlSecurity.validate(largeContent))
                .isInstanceOf(YmlSecurityException.class)
                .satisfies(e -> {
                    YmlSecurityException ex = (YmlSecurityException) e;
                    assertThat(ex.getType()).isEqualTo(SecurityViolationType.DOCUMENT_SIZE_EXCEEDED);
                });
        }

        @Test
        @DisplayName("should reject content with too many aliases")
        void shouldRejectContentWithTooManyAliases() {
            StringBuilder yaml = new StringBuilder();
            yaml.append("anchor: &anchor value\n");
            for (int i = 0; i < 100; i++) {
                yaml.append("ref").append(i).append(": *anchor\n");
            }

            assertThatThrownBy(() -> YmlSecurity.validate(yaml.toString()))
                .isInstanceOf(YmlSecurityException.class)
                .satisfies(e -> {
                    YmlSecurityException ex = (YmlSecurityException) e;
                    assertThat(ex.getType()).isEqualTo(SecurityViolationType.ALIAS_LIMIT_EXCEEDED);
                });
        }

        @Test
        @DisplayName("should reject content with dangerous Python tag")
        void shouldRejectContentWithDangerousPythonTag() {
            String yaml = "exploit: !!python/object:os.system 'rm -rf /'";

            assertThatThrownBy(() -> YmlSecurity.validate(yaml))
                .isInstanceOf(YmlSecurityException.class)
                .satisfies(e -> {
                    YmlSecurityException ex = (YmlSecurityException) e;
                    assertThat(ex.getType()).isEqualTo(SecurityViolationType.FORBIDDEN_TYPE);
                });
        }

        @Test
        @DisplayName("should reject content with dangerous Java tag")
        void shouldRejectContentWithDangerousJavaTag() {
            String yaml = "exploit: !!java/object:java.lang.Runtime";

            assertThatThrownBy(() -> YmlSecurity.validate(yaml))
                .isInstanceOf(YmlSecurityException.class)
                .satisfies(e -> {
                    YmlSecurityException ex = (YmlSecurityException) e;
                    assertThat(ex.getType()).isEqualTo(SecurityViolationType.FORBIDDEN_TYPE);
                });
        }
    }

    @Nested
    @DisplayName("validate method with custom limits")
    class ValidateCustomLimitsTests {

        @Test
        @DisplayName("should accept content within custom size limit")
        void shouldAcceptContentWithinCustomSizeLimit() {
            String yaml = "key: value";
            assertThatNoException().isThrownBy(() ->
                YmlSecurity.validate(yaml, 1000, 10));
        }

        @Test
        @DisplayName("should reject content exceeding custom size limit")
        void shouldRejectContentExceedingCustomSizeLimit() {
            String yaml = "key: " + "x".repeat(100);

            assertThatThrownBy(() -> YmlSecurity.validate(yaml, 50, 10))
                .isInstanceOf(YmlSecurityException.class)
                .satisfies(e -> {
                    YmlSecurityException ex = (YmlSecurityException) e;
                    assertThat(ex.getType()).isEqualTo(SecurityViolationType.DOCUMENT_SIZE_EXCEEDED);
                });
        }

        @Test
        @DisplayName("should accept content within custom alias limit")
        void shouldAcceptContentWithinCustomAliasLimit() {
            String yaml = """
                anchor: &anchor value
                ref1: *anchor
                ref2: *anchor
                """;
            assertThatNoException().isThrownBy(() ->
                YmlSecurity.validate(yaml, 10000, 5));
        }

        @Test
        @DisplayName("should reject content exceeding custom alias limit")
        void shouldRejectContentExceedingCustomAliasLimit() {
            StringBuilder yaml = new StringBuilder();
            yaml.append("anchor: &anchor value\n");
            for (int i = 0; i < 10; i++) {
                yaml.append("ref").append(i).append(": *anchor\n");
            }

            assertThatThrownBy(() -> YmlSecurity.validate(yaml.toString(), 10000, 5))
                .isInstanceOf(YmlSecurityException.class)
                .satisfies(e -> {
                    YmlSecurityException ex = (YmlSecurityException) e;
                    assertThat(ex.getType()).isEqualTo(SecurityViolationType.ALIAS_LIMIT_EXCEEDED);
                });
        }

        @Test
        @DisplayName("should still check dangerous patterns with custom limits")
        void shouldStillCheckDangerousPatternsWithCustomLimits() {
            String yaml = "exploit: !!python/object:os.system 'cmd'";

            assertThatThrownBy(() -> YmlSecurity.validate(yaml, 100000, 100))
                .isInstanceOf(YmlSecurityException.class)
                .satisfies(e -> {
                    YmlSecurityException ex = (YmlSecurityException) e;
                    assertThat(ex.getType()).isEqualTo(SecurityViolationType.FORBIDDEN_TYPE);
                });
        }

        @Test
        @DisplayName("should accept null with custom limits")
        void shouldAcceptNullWithCustomLimits() {
            assertThatNoException().isThrownBy(() ->
                YmlSecurity.validate(null, 100, 10));
        }

        @Test
        @DisplayName("should use exact size boundary")
        void shouldUseExactSizeBoundary() {
            String yaml = "x".repeat(100);
            // Should pass at exactly the limit
            assertThatNoException().isThrownBy(() ->
                YmlSecurity.validate(yaml, 100, 10));
            // Should fail at limit + 1
            assertThatThrownBy(() -> YmlSecurity.validate(yaml + "x", 100, 10))
                .isInstanceOf(YmlSecurityException.class);
        }
    }

    // ==================== containsDangerousPatterns() Tests ====================

    @Nested
    @DisplayName("containsDangerousPatterns method")
    class ContainsDangerousPatternsTests {

        @Test
        @DisplayName("should return false for null content")
        void shouldReturnFalseForNullContent() {
            assertThat(YmlSecurity.containsDangerousPatterns(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for empty content")
        void shouldReturnFalseForEmptyContent() {
            assertThat(YmlSecurity.containsDangerousPatterns("")).isFalse();
        }

        @Test
        @DisplayName("should return false for safe YAML")
        void shouldReturnFalseForSafeYaml() {
            String yaml = """
                name: John
                age: 30
                active: true
                """;
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isFalse();
        }

        @Test
        @DisplayName("should detect !!python/ tag")
        void shouldDetectPythonTag() {
            String yaml = "exploit: !!python/object:os.system 'ls'";
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isTrue();
        }

        @Test
        @DisplayName("should detect !!java/ tag")
        void shouldDetectJavaTag() {
            String yaml = "exploit: !!java/object:java.net.URL ['http://evil.com']";
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isTrue();
        }

        @Test
        @DisplayName("should detect !!ruby/ tag")
        void shouldDetectRubyTag() {
            String yaml = "exploit: !!ruby/object:Gem::Installer";
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isTrue();
        }

        @Test
        @DisplayName("should detect !!perl/ tag")
        void shouldDetectPerlTag() {
            String yaml = "exploit: !!perl/code 'sub { system(\"ls\") }'";
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isTrue();
        }

        @Test
        @DisplayName("should detect tag:yaml.org,2002: pattern")
        void shouldDetectYamlOrgTag() {
            String yaml = "exploit: !<tag:yaml.org,2002:java/object:java.lang.Runtime>";
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isTrue();
        }

        @Test
        @DisplayName("should detect !!javax.script tag")
        void shouldDetectJavaxScriptTag() {
            String yaml = "exploit: !!javax.script.ScriptEngineManager";
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isTrue();
        }

        @Test
        @DisplayName("should detect !!com.sun. tag")
        void shouldDetectComSunTag() {
            String yaml = "exploit: !!com.sun.rowset.JdbcRowSetImpl";
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isTrue();
        }

        @Test
        @DisplayName("should detect !!java.lang.Runtime tag")
        void shouldDetectRuntimeTag() {
            String yaml = "exploit: !!java.lang.Runtime";
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isTrue();
        }

        @Test
        @DisplayName("should detect !!java.lang.ProcessBuilder tag")
        void shouldDetectProcessBuilderTag() {
            String yaml = "exploit: !!java.lang.ProcessBuilder [['ls', '-la']]";
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isTrue();
        }

        @Test
        @DisplayName("should detect dangerous pattern in multiline YAML")
        void shouldDetectDangerousPatternInMultilineYaml() {
            String yaml = """
                name: innocent
                data:
                  value: 123
                  exploit: !!python/object:subprocess.call ['rm', '-rf', '/']
                footer: done
                """;
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isTrue();
        }

        @Test
        @DisplayName("should detect dangerous pattern anywhere in content")
        void shouldDetectDangerousPatternAnywhereInContent() {
            String yaml = "# This is a comment containing !!java/ for documentation";
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isTrue();
        }

        @Test
        @DisplayName("should not falsely detect similar safe patterns")
        void shouldNotFalselyDetectSimilarSafePatterns() {
            String yaml = """
                message: "Use !! for important notes"
                language: python
                framework: java
                """;
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isFalse();
        }
    }

    // ==================== countAliases() Tests ====================

    @Nested
    @DisplayName("countAliases method")
    class CountAliasesTests {

        @Test
        @DisplayName("should return 0 for null content")
        void shouldReturnZeroForNullContent() {
            assertThat(YmlSecurity.countAliases(null)).isZero();
        }

        @Test
        @DisplayName("should return 0 for empty content")
        void shouldReturnZeroForEmptyContent() {
            assertThat(YmlSecurity.countAliases("")).isZero();
        }

        @Test
        @DisplayName("should return 0 for content without aliases")
        void shouldReturnZeroForContentWithoutAliases() {
            String yaml = """
                name: test
                value: 123
                """;
            assertThat(YmlSecurity.countAliases(yaml)).isZero();
        }

        @Test
        @DisplayName("should count single alias")
        void shouldCountSingleAlias() {
            String yaml = """
                anchor: &anchor value
                ref: *anchor
                """;
            assertThat(YmlSecurity.countAliases(yaml)).isEqualTo(1);
        }

        @Test
        @DisplayName("should count multiple aliases")
        void shouldCountMultipleAliases() {
            String yaml = """
                anchor: &anchor value
                ref1: *anchor
                ref2: *anchor
                ref3: *anchor
                """;
            assertThat(YmlSecurity.countAliases(yaml)).isEqualTo(3);
        }

        @Test
        @DisplayName("should count aliases from multiple anchors")
        void shouldCountAliasesFromMultipleAnchors() {
            String yaml = """
                anchor1: &a1 value1
                anchor2: &a2 value2
                ref1: *a1
                ref2: *a2
                ref3: *a1
                """;
            assertThat(YmlSecurity.countAliases(yaml)).isEqualTo(3);
        }

        @Test
        @DisplayName("should not count escaped asterisks")
        void shouldNotCountEscapedAsterisks() {
            String yaml = """
                message: "use \\* for wildcards"
                note: regular *star* without escape
                """;
            // The escaped one shouldn't be counted, but the regular ones should
            int count = YmlSecurity.countAliases(yaml);
            assertThat(count).isEqualTo(2); // *star* contains two asterisks
        }

        @Test
        @DisplayName("should count asterisk at start of content")
        void shouldCountAsteriskAtStartOfContent() {
            String yaml = "*alias";
            assertThat(YmlSecurity.countAliases(yaml)).isEqualTo(1);
        }

        @Test
        @DisplayName("should count YAML bomb pattern aliases")
        void shouldCountYamlBombPatternAliases() {
            String yaml = """
                a: &a ["lol","lol","lol","lol","lol","lol","lol","lol","lol"]
                b: &b [*a,*a,*a,*a,*a,*a,*a,*a,*a]
                c: &c [*b,*b,*b,*b,*b,*b,*b,*b,*b]
                """;
            assertThat(YmlSecurity.countAliases(yaml)).isEqualTo(18);
        }
    }

    // ==================== countAnchors() Tests ====================

    @Nested
    @DisplayName("countAnchors method")
    class CountAnchorsTests {

        @Test
        @DisplayName("should return 0 for null content")
        void shouldReturnZeroForNullContent() {
            assertThat(YmlSecurity.countAnchors(null)).isZero();
        }

        @Test
        @DisplayName("should return 0 for empty content")
        void shouldReturnZeroForEmptyContent() {
            assertThat(YmlSecurity.countAnchors("")).isZero();
        }

        @Test
        @DisplayName("should return 0 for content without anchors")
        void shouldReturnZeroForContentWithoutAnchors() {
            String yaml = """
                name: test
                value: 123
                """;
            assertThat(YmlSecurity.countAnchors(yaml)).isZero();
        }

        @Test
        @DisplayName("should count single anchor")
        void shouldCountSingleAnchor() {
            String yaml = """
                anchor: &anchor value
                ref: *anchor
                """;
            assertThat(YmlSecurity.countAnchors(yaml)).isEqualTo(1);
        }

        @Test
        @DisplayName("should count multiple anchors")
        void shouldCountMultipleAnchors() {
            String yaml = """
                anchor1: &a1 value1
                anchor2: &a2 value2
                anchor3: &a3 value3
                """;
            assertThat(YmlSecurity.countAnchors(yaml)).isEqualTo(3);
        }

        @Test
        @DisplayName("should not count escaped ampersands")
        void shouldNotCountEscapedAmpersands() {
            String yaml = """
                message: "use \\& for escaping"
                note: Tom & Jerry
                """;
            // The escaped one shouldn't be counted
            int count = YmlSecurity.countAnchors(yaml);
            assertThat(count).isEqualTo(1); // "Tom & Jerry" has one &
        }

        @Test
        @DisplayName("should count ampersand at start of content")
        void shouldCountAmpersandAtStartOfContent() {
            String yaml = "&anchor value";
            assertThat(YmlSecurity.countAnchors(yaml)).isEqualTo(1);
        }

        @Test
        @DisplayName("should count YAML bomb pattern anchors")
        void shouldCountYamlBombPatternAnchors() {
            String yaml = """
                a: &a ["lol","lol","lol"]
                b: &b [*a,*a,*a]
                c: &c [*b,*b,*b]
                d: &d [*c,*c,*c]
                """;
            assertThat(YmlSecurity.countAnchors(yaml)).isEqualTo(4);
        }
    }

    // ==================== createSafeConfig() Tests ====================

    @Nested
    @DisplayName("createSafeConfig factory method without parameters")
    class CreateSafeConfigDefaultTests {

        @Test
        @DisplayName("should return non-null config")
        void shouldReturnNonNullConfig() {
            YmlConfig config = YmlSecurity.createSafeConfig();
            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("should enable safe mode")
        void shouldEnableSafeMode() {
            YmlConfig config = YmlSecurity.createSafeConfig();
            assertThat(config.isSafeMode()).isTrue();
        }

        @Test
        @DisplayName("should set default max aliases")
        void shouldSetDefaultMaxAliases() {
            YmlConfig config = YmlSecurity.createSafeConfig();
            assertThat(config.getMaxAliasesForCollections())
                .isEqualTo(YmlSecurity.DEFAULT_MAX_ALIASES);
        }

        @Test
        @DisplayName("should set default max document size")
        void shouldSetDefaultMaxDocumentSize() {
            YmlConfig config = YmlSecurity.createSafeConfig();
            assertThat(config.getMaxDocumentSize())
                .isEqualTo(YmlSecurity.DEFAULT_MAX_DOCUMENT_SIZE);
        }

        @Test
        @DisplayName("should disallow duplicate keys")
        void shouldDisallowDuplicateKeys() {
            YmlConfig config = YmlSecurity.createSafeConfig();
            assertThat(config.isAllowDuplicateKeys()).isFalse();
        }

        @Test
        @DisplayName("should return new instance each time")
        void shouldReturnNewInstanceEachTime() {
            YmlConfig config1 = YmlSecurity.createSafeConfig();
            YmlConfig config2 = YmlSecurity.createSafeConfig();
            assertThat(config1).isNotSameAs(config2);
        }
    }

    @Nested
    @DisplayName("createSafeConfig factory method with parameters")
    class CreateSafeConfigWithParamsTests {

        @Test
        @DisplayName("should return non-null config with custom params")
        void shouldReturnNonNullConfigWithCustomParams() {
            YmlConfig config = YmlSecurity.createSafeConfig(5000, 25);
            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("should enable safe mode with custom params")
        void shouldEnableSafeModeWithCustomParams() {
            YmlConfig config = YmlSecurity.createSafeConfig(5000, 25);
            assertThat(config.isSafeMode()).isTrue();
        }

        @Test
        @DisplayName("should use custom max aliases")
        void shouldUseCustomMaxAliases() {
            YmlConfig config = YmlSecurity.createSafeConfig(5000, 25);
            assertThat(config.getMaxAliasesForCollections()).isEqualTo(25);
        }

        @Test
        @DisplayName("should use custom max document size")
        void shouldUseCustomMaxDocumentSize() {
            YmlConfig config = YmlSecurity.createSafeConfig(5000, 25);
            assertThat(config.getMaxDocumentSize()).isEqualTo(5000);
        }

        @Test
        @DisplayName("should disallow duplicate keys with custom params")
        void shouldDisallowDuplicateKeysWithCustomParams() {
            YmlConfig config = YmlSecurity.createSafeConfig(10000, 100);
            assertThat(config.isAllowDuplicateKeys()).isFalse();
        }

        @Test
        @DisplayName("should accept small size limit")
        void shouldAcceptSmallSizeLimit() {
            YmlConfig config = YmlSecurity.createSafeConfig(100, 5);
            assertThat(config.getMaxDocumentSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("should accept large size limit")
        void shouldAcceptLargeSizeLimit() {
            YmlConfig config = YmlSecurity.createSafeConfig(100_000_000L, 1000);
            assertThat(config.getMaxDocumentSize()).isEqualTo(100_000_000L);
        }

        @Test
        @DisplayName("should accept zero alias limit")
        void shouldAcceptZeroAliasLimit() {
            YmlConfig config = YmlSecurity.createSafeConfig(1000, 0);
            assertThat(config.getMaxAliasesForCollections()).isZero();
        }
    }

    // ==================== sanitize() Tests ====================

    @Nested
    @DisplayName("sanitize method")
    class SanitizeTests {

        @Test
        @DisplayName("should return null for null content")
        void shouldReturnNullForNullContent() {
            assertThat(YmlSecurity.sanitize(null)).isNull();
        }

        @Test
        @DisplayName("should return empty for empty content")
        void shouldReturnEmptyForEmptyContent() {
            assertThat(YmlSecurity.sanitize("")).isEmpty();
        }

        @Test
        @DisplayName("should return unchanged safe YAML")
        void shouldReturnUnchangedSafeYaml() {
            String yaml = """
                name: test
                value: 123
                """;
            assertThat(YmlSecurity.sanitize(yaml)).isEqualTo(yaml);
        }

        @Test
        @DisplayName("should remove !!python/ tag")
        void shouldRemovePythonTag() {
            String yaml = "exploit: !!python/object:os.system 'ls'";
            String sanitized = YmlSecurity.sanitize(yaml);
            assertThat(sanitized).doesNotContain("!!python/");
            assertThat(sanitized).contains("exploit:");
        }

        @Test
        @DisplayName("should remove !!java/ tag")
        void shouldRemoveJavaTag() {
            String yaml = "exploit: !!java/object:java.net.URL";
            String sanitized = YmlSecurity.sanitize(yaml);
            assertThat(sanitized).doesNotContain("!!java/");
        }

        @Test
        @DisplayName("should remove !!ruby/ tag")
        void shouldRemoveRubyTag() {
            String yaml = "exploit: !!ruby/object:Gem::Installer";
            String sanitized = YmlSecurity.sanitize(yaml);
            assertThat(sanitized).doesNotContain("!!ruby/");
        }

        @Test
        @DisplayName("should remove !!perl/ tag")
        void shouldRemovePerlTag() {
            String yaml = "exploit: !!perl/code 'sub { }'";
            String sanitized = YmlSecurity.sanitize(yaml);
            assertThat(sanitized).doesNotContain("!!perl/");
        }

        @Test
        @DisplayName("should remove tag:yaml.org,2002: pattern")
        void shouldRemoveYamlOrgTag() {
            String yaml = "exploit: !<tag:yaml.org,2002:java/object>";
            String sanitized = YmlSecurity.sanitize(yaml);
            assertThat(sanitized).doesNotContain("tag:yaml.org,2002:");
        }

        @Test
        @DisplayName("should remove !!javax.script tag")
        void shouldRemoveJavaxScriptTag() {
            String yaml = "exploit: !!javax.script.ScriptEngineManager";
            String sanitized = YmlSecurity.sanitize(yaml);
            assertThat(sanitized).doesNotContain("!!javax.script");
        }

        @Test
        @DisplayName("should remove !!com.sun. tag")
        void shouldRemoveComSunTag() {
            String yaml = "exploit: !!com.sun.rowset.JdbcRowSetImpl";
            String sanitized = YmlSecurity.sanitize(yaml);
            assertThat(sanitized).doesNotContain("!!com.sun.");
        }

        @Test
        @DisplayName("should remove !!java.lang.Runtime tag")
        void shouldRemoveRuntimeTag() {
            String yaml = "exploit: !!java.lang.Runtime";
            String sanitized = YmlSecurity.sanitize(yaml);
            assertThat(sanitized).doesNotContain("!!java.lang.Runtime");
        }

        @Test
        @DisplayName("should remove !!java.lang.ProcessBuilder tag")
        void shouldRemoveProcessBuilderTag() {
            String yaml = "exploit: !!java.lang.ProcessBuilder [['ls']]";
            String sanitized = YmlSecurity.sanitize(yaml);
            assertThat(sanitized).doesNotContain("!!java.lang.ProcessBuilder");
        }

        @Test
        @DisplayName("should remove multiple dangerous patterns")
        void shouldRemoveMultipleDangerousPatterns() {
            String yaml = """
                a: !!python/object:os.system 'cmd1'
                b: !!java/object:java.net.URL 'http://evil.com'
                c: !!ruby/object:Kernel 'eval'
                """;
            String sanitized = YmlSecurity.sanitize(yaml);
            assertThat(sanitized).doesNotContain("!!python/");
            assertThat(sanitized).doesNotContain("!!java/");
            assertThat(sanitized).doesNotContain("!!ruby/");
        }

        @Test
        @DisplayName("should preserve structure after sanitization")
        void shouldPreserveStructureAfterSanitization() {
            String yaml = """
                name: test
                exploit: !!python/object 'cmd'
                value: 123
                """;
            String sanitized = YmlSecurity.sanitize(yaml);
            assertThat(sanitized).contains("name: test");
            assertThat(sanitized).contains("exploit:");
            assertThat(sanitized).contains("value: 123");
        }

        @Test
        @DisplayName("should be idempotent")
        void shouldBeIdempotent() {
            String yaml = "exploit: !!python/object:os.system 'ls'";
            String sanitized1 = YmlSecurity.sanitize(yaml);
            String sanitized2 = YmlSecurity.sanitize(sanitized1);
            assertThat(sanitized1).isEqualTo(sanitized2);
        }
    }

    // ==================== isSafeType() Tests ====================

    @Nested
    @DisplayName("isSafeType method")
    class IsSafeTypeTests {

        @Nested
        @DisplayName("null and empty handling")
        class NullAndEmptyHandlingTests {

            @Test
            @DisplayName("should return false for null type name")
            void shouldReturnFalseForNullTypeName() {
                assertThat(YmlSecurity.isSafeType(null)).isFalse();
            }

            @Test
            @DisplayName("should return false for empty type name")
            void shouldReturnFalseForEmptyTypeName() {
                assertThat(YmlSecurity.isSafeType("")).isFalse();
            }
        }

        @Nested
        @DisplayName("basic safe types")
        class BasicSafeTypesTests {

            @Test
            @DisplayName("should return true for java.lang.String")
            void shouldReturnTrueForString() {
                assertThat(YmlSecurity.isSafeType("java.lang.String")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.lang.Integer")
            void shouldReturnTrueForInteger() {
                assertThat(YmlSecurity.isSafeType("java.lang.Integer")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.lang.Long")
            void shouldReturnTrueForLong() {
                assertThat(YmlSecurity.isSafeType("java.lang.Long")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.lang.Double")
            void shouldReturnTrueForDouble() {
                assertThat(YmlSecurity.isSafeType("java.lang.Double")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.lang.Float")
            void shouldReturnTrueForFloat() {
                assertThat(YmlSecurity.isSafeType("java.lang.Float")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.lang.Boolean")
            void shouldReturnTrueForBoolean() {
                assertThat(YmlSecurity.isSafeType("java.lang.Boolean")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.lang.Short")
            void shouldReturnTrueForShort() {
                assertThat(YmlSecurity.isSafeType("java.lang.Short")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.lang.Byte")
            void shouldReturnTrueForByte() {
                assertThat(YmlSecurity.isSafeType("java.lang.Byte")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.lang.Character")
            void shouldReturnTrueForCharacter() {
                assertThat(YmlSecurity.isSafeType("java.lang.Character")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.math.BigDecimal")
            void shouldReturnTrueForBigDecimal() {
                assertThat(YmlSecurity.isSafeType("java.math.BigDecimal")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.math.BigInteger")
            void shouldReturnTrueForBigInteger() {
                assertThat(YmlSecurity.isSafeType("java.math.BigInteger")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.util.Date")
            void shouldReturnTrueForDate() {
                assertThat(YmlSecurity.isSafeType("java.util.Date")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.time.LocalDate")
            void shouldReturnTrueForLocalDate() {
                assertThat(YmlSecurity.isSafeType("java.time.LocalDate")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.time.LocalDateTime")
            void shouldReturnTrueForLocalDateTime() {
                assertThat(YmlSecurity.isSafeType("java.time.LocalDateTime")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.time.LocalTime")
            void shouldReturnTrueForLocalTime() {
                assertThat(YmlSecurity.isSafeType("java.time.LocalTime")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.time.Instant")
            void shouldReturnTrueForInstant() {
                assertThat(YmlSecurity.isSafeType("java.time.Instant")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.util.UUID")
            void shouldReturnTrueForUuid() {
                assertThat(YmlSecurity.isSafeType("java.util.UUID")).isTrue();
            }
        }

        @Nested
        @DisplayName("collection and map types")
        class CollectionAndMapTypesTests {

            @Test
            @DisplayName("should return true for java.util.List")
            void shouldReturnTrueForList() {
                assertThat(YmlSecurity.isSafeType("java.util.List")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.util.Set")
            void shouldReturnTrueForSet() {
                assertThat(YmlSecurity.isSafeType("java.util.Set")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.util.Map")
            void shouldReturnTrueForMap() {
                assertThat(YmlSecurity.isSafeType("java.util.Map")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.util.ArrayList")
            void shouldReturnTrueForArrayList() {
                assertThat(YmlSecurity.isSafeType("java.util.ArrayList")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.util.LinkedList")
            void shouldReturnTrueForLinkedList() {
                assertThat(YmlSecurity.isSafeType("java.util.LinkedList")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.util.HashSet")
            void shouldReturnTrueForHashSet() {
                assertThat(YmlSecurity.isSafeType("java.util.HashSet")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.util.TreeSet")
            void shouldReturnTrueForTreeSet() {
                assertThat(YmlSecurity.isSafeType("java.util.TreeSet")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.util.HashMap")
            void shouldReturnTrueForHashMap() {
                assertThat(YmlSecurity.isSafeType("java.util.HashMap")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.util.TreeMap")
            void shouldReturnTrueForTreeMap() {
                assertThat(YmlSecurity.isSafeType("java.util.TreeMap")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.util.LinkedHashMap")
            void shouldReturnTrueForLinkedHashMap() {
                assertThat(YmlSecurity.isSafeType("java.util.LinkedHashMap")).isTrue();
            }

            @Test
            @DisplayName("should return true for generic List type")
            void shouldReturnTrueForGenericListType() {
                assertThat(YmlSecurity.isSafeType("java.util.List<String>")).isTrue();
            }

            @Test
            @DisplayName("should return true for generic Map type")
            void shouldReturnTrueForGenericMapType() {
                assertThat(YmlSecurity.isSafeType("java.util.Map<String, Object>")).isTrue();
            }
        }

        @Nested
        @DisplayName("unsafe types")
        class UnsafeTypesTests {

            @Test
            @DisplayName("should return false for java.lang.Runtime")
            void shouldReturnFalseForRuntime() {
                assertThat(YmlSecurity.isSafeType("java.lang.Runtime")).isFalse();
            }

            @Test
            @DisplayName("should return false for java.lang.ProcessBuilder")
            void shouldReturnFalseForProcessBuilder() {
                assertThat(YmlSecurity.isSafeType("java.lang.ProcessBuilder")).isFalse();
            }

            @Test
            @DisplayName("should return false for javax.script.ScriptEngine")
            void shouldReturnFalseForScriptEngine() {
                assertThat(YmlSecurity.isSafeType("javax.script.ScriptEngine")).isFalse();
            }

            @Test
            @DisplayName("should return false for java.io.File")
            void shouldReturnFalseForFile() {
                assertThat(YmlSecurity.isSafeType("java.io.File")).isFalse();
            }

            @Test
            @DisplayName("should return false for java.net.URL")
            void shouldReturnFalseForUrl() {
                assertThat(YmlSecurity.isSafeType("java.net.URL")).isFalse();
            }

            @Test
            @DisplayName("should return false for custom class")
            void shouldReturnFalseForCustomClass() {
                assertThat(YmlSecurity.isSafeType("com.example.CustomClass")).isFalse();
            }

            @Test
            @DisplayName("should return false for arbitrary class name")
            void shouldReturnFalseForArbitraryClassName() {
                assertThat(YmlSecurity.isSafeType("org.malicious.Exploit")).isFalse();
            }
        }
    }

    // ==================== Integration Tests ====================

    @Nested
    @DisplayName("Integration scenarios")
    class IntegrationTests {

        @Test
        @DisplayName("should validate and create safe config for typical use case")
        void shouldValidateAndCreateSafeConfigForTypicalUseCase() {
            String yaml = """
                database:
                  host: localhost
                  port: 5432
                  name: mydb
                server:
                  port: 8080
                """;

            // Validate should pass
            assertThatNoException().isThrownBy(() -> YmlSecurity.validate(yaml));

            // Should not contain dangerous patterns
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isFalse();

            // Safe config should be created successfully
            YmlConfig config = YmlSecurity.createSafeConfig();
            assertThat(config.isSafeMode()).isTrue();
        }

        @Test
        @DisplayName("should detect and sanitize YAML bomb attempt")
        void shouldDetectAndSanitizeYamlBombAttempt() {
            StringBuilder yaml = new StringBuilder();
            yaml.append("a: &a [\"lol\"]\n");
            for (int i = 0; i < 60; i++) {
                yaml.append("b").append(i).append(": *a\n");
            }

            // Should detect too many aliases
            assertThatThrownBy(() -> YmlSecurity.validate(yaml.toString()))
                .isInstanceOf(YmlSecurityException.class);

            // Count should show excessive aliases
            assertThat(YmlSecurity.countAliases(yaml.toString())).isGreaterThan(50);
        }

        @Test
        @DisplayName("should detect and sanitize code injection attempt")
        void shouldDetectAndSanitizeCodeInjectionAttempt() {
            String yaml = """
                innocent: data
                exploit: !!python/object:subprocess.call ['rm', '-rf', '/']
                more: data
                """;

            // Should detect dangerous patterns
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isTrue();

            // Validate should fail
            assertThatThrownBy(() -> YmlSecurity.validate(yaml))
                .isInstanceOf(YmlSecurityException.class);

            // Sanitize should remove dangerous pattern
            String sanitized = YmlSecurity.sanitize(yaml);
            assertThat(YmlSecurity.containsDangerousPatterns(sanitized)).isFalse();
        }

        @Test
        @DisplayName("should work with safe config for strict parsing")
        void shouldWorkWithSafeConfigForStrictParsing() {
            YmlConfig config = YmlSecurity.createSafeConfig(1024, 10);

            assertThat(config.isSafeMode()).isTrue();
            assertThat(config.getMaxDocumentSize()).isEqualTo(1024);
            assertThat(config.getMaxAliasesForCollections()).isEqualTo(10);
            assertThat(config.isAllowDuplicateKeys()).isFalse();
        }
    }

    // ==================== Edge Cases ====================

    @Nested
    @DisplayName("Edge cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle whitespace-only content")
        void shouldHandleWhitespaceOnlyContent() {
            String yaml = "   \n\t\n   ";
            assertThatNoException().isThrownBy(() -> YmlSecurity.validate(yaml));
            assertThat(YmlSecurity.containsDangerousPatterns(yaml)).isFalse();
        }

        @Test
        @DisplayName("should handle content with special characters")
        void shouldHandleContentWithSpecialCharacters() {
            String yaml = "message: 'Hello, World! @#$%^&*()_+='";
            assertThatNoException().isThrownBy(() -> YmlSecurity.validate(yaml));
        }

        @Test
        @DisplayName("should handle Unicode content")
        void shouldHandleUnicodeContent() {
            String yaml = """
                greeting: 你好世界
                emoji: \uD83D\uDE00
                japanese: こんにちは
                """;
            assertThatNoException().isThrownBy(() -> YmlSecurity.validate(yaml));
        }

        @Test
        @DisplayName("should handle content at exact size boundary")
        void shouldHandleContentAtExactSizeBoundary() {
            String yaml = "x".repeat(100);
            // At exactly the limit - should pass
            assertThatNoException().isThrownBy(() ->
                YmlSecurity.validate(yaml, 100, 10));
        }

        @Test
        @DisplayName("should handle content at exact alias boundary")
        void shouldHandleContentAtExactAliasBoundary() {
            StringBuilder yaml = new StringBuilder();
            yaml.append("anchor: &anchor value\n");
            for (int i = 0; i < 5; i++) {
                yaml.append("ref").append(i).append(": *anchor\n");
            }
            // Exactly 5 aliases with limit of 5
            assertThatNoException().isThrownBy(() ->
                YmlSecurity.validate(yaml.toString(), 10000, 5));
        }

        @Test
        @DisplayName("should handle deeply nested YAML with only anchors")
        void shouldHandleDeeplyNestedYamlWithOnlyAnchors() {
            String yaml = """
                level1: &l1
                  level2: &l2
                    level3: &l3
                      value: test
                """;
            assertThatNoException().isThrownBy(() -> YmlSecurity.validate(yaml));
            assertThat(YmlSecurity.countAnchors(yaml)).isEqualTo(3);
            assertThat(YmlSecurity.countAliases(yaml)).isZero();
        }

        @Test
        @DisplayName("should handle case sensitivity in type checking")
        void shouldHandleCaseSensitivityInTypeChecking() {
            // Case sensitive - should not match
            assertThat(YmlSecurity.isSafeType("JAVA.LANG.STRING")).isFalse();
            assertThat(YmlSecurity.isSafeType("Java.Lang.String")).isFalse();
            // Correct case - should match
            assertThat(YmlSecurity.isSafeType("java.lang.String")).isTrue();
        }
    }
}
