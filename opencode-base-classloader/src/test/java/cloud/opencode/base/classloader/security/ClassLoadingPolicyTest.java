package cloud.opencode.base.classloader.security;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ClassLoadingPolicy
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V2.0.0
 */
@DisplayName("ClassLoadingPolicy Tests")
class ClassLoadingPolicyTest {

    private static final byte[] SAMPLE_BYTECODE = new byte[]{1, 2, 3, 4};

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build empty policy with defaults")
        void shouldBuildEmptyPolicy() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder().build();

            assertThat(policy.allowedPackages()).isEmpty();
            assertThat(policy.deniedPackages()).isEmpty();
            assertThat(policy.maxLoadedClasses()).isZero();
            assertThat(policy.maxBytecodeSize()).isZero();
            assertThat(policy.bytecodeVerifier()).isNull();
        }

        @Test
        @DisplayName("Should set allowed packages")
        void shouldSetAllowedPackages() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .allowedPackages(Set.of("com.example.", "org.safe."))
                    .build();

            assertThat(policy.allowedPackages()).containsExactlyInAnyOrder("com.example.", "org.safe.");
        }

        @Test
        @DisplayName("Should add single allowed package")
        void shouldAddSingleAllowedPackage() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addAllowedPackage("com.example.")
                    .addAllowedPackage("org.safe.")
                    .build();

            assertThat(policy.allowedPackages()).containsExactlyInAnyOrder("com.example.", "org.safe.");
        }

        @Test
        @DisplayName("Should set denied packages")
        void shouldSetDeniedPackages() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .deniedPackages(Set.of("sun.", "com.sun."))
                    .build();

            assertThat(policy.deniedPackages()).containsExactlyInAnyOrder("sun.", "com.sun.");
        }

        @Test
        @DisplayName("Should add single denied package")
        void shouldAddSingleDeniedPackage() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addDeniedPackage("sun.")
                    .build();

            assertThat(policy.deniedPackages()).containsExactly("sun.");
        }

        @Test
        @DisplayName("Should set maxLoadedClasses")
        void shouldSetMaxLoadedClasses() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .maxLoadedClasses(100)
                    .build();

            assertThat(policy.maxLoadedClasses()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should reject negative maxLoadedClasses")
        void shouldRejectNegativeMaxLoadedClasses() {
            assertThatThrownBy(() -> ClassLoadingPolicy.builder().maxLoadedClasses(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maxLoadedClasses");
        }

        @Test
        @DisplayName("Should set maxBytecodeSize")
        void shouldSetMaxBytecodeSize() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .maxBytecodeSize(65536)
                    .build();

            assertThat(policy.maxBytecodeSize()).isEqualTo(65536);
        }

        @Test
        @DisplayName("Should reject negative maxBytecodeSize")
        void shouldRejectNegativeMaxBytecodeSize() {
            assertThatThrownBy(() -> ClassLoadingPolicy.builder().maxBytecodeSize(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maxBytecodeSize");
        }

        @Test
        @DisplayName("Should set bytecodeVerifier")
        void shouldSetBytecodeVerifier() {
            BytecodeVerifier verifier = b -> true;
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .bytecodeVerifier(verifier)
                    .build();

            assertThat(policy.bytecodeVerifier()).isSameAs(verifier);
        }

        @Test
        @DisplayName("Should reject null allowedPackages set")
        void shouldRejectNullAllowedPackagesSet() {
            assertThatThrownBy(() -> ClassLoadingPolicy.builder().allowedPackages(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject null deniedPackages set")
        void shouldRejectNullDeniedPackagesSet() {
            assertThatThrownBy(() -> ClassLoadingPolicy.builder().deniedPackages(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject null addAllowedPackage")
        void shouldRejectNullAddAllowedPackage() {
            assertThatThrownBy(() -> ClassLoadingPolicy.builder().addAllowedPackage(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject null addDeniedPackage")
        void shouldRejectNullAddDeniedPackage() {
            assertThatThrownBy(() -> ClassLoadingPolicy.builder().addDeniedPackage(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("allowedPackages should replace previous values")
        void allowedPackagesShouldReplacePrevious() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addAllowedPackage("old.")
                    .allowedPackages(Set.of("new."))
                    .build();

            assertThat(policy.allowedPackages()).containsExactly("new.");
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Allowed packages should be unmodifiable")
        void allowedPackagesShouldBeUnmodifiable() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addAllowedPackage("com.example.")
                    .build();

            assertThatThrownBy(() -> policy.allowedPackages().add("hack."))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Denied packages should be unmodifiable")
        void deniedPackagesShouldBeUnmodifiable() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addDeniedPackage("sun.")
                    .build();

            assertThatThrownBy(() -> policy.deniedPackages().add("hack."))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("checkAllowed - Denied Packages Tests")
    class DeniedPackagesTests {

        @Test
        @DisplayName("Should throw for denied package")
        void shouldThrowForDeniedPackage() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addDeniedPackage("sun.")
                    .build();

            assertThatThrownBy(() -> policy.checkAllowed("sun.misc.Unsafe", SAMPLE_BYTECODE, 0))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("blacklisted")
                    .hasMessageContaining("sun.");
        }

        @Test
        @DisplayName("Should allow class not in denied packages")
        void shouldAllowClassNotInDenied() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addDeniedPackage("sun.")
                    .build();

            assertThatCode(() -> policy.checkAllowed("com.example.MyClass", SAMPLE_BYTECODE, 0))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("checkAllowed - Allowed Packages Tests")
    class AllowedPackagesTests {

        @Test
        @DisplayName("Should allow class in whitelist")
        void shouldAllowClassInWhitelist() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addAllowedPackage("com.example.")
                    .build();

            assertThatCode(() -> policy.checkAllowed("com.example.MyClass", SAMPLE_BYTECODE, 0))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw for class not in whitelist")
        void shouldThrowForClassNotInWhitelist() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addAllowedPackage("com.example.")
                    .build();

            assertThatThrownBy(() -> policy.checkAllowed("org.other.MyClass", SAMPLE_BYTECODE, 0))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("not in any allowed package");
        }

        @Test
        @DisplayName("Should allow any class when whitelist is empty")
        void shouldAllowAnyWhenWhitelistEmpty() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder().build();

            assertThatCode(() -> policy.checkAllowed("any.Class", SAMPLE_BYTECODE, 0))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("checkAllowed - Package Prefix Boundary Tests")
    class PackagePrefixBoundaryTests {

        @Test
        @DisplayName("Deny prefix without trailing dot should block exact package match")
        void denyWithoutDotBlocksExactPackage() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addDeniedPackage("com.internal")
                    .build();

            assertThatThrownBy(() -> policy.checkNameAllowed("com.internal.Secret", 0))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("blacklisted");
        }

        @Test
        @DisplayName("Deny prefix without trailing dot should NOT block similar-named package")
        void denyWithoutDotDoesNotBlockSimilarName() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addDeniedPackage("com.internal")
                    .build();

            // "com.internalhack" is a different package — must NOT be blocked
            assertThatCode(() -> policy.checkNameAllowed("com.internalhack.Exploit", 0))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Allow prefix without trailing dot should allow exact package match")
        void allowWithoutDotAllowsExactPackage() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addAllowedPackage("com.example")
                    .build();

            assertThatCode(() -> policy.checkNameAllowed("com.example.MyClass", 0))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Allow prefix without trailing dot should NOT allow similar-named package")
        void allowWithoutDotBlocksSimilarName() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addAllowedPackage("com.example")
                    .build();

            // "com.exampleEvil" is a different package — must be blocked
            assertThatThrownBy(() -> policy.checkNameAllowed("com.exampleEvil.Malicious", 0))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("not in any allowed package");
        }

        @Test
        @DisplayName("Allow prefix with trailing dot should still work correctly")
        void allowWithDotWorksCorrectly() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addAllowedPackage("com.example.")
                    .build();

            assertThatCode(() -> policy.checkNameAllowed("com.example.Safe", 0))
                    .doesNotThrowAnyException();
            assertThatThrownBy(() -> policy.checkNameAllowed("com.other.Bad", 0))
                    .isInstanceOf(OpenClassLoaderException.class);
        }

        @Test
        @DisplayName("Exact class name match should be allowed")
        void exactClassNameMatch() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addAllowedPackage("com.example.SpecificClass")
                    .build();

            assertThatCode(() -> policy.checkNameAllowed("com.example.SpecificClass", 0))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("checkAllowed - Max Loaded Classes Tests")
    class MaxLoadedClassesTests {

        @Test
        @DisplayName("Should throw when max loaded classes reached")
        void shouldThrowWhenMaxReached() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .maxLoadedClasses(10)
                    .build();

            assertThatThrownBy(() -> policy.checkAllowed("com.example.MyClass", SAMPLE_BYTECODE, 10))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("max loaded classes");
        }

        @Test
        @DisplayName("Should allow when below max")
        void shouldAllowWhenBelowMax() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .maxLoadedClasses(10)
                    .build();

            assertThatCode(() -> policy.checkAllowed("com.example.MyClass", SAMPLE_BYTECODE, 9))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow any count when maxLoadedClasses is 0")
        void shouldAllowAnyCountWhenZero() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .maxLoadedClasses(0)
                    .build();

            assertThatCode(() -> policy.checkAllowed("com.example.MyClass", SAMPLE_BYTECODE, 999999))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw when count exceeds max")
        void shouldThrowWhenCountExceedsMax() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .maxLoadedClasses(5)
                    .build();

            assertThatThrownBy(() -> policy.checkAllowed("com.example.MyClass", SAMPLE_BYTECODE, 100))
                    .isInstanceOf(OpenClassLoaderException.class);
        }
    }

    @Nested
    @DisplayName("checkAllowed - Max Bytecode Size Tests")
    class MaxBytecodeSizeTests {

        @Test
        @DisplayName("Should throw when bytecode exceeds max size")
        void shouldThrowWhenBytecodeTooLarge() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .maxBytecodeSize(2)
                    .build();

            assertThatThrownBy(() -> policy.checkAllowed("com.example.MyClass", new byte[]{1, 2, 3}, 0))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("bytecode size")
                    .hasMessageContaining("exceeds max");
        }

        @Test
        @DisplayName("Should allow when bytecode within limit")
        void shouldAllowWhenWithinLimit() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .maxBytecodeSize(100)
                    .build();

            assertThatCode(() -> policy.checkAllowed("com.example.MyClass", SAMPLE_BYTECODE, 0))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow bytecode equal to max size")
        void shouldAllowBytecodeEqualToMax() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .maxBytecodeSize(4)
                    .build();

            assertThatCode(() -> policy.checkAllowed("com.example.MyClass", SAMPLE_BYTECODE, 0))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow any size when maxBytecodeSize is 0")
        void shouldAllowAnySizeWhenZero() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .maxBytecodeSize(0)
                    .build();

            assertThatCode(() -> policy.checkAllowed("com.example.MyClass", new byte[1_000_000], 0))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("checkAllowed - Bytecode Verifier Tests")
    class BytecodeVerifierTests {

        @Test
        @DisplayName("Should throw when verifier returns false")
        void shouldThrowWhenVerifierReturnsFalse() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .bytecodeVerifier(b -> false)
                    .build();

            assertThatThrownBy(() -> policy.checkAllowed("com.example.MyClass", SAMPLE_BYTECODE, 0))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("bytecode verification failed");
        }

        @Test
        @DisplayName("Should allow when verifier returns true")
        void shouldAllowWhenVerifierReturnsTrue() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .bytecodeVerifier(b -> true)
                    .build();

            assertThatCode(() -> policy.checkAllowed("com.example.MyClass", SAMPLE_BYTECODE, 0))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should skip verification when no verifier configured")
        void shouldSkipWhenNoVerifier() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder().build();

            assertThatCode(() -> policy.checkAllowed("com.example.MyClass", SAMPLE_BYTECODE, 0))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("checkAllowed - Null Argument Tests")
    class NullArgumentTests {

        @Test
        @DisplayName("Should reject null className")
        void shouldRejectNullClassName() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder().build();

            assertThatThrownBy(() -> policy.checkAllowed(null, SAMPLE_BYTECODE, 0))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("className");
        }

        @Test
        @DisplayName("checkAllowed should accept null bytecode (skips bytecode checks)")
        void shouldAcceptNullBytecodeInCheckAllowed() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder().build();

            // null bytecode skips bytecode-specific checks
            assertThatCode(() -> policy.checkAllowed("com.example.MyClass", null, 0))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("checkBytecodeAllowed should reject null bytecode")
        void shouldRejectNullBytecodeInCheckBytecodeAllowed() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder().build();

            assertThatThrownBy(() -> policy.checkBytecodeAllowed("com.example.MyClass", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("bytecode");
        }
    }

    @Nested
    @DisplayName("checkAllowed - Priority Order Tests")
    class PriorityOrderTests {

        @Test
        @DisplayName("Denied check should take priority over allowed check")
        void deniedShouldTakePriorityOverAllowed() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addAllowedPackage("com.")
                    .addDeniedPackage("com.evil.")
                    .build();

            // com.evil. matches denied — should throw even though com. is allowed
            assertThatThrownBy(() -> policy.checkAllowed("com.evil.Malware", SAMPLE_BYTECODE, 0))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("blacklisted");
        }

        @Test
        @DisplayName("All checks pass for valid class")
        void allChecksShouldPassForValidClass() {
            ClassLoadingPolicy policy = ClassLoadingPolicy.builder()
                    .addAllowedPackage("com.example.")
                    .addDeniedPackage("com.evil.")
                    .maxLoadedClasses(100)
                    .maxBytecodeSize(65536)
                    .bytecodeVerifier(b -> true)
                    .build();

            assertThatCode(() -> policy.checkAllowed("com.example.Safe", SAMPLE_BYTECODE, 50))
                    .doesNotThrowAnyException();
        }
    }
}
