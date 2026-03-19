package cloud.opencode.base.core.builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * RecordBuilder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("RecordBuilder 测试")
class RecordBuilderTest {

    @Nested
    @DisplayName("of 静态方法测试")
    class OfTests {

        @Test
        @DisplayName("of 创建构建器")
        void testOf() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("of 非 Record 类抛异常")
        void testOfNonRecord() {
            assertThatThrownBy(() -> RecordBuilder.of((Class) String.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must be a record");
        }
    }

    @Nested
    @DisplayName("from 静态方法测试")
    class FromTests {

        @Test
        @DisplayName("from 复制现有 Record")
        void testFrom() {
            TestRecord source = new TestRecord("Original", 30);

            TestRecord copy = RecordBuilder.from(source).build();

            assertThat(copy.name()).isEqualTo("Original");
            assertThat(copy.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("from 允许修改")
        void testFromWithModification() {
            TestRecord source = new TestRecord("Original", 30);

            TestRecord modified = RecordBuilder.from(source)
                    .set("name", "Modified")
                    .build();

            assertThat(modified.name()).isEqualTo("Modified");
            assertThat(modified.age()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("set 测试")
    class SetTests {

        @Test
        @DisplayName("set 设置组件")
        void testSet() {
            TestRecord record = RecordBuilder.of(TestRecord.class)
                    .set("name", "John")
                    .set("age", 25)
                    .build();

            assertThat(record.name()).isEqualTo("John");
            assertThat(record.age()).isEqualTo(25);
        }

        @Test
        @DisplayName("set 链式调用")
        void testSetChaining() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);

            RecordBuilder<TestRecord> result = builder
                    .set("name", "John")
                    .set("age", 25);

            assertThat(result).isSameAs(builder);
        }

        @Test
        @DisplayName("set 覆盖已有值")
        void testSetOverwrite() {
            TestRecord record = RecordBuilder.of(TestRecord.class)
                    .set("name", "First")
                    .set("name", "Second")
                    .set("age", 25)
                    .build();

            assertThat(record.name()).isEqualTo("Second");
        }
    }

    @Nested
    @DisplayName("setIfNotNull 测试")
    class SetIfNotNullTests {

        @Test
        @DisplayName("setIfNotNull 非 null 时设置")
        void testSetIfNotNullWithValue() {
            TestRecord record = RecordBuilder.of(TestRecord.class)
                    .set("age", 25)
                    .setIfNotNull("name", "John")
                    .build();

            assertThat(record.name()).isEqualTo("John");
        }

        @Test
        @DisplayName("setIfNotNull null 时不设置")
        void testSetIfNotNullWithNull() {
            TestRecord record = RecordBuilder.of(TestRecord.class)
                    .set("name", "Default")
                    .set("age", 25)
                    .setIfNotNull("name", null)
                    .build();

            assertThat(record.name()).isEqualTo("Default");
        }
    }

    @Nested
    @DisplayName("setIf 测试")
    class SetIfTests {

        @Test
        @DisplayName("setIf 条件为 true 时设置")
        void testSetIfTrue() {
            TestRecord record = RecordBuilder.of(TestRecord.class)
                    .set("age", 25)
                    .setIf(true, "name", "John")
                    .build();

            assertThat(record.name()).isEqualTo("John");
        }

        @Test
        @DisplayName("setIf 条件为 false 时不设置")
        void testSetIfFalse() {
            TestRecord record = RecordBuilder.of(TestRecord.class)
                    .set("name", "Default")
                    .set("age", 25)
                    .setIf(false, "name", "John")
                    .build();

            assertThat(record.name()).isEqualTo("Default");
        }
    }

    @Nested
    @DisplayName("setAll 测试")
    class SetAllTests {

        @Test
        @DisplayName("setAll 批量设置")
        void testSetAll() {
            Map<String, Object> props = Map.of(
                    "name", "John",
                    "age", 25
            );

            TestRecord record = RecordBuilder.of(TestRecord.class)
                    .setAll(props)
                    .build();

            assertThat(record.name()).isEqualTo("John");
            assertThat(record.age()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("configure 测试")
    class ConfigureTests {

        @Test
        @DisplayName("configure 回调配置")
        void testConfigure() {
            TestRecord record = RecordBuilder.of(TestRecord.class)
                    .configure(builder -> {
                        builder.set("name", "John");
                        builder.set("age", 25);
                    })
                    .build();

            assertThat(record.name()).isEqualTo("John");
            assertThat(record.age()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("build 测试")
    class BuildTests {

        @Test
        @DisplayName("build 创建新 Record")
        void testBuild() {
            TestRecord record = RecordBuilder.of(TestRecord.class)
                    .set("name", "John")
                    .set("age", 25)
                    .build();

            assertThat(record).isNotNull();
            assertThat(record.name()).isEqualTo("John");
            assertThat(record.age()).isEqualTo(25);
        }

        @Test
        @DisplayName("build 返回不同实例")
        void testBuildReturnsDifferentInstances() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class)
                    .set("name", "John")
                    .set("age", 25);

            TestRecord record1 = builder.build();
            TestRecord record2 = builder.build();

            assertThat(record1).isNotSameAs(record2);
            assertThat(record1).isEqualTo(record2);
        }
    }

    @Nested
    @DisplayName("buildAndValidate 测试")
    class BuildAndValidateTests {

        @Test
        @DisplayName("buildAndValidate 执行验证")
        void testBuildAndValidate() {
            AtomicBoolean validated = new AtomicBoolean(false);

            TestRecord record = RecordBuilder.of(TestRecord.class)
                    .set("name", "John")
                    .set("age", 25)
                    .buildAndValidate(r -> validated.set(true));

            assertThat(validated.get()).isTrue();
            assertThat(record.name()).isEqualTo("John");
        }

        @Test
        @DisplayName("buildAndValidate 验证失败抛异常")
        void testBuildAndValidateThrows() {
            assertThatThrownBy(() ->
                    RecordBuilder.of(TestRecord.class)
                            .set("name", "John")
                            .set("age", -1)
                            .buildAndValidate(r -> {
                                if (r.age() < 0) {
                                    throw new IllegalArgumentException("Age must be positive");
                                }
                            })
            ).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("复杂 Record 测试")
    class ComplexRecordTests {

        @Test
        @DisplayName("嵌套 Record")
        void testNestedRecord() {
            Address address = new Address("Beijing", "100000");

            PersonWithAddress person = RecordBuilder.of(PersonWithAddress.class)
                    .set("name", "John")
                    .set("address", address)
                    .build();

            assertThat(person.name()).isEqualTo("John");
            assertThat(person.address()).isEqualTo(address);
        }

        @Test
        @DisplayName("多组件 Record")
        void testMultiComponentRecord() {
            DetailedPerson person = RecordBuilder.of(DetailedPerson.class)
                    .set("name", "John")
                    .set("age", 25)
                    .set("email", "john@example.com")
                    .set("active", true)
                    .build();

            assertThat(person.name()).isEqualTo("John");
            assertThat(person.age()).isEqualTo(25);
            assertThat(person.email()).isEqualTo("john@example.com");
            assertThat(person.active()).isTrue();
        }
    }

    @Nested
    @DisplayName("Builder 接口实现测试")
    class BuilderInterfaceTests {

        @Test
        @DisplayName("实现 Builder 接口")
        void testImplementsBuilder() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            assertThat(builder).isInstanceOf(Builder.class);
        }
    }

    public record TestRecord(String name, int age) {}

    public record Address(String city, String zipCode) {}

    public record PersonWithAddress(String name, Address address) {}

    public record DetailedPerson(String name, int age, String email, boolean active) {}
}
