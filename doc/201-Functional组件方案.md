# Functional 组件方案

## 1. 组件概述

### 1.1 设计目标

`opencode-base-functional` 模块提供完整的函数式编程工具库，基于 JDK 25 构建，充分利用 Sealed Interface、Record、Virtual Thread 等现代特性。

**核心特性：**
- Monad 类型（Try / Either / Option / Validation / Lazy / Sequence / Trampoline）
- For 表达式（简化 flatMap 嵌套）
- 函数工具（组合、柯里化、记忆化、部分应用）
- 模式匹配（OpenMatch + Case + Pattern）
- 管道操作（Pipeline + PipeUtil）
- 光学类型（Lens / OptionalLens）
- 异步工具（Future / LazyAsync / AsyncFunctionUtil，基于 Virtual Thread）
- Record 工具（RecordUtil，反射式 Record 操作）
- 增强函数接口（CheckedBiFunction / CheckedBiConsumer / TriFunction）

**与 Core 组件的关系：**
- **元组（Tuple）**：使用 `core.tuple` 包，本组件不重复定义
- **函数接口**：`core.func` 提供基础的可抛异常接口（CheckedSupplier, CheckedConsumer, CheckedFunction 等），本组件在此基础上提供更丰富的函数式工具

### 1.2 架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                   OpenFunctional (统一门面)                       │
│  tryOf() / either() / option() / match() / lens() / pipe()     │
│  compose() / curry() / memoize() / async() / parallelMap()     │
└─────────────────────────────────────────────────────────────────┘
        │                       │                       │
        ▼                       ▼                       ▼
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│   Monad 类型    │   │   函数工具      │   │   模式匹配      │
│                 │   │                 │   │                 │
│ Try<T>          │   │ FunctionUtil   │   │ OpenMatch       │
│ Either<L,R>     │   │ compose()      │   │ Case            │
│ Option<T>       │   │ curry()        │   │ Pattern         │
│ Validation<E,T> │   │ memoize()      │   │                 │
│ Lazy<T>         │   │ partial()      │   │                 │
│ Sequence<T>     │   │ flip()         │   │                 │
│ Trampoline<T>   │   │ unchecked()    │   │                 │
│ For             │   │                 │   │                 │
└─────────────────┘   └─────────────────┘   └─────────────────┘
        │                       │                       │
        └───────────────────────┼───────────────────────┘
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    管道 & 光学 & 异步                              │
│  ┌───────────┐  ┌───────────┐  ┌────────────┐  ┌────────────┐  │
│  │ Pipeline  │  │ PipeUtil  │  │ Lens       │  │ Future     │  │
│  │ (数据流)  │  │ (管道)    │  │ OptLens    │  │ LazyAsync  │  │
│  └───────────┘  └───────────┘  └────────────┘  └────────────┘  │
│  ┌───────────┐  ┌────────────────────────────────────────────┐  │
│  │ RecordUtil│  │ AsyncFunctionUtil (Virtual Thread 异步)    │  │
│  └───────────┘  └────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Core 组件基础设施                               │
│  ┌───────────┐  ┌───────────┐  ┌─────────────────────────────┐  │
│  │   Tuple   │  │   Opt     │  │  CheckedSupplier/Consumer  │  │
│  │ Tuple2/3/4│  │(增强Opt)  │  │  CheckedFunction/Predicate │  │
│  └───────────┘  └───────────┘  └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 模块依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-functional</artifactId>
    <version>${version}</version>
</dependency>
```

**依赖关系：**
```
functional 模块依赖:
└── opencode-base-core (必需，提供 Tuple、基础函数接口)
```

---

## 2. 包结构

```
cloud.opencode.base.functional
├── OpenFunctional.java                 # 统一门面入口
│
├── monad/                              # Monad 类型
│   ├── Try.java                        # Try Monad（封装可能失败的计算）
│   ├── Either.java                     # Either Monad（二选一类型）
│   ├── Option.java                     # Option Monad（增强版 Optional）
│   ├── Validation.java                 # Validation Monad（累积错误验证）
│   ├── Lazy.java                       # 惰性求值容器
│   ├── Sequence.java                   # 惰性求值序列
│   ├── Trampoline.java                 # 蹦床（栈安全递归）
│   └── For.java                        # For 表达式（简化 flatMap）
│
├── function/                           # 函数工具
│   ├── FunctionUtil.java               # 函数组合/柯里化/记忆化工具
│   ├── CheckedFunction.java            # 可抛异常的单参函数
│   ├── CheckedBiFunction.java          # 可抛异常的双参函数
│   ├── CheckedBiConsumer.java          # 可抛异常的双参消费者
│   └── TriFunction.java               # 三参函数接口
│
├── pattern/                            # 模式匹配
│   ├── OpenMatch.java                  # 模式匹配入口
│   ├── Case.java                       # 匹配分支定义
│   └── Pattern.java                    # 模式接口
│
├── pipeline/                           # 管道操作
│   ├── Pipeline.java                   # 可组合的数据转换管道
│   └── PipeUtil.java                   # 管道工具方法
│
├── optics/                             # 光学类型
│   ├── Lens.java                       # 透镜（不可变数据读写）
│   └── OptionalLens.java              # 可选透镜（可选数据读写）
│
├── async/                              # 异步工具
│   ├── Future.java                     # Future 函数式包装
│   ├── LazyAsync.java                  # 惰性异步计算
│   └── AsyncFunctionUtil.java          # Virtual Thread 异步工具
│
├── record/                             # Record 工具
│   └── RecordUtil.java                 # Java Record 反射工具
│
└── exception/                          # 异常
    ├── OpenFunctionalException.java    # 函数式操作基础异常
    └── OpenMatchException.java         # 模式匹配失败异常
```

---

## 3. OpenFunctional 门面

`OpenFunctional` 是函数式编程组件的统一入口点，提供所有核心类型的工厂方法。

```java
public final class OpenFunctional {

    // ==================== Try ====================
    /** 执行可能失败的操作 */
    public static <T> Try<T> tryOf(CheckedSupplier<T> supplier);
    /** 创建成功 Try */
    public static <T> Try<T> success(T value);
    /** 创建失败 Try */
    public static <T> Try<T> failure(Throwable throwable);

    // ==================== Either ====================
    /** 创建 Left */
    public static <L, R> Either<L, R> left(L value);
    /** 创建 Right */
    public static <L, R> Either<L, R> right(R value);

    // ==================== Option ====================
    /** 创建 Some */
    public static <T> Option<T> some(T value);
    /** 创建 None */
    public static <T> Option<T> none();
    /** 从可能为 null 的值创建 Option */
    public static <T> Option<T> option(T value);

    // ==================== Validation ====================
    /** 创建有效值 */
    public static <E, T> Validation<E, T> valid(T value);
    /** 创建无效值 */
    public static <E, T> Validation<E, T> invalid(E error);

    // ==================== Lazy ====================
    /** 创建惰性计算 */
    public static <T> Lazy<T> lazy(Supplier<T> supplier);

    // ==================== 模式匹配 ====================
    /** 开始模式匹配 */
    public static <T> OpenMatch.Matcher<T> match(T value);
    /** 创建类型模式 */
    public static <T, R> Pattern<T, R> typePattern(Class<R> type);
    /** 创建类型匹配分支 */
    public static <T, U, R> Case<T, R> caseOf(Class<U> type, Function<? super U, ? extends R> action);
    /** 创建条件匹配分支 */
    public static <T, R> Case<T, R> when(Predicate<? super T> predicate,
                                          Function<? super T, ? extends R> action);

    // ==================== 函数工具 ====================
    /** 函数组合 */
    public static <A, B, C> Function<A, C> compose(Function<A, B> f, Function<B, C> g);
    /** 柯里化 */
    public static <A, B, C> Function<A, Function<B, C>> curry(BiFunction<A, B, C> f);
    /** 记忆化 */
    public static <T, R> Function<T, R> memoize(Function<T, R> f);
    /** 记忆化（指定缓存大小） */
    public static <T, R> Function<T, R> memoize(Function<T, R> f, int maxSize);
    /** 包装可抛异常双参函数 */
    public static <T, U, R> CheckedBiFunction<T, U, R> checkedBiFunction(...);
    /** 包装可抛异常双参消费者 */
    public static <T, U> CheckedBiConsumer<T, U> checkedBiConsumer(...);
    /** 将可抛异常双参函数转为普通函数 */
    public static <T, U, R> BiFunction<T, U, R> uncheckedBiFunction(...);

    // ==================== Lens ====================
    /** 创建透镜 */
    public static <S, A> Lens<S, A> lens(Function<S, A> getter, BiFunction<S, A, S> setter);
    /** 创建可选透镜 */
    public static <S, A> OptionalLens<S, A> optionalLens(...);

