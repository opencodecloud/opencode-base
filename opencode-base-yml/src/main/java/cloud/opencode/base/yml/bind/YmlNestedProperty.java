/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.yml.bind;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * YAML Nested Property Annotation - Binds Nested Object from YAML
 * YAML 嵌套属性注解 - 从 YAML 绑定嵌套对象
 *
 * <p>Marks a field as a nested configuration object that should be bound
 * from a sub-tree of the YAML document.</p>
 * <p>标记字段为嵌套配置对象，应从 YAML 文档的子树绑定。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Bind nested YAML sub-trees to object fields - 将嵌套 YAML 子树绑定到对象字段</li>
 *   <li>Custom prefix path with dot notation support - 支持点号表示法的自定义前缀路径</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * public class AppConfig {
 *
 *     @YmlNestedProperty(prefix = "database")
 *     private DatabaseConfig database;
 *
 *     @YmlNestedProperty(prefix = "cache")
 *     private CacheConfig cache;
 * }
 *
 * public class DatabaseConfig {
 *     @YmlProperty("host")
 *     private String host;
 *
 *     @YmlProperty("port")
 *     private int port;
 *
 *     @YmlProperty("name")
 *     private String name;
 * }
 *
 * // YAML content:
 * // database:
 * //   host: localhost
 * //   port: 5432
 * //   name: mydb
 * // cache:
 * //   enabled: true
 * //   ttl: 3600
 *
 * AppConfig config = YmlBinder.bind(yaml, AppConfig.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (annotation is inherently immutable) - 线程安全: 是（注解本身不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see YmlProperty
 * @see YmlValue
 * @see YmlBinder
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface YmlNestedProperty {

    /**
     * The prefix path for nested properties
     * 嵌套属性的前缀路径
     *
     * <p>If empty, uses the field name as the prefix.
     * Supports dot notation for deeply nested structures.</p>
     * <p>如果为空，使用字段名作为前缀。
     * 支持点表示法用于深层嵌套结构。</p>
     *
     * @return prefix path | 前缀路径
     */
    String prefix() default "";
}
