package cloud.opencode.base.core.reflect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ConstructorUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("ConstructorUtil 测试")
class ConstructorUtilTest {

    // 测试用注解
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.CONSTRUCTOR)
    @interface Inject {}

    // 测试用类
    static class MultiConstructorClass {
        public MultiConstructorClass() {}
        public MultiConstructorClass(String name) {}
        public MultiConstructorClass(String name, int age) {}
        private MultiConstructorClass(int id) {}

        @Inject
        public MultiConstructorClass(String name, int age, boolean active) {}
    }

    static class SingleConstructorClass {
        private SingleConstructorClass(String value) {}
    }

    static class NoDefaultConstructorClass {
        public NoDefaultConstructorClass(String required) {}
    }

    @Nested
    @DisplayName("获取构造器测试")
    class GetConstructorsTests {

        @Test
        @DisplayName("getAllConstructors")
        void testGetAllConstructors() {
            List<Constructor<MultiConstructorClass>> constructors =
                    ConstructorUtil.getAllConstructors(MultiConstructorClass.class);
            assertThat(constructors).hasSize(5);
        }

        @Test
        @DisplayName("getPublicConstructors")
        void testGetPublicConstructors() {
            List<Constructor<MultiConstructorClass>> constructors =
                    ConstructorUtil.getPublicConstructors(MultiConstructorClass.class);
            assertThat(constructors).hasSize(4); // 排除 private 的
        }

        @Test
        @DisplayName("getDefaultConstructor")
        void testGetDefaultConstructor() {
            Optional<Constructor<MultiConstructorClass>> constructor =
                    ConstructorUtil.getDefaultConstructor(MultiConstructorClass.class);
            assertThat(constructor).isPresent();
            assertThat(constructor.get().getParameterCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("getDefaultConstructor 不存在")
        void testGetDefaultConstructorNotFound() {
            Optional<Constructor<NoDefaultConstructorClass>> constructor =
                    ConstructorUtil.getDefaultConstructor(NoDefaultConstructorClass.class);
            assertThat(constructor).isEmpty();
        }

        @Test
        @DisplayName("getConstructor 按参数类型")
        void testGetConstructorByParamTypes() {
            Optional<Constructor<MultiConstructorClass>> constructor =
                    ConstructorUtil.getConstructor(MultiConstructorClass.class, String.class, int.class);
            assertThat(constructor).isPresent();
            assertThat(constructor.get().getParameterCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("getConstructor 不存在的参数类型")
        void testGetConstructorByParamTypesNotFound() {
            Optional<Constructor<MultiConstructorClass>> constructor =
                    ConstructorUtil.getConstructor(MultiConstructorClass.class, Double.class);
            assertThat(constructor).isEmpty();
        }
    }

    @Nested
    @DisplayName("按条件过滤构造器测试")
    class FilterConstructorsTests {

        @Test
        @DisplayName("getConstructorsWithAnnotation")
        void testGetConstructorsWithAnnotation() {
            List<Constructor<MultiConstructorClass>> constructors =
                    ConstructorUtil.getConstructorsWithAnnotation(MultiConstructorClass.class, Inject.class);
            assertThat(constructors).hasSize(1);
            assertThat(constructors.get(0).getParameterCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("getMinArgsConstructor")
        void testGetMinArgsConstructor() {
            Optional<Constructor<MultiConstructorClass>> constructor =
                    ConstructorUtil.getMinArgsConstructor(MultiConstructorClass.class);
            assertThat(constructor).isPresent();
            assertThat(constructor.get().getParameterCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("getMaxArgsConstructor")
        void testGetMaxArgsConstructor() {
            Optional<Constructor<MultiConstructorClass>> constructor =
                    ConstructorUtil.getMaxArgsConstructor(MultiConstructorClass.class);
            assertThat(constructor).isPresent();
            assertThat(constructor.get().getParameterCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("构造器检查测试")
    class ConstructorCheckTests {

        @Test
        @DisplayName("hasDefaultConstructor true")
        void testHasDefaultConstructorTrue() {
            assertThat(ConstructorUtil.hasDefaultConstructor(MultiConstructorClass.class)).isTrue();
        }

        @Test
        @DisplayName("hasDefaultConstructor false")
        void testHasDefaultConstructorFalse() {
            assertThat(ConstructorUtil.hasDefaultConstructor(NoDefaultConstructorClass.class)).isFalse();
        }

        @Test
        @DisplayName("isPublic")
        void testIsPublic() {
            Optional<Constructor<MultiConstructorClass>> publicCtor =
                    ConstructorUtil.getDefaultConstructor(MultiConstructorClass.class);
            assertThat(publicCtor).isPresent();
            assertThat(ConstructorUtil.isPublic(publicCtor.get())).isTrue();

            Optional<Constructor<SingleConstructorClass>> privateCtor =
                    ConstructorUtil.getConstructor(SingleConstructorClass.class, String.class);
            assertThat(privateCtor).isPresent();
            assertThat(ConstructorUtil.isPublic(privateCtor.get())).isFalse();
        }

        @Test
        @DisplayName("isPrivate")
        void testIsPrivate() {
            Optional<Constructor<SingleConstructorClass>> privateCtor =
                    ConstructorUtil.getConstructor(SingleConstructorClass.class, String.class);
            assertThat(privateCtor).isPresent();
            assertThat(ConstructorUtil.isPrivate(privateCtor.get())).isTrue();

            Optional<Constructor<MultiConstructorClass>> publicCtor =
                    ConstructorUtil.getDefaultConstructor(MultiConstructorClass.class);
            assertThat(publicCtor).isPresent();
            assertThat(ConstructorUtil.isPrivate(publicCtor.get())).isFalse();
        }
    }

    @Nested
    @DisplayName("创建实例测试")
    class NewInstanceTests {

        @Test
        @DisplayName("newInstance Constructor 无参")
        void testNewInstanceConstructorNoArgs() {
            Optional<Constructor<MultiConstructorClass>> constructor =
                    ConstructorUtil.getDefaultConstructor(MultiConstructorClass.class);
            assertThat(constructor).isPresent();

            MultiConstructorClass instance = ConstructorUtil.newInstance(constructor.get());
            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("newInstance Constructor 带参")
        void testNewInstanceConstructorWithArgs() {
            Optional<Constructor<MultiConstructorClass>> constructor =
                    ConstructorUtil.getConstructor(MultiConstructorClass.class, String.class, int.class);
            assertThat(constructor).isPresent();

            MultiConstructorClass instance = ConstructorUtil.newInstance(constructor.get(), "Test", 25);
            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("newInstance Constructor 私有")
        void testNewInstancePrivateConstructor() {
            Optional<Constructor<SingleConstructorClass>> constructor =
                    ConstructorUtil.getConstructor(SingleConstructorClass.class, String.class);
            assertThat(constructor).isPresent();

            SingleConstructorClass instance = ConstructorUtil.newInstance(constructor.get(), "value");
            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("newInstance Class")
        void testNewInstanceClass() {
            MultiConstructorClass instance = ConstructorUtil.newInstance(MultiConstructorClass.class);
            assertThat(instance).isNotNull();
        }

        @Test
        @DisplayName("newInstance Class 无默认构造抛异常")
        void testNewInstanceClassNoDefaultConstructor() {
            assertThatThrownBy(() -> ConstructorUtil.newInstance(NoDefaultConstructorClass.class))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No default constructor");
        }
    }

    @Nested
    @DisplayName("参数名测试")
    class ParameterNamesTests {

        @Test
        @DisplayName("getParameterNames")
        void testGetParameterNames() {
            Optional<Constructor<MultiConstructorClass>> constructor =
                    ConstructorUtil.getConstructor(MultiConstructorClass.class, String.class, int.class);
            assertThat(constructor).isPresent();

            String[] names = ConstructorUtil.getParameterNames(constructor.get());
            assertThat(names).hasSize(2);
            // 如果没有 -parameters 编译选项，参数名可能是 arg0, arg1
        }

        @Test
        @DisplayName("getParameterNames 无参构造")
        void testGetParameterNamesNoArgs() {
            Optional<Constructor<MultiConstructorClass>> constructor =
                    ConstructorUtil.getDefaultConstructor(MultiConstructorClass.class);
            assertThat(constructor).isPresent();

            String[] names = ConstructorUtil.getParameterNames(constructor.get());
            assertThat(names).isEmpty();
        }
    }
}
