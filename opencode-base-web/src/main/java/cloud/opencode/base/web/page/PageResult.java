package cloud.opencode.base.web.page;

import java.util.List;
import java.util.function.Function;

/**
 * Page Result
 * 分页结果
 *
 * <p>Paginated list result.</p>
 * <p>分页列表结果。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable paginated result container - 不可变的分页结果容器</li>
 *   <li>Page navigation helpers (hasNext, hasPrevious) - 分页导航辅助方法</li>
 *   <li>Functional mapping support - 函数式映射支持</li>
 *   <li>Delegate methods for page metadata - 分页元数据的委托方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create page result
 * PageResult<User> result = PageResult.of(userList, 100, 1, 10);
 *
 * // Map items
 * PageResult<UserDTO> dtoResult = result.map(user -> toDTO(user));
 *
 * // Check navigation
 * boolean hasMore = result.hasNext();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (null items default to empty list) - 空值安全: 是（null 项默认为空列表）</li>
 * </ul>
 *
 * @param <T> the item type | 项类型
 * @param items the items | 项列表
 * @param pageInfo the page info | 分页信息
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public record PageResult<T>(
    List<T> items,
    PageInfo pageInfo
) {

    /**
     * Compact constructor
     * 紧凑构造函数
     */
    public PageResult {
        items = items != null ? List.copyOf(items) : List.of();
        pageInfo = pageInfo != null ? pageInfo : PageInfo.empty();
    }

    /**
     * Create page result
     * 创建分页结果
     *
     * @param items the items | 项列表
     * @param pageInfo the page info | 分页信息
     * @param <T> the item type | 项类型
     * @return the result | 结果
     */
    public static <T> PageResult<T> of(List<T> items, PageInfo pageInfo) {
        return new PageResult<>(items, pageInfo);
    }

    /**
     * Create page result
     * 创建分页结果
     *
     * @param items the items | 项列表
     * @param total the total count | 总数
     * @param page the page number | 页码
     * @param size the page size | 页大小
     * @param <T> the item type | 项类型
     * @return the result | 结果
     */
    public static <T> PageResult<T> of(List<T> items, long total, int page, int size) {
        return new PageResult<>(items, PageInfo.of(page, size, total));
    }

    /**
     * Create page result from request
     * 从请求创建分页结果
     *
     * @param items the items | 项列表
     * @param total the total count | 总数
     * @param request the page request | 分页请求
     * @param <T> the item type | 项类型
     * @return the result | 结果
     */
    public static <T> PageResult<T> of(List<T> items, long total, PageRequest request) {
        return new PageResult<>(items, PageInfo.of(request.page(), request.size(), total));
    }

    /**
     * Create empty result
     * 创建空结果
     *
     * @param <T> the item type | 项类型
     * @return the result | 结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(List.of(), PageInfo.empty());
    }

    /**
     * Create empty result with page info
     * 创建带分页信息的空结果
     *
     * @param page the page number | 页码
     * @param size the page size | 页大小
     * @param <T> the item type | 项类型
     * @return the result | 结果
     */
    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(List.of(), PageInfo.of(page, size, 0));
    }

    /**
     * Create single item result
     * 创建单项结果
     *
     * @param item the single item | 单个项
     * @param <T> the item type | 项类型
     * @return the result with single item | 包含单项的结果
     */
    public static <T> PageResult<T> single(T item) {
        if (item == null) {
            return empty();
        }
        return new PageResult<>(List.of(item), PageInfo.of(1, 1, 1));
    }

    /**
     * Create single item result from list (takes first item)
     * 从列表创建单项结果（取第一项）
     *
     * @param list the list | 列表
     * @param <T> the item type | 项类型
     * @return the result with single item | 包含单项的结果
     */
    public static <T> PageResult<T> single(List<T> list) {
        if (list == null || list.isEmpty()) {
            return empty();
        }
        return single(list.getFirst());
    }

    // === Delegate methods ===

    /**
     * Get page number
     * 获取页码
     *
     * @return the page number | 页码
     */
    public int getPage() {
        return pageInfo.page();
    }

    /**
     * Get page size
     * 获取页大小
     *
     * @return the page size | 页大小
     */
    public int getSize() {
        return pageInfo.size();
    }

    /**
     * Get total count
     * 获取总数
     *
     * @return the total count | 总数
     */
    public long getTotal() {
        return pageInfo.total();
    }

    /**
     * Get total pages
     * 获取总页数
     *
     * @return the total pages | 总页数
     */
    public int getTotalPages() {
        return pageInfo.totalPages();
    }

    /**
     * Check if has next page
     * 检查是否有下一页
     *
     * @return true if has next | 如果有下一页返回true
     */
    public boolean hasNext() {
        return pageInfo.hasNext();
    }

    /**
     * Check if has previous page
     * 检查是否有上一页
     *
     * @return true if has previous | 如果有上一页返回true
     */
    public boolean hasPrevious() {
        return pageInfo.hasPrevious();
    }

    /**
     * Check if first page
     * 检查是否第一页
     *
     * @return true if first | 如果是第一页返回true
     */
    public boolean isFirst() {
        return pageInfo.isFirst();
    }

    /**
     * Check if last page
     * 检查是否最后一页
     *
     * @return true if last | 如果是最后一页返回true
     */
    public boolean isLast() {
        return pageInfo.isLast();
    }

    /**
     * Check if empty
     * 检查是否为空
     *
     * @return true if empty | 如果为空返回true
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Get item count
     * 获取项数量
     *
     * @return the item count | 项数量
     */
    public int getCount() {
        return items.size();
    }

    /**
     * Map items to another type
     * 将项映射为另一种类型
     *
     * @param mapper the mapper function | 映射函数
     * @param <R> the result type | 结果类型
     * @return the new page result | 新分页结果
     */
    public <R> PageResult<R> map(Function<T, R> mapper) {
        List<R> mappedItems = items.stream()
            .map(mapper)
            .toList();
        return new PageResult<>(mappedItems, pageInfo);
    }

    /**
     * Map entire list to another type
     * 转换整个列表为另一种类型
     *
     * @param mapper the list mapper function | 列表映射函数
     * @param <R> the result type | 结果类型
     * @return the new page result | 新分页结果
     */
    public <R> PageResult<R> mapList(Function<List<T>, List<R>> mapper) {
        return new PageResult<>(mapper.apply(items), pageInfo);
    }

    /**
     * Get first item
     * 获取第一项
     *
     * @return the first item or null | 第一项或null
     */
    public T getFirst() {
        return items.isEmpty() ? null : items.getFirst();
    }

    /**
     * Get last item
     * 获取最后一项
     *
     * @return the last item or null | 最后一项或null
     */
    public T getLast() {
        return items.isEmpty() ? null : items.getLast();
    }
}
