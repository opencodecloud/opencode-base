package cloud.opencode.base.web.internal;

import cloud.opencode.base.web.context.RequestContext;
import cloud.opencode.base.web.context.RequestContextHolder;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TraceIdResolverTest Tests
 * TraceIdResolverTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("TraceIdResolver Tests")
class TraceIdResolverTest {

    @BeforeEach
    void setUp() {
        RequestContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Nested
    @DisplayName("Resolve Tests")
    class ResolveTests {

        @Test
        @DisplayName("resolve should return trace ID from context")
        void resolveShouldReturnTraceIdFromContext() {
            RequestContext context = RequestContext.of("trace-123");
            RequestContextHolder.setContext(context);

            String traceId = TraceIdResolver.resolve();

            assertThat(traceId).isEqualTo("trace-123");
        }

        @Test
        @DisplayName("resolve should generate new trace ID when no context")
        void resolveShouldGenerateNewTraceIdWhenNoContext() {
            String traceId = TraceIdResolver.resolve();

            assertThat(traceId).isNotNull();
            assertThat(traceId).hasSize(32);
        }

        @Test
        @DisplayName("resolve should generate new trace ID when context has null trace ID")
        void resolveShouldGenerateNewTraceIdWhenContextHasNullTraceId() {
            RequestContext context = RequestContext.builder().build();
            RequestContextHolder.setContext(context);

            String traceId = TraceIdResolver.resolve();

            assertThat(traceId).isNotNull();
            assertThat(traceId).hasSize(32);
        }
    }

    @Nested
    @DisplayName("Generate Trace ID Tests")
    class GenerateTraceIdTests {

        @Test
        @DisplayName("generateTraceId should generate valid trace ID")
        void generateTraceIdShouldGenerateValidTraceId() {
            String traceId = TraceIdResolver.generateTraceId();

            assertThat(traceId).isNotNull();
            assertThat(traceId).hasSize(32);
            assertThat(traceId).matches("[a-f0-9]+");
        }

        @Test
        @DisplayName("generateTraceId should generate unique trace IDs")
        void generateTraceIdShouldGenerateUniqueTraceIds() {
            String traceId1 = TraceIdResolver.generateTraceId();
            String traceId2 = TraceIdResolver.generateTraceId();

            assertThat(traceId1).isNotEqualTo(traceId2);
        }
    }
}
