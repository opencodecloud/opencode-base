# Reflect 组件方案

## 1. 组件概述

`opencode-base-reflect` 提供全面的 Java 反射工具能力，核心特性包括：泛型类型捕获（TypeToken）、流畅 API（Invokable）、字段/方法/构造器/类的全方位反射工具、Bean 复制与属性访问、Record/Sealed Class/Lambda 等 JDK 25 新特性支持、类路径扫描与注解扫描、MethodHandle/VarHandle 高性能访问。零外部依赖，纯 JDK 实现。

## 2. 包结构

```
cloud.opencode.base.reflect
├── OpenReflect                     # 反射主门面
├── OpenField                       # 字段门面
├── OpenMethod                      # 方法门面
├── OpenConstructor                 # 构造器门面
├── OpenClass                       # 类门面
├── OpenAnnotation                  # 注解门面
├── OpenModifier                    # 修饰符门面
├── FieldUtil                       # 字段工具类
├── MethodUtil                      # 方法工具类
├── ConstructorUtil                 # 构造器工具类
├── ClassUtil                       # 类工具类
├── AnnotationUtil                  # 注解工具类
├── ModifierUtil                    # 修饰符工具类
├── ReflectUtil                     # 反射通用工具类
├── ReflectCache                    # 反射结果缓存
├── accessor/                       # 属性访问器
│   ├── PropertyAccessor            # 属性访问器接口
│   ├── PropertyAccessors           # 访问器工厂
│   ├── BeanAccessor                # 基于 getter/setter 的访问器
│   ├── FieldAccessor               # 基于 Field 的访问器
│   ├── MethodHandleAccessor        # 基于 MethodHandle 的访问器
│   └── VarHandleAccessor           # 基于 VarHandle 的访问器
├── bean/                           # Bean 操作
│   ├── OpenBean                    # Bean 门面
│   ├── BeanCopier                  # Bean 复制器
│   ├── BeanMap                     # Bean 映射
│   ├── BeanPath                    # Bean 路径导航
│   ├── BeanUtil                    # Bean 工具类
│   ├── PropertyConverter           # 属性类型转换器
│   └── PropertyDescriptor          # 属性描述符
├── invokable/                      # 流畅调用封装
│   ├── Invokable                   # Invokable 抽象基类
│   ├── MethodInvokable             # 方法 Invokable
│   ├── ConstructorInvokable        # 构造器 Invokable
│   └── Parameter                   # 参数包装器
├── lambda/                         # Lambda 解析
│   ├── OpenLambda                  # Lambda 门面
│   ├── LambdaUtil                  # Lambda 工具类
│   ├── LambdaInfo                  # Lambda 信息持有者
│   ├── FunctionalInterfaceUtil     # 函数式接口工具
│   ├── SerializedLambdaWrapper     # SerializedLambda 包装器
│   ├── SerializableFunction        # 可序列化 Function
│   ├── SerializableConsumer        # 可序列化 Consumer
│   ├── SerializablePredicate       # 可序列化 Predicate
│   ├── SerializableSupplier        # 可序列化 Supplier
│   └── SerializableBiFunction      # 可序列化 BiFunction
├── proxy/                          # 动态代理
│   ├── OpenProxy                   # 代理门面
│   ├── ProxyFactory                # 代理工厂
│   ├── ProxyUtil                   # 代理工具类
│   ├── MethodInterceptor           # 方法拦截器接口
│   ├── MethodInvoker               # 方法调用者接口
│   └── AbstractInvocationHandler   # 抽象调用处理器
├── record/                         # Record 操作
│   ├── OpenRecord                  # Record 门面
│   ├── RecordBuilder               # Record 构建器
│   ├── RecordComponent             # Record 组件包装器
│   └── RecordUtil                  # Record 工具类
├── sealed/                         # Sealed Class 操作
│   ├── OpenSealed                  # Sealed 门面
│   ├── PermittedSubclasses         # 许可子类集合
│   └── SealedUtil                  # Sealed 工具类
├── scan/                           # 类路径扫描
│   ├── ClassPath                   # 类路径扫描器
│   ├── ClassScanner                # 类扫描器
│   ├── AnnotationScanner           # 注解扫描器
│   ├── ResourceScanner             # 资源扫描器
│   ├── ClassInfo                   # 类信息
│   └── ResourceInfo                # 资源信息
├── type/                           # 类型系统
│   ├── TypeToken                   # 泛型类型令牌
│   ├── TypeLiteral                 # 类型字面量
│   ├── TypeResolver                # 类型解析器
│   ├── TypeUtil                    # 类型工具类
│   ├── TypeVariableUtil            # 类型变量工具
│   ├── ParameterizedTypeImpl       # ParameterizedType 实现
│   ├── GenericArrayTypeImpl        # GenericArrayType 实现
│   └── WildcardTypeImpl            # WildcardType 实现
└── exception/
    └── OpenReflectException        # 反射异常
```

## 3. 核心 API

### 3.1 OpenReflect

> 反射主门面入口类，委托到各专门门面类，提供统一的反射操作入口。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `forName(String className)` | 按类名加载类 |
| `forNameSafe(String className)` | 安全加载类（返回 Optional） |
| `classExists(String className)` | 类是否存在 |
| `typeOf(Class<T> clazz)` | 创建 TypeToken |
| `getField(Class<?> clazz, String fieldName)` | 获取字段 |
| `getAllFields(Class<?> clazz)` | 获取所有字段 |
| `readField(Object target, String fieldName)` | 读取字段值 |
| `writeField(Object target, String fieldName, Object value)` | 写入字段值 |
| `getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes)` | 获取方法 |
| `getAllMethods(Class<?> clazz)` | 获取所有方法 |
| `invokeMethod(Object target, String methodName, Object... args)` | 调用方法 |
| `invokeStaticMethod(Class<?> clazz, String methodName, Object... args)` | 调用静态方法 |
| `getConstructor(Class<T> clazz, Class<?>... parameterTypes)` | 获取构造器 |
| `newInstance(Class<T> clazz)` | 创建实例 |
| `newInstance(Class<T> clazz, Object... args)` | 创建实例（带参数） |
| `getAnnotation(AnnotatedElement element, Class<A> annotationClass)` | 获取注解 |
| `findAnnotation(AnnotatedElement element, Class<A> annotationClass)` | 查找注解（返回 Optional） |
| `hasAnnotation(AnnotatedElement element, Class<? extends Annotation> annotationClass)` | 是否存在注解 |
| `isPublic(Member member)` | 是否为 public |
| `isPrivate(Member member)` | 是否为 private |
| `isStatic(Member member)` | 是否为 static |
| `isFinal(Member member)` | 是否为 final |
| `toInvokable(Method method)` | 方法转 Invokable |
| `toInvokable(Constructor<T> constructor)` | 构造器转 Invokable |
| `isPrimitive(Class<?> clazz)` | 是否为基本类型 |
| `isWrapper(Class<?> clazz)` | 是否为包装类型 |
| `isRecord(Class<?> clazz)` | 是否为 Record |
| `isSealed(Class<?> clazz)` | 是否为密封类 |
| `primitiveToWrapper(Class<?> primitiveType)` | 基本类型转包装类型 |
| `wrapperToPrimitive(Class<?> wrapperType)` | 包装类型转基本类型 |
| `getAllSuperclasses(Class<?> clazz)` | 获取所有父类 |
| `getAllInterfaces(Class<?> clazz)` | 获取所有接口 |
| `getClassHierarchy(Class<?> clazz)` | 获取完整类层次 |
| `makeAccessible(T accessible)` | 设置可访问 |
| `isSamePackage(Class<?> class1, Class<?> class2)` | 是否在同一包 |
| `getSimpleName(Class<?> clazz)` | 获取简单类名 |
| `getCanonicalNameOrName(Class<?> clazz)` | 获取规范名或名称 |

