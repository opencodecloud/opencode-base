package cloud.opencode.base.captcha.validator;

import cloud.opencode.base.captcha.ValidationResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Composite Validator - Chains multiple CaptchaValidator instances with short-circuit semantics
 * 组合验证器 - 链式组合多个 CaptchaValidator 实例，支持短路语义
 *
 * <p>This validator chains multiple {@link CaptchaValidator} instances, executing them
 * in order. If any validator returns a non-success result, the chain short-circuits
 * and returns that failure immediately. If all validators succeed, the last success
 * result is returned.</p>
 * <p>此验证器将多个 {@link CaptchaValidator} 实例链式组合，按顺序执行。
 * 如果任何验证器返回非成功结果，链会立即短路并返回该失败结果。
 * 如果所有验证器都成功，则返回最后一个成功结果。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Short-circuit evaluation on first failure - 首次失败时短路求值</li>
 *   <li>Immutable validator list after construction - 构造后验证器列表不可变</li>
 *   <li>Supports both pre-check validators and primary answer validator - 支持预检验证器和主答案验证器</li>
 *   <li>Builder pattern for flexible construction - 构建器模式灵活构造</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Chain a rate limiter check before the actual answer validator
 * CaptchaValidator composite = CompositeValidator.of(
 *     CaptchaValidator.simple(store),
 *     rateLimiterValidator
 * );
 * ValidationResult result = composite.validate(id, answer);
 *
 * // Using the builder
 * CaptchaValidator composite = CompositeValidator.builder()
 *     .addValidator(rateLimiterValidator)
 *     .addValidator(CaptchaValidator.simple(store))
 *     .build();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n is the number of validators - 时间复杂度: O(n)，n 为验证器数量</li>
 *   <li>Space complexity: O(1) per validation call - 空间复杂度: 每次验证调用 O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes if all contained validators are thread-safe - 线程安全: 如果所有包含的验证器都是线程安全的则是</li>
 *   <li>Null-safe: No (id and answer must not be null) - 空值安全: 否（ID 和答案不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public final class CompositeValidator implements CaptchaValidator {

    private final List<CaptchaValidator> validators;

    /**
     * Constructs a CompositeValidator with the given validators.
     * 使用给定的验证器构造组合验证器。
     *
     * @param validators the validators to chain (must not be empty) | 要链接的验证器（不能为空）
     */
    private CompositeValidator(List<CaptchaValidator> validators) {
        if (validators.isEmpty()) {
            throw new IllegalArgumentException("validators must not be empty");
        }
        this.validators = validators;
    }

    /**
     * Creates a CompositeValidator with the given validators.
     * 使用给定的验证器创建组合验证器。
     *
     * <p>The first validator is executed first, followed by the rest in order.
     * Typically the first validator does the actual store lookup and answer check,
     * while subsequent validators perform additional checks (rate limiting, behavior analysis, etc.).</p>
     * <p>第一个验证器最先执行，其余按顺序执行。通常第一个验证器进行实际的存储查找和答案检查，
     * 后续验证器执行附加检查（速率限制、行为分析等）。</p>
     *
     * @param first the first validator | 第一个验证器
     * @param rest  additional validators | 附加验证器
     * @return a new CompositeValidator | 新的组合验证器
     * @throws NullPointerException if first or any element in rest is null | 如果 first 或 rest 中任何元素为 null
     */
    public static CompositeValidator of(CaptchaValidator first, CaptchaValidator... rest) {
        Objects.requireNonNull(first, "first validator must not be null");
        Objects.requireNonNull(rest, "rest must not be null");
        List<CaptchaValidator> all = new ArrayList<>(1 + rest.length);
        all.add(first);
        for (CaptchaValidator v : rest) {
            Objects.requireNonNull(v, "validator must not be null");
            all.add(v);
        }
        return new CompositeValidator(List.copyOf(all));
    }

    /**
     * Creates a CompositeValidator from a list of validators.
     * 从验证器列表创建组合验证器。
     *
     * @param validators the validators (must not be null or empty) | 验证器列表（不能为 null 或空）
     * @return a new CompositeValidator | 新的组合验证器
     * @throws NullPointerException     if validators is null or contains null | 如果 validators 为 null 或包含 null
     * @throws IllegalArgumentException if validators is empty | 如果 validators 为空
     */
    public static CompositeValidator ofList(List<CaptchaValidator> validators) {
        Objects.requireNonNull(validators, "validators must not be null");
        if (validators.isEmpty()) {
            throw new IllegalArgumentException("validators must not be empty");
        }
        for (CaptchaValidator v : validators) {
            Objects.requireNonNull(v, "validator must not be null");
        }
        return new CompositeValidator(List.copyOf(validators));
    }

    /**
     * Creates a builder for constructing a CompositeValidator.
     * 创建用于构造组合验证器的构建器。
     *
     * @return a new builder | 新的构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Validates a CAPTCHA answer by chaining all validators.
     * 通过链接所有验证器来验证验证码答案。
     *
     * <p>Validators are executed in order. The first non-success result causes
     * immediate return (short-circuit). If all succeed, the last success result
     * is returned.</p>
     * <p>验证器按顺序执行。第一个非成功结果导致立即返回（短路）。
     * 如果全部成功，则返回最后一个成功结果。</p>
     *
     * @param id     the CAPTCHA ID | 验证码 ID
     * @param answer the provided answer | 提供的答案
     * @return the validation result | 验证结果
     */
    @Override
    public ValidationResult validate(String id, String answer) {
        // Sentinel guards against impossible empty-list case (constructor validates non-empty)
        ValidationResult lastResult = ValidationResult.invalidInput();
        for (CaptchaValidator validator : validators) {
            lastResult = validator.validate(id, answer);
            if (!lastResult.success()) {
                return lastResult;
            }
        }
        return lastResult;
    }

    /**
     * Validates a CAPTCHA answer with case sensitivity option by chaining all validators.
     * 通过链接所有验证器来验证验证码答案（带大小写敏感选项）。
     *
     * @param id            the CAPTCHA ID | 验证码 ID
     * @param answer        the provided answer | 提供的答案
     * @param caseSensitive whether case sensitive | 是否区分大小写
     * @return the validation result | 验证结果
     */
    @Override
    public ValidationResult validate(String id, String answer, boolean caseSensitive) {
        // Sentinel guards against impossible empty-list case (constructor validates non-empty)
        ValidationResult lastResult = ValidationResult.invalidInput();
        for (CaptchaValidator validator : validators) {
            lastResult = validator.validate(id, answer, caseSensitive);
            if (!lastResult.success()) {
                return lastResult;
            }
        }
        return lastResult;
    }

    /**
     * Returns the number of validators in this composite.
     * 返回此组合中的验证器数量。
     *
     * @return the validator count | 验证器数量
     */
    public int size() {
        return validators.size();
    }

    /**
     * Returns an unmodifiable view of the validators in this composite.
     * 返回此组合中验证器的不可修改视图。
     *
     * @return the validators | 验证器列表
     */
    public List<CaptchaValidator> getValidators() {
        return validators;
    }

    /**
     * Builder for CompositeValidator
     * 组合验证器构建器
     *
     * <p>Allows step-by-step construction of a CompositeValidator.</p>
     * <p>允许逐步构建组合验证器。</p>
     */
    public static final class Builder {

        private final List<CaptchaValidator> validators = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds a validator to the chain.
         * 向链中添加验证器。
         *
         * @param validator the validator to add | 要添加的验证器
         * @return this builder | 此构建器
         * @throws NullPointerException if validator is null | 如果 validator 为 null
         */
        public Builder addValidator(CaptchaValidator validator) {
            Objects.requireNonNull(validator, "validator must not be null");
            validators.add(validator);
            return this;
        }

        /**
         * Builds the CompositeValidator.
         * 构建组合验证器。
         *
         * @return the composite validator | 组合验证器
         * @throws IllegalStateException if no validators have been added | 如果未添加任何验证器
         */
        public CompositeValidator build() {
            if (validators.isEmpty()) {
                throw new IllegalStateException("At least one validator must be added");
            }
            return new CompositeValidator(List.copyOf(validators));
        }
    }
}
