package cloud.opencode.base.i18n.fallback;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ChainedLocaleFallback
 */
@DisplayName("ChainedLocaleFallback")
class ChainedLocaleFallbackTest {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final Locale PT_PT = Locale.of("pt", "PT");
    private static final Locale ES    = Locale.of("es");
    private static final Locale FR_CA = Locale.of("fr", "CA");
    private static final Locale FR    = Locale.of("fr");

    @Nested
    @DisplayName("Exact locale match")
    class ExactMatch {
        @Test
        void exactMatchUsesConfiguredChain() {
            ChainedLocaleFallback fb = ChainedLocaleFallback.builder()
                    .chain(PT_BR, PT_PT, ES, Locale.ENGLISH)
                    .build();
            List<Locale> chain = fb.getFallbackChain(PT_BR);
            assertThat(chain).containsExactly(PT_BR, PT_PT, ES, Locale.ENGLISH);
        }
    }

    @Nested
    @DisplayName("Language-only fallback")
    class LanguageOnly {
        @Test
        void regionLocaleMatchesLanguageChain() {
            ChainedLocaleFallback fb = ChainedLocaleFallback.builder()
                    .chain(FR_CA, FR, Locale.ENGLISH)
                    .build();
            // fr-DE has no exact match, but language "fr" matches fr-CA's chain
            Locale frDE = Locale.of("fr", "DE");
            List<Locale> chain = fb.getFallbackChain(frDE);
            assertThat(chain).containsExactly(FR_CA, FR, Locale.ENGLISH);
        }
    }

    @Nested
    @DisplayName("Default chain when no match")
    class DefaultChain {
        @Test
        void defaultChainIncludesLanguageAndUltimate() {
            ChainedLocaleFallback fb = ChainedLocaleFallback.builder()
                    .ultimateFallback(Locale.ENGLISH)
                    .build();
            List<Locale> chain = fb.getFallbackChain(Locale.of("de", "AT"));
            assertThat(chain).containsExactly(Locale.of("de", "AT"), Locale.of("de"), Locale.ENGLISH);
        }

        @Test
        void languageOnlyLocaleDefaultChain() {
            ChainedLocaleFallback fb = ChainedLocaleFallback.builder()
                    .ultimateFallback(Locale.ENGLISH)
                    .build();
            List<Locale> chain = fb.getFallbackChain(Locale.of("ja"));
            assertThat(chain).containsExactly(Locale.of("ja"), Locale.ENGLISH);
        }

        @Test
        void noUltimateFallback() {
            ChainedLocaleFallback fb = ChainedLocaleFallback.builder().build();
            List<Locale> chain = fb.getFallbackChain(Locale.ENGLISH);
            assertThat(chain).containsExactly(Locale.ENGLISH);
        }
    }

    @Nested
    @DisplayName("Builder")
    class Builder {
        @Test
        void multipleChains() {
            ChainedLocaleFallback fb = ChainedLocaleFallback.builder()
                    .chain(PT_BR, PT_PT, ES, Locale.ENGLISH)
                    .chain(FR_CA, FR, Locale.ENGLISH)
                    .ultimateFallback(Locale.ENGLISH)
                    .build();
            assertThat(fb.getFallbackChain(PT_BR)).containsExactly(PT_BR, PT_PT, ES, Locale.ENGLISH);
            assertThat(fb.getFallbackChain(FR_CA)).containsExactly(FR_CA, FR, Locale.ENGLISH);
        }

        @Test
        void nullSourceThrows() {
            assertThatNullPointerException().isThrownBy(() ->
                    ChainedLocaleFallback.builder().chain(null, Locale.ENGLISH).build());
        }
    }
}
