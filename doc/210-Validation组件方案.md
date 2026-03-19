# Validation 组件方案

## 1. 组件概述

`opencode-base-validation` 提供轻量级、高性能的数据校验框架，支持注解驱动的声明式校验和流式 API 编程式校验两种模式。内置 30+ 常用约束注解，支持校验分组、级联校验、异步校验、方法级校验。JDK 25 原生支持 Record 和 Pattern Matching。无第三方依赖。

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-validation</artifactId>
    <version>${version}</version>
</dependency>
```

## 2. 包结构

```
cloud.opencode.base.validation
├── OpenValidator.java               # 校验门面入口
├── Validator.java                   # 校验器核心接口
├── ValidationResult.java           # 校验结果（Record）
├── ConstraintViolation.java        # 约束违规（Record）
├── ValidationUtil.java              # 快捷校验工具
├── AssertUtil.java                  # 断言工具
├── ValidatorFactory.java           # 校验器工厂
├── ValidatorBuilder.java           # 校验器构建器
├── AsyncValidator.java             # 异步校验接口
├── MethodValidator.java            # 方法级校验接口
├── annotation/                      # 校验注解
│   ├── Valid.java                  # 级联校验
│   ├── Validated.java              # 启用校验
│   ├── Constraint.java            # 约束元注解
│   ├── Payload.java               # 负载接口
│   ├── constraint/                 # 约束注解
│   │   ├── NotNull, NotEmpty, NotBlank
│   │   ├── Size, Length, Min, Max, Range, Digits
│   │   ├── Pattern, Email, Phone, URL, IP
│   │   ├── IdCard, CreditCard, USCI
│   │   ├── Past, Future, PastOrPresent, FutureOrPresent
│   │   ├── Positive, Negative, PositiveOrZero, NegativeOrZero
│   │   ├── DateFormat, DateRange, FieldMatch, FieldCompare
│   │   ├── Script, ScriptAssert, ConditionalValidation
│   │   └── Custom
│   └── group/                      # 校验分组
│       ├── Default, Create, Update, Delete
├── constraint/                      # 约束处理器
│   ├── ConstraintValidator.java    # 约束校验器接口
│   ├── AsyncConstraintValidator.java # 异步约束校验器
│   ├── ConstraintValidatorContext.java # 校验上下文
│   └── ConstraintDescriptor.java   # 约束描述符
├── fluent/                          # 流式校验 API
│   ├── FluentValidator.java        # 流式校验入口
│   ├── FieldValidator.java         # 字段校验器
│   ├── ValidatorChain.java         # 校验链
│   ├── Rule.java                   # 校验规则接口
│   └── Rules.java                  # 内置规则集
├── expression/                      # 表达式校验
│   └── ExpressionValidator.java    # 表达式校验器
├── message/                         # 消息处理
│   ├── MessageResolver.java        # 消息解析器接口
│   ├── MessageInterpolator.java    # 消息插值器
│   ├── DefaultMessageResolver.java
│   └── ResourceBundleMessageResolver.java
├── metadata/                        # 元数据
│   ├── BeanDescriptor.java         # Bean 描述符
│   ├── PropertyDescriptor.java     # 属性描述符
│   └── MetadataCache.java          # 元数据缓存（单例）
├── spi/                             # SPI 扩展
│   ├── ValidatorProvider.java      # 校验器提供者
│   ├── MessageSourceProvider.java  # 消息源提供者
│   ├── ConstraintValidatorFactory.java # 校验器工厂
│   └── DefaultConstraintValidatorFactory.java # 默认工厂
└── exception/                       # 异常体系
    ├── ValidationException.java     # 校验异常基类
    ├── ConstraintViolationException.java  # 约束违规异常
    ├── ConstraintDeclarationException.java # 约束声明异常
    ├── ConstraintDefinitionException.java  # 约束定义异常
    ├── GroupDefinitionException.java       # 分组定义异常
    ├── UnexpectedTypeException.java        # 类型不匹配异常
    └── ValidationTimeoutException.java     # 校验超时异常
