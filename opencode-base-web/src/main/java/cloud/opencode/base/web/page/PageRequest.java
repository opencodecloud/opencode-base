package cloud.opencode.base.web.page;

/**
 * Page Request
 * 分页请求
 *
 * <p>Standard pagination request parameters.</p>
 * <p>标准分页请求参数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>1-based page numbering with validation - 基于 1 的页码编号并带验证</li>
 *   <li>Sort criteria support - 排序条件支持</li>
 *   <li>Page navigation (next, previous, first) - 分页导航（下一页、上一页、第一页）</li>
 *   <li>SQL offset calculation - SQL 偏移量计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple page request
 * PageRequest request = PageRequest.of(1, 20);
 *
 * // With sort
 * PageRequest request = PageRequest.of(1, 20, "name", "asc");
 *
 * // Navigation
 * PageRequest next = request.next();
 * int offset = request.getOffset();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (sort defaults to unsorted) - 空值安全: 是（排序默认为无排序）</li>
 * </ul>
 *
 * @param page the page number (1-based) | 页码（从1开始）
 * @param size the page size | 页大小
 * @param sort the sort criteria | 排序条件
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public record PageRequest(
    int page,
    int size,
    Sort sort
) {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 1000;

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造函数
     */
    public PageRequest {
        page = Math.max(DEFAULT_PAGE, page);
        size = Math.max(1, Math.min(size, MAX_SIZE));
        sort = sort != null ? sort : Sort.unsorted();
    }

    /**
     * Create page request
     * 创建分页请求
     *
     * @param page the page number | 页码
     * @param size the page size | 页大小
     * @return the request | 请求
     */
    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, Sort.unsorted());
    }

    /**
     * Create page request with sort
     * 创建带排序的分页请求
     *
     * @param page the page number | 页码
     * @param size the page size | 页大小
     * @param sort the sort criteria | 排序条件
     * @return the request | 请求
     */
    public static PageRequest of(int page, int size, Sort sort) {
        return new PageRequest(page, size, sort);
    }

    /**
     * Create page request with sort string
     * 创建带排序字符串的分页请求
     *
     * @param page the page number | 页码
     * @param size the page size | 页大小
     * @param sortBy the sort field | 排序字段
     * @param sortOrder the sort order (asc/desc) | 排序顺序
     * @return the request | 请求
     */
    public static PageRequest of(int page, int size, String sortBy, String sortOrder) {
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isBlank()) {
            sort = "desc".equalsIgnoreCase(sortOrder) ? Sort.desc(sortBy) : Sort.asc(sortBy);
        }
        return new PageRequest(page, size, sort);
    }

    /**
     * Get default page request
     * 获取默认分页请求
     *
     * @return the request | 请求
     */
    public static PageRequest defaultRequest() {
        return new PageRequest(DEFAULT_PAGE, DEFAULT_SIZE, Sort.unsorted());
    }

    /**
     * Get first page request
     * 获取第一页请求
     *
     * @param size the page size | 页大小
     * @return the request | 请求
     */
    public static PageRequest first(int size) {
        return new PageRequest(1, size, Sort.unsorted());
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
     * Check if has sort
     * 检查是否有排序
     *
     * @return true if has sort | 如果有排序返回true
     */
    public boolean hasSort() {
        return sort != null && sort.isSorted();
    }

    /**
     * Get next page request
     * 获取下一页请求
     *
     * @return the next page request | 下一页请求
     */
    public PageRequest next() {
        return new PageRequest(page + 1, size, sort);
    }

    /**
     * Get previous page request
     * 获取上一页请求
     *
     * @return the previous page request | 上一页请求
     */
    public PageRequest previous() {
        return new PageRequest(Math.max(1, page - 1), size, sort);
    }

    /**
     * Get first page request
     * 获取第一页请求
     *
     * @return the first page request | 第一页请求
     */
    public PageRequest first() {
        return new PageRequest(1, size, sort);
    }

    /**
     * With different page
     * 使用不同的页码
     *
     * @param newPage the new page number | 新页码
     * @return the new request | 新请求
     */
    public PageRequest withPage(int newPage) {
        return new PageRequest(newPage, size, sort);
    }

    /**
     * With different size
     * 使用不同的页大小
     *
     * @param newSize the new size | 新页大小
     * @return the new request | 新请求
     */
    public PageRequest withSize(int newSize) {
        return new PageRequest(page, newSize, sort);
    }

    /**
     * With different sort
     * 使用不同的排序
     *
     * @param newSort the new sort | 新排序
     * @return the new request | 新请求
     */
    public PageRequest withSort(Sort newSort) {
        return new PageRequest(page, size, newSort);
    }

    /**
     * With ascending sort
     * 使用升序
     *
     * @param property the property | 属性
     * @return the new request | 新请求
     */
    public PageRequest sortAsc(String property) {
        return new PageRequest(page, size, Sort.asc(property));
    }

    /**
     * With descending sort
     * 使用降序
     *
     * @param property the property | 属性
     * @return the new request | 新请求
     */
    public PageRequest sortDesc(String property) {
        return new PageRequest(page, size, Sort.desc(property));
    }
}