**示例:**

```java
// 类操作
Class<?> clazz = OpenReflect.forName("com.example.User");
boolean exists = OpenReflect.classExists("com.example.User");

// 字段操作
Object value = OpenReflect.readField(user, "name");
OpenReflect.writeField(user, "name", "newValue");

// 方法调用
Object result = OpenReflect.invokeMethod(service, "process", arg1, arg2);

// 实例创建
User user = OpenReflect.newInstance(User.class, "John", 25);
```

### 3.2 OpenField

> 字段门面入口类，提供字段查找、读写、批量操作等。类似 Commons Lang FieldUtils。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `getField(Class<?> clazz, String fieldName)` | 获取字段（含继承） |
| `getField(Class<?> clazz, String fieldName, boolean forceAccess)` | 获取字段（可强制访问） |
| `getDeclaredField(Class<?> clazz, String fieldName)` | 获取声明的字段 |
| `getAllFields(Class<?> clazz)` | 获取所有字段（含继承） |
| `getDeclaredFields(Class<?> clazz)` | 获取声明的所有字段 |
| `getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass)` | 按注解获取字段 |
| `getFieldsOfType(Class<?> clazz, Class<?> fieldType)` | 按类型获取字段 |
| `getFieldsWithModifiers(Class<?> clazz, int... modifiers)` | 按修饰符获取字段 |
| `readField(Object target, String fieldName)` | 读取字段值 |
| `readField(Object target, String fieldName, boolean forceAccess)` | 读取字段值（可强制访问） |
| `readField(Field field, Object target)` | 通过 Field 对象读取 |
| `readField(Object target, String fieldName, Class<T> valueType)` | 读取并转换类型 |
| `readStaticField(Class<?> clazz, String fieldName)` | 读取静态字段值 |
| `writeField(Object target, String fieldName, Object value)` | 写入字段值 |
| `writeField(Object target, String fieldName, Object value, boolean forceAccess)` | 写入（可强制访问） |
| `writeField(Field field, Object target, Object value)` | 通过 Field 对象写入 |
| `writeStaticField(Class<?> clazz, String fieldName, Object value)` | 写入静态字段值 |
| `removeFinalAndWrite(Object target, String fieldName, Object value)` | 移除 final 后写入 |
| `getFieldType(Class<?> clazz, String fieldName)` | 获取字段类型 |
| `getFieldGenericType(Class<?> clazz, String fieldName)` | 获取字段泛型类型 |
| `getFieldTypeToken(Class<?> clazz, String fieldName)` | 获取字段 TypeToken |
| `hasField(Class<?> clazz, String fieldName)` | 字段是否存在 |
| `forEach(Class<?> clazz, Consumer<Field> action)` | 遍历所有字段 |
| `findFirst(Class<?> clazz, Predicate<Field> predicate)` | 查找第一个匹配字段 |
| `stream(Class<?> clazz)` | 获取字段流 |
| `readFieldsToMap(Object target)` | 读取所有字段为 Map |
| `readFields(Object target, String... fieldNames)` | 读取指定字段为 Map |
| `writeFieldsFromMap(Object target, Map<String, Object> values)` | 从 Map 写入字段 |

**示例:**

```java
Object value = OpenField.readField(obj, "name");
OpenField.writeField(obj, "name", "newValue");
List<Field> fields = OpenField.getFieldsWithAnnotation(User.class, Column.class);
Map<String, Object> map = OpenField.readFieldsToMap(user);
```

### 3.3 OpenMethod

