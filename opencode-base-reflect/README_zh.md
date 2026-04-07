# OpenCode Base Reflect

**全面的 Java 25+ 反射工具库**

`opencode-base-reflect` 提供了丰富的反射工具集，涵盖类、字段、方法、构造器、注解、代理、Bean、Record、密封类、Lambda 内省、类型解析和类路径扫描。使用了 Record、密封类、VarHandle/MethodHandle 等现代 Java 特性。

## 功能特性

### 核心反射
- **类操作**：加载类，检查类型（原始类型、包装类型、Record、密封类），导航类层次结构
- **字段操作**：读写字段（包括私有字段），列出所有字段（含继承）
- **方法操作**：查找和调用方法（实例和静态），列出所有方法
- **构造器操作**：查找构造器，带参数或无参数创建实例
- **注解操作**：在任意元素上获取、查找（Optional）和检查注解
- **修饰符检查**：public、private、static、final、abstract、synchronized 检查

### Bean 工具
- **BeanCopier**：在 Bean 之间复制属性，支持类型转换
- **BeanMap**：Bean 属性的 Map 视图
- **BeanPath**：通过点分隔路径访问嵌套属性
- **BeanUtil**：通用 Bean 操作工具
- **BeanDiff**：检测两个 Bean 之间的差异
- **PropertyDescriptor**：描述 Bean 属性（名称、类型、getter、setter）
- **PropertyConverter**：在类型之间转换属性值

### 代理与调用
- **ProxyFactory**：使用流式 API 创建 JDK 动态代理
- **MethodInterceptor**：拦截方法调用，支持前置/后置/环绕语义
- **AbstractInvocationHandler**：自定义调用处理器的基类
- **Invokable**：方法和构造器的统一抽象

### Lambda 内省
- **LambdaUtil**：检查 Lambda 表达式并提取方法引用
- **LambdaInfo**：Lambda 的元数据（实现方法、捕获参数）
- **SerializableFunction/Consumer/Predicate/Supplier/BiFunction**：可序列化的函数式接口，用于 Lambda 内省

### Record 支持
- **RecordUtil**：Java Record 工具（组件、构造、转换）
- **RecordBuilder**：动态构建 Record 实例
- **RecordComponent**：描述 Record 组件
- **RecordMapper**：高级 Record/Bean 映射，支持字段重命名、排除和类型转换

### 密封类支持
- **SealedUtil**：密封类和接口的工具
- **PermittedSubclasses**：枚举和检查许可的子类
- **SealedDispatcher**：类型安全的密封类穷举分发器

### 类型系统
- **TypeToken**：在运行时捕获完整的泛型类型信息
- **TypeLiteral**：字面量类型表示
- **TypeResolver**：根据声明类解析泛型类型
- **TypeUtil**：java.lang.reflect.Type 工具
- **TypeVariableUtil**：在类层次结构中解析类型变量
- **ParameterizedTypeImpl/GenericArrayTypeImpl/WildcardTypeImpl**：自定义 Type 实现

### 类路径扫描
- **ClassScanner**：扫描包中匹配条件的类
- **AnnotationScanner**：查找带有特定注解的类
- **ResourceScanner**：扫描类路径中的资源
- **ClassPath**：带扫描能力的完整类路径表示
- **ClassInfo/ResourceInfo**：已发现的类和资源的元数据

### 访问器层
- **FieldAccessor**：直接字段访问（基于反射）
- **MethodHandleAccessor**：基于 MethodHandle 的高性能访问
- **VarHandleAccessor**：基于 VarHandle 的原子操作访问
- **LambdaAccessor**：基于 LambdaMetafactory 的近零开销访问（预热后最快）
- **PropertyAccessor**：统一属性访问接口
- **PropertyAccessors**：创建最优访问器实例的工厂（支持 LAMBDA 策略）
- **BeanAccessor**：高级 Bean 属性访问器

### 模块系统
- **ModuleUtil**：模块系统感知反射工具，支持可达性检查、诊断和优雅降级

### 注解合并
- **AnnotationMerger**：组合注解属性解析（类似 Spring 的 AnnotatedElementUtils）

### 方法签名
- **MethodSignature**：方法签名表示、匹配、覆盖检测和 JVM 描述符生成

### 性能与安全
- **ReflectCache**：GC 感知的反射元数据缓存（WeakHashMap 防止 ClassLoader 泄漏）
- **平台类型保护**：所有 `setAccessible` 调用均对 JDK 内部类型（`java.*`、`javax.*`、`sun.*`、`jdk.*`）进行拦截

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-reflect</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 基本反射
```java
import cloud.opencode.base.reflect.OpenReflect;

// 加载类
Class<?> clazz = OpenReflect.forName("com.example.User");

// 读写字段
Object name = OpenReflect.readField(user, "name");
OpenReflect.writeField(user, "name", "Alice");

// 调用方法
Object result = OpenReflect.invokeMethod(user, "getName");

// 创建实例
User user = OpenReflect.newInstance(User.class);
User user2 = OpenReflect.newInstance(User.class, "Alice", 25);

// 检查注解
boolean hasAnno = OpenReflect.hasAnnotation(method, Deprecated.class);
```

