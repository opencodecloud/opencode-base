/**
 * OpenCode Base Hash Module
 * OpenCode 基础哈希模块
 *
 * <p>Provides hash-based data structures and algorithms based on JDK 25,
 * including Bloom filters, consistent hashing, SimHash, and custom hash functions.</p>
 * <p>提供基于 JDK 25 的哈希数据结构与算法，包括布隆过滤器、一致性哈希、SimHash 和自定义哈希函数。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Bloom Filter - 布隆过滤器</li>
 *   <li>Consistent Hashing - 一致性哈希</li>
 *   <li>SimHash (Near-Duplicate Detection) - SimHash（近似去重）</li>
 *   <li>Custom Hash Functions (MurmurHash, FNV, CRC) - 自定义哈希函数</li>
 *   <li>Hash Ring - 哈希环</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
module cloud.opencode.base.hash {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.hash;
    exports cloud.opencode.base.hash.bloom;
    exports cloud.opencode.base.hash.consistent;
    exports cloud.opencode.base.hash.exception;
    exports cloud.opencode.base.hash.function;
    exports cloud.opencode.base.hash.simhash;
}
