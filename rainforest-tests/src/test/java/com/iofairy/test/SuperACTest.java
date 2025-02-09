package com.iofairy.test;

import cn.hutool.core.io.FileUtil;
import com.iofairy.falcon.io.IOs;
import com.iofairy.falcon.io.MultiByteArrayInputStream;
import com.iofairy.falcon.time.DateTime;
import com.iofairy.falcon.util.Numbers;
import com.iofairy.falcon.zip.ArchiveFormat;
import com.iofairy.rainforest.zip.ac.Super7Zip;
import com.iofairy.rainforest.zip.ac.SuperAC;
import com.iofairy.rainforest.zip.ac.SuperACs;
import com.iofairy.rainforest.zip.ac.SuperZipProtected;
import com.iofairy.rainforest.zip.base.*;
import com.iofairy.rainforest.zip.config.PasswordProvider;
import com.iofairy.rainforest.zip.config.ZipPassword;
import com.iofairy.rainforest.zip.error.SuperACException;
import com.iofairy.top.G;
import com.iofairy.tuple.Tuple;
import com.iofairy.tuple.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author GG
 * @version 1.0
 * @date 2024/4/1 16:28
 */
@Slf4j
public class SuperACTest {
    static File resDir = null;     // 资源目录
    static File zipDir = null;     // 压缩包目录
    static File rezipOutputDir = null; // 重压缩输出目录
    static File beforeActionDir = null; // 操作前目录
    static File afterActionDir = null; // 操作后目录

    static {
        resDir = new File("src/test/resources");
        zipDir = new File(resDir, "zip-files");
        rezipOutputDir = new File(zipDir, "rezipOutput");
        afterActionDir = new File(zipDir, "afterAction");
        beforeActionDir = new File(zipDir, "beforeAction");
        FileUtil.mkdir(rezipOutputDir);
        FileUtil.mkdir(afterActionDir);
        FileUtil.mkdir(beforeActionDir);

        /*
         JUL 转 logback
         */
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }


