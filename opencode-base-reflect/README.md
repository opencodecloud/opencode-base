# OpenCode Base Reflect

**Comprehensive reflection utilities for Java 25+**

`opencode-base-reflect` provides a rich set of reflection utilities covering classes, fields, methods, constructors, annotations, proxies, beans, records, sealed classes, lambda introspection, type resolution, and classpath scanning. It uses modern Java features including records, sealed classes, and VarHandle/MethodHandle access.

## Features

### Core Reflection
- **Class Operations**: Load classes, check types (primitive, wrapper, record, sealed), navigate hierarchy
- **Field Operations**: Read/write fields (including private), list all fields with inheritance
- **Method Operations**: Find and invoke methods (instance and static), list all methods
- **Constructor Operations**: Find constructors, create instances with or without arguments
- **Annotation Operations**: Get, find (Optional), and check annotations on any element
- **Modifier Checks**: Public, private, static, final, abstract, synchronized checks

### Bean Utilities
- **BeanCopier**: Copy properties between beans with type conversion
- **BeanMap**: Map-like view of bean properties
- **BeanPath**: Nested property access via dot-separated paths
- **BeanUtil**: General bean manipulation utilities
- **BeanDiff**: Detect differences between two beans
- **PropertyDescriptor**: Describes a bean property (name, type, getter, setter)
- **PropertyConverter**: Convert property values between types

### Proxy & Invocation
- **ProxyFactory**: Create JDK dynamic proxies with fluent API
- **MethodInterceptor**: Intercept method calls with before/after/around semantics
- **AbstractInvocationHandler**: Base class for custom invocation handlers
- **Invokable**: Unified abstraction for methods and constructors

### Lambda Introspection
- **LambdaUtil**: Inspect lambda expressions and extract method references
- **LambdaInfo**: Metadata about a lambda (implementing method, captured args)
- **SerializableFunction/Consumer/Predicate/Supplier/BiFunction**: Serializable functional interfaces for lambda introspection

### Record Support
- **RecordUtil**: Utilities for Java records (components, construction, conversion)
- **RecordBuilder**: Build record instances dynamically
- **RecordComponent**: Describes a record component

### Sealed Class Support
- **SealedUtil**: Utilities for sealed classes and interfaces
- **PermittedSubclasses**: Enumerate and inspect permitted subclasses

### Type System
- **TypeToken**: Captures full generic type information at runtime
- **TypeLiteral**: Literal type representation
- **TypeResolver**: Resolves generic types against declaring classes
- **TypeUtil**: Utilities for working with java.lang.reflect.Type
- **TypeVariableUtil**: Resolves type variables in class hierarchies
- **ParameterizedTypeImpl/GenericArrayTypeImpl/WildcardTypeImpl**: Custom Type implementations

### Classpath Scanning
- **ClassScanner**: Scan packages for classes matching criteria
- **AnnotationScanner**: Find classes annotated with specific annotations
- **ResourceScanner**: Scan classpath for resources
- **ClassPath**: Represents the full classpath with scanning capabilities
- **ClassInfo/ResourceInfo**: Metadata about discovered classes and resources

### Accessor Layer
- **FieldAccessor**: Direct field access (reflection-based)
- **MethodHandleAccessor**: High-performance access via MethodHandle
- **VarHandleAccessor**: Highest-performance access via VarHandle
- **PropertyAccessor**: Unified property access interface
- **PropertyAccessors**: Factory for creating optimal accessor instances
- **BeanAccessor**: High-level bean property accessor

### Performance
- **ReflectCache**: Thread-safe cache for reflection metadata (fields, methods, constructors)

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-reflect</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Reflection
```java
import cloud.opencode.base.reflect.OpenReflect;

// Load a class
Class<?> clazz = OpenReflect.forName("com.example.User");

// Read/write fields
Object name = OpenReflect.readField(user, "name");
OpenReflect.writeField(user, "name", "Alice");

// Invoke methods
Object result = OpenReflect.invokeMethod(user, "getName");

// Create instances
User user = OpenReflect.newInstance(User.class);
User user2 = OpenReflect.newInstance(User.class, "Alice", 25);

// Check annotations
boolean hasAnno = OpenReflect.hasAnnotation(method, Deprecated.class);
```

### Bean Operations
```java
import cloud.opencode.base.reflect.bean.*;

// Copy properties between beans
BeanCopier.copy(source, target);

// Access nested properties
Object city = BeanPath.get(user, "address.city");

// View bean as Map
Map<String, Object> map = BeanMap.of(user);

// Compare beans
List<BeanDiff.Diff> diffs = BeanDiff.diff(user1, user2);
```

### Dynamic Proxy
```java
import cloud.opencode.base.reflect.proxy.*;

MyService proxy = ProxyFactory.create(MyService.class, (obj, method, args) -> {
    System.out.println("Before: " + method.getName());
    Object result = method.invoke(realService, args);
    System.out.println("After: " + method.getName());
    return result;
});
```

### Record Utilities
```java
import cloud.opencode.base.reflect.record.*;

// Build a record dynamically
Person person = RecordBuilder.of(Person.class)
    .set("name", "Alice")
    .set("age", 25)
    .build();

// Get record components
List<RecordComponent> components = RecordUtil.getComponents(Person.class);
```

### Type Tokens
```java
import cloud.opencode.base.reflect.type.*;

TypeToken<List<String>> token = new TypeToken<List<String>>() {};
Type type = token.getType(); // java.util.List<java.lang.String>
```

## Class Reference

