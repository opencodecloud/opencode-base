package cloud.opencode.base.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * ConfigChangeEvent 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("ConfigChangeEvent 测试")
class ConfigChangeEventTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建变更事件")
        void testConstructor() {
            Instant now = Instant.now();
            ConfigChangeEvent event = new ConfigChangeEvent(
                    "test.key", "oldValue", "newValue",
                    ConfigChangeEvent.ChangeType.MODIFIED, now);

            assertThat(event.key()).isEqualTo("test.key");
            assertThat(event.oldValue()).isEqualTo("oldValue");
            assertThat(event.newValue()).isEqualTo("newValue");
            assertThat(event.changeType()).isEqualTo(ConfigChangeEvent.ChangeType.MODIFIED);
            assertThat(event.timestamp()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("added - 创建添加事件")
        void testAdded() {
            ConfigChangeEvent event = ConfigChangeEvent.added("new.key", "value");

            assertThat(event.key()).isEqualTo("new.key");
            assertThat(event.oldValue()).isNull();
            assertThat(event.newValue()).isEqualTo("value");
            assertThat(event.changeType()).isEqualTo(ConfigChangeEvent.ChangeType.ADDED);
            assertThat(event.timestamp()).isNotNull();
        }

        @Test
        @DisplayName("modified - 创建修改事件")
        void testModified() {
            ConfigChangeEvent event = ConfigChangeEvent.modified("key", "old", "new");

            assertThat(event.key()).isEqualTo("key");
            assertThat(event.oldValue()).isEqualTo("old");
            assertThat(event.newValue()).isEqualTo("new");
            assertThat(event.changeType()).isEqualTo(ConfigChangeEvent.ChangeType.MODIFIED);
            assertThat(event.timestamp()).isNotNull();
        }

        @Test
        @DisplayName("removed - 创建删除事件")
        void testRemoved() {
            ConfigChangeEvent event = ConfigChangeEvent.removed("old.key", "value");

            assertThat(event.key()).isEqualTo("old.key");
            assertThat(event.oldValue()).isEqualTo("value");
            assertThat(event.newValue()).isNull();
            assertThat(event.changeType()).isEqualTo(ConfigChangeEvent.ChangeType.REMOVED);
            assertThat(event.timestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("便捷方法测试")
    class ConvenienceMethodTests {

        @Test
        @DisplayName("isAdded - 检查添加事件")
        void testIsAdded() {
            ConfigChangeEvent addedEvent = ConfigChangeEvent.added("key", "value");
            ConfigChangeEvent modifiedEvent = ConfigChangeEvent.modified("key", "old", "new");

            assertThat(addedEvent.isAdded()).isTrue();
            assertThat(addedEvent.isModified()).isFalse();
            assertThat(addedEvent.isRemoved()).isFalse();

            assertThat(modifiedEvent.isAdded()).isFalse();
        }

        @Test
        @DisplayName("isModified - 检查修改事件")
        void testIsModified() {
            ConfigChangeEvent modifiedEvent = ConfigChangeEvent.modified("key", "old", "new");

            assertThat(modifiedEvent.isModified()).isTrue();
            assertThat(modifiedEvent.isAdded()).isFalse();
            assertThat(modifiedEvent.isRemoved()).isFalse();
        }

        @Test
        @DisplayName("isRemoved - 检查删除事件")
        void testIsRemoved() {
            ConfigChangeEvent removedEvent = ConfigChangeEvent.removed("key", "value");

            assertThat(removedEvent.isRemoved()).isTrue();
            assertThat(removedEvent.isAdded()).isFalse();
            assertThat(removedEvent.isModified()).isFalse();
        }
    }

    @Nested
    @DisplayName("ChangeType枚举测试")
    class ChangeTypeTests {

        @Test
        @DisplayName("ChangeType - 枚举值")
        void testChangeTypeValues() {
            assertThat(ConfigChangeEvent.ChangeType.values())
                    .containsExactly(
                            ConfigChangeEvent.ChangeType.ADDED,
                            ConfigChangeEvent.ChangeType.MODIFIED,
                            ConfigChangeEvent.ChangeType.REMOVED);
        }

        @Test
        @DisplayName("ChangeType - valueOf")
        void testChangeTypeValueOf() {
            assertThat(ConfigChangeEvent.ChangeType.valueOf("ADDED"))
                    .isEqualTo(ConfigChangeEvent.ChangeType.ADDED);
            assertThat(ConfigChangeEvent.ChangeType.valueOf("MODIFIED"))
                    .isEqualTo(ConfigChangeEvent.ChangeType.MODIFIED);
            assertThat(ConfigChangeEvent.ChangeType.valueOf("REMOVED"))
                    .isEqualTo(ConfigChangeEvent.ChangeType.REMOVED);
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString - 包含 key 和 changeType，不泄露值")
        void testToString() {
            ConfigChangeEvent event = ConfigChangeEvent.modified("key", "old", "new");

            String str = event.toString();
            assertThat(str).contains("ConfigChangeEvent");
            assertThat(str).contains("key='key'");
            assertThat(str).contains("MODIFIED");
            // Values are redacted from toString() to prevent sensitive data leakage
            assertThat(str).doesNotContain("old");
            assertThat(str).doesNotContain("new");
        }
    }
}
