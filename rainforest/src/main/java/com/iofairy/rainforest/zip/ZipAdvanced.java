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

import com.iofairy.except.UnexpectedParameterException;
import com.iofairy.falcon.io.*;
import com.iofairy.lambda.RT3;
import com.iofairy.lambda.RT4;
import com.iofairy.rainforest.io.IOStreams;
import com.iofairy.tcf.Close;
import com.iofairy.top.G;
import com.iofairy.tuple.Tuple;
import com.iofairy.tuple.Tuple2;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiPredicate;

import static com.iofairy.rainforest.zip.ArchiveFormat.*;

/**
 * 解压缩高级方法
 *
 * @since 0.0.1
 */
public class ZipAdvanced {
    /**
     * ZIP高级方法中支持的归档格式类型
     */
    private static final ArchiveFormat[] SUPPORTED_ARCHIVE_FORMATS = {ZIP, GZIP};

    /**
     * 归档压缩包默认的文件名编码
     */
    private static Map<ArchiveFormat, Charset> defaultFileNameCharsetMap = new HashMap<>();

    static {
        Charset charset = Charset.forName("GBK");
        for (ArchiveFormat supportedArchiveFormat : SUPPORTED_ARCHIVE_FORMATS) {
            defaultFileNameCharsetMap.put(supportedArchiveFormat, charset);
        }
    }

    public static Map<ArchiveFormat, Charset> getDefaultFileNameCharsetMap() {
        return defaultFileNameCharsetMap;
    }

    public static void setDefaultFileNameCharsetMap(Map<ArchiveFormat, Charset> fileNameCharsetMap) {
        Objects.requireNonNull(fileNameCharsetMap, "Parameter `fileNameCharsetMap` must be non-null!");
        ZipAdvanced.defaultFileNameCharsetMap = fileNameCharsetMap;
    }

    /**
     * 判断一个归档格式是否支持ZIP高级方法
     *
     * @param archiveFormat 归档格式
     * @return 是否支持
     */
    public static boolean isSupported(ArchiveFormat archiveFormat) {
        return Arrays.asList(SUPPORTED_ARCHIVE_FORMATS).contains(archiveFormat);
    }

    /**
     * 判断一组归档格式是否支持ZIP高级方法
     *
     * @param archiveFormats 一组归档格式
     * @throws UnexpectedParameterException 当有元素不支持时，抛出此异常
     */
    public static void checkIsSupported(ArchiveFormat[] archiveFormats) {
        List<ArchiveFormat> supportedArchiveFormats = Arrays.asList(SUPPORTED_ARCHIVE_FORMATS);
        for (ArchiveFormat archiveFormat : archiveFormats) {
            if (!supportedArchiveFormats.contains(archiveFormat)) {
                throw new UnexpectedParameterException("Unsupported ArchiveFormat `" + archiveFormat + "`, only supported: " + supportedArchiveFormats);
            }
        }
    }

    /**
     * 判断一组归档格式的文件名编码是否未设置，或被设置为 {@code null} 值
     *
     * @param archiveFormats     一组归档格式
     * @param fileNameCharsetMap 所提供的归档格式对应的文件名编码所存放的 map
     */
    private static void checkNullCharset(ArchiveFormat[] archiveFormats, Map<ArchiveFormat, Charset> fileNameCharsetMap) {
        for (ArchiveFormat archiveFormat : archiveFormats) {
            if (!fileNameCharsetMap.containsKey(archiveFormat) || fileNameCharsetMap.get(archiveFormat) == null) {
                throw new RuntimeException(G.toString(archiveFormats) + "'s filename Charset must be set in the `fileNameCharsetMap`, " +
                        "their filename Charset must be non-null！");
            }
        }
    }

    /**
     * 检验 needUnZipFormats 与 fileNameCharsetMap 是否合法
     *
     * @param needUnZipFormats   需要解压的归档格式
     * @param fileNameCharsetMap 需要解压的格式对应的文件名编码
     * @return 返回检验通过的 needUnZipFormats 和 fileNameCharsetMap
     */
    private static Tuple2<List<ArchiveFormat>, Map<ArchiveFormat, Charset>> checkParameters(ArchiveFormat[] needUnZipFormats,
                                                                                            Map<ArchiveFormat, Charset> fileNameCharsetMap) {
        if (needUnZipFormats == null) needUnZipFormats = SUPPORTED_ARCHIVE_FORMATS;
        if (G.isEmpty(fileNameCharsetMap)) fileNameCharsetMap = defaultFileNameCharsetMap;

        checkIsSupported(needUnZipFormats);
        checkNullCharset(needUnZipFormats, fileNameCharsetMap);

        return Tuple.of(Arrays.asList(needUnZipFormats), fileNameCharsetMap);
    }