> 方法门面入口类，提供方法查找、调用等。类似 Commons Lang MethodUtils。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes)` | 获取方法 |
| `getMethod(Class<?> clazz, String methodName, boolean forceAccess, Class<?>... parameterTypes)` | 获取方法（可强制访问） |
| `getMatchingMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes)` | 获取匹配方法（支持参数自动转换） |
| `getAllMethods(Class<?> clazz)` | 获取所有方法（含继承） |
| `getDeclaredMethods(Class<?> clazz)` | 获取声明的方法 |
| `getMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass)` | 按注解获取方法 |
| `getOverloadMethods(Class<?> clazz, String methodName)` | 获取同名重载方法 |
| `getGetters(Class<?> clazz)` | 获取所有 getter |
| `getSetters(Class<?> clazz)` | 获取所有 setter |
| `invokeMethod(Object target, String methodName, Object... args)` | 调用方法 |
| `invokeMethod(Object target, boolean forceAccess, String methodName, Object... args)` | 调用方法（可强制访问） |
| `invokeMethod(Object target, String methodName, Class<T> returnType, Object... args)` | 调用并转换返回类型 |
| `invokeStaticMethod(Class<?> clazz, String methodName, Object... args)` | 调用静态方法 |
| `invokeStaticMethod(Class<?> clazz, String methodName, Class<T> returnType, Object... args)` | 调用静态方法并转换类型 |
| `hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes)` | 方法是否存在 |
| `isGetter(Method method)` | 是否为 getter |
| `isSetter(Method method)` | 是否为 setter |
| `forEach(Class<?> clazz, Consumer<Method> action)` | 遍历所有方法 |
| `findFirst(Class<?> clazz, Predicate<Method> predicate)` | 查找第一个匹配方法 |
| `stream(Class<?> clazz)` | 获取方法流 |
| `toInvokable(Method method)` | 转为 Invokable |

**示例:**

```java
Object result = OpenMethod.invokeMethod(service, "process", arg1, arg2);
List<Method> getters = OpenMethod.getGetters(User.class);
boolean exists = OpenMethod.hasMethod(User.class, "getName");
```

### 3.4 OpenConstructor

> 构造器门面入口类，提供构造器查找、实例创建、工厂方法发现等。类似 Commons Lang ConstructorUtils。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `getConstructor(Class<T> clazz, Class<?>... parameterTypes)` | 获取构造器 |
| `getConstructor(Class<T> clazz, boolean forceAccess, Class<?>... parameterTypes)` | 获取构造器（可强制访问） |
| `getMatchingConstructor(Class<T> clazz, Class<?>... parameterTypes)` | 获取匹配构造器 |
| `getDefaultConstructor(Class<T> clazz)` | 获取无参构造器 |
| `getConstructors(Class<T> clazz)` | 获取所有构造器 |
| `getConstructorsWithAnnotation(Class<T> clazz, Class<? extends Annotation> annotationClass)` | 按注解获取构造器 |
| `newInstance(Class<T> clazz)` | 无参创建实例 |
| `newInstance(Class<T> clazz, Object... args)` | 带参创建实例 |
| `newInstance(Class<T> clazz, Class<?>[] parameterTypes, Object... args)` | 指定参数类型创建实例 |
| `newInstance(Constructor<T> constructor, Object... args)` | 通过构造器创建 |
| `newInstanceForced(Class<T> clazz, Object... args)` | 强制创建实例（忽略访问限制） |
| `hasDefaultConstructor(Class<?> clazz)` | 是否有无参构造器 |
| `hasConstructor(Class<?> clazz, Class<?>... parameterTypes)` | 是否存在指定构造器 |
| `getParameterNames(Constructor<?> constructor)` | 获取参数名称列表 |
| `getParameterTypes(Constructor<?> constructor)` | 获取参数类型数组 |
| `findFactoryMethod(Class<T> clazz, String methodName)` | 查找工厂方法 |
| `findFactoryMethods(Class<T> clazz)` | 查找所有工厂方法 |
| `newInstanceByFactory(Class<T> clazz, String factoryMethod, Object... args)` | 通过工厂方法创建实例 |
| `toInvokable(Constructor<T> constructor)` | 转为 Invokable |

**示例:**

```java
User user = OpenConstructor.newInstance(User.class, "John", 25);
boolean hasDef = OpenConstructor.hasDefaultConstructor(User.class);
```

### 3.5 OpenClass

> 类门面入口类，提供类加载、类型判断、类层次查询等。类似 Commons Lang ClassUtils。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `forName(String className)` | 按名称加载类 |
| `forName(String className, ClassLoader classLoader)` | 指定 ClassLoader 加载 |
| `forNameWithoutInit(String className)` | 加载但不初始化 |
| `forNameSafe(String className)` | 安全加载（返回 Optional） |
| `exists(String className)` | 类是否存在 |
| `getPrimitiveClass(String name)` | 获取基本类型 Class |
| `getShortClassName(Class<?> clazz)` | 获取短类名 |
| `getSimpleName(Class<?> clazz)` | 获取简单类名 |
| `getPackageName(Class<?> clazz)` | 获取包名 |
| `getCanonicalName(Class<?> clazz)` | 获取规范名 |
| `getClassLocation(Class<?> clazz)` | 获取类文件位置 |
| `isPrimitive(Class<?> clazz)` | 是否为基本类型 |
| `isWrapper(Class<?> clazz)` | 是否为包装类型 |
| `isPrimitiveOrWrapper(Class<?> clazz)` | 是否为基本类型或包装类型 |
| `isArray(Class<?> clazz)` | 是否为数组 |
| `isEnum(Class<?> clazz)` | 是否为枚举 |
| `isAnnotation(Class<?> clazz)` | 是否为注解 |
| `isInterface(Class<?> clazz)` | 是否为接口 |
| `isAbstract(Class<?> clazz)` | 是否为抽象类 |
| `isFinal(Class<?> clazz)` | 是否为 final |
| `isInnerClass(Class<?> clazz)` | 是否为内部类 |
| `isAnonymousClass(Class<?> clazz)` | 是否为匿名类 |
| `isRecord(Class<?> clazz)` | 是否为 Record |
| `isSealed(Class<?> clazz)` | 是否为密封类 |
| `isFunctionalInterface(Class<?> clazz)` | 是否为函数式接口 |
| `isAssignable(Class<?> target, Class<?> source)` | 是否可赋值 |
| `isAssignable(Class<?> target, Class<?> source, boolean autoboxing)` | 是否可赋值（含自动装箱） |
| `getAllSuperclasses(Class<?> clazz)` | 获取所有父类 |
| `getAllInterfaces(Class<?> clazz)` | 获取所有接口 |
| `getClassHierarchy(Class<?> clazz)` | 获取完整类层次 |
| `primitiveToWrapper(Class<?> clazz)` | 基本类型转包装类型 |
| `wrapperToPrimitive(Class<?> clazz)` | 包装类型转基本类型 |
| `getComponentType(Class<?> arrayClass)` | 获取数组组件类型 |
| `getArrayClass(Class<?> componentType)` | 获取数组 Class |
| `isInstantiable(Class<?> clazz)` | 是否可实例化 |
| `hasDefaultConstructor(Class<?> clazz)` | 是否有无参构造器 |

**示例:**

```java
Class<?> clazz = OpenClass.forName("com.example.User");
boolean isRecord = OpenClass.isRecord(User.class);
List<Class<?>> interfaces = OpenClass.getAllInterfaces(ArrayList.class);
```

### 3.6 OpenAnnotation

> 注解门面入口类，提供注解查找、属性读取、元注解支持等。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `getAnnotation(AnnotatedElement element, Class<A> annotationClass)` | 获取注解 |
| `findAnnotation(AnnotatedElement element, Class<A> annotationClass)` | 查找注解（Optional） |
| `getAnnotations(AnnotatedElement element)` | 获取所有注解 |
| `getDeclaredAnnotations(AnnotatedElement element)` | 获取声明的注解 |
| `getAnnotationsByType(AnnotatedElement element, Class<A> annotationClass)` | 获取重复注解 |
| `findAnnotationInherited(Class<?> clazz, Class<A> annotationClass)` | 查找继承注解 |
| `findAnnotationOnMethod(Method method, Class<A> annotationClass)` | 在方法上查找注解（含接口） |
| `isAnnotationPresent(AnnotatedElement element, Class<? extends Annotation> annotationClass)` | 注解是否存在 |
| `isAnyAnnotationPresent(AnnotatedElement element, Class<?>... annotationClasses)` | 任一注解是否存在 |
| `isAllAnnotationsPresent(AnnotatedElement element, Class<?>... annotationClasses)` | 所有注解是否存在 |
| `getAttributeValue(Annotation annotation, String attributeName)` | 获取注解属性值 |
| `getAttributeValue(Annotation annotation, String attributeName, Class<T> type)` | 获取并转换属性值 |
| `getAttributeValues(Annotation annotation)` | 获取所有属性值 Map |
| `getDefaultValue(Class<? extends Annotation> annotationClass, String attributeName)` | 获取属性默认值 |
| `isMetaAnnotationPresent(Class<? extends Annotation> annotation, Class<? extends Annotation> metaAnnotation)` | 元注解是否存在 |
| `findMetaAnnotation(Class<? extends Annotation> annotation, Class<A> metaAnnotation)` | 查找元注解 |
| `getMetaAnnotations(Class<? extends Annotation> annotation)` | 获取所有元注解 |
| `isRuntimeRetained(Class<? extends Annotation> annotationClass)` | 是否为运行时保留 |
| `isRepeatable(Class<? extends Annotation> annotationClass)` | 是否可重复 |
| `getRepeatableContainer(Class<? extends Annotation> annotationClass)` | 获取重复注解容器 |
| `getAttributes(Class<? extends Annotation> annotationClass)` | 获取注解属性方法列表 |
| `getAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotationClass)` | 获取带注解的字段 |
| `getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationClass)` | 获取带注解的方法 |
| `getAnnotatedConstructors(Class<?> clazz, Class<? extends Annotation> annotationClass)` | 获取带注解的构造器 |
| `getAnnotatedParameters(Method method, Class<? extends Annotation> annotationClass)` | 获取带注解的参数 |
| `filterAnnotations(AnnotatedElement element, Predicate<Annotation> predicate)` | 过滤注解 |
| `getAnnotationsWithMeta(AnnotatedElement element, Class<? extends Annotation> metaAnnotation)` | 获取带特定元注解的注解 |

**示例:**

```java
Optional<MyAnnotation> ann = OpenAnnotation.findAnnotation(method, MyAnnotation.class);
Map<String, Object> attrs = OpenAnnotation.getAttributeValues(annotation);
List<Field> fields = OpenAnnotation.getAnnotatedFields(User.class, Column.class);
```

### 3.7 OpenModifier

> 修饰符门面入口类，提供修饰符检查和查询。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `isPublic(int modifiers)` / `isPublic(Member)` / `isPublic(Class<?>)` | 是否为 public |
| `isPrivate(int modifiers)` / `isPrivate(Member)` / `isPrivate(Class<?>)` | 是否为 private |
| `isProtected(int modifiers)` / `isProtected(Member)` / `isProtected(Class<?>)` | 是否为 protected |
| `isPackagePrivate(int modifiers)` / `isPackagePrivate(Member)` / `isPackagePrivate(Class<?>)` | 是否为包私有 |
| `isStatic(int modifiers)` / `isStatic(Member)` | 是否为 static |
| `isFinal(int modifiers)` / `isFinal(Member)` / `isFinal(Class<?>)` | 是否为 final |
| `isAbstract(int modifiers)` / `isAbstract(Class<?>)` / `isAbstract(Method)` | 是否为 abstract |
| `isSynchronized(int modifiers)` / `isSynchronized(Method)` | 是否为 synchronized |
| `isVolatile(int modifiers)` / `isVolatile(Field)` | 是否为 volatile |
| `isTransient(int modifiers)` / `isTransient(Field)` | 是否为 transient |
| `isNative(int modifiers)` / `isNative(Method)` | 是否为 native |
| `isInterface(int modifiers)` | 是否为 interface |
| `isStrict(int modifiers)` | 是否为 strictfp |
| `isSynthetic(Method)` / `isSynthetic(Field)` / `isSynthetic(Class<?>)` | 是否为合成 |
| `toString(int modifiers)` | 修饰符转字符串 |
| `toList(int modifiers)` | 修饰符转列表 |
| `getAccessLevel(int modifiers)` / `getAccessLevel(Member)` / `getAccessLevel(Class<?>)` | 获取访问级别 |
| `isAccessAtLeast(int modifiers, AccessLevel level)` | 访问级别是否满足 |
| `isOverridable(Method method)` | 方法是否可重写 |
| `isExtendable(Class<?> clazz)` | 类是否可继承 |

**AccessLevel 枚举:**

| 值 | 描述 |
|------|------|
| `PUBLIC` | public |
| `PROTECTED` | protected |
| `PACKAGE_PRIVATE` | 包私有 |
| `PRIVATE` | private |

### 3.8 TypeToken

> 泛型类型令牌，类似 Guava TypeToken，通过匿名子类捕获泛型类型参数。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `of(Class<T> type)` | 从 Class 创建 |
| `of(Type type)` | 从 Type 创建 |
| `getType()` | 获取 Type |
| `getRawType()` | 获取原始 Class |
| `getTypeParameter(int index)` | 获取第 N 个类型参数 |
| `getTypeParameters()` | 获取所有类型参数 |
| `getComponentType()` | 获取数组组件类型 |
| `isPrimitive()` | 是否为基本类型 |
| `isArray()` | 是否为数组 |
| `isParameterized()` | 是否为参数化类型 |
| `isWildcard()` | 是否为通配符 |
| `isTypeVariable()` | 是否为类型变量 |
| `isSupertypeOf(TypeToken<?> other)` | 是否为超类型 |
| `isSubtypeOf(TypeToken<?> other)` | 是否为子类型 |
| `isAssignableFrom(TypeToken<?> other)` | 是否可赋值 |
| `resolveType(Type toResolve)` | 解析类型 |
| `wrap()` | 基本类型转包装 |
| `unwrap()` | 包装转基本类型 |
| `resolveFieldType(Field field)` | 解析字段类型 |
| `resolveReturnType(Method method)` | 解析方法返回类型 |
| `resolveParameterTypes(Method method)` | 解析方法参数类型 |
| `listOf(Class<E> elementType)` | 创建 List 类型 |
| `setOf(Class<E> elementType)` | 创建 Set 类型 |
| `mapOf(Class<K> keyType, Class<V> valueType)` | 创建 Map 类型 |
| `optionalOf(Class<T> valueType)` | 创建 Optional 类型 |

**示例:**

```java
TypeToken<List<String>> listType = new TypeToken<List<String>>() {};
Class<?> rawType = listType.getRawType(); // List.class
TypeToken<?> elementType = listType.getTypeParameter(0); // String

