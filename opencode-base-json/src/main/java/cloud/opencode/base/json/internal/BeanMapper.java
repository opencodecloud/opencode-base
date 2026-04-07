package cloud.opencode.base.json.internal;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.annotation.JsonIgnore;
import cloud.opencode.base.json.annotation.JsonProperty;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;

import java.lang.reflect.*;
import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean Mapper - Reflection-Based POJO to/from JsonNode Mapper
 * Bean映射器 - 基于反射的POJO与JsonNode双向映射器
 *
 * <p>Provides full POJO mapping using pure JDK reflection, supporting:</p>
 * <p>使用纯JDK反射提供完整的POJO映射，支持：</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>POJO class / Record class mapping - POJO类 / Record类映射</li>
 *   <li>{@code @JsonProperty} name mapping and required validation - 名称映射和必填验证</li>
 *   <li>{@code @JsonIgnore} field exclusion - 字段排除</li>
 *   <li>Nested object and collection mapping - 嵌套对象和集合映射</li>
 *   <li>Generic type resolution (List&lt;User&gt;, Map&lt;String, User&gt;) - 泛型类型解析</li>
 *   <li>Enum, Date/Time (java.time), UUID, Optional support - 枚举、时间、UUID、Optional支持</li>
 *   <li>Field metadata caching for performance - 字段元数据缓存提升性能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap cache) - 线程安全: 是</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // See class-level documentation for usage
 * // 参见类级文档了解用法
 * }</pre>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
final class BeanMapper {

    private static final ConcurrentHashMap<Class<?>, List<FieldMeta>> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, RecordMeta> RECORD_META_CACHE = new ConcurrentHashMap<>();

    private static final Set<String> DENIED_PACKAGES = Set.of(
            "java.lang.Runtime", "java.lang.ProcessBuilder",
            "java.lang.reflect.Proxy", "java.net.URLClassLoader",
            "javax.script.ScriptEngineManager", "java.rmi.",
            "javax.naming.", "javax.management."
    );

    private static void checkClassSafety(Class<?> clazz) {
        String name = clazz.getName();
        for (String denied : DENIED_PACKAGES) {
            if (name.startsWith(denied) || name.equals(denied)) {
                throw OpenJsonProcessingException.deserializationError(
                        "Deserialization of " + name + " is not allowed for security reasons", null);
            }
        }
    }

    private record RecordMeta(
            Constructor<?> constructor,
            String[] jsonNames,
            Class<?>[] paramTypes,
            Type[] genericTypes
    ) {}

    private BeanMapper() {
    }

    // ==================== Record for cached field metadata ====================

    private record FieldMeta(
            Field field,
            String jsonName,
            boolean ignored,
            boolean ignoreSerialize,
            boolean ignoreDeserialize,
            boolean required
    ) {
    }

    // ==================== Object → JsonNode (Serialization) ====================

    /**
     * Converts a POJO to a JsonNode
     * 将POJO转换为JsonNode
     *
     * @param obj the object | 对象
     * @return the JsonNode | JsonNode
     */
    private static final int MAX_DEPTH = 128;

    static JsonNode toTree(Object obj) {
        if (obj == null) return JsonNode.nullNode();
        return toNode(obj, obj.getClass(), 0, null);
    }

