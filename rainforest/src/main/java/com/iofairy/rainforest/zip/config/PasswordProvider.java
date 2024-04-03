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

import com.iofairy.top.G;
import com.iofairy.top.S;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * 压缩包密码提供者，根据文件名获取密码
 *
 * @since 0.1.0
 */
public class PasswordProvider {
    private final Map<String, ZipPassword> zipPasswordMap = new ConcurrentHashMap<>();
    private List<ZipPassword> zipPasswordList = new ArrayList<>();

    /**
     * 预留的备用密码（输入流获取不到名称时使用）
     *
     * @deprecated 从版本 0.5.0 开始，不再使用预留密码，如果设置这个，可能会导致原本压缩包就是不带密码，但是使用此密码设置成压缩包密码
     */
    @Deprecated
    @Getter
    @Setter
    @Accessors(chain = true)
    private char[] reservedPassword;

    /**
     * 获取 PasswordProvider
     *
     * @param reservedPassword 预留密码
     * @param zipPasswords     压缩包密码
     * @return PasswordProvider
     * @deprecated 从版本 0.5.0 开始，不再使用预留密码。请使用 {@link #of(ZipPassword...)} 构造方法
     */
    @Deprecated
    public static PasswordProvider of(char[] reservedPassword, ZipPassword... zipPasswords) {
        PasswordProvider passwordProvider = new PasswordProvider();
        passwordProvider.reservedPassword = reservedPassword;
        return passwordProvider.addPasswords(zipPasswords);
    }

    public static PasswordProvider of(ZipPassword... zipPasswords) {
        return of(null, zipPasswords);
    }

    /**
     * 根据文件名获取密码
     *
     * @param fileName 文件名
     * @return 返回密码。如果未找到匹配的文件名，则返回 {@code null}
     */
    public synchronized char[] getPassword(String fileName) {
        if (S.isEmpty(fileName)) return null;

        for (ZipPassword password : zipPasswordList) {
            Matcher matcher = password.getPattern().matcher(fileName);
            if (matcher.matches()) return password.getPassword();
        }
        return null;
    }

    public synchronized PasswordProvider addPassword(ZipPassword zipPassword) {
        if (zipPassword != null) {
            zipPasswordMap.put(zipPassword.getFileName(), zipPassword);
            sortPasswordPattern();
        }

        return this;
    }

    public synchronized PasswordProvider addPasswords(ZipPassword... zipPasswords) {
        if (!G.isEmpty(zipPasswords)) {
            for (ZipPassword zipPassword : zipPasswords) {
                if (zipPassword == null) continue;
                zipPasswordMap.put(zipPassword.getFileName(), zipPassword);
            }
            sortPasswordPattern();
        }

        return this;
    }

    public synchronized PasswordProvider addPasswords(List<ZipPassword> zipPasswords) {
        if (!G.isEmpty(zipPasswords)) {
            for (ZipPassword zipPassword : zipPasswords) {
                if (zipPassword == null) continue;
                zipPasswordMap.put(zipPassword.getFileName(), zipPassword);
            }
            sortPasswordPattern();
        }
        return this;
    }

    /**
     * 按文件名对密码排序<br>
     * 排序规则如下：<br>
     * 1、文件名不包含通配符，排最前面（排前面意味着比较小），都不包含通配符，视为相等<br>
     * 2、都包含通配符，则长度长的排前面<br>
     * 3、都包含通配符且长度相等，则其他字符串排在 ? 和 * 前面， ? 号排在 * 号前面
     */
    private void sortPasswordPattern() {
        Collection<ZipPassword> zipPasswords = zipPasswordMap.values();
        zipPasswordList = zipPasswords.stream().sorted((zipPassword1, zipPassword2) -> {
            String fileName1 = zipPassword1.getFileName();
            String fileName2 = zipPassword2.getFileName();

            if (fileName1.equals(fileName2)) return 0;

            boolean hasWildcard1 = fileName1.contains("*") || fileName1.contains("?");
            boolean hasWildcard2 = fileName2.contains("*") || fileName2.contains("?");

            if (!hasWildcard1 && !hasWildcard2) return 0;   // 都不包含通配符，则不排序
            if (!hasWildcard1) return -1;
            if (!hasWildcard2) return 1;

            if (fileName1.length() != fileName2.length()) return fileName2.length() - fileName1.length();

            // fileName1.length() 等于 fileName2.length()
            for (int i = 0; i < fileName1.length(); i++) {
                char c1 = fileName1.charAt(i);
                char c2 = fileName2.charAt(i);

                if (c1 == c2) continue;                                         // 相等，继续比较下一个字符
                else if (c1 == '*' || c2 == '*') return c1 == '*' ? 1 : -1;     // 有一个是 *，另一个不是，* 比较大（排后面）
                else if (c1 == '?' || c2 == '?') return c1 == '?' ? 1 : -1;     // 有一个是 ?，另一个不是，? 比较大（排后面）
                else return 0;                                                  // 其他情况，直接视为相等
            }
            return 0;
        }).collect(Collectors.toList());
    }

    public Map<String, ZipPassword> getZipPasswordMap() {
        return Collections.unmodifiableMap(zipPasswordMap);
    }

    public List<ZipPassword> getZipPasswordList() {
        return Collections.unmodifiableList(zipPasswordList);
    }

}
