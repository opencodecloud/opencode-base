package cloud.opencode.base.collections.specialized;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ClassToInstanceMap 接口测试
 * 通过 MutableClassToInstanceMap 实现类测试接口的所有方法
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ClassToInstanceMap 接口测试")
class ClassToInstanceMapTest {

    @Nested
    @DisplayName("getInstance 方法测试")
    class GetInstanceMethodTests {

        @Test
        @DisplayName("getInstance - 获取存在的实例")
        void testGetInstanceExists() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            String value = map.getInstance(String.class);

            assertThat(value).isEqualTo("hello");
        }

        @Test
        @DisplayName("getInstance - 获取不存在的实例返回 null")
        void testGetInstanceNotExists() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            String value = map.getInstance(String.class);

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("getInstance - 类型安全获取")
        void testGetInstanceTypeSafe() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(Integer.class, 42);

            Integer value = map.getInstance(Integer.class);

            assertThat(value).isEqualTo(42);
        }

        @Test
        @DisplayName("getInstance - 不同类型")
        void testGetInstanceMultipleTypes() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");
            map.putInstance(Integer.class, 42);
            map.putInstance(Double.class, 3.14);

            assertThat(map.getInstance(String.class)).isEqualTo("hello");
            assertThat(map.getInstance(Integer.class)).isEqualTo(42);
            assertThat(map.getInstance(Double.class)).isEqualTo(3.14);
        }
    }

    @Nested
    @DisplayName("putInstance 方法测试")
    class PutInstanceMethodTests {

        @Test
        @DisplayName("putInstance - 放置新实例")
        void testPutInstanceNew() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            String old = map.putInstance(String.class, "hello");

            assertThat(old).isNull();
            assertThat(map.getInstance(String.class)).isEqualTo("hello");
        }

        @Test
        @DisplayName("putInstance - 替换实例")
        void testPutInstanceReplace() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            String old = map.putInstance(String.class, "world");

            assertThat(old).isEqualTo("hello");
            assertThat(map.getInstance(String.class)).isEqualTo("world");
        }

        @Test
        @DisplayName("putInstance - null 类型抛异常")
        void testPutInstanceNullType() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            assertThatThrownBy(() -> map.putInstance(null, "hello"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("putInstance - null 值抛异常")
        void testPutInstanceNullValue() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            assertThatThrownBy(() -> map.putInstance(String.class, null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    @DisplayName("Map 接口方法测试")
    class MapInterfaceMethodTests {

        @Test
        @DisplayName("继承 Map 接口")
        void testExtendsMap() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            assertThat(map).isInstanceOf(Map.class);
        }

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");
            map.putInstance(Integer.class, 42);

            assertThat(map.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

            assertThat(map.isEmpty()).isTrue();

            map.putInstance(String.class, "hello");

            assertThat(map.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("containsKey - 包含键")
        void testContainsKey() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            assertThat(map.containsKey(String.class)).isTrue();
            assertThat(map.containsKey(Integer.class)).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            assertThat(map.containsValue("hello")).isTrue();
            assertThat(map.containsValue("world")).isFalse();
        }

        @Test
        @DisplayName("get - 通过类获取")
        void testGet() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            assertThat(map.get(String.class)).isEqualTo("hello");
        }

        @Test
        @DisplayName("remove - 移除")
        void testRemove() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            Object removed = map.remove(String.class);

            assertThat(removed).isEqualTo("hello");
            assertThat(map.containsKey(String.class)).isFalse();
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");
            map.putInstance(Integer.class, 42);

            map.clear();

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("keySet - 键集")
        void testKeySet() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");
            map.putInstance(Integer.class, 42);

            assertThat(map.keySet()).containsExactlyInAnyOrder(String.class, Integer.class);
        }

        @Test
        @DisplayName("values - 值集合")
        void testValues() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");
            map.putInstance(Integer.class, 42);

            assertThat(map.values()).containsExactlyInAnyOrder("hello", 42);
        }

        @Test
        @DisplayName("entrySet - 条目集")
        void testEntrySet() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            assertThat(map.entrySet()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("类型层次结构测试")
    class TypeHierarchyTests {

        @Test
        @DisplayName("基类约束")
        void testBaseTypeConstraint() {
            ClassToInstanceMap<Number> map = MutableClassToInstanceMap.create();

            map.putInstance(Integer.class, 42);
            map.putInstance(Long.class, 100L);
            map.putInstance(Double.class, 3.14);

            assertThat(map.getInstance(Integer.class)).isEqualTo(42);
            assertThat(map.getInstance(Long.class)).isEqualTo(100L);
            assertThat(map.getInstance(Double.class)).isEqualTo(3.14);
        }

        @Test
        @DisplayName("接口类型实例")
        void testInterfaceType() {
            ClassToInstanceMap<CharSequence> map = MutableClassToInstanceMap.create();

            map.putInstance(String.class, "hello");

            assertThat(map.getInstance(String.class)).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            ClassToInstanceMap<Object> map1 = MutableClassToInstanceMap.create();
            map1.putInstance(String.class, "hello");

            ClassToInstanceMap<Object> map2 = MutableClassToInstanceMap.create();
            map2.putInstance(String.class, "hello");

            assertThat(map1).isEqualTo(map2);
        }

        @Test
        @DisplayName("hashCode - 相同哈希码")
        void testHashCode() {
            ClassToInstanceMap<Object> map1 = MutableClassToInstanceMap.create();
            map1.putInstance(String.class, "hello");

            ClassToInstanceMap<Object> map2 = MutableClassToInstanceMap.create();
            map2.putInstance(String.class, "hello");

            assertThat(map1.hashCode()).isEqualTo(map2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
            map.putInstance(String.class, "hello");

            String str = map.toString();

            assertThat(str).contains("String");
            assertThat(str).contains("hello");
        }
    }
}
