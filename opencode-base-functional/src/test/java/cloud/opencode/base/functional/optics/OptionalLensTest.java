package cloud.opencode.base.functional.optics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * OptionalLens 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("OptionalLens 测试")
class OptionalLensTest {

    // Test record types
    record User(String name, Address address) {}
    record Address(String city, String street) {}
    record Team(String name, User leader) {}

    // OptionalLens definitions for tests
    static final OptionalLens<User, Address> ADDRESS_LENS = OptionalLens.of(
            user -> Optional.ofNullable(user.address()),
            (user, address) -> new User(user.name(), address)
    );

    static final Lens<Address, String> CITY_LENS = Lens.of(
            Address::city,
            (addr, city) -> new Address(city, addr.street())
    );

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() 创建 OptionalLens")
        void testOf() {
            OptionalLens<User, Address> lens = OptionalLens.of(
                    user -> Optional.ofNullable(user.address()),
                    (user, address) -> new User(user.name(), address)
            );

            assertThat(lens).isNotNull();
        }

        @Test
        @DisplayName("ofNullable() 从可空 getter 创建")
        void testOfNullable() {
            OptionalLens<User, Address> lens = OptionalLens.ofNullable(
                    User::address,
                    (user, address) -> new User(user.name(), address)
            );
            User user = new User("Alice", new Address("NYC", "5th Ave"));

            assertThat(lens.get(user)).contains(new Address("NYC", "5th Ave"));
        }

        @Test
        @DisplayName("ofNullable() 处理 null")
        void testOfNullableWithNull() {
            OptionalLens<User, Address> lens = OptionalLens.ofNullable(
                    User::address,
                    (user, address) -> new User(user.name(), address)
            );
            User user = new User("Alice", null);

            assertThat(lens.get(user)).isEmpty();
        }

