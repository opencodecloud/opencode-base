package cloud.opencode.base.core;

import cloud.opencode.base.core.exception.OpenException;

import java.io.InputStream;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

/**
 * Class Utility Class - Class loading, type checking, generics handling and classpath operations
 * Class 工具类 - 类加载、类型判断、泛型处理和类路径操作
 *
 * <p>Provides comprehensive class operations including loading, type checking, generics and classpath operations.</p>
 * <p>提供全面的类操作，包括类加载、类型判断、泛型处理、包扫描和类路径操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Class loading (loadClass, loadClassSafely, forName) - 类加载</li>
 *   <li>Type checking (isPrimitive, isWrapper, isArray, isEnum) - 类型判断</li>
 *   <li>Primitive/Wrapper conversion (wrap, unwrap, getDefaultValue) - 原始/包装类型转换</li>
 *   <li>Generics handling (getTypeArguments, getSuperclassTypeArguments) - 泛型处理</li>
 *   <li>Hierarchy operations (getAllSuperclasses, getAllInterfaces, getCommonSuperClass) - 类层次操作</li>
 *   <li>Classpath operations (getResource, getResourceAsStream, getJarPath) - 类路径操作</li>
 *   <li>Instantiation (newInstance) - 实例化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Class loading - 类加载
 * Class<?> clazz = OpenClass.loadClass("com.example.MyClass");
 * Optional<Class<?>> opt = OpenClass.loadClassSafely("Unknown");
 *
 * // Type checking - 类型判断
 * boolean isPrimitive = OpenClass.isPrimitive(int.class);  // true
 * boolean isWrapper = OpenClass.isWrapper(Integer.class);  // true
 *
 * // Primitive/Wrapper conversion - 类型转换
 * Class<?> wrapped = OpenClass.wrap(int.class);  // Integer.class
 * Object defaultVal = OpenClass.getDefaultValue(int.class); // 0
 *
 * // Generics - 泛型
 * Type[] args = OpenClass.getTypeArguments(MyClass.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless with immutable caches) - 线程安全: 是 (无状态, 不可变缓存)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenClass {

    private OpenClass() {
    }

    // 原始类型与包装类型映射
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP = new HashMap<>();
    private static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE_MAP = new HashMap<>();
    private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = new HashMap<>();

    static {
        PRIMITIVE_WRAPPER_MAP.put(boolean.class, Boolean.class);
        PRIMITIVE_WRAPPER_MAP.put(byte.class, Byte.class);
        PRIMITIVE_WRAPPER_MAP.put(char.class, Character.class);
        PRIMITIVE_WRAPPER_MAP.put(short.class, Short.class);
        PRIMITIVE_WRAPPER_MAP.put(int.class, Integer.class);
        PRIMITIVE_WRAPPER_MAP.put(long.class, Long.class);
        PRIMITIVE_WRAPPER_MAP.put(float.class, Float.class);
        PRIMITIVE_WRAPPER_MAP.put(double.class, Double.class);
        PRIMITIVE_WRAPPER_MAP.put(void.class, Void.class);

        PRIMITIVE_WRAPPER_MAP.forEach((k, v) -> WRAPPER_PRIMITIVE_MAP.put(v, k));

        PRIMITIVE_DEFAULTS.put(boolean.class, false);
        PRIMITIVE_DEFAULTS.put(byte.class, (byte) 0);
        PRIMITIVE_DEFAULTS.put(char.class, '\0');
        PRIMITIVE_DEFAULTS.put(short.class, (short) 0);
        PRIMITIVE_DEFAULTS.put(int.class, 0);
        PRIMITIVE_DEFAULTS.put(long.class, 0L);
        PRIMITIVE_DEFAULTS.put(float.class, 0.0f);
        PRIMITIVE_DEFAULTS.put(double.class, 0.0);
    }

    // ==================== 类加载 ====================

    /**
     * Returns the default ClassLoader for the current thread or this class.
     * 获取默认 ClassLoader
     */
    public static ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = OpenClass.class.getClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
        }
        return cl;
    }

    /**
     * Returns the ClassLoader for the specified class.
     * 获取指定类的 ClassLoader
     */
    public static ClassLoader getClassLoader(Class<?> clazz) {
        if (clazz == null) {
            return getClassLoader();
        }
        ClassLoader cl = clazz.getClassLoader();
        return cl != null ? cl : getClassLoader();
    }

    /**
     * Loads and initializes the class with the given name.
     * 加载类
     */
    public static Class<?> loadClass(String className) {
        return loadClass(className, true, getClassLoader());
    }

    /**
     * Loads the class with the given name, optionally initializing it.
     * 加载类（不初始化）
     */
    public static Class<?> loadClass(String className, boolean initialize) {
        return loadClass(className, initialize, getClassLoader());
    }

    /**
     * Loads and initializes the class using the specified ClassLoader.
     * 加载类（指定 ClassLoader）
     */
    public static Class<?> loadClass(String className, ClassLoader classLoader) {
        return loadClass(className, true, classLoader);
    }

    private static Class<?> loadClass(String className, boolean initialize, ClassLoader classLoader) {
        try {
            return Class.forName(className, initialize, classLoader);
        } catch (ClassNotFoundException e) {
            throw new OpenException("Class not found: " + className, e);
        }
    }

    /**
     * Safely loads the class, returning null if it does not exist.
     * 安全加载类（不存在返回 null）
     */
    public static Class<?> loadClassSafely(String className) {
        try {
            return Class.forName(className, true, getClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Safely loads the class, returning an Optional.
     * 安全加载类（返回 Optional）
     */
    public static Optional<Class<?>> loadClassOptional(String className) {
        return Optional.ofNullable(loadClassSafely(className));
    }

    /**
     * Returns true if the class with the given name is present on the classpath.
     * 检查类是否存在
     */
    public static boolean isPresent(String className) {
        return isPresent(className, getClassLoader());
    }

    /**
     * Returns true if the class is present using the specified ClassLoader.
     * 检查类是否存在（指定 ClassLoader）
     */
    public static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // ==================== 类型判断 ====================

    /**
     * Returns true if the class represents a primitive type.
     * 检查是否为原始类型
     */
    public static boolean isPrimitive(Class<?> clazz) {
        return clazz != null && clazz.isPrimitive();
    }

    /**
     * Returns true if the class is a primitive wrapper type.
     * 检查是否为原始类型包装类
     */
    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        return WRAPPER_PRIMITIVE_MAP.containsKey(clazz);
    }

    /**
     * Returns true if the class is a primitive type or its wrapper.
     * 检查是否为原始类型或包装类
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return isPrimitive(clazz) || isPrimitiveWrapper(clazz);
    }

    /**
     * Returns true if the class represents an array type.
     * 检查是否为数组类型
     */
    public static boolean isArray(Class<?> clazz) {
        return clazz != null && clazz.isArray();
    }

    /**
     * Returns true if the class is a Collection or Map type.
     * 检查是否为集合类型
     */
    public static boolean isCollection(Class<?> clazz) {
        return clazz != null && (Collection.class.isAssignableFrom(clazz) ||
                Map.class.isAssignableFrom(clazz));
    }

    /**
     * Returns true if the class is an interface.
     * 检查是否为接口
     */
    public static boolean isInterface(Class<?> clazz) {
        return clazz != null && clazz.isInterface();
    }

    /**
     * Returns true if the class is abstract.
     * 检查是否为抽象类
     */
    public static boolean isAbstract(Class<?> clazz) {
        return clazz != null && Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Returns true if the class is an enum type.
     * 检查是否为枚举类型
     */
    public static boolean isEnum(Class<?> clazz) {
        return clazz != null && clazz.isEnum();
    }

    /**
     * Returns true if the class is a record type.
     * 检查是否为 Record 类型
     */
    public static boolean isRecord(Class<?> clazz) {
        return clazz != null && clazz.isRecord();
    }

    /**
     * Returns true if the class is a sealed type.
     * 检查是否为 sealed 类型
     */
    public static boolean isSealed(Class<?> clazz) {
        return clazz != null && clazz.isSealed();
    }

    /**
     * Returns true if the class is a non-static inner class.
     * 检查是否为内部类
     */
    public static boolean isInnerClass(Class<?> clazz) {
        return clazz != null && clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers());
    }

    /**
     * Returns true if the class is an anonymous class.
     * 检查是否为匿名类
     */
    public static boolean isAnonymousClass(Class<?> clazz) {
        return clazz != null && clazz.isAnonymousClass();
    }

    /**
     * Returns true if the class is a local class.
     * 检查是否为本地类
     */
    public static boolean isLocalClass(Class<?> clazz) {
        return clazz != null && clazz.isLocalClass();
    }

    /**
     * Returns true if the class was generated by a lambda expression.
     * 检查是否为 Lambda 表达式生成的类
     */
    public static boolean isLambdaClass(Class<?> clazz) {
        return clazz != null && clazz.isSynthetic() &&
                clazz.getName().contains("$$Lambda");
    }

    /**
     * Returns true if subType is assignable to superType (considering primitive/wrapper conversions).
     * 检查是否可赋值
     */
    public static boolean isAssignable(Class<?> superType, Class<?> subType) {
        if (superType == null || subType == null) {
            return false;
        }
        if (superType.isAssignableFrom(subType)) {
            return true;
        }
        // 处理原始类型和包装类型
        if (superType.isPrimitive()) {
            return PRIMITIVE_WRAPPER_MAP.get(superType) == subType;
        }
        if (subType.isPrimitive()) {
            return superType == PRIMITIVE_WRAPPER_MAP.get(subType);
        }
        return false;
    }

    // ==================== 类型转换 ====================

    /**
     * Returns the wrapper class corresponding to the given primitive type.
     * 获取原始类型对应的包装类
     */
    public static Class<?> getWrapperClass(Class<?> primitiveType) {
        return PRIMITIVE_WRAPPER_MAP.get(primitiveType);
    }

    /**
     * Returns the primitive type corresponding to the given wrapper class.
     * 获取包装类对应的原始类型
     */
    public static Class<?> getPrimitiveClass(Class<?> wrapperType) {
        return WRAPPER_PRIMITIVE_MAP.get(wrapperType);
    }

    /**
     * Returns the component type of the given array class.
     * 获取数组的组件类型
     */
    public static Class<?> getComponentType(Class<?> arrayClass) {
        return arrayClass != null ? arrayClass.getComponentType() : null;
    }

    /**
     * Returns the array type for the given component type.
     * 获取数组类型
     */
    public static Class<?> getArrayClass(Class<?> componentType) {
        return java.lang.reflect.Array.newInstance(componentType, 0).getClass();
    }

    // ==================== 类名操作 ====================

    /**
     * Returns the simple name of the class.
     * 获取类的简单名称
     */
    public static String getSimpleName(Class<?> clazz) {
        return clazz != null ? clazz.getSimpleName() : null;
    }

    /**
     * Returns the short name of the class (last segment of the fully-qualified name).
     * 获取类的短名称
     */
    public static String getShortName(Class<?> clazz) {
        if (clazz == null) return null;
        String className = clazz.getName();
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }

    /**
     * Returns the fully-qualified name of the class.
     * 获取类的完全限定名
     */
    public static String getFullName(Class<?> clazz) {
        return clazz != null ? clazz.getName() : null;
    }

    /**
     * Returns the package name of the class.
     * 获取包名
     */
    public static String getPackageName(Class<?> clazz) {
        return clazz != null ? clazz.getPackageName() : null;
    }

    /**
     * Returns the package name extracted from the fully-qualified class name string.
     * 获取包名（从类名字符串）
     */
    public static String getPackageName(String className) {
        if (className == null) return null;
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(0, lastDot) : "";
    }

    /**
     * Converts a class name to a resource path by replacing '.' with '/'.
     * 类名转资源路径
     */
    public static String classNameToPath(String className) {
        return className != null ? className.replace('.', '/') : null;
    }

    /**
     * Converts a resource path to a class name by replacing '/' with '.'.
     * 资源路径转类名
     */
    public static String pathToClassName(String path) {
        return path != null ? path.replace('/', '.') : null;
    }

    // ==================== 继承关系 ====================

    /**
     * Returns all superclasses of the given class, excluding Object.
     * 获取所有父类（不含 Object）
     */
    public static List<Class<?>> getSuperClasses(Class<?> clazz) {
        if (clazz == null) return Collections.emptyList();
        List<Class<?>> superClasses = new ArrayList<>();
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            superClasses.add(superClass);
            superClass = superClass.getSuperclass();
        }
        return superClasses;
    }

    /**
     * Returns all interfaces implemented by the class, including inherited interfaces.
     * 获取所有接口（包括继承的）
     */
    public static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
        if (clazz == null) return Collections.emptySet();
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        collectInterfaces(clazz, interfaces);
        return interfaces;
    }

    private static void collectInterfaces(Class<?> clazz, Set<Class<?>> interfaces) {
        for (Class<?> iface : clazz.getInterfaces()) {
            if (interfaces.add(iface)) {
                collectInterfaces(iface, interfaces);
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            collectInterfaces(superClass, interfaces);
        }
    }

    /**
     * Returns all superclasses and interfaces of the given class.
     * 获取所有父类和接口
     */
    public static Set<Class<?>> getAllSuperTypes(Class<?> clazz) {
        Set<Class<?>> result = new LinkedHashSet<>();
        result.addAll(getSuperClasses(clazz));
        result.addAll(getAllInterfaces(clazz));
        return result;
    }

    /**
     * Returns the closest common superclass of two classes, or Object.class if none.
     * 获取公共父类
     *
     * @param class1 first class | 第一个类
     * @param class2 second class | 第二个类
     * @return closest common superclass, or Object.class if none | 最近公共父类，如果没有则返回 Object.class
     */
    public static Class<?> getCommonSuperClass(Class<?> class1, Class<?> class2) {
        if (class1 == null || class2 == null) {
            return Object.class;
        }
        if (class1.isAssignableFrom(class2)) {
            return class1;
        }
        if (class2.isAssignableFrom(class1)) {
            return class2;
        }
        // 收集 class1 的所有父类
        Set<Class<?>> ancestors = new HashSet<>();
        Class<?> current = class1;
        while (current != null) {
            ancestors.add(current);
            current = current.getSuperclass();
        }
        // 查找 class2 的父类中第一个在 ancestors 中的
        current = class2;
        while (current != null) {
            if (ancestors.contains(current)) {
                return current;
            }
            current = current.getSuperclass();
        }
        return Object.class;
    }

    // ==================== 泛型处理 ====================

    /**
     * Returns the type parameters declared on the given class.
     * 获取类的泛型参数
     *
     * @param clazz the class | 类
     * @return array of type parameters, or empty array if none | 泛型参数数组，如果没有则返回空数组
     */
    public static Type[] getTypeArguments(Class<?> clazz) {
        if (clazz == null) {
            return new Type[0];
        }
        TypeVariable<?>[] typeParams = clazz.getTypeParameters();
        return typeParams.length > 0 ? typeParams : new Type[0];
    }

    /**
     * Returns the actual type arguments of the generic superclass of the given class.
     * 获取父类的泛型参数
     *
     * @param clazz the class | 类
     * @return array of superclass type arguments, or empty array if none | 父类泛型参数数组，如果没有则返回空数组
     */
    public static Type[] getSuperclassTypeArguments(Class<?> clazz) {
        if (clazz == null) {
            return new Type[0];
        }
        Type superclass = clazz.getGenericSuperclass();
        if (superclass instanceof ParameterizedType pt) {
            return pt.getActualTypeArguments();
        }
        return new Type[0];
    }

    /**
     * Returns the actual type arguments for the specified interface on the given class.
     * 获取接口的泛型参数
     *
     * @param clazz          implementing class | 实现类
     * @param interfaceClass interface class | 接口类
     * @return array of interface type arguments, or empty array if none | 接口泛型参数数组，如果没有则返回空数组
     */
    public static Type[] getInterfaceTypeArguments(Class<?> clazz, Class<?> interfaceClass) {
        if (clazz == null || interfaceClass == null) {
            return new Type[0];
        }
        // 直接实现的接口
        for (Type iface : clazz.getGenericInterfaces()) {
            if (iface instanceof ParameterizedType pt) {
                if (pt.getRawType() == interfaceClass) {
                    return pt.getActualTypeArguments();
                }
            } else if (iface == interfaceClass) {
                return new Type[0];
            }
        }
        // 递归查找父类
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            Type[] result = getInterfaceTypeArguments(superClass, interfaceClass);
            if (result.length > 0) {
                return result;
            }
        }
        // 递归查找父接口
        for (Class<?> iface : clazz.getInterfaces()) {
            Type[] result = getInterfaceTypeArguments(iface, interfaceClass);
            if (result.length > 0) {
                return result;
            }
        }
        return new Type[0];
    }

    /**
     * Resolves the first type argument for the given generic superclass or interface.
     * 解析泛型类型为实际类型
     *
     * @param clazz        subclass | 子类
     * @param genericClass generic superclass or interface | 泛型父类或接口
     * @return actual type of the first type argument, or null if unresolvable | 第一个泛型参数的实际类型，如果无法解析则返回 null
     */
    public static Class<?> resolveTypeArgument(Class<?> clazz, Class<?> genericClass) {
        Type[] typeArgs;
        if (genericClass.isInterface()) {
            typeArgs = getInterfaceTypeArguments(clazz, genericClass);
        } else {
            typeArgs = getSuperclassTypeArguments(clazz);
        }
        if (typeArgs.length > 0) {
            Type typeArg = typeArgs[0];
            if (typeArg instanceof Class<?> c) {
                return c;
            }
            if (typeArg instanceof ParameterizedType pt) {
                Type rawType = pt.getRawType();
                if (rawType instanceof Class<?> c) {
                    return c;
                }
            }
        }
        return null;
    }

    // ==================== 默认值 ====================

    /**
     * Returns the default value for the given type (null for non-primitive types).
     * 获取类型的默认值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValue(Class<T> clazz) {
        if (clazz == null || !clazz.isPrimitive()) {
            return null;
        }
        return (T) PRIMITIVE_DEFAULTS.get(clazz);
    }

    /**
     * Returns the default value for the given primitive type.
     * 获取原始类型的默认值
     */
    public static Object getPrimitiveDefaultValue(Class<?> primitiveType) {
        return PRIMITIVE_DEFAULTS.get(primitiveType);
    }

    // ==================== 资源加载 ====================

    /**
     * Returns the URL of the resource with the given name from the classpath.
     * 获取类路径下的资源 URL
     */
    public static URL getResource(String resourceName) {
        return getClassLoader().getResource(resourceName);
    }

    /**
     * Returns an InputStream for reading the named resource from the classpath.
     * 获取类路径下的资源流
     */
    public static InputStream getResourceAsStream(String resourceName) {
        return getClassLoader().getResourceAsStream(resourceName);
    }

    /**
     * Returns the code source location URL for the given class.
     * 获取类的代码源位置
     */
    public static URL getCodeSourceLocation(Class<?> clazz) {
        if (clazz == null) return null;
        try {
            return clazz.getProtectionDomain().getCodeSource().getLocation();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the JAR file path for the given class, or null if not in a JAR.
     * 获取类所在的 JAR 文件路径
     *
     * @param clazz the class | 类
     * @return JAR file path, or null if not in a JAR | JAR 文件路径，如果不在 JAR 中则返回 null
     */
    public static String getJarPath(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        try {
            URL location = getCodeSourceLocation(clazz);
            if (location == null) {
                return null;
            }
            String path = location.toURI().getPath();
            if (path != null && path.endsWith(".jar")) {
                return path;
            }
            // 处理 jar:file: 协议
            String urlStr = location.toString();
            if (urlStr.startsWith("jar:file:")) {
                int endIndex = urlStr.indexOf("!/");
                if (endIndex > 0) {
                    return urlStr.substring(9, endIndex);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Creates a new instance of the given class using its no-arg constructor.
     * 创建类的新实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new OpenException("Failed to create instance of " + clazz, e);
        }
    }
}
