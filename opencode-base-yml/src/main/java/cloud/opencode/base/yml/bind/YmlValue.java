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
 * YAML Value Annotation - Binds YAML Path to Field
 * YAML 值注解 - 将 YAML 路径绑定到字段
 *
 * <p>Specifies a YAML path to bind to a field. Supports nested path notation
 * (e.g., "database.host") and default values.</p>
 * <p>指定要绑定到字段的 YAML 路径。支持嵌套路径表示法
 * （例如 "database.host"）和默认值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Bind nested YAML paths to fields using dot notation - 使用点号表示法将嵌套 YAML 路径绑定到字段</li>
 *   <li>Default value fallback for missing properties - 缺失属性的默认值回退</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * public class AppConfig {
 *
 *     @YmlValue("server.port")
 *     private int port;
 *
 *     @YmlValue(value = "server.host", defaultValue = "localhost")
 *     private String host;
 *
 *     @YmlValue("database.connection-timeout")
 *     private Duration timeout;
 * }
 *
 * // YAML content:
 * // server:
 * //   port: 8080
 * //   host: api.example.com
 * // database:
 * //   connection-timeout: 30s
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
 * @see YmlNestedProperty
 * @see YmlBinder
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface YmlValue {

    /**
     * The YAML property path
     * YAML 属性路径
     *
     * <p>Supports dot notation for nested properties (e.g., "database.host").</p>
     * <p>支持点表示法用于嵌套属性（例如 "database.host"）。</p>
     *
     * @return property path | 属性路径
     */
    String value();

    /**
     * Default value if property is not found
     * 属性未找到时的默认值
     *
     * <p>Empty string means no default value.</p>
     * <p>空字符串表示无默认值。</p>
     *
     * @return default value | 默认值
     */
    String defaultValue() default "";
}
