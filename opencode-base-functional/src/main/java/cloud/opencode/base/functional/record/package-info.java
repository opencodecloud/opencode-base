/**
 * Record Utilities - Functional utilities for Java Records
 * Record 工具 - Java Record 的函数式工具
 *
 * <p>Provides utilities for working with Java Records in a functional style,
 * including destructuring, field extraction, and tuple conversion.</p>
 * <p>提供以函数式风格处理 Java Record 的工具，包括解构、字段提取和元组转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.functional.record.RecordUtil} - Record utilities</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * record Point(int x, int y) {}
 * record Person(String name, int age, String email) {}
 *
 * // Destructure record
 * Point p = new Point(10, 20);
 * int sum = RecordUtil.destructure(p, (x, y) -> x + y);
 *
 * // Convert to tuple
 * Pair<Integer, Integer> tuple = RecordUtil.toPair(p);
 *
 * // Field extractor
 * Function<Person, String> getName = RecordUtil.field(Person.class, "name");
 * String name = getName.apply(person);
 *
 * // Comparing by fields
 * Comparator<Person> comparator = RecordUtil.comparing(
 *     Person.class, "name", "age");
 * }</pre>
 *
 * <p><strong>Integration | 集成:</strong></p>
 * <p>Uses Core module's Tuple types (Pair, Triple, Quadruple).</p>
 * <p>使用 Core 模块的元组类型（Pair、Triple、Quadruple）。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
package cloud.opencode.base.functional.record;
