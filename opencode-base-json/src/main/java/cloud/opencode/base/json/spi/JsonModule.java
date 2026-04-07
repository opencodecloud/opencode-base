
package cloud.opencode.base.json.spi;

import cloud.opencode.base.json.JsonConfig;
import cloud.opencode.base.json.adapter.JsonTypeAdapter;
import cloud.opencode.base.json.adapter.JsonTypeAdapterFactory;

import java.util.*;

/**
 * JSON Module - Pluggable Module for Extending JSON Processing
 * JSON 模块 - 用于扩展 JSON 处理的可插拔模块
 *
 * <p>This interface defines a pluggable module that can register custom
 * type adapters, adapter factories, and mixin annotations with the
 * JSON processing framework. Similar to Jackson's Module concept.</p>
 * <p>此接口定义了一个可插拔模块，可以向 JSON 处理框架注册
 * 自定义类型适配器、适配器工厂和混入注解。类似于 Jackson 的 Module 概念。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * JsonModule module = new JsonModule.SimpleModule("my-module", "1.0.0")
 *     .addAdapter(new MoneyAdapter())
 *     .addMixin(User.class, UserMixin.class);
 *
 * OpenJson json = OpenJson.withConfig(config)
 *     .registerModule(module);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pluggable module system for JSON extensions - 可插拔的 JSON 扩展模块系统</li>
 *   <li>Register adapters, factories, and mixins - 注册适配器、工厂和混入</li>
 *   <li>SimpleModule for convenient module creation - SimpleModule 用于便捷的模块创建</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public interface JsonModule {

    /**
     * Returns the module name.
     * 返回模块名称。
     *
     * @return the module name - 模块名称
     */
    String getName();

    /**
     * Returns the module version.
     * 返回模块版本。
     *
     * @return the module version - 模块版本
     */
    String getVersion();

    /**
     * Configures the module using the provided setup context.
     * 使用提供的设置上下文配置模块。
     *
     * @param context the setup context for registering components - 用于注册组件的设置上下文
     */
    void setupModule(SetupContext context);

    // ==================== SetupContext ====================

    /**
     * Setup Context - Context for Module Configuration
     * 设置上下文 - 模块配置的上下文
     *
     * <p>Provides methods for modules to register their components
     * (adapters, factories, mixins) with the JSON processing framework.</p>
     * <p>为模块提供向 JSON 处理框架注册其组件（适配器、工厂、混入）的方法。</p>
     */
    interface SetupContext {

        /**
         * Registers a type adapter.
         * 注册类型适配器。
         *
         * @param adapter the type adapter to register - 要注册的类型适配器
         */
        void addTypeAdapter(JsonTypeAdapter<?> adapter);

        /**
         * Registers a type adapter factory.
         * 注册类型适配器工厂。
         *
         * @param factory the factory to register - 要注册的工厂
         */
        void addTypeAdapterFactory(JsonTypeAdapterFactory factory);

        /**
         * Registers a mixin annotation class for a target type.
         * 为目标类型注册混入注解类。
         *
         * @param target the target class to apply mixin to - 要应用混入的目标类
         * @param mixin  the mixin class containing annotations - 包含注解的混入类
         */
        void addMixin(Class<?> target, Class<?> mixin);

        /**
         * Registers a key adapter for map key serialization.
         * 注册用于 Map 键序列化的键适配器。
         *
         * @param type    the key type - 键类型
         * @param adapter the key adapter - 键适配器
         */
        void addKeyAdapter(Class<?> type, JsonTypeAdapter<?> adapter);

        /**
         * Returns the current JSON configuration.
         * 返回当前的 JSON 配置。
         *
         * @return the JSON config - JSON 配置
         */
        JsonConfig getConfig();
    }

    // ==================== SimpleModule ====================

    /**
     * Simple Module - Convenient Base Implementation of JsonModule
     * 简单模块 - JsonModule 的便捷基础实现
     *
     * <p>Provides a convenient way to create modules by collecting
     * adapters, factories, and mixins before registering them via
     * {@link #setupModule(SetupContext)}.</p>
     * <p>提供一种便捷的方式来创建模块，在通过 {@link #setupModule(SetupContext)}
     * 注册之前收集适配器、工厂和混入。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * SimpleModule module = new SimpleModule("custom", "1.0.0")
     *     .addAdapter(new MoneyAdapter())
     *     .addFactory(new EnumAdapterFactory())
     *     .addMixin(User.class, UserMixin.class);
     * }</pre>
     */
    abstract class SimpleModule implements JsonModule {

        /**
         * Module name
         * 模块名称
         */
        private final String name;

        /**
         * Module version
         * 模块版本
         */
        private final String version;

        /**
         * Registered type adapters
         * 注册的类型适配器
         */
        private final List<JsonTypeAdapter<?>> adapters = new ArrayList<>();

        /**
         * Registered adapter factories
         * 注册的适配器工厂
         */
        private final List<JsonTypeAdapterFactory> factories = new ArrayList<>();

        /**
         * Registered mixin mappings (target -> mixin)
         * 注册的混入映射（目标 -> 混入）
         */
        private final Map<Class<?>, Class<?>> mixins = new LinkedHashMap<>();

        /**
         * Registered key adapters (type -> adapter)
         * 注册的键适配器（类型 -> 适配器）
         */
        private final Map<Class<?>, JsonTypeAdapter<?>> keyAdapters = new LinkedHashMap<>();

        /**
         * Constructs a SimpleModule with the given name and version.
         * 使用给定的名称和版本构造 SimpleModule。
         *
         * @param name    the module name - 模块名称
         * @param version the module version - 模块版本
         */
        protected SimpleModule(String name, String version) {
            this.name = Objects.requireNonNull(name, "Module name must not be null");
            this.version = Objects.requireNonNull(version, "Module version must not be null");
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getVersion() {
            return version;
        }

        /**
         * Adds a type adapter to this module.
         * 向此模块添加类型适配器。
         *
         * @param adapter the adapter to add - 要添加的适配器
         * @return this module for chaining - 此模块，用于链式调用
         */
        public SimpleModule addAdapter(JsonTypeAdapter<?> adapter) {
            Objects.requireNonNull(adapter, "Adapter must not be null");
            adapters.add(adapter);
            return this;
        }

        /**
         * Adds a type adapter factory to this module.
         * 向此模块添加类型适配器工厂。
         *
         * @param factory the factory to add - 要添加的工厂
         * @return this module for chaining - 此模块，用于链式调用
         */
        public SimpleModule addFactory(JsonTypeAdapterFactory factory) {
            Objects.requireNonNull(factory, "Factory must not be null");
            factories.add(factory);
            return this;
        }

        /**
         * Adds a mixin mapping to this module.
         * 向此模块添加混入映射。
         *
         * @param target the target class - 目标类
         * @param mixin  the mixin class - 混入类
         * @return this module for chaining - 此模块，用于链式调用
         */
        public SimpleModule addMixin(Class<?> target, Class<?> mixin) {
            Objects.requireNonNull(target, "Target class must not be null");
            Objects.requireNonNull(mixin, "Mixin class must not be null");
            mixins.put(target, mixin);
            return this;
        }

        /**
         * Adds a key adapter to this module.
         * 向此模块添加键适配器。
         *
         * @param type    the key type - 键类型
         * @param adapter the key adapter - 键适配器
         * @return this module for chaining - 此模块，用于链式调用
         */
        public SimpleModule addKeyAdapter(Class<?> type, JsonTypeAdapter<?> adapter) {
            Objects.requireNonNull(type, "Key type must not be null");
            Objects.requireNonNull(adapter, "Adapter must not be null");
            keyAdapters.put(type, adapter);
            return this;
        }

        /**
         * Returns the registered adapters.
         * 返回注册的适配器。
         *
         * @return unmodifiable list of adapters - 不可修改的适配器列表
         */
        public List<JsonTypeAdapter<?>> getAdapters() {
            return Collections.unmodifiableList(adapters);
        }

        /**
         * Returns the registered factories.
         * 返回注册的工厂。
         *
         * @return unmodifiable list of factories - 不可修改的工厂列表
         */
        public List<JsonTypeAdapterFactory> getFactories() {
            return Collections.unmodifiableList(factories);
        }

        /**
         * Returns the registered mixins.
         * 返回注册的混入。
         *
         * @return unmodifiable map of mixins - 不可修改的混入映射
         */
        public Map<Class<?>, Class<?>> getMixins() {
            return Collections.unmodifiableMap(mixins);
        }

        /**
         * Returns the registered key adapters.
         * 返回注册的键适配器。
         *
         * @return unmodifiable map of key adapters - 不可修改的键适配器映射
         */
        public Map<Class<?>, JsonTypeAdapter<?>> getKeyAdapters() {
            return Collections.unmodifiableMap(keyAdapters);
        }

        @Override
        public void setupModule(SetupContext context) {
            for (JsonTypeAdapter<?> adapter : adapters) {
                context.addTypeAdapter(adapter);
            }
            for (JsonTypeAdapterFactory factory : factories) {
                context.addTypeAdapterFactory(factory);
            }
            for (Map.Entry<Class<?>, Class<?>> entry : mixins.entrySet()) {
                context.addMixin(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<Class<?>, JsonTypeAdapter<?>> entry : keyAdapters.entrySet()) {
                context.addKeyAdapter(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public String toString() {
            return "SimpleModule{name='" + name + "', version='" + version + "'}";
        }
    }
}
