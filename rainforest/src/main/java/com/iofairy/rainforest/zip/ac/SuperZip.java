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

import com.iofairy.falcon.fs.*;
import com.iofairy.falcon.io.*;
import com.iofairy.falcon.zip.ArchiveFormat;
import com.iofairy.lambda.*;
import com.iofairy.rainforest.zip.attr.ZipInputProperty;
import com.iofairy.rainforest.zip.attr.ZipOutputProperty;
import com.iofairy.rainforest.zip.base.*;
import com.iofairy.tcf.Close;
import com.iofairy.tuple.Tuple2;
import lombok.*;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.*;
import java.util.*;

/**
 * 超级ZIP解压缩与重压缩
 *
 * @since 0.2.0
 */
@Getter
@ToString(exclude = {"unzipACMap", "reZipACMap"})
public class SuperZip implements SuperAC {
    private Map<ArchiveFormat, SuperAC> unzipACMap;
    private Map<ArchiveFormat, SuperAC> reZipACMap;
    private ZipInputProperty unzipInputProperty = ZipInputProperty.of();
    private ZipInputProperty reZipInputProperty = ZipInputProperty.of();
    private ZipOutputProperty reZipOutputProperty = ZipOutputProperty.of();

    public SuperZip() {
    }

    public SuperZip(ZipInputProperty unzipInputProperty, ZipInputProperty reZipInputProperty, ZipOutputProperty reZipOutputProperty) {
        this.unzipInputProperty = unzipInputProperty == null ? ZipInputProperty.of() : unzipInputProperty;
        this.reZipInputProperty = reZipInputProperty == null ? ZipInputProperty.of() : reZipInputProperty;
        this.reZipOutputProperty = reZipOutputProperty == null ? ZipOutputProperty.of() : reZipOutputProperty;
    }

    public static SuperZip of() {
        return new SuperZip();
    }

    public static SuperZip of(ZipInputProperty unzipInputProperty, ZipInputProperty reZipInputProperty, ZipOutputProperty reZipOutputProperty) {
        return new SuperZip(unzipInputProperty, reZipInputProperty, reZipOutputProperty);
    }

    public SuperZip setUnzipInputProperty(ZipInputProperty unzipInputProperty) {
        this.unzipInputProperty = unzipInputProperty == null ? ZipInputProperty.of() : unzipInputProperty;
        return this;
    }

    public SuperZip setReZipInputProperty(ZipInputProperty reZipInputProperty) {
        this.reZipInputProperty = reZipInputProperty == null ? ZipInputProperty.of() : reZipInputProperty;
        return this;
    }

    public SuperZip setReZipOutputProperty(ZipOutputProperty reZipOutputProperty) {
        this.reZipOutputProperty = reZipOutputProperty == null ? ZipOutputProperty.of() : reZipOutputProperty;
        return this;
    }

    @Override
    public ArchiveFormat format() {
        return ArchiveFormat.ZIP;
    }

