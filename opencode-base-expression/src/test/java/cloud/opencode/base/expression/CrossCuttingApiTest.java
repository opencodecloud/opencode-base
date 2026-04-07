package cloud.opencode.base.expression;

import cloud.opencode.base.expression.ast.BetweenNode;
import cloud.opencode.base.expression.ast.BinaryOpNode;
import cloud.opencode.base.expression.ast.BitwiseOpNode;
import cloud.opencode.base.expression.ast.CollectionFilterNode;
import cloud.opencode.base.expression.ast.CollectionProjectNode;
import cloud.opencode.base.expression.ast.ElvisNode;
import cloud.opencode.base.expression.ast.FunctionCallNode;
import cloud.opencode.base.expression.ast.IdentifierNode;
import cloud.opencode.base.expression.ast.InNode;
import cloud.opencode.base.expression.ast.IndexAccessNode;
import cloud.opencode.base.expression.ast.LambdaNode;
import cloud.opencode.base.expression.ast.ListLiteralNode;
import cloud.opencode.base.expression.ast.LiteralNode;
import cloud.opencode.base.expression.ast.MapLiteralNode;
import cloud.opencode.base.expression.ast.MethodCallNode;
import cloud.opencode.base.expression.ast.Node;
import cloud.opencode.base.expression.ast.PropertyAccessNode;
import cloud.opencode.base.expression.ast.StringInterpolationNode;
import cloud.opencode.base.expression.ast.TernaryOpNode;
import cloud.opencode.base.expression.ast.UnaryOpNode;
import cloud.opencode.base.expression.context.EvaluationContext;
import cloud.opencode.base.expression.context.StandardContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Cross-Cutting API Tests
 * 横切关注点API测试
 *
 * <p>Comprehensive tests for cross-cutting APIs introduced in V1.0.3:
 * {@link VariableExtractor}, {@link ExpressionVisitor}, {@link EvaluationListener},
 * {@link ExpressionTemplate}, and {@link ArithmeticMode}.</p>
 * <p>V1.0.3中引入的横切关注点API的综合测试：
 * {@link VariableExtractor}、{@link ExpressionVisitor}、{@link EvaluationListener}、
 * {@link ExpressionTemplate} 和 {@link ArithmeticMode}。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
@DisplayName("Cross-Cutting API Tests | 横切关注点API测试")
class CrossCuttingApiTest {

    // ==================== VariableExtractor Tests | 变量提取器测试 ====================

    @Nested
    @DisplayName("VariableExtractor Tests | 变量提取器测试")
    class VariableExtractorTests {

