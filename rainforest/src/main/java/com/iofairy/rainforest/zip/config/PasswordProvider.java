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
     * 初始化密码（输入流获取不到名称时使用）
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private char[] initializedPassword;

    public static PasswordProvider of(char[] initializedPassword, ZipPassword... zipPasswords) {
        PasswordProvider passwordProvider = new PasswordProvider();
        passwordProvider.initializedPassword = initializedPassword;
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

    private void sortPasswordPattern() {
        Collection<ZipPassword> zipPasswords = zipPasswordMap.values();
        zipPasswordList = zipPasswords.stream().sorted((zipPassword1, zipPassword2) -> {
            String fileName1 = zipPassword1.getFileName();
            String fileName2 = zipPassword2.getFileName();
            boolean hasWildcard1 = fileName1.contains("*") || fileName1.contains("?");
            boolean hasWildcard2 = fileName2.contains("*") || fileName2.contains("?");

            if (!hasWildcard1 && !hasWildcard2) return 0;   // 都不包含通配符，则不排序
            if (!hasWildcard1) return -1;
            if (!hasWildcard2) return 1;

            if (fileName1.length() != fileName2.length()) return fileName2.length() - fileName1.length();

            return fileName2.compareTo(fileName1);          // 相同长度下，? 排 * 前面
        }).collect(Collectors.toList());
    }

    public Map<String, ZipPassword> getZipPasswordMap() {
        return Collections.unmodifiableMap(zipPasswordMap);
    }

    public List<ZipPassword> getZipPasswordList() {
        return Collections.unmodifiableList(zipPasswordList);
    }

}
