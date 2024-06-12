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
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * 7zip解压时的属性设置
 *
 * @since 0.2.0
 */
@Getter
@ToString
public class SevenZipInputProperty implements ArchiveInputProperty {
    /**
     * 文件名编码
     */
    private String fileNameEncoding = "GBK";
    /**
     * 默认的 SevenZFile.Builder
     */
    private SevenZFile.Builder sevenZFileBuilder = SevenZFile.builder();

    public SevenZipInputProperty() {
    }

    public static SevenZipInputProperty of() {
        return new SevenZipInputProperty();
    }

    public SevenZipInputProperty setFileNameEncoding(String fileNameEncoding) {
        if (!Charset.isSupported(fileNameEncoding)) throw new UnsupportedCharsetException(fileNameEncoding);

        this.fileNameEncoding = fileNameEncoding;
        return this;
    }

    public SevenZipInputProperty setSevenZFileBuilder(SevenZFile.Builder sevenZFileBuilder) {
        if (sevenZFileBuilder != null) {
            this.sevenZFileBuilder = sevenZFileBuilder;
        }
        return this;
    }

}
