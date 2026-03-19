package cloud.opencode.base.timeseries.window;

import cloud.opencode.base.timeseries.DataPoint;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Session Window
 * 会话窗口
 *
 * <p>Dynamic windows based on activity gaps.</p>
 * <p>基于活动间隙的动态窗口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Dynamic windows that merge based on inactivity gap - 基于非活动间隙合并的动态窗口</li>
 *   <li>Automatic session merging - 自动会话合并</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SessionWindow sw = SessionWindow.of(Duration.ofMinutes(5));
 * List<Long> keys = sw.assignWindows(dataPoint);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable session state) - 线程安全: 否（可变会话状态）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class SessionWindow implements Window {

    private final Duration gap;
    private final long gapMillis;
    private final NavigableMap<Long, Long> sessions;

    /**
     * Create session window
     * 创建会话窗口
     *
     * @param gap the inactivity gap | 非活动间隙
     */
    public SessionWindow(Duration gap) {
        Objects.requireNonNull(gap, "gap cannot be null");
        if (gap.isZero() || gap.isNegative()) {
            throw new IllegalArgumentException("Gap must be positive");
        }
        this.gap = gap;
        this.gapMillis = gap.toMillis();
        this.sessions = new TreeMap<>();
    }

    /**
     * Create session window
     * 创建会话窗口
     *
     * @param gap the inactivity gap | 非活动间隙
     * @return the window | 窗口
     */
    public static SessionWindow of(Duration gap) {
        return new SessionWindow(gap);
    }

    /**
     * Get gap duration
     * 获取间隙时长
     *
     * @return the gap | 间隙
     */
    public Duration getGap() {
        return gap;
    }

    @Override
    public List<Long> assignWindows(DataPoint point) {
        long timestamp = point.epochMillis();

        // Find existing session or create new one
        Map.Entry<Long, Long> floorEntry = sessions.floorEntry(timestamp);
        Map.Entry<Long, Long> ceilingEntry = sessions.ceilingEntry(timestamp);

        Long sessionKey = null;

        // Check if point fits in floor session
        if (floorEntry != null && timestamp <= floorEntry.getValue() + gapMillis) {
            sessionKey = floorEntry.getKey();
            // Extend session end time
            sessions.put(sessionKey, Math.max(floorEntry.getValue(), timestamp));
        }
        // Check if point should merge with ceiling session
        else if (ceilingEntry != null && ceilingEntry.getKey() - gapMillis <= timestamp) {
            // Merge sessions if both conditions met
            if (sessionKey != null) {
                // Merge floor and ceiling sessions
                sessions.remove(ceilingEntry.getKey());
                sessions.put(sessionKey, ceilingEntry.getValue());
            } else {
                // Extend ceiling session start
                sessions.remove(ceilingEntry.getKey());
                sessions.put(timestamp, ceilingEntry.getValue());
                sessionKey = timestamp;
            }
        }
        // Create new session
        else {
            sessionKey = timestamp;
            sessions.put(timestamp, timestamp);
        }

        return List.of(sessionKey);
    }

    @Override
    public Instant getWindowStart(long windowKey) {
        return Instant.ofEpochMilli(windowKey);
    }

    @Override
    public Instant getWindowEnd(long windowKey) {
        Long endTime = sessions.get(windowKey);
        return endTime != null ? Instant.ofEpochMilli(endTime + gapMillis) : Instant.ofEpochMilli(windowKey);
    }

    @Override
    public Duration getSize() {
        return gap; // Dynamic, but gap is the minimum
    }

    /**
     * Get all session windows
     * 获取所有会话窗口
     *
     * @return the sessions (start -> end) | 会话（开始时间 -> 结束时间）
     */
    public Map<Instant, Instant> getSessions() {
        Map<Instant, Instant> result = new LinkedHashMap<>();
        for (Map.Entry<Long, Long> entry : sessions.entrySet()) {
            result.put(
                Instant.ofEpochMilli(entry.getKey()),
                Instant.ofEpochMilli(entry.getValue())
            );
        }
        return result;
    }

    /**
     * Get session count
     * 获取会话数量
     *
     * @return the session count | 会话数量
     */
    public int getSessionCount() {
        return sessions.size();
    }

    /**
     * Clear all sessions
     * 清除所有会话
     */
    public void clear() {
        sessions.clear();
    }

    @Override
    public String toString() {
        return String.format("SessionWindow[gap=%s, sessions=%d]", gap, sessions.size());
    }
}
