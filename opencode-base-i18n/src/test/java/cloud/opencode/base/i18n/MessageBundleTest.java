package cloud.opencode.base.i18n;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * MessageBundle 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@DisplayName("MessageBundle 接口测试")
class MessageBundleTest {

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("getMessage方法存在")
        void testGetMessageExists() throws NoSuchMethodException {
            var method = MessageBundle.class.getMethod("getMessage", String.class);

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("containsKey方法存在")
        void testContainsKeyExists() throws NoSuchMethodException {
            var method = MessageBundle.class.getMethod("containsKey", String.class);

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("getKeys方法存在")
        void testGetKeysExists() throws NoSuchMethodException {
            var method = MessageBundle.class.getMethod("getKeys");

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Set.class);
        }

        @Test
        @DisplayName("toMap方法存在")
        void testToMapExists() throws NoSuchMethodException {
            var method = MessageBundle.class.getMethod("toMap");

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Map.class);
        }

        @Test
        @DisplayName("getLocale方法存在")
        void testGetLocaleExists() throws NoSuchMethodException {
            var method = MessageBundle.class.getMethod("getLocale");

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Locale.class);
        }

        @Test
        @DisplayName("getParent方法存在")
        void testGetParentExists() throws NoSuchMethodException {
            var method = MessageBundle.class.getMethod("getParent");

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(MessageBundle.class);
        }
    }

    @Nested
    @DisplayName("实现测试")
    class ImplementationTests {

        private MessageBundle createMockBundle() {
            return new MessageBundle() {
                private final Map<String, String> messages = Map.of(
                        "greeting", "Hello",
                        "farewell", "Goodbye"
                );

                @Override
                public String getMessage(String key) {
                    return messages.get(key);
                }

                @Override
                public boolean containsKey(String key) {
                    return messages.containsKey(key);
                }

                @Override
                public Set<String> getKeys() {
                    return messages.keySet();
                }

                @Override
                public Map<String, String> toMap() {
                    return Map.copyOf(messages);
                }

                @Override
                public Locale getLocale() {
                    return Locale.ENGLISH;
                }

                @Override
                public MessageBundle getParent() {
                    return null;
                }
            };
        }

        @Test
        @DisplayName("getMessage返回消息")
        void testGetMessage() {
            MessageBundle bundle = createMockBundle();

            String result = bundle.getMessage("greeting");

            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("getMessage不存在返回null")
        void testGetMessageNotExists() {
            MessageBundle bundle = createMockBundle();

            String result = bundle.getMessage("nonexistent");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("containsKey存在返回true")
        void testContainsKeyTrue() {
            MessageBundle bundle = createMockBundle();

            assertThat(bundle.containsKey("greeting")).isTrue();
        }

        @Test
        @DisplayName("containsKey不存在返回false")
        void testContainsKeyFalse() {
            MessageBundle bundle = createMockBundle();

            assertThat(bundle.containsKey("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("getKeys返回所有键")
        void testGetKeys() {
            MessageBundle bundle = createMockBundle();

            Set<String> keys = bundle.getKeys();

            assertThat(keys).containsExactlyInAnyOrder("greeting", "farewell");
        }

        @Test
        @DisplayName("toMap返回Map")
        void testToMap() {
            MessageBundle bundle = createMockBundle();

            Map<String, String> map = bundle.toMap();

            assertThat(map).containsEntry("greeting", "Hello");
            assertThat(map).containsEntry("farewell", "Goodbye");
        }

        @Test
        @DisplayName("getLocale返回Locale")
        void testGetLocale() {
            MessageBundle bundle = createMockBundle();

            Locale locale = bundle.getLocale();

            assertThat(locale).isEqualTo(Locale.ENGLISH);
        }

        @Test
        @DisplayName("getParent返回父Bundle")
        void testGetParent() {
            MessageBundle bundle = createMockBundle();

            MessageBundle parent = bundle.getParent();

            assertThat(parent).isNull();
        }
    }

    @Nested
    @DisplayName("父Bundle测试")
    class ParentBundleTests {

        @Test
        @DisplayName("带父Bundle的实现")
        void testWithParentBundle() {
            MessageBundle parent = new MessageBundle() {
                @Override
                public String getMessage(String key) {
                    return "parent_" + key;
                }

                @Override
                public boolean containsKey(String key) {
                    return true;
                }

                @Override
                public Set<String> getKeys() {
                    return Set.of("parent.key");
                }

                @Override
                public Map<String, String> toMap() {
                    return Map.of("parent.key", "parent_value");
                }

                @Override
                public Locale getLocale() {
                    return Locale.ENGLISH;
                }

                @Override
                public MessageBundle getParent() {
                    return null;
                }
            };

            MessageBundle child = new MessageBundle() {
                @Override
                public String getMessage(String key) {
                    if ("child.key".equals(key)) {
                        return "child_value";
                    }
                    return parent.getMessage(key);
                }

                @Override
                public boolean containsKey(String key) {
                    return "child.key".equals(key) || parent.containsKey(key);
                }

                @Override
                public Set<String> getKeys() {
                    Set<String> keys = new java.util.HashSet<>(parent.getKeys());
                    keys.add("child.key");
                    return keys;
                }

                @Override
                public Map<String, String> toMap() {
                    Map<String, String> map = new java.util.HashMap<>(parent.toMap());
                    map.put("child.key", "child_value");
                    return map;
                }

                @Override
                public Locale getLocale() {
                    return Locale.US;
                }

                @Override
                public MessageBundle getParent() {
                    return parent;
                }
            };

            assertThat(child.getParent()).isEqualTo(parent);
            assertThat(child.getMessage("child.key")).isEqualTo("child_value");
            assertThat(child.getMessage("parent.key")).isEqualTo("parent_parent.key");
            assertThat(child.getKeys()).containsExactlyInAnyOrder("child.key", "parent.key");
        }
    }
}
