package cloud.opencode.base.core.builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MapBuilder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("MapBuilder 测试")
class MapBuilderTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of 默认")
        void testOf() {
            MapBuilder<String, Object> builder = MapBuilder.of();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("of 自定义 Supplier")
        void testOfWithSupplier() {
            MapBuilder<String, Object> builder = MapBuilder.of(TreeMap::new);
            Map<String, Object> map = builder.put("key", "value").build();
            assertThat(map).isInstanceOf(TreeMap.class);
        }

        @Test
        @DisplayName("hashMap")
        void testHashMap() {
            Map<String, Object> map = MapBuilder.<String, Object>hashMap()
                    .put("key", "value")
                    .build();
            assertThat(map).isInstanceOf(HashMap.class);
        }

        @Test
        @DisplayName("linkedHashMap")
        void testLinkedHashMap() {
            Map<String, Object> map = MapBuilder.<String, Object>linkedHashMap()
                    .put("key", "value")
                    .build();
            assertThat(map).isInstanceOf(LinkedHashMap.class);
        }

        @Test
        @DisplayName("treeMap")
        void testTreeMap() {
            Map<String, Object> map = MapBuilder.<String, Object>treeMap()
                    .put("key", "value")
                    .build();
            assertThat(map).isInstanceOf(TreeMap.class);
        }
    }

    @Nested
    @DisplayName("put 测试")
    class PutTests {

        @Test
        @DisplayName("put 添加键值对")
        void testPut() {
            Map<String, Object> map = MapBuilder.<String, Object>of()
                    .put("key1", "value1")
                    .put("key2", "value2")
                    .build();

            assertThat(map).hasSize(2);
            assertThat(map.get("key1")).isEqualTo("value1");
            assertThat(map.get("key2")).isEqualTo("value2");
        }

        @Test
        @DisplayName("put 链式调用")
        void testPutChaining() {
            MapBuilder<String, Object> builder = MapBuilder.of();
            MapBuilder<String, Object> result = builder.put("key", "value");
            assertThat(result).isSameAs(builder);
        }

        @Test
        @DisplayName("put 覆盖已有值")
        void testPutOverwrite() {
            Map<String, Object> map = MapBuilder.<String, Object>of()
                    .put("key", "value1")
                    .put("key", "value2")
                    .build();

            assertThat(map.get("key")).isEqualTo("value2");
        }

        @Test
        @DisplayName("put null 值")
        void testPutNullValue() {
            Map<String, Object> map = MapBuilder.<String, Object>of()
                    .put("key", null)
                    .build();

            assertThat(map.containsKey("key")).isTrue();
            assertThat(map.get("key")).isNull();
        }
    }

    @Nested
    @DisplayName("putIfNotNull 测试")
    class PutIfNotNullTests {

        @Test
        @DisplayName("putIfNotNull 非 null 时添加")
        void testPutIfNotNullWithValue() {
            Map<String, Object> map = MapBuilder.<String, Object>of()
                    .putIfNotNull("key", "value")
                    .build();

            assertThat(map.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("putIfNotNull null 时不添加")
        void testPutIfNotNullWithNull() {
            Map<String, Object> map = MapBuilder.<String, Object>of()
                    .putIfNotNull("key", null)
                    .build();

            assertThat(map.containsKey("key")).isFalse();
        }
    }

    @Nested
    @DisplayName("putIf 测试")
    class PutIfTests {

        @Test
        @DisplayName("putIf 条件为 true 时添加")
        void testPutIfTrue() {
            Map<String, Object> map = MapBuilder.<String, Object>of()
                    .putIf(true, "key", "value")
                    .build();

            assertThat(map.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("putIf 条件为 false 时不添加")
        void testPutIfFalse() {
            Map<String, Object> map = MapBuilder.<String, Object>of()
                    .putIf(false, "key", "value")
                    .build();

            assertThat(map.containsKey("key")).isFalse();
        }
    }

    @Nested
    @DisplayName("putAll 测试")
    class PutAllTests {

        @Test
        @DisplayName("putAll 批量添加")
        void testPutAll() {
            Map<String, Object> source = Map.of("key1", "value1", "key2", "value2");

            Map<String, Object> map = MapBuilder.<String, Object>of()
                    .putAll(source)
                    .build();

            assertThat(map).hasSize(2);
            assertThat(map.get("key1")).isEqualTo("value1");
            assertThat(map.get("key2")).isEqualTo("value2");
        }
    }

    @Nested
    @DisplayName("remove 测试")
    class RemoveTests {

        @Test
        @DisplayName("remove 移除键")
        void testRemove() {
            Map<String, Object> map = MapBuilder.<String, Object>of()
                    .put("key1", "value1")
                    .put("key2", "value2")
                    .remove("key1")
                    .build();

            assertThat(map.containsKey("key1")).isFalse();
            assertThat(map.get("key2")).isEqualTo("value2");
        }
    }

    @Nested
    @DisplayName("unmodifiable 测试")
    class UnmodifiableTests {

        @Test
        @DisplayName("unmodifiable 创建不可变 Map")
        void testUnmodifiable() {
            Map<String, Object> map = MapBuilder.<String, Object>of()
                    .put("key", "value")
                    .unmodifiable()
                    .build();

            assertThatThrownBy(() -> map.put("new", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("unmodifiable 保持内容")
        void testUnmodifiableContent() {
            Map<String, Object> map = MapBuilder.<String, Object>of()
                    .put("key", "value")
                    .unmodifiable()
                    .build();

            assertThat(map.get("key")).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("configure 测试")
    class ConfigureTests {

        @Test
        @DisplayName("configure 回调配置")
        void testConfigure() {
            Map<String, Object> map = MapBuilder.<String, Object>of()
                    .configure(builder -> {
                        builder.put("key1", "value1");
                        builder.put("key2", "value2");
                    })
                    .build();

            assertThat(map).hasSize(2);
        }
    }

    @Nested
    @DisplayName("build 测试")
    class BuildTests {

        @Test
        @DisplayName("build 返回新实例")
        void testBuildReturnsNewInstance() {
            MapBuilder<String, Object> builder = MapBuilder.<String, Object>of()
                    .put("key", "value");

            Map<String, Object> map1 = builder.build();
            Map<String, Object> map2 = builder.build();

            assertThat(map1).isNotSameAs(map2);
        }

        @Test
        @DisplayName("build 保持插入顺序 (LinkedHashMap)")
        void testBuildPreservesInsertionOrder() {
            Map<String, Object> map = MapBuilder.<String, Object>linkedHashMap()
                    .put("c", 3)
                    .put("a", 1)
                    .put("b", 2)
                    .build();

            assertThat(map.keySet()).containsExactly("c", "a", "b");
        }

        @Test
        @DisplayName("build 排序 (TreeMap)")
        void testBuildSorted() {
            Map<String, Object> map = MapBuilder.<String, Object>treeMap()
                    .put("c", 3)
                    .put("a", 1)
                    .put("b", 2)
                    .build();

            assertThat(map.keySet()).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("size 测试")
    class SizeTests {

        @Test
        @DisplayName("size 返回当前大小")
        void testSize() {
            MapBuilder<String, Object> builder = MapBuilder.<String, Object>of()
                    .put("key1", "value1")
                    .put("key2", "value2");

            assertThat(builder.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("size 初始为 0")
        void testSizeInitiallyZero() {
            MapBuilder<String, Object> builder = MapBuilder.of();
            assertThat(builder.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("containsKey 测试")
    class ContainsKeyTests {

        @Test
        @DisplayName("containsKey 存在的键")
        void testContainsKeyExists() {
            MapBuilder<String, Object> builder = MapBuilder.<String, Object>of()
                    .put("key", "value");

            assertThat(builder.containsKey("key")).isTrue();
        }

        @Test
        @DisplayName("containsKey 不存在的键")
        void testContainsKeyNotExists() {
            MapBuilder<String, Object> builder = MapBuilder.<String, Object>of()
                    .put("key", "value");

            assertThat(builder.containsKey("other")).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder 接口实现测试")
    class BuilderInterfaceTests {

        @Test
        @DisplayName("实现 Builder 接口")
        void testImplementsBuilder() {
            MapBuilder<String, Object> builder = MapBuilder.of();
            assertThat(builder).isInstanceOf(Builder.class);
        }
    }
}
