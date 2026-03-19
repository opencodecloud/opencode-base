package cloud.opencode.base.pdf.form;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfForm 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfForm 测试")
class PdfFormTest {

    @Nested
    @DisplayName("接口方法声明测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("声明 getFieldNames 方法")
        void testHasGetFieldNamesMethod() throws NoSuchMethodException {
            assertThat(PdfForm.class.getMethod("getFieldNames")).isNotNull();
        }

        @Test
        @DisplayName("声明 getField 方法")
        void testHasGetFieldMethod() throws NoSuchMethodException {
            assertThat(PdfForm.class.getMethod("getField", String.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 getFields 方法")
        void testHasGetFieldsMethod() throws NoSuchMethodException {
            assertThat(PdfForm.class.getMethod("getFields")).isNotNull();
        }

        @Test
        @DisplayName("声明泛型 getFields 方法")
        void testHasGenericGetFieldsMethod() throws NoSuchMethodException {
            assertThat(PdfForm.class.getMethod("getFields", Class.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 setFieldValue 方法")
        void testHasSetFieldValueMethod() throws NoSuchMethodException {
            assertThat(PdfForm.class.getMethod("setFieldValue", String.class, String.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 setFieldValues 方法")
        void testHasSetFieldValuesMethod() throws NoSuchMethodException {
            assertThat(PdfForm.class.getMethod("setFieldValues", Map.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 getFieldValue 方法")
        void testHasGetFieldValueMethod() throws NoSuchMethodException {
            assertThat(PdfForm.class.getMethod("getFieldValue", String.class)).isNotNull();
        }

        @Test
        @DisplayName("声明 getFieldValues 方法")
        void testHasGetFieldValuesMethod() throws NoSuchMethodException {
            assertThat(PdfForm.class.getMethod("getFieldValues")).isNotNull();
        }

        @Test
        @DisplayName("声明 flatten 方法")
        void testHasFlattenMethod() throws NoSuchMethodException {
            assertThat(PdfForm.class.getMethod("flatten")).isNotNull();
        }

        @Test
        @DisplayName("声明带参数的 flatten 方法")
        void testHasFlattenWithArgsMethod() throws NoSuchMethodException {
            assertThat(PdfForm.class.getMethod("flatten", String[].class)).isNotNull();
        }

        @Test
        @DisplayName("声明 clearValues 方法")
        void testHasClearValuesMethod() throws NoSuchMethodException {
            assertThat(PdfForm.class.getMethod("clearValues")).isNotNull();
        }

        @Test
        @DisplayName("声明 needsAppearances 方法")
        void testHasNeedsAppearancesMethod() throws NoSuchMethodException {
            assertThat(PdfForm.class.getMethod("needsAppearances")).isNotNull();
        }

        @Test
        @DisplayName("声明 generateAppearances 方法")
        void testHasGenerateAppearancesMethod() throws NoSuchMethodException {
            assertThat(PdfForm.class.getMethod("generateAppearances")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Mock 实现测试")
    class MockImplementationTests {

        @Test
        @DisplayName("getFieldNames 返回字段名集合")
        void testGetFieldNames() {
            PdfForm form = createMockForm();

            assertThat(form.getFieldNames()).containsExactlyInAnyOrder("name", "email", "agree");
        }

        @Test
        @DisplayName("getField 返回指定字段")
        void testGetField() {
            PdfForm form = createMockForm();

            assertThat(form.getField("name")).isNotNull();
            assertThat(form.getField("name").getName()).isEqualTo("name");
        }

        @Test
        @DisplayName("getField 返回 null 当字段不存在")
        void testGetFieldNotFound() {
            PdfForm form = createMockForm();

            assertThat(form.getField("nonexistent")).isNull();
        }

        @Test
        @DisplayName("getFields 返回所有字段")
        void testGetFields() {
            PdfForm form = createMockForm();

            assertThat(form.getFields()).hasSize(3);
        }

        @Test
        @DisplayName("getFields 按类型过滤")
        void testGetFieldsByType() {
            PdfForm form = createMockForm();

            List<TextField> textFields = form.getFields(TextField.class);
            assertThat(textFields).hasSize(2);
        }

        @Test
        @DisplayName("getFieldValue 返回字段值")
        void testGetFieldValue() {
            PdfForm form = createMockForm();

            assertThat(form.getFieldValue("name")).isEqualTo("John");
            assertThat(form.getFieldValue("email")).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("getFieldValues 返回所有字段值")
        void testGetFieldValues() {
            PdfForm form = createMockForm();

            Map<String, String> values = form.getFieldValues();
            assertThat(values).containsEntry("name", "John");
            assertThat(values).containsEntry("email", "john@example.com");
        }

        @Test
        @DisplayName("needsAppearances 返回布尔值")
        void testNeedsAppearances() {
            PdfForm form = createMockForm();

            assertThat(form.needsAppearances()).isFalse();
        }
    }

    private PdfForm createMockForm() {
        TextField nameField = createMockTextField("name", "John");
        TextField emailField = createMockTextField("email", "john@example.com");
        CheckBox agreeField = createMockCheckBox("agree", true);

        List<FormField> fields = List.of(nameField, emailField, agreeField);

        return new PdfForm() {
            @Override
            public Set<String> getFieldNames() {
                return Set.of("name", "email", "agree");
            }

            @Override
            public FormField getField(String name) {
                return fields.stream()
                    .filter(f -> f.getName().equals(name))
                    .findFirst()
                    .orElse(null);
            }

            @Override
            public List<FormField> getFields() {
                return fields;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T extends FormField> List<T> getFields(Class<T> fieldType) {
                return fields.stream()
                    .filter(fieldType::isInstance)
                    .map(f -> (T) f)
                    .toList();
            }

            @Override
            public void setFieldValue(String name, String value) {}

            @Override
            public void setFieldValues(Map<String, String> values) {}

            @Override
            public String getFieldValue(String name) {
                FormField field = getField(name);
                return field != null ? field.getValue() : null;
            }

            @Override
            public Map<String, String> getFieldValues() {
                return Map.of("name", "John", "email", "john@example.com", "agree", "Yes");
            }

            @Override
            public void flatten() {}

            @Override
            public void flatten(String... fieldNames) {}

            @Override
            public void clearValues() {}

            @Override
            public boolean needsAppearances() { return false; }

            @Override
            public void generateAppearances() {}
        };
    }

    private TextField createMockTextField(String name, String value) {
        return new TextField() {
            @Override
            public int getMaxLength() { return -1; }
            @Override
            public boolean isMultiline() { return false; }
            @Override
            public boolean isPassword() { return false; }
            @Override
            public int getAlignment() { return 0; }
            @Override
            public String getName() { return name; }
            @Override
            public String getValue() { return value; }
            @Override
            public void setValue(String v) {}
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

    private CheckBox createMockCheckBox(String name, boolean checked) {
        return new CheckBox() {
            @Override
            public boolean isChecked() { return checked; }
            @Override
            public void setChecked(boolean c) {}
            @Override
            public String getExportValue() { return "Yes"; }
            @Override
            public String getName() { return name; }
            @Override
            public String getValue() { return checked ? "Yes" : "Off"; }
            @Override
            public void setValue(String v) {}
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
