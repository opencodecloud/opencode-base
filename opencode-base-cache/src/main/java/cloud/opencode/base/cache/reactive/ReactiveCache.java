package cloud.opencode.base.cache.reactive;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.CacheStats;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.Function;

/**
 * Reactive Cache - Cache with Reactive Streams support (JDK 9+ Flow API)
 * 响应式缓存 - 支持响应式流的缓存（JDK 9+ Flow API）
 *
 * <p>Provides reactive programming support for cache operations using JDK's
 * built-in Flow API and {@link CompletableFuture}.</p>
 * <p>使用 JDK 内置的 Flow API 和 {@link CompletableFuture} 为缓存操作提供响应式编程支持。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create reactive cache
 * Cache<String, User> cache = OpenCache.getOrCreate("users");
 * ReactiveCache<String, User> reactiveCache = ReactiveCache.wrap(cache);
 *
 * // Using JDK Flow API
 * reactiveCache.getMono("user:1")
 *     .subscribe(new Flow.Subscriber<>() {
 *         public void onSubscribe(Flow.Subscription s) { s.request(1); }
 *         public void onNext(User user) { process(user); }
 *         public void onError(Throwable t) { handleError(t); }
 *         public void onComplete() { }
 *     });
 *
 * // Reactive load via CompletableFuture
 * reactiveCache.getOrLoad("user:1", key -> loadUser(key))
 *     .thenAccept(user -> process(user));
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JDK Flow API integration - JDK Flow API 集成</li>
 *   <li>CompletableFuture-based async operations - 基于 CompletableFuture 的异步操作</li>
 *   <li>Reactive load support - 响应式加载支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.9.0
 */
public final class ReactiveCache<K, V> {

    private final Cache<K, V> delegate;

    private ReactiveCache(Cache<K, V> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
    }

    /**
     * Wrap a cache with reactive support
     * 使用响应式支持包装缓存
     *
     * @param cache the cache to wrap | 要包装的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return reactive cache | 响应式缓存
     */
    public static <K, V> ReactiveCache<K, V> wrap(Cache<K, V> cache) {
        return new ReactiveCache<>(cache);
    }

    // ==================== JDK Flow API | JDK Flow API ====================

