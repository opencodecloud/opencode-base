package cloud.opencode.base.cache.spi;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Cache Serializer SPI - Cache value serialization interface
 * 缓存序列化器 SPI - 缓存值序列化接口
 *
 * <p>Provides interface for serializing and deserializing cache values.</p>
 * <p>提供缓存值序列化和反序列化的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Serialize to bytes - 序列化为字节</li>
 *   <li>Deserialize from bytes - 从字节反序列化</li>
 *   <li>Size estimation - 大小估算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CacheSerializer<User> serializer = CacheSerializer.jdk();
 * byte[] bytes = serializer.serialize(user);
 * User user = serializer.deserialize(bytes);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No (throws on null) - 空值安全: 否（null 时抛异常）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for serialize/deserialize where n is the data size - 时间复杂度: serialize/deserialize 为 O(n)，n为数据大小</li>
 *   <li>Space complexity: O(n) for the serialized byte array - 空间复杂度: O(n)，存储序列化字节数组</li>
 * </ul>
 *
 * @param <V> the type of values | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public interface CacheSerializer<V> {

    /**
     * Serialize value to byte array
     * 将值序列化为字节数组
     *
     * @param value the value | 值
     * @return serialized bytes | 序列化后的字节
     */
    byte[] serialize(V value);

    /**
     * Deserialize byte array to value
     * 将字节数组反序列化为值
     *
     * @param bytes the bytes | 字节数组
     * @return deserialized value | 反序列化后的值
     */
    V deserialize(byte[] bytes);

    /**
     * Estimate serialized size
     * 估算序列化后的大小
     *
     * @param value the value | 值
     * @return estimated size in bytes | 估算的字节大小
     */
    default long estimateSize(V value) {
        return serialize(value).length;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create JDK serializer using Java serialization
     * 创建使用 Java 序列化的 JDK 序列化器
     *
     * @param <V> value type | 值类型
     * @return JDK serializer | JDK 序列化器
     */
    @SuppressWarnings("unchecked")
    static <V> CacheSerializer<V> jdk() {
        return new CacheSerializer<>() {
            @Override
            public byte[] serialize(V value) {
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                     ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                    oos.writeObject(value);
                    return bos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("Serialization failed", e);
                }
            }

            @Override
            public V deserialize(byte[] bytes) {
                try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                     ObjectInputStream ois = new ObjectInputStream(bis)) {
                    ois.setObjectInputFilter(filterInfo -> {
                        Class<?> clazz = filterInfo.serialClass();
                        if (clazz != null) {
                            String name = clazz.getName();
                            if (name.startsWith("javax.naming.") ||
                                name.startsWith("java.rmi.") ||
                                name.startsWith("sun.rmi.") ||
                                name.startsWith("org.apache.commons.collections") ||
                                name.startsWith("org.apache.xalan") ||
                                name.startsWith("javassist.") ||
                                name.startsWith("com.sun.org.apache.xalan")) {
                                return ObjectInputFilter.Status.REJECTED;
                            }
                        }
                        if (filterInfo.depth() > 20) return ObjectInputFilter.Status.REJECTED;
                        if (filterInfo.references() > 1000) return ObjectInputFilter.Status.REJECTED;
                        return ObjectInputFilter.Status.ALLOWED;
                    });
                    return (V) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException("Deserialization failed", e);
                }
            }
        };
    }

    /**
     * Create string serializer
     * 创建字符串序列化器
     *
     * @return string serializer | 字符串序列化器
     */
    static CacheSerializer<String> string() {
        return new CacheSerializer<>() {
            @Override
            public byte[] serialize(String value) {
                return value.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String deserialize(byte[] bytes) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        };
    }

    /**
     * Create identity serializer for byte arrays
     * 创建字节数组透传序列化器
     *
     * @return identity serializer | 透传序列化器
     */
    static CacheSerializer<byte[]> identity() {
        return new CacheSerializer<>() {
            @Override
            public byte[] serialize(byte[] value) {
                return value;
            }

            @Override
            public byte[] deserialize(byte[] bytes) {
                return bytes;
            }
        };
    }

    /**
     * Create JSON serializer using reflection (auto-detect Jackson or Gson)
     * 使用反射创建 JSON 序列化器（自动检测 Jackson 或 Gson）
     *
     * <p>Tries to detect and use Jackson first, then Gson. Falls back to basic
     * toString/fromString if neither is available.</p>
     * <p>优先尝试检测使用 Jackson，然后是 Gson。如果都不可用则回退到基本的
     * toString/fromString。</p>
     *
     * @param <V>  value type | 值类型
     * @param type the class type for deserialization | 反序列化的类类型
     * @return JSON serializer | JSON 序列化器
     * @since V1.9.0
     */
    static <V> CacheSerializer<V> json(Class<V> type) {
        return JsonSerializerFactory.create(type);
    }

    /**
     * Create Kryo serializer using reflection
     * 使用反射创建 Kryo 序列化器
     *
     * <p>Kryo provides high-performance serialization. Falls back to JDK
     * serialization if Kryo is not available.</p>
     * <p>Kryo 提供高性能序列化。如果 Kryo 不可用则回退到 JDK 序列化。</p>
     *
     * @param <V>  value type | 值类型
     * @param type the class type | 类类型
     * @return Kryo serializer or JDK fallback | Kryo 序列化器或 JDK 回退
     * @since V1.9.0
     */
    static <V> CacheSerializer<V> kryo(Class<V> type) {
        return KryoSerializerFactory.create(type);
    }

    /**
     * Create a compressed serializer wrapper
     * 创建压缩序列化器包装器
     *
     * <p>Wraps another serializer and applies GZIP compression.</p>
     * <p>包装另一个序列化器并应用 GZIP 压缩。</p>
     *
     * @param <V>      value type | 值类型
     * @param delegate the underlying serializer | 底层序列化器
     * @return compressed serializer | 压缩序列化器
     * @since V1.9.0
     */
    static <V> CacheSerializer<V> compressed(CacheSerializer<V> delegate) {
        return new CacheSerializer<>() {
            @Override
            public byte[] serialize(V value) {
                byte[] data = delegate.serialize(value);
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                     java.util.zip.GZIPOutputStream gzip = new java.util.zip.GZIPOutputStream(bos)) {
                    gzip.write(data);
                    gzip.finish();
                    return bos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("Compression failed", e);
                }
            }

            @Override
            public V deserialize(byte[] bytes) {
                try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                     java.util.zip.GZIPInputStream gzip = new java.util.zip.GZIPInputStream(bis);
                     ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = gzip.read(buffer)) > 0) {
                        bos.write(buffer, 0, len);
                    }
                    return delegate.deserialize(bos.toByteArray());
                } catch (IOException e) {
                    throw new RuntimeException("Decompression failed", e);
                }
            }

            @Override
            public long estimateSize(V value) {
                // Compression typically reduces size by 50-90%
                return (long) (delegate.estimateSize(value) * 0.3);
            }
        };
    }
}

