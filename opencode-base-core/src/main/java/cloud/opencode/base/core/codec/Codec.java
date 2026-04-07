package cloud.opencode.base.core.codec;

import java.util.Objects;

/**
 * Generic Codec Interface - Reversible encoding and decoding with composition support
 * 通用编解码接口 - 支持可逆编解码和组合
 *
 * <p>A codec encodes values of type {@code I} to type {@code O} and decodes back.
 * Codecs are composable via {@link #andThen(Codec)} to build encoding pipelines.</p>
 * <p>编解码器将类型 {@code I} 的值编码为类型 {@code O}，并可解码还原。
 * 编解码器可通过 {@link #andThen(Codec)} 组合构建编码管道。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Codec<byte[], String> base64 = OpenCodec.base64();
 * String encoded = base64.encode(data);
 * byte[] decoded = base64.decode(encoded);
 *
 * // Composition - 组合
 * Codec<byte[], String> combined = codecA.andThen(codecB);
 * }</pre>
 *
 * <p><strong>Contract | 契约:</strong></p>
 * <ul>
 *   <li>Roundtrip: {@code decode(encode(x)).equals(x)} must hold - 往返: 编码后解码必须还原</li>
 *   <li>Null-safety: null input throws {@link NullPointerException} - 空值安全: null 输入抛出 NPE</li>
 *   <li>Thread-safety: implementations must be thread-safe - 线程安全: 实现必须线程安全</li>
 * </ul>
 *
 * @param <I> input type (encoding source, decoding target) | 输入类型（编码源、解码目标）
 * @param <O> output type (encoding target, decoding source) | 输出类型（编码目标、解码源）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see OpenCodec
 * @since JDK 25, opencode-base-core V1.0.3
 */
public interface Codec<I, O> {

    /**
     * Encodes the input value
     * 编码输入值
     *
     * @param input the value to encode | 要编码的值
     * @return the encoded value | 编码后的值
     * @throws NullPointerException if input is null | 如果输入为 null
     */
    O encode(I input);

    /**
     * Decodes the encoded value back to the original type
     * 将编码值解码还原为原始类型
     *
     * @param output the value to decode | 要解码的值
     * @return the decoded value | 解码后的值
     * @throws NullPointerException  if output is null | 如果输入为 null
     * @throws IllegalArgumentException if the input is malformed | 如果输入格式错误
     */
    I decode(O output);

    /**
     * Composes this codec with another, creating a pipeline: this encodes first, then {@code after}
     * 将此编解码器与另一个组合，创建管道：先由此编码，再由 after 编码
     *
     * <p>For codecs A (I→M) and B (M→O), {@code A.andThen(B)} produces codec C (I→O) where:</p>
     * <ul>
     *   <li>{@code C.encode(x) == B.encode(A.encode(x))}</li>
     *   <li>{@code C.decode(y) == A.decode(B.decode(y))}</li>
     * </ul>
     *
     * @param <R>   the final output type | 最终输出类型
     * @param after the codec to apply after this one | 在此之后应用的编解码器
     * @return a composed codec | 组合后的编解码器
     * @throws NullPointerException if after is null | 如果 after 为 null
     */
    default <R> Codec<I, R> andThen(Codec<O, R> after) {
        Objects.requireNonNull(after, "after codec must not be null");
        Codec<I, O> self = this;
        return new Codec<>() {
            @Override
            public R encode(I input) {
                return after.encode(self.encode(input));
            }

            @Override
            public I decode(R output) {
                return self.decode(after.decode(output));
            }
        };
    }
}