    // ==================== 管道 ====================
    /** 创建 Pipeline */
    public static <T> Pipeline.PipelineBuilder<T> pipeline(T value);
    /** 创建 Pipe */
    public static <T> PipeUtil.Pipe<T> pipe(T value);

    // ==================== 异步 ====================
    /** 异步执行（Virtual Thread） */
    public static <T> CompletableFuture<T> async(Supplier<T> supplier);
    /** 带超时异步执行 */
    public static <T> Try<T> asyncTimeout(Supplier<T> supplier, Duration timeout);
    /** 惰性异步 */
    public static <T> LazyAsync<T> lazyAsync(Supplier<T> supplier);
    /** 并行映射 */
    public static <T, R> List<R> parallelMap(List<T> items, Function<T, R> mapper);

    // ==================== Record 工具 ====================
    /** 为 Record 组件创建 Lens */
    public static <R extends Record, T> Lens<R, T> recordLens(Class<R> recordClass,
                                                                String componentName);
    /** 复制 Record 并修改字段 */
    public static <R extends Record> R copyRecord(R record,
                                                    Map<String, Object> modifications);
    /** Record 转 Map */
    public static Map<String, Object> recordToMap(Record record);
}
```

**使用示例：**

```java
// Try
Try<Integer> result = OpenFunctional.tryOf(() -> Integer.parseInt(input));

// Either
Either<String, User> user = OpenFunctional.right(new User("Alice"));

// Option
Option<String> name = OpenFunctional.some("value");

// 模式匹配
String type = OpenFunctional.match(value)
    .caseOf(String.class, s -> "字符串: " + s)
    .caseOf(Integer.class, n -> "整数: " + n)
    .orElseGet("未知类型");

// 函数组合
Function<String, Integer> parseAndDouble = OpenFunctional.compose(
    Integer::parseInt, n -> n * 2);

// 管道
String result = OpenFunctional.pipe("  hello  ")
    .then(String::trim)
    .then(String::toUpperCase)
    .get();

// 异步
CompletableFuture<Data> future = OpenFunctional.async(() -> fetchData());
```

---

## 4. Monad 类型

### 4.1 Try<T>

封装可能失败的计算。使用 Sealed Interface 实现，子类型为 `Success` 和 `Failure`。

```java
public sealed interface Try<T> permits Try.Success, Try.Failure {

    // ==================== 工厂方法 ====================
    /** 执行可能失败的操作 */
    static <T> Try<T> of(CheckedSupplier<T> supplier);
    /** 创建成功 */
    static <T> Try<T> success(T value);
    /** 创建失败 */
    static <T> Try<T> failure(Throwable t);

    // ==================== 查询方法 ====================
    boolean isSuccess();
    boolean isFailure();
    /** 获取值（失败时抛异常） */
    T get();
    /** 获取异常（成功时为空） */
    Optional<Throwable> getCause();

    // ==================== 转换方法 ====================
    <U> Try<U> map(Function<? super T, ? extends U> mapper);
    <U> Try<U> flatMap(Function<? super T, Try<U>> mapper);
    Try<T> filter(Predicate<? super T> predicate);

    // ==================== 恢复方法 ====================
    T getOrElse(T defaultValue);
    Try<T> orElse(Try<T> other);
    Try<T> recover(Function<Throwable, T> recovery);
    Try<T> recoverWith(Function<Throwable, Try<T>> recovery);

    // ==================== 转换为其他类型 ====================
    Optional<T> toOptional();
    Either<Throwable, T> toEither();

    // ==================== 副作用 ====================
    Try<T> peek(Consumer<? super T> action);
    Try<T> onFailure(Consumer<Throwable> action);
    Try<T> onSuccess(Consumer<? super T> action);

    // ==================== 子类型 ====================
    record Success<T>(T value) implements Try<T> { ... }
    record Failure<T>(Throwable cause) implements Try<T> { ... }
}
```

**使用示例：**

```java
// 基本使用
Try<Integer> result = Try.of(() -> Integer.parseInt("123"));

// 链式处理
String message = Try.of(() -> readFile("config.json"))
    .map(content -> parse(content))
    .map(config -> config.getMessage())
    .getOrElse("默认消息");

// 错误恢复
User user = Try.of(() -> userService.findById(id))
    .recover(ex -> User.defaultUser())
    .get();

// 副作用
Try.of(() -> sendEmail(to, subject, body))
    .onSuccess(r -> log.info("发送成功"))
    .onFailure(ex -> log.error("发送失败", ex));

// 转换
Either<Throwable, Integer> either = result.toEither();
Optional<Integer> optional = result.toOptional();
```

---

### 4.2 Either<L, R>

表示两种可能值之一。通常 `Left` 表示错误，`Right` 表示成功。

```java
public sealed interface Either<L, R> permits Either.Left, Either.Right {

    // ==================== 工厂方法 ====================
    static <L, R> Either<L, R> left(L value);
    static <L, R> Either<L, R> right(R value);

    // ==================== 查询方法 ====================
    boolean isLeft();
    boolean isRight();
    Optional<L> getLeft();
    Optional<R> getRight();

    // ==================== 转换方法 ====================
    /** 映射右值 */
    <U> Either<L, U> map(Function<? super R, ? extends U> mapper);
    /** 扁平映射右值 */
    <U> Either<L, U> flatMap(Function<? super R, Either<L, U>> mapper);
    /** 映射左值 */
    <U> Either<U, R> mapLeft(Function<? super L, ? extends U> mapper);
    /** 双向映射 */
    <L2, R2> Either<L2, R2> bimap(Function<? super L, ? extends L2> leftMapper,
                                   Function<? super R, ? extends R2> rightMapper);

    // ==================== 恢复方法 ====================
    R getOrElse(R defaultValue);
    Either<L, R> orElse(Either<L, R> other);

    // ==================== 折叠 ====================
    /** 将 Left 或 Right 折叠为单一类型 */
    <T> T fold(Function<? super L, ? extends T> leftMapper,
               Function<? super R, ? extends T> rightMapper);

    // ==================== 其他 ====================
    /** 交换左右 */
    Either<R, L> swap();
    /** 右值副作用 */
    Either<L, R> peek(Consumer<? super R> action);
    /** 左值副作用 */
    Either<L, R> peekLeft(Consumer<? super L> action);

    // ==================== 子类型 ====================
    record Left<L, R>(L value) implements Either<L, R> { ... }
    record Right<L, R>(R value) implements Either<L, R> { ... }
}
```

**使用示例：**

```java
// 成功/失败
Either<String, User> success = Either.right(user);
Either<String, User> error = Either.left("用户不存在");

// 链式处理
String displayName = findUser(id)
    .map(User::getName)
    .map(String::toUpperCase)
    .getOrElse("未知用户");

// 折叠
String message = result.fold(
    err -> "错误: " + err,
    user -> "用户: " + user.getName()
);

// 双向映射
Either<Integer, String> mapped = result.bimap(
    error -> error.length(),
    user -> user.toString()
);
```

---

### 4.3 Option<T>

增强的 Optional，提供更多函数式操作。使用 Sealed Interface 实现，子类型为 `Some` 和 `None`。

```java
public sealed interface Option<T> permits Option.Some, Option.None {

    // ==================== 工厂方法 ====================
    static <T> Option<T> some(T value);
    static <T> Option<T> none();
    /** 从可能为 null 的值创建（null -> None） */
    static <T> Option<T> of(T value);

    // ==================== 查询方法 ====================
    boolean isSome();
    boolean isNone();
    /** 获取值（None 时抛异常） */
    T get();

    // ==================== 转换方法 ====================
    <U> Option<U> map(Function<? super T, ? extends U> mapper);
    <U> Option<U> flatMap(Function<? super T, Option<U>> mapper);
    Option<T> filter(Predicate<? super T> predicate);

    // ==================== 折叠 ====================
    <R> R fold(Supplier<? extends R> ifNone, Function<? super T, ? extends R> ifSome);

    // ==================== 恢复方法 ====================
    T getOrElse(T defaultValue);
    T getOrElse(Supplier<? extends T> supplier);
    Option<T> orElse(Option<T> other);
    Option<T> orElse(Supplier<Option<T>> supplier);

    // ==================== 转换为其他类型 ====================
    Optional<T> toOptional();
    <L> Either<L, T> toEither(L left);

    // ==================== 副作用 ====================
    Option<T> peek(Consumer<? super T> action);
    Option<T> onNone(Runnable action);

    // ==================== 子类型 ====================
    record Some<T>(T value) implements Option<T> { ... }
    record None<T>() implements Option<T> { ... }
}
```

**使用示例：**

```java
Option<String> some = Option.some("value");
Option<String> none = Option.none();
Option<String> fromNullable = Option.of(nullableValue);

