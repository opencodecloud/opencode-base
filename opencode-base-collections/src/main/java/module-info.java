/**
 * OpenCode Base Collections Module
 * OpenCode 基础集合模块
 *
 * <p>Provides advanced collection data structures based on JDK 25,
 * including concurrent collections, immutable wrappers, primitive collections,
 * trees, graphs, and transformation utilities.</p>
 * <p>提供基于 JDK 25 的高级集合数据结构，包括并发集合、不可变包装、原始类型集合、
 * 树、图和转换工具。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Concurrent Collections - 并发集合</li>
 *   <li>Immutable Collections - 不可变集合</li>
 *   <li>Primitive Collections (int/long/double) - 原始类型集合</li>
 *   <li>Specialized Structures (Trie, Skip List, AVL) - 特殊数据结构</li>
 *   <li>Collection Transformations - 集合转换</li>
 *   <li>Tree Collections - 树形集合</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
module cloud.opencode.base.collections {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.collections;
    exports cloud.opencode.base.collections.concurrent;
    exports cloud.opencode.base.collections.exception;
    exports cloud.opencode.base.collections.graph;
    exports cloud.opencode.base.collections.immutable;
    exports cloud.opencode.base.collections.primitive;
    exports cloud.opencode.base.collections.specialized;
    exports cloud.opencode.base.collections.transform;
    exports cloud.opencode.base.collections.tree;
}
