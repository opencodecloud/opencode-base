package cloud.opencode.base.core.reflect;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * RecordUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("RecordUtil 测试")
class RecordUtilTest {

    // 测试用 Record
    record Person(String name, int age) {}
    record Address(String city, String street, int zipCode) {}
    record GenericRecord(List<String> items, Map<String, Integer> map) {}
    record EmptyRecord() {}

    // 非 Record 类
    static class NotARecord {
        String name;
    }

    @Nested
    @DisplayName("isRecord 测试")
    class IsRecordTests {

        @Test
        @DisplayName("isRecord Class - 是 Record")
        void testIsRecordClassTrue() {
            assertThat(RecordUtil.isRecord(Person.class)).isTrue();
            assertThat(RecordUtil.isRecord(Address.class)).isTrue();
            assertThat(RecordUtil.isRecord(EmptyRecord.class)).isTrue();
        }

        @Test
        @DisplayName("isRecord Class - 不是 Record")
        void testIsRecordClassFalse() {
            assertThat(RecordUtil.isRecord(NotARecord.class)).isFalse();
            assertThat(RecordUtil.isRecord(String.class)).isFalse();
            assertThat(RecordUtil.isRecord((Class<?>) null)).isFalse();
        }

        @Test
        @DisplayName("isRecord Object - 是 Record")
        void testIsRecordObjectTrue() {
            Person person = new Person("John", 30);
            assertThat(RecordUtil.isRecord(person)).isTrue();
        }

        @Test
        @DisplayName("isRecord Object - 不是 Record")
        void testIsRecordObjectFalse() {
            assertThat(RecordUtil.isRecord(new NotARecord())).isFalse();
            assertThat(RecordUtil.isRecord("string")).isFalse();
            assertThat(RecordUtil.isRecord((Object) null)).isFalse();
        }
    }

    @Nested
    @DisplayName("getComponents 测试")
    class GetComponentsTests {

        @Test
        @DisplayName("getComponents 正常")
        void testGetComponents() {
            RecordComponent[] components = RecordUtil.getComponents(Person.class);
            assertThat(components).hasSize(2);
            assertThat(components[0].getName()).isEqualTo("name");
            assertThat(components[1].getName()).isEqualTo("age");
        }

        @Test
        @DisplayName("getComponents 空 Record")
        void testGetComponentsEmpty() {
            RecordComponent[] components = RecordUtil.getComponents(EmptyRecord.class);
            assertThat(components).isEmpty();
        }

        @Test
        @DisplayName("getComponents 非 Record 抛异常")
        void testGetComponentsNotRecord() {
            assertThatThrownBy(() -> RecordUtil.getComponents(NotARecord.class))
                    .isInstanceOf(OpenException.class)
                    .hasMessageContaining("not a record");
        }
    }

    @Nested
    @DisplayName("getComponentNames 测试")
    class GetComponentNamesTests {

        @Test
        @DisplayName("getComponentNames 正常")
        void testGetComponentNames() {
            List<String> names = RecordUtil.getComponentNames(Person.class);
            assertThat(names).containsExactly("name", "age");
        }

        @Test
        @DisplayName("getComponentNames Address")
        void testGetComponentNamesAddress() {
            List<String> names = RecordUtil.getComponentNames(Address.class);
            assertThat(names).containsExactly("city", "street", "zipCode");
        }
    }

    @Nested
    @DisplayName("getComponentTypes 测试")
    class GetComponentTypesTests {

        @Test
        @DisplayName("getComponentTypes 正常")
        void testGetComponentTypes() {
            Map<String, Class<?>> types = RecordUtil.getComponentTypes(Person.class);
            assertThat(types).hasSize(2);
            assertThat(types.get("name")).isEqualTo(String.class);
            assertThat(types.get("age")).isEqualTo(int.class);
        }

        @Test
        @DisplayName("getComponentTypes Address")
        void testGetComponentTypesAddress() {
            Map<String, Class<?>> types = RecordUtil.getComponentTypes(Address.class);
            assertThat(types.get("city")).isEqualTo(String.class);
            assertThat(types.get("street")).isEqualTo(String.class);
            assertThat(types.get("zipCode")).isEqualTo(int.class);
        }
    }

    @Nested
    @DisplayName("getComponentValue 测试")
    class GetComponentValueTests {

        @Test
        @DisplayName("getComponentValue 正常")
        void testGetComponentValue() {
            Person person = new Person("John", 30);

            String name = RecordUtil.getComponentValue(person, "name");
            int age = RecordUtil.getComponentValue(person, "age");

            assertThat(name).isEqualTo("John");
            assertThat(age).isEqualTo(30);
        }

