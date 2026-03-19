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

/**
 * SPI (Service Provider Interface) - Pluggable Logging Providers
 * SPI（服务提供者接口） - 可插拔日志提供者
 *
 * <p>This package contains the SPI interfaces and classes for integrating
 * different logging frameworks. Providers are discovered via Java ServiceLoader.</p>
 * <p>本包包含用于集成不同日志框架的 SPI 接口和类。
 * 提供者通过 Java ServiceLoader 发现。</p>
 *
 * <h2>Key Classes | 核心类</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.log.spi.LogProvider} - Main SPI interface for logging providers</li>
 *   <li>{@link cloud.opencode.base.log.spi.LogProviderFactory} - Factory for discovering and managing providers</li>
 *   <li>{@link cloud.opencode.base.log.spi.MDCAdapter} - MDC adapter interface</li>
 *   <li>{@link cloud.opencode.base.log.spi.NDCAdapter} - NDC adapter interface</li>
 *   <li>{@link cloud.opencode.base.log.spi.LogAdapter} - Marker conversion adapter</li>
 *   <li>{@link cloud.opencode.base.log.spi.DefaultLogProvider} - Console-based fallback provider</li>
 * </ul>
 *
 * <h2>Implementing a Provider | 实现提供者</h2>
 * <pre>{@code
 * public class Slf4jLogProvider implements LogProvider {
 *     @Override
 *     public String getName() { return "SLF4J"; }
 *
 *     @Override
 *     public int getPriority() { return 10; } // Lower = higher priority
 *
 *     @Override
 *     public boolean isAvailable() {
 *         try {
 *             Class.forName("org.slf4j.LoggerFactory");
 *             return true;
 *         } catch (ClassNotFoundException e) {
 *             return false;
 *         }
 *     }
 *
 *     @Override
 *     public Logger getLogger(String name) {
 *         return new Slf4jLoggerAdapter(org.slf4j.LoggerFactory.getLogger(name));
 *     }
 *
 *     @Override
 *     public MDCAdapter getMDCAdapter() {
 *         return new Slf4jMDCAdapter();
 *     }
 * }
 * }</pre>
 *
 * <h2>Provider Registration | 提供者注册</h2>
 * <p>Create file: {@code META-INF/services/cloud.opencode.base.log.spi.LogProvider}</p>
 * <pre>
 * com.example.Slf4jLogProvider
 * </pre>
 *
 * @author OpenCode Cloud Group
 * @see cloud.opencode.base.log.spi.LogProvider
 * @see cloud.opencode.base.log.spi.LogProviderFactory
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
package cloud.opencode.base.log.spi;
