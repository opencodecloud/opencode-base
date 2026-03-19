package cloud.opencode.base.classloader.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Class Metadata - Immutable class information
 * 类元数据 - 不可变的类信息
 *
 * <p>Represents class metadata read from class files without loading the class.</p>
 * <p>表示从类文件读取的类元数据，无需加载类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Class hierarchy information - 类层次信息</li>
 *   <li>Method and field metadata - 方法和字段元数据</li>
 *   <li>Annotation metadata - 注解元数据</li>
 *   <li>Type information (interface, abstract, enum, record, sealed) - 类型信息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ClassMetadata metadata = OpenMetadata.read("com.example.MyClass");
 * String className = metadata.className();
 * boolean hasService = metadata.hasAnnotation("org.springframework.stereotype.Service");
 * List<MethodMetadata> methods = metadata.methods();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public final class ClassMetadata {

    private final String className;
    private final String packageName;
    private final String simpleName;
    private final String superClassName;
    private final List<String> interfaceNames;
    private final int modifiers;
    private final boolean isInterface;
    private final boolean isAnnotation;
    private final boolean isEnum;
    private final boolean isRecord;
    private final boolean isSealed;
    private final List<String> permittedSubclasses;
    private final List<MethodMetadata> methods;
    private final List<FieldMetadata> fields;
    private final List<AnnotationMetadata> annotations;
    private final String sourceFile;

    /**
     * Create class metadata using builder
     * 使用构建器创建类元数据
     *
     * @param builder builder instance | 构建器实例
     */
    private ClassMetadata(Builder builder) {
        this.className = Objects.requireNonNull(builder.className, "Class name must not be null");
        this.packageName = extractPackageName(className);
        this.simpleName = extractSimpleName(className);
        this.superClassName = builder.superClassName;
        this.interfaceNames = builder.interfaceNames != null ? List.copyOf(builder.interfaceNames) : List.of();
        this.modifiers = builder.modifiers;
        this.isInterface = builder.isInterface;
        this.isAnnotation = builder.isAnnotation;
        this.isEnum = builder.isEnum;
        this.isRecord = builder.isRecord;
        this.isSealed = builder.isSealed;
        this.permittedSubclasses = builder.permittedSubclasses != null ?
                List.copyOf(builder.permittedSubclasses) : List.of();
        this.methods = builder.methods != null ? List.copyOf(builder.methods) : List.of();
        this.fields = builder.fields != null ? List.copyOf(builder.fields) : List.of();
        this.annotations = builder.annotations != null ? List.copyOf(builder.annotations) : List.of();
        this.sourceFile = builder.sourceFile;
    }

    // ==================== Getters ====================

    /**
     * Get fully qualified class name
     * 获取完全限定类名
     *
     * @return class name | 类名
     */
    public String className() {
        return className;
    }

    /**
     * Get package name
     * 获取包名
     *
     * @return package name | 包名
     */
    public String packageName() {
        return packageName;
    }

    /**
     * Get simple class name
     * 获取简单类名
     *
     * @return simple name | 简单名称
     */
    public String simpleName() {
        return simpleName;
    }

    /**
     * Get super class name
     * 获取父类名
     *
     * @return super class name or null | 父类名或 null
     */
    public String superClassName() {
        return superClassName;
    }

    /**
     * Get interface names
     * 获取接口名列表
     *
     * @return list of interface names | 接口名列表
     */
    public List<String> interfaceNames() {
        return interfaceNames;
    }

    /**
     * Get modifiers
     * 获取修饰符
     *
     * @return modifier flags | 修饰符标志
     */
    public int modifiers() {
        return modifiers;
    }

    /**
     * Check if interface
     * 检查是否为接口
     *
     * @return true if interface | 是接口返回 true
     */
    public boolean isInterface() {
        return isInterface;
    }

    /**
     * Check if abstract
     * 检查是否为抽象类
     *
     * @return true if abstract | 是抽象类返回 true
     */
    public boolean isAbstract() {
        return Modifier.isAbstract(modifiers);
    }

    /**
     * Check if annotation
     * 检查是否为注解
     *
     * @return true if annotation | 是注解返回 true
     */
    public boolean isAnnotation() {
        return isAnnotation;
    }

    /**
     * Check if enum
     * 检查是否为枚举
     *
     * @return true if enum | 是枚举返回 true
     */
    public boolean isEnum() {
        return isEnum;
    }

    /**
     * Check if record
     * 检查是否为 Record
     *
     * @return true if record | 是 Record 返回 true
     */
    public boolean isRecord() {
        return isRecord;
    }

    /**
     * Check if sealed
     * 检查是否为密封类
     *
     * @return true if sealed | 是密封类返回 true
     */
    public boolean isSealed() {
        return isSealed;
    }

    /**
     * Check if final
     * 检查是否为 final
     *
     * @return true if final | 是 final 返回 true
     */
    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    /**
     * Get permitted subclasses (for sealed types)
     * 获取允许的子类（用于密封类型）
     *
     * @return list of permitted subclass names | 允许的子类名列表
     */
    public List<String> permittedSubclasses() {
        return permittedSubclasses;
    }

    /**
     * Get methods
     * 获取方法列表
     *
     * @return list of method metadata | 方法元数据列表
     */
    public List<MethodMetadata> methods() {
        return methods;
    }

    /**
     * Get fields
     * 获取字段列表
     *
     * @return list of field metadata | 字段元数据列表
     */
    public List<FieldMetadata> fields() {
        return fields;
    }

    /**
     * Get annotations
     * 获取注解列表
     *
     * @return list of annotation metadata | 注解元数据列表
     */
    public List<AnnotationMetadata> annotations() {
        return annotations;
    }

    /**
     * Get source file name
     * 获取源文件名
     *
     * @return source file name or null | 源文件名或 null
     */
    public String sourceFile() {
        return sourceFile;
    }

    // ==================== Convenience Methods | 便捷方法 ====================

    /**
     * Check if has specified annotation by class name
     * 检查是否有指定注解（按类名）
     *
     * @param annotationClassName annotation class name | 注解类名
     * @return true if has annotation | 有注解返回 true
     */
    public boolean hasAnnotation(String annotationClassName) {
        return annotations.stream()
                .anyMatch(a -> a.annotationType().equals(annotationClassName));
    }

    /**
     * Check if has specified annotation
     * 检查是否有指定注解
     *
     * @param annotationClass annotation class | 注解类
     * @return true if has annotation | 有注解返回 true
     */
    public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
        return hasAnnotation(annotationClass.getName());
    }

    /**
     * Get specified annotation
     * 获取指定注解
     *
     * @param annotationClassName annotation class name | 注解类名
     * @return optional annotation | 可选的注解
     */
    public Optional<AnnotationMetadata> getAnnotation(String annotationClassName) {
        return annotations.stream()
                .filter(a -> a.annotationType().equals(annotationClassName))
                .findFirst();
    }

    /**
     * Check if is subtype of specified class
     * 检查是否为指定类的子类型
     *
     * @param className class name | 类名
     * @return true if is subtype | 是子类型返回 true
     */
    public boolean isSubTypeOf(String className) {
        return className.equals(superClassName) || interfaceNames.contains(className);
    }

    /**
     * Get all method names
     * 获取所有方法名
     *
     * @return list of method names | 方法名列表
     */
    public List<String> getMethodNames() {
        return methods.stream().map(MethodMetadata::methodName).toList();
    }

    /**
     * Get all field names
     * 获取所有字段名
     *
     * @return list of field names | 字段名列表
     */
    public List<String> getFieldNames() {
        return fields.stream().map(FieldMetadata::fieldName).toList();
    }

    /**
     * Check if is concrete class (not abstract and not interface)
     * 检查是否为具体类（非抽象非接口）
     *
     * @return true if concrete | 是具体类返回 true
     */
    public boolean isConcrete() {
        return !isInterface && !isAbstract();
    }

    /**
     * Check if is inner class
     * 检查是否为内部类
     *
     * @return true if inner class | 是内部类返回 true
     */
    public boolean isInnerClass() {
        return className.contains("$");
    }

    /**
     * Get outer class name if inner class
     * 获取外部类名（如果是内部类）
     *
     * @return optional outer class name | 可选的外部类名
     */
    public Optional<String> getOuterClassName() {
        int idx = className.lastIndexOf('$');
        return idx > 0 ? Optional.of(className.substring(0, idx)) : Optional.empty();
    }

    /**
     * Get method by name
     * 按名称获取方法
     *
     * @param methodName method name | 方法名
     * @return list of methods with the name | 具有该名称的方法列表
     */
    public List<MethodMetadata> getMethodsByName(String methodName) {
        return methods.stream()
                .filter(m -> m.methodName().equals(methodName))
                .toList();
    }

    /**
     * Get field by name
     * 按名称获取字段
     *
     * @param fieldName field name | 字段名
     * @return optional field | 可选的字段
     */
    public Optional<FieldMetadata> getField(String fieldName) {
        return fields.stream()
                .filter(f -> f.fieldName().equals(fieldName))
                .findFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassMetadata that)) return false;
        return className.equals(that.className);
    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String mods = Modifier.toString(modifiers);
        if (!mods.isEmpty()) sb.append(mods).append(" ");
        if (isInterface) {
            sb.append("interface ");
        } else if (isEnum) {
            sb.append("enum ");
        } else if (isRecord) {
            sb.append("record ");
        } else if (isAnnotation) {
            sb.append("@interface ");
        } else {
            sb.append("class ");
        }
        sb.append(simpleName);
        return sb.toString();
    }

    private static String extractPackageName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot != -1 ? className.substring(0, lastDot) : "";
    }

    private static String extractSimpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        String name = lastDot != -1 ? className.substring(lastDot + 1) : className;
        int lastDollar = name.lastIndexOf('$');
        return lastDollar != -1 ? name.substring(lastDollar + 1) : name;
    }

    /**
     * Create builder
     * 创建构建器
     *
     * @return new builder | 新构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ClassMetadata
     * ClassMetadata 构建器
     */
    public static class Builder {
        private String className;
        private String superClassName;
        private List<String> interfaceNames;
        private int modifiers;
        private boolean isInterface;
        private boolean isAnnotation;
        private boolean isEnum;
        private boolean isRecord;
        private boolean isSealed;
        private List<String> permittedSubclasses;
        private List<MethodMetadata> methods;
        private List<FieldMetadata> fields;
        private List<AnnotationMetadata> annotations;
        private String sourceFile;

        public Builder className(String className) {
            this.className = className;
            return this;
        }

        public Builder superClassName(String superClassName) {
            this.superClassName = superClassName;
            return this;
        }

        public Builder interfaceNames(List<String> interfaceNames) {
            this.interfaceNames = interfaceNames;
            return this;
        }

        public Builder modifiers(int modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        public Builder isInterface(boolean isInterface) {
            this.isInterface = isInterface;
            return this;
        }

        public Builder isAnnotation(boolean isAnnotation) {
            this.isAnnotation = isAnnotation;
            return this;
        }

        public Builder isEnum(boolean isEnum) {
            this.isEnum = isEnum;
            return this;
        }

        public Builder isRecord(boolean isRecord) {
            this.isRecord = isRecord;
            return this;
        }

        public Builder isSealed(boolean isSealed) {
            this.isSealed = isSealed;
            return this;
        }

        public Builder permittedSubclasses(List<String> permittedSubclasses) {
            this.permittedSubclasses = permittedSubclasses;
            return this;
        }

        public Builder methods(List<MethodMetadata> methods) {
            this.methods = methods;
            return this;
        }

        public Builder fields(List<FieldMetadata> fields) {
            this.fields = fields;
            return this;
        }

        public Builder annotations(List<AnnotationMetadata> annotations) {
            this.annotations = annotations;
            return this;
        }

        public Builder sourceFile(String sourceFile) {
            this.sourceFile = sourceFile;
            return this;
        }

        public ClassMetadata build() {
            return new ClassMetadata(this);
        }
    }
}
