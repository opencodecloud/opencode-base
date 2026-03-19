package cloud.opencode.base.deepclone.handler;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.Cloner;
import cloud.opencode.base.deepclone.OpenClone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * RecordHandler 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("RecordHandler 测试")
class RecordHandlerTest {

    private RecordHandler handler;
    private Cloner cloner;
    private CloneContext context;

    @BeforeEach
    void setUp() {
        handler = new RecordHandler();
        cloner = OpenClone.getDefaultCloner();
        context = CloneContext.create();
    }

    // Test records
    public record SimpleRecord(String name, int value) {}

    public record NestedRecord(String id, SimpleRecord inner) {}

    public record RecordWithList(String name, List<String> items) {}

    public record EmptyRecord() {}

    public record ManyFieldsRecord(String a, int b, double c, boolean d, String e) {}

    // Non-record class for testing
    public static class NotARecord {
        private String name;
        public NotARecord(String name) { this.name = name; }
    }

    @Nested
    @DisplayName("clone() 测试")
    class CloneTests {

        @Test
        @DisplayName("克隆null返回null")
        void testCloneNull() {
            Record result = handler.clone(null, cloner, context);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("克隆简单Record")
        void testCloneSimpleRecord() {
            SimpleRecord original = new SimpleRecord("test", 123);

            Record cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(SimpleRecord.class);

            SimpleRecord clonedRecord = (SimpleRecord) cloned;
            assertThat(clonedRecord.name()).isEqualTo("test");
            assertThat(clonedRecord.value()).isEqualTo(123);
        }

        @Test
        @DisplayName("克隆嵌套Record")
        void testCloneNestedRecord() {
            SimpleRecord inner = new SimpleRecord("inner", 100);
            NestedRecord original = new NestedRecord("outer", inner);

            NestedRecord cloned = (NestedRecord) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.inner()).isNotSameAs(original.inner());
            assertThat(cloned.inner().name()).isEqualTo("inner");
        }

        @Test
        @DisplayName("克隆空Record")
        void testCloneEmptyRecord() {
            EmptyRecord original = new EmptyRecord();

            EmptyRecord cloned = (EmptyRecord) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
        }

        @Test
        @DisplayName("克隆多字段Record")
        void testCloneManyFieldsRecord() {
            ManyFieldsRecord original = new ManyFieldsRecord("a", 1, 2.0, true, "e");

            ManyFieldsRecord cloned = (ManyFieldsRecord) handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.a()).isEqualTo("a");
            assertThat(cloned.b()).isEqualTo(1);
            assertThat(cloned.c()).isEqualTo(2.0);
            assertThat(cloned.d()).isTrue();
            assertThat(cloned.e()).isEqualTo("e");
        }
    }

    @Nested
    @DisplayName("cloneRecord() 测试")
    class CloneRecordTests {

        @Test
        @DisplayName("克隆Record泛型方法")
        void testCloneRecordGeneric() {
            SimpleRecord original = new SimpleRecord("test", 123);

            SimpleRecord cloned = handler.cloneRecord(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.name()).isEqualTo("test");
            assertThat(cloned.value()).isEqualTo(123);
        }

        @Test
        @DisplayName("克隆null Record返回null")
        void testCloneRecordNull() {
            SimpleRecord cloned = handler.cloneRecord(null, cloner, context);
            assertThat(cloned).isNull();
        }
    }

    @Nested
    @DisplayName("getComponents() 测试")
    class GetComponentsTests {

        @Test
        @DisplayName("获取Record组件值")
        void testGetComponents() {
            SimpleRecord record = new SimpleRecord("test", 123);

            Object[] components = handler.getComponents(record);

            assertThat(components).hasSize(2);
            assertThat(components[0]).isEqualTo("test");
            assertThat(components[1]).isEqualTo(123);
        }

        @Test
        @DisplayName("获取空Record组件值")
        void testGetComponentsEmpty() {
            EmptyRecord record = new EmptyRecord();

            Object[] components = handler.getComponents(record);

            assertThat(components).isEmpty();
        }

        @Test
        @DisplayName("获取多字段Record组件值")
        void testGetComponentsManyFields() {
            ManyFieldsRecord record = new ManyFieldsRecord("a", 1, 2.0, true, "e");

            Object[] components = handler.getComponents(record);

            assertThat(components).hasSize(5);
            assertThat(components[0]).isEqualTo("a");
            assertThat(components[1]).isEqualTo(1);
            assertThat(components[2]).isEqualTo(2.0);
            assertThat(components[3]).isEqualTo(true);
            assertThat(components[4]).isEqualTo("e");
        }
    }

    @Nested
    @DisplayName("createInstance() 测试")
    class CreateInstanceTests {

        @Test
        @DisplayName("使用值创建Record实例")
        void testCreateInstance() {
            Object[] values = {"created", 456};

            SimpleRecord instance = handler.createInstance(SimpleRecord.class, values);

            assertThat(instance.name()).isEqualTo("created");
            assertThat(instance.value()).isEqualTo(456);
        }

        @Test
        @DisplayName("创建空Record实例")
        void testCreateInstanceEmpty() {
            Object[] values = {};

            EmptyRecord instance = handler.createInstance(EmptyRecord.class, values);

            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("创建多字段Record实例")
        void testCreateInstanceManyFields() {
            Object[] values = {"x", 10, 20.0, false, "y"};

            ManyFieldsRecord instance = handler.createInstance(ManyFieldsRecord.class, values);

            assertThat(instance.a()).isEqualTo("x");
            assertThat(instance.b()).isEqualTo(10);
            assertThat(instance.c()).isEqualTo(20.0);
            assertThat(instance.d()).isFalse();
            assertThat(instance.e()).isEqualTo("y");
        }
    }

    @Nested
    @DisplayName("supports() 测试")
    class SupportsTests {

        @Test
        @DisplayName("支持Record类型")
        void testSupportsRecord() {
            assertThat(handler.supports(SimpleRecord.class)).isTrue();
            assertThat(handler.supports(NestedRecord.class)).isTrue();
            assertThat(handler.supports(EmptyRecord.class)).isTrue();
        }

        @Test
        @DisplayName("不支持非Record类型")
        void testNotSupportsNonRecord() {
            assertThat(handler.supports(String.class)).isFalse();
            assertThat(handler.supports(NotARecord.class)).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testSupportsNull() {
            assertThat(handler.supports(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("priority() 测试")
    class PriorityTests {

        @Test
        @DisplayName("优先级为15")
        void testPriority() {
            assertThat(handler.priority()).isEqualTo(15);
        }
    }
}
