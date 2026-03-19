package cloud.opencode.base.functional.optics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Lens 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("Lens 测试")
class LensTest {

    // Test record types
    record Person(String name, int age) {}
    record Address(String city, String street) {}
    record Company(String name, Person ceo) {}
    record PersonWithAddress(String name, Address address) {}

    // Lens definitions for tests
    static final Lens<Person, String> NAME_LENS = Lens.of(
            Person::name,
            (person, name) -> new Person(name, person.age())
    );

    static final Lens<Person, Integer> AGE_LENS = Lens.of(
            Person::age,
            (person, age) -> new Person(person.name(), age)
    );

    static final Lens<Company, Person> CEO_LENS = Lens.of(
            Company::ceo,
            (company, ceo) -> new Company(company.name(), ceo)
    );

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() 创建 Lens")
        void testOf() {
            Lens<Person, String> lens = Lens.of(
                    Person::name,
                    (person, name) -> new Person(name, person.age())
            );

            assertThat(lens).isNotNull();
        }

        @Test
        @DisplayName("identity() 创建恒等 Lens")
        void testIdentity() {
            Lens<String, String> lens = Lens.identity();
            String value = "test";

            assertThat(lens.get(value)).isEqualTo(value);
            assertThat(lens.set(value, "new")).isEqualTo("new");
        }