// 链式处理
String result = Option.of(user)
    .map(User::getName)
    .filter(name -> !name.isEmpty())
    .map(String::toUpperCase)
    .getOrElse("默认名称");

// 使用 switch 模式匹配（JDK 25）
String desc = switch (option) {
    case Option.Some(var v) -> "有值: " + v;
    case Option.None() -> "无值";
};

// 转换
Optional<String> optional = some.toOptional();
Either<String, String> either = some.toEither("错误信息");
```

---

### 4.4 Validation<E, T>

累积错误的验证 Monad。与 Either 不同，Validation 可以收集多个错误。

```java
public sealed interface Validation<E, T> permits Validation.Valid, Validation.Invalid {

    // ==================== 工厂方法 ====================
    static <E, T> Validation<E, T> valid(T value);
    static <E, T> Validation<E, T> invalid(E error);
    static <E, T> Validation<E, T> invalid(List<E> errors);

    // ==================== 查询方法 ====================
    boolean isValid();
    boolean isInvalid();
    Optional<T> getValue();
    List<E> getErrors();

    // ==================== 转换方法 ====================
    <U> Validation<E, U> map(Function<? super T, ? extends U> mapper);
    <U> Validation<E, U> flatMap(Function<? super T, Validation<E, U>> mapper);
    /** 应用式组合 */
    <U> Validation<E, U> ap(Validation<E, Function<? super T, ? extends U>> vf);

    // ==================== 折叠 ====================
    <R> R fold(Function<? super List<E>, ? extends R> ifInvalid,
               Function<? super T, ? extends R> ifValid);

    // ==================== 转换为其他类型 ====================
    Either<List<E>, T> toEither();

    // ==================== 组合验证（静态方法） ====================
    static <E, T1, T2, R> Validation<E, R> combine(
        Validation<E, T1> v1, Validation<E, T2> v2,
        BiFunction<T1, T2, R> combiner);

    // ==================== 子类型 ====================
    record Valid<E, T>(T value) implements Validation<E, T> { ... }
    record Invalid<E, T>(List<E> errors) implements Validation<E, T> { ... }
}
```

**使用示例：**

```java
// 定义验证规则
Validation<String, String> validateName(String name) {
    return name != null && !name.isBlank()
        ? Validation.valid(name)
        : Validation.invalid("姓名不能为空");
}

Validation<String, Integer> validateAge(int age) {
    return age > 0 && age < 150
        ? Validation.valid(age)
        : Validation.invalid("年龄不合法: " + age);
}

// 组合验证（累积所有错误）
Validation<String, User> result = Validation.combine(
    validateName(name),
    validateAge(age),
    User::new
);

// 处理结果
result.fold(
    errors -> "验证失败: " + String.join(", ", errors),
    user -> "创建用户: " + user
);
```

---

### 4.5 Lazy<T>

惰性求值容器。计算延迟到首次访问时执行，且只执行一次，结果被缓存。线程安全。

```java
public final class Lazy<T> implements Supplier<T> {

    /** 创建惰性计算 */
    public static <T> Lazy<T> of(Supplier<T> supplier);

    /** 创建已有值的惰性容器 */
    public static <T> Lazy<T> value(T value);

    /** 获取值（首次调用时执行计算） */
    public T get();

    /** 是否已经求值 */
    public boolean isEvaluated();

    /** 映射 */
    public <U> Lazy<U> map(Function<? super T, ? extends U> mapper);

    /** 扁平映射 */
    public <U> Lazy<U> flatMap(Function<? super T, Lazy<U>> mapper);

    /** 过滤 */
    public Lazy<T> filter(Predicate<? super T> predicate);

    /** 获取值或默认值 */
    public T getOrElse(T defaultValue);
    public T getOrElse(Supplier<? extends T> supplier);

    /** 转换为 Try */
    public Try<T> toTry();

    /** 转换为 Option */
    public Option<T> toOption();
}
```

**使用示例：**

```java
// 延迟创建昂贵对象
Lazy<ExpensiveObject> lazy = Lazy.of(() -> createExpensiveObject());

// 尚未计算
System.out.println(lazy.isEvaluated()); // false

// 首次访问时计算
ExpensiveObject obj = lazy.get();
System.out.println(lazy.isEvaluated()); // true

// 再次访问，使用缓存值
ExpensiveObject same = lazy.get(); // 不会重新计算

// 链式转换（惰性）
Lazy<String> result = Lazy.of(() -> fetchData())
    .map(data -> process(data))
    .map(processed -> format(processed));
```

---

### 4.6 Sequence<T>

惰性求值序列，类似 Kotlin 的 Sequence 或 Scala 的 LazyList。与 Java Stream 不同，Sequence 可以重复遍历。

```java
public final class Sequence<T> implements Iterable<T> {

    // ==================== 工厂方法 ====================
    /** 空序列 */
    public static <T> Sequence<T> empty();
    /** 从可变参数创建 */
    @SafeVarargs
    public static <T> Sequence<T> of(T... elements);
    /** 从 Iterable 创建 */
    public static <T> Sequence<T> from(Iterable<T> iterable);
    /** 从 Stream 创建 */
    public static <T> Sequence<T> fromStream(Stream<T> stream);
    /** 创建迭代序列（可无限） */
    public static <T> Sequence<T> iterate(T seed, UnaryOperator<T> f);
    /** 创建生成序列（可无限） */
    public static <T> Sequence<T> generate(Supplier<T> supplier);
    /** 创建整数范围 [start, end) */
    public static Sequence<Integer> range(int start, int end);
    /** 创建整数范围 [start, end] */
    public static Sequence<Integer> rangeClosed(int start, int end);

    // ==================== 中间操作（惰性） ====================
    public <R> Sequence<R> map(Function<? super T, ? extends R> mapper);
    public <R> Sequence<R> flatMap(Function<? super T, ? extends Sequence<R>> mapper);
    public Sequence<T> filter(Predicate<? super T> predicate);
    public Sequence<T> filterNot(Predicate<? super T> predicate);
    public Sequence<T> take(int n);
    public Sequence<T> takeWhile(Predicate<? super T> predicate);
    public Sequence<T> drop(int n);
    public Sequence<T> dropWhile(Predicate<? super T> predicate);
    public Sequence<T> distinct();
    public Sequence<T> sorted();
    public Sequence<T> sorted(Comparator<? super T> comparator);
    public <U, R> Sequence<R> zip(Sequence<U> other, BiFunction<? super T, ? super U, ? extends R> zipper);
    public Sequence<IndexedValue<T>> zipWithIndex();

    // ==================== 终端操作 ====================
    public <R> R fold(R initial, BiFunction<? super R, ? super T, ? extends R> folder);
    public Optional<T> reduce(BinaryOperator<T> reducer);
    public List<T> toList();
    public Set<T> toSet();
    public <R, A> R collect(Collector<? super T, A, R> collector);
    public Optional<T> find(Predicate<? super T> predicate);
    public Optional<T> first();
    public Optional<T> last();
    public boolean any(Predicate<? super T> predicate);
    public boolean all(Predicate<? super T> predicate);
    public boolean none(Predicate<? super T> predicate);
    public long count();
    public void forEach(Consumer<? super T> action);
    public Stream<T> toStream();

    /** 带索引的值 */
    public record IndexedValue<T>(int index, T value) {}
}
```

**使用示例：**

```java
// 基本使用
Sequence<Integer> seq = Sequence.of(1, 2, 3, 4, 5);
List<Integer> result = seq
    .filter(n -> n % 2 == 0)
    .map(n -> n * 2)
    .toList();  // [4, 8]

// 无限序列
Sequence<Integer> naturals = Sequence.iterate(1, n -> n + 1);
List<Integer> first10 = naturals.take(10).toList();

// 随机数序列
Sequence<Double> randoms = Sequence.generate(Math::random);
List<Double> sample = randoms.take(5).toList();

// 范围
Sequence.range(1, 100)
    .filter(n -> n % 3 == 0)
    .take(10)
    .forEach(System.out::println);

// zip
Sequence<String> names = Sequence.of("Alice", "Bob", "Charlie");
Sequence<Integer> ages = Sequence.of(25, 30, 35);
List<String> pairs = names.zip(ages, (n, a) -> n + ":" + a).toList();
```

---

### 4.7 Trampoline<T>

蹦床模式，将递归转换为栈安全的迭代，避免 StackOverflowError。

```java
public sealed interface Trampoline<T> permits Trampoline.Done, Trampoline.More, Trampoline.FlatMap {

    /** 返回最终值（递归终止） */
    static <T> Trampoline<T> done(T value);

