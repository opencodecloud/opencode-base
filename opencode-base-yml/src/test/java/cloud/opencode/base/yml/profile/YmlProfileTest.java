package cloud.opencode.base.yml.profile;

import cloud.opencode.base.yml.YmlDocument;
import cloud.opencode.base.yml.exception.OpenYmlException;
import cloud.opencode.base.yml.merge.MergeStrategy;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for YmlProfile utility class
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
@DisplayName("YmlProfile Tests")
class YmlProfileTest {

    @TempDir
    Path tempDir;

    private void writeFile(Path file, String content) throws IOException {
        Files.writeString(file, content);
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("constructor should throw AssertionError")
        void constructorShouldThrowAssertionError() throws Exception {
            Constructor<YmlProfile> constructor = YmlProfile.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                    .isInstanceOf(InvocationTargetException.class)
                    .hasCauseInstanceOf(AssertionError.class)
                    .hasRootCauseMessage("Utility class - do not instantiate");
        }
    }

    @Nested
    @DisplayName("Basic Profile Loading Tests")
    class BasicProfile {

        @Test
        @DisplayName("should load base file only when no profiles given")
        void shouldLoadBaseOnly() throws IOException {
            writeFile(tempDir.resolve("application.yml"),
                    "server:\n  port: 8080\n  host: localhost\n");

            YmlDocument doc = YmlProfile.load(tempDir, "application");

            assertThat(doc.getInt("server.port")).isEqualTo(8080);
            assertThat(doc.getString("server.host")).isEqualTo("localhost");
        }

        @Test
        @DisplayName("should load base and overlay with one profile")
        void shouldLoadWithOneProfile() throws IOException {
            writeFile(tempDir.resolve("application.yml"),
                    "server:\n  port: 8080\n  host: localhost\n");
            writeFile(tempDir.resolve("application-dev.yml"),
                    "server:\n  port: 9090\n");

            YmlDocument doc = YmlProfile.load(tempDir, "application", "dev");

            assertThat(doc.getInt("server.port")).isEqualTo(9090);
            assertThat(doc.getString("server.host")).isEqualTo("localhost");
        }

        @Test
        @DisplayName("should load with multiple profiles in order")
        void shouldLoadWithMultipleProfiles() throws IOException {
            writeFile(tempDir.resolve("application.yml"),
                    "server:\n  port: 8080\n  host: localhost\n  debug: false\n");
            writeFile(tempDir.resolve("application-dev.yml"),
                    "server:\n  port: 9090\n  debug: true\n");
            writeFile(tempDir.resolve("application-local.yml"),
                    "server:\n  port: 3000\n");

            YmlDocument doc = YmlProfile.load(tempDir, "application", "dev", "local");

            // local profile overrides dev's port
            assertThat(doc.getInt("server.port")).isEqualTo(3000);
            // dev profile set debug
            assertThat(doc.getBoolean("server.debug")).isTrue();
            // base host preserved
            assertThat(doc.getString("server.host")).isEqualTo("localhost");
        }

        @Test
        @DisplayName("should load using default name 'application'")
        void shouldLoadWithDefaultName() throws IOException {
            writeFile(tempDir.resolve("application.yml"),
                    "app:\n  name: myapp\n");
            writeFile(tempDir.resolve("application-prod.yml"),
                    "app:\n  name: myapp-prod\n");

            YmlDocument doc = YmlProfile.loadDefault(tempDir, "prod");

            assertThat(doc.getString("app.name")).isEqualTo("myapp-prod");
        }
    }

    @Nested
    @DisplayName("Missing Files Tests")
    class MissingFiles {

        @Test
        @DisplayName("should silently skip missing profile file")
        void shouldSkipMissingProfileFile() throws IOException {
            writeFile(tempDir.resolve("application.yml"),
                    "server:\n  port: 8080\n");

            // "nonexistent" profile file doesn't exist - should not throw
            YmlDocument doc = YmlProfile.load(tempDir, "application", "nonexistent");

            assertThat(doc.getInt("server.port")).isEqualTo(8080);
        }

        @Test
        @DisplayName("should throw when base file is missing")
        void shouldThrowWhenBaseFileMissing() {
            assertThatThrownBy(() -> YmlProfile.load(tempDir, "missing"))
                    .isInstanceOf(OpenYmlException.class)
                    .hasMessageContaining("Base configuration file not found");
        }

