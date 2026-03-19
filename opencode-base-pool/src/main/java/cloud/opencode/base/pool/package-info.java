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
 * Pool Package - High-Performance Object Pool (JDK 25)
 * Pool包 - 高性能对象池 (JDK 25)
 *
 * <p>This package provides a high-performance, configurable object pool
 * implementation with Virtual Thread support and JDK 25 features.</p>
 * <p>此包提供高性能、可配置的对象池实现，支持虚拟线程和JDK 25特性。</p>
 *
 * <h2>Core Interfaces | 核心接口</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.pool.ObjectPool} - Object pool interface</li>
 *   <li>{@link cloud.opencode.base.pool.KeyedObjectPool} - Keyed object pool interface</li>
 *   <li>{@link cloud.opencode.base.pool.PooledObject} - Pooled object wrapper</li>
 *   <li>{@link cloud.opencode.base.pool.PooledObjectFactory} - Object factory interface</li>
 * </ul>
 *
 * <h2>Entry Point | 入口点</h2>
 * <ul>
 *   <li>{@link cloud.opencode.base.pool.OpenPool} - Facade entry class</li>
 *   <li>{@link cloud.opencode.base.pool.PoolConfig} - Pool configuration</li>
 * </ul>
 *
 * <h2>Quick Start | 快速开始</h2>
 * <pre>{@code
 * // Create factory
 * PooledObjectFactory<Connection> factory = new BasePooledObjectFactory<>() {
 *     @Override
 *     protected Connection create() {
 *         return DriverManager.getConnection(url);
 *     }
 * };
 *
 * // Create pool
 * ObjectPool<Connection> pool = OpenPool.createPool(factory,
 *     OpenPool.configBuilder()
 *         .maxTotal(20)
 *         .testOnBorrow(true)
 *         .build());
 *
 * // Use pool
 * pool.execute(conn -> {
 *     conn.executeQuery("SELECT...");
 * });
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
package cloud.opencode.base.pool;
