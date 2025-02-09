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

import com.github.luben.zstd.BufferPool;
import com.github.luben.zstd.NoPool;
import com.github.luben.zstd.ZstdDictDecompress;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Zstd (Zstandard)解压时的属性设置
 *
 * @since 0.5.12
 */
@Getter
@Setter
@ToString
public class ZstdInputProperty implements ArchiveInputProperty {
    /**
     * the pool to fetch and return buffers
     */
    @Accessors(chain = true)
    BufferPool bufferPool = NoPool.INSTANCE;
    /**
     * Don't break on unfinished frames
     * Use case: decompressing files that are not yet finished writing and compressing
     */
    @Accessors(chain = true)
    boolean continuous = false;
    /**
     * the dictionary buffer
     */
    @Accessors(chain = true)
    byte[] dict;
    /**
     * the dictionary
     */
    @Accessors(chain = true)
    ZstdDictDecompress dictDecompress;
    /**
     * 通过限制窗口大小的最大值（单位：字节），避免解压超大窗口时占用过多内存
     */
    @Accessors(chain = true)
    Integer windowLogMax;
    /**
     * Enable or disable support for multiple dictionary references. <br>
     * Enables references table for DDict, so the DDict used for decompression will be determined per the dictId in the frame, default: false
     */
    @Accessors(chain = true)
    boolean useMultiple = false;


    public ZstdInputProperty() {
    }

    public static ZstdInputProperty of() {
        return new ZstdInputProperty();
    }

}