    /**
     * 压缩包解压并处理文件（自动解压）<br>
     * <br>
     * <b>注：</b><br>
     * <ul>
     * <li><b>方法内部会自动关闭 InputStream 输入流，因为内部会有包装此 InputStream 的其他流需要关闭</b>
     * <li><b>方法内部提供或产生的流都不需要外部调用者关闭，否则可能报错或产生预期之外的结果。只有调用者自己创建的流才需要关闭</b>
     * <li><b>外部调用者不建议调用此实例方法，你应该调用静态方法： {@link SuperAC#unzip(InputStream, ArchiveFormat, String, int, PT3, PT3, PT3, RT4, RT4, ZipLogLevel, List)}</b>
     * <li><b>{@code isCloseStream} 参数在外部调用时，一定要设置为 {@code true}，方法内部有很多流需要关闭</b>
     * </ul>
     *
     * @param is                输入流
     * @param zipFileName       压缩包文件名
     * @param unzipTimes        压缩包的第几层。最开始的压缩包解压后，里面的文件为第一层，压缩包里的压缩包再解压，则加一层。以此类推……
     * @param unzipLevel        解压层级。-1：无限解压，碰到压缩包就解压；0：只解压<b>当前压缩包</b>，不解压内部压缩包；&gt;=1：对内部压缩包的解压次数
     * @param isCloseStream     是否关闭流（第一次调用此方法，一定要设置为{@code true}，因为内部会有包装此 InputStream 的其他流需要关闭）
     * @param unzipFilter       内部压缩包的是否解压的过滤器，为{@code null}则<b>都解压</b>， {@code PT3<Integer, String, String, Exception>(压缩包的第几层, 父压缩包的文件名，当前内部文件的名称)}
     * @param otherFilter       除压缩包以外的文件是否处理的过滤器，为{@code null}则<b>都处理</b>， {@code PT3<Integer, String, String, Exception>(压缩包的第几层, 父压缩包的文件名，当前内部文件的名称)}
     * @param beforeUnzipFilter 压缩包解压缩前的Action前的过滤器，为{@code null}则<b>都不处理</b>， {@code PT3<Integer, String, String, Exception>(压缩包的第几层, 父压缩包的文件名，当前内部文件的名称)}
     * @param beforeUnzipAction 解压之前的操作 {@code RT4<InputStream, Integer, String, String, R, Exception>(解压之前文件流, 压缩包的第几层, 父压缩包的文件名，当前内部文件的名称, 返回值)}
     * @param otherAction       非压缩包的处理逻辑 {@code RT4<InputStream, Integer, String, String, R, Exception>(解压之前文件流, 压缩包的第几层, 父压缩包的文件名，当前内部文件的名称, 返回值)}
     * @param zipLogLevel       解压缩日志等级
     * @param superACs          支持哪些类型的压缩/解压处理器（理论上应该传入不可变的Map{@link Collections#unmodifiableMap(Map)}，避免被外部修改）
     * @return 返回任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    @Override
    public <R> List<R> unzip(final InputStream is,
                             String zipFileName,
                             final int unzipTimes,
                             final int unzipLevel,
                             final boolean isCloseStream,
                             PT3<? super Integer, ? super String, ? super String, Exception> unzipFilter,
                             PT3<? super Integer, ? super String, ? super String, Exception> otherFilter,
                             PT3<? super Integer, ? super String, ? super String, Exception> beforeUnzipFilter,
                             RT4<InputStream, ? super Integer, ? super String, ? super String, ? extends R, Exception> beforeUnzipAction,
                             RT4<InputStream, ? super Integer, ? super String, ? super String, ? extends R, Exception> otherAction,
                             ZipLogLevel zipLogLevel,
                             Map<ArchiveFormat, SuperAC> superACs) throws Exception {

        if (zipFileName == null) zipFileName = "";

        // >>> 打印日志参数
        String unzipId = SuperACs.getUnzipId(5);
        String logSource = getClass().getSimpleName() + ".unzip()";
        // <<< 打印日志参数

        ArrayList<R> rs = new ArrayList<>();
        ZipArchiveInputStream zipis = null;
        try {
            if (unzipACMap == null) unzipACMap = SuperACs.toSuperACMap(superACs);

            zipis = new ZipArchiveInputStream(is, unzipInputProperty.getFileNameEncoding());

            int newUnzipTimes = unzipTimes + 1;
            int newUnzipLevel = unzipLevel <= 0 ? unzipLevel : unzipLevel - 1;

            ArchiveEntry entry;
            while ((entry = zipis.getNextEntry()) != null) {
                String entryFileName = entry.getName();
                if (entry.isDirectory()) continue;
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
                    InputStream entryIs = zipis;
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
                        rs.add(otherAction.$(zipis, unzipTimes, zipFileName, entryFileName));
                        // 打印日志信息
                        LogPrinter.printAfterOther(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, startTime);

                    }
                }

            }
        } finally {
            if (isCloseStream) {
                Close.close(zipis);
                Close.close(is);
            }
        }
        return rs;
    }

    /**
     * 解压处理压缩包中的文件并重新打包压缩（自动解压缩）<br>
     * <br>
     * <b>注：</b><br>
     * <ul>
     * <li><b>方法内部会自动关闭 InputStream 输入流，因为内部会有包装此 InputStream 的其他流需要关闭</b>
     * <li><b>方法内部提供或产生的流都不需要外部调用者关闭，否则可能报错或产生预期之外的结果。只有调用者自己创建的流才需要关闭</b>
     * <li><b>外部调用者不建议调用此实例方法，你应该调用静态方法： {@link SuperAC#reZip(InputStream, ArchiveFormat, String, int, PT2, PT3, PT3, PT3, PT3, PT3, RT2, RT2, RT4, RT4, RT4, RT5, ZipLogLevel, List)} </b>
     * <li><b>{@code isCloseStream} 参数在外部调用时，一定要设置为 {@code true}，方法内部有很多流需要关闭</b>
     * </ul>
     *
     * @param is                输入流
     * @param zipFileName       压缩包文件名
     * @param unzipTimes        压缩包的第几层。最开始的压缩包解压后，里面的文件为第一层，压缩包里的压缩包再解压，则加一层。以此类推……
     * @param unzipLevel        解压层级。-1：无限解压，碰到压缩包就解压；0：只解压<b>当前压缩包</b>，不解压内部压缩包；&gt;=1：对内部压缩包的解压次数
     * @param isCloseStream     是否关闭流（第一次调用此方法，一定要设置为{@code true}，因为内部会有包装此 InputStream 的其他流需要关闭）
     * @param addFileFilter     是否添加文件，为{@code null}则<b>不添加文件</b>， {@code PT2<Integer, String, Exception>(压缩包的第几层, 父压缩包的文件名)}
     * @param deleteFileFilter  是否删除该文件，为{@code null}则<b>都不删除</b>， {@code PT3<Integer, String, String, Exception>(压缩包的第几层, 父压缩包的文件名，当前内部文件的名称)}
     * @param unzipFilter       内部压缩包的是否解压的过滤器，为{@code null}则<b>都解压</b>， {@code PT3<Integer, String, String, Exception>(压缩包的第几层, 父压缩包的文件名，当前内部文件的名称)}
     * @param otherFilter       除压缩包以外的文件是否处理的过滤器，为{@code null}则<b>都处理</b>， {@code PT3<Integer, String, String, Exception>(压缩包的第几层, 父压缩包的文件名，当前内部文件的名称)}
     * @param beforeUnzipFilter 压缩包解压缩前的Action的过滤器，为{@code null}则<b>都不处理</b>， {@code PT3<Integer, String, String, Exception>(压缩包的第几层, 父压缩包的文件名，当前内部文件的名称)}
     * @param afterZipFilter    压缩包重压缩后的Action的过滤器，为{@code null}则<b>都不处理</b>， {@code PT3<Integer, String, String, Exception>(压缩包的第几层, 父压缩包的文件名，当前内部文件的名称)}
     * @param addFilesAction    添加指定的文件到压缩包 {@code RT2<Integer, String, Tuple2<List<AddFile>, List<R>>, Exception>(压缩包的第几层, 父压缩包的文件名，返回文件列表与返回值列表)}
     * @param addBytesAction    添加指定的字节数组到压缩包 {@code RT2<Integer, String, Tuple2<List<AddBytes>, List<R>>, Exception>(压缩包的第几层, 父压缩包的文件名，返回字节数组与返回值列表)}
     * @param deleteFileAction  对删除的文件的操作（如：备份到其他地方） {@code RT4<InputStream, Integer, String, String, R, Exception>(解压之前文件流, 压缩包的第几层, 父压缩包的文件名，当前内部文件的名称, 返回值)}
     * @param beforeUnzipAction 解压之前的操作 {@code RT4<InputStream, Integer, String, String, R, Exception>(解压之前文件流, 压缩包的第几层, 父压缩包的文件名，当前内部文件的名称, 返回值)}
     * @param afterZipAction    压缩之后的操作 {@code RT4<InputStream, Integer, String, String, R, Exception>(解压之前文件流, 压缩包的第几层, 父压缩包的文件名，当前内部文件的名称, 返回值)}
     * @param otherAction       非压缩包的处理逻辑 {@code RT5<InputStream, OutputStream, Integer, String, String, R, Exception>
     *                          (压缩之后文件流, 处理完文件的输出流，压缩包的第几层, 父压缩包的文件名，当前内部文件的名称, 返回值)}<b>（处理完后，一定要写入所提供的输出流中 OutputStream）</b>
     * @param zipLogLevel       解压缩日志等级
     * @param superACs          支持哪些类型的压缩/解压处理器（理论上应该传入不可变的Map{@link Collections#unmodifiableMap(Map)}，避免被外部修改）
     * @return 返回 压缩后的字节流数组 以及 任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    @Override
    public <R> ZipResult<R> reZip(final InputStream is,
                                  String zipFileName,
                                  final int unzipTimes,
                                  final int unzipLevel,
                                  final boolean isCloseStream,
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
                                  Map<ArchiveFormat, SuperAC> superACs) throws Exception {

        if (zipFileName == null) zipFileName = "";

        // >>> 打印日志参数
        String unzipId = SuperACs.getUnzipId(5);
        String logSource = getClass().getSimpleName() + ".reZip()";
        // <<< 打印日志参数

        ArrayList<R> rs = new ArrayList<>();
        ZipArchiveInputStream zipis = null;
        MultiByteArrayOutputStream baos = new MultiByteArrayOutputStream();
        ZipArchiveOutputStream zos = null;
        try {
            if (reZipACMap == null) reZipACMap = SuperACs.toSuperACMap(superACs);

            zipis = new ZipArchiveInputStream(is, reZipInputProperty.getFileNameEncoding());
            zos = new ZipArchiveOutputStream(baos);
            zos.setLevel(reZipOutputProperty.getLevel());
            zos.setMethod(reZipOutputProperty.getMethod());
            zos.setUseZip64(reZipOutputProperty.getZip64Mode());
            zos.setEncoding(reZipOutputProperty.getFileNameEncoding());

            int newUnzipTimes = unzipTimes + 1;
            int newUnzipLevel = unzipLevel <= 0 ? unzipLevel : unzipLevel - 1;

            ArchiveEntry entry;
            while ((entry = zipis.getNextEntry()) != null) {
                String entryFileName = entry.getName();

                /*
                 * 删除文件
                 */
                if (deleteFileFilter != null && deleteFileFilter.$(unzipTimes, zipFileName, entryFileName)) {
                    // 打印日志信息
                    LogPrinter.printDeleteLogs(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);

                    if (!entry.isDirectory() && deleteFileAction != null) {
                        // 打印日志信息
                        LogPrinter.printDeleteActionLogs(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);

                        R r = deleteFileAction.$(zipis, unzipTimes, zipFileName, entryFileName);
                        rs.add(r);
                    }
                    continue;
                }

