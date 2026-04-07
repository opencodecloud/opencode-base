package cloud.opencode.base.core.page;

import cloud.opencode.base.core.Preconditions;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Page - Immutable pagination result container
 * 分页 - 不可变分页结果容器
 *
 * <p>Holds paginated query results with metadata including current page,
 * page size, total count, and computed properties like total pages.
 * This is an immutable record — all fields are set at construction time
 * and the records list is defensively copied.</p>
 * <p>保存分页查询结果及元数据，包括当前页、页大小、总数以及总页数等计算属性。
 * 这是一个不可变记录——所有字段在构造时设置，记录列表进行防御性复制。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Page<User> page = Page.of(1, 10, 100, userList);
 * long totalPages = page.pages(); // 10
 * Page<UserDto> dtoPage = page.map(UserDto::from);
 * Page<User> empty = Page.empty(10);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable pagination result container with metadata - 带元数据的不可变分页结果容器</li>
 *   <li>Computed properties: total pages, hasNext, hasPrevious, offset - 计算属性: 总页数、是否有下一页/上一页、偏移量</li>
 *   <li>Type-safe mapping via {@link #map(Function)} - 通过 {@link #map(Function)} 进行类型安全映射</li>
 *   <li>Defensive copy of records list - 记录列表的防御性复制</li>
 *   <li>Generic type parameter for record type - 记录类型的泛型参数</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes, records must not be null - 空值安全: 是，记录列表不能为 null</li>
 * </ul>
 *
 * @param current the page number (1-based) | 页码（从 1 开始）
 * @param size    the page size | 每页大小
 * @param total   the total record count | 总记录数
 * @param records the current page records (unmodifiable) | 当前页记录（不可修改）
 * @param <T>     the record type | 记录类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.3
 */
public record Page<T>(
        long current,
        long size,
        long total,
        List<T> records
) {

    /**
     * Compact constructor with validation and defensive copy.
     * 紧凑构造器，包含验证和防御性复制。
     */
    public Page {
        Preconditions.checkArgument(current >= 1, "current must >= 1, got %s", current);
        Preconditions.checkArgument(size >= 1, "size must >= 1, got %s", size);
        Preconditions.checkArgument(total >= 0, "total must >= 0, got %s", total);
        Objects.requireNonNull(records, "records must not be null");
        records = List.copyOf(records);
    }

    // ============ Factory Methods | 工厂方法 ============

    /**
     * Creates a new Page with the given parameters.
     * 使用给定参数创建新的分页对象。
     *
     * @param current the page number (1-based) | 页码（从 1 开始）
     * @param size    the page size | 每页大小
     * @param total   the total record count | 总记录数
     * @param records the current page records | 当前页记录
     * @param <T>     the record type | 记录类型
     * @return a new Page instance | 新的分页实例
     * @since JDK 25, opencode-base-core V1.0.3
     */
    public static <T> Page<T> of(long current, long size, long total, List<T> records) {
        return new Page<>(current, size, total, records);
    }

    /**
     * Creates an empty Page with no records and zero total.
     * 创建一个没有记录且总数为零的空分页对象。
     *
     * @param size the page size | 每页大小
     * @param <T>  the record type | 记录类型
     * @return an empty Page instance | 空分页实例
     * @since JDK 25, opencode-base-core V1.0.3
     */
    public static <T> Page<T> empty(long size) {
        return new Page<>(1, size, 0, List.of());
    }

    // ============ Computed Properties | 计算属性 ============

    /**
     * Computes the total number of pages.
     * 计算总页数。
     *
     * @return the total number of pages, or 0 if total is 0 | 总页数，若总数为 0 则返回 0
     * @since JDK 25, opencode-base-core V1.0.3
     */
    public long pages() {
        if (total == 0) {
            return 0;
        }
        return Math.ceilDiv(total, size);
    }

    /**
     * Returns {@code true} if there is a next page.
     * 如果有下一页则返回 {@code true}。
     *
     * @return whether a next page exists | 是否存在下一页
     */
    public boolean hasNext() {
        return current < pages();
    }

    /**
     * Returns {@code true} if there is a previous page (i.e., current &gt; 1).
     * 如果有上一页（即 current &gt; 1）则返回 {@code true}。
     *
     * @return whether a previous page exists | 是否存在上一页
     */
    public boolean hasPrevious() {
        return current > 1;
    }

    /**
     * Computes the zero-based offset for the current page.
     * 计算当前页的零基偏移量。
     *
     * @return the offset: {@code (current - 1) * size} | 偏移量
     */
    public long offset() {
        return Math.multiplyExact(current - 1, size);
    }

    // ============ Mapping | 映射 ============

    /**
     * Maps the records of this page using the given function, preserving pagination metadata.
     * 使用给定函数映射本页记录，保留分页元数据。
     *
     * @param mapper the mapping function | 映射函数
     * @param <U>    the target record type | 目标记录类型
     * @return a new Page with mapped records | 包含映射后记录的新分页对象
     * @throws NullPointerException if mapper is null | 如果 mapper 为 null
     * @since JDK 25, opencode-base-core V1.0.3
     */
    public <U> Page<U> map(Function<T, U> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        List<U> mapped = records.stream().map(mapper).toList();
        return new Page<>(current, size, total, mapped);
    }

    @Override
    public String toString() {
        return "Page{current=" + current + ", size=" + size + ", total=" + total
                + ", pages=" + pages() + ", records=" + records.size() + "}";
    }
}