    /** 返回下一步计算（递归继续） */
    static <T> Trampoline<T> more(Supplier<Trampoline<T>> next);

    /** 执行蹦床，获取最终结果 */
    T run();

    /** 是否已完成 */
    boolean isDone();

    /** 映射结果 */
    <U> Trampoline<U> map(Function<? super T, ? extends U> mapper);

    /** 扁平映射 */
    <U> Trampoline<U> flatMap(Function<? super T, Trampoline<U>> f);
}
```

**使用示例：**

```java
// 阶乘（栈安全）
Trampoline<Long> factorial(long n, long acc) {
    if (n <= 1) return Trampoline.done(acc);
    return Trampoline.more(() -> factorial(n - 1, n * acc));
}

long result = factorial(100000, 1).run(); // 不会 StackOverflow

// 斐波那契
Trampoline<Long> fibonacci(int n, long a, long b) {
    if (n == 0) return Trampoline.done(a);
    return Trampoline.more(() -> fibonacci(n - 1, b, a + b));
}

// 链式转换
Trampoline<String> formatted = factorial(100, 1)
    .map(n -> "结果: " + n);
String result = formatted.run();

// 相互递归
Trampoline<Boolean> isEven(int n) {
    if (n == 0) return Trampoline.done(true);
    return Trampoline.more(() -> isOdd(n - 1));
}

Trampoline<Boolean> isOdd(int n) {
    if (n == 0) return Trampoline.done(false);
    return Trampoline.more(() -> isEven(n - 1));
}
```

---

### 4.8 For 表达式

简化多重 flatMap 嵌套，支持 Option、Try、Iterable、Sequence 四种类型。支持 1 到 4 个变量的绑定。

```java
public final class For {

    // ==================== Option For ====================
    public static <T> OptionFor1<T> of(Option<T> option);

    public static final class OptionFor1<T1> {
        public <T2> OptionFor2<T1, T2> and(Option<T2> v2);
        public <T2> OptionFor2<T1, T2> and(Supplier<Option<T2>> supplier);
        public <R> Option<R> yield(Function<? super T1, ? extends R> mapper);
        public Option<T1> filter(Predicate<? super T1> predicate);
    }

    public static final class OptionFor2<T1, T2> {
        public <T3> OptionFor3<T1, T2, T3> and(Option<T3> v3);
        public <R> Option<R> yield(BiFunction<? super T1, ? super T2, ? extends R> mapper);
        public OptionFor2<T1, T2> filter(BiPredicate<? super T1, ? super T2> predicate);
    }

    // OptionFor3, OptionFor4 类似...

    // ==================== Try For ====================
    public static <T> TryFor1<T> of(Try<T> t);

    public static final class TryFor1<T1> {
        public <T2> TryFor2<T1, T2> and(Try<T2> v2);
        public <T2> TryFor2<T1, T2> and(Supplier<Try<T2>> supplier);
        public <R> Try<R> yield(Function<? super T1, ? extends R> mapper);
        public Try<T1> filter(Predicate<? super T1> predicate);
    }

    // TryFor2, TryFor3, TryFor4 类似...

    // ==================== Iterable For ====================
    public static <T> IterableFor1<T> of(Iterable<T> iterable);
    // IterableFor1, IterableFor2, IterableFor3...

    // ==================== Sequence For ====================
    public static <T> SequenceFor1<T> of(Sequence<T> sequence);
    // SequenceFor1, SequenceFor2, SequenceFor3...

    // ==================== 辅助函数接口 ====================
    public interface Function3<T1, T2, T3, R> { R apply(T1 t1, T2 t2, T3 t3); }
    public interface Function4<T1, T2, T3, T4, R> { R apply(T1 t1, T2 t2, T3 t3, T4 t4); }
    public interface Function5<T1, T2, T3, T4, T5, R> { R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5); }
}
```

**使用示例：**

```java
// Option For 表达式
Option<String> result = For.of(getName())
    .and(getAge())
    .yield((name, age) -> name + ":" + age);

// Try For 表达式
Try<Integer> result = For.of(parseNumber(a))
    .and(parseNumber(b))
    .yield((x, y) -> x + y);

// Iterable For 表达式（笛卡尔积）
List<String> pairs = For.of(List.of("a", "b"))
    .and(List.of("1", "2"))
    .yield((letter, number) -> letter + number)
    .toList();
// 结果: ["a1", "a2", "b1", "b2"]

// 三个变量
For.of(users)
    .and(roles)
    .and(permissions)
    .yield((user, role, perm) -> new Assignment(user, role, perm));
```

---

## 5. 函数工具

### 5.1 FunctionUtil

函数组合、柯里化、记忆化工具类。

```java
public final class FunctionUtil {

    // ==================== 函数组合 ====================
    /** 双函数组合 f.andThen(g) */
    public static <A, B, C> Function<A, C> compose(
        Function<A, B> f, Function<B, C> g);

    /** 三函数组合 */
    public static <A, B, C, D> Function<A, D> compose(
        Function<A, B> f, Function<B, C> g, Function<C, D> h);

    // ==================== 柯里化 ====================
    /** 二参柯里化 */
    public static <A, B, R> Function<A, Function<B, R>> curry(BiFunction<A, B, R> f);

    /** 三参柯里化 */
    public static <T1, T2, T3, R> Function<T1, Function<T2, Function<T3, R>>> curry(
        TriFunction<T1, T2, T3, R> f);

    /** 反柯里化 */
    public static <A, B, R> BiFunction<A, B, R> uncurry(Function<A, Function<B, R>> f);

    // ==================== 部分应用 ====================
    /** 固定第一个参数 */
    public static <A, B, R> Function<B, R> partial(BiFunction<A, B, R> f, A a);

    /** 固定第二个参数 */
    public static <A, B, R> Function<A, R> partialRight(BiFunction<A, B, R> f, B b);

    // ==================== 翻转 ====================
    /** 翻转参数顺序 */
    public static <A, B, R> BiFunction<B, A, R> flip(BiFunction<A, B, R> f);

    // ==================== 记忆化 ====================
    /** 记忆化函数（默认缓存 1000 条，LRU 驱逐） */
    public static <T, R> Function<T, R> memoize(Function<T, R> f);

    /** 记忆化函数（自定义缓存大小） */
    public static <T, R> Function<T, R> memoize(Function<T, R> f, int maxSize);

    /** 记忆化 Supplier */
    public static <T> Supplier<T> memoize(Supplier<T> supplier);

    // ==================== 异常转换 ====================
    /** 将 CheckedFunction 转为普通 Function */
    public static <T, R> Function<T, R> unchecked(CheckedFunction<T, R> f);

    /** 将 CheckedBiFunction 转为普通 BiFunction */
    public static <T, U, R> BiFunction<T, U, R> unchecked(CheckedBiFunction<T, U, R> f);

    // ==================== 工具函数 ====================
    /** 恒等函数 */
    public static <T> Function<T, T> identity();

    /** 常量函数 */
    public static <T, R> Function<T, R> constant(R value);

    /** 谓词取反 */
    public static <T> Predicate<T> not(Predicate<T> predicate);
}
```

**使用示例：**

```java
// 柯里化
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
Function<Integer, Integer> add5 = FunctionUtil.curry(add).apply(5);
System.out.println(add5.apply(3)); // 8

// 记忆化
Function<Integer, Integer> fib = FunctionUtil.memoize(n ->
    n <= 1 ? n : fib.apply(n - 1) + fib.apply(n - 2));

// 部分应用
BiFunction<String, String, String> concat = (a, b) -> a + b;
Function<String, String> hello = FunctionUtil.partial(concat, "Hello, ");
System.out.println(hello.apply("World")); // "Hello, World"

// 函数组合
Function<String, Integer> parseAndDouble = FunctionUtil.compose(
    Integer::parseInt, n -> n * 2);

// 异常转换
Function<Path, String> readFile = FunctionUtil.unchecked(
    path -> Files.readString(path));
```

---

### 5.2 CheckedBiFunction<T, U, R>

可抛出受检异常的双参函数接口。

```java
public interface CheckedBiFunction<T, U, R> {
    R apply(T t, U u) throws Exception;

    /** 转为普通 BiFunction */
    default BiFunction<T, U, R> unchecked();

    /** 带默认值执行 */
    default R applyOrDefault(T t, U u, R defaultValue);
}
```

### 5.3 CheckedBiConsumer<T, U>

可抛出受检异常的双参消费者接口。

```java
public interface CheckedBiConsumer<T, U> {
    void accept(T t, U u) throws Exception;

    /** 转为普通 BiConsumer */
    default BiConsumer<T, U> unchecked();
}
```

### 5.4 CheckedFunction<T, R>

可抛出受检异常的单参函数接口。

```java
public interface CheckedFunction<T, R> {
    R apply(T t) throws Exception;

