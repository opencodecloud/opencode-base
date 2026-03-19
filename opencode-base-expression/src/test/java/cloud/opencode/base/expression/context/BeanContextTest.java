package cloud.opencode.base.expression.context;

import cloud.opencode.base.expression.OpenExpression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * BeanContext Tests
 * BeanContext 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("BeanContext Tests | BeanContext 测试")
class BeanContextTest {

    // Test bean class
    public record User(String name, int age) {}

    public static class Person {
        private String firstName;
        private String lastName;
        private int age;

        public Person(String firstName, String lastName, int age) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
        }

        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public int getAge() { return age; }
        public String getFullName() { return firstName + " " + lastName; }
    }

    @Nested
    @DisplayName("Constructor Tests | 构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("Create with bean | 使用 Bean 创建")
        void testBeanConstructor() {
            User user = new User("John", 30);
            BeanContext ctx = new BeanContext(user);
            assertThat(ctx.getRootObject()).isEqualTo(user);
        }

        @Test
        @DisplayName("Create with null bean | 使用 null Bean 创建")
        void testNullBeanConstructor() {
            BeanContext ctx = new BeanContext(null);
            assertThat(ctx.getRootObject()).isNull();
        }
    }

    @Nested
    @DisplayName("Variable Operations Tests | 变量操作测试")
    class VariableOperationsTests {

        @Test
        @DisplayName("Set and get variable | 设置和获取变量")
        void testSetAndGetVariable() {
            BeanContext ctx = new BeanContext(null);
            ctx.setVariable("x", 100);
            assertThat(ctx.getVariable("x")).isEqualTo(100);
        }

        @Test
        @DisplayName("Has variable | 检查变量存在")
        void testHasVariable() {
            BeanContext ctx = new BeanContext(null);
            ctx.setVariable("test", "value");
            assertThat(ctx.hasVariable("test")).isTrue();
            assertThat(ctx.hasVariable("missing")).isFalse();
        }
    }

    @Nested
    @DisplayName("Child Context Tests | 子上下文测试")
    class ChildContextTests {

        @Test
        @DisplayName("Create child context | 创建子上下文")
        void testCreateChild() {
            User user = new User("Alice", 25);
            BeanContext parent = new BeanContext(user);
            parent.setVariable("x", 10);

            EvaluationContext child = parent.createChild();
            assertThat(child.getRootObject()).isEqualTo(user);
            assertThat(child.getVariable("x")).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Builder Tests | 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("Build with root object | 使用根对象构建")
        void testBuilderWithRootObject() {
            User user = new User("Bob", 35);
            BeanContext ctx = BeanContext.builder()
                    .rootObject(user)
                    .build();

            assertThat(ctx.getRootObject()).isEqualTo(user);
        }

        @Test
        @DisplayName("Build with variables | 使用变量构建")
        void testBuilderWithVariables() {
            BeanContext ctx = BeanContext.builder()
                    .variable("a", 1)
                    .variable("b", 2)
                    .build();

            assertThat(ctx.getVariable("a")).isEqualTo(1);
            assertThat(ctx.getVariable("b")).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Integration Tests | 集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("Access record properties | 访问 Record 属性")
        void testRecordPropertyAccess() {
            User user = new User("Charlie", 28);
            BeanContext ctx = BeanContext.of(user);
            Object name = OpenExpression.eval("name", ctx);
            Object age = OpenExpression.eval("age", ctx);
            assertThat(name).isEqualTo("Charlie");
            assertThat(age).isEqualTo(28);
        }

        @Test
        @DisplayName("Access class properties | 访问类属性")
        void testClassPropertyAccess() {
            Person person = new Person("John", "Doe", 40);
            BeanContext ctx = BeanContext.of(person);
            Object firstName = OpenExpression.eval("firstName", ctx);
            Object lastName = OpenExpression.eval("lastName", ctx);
            assertThat(firstName).isEqualTo("John");
            assertThat(lastName).isEqualTo("Doe");
        }

        @Test
        @DisplayName("Static factory method | 静态工厂方法")
        void testOfMethod() {
            User user = new User("Test", 20);
            BeanContext ctx = BeanContext.of(user);
            assertThat(ctx.getRootObject()).isEqualTo(user);
        }
    }
}
