package cloud.opencode.base.deepclone.handler;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.Cloner;

import java.util.Optional;

/**
 * Type handler for Optional values
 * Optional值的类型处理器
 *
 * <p>Deep clones the content of an Optional, preserving the empty/present state.</p>
 * <p>深度克隆Optional的内容，保持空/存在状态。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.3
 */
@SuppressWarnings("rawtypes")
public class OptionalHandler implements TypeHandler<Optional> {

    @Override
    public Optional clone(Optional original, Cloner cloner, CloneContext context) {
        if (original == null) {
            return null;
        }
        if (original.isEmpty()) {
            return Optional.empty();
        }
        Object clonedValue = cloner.clone(original.get(), context);
        return Optional.ofNullable(clonedValue);
    }

    @Override
    public boolean supports(Class<?> type) {
        return type != null && Optional.class.isAssignableFrom(type);
    }

    @Override
    public int priority() {
        return 15;
    }
}
