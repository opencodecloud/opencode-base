package cloud.opencode.base.parallel.deadline;

import cloud.opencode.base.core.Preconditions;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Deadline Context — ScopedValue-based deadline propagation for virtual threads
 * 截止时间上下文 — 基于 ScopedValue 的虚拟线程截止时间传播
 *
 * <p>Allows binding a deadline to the current virtual thread scope so that
 * all operations within that scope can check the deadline.</p>
 * <p>允许将截止时间绑定到当前虚拟线程作用域，使该作用域内的所有操作可以检查截止时间。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * DeadlineContext.withTimeout(Duration.ofSeconds(5), () -> {
 *     // All operations in this scope can check the deadline
 *     Optional<Duration> remaining = DeadlineContext.remaining();
 * });
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ScopedValue-based deadline propagation - 基于ScopedValue的截止时间传播</li>
 *   <li>Timeout and deadline binding - 超时和截止时间绑定</li>
 *   <li>Remaining time checking - 剩余时间检查</li>
 *   <li>Virtual thread scope inheritance - 虚拟线程作用域继承</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ScopedValue is inherently thread-safe) - 线程安全: 是（ScopedValue天然线程安全）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
public final class DeadlineContext {

    private static final ScopedValue<Instant> DEADLINE = ScopedValue.newInstance();

    private DeadlineContext() {}

    public static void withDeadline(Instant deadline, Runnable action) {
        Preconditions.checkNotNull(deadline, "deadline must not be null");
        Preconditions.checkNotNull(action, "action must not be null");
        ScopedValue.where(DEADLINE, deadline).run(action);
    }

    public static <T> T withDeadline(Instant deadline, Callable<T> action) throws Exception {
        Preconditions.checkNotNull(deadline, "deadline must not be null");
        Preconditions.checkNotNull(action, "action must not be null");
        return ScopedValue.where(DEADLINE, deadline).call(action::call);
    }

    public static void withTimeout(Duration timeout, Runnable action) {
        withDeadline(Instant.now().plus(timeout), action);
    }

    public static <T> T withTimeout(Duration timeout, Callable<T> action) throws Exception {
        return withDeadline(Instant.now().plus(timeout), action::call);
    }

    public static Optional<Instant> current() {
        return DEADLINE.isBound() ? Optional.of(DEADLINE.get()) : Optional.empty();
    }

    public static Optional<Duration> remaining() {
        Optional<Instant> deadline = current();
        if (deadline.isEmpty()) return Optional.empty();
        Duration remaining = Duration.between(Instant.now(), deadline.get());
        return Optional.of(remaining.isNegative() ? Duration.ZERO : remaining);
    }

    public static boolean isBound() {
        return DEADLINE.isBound();
    }

    public static boolean isExpired() {
        Optional<Instant> deadline = current();
        return deadline.isPresent() && Instant.now().isAfter(deadline.get());
    }
}
