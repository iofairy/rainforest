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
import org.apache.commons.compress.compressors.gzip.GzipParameters;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * GZIP压缩时的属性设置
 *
 * @since 0.0.3
 */
@Getter
@ToString
public class GzipOutputProperty implements ArchiveOutputProperty {
    /**
     * 文件名编码1
     */
    private String fileNameEncoding1 = "ISO-8859-1";
    /**
     * 文件名编码2
     */
    private String fileNameEncoding2 = "GBK";

    private GzipParameters gzipParameters = new GzipParameters();

    public GzipOutputProperty() {
    }

    public static GzipOutputProperty of() {
        return new GzipOutputProperty();
    }

    public GzipOutputProperty setFileNameEncoding1(String fileNameEncoding) {
        if (!Charset.isSupported(fileNameEncoding)) throw new UnsupportedCharsetException(fileNameEncoding);

        this.fileNameEncoding1 = fileNameEncoding;
        return this;
    }

    public GzipOutputProperty setFileNameEncoding2(String fileNameEncoding) {
        if (!Charset.isSupported(fileNameEncoding)) throw new UnsupportedCharsetException(fileNameEncoding);

        this.fileNameEncoding2 = fileNameEncoding;
        return this;
    }

    public GzipOutputProperty setGzipParameters(GzipParameters gzipParameters) {
        if (gzipParameters != null) {
            this.gzipParameters = gzipParameters;
        }
        return this;
    }
}