    /**
     * gzip包的处理逻辑（自动解压）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param fileNameCharsetMap      归档格式文件名编码
     * @param unzipLevel              解压层级。-1：无限解压，碰到压缩包就解压；0、1：只解压当前压缩包；&gt;1：解压次数
     * @param gzipFileName            gzip包的包名
     * @param unzipFilter             内部压缩包的名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param otherFilter             除压缩包以外的文件名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeAfterActionFilter 压缩包解压缩前后的Action前的过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeUnzipAction       解压之前的操作 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param otherAction             非压缩包的处理逻辑 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param <T>                     Action返回值类型
     * @return 返回任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    public static <T> List<T> gzipHandle(InputStream is,
                                         Map<ArchiveFormat, Charset> fileNameCharsetMap,
                                         int unzipLevel,
                                         String gzipFileName,
                                         BiPredicate<Integer, String> unzipFilter,
                                         BiPredicate<Integer, String> otherFilter,
                                         BiPredicate<Integer, String> beforeAfterActionFilter,
                                         RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                         RT3<InputStream, Integer, String, T, Exception> otherAction) throws Exception {
        Tuple2<List<ArchiveFormat>, Map<ArchiveFormat, Charset>> formatsAndMap = checkParameters(null, fileNameCharsetMap);
        return gzipHandle(is, formatsAndMap._1, formatsAndMap._2, 1, unzipLevel, gzipFileName, true,
                unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction);
    }

    /**
     * gzip包的处理逻辑（自动解压）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param needUnZipFormats        需要解压的归档格式（必须是支持的格式 {@link #isSupported(ArchiveFormat)}），不需要解压的格式则按普通文件处理
     * @param fileNameCharsetMap      归档格式文件名编码
     * @param unzipLevel              解压层级。-1：无限解压，碰到压缩包就解压；0、1：只解压当前压缩包；&gt;1：解压次数
     * @param gzipFileName            gzip包的包名
     * @param unzipFilter             内部压缩包的名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param otherFilter             除压缩包以外的文件名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeAfterActionFilter 压缩包解压缩前后的Action前的过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeUnzipAction       解压之前的操作 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param otherAction             非压缩包的处理逻辑 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param <T>                     Action返回值类型
     * @return 返回任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    public static <T> List<T> gzipHandle(InputStream is,
                                         ArchiveFormat[] needUnZipFormats,
                                         Map<ArchiveFormat, Charset> fileNameCharsetMap,
                                         int unzipLevel,
                                         String gzipFileName,
                                         BiPredicate<Integer, String> unzipFilter,
                                         BiPredicate<Integer, String> otherFilter,
                                         BiPredicate<Integer, String> beforeAfterActionFilter,
                                         RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                         RT3<InputStream, Integer, String, T, Exception> otherAction) throws Exception {
        Tuple2<List<ArchiveFormat>, Map<ArchiveFormat, Charset>> formatsAndMap = checkParameters(needUnZipFormats, fileNameCharsetMap);
        return gzipHandle(is, formatsAndMap._1, formatsAndMap._2, 1, unzipLevel, gzipFileName, true,
                unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction);
    }

    /**
     * gzip包的处理逻辑（自动解压）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param needUnZipFormats        需要解压的归档格式（必须是支持的格式 {@link #isSupported(ArchiveFormat)}），不需要解压的格式则按普通文件处理
     * @param fileNameCharsetMap      归档格式文件名编码
     * @param unzipTimes              压缩包的第几层。最开始的压缩包为第一层，压缩包里面的文件是第二层，压缩包里的压缩包再解压，则加一层。以此类推……
     * @param unzipLevel              解压层级。-1：无限解压，碰到压缩包就解压；0、1：只解压当前压缩包；&gt;1：解压次数
     * @param gzipFileName            gzip包的包名
     * @param isCloseStream           是否关闭流
     * @param unzipFilter             内部压缩包的名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param otherFilter             除压缩包以外的文件名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeAfterActionFilter 压缩包解压缩前后的Action前的过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeUnzipAction       解压之前的操作 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param otherAction             非压缩包的处理逻辑 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param <T>                     Action返回值类型
     * @return 返回任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    private static <T> List<T> gzipHandle(InputStream is,
                                          List<ArchiveFormat> needUnZipFormats,
                                          Map<ArchiveFormat, Charset> fileNameCharsetMap,
                                          int unzipTimes,
                                          int unzipLevel,
                                          String gzipFileName,
                                          boolean isCloseStream,
                                          BiPredicate<Integer, String> unzipFilter,
                                          BiPredicate<Integer, String> otherFilter,
                                          BiPredicate<Integer, String> beforeAfterActionFilter,
                                          RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                          RT3<InputStream, Integer, String, T, Exception> otherAction) throws Exception {
        Charset gzipNameCharset = fileNameCharsetMap.get(GZIP);
        ArrayList<T> ts = new ArrayList<>();
        GzipCompressorInputStream gcis = null;
        try {
            gcis = new GzipCompressorInputStream(is);

            int newUnzipTimes = unzipTimes + 1;
            int newUnzipLevel = unzipLevel < 0 ? unzipLevel : unzipLevel - 1;

            String fileNameInGzip = ZipUtils.fileNameInGzip(gcis, gzipFileName, gzipNameCharset);
            String extension = FilenameUtils.getExtension(fileNameInGzip);

            ArchiveFormat archiveFormat = ArchiveFormat.of(extension);
            if (needUnZipFormats.contains(archiveFormat)) {
                InputStream entryIs = gcis;
                if ((beforeAfterActionFilter == null || beforeAfterActionFilter.test(newUnzipTimes, fileNameInGzip)) && beforeUnzipAction != null) {
                    entryIs = IOStreams.transferTo(entryIs);
                    T t = beforeUnzipAction.$(entryIs, newUnzipTimes, fileNameInGzip);
                    ts.add(t);
                    ((MultiByteArrayInputStream) entryIs).reset();      // 重复利用 MultiByteArrayInputStream，后续还要使用
                }

                if (!(unzipLevel == 0 || unzipLevel == 1)) {
                    if (unzipFilter == null || unzipFilter.test(newUnzipTimes, fileNameInGzip)) {
                        List<T> tmpTs = archiveFormat == ZIP
                                ? zipHandle(entryIs, needUnZipFormats, fileNameCharsetMap, newUnzipTimes, newUnzipLevel,
                                false, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction)
                                : gzipHandle(entryIs, needUnZipFormats, fileNameCharsetMap, newUnzipTimes, newUnzipLevel,
                                fileNameInGzip, false, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction);
                        ts.addAll(tmpTs);
                    }
                }
            } else {
                if (otherFilter == null || otherFilter.test(newUnzipTimes, fileNameInGzip)) {
                    ts.add(otherAction.$(gcis, newUnzipTimes, fileNameInGzip));
                }
            }

        } finally {
            if (isCloseStream) {
                Close.close(gcis);
                Close.close(is);
            }
        }
        return ts;
    }

    /**
     * zip包的处理逻辑（自动解压）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param fileNameCharsetMap      归档格式文件名编码
     * @param unzipLevel              解压层级。-1：无限解压，碰到压缩包就解压；0、1：只解压当前压缩包；&gt;1：解压次数
     * @param unzipFilter             内部压缩包的名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param otherFilter             除压缩包以外的文件名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeAfterActionFilter 压缩包解压缩前后的Action前的过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeUnzipAction       解压之前的操作 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param otherAction             非压缩包的处理逻辑 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param <T>                     Action返回值类型
     * @return 返回任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    public static <T> List<T> zipHandle(InputStream is,
                                        Map<ArchiveFormat, Charset> fileNameCharsetMap,
                                        int unzipLevel,
                                        BiPredicate<Integer, String> unzipFilter,
                                        BiPredicate<Integer, String> otherFilter,
                                        BiPredicate<Integer, String> beforeAfterActionFilter,
                                        RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                        RT3<InputStream, Integer, String, T, Exception> otherAction) throws Exception {
        Tuple2<List<ArchiveFormat>, Map<ArchiveFormat, Charset>> formatsAndMap = checkParameters(null, fileNameCharsetMap);
        return zipHandle(is, formatsAndMap._1, formatsAndMap._2, 1, unzipLevel, true,
                unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction);
    }

    /**
     * zip包的处理逻辑（自动解压）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param needUnZipFormats        需要解压的归档格式（必须是支持的格式 {@link #isSupported(ArchiveFormat)}），不需要解压的格式则按普通文件处理
     * @param fileNameCharsetMap      归档格式文件名编码
     * @param unzipLevel              解压层级。-1：无限解压，碰到压缩包就解压；0、1：只解压当前压缩包；&gt;1：解压次数
     * @param unzipFilter             内部压缩包的名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param otherFilter             除压缩包以外的文件名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeAfterActionFilter 压缩包解压缩前后的Action前的过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeUnzipAction       解压之前的操作 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param otherAction             非压缩包的处理逻辑 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param <T>                     Action返回值类型
     * @return 返回任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    public static <T> List<T> zipHandle(InputStream is,
                                        ArchiveFormat[] needUnZipFormats,
                                        Map<ArchiveFormat, Charset> fileNameCharsetMap,
                                        int unzipLevel,
                                        BiPredicate<Integer, String> unzipFilter,
                                        BiPredicate<Integer, String> otherFilter,
                                        BiPredicate<Integer, String> beforeAfterActionFilter,
                                        RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                        RT3<InputStream, Integer, String, T, Exception> otherAction) throws Exception {
        Tuple2<List<ArchiveFormat>, Map<ArchiveFormat, Charset>> formatsAndMap = checkParameters(needUnZipFormats, fileNameCharsetMap);
        return zipHandle(is, formatsAndMap._1, formatsAndMap._2, 1, unzipLevel, true,
                unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction);
    }

    /**
     * zip包的处理逻辑（自动解压）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param needUnZipFormats        需要解压的归档格式（必须是支持的格式 {@link #isSupported(ArchiveFormat)}），不需要解压的格式则按普通文件处理
     * @param fileNameCharsetMap      归档格式文件名编码
     * @param unzipTimes              压缩包的第几层。最开始的压缩包为第一层，压缩包里面的文件是第二层，压缩包里的压缩包再解压，则加一层。以此类推……
     * @param unzipLevel              解压层级。-1：无限解压，碰到压缩包就解压；0、1：只解压当前压缩包；&gt;1：解压次数
     * @param isCloseStream           是否关闭流
     * @param unzipFilter             内部压缩包的名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param otherFilter             除压缩包以外的文件名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeAfterActionFilter 压缩包解压缩前后的Action前的过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeUnzipAction       解压之前的操作 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param otherAction             非压缩包的处理逻辑 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param <T>                     Action返回值类型
     * @return 返回任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    private static <T> List<T> zipHandle(InputStream is,
                                         List<ArchiveFormat> needUnZipFormats,
                                         Map<ArchiveFormat, Charset> fileNameCharsetMap,
                                         int unzipTimes,
                                         int unzipLevel,
                                         boolean isCloseStream,
                                         BiPredicate<Integer, String> unzipFilter,
                                         BiPredicate<Integer, String> otherFilter,
                                         BiPredicate<Integer, String> beforeAfterActionFilter,
                                         RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                         RT3<InputStream, Integer, String, T, Exception> otherAction) throws Exception {
        Charset zipEntryNameCharset = fileNameCharsetMap.get(ZIP);
        ArrayList<T> ts = new ArrayList<>();
        ZipArchiveInputStream zipis = null;
        try {
            zipis = new ZipArchiveInputStream(is, zipEntryNameCharset.name());

            int newUnzipTimes = unzipTimes + 1;
            int newUnzipLevel = unzipLevel < 0 ? unzipLevel : unzipLevel - 1;

            ArchiveEntry entry;
            while ((entry = zipis.getNextEntry()) != null) {
                String entryFileName = entry.getName();
                if (entry.isDirectory()) continue;
                String extension = FilenameUtils.getExtension(entryFileName);

                ArchiveFormat archiveFormat = ArchiveFormat.of(extension);
                if (needUnZipFormats.contains(archiveFormat)) {
                    InputStream entryIs = zipis;
                    if ((beforeAfterActionFilter == null || beforeAfterActionFilter.test(newUnzipTimes, entryFileName)) && beforeUnzipAction != null) {
                        entryIs = IOStreams.transferTo(entryIs);
                        T t = beforeUnzipAction.$(entryIs, newUnzipTimes, entryFileName);
                        ts.add(t);
                        ((MultiByteArrayInputStream) entryIs).reset();      // 重复利用 MultiByteArrayInputStream，后续还要使用
                    }

                    if (!(unzipLevel == 0 || unzipLevel == 1)) {
                        if (unzipFilter == null || unzipFilter.test(newUnzipTimes, entryFileName)) {
                            List<T> tmpTs = archiveFormat == ZIP
                                    ? zipHandle(entryIs, needUnZipFormats, fileNameCharsetMap, newUnzipTimes, newUnzipLevel, false,
                                    unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction)
                                    : gzipHandle(entryIs, needUnZipFormats, fileNameCharsetMap, newUnzipTimes, newUnzipLevel, entryFileName,
                                    false, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction);
                            ts.addAll(tmpTs);
                        }
                    }
                } else {
                    if (otherFilter == null || otherFilter.test(newUnzipTimes, entryFileName)) {
                        ts.add(otherAction.$(zipis, newUnzipTimes, entryFileName));
                    }
                }
            }
        } finally {
            if (isCloseStream) {
                Close.close(zipis);
                Close.close(is);
            }
        }
        return ts;
    }


    /**
     * 解析处理gzip包的内容并重新打包压缩（自动解压缩）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param fileNameCharsetMap      归档格式文件名编码
     * @param unzipLevel              解压层级。-1：无限解压，碰到压缩包就解压；0、1：只解压当前压缩包；&gt;1：解压次数
     * @param gzipFileName            gzip包的包名
     * @param unzipFilter             内部压缩包的名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param otherFilter             除压缩包以外的文件名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeAfterActionFilter 压缩包解压缩前后的Action前的过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeUnzipAction       解压之前的操作 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param afterZipAction          压缩之后的操作 {@code RT3<InputStream, Integer, String, T, Exception>(压缩之后文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param otherAction             非压缩包的处理逻辑 {@code RT4<InputStream, OutputStream, Integer, String, T, Exception>
     *                                (压缩之后文件流, 处理完文件的输出流，压缩包的第几层, 文件名, 返回值)}<b>（处理完后，一定要写入所提供的输出流中 OutputStream）</b>
     * @param <T>                     Action返回值类型
     * @return 返回 压缩后的字节流数组 以及 任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    public static <T> Tuple2<byte[][], List<T>> reGzipHandle(InputStream is,
                                                             Map<ArchiveFormat, Charset> fileNameCharsetMap,
                                                             int unzipLevel,
                                                             String gzipFileName,
                                                             BiPredicate<Integer, String> unzipFilter,
                                                             BiPredicate<Integer, String> otherFilter,
                                                             BiPredicate<Integer, String> beforeAfterActionFilter,
                                                             RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                                             RT3<InputStream, Integer, String, T, Exception> afterZipAction,
                                                             RT4<InputStream, OutputStream, Integer, String, T, Exception> otherAction) throws Exception {
        Tuple2<List<ArchiveFormat>, Map<ArchiveFormat, Charset>> formatsAndMap = checkParameters(null, fileNameCharsetMap);
        return reGzipHandle(is, formatsAndMap._1, formatsAndMap._2, 1, unzipLevel, gzipFileName, true, unzipFilter, otherFilter,
                beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction);
    }

    /**
     * 解析处理gzip包的内容并重新打包压缩（自动解压缩）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param needUnZipFormats        需要解压的归档格式（必须是支持的格式 {@link #isSupported(ArchiveFormat)}），不需要解压的格式则按普通文件处理
     * @param fileNameCharsetMap      归档格式文件名编码
     * @param unzipLevel              解压层级。-1：无限解压，碰到压缩包就解压；0、1：只解压当前压缩包；&gt;1：解压次数
     * @param gzipFileName            gzip包的包名
     * @param unzipFilter             内部压缩包的名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param otherFilter             除压缩包以外的文件名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeAfterActionFilter 压缩包解压缩前后的Action前的过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeUnzipAction       解压之前的操作 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param afterZipAction          压缩之后的操作 {@code RT3<InputStream, Integer, String, T, Exception>(压缩之后文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param otherAction             非压缩包的处理逻辑 {@code RT4<InputStream, OutputStream, Integer, String, T, Exception>
     *                                (压缩之后文件流, 处理完文件的输出流，压缩包的第几层, 文件名, 返回值)}<b>（处理完后，一定要写入所提供的输出流中 OutputStream）</b>
     * @param <T>                     Action返回值类型
     * @return 返回 压缩后的字节流数组 以及 任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    public static <T> Tuple2<byte[][], List<T>> reGzipHandle(InputStream is,
                                                             ArchiveFormat[] needUnZipFormats,
                                                             Map<ArchiveFormat, Charset> fileNameCharsetMap,
                                                             int unzipLevel,
                                                             String gzipFileName,
                                                             BiPredicate<Integer, String> unzipFilter,
                                                             BiPredicate<Integer, String> otherFilter,
                                                             BiPredicate<Integer, String> beforeAfterActionFilter,
                                                             RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                                             RT3<InputStream, Integer, String, T, Exception> afterZipAction,
                                                             RT4<InputStream, OutputStream, Integer, String, T, Exception> otherAction) throws Exception {
        Tuple2<List<ArchiveFormat>, Map<ArchiveFormat, Charset>> formatsAndMap = checkParameters(needUnZipFormats, fileNameCharsetMap);
        return reGzipHandle(is, formatsAndMap._1, formatsAndMap._2, 1, unzipLevel, gzipFileName, true, unzipFilter, otherFilter,
                beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction);
    }

    /**
     * 解析处理gzip包的内容并重新打包压缩（自动解压缩）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param needUnZipFormats        需要解压的归档格式（必须是支持的格式 {@link #isSupported(ArchiveFormat)}），不需要解压的格式则按普通文件处理
     * @param fileNameCharsetMap      归档格式文件名编码
     * @param unzipTimes              压缩包的第几层。最开始的压缩包为第一层，压缩包里面的文件是第二层，压缩包里的压缩包再解压，则加一层。以此类推……
     * @param unzipLevel              解压层级。-1：无限解压，碰到压缩包就解压；0、1：只解压当前压缩包；&gt;1：解压次数
     * @param gzipFileName            gzip包的包名
     * @param isCloseStream           是否关闭流
     * @param unzipFilter             内部压缩包的名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param otherFilter             除压缩包以外的文件名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeAfterActionFilter 压缩包解压缩前后的Action前的过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeUnzipAction       解压之前的操作 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param afterZipAction          压缩之后的操作 {@code RT3<InputStream, Integer, String, T, Exception>(压缩之后文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param otherAction             非压缩包的处理逻辑 {@code RT4<InputStream, OutputStream, Integer, String, T, Exception>
     *                                (压缩之后文件流, 处理完文件的输出流，压缩包的第几层, 文件名, 返回值)}<b>（处理完后，一定要写入所提供的输出流中 OutputStream）</b>
     * @param <T>                     Action返回值类型
     * @return 返回 压缩后的字节流数组 以及 任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    private static <T> Tuple2<byte[][], List<T>> reGzipHandle(InputStream is,
                                                              List<ArchiveFormat> needUnZipFormats,
                                                              Map<ArchiveFormat, Charset> fileNameCharsetMap,
                                                              int unzipTimes,
                                                              int unzipLevel,
                                                              String gzipFileName,
                                                              boolean isCloseStream,
                                                              BiPredicate<Integer, String> unzipFilter,
                                                              BiPredicate<Integer, String> otherFilter,
                                                              BiPredicate<Integer, String> beforeAfterActionFilter,
                                                              RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                                              RT3<InputStream, Integer, String, T, Exception> afterZipAction,
                                                              RT4<InputStream, OutputStream, Integer, String, T, Exception> otherAction) throws Exception {
        Charset gzipNameCharset = fileNameCharsetMap.get(GZIP);
        ArrayList<T> ts = new ArrayList<>();
        GzipCompressorInputStream gcis = null;
        byte[][] gzipBytes;
        try {
            gcis = new GzipCompressorInputStream(is);
            String fileNameInGzip = ZipUtils.fileNameInGzip(gcis, gzipFileName, gzipNameCharset);
            String extension = FilenameUtils.getExtension(fileNameInGzip);
            MultiByteArrayOutputStream baos = new MultiByteArrayOutputStream();

            int newUnzipTimes = unzipTimes + 1;
            int newUnzipLevel = unzipLevel < 0 ? unzipLevel : unzipLevel - 1;

            ArchiveFormat archiveFormat = ArchiveFormat.of(extension);
            if (needUnZipFormats.contains(archiveFormat)) {
                InputStream entryIs = gcis;
                boolean isRunBeforeAfterAction = beforeAfterActionFilter == null || beforeAfterActionFilter.test(newUnzipTimes, fileNameInGzip);
                if (isRunBeforeAfterAction && (beforeUnzipAction != null || afterZipAction != null)) {
                    entryIs = IOStreams.transferTo(entryIs);
                    if (beforeUnzipAction != null) {
                        T t = beforeUnzipAction.$(entryIs, newUnzipTimes, fileNameInGzip);
                        ts.add(t);
                        ((MultiByteArrayInputStream) entryIs).reset();      // 重复利用 MultiByteArrayInputStream，后续还要使用
                    }
                }

                if (!(unzipLevel == 0 || unzipLevel == 1)) {
                    if (unzipFilter == null || unzipFilter.test(newUnzipTimes, fileNameInGzip)) {
                        Tuple2<byte[][], List<T>> listTuple2 = archiveFormat == ZIP
                                ? reZipHandle(entryIs, needUnZipFormats, fileNameCharsetMap, newUnzipTimes, newUnzipLevel, false, unzipFilter,
                                otherFilter, beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction)
                                : reGzipHandle(entryIs, needUnZipFormats, fileNameCharsetMap, newUnzipTimes, newUnzipLevel, fileNameInGzip, false,
                                unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction);
                        ts.addAll(listTuple2._2);
                        entryIs = new MultiByteArrayInputStream(listTuple2._1);
                    }
                }

                gzipBytes = ZipUtils.gzip(entryIs, fileNameInGzip, gzipNameCharset);

                if (isRunBeforeAfterAction && afterZipAction != null) {
                    ((MultiByteArrayInputStream) entryIs).reset();
                    T t = afterZipAction.$(entryIs, newUnzipTimes, fileNameInGzip);
                    ts.add(t);
                }

            } else {
                if (otherFilter == null || otherFilter.test(newUnzipTimes, fileNameInGzip)) {
                    ts.add(otherAction.$(gcis, baos, newUnzipTimes, fileNameInGzip));
                    gzipBytes = ZipUtils.gzip(new MultiByteArrayInputStream(baos.toByteArrays()), fileNameInGzip, gzipNameCharset);
                } else {
                    gzipBytes = ZipUtils.gzip(gcis, fileNameInGzip, gzipNameCharset);
                }
            }

        } finally {
            if (isCloseStream) {
                Close.close(gcis);
                Close.close(is);
            }
        }
        return Tuple.of(gzipBytes, ts);
    }

    /**
     * 解析处理zip包的内容并重新打包压缩（自动解压缩）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param fileNameCharsetMap      归档格式文件名编码
     * @param unzipLevel              解压层级。-1：无限解压，碰到压缩包就解压；0、1：只解压当前压缩包；&gt;1：解压次数
     * @param unzipFilter             内部压缩包的名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param otherFilter             除压缩包以外的文件名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeAfterActionFilter 压缩包解压缩前后的Action前的过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeUnzipAction       解压之前的操作 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param afterZipAction          压缩之后的操作 {@code RT3<InputStream, Integer, String, T, Exception>(压缩之后文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param otherAction             非压缩包的处理逻辑 {@code RT4<InputStream, OutputStream, Integer, String, T, Exception>
     *                                (压缩之后文件流, 处理完文件的输出流，压缩包的第几层, 文件名, 返回值)}<b>（处理完后，一定要写入所提供的输出流中 OutputStream）</b>
     * @param <T>                     Action返回值类型
     * @return 返回 压缩后的字节流数组 以及 任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    public static <T> Tuple2<byte[][], List<T>> reZipHandle(InputStream is,
                                                            Map<ArchiveFormat, Charset> fileNameCharsetMap,
                                                            int unzipLevel,
                                                            BiPredicate<Integer, String> unzipFilter,
                                                            BiPredicate<Integer, String> otherFilter,
                                                            BiPredicate<Integer, String> beforeAfterActionFilter,
                                                            RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                                            RT3<InputStream, Integer, String, T, Exception> afterZipAction,
                                                            RT4<InputStream, OutputStream, Integer, String, T, Exception> otherAction) throws Exception {
        Tuple2<List<ArchiveFormat>, Map<ArchiveFormat, Charset>> formatsAndMap = checkParameters(null, fileNameCharsetMap);
        return reZipHandle(is, formatsAndMap._1, formatsAndMap._2, 1, unzipLevel, true, unzipFilter, otherFilter,
                beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction);
    }

    /**
     * 解析处理zip包的内容并重新打包压缩（自动解压缩）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param needUnZipFormats        需要解压的归档格式（必须是支持的格式 {@link #isSupported(ArchiveFormat)}），不需要解压的格式则按普通文件处理
     * @param fileNameCharsetMap      归档格式文件名编码
     * @param unzipLevel              解压层级。-1：无限解压，碰到压缩包就解压；0、1：只解压当前压缩包；&gt;1：解压次数
     * @param unzipFilter             内部压缩包的名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param otherFilter             除压缩包以外的文件名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeAfterActionFilter 压缩包解压缩前后的Action前的过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeUnzipAction       解压之前的操作 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param afterZipAction          压缩之后的操作 {@code RT3<InputStream, Integer, String, T, Exception>(压缩之后文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param otherAction             非压缩包的处理逻辑 {@code RT4<InputStream, OutputStream, Integer, String, T, Exception>
     *                                (压缩之后文件流, 处理完文件的输出流，压缩包的第几层, 文件名, 返回值)}<b>（处理完后，一定要写入所提供的输出流中 OutputStream）</b>
     * @param <T>                     Action返回值类型
     * @return 返回 压缩后的字节流数组 以及 任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    public static <T> Tuple2<byte[][], List<T>> reZipHandle(InputStream is,
                                                            ArchiveFormat[] needUnZipFormats,
                                                            Map<ArchiveFormat, Charset> fileNameCharsetMap,
                                                            int unzipLevel,
                                                            BiPredicate<Integer, String> unzipFilter,
                                                            BiPredicate<Integer, String> otherFilter,
                                                            BiPredicate<Integer, String> beforeAfterActionFilter,
                                                            RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                                            RT3<InputStream, Integer, String, T, Exception> afterZipAction,
                                                            RT4<InputStream, OutputStream, Integer, String, T, Exception> otherAction) throws Exception {
        Tuple2<List<ArchiveFormat>, Map<ArchiveFormat, Charset>> formatsAndMap = checkParameters(needUnZipFormats, fileNameCharsetMap);
        return reZipHandle(is, formatsAndMap._1, formatsAndMap._2, 1, unzipLevel, true, unzipFilter, otherFilter,
                beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction);
    }

    /**
     * 解析处理zip包的内容并重新打包压缩（自动解压缩）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param needUnZipFormats        需要解压的归档格式（必须是支持的格式 {@link #isSupported(ArchiveFormat)}），不需要解压的格式则按普通文件处理
     * @param fileNameCharsetMap      归档格式文件名编码
     * @param unzipTimes              压缩包的第几层。最开始的压缩包为第一层，压缩包里面的文件是第二层，压缩包里的压缩包再解压，则加一层。以此类推……
     * @param unzipLevel              解压层级。-1：无限解压，碰到压缩包就解压；0、1：只解压当前压缩包；&gt;1：解压次数
     * @param isCloseStream           是否关闭流
     * @param unzipFilter             内部压缩包的名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param otherFilter             除压缩包以外的文件名称过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeAfterActionFilter 压缩包解压缩前后的Action前的过滤 {@code BiPredicate<Integer, String>(压缩包的第几层, 文件名)}
     * @param beforeUnzipAction       解压之前的操作 {@code RT3<InputStream, Integer, String, T, Exception>(解压之前文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param afterZipAction          压缩之后的操作 {@code RT3<InputStream, Integer, String, T, Exception>(压缩之后文件流, 压缩包的第几层, 文件名, 返回值)}
     * @param otherAction             非压缩包的处理逻辑 {@code RT4<InputStream, OutputStream, Integer, String, T, Exception>
     *                                (压缩之后文件流, 处理完文件的输出流，压缩包的第几层, 文件名, 返回值)}<b>（处理完后，一定要写入所提供的输出流中 OutputStream）</b>
     * @param <T>                     Action返回值类型
     * @return 返回 压缩后的字节流数组 以及 任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    private static <T> Tuple2<byte[][], List<T>> reZipHandle(InputStream is,
                                                             List<ArchiveFormat> needUnZipFormats,
                                                             Map<ArchiveFormat, Charset> fileNameCharsetMap,
                                                             int unzipTimes,
                                                             int unzipLevel,
                                                             boolean isCloseStream,
                                                             BiPredicate<Integer, String> unzipFilter,
                                                             BiPredicate<Integer, String> otherFilter,
                                                             BiPredicate<Integer, String> beforeAfterActionFilter,
                                                             RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                                             RT3<InputStream, Integer, String, T, Exception> afterZipAction,
                                                             RT4<InputStream, OutputStream, Integer, String, T, Exception> otherAction) throws Exception {
        Charset zipEntryNameCharset = fileNameCharsetMap.get(ZIP);
        ArrayList<T> ts = new ArrayList<>();
        ZipArchiveInputStream zipis = null;
        MultiByteArrayOutputStream baos = new MultiByteArrayOutputStream();
        ZipArchiveOutputStream zos = null;
        try {
            zipis = new ZipArchiveInputStream(is, zipEntryNameCharset.name());
            zos = new ZipArchiveOutputStream(baos);

            int newUnzipTimes = unzipTimes + 1;
            int newUnzipLevel = unzipLevel < 0 ? unzipLevel : unzipLevel - 1;

            ArchiveEntry entry;
            while ((entry = zipis.getNextEntry()) != null) {
                String entryFileName = entry.getName();
                zos.putArchiveEntry(new ZipArchiveEntry(entryFileName));
                if (entry.isDirectory()) {
                    zos.closeArchiveEntry();
                    continue;
                }
                String extension = FilenameUtils.getExtension(entryFileName);

                MultiByteArrayOutputStream tmpBaos = new MultiByteArrayOutputStream();

                byte[][] byteArrays;

                ArchiveFormat archiveFormat = ArchiveFormat.of(extension);
                if (needUnZipFormats.contains(archiveFormat)) {

                    InputStream entryIs = zipis;

                    boolean isRunBeforeAfterAction = beforeAfterActionFilter == null || beforeAfterActionFilter.test(newUnzipTimes, entryFileName);
                    if (isRunBeforeAfterAction && beforeUnzipAction != null) {
                        entryIs = IOStreams.transferTo(entryIs);
                        T t = beforeUnzipAction.$(entryIs, newUnzipTimes, entryFileName);
                        ts.add(t);
                        ((MultiByteArrayInputStream) entryIs).reset();      // 重复利用 MultiByteArrayInputStream，后续还要使用
                    }

                    if (unzipLevel == 0 || unzipLevel == 1) {
                        IOUtils.copy(entryIs, tmpBaos);
                        byteArrays = tmpBaos.toByteArrays();
                    } else {
                        if (unzipFilter == null || unzipFilter.test(newUnzipTimes, entryFileName)) {
                            Tuple2<byte[][], List<T>> listTuple2 = archiveFormat == ZIP
                                    ? reZipHandle(entryIs, needUnZipFormats, fileNameCharsetMap, newUnzipTimes, newUnzipLevel, false, unzipFilter,
                                    otherFilter, beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction)
                                    : reGzipHandle(entryIs, needUnZipFormats, fileNameCharsetMap, newUnzipTimes, newUnzipLevel, entryFileName,
                                    false, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction);

                            ts.addAll(listTuple2._2);
                            byteArrays = listTuple2._1;
                        } else {
                            IOUtils.copy(entryIs, tmpBaos);
                            byteArrays = tmpBaos.toByteArrays();
                        }
                    }

                    if (isRunBeforeAfterAction && afterZipAction != null) {
                        T t = afterZipAction.$(new MultiByteArrayInputStream(byteArrays), newUnzipTimes, entryFileName);
                        ts.add(t);
                    }
                } else {
                    if (otherFilter == null || otherFilter.test(newUnzipTimes, entryFileName)) {
                        ts.add(otherAction.$(zipis, tmpBaos, newUnzipTimes, entryFileName));
                    } else {
                        IOUtils.copy(zipis, tmpBaos);
                    }
                    byteArrays = tmpBaos.toByteArrays();
                }

                for (byte[] bytes : byteArrays) {
                    zos.write(bytes);
                }
                zos.closeArchiveEntry();
            }
        } finally {
            if (isCloseStream) {
                Close.close(zipis);
                Close.close(is);
            }
            Close.close(zos);
        }
        return Tuple.of(baos.toByteArrays(), ts);
    }

}
