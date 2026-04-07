package cloud.opencode.base.web.cookie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CookieJar")
class CookieJarTest {

    private static final URI TEST_URI = URI.create("https://example.com");

    private HttpCookie createCookie(String name, String value) {
        HttpCookie cookie = new HttpCookie(name, value);
        cookie.setPath("/");
        cookie.setDomain("example.com");
        return cookie;
    }

    @Nested
    @DisplayName("inMemory()")
    class InMemory {

        @Test
        @DisplayName("should create empty cookie jar")
        void shouldCreateEmpty() {
            CookieJar jar = CookieJar.inMemory();
            assertThat(jar.size()).isZero();
            assertThat(jar.allCookies()).isEmpty();
        }

        @Test
        @DisplayName("should accept all cookies by default")
        void shouldAcceptAllCookies() {
            CookieJar jar = CookieJar.inMemory();
            jar.add(TEST_URI, createCookie("session", "abc"));
            assertThat(jar.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("inMemory(CookiePolicy)")
    class InMemoryWithPolicy {

        @Test
        @DisplayName("should accept cookies with custom policy")
        void shouldAcceptWithPolicy() {
            CookieJar jar = CookieJar.inMemory(java.net.CookiePolicy.ACCEPT_ALL);
            jar.add(TEST_URI, createCookie("key", "val"));
            assertThat(jar.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("strict()")
    class Strict {

        @Test
        @DisplayName("should create strict cookie jar")
        void shouldCreateStrict() {
            CookieJar jar = CookieJar.strict();
            assertThat(jar).isNotNull();
            assertThat(jar.size()).isZero();
        }
    }

    @Nested
    @DisplayName("disabled()")
    class Disabled {

        @Test
        @DisplayName("should create disabled cookie jar")
        void shouldCreateDisabled() {
            CookieJar jar = CookieJar.disabled();
            assertThat(jar).isNotNull();
        }
    }

    @Nested
    @DisplayName("add(URI, HttpCookie)")
    class Add {

        @Test
        @DisplayName("should add cookie")
        void shouldAddCookie() {
            CookieJar jar = CookieJar.inMemory();
            jar.add(TEST_URI, createCookie("name", "value"));
            assertThat(jar.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("should add multiple cookies")
        void shouldAddMultiple() {
            CookieJar jar = CookieJar.inMemory();
            jar.add(TEST_URI, createCookie("a", "1"));
            jar.add(TEST_URI, createCookie("b", "2"));
            assertThat(jar.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("cookies(URI)")
    class Cookies {

        @Test
        @DisplayName("should return cookies for URI")
        void shouldReturnCookies() {
            CookieJar jar = CookieJar.inMemory();
            jar.add(TEST_URI, createCookie("session", "abc123"));
            assertThat(jar.cookies(TEST_URI)).isNotEmpty();
        }

        @Test
        @DisplayName("should return empty list for URI with no cookies")
        void shouldReturnEmptyForNoCookies() {
            CookieJar jar = CookieJar.inMemory();
            assertThat(jar.cookies(URI.create("https://other.com"))).isEmpty();
        }
    }

    @Nested
    @DisplayName("allCookies()")
    class AllCookies {

        @Test
        @DisplayName("should return all cookies")
        void shouldReturnAll() {
            CookieJar jar = CookieJar.inMemory();
            jar.add(TEST_URI, createCookie("a", "1"));
            jar.add(URI.create("https://other.com"), createCookie("b", "2"));
            assertThat(jar.allCookies()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("clear()")
    class Clear {

        @Test
        @DisplayName("should remove all cookies")
        void shouldClear() {
            CookieJar jar = CookieJar.inMemory();
            jar.add(TEST_URI, createCookie("a", "1"));
            jar.add(TEST_URI, createCookie("b", "2"));
            jar.clear();
            assertThat(jar.size()).isZero();
            assertThat(jar.allCookies()).isEmpty();
        }
    }

    @Nested
    @DisplayName("size()")
    class Size {

        @Test
        @DisplayName("should return number of cookies")
        void shouldReturnSize() {
            CookieJar jar = CookieJar.inMemory();
            assertThat(jar.size()).isZero();
            jar.add(TEST_URI, createCookie("a", "1"));
            assertThat(jar.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("asCookieManager()")
    class AsCookieManager {

        @Test
        @DisplayName("should return underlying CookieManager")
        void shouldReturnManager() {
            CookieJar jar = CookieJar.inMemory();
            CookieManager manager = jar.asCookieManager();
            assertThat(manager).isNotNull();
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTest {

        @Test
        @DisplayName("should include cookie count")
        void shouldIncludeCount() {
            CookieJar jar = CookieJar.inMemory();
            assertThat(jar.toString()).contains("CookieJar", "cookies=0");
            jar.add(TEST_URI, createCookie("a", "1"));
            assertThat(jar.toString()).contains("cookies=1");
        }
    }
}
