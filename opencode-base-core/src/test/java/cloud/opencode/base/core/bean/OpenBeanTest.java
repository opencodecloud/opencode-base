package cloud.opencode.base.core.bean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenBean 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenBean 测试")
class OpenBeanTest {

    // 测试用 Bean
    static class User {
        private String name;
        private int age;
        private String email;
        private boolean active;

        public User() {}
        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    static class Employee {
        private String name;
        private int age;
        private String department;
        private double salary;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public double getSalary() { return salary; }
        public void setSalary(double salary) { this.salary = salary; }
    }

    static class UserDTO {
        private String userName;
        private int userAge;

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public int getUserAge() { return userAge; }
        public void setUserAge(int userAge) { this.userAge = userAge; }
    }

    // 测试用 Record
    record PersonRecord(String name, int age) {}

    @Nested
    @DisplayName("Bean 复制测试")
    class CopyPropertiesTests {

        @Test
        @DisplayName("copyProperties 基本复制")
        void testCopyPropertiesBasic() {
            User source = new User("Leon", 30);
            source.setEmail("leon@test.com");

            User target = new User();
            OpenBean.copyProperties(source, target);

            assertThat(target.getName()).isEqualTo("Leon");
            assertThat(target.getAge()).isEqualTo(30);
            assertThat(target.getEmail()).isEqualTo("leon@test.com");
        }

        @Test
        @DisplayName("copyProperties 忽略属性")
        void testCopyPropertiesWithIgnore() {
            User source = new User("Leon", 30);
            source.setEmail("leon@test.com");

            User target = new User();
            OpenBean.copyProperties(source, target, "email");

            assertThat(target.getName()).isEqualTo("Leon");
            assertThat(target.getAge()).isEqualTo(30);
            assertThat(target.getEmail()).isNull();
        }

