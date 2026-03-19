/**
 * OpenCode Base IO Module
 * OpenCode 基础 IO 模块
 *
 * <p>Provides file I/O utilities based on NIO.2 API with unchecked exceptions.</p>
 * <p>提供基于 NIO.2 API 的文件 I/O 工具，使用非受检异常。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>File and directory operations - 文件和目录操作</li>
 *   <li>Resource loading (classpath/file/URL) - 资源加载</li>
 *   <li>Stream processing utilities - 流处理工具</li>
 *   <li>Checksum calculation - 校验和计算</li>
 *   <li>Temporary file management - 临时文件管理</li>
 *   <li>File watching - 文件监听</li>
 *   <li>MIME type detection - MIME 类型检测</li>
 *   <li>Batch/parallel operations - 批量/并行操作</li>
 *   <li>File locking utilities - 文件锁定工具</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-io V1.0.0
 */
module cloud.opencode.base.io {
    // Required modules
    requires transitive cloud.opencode.base.core;

    // Export public API packages
    exports cloud.opencode.base.io;
    exports cloud.opencode.base.io.batch;
    exports cloud.opencode.base.io.checksum;
    exports cloud.opencode.base.io.exception;
    exports cloud.opencode.base.io.file;
    exports cloud.opencode.base.io.lock;
    exports cloud.opencode.base.io.resource;
    exports cloud.opencode.base.io.stream;
    exports cloud.opencode.base.io.temp;
    exports cloud.opencode.base.io.progress;
    exports cloud.opencode.base.io.serialization;
}
