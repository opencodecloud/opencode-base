package cloud.opencode.base.reflect.record;

import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * RecordUtilTest Tests
 * RecordUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("RecordUtil 测试")
class RecordUtilTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = RecordUtil.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("isRecord方法测试")
    class IsRecordTests {

        @Test
        @DisplayName("record类返回true")
        void testIsRecordTrue() {
            assertThat(RecordUtil.isRecord(TestRecord.class)).isTrue();
        }

        @Test
        @DisplayName("非record类返回false")
        void testIsRecordFalse() {
            assertThat(RecordUtil.isRecord(String.class)).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testIsRecordNull() {
            assertThat(RecordUtil.isRecord(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isRecordInstance方法测试")
    class IsRecordInstanceTests {

        @Test
        @DisplayName("record实例返回true")
        void testIsRecordInstanceTrue() {
            assertThat(RecordUtil.isRecordInstance(new TestRecord("test", 25))).isTrue();
        }

        @Test
        @DisplayName("非record实例返回false")
        void testIsRecordInstanceFalse() {
            assertThat(RecordUtil.isRecordInstance("test")).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testIsRecordInstanceNull() {
            assertThat(RecordUtil.isRecordInstance(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("requireRecord方法测试")
    class RequireRecordTests {

        @Test
        @DisplayName("record类不抛异常")
        void testRequireRecordSuccess() {
            assertThatCode(() -> RecordUtil.requireRecord(TestRecord.class)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("非record类抛出异常")
        void testRequireRecordFailure() {
            assertThatThrownBy(() -> RecordUtil.requireRecord(String.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getRecordComponents方法测试")
    class GetRecordComponentsTests {

        @Test
        @DisplayName("获取record组件")
        void testGetRecordComponents() {
            var components = RecordUtil.getRecordComponents(TestRecord.class);
            assertThat(components).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getComponentCount方法测试")
    class GetComponentCountTests {

        @Test
        @DisplayName("获取组件数量")
        void testGetComponentCount() {
            assertThat(RecordUtil.getComponentCount(TestRecord.class)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getComponentNames方法测试")
    class GetComponentNamesTests {

        @Test
        @DisplayName("获取组件名称")
        void testGetComponentNames() {
            List<String> names = RecordUtil.getComponentNames(TestRecord.class);
            assertThat(names).containsExactly("name", "age");
        }
    }

    @Nested
    @DisplayName("getComponentTypes方法测试")
    class GetComponentTypesTests {

        @Test
        @DisplayName("获取组件类型")
        void testGetComponentTypes() {
            Class<?>[] types = RecordUtil.getComponentTypes(TestRecord.class);
            assertThat(types).containsExactly(String.class, int.class);
        }
    }

    @Nested
    @DisplayName("getComponent按名称方法测试")
    class GetComponentByNameTests {

        @Test
        @DisplayName("按名称获取组件")
        void testGetComponentByName() {
            var component = RecordUtil.getComponent(TestRecord.class, "name");
            assertThat(component).isNotNull();
            assertThat(component.getName()).isEqualTo("name");
        }

        @Test
        @DisplayName("不存在的组件返回null")
        void testGetComponentByNameNotFound() {
            var component = RecordUtil.getComponent(TestRecord.class, "nonexistent");
            assertThat(component).isNull();
        }
    }

    @Nested
    @DisplayName("getComponent按索引方法测试")
    class GetComponentByIndexTests {

        @Test
        @DisplayName("按索引获取组件")
        void testGetComponentByIndex() {
            var component = RecordUtil.getComponent(TestRecord.class, 0);
            assertThat(component.getName()).isEqualTo("name");
        }

        @Test
        @DisplayName("索引越界抛出异常")
        void testGetComponentByIndexOutOfBounds() {
            assertThatThrownBy(() -> RecordUtil.getComponent(TestRecord.class, 10))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("getAccessor方法测试")
    class GetAccessorTests {

        @Test
        @DisplayName("获取访问器方法")
        void testGetAccessor() {
            Method accessor = RecordUtil.getAccessor(TestRecord.class, "name");
            assertThat(accessor).isNotNull();
            assertThat(accessor.getName()).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("getAccessors方法测试")
    class GetAccessorsTests {

        @Test
        @DisplayName("获取所有访问器方法")
        void testGetAccessors() {
            List<Method> accessors = RecordUtil.getAccessors(TestRecord.class);
            assertThat(accessors).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getComponentValue方法测试")
    class GetComponentValueTests {

        @Test
        @DisplayName("获取组件值")
        void testGetComponentValue() {
            TestRecord record = new TestRecord("test", 25);
            Object value = RecordUtil.getComponentValue(record, "name");
            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("获取组件值带类型")
        void testGetComponentValueTyped() {
            TestRecord record = new TestRecord("test", 25);
            String value = RecordUtil.getComponentValue(record, "name", String.class);
            assertThat(value).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("getComponentValues方法测试")
    class GetComponentValuesTests {

        @Test
        @DisplayName("获取所有组件值")
        void testGetComponentValues() {
            TestRecord record = new TestRecord("test", 25);
            Object[] values = RecordUtil.getComponentValues(record);
            assertThat(values).containsExactly("test", 25);
        }
    }

    @Nested
    @DisplayName("getCanonicalConstructor方法测试")
    class GetCanonicalConstructorTests {

        @Test
        @DisplayName("获取规范构造器")
        void testGetCanonicalConstructor() {
            Constructor<TestRecord> constructor = RecordUtil.getCanonicalConstructor(TestRecord.class);
            assertThat(constructor).isNotNull();
            assertThat(constructor.getParameterCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("newInstance方法测试")
    class NewInstanceTests {

        @Test
        @DisplayName("创建新record实例")
        void testNewInstance() {
            TestRecord record = RecordUtil.newInstance(TestRecord.class, "test", 30);
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
            TestRecord copy = RecordUtil.copy(original);
            assertThat(copy).isEqualTo(original);
            assertThat(copy).isNotSameAs(original);
        }
    }

    @Nested
    @DisplayName("toMap方法测试")
    class ToMapTests {

        @Test
        @DisplayName("将record转换为map")
        void testToMap() {
            TestRecord record = new TestRecord("test", 25);
            Map<String, Object> map = RecordUtil.toMap(record);
            assertThat(map).containsEntry("name", "test");
            assertThat(map).containsEntry("age", 25);
        }
    }

    @Nested
    @DisplayName("fromMap方法测试")
    class FromMapTests {

        @Test
        @DisplayName("从map创建record")
        void testFromMap() {
            Map<String, Object> map = Map.of("name", "test", "age", 30);
            TestRecord record = RecordUtil.fromMap(TestRecord.class, map);
            assertThat(record.name()).isEqualTo("test");
            assertThat(record.age()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("缓存管理测试")
    class CacheManagementTests {

        @Test
        @DisplayName("清除缓存")
        void testClearCache() {
            RecordUtil.getRecordComponents(TestRecord.class);
            assertThatCode(() -> RecordUtil.clearCache()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("清除特定类的缓存")
        void testClearCacheForClass() {
            RecordUtil.getRecordComponents(TestRecord.class);
            assertThatCode(() -> RecordUtil.clearCache(TestRecord.class)).doesNotThrowAnyException();
        }
    }

    // Test helper record
    record TestRecord(String name, int age) {}
}
