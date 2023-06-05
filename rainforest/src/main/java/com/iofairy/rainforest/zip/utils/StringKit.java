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


import java.util.Objects;

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
        Objects.requireNonNull(str, "Parameter `str` must be non-null!");
        if (repeatTimes <= 0) return "";

        return new String(new char[repeatTimes]).replace("\0", str);
    }
}
