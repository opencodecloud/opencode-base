
package cloud.opencode.base.json.identity;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Object ID Generators - Common Object Identity Generator Implementations
 * 对象 ID 生成器集合 - 常用对象身份生成器实现
 *
 * <p>Provides built-in generator implementations for common identity strategies:
 * auto-incrementing integers, UUIDs, property-based IDs, and string IDs.</p>
 * <p>提供常用身份策略的内置生成器实现：自增整数、UUID、基于属性的 ID 和字符串 ID。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
 * public class Node {
 *     private String name;
 *     private List<Node> children;
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: IntSequenceGenerator uses AtomicInteger - 线程安全: IntSequenceGenerator 使用 AtomicInteger</li>
 *   <li>Null-safe: Generators accept null forPojo where applicable - 空值安全: 生成器在适用时接受空 forPojo</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ObjectIdGenerator
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class ObjectIdGenerators {

    private ObjectIdGenerators() {
        // Utility class, no instantiation
        // 工具类，禁止实例化
    }

    /**
     * Integer Sequence Generator - Auto-Incrementing Integer IDs
     * 整数序列生成器 - 自增整数 ID
     *
     * <p>Generates sequential integer identifiers starting from 1.
     * Thread-safe via {@link AtomicInteger}.</p>
     * <p>从 1 开始生成连续整数标识符。通过 {@link AtomicInteger} 保证线程安全。</p>
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-json V1.0.0
     */
    public static class IntSequenceGenerator extends ObjectIdGenerator<Integer> {

        private final AtomicInteger nextValue = new AtomicInteger(1);
        private final Class<?> scope;

        /**
         * Creates a generator with default scope ({@code Object.class}).
         * 创建具有默认作用域（{@code Object.class}）的生成器。
         */
        public IntSequenceGenerator() {
            this(Object.class);
        }

        /**
         * Creates a generator with the specified scope.
         * 创建具有指定作用域的生成器。
         *
         * @param scope the scope class - 作用域类
         */
        public IntSequenceGenerator(Class<?> scope) {
            this.scope = scope;
        }

        @Override
        public Integer generateId(Object forPojo) {
            return nextValue.getAndIncrement();
        }

        @Override
        public Class<?> getScope() {
            return scope;
        }

        @Override
        public boolean canUseFor(ObjectIdGenerator<?> gen) {
            return gen != null && gen.getClass() == getClass() && gen.getScope() == scope;
        }
    }

    /**
     * UUID Generator - UUID-Based Object IDs
     * UUID 生成器 - 基于 UUID 的对象 ID
     *
     * <p>Generates random {@link UUID} identifiers for each object.</p>
     * <p>为每个对象生成随机 {@link UUID} 标识符。</p>
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-json V1.0.0
     */
    public static class UUIDGenerator extends ObjectIdGenerator<UUID> {

        private final Class<?> scope;

        /**
         * Creates a generator with default scope ({@code Object.class}).
         * 创建具有默认作用域（{@code Object.class}）的生成器。
         */
        public UUIDGenerator() {
            this(Object.class);
        }

        /**
         * Creates a generator with the specified scope.
         * 创建具有指定作用域的生成器。
         *
         * @param scope the scope class - 作用域类
         */
        public UUIDGenerator(Class<?> scope) {
            this.scope = scope;
        }

        @Override
        public UUID generateId(Object forPojo) {
            return UUID.randomUUID();
        }

        @Override
        public Class<?> getScope() {
            return scope;
        }

        @Override
        public boolean canUseFor(ObjectIdGenerator<?> gen) {
            return gen != null && gen.getClass() == getClass() && gen.getScope() == scope;
        }
    }

    /**
     * Property Generator - Uses an Existing Property as Object ID
     * 属性生成器 - 使用现有属性作为对象 ID
     *
     * <p>A marker generator indicating that an existing property of the object
     * should be used as its identity. The actual value is extracted from the
     * property rather than generated.</p>
     * <p>一个标记生成器，指示应使用对象的现有属性作为其身份。
     * 实际值从属性中提取而非生成。</p>
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-json V1.0.0
     */
    public static class PropertyGenerator extends ObjectIdGenerator<Object> {

        private final Class<?> scope;

        /**
         * Creates a generator with default scope ({@code Object.class}).
         * 创建具有默认作用域（{@code Object.class}）的生成器。
         */
        public PropertyGenerator() {
            this(Object.class);
        }

        /**
         * Creates a generator with the specified scope.
         * 创建具有指定作用域的生成器。
         *
         * @param scope the scope class - 作用域类
         */
        public PropertyGenerator(Class<?> scope) {
            this.scope = scope;
        }

        @Override
        public Object generateId(Object forPojo) {
            throw new UnsupportedOperationException(
                    "PropertyGenerator should not be called directly; "
                            + "the property value is used as-is / "
                            + "PropertyGenerator 不应直接调用；属性值直接使用");
        }

        @Override
        public Class<?> getScope() {
            return scope;
        }

        @Override
        public boolean canUseFor(ObjectIdGenerator<?> gen) {
            return gen != null && gen.getClass() == getClass() && gen.getScope() == scope;
        }
    }

    /**
     * String ID Generator - String-Based Object IDs
     * 字符串 ID 生成器 - 基于字符串的对象 ID
     *
     * <p>Generates string identifiers using UUID-based random strings.</p>
     * <p>使用基于 UUID 的随机字符串生成字符串标识符。</p>
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-json V1.0.0
     */
    public static class StringIdGenerator extends ObjectIdGenerator<String> {

        private final Class<?> scope;

        /**
         * Creates a generator with default scope ({@code Object.class}).
         * 创建具有默认作用域（{@code Object.class}）的生成器。
         */
        public StringIdGenerator() {
            this(Object.class);
        }

        /**
         * Creates a generator with the specified scope.
         * 创建具有指定作用域的生成器。
         *
         * @param scope the scope class - 作用域类
         */
        public StringIdGenerator(Class<?> scope) {
            this.scope = scope;
        }

        @Override
        public String generateId(Object forPojo) {
            return UUID.randomUUID().toString();
        }

        @Override
        public Class<?> getScope() {
            return scope;
        }

        @Override
        public boolean canUseFor(ObjectIdGenerator<?> gen) {
            return gen != null && gen.getClass() == getClass() && gen.getScope() == scope;
        }
    }
}
