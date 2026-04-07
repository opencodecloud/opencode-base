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

package cloud.opencode.base.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive tests for TaggedCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.3
 */
@DisplayName("TaggedCache Tests")
class TaggedCacheTest {

    private Cache<String, String> baseCache;
    private TaggedCache<String, String> taggedCache;

    @BeforeEach
    void setUp() {
        baseCache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build();
        taggedCache = TaggedCache.wrap(baseCache);
    }

    @Nested
    @DisplayName("Put With Tags")
    class PutWithTagsTests {

        @Test
        @DisplayName("put with tags stores value and associates tags")
        void putWithTagsStoresValueAndTags() {
            taggedCache.put("key1", "value1", "tagA", "tagB");

            assertThat(taggedCache.get("key1")).isEqualTo("value1");
            assertThat(taggedCache.getTags("key1")).containsExactlyInAnyOrder("tagA", "tagB");
            assertThat(taggedCache.getKeysByTag("tagA")).containsExactly("key1");
            assertThat(taggedCache.getKeysByTag("tagB")).containsExactly("key1");
        }

        @Test
        @DisplayName("put with no tags stores value without tags")
        void putWithNoTagsStoresValue() {
            taggedCache.put("key1", "value1");

            assertThat(taggedCache.get("key1")).isEqualTo("value1");
            assertThat(taggedCache.getTags("key1")).isEmpty();
        }

        @Test
        @DisplayName("putWithTtl with tags stores value and associates tags")
        void putWithTtlAndTags() {
            taggedCache.putWithTtl("key1", "value1", Duration.ofMinutes(5), "tagA");

            assertThat(taggedCache.get("key1")).isEqualTo("value1");
            assertThat(taggedCache.getTags("key1")).containsExactly("tagA");
            assertThat(taggedCache.getKeysByTag("tagA")).containsExactly("key1");
        }

        @Test
        @DisplayName("put overwrites value but preserves existing tags")
        void putOverwritesValuePreservesTags() {
            taggedCache.put("key1", "value1", "tagA");
            taggedCache.put("key1", "value2");

            assertThat(taggedCache.get("key1")).isEqualTo("value2");
            assertThat(taggedCache.getTags("key1")).containsExactly("tagA");
        }

        @Test
        @DisplayName("put with tags overwrites value and adds new tags")
        void putWithTagsAddsNewTags() {
            taggedCache.put("key1", "value1", "tagA");
            taggedCache.put("key1", "value2", "tagB");

            assertThat(taggedCache.get("key1")).isEqualTo("value2");
            assertThat(taggedCache.getTags("key1")).containsExactlyInAnyOrder("tagA", "tagB");
        }
    }

    @Nested
    @DisplayName("Invalidate By Tag")
    class InvalidateByTagTests {

        @Test
        @DisplayName("invalidateByTag removes all entries with that tag")
        void invalidateByTagRemovesAllEntries() {
            taggedCache.put("key1", "value1", "tagA");
            taggedCache.put("key2", "value2", "tagA");
            taggedCache.put("key3", "value3", "tagB");

            taggedCache.invalidateByTag("tagA");

            assertThat(taggedCache.get("key1")).isNull();
            assertThat(taggedCache.get("key2")).isNull();
            assertThat(taggedCache.get("key3")).isEqualTo("value3");
        }

        @Test
        @DisplayName("invalidateByTag cleans up tag indexes")
        void invalidateByTagCleansIndexes() {
            taggedCache.put("key1", "value1", "tagA", "tagB");
            taggedCache.put("key2", "value2", "tagA");

            taggedCache.invalidateByTag("tagA");

            assertThat(taggedCache.getKeysByTag("tagA")).isEmpty();
            assertThat(taggedCache.getKeysByTag("tagB")).isEmpty();
            assertThat(taggedCache.getTags("key1")).isEmpty();
            assertThat(taggedCache.getTags("key2")).isEmpty();
            assertThat(taggedCache.getAllTags()).doesNotContain("tagA");
        }