    /**
     * 混合压缩包测试
     */
    @Test
    void testRezip() {
        String zipFileName = "tar（1）.tar.bz2";

        try (FileInputStream is = new FileInputStream(new File(zipDir, zipFileName))) {
            ZipResult<String> zipResult = SuperAC.reZip(
                    is,
                    ArchiveFormat.TAR_BZ2,
                    zipFileName,
                    -1,
                    (times, zipName) -> true,
                    (times, zipName, entryName) -> entryName.startsWith("要删除的" + times),
                    null,
                    (times, zipName, entryName) -> entryName.endsWith("txt"),
                    null,
                    (times, zipName, entryName) -> true,
                    /*
                     设置要往压缩包中添加的文件，如果没有，直接设置为 null
                     */
                    (times, zipName) -> {
                        List<File> files = Arrays.asList(new File(zipDir, "add-files/++新建 DOCX 文档 - 副本.docx"));
                        List<AddFile> addFiles = files.stream().map(e -> AddFile.of(e, "添加的文件/" + e.getName(), e.isDirectory())).collect(Collectors.toList());
                        addFiles.add(AddFile.of(null, "添加的目录/", true));

                        List<String> returnList = files.stream().map(e -> e.getName()).collect(Collectors.toList());
                        return Tuple.of(addFiles, returnList);
                    },
                    /*
                     设置要往压缩包中添加的字节，如果没有，直接设置为 null
                     */
                    (times, zipName) -> {
                        ArrayList<AddBytes> addBytes = new ArrayList<>();
                        try (FileInputStream fis1 = new FileInputStream(new File(zipDir, "add-files/++新建 文本文档.txt"));
                             FileInputStream fis2 = new FileInputStream(new File(zipDir, "add-files/++新建 DOCX 文档.docx"));
                        ) {
                            byte[][] bytes1 = IOs.toMultiBAOS(fis1).toByteArrays();
                            byte[][] bytes2 = IOs.toMultiBAOS(fis2).toByteArrays();
                            AddBytes addBytes1 = AddBytes.of(bytes1, "添加字节/++新建 文本文档.txt", false);
                            AddBytes addBytes2 = AddBytes.of(bytes2, "添加字节/++新建 DOCX 文档.docx", false);
                            addBytes.add(addBytes1);
                            addBytes.add(addBytes2);
                        }

                        /*
                         是目录，带 /    --------- 这个被当作目录
                         */
                        AddBytes addBytes1 = AddBytes.of(null, "AddBytes添加的目录1/", true);
                        // 是目录，不带 /   --------- 这个被当作 0 字节的文件
                        AddBytes addBytes2 = AddBytes.of(null, "AddBytes添加的目录2", true);
                        /*
                         不是目录，带 /，字节为0      --------- 这个被当作目录
                         */
                        AddBytes addBytes3 = AddBytes.of(new byte[0][], "AddBytes添加的目录3/", false);
                        // 不是目录，不带 /，但字节为0      --------- 这个被当作 0 字节的文件
                        AddBytes addBytes4 = AddBytes.of(new byte[0][], "AddBytes添加的目录4", false);

                        addBytes.add(addBytes1);
                        addBytes.add(addBytes2);
                        addBytes.add(addBytes3);
                        addBytes.add(addBytes4);

                        List<String> returnList = addBytes.stream().map(e -> e.getEntryFileName()).collect(Collectors.toList());
                        return Tuple.of(addBytes, returnList);
                    },
                    null,
                    null,
                    null,
                    (input, output, times, zipName, entryName) -> {
                        System.out.println("处理其他文件：" + zipName + "---" + entryName);

                        IOs.copy(input, output);
                        output.write(("\n" + DateTime.nowDate() + ">>>>>>>这是这是新增加的一行\n").getBytes());

                        return "其他文件处理：" + entryName;
                    },
                    ZipLogLevel.DETAIL,
                    SuperACs.allSupportedSuperACs()
            );

            List<String> results = zipResult.getResults();
            log.info("===============================================================================");
            System.out.println(results);

            byte[][] bytes = zipResult.getBytes();
            String outputFilename = "重压缩输出_" + Numbers.randomInt(4) + ".tar.bz2";
            log.info("输出的文件名：{}", outputFilename);
            File dest = new File(rezipOutputDir, outputFilename);

            MultiByteArrayInputStream multiByteArrayInputStream = new MultiByteArrayInputStream(bytes);
            FileUtil.writeFromStream(multiByteArrayInputStream, dest, true);

            log.info("===============================================================================");
        } catch (Exception e) {
            System.out.println("\n" + G.stackTrace(e));
        }
    }