### Bean 操作
```java
import cloud.opencode.base.reflect.bean.*;

// 在 Bean 之间复制属性
BeanCopier.copy(source, target);

// 访问嵌套属性
Object city = BeanPath.get(user, "address.city");

// 将 Bean 视为 Map
Map<String, Object> map = BeanMap.of(user);

// 比较 Bean
List<BeanDiff.Diff> diffs = BeanDiff.diff(user1, user2);
```

### 动态代理
```java
import cloud.opencode.base.reflect.proxy.*;

MyService proxy = ProxyFactory.create(MyService.class, (obj, method, args) -> {
    System.out.println("调用前: " + method.getName());
    Object result = method.invoke(realService, args);
    System.out.println("调用后: " + method.getName());
    return result;
});
```

### Record 工具
```java
import cloud.opencode.base.reflect.record.*;

// 动态构建 Record
Person person = RecordBuilder.of(Person.class)
    .set("name", "Alice")
    .set("age", 25)
    .build();

// 获取 Record 组件
List<RecordComponent> components = RecordUtil.getComponents(Person.class);
```

### LambdaAccessor（高性能访问器）
```java
import cloud.opencode.base.reflect.accessor.*;

// 通过 LambdaMetafactory 实现近零开销属性访问
PropertyAccessor<User> accessor = PropertyAccessors.create(User.class, "name", PropertyAccessors.Strategy.LAMBDA);
String name = accessor.get(user, String.class);
accessor.set(user, "Alice");
```

### 密封类分发
```java
import cloud.opencode.base.reflect.sealed.*;

// 类型安全的密封类穷举分发
SealedDispatcher<Shape, Double> areaCalc = SealedDispatcher.builder(Shape.class, Double.class)
    .on(Circle.class, c -> Math.PI * c.radius() * c.radius())
    .on(Rectangle.class, r -> r.width() * r.height())
    .on(Triangle.class, t -> 0.5 * t.base() * t.height())
    .build(); // 验证所有分支都已覆盖

double area = areaCalc.dispatch(someShape);
```

### Record 映射
```java
import cloud.opencode.base.reflect.record.*;

// Record ↔ Record / Bean 映射，支持重命名和转换
RecordMapper<UserDTO, User> mapper = RecordMapper.builder(UserDTO.class, User.class)
    .map("userName", "name")
    .exclude("password")
    .convert("age", val -> Integer.parseInt(val.toString()))
    .build();

User user = mapper.map(dto);
```

### 注解合并
```java
import cloud.opencode.base.reflect.AnnotationMerger;

// 解析组合注解属性（类似 Spring 的 @GetMapping → @RequestMapping）
RequestMapping merged = AnnotationMerger.getMergedAnnotation(method, RequestMapping.class);
```

### 模块系统工具
```java
import cloud.opencode.base.reflect.module.ModuleUtil;

// 检查是否可以进行深度反射
boolean canReflect = ModuleUtil.canDeepReflect(targetClass, callerClass);
String diagnostic = ModuleUtil.getAccessDiagnostic(targetClass, callerClass);
```

### 方法签名
```java
import cloud.opencode.base.reflect.invokable.MethodSignature;

MethodSignature sig = MethodSignature.of(method);
boolean isOverride = sig.isOverrideOf(superMethod);
String descriptor = sig.toDescriptor(); // "(Ljava/lang/String;I)V"
```

### 类型令牌
```java
import cloud.opencode.base.reflect.type.*;

TypeToken<List<String>> token = new TypeToken<List<String>>() {};
Type type = token.getType(); // java.util.List<java.lang.String>
```

## 类参考

### 根包 (`cloud.opencode.base.reflect`)
| 类 | 说明 |
|----|------|
| `OpenReflect` | 所有反射操作的主门面 |
| `OpenClass` | 类加载、类型检查和层次结构导航 |
| `OpenField` | 字段访问和操作 |
| `OpenMethod` | 方法发现和调用 |
| `OpenConstructor` | 构造器发现和实例创建 |
| `OpenAnnotation` | 注解发现和检查 |
| `OpenModifier` | 修饰符标志检查工具 |
| `ClassUtil` | 额外的类工具方法 |
| `FieldUtil` | 额外的字段工具方法 |
| `MethodUtil` | 额外的方法工具方法 |
| `ConstructorUtil` | 额外的构造器工具方法 |
| `AnnotationUtil` | 额外的注解工具方法 |
| `ModifierUtil` | 额外的修饰符工具方法 |
| `ReflectUtil` | 通用反射辅助方法 |
| `ReflectCache` | 反射元数据的线程安全缓存 |
| `AnnotationMerger` | 组合注解属性合并和合成 |

### 访问器 (`cloud.opencode.base.reflect.accessor`)
| 类 | 说明 |
|----|------|
| `PropertyAccessor` | 统一属性访问接口 |
| `PropertyAccessors` | 创建最优访问器实例的工厂 |
| `FieldAccessor` | 基于字段的直接属性访问 |
| `MethodHandleAccessor` | 基于 MethodHandle 的高性能访问 |
| `VarHandleAccessor` | 基于 VarHandle 的最高性能访问 |
| `BeanAccessor` | 高级 Bean 属性访问器 |
| `LambdaAccessor` | 基于 LambdaMetafactory 的近零开销访问 |

