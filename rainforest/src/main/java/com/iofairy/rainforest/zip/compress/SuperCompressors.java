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
package com.iofairy.rainforest.zip.compress;

import com.iofairy.falcon.zip.ArchiveFormat;

import java.util.*;

import static com.iofairy.falcon.zip.ArchiveFormat.*;

/**
 * 接口SuperCompressor工具类
 *
 * @since 0.2.0
 */
public class SuperCompressors {
    private static final char[] LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] NUMBERS = "0123456789".toCharArray();

    public static String getUnzipId(int length) {
        if (length < 2) throw new IllegalArgumentException("参数`length`必须 >= 2！");

        final Random random = new Random();
        char[] result = new char[length];
        // 第一位是字母
        result[0] = LETTERS[random.nextInt(LETTERS.length)];
        // 第二位是字母
        result[1] = LETTERS[random.nextInt(LETTERS.length)];

        // 后面都是数字
        for (int i = 2; i < length; i++) {
            result[i] = NUMBERS[random.nextInt(NUMBERS.length)];
        }

        return new String(result);
    }

    static Map<ArchiveFormat, SuperCompressor> toCompressorMap(List<SuperCompressor> compressors) {
        Map<ArchiveFormat, SuperCompressor> compressorMap = new HashMap<>();
        for (SuperCompressor compressor : compressors) {
            if (compressor == null) continue;
            compressorMap.put(compressor.format(), compressor);
        }
        fillMap(compressorMap);
        return Collections.unmodifiableMap(compressorMap);
    }

    static Map<ArchiveFormat, SuperCompressor> toCompressorMap(Map<ArchiveFormat, SuperCompressor> map) {
        Map<ArchiveFormat, SuperCompressor> compressorMap = new HashMap<>(map);
        fillMap(compressorMap);
        return Collections.unmodifiableMap(compressorMap);
    }

    private static void fillMap(Map<ArchiveFormat, SuperCompressor> compressorMap) {
        fillMap(compressorMap, TAR_GZ, TGZ);
        fillMap(compressorMap, TAR_BZ2, TBZ2);
        fillMap(compressorMap, TAR_LZ, TLZ);
        fillMap(compressorMap, TAR_XZ, TXZ);
        fillMap(compressorMap, TAR_ZST, TZST);
    }

    private static void fillMap(Map<ArchiveFormat, SuperCompressor> compressorMap, ArchiveFormat format1, ArchiveFormat format2) {
        if (compressorMap.containsKey(format1) && !compressorMap.containsKey(format2)) compressorMap.put(format2, compressorMap.get(format1));
        else if (compressorMap.containsKey(format2) && !compressorMap.containsKey(format1)) compressorMap.put(format1, compressorMap.get(format2));
    }

}
