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

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * xz解压时的属性设置
 *
 * @since 0.2.0
 */
@Getter
@ToString
public class XzInputProperty implements ArchiveInputProperty {
    /**
     * 文件名编码
     */
    private String fileNameEncoding = "GBK";
    /**
     * 如果为真，则一直解压缩直到输入的结尾；
     * 如果为假，则在第一个.xz流之后停止，并使输入位置指向.xz流之后的下一个字节。
     */
    @Setter
    @Accessors(chain = true)
    boolean decompressConcatenated = false;
    /**
     * 内存限制（单位：kibibytes (KiB)），超出内存限制，则会抛出 {@link org.apache.commons.compress.MemoryLimitException}
     */
    @Setter
    @Accessors(chain = true)
    int memoryLimitInKb = -1;
    /**
     * If {@code true}, the integrity checks will be verified; this should almost never be set to {@code false}
     */
    @Setter
    @Accessors(chain = true)
    boolean verifyCheck;
    /**
     * Cache to be used for allocating large arrays
     */
    @Setter
    @Accessors(chain = true)
    ArrayCache arrayCache = ArrayCache.getDefaultCache();

    public XzInputProperty() {
    }

    public static XzInputProperty of() {
        return new XzInputProperty();
    }

    public XzInputProperty setFileNameEncoding(String fileNameEncoding) {
        if (!Charset.isSupported(fileNameEncoding)) throw new UnsupportedCharsetException(fileNameEncoding);

        this.fileNameEncoding = fileNameEncoding;
        return this;
    }

}
