package cloud.opencode.base.expression.function;

import cloud.opencode.base.expression.OpenExpressionException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * String Functions
 * 字符串函数
 *
 * <p>Provides built-in string manipulation functions for expressions.</p>
 * <p>为表达式提供内置的字符串操作函数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Case conversion: upper, lower, capitalize - 大小写转换</li>
 *   <li>Trimming: trim, ltrim, rtrim - 修剪</li>
 *   <li>Search: contains, startswith, endswith, indexof - 搜索</li>
 *   <li>Manipulation: replace, split, join, concat, pad, repeat, reverse - 操作</li>
 *   <li>Validation: isempty, isblank, matches (regex) - 验证</li>
 *   <li>Formatting: format, substring - 格式化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Object result = OpenExpression.eval("upper('hello')");  // "HELLO"
 * Object trimmed = OpenExpression.eval("trim('  hi  ')");  // "hi"
 * Object has = OpenExpression.eval("contains('hello', 'ell')");  // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, stateless utility class - 线程安全: 是，无状态工具类</li>
 *   <li>Null-safe: Yes, null arguments return sensible defaults - 空值安全: 是，null参数返回合理默认值</li>
 *   <li>ReDoS protection: regex operations have timeout and length limits - ReDoS防护: 正则操作有超时和长度限制</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public final class StringFunctions {

    /**
     * Maximum allowed regex pattern length to prevent ReDoS.
     */
    private static final int MAX_PATTERN_LENGTH = 1000;

    /**
     * Timeout for regex operations in seconds.
     */
    private static final long REGEX_TIMEOUT_SECONDS = 5;

    private StringFunctions() {
    }

    /**
     * Get all string functions
     * 获取所有字符串函数
     *
     * @return the function map | 函数映射
     */
    public static Map<String, Function> getFunctions() {
        Map<String, Function> functions = new LinkedHashMap<>();

        // Length
        functions.put("len", args -> {
            if (args.length < 1 || args[0] == null) return 0;
            return args[0].toString().length();
        });

        functions.put("length", args -> {
            if (args.length < 1 || args[0] == null) return 0;
            return args[0].toString().length();
        });

        // Case conversion
        functions.put("upper", args -> {
            if (args.length < 1 || args[0] == null) return null;
            return args[0].toString().toUpperCase();
        });

        functions.put("lower", args -> {
            if (args.length < 1 || args[0] == null) return null;
            return args[0].toString().toLowerCase();
        });

        functions.put("capitalize", args -> {
            if (args.length < 1 || args[0] == null) return null;
            String s = args[0].toString();
            if (s.isEmpty()) return s;
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        });

        // Trimming
        functions.put("trim", args -> {
            if (args.length < 1 || args[0] == null) return null;
            return args[0].toString().trim();
        });

        functions.put("ltrim", args -> {
            if (args.length < 1 || args[0] == null) return null;
            return args[0].toString().stripLeading();
        });

        functions.put("rtrim", args -> {
            if (args.length < 1 || args[0] == null) return null;
            return args[0].toString().stripTrailing();
        });

        // Substring
        functions.put("substring", args -> {
            if (args.length < 2 || args[0] == null) return null;
            String s = args[0].toString();
            int start = toInt(args[1]);
            if (args.length >= 3) {
                int end = toInt(args[2]);
                return s.substring(Math.max(0, start), Math.min(s.length(), end));
            }
            return s.substring(Math.max(0, Math.min(start, s.length())));
        });

        functions.put("substr", functions.get("substring"));

        // Contains/StartsWith/EndsWith
        functions.put("contains", args -> {
            if (args.length < 2 || args[0] == null) return false;
            return args[0].toString().contains(toString(args[1]));
        });

        functions.put("startswith", args -> {
            if (args.length < 2 || args[0] == null) return false;
            return args[0].toString().startsWith(toString(args[1]));
        });

        functions.put("endswith", args -> {
            if (args.length < 2 || args[0] == null) return false;
            return args[0].toString().endsWith(toString(args[1]));
        });

        // Index
        functions.put("indexof", args -> {
            if (args.length < 2 || args[0] == null) return -1;
            return args[0].toString().indexOf(toString(args[1]));
        });

        functions.put("lastindexof", args -> {
            if (args.length < 2 || args[0] == null) return -1;
            return args[0].toString().lastIndexOf(toString(args[1]));
        });

        // Replace
        functions.put("replace", args -> {
            if (args.length < 3 || args[0] == null) return args.length > 0 ? args[0] : null;
            return args[0].toString().replace(toString(args[1]), toString(args[2]));
        });

        functions.put("replaceall", args -> {
            if (args.length < 3 || args[0] == null) return args.length > 0 ? args[0] : null;
            String pattern = toString(args[1]);
            validateRegex(pattern);
            return safeRegexOp(() -> args[0].toString().replaceAll(pattern, toString(args[2])));
        });

        functions.put("replacefirst", args -> {
            if (args.length < 3 || args[0] == null) return args.length > 0 ? args[0] : null;
            String pattern = toString(args[1]);
            validateRegex(pattern);
            return safeRegexOp(() -> args[0].toString().replaceFirst(pattern, toString(args[2])));
        });

        // Split/Join
        functions.put("split", args -> {
            if (args.length < 2 || args[0] == null) return new String[0];
            String pattern = toString(args[1]);
            validateRegex(pattern);
            return safeRegexOp(() -> args[0].toString().split(pattern));
        });

        functions.put("join", args -> {
            if (args.length < 2) return "";
            String delimiter = toString(args[0]);
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) sb.append(delimiter);
                sb.append(toString(args[i]));
            }
            return sb.toString();
        });

        // Concat
        functions.put("concat", args -> {
            StringBuilder sb = new StringBuilder();
            for (Object arg : args) {
                sb.append(toString(arg));
            }
            return sb.toString();
        });

        // Padding
        functions.put("lpad", args -> {
            if (args.length < 2 || args[0] == null) return args.length > 0 ? args[0] : null;
            String s = args[0].toString();
            int length = toInt(args[1]);
            String pad = args.length >= 3 ? toString(args[2]) : " ";
            if (pad.isEmpty()) pad = " ";
            while (s.length() < length) {
                s = pad + s;
            }
            return s.length() > length ? s.substring(s.length() - length) : s;
        });

        functions.put("rpad", args -> {
            if (args.length < 2 || args[0] == null) return args.length > 0 ? args[0] : null;
            String s = args[0].toString();
            int length = toInt(args[1]);
            String pad = args.length >= 3 ? toString(args[2]) : " ";
            if (pad.isEmpty()) pad = " ";
            while (s.length() < length) {
                s = s + pad;
            }
            return s.length() > length ? s.substring(0, length) : s;
        });

        // Repeat
        functions.put("repeat", args -> {
            if (args.length < 2 || args[0] == null) return "";
            return args[0].toString().repeat(Math.max(0, toInt(args[1])));
        });

        // Reverse
        functions.put("reverse", args -> {
            if (args.length < 1 || args[0] == null) return null;
            return new StringBuilder(args[0].toString()).reverse().toString();
        });

        // Regex match
        functions.put("matches", args -> {
            if (args.length < 2 || args[0] == null) return false;
            String pattern = toString(args[1]);
            validateRegex(pattern);
            return safeRegexOp(() -> Pattern.matches(pattern, args[0].toString()));
        });

        // Empty/Blank check
        functions.put("isempty", args -> {
            if (args.length < 1 || args[0] == null) return true;
            return args[0].toString().isEmpty();
        });

        functions.put("isblank", args -> {
            if (args.length < 1 || args[0] == null) return true;
            return args[0].toString().isBlank();
        });

        functions.put("isnotempty", args -> {
            if (args.length < 1 || args[0] == null) return false;
            return !args[0].toString().isEmpty();
        });

        functions.put("isnotblank", args -> {
            if (args.length < 1 || args[0] == null) return false;
            return !args[0].toString().isBlank();
        });

        // Format
        functions.put("format", args -> {
            if (args.length < 1) return "";
            String format = toString(args[0]);
            Object[] formatArgs = new Object[args.length - 1];
            System.arraycopy(args, 1, formatArgs, 0, formatArgs.length);
            return String.format(format, formatArgs);
        });

        return functions;
    }

    private static String toString(Object value) {
        return value == null ? "" : value.toString();
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

    /**
     * Validates a regex pattern for length and syntax before use.
     */
    private static void validateRegex(String pattern) {
        if (pattern.length() > MAX_PATTERN_LENGTH) {
            throw OpenExpressionException.evaluationError(
                "Regex pattern exceeds maximum length of " + MAX_PATTERN_LENGTH + " characters");
        }
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            throw OpenExpressionException.evaluationError("Invalid regex pattern: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a regex operation with a timeout to prevent ReDoS.
     */
    @SuppressWarnings("unchecked")
    private static <T> T safeRegexOp(java.util.concurrent.Callable<T> operation) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return operation.call();
                } catch (StackOverflowError e) {
                    throw OpenExpressionException.evaluationError(
                        "Regex pattern caused stack overflow (possible ReDoS)", e);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).get(REGEX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw OpenExpressionException.timeout(REGEX_TIMEOUT_SECONDS * 1000);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof OpenExpressionException oee) {
                throw oee;
            }
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw OpenExpressionException.evaluationError(
                "Regex operation failed: " + e.getCause().getMessage(), e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenExpressionException.evaluationError("Regex operation was interrupted", e);
        }
    }
}
