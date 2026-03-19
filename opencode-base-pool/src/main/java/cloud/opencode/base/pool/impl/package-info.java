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
 * Impl Package - Pool Implementations
 * 实现包 - 池实现
 *
 * <p>This package provides concrete implementations of object pools
 * optimized for different use cases.</p>
 * <p>此包提供针对不同用例优化的对象池具体实现。</p>
 *
 * <h2>Classes | 类</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.pool.impl.GenericObjectPool} - General-purpose pool</li>
 *   <li>{@link cloud.opencode.base.pool.impl.GenericKeyedObjectPool} - Keyed pool for multi-tenant</li>
 *   <li>{@link cloud.opencode.base.pool.impl.SoftReferencePool} - GC-friendly pool</li>
 *   <li>{@link cloud.opencode.base.pool.impl.ThreadLocalPool} - Per-thread pool</li>
 * </ul>
 *
 * <h2>Choosing a Pool | 选择池</h2>
 * <ul>
 *   <li>GenericObjectPool - Most common use cases - 最常见用例</li>
 *   <li>GenericKeyedObjectPool - Multi-datasource/tenant - 多数据源/租户</li>
 *   <li>SoftReferencePool - Memory-constrained - 内存受限环境</li>
 *   <li>ThreadLocalPool - Thread-affine workloads - 线程亲和工作负载</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
package cloud.opencode.base.pool.impl;
