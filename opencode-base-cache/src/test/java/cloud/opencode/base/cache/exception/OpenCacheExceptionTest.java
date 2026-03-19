package cloud.opencode.base.cache.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenCacheException Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class OpenCacheExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        OpenCacheException ex = new OpenCacheException("Test error");

        assertThat(ex.getMessage()).contains("Test error");
        assertThat(ex.getCacheName()).isEmpty();
        assertThat(ex.getKey()).isEmpty();
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void shouldCreateWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        OpenCacheException ex = new OpenCacheException("Test error", cause);

        assertThat(ex.getMessage()).contains("Test error");
        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getCacheName()).isEmpty();
        assertThat(ex.getKey()).isEmpty();
    }

    @Test
    void shouldCreateWithCacheNameKeyAndMessage() {
        OpenCacheException ex = new OpenCacheException("users", "user:1001", "Not found");

        assertThat(ex.getMessage()).contains("Cache[users]");
        assertThat(ex.getMessage()).contains("key[user:1001]");
        assertThat(ex.getMessage()).contains("Not found");
        assertThat(ex.getCacheName()).hasValue("users");
        assertThat(ex.getKey()).hasValue("user:1001");
    }

    @Test
    void shouldCreateWithCacheNameKeyAndCause() {
        RuntimeException cause = new RuntimeException("DB error");
        OpenCacheException ex = new OpenCacheException("users", "user:1001", cause);

        assertThat(ex.getMessage()).contains("Cache[users]");
        assertThat(ex.getMessage()).contains("key[user:1001]");
        assertThat(ex.getMessage()).contains("DB error");
        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getCacheName()).hasValue("users");
        assertThat(ex.getKey()).hasValue("user:1001");
    }

    @Test
    void shouldCreateWithCacheNameKeyMessageAndCause() {
        RuntimeException cause = new RuntimeException("DB error");
        OpenCacheException ex = new OpenCacheException("users", "user:1001", "Loading failed", cause);

        assertThat(ex.getMessage()).contains("Cache[users]");
        assertThat(ex.getMessage()).contains("key[user:1001]");
        assertThat(ex.getMessage()).contains("Loading failed");
        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getCacheName()).hasValue("users");
        assertThat(ex.getKey()).hasValue("user:1001");
    }

    @Test
    void shouldFormatMessageWithCacheNameOnly() {
        OpenCacheException ex = new OpenCacheException("users", null, "Cache error");

        assertThat(ex.getMessage()).contains("Cache[users]");
        assertThat(ex.getMessage()).contains("Cache error");
        assertThat(ex.getCacheName()).hasValue("users");
        assertThat(ex.getKey()).isEmpty();
    }

    @Test
    void shouldFormatMessageWithNullCacheName() {
        OpenCacheException ex = new OpenCacheException(null, null, "Generic error");

        assertThat(ex.getMessage()).contains("Generic error");
    }

    @Test
    void shouldCreateLoadingFailedException() {
        RuntimeException cause = new RuntimeException("DB error");
        OpenCacheException ex = OpenCacheException.loadingFailed("users", "user:1001", cause);

        assertThat(ex.getMessage()).contains("Failed to load value");
        assertThat(ex.getCacheName()).hasValue("users");
        assertThat(ex.getKey()).hasValue("user:1001");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldCreateTimeoutException() {
        OpenCacheException ex = OpenCacheException.timeout("users", "user:1001");

        assertThat(ex.getMessage()).contains("Operation timed out");
        assertThat(ex.getCacheName()).hasValue("users");
        assertThat(ex.getKey()).hasValue("user:1001");
    }

    @Test
    void shouldCreateCapacityExceededException() {
        OpenCacheException ex = OpenCacheException.capacityExceeded("users", 10000);

        assertThat(ex.getMessage()).contains("Cache capacity exceeded");
        assertThat(ex.getMessage()).contains("max=10000");
        assertThat(ex.getCacheName()).hasValue("users");
    }

    @Test
    void shouldExtendRuntimeException() {
        OpenCacheException ex = new OpenCacheException("Test");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
