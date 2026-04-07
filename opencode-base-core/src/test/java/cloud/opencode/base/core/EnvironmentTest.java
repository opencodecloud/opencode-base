package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Environment}.
 *
 * @author Leon Soo
 * @since 1.0.3
 */
class EnvironmentTest {

    @Nested
    class JavaInfo {

        @Test
        void javaVersion_shouldBeAtLeast25() {
            assertThat(Environment.javaVersion()).isGreaterThanOrEqualTo(25);
        }

        @Test
        void javaVendor_shouldNotBeNullOrEmpty() {
            assertThat(Environment.javaVendor()).isNotNull().isNotEmpty();
        }

        @Test
        void isJavaVersionAtLeast_shouldReturnTrueFor25() {
            assertThat(Environment.isJavaVersionAtLeast(25)).isTrue();
        }

        @Test
        void isJavaVersionAtLeast_shouldReturnFalseFor99() {
            assertThat(Environment.isJavaVersionAtLeast(99)).isFalse();
        }

        @Test
        void javaVersion_shouldBeCached() {
            int first = Environment.javaVersion();
            int second = Environment.javaVersion();
            assertThat(first).isEqualTo(second);
        }
    }

    @Nested
    class OsDetection {

        @Test
        void osName_shouldNotBeNullOrEmpty() {
            assertThat(Environment.osName()).isNotNull().isNotEmpty();
        }

        @Test
        void exactlyOneOsTypeShouldMatch() {
            boolean windows = Environment.isWindows();
            boolean linux = Environment.isLinux();
            boolean mac = Environment.isMacOS();

            // At least one should match on any standard OS
            assertThat(windows || linux || mac)
                    .as("At least one OS type should be detected")
                    .isTrue();

            // At most one should match (mutually exclusive)
            int count = (windows ? 1 : 0) + (linux ? 1 : 0) + (mac ? 1 : 0);
            assertThat(count)
                    .as("Exactly one OS type should be detected, got windows=%s linux=%s mac=%s",
                            windows, linux, mac)
                    .isEqualTo(1);
        }

        @Test
        void osName_shouldBeCached() {
            String first = Environment.osName();
            String second = Environment.osName();
            assertThat(first).isSameAs(second);
        }
    }

    @Nested
    class RuntimeDetection {

        @Test
        void isGraalVmNativeImage_shouldReturnFalseOnStandardJvm() {
            // Unless running in GraalVM native image, this should be false
            if (System.getProperty("org.graalvm.nativeimage.imagecode") == null) {
                assertThat(Environment.isGraalVmNativeImage()).isFalse();
            }
        }

        @Test
        void isContainer_shouldNotThrow() {
            // Just verify it runs without exceptions; result depends on environment
            boolean result = Environment.isContainer();
            assertThat(result).isIn(true, false);
        }
    }

    @Nested
    class VirtualThreadDetection {

        @Test
        void isVirtualThread_shouldBeFalseOnPlatformThread() {
            assertThat(Environment.isVirtualThread()).isFalse();
        }

        @Test
        void isVirtualThread_shouldBeTrueInsideVirtualThread() throws Exception {
            boolean[] result = new boolean[1];
            Thread vt = Thread.ofVirtual().start(() -> result[0] = Environment.isVirtualThread());
            vt.join();
            assertThat(result[0]).isTrue();
        }
    }

    @Nested
    class ResourceInfo {

        @Test
        void availableProcessors_shouldBePositive() {
            assertThat(Environment.availableProcessors()).isGreaterThan(0);
        }

        @Test
        void maxMemory_shouldBePositive() {
            assertThat(Environment.maxMemory()).isGreaterThan(0);
        }

        @Test
        void totalMemory_shouldBePositive() {
            assertThat(Environment.totalMemory()).isGreaterThan(0);
        }

        @Test
        void freeMemory_shouldBeNonNegative() {
            assertThat(Environment.freeMemory()).isGreaterThanOrEqualTo(0);
        }

        @Test
        void totalMemory_shouldNotExceedMaxMemory() {
            assertThat(Environment.totalMemory()).isLessThanOrEqualTo(Environment.maxMemory());
        }

        @Test
        void freeMemory_shouldNotExceedTotalMemory() {
            assertThat(Environment.freeMemory()).isLessThanOrEqualTo(Environment.totalMemory());
        }
    }

    @Nested
    @DisplayName("Process info")
    class ProcessInfoTests {
        @Test void pidIsPositive() {
            assertThat(Environment.pid()).isGreaterThan(0);
        }

        @Test void uptimeIsPositive() {
            assertThat(Environment.uptime().toMillis()).isGreaterThan(0);
        }

        @Test void javaHomeIsNotBlank() {
            assertThat(Environment.javaHome()).isNotBlank();
        }

        @Test void userDirIsNotBlank() {
            assertThat(Environment.userDir()).isNotBlank();
        }

        @Test void tempDirIsNotBlank() {
            assertThat(Environment.tempDir()).isNotBlank();
        }
    }
}
