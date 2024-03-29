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
package com.iofairy.rainforest.zip.utils;

import com.iofairy.falcon.fs.FileName;
import com.iofairy.falcon.fs.FilePath;
import com.iofairy.falcon.fs.PathInfo;
import com.iofairy.falcon.io.IOs;
import com.iofairy.falcon.io.MultiByteArrayOutputStream;
import com.iofairy.falcon.zip.ArchiveFormat;
import com.iofairy.falcon.zip.ArchiveType;
import com.iofairy.tcf.Close;
import com.iofairy.top.G;
import org.apache.commons.compress.compressors.gzip.*;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.iofairy.falcon.zip.ArchiveFormat.*;

/**
 * 解压缩工具类
 *
 * @since 0.0.1
 */
public class ZipKit {
    /**
     * 获取解压后的文件名
     *
     * @param fileName      文件名
     * @param archiveFormat 只接收 {@link ArchiveType#COMPRESSION_ONLY} 的 ArchiveFormat 或 [TGZ, TBZ2, TLZ, TXZ, TZST] 这些类型其一。
     * @return 解压后的文件名
     * @since 0.2.0
     */
    public static String getUncompressedName(String fileName, ArchiveFormat archiveFormat) {
        if (G.hasNull(fileName, archiveFormat)) throw new NullPointerException("参数`fileName`，`archiveFormat`不能为null！");
        List<ArchiveFormat> archiveFormats = Arrays.asList(TGZ, TBZ2, TLZ, TXZ, TZST);
        boolean isContains = archiveFormats.contains(archiveFormat);
        if (!(archiveFormat.archiveTypes.contains(ArchiveType.COMPRESSION_ONLY) || isContains))
            throw new IllegalArgumentException("参数`archiveFormat`的`archiveTypes`需包含\"ArchiveType.COMPRESSION_ONLY\" 或 `archiveFormat`是[TGZ, TBZ2, TLZ, TXZ, TZST] 这些类型其一！");

        String extName = archiveFormat.extName;
        if (!fileName.endsWith(extName)) throw new IllegalArgumentException("当前`fileName`必须以[" + extName + "]为后缀，当前fileName为：" + fileName);
        PathInfo pathInfo = FilePath.info(fileName);
        FileName fn = pathInfo.getFileName();
        if (fn.name.length() < extName.length() + 1) throw new IllegalArgumentException("参数`fileName`除后缀名以外的名称不能为空！");

        String fileNameInZip = fn.name.substring(0, fn.name.length() - extName.length());
        return isContains ? fileNameInZip + ".tar" : fileNameInZip;
    }

    /**
     * 获取gzip中的文件名称。
     *
     * @param gcis         GzipCompressorInputStream流
     * @param gzipFileName gzip包的名称，如：<code>gzip包.csv.gz</code>，则内部的文件名为：<code>gzip包.csv</code>
     * @param fromCharset  gzip包的文件名编码，一般为：ISO-8859-1
     * @param toCharset    gzip包的文件名编码，一般为：GBK
     * @return gzip内部文件名
     * @deprecated Since version 0.2.0, replaced by {@link #getUncompressedName(String, ArchiveFormat)}
     */
    @Deprecated
    public static String fileNameInGzip(GzipCompressorInputStream gcis, String gzipFileName, Charset fromCharset, Charset toCharset) {
        if (gcis == null && G.isEmpty(gzipFileName)) throw new NullPointerException("参数 gcis 与 gzipFileName 不能都为 null！");
        String filename = gcis == null ? null : gcis.getMetaData().getFilename();
        if (G.isEmpty(filename)) {
            Objects.requireNonNull(gzipFileName, "参数 gzipFileName 不能都为 null！");
            String newGzipFileName = FilePath.info(gzipFileName).getFileName().name;
            return GzipUtils.getUncompressedFilename(newGzipFileName);
        } else {
            return new String(filename.getBytes(fromCharset), toCharset);
        }
    }

    /**
     * gzip压缩输入流，并转为 byte数组返回<br>
     * <b>注：此处 InputStream 输入流不能关闭，调用此方法的地方还会继续使用</b>
     *
     * @param in          输入流
     * @param fileName    压缩成gzip后，其内部的文件名
     * @param fromCharset gzip包的文件名编码，一般为：GBK
     * @param toCharset   gzip包的文件名编码，一般为：ISO-8859-1
     * @return gzip压缩的 byte数组
     * @throws Exception 处理过程可能抛异常
     * @deprecated Since version 0.2.0
     */
    @Deprecated
    public static byte[][] gzip(InputStream in, String fileName, Charset fromCharset, Charset toCharset) throws Exception {
        final MultiByteArrayOutputStream bos = new MultiByteArrayOutputStream();
        GzipCompressorOutputStream gcos = null;
        try {
            GzipParameters gzipParameters = new GzipParameters();
            gzipParameters.setFilename(new String(fileName.getBytes(fromCharset), toCharset));
            gcos = new GzipCompressorOutputStream(bos, gzipParameters);
            IOs.copy(in, gcos);
        } finally {
            Close.close(gcos);
        }
        // 返回必须在关闭gos后进行，因为关闭时会自动执行finish()方法，保证数据全部写出
        return bos.toByteArrays();
    }

}
