package cloud.opencode.base.expression.context;

import cloud.opencode.base.expression.function.FunctionRegistry;
import cloud.opencode.base.expression.sandbox.DefaultSandbox;
import cloud.opencode.base.expression.spi.PropertyAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * StandardContext Tests
 * StandardContext 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("StandardContext Tests | StandardContext 测试")
class StandardContextTest {

    private StandardContext context;

    @BeforeEach
    void setup() {
        context = new StandardContext();
    }

    @Nested
    @DisplayName("Constructor Tests | 构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor | 默认构造函数")
        void testDefaultConstructor() {
            StandardContext ctx = new StandardContext();
            assertThat(ctx.getRootObject()).isNull();
            assertThat(ctx.getFunctionRegistry()).isNotNull();
            assertThat(ctx.getPropertyAccessors()).isNotEmpty();
        }

        @Test
        @DisplayName("constructor with root object | 带根对象的构造函数")
        void testConstructorWithRoot() {
            Object root = Map.of("key", "value");
            StandardContext ctx = new StandardContext(root);
            assertThat(ctx.getRootObject()).isSameAs(root);
        }

        @Test
        @DisplayName("full constructor | 完整构造函数")
        void testFullConstructor() {
            FunctionRegistry registry = FunctionRegistry.create();
            PropertyAccessor accessor = new PropertyAccessor() {
                @Override
                public Class<?>[] getSpecificTargetClasses() { return null; }
                @Override
                public boolean canRead(Object target, String name) { return false; }
                @Override
                public Object read(Object target, String name) { return null; }
            };
            DefaultSandbox sandbox = DefaultSandbox.permissive();

            StandardContext ctx = new StandardContext(
                    "root", registry, List.of(accessor), null, sandbox, null);
            assertThat(ctx.getRootObject()).isEqualTo("root");
            assertThat(ctx.getFunctionRegistry()).isSameAs(registry);
            assertThat(ctx.getSandbox()).isSameAs(sandbox);
        }
    }

    @Nested
    @DisplayName("Root Object Tests | 根对象测试")
    class RootObjectTests {

        @Test
        @DisplayName("get and set root object | 获取和设置根对象")
        void testGetSetRootObject() {
            assertThat(context.getRootObject()).isNull();
            context.setRootObject("newRoot");
            assertThat(context.getRootObject()).isEqualTo("newRoot");
        }
    }

    @Nested
    @DisplayName("Variable Tests | 变量测试")
    class VariableTests {

        @Test
        @DisplayName("set and get variable | 设置和获取变量")
        void testSetGetVariable() {
            context.setVariable("x", 10);
            assertThat(context.getVariable("x")).isEqualTo(10);
        }

        @Test
        @DisplayName("getVariable returns null for missing | getVariable 对缺失项返回 null")
        void testGetVariableMissing() {
            assertThat(context.getVariable("missing")).isNull();
        }

        @Test
        @DisplayName("getVariable returns null for null name | getVariable 对 null 名称返回 null")
        void testGetVariableNullName() {
            assertThat(context.getVariable(null)).isNull();
        }

        @Test
        @DisplayName("setVariable ignores null name | setVariable 忽略 null 名称")
        void testSetVariableNullName() {
            context.setVariable(null, "value");
            assertThat(context.getVariables()).isEmpty();
        }

        @Test
        @DisplayName("hasVariable returns true for existing | hasVariable 对存在的返回 true")
        void testHasVariableTrue() {
            context.setVariable("x", 1);
            assertThat(context.hasVariable("x")).isTrue();
        }

        @Test
        @DisplayName("hasVariable returns false for missing | hasVariable 对缺失的返回 false")
        void testHasVariableFalse() {
            assertThat(context.hasVariable("missing")).isFalse();
        }

        @Test
        @DisplayName("hasVariable returns false for null | hasVariable 对 null 返回 false")
        void testHasVariableNull() {
            assertThat(context.hasVariable(null)).isFalse();
        }

        @Test
        @DisplayName("getVariables returns all variables | getVariables 返回所有变量")
        void testGetVariables() {
            context.setVariable("a", 1);
            context.setVariable("b", 2);
            Map<String, Object> vars = context.getVariables();
            assertThat(vars).containsEntry("a", 1).containsEntry("b", 2);
        }
    }

    @Nested
    @DisplayName("Registry Tests | 注册表测试")
    class RegistryTests {

        @Test
        @DisplayName("getFunctionRegistry returns registry | getFunctionRegistry 返回注册表")
        void testGetFunctionRegistry() {
            assertThat(context.getFunctionRegistry()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Property Accessor Tests | 属性访问器测试")
    class PropertyAccessorTests {

        @Test
        @DisplayName("getPropertyAccessors returns list | getPropertyAccessors 返回列表")
        void testGetPropertyAccessors() {
            assertThat(context.getPropertyAccessors()).isNotEmpty();
        }

        @Test
        @DisplayName("addPropertyAccessor adds accessor | addPropertyAccessor 添加访问器")
        void testAddPropertyAccessor() {
            int initialSize = context.getPropertyAccessors().size();
            PropertyAccessor accessor = new PropertyAccessor() {
                @Override
                public Class<?>[] getSpecificTargetClasses() { return null; }
                @Override
                public boolean canRead(Object target, String name) { return false; }
                @Override
                public Object read(Object target, String name) { return null; }
            };
            context.addPropertyAccessor(accessor);
            assertThat(context.getPropertyAccessors()).hasSize(initialSize + 1);
        }

        @Test
        @DisplayName("addPropertyAccessor ignores null | addPropertyAccessor 忽略 null")
        void testAddPropertyAccessorNull() {
            int initialSize = context.getPropertyAccessors().size();
            context.addPropertyAccessor(null);
            assertThat(context.getPropertyAccessors()).hasSize(initialSize);
        }
    }

    @Nested
    @DisplayName("Type Converter Tests | 类型转换器测试")
    class TypeConverterTests {

        @Test
        @DisplayName("getTypeConverter returns null by default | getTypeConverter 默认返回 null")
        void testGetTypeConverter() {
            assertThat(context.getTypeConverter()).isNull();
        }
    }

    @Nested
    @DisplayName("Sandbox Tests | 沙箱测试")
    class SandboxTests {

        @Test
        @DisplayName("getSandbox returns null by default | getSandbox 默认返回 null")
        void testGetSandbox() {
            assertThat(context.getSandbox()).isNull();
        }
    }

    @Nested
    @DisplayName("Child Context Tests | 子上下文测试")
    class ChildContextTests {

        @Test
        @DisplayName("createChild creates child context | createChild 创建子上下文")
        void testCreateChild() {
            context.setVariable("parent_var", "parent_value");
            EvaluationContext child = context.createChild();
            assertThat(child).isNotNull();
            assertThat(child.getVariable("parent_var")).isEqualTo("parent_value");
        }

        @Test
        @DisplayName("child can override parent variable | 子上下文可以覆盖父变量")
        void testChildOverrideVariable() {
            context.setVariable("x", 1);
            EvaluationContext child = context.createChild();
            child.setVariable("x", 2);
            assertThat(child.getVariable("x")).isEqualTo(2);
            assertThat(context.getVariable("x")).isEqualTo(1);
        }

        @Test
        @DisplayName("child hasVariable checks parent | 子上下文 hasVariable 检查父上下文")
        void testChildHasVariableFromParent() {
            context.setVariable("parent_only", "value");
            EvaluationContext child = context.createChild();
            assertThat(child.hasVariable("parent_only")).isTrue();
        }

        @Test
        @DisplayName("child getVariables includes parent | 子上下文 getVariables 包含父变量")
        void testChildGetVariablesIncludesParent() {
            context.setVariable("a", 1);
            EvaluationContext child = context.createChild();
            child.setVariable("b", 2);
            assertThat(child.getVariables()).containsEntry("a", 1).containsEntry("b", 2);
        }
    }

    @Nested
    @DisplayName("Builder Tests | 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("builder creates context | builder 创建上下文")
        void testBuilder() {
            StandardContext ctx = StandardContext.builder()
                    .rootObject("root")
                    .build();
            assertThat(ctx.getRootObject()).isEqualTo("root");
        }

        @Test
        @DisplayName("builder with all options | builder 所有选项")
        void testBuilderAllOptions() {
            FunctionRegistry registry = FunctionRegistry.create();
            DefaultSandbox sandbox = DefaultSandbox.permissive();

            StandardContext ctx = StandardContext.builder()
                    .rootObject("root")
                    .functionRegistry(registry)
                    .sandbox(sandbox)
                    .build();

            assertThat(ctx.getRootObject()).isEqualTo("root");
            assertThat(ctx.getFunctionRegistry()).isSameAs(registry);
            assertThat(ctx.getSandbox()).isSameAs(sandbox);
        }

        @Test
        @DisplayName("builder addPropertyAccessor | builder 添加属性访问器")
        void testBuilderAddPropertyAccessor() {
            PropertyAccessor accessor = new PropertyAccessor() {
                @Override
                public Class<?>[] getSpecificTargetClasses() { return null; }
                @Override
                public boolean canRead(Object target, String name) { return false; }
                @Override
                public Object read(Object target, String name) { return null; }
            };

            StandardContext ctx = StandardContext.builder()
                    .addPropertyAccessor(accessor)
                    .build();

            assertThat(ctx.getPropertyAccessors()).contains(accessor);
        }
    }
}
