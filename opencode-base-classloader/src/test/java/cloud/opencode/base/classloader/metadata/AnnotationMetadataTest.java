package cloud.opencode.base.classloader.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for AnnotationMetadata
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("AnnotationMetadata Tests")
class AnnotationMetadataTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with annotation type and attributes")
        void shouldCreateWithAnnotationTypeAndAttributes() {
            Map<String, Object> attributes = Map.of("value", "test");
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", attributes, true);

            assertThat(metadata.annotationType()).isEqualTo("org.example.MyAnnotation");
            assertThat(metadata.attributes()).containsEntry("value", "test");
            assertThat(metadata.isRuntimeVisible()).isTrue();
        }

        @Test
        @DisplayName("Should create with empty attributes")
        void shouldCreateWithEmptyAttributes() {
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", Map.of(), true);

            assertThat(metadata.attributes()).isEmpty();
        }

        @Test
        @DisplayName("Should throw on null annotation type")
        void shouldThrowOnNullAnnotationType() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new AnnotationMetadata(null, Map.of(), true));
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return annotation type")
        void shouldReturnAnnotationType() {
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", Map.of(), true);

            assertThat(metadata.annotationType()).isEqualTo("org.example.MyAnnotation");
        }

        @Test
        @DisplayName("Should return immutable attributes")
        void shouldReturnImmutableAttributes() {
            Map<String, Object> attributes = Map.of("value", "test");
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", attributes, true);

            assertThat(metadata.attributes()).isUnmodifiable();
        }

        @Test
        @DisplayName("Should return runtime visibility")
        void shouldReturnRuntimeVisibility() {
            AnnotationMetadata visible = new AnnotationMetadata(
                    "org.example.MyAnnotation", Map.of(), true);
            AnnotationMetadata invisible = new AnnotationMetadata(
                    "org.example.MyAnnotation", Map.of(), false);

            assertThat(visible.isRuntimeVisible()).isTrue();
            assertThat(invisible.isRuntimeVisible()).isFalse();
        }
    }

    @Nested
    @DisplayName("Attribute Access Tests")
    class AttributeAccessTests {

        @Test
        @DisplayName("Should get attribute by name and type")
        void shouldGetAttributeByNameAndType() {
            Map<String, Object> attributes = Map.of("value", "test", "count", 42);
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", attributes, true);

            assertThat(metadata.getAttribute("value", String.class))
                    .isPresent()
                    .contains("test");
            assertThat(metadata.getAttribute("count", Integer.class))
                    .isPresent()
                    .contains(42);
        }

        @Test
        @DisplayName("Should return empty for nonexistent attribute")
        void shouldReturnEmptyForNonexistentAttribute() {
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", Map.of(), true);

            assertThat(metadata.getAttribute("nonexistent", String.class))
                    .isEmpty();
        }

        @Test
        @DisplayName("Should return empty for wrong type")
        void shouldReturnEmptyForWrongType() {
            Map<String, Object> attributes = Map.of("value", "test");
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", attributes, true);

            assertThat(metadata.getAttribute("value", Integer.class))
                    .isEmpty();
        }

        @Test
        @DisplayName("Should get attribute with default value")
        void shouldGetAttributeWithDefaultValue() {
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", Map.of(), true);

            assertThat(metadata.getAttribute("nonexistent", String.class, "default"))
                    .isEqualTo("default");
        }

        @Test
        @DisplayName("Should get value attribute")
        void shouldGetValueAttribute() {
            Map<String, Object> attributes = Map.of("value", "test");
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", attributes, true);

            assertThat(metadata.getValue())
                    .isPresent()
                    .contains("test");
        }

        @Test
        @DisplayName("Should return empty value when not present")
        void shouldReturnEmptyValueWhenNotPresent() {
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", Map.of("other", "value"), true);

            assertThat(metadata.getValue()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Attribute Query Tests")
    class AttributeQueryTests {

        @Test
        @DisplayName("Should check if has attribute")
        void shouldCheckIfHasAttribute() {
            Map<String, Object> attributes = Map.of("value", "test");
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", attributes, true);

            assertThat(metadata.hasAttribute("value")).isTrue();
            assertThat(metadata.hasAttribute("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("Should get attribute names")
        void shouldGetAttributeNames() {
            Map<String, Object> attributes = Map.of("value", "test", "count", 42);
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", attributes, true);

            Set<String> names = metadata.getAttributeNames();

            assertThat(names).containsExactlyInAnyOrder("value", "count");
        }

        @Test
        @DisplayName("Should return empty attribute names for no attributes")
        void shouldReturnEmptyAttributeNamesForNoAttributes() {
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", Map.of(), true);

            assertThat(metadata.getAttributeNames()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Complex Attribute Tests")
    class ComplexAttributeTests {

        @Test
        @DisplayName("Should handle array attributes")
        void shouldHandleArrayAttributes() {
            String[] array = {"a", "b", "c"};
            Map<String, Object> attributes = Map.of("values", array);
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", attributes, true);

            assertThat(metadata.getAttribute("values", String[].class))
                    .isPresent()
                    .contains(array);
        }

        @Test
        @DisplayName("Should handle class attributes")
        void shouldHandleClassAttributes() {
            Map<String, Object> attributes = Map.of("type", "java.lang.String");
            AnnotationMetadata metadata = new AnnotationMetadata(
                    "org.example.MyAnnotation", attributes, true);

            assertThat(metadata.getAttribute("type", String.class))
                    .isPresent()
                    .contains("java.lang.String");
        }
    }
}
