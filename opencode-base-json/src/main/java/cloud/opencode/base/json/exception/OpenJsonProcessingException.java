/*
 * Copyright 2025 �� ��  Leon Soo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.opencode.base.json.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * JSON Processing Exception - Base Exception for JSON Operations
 * JSON 处理异常 - JSON 操作的基础异常
 *
 * <p>This exception is thrown when JSON serialization, deserialization,
 * or other JSON processing operations fail.</p>
 * <p>当 JSON 序列化、反序列化或其他 JSON 处理操作失败时抛出此异常。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * try {
 *     Object result = OpenJson.parse(invalidJson);
 * } catch (OpenJsonProcessingException e) {
 *     System.err.println("JSON error at line " + e.getLine() +
 *                        ", column " + e.getColumn());
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Categorized error types (parse, serialization, deserialization, etc.) - 分类错误类型</li>
 *   <li>Optional error location (line, column) tracking - 可选的错误位置（行、列）跟踪</li>
 *   <li>Factory methods for common error scenarios - 常见错误场景的工厂方法</li>
 *   <li>Extends {@link OpenException} with component="json" - 继承 OpenException，组件名="json"</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public class OpenJsonProcessingException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Error type enumeration
     * 错误类型枚举
     */
    public enum ErrorType {
        /** Parsing error - 解析错误 */
        PARSE_ERROR,
        /** Serialization error - 序列化错误 */
        SERIALIZATION_ERROR,
        /** Deserialization error - 反序列化错误 */
        DESERIALIZATION_ERROR,
        /** Type conversion error - 类型转换错误 */
        TYPE_CONVERSION_ERROR,
        /** Path evaluation error - 路径求值错误 */
        PATH_ERROR,
        /** IO error - IO错误 */
        IO_ERROR,
        /** Configuration error - 配置错误 */
        CONFIG_ERROR,
        /** Unknown error - 未知错误 */
        UNKNOWN
    }

    /**
     * Error type
     * 错误类型
     */
    private final ErrorType errorType;

    /**
     * Error location: line number (1-based, -1 if unknown)
     * 错误位置：行号（从1开始，-1表示未知）
     */
    private final int line;

    /**
     * Error location: column number (1-based, -1 if unknown)
     * 错误位置：列号（从1开始，-1表示未知）
     */
    private final int column;

    /**
     * Source path or identifier
     * 源路径或标识符
     */
    private final String source;

    /**
     * Constructs a new exception with the specified message.
     * 使用指定消息构造新异常。
     *
     * @param message the detail message - 详细消息
     */
    public OpenJsonProcessingException(String message) {
        this(message, ErrorType.UNKNOWN, (Throwable) null);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     * 使用指定消息和原因构造新异常。
     *
     * @param message the detail message - 详细消息
     * @param cause   the cause - 原因
     */
    public OpenJsonProcessingException(String message, Throwable cause) {
        this(message, ErrorType.UNKNOWN, (Throwable) cause);
    }

    /**
     * Constructs a new exception with the specified message and error type.
     * 使用指定消息和错误类型构造新异常。
     *
     * @param message   the detail message - 详细消息
     * @param errorType the error type - 错误类型
     */
    public OpenJsonProcessingException(String message, ErrorType errorType) {
        this(message, errorType, null);
    }

    /**
     * Constructs a new exception with the specified message, error type, and cause.
     * 使用指定消息、错误类型和原因构造新异常。
     *
     * @param message   the detail message - 详细消息
     * @param errorType the error type - 错误类型
     * @param cause     the cause - 原因
     */
    public OpenJsonProcessingException(String message, ErrorType errorType, Throwable cause) {
        super("json", (errorType != null ? errorType : ErrorType.UNKNOWN).name(), message, cause);
        this.errorType = errorType != null ? errorType : ErrorType.UNKNOWN;
        this.line = -1;
        this.column = -1;
        this.source = null;
    }

    /**
     * Constructs a new exception with full location information.
     * 使用完整位置信息构造新异常。
     *
     * @param message   the detail message - 详细消息
     * @param errorType the error type - 错误类型
     * @param cause     the cause - 原因
     * @param line      the line number - 行号
     * @param column    the column number - 列号
     * @param source    the source identifier - 源标识符
     */
    public OpenJsonProcessingException(String message, ErrorType errorType,
                                       Throwable cause, int line, int column, String source) {
        super("json", (errorType != null ? errorType : ErrorType.UNKNOWN).name(), message, cause);
        this.errorType = errorType != null ? errorType : ErrorType.UNKNOWN;
        this.line = line;
        this.column = column;
        this.source = source;
    }

    /**
     * Returns the error type.
     * 返回错误类型。
     *
     * @return the error type - 错误类型
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * Returns the line number where the error occurred.
     * 返回发生错误的行号。
     *
     * @return the line number, or -1 if unknown - 行号，未知时返回-1
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the column number where the error occurred.
     * 返回发生错误的列号。
     *
     * @return the column number, or -1 if unknown - 列号，未知时返回-1
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the source identifier.
     * 返回源标识符。
     *
     * @return the source, or null if unknown - 源标识符，未知时返回null
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns whether location information is available.
     * 返回位置信息是否可用。
     *
     * @return true if line and column are known - 如果行号和列号已知则返回true
     */
    public boolean hasLocation() {
        return line >= 0 && column >= 0;
    }

    /**
     * Returns a formatted location string.
     * 返回格式化的位置字符串。
     *
     * @return the location string - 位置字符串
     */
    public String getLocationString() {
        if (!hasLocation()) {
            return "unknown location";
        }
        StringBuilder sb = new StringBuilder();
        if (source != null) {
            sb.append(source).append(":");
        }
        sb.append("line ").append(line).append(", column ").append(column);
        return sb.toString();
    }

    @Override
    public String getMessage() {
        String baseMessage = super.getMessage();
        if (hasLocation()) {
            return baseMessage + " at " + getLocationString();
        }
        return baseMessage;
    }

    /**
     * Returns the raw message without component/error-code prefix or location suffix.
     * 返回不含组件/错误码前缀和位置后缀的原始消息。
     *
     * @return the raw message - 原始消息
     */
    @Override
    public String getRawMessage() {
        return super.getRawMessage();
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a parse error exception.
     * 创建解析错误异常。
     *
     * @param message the detail message - 详细消息
     * @return the exception - 异常
     */
    public static OpenJsonProcessingException parseError(String message) {
        return new OpenJsonProcessingException(message, ErrorType.PARSE_ERROR);
    }

    /**
     * Creates a parse error exception with location.
     * 创建带位置信息的解析错误异常。
     *
     * @param message the detail message - 详细消息
     * @param line    the line number - 行号
     * @param column  the column number - 列号
     * @return the exception - 异常
     */
    public static OpenJsonProcessingException parseError(String message, int line, int column) {
        return new OpenJsonProcessingException(message, ErrorType.PARSE_ERROR, null, line, column, null);
    }

    /**
     * Creates a serialization error exception.
     * 创建序列化错误异常。
     *
     * @param message the detail message - 详细消息
     * @param cause   the cause - 原因
     * @return the exception - 异常
     */
    public static OpenJsonProcessingException serializationError(String message, Throwable cause) {
        return new OpenJsonProcessingException(message, ErrorType.SERIALIZATION_ERROR, cause);
    }

    /**
     * Creates a deserialization error exception.
     * 创建反序列化错误异常。
     *
     * @param message the detail message - 详细消息
     * @param cause   the cause - 原因
     * @return the exception - 异常
     */
    public static OpenJsonProcessingException deserializationError(String message, Throwable cause) {
        return new OpenJsonProcessingException(message, ErrorType.DESERIALIZATION_ERROR, cause);
    }

    /**
     * Creates a type conversion error exception.
     * 创建类型转换错误异常。
     *
     * @param message the detail message - 详细消息
     * @return the exception - 异常
     */
    public static OpenJsonProcessingException typeConversionError(String message) {
        return new OpenJsonProcessingException(message, ErrorType.TYPE_CONVERSION_ERROR);
    }

    /**
     * Creates a path evaluation error exception.
     * 创建路径求值错误异常。
     *
     * @param message the detail message - 详细消息
     * @return the exception - 异常
     */
    public static OpenJsonProcessingException pathError(String message) {
        return new OpenJsonProcessingException(message, ErrorType.PATH_ERROR);
    }

    /**
     * Creates an IO error exception.
     * 创建IO错误异常。
     *
     * @param message the detail message - 详细消息
     * @param cause   the cause - 原因
     * @return the exception - 异常
     */
    public static OpenJsonProcessingException ioError(String message, Throwable cause) {
        return new OpenJsonProcessingException(message, ErrorType.IO_ERROR, cause);
    }

    /**
     * Creates a configuration error exception.
     * 创建配置错误异常。
     *
     * @param message the detail message - 详细消息
     * @return the exception - 异常
     */
    public static OpenJsonProcessingException configError(String message) {
        return new OpenJsonProcessingException(message, ErrorType.CONFIG_ERROR);
    }
}