        @Test
        @DisplayName("copyProperties 属性映射")
        void testCopyPropertiesWithMapping() {
            User source = new User("Leon", 30);

            UserDTO target = new UserDTO();
            Map<String, String> mapping = Map.of(
                    "name", "userName",
                    "age", "userAge"
            );
            OpenBean.copyProperties(source, target, mapping);

            assertThat(target.getUserName()).isEqualTo("Leon");
            assertThat(target.getUserAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("copyProperties 带转换器")
        void testCopyPropertiesWithConverter() {
            User source = new User("Leon", 30);

            User target = new User();
            PropertyConverter converter = (value, srcType, tgtType, name) -> {
                if ("name".equals(name) && value instanceof String s) {
                    return s.toUpperCase();
                }
                return value;
            };
            OpenBean.copyProperties(source, target, converter);

            assertThat(target.getName()).isEqualTo("LEON");
            assertThat(target.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("copyProperties null source")
        void testCopyPropertiesNullSource() {
            User target = new User();
            OpenBean.copyProperties(null, target);
            // 不应抛异常
            assertThat(target.getName()).isNull();
        }

        @Test
        @DisplayName("copyProperties null target")
        void testCopyPropertiesNullTarget() {
            User source = new User("Leon", 30);
            OpenBean.copyProperties(source, null);
            // 不应抛异常
        }

        @Test
        @DisplayName("copyToNew")
        void testCopyToNew() {
            User source = new User("Leon", 30);
            source.setEmail("leon@test.com");

            User copy = OpenBean.copyToNew(source, User.class);

            assertThat(copy).isNotSameAs(source);
            assertThat(copy.getName()).isEqualTo("Leon");
            assertThat(copy.getAge()).isEqualTo(30);
            assertThat(copy.getEmail()).isEqualTo("leon@test.com");
        }

        @Test
        @DisplayName("copyToNew 带忽略属性")
        void testCopyToNewWithIgnore() {
            User source = new User("Leon", 30);
            source.setEmail("leon@test.com");

            User copy = OpenBean.copyToNew(source, User.class, "email");

            assertThat(copy.getName()).isEqualTo("Leon");
            assertThat(copy.getEmail()).isNull();
        }

        @Test
        @DisplayName("copyToNew null source")
        void testCopyToNewNullSource() {
            User copy = OpenBean.copyToNew(null, User.class);
            assertThat(copy).isNull();
        }

        @Test
        @DisplayName("不同类型间复制")
        void testCopyBetweenDifferentTypes() {
            User source = new User("Leon", 30);

            Employee target = new Employee();
            OpenBean.copyProperties(source, target);

            assertThat(target.getName()).isEqualTo("Leon");
            assertThat(target.getAge()).isEqualTo(30);
            assertThat(target.getDepartment()).isNull();
        }
    }

    @Nested
    @DisplayName("Bean 转 Map 测试")
    class ToMapTests {

        @Test
        @DisplayName("toMap 基本转换")
        void testToMapBasic() {
            User user = new User("Leon", 30);
            user.setEmail("leon@test.com");
            user.setActive(true);

            Map<String, Object> map = OpenBean.toMap(user);

            assertThat(map.get("name")).isEqualTo("Leon");
            assertThat(map.get("age")).isEqualTo(30);
            assertThat(map.get("email")).isEqualTo("leon@test.com");
            assertThat(map.get("active")).isEqualTo(true);
        }

        @Test
        @DisplayName("toMap 带忽略属性")
        void testToMapWithIgnore() {
            User user = new User("Leon", 30);
            user.setEmail("leon@test.com");

            Map<String, Object> map = OpenBean.toMap(user, "email");

            assertThat(map.get("name")).isEqualTo("Leon");
            assertThat(map).doesNotContainKey("email");
        }

        @Test
        @DisplayName("toMap null bean")
        void testToMapNullBean() {
            Map<String, Object> map = OpenBean.toMap(null);
            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("toMapNonNull 仅包含非空属性")
        void testToMapNonNull() {
            User user = new User();
            user.setName("Leon");
            // email 为 null

            Map<String, Object> map = OpenBean.toMapNonNull(user);

            assertThat(map).containsKey("name");
            assertThat(map).doesNotContainKey("email");
        }

        @Test
        @DisplayName("toUnderlineKeyMap 驼峰转下划线")
        void testToUnderlineKeyMap() {
            UserDTO dto = new UserDTO();
            dto.setUserName("Leon");
            dto.setUserAge(30);

            Map<String, Object> map = OpenBean.toUnderlineKeyMap(dto);

            assertThat(map).containsKey("user_name");
            assertThat(map).containsKey("user_age");
            assertThat(map.get("user_name")).isEqualTo("Leon");
        }
    }

    @Nested
    @DisplayName("Map 转 Bean 测试")
    class ToBeanTests {

        @Test
        @DisplayName("toBean 基本转换")
        void testToBeanBasic() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "Leon");
            map.put("age", 30);
            map.put("email", "leon@test.com");

            User user = OpenBean.toBean(map, User.class);

            assertThat(user.getName()).isEqualTo("Leon");
            assertThat(user.getAge()).isEqualTo(30);
            assertThat(user.getEmail()).isEqualTo("leon@test.com");
        }

        @Test
        @DisplayName("toBean 类型转换")
        void testToBeanTypeConversion() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "Leon");
            map.put("age", "30"); // 字符串转 int

            User user = OpenBean.toBean(map, User.class);

            assertThat(user.getName()).isEqualTo("Leon");
            assertThat(user.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("toBean 带属性映射")
        void testToBeanWithMapping() {
            Map<String, Object> map = new HashMap<>();
            map.put("userName", "Leon");
            map.put("userAge", 30);

            Map<String, String> mapping = Map.of(
                    "userName", "name",
                    "userAge", "age"
            );

            User user = OpenBean.toBean(map, User.class, mapping);

            assertThat(user.getName()).isEqualTo("Leon");
            assertThat(user.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("toBean null map")
        void testToBeanNullMap() {
            User user = OpenBean.toBean(null, User.class);
            assertThat(user).isNull();
        }

        @Test
        @DisplayName("toBeanFromUnderlineKey 下划线转驼峰")
        void testToBeanFromUnderlineKey() {
            Map<String, Object> map = new HashMap<>();
            map.put("user_name", "Leon");
            map.put("user_age", 30);

            UserDTO dto = OpenBean.toBeanFromUnderlineKey(map, UserDTO.class);

            assertThat(dto.getUserName()).isEqualTo("Leon");
            assertThat(dto.getUserAge()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("属性访问测试")
    class PropertyAccessTests {

        @Test
        @DisplayName("getProperty")
        void testGetProperty() {
            User user = new User("Leon", 30);

            assertThat(OpenBean.getProperty(user, "name")).isEqualTo("Leon");
            assertThat(OpenBean.getProperty(user, "age")).isEqualTo(30);
        }

        @Test
        @DisplayName("getProperty 带类型")
        void testGetPropertyWithType() {
            User user = new User("Leon", 30);

            String name = OpenBean.getProperty(user, "name", String.class);
            Integer age = OpenBean.getProperty(user, "age", Integer.class);

            assertThat(name).isEqualTo("Leon");
            assertThat(age).isEqualTo(30);
        }

        @Test
        @DisplayName("getProperty null bean")
        void testGetPropertyNullBean() {
            assertThat(OpenBean.getProperty(null, "name")).isNull();
        }

        @Test
        @DisplayName("getProperty 不存在的属性")
        void testGetPropertyNotFound() {
            User user = new User("Leon", 30);
            assertThat(OpenBean.getProperty(user, "nonExistent")).isNull();
        }

        @Test
        @DisplayName("getPropertyOptional")
        void testGetPropertyOptional() {
            User user = new User("Leon", 30);

            Optional<String> name = OpenBean.getPropertyOptional(user, "name", String.class);
            assertThat(name).isPresent().contains("Leon");

            Optional<String> email = OpenBean.getPropertyOptional(user, "email", String.class);
            assertThat(email).isEmpty();
        }

        @Test
        @DisplayName("setProperty")
        void testSetProperty() {
            User user = new User();
            OpenBean.setProperty(user, "name", "Leon");
            OpenBean.setProperty(user, "age", 30);

            assertThat(user.getName()).isEqualTo("Leon");
            assertThat(user.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("setPropertyWithConvert")
        void testSetPropertyWithConvert() {
            User user = new User();
            OpenBean.setPropertyWithConvert(user, "age", "30"); // 字符串转 int

            assertThat(user.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("setProperties 批量设置")
        void testSetProperties() {
            User user = new User();
            Map<String, Object> properties = Map.of(
                    "name", "Leon",
                    "age", 30,
                    "email", "leon@test.com"
            );

            OpenBean.setProperties(user, properties);

            assertThat(user.getName()).isEqualTo("Leon");
            assertThat(user.getAge()).isEqualTo(30);
            assertThat(user.getEmail()).isEqualTo("leon@test.com");
        }
    }

    @Nested
    @DisplayName("属性描述测试")
    class PropertyDescriptorTests {

        @Test
        @DisplayName("getPropertyDescriptors")
        void testGetPropertyDescriptors() {
            List<PropertyDescriptor> descriptors = OpenBean.getPropertyDescriptors(User.class);

            assertThat(descriptors).isNotEmpty();
            List<String> names = descriptors.stream().map(PropertyDescriptor::name).toList();
            assertThat(names).contains("name", "age", "email", "active");
        }

        @Test
        @DisplayName("getPropertyDescriptor")
        void testGetPropertyDescriptor() {
            Optional<PropertyDescriptor> pd = OpenBean.getPropertyDescriptor(User.class, "name");

            assertThat(pd).isPresent();
            assertThat(pd.get().name()).isEqualTo("name");
            assertThat(pd.get().type()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("getPropertyNames")
        void testGetPropertyNames() {
            List<String> names = OpenBean.getPropertyNames(User.class);
            assertThat(names).contains("name", "age", "email", "active");
        }

        @Test
        @DisplayName("getReadablePropertyNames")
        void testGetReadablePropertyNames() {
            List<String> names = OpenBean.getReadablePropertyNames(User.class);
            assertThat(names).contains("name", "age", "email", "active");
        }

        @Test
        @DisplayName("getWritablePropertyNames")
        void testGetWritablePropertyNames() {
            List<String> names = OpenBean.getWritablePropertyNames(User.class);
            assertThat(names).contains("name", "age", "email", "active");
        }

        @Test
        @DisplayName("hasProperty")
        void testHasProperty() {
            assertThat(OpenBean.hasProperty(User.class, "name")).isTrue();
            assertThat(OpenBean.hasProperty(User.class, "nonExistent")).isFalse();
        }

        @Test
        @DisplayName("getPropertyType")
        void testGetPropertyType() {
            assertThat(OpenBean.getPropertyType(User.class, "name")).isEqualTo(String.class);
            assertThat(OpenBean.getPropertyType(User.class, "age")).isEqualTo(int.class);
            assertThat(OpenBean.getPropertyType(User.class, "active")).isEqualTo(boolean.class);
            assertThat(OpenBean.getPropertyType(User.class, "nonExistent")).isNull();
        }
    }

    @Nested
    @DisplayName("Bean 比较测试")
    class ComparisonTests {

        @Test
        @DisplayName("equals 相同")
        void testEqualsTrue() {
            User user1 = new User("Leon", 30);
            user1.setEmail("leon@test.com");

            User user2 = new User("Leon", 30);
            user2.setEmail("leon@test.com");

            assertThat(OpenBean.equals(user1, user2)).isTrue();
        }

        @Test
        @DisplayName("equals 不同")
        void testEqualsFalse() {
            User user1 = new User("Leon", 30);
            User user2 = new User("Leon", 31);

            assertThat(OpenBean.equals(user1, user2)).isFalse();
        }

        @Test
        @DisplayName("equals 同一实例")
        void testEqualsSameInstance() {
            User user = new User("Leon", 30);
            assertThat(OpenBean.equals(user, user)).isTrue();
        }

        @Test
        @DisplayName("equals null")
        void testEqualsNull() {
            User user = new User("Leon", 30);
            assertThat(OpenBean.equals(user, null)).isFalse();
            assertThat(OpenBean.equals(null, user)).isFalse();
        }

        @Test
        @DisplayName("equals 指定属性")
        void testEqualsSpecificProperties() {
            User user1 = new User("Leon", 30);
            user1.setEmail("email1@test.com");

            User user2 = new User("Leon", 30);
            user2.setEmail("email2@test.com");

            assertThat(OpenBean.equals(user1, user2, "name", "age")).isTrue();
            assertThat(OpenBean.equals(user1, user2, "email")).isFalse();
        }

        @Test
        @DisplayName("diff 差异属性")
        void testDiff() {
            User user1 = new User("Leon", 30);
            User user2 = new User("Leon", 31);

            Map<String, Object[]> diff = OpenBean.diff(user1, user2);

            assertThat(diff).containsKey("age");
            assertThat(diff.get("age")[0]).isEqualTo(30);
            assertThat(diff.get("age")[1]).isEqualTo(31);
        }

        @Test
        @DisplayName("diff 指定属性")
        void testDiffSpecificProperties() {
            User user1 = new User("Leon", 30);
            user1.setEmail("email1@test.com");

            User user2 = new User("Leon", 31);
            user2.setEmail("email2@test.com");

            Map<String, Object[]> diff = OpenBean.diff(user1, user2, "age");

            assertThat(diff).containsKey("age");
            assertThat(diff).doesNotContainKey("email");
        }
    }

    @Nested
    @DisplayName("Bean 校验测试")
    class ValidationTests {

        @Test
        @DisplayName("isEmpty 空 Bean")
        void testIsEmptyTrue() {
            User user = new User();
            assertThat(OpenBean.isEmpty(user)).isTrue();
        }

        @Test
        @DisplayName("isEmpty 非空 Bean")
        void testIsEmptyFalse() {
            User user = new User("Leon", 30);
            assertThat(OpenBean.isEmpty(user)).isFalse();
        }

        @Test
        @DisplayName("isEmpty null")
        void testIsEmptyNull() {
            assertThat(OpenBean.isEmpty(null)).isTrue();
        }

        @Test
        @DisplayName("hasNonNullProperty")
        void testHasNonNullProperty() {
            User empty = new User();
            assertThat(OpenBean.hasNonNullProperty(empty)).isFalse();

            User user = new User("Leon", 0);
            assertThat(OpenBean.hasNonNullProperty(user)).isTrue();
        }

        @Test
        @DisplayName("getNonNullPropertyNames")
        void testGetNonNullPropertyNames() {
            User user = new User();
            user.setName("Leon");
            // email 为 null

            List<String> names = OpenBean.getNonNullPropertyNames(user);
            assertThat(names).contains("name");
            assertThat(names).doesNotContain("email");
        }

        @Test
        @DisplayName("getNonNullPropertyNames null bean")
        void testGetNonNullPropertyNamesNullBean() {
            List<String> names = OpenBean.getNonNullPropertyNames(null);
            assertThat(names).isEmpty();
        }
    }

    @Nested
    @DisplayName("Record 支持测试")
    class RecordSupportTests {

        @Test
        @DisplayName("fromRecord")
        void testFromRecord() {
            PersonRecord record = new PersonRecord("Leon", 30);

            User user = OpenBean.fromRecord(record, User.class);

            assertThat(user.getName()).isEqualTo("Leon");
            assertThat(user.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("fromRecord null")
        void testFromRecordNull() {
            User user = OpenBean.fromRecord(null, User.class);
            assertThat(user).isNull();
        }

        @Test
        @DisplayName("toRecord")
        void testToRecord() {
            User user = new User("Leon", 30);

            PersonRecord record = OpenBean.toRecord(user, PersonRecord.class);

            assertThat(record.name()).isEqualTo("Leon");
            assertThat(record.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("toRecord null")
        void testToRecordNull() {
            PersonRecord record = OpenBean.toRecord(null, PersonRecord.class);
            assertThat(record).isNull();
        }
    }

    @Nested
    @DisplayName("缓存测试")
    class CacheTests {

        @Test
        @DisplayName("PropertyDescriptor 缓存")
        void testPropertyDescriptorCaching() {
            List<PropertyDescriptor> descriptors1 = OpenBean.getPropertyDescriptors(User.class);
            List<PropertyDescriptor> descriptors2 = OpenBean.getPropertyDescriptors(User.class);

            // 应该返回相同的缓存列表
            assertThat(descriptors1).isSameAs(descriptors2);
        }
    }
}