    /** 转为普通 Function */
    default Function<T, R> unchecked();

    /** 带默认值执行 */
    default R applyOrDefault(T t, R defaultValue);
}
```

### 5.5 TriFunction<T1, T2, T3, R>

三参函数接口。

```java
public interface TriFunction<T1, T2, T3, R> {
    R apply(T1 t1, T2 t2, T3 t3);

    /** 后置函数组合 */
    default <V> TriFunction<T1, T2, T3, V> andThen(Function<? super R, ? extends V> after);
}
```

---

## 6. 模式匹配

### 6.1 OpenMatch

模式匹配入口，流式 API 风格。

```java
public final class OpenMatch {

    /** 创建匹配器 */
    public static <T> Matcher<T> of(T value);

    public static final class Matcher<T> {

        /** 类型匹配分支 */
        public <U, R> Matcher<T> caseOf(Class<U> type, Function<? super U, R> action);

        /** 条件匹配分支 */
        public <R> Matcher<T> when(Predicate<? super T> predicate, Function<? super T, R> action);

        /** 相等匹配分支 */
        public <R> Matcher<T> whenEquals(T expected, Function<? super T, R> action);

        /** null 匹配分支 */
        public <R> Matcher<T> whenNull(Function<? super T, R> action);

        /** Record 解构匹配 */
        public <R1, R2, Out> Matcher<T> caseRecord(
            Class<?> recordClass, Function<R1, Function<R2, Out>> combiner);

        /** Sealed 类型匹配 */
        public <S, R> Matcher<T> caseSealed(Class<S> sealedType, Function<S, R> action);

        /** 使用 Case 对象匹配 */
        public <R> Matcher<T> match(Case<T, R> matchCase);

        /** 使用 Pattern + Action 匹配 */
        public <U, R> Matcher<T> match(Pattern<T, U> pattern, Function<? super U, R> action);

        /** 默认分支（函数） */
        public <R> R orElse(Function<? super T, R> defaultAction);

        /** 默认分支（值） */
        public <R> R orElseGet(R defaultValue);

        /** 无默认分支（未匹配时抛异常） */
        public <R> R orElseThrow();

        /** 类型安全获取结果 */
        public <R> R getAs(Class<R> resultType, R defaultValue);
        public <R> R getAs(Class<R> resultType);

        /** 是否已匹配 */
        public boolean isMatched();

        /** 获取原始值 */
        public T value();
    }
}
```

**使用示例：**

```java
// 类型匹配
String result = OpenMatch.of(value)
    .caseOf(String.class, s -> "字符串: " + s)
    .caseOf(Integer.class, n -> "整数: " + n)
    .caseOf(List.class, l -> "列表，大小: " + l.size())
    .orElseGet("未知类型");

// 条件匹配
String desc = OpenMatch.of(number)
    .when(n -> n < 0, n -> "负数")
    .when(n -> n == 0, n -> "零")
    .when(n -> n > 0, n -> "正数")
    .orElseGet("未知");

// 相等匹配
String day = OpenMatch.of(dayOfWeek)
    .whenEquals(DayOfWeek.MONDAY, d -> "星期一")
    .whenEquals(DayOfWeek.FRIDAY, d -> "星期五")
    .orElse(d -> "其他: " + d);
```

---

### 6.2 Case<T, R>

匹配分支定义，组合模式与动作。

```java
public final class Case<T, R> {

    /** 类型匹配 */
    public static <T, U, R> Case<T, R> type(Class<U> type, Function<? super U, ? extends R> action);

    /** 条件匹配 */
    public static <T, R> Case<T, R> when(Predicate<? super T> predicate,
                                          Function<? super T, ? extends R> action);

    /** 相等匹配 */
    public static <T, R> Case<T, R> equals(T expected, Function<? super T, ? extends R> action);

    /** null 匹配 */
    public static <T, R> Case<T, R> isNull(Function<? super T, ? extends R> action);

    /** 默认匹配 */
    public static <T, R> Case<T, R> otherwise(Function<? super T, ? extends R> action);

    /** 从 Pattern 创建 */
    public static <T, U, R> Case<T, R> of(Pattern<T, U> pattern,
                                            Function<? super U, ? extends R> action);

    /** 应用匹配 */
    public Optional<R> apply(T value);

    /** 是否匹配 */
    public boolean matches(T value);

    /** 获取模式 */
    public Pattern<T, ?> pattern();

    /** 获取动作 */
    public Function<? super T, ? extends R> action();
}
```

---

### 6.3 Pattern<T, R>

模式接口。

```java
public interface Pattern<T, R> {

    /** 匹配值，成功返回提取的值，失败返回空 */
    Optional<R> match(T value);

    /** 是否匹配 */
    default boolean matches(T value);

    // ==================== 工厂方法 ====================
    /** 类型模式 */
    static <T, R> Pattern<T, R> type(Class<R> type);

    /** 条件模式 */
    static <T> Pattern<T, T> when(Predicate<? super T> predicate);

    /** 相等模式 */
    static <T> Pattern<T, T> equals(T expected);

    /** null 模式 */
    static <T> Pattern<T, T> isNull();

    /** 任意匹配模式 */
    static <T> Pattern<T, T> any();

    // ==================== 组合 ====================
    /** 与操作 */
    default Pattern<T, R> and(Pattern<T, ?> other);

    /** 或操作 */
    default Pattern<T, R> or(Pattern<T, R> other);

    /** 守卫条件 */
    default Pattern<T, R> guard(Predicate<? super R> condition);

    /** 转换 */
    default <U> Pattern<T, U> map(Function<? super R, ? extends U> mapper);
}
```

---

## 7. 管道操作

### 7.1 Pipeline<T, R>

可组合的数据转换管道，支持普通管道、Try 管道和集合管道。

```java
public final class Pipeline<T, R> {

    // ==================== 工厂方法 ====================
    /** 创建管道构建器 */
    public static <T> PipelineBuilder<T> of(T value);

    /** 创建恒等管道 */
    public static <T> Pipeline<T, T> identity();

    /** 从函数创建管道 */
    public static <T, R> Pipeline<T, R> from(Function<T, R> function);

    /** 创建集合管道 */
    public static <T> CollectionPipeline<T, T> ofCollection(Collection<T> collection);

    // ==================== 实例方法 ====================
    /** 应用管道 */
    public R apply(T input);

    /** 安全应用管道（返回 Try） */
    public Try<R> applyTry(T input);

    /** 追加转换 */
    public <U> Pipeline<T, U> andThen(Function<? super R, ? extends U> mapper);

    /** 前置管道 */
    public <V> Pipeline<V, R> compose(Pipeline<V, T> before);

    /** 转为 Function */
    public Function<T, R> toFunction();

    // ==================== PipelineBuilder ====================
    public static final class PipelineBuilder<T> {
        public <R> PipelineBuilder<R> map(Function<? super T, ? extends R> mapper);
        public <R> TryPipelineBuilder<R> mapTry(Function<? super T, ? extends R> mapper);
        public <R> PipelineBuilder<R> flatMap(Function<? super T, PipelineBuilder<R>> mapper);
        public PipelineBuilder<T> filter(Predicate<? super T> predicate);
        public PipelineBuilder<T> peek(Consumer<? super T> consumer);
        public T execute();
        public Optional<T> executeOptional();
        public T executeOrElse(T defaultValue);
        public T executeOrElseGet(Supplier<? extends T> supplier);
    }

    // ==================== TryPipelineBuilder ====================
    public static final class TryPipelineBuilder<T> {
        public <R> TryPipelineBuilder<R> map(Function<? super T, ? extends R> mapper);
        public <R> TryPipelineBuilder<R> flatMap(Function<? super T, Try<R>> mapper);
        public TryPipelineBuilder<T> filter(Predicate<? super T> predicate);
        public TryPipelineBuilder<T> recover(Function<Throwable, T> recovery);
        public Try<T> executeTry();
        public T execute();
        public T executeOrElse(T defaultValue);
    }

    // ==================== CollectionPipeline ====================
    public static final class CollectionPipeline<T, R> {
        public <U> CollectionPipeline<T, U> map(Function<? super R, ? extends U> mapper);
        public CollectionPipeline<T, R> filter(Predicate<? super T> predicate);
        public List<R> toList();
        public void forEach(Consumer<? super R> action);
        public long count();
        public Optional<R> findFirst();
        public boolean anyMatch(Predicate<? super R> predicate);
        public boolean allMatch(Predicate<? super R> predicate);
        public R reduce(R identity, BinaryOperator<R> accumulator);
    }
}
```

**使用示例：**

```java
// 普通管道
String result = Pipeline.of("  hello world  ")
    .map(String::trim)
    .map(String::toUpperCase)
    .map(s -> s + "!")
    .execute();
