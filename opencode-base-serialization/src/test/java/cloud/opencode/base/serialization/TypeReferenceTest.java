package cloud.opencode.base.serialization;

import org.junit.jupiter.api.*;

import java.lang.reflect.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TypeReferenceTest Tests
 * TypeReferenceTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
@DisplayName("TypeReference Tests")
class TypeReferenceTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should capture generic type from anonymous subclass")
        void shouldCaptureGenericTypeFromAnonymousSubclass() {
            TypeReference<List<String>> typeRef = new TypeReference<>() {};

            assertThat(typeRef.getType()).isNotNull();
            assertThat(typeRef.getRawType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Should capture nested generic type")
        void shouldCaptureNestedGenericType() {
            TypeReference<Map<String, List<Integer>>> typeRef = new TypeReference<>() {};

            assertThat(typeRef.getRawType()).isEqualTo(Map.class);
            assertThat(typeRef.isParameterized()).isTrue();
            assertThat(typeRef.getTypeArguments()).hasSize(2);
        }

        @Test
        @DisplayName("Should capture simple class type")
        void shouldCaptureSimpleClassType() {
            TypeReference<String> typeRef = new TypeReference<>() {};

            assertThat(typeRef.getRawType()).isEqualTo(String.class);
            assertThat(typeRef.isParameterized()).isFalse();
        }
    }

    @Nested
    @DisplayName("Factory Method of(Class) Tests")
    class OfClassTests {

        @Test
        @DisplayName("of(Class) should create TypeReference for class")
        void ofClassShouldCreateTypeReference() {
            TypeReference<String> typeRef = TypeReference.of(String.class);

            assertThat(typeRef.getType()).isEqualTo(String.class);
            assertThat(typeRef.getRawType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("of(Class) should reject null")
        void ofClassShouldRejectNull() {
            assertThatThrownBy(() -> TypeReference.of((Class<?>) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Class");
        }
    }

    @Nested
    @DisplayName("Factory Method of(Type) Tests")
    class OfTypeTests {

        @Test
        @DisplayName("of(Type) should create TypeReference for Type")
        void ofTypeShouldCreateTypeReference() {
            Type type = new TypeReference<List<String>>() {}.getType();
            TypeReference<List<String>> typeRef = TypeReference.of(type);

            assertThat(typeRef.getType()).isEqualTo(type);
            assertThat(typeRef.getRawType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("of(Type) should reject null")
        void ofTypeShouldRejectNull() {
            assertThatThrownBy(() -> TypeReference.of((Type) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Type");
        }
    }

    @Nested
    @DisplayName("Factory Method listOf Tests")
    class ListOfTests {

        @Test
        @DisplayName("listOf should create List TypeReference")
        void listOfShouldCreateListTypeReference() {
            TypeReference<List<String>> typeRef = TypeReference.listOf(String.class);

            assertThat(typeRef.getRawType()).isEqualTo(List.class);
            assertThat(typeRef.isParameterized()).isTrue();
            assertThat(typeRef.getTypeArguments()).containsExactly(String.class);
        }

        @Test
        @DisplayName("listOf should reject null")
        void listOfShouldRejectNull() {
            assertThatThrownBy(() -> TypeReference.listOf(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Element type");
        }
    }

    @Nested
    @DisplayName("Factory Method setOf Tests")
    class SetOfTests {

        @Test
        @DisplayName("setOf should create Set TypeReference")
        void setOfShouldCreateSetTypeReference() {
            TypeReference<Set<Integer>> typeRef = TypeReference.setOf(Integer.class);

            assertThat(typeRef.getRawType()).isEqualTo(Set.class);
            assertThat(typeRef.isParameterized()).isTrue();
            assertThat(typeRef.getTypeArguments()).containsExactly(Integer.class);
        }

        @Test
        @DisplayName("setOf should reject null")
        void setOfShouldRejectNull() {
            assertThatThrownBy(() -> TypeReference.setOf(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Element type");
        }
    }

    @Nested
    @DisplayName("Factory Method mapOf Tests")
    class MapOfTests {

        @Test
        @DisplayName("mapOf should create Map TypeReference")
        void mapOfShouldCreateMapTypeReference() {
            TypeReference<Map<String, Integer>> typeRef = TypeReference.mapOf(String.class, Integer.class);

            assertThat(typeRef.getRawType()).isEqualTo(Map.class);
            assertThat(typeRef.isParameterized()).isTrue();
            assertThat(typeRef.getTypeArguments()).containsExactly(String.class, Integer.class);
        }

        @Test
        @DisplayName("mapOf should reject null key type")
        void mapOfShouldRejectNullKeyType() {
            assertThatThrownBy(() -> TypeReference.mapOf(null, String.class))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Key type");
        }

        @Test
        @DisplayName("mapOf should reject null value type")
        void mapOfShouldRejectNullValueType() {
            assertThatThrownBy(() -> TypeReference.mapOf(String.class, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Value type");
        }
    }

    @Nested
    @DisplayName("Factory Method collectionOf Tests")
    class CollectionOfTests {

        @Test
        @DisplayName("collectionOf should create Collection TypeReference")
        void collectionOfShouldCreateCollectionTypeReference() {
            TypeReference<Collection<Double>> typeRef = TypeReference.collectionOf(Double.class);

            assertThat(typeRef.getRawType()).isEqualTo(Collection.class);
            assertThat(typeRef.isParameterized()).isTrue();
            assertThat(typeRef.getTypeArguments()).containsExactly(Double.class);
        }

        @Test
        @DisplayName("collectionOf should reject null")
        void collectionOfShouldRejectNull() {
            assertThatThrownBy(() -> TypeReference.collectionOf(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Element type");
        }
    }

    @Nested
    @DisplayName("Factory Method optionalOf Tests")
    class OptionalOfTests {

        @Test
        @DisplayName("optionalOf should create Optional TypeReference")
        void optionalOfShouldCreateOptionalTypeReference() {
            TypeReference<Optional<String>> typeRef = TypeReference.optionalOf(String.class);

            assertThat(typeRef.getRawType()).isEqualTo(Optional.class);
            assertThat(typeRef.isParameterized()).isTrue();
            assertThat(typeRef.getTypeArguments()).containsExactly(String.class);
        }

        @Test
        @DisplayName("optionalOf should reject null")
        void optionalOfShouldRejectNull() {
            assertThatThrownBy(() -> TypeReference.optionalOf(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Element type");
        }
    }

    @Nested
    @DisplayName("Getter Methods Tests")
    class GetterMethodsTests {

        @Test
        @DisplayName("getType should return the type")
        void getTypeShouldReturnType() {
            TypeReference<List<String>> typeRef = new TypeReference<>() {};

            Type type = typeRef.getType();

            assertThat(type).isInstanceOf(ParameterizedType.class);
        }

        @Test
        @DisplayName("getRawType should return raw class")
        void getRawTypeShouldReturnRawClass() {
            TypeReference<Map<String, Integer>> typeRef = new TypeReference<>() {};

            assertThat(typeRef.getRawType()).isEqualTo(Map.class);
        }

        @Test
        @DisplayName("isParameterized should return true for parameterized types")
        void isParameterizedShouldReturnTrueForParameterizedTypes() {
            TypeReference<List<String>> typeRef = new TypeReference<>() {};

            assertThat(typeRef.isParameterized()).isTrue();
        }

        @Test
        @DisplayName("isParameterized should return false for non-parameterized types")
        void isParameterizedShouldReturnFalseForNonParameterizedTypes() {
            TypeReference<String> typeRef = TypeReference.of(String.class);

            assertThat(typeRef.isParameterized()).isFalse();
        }

        @Test
        @DisplayName("getTypeArguments should return type arguments")
        void getTypeArgumentsShouldReturnTypeArguments() {
            TypeReference<Map<String, Integer>> typeRef = new TypeReference<>() {};

            Type[] typeArgs = typeRef.getTypeArguments();

            assertThat(typeArgs).hasSize(2);
            assertThat(typeArgs[0]).isEqualTo(String.class);
            assertThat(typeArgs[1]).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("getTypeArguments should return empty array for non-parameterized types")
        void getTypeArgumentsShouldReturnEmptyArrayForNonParameterizedTypes() {
            TypeReference<String> typeRef = TypeReference.of(String.class);

            assertThat(typeRef.getTypeArguments()).isEmpty();
        }
    }

    @Nested
    @DisplayName("equals and hashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            TypeReference<List<String>> typeRef = new TypeReference<>() {};

            assertThat(typeRef).isEqualTo(typeRef);
        }

        @Test
        @DisplayName("Should be equal to another with same type")
        void shouldBeEqualToAnotherWithSameType() {
            TypeReference<List<String>> typeRef1 = TypeReference.listOf(String.class);
            TypeReference<List<String>> typeRef2 = TypeReference.listOf(String.class);

            assertThat(typeRef1).isEqualTo(typeRef2);
            assertThat(typeRef1.hashCode()).isEqualTo(typeRef2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            TypeReference<List<String>> typeRef1 = TypeReference.listOf(String.class);
            TypeReference<List<Integer>> typeRef2 = TypeReference.listOf(Integer.class);

            assertThat(typeRef1).isNotEqualTo(typeRef2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            TypeReference<List<String>> typeRef = new TypeReference<>() {};

            assertThat(typeRef).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different object type")
        void shouldNotBeEqualToDifferentObjectType() {
            TypeReference<List<String>> typeRef = new TypeReference<>() {};

            assertThat(typeRef).isNotEqualTo("string");
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should contain type name")
        void toStringShouldContainTypeName() {
            TypeReference<List<String>> typeRef = new TypeReference<>() {};

            String str = typeRef.toString();

            assertThat(str).contains("TypeReference");
            assertThat(str).contains("List");
            assertThat(str).contains("String");
        }

        @Test
        @DisplayName("toString should work for simple class")
        void toStringShouldWorkForSimpleClass() {
            TypeReference<String> typeRef = TypeReference.of(String.class);

            String str = typeRef.toString();

            assertThat(str).contains("TypeReference");
            assertThat(str).contains("String");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle array types")
        void shouldHandleArrayTypes() {
            TypeReference<String[]> typeRef = TypeReference.of(String[].class);

            assertThat(typeRef.getRawType()).isEqualTo(String[].class);
        }

        @Test
        @DisplayName("Should handle primitive wrapper types")
        void shouldHandlePrimitiveWrapperTypes() {
            TypeReference<Integer> typeRef = TypeReference.of(Integer.class);

            assertThat(typeRef.getRawType()).isEqualTo(Integer.class);
            assertThat(typeRef.isParameterized()).isFalse();
        }
    }
}
