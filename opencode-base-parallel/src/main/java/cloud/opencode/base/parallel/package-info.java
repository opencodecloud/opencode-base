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
 * OpenCode Parallel - Modern Parallel Computing Utilities
 * OpenCode 并行 - 现代化并行计算工具
 *
 * <p>This package provides comprehensive parallel computing utilities built on
 * JDK 25 virtual threads and structured concurrency.</p>
 * <p>此包提供基于 JDK 25 虚拟线程和结构化并发的综合并行计算工具。</p>
 *
 * <h2>Core Classes | 核心类</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.parallel.OpenParallel} - Static facade for parallel operations</li>
 *   <li>{@link cloud.opencode.base.parallel.OpenStructured} - Structured concurrency facade</li>
 * </ul>
 *
 * <h2>Sub-packages | 子包</h2>
 * <ul>
 *   <li>{@code cloud.opencode.base.parallel.pipeline} - Async pipeline and functions</li>
 *   <li>{@code cloud.opencode.base.parallel.batch} - Batch processing utilities</li>
 *   <li>{@code cloud.opencode.base.parallel.executor} - Virtual thread executors</li>
 *   <li>{@code cloud.opencode.base.parallel.structured} - Structured concurrency tools</li>
 *   <li>{@code cloud.opencode.base.parallel.exception} - Parallel exceptions</li>
 * </ul>
 *
 * <h2>Example | 示例</h2>
 * <pre>{@code
 * // Parallel execution
 * OpenParallel.runAll(() -> taskA(), () -> taskB());
 *
 * // Parallel with results
 * List<String> results = OpenParallel.invokeAll(
 *     () -> fetchA(),
 *     () -> fetchB()
 * );
 *
 * // Structured concurrency
 * Result result = OpenStructured.parallel(
 *     () -> fetchUser(),
 *     () -> fetchOrders(),
 *     (user, orders) -> new Result(user, orders)
 * );
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
package cloud.opencode.base.parallel;
