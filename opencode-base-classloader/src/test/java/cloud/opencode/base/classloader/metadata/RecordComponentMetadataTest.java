package cloud.opencode.base.classloader.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for RecordComponentMetadata
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("RecordComponentMetadata Tests")
class RecordComponentMetadataTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with all parameters")
        void shouldCreateWithAllParameters() {
            List<AnnotationMetadata> annotations = List.of(
                    new AnnotationMetadata("java.lang.Deprecated", Map.of(), true)
            );

            RecordComponentMetadata metadata = new RecordComponentMetadata(
                    "name", "java.lang.String", "java.lang.String", annotations
            );

            assertThat(metadata.name()).isEqualTo("name");
            assertThat(metadata.type()).isEqualTo("java.lang.String");
            assertThat(metadata.genericType()).isEqualTo("java.lang.String");
            assertThat(metadata.annotations()).hasSize(1);
        }

        @Test
        @DisplayName("Should create with null generic type")
        void shouldCreateWithNullGenericType() {
            RecordComponentMetadata metadata = new RecordComponentMetadata(
                    "id", "int", null, List.of()
            );

            assertThat(metadata.name()).isEqualTo("id");
            assertThat(metadata.type()).isEqualTo("int");
            assertThat(metadata.genericType()).isNull();
        }

        @Test
        @DisplayName("Should throw on null name")
        void shouldThrowOnNullName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new RecordComponentMetadata(null, "int", null, List.of()));
        }

        @Test
        @DisplayName("Should throw on null type")
        void shouldThrowOnNullType() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new RecordComponentMetadata("name", null, null, List.of()));
        }

        @Test
        @DisplayName("Should handle null annotations")
        void shouldHandleNullAnnotations() {
            RecordComponentMetadata metadata = new RecordComponentMetadata(
                    "name", "java.lang.String", null, null
            );

            assertThat(metadata.annotations()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should return immutable annotations list")
        void shouldReturnImmutableAnnotationsList() {
            RecordComponentMetadata metadata = new RecordComponentMetadata(
                    "name", "java.lang.String", null,
                    List.of(new AnnotationMetadata("Test", Map.of(), true))
            );

            assertThatThrownBy(() -> metadata.annotations().add(
                    new AnnotationMetadata("Other", Map.of(), true)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should defensive copy annotations")
        void shouldDefensiveCopyAnnotations() {
            List<AnnotationMetadata> annotations = new ArrayList<>();
            annotations.add(new AnnotationMetadata("Test", Map.of(), true));

            RecordComponentMetadata metadata = new RecordComponentMetadata(
                    "name", "java.lang.String", null, annotations
            );

            annotations.add(new AnnotationMetadata("Other", Map.of(), true));

            assertThat(metadata.annotations()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal for same values")
        void shouldBeEqualForSameValues() {
            RecordComponentMetadata m1 = new RecordComponentMetadata(
                    "name", "java.lang.String", null, List.of()
            );
            RecordComponentMetadata m2 = new RecordComponentMetadata(
                    "name", "java.lang.String", null, List.of()
            );

            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different names")
        void shouldNotBeEqualForDifferentNames() {
            RecordComponentMetadata m1 = new RecordComponentMetadata(
                    "name1", "java.lang.String", null, List.of()
            );
            RecordComponentMetadata m2 = new RecordComponentMetadata(
                    "name2", "java.lang.String", null, List.of()
            );

            assertThat(m1).isNotEqualTo(m2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should include component info in toString")
        void shouldIncludeComponentInfoInToString() {
            RecordComponentMetadata metadata = new RecordComponentMetadata(
                    "name", "java.lang.String", "java.lang.String", List.of()
            );

            String str = metadata.toString();
            assertThat(str).contains("name");
            assertThat(str).contains("java.lang.String");
        }
    }
}
