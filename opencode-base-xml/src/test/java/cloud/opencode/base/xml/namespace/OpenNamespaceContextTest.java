package cloud.opencode.base.xml.namespace;

import org.junit.jupiter.api.*;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenNamespaceContextTest Tests
 * OpenNamespaceContextTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("OpenNamespaceContext Tests")
class OpenNamespaceContextTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create should create empty context")
        void createShouldCreateEmptyContext() {
            OpenNamespaceContext context = OpenNamespaceContext.create();

            assertThat(context).isNotNull();
        }

        @Test
        @DisplayName("of with map should create context with bindings")
        void ofWithMapShouldCreateContextWithBindings() {
            Map<String, String> bindings = Map.of("ns", "http://example.com");

            OpenNamespaceContext context = OpenNamespaceContext.of(bindings);

            assertThat(context.getNamespaceURI("ns")).isEqualTo("http://example.com");
        }

        @Test
        @DisplayName("of with prefix and uri should create context")
        void ofWithPrefixAndUriShouldCreateContext() {
            OpenNamespaceContext context = OpenNamespaceContext.of("ns", "http://example.com");

            assertThat(context.getNamespaceURI("ns")).isEqualTo("http://example.com");
        }
    }

    @Nested
    @DisplayName("Bind Tests")
    class BindTests {

        @Test
        @DisplayName("bind should add prefix binding")
        void bindShouldAddPrefixBinding() {
            OpenNamespaceContext context = OpenNamespaceContext.create()
                .bind("ns", "http://example.com");

            assertThat(context.getNamespaceURI("ns")).isEqualTo("http://example.com");
        }

        @Test
        @DisplayName("bind should allow chaining")
        void bindShouldAllowChaining() {
            OpenNamespaceContext context = OpenNamespaceContext.create()
                .bind("a", "http://a.com")
                .bind("b", "http://b.com")
                .bind("c", "http://c.com");

            assertThat(context.getNamespaceURI("a")).isEqualTo("http://a.com");
            assertThat(context.getNamespaceURI("b")).isEqualTo("http://b.com");
            assertThat(context.getNamespaceURI("c")).isEqualTo("http://c.com");
        }
    }

    @Nested
    @DisplayName("Set Default Namespace Tests")
    class SetDefaultNamespaceTests {

        @Test
        @DisplayName("setDefaultNamespace should set default namespace")
        void setDefaultNamespaceShouldSetDefaultNamespace() {
            OpenNamespaceContext context = OpenNamespaceContext.create()
                .setDefaultNamespace("http://default.com");

            assertThat(context.getNamespaceURI("")).isEqualTo("http://default.com");
        }
    }

    @Nested
    @DisplayName("Unbind Tests")
    class UnbindTests {

        @Test
        @DisplayName("unbind should remove prefix binding")
        void unbindShouldRemovePrefixBinding() {
            OpenNamespaceContext context = OpenNamespaceContext.create()
                .bind("ns", "http://example.com")
                .unbind("ns");

            assertThat(context.isBound("ns")).isFalse();
        }
    }

    @Nested
    @DisplayName("IsBound Tests")
    class IsBoundTests {

        @Test
        @DisplayName("isBound should return true for bound prefix")
        void isBoundShouldReturnTrueForBoundPrefix() {
            OpenNamespaceContext context = OpenNamespaceContext.create()
                .bind("ns", "http://example.com");

            assertThat(context.isBound("ns")).isTrue();
        }

        @Test
        @DisplayName("isBound should return false for unbound prefix")
        void isBoundShouldReturnFalseForUnboundPrefix() {
            OpenNamespaceContext context = OpenNamespaceContext.create();

            assertThat(context.isBound("ns")).isFalse();
        }
    }

    @Nested
    @DisplayName("GetNamespaceURI Tests")
    class GetNamespaceURITests {

        @Test
        @DisplayName("getNamespaceURI should return URI for bound prefix")
        void getNamespaceURIShouldReturnUriForBoundPrefix() {
            OpenNamespaceContext context = OpenNamespaceContext.create()
                .bind("ns", "http://example.com");

            assertThat(context.getNamespaceURI("ns")).isEqualTo("http://example.com");
        }

        @Test
        @DisplayName("getNamespaceURI should return empty string for unbound prefix")
        void getNamespaceURIShouldReturnEmptyStringForUnboundPrefix() {
            OpenNamespaceContext context = OpenNamespaceContext.create();

            // Per NamespaceContext contract, returns NULL_NS_URI (empty string) for unbound prefix
            assertThat(context.getNamespaceURI("unknown")).isEqualTo("");
        }

        @Test
        @DisplayName("getNamespaceURI should return XML namespace for xml prefix")
        void getNamespaceURIShouldReturnXmlNamespaceForXmlPrefix() {
            OpenNamespaceContext context = OpenNamespaceContext.create();

            assertThat(context.getNamespaceURI("xml"))
                .isEqualTo("http://www.w3.org/XML/1998/namespace");
        }

        @Test
        @DisplayName("getNamespaceURI should return XMLNS namespace for xmlns prefix")
        void getNamespaceURIShouldReturnXmlnsNamespaceForXmlnsPrefix() {
            OpenNamespaceContext context = OpenNamespaceContext.create();

            assertThat(context.getNamespaceURI("xmlns"))
                .isEqualTo("http://www.w3.org/2000/xmlns/");
        }
    }

    @Nested
    @DisplayName("GetPrefix Tests")
    class GetPrefixTests {

        @Test
        @DisplayName("getPrefix should return prefix for bound URI")
        void getPrefixShouldReturnPrefixForBoundUri() {
            OpenNamespaceContext context = OpenNamespaceContext.create()
                .bind("ns", "http://example.com");

            assertThat(context.getPrefix("http://example.com")).isEqualTo("ns");
        }

        @Test
        @DisplayName("getPrefix should return null for unknown URI")
        void getPrefixShouldReturnNullForUnknownUri() {
            OpenNamespaceContext context = OpenNamespaceContext.create();

            assertThat(context.getPrefix("http://unknown.com")).isNull();
        }
    }

    @Nested
    @DisplayName("GetPrefixes Tests")
    class GetPrefixesTests {

        @Test
        @DisplayName("getPrefixes should return all prefixes for URI")
        void getPrefixesShouldReturnAllPrefixesForUri() {
            OpenNamespaceContext context = OpenNamespaceContext.create()
                .bind("a", "http://example.com")
                .bind("b", "http://example.com");

            Iterator<String> prefixes = context.getPrefixes("http://example.com");

            assertThat(prefixes).toIterable().contains("a", "b");
        }

        @Test
        @DisplayName("getPrefixes should return empty iterator for unknown URI")
        void getPrefixesShouldReturnEmptyIteratorForUnknownUri() {
            OpenNamespaceContext context = OpenNamespaceContext.create();

            Iterator<String> prefixes = context.getPrefixes("http://unknown.com");

            assertThat(prefixes).toIterable().isEmpty();
        }
    }

    @Nested
    @DisplayName("GetBindings Tests")
    class GetBindingsTests {

        @Test
        @DisplayName("getBindings should return all bindings")
        void getBindingsShouldReturnAllBindings() {
            OpenNamespaceContext context = OpenNamespaceContext.create()
                .bind("a", "http://a.com")
                .bind("b", "http://b.com");

            Map<String, String> bindings = context.getBindings();

            assertThat(bindings).containsEntry("a", "http://a.com");
            assertThat(bindings).containsEntry("b", "http://b.com");
        }
    }

    @Nested
    @DisplayName("NamespaceContext Interface Tests")
    class NamespaceContextInterfaceTests {

        @Test
        @DisplayName("should implement NamespaceContext interface")
        void shouldImplementNamespaceContextInterface() {
            OpenNamespaceContext context = OpenNamespaceContext.create();

            assertThat(context).isInstanceOf(NamespaceContext.class);
        }
    }
}
