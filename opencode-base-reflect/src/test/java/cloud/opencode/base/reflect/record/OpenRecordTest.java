package cloud.opencode.base.reflect.record;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenRecordTest Tests
 * OpenRecordTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("OpenRecord 测试")
class OpenRecordTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = OpenRecord.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("isRecord方法测试")
    class IsRecordTests {

        @Test
        @DisplayName("record类返回true")
        void testIsRecordTrue() {
            assertThat(OpenRecord.isRecord(TestRecord.class)).isTrue();
        }

        @Test
        @DisplayName("非record类返回false")
        void testIsRecordFalse() {
            assertThat(OpenRecord.isRecord(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isRecordInstance方法测试")
    class IsRecordInstanceTests {

        @Test
        @DisplayName("record实例返回true")
        void testIsRecordInstanceTrue() {
            assertThat(OpenRecord.isRecordInstance(new TestRecord("test", 25))).isTrue();
        }

        @Test
        @DisplayName("非record实例返回false")
        void testIsRecordInstanceFalse() {
            assertThat(OpenRecord.isRecordInstance("test")).isFalse();
        }
    }

    @Nested
    @DisplayName("requireRecord方法测试")
    class RequireRecordTests {

        @Test
        @DisplayName("record类返回自身")
        void testRequireRecordSuccess() {
            Class<TestRecord> result = OpenRecord.requireRecord(TestRecord.class);
            assertThat(result).isEqualTo(TestRecord.class);
        }

        @Test
        @DisplayName("非record类抛出异常")
        void testRequireRecordFailure() {
            assertThatThrownBy(() -> OpenRecord.requireRecord(String.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getComponents方法测试")
    class GetComponentsTests {

        @Test
        @DisplayName("获取record组件")
        void testGetComponents() {
            List<RecordComponent> components = OpenRecord.getComponents(TestRecord.class);
            assertThat(components).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getComponent按名称方法测试")
    class GetComponentByNameTests {

        @Test
        @DisplayName("按名称获取组件")
        void testGetComponentByName() {
            RecordComponent component = OpenRecord.getComponent(TestRecord.class, "name");
            assertThat(component).isNotNull();
            assertThat(component.getName()).isEqualTo("name");
        }

        @Test
        @DisplayName("不存在的组件返回null")
        void testGetComponentByNameNotFound() {
            RecordComponent component = OpenRecord.getComponent(TestRecord.class, "nonexistent");
            assertThat(component).isNull();
        }
    }

    @Nested
    @DisplayName("getComponent按索引方法测试")
    class GetComponentByIndexTests {

        @Test
        @DisplayName("按索引获取组件")
        void testGetComponentByIndex() {
            RecordComponent component = OpenRecord.getComponent(TestRecord.class, 0);
            assertThat(component.getName()).isEqualTo("name");
        }

        @Test
        @DisplayName("索引越界抛出异常")
        void testGetComponentByIndexOutOfBounds() {
            assertThatThrownBy(() -> OpenRecord.getComponent(TestRecord.class, 10))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("getComponentNames方法测试")
    class GetComponentNamesTests {

        @Test
        @DisplayName("获取组件名称")
        void testGetComponentNames() {
            List<String> names = OpenRecord.getComponentNames(TestRecord.class);
            assertThat(names).containsExactly("name", "age");
        }
    }

    @Nested
    @DisplayName("getComponentTypes方法测试")
    class GetComponentTypesTests {

        @Test
        @DisplayName("获取组件类型")
        void testGetComponentTypes() {
            List<Class<?>> types = OpenRecord.getComponentTypes(TestRecord.class);
            assertThat(types).containsExactly(String.class, int.class);
        }
    }

    @Nested
    @DisplayName("getComponentCount方法测试")
    class GetComponentCountTests {

        @Test
        @DisplayName("获取组件数量")
        void testGetComponentCount() {
            assertThat(OpenRecord.getComponentCount(TestRecord.class)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getValue方法测试")
    class GetValueTests {

        @Test
        @DisplayName("获取组件值")
        void testGetValue() {
            TestRecord record = new TestRecord("test", 25);
            Object value = OpenRecord.getValue(record, "name");
            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("获取组件值带类型")
        void testGetValueTyped() {
            TestRecord record = new TestRecord("test", 25);
            String value = OpenRecord.getValue(record, "name", String.class);
            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("不存在的组件抛出异常")
        void testGetValueNotFound() {
            TestRecord record = new TestRecord("test", 25);
            assertThatThrownBy(() -> OpenRecord.getValue(record, "nonexistent"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getValues方法测试")
    class GetValuesTests {

        @Test
        @DisplayName("获取所有组件值")
        void testGetValues() {
            TestRecord record = new TestRecord("test", 25);
            Object[] values = OpenRecord.getValues(record);
            assertThat(values).containsExactly("test", 25);
        }
    }

    @Nested
    @DisplayName("toMap方法测试")
    class ToMapTests {

        @Test
        @DisplayName("将record转换为map")
        void testToMap() {
            TestRecord record = new TestRecord("test", 25);
            Map<String, Object> map = OpenRecord.toMap(record);
            assertThat(map).containsEntry("name", "test");
            assertThat(map).containsEntry("age", 25);
        }
    }

    @Nested
    @DisplayName("builder方法测试")
    class BuilderTests {

        @Test
        @DisplayName("创建record构建器")
        void testBuilder() {
            RecordBuilder<TestRecord> builder = OpenRecord.builder(TestRecord.class);
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("create方法测试")
    class CreateTests {

        @Test
        @DisplayName("从值创建record")
        void testCreate() {
            TestRecord record = OpenRecord.create(TestRecord.class, "test", 30);
            assertThat(record.name()).isEqualTo("test");
            assertThat(record.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("值数量不匹配抛出异常")
        void testCreateWrongValueCount() {
            assertThatThrownBy(() -> OpenRecord.create(TestRecord.class, "test"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("fromMap方法测试")
    class FromMapTests {

        @Test
        @DisplayName("从map创建record")
        void testFromMap() {
            Map<String, Object> map = Map.of("name", "test", "age", 30);
            TestRecord record = OpenRecord.fromMap(TestRecord.class, map);
            assertThat(record.name()).isEqualTo("test");
            assertThat(record.age()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("copy方法测试")
    class CopyTests {

        @Test
        @DisplayName("复制record")
        void testCopy() {
            TestRecord original = new TestRecord("test", 25);
            TestRecord copy = OpenRecord.copy(original);
            assertThat(copy).isEqualTo(original);
            assertThat(copy).isNotSameAs(original);
        }
    }

    @Nested
    @DisplayName("copyWith方法测试")
    class CopyWithTests {

        @Test
        @DisplayName("带修改值复制")
        void testCopyWithMap() {
            TestRecord original = new TestRecord("test", 25);
            TestRecord modified = OpenRecord.copyWith(original, Map.of("age", 30));
            assertThat(modified.name()).isEqualTo("test");
            assertThat(modified.age()).isEqualTo(30);
        }

        @Test
        @DisplayName("带单个修改值复制")
        void testCopyWithSingleValue() {
            TestRecord original = new TestRecord("test", 25);
            TestRecord modified = OpenRecord.copyWith(original, "name", "newName");
            assertThat(modified.name()).isEqualTo("newName");
            assertThat(modified.age()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("diff方法测试")
    class DiffTests {

        @Test
        @DisplayName("比较两个record")
        void testDiff() {
            TestRecord r1 = new TestRecord("test", 25);
            TestRecord r2 = new TestRecord("test", 30);
            Map<String, Object[]> diff = OpenRecord.diff(r1, r2);
            assertThat(diff).containsKey("age");
            assertThat(diff.get("age")).containsExactly(25, 30);
        }

        @Test
        @DisplayName("相同record无差异")
        void testDiffNoDifferences() {
            TestRecord r1 = new TestRecord("test", 25);
            TestRecord r2 = new TestRecord("test", 25);
            Map<String, Object[]> diff = OpenRecord.diff(r1, r2);
            assertThat(diff).isEmpty();
        }

        @Test
        @DisplayName("不同类型record抛出异常")
        void testDiffDifferentTypes() {
            TestRecord r1 = new TestRecord("test", 25);
            OtherRecord r2 = new OtherRecord("test");
            assertThatThrownBy(() -> OpenRecord.diff(r1, r2))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("valuesEqual方法测试")
    class ValuesEqualTests {

        @Test
        @DisplayName("相同值返回true")
        void testValuesEqualTrue() {
            TestRecord r1 = new TestRecord("test", 25);
            TestRecord r2 = new TestRecord("test", 25);
            assertThat(OpenRecord.valuesEqual(r1, r2)).isTrue();
        }

        @Test
        @DisplayName("不同值返回false")
        void testValuesEqualFalse() {
            TestRecord r1 = new TestRecord("test", 25);
            TestRecord r2 = new TestRecord("test", 30);
            assertThat(OpenRecord.valuesEqual(r1, r2)).isFalse();
        }
    }

    // Test helper records
    record TestRecord(String name, int age) {}
    record OtherRecord(String value) {}
}