TypeToken<List<Integer>> intList = TypeToken.listOf(Integer.class);
TypeToken<Map<String, Object>> mapType = TypeToken.mapOf(String.class, Object.class);
```

### 3.9 Invokable

> Method/Constructor 的流畅包装器，类似 Guava Invokable，提供类型安全的调用和元数据查询。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `from(Method method)` | 从 Method 创建 |
| `from(Constructor<T> constructor)` | 从 Constructor 创建 |
| `getDeclaringClass()` | 获取声明类 |
| `getReturnType()` | 获取返回类型 |
| `getParameters()` | 获取参数列表 |
| `getParameterTypes()` | 获取参数类型列表 |
| `getExceptionTypes()` | 获取异常类型列表 |
| `getTypeParameters()` | 获取类型参数 |
| `invoke(T receiver, Object... args)` | 调用 |
| `invokeForced(T receiver, Object... args)` | 强制调用（忽略访问限制） |
| `invokeSafe(T receiver, Object... args)` | 安全调用（返回 Optional） |
| `setAccessible(boolean flag)` | 设置可访问 |
| `isPublic()` | 是否 public |
| `isProtected()` | 是否 protected |
| `isPrivate()` | 是否 private |
| `isPackagePrivate()` | 是否包私有 |
| `isStatic()` | 是否 static |
| `isFinal()` | 是否 final |
| `isAbstract()` | 是否 abstract |
| `isNative()` | 是否 native |
| `isSynchronized()` | 是否 synchronized |
| `isVarArgs()` | 是否变参 |
| `isSynthetic()` | 是否合成 |
| `isOverridable()` | 是否可重写 |
| `getName()` | 获取名称 |
| `getModifiers()` | 获取修饰符 |

**示例:**

```java
Invokable<Service, String> invokable = Invokable.from(method);
String result = invokable.invoke(service, args);

