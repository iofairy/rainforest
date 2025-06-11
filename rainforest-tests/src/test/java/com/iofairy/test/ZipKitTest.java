package com.iofairy.test;

import com.iofairy.falcon.zip.ArchiveFormat;
import com.iofairy.rainforest.zip.utils.ZipKit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author GG
 * @version 1.0
 * @date 2023/6/8 20:22
 */
public class ZipKitTest {
    @Test
    void testGetUncompressedName() {
        try {
            ZipKit.getUncompressedName(null, ArchiveFormat.TAR);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), "None of these parameters [fileName, archiveFormat] can be null! But parameter [fileName] is null! ");
        }
        try {
            ZipKit.getUncompressedName("a", ArchiveFormat.TAR);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), "参数`archiveFormat`的`archiveTypes`需包含\"ArchiveType.COMPRESSION_ONLY\" 或 `archiveFormat`是 [TGZ, TAZ, TZ, TBZ2, TLZ, TXZ, TZST] 这些类型其一！");
        }
        try {
            ZipKit.getUncompressedName("/.bz2", ArchiveFormat.GZIP);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), "当前`fileName`必须以[.gz]为后缀，当前fileName为：/.bz2");
        }

        try {
            ZipKit.getUncompressedName("/.bz2", ArchiveFormat.BZIP2);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), "参数`fileName`除后缀名以外的名称不能为空！");
        }

        try {
            ZipKit.getUncompressedName("/.tbz2", ArchiveFormat.BZIP2);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), "当前`fileName`必须以[.bz2]为后缀，当前fileName为：/.tbz2");
        }
        try {
            ZipKit.getUncompressedName("/ .tbz2", ArchiveFormat.TBZ2);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(), "参数`fileName`除后缀名以外的名称不能为空！");
        }

        String uncompressedName = ZipKit.getUncompressedName("/ .bz2", ArchiveFormat.BZIP2);
        System.out.println(uncompressedName);
        assertEquals(uncompressedName, " ");

        uncompressedName = ZipKit.getUncompressedName("文件夹/文件.tar.bz2", ArchiveFormat.BZIP2);
        System.out.println(uncompressedName);
        assertEquals(uncompressedName, "文件.tar");

        uncompressedName = ZipKit.getUncompressedName("文件夹/文件.tbz2", ArchiveFormat.TBZ2);
        System.out.println(uncompressedName);
        assertEquals(uncompressedName, "文件.tar");

        uncompressedName = ZipKit.getUncompressedName("文件夹/文件.tgz", ArchiveFormat.TGZ);
        System.out.println(uncompressedName);
        assertEquals(uncompressedName, "文件.tar");

    }

}
