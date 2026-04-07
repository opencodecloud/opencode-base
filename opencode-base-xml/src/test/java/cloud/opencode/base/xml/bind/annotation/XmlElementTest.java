package cloud.opencode.base.xml.bind.annotation;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlElementTest Tests
 * XmlElementTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlElement Annotation Tests")
class XmlElementTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = XmlElement.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD and METHOD")
        void shouldTargetFieldAndMethod() {
            var target = XmlElement.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactlyInAnyOrder(ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT);
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(XmlElement.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @XmlElement
        private String defaultField;

        @XmlElement(value = "custom-name", namespace = "http://example.com", required = true, defaultValue = "hello", cdata = true)
        private String fullField;

        @Test
        @DisplayName("value should default to empty string")
        void valueShouldDefaultToEmpty() throws NoSuchFieldException {
            XmlElement annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(XmlElement.class);
            assertThat(annotation.value()).isEmpty();
        }

        @Test
        @DisplayName("namespace should default to empty string")
        void namespaceShouldDefaultToEmpty() throws NoSuchFieldException {
            XmlElement annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(XmlElement.class);
            assertThat(annotation.namespace()).isEmpty();
        }

        @Test
        @DisplayName("required should default to false")
        void requiredShouldDefaultToFalse() throws NoSuchFieldException {
            XmlElement annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(XmlElement.class);
            assertThat(annotation.required()).isFalse();
        }

        @Test
        @DisplayName("defaultValue should default to empty string")
        void defaultValueShouldDefaultToEmpty() throws NoSuchFieldException {
            XmlElement annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(XmlElement.class);
            assertThat(annotation.defaultValue()).isEmpty();
        }

        @Test
        @DisplayName("cdata should default to false")
        void cdataShouldDefaultToFalse() throws NoSuchFieldException {
            XmlElement annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(XmlElement.class);
            assertThat(annotation.cdata()).isFalse();
        }

        @Test
        @DisplayName("should support all custom values")
        void shouldSupportAllCustomValues() throws NoSuchFieldException {
            XmlElement annotation = getClass().getDeclaredField("fullField")
                .getAnnotation(XmlElement.class);
            assertThat(annotation.value()).isEqualTo("custom-name");
            assertThat(annotation.namespace()).isEqualTo("http://example.com");
            assertThat(annotation.required()).isTrue();
            assertThat(annotation.defaultValue()).isEqualTo("hello");
            assertThat(annotation.cdata()).isTrue();
        }
    }
}
