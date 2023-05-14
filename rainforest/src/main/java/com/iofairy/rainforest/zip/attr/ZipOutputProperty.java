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
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.compress.archivers.zip.Zip64Mode;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;

/**
 * ZIP压缩时的属性设置
 *
 * @since 0.0.3
 */
@Getter
@ToString
public class ZipOutputProperty implements ArchiveOutputProperty {
    /**
     * Compression level for next entry.
     */
    @Setter
    @Accessors(chain = true)
    private int level = Deflater.DEFAULT_COMPRESSION;
    /**
     * Default compression method for next entry.
     */
    @Setter
    @Accessors(chain = true)
    private int method = ZipEntry.DEFLATED;
    /**
     * 64位压缩模式
     */
    private Zip64Mode zip64Mode = Zip64Mode.AlwaysWithCompatibility;
    /**
     * 文件名编码
     */
    private String fileNameEncoding = "GBK";
    /**
     * zip4j 库 ZipParameters
     */
    private ZipParameters zipParameters = new ZipParameters();

    public ZipOutputProperty() {
        zipParameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
    }

    public static ZipOutputProperty of() {
        return new ZipOutputProperty();
    }

    public ZipOutputProperty setZip64Mode(Zip64Mode zip64Mode) {
        if (zip64Mode != null) {
            this.zip64Mode = zip64Mode;
        }
        return this;
    }

    public ZipOutputProperty setFileNameEncoding(String fileNameEncoding) {
        if (!Charset.isSupported(fileNameEncoding)) throw new UnsupportedCharsetException(fileNameEncoding);

        this.fileNameEncoding = fileNameEncoding;
        return this;
    }

    public ZipOutputProperty setZipParameters(ZipParameters zipParameters) {
        if (zipParameters != null) {
            this.zipParameters = zipParameters;
        }
        return this;
    }

}
