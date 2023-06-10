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
package com.iofairy.rainforest.zip.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * 字符串工具类
 *
 * @since 0.2.0
 */
public class StringKit {
    /**
     * 重复字符串指定的次数
     *
     * @param str         字符串
     * @param repeatTimes 次数
     * @return 字符串
     */
    public static String repeat(String str, int repeatTimes) {
        if (str == null) return null;
        if (str.length() == 0 || repeatTimes <= 0) return "";
        if (repeatTimes == 1) return str;
        if (repeatTimes > Integer.MAX_VALUE - 8)
            throw new IllegalArgumentException("Parameter `repeatTimes` must be <= (Integer.MAX_VALUE - 8), otherwise, the memory will overflow! ");

        return new String(new char[repeatTimes]).replace("\0", str);
    }

    /**
     * 将字节转成其他大小单位
     *
     * @param bytes 字节数
     * @return 带单位的大小
     */
    public static String convertByte(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1048576) {
            double kb = bytes / 1024.0;
            DecimalFormat df = new DecimalFormat("0.0");
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df.format(kb) + "KB";
        } else if (bytes < 1073741824) {
            double mb = bytes / 1048576.0;
            DecimalFormat df = new DecimalFormat("0.0");
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df.format(mb) + "MB";
        } else if (bytes < 1099511627776L) {
            double gb = bytes / 1073741824.0;
            DecimalFormat df = new DecimalFormat("0.0");
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df.format(gb) + "GB";
        } else if (bytes < 1125899906842624L) {
            double tb = bytes / 1099511627776.0d;
            DecimalFormat df = new DecimalFormat("0.0");
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df.format(tb) + "TB";
        } else {
            double pb = bytes / 1125899906842624.0d;
            DecimalFormat df = new DecimalFormat("0.0");
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df.format(pb) + "PB";
        }
    }

    /**
     * 将毫秒转成其他时间单位
     *
     * @param millis 毫秒数
     * @return 带单位的时间
     */
    public static String convertTime(long millis) {
        if (millis < 0) {
            millis = Math.abs(millis);
            return "-" + convertTime(millis);
        } else if (millis < 1000) {
            return millis + "毫秒";
        } else if (millis < 60000) {
            double seconds = millis / 1000.0;
            DecimalFormat df = new DecimalFormat("0.0");
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df.format(seconds) + "秒";
        } else if (millis < 3600000) {
            double minutes = millis / 60000.0;
            DecimalFormat df = new DecimalFormat("0.0");
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df.format(minutes) + "分";
        } else if (millis < 86400000) {
            double hours = millis / 3600000.0;
            DecimalFormat df = new DecimalFormat("0.0");
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df.format(hours) + "时";
        } else {
            double days = millis / 86400000.0;
            DecimalFormat df = new DecimalFormat("0.0");
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df.format(days) + "天";
        }
    }

}