        @Test
        @DisplayName("extract variables from arithmetic expression | 从算术表达式中提取变量")
        void testArithmeticExpression() {
            Set<String> vars = VariableExtractor.extract("a + b * c");
            assertThat(vars).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("extract single variable with constant | 提取带常量的单个变量")
        void testSingleVariableWithConstant() {
            Set<String> vars = VariableExtractor.extract("x + 1");
            assertThat(vars).containsExactly("x");
        }

        @Test
        @DisplayName("no variables from pure constants | 纯常量无变量")
        void testPureConstants() {
            Set<String> vars = VariableExtractor.extract("1 + 2");
            assertThat(vars).isEmpty();
        }

        @Test
        @DisplayName("property access extracts root identifier | 属性访问提取根标识符")
        void testPropertyAccess() {
            Set<String> vars = VariableExtractor.extract("user.name");
            assertThat(vars).containsExactly("user");
        }

        @Test
        @DisplayName("function call extracts only arguments | 函数调用仅提取参数")
        void testFunctionCallArguments() {
            Set<String> vars = VariableExtractor.extract("func(a, b)");
            assertThat(vars).containsExactly("a", "b");
        }

        @Test
        @DisplayName("ternary expression extracts all branches | 三元表达式提取所有分支")
        void testTernaryExpression() {
            Set<String> vars = VariableExtractor.extract("a > 3 ? b : c");
            assertThat(vars).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("elvis expression extracts both operands | Elvis表达式提取两个操作数")
        void testElvisExpression() {
            Set<String> vars = VariableExtractor.extract("x ?: y");
            assertThat(vars).containsExactly("x", "y");
        }

        @Test
        @DisplayName("in expression extracts value variable | in表达式提取值变量")
        void testInExpression() {
            Set<String> vars = VariableExtractor.extract("x in {1, 2, 3}");
            assertThat(vars).containsExactly("x");
        }

        @Test
        @DisplayName("between expression extracts all identifiers | between表达式提取所有标识符")
        void testBetweenExpression() {
            Set<String> vars = VariableExtractor.extract("val between low and high");
            assertThat(vars).containsExactly("val", "low", "high");
        }

        @Test
        @DisplayName("special identifiers are excluded | 特殊标识符被排除")
        void testSpecialIdentifiersExcluded() {
            Set<String> vars = VariableExtractor.extract("true");
            assertThat(vars).isEmpty();
        }

        @Test
        @DisplayName("duplicate variables appear once | 重复变量仅出现一次")
        void testDuplicateVariables() {
            Set<String> vars = VariableExtractor.extract("x + x + x");
            assertThat(vars).containsExactly("x");
        }

        @Test
        @DisplayName("null expression throws NullPointerException | null表达式抛出NullPointerException")
        void testNullExpression() {
            assertThatNullPointerException()
                    .isThrownBy(() -> VariableExtractor.extract((String) null))
                    .withMessageContaining("null");
        }

        @Test
        @DisplayName("null node throws NullPointerException | null节点抛出NullPointerException")
        void testNullNode() {
            assertThatNullPointerException()
                    .isThrownBy(() -> VariableExtractor.extract((Node) null))
                    .withMessageContaining("null");
        }

        @Test
        @DisplayName("extract from pre-parsed AST node | 从预解析的AST节点提取")
        void testExtractFromNode() {
            Node ast = new BinaryOpNode("+",
                    new IdentifierNode("a"),
                    new IdentifierNode("b"));
            Set<String> vars = VariableExtractor.extract(ast);
            assertThat(vars).containsExactly("a", "b");
        }

        @Test
        @DisplayName("result set is unmodifiable | 结果集不可修改")
        void testResultSetIsUnmodifiable() {
            Set<String> vars = VariableExtractor.extract("x + y");
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> vars.add("z"));
        }

        @Test
        @DisplayName("insertion order is preserved | 插入顺序被保留")
        void testInsertionOrder() {
            Set<String> vars = VariableExtractor.extract("c + a + b");
            assertThat(vars).containsExactly("c", "a", "b");
        }

        @Test
        @DisplayName("logical expression extracts all operands | 逻辑表达式提取所有操作数")
        void testLogicalExpression() {
            Set<String> vars = VariableExtractor.extract("a && b || c");
            assertThat(vars).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("unary expression extracts operand | 一元表达式提取操作数")
        void testUnaryExpression() {
            Set<String> vars = VariableExtractor.extract("!active");
            assertThat(vars).containsExactly("active");
        }

        @Test
        @DisplayName("nested property access extracts only root | 嵌套属性访问仅提取根")
        void testNestedPropertyAccess() {
            Set<String> vars = VariableExtractor.extract("user.address.city");
            assertThat(vars).containsExactly("user");
        }

        @Test
        @DisplayName("complex mixed expression | 复杂混合表达式")
        void testComplexMixedExpression() {
            Set<String> vars = VariableExtractor.extract("a > 0 ? func(b, c) : d + e");
            assertThat(vars).containsExactly("a", "b", "c", "d", "e");
        }
    }

    // ==================== ExpressionVisitor Tests | 表达式访问者测试 ====================

    @Nested
    @DisplayName("ExpressionVisitor Tests | 表达式访问者测试")
    class ExpressionVisitorTests {

        /**
         * A simple node counter that returns 1 for each node visited directly.
         * 一个简单的节点计数器，对每个直接访问的节点返回1。
         */
        static final ExpressionVisitor<Integer> NODE_COUNTER = new ExpressionVisitor<>() {
            @Override public Integer visit(LiteralNode node) { return 1; }
            @Override public Integer visit(IdentifierNode node) { return 1; }
            @Override public Integer visit(BinaryOpNode node) { return 1; }
            @Override public Integer visit(UnaryOpNode node) { return 1; }
            @Override public Integer visit(TernaryOpNode node) { return 1; }
            @Override public Integer visit(PropertyAccessNode node) { return 1; }
            @Override public Integer visit(IndexAccessNode node) { return 1; }
            @Override public Integer visit(MethodCallNode node) { return 1; }
            @Override public Integer visit(FunctionCallNode node) { return 1; }
            @Override public Integer visit(CollectionFilterNode node) { return 1; }
            @Override public Integer visit(CollectionProjectNode node) { return 1; }
            @Override public Integer visit(ListLiteralNode node) { return 1; }
            @Override public Integer visit(ElvisNode node) { return 1; }
            @Override public Integer visit(InNode node) { return 1; }
            @Override public Integer visit(BetweenNode node) { return 1; }
            @Override public Integer visit(BitwiseOpNode node) { return 1; }
            @Override public Integer visit(LambdaNode node) { return 1; }
            @Override public Integer visit(MapLiteralNode node) { return 1; }
            @Override public Integer visit(StringInterpolationNode node) { return 1; }
        };

        /**
         * A simple type name visitor that returns the node type name.
         * 一个简单的类型名称访问者，返回节点类型名称。
         */
        static final ExpressionVisitor<String> TYPE_NAME_VISITOR = new ExpressionVisitor<>() {
            @Override public String visit(LiteralNode node) { return "Literal"; }
            @Override public String visit(IdentifierNode node) { return "Identifier"; }
            @Override public String visit(BinaryOpNode node) { return "BinaryOp"; }
            @Override public String visit(UnaryOpNode node) { return "UnaryOp"; }
            @Override public String visit(TernaryOpNode node) { return "TernaryOp"; }
            @Override public String visit(PropertyAccessNode node) { return "PropertyAccess"; }
            @Override public String visit(IndexAccessNode node) { return "IndexAccess"; }
            @Override public String visit(MethodCallNode node) { return "MethodCall"; }
            @Override public String visit(FunctionCallNode node) { return "FunctionCall"; }
            @Override public String visit(CollectionFilterNode node) { return "CollectionFilter"; }
            @Override public String visit(CollectionProjectNode node) { return "CollectionProject"; }
            @Override public String visit(ListLiteralNode node) { return "ListLiteral"; }
            @Override public String visit(ElvisNode node) { return "Elvis"; }
            @Override public String visit(InNode node) { return "In"; }
            @Override public String visit(BetweenNode node) { return "Between"; }
            @Override public String visit(BitwiseOpNode node) { return "BitwiseOp"; }
            @Override public String visit(LambdaNode node) { return "Lambda"; }
            @Override public String visit(MapLiteralNode node) { return "MapLiteral"; }
            @Override public String visit(StringInterpolationNode node) { return "StringInterpolation"; }
        };

        @Test
        @DisplayName("NodeCounter visits literal node | 节点计数器访问字面量节点")
        void testNodeCounterLiteral() {
            assertThat(NODE_COUNTER.visit(LiteralNode.ofInt(42))).isEqualTo(1);
        }

        @Test
        @DisplayName("NodeCounter visits identifier node | 节点计数器访问标识符节点")
        void testNodeCounterIdentifier() {
            assertThat(NODE_COUNTER.visit(IdentifierNode.of("x"))).isEqualTo(1);
        }

        @Test
        @DisplayName("NodeCounter visits binary operation node | 节点计数器访问二元运算节点")
        void testNodeCounterBinaryOp() {
            BinaryOpNode node = new BinaryOpNode("+",
                    LiteralNode.ofInt(1), LiteralNode.ofInt(2));
            assertThat(NODE_COUNTER.visit(node)).isEqualTo(1);
        }

        @Test
        @DisplayName("NodeCounter visits all V1.0.3 node types | 节点计数器访问所有V1.0.3节点类型")
        void testNodeCounterV103Nodes() {
            assertThat(NODE_COUNTER.visit(new ElvisNode(
                    IdentifierNode.of("x"), LiteralNode.ofInt(0)))).isEqualTo(1);
            assertThat(NODE_COUNTER.visit(new InNode(
                    IdentifierNode.of("x"), new ListLiteralNode(List.of())))).isEqualTo(1);
            assertThat(NODE_COUNTER.visit(new BetweenNode(
                    IdentifierNode.of("x"), LiteralNode.ofInt(1), LiteralNode.ofInt(10)))).isEqualTo(1);
            assertThat(NODE_COUNTER.visit(new BitwiseOpNode("&",
                    LiteralNode.ofInt(5), LiteralNode.ofInt(3)))).isEqualTo(1);
            assertThat(NODE_COUNTER.visit(new MapLiteralNode(List.of()))).isEqualTo(1);
        }

        @Test
        @DisplayName("TypeNameVisitor returns correct names for core nodes | 类型名称访问者对核心节点返回正确名称")
        void testTypeNameVisitorCoreNodes() {
            assertThat(TYPE_NAME_VISITOR.visit(LiteralNode.ofInt(1))).isEqualTo("Literal");
            assertThat(TYPE_NAME_VISITOR.visit(IdentifierNode.of("x"))).isEqualTo("Identifier");
            assertThat(TYPE_NAME_VISITOR.visit(new BinaryOpNode("+",
                    LiteralNode.ofInt(1), LiteralNode.ofInt(2)))).isEqualTo("BinaryOp");
        }

        @Test
        @DisplayName("TypeNameVisitor returns correct names for V1.0.3 nodes | 类型名称访问者对V1.0.3节点返回正确名称")
        void testTypeNameVisitorV103Nodes() {
            assertThat(TYPE_NAME_VISITOR.visit(new ElvisNode(
                    IdentifierNode.of("x"), LiteralNode.ofInt(0)))).isEqualTo("Elvis");
            assertThat(TYPE_NAME_VISITOR.visit(new BetweenNode(
                    IdentifierNode.of("v"), LiteralNode.ofInt(0), LiteralNode.ofInt(9)))).isEqualTo("Between");
            assertThat(TYPE_NAME_VISITOR.visit(new InNode(
                    IdentifierNode.of("x"), new ListLiteralNode(List.of())))).isEqualTo("In");
            assertThat(TYPE_NAME_VISITOR.visit(new BitwiseOpNode("|",
                    LiteralNode.ofInt(1), LiteralNode.ofInt(2)))).isEqualTo("BitwiseOp");
        }

        @Test
        @DisplayName("visitor with String return type for FunctionCallNode | 对FunctionCallNode使用String返回类型的访问者")
        void testVisitorWithStringType() {
            String result = TYPE_NAME_VISITOR.visit(new FunctionCallNode("max", List.of(
                    LiteralNode.ofInt(1), LiteralNode.ofInt(2))));
            assertThat(result).isEqualTo("FunctionCall");
        }

        @Test
        @DisplayName("visitor interface covers all sealed permits | 访问者接口覆盖所有密封许可")
        void testVisitorCoversAllNodeTypes() {
            // Verify that NODE_COUNTER (which implements all methods) can visit
            // every node type without AbstractMethodError
            assertThat(NODE_COUNTER.visit(LiteralNode.ofNull())).isEqualTo(1);
            assertThat(NODE_COUNTER.visit(IdentifierNode.of("a"))).isEqualTo(1);
            assertThat(NODE_COUNTER.visit(new BinaryOpNode("+",
                    LiteralNode.ofInt(1), LiteralNode.ofInt(2)))).isEqualTo(1);
            assertThat(NODE_COUNTER.visit(new ElvisNode(
                    IdentifierNode.of("x"), LiteralNode.ofInt(0)))).isEqualTo(1);
            assertThat(NODE_COUNTER.visit(new InNode(
                    IdentifierNode.of("x"), new ListLiteralNode(List.of())))).isEqualTo(1);
            assertThat(NODE_COUNTER.visit(new BetweenNode(
                    IdentifierNode.of("x"), LiteralNode.ofInt(1), LiteralNode.ofInt(10)))).isEqualTo(1);
            assertThat(NODE_COUNTER.visit(new BitwiseOpNode("|",
                    LiteralNode.ofInt(1), LiteralNode.ofInt(2)))).isEqualTo(1);
            assertThat(NODE_COUNTER.visit(new MapLiteralNode(List.of()))).isEqualTo(1);
            assertThat(NODE_COUNTER.visit(new ListLiteralNode(List.of()))).isEqualTo(1);
        }
    }

    // ==================== EvaluationListener Tests | 求值监听器测试 ====================

    @Nested
    @DisplayName("EvaluationListener Tests | 求值监听器测试")
    class EvaluationListenerTests {

        /**
         * A recording listener that tracks lifecycle events.
         * 记录生命周期事件的监听器。
         */
        static class RecordingListener implements EvaluationListener {
            final List<String> events = new ArrayList<>();

            @Override
            public void beforeEvaluate(Node node, EvaluationContext context) {
                events.add("before:" + node.getTypeName());
            }

            @Override
            public void afterEvaluate(Node node, EvaluationContext context, Object result) {
                events.add("after:" + node.getTypeName() + "=" + result);
            }

            @Override
            public void onError(Node node, EvaluationContext context, Exception error) {
                events.add("error:" + node.getTypeName() + "=" + error.getMessage());
            }
        }

        @Test
        @DisplayName("noOp returns non-null listener | noOp返回非空监听器")
        void testNoOpReturnsNonNull() {
            EvaluationListener noOp = EvaluationListener.noOp();
            assertThat(noOp).isNotNull();
        }

        @Test
        @DisplayName("noOp listener does not throw | noOp监听器不抛出异常")
        void testNoOpDoesNotThrow() {
            EvaluationListener noOp = EvaluationListener.noOp();
            Node node = LiteralNode.ofInt(42);
            StandardContext ctx = new StandardContext();

            assertThatNoException().isThrownBy(() -> {
                noOp.beforeEvaluate(node, ctx);
                noOp.afterEvaluate(node, ctx, 42);
                noOp.onError(node, ctx, new RuntimeException("test"));
            });
        }

        @Test
        @DisplayName("noOp is singleton | noOp是单例")
        void testNoOpIsSingleton() {
            EvaluationListener first = EvaluationListener.noOp();
            EvaluationListener second = EvaluationListener.noOp();
            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("recording listener captures beforeEvaluate | 记录监听器捕获beforeEvaluate")
        void testRecordingListenerBefore() {
            RecordingListener listener = new RecordingListener();
            Node node = LiteralNode.ofInt(42);
            StandardContext ctx = new StandardContext();

            listener.beforeEvaluate(node, ctx);

            assertThat(listener.events).containsExactly("before:Literal");
        }

        @Test
        @DisplayName("recording listener captures afterEvaluate | 记录监听器捕获afterEvaluate")
        void testRecordingListenerAfter() {
            RecordingListener listener = new RecordingListener();
            Node node = IdentifierNode.of("x");
            StandardContext ctx = new StandardContext();

            listener.afterEvaluate(node, ctx, "hello");

            assertThat(listener.events).containsExactly("after:Identifier=hello");
        }

        @Test
        @DisplayName("recording listener captures onError | 记录监听器捕获onError")
        void testRecordingListenerError() {
            RecordingListener listener = new RecordingListener();
            Node node = LiteralNode.ofInt(0);
            StandardContext ctx = new StandardContext();

            listener.onError(node, ctx, new ArithmeticException("/ by zero"));

            assertThat(listener.events).containsExactly("error:Literal=/ by zero");
        }

        @Test
        @DisplayName("recording listener captures full lifecycle | 记录监听器捕获完整生命周期")
        void testRecordingListenerFullLifecycle() {
            RecordingListener listener = new RecordingListener();
            Node node = LiteralNode.ofInt(42);
            StandardContext ctx = new StandardContext();

            listener.beforeEvaluate(node, ctx);
            listener.afterEvaluate(node, ctx, 42);

            assertThat(listener.events).containsExactly(
                    "before:Literal",
                    "after:Literal=42"
            );
        }

        @Test
        @DisplayName("composite with multiple listeners invokes all | 组合多个监听器全部被调用")
        void testCompositeMultipleListeners() {
            RecordingListener first = new RecordingListener();
            RecordingListener second = new RecordingListener();
            EvaluationListener composite = EvaluationListener.composite(first, second);

            Node node = LiteralNode.ofInt(10);
            StandardContext ctx = new StandardContext();

            composite.beforeEvaluate(node, ctx);
            composite.afterEvaluate(node, ctx, 10);

            assertThat(first.events).containsExactly("before:Literal", "after:Literal=10");
            assertThat(second.events).containsExactly("before:Literal", "after:Literal=10");
        }

        @Test
        @DisplayName("composite with empty array returns noOp | 空数组组合返回noOp")
        void testCompositeEmptyReturnsNoOp() {
            EvaluationListener composite = EvaluationListener.composite();
            assertThat(composite).isSameAs(EvaluationListener.noOp());
        }

        @Test
        @DisplayName("composite with single listener returns that listener | 单个监听器组合返回该监听器")
        void testCompositeSingleListener() {
            RecordingListener listener = new RecordingListener();
            EvaluationListener composite = EvaluationListener.composite(listener);
            assertThat(composite).isSameAs(listener);
        }

        @Test
        @DisplayName("composite isolates exceptions between delegates | 组合监听器在委托间隔离异常")
        void testCompositeIsolatesExceptions() {
            EvaluationListener thrower = new EvaluationListener() {
                @Override
                public void beforeEvaluate(Node node, EvaluationContext context) {
                    throw new RuntimeException("boom");
                }

                @Override
                public void afterEvaluate(Node node, EvaluationContext context, Object result) {
                    throw new RuntimeException("boom");
                }

                @Override
                public void onError(Node node, EvaluationContext context, Exception error) {
                    throw new RuntimeException("boom");
                }
            };

            RecordingListener recorder = new RecordingListener();
            EvaluationListener composite = EvaluationListener.composite(thrower, recorder);

            Node node = LiteralNode.ofInt(1);
            StandardContext ctx = new StandardContext();

            // Should not throw, and recorder should still be called
            assertThatNoException().isThrownBy(() -> {
                composite.beforeEvaluate(node, ctx);
                composite.afterEvaluate(node, ctx, 1);
                composite.onError(node, ctx, new RuntimeException("test error"));
            });

            assertThat(recorder.events).containsExactly(
                    "before:Literal",
                    "after:Literal=1",
                    "error:Literal=test error"
            );
        }

        @Test
        @DisplayName("composite null array throws NullPointerException | null数组组合抛出NullPointerException")
        void testCompositeNullArrayThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> EvaluationListener.composite((EvaluationListener[]) null));
        }

        @Test
        @DisplayName("composite null element throws NullPointerException | null元素组合抛出NullPointerException")
        void testCompositeNullElementThrows() {
            RecordingListener listener = new RecordingListener();
            assertThatNullPointerException()
                    .isThrownBy(() -> EvaluationListener.composite(listener, null));
        }

        @Test
        @DisplayName("default interface methods are no-op | 默认接口方法是空操作")
        void testDefaultMethodsAreNoOp() {
            // Create a listener that overrides nothing (uses all defaults)
            EvaluationListener defaults = new EvaluationListener() {};
            Node node = LiteralNode.ofInt(1);
            StandardContext ctx = new StandardContext();

            assertThatNoException().isThrownBy(() -> {
                defaults.beforeEvaluate(node, ctx);
                defaults.afterEvaluate(node, ctx, 1);
                defaults.onError(node, ctx, new RuntimeException("test"));
            });
        }

        @Test
        @DisplayName("composite onError invokes all delegates | 组合onError调用所有委托")
        void testCompositeOnErrorInvokesAll() {
            AtomicInteger callCount = new AtomicInteger(0);
            EvaluationListener counter1 = new EvaluationListener() {
                @Override
                public void onError(Node node, EvaluationContext context, Exception error) {
                    callCount.incrementAndGet();
                }
            };
            EvaluationListener counter2 = new EvaluationListener() {
                @Override
                public void onError(Node node, EvaluationContext context, Exception error) {
                    callCount.incrementAndGet();
                }
            };

            EvaluationListener composite = EvaluationListener.composite(counter1, counter2);
            composite.onError(LiteralNode.ofInt(0), new StandardContext(),
                    new RuntimeException("err"));

            assertThat(callCount.get()).isEqualTo(2);
        }
    }

