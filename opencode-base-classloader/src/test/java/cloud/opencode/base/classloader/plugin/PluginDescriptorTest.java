package cloud.opencode.base.classloader.plugin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for PluginDescriptor record
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
@DisplayName("PluginDescriptor Tests")
class PluginDescriptorTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Valid construction succeeds")
        void validConstruction() {
            Path jarPath = Path.of("/plugins/auth.jar");
            PluginDescriptor desc = new PluginDescriptor(
                    "auth-plugin", "Authentication Plugin", "1.0.0",
                    "com.example.AuthPlugin", jarPath);

            assertThat(desc.id()).isEqualTo("auth-plugin");
            assertThat(desc.name()).isEqualTo("Authentication Plugin");
            assertThat(desc.version()).isEqualTo("1.0.0");
            assertThat(desc.mainClass()).isEqualTo("com.example.AuthPlugin");
            assertThat(desc.jarPath()).isEqualTo(jarPath);
        }

        @Test
        @DisplayName("Null id throws NullPointerException")
        void nullIdThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new PluginDescriptor(
                            null, "name", "1.0", "Main", Path.of("/a.jar")))
                    .withMessageContaining("id");
        }

        @Test
        @DisplayName("Null name throws NullPointerException")
        void nullNameThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new PluginDescriptor(
                            "id", null, "1.0", "Main", Path.of("/a.jar")))
                    .withMessageContaining("name");
        }

        @Test
        @DisplayName("Null version throws NullPointerException")
        void nullVersionThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new PluginDescriptor(
                            "id", "name", null, "Main", Path.of("/a.jar")))
                    .withMessageContaining("version");
        }

        @Test
        @DisplayName("Null mainClass throws NullPointerException")
        void nullMainClassThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new PluginDescriptor(
                            "id", "name", "1.0", null, Path.of("/a.jar")))
                    .withMessageContaining("mainClass");
        }

        @Test
        @DisplayName("Null jarPath throws NullPointerException")
        void nullJarPathThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new PluginDescriptor(
                            "id", "name", "1.0", "Main", null))
                    .withMessageContaining("jarPath");
        }
    }

    @Nested
    @DisplayName("Record Behavior Tests")
    class RecordBehaviorTests {

        @Test
        @DisplayName("equals and hashCode work correctly")
        void equalsAndHashCode() {
            Path jarPath = Path.of("/plugins/test.jar");
            PluginDescriptor d1 = new PluginDescriptor("id", "name", "1.0", "Main", jarPath);
            PluginDescriptor d2 = new PluginDescriptor("id", "name", "1.0", "Main", jarPath);

            assertThat(d1).isEqualTo(d2);
            assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
        }

        @Test
        @DisplayName("Different id produces inequality")
        void differentIdNotEqual() {
            Path jarPath = Path.of("/plugins/test.jar");
            PluginDescriptor d1 = new PluginDescriptor("id1", "name", "1.0", "Main", jarPath);
            PluginDescriptor d2 = new PluginDescriptor("id2", "name", "1.0", "Main", jarPath);

            assertThat(d1).isNotEqualTo(d2);
        }

        @Test
        @DisplayName("toString contains all fields")
        void toStringContainsFields() {
            PluginDescriptor desc = new PluginDescriptor(
                    "auth", "Auth Plugin", "2.0", "com.Auth", Path.of("/auth.jar"));
            String str = desc.toString();

            assertThat(str).contains("auth");
            assertThat(str).contains("Auth Plugin");
            assertThat(str).contains("2.0");
            assertThat(str).contains("com.Auth");
        }
    }
}
