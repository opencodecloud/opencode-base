package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.type.TypeToken;
import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ReflectCacheTest Tests
 * ReflectCacheTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("ReflectCache 测试")
class ReflectCacheTest {

    @BeforeEach
    void setUp() {
        ReflectCache.clearCache();
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = ReflectCache.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("字段缓存测试")
    class FieldCacheTests {

        @Test
        @DisplayName("缓存不存在时返回空")
        void testGetFieldsEmpty() {
            Optional<Field[]> result = ReflectCache.getFields(String.class);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("缓存字段后能获取")
        void testCacheAndGetFields() {
            Field[] fields = String.class.getDeclaredFields();
            ReflectCache.cacheFields(String.class, fields);

            Optional<Field[]> result = ReflectCache.getFields(String.class);
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(fields);
        }

        @Test
        @DisplayName("缓存null类不操作")
        void testCacheFieldsNullClass() {
            Field[] fields = String.class.getDeclaredFields();
            ReflectCache.cacheFields(null, fields);
            // Should not throw
        }

        @Test
        @DisplayName("缓存null字段不操作")
        void testCacheFieldsNullFields() {
            ReflectCache.cacheFields(String.class, null);
            Optional<Field[]> result = ReflectCache.getFields(String.class);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("方法缓存测试")
    class MethodCacheTests {

        @Test
        @DisplayName("缓存不存在时返回空")
        void testGetMethodsEmpty() {
            Optional<Method[]> result = ReflectCache.getMethods(String.class);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("缓存方法后能获取")
        void testCacheAndGetMethods() {
            Method[] methods = String.class.getDeclaredMethods();
            ReflectCache.cacheMethods(String.class, methods);

            Optional<Method[]> result = ReflectCache.getMethods(String.class);
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(methods);
        }

        @Test
        @DisplayName("缓存null类不操作")
        void testCacheMethodsNullClass() {
            Method[] methods = String.class.getDeclaredMethods();
            ReflectCache.cacheMethods(null, methods);
            // Should not throw
        }

        @Test
        @DisplayName("缓存null方法不操作")
        void testCacheMethodsNullMethods() {
            ReflectCache.cacheMethods(String.class, null);
            Optional<Method[]> result = ReflectCache.getMethods(String.class);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("构造器缓存测试")
    class ConstructorCacheTests {

        @Test
        @DisplayName("缓存不存在时返回空")
        void testGetConstructorsEmpty() {
            Optional<Constructor<?>[]> result = ReflectCache.getConstructors(String.class);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("缓存构造器后能获取")
        void testCacheAndGetConstructors() {
            Constructor<?>[] constructors = String.class.getDeclaredConstructors();
            ReflectCache.cacheConstructors(String.class, constructors);

            Optional<Constructor<?>[]> result = ReflectCache.getConstructors(String.class);
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(constructors);
        }

        @Test
        @DisplayName("缓存null类不操作")
        void testCacheConstructorsNullClass() {
            Constructor<?>[] constructors = String.class.getDeclaredConstructors();
            ReflectCache.cacheConstructors(null, constructors);
            // Should not throw
        }

        @Test
        @DisplayName("缓存null构造器不操作")
        void testCacheConstructorsNullConstructors() {
            ReflectCache.cacheConstructors(String.class, null);
            Optional<Constructor<?>[]> result = ReflectCache.getConstructors(String.class);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("TypeToken缓存测试")
    class TypeTokenCacheTests {

        @Test
        @DisplayName("缓存不存在时返回空")
        void testGetTypeTokenEmpty() {
            Optional<TypeToken<String>> result = ReflectCache.getTypeToken(String.class);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("缓存TypeToken后能获取")
        void testCacheAndGetTypeToken() {
            TypeToken<String> token = TypeToken.of(String.class);
            ReflectCache.cacheTypeToken(String.class, token);

            Optional<TypeToken<String>> result = ReflectCache.getTypeToken(String.class);
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(token);
        }

        @Test
        @DisplayName("缓存null类型不操作")
        void testCacheTypeTokenNullType() {
            TypeToken<String> token = TypeToken.of(String.class);
            ReflectCache.cacheTypeToken(null, token);
            // Should not throw
        }

        @Test
        @DisplayName("缓存null TypeToken不操作")
        void testCacheTypeTokenNullToken() {
            ReflectCache.cacheTypeToken(String.class, null);
            Optional<TypeToken<String>> result = ReflectCache.getTypeToken(String.class);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("clearCache方法测试")
    class ClearCacheTests {

        @Test
        @DisplayName("清除所有缓存")
        void testClearAllCache() {
            // Cache some data
            ReflectCache.cacheFields(String.class, String.class.getDeclaredFields());
            ReflectCache.cacheMethods(String.class, String.class.getDeclaredMethods());
            ReflectCache.cacheConstructors(String.class, String.class.getDeclaredConstructors());
            ReflectCache.cacheTypeToken(String.class, TypeToken.of(String.class));

            // Clear all
            ReflectCache.clearCache();

            // Verify all cleared
            assertThat(ReflectCache.getFields(String.class)).isEmpty();
            assertThat(ReflectCache.getMethods(String.class)).isEmpty();
            assertThat(ReflectCache.getConstructors(String.class)).isEmpty();
            assertThat(ReflectCache.getTypeToken(String.class)).isEmpty();
        }

        @Test
        @DisplayName("清除特定类的缓存")
        void testClearCacheForClass() {
            // Cache data for multiple classes
            ReflectCache.cacheFields(String.class, String.class.getDeclaredFields());
            ReflectCache.cacheFields(Integer.class, Integer.class.getDeclaredFields());

            // Clear only String.class
            ReflectCache.clearCache(String.class);

            // Verify
            assertThat(ReflectCache.getFields(String.class)).isEmpty();
            assertThat(ReflectCache.getFields(Integer.class)).isPresent();
        }

        @Test
        @DisplayName("清除null类不操作")
        void testClearCacheNullClass() {
            ReflectCache.cacheFields(String.class, String.class.getDeclaredFields());
            ReflectCache.clearCache(null);
            assertThat(ReflectCache.getFields(String.class)).isPresent();
        }
    }

    @Nested
    @DisplayName("getCacheStats方法测试")
    class CacheStatsTests {

        @Test
        @DisplayName("初始统计为空")
        void testInitialStats() {
            ReflectCache.CacheStats stats = ReflectCache.getCacheStats();
            assertThat(stats.hitCount()).isZero();
            assertThat(stats.missCount()).isZero();
            assertThat(stats.hitRate()).isZero();
        }

        @Test
        @DisplayName("统计命中和未命中")
        void testHitAndMiss() {
            // Miss
            ReflectCache.getFields(String.class);
            // Cache and hit
            ReflectCache.cacheFields(String.class, String.class.getDeclaredFields());
            ReflectCache.getFields(String.class);

            ReflectCache.CacheStats stats = ReflectCache.getCacheStats();
            assertThat(stats.hitCount()).isEqualTo(1);
            assertThat(stats.missCount()).isEqualTo(1);
            assertThat(stats.hitRate()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("统计缓存大小")
        void testCacheSize() {
            ReflectCache.cacheFields(String.class, String.class.getDeclaredFields());
            ReflectCache.cacheMethods(String.class, String.class.getDeclaredMethods());
            ReflectCache.cacheConstructors(String.class, String.class.getDeclaredConstructors());
            ReflectCache.cacheTypeToken(String.class, TypeToken.of(String.class));

            ReflectCache.CacheStats stats = ReflectCache.getCacheStats();
            assertThat(stats.fieldCacheSize()).isEqualTo(1);
            assertThat(stats.methodCacheSize()).isEqualTo(1);
            assertThat(stats.constructorCacheSize()).isEqualTo(1);
            assertThat(stats.typeTokenCacheSize()).isEqualTo(1);
            assertThat(stats.totalSize()).isEqualTo(4);
        }

        @Test
        @DisplayName("统计总请求数")
        void testTotalRequests() {
            ReflectCache.getFields(String.class);
            ReflectCache.getMethods(String.class);
            ReflectCache.getConstructors(String.class);

            ReflectCache.CacheStats stats = ReflectCache.getCacheStats();
            assertThat(stats.totalRequests()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("CacheStats记录测试")
    class CacheStatsRecordTests {

        @Test
        @DisplayName("CacheStats记录属性")
        void testCacheStatsRecord() {
            ReflectCache.CacheStats stats = new ReflectCache.CacheStats(
                    10, 5, 0.666, 2, 3, 4, 1);

            assertThat(stats.hitCount()).isEqualTo(10);
            assertThat(stats.missCount()).isEqualTo(5);
            assertThat(stats.hitRate()).isEqualTo(0.666);
            assertThat(stats.fieldCacheSize()).isEqualTo(2);
            assertThat(stats.methodCacheSize()).isEqualTo(3);
            assertThat(stats.constructorCacheSize()).isEqualTo(4);
            assertThat(stats.typeTokenCacheSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("totalSize计算正确")
        void testTotalSize() {
            ReflectCache.CacheStats stats = new ReflectCache.CacheStats(
                    0, 0, 0, 2, 3, 4, 1);
            assertThat(stats.totalSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("totalRequests计算正确")
        void testTotalRequests() {
            ReflectCache.CacheStats stats = new ReflectCache.CacheStats(
                    10, 5, 0.666, 0, 0, 0, 0);
            assertThat(stats.totalRequests()).isEqualTo(15);
        }
    }
}
