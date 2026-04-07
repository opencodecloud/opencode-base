package cloud.opencode.base.classloader.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Service Bridge - Cross-ClassLoader service discovery utility
 * 服务桥接 - 跨类加载器服务发现工具
 *
 * <p>Discovers service implementations across multiple ClassLoaders using
 * {@link ServiceLoader}, orders them by {@code @Priority} annotation value,
 * and returns them as {@link ServiceEntry} records.</p>
 * <p>通过 {@link ServiceLoader} 在多个类加载器中发现服务实现，
 * 按 {@code @Priority} 注解值排序，以 {@link ServiceEntry} 记录返回。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load services across multiple ClassLoaders - 跨多个类加载器加载服务</li>
 *   <li>Priority-based ordering via @Priority annotation - 基于 @Priority 注解的优先级排序</li>
 *   <li>Supports both javax.annotation.Priority and jakarta.annotation.Priority - 支持 javax 和 jakarta 两种 Priority 注解</li>
 *   <li>Graceful error handling (logs and skips failures) - 优雅的错误处理（记录并跳过失败）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Load all implementations across classloaders
 * List<ServiceEntry<MyService>> entries = ServiceBridge.load(
 *     MyService.class, cl1, cl2, cl3);
 *
 * // Get the highest-priority implementation
 * Optional<MyService> best = ServiceBridge.loadFirst(
 *     MyService.class, cl1, cl2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Does not hold strong ClassLoader references - 不持有类加载器的强引用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see ServiceEntry
 * @see ServiceLoader
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public final class ServiceBridge {

    private static final System.Logger LOGGER = System.getLogger(ServiceBridge.class.getName());

    /**
     * Priority annotation class names to check reflectively.
     * 通过反射检查的 Priority 注解类名。
     */
    private static final String[] PRIORITY_ANNOTATIONS = {
            "jakarta.annotation.Priority",
            "javax.annotation.Priority"
    };

    private ServiceBridge() {
        // Utility class — no instantiation | 工具类，禁止实例化
    }

    /**
     * Load services of the given type from multiple ClassLoaders.
     * 从多个类加载器中加载指定类型的服务。
     *
     * @param serviceType  the service interface or abstract class | 服务接口或抽象类
     * @param classLoaders the ClassLoaders to search | 要搜索的类加载器
     * @param <S>          the service type | 服务类型
     * @return sorted list of service entries (by priority ascending) | 按优先级升序排序的服务条目列表
     * @throws NullPointerException if serviceType or classLoaders is null | 如果参数为 null 则抛出空指针异常
     */
    public static <S> List<ServiceEntry<S>> load(Class<S> serviceType, ClassLoader... classLoaders) {
        Objects.requireNonNull(serviceType, "serviceType must not be null | serviceType 不能为 null");
        Objects.requireNonNull(classLoaders, "classLoaders must not be null | classLoaders 不能为 null");
        return load(serviceType, Arrays.asList(classLoaders));
    }

    /**
     * Load services of the given type from a collection of ClassLoaders.
     * 从类加载器集合中加载指定类型的服务。
     *
     * @param serviceType  the service interface or abstract class | 服务接口或抽象类
     * @param classLoaders the ClassLoaders to search | 要搜索的类加载器
     * @param <S>          the service type | 服务类型
     * @return sorted list of service entries (by priority ascending) | 按优先级升序排序的服务条目列表
     * @throws NullPointerException if serviceType or classLoaders is null | 如果参数为 null 则抛出空指针异常
     */
    public static <S> List<ServiceEntry<S>> load(Class<S> serviceType, Collection<ClassLoader> classLoaders) {
        Objects.requireNonNull(serviceType, "serviceType must not be null | serviceType 不能为 null");
        Objects.requireNonNull(classLoaders, "classLoaders must not be null | classLoaders 不能为 null");

        List<ServiceEntry<S>> entries = new ArrayList<>();

        for (ClassLoader cl : classLoaders) {
            if (cl == null) {
                continue;
            }
            String clName = cl.getName() != null ? cl.getName() : cl.toString();
            try {
                ServiceLoader<S> loader = ServiceLoader.load(serviceType, cl);
                for (S service : loader) {
                    int priority = getPriority(service);
                    entries.add(new ServiceEntry<>(service, clName, priority));
                }
            } catch (ServiceConfigurationError e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Failed to load service {0} from ClassLoader {1}: {2}",
                        serviceType.getName(), clName, e.getMessage());
            }
        }

        Collections.sort(entries);
        return Collections.unmodifiableList(entries);
    }

    /**
     * Load the highest-priority service of the given type from multiple ClassLoaders.
     * 从多个类加载器中加载最高优先级的服务。
     *
     * @param serviceType  the service interface or abstract class | 服务接口或抽象类
     * @param classLoaders the ClassLoaders to search | 要搜索的类加载器
     * @param <S>          the service type | 服务类型
     * @return the highest-priority service, or empty if none found | 最高优先级的服务，若未找到则为空
     * @throws NullPointerException if serviceType or classLoaders is null | 如果参数为 null 则抛出空指针异常
     */
    public static <S> Optional<S> loadFirst(Class<S> serviceType, ClassLoader... classLoaders) {
        Objects.requireNonNull(serviceType, "serviceType must not be null | serviceType 不能为 null");
        Objects.requireNonNull(classLoaders, "classLoaders must not be null | classLoaders 不能为 null");

        List<ServiceEntry<S>> entries = load(serviceType, classLoaders);
        if (entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(entries.getFirst().service());
    }

    /**
     * Reflectively get the priority value from a @Priority annotation on the service.
     * 通过反射获取服务上 @Priority 注解的优先级值。
     *
     * <p>Checks for both {@code jakarta.annotation.Priority} and
     * {@code javax.annotation.Priority} by class name to avoid
     * compile-time dependency.</p>
     * <p>通过类名检查 jakarta 和 javax 两种 Priority 注解，
     * 避免编译时依赖。</p>
     *
     * @param service the service instance | 服务实例
     * @return the priority value, or {@link Integer#MAX_VALUE} if not annotated | 优先级值，未标注则返回最大整数值
     */
    private static int getPriority(Object service) {
        for (Annotation annotation : service.getClass().getAnnotations()) {
            String annotationTypeName = annotation.annotationType().getName();
            for (String priorityName : PRIORITY_ANNOTATIONS) {
                if (priorityName.equals(annotationTypeName)) {
                    try {
                        Method valueMethod = annotation.annotationType().getMethod("value");
                        Object result = valueMethod.invoke(annotation);
                        if (result instanceof Integer intValue) {
                            return intValue;
                        }
                    } catch (ReflectiveOperationException e) {
                        LOGGER.log(System.Logger.Level.DEBUG,
                                "Failed to read @Priority value from {0}: {1}",
                                service.getClass().getName(), e.getMessage());
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }
}
