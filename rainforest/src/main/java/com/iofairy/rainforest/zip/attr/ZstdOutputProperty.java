/*
 * Copyright (C) 2021 iofairy, <https://github.com/iofairy/rainforest>
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
package com.iofairy.rainforest.zip.attr;

import com.github.luben.zstd.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Zstd (Zstandard)压缩时的属性设置
 *
 * @since 0.5.12
 */
@Getter
@Setter
@ToString
public class ZstdOutputProperty implements ArchiveInputProperty {
    /**
     * the pool to fetch and return buffers
     */
    @Accessors(chain = true)
    BufferPool bufferPool = NoPool.INSTANCE;
    /**
     * the dictionary buffer
     */
    @Accessors(chain = true)
    byte[] dict;
    /**
     * the dictionary
     */
    @Accessors(chain = true)
    ZstdDictCompress dictCompress;
    /**
     * Enable checksums for the compressed stream.
     * <p>
     * Default: false
     */
    @Accessors(chain = true)
    boolean useChecksums = false;
    /**
     * Set the compression level.
     * <p>
     * Default: {@link Zstd#defaultCompressionLevel()}
     */
    @Accessors(chain = true)
    Integer level;
    /**
     * Set the Long Distance Matching.
     * <p>
     * Values for windowLog outside the range 10-27 will disable and reset LDM
     */
    @Accessors(chain = true)
    Integer longDistanceMatching;
    /**
     * Enable use of worker threads for parallel compression.
     * <p>
     * Default: no worker threads.
     */
    @Accessors(chain = true)
    Integer workers;
    /**
     * Advanced Compression Option: Set the amount of data reloaded from the previous job. <br>
     * See <a href="https://facebook.github.io/zstd/zstd_manual.html#Chapter5">Advanced compression API</a> for more information.
     */
    @Accessors(chain = true)
    Integer overlapLog;
    /**
     * Advanced Compression Option: Set the size of each compression job. Only applies when multi threaded compression is enabled. <br>
     * See <a href="https://facebook.github.io/zstd/zstd_manual.html#Chapter5">Advanced compression API</a> for more information.
     */
    @Accessors(chain = true)
    Integer jobSize;
    /**
     * Advanced Compression Option: Set the target match length. <br>
     * See <a href="https://facebook.github.io/zstd/zstd_manual.html#Chapter5">Advanced compression API</a> for more information.
     */
    @Accessors(chain = true)
    Integer targetLength;
    /**
     * Advanced Compression Option: Set the minimum match length. <br>
     * See <a href="https://facebook.github.io/zstd/zstd_manual.html#Chapter5">Advanced compression API</a> for more information.
     */
    @Accessors(chain = true)
    Integer minMatch;
    /**
     * Advanced Compression Option: Set the maximum number of searches in a hash chain or a binary tree using logarithmic scale. <br>
     * See <a href="https://facebook.github.io/zstd/zstd_manual.html#Chapter5">Advanced compression API</a> for more information.
     */
    @Accessors(chain = true)
    Integer searchLog;
    /**
     * Advanced Compression Option: Set the maximum number of bits for the secondary search structure. <br>
     * See <a href="https://facebook.github.io/zstd/zstd_manual.html#Chapter5">Advanced compression API</a> for more information.
     */
    @Accessors(chain = true)
    Integer chainLog;
    /**
     * Advanced Compression Option: Set the maximum number of bits for a hash table. <br>
     * See <a href="https://facebook.github.io/zstd/zstd_manual.html#Chapter5">Advanced compression API</a> for more information.
     */
    @Accessors(chain = true)
    Integer hashLog;
    /**
     * Advanced Compression Option: Set the maximum number of bits for a match distance. <br>
     * See <a href="https://facebook.github.io/zstd/zstd_manual.html#Chapter5">Advanced compression API</a> for more information.
     */
    @Accessors(chain = true)
    Integer windowLog;
    /**
     * Advanced Compression Option: Set the strategy used by a match finder. <br>
     * See <a href="https://facebook.github.io/zstd/zstd_manual.html#Chapter5">Advanced compression API</a> for more information.
     */
    @Accessors(chain = true)
    Integer strategy;
    /**
     * Enable closing the frame on flush. <br>
     * <p>
     * This will guarantee that it can be ready fully if the process crashes
     * before closing the stream. On the downside it will negatively affect
     * the compression ratio. <br>
     * <p>
     * Default: false.
     */
    @Accessors(chain = true)
    boolean closeFrameOnFlush = false;


    public ZstdOutputProperty() {
    }

    public static ZstdOutputProperty of() {
        return new ZstdOutputProperty();
    }

}
