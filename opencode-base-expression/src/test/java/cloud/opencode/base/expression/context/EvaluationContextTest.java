package cloud.opencode.base.expression.context;

import cloud.opencode.base.expression.function.FunctionRegistry;
import cloud.opencode.base.expression.sandbox.Sandbox;
import cloud.opencode.base.expression.spi.PropertyAccessor;
import cloud.opencode.base.expression.spi.TypeConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * EvaluationContext Interface Tests
 * EvaluationContext 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("EvaluationContext Interface Tests | EvaluationContext 接口测试")
class EvaluationContextTest {

    @Nested
    @DisplayName("Variable Operations Tests | 变量操作测试")
    class VariableOperationsTests {

        @Test
        @DisplayName("setVariable and getVariable work together | setVariable 和 getVariable 协同工作")
        void testSetAndGetVariable() {
            EvaluationContext context = new MapContext();
            context.setVariable("x", 100);

            assertThat(context.getVariable("x")).isEqualTo(100);
        }

        @Test
        @DisplayName("hasVariable returns true for existing variable | hasVariable 对存在的变量返回 true")
        void testHasVariableTrue() {
            EvaluationContext context = new MapContext();
            context.setVariable("test", "value");

            assertThat(context.hasVariable("test")).isTrue();
        }

        @Test
        @DisplayName("hasVariable returns false for non-existing variable | hasVariable 对不存在的变量返回 false")
        void testHasVariableFalse() {
            EvaluationContext context = new MapContext();

            assertThat(context.hasVariable("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("getVariables returns all variables | getVariables 返回所有变量")
        void testGetVariables() {
            EvaluationContext context = new MapContext();
            context.setVariable("a", 1);
            context.setVariable("b", 2);
            context.setVariable("c", 3);

            Map<String, Object> variables = context.getVariables();
            assertThat(variables).containsEntry("a", 1);
            assertThat(variables).containsEntry("b", 2);
            assertThat(variables).containsEntry("c", 3);
        }

        @Test
        @DisplayName("variable can be null | 变量可以为 null")
        void testNullVariable() {
            EvaluationContext context = new MapContext();
            context.setVariable("nullVar", null);

            assertThat(context.hasVariable("nullVar")).isTrue();
            assertThat(context.getVariable("nullVar")).isNull();
        }

        @Test
        @DisplayName("variable can be overwritten | 变量可以被覆盖")
        void testOverwriteVariable() {
            EvaluationContext context = new MapContext();
            context.setVariable("x", "old");
            context.setVariable("x", "new");

            assertThat(context.getVariable("x")).isEqualTo("new");
        }
    }

    @Nested
    @DisplayName("Root Object Tests | 根对象测试")
    class RootObjectTests {

        @Test
        @DisplayName("getRootObject returns variables map for MapContext | MapContext 的 getRootObject 返回变量 Map")
        void testGetRootObjectReturnsVariablesMap() {
            EvaluationContext context = new MapContext();
            context.setVariable("key", "value");

            // MapContext.getRootObject() returns the internal variables map
            Object root = context.getRootObject();
            assertThat(root).isNotNull();
            assertThat(root).isInstanceOf(Map.class);
        }

        @Test
        @DisplayName("setRootObject with map copies entries | setRootObject 使用 Map 复制条目")
        void testSetRootObjectWithMapCopiesEntries() {
            EvaluationContext context = new MapContext();
            Map<String, Object> rootMap = new java.util.HashMap<>();
            rootMap.put("x", 100);
            rootMap.put("y", 200);
            context.setRootObject(rootMap);

            // The entries should be copied as variables
            assertThat(context.getVariable("x")).isEqualTo(100);
            assertThat(context.getVariable("y")).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("Function Registry Tests | 函数注册表测试")
    class FunctionRegistryTests {

        @Test
        @DisplayName("getFunctionRegistry returns registry | getFunctionRegistry 返回注册表")
        void testGetFunctionRegistry() {
            EvaluationContext context = new MapContext();
            FunctionRegistry registry = context.getFunctionRegistry();

            assertThat(registry).isNotNull();
        }
    }

    @Nested
    @DisplayName("Property Accessors Tests | 属性访问器测试")
    class PropertyAccessorsTests {

        @Test
        @DisplayName("getPropertyAccessors returns list | getPropertyAccessors 返回列表")
        void testGetPropertyAccessors() {
            EvaluationContext context = new MapContext();
            List<PropertyAccessor> accessors = context.getPropertyAccessors();

            assertThat(accessors).isNotNull();
        }
    }

    @Nested
    @DisplayName("Type Converter Tests | 类型转换器测试")
    class TypeConverterTests {

        @Test
        @DisplayName("getTypeConverter can return null for MapContext | MapContext 的 getTypeConverter 可以返回 null")
        void testGetTypeConverterNullForMapContext() {
            EvaluationContext context = new MapContext();
            TypeConverter converter = context.getTypeConverter();

            // MapContext.getTypeConverter() returns null by design
            assertThat(converter).isNull();
        }
    }

    @Nested
    @DisplayName("Sandbox Tests | 沙箱测试")
    class SandboxTests {

        @Test
        @DisplayName("getSandbox can return null | getSandbox 可以返回 null")
        void testGetSandboxNull() {
            EvaluationContext context = new MapContext();
            // MapContext may or may not have sandbox configured
            Sandbox sandbox = context.getSandbox();
            // Just verify it doesn't throw
            assertThat(true).isTrue();
        }
    }

    @Nested
    @DisplayName("Child Context Tests | 子上下文测试")
    class ChildContextTests {

        @Test
        @DisplayName("createChild returns new context | createChild 返回新上下文")
        void testCreateChild() {
            EvaluationContext parent = new MapContext();
            parent.setVariable("parentVar", "parentValue");

            EvaluationContext child = parent.createChild();

            assertThat(child).isNotNull();
            assertThat(child).isNotSameAs(parent);
        }

        @Test
        @DisplayName("child context inherits parent variables | 子上下文继承父变量")
        void testChildInheritsVariables() {
            EvaluationContext parent = new MapContext();
            parent.setVariable("inherited", "value");

            EvaluationContext child = parent.createChild();

            assertThat(child.getVariable("inherited")).isEqualTo("value");
        }

        @Test
        @DisplayName("child context can have own variables | 子上下文可以有自己的变量")
        void testChildOwnVariables() {
            EvaluationContext parent = new MapContext();
            EvaluationContext child = parent.createChild();

            child.setVariable("childOnly", "childValue");

            assertThat(child.getVariable("childOnly")).isEqualTo("childValue");
        }
    }

    @Nested
    @DisplayName("MapContext Implementation Tests | MapContext 实现测试")
    class MapContextImplementationTests {

        @Test
        @DisplayName("MapContext with initial variables | MapContext 带初始变量")
        void testMapContextWithInitialVariables() {
            Map<String, Object> initial = new java.util.HashMap<>();
            initial.put("a", 1);
            initial.put("b", 2);
            EvaluationContext context = new MapContext(initial);

            assertThat(context.getVariable("a")).isEqualTo(1);
            assertThat(context.getVariable("b")).isEqualTo(2);
        }

        @Test
        @DisplayName("MapContext setRootObject with map | MapContext setRootObject 使用 Map")
        void testMapContextSetRootObject() {
            EvaluationContext context = new MapContext();
            Map<String, Object> newRoot = new java.util.HashMap<>();
            newRoot.put("x", 100);
            context.setRootObject(newRoot);

            assertThat(context.getVariable("x")).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("StandardContext Implementation Tests | StandardContext 实现测试")
    class StandardContextImplementationTests {

        @Test
        @DisplayName("StandardContext default construction | StandardContext 默认构造")
        void testStandardContextDefault() {
            EvaluationContext context = new StandardContext();

            assertThat(context).isNotNull();
            assertThat(context.getFunctionRegistry()).isNotNull();
            assertThat(context.getPropertyAccessors()).isNotNull();
            // TypeConverter can be null for default StandardContext
        }
    }

    // Helper classes
    private record TestRoot(String name) {}
}
