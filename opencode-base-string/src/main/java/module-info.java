/**
 * OpenCode Base String Module
 * OpenCode 基础字符串模块
 *
 * <p>Provides comprehensive string processing capabilities including
 * naming conversion, template engine, text comparison, regex, Unicode handling, and data masking.</p>
 * <p>提供全面的字符串处理能力，包括命名转换、模板引擎、文本比较、正则、Unicode处理和数据脱敏。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>OpenString - Enhanced string operations - 字符串增强操作</li>
 *   <li>OpenNaming - Naming case conversion (camel, snake, kebab) - 命名风格转换</li>
 *   <li>OpenTemplate - Template engine with variables, conditions, loops - 模板引擎</li>
 *   <li>OpenMask - Data desensitization (phone, ID, email, card) - 数据脱敏</li>
 *   <li>OpenSimilarity - Text similarity (Levenshtein, Jaccard, Cosine) - 相似度计算</li>
 *   <li>OpenFuzzyMatch - Fuzzy matching and search suggestions - 模糊匹配和搜索建议</li>
 *   <li>AhoCorasick - Multi-pattern matching for sensitive word filtering - 多模式匹配（敏感词过滤）</li>
 *   <li>OpenDiff - Text difference comparison - 文本差异对比</li>
 *   <li>OpenRegex - Regex pattern building - 正则表达式构建</li>
 *   <li>Joiner/Splitter/CharMatcher - Guava-style builders - Guava风格构建器</li>
 *   <li>OpenEscape - HTML/Java/SQL escaping - 转义处理</li>
 *   <li>OpenUnicode - Full-width/Chinese processing - Unicode处理</li>
 *   <li>OpenGrapheme - Grapheme cluster operations (emoji-safe) - 字素簇操作（emoji安全）</li>
 *   <li>OpenSlug - URL-friendly slug generation - URL友好别名生成</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-string V1.0.0
 */
module cloud.opencode.base.string {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.string;
    exports cloud.opencode.base.string.abbr;
    exports cloud.opencode.base.string.builder;
    exports cloud.opencode.base.string.desensitize;
    exports cloud.opencode.base.string.desensitize.annotation;
    exports cloud.opencode.base.string.desensitize.exception;
    exports cloud.opencode.base.string.desensitize.handler;
    exports cloud.opencode.base.string.desensitize.strategy;
    exports cloud.opencode.base.string.diff;
    exports cloud.opencode.base.string.escape;
    exports cloud.opencode.base.string.exception;
    exports cloud.opencode.base.string.format;
    exports cloud.opencode.base.string.naming;
    exports cloud.opencode.base.string.parse;
    exports cloud.opencode.base.string.regex;
    exports cloud.opencode.base.string.similarity;
    exports cloud.opencode.base.string.match;
    exports cloud.opencode.base.string.template;
    exports cloud.opencode.base.string.template.node;
    exports cloud.opencode.base.string.text;
    exports cloud.opencode.base.string.unicode;
    exports cloud.opencode.base.string.codec;
}
