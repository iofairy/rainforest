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

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * GZIP解压时的属性设置
 *
 * @since 0.0.3
 */
@Getter
@ToString
public class GzipInputProperty implements ArchiveInputProperty {
    /**
     * 文件名编码1
     */
    private String fileNameEncoding1 = "ISO-8859-1";
    /**
     * 文件名编码2
     */
    private String fileNameEncoding2 = "GBK";
    /**
     * 如果为真，则一直解压缩直到输入的结尾；
     * 如果为假，则在第一个.gz流之后停止，并使输入位置指向.gz流之后的下一个字节。
     */
    @Setter
    @Accessors(chain = true)
    boolean decompressConcatenated = false;

    public GzipInputProperty() {
    }

    public static GzipInputProperty of() {
        return new GzipInputProperty();
    }

    public GzipInputProperty setFileNameEncoding1(String fileNameEncoding) {
        if (!Charset.isSupported(fileNameEncoding)) throw new UnsupportedCharsetException(fileNameEncoding);

        this.fileNameEncoding1 = fileNameEncoding;
        return this;
    }

    public GzipInputProperty setFileNameEncoding2(String fileNameEncoding) {
        if (!Charset.isSupported(fileNameEncoding)) throw new UnsupportedCharsetException(fileNameEncoding);

        this.fileNameEncoding2 = fileNameEncoding;
        return this;
    }

}
