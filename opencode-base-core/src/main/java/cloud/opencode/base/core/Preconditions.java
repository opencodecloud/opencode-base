package cloud.opencode.base.core;

import cloud.opencode.base.core.exception.OpenIllegalArgumentException;
import cloud.opencode.base.core.exception.OpenIllegalStateException;

/**
 * Preconditions Class - Parameter validation and state checking (Guava-style)
 * 前置条件校验类 - 参数校验和状态检查 (Guava 风格)
 *
 * <p>Provides parameter validation, state checking and index validation with fluent API support.</p>
 * <p>用于参数校验和状态检查。所有方法返回校验后的值，支持链式调用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Null checking (checkNotNull) - 非空校验</li>
 *   <li>Argument validation (checkArgument) - 参数校验</li>
 *   <li>State validation (checkState) - 状态校验</li>
 *   <li>Index validation (checkElementIndex, checkPositionIndex) - 索引校验</li>
 *   <li>Formatted error messages with %s placeholders - 格式化错误消息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Null checking - 非空校验
 * this.name = Preconditions.checkNotNull(name, "name cannot be null");
 *
 * // Argument validation - 参数校验
 * Preconditions.checkArgument(age > 0, "age must be positive, got: %s", age);
 *
 * // State validation - 状态校验
 * Preconditions.checkState(isInitialized, "service not initialized");
 *
 * // Index validation - 索引校验
 * int idx = Preconditions.checkElementIndex(index, list.size(), "index");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class Preconditions {

    private Preconditions() {
        // 工具类不可实例化
    }

    // ==================== 非空校验 ====================

    /**
     * Checks that the object is not null, otherwise throws NullPointerException
     * 检查对象非空，否则抛出 NullPointerException
     *
     * @param reference the object to check | 待检查的对象
     * @param <T> the object type | 对象类型
     * @return the validated object (supports chaining) | 校验后的对象（支持链式调用）
     * @throws NullPointerException if the object is null | 如果对象为 null
     */
    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /**
     * Checks that the object is not null, otherwise throws NullPointerException with message
     * 检查对象非空，否则抛出带消息的 NullPointerException
     *
     * @param reference the object to check | 待检查的对象
     * @param errorMessage the error message | 异常消息
     * @param <T> the object type | 对象类型
     * @return the validated object (supports chaining) | 校验后的对象（支持链式调用）
     * @throws NullPointerException if the object is null | 如果对象为 null
     */
    public static <T> T checkNotNull(T reference, String errorMessage) {
        if (reference == null) {
            throw new NullPointerException(errorMessage);
        }
        return reference;
    }

    /**
     * Checks that the object is not null, otherwise throws NullPointerException with formatted message
     * 检查对象非空，否则抛出格式化消息的 NullPointerException
     *
     * @param reference the object to check | 待检查的对象
     * @param template the message template with %s placeholders | 消息模板，使用 %s 作为占位符
     * @param args the template arguments | 模板参数
     * @param <T> the object type | 对象类型
     * @return the validated object (supports chaining) | 校验后的对象（支持链式调用）
     * @throws NullPointerException if the object is null | 如果对象为 null
     */
    public static <T> T checkNotNull(T reference, String template, Object... args) {
        if (reference == null) {
            throw new NullPointerException(format(template, args));
        }
        return reference;
    }

    // ==================== 参数校验 ====================

    /**
     * Checks the argument condition, otherwise throws OpenIllegalArgumentException
     * 检查参数条件，否则抛出 OpenIllegalArgumentException
     *
     * @param expression the condition expression | 条件表达式
     * @throws OpenIllegalArgumentException if the condition is false | 如果条件为 false
     */
    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new OpenIllegalArgumentException("Invalid argument");
        }
    }

    /**
     * Checks the argument condition, otherwise throws OpenIllegalArgumentException with message
     * 检查参数条件，否则抛出带消息的 OpenIllegalArgumentException
     *
     * @param expression the condition expression | 条件表达式
     * @param errorMessage the error message | 异常消息
     * @throws OpenIllegalArgumentException if the condition is false | 如果条件为 false
     */
    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new OpenIllegalArgumentException(errorMessage);
        }
    }

    /**
     * Checks the argument condition, otherwise throws OpenIllegalArgumentException with formatted message
     * 检查参数条件，否则抛出格式化消息的 OpenIllegalArgumentException
     *
     * @param expression the condition expression | 条件表达式
     * @param template the message template with %s placeholders | 消息模板，使用 %s 作为占位符
     * @param args the template arguments | 模板参数
     * @throws OpenIllegalArgumentException if the condition is false | 如果条件为 false
     */
    public static void checkArgument(boolean expression, String template, Object... args) {
        if (!expression) {
            throw new OpenIllegalArgumentException(format(template, args));
        }
    }

    // ==================== 状态校验 ====================

    /**
     * Checks the state condition, otherwise throws OpenIllegalStateException
     * 检查状态条件，否则抛出 OpenIllegalStateException
     *
     * @param expression the condition expression | 条件表达式
     * @throws OpenIllegalStateException if the condition is false | 如果条件为 false
     */
    public static void checkState(boolean expression) {
        if (!expression) {
            throw new OpenIllegalStateException("Invalid state");
        }
    }

    /**
     * Checks the state condition, otherwise throws OpenIllegalStateException with message
     * 检查状态条件，否则抛出带消息的 OpenIllegalStateException
     *
     * @param expression the condition expression | 条件表达式
     * @param errorMessage the error message | 异常消息
     * @throws OpenIllegalStateException if the condition is false | 如果条件为 false
     */
    public static void checkState(boolean expression, String errorMessage) {
        if (!expression) {
            throw new OpenIllegalStateException(errorMessage);
        }
    }

    /**
     * Checks the state condition, otherwise throws OpenIllegalStateException with formatted message
     * 检查状态条件，否则抛出格式化消息的 OpenIllegalStateException
     *
     * @param expression the condition expression | 条件表达式
     * @param template the message template with %s placeholders | 消息模板，使用 %s 作为占位符
     * @param args the template arguments | 模板参数
     * @throws OpenIllegalStateException if the condition is false | 如果条件为 false
     */
    public static void checkState(boolean expression, String template, Object... args) {
        if (!expression) {
            throw new OpenIllegalStateException(format(template, args));
        }
    }

    // ==================== 索引校验 ====================

    /**
     * Checks if the element index is valid [0, size)
     * 检查元素索引是否有效 [0, size)
     *
     * @param index the index value | 索引值
     * @param size the collection size | 集合大小
     * @return the validated index | 校验后的索引
     * @throws OpenIllegalArgumentException if the index is invalid | 如果索引无效
     */
    public static int checkElementIndex(int index, int size) {
        return checkElementIndex(index, size, "index");
    }

    /**
     * Checks if the element index is valid [0, size)
     * 检查元素索引是否有效 [0, size)
     *
     * @param index the index value | 索引值
     * @param size the collection size | 集合大小
     * @param desc the index description | 索引描述
     * @return the validated index | 校验后的索引
     * @throws OpenIllegalArgumentException if the index is invalid | 如果索引无效
     */
    public static int checkElementIndex(int index, int size, String desc) {
        if (size < 0) {
            throw new OpenIllegalArgumentException("negative size: " + size);
        }
        if (index < 0 || index >= size) {
            throw new OpenIllegalArgumentException(badElementIndex(index, size, desc));
        }
        return index;
    }

    /**
     * Checks if the position index is valid [0, size]
     * 检查位置索引是否有效 [0, size]
     *
     * @param index the index value | 索引值
     * @param size the collection size | 集合大小
     * @return the validated index | 校验后的索引
     * @throws OpenIllegalArgumentException if the index is invalid | 如果索引无效
     */
    public static int checkPositionIndex(int index, int size) {
        return checkPositionIndex(index, size, "index");
    }

    /**
     * Checks if the position index is valid [0, size]
     * 检查位置索引是否有效 [0, size]
     *
     * @param index the index value | 索引值
     * @param size the collection size | 集合大小
     * @param desc the index description | 索引描述
     * @return the validated index | 校验后的索引
     * @throws OpenIllegalArgumentException if the index is invalid | 如果索引无效
     */
    public static int checkPositionIndex(int index, int size, String desc) {
        if (size < 0) {
            throw new OpenIllegalArgumentException("negative size: " + size);
        }
        if (index < 0 || index > size) {
            throw new OpenIllegalArgumentException(badPositionIndex(index, size, desc));
        }
        return index;
    }

    /**
     * Checks if the position range is valid [start, end] and end <= size
     * 检查位置范围是否有效 [start, end] 且 end <= size
     *
     * @param start the start index | 起始索引
     * @param end the end index | 结束索引
     * @param size the collection size | 集合大小
     * @throws OpenIllegalArgumentException if the range is invalid | 如果范围无效
     */
    public static void checkPositionIndexes(int start, int end, int size) {
        if (start < 0 || end < start || end > size) {
            throw new OpenIllegalArgumentException(badPositionIndexes(start, end, size));
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * Formats the message
     * 格式化消息
     */
    private static String format(String template, Object... args) {
        if (template == null) {
            return null;
        }
        if (args == null || args.length == 0) {
            return template;
        }

        StringBuilder sb = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;
        int i = 0;

        while (i < args.length) {
            int placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }
            sb.append(template, templateStart, placeholderStart);
            sb.append(args[i++]);
            templateStart = placeholderStart + 2;
        }

        sb.append(template, templateStart, template.length());

        // 追加剩余参数
        while (i < args.length) {
            sb.append(" [").append(args[i++]).append("]");
        }

        return sb.toString();
    }

    private static String badElementIndex(int index, int size, String desc) {
        if (index < 0) {
            return desc + " (" + index + ") must not be negative";
        } else {
            return desc + " (" + index + ") must be less than size (" + size + ")";
        }
    }

    private static String badPositionIndex(int index, int size, String desc) {
        if (index < 0) {
            return desc + " (" + index + ") must not be negative";
        } else {
            return desc + " (" + index + ") must not be greater than size (" + size + ")";
        }
    }

    private static String badPositionIndexes(int start, int end, int size) {
        if (start < 0 || start > size) {
            return badPositionIndex(start, size, "start index");
        }
        if (end < 0 || end > size) {
            return badPositionIndex(end, size, "end index");
        }
        return "end index (" + end + ") must not be less than start index (" + start + ")";
    }
}
