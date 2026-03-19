/**
 * OpenCode Base Tree Module
 * OpenCode 基础树模块
 *
 * <p>Provides tree structure processing capabilities including
 * list-to-tree conversion, traversal, search, filtering, and diff.</p>
 * <p>提供树形结构处理能力，包括列表转树、遍历、查找、过滤和差异计算。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>OpenTree - Main facade for tree operations - 树操作门面</li>
 *   <li>List to Tree - One-line conversion from flat list - 一行代码列表转树</li>
 *   <li>Traversal - PreOrder, PostOrder, LevelOrder, Iterative - 遍历算法</li>
 *   <li>Search - Find by ID, findAll, getPath, getLeaves - 查找</li>
 *   <li>Filter - Keep matching nodes with ancestors - 过滤（保留祖先链）</li>
 *   <li>TreePrinter - ASCII art tree printing - 树形打印</li>
 *   <li>TreeDiff - Tree difference calculation - 树差异计算</li>
 *   <li>CycleDetector - Cycle detection in tree - 循环检测</li>
 *   <li>TreeNodeValidator - Node validation - 节点验证</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-tree V1.0.0
 */
module cloud.opencode.base.tree {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.tree;
    exports cloud.opencode.base.tree.builder;
    exports cloud.opencode.base.tree.diff;
    exports cloud.opencode.base.tree.exception;
    exports cloud.opencode.base.tree.operation;
    exports cloud.opencode.base.tree.path;
    exports cloud.opencode.base.tree.result;
    exports cloud.opencode.base.tree.serialization;
    exports cloud.opencode.base.tree.traversal;
    exports cloud.opencode.base.tree.validation;
    exports cloud.opencode.base.tree.virtual;
}
