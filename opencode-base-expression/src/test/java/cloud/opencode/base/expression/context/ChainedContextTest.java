package cloud.opencode.base.expression.context;

import cloud.opencode.base.expression.OpenExpression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ChainedContext Tests
 * ChainedContext 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("ChainedContext Tests | ChainedContext 测试")
class ChainedContextTest {

    @Nested
    @DisplayName("Constructor Tests | 构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("Create with parent | 使用父上下文创建")
        void testParentConstructor() {
            StandardContext parent = new StandardContext();
            parent.setVariable("x", 10);

            ChainedContext child = new ChainedContext(parent);
            assertThat(child.getParent()).isEqualTo(parent);
        }

        @Test
        @DisplayName("Null parent throws exception | null 父上下文抛异常")
        void testNullParentThrows() {
            assertThatThrownBy(() -> new ChainedContext(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Variable Inheritance Tests | 变量继承测试")
    class VariableInheritanceTests {

        @Test
        @DisplayName("Inherit parent variables | 继承父变量")
        void testInheritParentVariables() {
            StandardContext parent = new StandardContext();
            parent.setVariable("parentVar", "parentValue");

            ChainedContext child = ChainedContext.of(parent);
            assertThat(child.getVariable("parentVar")).isEqualTo("parentValue");
        }

        @Test
        @DisplayName("Local variables shadow parent | 本地变量覆盖父变量")
        void testLocalShadowsParent() {
            StandardContext parent = new StandardContext();
            parent.setVariable("x", 10);

            ChainedContext child = ChainedContext.of(parent);
            child.setVariable("x", 100);

            assertThat(child.getVariable("x")).isEqualTo(100);
            assertThat(parent.getVariable("x")).isEqualTo(10);
        }

        @Test
        @DisplayName("Has variable checks both | hasVariable 检查两者")
        void testHasVariableChecksBoth() {
            StandardContext parent = new StandardContext();
            parent.setVariable("parentOnly", "value");

            ChainedContext child = ChainedContext.of(parent);
            child.setVariable("childOnly", "value");

            assertThat(child.hasVariable("parentOnly")).isTrue();
            assertThat(child.hasVariable("childOnly")).isTrue();
            assertThat(child.hasVariable("missing")).isFalse();
        }

        @Test
        @DisplayName("Get all variables includes both | 获取所有变量包含两者")
        void testGetAllVariables() {
            StandardContext parent = new StandardContext();
            parent.setVariable("a", 1);

            ChainedContext child = ChainedContext.of(parent);
            child.setVariable("b", 2);

            assertThat(child.getVariables())
                    .containsEntry("a", 1)
                    .containsEntry("b", 2);
        }
    }

    @Nested
    @DisplayName("Depth Tests | 深度测试")
    class DepthTests {

        @Test
        @DisplayName("Single level depth | 单层深度")
        void testSingleLevelDepth() {
            StandardContext parent = new StandardContext();
            ChainedContext child = ChainedContext.of(parent);
            assertThat(child.getDepth()).isEqualTo(2);
        }

        @Test
        @DisplayName("Multi level depth | 多层深度")
        void testMultiLevelDepth() {
            StandardContext root = new StandardContext();
            ChainedContext level1 = ChainedContext.of(root);
            ChainedContext level2 = ChainedContext.of(level1);
            ChainedContext level3 = ChainedContext.of(level2);

            assertThat(level3.getDepth()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Child Context Tests | 子上下文测试")
    class ChildContextTests {

        @Test
        @DisplayName("Create child creates chained | 创建子上下文创建链式")
        void testCreateChildCreatesChained() {
            StandardContext parent = new StandardContext();
            ChainedContext child = ChainedContext.of(parent);
            EvaluationContext grandchild = child.createChild();

            assertThat(grandchild).isInstanceOf(ChainedContext.class);
        }
    }

    @Nested
    @DisplayName("Builder Tests | 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("Build with variables | 使用变量构建")
        void testBuilderWithVariables() {
            StandardContext parent = new StandardContext();
            parent.setVariable("parentVar", "parent");

            ChainedContext ctx = ChainedContext.builder(parent)
                    .variable("childVar", "child")
                    .build();

            assertThat(ctx.getVariable("parentVar")).isEqualTo("parent");
            assertThat(ctx.getVariable("childVar")).isEqualTo("child");
        }

        @Test
        @DisplayName("Build with root object | 使用根对象构建")
        void testBuilderWithRootObject() {
            StandardContext parent = new StandardContext();
            Object root = new Object();

            ChainedContext ctx = ChainedContext.builder(parent)
                    .rootObject(root)
                    .build();

            assertThat(ctx.getRootObject()).isEqualTo(root);
        }
    }

    @Nested
    @DisplayName("Integration Tests | 集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("Use with OpenExpression | 与 OpenExpression 配合使用")
        void testWithOpenExpression() {
            StandardContext parent = new StandardContext();
            parent.setVariable("x", 10);

            ChainedContext child = ChainedContext.of(parent);
            child.setVariable("y", 5);

            Object result = OpenExpression.eval("x + y", child);
            assertThat(result).isEqualTo(15);
        }

        @Test
        @DisplayName("Get local variables only | 仅获取本地变量")
        void testGetLocalVariablesOnly() {
            StandardContext parent = new StandardContext();
            parent.setVariable("parentVar", "parent");

            ChainedContext child = ChainedContext.of(parent);
            child.setVariable("childVar", "child");

            assertThat(child.getLocalVariables())
                    .containsEntry("childVar", "child")
                    .doesNotContainKey("parentVar");
        }
    }
}
