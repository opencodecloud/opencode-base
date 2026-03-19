package cloud.opencode.base.rules.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default Fact Store Implementation
 * 默认事实存储实现
 *
 * <p>Thread-safe implementation using ConcurrentHashMap and CopyOnWriteArrayList.</p>
 * <p>使用ConcurrentHashMap和CopyOnWriteArrayList的线程安全实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Named and typed fact storage - 命名和类型化事实存储</li>
 *   <li>Type-based retrieval - 基于类型的检索</li>
 *   <li>Concurrent access support - 并发访问支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FactStore store = new DefaultFactStore();
 * store.add("customer", customerObj);
 * store.add(orderObj);
 * Optional<Order> order = store.get(Order.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public class DefaultFactStore implements FactStore {

    private final Map<String, Object> namedFacts = new ConcurrentHashMap<>();
    private final List<Object> typedFacts = new CopyOnWriteArrayList<>();

    @Override
    public void add(Object fact) {
        if (fact != null) {
            typedFacts.add(fact);
        }
    }

    @Override
    public void add(String name, Object fact) {
        if (name != null && fact != null) {
            namedFacts.put(name, fact);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(Class<T> type) {
        return typedFacts.stream()
                .filter(type::isInstance)
                .map(f -> (T) f)
                .findFirst();
    }

    @Override
    public Object get(String name) {
        return namedFacts.get(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getAll(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Object fact : typedFacts) {
            if (type.isInstance(fact)) {
                result.add((T) fact);
            }
        }
        return result;
    }

    @Override
    public boolean contains(String name) {
        return namedFacts.containsKey(name);
    }

    @Override
    public boolean contains(Class<?> type) {
        return typedFacts.stream().anyMatch(type::isInstance);
    }

    @Override
    public Object remove(String name) {
        return namedFacts.remove(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> removeAll(Class<T> type) {
        List<T> removed = new ArrayList<>();
        typedFacts.removeIf(fact -> {
            if (type.isInstance(fact)) {
                removed.add((T) fact);
                return true;
            }
            return false;
        });
        return removed;
    }

    @Override
    public void clear() {
        namedFacts.clear();
        typedFacts.clear();
    }

    @Override
    public int size() {
        return namedFacts.size() + typedFacts.size();
    }
}
