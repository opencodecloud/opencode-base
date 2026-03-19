package cloud.opencode.base.collections.immutable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableClassToInstanceMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ImmutableClassToInstanceMap 测试")
class ImmutableClassToInstanceMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of - 空映射")
        void testOfEmpty() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.of();

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("of - 单个条目")
        void testOfSingleEntry() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.of(String.class, "hello");

            assertThat(map).hasSize(1);
            assertThat(map.getInstance(String.class)).isEqualTo("hello");
        }

        @Test
        @DisplayName("of - null 类型抛异常")
        void testOfNullType() {
            assertThatThrownBy(() -> ImmutableClassToInstanceMap.of(null, "hello"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of - null 值抛异常")
        void testOfNullValue() {
            assertThatThrownBy(() -> ImmutableClassToInstanceMap.of(String.class, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("copyOf - 从映射复制")
        void testCopyOf() {
            Map<Class<? extends Object>, Object> source = new HashMap<>();
            source.put(String.class, "hello");
            source.put(Integer.class, 42);

            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.copyOf(source);

            assertThat(map).hasSize(2);
            assertThat(map.getInstance(String.class)).isEqualTo("hello");
            assertThat(map.getInstance(Integer.class)).isEqualTo(42);
        }

        @Test
        @DisplayName("copyOf - 从空映射复制")
        void testCopyOfEmpty() {
            Map<Class<? extends Object>, Object> source = new HashMap<>();

            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.copyOf(source);

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("copyOf - 从自身复制返回同一实例")
        void testCopyOfSelf() {
            ImmutableClassToInstanceMap<Object> original = ImmutableClassToInstanceMap.of(String.class, "hello");

            ImmutableClassToInstanceMap<Object> copy = ImmutableClassToInstanceMap.copyOf(original);

            assertThat(copy).isSameAs(original);
        }

        @Test
        @DisplayName("builder - 构建器创建")
        void testBuilder() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.builder()
                    .put(String.class, "hello")
                    .put(Integer.class, 42)
                    .build();

            assertThat(map).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("put - 添加条目")
        void testBuilderPut() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.<Object>builder()
                    .put(String.class, "hello")
                    .build();

            assertThat(map.getInstance(String.class)).isEqualTo("hello");
        }

        @Test
        @DisplayName("put - null 类型抛异常")
        void testBuilderPutNullType() {
            ImmutableClassToInstanceMap.Builder<Object> builder = ImmutableClassToInstanceMap.builder();

            assertThatThrownBy(() -> builder.put(null, "hello"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("put - null 值抛异常")
        void testBuilderPutNullValue() {
            ImmutableClassToInstanceMap.Builder<Object> builder = ImmutableClassToInstanceMap.builder();

            assertThatThrownBy(() -> builder.put(String.class, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("putAll - 批量添加")
        void testBuilderPutAll() {
            Map<Class<? extends Object>, Object> source = new HashMap<>();
            source.put(String.class, "hello");
            source.put(Integer.class, 42);

            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.<Object>builder()
                    .putAll(source)
                    .build();

            assertThat(map).hasSize(2);
        }

        @Test
        @DisplayName("build - 空构建器返回空映射")
        void testBuilderBuildEmpty() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.<Object>builder().build();

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("build - 多次调用 build")
        void testBuilderMultipleBuild() {
            ImmutableClassToInstanceMap.Builder<Object> builder = ImmutableClassToInstanceMap.<Object>builder()
                    .put(String.class, "hello");

            ImmutableClassToInstanceMap<Object> map1 = builder.build();
            ImmutableClassToInstanceMap<Object> map2 = builder.build();

            assertThat(map1).isEqualTo(map2);
        }
    }

    @Nested
    @DisplayName("类型安全方法测试")
    class TypeSafeMethodTests {

        @Test
        @DisplayName("getInstance - 获取实例")
        void testGetInstance() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.of(String.class, "hello");

            String value = map.getInstance(String.class);

            assertThat(value).isEqualTo("hello");
        }

        @Test
        @DisplayName("getInstance - 不存在返回 null")
        void testGetInstanceNotExists() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.of();

            String value = map.getInstance(String.class);

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("getInstance - 多种类型")
        void testGetInstanceMultipleTypes() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.<Object>builder()
                    .put(String.class, "hello")
                    .put(Integer.class, 42)
                    .put(Double.class, 3.14)
                    .build();

            assertThat(map.getInstance(String.class)).isEqualTo("hello");
            assertThat(map.getInstance(Integer.class)).isEqualTo(42);
            assertThat(map.getInstance(Double.class)).isEqualTo(3.14);
        }
    }

    @Nested
    @DisplayName("Map 方法测试")
    class MapMethodTests {

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.<Object>builder()
                    .put(String.class, "hello")
                    .put(Integer.class, 42)
                    .build();

            assertThat(map.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            ImmutableClassToInstanceMap<Object> emptyMap = ImmutableClassToInstanceMap.of();
            ImmutableClassToInstanceMap<Object> nonEmptyMap = ImmutableClassToInstanceMap.of(String.class, "hello");

            assertThat(emptyMap.isEmpty()).isTrue();
            assertThat(nonEmptyMap.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("containsKey - 包含键")
        void testContainsKey() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.of(String.class, "hello");

            assertThat(map.containsKey(String.class)).isTrue();
            assertThat(map.containsKey(Integer.class)).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.of(String.class, "hello");

            assertThat(map.containsValue("hello")).isTrue();
            assertThat(map.containsValue("world")).isFalse();
        }

        @Test
        @DisplayName("get - 获取")
        void testGet() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.of(String.class, "hello");

            assertThat(map.get(String.class)).isEqualTo("hello");
            assertThat(map.get(Integer.class)).isNull();
        }

        @Test
        @DisplayName("entrySet - 条目集")
        void testEntrySet() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.<Object>builder()
                    .put(String.class, "hello")
                    .put(Integer.class, 42)
                    .build();

            Set<Map.Entry<Class<? extends Object>, Object>> entries = map.entrySet();

            assertThat(entries).hasSize(2);
        }

        @Test
        @DisplayName("entrySet - 不可修改")
        void testEntrySetUnmodifiable() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.of(String.class, "hello");

            Set<Map.Entry<Class<? extends Object>, Object>> entries = map.entrySet();

            assertThatThrownBy(entries::clear)
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("不可变性测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("put - 抛出异常")
        void testPutThrows() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.of(String.class, "hello");

            assertThatThrownBy(() -> map.put(Integer.class, 42))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("remove - 抛出异常")
        void testRemoveThrows() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.of(String.class, "hello");

            assertThatThrownBy(() -> map.remove(String.class))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("clear - 抛出异常")
        void testClearThrows() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.of(String.class, "hello");

            assertThatThrownBy(map::clear)
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("类型层次结构测试")
    class TypeHierarchyTests {

        @Test
        @DisplayName("子类型实例")
        void testSubtypeInstance() {
            ImmutableClassToInstanceMap<Number> map = ImmutableClassToInstanceMap.<Number>builder()
                    .put(Integer.class, 42)
                    .put(Long.class, 100L)
                    .put(Double.class, 3.14)
                    .build();

            assertThat(map.getInstance(Integer.class)).isEqualTo(42);
            assertThat(map.getInstance(Long.class)).isEqualTo(100L);
            assertThat(map.getInstance(Double.class)).isEqualTo(3.14);
        }

        @Test
        @DisplayName("接口类型")
        void testInterfaceType() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.<Object>builder()
                    .put(List.class, new ArrayList<>())
                    .put(Set.class, new HashSet<>())
                    .build();

            assertThat(map.getInstance(List.class)).isInstanceOf(List.class);
            assertThat(map.getInstance(Set.class)).isInstanceOf(Set.class);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            ImmutableClassToInstanceMap<Object> map1 = ImmutableClassToInstanceMap.of(String.class, "hello");
            ImmutableClassToInstanceMap<Object> map2 = ImmutableClassToInstanceMap.of(String.class, "hello");

            assertThat(map1).isEqualTo(map2);
        }

        @Test
        @DisplayName("equals - 不相等")
        void testNotEquals() {
            ImmutableClassToInstanceMap<Object> map1 = ImmutableClassToInstanceMap.of(String.class, "hello");
            ImmutableClassToInstanceMap<Object> map2 = ImmutableClassToInstanceMap.of(String.class, "world");

            assertThat(map1).isNotEqualTo(map2);
        }

        @Test
        @DisplayName("hashCode - 相等映射相同哈希码")
        void testHashCode() {
            ImmutableClassToInstanceMap<Object> map1 = ImmutableClassToInstanceMap.of(String.class, "hello");
            ImmutableClassToInstanceMap<Object> map2 = ImmutableClassToInstanceMap.of(String.class, "hello");

            assertThat(map1.hashCode()).isEqualTo(map2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.of(String.class, "hello");

            String str = map.toString();

            assertThat(str).contains("String");
            assertThat(str).contains("hello");
        }
    }
}
