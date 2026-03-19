package cloud.opencode.base.config;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ConfigTest Tests
 * ConfigTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("Config Interface Tests")
class ConfigTest {

    @Nested
    @DisplayName("Interface Contract Tests")
    class InterfaceContractTests {

        @Test
        @DisplayName("should be an interface")
        void shouldBeAnInterface() {
            assertThat(Config.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("should declare getString method")
        void shouldDeclareGetStringMethod() throws NoSuchMethodException {
            assertThat(Config.class.getMethod("getString", String.class)).isNotNull();
        }

        @Test
        @DisplayName("should declare getString with default method")
        void shouldDeclareGetStringWithDefaultMethod() throws NoSuchMethodException {
            assertThat(Config.class.getMethod("getString", String.class, String.class)).isNotNull();
        }

        @Test
        @DisplayName("should declare getInt method")
        void shouldDeclareGetIntMethod() throws NoSuchMethodException {
            assertThat(Config.class.getMethod("getInt", String.class)).isNotNull();
        }

        @Test
        @DisplayName("should declare getBoolean method")
        void shouldDeclareGetBooleanMethod() throws NoSuchMethodException {
            assertThat(Config.class.getMethod("getBoolean", String.class)).isNotNull();
        }

        @Test
        @DisplayName("should declare getDuration method")
        void shouldDeclareGetDurationMethod() throws NoSuchMethodException {
            assertThat(Config.class.getMethod("getDuration", String.class)).isNotNull();
        }

        @Test
        @DisplayName("should declare hasKey method")
        void shouldDeclareHasKeyMethod() throws NoSuchMethodException {
            assertThat(Config.class.getMethod("hasKey", String.class)).isNotNull();
        }

        @Test
        @DisplayName("should declare getKeys method")
        void shouldDeclareGetKeysMethod() throws NoSuchMethodException {
            assertThat(Config.class.getMethod("getKeys")).isNotNull();
        }

        @Test
        @DisplayName("should declare getSubConfig method")
        void shouldDeclareGetSubConfigMethod() throws NoSuchMethodException {
            assertThat(Config.class.getMethod("getSubConfig", String.class)).isNotNull();
        }

        @Test
        @DisplayName("should declare bind method")
        void shouldDeclareBindMethod() throws NoSuchMethodException {
            assertThat(Config.class.getMethod("bind", String.class, Class.class)).isNotNull();
        }

        @Test
        @DisplayName("should declare addListener methods")
        void shouldDeclareAddListenerMethods() throws NoSuchMethodException {
            assertThat(Config.class.getMethod("addListener", ConfigListener.class)).isNotNull();
            assertThat(Config.class.getMethod("addListener", String.class, ConfigListener.class)).isNotNull();
        }

        @Test
        @DisplayName("should declare getOptional method")
        void shouldDeclareGetOptionalMethod() throws NoSuchMethodException {
            assertThat(Config.class.getMethod("getOptional", String.class)).isNotNull();
        }
    }
}
