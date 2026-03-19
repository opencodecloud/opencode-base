package cloud.opencode.base.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ConfigChangeType 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigChangeType 测试")
class ConfigChangeTypeTest {

    @Test
    @DisplayName("枚举值 - 包含所有类型")
    void testEnumValues() {
        ConfigChangeType[] values = ConfigChangeType.values();

        assertThat(values).hasSize(3);
        assertThat(values).containsExactly(
                ConfigChangeType.ADDED,
                ConfigChangeType.MODIFIED,
                ConfigChangeType.REMOVED);
    }

    @Test
    @DisplayName("valueOf - ADDED")
    void testValueOfAdded() {
        assertThat(ConfigChangeType.valueOf("ADDED")).isEqualTo(ConfigChangeType.ADDED);
    }

    @Test
    @DisplayName("valueOf - MODIFIED")
    void testValueOfModified() {
        assertThat(ConfigChangeType.valueOf("MODIFIED")).isEqualTo(ConfigChangeType.MODIFIED);
    }

    @Test
    @DisplayName("valueOf - REMOVED")
    void testValueOfRemoved() {
        assertThat(ConfigChangeType.valueOf("REMOVED")).isEqualTo(ConfigChangeType.REMOVED);
    }

    @Test
    @DisplayName("valueOf - 无效值抛异常")
    void testValueOfInvalid() {
        assertThatThrownBy(() -> ConfigChangeType.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("ordinal - 顺序正确")
    void testOrdinal() {
        assertThat(ConfigChangeType.ADDED.ordinal()).isEqualTo(0);
        assertThat(ConfigChangeType.MODIFIED.ordinal()).isEqualTo(1);
        assertThat(ConfigChangeType.REMOVED.ordinal()).isEqualTo(2);
    }

    @Test
    @DisplayName("name - 名称正确")
    void testName() {
        assertThat(ConfigChangeType.ADDED.name()).isEqualTo("ADDED");
        assertThat(ConfigChangeType.MODIFIED.name()).isEqualTo("MODIFIED");
        assertThat(ConfigChangeType.REMOVED.name()).isEqualTo("REMOVED");
    }
}
