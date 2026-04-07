package cloud.opencode.base.classloader.security;

/**
 * Functional interface for custom bytecode verification
 * 自定义字节码验证的函数式接口
 *
 * <p>Implementations inspect raw class bytecode and determine whether it should
 * be allowed to load. This can be used to enforce security policies such as
 * banning specific opcodes, enforcing size limits, or running static analysis.</p>
 *
 * <p>实现检查原始类字节码并确定是否允许加载。可用于执行安全策略，
 * 如禁止特定操作码、强制大小限制或运行静态分析。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
@FunctionalInterface
public interface BytecodeVerifier {

    /**
     * Verify whether the given bytecode should be allowed to load
     * 验证给定的字节码是否应被允许加载
     *
     * @param bytecode the raw class bytecode to verify | 要验证的原始类字节码
     * @return true if the bytecode passes verification, false otherwise |
     *         如果字节码通过验证返回 true，否则返回 false
     */
    boolean verify(byte[] bytecode);
}
