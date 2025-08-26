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

import com.iofairy.falcon.io.*;
import com.iofairy.time.Stopwatch;
import com.iofairy.falcon.zip.ArchiveFormat;
import com.iofairy.lambda.*;
import com.iofairy.rainforest.zip.attr.Bzip2InputProperty;
import com.iofairy.rainforest.zip.attr.Bzip2OutputProperty;
import com.iofairy.rainforest.zip.base.*;
import com.iofairy.rainforest.zip.utils.ZipKit;
import com.iofairy.tcf.Close;
import com.iofairy.tuple.Tuple2;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * 超级BZIP2解压缩与重压缩
 *
 * @since 0.2.0
 */
@Getter
@ToString
@NoArgsConstructor
public class SuperBzip2 extends SuperACs {
    private Bzip2InputProperty unzipInputProperty = Bzip2InputProperty.of();
    private Bzip2InputProperty reZipInputProperty = Bzip2InputProperty.of();
    private Bzip2OutputProperty reZipOutputProperty = Bzip2OutputProperty.of();

    public SuperBzip2(Bzip2InputProperty unzipInputProperty, Bzip2InputProperty reZipInputProperty, Bzip2OutputProperty reZipOutputProperty) {
        this.unzipInputProperty = unzipInputProperty == null ? Bzip2InputProperty.of() : unzipInputProperty;
        this.reZipInputProperty = reZipInputProperty == null ? Bzip2InputProperty.of() : reZipInputProperty;
        this.reZipOutputProperty = reZipOutputProperty == null ? Bzip2OutputProperty.of() : reZipOutputProperty;
    }

    public static SuperBzip2 of() {
        return new SuperBzip2();
    }

    public static SuperBzip2 of(Bzip2InputProperty unzipInputProperty, Bzip2InputProperty reZipInputProperty, Bzip2OutputProperty reZipOutputProperty) {
        return new SuperBzip2(unzipInputProperty, reZipInputProperty, reZipOutputProperty);
    }

    public SuperBzip2 setUnzipInputProperty(Bzip2InputProperty unzipInputProperty) {
        this.unzipInputProperty = unzipInputProperty == null ? Bzip2InputProperty.of() : unzipInputProperty;
        return this;
    }

    public SuperBzip2 setReZipInputProperty(Bzip2InputProperty reZipInputProperty) {
        this.reZipInputProperty = reZipInputProperty == null ? Bzip2InputProperty.of() : reZipInputProperty;
        return this;
    }

    public SuperBzip2 setReZipOutputProperty(Bzip2OutputProperty reZipOutputProperty) {
        this.reZipOutputProperty = reZipOutputProperty == null ? Bzip2OutputProperty.of() : reZipOutputProperty;
        return this;
    }

    @Override
    public ArchiveFormat format() {
        return ArchiveFormat.BZIP2;
    }

