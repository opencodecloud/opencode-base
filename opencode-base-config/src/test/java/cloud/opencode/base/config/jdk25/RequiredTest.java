package cloud.opencode.base.config.jdk25;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * RequiredTest Tests
 * RequiredTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("Required Annotation Tests")
class RequiredTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = Required.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target RECORD_COMPONENT")
        void shouldTargetRecordComponent() {
            var target = Required.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.RECORD_COMPONENT);
        }
    }

    @Nested
    @DisplayName("Annotation Presence Tests")
    class AnnotationPresenceTests {

        record DatabaseConfig(
            @Required String url,
            @Required String username,
            int poolSize
        ) {}

        @Test
        @DisplayName("should be present on annotated record components")
        void shouldBePresentOnAnnotatedComponents() {
            var components = DatabaseConfig.class.getRecordComponents();
            assertThat(components[0].isAnnotationPresent(Required.class)).isTrue();
            assertThat(components[1].isAnnotationPresent(Required.class)).isTrue();
        }

        @Test
        @DisplayName("should not be present on unannotated record components")
        void shouldNotBePresentOnUnannotatedComponents() {
            var components = DatabaseConfig.class.getRecordComponents();
            assertThat(components[2].isAnnotationPresent(Required.class)).isFalse();
        }
    }
}
