package cloud.opencode.base.test.data;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * RepeatableRandomTest Tests
 * RepeatableRandomTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("RepeatableRandom Tests")
class RepeatableRandomTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with seed")
        void shouldCreateWithSeed() {
            RepeatableRandom random = new RepeatableRandom(12345L);
            assertThat(random.getSeed()).isEqualTo(12345L);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("withRandomSeed should create with random seed")
        void withRandomSeedShouldCreateWithRandomSeed() {
            RepeatableRandom random = RepeatableRandom.withRandomSeed();
            assertThat(random.getSeed()).isNotZero();
        }

        @Test
        @DisplayName("withSeed should create with specified seed")
        void withSeedShouldCreateWithSpecifiedSeed() {
            RepeatableRandom random = RepeatableRandom.withSeed(54321L);
            assertThat(random.getSeed()).isEqualTo(54321L);
        }
    }

    @Nested
    @DisplayName("Repeatability Tests")
    class RepeatabilityTests {

        @Test
        @DisplayName("Same seed should produce same sequence")
        void sameSeedShouldProduceSameSequence() {
            RepeatableRandom r1 = new RepeatableRandom(12345L);
            RepeatableRandom r2 = new RepeatableRandom(12345L);

            assertThat(r1.nextInt()).isEqualTo(r2.nextInt());
            assertThat(r1.nextInt(100)).isEqualTo(r2.nextInt(100));
            assertThat(r1.nextLong()).isEqualTo(r2.nextLong());
            assertThat(r1.nextDouble()).isEqualTo(r2.nextDouble());
            assertThat(r1.nextBoolean()).isEqualTo(r2.nextBoolean());
            assertThat(r1.nextString(10)).isEqualTo(r2.nextString(10));
        }

        @Test
        @DisplayName("Different seed should produce different sequence")
        void differentSeedShouldProduceDifferentSequence() {
            RepeatableRandom r1 = new RepeatableRandom(12345L);
            RepeatableRandom r2 = new RepeatableRandom(54321L);

            // At least one should be different
            boolean allSame = r1.nextInt() == r2.nextInt() &&
                              r1.nextInt() == r2.nextInt() &&
                              r1.nextInt() == r2.nextInt();
            assertThat(allSame).isFalse();
        }
    }

    @Nested
    @DisplayName("Integer Generation Tests")
    class IntegerGenerationTests {

        @Test
        @DisplayName("nextInt should return integer")
        void nextIntShouldReturnInteger() {
            RepeatableRandom random = new RepeatableRandom(12345L);
            // Just verify it returns without error
            int value = random.nextInt();
            assertThat(value).isNotNull();
        }

        @Test
        @DisplayName("nextInt with bound should return value in range")
        void nextIntWithBoundShouldReturnValueInRange() {
            RepeatableRandom random = new RepeatableRandom(12345L);

            for (int i = 0; i < 100; i++) {
                int value = random.nextInt(100);
                assertThat(value).isGreaterThanOrEqualTo(0).isLessThan(100);
            }
        }

        @Test
        @DisplayName("nextInt with min and max should return value in range")
        void nextIntWithMinAndMaxShouldReturnValueInRange() {
            RepeatableRandom random = new RepeatableRandom(12345L);

            for (int i = 0; i < 100; i++) {
                int value = random.nextInt(10, 20);
                assertThat(value).isGreaterThanOrEqualTo(10).isLessThanOrEqualTo(20);
            }
        }
    }

    @Nested
    @DisplayName("Long Generation Tests")
    class LongGenerationTests {

        @Test
        @DisplayName("nextLong should return long")
        void nextLongShouldReturnLong() {
            RepeatableRandom random = new RepeatableRandom(12345L);
            long value = random.nextLong();
            assertThat(value).isNotNull();
        }

        @Test
        @DisplayName("nextLong with bound should return value in range")
        void nextLongWithBoundShouldReturnValueInRange() {
            RepeatableRandom random = new RepeatableRandom(12345L);

            for (int i = 0; i < 100; i++) {
                long value = random.nextLong(1000L);
                assertThat(value).isGreaterThanOrEqualTo(0).isLessThan(1000);
            }
        }
    }

    @Nested
    @DisplayName("Double Generation Tests")
    class DoubleGenerationTests {

        @Test
        @DisplayName("nextDouble should return value between 0 and 1")
        void nextDoubleShouldReturnValueBetween0And1() {
            RepeatableRandom random = new RepeatableRandom(12345L);

            for (int i = 0; i < 100; i++) {
                double value = random.nextDouble();
                assertThat(value).isGreaterThanOrEqualTo(0.0).isLessThan(1.0);
            }
        }

        @Test
        @DisplayName("nextDouble with min and max should return value in range")
        void nextDoubleWithMinAndMaxShouldReturnValueInRange() {
            RepeatableRandom random = new RepeatableRandom(12345L);

            for (int i = 0; i < 100; i++) {
                double value = random.nextDouble(10.0, 20.0);
                assertThat(value).isGreaterThanOrEqualTo(10.0).isLessThan(20.0);
            }
        }
    }

    @Nested
    @DisplayName("Boolean Generation Tests")
    class BooleanGenerationTests {

        @Test
        @DisplayName("nextBoolean should return boolean")
        void nextBooleanShouldReturnBoolean() {
            RepeatableRandom random = new RepeatableRandom(12345L);

            boolean hasTrue = false;
            boolean hasFalse = false;

            for (int i = 0; i < 100; i++) {
                if (random.nextBoolean()) {
                    hasTrue = true;
                } else {
                    hasFalse = true;
                }
            }

            assertThat(hasTrue).isTrue();
            assertThat(hasFalse).isTrue();
        }

        @Test
        @DisplayName("nextBoolean with probability 0 should always return false")
        void nextBooleanWithProbability0ShouldAlwaysReturnFalse() {
            RepeatableRandom random = new RepeatableRandom(12345L);

            for (int i = 0; i < 100; i++) {
                assertThat(random.nextBoolean(0.0)).isFalse();
            }
        }

        @Test
        @DisplayName("nextBoolean with probability 1 should always return true")
        void nextBooleanWithProbability1ShouldAlwaysReturnTrue() {
            RepeatableRandom random = new RepeatableRandom(12345L);

            for (int i = 0; i < 100; i++) {
                assertThat(random.nextBoolean(1.0)).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("String Generation Tests")
    class StringGenerationTests {

        @Test
        @DisplayName("nextString should return string of specified length")
        void nextStringShouldReturnStringOfSpecifiedLength() {
            RepeatableRandom random = new RepeatableRandom(12345L);
            String str = random.nextString(20);

            assertThat(str).hasSize(20);
            assertThat(str).matches("[A-Za-z0-9]+");
        }

        @Test
        @DisplayName("nextDigits should return digits only")
        void nextDigitsShouldReturnDigitsOnly() {
            RepeatableRandom random = new RepeatableRandom(12345L);
            String digits = random.nextDigits(10);

            assertThat(digits).hasSize(10);
            assertThat(digits).matches("\\d+");
        }
    }

    @Nested
    @DisplayName("Array Selection Tests")
    class ArraySelectionTests {

        @Test
        @DisplayName("nextElement should return element from array")
        void nextElementShouldReturnElementFromArray() {
            RepeatableRandom random = new RepeatableRandom(12345L);
            String[] array = {"a", "b", "c", "d"};

            String element = random.nextElement(array);
            assertThat(element).isIn("a", "b", "c", "d");
        }
    }

    @Nested
    @DisplayName("Reset Tests")
    class ResetTests {

        @Test
        @DisplayName("reset should return new instance with same seed")
        void resetShouldReturnNewInstanceWithSameSeed() {
            RepeatableRandom original = new RepeatableRandom(12345L);
            int first = original.nextInt();
            int second = original.nextInt();

            RepeatableRandom reset = original.reset();
            assertThat(reset.getSeed()).isEqualTo(original.getSeed());
            assertThat(reset.nextInt()).isEqualTo(first);
            assertThat(reset.nextInt()).isEqualTo(second);
        }
    }
}
