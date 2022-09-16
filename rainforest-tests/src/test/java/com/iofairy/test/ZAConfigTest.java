package com.iofairy.test;

import com.iofairy.falcon.zip.ArchiveFormat;
import com.iofairy.rainforest.zip.ZAConfig;
import com.iofairy.rainforest.zip.attr.GzipInputProperty;
import com.iofairy.rainforest.zip.attr.ZipOutputProperty;
import org.junit.jupiter.api.Test;

import static com.iofairy.falcon.zip.ArchiveFormat.*;

/**
 * @author GG
 * @version 1.0
 * @date 2022/9/13 6:21
 */
public class ZAConfigTest {
    @Test
    void testZAConfig() {
        ZAConfig zaConfig0 = ZAConfig.DEFAULT_ZACONFIG;
        ZAConfig zaConfig1 = ZAConfig.of();
        ZAConfig zaConfig2 = ZAConfig.of(new ArchiveFormat[]{ZIP, TAR, TAR_GZ}, GzipInputProperty.of(), ZipOutputProperty.of());
        System.out.println("zaConfig0: " + zaConfig0);
        System.out.println("zaConfig1: " + zaConfig1);
        System.out.println("zaConfig2: " + zaConfig2);
        zaConfig2.addToNeedUnZipFormats(GZIP);
        System.out.println("zaConfig2: " + zaConfig2);
        zaConfig2.setNeedUnZipFormats(GZIP);
        System.out.println("zaConfig2: " + zaConfig2);
        zaConfig2.setNeedUnZipFormats(null);
        System.out.println("zaConfig2: " + zaConfig2);
        zaConfig2.putProperties(GzipInputProperty.of().setFileNameEncoding("UTF-8"));
        System.out.println("zaConfig2: " + zaConfig2);

    }
}
