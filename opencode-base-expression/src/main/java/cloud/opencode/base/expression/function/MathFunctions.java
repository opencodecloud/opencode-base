package cloud.opencode.base.expression.function;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Math Functions
 * 数学函数
 *
 * <p>Provides built-in mathematical functions for expressions.</p>
 * <p>为表达式提供内置的数学函数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Basic: abs, neg, ceil, floor, round, trunc, min, max, clamp - 基本运算</li>
 *   <li>Power/root: pow, sqrt, cbrt, exp, log, log10, log2 - 幂/根运算</li>
 *   <li>Trigonometric: sin, cos, tan, asin, acos, atan, atan2 - 三角函数</li>
 *   <li>Conversion: todegrees, toradians, int, long, double, decimal - 转换</li>
 *   <li>Aggregate: sum, avg - 聚合</li>
 *   <li>Random: random, randomint - 随机数</li>
 *   <li>Constants: pi, e - 常量</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Object result = OpenExpression.eval("round(3.14159, 2)");  // 3.14
 * Object max = OpenExpression.eval("max(10, 20, 30)");  // 30.0
 * Object sq = OpenExpression.eval("sqrt(144)");  // 12.0
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, stateless utility class with ThreadLocalRandom - 线程安全: 是，使用ThreadLocalRandom的无状态工具类</li>
 *   <li>Null-safe: Yes, null arguments treated as 0 - 空值安全: 是，null参数视为0</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public final class MathFunctions {

    private MathFunctions() {
    }

    /**
     * Get all math functions
     * 获取所有数学函数
     *
     * @return the function map | 函数映射
     */
    public static Map<String, Function> getFunctions() {
        Map<String, Function> functions = new LinkedHashMap<>();

        // Basic arithmetic
        functions.put("abs", args -> {
            if (args.length < 1) return 0;
            double d = toDouble(args[0]);
            return Math.abs(d);
        });

        functions.put("neg", args -> {
            if (args.length < 1) return 0;
            return -toDouble(args[0]);
        });

        // Rounding
        functions.put("ceil", args -> {
            if (args.length < 1) return 0;
            return Math.ceil(toDouble(args[0]));
        });

        functions.put("floor", args -> {
            if (args.length < 1) return 0;
            return Math.floor(toDouble(args[0]));
        });

        functions.put("round", args -> {
            if (args.length < 1) return 0;
            double d = toDouble(args[0]);
            if (args.length >= 2) {
                int scale = toInt(args[1]);
                return BigDecimal.valueOf(d).setScale(scale, RoundingMode.HALF_UP).doubleValue();
            }
            return Math.round(d);
        });

        functions.put("trunc", args -> {
            if (args.length < 1) return 0;
            double d = toDouble(args[0]);
            return d < 0 ? Math.ceil(d) : Math.floor(d);
        });

        // Min/Max
        functions.put("min", args -> {
            if (args.length < 1) return null;
            double min = toDouble(args[0]);
            for (int i = 1; i < args.length; i++) {
                min = Math.min(min, toDouble(args[i]));
            }
            return min;
        });

        functions.put("max", args -> {
            if (args.length < 1) return null;
            double max = toDouble(args[0]);
            for (int i = 1; i < args.length; i++) {
                max = Math.max(max, toDouble(args[i]));
            }
            return max;
        });

        // Power/Root
        functions.put("pow", args -> {
            if (args.length < 2) return 0;
            return Math.pow(toDouble(args[0]), toDouble(args[1]));
        });

        functions.put("sqrt", args -> {
            if (args.length < 1) return 0;
            return Math.sqrt(toDouble(args[0]));
        });

        functions.put("cbrt", args -> {
            if (args.length < 1) return 0;
            return Math.cbrt(toDouble(args[0]));
        });

        // Exponential/Logarithmic
        functions.put("exp", args -> {
            if (args.length < 1) return 0;
            return Math.exp(toDouble(args[0]));
        });

        functions.put("log", args -> {
            if (args.length < 1) return 0;
            return Math.log(toDouble(args[0]));
        });

        functions.put("log10", args -> {
            if (args.length < 1) return 0;
            return Math.log10(toDouble(args[0]));
        });

        functions.put("log2", args -> {
            if (args.length < 1) return 0;
            return Math.log(toDouble(args[0])) / Math.log(2);
        });

        // Trigonometric
        functions.put("sin", args -> {
            if (args.length < 1) return 0;
            return Math.sin(toDouble(args[0]));
        });

        functions.put("cos", args -> {
            if (args.length < 1) return 0;
            return Math.cos(toDouble(args[0]));
        });

        functions.put("tan", args -> {
            if (args.length < 1) return 0;
            return Math.tan(toDouble(args[0]));
        });

        functions.put("asin", args -> {
            if (args.length < 1) return 0;
            return Math.asin(toDouble(args[0]));
        });

        functions.put("acos", args -> {
            if (args.length < 1) return 0;
            return Math.acos(toDouble(args[0]));
        });

        functions.put("atan", args -> {
            if (args.length < 1) return 0;
            return Math.atan(toDouble(args[0]));
        });

        functions.put("atan2", args -> {
            if (args.length < 2) return 0;
            return Math.atan2(toDouble(args[0]), toDouble(args[1]));
        });

        // Hyperbolic
        functions.put("sinh", args -> {
            if (args.length < 1) return 0;
            return Math.sinh(toDouble(args[0]));
        });

        functions.put("cosh", args -> {
            if (args.length < 1) return 0;
            return Math.cosh(toDouble(args[0]));
        });

        functions.put("tanh", args -> {
            if (args.length < 1) return 0;
            return Math.tanh(toDouble(args[0]));
        });

        // Conversion
        functions.put("todegrees", args -> {
            if (args.length < 1) return 0;
            return Math.toDegrees(toDouble(args[0]));
        });

        functions.put("toradians", args -> {
            if (args.length < 1) return 0;
            return Math.toRadians(toDouble(args[0]));
        });

        // Sign
        functions.put("sign", args -> {
            if (args.length < 1) return 0;
            return Math.signum(toDouble(args[0]));
        });

        // Random
        functions.put("random", args -> {
            if (args.length >= 2) {
                double min = toDouble(args[0]);
                double max = toDouble(args[1]);
                return min + ThreadLocalRandom.current().nextDouble() * (max - min);
            } else if (args.length == 1) {
                return ThreadLocalRandom.current().nextDouble() * toDouble(args[0]);
            }
            return ThreadLocalRandom.current().nextDouble();
        });

        functions.put("randomint", args -> {
            if (args.length >= 2) {
                int min = toInt(args[0]);
                int max = toInt(args[1]);
                return ThreadLocalRandom.current().nextInt(min, max + 1);
            } else if (args.length == 1) {
                return ThreadLocalRandom.current().nextInt(toInt(args[0]) + 1);
            }
            return ThreadLocalRandom.current().nextInt();
        });

        // Modulo
        functions.put("mod", args -> {
            if (args.length < 2) return 0;
            return toDouble(args[0]) % toDouble(args[1]);
        });

        // Sum/Average
        functions.put("sum", args -> {
            double sum = 0;
            for (Object arg : args) {
                sum += toDouble(arg);
            }
            return sum;
        });

        functions.put("avg", args -> {
            if (args.length == 0) return 0;
            double sum = 0;
            for (Object arg : args) {
                sum += toDouble(arg);
            }
            return sum / args.length;
        });

        // Constants
        functions.put("pi", args -> Math.PI);
        functions.put("e", args -> Math.E);

        // Number type conversion
        functions.put("int", args -> {
            if (args.length < 1) return 0;
            return toInt(args[0]);
        });

        functions.put("long", args -> {
            if (args.length < 1) return 0L;
            return toLong(args[0]);
        });

        functions.put("double", args -> {
            if (args.length < 1) return 0.0;
            return toDouble(args[0]);
        });

        functions.put("decimal", args -> {
            if (args.length < 1) return BigDecimal.ZERO;
            if (args[0] instanceof BigDecimal bd) return bd;
            if (args[0] instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
            try {
                return new BigDecimal(args[0].toString());
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        });

        // Clamp
        functions.put("clamp", args -> {
            if (args.length < 3) return args.length > 0 ? toDouble(args[0]) : 0;
            double value = toDouble(args[0]);
            double min = toDouble(args[1]);
            double max = toDouble(args[2]);
            return Math.max(min, Math.min(max, value));
        });

        return functions;
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

    private static int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
