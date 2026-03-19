package cloud.opencode.base.string.desensitize.jackson;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DesensitizeModuleTest Tests
 * DesensitizeModuleTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("DesensitizeModule Tests")
class DesensitizeModuleTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create module with correct name")
        void shouldCreateModuleWithCorrectName() {
            DesensitizeModule module = new DesensitizeModule();
            assertThat(module.getModuleName()).isEqualTo("DesensitizeModule");
        }

        @Test
        @DisplayName("Should be instance of SimpleModule")
        void shouldBeInstanceOfSimpleModule() {
            DesensitizeModule module = new DesensitizeModule();
            assertThat(module).isInstanceOf(com.fasterxml.jackson.databind.module.SimpleModule.class);
        }
    }
}
