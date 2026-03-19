package cloud.opencode.base.pdf.form;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ComboBox 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("ComboBox 测试")
class ComboBoxTest {

    @Nested
    @DisplayName("接口继承测试")
    class InterfaceInheritanceTests {

        @Test
        @DisplayName("ComboBox 继承 FormField")
        void testExtendsFormField() {
            assertThat(FormField.class.isAssignableFrom(ComboBox.class)).isTrue();
        }

        @Test
        @DisplayName("ComboBox 是 non-sealed 接口")
        void testIsNonSealed() {
            assertThat(ComboBox.class.isSealed()).isFalse();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("getType 返回 COMBO")
        void testGetTypeReturnsCombo() {
            ComboBox comboBox = createMockComboBox(List.of("A", "B"), false);

            assertThat(comboBox.getType()).isEqualTo(FormField.FieldType.COMBO);
        }
    }

    @Nested
    @DisplayName("接口方法声明测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("声明 getOptions 方法")
        void testHasGetOptionsMethod() throws NoSuchMethodException {
            assertThat(ComboBox.class.getMethod("getOptions")).isNotNull();
        }

        @Test
        @DisplayName("声明 isEditable 方法")
        void testHasIsEditableMethod() throws NoSuchMethodException {
            assertThat(ComboBox.class.getMethod("isEditable")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Mock 实现测试")
    class MockImplementationTests {

        @Test
        @DisplayName("getOptions 返回选项列表")
        void testGetOptions() {
            List<String> options = List.of("Small", "Medium", "Large");
            ComboBox comboBox = createMockComboBox(options, false);

            assertThat(comboBox.getOptions()).containsExactly("Small", "Medium", "Large");
        }

        @Test
        @DisplayName("isEditable 返回 false")
        void testNotEditable() {
            ComboBox comboBox = createMockComboBox(List.of("A", "B"), false);

            assertThat(comboBox.isEditable()).isFalse();
        }

        @Test
        @DisplayName("isEditable 返回 true")
        void testEditable() {
            ComboBox comboBox = createMockComboBox(List.of("A", "B"), true);

            assertThat(comboBox.isEditable()).isTrue();
        }

        @Test
        @DisplayName("空选项列表")
        void testEmptyOptions() {
            ComboBox comboBox = createMockComboBox(List.of(), false);

            assertThat(comboBox.getOptions()).isEmpty();
        }

        @Test
        @DisplayName("多个选项")
        void testMultipleOptions() {
            List<String> options = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
            ComboBox comboBox = createMockComboBox(options, false);

            assertThat(comboBox.getOptions()).hasSize(12);
        }
    }

    private ComboBox createMockComboBox(List<String> options, boolean editable) {
        return new ComboBox() {
            @Override
            public List<String> getOptions() { return options; }
            @Override
            public boolean isEditable() { return editable; }
            @Override
            public String getName() { return "combo"; }
            @Override
            public String getValue() { return options.isEmpty() ? "" : options.getFirst(); }
            @Override
            public void setValue(String value) {}
            @Override
            public String getTooltip() { return null; }
            @Override
            public boolean isRequired() { return false; }
            @Override
            public boolean isReadOnly() { return false; }
            @Override
            public float[] getRectangle() { return new float[4]; }
        };
    }
}
