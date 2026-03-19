package cloud.opencode.base.reflect.bean;

import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * BeanMapTest Tests
 * BeanMapTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("BeanMap 测试")
class BeanMapTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建BeanMap")
        void testCreate() {
            TestBean bean = new TestBean();
            Map<String, PropertyDescriptor> descriptors = OpenBean.getPropertyDescriptors(TestBean.class);
            BeanMap<TestBean> beanMap = new BeanMap<>(bean, descriptors);
            assertThat(beanMap).isNotNull();
        }

        @Test
        @DisplayName("null bean抛出异常")
        void testCreateNullBean() {
            Map<String, PropertyDescriptor> descriptors = OpenBean.getPropertyDescriptors(TestBean.class);
            assertThatThrownBy(() -> new BeanMap<>(null, descriptors))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("from静态方法测试")
    class FromTests {

        @Test
        @DisplayName("从bean创建BeanMap")
        void testFrom() {
            TestBean bean = new TestBean();
            bean.setName("test");
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            assertThat(beanMap).isNotNull();
            assertThat(beanMap.get("name")).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("getBean方法测试")
    class GetBeanTests {

        @Test
        @DisplayName("获取底层bean")
        void testGetBean() {
            TestBean bean = new TestBean();
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            assertThat(beanMap.getBean()).isSameAs(bean);
        }
    }

    @Nested
    @DisplayName("get方法测试")
    class GetTests {

        @Test
        @DisplayName("获取属性值")
        void testGet() {
            TestBean bean = new TestBean();
            bean.setName("hello");
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            assertThat(beanMap.get("name")).isEqualTo("hello");
        }

        @Test
        @DisplayName("不存在的属性返回null")
        void testGetNotFound() {
            TestBean bean = new TestBean();
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            assertThat(beanMap.get("nonexistent")).isNull();
        }

        @Test
        @DisplayName("非String键返回null")
        void testGetNonString() {
            TestBean bean = new TestBean();
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            assertThat(beanMap.get(123)).isNull();
        }
    }

    @Nested
    @DisplayName("put方法测试")
    class PutTests {

        @Test
        @DisplayName("设置属性值")
        void testPut() {
            TestBean bean = new TestBean();
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            beanMap.put("name", "newValue");
            assertThat(bean.getName()).isEqualTo("newValue");
        }

        @Test
        @DisplayName("返回旧值")
        void testPutReturnsOld() {
            TestBean bean = new TestBean();
            bean.setName("old");
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            Object oldValue = beanMap.put("name", "new");
            assertThat(oldValue).isEqualTo("old");
        }

        @Test
        @DisplayName("未知属性被忽略")
        void testPutUnknown() {
            TestBean bean = new TestBean();
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            Object result = beanMap.put("unknown", "value");
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("containsKey方法测试")
    class ContainsKeyTests {

        @Test
        @DisplayName("存在的键返回true")
        void testContainsKeyTrue() {
            TestBean bean = new TestBean();
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            assertThat(beanMap.containsKey("name")).isTrue();
        }

        @Test
        @DisplayName("不存在的键返回false")
        void testContainsKeyFalse() {
            TestBean bean = new TestBean();
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            assertThat(beanMap.containsKey("nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("keySet方法测试")
    class KeySetTests {

        @Test
        @DisplayName("获取所有键")
        void testKeySet() {
            TestBean bean = new TestBean();
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            Set<String> keys = beanMap.keySet();
            assertThat(keys).contains("name", "age");
        }
    }

    @Nested
    @DisplayName("entrySet方法测试")
    class EntrySetTests {

        @Test
        @DisplayName("获取所有条目")
        void testEntrySet() {
            TestBean bean = new TestBean();
            bean.setName("test");
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            assertThat(beanMap.entrySet()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("size方法测试")
    class SizeTests {

        @Test
        @DisplayName("获取大小")
        void testSize() {
            TestBean bean = new TestBean();
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            assertThat(beanMap.size()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("copyTo方法测试")
    class CopyToTests {

        @Test
        @DisplayName("复制到另一个bean")
        void testCopyTo() {
            TestBean source = new TestBean();
            source.setName("copied");
            source.setAge(25);
            TestBean target = new TestBean();

            BeanMap.from(source).copyTo(target);
            assertThat(target.getName()).isEqualTo("copied");
            assertThat(target.getAge()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("copyFrom方法测试")
    class CopyFromTests {

        @Test
        @DisplayName("从Map复制")
        void testCopyFrom() {
            TestBean bean = new TestBean();
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            beanMap.copyFrom(Map.of("name", "fromMap", "age", 30));
            assertThat(bean.getName()).isEqualTo("fromMap");
            assertThat(bean.getAge()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("toMap方法测试")
    class ToMapTests {

        @Test
        @DisplayName("转换为普通Map")
        void testToMap() {
            TestBean bean = new TestBean();
            bean.setName("test");
            bean.setAge(20);
            Map<String, Object> map = BeanMap.from(bean).toMap();
            assertThat(map).containsEntry("name", "test");
            assertThat(map).containsEntry("age", 20);
        }
    }

    @Nested
    @DisplayName("getReadableProperties方法测试")
    class GetReadablePropertiesTests {

        @Test
        @DisplayName("获取可读属性")
        void testGetReadableProperties() {
            TestBean bean = new TestBean();
            bean.setName("test");
            Map<String, Object> readable = BeanMap.from(bean).getReadableProperties();
            assertThat(readable).containsKey("name");
        }
    }

    @Nested
    @DisplayName("getPropertyDescriptor方法测试")
    class GetPropertyDescriptorTests {

        @Test
        @DisplayName("获取属性描述符")
        void testGetPropertyDescriptor() {
            TestBean bean = new TestBean();
            BeanMap<TestBean> beanMap = BeanMap.from(bean);
            PropertyDescriptor descriptor = beanMap.getPropertyDescriptor("name");
            assertThat(descriptor).isNotNull();
        }
    }

    // Test helper class
    public static class TestBean {
        private String name;
        private int age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
}
