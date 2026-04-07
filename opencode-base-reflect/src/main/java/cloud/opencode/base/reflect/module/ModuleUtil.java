package cloud.opencode.base.reflect.module;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.invoke.MethodHandles;
import java.lang.module.ModuleDescriptor;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Module-system-aware Reflection Utilities
 * 模块系统感知的反射工具类
 *
 * <p>Provides utilities for inspecting and working with the Java Platform Module System (JPMS).
 * Helps determine whether reflective access is permitted between modules, and provides
 * diagnostic information when access fails.</p>
 * <p>提供用于检查和使用 Java 平台模块系统 (JPMS) 的工具。
 * 帮助确定模块之间是否允许反射访问，并在访问失败时提供诊断信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Module accessibility checking - 模块可访问性检查</li>
 *   <li>Package export/open status inspection - 包导出/开放状态检查</li>
 *   <li>Deep reflection capability detection - 深度反射能力检测</li>
 *   <li>Access diagnostic information - 访问诊断信息</li>
 *   <li>MethodHandles.Lookup acquisition - MethodHandles.Lookup 获取</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if a class is accessible from the caller's module
 * boolean accessible = ModuleUtil.isAccessible(String.class, MyClass.class);
 *
 * // Get module information for a class
 * ModuleUtil.ModuleInfo info = ModuleUtil.getModuleInfo(String.class);
 *
 * // Get diagnostic info about why access might fail
 * String diagnostic = ModuleUtil.getAccessDiagnostic(TargetClass.class, CallerClass.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (validates all parameters) - 空值安全: 是（验证所有参数）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for most checks - 时间复杂度: 大部分检查为 O(1)</li>
 *   <li>Space complexity: O(n) for ModuleInfo (n = number of packages) - 空间复杂度: O(n)（n = 包数量）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.3
 */
public final class ModuleUtil {

    /**
     * Private constructor to prevent instantiation
     * 私有构造函数防止实例化
     */
    private ModuleUtil() {
        throw new AssertionError("No ModuleUtil instances for you!");
    }

    // ==================== Module Info Record | 模块信息记录 ====================

    /**
     * Module information for a class
     * 类的模块信息
     *
     * @param moduleName      the module name, or "unnamed" for unnamed modules | 模块名，未命名模块返回 "unnamed"
     * @param packageName     the package name of the class | 类的包名
     * @param isNamed         whether the module is a named module | 是否为命名模块
     * @param isOpen          whether the package is unconditionally open | 包是否无条件开放
     * @param isExported      whether the package is unconditionally exported | 包是否无条件导出
     * @param openPackages    set of packages that are unconditionally open | 无条件开放的包集合
     * @param exportedPackages set of packages that are unconditionally exported | 无条件导出的包集合
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-reflect V1.0.3
     */
    public record ModuleInfo(
            String moduleName,
            String packageName,
            boolean isNamed,
            boolean isOpen,
            boolean isExported,
            Set<String> openPackages,
            Set<String> exportedPackages
    ) {
        /**
         * Creates a ModuleInfo with validation
         * 创建带验证的 ModuleInfo
         */
        public ModuleInfo {
            Objects.requireNonNull(moduleName, "moduleName must not be null");
            Objects.requireNonNull(packageName, "packageName must not be null");
            openPackages = Set.copyOf(openPackages);
            exportedPackages = Set.copyOf(exportedPackages);
        }
    }

    // ==================== Accessibility Checks | 可访问性检查 ====================

    /**
     * Checks if a package in the target class's module is accessible (exported or open) to the caller's module.
     * 检查目标类所在模块的包对调用方模块是否可访问（已导出或已开放）。
     *
     * <p>A package is considered accessible if any of the following is true:</p>
     * <ul>
     *   <li>Both classes are in the same module</li>
     *   <li>The target class is in an unnamed module</li>
     *   <li>The target package is exported to the caller's module</li>
     * </ul>
     *
     * @param targetClass the target class to check accessibility for | 要检查可访问性的目标类
     * @param callerClass the caller class | 调用方类
     * @return true if the target class's package is accessible | 如果目标类的包可访问则返回 true
     * @throws OpenReflectException if targetClass or callerClass is null | 如果参数为 null 则抛出异常
     */
    public static boolean isAccessible(Class<?> targetClass, Class<?> callerClass) {
        validateParams(targetClass, "targetClass");
        validateParams(callerClass, "callerClass");

        Module targetModule = targetClass.getModule();
        Module callerModule = callerClass.getModule();

        // Same module → always accessible
        if (targetModule == callerModule) {
            return true;
        }

        // Unnamed modules are always accessible
        if (!targetModule.isNamed()) {
            return true;
        }

        String packageName = targetClass.getPackageName();
        return targetModule.isExported(packageName, callerModule);
    }

    /**
     * Checks if the target class's package is open to the given module (allows deep reflection).
     * 检查目标类的包是否对给定模块开放（允许深度反射）。
     *
     * <p>An open package allows {@code setAccessible(true)} on its members.</p>
     * <p>开放的包允许对其成员调用 {@code setAccessible(true)}。</p>
     *
     * @param targetClass  the target class | 目标类
     * @param callerModule the caller's module | 调用方的模块
     * @return true if the package is open to the caller module | 如果包对调用方模块开放则返回 true
     * @throws OpenReflectException if targetClass or callerModule is null | 如果参数为 null 则抛出异常
     */
    public static boolean isOpen(Class<?> targetClass, Module callerModule) {
        validateParams(targetClass, "targetClass");
        validateParams(callerModule, "callerModule");

        Module targetModule = targetClass.getModule();

        // Same module → always open
        if (targetModule == callerModule) {
            return true;
        }

        // Unnamed modules are always open
        if (!targetModule.isNamed()) {
            return true;
        }

        String packageName = targetClass.getPackageName();
        return targetModule.isOpen(packageName, callerModule);
    }

    /**
     * Checks if the target class's package is exported to the given module.
     * 检查目标类的包是否已导出到给定模块。
     *
     * <p>Exported packages allow compile-time (public API) access but not deep reflection.</p>
     * <p>导出的包允许编译时（公共 API）访问，但不允许深度反射。</p>
     *
     * @param targetClass  the target class | 目标类
     * @param callerModule the caller's module | 调用方的模块
     * @return true if the package is exported to the caller module | 如果包已导出到调用方模块则返回 true
     * @throws OpenReflectException if targetClass or callerModule is null | 如果参数为 null 则抛出异常
     */
    public static boolean isExported(Class<?> targetClass, Module callerModule) {
        validateParams(targetClass, "targetClass");
        validateParams(callerModule, "callerModule");

        Module targetModule = targetClass.getModule();

        // Same module → always exported
        if (targetModule == callerModule) {
            return true;
        }

        // Unnamed modules are always exported
        if (!targetModule.isNamed()) {
            return true;
        }

        String packageName = targetClass.getPackageName();
        return targetModule.isExported(packageName, callerModule);
    }

    /**
     * Checks if deep reflection ({@code setAccessible(true)}) can work on the target class from the caller.
     * 检查从调用方对目标类进行深度反射（{@code setAccessible(true)}）是否可行。
     *
     * <p>Deep reflection requires the target package to be open to the caller's module,
     * either unconditionally or via a qualified opens directive.</p>
     * <p>深度反射要求目标包对调用方模块开放，可以是无条件开放或通过限定的 opens 指令。</p>
     *
     * @param targetClass the target class | 目标类
     * @param callerClass the caller class | 调用方类
     * @return true if deep reflection is possible | 如果深度反射可行则返回 true
     * @throws OpenReflectException if targetClass or callerClass is null | 如果参数为 null 则抛出异常
     */
    public static boolean canDeepReflect(Class<?> targetClass, Class<?> callerClass) {
        validateParams(targetClass, "targetClass");
        validateParams(callerClass, "callerClass");

        Module targetModule = targetClass.getModule();
        Module callerModule = callerClass.getModule();

        // Same module → always allowed
        if (targetModule == callerModule) {
            return true;
        }

        // Unnamed modules are always open
        if (!targetModule.isNamed()) {
            return true;
        }

        String packageName = targetClass.getPackageName();

        // Check if the package is open to the caller's module
        return targetModule.isOpen(packageName, callerModule);
    }

    // ==================== Diagnostic | 诊断 ====================

    /**
     * Gets diagnostic information about why reflective access might fail between two classes.
     * 获取关于两个类之间反射访问可能失败原因的诊断信息。
     *
     * <p>Returns a human-readable string explaining module relationships, export/open status,
     * and what module directives would be needed for access.</p>
     * <p>返回人类可读的字符串，解释模块关系、导出/开放状态以及访问所需的模块指令。</p>
     *
     * <p><strong>Security Warning | 安全警告:</strong> The returned string contains module topology
     * details (module names, package names, export/open status). Do not expose this output in
     * API responses, user-facing error messages, or unsecured log channels. Use only for
     * internal debugging and development diagnostics.</p>
     * <p>返回的字符串包含模块拓扑详情（模块名、包名、导出/开放状态）。
     * 不要在 API 响应、用户可见的错误消息或不安全的日志通道中暴露此输出。
     * 仅用于内部调试和开发诊断。</p>
     *
     * @param targetClass the target class | 目标类
     * @param callerClass the caller class | 调用方类
     * @return diagnostic string | 诊断字符串
     * @throws OpenReflectException if targetClass or callerClass is null | 如果参数为 null 则抛出异常
     */
    public static String getAccessDiagnostic(Class<?> targetClass, Class<?> callerClass) {
        validateParams(targetClass, "targetClass");
        validateParams(callerClass, "callerClass");

        Module targetModule = targetClass.getModule();
        Module callerModule = callerClass.getModule();
        String targetPackage = targetClass.getPackageName();
        String targetModuleName = targetModule.isNamed() ? targetModule.getName() : "<unnamed>";
        String callerModuleName = callerModule.isNamed() ? callerModule.getName() : "<unnamed>";

        var sb = new StringBuilder();
        sb.append("Access diagnostic for ").append(targetClass.getName())
                .append(" from ").append(callerClass.getName()).append(":\n");
        sb.append("  Target module: ").append(targetModuleName).append('\n');
        sb.append("  Caller module: ").append(callerModuleName).append('\n');
        sb.append("  Target package: ").append(targetPackage).append('\n');

        if (targetModule == callerModule) {
            sb.append("  Status: SAME MODULE - full access granted.\n");
            return sb.toString();
        }

        if (!targetModule.isNamed()) {
            sb.append("  Status: Target is in an unnamed module - full access granted.\n");
            return sb.toString();
        }

        boolean exported = targetModule.isExported(targetPackage, callerModule);
        boolean open = targetModule.isOpen(targetPackage, callerModule);

        sb.append("  Exported to caller: ").append(exported).append('\n');
        sb.append("  Open to caller: ").append(open).append('\n');

        if (open) {
            sb.append("  Status: Full reflective access (deep reflection) is available.\n");
        } else if (exported) {
            sb.append("  Status: Public API access only. Deep reflection (setAccessible) will fail.\n");
            sb.append("  Fix: Add 'opens ").append(targetPackage)
                    .append(" to ").append(callerModuleName)
                    .append(";' to module ").append(targetModuleName).append(".\n");
        } else {
            sb.append("  Status: No access. The package is neither exported nor open to the caller.\n");
            sb.append("  Fix: Add 'exports ").append(targetPackage)
                    .append(" to ").append(callerModuleName)
                    .append(";' for public API access, or\n");
            sb.append("        'opens ").append(targetPackage)
                    .append(" to ").append(callerModuleName)
                    .append(";' for deep reflection access in module ")
                    .append(targetModuleName).append(".\n");
        }

        return sb.toString();
    }

    // ==================== Module Info | 模块信息 ====================

    /**
     * Gets module information for a class.
     * 获取类的模块信息。
     *
     * @param clazz the class to inspect | 要检查的类
     * @return module information record | 模块信息记录
     * @throws OpenReflectException if clazz is null | 如果 clazz 为 null 则抛出异常
     */
    public static ModuleInfo getModuleInfo(Class<?> clazz) {
        validateParams(clazz, "clazz");

        Module module = clazz.getModule();
        String packageName = clazz.getPackageName();
        boolean isNamed = module.isNamed();
        String moduleName = isNamed ? module.getName() : "unnamed";

        if (!isNamed) {
            // Unnamed modules: all packages are open and exported
            Set<String> pkgSet = packageName.isEmpty() ? Set.of() : Set.of(packageName);
            return new ModuleInfo(
                    moduleName,
                    packageName,
                    false,
                    true,
                    true,
                    pkgSet,
                    pkgSet
            );
        }

        ModuleDescriptor descriptor = module.getDescriptor();
        boolean isOpen = module.isOpen(packageName);
        boolean isExported = module.isExported(packageName);

        // Guard against null descriptor (synthetic/dynamic modules)
        if (descriptor == null) {
            return new ModuleInfo(moduleName, packageName, true, isOpen, isExported, Set.of(), Set.of());
        }

        Set<String> openPackages;
        if (descriptor.isOpen()) {
            // Open module: all packages are implicitly open
            openPackages = Set.copyOf(descriptor.packages());
        } else {
            openPackages = descriptor.opens().stream()
                    .filter(o -> o.targets().isEmpty())
                    .map(ModuleDescriptor.Opens::source)
                    .collect(Collectors.toUnmodifiableSet());
        }

        Set<String> exportedPackages = descriptor.exports().stream()
                .filter(e -> e.targets().isEmpty())
                .map(ModuleDescriptor.Exports::source)
                .collect(Collectors.toUnmodifiableSet());

        return new ModuleInfo(
                moduleName,
                packageName,
                true,
                isOpen,
                isExported,
                openPackages,
                exportedPackages
        );
    }

    /**
     * Checks if a class is in a named module.
     * 检查类是否在命名模块中。
     *
     * <p>Classes loaded from the classpath are in unnamed modules.
     * Classes from the JDK or explicitly declared modules are in named modules.</p>
     * <p>从类路径加载的类在未命名模块中。来自 JDK 或显式声明的模块中的类在命名模块中。</p>
     *
     * @param clazz the class to check | 要检查的类
     * @return true if the class is in a named module | 如果类在命名模块中则返回 true
     * @throws OpenReflectException if clazz is null | 如果 clazz 为 null 则抛出异常
     */
    public static boolean isInNamedModule(Class<?> clazz) {
        validateParams(clazz, "clazz");
        return clazz.getModule().isNamed();
    }

    // ==================== Lookup | 查找 ====================

    /**
     * Tries to get a {@link MethodHandles.Lookup} that can access the target class with private access.
     * 尝试获取可以以私有访问权限访问目标类的 {@link MethodHandles.Lookup}。
     *
     * <p>This uses {@link MethodHandles#privateLookupIn(Class, MethodHandles.Lookup)} which requires
     * the target class's package to be open to the caller's module.</p>
     * <p>使用 {@link MethodHandles#privateLookupIn(Class, MethodHandles.Lookup)}，
     * 要求目标类的包对调用方模块开放。</p>
     *
     * @param targetClass the target class | 目标类
     * @return an Optional containing the Lookup if successful, or empty if access is denied |
     *         成功时包含 Lookup 的 Optional，访问被拒绝时返回空
     * @throws OpenReflectException if targetClass is null | 如果 targetClass 为 null 则抛出异常
     */
    public static Optional<MethodHandles.Lookup> tryPrivateLookup(Class<?> targetClass) {
        validateParams(targetClass, "targetClass");

        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                    targetClass, MethodHandles.lookup());
            return Optional.of(lookup);
        } catch (IllegalAccessException | SecurityException e) {
            return Optional.empty();
        }
    }

    // ==================== Internal | 内部方法 ====================

    /**
     * Validates that a parameter is not null
     * 验证参数不为 null
     *
     * @param param     the parameter to validate | 要验证的参数
     * @param paramName the parameter name for error messages | 用于错误消息的参数名称
     * @throws OpenReflectException if param is null | 如果参数为 null 则抛出异常
     */
    private static void validateParams(Object param, String paramName) {
        if (param == null) {
            throw new OpenReflectException(
                    "Parameter '" + paramName + "' must not be null");
        }
    }
}
