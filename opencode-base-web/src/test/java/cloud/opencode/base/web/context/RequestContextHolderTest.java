package cloud.opencode.base.web.context;

import org.junit.jupiter.api.*;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

/**
 * RequestContextHolderTest Tests
 * RequestContextHolderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("RequestContextHolder Tests")
class RequestContextHolderTest {

    @BeforeEach
    void setUp() {
        RequestContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Nested
    @DisplayName("Basic Operations Tests")
    class BasicOperationsTests {

        @Test
        @DisplayName("setContext should set context")
        void setContextShouldSetContext() {
            RequestContext context = RequestContext.of("trace-123");

            RequestContextHolder.setContext(context);

            assertThat(RequestContextHolder.getContext()).isEqualTo(context);
        }

        @Test
        @DisplayName("setContext with null should clear context")
        void setContextWithNullShouldClearContext() {
            RequestContextHolder.setContext(RequestContext.of("trace-123"));

            RequestContextHolder.setContext(null);

            assertThat(RequestContextHolder.getContext()).isNull();
        }

        @Test
        @DisplayName("getContext should return null when not set")
        void getContextShouldReturnNullWhenNotSet() {
            assertThat(RequestContextHolder.getContext()).isNull();
        }

        @Test
        @DisplayName("clear should remove context")
        void clearShouldRemoveContext() {
            RequestContextHolder.setContext(RequestContext.of("trace-123"));

            RequestContextHolder.clear();

            assertThat(RequestContextHolder.getContext()).isNull();
        }

        @Test
        @DisplayName("hasContext should return true when context is set")
        void hasContextShouldReturnTrueWhenContextIsSet() {
            RequestContextHolder.setContext(RequestContext.of("trace-123"));

            assertThat(RequestContextHolder.hasContext()).isTrue();
        }

        @Test
        @DisplayName("hasContext should return false when context is not set")
        void hasContextShouldReturnFalseWhenContextIsNotSet() {
            assertThat(RequestContextHolder.hasContext()).isFalse();
        }
    }

    @Nested
    @DisplayName("Get Or Create Tests")
    class GetOrCreateTests {

        @Test
        @DisplayName("getOrCreate should return existing context")
        void getOrCreateShouldReturnExistingContext() {
            RequestContext existing = RequestContext.of("existing-trace");
            RequestContextHolder.setContext(existing);

            RequestContext result = RequestContextHolder.getOrCreate(
                () -> RequestContext.of("new-trace")
            );

            assertThat(result).isEqualTo(existing);
        }

        @Test
        @DisplayName("getOrCreate should create context when not set")
        void getOrCreateShouldCreateContextWhenNotSet() {
            RequestContext result = RequestContextHolder.getOrCreate(
                () -> RequestContext.of("new-trace")
            );

            assertThat(result.traceId()).isEqualTo("new-trace");
            assertThat(RequestContextHolder.getContext()).isEqualTo(result);
        }
    }

    @Nested
    @DisplayName("Shortcut Methods Tests")
    class ShortcutMethodsTests {

        @Test
        @DisplayName("getTraceId should return trace ID from context")
        void getTraceIdShouldReturnTraceIdFromContext() {
            RequestContextHolder.setContext(RequestContext.of("trace-123"));

            assertThat(RequestContextHolder.getTraceId()).isEqualTo("trace-123");
        }

        @Test
        @DisplayName("getTraceId should return null when no context")
        void getTraceIdShouldReturnNullWhenNoContext() {
            assertThat(RequestContextHolder.getTraceId()).isNull();
        }

        @Test
        @DisplayName("getUser should return user from context")
        void getUserShouldReturnUserFromContext() {
            UserContext user = UserContext.of("user1", "testuser");
            RequestContext context = RequestContext.builder()
                .traceId("trace")
                .user(user)
                .build();
            RequestContextHolder.setContext(context);

            assertThat(RequestContextHolder.getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("getUser should return null when no context")
        void getUserShouldReturnNullWhenNoContext() {
            assertThat(RequestContextHolder.getUser()).isNull();
        }

        @Test
        @DisplayName("getUserId should return user ID from context")
        void getUserIdShouldReturnUserIdFromContext() {
            UserContext user = UserContext.of("user1", "testuser");
            RequestContext context = RequestContext.builder()
                .traceId("trace")
                .user(user)
                .build();
            RequestContextHolder.setContext(context);

            assertThat(RequestContextHolder.getUserId()).isEqualTo("user1");
        }

        @Test
        @DisplayName("getUserId should return null when no user")
        void getUserIdShouldReturnNullWhenNoUser() {
            assertThat(RequestContextHolder.getUserId()).isNull();
        }
    }

    @Nested
    @DisplayName("Wrap Runnable Tests")
    class WrapRunnableTests {

        @Test
        @DisplayName("wrap should preserve context in runnable")
        void wrapShouldPreserveContextInRunnable() {
            RequestContext context = RequestContext.of("trace-123");
            RequestContextHolder.setContext(context);
            AtomicReference<String> captured = new AtomicReference<>();

            Runnable wrapped = RequestContextHolder.wrap(() -> {
                captured.set(RequestContextHolder.getTraceId());
            });

            RequestContextHolder.clear();
            wrapped.run();

            assertThat(captured.get()).isEqualTo("trace-123");
        }

        @Test
        @DisplayName("wrap should restore previous context after execution")
        void wrapShouldRestorePreviousContextAfterExecution() {
            RequestContext originalContext = RequestContext.of("original");
            RequestContext wrappedContext = RequestContext.of("wrapped");

            RequestContextHolder.setContext(wrappedContext);
            Runnable wrapped = RequestContextHolder.wrap(() -> {});

            RequestContextHolder.setContext(originalContext);
            wrapped.run();

            assertThat(RequestContextHolder.getTraceId()).isEqualTo("original");
        }
    }

    @Nested
    @DisplayName("Wrap Callable Tests")
    class WrapCallableTests {

        @Test
        @DisplayName("wrap should preserve context in callable")
        void wrapShouldPreserveContextInCallable() throws Exception {
            RequestContext context = RequestContext.of("trace-123");
            RequestContextHolder.setContext(context);

            Callable<String> callable = () -> RequestContextHolder.getTraceId();
            Callable<String> wrapped = RequestContextHolder.wrap(callable);

            RequestContextHolder.clear();
            String result = wrapped.call();

            assertThat(result).isEqualTo("trace-123");
        }
    }

    @Nested
    @DisplayName("Wrap Supplier Tests")
    class WrapSupplierTests {

        @Test
        @DisplayName("wrap should preserve context in supplier")
        void wrapShouldPreserveContextInSupplier() {
            RequestContext context = RequestContext.of("trace-123");
            RequestContextHolder.setContext(context);

            Supplier<String> supplier = () -> RequestContextHolder.getTraceId();
            Supplier<String> wrapped = RequestContextHolder.wrap(supplier);

            RequestContextHolder.clear();
            String result = wrapped.get();

            assertThat(result).isEqualTo("trace-123");
        }
    }

    @Nested
    @DisplayName("Execute With Context Tests")
    class ExecuteWithContextTests {

        @Test
        @DisplayName("execute should run runnable with context")
        void executeShouldRunRunnableWithContext() {
            RequestContext context = RequestContext.of("trace-123");
            AtomicReference<String> captured = new AtomicReference<>();

            RequestContextHolder.execute(context, () -> {
                captured.set(RequestContextHolder.getTraceId());
            });

            assertThat(captured.get()).isEqualTo("trace-123");
        }

        @Test
        @DisplayName("execute should restore context after runnable")
        void executeShouldRestoreContextAfterRunnable() {
            RequestContext original = RequestContext.of("original");
            RequestContext temporary = RequestContext.of("temporary");
            RequestContextHolder.setContext(original);

            RequestContextHolder.execute(temporary, () -> {});

            assertThat(RequestContextHolder.getTraceId()).isEqualTo("original");
        }

        @Test
        @DisplayName("execute should run supplier with context and return value")
        void executeShouldRunSupplierWithContextAndReturnValue() {
            RequestContext context = RequestContext.of("trace-123");

            String result = RequestContextHolder.execute(context,
                () -> RequestContextHolder.getTraceId()
            );

            assertThat(result).isEqualTo("trace-123");
        }

        @Test
        @DisplayName("execute should clear context if no previous context")
        void executeShouldClearContextIfNoPreviousContext() {
            RequestContext context = RequestContext.of("trace-123");

            RequestContextHolder.execute(context, () -> {});

            assertThat(RequestContextHolder.getContext()).isNull();
        }
    }
}
