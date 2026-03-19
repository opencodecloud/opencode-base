package cloud.opencode.base.reflect.bean;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenBeanTest Tests
 * OpenBeanTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("OpenBean 测试")
class OpenBeanTest {

    @BeforeEach
    void setUp() {
        OpenBean.clearAllCache();
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = OpenBean.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getPropertyDescriptors方法测试")
    class GetPropertyDescriptorsTests {

        @Test
        @DisplayName("获取所有属性描述符")
        void testGetPropertyDescriptors() {
            Map<String, PropertyDescriptor> descriptors = OpenBean.getPropertyDescriptors(TestBean.class);
            assertThat(descriptors).containsKey("name");
            assertThat(descriptors).containsKey("age");
        }

        @Test
        @DisplayName("缓存描述符")
        void testGetPropertyDescriptorsCached() {
            Map<String, PropertyDescriptor> d1 = OpenBean.getPropertyDescriptors(TestBean.class);
            Map<String, PropertyDescriptor> d2 = OpenBean.getPropertyDescriptors(TestBean.class);
            assertThat(d1).isSameAs(d2);
        }
    }

    @Nested
    @DisplayName("getPropertyDescriptor方法测试")
    class GetPropertyDescriptorTests {

        @Test
        @DisplayName("按名称获取描述符")
        void testGetPropertyDescriptor() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "name");
            assertThat(descriptor).isNotNull();
            assertThat(descriptor.getName()).isEqualTo("name");
        }

