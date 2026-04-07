package cloud.opencode.base.log;

import java.util.Objects;

/**
 * Caller Information Record - Captures Stack Frame Location
 * 调用者信息记录 - 捕获堆栈帧位置
 *
 * <p>An immutable record that encapsulates the source location of a log call,
 * including class name, method name, file name and line number.</p>
 * <p>一个不可变记录，封装日志调用的源位置信息，
 * 包括类名、方法名、文件名和行号。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Captures caller location via StackWalker - 通过 StackWalker 捕获调用者位置</li>
 *   <li>Provides short and compact string representations - 提供短格式和紧凑格式的字符串表示</li>
 *   <li>Immutable and thread-safe - 不可变且线程安全</li>
 *   <li>Supports frame skipping for wrapper methods - 支持跳过包装方法的帧</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Capture caller info
 * CallerInfo info = CallerInfo.capture();
 * System.out.println(info.toShortString());  // "MyClass.myMethod:42"
 *
 * // Capture with additional frame skip
 * CallerInfo info = CallerInfo.capture(2);
 *
 * // Use UNKNOWN for cases where caller info is unavailable
 * CallerInfo unknown = CallerInfo.UNKNOWN;
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (components must not be null) - 空值安全: 否（组件不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
public record CallerInfo(String className, String methodName, String fileName, int lineNumber) {

    /**
     * Unknown caller info constant, used when caller information is unavailable.
     * 未知调用者信息常量，当调用者信息不可用时使用。
     */
    public static final CallerInfo UNKNOWN = new CallerInfo("unknown", "unknown", "unknown", -1);

    private static final String LOG_PACKAGE_PREFIX = "cloud.opencode.base.log";

    private static final StackWalker STACK_WALKER = StackWalker.getInstance();

    /**
     * Compact constructor that validates non-null parameters.
     * 紧凑构造函数，验证非空参数。
     *
     * @param className  the fully qualified class name | 完全限定类名
     * @param methodName the method name | 方法名
     * @param fileName   the source file name | 源文件名
     * @param lineNumber the line number | 行号
     */
    public CallerInfo {
        Objects.requireNonNull(className, "className must not be null");
        Objects.requireNonNull(methodName, "methodName must not be null");
        Objects.requireNonNull(fileName, "fileName must not be null");
    }

    /**
     * Captures the caller location by walking the stack and skipping internal log frames.
     * 通过遍历堆栈并跳过内部日志帧来捕获调用者位置。
     *
     * @return the caller info, or {@link #UNKNOWN} if not found | 调用者信息，如果未找到则返回 {@link #UNKNOWN}
     */
    public static CallerInfo capture() {
        return capture(0);
    }

    /**
     * Captures the caller location with additional frame skipping.
     * 捕获调用者位置，并跳过额外的帧数。
     *
     * <p>The method automatically skips all internal frames from the
     * {@code cloud.opencode.base.log} package, then skips the specified
     * number of additional frames.</p>
     * <p>该方法自动跳过 {@code cloud.opencode.base.log} 包中的所有内部帧，
     * 然后跳过指定数量的额外帧。</p>
     *
     * @param skipFrames the number of additional frames to skip | 要跳过的额外帧数
     * @return the caller info, or {@link #UNKNOWN} if not found | 调用者信息，如果未找到则返回 {@link #UNKNOWN}
     * @throws IllegalArgumentException if skipFrames is negative | 如果 skipFrames 为负数
     */
    public static CallerInfo capture(int skipFrames) {
        if (skipFrames < 0) {
            throw new IllegalArgumentException("skipFrames must not be negative: " + skipFrames);
        }
        return STACK_WALKER.walk(frames -> {
            var iterator = frames
                    .dropWhile(f -> f.getClassName().startsWith(LOG_PACKAGE_PREFIX))
                    .skip(skipFrames)
                    .iterator();
            if (iterator.hasNext()) {
                StackWalker.StackFrame frame = iterator.next();
                return new CallerInfo(
                        frame.getClassName(),
                        frame.getMethodName(),
                        frame.getFileName() != null ? frame.getFileName() : "unknown",
                        frame.getLineNumber());
            }
            return UNKNOWN;
        });
    }

    /**
     * Returns a short string representation in "SimpleClassName.method:line" format.
     * 返回 "简单类名.方法:行号" 格式的短字符串表示。
     *
     * @return short string representation | 短字符串表示
     */
    public String toShortString() {
        return simpleClassName() + "." + methodName + ":" + lineNumber;
    }

    /**
     * Returns a compact string representation in "SimpleClassName:line" format.
     * 返回 "简单类名:行号" 格式的紧凑字符串表示。
     *
     * @return compact string representation | 紧凑字符串表示
     */
    public String toCompactString() {
        return simpleClassName() + ":" + lineNumber;
    }

    @Override
    public String toString() {
        return className + "." + methodName + "(" + fileName + ":" + lineNumber + ")";
    }

    /**
     * Extracts the simple class name from the fully qualified class name.
     * 从完全限定类名中提取简单类名。
     */
    private String simpleClassName() {
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }
}
