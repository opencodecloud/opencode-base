package cloud.opencode.base.classloader.plugin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for PluginState enum
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
@DisplayName("PluginState Tests")
class PluginStateTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("All expected states exist")
        void allExpectedStatesExist() {
            PluginState[] values = PluginState.values();
            assertThat(values).hasSize(6);
            assertThat(values).containsExactly(
                    PluginState.DISCOVERED,
                    PluginState.LOADED,
                    PluginState.STARTED,
                    PluginState.STOPPED,
                    PluginState.UNLOADED,
                    PluginState.FAILED
            );
        }

        @Test
        @DisplayName("valueOf returns correct state")
        void valueOfReturnsCorrectState() {
            assertThat(PluginState.valueOf("DISCOVERED")).isEqualTo(PluginState.DISCOVERED);
            assertThat(PluginState.valueOf("LOADED")).isEqualTo(PluginState.LOADED);
            assertThat(PluginState.valueOf("STARTED")).isEqualTo(PluginState.STARTED);
            assertThat(PluginState.valueOf("STOPPED")).isEqualTo(PluginState.STOPPED);
            assertThat(PluginState.valueOf("UNLOADED")).isEqualTo(PluginState.UNLOADED);
            assertThat(PluginState.valueOf("FAILED")).isEqualTo(PluginState.FAILED);
        }

        @Test
        @DisplayName("valueOf with invalid name throws exception")
        void valueOfWithInvalidNameThrows() {
            assertThatThrownBy(() -> PluginState.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
