package cloud.opencode.base.tree.validation;

import cloud.opencode.base.tree.DefaultTreeNode;
import cloud.opencode.base.tree.exception.TreeException;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeNodeValidatorTest Tests
 * TreeNodeValidatorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@DisplayName("TreeNodeValidator Tests")
class TreeNodeValidatorTest {

    @Nested
    @DisplayName("Validate Tests")
    class ValidateTests {

        @Test
        @DisplayName("validate should return valid for valid nodes")
        void validateShouldReturnValidForValidNodes() {
            List<DefaultTreeNode<Long>> nodes = List.of(
                new DefaultTreeNode<>(1L, null, "Node1"),
                new DefaultTreeNode<>(2L, 1L, "Node2"),
                new DefaultTreeNode<>(3L, 1L, "Node3")
            );

            TreeNodeValidator.ValidationResult result = TreeNodeValidator.validate(nodes);

            assertThat(result.valid()).isTrue();
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("validate should detect null ID")
        void validateShouldDetectNullId() {
            List<DefaultTreeNode<Long>> nodes = List.of(
                new DefaultTreeNode<>(null, null, "NoId")
            );

            TreeNodeValidator.ValidationResult result = TreeNodeValidator.validate(nodes);

            assertThat(result.valid()).isFalse();
            assertThat(result.hasErrors()).isTrue();
            assertThat(result.errors()).anyMatch(e -> e.contains("null ID"));
        }

        @Test
        @DisplayName("validate should detect duplicate IDs")
        void validateShouldDetectDuplicateIds() {
            List<DefaultTreeNode<Long>> nodes = List.of(
                new DefaultTreeNode<>(1L, null, "Node1"),
                new DefaultTreeNode<>(1L, null, "Duplicate")
            );

            TreeNodeValidator.ValidationResult result = TreeNodeValidator.validate(nodes);

            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.contains("Duplicate ID"));
        }

        @Test
        @DisplayName("validate should handle empty list")
        void validateShouldHandleEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = List.of();

            TreeNodeValidator.ValidationResult result = TreeNodeValidator.validate(emptyList);

            assertThat(result.valid()).isTrue();
        }
    }

    @Nested
    @DisplayName("ValidateStructure Tests")
    class ValidateStructureTests {

        @Test
        @DisplayName("validateStructure should return valid for valid tree")
        void validateStructureShouldReturnValidForValidTree() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "Root");
            DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "Child");
            root.setChildren(new ArrayList<>(List.of(child)));

            TreeNodeValidator.ValidationResult result = TreeNodeValidator.validateStructure(List.of(root));

            assertThat(result.valid()).isTrue();
        }

        @Test
        @DisplayName("validateStructure should detect null ID in tree")
        void validateStructureShouldDetectNullIdInTree() {
            DefaultTreeNode<Long> root = new DefaultTreeNode<>(null, null, "NoId");

            TreeNodeValidator.ValidationResult result = TreeNodeValidator.validateStructure(List.of(root));

            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).anyMatch(e -> e.contains("null ID"));
        }

        @Test
        @DisplayName("validateStructure should handle empty list")
        void validateStructureShouldHandleEmptyList() {
            List<DefaultTreeNode<Long>> emptyList = List.of();

            TreeNodeValidator.ValidationResult result = TreeNodeValidator.validateStructure(emptyList);

            assertThat(result.valid()).isTrue();
        }
    }

    @Nested
    @DisplayName("ValidateOrThrow Tests")
    class ValidateOrThrowTests {

        @Test
        @DisplayName("validateOrThrow should not throw for valid nodes")
        void validateOrThrowShouldNotThrowForValidNodes() {
            List<DefaultTreeNode<Long>> nodes = List.of(
                new DefaultTreeNode<>(1L, null, "Node1")
            );

            assertThatCode(() -> TreeNodeValidator.validateOrThrow(nodes))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validateOrThrow should throw for invalid nodes")
        void validateOrThrowShouldThrowForInvalidNodes() {
            List<DefaultTreeNode<Long>> nodes = List.of(
                new DefaultTreeNode<>(null, null, "NoId")
            );

            assertThatThrownBy(() -> TreeNodeValidator.validateOrThrow(nodes))
                .isInstanceOf(TreeException.class)
                .hasMessageContaining("Validation failed");
        }
    }

    @Nested
    @DisplayName("ValidationResult Tests")
    class ValidationResultTests {

        @Test
        @DisplayName("success should create valid result")
        void successShouldCreateValidResult() {
            TreeNodeValidator.ValidationResult result = TreeNodeValidator.ValidationResult.success();

            assertThat(result.valid()).isTrue();
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("failure should create invalid result")
        void failureShouldCreateInvalidResult() {
            TreeNodeValidator.ValidationResult result = TreeNodeValidator.ValidationResult.failure(
                List.of("Error 1", "Error 2")
            );

            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).hasSize(2);
        }

        @Test
        @DisplayName("hasErrors should return true when errors exist")
        void hasErrorsShouldReturnTrueWhenErrorsExist() {
            TreeNodeValidator.ValidationResult result = TreeNodeValidator.ValidationResult.failure(
                List.of("Some error")
            );

            assertThat(result.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("getErrorMessage should join errors")
        void getErrorMessageShouldJoinErrors() {
            TreeNodeValidator.ValidationResult result = TreeNodeValidator.ValidationResult.failure(
                List.of("Error A", "Error B")
            );

            assertThat(result.getErrorMessage()).isEqualTo("Error A; Error B");
        }

        @Test
        @DisplayName("constructor should handle null errors")
        void constructorShouldHandleNullErrors() {
            TreeNodeValidator.ValidationResult result = new TreeNodeValidator.ValidationResult(false, null);

            assertThat(result.errors()).isEmpty();
        }
    }
}
