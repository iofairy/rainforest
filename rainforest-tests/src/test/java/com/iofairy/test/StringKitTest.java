package com.iofairy.test;

import com.iofairy.rainforest.zip.utils.StringKit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author GG
 * @version 1.0
 * @date 2023/6/10 21:39
 */
public class StringKitTest {
    @Test
    void testConvertByte() {
        String byte01 = StringKit.convertByte(0);
        String byte02 = StringKit.convertByte(1);
        String byte03 = StringKit.convertByte(500);
        String byte04 = StringKit.convertByte(1024);
        String byte05 = StringKit.convertByte(2024);
        String byte06 = StringKit.convertByte(5024);
        String byte07 = StringKit.convertByte(1073741824);
        String byte08 = StringKit.convertByte(2173741824L);
        String byte09 = StringKit.convertByte(3573741824L);
        String byte10 = StringKit.convertByte(1099511627776L);
        String byte11 = StringKit.convertByte(1999511627776L);
        System.out.println(byte01);     // 0B
        System.out.println(byte02);     // 1B
        System.out.println(byte03);     // 500B
        System.out.println(byte04);     // 1.0KB
        System.out.println(byte05);     // 2.0KB
        System.out.println(byte06);     // 4.9KB
        System.out.println(byte07);     // 1.0GB
        System.out.println(byte08);     // 2.0GB
        System.out.println(byte09);     // 3.3GB
        System.out.println(byte10);     // 1.0TB
        System.out.println(byte11);     // 1.8TB

        assertEquals(byte01, "0B");
        assertEquals(byte02, "1B");
        assertEquals(byte03, "500B");
        assertEquals(byte04, "1.0KB");
        assertEquals(byte05, "2.0KB");
        assertEquals(byte06, "4.9KB");
        assertEquals(byte07, "1.0GB");
        assertEquals(byte08, "2.0GB");
        assertEquals(byte09, "3.3GB");
        assertEquals(byte10, "1.0TB");
        assertEquals(byte11, "1.8TB");
    }

    @Test
    void testConvertTime() {
        String time01 = StringKit.convertTime(-1000000);
        String time02 = StringKit.convertTime(-1);
        String time03 = StringKit.convertTime(0);
        String time04 = StringKit.convertTime(1);
        String time05 = StringKit.convertTime(500);
        String time06 = StringKit.convertTime(1000);
        String time07 = StringKit.convertTime(3000);
        String time08 = StringKit.convertTime(9000);
        String time09 = StringKit.convertTime(7000000);
        String time10 = StringKit.convertTime(86400000);
        String time11 = StringKit.convertTime(306400000);

        System.out.println(time01);
        System.out.println(time02);
        System.out.println(time03);
        System.out.println(time04);
        System.out.println(time05);
        System.out.println(time06);
        System.out.println(time07);
        System.out.println(time08);
        System.out.println(time09);
        System.out.println(time10);
        System.out.println(time11);

        assertEquals(time01, "-16.7分");
        assertEquals(time02, "-1毫秒");
        assertEquals(time03, "0毫秒");
        assertEquals(time04, "1毫秒");
        assertEquals(time05, "500毫秒");
        assertEquals(time06, "1.0秒");
        assertEquals(time07, "3.0秒");
        assertEquals(time08, "9.0秒");
        assertEquals(time09, "1.9时");
        assertEquals(time10, "1.0天");
        assertEquals(time11, "3.5天");

    }

}