```

## 3. 核心 API

### 3.1 OpenValidator

> 校验操作的统一门面入口，提供对象校验、属性校验、快速失败、抛异常校验、错误消息提取等静态方法。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static ValidationResult validate(Object object, Class<?>... groups)` | 校验对象 |
| `static ValidationResult validateFailFast(Object object, Class<?>... groups)` | 快速失败模式校验 |
| `static void validateAndThrow(Object object, Class<?>... groups)` | 校验失败则抛异常 |
| `static <T> T validateAndGet(T object, Class<?>... groups)` | 校验通过则返回对象 |
| `static ValidationResult validateProperty(Object object, String propertyName, Class<?>... groups)` | 校验指定属性 |
| `static <T> ValidationResult validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups)` | 校验指定值 |
| `static boolean isValid(Object object, Class<?>... groups)` | 是否校验通过 |
| `static List<String> getErrorMessages(Object object, Class<?>... groups)` | 获取错误消息列表 |
| `static Map<String, List<String>> getErrorMap(Object object, Class<?>... groups)` | 获取错误消息 Map |
| `static <T> BeanDescriptor getConstraintsForClass(Class<T> clazz)` | 获取约束元数据 |
| `static <T> FluentValidator<T> fluent(T object)` | 创建流式校验器 |
| `static Validator getValidator()` | 获取默认校验器 |

**示例：**

```java
// 基本校验
ValidationResult result = OpenValidator.validate(user);
if (result.hasErrors()) {
    System.out.println(result.toMessage(", "));
}

// 快速失败
ValidationResult fast = OpenValidator.validateFailFast(user);

// 校验失败抛异常
OpenValidator.validateAndThrow(user, Create.class);

// 流式校验
ValidationResult fluent = OpenValidator.fluent(user)
    .field("name", User::getName).notBlank().length(2, 50).and()
    .field("age", User::getAge).satisfies(a -> a > 0, "年龄必须为正数").and()
    .validate();
```

### 3.2 Validator

> 校验器核心接口，定义对象校验、属性校验、值校验、快速失败校验等契约。实现类应线程安全且可复用。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `<T> ValidationResult validate(T object, Class<?>... groups)` | 校验对象 |
| `<T> ValidationResult validateProperty(T object, String propertyName, Class<?>... groups)` | 校验属性 |
| `<T> ValidationResult validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups)` | 校验值 |
| `<T> BeanDescriptor getConstraintsForClass(Class<T> clazz)` | 获取约束元数据 |
| `<T> ValidationResult validateFailFast(T object, Class<?>... groups)` | 快速失败校验 |
| `default <T> void validateAndThrow(T object, Class<?>... groups)` | 校验失败抛异常 |
| `default <T> T validateAndGet(T object, Class<?>... groups)` | 校验通过返回对象 |

### 3.3 ValidationResult

> 校验结果 Record，包含是否通过和约束违规列表。提供便捷的查询、过滤和转换方法。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static ValidationResult success()` | 创建成功结果 |
| `static ValidationResult invalid(List<ConstraintViolation> violations)` | 创建失败结果 |
| `static ValidationResult invalid(ConstraintViolation violation)` | 创建单个失败结果 |
| `boolean isValid()` | 是否通过 |
| `boolean hasErrors()` | 是否有错误 |
| `Optional<ConstraintViolation> getFirstViolation()` | 获取第一个违规 |
| `List<ConstraintViolation> getViolationsFor(String propertyName)` | 获取指定属性的违规 |
| `Map<String, List<String>> toMap()` | 转为属性->错误消息 Map |
| `String toMessage(String delimiter)` | 拼接为错误消息字符串 |
| `String toDetailedMessage(String delimiter)` | 拼接为详细错误消息 |
| `int violationCount()` | 违规数量 |
| `List<ConstraintViolation> getViolations()` | 获取所有违规 |
| `static Builder builder()` | 创建 Builder |

### 3.4 ConstraintViolation

> 约束违规 Record，表示单个校验失败，包含消息、属性路径、无效值、约束注解名。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String message()` | 错误消息 |
| `String propertyPath()` | 属性路径（如 "user.name"） |
| `Object invalidValue()` | 无效值 |
| `String constraintName()` | 约束注解名称 |
| `static ConstraintViolation of(String message, String propertyPath, Object invalidValue)` | 创建违规 |
| `static ConstraintViolation of(String message, String propertyPath, Object invalidValue, String constraintName)` | 创建违规（含注解名） |

