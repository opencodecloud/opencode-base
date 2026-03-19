package cloud.opencode.base.functional.record;

import cloud.opencode.base.functional.monad.Try;
import cloud.opencode.base.functional.optics.Lens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * RecordUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("RecordUtil 测试")
class RecordUtilTest {

    // Test records
    record Person(String name, int age) {}
    record Address(String city, String street) {}
    record PersonWithAddress(String name, int age, Address address) {}

    @Nested
    @DisplayName("isRecord() 测试")
    class IsRecordTests {

        @Test
        @DisplayName("isRecord() record 类返回 true")
        void testIsRecordTrue() {
            assertThat(RecordUtil.isRecord(Person.class)).isTrue();
        }

        @Test
        @DisplayName("isRecord() 普通类返回 false")
        void testIsRecordFalse() {
            assertThat(RecordUtil.isRecord(String.class)).isFalse();
        }

        @Test
        @DisplayName("isRecord() null 返回 false")
        void testIsRecordNull() {
            assertThat(RecordUtil.isRecord(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isRecordInstance() 测试")
    class IsRecordInstanceTests {

        @Test
        @DisplayName("isRecordInstance() record 实例返回 true")
        void testIsRecordInstanceTrue() {
            Person person = new Person("Alice", 30);

            assertThat(RecordUtil.isRecordInstance(person)).isTrue();
        }

        @Test
        @DisplayName("isRecordInstance() 普通对象返回 false")
        void testIsRecordInstanceFalse() {
            assertThat(RecordUtil.isRecordInstance("hello")).isFalse();
        }

        @Test
        @DisplayName("isRecordInstance() null 返回 false")
        void testIsRecordInstanceNull() {
            assertThat(RecordUtil.isRecordInstance(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("componentNames() 测试")
    class ComponentNamesTests {

        @Test
        @DisplayName("componentNames() 返回组件名称列表")
        void testComponentNames() {
            List<String> names = RecordUtil.componentNames(Person.class);

            assertThat(names).containsExactly("name", "age");
        }

        @Test
        @DisplayName("componentNames() 保持顺序")
        void testComponentNamesOrder() {
            List<String> names = RecordUtil.componentNames(PersonWithAddress.class);

            assertThat(names).containsExactly("name", "age", "address");
        }
    }

    @Nested
    @DisplayName("componentTypes() 测试")
    class ComponentTypesTests {

        @Test
        @DisplayName("componentTypes() 返回组件类型列表")
        void testComponentTypes() {
            List<Class<?>> types = RecordUtil.componentTypes(Person.class);

            assertThat(types).containsExactly(String.class, int.class);
        }
    }

    @Nested
    @DisplayName("componentCount() 测试")
    class ComponentCountTests {

        @Test
        @DisplayName("componentCount() 返回组件数量")
        void testComponentCount() {
            assertThat(RecordUtil.componentCount(Person.class)).isEqualTo(2);
            assertThat(RecordUtil.componentCount(PersonWithAddress.class)).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("componentValues() 测试")
    class ComponentValuesTests {

        @Test
        @DisplayName("componentValues() 返回所有组件值")
        void testComponentValues() {
            Person person = new Person("Alice", 30);

            List<Object> values = RecordUtil.componentValues(person);

            assertThat(values).containsExactly("Alice", 30);
        }
    }

    @Nested
    @DisplayName("getComponent() 测试")
    class GetComponentTests {

        @Test
        @DisplayName("getComponent() 返回指定组件值")
        void testGetComponent() {
            Person person = new Person("Alice", 30);

            String name = RecordUtil.getComponent(person, "name");
            int age = RecordUtil.getComponent(person, "age");

            assertThat(name).isEqualTo("Alice");
            assertThat(age).isEqualTo(30);
        }

        @Test
        @DisplayName("getComponent() 组件不存在时抛出异常")
        void testGetComponentNotFound() {
            Person person = new Person("Alice", 30);

            assertThatThrownBy(() -> RecordUtil.getComponent(person, "notExist"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Component not found");
        }
    }

    @Nested
    @DisplayName("getComponentTry() 测试")
    class GetComponentTryTests {

        @Test
        @DisplayName("getComponentTry() 成功返回 Success")
        void testGetComponentTrySuccess() {
            Person person = new Person("Alice", 30);

            Try<String> result = RecordUtil.getComponentTry(person, "name");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("getComponentTry() 失败返回 Failure")
        void testGetComponentTryFailure() {
            Person person = new Person("Alice", 30);

            Try<String> result = RecordUtil.getComponentTry(person, "notExist");

            assertThat(result.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("copy() 测试")
    class CopyTests {

        @Test
        @DisplayName("copy() 带修改复制 record")
        void testCopy() {
            Person person = new Person("Alice", 30);

            Person copied = RecordUtil.copy(person, Map.of("age", 31));

            assertThat(copied.name()).isEqualTo("Alice");
            assertThat(copied.age()).isEqualTo(31);
        }

        @Test
        @DisplayName("copy() 修改多个组件")
        void testCopyMultiple() {
            Person person = new Person("Alice", 30);

            Person copied = RecordUtil.copy(person, Map.of("name", "Bob", "age", 25));

            assertThat(copied.name()).isEqualTo("Bob");
            assertThat(copied.age()).isEqualTo(25);
        }

        @Test
        @DisplayName("copy() 空修改复制相同值")
        void testCopyNoChanges() {
            Person person = new Person("Alice", 30);

            Person copied = RecordUtil.copy(person, Map.of());

            assertThat(copied).isEqualTo(person);
        }
    }

    @Nested
    @DisplayName("copyWith() 测试")
    class CopyWithTests {

        @Test
        @DisplayName("copyWith() 修改单个组件")
        void testCopyWith() {
            Person person = new Person("Alice", 30);

            Person copied = RecordUtil.copyWith(person, "name", "Bob");

            assertThat(copied.name()).isEqualTo("Bob");
            assertThat(copied.age()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("copyTransforming() 测试")
    class CopyTransformingTests {

        @Test
        @DisplayName("copyTransforming() 转换组件值")
        void testCopyTransforming() {
            Person person = new Person("alice", 30);

            Person transformed = RecordUtil.copyTransforming(person, "name",
                    (String s) -> s.toUpperCase());

            assertThat(transformed.name()).isEqualTo("ALICE");
            assertThat(transformed.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("copyTransforming() 转换整数组件")
        void testCopyTransformingInteger() {
            Person person = new Person("Alice", 30);

            Person older = RecordUtil.copyTransforming(person, "age",
                    (Integer age) -> age + 1);

            assertThat(older.age()).isEqualTo(31);
        }
    }

    @Nested
    @DisplayName("lens() 测试")
    class LensTests {

        @Test
        @DisplayName("lens() 创建透镜")
        void testLens() {
            Lens<Person, String> nameLens = RecordUtil.lens(Person.class, "name");
            Person person = new Person("Alice", 30);

            assertThat(nameLens.get(person)).isEqualTo("Alice");
        }

        @Test
        @DisplayName("lens() 透镜 set")
        void testLensSet() {
            Lens<Person, String> nameLens = RecordUtil.lens(Person.class, "name");
            Person person = new Person("Alice", 30);

            Person renamed = nameLens.set(person, "Bob");

            assertThat(renamed.name()).isEqualTo("Bob");
            assertThat(renamed.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("lens() 透镜 modify")
        void testLensModify() {
            Lens<Person, String> nameLens = RecordUtil.lens(Person.class, "name");
            Person person = new Person("alice", 30);

            Person modified = nameLens.modify(person, String::toUpperCase);

            assertThat(modified.name()).isEqualTo("ALICE");
        }
    }

    @Nested
    @DisplayName("lenses() 测试")
    class LensesTests {

        @Test
        @DisplayName("lenses() 为所有组件创建透镜")
        void testLenses() {
            Map<String, Lens<Person, ?>> lenses = RecordUtil.lenses(Person.class);

            assertThat(lenses).containsKeys("name", "age");
        }

        @Test
        @DisplayName("lenses() 创建的透镜可用")
        void testLensesUsable() {
            Map<String, Lens<Person, ?>> lenses = RecordUtil.lenses(Person.class);
            Person person = new Person("Alice", 30);

            @SuppressWarnings("unchecked")
            Lens<Person, String> nameLens = (Lens<Person, String>) lenses.get("name");

            assertThat(nameLens.get(person)).isEqualTo("Alice");
        }
    }

    @Nested
    @DisplayName("toMap() 测试")
    class ToMapTests {

        @Test
        @DisplayName("toMap() 将 record 转换为 Map")
        void testToMap() {
            Person person = new Person("Alice", 30);

            Map<String, Object> map = RecordUtil.toMap(person);

            assertThat(map).containsEntry("name", "Alice");
            assertThat(map).containsEntry("age", 30);
        }

        @Test
        @DisplayName("toMap() 保持组件顺序")
        void testToMapOrder() {
            Person person = new Person("Alice", 30);

            Map<String, Object> map = RecordUtil.toMap(person);
            List<String> keys = List.copyOf(map.keySet());

            assertThat(keys).containsExactly("name", "age");
        }
    }

    @Nested
    @DisplayName("fromMap() 测试")
    class FromMapTests {

        @Test
        @DisplayName("fromMap() 从 Map 创建 record")
        void testFromMap() {
            Map<String, Object> map = Map.of("name", "Alice", "age", 30);

            Person person = RecordUtil.fromMap(Person.class, map);

            assertThat(person.name()).isEqualTo("Alice");
            assertThat(person.age()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("toMapTry() 测试")
    class ToMapTryTests {

        @Test
        @DisplayName("toMapTry() 成功返回 Success")
        void testToMapTrySuccess() {
            Person person = new Person("Alice", 30);

            Try<Map<String, Object>> result = RecordUtil.toMapTry(person);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).containsEntry("name", "Alice");
        }
    }

    @Nested
    @DisplayName("fromMapTry() 测试")
    class FromMapTryTests {

        @Test
        @DisplayName("fromMapTry() 成功返回 Success")
        void testFromMapTrySuccess() {
            Map<String, Object> map = Map.of("name", "Alice", "age", 30);

            Try<Person> result = RecordUtil.fromMapTry(Person.class, map);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get().name()).isEqualTo("Alice");
        }
    }

    @Nested
    @DisplayName("diff() 测试")
    class DiffTests {

        @Test
        @DisplayName("diff() 找出不同的组件")
        void testDiff() {
            Person p1 = new Person("Alice", 30);
            Person p2 = new Person("Alice", 31);

            Map<String, Object[]> differences = RecordUtil.diff(p1, p2);

            assertThat(differences).containsKey("age");
            assertThat(differences.get("age")[0]).isEqualTo(30);
            assertThat(differences.get("age")[1]).isEqualTo(31);
        }

        @Test
        @DisplayName("diff() 相同 record 返回空 Map")
        void testDiffSame() {
            Person p1 = new Person("Alice", 30);
            Person p2 = new Person("Alice", 30);

            Map<String, Object[]> differences = RecordUtil.diff(p1, p2);

            assertThat(differences).isEmpty();
        }

        @Test
        @DisplayName("diff() 多个不同组件")
        void testDiffMultiple() {
            Person p1 = new Person("Alice", 30);
            Person p2 = new Person("Bob", 25);

            Map<String, Object[]> differences = RecordUtil.diff(p1, p2);

            assertThat(differences).containsKeys("name", "age");
        }

        @Test
        @DisplayName("diff() 不同类型抛出异常")
        void testDiffDifferentTypes() {
            Person person = new Person("Alice", 30);
            Address address = new Address("NYC", "5th Ave");

            assertThatThrownBy(() -> RecordUtil.diff(person, address))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("same type");
        }
    }

    @Nested
    @DisplayName("factory() 测试")
    class FactoryTests {

        @Test
        @DisplayName("factory() 创建工厂函数")
        void testFactory() {
            Function<Object[], Person> factory = RecordUtil.factory(Person.class);

            Person person = factory.apply(new Object[]{"Alice", 30});

            assertThat(person.name()).isEqualTo("Alice");
            assertThat(person.age()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("factory2() 测试")
    class Factory2Tests {

        @Test
        @DisplayName("factory2() 创建双参数工厂")
        void testFactory2() {
            BiFunction<String, Integer, Person> factory = RecordUtil.factory2(Person.class);

            Person person = factory.apply("Alice", 30);

            assertThat(person.name()).isEqualTo("Alice");
            assertThat(person.age()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("hasComponent() 测试")
    class HasComponentTests {

        @Test
        @DisplayName("hasComponent() 存在时返回 true")
        void testHasComponentTrue() {
            assertThat(RecordUtil.hasComponent(Person.class, "name")).isTrue();
            assertThat(RecordUtil.hasComponent(Person.class, "age")).isTrue();
        }

        @Test
        @DisplayName("hasComponent() 不存在时返回 false")
        void testHasComponentFalse() {
            assertThat(RecordUtil.hasComponent(Person.class, "address")).isFalse();
            assertThat(RecordUtil.hasComponent(Person.class, "notExist")).isFalse();
        }
    }

    @Nested
    @DisplayName("综合场景测试")
    class CompleteScenarioTests {

        @Test
        @DisplayName("Record 操作完整流程")
        void testCompleteWorkflow() {
            // 创建
            Person person = new Person("alice", 30);

            // 转换为 Map
            Map<String, Object> map = RecordUtil.toMap(person);
            assertThat(map).hasSize(2);

            // 使用透镜修改
            Lens<Person, String> nameLens = RecordUtil.lens(Person.class, "name");
            Person capitalized = nameLens.modify(person, String::toUpperCase);
            assertThat(capitalized.name()).isEqualTo("ALICE");

            // 使用 copy 修改
            Person older = RecordUtil.copy(capitalized, Map.of("age", 31));
            assertThat(older.age()).isEqualTo(31);

            // 比较差异
            Map<String, Object[]> diff = RecordUtil.diff(person, older);
            assertThat(diff).containsKeys("name", "age");
        }

        @Test
        @DisplayName("嵌套 Record 操作")
        void testNestedRecord() {
            Address address = new Address("NYC", "5th Ave");
            PersonWithAddress person = new PersonWithAddress("Alice", 30, address);

            // 获取嵌套组件
            Address addr = RecordUtil.getComponent(person, "address");
            assertThat(addr.city()).isEqualTo("NYC");

            // 修改嵌套组件
            Address newAddress = new Address("LA", "Sunset Blvd");
            PersonWithAddress moved = RecordUtil.copyWith(person, "address", newAddress);
            assertThat(moved.address().city()).isEqualTo("LA");
        }
    }
}
