
package cloud.opencode.base.serialization.binary;

import cloud.opencode.base.serialization.OpenSerializer;
import cloud.opencode.base.serialization.Serializer;
import cloud.opencode.base.serialization.TypeReference;
import cloud.opencode.base.serialization.exception.OpenSerializationException;
import cloud.opencode.base.serialization.filter.ClassFilter;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * JdkSerializer - JDK Native Serialization
 * JDK 原生序列化器
 *
 * <p>Uses Java's built-in ObjectInputStream/ObjectOutputStream for serialization.
 * Objects must implement Serializable interface.</p>
 * <p>使用 Java 内置的 ObjectInputStream/ObjectOutputStream 进行序列化。
 * 对象必须实现 Serializable 接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>No external dependencies - 无外部依赖</li>
 *   <li>Full Java type support - 完整的 Java 类型支持</li>
 *   <li>Preserves object graph - 保持对象图</li>
 * </ul>
 *
 * <p><strong>Limitations | 限制:</strong></p>
 * <ul>
 *   <li>Objects must implement Serializable - 对象必须实现 Serializable</li>
 *   <li>Larger output size than binary formats - 输出大小比二进制格式大</li>
 *   <li>Slower than Kryo or Protobuf - 比 Kryo 或 Protobuf 慢</li>
 *   <li>Security concerns with deserialization - 反序列化的安全问题</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * JdkSerializer serializer = new JdkSerializer();
 *
 * // Serialize
 * byte[] data = serializer.serialize(user);
 *
 * // Deserialize
 * User restored = serializer.deserialize(data, User.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = object graph size - O(n), n为对象图大小</li>
 *   <li>Space complexity: O(n) for serialized form - 序列化形式 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
public class JdkSerializer implements Serializer {

    private static final System.Logger LOGGER = System.getLogger(JdkSerializer.class.getName());

    /**
     * Format name
     * 格式名称
     */
    public static final String FORMAT = "jdk";

    /**
     * Default denied classes for deserialization security
     * 反序列化安全的默认拒绝类
     */
    private static final Set<String> DENIED_CLASSES = Set.of(
            // Java core dangerous classes
            "java.lang.Runtime",
            "java.lang.ProcessBuilder",
            "java.lang.reflect.Proxy",
            "javax.script.ScriptEngine",
            "javax.script.ScriptEngineManager",
            // RMI and JNDI attacks
            "java.rmi.registry.Registry",
            "java.rmi.server.RemoteObject",
            "java.rmi.server.UnicastRemoteObject",
            "javax.naming.InitialContext",
            "javax.naming.spi.NamingManager",
            "com.sun.jndi.rmi.registry.RegistryContext",
            // Commons Collections gadgets
            "org.apache.commons.collections.functors.InvokerTransformer",
            "org.apache.commons.collections.functors.InstantiateTransformer",
            "org.apache.commons.collections.functors.ChainedTransformer",
            "org.apache.commons.collections.functors.ConstantTransformer",
            "org.apache.commons.collections.keyvalue.TiedMapEntry",
            "org.apache.commons.collections4.functors.InvokerTransformer",
            "org.apache.commons.collections4.functors.InstantiateTransformer",
            "org.apache.commons.collections4.functors.ChainedTransformer",
            "org.apache.commons.collections4.functors.ConstantTransformer",
            "org.apache.commons.collections4.keyvalue.TiedMapEntry",
            // Commons BeanUtils gadgets
            "org.apache.commons.beanutils.BeanComparator",
            // XSLT template injection
            "org.apache.xalan.xsltc.trax.TemplatesImpl",
            "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl",
            "org.apache.xalan.xsltc.trax.TrAXFilter",
            // Spring gadgets
            "org.springframework.beans.factory.config.PropertyPathFactoryBean",
            "org.springframework.aop.framework.JdkDynamicAopProxy",
            "org.springframework.beans.factory.ObjectFactory",
            "org.springframework.core.SerializableTypeWrapper",
            "org.springframework.transaction.jta.JtaTransactionManager",
            // C3P0 gadgets
            "com.mchange.v2.c3p0.WrapperConnectionPoolDataSource",
            "com.mchange.v2.c3p0.JndiRefForwardingDataSource",
            "com.mchange.v2.c3p0.PoolBackedDataSource",
            // JDBC gadgets
            "com.sun.rowset.JdbcRowSetImpl",
            // Groovy gadgets
            "org.codehaus.groovy.runtime.ConvertedClosure",
            "org.codehaus.groovy.runtime.MethodClosure",
            "groovy.util.Expando",
            // BCEL gadgets
            "org.apache.bcel.util.ClassLoader",
            // JBoss gadgets
            "org.jboss.interceptor.proxy.InterceptorMethodHandler",
            // Hibernate gadgets
            "org.hibernate.tuple.component.AbstractComponentTuplizer",
            // Wicket gadgets
            "org.apache.wicket.util.io.DeferredFileOutputStream",
            // Myfaces gadgets
            "org.apache.myfaces.context.servlet.FacesContextImpl",
            // Mozilla Rhino gadgets
            "org.mozilla.javascript.NativeJavaObject"
    );

    /**
     * Maximum depth for object deserialization
     * 对象反序列化的最大深度
     */
    private static final long MAX_DEPTH = 100;

    /**
     * Maximum array size for deserialization
     * 反序列化的最大数组大小
     */
    private static final long MAX_ARRAY_SIZE = 10_000_000;

    /**
     * Maximum total number of object references for deserialization (prevents DoS via many small objects)
     * 反序列化的最大对象引用总数（防止通过大量小对象进行 DoS）
     */
    private static final long MAX_REFERENCES = 1_000_000;

    /**
     * Maximum allowed byte array size for direct deserialization (64 MB).
     * 直接反序列化时允许的最大字节数组大小（64 MB）。
     */
    private static final int MAX_BINARY_INPUT_SIZE = 64 * 1024 * 1024;

    /**
     * Reusable ObjectInputFilter instance — avoids lambda allocation on every deserialize() call.
     * 可重用的 ObjectInputFilter 实例 — 避免每次 deserialize() 调用时分配 lambda。
     *
     * <p>Reads {@link OpenSerializer#getConfig()} on each invocation (volatile read)
     * to respect runtime configuration changes.</p>
     */
    private static final ObjectInputFilter DESERIALIZATION_FILTER = filterInfo -> {
        // Check depth | 检查深度
        if (filterInfo.depth() > MAX_DEPTH) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Deserialization rejected: depth {0} exceeds maximum {1}",
                    filterInfo.depth(), MAX_DEPTH);
            return ObjectInputFilter.Status.REJECTED;
        }
        // Check array size | 检查数组大小
        if (filterInfo.arrayLength() > MAX_ARRAY_SIZE) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Deserialization rejected: array length {0} exceeds maximum {1}",
                    filterInfo.arrayLength(), MAX_ARRAY_SIZE);
            return ObjectInputFilter.Status.REJECTED;
        }
        // Check total object references to prevent DoS via many small objects
        // 检查总对象引用数以防止通过大量小对象进行 DoS
        if (filterInfo.references() > MAX_REFERENCES) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Deserialization rejected: reference count {0} exceeds maximum {1}",
                    filterInfo.references(), MAX_REFERENCES);
            return ObjectInputFilter.Status.REJECTED;
        }
        // Check class | 检查类
        Class<?> clazz = filterInfo.serialClass();
        if (clazz != null) {
            String className = clazz.getName();
            // Strip JVM array descriptor to get the component class name
            // e.g. "[Ljavax.naming.Reference;" -> "javax.naming.Reference"
            // 剥离 JVM 数组描述符以获取组件类名
            String effectiveClassName = stripArrayDescriptor(className);

            // Check global ClassFilter from SerializerConfig (if configured)
            // 检查 SerializerConfig 中的全局 ClassFilter（如已配置）
            ClassFilter globalFilter = OpenSerializer.getConfig().getClassFilter();
            if (globalFilter != null && !globalFilter.isAllowed(effectiveClassName)) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Deserialization rejected by global ClassFilter: {0}", className);
                return ObjectInputFilter.Status.REJECTED;
            }
            // Reject known dangerous classes | 拒绝已知危险类
            if (DENIED_CLASSES.contains(effectiveClassName)) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Deserialization rejected: denied class {0}", className);
                return ObjectInputFilter.Status.REJECTED;
            }
            // Reject dangerous package prefixes | 拒绝危险包前缀
            if (effectiveClassName.startsWith("javax.naming.") ||
                effectiveClassName.startsWith("java.rmi.") ||
                effectiveClassName.startsWith("sun.rmi.") ||
                effectiveClassName.startsWith("com.sun.org.apache.xalan") ||
                effectiveClassName.startsWith("sun.")) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Deserialization rejected: dangerous package prefix in class {0}", className);
                return ObjectInputFilter.Status.REJECTED;
            }
            // Reject classes with dangerous patterns | 拒绝具有危险模式的类
            if (isDangerousClassName(effectiveClassName)) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Deserialization rejected: dangerous class name pattern in {0}", className);
                return ObjectInputFilter.Status.REJECTED;
            }
        }
        // Explicitly ALLOWED — defense-in-depth: do not rely on UNDECIDED semantics
        // 显式 ALLOWED — 纵深防御：不依赖 UNDECIDED 语义
        return ObjectInputFilter.Status.ALLOWED;
    };

    // ==================== Serializer Implementation | 序列化器实现 ====================

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return new byte[0];
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw OpenSerializationException.serializeFailed(obj, FORMAT, e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> type) {
        if (data == null || data.length == 0) {
            return null;
        }
        if (data.length > MAX_BINARY_INPUT_SIZE) {
            throw new OpenSerializationException(
                    "Input data size " + data.length + " bytes exceeds maximum allowed " + MAX_BINARY_INPUT_SIZE + " bytes");
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            // Set deserialization filter for security (static instance, zero allocation)
            ois.setObjectInputFilter(DESERIALIZATION_FILTER);
            Object obj = ois.readObject();
            return type.cast(obj);
        } catch (IOException | ClassNotFoundException e) {
            throw OpenSerializationException.deserializeFailed(data, type, FORMAT, e);
        }
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

    // ==================== Streaming Overrides | 流式覆盖 ====================

    /**
     * Writes directly to the output stream, avoiding the intermediate byte[] allocation.
     * 直接写入输出流，避免中间 byte[] 分配。
     */
    @Override
    public void serialize(Object obj, OutputStream out) {
        java.util.Objects.requireNonNull(out, "OutputStream must not be null");
        if (obj == null) {
            return;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(obj);
            oos.flush();
        } catch (IOException e) {
            throw OpenSerializationException.serializeFailed(obj, FORMAT, e);
        }
    }

    /**
     * Reads directly from the input stream, avoiding the readLimited -> byte[] -> ByteArrayInputStream double-copy.
     * 直接从输入流读取，避免 readLimited -> byte[] -> ByteArrayInputStream 的双拷贝。
     */
    @Override
    public <T> T deserialize(InputStream in, Class<T> type) {
        java.util.Objects.requireNonNull(in, "InputStream must not be null");
        java.util.Objects.requireNonNull(type, "Type must not be null");

        try (ObjectInputStream ois = new ObjectInputStream(in)) {
            ois.setObjectInputFilter(DESERIALIZATION_FILTER);
            Object obj = ois.readObject();
            return type.cast(obj);
        } catch (IOException | ClassNotFoundException e) {
            throw OpenSerializationException.deserializeFailed(null, type, FORMAT, e);
        }
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public String getMimeType() {
        return "application/x-java-serialized-object";
    }

    @Override
    public boolean supports(Class<?> type) {
        return Serializable.class.isAssignableFrom(type);
    }

    // ==================== Internal Helpers | 内部辅助 ====================

    /**
     * Strips JVM array descriptor prefix from a class name to get the component type.
     * 剥离 JVM 数组描述符前缀以获取组件类型名。
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code "[Ljavax.naming.Reference;"} → {@code "javax.naming.Reference"}</li>
     *   <li>{@code "[[Ljava.lang.String;"} → {@code "java.lang.String"}</li>
     *   <li>{@code "[I"} → {@code "[I"} (primitive arrays are kept as-is)</li>
     *   <li>{@code "java.lang.String"} → {@code "java.lang.String"} (no change)</li>
     * </ul>
     */
    static String stripArrayDescriptor(String className) {
        int i = 0;
        while (i < className.length() && className.charAt(i) == '[') {
            i++;
        }
        if (i > 0 && i < className.length() && className.charAt(i) == 'L' && className.endsWith(";")) {
            return className.substring(i + 1, className.length() - 1);
        }
        return className;
    }

    /**
     * Checks if a class name matches dangerous patterns.
     * 检查类名是否匹配危险模式。
     */
    private static boolean isDangerousClassName(String className) {
        // Commons collections transformers
        if (className.contains("Transformer") &&
            (className.contains("Invoke") || className.contains("Instantiate") ||
             className.contains("Chained") || className.contains("Constant"))) {
            return true;
        }
        // XSLT template injection
        if (className.contains("TemplatesImpl") || className.contains("TrAXFilter")) {
            return true;
        }
        // JNDI injection patterns
        if (className.contains("JndiRef") || className.contains("JndiLookup")) {
            return true;
        }
        // RMI attacks
        if (className.startsWith("java.rmi.") &&
            (className.contains("Registry") || className.contains("Remote"))) {
            return true;
        }
        // Groovy RCE
        if (className.contains("groovy") &&
            (className.contains("Closure") || className.contains("Expando"))) {
            return true;
        }
        // BCEL class loading
        if (className.contains("bcel") && className.contains("ClassLoader")) {
            return true;
        }
        // Spring JTA transaction manager (JNDI lookup)
        if (className.contains("JtaTransactionManager")) {
            return true;
        }
        // RowSet JDBC attacks
        if (className.contains("RowSet") && className.contains("Impl")) {
            return true;
        }
        return false;
    }
}
