package cloud.opencode.base.yml.security;

import cloud.opencode.base.yml.exception.YmlSecurityException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SafeConstructorTest Tests
 * SafeConstructorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("SafeConstructor Tests")
class SafeConstructorTest {

    // ==================== BASIC_SAFE_TYPES Constant Tests ====================

    @Nested
    @DisplayName("BASIC_SAFE_TYPES constant")
    class BasicSafeTypesConstantTests {

        @Test
        @DisplayName("should contain String class")
        void shouldContainStringClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(String.class);
        }

        @Test
        @DisplayName("should contain Integer class")
        void shouldContainIntegerClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(Integer.class);
        }

        @Test
        @DisplayName("should contain int primitive")
        void shouldContainIntPrimitive() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(int.class);
        }

        @Test
        @DisplayName("should contain Long class")
        void shouldContainLongClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(Long.class);
        }

        @Test
        @DisplayName("should contain long primitive")
        void shouldContainLongPrimitive() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(long.class);
        }

        @Test
        @DisplayName("should contain Double class")
        void shouldContainDoubleClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(Double.class);
        }

        @Test
        @DisplayName("should contain double primitive")
        void shouldContainDoublePrimitive() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(double.class);
        }

        @Test
        @DisplayName("should contain Float class")
        void shouldContainFloatClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(Float.class);
        }

        @Test
        @DisplayName("should contain float primitive")
        void shouldContainFloatPrimitive() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(float.class);
        }

        @Test
        @DisplayName("should contain Boolean class")
        void shouldContainBooleanClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(Boolean.class);
        }

        @Test
        @DisplayName("should contain boolean primitive")
        void shouldContainBooleanPrimitive() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(boolean.class);
        }

        @Test
        @DisplayName("should contain Short class")
        void shouldContainShortClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(Short.class);
        }

        @Test
        @DisplayName("should contain short primitive")
        void shouldContainShortPrimitive() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(short.class);
        }

        @Test
        @DisplayName("should contain Byte class")
        void shouldContainByteClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(Byte.class);
        }

        @Test
        @DisplayName("should contain byte primitive")
        void shouldContainBytePrimitive() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(byte.class);
        }

        @Test
        @DisplayName("should contain Character class")
        void shouldContainCharacterClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(Character.class);
        }

        @Test
        @DisplayName("should contain char primitive")
        void shouldContainCharPrimitive() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(char.class);
        }

        @Test
        @DisplayName("should contain BigDecimal class")
        void shouldContainBigDecimalClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(BigDecimal.class);
        }

        @Test
        @DisplayName("should contain BigInteger class")
        void shouldContainBigIntegerClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(BigInteger.class);
        }

        @Test
        @DisplayName("should contain Date class")
        void shouldContainDateClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(Date.class);
        }

        @Test
        @DisplayName("should contain LocalDate class")
        void shouldContainLocalDateClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(LocalDate.class);
        }

        @Test
        @DisplayName("should contain LocalDateTime class")
        void shouldContainLocalDateTimeClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(LocalDateTime.class);
        }

        @Test
        @DisplayName("should contain LocalTime class")
        void shouldContainLocalTimeClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(LocalTime.class);
        }

        @Test
        @DisplayName("should contain Instant class")
        void shouldContainInstantClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(Instant.class);
        }

        @Test
        @DisplayName("should contain UUID class")
        void shouldContainUuidClass() {
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).contains(UUID.class);
        }

        @Test
        @DisplayName("should be immutable set")
        void shouldBeImmutableSet() {
            assertThatThrownBy(() -> SafeConstructor.BASIC_SAFE_TYPES.add(Object.class))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should have expected number of types")
        void shouldHaveExpectedNumberOfTypes() {
            // 25 types: String, Integer/int, Long/long, Double/double, Float/float,
            // Boolean/boolean, Short/short, Byte/byte, Character/char,
            // BigDecimal, BigInteger, Date, LocalDate, LocalDateTime, LocalTime, Instant, UUID
            // Count: 1 + 2*8 + 2 + 1 + 5 + 1 = 25
            assertThat(SafeConstructor.BASIC_SAFE_TYPES).hasSize(25);
        }
    }

    // ==================== create() Factory Method Tests ====================

    @Nested
    @DisplayName("create() factory method")
    class CreateFactoryMethodTests {

        @Test
        @DisplayName("should return non-null SafeConstructor")
        void shouldReturnNonNullSafeConstructor() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThat(constructor).isNotNull();
        }

        @Test
        @DisplayName("should return DefaultSafeConstructor instance")
        void shouldReturnDefaultSafeConstructorInstance() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThat(constructor).isInstanceOf(SafeConstructor.DefaultSafeConstructor.class);
        }

        @Test
        @DisplayName("should return new instance each time")
        void shouldReturnNewInstanceEachTime() {
            SafeConstructor constructor1 = SafeConstructor.create();
            SafeConstructor constructor2 = SafeConstructor.create();
            assertThat(constructor1).isNotSameAs(constructor2);
        }

        @Test
        @DisplayName("should have empty allowed types set")
        void shouldHaveEmptyAllowedTypesSet() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThat(constructor.getAllowedTypes()).isEmpty();
        }

        @Test
        @DisplayName("should have empty allowed packages set")
        void shouldHaveEmptyAllowedPackagesSet() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThat(constructor.getAllowedPackages()).isEmpty();
        }
    }

    // ==================== builder() Factory Method Tests ====================

    @Nested
    @DisplayName("builder() factory method")
    class BuilderFactoryMethodTests {

        @Test
        @DisplayName("should return non-null Builder")
        void shouldReturnNonNullBuilder() {
            SafeConstructor.Builder builder = SafeConstructor.builder();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should return new Builder instance each time")
        void shouldReturnNewBuilderInstanceEachTime() {
            SafeConstructor.Builder builder1 = SafeConstructor.builder();
            SafeConstructor.Builder builder2 = SafeConstructor.builder();
            assertThat(builder1).isNotSameAs(builder2);
        }

        @Test
        @DisplayName("should build SafeConstructor with empty settings")
        void shouldBuildSafeConstructorWithEmptySettings() {
            SafeConstructor constructor = SafeConstructor.builder().build();
            assertThat(constructor).isNotNull();
            assertThat(constructor.getAllowedTypes()).isEmpty();
            assertThat(constructor.getAllowedPackages()).isEmpty();
        }
    }

    // ==================== isSafeType(Class<?>) Tests ====================

    @Nested
    @DisplayName("isSafeType(Class<?>) method")
    class IsSafeTypeClassTests {

        @Nested
        @DisplayName("null handling")
        class NullHandlingTests {

            @Test
            @DisplayName("should return false for null class")
            void shouldReturnFalseForNullClass() {
                SafeConstructor constructor = SafeConstructor.create();
                assertThat(constructor.isSafeType((Class<?>) null)).isFalse();
            }
        }

        @Nested
        @DisplayName("basic safe types")
        class BasicSafeTypesClassTests {

            private SafeConstructor constructor;

            @BeforeEach
            void setUp() {
                constructor = SafeConstructor.create();
            }

            @Test
            @DisplayName("should return true for String")
            void shouldReturnTrueForString() {
                assertThat(constructor.isSafeType(String.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for Integer")
            void shouldReturnTrueForInteger() {
                assertThat(constructor.isSafeType(Integer.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for int primitive")
            void shouldReturnTrueForIntPrimitive() {
                assertThat(constructor.isSafeType(int.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for Long")
            void shouldReturnTrueForLong() {
                assertThat(constructor.isSafeType(Long.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for Double")
            void shouldReturnTrueForDouble() {
                assertThat(constructor.isSafeType(Double.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for Boolean")
            void shouldReturnTrueForBoolean() {
                assertThat(constructor.isSafeType(Boolean.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for BigDecimal")
            void shouldReturnTrueForBigDecimal() {
                assertThat(constructor.isSafeType(BigDecimal.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for BigInteger")
            void shouldReturnTrueForBigInteger() {
                assertThat(constructor.isSafeType(BigInteger.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for LocalDate")
            void shouldReturnTrueForLocalDate() {
                assertThat(constructor.isSafeType(LocalDate.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for LocalDateTime")
            void shouldReturnTrueForLocalDateTime() {
                assertThat(constructor.isSafeType(LocalDateTime.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for Instant")
            void shouldReturnTrueForInstant() {
                assertThat(constructor.isSafeType(Instant.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for UUID")
            void shouldReturnTrueForUuid() {
                assertThat(constructor.isSafeType(UUID.class)).isTrue();
            }
        }

        @Nested
        @DisplayName("collection types")
        class CollectionTypesTests {

            private SafeConstructor constructor;

            @BeforeEach
            void setUp() {
                constructor = SafeConstructor.create();
            }

            @Test
            @DisplayName("should return true for List")
            void shouldReturnTrueForList() {
                assertThat(constructor.isSafeType(List.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for ArrayList")
            void shouldReturnTrueForArrayList() {
                assertThat(constructor.isSafeType(ArrayList.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for LinkedList")
            void shouldReturnTrueForLinkedList() {
                assertThat(constructor.isSafeType(LinkedList.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for Set")
            void shouldReturnTrueForSet() {
                assertThat(constructor.isSafeType(Set.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for HashSet")
            void shouldReturnTrueForHashSet() {
                assertThat(constructor.isSafeType(HashSet.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for TreeSet")
            void shouldReturnTrueForTreeSet() {
                assertThat(constructor.isSafeType(TreeSet.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for Map")
            void shouldReturnTrueForMap() {
                assertThat(constructor.isSafeType(Map.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for HashMap")
            void shouldReturnTrueForHashMap() {
                assertThat(constructor.isSafeType(HashMap.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for LinkedHashMap")
            void shouldReturnTrueForLinkedHashMap() {
                assertThat(constructor.isSafeType(LinkedHashMap.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for TreeMap")
            void shouldReturnTrueForTreeMap() {
                assertThat(constructor.isSafeType(TreeMap.class)).isTrue();
            }

            @Test
            @DisplayName("should return true for Collection interface")
            void shouldReturnTrueForCollectionInterface() {
                assertThat(constructor.isSafeType(Collection.class)).isTrue();
            }
        }

        @Nested
        @DisplayName("array types")
        class ArrayTypesTests {

            private SafeConstructor constructor;

            @BeforeEach
            void setUp() {
                constructor = SafeConstructor.create();
            }

            @Test
            @DisplayName("should return true for String array")
            void shouldReturnTrueForStringArray() {
                assertThat(constructor.isSafeType(String[].class)).isTrue();
            }

            @Test
            @DisplayName("should return true for Integer array")
            void shouldReturnTrueForIntegerArray() {
                assertThat(constructor.isSafeType(Integer[].class)).isTrue();
            }

            @Test
            @DisplayName("should return true for int array")
            void shouldReturnTrueForIntArray() {
                assertThat(constructor.isSafeType(int[].class)).isTrue();
            }

            @Test
            @DisplayName("should return true for double array")
            void shouldReturnTrueForDoubleArray() {
                assertThat(constructor.isSafeType(double[].class)).isTrue();
            }

            @Test
            @DisplayName("should return true for Long array")
            void shouldReturnTrueForLongArray() {
                assertThat(constructor.isSafeType(Long[].class)).isTrue();
            }

            @Test
            @DisplayName("should return false for Object array")
            void shouldReturnFalseForObjectArray() {
                assertThat(constructor.isSafeType(Object[].class)).isFalse();
            }
        }

        @Nested
        @DisplayName("unsafe types")
        class UnsafeTypesTests {

            private SafeConstructor constructor;

            @BeforeEach
            void setUp() {
                constructor = SafeConstructor.create();
            }

            @Test
            @DisplayName("should return false for Runtime")
            void shouldReturnFalseForRuntime() {
                assertThat(constructor.isSafeType(Runtime.class)).isFalse();
            }

            @Test
            @DisplayName("should return false for ProcessBuilder")
            void shouldReturnFalseForProcessBuilder() {
                assertThat(constructor.isSafeType(ProcessBuilder.class)).isFalse();
            }

            @Test
            @DisplayName("should return false for Object")
            void shouldReturnFalseForObject() {
                assertThat(constructor.isSafeType(Object.class)).isFalse();
            }

            @Test
            @DisplayName("should return false for Thread")
            void shouldReturnFalseForThread() {
                assertThat(constructor.isSafeType(Thread.class)).isFalse();
            }
        }
    }

    // ==================== isSafeType(String) Tests ====================

    @Nested
    @DisplayName("isSafeType(String) method")
    class IsSafeTypeStringTests {

        @Nested
        @DisplayName("null and empty handling")
        class NullAndEmptyHandlingTests {

            @Test
            @DisplayName("should return false for null type name")
            void shouldReturnFalseForNullTypeName() {
                SafeConstructor constructor = SafeConstructor.create();
                assertThat(constructor.isSafeType((String) null)).isFalse();
            }

            @Test
            @DisplayName("should return false for empty type name")
            void shouldReturnFalseForEmptyTypeName() {
                SafeConstructor constructor = SafeConstructor.create();
                assertThat(constructor.isSafeType("")).isFalse();
            }
        }

        @Nested
        @DisplayName("dangerous type names")
        class DangerousTypeNamesTests {

            private SafeConstructor constructor;

            @BeforeEach
            void setUp() {
                constructor = SafeConstructor.create();
            }

            @Test
            @DisplayName("should return false for type containing Runtime")
            void shouldReturnFalseForTypeContainingRuntime() {
                assertThat(constructor.isSafeType("java.lang.Runtime")).isFalse();
            }

            @Test
            @DisplayName("should return false for type containing ProcessBuilder")
            void shouldReturnFalseForTypeContainingProcessBuilder() {
                assertThat(constructor.isSafeType("java.lang.ProcessBuilder")).isFalse();
            }

            @Test
            @DisplayName("should return false for type containing ScriptEngine")
            void shouldReturnFalseForTypeContainingScriptEngine() {
                assertThat(constructor.isSafeType("javax.script.ScriptEngine")).isFalse();
            }

            @Test
            @DisplayName("should return false for javax.script prefix")
            void shouldReturnFalseForJavaxScriptPrefix() {
                assertThat(constructor.isSafeType("javax.script.SomeEngine")).isFalse();
            }

            @Test
            @DisplayName("should return false for com.sun prefix")
            void shouldReturnFalseForComSunPrefix() {
                assertThat(constructor.isSafeType("com.sun.rowset.JdbcRowSetImpl")).isFalse();
            }

            @Test
            @DisplayName("should return false for type containing Unsafe")
            void shouldReturnFalseForTypeContainingUnsafe() {
                assertThat(constructor.isSafeType("sun.misc.Unsafe")).isFalse();
            }

            @Test
            @DisplayName("should return false for custom Runtime wrapper")
            void shouldReturnFalseForCustomRuntimeWrapper() {
                assertThat(constructor.isSafeType("com.example.RuntimeWrapper")).isFalse();
            }

            @Test
            @DisplayName("should return false for custom ProcessBuilder wrapper")
            void shouldReturnFalseForCustomProcessBuilderWrapper() {
                assertThat(constructor.isSafeType("com.example.MyProcessBuilder")).isFalse();
            }
        }

        @Nested
        @DisplayName("safe type names")
        class SafeTypeNamesTests {

            private SafeConstructor constructor;

            @BeforeEach
            void setUp() {
                constructor = SafeConstructor.create();
            }

            @Test
            @DisplayName("should return true for java.lang.String")
            void shouldReturnTrueForJavaLangString() {
                assertThat(constructor.isSafeType("java.lang.String")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.lang.Integer")
            void shouldReturnTrueForJavaLangInteger() {
                assertThat(constructor.isSafeType("java.lang.Integer")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.util.ArrayList")
            void shouldReturnTrueForJavaUtilArrayList() {
                assertThat(constructor.isSafeType("java.util.ArrayList")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.util.HashMap")
            void shouldReturnTrueForJavaUtilHashMap() {
                assertThat(constructor.isSafeType("java.util.HashMap")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.time.LocalDate")
            void shouldReturnTrueForJavaTimeLocalDate() {
                assertThat(constructor.isSafeType("java.time.LocalDate")).isTrue();
            }

            @Test
            @DisplayName("should return true for java.util.UUID")
            void shouldReturnTrueForJavaUtilUuid() {
                assertThat(constructor.isSafeType("java.util.UUID")).isTrue();
            }
        }

        @Nested
        @DisplayName("non-existent class names")
        class NonExistentClassNamesTests {

            @Test
            @DisplayName("should return false for unknown class without allowed package")
            void shouldReturnFalseForUnknownClassWithoutAllowedPackage() {
                SafeConstructor constructor = SafeConstructor.create();
                assertThat(constructor.isSafeType("com.nonexistent.SomeClass")).isFalse();
            }

            @Test
            @DisplayName("should return true for unknown class in allowed package")
            void shouldReturnTrueForUnknownClassInAllowedPackage() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowPackage("com.allowed")
                    .build();
                assertThat(constructor.isSafeType("com.allowed.SomeUnknownClass")).isTrue();
            }
        }
    }

    // ==================== validateType(Class<?>) Tests ====================

    @Nested
    @DisplayName("validateType(Class<?>) method")
    class ValidateTypeClassTests {

        @Test
        @DisplayName("should not throw for safe type")
        void shouldNotThrowForSafeType() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThatNoException().isThrownBy(() -> constructor.validateType(String.class));
        }

        @Test
        @DisplayName("should not throw for collection type")
        void shouldNotThrowForCollectionType() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThatNoException().isThrownBy(() -> constructor.validateType(ArrayList.class));
        }

        @Test
        @DisplayName("should throw YmlSecurityException for unsafe type")
        void shouldThrowYmlSecurityExceptionForUnsafeType() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThatThrownBy(() -> constructor.validateType(Runtime.class))
                .isInstanceOf(YmlSecurityException.class);
        }

        @Test
        @DisplayName("should throw NullPointerException for null type")
        void shouldThrowNullPointerExceptionForNullType() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThatThrownBy(() -> constructor.validateType((Class<?>) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should include type name in exception message")
        void shouldIncludeTypeNameInExceptionMessage() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThatThrownBy(() -> constructor.validateType(ProcessBuilder.class))
                .isInstanceOf(YmlSecurityException.class)
                .hasMessageContaining("ProcessBuilder");
        }

        @Test
        @DisplayName("should not throw for custom allowed type")
        void shouldNotThrowForCustomAllowedType() {
            SafeConstructor constructor = SafeConstructor.builder()
                .allowType(CustomConfig.class)
                .build();
            assertThatNoException().isThrownBy(() -> constructor.validateType(CustomConfig.class));
        }
    }

    // ==================== validateType(String) Tests ====================

    @Nested
    @DisplayName("validateType(String) method")
    class ValidateTypeStringTests {

        @Test
        @DisplayName("should not throw for safe type name")
        void shouldNotThrowForSafeTypeName() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThatNoException().isThrownBy(() -> constructor.validateType("java.lang.String"));
        }

        @Test
        @DisplayName("should throw YmlSecurityException for dangerous type name")
        void shouldThrowYmlSecurityExceptionForDangerousTypeName() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThatThrownBy(() -> constructor.validateType("java.lang.Runtime"))
                .isInstanceOf(YmlSecurityException.class);
        }

        @Test
        @DisplayName("should throw YmlSecurityException for null type name")
        void shouldThrowYmlSecurityExceptionForNullTypeName() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThatThrownBy(() -> constructor.validateType((String) null))
                .isInstanceOf(YmlSecurityException.class);
        }

        @Test
        @DisplayName("should throw YmlSecurityException for empty type name")
        void shouldThrowYmlSecurityExceptionForEmptyTypeName() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThatThrownBy(() -> constructor.validateType(""))
                .isInstanceOf(YmlSecurityException.class);
        }

        @Test
        @DisplayName("should include type name in exception message")
        void shouldIncludeTypeNameInExceptionMessage() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThatThrownBy(() -> constructor.validateType("javax.script.ScriptEngine"))
                .isInstanceOf(YmlSecurityException.class)
                .hasMessageContaining("javax.script.ScriptEngine");
        }
    }

    // ==================== getAllowedTypes() Tests ====================

    @Nested
    @DisplayName("getAllowedTypes() method")
    class GetAllowedTypesTests {

        @Test
        @DisplayName("should return empty set for default constructor")
        void shouldReturnEmptySetForDefaultConstructor() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThat(constructor.getAllowedTypes()).isEmpty();
        }

        @Test
        @DisplayName("should return set with single allowed type")
        void shouldReturnSetWithSingleAllowedType() {
            SafeConstructor constructor = SafeConstructor.builder()
                .allowType(CustomConfig.class)
                .build();
            assertThat(constructor.getAllowedTypes()).containsExactly(CustomConfig.class);
        }

        @Test
        @DisplayName("should return set with multiple allowed types")
        void shouldReturnSetWithMultipleAllowedTypes() {
            SafeConstructor constructor = SafeConstructor.builder()
                .allowTypes(CustomConfig.class, AnotherConfig.class)
                .build();
            assertThat(constructor.getAllowedTypes()).containsExactlyInAnyOrder(
                CustomConfig.class, AnotherConfig.class);
        }

        @Test
        @DisplayName("should return immutable set")
        void shouldReturnImmutableSet() {
            SafeConstructor constructor = SafeConstructor.builder()
                .allowType(CustomConfig.class)
                .build();
            assertThatThrownBy(() -> constructor.getAllowedTypes().add(String.class))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ==================== getAllowedPackages() Tests ====================

    @Nested
    @DisplayName("getAllowedPackages() method")
    class GetAllowedPackagesTests {

        @Test
        @DisplayName("should return empty set for default constructor")
        void shouldReturnEmptySetForDefaultConstructor() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThat(constructor.getAllowedPackages()).isEmpty();
        }

        @Test
        @DisplayName("should return set with single allowed package")
        void shouldReturnSetWithSingleAllowedPackage() {
            SafeConstructor constructor = SafeConstructor.builder()
                .allowPackage("com.example.config")
                .build();
            assertThat(constructor.getAllowedPackages()).containsExactly("com.example.config");
        }

        @Test
        @DisplayName("should return set with multiple allowed packages")
        void shouldReturnSetWithMultipleAllowedPackages() {
            SafeConstructor constructor = SafeConstructor.builder()
                .allowPackages("com.example.config", "com.example.model")
                .build();
            assertThat(constructor.getAllowedPackages()).containsExactlyInAnyOrder(
                "com.example.config", "com.example.model");
        }

        @Test
        @DisplayName("should return immutable set")
        void shouldReturnImmutableSet() {
            SafeConstructor constructor = SafeConstructor.builder()
                .allowPackage("com.example")
                .build();
            assertThatThrownBy(() -> constructor.getAllowedPackages().add("com.other"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ==================== Builder Tests ====================

    @Nested
    @DisplayName("Builder class")
    class BuilderTests {

        @Nested
        @DisplayName("allowType method")
        class AllowTypeTests {

            @Test
            @DisplayName("should add single type to allowed types")
            void shouldAddSingleTypeToAllowedTypes() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowType(CustomConfig.class)
                    .build();
                assertThat(constructor.getAllowedTypes()).contains(CustomConfig.class);
            }

            @Test
            @DisplayName("should return builder for chaining")
            void shouldReturnBuilderForChaining() {
                SafeConstructor.Builder builder = SafeConstructor.builder();
                SafeConstructor.Builder result = builder.allowType(CustomConfig.class);
                assertThat(result).isSameAs(builder);
            }

            @Test
            @DisplayName("should allow chaining multiple allowType calls")
            void shouldAllowChainingMultipleAllowTypeCalls() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowType(CustomConfig.class)
                    .allowType(AnotherConfig.class)
                    .build();
                assertThat(constructor.getAllowedTypes())
                    .containsExactlyInAnyOrder(CustomConfig.class, AnotherConfig.class);
            }

            @Test
            @DisplayName("should make added type safe")
            void shouldMakeAddedTypeSafe() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowType(CustomConfig.class)
                    .build();
                assertThat(constructor.isSafeType(CustomConfig.class)).isTrue();
            }
        }

        @Nested
        @DisplayName("allowTypes method")
        class AllowTypesTests {

            @Test
            @DisplayName("should add multiple types to allowed types")
            void shouldAddMultipleTypesToAllowedTypes() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowTypes(CustomConfig.class, AnotherConfig.class)
                    .build();
                assertThat(constructor.getAllowedTypes())
                    .containsExactlyInAnyOrder(CustomConfig.class, AnotherConfig.class);
            }

            @Test
            @DisplayName("should return builder for chaining")
            void shouldReturnBuilderForChaining() {
                SafeConstructor.Builder builder = SafeConstructor.builder();
                SafeConstructor.Builder result = builder.allowTypes(CustomConfig.class);
                assertThat(result).isSameAs(builder);
            }

            @Test
            @DisplayName("should handle varargs with single element")
            void shouldHandleVarargsWithSingleElement() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowTypes(CustomConfig.class)
                    .build();
                assertThat(constructor.getAllowedTypes()).hasSize(1);
            }

            @Test
            @DisplayName("should combine with allowType calls")
            void shouldCombineWithAllowTypeCalls() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowType(CustomConfig.class)
                    .allowTypes(AnotherConfig.class, ThirdConfig.class)
                    .build();
                assertThat(constructor.getAllowedTypes()).hasSize(3);
            }
        }

        @Nested
        @DisplayName("allowPackage method")
        class AllowPackageTests {

            @Test
            @DisplayName("should add package to allowed packages")
            void shouldAddPackageToAllowedPackages() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowPackage("com.example.config")
                    .build();
                assertThat(constructor.getAllowedPackages()).contains("com.example.config");
            }

            @Test
            @DisplayName("should return builder for chaining")
            void shouldReturnBuilderForChaining() {
                SafeConstructor.Builder builder = SafeConstructor.builder();
                SafeConstructor.Builder result = builder.allowPackage("com.example");
                assertThat(result).isSameAs(builder);
            }

            @Test
            @DisplayName("should make types in package safe")
            void shouldMakeTypesInPackageSafe() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowPackage("cloud.opencode.base.yml.security")
                    .build();
                assertThat(constructor.isSafeType(SafeConstructorTest.CustomConfig.class)).isTrue();
            }

            @Test
            @DisplayName("should match subpackages")
            void shouldMatchSubpackages() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowPackage("cloud.opencode")
                    .build();
                assertThat(constructor.isSafeType(SafeConstructorTest.CustomConfig.class)).isTrue();
            }
        }

        @Nested
        @DisplayName("allowPackages method")
        class AllowPackagesTests {

            @Test
            @DisplayName("should add multiple packages to allowed packages")
            void shouldAddMultiplePackagesToAllowedPackages() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowPackages("com.example.config", "com.example.model")
                    .build();
                assertThat(constructor.getAllowedPackages())
                    .containsExactlyInAnyOrder("com.example.config", "com.example.model");
            }

            @Test
            @DisplayName("should return builder for chaining")
            void shouldReturnBuilderForChaining() {
                SafeConstructor.Builder builder = SafeConstructor.builder();
                SafeConstructor.Builder result = builder.allowPackages("com.example");
                assertThat(result).isSameAs(builder);
            }

            @Test
            @DisplayName("should combine with allowPackage calls")
            void shouldCombineWithAllowPackageCalls() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowPackage("com.example.config")
                    .allowPackages("com.example.model", "com.example.dto")
                    .build();
                assertThat(constructor.getAllowedPackages()).hasSize(3);
            }
        }

        @Nested
        @DisplayName("customValidator method")
        class CustomValidatorTests {

            @Test
            @DisplayName("should accept custom validator predicate")
            void shouldAcceptCustomValidatorPredicate() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .customValidator(clazz -> clazz.getSimpleName().endsWith("Config"))
                    .build();
                assertThat(constructor.isSafeType(CustomConfig.class)).isTrue();
            }

            @Test
            @DisplayName("should return builder for chaining")
            void shouldReturnBuilderForChaining() {
                SafeConstructor.Builder builder = SafeConstructor.builder();
                SafeConstructor.Builder result = builder.customValidator(clazz -> true);
                assertThat(result).isSameAs(builder);
            }

            @Test
            @DisplayName("should use custom validator for type checking")
            void shouldUseCustomValidatorForTypeChecking() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .customValidator(clazz -> clazz.isInterface())
                    .build();
                assertThat(constructor.isSafeType(Runnable.class)).isTrue();
                assertThat(constructor.isSafeType(Thread.class)).isFalse();
            }

            @Test
            @DisplayName("should combine with other allow methods")
            void shouldCombineWithOtherAllowMethods() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowType(CustomConfig.class)
                    .customValidator(clazz -> clazz.getSimpleName().startsWith("Another"))
                    .build();
                assertThat(constructor.isSafeType(CustomConfig.class)).isTrue();
                assertThat(constructor.isSafeType(AnotherConfig.class)).isTrue();
            }
        }

        @Nested
        @DisplayName("build method")
        class BuildMethodTests {

            @Test
            @DisplayName("should create SafeConstructor instance")
            void shouldCreateSafeConstructorInstance() {
                SafeConstructor constructor = SafeConstructor.builder().build();
                assertThat(constructor).isNotNull();
            }

            @Test
            @DisplayName("should create DefaultSafeConstructor instance")
            void shouldCreateDefaultSafeConstructorInstance() {
                SafeConstructor constructor = SafeConstructor.builder().build();
                assertThat(constructor).isInstanceOf(SafeConstructor.DefaultSafeConstructor.class);
            }

            @Test
            @DisplayName("should create new instance each time")
            void shouldCreateNewInstanceEachTime() {
                SafeConstructor.Builder builder = SafeConstructor.builder();
                SafeConstructor constructor1 = builder.build();
                SafeConstructor constructor2 = builder.build();
                assertThat(constructor1).isNotSameAs(constructor2);
            }

            @Test
            @DisplayName("should preserve all builder settings")
            void shouldPreserveAllBuilderSettings() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowType(CustomConfig.class)
                    .allowPackage("com.example")
                    .build();
                assertThat(constructor.getAllowedTypes()).contains(CustomConfig.class);
                assertThat(constructor.getAllowedPackages()).contains("com.example");
            }
        }

        @Nested
        @DisplayName("complete builder chain")
        class CompleteBuilderChainTests {

            @Test
            @DisplayName("should support full builder chain")
            void shouldSupportFullBuilderChain() {
                SafeConstructor constructor = SafeConstructor.builder()
                    .allowType(CustomConfig.class)
                    .allowTypes(AnotherConfig.class, ThirdConfig.class)
                    .allowPackage("com.example.config")
                    .allowPackages("com.example.model", "com.example.dto")
                    .customValidator(clazz -> clazz.isRecord())
                    .build();

                assertThat(constructor).isNotNull();
                assertThat(constructor.getAllowedTypes()).hasSize(3);
                assertThat(constructor.getAllowedPackages()).hasSize(3);
            }
        }
    }

    // ==================== Integration Tests ====================

    @Nested
    @DisplayName("Integration scenarios")
    class IntegrationTests {

        @Test
        @DisplayName("should allow basic safe types by default")
        void shouldAllowBasicSafeTypesByDefault() {
            SafeConstructor constructor = SafeConstructor.create();

            assertThat(constructor.isSafeType(String.class)).isTrue();
            assertThat(constructor.isSafeType(Integer.class)).isTrue();
            assertThat(constructor.isSafeType(LocalDateTime.class)).isTrue();
            assertThat(constructor.isSafeType(UUID.class)).isTrue();
        }

        @Test
        @DisplayName("should allow collections by default")
        void shouldAllowCollectionsByDefault() {
            SafeConstructor constructor = SafeConstructor.create();

            assertThat(constructor.isSafeType(List.class)).isTrue();
            assertThat(constructor.isSafeType(Map.class)).isTrue();
            assertThat(constructor.isSafeType(Set.class)).isTrue();
        }

        @Test
        @DisplayName("should block dangerous types by default")
        void shouldBlockDangerousTypesByDefault() {
            SafeConstructor constructor = SafeConstructor.create();

            assertThat(constructor.isSafeType("java.lang.Runtime")).isFalse();
            assertThat(constructor.isSafeType("java.lang.ProcessBuilder")).isFalse();
            assertThat(constructor.isSafeType("javax.script.ScriptEngine")).isFalse();
        }

        @Test
        @DisplayName("should work with custom types and packages")
        void shouldWorkWithCustomTypesAndPackages() {
            SafeConstructor constructor = SafeConstructor.builder()
                .allowType(CustomConfig.class)
                .allowPackage("com.myapp.model")
                .build();

            assertThat(constructor.isSafeType(CustomConfig.class)).isTrue();
            assertThat(constructor.isSafeType(String.class)).isTrue(); // Still allows basic types
        }

        @Test
        @DisplayName("should validate and throw appropriately")
        void shouldValidateAndThrowAppropriately() {
            SafeConstructor constructor = SafeConstructor.create();

            // Should pass validation
            assertThatNoException().isThrownBy(() -> constructor.validateType(String.class));
            assertThatNoException().isThrownBy(() -> constructor.validateType("java.util.ArrayList"));

            // Should fail validation
            assertThatThrownBy(() -> constructor.validateType(Runtime.class))
                .isInstanceOf(YmlSecurityException.class);
            assertThatThrownBy(() -> constructor.validateType("java.lang.ProcessBuilder"))
                .isInstanceOf(YmlSecurityException.class);
        }
    }

    // ==================== Edge Cases ====================

    @Nested
    @DisplayName("Edge cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle duplicate allowed types")
        void shouldHandleDuplicateAllowedTypes() {
            SafeConstructor constructor = SafeConstructor.builder()
                .allowType(CustomConfig.class)
                .allowType(CustomConfig.class)
                .build();
            assertThat(constructor.getAllowedTypes()).hasSize(1);
        }

        @Test
        @DisplayName("should handle duplicate allowed packages")
        void shouldHandleDuplicateAllowedPackages() {
            SafeConstructor constructor = SafeConstructor.builder()
                .allowPackage("com.example")
                .allowPackage("com.example")
                .build();
            assertThat(constructor.getAllowedPackages()).hasSize(1);
        }

        @Test
        @DisplayName("should handle array of custom allowed type")
        void shouldHandleArrayOfCustomAllowedType() {
            SafeConstructor constructor = SafeConstructor.builder()
                .allowType(CustomConfig.class)
                .build();
            assertThat(constructor.isSafeType(CustomConfig[].class)).isTrue();
        }

        @Test
        @DisplayName("should handle nested arrays of safe types")
        void shouldHandleNestedArraysOfSafeTypes() {
            SafeConstructor constructor = SafeConstructor.create();
            assertThat(constructor.isSafeType(String[][].class)).isTrue();
            assertThat(constructor.isSafeType(int[][].class)).isTrue();
        }

        @Test
        @DisplayName("should handle partial package match using startsWith")
        void shouldHandlePartialPackageMatchUsingStartsWith() {
            SafeConstructor constructor = SafeConstructor.builder()
                .allowPackage("com.example.")
                .build();
            // Should match subpackages when using trailing dot
            assertThat(constructor.isSafeType("com.example.sub.MyClass")).isTrue();
            // Should not match similar but different packages with trailing dot
            assertThat(constructor.isSafeType("com.examples.MyClass")).isFalse();
        }

        @Test
        @DisplayName("should handle custom validator returning false for all types")
        void shouldHandleCustomValidatorReturningFalseForAllTypes() {
            SafeConstructor constructor = SafeConstructor.builder()
                .customValidator(clazz -> false)
                .build();
            // Basic safe types should still work
            assertThat(constructor.isSafeType(String.class)).isTrue();
            // Custom types should not pass
            assertThat(constructor.isSafeType(CustomConfig.class)).isFalse();
        }

        @Test
        @DisplayName("should handle custom validator returning true for all types")
        void shouldHandleCustomValidatorReturningTrueForAllTypes() {
            SafeConstructor constructor = SafeConstructor.builder()
                .customValidator(clazz -> true)
                .build();
            // Even normally unsafe types pass due to custom validator
            assertThat(constructor.isSafeType(Thread.class)).isTrue();
        }

        @Test
        @DisplayName("dangerous patterns should take precedence over custom validator in string check")
        void dangerousPatternsShouldTakePrecedenceInStringCheck() {
            SafeConstructor constructor = SafeConstructor.builder()
                .customValidator(clazz -> true)
                .build();
            // String-based check still blocks dangerous patterns
            assertThat(constructor.isSafeType("java.lang.Runtime")).isFalse();
        }
    }

    // ==================== Helper Classes for Testing ====================

    static class CustomConfig {
        private String name;
        private int value;
    }

    static class AnotherConfig {
        private String setting;
    }

    static class ThirdConfig {
        private boolean enabled;
    }
}
