package cloud.opencode.base.core.builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * BeanBuilder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("BeanBuilder 测试")
class BeanBuilderTest {

    @Nested
    @DisplayName("of 静态方法测试")
    class OfTests {

        @Test
        @DisplayName("of 创建构建器")
        void testOf() {
            BeanBuilder<TestBean> builder = BeanBuilder.of(TestBean.class);
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("of 返回类型正确")
        void testOfReturnsCorrectType() {
            BeanBuilder<TestBean> builder = BeanBuilder.of(TestBean.class);
            assertThat(builder).isInstanceOf(BeanBuilder.class);
        }
    }

    @Nested
    @DisplayName("from 静态方法测试")
    class FromTests {

        @Test
        @DisplayName("from 复制现有实例")
        void testFrom() {
            TestBean source = new TestBean();
            source.setName("Original");
            source.setAge(30);

            TestBean copy = BeanBuilder.from(source).build();

            assertThat(copy.getName()).isEqualTo("Original");
            assertThat(copy.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("from 允许修改")
        void testFromWithModification() {
            TestBean source = new TestBean();
            source.setName("Original");
            source.setAge(30);

            TestBean modified = BeanBuilder.from(source)
                    .set("name", "Modified")
                    .build();

            assertThat(modified.getName()).isEqualTo("Modified");
            assertThat(modified.getAge()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("set 测试")
    class SetTests {

        @Test
        @DisplayName("set 设置属性")
        void testSet() {
            TestBean bean = BeanBuilder.of(TestBean.class)
                    .set("name", "John")
                    .set("age", 25)
                    .build();

            assertThat(bean.getName()).isEqualTo("John");
            assertThat(bean.getAge()).isEqualTo(25);
        }

        @Test
        @DisplayName("set 链式调用")
        void testSetChaining() {
            BeanBuilder<TestBean> builder = BeanBuilder.of(TestBean.class);

            BeanBuilder<TestBean> result = builder
                    .set("name", "John")
                    .set("age", 25);

            assertThat(result).isSameAs(builder);
        }

        @Test
        @DisplayName("set 类型转换")
        void testSetTypeConversion() {
            TestBean bean = BeanBuilder.of(TestBean.class)
                    .set("age", "25")
                    .build();

            assertThat(bean.getAge()).isEqualTo(25);
        }

        @Test
        @DisplayName("set 不存在的属性忽略")
        void testSetNonExistentProperty() {
            TestBean bean = BeanBuilder.of(TestBean.class)
                    .set("nonExistent", "value")
                    .set("name", "John")
                    .build();

            assertThat(bean.getName()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("setIfNotNull 测试")
    class SetIfNotNullTests {

        @Test
        @DisplayName("setIfNotNull 非 null 时设置")
        void testSetIfNotNullWithValue() {
            TestBean bean = BeanBuilder.of(TestBean.class)
                    .setIfNotNull("name", "John")
                    .build();

            assertThat(bean.getName()).isEqualTo("John");
        }

        @Test
        @DisplayName("setIfNotNull null 时不设置")
        void testSetIfNotNullWithNull() {
            TestBean bean = BeanBuilder.of(TestBean.class)
                    .set("name", "Default")
                    .setIfNotNull("name", null)
                    .build();

            assertThat(bean.getName()).isEqualTo("Default");
        }
    }

    @Nested
    @DisplayName("setIf 测试")
    class SetIfTests {

        @Test
        @DisplayName("setIf 条件为 true 时设置")
        void testSetIfTrue() {
            TestBean bean = BeanBuilder.of(TestBean.class)
                    .setIf(true, "name", "John")
                    .build();

            assertThat(bean.getName()).isEqualTo("John");
        }

        @Test
        @DisplayName("setIf 条件为 false 时不设置")
        void testSetIfFalse() {
            TestBean bean = BeanBuilder.of(TestBean.class)
                    .set("name", "Default")
                    .setIf(false, "name", "John")
                    .build();

            assertThat(bean.getName()).isEqualTo("Default");
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

            TestBean bean = BeanBuilder.of(TestBean.class)
                    .setAll(props)
                    .build();

            assertThat(bean.getName()).isEqualTo("John");
            assertThat(bean.getAge()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("configure 测试")
    class ConfigureTests {

        @Test
        @DisplayName("configure 回调配置")
        void testConfigure() {
            TestBean bean = BeanBuilder.of(TestBean.class)
                    .configure(builder -> {
                        builder.set("name", "John");
                        builder.set("age", 25);
                    })
                    .build();

            assertThat(bean.getName()).isEqualTo("John");
            assertThat(bean.getAge()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("build 测试")
    class BuildTests {

        @Test
        @DisplayName("build 返回新实例")
        void testBuildReturnsNewInstance() {
            BeanBuilder<TestBean> builder = BeanBuilder.of(TestBean.class)
                    .set("name", "John");

            TestBean bean1 = builder.build();
            TestBean bean2 = builder.build();

            assertThat(bean1).isNotSameAs(bean2);
        }

        @Test
        @DisplayName("build 应用所有设置")
        void testBuildAppliesAllSettings() {
            TestBean bean = BeanBuilder.of(TestBean.class)
                    .set("name", "John")
                    .set("age", 25)
                    .set("active", true)
                    .build();

            assertThat(bean.getName()).isEqualTo("John");
            assertThat(bean.getAge()).isEqualTo(25);
            assertThat(bean.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("buildAndValidate 测试")
    class BuildAndValidateTests {

        @Test
        @DisplayName("buildAndValidate 执行验证")
        void testBuildAndValidate() {
            AtomicBoolean validated = new AtomicBoolean(false);

            TestBean bean = BeanBuilder.of(TestBean.class)
                    .set("name", "John")
                    .buildAndValidate(b -> validated.set(true));

            assertThat(validated.get()).isTrue();
            assertThat(bean.getName()).isEqualTo("John");
        }

        @Test
        @DisplayName("buildAndValidate 验证失败抛异常")
        void testBuildAndValidateThrows() {
            assertThatThrownBy(() ->
                    BeanBuilder.of(TestBean.class)
                            .set("name", "John")
                            .buildAndValidate(b -> {
                                if (b.getAge() == 0) {
                                    throw new IllegalStateException("Age is required");
                                }
                            })
            ).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Builder 接口实现测试")
    class BuilderInterfaceTests {

        @Test
        @DisplayName("实现 Builder 接口")
        void testImplementsBuilder() {
            BeanBuilder<TestBean> builder = BeanBuilder.of(TestBean.class);
            assertThat(builder).isInstanceOf(Builder.class);
        }
    }

    public static class TestBean {
        private String name;
        private int age;
        private boolean active;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