        @Test
        @DisplayName("fromLens() 从 Lens 创建")
        void testFromLens() {
            Lens<Address, String> cityLens = Lens.of(
                    Address::city,
                    (addr, city) -> new Address(city, addr.street())
            );
            OptionalLens<Address, String> optLens = OptionalLens.fromLens(cityLens);
            Address address = new Address("NYC", "5th Ave");

            assertThat(optLens.get(address)).contains("NYC");
        }
    }

    @Nested
    @DisplayName("get() 测试")
    class GetTests {

        @Test
        @DisplayName("get() 存在时返回 Optional.of")
        void testGetPresent() {
            User user = new User("Alice", new Address("NYC", "5th Ave"));

            Optional<Address> result = ADDRESS_LENS.get(user);

            assertThat(result).contains(new Address("NYC", "5th Ave"));
        }

        @Test
        @DisplayName("get() 不存在时返回 Optional.empty")
        void testGetAbsent() {
            User user = new User("Alice", null);

            Optional<Address> result = ADDRESS_LENS.get(user);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getOrElse() 测试")
    class GetOrElseTests {

        @Test
        @DisplayName("getOrElse() 存在时返回值")
        void testGetOrElsePresent() {
            User user = new User("Alice", new Address("NYC", "5th Ave"));
            Address defaultAddr = new Address("Default", "Default St");

            Address result = ADDRESS_LENS.getOrElse(user, defaultAddr);

            assertThat(result).isEqualTo(new Address("NYC", "5th Ave"));
        }

        @Test
        @DisplayName("getOrElse() 不存在时返回默认值")
        void testGetOrElseAbsent() {
            User user = new User("Alice", null);
            Address defaultAddr = new Address("Default", "Default St");

            Address result = ADDRESS_LENS.getOrElse(user, defaultAddr);

            assertThat(result).isEqualTo(defaultAddr);
        }
    }

    @Nested
    @DisplayName("isPresent() 测试")
    class IsPresentTests {

        @Test
        @DisplayName("isPresent() 存在时返回 true")
        void testIsPresentTrue() {
            User user = new User("Alice", new Address("NYC", "5th Ave"));

            assertThat(ADDRESS_LENS.isPresent(user)).isTrue();
        }

        @Test
        @DisplayName("isPresent() 不存在时返回 false")
        void testIsPresentFalse() {
            User user = new User("Alice", null);

            assertThat(ADDRESS_LENS.isPresent(user)).isFalse();
        }
    }

    @Nested
    @DisplayName("set() 测试")
    class SetTests {

        @Test
        @DisplayName("set() 设置值")
        void testSet() {
            User user = new User("Alice", null);
            Address newAddress = new Address("NYC", "5th Ave");

            User updated = ADDRESS_LENS.set(user, newAddress);

            assertThat(updated.address()).isEqualTo(newAddress);
            assertThat(updated.name()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("set() 替换现有值")
        void testSetReplace() {
            User user = new User("Alice", new Address("LA", "Sunset Blvd"));
            Address newAddress = new Address("NYC", "5th Ave");

            User updated = ADDRESS_LENS.set(user, newAddress);

            assertThat(updated.address()).isEqualTo(newAddress);
        }
    }

    @Nested
    @DisplayName("setIfPresent() 测试")
    class SetIfPresentTests {

        @Test
        @DisplayName("setIfPresent() Optional 有值时设置")
        void testSetIfPresentWithValue() {
            User user = new User("Alice", null);
            Address newAddress = new Address("NYC", "5th Ave");

            User updated = ADDRESS_LENS.setIfPresent(user, Optional.of(newAddress));

            assertThat(updated.address()).isEqualTo(newAddress);
        }

        @Test
        @DisplayName("setIfPresent() Optional 为空时不设置")
        void testSetIfPresentEmpty() {
            User user = new User("Alice", new Address("LA", "Sunset Blvd"));

            User updated = ADDRESS_LENS.setIfPresent(user, Optional.empty());

            assertThat(updated.address()).isEqualTo(new Address("LA", "Sunset Blvd"));
        }
    }

    @Nested
    @DisplayName("modify() 测试")
    class ModifyTests {

        @Test
        @DisplayName("modify() 存在时修改")
        void testModifyPresent() {
            User user = new User("Alice", new Address("nyc", "5th Ave"));

            User updated = ADDRESS_LENS.modify(user, addr ->
                    new Address(addr.city().toUpperCase(), addr.street())
            );

            assertThat(updated.address().city()).isEqualTo("NYC");
        }

        @Test
        @DisplayName("modify() 不存在时返回原对象")
        void testModifyAbsent() {
            User user = new User("Alice", null);

            User updated = ADDRESS_LENS.modify(user, addr ->
                    new Address(addr.city().toUpperCase(), addr.street())
            );

            assertThat(updated).isEqualTo(user);
            assertThat(updated.address()).isNull();
        }
    }

    @Nested
    @DisplayName("modifyOrSet() 测试")
    class ModifyOrSetTests {

        @Test
        @DisplayName("modifyOrSet() 存在时修改")
        void testModifyOrSetPresent() {
            User user = new User("Alice", new Address("nyc", "5th Ave"));
            Address defaultAddr = new Address("Default", "Default St");

            User updated = ADDRESS_LENS.modifyOrSet(user, addr ->
                    new Address(addr.city().toUpperCase(), addr.street()), defaultAddr);

            assertThat(updated.address().city()).isEqualTo("NYC");
        }

        @Test
        @DisplayName("modifyOrSet() 不存在时设置默认值")
        void testModifyOrSetAbsent() {
            User user = new User("Alice", null);
            Address defaultAddr = new Address("Default", "Default St");

            User updated = ADDRESS_LENS.modifyOrSet(user, addr ->
                    new Address(addr.city().toUpperCase(), addr.street()), defaultAddr);

            assertThat(updated.address()).isEqualTo(defaultAddr);
        }
    }

    @Nested
    @DisplayName("andThen(OptionalLens) 组合测试")
    class AndThenOptionalLensTests {

        @Test
        @DisplayName("andThen() 组合两个 OptionalLens")
        void testAndThenOptionalLens() {
            OptionalLens<Address, String> optCityLens = OptionalLens.ofNullable(
                    Address::city,
                    (addr, city) -> new Address(city, addr.street())
            );
            OptionalLens<User, String> userCityLens = ADDRESS_LENS.andThen(optCityLens);

            User user = new User("Alice", new Address("NYC", "5th Ave"));

            assertThat(userCityLens.get(user)).contains("NYC");
        }

        @Test
        @DisplayName("andThen() 组合后第一个不存在返回空")
        void testAndThenOptionalLensFirstAbsent() {
            OptionalLens<Address, String> optCityLens = OptionalLens.ofNullable(
                    Address::city,
                    (addr, city) -> new Address(city, addr.street())
            );
            OptionalLens<User, String> userCityLens = ADDRESS_LENS.andThen(optCityLens);

            User user = new User("Alice", null);

            assertThat(userCityLens.get(user)).isEmpty();
        }

        @Test
        @DisplayName("andThen() 组合后可 set")
        void testAndThenOptionalLensSet() {
            OptionalLens<Address, String> optCityLens = OptionalLens.ofNullable(
                    Address::city,
                    (addr, city) -> new Address(city, addr.street())
            );
            OptionalLens<User, String> userCityLens = ADDRESS_LENS.andThen(optCityLens);

            User user = new User("Alice", new Address("NYC", "5th Ave"));
            User updated = userCityLens.set(user, "LA");

            // Note: set will modify if the intermediate exists
            assertThat(updated.address().city()).isEqualTo("LA");
        }
    }

    @Nested
    @DisplayName("andThen(Lens) 组合测试")
    class AndThenLensTests {

        @Test
        @DisplayName("andThen() 与 Lens 组合")
        void testAndThenLens() {
            OptionalLens<User, String> userCityLens = ADDRESS_LENS.andThen(CITY_LENS);

            User user = new User("Alice", new Address("NYC", "5th Ave"));

            assertThat(userCityLens.get(user)).contains("NYC");
        }

        @Test
        @DisplayName("andThen() 与 Lens 组合后第一个不存在返回空")
        void testAndThenLensFirstAbsent() {
            OptionalLens<User, String> userCityLens = ADDRESS_LENS.andThen(CITY_LENS);

            User user = new User("Alice", null);

            assertThat(userCityLens.get(user)).isEmpty();
        }

        @Test
        @DisplayName("andThen() 与 Lens 组合后可 modify")
        void testAndThenLensModify() {
            OptionalLens<User, String> userCityLens = ADDRESS_LENS.andThen(CITY_LENS);

            User user = new User("Alice", new Address("nyc", "5th Ave"));
            User updated = userCityLens.modify(user, String::toUpperCase);

            assertThat(updated.address().city()).isEqualTo("NYC");
        }
    }

    @Nested
    @DisplayName("compose(OptionalLens) 组合测试")
    class ComposeOptionalLensTests {

        @Test
        @DisplayName("compose() 与 OptionalLens 组合")
        void testComposeOptionalLens() {
            OptionalLens<Team, User> leaderLens = OptionalLens.ofNullable(
                    Team::leader,
                    (team, leader) -> new Team(team.name(), leader)
            );
            OptionalLens<Team, Address> teamLeaderAddressLens = ADDRESS_LENS.compose(leaderLens);

            Team team = new Team("Dev", new User("Alice", new Address("NYC", "5th Ave")));

            assertThat(teamLeaderAddressLens.get(team)).contains(new Address("NYC", "5th Ave"));
        }
    }

    @Nested
    @DisplayName("compose(Lens) 组合测试")
    class ComposeLensTests {

        @Test
        @DisplayName("compose() 与 Lens 组合")
        void testComposeLens() {
            Lens<Team, User> leaderLens = Lens.of(
                    Team::leader,
                    (team, leader) -> new Team(team.name(), leader)
            );
            OptionalLens<Team, Address> teamLeaderAddressLens = ADDRESS_LENS.compose(leaderLens);

            Team team = new Team("Dev", new User("Alice", new Address("NYC", "5th Ave")));

            assertThat(teamLeaderAddressLens.get(team)).contains(new Address("NYC", "5th Ave"));
        }

        @Test
        @DisplayName("compose() 与 Lens 组合后可 set")
        void testComposeLensSet() {
            Lens<Team, User> leaderLens = Lens.of(
                    Team::leader,
                    (team, leader) -> new Team(team.name(), leader)
            );
            OptionalLens<Team, Address> teamLeaderAddressLens = ADDRESS_LENS.compose(leaderLens);

            Team team = new Team("Dev", new User("Alice", null));
            Address newAddr = new Address("LA", "Sunset Blvd");

            Team updated = teamLeaderAddressLens.set(team, newAddr);

            assertThat(updated.leader().address()).isEqualTo(newAddr);
        }
    }

    @Nested
    @DisplayName("getter()/setter() 测试")
    class GetterSetterTests {

        @Test
        @DisplayName("getter() 返回 getter 函数")
        void testGetter() {
            var getter = ADDRESS_LENS.getter();
            User user = new User("Alice", new Address("NYC", "5th Ave"));

            assertThat(getter.apply(user)).contains(new Address("NYC", "5th Ave"));
        }

        @Test
        @DisplayName("setter() 返回 setter 函数")
        void testSetter() {
            var setter = ADDRESS_LENS.setter();
            User user = new User("Alice", null);
            Address newAddr = new Address("NYC", "5th Ave");

            User updated = setter.apply(user, newAddr);

            assertThat(updated.address()).isEqualTo(newAddr);
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString() 返回描述")
        void testToString() {
            String str = ADDRESS_LENS.toString();

            assertThat(str).startsWith("OptionalLens[");
        }
    }
}