### Bean (`cloud.opencode.base.reflect.bean`)
| 类 | 说明 |
|----|------|
| `OpenBean` | Bean 操作门面 |
| `BeanCopier` | 在 Bean 实例之间复制属性 |
| `BeanMap` | Bean 属性的 Map 视图 |
| `BeanPath` | 通过点分隔路径访问嵌套属性 |
| `BeanUtil` | 通用 Bean 操作工具 |
| `BeanDiff` | 检测和报告两个 Bean 之间的差异 |
| `PropertyDescriptor` | 描述一个 Bean 属性 |
| `PropertyConverter` | 在类型之间转换属性值 |

### 可调用 (`cloud.opencode.base.reflect.invokable`)
| 类 | 说明 |
|----|------|
| `Invokable<T,R>` | 方法和构造器的统一抽象 |
| `MethodInvokable<T,R>` | 方法的 Invokable 包装器 |
| `ConstructorInvokable<T>` | 构造器的 Invokable 包装器 |
| `Parameter` | 描述方法/构造器参数 |
| `MethodSignature` | 方法签名表示、匹配和覆盖检测 |

### Lambda (`cloud.opencode.base.reflect.lambda`)
| 类 | 说明 |
|----|------|
| `OpenLambda` | Lambda 内省门面 |
| `LambdaUtil` | Lambda 检查和方法引用提取 |
| `LambdaInfo` | Lambda 表达式的元数据 |
| `FunctionalInterfaceUtil` | 函数式接口工具 |
| `SerializableFunction<T,R>` | 可序列化的 Function，用于 Lambda 内省 |
| `SerializableBiFunction<T,U,R>` | 可序列化的 BiFunction |
| `SerializableConsumer<T>` | 可序列化的 Consumer |
| `SerializablePredicate<T>` | 可序列化的 Predicate |
| `SerializableSupplier<T>` | 可序列化的 Supplier |
| `SerializedLambdaWrapper` | SerializedLambda 对象的包装器 |

### 代理 (`cloud.opencode.base.reflect.proxy`)
| 类 | 说明 |
|----|------|
| `OpenProxy` | 代理创建门面 |
| `ProxyFactory` | 创建 JDK 动态代理的工厂 |
| `ProxyUtil` | 代理工具方法 |
| `MethodInterceptor` | 方法调用拦截器接口 |
| `MethodInvoker` | 方法调用辅助器 |
| `AbstractInvocationHandler` | 调用处理器的基类 |

### Record (`cloud.opencode.base.reflect.record`)
| 类 | 说明 |
|----|------|
| `OpenRecord` | Record 操作门面 |
| `RecordBuilder<T>` | Record 实例的动态构建器 |
| `RecordComponent` | 描述一个 Record 组件 |
| `RecordUtil` | Record 工具方法 |
| `RecordMapper<S,T>` | 高级 Record/Bean 映射，支持重命名、排除和转换 |

### 密封类 (`cloud.opencode.base.reflect.sealed`)
| 类 | 说明 |
|----|------|
| `OpenSealed` | 密封类操作门面 |
| `SealedUtil` | 密封类工具方法 |
| `PermittedSubclasses` | 枚举密封类型的许可子类 |
| `SealedDispatcher<T,R>` | 类型安全的密封类穷举分发器 |

### 模块 (`cloud.opencode.base.reflect.module`)
| 类 | 说明 |
|----|------|
| `ModuleUtil` | 模块系统感知的反射工具，支持可访问性检查和诊断 |
| `ModuleUtil.ModuleInfo` | 包含模块元数据的记录 |

### 扫描 (`cloud.opencode.base.reflect.scan`)
| 类 | 说明 |
|----|------|
| `ClassScanner` | 扫描包中匹配条件的类 |
| `AnnotationScanner` | 查找带有特定注解的类 |
| `ResourceScanner` | 扫描类路径中的资源 |
| `ClassPath` | 带扫描能力的完整类路径表示 |
| `ClassInfo` | 已发现类的元数据 |
| `ResourceInfo` | 已发现资源的元数据 |

### 类型 (`cloud.opencode.base.reflect.type`)
| 类 | 说明 |
|----|------|
| `TypeToken<T>` | 在运行时捕获泛型类型信息 |
| `TypeLiteral<T>` | 字面量类型表示 |
| `TypeResolver` | 根据声明类解析泛型类型 |
| `TypeUtil` | java.lang.reflect.Type 工具 |
| `TypeVariableUtil` | 在层次结构中解析类型变量 |
| `ParameterizedTypeImpl` | 自定义 ParameterizedType 实现 |
| `GenericArrayTypeImpl` | 自定义 GenericArrayType 实现 |
| `WildcardTypeImpl` | 自定义 WildcardType 实现 |

### 异常 (`cloud.opencode.base.reflect.exception`)
| 类 | 说明 |
|----|------|
| `OpenReflectException` | 反射错误的运行时异常 |

## 环境要求

- Java 25+
- 无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
