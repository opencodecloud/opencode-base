package cloud.opencode.base.serialization.filter;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultClassFilterTest Tests
 * DefaultClassFilterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.3
 */
@DisplayName("DefaultClassFilter Tests")
class DefaultClassFilterTest {

    @Nested
    @DisplayName("Secure Filter Tests")
    class SecureFilterTests {

        private ClassFilter secureFilter;

        @BeforeEach
        void setUp() {
            secureFilter = DefaultClassFilter.secure();
        }

        @Test
        @DisplayName("should deny Runtime class")
        void shouldDenyRuntimeClass() {
            assertThat(secureFilter.isAllowed("java.lang.Runtime")).isFalse();
        }

        @Test
        @DisplayName("should deny ProcessBuilder class")
        void shouldDenyProcessBuilderClass() {
            assertThat(secureFilter.isAllowed("java.lang.ProcessBuilder")).isFalse();
        }

        @Test
        @DisplayName("should deny ClassLoader class")
        void shouldDenyClassLoaderClass() {
            assertThat(secureFilter.isAllowed("java.lang.ClassLoader")).isFalse();
        }

        @Test
        @DisplayName("should deny reflection classes")
        void shouldDenyReflectionClasses() {
            assertThat(secureFilter.isAllowed("java.lang.reflect.Method")).isFalse();
            assertThat(secureFilter.isAllowed("java.lang.reflect.Field")).isFalse();
            assertThat(secureFilter.isAllowed("java.lang.reflect.Constructor")).isFalse();
        }

        @Test
        @DisplayName("should allow standard safe classes")
        void shouldAllowStandardSafeClasses() {
            assertThat(secureFilter.isAllowed("java.lang.String")).isTrue();
            assertThat(secureFilter.isAllowed("java.lang.Integer")).isTrue();
            assertThat(secureFilter.isAllowed("java.lang.Long")).isTrue();
            assertThat(secureFilter.isAllowed("java.lang.Boolean")).isTrue();
        }

        @Test
        @DisplayName("should allow collection classes")
        void shouldAllowCollectionClasses() {
            assertThat(secureFilter.isAllowed("java.util.List")).isTrue();
            assertThat(secureFilter.isAllowed("java.util.Map")).isTrue();
            assertThat(secureFilter.isAllowed("java.util.ArrayList")).isTrue();
            assertThat(secureFilter.isAllowed("java.util.HashMap")).isTrue();
        }

        @Test
        @DisplayName("should deny Thread class")
        void shouldDenyThreadClass() {
            assertThat(secureFilter.isAllowed("java.lang.Thread")).isFalse();
        }
    }

    @Nested
    @DisplayName("Strict Filter Tests")
    class StrictFilterTests {

        private ClassFilter strictFilter;

        @BeforeEach
        void setUp() {
            strictFilter = DefaultClassFilter.strict();
        }

        @Test
        @DisplayName("should allow String class")
        void shouldAllowStringClass() {
            assertThat(strictFilter.isAllowed("java.lang.String")).isTrue();
        }

        @Test
        @DisplayName("should allow primitive wrapper types")
        void shouldAllowPrimitiveWrapperTypes() {
            assertThat(strictFilter.isAllowed("java.lang.Integer")).isTrue();
            assertThat(strictFilter.isAllowed("java.lang.Long")).isTrue();
            assertThat(strictFilter.isAllowed("java.lang.Double")).isTrue();
            assertThat(strictFilter.isAllowed("java.lang.Float")).isTrue();
            assertThat(strictFilter.isAllowed("java.lang.Boolean")).isTrue();
            assertThat(strictFilter.isAllowed("java.lang.Byte")).isTrue();
            assertThat(strictFilter.isAllowed("java.lang.Short")).isTrue();
            assertThat(strictFilter.isAllowed("java.lang.Character")).isTrue();
        }

        @Test
        @DisplayName("should allow standard collection types")
        void shouldAllowStandardCollectionTypes() {
            assertThat(strictFilter.isAllowed("java.util.ArrayList")).isTrue();
            assertThat(strictFilter.isAllowed("java.util.HashMap")).isTrue();
            assertThat(strictFilter.isAllowed("java.util.HashSet")).isTrue();
        }

        @Test
        @DisplayName("should deny dangerous classes")
        void shouldDenyDangerousClasses() {
            assertThat(strictFilter.isAllowed("java.lang.Runtime")).isFalse();
            assertThat(strictFilter.isAllowed("java.lang.ProcessBuilder")).isFalse();
            assertThat(strictFilter.isAllowed("java.lang.ClassLoader")).isFalse();
        }

        @Test
        @DisplayName("should deny reflection classes")
        void shouldDenyReflectionClasses() {
            assertThat(strictFilter.isAllowed("java.lang.reflect.Method")).isFalse();
            assertThat(strictFilter.isAllowed("java.lang.reflect.Field")).isFalse();
        }

        @Test
        @DisplayName("should deny Thread class")
        void shouldDenyThreadClass() {
            assertThat(strictFilter.isAllowed("java.lang.Thread")).isFalse();
        }

        @Test
        @DisplayName("should deny System class")
        void shouldDenySystemClass() {
            assertThat(strictFilter.isAllowed("java.lang.System")).isFalse();
        }

        @Test
        @DisplayName("strict should be more restrictive than secure")
        void strictShouldBeMoreRestrictiveThanSecure() {
            ClassFilter secure = DefaultClassFilter.secure();
            // Both should deny dangerous classes
            assertThat(strictFilter.isAllowed("java.lang.Runtime")).isFalse();
            assertThat(secure.isAllowed("java.lang.Runtime")).isFalse();
            // Both should allow basic types
            assertThat(strictFilter.isAllowed("java.lang.String")).isTrue();
            assertThat(secure.isAllowed("java.lang.String")).isTrue();
        }
    }
}
