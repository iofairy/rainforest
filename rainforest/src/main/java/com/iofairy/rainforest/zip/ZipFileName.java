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
package com.iofairy.rainforest.zip;


/**
 * 压缩包的文件名，针对归档类型为 {@link ArchiveType#COMPRESSION_ONLY}
 *
 * @since 0.0.3
 */
public class ZipFileName implements Cloneable {
    private String brotliName;        // .br
    private String bzip2Name;         // .bz2
    private String gzipName;          // .gz
    private String lzipName;          // .lz
    private String lz4Name;           // .lz4
    private String lzmaName;          // .lzma
    private String lzopName;          // .lzo
    private String rzipName;          // .rz
    private String snappyName;        // .sz
    private String xzName;            // .xz
    private String zPackName;         // .z
    private String zCompressName;     // .Z
    private String zstandardName;     // .zst

    public static ZipFileName of() {
        return new ZipFileName();
    }

    public String getBrotliName() {
        return brotliName;
    }

    public ZipFileName setBrotliName(String brotliName) {
        this.brotliName = brotliName;
        return this;
    }

    public String getBzip2Name() {
        return bzip2Name;
    }

    public ZipFileName setBzip2Name(String bzip2Name) {
        this.bzip2Name = bzip2Name;
        return this;
    }

    public String getGzipName() {
        return gzipName;
    }

    public ZipFileName setGzipName(String gzipName) {
        this.gzipName = gzipName;
        return this;
    }

    public String getLzipName() {
        return lzipName;
    }

    public ZipFileName setLzipName(String lzipName) {
        this.lzipName = lzipName;
        return this;
    }

    public String getLz4Name() {
        return lz4Name;
    }

    public ZipFileName setLz4Name(String lz4Name) {
        this.lz4Name = lz4Name;
        return this;
    }

    public String getLzmaName() {
        return lzmaName;
    }

    public ZipFileName setLzmaName(String lzmaName) {
        this.lzmaName = lzmaName;
        return this;
    }

    public String getLzopName() {
        return lzopName;
    }

    public ZipFileName setLzopName(String lzopName) {
        this.lzopName = lzopName;
        return this;
    }

    public String getRzipName() {
        return rzipName;
    }

    public ZipFileName setRzipName(String rzipName) {
        this.rzipName = rzipName;
        return this;
    }

    public String getSnappyName() {
        return snappyName;
    }

    public ZipFileName setSnappyName(String snappyName) {
        this.snappyName = snappyName;
        return this;
    }

    public String getXzName() {
        return xzName;
    }

    public ZipFileName setXzName(String xzName) {
        this.xzName = xzName;
        return this;
    }

    public String getZPackName() {
        return zPackName;
    }

    public ZipFileName setZPackName(String zPackName) {
        this.zPackName = zPackName;
        return this;
    }

    public String getZCompressName() {
        return zCompressName;
    }

    public ZipFileName setZCompressName(String zCompressName) {
        this.zCompressName = zCompressName;
        return this;
    }

    public String getZstandardName() {
        return zstandardName;
    }

    public ZipFileName setZstandardName(String zstandardName) {
        this.zstandardName = zstandardName;
        return this;
    }

    @Override
    public ZipFileName clone() {
        try {
            ZipFileName other = (ZipFileName) super.clone();
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
            other.zstandardName = this.zstandardName;
            return other;
        } catch (Exception e) {
            // this shouldn't happen, since we are Cloneable
        }
        return null;
    }

    @Override
    public String toString() {
        return "ZipFileName{" +
                "brotliName='" + brotliName + '\'' +
                ", bzip2Name='" + bzip2Name + '\'' +
                ", gzipName='" + gzipName + '\'' +
                ", lzipName='" + lzipName + '\'' +
                ", lz4Name='" + lz4Name + '\'' +
                ", lzmaName='" + lzmaName + '\'' +
                ", lzopName='" + lzopName + '\'' +
                ", rzipName='" + rzipName + '\'' +
                ", snappyName='" + snappyName + '\'' +
                ", xzName='" + xzName + '\'' +
                ", zPackName='" + zPackName + '\'' +
                ", zCompressName='" + zCompressName + '\'' +
                ", zstandardName='" + zstandardName + '\'' +
                '}';
    }
}
