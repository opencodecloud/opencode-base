package cloud.opencode.base.event.serialization;

import cloud.opencode.base.event.Event;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

/**
 * Event Serializer with Optional Serialization Module Delegation
 * 支持可选序列化模块委托的事件序列化器
 *
 * <p>Serializes and deserializes events for transmission or storage.
 * If the Serialization module (opencode-base-serialization) is available, it delegates to OpenSerializer
 * for better performance and format support. Otherwise, falls back to Java serialization.</p>
 * <p>序列化和反序列化事件以进行传输或存储。
 * 如果序列化模块可用，则委托给 OpenSerializer 以获得更好的性能和格式支持。
 * 否则降级到 Java 序列化。</p>
 *
 * <p><strong>Supported Formats (with Serialization module) | 支持的格式（使用序列化模块时）:</strong></p>
 * <ul>
 *   <li>JSON - for human-readable format | 人类可读格式</li>
 *   <li>MessagePack - for compact binary format | 紧凑二进制格式</li>
 *   <li>Java Serialization - fallback | 回退方案</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if serialization module is available
 * if (EventSerializer.isSerializationModuleAvailable()) {
 *     // Serialize event
 *     byte[] data = EventSerializer.serialize(event);
 *
 *     // Deserialize event
 *     MyEvent restored = EventSerializer.deserialize(data, MyEvent.class);
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Event serialization and deserialization - 事件序列化和反序列化</li>
 *   <li>Secure deserialization with class filtering - 带类过滤的安全反序列化</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n is the serialized event size - 时间复杂度: O(n)，n 为序列化事件大小</li>
 *   <li>Space complexity: O(n) for serialization buffers - 空间复杂度: O(n) 序列化缓冲区</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public final class EventSerializer {

    private static final MethodHandle SERIALIZE_HANDLE;
    private static final MethodHandle DESERIALIZE_HANDLE;

    static {
        SERIALIZE_HANDLE = initSerializeHandle();
        DESERIALIZE_HANDLE = initDeserializeHandle();
    }

    private EventSerializer() {
    }

    private static MethodHandle initSerializeHandle() {
        try {
            Class<?> openSerializerClass = Class.forName("cloud.opencode.base.serialization.OpenSerializer");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findStatic(openSerializerClass, "serialize",
                    MethodType.methodType(byte[].class, Object.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    private static MethodHandle initDeserializeHandle() {
        try {
            Class<?> openSerializerClass = Class.forName("cloud.opencode.base.serialization.OpenSerializer");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findStatic(openSerializerClass, "deserialize",
                    MethodType.methodType(Object.class, byte[].class, Class.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Checks if the Serialization module is available.
     * 检查序列化模块是否可用
     *
     * @return true if Serialization module is available | 如果序列化模块可用返回 true
     */
    public static boolean isSerializationModuleAvailable() {
        return SERIALIZE_HANDLE != null && DESERIALIZE_HANDLE != null;
    }

    /**
     * Serializes an event to bytes.
     * 将事件序列化为字节数组
     *
     * @param event the event to serialize | 要序列化的事件
     * @param <T> the event type | 事件类型
     * @return the serialized bytes | 序列化的字节数组
     * @throws EventSerializationException if serialization fails | 如果序列化失败
     */
    public static <T extends Event> byte[] serialize(T event) {
        Objects.requireNonNull(event, "event must not be null");

        if (SERIALIZE_HANDLE != null) {
            try {
                return (byte[]) SERIALIZE_HANDLE.invokeWithArguments(event);
            } catch (Throwable e) {
                throw new EventSerializationException("Failed to serialize event using OpenSerializer", e);
            }
        }

        // Fallback to Java serialization
        return serializeWithJava(event);
    }

    /**
     * Deserializes bytes to an event.
     * 将字节数组反序列化为事件
     *
     * @param data the serialized data | 序列化的数据
     * @param eventType the event class | 事件类
     * @param <T> the event type | 事件类型
     * @return the deserialized event | 反序列化的事件
     * @throws EventSerializationException if deserialization fails | 如果反序列化失败
     */
    @SuppressWarnings("unchecked")
    public static <T extends Event> T deserialize(byte[] data, Class<T> eventType) {
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");

        if (DESERIALIZE_HANDLE != null) {
            try {
                return (T) DESERIALIZE_HANDLE.invokeWithArguments(data, eventType);
            } catch (Throwable e) {
                throw new EventSerializationException("Failed to deserialize event using OpenSerializer", e);
            }
        }

        // Fallback to Java serialization
        return deserializeWithJava(data, eventType);
    }

    /**
     * Serializes an event to a Base64 string.
     * 将事件序列化为 Base64 字符串
     *
     * @param event the event to serialize | 要序列化的事件
     * @param <T> the event type | 事件类型
     * @return the Base64 encoded string | Base64 编码的字符串
     * @throws EventSerializationException if serialization fails | 如果序列化失败
     */
    public static <T extends Event> String serializeToString(T event) {
        byte[] data = serialize(event);
        return java.util.Base64.getEncoder().encodeToString(data);
    }

    /**
     * Deserializes a Base64 string to an event.
     * 将 Base64 字符串反序列化为事件
     *
     * @param base64 the Base64 encoded string | Base64 编码的字符串
     * @param eventType the event class | 事件类
     * @param <T> the event type | 事件类型
     * @return the deserialized event | 反序列化的事件
     * @throws EventSerializationException if deserialization fails | 如果反序列化失败
     */
    public static <T extends Event> T deserializeFromString(String base64, Class<T> eventType) {
        Objects.requireNonNull(base64, "base64 must not be null");
        byte[] data = java.util.Base64.getDecoder().decode(base64);
        return deserialize(data, eventType);
    }

    private static byte[] serializeWithJava(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new EventSerializationException("Failed to serialize event using Java serialization", e);
        }
    }

    /**
     * Maximum depth for object deserialization
     */
    private static final long MAX_DEPTH = 20;

    /**
     * Maximum array size for deserialization
     */
    private static final long MAX_ARRAY_SIZE = 1_000_000;

    /**
     * Maximum total number of object references for deserialization
     */
    private static final long MAX_REFERENCES = 100_000;

    /**
     * Allowed package prefixes for deserialization
     */
    private static final String[] ALLOWED_PACKAGE_PREFIXES = {
            "cloud.opencode.base.event.",
            "java.lang.",
            "java.util.",
            "java.time.",
            "java.math.",
            "java.net.URI",
            "java.net.URL",
            "[B", // byte[]
            "[C", // char[]
            "[I", // int[]
            "[J", // long[]
            "[D", // double[]
            "[F", // float[]
            "[Z", // boolean[]
            "[S", // short[]
    };

    /**
     * Denied class prefixes for deserialization security
     */
    private static final String[] DENIED_PACKAGE_PREFIXES = {
            "java.lang.Runtime",
            "java.lang.ProcessBuilder",
            "java.lang.reflect.Proxy",
            "java.lang.Thread",
            "java.beans.XMLDecoder",
            "java.net.URLClassLoader",
            "java.rmi.",
            "javax.naming.",
            "javax.script.",
            "javax.management.",
            "javax.el.",
            "sun.",
            "com.sun.",
            "org.apache.commons.collections",
            "org.apache.xalan",
            "org.apache.bcel",
            "org.codehaus.groovy",
            "org.mozilla.javascript",
            "javassist.",
            "org.springframework.expression",
    };

    @SuppressWarnings("unchecked")
    private static <T> T deserializeWithJava(byte[] data, Class<T> type) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            ois.setObjectInputFilter(filterInfo -> {
                // Check depth
                if (filterInfo.depth() > MAX_DEPTH) {
                    return ObjectInputFilter.Status.REJECTED;
                }
                // Check array size
                if (filterInfo.arrayLength() >= 0 && filterInfo.arrayLength() > MAX_ARRAY_SIZE) {
                    return ObjectInputFilter.Status.REJECTED;
                }
                // Check total object references
                if (filterInfo.references() > MAX_REFERENCES) {
                    return ObjectInputFilter.Status.REJECTED;
                }
                // Check class
                Class<?> clazz = filterInfo.serialClass();
                if (clazz != null) {
                    String className = clazz.getName();
                    // Reject known dangerous class prefixes
                    for (String denied : DENIED_PACKAGE_PREFIXES) {
                        if (className.startsWith(denied)) {
                            return ObjectInputFilter.Status.REJECTED;
                        }
                    }
                    // Allow event package hierarchy and safe JDK types
                    for (String allowed : ALLOWED_PACKAGE_PREFIXES) {
                        if (className.startsWith(allowed)) {
                            return ObjectInputFilter.Status.ALLOWED;
                        }
                    }
                    // Allow array types of allowed classes (format: "[Lcom.example.Foo;")
                    if (className.startsWith("[L") && className.length() > 3 && className.endsWith(";")) {
                        String elementClass = className.substring(2, className.length() - 1);
                        for (String allowed : ALLOWED_PACKAGE_PREFIXES) {
                            if (elementClass.startsWith(allowed)) {
                                return ObjectInputFilter.Status.ALLOWED;
                            }
                        }
                    }
                    // Reject everything else by default
                    return ObjectInputFilter.Status.REJECTED;
                }
                // Null class (e.g., proxy, lambda) — reject by default for safety
                return ObjectInputFilter.Status.REJECTED;
            });
            Object obj = ois.readObject();
            if (!type.isInstance(obj)) {
                throw new EventSerializationException(
                        "Deserialized object is not of expected type: " + type.getName());
            }
            return (T) obj;
        } catch (IOException | ClassNotFoundException e) {
            throw new EventSerializationException("Failed to deserialize event using Java serialization", e);
        }
    }

    /**
     * Exception thrown when event serialization fails.
     * 事件序列化失败时抛出的异常
     */
    public static class EventSerializationException extends RuntimeException {
        public EventSerializationException(String message) {
            super(message);
        }

        public EventSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
