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
import com.iofairy.falcon.zip.ArchiveFormat;
import com.iofairy.falcon.zip.ArchiveType;

import java.util.Arrays;
import java.util.List;

import static com.iofairy.falcon.zip.ArchiveFormat.*;
import static com.iofairy.validator.Preconditions.*;

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
     * @param archiveFormat 只接收 {@link ArchiveType#COMPRESSION_ONLY} 的 ArchiveFormat 或 [TGZ, TAZ, TZ, TBZ2, TLZ, TXZ, TZST] 这些类型其一。
     * @return 解压后的文件名
     * @since 0.2.0
     */
    public static String getUncompressedName(String fileName, ArchiveFormat archiveFormat) {
        checkHasNullNPE(args(fileName, archiveFormat), args("fileName", "archiveFormat"));

        List<ArchiveFormat> archiveFormats = Arrays.asList(TGZ, TAZ, TZ, TBZ2, TLZ, TXZ, TZST);
        boolean isContains = archiveFormats.contains(archiveFormat);

        checkArgument(!(archiveFormat.archiveTypes.contains(ArchiveType.COMPRESSION_ONLY) || isContains),
                "参数`archiveFormat`的`archiveTypes`需包含\"ArchiveType.COMPRESSION_ONLY\" 或 `archiveFormat`是 ${…} 这些类型其一！", archiveFormats);

        String extName = archiveFormat.extName;
        checkArgument(!fileName.endsWith(extName), "当前`fileName`必须以[${…}]为后缀，当前fileName为：${…}", extName, fileName);

        FileName fn = FilePath.info(fileName).getFileName();
        checkArgument(fn.name.length() < extName.length() + 1, "参数`fileName`除后缀名以外的名称不能为空！");

        String fileNameInZip = fn.name.substring(0, fn.name.length() - extName.length());
        return isContains ? fileNameInZip + ".tar" : fileNameInZip;
    }

}
