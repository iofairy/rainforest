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
package com.iofairy.rainforest.zip.ac;

import com.iofairy.falcon.fs.FileName;
import com.iofairy.falcon.fs.FilePath;
import com.iofairy.falcon.fs.PathInfo;
import com.iofairy.falcon.io.IOs;
import com.iofairy.falcon.io.MultiByteArrayInputStream;
import com.iofairy.falcon.io.MultiByteArrayOutputStream;
import com.iofairy.falcon.zip.ArchiveFormat;
import com.iofairy.lambda.*;
import com.iofairy.rainforest.zip.base.*;
import com.iofairy.top.G;
import com.iofairy.tuple.Tuple;
import com.iofairy.tuple.Tuple2;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import static com.iofairy.falcon.zip.ArchiveFormat.*;

/**
 * 接口 {@link SuperAC}工具类
 *
 * @since 0.2.0
 */
public class SuperACs {
    private static final char[] LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] NUMBERS = "0123456789".toCharArray();

    public static String getUnzipId(int length) {
        if (length < 2) throw new IllegalArgumentException("参数`length`必须 >= 2！");

        final Random random = new Random();
        char[] result = new char[length];
        // 第一位是字母
        result[0] = LETTERS[random.nextInt(LETTERS.length)];
        // 第二位是字母
        result[1] = LETTERS[random.nextInt(LETTERS.length)];

        // 后面都是数字
        for (int i = 2; i < length; i++) {
            result[i] = NUMBERS[random.nextInt(NUMBERS.length)];
        }

        return new String(result);
    }


    public static <R> void unzip(InputStream currentIs,
                                 ArrayList<R> rs,
                                 String zipFileName,
                                 String entryFileName,
                                 int unzipTimes,
                                 int unzipLevel,
                                 int newUnzipTimes,
                                 int newUnzipLevel,
                                 Map<ArchiveFormat, SuperAC> unzipACMap,
                                 PT3<? super Integer, ? super String, ? super String, Exception> unzipFilter,
                                 PT3<? super Integer, ? super String, ? super String, Exception> otherFilter,
                                 PT3<? super Integer, ? super String, ? super String, Exception> beforeUnzipFilter,
                                 RT4<InputStream, ? super Integer, ? super String, ? super String, ? extends R, Exception> beforeUnzipAction,
                                 RT4<InputStream, ? super Integer, ? super String, ? super String, ? extends R, Exception> otherAction,
                                 ZipLogLevel zipLogLevel,
                                 String unzipId,
                                 String logSource) throws Exception {
        /*
         * 这里的 entryFileName 已经是文件，而不是目录，目录在上面过滤掉了
         */
        PathInfo pathInfo = FilePath.info(entryFileName);
        FileName fileName = pathInfo.getFileName();

        ArchiveFormat archiveFormat = ArchiveFormat.of(fileName.ext1);
        boolean isMultiExtsFormat = ArchiveFormat.isMultiExtsFormat(archiveFormat); // 判断是否是多扩展名的格式
        // 单扩展名的格式
        if (!isMultiExtsFormat) archiveFormat = ArchiveFormat.of(fileName.ext);

        SuperAC superAC = unzipACMap.get(archiveFormat);

        if (superAC != null) {
            InputStream entryIs = currentIs;
            if (beforeUnzipFilter != null && beforeUnzipFilter.$(unzipTimes, zipFileName, entryFileName) && beforeUnzipAction != null) {
                entryIs = IOs.toMultiBAIS(entryIs);
                // 打印日志信息
                LogPrinter.printBeforeAfter(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, "前");

                R r = beforeUnzipAction.$(entryIs, unzipTimes, zipFileName, entryFileName);
                rs.add(r);
                ((MultiByteArrayInputStream) entryIs).reset();      // 重复利用 MultiByteArrayInputStream，后续还要使用
            }

            if (unzipLevel != 0) {
                if (unzipFilter == null || unzipFilter.$(unzipTimes, zipFileName, entryFileName)) {
                    // 打印日志信息
                    long startTime = System.currentTimeMillis();
                    LogPrinter.printBeforeUnzip(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);
                    /*
                     * 解压文件
                     */
                    List<R> tmpTs = superAC.unzip(entryIs, entryFileName, newUnzipTimes, newUnzipLevel, false,
                            unzipFilter, otherFilter, beforeUnzipFilter, beforeUnzipAction, otherAction, zipLogLevel, unzipACMap);
                    rs.addAll(tmpTs);

                    // 打印日志信息
                    LogPrinter.printAfterUnzip(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, startTime);

                }
            }
        } else {
            if ((otherFilter == null || otherFilter.$(unzipTimes, zipFileName, entryFileName)) && otherAction != null) {
                // 打印日志信息
                long startTime = System.currentTimeMillis();
                LogPrinter.printBeforeOther(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);
                /*
                 * 文件处理
                 */
                rs.add(otherAction.$(currentIs, unzipTimes, zipFileName, entryFileName));
                // 打印日志信息
                LogPrinter.printAfterOther(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, startTime);

            }
        }
    }


    public static <R> byte[][] reZip(InputStream currentIs,
                                     ArrayList<R> rs,
                                     String zipFileName,
                                     String entryFileName,
                                     int unzipTimes,
                                     int unzipLevel,
                                     int newUnzipTimes,
                                     int newUnzipLevel,
                                     Map<ArchiveFormat, SuperAC> reZipACMap,
                                     PT2<? super Integer, ? super String, Exception> addFileFilter,
                                     PT3<? super Integer, ? super String, ? super String, Exception> deleteFileFilter,
                                     PT3<? super Integer, ? super String, ? super String, Exception> unzipFilter,
                                     PT3<? super Integer, ? super String, ? super String, Exception> otherFilter,
                                     PT3<? super Integer, ? super String, ? super String, Exception> beforeUnzipFilter,
                                     PT3<? super Integer, ? super String, ? super String, Exception> afterZipFilter,
                                     RT2<? super Integer, ? super String, Tuple2<List<AddFile>, List<R>>, Exception> addFilesAction,
                                     RT2<? super Integer, ? super String, Tuple2<List<AddBytes>, List<R>>, Exception> addBytesAction,
                                     RT4<InputStream, ? super Integer, ? super String, ? super String, ? extends R, Exception> deleteFileAction,
                                     RT4<InputStream, ? super Integer, ? super String, ? super String, ? extends R, Exception> beforeUnzipAction,
                                     RT4<InputStream, ? super Integer, ? super String, ? super String, ? extends R, Exception> afterZipAction,
                                     RT5<InputStream, OutputStream, ? super Integer, ? super String, ? super String, ? extends R, Exception> otherAction,
                                     ZipLogLevel zipLogLevel,
                                     String unzipId,
                                     String logSource) throws Exception {
        MultiByteArrayOutputStream entryBaos = new MultiByteArrayOutputStream();
        byte[][] byteArrays;
        /*
         * 这里的 entryFileName 已经是文件，而不是目录，目录在上面过滤掉了
         */
        PathInfo pathInfo = FilePath.info(entryFileName);
        FileName fileName = pathInfo.getFileName();

        ArchiveFormat archiveFormat = ArchiveFormat.of(fileName.ext1);
        boolean isMultiExtsFormat = ArchiveFormat.isMultiExtsFormat(archiveFormat); // 判断是否是多扩展名的格式
        // 单扩展名的格式
        if (!isMultiExtsFormat) archiveFormat = ArchiveFormat.of(fileName.ext);

        SuperAC superAC = reZipACMap.get(archiveFormat);

        if (superAC != null) {
            InputStream entryIs = currentIs;

            boolean isRunBeforeUnzipAction = beforeUnzipFilter != null && beforeUnzipFilter.$(unzipTimes, zipFileName, entryFileName) && beforeUnzipAction != null;
            boolean isRunAfterZipAction = afterZipFilter != null && afterZipFilter.$(unzipTimes, zipFileName, entryFileName) && afterZipAction != null;

            // afterZipAction 没有用到 entryIs
            if (isRunBeforeUnzipAction) entryIs = IOs.toMultiBAIS(entryIs);

            if (isRunBeforeUnzipAction) {
                // 打印日志信息
                LogPrinter.printBeforeAfter(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, "前");

                R r = beforeUnzipAction.$(entryIs, unzipTimes, zipFileName, entryFileName);
                rs.add(r);
                ((MultiByteArrayInputStream) entryIs).reset();      // 重复利用 MultiByteArrayInputStream，后续还要使用
            }

            if (unzipLevel != 0 && (unzipFilter == null || unzipFilter.$(unzipTimes, zipFileName, entryFileName))) {
                // 打印日志信息
                long startTime = System.currentTimeMillis();
                LogPrinter.printBeforeUnzip(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);
                /*
                 * 解压并重压缩文件
                 */
                ZipResult<R> zipResult = superAC.reZip(entryIs, entryFileName, newUnzipTimes, newUnzipLevel, false, addFileFilter,
                        deleteFileFilter, unzipFilter, otherFilter, beforeUnzipFilter, afterZipFilter, addFilesAction, addBytesAction,
                        deleteFileAction, beforeUnzipAction, afterZipAction, otherAction, zipLogLevel, reZipACMap);
                rs.addAll(zipResult.getResults());
                byteArrays = zipResult.getBytes();

                // 打印日志信息
                LogPrinter.printAfterUnzip(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, startTime);

            } else {
                // 打印日志信息
                LogPrinter.printFilterLogs(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);

                IOs.copy(entryIs, entryBaos);
                byteArrays = entryBaos.toByteArrays();
            }

            // 这段代码需要放在此处，即使压缩包没有被修改。因为可能 isRunBeforeUnzipAction 为false，有些操作就放在 此处执行
            if (isRunAfterZipAction) {
                // 打印日志信息
                LogPrinter.printBeforeAfter(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, "后");

                MultiByteArrayInputStream afterZipIs = new MultiByteArrayInputStream(byteArrays);
                R r = afterZipAction.$(afterZipIs, unzipTimes, zipFileName, entryFileName);
                rs.add(r);
            }

        } else {
            if ((otherFilter == null || otherFilter.$(unzipTimes, zipFileName, entryFileName)) && otherAction != null) {
                // 打印日志信息
                long startTime = System.currentTimeMillis();
                LogPrinter.printBeforeOther(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);

                /*
                 * 文件处理
                 */
                R r = otherAction.$(currentIs, entryBaos, unzipTimes, zipFileName, entryFileName);
                rs.add(r);

                // 打印日志信息
                LogPrinter.printAfterOther(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, startTime);

            } else {
                // 打印日志信息
                LogPrinter.printFilterLogs(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);

                IOs.copy(currentIs, entryBaos);
            }
            byteArrays = entryBaos.toByteArrays();
        }
        return byteArrays;
    }


    static Tuple2<Map<ArchiveFormat, SuperAC>, SuperAC> checkParameters(InputStream is, ArchiveFormat inputStreamType, List<SuperAC> superACs) {
        if (G.hasNull(is, inputStreamType)) throw new NullPointerException("参数`is`或`inputStreamType`不能为null！");
        if (G.isEmpty(superACs)) throw new NullPointerException("参数`superACs`不能为null或空！");

        Map<ArchiveFormat, SuperAC> superACMap = toSuperACMap(superACs);

        SuperAC superAC = superACMap.get(inputStreamType);
        if (superAC == null) throw new IllegalArgumentException("在参数`superACs`中未找到与`inputStreamType`相匹配 superAC 对象！");

        return Tuple.of(superACMap, superAC);
    }

    public static Map<ArchiveFormat, SuperAC> toSuperACMap(List<SuperAC> superACs) {
        Map<ArchiveFormat, SuperAC> superACMap = new HashMap<>();
        for (SuperAC superAC : superACs) {
            if (superAC == null) continue;
            superACMap.put(superAC.format(), superAC);
        }
        fillMap(superACMap);
        return Collections.unmodifiableMap(superACMap);
    }

    public static Map<ArchiveFormat, SuperAC> toSuperACMap(Map<ArchiveFormat, SuperAC> map) {
        Map<ArchiveFormat, SuperAC> superACMap = new HashMap<>(map);
        fillMap(superACMap);
        return Collections.unmodifiableMap(superACMap);
    }

    private static void fillMap(Map<ArchiveFormat, SuperAC> superACMap) {
        fillMap(superACMap, TAR_GZ, TGZ);
        fillMap(superACMap, TAR_BZ2, TBZ2);
        fillMap(superACMap, TAR_LZ, TLZ);
        fillMap(superACMap, TAR_XZ, TXZ);
        fillMap(superACMap, TAR_ZST, TZST);
    }

    private static void fillMap(Map<ArchiveFormat, SuperAC> superACMap, ArchiveFormat format1, ArchiveFormat format2) {
        if (superACMap.containsKey(format1) && !superACMap.containsKey(format2)) superACMap.put(format2, superACMap.get(format1));
        else if (superACMap.containsKey(format2) && !superACMap.containsKey(format1)) superACMap.put(format1, superACMap.get(format2));
    }

}
