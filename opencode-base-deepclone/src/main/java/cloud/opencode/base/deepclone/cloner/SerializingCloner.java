package cloud.opencode.base.deepclone.cloner;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.exception.OpenDeepCloneException;

import java.io.*;
import java.util.Set;

/**
 * Serialization-based deep cloner
 * 基于序列化的深度克隆器
 *
 * <p>Uses Java serialization to create deep copies. Objects must implement
 * Serializable interface. Simple but slower than reflection-based cloning.</p>
 * <p>使用Java序列化创建深度副本。对象必须实现Serializable接口。
 * 简单但比基于反射的克隆慢。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Complete object graph copy - 完整对象图复制</li>
 *   <li>Handles circular references automatically - 自动处理循环引用</li>
 *   <li>Simple implementation - 实现简单</li>
 * </ul>
 *
 * <p><strong>Limitations | 限制:</strong></p>
 * <ul>
 *   <li>Objects must implement Serializable - 对象必须实现Serializable</li>
 *   <li>Slower than reflection/Unsafe cloning - 比反射/Unsafe克隆慢</li>
 *   <li>Transient fields are not cloned - transient字段不会被克隆</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SerializingCloner cloner = SerializingCloner.create();
 * User cloned = cloner.clone(originalUser); // User must be Serializable
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public final class SerializingCloner extends AbstractCloner {

    /**
     * Rejected classes for deserialization to prevent gadget chain attacks.
     * 反序列化时拒绝的类，防止反序列化利用链攻击。
     */
    private static final Set<String> REJECTED_CLASSES = Set.of(
            // JDK dangerous classes
            "java.lang.Runtime",
            "java.lang.ProcessBuilder",
            "java.lang.Process",
            "java.lang.Thread",
            "java.net.URLClassLoader",
            "javax.script.ScriptEngineManager",
            // JNDI injection
            "javax.naming.InitialContext",
            "javax.naming.spi.NamingManager",
            "com.sun.jndi.rmi.registry.RegistryContext",
            "com.sun.jndi.ldap.LdapCtx",
            // RMI
            "java.rmi.server.UnicastRemoteObject",
            "java.rmi.server.RemoteObjectInvocationHandler",
            // JMX
            "javax.management.BadAttributeValueExpException",
            // JDBC / DataSource
            "com.sun.rowset.JdbcRowSetImpl",
            "com.mchange.v2.c3p0.WrapperConnectionPoolDataSource",
            // Apache Commons
            "org.apache.commons.collections.functors.InvokerTransformer",
            "org.apache.commons.collections.functors.InstantiateTransformer",
            "org.apache.commons.collections4.functors.InvokerTransformer",
            "org.apache.commons.collections4.functors.InstantiateTransformer",
            "org.apache.commons.beanutils.BeanComparator",
            // Xalan / XSLT
            "org.apache.xalan.xsltc.trax.TemplatesImpl",
            "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl",
            // Spring
            "org.springframework.beans.factory.ObjectFactory",
            // Groovy
            "org.codehaus.groovy.runtime.ConvertedClosure",
            "org.codehaus.groovy.runtime.MethodClosure"
    );

    /**
     * Maximum allowed deserialization depth.
     * 最大允许反序列化深度。
     */
    private static final int MAX_DEPTH = 100;

    /**
     * Maximum number of references allowed during deserialization.
     * 反序列化时允许的最大引用数。
     */
    private static final long MAX_REFERENCES = 10_000L;

    /**
     * ObjectInputFilter that rejects dangerous classes.
     * 拒绝危险类的 ObjectInputFilter。
     */
    private static final ObjectInputFilter DESERIALIZATION_FILTER = filterInfo -> {
        if (filterInfo.depth() > MAX_DEPTH) {
            return ObjectInputFilter.Status.REJECTED;
        }
        if (filterInfo.references() > MAX_REFERENCES) {
            return ObjectInputFilter.Status.REJECTED;
        }
        Class<?> clazz = filterInfo.serialClass();
        if (clazz != null) {
            String className = clazz.getName();
            if (REJECTED_CLASSES.contains(className)) {
                return ObjectInputFilter.Status.REJECTED;
            }
            // Reject known dangerous package prefixes
            if (className.startsWith("org.apache.commons.collections.functors.")
                    || className.startsWith("org.apache.commons.collections4.functors.")
                    || className.startsWith("javassist.")
                    || className.startsWith("org.codehaus.groovy.runtime.")
                    || className.startsWith("org.mozilla.javascript.")
                    || className.startsWith("org.apache.bcel.")
                    || className.startsWith("com.sun.jndi.")) {
                return ObjectInputFilter.Status.REJECTED;
            }
        }
        return ObjectInputFilter.Status.UNDECIDED;
    };

    private SerializingCloner() {
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a SerializingCloner
     * 创建SerializingCloner
     *
     * @return the cloner | 克隆器
     */
    public static SerializingCloner create() {
        return new SerializingCloner();
    }

    // ==================== Clone Implementation | 克隆实现 ====================

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doClone(T original, CloneContext context) {
        if (!(original instanceof Serializable)) {
            throw OpenDeepCloneException.unsupportedType(original.getClass());
        }

        try {
            // Serialize
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(original);
            }

            // Deserialize with deserialization filter to reject dangerous classes
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                ois.setObjectInputFilter(DESERIALIZATION_FILTER);
                T cloned = (T) ois.readObject();
                context.registerCloned(original, cloned);
                return cloned;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw OpenDeepCloneException.serializationFailed(original.getClass(), e);
        }
    }

    @Override
    public String getStrategyName() {
        return "serializing";
    }

    @Override
    public boolean supports(Class<?> type) {
        return Serializable.class.isAssignableFrom(type);
    }
}
