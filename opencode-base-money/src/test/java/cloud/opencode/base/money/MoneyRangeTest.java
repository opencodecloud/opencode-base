package cloud.opencode.base.money;

import cloud.opencode.base.money.exception.CurrencyMismatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * MoneyRange test class | MoneyRange 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.3
 */
@DisplayName("MoneyRange Test | 金额区间测试")
class MoneyRangeTest {

    // ============ Construction | 构造 ============

    @Nested
    @DisplayName("Construction | 构造测试")
    class ConstructionTests {

        @Test
        @DisplayName("Valid range creation | 创建有效区间")
        void validRange() {
            MoneyRange range = MoneyRange.of(Money.of("10"), Money.of("100"));

            assertThat(range.min()).isEqualTo(Money.of("10"));
            assertThat(range.max()).isEqualTo(Money.of("100"));
            assertThat(range.currency()).isEqualTo(Currency.CNY);
        }

        @Test
        @DisplayName("Singleton range creation | 创建单点区间")
        void singletonRange() {
            Money value = Money.of("50");
            MoneyRange range = MoneyRange.singleton(value);

            assertThat(range.min()).isEqualTo(value);
            assertThat(range.max()).isEqualTo(value);
        }

        @Test
        @DisplayName("Null min throws NullPointerException | min为null抛出异常")
        void nullMin() {
            assertThatThrownBy(() -> MoneyRange.of(null, Money.of("100")))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Min");
        }

