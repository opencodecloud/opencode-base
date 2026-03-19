/**
 * OpenCode Base Rules - Lightweight Rule Engine for Java
 * OpenCode基础规则 - Java轻量级规则引擎
 *
 * <p>This package provides the core interfaces and classes for the rule engine.</p>
 * <p>此包提供规则引擎的核心接口和类。</p>
 *
 * <p><strong>Core Components | 核心组件:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.rules.OpenRules} - Main facade | 主门面</li>
 *   <li>{@link cloud.opencode.base.rules.Rule} - Rule interface | 规则接口</li>
 *   <li>{@link cloud.opencode.base.rules.RuleEngine} - Engine interface | 引擎接口</li>
 *   <li>{@link cloud.opencode.base.rules.RuleContext} - Execution context | 执行上下文</li>
 *   <li>{@link cloud.opencode.base.rules.RuleResult} - Execution result | 执行结果</li>
 * </ul>
 *
 * <p><strong>Quick Start | 快速开始:</strong></p>
 * <pre>{@code
 * // Create a rule
 * Rule rule = OpenRules.rule("discount-rule")
 *     .when(ctx -> "VIP".equals(ctx.get("customerType")))
 *     .then(ctx -> ctx.put("discount", 0.15))
 *     .build();
 *
 * // Create engine and fire rules
 * RuleEngine engine = OpenRules.engineWith(rule);
 * RuleContext context = OpenRules.contextOf("customerType", "VIP");
 * RuleResult result = engine.fire(context);
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
package cloud.opencode.base.rules;
