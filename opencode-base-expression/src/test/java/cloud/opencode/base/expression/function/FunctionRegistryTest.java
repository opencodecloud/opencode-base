package cloud.opencode.base.expression.function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * FunctionRegistry Tests
 * FunctionRegistry 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("FunctionRegistry Tests | FunctionRegistry 测试")
class FunctionRegistryTest {

    @Nested
    @DisplayName("Factory Method Tests | 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("getGlobal returns singleton | getGlobal 返回单例")
        void testGetGlobal() {
            FunctionRegistry global1 = FunctionRegistry.getGlobal();
            FunctionRegistry global2 = FunctionRegistry.getGlobal();
            assertThat(global1).isSameAs(global2);
        }

        @Test
        @DisplayName("create returns registry with global functions | create 返回带全局函数的注册表")
        void testCreate() {
            FunctionRegistry registry = FunctionRegistry.create();
            assertThat(registry).isNotSameAs(FunctionRegistry.getGlobal());
            // Should have built-in functions
            assertThat(registry.has("upper")).isTrue();
            assertThat(registry.has("lower")).isTrue();
            assertThat(registry.has("abs")).isTrue();
        }

        @Test
        @DisplayName("empty returns empty registry | empty 返回空注册表")
        void testEmpty() {
            FunctionRegistry registry = FunctionRegistry.empty();
            assertThat(registry.size()).isEqualTo(0);
            assertThat(registry.has("upper")).isFalse();
        }
    }

    @Nested
    @DisplayName("Register Tests | 注册测试")
    class RegisterTests {

        @Test
        @DisplayName("Register function | 注册函数")
        void testRegister() {
            FunctionRegistry registry = FunctionRegistry.empty();
            Function myFunc = args -> "hello";
            registry.register("myfunc", myFunc);
            assertThat(registry.has("myfunc")).isTrue();
            assertThat(registry.get("myfunc").apply()).isEqualTo("hello");
        }

        @Test
        @DisplayName("Register is case insensitive | 注册不区分大小写")
        void testRegisterCaseInsensitive() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.register("MyFunc", args -> "hello");
            assertThat(registry.has("myfunc")).isTrue();
            assertThat(registry.has("MYFUNC")).isTrue();
            assertThat(registry.has("MyFunc")).isTrue();
        }

        @Test
        @DisplayName("Register null name does nothing | 注册 null 名称无操作")
        void testRegisterNullName() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.register(null, args -> "hello");
            assertThat(registry.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Register null function does nothing | 注册 null 函数无操作")
        void testRegisterNullFunction() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.register("func", null);
            assertThat(registry.has("func")).isFalse();
        }

