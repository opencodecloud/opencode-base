package cloud.opencode.base.core.bean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * BeanPath 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("BeanPath 测试")
class BeanPathTest {

    // 测试用类
    static class Address {
        private String city;
        private String street;
        private int zipCode;

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public int getZipCode() { return zipCode; }
        public void setZipCode(int zipCode) { this.zipCode = zipCode; }
    }

    static class User {
        private String name;
        private int age;
        private Address address;
        private List<String> tags;
        private String[] hobbies;
        private Map<String, Object> extra;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
        public String[] getHobbies() { return hobbies; }
        public void setHobbies(String[] hobbies) { this.hobbies = hobbies; }
        public Map<String, Object> getExtra() { return extra; }
        public void setExtra(Map<String, Object> extra) { this.extra = extra; }
    }

    static class Company {
        private String name;
        private List<User> employees;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<User> getEmployees() { return employees; }
        public void setEmployees(List<User> employees) { this.employees = employees; }
    }

    private User createTestUser() {
        User user = new User();
        user.setName("Leon");
        user.setAge(30);

        Address address = new Address();
        address.setCity("Beijing");
        address.setStreet("Main St");
        address.setZipCode(100000);
        user.setAddress(address);

        user.setTags(new ArrayList<>(Arrays.asList("java", "python", "go")));
        user.setHobbies(new String[]{"reading", "coding", "gaming"});

        Map<String, Object> extra = new HashMap<>();
        extra.put("score", 95);
        extra.put("level", "senior");
        user.setExtra(extra);

        return user;
    }

    @Nested
    @DisplayName("get 测试")
    class GetTests {

        @Test
        @DisplayName("get 简单属性")
        void testGetSimpleProperty() {
            User user = createTestUser();
            assertThat(BeanPath.get(user, "name")).isEqualTo("Leon");
            assertThat(BeanPath.get(user, "age")).isEqualTo(30);
        }

        @Test
        @DisplayName("get 嵌套属性")
        void testGetNestedProperty() {
            User user = createTestUser();
            assertThat(BeanPath.get(user, "address.city")).isEqualTo("Beijing");
            assertThat(BeanPath.get(user, "address.street")).isEqualTo("Main St");
            assertThat(BeanPath.get(user, "address.zipCode")).isEqualTo(100000);
        }

        @Test
        @DisplayName("get List 索引")
        void testGetListIndex() {
            User user = createTestUser();
            assertThat(BeanPath.get(user, "tags[0]")).isEqualTo("java");
            assertThat(BeanPath.get(user, "tags[1]")).isEqualTo("python");
            assertThat(BeanPath.get(user, "tags[2]")).isEqualTo("go");
        }

        @Test
        @DisplayName("get 数组索引")
        void testGetArrayIndex() {
            User user = createTestUser();
            assertThat(BeanPath.get(user, "hobbies[0]")).isEqualTo("reading");
            assertThat(BeanPath.get(user, "hobbies[1]")).isEqualTo("coding");
        }

        @Test
        @DisplayName("get Map 键")
        void testGetMapKey() {
            User user = createTestUser();
            assertThat(BeanPath.get(user, "extra[score]")).isEqualTo(95);
            assertThat(BeanPath.get(user, "extra[level]")).isEqualTo("senior");
        }

        @Test
        @DisplayName("get 带类型转换")
        void testGetWithType() {
            User user = createTestUser();
            Integer age = BeanPath.get(user, "age", Integer.class);
            assertThat(age).isEqualTo(30);

            String cityStr = BeanPath.get(user, "address.city", String.class);
            assertThat(cityStr).isEqualTo("Beijing");
        }

        @Test
        @DisplayName("get null bean")
        void testGetNullBean() {
            assertThat(BeanPath.get(null, "name")).isNull();
        }

        @Test
        @DisplayName("get null 路径")
        void testGetNullPath() {
            User user = createTestUser();
            assertThat(BeanPath.get(user, null)).isNull();
            assertThat(BeanPath.get(user, "")).isNull();
        }

        @Test
        @DisplayName("get 中间路径为 null")
        void testGetIntermediateNull() {
            User user = new User();
            assertThat(BeanPath.get(user, "address.city")).isNull();
        }

        @Test
        @DisplayName("get 索引越界返回 null")
        void testGetIndexOutOfBounds() {
            User user = createTestUser();
            assertThat(BeanPath.get(user, "tags[10]")).isNull();
            assertThat(BeanPath.get(user, "hobbies[10]")).isNull();
        }
    }

    @Nested
    @DisplayName("getOptional 测试")
    class GetOptionalTests {

