package cloud.opencode.base.pdf.form;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ListBox 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("ListBox 测试")
class ListBoxTest {

    @Nested
    @DisplayName("接口继承测试")
    class InterfaceInheritanceTests {

        @Test
        @DisplayName("ListBox 继承 FormField")
        void testExtendsFormField() {
            assertThat(FormField.class.isAssignableFrom(ListBox.class)).isTrue();
        }

        @Test
        @DisplayName("ListBox 是 non-sealed 接口")
        void testIsNonSealed() {
            assertThat(ListBox.class.isSealed()).isFalse();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("getType 返回 LIST")
        void testGetTypeReturnsList() {
            ListBox listBox = createMockListBox(List.of("A", "B"), List.of(), false);

            assertThat(listBox.getType()).isEqualTo(FormField.FieldType.LIST);
        }
    }

    @Nested
    @DisplayName("接口方法声明测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("声明 getOptions 方法")
        void testHasGetOptionsMethod() throws NoSuchMethodException {
            assertThat(ListBox.class.getMethod("getOptions")).isNotNull();
        }

        @Test
        @DisplayName("声明 getSelectedOptions 方法")
        void testHasGetSelectedOptionsMethod() throws NoSuchMethodException {
            assertThat(ListBox.class.getMethod("getSelectedOptions")).isNotNull();
        }

        @Test
        @DisplayName("声明 isMultiSelect 方法")
        void testHasIsMultiSelectMethod() throws NoSuchMethodException {
            assertThat(ListBox.class.getMethod("isMultiSelect")).isNotNull();
        }

        @Test
        @DisplayName("声明 setSelectedOptions 方法")
        void testHasSetSelectedOptionsMethod() throws NoSuchMethodException {
            assertThat(ListBox.class.getMethod("setSelectedOptions", List.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Mock 实现测试")
    class MockImplementationTests {

        @Test
        @DisplayName("getOptions 返回选项列表")
        void testGetOptions() {
            List<String> options = List.of("Apple", "Banana", "Cherry");
            ListBox listBox = createMockListBox(options, List.of(), false);

            assertThat(listBox.getOptions()).containsExactly("Apple", "Banana", "Cherry");
        }

        @Test
        @DisplayName("getSelectedOptions 返回空列表")
        void testNoSelection() {
            ListBox listBox = createMockListBox(List.of("A", "B", "C"), List.of(), false);

            assertThat(listBox.getSelectedOptions()).isEmpty();
        }

        @Test
        @DisplayName("getSelectedOptions 返回单选项")
        void testSingleSelection() {
            ListBox listBox = createMockListBox(
                List.of("A", "B", "C"), List.of("B"), false);

            assertThat(listBox.getSelectedOptions()).containsExactly("B");
        }

        @Test
        @DisplayName("getSelectedOptions 返回多选项")
        void testMultiSelection() {
            ListBox listBox = createMockListBox(
                List.of("A", "B", "C", "D"), List.of("A", "C"), true);

            assertThat(listBox.getSelectedOptions()).containsExactly("A", "C");
        }

        @Test
        @DisplayName("isMultiSelect 返回 false")
        void testSingleSelect() {
            ListBox listBox = createMockListBox(List.of("A", "B"), List.of(), false);

            assertThat(listBox.isMultiSelect()).isFalse();
        }

        @Test
        @DisplayName("isMultiSelect 返回 true")
        void testMultiSelect() {
            ListBox listBox = createMockListBox(List.of("A", "B"), List.of(), true);

            assertThat(listBox.isMultiSelect()).isTrue();
        }

        @Test
        @DisplayName("setSelectedOptions 可以改变选中项")
        void testSetSelectedOptions() {
            List<String> options = List.of("X", "Y", "Z");
            List<String> selected = new ArrayList<>();

            ListBox listBox = new ListBox() {
                @Override
                public List<String> getOptions() { return options; }
                @Override
                public List<String> getSelectedOptions() { return List.copyOf(selected); }
                @Override
                public boolean isMultiSelect() { return true; }
                @Override
                public void setSelectedOptions(List<String> opts) {
                    selected.clear();
                    selected.addAll(opts);
                }
                @Override
                public String getName() { return "list"; }
                @Override
                public String getValue() { return String.join(",", selected); }
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

            assertThat(listBox.getSelectedOptions()).isEmpty();
            listBox.setSelectedOptions(List.of("X", "Z"));
            assertThat(listBox.getSelectedOptions()).containsExactly("X", "Z");
            listBox.setSelectedOptions(List.of("Y"));
            assertThat(listBox.getSelectedOptions()).containsExactly("Y");
        }

        @Test
        @DisplayName("空选项列表")
        void testEmptyOptions() {
            ListBox listBox = createMockListBox(List.of(), List.of(), false);

            assertThat(listBox.getOptions()).isEmpty();
            assertThat(listBox.getSelectedOptions()).isEmpty();
        }
    }

    private ListBox createMockListBox(List<String> options, List<String> selected, boolean multiSelect) {
        return new ListBox() {
            @Override
            public List<String> getOptions() { return options; }
            @Override
            public List<String> getSelectedOptions() { return selected; }
            @Override
            public boolean isMultiSelect() { return multiSelect; }
            @Override
            public void setSelectedOptions(List<String> opts) {}
            @Override
            public String getName() { return "list"; }
            @Override
            public String getValue() { return String.join(",", selected); }
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
