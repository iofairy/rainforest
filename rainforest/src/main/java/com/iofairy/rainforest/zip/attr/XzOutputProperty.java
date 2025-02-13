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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZ;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * xz压缩时的属性设置
 *
 * @since 0.2.0
 */
@Getter
@ToString
public class XzOutputProperty implements ArchiveInputProperty {
    /**
     * 文件名编码
     */
    private String fileNameEncoding = "GBK";
    /**
     * 预设0-3是快速预设与中等压缩。预设4-6是相当慢的预设与高压缩。默认值为 6。<br>
     * 预设7-9类似于预设6，但使用更大的字典，并有更高的压缩器和解压缩器内存要求。<br>
     * 除非未压缩的文件大小超过8mib、16mib或32mib，否则分别使用预置的7、8或9会浪费内存。
     */
    @Setter
    @Accessors(chain = true)
    private int preset = LZMA2Options.PRESET_DEFAULT;
    /**
     * 过滤操作。这个值不为空时，将使用该值作为压缩级别，而忽略 {@link #preset} 的值
     */
    @Setter
    @Accessors(chain = true)
    FilterOptions[] filterOptions;
    /**
     * Type of the integrity check, for example {@link XZ#CHECK_CRC64}
     */
    @Setter
    @Accessors(chain = true)
    int checkType = XZ.CHECK_CRC64;
    /**
     * Cache to be used for allocating large arrays
     */
    @Setter
    @Accessors(chain = true)
    ArrayCache arrayCache = ArrayCache.getDefaultCache();

    public XzOutputProperty() {
    }

    public static XzOutputProperty of() {
        return new XzOutputProperty();
    }

    public XzOutputProperty setFileNameEncoding(String fileNameEncoding) {
        if (!Charset.isSupported(fileNameEncoding)) throw new UnsupportedCharsetException(fileNameEncoding);

        this.fileNameEncoding = fileNameEncoding;
        return this;
    }

}
