package cloud.opencode.base.json.adapter;

import cloud.opencode.base.json.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.*;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonAdapterRegistry 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonAdapterRegistry 测试")
class JsonAdapterRegistryTest {

    @AfterEach
    void tearDown() {
        // Reset registry after each test
        JsonAdapterRegistry.clear();
    }

    @Nested
    @DisplayName("register方法测试")
    class RegisterTests {

        @Test
        @DisplayName("注册适配器")
        void testRegisterAdapter() {
            JsonTypeAdapter<TestClass> adapter = createTestAdapter();

            JsonAdapterRegistry.register(adapter);

            assertThat(JsonAdapterRegistry.hasAdapter(TestClass.class)).isTrue();
        }

        @Test
        @DisplayName("注册null适配器抛出异常")
        void testRegisterNullAdapter() {
            assertThatThrownBy(() -> JsonAdapterRegistry.register(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("按类型注册适配器")
        void testRegisterWithType() {
            JsonTypeAdapter<TestClass> adapter = createTestAdapter();

            JsonAdapterRegistry.register(TestClass.class, adapter);

            assertThat(JsonAdapterRegistry.getAdapter(TestClass.class)).isEqualTo(adapter);
        }

        @Test
        @DisplayName("注册null类型抛出异常")
        void testRegisterNullType() {
            JsonTypeAdapter<TestClass> adapter = createTestAdapter();

            assertThatThrownBy(() -> JsonAdapterRegistry.register(null, adapter))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("registerFactory方法测试")
    class RegisterFactoryTests {

        @Test
        @DisplayName("注册工厂")
        void testRegisterFactory() {
            JsonAdapterRegistry.AdapterFactory factory = type -> {
                if (type == AnotherTestClass.class) {
                    return createAnotherTestAdapter();
                }
                return null;
            };

            JsonAdapterRegistry.registerFactory(factory);

            assertThat(JsonAdapterRegistry.getAdapter(AnotherTestClass.class)).isNotNull();
        }

        @Test
        @DisplayName("注册null工厂抛出异常")
        void testRegisterNullFactory() {
            assertThatThrownBy(() -> JsonAdapterRegistry.registerFactory(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("工厂创建的适配器被缓存")
        void testFactoryResultCached() {
            int[] callCount = {0};
            JsonAdapterRegistry.AdapterFactory factory = type -> {
                if (type == AnotherTestClass.class) {
                    callCount[0]++;
                    return createAnotherTestAdapter();
                }
                return null;
            };

            JsonAdapterRegistry.registerFactory(factory);

            JsonAdapterRegistry.getAdapter(AnotherTestClass.class);
            JsonAdapterRegistry.getAdapter(AnotherTestClass.class);

            assertThat(callCount[0]).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getAdapter方法测试")
    class GetAdapterTests {

        @Test
        @DisplayName("获取已注册适配器")
        void testGetRegisteredAdapter() {
            JsonTypeAdapter<TestClass> adapter = createTestAdapter();
            JsonAdapterRegistry.register(adapter);

            JsonTypeAdapter<TestClass> retrieved = JsonAdapterRegistry.getAdapter(TestClass.class);

            assertThat(retrieved).isEqualTo(adapter);
        }

        @Test
        @DisplayName("获取未注册类型返回null")
        void testGetUnregisteredAdapter() {
            JsonTypeAdapter<?> adapter = JsonAdapterRegistry.getAdapter(UnregisteredClass.class);

            assertThat(adapter).isNull();
        }

        @Test
        @DisplayName("通过Type获取适配器")
        void testGetAdapterByType() {
            JsonTypeAdapter<TestClass> adapter = createTestAdapter();
            JsonAdapterRegistry.register(adapter);

            JsonTypeAdapter<?> retrieved = JsonAdapterRegistry.getAdapter((Type) TestClass.class);

            assertThat(retrieved).isNotNull();
        }
    }

    @Nested
    @DisplayName("hasAdapter方法测试")
    class HasAdapterTests {

        @Test
        @DisplayName("已注册返回true")
        void testHasAdapterTrue() {
            JsonAdapterRegistry.register(createTestAdapter());

            assertThat(JsonAdapterRegistry.hasAdapter(TestClass.class)).isTrue();
        }

        @Test
        @DisplayName("未注册返回false")
        void testHasAdapterFalse() {
            assertThat(JsonAdapterRegistry.hasAdapter(UnregisteredClass.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("unregister方法测试")
    class UnregisterTests {

        @Test
        @DisplayName("注销已注册适配器")
        void testUnregister() {
            JsonAdapterRegistry.register(createTestAdapter());

            JsonTypeAdapter<?> removed = JsonAdapterRegistry.unregister(TestClass.class);

            assertThat(removed).isNotNull();
            assertThat(JsonAdapterRegistry.hasAdapter(TestClass.class)).isFalse();
        }

        @Test
        @DisplayName("注销未注册适配器返回null")
        void testUnregisterNotRegistered() {
            JsonTypeAdapter<?> removed = JsonAdapterRegistry.unregister(UnregisteredClass.class);

            assertThat(removed).isNull();
        }
    }

    @Nested
    @DisplayName("getRegisteredTypes方法测试")
    class GetRegisteredTypesTests {

        @Test
        @DisplayName("返回注册类型集合")
        void testGetRegisteredTypes() {
            JsonAdapterRegistry.register(createTestAdapter());

            Set<Type> types = JsonAdapterRegistry.getRegisteredTypes();

            assertThat(types).contains(TestClass.class);
        }

        @Test
        @DisplayName("返回不可修改集合")
        void testUnmodifiableSet() {
            Set<Type> types = JsonAdapterRegistry.getRegisteredTypes();

            assertThatThrownBy(() -> types.add(String.class))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清除自定义适配器")
        void testClear() {
            JsonAdapterRegistry.register(createTestAdapter());

            JsonAdapterRegistry.clear();

            assertThat(JsonAdapterRegistry.hasAdapter(TestClass.class)).isFalse();
        }

        @Test
        @DisplayName("保留内置适配器")
        void testClearKeepsBuiltIn() {
            JsonAdapterRegistry.clear();

            // Built-in adapters should still work
            assertThat(JsonAdapterRegistry.hasAdapter(LocalDate.class)).isTrue();
            assertThat(JsonAdapterRegistry.hasAdapter(UUID.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("内置适配器测试")
    class BuiltInAdaptersTests {

        @Test
        @DisplayName("LocalDate适配器")
        void testLocalDateAdapter() {
            JsonTypeAdapter<LocalDate> adapter = JsonAdapterRegistry.getAdapter(LocalDate.class);
            LocalDate date = LocalDate.of(2024, 1, 15);

            JsonNode json = adapter.toJson(date);
            LocalDate result = adapter.fromJson(json);

            assertThat(json.asString()).isEqualTo("2024-01-15");
            assertThat(result).isEqualTo(date);
        }

        @Test
        @DisplayName("LocalTime适配器")
        void testLocalTimeAdapter() {
            JsonTypeAdapter<LocalTime> adapter = JsonAdapterRegistry.getAdapter(LocalTime.class);
            LocalTime time = LocalTime.of(10, 30, 45);

            JsonNode json = adapter.toJson(time);
            LocalTime result = adapter.fromJson(json);

            assertThat(result).isEqualTo(time);
        }

        @Test
        @DisplayName("LocalDateTime适配器")
        void testLocalDateTimeAdapter() {
            JsonTypeAdapter<LocalDateTime> adapter = JsonAdapterRegistry.getAdapter(LocalDateTime.class);
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30);

            JsonNode json = adapter.toJson(dateTime);
            LocalDateTime result = adapter.fromJson(json);

            assertThat(result).isEqualTo(dateTime);
        }

        @Test
        @DisplayName("Instant适配器")
        void testInstantAdapter() {
            JsonTypeAdapter<Instant> adapter = JsonAdapterRegistry.getAdapter(Instant.class);
            Instant instant = Instant.parse("2024-01-15T10:30:00Z");

            JsonNode json = adapter.toJson(instant);
            Instant result = adapter.fromJson(json);

            assertThat(result).isEqualTo(instant);
        }

        @Test
        @DisplayName("Duration适配器")
        void testDurationAdapter() {
            JsonTypeAdapter<Duration> adapter = JsonAdapterRegistry.getAdapter(Duration.class);
            Duration duration = Duration.ofHours(2).plusMinutes(30);

            JsonNode json = adapter.toJson(duration);
            Duration result = adapter.fromJson(json);

            assertThat(result).isEqualTo(duration);
        }

        @Test
        @DisplayName("Period适配器")
        void testPeriodAdapter() {
            JsonTypeAdapter<Period> adapter = JsonAdapterRegistry.getAdapter(Period.class);
            Period period = Period.of(1, 2, 3);

            JsonNode json = adapter.toJson(period);
            Period result = adapter.fromJson(json);

            assertThat(result).isEqualTo(period);
        }

        @Test
        @DisplayName("UUID适配器")
        void testUuidAdapter() {
            JsonTypeAdapter<UUID> adapter = JsonAdapterRegistry.getAdapter(UUID.class);
            UUID uuid = UUID.randomUUID();

            JsonNode json = adapter.toJson(uuid);
            UUID result = adapter.fromJson(json);

            assertThat(result).isEqualTo(uuid);
        }

        @Test
        @DisplayName("Currency适配器")
        void testCurrencyAdapter() {
            JsonTypeAdapter<Currency> adapter = JsonAdapterRegistry.getAdapter(Currency.class);
            Currency currency = Currency.getInstance("USD");

            JsonNode json = adapter.toJson(currency);
            Currency result = adapter.fromJson(json);

            assertThat(json.asString()).isEqualTo("USD");
            assertThat(result).isEqualTo(currency);
        }

        @Test
        @DisplayName("Locale适配器")
        void testLocaleAdapter() {
            JsonTypeAdapter<Locale> adapter = JsonAdapterRegistry.getAdapter(Locale.class);
            Locale locale = Locale.US;

            JsonNode json = adapter.toJson(locale);
            Locale result = adapter.fromJson(json);

            assertThat(result).isEqualTo(locale);
        }

        @Test
        @DisplayName("TimeZone适配器")
        void testTimeZoneAdapter() {
            JsonTypeAdapter<TimeZone> adapter = JsonAdapterRegistry.getAdapter(TimeZone.class);
            TimeZone timezone = TimeZone.getTimeZone("America/New_York");

            JsonNode json = adapter.toJson(timezone);
            TimeZone result = adapter.fromJson(json);

            assertThat(result.getID()).isEqualTo(timezone.getID());
        }

        @Test
        @DisplayName("ZoneId适配器")
        void testZoneIdAdapter() {
            JsonTypeAdapter<ZoneId> adapter = JsonAdapterRegistry.getAdapter(ZoneId.class);
            ZoneId zoneId = ZoneId.of("Europe/London");

            JsonNode json = adapter.toJson(zoneId);
            ZoneId result = adapter.fromJson(json);

            assertThat(result).isEqualTo(zoneId);
        }
    }

    @Nested
    @DisplayName("Registry实例测试")
    class RegistryInstanceTests {

        @Test
        @DisplayName("创建隔离注册表")
        void testCreateRegistry() {
            JsonAdapterRegistry.Registry registry = JsonAdapterRegistry.createRegistry();

            assertThat(registry).isNotNull();
        }

        @Test
        @DisplayName("注册表隔离")
        void testRegistryIsolation() {
            JsonAdapterRegistry.Registry registry = JsonAdapterRegistry.createRegistry();
            JsonTypeAdapter<TestClass> adapter = createTestAdapter();

            registry.register(adapter);

            // Should not affect global registry
            assertThat(JsonAdapterRegistry.hasAdapter(TestClass.class)).isFalse();
        }

        @Test
        @DisplayName("注册表获取适配器")
        void testRegistryGetAdapter() {
            JsonAdapterRegistry.Registry registry = JsonAdapterRegistry.createRegistry();
            JsonTypeAdapter<TestClass> adapter = createTestAdapter();
            registry.register(adapter);

            JsonTypeAdapter<TestClass> retrieved = registry.getAdapter(TestClass.class);

            assertThat(retrieved).isEqualTo(adapter);
        }

        @Test
        @DisplayName("注册表按类型注册")
        void testRegistryRegisterByType() {
            JsonAdapterRegistry.Registry registry = JsonAdapterRegistry.createRegistry();
            JsonTypeAdapter<TestClass> adapter = createTestAdapter();

            registry.register(TestClass.class, adapter);

            assertThat(registry.getAdapter(TestClass.class)).isNotNull();
        }

        @Test
        @DisplayName("注册表工厂")
        void testRegistryFactory() {
            JsonAdapterRegistry.Registry registry = JsonAdapterRegistry.createRegistry();
            registry.registerFactory(type -> {
                if (type == AnotherTestClass.class) {
                    return createAnotherTestAdapter();
                }
                return null;
            });

            assertThat(registry.getAdapter(AnotherTestClass.class)).isNotNull();
        }

        @Test
        @DisplayName("注册表回退到全局")
        void testRegistryFallbackToGlobal() {
            JsonAdapterRegistry.Registry registry = JsonAdapterRegistry.createRegistry();

            // Should fallback to global registry for built-in adapters
            JsonTypeAdapter<LocalDate> adapter = registry.getAdapter(LocalDate.class);

            assertThat(adapter).isNotNull();
        }
    }

    @Nested
    @DisplayName("AdapterFactory接口测试")
    class AdapterFactoryTests {

        @Test
        @DisplayName("工厂创建适配器")
        void testFactoryCreate() {
            JsonAdapterRegistry.AdapterFactory factory = type -> {
                if (type == TestClass.class) {
                    return createTestAdapter();
                }
                return null;
            };

            JsonTypeAdapter<?> adapter = factory.create(TestClass.class);

            assertThat(adapter).isNotNull();
        }

        @Test
        @DisplayName("不支持类型返回null")
        void testFactoryReturnsNull() {
            JsonAdapterRegistry.AdapterFactory factory = type -> null;

            JsonTypeAdapter<?> adapter = factory.create(TestClass.class);

            assertThat(adapter).isNull();
        }
    }

    // Test helper classes
    static class TestClass {
        String value;
    }

    static class AnotherTestClass {
        int number;
    }

    static class UnregisteredClass {}

    private JsonTypeAdapter<TestClass> createTestAdapter() {
        return new JsonTypeAdapter<>() {
            @Override
            public Class<TestClass> getType() {
                return TestClass.class;
            }

            @Override
            public JsonNode toJson(TestClass value) {
                return JsonNode.object().put("value", value.value);
            }

            @Override
            public TestClass fromJson(JsonNode node) {
                TestClass obj = new TestClass();
                obj.value = node.get("value").asString();
                return obj;
            }
        };
    }

    private JsonTypeAdapter<AnotherTestClass> createAnotherTestAdapter() {
        return new JsonTypeAdapter<>() {
            @Override
            public Class<AnotherTestClass> getType() {
                return AnotherTestClass.class;
            }

            @Override
            public JsonNode toJson(AnotherTestClass value) {
                return JsonNode.object().put("number", value.number);
            }

            @Override
            public AnotherTestClass fromJson(JsonNode node) {
                AnotherTestClass obj = new AnotherTestClass();
                obj.number = node.get("number").asInt();
                return obj;
            }
        };
    }
}