// "HELLO WORLD!"

// Try 管道
Try<Integer> parsed = Pipeline.of(userInput)
    .mapTry(Integer::parseInt)
    .map(n -> n * 2)
    .recover(ex -> 0)
    .executeTry();

// 集合管道
List<String> names = Pipeline.ofCollection(users)
    .filter(u -> u.isActive())
    .map(User::getName)
    .toList();

// 可组合管道
Pipeline<String, Integer> parseAndValidate = Pipeline.<String>identity()
    .andThen(String::trim)
    .andThen(Integer::parseInt);

int value = parseAndValidate.apply("  42  "); // 42
```

---

### 7.2 PipeUtil

管道工具方法，提供 Pipe 链式调用和各种转换工具。

```java
public final class PipeUtil {

    /** 创建 Pipe */
    public static <T> Pipe<T> pipe(T value);

    public static final class Pipe<T> {
        /** 转换 */
        public <R> Pipe<R> then(Function<? super T, ? extends R> function);
        /** 转换（null 安全） */
        public <R> Pipe<R> thenIfPresent(Function<? super T, ? extends R> function);
        /** 条件转换 */
        public Pipe<T> thenIf(boolean condition, UnaryOperator<T> function);
        /** 副作用 */
        public Pipe<T> tap(Consumer<? super T> consumer);
        /** 获取值 */
        public T get();
        /** 获取值或默认值 */
        public T getOrElse(T defaultValue);
    }

    // ==================== 条件工具 ====================
    /** 条件转换 */
    public static <T> T when(boolean condition, T value, UnaryOperator<T> function);
    /** 非空转换 */
    public static <T, R> R whenNonNull(T value, Function<? super T, ? extends R> function);
    /** 非空转换或默认值 */
    public static <T, R> R whenNonNullOrElse(T value, Function<? super T, ? extends R> function, R defaultValue);
    /** 谓词条件转换 */
    public static <T> T whenMatches(T value, Predicate<? super T> predicate, UnaryOperator<T> function);

    // ==================== tap 工具 ====================
    /** 执行副作用并返回原值 */
    public static <T> T tap(T value, Consumer<? super T> consumer);
    /** null 安全的副作用 */
    public static <T> T tapIfPresent(T value, Consumer<? super T> consumer);

    // ==================== 集合工具 ====================
    /** 转换集合 */
    public static <T, R> List<R> transform(Collection<T> collection, Function<? super T, ? extends R> mapper);
    /** 过滤并转换 */
    public static <T, R> List<R> filterAndTransform(Collection<T> collection,
                                                      Predicate<? super T> filter,
                                                      Function<? super T, ? extends R> mapper);
    /** 过滤 null */
    public static <T> List<T> filterNonNull(Collection<T> collection);

    // ==================== 函数链 ====================
    /** 二函数链 */
    public static <T, U, R> Function<T, R> chain(Function<? super T, ? extends U> first,
                                                   Function<? super U, ? extends R> second);
    /** 三函数链 */
    public static <T, U, V, R> Function<T, R> chain(Function<? super T, ? extends U> first,
                                                       Function<? super U, ? extends V> second,
                                                       Function<? super V, ? extends R> third);

    /** 顺序组合多个转换 */
    @SafeVarargs
    public static <T> UnaryOperator<T> sequence(UnaryOperator<T>... transformations);

    /** 应用函数 */
    public static <T, R> R apply(T value, Function<? super T, ? extends R> function);
}
```

**使用示例：**

```java
// Pipe 链式调用
String result = PipeUtil.pipe("  hello  ")
    .then(String::trim)
    .then(String::toUpperCase)
    .then(s -> s + "!")
    .get();
// "HELLO!"

// 条件转换
String cleaned = PipeUtil.when(input != null, input, String::trim);

// tap
String value = PipeUtil.tap(computeValue(), System.out::println);

// 集合转换
List<String> names = PipeUtil.transform(users, User::getName);

// 函数链
Function<String, Integer> parseLength = PipeUtil.chain(
    String::trim, String::length);
```

---

## 8. 光学类型

### 8.1 Lens<S, A>

函数式透镜，用于不可变数据结构的访问和修改。

```java
public final class Lens<S, A> {

    /** 创建透镜 */
    public static <S, A> Lens<S, A> of(Function<S, A> getter, BiFunction<S, A, S> setter);

    /** 创建恒等透镜 */
    public static <S> Lens<S, S> identity();

    /** 为 Record 创建透镜 */
    public static <S, A> Lens<S, A> forRecord(Function<S, A> getter, BiFunction<S, A, S> setter);

    /** 读取 */
    public A get(S source);

    /** 写入（返回新对象） */
    public S set(S source, A value);

    /** 修改（返回新对象） */
    public S modify(S source, UnaryOperator<A> modifier);

    /** 创建修改函数 */
    public UnaryOperator<S> modifier(UnaryOperator<A> modifier);

    /** 组合透镜（深入嵌套） */
    public <B> Lens<S, B> andThen(Lens<A, B> other);

    /** 反向组合 */
    public <T> Lens<T, A> compose(Lens<T, S> other);

    /** 转为 OptionalLens */
    public OptionalLens<S, A> asOptional();

    /** 获取 getter */
    public Function<S, A> getter();

    /** 获取 setter */
    public BiFunction<S, A, S> setter();
}
```

**使用示例：**

```java
// 定义透镜
Lens<Person, String> nameLens = Lens.of(
    Person::name,
    (p, name) -> new Person(name, p.age())
);

Lens<Person, Address> addressLens = Lens.of(
    Person::address,
    (p, addr) -> new Person(p.name(), p.age(), addr)
);

Lens<Address, String> cityLens = Lens.of(
    Address::city,
    (addr, city) -> new Address(city, addr.street())
);

// 读取
String name = nameLens.get(person);

// 写入（返回新 Person，原对象不变）
Person updated = nameLens.set(person, "Alice");

// 修改
Person upperCased = nameLens.modify(person, String::toUpperCase);

// 组合透镜（深入嵌套结构）
Lens<Person, String> personCityLens = addressLens.andThen(cityLens);
Person moved = personCityLens.set(person, "New York");
```

---

### 8.2 OptionalLens<S, A>

可选透镜，getter 返回 `Optional`，适用于可选字段。

```java
public final class OptionalLens<S, A> {

    /** 创建可选透镜 */
    public static <S, A> OptionalLens<S, A> of(Function<S, Optional<A>> getter,
                                                  BiFunction<S, A, S> setter);

    /** 从可能为 null 的 getter 创建 */
    public static <S, A> OptionalLens<S, A> ofNullable(Function<S, A> getter,
                                                         BiFunction<S, A, S> setter);

    /** 从普通 Lens 转换 */
    public static <S, A> OptionalLens<S, A> fromLens(Lens<S, A> lens);

    /** 读取 */
    public Optional<A> get(S source);

    /** 读取或默认值 */
    public A getOrElse(S source, A defaultValue);

    /** 是否存在值 */
    public boolean isPresent(S source);

    /** 写入 */
    public S set(S source, A value);

    /** 有值时写入 */
    public S setIfPresent(S source, Optional<A> value);

    /** 修改 */
    public S modify(S source, UnaryOperator<A> modifier);

    /** 修改或设置默认值 */
    public S modifyOrSet(S source, UnaryOperator<A> modifier, A defaultValue);

    /** 组合 */
    public <B> OptionalLens<S, B> andThen(OptionalLens<A, B> other);
    public <B> OptionalLens<S, B> andThen(Lens<A, B> other);
    public <T> OptionalLens<T, A> compose(OptionalLens<T, S> other);
    public <T> OptionalLens<T, A> compose(Lens<T, S> other);

    /** 获取 getter */
    public Function<S, Optional<A>> getter();

    /** 获取 setter */
    public BiFunction<S, A, S> setter();
}
```

**使用示例：**

```java
OptionalLens<User, Address> addressLens = OptionalLens.of(
    user -> Optional.ofNullable(user.address()),
    (user, addr) -> new User(user.name(), addr)
);

// 安全读取
Optional<Address> address = addressLens.get(user);

// 修改（若有值才修改）
User updated = addressLens.modify(user, Address::normalize);

// 组合
Lens<Address, String> cityLens = Lens.of(Address::city, Address::withCity);
OptionalLens<User, String> userCityLens = addressLens.andThen(cityLens);
```

---

## 9. 异步工具

### 9.1 Future<T>

CompletableFuture 的函数式包装，提供更优雅的 API。

```java
public final class Future<T> {

