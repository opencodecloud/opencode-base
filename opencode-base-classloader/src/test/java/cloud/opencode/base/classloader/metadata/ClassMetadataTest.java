package cloud.opencode.base.classloader.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ClassMetadata
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("ClassMetadata Tests")
class ClassMetadataTest {

    private MethodMetadata createMethod(String name, String returnType) {
        return new MethodMetadata(name, returnType, List.of(), List.of(), List.of(),
                Modifier.PUBLIC, false, false, false, List.of(), List.of());
    }

    private MethodMetadata createMethod(String name, String returnType, List<String> paramTypes) {
        return new MethodMetadata(name, returnType, paramTypes, List.of(), List.of(),
                Modifier.PUBLIC, false, false, false, List.of(), List.of());
    }

    private FieldMetadata createField(String name, String type) {
        return new FieldMetadata(name, type, Modifier.PRIVATE, null, List.of());
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build with required fields")
        void shouldBuildWithRequiredFields() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .build();

            assertThat(metadata.className()).isEqualTo("com.example.MyClass");
        }

        @Test
        @DisplayName("Should build with all fields")
        void shouldBuildWithAllFields() {
            List<AnnotationMetadata> annotations = List.of(
                    new AnnotationMetadata("org.example.MyAnnotation", Map.of(), true)
            );
            List<MethodMetadata> methods = List.of(createMethod("myMethod", "void"));
            List<FieldMetadata> fields = List.of(createField("myField", "java.lang.String"));

            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .superClassName("java.lang.Object")
                    .interfaceNames(List.of("java.io.Serializable"))
                    .modifiers(Modifier.PUBLIC)
                    .isInterface(false)
                    .isAnnotation(false)
                    .isEnum(false)
                    .isRecord(false)
                    .isSealed(false)
                    .permittedSubclasses(List.of())
                    .methods(methods)
                    .fields(fields)
                    .annotations(annotations)
                    .sourceFile("MyClass.java")
                    .build();

            assertThat(metadata.className()).isEqualTo("com.example.MyClass");
            assertThat(metadata.superClassName()).isEqualTo("java.lang.Object");
            assertThat(metadata.interfaceNames()).containsExactly("java.io.Serializable");
            assertThat(metadata.modifiers()).isEqualTo(Modifier.PUBLIC);
        }

