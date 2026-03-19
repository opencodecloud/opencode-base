package cloud.opencode.base.config.bind;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * ConfigPropertiesTest Tests
 * ConfigPropertiesTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigProperties Annotation Tests")
class ConfigPropertiesTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = ConfigProperties.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target TYPE only")
        void shouldTargetType() {
            var target = ConfigProperties.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.TYPE);
        }
    }

    @Nested
    @DisplayName("Annotation Values Tests")
    class AnnotationValuesTests {

        @ConfigProperties(prefix = "database")
        static class DatabaseConfig {}

        @ConfigProperties(prefix = "app.server")
        static class ServerConfig {}

        @Test
        @DisplayName("should read prefix value")
        void shouldReadPrefixValue() {
            ConfigProperties annotation = DatabaseConfig.class.getAnnotation(ConfigProperties.class);
            assertThat(annotation).isNotNull();
            assertThat(annotation.prefix()).isEqualTo("database");
        }

        @Test
        @DisplayName("should support dotted prefix")
        void shouldSupportDottedPrefix() {
            ConfigProperties annotation = ServerConfig.class.getAnnotation(ConfigProperties.class);
            assertThat(annotation).isNotNull();
            assertThat(annotation.prefix()).isEqualTo("app.server");
        }
    }
}
