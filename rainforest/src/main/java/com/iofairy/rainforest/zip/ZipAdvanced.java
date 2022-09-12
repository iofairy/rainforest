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

import com.iofairy.falcon.fs.FileName;
import com.iofairy.falcon.io.*;
import com.iofairy.lambda.RT3;
import com.iofairy.lambda.RT4;
import com.iofairy.rainforest.zip.attr.*;
import com.iofairy.tcf.Close;
import com.iofairy.tuple.Tuple;
import com.iofairy.tuple.Tuple2;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.*;

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
     * gzip包的处理逻辑（自动解压）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param zipFileName             压缩包文件名
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
    public static <T> List<T> gzipHandle(InputStream is,
                                         ZipFileName zipFileName,
                                         int unzipLevel,
                                         BiPredicate<Integer, String> unzipFilter,
                                         BiPredicate<Integer, String> otherFilter,
                                         BiPredicate<Integer, String> beforeAfterActionFilter,
                                         RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                         RT3<InputStream, Integer, String, T, Exception> otherAction) throws Exception {
        return gzipHandle(is, null, zipFileName, unzipLevel, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction);
    }

    /**
     * gzip包的处理逻辑（自动解压）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param zaConfig                解压缩配置
     * @param zipFileName             压缩包文件名
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
    public static <T> List<T> gzipHandle(InputStream is,
                                         ZAConfig zaConfig,
                                         ZipFileName zipFileName,
                                         int unzipLevel,
                                         BiPredicate<Integer, String> unzipFilter,
                                         BiPredicate<Integer, String> otherFilter,
                                         BiPredicate<Integer, String> beforeAfterActionFilter,
                                         RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                         RT3<InputStream, Integer, String, T, Exception> otherAction) throws Exception {
        return gzipHandle(is, zaConfig == null ? ZAConfig.DEFAULT_ZACONFIG : zaConfig, zipFileName == null ? ZipFileName.of() : zipFileName.clone(),
                1, unzipLevel, true, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction);
    }

    /**
     * gzip包的处理逻辑（自动解压）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param zaConfig                解压缩配置
     * @param zipFileName             压缩包文件名
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
    private static <T> List<T> gzipHandle(InputStream is,
                                          ZAConfig zaConfig,
                                          ZipFileName zipFileName,
                                          int unzipTimes,
                                          int unzipLevel,
                                          boolean isCloseStream,
                                          BiPredicate<Integer, String> unzipFilter,
                                          BiPredicate<Integer, String> otherFilter,
                                          BiPredicate<Integer, String> beforeAfterActionFilter,
                                          RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                          RT3<InputStream, Integer, String, T, Exception> otherAction) throws Exception {
        GzipInputProperty gzipInputProperty = (GzipInputProperty) zaConfig.getInputProperty(GZIP);
        Charset gzipNameCharset = Charset.forName(gzipInputProperty.getFileNameEncoding());
        ArrayList<T> ts = new ArrayList<>();
        GzipCompressorInputStream gcis = null;
        try {
            gcis = new GzipCompressorInputStream(is);

            int newUnzipTimes = unzipTimes + 1;
            int newUnzipLevel = unzipLevel < 0 ? unzipLevel : unzipLevel - 1;

            String fileNameInGzip = ZipKit.fileNameInGzip(gcis, zipFileName.getGzipName(), gzipNameCharset);
            String extension = FileName.of(fileNameInGzip).ext;

            ArchiveFormat archiveFormat = ArchiveFormat.of(extension);
            if (zaConfig.isSupportedFormat(archiveFormat)) {
                InputStream entryIs = gcis;
                if ((beforeAfterActionFilter == null || beforeAfterActionFilter.test(newUnzipTimes, fileNameInGzip)) && beforeUnzipAction != null) {
                    entryIs = IOs.toMultiBAIS(entryIs);
                    T t = beforeUnzipAction.$(entryIs, newUnzipTimes, fileNameInGzip);
                    ts.add(t);
                    ((MultiByteArrayInputStream) entryIs).reset();      // 重复利用 MultiByteArrayInputStream，后续还要使用
                }

                if (!(unzipLevel == 0 || unzipLevel == 1)) {
                    if (unzipFilter == null || unzipFilter.test(newUnzipTimes, fileNameInGzip)) {
                        List<T> tmpTs = archiveFormat == ZIP
                                ? zipHandle(entryIs, zaConfig, zipFileName, newUnzipTimes, newUnzipLevel,
                                false, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction)
                                : gzipHandle(entryIs, zaConfig, zipFileName.setGzipName(fileNameInGzip), newUnzipTimes, newUnzipLevel,
                                false, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction);
                        ts.addAll(tmpTs);
                    }
                }
            } else {
                if ((otherFilter == null || otherFilter.test(newUnzipTimes, fileNameInGzip)) && otherAction != null) {
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
     * @param zipFileName             压缩包文件名
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
                                        ZipFileName zipFileName,
                                        int unzipLevel,
                                        BiPredicate<Integer, String> unzipFilter,
                                        BiPredicate<Integer, String> otherFilter,
                                        BiPredicate<Integer, String> beforeAfterActionFilter,
                                        RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                        RT3<InputStream, Integer, String, T, Exception> otherAction) throws Exception {
        return zipHandle(is, null, zipFileName, unzipLevel, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction);
    }

    /**
     * zip包的处理逻辑（自动解压）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param zaConfig                解压缩配置
     * @param zipFileName             压缩包文件名
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
                                        ZAConfig zaConfig,
                                        ZipFileName zipFileName,
                                        int unzipLevel,
                                        BiPredicate<Integer, String> unzipFilter,
                                        BiPredicate<Integer, String> otherFilter,
                                        BiPredicate<Integer, String> beforeAfterActionFilter,
                                        RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                        RT3<InputStream, Integer, String, T, Exception> otherAction) throws Exception {
        return zipHandle(is, zaConfig == null ? ZAConfig.DEFAULT_ZACONFIG : zaConfig, zipFileName == null ? ZipFileName.of() : zipFileName.clone(),
                1, unzipLevel, true, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction);
    }

    /**
     * zip包的处理逻辑（自动解压）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param zaConfig                解压缩配置
     * @param zipFileName             压缩包文件名
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
                                         ZAConfig zaConfig,
                                         ZipFileName zipFileName,
                                         int unzipTimes,
                                         int unzipLevel,
                                         boolean isCloseStream,
                                         BiPredicate<Integer, String> unzipFilter,
                                         BiPredicate<Integer, String> otherFilter,
                                         BiPredicate<Integer, String> beforeAfterActionFilter,
                                         RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                         RT3<InputStream, Integer, String, T, Exception> otherAction) throws Exception {
        ZipInputProperty zipInputProperty = (ZipInputProperty) zaConfig.getInputProperty(ZIP);
        ArrayList<T> ts = new ArrayList<>();
        ZipArchiveInputStream zipis = null;
        try {
            zipis = new ZipArchiveInputStream(is, zipInputProperty.getFileNameEncoding());

            int newUnzipTimes = unzipTimes + 1;
            int newUnzipLevel = unzipLevel < 0 ? unzipLevel : unzipLevel - 1;

            ArchiveEntry entry;
            while ((entry = zipis.getNextEntry()) != null) {
                String entryFileName = entry.getName();
                if (entry.isDirectory()) continue;
                String extension = FileName.of(entryFileName).ext;

                ArchiveFormat archiveFormat = ArchiveFormat.of(extension);
                if (zaConfig.isSupportedFormat(archiveFormat)) {
                    InputStream entryIs = zipis;
                    if ((beforeAfterActionFilter == null || beforeAfterActionFilter.test(newUnzipTimes, entryFileName)) && beforeUnzipAction != null) {
                        entryIs = IOs.toMultiBAIS(entryIs);
                        T t = beforeUnzipAction.$(entryIs, newUnzipTimes, entryFileName);
                        ts.add(t);
                        ((MultiByteArrayInputStream) entryIs).reset();      // 重复利用 MultiByteArrayInputStream，后续还要使用
                    }

                    if (!(unzipLevel == 0 || unzipLevel == 1)) {
                        if (unzipFilter == null || unzipFilter.test(newUnzipTimes, entryFileName)) {
                            List<T> tmpTs = archiveFormat == ZIP
                                    ? zipHandle(entryIs, zaConfig, zipFileName, newUnzipTimes, newUnzipLevel, false,
                                    unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction)
                                    : gzipHandle(entryIs, zaConfig, zipFileName.setGzipName(entryFileName), newUnzipTimes, newUnzipLevel,
                                    false, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, otherAction);
                            ts.addAll(tmpTs);
                        }
                    }
                } else {
                    if ((otherFilter == null || otherFilter.test(newUnzipTimes, entryFileName)) && otherAction != null) {
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
     * @param zipFileName             压缩包文件名
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
    public static <T> Tuple2<byte[][], List<T>> reGzipHandle(InputStream is,
                                                             ZipFileName zipFileName,
                                                             int unzipLevel,
                                                             BiPredicate<Integer, String> unzipFilter,
                                                             BiPredicate<Integer, String> otherFilter,
                                                             BiPredicate<Integer, String> beforeAfterActionFilter,
                                                             RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                                             RT3<InputStream, Integer, String, T, Exception> afterZipAction,
                                                             RT4<InputStream, OutputStream, Integer, String, T, Exception> otherAction) throws Exception {
        return reGzipHandle(is, null, zipFileName, unzipLevel, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction);
    }

    /**
     * 解析处理gzip包的内容并重新打包压缩（自动解压缩）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param zaConfig                解压缩配置
     * @param zipFileName             压缩包文件名
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
    public static <T> Tuple2<byte[][], List<T>> reGzipHandle(InputStream is,
                                                             ZAConfig zaConfig,
                                                             ZipFileName zipFileName,
                                                             int unzipLevel,
                                                             BiPredicate<Integer, String> unzipFilter,
                                                             BiPredicate<Integer, String> otherFilter,
                                                             BiPredicate<Integer, String> beforeAfterActionFilter,
                                                             RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                                             RT3<InputStream, Integer, String, T, Exception> afterZipAction,
                                                             RT4<InputStream, OutputStream, Integer, String, T, Exception> otherAction) throws Exception {
        return reGzipHandle(is, zaConfig == null ? ZAConfig.DEFAULT_ZACONFIG : zaConfig, zipFileName == null ? ZipFileName.of() : zipFileName.clone(),
                1, unzipLevel, true, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction);
    }

    /**
     * 解析处理gzip包的内容并重新打包压缩（自动解压缩）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param zaConfig                解压缩配置
     * @param zipFileName             压缩包文件名
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
    private static <T> Tuple2<byte[][], List<T>> reGzipHandle(InputStream is,
                                                              ZAConfig zaConfig,
                                                              ZipFileName zipFileName,
                                                              int unzipTimes,
                                                              int unzipLevel,
                                                              boolean isCloseStream,
                                                              BiPredicate<Integer, String> unzipFilter,
                                                              BiPredicate<Integer, String> otherFilter,
                                                              BiPredicate<Integer, String> beforeAfterActionFilter,
                                                              RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                                              RT3<InputStream, Integer, String, T, Exception> afterZipAction,
                                                              RT4<InputStream, OutputStream, Integer, String, T, Exception> otherAction) throws Exception {
        GzipInputProperty gzipInputProperty = (GzipInputProperty) zaConfig.getInputProperty(GZIP);
        GzipOutputProperty gzipOutputProperty = (GzipOutputProperty) zaConfig.getOutputProperty(GZIP);
        Charset inputGzipNameCharset = Charset.forName(gzipInputProperty.getFileNameEncoding());
        Charset outputGzipNameCharset = Charset.forName(gzipOutputProperty.getFileNameEncoding());

        ArrayList<T> ts = new ArrayList<>();
        GzipCompressorInputStream gcis = null;
        byte[][] gzipBytes;
        try {
            gcis = new GzipCompressorInputStream(is);
            String fileNameInGzip = ZipKit.fileNameInGzip(gcis, zipFileName.getGzipName(), inputGzipNameCharset);
            String extension = FileName.of(fileNameInGzip).ext;
            MultiByteArrayOutputStream baos = new MultiByteArrayOutputStream();

            int newUnzipTimes = unzipTimes + 1;
            int newUnzipLevel = unzipLevel < 0 ? unzipLevel : unzipLevel - 1;

            ArchiveFormat archiveFormat = ArchiveFormat.of(extension);
            if (zaConfig.isSupportedFormat(archiveFormat)) {
                InputStream entryIs = gcis;
                boolean isRunBeforeAfterAction = beforeAfterActionFilter == null || beforeAfterActionFilter.test(newUnzipTimes, fileNameInGzip);
                if (isRunBeforeAfterAction && (beforeUnzipAction != null || afterZipAction != null)) {
                    entryIs = IOs.toMultiBAIS(entryIs);
                    if (beforeUnzipAction != null) {
                        T t = beforeUnzipAction.$(entryIs, newUnzipTimes, fileNameInGzip);
                        ts.add(t);
                        ((MultiByteArrayInputStream) entryIs).reset();      // 重复利用 MultiByteArrayInputStream，后续还要使用
                    }
                }

                if (!(unzipLevel == 0 || unzipLevel == 1)) {
                    if (unzipFilter == null || unzipFilter.test(newUnzipTimes, fileNameInGzip)) {
                        Tuple2<byte[][], List<T>> listTuple2 = archiveFormat == ZIP
                                ? reZipHandle(entryIs, zaConfig, zipFileName, newUnzipTimes, newUnzipLevel, false, unzipFilter,
                                otherFilter, beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction)
                                : reGzipHandle(entryIs, zaConfig, zipFileName.setGzipName(fileNameInGzip), newUnzipTimes, newUnzipLevel, false,
                                unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction);
                        ts.addAll(listTuple2._2);
                        entryIs = new MultiByteArrayInputStream(listTuple2._1);
                    }
                }

                gzipBytes = ZipKit.gzip(entryIs, fileNameInGzip, outputGzipNameCharset);

                if (isRunBeforeAfterAction && afterZipAction != null) {
                    ((MultiByteArrayInputStream) entryIs).reset();
                    T t = afterZipAction.$(entryIs, newUnzipTimes, fileNameInGzip);
                    ts.add(t);
                }

            } else {
                if ((otherFilter == null || otherFilter.test(newUnzipTimes, fileNameInGzip)) && otherAction != null) {
                    ts.add(otherAction.$(gcis, baos, newUnzipTimes, fileNameInGzip));
                    gzipBytes = ZipKit.gzip(new MultiByteArrayInputStream(baos.toByteArrays()), fileNameInGzip, outputGzipNameCharset);
                } else {
                    gzipBytes = ZipKit.gzip(gcis, fileNameInGzip, outputGzipNameCharset);
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
     * @param zipFileName             压缩包文件名
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
                                                            ZipFileName zipFileName,
                                                            int unzipLevel,
                                                            BiPredicate<Integer, String> unzipFilter,
                                                            BiPredicate<Integer, String> otherFilter,
                                                            BiPredicate<Integer, String> beforeAfterActionFilter,
                                                            RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                                            RT3<InputStream, Integer, String, T, Exception> afterZipAction,
                                                            RT4<InputStream, OutputStream, Integer, String, T, Exception> otherAction) throws Exception {
        return reZipHandle(is, null, zipFileName, unzipLevel, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction);
    }

    /**
     * 解析处理zip包的内容并重新打包压缩（自动解压缩）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param zaConfig                解压缩配置
     * @param zipFileName             压缩包文件名
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
                                                            ZAConfig zaConfig,
                                                            ZipFileName zipFileName,
                                                            int unzipLevel,
                                                            BiPredicate<Integer, String> unzipFilter,
                                                            BiPredicate<Integer, String> otherFilter,
                                                            BiPredicate<Integer, String> beforeAfterActionFilter,
                                                            RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                                            RT3<InputStream, Integer, String, T, Exception> afterZipAction,
                                                            RT4<InputStream, OutputStream, Integer, String, T, Exception> otherAction) throws Exception {
        return reZipHandle(is, zaConfig == null ? ZAConfig.DEFAULT_ZACONFIG : zaConfig, zipFileName == null ? ZipFileName.of() : zipFileName.clone(),
                1, unzipLevel, true, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction);
    }

    /**
     * 解析处理zip包的内容并重新打包压缩（自动解压缩）<br>
     * <b>注：内部会自动关闭 InputStream 输入流</b>
     *
     * @param is                      输入流
     * @param zaConfig                解压缩配置
     * @param zipFileName             压缩包文件名
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
                                                             ZAConfig zaConfig,
                                                             ZipFileName zipFileName,
                                                             int unzipTimes,
                                                             int unzipLevel,
                                                             boolean isCloseStream,
                                                             BiPredicate<Integer, String> unzipFilter,
                                                             BiPredicate<Integer, String> otherFilter,
                                                             BiPredicate<Integer, String> beforeAfterActionFilter,
                                                             RT3<InputStream, Integer, String, T, Exception> beforeUnzipAction,
                                                             RT3<InputStream, Integer, String, T, Exception> afterZipAction,
                                                             RT4<InputStream, OutputStream, Integer, String, T, Exception> otherAction) throws Exception {
        ZipInputProperty zipInputProperty = (ZipInputProperty) zaConfig.getInputProperty(ZIP);
        ZipOutputProperty zipOutputProperty = (ZipOutputProperty) zaConfig.getOutputProperty(ZIP);

        ArrayList<T> ts = new ArrayList<>();
        ZipArchiveInputStream zipis = null;
        MultiByteArrayOutputStream baos = new MultiByteArrayOutputStream();
        ZipArchiveOutputStream zos = null;
        try {
            zipis = new ZipArchiveInputStream(is, zipInputProperty.getFileNameEncoding());
            zos = new ZipArchiveOutputStream(baos);
            zos.setLevel(zipOutputProperty.getLevel());
            zos.setMethod(zipOutputProperty.getMethod());
            zos.setUseZip64(zipOutputProperty.getZip64Mode());
            zos.setEncoding(zipOutputProperty.getFileNameEncoding());

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

                try {
                    String extension = FileName.of(entryFileName).ext;

                    MultiByteArrayOutputStream tmpBaos = new MultiByteArrayOutputStream();

                    byte[][] byteArrays;

                    ArchiveFormat archiveFormat = ArchiveFormat.of(extension);
                    if (zaConfig.isSupportedFormat(archiveFormat)) {

                        InputStream entryIs = zipis;

                        boolean isRunBeforeAfterAction = beforeAfterActionFilter == null || beforeAfterActionFilter.test(newUnzipTimes, entryFileName);
                        if (isRunBeforeAfterAction && beforeUnzipAction != null) {
                            entryIs = IOs.toMultiBAIS(entryIs);
                            T t = beforeUnzipAction.$(entryIs, newUnzipTimes, entryFileName);
                            ts.add(t);
                            ((MultiByteArrayInputStream) entryIs).reset();      // 重复利用 MultiByteArrayInputStream，后续还要使用
                        }

                        if (unzipLevel == 0 || unzipLevel == 1) {
                            IOs.copy(entryIs, tmpBaos);
                            byteArrays = tmpBaos.toByteArrays();
                        } else {
                            if (unzipFilter == null || unzipFilter.test(newUnzipTimes, entryFileName)) {
                                Tuple2<byte[][], List<T>> listTuple2 = archiveFormat == ZIP
                                        ? reZipHandle(entryIs, zaConfig, zipFileName, newUnzipTimes, newUnzipLevel, false, unzipFilter,
                                        otherFilter, beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction)
                                        : reGzipHandle(entryIs, zaConfig, zipFileName.setGzipName(entryFileName), newUnzipTimes, newUnzipLevel,
                                        false, unzipFilter, otherFilter, beforeAfterActionFilter, beforeUnzipAction, afterZipAction, otherAction);

                                ts.addAll(listTuple2._2);
                                byteArrays = listTuple2._1;
                            } else {
                                IOs.copy(entryIs, tmpBaos);
                                byteArrays = tmpBaos.toByteArrays();
                            }
                        }

                        if (isRunBeforeAfterAction && afterZipAction != null) {
                            T t = afterZipAction.$(new MultiByteArrayInputStream(byteArrays), newUnzipTimes, entryFileName);
                            ts.add(t);
                        }
                    } else {
                        if ((otherFilter == null || otherFilter.test(newUnzipTimes, entryFileName)) && otherAction != null) {
                            ts.add(otherAction.$(zipis, tmpBaos, newUnzipTimes, entryFileName));
                        } else {
                            IOs.copy(zipis, tmpBaos);
                        }
                        byteArrays = tmpBaos.toByteArrays();
                    }

                    for (byte[] bytes : byteArrays) {
                        zos.write(bytes);
                    }

                } finally {
                    zos.closeArchiveEntry();
                }
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
