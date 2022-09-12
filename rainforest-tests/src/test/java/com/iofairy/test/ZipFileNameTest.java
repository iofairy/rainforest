package com.iofairy.test;

import com.iofairy.rainforest.zip.ZipFileName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author GG
 * @version 1.0
 * @date 2022/9/13 6:21
 */
public class ZipFileNameTest {
    @Test
    void testZipFileName() {
        ZipFileName zipFileName = ZipFileName.of().setGzipName("abc.csv.gz");
        ZipFileName other = zipFileName.clone();

        System.out.println(zipFileName);
        System.out.println(other);

        assertSame(zipFileName, zipFileName);
        assertNotSame(zipFileName, other);
    }
}