Invokable<User, User> ctor = Invokable.from(constructor);
User user = ctor.invoke(null, "name", 25);
```

### 3.10 OpenBean

> Bean 门面入口类，提供属性读写、Bean 复制、Map 转换等操作。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `getPropertyDescriptors(Class<?> clazz)` | 获取所有属性描述符 |
| `getPropertyDescriptor(Class<?> clazz, String propertyName)` | 获取属性描述符 |
| `getPropertyNames(Class<?> clazz)` | 获取所有属性名 |
| `getReadablePropertyNames(Class<?> clazz)` | 获取可读属性名 |
| `getWritablePropertyNames(Class<?> clazz)` | 获取可写属性名 |
| `getProperty(Object bean, String propertyName)` | 读取属性值 |
| `getProperty(Object bean, String propertyName, Class<T> type)` | 读取并转换属性值 |
| `setProperty(Object bean, String propertyName, Object value)` | 写入属性值 |
| `setPropertyIfWritable(Object bean, String propertyName, Object value)` | 可写时写入 |
| `copyProperties(Object source, Object target)` | 复制属性 |
| `copyProperties(Object source, Object target, String... excludeProperties)` | 复制属性（排除指定） |
| `copyProperties(Object source, Class<T> targetClass)` | 复制到新实例 |
| `toMap(Object bean)` | Bean 转 Map |
| `asBeanMap(T bean)` | Bean 转 BeanMap 视图 |
| `populate(Object bean, Map<String, ?> map)` | 从 Map 填充 Bean |
| `fromMap(Map<String, ?> map, Class<T> clazz)` | 从 Map 创建 Bean |
| `createCopier(Class<S> sourceClass, Class<T> targetClass)` | 创建 BeanCopier |
| `hasProperty(Class<?> clazz, String propertyName)` | 属性是否存在 |
| `clearCache(Class<?> clazz)` | 清除指定类缓存 |
| `clearAllCache()` | 清除所有缓存 |

**示例:**

```java
// 属性操作
Object name = OpenBean.getProperty(user, "name");
OpenBean.setProperty(user, "name", "newName");

// Bean 复制
UserDTO dto = OpenBean.copyProperties(user, UserDTO.class);
OpenBean.copyProperties(source, target, "id", "createTime");

// Map 转换
Map<String, Object> map = OpenBean.toMap(user);
User user = OpenBean.fromMap(map, User.class);
```

### 3.11 BeanCopier

> Bean 复制器，支持属性映射、类型转换、条件复制等高级功能。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `builder(Class<S> sourceClass, Class<T> targetClass)` | 创建构建器 |
| `create(Class<S> sourceClass, Class<T> targetClass)` | 创建默认复制器 |
| `copy(S source, T target)` | 复制属性到已有对象 |
| `copy(S source)` | 复制到新对象 |
| `copyList(List<S> sources)` | 批量复制列表 |
| `getSourceClass()` | 获取源类 |
| `getTargetClass()` | 获取目标类 |

**Builder 方法:**

| 方法 | 描述 |
|------|------|
| `map(String sourceProperty, String targetProperty)` | 属性映射 |
| `convert(String sourceProperty, Function<Object, Object> converter)` | 属性转换 |
| `exclude(String... properties)` | 排除属性 |
| `ignoreNulls(boolean ignoreNulls)` | 忽略 null 值 |
| `when(BiPredicate<String, Object> condition)` | 条件复制 |
| `build()` | 构建 |

**示例:**

```java
BeanCopier<User, UserDTO> copier = BeanCopier.builder(User.class, UserDTO.class)
    .map("fullName", "name")
    .exclude("password")
    .ignoreNulls(true)
    .build();
