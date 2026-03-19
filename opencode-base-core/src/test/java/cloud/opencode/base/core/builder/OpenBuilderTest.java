package cloud.opencode.base.core.builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenBuilder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenBuilder 测试")
class OpenBuilderTest {

    @Nested
    @DisplayName("ofBean 测试")
    class OfBeanTests {

        @Test
        @DisplayName("ofBean 创建构建器")
        void testOfBean() {
            BeanBuilder<TestBean> builder = OpenBuilder.ofBean(TestBean.class);
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("ofBean 构建对象")
        void testOfBeanBuild() {
            TestBean bean = OpenBuilder.ofBean(TestBean.class)
                    .set("name", "John")
                    .set("age", 25)
                    .build();

            assertThat(bean.getName()).isEqualTo("John");
            assertThat(bean.getAge()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("from 测试")
    class FromTests {

        @Test
        @DisplayName("from 从现有实例创建")
        void testFrom() {
            TestBean source = new TestBean();
            source.setName("Original");
            source.setAge(30);

            TestBean copy = OpenBuilder.from(source)
                    .set("name", "Modified")
                    .build();

            assertThat(copy.getName()).isEqualTo("Modified");
            assertThat(copy.getAge()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("ofRecord 测试")
    class OfRecordTests {

        @Test
        @DisplayName("ofRecord 创建构建器")
        void testOfRecord() {
            RecordBuilder<TestRecord> builder = OpenBuilder.ofRecord(TestRecord.class);
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("ofRecord 构建 Record")
        void testOfRecordBuild() {
            TestRecord record = OpenBuilder.ofRecord(TestRecord.class)
                    .set("name", "John")
                    .set("age", 25)
                    .build();

            assertThat(record.name()).isEqualTo("John");
            assertThat(record.age()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("fromRecord 测试")
    class FromRecordTests {

        @Test
        @DisplayName("fromRecord 从现有 Record 创建")
        void testFromRecord() {
            TestRecord source = new TestRecord("Original", 30);

            TestRecord modified = OpenBuilder.fromRecord(source)
                    .set("name", "Modified")
                    .build();

            assertThat(modified.name()).isEqualTo("Modified");
            assertThat(modified.age()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("ofMap 测试")
    class OfMapTests {

        @Test
        @DisplayName("ofMap 默认")
        void testOfMap() {
            Map<String, Object> map = OpenBuilder.<String, Object>ofMap()
                    .put("key1", "value1")
                    .put("key2", "value2")
                    .build();

            assertThat(map).hasSize(2);
            assertThat(map.get("key1")).isEqualTo("value1");
        }

        @Test
        @DisplayName("ofMap 自定义 Supplier")
        void testOfMapWithSupplier() {
            Map<String, Object> map = OpenBuilder.<String, Object>ofMap(TreeMap::new)
                    .put("b", 2)
                    .put("a", 1)
                    .build();

            assertThat(map).isInstanceOf(TreeMap.class);
        }
    }

    @Nested
    @DisplayName("ofHashMap 测试")
    class OfHashMapTests {

        @Test
        @DisplayName("ofHashMap 创建 HashMap")
        void testOfHashMap() {
            Map<String, Integer> map = OpenBuilder.<String, Integer>ofHashMap()
                    .put("one", 1)
                    .put("two", 2)
                    .build();

            assertThat(map).isInstanceOf(HashMap.class);
            assertThat(map).hasSize(2);
        }
    }

    @Nested
    @DisplayName("ofLinkedHashMap 测试")
    class OfLinkedHashMapTests {

        @Test
        @DisplayName("ofLinkedHashMap 创建 LinkedHashMap")
        void testOfLinkedHashMap() {
            Map<String, Integer> map = OpenBuilder.<String, Integer>ofLinkedHashMap()
                    .put("one", 1)
                    .put("two", 2)
                    .build();

            assertThat(map).isInstanceOf(LinkedHashMap.class);
        }

        @Test
        @DisplayName("ofLinkedHashMap 保持顺序")
        void testOfLinkedHashMapOrder() {
            Map<String, Integer> map = OpenBuilder.<String, Integer>ofLinkedHashMap()
                    .put("c", 3)
                    .put("a", 1)
                    .put("b", 2)
                    .build();

            assertThat(map.keySet()).containsExactly("c", "a", "b");
        }
    }

    @Nested
    @DisplayName("ofTreeMap 测试")
    class OfTreeMapTests {

        @Test
        @DisplayName("ofTreeMap 创建 TreeMap")
        void testOfTreeMap() {
            Map<String, Integer> map = OpenBuilder.<String, Integer>ofTreeMap()
                    .put("c", 3)
                    .put("a", 1)
                    .put("b", 2)
                    .build();

            assertThat(map).isInstanceOf(TreeMap.class);
        }

        @Test
        @DisplayName("ofTreeMap 排序")
        void testOfTreeMapSorted() {
            Map<String, Integer> map = OpenBuilder.<String, Integer>ofTreeMap()
                    .put("c", 3)
                    .put("a", 1)
                    .put("b", 2)
                    .build();

            assertThat(map.keySet()).containsExactly("a", "b", "c");
        }
    }

    public static class TestBean {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    public record TestRecord(String name, int age) {}
}
