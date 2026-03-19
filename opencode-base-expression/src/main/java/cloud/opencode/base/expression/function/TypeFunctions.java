package cloud.opencode.base.expression.function;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Type Functions
 * 类型函数
 *
 * <p>Provides built-in type checking and conversion functions for expressions.</p>
 * <p>为表达式提供内置的类型检查和转换函数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type checking: isnull, isnumber, isstring, isboolean, islist, ismap, isdate, etc. - 类型检查</li>
 *   <li>Type conversion: toint, tolong, todouble, tostring, toboolean, tolist, toset - 类型转换</li>
 *   <li>Type info: typeof, classname, simpleclassname - 类型信息</li>
 *   <li>Default values: nvl, coalesce, defaultifnull, defaultifempty, defaultifblank - 默认值</li>
 *   <li>Type matching: istype - 类型匹配</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Object check = OpenExpression.eval("isnull(null)");  // true
 * Object type = OpenExpression.eval("typeof(42)");  // "integer"
 * Object def = OpenExpression.eval("nvl(null, 'default')");  // "default"
 * Object conv = OpenExpression.eval("toint('42')");  // 42
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, stateless utility class - 线程安全: 是，无状态工具类</li>
 *   <li>Null-safe: Yes, null arguments return appropriate defaults - 空值安全: 是，null参数返回适当默认值</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public final class TypeFunctions {

    private TypeFunctions() {
    }

    /**
     * Get all type functions
     * 获取所有类型函数
     *
     * @return the function map | 函数映射
     */
    public static Map<String, Function> getFunctions() {
        Map<String, Function> functions = new LinkedHashMap<>();

        // ==================== Type Checking ====================

        // isNull
        functions.put("isnull", args -> {
            if (args.length < 1) return true;
            return args[0] == null;
        });

        // isNotNull
        functions.put("isnotnull", args -> {
            if (args.length < 1) return false;
            return args[0] != null;
        });

        // isNumber
        functions.put("isnumber", args -> {
            if (args.length < 1 || args[0] == null) return false;
            return args[0] instanceof Number;
        });

        // isString
        functions.put("isstring", args -> {
            if (args.length < 1 || args[0] == null) return false;
            return args[0] instanceof String;
        });

        // isBoolean
        functions.put("isboolean", args -> {
            if (args.length < 1 || args[0] == null) return false;
            return args[0] instanceof Boolean;
        });

        // isArray
        functions.put("isarray", args -> {
            if (args.length < 1 || args[0] == null) return false;
            return args[0].getClass().isArray();
        });

        // isList
        functions.put("islist", args -> {
            if (args.length < 1 || args[0] == null) return false;
            return args[0] instanceof List;
        });

        // isMap
        functions.put("ismap", args -> {
            if (args.length < 1 || args[0] == null) return false;
            return args[0] instanceof Map;
        });

        // isCollection
        functions.put("iscollection", args -> {
            if (args.length < 1 || args[0] == null) return false;
            return args[0] instanceof Collection;
        });

        // isDate
        functions.put("isdate", args -> {
            if (args.length < 1 || args[0] == null) return false;
            return args[0] instanceof LocalDate ||
                   args[0] instanceof LocalDateTime ||
                   args[0] instanceof java.util.Date;
        });

        // isInteger
        functions.put("isinteger", args -> {
            if (args.length < 1 || args[0] == null) return false;
            return args[0] instanceof Integer ||
                   args[0] instanceof Long ||
                   args[0] instanceof Short ||
                   args[0] instanceof Byte ||
                   args[0] instanceof BigInteger;
        });

        // isDecimal
        functions.put("isdecimal", args -> {
            if (args.length < 1 || args[0] == null) return false;
            return args[0] instanceof Double ||
                   args[0] instanceof Float ||
                   args[0] instanceof BigDecimal;
        });

        // ==================== Type Conversion ====================

        // toInt / toInteger
        functions.put("toint", args -> {
            if (args.length < 1 || args[0] == null) return 0;
            Object val = args[0];
            if (val instanceof Number n) return n.intValue();
            if (val instanceof String s) {
                try {
                    return Integer.parseInt(s.trim());
                } catch (NumberFormatException e) {
                    try {
                        return (int) Double.parseDouble(s.trim());
                    } catch (NumberFormatException e2) {
                        return 0;
                    }
                }
            }
            if (val instanceof Boolean b) return b ? 1 : 0;
            return 0;
        });
        functions.put("tointeger", functions.get("toint"));

        // toLong
        functions.put("tolong", args -> {
            if (args.length < 1 || args[0] == null) return 0L;
            Object val = args[0];
            if (val instanceof Number n) return n.longValue();
            if (val instanceof String s) {
                try {
                    return Long.parseLong(s.trim());
                } catch (NumberFormatException e) {
                    try {
                        return (long) Double.parseDouble(s.trim());
                    } catch (NumberFormatException e2) {
                        return 0L;
                    }
                }
            }
            if (val instanceof Boolean b) return b ? 1L : 0L;
            return 0L;
        });

        // toDouble
        functions.put("todouble", args -> {
            if (args.length < 1 || args[0] == null) return 0.0;
            Object val = args[0];
            if (val instanceof Number n) return n.doubleValue();
            if (val instanceof String s) {
                try {
                    return Double.parseDouble(s.trim());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            if (val instanceof Boolean b) return b ? 1.0 : 0.0;
            return 0.0;
        });

        // toFloat
        functions.put("tofloat", args -> {
            if (args.length < 1 || args[0] == null) return 0.0f;
            Object val = args[0];
            if (val instanceof Number n) return n.floatValue();
            if (val instanceof String s) {
                try {
                    return Float.parseFloat(s.trim());
                } catch (NumberFormatException e) {
                    return 0.0f;
                }
            }
            if (val instanceof Boolean b) return b ? 1.0f : 0.0f;
            return 0.0f;
        });

        // toString
        functions.put("tostring", args -> {
            if (args.length < 1 || args[0] == null) return "";
            return args[0].toString();
        });

        // toBoolean
        functions.put("toboolean", args -> {
            if (args.length < 1 || args[0] == null) return false;
            Object val = args[0];
            if (val instanceof Boolean b) return b;
            if (val instanceof Number n) return n.doubleValue() != 0;
            if (val instanceof String s) {
                s = s.trim().toLowerCase();
                return !s.isEmpty() && !s.equals("false") && !s.equals("0") && !s.equals("no");
            }
            return true;
        });
        functions.put("tobool", functions.get("toboolean"));

        // toList
        functions.put("tolist", args -> {
            if (args.length < 1 || args[0] == null) return new ArrayList<>();
            Object val = args[0];
            if (val instanceof List<?> l) return new ArrayList<>(l);
            if (val instanceof Collection<?> c) return new ArrayList<>(c);
            if (val.getClass().isArray()) {
                List<Object> list = new ArrayList<>();
                int len = java.lang.reflect.Array.getLength(val);
                for (int i = 0; i < len; i++) {
                    list.add(java.lang.reflect.Array.get(val, i));
                }
                return list;
            }
            return List.of(val);
        });

        // toSet
        functions.put("toset", args -> {
            if (args.length < 1 || args[0] == null) return new HashSet<>();
            Object val = args[0];
            if (val instanceof Set<?> s) return new HashSet<>(s);
            if (val instanceof Collection<?> c) return new HashSet<>(c);
            if (val.getClass().isArray()) {
                Set<Object> set = new LinkedHashSet<>();
                int len = java.lang.reflect.Array.getLength(val);
                for (int i = 0; i < len; i++) {
                    set.add(java.lang.reflect.Array.get(val, i));
                }
                return set;
            }
            return Set.of(val);
        });

        // ==================== Type Info ====================

        // typeof
        functions.put("typeof", args -> {
            if (args.length < 1 || args[0] == null) return "null";
            Object val = args[0];
            if (val instanceof String) return "string";
            if (val instanceof Integer || val instanceof Long ||
                val instanceof Short || val instanceof Byte) return "integer";
            if (val instanceof Double || val instanceof Float) return "decimal";
            if (val instanceof Number) return "number";
            if (val instanceof Boolean) return "boolean";
            if (val instanceof List) return "list";
            if (val instanceof Map) return "map";
            if (val instanceof Set) return "set";
            if (val instanceof Collection) return "collection";
            if (val.getClass().isArray()) return "array";
            if (val instanceof LocalDate) return "date";
            if (val instanceof LocalDateTime) return "datetime";
            if (val instanceof LocalTime) return "time";
            return val.getClass().getSimpleName().toLowerCase();
        });

        // classname
        functions.put("classname", args -> {
            if (args.length < 1 || args[0] == null) return "null";
            return args[0].getClass().getName();
        });

        // simpleclassname
        functions.put("simpleclassname", args -> {
            if (args.length < 1 || args[0] == null) return "null";
            return args[0].getClass().getSimpleName();
        });

        // ==================== Default Values ====================

        // nvl / coalesce - return first non-null value
        functions.put("nvl", args -> {
            for (Object arg : args) {
                if (arg != null) return arg;
            }
            return null;
        });
        functions.put("coalesce", functions.get("nvl"));

        // defaultIfNull
        functions.put("defaultifnull", args -> {
            if (args.length < 2) return args.length > 0 ? args[0] : null;
            return args[0] != null ? args[0] : args[1];
        });

        // defaultIfEmpty - for strings/collections
        functions.put("defaultifempty", args -> {
            if (args.length < 2) return args.length > 0 ? args[0] : null;
            Object val = args[0];
            if (val == null) return args[1];
            if (val instanceof String s && s.isEmpty()) return args[1];
            if (val instanceof Collection<?> c && c.isEmpty()) return args[1];
            if (val instanceof Map<?, ?> m && m.isEmpty()) return args[1];
            return val;
        });

        // defaultIfBlank - for strings
        functions.put("defaultifblank", args -> {
            if (args.length < 2) return args.length > 0 ? args[0] : null;
            Object val = args[0];
            if (val == null) return args[1];
            if (val instanceof String s && s.isBlank()) return args[1];
            return val;
        });

        // ==================== Type Check ====================

        // istype (simplified, checks common types) - use istype instead of instanceof to avoid keyword conflict
        functions.put("istype", args -> {
            if (args.length < 2 || args[0] == null || args[1] == null) return false;
            String typeName = args[1].toString().toLowerCase();
            Object val = args[0];
            return switch (typeName) {
                case "string" -> val instanceof String;
                case "number" -> val instanceof Number;
                case "integer", "int", "long" -> val instanceof Integer || val instanceof Long;
                case "double", "float", "decimal" -> val instanceof Double || val instanceof Float;
                case "boolean", "bool" -> val instanceof Boolean;
                case "list" -> val instanceof List;
                case "map" -> val instanceof Map;
                case "set" -> val instanceof Set;
                case "collection" -> val instanceof Collection;
                case "array" -> val.getClass().isArray();
                case "date" -> val instanceof LocalDate || val instanceof java.util.Date;
                case "datetime" -> val instanceof LocalDateTime;
                default -> false;
            };
        });

        return functions;
    }
}
