package cloud.opencode.base.core.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Experimental}.
 */
@DisplayName("Experimental Annotation Tests")
class ExperimentalTest {

    @Experimental(since = "1.0.0", reason = "Testing annotation")
    static class AnnotatedClass {}

    @Experimental
    static class DefaultAnnotatedClass {}

    @Experimental(since = "2.0.0", reason = "Custom reason")
    static void annotatedMethod() {}

    @Experimental
    static String annotatedField = "test";

    @Nested
    @DisplayName("Annotation presence")
    class PresenceTests {

        @Test
        @DisplayName("is present on annotated class")
        void presentOnClass() {
            assertThat(AnnotatedClass.class.isAnnotationPresent(Experimental.class)).isTrue();
        }

        @Test
        @DisplayName("is not present on non-annotated class")
        void notPresentOnNonAnnotated() {
            assertThat(String.class.isAnnotationPresent(Experimental.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("since() attribute")
    class SinceTests {

        @Test
        @DisplayName("returns configured value")
        void returnsConfiguredValue() {
            Experimental annotation = AnnotatedClass.class.getAnnotation(Experimental.class);
            assertThat(annotation.since()).isEqualTo("1.0.0");
        }

        @Test
        @DisplayName("defaults to empty string")
        void defaultsToEmpty() {
            Experimental annotation = DefaultAnnotatedClass.class.getAnnotation(Experimental.class);
            assertThat(annotation.since()).isEmpty();
        }
    }

    @Nested
    @DisplayName("reason() attribute")
    class ReasonTests {

        @Test
        @DisplayName("returns configured reason")
        void returnsConfiguredReason() {
            Experimental annotation = AnnotatedClass.class.getAnnotation(Experimental.class);
            assertThat(annotation.reason()).isEqualTo("Testing annotation");
        }

        @Test
        @DisplayName("defaults to standard message")
        void defaultsToStandardMessage() {
            Experimental annotation = DefaultAnnotatedClass.class.getAnnotation(Experimental.class);
            assertThat(annotation.reason()).isEqualTo("API shape may change based on community feedback");
        }
    }

    @Nested
    @DisplayName("Meta-annotations")
    class MetaAnnotationTests {

        @Test
        @DisplayName("has RUNTIME retention")
        void hasRuntimeRetention() {
            Retention retention = Experimental.class.getAnnotation(Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("targets TYPE, METHOD, CONSTRUCTOR, FIELD")
        void hasCorrectTargets() {
            Target target = Experimental.class.getAnnotation(Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactlyInAnyOrder(
                    ElementType.TYPE,
                    ElementType.METHOD,
                    ElementType.CONSTRUCTOR,
                    ElementType.FIELD
            );
        }

        @Test
        @DisplayName("is documented")
        void isDocumented() {
            assertThat(Experimental.class.isAnnotationPresent(Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Target element types")
    class TargetTests {

        @Test
        @DisplayName("can annotate methods")
        void canAnnotateMethod() throws NoSuchMethodException {
            var method = ExperimentalTest.class.getDeclaredMethod("annotatedMethod");
            Experimental annotation = method.getAnnotation(Experimental.class);
            assertThat(annotation).isNotNull();
            assertThat(annotation.since()).isEqualTo("2.0.0");
        }

        @Test
        @DisplayName("can annotate fields")
        void canAnnotateField() throws NoSuchFieldException {
            var field = ExperimentalTest.class.getDeclaredField("annotatedField");
            Experimental annotation = field.getAnnotation(Experimental.class);
            assertThat(annotation).isNotNull();
        }
    }
}
