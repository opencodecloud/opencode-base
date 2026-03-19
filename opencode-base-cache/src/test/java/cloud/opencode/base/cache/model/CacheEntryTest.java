package cloud.opencode.base.cache.model;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CacheEntry Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class CacheEntryTest {

    @Test
    void shouldCreateEntryWithKeyAndValue() {
        CacheEntry<String, String> entry = new CacheEntry<>("key", "value");

        assertThat(entry.key()).isEqualTo("key");
        assertThat(entry.value()).isEqualTo("value");
        assertThat(entry.weight()).isEqualTo(1);
        assertThat(entry.accessCount()).isEqualTo(0);
        assertThat(entry.createTime()).isGreaterThan(0);
        assertThat(entry.lastAccessTime()).isEqualTo(entry.createTime());
    }

    @Test
    void shouldCreateEntryWithWeight() {
        CacheEntry<String, String> entry = new CacheEntry<>("key", "value", 10);

        assertThat(entry.key()).isEqualTo("key");
        assertThat(entry.value()).isEqualTo("value");
        assertThat(entry.weight()).isEqualTo(10);
    }

    @Test
    void shouldCreateEntryWithAllFields() {
        long now = System.currentTimeMillis();
        CacheEntry<String, String> entry = new CacheEntry<>("key", "value", now, now + 100, 5, 10);

        assertThat(entry.key()).isEqualTo("key");
        assertThat(entry.value()).isEqualTo("value");
        assertThat(entry.createTime()).isEqualTo(now);
        assertThat(entry.lastAccessTime()).isEqualTo(now + 100);
        assertThat(entry.accessCount()).isEqualTo(5);
        assertThat(entry.weight()).isEqualTo(10);
    }

    @Test
    void shouldCalculateAge() throws Exception {
        CacheEntry<String, String> entry = new CacheEntry<>("key", "value");
        Thread.sleep(50);

        long age = entry.age();
        assertThat(age).isGreaterThanOrEqualTo(50);
    }

    @Test
    void shouldCalculateIdleTime() throws Exception {
        CacheEntry<String, String> entry = new CacheEntry<>("key", "value");
        Thread.sleep(50);

        long idle = entry.idleTime();
        assertThat(idle).isGreaterThanOrEqualTo(50);
    }

    @Test
    void shouldRecordAccess() throws Exception {
        CacheEntry<String, String> entry = new CacheEntry<>("key", "value");
        long originalAccessTime = entry.lastAccessTime();

        Thread.sleep(10);
        CacheEntry<String, String> accessed = entry.recordAccess();

        assertThat(accessed.key()).isEqualTo(entry.key());
        assertThat(accessed.value()).isEqualTo(entry.value());
        assertThat(accessed.createTime()).isEqualTo(entry.createTime());
        assertThat(accessed.lastAccessTime()).isGreaterThan(originalAccessTime);
        assertThat(accessed.accessCount()).isEqualTo(entry.accessCount() + 1);
        assertThat(accessed.weight()).isEqualTo(entry.weight());
    }

    @Test
    void shouldUpdateValue() {
        CacheEntry<String, String> entry = new CacheEntry<>("key", "value");
        CacheEntry<String, String> updated = entry.withValue("newValue");

        assertThat(updated.key()).isEqualTo("key");
        assertThat(updated.value()).isEqualTo("newValue");
        assertThat(updated.createTime()).isEqualTo(entry.createTime());
        assertThat(updated.accessCount()).isEqualTo(entry.accessCount());
        assertThat(updated.weight()).isEqualTo(entry.weight());
    }

    @Test
    void shouldUpdateWeight() {
        CacheEntry<String, String> entry = new CacheEntry<>("key", "value");
        CacheEntry<String, String> updated = entry.withWeight(100);

        assertThat(updated.key()).isEqualTo("key");
        assertThat(updated.value()).isEqualTo("value");
        assertThat(updated.weight()).isEqualTo(100);
    }

    @Test
    void shouldBeImmutable() {
        CacheEntry<String, String> original = new CacheEntry<>("key", "value", 1);

        CacheEntry<String, String> accessed = original.recordAccess();
        CacheEntry<String, String> withNewValue = original.withValue("new");
        CacheEntry<String, String> withNewWeight = original.withWeight(50);

        // Original should be unchanged
        assertThat(original.value()).isEqualTo("value");
        assertThat(original.weight()).isEqualTo(1);
        assertThat(original.accessCount()).isEqualTo(0);

        // New instances should have new values
        assertThat(accessed.accessCount()).isEqualTo(1);
        assertThat(withNewValue.value()).isEqualTo("new");
        assertThat(withNewWeight.weight()).isEqualTo(50);
    }

    @Test
    void shouldSupportEquality() {
        long now = System.currentTimeMillis();
        CacheEntry<String, String> entry1 = new CacheEntry<>("key", "value", now, now, 0, 1);
        CacheEntry<String, String> entry2 = new CacheEntry<>("key", "value", now, now, 0, 1);

        assertThat(entry1).isEqualTo(entry2);
        assertThat(entry1.hashCode()).isEqualTo(entry2.hashCode());
    }

    @Test
    void shouldSupportToString() {
        CacheEntry<String, String> entry = new CacheEntry<>("key", "value");
        String str = entry.toString();

        assertThat(str).contains("key");
        assertThat(str).contains("value");
    }
}
