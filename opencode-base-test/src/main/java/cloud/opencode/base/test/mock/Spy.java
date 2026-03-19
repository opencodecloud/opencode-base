package cloud.opencode.base.test.mock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Spy
 * 间谍
 *
 * <p>Records method invocations for verification.</p>
 * <p>记录方法调用以供验证。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Method invocation recording - 方法调用记录</li>
 *   <li>Call verification support - 调用验证支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Spy spy = new Spy();
 * spy.record("methodName", args);
 * assertTrue(spy.wasCalled("methodName"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public class Spy {

    private final List<Invocation> invocations = new CopyOnWriteArrayList<>();

    /**
     * Record an invocation
     * 记录一次调用
     *
     * @param methodName the method name | 方法名称
     * @param args the arguments | 参数
     */
    public void record(String methodName, Object... args) {
        invocations.add(new Invocation(methodName, args));
    }

    /**
     * Verify method was called
     * 验证方法被调用
     *
     * @param methodName the method name | 方法名称
     * @return true if called | 如果被调用返回true
     */
    public boolean wasCalled(String methodName) {
        return invocations.stream().anyMatch(i -> i.methodName().equals(methodName));
    }

    /**
     * Verify method was called n times
     * 验证方法被调用n次
     *
     * @param methodName the method name | 方法名称
     * @param times the expected times | 期望次数
     * @return true if called n times | 如果被调用n次返回true
     */
    public boolean wasCalledTimes(String methodName, int times) {
        long count = invocations.stream().filter(i -> i.methodName().equals(methodName)).count();
        return count == times;
    }

    /**
     * Get call count
     * 获取调用次数
     *
     * @param methodName the method name | 方法名称
     * @return the count | 次数
     */
    public int getCallCount(String methodName) {
        return (int) invocations.stream().filter(i -> i.methodName().equals(methodName)).count();
    }

    /**
     * Get all invocations
     * 获取所有调用
     *
     * @return the invocations | 调用列表
     */
    public List<Invocation> getInvocations() {
        return List.copyOf(invocations);
    }

    /**
     * Get invocations for method
     * 获取方法的调用
     *
     * @param methodName the method name | 方法名称
     * @return the invocations | 调用列表
     */
    public List<Invocation> getInvocations(String methodName) {
        return invocations.stream()
            .filter(i -> i.methodName().equals(methodName))
            .toList();
    }

    /**
     * Get last invocation
     * 获取最后一次调用
     *
     * @return the invocation or null | 调用或null
     */
    public Invocation getLastInvocation() {
        return invocations.isEmpty() ? null : invocations.get(invocations.size() - 1);
    }

    /**
     * Clear recorded invocations
     * 清除记录的调用
     */
    public void clear() {
        invocations.clear();
    }

    /**
     * Verify no interactions
     * 验证没有交互
     *
     * @return true if no invocations | 如果没有调用返回true
     */
    public boolean noInteractions() {
        return invocations.isEmpty();
    }

    /**
     * Invocation record
     * 调用记录
     *
     * @param methodName the method name | 方法名称
     * @param args the arguments | 参数
     */
    public record Invocation(String methodName, Object[] args) {
        public Invocation {
            args = args != null ? args.clone() : new Object[0];
        }

        public Object getArg(int index) {
            return index < args.length ? args[index] : null;
        }

        public int getArgCount() {
            return args.length;
        }
    }
}
