package cloud.opencode.base.pdf.form;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * TextField 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("TextField 测试")
class TextFieldTest {

    @Nested
    @DisplayName("接口继承测试")
    class InterfaceInheritanceTests {

        @Test
        @DisplayName("TextField 继承 FormField")
        void testExtendsFormField() {
            assertThat(FormField.class.isAssignableFrom(TextField.class)).isTrue();
        }

        @Test
        @DisplayName("TextField 是 non-sealed 接口")
        void testIsNonSealed() {
            // non-sealed interfaces are not sealed
            assertThat(TextField.class.isSealed()).isFalse();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("getType 返回 TEXT")
        void testGetTypeReturnsText() {
            // Create a mock implementation to test default method
            TextField textField = new TextField() {
                @Override
                public int getMaxLength() { return -1; }
                @Override
                public boolean isMultiline() { return false; }
                @Override
                public boolean isPassword() { return false; }
                @Override
                public int getAlignment() { return 0; }
                @Override
                public String getName() { return "test"; }
                @Override
                public String getValue() { return ""; }
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

            assertThat(textField.getType()).isEqualTo(FormField.FieldType.TEXT);
        }
    }

    @Nested
    @DisplayName("接口方法声明测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("声明 getMaxLength 方法")
        void testHasGetMaxLengthMethod() throws NoSuchMethodException {
            assertThat(TextField.class.getMethod("getMaxLength")).isNotNull();
        }

        @Test
        @DisplayName("声明 isMultiline 方法")
        void testHasIsMultilineMethod() throws NoSuchMethodException {
            assertThat(TextField.class.getMethod("isMultiline")).isNotNull();
        }

        @Test
        @DisplayName("声明 isPassword 方法")
        void testHasIsPasswordMethod() throws NoSuchMethodException {
            assertThat(TextField.class.getMethod("isPassword")).isNotNull();
        }

        @Test
        @DisplayName("声明 getAlignment 方法")
        void testHasGetAlignmentMethod() throws NoSuchMethodException {
            assertThat(TextField.class.getMethod("getAlignment")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Mock 实现测试")
    class MockImplementationTests {

        private TextField createMockTextField(int maxLength, boolean multiline, boolean password, int alignment) {
            return new TextField() {
                @Override
                public int getMaxLength() { return maxLength; }
                @Override
                public boolean isMultiline() { return multiline; }
                @Override
                public boolean isPassword() { return password; }
                @Override
                public int getAlignment() { return alignment; }
                @Override
                public String getName() { return "field"; }
                @Override
                public String getValue() { return "value"; }
                @Override
                public void setValue(String value) {}
                @Override
                public String getTooltip() { return "tooltip"; }
                @Override
                public boolean isRequired() { return true; }
                @Override
                public boolean isReadOnly() { return false; }
                @Override
                public float[] getRectangle() { return new float[]{0, 0, 100, 20}; }
            };
        }

        @Test
        @DisplayName("maxLength 无限制返回 -1")
        void testUnlimitedMaxLength() {
            TextField field = createMockTextField(-1, false, false, 0);
            assertThat(field.getMaxLength()).isEqualTo(-1);
        }

        @Test
        @DisplayName("maxLength 有限制返回具体值")
        void testLimitedMaxLength() {
            TextField field = createMockTextField(100, false, false, 0);
            assertThat(field.getMaxLength()).isEqualTo(100);
        }

        @Test
        @DisplayName("multiline 为 true")
        void testMultilineTrue() {
            TextField field = createMockTextField(-1, true, false, 0);
            assertThat(field.isMultiline()).isTrue();
        }

        @Test
        @DisplayName("password 为 true")
        void testPasswordTrue() {
            TextField field = createMockTextField(-1, false, true, 0);
            assertThat(field.isPassword()).isTrue();
        }

        @Test
        @DisplayName("alignment 左对齐 (0)")
        void testAlignmentLeft() {
            TextField field = createMockTextField(-1, false, false, 0);
            assertThat(field.getAlignment()).isEqualTo(0);
        }

        @Test
        @DisplayName("alignment 居中对齐 (1)")
        void testAlignmentCenter() {
            TextField field = createMockTextField(-1, false, false, 1);
            assertThat(field.getAlignment()).isEqualTo(1);
        }

        @Test
        @DisplayName("alignment 右对齐 (2)")
        void testAlignmentRight() {
            TextField field = createMockTextField(-1, false, false, 2);
            assertThat(field.getAlignment()).isEqualTo(2);
        }
    }
}
