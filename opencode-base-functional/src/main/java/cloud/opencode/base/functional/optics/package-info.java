/**
 * Optics - Lens and other optical types for immutable data
 * 光学类型 - 用于不可变数据的 Lens 和其他光学类型
 *
 * <p>Provides Lens and related optical types for accessing and updating
 * deeply nested immutable data structures in a composable way.</p>
 * <p>提供 Lens 和相关光学类型，以可组合的方式访问和更新深层嵌套的不可变数据结构。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.functional.optics.Lens} - Getter/setter composition</li>
 *   <li>{@link cloud.opencode.base.functional.optics.OptionalLens} - Optional value lens</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * record Address(String city, String street) {}
 * record Person(String name, Address address) {}
 *
 * // Create lenses
 * Lens<Person, Address> addressLens = Lens.of(
 *     Person::address,
 *     (p, a) -> new Person(p.name(), a));
 *
 * Lens<Address, String> cityLens = Lens.of(
 *     Address::city,
 *     (a, c) -> new Address(c, a.street()));
 *
 * // Compose lenses
 * Lens<Person, String> personCityLens = addressLens.compose(cityLens);
 *
 * // Update deeply nested value
 * Person person = new Person("Alice", new Address("NYC", "5th Ave"));
 * Person updated = personCityLens.modify(person, String::toUpperCase);
 * // Result: Person("Alice", Address("NYC", "5th Ave"))
 * }</pre>
 *
 * <p><strong>Benefits | 优势:</strong></p>
 * <ul>
 *   <li>Type-safe nested updates - 类型安全的嵌套更新</li>
 *   <li>Composable accessors - 可组合的访问器</li>
 *   <li>Immutable data manipulation - 不可变数据操作</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
package cloud.opencode.base.functional.optics;