        @Test
        @DisplayName("Should throw on null class name")
        void shouldThrowOnNullClassName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClassMetadata.builder().build());
        }
    }

    @Nested
    @DisplayName("Name Extraction Tests")
    class NameExtractionTests {

        @Test
        @DisplayName("Should extract package name")
        void shouldExtractPackageName() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .build();

            assertThat(metadata.packageName()).isEqualTo("com.example");
        }

        @Test
        @DisplayName("Should extract simple name")
        void shouldExtractSimpleName() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .build();

            assertThat(metadata.simpleName()).isEqualTo("MyClass");
        }

        @Test
        @DisplayName("Should handle default package")
        void shouldHandleDefaultPackage() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("MyClass")
                    .build();

            assertThat(metadata.packageName()).isEmpty();
            assertThat(metadata.simpleName()).isEqualTo("MyClass");
        }

        @Test
        @DisplayName("Should extract inner class simple name")
        void shouldExtractInnerClassSimpleName() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.Outer$Inner")
                    .build();

            assertThat(metadata.simpleName()).isEqualTo("Inner");
        }
    }

    @Nested
    @DisplayName("Type Detection Tests")
    class TypeDetectionTests {

        @Test
        @DisplayName("Should detect interface")
        void shouldDetectInterface() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyInterface")
                    .isInterface(true)
                    .build();

            assertThat(metadata.isInterface()).isTrue();
        }

        @Test
        @DisplayName("Should detect annotation")
        void shouldDetectAnnotation() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyAnnotation")
                    .isAnnotation(true)
                    .build();

            assertThat(metadata.isAnnotation()).isTrue();
        }

        @Test
        @DisplayName("Should detect enum")
        void shouldDetectEnum() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyEnum")
                    .isEnum(true)
                    .build();

            assertThat(metadata.isEnum()).isTrue();
        }

        @Test
        @DisplayName("Should detect record")
        void shouldDetectRecord() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyRecord")
                    .isRecord(true)
                    .build();

            assertThat(metadata.isRecord()).isTrue();
        }

        @Test
        @DisplayName("Should detect sealed")
        void shouldDetectSealed() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MySealed")
                    .isSealed(true)
                    .permittedSubclasses(List.of("com.example.SubA", "com.example.SubB"))
                    .build();

            assertThat(metadata.isSealed()).isTrue();
            assertThat(metadata.permittedSubclasses()).containsExactly("com.example.SubA", "com.example.SubB");
        }

        @Test
        @DisplayName("Should detect abstract")
        void shouldDetectAbstract() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyAbstract")
                    .modifiers(Modifier.PUBLIC | Modifier.ABSTRACT)
                    .build();

            assertThat(metadata.isAbstract()).isTrue();
        }

        @Test
        @DisplayName("Should detect final")
        void shouldDetectFinal() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyFinal")
                    .modifiers(Modifier.PUBLIC | Modifier.FINAL)
                    .build();

            assertThat(metadata.isFinal()).isTrue();
        }

        @Test
        @DisplayName("Should detect concrete class")
        void shouldDetectConcreteClass() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .isInterface(false)
                    .modifiers(Modifier.PUBLIC)
                    .build();

            assertThat(metadata.isConcrete()).isTrue();
        }

        @Test
        @DisplayName("Should not detect concrete for interface")
        void shouldNotDetectConcreteForInterface() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyInterface")
                    .isInterface(true)
                    .build();

            assertThat(metadata.isConcrete()).isFalse();
        }

        @Test
        @DisplayName("Should not detect concrete for abstract")
        void shouldNotDetectConcreteForAbstract() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyAbstract")
                    .modifiers(Modifier.ABSTRACT)
                    .build();

            assertThat(metadata.isConcrete()).isFalse();
        }
    }

    @Nested
    @DisplayName("Inner Class Tests")
    class InnerClassTests {

        @Test
        @DisplayName("Should detect inner class")
        void shouldDetectInnerClass() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.Outer$Inner")
                    .build();

            assertThat(metadata.isInnerClass()).isTrue();
        }

        @Test
        @DisplayName("Should not detect top-level class as inner")
        void shouldNotDetectTopLevelClassAsInner() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .build();

            assertThat(metadata.isInnerClass()).isFalse();
        }

        @Test
        @DisplayName("Should get outer class name")
        void shouldGetOuterClassName() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.Outer$Inner")
                    .build();

            assertThat(metadata.getOuterClassName())
                    .isPresent()
                    .contains("com.example.Outer");
        }

        @Test
        @DisplayName("Should return empty for top-level class")
        void shouldReturnEmptyForTopLevelClass() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .build();

            assertThat(metadata.getOuterClassName()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Annotation Tests")
    class AnnotationTests {

        @Test
        @DisplayName("Should have annotation by class name")
        void shouldHaveAnnotationByClassName() {
            List<AnnotationMetadata> annotations = List.of(
                    new AnnotationMetadata("org.example.MyAnnotation", Map.of(), true)
            );

            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .annotations(annotations)
                    .build();

            assertThat(metadata.hasAnnotation("org.example.MyAnnotation")).isTrue();
        }

        @Test
        @DisplayName("Should have annotation by class")
        void shouldHaveAnnotationByClass() {
            List<AnnotationMetadata> annotations = List.of(
                    new AnnotationMetadata(Deprecated.class.getName(), Map.of(), true)
            );

            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .annotations(annotations)
                    .build();

            assertThat(metadata.hasAnnotation(Deprecated.class)).isTrue();
        }

        @Test
        @DisplayName("Should get annotation")
        void shouldGetAnnotation() {
            AnnotationMetadata annotation = new AnnotationMetadata(
                    "org.example.MyAnnotation", Map.of("value", "test"), true);
            List<AnnotationMetadata> annotations = List.of(annotation);

            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .annotations(annotations)
                    .build();

            assertThat(metadata.getAnnotation("org.example.MyAnnotation"))
                    .isPresent()
                    .contains(annotation);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should check subtype of superclass")
        void shouldCheckSubtypeOfSuperclass() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .superClassName("com.example.BaseClass")
                    .build();

            assertThat(metadata.isSubTypeOf("com.example.BaseClass")).isTrue();
        }

        @Test
        @DisplayName("Should check subtype of interface")
        void shouldCheckSubtypeOfInterface() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .interfaceNames(List.of("java.io.Serializable"))
                    .build();

            assertThat(metadata.isSubTypeOf("java.io.Serializable")).isTrue();
        }

        @Test
        @DisplayName("Should not be subtype of unrelated class")
        void shouldNotBeSubtypeOfUnrelatedClass() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .superClassName("java.lang.Object")
                    .interfaceNames(List.of())
                    .build();

            assertThat(metadata.isSubTypeOf("com.example.Other")).isFalse();
        }
    }

    @Nested
    @DisplayName("Member Access Tests")
    class MemberAccessTests {

        @Test
        @DisplayName("Should get method names")
        void shouldGetMethodNames() {
            List<MethodMetadata> methods = List.of(
                    createMethod("method1", "void"),
                    createMethod("method2", "void")
            );

            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .methods(methods)
                    .build();

            assertThat(metadata.getMethodNames()).containsExactly("method1", "method2");
        }

        @Test
        @DisplayName("Should get field names")
        void shouldGetFieldNames() {
            List<FieldMetadata> fields = List.of(
                    createField("field1", "int"),
                    createField("field2", "String")
            );

            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .fields(fields)
                    .build();

            assertThat(metadata.getFieldNames()).containsExactly("field1", "field2");
        }

        @Test
        @DisplayName("Should get methods by name")
        void shouldGetMethodsByName() {
            List<MethodMetadata> methods = List.of(
                    createMethod("method", "void", List.of()),
                    createMethod("method", "void", List.of("int")),
                    createMethod("other", "void")
            );

            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .methods(methods)
                    .build();

            assertThat(metadata.getMethodsByName("method")).hasSize(2);
        }

        @Test
        @DisplayName("Should get field by name")
        void shouldGetFieldByName() {
            FieldMetadata field = createField("myField", "int");
            List<FieldMetadata> fields = List.of(field);

            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .fields(fields)
                    .build();

            assertThat(metadata.getField("myField"))
                    .isPresent()
                    .contains(field);
        }

        @Test
        @DisplayName("Should return empty for nonexistent field")
        void shouldReturnEmptyForNonexistentField() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .fields(List.of())
                    .build();

            assertThat(metadata.getField("nonexistent")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Generic Signature Tests")
    class GenericSignatureTests {

        @Test
        @DisplayName("Should store generic signature")
        void shouldStoreGenericSignature() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .genericSignature("<T extends Comparable<T>>")
                    .build();

            assertThat(metadata.getGenericSignature()).isEqualTo("<T extends Comparable<T>>");
        }

        @Test
        @DisplayName("Should return null for non-generic class")
        void shouldReturnNullForNonGenericClass() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .build();

            assertThat(metadata.getGenericSignature()).isNull();
        }

        @Test
        @DisplayName("Should store type parameters")
        void shouldStoreTypeParameters() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .typeParameters(List.of("T", "K extends Comparable<K>"))
                    .build();

            assertThat(metadata.getTypeParameters()).containsExactly("T", "K extends Comparable<K>");
        }

        @Test
        @DisplayName("Should return empty list for no type parameters")
        void shouldReturnEmptyListForNoTypeParameters() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .build();

            assertThat(metadata.getTypeParameters()).isEmpty();
        }

        @Test
        @DisplayName("Should check has type parameters")
        void shouldCheckHasTypeParameters() {
            ClassMetadata withParams = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .typeParameters(List.of("T"))
                    .build();

            ClassMetadata withoutParams = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .build();

            assertThat(withParams.hasTypeParameters()).isTrue();
            assertThat(withoutParams.hasTypeParameters()).isFalse();
        }

        @Test
        @DisplayName("Should return immutable type parameters list")
        void shouldReturnImmutableTypeParametersList() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .typeParameters(List.of("T"))
                    .build();

            assertThatThrownBy(() -> metadata.getTypeParameters().add("K"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Record Component Tests")
    class RecordComponentTests {

        @Test
        @DisplayName("Should store record components")
        void shouldStoreRecordComponents() {
            List<RecordComponentMetadata> components = List.of(
                    new RecordComponentMetadata("name", "java.lang.String", null, List.of()),
                    new RecordComponentMetadata("age", "int", null, List.of())
            );

            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.Person")
                    .isRecord(true)
                    .recordComponents(components)
                    .build();

            assertThat(metadata.getRecordComponents()).hasSize(2);
            assertThat(metadata.getRecordComponents().get(0).name()).isEqualTo("name");
            assertThat(metadata.getRecordComponents().get(1).name()).isEqualTo("age");
        }

        @Test
        @DisplayName("Should return empty list for non-record class")
        void shouldReturnEmptyListForNonRecordClass() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .build();

            assertThat(metadata.getRecordComponents()).isEmpty();
        }

        @Test
        @DisplayName("Should return immutable record components list")
        void shouldReturnImmutableRecordComponentsList() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.Person")
                    .isRecord(true)
                    .recordComponents(List.of(
                            new RecordComponentMetadata("name", "java.lang.String", null, List.of())
                    ))
                    .build();

            assertThatThrownBy(() -> metadata.getRecordComponents().add(
                    new RecordComponentMetadata("extra", "int", null, List.of())))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal for same class name")
        void shouldBeEqualForSameClassName() {
            ClassMetadata metadata1 = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .build();
            ClassMetadata metadata2 = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .build();

            assertThat(metadata1).isEqualTo(metadata2);
            assertThat(metadata1.hashCode()).isEqualTo(metadata2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different class names")
        void shouldNotBeEqualForDifferentClassNames() {
            ClassMetadata metadata1 = ClassMetadata.builder()
                    .className("com.example.MyClass1")
                    .build();
            ClassMetadata metadata2 = ClassMetadata.builder()
                    .className("com.example.MyClass2")
                    .build();

            assertThat(metadata1).isNotEqualTo(metadata2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should include class type in toString")
        void shouldIncludeClassTypeInToString() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyClass")
                    .modifiers(Modifier.PUBLIC)
                    .build();

            assertThat(metadata.toString()).contains("class").contains("MyClass");
        }

        @Test
        @DisplayName("Should show interface in toString")
        void shouldShowInterfaceInToString() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyInterface")
                    .isInterface(true)
                    .build();

            assertThat(metadata.toString()).contains("interface");
        }

        @Test
        @DisplayName("Should show enum in toString")
        void shouldShowEnumInToString() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyEnum")
                    .isEnum(true)
                    .build();

            assertThat(metadata.toString()).contains("enum");
        }

        @Test
        @DisplayName("Should show record in toString")
        void shouldShowRecordInToString() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyRecord")
                    .isRecord(true)
                    .build();

            assertThat(metadata.toString()).contains("record");
        }

        @Test
        @DisplayName("Should show annotation in toString")
        void shouldShowAnnotationInToString() {
            ClassMetadata metadata = ClassMetadata.builder()
                    .className("com.example.MyAnnotation")
                    .isAnnotation(true)
                    .build();

            assertThat(metadata.toString()).contains("@interface");
        }
    }
}
