/**
 * Pattern Matching - Enhanced pattern matching utilities
 * 模式匹配 - 增强的模式匹配工具
 *
 * <p>Provides pattern matching utilities that complement JDK 25's native
 * pattern matching with additional features for complex matching scenarios.</p>
 * <p>提供补充 JDK 25 原生模式匹配的工具，支持复杂匹配场景的附加功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.functional.pattern.OpenMatch} - Pattern matching entry point</li>
 *   <li>{@link cloud.opencode.base.functional.pattern.Case} - Match case definition</li>
 *   <li>{@link cloud.opencode.base.functional.pattern.Pattern} - Pattern interface</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Type matching
 * String result = OpenMatch.of(value)
 *     .caseOf(String.class, s -> "String: " + s)
 *     .caseOf(Integer.class, n -> "Number: " + n)
 *     .when(Objects::isNull, o -> "null")
 *     .orElse(o -> "Unknown");
 *
 * // With JDK 25 native pattern matching
 * double area = switch (shape) {
 *     case Circle(var r) -> Math.PI * r * r;
 *     case Rectangle(var w, var h) -> w * h;
 * };
 * }</pre>
 *
 * <p><strong>JDK 25 Integration | JDK 25 集成:</strong></p>
 * <p>Works alongside JDK 25's pattern matching for switch and instanceof.</p>
 * <p>与 JDK 25 的 switch 和 instanceof 模式匹配协同工作。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
package cloud.opencode.base.functional.pattern;
