/**
 * Configuration Validation Package
 * 配置验证包
 *
 * <p>This package provides configuration validation capabilities for
 * ensuring configuration values meet specified requirements.</p>
 * <p>此包提供配置验证能力，确保配置值满足指定要求。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Required field validation - 必填字段验证</li>
 *   <li>Range validation - 范围验证</li>
 *   <li>Pattern validation - 模式验证</li>
 *   <li>Custom validator support - 自定义验证器支持</li>
 *   <li>Validation result aggregation - 验证结果聚合</li>
 * </ul>
 *
 * <p><strong>Components | 组件:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.config.validation.ConfigValidator} - Validator interface (SPI) - 验证器接口</li>
 *   <li>{@link cloud.opencode.base.config.validation.RequiredValidator} - Required field validator - 必填验证器</li>
 *   <li>{@link cloud.opencode.base.config.validation.ValidationResult} - Validation result - 验证结果</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Add validation during build
 * Config config = OpenConfig.builder()
 *     .addClasspathResource("application.properties")
 *     .required("database.url", "database.username", "database.password")
 *     .addValidator(new RangeValidator("server.port", 1024, 65535))
 *     .build();
 *
 * // Custom validator
 * public class PortValidator implements ConfigValidator {
 *     @Override
 *     public ValidationResult validate(Config config) {
 *         int port = config.getInt("server.port", 8080);
 *         if (port < 1024 || port > 65535) {
 *             return ValidationResult.failure("Port must be between 1024 and 65535");
 *         }
 *         return ValidationResult.success();
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Validation Failure Behavior | 验证失败行为:</strong></p>
 * <p>When validation fails during {@code ConfigBuilder.build()}, an
 * {@link cloud.opencode.base.config.OpenConfigException} is thrown with
 * aggregated error messages.</p>
 * <p>当在{@code ConfigBuilder.build()}期间验证失败时，将抛出
 * {@link cloud.opencode.base.config.OpenConfigException}，包含聚合的错误消息。</p>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
package cloud.opencode.base.config.validation;