        @Test
        @DisplayName("getComponentValue 非 Record 抛异常")
        void testGetComponentValueNotRecord() {
            assertThatThrownBy(() -> RecordUtil.getComponentValue(new NotARecord(), "name"))
                    .isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("getComponentValue 不存在的组件")
        void testGetComponentValueNotFound() {
            Person person = new Person("John", 30);
            assertThatThrownBy(() -> RecordUtil.getComponentValue(person, "nonExistent"))
                    .isInstanceOf(OpenException.class);
        }
    }

    @Nested
    @DisplayName("toMap 测试")
    class ToMapTests {

        @Test
        @DisplayName("toMap 正常")
        void testToMap() {
            Person person = new Person("John", 30);
            Map<String, Object> map = RecordUtil.toMap(person);

            assertThat(map).hasSize(2);
            assertThat(map.get("name")).isEqualTo("John");
            assertThat(map.get("age")).isEqualTo(30);
        }

        @Test
        @DisplayName("toMap Address")
        void testToMapAddress() {
            Address address = new Address("Beijing", "Main St", 100000);
            Map<String, Object> map = RecordUtil.toMap(address);

            assertThat(map.get("city")).isEqualTo("Beijing");
            assertThat(map.get("street")).isEqualTo("Main St");
            assertThat(map.get("zipCode")).isEqualTo(100000);
        }

        @Test
        @DisplayName("toMap 空 Record")
        void testToMapEmptyRecord() {
            EmptyRecord empty = new EmptyRecord();
            Map<String, Object> map = RecordUtil.toMap(empty);
            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("toMap 非 Record 抛异常")
        void testToMapNotRecord() {
            assertThatThrownBy(() -> RecordUtil.toMap(new NotARecord()))
                    .isInstanceOf(OpenException.class);
        }
    }

    @Nested
    @DisplayName("fromMap 测试")
    class FromMapTests {

        @Test
        @DisplayName("fromMap 正常")
        void testFromMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "John");
            map.put("age", 30);

            Person person = RecordUtil.fromMap(map, Person.class);
            assertThat(person.name()).isEqualTo("John");
            assertThat(person.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("fromMap Address")
        void testFromMapAddress() {
            Map<String, Object> map = new HashMap<>();
            map.put("city", "Beijing");
            map.put("street", "Main St");
            map.put("zipCode", 100000);

            Address address = RecordUtil.fromMap(map, Address.class);
            assertThat(address.city()).isEqualTo("Beijing");
            assertThat(address.street()).isEqualTo("Main St");
            assertThat(address.zipCode()).isEqualTo(100000);
        }

        @Test
        @DisplayName("fromMap 空 Record")
        void testFromMapEmptyRecord() {
            Map<String, Object> map = new HashMap<>();
            EmptyRecord empty = RecordUtil.fromMap(map, EmptyRecord.class);
            assertThat(empty).isNotNull();
        }

        @Test
        @DisplayName("fromMap 缺少字段使用默认值")
        void testFromMapMissingFields() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "John");
            // age 缺失，应使用默认值 0

            Person person = RecordUtil.fromMap(map, Person.class);
            assertThat(person.name()).isEqualTo("John");
            assertThat(person.age()).isEqualTo(0);
        }

        @Test
        @DisplayName("fromMap 非 Record 抛异常")
        @SuppressWarnings("unchecked")
        void testFromMapNotRecord() {
            Map<String, Object> map = new HashMap<>();
            // 使用强制转换绕过编译时泛型约束，测试运行时行为
            Class<? extends Record> notARecordClass = (Class<? extends Record>) (Class<?>) NotARecord.class;
            assertThatThrownBy(() -> RecordUtil.fromMap(map, notARecordClass))
                    .isInstanceOf(OpenException.class);
        }
    }

    @Nested
    @DisplayName("copyWith 测试")
    class CopyWithTests {

