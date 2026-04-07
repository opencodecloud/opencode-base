package cloud.opencode.base.graph.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.util.Objects;

/**
 * Graph Exception
 * 图异常基类
 *
 * <p>Base exception class for all graph-related exceptions.</p>
 * <p>所有图相关异常的基类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Carries {@link GraphErrorCode} for programmatic error handling - 携带GraphErrorCode用于程序化错误处理</li>
 *   <li>Base class for all graph exceptions - 所有图异常的基类</li>
 *   <li>Extends {@link OpenException} for unified exception hierarchy - 继承OpenException统一异常体系</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     OpenGraph.topologicalSort(graph);
 * } catch (GraphException e) {
 *     GraphErrorCode code = e.getGraphErrorCode();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-graph V1.0.3
 */
public class GraphException extends OpenException {

    private final GraphErrorCode graphErrorCode;

    /**
     * Create graph exception
     * 创建图异常
     *
     * @param message the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public GraphException(String message, GraphErrorCode errorCode) {
        super("Graph", Objects.requireNonNull(errorCode, "errorCode must not be null").getCode() + "", message);
        this.graphErrorCode = errorCode;
    }

    /**
     * Create graph exception with cause
     * 创建带原因的图异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     * @param errorCode the error code | 错误码
     */
    public GraphException(String message, Throwable cause, GraphErrorCode errorCode) {
        super("Graph", Objects.requireNonNull(errorCode, "errorCode must not be null").getCode() + "", message, cause);
        this.graphErrorCode = errorCode;
    }

    /**
     * Get the graph error code
     * 获取图错误码
     *
     * @return graph error code | 图错误码
     */
    public GraphErrorCode getGraphErrorCode() {
        return graphErrorCode;
    }
}
