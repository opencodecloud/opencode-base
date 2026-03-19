package cloud.opencode.base.core.reflect;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * ReflectUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("ReflectUtil 测试")
class ReflectUtilTest {

    // 测试用类
    static class TestClass {
        private String name;
        public int age;
        private static String staticField = "static";

        public TestClass() {}
        public TestClass(String name) { this.name = name; }
        public TestClass(String name, int age) { this.name = name; this.age = age; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        private String privateMethod() { return "private"; }
        public static String staticMethod() { return "staticMethod"; }
        public String concat(String a, String b) { return a + b; }
    }

    static class ChildClass extends TestClass {
        private String childField;
        public ChildClass() { super(); }
    }

    @Nested
    @DisplayName("实例创建测试")
    class InstanceCreationTests {

        @Test
        @DisplayName("newInstance 无参构造")
        void testNewInstanceNoArgs() {
            TestClass instance = ReflectUtil.newInstance(TestClass.class);
            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("newInstance 带参构造")
        void testNewInstanceWithArgs() {
            TestClass instance = ReflectUtil.newInstance(TestClass.class, "Leon");
            assertThat(instance).isNotNull();
            assertThat(instance.getName()).isEqualTo("Leon");
        }

        @Test
        @DisplayName("newInstance 多参构造")
        void testNewInstanceMultiArgs() {
            TestClass instance = ReflectUtil.newInstance(TestClass.class, "Leon", 30);
            assertThat(instance).isNotNull();
            assertThat(instance.getName()).isEqualTo("Leon");
            assertThat(instance.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("newInstance 无默认构造抛异常")
        void testNewInstanceNoDefaultConstructor() {
            assertThatThrownBy(() -> ReflectUtil.newInstance(Integer.class))
                    .isInstanceOf(OpenException.class);
        }
    }

    @Nested
    @DisplayName("方法调用测试")
    class MethodInvocationTests {

        @Test
        @DisplayName("invoke 无参方法")
        void testInvokeNoArgs() {
            TestClass obj = new TestClass("Leon");
            String result = ReflectUtil.invoke(obj, "getName");
            assertThat(result).isEqualTo("Leon");
        }

        @Test
        @DisplayName("invoke 带参方法")
        void testInvokeWithArgs() {
            TestClass obj = new TestClass();
            String result = ReflectUtil.invoke(obj, "concat", "Hello", " World");
            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("invoke void 方法")
        void testInvokeVoidMethod() {
            TestClass obj = new TestClass();
            ReflectUtil.invoke(obj, "setName", "Test");
            assertThat(obj.getName()).isEqualTo("Test");
        }

        @Test
        @DisplayName("invoke 私有方法")
        void testInvokePrivateMethod() {
            TestClass obj = new TestClass();
            String result = ReflectUtil.invoke(obj, "privateMethod");
            assertThat(result).isEqualTo("private");
        }

        @Test
        @DisplayName("invoke 不存在的方法抛异常")
        void testInvokeNonExistentMethod() {
            TestClass obj = new TestClass();
            assertThatThrownBy(() -> ReflectUtil.invoke(obj, "nonExistent"))
                    .isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("invokeStatic")
        void testInvokeStatic() {
            String result = ReflectUtil.invokeStatic(TestClass.class, "staticMethod");
            assertThat(result).isEqualTo("staticMethod");
        }
    }

    @Nested
    @DisplayName("字段操作测试")
    class FieldOperationTests {

        @Test
        @DisplayName("getFieldValue")
        void testGetFieldValue() {
            TestClass obj = new TestClass("Leon");
            String value = ReflectUtil.getFieldValue(obj, "name");
            assertThat(value).isEqualTo("Leon");
        }

        @Test
        @DisplayName("getFieldValue 公共字段")
        void testGetFieldValuePublic() {
            TestClass obj = new TestClass();
            obj.age = 30;
            int value = ReflectUtil.getFieldValue(obj, "age");
            assertThat(value).isEqualTo(30);
        }

        @Test
        @DisplayName("setFieldValue")
        void testSetFieldValue() {
            TestClass obj = new TestClass();
            ReflectUtil.setFieldValue(obj, "name", "Test");
            assertThat(obj.getName()).isEqualTo("Test");
        }

        @Test
        @DisplayName("setFieldValue 公共字段")
        void testSetFieldValuePublic() {
            TestClass obj = new TestClass();
            ReflectUtil.setFieldValue(obj, "age", 25);
            assertThat(obj.getAge()).isEqualTo(25);
        }

        @Test
        @DisplayName("getFieldValue 不存在的字段抛异常")
        void testGetFieldValueNonExistent() {
            TestClass obj = new TestClass();
            assertThatThrownBy(() -> ReflectUtil.getFieldValue(obj, "nonExistent"))
                    .isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("getStaticFieldValue")
        void testGetStaticFieldValue() {
            String value = ReflectUtil.getStaticFieldValue(TestClass.class, "staticField");
            assertThat(value).isEqualTo("static");
        }

        @Test
        @DisplayName("setStaticFieldValue")
        void testSetStaticFieldValue() {
            ReflectUtil.setStaticFieldValue(TestClass.class, "staticField", "newValue");
            String value = ReflectUtil.getStaticFieldValue(TestClass.class, "staticField");
            assertThat(value).isEqualTo("newValue");
            // 恢复原值
            ReflectUtil.setStaticFieldValue(TestClass.class, "staticField", "static");
        }
    }

    @Nested
    @DisplayName("获取成员测试")
    class GetMemberTests {

        @Test
        @DisplayName("getFields 包括父类")
        void testGetFieldsWithInheritance() {
            Field[] fields = ReflectUtil.getFields(ChildClass.class);
            assertThat(fields.length).isGreaterThanOrEqualTo(3);

            boolean hasName = false, hasAge = false, hasChildField = false;
            for (Field field : fields) {
                if (field.getName().equals("name")) hasName = true;
                if (field.getName().equals("age")) hasAge = true;
                if (field.getName().equals("childField")) hasChildField = true;
            }
            assertThat(hasName).isTrue();
            assertThat(hasAge).isTrue();
            assertThat(hasChildField).isTrue();
        }

        @Test
        @DisplayName("getField")
        void testGetField() {
            Field field = ReflectUtil.getField(TestClass.class, "name");
            assertThat(field).isNotNull();
            assertThat(field.getName()).isEqualTo("name");
        }

        @Test
        @DisplayName("getField 不存在返回 null")
        void testGetFieldNotFound() {
            Field field = ReflectUtil.getField(TestClass.class, "nonExistent");
            assertThat(field).isNull();
        }

        @Test
        @DisplayName("getMethods 包括父类")
        void testGetMethodsWithInheritance() {
            Method[] methods = ReflectUtil.getMethods(ChildClass.class);
            assertThat(methods.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("getMethod")
        void testGetMethod() {
            Method method = ReflectUtil.getMethod(TestClass.class, "getName");
            assertThat(method).isNotNull();
            assertThat(method.getName()).isEqualTo("getName");
        }

        @Test
        @DisplayName("getMethod 带参数类型")
        void testGetMethodWithParamTypes() {
            Method method = ReflectUtil.getMethod(TestClass.class, "setName", String.class);
            assertThat(method).isNotNull();
        }

        @Test
        @DisplayName("getConstructors")
        void testGetConstructors() {
            Constructor<?>[] constructors = ReflectUtil.getConstructors(TestClass.class);
            assertThat(constructors).hasSize(3);
        }

        @Test
        @DisplayName("getDefaultConstructor")
        void testGetDefaultConstructor() {
            Constructor<TestClass> constructor = ReflectUtil.getDefaultConstructor(TestClass.class);
            assertThat(constructor).isNotNull();
            assertThat(constructor.getParameterCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("getDefaultConstructor 无默认构造返回 null")
        void testGetDefaultConstructorNotFound() {
            Constructor<Integer> constructor = ReflectUtil.getDefaultConstructor(Integer.class);
            assertThat(constructor).isNull();
        }
    }

    @Nested
    @DisplayName("缓存测试")
    class CachingTests {

        @Test
        @DisplayName("getFields 返回防御性拷贝，内容相同但不同引用")
        void testFieldCaching() {
            Field[] fields1 = ReflectUtil.getFields(TestClass.class);
            Field[] fields2 = ReflectUtil.getFields(TestClass.class);
            // Defensive copy: different array instances with same content
            assertThat(fields1).isNotSameAs(fields2);
            assertThat(fields1).containsExactly(fields2);
        }

        @Test
        @DisplayName("getMethods 返回防御性拷贝，内容相同但不同引用")
        void testMethodCaching() {
            Method[] methods1 = ReflectUtil.getMethods(TestClass.class);
            Method[] methods2 = ReflectUtil.getMethods(TestClass.class);
            // Defensive copy: different array instances with same content
            assertThat(methods1).isNotSameAs(methods2);
            assertThat(methods1).containsExactly(methods2);
        }

        @Test
        @DisplayName("getConstructors 返回防御性拷贝，内容相同但不同引用")
        void testConstructorCaching() {
            Constructor<?>[] ctors1 = ReflectUtil.getConstructors(TestClass.class);
            Constructor<?>[] ctors2 = ReflectUtil.getConstructors(TestClass.class);
            // Defensive copy: different array instances with same content
            assertThat(ctors1).isNotSameAs(ctors2);
            assertThat(ctors1).containsExactly(ctors2);
        }

        @Test
        @DisplayName("getFields 修改返回数组不影响缓存")
        void testFieldDefensiveCopy() {
            Field[] fields = ReflectUtil.getFields(TestClass.class);
            int originalLength = fields.length;
            fields[0] = null; // mutate the returned array
            Field[] freshFields = ReflectUtil.getFields(TestClass.class);
            // The cached data is unaffected by the mutation
            assertThat(freshFields).hasSize(originalLength);
            assertThat(freshFields[0]).isNotNull();
        }

        @Test
        @DisplayName("getMethods 修改返回数组不影响缓存")
        void testMethodDefensiveCopy() {
            Method[] methods = ReflectUtil.getMethods(TestClass.class);
            int originalLength = methods.length;
            methods[0] = null; // mutate the returned array
            Method[] freshMethods = ReflectUtil.getMethods(TestClass.class);
            // The cached data is unaffected by the mutation
            assertThat(freshMethods).hasSize(originalLength);
            assertThat(freshMethods[0]).isNotNull();
        }

        @Test
        @DisplayName("getConstructors 修改返回数组不影响缓存")
        void testConstructorDefensiveCopy() {
            Constructor<?>[] ctors = ReflectUtil.getConstructors(TestClass.class);
            int originalLength = ctors.length;
            ctors[0] = null; // mutate the returned array
            Constructor<?>[] freshCtors = ReflectUtil.getConstructors(TestClass.class);
            // The cached data is unaffected by the mutation
            assertThat(freshCtors).hasSize(originalLength);
            assertThat(freshCtors[0]).isNotNull();
        }
    }
}