/**
 * Factory for JSON serializer using reflection
 */
class JsonSerializerFactory {
    private static final boolean JACKSON_AVAILABLE;
    private static final boolean GSON_AVAILABLE;

    static {
        JACKSON_AVAILABLE = isClassAvailable("com.fasterxml.jackson.databind.ObjectMapper");
        GSON_AVAILABLE = isClassAvailable("com.google.gson.Gson");
    }

    private static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    static <V> CacheSerializer<V> create(Class<V> type) {
        if (JACKSON_AVAILABLE) {
            return createJacksonSerializer(type);
        } else if (GSON_AVAILABLE) {
            return createGsonSerializer(type);
        } else {
            // Fallback to simple toString/reflection (limited functionality)
            return createFallbackSerializer(type);
        }
    }

    private static <V> CacheSerializer<V> createJacksonSerializer(Class<V> type) {
        return new CacheSerializer<>() {
            private final Object mapper = createObjectMapper();

            private Object createObjectMapper() {
                try {
                    return Class.forName("com.fasterxml.jackson.databind.ObjectMapper")
                            .getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create ObjectMapper", e);
                }
            }

            @Override
            public byte[] serialize(V value) {
                try {
                    return (byte[]) mapper.getClass()
                            .getMethod("writeValueAsBytes", Object.class)
                            .invoke(mapper, value);
                } catch (Exception e) {
                    throw new RuntimeException("Jackson serialization failed", e);
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public V deserialize(byte[] bytes) {
                try {
                    return (V) mapper.getClass()
                            .getMethod("readValue", byte[].class, Class.class)
                            .invoke(mapper, bytes, type);
                } catch (Exception e) {
                    throw new RuntimeException("Jackson deserialization failed", e);
                }
            }
        };
    }

    private static <V> CacheSerializer<V> createGsonSerializer(Class<V> type) {
        return new CacheSerializer<>() {
            private final Object gson = createGson();

            private Object createGson() {
                try {
                    return Class.forName("com.google.gson.Gson")
                            .getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create Gson", e);
                }
            }

            @Override
            public byte[] serialize(V value) {
                try {
                    String json = (String) gson.getClass()
                            .getMethod("toJson", Object.class)
                            .invoke(gson, value);
                    return json.getBytes(StandardCharsets.UTF_8);
                } catch (Exception e) {
                    throw new RuntimeException("Gson serialization failed", e);
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public V deserialize(byte[] bytes) {
                try {
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    return (V) gson.getClass()
                            .getMethod("fromJson", String.class, Class.class)
                            .invoke(gson, json, type);
                } catch (Exception e) {
                    throw new RuntimeException("Gson deserialization failed", e);
                }
            }
        };
    }

    private static <V> CacheSerializer<V> createFallbackSerializer(Class<V> type) {
        // Fallback to JDK serialization with a warning
        System.getLogger(JsonSerializerFactory.class.getName())
                .log(System.Logger.Level.WARNING, "Neither Jackson nor Gson found. Using JDK serialization as fallback.");
        return CacheSerializer.jdk();
    }
}

/**
 * Factory for Kryo serializer using reflection
 */
class KryoSerializerFactory {
    private static final boolean KRYO_AVAILABLE;

    static {
        KRYO_AVAILABLE = isClassAvailable("com.esotericsoftware.kryo.Kryo");
    }

    private static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    static <V> CacheSerializer<V> create(Class<V> type) {
        if (KRYO_AVAILABLE) {
            return createKryoSerializer(type);
        } else {
            // Fallback to JDK serialization
            return CacheSerializer.jdk();
        }
    }

    private static <V> CacheSerializer<V> createKryoSerializer(Class<V> type) {
        return new CacheSerializer<>() {
            // Use ThreadLocal for thread-safety as Kryo is not thread-safe
            private final ThreadLocal<Object[]> kryoHolder = ThreadLocal.withInitial(() -> {
                try {
                    Object kryo = Class.forName("com.esotericsoftware.kryo.Kryo")
                            .getDeclaredConstructor().newInstance();
                    Object output = Class.forName("com.esotericsoftware.kryo.io.Output")
                            .getDeclaredConstructor(int.class).newInstance(4096);
                    Object input = Class.forName("com.esotericsoftware.kryo.io.Input")
                            .getDeclaredConstructor().newInstance();
                    return new Object[]{kryo, output, input};
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create Kryo", e);
                }
            });

            @Override
            public byte[] serialize(V value) {
                try {
                    Object[] holder = kryoHolder.get();
                    Object kryo = holder[0];
                    Object output = holder[1];

                    // Reset output
                    output.getClass().getMethod("reset").invoke(output);

                    // Write object
                    kryo.getClass().getMethod("writeObject",
                            Class.forName("com.esotericsoftware.kryo.io.Output"), Object.class)
                            .invoke(kryo, output, value);

                    // Get bytes
                    return (byte[]) output.getClass().getMethod("toBytes").invoke(output);
                } catch (Exception e) {
                    throw new RuntimeException("Kryo serialization failed", e);
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public V deserialize(byte[] bytes) {
                try {
                    Object[] holder = kryoHolder.get();
                    Object kryo = holder[0];
                    Object input = holder[2];

                    // Set buffer
                    input.getClass().getMethod("setBuffer", byte[].class).invoke(input, bytes);

                    // Read object
                    return (V) kryo.getClass().getMethod("readObject",
                            Class.forName("com.esotericsoftware.kryo.io.Input"), Class.class)
                            .invoke(kryo, input, type);
                } catch (Exception e) {
                    throw new RuntimeException("Kryo deserialization failed", e);
                }
            }
        };
    }
}