    @SuppressWarnings("unchecked")
    private static JsonNode toNode(Object obj, Type declaredType, int depth, Set<Object> visited) {
        if (depth > MAX_DEPTH) {
            throw OpenJsonProcessingException.serializationError("Nesting depth exceeds " + MAX_DEPTH, null);
        }
        if (obj == null) return JsonNode.nullNode();

        // Primitives & wrappers
        if (obj instanceof String s) return new JsonNode.StringNode(s);
        if (obj instanceof Boolean b) return new JsonNode.BooleanNode(b);
        if (obj instanceof Integer i) return new JsonNode.NumberNode(i);
        if (obj instanceof Long l) return new JsonNode.NumberNode(l);
        if (obj instanceof Double d) return new JsonNode.NumberNode(d);
        if (obj instanceof Float f) return new JsonNode.NumberNode(f.doubleValue());
        if (obj instanceof Short s) return new JsonNode.NumberNode(s.intValue());
        if (obj instanceof Byte b) return new JsonNode.NumberNode(b.intValue());
        if (obj instanceof BigDecimal bd) return new JsonNode.NumberNode(bd);
        if (obj instanceof BigInteger bi) return new JsonNode.NumberNode(new BigDecimal(bi));
        if (obj instanceof Number n) return new JsonNode.NumberNode(n.doubleValue());
        if (obj instanceof Character c) return new JsonNode.StringNode(c.toString());

        // Enums
        if (obj instanceof Enum<?> e) return new JsonNode.StringNode(e.name());

        // Date/Time
        if (obj instanceof Instant t) return new JsonNode.StringNode(t.toString());
        if (obj instanceof LocalDateTime t) return new JsonNode.StringNode(t.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        if (obj instanceof LocalDate t) return new JsonNode.StringNode(t.format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (obj instanceof LocalTime t) return new JsonNode.StringNode(t.format(DateTimeFormatter.ISO_LOCAL_TIME));
        if (obj instanceof ZonedDateTime t) return new JsonNode.StringNode(t.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        if (obj instanceof OffsetDateTime t) return new JsonNode.StringNode(t.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        if (obj instanceof Duration d) return new JsonNode.StringNode(d.toString());
        if (obj instanceof Period p) return new JsonNode.StringNode(p.toString());

        // UUID
        if (obj instanceof UUID u) return new JsonNode.StringNode(u.toString());

        // Optional
        if (obj instanceof Optional<?> opt) {
            Set<Object> visitedRef = visited;
            return opt.map(v -> toNode(v, Object.class, depth + 1, visitedRef)).orElse(JsonNode.nullNode());
        }

        // JsonNode pass-through
        if (obj instanceof JsonNode node) return node;

        // Circular reference detection for composite types (Map, Collection, Array, POJO)
        if (visited == null) {
            visited = Collections.newSetFromMap(new IdentityHashMap<>());
        }
        if (!visited.add(obj)) {
            throw OpenJsonProcessingException.serializationError(
                    "Circular reference detected for " + obj.getClass().getName(), null);
        }

        try {
            // Map
            if (obj instanceof Map<?, ?> map) {
                JsonNode.ObjectNode objNode = JsonNode.object();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    String key = String.valueOf(entry.getKey());
                    objNode.put(key, toNode(entry.getValue(), Object.class, depth + 1, visited));
                }
                return objNode;
            }

            // Collection
            if (obj instanceof Collection<?> coll) {
                JsonNode.ArrayNode arrNode = JsonNode.array();
                for (Object item : coll) {
                    arrNode.add(toNode(item, Object.class, depth + 1, visited));
                }
                return arrNode;
            }

            // Array
            if (obj.getClass().isArray()) {
                JsonNode.ArrayNode arrNode = JsonNode.array();
                int len = Array.getLength(obj);
                for (int i = 0; i < len; i++) {
                    arrNode.add(toNode(Array.get(obj, i), Object.class, depth + 1, visited));
                }
                return arrNode;
            }

            // POJO / Record → Object
            return beanToNode(obj, depth, visited);
        } finally {
            visited.remove(obj);
        }
    }

    private static JsonNode beanToNode(Object obj, int depth, Set<Object> visited) {
        Class<?> clazz = obj.getClass();
        JsonNode.ObjectNode node = JsonNode.object();
        List<FieldMeta> fields = getFieldMetas(clazz);

        for (FieldMeta fm : fields) {
            if (fm.ignored || fm.ignoreSerialize) continue;
            try {
                Object value = fm.field.get(obj);
                node.put(fm.jsonName, toNode(value, fm.field.getGenericType(), depth + 1, visited));
            } catch (IllegalAccessException e) {
                // skip inaccessible field
            }
        }
        return node;
    }

    // ==================== JsonNode → Object (Deserialization) ====================

    /**
     * Converts a JsonNode to a POJO of the specified type
     * 将JsonNode转换为指定类型的POJO
     *
     * @param node the JsonNode | JsonNode
     * @param type the target type | 目标类型
     * @param <T>  the target type | 目标类型
     * @return the deserialized object | 反序列化的对象
     */
    @SuppressWarnings("unchecked")
    static <T> T fromTree(JsonNode node, Type type) {
        if (node == null || node.isNull()) return null;
        return (T) fromNode(node, type, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object fromNode(JsonNode node, Type type, int depth) {
        if (depth > 128) {
            throw OpenJsonProcessingException.deserializationError("Nesting depth exceeds 128", null);
        }
        if (node == null || node.isNull()) return null;

        Class<?> rawClass = getRawClass(type);

        // JsonNode itself
        if (JsonNode.class.isAssignableFrom(rawClass)) return node;

        // String
        if (rawClass == String.class) return nodeToString(node);

        // Primitive wrappers
        if (rawClass == Integer.class || rawClass == int.class) return node.asInt();
        if (rawClass == Long.class || rawClass == long.class) return node.asLong();
        if (rawClass == Double.class || rawClass == double.class) return node.asDouble();
        if (rawClass == Float.class || rawClass == float.class) return (float) node.asDouble();
        if (rawClass == Boolean.class || rawClass == boolean.class) return node.asBoolean();
        if (rawClass == Short.class || rawClass == short.class) {
            int val = node.asInt();
            if (val < Short.MIN_VALUE || val > Short.MAX_VALUE) {
                throw OpenJsonProcessingException.deserializationError(
                        "Value " + val + " out of range for short", null);
            }
            return (short) val;
        }
        if (rawClass == Byte.class || rawClass == byte.class) {
            int val = node.asInt();
            if (val < Byte.MIN_VALUE || val > Byte.MAX_VALUE) {
                throw OpenJsonProcessingException.deserializationError(
                        "Value " + val + " out of range for byte", null);
            }
            return (byte) val;
        }
        if (rawClass == Character.class || rawClass == char.class) {
            String s = nodeToString(node);
            return (s != null && !s.isEmpty()) ? s.charAt(0) : '\0';
        }
        if (rawClass == BigDecimal.class) return node.asBigDecimal();
        if (rawClass == BigInteger.class) {
            BigDecimal bd = node.asBigDecimal();
            return bd != null ? bd.toBigInteger() : null;
        }
        if (Number.class.isAssignableFrom(rawClass)) return node.asDouble();

        // Enum
        if (rawClass.isEnum()) {
            String name = nodeToString(node);
            return name != null ? Enum.valueOf((Class<Enum>) rawClass, name) : null;
        }

        // Date/Time
        if (rawClass == Instant.class) return Instant.parse(node.asString());
        if (rawClass == LocalDateTime.class) return LocalDateTime.parse(node.asString());
        if (rawClass == LocalDate.class) return LocalDate.parse(node.asString());
        if (rawClass == LocalTime.class) return LocalTime.parse(node.asString());
        if (rawClass == ZonedDateTime.class) return ZonedDateTime.parse(node.asString());
        if (rawClass == OffsetDateTime.class) return OffsetDateTime.parse(node.asString());
        if (rawClass == Duration.class) return Duration.parse(node.asString());
        if (rawClass == Period.class) return Period.parse(node.asString());

        // UUID
        if (rawClass == UUID.class) return UUID.fromString(node.asString());

        // Optional
        if (rawClass == Optional.class) {
            Type innerType = getTypeArgument(type, 0, Object.class);
            return Optional.ofNullable(fromNode(node, innerType, depth + 1));
        }

        // Object (untyped)
        if (rawClass == Object.class) return nodeToUntyped(node);

        // Map
        if (Map.class.isAssignableFrom(rawClass) && node.isObject()) {
            Type valueType = getTypeArgument(type, 1, Object.class);
            JsonNode.ObjectNode obj = (JsonNode.ObjectNode) node;
            Map<String, Object> map = new LinkedHashMap<>(obj.size() * 4 / 3 + 1);
            for (String key : obj.keys()) {
                map.put(key, fromNode(obj.get(key), valueType, depth + 1));
            }
            return map;
        }

        // List / Collection
        if (List.class.isAssignableFrom(rawClass) || Collection.class.isAssignableFrom(rawClass)) {
            if (!node.isArray()) {
                throw OpenJsonProcessingException.deserializationError("Expected JSON array for " + rawClass.getName(), null);
            }
            Type elemType = getTypeArgument(type, 0, Object.class);
            JsonNode.ArrayNode arr = (JsonNode.ArrayNode) node;
            List<Object> list = new ArrayList<>(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                list.add(fromNode(arr.get(i), elemType, depth + 1));
            }
            if (Set.class.isAssignableFrom(rawClass)) return new LinkedHashSet<>(list);
            return list;
        }

        // Array
        if (rawClass.isArray()) {
            if (!node.isArray()) {
                throw OpenJsonProcessingException.deserializationError("Expected JSON array for array type", null);
            }
            Class<?> componentType = rawClass.getComponentType();
            JsonNode.ArrayNode arr = (JsonNode.ArrayNode) node;
            Object array = Array.newInstance(componentType, arr.size());
            for (int i = 0; i < arr.size(); i++) {
                Array.set(array, i, fromNode(arr.get(i), componentType, depth + 1));
            }
            return array;
        }

        // POJO / Record
        if (node.isObject()) {
            return nodeToBean((JsonNode.ObjectNode) node, rawClass, depth);
        }

        throw OpenJsonProcessingException.deserializationError(
                "Cannot deserialize " + node.getClass().getSimpleName() + " to " + rawClass.getName(), null);
    }

    private static Object nodeToBean(JsonNode.ObjectNode node, Class<?> clazz, int depth) {
        if (clazz.isRecord()) {
            return nodeToRecord(node, clazz, depth);
        }
        return nodeToPojo(node, clazz, depth);
    }

    private static Object nodeToRecord(JsonNode.ObjectNode node, Class<?> recordClass, int depth) {
        checkClassSafety(recordClass);
        RecordMeta meta = getRecordMeta(recordClass);
        Object[] args = new Object[meta.paramTypes.length];

        for (int i = 0; i < meta.paramTypes.length; i++) {
            JsonNode fieldNode = node.get(meta.jsonNames[i]);
            if (fieldNode == null || fieldNode.isNull()) {
                args[i] = defaultValue(meta.paramTypes[i]);
            } else {
                args[i] = fromNode(fieldNode, meta.genericTypes[i], depth + 1);
            }
        }

        try {
            return meta.constructor.newInstance(args);
        } catch (Exception e) {
            throw OpenJsonProcessingException.deserializationError(
                    "Failed to construct record " + recordClass.getName() + ": " + e.getMessage(), e);
        }
    }

    private static RecordMeta getRecordMeta(Class<?> recordClass) {
        RecordMeta cached = RECORD_META_CACHE.get(recordClass);
        if (cached != null) return cached;
        RecordMeta meta = buildRecordMeta(recordClass);
        if (RECORD_META_CACHE.size() < 10_000) {
            RECORD_META_CACHE.putIfAbsent(recordClass, meta);
        }
        return meta;
    }

    private static RecordMeta buildRecordMeta(Class<?> recordClass) {
        RecordComponent[] components = recordClass.getRecordComponents();
        String[] jsonNames = new String[components.length];
        Class<?>[] paramTypes = new Class<?>[components.length];
        Type[] genericTypes = new Type[components.length];

        for (int i = 0; i < components.length; i++) {
            paramTypes[i] = components[i].getType();
            genericTypes[i] = components[i].getGenericType();
            String name = components[i].getName();
            JsonProperty prop = components[i].getAnnotation(JsonProperty.class);
            if (prop == null) {
                try {
                    Field field = recordClass.getDeclaredField(components[i].getName());
                    prop = field.getAnnotation(JsonProperty.class);
                } catch (NoSuchFieldException ignored) {
                }
            }
            jsonNames[i] = (prop != null && !prop.value().isEmpty()) ? prop.value() : name;
        }

        try {
            Constructor<?> ctor = recordClass.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return new RecordMeta(ctor, jsonNames, paramTypes, genericTypes);
        } catch (NoSuchMethodException e) {
            throw OpenJsonProcessingException.deserializationError(
                    "No canonical constructor for record " + recordClass.getName(), e);
        }
    }

    private static Object nodeToPojo(JsonNode.ObjectNode node, Class<?> clazz, int depth) {
        checkClassSafety(clazz);
        Object instance;
        try {
            Constructor<?> ctor = getNoArgConstructor(clazz);
            ctor.setAccessible(true);
            instance = ctor.newInstance();
        } catch (Exception e) {
            throw OpenJsonProcessingException.deserializationError(
                    "No accessible no-arg constructor for " + clazz.getName(), e);
        }

        List<FieldMeta> fields = getFieldMetas(clazz);
        for (FieldMeta fm : fields) {
            if (fm.ignored || fm.ignoreDeserialize) continue;
            JsonNode fieldNode = node.get(fm.jsonName);
            if (fieldNode == null || fieldNode.isNull()) {
                if (fm.required) {
                    throw OpenJsonProcessingException.deserializationError(
                            "Required field '" + fm.jsonName + "' is missing", null);
                }
                continue;
            }
            try {
                Object value = fromNode(fieldNode, fm.field.getGenericType(), depth + 1);
                fm.field.set(instance, value);
            } catch (IllegalAccessException e) {
                // skip
            }
        }
        return instance;
    }

    // ==================== Field Metadata Cache ====================

    private static List<FieldMeta> getFieldMetas(Class<?> clazz) {
        List<FieldMeta> cached = FIELD_CACHE.get(clazz);
        if (cached != null) return cached;
        List<FieldMeta> metas = buildFieldMetas(clazz);
        if (FIELD_CACHE.size() < 10_000) {
            FIELD_CACHE.putIfAbsent(clazz, metas);
            cached = FIELD_CACHE.get(clazz);
            return cached != null ? cached : metas;
        }
        return metas;
    }

    private static List<FieldMeta> buildFieldMetas(Class<?> clazz) {
        List<FieldMeta> metas = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) continue;
                field.setAccessible(true);

                String jsonName = field.getName();
                boolean ignored = false;
                boolean ignoreSer = false;
                boolean ignoreDeser = false;
                boolean required = false;

                JsonProperty prop = field.getAnnotation(JsonProperty.class);
                if (prop != null) {
                    if (!prop.value().isEmpty()) jsonName = prop.value();
                    required = prop.required();
                    if (prop.access() == JsonProperty.Access.READ_ONLY) ignoreDeser = true;
                    if (prop.access() == JsonProperty.Access.WRITE_ONLY) ignoreSer = true;
                }

                JsonIgnore ignore = field.getAnnotation(JsonIgnore.class);
                if (ignore != null) {
                    ignoreSer = ignore.serialize();
                    ignoreDeser = ignore.deserialize();
                    if (ignoreSer && ignoreDeser) ignored = true;
                }

                metas.add(new FieldMeta(field, jsonName, ignored, ignoreSer, ignoreDeser, required));
            }
            current = current.getSuperclass();
        }
        return Collections.unmodifiableList(metas);
    }

    private static Constructor<?> getNoArgConstructor(Class<?> clazz) {
        Constructor<?> cached = CONSTRUCTOR_CACHE.get(clazz);
        if (cached != null) return cached;
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            if (CONSTRUCTOR_CACHE.size() < 10_000) {
                CONSTRUCTOR_CACHE.putIfAbsent(clazz, ctor);
            }
            return ctor;
        } catch (NoSuchMethodException e) {
            throw OpenJsonProcessingException.deserializationError(
                    "No no-arg constructor for " + clazz.getName(), e);
        }
    }

    // ==================== Helpers ====================

    private static String nodeToString(JsonNode node) {
        if (node.isString()) return node.asString();
        if (node.isNull()) return null;
        if (node.isNumber()) return String.valueOf(node.asDouble());
        if (node.isBoolean()) return String.valueOf(node.asBoolean());
        return node.asString();
    }

    private static Object nodeToUntyped(JsonNode node) {
        return switch (node) {
            case JsonNode.ObjectNode obj -> {
                Map<String, Object> map = new LinkedHashMap<>();
                for (String key : obj.keys()) map.put(key, nodeToUntyped(obj.get(key)));
                yield map;
            }
            case JsonNode.ArrayNode arr -> {
                List<Object> list = new ArrayList<>(arr.size());
                for (int i = 0; i < arr.size(); i++) list.add(nodeToUntyped(arr.get(i)));
                yield list;
            }
            case JsonNode.StringNode s -> s.value();
            case JsonNode.NumberNode n -> n.value();
            case JsonNode.BooleanNode b -> b.value();
            case JsonNode.NullNode _ -> null;
        };
    }

    private static Class<?> getRawClass(Type type) {
        if (type instanceof Class<?> c) return c;
        if (type instanceof ParameterizedType pt) return (Class<?>) pt.getRawType();
        if (type instanceof GenericArrayType gat) {
            Class<?> component = getRawClass(gat.getGenericComponentType());
            return Array.newInstance(component, 0).getClass();
        }
        if (type instanceof WildcardType wt) {
            Type[] upper = wt.getUpperBounds();
            return upper.length > 0 ? getRawClass(upper[0]) : Object.class;
        }
        return Object.class;
    }

    private static Type getTypeArgument(Type type, int index, Type defaultType) {
        if (type instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (index < args.length) return args[index];
        }
        return defaultType;
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == boolean.class) return false;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == char.class) return '\0';
        return null;
    }
}
