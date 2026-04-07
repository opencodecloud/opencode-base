package cloud.opencode.base.xml.bind.annotation;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlValueTest Tests
 * XmlValueTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlValue Annotation Tests")
class XmlValueTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = XmlValue.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD and METHOD")
        void shouldTargetFieldAndMethod() {
            var target = XmlValue.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactlyInAnyOrder(ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT);
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(XmlValue.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Annotation Presence Tests")
    class AnnotationPresenceTests {

        @XmlValue
        private String valueField;

        private String normalField;

        @Test
        @DisplayName("should be present on annotated field")
        void shouldBePresentOnAnnotatedField() throws NoSuchFieldException {
            assertThat(getClass().getDeclaredField("valueField")
                .isAnnotationPresent(XmlValue.class)).isTrue();
        }

        @Test
        @DisplayName("should not be present on unannotated field")
        void shouldNotBePresentOnUnannotatedField() throws NoSuchFieldException {
            assertThat(getClass().getDeclaredField("normalField")
                .isAnnotationPresent(XmlValue.class)).isFalse();
        }
    }
}
