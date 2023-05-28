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
import lombok.ToString;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.tukaani.xz.LZMA2Options;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * 7zip压缩时的属性设置
 *
 * @since 0.2.0
 */
@Getter
@ToString
public class SevenZipOutputProperty implements ArchiveInputProperty {
    /**
     * 文件名编码
     */
    private String fileNameEncoding = "GBK";
    /**
     * 默认压缩方法“存储”（使用其他方法非常非常慢）
     */
    private SevenZMethod sevenZMethod = SevenZMethod.COPY;

    public SevenZipOutputProperty() {
    }

    public static SevenZipOutputProperty of() {
        return new SevenZipOutputProperty();
    }

    public SevenZipOutputProperty setFileNameEncoding(String fileNameEncoding) {
        if (!Charset.isSupported(fileNameEncoding)) throw new UnsupportedCharsetException(fileNameEncoding);

        this.fileNameEncoding = fileNameEncoding;
        return this;
    }

    public SevenZipOutputProperty setSevenZMethod(SevenZMethod sevenZMethod) {
        if (sevenZMethod != null) {
            this.sevenZMethod = sevenZMethod;
        }
        return this;
    }

}