UserDTO dto = copier.copy(user);
```

### 3.12 BeanMap

> Bean 的 Map 视图，以 Map 接口操作 Bean 属性。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `from(T bean)` | 从 Bean 创建 |
| `getBean()` | 获取底层 Bean |
| `get(Object key)` | 读取属性 |
| `put(String key, Object value)` | 写入属性 |
| `containsKey(Object key)` | 属性是否存在 |
| `keySet()` | 属性名集合 |
| `entrySet()` | 属性条目集合 |
| `size()` | 属性数量 |
| `copyTo(U target)` | 复制到另一个对象 |
| `copyFrom(Map<String, ?> source)` | 从 Map 复制 |
| `toMap()` | 转为普通 Map |
| `getReadableProperties()` | 获取可读属性 Map |
| `getPropertyDescriptor(String name)` | 获取属性描述符 |

**示例:**

```java
BeanMap<User> map = BeanMap.from(user);
Object name = map.get("name");
map.put("name", "newName");
```

### 3.13 BeanPath

> Bean 路径导航器，支持点号分隔的嵌套属性访问（如 `user.address.city`）。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `get(Object bean, String path)` | 获取嵌套属性值 |
| `get(Object bean, String path, Class<T> type)` | 获取并转换类型 |
| `getOrDefault(Object bean, String path, T defaultValue)` | 获取（带默认值） |
| `set(Object bean, String path, Object value)` | 设置嵌套属性值 |
| `hasPath(Object bean, String path)` | 路径是否存在 |
| `getPathValues(Object bean, String path)` | 获取路径上所有值 |
| `copy(Object source, String sourcePath, Object target, String targetPath)` | 路径复制 |

**示例:**

```java
String city = BeanPath.get(user, "address.city", String.class);
BeanPath.set(user, "address.city", "Beijing");
boolean exists = BeanPath.hasPath(user, "address.city");
```

### 3.14 PropertyAccessor / PropertyAccessors

> 属性访问器接口与工厂，支持多种访问策略。

**PropertyAccessor 接口方法:**

| 方法 | 描述 |
|------|------|
| `getName()` | 属性名 |
| `getType()` | 属性类型 |
| `getGenericType()` | 泛型类型 |
| `getDeclaringClass()` | 声明类 |
| `isReadable()` | 是否可读 |
| `isWritable()` | 是否可写 |
| `get(T target)` | 读取值 |
| `set(T target, Object value)` | 写入值 |

**PropertyAccessors 工厂方法:**

| 方法 | 描述 |
|------|------|
| `create(Class<T> clazz, String propertyName)` | 创建默认访问器 |
| `create(Class<T> clazz, String propertyName, Strategy strategy)` | 按策略创建 |
| `createAll(Class<T> clazz)` | 创建所有属性的访问器 |
| `createAll(Class<T> clazz, Strategy strategy)` | 按策略创建所有 |
| `createFieldAccessors(Class<T> clazz)` | 创建字段访问器 |
| `createBeanAccessors(Class<T> clazz)` | 创建 Bean 访问器 |
| `createVarHandleAccessors(Class<T> clazz)` | 创建 VarHandle 访问器 |

**Strategy 枚举:**

| 值 | 描述 |
|------|------|
| `FIELD` | 直接字段访问 |
| `BEAN` | getter/setter 访问 |
| `METHOD_HANDLE` | MethodHandle 访问 |
| `VAR_HANDLE` | VarHandle 访问 |
| `AUTO` | 自动选择最佳策略 |

**VarHandleAccessor 特有方法:**

| 方法 | 描述 |
|------|------|
| `getVolatile(T target)` | volatile 读取 |
| `setVolatile(T target, Object value)` | volatile 写入 |
| `getAndSet(T target, Object newValue)` | 原子获取并设置 |
| `compareAndSet(T target, Object expected, Object newValue)` | CAS 操作 |
| `compareAndExchange(T target, Object expected, Object newValue)` | CAS 交换 |
| `getAndAdd(T target, Object delta)` | 原子获取并加 |
| `getAcquire(T target)` | acquire 语义读取 |
| `setRelease(T target, Object value)` | release 语义写入 |

### 3.15 OpenRecord

> Record 门面入口类，提供 Record 组件查询、值获取、构建器、Map 转换等。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `isRecord(Class<?> clazz)` | 是否为 Record |
| `isRecordInstance(Object obj)` | 实例是否为 Record |
| `requireRecord(Class<T> clazz)` | 要求为 Record（否则抛异常） |
| `getComponents(Class<?> recordClass)` | 获取所有组件 |
| `getComponent(Class<?> recordClass, String componentName)` | 按名获取组件 |
| `getComponent(Class<?> recordClass, int index)` | 按索引获取组件 |
| `getComponentNames(Class<?> recordClass)` | 获取组件名列表 |
| `getComponentTypes(Class<?> recordClass)` | 获取组件类型列表 |
| `getComponentCount(Class<?> recordClass)` | 获取组件数量 |
| `getValue(Record record, String componentName)` | 获取组件值 |
| `getValue(Record record, String componentName, Class<T> type)` | 获取并转换组件值 |
| `getValues(Record record)` | 获取所有组件值 |
| `toMap(Record record)` | Record 转 Map |
| `builder(Class<T> recordClass)` | 创建构建器 |
| `create(Class<T> recordClass, Object... values)` | 创建 Record 实例 |
| `fromMap(Class<T> recordClass, Map<String, ?> values)` | 从 Map 创建 |
| `copy(T record)` | 复制 Record |
| `copyWith(T record, Map<String, ?> modifications)` | 复制并修改 |
| `copyWith(T record, String componentName, Object newValue)` | 复制并修改单个值 |
| `diff(Record record1, Record record2)` | 比较两个 Record |
| `valuesEqual(Record record1, Record record2)` | 值是否相等 |

**示例:**

```java
// Record 查询
List<String> names = OpenRecord.getComponentNames(UserRecord.class);
Map<String, Object> map = OpenRecord.toMap(record);

// Record 构建
UserRecord user = OpenRecord.builder(UserRecord.class)
    .set("name", "John")
    .set("age", 25)
    .build();

