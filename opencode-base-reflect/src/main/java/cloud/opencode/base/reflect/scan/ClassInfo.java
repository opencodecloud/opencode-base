package cloud.opencode.base.reflect.scan;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Class Information Holder
 * 类信息持有者
 *
 * <p>Holds information about a class discovered during scanning.
 * Allows lazy loading of the actual Class object.</p>
 * <p>持有扫描期间发现的类的信息。
 * 允许延迟加载实际的Class对象。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Class name and package extraction - 类名和包提取</li>
 *   <li>Lazy class loading - 延迟类加载</li>
 *   <li>Class metadata filtering - 类元数据过滤</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ClassInfo info = ClassInfo.of("com.example.User", classLoader);
 * String className = info.getClassName();
 * Class<?> clazz = info.load();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (volatile lazy loading) - 线程安全: 是（volatile延迟加载）</li>
 *   <li>Null-safe: No (resource name and class loader must be non-null) - 空值安全: 否（资源名和类加载器须非空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public class ClassInfo extends ResourceInfo {

    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("\\d+");

    private final String className;
    private volatile Class<?> loadedClass;

    /**
     * Creates a ClassInfo from resource name
     * 从资源名创建ClassInfo
     *
     * @param resourceName the resource name (e.g., com/example/Foo.class) | 资源名
     * @param classLoader  the class loader | 类加载器
     */
    public ClassInfo(String resourceName, ClassLoader classLoader) {
        super(resourceName, classLoader);
        this.className = resourceNameToClassName(resourceName);
    }

    /**
     * Creates a ClassInfo from class name
     * 从类名创建ClassInfo
     *
     * @param className   the class name (e.g., com.example.Foo) | 类名
     * @param classLoader the class loader | 类加载器
     * @return the ClassInfo | ClassInfo
     */
    public static ClassInfo fromClassName(String className, ClassLoader classLoader) {
        String resourceName = className.replace('.', '/') + ".class";
        ClassInfo info = new ClassInfo(resourceName, classLoader);
        return info;
    }

    /**
     * Creates a ClassInfo from Class object
     * 从Class对象创建ClassInfo
     *
     * @param clazz the class | 类
     * @return the ClassInfo | ClassInfo
     */
    public static ClassInfo fromClass(Class<?> clazz) {
        String resourceName = clazz.getName().replace('.', '/') + ".class";
        ClassInfo info = new ClassInfo(resourceName, clazz.getClassLoader());
        info.loadedClass = clazz;
        return info;
    }

    /**
     * Gets the fully qualified class name
     * 获取完全限定类名
     *
     * @return the class name | 类名
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets the simple class name (without package)
     * 获取简单类名（不含包）
     *
     * @return the simple name | 简单名
     */
    @Override
    public String getSimpleName() {
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }

    /**
     * Gets the package name
     * 获取包名
     *
     * @return the package name | 包名
     */
    @Override
    public String getPackageName() {
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(0, lastDot) : "";
    }

    /**
     * Loads and returns the Class object
     * 加载并返回Class对象
     *
     * @return the Class | 类
     * @throws OpenReflectException if class cannot be loaded | 如果类无法加载
     */
    public Class<?> load() {
        Class<?> clazz = loadedClass;
        if (clazz == null) {
            synchronized (this) {
                clazz = loadedClass;
                if (clazz == null) {
                    try {
                        clazz = getClassLoader().loadClass(className);
                        loadedClass = clazz;
                    } catch (ClassNotFoundException e) {
                        throw OpenReflectException.classLoadFailed(className, e);
                    }
                }
            }
        }
        return clazz;
    }

    /**
     * Checks if the class is already loaded
     * 检查类是否已加载
     *
     * @return true if loaded | 如果已加载返回true
     */
    public boolean isLoaded() {
        return loadedClass != null;
    }

    /**
     * Checks if this is an inner class
     * 检查是否为内部类
     *
     * @return true if inner class | 如果是内部类返回true
     */
    public boolean isInnerClass() {
        return className.contains("$");
    }

    /**
     * Checks if this is an anonymous class
     * 检查是否为匿名类
     *
     * @return true if anonymous | 如果是匿名类返回true
     */
    public boolean isAnonymousClass() {
        String simple = getSimpleName();
        if (simple.contains("$")) {
            String afterDollar = simple.substring(simple.lastIndexOf('$') + 1);
            return DIGITS_ONLY_PATTERN.matcher(afterDollar).matches();
        }
        return false;
    }

    /**
     * Gets the outer class name for inner classes
     * 获取内部类的外部类名
     *
     * @return the outer class name or null | 外部类名或null
     */
    public String getOuterClassName() {
        int dollar = className.lastIndexOf('$');
        return dollar >= 0 ? className.substring(0, dollar) : null;
    }

    /**
     * Checks if this class is in the given package
     * 检查此类是否在给定包中
     *
     * @param packageName the package name | 包名
     * @return true if in package | 如果在包中返回true
     */
    public boolean isInPackage(String packageName) {
        return getPackageName().equals(packageName);
    }

    /**
     * Checks if this class is in the given package or subpackage
     * 检查此类是否在给定包或子包中
     *
     * @param packageName the package name | 包名
     * @return true if in package or subpackage | 如果在包或子包中返回true
     */
    public boolean isInPackageOrSubpackage(String packageName) {
        String pkg = getPackageName();
        return pkg.equals(packageName) || pkg.startsWith(packageName + ".");
    }

    private static String resourceNameToClassName(String resourceName) {
        if (resourceName.endsWith(".class")) {
            resourceName = resourceName.substring(0, resourceName.length() - 6);
        }
        return resourceName.replace('/', '.');
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassInfo that)) return false;
        return className.equals(that.className);
    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }

    @Override
    public String toString() {
        return "ClassInfo[" + className + "]";
    }
}
