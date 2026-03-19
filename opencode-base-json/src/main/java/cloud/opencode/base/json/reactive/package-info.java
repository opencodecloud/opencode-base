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
 * Reactive JSON Processing Package
 * 响应式JSON处理包
 *
 * <p>This package provides reactive streaming APIs for JSON processing using
 * Java's Flow API (Reactive Streams). It enables non-blocking, backpressure-aware
 * processing of large JSON documents.</p>
 * <p>此包使用Java的Flow API（响应式流）提供响应式流式JSON处理API。
 * 它支持对大型JSON文档进行非阻塞、支持背压的处理。</p>
 *
 * <p><strong>Key Classes | 关键类:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.json.reactive.ReactiveJsonReader} - Reactive JSON reader</li>
 *   <li>{@link cloud.opencode.base.json.reactive.ReactiveJsonWriter} - Reactive JSON writer</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
package cloud.opencode.base.json.reactive;
