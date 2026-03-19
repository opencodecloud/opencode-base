package cloud.opencode.base.config.converter.impl;

import cloud.opencode.base.config.converter.ConfigConverter;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Number Converters Collection
 * 数字转换器集合
 *
 * <p>Provides converters for various numeric types.</p>
 * <p>提供各种数值类型的转换器。</p>
 *
 * <p><strong>Included Converters | 包含的转换器:</strong></p>
 * <ul>
 *   <li>{@link IntegerConverter} - Integer conversion</li>
 *   <li>{@link LongConverter} - Long conversion</li>
 *   <li>{@link DoubleConverter} - Double conversion</li>
 *   <li>{@link FloatConverter} - Float conversion</li>
 *   <li>{@link BigDecimalConverter} - BigDecimal conversion</li>
 *   <li>{@link BigIntegerConverter} - BigInteger conversion</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * var intConverter = new NumberConverters.IntegerConverter();
 * int port = intConverter.convert("8080");
 *
 * var decimalConverter = new NumberConverters.BigDecimalConverter();
 * BigDecimal price = decimalConverter.convert("99.99");
 * }</pre>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core NumberConverters functionality - NumberConverters核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per conversion - 每次转换 O(1)</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class NumberConverters {
    
    public static class IntegerConverter implements ConfigConverter<Integer> {
        @Override
        public Integer convert(String value) {
            return Integer.parseInt(value);
        }
        
        @Override
        public Class<Integer> getType() {
            return Integer.class;
        }
    }
    
    public static class LongConverter implements ConfigConverter<Long> {
        @Override
        public Long convert(String value) {
            return Long.parseLong(value);
        }
        
        @Override
        public Class<Long> getType() {
            return Long.class;
        }
    }
    
    public static class DoubleConverter implements ConfigConverter<Double> {
        @Override
        public Double convert(String value) {
            return Double.parseDouble(value);
        }
        
        @Override
        public Class<Double> getType() {
            return Double.class;
        }
    }
    
    public static class FloatConverter implements ConfigConverter<Float> {
        @Override
        public Float convert(String value) {
            return Float.parseFloat(value);
        }
        
        @Override
        public Class<Float> getType() {
            return Float.class;
        }
    }
    
    public static class BigDecimalConverter implements ConfigConverter<BigDecimal> {
        @Override
        public BigDecimal convert(String value) {
            return new BigDecimal(value);
        }
        
        @Override
        public Class<BigDecimal> getType() {
            return BigDecimal.class;
        }
    }
    
    public static class BigIntegerConverter implements ConfigConverter<BigInteger> {
        @Override
        public BigInteger convert(String value) {
            return new BigInteger(value);
        }
        
        @Override
        public Class<BigInteger> getType() {
            return BigInteger.class;
        }
    }
}