        @Test
        @DisplayName("forRecord() 为 record 创建 Lens")
        void testForRecord() {
            Lens<Person, String> lens = Lens.forRecord(
                    Person::name,
                    (person, name) -> new Person(name, person.age())
            );
            Person person = new Person("Alice", 30);

            assertThat(lens.get(person)).isEqualTo("Alice");
            assertThat(lens.set(person, "Bob")).isEqualTo(new Person("Bob", 30));
        }
    }

    @Nested
    @DisplayName("get() 测试")
    class GetTests {

        @Test
        @DisplayName("get() 获取值")
        void testGet() {
            Person person = new Person("Alice", 30);

            String name = NAME_LENS.get(person);

            assertThat(name).isEqualTo("Alice");
        }

        @Test
        @DisplayName("get() 获取整数值")
        void testGetInteger() {
            Person person = new Person("Alice", 30);

            int age = AGE_LENS.get(person);

            assertThat(age).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("set() 测试")
    class SetTests {

        @Test
        @DisplayName("set() 设置值并返回新对象")
        void testSet() {
            Person person = new Person("Alice", 30);

            Person updated = NAME_LENS.set(person, "Bob");

            assertThat(updated.name()).isEqualTo("Bob");
            assertThat(updated.age()).isEqualTo(30);
            // 原对象不变
            assertThat(person.name()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("set() 设置整数值")
        void testSetInteger() {
            Person person = new Person("Alice", 30);

            Person updated = AGE_LENS.set(person, 25);

            assertThat(updated.age()).isEqualTo(25);
            assertThat(updated.name()).isEqualTo("Alice");
        }
    }

    @Nested
    @DisplayName("modify() 测试")
    class ModifyTests {

        @Test
        @DisplayName("modify() 使用函数修改值")
        void testModify() {
            Person person = new Person("alice", 30);

            Person updated = NAME_LENS.modify(person, String::toUpperCase);

            assertThat(updated.name()).isEqualTo("ALICE");
            assertThat(updated.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("modify() 修改整数值")
        void testModifyInteger() {
            Person person = new Person("Alice", 30);

            Person updated = AGE_LENS.modify(person, age -> age + 1);

            assertThat(updated.age()).isEqualTo(31);
        }
    }

    @Nested
    @DisplayName("modifier() 测试")
    class ModifierTests {

        @Test
        @DisplayName("modifier() 创建修改函数")
        void testModifier() {
            var toUpper = NAME_LENS.modifier(String::toUpperCase);
            Person person = new Person("alice", 30);

            Person updated = toUpper.apply(person);

            assertThat(updated.name()).isEqualTo("ALICE");
        }

        @Test
        @DisplayName("modifier() 可复用")
        void testModifierReusable() {
            var incrementAge = AGE_LENS.modifier(age -> age + 1);
            Person person = new Person("Alice", 30);

            Person updated1 = incrementAge.apply(person);
            Person updated2 = incrementAge.apply(updated1);

            assertThat(updated1.age()).isEqualTo(31);
            assertThat(updated2.age()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("andThen() 组合测试")
    class AndThenTests {

        @Test
        @DisplayName("andThen() 组合两个 Lens")
        void testAndThen() {
            Company company = new Company("Acme", new Person("Alice", 30));
            Lens<Company, String> ceoNameLens = CEO_LENS.andThen(NAME_LENS);

            String ceoName = ceoNameLens.get(company);

            assertThat(ceoName).isEqualTo("Alice");
        }

        @Test
        @DisplayName("andThen() 组合后可 set")
        void testAndThenSet() {
            Company company = new Company("Acme", new Person("Alice", 30));
            Lens<Company, String> ceoNameLens = CEO_LENS.andThen(NAME_LENS);

            Company updated = ceoNameLens.set(company, "Bob");

            assertThat(updated.ceo().name()).isEqualTo("Bob");
            assertThat(updated.ceo().age()).isEqualTo(30);
            assertThat(updated.name()).isEqualTo("Acme");
        }

        @Test
        @DisplayName("andThen() 组合后可 modify")
        void testAndThenModify() {
            Company company = new Company("Acme", new Person("alice", 30));
            Lens<Company, String> ceoNameLens = CEO_LENS.andThen(NAME_LENS);

            Company updated = ceoNameLens.modify(company, String::toUpperCase);

            assertThat(updated.ceo().name()).isEqualTo("ALICE");
        }
    }

    @Nested
    @DisplayName("compose() 组合测试")
    class ComposeTests {

        @Test
        @DisplayName("compose() 组合两个 Lens")
        void testCompose() {
            Company company = new Company("Acme", new Person("Alice", 30));
            Lens<Company, String> ceoNameLens = NAME_LENS.compose(CEO_LENS);

            String ceoName = ceoNameLens.get(company);

            assertThat(ceoName).isEqualTo("Alice");
        }

        @Test
        @DisplayName("compose() 与 andThen() 等价")
        void testComposeEquivalentToAndThen() {
            Company company = new Company("Acme", new Person("Alice", 30));
            Lens<Company, String> lens1 = CEO_LENS.andThen(NAME_LENS);
            Lens<Company, String> lens2 = NAME_LENS.compose(CEO_LENS);

            assertThat(lens1.get(company)).isEqualTo(lens2.get(company));
            assertThat(lens1.set(company, "Bob")).isEqualTo(lens2.set(company, "Bob"));
        }
    }

    @Nested
    @DisplayName("asOptional() 转换测试")
    class AsOptionalTests {

        @Test
        @DisplayName("asOptional() 转换为 OptionalLens")
        void testAsOptional() {
            OptionalLens<Person, String> optLens = NAME_LENS.asOptional();
            Person person = new Person("Alice", 30);

            assertThat(optLens.get(person)).contains("Alice");
        }

        @Test
        @DisplayName("asOptional() 处理 null 值")
        void testAsOptionalWithNull() {
            Lens<Person, String> nullNameLens = Lens.of(
                    p -> null,
                    (p, name) -> new Person(name, p.age())
            );
            OptionalLens<Person, String> optLens = nullNameLens.asOptional();
            Person person = new Person("Alice", 30);

            assertThat(optLens.get(person)).isEmpty();
        }
    }

    @Nested
    @DisplayName("getter()/setter() 测试")
    class GetterSetterTests {

        @Test
        @DisplayName("getter() 返回 getter 函数")
        void testGetter() {
            var getter = NAME_LENS.getter();
            Person person = new Person("Alice", 30);

            assertThat(getter.apply(person)).isEqualTo("Alice");
        }

        @Test
        @DisplayName("setter() 返回 setter 函数")
        void testSetter() {
            var setter = NAME_LENS.setter();
            Person person = new Person("Alice", 30);

            Person updated = setter.apply(person, "Bob");

            assertThat(updated.name()).isEqualTo("Bob");
        }
    }

    @Nested
    @DisplayName("Lens Laws 测试")
    class LensLawsTests {

        @Test
        @DisplayName("Get-Set Law: set(s, get(s)) == s")
        void testGetSetLaw() {
            Person person = new Person("Alice", 30);

            Person result = NAME_LENS.set(person, NAME_LENS.get(person));

            assertThat(result).isEqualTo(person);
        }

        @Test
        @DisplayName("Set-Get Law: get(set(s, a)) == a")
        void testSetGetLaw() {
            Person person = new Person("Alice", 30);
            String newName = "Bob";

            String result = NAME_LENS.get(NAME_LENS.set(person, newName));

            assertThat(result).isEqualTo(newName);
        }

        @Test
        @DisplayName("Set-Set Law: set(set(s, a), b) == set(s, b)")
        void testSetSetLaw() {
            Person person = new Person("Alice", 30);
            String name1 = "Bob";
            String name2 = "Charlie";

            Person result1 = NAME_LENS.set(NAME_LENS.set(person, name1), name2);
            Person result2 = NAME_LENS.set(person, name2);

            assertThat(result1).isEqualTo(result2);
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString() 返回描述")
        void testToString() {
            String str = NAME_LENS.toString();

            assertThat(str).startsWith("Lens[");
        }
    }

    @Nested
    @DisplayName("深层嵌套测试")
    class DeepNestingTests {

        @Test
        @DisplayName("三层嵌套 get")
        void testThreeLevelNestingGet() {
            Lens<Address, String> cityLens = Lens.of(
                    Address::city,
                    (addr, city) -> new Address(city, addr.street())
            );
            Lens<PersonWithAddress, Address> addressLens = Lens.of(
                    PersonWithAddress::address,
                    (p, addr) -> new PersonWithAddress(p.name(), addr)
            );
            Lens<PersonWithAddress, String> personCityLens = addressLens.andThen(cityLens);

            PersonWithAddress person = new PersonWithAddress("Alice",
                    new Address("New York", "5th Ave"));

            assertThat(personCityLens.get(person)).isEqualTo("New York");
        }

        @Test
        @DisplayName("三层嵌套 set")
        void testThreeLevelNestingSet() {
            Lens<Address, String> cityLens = Lens.of(
                    Address::city,
                    (addr, city) -> new Address(city, addr.street())
            );
            Lens<PersonWithAddress, Address> addressLens = Lens.of(
                    PersonWithAddress::address,
                    (p, addr) -> new PersonWithAddress(p.name(), addr)
            );
            Lens<PersonWithAddress, String> personCityLens = addressLens.andThen(cityLens);

            PersonWithAddress person = new PersonWithAddress("Alice",
                    new Address("New York", "5th Ave"));

            PersonWithAddress moved = personCityLens.set(person, "Los Angeles");

            assertThat(moved.address().city()).isEqualTo("Los Angeles");
            assertThat(moved.address().street()).isEqualTo("5th Ave");
            assertThat(moved.name()).isEqualTo("Alice");
        }
    }
}