        @Test
        @DisplayName("Register returns this for chaining | 注册返回 this 以支持链式调用")
        void testRegisterChaining() {
            FunctionRegistry registry = FunctionRegistry.empty();
            FunctionRegistry result = registry.register("f1", args -> 1)
                    .register("f2", args -> 2)
                    .register("f3", args -> 3);
            assertThat(result).isSameAs(registry);
            assertThat(registry.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("RegisterAll Tests | registerAll 测试")
    class RegisterAllTests {

        @Test
        @DisplayName("Register all functions | 注册所有函数")
        void testRegisterAll() {
            FunctionRegistry registry = FunctionRegistry.empty();
            Map<String, Function> funcs = Map.of(
                    "f1", args -> 1,
                    "f2", args -> 2
            );
            registry.registerAll(funcs);
            assertThat(registry.size()).isEqualTo(2);
            assertThat(registry.has("f1")).isTrue();
            assertThat(registry.has("f2")).isTrue();
        }

        @Test
        @DisplayName("Register all null does nothing | 注册 null 映射无操作")
        void testRegisterAllNull() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.registerAll(null);
            assertThat(registry.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Register all is case insensitive | registerAll 不区分大小写")
        void testRegisterAllCaseInsensitive() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.registerAll(Map.of("MyFunc", args -> "hello"));
            assertThat(registry.get("myfunc")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Unregister Tests | 注销测试")
    class UnregisterTests {

        @Test
        @DisplayName("Unregister function | 注销函数")
        void testUnregister() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.register("func", args -> "hello");
            assertThat(registry.has("func")).isTrue();
            registry.unregister("func");
            assertThat(registry.has("func")).isFalse();
        }

        @Test
        @DisplayName("Unregister is case insensitive | 注销不区分大小写")
        void testUnregisterCaseInsensitive() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.register("MyFunc", args -> "hello");
            registry.unregister("myfunc");
            assertThat(registry.has("MyFunc")).isFalse();
        }

        @Test
        @DisplayName("Unregister null does nothing | 注销 null 无操作")
        void testUnregisterNull() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.register("func", args -> "hello");
            registry.unregister(null);
            assertThat(registry.has("func")).isTrue();
        }

        @Test
        @DisplayName("Unregister non-existent does nothing | 注销不存在的函数无操作")
        void testUnregisterNonExistent() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.unregister("nonexistent"); // Should not throw
            assertThat(registry.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Unregister returns this for chaining | 注销返回 this 以支持链式调用")
        void testUnregisterChaining() {
            FunctionRegistry registry = FunctionRegistry.empty()
                    .register("f1", args -> 1)
                    .register("f2", args -> 2);
            FunctionRegistry result = registry.unregister("f1").unregister("f2");
            assertThat(result).isSameAs(registry);
            assertThat(registry.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Get Tests | 获取测试")
    class GetTests {

        @Test
        @DisplayName("Get existing function | 获取存在的函数")
        void testGetExisting() {
            FunctionRegistry registry = FunctionRegistry.empty();
            Function func = args -> "hello";
            registry.register("func", func);
            assertThat(registry.get("func")).isSameAs(func);
        }

        @Test
        @DisplayName("Get is case insensitive | 获取不区分大小写")
        void testGetCaseInsensitive() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.register("MyFunc", args -> "hello");
            assertThat(registry.get("myfunc")).isNotNull();
            assertThat(registry.get("MYFUNC")).isNotNull();
        }

        @Test
        @DisplayName("Get non-existent returns null | 获取不存在的返回 null")
        void testGetNonExistent() {
            FunctionRegistry registry = FunctionRegistry.empty();
            assertThat(registry.get("nonexistent")).isNull();
        }

        @Test
        @DisplayName("Get null returns null | 获取 null 返回 null")
        void testGetNull() {
            FunctionRegistry registry = FunctionRegistry.empty();
            assertThat(registry.get(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Has Tests | 存在检查测试")
    class HasTests {

        @Test
        @DisplayName("Has existing function | 存在的函数")
        void testHasExisting() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.register("func", args -> "hello");
            assertThat(registry.has("func")).isTrue();
        }

        @Test
        @DisplayName("Has is case insensitive | 检查不区分大小写")
        void testHasCaseInsensitive() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.register("MyFunc", args -> "hello");
            assertThat(registry.has("myfunc")).isTrue();
            assertThat(registry.has("MYFUNC")).isTrue();
        }

        @Test
        @DisplayName("Has non-existent returns false | 不存在的返回 false")
        void testHasNonExistent() {
            FunctionRegistry registry = FunctionRegistry.empty();
            assertThat(registry.has("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("Has null returns false | null 返回 false")
        void testHasNull() {
            FunctionRegistry registry = FunctionRegistry.empty();
            assertThat(registry.has(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("GetNames Tests | 获取名称测试")
    class GetNamesTests {

        @Test
        @DisplayName("Get all names | 获取所有名称")
        void testGetNames() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.register("f1", args -> 1);
            registry.register("f2", args -> 2);
            Set<String> names = registry.getNames();
            assertThat(names).containsExactlyInAnyOrder("f1", "f2");
        }

        @Test
        @DisplayName("Names are immutable | 名称集合不可变")
        void testNamesImmutable() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.register("func", args -> "hello");
            Set<String> names = registry.getNames();
            assertThatThrownBy(() -> names.add("new"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Empty registry returns empty set | 空注册表返回空集合")
        void testGetNamesEmpty() {
            FunctionRegistry registry = FunctionRegistry.empty();
            assertThat(registry.getNames()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Size Tests | 大小测试")
    class SizeTests {

        @Test
        @DisplayName("Size returns count | size 返回数量")
        void testSize() {
            FunctionRegistry registry = FunctionRegistry.empty();
            assertThat(registry.size()).isEqualTo(0);
            registry.register("f1", args -> 1);
            assertThat(registry.size()).isEqualTo(1);
            registry.register("f2", args -> 2);
            assertThat(registry.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("Global registry has built-in functions | 全局注册表有内置函数")
        void testGlobalRegistrySize() {
            FunctionRegistry global = FunctionRegistry.getGlobal();
            assertThat(global.size()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Clear Tests | 清除测试")
    class ClearTests {

        @Test
        @DisplayName("Clear removes all functions | clear 移除所有函数")
        void testClear() {
            FunctionRegistry registry = FunctionRegistry.empty();
            registry.register("f1", args -> 1);
            registry.register("f2", args -> 2);
            assertThat(registry.size()).isEqualTo(2);
            registry.clear();
            assertThat(registry.size()).isEqualTo(0);
            assertThat(registry.has("f1")).isFalse();
            assertThat(registry.has("f2")).isFalse();
        }
    }

    @Nested
    @DisplayName("Built-in Functions Tests | 内置函数测试")
    class BuiltInFunctionsTests {

        @Test
        @DisplayName("Global registry has string functions | 全局注册表有字符串函数")
        void testHasStringFunctions() {
            FunctionRegistry global = FunctionRegistry.getGlobal();
            assertThat(global.has("upper")).isTrue();
            assertThat(global.has("lower")).isTrue();
            assertThat(global.has("trim")).isTrue();
            assertThat(global.has("len")).isTrue();
        }

        @Test
        @DisplayName("Global registry has math functions | 全局注册表有数学函数")
        void testHasMathFunctions() {
            FunctionRegistry global = FunctionRegistry.getGlobal();
            assertThat(global.has("abs")).isTrue();
            assertThat(global.has("min")).isTrue();
            assertThat(global.has("max")).isTrue();
            assertThat(global.has("round")).isTrue();
        }

        @Test
        @DisplayName("Global registry has collection functions | 全局注册表有集合函数")
        void testHasCollectionFunctions() {
            FunctionRegistry global = FunctionRegistry.getGlobal();
            assertThat(global.has("size")).isTrue();
            assertThat(global.has("first")).isTrue();
            assertThat(global.has("last")).isTrue();
        }

        @Test
        @DisplayName("Global registry has date functions | 全局注册表有日期函数")
        void testHasDateFunctions() {
            FunctionRegistry global = FunctionRegistry.getGlobal();
            assertThat(global.has("now")).isTrue();
            assertThat(global.has("today")).isTrue();
            assertThat(global.has("year")).isTrue();
        }

        @Test
        @DisplayName("Global registry has type functions | 全局注册表有类型函数")
        void testHasTypeFunctions() {
            FunctionRegistry global = FunctionRegistry.getGlobal();
            assertThat(global.has("isnull")).isTrue();
            assertThat(global.has("typeof")).isTrue();
            assertThat(global.has("tostring")).isTrue();
        }
    }
}
