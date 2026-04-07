package cloud.opencode.base.expression.function;

import cloud.opencode.base.expression.OpenExpressionException;

import java.util.*;

/**
 * Collection Functions
 * 集合函数
 *
 * <p>Provides built-in collection manipulation functions for expressions.</p>
 * <p>为表达式提供内置的集合操作函数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Size/empty checks: size, count, empty, notempty - 大小/空检查</li>
 *   <li>Access: first, last, get, sublist, take, skip - 访问</li>
 *   <li>Transform: sort, distinct, reverselist, flatten - 转换</li>
 *   <li>Search: containskey, containsvalue - 搜索</li>
 *   <li>Map operations: keys, values, entries - Map操作</li>
 *   <li>Creation: list, listof, setof, range - 创建</li>
 *   <li>Aggregation: sumlist, avglist, minlist, maxlist - 聚合</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Object s = OpenExpression.eval("size({1, 2, 3})");  // 3
 * Object f = OpenExpression.eval("first({10, 20, 30})");  // 10
 * Object r = OpenExpression.eval("sort({3, 1, 2})");  // [1, 2, 3]
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, stateless utility class - 线程安全: 是，无状态工具类</li>
 *   <li>Null-safe: Yes, null arguments return sensible defaults - 空值安全: 是，null参数返回合理默认值</li>
 *   <li>Flatten depth limited to 100 to prevent stack overflow - 展平深度限制为100以防止栈溢出</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public final class CollectionFunctions {

    private static final int MAX_FLATTEN_DEPTH = 100;

    private CollectionFunctions() {
    }

    /**
     * Get all collection functions
     * 获取所有集合函数
     *
     * @return the function map | 函数映射
     */
    public static Map<String, Function> getFunctions() {
        Map<String, Function> functions = new LinkedHashMap<>();

        // Size/Count
        functions.put("size", args -> {
            if (args.length < 1 || args[0] == null) return 0;
            Object obj = args[0];
            if (obj instanceof Collection<?> c) return c.size();
            if (obj instanceof Map<?, ?> m) return m.size();
            if (obj instanceof Object[] arr) return arr.length;
            if (obj instanceof String s) return s.length();
            return 1;
        });

        functions.put("count", functions.get("size"));

        // Empty check
        functions.put("empty", args -> {
            if (args.length < 1 || args[0] == null) return true;
            Object obj = args[0];
            if (obj instanceof Collection<?> c) return c.isEmpty();
            if (obj instanceof Map<?, ?> m) return m.isEmpty();
            if (obj instanceof Object[] arr) return arr.length == 0;
            if (obj instanceof String s) return s.isEmpty();
            return false;
        });

        functions.put("notempty", args -> {
            if (args.length < 1 || args[0] == null) return false;
            Object obj = args[0];
            if (obj instanceof Collection<?> c) return !c.isEmpty();
            if (obj instanceof Map<?, ?> m) return !m.isEmpty();
            if (obj instanceof Object[] arr) return arr.length > 0;
            if (obj instanceof String s) return !s.isEmpty();
            return true;
        });

        // First/Last
        functions.put("first", args -> {
            if (args.length < 1 || args[0] == null) return null;
            Object obj = args[0];
            if (obj instanceof List<?> list) return list.isEmpty() ? null : list.getFirst();
            if (obj instanceof Collection<?> c) return c.stream().findFirst().orElse(null);
            if (obj instanceof Object[] arr) return arr.length > 0 ? arr[0] : null;
            return obj;
        });

        functions.put("last", args -> {
            if (args.length < 1 || args[0] == null) return null;
            Object obj = args[0];
            if (obj instanceof List<?> list) return list.isEmpty() ? null : list.getLast();
            if (obj instanceof Collection<?> c) {
                Object last = null;
                for (Object item : c) last = item;
                return last;
            }
            if (obj instanceof Object[] arr) return arr.length > 0 ? arr[arr.length - 1] : null;
            return obj;
        });

        // Contains
        functions.put("containskey", args -> {
            if (args.length < 2 || args[0] == null) return false;
            Object obj = args[0];
            if (obj instanceof Map<?, ?> m) return m.containsKey(args[1]);
            return false;
        });

        functions.put("containsvalue", args -> {
            if (args.length < 2 || args[0] == null) return false;
            Object obj = args[0];
            if (obj instanceof Collection<?> c) return c.contains(args[1]);
            if (obj instanceof Map<?, ?> m) return m.containsValue(args[1]);
            if (obj instanceof Object[] arr) {
                for (Object item : arr) {
                    if (Objects.equals(item, args[1])) return true;
                }
                return false;
            }
            return Objects.equals(obj, args[1]);
        });

        // Get by index/key
        functions.put("get", args -> {
            if (args.length < 2 || args[0] == null) return null;
            Object obj = args[0];
            Object key = args[1];
            if (obj instanceof Map<?, ?> m) return m.get(key);
            if (obj instanceof List<?> list) {
                int index = toInt(key);
                return (index >= 0 && index < list.size()) ? list.get(index) : null;
            }
            if (obj instanceof Object[] arr) {
                int index = toInt(key);
                return (index >= 0 && index < arr.length) ? arr[index] : null;
            }
            return null;
        });

        // Sublist
        functions.put("sublist", args -> {
            if (args.length < 2 || args[0] == null) return List.of();
            Object obj = args[0];
            int from = toInt(args[1]);
            int to = args.length >= 3 ? toInt(args[2]) : Integer.MAX_VALUE;

            if (obj instanceof List<?> list) {
                from = Math.max(0, from);
                to = Math.min(list.size(), to);
                return list.subList(from, to);
            }
            if (obj instanceof Object[] arr) {
                from = Math.max(0, from);
                to = Math.min(arr.length, to);
                return Arrays.asList(arr).subList(from, to);
            }
            return List.of();
        });

        // Take/Skip
        functions.put("take", args -> {
            if (args.length < 2 || args[0] == null) return List.of();
            int n = toInt(args[1]);
            return toList(args[0]).stream().limit(n).toList();
        });

        functions.put("skip", args -> {
            if (args.length < 2 || args[0] == null) return List.of();
            int n = toInt(args[1]);
            return toList(args[0]).stream().skip(n).toList();
        });

        // Distinct
        functions.put("distinct", args -> {
            if (args.length < 1 || args[0] == null) return List.of();
            return toList(args[0]).stream().distinct().toList();
        });

        // Sort
        functions.put("sort", args -> {
            if (args.length < 1 || args[0] == null) return List.of();
            List<Object> list = new ArrayList<>(toList(args[0]));
            list.sort((a, b) -> {
                if (a == null && b == null) return 0;
                if (a == null) return -1;
                if (b == null) return 1;
                if (a instanceof Comparable && b instanceof Comparable) {
                    @SuppressWarnings("unchecked")
                    Comparable<Object> ca = (Comparable<Object>) a;
                    return ca.compareTo(b);
                }
                return a.toString().compareTo(b.toString());
            });
            return list;
        });

        // Reverse
        functions.put("reverselist", args -> {
            if (args.length < 1 || args[0] == null) return List.of();
            List<Object> list = new ArrayList<>(toList(args[0]));
            Collections.reverse(list);
            return list;
        });

        // Flatten
        functions.put("flatten", args -> {
            if (args.length < 1 || args[0] == null) return List.of();
            List<Object> result = new ArrayList<>();
            flatten(args[0], result, 0);
            return result;
        });

        // Keys/Values
        functions.put("keys", args -> {
            if (args.length < 1 || args[0] == null) return List.of();
            Object obj = args[0];
            if (obj instanceof Map<?, ?> m) return new ArrayList<>(m.keySet());
            return List.of();
        });

        functions.put("values", args -> {
            if (args.length < 1 || args[0] == null) return List.of();
            Object obj = args[0];
            if (obj instanceof Map<?, ?> m) return new ArrayList<>(m.values());
            return List.of();
        });

        functions.put("entries", args -> {
            if (args.length < 1 || args[0] == null) return List.of();
            Object obj = args[0];
            if (obj instanceof Map<?, ?> m) return new ArrayList<>(m.entrySet());
            return List.of();
        });

        // List creation
        functions.put("list", args -> Arrays.asList(args));

        functions.put("listof", args -> List.of(args));

        functions.put("setof", args -> Set.of(args));

        // Range
        functions.put("range", args -> {
            if (args.length < 1) return List.of();
            int start = 0;
            int end;
            int step = 1;

            if (args.length == 1) {
                end = toInt(args[0]);
            } else if (args.length == 2) {
                start = toInt(args[0]);
                end = toInt(args[1]);
            } else {
                start = toInt(args[0]);
                end = toInt(args[1]);
                step = toInt(args[2]);
                if (step == 0) step = 1;
            }

            // Prevent unbounded list creation
            long count = (step > 0 && end > start) ? ((long) end - start + step - 1) / step
                    : (step < 0 && start > end) ? ((long) start - end - step - 1) / -step
                    : 0;
            if (count > 1_000_000) {
                throw OpenExpressionException.evaluationError(
                        "Range produces " + count + " elements, exceeds limit of 1000000");
            }

            List<Integer> result = new ArrayList<>((int) Math.min(count, 1_000_000));
            if (step > 0) {
                for (int i = start; i < end; i += step) {
                    result.add(i);
                }
            } else {
                for (int i = start; i > end; i += step) {
                    result.add(i);
                }
            }
            return result;
        });

        // Sum/Avg for numeric collections
        functions.put("sumlist", args -> {
            if (args.length < 1 || args[0] == null) return 0.0;
            double sum = 0;
            for (Object item : toList(args[0])) {
                sum += toDouble(item);
            }
            return sum;
        });

        functions.put("avglist", args -> {
            if (args.length < 1 || args[0] == null) return 0.0;
            List<?> list = toList(args[0]);
            if (list.isEmpty()) return 0.0;
            double sum = 0;
            for (Object item : list) {
                sum += toDouble(item);
            }
            return sum / list.size();
        });

        functions.put("minlist", args -> {
            if (args.length < 1 || args[0] == null) return null;
            List<?> list = toList(args[0]);
            if (list.isEmpty()) return null;
            double min = Double.MAX_VALUE;
            for (Object item : list) {
                min = Math.min(min, toDouble(item));
            }
            return min;
        });

        functions.put("maxlist", args -> {
            if (args.length < 1 || args[0] == null) return null;
            List<?> list = toList(args[0]);
            if (list.isEmpty()) return null;
            double max = -Double.MAX_VALUE;
            for (Object item : list) {
                max = Math.max(max, toDouble(item));
            }
            return max;
        });

        return functions;
    }

    private static List<?> toList(Object obj) {
        if (obj == null) return List.of();
        if (obj instanceof List<?> list) return list;
        if (obj instanceof Collection<?> c) return new ArrayList<>(c);
        if (obj instanceof Object[] arr) return Arrays.asList(arr);
        return List.of(obj);
    }

    private static void flatten(Object obj, List<Object> result, int depth) {
        if (obj == null) return;
        if (depth > MAX_FLATTEN_DEPTH) {
            throw new IllegalStateException("Flatten depth exceeded maximum of " + MAX_FLATTEN_DEPTH);
        }
        if (obj instanceof Collection<?> c) {
            for (Object item : c) {
                flatten(item, result, depth + 1);
            }
        } else if (obj instanceof Object[] arr) {
            for (Object item : arr) {
                flatten(item, result, depth + 1);
            }
        } else {
            result.add(obj);
        }
    }

    private static int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double toDouble(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
