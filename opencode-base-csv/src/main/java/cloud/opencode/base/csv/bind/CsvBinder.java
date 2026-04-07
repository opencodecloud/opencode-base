package cloud.opencode.base.csv.bind;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.bind.annotation.CsvColumn;
import cloud.opencode.base.csv.bind.annotation.CsvFormat;
import cloud.opencode.base.csv.exception.CsvBindException;
import cloud.opencode.base.csv.internal.RecordBinder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * CSV Binder - Object binding between CSV data and Java objects
 * CSV绑定器 - CSV数据和Java对象之间的对象绑定
 *
 * <p>Provides bidirectional mapping between CSV documents and Java objects,
 * supporting both Java Records and traditional POJOs. Uses annotation-based
 * configuration via {@link CsvColumn} and {@link CsvFormat} for flexible mapping.</p>
 * <p>提供CSV文档和Java对象之间的双向映射，支持Java Record和传统POJO。
 * 通过 {@link CsvColumn} 和 {@link CsvFormat} 注解进行灵活映射配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Java Record binding via canonical constructor - 通过规范构造函数绑定Java Record</li>
 *   <li>POJO binding via setters or direct field access - 通过setter或直接字段访问绑定POJO</li>
 *   <li>@CsvColumn for custom column name/index mapping - @CsvColumn自定义列名/索引映射</li>
 *   <li>@CsvFormat for date/number pattern formatting - @CsvFormat日期/数字格式化</li>
 *   <li>Required field validation - 必填字段验证</li>
 *   <li>Automatic type conversion (primitives, BigDecimal, dates, enums) - 自动类型转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Bind CSV to Records
 * List<PersonRecord> people = CsvBinder.bind(doc, PersonRecord.class);
 *
 * // Bind CSV to POJOs
 * List<Person> people = CsvBinder.bind(doc, Person.class);
 *
 * // Convert objects to CSV
 * CsvDocument doc = CsvBinder.fromObjects(people, Person.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see CsvColumn
 * @see CsvFormat
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvBinder {

    private CsvBinder() {
        // utility class
    }

    /**
     * Binds CSV document rows to Java objects
     * 将CSV文档行绑定到Java对象
     *
     * <p>Supports both Record types (via canonical constructor) and POJOs
     * (via no-arg constructor + setters/field access).</p>
     * <p>支持Record类型（通过规范构造函数）和POJO（通过无参构造函数+setter/字段访问）。</p>
     *
     * @param doc  the CSV document | CSV文档
     * @param type the target type | 目标类型
     * @param <T>  the target type | 目标类型
     * @return list of bound objects | 绑定的对象列表
     * @throws CsvBindException if binding fails | 如果绑定失败
     */
    public static <T> List<T> bind(CsvDocument doc, Class<T> type) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(type, "type must not be null");

        if (type.isRecord()) {
            return RecordBinder.bindRecords(doc, type);
        }
        return bindPojos(doc, type);
    }

    /**
     * Converts a collection of Java objects to a CSV document
     * 将Java对象集合转换为CSV文档
     *
     * <p>Extracts headers from field names or {@link CsvColumn} annotations,
     * and values via getter methods or field access.</p>
     * <p>从字段名或 {@link CsvColumn} 注解提取标题，通过getter方法或字段访问提取值。</p>
     *
     * @param objects the objects to convert | 要转换的对象
     * @param type    the object type | 对象类型
     * @param <T>     the object type | 对象类型
     * @return the CSV document | CSV文档
     * @throws CsvBindException if conversion fails | 如果转换失败
     */
    public static <T> CsvDocument fromObjects(Collection<T> objects, Class<T> type) {
        Objects.requireNonNull(objects, "objects must not be null");
        Objects.requireNonNull(type, "type must not be null");

        if (type.isRecord()) {
            return RecordBinder.fromRecords(objects, type);
        }
        return fromPojos(objects, type);
    }

    // ==================== POJO Binding | POJO绑定 ====================

    /**
     * Binds CSV rows to POJO instances via no-arg constructor and setters/fields
     */
    private static <T> List<T> bindPojos(CsvDocument doc, Class<T> type) {
        Field[] fields = getBindableFields(type);
        List<String> headers = doc.headers();
        int[] columnIndices = resolvePojoColumnIndices(fields, headers, type);

        List<T> result = new ArrayList<>(doc.rowCount());
        for (CsvRow row : doc.rows()) {
            T instance = createInstance(type);
            for (int i = 0; i < fields.length; i++) {
                String rawValue = getFieldValue(row, columnIndices[i]);
                CsvFormat format = fields[i].getAnnotation(CsvFormat.class);
                CsvColumn column = fields[i].getAnnotation(CsvColumn.class);

                // Check required
                if (column != null && column.required() && isBlank(rawValue)) {
                    throw new CsvBindException(
                            "Required field '" + fields[i].getName()
                                    + "' is blank at row " + row.rowNumber(),
                            type, fields[i].getName());
                }

                Object value = RecordBinder.convertValue(rawValue, fields[i].getType(),
                        format, fields[i].getName(), type);
                setFieldValue(instance, fields[i], value, type);
            }
            result.add(instance);
        }

        return List.copyOf(result);
    }

    /**
     * Converts POJO instances to CSV document
     */
    private static <T> CsvDocument fromPojos(Collection<T> objects, Class<T> type) {
        Field[] fields = getBindableFields(type);
        String[] headers = extractPojoHeaders(fields);

        CsvDocument.Builder builder = CsvDocument.builder().header(headers);

        for (T obj : objects) {
            String[] values = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                Object value = getPojoFieldValue(obj, fields[i], type);
                CsvFormat format = fields[i].getAnnotation(CsvFormat.class);
                values[i] = RecordBinder.formatValue(value, format);
            }
            builder.addRow(values);
        }

        return builder.build();
    }

    /**
     * Gets all bindable (non-static, non-transient) fields
     */
    private static Field[] getBindableFields(Class<?> type) {
        List<Field> bindable = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (!Modifier.isStatic(mod) && !Modifier.isTransient(mod)) {
                bindable.add(field);
            }
        }
        return bindable.toArray(new Field[0]);
    }

    /**
     * Resolves column indices for each POJO field
     */
    private static int[] resolvePojoColumnIndices(Field[] fields, List<String> headers,
                                                   Class<?> type) {
        int[] indices = new int[fields.length];
        for (int i = 0; i < fields.length; i++) {
            CsvColumn annotation = fields[i].getAnnotation(CsvColumn.class);

            if (annotation != null && !annotation.value().isEmpty()) {
                indices[i] = RecordBinder.findHeaderIndex(headers, annotation.value());
                if (indices[i] < 0) {
                    throw new CsvBindException(
                            "Header '" + annotation.value() + "' not found for field '"
                                    + fields[i].getName() + "'",
                            type, fields[i].getName());
                }
            } else if (annotation != null && annotation.index() >= 0) {
                indices[i] = annotation.index();
            } else {
                indices[i] = RecordBinder.findHeaderIndex(headers, fields[i].getName());
                if (indices[i] < 0) {
                    indices[i] = i; // fallback to positional
                }
            }
        }
        return indices;
    }

    /**
     * Creates an instance using the no-arg constructor
     */
    private static <T> T createInstance(Class<T> type) {
        try {
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new CsvBindException(
                    "No-arg constructor not found for type: " + type.getName(),
                    type, null, e);
        } catch (Exception e) {
            throw new CsvBindException(
                    "Failed to create instance of " + type.getName() + ": " + e.getMessage(),
                    type, null, e);
        }
    }

    /**
     * Sets a field value using setter or direct field access
     */
    private static void setFieldValue(Object instance, Field field, Object value,
                                       Class<?> type) {
        if (value == null) {
            return;
        }

        // Try setter first
        String setterName = "set" + capitalize(field.getName());
        try {
            Method setter = type.getMethod(setterName, field.getType());
            setter.invoke(instance, value);
            return;
        } catch (NoSuchMethodException ignored) {
            // Fall through to direct field access
        } catch (Exception e) {
            throw new CsvBindException(
                    "Failed to invoke setter " + setterName + ": " + e.getMessage(),
                    type, field.getName(), e);
        }

        // Direct field access
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new CsvBindException(
                    "Failed to set field '" + field.getName() + "': " + e.getMessage(),
                    type, field.getName(), e);
        }
    }

    /**
     * Gets a field value using getter or direct field access
     */
    private static Object getPojoFieldValue(Object instance, Field field, Class<?> type) {
        // Try getter first
        String getterName = (field.getType() == boolean.class || field.getType() == Boolean.class)
                ? "is" + capitalize(field.getName())
                : "get" + capitalize(field.getName());
        try {
            Method getter = type.getMethod(getterName);
            return getter.invoke(instance);
        } catch (NoSuchMethodException ignored) {
            // Try "get" prefix for booleans too
            if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                try {
                    Method getter = type.getMethod("get" + capitalize(field.getName()));
                    return getter.invoke(instance);
                } catch (NoSuchMethodException ignored2) {
                    // Fall through
                } catch (Exception e) {
                    throw new CsvBindException(
                            "Failed to invoke getter: " + e.getMessage(),
                            type, field.getName(), e);
                }
            }
        } catch (Exception e) {
            throw new CsvBindException(
                    "Failed to invoke getter " + getterName + ": " + e.getMessage(),
                    type, field.getName(), e);
        }

        // Direct field access
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            throw new CsvBindException(
                    "Failed to read field '" + field.getName() + "': " + e.getMessage(),
                    type, field.getName(), e);
        }
    }

    /**
     * Extracts header names from POJO fields
     */
    private static String[] extractPojoHeaders(Field[] fields) {
        String[] headers = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            CsvColumn annotation = fields[i].getAnnotation(CsvColumn.class);
            if (annotation != null && !annotation.value().isEmpty()) {
                headers[i] = annotation.value();
            } else {
                headers[i] = fields[i].getName();
            }
        }
        return headers;
    }

    /**
     * Gets field value from row by column index
     */
    private static String getFieldValue(CsvRow row, int columnIndex) {
        if (columnIndex < 0 || columnIndex >= row.size()) {
            return null;
        }
        return row.get(columnIndex);
    }

    /**
     * Capitalizes the first character of a string
     */
    private static String capitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Checks if a string is blank
     */
    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
