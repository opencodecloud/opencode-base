package cloud.opencode.base.functional.optics;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Lens - Functional lens for immutable data access and modification
 * Lens - 用于不可变数据访问和修改的函数式透镜
 *
 * <p>A lens provides a way to focus on a part of a data structure,
 * allowing both reading and functional updating of that part.
 * Particularly useful for nested immutable data structures like records.</p>
 * <p>透镜提供了一种聚焦数据结构某部分的方式，允许读取和函数式更新该部分。
 * 对于嵌套的不可变数据结构（如 record）特别有用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Get: read a part of a structure - 获取: 读取结构的一部分</li>
 *   <li>Set: create a copy with modified part - 设置: 创建带修改部分的副本</li>
 *   <li>Modify: transform a part - 修改: 转换一部分</li>
 *   <li>Compose: combine lenses - 组合: 组合透镜</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Define a lens for Person.name
 * Lens<Person, String> nameLens = Lens.of(
 *     Person::name,
 *     (person, name) -> new Person(name, person.age())
 * );
 *
 * // Read value
 * String name = nameLens.get(person);
 *
 * // Create modified copy
 * Person updated = nameLens.set(person, "Alice");
 *
 * // Transform value
 * Person upperCased = nameLens.modify(person, String::toUpperCase);
 *
 * // Compose lenses for nested access
 * Lens<Company, String> ceoNameLens = companyToPersonLens.andThen(nameLens);
 *
 * // Record lens example
 * record Address(String city, String street) {}
 * record Person(String name, Address address) {}
 *
 * Lens<Person, Address> addressLens = Lens.of(
 *     Person::address,
 *     (p, addr) -> new Person(p.name(), addr)
 * );
 *
 * Lens<Address, String> cityLens = Lens.of(
 *     Address::city,
 *     (addr, city) -> new Address(city, addr.street())
 * );
 *
 * // Compose to access nested field
 * Lens<Person, String> personCityLens = addressLens.andThen(cityLens);
 * Person moved = personCityLens.set(person, "New York");
 * }</pre>
 *
 * <p><strong>Laws | 透镜定律:</strong></p>
 * <ul>
 *   <li>Get-Set: set(s, get(s)) == s - 获取-设置: set(s, get(s)) == s</li>
 *   <li>Set-Get: get(set(s, a)) == a - 设置-获取: get(set(s, a)) == a</li>
 *   <li>Set-Set: set(set(s, a), b) == set(s, b) - 设置-设置: set(set(s, a), b) == set(s, b)</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Get: O(1) - 获取: O(1)</li>
 *   <li>Set: O(1) (assuming O(1) copy) - 设置: O(1) (假设 O(1) 复制)</li>
 *   <li>Compose: O(depth) for nested access - 组合: O(深度) 用于嵌套访问</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Null-safe: Depends on getter/setter - 空值安全: 取决于 getter/setter</li>
 * </ul>
 *
 * @param <S> source/whole type - 源/整体类型
 * @param <A> target/part type - 目标/部分类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class Lens<S, A> {

    private final Function<S, A> getter;
    private final BiFunction<S, A, S> setter;

    private Lens(Function<S, A> getter, BiFunction<S, A, S> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a lens from getter and setter
     * 从 getter 和 setter 创建透镜
     *
     * @param getter function to get the part - 获取部分的函数
     * @param setter function to set the part (returns new whole) - 设置部分的函数（返回新整体）
     * @param <S>    source/whole type - 源/整体类型
     * @param <A>    target/part type - 目标/部分类型
     * @return lens
     */
    public static <S, A> Lens<S, A> of(Function<S, A> getter, BiFunction<S, A, S> setter) {
        return new Lens<>(getter, setter);
    }

    /**
     * Create an identity lens
     * 创建恒等透镜
     *
     * @param <S> type - 类型
     * @return identity lens
     */
    public static <S> Lens<S, S> identity() {
        return new Lens<>(Function.identity(), (s, a) -> a);
    }

    // ==================== Core Operations | 核心操作 ====================

    /**
     * Get the focused value from the source
     * 从源获取聚焦的值
     *
     * @param source the source structure - 源结构
     * @return the focused part
     */
    public A get(S source) {
        return getter.apply(source);
    }

    /**
     * Set the focused value, returning a new source
     * 设置聚焦的值，返回新的源
     *
     * @param source the source structure - 源结构
     * @param value  the new value - 新值
     * @return new source with updated part
     */
    public S set(S source, A value) {
        return setter.apply(source, value);
    }

    /**
     * Modify the focused value using a function
     * 使用函数修改聚焦的值
     *
     * @param source   the source structure - 源结构
     * @param modifier function to modify the value - 修改值的函数
     * @return new source with modified part
     */
    public S modify(S source, UnaryOperator<A> modifier) {
        return set(source, modifier.apply(get(source)));
    }

    /**
     * Create a modifier function for this lens
     * 为此透镜创建修改器函数
     *
     * @param modifier function to modify the value - 修改值的函数
     * @return function that modifies source
     */
    public UnaryOperator<S> modifier(UnaryOperator<A> modifier) {
        return source -> modify(source, modifier);
    }

    // ==================== Composition | 组合 ====================

    /**
     * Compose with another lens (this lens first, then other)
     * 与另一个透镜组合（先应用此透镜，然后应用其他）
     *
     * <p>Creates a lens that focuses on a part of the part.</p>
     * <p>创建聚焦于部分的部分的透镜。</p>
     *
     * @param other lens to compose with - 要组合的透镜
     * @param <B>   deeper target type - 更深的目标类型
     * @return composed lens
     */
    public <B> Lens<S, B> andThen(Lens<A, B> other) {
        return new Lens<>(
            source -> other.get(this.get(source)),
            (source, b) -> this.set(source, other.set(this.get(source), b))
        );
    }

    /**
     * Compose with another lens (other lens first, then this)
     * 与另一个透镜组合（先应用其他，然后应用此透镜）
     *
     * @param other lens to compose with - 要组合的透镜
     * @param <T>   outer source type - 外部源类型
     * @return composed lens
     */
    public <T> Lens<T, A> compose(Lens<T, S> other) {
        return other.andThen(this);
    }

    // ==================== Conversions | 转换 ====================

    /**
     * Convert to an OptionalLens
     * 转换为 OptionalLens
     *
     * @return optional lens
     */
    public OptionalLens<S, A> asOptional() {
        return OptionalLens.of(
            source -> java.util.Optional.ofNullable(get(source)),
            this::set
        );
    }

    /**
     * Get the getter function
     * 获取 getter 函数
     *
     * @return getter function
     */
    public Function<S, A> getter() {
        return getter;
    }

    /**
     * Get the setter function
     * 获取 setter 函数
     *
     * @return setter function
     */
    public BiFunction<S, A, S> setter() {
        return setter;
    }

    // ==================== Record Support | Record 支持 ====================

    /**
     * Create a lens for a record component
     * 为 record 组件创建透镜
     *
     * <p>Helper for creating lenses for record fields.</p>
     * <p>为 record 字段创建透镜的辅助方法。</p>
     *
     * @param getter      record component accessor - record 组件访问器
     * @param constructor record constructor with updated field - 带更新字段的 record 构造器
     * @param <S>         record type - record 类型
     * @param <A>         component type - 组件类型
     * @return lens for record component
     */
    public static <S, A> Lens<S, A> forRecord(Function<S, A> getter,
                                               BiFunction<S, A, S> constructor) {
        return of(getter, constructor);
    }

    @Override
    public String toString() {
        return "Lens[" + getter + " -> " + setter + "]";
    }
}