    /**
     * Get value as a Publisher (JDK Flow API)
     * 获取值作为 Publisher（JDK Flow API）
     *
     * @param key the key | 键
     * @return publisher emitting value or empty | 发射值或空的 Publisher
     */
    public Flow.Publisher<V> getMono(K key) {
        return subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                private volatile boolean cancelled = false;

                @Override
                public void request(long n) {
                    if (cancelled || n <= 0) return;
                    try {
                        V value = delegate.get(key);
                        if (value != null && !cancelled) {
                            subscriber.onNext(value);
                        }
                        if (!cancelled) {
                            subscriber.onComplete();
                        }
                    } catch (Throwable t) {
                        if (!cancelled) {
                            subscriber.onError(t);
                        }
                    }
                }

                @Override
                public void cancel() {
                    cancelled = true;
                }
            });
        };
    }

    /**
     * Get or load value as a Publisher
     * 获取或加载值作为 Publisher
     *
     * @param key    the key | 键
     * @param loader the loader function | 加载函数
     * @return publisher emitting loaded value | 发射加载值的 Publisher
     */
    public Flow.Publisher<V> getOrLoadMono(K key, Function<? super K, ? extends V> loader) {
        return subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                private volatile boolean cancelled = false;

                @Override
                public void request(long n) {
                    if (cancelled || n <= 0) return;
                    try {
                        V value = delegate.get(key, loader);
                        if (!cancelled) {
                            if (value != null) {
                                subscriber.onNext(value);
                            }
                            subscriber.onComplete();
                        }
                    } catch (Throwable t) {
                        if (!cancelled) {
                            subscriber.onError(t);
                        }
                    }
                }

                @Override
                public void cancel() {
                    cancelled = true;
                }
            });
        };
    }

    /**
     * Get all values as a Publisher (emits each value)
     * 获取所有值作为 Publisher（发射每个值）
     *
     * @param keys the keys | 键集合
     * @return publisher emitting values | 发射值的 Publisher
     */
    public Flow.Publisher<V> getAllFlux(Iterable<? extends K> keys) {
        return subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                private volatile boolean cancelled = false;
                private final java.util.concurrent.atomic.AtomicLong requested = new java.util.concurrent.atomic.AtomicLong(0);

                @Override
                public void request(long n) {
                    if (cancelled || n <= 0) return;
                    requested.addAndGet(n);

                    CompletableFuture.runAsync(() -> {
                        try {
                            Map<K, V> values = delegate.getAll(keys);
                            for (V value : values.values()) {
                                if (cancelled || requested.get() <= 0) break;
                                subscriber.onNext(value);
                                requested.decrementAndGet();
                            }
                            if (!cancelled) {
                                subscriber.onComplete();
                            }
                        } catch (Throwable t) {
                            if (!cancelled) {
                                subscriber.onError(t);
                            }
                        }
                    });
                }

                @Override
                public void cancel() {
                    cancelled = true;
                }
            });
        };
    }

    /**
     * Get all keys as a Publisher
     * 获取所有键作为 Publisher
     *
     * @return publisher emitting keys | 发射键的 Publisher
     */
    public Flow.Publisher<K> keysFlux() {
        return subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                private volatile boolean cancelled = false;

                @Override
                public void request(long n) {
                    if (cancelled || n <= 0) return;
                    CompletableFuture.runAsync(() -> {
                        try {
                            long count = 0;
                            for (K key : delegate.keys()) {
                                if (cancelled || count >= n) break;
                                subscriber.onNext(key);
                                count++;
                            }
                            if (!cancelled) {
                                subscriber.onComplete();
                            }
                        } catch (Throwable t) {
                            if (!cancelled) {
                                subscriber.onError(t);
                            }
                        }
                    });
                }

                @Override
                public void cancel() {
                    cancelled = true;
                }
            });
        };
    }

    /**
     * Get all values as a Publisher
     * 获取所有值作为 Publisher
     *
     * @return publisher emitting values | 发射值的 Publisher
     */
    public Flow.Publisher<V> valuesFlux() {
        return subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                private volatile boolean cancelled = false;

                @Override
                public void request(long n) {
                    if (cancelled || n <= 0) return;
                    CompletableFuture.runAsync(() -> {
                        try {
                            long count = 0;
                            for (V value : delegate.values()) {
                                if (cancelled || count >= n) break;
                                subscriber.onNext(value);
                                count++;
                            }
                            if (!cancelled) {
                                subscriber.onComplete();
                            }
                        } catch (Throwable t) {
                            if (!cancelled) {
                                subscriber.onError(t);
                            }
                        }
                    });
                }

                @Override
                public void cancel() {
                    cancelled = true;
                }
            });
        };
    }

    /**
     * Get all entries as a Publisher
     * 获取所有条目作为 Publisher
     *
     * @return publisher emitting entries | 发射条目的 Publisher
     */
    public Flow.Publisher<Map.Entry<K, V>> entriesFlux() {
        return subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                private volatile boolean cancelled = false;

                @Override
                public void request(long n) {
                    if (cancelled || n <= 0) return;
                    CompletableFuture.runAsync(() -> {
                        try {
                            long count = 0;
                            for (Map.Entry<K, V> entry : delegate.entries()) {
                                if (cancelled || count >= n) break;
                                subscriber.onNext(entry);
                                count++;
                            }
                            if (!cancelled) {
                                subscriber.onComplete();
                            }
                        } catch (Throwable t) {
                            if (!cancelled) {
                                subscriber.onError(t);
                            }
                        }
                    });
                }

                @Override
                public void cancel() {
                    cancelled = true;
                }
            });
        };
    }

    // ==================== CompletableFuture API ====================

    /**
     * Get value asynchronously
     * 异步获取值
     *
     * @param key the key | 键
     * @return future with value or null | 包含值或 null 的 Future
     */
    public CompletableFuture<V> getAsync(K key) {
        return CompletableFuture.supplyAsync(() -> delegate.get(key));
    }

    /**
     * Get or load value asynchronously
     * 异步获取或加载值
     *
     * @param key    the key | 键
     * @param loader the loader | 加载器
     * @return future with value | 包含值的 Future
     */
    public CompletableFuture<V> getOrLoad(K key, Function<? super K, ? extends V> loader) {
        return CompletableFuture.supplyAsync(() -> delegate.get(key, loader));
    }

    /**
     * Put value asynchronously
     * 异步放入值
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return future completing when done | 完成时的 Future
     */
    public CompletableFuture<Void> putAsync(K key, V value) {
        return CompletableFuture.runAsync(() -> delegate.put(key, value));
    }

    /**
     * Put value with TTL asynchronously
     * 异步放入带 TTL 的值
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @param ttl   the TTL | 存活时间
     * @return future completing when done | 完成时的 Future
     */
    public CompletableFuture<Void> putWithTtlAsync(K key, V value, Duration ttl) {
        return CompletableFuture.runAsync(() -> delegate.putWithTtl(key, value, ttl));
    }

    /**
     * Invalidate key asynchronously
     * 异步使键失效
     *
     * @param key the key | 键
     * @return future completing when done | 完成时的 Future
     */
    public CompletableFuture<Void> invalidateAsync(K key) {
        return CompletableFuture.runAsync(() -> delegate.invalidate(key));
    }

    /**
     * Invalidate by pattern asynchronously
     * 异步按模式使键失效
     *
     * @param pattern the pattern | 模式
     * @return future with count of invalidated entries | 包含失效条目数的 Future
     */
    public CompletableFuture<Long> invalidateByPatternAsync(String pattern) {
        return CompletableFuture.supplyAsync(() -> delegate.invalidateByPattern(pattern));
    }

    /**
     * Get stats asynchronously
     * 异步获取统计
     *
     * @return future with stats | 包含统计的 Future
     */
    public CompletableFuture<CacheStats> statsAsync() {
        return CompletableFuture.supplyAsync(delegate::stats);
    }

    /**
     * Get the underlying cache
     * 获取底层缓存
     *
     * @return delegate cache | 委托缓存
     */
    public Cache<K, V> getDelegate() {
        return delegate;
    }
}
