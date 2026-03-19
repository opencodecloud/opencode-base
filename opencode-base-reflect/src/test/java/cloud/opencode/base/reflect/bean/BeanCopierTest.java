package cloud.opencode.base.reflect.bean;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * BeanCopierTest Tests
 * BeanCopierTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("BeanCopier 测试")
class BeanCopierTest {

    @Nested
    @DisplayName("builder方法测试")
    class BuilderTests {

        @Test
        @DisplayName("创建构建器")
        void testBuilder() {
            BeanCopier.Builder<Source, Target> builder = BeanCopier.builder(Source.class, Target.class);
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("create方法测试")
    class CreateTests {

        @Test
        @DisplayName("创建简单复制器")
        void testCreate() {
            BeanCopier<Source, Target> copier = BeanCopier.create(Source.class, Target.class);
            assertThat(copier).isNotNull();
        }
    }

    @Nested
    @DisplayName("copy方法测试")
    class CopyTests {

        @Test
        @DisplayName("复制属性到目标")
        void testCopyToTarget() {
            BeanCopier<Source, Target> copier = BeanCopier.create(Source.class, Target.class);
            Source source = new Source();
            source.setName("John");
            source.setAge(25);

            Target target = new Target();
            copier.copy(source, target);

            assertThat(target.getName()).isEqualTo("John");
            assertThat(target.getAge()).isEqualTo(25);
        }

        @Test
        @DisplayName("创建新实例并复制")
        void testCopyAndCreate() {
            BeanCopier<Source, Target> copier = BeanCopier.create(Source.class, Target.class);
            Source source = new Source();
            source.setName("John");
            source.setAge(25);

            Target target = copier.copy(source);

            assertThat(target.getName()).isEqualTo("John");
            assertThat(target.getAge()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("copyList方法测试")
    class CopyListTests {

        @Test
        @DisplayName("复制列表")
        void testCopyList() {
            BeanCopier<Source, Target> copier = BeanCopier.create(Source.class, Target.class);
            Source s1 = new Source();
            s1.setName("John");
            Source s2 = new Source();
            s2.setName("Jane");

            List<Target> targets = copier.copyList(List.of(s1, s2));

            assertThat(targets).hasSize(2);
            assertThat(targets.get(0).getName()).isEqualTo("John");
            assertThat(targets.get(1).getName()).isEqualTo("Jane");
        }
    }

    @Nested
    @DisplayName("map方法测试")
    class MapTests {

        @Test
        @DisplayName("映射不同属性名")
        void testMap() {
            BeanCopier<Source, DifferentTarget> copier = BeanCopier.builder(Source.class, DifferentTarget.class)
                    .map("name", "fullName")
                    .build();

            Source source = new Source();
            source.setName("John");

            DifferentTarget target = copier.copy(source);
            assertThat(target.getFullName()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("convert方法测试")
    class ConvertTests {

        @Test
        @DisplayName("使用转换器")
        void testConvert() {
            BeanCopier<Source, Target> copier = BeanCopier.builder(Source.class, Target.class)
                    .convert("name", v -> ((String) v).toUpperCase())
                    .build();

            Source source = new Source();
            source.setName("John");

            Target target = copier.copy(source);
            assertThat(target.getName()).isEqualTo("JOHN");
        }
    }

    @Nested
    @DisplayName("exclude方法测试")
    class ExcludeTests {

        @Test
        @DisplayName("排除属性")
        void testExclude() {
            BeanCopier<Source, Target> copier = BeanCopier.builder(Source.class, Target.class)
                    .exclude("age")
                    .build();

            Source source = new Source();
            source.setName("John");
            source.setAge(25);

            Target target = new Target();
            target.setAge(0);
            copier.copy(source, target);

            assertThat(target.getName()).isEqualTo("John");
            assertThat(target.getAge()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("ignoreNulls方法测试")
    class IgnoreNullsTests {

        @Test
        @DisplayName("忽略null值")
        void testIgnoreNulls() {
            BeanCopier<Source, Target> copier = BeanCopier.builder(Source.class, Target.class)
                    .ignoreNulls(true)
                    .build();

            Source source = new Source();
            source.setName(null);
            source.setAge(25);

            Target target = new Target();
            target.setName("existing");
            copier.copy(source, target);

            assertThat(target.getName()).isEqualTo("existing");
            assertThat(target.getAge()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("when方法测试")
    class WhenTests {

        @Test
        @DisplayName("条件复制")
        void testWhen() {
            BeanCopier<Source, Target> copier = BeanCopier.builder(Source.class, Target.class)
                    .when((name, value) -> value != null)
                    .build();

            Source source = new Source();
            source.setName(null);
            source.setAge(25);

            Target target = new Target();
            target.setName("existing");
            copier.copy(source, target);

            assertThat(target.getName()).isEqualTo("existing");
            assertThat(target.getAge()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("getSourceClass方法测试")
    class GetSourceClassTests {

        @Test
        @DisplayName("获取源类")
        void testGetSourceClass() {
            BeanCopier<Source, Target> copier = BeanCopier.create(Source.class, Target.class);
            assertThat(copier.getSourceClass()).isEqualTo(Source.class);
        }
    }

    @Nested
    @DisplayName("getTargetClass方法测试")
    class GetTargetClassTests {

        @Test
        @DisplayName("获取目标类")
        void testGetTargetClass() {
            BeanCopier<Source, Target> copier = BeanCopier.create(Source.class, Target.class);
            assertThat(copier.getTargetClass()).isEqualTo(Target.class);
        }
    }

    // Test helper classes
    public static class Source {
        private String name;
        private int age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    public static class Target {
        private String name;
        private int age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    public static class DifferentTarget {
        private String fullName;
        private int age;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
}
