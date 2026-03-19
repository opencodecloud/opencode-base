package cloud.opencode.base.pdf.form;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CheckBox 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("CheckBox 测试")
class CheckBoxTest {

    @Nested
    @DisplayName("接口继承测试")
    class InterfaceInheritanceTests {

        @Test
        @DisplayName("CheckBox 继承 FormField")
        void testExtendsFormField() {
            assertThat(FormField.class.isAssignableFrom(CheckBox.class)).isTrue();
        }

        @Test
        @DisplayName("CheckBox 是 non-sealed 接口")
        void testIsNonSealed() {
            assertThat(CheckBox.class.isSealed()).isFalse();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("getType 返回 CHECKBOX")
        void testGetTypeReturnsCheckbox() {
            CheckBox checkBox = createMockCheckBox(false, "Yes");

            assertThat(checkBox.getType()).isEqualTo(FormField.FieldType.CHECKBOX);
        }
    }

    @Nested
    @DisplayName("接口方法声明测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("声明 isChecked 方法")
        void testHasIsCheckedMethod() throws NoSuchMethodException {
            assertThat(CheckBox.class.getMethod("isChecked")).isNotNull();
        }

        @Test
        @DisplayName("声明 setChecked 方法")
        void testHasSetCheckedMethod() throws NoSuchMethodException {
            assertThat(CheckBox.class.getMethod("setChecked", boolean.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 getExportValue 方法")
        void testHasGetExportValueMethod() throws NoSuchMethodException {
            assertThat(CheckBox.class.getMethod("getExportValue")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Mock 实现测试")
    class MockImplementationTests {

        @Test
        @DisplayName("isChecked 返回 false")
        void testUnchecked() {
            CheckBox checkBox = createMockCheckBox(false, "Yes");
            assertThat(checkBox.isChecked()).isFalse();
        }

        @Test
        @DisplayName("isChecked 返回 true")
        void testChecked() {
            CheckBox checkBox = createMockCheckBox(true, "Yes");
            assertThat(checkBox.isChecked()).isTrue();
        }

        @Test
        @DisplayName("getExportValue 返回正确值")
        void testExportValue() {
            CheckBox checkBox = createMockCheckBox(true, "Yes");
            assertThat(checkBox.getExportValue()).isEqualTo("Yes");
        }

        @Test
        @DisplayName("getExportValue 自定义值")
        void testCustomExportValue() {
            CheckBox checkBox = createMockCheckBox(true, "1");
            assertThat(checkBox.getExportValue()).isEqualTo("1");
        }

        @Test
        @DisplayName("setChecked 可以改变状态")
        void testSetChecked() {
            final boolean[] state = {false};
            CheckBox checkBox = new CheckBox() {
                @Override
                public boolean isChecked() { return state[0]; }
                @Override
                public void setChecked(boolean checked) { state[0] = checked; }
                @Override
                public String getExportValue() { return "Yes"; }
                @Override
                public String getName() { return "checkbox"; }
                @Override
                public String getValue() { return state[0] ? "Yes" : "Off"; }
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

            assertThat(checkBox.isChecked()).isFalse();
            checkBox.setChecked(true);
            assertThat(checkBox.isChecked()).isTrue();
            checkBox.setChecked(false);
            assertThat(checkBox.isChecked()).isFalse();
        }
    }

    private CheckBox createMockCheckBox(boolean checked, String exportValue) {
        return new CheckBox() {
            @Override
            public boolean isChecked() { return checked; }
            @Override
            public void setChecked(boolean checked) {}
            @Override
            public String getExportValue() { return exportValue; }
            @Override
            public String getName() { return "checkbox"; }
            @Override
            public String getValue() { return checked ? exportValue : "Off"; }
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