                zos.putArchiveEntry(new ZipArchiveEntry(entryFileName));
                if (entry.isDirectory()) {
                    zos.closeArchiveEntry();
                    continue;
                }

                try {
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
                        InputStream entryIs = zipis;

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
                            R r = otherAction.$(zipis, entryBaos, unzipTimes, zipFileName, entryFileName);
                            rs.add(r);

                            // 打印日志信息
                            LogPrinter.printAfterOther(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, startTime);

                        } else {
                            // 打印日志信息
                            LogPrinter.printFilterLogs(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);

                            IOs.copy(zipis, entryBaos);
                        }
                        byteArrays = entryBaos.toByteArrays();
                    }

                    for (byte[] bytes : byteArrays) {
                        zos.write(bytes);
                    }

                } finally {
                    zos.closeArchiveEntry();
                }
            }

            /*
             * 添加文件。
             * 注：如果要添加文件夹，最后一定要带上"/"，否则一律当作文件
             */
            if (addFileFilter != null && addFileFilter.$(unzipTimes, zipFileName)) {
                String errMsg = "错误发生在[" + zipFileName + "]压缩包，unzipTimes为：[" + unzipTimes + "]。";
                List<R> returnList = null;
                if (addFilesAction != null) {
                    Tuple2<List<AddFile>, List<R>> tuple = addFilesAction.$(unzipTimes, zipFileName);
                    List<AddFile> addFiles = tuple._1;
                    returnList = tuple._2;
                    if (addFiles != null) {
                        for (AddFile addFile : addFiles) {
                            if (addFile != null) {
                                String entryFileName = addFile.getEntryFileName();
                                Objects.requireNonNull(entryFileName, "AddFile实例对象中的成员变量`entryFileName`不能为null！" + errMsg);

                                try {
                                    zos.putArchiveEntry(new ZipArchiveEntry(entryFileName));

                                    // 打印日志信息
                                    LogPrinter.printAppendLogs(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);

                                    if (!addFile.isDirectory()) {
                                        File file = addFile.getFile();
                                        Objects.requireNonNull(file, "AddFile实例对象中的成员变量`isDirectory`为false时，`file`不能为null！" + errMsg);
                                        if (!file.exists()) throw new FileNotFoundException("文件[" + file.getAbsolutePath() + "]不存在。" + errMsg);

                                        if (file.isFile()) {
                                            // 自动关闭文件输入流
                                            try (FileInputStream fis = new FileInputStream(file)) {
                                                IOs.copy(fis, zos);
                                            }
                                        }
                                    }

                                } finally {
                                    zos.closeArchiveEntry();
                                }
                            }
                        }
                    }

                    if (returnList != null) rs.addAll(returnList);
                }

                if (addBytesAction != null) {
                    Tuple2<List<AddBytes>, List<R>> tuple = addBytesAction.$(unzipTimes, zipFileName);
                    List<AddBytes> addBytesArray = tuple._1;
                    returnList = tuple._2;
                    if (addBytesArray != null) {
                        for (AddBytes addBytes : addBytesArray) {
                            if (addBytes != null) {
                                String entryFileName = addBytes.getEntryFileName();
                                Objects.requireNonNull(entryFileName, "AddBytes实例对象中的成员变量`entryFileName`不能为null！" + errMsg);

                                try {
                                    zos.putArchiveEntry(new ZipArchiveEntry(entryFileName));

                                    // 打印日志信息
                                    LogPrinter.printAppendLogs(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);

                                    if (!addBytes.isDirectory()) {
                                        byte[][] bytesArray = addBytes.getBytes();
                                        Objects.requireNonNull(bytesArray, "AddBytes实例对象中的成员变量`isDirectory`为false时，`bytes`不能为null！" + errMsg);

                                        for (byte[] bytes : bytesArray) {
                                            zos.write(bytes);
                                        }
                                    }

                                } finally {
                                    zos.closeArchiveEntry();
                                }
                            }
                        }
                    }

                    if (returnList != null) rs.addAll(returnList);
                }
            }

        } finally {
            if (isCloseStream) {
                Close.close(zipis);
                Close.close(is);
            }
            Close.close(zos);
        }
        return ZipResult.of(baos.toByteArrays(), rs);
    }


}
