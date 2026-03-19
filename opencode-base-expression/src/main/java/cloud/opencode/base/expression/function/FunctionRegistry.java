package cloud.opencode.base.expression.function;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Function Registry
 * 函数注册表
 *
 * <p>Manages function registration and lookup for expression evaluation.</p>
 * <p>管理表达式求值的函数注册和查找。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Case-insensitive function name lookup - 不区分大小写的函数名查找</li>
 *   <li>Global registry with built-in functions (string, math, collection, date, type) - 全局注册表，内置函数</li>
 *   <li>Register/unregister custom functions - 注册/注销自定义函数</li>
 *   <li>Create isolated registries - 创建隔离的注册表</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FunctionRegistry registry = FunctionRegistry.create();
 * registry.register("greet", args -> "Hello, " + args[0]);
 * boolean exists = registry.has("upper");  // true (built-in)
 * Function fn = registry.get("greet");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, ConcurrentHashMap for function storage - 线程安全: 是，函数存储使用ConcurrentHashMap</li>
 *   <li>Null-safe: Yes, null name returns null/ignored - 空值安全: 是，null名称返回null/被忽略</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class FunctionRegistry {

    private static final FunctionRegistry GLOBAL = new FunctionRegistry();

    private final Map<String, Function> functions = new ConcurrentHashMap<>();

    static {
        // Register built-in functions
        GLOBAL.registerAll(StringFunctions.getFunctions());
        GLOBAL.registerAll(MathFunctions.getFunctions());
        GLOBAL.registerAll(CollectionFunctions.getFunctions());
        GLOBAL.registerAll(DateFunctions.getFunctions());
        GLOBAL.registerAll(TypeFunctions.getFunctions());
    }

    /**
     * Get global function registry
     * 获取全局函数注册表
     *
     * @return the global registry | 全局注册表
     */
    public static FunctionRegistry getGlobal() {
        return GLOBAL;
    }

    /**
     * Create new registry with global functions
     * 创建包含全局函数的新注册表
     *
     * @return the new registry | 新注册表
     */
    public static FunctionRegistry create() {
        FunctionRegistry registry = new FunctionRegistry();
        registry.functions.putAll(GLOBAL.functions);
        return registry;
    }

    /**
     * Create empty registry
     * 创建空注册表
     *
     * @return the empty registry | 空注册表
     */
    public static FunctionRegistry empty() {
        return new FunctionRegistry();
    }

    /**
     * Register a function
     * 注册函数
     *
     * @param name the function name | 函数名
     * @param function the function | 函数
     * @return this registry for chaining | 用于链式调用的注册表
     */
    public FunctionRegistry register(String name, Function function) {
        if (name != null && function != null) {
            functions.put(name.toLowerCase(), function);
        }
        return this;
    }

    /**
     * Register multiple functions
     * 注册多个函数
     *
     * @param funcs the functions map | 函数映射
     * @return this registry for chaining | 用于链式调用的注册表
     */
    public FunctionRegistry registerAll(Map<String, Function> funcs) {
        if (funcs != null) {
            funcs.forEach((name, func) -> functions.put(name.toLowerCase(), func));
        }
        return this;
    }

    /**
     * Unregister a function
     * 注销函数
     *
     * @param name the function name | 函数名
     * @return this registry for chaining | 用于链式调用的注册表
     */
    public FunctionRegistry unregister(String name) {
        if (name != null) {
            functions.remove(name.toLowerCase());
        }
        return this;
    }

    /**
     * Get a function by name
     * 按名称获取函数
     *
     * @param name the function name | 函数名
     * @return the function, or null if not found | 函数，如果未找到则为null
     */
    public Function get(String name) {
        if (name == null) {
            return null;
        }
        return functions.get(name.toLowerCase());
    }

    /**
     * Check if function exists
     * 检查函数是否存在
     *
     * @param name the function name | 函数名
     * @return true if exists | 如果存在返回true
     */
    public boolean has(String name) {
        return name != null && functions.containsKey(name.toLowerCase());
    }

    /**
     * Get all function names
     * 获取所有函数名
     *
     * @return the function names | 函数名集合
     */
    public Set<String> getNames() {
        return Set.copyOf(functions.keySet());
    }

    /**
     * Get function count
     * 获取函数数量
     *
     * @return the count | 数量
     */
    public int size() {
        return functions.size();
    }

    /**
     * Clear all functions
     * 清除所有函数
     */
    public void clear() {
        functions.clear();
    }
}
