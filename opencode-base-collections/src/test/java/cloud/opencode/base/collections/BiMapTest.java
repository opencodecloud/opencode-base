package cloud.opencode.base.collections;

import org.junit.jupiter.api.*;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * BiMapTest Tests
 * BiMapTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("BiMap 接口测试")
class BiMapTest {

    @Nested
    @DisplayName("通过HashBiMap实现测试接口契约")
    class InterfaceContractTests {

        @Test
        @DisplayName("put添加键值对")
        void testPut() {
            BiMap<String, Integer> bimap = HashBiMap.create();

            bimap.put("one", 1);

            assertThat(bimap.get("one")).isEqualTo(1);
        }

        @Test
        @DisplayName("put重复值抛出异常")
        void testPutDuplicateValueThrows() {
            BiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("one", 1);

            assertThatThrownBy(() -> bimap.put("two", 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("forcePut替换已有映射")
        void testForcePut() {
            BiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("one", 1);

            bimap.forcePut("two", 1);

            assertThat(bimap.containsKey("one")).isFalse();
            assertThat(bimap.get("two")).isEqualTo(1);
        }

        @Test
        @DisplayName("inverse返回反向视图")
        void testInverse() {
            BiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("one", 1);
            bimap.put("two", 2);

            BiMap<Integer, String> inverse = bimap.inverse();

            assertThat(inverse.get(1)).isEqualTo("one");
            assertThat(inverse.get(2)).isEqualTo("two");
        }

        @Test
        @DisplayName("values返回Set视图")
        void testValuesReturnSet() {
            BiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("one", 1);
            bimap.put("two", 2);

            Set<Integer> values = bimap.values();

            assertThat(values).containsExactlyInAnyOrder(1, 2);
        }
    }

    @Nested
    @DisplayName("继承Map接口测试")
    class MapInterfaceTests {

        @Test
        @DisplayName("size返回正确大小")
        void testSize() {
            BiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);
            bimap.put("b", 2);

            assertThat(bimap.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty检查空映射")
        void testIsEmpty() {
            BiMap<String, Integer> bimap = HashBiMap.create();

            assertThat(bimap.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("containsKey检查键存在")
        void testContainsKey() {
            BiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("one", 1);

            assertThat(bimap.containsKey("one")).isTrue();
            assertThat(bimap.containsKey("two")).isFalse();
        }
    }
}
