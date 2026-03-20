package cloud.opencode.base.i18n.spi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MessageBundleProvider")
class MessageBundleProviderTest {

    @Nested
    @DisplayName("baseName")
    class BaseName {

        @Test
        @DisplayName("should return configured base name from implementation")
        void shouldReturnBaseName() {
            MessageBundleProvider provider = new MessageBundleProvider() {
                @Override
                public String baseName() {
                    return "my-messages";
                }
            };
            assertThat(provider.baseName()).isEqualTo("my-messages");
        }

        @Test
        @DisplayName("should allow different base names per implementation")
        void shouldAllowDifferentBaseNames() {
            MessageBundleProvider provider1 = () -> "messages-core";
            MessageBundleProvider provider2 = () -> "messages-web";
            assertThat(provider1.baseName()).isNotEqualTo(provider2.baseName());
        }
    }

    @Nested
    @DisplayName("priority (default method)")
    class Priority {

        @Test
        @DisplayName("should return 0 as default priority")
        void shouldReturnDefaultPriority() {
            MessageBundleProvider provider = () -> "test-bundle";
            assertThat(provider.priority()).isEqualTo(0);
        }

        @Test
        @DisplayName("should allow overriding priority")
        void shouldAllowOverridingPriority() {
            MessageBundleProvider provider = new MessageBundleProvider() {
                @Override
                public String baseName() {
                    return "high-priority";
                }

                @Override
                public int priority() {
                    return -10;
                }
            };
            assertThat(provider.priority()).isEqualTo(-10);
        }

        @Test
        @DisplayName("should allow positive priority")
        void shouldAllowPositivePriority() {
            MessageBundleProvider provider = new MessageBundleProvider() {
                @Override
                public String baseName() {
                    return "low-priority";
                }

                @Override
                public int priority() {
                    return 100;
                }
            };
            assertThat(provider.priority()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("lambda implementation")
    class LambdaImplementation {

        @Test
        @DisplayName("should work as functional interface for baseName")
        void shouldWorkAsLambda() {
            MessageBundleProvider provider = () -> "lambda-bundle";
            assertThat(provider.baseName()).isEqualTo("lambda-bundle");
            assertThat(provider.priority()).isEqualTo(0);
        }
    }
}
