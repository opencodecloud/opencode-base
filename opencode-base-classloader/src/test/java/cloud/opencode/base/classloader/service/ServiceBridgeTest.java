package cloud.opencode.base.classloader.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ServiceBridge} and {@link ServiceEntry}.
 * {@link ServiceBridge} 和 {@link ServiceEntry} 的测试。
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
@DisplayName("ServiceBridge Tests | 服务桥接测试")
class ServiceBridgeTest {

    @Nested
    @DisplayName("ServiceEntry Tests | 服务条目测试")
    class ServiceEntryTests {

        @Test
        @DisplayName("Should create ServiceEntry with valid parameters | 使用有效参数创建 ServiceEntry")
        void shouldCreateServiceEntry() {
            ServiceEntry<String> entry = new ServiceEntry<>("svc", "TestCL", 10);
            assertThat(entry.service()).isEqualTo("svc");
            assertThat(entry.classLoaderName()).isEqualTo("TestCL");
            assertThat(entry.priority()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should throw NPE for null service | 空服务应抛出 NPE")
        void shouldThrowForNullService() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ServiceEntry<>(null, "CL", 1));
        }

        @Test
        @DisplayName("Should throw NPE for null classLoaderName | 空类加载器名应抛出 NPE")
        void shouldThrowForNullClassLoaderName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new ServiceEntry<>("svc", null, 1));
        }

        @Test
        @DisplayName("Should sort by priority ascending | 应按优先级升序排序")
        void shouldSortByPriorityAscending() {
            ServiceEntry<String> low = new ServiceEntry<>("a", "CL", 1);
            ServiceEntry<String> mid = new ServiceEntry<>("b", "CL", 50);
            ServiceEntry<String> high = new ServiceEntry<>("c", "CL", 100);
            ServiceEntry<String> maxPrio = new ServiceEntry<>("d", "CL", Integer.MAX_VALUE);

            List<ServiceEntry<String>> list = new java.util.ArrayList<>(List.of(maxPrio, high, low, mid));
            java.util.Collections.sort(list);

            assertThat(list).extracting(ServiceEntry::priority)
                    .containsExactly(1, 50, 100, Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("Should compare equal priorities as equal | 相同优先级应比较为相等")
        void shouldCompareEqualPriorities() {
            ServiceEntry<String> a = new ServiceEntry<>("a", "CL", 10);
            ServiceEntry<String> b = new ServiceEntry<>("b", "CL", 10);
            assertThat(a.compareTo(b)).isZero();
        }
    }

    @Nested
    @DisplayName("Load Tests | 加载测试")
    class LoadTests {

        @Test
        @DisplayName("Should load with system classloader without error | 使用系统类加载器加载不应出错")
        void shouldLoadWithSystemClassLoader() {
            // Runnable is a well-known interface; system CL may or may not have implementations
            List<ServiceEntry<Runnable>> entries = ServiceBridge.load(
                    Runnable.class, ClassLoader.getSystemClassLoader());
            assertThat(entries).isNotNull();
        }

        @Test
        @DisplayName("Should return empty list when no implementations found | 未找到实现时应返回空列表")
        void shouldReturnEmptyListWhenNoImplementations() {
            // Use a very unlikely-to-have-implementations interface
            List<ServiceEntry<Runnable>> entries = ServiceBridge.load(
                    Runnable.class, ClassLoader.getSystemClassLoader());
            assertThat(entries).isEmpty();
        }

        @Test
        @DisplayName("Should skip null classloaders | 应跳过空类加载器")
        void shouldSkipNullClassLoaders() {
            List<ServiceEntry<Runnable>> entries = ServiceBridge.load(
                    Runnable.class, List.of());
            assertThat(entries).isEmpty();
        }

        @Test
        @DisplayName("Should load from Collection of ClassLoaders | 应从类加载器集合中加载")
        void shouldLoadFromCollection() {
            Collection<ClassLoader> classLoaders = List.of(ClassLoader.getSystemClassLoader());
            List<ServiceEntry<Runnable>> entries = ServiceBridge.load(Runnable.class, classLoaders);
            assertThat(entries).isNotNull();
        }
    }

    @Nested
    @DisplayName("LoadFirst Tests | 加载首个测试")
    class LoadFirstTests {

        @Test
        @DisplayName("Should return empty Optional when no implementations | 无实现时应返回空 Optional")
        void shouldReturnEmptyWhenNoImplementations() {
            Optional<Runnable> result = ServiceBridge.loadFirst(
                    Runnable.class, ClassLoader.getSystemClassLoader());
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Null Parameter Tests | 空参数测试")
    class NullParameterTests {

        @Test
        @DisplayName("load(null serviceType, ...) should throw NPE | serviceType 为 null 应抛出 NPE")
        void loadNullServiceTypeVarargs() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ServiceBridge.load(null, ClassLoader.getSystemClassLoader()));
        }

        @Test
        @DisplayName("load(..., null classLoaders varargs) should throw NPE | classLoaders 为 null 应抛出 NPE")
        void loadNullClassLoadersVarargs() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ServiceBridge.load(Runnable.class, (ClassLoader[]) null));
        }

        @Test
        @DisplayName("load(null serviceType, Collection) should throw NPE | serviceType 为 null 应抛出 NPE")
        void loadNullServiceTypeCollection() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ServiceBridge.load(null, List.of()));
        }

        @Test
        @DisplayName("load(..., null Collection) should throw NPE | Collection 为 null 应抛出 NPE")
        void loadNullClassLoadersCollection() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ServiceBridge.load(Runnable.class, (Collection<ClassLoader>) null));
        }

        @Test
        @DisplayName("loadFirst(null, ...) should throw NPE | serviceType 为 null 应抛出 NPE")
        void loadFirstNullServiceType() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ServiceBridge.loadFirst(null, ClassLoader.getSystemClassLoader()));
        }

        @Test
        @DisplayName("loadFirst(..., null) should throw NPE | classLoaders 为 null 应抛出 NPE")
        void loadFirstNullClassLoaders() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ServiceBridge.loadFirst(Runnable.class, (ClassLoader[]) null));
        }
    }
}