        @Test
        @DisplayName("不存在的属性返回null")
        void testGetPropertyDescriptorNotFound() {
            PropertyDescriptor descriptor = OpenBean.getPropertyDescriptor(TestBean.class, "nonexistent");
            assertThat(descriptor).isNull();
        }
    }

    @Nested
    @DisplayName("getPropertyNames方法测试")
    class GetPropertyNamesTests {

        @Test
        @DisplayName("获取所有属性名")
        void testGetPropertyNames() {
            Set<String> names = OpenBean.getPropertyNames(TestBean.class);
            assertThat(names).contains("name", "age");
        }
    }

    @Nested
    @DisplayName("getReadablePropertyNames方法测试")
    class GetReadablePropertyNamesTests {

        @Test
        @DisplayName("获取可读属性名")
        void testGetReadablePropertyNames() {
            Set<String> names = OpenBean.getReadablePropertyNames(TestBean.class);
            assertThat(names).contains("name", "age");
        }
    }

    @Nested
    @DisplayName("getWritablePropertyNames方法测试")
    class GetWritablePropertyNamesTests {

        @Test
        @DisplayName("获取可写属性名")
        void testGetWritablePropertyNames() {
            Set<String> names = OpenBean.getWritablePropertyNames(TestBean.class);
            assertThat(names).contains("name", "age");
        }
    }

    @Nested
    @DisplayName("getProperty方法测试")
    class GetPropertyTests {

        @Test
        @DisplayName("获取属性值")
        void testGetProperty() {
            TestBean bean = new TestBean();
            bean.setName("John");
            Object value = OpenBean.getProperty(bean, "name");
            assertThat(value).isEqualTo("John");
        }

        @Test
        @DisplayName("获取带类型的属性值")
        void testGetPropertyWithType() {
            TestBean bean = new TestBean();
            bean.setName("John");
            String value = OpenBean.getProperty(bean, "name", String.class);
            assertThat(value).isEqualTo("John");
        }

        @Test
        @DisplayName("不存在的属性抛出异常")
        void testGetPropertyNotFound() {
            TestBean bean = new TestBean();
            assertThatThrownBy(() -> OpenBean.getProperty(bean, "nonexistent"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("setProperty方法测试")
    class SetPropertyTests {

        @Test
        @DisplayName("设置属性值")
        void testSetProperty() {
            TestBean bean = new TestBean();
            OpenBean.setProperty(bean, "name", "John");
            assertThat(bean.getName()).isEqualTo("John");
        }

        @Test
        @DisplayName("不存在的属性抛出异常")
        void testSetPropertyNotFound() {
            TestBean bean = new TestBean();
            assertThatThrownBy(() -> OpenBean.setProperty(bean, "nonexistent", "value"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("setPropertyIfWritable方法测试")
    class SetPropertyIfWritableTests {

        @Test
        @DisplayName("可写属性设置成功")
        void testSetPropertyIfWritableSuccess() {
            TestBean bean = new TestBean();
            boolean result = OpenBean.setPropertyIfWritable(bean, "name", "John");
            assertThat(result).isTrue();
            assertThat(bean.getName()).isEqualTo("John");
        }

        @Test
        @DisplayName("不可写属性返回false")
        void testSetPropertyIfWritableFailed() {
            TestBean bean = new TestBean();
            boolean result = OpenBean.setPropertyIfWritable(bean, "nonexistent", "value");
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("copyProperties方法测试")
    class CopyPropertiesTests {

        @Test
        @DisplayName("复制属性")
        void testCopyProperties() {
            TestBean source = new TestBean();
            source.setName("John");
            source.setAge(25);

            TestBean target = new TestBean();
            OpenBean.copyProperties(source, target);

            assertThat(target.getName()).isEqualTo("John");
            assertThat(target.getAge()).isEqualTo(25);
        }

        @Test
        @DisplayName("排除属性复制")
        void testCopyPropertiesExclude() {
            TestBean source = new TestBean();
            source.setName("John");
            source.setAge(25);

            TestBean target = new TestBean();
            target.setAge(0);
            OpenBean.copyProperties(source, target, "age");

            assertThat(target.getName()).isEqualTo("John");
            assertThat(target.getAge()).isEqualTo(0);
        }

        @Test
        @DisplayName("复制并创建新实例")
        void testCopyPropertiesAndCreate() {
            TestBean source = new TestBean();
            source.setName("John");
            source.setAge(25);

            TestBean target = OpenBean.copyProperties(source, TestBean.class);

            assertThat(target.getName()).isEqualTo("John");
            assertThat(target.getAge()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("toMap方法测试")
    class ToMapTests {

        @Test
        @DisplayName("Bean转Map")
        void testToMap() {
            TestBean bean = new TestBean();
            bean.setName("John");
            bean.setAge(25);

            Map<String, Object> map = OpenBean.toMap(bean);
            assertThat(map).containsEntry("name", "John");
            assertThat(map).containsEntry("age", 25);
        }
    }

    @Nested
    @DisplayName("asBeanMap方法测试")
    class AsBeanMapTests {

        @Test
        @DisplayName("创建BeanMap视图")
        void testAsBeanMap() {
            TestBean bean = new TestBean();
            bean.setName("John");

            BeanMap<TestBean> beanMap = OpenBean.asBeanMap(bean);
            assertThat(beanMap.get("name")).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("populate方法测试")
    class PopulateTests {

        @Test
        @DisplayName("从Map填充Bean")
        void testPopulate() {
            TestBean bean = new TestBean();
            Map<String, Object> map = Map.of("name", "John", "age", 25);

            OpenBean.populate(bean, map);

            assertThat(bean.getName()).isEqualTo("John");
            assertThat(bean.getAge()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("fromMap方法测试")
    class FromMapTests {

        @Test
        @DisplayName("从Map创建Bean")
        void testFromMap() {
            Map<String, Object> map = Map.of("name", "John", "age", 25);

            TestBean bean = OpenBean.fromMap(map, TestBean.class);

            assertThat(bean.getName()).isEqualTo("John");
            assertThat(bean.getAge()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("createCopier方法测试")
    class CreateCopierTests {

        @Test
        @DisplayName("创建BeanCopier")
        void testCreateCopier() {
            BeanCopier<TestBean, TestBean> copier = OpenBean.createCopier(TestBean.class, TestBean.class);
            assertThat(copier).isNotNull();
        }
    }

    @Nested
    @DisplayName("hasProperty方法测试")
    class HasPropertyTests {

        @Test
        @DisplayName("有属性返回true")
        void testHasPropertyTrue() {
            assertThat(OpenBean.hasProperty(TestBean.class, "name")).isTrue();
        }

        @Test
        @DisplayName("无属性返回false")
        void testHasPropertyFalse() {
            assertThat(OpenBean.hasProperty(TestBean.class, "nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("clearCache方法测试")
    class ClearCacheTests {

        @Test
        @DisplayName("清除类缓存")
        void testClearCache() {
            OpenBean.getPropertyDescriptors(TestBean.class);
            OpenBean.clearCache(TestBean.class);
            // No assertion, just verify no exception
        }

        @Test
        @DisplayName("清除所有缓存")
        void testClearAllCache() {
            OpenBean.getPropertyDescriptors(TestBean.class);
            OpenBean.clearAllCache();
            // No assertion, just verify no exception
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
