package cloud.opencode.base.deepclone.internal;

import java.util.*;

/**
 * Immutable Detector - Detects JDK immutable collection types
 * 不可变检测器 - 检测 JDK 不可变集合类型
 *
 * <p>Detects JDK immutable/unmodifiable collections such as {@code List.of()},
 * {@code Set.of()}, {@code Map.of()}, and {@code Collections.unmodifiable*()}.
 * These types do not need deep cloning and can be safely referenced.</p>
 * <p>检测 JDK 不可变/不可修改集合，如 {@code List.of()}、{@code Set.of()}、
 * {@code Map.of()} 和 {@code Collections.unmodifiable*()}。
 * 这些类型不需要深度克隆，可以安全地引用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Detects List.of(), Set.of(), Map.of() results - 检测 List.of()、Set.of()、Map.of() 结果</li>
 *   <li>Detects Collections.unmodifiable* wrappers - 检测 Collections.unmodifiable* 包装器</li>
 *   <li>Detects Collections.empty* instances - 检测 Collections.empty* 实例</li>
 *   <li>Detects Collections.singleton* instances - 检测 Collections.singleton* 实例</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless, uses class name matching) - 线程安全: 是（无状态，使用类名匹配）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.3
 */
public final class ImmutableDetector {

    /**
     * Known immutable collection class names
     * 已知不可变集合类名
     */
    private static final Set<String> IMMUTABLE_CLASS_NAMES;

    static {
        Set<String> names = new HashSet<>();

        // JDK immutable collections (List.of, Set.of, Map.of etc.)
        // These are internal JDK classes with names like java.util.ImmutableCollections$*
        addClassName(names, List.of());
        addClassName(names, List.of(1));
        addClassName(names, List.of(1, 2));
        addClassName(names, Set.of());
        addClassName(names, Set.of(1));
        addClassName(names, Set.of(1, 2));
        addClassName(names, Map.of());
        addClassName(names, Map.of("k", "v"));
        addClassName(names, Map.of("a", 1, "b", 2));

        // Collections.unmodifiable* wrappers
        addClassName(names, Collections.unmodifiableList(new ArrayList<>()));
        addClassName(names, Collections.unmodifiableSet(new HashSet<>()));
        addClassName(names, Collections.unmodifiableMap(new HashMap<>()));
        addClassName(names, Collections.unmodifiableSortedSet(new TreeSet<>()));
        addClassName(names, Collections.unmodifiableSortedMap(new TreeMap<>()));
        addClassName(names, Collections.unmodifiableNavigableSet(new TreeSet<>()));
        addClassName(names, Collections.unmodifiableNavigableMap(new TreeMap<>()));
        addClassName(names, Collections.unmodifiableCollection(new ArrayList<>()));
        addClassName(names, Collections.unmodifiableSequencedCollection(new ArrayList<>()));
        addClassName(names, Collections.unmodifiableSequencedSet(new LinkedHashSet<>()));
        addClassName(names, Collections.unmodifiableSequencedMap(new LinkedHashMap<>()));

        // Collections.empty*
        addClassName(names, Collections.emptyList());
        addClassName(names, Collections.emptySet());
        addClassName(names, Collections.emptyMap());
        addClassName(names, Collections.emptySortedSet());
        addClassName(names, Collections.emptySortedMap());
        addClassName(names, Collections.emptyNavigableSet());
        addClassName(names, Collections.emptyNavigableMap());

        // Collections.singleton*
        addClassName(names, Collections.singletonList(1));
        addClassName(names, Collections.singleton(1));
        addClassName(names, Collections.singletonMap("k", 1));

        IMMUTABLE_CLASS_NAMES = Set.copyOf(names);
    }

    private ImmutableDetector() {
        // Utility class
    }

    /**
     * Adds the class name of the given object to the set
     * 将给定对象的类名添加到集合中
     *
     * @param names the set to add to | 要添加到的集合
     * @param obj   the object whose class name to add | 要添加类名的对象
     */
    private static void addClassName(Set<String> names, Object obj) {
        names.add(obj.getClass().getName());
    }

    /**
     * Checks if the given object is a JDK immutable/unmodifiable collection
     * 检查给定对象是否为 JDK 不可变/不可修改集合
     *
     * @param obj the object to check | 要检查的对象
     * @return true if the object is an immutable collection | 如果是不可变集合返回 true
     */
    public static boolean isImmutableCollection(Object obj) {
        if (obj == null) {
            return false;
        }
        return IMMUTABLE_CLASS_NAMES.contains(obj.getClass().getName());
    }

    /**
     * Checks if the given class is a JDK immutable/unmodifiable collection type
     * 检查给定类是否为 JDK 不可变/不可修改集合类型
     *
     * @param type the type to check | 要检查的类型
     * @return true if the type is an immutable collection type | 如果是不可变集合类型返回 true
     */
    public static boolean isImmutableCollectionType(Class<?> type) {
        if (type == null) {
            return false;
        }
        return IMMUTABLE_CLASS_NAMES.contains(type.getName());
    }
}
