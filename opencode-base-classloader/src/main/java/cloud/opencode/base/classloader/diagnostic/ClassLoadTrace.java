package cloud.opencode.base.classloader.diagnostic;

import java.util.List;
import java.util.Objects;

/**
 * Immutable trace of a class loading delegation chain
 * 类加载委托链的不可变跟踪记录
 *
 * <p>Captures the full delegation path traversed when loading a class, showing which
 * ClassLoaders were consulted and which one ultimately defined the class. This is
 * invaluable for debugging ClassNotFoundException or unexpected class versions.</p>
 *
 * <p>捕获加载类时遍历的完整委托路径，显示咨询了哪些 ClassLoader 以及最终由哪个定义了该类。
 * 这对于调试 ClassNotFoundException 或意外的类版本非常有价值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Records full delegation chain - 记录完整的委托链</li>
 *   <li>Identifies the defining loader - 标识定义加载器</li>
 *   <li>Captures resource location (URL) - 捕获资源位置 (URL)</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ClassLoadTrace trace = ClassLoaderDiagnostics.traceClassLoading(
 *     "com.example.Foo", myClassLoader
 * );
 * System.out.println("Defined by: " + trace.definingLoader());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是 (不可变记录)</li>
 * </ul>
 *
 * @param className       the fully qualified class name | 完全限定类名
 * @param delegationChain the ordered list of ClassLoader names consulted during loading |
 *                        加载期间按顺序咨询的 ClassLoader 名称列表
 * @param definingLoader  the name of the ClassLoader that defines the class, or "bootstrap" |
 *                        定义该类的 ClassLoader 名称，或 "bootstrap"
 * @param location        the resource URL where the class was found, or null if not found |
 *                        发现该类的资源 URL，如果未找到则为 null
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public record ClassLoadTrace(
        String className,
        List<String> delegationChain,
        String definingLoader,
        String location
) {

    /**
     * Compact constructor with validation and defensive copies
     * 带验证和防御性拷贝的紧凑构造器
     *
     * @throws NullPointerException if className, delegationChain, or definingLoader is null |
     *                              当 className、delegationChain 或 definingLoader 为 null 时
     */
    public ClassLoadTrace {
        Objects.requireNonNull(className, "className must not be null");
        Objects.requireNonNull(delegationChain, "delegationChain must not be null");
        Objects.requireNonNull(definingLoader, "definingLoader must not be null");
        // location may be null (class not found)
        delegationChain = List.copyOf(delegationChain);
    }
}
