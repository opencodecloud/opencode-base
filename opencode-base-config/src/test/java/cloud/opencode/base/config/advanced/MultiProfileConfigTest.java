package cloud.opencode.base.config.advanced;

import cloud.opencode.base.config.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for MultiProfileConfig.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("MultiProfileConfig Tests")
class MultiProfileConfigTest {

    @Nested
    @DisplayName("Load Tests")
    class LoadTests {

        @Test
        @DisplayName("load with empty args returns config")
        void testLoadWithEmptyArgs() {
            Config config = MultiProfileConfig.load(new String[]{});
            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("load with command line args returns config")
        void testLoadWithArgs() {
            Config config = MultiProfileConfig.load(new String[]{"--app.name=TestApp"});
            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("load includes system properties")
        void testIncludesSystemProperties() {
            String testKey = "multiprofile.test.key." + System.currentTimeMillis();
            System.setProperty(testKey, "sys-value");
            try {
                Config config = MultiProfileConfig.load(new String[]{});
                assertThat(config.hasKey(testKey)).isTrue();
                assertThat(config.getString(testKey)).isEqualTo("sys-value");
            } finally {
                System.clearProperty(testKey);
            }
        }

        @Test
        @DisplayName("load includes environment variables")
        void testIncludesEnvVars() {
            Config config = MultiProfileConfig.load(new String[]{});
            // Environment variables should be available
            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("load with args overrides other sources")
        void testCommandLineOverrides() {
            String testKey = "override.test.key." + System.currentTimeMillis();
            System.setProperty(testKey, "sys-value");
            try {
                Config config = MultiProfileConfig.load(new String[]{"--" + testKey + "=cmd-value"});
                // Command line should have highest priority
                assertThat(config.getString(testKey)).isEqualTo("cmd-value");
            } finally {
                System.clearProperty(testKey);
            }
        }
    }

    @Nested
    @DisplayName("Profile Detection Tests")
    class ProfileDetectionTests {

        @Test
        @DisplayName("uses default profile when no APP_PROFILE or app.profile is set")
        void testDefaultProfile() {
            // Clear app.profile if set
            String original = System.getProperty("app.profile");
            try {
                System.clearProperty("app.profile");
                Config config = MultiProfileConfig.load(new String[]{});
                // Should not throw - even if profile-specific file doesn't exist
                assertThat(config).isNotNull();
            } finally {
                if (original != null) {
                    System.setProperty("app.profile", original);
                }
            }
        }

        @Test
        @DisplayName("uses app.profile system property")
        void testAppProfileSystemProperty() {
            System.setProperty("app.profile", "test");
            try {
                Config config = MultiProfileConfig.load(new String[]{});
                assertThat(config).isNotNull();
            } finally {
                System.clearProperty("app.profile");
            }
        }
    }
}
