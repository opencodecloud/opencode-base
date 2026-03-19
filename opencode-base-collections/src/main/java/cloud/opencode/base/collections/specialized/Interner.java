/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Equivalence;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Interner - Object Interning (Pooling) Utility
 * Interner - 对象驻留（池化）工具
 *
 * <p>Provides canonical instances of equivalent objects, similar to String.intern()
 * but for any object type. This reduces memory usage by ensuring that only one
 * instance of each distinct value exists.</p>
 * <p>为等价对象提供规范实例，类似于 String.intern() 但适用于任何对象类型。
 * 通过确保每个不同值只存在一个实例来减少内存使用。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Strong interner - keeps all entries
 * Interner<String> strongInterner = Interner.strong();
 * String canonical = strongInterner.intern("hello");
 *
 * // Weak interner - allows garbage collection
 * Interner<MyClass> weakInterner = Interner.weak();
 * MyClass canonical = weakInterner.intern(new MyClass("data"));
 *
 * // Custom equivalence
 * Interner<String> caseInsensitive = Interner.<String>newBuilder()
 *     .equivalence(Equivalence.from(
 *         String::equalsIgnoreCase,
 *         s -> s.toLowerCase().hashCode()))
 *     .weak()
 *     .build();
 *
 * // With concurrency level
 * Interner<String> concurrent = Interner.<String>newBuilder()
 *     .concurrencyLevel(16)
 *     .strong()
 *     .build();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>intern: O(1) average - intern: O(1) 平均</li>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Memory: Strong keeps all, Weak allows GC - 内存: Strong 保留所有，Weak 允许 GC</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Strong and weak reference interning - 强引用和弱引用驻留</li>
 *   <li>Custom equivalence support - 自定义等价关系支持</li>
 *   <li>Configurable concurrency level - 可配置的并发级别</li>
 *   <li>Automatic garbage collection for weak references - 弱引用自动垃圾回收</li>
 * </ul>
 * @param <E> the type of objects to intern | 要驻留的对象类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public abstract class Interner<E> {

    /**
     * Returns a canonical instance for the given object.
     * 返回给定对象的规范实例。
     *
     * <p>If an equivalent object has been interned before, returns the previously
     * interned instance. Otherwise, interns and returns the given object.</p>
     * <p>如果之前已驻留等价对象，则返回之前驻留的实例。否则，驻留并返回给定对象。</p>
     *
     * @param sample the object to intern | 要驻留的对象
     * @return the canonical instance | 规范实例
     * @throws NullPointerException if sample is null | 如果 sample 为 null
     */
    public abstract E intern(E sample);

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a strong interner that uses equals() for equivalence.
     * 创建使用 equals() 进行等价判断的强驻留器。
     *
     * <p>Strong interners keep all interned objects indefinitely.</p>
     * <p>强驻留器无限期保留所有驻留对象。</p>
     *
     * @param <E> the type | 类型
     * @return a new strong interner | 新的强驻留器
     */
    public static <E> Interner<E> strong() {
        return Interner.<E>newBuilder().strong().build();
    }

    /**
     * Creates a weak interner that uses equals() for equivalence.
     * 创建使用 equals() 进行等价判断的弱驻留器。
     *
     * <p>Weak interners allow garbage collection of interned objects
     * when they are no longer referenced elsewhere.</p>
     * <p>弱驻留器允许在驻留对象不再被其他地方引用时进行垃圾回收。</p>
     *
     * @param <E> the type | 类型
     * @return a new weak interner | 新的弱驻留器
     */
    public static <E> Interner<E> weak() {
        return Interner.<E>newBuilder().weak().build();
    }

    /**
     * Creates a new interner builder.
     * 创建新的驻留器构建器。
     *
     * @param <E> the type | 类型
     * @return a new builder | 新的构建器
     */
    public static <E> Builder<E> newBuilder() {
        return new Builder<>();
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for creating Interner instances.
     * 用于创建 Interner 实例的构建器。
     *
     * @param <E> the type | 类型
     */
    public static final class Builder<E> {
        private Equivalence<E> equivalence;
        private int concurrencyLevel = 4;
        private boolean weak = false;

        private Builder() {
        }

        /**
         * Sets the equivalence strategy.
         * 设置等价策略。
         *
         * @param equivalence the equivalence | 等价策略
         * @return this builder | 此构建器
         */
        public Builder<E> equivalence(Equivalence<E> equivalence) {
            this.equivalence = Objects.requireNonNull(equivalence);
            return this;
        }

        /**
         * Sets the concurrency level for the backing map.
         * 设置后备映射的并发级别。
         *
         * @param concurrencyLevel the concurrency level | 并发级别
         * @return this builder | 此构建器
         */
        public Builder<E> concurrencyLevel(int concurrencyLevel) {
            if (concurrencyLevel < 1) {
                throw new IllegalArgumentException("Concurrency level must be positive: " + concurrencyLevel);
            }
            this.concurrencyLevel = concurrencyLevel;
            return this;
        }

        /**
         * Configures the interner to be strong (keeps all objects).
         * 配置驻留器为强模式（保留所有对象）。
         *
         * @return this builder | 此构建器
         */
        public Builder<E> strong() {
            this.weak = false;
            return this;
        }

        /**
         * Configures the interner to be weak (allows garbage collection).
         * 配置驻留器为弱模式（允许垃圾回收）。
         *
         * @return this builder | 此构建器
         */
        public Builder<E> weak() {
            this.weak = true;
            return this;
        }

        /**
         * Builds the interner.
         * 构建驻留器。
         *
         * @return the interner | 驻留器
         */
        public Interner<E> build() {
            Equivalence<E> eq = equivalence != null ? equivalence : Equivalence.equals();
            if (weak) {
                return new WeakInterner<>(eq, concurrencyLevel);
            } else {
                return new StrongInterner<>(eq, concurrencyLevel);
            }
        }
    }

    // ==================== Strong Interner | 强驻留器 ====================

    private static final class StrongInterner<E> extends Interner<E> {
        private final ConcurrentMap<EquivalenceWrapper<E>, E> map;
        private final Equivalence<E> equivalence;

        StrongInterner(Equivalence<E> equivalence, int concurrencyLevel) {
            this.equivalence = equivalence;
            this.map = new ConcurrentHashMap<>(16, 0.75f, concurrencyLevel);
        }

        @Override
        public E intern(E sample) {
            Objects.requireNonNull(sample);
            EquivalenceWrapper<E> wrapper = new EquivalenceWrapper<>(equivalence, sample);
            E existing = map.putIfAbsent(wrapper, sample);
            return existing != null ? existing : sample;
        }
    }

    // ==================== Weak Interner | 弱驻留器 ====================

    private static final class WeakInterner<E> extends Interner<E> {
        private final ConcurrentMap<EquivalenceWrapper<E>, WeakValueReference<E>> map;
        private final Equivalence<E> equivalence;
        private final ReferenceQueue<E> referenceQueue;

        WeakInterner(Equivalence<E> equivalence, int concurrencyLevel) {
            this.equivalence = equivalence;
            this.map = new ConcurrentHashMap<>(16, 0.75f, concurrencyLevel);
            this.referenceQueue = new ReferenceQueue<>();
        }

        @Override
        public E intern(E sample) {
            Objects.requireNonNull(sample);

            // Clean up stale entries periodically
            cleanUp();

            EquivalenceWrapper<E> wrapper = new EquivalenceWrapper<>(equivalence, sample);

            while (true) {
                WeakValueReference<E> existing = map.get(wrapper);
                if (existing != null) {
                    E value = existing.get();
                    if (value != null) {
                        return value;
                    }
                    // Reference was cleared, remove stale entry
                    map.remove(wrapper, existing);
                }

                // Try to insert new entry
                WeakValueReference<E> newRef = new WeakValueReference<>(sample, referenceQueue, wrapper);
                WeakValueReference<E> prev = map.putIfAbsent(wrapper, newRef);

                if (prev == null) {
                    return sample;
                }

                // Another thread inserted, try to use their value
                E prevValue = prev.get();
                if (prevValue != null) {
                    return prevValue;
                }
                // Their reference was cleared, loop and try again
            }
        }

        private void cleanUp() {
            WeakValueReference<?> ref;
            while ((ref = (WeakValueReference<?>) referenceQueue.poll()) != null) {
                @SuppressWarnings("unchecked")
                EquivalenceWrapper<E> key = (EquivalenceWrapper<E>) ref.key;
                map.remove(key, ref);
            }
        }
    }

    // ==================== Helper Classes | 辅助类 ====================

    private static final class EquivalenceWrapper<E> {
        private final Equivalence<E> equivalence;
        private final E value;
        private final int hashCode;

        EquivalenceWrapper(Equivalence<E> equivalence, E value) {
            this.equivalence = equivalence;
            this.value = value;
            this.hashCode = equivalence.hash(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof EquivalenceWrapper<?> that)) return false;
            @SuppressWarnings("unchecked")
            E otherValue = (E) that.value;
            return equivalence.equivalent(value, otherValue);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    private static final class WeakValueReference<E> extends WeakReference<E> {
        final Object key;

        WeakValueReference(E referent, ReferenceQueue<? super E> queue, Object key) {
            super(referent, queue);
            this.key = key;
        }
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Creates a function that interns its input using this interner.
     * 创建使用此驻留器驻留其输入的函数。
     *
     * @return the interning function | 驻留函数
     */
    public Function<E, E> asFunction() {
        return this::intern;
    }
}
