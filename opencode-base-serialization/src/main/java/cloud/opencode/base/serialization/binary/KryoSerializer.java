
package cloud.opencode.base.serialization.binary;

import cloud.opencode.base.serialization.Serializer;
import cloud.opencode.base.serialization.TypeReference;
import cloud.opencode.base.serialization.exception.OpenSerializationException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * KryoSerializer - High Performance Kryo Serialization
 * Kryo 高性能序列化器
 *
 * <p>Uses Kryo library for fast binary serialization with pool management for thread safety.</p>
 * <p>使用 Kryo 库进行快速二进制序列化，通过池管理确保线程安全。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>High performance serialization - 高性能序列化</li>
 *   <li>Compact binary format - 紧凑的二进制格式</li>
 *   <li>Thread-safe via Kryo Pool - 通过 Kryo 池实现线程安全</li>
 *   <li>Optional class registration - 可选的类注册</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>10x faster than JDK serialization - 比 JDK 序列化快 10 倍</li>
 *   <li>5x smaller output than JDK - 输出比 JDK 小 5 倍</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * KryoSerializer kryo = new KryoSerializer();
 *
 * // Optional: Register classes for better performance
 * kryo.register(User.class, Order.class);
 *
 * // Serialize
 * byte[] data = kryo.serialize(user);
 *
 * // Deserialize
 * User restored = kryo.deserialize(data, User.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (via pool) - 线程安全: 是 (通过池)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
public class KryoSerializer implements Serializer {

    /**
     * Format name
     * 格式名称
     */
    public static final String FORMAT = "kryo";

    /**
     * Default pool size
     * 默认池大小
     */
    private static final int DEFAULT_POOL_SIZE = 16;

    /**
     * Dangerous classes that should never be deserialized
     * 永远不应反序列化的危险类
     */
    private static final Set<String> DENIED_CLASSES = Set.of(
            "java.lang.Runtime",
            "java.lang.ProcessBuilder",
            "java.lang.reflect.Proxy",
            "javax.script.ScriptEngine",
            "javax.script.ScriptEngineManager",
            "org.apache.commons.collections.functors.InvokerTransformer",
            "org.apache.commons.collections.functors.InstantiateTransformer",
            "org.apache.commons.collections.functors.ChainedTransformer",
            "org.apache.commons.collections4.functors.InvokerTransformer",
            "org.apache.commons.collections4.functors.InstantiateTransformer",
            "org.apache.commons.collections4.functors.ChainedTransformer",
            "org.apache.xalan.xsltc.trax.TemplatesImpl",
            "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl",
            "org.springframework.beans.factory.config.PropertyPathFactoryBean",
            "org.springframework.aop.framework.JdkDynamicAopProxy",
            "com.mchange.v2.c3p0.WrapperConnectionPoolDataSource",
            "com.mchange.v2.c3p0.JndiRefForwardingDataSource",
            "com.sun.rowset.JdbcRowSetImpl",
            "java.rmi.registry.Registry",
            "java.rmi.server.RemoteObject",
            "javax.naming.InitialContext",
            "org.apache.bcel.util.ClassLoader",
            "org.codehaus.groovy.runtime.ConvertedClosure",
            "org.codehaus.groovy.runtime.MethodClosure"
    );

    /**
     * Kryo instance pool
     * Kryo 实例池
     */
    private final Pool<Kryo> kryoPool;

    /**
     * Whether to use secure mode (registration required)
     * 是否使用安全模式（需要注册）
     */
    private final boolean secureMode;

    /**
     * Allowed classes for deserialization (whitelist)
     * 反序列化允许的类（白名单）
     */
    private final Set<Class<?>> allowedClasses = new CopyOnWriteArraySet<>();

    /**
     * Registered classes for serialization performance
     * 注册用于序列化性能的类
     */
    private final Set<Class<?>> registeredClasses = new CopyOnWriteArraySet<>();

    /**
     * Registered classes with specific IDs
     * 带特定 ID 的注册类
     */
    private final java.util.concurrent.ConcurrentHashMap<Class<?>, Integer> registeredClassIds = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Creates a new KryoSerializer with default configuration (non-secure mode).
     * 创建带默认配置的新 KryoSerializer（非安全模式）。
     */
    public KryoSerializer() {
        this(DEFAULT_POOL_SIZE, false);
    }

    /**
     * Creates a new KryoSerializer with custom pool size.
     * 创建带自定义池大小的新 KryoSerializer。
     *
     * @param poolSize the pool size - 池大小
     */
    public KryoSerializer(int poolSize) {
        this(poolSize, false);
    }

    /**
     * Creates a new KryoSerializer with secure mode option.
     * 创建带安全模式选项的新 KryoSerializer。
     *
     * @param poolSize   the pool size - 池大小
     * @param secureMode if true, only registered classes can be deserialized - 如果为true，只有注册的类可以反序列化
     */
    public KryoSerializer(int poolSize, boolean secureMode) {
        this.secureMode = secureMode;
        this.kryoPool = new Pool<>(true, false, poolSize) {
            @Override
            protected Kryo create() {
                return createKryo();
            }
        };
    }

    /**
     * Creates a secure KryoSerializer that requires class registration.
     * 创建需要类注册的安全 KryoSerializer。
     *
     * @return a secure KryoSerializer instance - 安全的 KryoSerializer 实例
     */
    public static KryoSerializer secure() {
        return new KryoSerializer(DEFAULT_POOL_SIZE, true);
    }

    /**
     * Creates and configures a new Kryo instance.
     * 创建并配置新的 Kryo 实例。
     */
    protected Kryo createKryo() {
        Kryo kryo = new Kryo() {
            @Override
            public com.esotericsoftware.kryo.Registration getRegistration(Class type) {
                // Security check BEFORE Kryo constructs the object
                validateClassSecurity(type);
                return super.getRegistration(type);
            }
        };
        kryo.setRegistrationRequired(secureMode);
        kryo.setReferences(true);
        // Register allowed classes in secure mode
        if (secureMode) {
            for (Class<?> clazz : allowedClasses) {
                kryo.register(clazz);
            }
        }
        // Apply stored registrations
        for (Class<?> clazz : registeredClasses) {
            Integer id = registeredClassIds.get(clazz);
            if (id != null) {
                kryo.register(clazz, id);
            } else {
                kryo.register(clazz);
            }
        }
        return kryo;
    }

    // ==================== Serializer Implementation | 序列化器实现 ====================

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return new byte[0];
        }

        Kryo kryo = kryoPool.obtain();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Output output = new Output(bos)) {
            kryo.writeClassAndObject(output, obj);
            output.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            throw OpenSerializationException.serializeFailed(obj, FORMAT, e);
        } finally {
            kryoPool.free(kryo);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> type) {
        if (data == null || data.length == 0) {
            return null;
        }

        // Security check: validate target type is not in denied list
        validateClassSecurity(type);

        Kryo kryo = kryoPool.obtain();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             Input input = new Input(bis)) {
            Object obj = kryo.readClassAndObject(input);
            // Security check: validate deserialized object type
            if (obj != null) {
                validateClassSecurity(obj.getClass());
            }
            return type.cast(obj);
        } catch (Exception e) {
            throw OpenSerializationException.deserializeFailed(data, type, FORMAT, e);
        } finally {
            kryoPool.free(kryo);
        }
    }

    /**
     * Validates that a class is safe for deserialization.
     * 验证类对于反序列化是安全的。
     *
     * @param clazz the class to validate - 要验证的类
     * @throws OpenSerializationException if the class is not allowed - 如果类不被允许
     */
    private void validateClassSecurity(Class<?> clazz) {
        String className = clazz.getName();
        // Check against denied classes
        if (DENIED_CLASSES.contains(className)) {
            throw new OpenSerializationException(
                    "Deserialization of class is not allowed for security reasons: " + className);
        }
        // Check for dangerous class name patterns
        if (isDangerousClassName(className)) {
            throw new OpenSerializationException(
                    "Deserialization of class is not allowed for security reasons: " + className);
        }
    }

    /**
     * Checks if a class name matches dangerous patterns.
     * 检查类名是否匹配危险模式。
     */
    private boolean isDangerousClassName(String className) {
        // Commons collections transformers
        if (className.contains("Transformer") &&
            (className.contains("Invoke") || className.contains("Instantiate") || className.contains("Chained"))) {
            return true;
        }
        // XSLT template injection
        if (className.contains("TemplatesImpl")) {
            return true;
        }
        // JNDI injection
        if (className.contains("JndiRef") || className.contains("InitialContext")) {
            return true;
        }
        // RMI attacks
        if (className.startsWith("java.rmi.") &&
            (className.contains("Registry") || className.contains("Remote"))) {
            return true;
        }
        // Groovy RCE
        if (className.contains("groovy") && className.contains("Closure")) {
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, TypeReference<T> typeRef) {
        return (T) deserialize(data, typeRef.getRawType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, Type type) {
        if (type instanceof Class<?> clazz) {
            return (T) deserialize(data, clazz);
        }
        throw OpenSerializationException.unsupportedType(type, FORMAT);
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public String getMimeType() {
        return "application/x-kryo";
    }

    // ==================== Registration Methods | 注册方法 ====================

    /**
     * Registers classes for better serialization performance.
     * 注册类以获得更好的序列化性能。
     *
     * <p>Registration is optional but improves performance and reduces output size.</p>
     * <p>注册是可选的，但可以提高性能并减少输出大小。</p>
     *
     * @param classes the classes to register - 要注册的类
     * @return this serializer - 此序列化器
     */
    public KryoSerializer register(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            registeredClasses.add(clazz);
        }
        return this;
    }

    /**
     * Registers a class with a specific ID.
     * 注册带特定 ID 的类。
     *
     * @param clazz the class to register - 要注册的类
     * @param id    the registration ID - 注册 ID
     * @return this serializer - 此序列化器
     */
    public KryoSerializer register(Class<?> clazz, int id) {
        registeredClasses.add(clazz);
        registeredClassIds.put(clazz, id);
        return this;
    }

    /**
     * Adds classes to the allowed whitelist (for secure mode).
     * 将类添加到允许的白名单（用于安全模式）。
     *
     * @param classes the classes to allow - 要允许的类
     * @return this serializer - 此序列化器
     */
    public KryoSerializer allow(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            allowedClasses.add(clazz);
        }
        return this;
    }

    /**
     * Returns whether this serializer is in secure mode.
     * 返回此序列化器是否处于安全模式。
     *
     * @return true if secure mode is enabled - 如果启用了安全模式则返回 true
     */
    public boolean isSecureMode() {
        return secureMode;
    }
}