// Record 复制
UserRecord modified = OpenRecord.copyWith(user, "name", "Jane");
```

### 3.16 RecordBuilder

> Record 构建器，以流式 API 构建 Record 实例。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `of(Class<T> recordClass)` | 创建空构建器 |
| `from(T record)` | 从已有 Record 创建 |
| `set(String name, Object value)` | 设置组件值 |
| `set(int index, Object value)` | 按索引设置 |
| `setAll(Map<String, ?> map)` | 批量设置 |
| `copyFrom(T record)` | 从 Record 复制值 |
| `setIfAbsent(String name, Object value)` | 若缺则设置 |
| `setIfNotNull(String name, Object value)` | 非 null 时设置 |
| `clear(String name)` | 清除值 |
| `clearAll()` | 清除所有值 |
| `getValue(String name)` | 获取已设值 |
| `hasValue(String name)` | 是否已设值 |
| `getRecordClass()` | 获取 Record 类 |
| `getComponents()` | 获取组件列表 |
| `build()` | 构建 Record |
| `buildValidated()` | 构建并验证 |

### 3.17 OpenSealed

> 密封类门面入口类，提供许可子类查询、层次分析等。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `isSealed(Class<?> clazz)` | 是否为密封类 |
| `isSealedClass(Object obj)` | 实例的类是否为密封类 |
| `requireSealed(Class<T> clazz)` | 要求为密封类 |
| `getPermittedSubclasses(Class<?> sealedClass)` | 获取许可子类集合 |
| `getPermittedSubclassList(Class<?> sealedClass)` | 获取许可子类列表 |
| `getPermittedSubclassCount(Class<?> sealedClass)` | 获取许可子类数量 |
| `isPermittedSubclass(Class<?> sealedClass, Class<?> subclass)` | 是否为许可子类 |
| `getAllSubclassesRecursive(Class<?> sealedClass)` | 递归获取所有子类 |
| `getLeafClasses(Class<?> sealedClass)` | 获取叶子类 |
| `getHierarchy(Class<?> sealedClass)` | 获取层次树 |
| `getSealedParent(Class<?> clazz)` | 获取密封父类 |
| `getSealedInterfaces(Class<?> clazz)` | 获取密封接口 |
| `validateHierarchy(Class<?> sealedClass)` | 验证层次结构 |
| `isExhaustive(Class<?> sealedClass)` | 是否穷举完全 |
| `getConcreteTypes(Class<?> sealedClass)` | 获取具体类型 |
| `generateSwitchTemplate(Class<?> sealedClass, String variableName)` | 生成 switch 模板 |

**示例:**

```java
List<Class<?>> permitted = OpenSealed.getPermittedSubclassList(Shape.class);
boolean exhaustive = OpenSealed.isExhaustive(Shape.class);
String template = OpenSealed.generateSwitchTemplate(Shape.class, "shape");
```

### 3.18 OpenLambda

> Lambda 门面入口类，提供 Lambda 解析、方法引用提取、函数式接口工具等。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `getInfo(Serializable lambda)` | 获取 Lambda 信息 |
| `getImplMethod(Serializable lambda)` | 获取实现方法 |
| `getImplMethodName(Serializable lambda)` | 获取实现方法名 |
| `getImplClass(Serializable lambda)` | 获取实现类 |
| `isMethodReference(Serializable lambda)` | 是否为方法引用 |
| `getPropertyName(SerializableFunction<T, R> getter)` | 从 getter 提取属性名 |
| `getPropertyNameFromSetter(SerializableConsumer<T> setter)` | 从 setter 提取属性名 |
| `getPropertyClass(SerializableFunction<T, R> getter)` | 从 getter 获取属性类型 |
| `isFunctionalInterface(Class<?> clazz)` | 是否为函数式接口 |
| `getSingleAbstractMethod(Class<?> clazz)` | 获取唯一抽象方法 |
| `classify(Class<?> clazz)` | 分类函数式接口 |
| `constant(T value)` | 创建常量 Supplier |
| `alwaysTrue()` | 创建始终为 true 的 Predicate |
| `alwaysFalse()` | 创建始终为 false 的 Predicate |
| `identity()` | 创建恒等函数 |
| `noOp()` | 创建空操作 Consumer |
| `bind(Consumer<T> consumer, T value)` | 绑定参数为 Runnable |
| `bind(Function<T, R> function, T input)` | 绑定参数为 Supplier |
| `safe(ThrowingFunction<T, R> function)` | 包装为安全 Function |
| `safe(ThrowingConsumer<T> consumer)` | 包装为安全 Consumer |
| `safe(ThrowingSupplier<T> supplier)` | 包装为安全 Supplier |

**示例:**

```java
// 从方法引用提取属性名
String name = OpenLambda.getPropertyName(User::getName); // "name"

