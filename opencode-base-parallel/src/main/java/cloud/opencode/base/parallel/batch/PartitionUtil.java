package cloud.opencode.base.parallel.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Partition Util - List Partition Utility
 * 分区工具 - 列表分区工具
 *
 * <p>Utility class for partitioning lists into smaller chunks for batch processing.</p>
 * <p>用于将列表分割成较小块以进行批处理的工具类。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7);
 * List<List<Integer>> partitions = PartitionUtil.partition(list, 3);
 * // [[1, 2, 3], [4, 5, 6], [7]]
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>List partitioning by size - 按大小分割列表</li>
 *   <li>Stream-based lazy partitioning - 基于Stream的延迟分割</li>
 *   <li>Collection partitioning - 集合分割</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (utility class, stateless) - 线程安全: 是（工具类，无状态）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n/s) where s is the partition size - creates ceil(n/s) subList views in one loop - 时间复杂度: O(n/s)，s 为分区大小 - 单次循环创建 ceil(n/s) 个 subList 视图</li>
 *   <li>Space complexity: O(n/s) - result list of partition views; subList is a view with O(1) overhead per partition - 空间复杂度: O(n/s) - 分区视图的结果列表；subList 为视图，每个分区 O(1) 开销</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
public final class PartitionUtil {

    private PartitionUtil() {
        // Static utility class
    }

    /**
     * Partitions a list into smaller lists of specified size.
     * 将列表分割成指定大小的较小列表。
     *
     * @param list the list to partition - 要分割的列表
     * @param size the partition size - 分区大小
     * @param <T>  the element type - 元素类型
     * @return the list of partitions - 分区列表
     * @throws IllegalArgumentException if size is not positive - 如果大小不是正数
     */
    public static <T> List<List<T>> partition(List<T> list, int size) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Partition size must be positive: " + size);
        }

        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return result;
    }

    /**
     * Partitions a collection into smaller lists of specified size.
     * 将集合分割成指定大小的较小列表。
     *
     * @param collection the collection to partition - 要分割的集合
     * @param size       the partition size - 分区大小
     * @param <T>        the element type - 元素类型
     * @return the list of partitions - 分区列表
     */
    public static <T> List<List<T>> partition(Collection<T> collection, int size) {
        if (collection instanceof List<T> list) {
            return partition(list, size);
        }
        return partition(new ArrayList<>(collection), size);
    }

    /**
     * Creates a stream of partitions (lazy evaluation).
     * 创建分区流（惰性求值）。
     *
     * @param list the list to partition - 要分割的列表
     * @param size the partition size - 分区大小
     * @param <T>  the element type - 元素类型
     * @return the stream of partitions - 分区流
     */
    public static <T> Stream<List<T>> partitionStream(List<T> list, int size) {
        if (list == null || list.isEmpty()) {
            return Stream.empty();
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Partition size must be positive: " + size);
        }

        Iterable<List<T>> iterable = () -> new PartitionIterator<>(list, size);
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * Partitions into a specified number of partitions.
     * 分割成指定数量的分区。
     *
     * @param list  the list to partition - 要分割的列表
     * @param count the number of partitions - 分区数量
     * @param <T>   the element type - 元素类型
     * @return the list of partitions - 分区列表
     */
    public static <T> List<List<T>> partitionInto(List<T> list, int count) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Partition count must be positive: " + count);
        }

        int size = (list.size() + count - 1) / count; // Ceiling division
        return partition(list, size);
    }

    /**
     * Calculates the number of partitions.
     * 计算分区数量。
     *
     * @param totalSize     the total size - 总大小
     * @param partitionSize the partition size - 分区大小
     * @return the number of partitions - 分区数量
     */
    public static int partitionCount(int totalSize, int partitionSize) {
        if (totalSize <= 0) {
            return 0;
        }
        if (partitionSize <= 0) {
            throw new IllegalArgumentException("Partition size must be positive: " + partitionSize);
        }
        return (totalSize + partitionSize - 1) / partitionSize;
    }

    /**
     * Gets a specific partition by index.
     * 根据索引获取特定分区。
     *
     * @param list           the list - 列表
     * @param partitionSize  the partition size - 分区大小
     * @param partitionIndex the partition index - 分区索引
     * @param <T>            the element type - 元素类型
     * @return the partition - 分区
     */
    public static <T> List<T> getPartition(List<T> list, int partitionSize, int partitionIndex) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        if (partitionSize <= 0) {
            throw new IllegalArgumentException("Partition size must be positive: " + partitionSize);
        }
        if (partitionIndex < 0) {
            throw new IllegalArgumentException("Partition index must be non-negative: " + partitionIndex);
        }

        int start = partitionIndex * partitionSize;
        if (start >= list.size()) {
            return Collections.emptyList();
        }
        int end = Math.min(start + partitionSize, list.size());
        return list.subList(start, end);
    }

    /**
     * Partition iterator for lazy evaluation.
     * 用于惰性求值的分区迭代器。
     */
    private static class PartitionIterator<T> implements Iterator<List<T>> {
        private final List<T> list;
        private final int size;
        private int index = 0;

        PartitionIterator(List<T> list, int size) {
            this.list = list;
            this.size = size;
        }

        @Override
        public boolean hasNext() {
            return index < list.size();
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            List<T> partition = list.subList(index, Math.min(index + size, list.size()));
            index += size;
            return partition;
        }
    }
}