    // ==================== 工厂方法 ====================
    /** 在默认线程池异步执行 */
    public static <T> Future<T> of(Supplier<T> supplier);
    /** 在指定 Executor 异步执行 */
    public static <T> Future<T> of(Supplier<T> supplier, Executor executor);
    /** 在 Virtual Thread 异步执行 */
    public static <T> Future<T> ofVirtual(Supplier<T> supplier);
    /** 从 Callable 创建 */
    public static <T> Future<T> fromCallable(Callable<T> callable);
    /** 创建已完成的 Future */
    public static <T> Future<T> successful(T value);
    /** 创建已失败的 Future */
    public static <T> Future<T> failed(Throwable exception);
    /** 从 CompletableFuture 包装 */
    public static <T> Future<T> fromCompletableFuture(CompletableFuture<T> cf);
    /** 永不完成的 Future */
    public static <T> Future<T> never();

    // ==================== 转换方法 ====================
    public <R> Future<R> map(Function<? super T, ? extends R> mapper);
    public <R> Future<R> flatMap(Function<? super T, ? extends Future<R>> mapper);
    public Future<T> filter(Predicate<? super T> predicate);

    // ==================== 副作用 ====================
    public Future<T> onSuccess(Consumer<? super T> action);
    public Future<T> onFailure(Consumer<? super Throwable> action);
    public Future<T> onComplete(BiConsumer<? super T, ? super Throwable> action);

    // ==================== 恢复 ====================
    public Future<T> recover(Function<? super Throwable, ? extends T> recovery);
    public <E extends Throwable> Future<T> recover(Class<E> type, Function<? super E, ? extends T> recovery);
    public Future<T> recoverWith(Function<? super Throwable, ? extends Future<T>> recovery);
    public Future<T> orElse(T fallback);
    public Future<T> orElse(Future<T> fallback);

    // ==================== 超时 ====================
    public Future<T> timeout(Duration duration);
    public Future<T> timeout(Duration duration, T defaultValue);

    // ==================== 获取结果 ====================
    public Try<T> await();
    public Try<T> await(Duration duration);
    public T get();
    public T getOrElse(T defaultValue);

    // ==================== 状态查询 ====================
    public boolean isCompleted();
    public boolean isSuccess();
    public boolean isFailure();

    // ==================== 转换为其他类型 ====================
    public Option<T> toOption();
    public Try<T> toTry();
    public CompletableFuture<T> toCompletableFuture();

    // ==================== 组合 ====================
    /** 合并两个 Future */
    public static <A, B, R> Future<R> zip(Future<A> fa, Future<B> fb,
                                            BiFunction<? super A, ? super B, ? extends R> zipper);
    public <U, R> Future<R> zipWith(Future<U> other,
                                      BiFunction<? super T, ? super U, ? extends R> zipper);

    /** 等待所有 Future 完成 */
    @SafeVarargs
    public static <T> Future<List<T>> sequence(Future<T>... futures);
    public static <T> Future<List<T>> sequence(List<Future<T>> futures);

    /** 并行遍历 */
    public static <A, B> Future<List<B>> traverse(List<A> items,
                                                    Function<? super A, Future<B>> mapper);

    /** 竞速：返回最先完成的 */
    @SafeVarargs
    public static <T> Future<T> firstCompleted(Future<T>... futures);

    // ==================== Virtual Thread 方法 ====================
    public <R> Future<R> andThenVirtual(Function<? super T, ? extends R> action);
    public <R> Future<R> flatMapVirtual(Function<? super T, ? extends Future<R>> mapper);
}
```

**使用示例：**

```java
// 基本使用
Future<String> future = Future.of(() -> fetchData())
    .map(data -> process(data))
    .recover(ex -> "默认值");

// Virtual Thread
Future<String> vt = Future.ofVirtual(() -> compute());

// 超时
Future<String> withTimeout = future.timeout(Duration.ofSeconds(5));

// 组合
Future<String> combined = Future.zip(
    Future.of(() -> fetchName()),
    Future.of(() -> fetchAge()),
    (name, age) -> name + ": " + age
);

// 等待结果
Try<String> result = future.await();
Option<String> value = future.toOption();

// 批量等待
Future<List<User>> allUsers = Future.traverse(
    userIds, id -> Future.of(() -> loadUser(id)));
```

---

### 9.2 LazyAsync<T>

惰性异步计算，首次调用 `start()` 或 `force()` 时才开始计算，结果被缓存。

```java
public final class LazyAsync<T> {

    public enum State { NOT_STARTED, RUNNING, COMPLETED, FAILED }

    // ==================== 工厂方法 ====================
    /** 创建惰性异步计算 */
    public static <T> LazyAsync<T> of(Supplier<T> supplier);
    /** 创建已完成的 LazyAsync */
    public static <T> LazyAsync<T> completed(T value);
    /** 创建已失败的 LazyAsync */
    public static <T> LazyAsync<T> failed(Throwable throwable);

    // ==================== 启动与获取 ====================
    /** 启动计算（返回 CompletableFuture） */
    public CompletableFuture<T> start();
    /** 阻塞获取结果 */
    public T force();
    /** 带超时获取结果 */
    public Try<T> get(Duration timeout);
    /** 转为 Try */
    public Try<T> toTry();

    // ==================== 状态查询 ====================
    public boolean isStarted();
    public boolean isRunning();
    public boolean isCompleted();
    public boolean isFailed();
    public State state();

    // ==================== 转换 ====================
    public <U> LazyAsync<U> map(Function<? super T, ? extends U> mapper);
    public <U> LazyAsync<U> flatMap(Function<? super T, LazyAsync<U>> mapper);
    public LazyAsync<T> recover(Function<Throwable, T> recovery);
    public LazyAsync<T> orElse(LazyAsync<T> fallback);

    // ==================== 组合 ====================
    /** 合并两个 LazyAsync */
    public static <T1, T2, R> LazyAsync<R> combine(
        LazyAsync<T1> la1, LazyAsync<T2> la2,
        BiFunction<? super T1, ? super T2, ? extends R> combiner);

    /** 竞速 */
    public static <T> LazyAsync<T> race(LazyAsync<T> la1, LazyAsync<T> la2);

    /** 获取底层 Future */
    public CompletableFuture<T> getFuture();
}
```

**使用示例：**

```java
// 创建惰性异步计算
LazyAsync<Data> lazy = LazyAsync.of(() -> fetchDataFromNetwork());

// 尚未开始计算
System.out.println(lazy.state()); // NOT_STARTED

// 启动计算
CompletableFuture<Data> future = lazy.start();
Data data = future.join();

// 或直接阻塞获取
Data data2 = lazy.force();

// 带超时获取
Try<Data> result = lazy.get(Duration.ofSeconds(5));

// 链式转换
LazyAsync<String> processed = lazy
    .map(Data::getName)
    .map(String::toUpperCase);

// 组合
LazyAsync<Combined> combined = LazyAsync.combine(
    LazyAsync.of(() -> fetchA()),
    LazyAsync.of(() -> fetchB()),
    Combined::new
);
```

---

### 9.3 AsyncFunctionUtil

基于 Virtual Thread 的异步工具类。

```java
public final class AsyncFunctionUtil {

    /** 异步执行（Virtual Thread） */
    public static <T> CompletableFuture<T> async(Supplier<T> supplier);

    /** 异步执行无返回值操作 */
    public static CompletableFuture<Void> asyncRun(Runnable runnable);

    /** 带超时异步执行 */
    public static <T> Try<T> asyncWithTimeout(Supplier<T> supplier, Duration timeout);

    /** 并行映射 */
    public static <T, R> List<R> parallelMap(List<T> items, Function<T, R> mapper);

    /** 并行映射（返回 Try） */
    public static <T, R> Try<List<R>> parallelMapTry(List<T> items, Function<T, R> mapper);

    /** 并行执行多个 Supplier */
    @SafeVarargs
    public static <T> List<CompletableFuture<T>> runAll(Supplier<T>... suppliers);

    /** 并行执行多个 Runnable */
    public static List<CompletableFuture<Void>> runAllAsync(Runnable... runnables);

    /** 等待所有 Future 完成 */
    public static <T> Try<List<T>> awaitAll(List<CompletableFuture<T>> futures);

    /** 带超时等待所有 Future */
    public static <T> Try<List<T>> awaitAll(List<CompletableFuture<T>> futures, Duration timeout);

    /** 等待第一个完成的 Future */
    public static <T> CompletableFuture<T> awaitFirst(List<CompletableFuture<T>> futures);

    /** 异步 then */
    public static <T, R> CompletableFuture<R> thenAsync(CompletableFuture<T> future,
                                                          Function<T, R> mapper);

    /** 异步 flatMap */
    public static <T, R> CompletableFuture<R> thenFlatAsync(CompletableFuture<T> future,
                                                               Function<T, CompletableFuture<R>> mapper);

