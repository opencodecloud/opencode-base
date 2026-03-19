/**
 * Internal Implementation Package
 * 内部实现包
 *
 * <p><strong>WARNING: Internal API - Do Not Use Directly</strong></p>
 * <p><strong>警告: 内部API - 请勿直接使用</strong></p>
 *
 * <p>This package contains internal implementation classes that are not part of
 * the public API. These classes may change without notice between versions.</p>
 * <p>此包包含非公开API的内部实现类。这些类可能在版本间无通知变更。</p>
 *
 * <p><strong>Internal Components | 内部组件:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.config.internal.DefaultConfig} - Default Config implementation - 默认Config实现</li>
 *   <li>{@link cloud.opencode.base.config.internal.ConfigWatcher} - Configuration file watcher - 配置文件监视器</li>
 * </ul>
 *
 * <p><strong>For Public API | 公开API:</strong></p>
 * <p>Use the classes in the main {@link cloud.opencode.base.config} package:</p>
 * <ul>
 *   <li>{@link cloud.opencode.base.config.OpenConfig} - Main entry point</li>
 *   <li>{@link cloud.opencode.base.config.Config} - Configuration interface</li>
 *   <li>{@link cloud.opencode.base.config.ConfigBuilder} - Configuration builder</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
package cloud.opencode.base.config.internal;
