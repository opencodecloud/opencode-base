package cloud.opencode.base.deepclone.handler;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.Cloner;

/**
 * Type handler for enum values
 * 枚举值的类型处理器
 *
 * <p>Enums are inherently immutable singletons, so cloning returns the same instance.</p>
 * <p>枚举本质上是不可变的单例，因此克隆返回相同的实例。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.3
 */
public class EnumHandler implements TypeHandler<Enum<?>> {

    @Override
    public Enum<?> clone(Enum<?> original, Cloner cloner, CloneContext context) {
        // Enums are singletons - return same instance
        return original;
    }

    @Override
    public boolean supports(Class<?> type) {
        return type != null && type.isEnum();
    }

    @Override
    public int priority() {
        return 5;
    }
}
