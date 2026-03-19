package cloud.opencode.base.deepclone.handler;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.Cloner;
import cloud.opencode.base.deepclone.exception.OpenDeepCloneException;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;

/**
 * Handler for cloning Record types
 * Record类型克隆处理器
 *
 * <p>Handles JDK 16+ Record classes by extracting component values,
 * deep cloning them, and constructing a new Record instance.</p>
 * <p>通过提取组件值、深度克隆并构造新的Record实例来处理JDK 16+的Record类。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * record Point(int x, int y, String label) {}
 *
 * RecordHandler handler = new RecordHandler();
 * Point original = new Point(10, 20, "origin");
 * Point cloned = handler.cloneRecord(original, cloner, context);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Record component extraction and cloning - Record组件提取和克隆</li>
 *   <li>Canonical constructor invocation - 规范构造函数调用</li>
 *   <li>JDK 16+ Record support - JDK 16+ Record支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public final class RecordHandler implements TypeHandler<Record> {

    @Override
    public Record clone(Record original, Cloner cloner, CloneContext context) {
        if (original == null) {
            return null;
        }

        return cloneRecord(original, cloner, context);
    }

    /**
     * Clones a Record
     * 克隆Record
     *
     * @param record  the original record | 原始Record
     * @param cloner  the cloner | 克隆器
     * @param context the context | 上下文
     * @param <T>     the record type | Record类型
     * @return the cloned record | 克隆的Record
     */
    @SuppressWarnings("unchecked")
    public <T extends Record> T cloneRecord(T record, Cloner cloner, CloneContext context) {
        if (record == null) {
            return null;
        }

        Class<T> recordClass = (Class<T>) record.getClass();
        Object[] componentValues = getComponents(record);

        // Deep clone each component
        Object[] clonedValues = new Object[componentValues.length];
        for (int i = 0; i < componentValues.length; i++) {
            clonedValues[i] = cloner.clone(componentValues[i], context);
        }

        T cloned = createInstance(recordClass, clonedValues);
        context.registerCloned(record, cloned);
        return cloned;
    }

    /**
     * Gets the component values of a Record
     * 获取Record的组件值
     *
     * @param record the record | Record
     * @return the component values array | 组件值数组
     */
    public Object[] getComponents(Record record) {
        RecordComponent[] components = record.getClass().getRecordComponents();
        Object[] values = new Object[components.length];

        try {
            for (int i = 0; i < components.length; i++) {
                values[i] = components[i].getAccessor().invoke(record);
            }
        } catch (Exception e) {
            throw new OpenDeepCloneException(record.getClass(), null,
                    "Failed to get record components", e);
        }

        return values;
    }

    /**
     * Creates a new Record instance with the given component values
     * 使用给定的组件值创建新的Record实例
     *
     * @param type   the record class | Record类
     * @param values the component values | 组件值
     * @param <T>    the record type | Record类型
     * @return the new record instance | 新的Record实例
     */
    public <T extends Record> T createInstance(Class<T> type, Object[] values) {
        RecordComponent[] components = type.getRecordComponents();
        Class<?>[] paramTypes = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            paramTypes[i] = components[i].getType();
        }

        try {
            Constructor<T> constructor = type.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(values);
        } catch (Exception e) {
            throw OpenDeepCloneException.instantiationFailed(type, e);
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return type != null && type.isRecord();
    }

    @Override
    public int priority() {
        return 15;
    }
}
