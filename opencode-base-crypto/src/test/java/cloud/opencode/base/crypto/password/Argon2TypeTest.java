package cloud.opencode.base.crypto.password;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link Argon2Type}.
 * Argon2类型枚举单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Argon2Type Tests / Argon2类型枚举测试")
class Argon2TypeTest {

    @Nested
    @DisplayName("Enum Values Tests / 枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("有三个Argon2类型值")
        void testEnumValuesCount() {
            Argon2Type[] values = Argon2Type.values();
            assertThat(values).hasSize(3);
        }

        @Test
        @DisplayName("ARGON2D存在")
        void testArgon2dExists() {
            Argon2Type type = Argon2Type.ARGON2D;
            assertThat(type).isNotNull();
        }

        @Test
        @DisplayName("ARGON2I存在")
        void testArgon2iExists() {
            Argon2Type type = Argon2Type.ARGON2I;
            assertThat(type).isNotNull();
        }

        @Test
        @DisplayName("ARGON2ID存在")
        void testArgon2idExists() {
            Argon2Type type = Argon2Type.ARGON2ID;
            assertThat(type).isNotNull();
        }
    }

    @Nested
    @DisplayName("TypeId Tests / 类型ID测试")
    class TypeIdTests {

        @Test
        @DisplayName("ARGON2D的typeId为0")
        void testArgon2dTypeId() {
            assertThat(Argon2Type.ARGON2D.getTypeId()).isEqualTo(0);
        }

        @Test
        @DisplayName("ARGON2I的typeId为1")
        void testArgon2iTypeId() {
            assertThat(Argon2Type.ARGON2I.getTypeId()).isEqualTo(1);
        }

        @Test
        @DisplayName("ARGON2ID的typeId为2")
        void testArgon2idTypeId() {
            assertThat(Argon2Type.ARGON2ID.getTypeId()).isEqualTo(2);
        }

        @Test
        @DisplayName("每个类型的typeId唯一")
        void testTypeIdUnique() {
            int[] typeIds = new int[3];
            for (int i = 0; i < Argon2Type.values().length; i++) {
                typeIds[i] = Argon2Type.values()[i].getTypeId();
            }
            assertThat(typeIds).containsExactly(0, 1, 2);
        }
    }

    @Nested
    @DisplayName("AlgorithmName Tests / 算法名称测试")
    class AlgorithmNameTests {

        @Test
        @DisplayName("ARGON2D的算法名称为argon2d")
        void testArgon2dAlgorithmName() {
            assertThat(Argon2Type.ARGON2D.getAlgorithmName()).isEqualTo("argon2d");
        }

        @Test
        @DisplayName("ARGON2I的算法名称为argon2i")
        void testArgon2iAlgorithmName() {
            assertThat(Argon2Type.ARGON2I.getAlgorithmName()).isEqualTo("argon2i");
        }

        @Test
        @DisplayName("ARGON2ID的算法名称为argon2id")
        void testArgon2idAlgorithmName() {
            assertThat(Argon2Type.ARGON2ID.getAlgorithmName()).isEqualTo("argon2id");
        }

        @Test
        @DisplayName("算法名称全小写")
        void testAlgorithmNameLowerCase() {
            for (Argon2Type type : Argon2Type.values()) {
                String name = type.getAlgorithmName();
                assertThat(name).isEqualTo(name.toLowerCase());
            }
        }

        @Test
        @DisplayName("算法名称以argon2开头")
        void testAlgorithmNamePrefix() {
            for (Argon2Type type : Argon2Type.values()) {
                assertThat(type.getAlgorithmName()).startsWith("argon2");
            }
        }
    }

    @Nested
    @DisplayName("ValueOf Tests / valueOf测试")
    class ValueOfTests {

        @Test
        @DisplayName("valueOf(ARGON2D)返回ARGON2D")
        void testValueOfArgon2d() {
            Argon2Type type = Argon2Type.valueOf("ARGON2D");
            assertThat(type).isEqualTo(Argon2Type.ARGON2D);
        }

        @Test
        @DisplayName("valueOf(ARGON2I)返回ARGON2I")
        void testValueOfArgon2i() {
            Argon2Type type = Argon2Type.valueOf("ARGON2I");
            assertThat(type).isEqualTo(Argon2Type.ARGON2I);
        }

        @Test
        @DisplayName("valueOf(ARGON2ID)返回ARGON2ID")
        void testValueOfArgon2id() {
            Argon2Type type = Argon2Type.valueOf("ARGON2ID");
            assertThat(type).isEqualTo(Argon2Type.ARGON2ID);
        }

        @Test
        @DisplayName("valueOf(invalid)抛出异常")
        void testValueOfInvalid() {
            assertThatThrownBy(() -> Argon2Type.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Ordinal Tests / 序号测试")
    class OrdinalTests {

        @Test
        @DisplayName("ARGON2D序号为0")
        void testArgon2dOrdinal() {
            assertThat(Argon2Type.ARGON2D.ordinal()).isEqualTo(0);
        }

        @Test
        @DisplayName("ARGON2I序号为1")
        void testArgon2iOrdinal() {
            assertThat(Argon2Type.ARGON2I.ordinal()).isEqualTo(1);
        }

        @Test
        @DisplayName("ARGON2ID序号为2")
        void testArgon2idOrdinal() {
            assertThat(Argon2Type.ARGON2ID.ordinal()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Name Tests / 名称测试")
    class NameTests {

        @Test
        @DisplayName("ARGON2D名称正确")
        void testArgon2dName() {
            assertThat(Argon2Type.ARGON2D.name()).isEqualTo("ARGON2D");
        }

        @Test
        @DisplayName("ARGON2I名称正确")
        void testArgon2iName() {
            assertThat(Argon2Type.ARGON2I.name()).isEqualTo("ARGON2I");
        }

        @Test
        @DisplayName("ARGON2ID名称正确")
        void testArgon2idName() {
            assertThat(Argon2Type.ARGON2ID.name()).isEqualTo("ARGON2ID");
        }
    }
}
