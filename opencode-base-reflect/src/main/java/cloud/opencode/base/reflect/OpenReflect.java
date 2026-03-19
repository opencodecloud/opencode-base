package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import cloud.opencode.base.reflect.invokable.Invokable;
import cloud.opencode.base.reflect.type.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Reflection Main Facade Entry Class
 * 反射主门面入口类
 *
 * <p>Unified entry point for all reflection operations.
 * Delegates to specialized facade classes.</p>
 * <p>所有反射操作的统一入口点。
 * 委托给专门的门面类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Class operations - 类操作</li>
 *   <li>Field operations - 字段操作</li>
 *   <li>Method operations - 方法操作</li>
 *   <li>Constructor operations - 构造器操作</li>
 *   <li>Annotation operations - 注解操作</li>
 *   <li>Type operations - 类型操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Load class and create instance
 * Class<?> clazz = OpenReflect.forName("com.example.User");
 * User user = OpenReflect.newInstance(User.class);
 *
 * // Read/write fields
 * Object name = OpenReflect.readField(user, "name");
 * OpenReflect.writeField(user, "name", "Alice");
 *
 * // Invoke method
 * Object result = OpenReflect.invokeMethod(user, "getName");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless facade, delegates to thread-safe classes) - 线程安全: 是（无状态门面，委托给线程安全类）</li>
 *   <li>Null-safe: No (caller must ensure non-null arguments) - 空值安全: 否（调用方须确保非空参数）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class OpenReflect {

    private OpenReflect() {
    }

    // ==================== Class Operations | 类操作 ====================

    /**
     * Loads a class by name
     * 按名称加载类
     *
     * @param className the class name | 类名
     * @return the class | 类
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static Class<?> forName(String className) {
        return OpenClass.forName(className);
    }

    /**
     * Loads a class by name (Optional)
     * 按名称加载类（Optional）
     *
     * @param className the class name | 类名
     * @return Optional of class | 类的Optional
     */
    public static Optional<Class<?>> forNameSafe(String className) {
        return OpenClass.forNameSafe(className);
    }

    /**
     * Checks if class exists
     * 检查类是否存在
     *
     * @param className the class name | 类名
     * @return true if exists | 如果存在返回true
     */
    public static boolean classExists(String className) {
        return OpenClass.exists(className);
    }

    /**
     * Creates a TypeToken for a class
     * 为类创建TypeToken
     *
     * @param clazz the class | 类
     * @param <T>   the type | 类型
     * @return the TypeToken | TypeToken
     */
    public static <T> TypeToken<T> typeOf(Class<T> clazz) {
        return TypeToken.of(clazz);
    }

    // ==================== Field Operations | 字段操作 ====================

    /**
     * Gets a field
     * 获取字段
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @return the field | 字段
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        return OpenField.getField(clazz, fieldName);
    }

    /**
     * Gets all fields (including inherited)
     * 获取所有字段（包含继承）
     *
     * @param clazz the class | 类
     * @return list of fields | 字段列表
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        return OpenField.getAllFields(clazz);
    }

    /**
     * Reads a field value
     * 读取字段值
     *
     * @param target    the target object | 目标对象
     * @param fieldName the field name | 字段名
     * @return the value | 值
     */
    public static Object readField(Object target, String fieldName) {
        return OpenField.readField(target, fieldName);
    }

    /**
     * Writes a field value
     * 写入字段值
     *
     * @param target    the target object | 目标对象
     * @param fieldName the field name | 字段名
     * @param value     the value | 值
     */
    public static void writeField(Object target, String fieldName, Object value) {
        OpenField.writeField(target, fieldName, value);
    }

    // ==================== Method Operations | 方法操作 ====================

    /**
     * Gets a method
     * 获取方法
     *
     * @param clazz          the class | 类
     * @param methodName     the method name | 方法名
     * @param parameterTypes the parameter types | 参数类型
     * @return the method | 方法
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return OpenMethod.getMethod(clazz, methodName, parameterTypes);
    }

    /**
     * Gets all methods (including inherited)
     * 获取所有方法（包含继承）
     *
     * @param clazz the class | 类
     * @return list of methods | 方法列表
     */
    public static List<Method> getAllMethods(Class<?> clazz) {
        return OpenMethod.getAllMethods(clazz);
    }

    /**
     * Invokes a method
     * 调用方法
     *
     * @param target     the target object | 目标对象
     * @param methodName the method name | 方法名
     * @param args       the arguments | 参数
     * @return the result | 结果
     */
    public static Object invokeMethod(Object target, String methodName, Object... args) {
        return OpenMethod.invokeMethod(target, methodName, args);
    }

    /**
     * Invokes a static method
     * 调用静态方法
     *
     * @param clazz      the class | 类
     * @param methodName the method name | 方法名
     * @param args       the arguments | 参数
     * @return the result | 结果
     */
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Object... args) {
        return OpenMethod.invokeStaticMethod(clazz, methodName, args);
    }

    // ==================== Constructor Operations | 构造器操作 ====================

    /**
     * Gets a constructor
     * 获取构造器
     *
     * @param clazz          the class | 类
     * @param parameterTypes the parameter types | 参数类型
     * @param <T>            the type | 类型
     * @return the constructor | 构造器
     */
    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        return OpenConstructor.getConstructor(clazz, parameterTypes);
    }

    /**
     * Creates a new instance
     * 创建新实例
     *
     * @param clazz the class | 类
     * @param <T>   the type | 类型
     * @return the instance | 实例
     */
    public static <T> T newInstance(Class<T> clazz) {
        return OpenConstructor.newInstance(clazz);
    }

    /**
     * Creates a new instance with arguments
     * 创建新实例（带参数）
     *
     * @param clazz the class | 类
     * @param args  the arguments | 参数
     * @param <T>   the type | 类型
     * @return the instance | 实例
     */
    public static <T> T newInstance(Class<T> clazz, Object... args) {
        return OpenConstructor.newInstance(clazz, args);
    }

    // ==================== Annotation Operations | 注解操作 ====================

    /**
     * Gets an annotation
     * 获取注解
     *
     * @param element         the annotated element | 被注解元素
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return the annotation or null | 注解或null
     */
    public static <A extends Annotation> A getAnnotation(AnnotatedElement element, Class<A> annotationClass) {
        return OpenAnnotation.getAnnotation(element, annotationClass);
    }

    /**
     * Finds an annotation (Optional)
     * 查找注解（Optional）
     *
     * @param element         the annotated element | 被注解元素
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return Optional of annotation | 注解的Optional
     */
    public static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationClass) {
        return OpenAnnotation.findAnnotation(element, annotationClass);
    }

    /**
     * Checks if annotation is present
     * 检查注解是否存在
     *
     * @param element         the annotated element | 被注解元素
     * @param annotationClass the annotation class | 注解类
     * @return true if present | 如果存在返回true
     */
    public static boolean hasAnnotation(AnnotatedElement element, Class<? extends Annotation> annotationClass) {
        return OpenAnnotation.isAnnotationPresent(element, annotationClass);
    }

    // ==================== Modifier Operations | 修饰符操作 ====================

    /**
     * Checks if public
     * 检查是否public
     *
     * @param member the member | 成员
     * @return true if public | 如果是public返回true
     */
    public static boolean isPublic(Member member) {
        return OpenModifier.isPublic(member);
    }

    /**
     * Checks if private
     * 检查是否private
     *
     * @param member the member | 成员
     * @return true if private | 如果是private返回true
     */
    public static boolean isPrivate(Member member) {
        return OpenModifier.isPrivate(member);
    }

    /**
     * Checks if static
     * 检查是否static
     *
     * @param member the member | 成员
     * @return true if static | 如果是static返回true
     */
    public static boolean isStatic(Member member) {
        return OpenModifier.isStatic(member);
    }

    /**
     * Checks if final
     * 检查是否final
     *
     * @param member the member | 成员
     * @return true if final | 如果是final返回true
     */
    public static boolean isFinal(Member member) {
        return OpenModifier.isFinal(member);
    }

    // ==================== Invokable Operations | Invokable操作 ====================

    /**
     * Creates an Invokable from a method
     * 从方法创建Invokable
     *
     * @param method the method | 方法
     * @param <T>    the declaring class type | 声明类类型
     * @return the Invokable | Invokable
     */
    public static <T> Invokable<T, Object> toInvokable(Method method) {
        return Invokable.from(method);
    }

    /**
     * Creates an Invokable from a constructor
     * 从构造器创建Invokable
     *
     * @param constructor the constructor | 构造器
     * @param <T>         the type | 类型
     * @return the Invokable | Invokable
     */
    public static <T> Invokable<T, T> toInvokable(Constructor<T> constructor) {
        return Invokable.from(constructor);
    }

    // ==================== Type Information | 类型信息 ====================

    /**
     * Checks if class is primitive
     * 检查是否为原始类型
     *
     * @param clazz the class | 类
     * @return true if primitive | 如果是原始类型返回true
     */
    public static boolean isPrimitive(Class<?> clazz) {
        return OpenClass.isPrimitive(clazz);
    }

    /**
     * Checks if class is wrapper
     * 检查是否为包装类型
     *
     * @param clazz the class | 类
     * @return true if wrapper | 如果是包装类型返回true
     */
    public static boolean isWrapper(Class<?> clazz) {
        return OpenClass.isWrapper(clazz);
    }

    /**
     * Checks if class is a record
     * 检查是否为Record类
     *
     * @param clazz the class | 类
     * @return true if record | 如果是Record返回true
     */
    public static boolean isRecord(Class<?> clazz) {
        return OpenClass.isRecord(clazz);
    }

    /**
     * Checks if class is sealed
     * 检查是否为密封类
     *
     * @param clazz the class | 类
     * @return true if sealed | 如果是密封类返回true
     */
    public static boolean isSealed(Class<?> clazz) {
        return OpenClass.isSealed(clazz);
    }

    /**
     * Converts primitive to wrapper
     * 原始类型转包装类型
     *
     * @param primitiveType the primitive type | 原始类型
     * @return the wrapper type | 包装类型
     */
    public static Class<?> primitiveToWrapper(Class<?> primitiveType) {
        return OpenClass.primitiveToWrapper(primitiveType);
    }

    /**
     * Converts wrapper to primitive
     * 包装类型转原始类型
     *
     * @param wrapperType the wrapper type | 包装类型
     * @return the primitive type | 原始类型
     */
    public static Class<?> wrapperToPrimitive(Class<?> wrapperType) {
        return OpenClass.wrapperToPrimitive(wrapperType);
    }

    // ==================== Class Hierarchy | 类层次 ====================

    /**
     * Gets all superclasses
     * 获取所有父类
     *
     * @param clazz the class | 类
     * @return list of superclasses | 父类列表
     */
    public static List<Class<?>> getAllSuperclasses(Class<?> clazz) {
        return OpenClass.getAllSuperclasses(clazz);
    }

    /**
     * Gets all interfaces (including inherited)
     * 获取所有接口（包含继承）
     *
     * @param clazz the class | 类
     * @return list of interfaces | 接口列表
     */
    public static List<Class<?>> getAllInterfaces(Class<?> clazz) {
        return OpenClass.getAllInterfaces(clazz);
    }

    /**
     * Gets the full class hierarchy
     * 获取完整类层次结构
     *
     * @param clazz the class | 类
     * @return list of all classes in hierarchy | 层次结构中的所有类
     */
    public static List<Class<?>> getClassHierarchy(Class<?> clazz) {
        return OpenClass.getClassHierarchy(clazz);
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Makes an accessible object accessible
     * 设置可访问对象为可访问
     *
     * @param accessible the accessible object | 可访问对象
     * @param <T>        the accessible type | 可访问类型
     * @return the accessible object | 可访问对象
     */
    public static <T extends AccessibleObject> T makeAccessible(T accessible) {
        if (!accessible.canAccess(null)) {
            accessible.setAccessible(true);
        }
        return accessible;
    }

    /**
     * Checks if two classes are in the same package
     * 检查两个类是否在同一包中
     *
     * @param class1 the first class | 第一个类
     * @param class2 the second class | 第二个类
     * @return true if same package | 如果在同一包返回true
     */
    public static boolean isSamePackage(Class<?> class1, Class<?> class2) {
        return class1.getPackageName().equals(class2.getPackageName());
    }

    /**
     * Gets the simple name of a class (handles arrays and inner classes)
     * 获取类的简单名称（处理数组和内部类）
     *
     * @param clazz the class | 类
     * @return the simple name | 简单名称
     */
    public static String getSimpleName(Class<?> clazz) {
        if (clazz.isArray()) {
            return getSimpleName(clazz.getComponentType()) + "[]";
        }
        String simpleName = clazz.getSimpleName();
        if (simpleName.isEmpty()) {
            // Anonymous class
            String name = clazz.getName();
            int lastDot = name.lastIndexOf('.');
            return lastDot >= 0 ? name.substring(lastDot + 1) : name;
        }
        return simpleName;
    }

    /**
     * Gets the canonical name or falls back to name
     * 获取规范名称或回退到名称
     *
     * @param clazz the class | 类
     * @return the canonical name or name | 规范名称或名称
     */
    public static String getCanonicalNameOrName(Class<?> clazz) {
        String canonical = clazz.getCanonicalName();
        return canonical != null ? canonical : clazz.getName();
    }
}