    // ==================== ExpressionTemplate Tests | 表达式模板测试 ====================

    @Nested
    @DisplayName("ExpressionTemplate Tests | 表达式模板测试")
    class ExpressionTemplateTests {

        @Test
        @DisplayName("simple variable substitution | 简单变量替换")
        void testSimpleVariableSubstitution() {
            String result = ExpressionTemplate.render("Hello ${name}", Map.of("name", "World"));
            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("expression evaluation within template | 模板内表达式求值")
        void testExpressionEvaluation() {
            String result = ExpressionTemplate.render("${a + b}", Map.of("a", 1, "b", 2));
            assertThat(result).isEqualTo("3");
        }

        @Test
        @DisplayName("no placeholders returns original text | 无占位符返回原始文本")
        void testNoPlaceholders() {
            String result = ExpressionTemplate.render("No vars", Map.of());
            assertThat(result).isEqualTo("No vars");
        }

        @Test
        @DisplayName("escaped placeholder produces literal dollar-brace | 转义占位符产生字面量${}")
        void testEscapedPlaceholder() {
            String result = ExpressionTemplate.render("Escaped \\${x}", Map.of("x", 1));
            assertThat(result).isEqualTo("Escaped ${x}");
        }

        @Test
        @DisplayName("multiple placeholders in single template | 单个模板中的多个占位符")
        void testMultiplePlaceholders() {
            String result = ExpressionTemplate.render(
                    "${x} + ${y}", Map.of("x", 10, "y", 20));
            assertThat(result).isEqualTo("10 + 20");
        }

        @Test
        @DisplayName("empty template returns empty string | 空模板返回空字符串")
        void testEmptyTemplate() {
            String result = ExpressionTemplate.render("", Map.of());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("missing variable throws exception | 缺少变量抛出异常")
        void testMissingVariable() {
            assertThatThrownBy(() -> ExpressionTemplate.render("${z}", Map.of()))
                    .isInstanceOf(OpenExpressionException.class);
        }

        @Test
        @DisplayName("ternary expression positive branch in template | 模板中三元表达式正分支")
        void testTernaryPositiveBranch() {
            String result = ExpressionTemplate.render(
                    "${x > 0 ? 'positive' : 'negative'}", Map.of("x", 5));
            assertThat(result).isEqualTo("positive");
        }

        @Test
        @DisplayName("ternary expression negative branch in template | 模板中三元表达式负分支")
        void testTernaryNegativeBranch() {
            String result = ExpressionTemplate.render(
                    "${x > 0 ? 'positive' : 'negative'}", Map.of("x", -3));
            assertThat(result).isEqualTo("negative");
        }

        @Test
        @DisplayName("unclosed delimiter throws exception | 未关闭的分隔符抛出异常")
        void testUnclosedDelimiter() {
            assertThatThrownBy(() -> ExpressionTemplate.render("${unclosed", Map.of()))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("Unclosed");
        }

        @Test
        @DisplayName("null template throws NullPointerException | null模板抛出NullPointerException")
        void testNullTemplate() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ExpressionTemplate.render(null, Map.of()));
        }

