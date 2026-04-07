package cloud.opencode.base.classloader.leak;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for LeakReport record
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V2.0.0
 */
@DisplayName("LeakReport Tests")
class LeakReportTest {

    private static final StackTraceElement SAMPLE_FRAME =
            new StackTraceElement("com.example.Foo", "bar", "Foo.java", 42);

    @Nested
    @DisplayName("Constructor Validation Tests")
    class ConstructorValidationTests {

        @Test
        @DisplayName("Should create valid report with SIMPLE level")
        void shouldCreateValidSimpleReport() {
            LeakReport report = new LeakReport("test-cl", LeakDetection.SIMPLE,
                    new StackTraceElement[0], 5, 1000L);

            assertThat(report.name()).isEqualTo("test-cl");
            assertThat(report.level()).isEqualTo(LeakDetection.SIMPLE);
            assertThat(report.creationStack()).isEmpty();
            assertThat(report.loadedClassCount()).isEqualTo(5);
            assertThat(report.createdAtNanos()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("Should create valid report with PARANOID level and stack trace")
        void shouldCreateValidParanoidReport() {
            StackTraceElement[] stack = {SAMPLE_FRAME};
            LeakReport report = new LeakReport("paranoid-cl", LeakDetection.PARANOID,
                    stack, 10, 2000L);

            assertThat(report.name()).isEqualTo("paranoid-cl");
            assertThat(report.level()).isEqualTo(LeakDetection.PARANOID);
            assertThat(report.creationStack()).containsExactly(SAMPLE_FRAME);
            assertThat(report.loadedClassCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should reject null name")
        void shouldRejectNullName() {
            assertThatThrownBy(() -> new LeakReport(null, LeakDetection.SIMPLE,
                    new StackTraceElement[0], 0, 0L))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("Should reject null level")
        void shouldRejectNullLevel() {
            assertThatThrownBy(() -> new LeakReport("test", null,
                    new StackTraceElement[0], 0, 0L))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("level");
        }

        @Test
        @DisplayName("Should reject null creationStack")
        void shouldRejectNullCreationStack() {
            assertThatThrownBy(() -> new LeakReport("test", LeakDetection.SIMPLE,
                    null, 0, 0L))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("creationStack");
        }

        @Test
        @DisplayName("Should reject negative loadedClassCount")
        void shouldRejectNegativeClassCount() {
            assertThatThrownBy(() -> new LeakReport("test", LeakDetection.SIMPLE,
                    new StackTraceElement[0], -1, 0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("loadedClassCount");
        }

        @Test
        @DisplayName("Should allow zero loadedClassCount")
        void shouldAllowZeroClassCount() {
            LeakReport report = new LeakReport("test", LeakDetection.SIMPLE,
                    new StackTraceElement[0], 0, 0L);
            assertThat(report.loadedClassCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Defensive Copy Tests")
    class DefensiveCopyTests {

        @Test
        @DisplayName("Should defensively copy creationStack on construction")
        void shouldDefensivelyCopyOnConstruction() {
            StackTraceElement[] original = {SAMPLE_FRAME};
            LeakReport report = new LeakReport("test", LeakDetection.PARANOID,
                    original, 1, 100L);

            // Mutate original — should not affect report
            original[0] = null;
            assertThat(report.creationStack()).containsExactly(SAMPLE_FRAME);
        }

        @Test
        @DisplayName("Should return defensive copy from accessor")
        void shouldReturnDefensiveCopyFromAccessor() {
            LeakReport report = new LeakReport("test", LeakDetection.PARANOID,
                    new StackTraceElement[]{SAMPLE_FRAME}, 1, 100L);

            StackTraceElement[] first = report.creationStack();
            first[0] = null;

            // Second call should still return original
            assertThat(report.creationStack()).containsExactly(SAMPLE_FRAME);
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should include name and level in toString for SIMPLE")
        void shouldIncludeBasicFieldsForSimple() {
            LeakReport report = new LeakReport("my-loader", LeakDetection.SIMPLE,
                    new StackTraceElement[0], 3, 500L);

            String str = report.toString();
            assertThat(str).contains("my-loader");
            assertThat(str).contains("SIMPLE");
            assertThat(str).contains("3");
            assertThat(str).contains("500");
            // No creationStack section for empty array
            assertThat(str).doesNotContain("creationStack");
        }

        @Test
        @DisplayName("Should include stack trace in toString for PARANOID")
        void shouldIncludeStackTraceForParanoid() {
            LeakReport report = new LeakReport("my-loader", LeakDetection.PARANOID,
                    new StackTraceElement[]{SAMPLE_FRAME}, 3, 500L);

            String str = report.toString();
            assertThat(str).contains("creationStack");
            assertThat(str).contains("Foo.java");
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Equal reports should be equal")
        void equalReportsShouldBeEqual() {
            LeakReport a = new LeakReport("test", LeakDetection.SIMPLE,
                    new StackTraceElement[0], 5, 100L);
            LeakReport b = new LeakReport("test", LeakDetection.SIMPLE,
                    new StackTraceElement[0], 5, 100L);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("Different names should not be equal")
        void differentNamesShouldNotBeEqual() {
            LeakReport a = new LeakReport("a", LeakDetection.SIMPLE,
                    new StackTraceElement[0], 5, 100L);
            LeakReport b = new LeakReport("b", LeakDetection.SIMPLE,
                    new StackTraceElement[0], 5, 100L);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("Different levels should not be equal")
        void differentLevelsShouldNotBeEqual() {
            LeakReport a = new LeakReport("test", LeakDetection.SIMPLE,
                    new StackTraceElement[0], 5, 100L);
            LeakReport b = new LeakReport("test", LeakDetection.PARANOID,
                    new StackTraceElement[0], 5, 100L);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("Different stack traces should not be equal")
        void differentStackTracesShouldNotBeEqual() {
            LeakReport a = new LeakReport("test", LeakDetection.PARANOID,
                    new StackTraceElement[0], 5, 100L);
            LeakReport b = new LeakReport("test", LeakDetection.PARANOID,
                    new StackTraceElement[]{SAMPLE_FRAME}, 5, 100L);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("Should not equal null")
        void shouldNotEqualNull() {
            LeakReport report = new LeakReport("test", LeakDetection.SIMPLE,
                    new StackTraceElement[0], 0, 0L);
            assertThat(report).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not equal different type")
        void shouldNotEqualDifferentType() {
            LeakReport report = new LeakReport("test", LeakDetection.SIMPLE,
                    new StackTraceElement[0], 0, 0L);
            assertThat(report).isNotEqualTo("not a report");
        }

        @Test
        @DisplayName("Same instance should be equal")
        void sameInstanceShouldBeEqual() {
            LeakReport report = new LeakReport("test", LeakDetection.SIMPLE,
                    new StackTraceElement[0], 0, 0L);
            assertThat(report).isEqualTo(report);
        }
    }
}