        @Test
        @DisplayName("Null max throws NullPointerException | max为null抛出异常")
        void nullMax() {
            assertThatThrownBy(() -> MoneyRange.of(Money.of("10"), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Max");
        }

        @Test
        @DisplayName("Null singleton value throws NullPointerException | 单点值为null抛出异常")
        void nullSingleton() {
            assertThatThrownBy(() -> MoneyRange.singleton(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Min > max throws IllegalArgumentException | min大于max抛出异常")
        void minGreaterThanMax() {
            assertThatThrownBy(() -> MoneyRange.of(Money.of("100"), Money.of("10")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must not be greater than");
        }

        @Test
        @DisplayName("Currency mismatch throws CurrencyMismatchException | 币种不匹配抛出异常")
        void currencyMismatch() {
            assertThatThrownBy(() -> MoneyRange.of(
                    Money.of("10", Currency.CNY),
                    Money.of("100", Currency.USD)))
                    .isInstanceOf(CurrencyMismatchException.class);
        }
    }

    // ============ contains(Money) ============

    @Nested
    @DisplayName("contains(Money) | 包含金额测试")
    class ContainsMoneyTests {

        private final MoneyRange range = MoneyRange.of(Money.of("10"), Money.of("100"));

        @Test
        @DisplayName("Inside range returns true | 区间内返回true")
        void insideRange() {
            assertThat(range.contains(Money.of("50"))).isTrue();
        }

        @Test
        @DisplayName("On min boundary returns true | 下界返回true")
        void onMinBoundary() {
            assertThat(range.contains(Money.of("10"))).isTrue();
        }

        @Test
        @DisplayName("On max boundary returns true | 上界返回true")
        void onMaxBoundary() {
            assertThat(range.contains(Money.of("100"))).isTrue();
        }

        @Test
        @DisplayName("Below min returns false | 低于下界返回false")
        void belowMin() {
            assertThat(range.contains(Money.of("9.99"))).isFalse();
        }

        @Test
        @DisplayName("Above max returns false | 高于上界返回false")
        void aboveMax() {
            assertThat(range.contains(Money.of("100.01"))).isFalse();
        }

        @Test
        @DisplayName("Currency mismatch throws CurrencyMismatchException | 币种不匹配抛出异常")
        void currencyMismatch() {
            assertThatThrownBy(() -> range.contains(Money.of("50", Currency.USD)))
                    .isInstanceOf(CurrencyMismatchException.class);
        }
    }

    // ============ contains(MoneyRange) ============

    @Nested
    @DisplayName("contains(MoneyRange) | 包含区间测试")
    class ContainsRangeTests {

        private final MoneyRange range = MoneyRange.of(Money.of("10"), Money.of("100"));

        @Test
        @DisplayName("Fully contained range returns true | 完全包含返回true")
        void fullyContained() {
            MoneyRange inner = MoneyRange.of(Money.of("20"), Money.of("80"));
            assertThat(range.contains(inner)).isTrue();
        }

        @Test
        @DisplayName("Same range returns true | 相同区间返回true")
        void sameRange() {
            MoneyRange same = MoneyRange.of(Money.of("10"), Money.of("100"));
            assertThat(range.contains(same)).isTrue();
        }

        @Test
        @DisplayName("Partially overlapping returns false | 部分重叠返回false")
        void partiallyOverlapping() {
            MoneyRange partial = MoneyRange.of(Money.of("50"), Money.of("150"));
            assertThat(range.contains(partial)).isFalse();
        }

        @Test
        @DisplayName("Not contained returns false | 不包含返回false")
        void notContained() {
            MoneyRange outside = MoneyRange.of(Money.of("200"), Money.of("300"));
            assertThat(range.contains(outside)).isFalse();
        }
    }

    // ============ clamp ============

    @Nested
    @DisplayName("clamp | 夹紧测试")
    class ClampTests {

        private final MoneyRange range = MoneyRange.of(Money.of("10"), Money.of("100"));

        @Test
        @DisplayName("Below min clamps to min | 低于下界夹紧到下界")
        void belowMin() {
            Money result = range.clamp(Money.of("5"));
            assertThat(result).isEqualTo(Money.of("10"));
        }

        @Test
        @DisplayName("Above max clamps to max | 高于上界夹紧到上界")
        void aboveMax() {
            Money result = range.clamp(Money.of("200"));
            assertThat(result).isEqualTo(Money.of("100"));
        }

        @Test
        @DisplayName("Within range returns same value | 区间内返回相同值")
        void withinRange() {
            Money input = Money.of("50");
            Money result = range.clamp(input);
            assertThat(result).isEqualTo(input);
        }
    }

    // ============ overlaps ============

    @Nested
    @DisplayName("overlaps | 重叠测试")
    class OverlapsTests {

        private final MoneyRange range = MoneyRange.of(Money.of("10"), Money.of("100"));

        @Test
        @DisplayName("Overlapping ranges return true | 重叠区间返回true")
        void overlapping() {
            MoneyRange other = MoneyRange.of(Money.of("80"), Money.of("200"));
            assertThat(range.overlaps(other)).isTrue();
        }

        @Test
        @DisplayName("Non-overlapping ranges return false | 不重叠区间返回false")
        void nonOverlapping() {
            MoneyRange other = MoneyRange.of(Money.of("200"), Money.of("300"));
            assertThat(range.overlaps(other)).isFalse();
        }

        @Test
        @DisplayName("Adjacent ranges (touching) return true | 相邻区间返回true")
        void adjacent() {
            MoneyRange other = MoneyRange.of(Money.of("100"), Money.of("200"));
            assertThat(range.overlaps(other)).isTrue();
        }

        @Test
        @DisplayName("Same range returns true | 相同区间返回true")
        void sameRange() {
            MoneyRange same = MoneyRange.of(Money.of("10"), Money.of("100"));
            assertThat(range.overlaps(same)).isTrue();
        }
    }

    // ============ intersection ============

    @Nested
    @DisplayName("intersection | 交集测试")
    class IntersectionTests {

        private final MoneyRange range = MoneyRange.of(Money.of("10"), Money.of("100"));

        @Test
        @DisplayName("Overlapping returns intersection | 重叠返回交集")
        void overlapping() {
            MoneyRange other = MoneyRange.of(Money.of("80"), Money.of("200"));
            MoneyRange result = range.intersection(other);

            assertThat(result).isNotNull();
            assertThat(result.min()).isEqualTo(Money.of("80"));
            assertThat(result.max()).isEqualTo(Money.of("100"));
        }

        @Test
        @DisplayName("Non-overlapping returns null | 不重叠返回null")
        void nonOverlapping() {
            MoneyRange other = MoneyRange.of(Money.of("200"), Money.of("300"));
            assertThat(range.intersection(other)).isNull();
        }

        @Test
        @DisplayName("Adjacent returns singleton intersection | 相邻返回单点交集")
        void adjacent() {
            MoneyRange other = MoneyRange.of(Money.of("100"), Money.of("200"));
            MoneyRange result = range.intersection(other);

            assertThat(result).isNotNull();
            assertThat(result.min()).isEqualTo(Money.of("100"));
            assertThat(result.max()).isEqualTo(Money.of("100"));
        }
    }

    // ============ span ============

    @Nested
    @DisplayName("span | 跨度测试")
    class SpanTests {

        @Test
        @DisplayName("Span covers both ranges | 跨度覆盖两个区间")
        void spanCoversBoth() {
            MoneyRange a = MoneyRange.of(Money.of("10"), Money.of("50"));
            MoneyRange b = MoneyRange.of(Money.of("80"), Money.of("200"));
            MoneyRange result = a.span(b);

            assertThat(result.min()).isEqualTo(Money.of("10"));
            assertThat(result.max()).isEqualTo(Money.of("200"));
        }

        @Test
        @DisplayName("Span of overlapping ranges | 重叠区间的跨度")
        void spanOverlapping() {
            MoneyRange a = MoneyRange.of(Money.of("10"), Money.of("100"));
            MoneyRange b = MoneyRange.of(Money.of("50"), Money.of("150"));
            MoneyRange result = a.span(b);

            assertThat(result.min()).isEqualTo(Money.of("10"));
            assertThat(result.max()).isEqualTo(Money.of("150"));
        }
    }

    // ============ gap ============

    @Nested
    @DisplayName("gap | 间隙测试")
    class GapTests {

        @Test
        @DisplayName("Non-overlapping returns gap | 不重叠返回间隙")
        void nonOverlapping() {
            MoneyRange a = MoneyRange.of(Money.of("10"), Money.of("50"));
            MoneyRange b = MoneyRange.of(Money.of("80"), Money.of("200"));
            MoneyRange result = a.gap(b);

            assertThat(result).isNotNull();
            assertThat(result.min()).isEqualTo(Money.of("50"));
            assertThat(result.max()).isEqualTo(Money.of("80"));
        }

        @Test
        @DisplayName("Overlapping returns null | 重叠返回null")
        void overlapping() {
            MoneyRange a = MoneyRange.of(Money.of("10"), Money.of("100"));
            MoneyRange b = MoneyRange.of(Money.of("50"), Money.of("200"));
            assertThat(a.gap(b)).isNull();
        }

        @Test
        @DisplayName("Adjacent returns null | 相邻返回null")
        void adjacent() {
            MoneyRange a = MoneyRange.of(Money.of("10"), Money.of("50"));
            MoneyRange b = MoneyRange.of(Money.of("50"), Money.of("100"));
            assertThat(a.gap(b)).isNull();
        }
    }

    // ============ isSingleton ============

    @Nested
    @DisplayName("isSingleton | 单点判断测试")
    class IsSingletonTests {

        @Test
        @DisplayName("Equal min and max returns true | min等于max返回true")
        void singleton() {
            MoneyRange range = MoneyRange.singleton(Money.of("50"));
            assertThat(range.isSingleton()).isTrue();
        }

        @Test
        @DisplayName("Different min and max returns false | min不等于max返回false")
        void notSingleton() {
            MoneyRange range = MoneyRange.of(Money.of("10"), Money.of("100"));
            assertThat(range.isSingleton()).isFalse();
        }
    }

    // ============ width ============

    @Nested
    @DisplayName("width | 宽度测试")
    class WidthTests {

        @Test
        @DisplayName("Correct width calculation | 正确计算宽度")
        void width() {
            MoneyRange range = MoneyRange.of(Money.of("10"), Money.of("100"));
            Money width = range.width();
            assertThat(width.amount()).isEqualByComparingTo("90");
        }

        @Test
        @DisplayName("Singleton width is zero | 单点宽度为零")
        void singletonWidth() {
            MoneyRange range = MoneyRange.singleton(Money.of("50"));
            assertThat(range.width().isZero()).isTrue();
        }
    }

    // ============ midpoint ============

    @Nested
    @DisplayName("midpoint | 中点测试")
    class MidpointTests {

        @Test
        @DisplayName("Correct midpoint | 正确计算中点")
        void midpoint() {
            MoneyRange range = MoneyRange.of(Money.of("10"), Money.of("100"));
            Money mid = range.midpoint();
            assertThat(mid.amount()).isEqualByComparingTo("55");
        }

        @Test
        @DisplayName("Singleton midpoint equals value | 单点中点等于值")
        void singletonMidpoint() {
            Money value = Money.of("50");
            MoneyRange range = MoneyRange.singleton(value);
            assertThat(range.midpoint().amount()).isEqualByComparingTo("50");
        }
    }

    // ============ toString ============

    @Nested
    @DisplayName("toString | 字符串表示测试")
    class ToStringTests {

        @Test
        @DisplayName("Format check with brackets | 格式检查带方括号")
        void formatCheck() {
            MoneyRange range = MoneyRange.of(Money.of("10"), Money.of("100"));
            String str = range.toString();

            assertThat(str).startsWith("[");
            assertThat(str).endsWith("]");
            assertThat(str).contains(",");
        }
    }
}
