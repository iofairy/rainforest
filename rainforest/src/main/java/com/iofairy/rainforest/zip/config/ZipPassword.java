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
package com.iofairy.rainforest.zip.config;

import com.iofairy.top.S;
import lombok.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 压缩包密码
 *
 * @since 0.1.0
 */
@Getter
@ToString(exclude = {"password"})
public class ZipPassword {
    /**
     * 带通配符的文件名，通配符仅支持：
     * <ul>
     *     <li> <b>{@code *} ：匹配0或多个任意字符</b>
     *     <li> <b>{@code ?} ：匹配任意1个字符</b>
     * </ul>
     */
    private String fileName;
    /**
     * 文件名匹配
     */
    private Pattern pattern;
    /**
     * 密码
     */
    private char[] password;

    private static final String REPLACEMENT = Matcher.quoteReplacement("\\$");

    public ZipPassword(String password) {
        this(null, password);
    }

    public ZipPassword(String fileName, String password) {
        this.pattern = Pattern.compile(S.isEmpty(fileName) ? ".*" : getRegex(fileName));
        this.fileName = S.isEmpty(fileName) ? "*" : fileName.replaceAll("(\\*)+", "*");
        this.password = password == null ? null : password.toCharArray();
    }


    public static ZipPassword of(String password) {
        return new ZipPassword(null, password);
    }

    public static ZipPassword of(String fileName, String password) {
        return new ZipPassword(fileName, password);
    }

    public ZipPassword setFileName(String fileName) {
        this.pattern = Pattern.compile(S.isEmpty(fileName) ? ".*" : getRegex(fileName));
        this.fileName = S.isEmpty(fileName) ? "*" : fileName.replaceAll("(\\*)+", "*");
        return this;
    }

    public ZipPassword setPassword(String password) {
        this.password = password == null ? null : password.toCharArray();
        return this;
    }

    private static String getRegex(String fileName) {

        String regex = fileName.replaceAll("\\-", "\\\\-")
                .replaceAll("\\[", "\\\\[")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)")
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\+", "\\\\+")
                .replaceAll("\\^", "\\\\^")
                .replaceAll("\\$", REPLACEMENT)
                .replaceAll("(\\*)+", ".*")
                .replaceAll("\\?", ".");
        return regex;
    }


}


