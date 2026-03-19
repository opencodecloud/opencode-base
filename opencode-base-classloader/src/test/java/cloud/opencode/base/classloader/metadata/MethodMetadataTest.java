package cloud.opencode.base.classloader.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for MethodMetadata
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("MethodMetadata Tests")
class MethodMetadataTest {

    private MethodMetadata createSimpleMethod(String name, String returnType) {
        return new MethodMetadata(
                name, returnType, List.of(), List.of(), List.of(),
                Modifier.PUBLIC, false, false, false, List.of(), List.of()
        );
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with all parameters")
        void shouldCreateWithAllParameters() {
            MethodMetadata metadata = new MethodMetadata(
                    "testMethod",
                    "java.lang.String",
                    List.of("java.lang.Integer", "java.lang.Boolean"),
                    List.of("count", "flag"),
                    List.of("java.io.IOException"),
                    Modifier.PUBLIC | Modifier.STATIC,
                    false,
                    false,
                    false,
                    List.of(),
                    List.of()
            );

            assertThat(metadata.methodName()).isEqualTo("testMethod");
            assertThat(metadata.returnType()).isEqualTo("java.lang.String");
            assertThat(metadata.parameterTypes()).containsExactly("java.lang.Integer", "java.lang.Boolean");
            assertThat(metadata.parameterNames()).containsExactly("count", "flag");
            assertThat(metadata.exceptionTypes()).containsExactly("java.io.IOException");
        }

        @Test
        @DisplayName("Should throw on null method name")
        void shouldThrowOnNullMethodName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new MethodMetadata(
                            null, "void", List.of(), List.of(), List.of(),
                            0, false, false, false, List.of(), List.of()
                    ));
        }

        @Test
        @DisplayName("Should throw on null return type")
        void shouldThrowOnNullReturnType() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new MethodMetadata(
                            "test", null, List.of(), List.of(), List.of(),
                            0, false, false, false, List.of(), List.of()
                    ));
        }

        @Test
        @DisplayName("Should handle null lists")
        void shouldHandleNullLists() {
            MethodMetadata metadata = new MethodMetadata(
                    "test", "void", null, null, null,
                    0, false, false, false, null, null
            );

            assertThat(metadata.parameterTypes()).isEmpty();
            assertThat(metadata.parameterNames()).isEmpty();
            assertThat(metadata.exceptionTypes()).isEmpty();
            assertThat(metadata.annotations()).isEmpty();
            assertThat(metadata.parameterAnnotations()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Modifier Tests")
    class ModifierTests {

        @Test
        @DisplayName("Should detect public modifier")
        void shouldDetectPublicModifier() {
            MethodMetadata metadata = new MethodMetadata(
                    "test", "void", List.of(), List.of(), List.of(),
                    Modifier.PUBLIC, false, false, false, List.of(), List.of()
            );

            assertThat(metadata.isPublic()).isTrue();
            assertThat(metadata.isPrivate()).isFalse();
        }

        @Test
        @DisplayName("Should detect private modifier")
        void shouldDetectPrivateModifier() {
            MethodMetadata metadata = new MethodMetadata(
                    "test", "void", List.of(), List.of(), List.of(),
                    Modifier.PRIVATE, false, false, false, List.of(), List.of()
            );

            assertThat(metadata.isPrivate()).isTrue();
            assertThat(metadata.isPublic()).isFalse();
        }

        @Test
        @DisplayName("Should detect static modifier")
        void shouldDetectStaticModifier() {
            MethodMetadata metadata = new MethodMetadata(
                    "test", "void", List.of(), List.of(), List.of(),
                    Modifier.STATIC, false, false, false, List.of(), List.of()
            );

            assertThat(metadata.isStatic()).isTrue();
        }

        @Test
        @DisplayName("Should detect final modifier")
        void shouldDetectFinalModifier() {
            MethodMetadata metadata = new MethodMetadata(
                    "test", "void", List.of(), List.of(), List.of(),
                    Modifier.FINAL, false, false, false, List.of(), List.of()
            );

            assertThat(metadata.isFinal()).isTrue();
        }

        @Test
        @DisplayName("Should detect abstract modifier")
        void shouldDetectAbstractModifier() {
            MethodMetadata metadata = new MethodMetadata(
                    "test", "void", List.of(), List.of(), List.of(),
                    Modifier.ABSTRACT, false, false, false, List.of(), List.of()
            );

            assertThat(metadata.isAbstract()).isTrue();
        }

        @Test
        @DisplayName("Should detect protected modifier")
        void shouldDetectProtectedModifier() {
            MethodMetadata metadata = new MethodMetadata(
                    "test", "void", List.of(), List.of(), List.of(),
                    Modifier.PROTECTED, false, false, false, List.of(), List.of()
            );

            assertThat(metadata.isProtected()).isTrue();
        }
    }

    @Nested
    @DisplayName("Getter Detection Tests")
    class GetterDetectionTests {

        @Test
        @DisplayName("Should detect get-style getter")
        void shouldDetectGetStyleGetter() {
            MethodMetadata metadata = createSimpleMethod("getValue", "java.lang.String");

            assertThat(metadata.isGetter()).isTrue();
        }

        @Test
        @DisplayName("Should detect is-style getter")
        void shouldDetectIsStyleGetter() {
            MethodMetadata metadata = createSimpleMethod("isEnabled", "boolean");

            assertThat(metadata.isGetter()).isTrue();
        }

        @Test
        @DisplayName("Should not detect getter with parameters")
        void shouldNotDetectGetterWithParameters() {
            MethodMetadata metadata = new MethodMetadata(
                    "getValue", "java.lang.String", List.of("int"), List.of("index"), List.of(),
                    Modifier.PUBLIC, false, false, false, List.of(), List.of()
            );

            assertThat(metadata.isGetter()).isFalse();
        }

        @Test
        @DisplayName("Should not detect void getter")
        void shouldNotDetectVoidGetter() {
            MethodMetadata metadata = createSimpleMethod("getValue", "void");

            assertThat(metadata.isGetter()).isFalse();
        }
    }

    @Nested
    @DisplayName("Setter Detection Tests")
    class SetterDetectionTests {

        @Test
        @DisplayName("Should detect setter")
        void shouldDetectSetter() {
            MethodMetadata metadata = new MethodMetadata(
                    "setValue", "void", List.of("java.lang.String"), List.of("value"), List.of(),
                    Modifier.PUBLIC, false, false, false, List.of(), List.of()
            );

            assertThat(metadata.isSetter()).isTrue();
        }

        @Test
        @DisplayName("Should not detect setter with no parameters")
        void shouldNotDetectSetterWithNoParameters() {
            MethodMetadata metadata = createSimpleMethod("setValue", "void");

            assertThat(metadata.isSetter()).isFalse();
        }

        @Test
        @DisplayName("Should not detect setter with return type")
        void shouldNotDetectSetterWithReturnType() {
            MethodMetadata metadata = new MethodMetadata(
                    "setValue", "java.lang.String", List.of("java.lang.String"), List.of("value"), List.of(),
                    Modifier.PUBLIC, false, false, false, List.of(), List.of()
            );

            assertThat(metadata.isSetter()).isFalse();
        }
    }

    @Nested
    @DisplayName("Constructor Detection Tests")
    class ConstructorDetectionTests {

        @Test
        @DisplayName("Should detect constructor")
        void shouldDetectConstructor() {
            MethodMetadata metadata = createSimpleMethod("<init>", "void");

            assertThat(metadata.isConstructor()).isTrue();
        }

        @Test
        @DisplayName("Should not detect regular method as constructor")
        void shouldNotDetectRegularMethodAsConstructor() {
            MethodMetadata metadata = createSimpleMethod("init", "void");

            assertThat(metadata.isConstructor()).isFalse();
        }
    }

    @Nested
    @DisplayName("Static Initializer Detection Tests")
    class StaticInitializerDetectionTests {

        @Test
        @DisplayName("Should detect static initializer")
        void shouldDetectStaticInitializer() {
            MethodMetadata metadata = createSimpleMethod("<clinit>", "void");

            assertThat(metadata.isStaticInitializer()).isTrue();
        }
    }

    @Nested
    @DisplayName("Signature Tests")
    class SignatureTests {

        @Test
        @DisplayName("Should get method signature")
        void shouldGetMethodSignature() {
            MethodMetadata metadata = new MethodMetadata(
                    "calculate", "int", List.of("int", "int"), List.of("a", "b"), List.of(),
                    Modifier.PUBLIC, false, false, false, List.of(), List.of()
            );

            assertThat(metadata.getSignature()).isEqualTo("calculate(int, int)");
        }

        @Test
        @DisplayName("Should get empty signature for no parameters")
        void shouldGetEmptySignatureForNoParameters() {
            MethodMetadata metadata = createSimpleMethod("test", "void");

            assertThat(metadata.getSignature()).isEqualTo("test()");
        }
    }

    @Nested
    @DisplayName("Parameter Tests")
    class ParameterTests {

        @Test
        @DisplayName("Should get parameter count")
        void shouldGetParameterCount() {
            MethodMetadata metadata = new MethodMetadata(
                    "test", "void", List.of("int", "String", "boolean"), List.of(), List.of(),
                    Modifier.PUBLIC, false, false, false, List.of(), List.of()
            );

            assertThat(metadata.parameterCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should check no parameters")
        void shouldCheckNoParameters() {
            MethodMetadata withParams = new MethodMetadata(
                    "test", "void", List.of("int"), List.of(), List.of(),
                    Modifier.PUBLIC, false, false, false, List.of(), List.of()
            );
            MethodMetadata withoutParams = createSimpleMethod("test", "void");

            assertThat(withParams.hasNoParameters()).isFalse();
            assertThat(withoutParams.hasNoParameters()).isTrue();
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
            MethodMetadata metadata = new MethodMetadata(
                    "test", "void", List.of(), List.of(), List.of(),
                    Modifier.PUBLIC, false, false, false, List.of(annotation), List.of()
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
            MethodMetadata metadata = new MethodMetadata(
                    "test", "void", List.of(), List.of(), List.of(),
                    Modifier.PUBLIC, false, false, false, List.of(annotation), List.of()
            );

            assertThat(metadata.getAnnotation("java.lang.Deprecated")).isPresent();
            assertThat(metadata.getAnnotation("java.lang.Override")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Special Method Flags Tests")
    class SpecialMethodFlagsTests {

        @Test
        @DisplayName("Should detect synthetic method")
        void shouldDetectSyntheticMethod() {
            MethodMetadata metadata = new MethodMetadata(
                    "test", "void", List.of(), List.of(), List.of(),
                    Modifier.PUBLIC, true, false, false, List.of(), List.of()
            );

            assertThat(metadata.isSynthetic()).isTrue();
        }

        @Test
        @DisplayName("Should detect bridge method")
        void shouldDetectBridgeMethod() {
            MethodMetadata metadata = new MethodMetadata(
                    "test", "void", List.of(), List.of(), List.of(),
                    Modifier.PUBLIC, false, true, false, List.of(), List.of()
            );

            assertThat(metadata.isBridge()).isTrue();
        }

        @Test
        @DisplayName("Should detect default method")
        void shouldDetectDefaultMethod() {
            MethodMetadata metadata = new MethodMetadata(
                    "test", "void", List.of(), List.of(), List.of(),
                    Modifier.PUBLIC, false, false, true, List.of(), List.of()
            );

            assertThat(metadata.isDefault()).isTrue();
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal for same name and parameters")
        void shouldBeEqualForSameNameAndParameters() {
            MethodMetadata m1 = new MethodMetadata(
                    "test", "void", List.of("int"), List.of(), List.of(),
                    Modifier.PUBLIC, false, false, false, List.of(), List.of()
            );
            MethodMetadata m2 = new MethodMetadata(
                    "test", "String", List.of("int"), List.of("x"), List.of(),
                    Modifier.PRIVATE, false, false, false, List.of(), List.of()
            );

            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different names")
        void shouldNotBeEqualForDifferentNames() {
            MethodMetadata m1 = createSimpleMethod("test1", "void");
            MethodMetadata m2 = createSimpleMethod("test2", "void");

            assertThat(m1).isNotEqualTo(m2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should format toString")
        void shouldFormatToString() {
            MethodMetadata metadata = new MethodMetadata(
                    "calculate", "int", List.of("int", "int"), List.of("a", "b"), List.of(),
                    Modifier.PUBLIC | Modifier.STATIC, false, false, false, List.of(), List.of()
            );

            String str = metadata.toString();
            assertThat(str).contains("public");
            assertThat(str).contains("static");
            assertThat(str).contains("int");
            assertThat(str).contains("calculate");
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should return immutable parameter types")
        void shouldReturnImmutableParameterTypes() {
            MethodMetadata metadata = new MethodMetadata(
                    "test", "void", List.of("String"), List.of(), List.of(),
                    Modifier.PUBLIC, false, false, false, List.of(), List.of()
            );

            assertThatThrownBy(() -> metadata.parameterTypes().add("int"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should return immutable annotations")
        void shouldReturnImmutableAnnotations() {
            MethodMetadata metadata = createSimpleMethod("test", "void");

            assertThatThrownBy(() -> metadata.annotations().add(
                    new AnnotationMetadata("Test", Map.of(), true)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
