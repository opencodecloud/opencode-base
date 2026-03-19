/*
 * Copyright 2025 OpenCode Cloud Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.yml.security;

import cloud.opencode.base.yml.exception.YmlSecurityException;

import java.util.Set;
import java.util.HashSet;
import java.util.function.Predicate;

/**
 * Safe Constructor - SPI interface for secure YAML construction
 * 安全构造器 - 安全 YAML 构造的 SPI 接口
 *
 * <p>This interface provides an abstraction for safe YAML object construction,
 * preventing arbitrary type instantiation and code execution vulnerabilities.</p>
 * <p>此接口为安全的 YAML 对象构造提供抽象，
 * 防止任意类型实例化和代码执行漏洞。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Whitelist-based type safety for YAML deserialization - 基于白名单的 YAML 反序列化类型安全</li>
 *   <li>Per-type and per-package allow rules - 按类型和按包的允许规则</li>
 *   <li>Custom validator predicates - 自定义验证器谓词</li>
 *   <li>Built-in safe types (primitives, dates, UUIDs) - 内置安全类型（基本类型、日期、UUID）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a safe constructor with default settings
 * SafeConstructor constructor = SafeConstructor.create();
 *
 * // Check if a type is safe
 * if (constructor.isSafeType(MyClass.class)) {
 *     // proceed with deserialization
 * }
 *
 * // Create with custom allowed types
 * SafeConstructor custom = SafeConstructor.builder()
 *     .allowType(MyConfig.class)
 *     .allowPackage("com.myapp.config")
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: Yes (returns false for null types) - 空值安全: 是（空类型返回 false）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see YmlSecurity
 * @see YmlSafeLoader
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public sealed interface SafeConstructor permits SafeConstructor.DefaultSafeConstructor {

    /**
     * Default set of safe basic types
     * 默认的安全基本类型集合
     */
    Set<Class<?>> BASIC_SAFE_TYPES = Set.of(
            String.class,
            Integer.class, int.class,
            Long.class, long.class,
            Double.class, double.class,
            Float.class, float.class,
            Boolean.class, boolean.class,
            Short.class, short.class,
            Byte.class, byte.class,
            Character.class, char.class,
            java.math.BigDecimal.class,
            java.math.BigInteger.class,
            java.util.Date.class,
            java.time.LocalDate.class,
            java.time.LocalDateTime.class,
            java.time.LocalTime.class,
            java.time.Instant.class,
            java.util.UUID.class
    );

    /**
     * Creates a default safe constructor.
     * 创建默认的安全构造器。
     *
     * @return safe constructor | 安全构造器
     */
    static SafeConstructor create() {
        return new DefaultSafeConstructor(Set.of(), Set.of(), t -> false);
    }

    /**
     * Creates a builder for safe constructor.
     * 创建安全构造器的构建器。
     *
     * @return builder | 构建器
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Checks if a type is safe for construction.
     * 检查类型是否可以安全构造。
     *
     * @param type the type to check | 要检查的类型
     * @return true if safe | 如果安全则返回 true
     */
    boolean isSafeType(Class<?> type);

    /**
     * Checks if a type name is safe for construction.
     * 检查类型名称是否可以安全构造。
     *
     * @param typeName the type name | 类型名称
     * @return true if safe | 如果安全则返回 true
     */
    boolean isSafeType(String typeName);

    /**
     * Validates that a type is safe, throwing if not.
     * 验证类型是否安全，如果不安全则抛出异常。
     *
     * @param type the type to validate | 要验证的类型
     * @throws YmlSecurityException if type is not safe | 如果类型不安全
     */
    default void validateType(Class<?> type) {
        if (!isSafeType(type)) {
            throw YmlSecurityException.unsafeType(type.getName());
        }
    }

    /**
     * Validates that a type name is safe, throwing if not.
     * 验证类型名称是否安全，如果不安全则抛出异常。
     *
     * @param typeName the type name to validate | 要验证的类型名称
     * @throws YmlSecurityException if type is not safe | 如果类型不安全
     */
    default void validateType(String typeName) {
        if (!isSafeType(typeName)) {
            throw YmlSecurityException.unsafeType(typeName);
        }
    }

    /**
     * Gets the set of allowed types.
     * 获取允许的类型集合。
     *
     * @return allowed types | 允许的类型
     */
    Set<Class<?>> getAllowedTypes();

    /**
     * Gets the set of allowed packages.
     * 获取允许的包名集合。
     *
     * @return allowed packages | 允许的包名
     */
    Set<String> getAllowedPackages();

    // ==================== Default Implementation | 默认实现 ====================

    /**
     * Default Safe Constructor Implementation
     * 默认安全构造器实现
     */
    final class DefaultSafeConstructor implements SafeConstructor {

        private final Set<Class<?>> allowedTypes;
        private final Set<String> allowedPackages;
        private final Predicate<Class<?>> customValidator;

        DefaultSafeConstructor(
                Set<Class<?>> allowedTypes,
                Set<String> allowedPackages,
                Predicate<Class<?>> customValidator) {
            this.allowedTypes = Set.copyOf(allowedTypes);
            this.allowedPackages = Set.copyOf(allowedPackages);
            this.customValidator = customValidator;
        }

        @Override
        public boolean isSafeType(Class<?> type) {
            if (type == null) {
                return false;
            }

            // Check basic safe types
            if (BASIC_SAFE_TYPES.contains(type)) {
                return true;
            }

            // Check collections and maps
            if (java.util.Collection.class.isAssignableFrom(type) ||
                java.util.Map.class.isAssignableFrom(type)) {
                return true;
            }

            // Check arrays of safe types
            if (type.isArray()) {
                return isSafeType(type.getComponentType());
            }

            // Check explicitly allowed types
            if (allowedTypes.contains(type)) {
                return true;
            }

            // Check allowed packages
            String packageName = type.getPackageName();
            for (String allowed : allowedPackages) {
                if (packageName.startsWith(allowed)) {
                    return true;
                }
            }

            // Check custom validator
            if (customValidator.test(type)) {
                return true;
            }

            return false;
        }

        @Override
        public boolean isSafeType(String typeName) {
            if (typeName == null || typeName.isEmpty()) {
                return false;
            }

            // Check for known dangerous patterns
            if (typeName.contains("Runtime") ||
                typeName.contains("ProcessBuilder") ||
                typeName.contains("ScriptEngine") ||
                typeName.startsWith("javax.script") ||
                typeName.startsWith("com.sun.") ||
                typeName.contains("Unsafe")) {
                return false;
            }

            try {
                Class<?> type = Class.forName(typeName);
                return isSafeType(type);
            } catch (ClassNotFoundException e) {
                // If class not found, check package patterns
                for (String allowed : allowedPackages) {
                    if (typeName.startsWith(allowed)) {
                        return true;
                    }
                }
                return false;
            }
        }

        @Override
        public Set<Class<?>> getAllowedTypes() {
            return allowedTypes;
        }

        @Override
        public Set<String> getAllowedPackages() {
            return allowedPackages;
        }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for SafeConstructor
     * SafeConstructor 的构建器
     */
    final class Builder {

        private final Set<Class<?>> allowedTypes = new HashSet<>();
        private final Set<String> allowedPackages = new HashSet<>();
        private Predicate<Class<?>> customValidator = t -> false;

        private Builder() {
        }

        /**
         * Allows a specific type.
         * 允许特定类型。
         *
         * @param type the type to allow | 要允许的类型
         * @return this builder | 此构建器
         */
        public Builder allowType(Class<?> type) {
            this.allowedTypes.add(type);
            return this;
        }

        /**
         * Allows multiple types.
         * 允许多个类型。
         *
         * @param types the types to allow | 要允许的类型
         * @return this builder | 此构建器
         */
        public Builder allowTypes(Class<?>... types) {
            for (Class<?> type : types) {
                this.allowedTypes.add(type);
            }
            return this;
        }

        /**
         * Allows all types in a package.
         * 允许包中的所有类型。
         *
         * @param packageName the package name | 包名
         * @return this builder | 此构建器
         */
        public Builder allowPackage(String packageName) {
            this.allowedPackages.add(packageName);
            return this;
        }

        /**
         * Allows types in multiple packages.
         * 允许多个包中的类型。
         *
         * @param packages the package names | 包名
         * @return this builder | 此构建器
         */
        public Builder allowPackages(String... packages) {
            for (String pkg : packages) {
                this.allowedPackages.add(pkg);
            }
            return this;
        }

        /**
         * Sets a custom type validator.
         * 设置自定义类型验证器。
         *
         * @param validator the validator | 验证器
         * @return this builder | 此构建器
         */
        public Builder customValidator(Predicate<Class<?>> validator) {
            this.customValidator = validator;
            return this;
        }

        /**
         * Builds the safe constructor.
         * 构建安全构造器。
         *
         * @return the safe constructor | 安全构造器
         */
        public SafeConstructor build() {
            return new DefaultSafeConstructor(allowedTypes, allowedPackages, customValidator);
        }
    }
}
