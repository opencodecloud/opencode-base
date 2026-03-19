/**
 * JDK 25 Enhanced Features Package
 * JDK 25增强特性包
 *
 * <p>This package provides advanced configuration features leveraging JDK 25
 * language and API enhancements.</p>
 * <p>此包提供利用JDK 25语言和API增强的高级配置特性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Record configuration binding - Record配置绑定</li>
 *   <li>Sealed configuration source types - 密封配置源类型</li>
 *   <li>Virtual thread hot reload - 虚拟线程热更新</li>
 *   <li>ScopedValue configuration context - ScopedValue配置上下文</li>
 *   <li>Reactive configuration values - 响应式配置值</li>
 *   <li>Pattern matching for configuration - 配置的模式匹配</li>
 * </ul>
 *
 * <p><strong>Components | 组件:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.config.jdk25.ConfigSourceType} - Sealed source types - 密封配置源类型</li>
 *   <li>{@link cloud.opencode.base.config.jdk25.ConfigSourceProcessor} - Pattern matching processor - 模式匹配处理器</li>
 *   <li>{@link cloud.opencode.base.config.jdk25.ConfigContext} - ScopedValue context - ScopedValue上下文</li>
 *   <li>{@link cloud.opencode.base.config.jdk25.ContextAwareConfig} - Context-aware configuration - 上下文感知配置</li>
 *   <li>{@link cloud.opencode.base.config.jdk25.VirtualThreadConfigWatcher} - Virtual thread watcher - 虚拟线程监视器</li>
 *   <li>{@link cloud.opencode.base.config.jdk25.ReactiveConfigValue} - Reactive config values - 响应式配置值</li>
 *   <li>{@link cloud.opencode.base.config.jdk25.Required} - Required annotation for Records - Record必填注解</li>
 *   <li>{@link cloud.opencode.base.config.jdk25.DefaultValue} - Default value annotation for Records - Record默认值注解</li>
 * </ul>
 *
 * <p><strong>Record Binding Example | Record绑定示例:</strong></p>
 * <pre>{@code
 * record DatabaseConfig(
 *     @Required String url,
 *     @Required String username,
 *     @Required String password,
 *     @DefaultValue("10") int maxPoolSize,
 *     @DefaultValue("30s") Duration connectionTimeout,
 *     PoolConfig pool
 * ) {}
 *
 * DatabaseConfig config = OpenConfig.bind("database", DatabaseConfig.class);
 * }</pre>
 *
 * <p><strong>ScopedValue Context Example | ScopedValue上下文示例:</strong></p>
 * <pre>{@code
 * ConfigContext.withTenant("tenant-1", () -> {
 *     // Configuration reads prioritize tenant-specific values
 *     String apiKey = OpenConfig.getString("api.key");
 *     return processRequest(request, apiKey);
 * });
 *
 * ConfigContext.withOverrides(Map.of("database.url", "jdbc:h2:mem:test"), () -> {
 *     // Temporary configuration override for testing
 *     runTests();
 *     return null;
 * });
 * }</pre>
 *
 * <p><strong>Reactive Configuration Example | 响应式配置示例:</strong></p>
 * <pre>{@code
 * ReactiveConfigValue<String> logLevel = ReactiveConfigValue
 *     .ofString(config, "log.level")
 *     .subscribe(level -> updateLogLevel(level));
 *
 * // When config changes, subscriber is notified automatically
 * String current = logLevel.get();
 * }</pre>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
package cloud.opencode.base.config.jdk25;
