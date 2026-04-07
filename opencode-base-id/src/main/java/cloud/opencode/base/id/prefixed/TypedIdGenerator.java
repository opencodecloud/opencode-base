package cloud.opencode.base.id.prefixed;

import cloud.opencode.base.id.IdGenerator;
import cloud.opencode.base.id.exception.OpenIdGenerationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Typed ID Generator - Wraps any string ID generator with a validated type prefix
 * 类型化ID生成器 - 用验证过的类型前缀包装任意字符串ID生成器
 *
 * <p>Produces IDs in the format {@code <prefix>_<rawId>}, making each ID self-describing.
 * This pattern (popularized by Stripe) embeds the entity type in the ID itself,
 * preventing accidental cross-entity ID usage and simplifying debugging.</p>
 * <p>生成格式为{@code <prefix>_<rawId>}的ID，使每个ID具有自描述性。
 * 这种模式（由Stripe推广）将实体类型嵌入ID本身，防止跨实体ID误用，简化调试。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Prefix validation at construction time - 构造时验证前缀</li>
 *   <li>Delegates to any {@link IdGenerator}{@code <String>} - 委托给任意字符串ID生成器</li>
 *   <li>Thread-safe if the inner generator is thread-safe - 内部生成器线程安全时本类也线程安全</li>
 *   <li>Returns both {@link String} and {@link PrefixedId} forms - 同时支持String和PrefixedId形式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create typed generators
 * TypedIdGenerator userGen = TypedIdGenerator.of("usr",
 *     NanoIdGenerator.create());
 * TypedIdGenerator orderGen = TypedIdGenerator.of("order",
 *     UlidGenerator.create());
 *
 * // Generate IDs
 * String userId  = userGen.generate();           // "usr_V1StGXR8_Z5jdHi6B-myT"
 * PrefixedId oid = orderGen.generatePrefixed();  // prefix="order", rawId="01ARZ3NDEK..."
 *
 * System.out.println(userId);           // "usr_V1StGXR8_Z5jdHi6B-myT"
 * System.out.println(oid);             // "order_01ARZ3NDEK..."
 * System.out.println(oid.prefix());    // "order"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Same as underlying generator - 线程安全: 与底层生成器相同</li>
 *   <li>Null-safe: No (throws on null) - 空值安全: 否（空值抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.3
 */
public final class TypedIdGenerator implements IdGenerator<String> {

    private final String prefix;
    private final IdGenerator<String> inner;

    private TypedIdGenerator(String prefix, IdGenerator<String> inner) {
        this.prefix = prefix;
        this.inner = inner;
    }

    /**
     * Creates a TypedIdGenerator with the given prefix and inner generator
     * 使用给定前缀和内部生成器创建TypedIdGenerator
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * TypedIdGenerator.of("usr",   UlidGenerator.create())  // user IDs
     * TypedIdGenerator.of("order", NanoIdGenerator.create()) // order IDs
     * </pre>
     *
     * @param prefix the entity-type prefix (lowercase, e.g., "usr", "order") | 实体类型前缀（小写，如"usr"、"order"）
     * @param inner  the underlying string ID generator | 底层字符串ID生成器
     * @return TypedIdGenerator instance | TypedIdGenerator实例
     * @throws OpenIdGenerationException if prefix is invalid or inner is null | 前缀无效或inner为null时抛出
     */
    public static TypedIdGenerator of(String prefix, IdGenerator<String> inner) {
        if (prefix == null || !PrefixedId.PREFIX_PATTERN.matcher(prefix).matches()) {
            throw OpenIdGenerationException.invalidPrefix(prefix == null ? "null" : prefix);
        }
        if (inner == null) {
            throw OpenIdGenerationException.invalidIdFormat("TypedIdGenerator",
                    "inner generator must not be null");
        }
        return new TypedIdGenerator(prefix, inner);
    }

    /**
     * Generates a prefixed ID string (e.g., "usr_01ARZ3NDEK...")
     * 生成带前缀的ID字符串（如"usr_01ARZ3NDEK..."）
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * TypedIdGenerator.of("usr", ulidGen).generate() = "usr_01ARZ3NDEKTSV4RRFFQ69G5FAV"
     * TypedIdGenerator.of("order", nanoGen).generate() = "order_V1StGXR8_Z5jdHi6B-myT"
     * </pre>
     *
     * <p><strong>Performance | 性能:</strong></p>
     * <p>Time: O(inner.generate()) + O(prefix.length()), Space: O(1)</p>
     *
     * @return prefixed ID string | 带前缀的ID字符串
     */
    @Override
    public String generate() {
        return prefix + "_" + inner.generate();
    }

    /**
     * Generates a batch of prefixed ID strings
     * 批量生成带前缀的ID字符串
     *
     * @param count number of IDs to generate | 要生成的ID数量
     * @return list of prefixed ID strings | 带前缀的ID字符串列表
     */
    @Override
    public List<String> generateBatch(int count) {
        if (count <= 0) {
            return List.of();
        }
        List<String> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(generate());
        }
        return result;
    }

    /**
     * Generates a PrefixedId (structured form with accessible prefix and rawId)
     * 生成PrefixedId（带可访问前缀和原始ID的结构化形式）
     *
     * @return PrefixedId instance | PrefixedId实例
     */
    public PrefixedId generatePrefixed() {
        return PrefixedId.of(prefix, inner.generate());
    }

    /**
     * Returns the type prefix of this generator
     * 返回此生成器的类型前缀
     *
     * @return prefix string | 前缀字符串
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the type identifier including prefix
     * 返回包含前缀的类型标识符
     *
     * @return type string | 类型字符串
     */
    @Override
    public String getType() {
        return "TypedId[" + prefix + "]";
    }
}
