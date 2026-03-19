package cloud.opencode.base.feature.proxy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import cloud.opencode.base.feature.OpenFeature;
import cloud.opencode.base.feature.annotation.FeatureToggle;
import cloud.opencode.base.feature.store.InMemoryFeatureStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureProxy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("FeatureProxy 测试")
class FeatureProxyTest {

    private OpenFeature features;
    private InMemoryFeatureStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryFeatureStore();
        features = OpenFeature.create(store);
    }

    // 测试接口
    public interface TestService {
        @FeatureToggle("test-feature")
        String doSomething();

        @FeatureToggle(value = "optional-feature", defaultEnabled = false)
        String optionalMethod();

        String normalMethod();

        @FeatureToggle("int-feature")
        int getInt();

        @FeatureToggle("boolean-feature")
        boolean getBoolean();

        @FeatureToggle("long-feature")
        long getLong();

        @FeatureToggle("double-feature")
        double getDouble();

        @FeatureToggle("float-feature")
        float getFloat();

        @FeatureToggle("byte-feature")
        byte getByte();

        @FeatureToggle("short-feature")
        short getShort();

        @FeatureToggle("char-feature")
        char getChar();

        @FeatureToggle("void-feature")
        void doVoid();
    }

    // 测试实现
    public static class TestServiceImpl implements TestService {
        @Override
        public String doSomething() {
            return "result";
        }

        @Override
        public String optionalMethod() {
            return "optional";
        }

        @Override
        public String normalMethod() {
            return "normal";
        }

        @Override
        public int getInt() {
            return 42;
        }

        @Override
        public boolean getBoolean() {
            return true;
        }

        @Override
        public long getLong() {
            return 100L;
        }

        @Override
        public double getDouble() {
            return 3.14;
        }

        @Override
        public float getFloat() {
            return 2.5f;
        }

        @Override
        public byte getByte() {
            return 10;
        }

        @Override
        public short getShort() {
            return 20;
        }

        @Override
        public char getChar() {
            return 'A';
        }

        @Override
        public void doVoid() {
            // void method
        }
    }

    @Nested
    @DisplayName("create() 测试")
    class CreateTests {

        @Test
        @DisplayName("创建代理")
        void testCreate() {
            TestService impl = new TestServiceImpl();

            TestService proxy = FeatureProxy.create(TestService.class, impl);

            assertThat(proxy).isNotNull();
            assertThat(proxy).isNotSameAs(impl);
        }

        @Test
        @DisplayName("使用自定义OpenFeature创建代理")
        void testCreateWithFeatures() {
            TestService impl = new TestServiceImpl();

            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            assertThat(proxy).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("创建Builder")
        void testBuilder() {
            TestService impl = new TestServiceImpl();

            FeatureProxy.Builder<TestService> builder = FeatureProxy.builder(TestService.class, impl);

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("null接口类型抛出异常")
        void testNullInterfaceType() {
            assertThatThrownBy(() -> FeatureProxy.builder(null, new TestServiceImpl()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("非接口类型抛出异常")
        void testNonInterfaceType() {
            assertThatThrownBy(() -> FeatureProxy.builder(TestServiceImpl.class, new TestServiceImpl()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("interface");
        }

        @Test
        @DisplayName("null目标抛出异常")
        void testNullTarget() {
            assertThatThrownBy(() -> FeatureProxy.builder(TestService.class, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("设置features")
        void testSetFeatures() {
            TestService impl = new TestServiceImpl();

            TestService proxy = FeatureProxy.builder(TestService.class, impl)
                    .features(features)
                    .build();

            assertThat(proxy).isNotNull();
        }

        @Test
        @DisplayName("null features使用默认值")
        void testNullFeatures() {
            TestService impl = new TestServiceImpl();

            TestService proxy = FeatureProxy.builder(TestService.class, impl)
                    .features(null)
                    .build();

            assertThat(proxy).isNotNull();
        }

        @Test
        @DisplayName("设置contextSupplier")
        void testSetContextSupplier() {
            TestService impl = new TestServiceImpl();

            TestService proxy = FeatureProxy.builder(TestService.class, impl)
                    .contextSupplier(() -> FeatureContext.ofUser("user1"))
                    .build();

            assertThat(proxy).isNotNull();
        }

        @Test
        @DisplayName("null contextSupplier使用默认值")
        void testNullContextSupplier() {
            TestService impl = new TestServiceImpl();

            TestService proxy = FeatureProxy.builder(TestService.class, impl)
                    .contextSupplier(null)
                    .build();

            assertThat(proxy).isNotNull();
        }

        @Test
        @DisplayName("设置whenDisabled")
        void testSetWhenDisabled() {
            TestService impl = new TestServiceImpl();

            TestService proxy = FeatureProxy.builder(TestService.class, impl)
                    .whenDisabled(FeatureProxy.DisabledBehavior.THROW_EXCEPTION)
                    .build();

            assertThat(proxy).isNotNull();
        }

        @Test
        @DisplayName("null whenDisabled使用默认值")
        void testNullWhenDisabled() {
            TestService impl = new TestServiceImpl();

            TestService proxy = FeatureProxy.builder(TestService.class, impl)
                    .whenDisabled(null)
                    .build();

            assertThat(proxy).isNotNull();
        }
    }

    @Nested
    @DisplayName("方法调用测试")
    class MethodInvocationTests {

        @Test
        @DisplayName("功能启用时执行方法")
        void testMethodExecutedWhenEnabled() {
            store.save(Feature.builder("test-feature").alwaysOn().build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            String result = proxy.doSomething();

            assertThat(result).isEqualTo("result");
        }

        @Test
        @DisplayName("功能禁用时返回默认值")
        void testMethodReturnsDefaultWhenDisabled() {
            store.save(Feature.builder("test-feature").alwaysOff().build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            String result = proxy.doSomething();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("无注解方法正常执行")
        void testNormalMethodExecuted() {
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            String result = proxy.normalMethod();

            assertThat(result).isEqualTo("normal");
        }

        @Test
        @DisplayName("功能不存在使用默认启用状态")
        void testDefaultEnabledWhenFeatureNotExists() {
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            // optional-feature 不存在，defaultEnabled=false
            String result = proxy.optionalMethod();

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("禁用行为测试")
    class DisabledBehaviorTests {

        @Test
        @DisplayName("RETURN_DEFAULT返回默认值")
        void testReturnDefault() {
            store.save(Feature.builder("test-feature").alwaysOff().build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.builder(TestService.class, impl)
                    .features(features)
                    .whenDisabled(FeatureProxy.DisabledBehavior.RETURN_DEFAULT)
                    .build();

            String result = proxy.doSomething();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("SKIP返回默认值")
        void testSkip() {
            store.save(Feature.builder("test-feature").alwaysOff().build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.builder(TestService.class, impl)
                    .features(features)
                    .whenDisabled(FeatureProxy.DisabledBehavior.SKIP)
                    .build();

            String result = proxy.doSomething();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("THROW_EXCEPTION抛出异常")
        void testThrowException() {
            store.save(Feature.builder("test-feature").alwaysOff().build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.builder(TestService.class, impl)
                    .features(features)
                    .whenDisabled(FeatureProxy.DisabledBehavior.THROW_EXCEPTION)
                    .build();

            assertThatThrownBy(proxy::doSomething)
                    .isInstanceOf(FeatureProxy.FeatureDisabledException.class)
                    .hasMessageContaining("test-feature");
        }
    }

    @Nested
    @DisplayName("基本类型默认值测试")
    class PrimitiveDefaultValueTests {

        @Test
        @DisplayName("int返回0")
        void testIntDefault() {
            store.save(Feature.builder("int-feature").alwaysOff().build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            int result = proxy.getInt();

            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("boolean返回false")
        void testBooleanDefault() {
            store.save(Feature.builder("boolean-feature").alwaysOff().build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            boolean result = proxy.getBoolean();

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("long返回0L")
        void testLongDefault() {
            store.save(Feature.builder("long-feature").alwaysOff().build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            long result = proxy.getLong();

            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("double返回0.0")
        void testDoubleDefault() {
            store.save(Feature.builder("double-feature").alwaysOff().build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            double result = proxy.getDouble();

            assertThat(result).isEqualTo(0.0d);
        }

        @Test
        @DisplayName("float返回0.0f")
        void testFloatDefault() {
            store.save(Feature.builder("float-feature").alwaysOff().build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            float result = proxy.getFloat();

            assertThat(result).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("byte返回0")
        void testByteDefault() {
            store.save(Feature.builder("byte-feature").alwaysOff().build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            byte result = proxy.getByte();

            assertThat(result).isEqualTo((byte) 0);
        }

        @Test
        @DisplayName("short返回0")
        void testShortDefault() {
            store.save(Feature.builder("short-feature").alwaysOff().build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            short result = proxy.getShort();

            assertThat(result).isEqualTo((short) 0);
        }

        @Test
        @DisplayName("char返回\\0")
        void testCharDefault() {
            store.save(Feature.builder("char-feature").alwaysOff().build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            char result = proxy.getChar();

            assertThat(result).isEqualTo('\0');
        }

        @Test
        @DisplayName("void方法正常返回")
        void testVoidDefault() {
            store.save(Feature.builder("void-feature").alwaysOff().build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            // 不应抛出异常
            proxy.doVoid();
        }
    }

    @Nested
    @DisplayName("Object方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("toString方法正常执行")
        void testToString() {
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            String result = proxy.toString();

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("hashCode方法正常执行")
        void testHashCode() {
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            int result = proxy.hashCode();

            assertThat(result).isEqualTo(impl.hashCode());
        }

        @Test
        @DisplayName("equals方法正常执行")
        void testEquals() {
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.create(TestService.class, impl, features);

            boolean result = proxy.equals(impl);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("上下文测试")
    class ContextTests {

        @Test
        @DisplayName("使用contextSupplier提供的上下文")
        void testContextSupplier() {
            store.save(Feature.builder("test-feature")
                    .forUsers("user1")
                    .build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.builder(TestService.class, impl)
                    .features(features)
                    .contextSupplier(() -> FeatureContext.ofUser("user1"))
                    .build();

            String result = proxy.doSomething();

            assertThat(result).isEqualTo("result");
        }

        @Test
        @DisplayName("用户不在列表中时禁用")
        void testContextUserNotInList() {
            store.save(Feature.builder("test-feature")
                    .forUsers("user1")
                    .build());
            TestService impl = new TestServiceImpl();
            TestService proxy = FeatureProxy.builder(TestService.class, impl)
                    .features(features)
                    .contextSupplier(() -> FeatureContext.ofUser("user2"))
                    .build();

            String result = proxy.doSomething();

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("DisabledBehavior 枚举测试")
    class DisabledBehaviorEnumTests {

        @Test
        @DisplayName("所有枚举值")
        void testAllValues() {
            FeatureProxy.DisabledBehavior[] values = FeatureProxy.DisabledBehavior.values();

            assertThat(values).hasSize(3);
            assertThat(values).containsExactlyInAnyOrder(
                    FeatureProxy.DisabledBehavior.RETURN_DEFAULT,
                    FeatureProxy.DisabledBehavior.THROW_EXCEPTION,
                    FeatureProxy.DisabledBehavior.SKIP
            );
        }

        @Test
        @DisplayName("valueOf方法")
        void testValueOf() {
            assertThat(FeatureProxy.DisabledBehavior.valueOf("RETURN_DEFAULT"))
                    .isEqualTo(FeatureProxy.DisabledBehavior.RETURN_DEFAULT);
            assertThat(FeatureProxy.DisabledBehavior.valueOf("THROW_EXCEPTION"))
                    .isEqualTo(FeatureProxy.DisabledBehavior.THROW_EXCEPTION);
            assertThat(FeatureProxy.DisabledBehavior.valueOf("SKIP"))
                    .isEqualTo(FeatureProxy.DisabledBehavior.SKIP);
        }
    }

    @Nested
    @DisplayName("FeatureDisabledException 测试")
    class FeatureDisabledExceptionTests {

        @Test
        @DisplayName("创建异常")
        void testCreateException() {
            FeatureProxy.FeatureDisabledException ex =
                    new FeatureProxy.FeatureDisabledException("my-feature", "myMethod");

            assertThat(ex.getFeatureKey()).isEqualTo("my-feature");
            assertThat(ex.getMethodName()).isEqualTo("myMethod");
            assertThat(ex.getMessage()).contains("my-feature");
            assertThat(ex.getMessage()).contains("myMethod");
        }
    }

    @Nested
    @DisplayName("类级别注解测试")
    class ClassLevelAnnotationTests {

        @FeatureToggle("class-feature")
        public interface ClassLevelService {
            String method1();
            String method2();
        }

        public static class ClassLevelServiceImpl implements ClassLevelService {
            @Override
            public String method1() {
                return "method1";
            }

            @Override
            public String method2() {
                return "method2";
            }
        }

        @Test
        @DisplayName("类级别注解影响所有方法")
        void testClassLevelAnnotation() {
            store.save(Feature.builder("class-feature").alwaysOn().build());
            ClassLevelService impl = new ClassLevelServiceImpl();
            ClassLevelService proxy = FeatureProxy.create(ClassLevelService.class, impl, features);

            assertThat(proxy.method1()).isEqualTo("method1");
            assertThat(proxy.method2()).isEqualTo("method2");
        }

        @Test
        @DisplayName("类级别注解禁用时影响所有方法")
        void testClassLevelAnnotationDisabled() {
            store.save(Feature.builder("class-feature").alwaysOff().build());
            ClassLevelService impl = new ClassLevelServiceImpl();
            ClassLevelService proxy = FeatureProxy.create(ClassLevelService.class, impl, features);

            assertThat(proxy.method1()).isNull();
            assertThat(proxy.method2()).isNull();
        }
    }
}
