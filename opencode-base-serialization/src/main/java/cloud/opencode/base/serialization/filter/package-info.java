/**
 * Deserialization Class Filter - Security filtering for deserialization
 * 反序列化类过滤器 - 反序列化安全过滤
 *
 * <p>Provides a unified class filtering mechanism to prevent deserialization of dangerous classes.
 * Similar in purpose to Jackson's PolymorphicTypeValidator and Kryo's ClassResolver,
 * but designed as a standalone, framework-agnostic security layer.</p>
 * <p>提供统一的类过滤机制，防止危险类的反序列化。
 * 类似于 Jackson 的 PolymorphicTypeValidator 和 Kryo 的 ClassResolver，
 * 但设计为独立的、框架无关的安全层。</p>
 *
 * <p><strong>Key Components | 核心组件:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.serialization.filter.ClassFilter} -
 *       Functional interface for class filtering - 类过滤函数式接口</li>
 *   <li>{@link cloud.opencode.base.serialization.filter.ClassFilterBuilder} -
 *       Builder for creating composite filters - 组合过滤器构建器</li>
 *   <li>{@link cloud.opencode.base.serialization.filter.DefaultClassFilter} -
 *       Pre-built secure filters - 预置安全过滤器</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.3
 */
package cloud.opencode.base.serialization.filter;
