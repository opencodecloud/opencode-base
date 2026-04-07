/**
 * OpenCode Base Reflect Module
 * OpenCode 基础反射模块
 *
 * <p>Provides comprehensive Java reflection utilities with type safety,
 * fluent API, and JDK 25 features support.</p>
 * <p>提供全面的 Java 反射工具，支持类型安全、流畅 API 和 JDK 25 特性。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>TypeToken - Generic type capture (similar to Guava) - 泛型类型捕获</li>
 *   <li>Invokable - Fluent method/constructor wrapper - 流畅方法/构造器包装</li>
 *   <li>ClassPath/Scanner - Class scanning - 类路径扫描</li>
 *   <li>PropertyAccessor - High-performance access (VarHandle/MethodHandle) - 高性能访问</li>
 *   <li>BeanCopier - Bean property copying - Bean属性复制</li>
 *   <li>Record Support - Record operations - Record操作</li>
 *   <li>Sealed Class Support - Sealed class operations - 密封类操作</li>
 *   <li>Lambda Support - Method reference parsing - Lambda/方法引用解析</li>
 *   <li>LambdaAccessor - Near-zero-cost property access via LambdaMetafactory - 基于LambdaMetafactory的近零开销属性访问</li>
 *   <li>SealedDispatcher - Type-safe sealed class dispatch - 类型安全密封类分发</li>
 *   <li>RecordMapper - Advanced Record/Bean mapping - 高级Record/Bean映射</li>
 *   <li>AnnotationMerger - Composed annotation attribute resolution - 组合注解属性解析</li>
 *   <li>ModuleUtil - Module-system-aware reflection - 模块系统感知反射</li>
 *   <li>MethodSignature - Method signature matching and override detection - 方法签名匹配和覆盖检测</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
module cloud.opencode.base.reflect {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.reflect;
    exports cloud.opencode.base.reflect.accessor;
    exports cloud.opencode.base.reflect.bean;
    exports cloud.opencode.base.reflect.exception;
    exports cloud.opencode.base.reflect.invokable;
    exports cloud.opencode.base.reflect.lambda;
    exports cloud.opencode.base.reflect.proxy;
    exports cloud.opencode.base.reflect.record;
    exports cloud.opencode.base.reflect.scan;
    exports cloud.opencode.base.reflect.sealed;
    exports cloud.opencode.base.reflect.module;
    exports cloud.opencode.base.reflect.type;
}