    /**
     * Zstd混合压缩包测试
     */
    @Test
    void testRezipZstd() {
        String zipFileName = "Zstd测试.7z";

        try (FileInputStream is = new FileInputStream(new File(zipDir, zipFileName))) {
            ZipResult<String> zipResult = SuperAC.reZip(
                    is,
                    ArchiveFormat.SEVEN_ZIP,
                    zipFileName,
                    -1,
                    (times, zipName) -> true,
                    (times, zipName, entryName) -> entryName.startsWith("要删除的" + times),
                    null,
                    (times, zipName, entryName) -> entryName.endsWith("csv"),
                    null,
                    (times, zipName, entryName) -> true,
                    /*
                     设置要往压缩包中添加的文件，如果没有，直接设置为 null
                     */
                    (times, zipName) -> {
                        List<File> files = Arrays.asList(new File(zipDir, "add-files/++新建 DOCX 文档 - 副本.docx"));
                        List<AddFile> addFiles = files.stream().map(e -> AddFile.of(e, "添加的文件/" + e.getName(), e.isDirectory())).collect(Collectors.toList());
                        addFiles.add(AddFile.of(null, "添加的目录/", true));

                        List<String> returnList = files.stream().map(e -> e.getName()).collect(Collectors.toList());
                        return Tuple.of(addFiles, returnList);
                    },
                    /*
                     设置要往压缩包中添加的字节，如果没有，直接设置为 null
                     */
                    (times, zipName) -> {
                        ArrayList<AddBytes> addBytes = new ArrayList<>();
                        try (FileInputStream fis1 = new FileInputStream(new File(zipDir, "add-files/++新建 文本文档.txt"));
                             FileInputStream fis2 = new FileInputStream(new File(zipDir, "add-files/++新建 DOCX 文档.docx"));
                        ) {
                            byte[][] bytes1 = IOs.toMultiBAOS(fis1).toByteArrays();
                            byte[][] bytes2 = IOs.toMultiBAOS(fis2).toByteArrays();
                            AddBytes addBytes1 = AddBytes.of(bytes1, "添加字节/++新建 文本文档.txt", false);
                            AddBytes addBytes2 = AddBytes.of(bytes2, "添加字节/++新建 DOCX 文档.docx", false);
                            addBytes.add(addBytes1);
                            addBytes.add(addBytes2);
                        }

                        /*
                         是目录，带 /    --------- 这个被当作目录
                         */
                        AddBytes addBytes1 = AddBytes.of(null, "AddBytes添加的目录1/", true);
                        // 是目录，不带 /   --------- 这个被当作 0 字节的文件
                        AddBytes addBytes2 = AddBytes.of(null, "AddBytes添加的目录2", true);
                        /*
                         不是目录，带 /，字节为0      --------- 这个被当作目录
                         */
                        AddBytes addBytes3 = AddBytes.of(new byte[0][], "AddBytes添加的目录3/", false);
                        // 不是目录，不带 /，但字节为0      --------- 这个被当作 0 字节的文件
                        AddBytes addBytes4 = AddBytes.of(new byte[0][], "AddBytes添加的目录4", false);

                        addBytes.add(addBytes1);
                        addBytes.add(addBytes2);
                        addBytes.add(addBytes3);
                        addBytes.add(addBytes4);

                        List<String> returnList = addBytes.stream().map(e -> e.getEntryFileName()).collect(Collectors.toList());
                        return Tuple.of(addBytes, returnList);
                    },
                    null,
                    null,
                    null,
                    (input, output, times, zipName, entryName) -> {
                        System.out.println("处理其他文件：" + zipName + "---" + entryName);

                        IOs.copy(input, output);
                        output.write(("\n" + DateTime.nowDate() + ">>>>>>>这是这是新增加的一行\n").getBytes());

                        return "其他文件处理：" + entryName;
                    },
                    ZipLogLevel.DETAIL,
                    SuperACs.allSupportedSuperACs()
            );

            List<String> results = zipResult.getResults();
            log.info("===============================================================================");
            System.out.println(results);

            byte[][] bytes = zipResult.getBytes();
            String outputFilename = "重压缩输出_" + Numbers.randomInt(4) + ".7z";
            log.info("输出的文件名：{}", outputFilename);
            File dest = new File(rezipOutputDir, outputFilename);

            MultiByteArrayInputStream multiByteArrayInputStream = new MultiByteArrayInputStream(bytes);
            FileUtil.writeFromStream(multiByteArrayInputStream, dest, true);

            log.info("===============================================================================");
        } catch (Exception e) {
            System.out.println("\n" + G.stackTrace(e));
        }
    }


