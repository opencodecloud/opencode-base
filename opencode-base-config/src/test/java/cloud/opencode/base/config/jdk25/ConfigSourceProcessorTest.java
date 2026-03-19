package cloud.opencode.base.config.jdk25;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ConfigSourceProcessor 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigSourceProcessor 测试")
class ConfigSourceProcessorTest {

    @Nested
    @DisplayName("类测试")
    class ClassTests {

        @Test
        @DisplayName("类存在")
        void testClassExists() {
            assertThat(ConfigSourceProcessor.class).isNotNull();
        }
    }
}