        @Test
        @DisplayName("null variables map throws NullPointerException | null变量映射抛出NullPointerException")
        void testNullVariables() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ExpressionTemplate.render("test", (Map<String, Object>) null));
        }

        @Test
        @DisplayName("mixed text and multiple expressions | 混合文本和多个表达式")
        void testMixedTextAndExpressions() {
            String result = ExpressionTemplate.render(
                    "Name: ${name}, Age: ${age}",
                    Map.of("name", "Alice", "age", 30));
            assertThat(result).isEqualTo("Name: Alice, Age: 30");
        }

        @Test
        @DisplayName("arithmetic expression in template | 模板中的算术表达式")
        void testArithmeticInTemplate() {
            String result = ExpressionTemplate.render(
                    "Total: ${price * qty}",
                    Map.of("price", 10, "qty", 3));
            assertThat(result).isEqualTo("Total: 30");
        }

        @Test
        @DisplayName("template with only expression | 仅包含表达式的模板")
        void testOnlyExpression() {
            String result = ExpressionTemplate.render("${42}", Map.of());
            assertThat(result).isEqualTo("42");
        }

        @Test
        @DisplayName("template with string literal in expression | 表达式中的字符串字面量")
        void testStringLiteralInExpression() {
            String result = ExpressionTemplate.render("${'hello'}", Map.of());
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("template with boolean expression | 布尔表达式模板")
        void testBooleanExpression() {
            String result = ExpressionTemplate.render(
                    "${x == y}", Map.of("x", 5, "y", 5));
            assertThat(result).isEqualTo("true");
        }

        @Test
        @DisplayName("empty expression placeholder throws exception | 空表达式占位符抛出异常")
        void testEmptyExpressionInTemplate() {
            assertThatThrownBy(() -> ExpressionTemplate.render("${}", Map.of()))
                    .isInstanceOf(OpenExpressionException.class)
                    .hasMessageContaining("Empty");
        }

        @Test
        @DisplayName("render with EvaluationContext | 使用EvaluationContext渲染")
        void testRenderWithContext() {
            StandardContext ctx = new StandardContext();
            ctx.setVariable("greeting", "Hi");
            String result = ExpressionTemplate.render("${greeting}!", ctx);
            assertThat(result).isEqualTo("Hi!");
        }

        @Test
        @DisplayName("null context throws NullPointerException | null上下文抛出NullPointerException")
        void testNullContext() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ExpressionTemplate.render("test", (EvaluationContext) null));
        }

        @Test
        @DisplayName("consecutive placeholders with no separator | 无分隔符的连续占位符")
        void testConsecutivePlaceholders() {
            String result = ExpressionTemplate.render(
                    "${a}${b}${c}",
                    Map.of("a", "X", "b", "Y", "c", "Z"));
            assertThat(result).isEqualTo("XYZ");
        }

        @Test
        @DisplayName("placeholder at start and end of template | 占位符在模板开头和结尾")
        void testPlaceholderAtStartAndEnd() {
            String result = ExpressionTemplate.render(
                    "${x} middle ${y}",
                    Map.of("x", "START", "y", "END"));
            assertThat(result).isEqualTo("START middle END");
        }
    }

    // ==================== ArithmeticMode Tests | 算术模式测试 ====================

    @Nested
    @DisplayName("ArithmeticMode Tests | 算术模式测试")
    class ArithmeticModeTests {

        @Test
        @DisplayName("STANDARD enum value exists | STANDARD枚举值存在")
        void testStandardExists() {
            assertThat(ArithmeticMode.STANDARD).isNotNull();
        }

        @Test
        @DisplayName("BIG_DECIMAL enum value exists | BIG_DECIMAL枚举值存在")
        void testBigDecimalExists() {
            assertThat(ArithmeticMode.BIG_DECIMAL).isNotNull();
        }

        @Test
        @DisplayName("valueOf returns correct values | valueOf返回正确值")
        void testValueOf() {
            assertThat(ArithmeticMode.valueOf("STANDARD")).isEqualTo(ArithmeticMode.STANDARD);
            assertThat(ArithmeticMode.valueOf("BIG_DECIMAL")).isEqualTo(ArithmeticMode.BIG_DECIMAL);
        }

        @Test
        @DisplayName("values returns all modes | values返回所有模式")
        void testValuesContainsAll() {
            ArithmeticMode[] values = ArithmeticMode.values();
            assertThat(values).containsExactly(ArithmeticMode.STANDARD, ArithmeticMode.BIG_DECIMAL);
        }

        @Test
        @DisplayName("valueOf invalid name throws IllegalArgumentException | 无效名称valueOf抛出IllegalArgumentException")
        void testValueOfInvalid() {
            assertThatThrownBy(() -> ArithmeticMode.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("enum ordinals are correct | 枚举序号正确")
        void testOrdinals() {
            assertThat(ArithmeticMode.STANDARD.ordinal()).isZero();
            assertThat(ArithmeticMode.BIG_DECIMAL.ordinal()).isEqualTo(1);
        }

        @Test
        @DisplayName("enum name returns correct string | 枚举name返回正确字符串")
        void testName() {
            assertThat(ArithmeticMode.STANDARD.name()).isEqualTo("STANDARD");
            assertThat(ArithmeticMode.BIG_DECIMAL.name()).isEqualTo("BIG_DECIMAL");
        }
    }
}
