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
 * ZIP解压时的属性设置
 *
 * @since 0.0.3
 */
@Getter
@ToString
public class ZipInputProperty implements ArchiveInputProperty {
    /**
     * 文件名编码
     */
    private String fileNameEncoding = "GBK";
    /**
     * 是否使用InfoZIP Unicode Extra Fields(如果有)来设置文件名。
     */
    @Setter
    @Accessors(chain = true)
    private boolean useUnicodeExtraFields = true;
    /**
     * zip流是否尝试读取使用数据描述符的STORED条目
     */
    @Setter
    @Accessors(chain = true)
    private boolean allowStoredEntriesWithDataDescriptor = false;
    /**
     * zip流是否在开始时尝试跳过zip拆分签名(08074B50)。如果要读取拆分归档，则需要将此设置为true。
     */
    @Setter
    @Accessors(chain = true)
    private boolean skipSplitSig = false;

    public ZipInputProperty() {
    }

    public static ZipInputProperty of() {
        return new ZipInputProperty();
    }

    public ZipInputProperty setFileNameEncoding(String fileNameEncoding) {
        if (!Charset.isSupported(fileNameEncoding)) throw new UnsupportedCharsetException(fileNameEncoding);

        this.fileNameEncoding = fileNameEncoding;
        return this;
    }

}
