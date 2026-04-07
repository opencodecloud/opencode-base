package cloud.opencode.base.xml.bind.annotation;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlAttributeTest Tests
 * XmlAttributeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlAttribute Annotation Tests")
class XmlAttributeTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = XmlAttribute.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD and METHOD")
        void shouldTargetFieldAndMethod() {
            var target = XmlAttribute.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactlyInAnyOrder(ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT);
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(XmlAttribute.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @XmlAttribute
        private String defaultField;

        @XmlAttribute("custom-name")
        private String namedField;

        @XmlAttribute(required = true)
        private String requiredField;

        @Test
        @DisplayName("value should default to empty string")
        void valueShouldDefaultToEmpty() throws NoSuchFieldException {
            XmlAttribute annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(XmlAttribute.class);
            assertThat(annotation.value()).isEmpty();
        }

        @Test
        @DisplayName("required should default to false")
        void requiredShouldDefaultToFalse() throws NoSuchFieldException {
            XmlAttribute annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(XmlAttribute.class);
            assertThat(annotation.required()).isFalse();
        }

        @Test
        @DisplayName("should support custom name")
        void shouldSupportCustomName() throws NoSuchFieldException {
            XmlAttribute annotation = getClass().getDeclaredField("namedField")
                .getAnnotation(XmlAttribute.class);
            assertThat(annotation.value()).isEqualTo("custom-name");
        }

        @Test
        @DisplayName("should support required flag")
        void shouldSupportRequiredFlag() throws NoSuchFieldException {
            XmlAttribute annotation = getClass().getDeclaredField("requiredField")
                .getAnnotation(XmlAttribute.class);
            assertThat(annotation.required()).isTrue();
        }
    }
}