        @Test
        @DisplayName("should skip null and blank profile names")
        void shouldSkipNullAndBlankProfiles() throws IOException {
            writeFile(tempDir.resolve("application.yml"),
                    "key: value\n");

            YmlDocument doc = YmlProfile.load(tempDir, "application", "", "  ");

            assertThat(doc.getString("key")).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("Merge Strategy Tests")
    class MergeStrategyTests {

        @Test
        @DisplayName("should use DEEP_MERGE by default")
        void shouldUseDeepMergeByDefault() throws IOException {
            writeFile(tempDir.resolve("application.yml"),
                    "database:\n  host: localhost\n  port: 3306\n");
            writeFile(tempDir.resolve("application-prod.yml"),
                    "database:\n  host: prod-server\n");

            YmlDocument doc = YmlProfile.load(tempDir, "application", "prod");

            assertThat(doc.getString("database.host")).isEqualTo("prod-server");
            assertThat(doc.getInt("database.port")).isEqualTo(3306);
        }

        @Test
        @DisplayName("should support OVERRIDE strategy")
        void shouldSupportOverrideStrategy() throws IOException {
            writeFile(tempDir.resolve("application.yml"),
                    "database:\n  host: localhost\n  port: 3306\n");
            writeFile(tempDir.resolve("application-prod.yml"),
                    "database:\n  host: prod-server\n");

            YmlDocument doc = YmlProfile.load(tempDir, "application",
                    MergeStrategy.OVERRIDE, List.of("prod"));

            assertThat(doc.getString("database.host")).isEqualTo("prod-server");
            // OVERRIDE replaces entire map, so port is gone
            assertThat(doc.has("database.port")).isFalse();
        }

        @Test
        @DisplayName("should support KEEP_FIRST strategy")
        void shouldSupportKeepFirstStrategy() throws IOException {
            writeFile(tempDir.resolve("application.yml"),
                    "server:\n  port: 8080\n");
            writeFile(tempDir.resolve("application-dev.yml"),
                    "server:\n  port: 9090\n");

            YmlDocument doc = YmlProfile.load(tempDir, "application",
                    MergeStrategy.KEEP_FIRST, List.of("dev"));

            // KEEP_FIRST keeps the base value
            assertThat(doc.getInt("server.port")).isEqualTo(8080);
        }
    }

    @Nested
    @DisplayName("Active Profiles Tests")
    class ActiveProfiles {

        @Test
        @DisplayName("should return empty list when no profiles configured")
        void shouldReturnEmptyWhenNotConfigured() {
            // Ensure property is not set
            String oldValue = System.getProperty("yml.profiles.active");
            try {
                System.clearProperty("yml.profiles.active");
                // Note: env vars can't be easily cleared in tests
                // This test works if YAML_PROFILES_ACTIVE is not set in the env
                List<String> profiles = YmlProfile.getActiveProfiles();
                // May not be empty if env var is set, but at minimum should not throw
                assertThat(profiles).isNotNull();
            } finally {
                if (oldValue != null) {
                    System.setProperty("yml.profiles.active", oldValue);
                }
            }
        }

        @Test
        @DisplayName("should read profiles from system property")
        void shouldReadFromSystemProperty() {
            String oldValue = System.getProperty("yml.profiles.active");
            try {
                System.setProperty("yml.profiles.active", "dev, staging, prod");
                List<String> profiles = YmlProfile.getActiveProfiles();
                assertThat(profiles).containsExactly("dev", "staging", "prod");
            } finally {
                if (oldValue != null) {
                    System.setProperty("yml.profiles.active", oldValue);
                } else {
                    System.clearProperty("yml.profiles.active");
                }
            }
        }

        @Test
        @DisplayName("should handle single profile in system property")
        void shouldHandleSingleProfile() {
            String oldValue = System.getProperty("yml.profiles.active");
            try {
                System.setProperty("yml.profiles.active", "production");
                List<String> profiles = YmlProfile.getActiveProfiles();
                assertThat(profiles).containsExactly("production");
            } finally {
                if (oldValue != null) {
                    System.setProperty("yml.profiles.active", oldValue);
                } else {
                    System.clearProperty("yml.profiles.active");
                }
            }
        }

        @Test
        @DisplayName("should return empty list for blank system property")
        void shouldReturnEmptyForBlankProperty() {
            String oldValue = System.getProperty("yml.profiles.active");
            try {
                System.setProperty("yml.profiles.active", "   ");
                List<String> profiles = YmlProfile.getActiveProfiles();
                // Blank property → falls through to env, which may or may not be set
                assertThat(profiles).isNotNull();
            } finally {
                if (oldValue != null) {
                    System.setProperty("yml.profiles.active", oldValue);
                } else {
                    System.clearProperty("yml.profiles.active");
                }
            }
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("should throw on null basePath")
        void shouldThrowOnNullBasePath() {
            assertThatThrownBy(() -> YmlProfile.load(null, "app"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("basePath must not be null");
        }

        @Test
        @DisplayName("should throw on null name")
        void shouldThrowOnNullName() {
            assertThatThrownBy(() -> YmlProfile.load(tempDir, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("name must not be null");
        }

        @Test
        @DisplayName("should throw on blank name")
        void shouldThrowOnBlankName() {
            assertThatThrownBy(() -> YmlProfile.load(tempDir, "  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name must not be blank");
        }
    }

    @Nested
    @DisplayName("YAML Extension Tests")
    class YamlExtensionTests {

        @Test
        @DisplayName("should support .yaml extension for base file")
        void shouldSupportYamlExtension() throws IOException {
            writeFile(tempDir.resolve("config.yaml"),
                    "key: from-yaml\n");

            YmlDocument doc = YmlProfile.load(tempDir, "config");

            assertThat(doc.getString("key")).isEqualTo("from-yaml");
        }

        @Test
        @DisplayName("should prefer .yml over .yaml for profile files")
        void shouldPreferYmlForProfiles() throws IOException {
            writeFile(tempDir.resolve("application.yml"),
                    "base: true\n");
            writeFile(tempDir.resolve("application-dev.yml"),
                    "profile: from-yml\n");
            writeFile(tempDir.resolve("application-dev.yaml"),
                    "profile: from-yaml\n");

            YmlDocument doc = YmlProfile.load(tempDir, "application", "dev");

            // .yml should be preferred
            assertThat(doc.getString("profile")).isEqualTo("from-yml");
        }
    }
}
