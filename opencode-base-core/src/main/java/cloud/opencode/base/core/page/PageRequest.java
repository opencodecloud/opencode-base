package cloud.opencode.base.core.page;

/**
 * PageRequest - Immutable pagination request with sorting
 * 分页请求 - 带排序的不可变分页请求
 *
 * <p>Represents a request for a specific page of data with optional sorting.
 * Page numbering is 1-based. Compatible with Spring Data {@code Pageable} semantics
 * but has no dependency on Spring Data Commons.</p>
 * <p>表示带可选排序的特定分页数据请求。页码从 1 开始。
 * 与 Spring Data {@code Pageable} 语义兼容，但不依赖 Spring Data Commons。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PageRequest req = PageRequest.of(1, 20, Sort.by("name"));
 * PageRequest next = req.next();
 * long offset = req.getOffset();  // (page - 1) * size
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable pagination request with sorting - 带排序的不可变分页请求</li>
 *   <li>1-based page numbering - 基于1的页码</li>
 *   <li>Navigation methods: next(), previous(), first() - 导航方法: next()、previous()、first()</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes, sort defaults to unsorted - 空值安全: 是，排序默认为未排序</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Sort
 * @see Page
 * @param page the page number (1-based) | 页码（从 1 开始）
 * @param size the page size | 每页大小
 * @param sort the sort criteria | 排序条件
 * @since JDK 25, opencode-base-core V1.0.0
 */
public record PageRequest(long page, long size, Sort sort) {

    public PageRequest {
        if (page < 1) throw new IllegalArgumentException("Page must be >= 1, got: " + page);
        if (size < 1) throw new IllegalArgumentException("Size must be >= 1, got: " + size);
        if (sort == null) sort = Sort.unsorted();
    }

    public static PageRequest of(long page, long size) {
        return new PageRequest(page, size, Sort.unsorted());
    }

    public static PageRequest of(long page, long size, Sort sort) {
        return new PageRequest(page, size, sort);
    }

    public static PageRequest ofSize(long size) {
        return new PageRequest(1, size, Sort.unsorted());
    }

    public long getOffset() {
        return Math.multiplyExact(page - 1, size);
    }

    public boolean isFirst() {
        return page == 1;
    }

    public PageRequest next() {
        return new PageRequest(Math.addExact(page, 1), size, sort);
    }

    public PageRequest previous() {
        return isFirst() ? this : new PageRequest(page - 1, size, sort);
    }

    public PageRequest first() {
        return isFirst() ? this : new PageRequest(1, size, sort);
    }

    public PageRequest withSort(Sort newSort) {
        return new PageRequest(page, size, newSort);
    }

    public PageRequest withPage(long newPage) {
        return new PageRequest(newPage, size, sort);
    }

    /**
     * Creates an empty {@link Page} matching this request's page number and size.
     * 创建一个与此请求的页码和页大小匹配的空 {@link Page}。
     *
     * @param <T> the record type | 记录类型
     * @return an empty Page | 空分页对象
     * @since JDK 25, opencode-base-core V1.0.3
     */
    public <T> Page<T> toPage() {
        return new Page<>(page, size, 0, java.util.List.of());
    }

    @Override
    public String toString() {
        return "PageRequest{page=" + page + ", size=" + size
                + (sort.isUnsorted() ? "" : ", sort=" + sort) + "}";
    }
}
