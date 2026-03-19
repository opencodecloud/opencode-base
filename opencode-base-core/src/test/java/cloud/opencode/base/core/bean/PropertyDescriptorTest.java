package cloud.opencode.base.core.bean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PropertyDescriptor 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("PropertyDescriptor 测试")
class PropertyDescriptorTest {

    // 测试用注解
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface TestAnnotation {
        String value() default "";
    }

    // 测试用类
    static class TestBean {
        @TestAnnotation("field")
        private String name;
        private int age;
        private List<String> items;
        private boolean active;

        @TestAnnotation("getter")
        public String getName() { return name; }
        @TestAnnotation("setter")
        public void setName(String name) { this.name = name; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public List<String> getItems() { return items; }
        public void setItems(List<String> items) { this.items = items; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    // 只读类
    static class ReadOnlyBean {
        private String value;
        public String getValue() { return value; }
        // 没有 setter
    }

    // 只写类
    static class WriteOnlyBean {
        private String value;
        // 没有 getter
        public void setValue(String value) { this.value = value; }
    }

    private PropertyDescriptor createDescriptor(Class<?> clazz, String name) throws Exception {
        Field field = null;
        Method getter = null;
        Method setter = null;
        Class<?> type = null;

        try {
            field = clazz.getDeclaredField(name);
            type = field.getType();
        } catch (NoSuchFieldException ignored) {}

        // 找 getter
        try {
            getter = clazz.getMethod("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
            type = getter.getReturnType();
        } catch (NoSuchMethodException ignored) {}

        if (getter == null) {
            try {
                getter = clazz.getMethod("is" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
                type = getter.getReturnType();
            } catch (NoSuchMethodException ignored) {}
        }

        // 找 setter
        if (type != null) {
            try {
                setter = clazz.getMethod("set" + Character.toUpperCase(name.charAt(0)) + name.substring(1), type);
            } catch (NoSuchMethodException ignored) {}
        }

        return new PropertyDescriptor(name, type, getter, setter, field);
    }

    @Nested
    @DisplayName("基本属性测试")
    class BasicPropertiesTests {

        @Test
        @DisplayName("name 获取")
        void testName() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "name");
            assertThat(pd.name()).isEqualTo("name");
        }

        @Test
        @DisplayName("type 获取")
        void testType() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "name");
            assertThat(pd.type()).isEqualTo(String.class);

            PropertyDescriptor pdAge = createDescriptor(TestBean.class, "age");
            assertThat(pdAge.type()).isEqualTo(int.class);
        }

        @Test
        @DisplayName("readMethod 获取")
        void testReadMethod() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "name");
            assertThat(pd.readMethod()).isNotNull();
            assertThat(pd.readMethod().getName()).isEqualTo("getName");
        }

