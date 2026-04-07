/**
 * OpenCode Base ClassLoader Module
 * OpenCode 基础类加载器模块
 *
 * <p>Provides class loading, scanning, and metadata inspection utilities based on JDK 25,
 * supporting dynamic class loading, resource scanning, and annotation processing.</p>
 * <p>提供基于 JDK 25 的类加载、扫描和元数据检查工具，支持动态类加载、资源扫描和注解处理。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Dynamic Class Loading - 动态类加载</li>
 *   <li>Classpath Resource Scanning - 类路径资源扫描</li>
 *   <li>Class Metadata Inspection - 类元数据检查</li>
 *   <li>Custom ClassLoader Implementations - 自定义类加载器</li>
 *   <li>Annotation Scanner - 注解扫描器</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
module cloud.opencode.base.classloader {
    // Required modules
    requires transitive cloud.opencode.base.core;
    requires java.sql;

    // Export public API packages
    exports cloud.opencode.base.classloader;
    exports cloud.opencode.base.classloader.exception;
    exports cloud.opencode.base.classloader.loader;
    exports cloud.opencode.base.classloader.metadata;
    exports cloud.opencode.base.classloader.resource;
    exports cloud.opencode.base.classloader.scanner;
    exports cloud.opencode.base.classloader.leak;
    exports cloud.opencode.base.classloader.security;
    exports cloud.opencode.base.classloader.index;
    exports cloud.opencode.base.classloader.graalvm;
    exports cloud.opencode.base.classloader.plugin;
    exports cloud.opencode.base.classloader.diagnostic;
    exports cloud.opencode.base.classloader.service;
    exports cloud.opencode.base.classloader.dependency;
    exports cloud.opencode.base.classloader.conflict;
}
