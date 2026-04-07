package cloud.opencode.base.xml.bind.annotation;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * XmlElementListTest Tests
 * XmlElementListTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("XmlElementList Annotation Tests")
class XmlElementListTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = XmlElementList.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD and METHOD")
        void shouldTargetFieldAndMethod() {
            var target = XmlElementList.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactlyInAnyOrder(ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT);
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(XmlElementList.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @XmlElementList
        private java.util.List<String> defaultField;

        @XmlElementList(value = "roles", itemName = "role")
        private java.util.List<String> customField;

        @Test
        @DisplayName("value should default to empty string")
        void valueShouldDefaultToEmpty() throws NoSuchFieldException {
            XmlElementList annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(XmlElementList.class);
            assertThat(annotation.value()).isEmpty();
        }

        @Test
        @DisplayName("itemName should default to empty string")
        void itemNameShouldDefaultToEmpty() throws NoSuchFieldException {
            XmlElementList annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(XmlElementList.class);
            assertThat(annotation.itemName()).isEmpty();
        }

        @Test
        @DisplayName("should support custom wrapper and item names")
        void shouldSupportCustomNames() throws NoSuchFieldException {
            XmlElementList annotation = getClass().getDeclaredField("customField")
                .getAnnotation(XmlElementList.class);
            assertThat(annotation.value()).isEqualTo("roles");
            assertThat(annotation.itemName()).isEqualTo("role");
        }
    }
}
