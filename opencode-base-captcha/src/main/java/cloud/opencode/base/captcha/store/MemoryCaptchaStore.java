package cloud.opencode.base.captcha.store;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Memory Captcha Store - In-memory CAPTCHA storage
 * 内存验证码存储 - 内存中的验证码存储
 *
 * <p>Thread-safe in-memory implementation of CaptchaStore.</p>
 * <p>线程安全的内存验证码存储实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ConcurrentHashMap-based storage - 基于 ConcurrentHashMap 的存储</li>
 *   <li>Automatic expiration cleanup via scheduled executor - 通过调度执行器自动清理过期数据</li>
 *   <li>Configurable maximum size with LRU eviction - 可配置最大大小及 LRU 驱逐</li>
 *   <li>AutoCloseable for resource cleanup - AutoCloseable 用于资源清理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (MemoryCaptchaStore store = new MemoryCaptchaStore(5000)) {
 *     store.store("id", "answer", Duration.ofMinutes(5));
 *     Optional<String> answer = store.getAndRemove("id");
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap + atomic operations) - 线程安全: 是</li>
 *   <li>Null-safe: No (parameters must be non-null) - 空值安全: 否（参数不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class MemoryCaptchaStore implements CaptchaStore, AutoCloseable {

    private static final int DEFAULT_MAX_SIZE = 10000;
    private static final long CLEANUP_INTERVAL_SECONDS = 60;

    private final Map<String, CaptchaEntry> store = new ConcurrentHashMap<>();
    private final int maxSize;
    private final ScheduledExecutorService scheduler;

    /**
     * Creates a store with default max size.
     * 创建具有默认最大大小的存储。
     */
    public MemoryCaptchaStore() {
        this(DEFAULT_MAX_SIZE);
    }

    /**
     * Creates a store with specified max size.
     * 创建具有指定最大大小的存储。
     *
     * @param maxSize the maximum size | 最大大小
     */
    public MemoryCaptchaStore(int maxSize) {
        this.maxSize = maxSize;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "captcha-cleanup");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(
            this::clearExpired,
            CLEANUP_INTERVAL_SECONDS,
            CLEANUP_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );
    }

    @Override
    public void store(String id, String answer, Duration ttl) {
        store.put(id, new CaptchaEntry(answer, Instant.now().plus(ttl)));
        if (store.size() > maxSize) {
            clearExpired();
            if (store.size() > maxSize) {
                // Remove oldest entry
                store.entrySet().stream()
                    .min((a, b) -> a.getValue().expiresAt.compareTo(b.getValue().expiresAt))
                    .ifPresent(e -> store.remove(e.getKey()));
            }
        }
    }

    @Override
    public Optional<String> get(String id) {
        CaptchaEntry entry = store.get(id);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired()) {
            store.remove(id);
            return Optional.empty();
        }
        return Optional.of(entry.answer);
    }

    @Override
    public Optional<String> getAndRemove(String id) {
        CaptchaEntry entry = store.remove(id);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired()) {
            return Optional.empty();
        }
        return Optional.of(entry.answer);
    }

    @Override
    public void remove(String id) {
        store.remove(id);
    }

    @Override
    public boolean exists(String id) {
        CaptchaEntry entry = store.get(id);
        if (entry == null) {
            return false;
        }
        if (entry.isExpired()) {
            store.remove(id);
            return false;
        }
        return true;
    }

    @Override
    public void clearExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, CaptchaEntry>> it = store.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, CaptchaEntry> entry = it.next();
            if (entry.getValue().expiresAt.isBefore(now)) {
                it.remove();
            }
        }
    }

    @Override
    public void clearAll() {
        store.clear();
    }

    @Override
    public int size() {
        return store.size();
    }

    /**
     * Shuts down the cleanup scheduler.
     * 关闭清理调度器。
     */
    public void shutdown() {
        scheduler.shutdown();
    }

    /**
     * Closes this store and shuts down the cleanup scheduler.
     * 关闭此存储并关闭清理调度器。
     *
     * <p>Delegates to {@link #shutdown()}. Enables try-with-resources usage.</p>
     * <p>委托给 {@link #shutdown()}。支持 try-with-resources 使用。</p>
     */
    @Override
    public void close() {
        shutdown();
    }

    /**
     * Internal entry class.
     */
    private record CaptchaEntry(String answer, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
