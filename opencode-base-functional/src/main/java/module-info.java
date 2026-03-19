/**
 * OpenCode Base Functional Module
 * OpenCode 基础函数式编程模块
 *
 * <p>Provides functional programming utilities based on JDK 25 features,
 * including monads, pattern matching, optics, pipelines, and async compositions.</p>
 * <p>提供基于 JDK 25 特性的函数式编程工具，包括单子、模式匹配、光学、管道和异步组合。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Monads (Try, Either, Option) - 单子类型</li>
 *   <li>Pattern Matching - 模式匹配</li>
 *   <li>Optics (Lens, Prism) - 光学抽象</li>
 *   <li>Async Composition - 异步组合</li>
 *   <li>Pipeline Builder - 管道构建器</li>
 *   <li>Function Combinators - 函数组合器</li>
 *   <li>Record Utilities - Record 工具</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
module cloud.opencode.base.functional {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.functional;
    exports cloud.opencode.base.functional.async;
    exports cloud.opencode.base.functional.exception;
    exports cloud.opencode.base.functional.function;
    exports cloud.opencode.base.functional.monad;
    exports cloud.opencode.base.functional.optics;
    exports cloud.opencode.base.functional.pattern;
    exports cloud.opencode.base.functional.pipeline;
    exports cloud.opencode.base.functional.record;
}
