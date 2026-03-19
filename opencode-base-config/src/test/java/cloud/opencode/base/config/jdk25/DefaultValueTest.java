package cloud.opencode.base.config.jdk25;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultValueTest Tests
 * DefaultValueTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("DefaultValue Annotation Tests")
class DefaultValueTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = DefaultValue.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target RECORD_COMPONENT")
        void shouldTargetRecordComponent() {
            var target = DefaultValue.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.RECORD_COMPONENT);
        }
    }

    @Nested
    @DisplayName("Annotation Values Tests")
    class AnnotationValuesTests {

        record TestConfig(
            @DefaultValue("8080") int port,
            @DefaultValue("localhost") String host
        ) {}

        @Test
        @DisplayName("should read default value from record component")
        void shouldReadDefaultValue() {
            var components = TestConfig.class.getRecordComponents();
            assertThat(components).isNotNull();

            // port component
            var portAnnotation = components[0].getAnnotation(DefaultValue.class);
            assertThat(portAnnotation).isNotNull();
            assertThat(portAnnotation.value()).isEqualTo("8080");

            // host component
            var hostAnnotation = components[1].getAnnotation(DefaultValue.class);
            assertThat(hostAnnotation).isNotNull();
            assertThat(hostAnnotation.value()).isEqualTo("localhost");
        }
    }
}
