package cloud.opencode.base.reflect.accessor;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * PropertyAccessorsTest Tests
 * PropertyAccessorsTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("PropertyAccessors 测试")
class PropertyAccessorsTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = PropertyAccessors.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("Strategy枚举测试")
    class StrategyEnumTests {

        @Test
        @DisplayName("所有策略值")
        void testStrategyValues() {
            assertThat(PropertyAccessors.Strategy.values()).containsExactly(
                    PropertyAccessors.Strategy.FIELD,
                    PropertyAccessors.Strategy.BEAN,
                    PropertyAccessors.Strategy.METHOD_HANDLE,
                    PropertyAccessors.Strategy.VAR_HANDLE,
                    PropertyAccessors.Strategy.AUTO
            );
        }
    }

    @Nested
    @DisplayName("create方法测试")
    class CreateTests {

        @Test
        @DisplayName("自动策略")
        void testCreateAuto() {
            PropertyAccessor<TestBean> accessor = PropertyAccessors.create(TestBean.class, "name");
            assertThat(accessor).isNotNull();
        }

        @Test
        @DisplayName("FIELD策略")
        void testCreateField() {
            PropertyAccessor<TestBean> accessor = PropertyAccessors.create(
                    TestBean.class, "name", PropertyAccessors.Strategy.FIELD);
            assertThat(accessor).isInstanceOf(FieldAccessor.class);
        }

        @Test
        @DisplayName("BEAN策略")
        void testCreateBean() {
            PropertyAccessor<TestBean> accessor = PropertyAccessors.create(
                    TestBean.class, "name", PropertyAccessors.Strategy.BEAN);
            assertThat(accessor).isInstanceOf(BeanAccessor.class);
        }

        @Test
        @DisplayName("METHOD_HANDLE策略")
        void testCreateMethodHandle() {
            PropertyAccessor<TestBean> accessor = PropertyAccessors.create(
                    TestBean.class, "name", PropertyAccessors.Strategy.METHOD_HANDLE);
            assertThat(accessor).isInstanceOf(MethodHandleAccessor.class);
        }

        @Test
        @DisplayName("VAR_HANDLE策略")
        void testCreateVarHandle() {
            PropertyAccessor<TestBean> accessor = PropertyAccessors.create(
                    TestBean.class, "name", PropertyAccessors.Strategy.VAR_HANDLE);
            assertThat(accessor).isInstanceOf(VarHandleAccessor.class);
        }
    }

    @Nested
    @DisplayName("createAll方法测试")
    class CreateAllTests {

        @Test
        @DisplayName("创建所有属性访问器")
        void testCreateAll() {
            Map<String, PropertyAccessor<TestBean>> accessors = PropertyAccessors.createAll(TestBean.class);
            assertThat(accessors).isNotEmpty();
            assertThat(accessors).containsKey("name");
        }

        @Test
        @DisplayName("指定策略创建所有访问器")
        void testCreateAllWithStrategy() {
            Map<String, PropertyAccessor<TestBean>> accessors = PropertyAccessors.createAll(
                    TestBean.class, PropertyAccessors.Strategy.FIELD);
            assertThat(accessors).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("createFieldAccessors方法测试")
    class CreateFieldAccessorsTests {

        @Test
        @DisplayName("创建所有字段访问器")
        void testCreateFieldAccessors() {
            Map<String, FieldAccessor<TestBean>> accessors = PropertyAccessors.createFieldAccessors(TestBean.class);
            assertThat(accessors).isNotEmpty();
            assertThat(accessors).containsKey("name");
            assertThat(accessors.get("name")).isInstanceOf(FieldAccessor.class);
        }

        @Test
        @DisplayName("排除静态字段")
        void testCreateFieldAccessorsExcludesStatic() {
            Map<String, FieldAccessor<TestBean>> accessors = PropertyAccessors.createFieldAccessors(TestBean.class);
            assertThat(accessors).doesNotContainKey("staticField");
        }
    }

    @Nested
    @DisplayName("createBeanAccessors方法测试")
    class CreateBeanAccessorsTests {

        @Test
        @DisplayName("创建所有bean访问器")
        void testCreateBeanAccessors() {
            Map<String, BeanAccessor<TestBean>> accessors = PropertyAccessors.createBeanAccessors(TestBean.class);
            assertThat(accessors).isNotEmpty();
            assertThat(accessors).containsKey("name");
            assertThat(accessors.get("name")).isInstanceOf(BeanAccessor.class);
        }
    }

    @Nested
    @DisplayName("createVarHandleAccessors方法测试")
    class CreateVarHandleAccessorsTests {

        @Test
        @DisplayName("创建所有VarHandle访问器")
        void testCreateVarHandleAccessors() {
            Map<String, VarHandleAccessor<TestBean>> accessors = PropertyAccessors.createVarHandleAccessors(TestBean.class);
            assertThat(accessors).isNotEmpty();
            assertThat(accessors).containsKey("name");
            assertThat(accessors.get("name")).isInstanceOf(VarHandleAccessor.class);
        }

        @Test
        @DisplayName("排除静态字段")
        void testCreateVarHandleAccessorsExcludesStatic() {
            Map<String, VarHandleAccessor<TestBean>> accessors = PropertyAccessors.createVarHandleAccessors(TestBean.class);
            assertThat(accessors).doesNotContainKey("staticField");
        }
    }

    // Test helper class
    public static class TestBean {
        private String name;
        private int age;
        public static String staticField;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
}
