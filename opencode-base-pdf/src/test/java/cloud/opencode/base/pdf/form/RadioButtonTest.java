package cloud.opencode.base.pdf.form;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * RadioButton 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("RadioButton 测试")
class RadioButtonTest {

    @Nested
    @DisplayName("接口继承测试")
    class InterfaceInheritanceTests {

        @Test
        @DisplayName("RadioButton 继承 FormField")
        void testExtendsFormField() {
            assertThat(FormField.class.isAssignableFrom(RadioButton.class)).isTrue();
        }

        @Test
        @DisplayName("RadioButton 是 non-sealed 接口")
        void testIsNonSealed() {
            assertThat(RadioButton.class.isSealed()).isFalse();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("getType 返回 RADIO")
        void testGetTypeReturnsRadio() {
            RadioButton radioButton = createMockRadioButton(
                List.of("Option1", "Option2", "Option3"), "Option1");

            assertThat(radioButton.getType()).isEqualTo(FormField.FieldType.RADIO);
        }
    }

    @Nested
    @DisplayName("接口方法声明测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("声明 getOptions 方法")
        void testHasGetOptionsMethod() throws NoSuchMethodException {
            assertThat(RadioButton.class.getMethod("getOptions")).isNotNull();
        }

        @Test
        @DisplayName("声明 getSelectedOption 方法")
        void testHasGetSelectedOptionMethod() throws NoSuchMethodException {
            assertThat(RadioButton.class.getMethod("getSelectedOption")).isNotNull();
        }

        @Test
        @DisplayName("声明 setSelectedOption 方法")
        void testHasSetSelectedOptionMethod() throws NoSuchMethodException {
            assertThat(RadioButton.class.getMethod("setSelectedOption", String.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Mock 实现测试")
    class MockImplementationTests {

        @Test
        @DisplayName("getOptions 返回选项列表")
        void testGetOptions() {
            List<String> options = List.of("Red", "Green", "Blue");
            RadioButton radioButton = createMockRadioButton(options, "Red");

            assertThat(radioButton.getOptions()).containsExactly("Red", "Green", "Blue");
        }

        @Test
        @DisplayName("getSelectedOption 返回选中的选项")
        void testGetSelectedOption() {
            RadioButton radioButton = createMockRadioButton(
                List.of("A", "B", "C"), "B");

            assertThat(radioButton.getSelectedOption()).isEqualTo("B");
        }

        @Test
        @DisplayName("setSelectedOption 可以改变选中项")
        void testSetSelectedOption() {
            final String[] selected = {"Option1"};
            List<String> options = List.of("Option1", "Option2", "Option3");

            RadioButton radioButton = new RadioButton() {
                @Override
                public List<String> getOptions() { return options; }
                @Override
                public String getSelectedOption() { return selected[0]; }
                @Override
                public void setSelectedOption(String option) { selected[0] = option; }
                @Override
                public String getName() { return "radio"; }
                @Override
                public String getValue() { return selected[0]; }
                @Override
                public void setValue(String value) { selected[0] = value; }
                @Override
                public String getTooltip() { return null; }
                @Override
                public boolean isRequired() { return false; }
                @Override
                public boolean isReadOnly() { return false; }
                @Override
                public float[] getRectangle() { return new float[4]; }
            };

            assertThat(radioButton.getSelectedOption()).isEqualTo("Option1");
            radioButton.setSelectedOption("Option2");
            assertThat(radioButton.getSelectedOption()).isEqualTo("Option2");
            radioButton.setSelectedOption("Option3");
            assertThat(radioButton.getSelectedOption()).isEqualTo("Option3");
        }

        @Test
        @DisplayName("无选中项时返回 null")
        void testNoSelection() {
            RadioButton radioButton = createMockRadioButton(
                List.of("A", "B", "C"), null);

            assertThat(radioButton.getSelectedOption()).isNull();
        }

        @Test
        @DisplayName("空选项列表")
        void testEmptyOptions() {
            RadioButton radioButton = createMockRadioButton(List.of(), null);

            assertThat(radioButton.getOptions()).isEmpty();
        }
    }

    private RadioButton createMockRadioButton(List<String> options, String selectedOption) {
        return new RadioButton() {
            @Override
            public List<String> getOptions() { return options; }
            @Override
            public String getSelectedOption() { return selectedOption; }
            @Override
            public void setSelectedOption(String option) {}
            @Override
            public String getName() { return "radio"; }
            @Override
            public String getValue() { return selectedOption; }
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
