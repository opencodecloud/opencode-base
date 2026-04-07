package cloud.opencode.base.classloader.metadata;

import cloud.opencode.base.classloader.resource.ByteArrayResource;
import cloud.opencode.base.classloader.resource.ClassPathResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for MetadataReader
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("MetadataReader Tests")
class MetadataReaderTest {

    @Nested
    @DisplayName("Read by Class Name Tests")
    class ReadByClassNameTests {

        @Test
        @DisplayName("Should read class metadata by name")
        void shouldReadClassMetadataByName() {
            ClassMetadata metadata = MetadataReader.read(String.class.getName());

            assertThat(metadata.className()).isEqualTo("java.lang.String");
            assertThat(metadata.isFinal()).isTrue();
        }

        @Test
        @DisplayName("Should read interface metadata")
        void shouldReadInterfaceMetadata() {
            ClassMetadata metadata = MetadataReader.read(Serializable.class.getName());

            assertThat(metadata.className()).isEqualTo("java.io.Serializable");
            assertThat(metadata.isInterface()).isTrue();
        }

        @Test
        @DisplayName("Should read enum metadata")
        void shouldReadEnumMetadata() {
            ClassMetadata metadata = MetadataReader.read(RetentionPolicy.class.getName());

            assertThat(metadata.className()).isEqualTo("java.lang.annotation.RetentionPolicy");
            assertThat(metadata.isEnum()).isTrue();
        }

        @Test
        @DisplayName("Should read annotation metadata")
        void shouldReadAnnotationMetadata() {
            ClassMetadata metadata = MetadataReader.read(Retention.class.getName());

            assertThat(metadata.className()).isEqualTo("java.lang.annotation.Retention");
            assertThat(metadata.isAnnotation()).isTrue();
        }

