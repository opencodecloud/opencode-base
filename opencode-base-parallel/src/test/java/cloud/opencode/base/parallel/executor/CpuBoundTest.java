package cloud.opencode.base.parallel.executor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link CpuBound} marker interface.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("CpuBound")
class CpuBoundTest {

    @Nested
    @DisplayName("interface properties")
    class InterfaceProperties {

        @Test
        @DisplayName("should extend Runnable")
        void shouldExtendRunnable() {
            assertThat(Runnable.class.isAssignableFrom(CpuBound.class)).isTrue();
        }

        @Test
        @DisplayName("should be a functional interface")
        void shouldBeFunctionalInterface() {
            assertThat(CpuBound.class.isAnnotationPresent(FunctionalInterface.class)).isTrue();
        }

        @Test
        @DisplayName("should be an interface")
        void shouldBeAnInterface() {
            assertThat(CpuBound.class.isInterface()).isTrue();
        }
    }

    @Nested
    @DisplayName("lambda instantiation")
    class LambdaInstantiation {

        @Test
        @DisplayName("should be instantiable as lambda")
        void shouldBeInstantiableAsLambda() {
            CpuBound task = () -> {};
            assertThat(task).isNotNull();
            assertThat(task).isInstanceOf(Runnable.class);
            assertThat(task).isInstanceOf(CpuBound.class);
        }

        @Test
        @DisplayName("lambda should execute run method")
        void lambdaShouldExecute() {
            AtomicBoolean executed = new AtomicBoolean(false);
            CpuBound task = () -> executed.set(true);
            task.run();
            assertThat(executed.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("instanceof checks")
    class InstanceOfChecks {

        @Test
        @DisplayName("CpuBound instance should be instanceof Runnable")
        void shouldBeInstanceOfRunnable() {
            CpuBound task = () -> {};
            assertThat(task instanceof Runnable).isTrue();
        }

        @Test
        @DisplayName("CpuBound instance should be detectable via instanceof")
        void shouldBeDetectable() {
            Runnable task = (CpuBound) () -> {};
            assertThat(task instanceof CpuBound).isTrue();
        }

        @Test
        @DisplayName("regular Runnable should not be instanceof CpuBound")
        void regularRunnableShouldNotBeCpuBound() {
            Runnable task = () -> {};
            assertThat(task instanceof CpuBound).isFalse();
        }
    }

    @Nested
    @DisplayName("anonymous class instantiation")
    class AnonymousClassInstantiation {

        @Test
        @DisplayName("should work as anonymous class")
        void shouldWorkAsAnonymousClass() {
            AtomicBoolean executed = new AtomicBoolean(false);
            CpuBound task = new CpuBound() {
                @Override
                public void run() {
                    executed.set(true);
                }
            };
            task.run();
            assertThat(executed.get()).isTrue();
            assertThat(task).isInstanceOf(CpuBound.class);
            assertThat(task).isInstanceOf(Runnable.class);
        }
    }
}
