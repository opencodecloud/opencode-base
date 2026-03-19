package cloud.opencode.base.id.uuid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * UuidGenerator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("UuidGenerator 测试")
class UuidGeneratorTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("v4工厂方法")
        void testV4() {
            UuidGenerator gen = UuidGenerator.v4();

            assertThat(gen).isNotNull();
            assertThat(gen.getVersion()).isEqualTo(4);
        }

        @Test
        @DisplayName("v7工厂方法")
        void testV7() {
            UuidGenerator gen = UuidGenerator.v7();

            assertThat(gen).isNotNull();
            assertThat(gen.getVersion()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("生成方法测试")
    class GenerateTests {

        @Test
        @DisplayName("生成UUID v4")
        void testGenerateV4() {
            UuidGenerator gen = UuidGenerator.v4();

            UUID uuid = gen.generate();

            assertThat(uuid).isNotNull();
            assertThat(uuid.version()).isEqualTo(4);
        }

        @Test
        @DisplayName("生成UUID v7")
        void testGenerateV7() {
            UuidGenerator gen = UuidGenerator.v7();

            UUID uuid = gen.generate();

            assertThat(uuid).isNotNull();
            assertThat(uuid.version()).isEqualTo(7);
        }

        @Test
        @DisplayName("生成字符串")
        void testGenerateStr() {
            UuidGenerator gen = UuidGenerator.v4();

            String uuidStr = gen.generateStr();

            assertThat(uuidStr).isNotNull();
            assertThat(uuidStr).hasSize(36);
            assertThat(uuidStr).contains("-");
        }

        @Test
        @DisplayName("生成简化字符串")
        void testGenerateSimple() {
            UuidGenerator gen = UuidGenerator.v4();

            String simple = gen.generateSimple();

            assertThat(simple).isNotNull();
            assertThat(simple).hasSize(32);
            assertThat(simple).doesNotContain("-");
        }

        @Test
        @DisplayName("多次生成唯一ID")
        void testGenerateUnique() {
            UuidGenerator gen = UuidGenerator.v4();
            Set<UUID> ids = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                ids.add(gen.generate());
            }

            assertThat(ids).hasSize(1000);
        }
    }

    @Nested
    @DisplayName("版本测试")
    class VersionTests {

        @Test
        @DisplayName("获取v4版本")
        void testGetVersionV4() {
            UuidGenerator gen = UuidGenerator.v4();

            assertThat(gen.getVersion()).isEqualTo(4);
        }

        @Test
        @DisplayName("获取v7版本")
        void testGetVersionV7() {
            UuidGenerator gen = UuidGenerator.v7();

            assertThat(gen.getVersion()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("类型测试")
    class TypeTests {

        @Test
        @DisplayName("获取v4类型")
        void testGetTypeV4() {
            UuidGenerator gen = UuidGenerator.v4();

            assertThat(gen.getType()).isEqualTo("UUID-v4");
        }

        @Test
        @DisplayName("获取v7类型")
        void testGetTypeV7() {
            UuidGenerator gen = UuidGenerator.v7();

            assertThat(gen.getType()).isEqualTo("UUID-v7");
        }
    }
}
