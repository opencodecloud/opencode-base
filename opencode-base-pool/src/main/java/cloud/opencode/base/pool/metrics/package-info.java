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
 * Metrics Package - Pool Metrics and Statistics
 * 指标包 - 池指标和统计
 *
 * <p>This package provides metrics collection and reporting for pool
 * operations with lock-free counters.</p>
 * <p>此包使用无锁计数器提供池操作的指标收集和报告。</p>
 *
 * <h2>Classes | 类</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.pool.metrics.PoolMetrics} - Metrics interface</li>
 *   <li>{@link cloud.opencode.base.pool.metrics.DefaultPoolMetrics} - Default implementation</li>
 *   <li>{@link cloud.opencode.base.pool.metrics.MetricsSnapshot} - Point-in-time snapshot</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
package cloud.opencode.base.pool.metrics;
