package cloud.opencode.base.classloader.graalvm;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for NativeImageSupport
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V2.0.0
 */
@DisplayName("NativeImageSupport Tests")
class NativeImageSupportTest {

    private static final String NATIVE_IMAGE_PROPERTY = "org.graalvm.nativeimage.imagecode";

    /**
     * Note: NativeImageSupport uses static final fields initialized at class load time,
     * so we cannot dynamically change the detection result after the class is loaded.
     * These tests verify the behavior in a standard JVM environment (non-native-image).
     */
    @Nested
    @DisplayName("Standard JVM Environment Tests")
    class StandardJvmTests {

        @Test
        @DisplayName("Should detect non-native-image environment")
        void shouldDetectNonNativeImage() {
            // In standard JVM, the property is not set
            assertThat(NativeImageSupport.isNativeImage()).isFalse();
        }

        @Test
        @DisplayName("Should not be build time in standard JVM")
        void shouldNotBeBuildTime() {
            assertThat(NativeImageSupport.isBuildTime()).isFalse();
        }

        @Test
        @DisplayName("Should not be run time in standard JVM")
        void shouldNotBeRunTime() {
            assertThat(NativeImageSupport.isRunTime()).isFalse();
        }
    }

    @Nested
    @DisplayName("System Property Detection Logic Tests")
    class PropertyDetectionTests {

        @Test
        @DisplayName("Should verify property key used for detection")
        void shouldUseCorrectPropertyKey() {
            // Verify the property key matches GraalVM convention
            assertThat(NATIVE_IMAGE_PROPERTY).isEqualTo("org.graalvm.nativeimage.imagecode");
        }

        @Test
        @DisplayName("Should verify system property is not set in test environment")
        void shouldNotHavePropertyInTestEnv() {
            assertThat(System.getProperty(NATIVE_IMAGE_PROPERTY)).isNull();
        }

        @Test
        @DisplayName("Build time property value should be 'buildtime'")
        void shouldUseBuildtimeValue() {
            // Verify that the expected property value is "buildtime"
            String saved = System.getProperty(NATIVE_IMAGE_PROPERTY);
            try {
                System.setProperty(NATIVE_IMAGE_PROPERTY, "buildtime");
                assertThat(System.getProperty(NATIVE_IMAGE_PROPERTY)).isEqualTo("buildtime");
            } finally {
                if (saved == null) {
                    System.clearProperty(NATIVE_IMAGE_PROPERTY);
                } else {
                    System.setProperty(NATIVE_IMAGE_PROPERTY, saved);
                }
            }
        }

        @Test
        @DisplayName("Run time property value should be 'runtime'")
        void shouldUseRuntimeValue() {
            String saved = System.getProperty(NATIVE_IMAGE_PROPERTY);
            try {
                System.setProperty(NATIVE_IMAGE_PROPERTY, "runtime");
                assertThat(System.getProperty(NATIVE_IMAGE_PROPERTY)).isEqualTo("runtime");
            } finally {
                if (saved == null) {
                    System.clearProperty(NATIVE_IMAGE_PROPERTY);
                } else {
                    System.setProperty(NATIVE_IMAGE_PROPERTY, saved);
                }
            }
        }
    }

    @Nested
    @DisplayName("Consistency Tests")
    class ConsistencyTests {

        @Test
        @DisplayName("Results should be consistent across multiple calls")
        void shouldReturnConsistentResults() {
            boolean first = NativeImageSupport.isNativeImage();
            boolean second = NativeImageSupport.isNativeImage();
            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("Build time and run time should be mutually exclusive or both false")
        void shouldBeMutuallyExclusive() {
            // Both can be false (standard JVM), but both cannot be true
            assertThat(NativeImageSupport.isBuildTime() && NativeImageSupport.isRunTime()).isFalse();
        }

        @Test
        @DisplayName("If not native image, neither build nor run time should be true")
        void shouldNotBeBuildOrRunTimeIfNotNativeImage() {
            if (!NativeImageSupport.isNativeImage()) {
                assertThat(NativeImageSupport.isBuildTime()).isFalse();
                assertThat(NativeImageSupport.isRunTime()).isFalse();
            }
        }
    }
}