    /**
     * 带密码的测试
     */
    @Test
    void testRezip_WithRightPassword() {
        PasswordProvider provider = PasswordProvider.of(
                ZipPassword.of("*.zip", "zipfdskafj$%"),
                ZipPassword.of("*.7z", "fdskafj#$7zip")
        );

        SuperZipProtected superZipProtected = SuperZipProtected.of().setReZipPasswordProvider(provider);
        Super7Zip super7Zip = Super7Zip.of().setReZipPasswordProvider(provider);

        String zipFileName = "7zip密码、zip密码、7zip密码.7z";

        try (FileInputStream is = new FileInputStream(new File(zipDir, zipFileName))) {
            ZipResult<String> zipResult = SuperAC.reZip(
                    is,
                    ArchiveFormat.SEVEN_ZIP,
                    zipFileName,
                    -1,
                    (times, zipName) -> true,
                    (times, zipName, entryName) -> entryName.startsWith("要删除的" + times),
                    null,
                    (times, zipName, entryName) -> entryName.endsWith("txt"),
                    null,
                    (times, zipName, entryName) -> true,
                    /*
                     设置要往压缩包中添加的文件，如果没有，直接设置为 null
                     */
                    (times, zipName) -> {
                        List<File> files = Arrays.asList(new File(zipDir, "add-files/++新建 DOCX 文档 - 副本.docx"));
                        List<AddFile> addFiles = files.stream().map(e -> AddFile.of(e, "添加的文件/" + e.getName(), e.isDirectory())).collect(Collectors.toList());
                        addFiles.add(AddFile.of(null, "添加的目录/", true));

                        List<String> returnList = files.stream().map(e -> e.getName()).collect(Collectors.toList());
                        return Tuple.of(addFiles, returnList);
                    },
                    /*
                     设置要往压缩包中添加的字节，如果没有，直接设置为 null
                     */
                    (times, zipName) -> {
                        ArrayList<AddBytes> addBytes = new ArrayList<>();
                        try (FileInputStream fis1 = new FileInputStream(new File(zipDir, "add-files/++新建 文本文档.txt"));
                             FileInputStream fis2 = new FileInputStream(new File(zipDir, "add-files/++新建 DOCX 文档.docx"));
                        ) {
                            byte[][] bytes1 = IOs.toMultiBAOS(fis1).toByteArrays();
                            byte[][] bytes2 = IOs.toMultiBAOS(fis2).toByteArrays();
                            AddBytes addBytes1 = AddBytes.of(bytes1, "添加字节/++新建 文本文档.txt", false);
                            AddBytes addBytes2 = AddBytes.of(bytes2, "添加字节/++新建 DOCX 文档.docx", false);
                            addBytes.add(addBytes1);
                            addBytes.add(addBytes2);
                        }

                        /*
                         是目录，带 /    --------- 这个被当作目录
                         */
                        AddBytes addBytes1 = AddBytes.of(null, "AddBytes添加的目录1/", true);
                        // 是目录，不带 /   --------- 这个被当作 0 字节的文件
                        AddBytes addBytes2 = AddBytes.of(null, "AddBytes添加的目录2", true);
                        /*
                         不是目录，带 /，字节为0      --------- 这个被当作目录
                         */
                        AddBytes addBytes3 = AddBytes.of(new byte[0][], "AddBytes添加的目录3/", false);
                        // 不是目录，不带 /，但字节为0      --------- 这个被当作 0 字节的文件
                        AddBytes addBytes4 = AddBytes.of(new byte[0][], "AddBytes添加的目录4", false);

                        addBytes.add(addBytes1);
                        addBytes.add(addBytes2);
                        addBytes.add(addBytes3);
                        addBytes.add(addBytes4);

                        List<String> returnList = addBytes.stream().map(e -> e.getEntryFileName()).collect(Collectors.toList());
                        return Tuple.of(addBytes, returnList);
                    },
                    null,
                    null,
                    null,
                    (input, output, times, zipName, entryName) -> {
                        System.out.println("处理其他文件：" + zipName + "---" + entryName);

                        IOs.copy(input, output);
                        output.write(("\n" + DateTime.nowDate() + ">>>>>>>这是这是新增加的一行\n").getBytes());

                        return "其他文件处理：" + entryName;
                    },
                    ZipLogLevel.DETAIL,
                    Arrays.asList(superZipProtected, super7Zip)
            );

            List<String> results = zipResult.getResults();
            log.info("===============================================================================");
            System.out.println(results);

            byte[][] bytes = zipResult.getBytes();
            String outputFilename = "重压缩输出_" + Numbers.randomInt(4) + ".7z";
            log.info("输出的文件名：{}", outputFilename);
            File dest = new File(rezipOutputDir, outputFilename);

            MultiByteArrayInputStream multiByteArrayInputStream = new MultiByteArrayInputStream(bytes);
            FileUtil.writeFromStream(multiByteArrayInputStream, dest, true);

            log.info("===============================================================================");
        } catch (Exception e) {
            System.out.println("\n" + G.stackTrace(e));
        }

    }