### 3.5 ValidationUtil

> 静态校验工具类，提供常用格式校验方法（邮箱、手机号、身份证、IP、URL 等）。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static boolean isEmail(String email)` | 是否为有效邮箱 |
| `static boolean isPhone(String phone)` | 是否为有效手机号 |
| `static boolean isChinaPhone(String phone)` | 是否为中国大陆手机号 |
| `static boolean isHKPhone(String phone)` | 是否为香港手机号 |
| `static boolean isTWPhone(String phone)` | 是否为台湾手机号 |
| `static boolean isUSPhone(String phone)` | 是否为美国手机号 |
| `static boolean isInternationalPhone(String phone)` | 是否为国际手机号 |
| `static boolean isIdCard(String idCard)` | 是否为有效身份证号（18位） |
| `static boolean isIdCard15(String idCard)` | 是否为有效身份证号（15位） |
| `static boolean isUrl(String url)` | 是否为有效 URL |
| `static boolean isIp(String ip)` | 是否为有效 IP |
| `static boolean isIpv4(String ip)` | 是否为有效 IPv4 |
| `static boolean isIpv6(String ip)` | 是否为有效 IPv6 |
| `static boolean isCreditCard(String cardNumber)` | 是否为有效信用卡号（Luhn） |
| `static boolean isUSCI(String usci)` | 是否为统一社会信用代码 |
| `static boolean isChinaPostalCode(String postalCode)` | 是否为中国邮政编码 |
| `static boolean isAlpha(String str)` | 是否全为字母 |
| `static boolean isAlphanumeric(String str)` | 是否为字母+数字 |
| `static boolean isNumeric(String str)` | 是否全为数字 |
| `static boolean matches(String str, String regex)` | 是否匹配正则 |

**示例：**

```java
boolean validEmail = ValidationUtil.isEmail("user@example.com");
boolean validPhone = ValidationUtil.isChinaPhone("13800138000");
boolean validId = ValidationUtil.isIdCard("110101199001011234");
boolean validCard = ValidationUtil.isCreditCard("4111111111111111");
```

### 3.6 AssertUtil

> 断言工具类，参数校验失败时抛出 IllegalArgumentException 或 IllegalStateException。`require*` 系列方法返回校验通过的值。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static void notNull(Object object, String message)` | 断言非 null |
| `static void notNull(Object object, Supplier<String> messageSupplier)` | 断言非 null（延迟消息） |
| `static void isNull(Object object, String message)` | 断言为 null |
| `static void notEmpty(String str, String message)` | 断言字符串非空 |
| `static void notBlank(String str, String message)` | 断言字符串非空白 |
| `static void hasLength(String str, int length, String message)` | 断言字符串达到指定长度 |
| `static void lengthBetween(String str, int min, int max, String message)` | 断言字符串长度范围 |
| `static void notEmpty(Collection<?> collection, String message)` | 断言集合非空 |
| `static void notEmpty(Map<?, ?> map, String message)` | 断言 Map 非空 |
| `static void notEmpty(Object[] array, String message)` | 断言数组非空 |
| `static void noNullElements(Collection<?> collection, String message)` | 断言集合无 null 元素 |
| `static void isTrue(boolean expression, String message)` | 断言为 true |
| `static void isFalse(boolean expression, String message)` | 断言为 false |
| `static void state(boolean expression, String message)` | 断言状态（抛 IllegalStateException） |
| `static void positive(Number number, String message)` | 断言为正数 |
| `static void notNegative(Number number, String message)` | 断言非负数 |
| `static void range(long value, long min, long max, String message)` | 断言范围 |
| `static void range(double value, double min, double max, String message)` | 断言浮点范围 |
| `static <T> T requireNotNull(T object, String message)` | 非 null 校验并返回 |
| `static String requireNotBlank(String str, String message)` | 非空白校验并返回 |
| `static String requireNotEmpty(String str, String message)` | 非空校验并返回 |
| `static <T extends Collection<?>> T requireNotEmpty(T collection, String message)` | 集合非空校验并返回 |
| `static <T extends Number> T requirePositive(T number, String message)` | 正数校验并返回 |