### Root Package (`cloud.opencode.base.reflect`)
| Class | Description |
|-------|-------------|
| `OpenReflect` | Main facade for all reflection operations |
| `OpenClass` | Class loading, type checks, and hierarchy navigation |
| `OpenField` | Field access and manipulation |
| `OpenMethod` | Method discovery and invocation |
| `OpenConstructor` | Constructor discovery and instance creation |
| `OpenAnnotation` | Annotation discovery and inspection |
| `OpenModifier` | Modifier flag checking utilities |
| `ClassUtil` | Additional class utility methods |
| `FieldUtil` | Additional field utility methods |
| `MethodUtil` | Additional method utility methods |
| `ConstructorUtil` | Additional constructor utility methods |
| `AnnotationUtil` | Additional annotation utility methods |
| `ModifierUtil` | Additional modifier utility methods |
| `ReflectUtil` | General reflection helper methods |
| `ReflectCache` | Thread-safe cache for reflection metadata |

### Accessor (`cloud.opencode.base.reflect.accessor`)
| Class | Description |
|-------|-------------|
| `PropertyAccessor` | Unified property access interface |
| `PropertyAccessors` | Factory for creating optimal accessor instances |
| `FieldAccessor` | Direct field-based property access |
| `MethodHandleAccessor` | MethodHandle-based high-performance access |
| `VarHandleAccessor` | VarHandle-based highest-performance access |
| `BeanAccessor` | High-level bean property accessor |

### Bean (`cloud.opencode.base.reflect.bean`)
| Class | Description |
|-------|-------------|
| `OpenBean` | Bean operation facade |
| `BeanCopier` | Copy properties between bean instances |
| `BeanMap` | Map-like view of bean properties |
| `BeanPath` | Nested property access via dot-separated paths |
| `BeanUtil` | General bean manipulation utilities |
| `BeanDiff` | Detect and report differences between two beans |
| `PropertyDescriptor` | Describes a bean property |
| `PropertyConverter` | Converts property values between types |

### Invokable (`cloud.opencode.base.reflect.invokable`)
| Class | Description |
|-------|-------------|
| `Invokable<T,R>` | Unified abstraction for methods and constructors |
| `MethodInvokable<T,R>` | Invokable wrapper for methods |
| `ConstructorInvokable<T>` | Invokable wrapper for constructors |
| `Parameter` | Describes a method/constructor parameter |

### Lambda (`cloud.opencode.base.reflect.lambda`)
| Class | Description |
|-------|-------------|
| `OpenLambda` | Lambda introspection facade |
| `LambdaUtil` | Lambda inspection and method reference extraction |
| `LambdaInfo` | Metadata about a lambda expression |
| `FunctionalInterfaceUtil` | Utilities for functional interfaces |
| `SerializableFunction<T,R>` | Serializable Function for lambda introspection |
| `SerializableBiFunction<T,U,R>` | Serializable BiFunction |
| `SerializableConsumer<T>` | Serializable Consumer |
| `SerializablePredicate<T>` | Serializable Predicate |
| `SerializableSupplier<T>` | Serializable Supplier |
| `SerializedLambdaWrapper` | Wrapper for SerializedLambda objects |

### Proxy (`cloud.opencode.base.reflect.proxy`)
| Class | Description |
|-------|-------------|
| `OpenProxy` | Proxy creation facade |
| `ProxyFactory` | Factory for creating JDK dynamic proxies |
| `ProxyUtil` | Proxy utility methods |
| `MethodInterceptor` | Method call interceptor interface |
| `MethodInvoker` | Method invocation helper |
| `AbstractInvocationHandler` | Base class for invocation handlers |

### Record (`cloud.opencode.base.reflect.record`)
| Class | Description |
|-------|-------------|
| `OpenRecord` | Record operations facade |
| `RecordBuilder<T>` | Dynamic builder for record instances |
| `RecordComponent` | Describes a record component |
| `RecordUtil` | Record utility methods |

### Sealed (`cloud.opencode.base.reflect.sealed`)
| Class | Description |
|-------|-------------|
| `OpenSealed` | Sealed class operations facade |
| `SealedUtil` | Sealed class utility methods |
| `PermittedSubclasses` | Enumerate permitted subclasses of a sealed type |

### Scan (`cloud.opencode.base.reflect.scan`)
| Class | Description |
|-------|-------------|
| `ClassScanner` | Scan packages for classes matching criteria |
| `AnnotationScanner` | Find classes with specific annotations |
| `ResourceScanner` | Scan classpath for resources |
| `ClassPath` | Full classpath representation with scanning |
| `ClassInfo` | Metadata about a discovered class |
| `ResourceInfo` | Metadata about a discovered resource |

### Type (`cloud.opencode.base.reflect.type`)
| Class | Description |
|-------|-------------|
| `TypeToken<T>` | Captures generic type information at runtime |
| `TypeLiteral<T>` | Literal type representation |
| `TypeResolver` | Resolves generic types against declaring classes |
| `TypeUtil` | Utilities for java.lang.reflect.Type |
| `TypeVariableUtil` | Resolves type variables in hierarchies |
| `ParameterizedTypeImpl` | Custom ParameterizedType implementation |
| `GenericArrayTypeImpl` | Custom GenericArrayType implementation |
| `WildcardTypeImpl` | Custom WildcardType implementation |

### Exception (`cloud.opencode.base.reflect.exception`)
| Class | Description |
|-------|-------------|
| `OpenReflectException` | Runtime exception for reflection errors |

## Requirements

- Java 25+
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
