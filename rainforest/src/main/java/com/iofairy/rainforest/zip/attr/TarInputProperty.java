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
import org.apache.commons.compress.archivers.tar.TarConstants;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * tar包解压时的属性设置
 *
 * @since 0.2.0
 */
@Getter
@ToString
public class TarInputProperty implements ArchiveInputProperty {
    /**
     * 文件名编码
     */
    private String fileNameEncoding = "UTF-8";
    /**
     * the block size to use
     */
    @Setter
    @Accessors(chain = true)
    private int blockSize = TarConstants.DEFAULT_BLKSIZE;
    /**
     * the record size to use
     */
    @Setter
    @Accessors(chain = true)
    private int recordSize = TarConstants.DEFAULT_RCDSIZE;
    /**
     * 当设置为true时，组/用户id，模式，设备编号和时间戳的非法值将被忽略，并且字段设置为TarArchiveEntry.UNKNOWN。当设置为false时，这些非法字段将导致异常。
     */
    @Setter
    @Accessors(chain = true)
    private boolean lenient = false;

    public TarInputProperty() {
    }

    public static TarInputProperty of() {
        return new TarInputProperty();
    }

    public TarInputProperty setFileNameEncoding(String fileNameEncoding) {
        if (!Charset.isSupported(fileNameEncoding)) throw new UnsupportedCharsetException(fileNameEncoding);

        this.fileNameEncoding = fileNameEncoding;
        return this;
    }

}
