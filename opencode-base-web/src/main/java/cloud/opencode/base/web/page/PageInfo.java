package cloud.opencode.base.web.page;

/**
 * Page Info
 * 分页信息
 *
 * <p>Contains pagination metadata.</p>
 * <p>包含分页元数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic total pages calculation - 自动计算总页数</li>
 *   <li>Page navigation queries (hasNext, hasPrevious) - 分页导航查询</li>
 *   <li>SQL offset computation - SQL 偏移量计算</li>
 *   <li>Current page size calculation - 当前页大小计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create page info
 * PageInfo info = PageInfo.of(1, 10, 100);
 *
 * // Query navigation
 * boolean hasNext = info.hasNext(); // true
 * int totalPages = info.totalPages(); // 10
 * int offset = info.getOffset(); // 0
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (values clamped to valid ranges) - 空值安全: 是（值限制在有效范围内）</li>
 * </ul>
 *
 * @param page the page number (1-based) | 页码（从1开始）
 * @param size the page size | 页大小
 * @param total the total count | 总数
 * @param totalPages the total pages | 总页数
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public record PageInfo(
    int page,
    int size,
    long total,
    int totalPages
) {

    /**
     * Compact constructor with calculation
     * 带计算的紧凑构造函数
     */
    public PageInfo {
        page = Math.max(1, page);
        size = Math.max(1, size);
        total = Math.max(0, total);
        totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
    }

    /**
     * Create page info
     * 创建分页信息
     *
     * @param page the page number | 页码
     * @param size the page size | 页大小
     * @param total the total count | 总数
     * @return the page info | 分页信息
     */
    public static PageInfo of(int page, int size, long total) {
        return new PageInfo(page, size, total, 0);
    }

    /**
     * Create from page request
     * 从分页请求创建
     *
     * @param request the page request | 分页请求
     * @param total the total count | 总数
     * @return the page info | 分页信息
     */
    public static PageInfo from(PageRequest request, long total) {
        return new PageInfo(request.page(), request.size(), total, 0);
    }

    /**
     * Create empty page info
     * 创建空分页信息
     *
     * @return the page info | 分页信息
     */
    public static PageInfo empty() {
        return new PageInfo(1, 10, 0, 0);
    }

    /**
     * Check if has next page
     * 检查是否有下一页
     *
     * @return true if has next | 如果有下一页返回true
     */
    public boolean hasNext() {
        return page < totalPages;
    }

    /**
     * Check if has previous page
     * 检查是否有上一页
     *
     * @return true if has previous | 如果有上一页返回true
     */
    public boolean hasPrevious() {
        return page > 1;
    }

    /**
     * Check if first page
     * 检查是否第一页
     *
     * @return true if first | 如果是第一页返回true
     */
    public boolean isFirst() {
        return page == 1;
    }

    /**
     * Check if last page
     * 检查是否最后一页
     *
     * @return true if last | 如果是最后一页返回true
     */
    public boolean isLast() {
        return page >= totalPages;
    }

    /**
     * Check if empty
     * 检查是否为空
     *
     * @return true if empty | 如果为空返回true
     */
    public boolean isEmpty() {
        return total == 0;
    }

    /**
     * Get offset for SQL
     * 获取SQL偏移量
     *
     * @return the offset | 偏移量
     */
    public int getOffset() {
        return Math.toIntExact((long)(page - 1) * size);
    }

    /**
     * Get next page number
     * 获取下一页页码
     *
     * @return the next page number | 下一页页码
     */
    public int getNextPage() {
        return hasNext() ? page + 1 : page;
    }

    /**
     * Get previous page number
     * 获取上一页页码
     *
     * @return the previous page number | 上一页页码
     */
    public int getPreviousPage() {
        return hasPrevious() ? page - 1 : page;
    }

    /**
     * Get end offset for current page
     * 获取当前页结束位置
     *
     * @return the end offset | 结束偏移量
     */
    public long getEndOffset() {
        return Math.min(getOffset() + size, total);
    }

    /**
     * Get current page actual size
     * 获取当前页实际大小
     *
     * @return the current page size | 当前页大小
     */
    public int getCurrentPageSize() {
        return (int) (getEndOffset() - getOffset());
    }
}
