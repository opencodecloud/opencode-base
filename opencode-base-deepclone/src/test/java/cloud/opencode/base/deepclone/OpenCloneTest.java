package cloud.opencode.base.deepclone;

import cloud.opencode.base.deepclone.annotation.CloneIgnore;
import cloud.opencode.base.deepclone.annotation.CloneReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenClone 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("OpenClone 测试")
class OpenCloneTest {

    // Test entities
    public static class Person implements Serializable {
        private String name;
        private int age;
        private Address address;

        public Person() {}

        public Person(String name, int age, Address address) {
            this.name = name;
            this.age = age;
            this.address = address;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }
    }

    public static class Address implements Serializable {
        private String city;
        private String street;

        public Address() {}

        public Address(String city, String street) {
            this.city = city;
            this.street = street;
        }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
    }

    // Used only for isImmutable test - never registered as immutable
    public static class NonImmutableTestClass {
        private String data;
        public NonImmutableTestClass() {}
    }

    @Nested
    @DisplayName("clone() 测试")
    class CloneTests {

        @Test
        @DisplayName("克隆null返回null")
        void testCloneNull() {
            Person result = OpenClone.clone(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("克隆简单对象")
        void testCloneSimpleObject() {
            Person original = new Person("John", 30, null);

            Person cloned = OpenClone.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getName()).isEqualTo("John");
            assertThat(cloned.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("克隆嵌套对象")
        void testCloneNestedObject() {
            Address address = new Address("New York", "Broadway");
            Person original = new Person("John", 30, address);

            Person cloned = OpenClone.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getAddress()).isNotSameAs(original.getAddress());
            assertThat(cloned.getAddress().getCity()).isEqualTo("New York");
        }

        @Test
        @DisplayName("修改克隆对象不影响原对象")
        void testCloneIsolation() {
            Address address = new Address("New York", "Broadway");
            Person original = new Person("John", 30, address);

            Person cloned = OpenClone.clone(original);
            cloned.setName("Jane");
            cloned.getAddress().setCity("Boston");

            assertThat(original.getName()).isEqualTo("John");
            assertThat(original.getAddress().getCity()).isEqualTo("New York");
        }
    }

    @Nested
    @DisplayName("cloneBySerialization() 测试")
    class SerializationCloneTests {

        @Test
        @DisplayName("序列化克隆")
        void testCloneBySerialization() {
            Person original = new Person("John", 30, new Address("NYC", "5th Ave"));

            Person cloned = OpenClone.cloneBySerialization(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getName()).isEqualTo("John");
            assertThat(cloned.getAddress()).isNotSameAs(original.getAddress());
        }
    }

    @Nested
    @DisplayName("cloneByUnsafe() 测试")
    class UnsafeCloneTests {

        @Test
        @DisplayName("Unsafe克隆")
        void testCloneByUnsafe() {
            Person original = new Person("John", 30, new Address("NYC", "5th Ave"));

            Person cloned = OpenClone.cloneByUnsafe(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getName()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("clone(T, Cloner) 测试")
    class CloneWithClonerTests {

        @Test
        @DisplayName("使用自定义克隆器")
        void testCloneWithCustomCloner() {
            Person original = new Person("John", 30, null);
            Cloner cloner = OpenClone.builder().reflective().build();

            Person cloned = OpenClone.clone(original, cloner);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getName()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("cloneBatch() 测试")
    class BatchCloneTests {

        @Test
        @DisplayName("批量克隆null返回null")
        void testCloneBatchNull() {
            List<Person> result = OpenClone.cloneBatch(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("批量克隆空列表返回空列表")
        void testCloneBatchEmpty() {
            List<Person> result = OpenClone.cloneBatch(new ArrayList<>());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("批量克隆列表")
        void testCloneBatch() {
            List<Person> originals = List.of(
                    new Person("John", 30, null),
                    new Person("Jane", 25, null)
            );

            List<Person> cloned = OpenClone.cloneBatch(originals);

            assertThat(cloned).hasSize(2);
            assertThat(cloned.get(0)).isNotSameAs(originals.get(0));
            assertThat(cloned.get(0).getName()).isEqualTo("John");
            assertThat(cloned.get(1).getName()).isEqualTo("Jane");
        }
    }

    @Nested
    @DisplayName("cloneBatchParallel() 测试")
    class ParallelBatchCloneTests {

        @Test
        @DisplayName("并行批量克隆null返回null")
        void testCloneBatchParallelNull() {
            List<Person> result = OpenClone.cloneBatchParallel(null, 4);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("并行批量克隆空列表返回空列表")
        void testCloneBatchParallelEmpty() {
            List<Person> result = OpenClone.cloneBatchParallel(new ArrayList<>(), 4);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("并行批量克隆列表")
        void testCloneBatchParallel() {
            List<Person> originals = List.of(
                    new Person("John", 30, null),
                    new Person("Jane", 25, null),
                    new Person("Bob", 35, null)
            );

            List<Person> cloned = OpenClone.cloneBatchParallel(originals, 2);

            assertThat(cloned).hasSize(3);
            assertThat(cloned.get(0).getName()).isEqualTo("John");
            assertThat(cloned.get(1).getName()).isEqualTo("Jane");
            assertThat(cloned.get(2).getName()).isEqualTo("Bob");
        }
    }

    @Nested
    @DisplayName("cloneAsync() 测试")
    class AsyncCloneTests {

        @Test
        @DisplayName("异步克隆")
        void testCloneAsync() throws ExecutionException, InterruptedException {
            Person original = new Person("John", 30, null);

            CompletableFuture<Person> future = OpenClone.cloneAsync(original);
            Person cloned = future.get();

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getName()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("cloneBatchAsync() 测试")
    class AsyncBatchCloneTests {

        @Test
        @DisplayName("异步批量克隆")
        void testCloneBatchAsync() throws ExecutionException, InterruptedException {
            List<Person> originals = List.of(
                    new Person("John", 30, null),
                    new Person("Jane", 25, null)
            );

            CompletableFuture<List<Person>> future = OpenClone.cloneBatchAsync(originals);
            List<Person> cloned = future.get();

            assertThat(cloned).hasSize(2);
            assertThat(cloned.get(0).getName()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("isImmutable() 测试")
    class ImmutableTests {

        @Test
        @DisplayName("null是不可变的")
        void testNullIsImmutable() {
            assertThat(OpenClone.isImmutable(null)).isTrue();
        }

        @Test
        @DisplayName("基本类型是不可变的")
        void testPrimitiveIsImmutable() {
            assertThat(OpenClone.isImmutable(int.class)).isTrue();
            assertThat(OpenClone.isImmutable(long.class)).isTrue();
            assertThat(OpenClone.isImmutable(boolean.class)).isTrue();
        }

        @Test
        @DisplayName("枚举是不可变的")
        void testEnumIsImmutable() {
            assertThat(OpenClone.isImmutable(Thread.State.class)).isTrue();
        }

        @Test
        @DisplayName("普通类不是不可变的")
        void testClassIsNotImmutable() {
            // Use a unique class that's never registered as immutable
            assertThat(OpenClone.isImmutable(NonImmutableTestClass.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("registerImmutable() 测试")
    class RegisterImmutableTests {

        @Test
        @DisplayName("注册不可变类型")
        void testRegisterImmutable() {
            // Register custom immutable type
            OpenClone.registerImmutable(Person.class);

            // Now Person should be considered immutable
            assertThat(OpenClone.isImmutable(Person.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("getDefaultCloner() 测试")
    class DefaultClonerTests {

        @Test
        @DisplayName("获取默认克隆器")
        void testGetDefaultCloner() {
            Cloner cloner = OpenClone.getDefaultCloner();

            assertThat(cloner).isNotNull();
            assertThat(cloner.getStrategyName()).isEqualTo("reflective");
        }
    }

    @Nested
    @DisplayName("builder() 测试")
    class BuilderTests {

        @Test
        @DisplayName("创建构建器")
        void testBuilder() {
            ClonerBuilder builder = OpenClone.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("构建反射克隆器")
        void testBuildReflectiveCloner() {
            Cloner cloner = OpenClone.builder()
                    .reflective()
                    .build();

            assertThat(cloner.getStrategyName()).isEqualTo("reflective");
        }

        @Test
        @DisplayName("构建序列化克隆器")
        void testBuildSerializingCloner() {
            Cloner cloner = OpenClone.builder()
                    .serializing()
                    .build();

            assertThat(cloner.getStrategyName()).isEqualTo("serializing");
        }

        @Test
        @DisplayName("构建Unsafe克隆器")
        void testBuildUnsafeCloner() {
            Cloner cloner = OpenClone.builder()
                    .unsafe()
                    .build();

            assertThat(cloner.getStrategyName()).isEqualTo("unsafe");
        }
    }
}
