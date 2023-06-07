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

import com.iofairy.falcon.zip.ArchiveFormat;
import com.iofairy.top.G;
import com.iofairy.tuple.Tuple;
import com.iofairy.tuple.Tuple2;

import java.io.InputStream;
import java.util.*;

import static com.iofairy.falcon.zip.ArchiveFormat.*;

/**
 * 接口 {@link SuperAC}工具类
 *
 * @since 0.2.0
 */
public class SuperACs {
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

    static Tuple2<Map<ArchiveFormat, SuperAC>, SuperAC> checkParameters(InputStream is, ArchiveFormat inputStreamType, List<SuperAC> superACs) {
        if (G.hasNull(is, inputStreamType)) throw new NullPointerException("参数`is`或`inputStreamType`不能为null！");
        if (G.isEmpty(superACs)) throw new NullPointerException("参数`superACs`不能为null或空！");

        Map<ArchiveFormat, SuperAC> superACMap = toSuperACMap(superACs);

        SuperAC superAC = superACMap.get(inputStreamType);
        if (superAC == null) throw new IllegalArgumentException("在参数`superACs`中未找到与`inputStreamType`相匹配 superAC 对象！");

        return Tuple.of(superACMap, superAC);
    }

    public static Map<ArchiveFormat, SuperAC> toSuperACMap(List<SuperAC> superACs) {
        Map<ArchiveFormat, SuperAC> superACMap = new HashMap<>();
        for (SuperAC superAC : superACs) {
            if (superAC == null) continue;
            superACMap.put(superAC.format(), superAC);
        }
        fillMap(superACMap);
        return Collections.unmodifiableMap(superACMap);
    }

    public static Map<ArchiveFormat, SuperAC> toSuperACMap(Map<ArchiveFormat, SuperAC> map) {
        Map<ArchiveFormat, SuperAC> superACMap = new HashMap<>(map);
        fillMap(superACMap);
        return Collections.unmodifiableMap(superACMap);
    }

    private static void fillMap(Map<ArchiveFormat, SuperAC> superACMap) {
        fillMap(superACMap, TAR_GZ, TGZ);
        fillMap(superACMap, TAR_BZ2, TBZ2);
        fillMap(superACMap, TAR_LZ, TLZ);
        fillMap(superACMap, TAR_XZ, TXZ);
        fillMap(superACMap, TAR_ZST, TZST);
    }

    private static void fillMap(Map<ArchiveFormat, SuperAC> superACMap, ArchiveFormat format1, ArchiveFormat format2) {
        if (superACMap.containsKey(format1) && !superACMap.containsKey(format2)) superACMap.put(format2, superACMap.get(format1));
        else if (superACMap.containsKey(format2) && !superACMap.containsKey(format1)) superACMap.put(format1, superACMap.get(format2));
    }

}
