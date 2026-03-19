package cloud.opencode.base.captcha.store;

import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaStore Test - Unit tests for the CaptchaStore interface
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
class CaptchaStoreTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("memory() should create a MemoryCaptchaStore")
        void memoryShouldCreateMemoryCaptchaStore() {
            CaptchaStore store = CaptchaStore.memory();

            assertThat(store).isNotNull();
            assertThat(store).isInstanceOf(MemoryCaptchaStore.class);
        }

        @Test
        @DisplayName("memory(maxSize) should create a MemoryCaptchaStore with custom size")
        void memoryWithMaxSizeShouldCreateStore() {
            CaptchaStore store = CaptchaStore.memory(500);

            assertThat(store).isNotNull();
            assertThat(store).isInstanceOf(MemoryCaptchaStore.class);
        }

        @Test
        @DisplayName("memory() should return different instances")
        void memoryShouldReturnDifferentInstances() {
            CaptchaStore s1 = CaptchaStore.memory();
            CaptchaStore s2 = CaptchaStore.memory();

            assertThat(s1).isNotSameAs(s2);
        }
    }

    @Nested
    @DisplayName("Interface Contract Tests")
    class InterfaceContractTests {

        @Test
        @DisplayName("store should support all interface methods")
        void storeShouldSupportAllInterfaceMethods() {
            CaptchaStore store = CaptchaStore.memory();

            assertThatCode(() -> {
                store.store("id-1", "answer", Duration.ofMinutes(5));
                store.get("id-1");
                store.getAndRemove("id-1");
                store.remove("id-2");
                store.exists("id-3");
                store.clearExpired();
                store.clearAll();
                store.size();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("new store should be empty")
        void newStoreShouldBeEmpty() {
            CaptchaStore store = CaptchaStore.memory();

            assertThat(store.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("store should report size after storing items")
        void storeShouldReportSizeAfterStoringItems() {
            CaptchaStore store = CaptchaStore.memory();

            store.store("id-1", "a", Duration.ofMinutes(1));
            store.store("id-2", "b", Duration.ofMinutes(1));

            assertThat(store.size()).isEqualTo(2);
        }
    }
}