        @Test
        @DisplayName("getOptional 存在值")
        void testGetOptionalPresent() {
            User user = createTestUser();
            Optional<String> name = BeanPath.getOptional(user, "name", String.class);
            assertThat(name).isPresent().contains("Leon");
        }

        @Test
        @DisplayName("getOptional 不存在值")
        void testGetOptionalEmpty() {
            User user = new User();
            Optional<String> name = BeanPath.getOptional(user, "name", String.class);
            assertThat(name).isEmpty();
        }
    }

    @Nested
    @DisplayName("set 测试")
    class SetTests {

        @Test
        @DisplayName("set 简单属性")
        void testSetSimpleProperty() {
            User user = createTestUser();
            BeanPath.set(user, "name", "NewName");
            assertThat(user.getName()).isEqualTo("NewName");
        }

        @Test
        @DisplayName("set 嵌套属性")
        void testSetNestedProperty() {
            User user = createTestUser();
            BeanPath.set(user, "address.city", "Shanghai");
            assertThat(user.getAddress().getCity()).isEqualTo("Shanghai");
        }

        @Test
        @DisplayName("set List 索引")
        void testSetListIndex() {
            User user = createTestUser();
            BeanPath.set(user, "tags[0]", "rust");
            assertThat(user.getTags().get(0)).isEqualTo("rust");
        }

        @Test
        @DisplayName("set 数组索引")
        void testSetArrayIndex() {
            User user = createTestUser();
            BeanPath.set(user, "hobbies[0]", "swimming");
            assertThat(user.getHobbies()[0]).isEqualTo("swimming");
        }

        @Test
        @DisplayName("set Map 键")
        void testSetMapKey() {
            User user = createTestUser();
            BeanPath.set(user, "extra[score]", 100);
            assertThat(user.getExtra().get("score")).isEqualTo(100);
        }

        @Test
        @DisplayName("set null bean 忽略")
        void testSetNullBean() {
            // 不应抛异常
            BeanPath.set(null, "name", "test");
        }

