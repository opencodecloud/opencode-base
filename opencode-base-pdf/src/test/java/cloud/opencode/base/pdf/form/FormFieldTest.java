package cloud.opencode.base.pdf.form;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * FormField 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("FormField 测试")
class FormFieldTest {

    @Nested
    @DisplayName("密封接口测试")
    class SealedInterfaceTests {

        @Test
        @DisplayName("FormField 是密封接口")
        void testIsSealed() {
            assertThat(FormField.class.isSealed()).isTrue();
        }

        @Test
        @DisplayName("允许的子类包含 TextField")
        void testPermitsTextField() {
            assertThat(FormField.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .contains("TextField");
        }

        @Test
        @DisplayName("允许的子类包含 CheckBox")
        void testPermitsCheckBox() {
            assertThat(FormField.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .contains("CheckBox");
        }

        @Test
        @DisplayName("允许的子类包含 RadioButton")
        void testPermitsRadioButton() {
            assertThat(FormField.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .contains("RadioButton");
        }

        @Test
        @DisplayName("允许的子类包含 ComboBox")
        void testPermitsComboBox() {
            assertThat(FormField.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .contains("ComboBox");
        }

        @Test
        @DisplayName("允许的子类包含 ListBox")
        void testPermitsListBox() {
            assertThat(FormField.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .contains("ListBox");
        }

        @Test
        @DisplayName("允许的子类包含 SignatureField")
        void testPermitsSignatureField() {
            assertThat(FormField.class.getPermittedSubclasses())
                .extracting(Class::getSimpleName)
                .contains("SignatureField");
        }

        @Test
        @DisplayName("共有6个允许的子类")
        void testPermittedSubclassCount() {
            assertThat(FormField.class.getPermittedSubclasses()).hasSize(6);
        }
    }

    @Nested
    @DisplayName("FieldType 枚举测试")
    class FieldTypeEnumTests {

        @Test
        @DisplayName("包含所有字段类型")
        void testAllFieldTypes() {
            assertThat(FormField.FieldType.values()).containsExactly(
                FormField.FieldType.TEXT,
                FormField.FieldType.CHECKBOX,
                FormField.FieldType.RADIO,
                FormField.FieldType.COMBO,
                FormField.FieldType.LIST,
                FormField.FieldType.SIGNATURE
            );
        }

        @Test
        @DisplayName("共有6种字段类型")
        void testFieldTypeCount() {
            assertThat(FormField.FieldType.values()).hasSize(6);
        }

        @Test
        @DisplayName("valueOf 方法正常工作")
        void testValueOf() {
            assertThat(FormField.FieldType.valueOf("TEXT")).isEqualTo(FormField.FieldType.TEXT);
            assertThat(FormField.FieldType.valueOf("CHECKBOX")).isEqualTo(FormField.FieldType.CHECKBOX);
            assertThat(FormField.FieldType.valueOf("RADIO")).isEqualTo(FormField.FieldType.RADIO);
            assertThat(FormField.FieldType.valueOf("COMBO")).isEqualTo(FormField.FieldType.COMBO);
            assertThat(FormField.FieldType.valueOf("LIST")).isEqualTo(FormField.FieldType.LIST);
            assertThat(FormField.FieldType.valueOf("SIGNATURE")).isEqualTo(FormField.FieldType.SIGNATURE);
        }

        @Test
        @DisplayName("无效名称抛出异常")
        void testInvalidValueOf() {
            assertThatThrownBy(() -> FormField.FieldType.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("接口方法声明测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("声明 getName 方法")
        void testHasGetNameMethod() throws NoSuchMethodException {
            assertThat(FormField.class.getMethod("getName")).isNotNull();
        }

        @Test
        @DisplayName("声明 getType 方法")
        void testHasGetTypeMethod() throws NoSuchMethodException {
            assertThat(FormField.class.getMethod("getType")).isNotNull();
        }

        @Test
        @DisplayName("声明 getValue 方法")
        void testHasGetValueMethod() throws NoSuchMethodException {
            assertThat(FormField.class.getMethod("getValue")).isNotNull();
        }

        @Test
        @DisplayName("声明 setValue 方法")
        void testHasSetValueMethod() throws NoSuchMethodException {
            assertThat(FormField.class.getMethod("setValue", String.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 getTooltip 方法")
        void testHasGetTooltipMethod() throws NoSuchMethodException {
            assertThat(FormField.class.getMethod("getTooltip")).isNotNull();
        }

        @Test
        @DisplayName("声明 isRequired 方法")
        void testHasIsRequiredMethod() throws NoSuchMethodException {
            assertThat(FormField.class.getMethod("isRequired")).isNotNull();
        }

        @Test
        @DisplayName("声明 isReadOnly 方法")
        void testHasIsReadOnlyMethod() throws NoSuchMethodException {
            assertThat(FormField.class.getMethod("isReadOnly")).isNotNull();
        }

        @Test
        @DisplayName("声明 getRectangle 方法")
        void testHasGetRectangleMethod() throws NoSuchMethodException {
            assertThat(FormField.class.getMethod("getRectangle")).isNotNull();
        }
    }
}