    /**
     * 带错误的密码测试
     */
    @Test
    void testRezip_WithWrongPassword() {
        PasswordProvider provider = PasswordProvider.of(
                // ZipPassword.of("*.zip", "zipfdskafj$%"),
                ZipPassword.of("*.7z", "fdskafj#$7zip")
        );

        SuperZipProtected superZipProtected = SuperZipProtected.of().setReZipPasswordProvider(provider);
        Super7Zip super7Zip = Super7Zip.of().setReZipPasswordProvider(provider);

        String zipFileName = "7zip密码、zip密码、7zip密码.7z";

        try (FileInputStream is = new FileInputStream(new File(zipDir, zipFileName))) {
            ZipResult<String> zipResult = SuperAC.reZip(
                    is,
                    ArchiveFormat.SEVEN_ZIP,
                    zipFileName,
                    -1,
                    (times, zipName) -> true,
                    (times, zipName, entryName) -> entryName.startsWith("要删除的" + times),
                    null,
                    (times, zipName, entryName) -> entryName.endsWith("txt"),
                    null,
                    (times, zipName, entryName) -> true,
                    null,
                    null,
                    null,
                    null,
                    null,
                    (input, output, times, zipName, entryName) -> {
                        System.out.println("处理其他文件：" + zipName + "---" + entryName);

                        IOs.copy(input, output);
                        output.write(("\n" + DateTime.nowDate() + ">>>>>>>这是这是新增加的一行\n").getBytes());

                        return "其他文件处理：" + entryName;
                    },
                    ZipLogLevel.DETAIL,
                    Arrays.asList(superZipProtected, super7Zip)
            );

            List<String> results = zipResult.getResults();
            log.info("===============================================================================");
            System.out.println(results);

            byte[][] bytes = zipResult.getBytes();
            String outputFilename = "重压缩输出_" + Numbers.randomInt(4) + ".7z";
            log.info("输出的文件名：{}", outputFilename);
            File dest = new File(rezipOutputDir, outputFilename);

            MultiByteArrayInputStream multiByteArrayInputStream = new MultiByteArrayInputStream(bytes);
            FileUtil.writeFromStream(multiByteArrayInputStream, dest, true);
            log.info("===============================================================================");
        } catch (Exception e) {
            assertSame(e.getClass(), SuperACException.class);
            System.out.println("\ntestRezip_WithWrongPassword: \n" + G.stackTrace(e));
        }
    }


