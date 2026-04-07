package cloud.opencode.base.csv.internal;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.bind.annotation.CsvColumn;
import cloud.opencode.base.csv.bind.annotation.CsvFormat;
import cloud.opencode.base.csv.exception.CsvBindException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Record Binder - Helper for binding CSV data to Java Records
 * Record绑定器 - 将CSV数据绑定到Java Record的辅助类
 *
 * <p>Uses {@link RecordComponent} reflection to discover record components,
 * match them to CSV headers, perform type conversion, and invoke the
 * canonical constructor. For annotation lookup on records, retrieves
 * annotations from the backing field since {@link CsvColumn} and
 * {@link CsvFormat} target {@code ElementType.FIELD}.</p>
 * <p>使用 {@link RecordComponent} 反射发现record组件，将其与CSV标题匹配，
 * 执行类型转换，并调用规范构造函数。对于record上的注解查找，由于
 * {@link CsvColumn} 和 {@link CsvFormat} 目标为 {@code ElementType.FIELD}，
 * 因此从对应的字段中检索注解。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class RecordBinder {

    private RecordBinder() {
        // utility class
    }

    /**
     * Binds CSV document rows to Record instances
     * 将CSV文档行绑定到Record实例
     *
     * @param doc        the CSV document | CSV文档
     * @param recordType the record class | record类
     * @param <T>        the record type | record类型
     * @return list of record instances | record实例列表
     * @throws CsvBindException if binding fails | 如果绑定失败
     */
    public static <T> List<T> bindRecords(CsvDocument doc, Class<T> recordType) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(recordType, "recordType must not be null");

        if (!recordType.isRecord()) {
            throw new CsvBindException("Type is not a record: " + recordType.getName(),
                    recordType, null);
        }

        RecordComponent[] components = recordType.getRecordComponents();
        Field[] backingFields = getRecordFields(recordType, components);
        List<String> headers = doc.headers();

        // Build column index mapping for each component (using field annotations)
        int[] columnIndices = resolveColumnIndices(components, backingFields, headers, recordType);

        // Get canonical constructor
        Constructor<T> constructor = getCanonicalConstructor(recordType, components);

        List<T> result = new ArrayList<>(doc.rowCount());
        for (CsvRow row : doc.rows()) {
            Object[] args = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                String rawValue = getFieldValue(row, columnIndices[i]);
                CsvFormat format = backingFields[i].getAnnotation(CsvFormat.class);
                CsvColumn column = backingFields[i].getAnnotation(CsvColumn.class);

                // Check required
                if (column != null && column.required() && isBlank(rawValue)) {
                    throw new CsvBindException(
                            "Required field '" + components[i].getName()
                                    + "' is blank at row " + row.rowNumber(),
                            recordType, components[i].getName());
                }

                args[i] = convertValue(rawValue, components[i].getType(), format,
                        components[i].getName(), recordType);
            }

            try {
                result.add(constructor.newInstance(args));
            } catch (CsvBindException e) {
                throw e;
            } catch (Exception e) {
                throw new CsvBindException(
                        "Failed to create record instance at row " + row.rowNumber()
                                + ": " + e.getMessage(),
                        recordType, null, e);
            }
        }

        return List.copyOf(result);
    }

    /**
     * Converts Record instances to a CSV document
     * 将Record实例转换为CSV文档
     *
     * @param records    the record instances | record实例
     * @param recordType the record class | record类
     * @param <T>        the record type | record类型
     * @return the CSV document | CSV文档
     * @throws CsvBindException if conversion fails | 如果转换失败
     */
    public static <T> CsvDocument fromRecords(Collection<T> records, Class<T> recordType) {
        Objects.requireNonNull(records, "records must not be null");
        Objects.requireNonNull(recordType, "recordType must not be null");

        if (!recordType.isRecord()) {
            throw new CsvBindException("Type is not a record: " + recordType.getName(),
                    recordType, null);
        }

        RecordComponent[] components = recordType.getRecordComponents();
        Field[] backingFields = getRecordFields(recordType, components);
        String[] headers = extractHeaders(components, backingFields);

        CsvDocument.Builder builder = CsvDocument.builder().header(headers);

        for (T record : records) {
            String[] values = new String[components.length];
            for (int i = 0; i < components.length; i++) {
                try {
                    var accessor = components[i].getAccessor();
                    accessor.setAccessible(true);
                    Object value = accessor.invoke(record);
                    CsvFormat format = backingFields[i].getAnnotation(CsvFormat.class);
                    values[i] = formatValue(value, format);
                } catch (CsvBindException e) {
                    throw e;
                } catch (Exception e) {
                    throw new CsvBindException(
                            "Failed to access record component '"
                                    + components[i].getName() + "': " + e.getMessage(),
                            recordType, components[i].getName(), e);
                }
            }
            builder.addRow(values);
        }

        return builder.build();
    }

    // ==================== Internal Helpers | 内部辅助方法 ====================

    /**
     * Gets the backing fields for record components.
     * Since @CsvColumn and @CsvFormat target ElementType.FIELD, annotations
     * must be retrieved from the field, not the RecordComponent.
     * 获取record组件的对应字段。由于 @CsvColumn 和 @CsvFormat 目标为 ElementType.FIELD，
     * 注解必须从字段而非RecordComponent获取。
     */
    private static Field[] getRecordFields(Class<?> recordType, RecordComponent[] components) {
        Field[] fields = new Field[components.length];
        for (int i = 0; i < components.length; i++) {
            try {
                fields[i] = recordType.getDeclaredField(components[i].getName());
                fields[i].setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new CsvBindException(
                        "Backing field not found for record component: " + components[i].getName(),
                        recordType, components[i].getName(), e);
            }
        }
        return fields;
    }

    /**
     * Resolves column indices for each record component using backing field annotations
     */
    private static int[] resolveColumnIndices(RecordComponent[] components,
                                              Field[] backingFields,
                                              List<String> headers,
                                              Class<?> recordType) {
        int[] indices = new int[components.length];
        for (int i = 0; i < components.length; i++) {
            CsvColumn annotation = backingFields[i].getAnnotation(CsvColumn.class);

            if (annotation != null && !annotation.value().isEmpty()) {
                // Match by annotation value (case-insensitive)
                indices[i] = findHeaderIndex(headers, annotation.value());
                if (indices[i] < 0) {
                    throw new CsvBindException(
                            "Header '" + annotation.value() + "' not found for component '"
                                    + components[i].getName() + "'",
                            recordType, components[i].getName());
                }
            } else if (annotation != null && annotation.index() >= 0) {
                // Match by explicit index
                indices[i] = annotation.index();
            } else {
                // Match by component name (case-insensitive)
                indices[i] = findHeaderIndex(headers, components[i].getName());
                if (indices[i] < 0) {
                    indices[i] = i; // fallback to positional
                }
            }
        }
        return indices;
    }

    /**
     * Finds header index by name (case-insensitive)
     * 按名称查找标题索引（不区分大小写）
     */
    public static int findHeaderIndex(List<String> headers, String name) {
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the canonical constructor for a record type
     */
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getCanonicalConstructor(Class<T> recordType,
                                                               RecordComponent[] components) {
        Class<?>[] paramTypes = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            paramTypes[i] = components[i].getType();
        }
        try {
            Constructor<T> constructor = recordType.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new CsvBindException(
                    "Canonical constructor not found for record: " + recordType.getName(),
                    recordType, null, e);
        }
    }

    /**
     * Gets field value from row by column index, returning null if out of bounds
     */
    private static String getFieldValue(CsvRow row, int columnIndex) {
        if (columnIndex < 0 || columnIndex >= row.size()) {
            return null;
        }
        return row.get(columnIndex);
    }

    /**
     * Extracts header names from record components using backing field annotations
     */
    private static String[] extractHeaders(RecordComponent[] components, Field[] backingFields) {
        String[] headers = new String[components.length];
        for (int i = 0; i < components.length; i++) {
            CsvColumn annotation = backingFields[i].getAnnotation(CsvColumn.class);
            if (annotation != null && !annotation.value().isEmpty()) {
                headers[i] = annotation.value();
            } else {
                headers[i] = components[i].getName();
            }
        }
        return headers;
    }

    /**
     * Converts a string value to the target type with optional format pattern
     * 将字符串值转换为目标类型，可选格式模式
     */
    public static Object convertValue(String raw, Class<?> type, CsvFormat format,
                               String fieldName, Class<?> ownerType) {
        if (raw == null || raw.isEmpty()) {
            // Handle null/empty with @CsvFormat.nullValue
            if (format != null && !format.nullValue().isEmpty()) {
                raw = format.nullValue();
            }
            if (raw == null || raw.isEmpty()) {
                return defaultValue(type);
            }
        }

        try {
            if (type == String.class) {
                return raw;
            }
            if (type == int.class || type == Integer.class) {
                return Integer.parseInt(raw.strip());
            }
            if (type == long.class || type == Long.class) {
                return Long.parseLong(raw.strip());
            }
            if (type == double.class || type == Double.class) {
                return Double.parseDouble(raw.strip());
            }
            if (type == float.class || type == Float.class) {
                return Float.parseFloat(raw.strip());
            }
            if (type == boolean.class || type == Boolean.class) {
                return Boolean.parseBoolean(raw.strip());
            }
            if (type == short.class || type == Short.class) {
                return Short.parseShort(raw.strip());
            }
            if (type == byte.class || type == Byte.class) {
                return Byte.parseByte(raw.strip());
            }
            if (type == char.class || type == Character.class) {
                return raw.charAt(0);
            }
            if (type == BigDecimal.class) {
                return new BigDecimal(raw.strip());
            }
            if (type == LocalDate.class) {
                if (format != null && !format.pattern().isEmpty()) {
                    return LocalDate.parse(raw.strip(),
                            DateTimeFormatter.ofPattern(format.pattern()));
                }
                return LocalDate.parse(raw.strip());
            }
            if (type == LocalDateTime.class) {
                if (format != null && !format.pattern().isEmpty()) {
                    return LocalDateTime.parse(raw.strip(),
                            DateTimeFormatter.ofPattern(format.pattern()));
                }
                return LocalDateTime.parse(raw.strip());
            }
            if (type.isEnum()) {
                return convertEnum(type, raw.strip());
            }

            throw new CsvBindException(
                    "Unsupported type: " + type.getName() + " for field '" + fieldName + "'",
                    ownerType, fieldName);
        } catch (CsvBindException e) {
            throw e;
        } catch (Exception e) {
            throw new CsvBindException(
                    "Failed to convert '" + raw + "' to " + type.getSimpleName()
                            + " for field '" + fieldName + "': " + e.getMessage(),
                    ownerType, fieldName, e);
        }
    }

    /**
     * Gets the default value for a primitive type
     */
    private static Object defaultValue(Class<?> type) {
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

    /**
     * Converts a string to an enum constant (case-insensitive)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object convertEnum(Class<?> enumType, String value) {
        Object[] constants = enumType.getEnumConstants();
        for (Object c : constants) {
            if (((Enum) c).name().equalsIgnoreCase(value)) {
                return c;
            }
        }
        return Enum.valueOf((Class<Enum>) enumType, value);
    }

    /**
     * Formats a value to string with optional format pattern
     * 将值格式化为字符串，可选格式模式
     */
    public static String formatValue(Object value, CsvFormat format) {
        if (value == null) {
            if (format != null && !format.nullValue().isEmpty()) {
                return format.nullValue();
            }
            return "";
        }
        if (value instanceof LocalDate date) {
            if (format != null && !format.pattern().isEmpty()) {
                return date.format(DateTimeFormatter.ofPattern(format.pattern()));
            }
            return date.toString();
        }
        if (value instanceof LocalDateTime dateTime) {
            if (format != null && !format.pattern().isEmpty()) {
                return dateTime.format(DateTimeFormatter.ofPattern(format.pattern()));
            }
            return dateTime.toString();
        }
        if (value instanceof Enum<?> e) {
            return e.name();
        }
        return String.valueOf(value);
    }

    /**
     * Checks if a string is blank (null, empty, or whitespace only)
     */
    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
