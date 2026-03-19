package cloud.opencode.base.expression.spi;

import cloud.opencode.base.expression.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * FunctionProvider Tests
 * FunctionProvider 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("FunctionProvider Tests | FunctionProvider 测试")
class FunctionProviderTest {

    @Nested
    @DisplayName("Interface Contract Tests | 接口契约测试")
    class InterfaceContractTests {

        @Test
        @DisplayName("getFunctions returns function map | getFunctions 返回函数映射")
        void testGetFunctions() {
            FunctionProvider provider = new TestFunctionProvider();
            Map<String, Function> functions = provider.getFunctions();
            assertThat(functions).isNotNull();
            assertThat(functions).containsKey("add");
            assertThat(functions).containsKey("multiply");
        }

        @Test
        @DisplayName("functions can be applied | 函数可以应用")
        void testFunctionsCanBeApplied() {
            FunctionProvider provider = new TestFunctionProvider();
            Map<String, Function> functions = provider.getFunctions();

            Function add = functions.get("add");
            assertThat(add.apply(1, 2)).isEqualTo(3);

            Function multiply = functions.get("multiply");
            assertThat(multiply.apply(3, 4)).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("Default Methods Tests | 默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("getPriority returns 0 by default | getPriority 默认返回 0")
        void testGetPriorityDefault() {
            FunctionProvider provider = new TestFunctionProvider();
            assertThat(provider.getPriority()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Priority Tests | 优先级测试")
    class PriorityTests {

        @Test
        @DisplayName("custom priority | 自定义优先级")
        void testCustomPriority() {
            FunctionProvider provider = new HighPriorityProvider();
            assertThat(provider.getPriority()).isEqualTo(100);
        }

        @Test
        @DisplayName("negative priority | 负优先级")
        void testNegativePriority() {
            FunctionProvider provider = new LowPriorityProvider();
            assertThat(provider.getPriority()).isEqualTo(-10);
        }
    }

    @Nested
    @DisplayName("Empty Provider Tests | 空提供者测试")
    class EmptyProviderTests {

        @Test
        @DisplayName("empty provider returns empty map | 空提供者返回空映射")
        void testEmptyProvider() {
            FunctionProvider provider = new EmptyFunctionProvider();
            assertThat(provider.getFunctions()).isEmpty();
        }
    }

    // Test implementations

    private static class TestFunctionProvider implements FunctionProvider {
        @Override
        public Map<String, Function> getFunctions() {
            Map<String, Function> functions = new HashMap<>();
            functions.put("add", args -> {
                if (args.length < 2) return 0;
                return ((Number) args[0]).intValue() + ((Number) args[1]).intValue();
            });
            functions.put("multiply", args -> {
                if (args.length < 2) return 0;
                return ((Number) args[0]).intValue() * ((Number) args[1]).intValue();
            });
            return functions;
        }
    }

    private static class HighPriorityProvider implements FunctionProvider {
        @Override
        public Map<String, Function> getFunctions() {
            return Map.of();
        }

        @Override
        public int getPriority() {
            return 100;
        }
    }

    private static class LowPriorityProvider implements FunctionProvider {
        @Override
        public Map<String, Function> getFunctions() {
            return Map.of();
        }

        @Override
        public int getPriority() {
            return -10;
        }
    }

    private static class EmptyFunctionProvider implements FunctionProvider {
        @Override
        public Map<String, Function> getFunctions() {
            return Map.of();
        }
    }
}
