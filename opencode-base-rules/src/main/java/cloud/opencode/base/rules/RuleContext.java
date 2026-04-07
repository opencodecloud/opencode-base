package cloud.opencode.base.rules;

import cloud.opencode.base.rules.key.TypedKey;
import cloud.opencode.base.rules.model.DefaultFactStore;
import cloud.opencode.base.rules.model.FactStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Rule Context - Manages Facts and Variables During Rule Execution
 * 规则上下文 - 管理规则执行期间的事实和变量
 *
 * <p>Provides storage for facts (domain objects) and variables used during rule evaluation
 * and action execution. Also stores rule execution results.</p>
 * <p>提供在规则评估和动作执行期间使用的事实（领域对象）和变量的存储。同时存储规则执行结果。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fact management (typed and named) - 事实管理（类型化和命名）</li>
 *   <li>Variable storage - 变量存储</li>
 *   <li>Result collection - 结果收集</li>
 *   <li>Fluent API - 流式API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create context with facts
 * RuleContext context = RuleContext.create()
 *     .put("customerType", "VIP")
 *     .put("orderAmount", 5000.0)
 *     .addFact(customer)
 *     .addFact(order);
 *
 * // Access values
 * String type = context.get("customerType");
 * Double amount = context.get("orderAmount");
 * Optional<Customer> cust = context.getFact(Customer.class);
 *
 * // Set results
 * context.setResult("discount", 0.15);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (create new instance per execution) - 线程安全: 否（每次执行创建新实例）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Rule
 * @see FactStore
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public final class RuleContext {

    private final FactStore facts;
    private final Map<String, Object> variables;
    private final Map<String, Object> results;

    private RuleContext(FactStore facts) {
        this.facts = facts;
        this.variables = new HashMap<>();
        this.results = new HashMap<>();
    }

    /**
     * Creates an empty rule context
     * 创建空的规则上下文
     *
     * @return new context instance | 新的上下文实例
     */
    public static RuleContext create() {
        return new RuleContext(new DefaultFactStore());
    }

    /**
     * Creates a context from a map of values
     * 从值的Map创建上下文
     *
     * @param values the initial values | 初始值
     * @return new context instance | 新的上下文实例
     */
    public static RuleContext of(Map<String, Object> values) {
        RuleContext context = create();
        values.forEach(context::put);
        return context;
    }

    /**
     * Creates a context from alternating key-value pairs
     * 从交替的键值对创建上下文
     *
     * @param keyValues alternating key-value pairs | 交替的键值对
     * @return new context instance | 新的上下文实例
     * @throws IllegalArgumentException if odd number of arguments
     */
    public static RuleContext of(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Expected even number of arguments (key-value pairs)");
        }
        RuleContext context = create();
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = String.valueOf(keyValues[i]);
            Object value = keyValues[i + 1];
            context.put(key, value);
        }
        return context;
    }

    /**
     * Creates a context with fact objects
     * 使用事实对象创建上下文
     *
     * @param facts the fact objects | 事实对象
     * @return new context instance | 新的上下文实例
     */
    public static RuleContext withFacts(Object... facts) {
        RuleContext context = create();
        for (Object fact : facts) {
            context.addFact(fact);
        }
        return context;
    }

    /**
     * Adds a fact object to the context
     * 向上下文添加事实对象
     *
     * @param fact the fact object | 事实对象
     * @return this context for chaining | 此上下文用于链式调用
     */
    public RuleContext addFact(Object fact) {
        facts.add(fact);
        return this;
    }

    /**
     * Adds a named fact object to the context
     * 向上下文添加命名的事实对象
     *
     * @param name the fact name | 事实名称
     * @param fact the fact object | 事实对象
     * @return this context for chaining | 此上下文用于链式调用
     */
    public RuleContext addFact(String name, Object fact) {
        facts.add(name, fact);
        return this;
    }

    /**
     * Sets a variable value in the context
     * 在上下文中设置变量值
     *
     * @param key   the variable name | 变量名
     * @param value the variable value | 变量值
     * @return this context for chaining | 此上下文用于链式调用
     */
    public RuleContext put(String key, Object value) {
        variables.put(key, value);
        return this;
    }

    /**
     * Gets a variable value from the context
     * 从上下文获取变量值
     *
     * @param key the variable name | 变量名
     * @param <T> the value type | 值类型
     * @return the value, or null if not found | 值，如果未找到则为null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Object value = variables.get(key);
        if (value == null) {
            value = facts.get(key);
        }
        return (T) value;
    }

    /**
     * Gets a variable value with a default
     * 获取变量值，带默认值
     *
     * @param key          the variable name | 变量名
     * @param defaultValue the default value | 默认值
     * @param <T>          the value type | 值类型
     * @return the value, or default if not found | 值，如果未找到则为默认值
     */
    public <T> T get(String key, T defaultValue) {
        T value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a fact by type
     * 按类型获取事实
     *
     * @param type the fact type | 事实类型
     * @param <T>  the fact type | 事实类型
     * @return optional containing the fact | 包含事实的Optional
     */
    public <T> Optional<T> getFact(Class<T> type) {
        return facts.get(type);
    }

    /**
     * Gets all facts of a specific type
     * 获取特定类型的所有事实
     *
     * @param type the fact type | 事实类型
     * @param <T>  the fact type | 事实类型
     * @return list of facts | 事实列表
     */
    public <T> List<T> getFacts(Class<T> type) {
        return facts.getAll(type);
    }

    /**
     * Checks if a variable exists
     * 检查变量是否存在
     *
     * @param key the variable name | 变量名
     * @return true if exists | 如果存在返回true
     */
    public boolean contains(String key) {
        return variables.containsKey(key) || facts.contains(key);
    }

    /**
     * Sets a result value
     * 设置结果值
     *
     * @param key   the result key | 结果键
     * @param value the result value | 结果值
     * @return this context for chaining | 此上下文用于链式调用
     */
    public RuleContext setResult(String key, Object value) {
        results.put(key, value);
        return this;
    }

    /**
     * Gets a result value
     * 获取结果值
     *
     * @param key the result key | 结果键
     * @param <T> the result type | 结果类型
     * @return the result value | 结果值
     */
    @SuppressWarnings("unchecked")
    public <T> T getResult(String key) {
        return (T) results.get(key);
    }

    /**
     * Gets a result value with default
     * 获取结果值，带默认值
     *
     * @param key          the result key | 结果键
     * @param defaultValue the default value | 默认值
     * @param <T>          the result type | 结果类型
     * @return the result value or default | 结果值或默认值
     */
    public <T> T getResult(String key, T defaultValue) {
        T value = getResult(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets all results as an immutable map
     * 获取所有结果作为不可变Map
     *
     * @return immutable map of results | 结果的不可变Map
     */
    public Map<String, Object> getResults() {
        return Map.copyOf(results);
    }

    /**
     * Gets all variables as an immutable map
     * 获取所有变量作为不可变Map
     *
     * @return immutable map of variables | 变量的不可变Map
     */
    public Map<String, Object> getVariables() {
        return Map.copyOf(variables);
    }

    /**
     * Gets the fact store
     * 获取事实存储
     *
     * @return the fact store | 事实存储
     */
    public FactStore facts() {
        return facts;
    }

    /**
     * Gets a typed value by typed key (checks variables first, then facts)
     * 通过类型化键获取类型化值（先检查变量，再检查事实）
     *
     * @param key the typed key | 类型化键
     * @param <T> the value type | 值类型
     * @return the typed value, or null if not found | 类型化值，如果未找到则为null
     * @since JDK 25, opencode-base-rules V1.0.3
     */
    public <T> T get(TypedKey<T> key) {
        Object value = variables.get(key.name());
        if (value == null) value = facts.get(key.name());
        if (value == null) return null;
        return key.type().isInstance(value) ? key.type().cast(value) : null;
    }

    /**
     * Gets a typed value by typed key with a default
     * 通过类型化键获取类型化值，带默认值
     *
     * @param key          the typed key | 类型化键
     * @param defaultValue the default value | 默认值
     * @param <T>          the value type | 值类型
     * @return the typed value, or default if not found | 类型化值，如果未找到则为默认值
     * @since JDK 25, opencode-base-rules V1.0.3
     */
    public <T> T get(TypedKey<T> key, T defaultValue) {
        T value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Puts a typed value by typed key into variables
     * 通过类型化键将类型化值放入变量中
     *
     * @param key   the typed key | 类型化键
     * @param value the value | 值
     * @param <T>   the value type | 值类型
     * @return this context for chaining | 此上下文用于链式调用
     * @since JDK 25, opencode-base-rules V1.0.3
     */
    public <T> RuleContext put(TypedKey<T> key, T value) {
        variables.put(key.name(), value);
        return this;
    }

    /**
     * Checks if a typed key exists in variables or facts
     * 检查类型化键是否存在于变量或事实中
     *
     * @param key the typed key | 类型化键
     * @param <T> the value type | 值类型
     * @return true if exists | 如果存在返回true
     * @since JDK 25, opencode-base-rules V1.0.3
     */
    public <T> boolean contains(TypedKey<T> key) {
        return variables.containsKey(key.name()) || facts.contains(key.name());
    }

    /**
     * Clears all results
     * 清除所有结果
     *
     * @return this context for chaining | 此上下文用于链式调用
     */
    public RuleContext clearResults() {
        results.clear();
        return this;
    }

    /**
     * Clears all variables
     * 清除所有变量
     *
     * @return this context for chaining | 此上下文用于链式调用
     */
    public RuleContext clearVariables() {
        variables.clear();
        return this;
    }
}
