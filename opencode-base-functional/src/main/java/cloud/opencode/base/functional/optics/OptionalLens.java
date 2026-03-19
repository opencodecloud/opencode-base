package cloud.opencode.base.functional.optics;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * OptionalLens - Functional lens for optional data access
 * OptionalLens - 用于可选数据访问的函数式透镜
 *
 * <p>A lens variant that handles cases where the focused part might not exist.
 * The getter returns Optional, and set/modify operations only apply if
 * the target exists.</p>
 * <p>处理聚焦部分可能不存在的情况的透镜变体。getter 返回 Optional，
 * set/modify 操作仅在目标存在时应用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Optional get - 可选获取</li>
 *   <li>Safe set/modify - 安全设置/修改</li>
 *   <li>Composable with Lens - 可与 Lens 组合</li>
 *   <li>Null-safe operations - 空值安全操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Define optional lens for nullable field
 * OptionalLens<User, Address> addressLens = OptionalLens.of(
 *     user -> Optional.ofNullable(user.address()),
 *     (user, addr) -> new User(user.name(), addr)
 * );
 *
 * // Safe get
 * Optional<Address> address = addressLens.get(user);
 *
 * // Set (always works)
 * User updated = addressLens.set(user, newAddress);
 *
 * // Modify only if present
 * User modified = addressLens.modify(user, Address::normalize);
 *
 * // Compose with regular lens
 * Lens<Address, String> cityLens = ...;
 * OptionalLens<User, String> userCityLens = addressLens.andThen(cityLens);
 *
 * // For collections
 * OptionalLens<Team, Member> firstMember = OptionalLens.of(
 *     team -> team.members().stream().findFirst(),
 *     (team, member) -> team.withFirstMember(member)
 * );
 * }</pre>
 *
 * <p><strong>Use Cases | 使用场景:</strong></p>
 * <ul>
 *   <li>Nullable fields - 可空字段</li>
 *   <li>Collection elements - 集合元素</li>
 *   <li>Map values - Map 值</li>
 *   <li>Conditional paths - 条件路径</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Get: O(1) + Optional wrapping - 获取: O(1) + Optional 包装</li>
 *   <li>Set: O(1) - 设置: O(1)</li>
 *   <li>Modify: O(1) with short-circuit - 修改: O(1) 带短路</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @param <S> source/whole type - 源/整体类型
 * @param <A> target/part type - 目标/部分类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class OptionalLens<S, A> {

    private final Function<S, Optional<A>> getter;
    private final BiFunction<S, A, S> setter;

    private OptionalLens(Function<S, Optional<A>> getter, BiFunction<S, A, S> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create an optional lens from getter and setter
     * 从 getter 和 setter 创建可选透镜
     *
     * @param getter function to get the optional part - 获取可选部分的函数
     * @param setter function to set the part - 设置部分的函数
     * @param <S>    source type - 源类型
     * @param <A>    target type - 目标类型
     * @return optional lens
     */
    public static <S, A> OptionalLens<S, A> of(Function<S, Optional<A>> getter,
                                                BiFunction<S, A, S> setter) {
        return new OptionalLens<>(getter, setter);
    }

    /**
     * Create an optional lens from nullable getter
     * 从可空 getter 创建可选透镜
     *
     * @param getter function to get nullable part - 获取可空部分的函数
     * @param setter function to set the part - 设置部分的函数
     * @param <S>    source type - 源类型
     * @param <A>    target type - 目标类型
     * @return optional lens
     */
    public static <S, A> OptionalLens<S, A> ofNullable(Function<S, A> getter,
                                                        BiFunction<S, A, S> setter) {
        return new OptionalLens<>(
            source -> Optional.ofNullable(getter.apply(source)),
            setter
        );
    }

    /**
     * Create from a regular lens
     * 从常规透镜创建
     *
     * @param lens the lens - 透镜
     * @param <S>  source type - 源类型
     * @param <A>  target type - 目标类型
     * @return optional lens
     */
    public static <S, A> OptionalLens<S, A> fromLens(Lens<S, A> lens) {
        return lens.asOptional();
    }

    // ==================== Core Operations | 核心操作 ====================

    /**
     * Get the optional focused value
     * 获取可选的聚焦值
     *
     * @param source the source structure - 源结构
     * @return Optional containing the value if present
     */
    public Optional<A> get(S source) {
        return getter.apply(source);
    }

    /**
     * Get the focused value or default
     * 获取聚焦值或默认值
     *
     * @param source       the source structure - 源结构
     * @param defaultValue default if not present - 不存在时的默认值
     * @return the value or default
     */
    public A getOrElse(S source, A defaultValue) {
        return get(source).orElse(defaultValue);
    }

    /**
     * Check if the focused value is present
     * 检查聚焦值是否存在
     *
     * @param source the source structure - 源结构
     * @return true if present
     */
    public boolean isPresent(S source) {
        return get(source).isPresent();
    }

    /**
     * Set the focused value
     * 设置聚焦值
     *
     * @param source the source structure - 源结构
     * @param value  the new value - 新值
     * @return new source with updated part
     */
    public S set(S source, A value) {
        return setter.apply(source, value);
    }

    /**
     * Set the focused value if present in Optional
     * 如果 Optional 中存在则设置聚焦值
     *
     * @param source the source structure - 源结构
     * @param value  optional new value - 可选的新值
     * @return new source with updated part, or original if value empty
     */
    public S setIfPresent(S source, Optional<A> value) {
        return value.map(v -> set(source, v)).orElse(source);
    }

    /**
     * Modify the focused value if present
     * 如果存在则修改聚焦值
     *
     * @param source   the source structure - 源结构
     * @param modifier function to modify the value - 修改值的函数
     * @return new source with modified part, or original if not present
     */
    public S modify(S source, UnaryOperator<A> modifier) {
        return get(source)
            .map(a -> set(source, modifier.apply(a)))
            .orElse(source);
    }

    /**
     * Modify the focused value, or set default if not present
     * 修改聚焦值，如果不存在则设置默认值
     *
     * @param source       the source structure - 源结构
     * @param modifier     function to modify the value - 修改值的函数
     * @param defaultValue default to set if not present - 不存在时设置的默认值
     * @return new source
     */
    public S modifyOrSet(S source, UnaryOperator<A> modifier, A defaultValue) {
        return get(source)
            .map(a -> set(source, modifier.apply(a)))
            .orElseGet(() -> set(source, defaultValue));
    }

    // ==================== Composition | 组合 ====================

    /**
     * Compose with another optional lens
     * 与另一个可选透镜组合
     *
     * @param other lens to compose with - 要组合的透镜
     * @param <B>   deeper target type - 更深的目标类型
     * @return composed optional lens
     */
    public <B> OptionalLens<S, B> andThen(OptionalLens<A, B> other) {
        return new OptionalLens<>(
            source -> this.get(source).flatMap(other::get),
            (source, b) -> this.modify(source, a -> other.set(a, b))
        );
    }

    /**
     * Compose with a regular lens
     * 与常规透镜组合
     *
     * @param other lens to compose with - 要组合的透镜
     * @param <B>   deeper target type - 更深的目标类型
     * @return composed optional lens
     */
    public <B> OptionalLens<S, B> andThen(Lens<A, B> other) {
        return new OptionalLens<>(
            source -> this.get(source).map(other::get),
            (source, b) -> this.modify(source, a -> other.set(a, b))
        );
    }

    /**
     * Compose with this lens (other first, then this)
     * 与此透镜组合（先其他，然后此）
     *
     * @param other lens to compose with - 要组合的透镜
     * @param <T>   outer source type - 外部源类型
     * @return composed optional lens
     */
    public <T> OptionalLens<T, A> compose(OptionalLens<T, S> other) {
        return other.andThen(this);
    }

    /**
     * Compose with a regular lens (other first, then this)
     * 与常规透镜组合（先其他，然后此）
     *
     * @param other lens to compose with - 要组合的透镜
     * @param <T>   outer source type - 外部源类型
     * @return composed optional lens
     */
    public <T> OptionalLens<T, A> compose(Lens<T, S> other) {
        return new OptionalLens<>(
            source -> this.get(other.get(source)),
            (source, a) -> other.modify(source, s -> this.set(s, a))
        );
    }

    // ==================== Conversions | 转换 ====================

    /**
     * Get the getter function
     * 获取 getter 函数
     *
     * @return getter function
     */
    public Function<S, Optional<A>> getter() {
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

    @Override
    public String toString() {
        return "OptionalLens[" + getter + " -> " + setter + "]";
    }
}
