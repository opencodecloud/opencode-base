/**
 * OpenCode Base Collections - Primitive Collections
 * OpenCode Base Collections - 原始类型集合
 *
 * <p>This package provides collections for primitive types that avoid
 * boxing overhead.</p>
 * <p>此包提供避免装箱开销的原始类型集合。</p>
 *
 * <h2>Package Contents | 包内容</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.collections.primitive.IntList} - Primitive int list | 原始 int 列表</li>
 *   <li>{@link cloud.opencode.base.collections.primitive.LongList} - Primitive long list | 原始 long 列表</li>
 *   <li>{@link cloud.opencode.base.collections.primitive.DoubleList} - Primitive double list | 原始 double 列表</li>
 * </ul>
 *
 * <h2>Usage Examples | 使用示例</h2>
 * <pre>{@code
 * // Create and use IntList - 创建和使用 IntList
 * IntList ints = IntList.of(1, 2, 3, 4, 5);
 * int sum = ints.stream().sum();
 *
 * // Create and use LongList - 创建和使用 LongList
 * LongList longs = LongList.range(0, 1000000);
 * long max = longs.max();
 *
 * // Create and use DoubleList - 创建和使用 DoubleList
 * DoubleList doubles = DoubleList.of(1.5, 2.5, 3.5);
 * double avg = doubles.average();
 * }</pre>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
package cloud.opencode.base.collections.primitive;
