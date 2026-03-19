package cloud.opencode.base.collections.specialized;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MutableClassToInstanceMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("MutableClassToInstanceMap 测试")
class MutableClassToInstanceMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空映射")
        void testCreate() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("create - 使用自定义支持映射")
        void testCreateWithBackingMap() {
            Map<Class<? extends Number>, Number> backingMap = new HashMap<>();
            MutableClassToInstanceMap<Number> map = MutableClassToInstanceMap.create(backingMap);

            map.putInstance(Integer.class, 42);

            assertThat(backingMap).containsKey(Integer.class);
        }
    }

    @Nested
    @DisplayName("ClassToInstanceMap 方法测试")
    class ClassToInstanceMapMethodTests {

        @Test
        @DisplayName("getInstance - 获取实例")
        void testGetInstance() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            String value = map.getInstance(String.class);

            assertThat(value).isEqualTo("hello");
        }

        @Test
        @DisplayName("getInstance - 不存在返回 null")
        void testGetInstanceNotExists() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            String value = map.getInstance(String.class);

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("putInstance - 放置实例")
        void testPutInstance() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            String old = map.putInstance(String.class, "hello");

            assertThat(old).isNull();
            assertThat(map.getInstance(String.class)).isEqualTo("hello");
        }

        @Test
        @DisplayName("putInstance - 替换实例")
        void testPutInstanceReplace() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            String old = map.putInstance(String.class, "world");

            assertThat(old).isEqualTo("hello");
            assertThat(map.getInstance(String.class)).isEqualTo("world");
        }

        @Test
        @DisplayName("putInstance - null 类型抛异常")
        void testPutInstanceNullType() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            assertThatThrownBy(() -> map.putInstance(null, "hello"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("putInstance - null 值抛异常")
        void testPutInstanceNullValue() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            assertThatThrownBy(() -> map.putInstance(String.class, null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    @DisplayName("Map 方法测试")
    class MapMethodTests {

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");
            map.putInstance(Integer.class, 42);

            assertThat(map.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            assertThat(map.isEmpty()).isTrue();

            map.putInstance(String.class, "hello");

            assertThat(map.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("containsKey - 包含键")
        void testContainsKey() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            assertThat(map.containsKey(String.class)).isTrue();
            assertThat(map.containsKey(Integer.class)).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            assertThat(map.containsValue("hello")).isTrue();
            assertThat(map.containsValue("world")).isFalse();
        }

        @Test
        @DisplayName("get - 获取")
        void testGet() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            assertThat(map.get(String.class)).isEqualTo("hello");
            assertThat(map.get(Integer.class)).isNull();
        }

        @Test
        @DisplayName("put - 放置")
        void testPut() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            Object old = map.put(String.class, "hello");

            assertThat(old).isNull();
            assertThat(map.get(String.class)).isEqualTo("hello");
        }

        @Test
        @DisplayName("put - 类型不匹配抛异常")
        void testPutTypeMismatch() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            assertThatThrownBy(() -> map.put(String.class, 42))
                    .isInstanceOf(ClassCastException.class);
        }

        @Test
        @DisplayName("remove - 移除")
        void testRemove() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            Object removed = map.remove(String.class);

            assertThat(removed).isEqualTo("hello");
            assertThat(map.containsKey(String.class)).isFalse();
        }

        @Test
        @DisplayName("putAll - 批量放置")
        void testPutAll() {
            MutableClassToInstanceMap<Object> source = MutableClassToInstanceMap.create();
            source.putInstance(String.class, "hello");

            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putAll(source);

            assertThat(map.getInstance(String.class)).isEqualTo("hello");
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            map.clear();

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("keySet - 键集")
        void testKeySet() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");
            map.putInstance(Integer.class, 42);

            assertThat(map.keySet()).containsExactlyInAnyOrder(String.class, Integer.class);
        }

        @Test
        @DisplayName("values - 值集合")
        void testValues() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");
            map.putInstance(Integer.class, 42);

            assertThat(map.values()).containsExactlyInAnyOrder("hello", 42);
        }

        @Test
        @DisplayName("entrySet - 条目集")
        void testEntrySet() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            assertThat(map.entrySet()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("类型安全测试")
    class TypeSafetyTests {

        @Test
        @DisplayName("多种类型共存")
        void testMultipleTypes() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            map.putInstance(String.class, "hello");
            map.putInstance(Integer.class, 42);
            map.putInstance(Double.class, 3.14);
            map.putInstance(Boolean.class, true);

            assertThat(map.getInstance(String.class)).isEqualTo("hello");
            assertThat(map.getInstance(Integer.class)).isEqualTo(42);
            assertThat(map.getInstance(Double.class)).isEqualTo(3.14);
            assertThat(map.getInstance(Boolean.class)).isEqualTo(true);
        }

        @Test
        @DisplayName("子类型实例")
        void testSubtypeInstance() {
            MutableClassToInstanceMap<Number> map = MutableClassToInstanceMap.create();

            map.putInstance(Integer.class, 42);
            map.putInstance(Long.class, 100L);

            assertThat(map.getInstance(Integer.class)).isEqualTo(42);
            assertThat(map.getInstance(Long.class)).isEqualTo(100L);
        }

        @Test
        @DisplayName("类型转换安全")
        void testTypeCastSafety() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            // 获取的类型安全，不需要强制转换
            String value = map.getInstance(String.class);

            assertThat(value).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            MutableClassToInstanceMap<Object> map1 = MutableClassToInstanceMap.create();
            map1.putInstance(String.class, "hello");

            MutableClassToInstanceMap<Object> map2 = MutableClassToInstanceMap.create();
            map2.putInstance(String.class, "hello");

            assertThat(map1).isEqualTo(map2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            MutableClassToInstanceMap<Object> map1 = MutableClassToInstanceMap.create();
            map1.putInstance(String.class, "hello");

            MutableClassToInstanceMap<Object> map2 = MutableClassToInstanceMap.create();
            map2.putInstance(String.class, "hello");

            assertThat(map1.hashCode()).isEqualTo(map2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            assertThat(map.toString()).contains("String").contains("hello");
        }
    }
}
