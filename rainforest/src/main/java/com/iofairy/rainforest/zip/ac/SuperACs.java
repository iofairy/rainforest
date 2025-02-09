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

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.iofairy.falcon.fs.FileName;
import com.iofairy.falcon.fs.FilePath;
import com.iofairy.falcon.fs.PathInfo;
import com.iofairy.falcon.io.IOs;
import com.iofairy.falcon.io.MultiByteArrayInputStream;
import com.iofairy.falcon.io.MultiByteArrayOutputStream;
import com.iofairy.falcon.time.Stopwatch;
import com.iofairy.falcon.zip.ArchiveFormat;
import com.iofairy.lambda.*;
import com.iofairy.rainforest.zip.attr.ZstdInputProperty;
import com.iofairy.rainforest.zip.attr.ZstdOutputProperty;
import com.iofairy.rainforest.zip.base.*;
import com.iofairy.top.O;
import com.iofairy.tuple.Tuple;
import com.iofairy.tuple.Tuple2;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import static com.iofairy.falcon.zip.ArchiveFormat.*;
import static com.iofairy.falcon.misc.Preconditions.*;

/**
 * 接口 {@link SuperAC} 的抽象实现类，用于放置公共方法
 *
 * @since 0.2.0
 */
public abstract class SuperACs implements SuperAC {
    private static final char[] LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] NUMBERS = "0123456789".toCharArray();

    protected Map<ArchiveFormat, SuperAC> unzipACMap;
    protected Map<ArchiveFormat, SuperAC> reZipACMap;


    protected static <R> void unzip(InputStream currentIs,
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
        SuperAC superAC = getSuperAC(entryFileName, unzipACMap);

        if (superAC != null) {
            /*
             * 为了避免 currentIs 在后续的 superAC.unzip 中被关闭，这里先复制一个
             */
            try (MultiByteArrayInputStream entryIs = IOs.toMultiBAIS(currentIs)) {
                if (beforeUnzipFilter != null && beforeUnzipFilter.$(unzipTimes, zipFileName, entryFileName) && beforeUnzipAction != null) {
                    // 打印日志信息
                    LogPrinter.printBeforeAfter(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, "前");

                    R r = beforeUnzipAction.$(entryIs, unzipTimes, zipFileName, entryFileName);
                    rs.add(r);
                    entryIs.reset();      // 重复利用 MultiByteArrayInputStream，后续还要使用
                }

                if (unzipLevel != 0) {
                    if (unzipFilter == null || unzipFilter.$(unzipTimes, zipFileName, entryFileName)) {
                        // 打印日志信息
                        Stopwatch stopwatch = Stopwatch.run();
                        LogPrinter.printBeforeUnzip(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);
                        /*
                         * 解压文件
                         */
                        List<R> tmpTs = superAC.unzip(entryIs, entryFileName, newUnzipTimes, newUnzipLevel,
                                unzipFilter, otherFilter, beforeUnzipFilter, beforeUnzipAction, otherAction, zipLogLevel, unzipACMap);
                        rs.addAll(tmpTs);

                        // 打印日志信息
                        LogPrinter.printAfterUnzip(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, stopwatch);

                    }
                }
            }
        } else {
            if ((otherFilter == null || otherFilter.$(unzipTimes, zipFileName, entryFileName)) && otherAction != null) {
                // 打印日志信息
                Stopwatch stopwatch = Stopwatch.run();
                LogPrinter.printBeforeOther(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);
                /*
                 * 文件处理
                 */
                rs.add(otherAction.$(currentIs, unzipTimes, zipFileName, entryFileName));
                // 打印日志信息
                LogPrinter.printAfterOther(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, stopwatch);

            }
        }

    }


    protected static <R> void unzipFast(InputStream currentIs,
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
                                        RT5<InputStream, ? super Integer, ? super String, ? super String, ? super Set<AutoCloseable>, ? extends R, Exception> otherAction,
                                        ZipLogLevel zipLogLevel,
                                        String unzipId,
                                        String logSource,
                                        Set<AutoCloseable> closeables) throws Exception {
        SuperAC superAC = getSuperAC(entryFileName, unzipACMap);

        if (superAC != null) {
            if (unzipLevel != 0) {
                if (unzipFilter == null || unzipFilter.$(unzipTimes, zipFileName, entryFileName)) {
                    // 打印日志信息
                    Stopwatch stopwatch = Stopwatch.run();
                    LogPrinter.printBeforeUnzip(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);
                    /*
                     * 解压文件
                     */
                    List<R> tmpTs = superAC.unzipFast(currentIs, entryFileName, newUnzipTimes, newUnzipLevel,
                            unzipFilter, otherFilter, otherAction, zipLogLevel, unzipACMap, closeables);
                    rs.addAll(tmpTs);

                    // 打印日志信息
                    LogPrinter.printAfterUnzip(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, stopwatch);

                }
            }
        } else {
            if ((otherFilter == null || otherFilter.$(unzipTimes, zipFileName, entryFileName)) && otherAction != null) {
                // 打印日志信息
                Stopwatch stopwatch = Stopwatch.run();
                LogPrinter.printBeforeOther(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);

                Set<AutoCloseable> tmpCloseables = new LinkedHashSet<>();
                try {
                    /*
                     * 文件处理
                     */
                    rs.add(otherAction.$(currentIs, unzipTimes, zipFileName, entryFileName, tmpCloseables));
                    // 打印日志信息
                    LogPrinter.printAfterOther(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, stopwatch);
                } finally {
                    closeables.addAll(tmpCloseables);
                }
            }
        }

    }


    protected static <R> byte[][] reZip(InputStream currentIs,
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
        SuperAC superAC = getSuperAC(entryFileName, reZipACMap);

        try (MultiByteArrayOutputStream entryOs = new MultiByteArrayOutputStream()) {
            byte[][] byteArrays;

            if (superAC != null) {
                /*
                 * 为了避免 currentIs 在后续的 superAC.reZip 中被关闭，这里先复制一个
                 */
                try (MultiByteArrayInputStream entryIs = IOs.toMultiBAIS(currentIs)) {
                    boolean isRunBeforeUnzipAction = beforeUnzipFilter != null && beforeUnzipFilter.$(unzipTimes, zipFileName, entryFileName) && beforeUnzipAction != null;

                    if (isRunBeforeUnzipAction) {
                        // 打印日志信息
                        LogPrinter.printBeforeAfter(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, "前");

                        R r = beforeUnzipAction.$(entryIs, unzipTimes, zipFileName, entryFileName);
                        rs.add(r);
                        entryIs.reset();      // 重复利用 MultiByteArrayInputStream，后续还要使用
                    }

                    if (unzipLevel != 0 && (unzipFilter == null || unzipFilter.$(unzipTimes, zipFileName, entryFileName))) {
                        // 打印日志信息
                        Stopwatch stopwatch = Stopwatch.run();
                        LogPrinter.printBeforeUnzip(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);
                        /*
                         * 解压并重压缩文件
                         */
                        ZipResult<R> zipResult = superAC.reZip(entryIs, entryFileName, newUnzipTimes, newUnzipLevel, addFileFilter,
                                deleteFileFilter, unzipFilter, otherFilter, beforeUnzipFilter, afterZipFilter, addFilesAction, addBytesAction,
                                deleteFileAction, beforeUnzipAction, afterZipAction, otherAction, zipLogLevel, reZipACMap);
                        rs.addAll(zipResult.getResults());
                        byteArrays = zipResult.getBytes();

                        // 打印日志信息
                        LogPrinter.printAfterUnzip(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, stopwatch);

                    } else {
                        // 打印日志信息
                        LogPrinter.printFilterLogs(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);

                        IOs.copy(entryIs, entryOs);
                        byteArrays = entryOs.toByteArrays();
                    }

                    // 这段代码需要放在此处，即使压缩包没有被修改。因为可能 isRunBeforeUnzipAction 为false，有些操作就放在 此处执行
                    if (afterZipFilter != null && afterZipFilter.$(unzipTimes, zipFileName, entryFileName) && afterZipAction != null) {
                        // 打印日志信息
                        LogPrinter.printBeforeAfter(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, "后");

                        try (MultiByteArrayInputStream afterZipIs = new MultiByteArrayInputStream(byteArrays)) {
                            R r = afterZipAction.$(afterZipIs, unzipTimes, zipFileName, entryFileName);
                            rs.add(r);
                        }
                    }
                }
            } else {
                if ((otherFilter == null || otherFilter.$(unzipTimes, zipFileName, entryFileName)) && otherAction != null) {
                    // 打印日志信息
                    Stopwatch stopwatch = Stopwatch.run();
                    LogPrinter.printBeforeOther(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);

                    /*
                     * 文件处理
                     */
                    R r = otherAction.$(currentIs, entryOs, unzipTimes, zipFileName, entryFileName);
                    rs.add(r);

                    // 打印日志信息
                    LogPrinter.printAfterOther(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, stopwatch);

                } else {
                    // 打印日志信息
                    LogPrinter.printFilterLogs(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);

                    IOs.copy(currentIs, entryOs);
                }
                byteArrays = entryOs.toByteArrays();
            }
            return byteArrays;
        }
    }

    private static SuperAC getSuperAC(String entryFileName, Map<ArchiveFormat, SuperAC> reZipACMap) {
        /*
         * 这里的 entryFileName 已经是文件，而不是目录，目录在之前已经过滤掉了
         */
        PathInfo pathInfo = FilePath.info(entryFileName);
        FileName fileName = pathInfo.getFileName();

        ArchiveFormat archiveFormat = ArchiveFormat.of(fileName.ext1);
        boolean isMultiExtsFormat = ArchiveFormat.isMultiExtsFormat(archiveFormat); // 判断是否是多扩展名的格式
        // 单扩展名的格式
        if (!isMultiExtsFormat) archiveFormat = ArchiveFormat.of(fileName.ext);

        return reZipACMap.get(archiveFormat);
    }

    protected static String getUnzipId(int length) {
        checkArgument(length < 2, "参数`length`必须 >= 2！");

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

    static Tuple2<Map<ArchiveFormat, SuperAC>, SuperAC> checkParameters(InputStream is, ArchiveFormat inputStreamType, List<SuperAC> superACs) {
        checkHasNullNPE(args(is, inputStreamType), args("is", "inputStreamType"));
        checkEmpty(superACs, args("superACs"));

        Map<ArchiveFormat, SuperAC> superACMap = toSuperACMap(superACs);
        SuperAC superAC = superACMap.get(inputStreamType);
        checkArgument(superAC == null, "在参数`superACs`中未找到与`inputStreamType`相匹配 SuperAC 对象！");

        return Tuple.of(superACMap, superAC);
    }

    protected static Map<ArchiveFormat, SuperAC> toSuperACMap(List<SuperAC> superACs) {
        Map<ArchiveFormat, SuperAC> superACMap = new HashMap<>();
        for (SuperAC superAC : superACs) {
            if (superAC == null) continue;
            superACMap.put(superAC.format(), superAC);
        }
        fillMap(superACMap);
        return Collections.unmodifiableMap(superACMap);
    }

    protected static Map<ArchiveFormat, SuperAC> toSuperACMap(Map<ArchiveFormat, SuperAC> map) {
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

    /**
     * 通过 归档文件格式 获取默认的 SuperAC
     *
     * @param archiveFormat 归档文件格式
     * @return 获取默认的 SuperAC
     * @since 0.3.2
     */
    public static SuperAC getSuperAC(ArchiveFormat archiveFormat) {
        switch (archiveFormat) {
            case SEVEN_ZIP:
                return Super7Zip.of();
            case BZIP2:
                return SuperBzip2.of();
            case GZIP:
                return SuperGzip.of();
            case TAR:
                return SuperTar.of();
            case TAR_BZ2:
            case TBZ2:
                return SuperTarBzip2.of();
            case TAR_GZ:
            case TGZ:
                return SuperTarGzip.of();
            case TAR_XZ:
            case TXZ:
                return SuperTarXz.of();
            case XZ:
                return SuperXz.of();
            case ZIP:
                return SuperZip.of();
            case TAR_ZST:
            case TZST:
                return SuperTarZstd.of();
            case ZSTD:
                return SuperZstd.of();
            default:
                return null;
        }
    }


    /**
     * 获取所有支持的默认的SuperAC实例
     *
     * @return 所有支持的默认的SuperAC实例
     * @since 0.3.2
     */
    public static List<SuperAC> allSupportedSuperACs() {
        List<SuperAC> superACs = new ArrayList<>();
        superACs.add(Super7Zip.of());
        superACs.add(SuperBzip2.of());
        superACs.add(SuperGzip.of());
        superACs.add(SuperTar.of());
        superACs.add(SuperTarBzip2.of());
        superACs.add(SuperTarGzip.of());
        superACs.add(SuperTarXz.of());
        superACs.add(SuperXz.of());
        superACs.add(SuperZip.of());
        superACs.add(SuperTarZstd.of());
        superACs.add(SuperZstd.of());
        return superACs;
    }

    /**
     * 向TAR输出流中写入数据
     *
     * @param zos           TarArchiveOutputStream
     * @param entryFileName entry文件名
     * @param byteArrays    byte数组
     * @param entrySize     entry大小
     * @since 0.5.6
     */
    public static void putTarArchiveEntry(TarArchiveOutputStream zos, String entryFileName, byte[][] byteArrays, long entrySize) {
        Throwable suppressed = null;
        try {
            TarArchiveEntry tarArchiveEntry = getTarArchiveEntry(entryFileName, entrySize);
            zos.putArchiveEntry(tarArchiveEntry);
            if (byteArrays != null && entrySize != 0) {
                for (byte[] bytes : byteArrays) {
                    zos.write(bytes);
                }
            }
        } catch (Throwable e) {
            suppressed = e;
            O.sneakyThrows(e);
        } finally {
            closeArchiveEntry(zos, suppressed);
        }
    }

    public static void closeArchiveEntry(ArchiveOutputStream<? extends ArchiveEntry> zos, Throwable suppressed) {
        try {
            zos.closeArchiveEntry();
        } catch (Throwable e) {
            if (suppressed != null) {
                e.addSuppressed(suppressed);
            }
            O.sneakyThrows(e);
        }
    }

    private static TarArchiveEntry getTarArchiveEntry(String entryFileName, long entrySize) {
        TarArchiveEntry archiveEntry = new TarArchiveEntry(entryFileName);
        archiveEntry.setSize(entrySize);
        return archiveEntry;
    }


    protected static void setZstdOutputStreamOptions(ZstdOutputStream zos, ZstdOutputProperty outputProperty) throws IOException {
        zos.setChecksum(outputProperty.isUseChecksums());
        zos.setCloseFrameOnFlush(outputProperty.isCloseFrameOnFlush());
        if (outputProperty.getDict() != null) zos.setDict(outputProperty.getDict());
        if (outputProperty.getDictCompress() != null) zos.setDict(outputProperty.getDictCompress());
        if (outputProperty.getLevel() != null) zos.setLevel(outputProperty.getLevel());
        if (outputProperty.getLongDistanceMatching() != null) zos.setLong(outputProperty.getLongDistanceMatching());
        if (outputProperty.getWorkers() != null) zos.setWorkers(outputProperty.getWorkers());
        if (outputProperty.getOverlapLog() != null) zos.setOverlapLog(outputProperty.getOverlapLog());
        if (outputProperty.getJobSize() != null) zos.setJobSize(outputProperty.getJobSize());
        if (outputProperty.getTargetLength() != null) zos.setTargetLength(outputProperty.getTargetLength());
        if (outputProperty.getMinMatch() != null) zos.setMinMatch(outputProperty.getMinMatch());
        if (outputProperty.getSearchLog() != null) zos.setSearchLog(outputProperty.getSearchLog());
        if (outputProperty.getChainLog() != null) zos.setChainLog(outputProperty.getChainLog());
        if (outputProperty.getHashLog() != null) zos.setHashLog(outputProperty.getHashLog());
        if (outputProperty.getWindowLog() != null) zos.setWindowLog(outputProperty.getWindowLog());
        if (outputProperty.getStrategy() != null) zos.setStrategy(outputProperty.getStrategy());
    }

    protected static void setZstdInputStreamOptions(ZstdInputStream zipis, ZstdInputProperty inputProperty) throws IOException {
        zipis.setContinuous(inputProperty.isContinuous());
        zipis.setRefMultipleDDicts(inputProperty.isUseMultiple());
        if (inputProperty.getDict() != null) zipis.setDict(inputProperty.getDict());
        if (inputProperty.getDictDecompress() != null) zipis.setDict(inputProperty.getDictDecompress());
        if (inputProperty.getWindowLogMax() != null) zipis.setLongMax(inputProperty.getWindowLogMax());
    }


}
