package cloud.opencode.base.reflect.record;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * RecordMapper Tests
 * RecordMapper 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.3
 */
@DisplayName("RecordMapper 测试")
class RecordMapperTest {

    // ==================== Test Types | 测试类型 ====================

    record SourceRecord(String name, int age, String email) {}

    record TargetRecord(String name, int age, String email) {}

    record RenamedRecord(String fullName, int years, String contact) {}

    record PartialRecord(String name, int age) {}

    record ExtraFieldRecord(String name, int age, String email, String address) {}

    static class SourceBean {
        private String name;
        private int age;
        private String email;

        public SourceBean() {}

        public SourceBean(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    static class TargetBean {
        private String name;
        private int age;
        private String email;

        public TargetBean() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // ==================== Record to Record Tests | Record到Record测试 ====================

    @Nested
    @DisplayName("Record到Record映射测试")
    class RecordToRecordTests {

        @Test
        @DisplayName("相同字段Record映射")
        void testSameFieldMapping() {
            RecordMapper<SourceRecord, TargetRecord> mapper =
                    RecordMapper.builder(SourceRecord.class, TargetRecord.class).build();

            SourceRecord source = new SourceRecord("Alice", 30, "alice@test.com");
            TargetRecord target = mapper.map(source);

            assertThat(target.name()).isEqualTo("Alice");
            assertThat(target.age()).isEqualTo(30);
            assertThat(target.email()).isEqualTo("alice@test.com");
        }

        @Test
        @DisplayName("字段重命名映射")
        void testFieldRenaming() {
            RecordMapper<SourceRecord, RenamedRecord> mapper =
                    RecordMapper.builder(SourceRecord.class, RenamedRecord.class)
                            .map("name", "fullName")
                            .map("age", "years")
                            .map("email", "contact")
                            .build();

            SourceRecord source = new SourceRecord("Bob", 25, "bob@test.com");
            RenamedRecord target = mapper.map(source);

            assertThat(target.fullName()).isEqualTo("Bob");
            assertThat(target.years()).isEqualTo(25);
            assertThat(target.contact()).isEqualTo("bob@test.com");
        }

        @Test
        @DisplayName("目标字段多于源字段时的优雅处理")
        void testExtraTargetFields() {
            RecordMapper<PartialRecord, TargetRecord> mapper =
                    RecordMapper.builder(PartialRecord.class, TargetRecord.class).build();

            PartialRecord source = new PartialRecord("Charlie", 35);
            TargetRecord target = mapper.map(source);

            assertThat(target.name()).isEqualTo("Charlie");
            assertThat(target.age()).isEqualTo(35);
            assertThat(target.email()).isNull();
        }

        @Test
        @DisplayName("源字段多于目标字段时的优雅处理")
        void testExtraSourceFields() {
            RecordMapper<SourceRecord, PartialRecord> mapper =
                    RecordMapper.builder(SourceRecord.class, PartialRecord.class).build();

            SourceRecord source = new SourceRecord("Diana", 28, "diana@test.com");
            PartialRecord target = mapper.map(source);

            assertThat(target.name()).isEqualTo("Diana");
            assertThat(target.age()).isEqualTo(28);
        }
    }

    // ==================== Record to Bean Tests | Record到Bean测试 ====================

    @Nested
    @DisplayName("Record到Bean映射测试")
    class RecordToBeanTests {

        @Test
        @DisplayName("Record到Bean映射")
        void testRecordToBean() {
            RecordMapper<SourceRecord, TargetBean> mapper =
                    RecordMapper.builder(SourceRecord.class, TargetBean.class).build();

            SourceRecord source = new SourceRecord("Eve", 22, "eve@test.com");
            TargetBean target = mapper.map(source);

            assertThat(target.getName()).isEqualTo("Eve");
            assertThat(target.getAge()).isEqualTo(22);
            assertThat(target.getEmail()).isEqualTo("eve@test.com");
        }
    }

    // ==================== Bean to Record Tests | Bean到Record测试 ====================

    @Nested
    @DisplayName("Bean到Record映射测试")
    class BeanToRecordTests {

        @Test
        @DisplayName("Bean到Record映射")
        void testBeanToRecord() {
            RecordMapper<SourceBean, TargetRecord> mapper =
                    RecordMapper.builder(SourceBean.class, TargetRecord.class).build();

            SourceBean source = new SourceBean("Frank", 40, "frank@test.com");
            TargetRecord target = mapper.map(source);

            assertThat(target.name()).isEqualTo("Frank");
            assertThat(target.age()).isEqualTo(40);
            assertThat(target.email()).isEqualTo("frank@test.com");
        }
    }

    // ==================== Exclusion Tests | 排除测试 ====================

    @Nested
    @DisplayName("字段排除测试")
    class ExclusionTests {

        @Test
        @DisplayName("排除单个字段")
        void testExcludeSingleField() {
            RecordMapper<SourceRecord, TargetRecord> mapper =
                    RecordMapper.builder(SourceRecord.class, TargetRecord.class)
                            .exclude("email")
                            .build();

            SourceRecord source = new SourceRecord("Grace", 33, "grace@test.com");
            TargetRecord target = mapper.map(source);

            assertThat(target.name()).isEqualTo("Grace");
            assertThat(target.age()).isEqualTo(33);
            assertThat(target.email()).isNull();
        }

        @Test
        @DisplayName("排除多个字段")
        void testExcludeMultipleFields() {
            RecordMapper<SourceRecord, TargetRecord> mapper =
                    RecordMapper.builder(SourceRecord.class, TargetRecord.class)
                            .exclude("age")
                            .exclude("email")
                            .build();

            SourceRecord source = new SourceRecord("Helen", 27, "helen@test.com");
            TargetRecord target = mapper.map(source);

            assertThat(target.name()).isEqualTo("Helen");
            assertThat(target.age()).isEqualTo(0); // default for int
            assertThat(target.email()).isNull();
        }
    }

    // ==================== Converter Tests | 转换器测试 ====================

    @Nested
    @DisplayName("自定义转换器测试")
    class ConverterTests {

        @Test
        @DisplayName("对目标字段应用转换器")
        void testCustomConverter() {
            RecordMapper<SourceRecord, TargetRecord> mapper =
                    RecordMapper.builder(SourceRecord.class, TargetRecord.class)
                            .convert("name", v -> ((String) v).toUpperCase())
                            .build();

            SourceRecord source = new SourceRecord("ivan", 45, "ivan@test.com");
            TargetRecord target = mapper.map(source);

            assertThat(target.name()).isEqualTo("IVAN");
            assertThat(target.age()).isEqualTo(45);
            assertThat(target.email()).isEqualTo("ivan@test.com");
        }

        @Test
        @DisplayName("带重命名的转换器")
        void testConverterWithRenaming() {
            RecordMapper<SourceRecord, RenamedRecord> mapper =
                    RecordMapper.builder(SourceRecord.class, RenamedRecord.class)
                            .map("name", "fullName")
                            .map("age", "years")
                            .map("email", "contact")
                            .convert("years", v -> ((int) v) + 1)
                            .build();

            SourceRecord source = new SourceRecord("Jack", 29, "jack@test.com");
            RenamedRecord target = mapper.map(source);

            assertThat(target.fullName()).isEqualTo("Jack");
            assertThat(target.years()).isEqualTo(30);
            assertThat(target.contact()).isEqualTo("jack@test.com");
        }
    }

    // ==================== IgnoreNulls Tests | 忽略空值测试 ====================

    @Nested
    @DisplayName("忽略空值测试")
    class IgnoreNullsTests {

        @Test
        @DisplayName("ignoreNulls=true时跳过null值")
        void testIgnoreNullsTrue() {
            RecordMapper<SourceRecord, TargetRecord> mapper =
                    RecordMapper.builder(SourceRecord.class, TargetRecord.class)
                            .ignoreNulls(true)
                            .build();

            SourceRecord source = new SourceRecord(null, 50, "kate@test.com");
            TargetRecord target = mapper.map(source);

            assertThat(target.name()).isNull(); // record default for reference type
            assertThat(target.age()).isEqualTo(50);
            assertThat(target.email()).isEqualTo("kate@test.com");
        }

        @Test
        @DisplayName("ignoreNulls=false时保留null值")
        void testIgnoreNullsFalse() {
            RecordMapper<SourceRecord, TargetRecord> mapper =
                    RecordMapper.builder(SourceRecord.class, TargetRecord.class)
                            .ignoreNulls(false)
                            .build();

            SourceRecord source = new SourceRecord(null, 50, null);
            TargetRecord target = mapper.map(source);

            assertThat(target.name()).isNull();
            assertThat(target.age()).isEqualTo(50);
            assertThat(target.email()).isNull();
        }

        @Test
        @DisplayName("ignoreNulls=true时Bean目标跳过null值")
        void testIgnoreNullsBeanTarget() {
            RecordMapper<SourceRecord, TargetBean> mapper =
                    RecordMapper.builder(SourceRecord.class, TargetBean.class)
                            .ignoreNulls(true)
                            .build();

            SourceRecord source = new SourceRecord(null, 20, "test@test.com");
            TargetBean target = mapper.map(source);

            assertThat(target.getName()).isNull(); // not set because ignoreNulls
            assertThat(target.getAge()).isEqualTo(20);
            assertThat(target.getEmail()).isEqualTo("test@test.com");
        }
    }

    // ==================== Batch Tests | 批量测试 ====================

    @Nested
    @DisplayName("批量映射测试")
    class BatchTests {

        @Test
        @DisplayName("mapAll批量映射")
        void testMapAll() {
            RecordMapper<SourceRecord, TargetRecord> mapper =
                    RecordMapper.builder(SourceRecord.class, TargetRecord.class).build();

            List<SourceRecord> sources = Arrays.asList(
                    new SourceRecord("Leo", 20, "leo@test.com"),
                    new SourceRecord("Mia", 25, "mia@test.com"),
                    new SourceRecord("Nick", 30, "nick@test.com")
            );

            List<TargetRecord> targets = mapper.mapAll(sources);

            assertThat(targets).hasSize(3);
            assertThat(targets.get(0).name()).isEqualTo("Leo");
            assertThat(targets.get(1).name()).isEqualTo("Mia");
            assertThat(targets.get(2).name()).isEqualTo("Nick");
        }

        @Test
        @DisplayName("mapAll空集合")
        void testMapAllEmpty() {
            RecordMapper<SourceRecord, TargetRecord> mapper =
                    RecordMapper.builder(SourceRecord.class, TargetRecord.class).build();

            List<TargetRecord> targets = mapper.mapAll(List.of());

            assertThat(targets).isEmpty();
        }
    }

    // ==================== Edge Case Tests | 边界测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("source为null时抛出NullPointerException")
        void testNullSource() {
            RecordMapper<SourceRecord, TargetRecord> mapper =
                    RecordMapper.builder(SourceRecord.class, TargetRecord.class).build();

            assertThatThrownBy(() -> mapper.map(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("source");
        }

        @Test
        @DisplayName("sources集合为null时抛出NullPointerException")
        void testNullSources() {
            RecordMapper<SourceRecord, TargetRecord> mapper =
                    RecordMapper.builder(SourceRecord.class, TargetRecord.class).build();

            assertThatThrownBy(() -> mapper.mapAll(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("sources");
        }

        @Test
        @DisplayName("builder参数校验")
        void testBuilderNullArgs() {
            assertThatThrownBy(() -> RecordMapper.builder(null, TargetRecord.class))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> RecordMapper.builder(SourceRecord.class, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("目标字段在源中不存在时使用默认值")
        void testMissingSourceFields() {
            RecordMapper<PartialRecord, TargetRecord> mapper =
                    RecordMapper.builder(PartialRecord.class, TargetRecord.class).build();

            PartialRecord source = new PartialRecord("Oscar", 42);
            TargetRecord target = mapper.map(source);

            assertThat(target.name()).isEqualTo("Oscar");
            assertThat(target.age()).isEqualTo(42);
            assertThat(target.email()).isNull();
        }

        @Test
        @DisplayName("Bean到Bean映射")
        void testBeanToBean() {
            RecordMapper<SourceBean, TargetBean> mapper =
                    RecordMapper.builder(SourceBean.class, TargetBean.class).build();

            SourceBean source = new SourceBean("Pete", 55, "pete@test.com");
            TargetBean target = mapper.map(source);

            assertThat(target.getName()).isEqualTo("Pete");
            assertThat(target.getAge()).isEqualTo(55);
            assertThat(target.getEmail()).isEqualTo("pete@test.com");
        }
    }
}
