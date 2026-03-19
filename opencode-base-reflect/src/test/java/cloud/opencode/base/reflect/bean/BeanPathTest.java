package cloud.opencode.base.reflect.bean;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * BeanPathTest Tests
 * BeanPathTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("BeanPath 测试")
class BeanPathTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = BeanPath.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("get方法测试")
    class GetTests {

        @Test
        @DisplayName("获取简单属性")
        void testGetSimple() {
            Person person = new Person();
            person.setName("John");
            assertThat(BeanPath.get(person, "name")).isEqualTo("John");
        }

        @Test
        @DisplayName("获取嵌套属性")
        void testGetNested() {
            Person person = new Person();
            Address address = new Address();
            address.setCity("Beijing");
            person.setAddress(address);
            assertThat(BeanPath.get(person, "address.city")).isEqualTo("Beijing");
        }

        @Test
        @DisplayName("null bean返回null")
        void testGetNullBean() {
            assertThat(BeanPath.get(null, "name")).isNull();
        }

        @Test
        @DisplayName("中间属性为null返回null")
        void testGetIntermediateNull() {
            Person person = new Person();
            assertThat(BeanPath.get(person, "address.city")).isNull();
        }

        @Test
        @DisplayName("获取索引属性")
        void testGetIndexed() {
            Person person = new Person();
            person.setTags(new ArrayList<>(List.of("tag1", "tag2")));
            assertThat(BeanPath.get(person, "tags[0]")).isEqualTo("tag1");
        }
    }

    @Nested
    @DisplayName("get带类型方法测试")
    class GetWithTypeTests {

        @Test
        @DisplayName("获取带类型的值")
        void testGetWithType() {
            Person person = new Person();
            person.setName("John");
            String name = BeanPath.get(person, "name", String.class);
            assertThat(name).isEqualTo("John");
        }

        @Test
        @DisplayName("类型不匹配抛出异常")
        void testGetWithTypeWrong() {
            Person person = new Person();
            person.setName("John");
            assertThatThrownBy(() -> BeanPath.get(person, "name", Integer.class))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("getOrDefault方法测试")
    class GetOrDefaultTests {

        @Test
        @DisplayName("有值返回值")
        void testGetOrDefaultWithValue() {
            Person person = new Person();
            person.setName("John");
            assertThat(BeanPath.getOrDefault(person, "name", "default")).isEqualTo("John");
        }

        @Test
        @DisplayName("无值返回默认值")
        void testGetOrDefaultNull() {
            Person person = new Person();
            assertThat(BeanPath.getOrDefault(person, "name", "default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("set方法测试")
    class SetTests {

        @Test
        @DisplayName("设置简单属性")
        void testSetSimple() {
            Person person = new Person();
            BeanPath.set(person, "name", "NewName");
            assertThat(person.getName()).isEqualTo("NewName");
        }

        @Test
        @DisplayName("设置嵌套属性")
        void testSetNested() {
            Person person = new Person();
            person.setAddress(new Address());
            BeanPath.set(person, "address.city", "Shanghai");
            assertThat(person.getAddress().getCity()).isEqualTo("Shanghai");
        }

        @Test
        @DisplayName("中间属性为null抛出异常")
        void testSetIntermediateNull() {
            Person person = new Person();
            assertThatThrownBy(() -> BeanPath.set(person, "address.city", "Shanghai"))
                    .isInstanceOf(OpenReflectException.class);
        }

        @Test
        @DisplayName("设置索引属性")
        void testSetIndexed() {
            Person person = new Person();
            person.setTags(new ArrayList<>(List.of("old")));
            BeanPath.set(person, "tags[0]", "new");
            assertThat(person.getTags().get(0)).isEqualTo("new");
        }
    }

    @Nested
    @DisplayName("hasPath方法测试")
    class HasPathTests {

        @Test
        @DisplayName("路径存在返回true")
        void testHasPathTrue() {
            Person person = new Person();
            person.setName("John");
            assertThat(BeanPath.hasPath(person, "name")).isTrue();
        }

        @Test
        @DisplayName("路径不存在返回false")
        void testHasPathFalse() {
            Person person = new Person();
            assertThat(BeanPath.hasPath(person, "nonexistent")).isFalse();
        }

        @Test
        @DisplayName("null bean返回false")
        void testHasPathNullBean() {
            assertThat(BeanPath.hasPath(null, "name")).isFalse();
        }
    }

    @Nested
    @DisplayName("getPathValues方法测试")
    class GetPathValuesTests {

        @Test
        @DisplayName("获取路径上所有值")
        void testGetPathValues() {
            Person person = new Person();
            Address address = new Address();
            address.setCity("Beijing");
            person.setAddress(address);

            List<Object> values = BeanPath.getPathValues(person, "address.city");
            assertThat(values).hasSize(3);
            assertThat(values.get(0)).isSameAs(person);
            assertThat(values.get(1)).isSameAs(address);
            assertThat(values.get(2)).isEqualTo("Beijing");
        }

        @Test
        @DisplayName("null bean返回空列表")
        void testGetPathValuesNullBean() {
            List<Object> values = BeanPath.getPathValues(null, "name");
            assertThat(values).isEmpty();
        }
    }

    @Nested
    @DisplayName("copy方法测试")
    class CopyTests {

        @Test
        @DisplayName("复制属性")
        void testCopy() {
            Person source = new Person();
            source.setName("John");
            Person target = new Person();

            BeanPath.copy(source, "name", target, "name");
            assertThat(target.getName()).isEqualTo("John");
        }
    }

    // Test helper classes
    public static class Person {
        private String name;
        private int age;
        private Address address;
        private List<String> tags;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }

    public static class Address {
        private String city;
        private String street;

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
    }
}
