package cloud.opencode.base.xml.bind.annotation;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlRootTest Tests
 * XmlRootTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlRoot Annotation Tests")
class XmlRootTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = XmlRoot.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target TYPE only")
        void shouldTargetType() {
            var target = XmlRoot.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.TYPE);
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(XmlRoot.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @XmlRoot
        static class DefaultRoot {}

        @XmlRoot(value = "user", namespace = "http://example.com")
        static class CustomRoot {}

        @Test
        @DisplayName("value should default to empty string")
        void valueShouldDefaultToEmpty() {
            XmlRoot annotation = DefaultRoot.class.getAnnotation(XmlRoot.class);
            assertThat(annotation.value()).isEmpty();
        }

        @Test
        @DisplayName("namespace should default to empty string")
        void namespaceShouldDefaultToEmpty() {
            XmlRoot annotation = DefaultRoot.class.getAnnotation(XmlRoot.class);
            assertThat(annotation.namespace()).isEmpty();
        }

        @Test
        @DisplayName("should support custom value and namespace")
        void shouldSupportCustomValues() {
            XmlRoot annotation = CustomRoot.class.getAnnotation(XmlRoot.class);
            assertThat(annotation.value()).isEqualTo("user");
            assertThat(annotation.namespace()).isEqualTo("http://example.com");
        }
    }
}
