package cloud.opencode.base.tree.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeExceptionTest Tests
 * TreeExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreeException Tests")
class TreeExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message should set message and default code")
        void constructorWithMessageShouldSetMessageAndDefaultCode() {
            TreeException ex = new TreeException("Test error");

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getCode()).isEqualTo(TreeErrorCode.OPERATION_FAILED.getCode());
        }

        @Test
        @DisplayName("constructor with code and message should set both")
        void constructorWithCodeAndMessageShouldSetBoth() {
            TreeException ex = new TreeException("CUSTOM_CODE", "Custom message");

            assertThat(ex.getMessage()).isEqualTo("Custom message");
            assertThat(ex.getCode()).isEqualTo("CUSTOM_CODE");
        }

        @Test
        @DisplayName("constructor with errorCode should use code and message")
        void constructorWithErrorCodeShouldUseCodeAndMessage() {
            TreeException ex = new TreeException(TreeErrorCode.CYCLE_DETECTED);

            assertThat(ex.getCode()).isEqualTo(TreeErrorCode.CYCLE_DETECTED.getCode());
            assertThat(ex.getMessage()).isEqualTo(TreeErrorCode.CYCLE_DETECTED.getMessage());
        }

        @Test
        @DisplayName("constructor with errorCode and message should use custom message")
        void constructorWithErrorCodeAndMessageShouldUseCustomMessage() {
            TreeException ex = new TreeException(TreeErrorCode.BUILD_FAILED, "Custom build error");

            assertThat(ex.getCode()).isEqualTo(TreeErrorCode.BUILD_FAILED.getCode());
            assertThat(ex.getMessage()).isEqualTo("Custom build error");
        }

        @Test
        @DisplayName("constructor with message and cause should set both")
        void constructorWithMessageAndCauseShouldSetBoth() {
            Throwable cause = new RuntimeException("Root cause");
            TreeException ex = new TreeException("Error occurred", cause);

            assertThat(ex.getMessage()).isEqualTo("Error occurred");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("constructor with errorCode and cause should set both")
        void constructorWithErrorCodeAndCauseShouldSetBoth() {
            Throwable cause = new RuntimeException("Root cause");
            TreeException ex = new TreeException(TreeErrorCode.INVALID_NODE, cause);

            assertThat(ex.getCode()).isEqualTo(TreeErrorCode.INVALID_NODE.getCode());
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("buildFailed should create exception with BUILD_FAILED code")
        void buildFailedShouldCreateExceptionWithBuildFailedCode() {
            TreeException ex = TreeException.buildFailed("Build error message");

            assertThat(ex.getCode()).isEqualTo(TreeErrorCode.BUILD_FAILED.getCode());
            assertThat(ex.getMessage()).isEqualTo("Build error message");
        }

        @Test
        @DisplayName("invalidNode should create exception with INVALID_NODE code")
        void invalidNodeShouldCreateExceptionWithInvalidNodeCode() {
            TreeException ex = TreeException.invalidNode("Invalid node message");

            assertThat(ex.getCode()).isEqualTo(TreeErrorCode.INVALID_NODE.getCode());
        }

        @Test
        @DisplayName("duplicateId should create exception with DUPLICATE_ID code")
        void duplicateIdShouldCreateExceptionWithDuplicateIdCode() {
            TreeException ex = TreeException.duplicateId(123L);

            assertThat(ex.getCode()).isEqualTo(TreeErrorCode.DUPLICATE_ID.getCode());
            assertThat(ex.getMessage()).contains("123");
        }

        @Test
        @DisplayName("parentNotFound should create exception with PARENT_NOT_FOUND code")
        void parentNotFoundShouldCreateExceptionWithParentNotFoundCode() {
            TreeException ex = TreeException.parentNotFound(456L);

            assertThat(ex.getCode()).isEqualTo(TreeErrorCode.PARENT_NOT_FOUND.getCode());
            assertThat(ex.getMessage()).contains("456");
        }

        @Test
        @DisplayName("nodeNotFound should create exception with NODE_NOT_FOUND code")
        void nodeNotFoundShouldCreateExceptionWithNodeNotFoundCode() {
            TreeException ex = TreeException.nodeNotFound(789L);

            assertThat(ex.getCode()).isEqualTo(TreeErrorCode.NODE_NOT_FOUND.getCode());
            assertThat(ex.getMessage()).contains("789");
        }

        @Test
        @DisplayName("maxDepthExceeded should create exception with MAX_DEPTH_EXCEEDED code")
        void maxDepthExceededShouldCreateExceptionWithMaxDepthExceededCode() {
            TreeException ex = TreeException.maxDepthExceeded(100);

            assertThat(ex.getCode()).isEqualTo(TreeErrorCode.MAX_DEPTH_EXCEEDED.getCode());
            assertThat(ex.getMessage()).contains("100");
        }
    }
}
