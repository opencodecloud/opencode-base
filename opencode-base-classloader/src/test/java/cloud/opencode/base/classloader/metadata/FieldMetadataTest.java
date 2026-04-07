package cloud.opencode.base.classloader.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for FieldMetadata
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("FieldMetadata Tests")
class FieldMetadataTest {

    private FieldMetadata createSimpleField(String name, String type, int modifiers) {
        return new FieldMetadata(name, type, modifiers, null, List.of());
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with all parameters")
        void shouldCreateWithAllParameters() {
            FieldMetadata metadata = new FieldMetadata(
                    "testField",
                    "java.lang.String",
                    Modifier.PRIVATE | Modifier.FINAL,
                    "constantValue",
                    List.of()
            );

            assertThat(metadata.fieldName()).isEqualTo("testField");
            assertThat(metadata.fieldType()).isEqualTo("java.lang.String");
            assertThat(metadata.modifiers()).isEqualTo(Modifier.PRIVATE | Modifier.FINAL);
            assertThat(metadata.constantValue()).isEqualTo("constantValue");
        }

        @Test
        @DisplayName("Should throw on null field name")
        void shouldThrowOnNullFieldName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new FieldMetadata(null, "int", 0, null, List.of()));
        }

        @Test
        @DisplayName("Should throw on null field type")
        void shouldThrowOnNullFieldType() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new FieldMetadata("field", null, 0, null, List.of()));
        }

        @Test
        @DisplayName("Should handle null annotations")
        void shouldHandleNullAnnotations() {
            FieldMetadata metadata = new FieldMetadata("field", "int", 0, null, null);

            assertThat(metadata.annotations()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Modifier Tests")
    class ModifierTests {

        @Test
        @DisplayName("Should detect public modifier")
        void shouldDetectPublicModifier() {
            FieldMetadata metadata = createSimpleField("field", "int", Modifier.PUBLIC);

            assertThat(metadata.isPublic()).isTrue();
            assertThat(metadata.isPrivate()).isFalse();
        }

        @Test
        @DisplayName("Should detect private modifier")
        void shouldDetectPrivateModifier() {
            FieldMetadata metadata = createSimpleField("field", "int", Modifier.PRIVATE);

            assertThat(metadata.isPrivate()).isTrue();
            assertThat(metadata.isPublic()).isFalse();
        }

        @Test
        @DisplayName("Should detect protected modifier")
        void shouldDetectProtectedModifier() {
            FieldMetadata metadata = createSimpleField("field", "int", Modifier.PROTECTED);

            assertThat(metadata.isProtected()).isTrue();
        }

        @Test
        @DisplayName("Should detect static modifier")
        void shouldDetectStaticModifier() {
            FieldMetadata metadata = createSimpleField("field", "int", Modifier.STATIC);

            assertThat(metadata.isStatic()).isTrue();
        }

        @Test
        @DisplayName("Should detect final modifier")
        void shouldDetectFinalModifier() {
            FieldMetadata metadata = createSimpleField("field", "int", Modifier.FINAL);

            assertThat(metadata.isFinal()).isTrue();
        }

        @Test
        @DisplayName("Should detect transient modifier")
        void shouldDetectTransientModifier() {
            FieldMetadata metadata = createSimpleField("field", "int", Modifier.TRANSIENT);

            assertThat(metadata.isTransient()).isTrue();
        }

        @Test
        @DisplayName("Should detect volatile modifier")
        void shouldDetectVolatileModifier() {
            FieldMetadata metadata = createSimpleField("field", "int", Modifier.VOLATILE);

            assertThat(metadata.isVolatile()).isTrue();
        }
    }

    @Nested
    @DisplayName("Constant Value Tests")
    class ConstantValueTests {

        @Test
        @DisplayName("Should have constant value")
        void shouldHaveConstantValue() {
            FieldMetadata metadata = new FieldMetadata(
                    "CONSTANT", "int", Modifier.STATIC | Modifier.FINAL, 42, List.of()
            );

            assertThat(metadata.hasConstantValue()).isTrue();
            assertThat(metadata.constantValue()).isEqualTo(42);
        }

        @Test
        @DisplayName("Should not have constant value")
        void shouldNotHaveConstantValue() {
            FieldMetadata metadata = createSimpleField("field", "int", Modifier.PRIVATE);

            assertThat(metadata.hasConstantValue()).isFalse();
            assertThat(metadata.constantValue()).isNull();
        }

        @Test
        @DisplayName("Should handle string constant value")
        void shouldHandleStringConstantValue() {
            FieldMetadata metadata = new FieldMetadata(
                    "MESSAGE", "String", Modifier.STATIC | Modifier.FINAL, "hello", List.of()
            );

            assertThat(metadata.hasConstantValue()).isTrue();
            assertThat(metadata.constantValue()).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("Annotation Tests")
    class AnnotationTests {

        @Test
        @DisplayName("Should check annotation presence")
        void shouldCheckAnnotationPresence() {
            AnnotationMetadata annotation = new AnnotationMetadata(
                    "java.lang.Deprecated", Map.of(), true
            );
            FieldMetadata metadata = new FieldMetadata(
                    "field", "int", 0, null, List.of(annotation)
            );

            assertThat(metadata.hasAnnotation("java.lang.Deprecated")).isTrue();
            assertThat(metadata.hasAnnotation("java.lang.Override")).isFalse();
        }

        @Test
        @DisplayName("Should get annotation")
        void shouldGetAnnotation() {
            AnnotationMetadata annotation = new AnnotationMetadata(
                    "java.lang.Deprecated", Map.of("since", "1.0"), true
            );
            FieldMetadata metadata = new FieldMetadata(
                    "field", "int", 0, null, List.of(annotation)
            );

            assertThat(metadata.getAnnotation("java.lang.Deprecated")).isPresent();
            assertThat(metadata.getAnnotation("java.lang.Override")).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for nonexistent annotation")
        void shouldReturnEmptyForNonexistentAnnotation() {
            FieldMetadata metadata = createSimpleField("field", "int", 0);

            assertThat(metadata.getAnnotation("java.lang.Deprecated")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Generic Type Tests")
    class GenericTypeTests {

        @Test
        @DisplayName("Should store generic type")
        void shouldStoreGenericType() {
            FieldMetadata metadata = new FieldMetadata(
                    "items", "java.util.List", Modifier.PRIVATE, null, List.of(),
                    "java.util.List<java.lang.String>"
            );

            assertThat(metadata.getGenericType()).isEqualTo("java.util.List<java.lang.String>");
        }

        @Test
        @DisplayName("Should return null for non-generic field")
        void shouldReturnNullForNonGenericField() {
            FieldMetadata metadata = new FieldMetadata(
                    "count", "int", Modifier.PRIVATE, null, List.of()
            );

            assertThat(metadata.getGenericType()).isNull();
        }

        @Test
        @DisplayName("Should default to null with old constructor")
        void shouldDefaultToNullWithOldConstructor() {
            FieldMetadata metadata = createSimpleField("field", "java.util.List", Modifier.PRIVATE);

            assertThat(metadata.getGenericType()).isNull();
        }
    }

    @Nested
    @DisplayName("Simple Type Name Tests")
    class SimpleTypeNameTests {

        @Test
        @DisplayName("Should get simple type name")
        void shouldGetSimpleTypeName() {
            FieldMetadata metadata = createSimpleField("field", "java.lang.String", 0);

            assertThat(metadata.getSimpleTypeName()).isEqualTo("String");
        }

        @Test
        @DisplayName("Should handle primitive type")
        void shouldHandlePrimitiveType() {
            FieldMetadata metadata = createSimpleField("field", "int", 0);

            assertThat(metadata.getSimpleTypeName()).isEqualTo("int");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal for same name and type")
        void shouldBeEqualForSameNameAndType() {
            FieldMetadata m1 = createSimpleField("field", "int", Modifier.PUBLIC);
            FieldMetadata m2 = createSimpleField("field", "int", Modifier.PRIVATE);

            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different names")
        void shouldNotBeEqualForDifferentNames() {
            FieldMetadata m1 = createSimpleField("field1", "int", 0);
            FieldMetadata m2 = createSimpleField("field2", "int", 0);

            assertThat(m1).isNotEqualTo(m2);
        }

        @Test
        @DisplayName("Should not be equal for different types")
        void shouldNotBeEqualForDifferentTypes() {
            FieldMetadata m1 = createSimpleField("field", "int", 0);
            FieldMetadata m2 = createSimpleField("field", "long", 0);

            assertThat(m1).isNotEqualTo(m2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should format toString")
        void shouldFormatToString() {
            FieldMetadata metadata = new FieldMetadata(
                    "maxCount", "int", Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL, null, List.of()
            );

            String str = metadata.toString();
            assertThat(str).contains("private");
            assertThat(str).contains("static");
            assertThat(str).contains("final");
            assertThat(str).contains("int");
            assertThat(str).contains("maxCount");
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should return immutable annotations list")
        void shouldReturnImmutableAnnotationsList() {
            AnnotationMetadata annotation = new AnnotationMetadata(
                    "java.lang.Deprecated", Map.of(), true
            );
            FieldMetadata metadata = new FieldMetadata(
                    "field", "int", 0, null, List.of(annotation)
            );

            assertThatThrownBy(() -> metadata.annotations().add(
                    new AnnotationMetadata("Test", Map.of(), true)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
