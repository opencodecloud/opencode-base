package cloud.opencode.base.tree.exception;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CycleDetectedExceptionTest Tests
 * CycleDetectedExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("CycleDetectedException Tests")
class CycleDetectedExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message should set message and empty cycle path")
        void constructorWithMessageShouldSetMessageAndEmptyCyclePath() {
            CycleDetectedException ex = new CycleDetectedException("Cycle found");

            assertThat(ex.getRawMessage()).isEqualTo("Cycle found");
            assertThat(ex.getCyclePath()).isEmpty();
            assertThat(ex.getCode()).isEqualTo(TreeErrorCode.CYCLE_DETECTED.getCode());
        }

        @Test
        @DisplayName("constructor with cycle path should set path and message")
        void constructorWithCyclePathShouldSetPathAndMessage() {
            List<Long> path = List.of(1L, 2L, 3L, 1L);

            CycleDetectedException ex = new CycleDetectedException(path);

            assertThat(ex.getCyclePath()).hasSize(4);
            assertThat(ex.getCyclePath().get(0)).isEqualTo(1L);
            assertThat(ex.getCyclePath().get(3)).isEqualTo(1L);
            assertThat(ex.getRawMessage()).contains("Cycle detected");
        }

        @Test
        @DisplayName("constructor with message and path should set both")
        void constructorWithMessageAndPathShouldSetBoth() {
            List<Long> path = List.of(1L, 2L);

            CycleDetectedException ex = new CycleDetectedException("Custom cycle message", path);

            assertThat(ex.getRawMessage()).isEqualTo("Custom cycle message");
            assertThat(ex.getCyclePath()).hasSize(2);
            assertThat(ex.getCyclePath().get(0)).isEqualTo(1L);
        }

        @Test
        @DisplayName("constructor with null path should set empty path")
        void constructorWithNullPathShouldSetEmptyPath() {
            CycleDetectedException ex = new CycleDetectedException("Message", null);

            assertThat(ex.getCyclePath()).isEmpty();
        }

        @Test
        @DisplayName("should be instance of TreeException and OpenException")
        void shouldBeInstanceOfTreeExceptionAndOpenException() {
            CycleDetectedException ex = new CycleDetectedException("test");

            assertThat(ex).isInstanceOf(TreeException.class);
            assertThat(ex).isInstanceOf(cloud.opencode.base.core.exception.OpenException.class);
        }
    }

    @Nested
    @DisplayName("GetCyclePath Tests")
    class GetCyclePathTests {

        @Test
        @DisplayName("getCyclePath should return immutable copy")
        void getCyclePathShouldReturnImmutableCopy() {
            List<Long> path = List.of(1L, 2L, 3L);
            CycleDetectedException ex = new CycleDetectedException(path);

            List<?> cyclePath = ex.getCyclePath();

            assertThatThrownBy(() -> ((List<Long>) cyclePath).add(4L))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