    /** 恢复 */
    public static <T> CompletableFuture<T> recover(CompletableFuture<T> future,
                                                      Function<Throwable, T> recovery);

    /** 异步恢复 */
    public static <T> CompletableFuture<T> recoverAsync(CompletableFuture<T> future,
                                                           Function<Throwable, CompletableFuture<T>> recovery);

    /** 延迟 */
    public static CompletableFuture<Void> delay(Duration duration);

    /** 创建已完成 Future */
    public static <T> CompletableFuture<T> completed(T value);

    /** 创建已失败 Future */
    public static <T> CompletableFuture<T> failed(Throwable throwable);

    /** 获取 Virtual Thread Executor */
    public static ExecutorService executor();
}
```

**使用示例：**

```java
// 异步执行
CompletableFuture<String> future = AsyncFunctionUtil.async(() -> fetchData());
String result = future.join();

// 带超时
Try<String> result = AsyncFunctionUtil.asyncWithTimeout(
    () -> slowOperation(), Duration.ofSeconds(5));

// 并行映射
List<User> users = List.of(id1, id2, id3);
List<Profile> profiles = AsyncFunctionUtil.parallelMap(
    users, id -> loadProfile(id));

// 批量执行
List<CompletableFuture<Result>> futures = AsyncFunctionUtil.runAll(
    () -> taskA(), () -> taskB(), () -> taskC());
Try<List<Result>> allResults = AsyncFunctionUtil.awaitAll(futures);
```

---

## 10. Record 工具

### 10.1 RecordUtil

Java Record 的反射工具类，提供组件访问、复制修改、Lens 创建、Map 转换等功能。

```java
public final class RecordUtil {

    // ==================== 类型查询 ====================
    /** 是否为 Record 类 */
    public static boolean isRecord(Class<?> clazz);
    /** 是否为 Record 实例 */
    public static boolean isRecordInstance(Object obj);

    // ==================== 组件访问 ====================
    /** 获取组件名称列表 */
    public static List<String> componentNames(Class<? extends Record> recordClass);
    /** 获取组件类型列表 */
    public static List<Class<?>> componentTypes(Class<? extends Record> recordClass);
    /** 获取组件数量 */
    public static int componentCount(Class<? extends Record> recordClass);
    /** 获取组件值列表 */
    public static List<Object> componentValues(Record record);
    /** 获取指定组件值 */
    public static <T> T getComponent(Record record, String componentName);
    /** 安全获取指定组件值 */
    public static <T> Try<T> getComponentTry(Record record, String componentName);
    /** 是否包含指定组件 */
    public static boolean hasComponent(Class<? extends Record> recordClass, String componentName);

    // ==================== 复制修改 ====================
    /** 复制 Record 并修改指定字段 */
    public static <R extends Record> R copy(R record, Map<String, Object> modifications);
    /** 复制并修改单个字段 */
    public static <R extends Record> R copyWith(R record, String componentName, Object newValue);
    /** 复制并转换单个字段 */
    public static <R extends Record, T> R copyTransforming(R record, String componentName,
                                                             Function<T, T> transformer);

    // ==================== Lens 创建 ====================
    /** 为 Record 组件创建 Lens */
    public static <R extends Record, T> Lens<R, T> lens(Class<R> recordClass, String componentName);
    /** 为所有组件创建 Lens Map */
    public static <R extends Record> Map<String, Lens<R, ?>> lenses(Class<R> recordClass);

    // ==================== Map 转换 ====================
    /** Record 转 Map */
    public static Map<String, Object> toMap(Record record);
    /** Map 转 Record */
    public static <R extends Record> R fromMap(Class<R> recordClass, Map<String, Object> map);
    /** 安全版 Record 转 Map */
    public static Try<Map<String, Object>> toMapTry(Record record);
    /** 安全版 Map 转 Record */
    public static <R extends Record> Try<R> fromMapTry(Class<R> recordClass, Map<String, Object> map);

    // ==================== 比较与工厂 ====================
    /** 比较两个 Record 的差异 */
    public static Map<String, Object[]> diff(Record r1, Record r2);
    /** 创建 Record 工厂函数 */
    public static <R extends Record> Function<Object[], R> factory(Class<R> recordClass);
    /** 创建双参 Record 工厂函数 */
    public static <R extends Record, A, B> BiFunction<A, B, R> factory2(Class<R> recordClass);
}
```

**使用示例：**

```java
record Person(String name, int age) {}

Person person = new Person("Alice", 30);

// 组件访问
List<String> names = RecordUtil.componentNames(Person.class); // ["name", "age"]
List<Object> values = RecordUtil.componentValues(person);       // ["Alice", 30]

// 复制修改
Person older = RecordUtil.copy(person, Map.of("age", 31));
Person renamed = RecordUtil.copyWith(person, "name", "Bob");

// Lens
Lens<Person, String> nameLens = RecordUtil.lens(Person.class, "name");
String name = nameLens.get(person);
Person updated = nameLens.set(person, "Charlie");

// Map 转换
Map<String, Object> map = RecordUtil.toMap(person);
Person fromMap = RecordUtil.fromMap(Person.class, map);

// 差异比较
Person p1 = new Person("Alice", 30);
Person p2 = new Person("Alice", 31);
Map<String, Object[]> diff = RecordUtil.diff(p1, p2);
// {"age": [30, 31]}
```

---

## 11. 异常体系

### 11.1 异常层次

```
OpenException (core)
└── OpenFunctionalException          # 函数式操作基础异常
    └── OpenMatchException           # 模式匹配失败异常
```

### 11.2 OpenFunctionalException

```java
public class OpenFunctionalException extends OpenException {

    public OpenFunctionalException(String message);
    public OpenFunctionalException(String message, Throwable cause);
    public OpenFunctionalException(String errorCode, String message);
    public OpenFunctionalException(String errorCode, String message, Throwable cause);

    // 工厂方法
    public static OpenFunctionalException computationFailed(String message);
    public static OpenFunctionalException computationFailed(String message, Throwable cause);
    public static OpenFunctionalException noValue(String message);
    public static OpenFunctionalException invalidState(String message);
    public static OpenFunctionalException mappingFailed(Throwable cause);
    public static OpenFunctionalException filterFailed(String message);
}
```

### 11.3 OpenMatchException

```java
public class OpenMatchException extends OpenFunctionalException {

    public OpenMatchException(String message);
    public OpenMatchException(String message, Object unmatchedValue);
    public OpenMatchException(String errorCode, String message, Object unmatchedValue);

    /** 获取未匹配的值 */
    public Object unmatchedValue();

    // 工厂方法
    public static OpenMatchException noMatch(Object value);
    public static OpenMatchException exhaustive(Object value, Class<?> sealedType);
    public static OpenMatchException guardFailed(Object value);
    public static OpenMatchException typeMismatch(Object value, Class<?> expectedType);
}
```

---

## 12. 使用场景

### 12.1 错误处理流水线

```java
Try<User> result = Try.of(() -> parseRequest(request))
    .flatMap(dto -> Try.of(() -> validateUser(dto)))
    .flatMap(validated -> Try.of(() -> saveUser(validated)))
    .onSuccess(user -> log.info("用户已创建: {}", user.getId()))
    .onFailure(ex -> log.error("创建用户失败", ex));
```

### 12.2 数据验证

```java
Validation<String, RegistrationForm> validated = Validation.combine(
    validateEmail(form.email()),
    validatePassword(form.password()),
    validateAge(form.age()),
    RegistrationForm::new
);

validated.fold(
    errors -> ResponseEntity.badRequest().body(errors),
    form -> ResponseEntity.ok(register(form))
);
```

### 12.3 不可变数据更新

```java
Lens<Company, String> ceoCityLens = Lens.of(Company::ceo, Company::withCeo)
    .andThen(Lens.of(Person::address, Person::withAddress))
    .andThen(Lens.of(Address::city, Address::withCity));

Company updated = ceoCityLens.set(company, "上海");
```

### 12.4 异步编排

```java
Future<OrderResult> result = Future.zip(
    Future.ofVirtual(() -> checkInventory(orderId)),
    Future.ofVirtual(() -> validatePayment(orderId)),
    Future.ofVirtual(() -> loadShippingInfo(orderId)),
    OrderResult::new
).recover(ex -> OrderResult.failed(ex.getMessage()));
```

### 12.5 惰性数据流

```java
List<String> topActiveUsers = Sequence.from(allUsers)
    .filter(User::isActive)
    .filter(u -> u.getLoginCount() > 100)
    .sorted(Comparator.comparing(User::getLoginCount).reversed())
    .take(10)
    .map(u -> u.getName() + " (" + u.getLoginCount() + " 次)")
    .toList();
```
