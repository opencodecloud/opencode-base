package cloud.opencode.base.collections.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * OpenCollectionException - Exception for collection operations
 * OpenCollectionException - 集合操作异常
 *
 * <p>This exception is thrown when collection operations fail or encounter invalid states.
 * Extends {@link OpenException} for unified exception handling across OpenCode components.</p>
 * <p>当集合操作失败或遇到无效状态时抛出此异常。继承 {@link OpenException} 以实现 OpenCode 组件间的统一异常处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Extends OpenException with component="Collections" - 继承 OpenException，组件名="Collections"</li>
 *   <li>Wraps underlying exceptions - 包装底层异常</li>
 *   <li>Provides meaningful error messages - 提供有意义的错误消息</li>
 *   <li>Supports exception chaining - 支持异常链</li>
 *   <li>Factory methods for common scenarios - 常见场景的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Throw with message - 带消息抛出
 * throw new OpenCollectionException("Collection is empty");
 *
 * // Throw with cause - 带原因抛出
 * throw new OpenCollectionException("Operation failed", cause);
 *
 * // Factory methods - 工厂方法
 * throw OpenCollectionException.emptyCollection("list");
 * throw OpenCollectionException.indexOutOfBounds(10, 5);
 * throw OpenCollectionException.duplicateKey("key1");
 *
 * // Catch with unified OpenException - 使用统一 OpenException 捕获
 * try { ... } catch (OpenException e) { // catches all OpenCode exceptions }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Serializable: Yes - 可序列化: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public class OpenCollectionException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "Collections";

    /**
     * Constructs a new exception with the specified message.
     * 构造带指定消息的新异常。
     *
     * @param message the detail message | 详细消息
     */
    public OpenCollectionException(String message) {
        super(COMPONENT, null, message, null);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     * 构造带指定消息和原因的新异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public OpenCollectionException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     * 构造带指定原因的新异常。
     *
     * @param cause the cause | 原因
     */
    public OpenCollectionException(Throwable cause) {
        super(COMPONENT, null, cause != null ? cause.getMessage() : null, cause);
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create exception for empty collection.
     * 创建空集合异常。
     *
     * @param collectionType the type of collection | 集合类型
     * @return new exception | 新异常
     */
    public static OpenCollectionException emptyCollection(String collectionType) {
        return new OpenCollectionException("Collection is empty: " + collectionType);
    }

    /**
     * Create exception for index out of bounds.
     * 创建索引越界异常。
     *
     * @param index the invalid index | 无效索引
     * @param size  the collection size | 集合大小
     * @return new exception | 新异常
     */
    public static OpenCollectionException indexOutOfBounds(int index, int size) {
        return new OpenCollectionException("Index: " + index + ", Size: " + size);
    }

    /**
     * Create exception for duplicate key.
     * 创建重复键异常。
     *
     * @param key the duplicate key | 重复的键
     * @return new exception | 新异常
     */
    public static OpenCollectionException duplicateKey(Object key) {
        return new OpenCollectionException("Duplicate key: " + key);
    }

    /**
     * Create exception for duplicate value.
     * 创建重复值异常。
     *
     * @param value the duplicate value | 重复的值
     * @return new exception | 新异常
     */
    public static OpenCollectionException duplicateValue(Object value) {
        return new OpenCollectionException("Duplicate value: " + value);
    }

    /**
     * Create exception for null element.
     * 创建空元素异常。
     *
     * @return new exception | 新异常
     */
    public static OpenCollectionException nullElement() {
        return new OpenCollectionException("Null element not allowed");
    }

    /**
     * Create exception for null key.
     * 创建空键异常。
     *
     * @return new exception | 新异常
     */
    public static OpenCollectionException nullKey() {
        return new OpenCollectionException("Null key not allowed");
    }

    /**
     * Create exception for null value.
     * 创建空值异常。
     *
     * @return new exception | 新异常
     */
    public static OpenCollectionException nullValue() {
        return new OpenCollectionException("Null value not allowed");
    }

    /**
     * Create exception for immutable collection modification.
     * 创建不可变集合修改异常。
     *
     * @return new exception | 新异常
     */
    public static OpenCollectionException immutableCollection() {
        return new OpenCollectionException("Cannot modify immutable collection");
    }

    /**
     * Create exception for element not found.
     * 创建元素未找到异常。
     *
     * @param element the element | 元素
     * @return new exception | 新异常
     */
    public static OpenCollectionException elementNotFound(Object element) {
        return new OpenCollectionException("Element not found: " + element);
    }

    /**
     * Create exception for key not found.
     * 创建键未找到异常。
     *
     * @param key the key | 键
     * @return new exception | 新异常
     */
    public static OpenCollectionException keyNotFound(Object key) {
        return new OpenCollectionException("Key not found: " + key);
    }

    /**
     * Create exception for illegal capacity.
     * 创建非法容量异常。
     *
     * @param capacity the illegal capacity | 非法容量
     * @return new exception | 新异常
     */
    public static OpenCollectionException illegalCapacity(int capacity) {
        return new OpenCollectionException("Illegal capacity: " + capacity);
    }

    /**
     * Create exception for multiple elements found when expecting one.
     * 创建期望单个元素但找到多个异常。
     *
     * @param count the actual count | 实际数量
     * @return new exception | 新异常
     */
    public static OpenCollectionException multipleElementsFound(int count) {
        return new OpenCollectionException("Expected one element but found: " + count);
    }

    /**
     * Create exception for negative size.
     * 创建负数大小异常。
     *
     * @param size the negative size | 负数大小
     * @return new exception | 新异常
     */
    public static OpenCollectionException negativeSize(int size) {
        return new OpenCollectionException("Size cannot be negative: " + size);
    }

    /**
     * Create exception for unsupported operation.
     * 创建不支持的操作异常。
     *
     * @param operation the operation name | 操作名称
     * @return new exception | 新异常
     */
    public static OpenCollectionException unsupportedOperation(String operation) {
        return new OpenCollectionException("Unsupported operation: " + operation);
    }
}
