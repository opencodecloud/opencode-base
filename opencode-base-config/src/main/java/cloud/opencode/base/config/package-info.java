/**
 * Configuration Management Core Package
 * 配置管理核心包
 *
 * <p>This package provides unified configuration management capabilities including
 * multi-source configuration loading, type-safe access, hot reload, and configuration binding.</p>
 * <p>此包提供统一的配置管理能力，包括多配置源加载、类型安全访问、热更新和配置绑定。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multi-source configuration: Properties, YAML, Environment, System Properties, Command Line - 多配置源: Properties、YAML、环境变量、系统属性、命令行</li>
 *   <li>Priority-based source merging - 基于优先级的配置源合并</li>
 *   <li>Type-safe configuration retrieval with 30+ built-in converters - 内置30+转换器的类型安全配置读取</li>
 *   <li>Placeholder resolution (${key} syntax) - 占位符解析 (${key} 语法)</li>
 *   <li>Hot reload with file change monitoring - 文件变更监听的热更新</li>
 *   <li>Configuration validation - 配置验证</li>
 *   <li>Record and POJO binding - Record和POJO绑定</li>
 * </ul>
 *
 * <p><strong>Architecture | 架构概览:</strong></p>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                         Application Layer                        │
 * └─────────────────────────────────────────────────────────────────┘
 *                                 │
 *                                 ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                      OpenConfig Facade                           │
 * │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
 * │  │  get/bind   │  │  Listeners  │  │   Placeholder ${key}    │  │
 * │  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────────┘
 *                                 │
 *         ┌───────────────────────┼───────────────────────┐
 *         ▼                       ▼                       ▼
 * ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
 * │  Converters     │   │  ConfigBinder   │   │  Validators     │
 * └─────────────────┘   └─────────────────┘   └─────────────────┘
 * </pre>
 *
 * <p><strong>Quick Start | 快速开始:</strong></p>
 * <pre>{@code
 * // Use default configuration
 * String dbUrl = OpenConfig.getString("database.url");
 * int port = OpenConfig.getInt("server.port", 8080);
 *
 * // Build custom configuration
 * Config config = OpenConfig.builder()
 *     .addClasspathResource("application.properties")
 *     .addEnvironmentVariables()
 *     .addSystemProperties()
 *     .enableHotReload()
 *     .build();
 * }</pre>
 *
 * <p><strong>Package Structure | 包结构:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.config.source} - Configuration sources - 配置源</li>
 *   <li>{@link cloud.opencode.base.config.converter} - Type converters - 类型转换器</li>
 *   <li>{@link cloud.opencode.base.config.bind} - Configuration binding - 配置绑定</li>
 *   <li>{@link cloud.opencode.base.config.validation} - Configuration validation - 配置验证</li>
 *   <li>{@link cloud.opencode.base.config.placeholder} - Placeholder resolution - 占位符解析</li>
 *   <li>{@link cloud.opencode.base.config.jdk25} - JDK 25 enhanced features - JDK 25增强特性</li>
 *   <li>{@link cloud.opencode.base.config.advanced} - Advanced features and SPI - 高级功能和SPI</li>
 * </ul>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>All public APIs are thread-safe - 所有公开API都是线程安全的</li>
 *   <li>Configuration snapshots are immutable - 配置快照不可变</li>
 *   <li>Hot reload uses atomic reference switching - 热更新使用原子引用切换</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
package cloud.opencode.base.config;