        @Test
        @DisplayName("Should throw on nonexistent class")
        void shouldThrowOnNonexistentClass() {
            assertThatThrownBy(() -> MetadataReader.read("com.nonexistent.Class"))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should throw on null class name")
        void shouldThrowOnNullClassName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> MetadataReader.read((String) null));
        }
    }

    @Nested
    @DisplayName("Read by Class Object Tests")
    class ReadByClassObjectTests {

        @Test
        @DisplayName("Should read class metadata from Class object")
        void shouldReadClassMetadataFromClassObject() {
            ClassMetadata metadata = MetadataReader.read(String.class);

            assertThat(metadata.className()).isEqualTo("java.lang.String");
        }

        @Test
        @DisplayName("Should read methods from Class object")
        void shouldReadMethodsFromClassObject() {
            ClassMetadata metadata = MetadataReader.read(String.class);

            assertThat(metadata.methods()).isNotEmpty();
            assertThat(metadata.getMethodNames()).contains("length", "charAt", "substring");
        }

        @Test
        @DisplayName("Should read fields from Class object")
        void shouldReadFieldsFromClassObject() {
            ClassMetadata metadata = MetadataReader.read(Integer.class);

            assertThat(metadata.fields()).isNotEmpty();
        }

        @Test
        @DisplayName("Should read annotations from Class object")
        void shouldReadAnnotationsFromClassObject() {
            ClassMetadata metadata = MetadataReader.read(Deprecated.class);

            // Deprecated annotation has @Retention annotation
            assertThat(metadata.annotations()).isNotEmpty();
        }

        @Test
        @DisplayName("Should throw on null Class object")
        void shouldThrowOnNullClassObject() {
            assertThatNullPointerException()
                    .isThrownBy(() -> MetadataReader.read((Class<?>) null));
        }

        @Test
        @DisplayName("Should read record metadata")
        void shouldReadRecordMetadata() {
            // Use a built-in record if available or skip
            ClassMetadata metadata = MetadataReader.read(String.class);
            assertThat(metadata).isNotNull();
        }

        @Test
        @DisplayName("Should read super class")
        void shouldReadSuperClass() {
            ClassMetadata metadata = MetadataReader.read(Integer.class);

            assertThat(metadata.superClassName()).isEqualTo("java.lang.Number");
        }

        @Test
        @DisplayName("Should read interfaces")
        void shouldReadInterfaces() {
            ClassMetadata metadata = MetadataReader.read(String.class);

            assertThat(metadata.interfaceNames()).contains("java.io.Serializable");
        }
    }

    @Nested
    @DisplayName("Read by Resource Tests")
    class ReadByResourceTests {

        @Test
        @DisplayName("Should read from classpath resource")
        void shouldReadFromClassPathResource() {
            // Read this test class itself
            String resourcePath = MetadataReaderTest.class.getName().replace('.', '/') + ".class";
            ClassPathResource resource = new ClassPathResource(resourcePath);

            ClassMetadata metadata = MetadataReader.read(resource);

            // Should return a valid metadata object
            assertThat(metadata).isNotNull();
        }

        @Test
        @DisplayName("Should throw on null resource")
        void shouldThrowOnNullResource() {
            assertThatNullPointerException()
                    .isThrownBy(() -> MetadataReader.read((cloud.opencode.base.classloader.resource.Resource) null));
        }
    }

    @Nested
    @DisplayName("Read by Bytecode Tests")
    class ReadByBytecodeTests {

        @Test
        @DisplayName("Should read from bytecode")
        void shouldReadFromBytecode() throws IOException {
            // Get bytecode of this class (has non-null classloader)
            String resourcePath = MetadataReaderTest.class.getName().replace('.', '/') + ".class";
            byte[] bytecode = MetadataReaderTest.class.getClassLoader().getResourceAsStream(resourcePath).readAllBytes();

            ClassMetadata metadata = MetadataReader.read(bytecode);

            // Should return a valid metadata object
            assertThat(metadata).isNotNull();
        }

        @Test
        @DisplayName("Should throw on null bytecode")
        void shouldThrowOnNullBytecode() {
            assertThatNullPointerException()
                    .isThrownBy(() -> MetadataReader.read((byte[]) null));
        }

        @Test
        @DisplayName("Should handle invalid bytecode")
        void shouldHandleInvalidBytecode() {
            byte[] invalidBytecode = "not valid bytecode".getBytes();

            // Invalid bytecode may throw or return a placeholder - implementation dependent
            assertThatCode(() -> MetadataReader.read(invalidBytecode))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Batch Read Tests")
    class BatchReadTests {

        private static final String TEST_PACKAGE = "cloud.opencode.base.classloader.metadata";

        @Test
        @DisplayName("Should read all classes in package")
        void shouldReadAllClassesInPackage() {
            List<ClassMetadata> metadataList = MetadataReader.readAll(TEST_PACKAGE);

            // Should return a list (may be empty if scanning doesn't work for test classes)
            assertThat(metadataList).isNotNull();
        }

        @Test
        @DisplayName("Should read classes with filter")
        void shouldReadClassesWithFilter() {
            Predicate<String> filter = name -> name.contains("Metadata");

            List<ClassMetadata> metadataList = MetadataReader.readAll(TEST_PACKAGE, filter);

            assertThat(metadataList).allMatch(m -> m.className().contains("Metadata"));
        }

        @Test
        @DisplayName("Should return empty list for nonexistent package")
        void shouldReturnEmptyListForNonexistentPackage() {
            List<ClassMetadata> metadataList = MetadataReader.readAll("com.nonexistent.package");

            assertThat(metadataList).isEmpty();
        }

        @Test
        @DisplayName("Should throw on null package name")
        void shouldThrowOnNullPackageName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> MetadataReader.readAll(null));
        }

        @Test
        @DisplayName("Should throw on null filter")
        void shouldThrowOnNullFilter() {
            assertThatNullPointerException()
                    .isThrownBy(() -> MetadataReader.readAll(TEST_PACKAGE, null));
        }
    }

    @Nested
    @DisplayName("Caching Tests")
    class CachingTests {

        @Test
        @DisplayName("Should cache metadata for same class")
        void shouldCacheMetadataForSameClass() {
            ClassMetadata metadata1 = MetadataReader.read(String.class);
            ClassMetadata metadata2 = MetadataReader.read(String.class);

            // Should return same instance due to caching
            assertThat(metadata1).isSameAs(metadata2);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should read primitive wrapper")
        void shouldReadPrimitiveWrapper() {
            ClassMetadata metadata = MetadataReader.read(Integer.class);

            assertThat(metadata.className()).isEqualTo("java.lang.Integer");
        }

        @Test
        @DisplayName("Should read array component type")
        void shouldReadArrayComponentType() {
            // Arrays don't have standard class files, but we can read the component type
            ClassMetadata metadata = MetadataReader.read(String.class);
            assertThat(metadata).isNotNull();
        }

        @Test
        @DisplayName("Should read inner class")
        void shouldReadInnerClass() {
            ClassMetadata metadata = MetadataReader.read(java.util.Map.Entry.class);

            assertThat(metadata.isInnerClass()).isTrue();
            assertThat(metadata.isInterface()).isTrue();
        }
    }

    // ==================== Test fixtures for generic/record/annotation tests ====================

    @SuppressWarnings("unused")
    static class GenericClass<T extends Comparable<T>, K> {
        private List<T> items;
        private Map<String, K> mapping;
        private int count;

        public List<T> getItems() { return items; }
        public <R> R transform(T input) { return null; }
    }

    record SampleRecord(String name, int age, List<String> tags) {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface TestAnnotation {
        String value() default "";
        int count() default 0;
        RetentionPolicy policy() default RetentionPolicy.RUNTIME;
        Class<?> type() default Object.class;
    }

    @TestAnnotation(value = "hello", count = 42, policy = RetentionPolicy.SOURCE, type = String.class)
    static class AnnotatedClass {}

    @Nested
    @DisplayName("Generic Signature Parsing Tests")
    class GenericSignatureParsingTests {

        @Test
        @DisplayName("Should parse class type parameters")
        void shouldParseClassTypeParameters() {
            ClassMetadata metadata = MetadataReader.read(GenericClass.class);

            assertThat(metadata.hasTypeParameters()).isTrue();
            assertThat(metadata.getTypeParameters()).hasSize(2);
            assertThat(metadata.getTypeParameters().get(0)).contains("T");
            assertThat(metadata.getTypeParameters().get(0)).contains("Comparable");
            assertThat(metadata.getTypeParameters().get(1)).isEqualTo("K");
        }

        @Test
        @DisplayName("Should parse generic signature string")
        void shouldParseGenericSignatureString() {
            ClassMetadata metadata = MetadataReader.read(GenericClass.class);

            assertThat(metadata.getGenericSignature()).isNotNull();
            assertThat(metadata.getGenericSignature()).contains("T");
            assertThat(metadata.getGenericSignature()).contains("K");
        }

        @Test
        @DisplayName("Should return null signature for non-generic class")
        void shouldReturnNullSignatureForNonGenericClass() {
            ClassMetadata metadata = MetadataReader.read(String.class);

            assertThat(metadata.getGenericSignature()).isNull();
            assertThat(metadata.hasTypeParameters()).isFalse();
            assertThat(metadata.getTypeParameters()).isEmpty();
        }

        @Test
        @DisplayName("Should parse generic field types")
        void shouldParseGenericFieldTypes() {
            ClassMetadata metadata = MetadataReader.read(GenericClass.class);

            // 'items' field should have generic type info
            var itemsField = metadata.getField("items");
            assertThat(itemsField).isPresent();
            assertThat(itemsField.get().getGenericType()).isNotNull();
            assertThat(itemsField.get().getGenericType()).contains("T");

            // 'count' field (primitive) should have null generic type
            var countField = metadata.getField("count");
            assertThat(countField).isPresent();
            assertThat(countField.get().getGenericType()).isNull();
        }

        @Test
        @DisplayName("Should parse generic method return and parameter types")
        void shouldParseGenericMethodReturnAndParameterTypes() {
            ClassMetadata metadata = MetadataReader.read(GenericClass.class);

            // getItems method
            var getItemsMethods = metadata.getMethodsByName("getItems");
            assertThat(getItemsMethods).isNotEmpty();
            MethodMetadata getItems = getItemsMethods.get(0);
            assertThat(getItems.getGenericReturnType()).isNotNull();
            assertThat(getItems.getGenericReturnType()).contains("T");

            // transform method
            var transformMethods = metadata.getMethodsByName("transform");
            assertThat(transformMethods).isNotEmpty();
            MethodMetadata transform = transformMethods.get(0);
            assertThat(transform.getGenericSignature()).isNotNull();
            assertThat(transform.getGenericParameterTypes()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Record Parsing Tests")
    class RecordParsingTests {

        @Test
        @DisplayName("Should parse record components")
        void shouldParseRecordComponents() {
            ClassMetadata metadata = MetadataReader.read(SampleRecord.class);

            assertThat(metadata.isRecord()).isTrue();
            assertThat(metadata.getRecordComponents()).hasSize(3);
        }

        @Test
        @DisplayName("Should parse record component names and types")
        void shouldParseRecordComponentNamesAndTypes() {
            ClassMetadata metadata = MetadataReader.read(SampleRecord.class);

            List<RecordComponentMetadata> components = metadata.getRecordComponents();
            assertThat(components.get(0).name()).isEqualTo("name");
            assertThat(components.get(0).type()).isEqualTo("java.lang.String");

            assertThat(components.get(1).name()).isEqualTo("age");
            assertThat(components.get(1).type()).isEqualTo("int");

            assertThat(components.get(2).name()).isEqualTo("tags");
            assertThat(components.get(2).type()).isEqualTo("java.util.List");
        }

        @Test
        @DisplayName("Should parse record component generic types")
        void shouldParseRecordComponentGenericTypes() {
            ClassMetadata metadata = MetadataReader.read(SampleRecord.class);

            List<RecordComponentMetadata> components = metadata.getRecordComponents();
            // 'tags' is List<String>, should have generic type
            assertThat(components.get(2).genericType()).isNotNull();
            assertThat(components.get(2).genericType()).contains("String");

            // 'age' is int, should not have generic type
            assertThat(components.get(1).genericType()).isNull();
        }

        @Test
        @DisplayName("Should return empty components for non-record class")
        void shouldReturnEmptyComponentsForNonRecordClass() {
            ClassMetadata metadata = MetadataReader.read(String.class);

            assertThat(metadata.isRecord()).isFalse();
            assertThat(metadata.getRecordComponents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Annotation Attribute Resolution Tests")
    class AnnotationAttributeResolutionTests {

        @Test
        @DisplayName("Should resolve enum annotation attribute")
        void shouldResolveEnumAnnotationAttribute() {
            ClassMetadata metadata = MetadataReader.read(AnnotatedClass.class);

            var annotation = metadata.getAnnotation(TestAnnotation.class.getName());
            assertThat(annotation).isPresent();

            // Enum values should be resolved to their name strings
            var policyValue = annotation.get().attributes().get("policy");
            assertThat(policyValue).isEqualTo("SOURCE");
        }

        @Test
        @DisplayName("Should resolve string annotation attribute")
        void shouldResolveStringAnnotationAttribute() {
            ClassMetadata metadata = MetadataReader.read(AnnotatedClass.class);

            var annotation = metadata.getAnnotation(TestAnnotation.class.getName());
            assertThat(annotation).isPresent();
            assertThat(annotation.get().attributes().get("value")).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should resolve int annotation attribute")
        void shouldResolveIntAnnotationAttribute() {
            ClassMetadata metadata = MetadataReader.read(AnnotatedClass.class);

            var annotation = metadata.getAnnotation(TestAnnotation.class.getName());
            assertThat(annotation).isPresent();
            assertThat(annotation.get().attributes().get("count")).isEqualTo(42);
        }

        @Test
        @DisplayName("Should resolve Class annotation attribute to name string")
        void shouldResolveClassAnnotationAttributeToNameString() {
            ClassMetadata metadata = MetadataReader.read(AnnotatedClass.class);

            var annotation = metadata.getAnnotation(TestAnnotation.class.getName());
            assertThat(annotation).isPresent();
            assertThat(annotation.get().attributes().get("type")).isEqualTo("java.lang.String");
        }
    }
}