**示例：**

```java
AssertUtil.notNull(config, "Config must not be null");
AssertUtil.notBlank(name, "Name must not be blank");
String validated = AssertUtil.requireNotBlank(name, "Name required");
AssertUtil.range(age, 0, 150, "Age out of range");
```

### 3.7 ValidatorFactory

> 校验器工厂，创建和缓存 Validator 实例。默认校验器全局单例复用。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static Validator getDefault()` | 获取默认校验器（单例） |
| `static Builder builder()` | 创建 Builder |
| `static Validator createValidator(ValidatorConfiguration configuration)` | 创建自定义校验器 |
| `Builder.failFast(boolean failFast)` | 设置快速失败 |
| `Builder.messageResolver(MessageResolver resolver)` | 设置消息解析器 |
| `Builder.constraintValidatorFactory(ConstraintValidatorFactory factory)` | 设置校验器工厂 |
| `Builder.defaultLocale(Locale locale)` | 设置默认语言 |
| `Builder.build()` | 构建校验器 |

### 3.8 ValidatorBuilder

> 流式构建器，创建配置好的 Validator、AsyncValidator 或 MethodValidator 实例。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static ValidatorBuilder create()` | 创建构建器 |
| `ValidatorBuilder failFast(boolean failFast)` | 设置快速失败 |
| `ValidatorBuilder locale(Locale locale)` | 设置语言 |
| `ValidatorBuilder messageInterpolator(MessageInterpolator interpolator)` | 设置消息插值器 |
| `ValidatorBuilder executor(Executor executor)` | 设置异步执行器 |
| `<A extends Annotation, T> ValidatorBuilder addConstraintValidator(Class<A> annotation, ConstraintValidator<A, T> validator)` | 添加自定义约束校验器 |
| `ValidatorBuilder ignoreAnnotation(Class<? extends Annotation> annotationType)` | 忽略注解 |
| `Validator build()` | 构建 Validator |
| `AsyncValidator buildAsync()` | 构建 AsyncValidator |
| `MethodValidator buildMethodValidator()` | 构建 MethodValidator |

### 3.9 AsyncValidator

> 异步校验接口，使用虚拟线程执行校验操作，返回 CompletableFuture。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static AsyncValidator create()` | 创建默认异步校验器 |
| `static AsyncValidator create(Executor executor)` | 使用指定执行器创建 |
| `<T> CompletableFuture<ValidationResult> validateAsync(T object, Class<?>... groups)` | 异步校验 |
| `<T> CompletableFuture<ValidationResult> validatePropertyAsync(T object, String propertyName, Class<?>... groups)` | 异步属性校验 |
| `<T> CompletableFuture<ValidationResult> validateValueAsync(Class<T> beanType, String propertyName, Object value, Class<?>... groups)` | 异步值校验 |
| `<T> CompletableFuture<ValidationResult> validateFailFastAsync(T object, Class<?>... groups)` | 异步快速失败校验 |
| `Validator getValidator()` | 获取内部校验器 |
| `Executor getExecutor()` | 获取执行器 |
| `AsyncValidator withExecutor(Executor newExecutor)` | 更换执行器 |

### 3.10 MethodValidator

> 方法级校验接口，校验方法参数和返回值。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static MethodValidator create()` | 创建方法校验器 |
| `<T> ValidationResult validateParameters(T object, Method method, Object[] params, Class<?>... groups)` | 校验方法参数 |
| `<T> ValidationResult validateReturnValue(T object, Method method, Object returnValue, Class<?>... groups)` | 校验返回值 |
| `<T> ValidationResult validateConstructorParameters(Constructor<? extends T> constructor, Object[] params, Class<?>... groups)` | 校验构造器参数 |
| `<T> ValidationResult validateConstructorReturnValue(Constructor<? extends T> constructor, T createdObject, Class<?>... groups)` | 校验构造器返回值 |