    /**
     * 压缩包解压并处理文件（自动解压）<br>
     * <br>
     * <b>注：</b><br>
     * <ul>
     * <li><b>方法内部会自动关闭 InputStream 输入流，因为内部会有包装此 InputStream 的其他流需要关闭</b>
     * <li><b>方法内部提供或产生的流都不需要外部调用者关闭，否则可能报错或产生预期之外的结果。只有调用者自己创建的流才需要关闭</b>
     * <li><b>外部调用者不建议调用此实例方法，你应该调用静态方法： {@link SuperAC#unzip(InputStream, ArchiveFormat, String, int, PT3, PT3, PT3, RT4, RT4, ZipLogLevel, List)}</b>
     * </ul>
     *
     * @param is                输入流
     * @param parentZipName     父级压缩包文件名
     * @param zipFileName       压缩包文件名
     * @param unzipTimes        压缩包的第几层。最开始的压缩包解压后，里面的文件为第一层，压缩包里的压缩包再解压，则加一层。以此类推……
     * @param unzipLevel        解压层级。-1：无限解压，碰到压缩包就解压；0：只解压<b>当前压缩包</b>，不解压内部压缩包；&gt;=1：对内部压缩包的解压次数
     * @param unzipFilter       内部压缩包的是否解压的过滤器，为{@code null}则<b>都解压</b>， {@code PT3<Integer, String, String, Exception>(压缩包的第几层, 父压缩包的文件名，当前内部文件的名称)}
     * @param otherFilter       除压缩包以外的文件是否处理的过滤器，为{@code null}则<b>都处理</b>， {@code PT3<Integer, String, String, Exception>(压缩包的第几层, 父压缩包的文件名，当前内部文件的名称)}
     * @param beforeUnzipFilter 压缩包解压缩前的Action前的过滤器，为{@code null}则<b>都不处理</b>， {@code PT3<Integer, String, String, Exception>(压缩包的第几层, 父压缩包的文件名，当前内部文件的名称)}
     * @param beforeUnzipAction 解压之前的操作 {@code RT4<InputStream, Integer, String, String, R, Exception>(解压之前文件流, 压缩包的第几层, 父压缩包的文件名，当前内部文件的名称, 返回值)}
     * @param otherAction       非压缩包的处理逻辑 {@code RT4<InputStream, Integer, String, String, R, Exception>(解压之前文件流, 压缩包的第几层, 父压缩包的文件名，当前内部文件的名称, 返回值)}
     * @param zipLogLevel       解压缩日志等级
     * @param superACs          支持哪些类型的压缩/解压处理器（理论上应该传入不可变的Map{@link Collections#unmodifiableMap(Map)}，避免被外部修改）
     * @param unzipId           解压ID，用于日志记录
     * @return 返回任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    @Override
    public <R> List<R> unzip(final InputStream is,
                             String parentZipName,
                             String zipFileName,
                             final int unzipTimes,
                             final int unzipLevel,
                             PT3<? super Integer, ? super String, ? super String, Exception> unzipFilter,
                             PT3<? super Integer, ? super String, ? super String, Exception> otherFilter,
                             PT3<? super Integer, ? super String, ? super String, Exception> beforeUnzipFilter,
                             RT4<InputStream, ? super Integer, ? super String, ? super String, ? extends R, Exception> beforeUnzipAction,
                             RT4<InputStream, ? super Integer, ? super String, ? super String, ? extends R, Exception> otherAction,
                             ZipLogLevel zipLogLevel,
                             Map<ArchiveFormat, SuperAC> superACs,
                             String unzipId
    ) throws Exception {
        if (zipFileName == null) zipFileName = "";

        // >>> 打印日志参数
        unzipId = getUnzipId(5, unzipId);
        final String logSource = getClass().getSimpleName() + ".unzip()";
        // <<< 打印日志参数

        final ArrayList<R> rs = new ArrayList<>();
        BZip2CompressorInputStream zipis = null;
        try {
            if (unzipACMap == null) unzipACMap = toSuperACMap(superACs);

            String entryFileName = ZipKit.getUncompressedName(zipFileName, format());

            zipis = new BZip2CompressorInputStream(is, unzipInputProperty.isDecompressConcatenated());

            final int newUnzipTimes = unzipTimes + 1;
            final int newUnzipLevel = unzipLevel <= 0 ? unzipLevel : unzipLevel - 1;

            unzip(zipis, rs, zipFileName, entryFileName, unzipTimes, unzipLevel, newUnzipTimes, newUnzipLevel, unzipACMap,
                    unzipFilter, otherFilter, beforeUnzipFilter, beforeUnzipAction, otherAction, zipLogLevel, unzipId, logSource);

        } finally {
            Close.close(zipis);
            Close.close(is);
        }
        return rs;
    }


    /**
     * 压缩包解压并处理文件<b>（快速自动解压，更节约内存）</b><br>
     * <br>
     * <b>注：</b><br>
     * <ul>
     * <li><b>方法内部会自动关闭 InputStream 输入流，因为内部会有包装此 InputStream 的其他流需要关闭</b>
     * <li><b>方法内部提供或产生的流都不需要外部调用者关闭，否则可能报错或产生预期之外的结果。只有调用者自己创建的流才需要关闭</b>
     * <li><b>外部调用者【禁止】调用此实例方法，你应该调用静态方法： {@link SuperAC#unzipFast(InputStream, ArchiveFormat, String, int, PT3, PT3, RT5, ZipLogLevel, List)}</b>
     * </ul>
     *
     * @param is            输入流
     * @param parentZipName 父级压缩包文件名
     * @param zipFileName   压缩包文件名
     * @param unzipTimes    压缩包的第几层。最开始的压缩包解压后，里面的文件为第一层，压缩包里的压缩包再解压，则加一层。以此类推……
     * @param unzipLevel    解压层级。-1：无限解压，碰到压缩包就解压；0：只解压<b>当前压缩包</b>，不解压内部压缩包；&gt;=1：对内部压缩包的解压次数
     * @param unzipFilter   内部压缩包的是否解压的过滤器，为{@code null}则<b>都解压</b>， {@code PT3<Integer, String, String, Exception>(压缩包的第几层, 父压缩包的文件名，当前内部文件的名称)}
     * @param otherFilter   除压缩包以外的文件是否处理的过滤器，为{@code null}则<b>都处理</b>， {@code PT3<Integer, String, String, Exception>(压缩包的第几层, 父压缩包的文件名，当前内部文件的名称)}
     * @param otherAction   非压缩包的处理逻辑 {@code RT5<InputStream, Integer, String, String, Set<AutoCloseable>, R, Exception>(解压之前文件流, 压缩包的第几层, 父压缩包的文件名，当前内部文件的名称, 外部调用者需要程序自动关闭的资源集合, 返回值)}<br>
     *                      <u><b>外部调用者需要程序自动关闭的资源集合：</b>外部调用者有自己需要关闭的资源，这些资源通常引用了内部的InputStream，为了避免将内部的InputStream关闭，则需要将InputStream复制一份，再关闭。但这会极大影响性能。
     *                      为了提高性能，外部调用者可以不必自己关闭资源，将需要关闭的资源添加进{@code Set<AutoCloseable>}，交由程序内部来进行关闭。</u>
     * @param zipLogLevel   解压缩日志等级
     * @param superACs      支持哪些类型的压缩/解压处理器（理论上应该传入不可变的Map{@link Collections#unmodifiableMap(Map)}，避免被外部修改）
     * @param closeables    解压过程涉及到的所有需要关闭的资源
     * @param unzipId       解压ID，用于日志记录
     * @param <R>           Action返回值类型
     * @return 返回任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     * @since 0.3.2
     */
    @Override
    public <R> List<R> unzipFast(InputStream is,
                                 String parentZipName,
                                 String zipFileName,
                                 int unzipTimes,
                                 int unzipLevel,
                                 PT3<? super Integer, ? super String, ? super String, Exception> unzipFilter,
                                 PT3<? super Integer, ? super String, ? super String, Exception> otherFilter,
                                 RT5<InputStream, ? super Integer, ? super String, ? super String, ? super Set<AutoCloseable>, ? extends R, Exception> otherAction,
                                 ZipLogLevel zipLogLevel,
                                 Map<ArchiveFormat, SuperAC> superACs,
                                 Set<AutoCloseable> closeables,
                                 String unzipId
    ) throws Exception {
        if (zipFileName == null) zipFileName = "";

        // >>> 打印日志参数
        unzipId = getUnzipId(5, unzipId);
        final String logSource = getClass().getSimpleName() + ".unzipFast()";
        // <<< 打印日志参数

        final ArrayList<R> rs = new ArrayList<>();
        BZip2CompressorInputStream zipis = null;
        try {
            if (unzipACMap == null) unzipACMap = toSuperACMap(superACs);

            String entryFileName = ZipKit.getUncompressedName(zipFileName, format());

            zipis = new BZip2CompressorInputStream(is, unzipInputProperty.isDecompressConcatenated());

            final int newUnzipTimes = unzipTimes + 1;
            final int newUnzipLevel = unzipLevel <= 0 ? unzipLevel : unzipLevel - 1;

            unzipFast(zipis, rs, zipFileName, entryFileName, unzipTimes, unzipLevel, newUnzipTimes, newUnzipLevel, unzipACMap,
                    unzipFilter, otherFilter, otherAction, zipLogLevel, unzipId, logSource, closeables);

        } finally {
            closeables.add(zipis);
            closeables.add(is);
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
     * </ul>
     *
     * @param is                输入流
     * @param parentZipName     父级压缩包文件名
     * @param zipFileName       压缩包文件名
     * @param unzipTimes        压缩包的第几层。最开始的压缩包解压后，里面的文件为第一层，压缩包里的压缩包再解压，则加一层。以此类推……
     * @param unzipLevel        解压层级。-1：无限解压，碰到压缩包就解压；0：只解压<b>当前压缩包</b>，不解压内部压缩包；&gt;=1：对内部压缩包的解压次数
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
     * @param unzipId           解压ID，用于日志记录
     * @return 返回 压缩后的字节流数组 以及 任意你想返回的内容，便于你在lambda表达式外进行操作
     * @throws Exception 处理过程可能抛异常
     */
    @Override
    public <R> ZipResult<R> reZip(final InputStream is,
                                  String parentZipName,
                                  String zipFileName,
                                  final int unzipTimes,
                                  final int unzipLevel,
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
                                  Map<ArchiveFormat, SuperAC> superACs,
                                  String unzipId
    ) throws Exception {
        if (zipFileName == null) zipFileName = "";

        // >>> 打印日志参数
        unzipId = getUnzipId(5, unzipId);
        final String logSource = getClass().getSimpleName() + ".reZip()";
        // <<< 打印日志参数

        final ArrayList<R> rs = new ArrayList<>();
        BZip2CompressorInputStream zipis = null;
        MultiByteArrayOutputStream baos = null;
        BZip2CompressorOutputStream zos = null;
        try {
            if (reZipACMap == null) reZipACMap = toSuperACMap(superACs);

            baos = new MultiByteArrayOutputStream();
            zipis = new BZip2CompressorInputStream(is, reZipInputProperty.isDecompressConcatenated());
            String entryFileName = ZipKit.getUncompressedName(zipFileName, format());

            zos = new BZip2CompressorOutputStream(baos, reZipOutputProperty.getBlockSize());

            final int newUnzipTimes = unzipTimes + 1;
            final int newUnzipLevel = unzipLevel <= 0 ? unzipLevel : unzipLevel - 1;

            byte[][] byteArrays = reZip(zipis, rs, zipFileName, entryFileName, unzipTimes, unzipLevel,
                    newUnzipTimes, newUnzipLevel, reZipACMap, addFileFilter, deleteFileFilter, unzipFilter, otherFilter,
                    beforeUnzipFilter, afterZipFilter, addFilesAction, addBytesAction, deleteFileAction,
                    beforeUnzipAction, afterZipAction, otherAction, zipLogLevel, unzipId, logSource);

            // 打印日志信息
            Stopwatch stopwatch = Stopwatch.run();
            LogPrinter.printBeforeWriteZip(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource);

            long byteLength = 0;
            for (byte[] bytes : byteArrays) {
                byteLength += bytes.length;
                zos.write(bytes);
            }

            // 打印日志信息
            LogPrinter.printAfterWriteZip(unzipId, unzipTimes, zipFileName, entryFileName, zipLogLevel, logSource, stopwatch, byteLength);

        } finally {
            Close.close(zipis);
            Close.close(is);
            Close.close(zos);
            Close.close(baos);
        }
        return ZipResult.of(baos.toByteArrays(), rs);
    }

}
