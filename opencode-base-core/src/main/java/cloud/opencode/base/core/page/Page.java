package cloud.opencode.base.core.page;

import java.util.ArrayList;
import java.util.List;

/**
 * Page - Pagination result container
 * 分页 - 分页结果容器
 *
 * <p>Holds paginated query results with metadata including current page,
 * page size, total count, and computed properties like total pages.</p>
 * <p>保存分页查询结果及元数据，包括当前页、页大小、总数以及总页数等计算属性。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Page<User> page = Page.of(1, 10);
 * page.setTotal(100);
 * page.setRecords(userList);
 * long totalPages = page.getPages(); // 10
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pagination result container with metadata - 带元数据的分页结果容器</li>
 *   <li>Computed properties: total pages, hasNext, hasPrevious - 计算属性: 总页数、是否有下一页/上一页</li>
 *   <li>Generic type parameter for record type - 记录类型的泛型参数</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No, mutable state (records, total) - 线程安全: 否，可变状态（记录列表、总数）</li>
 *   <li>Null-safe: Yes, defaults to empty list - 空值安全: 是，默认为空列表</li>
 * </ul>
 *
 * @param <T> the record type | 记录类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public class Page<T> {

    private long current;
    private long size;
    private long total;
    private List<T> records;

    public Page() {
        this(1, 10);
    }

    public Page(long current, long size) {
        this.current = Math.max(current, 1);
        this.size = Math.max(size, 1);
        this.total = 0;
        this.records = new ArrayList<>();
    }

    public static <T> Page<T> of(long current, long size) {
        return new Page<>(current, size);
    }

    // ============ Getters & Setters ============

    public long getCurrent() { return current; }
    public Page<T> setCurrent(long current) { this.current = Math.max(current, 1); return this; }
    public long getSize() { return size; }
    public Page<T> setSize(long size) { this.size = Math.max(size, 1); return this; }
    public long getTotal() { return total; }
    public Page<T> setTotal(long total) { this.total = Math.max(total, 0); return this; }
    public List<T> getRecords() { return records; }
    public Page<T> setRecords(List<T> records) { this.records = records != null ? records : new ArrayList<>(); return this; }

    // ============ Computed Properties ============

    public long getPages() {
        if (size == 0) return 0;
        long pages = total / size;
        if (total % size != 0) pages++;
        return pages;
    }

    public boolean hasNext() {
        return current < getPages();
    }

    public boolean hasPrevious() {
        return current > 1;
    }

    public long getOffset() {
        return (current - 1) * size;
    }

    @Override
    public String toString() {
        return "Page{current=" + current + ", size=" + size + ", total=" + total +
               ", pages=" + getPages() + ", records=" + (records != null ? records.size() : 0) + "}";
    }
}