### 3.11 FluentValidator

> 流式校验器，提供链式 API 对对象进行编程式校验。支持快速失败和条件校验。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T> FluentValidator<T> of(T target)` | 创建流式校验器 |
| `FluentValidator<T> failFast()` | 启用快速失败 |
| `<V> FieldValidator<T, V> field(String name, Function<T, V> getter)` | 指定要校验的字段 |
| `FluentValidator<T> when(boolean condition, Consumer<FluentValidator<T>> validations)` | 条件校验 |
| `FluentValidator<T> satisfies(Predicate<T> predicate, String message)` | 对象级校验 |
| `ValidationResult validate()` | 执行校验 |
| `T validateAndGet()` | 校验通过返回对象 |

**示例：**

```java
ValidationResult result = FluentValidator.of(user)
    .failFast()
    .field("name", User::getName).notBlank().length(2, 50).and()
    .field("email", User::getEmail).notBlank().email().and()
    .field("age", User::getAge).notNull().range(1, 200, "年龄不合法").and()
    .when(user.getRole() == Role.ADMIN, v ->
        v.field("adminCode", User::getAdminCode).notBlank("管理员编码必填").and())
    .validate();
```

### 3.12 FieldValidator

> 字段校验器，在流式 API 中对单个字段应用校验规则。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `FieldValidator<T, V> notNull(String message)` | 非 null 校验 |
| `FieldValidator<T, V> notNull()` | 非 null 校验（默认消息） |
| `FieldValidator<T, V> notBlank(String message)` | 非空白校验 |
| `FieldValidator<T, V> notBlank()` | 非空白校验（默认消息） |
| `FieldValidator<T, V> length(int min, int max, String message)` | 长度校验 |
| `FieldValidator<T, V> length(int min, int max)` | 长度校验（默认消息） |
| `FieldValidator<T, V> range(long min, long max, String message)` | 范围校验 |
| `FieldValidator<T, V> matches(String pattern, String message)` | 正则校验 |
| `FieldValidator<T, V> email(String message)` | 邮箱校验 |
| `FieldValidator<T, V> email()` | 邮箱校验（默认消息） |
| `FieldValidator<T, V> satisfies(Predicate<V> predicate, String message)` | 自定义校验 |
| `FieldValidator<T, V> apply(Rule<? super V> rule)` | 应用校验规则 |
| `FluentValidator<T> and()` | 返回父校验器 |

### 3.13 ValidatorChain

> 校验链，对单个值进行链式校验。支持快速失败、映射转换。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T> ValidatorChain<T> of(T value)` | 创建校验链 |
| `static <T> ValidatorChain<T> of(T value, String propertyName)` | 创建校验链（指定属性名） |
| `ValidatorChain<T> failFast()` | 快速失败 |
| `ValidatorChain<T> notNull(String message)` | 非 null |
| `ValidatorChain<T> notBlank(String message)` | 非空白 |
| `ValidatorChain<T> matches(String pattern, String message)` | 正则匹配 |
| `ValidatorChain<T> length(int min, int max, String message)` | 长度 |
| `ValidatorChain<T> satisfies(Predicate<T> predicate, String message)` | 自定义 |
| `ValidatorChain<T> apply(Rule<T> rule)` | 应用规则 |
| `<R> ValidatorChain<R> map(Function<T, R> mapper)` | 映射转换 |
| `ValidationResult validate()` | 执行校验 |
| `boolean isValid()` | 是否通过 |
| `T get()` | 获取值 |

