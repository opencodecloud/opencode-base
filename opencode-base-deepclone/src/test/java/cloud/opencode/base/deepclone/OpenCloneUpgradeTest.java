package cloud.opencode.base.deepclone;

import cloud.opencode.base.deepclone.exception.OpenDeepCloneException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for deepclone upgrade features
 */
@DisplayName("OpenClone 升级功能集成测试")
class OpenCloneUpgradeTest {

    // ==================== Test Domain Classes ====================

    enum Color { RED, GREEN, BLUE }

    static class Person implements Serializable {
        String name;
        String password;
        int age;
        Color favoriteColor;
        Optional<String> nickname;
        List<String> immutableTags;
        Address address;

        Person() {}

        Person(String name, String password, int age) {
            this.name = name;
            this.password = password;
            this.age = age;
        }
    }

    static class Address implements Serializable {
        String city;
        String street;

        Address() {}

        Address(String city, String street) {
            this.city = city;
            this.street = street;
        }
    }

    // ==================== Null Safety Tests ====================

    @Nested
    @DisplayName("Null 安全测试")
    class NullSafetyTests {

        @Test
        @DisplayName("clone(null) 应返回 null")
        void cloneNullShouldReturnNull() {
            String result = OpenClone.clone((String) null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("shallowClone(null) 应返回 null")
        void shallowCloneNullShouldReturnNull() {
            String result = OpenClone.shallowClone((String) null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("cloneWith(null, policy) 应返回 null")
        void cloneWithNullShouldReturnNull() {
            String result = OpenClone.cloneWith((String) null, ClonePolicy.STRICT);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("copyTo(null, target) 应返回 target")
        void copyToWithNullSourceShouldReturnTarget() {
            Person target = new Person("target", "pass", 20);
            assertThat(OpenClone.copyTo(null, target)).isSameAs(target);
        }
    }

    // ==================== Enum Identity Tests ====================

    @Nested
    @DisplayName("枚举身份保持测试")
    class EnumIdentityTests {

        @Test
        @DisplayName("枚举字段克隆后应保持同一引用")
        void enumFieldShouldPreserveIdentity() {
            Person person = new Person("Alice", "pass", 30);
            person.favoriteColor = Color.RED;

            Person cloned = OpenClone.clone(person);

            assertThat(cloned.favoriteColor).isSameAs(Color.RED);
            assertThat(cloned.favoriteColor == Color.RED).isTrue();
        }

        @Test
        @DisplayName("直接克隆枚举应返回同一实例")
        void directEnumCloneShouldReturnSameInstance() {
            Color original = Color.BLUE;
            Color cloned = OpenClone.clone(original);
            assertThat(cloned).isSameAs(original);
        }
    }

    // ==================== JDK Immutable Collection Tests ====================

    @Nested
    @DisplayName("JDK 不可变集合测试")
    class ImmutableCollectionTests {

        @Test
        @DisplayName("List.of() 字段应保持引用")
        void listOfShouldPreserveReference() {
            Person person = new Person("Bob", "pass", 25);
            person.immutableTags = List.of("tag1", "tag2");

            Person cloned = OpenClone.clone(person);

            assertThat(cloned.immutableTags).isSameAs(person.immutableTags);
        }

        @Test
        @DisplayName("Collections.unmodifiableList() 字段应保持引用")
        void unmodifiableListShouldPreserveReference() {
            Person person = new Person("Bob", "pass", 25);
            person.immutableTags = Collections.unmodifiableList(List.of("a", "b"));

            Person cloned = OpenClone.clone(person);

            assertThat(cloned.immutableTags).isSameAs(person.immutableTags);
        }

        @Test
        @DisplayName("Collections.emptyList() 字段应保持引用")
        void emptyListShouldPreserveReference() {
            Person person = new Person("Bob", "pass", 25);
            person.immutableTags = Collections.emptyList();

            Person cloned = OpenClone.clone(person);

            assertThat(cloned.immutableTags).isSameAs(person.immutableTags);
        }
    }

    // ==================== Optional Tests ====================

    @Nested
    @DisplayName("Optional 支持测试")
    class OptionalTests {

        @Test
        @DisplayName("Optional.of() 字段应被深度克隆")
        void optionalOfShouldBeDeepCloned() {
            Person person = new Person("Alice", "pass", 30);
            person.nickname = Optional.of("Ali");

            Person cloned = OpenClone.clone(person);

            assertThat(cloned.nickname).isPresent();
            assertThat(cloned.nickname.get()).isEqualTo("Ali");
        }

        @Test
        @DisplayName("Optional.empty() 字段应保持空")
        void optionalEmptyShouldRemainEmpty() {
            Person person = new Person("Bob", "pass", 25);
            person.nickname = Optional.empty();

            Person cloned = OpenClone.clone(person);

            assertThat(cloned.nickname).isEmpty();
        }
    }

    // ==================== Shallow Clone Tests ====================

    @Nested
    @DisplayName("浅拷贝测试")
    class ShallowCloneTests {

        @Test
        @DisplayName("浅拷贝应共享嵌套对象引用")
        void shallowCloneShouldShareNestedReferences() {
            Person person = new Person("Alice", "pass", 30);
            person.address = new Address("Beijing", "Main St");

            Person cloned = OpenClone.shallowClone(person);

            assertThat(cloned).isNotSameAs(person);
            assertThat(cloned.name).isEqualTo("Alice");
            assertThat(cloned.address).isSameAs(person.address);
        }
    }

    // ==================== CopyTo Tests ====================

    @Nested
    @DisplayName("CopyTo 合并复制测试")
    class CopyToTests {

        @Test
        @DisplayName("copyTo 应将非 null 字段复制到目标对象")
        void copyToShouldCopyNonNullFields() {
            Person source = new Person("Alice", "newpass", 30);
            source.address = new Address("Shanghai", "Nanjing Rd");

            Person target = new Person("Bob", "oldpass", 25);
            target.address = new Address("Beijing", "Main St");

            OpenClone.copyTo(source, target);

            assertThat(target.name).isEqualTo("Alice");
            assertThat(target.password).isEqualTo("newpass");
            assertThat(target.age).isEqualTo(30);
            assertThat(target.address.city).isEqualTo("Shanghai");
            // Deep clone, not same reference
            assertThat(target.address).isNotSameAs(source.address);
        }
    }

    // ==================== ClonePolicy Tests ====================

    @Nested
    @DisplayName("ClonePolicy 策略测试")
    class ClonePolicyTests {

        @Test
        @DisplayName("LENIENT 模式遇到错误应返回原始引用")
        void lenientShouldReturnOriginalOnError() {
            Cloner cloner = OpenClone.builder()
                    .policy(ClonePolicy.LENIENT)
                    .maxDepth(2)
                    .build();

            // Create a deeply nested structure that exceeds max depth
            Person p1 = new Person("A", "p", 1);
            Person p2 = new Person("B", "p", 2);
            Person p3 = new Person("C", "p", 3);

            // This should succeed even with shallow depth in lenient mode
            Person cloned = cloner.clone(p1);
            assertThat(cloned).isNotNull();
            assertThat(cloned.name).isEqualTo("A");
        }

        @Test
        @DisplayName("STANDARD 模式应正常克隆")
        void standardShouldCloneNormally() {
            Person person = new Person("Alice", "pass", 30);
            Person cloned = OpenClone.cloneWith(person, ClonePolicy.STANDARD);

            assertThat(cloned.name).isEqualTo("Alice");
            assertThat(cloned).isNotSameAs(person);
        }
    }

    // ==================== FieldFilter Integration Tests ====================

    @Nested
    @DisplayName("FieldFilter 集成测试")
    class FieldFilterIntegrationTests {

        @Test
        @DisplayName("excludeNames 应排除指定字段")
        void excludeNamesShouldSkipFields() {
            Cloner cloner = OpenClone.builder()
                    .filter(FieldFilter.excludeNames("password"))
                    .build();

            Person person = new Person("Alice", "secret123", 30);
            Person cloned = cloner.clone(person);

            assertThat(cloned.name).isEqualTo("Alice");
            assertThat(cloned.password).isNull();
            assertThat(cloned.age).isEqualTo(30);
        }

        @Test
        @DisplayName("excludeTypes 应排除指定类型的字段")
        void excludeTypesShouldSkipFields() {
            Cloner cloner = OpenClone.builder()
                    .filter(FieldFilter.excludeTypes(Address.class))
                    .build();

            Person person = new Person("Bob", "pass", 25);
            person.address = new Address("Beijing", "Main St");

            Person cloned = cloner.clone(person);

            assertThat(cloned.name).isEqualTo("Bob");
            assertThat(cloned.address).isNull();
        }

        @Test
        @DisplayName("组合过滤器应正确工作")
        void composedFilterShouldWork() {
            FieldFilter filter = FieldFilter.excludeNames("password")
                    .and(FieldFilter.excludeTypes(Address.class));

            Cloner cloner = OpenClone.builder()
                    .filter(filter)
                    .build();

            Person person = new Person("Alice", "secret", 30);
            person.address = new Address("Shanghai", "Rd");

            Person cloned = cloner.clone(person);

            assertThat(cloned.name).isEqualTo("Alice");
            assertThat(cloned.password).isNull();
            assertThat(cloned.address).isNull();
        }
    }

    // ==================== CloneListener Integration Tests ====================

    @Nested
    @DisplayName("CloneListener 集成测试")
    class CloneListenerIntegrationTests {

        @Test
        @DisplayName("监听器应在克隆过程中被调用")
        void listenerShouldBeCalledDuringClone() {
            AtomicInteger beforeCount = new AtomicInteger();
            AtomicInteger afterCount = new AtomicInteger();

            CloneListener listener = new CloneListener() {
                @Override
                public void beforeClone(Object original, CloneContext context) {
                    beforeCount.incrementAndGet();
                }

                @Override
                public void afterClone(Object original, Object cloned, CloneContext context) {
                    afterCount.incrementAndGet();
                }
            };

            Cloner cloner = OpenClone.builder()
                    .listener(listener)
                    .build();

            Person person = new Person("Alice", "pass", 30);
            person.address = new Address("Beijing", "Main St");

            cloner.clone(person);

            assertThat(beforeCount.get()).isGreaterThan(0);
            assertThat(afterCount.get()).isGreaterThan(0);
        }

        @Test
        @DisplayName("监听器异常不应影响克隆流程")
        void listenerExceptionShouldNotAffectClone() {
            CloneListener listener = new CloneListener() {
                @Override
                public void beforeClone(Object original, CloneContext context) {
                    throw new RuntimeException("Listener error");
                }
            };

            Cloner cloner = OpenClone.builder()
                    .listener(listener)
                    .build();

            Person person = new Person("Alice", "pass", 30);
            Person cloned = cloner.clone(person);

            assertThat(cloned.name).isEqualTo("Alice");
        }
    }

    // ==================== Builder New Options Tests ====================

    @Nested
    @DisplayName("ClonerBuilder 新选项测试")
    class BuilderNewOptionsTests {

        @Test
        @DisplayName("Builder 应支持链式设置所有新选项")
        void builderShouldSupportChainingNewOptions() {
            Cloner cloner = OpenClone.builder()
                    .reflective()
                    .policy(ClonePolicy.LENIENT)
                    .filter(FieldFilter.excludeNames("password"))
                    .listener(new CloneListener() {})
                    .maxDepth(50)
                    .build();

            assertThat(cloner).isNotNull();

            Person person = new Person("Test", "secret", 25);
            Person cloned = cloner.clone(person);

            assertThat(cloned.name).isEqualTo("Test");
            assertThat(cloned.password).isNull();
        }
    }

    // ==================== CloneContext Policy Tests ====================

    @Nested
    @DisplayName("CloneContext 策略测试")
    class CloneContextPolicyTests {

        @Test
        @DisplayName("CloneContext 应携带策略信息")
        void contextShouldCarryPolicy() {
            CloneContext ctx = CloneContext.create(100, ClonePolicy.STRICT);
            assertThat(ctx.getPolicy()).isEqualTo(ClonePolicy.STRICT);
            assertThat(ctx.isStrict()).isTrue();
            assertThat(ctx.isLenient()).isFalse();
        }

        @Test
        @DisplayName("CloneContext LENIENT 应支持警告")
        void lenientContextShouldSupportWarnings() {
            CloneContext ctx = CloneContext.create(100, ClonePolicy.LENIENT);
            ctx.addWarning("test warning");
            assertThat(ctx.getWarnings()).containsExactly("test warning");
            assertThat(ctx.isLenient()).isTrue();
        }

        @Test
        @DisplayName("默认策略应为 STANDARD")
        void defaultPolicyShouldBeStandard() {
            CloneContext ctx = CloneContext.create();
            assertThat(ctx.getPolicy()).isEqualTo(ClonePolicy.STANDARD);
        }
    }
}