        @Test
        @DisplayName("writeMethod 获取")
        void testWriteMethod() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "name");
            assertThat(pd.writeMethod()).isNotNull();
            assertThat(pd.writeMethod().getName()).isEqualTo("setName");
        }

        @Test
        @DisplayName("field 获取")
        void testField() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "name");
            assertThat(pd.field()).isNotNull();
            assertThat(pd.field().getName()).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("状态判断测试")
    class StateCheckTests {

        @Test
        @DisplayName("isReadable")
        void testIsReadable() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "name");
            assertThat(pd.isReadable()).isTrue();

            PropertyDescriptor woBean = createDescriptor(WriteOnlyBean.class, "value");
            assertThat(woBean.isReadable()).isFalse();
        }

        @Test
        @DisplayName("isWritable")
        void testIsWritable() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "name");
            assertThat(pd.isWritable()).isTrue();

            PropertyDescriptor roBean = createDescriptor(ReadOnlyBean.class, "value");
            assertThat(roBean.isWritable()).isFalse();
        }

        @Test
        @DisplayName("hasField")
        void testHasField() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "name");
            assertThat(pd.hasField()).isTrue();

            // 不存在字段的情况
            PropertyDescriptor noField = new PropertyDescriptor("virtual", String.class,
                    TestBean.class.getMethod("getName"), null, null);
            assertThat(noField.hasField()).isFalse();
        }

        @Test
        @DisplayName("isActive 布尔属性")
        void testBooleanProperty() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "active");
            assertThat(pd.isReadable()).isTrue();
            assertThat(pd.isWritable()).isTrue();
            assertThat(pd.readMethod().getName()).isEqualTo("isActive");
        }
    }

    @Nested
    @DisplayName("值操作测试")
    class ValueOperationTests {

        @Test
        @DisplayName("getValue")
        void testGetValue() throws Exception {
            TestBean bean = new TestBean();
            bean.setName("Leon");

            PropertyDescriptor pd = createDescriptor(TestBean.class, "name");
            Object value = pd.getValue(bean);
            assertThat(value).isEqualTo("Leon");
        }

        @Test
        @DisplayName("getValue int 类型")
        void testGetValueInt() throws Exception {
            TestBean bean = new TestBean();
            bean.setAge(30);

            PropertyDescriptor pd = createDescriptor(TestBean.class, "age");
            Object value = pd.getValue(bean);
            assertThat(value).isEqualTo(30);
        }

        @Test
        @DisplayName("setValue")
        void testSetValue() throws Exception {
            TestBean bean = new TestBean();

            PropertyDescriptor pd = createDescriptor(TestBean.class, "name");
            pd.setValue(bean, "Test");
            assertThat(bean.getName()).isEqualTo("Test");
        }

        @Test
        @DisplayName("setValue int 类型")
        void testSetValueInt() throws Exception {
            TestBean bean = new TestBean();

            PropertyDescriptor pd = createDescriptor(TestBean.class, "age");
            pd.setValue(bean, 25);
            assertThat(bean.getAge()).isEqualTo(25);
        }

        @Test
        @DisplayName("getValue 通过字段（无 getter）")
        void testGetValueFromField() throws Exception {
            WriteOnlyBean bean = new WriteOnlyBean();
            bean.setValue("test");

            Field field = WriteOnlyBean.class.getDeclaredField("value");
            PropertyDescriptor pd = new PropertyDescriptor("value", String.class, null, null, field);

            // 由于有字段可以读取
            Object value = pd.getValue(bean);
            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("setValue 通过字段（无 setter）")
        void testSetValueToField() throws Exception {
            ReadOnlyBean bean = new ReadOnlyBean();

            Field field = ReadOnlyBean.class.getDeclaredField("value");
            PropertyDescriptor pd = new PropertyDescriptor("value", String.class, null, null, field);

            pd.setValue(bean, "test");
            assertThat(pd.getValue(bean)).isEqualTo("test");
        }

        @Test
        @DisplayName("getValue 无读取方法抛异常")
        void testGetValueNoReadMethod() {
            PropertyDescriptor pd = new PropertyDescriptor("test", String.class, null, null, null);
            assertThatThrownBy(() -> pd.getValue(new TestBean()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not readable");
        }

        @Test
        @DisplayName("setValue 无写入方法抛异常")
        void testSetValueNoWriteMethod() {
            PropertyDescriptor pd = new PropertyDescriptor("test", String.class, null, null, null);
            assertThatThrownBy(() -> pd.setValue(new TestBean(), "value"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not writable");
        }
    }

    @Nested
    @DisplayName("类型信息测试")
    class TypeInfoTests {

        @Test
        @DisplayName("getGenericType 从 getter")
        void testGetGenericTypeFromGetter() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "items");
            Type genericType = pd.getGenericType();
            assertThat(genericType.getTypeName()).contains("List");
            assertThat(genericType.getTypeName()).contains("String");
        }

        @Test
        @DisplayName("getGenericType 从字段")
        void testGetGenericTypeFromField() throws Exception {
            Field field = TestBean.class.getDeclaredField("items");
            PropertyDescriptor pd = new PropertyDescriptor("items", List.class, null, null, field);
            Type genericType = pd.getGenericType();
            assertThat(genericType.getTypeName()).contains("List");
        }

        @Test
        @DisplayName("getGenericType 无泛型")
        void testGetGenericTypeSimple() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "name");
            Type genericType = pd.getGenericType();
            assertThat(genericType).isEqualTo(String.class);
        }

        @Test
        @DisplayName("getGenericType 无方法和字段返回类型")
        void testGetGenericTypeNoMethodOrField() {
            PropertyDescriptor pd = new PropertyDescriptor("test", String.class, null, null, null);
            Type genericType = pd.getGenericType();
            assertThat(genericType).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("注解操作测试")
    class AnnotationTests {

        @Test
        @DisplayName("getAnnotation 从 getter")
        void testGetAnnotationFromGetter() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "name");
            TestAnnotation annotation = pd.getAnnotation(TestAnnotation.class);
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("getter");
        }

        @Test
        @DisplayName("getAnnotation 从字段")
        void testGetAnnotationFromField() throws Exception {
            Field field = TestBean.class.getDeclaredField("name");
            PropertyDescriptor pd = new PropertyDescriptor("name", String.class, null, null, field);
            TestAnnotation annotation = pd.getAnnotation(TestAnnotation.class);
            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("field");
        }

        @Test
        @DisplayName("getAnnotation 不存在")
        void testGetAnnotationNotFound() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "age");
            TestAnnotation annotation = pd.getAnnotation(TestAnnotation.class);
            assertThat(annotation).isNull();
        }

        @Test
        @DisplayName("hasAnnotation")
        void testHasAnnotation() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "name");
            assertThat(pd.hasAnnotation(TestAnnotation.class)).isTrue();

            PropertyDescriptor pdAge = createDescriptor(TestBean.class, "age");
            assertThat(pdAge.hasAnnotation(TestAnnotation.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString 正常")
        void testToString() throws Exception {
            PropertyDescriptor pd = createDescriptor(TestBean.class, "name");
            String str = pd.toString();
            assertThat(str).contains("name");
            assertThat(str).contains("String");
            assertThat(str).contains("readable=true");
            assertThat(str).contains("writable=true");
        }

        @Test
        @DisplayName("toString 只读属性")
        void testToStringReadOnly() throws Exception {
            PropertyDescriptor pd = createDescriptor(ReadOnlyBean.class, "value");
            String str = pd.toString();
            assertThat(str).contains("readable=true");
            assertThat(str).contains("writable=false");
        }
    }
}
