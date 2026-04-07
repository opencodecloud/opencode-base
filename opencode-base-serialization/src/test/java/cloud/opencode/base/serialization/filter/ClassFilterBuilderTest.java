package cloud.opencode.base.serialization.filter;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ClassFilterBuilderTest Tests
 * ClassFilterBuilderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.3
 */
@DisplayName("ClassFilterBuilder Tests")
class ClassFilterBuilderTest {

    @Nested
    @DisplayName("Allow Class Tests")
    class AllowClassTests {

        @Test
        @DisplayName("should allow explicitly allowed class")
        void shouldAllowExplicitlyAllowedClass() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultDeny()
                    .allow("java.lang.String")
                    .build();

            assertThat(filter.isAllowed("java.lang.String")).isTrue();
        }

        @Test
        @DisplayName("should allow multiple explicitly allowed classes")
        void shouldAllowMultipleExplicitlyAllowedClasses() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultDeny()
                    .allow("java.lang.String")
                    .allow("java.lang.Integer")
                    .allow("java.lang.Long")
                    .build();

            assertThat(filter.isAllowed("java.lang.String")).isTrue();
            assertThat(filter.isAllowed("java.lang.Integer")).isTrue();
            assertThat(filter.isAllowed("java.lang.Long")).isTrue();
        }

        @Test
        @DisplayName("should deny non-allowed class when defaultDeny")
        void shouldDenyNonAllowedClassWhenDefaultDeny() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultDeny()
                    .allow("java.lang.String")
                    .build();

            assertThat(filter.isAllowed("java.lang.Integer")).isFalse();
        }
    }

    @Nested
    @DisplayName("Deny Class Tests")
    class DenyClassTests {

        @Test
        @DisplayName("should deny explicitly denied class")
        void shouldDenyExplicitlyDeniedClass() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultAllow()
                    .deny("java.lang.Runtime")
                    .build();

            assertThat(filter.isAllowed("java.lang.Runtime")).isFalse();
        }

        @Test
        @DisplayName("should deny multiple explicitly denied classes")
        void shouldDenyMultipleExplicitlyDeniedClasses() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultAllow()
                    .deny("java.lang.Runtime")
                    .deny("java.lang.ProcessBuilder")
                    .build();

            assertThat(filter.isAllowed("java.lang.Runtime")).isFalse();
            assertThat(filter.isAllowed("java.lang.ProcessBuilder")).isFalse();
        }

        @Test
        @DisplayName("should allow non-denied class when defaultAllow")
        void shouldAllowNonDeniedClassWhenDefaultAllow() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultAllow()
                    .deny("java.lang.Runtime")
                    .build();

            assertThat(filter.isAllowed("java.lang.String")).isTrue();
        }
    }

    @Nested
    @DisplayName("Allow Package Tests")
    class AllowPackageTests {

        @Test
        @DisplayName("should allow classes from allowed package")
        void shouldAllowClassesFromAllowedPackage() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultDeny()
                    .allowPackage("java.lang")
                    .build();

            assertThat(filter.isAllowed("java.lang.String")).isTrue();
            assertThat(filter.isAllowed("java.lang.Integer")).isTrue();
        }

        @Test
        @DisplayName("should allow classes from sub-packages")
        void shouldAllowClassesFromSubPackages() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultDeny()
                    .allowPackage("java.util")
                    .build();

            assertThat(filter.isAllowed("java.util.List")).isTrue();
            assertThat(filter.isAllowed("java.util.Map")).isTrue();
        }

        @Test
        @DisplayName("should deny classes from non-allowed packages")
        void shouldDenyClassesFromNonAllowedPackages() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultDeny()
                    .allowPackage("java.lang")
                    .build();

            assertThat(filter.isAllowed("java.util.List")).isFalse();
        }

        @Test
        @DisplayName("should not match partial package names")
        void shouldNotMatchPartialPackageNames() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultDeny()
                    .allowPackage("java.lang")
                    .build();

            // "java.language.Foo" should not match "java.lang" package
            assertThat(filter.isAllowed("java.language.Foo")).isFalse();
        }
    }

    @Nested
    @DisplayName("Deny Package Tests")
    class DenyPackageTests {

        @Test
        @DisplayName("should deny classes from denied package")
        void shouldDenyClassesFromDeniedPackage() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultAllow()
                    .denyPackage("java.lang.reflect")
                    .build();

            assertThat(filter.isAllowed("java.lang.reflect.Method")).isFalse();
            assertThat(filter.isAllowed("java.lang.reflect.Field")).isFalse();
        }

        @Test
        @DisplayName("should allow classes from non-denied packages")
        void shouldAllowClassesFromNonDeniedPackages() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultAllow()
                    .denyPackage("java.lang.reflect")
                    .build();

            assertThat(filter.isAllowed("java.lang.String")).isTrue();
        }
    }

    @Nested
    @DisplayName("Pattern Tests")
    class PatternTests {

        @Test
        @DisplayName("should allow classes matching allowed pattern")
        void shouldAllowClassesMatchingAllowedPattern() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultDeny()
                    .allowPattern("java\\.lang\\..*")
                    .build();

            assertThat(filter.isAllowed("java.lang.String")).isTrue();
            assertThat(filter.isAllowed("java.lang.Integer")).isTrue();
        }

        @Test
        @DisplayName("should deny classes matching denied pattern")
        void shouldDenyClassesMatchingDeniedPattern() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultAllow()
                    .denyPattern(".*Runtime.*")
                    .build();

            assertThat(filter.isAllowed("java.lang.Runtime")).isFalse();
        }

        @Test
        @DisplayName("should support complex regex patterns")
        void shouldSupportComplexRegexPatterns() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultDeny()
                    .allowPattern("java\\.(lang|util)\\..*")
                    .build();

            assertThat(filter.isAllowed("java.lang.String")).isTrue();
            assertThat(filter.isAllowed("java.util.List")).isTrue();
        }
    }

    @Nested
    @DisplayName("Default Policy Tests")
    class DefaultPolicyTests {

        @Test
        @DisplayName("defaultAllow should allow unspecified classes")
        void defaultAllowShouldAllowUnspecifiedClasses() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultAllow()
                    .build();

            assertThat(filter.isAllowed("java.lang.String")).isTrue();
            assertThat(filter.isAllowed("java.lang.Object")).isTrue();
        }

        @Test
        @DisplayName("defaultDeny should deny unspecified classes")
        void defaultDenyShouldDenyUnspecifiedClasses() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultDeny()
                    .build();

            assertThat(filter.isAllowed("java.lang.String")).isFalse();
            assertThat(filter.isAllowed("java.lang.Object")).isFalse();
        }
    }

    @Nested
    @DisplayName("Null Handling Tests")
    class NullHandlingTests {

        @Test
        @DisplayName("should deny null class name")
        void shouldDenyNullClassName() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultAllow()
                    .build();

            assertThat(filter.isAllowed(null)).isFalse();
        }

        @Test
        @DisplayName("should reject null class name in allow")
        void shouldRejectNullClassNameInAllow() {
            assertThatThrownBy(() -> new ClassFilterBuilder().allow((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject null class name in deny")
        void shouldRejectNullClassNameInDeny() {
            assertThatThrownBy(() -> new ClassFilterBuilder().deny((String) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Combined Rules Tests")
    class CombinedTests {

        @Test
        @DisplayName("should handle allow package with deny specific class")
        void shouldHandleAllowPackageWithDenySpecificClass() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultDeny()
                    .allowPackage("java.lang")
                    .deny("java.lang.Runtime")
                    .build();

            assertThat(filter.isAllowed("java.lang.String")).isTrue();
            assertThat(filter.isAllowed("java.lang.Runtime")).isFalse();
        }

        @Test
        @DisplayName("should handle deny package with allow specific class")
        void shouldHandleDenyPackageWithAllowSpecificClass() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultAllow()
                    .denyPackage("java.lang.reflect")
                    .allow("java.lang.reflect.Method")
                    .build();

            // Deny rules are evaluated first, so package deny overrides class allow
            // Other reflect classes should be denied
            assertThat(filter.isAllowed("java.lang.reflect.Field")).isFalse();
        }

        @Test
        @DisplayName("should handle mixed allow and deny rules")
        void shouldHandleMixedAllowAndDenyRules() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultDeny()
                    .allowPackage("java.lang")
                    .allowPackage("java.util")
                    .deny("java.lang.Runtime")
                    .deny("java.lang.ProcessBuilder")
                    .build();

            assertThat(filter.isAllowed("java.lang.String")).isTrue();
            assertThat(filter.isAllowed("java.util.List")).isTrue();
            assertThat(filter.isAllowed("java.lang.Runtime")).isFalse();
            assertThat(filter.isAllowed("java.lang.ProcessBuilder")).isFalse();
        }

        @Test
        @DisplayName("should handle allow pattern with deny class")
        void shouldHandleAllowPatternWithDenyClass() {
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultDeny()
                    .allowPattern("java\\.lang\\..*")
                    .deny("java.lang.Runtime")
                    .build();

            assertThat(filter.isAllowed("java.lang.String")).isTrue();
            assertThat(filter.isAllowed("java.lang.Runtime")).isFalse();
        }

        @Test
        @DisplayName("deny rules should take precedence over allow rules")
        void denyRulesShouldTakePrecedence() {
            // Deny by package should override allow by exact class
            ClassFilter filter = new ClassFilterBuilder()
                    .defaultDeny()
                    .allow("javax.naming.InitialContext")
                    .denyPackage("javax.naming")
                    .build();

            // Deny is checked first, so the package deny wins
            assertThat(filter.isAllowed("javax.naming.InitialContext")).isFalse();
        }
    }
}
