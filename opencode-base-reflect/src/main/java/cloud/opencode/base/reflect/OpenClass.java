package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

/**
 * Class Facade Entry Class
 * 类门面入口类
 *
 * <p>Provides common class operations API.
 * Similar to Commons Lang ClassUtils.</p>
 * <p>提供常用类操作API。
 * 对标Commons Lang ClassUtils。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Class loading - 类加载</li>
 *   <li>Type checking - 类型检查</li>
 *   <li>Inheritance analysis - 继承分析</li>
 *   <li>Primitive/wrapper conversion - 原始/包装类型转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Load class by name
 * Class<?> clazz = OpenClass.forName("com.example.MyClass");
 *
 * // Check type
 * boolean isRecord = OpenClass.isRecord(clazz);
 *
 * // Get all interfaces
 * List<Class<?>> interfaces = OpenClass.getAllInterfaces(clazz);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class, immutable maps) - 线程安全: 是（无状态工具类，不可变映射）</li>
 *   <li>Null-safe: No (caller must ensure non-null arguments) - 空值安全: 否（调用方须确保非空参数）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class OpenClass {

    private static final Map<String, Class<?>> PRIMITIVE_NAME_TO_TYPE;
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER;
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE;

    static {
        Map<String, Class<?>> primitives = new HashMap<>();
        primitives.put("boolean", boolean.class);
        primitives.put("byte", byte.class);
        primitives.put("char", char.class);
        primitives.put("short", short.class);
        primitives.put("int", int.class);
        primitives.put("long", long.class);
        primitives.put("float", float.class);
        primitives.put("double", double.class);
        primitives.put("void", void.class);
        PRIMITIVE_NAME_TO_TYPE = Collections.unmodifiableMap(primitives);

        Map<Class<?>, Class<?>> p2w = new HashMap<>();
        p2w.put(boolean.class, Boolean.class);
        p2w.put(byte.class, Byte.class);
        p2w.put(char.class, Character.class);
        p2w.put(short.class, Short.class);
        p2w.put(int.class, Integer.class);
        p2w.put(long.class, Long.class);
        p2w.put(float.class, Float.class);
        p2w.put(double.class, Double.class);
        p2w.put(void.class, Void.class);
        PRIMITIVE_TO_WRAPPER = Collections.unmodifiableMap(p2w);

        Map<Class<?>, Class<?>> w2p = new HashMap<>();
        for (Map.Entry<Class<?>, Class<?>> entry : p2w.entrySet()) {
            w2p.put(entry.getValue(), entry.getKey());
        }
        WRAPPER_TO_PRIMITIVE = Collections.unmodifiableMap(w2p);
    }

    private OpenClass() {
    }

    // ==================== Class Loading | 类加载 ====================

    /**
     * Loads a class using context class loader
     * 加载类（使用当前线程类加载器）
     *
     * @param className the class name | 类名
     * @return the class | 类
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static Class<?> forName(String className) {
        return forName(className, getDefaultClassLoader());
    }

    /**
     * Loads a class with specified class loader
     * 加载类（指定类加载器）
     *
     * @param className   the class name | 类名
     * @param classLoader the class loader | 类加载器
     * @return the class | 类
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static Class<?> forName(String className, ClassLoader classLoader) {
        Objects.requireNonNull(className, "className must not be null");

        // Check for primitive types
        Class<?> primitive = PRIMITIVE_NAME_TO_TYPE.get(className);
        if (primitive != null) {
            return primitive;
        }

        try {
            return Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw OpenReflectException.classNotFound(className);
        }
    }

    /**
     * Loads a class without initialization
     * 加载类（不初始化）
     *
     * @param className the class name | 类名
     * @return the class | 类
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static Class<?> forNameWithoutInit(String className) {
        try {
            return Class.forName(className, false, getDefaultClassLoader());
        } catch (ClassNotFoundException e) {
            throw OpenReflectException.classNotFound(className);
        }
    }

    /**
     * Safely loads a class returning Optional
     * 安全加载类（返回Optional）
     *
     * @param className the class name | 类名
     * @return Optional of class | 类的Optional
     */
    public static Optional<Class<?>> forNameSafe(String className) {
        try {
            return Optional.of(forName(className));
        } catch (OpenReflectException e) {
            return Optional.empty();
        }
    }

    /**
     * Checks if a class exists
     * 检查类是否存在
     *
     * @param className the class name | 类名
     * @return true if exists | 如果存在返回true
     */
    public static boolean exists(String className) {
        return forNameSafe(className).isPresent();
    }

    /**
     * Gets a primitive class by name
     * 加载原始类型
     *
     * @param name the primitive name | 原始类型名
     * @return the primitive class or null | 原始类型或null
     */
    public static Class<?> getPrimitiveClass(String name) {
        return PRIMITIVE_NAME_TO_TYPE.get(name);
    }

    // ==================== Class Information | 类信息 ====================

    /**
     * Gets short class name
     * 获取类名（简短）
     *
     * @param clazz the class | 类
     * @return the short class name | 简短类名
     */
    public static String getShortClassName(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }
        String name = clazz.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot < 0 ? name : name.substring(lastDot + 1);
    }

    /**
     * Gets simple class name
     * 获取类名（不含包名）
     *
     * @param clazz the class | 类
     * @return the simple name | 简单名
     */
    public static String getSimpleName(Class<?> clazz) {
        return clazz.getSimpleName();
    }

    /**
     * Gets package name
     * 获取包名
     *
     * @param clazz the class | 类
     * @return the package name | 包名
     */
    public static String getPackageName(Class<?> clazz) {
        Package pkg = clazz.getPackage();
        return pkg != null ? pkg.getName() : "";
    }

    /**
     * Gets canonical name
     * 获取规范名
     *
     * @param clazz the class | 类
     * @return the canonical name | 规范名
     */
    public static String getCanonicalName(Class<?> clazz) {
        String name = clazz.getCanonicalName();
        return name != null ? name : clazz.getName();
    }

    /**
     * Gets the class location (JAR file)
     * 获取类所在JAR文件
     *
     * @param clazz the class | 类
     * @return Optional of URL | URL的Optional
     */
    public static Optional<URL> getClassLocation(Class<?> clazz) {
        try {
            return Optional.ofNullable(clazz.getProtectionDomain().getCodeSource().getLocation());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // ==================== Type Checking | 类型判断 ====================

    /**
     * Checks if primitive type
     * 检查是否为原始类型
     *
     * @param clazz the class | 类
     * @return true if primitive | 如果是原始类型返回true
     */
    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive();
    }

    /**
     * Checks if wrapper type
     * 检查是否为包装类型
     *
     * @param clazz the class | 类
     * @return true if wrapper | 如果是包装类型返回true
     */
    public static boolean isWrapper(Class<?> clazz) {
        return WRAPPER_TO_PRIMITIVE.containsKey(clazz);
    }

    /**
     * Checks if primitive or wrapper type
     * 检查是否为原始类型或包装类型
     *
     * @param clazz the class | 类
     * @return true if primitive or wrapper | 如果是原始类型或包装类型返回true
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return isPrimitive(clazz) || isWrapper(clazz);
    }

    /**
     * Checks if array
     * 检查是否为数组
     *
     * @param clazz the class | 类
     * @return true if array | 如果是数组返回true
     */
    public static boolean isArray(Class<?> clazz) {
        return clazz.isArray();
    }

    /**
     * Checks if enum
     * 检查是否为枚举
     *
     * @param clazz the class | 类
     * @return true if enum | 如果是枚举返回true
     */
    public static boolean isEnum(Class<?> clazz) {
        return clazz.isEnum();
    }

    /**
     * Checks if annotation
     * 检查是否为注解
     *
     * @param clazz the class | 类
     * @return true if annotation | 如果是注解返回true
     */
    public static boolean isAnnotation(Class<?> clazz) {
        return clazz.isAnnotation();
    }

    /**
     * Checks if interface
     * 检查是否为接口
     *
     * @param clazz the class | 类
     * @return true if interface | 如果是接口返回true
     */
    public static boolean isInterface(Class<?> clazz) {
        return clazz.isInterface();
    }

    /**
     * Checks if abstract
     * 检查是否为抽象类
     *
     * @param clazz the class | 类
     * @return true if abstract | 如果是抽象类返回true
     */
    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Checks if final
     * 检查是否为final类
     *
     * @param clazz the class | 类
     * @return true if final | 如果是final类返回true
     */
    public static boolean isFinal(Class<?> clazz) {
        return Modifier.isFinal(clazz.getModifiers());
    }

    /**
     * Checks if inner class
     * 检查是否为内部类
     *
     * @param clazz the class | 类
     * @return true if inner class | 如果是内部类返回true
     */
    public static boolean isInnerClass(Class<?> clazz) {
        return clazz.getEnclosingClass() != null;
    }

    /**
     * Checks if anonymous class
     * 检查是否为匿名类
     *
     * @param clazz the class | 类
     * @return true if anonymous | 如果是匿名类返回true
     */
    public static boolean isAnonymousClass(Class<?> clazz) {
        return clazz.isAnonymousClass();
    }

    /**
     * Checks if Record (JDK 16+)
     * 检查是否为Record（JDK 16+）
     *
     * @param clazz the class | 类
     * @return true if record | 如果是Record返回true
     */
    public static boolean isRecord(Class<?> clazz) {
        return clazz.isRecord();
    }

    /**
     * Checks if Sealed class (JDK 17+)
     * 检查是否为Sealed类（JDK 17+）
     *
     * @param clazz the class | 类
     * @return true if sealed | 如果是Sealed类返回true
     */
    public static boolean isSealed(Class<?> clazz) {
        return clazz.isSealed();
    }

    /**
     * Checks if functional interface
     * 检查是否为函数式接口
     *
     * @param clazz the class | 类
     * @return true if functional interface | 如果是函数式接口返回true
     */
    public static boolean isFunctionalInterface(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return false;
        }
        return clazz.isAnnotationPresent(FunctionalInterface.class)
                || countAbstractMethods(clazz) == 1;
    }

    // ==================== Inheritance | 继承关系 ====================

    /**
     * Checks if target is assignable from source
     * 检查是否可赋值
     *
     * @param target the target class | 目标类
     * @param source the source class | 源类
     * @return true if assignable | 如果可赋值返回true
     */
    public static boolean isAssignable(Class<?> target, Class<?> source) {
        return isAssignable(target, source, true);
    }

    /**
     * Checks if target is assignable from source with autoboxing option
     * 检查是否可赋值（支持原始类型转换）
     *
     * @param target     the target class | 目标类
     * @param source     the source class | 源类
     * @param autoboxing whether to consider autoboxing | 是否考虑自动装箱
     * @return true if assignable | 如果可赋值返回true
     */
    public static boolean isAssignable(Class<?> target, Class<?> source, boolean autoboxing) {
        if (target.isAssignableFrom(source)) {
            return true;
        }
        if (autoboxing) {
            if (target.isPrimitive()) {
                return primitiveToWrapper(target).isAssignableFrom(source);
            }
            if (source.isPrimitive()) {
                return target.isAssignableFrom(primitiveToWrapper(source));
            }
        }
        return false;
    }

    /**
     * Gets all superclasses (excluding Object)
     * 获取所有父类（不含Object）
     *
     * @param clazz the class | 类
     * @return list of superclasses | 父类列表
     */
    public static List<Class<?>> getAllSuperclasses(Class<?> clazz) {
        List<Class<?>> result = new ArrayList<>();
        Class<?> current = clazz.getSuperclass();
        while (current != null && current != Object.class) {
            result.add(current);
            current = current.getSuperclass();
        }
        return result;
    }

    /**
     * Gets all interfaces
     * 获取所有接口
     *
     * @param clazz the class | 类
     * @return list of interfaces | 接口列表
     */
    public static List<Class<?>> getAllInterfaces(Class<?> clazz) {
        Set<Class<?>> result = new LinkedHashSet<>();
        collectInterfaces(clazz, result);
        return new ArrayList<>(result);
    }

    /**
     * Gets class hierarchy (from current to Object)
     * 获取继承层次（从当前类到Object）
     *
     * @param clazz the class | 类
     * @return list of classes in hierarchy | 继承层次中的类列表
     */
    public static List<Class<?>> getClassHierarchy(Class<?> clazz) {
        List<Class<?>> result = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null) {
            result.add(current);
            current = current.getSuperclass();
        }
        return result;
    }

    // ==================== Type Conversion | 类型转换 ====================

    /**
     * Converts primitive to wrapper
     * 原始类型转包装类型
     *
     * @param clazz the primitive class | 原始类型
     * @return the wrapper class | 包装类型
     */
    public static Class<?> primitiveToWrapper(Class<?> clazz) {
        return PRIMITIVE_TO_WRAPPER.getOrDefault(clazz, clazz);
    }

    /**
     * Converts wrapper to primitive
     * 包装类型转原始类型
     *
     * @param clazz the wrapper class | 包装类型
     * @return the primitive class or original if not wrapper | 原始类型，如果不是包装类型则返回原类型
     */
    public static Class<?> wrapperToPrimitive(Class<?> clazz) {
        return WRAPPER_TO_PRIMITIVE.getOrDefault(clazz, clazz);
    }

    /**
     * Gets array component type
     * 获取数组组件类型
     *
     * @param arrayClass the array class | 数组类
     * @return the component type | 组件类型
     */
    public static Class<?> getComponentType(Class<?> arrayClass) {
        return arrayClass.getComponentType();
    }

    /**
     * Gets array class for component type
     * 获取数组类型
     *
     * @param componentType the component type | 组件类型
     * @return the array class | 数组类
     */
    public static Class<?> getArrayClass(Class<?> componentType) {
        return java.lang.reflect.Array.newInstance(componentType, 0).getClass();
    }

    // ==================== Instantiation | 实例化 ====================

    /**
     * Checks if class can be instantiated
     * 检查是否可实例化
     *
     * @param clazz the class | 类
     * @return true if can be instantiated | 如果可实例化返回true
     */
    public static boolean isInstantiable(Class<?> clazz) {
        return !clazz.isInterface()
                && !isAbstract(clazz)
                && !clazz.isArray()
                && !clazz.isPrimitive()
                && hasDefaultConstructor(clazz);
    }

    /**
     * Checks if class has default constructor
     * 检查是否有默认构造器
     *
     * @param clazz the class | 类
     * @return true if has default constructor | 如果有默认构造器返回true
     */
    public static boolean hasDefaultConstructor(Class<?> clazz) {
        try {
            clazz.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = OpenClass.class.getClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
        }
        return cl;
    }

    private static void collectInterfaces(Class<?> clazz, Set<Class<?>> result) {
        if (clazz == null) {
            return;
        }
        for (Class<?> iface : clazz.getInterfaces()) {
            result.add(iface);
            collectInterfaces(iface, result);
        }
        collectInterfaces(clazz.getSuperclass(), result);
    }

    private static int countAbstractMethods(Class<?> clazz) {
        int count = 0;
        for (java.lang.reflect.Method method : clazz.getMethods()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                count++;
            }
        }
        return count;
    }
}