        @Test
        @DisplayName("copyWith 单个组件")
        void testCopyWithSingle() {
            Person person = new Person("John", 30);
            Person updated = RecordUtil.copyWith(person, "name", "Jane");

            assertThat(updated.name()).isEqualTo("Jane");
            assertThat(updated.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("copyWith 修改 int")
        void testCopyWithInt() {
            Person person = new Person("John", 30);
            Person updated = RecordUtil.copyWith(person, "age", 35);

            assertThat(updated.name()).isEqualTo("John");
            assertThat(updated.age()).isEqualTo(35);
        }

        @Test
        @DisplayName("copyWith Map 多个组件")
        void testCopyWithMap() {
            Person person = new Person("John", 30);
            Map<String, Object> changes = new HashMap<>();
            changes.put("name", "Jane");
            changes.put("age", 25);

            Person updated = RecordUtil.copyWith(person, changes);
            assertThat(updated.name()).isEqualTo("Jane");
            assertThat(updated.age()).isEqualTo(25);
        }

        @Test
        @DisplayName("copyWith 非 Record 抛异常")
        void testCopyWithNotRecord() {
            assertThatThrownBy(() -> RecordUtil.copyWith((Record) null, "name", "value"))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("equals 测试")
    class EqualsTests {

        @Test
        @DisplayName("equals 相同实例")
        void testEqualsSameInstance() {
            Person person = new Person("John", 30);
            assertThat(RecordUtil.equals(person, person)).isTrue();
        }

        @Test
        @DisplayName("equals 相等值")
        void testEqualsEqualValues() {
            Person person1 = new Person("John", 30);
            Person person2 = new Person("John", 30);
            assertThat(RecordUtil.equals(person1, person2)).isTrue();
        }

        @Test
        @DisplayName("equals 不同值")
        void testEqualsDifferentValues() {
            Person person1 = new Person("John", 30);
            Person person2 = new Person("Jane", 25);
            assertThat(RecordUtil.equals(person1, person2)).isFalse();
        }

        @Test
        @DisplayName("equals null")
        void testEqualsNull() {
            Person person = new Person("John", 30);
            assertThat(RecordUtil.equals(person, null)).isFalse();
            assertThat(RecordUtil.equals(null, person)).isFalse();
            assertThat(RecordUtil.equals(null, null)).isFalse();
        }

        @Test
        @DisplayName("equals 不同类型")
        void testEqualsDifferentTypes() {
            Person person = new Person("John", 30);
            Address address = new Address("Beijing", "Main", 100000);
            assertThat(RecordUtil.equals(person, address)).isFalse();
        }

        @Test
        @DisplayName("equals 非 Record")
        void testEqualsNotRecord() {
            NotARecord obj1 = new NotARecord();
            NotARecord obj2 = new NotARecord();
            assertThat(RecordUtil.equals(obj1, obj2)).isFalse();
        }
    }

    @Nested
    @DisplayName("getComponentCount 测试")
    class GetComponentCountTests {

        @Test
        @DisplayName("getComponentCount Person")
        void testGetComponentCountPerson() {
            assertThat(RecordUtil.getComponentCount(Person.class)).isEqualTo(2);
        }

        @Test
        @DisplayName("getComponentCount Address")
        void testGetComponentCountAddress() {
            assertThat(RecordUtil.getComponentCount(Address.class)).isEqualTo(3);
        }

        @Test
        @DisplayName("getComponentCount EmptyRecord")
        void testGetComponentCountEmpty() {
            assertThat(RecordUtil.getComponentCount(EmptyRecord.class)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("hasComponent 测试")
    class HasComponentTests {

        @Test
        @DisplayName("hasComponent 存在")
        void testHasComponentTrue() {
            assertThat(RecordUtil.hasComponent(Person.class, "name")).isTrue();
            assertThat(RecordUtil.hasComponent(Person.class, "age")).isTrue();
        }

        @Test
        @DisplayName("hasComponent 不存在")
        void testHasComponentFalse() {
            assertThat(RecordUtil.hasComponent(Person.class, "email")).isFalse();
            assertThat(RecordUtil.hasComponent(Person.class, "nonExistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("getComponentGenericType 测试")
    class GetComponentGenericTypeTests {

        @Test
        @DisplayName("getComponentGenericType 普通类型")
        void testGetComponentGenericTypeSimple() {
            Type type = RecordUtil.getComponentGenericType(Person.class, "name");
            assertThat(type).isEqualTo(String.class);
        }

        @Test
        @DisplayName("getComponentGenericType 泛型类型")
        void testGetComponentGenericTypeGeneric() {
            Type type = RecordUtil.getComponentGenericType(GenericRecord.class, "items");
            assertThat(type.getTypeName()).contains("List");
            assertThat(type.getTypeName()).contains("String");
        }

        @Test
        @DisplayName("getComponentGenericType Map 类型")
        void testGetComponentGenericTypeMap() {
            Type type = RecordUtil.getComponentGenericType(GenericRecord.class, "map");
            assertThat(type.getTypeName()).contains("Map");
            assertThat(type.getTypeName()).contains("String");
            assertThat(type.getTypeName()).contains("Integer");
        }

        @Test
        @DisplayName("getComponentGenericType 不存在的组件")
        void testGetComponentGenericTypeNotFound() {
            Type type = RecordUtil.getComponentGenericType(Person.class, "nonExistent");
            assertThat(type).isNull();
        }
    }

    @Nested
    @DisplayName("类型转换测试")
    class TypeConversionTests {

        @Test
        @DisplayName("fromMap 数字类型转换")
        void testFromMapNumberConversion() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "John");
            map.put("age", 30L); // Long -> int

            Person person = RecordUtil.fromMap(map, Person.class);
            assertThat(person.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("fromMap 已是目标类型")
        void testFromMapSameType() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "John");
            map.put("age", 30); // 已经是 int

            Person person = RecordUtil.fromMap(map, Person.class);
            assertThat(person.age()).isEqualTo(30);
        }
    }
}
