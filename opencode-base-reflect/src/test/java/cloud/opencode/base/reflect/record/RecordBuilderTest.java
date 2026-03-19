package cloud.opencode.base.reflect.record;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * RecordBuilderTest Tests
 * RecordBuilderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("RecordBuilder 测试")
class RecordBuilderTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建RecordBuilder")
        void testCreate() {
            RecordBuilder<TestRecord> builder = new RecordBuilder<>(TestRecord.class);
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("非record类抛出异常")
        void testCreateNonRecord() {
            assertThatThrownBy(() -> new RecordBuilder(String.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("of静态方法测试")
    class OfTests {

        @Test
        @DisplayName("创建构建器")
        void testOf() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("from静态方法测试")
    class FromTests {

        @Test
        @DisplayName("从现有record复制")
        void testFrom() {
            TestRecord original = new TestRecord("test", 25);
            RecordBuilder<TestRecord> builder = RecordBuilder.from(original);
            TestRecord copy = builder.build();
            assertThat(copy).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("set按名称方法测试")
    class SetByNameTests {

        @Test
        @DisplayName("按名称设置值")
        void testSetByName() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.set("name", "test").set("age", 30);
            TestRecord record = builder.build();
            assertThat(record.name()).isEqualTo("test");
            assertThat(record.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("未知组件抛出异常")
        void testSetUnknown() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            assertThatThrownBy(() -> builder.set("unknown", "value"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("set按索引方法测试")
    class SetByIndexTests {

        @Test
        @DisplayName("按索引设置值")
        void testSetByIndex() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.set(0, "test").set(1, 30);
            TestRecord record = builder.build();
            assertThat(record.name()).isEqualTo("test");
            assertThat(record.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("索引越界抛出异常")
        void testSetByIndexOutOfBounds() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            assertThatThrownBy(() -> builder.set(10, "value"))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("setAll方法测试")
    class SetAllTests {

        @Test
        @DisplayName("从map设置多个值")
        void testSetAll() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.setAll(Map.of("name", "test", "age", 30));
            TestRecord record = builder.build();
            assertThat(record.name()).isEqualTo("test");
            assertThat(record.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("忽略未知键")
        void testSetAllIgnoresUnknown() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.setAll(Map.of("name", "test", "unknown", "ignored"));
            assertThat(builder.getValue("name")).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("copyFrom方法测试")
    class CopyFromTests {

        @Test
        @DisplayName("从record复制值")
        void testCopyFrom() {
            TestRecord source = new TestRecord("source", 25);
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.copyFrom(source);
            TestRecord copy = builder.build();
            assertThat(copy).isEqualTo(source);
        }
    }

    @Nested
    @DisplayName("setIfAbsent方法测试")
    class SetIfAbsentTests {

        @Test
        @DisplayName("值不存在时设置")
        void testSetIfAbsentWhenAbsent() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.setIfAbsent("name", "default");
            assertThat(builder.getValue("name")).isEqualTo("default");
        }

        @Test
        @DisplayName("值存在时不覆盖")
        void testSetIfAbsentWhenPresent() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.set("name", "existing");
            builder.setIfAbsent("name", "default");
            assertThat(builder.getValue("name")).isEqualTo("existing");
        }
    }

    @Nested
    @DisplayName("setIfNotNull方法测试")
    class SetIfNotNullTests {

        @Test
        @DisplayName("非null值设置")
        void testSetIfNotNullWithValue() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.setIfNotNull("name", "test");
            assertThat(builder.getValue("name")).isEqualTo("test");
        }

        @Test
        @DisplayName("null值不设置")
        void testSetIfNotNullWithNull() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.set("name", "existing");
            builder.setIfNotNull("name", null);
            assertThat(builder.getValue("name")).isEqualTo("existing");
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清除特定值")
        void testClear() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.set("name", "test");
            builder.clear("name");
            assertThat(builder.hasValue("name")).isFalse();
        }
    }

    @Nested
    @DisplayName("clearAll方法测试")
    class ClearAllTests {

        @Test
        @DisplayName("清除所有值")
        void testClearAll() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.set("name", "test").set("age", 30);
            builder.clearAll();
            assertThat(builder.hasValue("name")).isFalse();
            assertThat(builder.hasValue("age")).isFalse();
        }
    }

    @Nested
    @DisplayName("getValue方法测试")
    class GetValueTests {

        @Test
        @DisplayName("获取当前值")
        void testGetValue() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.set("name", "test");
            assertThat(builder.getValue("name")).isEqualTo("test");
        }

        @Test
        @DisplayName("未设置值返回null")
        void testGetValueNull() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            assertThat(builder.getValue("name")).isNull();
        }
    }

    @Nested
    @DisplayName("hasValue方法测试")
    class HasValueTests {

        @Test
        @DisplayName("有值返回true")
        void testHasValueTrue() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.set("name", "test");
            assertThat(builder.hasValue("name")).isTrue();
        }

        @Test
        @DisplayName("无值返回false")
        void testHasValueFalse() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            assertThat(builder.hasValue("name")).isFalse();
        }
    }

    @Nested
    @DisplayName("getRecordClass方法测试")
    class GetRecordClassTests {

        @Test
        @DisplayName("获取record类")
        void testGetRecordClass() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            assertThat(builder.getRecordClass()).isEqualTo(TestRecord.class);
        }
    }

    @Nested
    @DisplayName("getComponents方法测试")
    class GetComponentsTests {

        @Test
        @DisplayName("获取组件列表")
        void testGetComponents() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            List<RecordComponent> components = builder.getComponents();
            assertThat(components).hasSize(2);
        }

        @Test
        @DisplayName("组件列表不可修改")
        void testGetComponentsUnmodifiable() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            List<RecordComponent> components = builder.getComponents();
            assertThatThrownBy(() -> components.add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("build方法测试")
    class BuildTests {

        @Test
        @DisplayName("构建record实例")
        void testBuild() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.set("name", "test").set("age", 30);
            TestRecord record = builder.build();
            assertThat(record.name()).isEqualTo("test");
            assertThat(record.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("原始类型使用默认值")
        void testBuildWithDefaultPrimitive() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.set("name", "test");
            TestRecord record = builder.build();
            assertThat(record.age()).isEqualTo(0);
        }

        @Test
        @DisplayName("所有原始类型默认值")
        void testAllPrimitiveDefaults() {
            RecordBuilder<PrimitiveRecord> builder = RecordBuilder.of(PrimitiveRecord.class);
            PrimitiveRecord record = builder.build();
            assertThat(record.boolVal()).isFalse();
            assertThat(record.byteVal()).isEqualTo((byte) 0);
            assertThat(record.shortVal()).isEqualTo((short) 0);
            assertThat(record.intVal()).isEqualTo(0);
            assertThat(record.longVal()).isEqualTo(0L);
            assertThat(record.floatVal()).isEqualTo(0f);
            assertThat(record.doubleVal()).isEqualTo(0d);
            assertThat(record.charVal()).isEqualTo('\0');
        }
    }

    @Nested
    @DisplayName("buildValidated方法测试")
    class BuildValidatedTests {

        @Test
        @DisplayName("所有值都设置时成功构建")
        void testBuildValidatedSuccess() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.set("name", "test").set("age", 30);
            TestRecord record = builder.buildValidated();
            assertThat(record).isNotNull();
        }

        @Test
        @DisplayName("缺少必需值时抛出异常")
        void testBuildValidatedMissing() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.set("age", 30);  // name is missing
            assertThatThrownBy(() -> builder.buildValidated())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("原始类型不视为缺失")
        void testBuildValidatedPrimitive() {
            RecordBuilder<TestRecord> builder = RecordBuilder.of(TestRecord.class);
            builder.set("name", "test");  // age (primitive) not set
            assertThatCode(() -> builder.buildValidated()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("链式设置多个值")
        void testFluentChaining() {
            TestRecord record = RecordBuilder.of(TestRecord.class)
                    .set("name", "fluent")
                    .set("age", 25)
                    .build();

            assertThat(record.name()).isEqualTo("fluent");
            assertThat(record.age()).isEqualTo(25);
        }
    }

    // Test helper records
    record TestRecord(String name, int age) {}
    record PrimitiveRecord(boolean boolVal, byte byteVal, short shortVal, int intVal,
                           long longVal, float floatVal, double doubleVal, char charVal) {}
}