    /**
     * 带密码的测试
     */
    @Test
    void testRezip_WithRightPassword1() {
        PasswordProvider provider = PasswordProvider.of(
                ZipPassword.of("*.zip", "zipfdskafj$%"),
                // ZipPassword.of("*.7z", "fdskafj#$7zip")
                ZipPassword.of("7zip密码、zip密码、7zip无.7z", "fdskafj#$7zip")
        );

        SuperZipProtected superZipProtected = SuperZipProtected.of().setReZipPasswordProvider(provider);
        Super7Zip super7Zip = Super7Zip.of().setReZipPasswordProvider(provider);

        String zipFileName = "7zip密码、zip密码、7zip无.7z";

        try (FileInputStream is = new FileInputStream(new File(zipDir, zipFileName))) {
            ZipResult<String> zipResult = SuperAC.reZip(
                    is,
                    ArchiveFormat.SEVEN_ZIP,
                    zipFileName,
                    -1,
                    (times, zipName) -> true,
                    (times, zipName, entryName) -> entryName.startsWith("要删除的" + times),
                    null,
                    (times, zipName, entryName) -> entryName.endsWith("txt"),
                    null,
                    (times, zipName, entryName) -> true,
                    /*
                     设置要往压缩包中添加的文件，如果没有，直接设置为 null
                     */
                    (times, zipName) -> {
                        List<File> files = Arrays.asList(new File(zipDir, "add-files/++新建 DOCX 文档 - 副本.docx"));
                        List<AddFile> addFiles = files.stream().map(e -> AddFile.of(e, "添加的文件/" + e.getName(), e.isDirectory())).collect(Collectors.toList());
                        addFiles.add(AddFile.of(null, "添加的目录/", true));

                        List<String> returnList = files.stream().map(e -> e.getName()).collect(Collectors.toList());
                        return Tuple.of(addFiles, returnList);
                    },
                    /*
                     设置要往压缩包中添加的字节，如果没有，直接设置为 null
                     */
                    (times, zipName) -> {
                        ArrayList<AddBytes> addBytes = new ArrayList<>();
                        try (FileInputStream fis1 = new FileInputStream(new File(zipDir, "add-files/++新建 文本文档.txt"));
                             FileInputStream fis2 = new FileInputStream(new File(zipDir, "add-files/++新建 DOCX 文档.docx"));
                        ) {
                            byte[][] bytes1 = IOs.toMultiBAOS(fis1).toByteArrays();
                            byte[][] bytes2 = IOs.toMultiBAOS(fis2).toByteArrays();
                            AddBytes addBytes1 = AddBytes.of(bytes1, "添加字节/++新建 文本文档.txt", false);
                            AddBytes addBytes2 = AddBytes.of(bytes2, "添加字节/++新建 DOCX 文档.docx", false);
                            addBytes.add(addBytes1);
                            addBytes.add(addBytes2);
                        }

                        /*
                         是目录，带 /    --------- 这个被当作目录
                         */
                        AddBytes addBytes1 = AddBytes.of(null, "AddBytes添加的目录1/", true);
                        // 是目录，不带 /   --------- 这个被当作 0 字节的文件
                        AddBytes addBytes2 = AddBytes.of(null, "AddBytes添加的目录2", true);
                        /*
                         不是目录，带 /，字节为0      --------- 这个被当作目录
                         */
                        AddBytes addBytes3 = AddBytes.of(new byte[0][], "AddBytes添加的目录3/", false);
                        // 不是目录，不带 /，但字节为0      --------- 这个被当作 0 字节的文件
                        AddBytes addBytes4 = AddBytes.of(new byte[0][], "AddBytes添加的目录4", false);

                        addBytes.add(addBytes1);
                        addBytes.add(addBytes2);
                        addBytes.add(addBytes3);
                        addBytes.add(addBytes4);

                        List<String> returnList = addBytes.stream().map(e -> e.getEntryFileName()).collect(Collectors.toList());
                        return Tuple.of(addBytes, returnList);
                    },
                    null,
                    null,
                    null,
                    (input, output, times, zipName, entryName) -> {
                        System.out.println("处理其他文件：" + zipName + "---" + entryName);

                        IOs.copy(input, output);
                        output.write(("\n" + DateTime.nowDate() + ">>>>>>>这是这是新增加的一行\n").getBytes());

                        return "其他文件处理：" + entryName;
                    },
                    ZipLogLevel.DETAIL,
                    Arrays.asList(superZipProtected, super7Zip)
            );

            List<String> results = zipResult.getResults();
            log.info("===============================================================================");
            System.out.println(results);

            byte[][] bytes = zipResult.getBytes();
            String outputFilename = "重压缩输出_" + Numbers.randomInt(4) + ".7z";
            log.info("输出的文件名：{}", outputFilename);
            File dest = new File(rezipOutputDir, outputFilename);

            MultiByteArrayInputStream multiByteArrayInputStream = new MultiByteArrayInputStream(bytes);
            FileUtil.writeFromStream(multiByteArrayInputStream, dest, true);

            log.info("===============================================================================");
        } catch (Exception e) {
            System.out.println("\n" + G.stackTrace(e));
        }
    }