**示例：**

```java
ValidationResult result = ValidatorChain.of(email, "email")
    .notBlank("邮箱不能为空")
    .matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", "邮箱格式不正确")
    .validate();
```

### 3.14 Rule

> 校验规则接口，定义可复用的校验逻辑。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `boolean test(T value)` | 测试值是否通过 |
| `String message()` | 错误消息 |
| `static <T> Rule<T> of(Predicate<T> predicate, String message)` | 创建规则 |

### 3.15 Rules

> 内置校验规则集，提供预定义的常用规则常量。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static final Rule<String> ALPHA` | 纯字母规则 |
| `static final Rule<String> ALPHA_NUMERIC` | 字母+数字规则 |
| `static final Rule<String> NUMERIC` | 纯数字规则 |
| `static final Rule<String> LOWERCASE` | 全小写规则 |
| `static final Rule<String> UPPERCASE` | 全大写规则 |
| `static final Rule<String> CHINA_MOBILE` | 中国手机号规则 |
| `static final Rule<String> CHINA_ID_CARD` | 中国身份证号规则 |
| `static final Rule<String> CHINA_POSTAL_CODE` | 中国邮政编码规则 |
| `static final Rule<Number> POSITIVE` | 正数规则 |
| `static final Rule<Number> NEGATIVE` | 负数规则 |
| `static final Rule<Number> NOT_ZERO` | 非零规则 |
| `static <T> Rule<Collection<T>> notEmpty()` | 集合非空规则 |
| `static <T> Rule<Collection<T>> maxSize(int max)` | 集合最大长度规则 |
| `static <T> Rule<Collection<T>> minSize(int min)` | 集合最小长度规则 |
| `static final Rule<LocalDate> PAST_DATE` | 过去日期规则 |
| `static final Rule<LocalDate> FUTURE_DATE` | 未来日期规则 |
| `static final Rule<LocalDate> TODAY_OR_PAST` | 今天或过去规则 |
| `static final Rule<LocalDate> TODAY_OR_FUTURE` | 今天或未来规则 |

### 3.16 ExpressionValidator

> 基于表达式的校验器，支持可选的 Expression 模块委托。提供表达式规则创建。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static boolean isExpressionModuleAvailable()` | Expression 模块是否可用 |
| `static boolean validate(String expression, Map<String, Object> variables)` | 表达式校验 |
| `static boolean validateObject(Object rootObject, String expression)` | 对象表达式校验 |
| `static boolean isValidExpression(String expression)` | 表达式是否有效 |
| `static Rule<Map<String, Object>> expressionRule(String expression, String message)` | 创建表达式规则 |
| `static <T> Rule<T> objectRule(String expression, String message)` | 创建对象表达式规则 |

### 3.17 约束注解

> 内置约束注解，涵盖空值校验、字符串校验、数字校验、日期校验、格式校验、跨字段校验等。

