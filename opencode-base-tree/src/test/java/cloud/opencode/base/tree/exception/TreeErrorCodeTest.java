package cloud.opencode.base.tree.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeErrorCodeTest Tests
 * TreeErrorCodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreeErrorCode Tests")
class TreeErrorCodeTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("should have all expected error codes")
        void shouldHaveAllExpectedErrorCodes() {
            TreeErrorCode[] codes = TreeErrorCode.values();

            assertThat(codes).contains(
                TreeErrorCode.BUILD_FAILED,
                TreeErrorCode.INVALID_NODE,
                TreeErrorCode.DUPLICATE_ID,
                TreeErrorCode.PARENT_NOT_FOUND,
                TreeErrorCode.TRAVERSAL_ERROR,
                TreeErrorCode.MAX_DEPTH_EXCEEDED,
                TreeErrorCode.STACK_OVERFLOW,
                TreeErrorCode.OPERATION_FAILED,
                TreeErrorCode.NODE_NOT_FOUND,
                TreeErrorCode.INVALID_OPERATION,
                TreeErrorCode.CYCLE_DETECTED,
                TreeErrorCode.VALIDATION_FAILED,
                TreeErrorCode.NULL_ID
            );
        }

        @Test
        @DisplayName("valueOf should return correct enum")
        void valueOfShouldReturnCorrectEnum() {
            assertThat(TreeErrorCode.valueOf("BUILD_FAILED")).isEqualTo(TreeErrorCode.BUILD_FAILED);
            assertThat(TreeErrorCode.valueOf("CYCLE_DETECTED")).isEqualTo(TreeErrorCode.CYCLE_DETECTED);
        }
    }

    @Nested
    @DisplayName("GetCode Tests")
    class GetCodeTests {

        @Test
        @DisplayName("getCode should return non-empty code")
        void getCodeShouldReturnNonEmptyCode() {
            for (TreeErrorCode code : TreeErrorCode.values()) {
                assertThat(code.getCode())
                    .as("Code for %s should not be blank", code)
                    .isNotBlank();
            }
        }

        @Test
        @DisplayName("getCode should follow TREE-Xxxx pattern")
        void getCodeShouldFollowPattern() {
            for (TreeErrorCode code : TreeErrorCode.values()) {
                assertThat(code.getCode())
                    .as("Code for %s should start with TREE-", code)
                    .startsWith("TREE-");
            }
        }
    }

    @Nested
    @DisplayName("GetMessage Tests")
    class GetMessageTests {

        @Test
        @DisplayName("getMessage should return non-empty message")
        void getMessageShouldReturnNonEmptyMessage() {
            for (TreeErrorCode code : TreeErrorCode.values()) {
                assertThat(code.getMessage())
                    .as("Message for %s should not be blank", code)
                    .isNotBlank();
            }
        }

        @Test
        @DisplayName("getMessage should return meaningful messages")
        void getMessageShouldReturnMeaningfulMessages() {
            assertThat(TreeErrorCode.BUILD_FAILED.getMessage()).containsIgnoringCase("build");
            assertThat(TreeErrorCode.CYCLE_DETECTED.getMessage()).containsIgnoringCase("cycle");
            assertThat(TreeErrorCode.DUPLICATE_ID.getMessage()).containsIgnoringCase("duplicate");
        }
    }

    @Nested
    @DisplayName("Uniqueness Tests")
    class UniquenessTests {

        @Test
        @DisplayName("all codes should be unique")
        void allCodesShouldBeUnique() {
            TreeErrorCode[] codes = TreeErrorCode.values();
            java.util.Set<String> codeValues = new java.util.HashSet<>();

            for (TreeErrorCode code : codes) {
                assertThat(codeValues.add(code.getCode()))
                    .as("Code %s should be unique", code.getCode())
                    .isTrue();
            }
        }

        @Test
        @DisplayName("all enum names should be unique")
        void allEnumNamesShouldBeUnique() {
            TreeErrorCode[] codes = TreeErrorCode.values();
            java.util.Set<String> names = new java.util.HashSet<>();

            for (TreeErrorCode code : codes) {
                assertThat(names.add(code.name()))
                    .as("Name %s should be unique", code.name())
                    .isTrue();
            }
        }
    }
}
