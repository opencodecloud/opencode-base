/**
 * Retry utilities with configurable backoff strategies.
 * 重试工具，支持可配置的退避策略。
 *
 * <p>Provides a general-purpose retry utility ({@link cloud.opencode.base.core.retry.Retry})
 * with configurable backoff strategies ({@link cloud.opencode.base.core.retry.BackoffStrategy})
 * including fixed, exponential, exponential with jitter, and Fibonacci.</p>
 *
 * @since JDK 25, opencode-base-core V1.0.3
 */
@NullMarked
package cloud.opencode.base.core.retry;

import org.jspecify.annotations.NullMarked;