        @Test
        @DisplayName("invalidateByTag on nonexistent tag does nothing")
        void invalidateByNonexistentTagDoesNothing() {
            taggedCache.put("key1", "value1", "tagA");

            taggedCache.invalidateByTag("nonexistent");

            assertThat(taggedCache.get("key1")).isEqualTo("value1");
            assertThat(taggedCache.getTags("key1")).containsExactly("tagA");
        }

        @Test
        @DisplayName("invalidateByTags removes entries for multiple tags")
        void invalidateByTagsRemovesMultiple() {
            taggedCache.put("key1", "value1", "tagA");
            taggedCache.put("key2", "value2", "tagB");
            taggedCache.put("key3", "value3", "tagC");

            taggedCache.invalidateByTags("tagA", "tagB");

            assertThat(taggedCache.get("key1")).isNull();
            assertThat(taggedCache.get("key2")).isNull();
            assertThat(taggedCache.get("key3")).isEqualTo("value3");
        }

        @Test
        @DisplayName("invalidateByTags with null does nothing")
        void invalidateByTagsWithNullDoesNothing() {
            taggedCache.put("key1", "value1", "tagA");

            taggedCache.invalidateByTags((String[]) null);

            assertThat(taggedCache.get("key1")).isEqualTo("value1");
        }
    }

    @Nested
    @DisplayName("Multi-Tag")
    class MultiTagTests {

        @Test
        @DisplayName("one key can have multiple tags")
        void oneKeyMultipleTags() {
            taggedCache.put("key1", "value1", "tagA", "tagB", "tagC");

            assertThat(taggedCache.getTags("key1")).containsExactlyInAnyOrder("tagA", "tagB", "tagC");
            assertThat(taggedCache.getKeysByTag("tagA")).containsExactly("key1");
            assertThat(taggedCache.getKeysByTag("tagB")).containsExactly("key1");
            assertThat(taggedCache.getKeysByTag("tagC")).containsExactly("key1");
        }

        @Test
        @DisplayName("one tag can have multiple keys")
        void oneTagMultipleKeys() {
            taggedCache.put("key1", "value1", "shared");
            taggedCache.put("key2", "value2", "shared");
            taggedCache.put("key3", "value3", "shared");

            assertThat(taggedCache.getKeysByTag("shared"))
                    .containsExactlyInAnyOrder("key1", "key2", "key3");
        }

        @Test
        @DisplayName("invalidating one tag does not affect other tags on same key")
        void invalidateOneTagAffectsOnlyThatTag() {
            taggedCache.put("key1", "value1", "tagA", "tagB");
            taggedCache.put("key2", "value2", "tagB");

            taggedCache.invalidateByTag("tagA");

            // key1 was invalidated (had tagA)
            assertThat(taggedCache.get("key1")).isNull();
            // key2 still present (only had tagB)
            assertThat(taggedCache.get("key2")).isEqualTo("value2");
            assertThat(taggedCache.getKeysByTag("tagB")).containsExactly("key2");
        }

        @Test
        @DisplayName("addTags appends tags to existing key")
        void addTagsAppendsToExistingKey() {
            taggedCache.put("key1", "value1", "tagA");
            taggedCache.addTags("key1", "tagB", "tagC");

            assertThat(taggedCache.getTags("key1")).containsExactlyInAnyOrder("tagA", "tagB", "tagC");
            assertThat(taggedCache.getKeysByTag("tagB")).containsExactly("key1");
        }