    /**
     * 解压
     */
    @Test
    void testUnzip() {
        String zipFileName = "unzip.zip";

        try (FileInputStream is = new FileInputStream(new File(zipDir, zipFileName))) {
            List<Tuple2<String, Long>> result = SuperAC.unzip(
                    is,
                    ArchiveFormat.ZIP,                     // 压缩包的类型
                    zipFileName,                                      // 压缩包名称
                    -1,                                                 // 解压几层，-1则无限解压
                    (times, zipName, entryName) -> true,                // 压缩包内部的压缩包是否解压
                    (times, zipName, entryName) -> entryName.endsWith(".csv"),    // 非压缩包中，只处理 .csv 的文件
                    (times, zipName, entryName) -> false,
                    null,
                    (input, times, zipName, entryName) -> {     // 如何处理非压缩包
                        // 这里必须复制一份，否则内部流会被关闭，导致异常
                        MultiByteArrayInputStream inputStream = IOs.toMultiBAIS(input);
                        return Tuple.of(zipName + "/" + entryName, unzipCountLine(inputStream, "UTF-8"));
                    },
                    ZipLogLevel.DETAIL,
                    SuperACs.allSupportedSuperACs()
            );
            log.info("===============================================================================");
            System.out.println(result);
            log.info("===============================================================================");

        } catch (Exception e) {
            System.out.println(G.stackTrace(e));
        }
    }


    /**
     * 快速解压
     */
    @Test
    void testUnzipFast() {
        PasswordProvider provider = PasswordProvider.of(ZipPassword.of("*.7z", "fdskafj#$7zip"));
        Super7Zip super7Zip = Super7Zip.of().setUnzipPasswordProvider(provider);

        String zipFileName = "统计行数.7z.gz";

        try (FileInputStream is = new FileInputStream(new File(zipDir, zipFileName))) {
            List<SuperAC> superACs = SuperACs.allSupportedSuperACs();
            superACs.add(super7Zip);

            List<Tuple2<String, Long>> result = SuperAC.unzipFast(
                    is,
                    ArchiveFormat.GZIP,                     // 压缩包的类型
                    zipFileName,                                      // 压缩包名称
                    -1,                                                 // 解压几层，-1则无限解压
                    (times, zipName, entryName) -> true,                // 压缩包内部的压缩包是否解压
                    (times, zipName, entryName) -> entryName.endsWith(".csv"),    // 非压缩包中，只处理 .csv 的文件
                    (input, times, zipName, entryName, closeables) -> {     // 如何处理非压缩包
                        return Tuple.of(zipName + "/" + entryName, unzipFastCountLine(input, "UTF-8", closeables));
                    },
                    ZipLogLevel.DETAIL,
                    superACs
            );

            log.info("===============================================================================");
            System.out.println(result);
            log.info("===============================================================================");

        } catch (Exception e) {
            System.out.println(G.stackTrace(e));
        }
    }

    private static long unzipCountLine(InputStream inputStream, String charsetName) throws IOException {
        long rowCount = 0;

        /*
         * 必须自己关闭流！
         * 必须自己关闭流！
         * 必须自己关闭流！
         */
        try (InputStreamReader in = new InputStreamReader(inputStream, charsetName);
             BufferedReader br = new BufferedReader(in)) {
            String line;
            while ((line = br.readLine()) != null) {
                rowCount++;
            }
        }
        return rowCount;
    }

    private static long unzipFastCountLine(InputStream is, String charsetName, Set<AutoCloseable> closeables) throws Exception {
        long rowCount = 0;

        InputStreamReader in = null;
        BufferedReader br = null;
        try {
            in = new InputStreamReader(is, charsetName);
            br = new BufferedReader(in);
            while (br.readLine() != null) {
                rowCount++;
            }
            return rowCount;
        } finally {
            /*
             * 必须将要关闭的对象放入 closeables 集合中，交由 SuperAC.unzipFast 统一关闭！
             * 必须将要关闭的对象放入 closeables 集合中，交由 SuperAC.unzipFast 统一关闭！
             * 必须将要关闭的对象放入 closeables 集合中，交由 SuperAC.unzipFast 统一关闭！
             */
            if (closeables != null) {
                closeables.add(br);
                closeables.add(in);
            }
        }
    }

    @Test
    void testAllSupportedSuperACs() {
        List<SuperAC> superACS = SuperACs.allSupportedSuperACs();
        List<ArchiveFormat> formats = superACS.stream().map(e -> e.format()).collect(Collectors.toList());
        System.out.println(formats);    // [SEVEN_ZIP, BZIP2, GZIP, TAR, TAR_BZ2, TAR_GZ, TAR_XZ, XZ, ZIP]
    }

}
