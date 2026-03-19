package cloud.opencode.base.expression.context;

import cloud.opencode.base.expression.OpenExpression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * MapContext Tests
 * MapContext 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("MapContext Tests | MapContext 测试")
class MapContextTest {

    @Nested
    @DisplayName("Constructor Tests | 构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("Create empty context | 创建空上下文")
        void testEmptyConstructor() {
            MapContext ctx = new MapContext();
            assertThat(ctx.getVariables()).isEmpty();
            assertThat(ctx.getRootObject()).isInstanceOf(Map.class);
        }

        @Test
        @DisplayName("Create with map | 使用 Map 创建")
        void testMapConstructor() {
            Map<String, Object> vars = Map.of("x", 10, "y", 20);
            MapContext ctx = new MapContext(vars);
            assertThat(ctx.getVariable("x")).isEqualTo(10);
            assertThat(ctx.getVariable("y")).isEqualTo(20);
        }

        @Test
        @DisplayName("Create with null map | 使用 null Map 创建")
        void testNullMapConstructor() {
            MapContext ctx = new MapContext(null);
            assertThat(ctx.getVariables()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Variable Operations Tests | 变量操作测试")
    class VariableOperationsTests {

        @Test
        @DisplayName("Set and get variable | 设置和获取变量")
        void testSetAndGetVariable() {
            MapContext ctx = new MapContext();
            ctx.setVariable("name", "John");
            assertThat(ctx.getVariable("name")).isEqualTo("John");
        }

        @Test
        @DisplayName("Has variable | 检查变量存在")
        void testHasVariable() {
            MapContext ctx = new MapContext();
            ctx.setVariable("x", 10);
            assertThat(ctx.hasVariable("x")).isTrue();
            assertThat(ctx.hasVariable("y")).isFalse();
        }

        @Test
        @DisplayName("Get non-existent variable | 获取不存在的变量")
        void testGetNonExistentVariable() {
            MapContext ctx = new MapContext();
            assertThat(ctx.getVariable("missing")).isNull();
        }

        @Test
        @DisplayName("Set null name variable | 设置 null 名称变量")
        void testSetNullNameVariable() {
            MapContext ctx = new MapContext();
            ctx.setVariable(null, "value");
            assertThat(ctx.getVariables()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Child Context Tests | 子上下文测试")
    class ChildContextTests {

        @Test
        @DisplayName("Create child context | 创建子上下文")
        void testCreateChild() {
            MapContext parent = new MapContext();
            parent.setVariable("x", 10);

            EvaluationContext child = parent.createChild();
            assertThat(child.getVariable("x")).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Builder Tests | 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("Build with variables | 使用变量构建")
        void testBuilderWithVariables() {
            MapContext ctx = MapContext.builder()
                    .variable("a", 1)
                    .variable("b", 2)
                    .build();

            assertThat(ctx.getVariable("a")).isEqualTo(1);
            assertThat(ctx.getVariable("b")).isEqualTo(2);
        }

        @Test
        @DisplayName("Build with map variables | 使用 Map 变量构建")
        void testBuilderWithMapVariables() {
            MapContext ctx = MapContext.builder()
                    .variables(Map.of("x", 10, "y", 20))
                    .build();

            assertThat(ctx.getVariable("x")).isEqualTo(10);
            assertThat(ctx.getVariable("y")).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Integration Tests | 集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("Use with OpenExpression | 与 OpenExpression 配合使用")
        void testWithOpenExpression() {
            MapContext ctx = MapContext.of(Map.of("x", 5, "y", 3));
            Object result = OpenExpression.eval("x + y", ctx);
            assertThat(result).isEqualTo(8);
        }

        @Test
        @DisplayName("Static factory method | 静态工厂方法")
        void testOfMethod() {
            MapContext ctx = MapContext.of(Map.of("name", "Test"));
            assertThat(ctx.getVariable("name")).isEqualTo("Test");
        }
    }
}
