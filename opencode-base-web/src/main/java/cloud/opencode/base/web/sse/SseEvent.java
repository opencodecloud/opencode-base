package cloud.opencode.base.web.sse;

import java.util.Optional;

/**
 * SSE Event - Represents a Server-Sent Event
 * SSE 事件 - 表示服务器发送的事件
 *
 * <p>Server-Sent Events (SSE) is a technology for pushing events from server to client
 * over HTTP.</p>
 * <p>服务器发送事件（SSE）是一种通过 HTTP 从服务器向客户端推送事件的技术。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable SSE event record - 不可变SSE事件记录</li>
 *   <li>Builder pattern for event construction - 构建器模式</li>
 *   <li>Optional fields (id, retry) - 可选字段（id、retry）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SseEvent event = SseEvent.of("Hello, World!");
 * SseEvent typed = SseEvent.of("update", jsonData);
 * SseEvent full = SseEvent.builder()
 *     .id("123").event("update").data(json).retry(3000).build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 是（不可变记录）</li>
 *   <li>Null-safe: Partial (data can be null) - 部分（数据可以为null）</li>
 * </ul>
 * @param id    the event ID (optional) - 事件 ID（可选）
 * @param event the event type (default: "message") - 事件类型（默认："message"）
 * @param data  the event data - 事件数据
 * @param retry the reconnection time in milliseconds (optional) - 重连时间（毫秒，可选）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public record SseEvent(
        String id,
        String event,
        String data,
        Long retry
) {

    public static final String DEFAULT_EVENT = "message";

    public static SseEvent of(String data) {
        return new SseEvent(null, DEFAULT_EVENT, data, null);
    }

    public static SseEvent of(String event, String data) {
        return new SseEvent(null, event, data, null);
    }

    public static SseEvent of(String id, String event, String data) {
        return new SseEvent(id, event, data, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String eventOrDefault() {
        return event != null ? event : DEFAULT_EVENT;
    }

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public Optional<Long> getRetry() {
        return Optional.ofNullable(retry);
    }

    public boolean isMessage() {
        return event == null || DEFAULT_EVENT.equals(event);
    }

    public boolean hasData() {
        return data != null && !data.isEmpty();
    }

    public boolean hasId() {
        return id != null && !id.isEmpty();
    }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder("SseEvent{");
        if (id != null) sb.append("id=").append(id).append(", ");
        if (event != null) sb.append("event=").append(event).append(", ");
        if (data != null) {
            String preview = data.length() > 50 ? data.substring(0, 50) + "..." : data;
            sb.append("data=").append(preview);
        }
        if (retry != null) sb.append(", retry=").append(retry);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return toDebugString();
    }

    public static final class Builder {
        private String id;
        private String event;
        private String data;
        private Long retry;

        private Builder() {}

        public Builder id(String id) { this.id = id; return this; }
        public Builder event(String event) { this.event = event; return this; }
        public Builder data(String data) { this.data = data; return this; }

        public Builder appendData(String data) {
            if (this.data == null) {
                this.data = data;
            } else {
                this.data = this.data + "\n" + data;
            }
            return this;
        }

        public Builder retry(long retry) { this.retry = retry; return this; }

        public SseEvent build() {
            return new SseEvent(id, event, data, retry);
        }

        public boolean hasContent() {
            return data != null || id != null || event != null || retry != null;
        }

        public Builder reset() {
            this.id = null;
            this.event = null;
            this.data = null;
            this.retry = null;
            return this;
        }
    }
}
