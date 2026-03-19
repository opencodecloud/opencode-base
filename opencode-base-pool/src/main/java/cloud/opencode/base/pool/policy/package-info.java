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
 * Policy Package - Pool Policies (JDK 25 Sealed Types)
 * 策略包 - 池策略 (JDK 25 密封类型)
 *
 * <p>This package provides policy classes for eviction, waiting,
 * and validation using JDK 25 sealed interfaces and records.</p>
 * <p>此包使用JDK 25密封接口和记录提供驱逐、等待和验证的策略类。</p>
 *
 * <h2>Classes | 类</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.pool.policy.EvictionPolicy} - Sealed eviction policy interface</li>
 *   <li>{@link cloud.opencode.base.pool.policy.EvictionContext} - Eviction context record</li>
 *   <li>{@link cloud.opencode.base.pool.policy.WaitPolicy} - Wait policy enumeration</li>
 *   <li>{@link cloud.opencode.base.pool.policy.ValidationPolicy} - Validation policy record</li>
 * </ul>
 *
 * <h2>Eviction Policies | 驱逐策略</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.pool.policy.EvictionPolicy.IdleTime} - Idle time based eviction</li>
 *   <li>{@link cloud.opencode.base.pool.policy.EvictionPolicy.LRU} - Least Recently Used</li>
 *   <li>{@link cloud.opencode.base.pool.policy.EvictionPolicy.LFU} - Least Frequently Used</li>
 *   <li>{@link cloud.opencode.base.pool.policy.EvictionPolicy.Composite} - Combined policies</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
package cloud.opencode.base.pool.policy;
