package cloud.opencode.base.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ConnectionTestResult")
class ConnectionTestResultTest {

    @Test
    @DisplayName("success result should have correct fields")
    void successResult() {
        ConnectionTestResult result = ConnectionTestResult.success("220 smtp.example.com", Duration.ofMillis(150));
        assertThat(result.success()).isTrue();
        assertThat(result.serverGreeting()).isEqualTo("220 smtp.example.com");
        assertThat(result.latency()).isEqualTo(Duration.ofMillis(150));
        assertThat(result.errorMessage()).isNull();
        assertThat(result.cause()).isNull();
    }

    @Test
    @DisplayName("failure result should have correct fields")
    void failureResult() {
        RuntimeException cause = new RuntimeException("Connection refused");
        ConnectionTestResult result = ConnectionTestResult.failure("Connection refused", cause, Duration.ofMillis(5000));
        assertThat(result.success()).isFalse();
        assertThat(result.serverGreeting()).isNull();
        assertThat(result.latency()).isEqualTo(Duration.ofMillis(5000));
        assertThat(result.errorMessage()).isEqualTo("Connection refused");
        assertThat(result.cause()).isEqualTo(cause);
    }
}
