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
package com.iofairy.rainforest.zip.config;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 压缩包的文件名
 *
 * @since 0.0.3
 */
@Data
@Accessors(chain = true)
public class ZipFileName implements Cloneable {
    private String zipName;             // .zip
    private String rarName;             // .rar
    private String sevenZName;          // .7z
    private String zZipName;            // .zz
    private String brotliName;          // .br
    private String bzip2Name;           // .bz2
    private String gzipName;            // .gz
    private String lzipName;            // .lz
    private String lz4Name;             // .lz4
    private String lzmaName;            // .lzma
    private String lzopName;            // .lzo
    private String rzipName;            // .rz
    private String snappyName;          // .sz
    private String xzName;              // .xz
    private String zPackName;           // .z
    private String zCompressName;       // .Z
    private String zStandardName;       // .zst
    private String jarName;             // .jar
    private String warName;             // .war

    public static ZipFileName of() {
        return new ZipFileName();
    }

    @Override
    public ZipFileName clone() {
        try {
            ZipFileName other = (ZipFileName) super.clone();
            other.zipName = this.zipName;
            other.rarName = this.rarName;
            other.sevenZName = this.sevenZName;
            other.zZipName = this.zZipName;
            other.brotliName = this.brotliName;
            other.bzip2Name = this.bzip2Name;
            other.gzipName = this.gzipName;
            other.lzipName = this.lzipName;
            other.lz4Name = this.lz4Name;
            other.lzmaName = this.lzmaName;
            other.lzopName = this.lzopName;
            other.rzipName = this.rzipName;
            other.snappyName = this.snappyName;
            other.xzName = this.xzName;
            other.zPackName = this.zPackName;
            other.zCompressName = this.zCompressName;
            other.zStandardName = this.zStandardName;
            other.jarName = this.jarName;
            other.warName = this.warName;
            return other;
        } catch (Exception e) {
            // this shouldn't happen, since we are Cloneable
        }
        return null;
    }

}