// 安全包装
Function<String, Integer> parser = OpenLambda.safe(Integer::parseInt);
```

### 3.19 OpenProxy

> 动态代理门面入口类，简化 JDK 动态代理的创建。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `create(Class<T> interfaceClass, MethodInterceptor interceptor)` | 创建拦截代理 |
| `create(Class<T> interfaceClass, InvocationHandler handler)` | 创建代理 |
| `wrap(Class<T> interfaceClass, T target)` | 包装已有对象 |
| `wrap(Class<T> interfaceClass, T target, MethodInterceptor interceptor)` | 包装并拦截 |
| `create(InvocationHandler handler, Class<?>... interfaces)` | 多接口代理 |
| `factory(Class<T> interfaceClass)` | 创建代理工厂 |
| `isProxy(Object object)` | 是否为代理 |
| `getHandler(Object proxy)` | 获取 InvocationHandler |
| `getInterfaces(Object proxy)` | 获取代理接口 |
| `implementsInterface(Object proxy, Class<?> interfaceClass)` | 是否实现接口 |
| `unwrap(Object proxy)` | 解包代理 |
| `createNoOp(Class<T> interfaceClass)` | 创建空操作代理 |
| `createRecording(Class<T> interfaceClass)` | 创建录制代理 |

**RecordingProxy 方法:**

| 方法 | 描述 |
|------|------|
| `proxy()` | 获取代理对象 |
| `calls()` | 获取调用记录 |
| `getCallCount()` | 调用次数 |
| `wasCalled(String methodName)` | 方法是否被调用 |
| `getCallsFor(String methodName)` | 获取指定方法的调用 |
| `clearCalls()` | 清除调用记录 |

**示例:**

```java
// 创建代理
MyService proxy = OpenProxy.create(MyService.class, (target, method, args, invoker) -> {
    System.out.println("Before: " + method.getName());
    Object result = invoker.invoke(args);
    System.out.println("After: " + method.getName());
    return result;
});
```

### 3.20 ProxyFactory

> 代理工厂，提供更精细的代理配置。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `forInterface(Class<T> interfaceClass)` | 为接口创建工厂 |
| `implement(Class<?> interfaceClass)` | 添加额外接口 |
| `classLoader(ClassLoader classLoader)` | 设置 ClassLoader |
| `target(Object target)` | 设置代理目标 |
| `intercept(MethodInterceptor interceptor)` | 全局拦截器 |
| `intercept(String methodName, MethodInterceptor interceptor)` | 按方法名拦截 |
| `intercept(String methodName, Class<?>[] parameterTypes, MethodInterceptor interceptor)` | 按签名拦截 |
| `handler(InvocationHandler handler)` | 设置处理器 |
| `create()` | 创建代理 |

### 3.21 ClassPath / ClassScanner / AnnotationScanner / ResourceScanner

> 类路径扫描工具集，提供类、注解、资源的扫描能力。类似 Guava ClassPath。

**ClassPath 方法:**

| 方法 | 描述 |
|------|------|
| `from(ClassLoader classLoader)` | 创建 ClassPath |
| `fromSystemClassLoader()` | 从系统 ClassLoader 创建 |
| `fromContextClassLoader()` | 从上下文 ClassLoader 创建 |
| `getAllClasses()` | 获取所有类 |
| `getResources()` | 获取所有资源 |
| `getClassesInPackage(String packageName)` | 获取包中的类 |
| `getTopLevelClassesInPackage(String packageName)` | 获取顶级类 |
| `getClassesRecursively(String packageName)` | 递归获取类 |
| `streamClasses()` | 类流 |
| `streamResources()` | 资源流 |

**ClassScanner 方法:**

| 方法 | 描述 |
|------|------|
| `from(ClassPath classPath)` | 从 ClassPath 创建 |
| `from(ClassLoader classLoader)` | 从 ClassLoader 创建 |
| `create()` | 创建默认扫描器 |
| `inPackage(String packageName)` | 限定包 |
| `inPackages(String... packageNames)` | 限定多个包 |
| `recursive(boolean recursive)` | 是否递归 |
| `includeInnerClasses(boolean include)` | 是否包含内部类 |
| `filter(Predicate<Class<?>> predicate)` | 自定义过滤 |
| `withAnnotation(Class<? extends Annotation> annotationClass)` | 按注解过滤 |
| `subtypeOf(Class<?> superClass)` | 按父类过滤 |
| `implementing(Class<?> interfaceClass)` | 按接口过滤 |
| `interfacesOnly()` | 仅接口 |
| `concreteOnly()` | 仅具体类 |
| `recordsOnly()` | 仅 Record |
| `enumsOnly()` | 仅枚举 |
| `scan()` | 执行扫描 |
| `stream()` | 获取类流 |
| `toList()` | 获取类列表 |

**AnnotationScanner 方法:**

| 方法 | 描述 |
|------|------|
| `from(ClassScanner classScanner)` | 从 ClassScanner 创建 |
| `create()` | 创建默认扫描器 |
| `inPackage(String packageName)` | 限定包 |
| `includeMetaAnnotations(boolean include)` | 包含元注解 |
| `findClassesWithAnnotation(Class<? extends Annotation> annotationClass)` | 查找带注解的类 |
| `findMethodsWithAnnotation(Class<? extends Annotation> annotationClass)` | 查找带注解的方法 |
| `findFieldsWithAnnotation(Class<? extends Annotation> annotationClass)` | 查找带注解的字段 |

**ResourceScanner 方法:**

| 方法 | 描述 |
|------|------|
| `create()` | 创建默认扫描器 |
| `withExtension(String extension)` | 按扩展名过滤 |
| `inPackage(String packageName)` | 限定包 |
| `matching(String pattern)` | 模式匹配 |
| `scan()` | 执行扫描 |
| `findProperties()` | 查找 .properties 文件 |
| `findXml()` | 查找 XML 文件 |
| `findJson()` | 查找 JSON 文件 |
| `findYaml()` | 查找 YAML 文件 |

**示例:**

```java
// 类扫描
Set<Class<?>> services = ClassScanner.create()
    .inPackage("com.example")
    .withAnnotation(Service.class)
    .concreteOnly()
    .scan();

// 注解扫描
Set<Class<?>> entities = AnnotationScanner.create()
    .inPackage("com.example.entity")
    .findClassesWithAnnotation(Entity.class);
```

### 3.22 ReflectCache

> 反射结果集中缓存，提升反射操作性能。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `getFields(Class<?> clazz)` | 获取缓存的字段数组 |
| `cacheFields(Class<?> clazz, Field[] fields)` | 缓存字段数组 |
| `getMethods(Class<?> clazz)` | 获取缓存的方法数组 |
| `cacheMethods(Class<?> clazz, Method[] methods)` | 缓存方法数组 |
| `getConstructors(Class<?> clazz)` | 获取缓存的构造器数组 |
| `cacheConstructors(Class<?> clazz, Constructor<?>[] constructors)` | 缓存构造器数组 |
| `getTypeToken(Type type)` | 获取缓存的 TypeToken |
| `cacheTypeToken(Type type, TypeToken<T> token)` | 缓存 TypeToken |
| `clearCache()` | 清除所有缓存 |
| `clearCache(Class<?> clazz)` | 清除指定类缓存 |
| `getCacheStats()` | 获取缓存统计 |

**CacheStats 记录:**

| 方法 | 描述 |
|------|------|
| `fieldCacheSize()` | 字段缓存大小 |
| `methodCacheSize()` | 方法缓存大小 |
| `constructorCacheSize()` | 构造器缓存大小 |
| `typeTokenCacheSize()` | TypeToken 缓存大小 |
| `totalSize()` | 总缓存大小 |
| `totalRequests()` | 总请求数 |

### 3.23 OpenReflectException

> 反射操作非受检异常，提供丰富的工厂方法。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `targetType()` | 目标类型 |
| `memberName()` | 成员名称 |
| `operation()` | 操作名称 |
| `fieldNotFound(Class<?> clazz, String fieldName)` | 字段未找到 |
| `fieldAccessFailed(Class<?> clazz, String fieldName, Throwable cause)` | 字段访问失败 |
| `methodNotFound(Class<?> clazz, String methodName, Class<?>[] paramTypes)` | 方法未找到 |
| `methodInvokeFailed(Class<?> clazz, String methodName, Throwable cause)` | 方法调用失败 |
| `constructorNotFound(Class<?> clazz, Class<?>[] paramTypes)` | 构造器未找到 |
| `instantiationFailed(Class<?> clazz, Throwable cause)` | 实例化失败 |
| `classNotFound(String className)` | 类未找到 |
| `classLoadFailed(String className, Throwable cause)` | 类加载失败 |
| `typeCastFailed(Class<?> targetType, Object value)` | 类型转换失败 |
| `copyFailed(Class<?> sourceType, Class<?> targetType, Throwable cause)` | 复制失败 |
| `proxyCreationFailed(Class<?> interfaceType, Throwable cause)` | 代理创建失败 |
| `annotationNotFound(Class<?> clazz, Class<?> annotationType)` | 注解未找到 |
| `lambdaParseFailed(String reason)` | Lambda 解析失败 |
| `recordOperationFailed(Class<?> recordClass, String operation, Throwable cause)` | Record 操作失败 |
| `illegalAccess(Class<?> clazz, String memberName, Throwable cause)` | 非法访问 |