| 注解 | 描述 |
|------|------|
| `@NotNull` | 值不能为 null |
| `@NotEmpty` | 字符串/集合/数组不能为空 |
| `@NotBlank` | 字符串不能为空白 |
| `@Size(min, max)` | 大小/长度范围 |
| `@Length(min, max)` | 字符串长度范围 |
| `@Min(value)` | 最小值 |
| `@Max(value)` | 最大值 |
| `@Range(min, max)` | 数值范围 |
| `@Digits(integer, fraction)` | 数字位数 |
| `@Pattern(regexp)` | 正则匹配 |
| `@Email` | 邮箱格式 |
| `@Phone` | 手机号格式 |
| `@URL` | URL 格式 |
| `@IP` | IP 地址格式 |
| `@IdCard` | 身份证号格式 |
| `@CreditCard` | 信用卡号格式（Luhn） |
| `@USCI` | 统一社会信用代码 |
| `@Past` | 过去时间 |
| `@Future` | 未来时间 |
| `@PastOrPresent` | 过去或当前时间 |
| `@FutureOrPresent` | 未来或当前时间 |
| `@Positive` | 正数 |
| `@Negative` | 负数 |
| `@PositiveOrZero` | 正数或零 |
| `@NegativeOrZero` | 负数或零 |
| `@DateFormat(pattern)` | 日期格式匹配 |
| `@DateRange(start, end)` | 日期范围（跨字段） |
| `@FieldMatch(first, second)` | 两字段值相等 |
| `@FieldCompare(first, second, operator)` | 两字段值比较（LT/LE/EQ/NE/GE/GT） |
| `@Script(lang, value)` | 脚本校验 |
| `@ScriptAssert(lang, script)` | 类级脚本校验 |
| `@ConditionalValidation(condition)` | 条件校验 |
| `@Custom(validatedBy)` | 自定义校验 |

**示例：**

```java
public record UserForm(
    @NotBlank(message = "名称不能为空")
    String name,

    @Email(message = "邮箱格式不正确")
    String email,

    @Range(min = 1, max = 150, message = "年龄不合法")
    int age,

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    String phone
) {}

ValidationResult result = OpenValidator.validate(new UserForm("张三", "test@example.com", 25, "13800138000"));
```

### 3.18 校验分组

> 校验分组接口，用于按操作类型执行不同的校验规则。

| 分组 | 描述 |
|------|------|
| `Default` | 默认分组 |
| `Create` | 创建操作分组 |
| `Update` | 更新操作分组 |
| `Delete` | 删除操作分组 |

**示例：**

```java
public record User(
    @NotNull(groups = Update.class, message = "更新时ID必填")
    Long id,

    @NotBlank(groups = {Create.class, Update.class})
    String name
) {}

// 按分组校验
OpenValidator.validate(user, Create.class);
OpenValidator.validate(user, Update.class);
```

### 3.19 ConstraintValidator

> 约束校验器接口，实现自定义约束的校验逻辑。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `default void initialize(A constraintAnnotation)` | 初始化（读取注解参数） |
| `boolean isValid(T value, ConstraintValidatorContext context)` | 校验逻辑 |

### 3.20 消息处理

> 消息解析和插值，支持 ResourceBundle 国际化。

| 类 | 描述 |
|------|------|
| `MessageResolver` | 消息解析器接口 |
| `DefaultMessageResolver` | 默认消息解析器（简单插值） |
| `ResourceBundleMessageResolver` | ResourceBundle 消息解析器 |
| `MessageInterpolator` | 消息插值器（模板参数替换） |

### 3.21 元数据

> 约束元数据缓存，提升反射校验性能。

| 类 | 描述 |
|------|------|
| `BeanDescriptor` | Bean 约束描述符接口 |
| `PropertyDescriptor` | 属性约束描述符接口 |
| `MetadataCache` | 全局元数据缓存（单例，ConcurrentHashMap） |

### 3.22 异常体系

> 校验异常层次结构，均继承自 `ValidationException`（继承 RuntimeException）。

| 异常类 | 描述 |
|------|------|
| `ValidationException` | 校验异常基类，可包含 ValidationResult |
| `ConstraintViolationException` | 约束违规异常，包含违规集合 |
| `ConstraintDeclarationException` | 约束注解使用错误 |
| `ConstraintDefinitionException` | 自定义约束定义错误 |
| `GroupDefinitionException` | 校验分组定义错误 |
| `UnexpectedTypeException` | 值类型与校验器不匹配 |
| `ValidationTimeoutException` | 异步校验超时 |

**示例：**

```java
try {
    OpenValidator.validateAndThrow(user);
} catch (ValidationException e) {
    e.getResult().ifPresent(result ->
        result.getViolations().forEach(v ->
            log.warn("{}: {}", v.propertyPath(), v.message())));
    Map<String, List<String>> errorMap = e.toErrorMap();
}
```