        @Test
        @DisplayName("addTags on nonexistent key adds to index (cleaned up lazily)")
        void addTagsOnNonexistentKeyAddsToIndex() {
            taggedCache.addTags("nonexistent", "tagA");

            // Tags are added to the index even if the key is not in the cache;
            // stale entries are cleaned up during invalidate/invalidateByTag.
            assertThat(taggedCache.getKeysByTag("tagA")).containsExactly("nonexistent");
            assertThat(taggedCache.getAllTags()).containsExactly("tagA");

            // Invalidating the key cleans up the stale tag index entry
            taggedCache.invalidate("nonexistent");
            assertThat(taggedCache.getKeysByTag("tagA")).isEmpty();
            assertThat(taggedCache.getAllTags()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tag Cleanup")
    class TagCleanupTests {

        @Test
        @DisplayName("invalidate(key) cleans up tag indexes")
        void invalidateKeyCleansTagIndex() {
            taggedCache.put("key1", "value1", "tagA", "tagB");
            taggedCache.put("key2", "value2", "tagA");

            taggedCache.invalidate("key1");

            assertThat(taggedCache.getTags("key1")).isEmpty();
            assertThat(taggedCache.getKeysByTag("tagA")).containsExactly("key2");
            assertThat(taggedCache.getKeysByTag("tagB")).isEmpty();
        }

        @Test
        @DisplayName("invalidateAll(keys) cleans up tag indexes")
        void invalidateAllKeysCleansTagIndex() {
            taggedCache.put("key1", "value1", "tagA");
            taggedCache.put("key2", "value2", "tagA");
            taggedCache.put("key3", "value3", "tagB");

            taggedCache.invalidateAll(java.util.List.of("key1", "key2"));

            assertThat(taggedCache.getKeysByTag("tagA")).isEmpty();
            assertThat(taggedCache.getKeysByTag("tagB")).containsExactly("key3");
        }

        @Test
        @DisplayName("getAndRemove cleans up tag indexes")
        void getAndRemoveCleansTagIndex() {
            taggedCache.put("key1", "value1", "tagA");

            String value = taggedCache.getAndRemove("key1");

            assertThat(value).isEqualTo("value1");
            assertThat(taggedCache.getTags("key1")).isEmpty();
            assertThat(taggedCache.getKeysByTag("tagA")).isEmpty();
        }

        @Test
        @DisplayName("empty tag set is cleaned from tagToKeys")
        void emptyTagSetCleanedFromIndex() {
            taggedCache.put("key1", "value1", "tagA");

            taggedCache.invalidate("key1");

            assertThat(taggedCache.getAllTags()).doesNotContain("tagA");
        }
    }

    @Nested
    @DisplayName("Invalidate All")
    class InvalidateAllTests {

        @Test
        @DisplayName("invalidateAll clears all entries and indexes")
        void invalidateAllClearsAll() {
            taggedCache.put("key1", "value1", "tagA");
            taggedCache.put("key2", "value2", "tagB");
            taggedCache.put("key3", "value3", "tagA", "tagB");

            taggedCache.invalidateAll();

            assertThat(taggedCache.get("key1")).isNull();
            assertThat(taggedCache.get("key2")).isNull();
            assertThat(taggedCache.get("key3")).isNull();
            assertThat(taggedCache.getAllTags()).isEmpty();
            assertThat(taggedCache.getTags("key1")).isEmpty();
            assertThat(taggedCache.getTags("key2")).isEmpty();
            assertThat(taggedCache.getTags("key3")).isEmpty();
        }

        @Test
        @DisplayName("invalidateAll on empty cache does nothing")
        void invalidateAllEmptyCacheDoesNothing() {
            taggedCache.invalidateAll();

            assertThat(taggedCache.estimatedSize()).isZero();
            assertThat(taggedCache.getAllTags()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("null tag in put throws NullPointerException")
        void nullTagInPutThrows() {
            assertThatThrownBy(() -> taggedCache.put("key1", "value1", (String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null tag in invalidateByTag throws NullPointerException")
        void nullTagInInvalidateByTagThrows() {
            assertThatThrownBy(() -> taggedCache.invalidateByTag(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null key in getTags throws NullPointerException")
        void nullKeyInGetTagsThrows() {
            assertThatThrownBy(() -> taggedCache.getTags(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null tag in getKeysByTag throws NullPointerException")
        void nullTagInGetKeysByTagThrows() {
            assertThatThrownBy(() -> taggedCache.getKeysByTag(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null tag in getTagSize throws NullPointerException")
        void nullTagInGetTagSizeThrows() {
            assertThatThrownBy(() -> taggedCache.getTagSize(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("getKeysByTag for nonexistent tag returns empty set")
        void getKeysByNonexistentTagReturnsEmpty() {
            assertThat(taggedCache.getKeysByTag("nonexistent")).isEmpty();
        }

        @Test
        @DisplayName("getTags for untagged key returns empty set")
        void getTagsForUntaggedKeyReturnsEmpty() {
            taggedCache.put("key1", "value1");
            assertThat(taggedCache.getTags("key1")).isEmpty();
        }

        @Test
        @DisplayName("getTagSize for nonexistent tag returns zero")
        void getTagSizeForNonexistentTagReturnsZero() {
            assertThat(taggedCache.getTagSize("nonexistent")).isZero();
        }

        @Test
        @DisplayName("getTagSize returns correct count")
        void getTagSizeReturnsCorrectCount() {
            taggedCache.put("key1", "value1", "tagA");
            taggedCache.put("key2", "value2", "tagA");
            taggedCache.put("key3", "value3", "tagB");

            assertThat(taggedCache.getTagSize("tagA")).isEqualTo(2);
            assertThat(taggedCache.getTagSize("tagB")).isEqualTo(1);
        }

        @Test
        @DisplayName("wrap with null throws NullPointerException")
        void wrapWithNullThrows() {
            assertThatThrownBy(() -> TaggedCache.wrap(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("wrap returns same instance for already wrapped")
        void wrapReturnsSameForWrapped() {
            TaggedCache<String, String> doubleWrapped = TaggedCache.wrap(taggedCache);
            assertThat(doubleWrapped).isSameAs(taggedCache);
        }

        @Test
        @DisplayName("getAllTags returns all known tags")
        void getAllTagsReturnsAllKnown() {
            taggedCache.put("key1", "value1", "tagA", "tagB");
            taggedCache.put("key2", "value2", "tagC");

            assertThat(taggedCache.getAllTags()).containsExactlyInAnyOrder("tagA", "tagB", "tagC");
        }
    }

    @Nested
    @DisplayName("Delegate")
    class DelegateTests {

        @Test
        @DisplayName("get delegates to underlying cache")
        void getDelegates() {
            baseCache.put("key1", "value1");

            assertThat(taggedCache.get("key1")).isEqualTo("value1");
        }

        @Test
        @DisplayName("containsKey delegates to underlying cache")
        void containsKeyDelegates() {
            taggedCache.put("key1", "value1");

            assertThat(taggedCache.containsKey("key1")).isTrue();
            assertThat(taggedCache.containsKey("key2")).isFalse();
        }

        @Test
        @DisplayName("size delegates to underlying cache")
        void sizeDelegates() {
            taggedCache.put("key1", "value1", "tagA");
            taggedCache.put("key2", "value2", "tagA");

            assertThat(taggedCache.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("keys delegates to underlying cache")
        void keysDelegates() {
            taggedCache.put("key1", "value1", "tagA");
            taggedCache.put("key2", "value2", "tagB");

            assertThat(taggedCache.keys()).containsExactlyInAnyOrder("key1", "key2");
        }

        @Test
        @DisplayName("name delegates to underlying cache")
        void nameDelegates() {
            assertThat(taggedCache.name()).isEqualTo(baseCache.name());
        }

        @Test
        @DisplayName("putIfAbsent delegates to underlying cache")
        void putIfAbsentDelegates() {
            taggedCache.put("key1", "value1");

            boolean result = taggedCache.putIfAbsent("key1", "value2");
            assertThat(result).isFalse();
            assertThat(taggedCache.get("key1")).isEqualTo("value1");

            boolean result2 = taggedCache.putIfAbsent("key2", "value2");
            assertThat(result2).isTrue();
            assertThat(taggedCache.get("key2")).isEqualTo("value2");
        }

        @Test
        @DisplayName("stats delegates to underlying cache")
        void statsDelegates() {
            assertThat(taggedCache.stats()).isNotNull();
        }

        @Test
        @DisplayName("cleanUp delegates to underlying cache")
        void cleanUpDelegates() {
            taggedCache.put("key1", "value1");
            taggedCache.cleanUp();
            // No exception = success
        }
    }
}