        @Test
        @DisplayName("set 中间路径为 null 抛异常")
        void testSetIntermediateNull() {
            User user = new User();
            assertThatThrownBy(() -> BeanPath.set(user, "address.city", "Beijing"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("setWithCreate 测试")
    class SetWithCreateTests {

        @Test
        @DisplayName("setWithCreate 自动创建嵌套对象")
        void testSetWithCreateNestedObject() {
            User user = new User();
            // 需要 Address 有默认构造器
            BeanPath.setWithCreate(user, "address.city", "Beijing");
            assertThat(user.getAddress()).isNotNull();
            assertThat(user.getAddress().getCity()).isEqualTo("Beijing");
        }

        @Test
        @DisplayName("setWithCreate 自动创建 List")
        void testSetWithCreateList() {
            // 这是一个边缘情况，依赖于实现
            User user = createTestUser();
            BeanPath.setWithCreate(user, "tags[0]", "new");
            assertThat(user.getTags().get(0)).isEqualTo("new");
        }
    }

    @Nested
    @DisplayName("exists 测试")
    class ExistsTests {

        @Test
        @DisplayName("exists 存在")
        void testExistsTrue() {
            User user = createTestUser();
            assertThat(BeanPath.exists(user, "name")).isTrue();
            assertThat(BeanPath.exists(user, "address.city")).isTrue();
            assertThat(BeanPath.exists(user, "tags[0]")).isTrue();
        }

        @Test
        @DisplayName("exists 不存在")
        void testExistsFalse() {
            User user = new User();
            assertThat(BeanPath.exists(user, "name")).isFalse();
            assertThat(BeanPath.exists(user, "address.city")).isFalse();
        }
    }

    @Nested
    @DisplayName("isNull / isNotNull 测试")
    class NullCheckTests {

        @Test
        @DisplayName("isNull")
        void testIsNull() {
            User user = new User();
            assertThat(BeanPath.isNull(user, "name")).isTrue();

            user.setName("Leon");
            assertThat(BeanPath.isNull(user, "name")).isFalse();
        }

        @Test
        @DisplayName("isNotNull")
        void testIsNotNull() {
            User user = new User();
            assertThat(BeanPath.isNotNull(user, "name")).isFalse();

            user.setName("Leon");
            assertThat(BeanPath.isNotNull(user, "name")).isTrue();
        }
    }

    @Nested
    @DisplayName("parsePath 测试")
    class ParsePathTests {

        @Test
        @DisplayName("parsePath 简单属性")
        void testParsePathSimple() {
            List<BeanPath.PathSegment> segments = BeanPath.parsePath("name");
            assertThat(segments).hasSize(1);
            assertThat(segments.get(0)).isInstanceOf(BeanPath.PropertySegment.class);
            assertThat(segments.get(0).value()).isEqualTo("name");
        }

        @Test
        @DisplayName("parsePath 嵌套属性")
        void testParsePathNested() {
            List<BeanPath.PathSegment> segments = BeanPath.parsePath("address.city");
            assertThat(segments).hasSize(2);
            assertThat(segments.get(0).value()).isEqualTo("address");
            assertThat(segments.get(1).value()).isEqualTo("city");
        }

        @Test
        @DisplayName("parsePath 带索引")
        void testParsePathWithIndex() {
            List<BeanPath.PathSegment> segments = BeanPath.parsePath("items[0]");
            assertThat(segments).hasSize(2);
            assertThat(segments.get(0)).isInstanceOf(BeanPath.PropertySegment.class);
            assertThat(segments.get(1)).isInstanceOf(BeanPath.IndexSegment.class);
            assertThat(((BeanPath.IndexSegment) segments.get(1)).index()).isEqualTo(0);
        }

        @Test
        @DisplayName("parsePath 带 Map 键")
        void testParsePathWithMapKey() {
            List<BeanPath.PathSegment> segments = BeanPath.parsePath("extra[key]");
            assertThat(segments).hasSize(2);
            assertThat(segments.get(0)).isInstanceOf(BeanPath.PropertySegment.class);
            assertThat(segments.get(1)).isInstanceOf(BeanPath.MapKeySegment.class);
            assertThat(((BeanPath.MapKeySegment) segments.get(1)).key()).isEqualTo("key");
        }

        @Test
        @DisplayName("parsePath 复杂路径")
        void testParsePathComplex() {
            List<BeanPath.PathSegment> segments = BeanPath.parsePath("company.employees[0].address.city");
            assertThat(segments).hasSize(5);
        }
    }

    @Nested
    @DisplayName("路径工具方法测试")
    class PathUtilityTests {

        @Test
        @DisplayName("getParentPath")
        void testGetParentPath() {
            assertThat(BeanPath.getParentPath("address.city")).isEqualTo("address");
            assertThat(BeanPath.getParentPath("items[0]")).isEqualTo("items");
            assertThat(BeanPath.getParentPath("a.b.c")).isEqualTo("a.b");
            assertThat(BeanPath.getParentPath("name")).isEmpty();
        }

        @Test
        @DisplayName("getLastSegment")
        void testGetLastSegment() {
            assertThat(BeanPath.getLastSegment("address.city")).isEqualTo("city");
            assertThat(BeanPath.getLastSegment("a.b.c")).isEqualTo("c");
            assertThat(BeanPath.getLastSegment("name")).isEqualTo("name");
        }

        @Test
        @DisplayName("joinPath")
        void testJoinPath() {
            assertThat(BeanPath.joinPath("address", "city")).isEqualTo("address.city");
            assertThat(BeanPath.joinPath("a", "b", "c")).isEqualTo("a.b.c");
        }
    }

    @Nested
    @DisplayName("PathSegment 类型测试")
    class PathSegmentTests {

        @Test
        @DisplayName("PropertySegment")
        void testPropertySegment() {
            BeanPath.PropertySegment segment = new BeanPath.PropertySegment("name");
            assertThat(segment.value()).isEqualTo("name");
        }

        @Test
        @DisplayName("IndexSegment")
        void testIndexSegment() {
            BeanPath.IndexSegment segment = new BeanPath.IndexSegment("[0]", 0);
            assertThat(segment.value()).isEqualTo("[0]");
            assertThat(segment.index()).isEqualTo(0);
        }

        @Test
        @DisplayName("MapKeySegment")
        void testMapKeySegment() {
            BeanPath.MapKeySegment segment = new BeanPath.MapKeySegment("[key]", "key");
            assertThat(segment.value()).isEqualTo("[key]");
            assertThat(segment.key()).isEqualTo("key");
        }
    }

    @Nested
    @DisplayName("复杂场景测试")
    class ComplexScenarioTests {

        @Test
        @DisplayName("公司员工嵌套访问")
        void testCompanyEmployeeAccess() {
            Company company = new Company();
            company.setName("OpenCode");

            User user = createTestUser();
            company.setEmployees(new ArrayList<>(List.of(user)));

            assertThat(BeanPath.get(company, "name")).isEqualTo("OpenCode");
            assertThat(BeanPath.get(company, "employees[0].name")).isEqualTo("Leon");
            assertThat(BeanPath.get(company, "employees[0].address.city")).isEqualTo("Beijing");
        }

        @Test
        @DisplayName("多维索引访问")
        void testMultiDimensionalAccess() {
            User user = createTestUser();
            // 访问 tags[1] 应该得到 "python"
            assertThat(BeanPath.get(user, "tags[1]")).isEqualTo("python");
        }
    }
}
