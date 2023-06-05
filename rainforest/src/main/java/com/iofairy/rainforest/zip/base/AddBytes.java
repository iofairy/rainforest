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
package com.iofairy.rainforest.zip.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 需要添加进压缩包的字节数组
 *
 * @since 0.2.0
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class AddBytes {
    /**
     * 要添加的文件字节数组（如果 {@link #isDirectory} 为 {@code true}），bytes 可为 {@code null}
     */
    byte[][] bytes;
    /**
     * 添加进压缩包的名称
     */
    String entryFileName;
    /**
     * 是否是目录
     */
    boolean isDirectory;

    public static AddBytes of(final byte[][] bytes, final String entryFileName, final boolean isDirectory) {
        return new AddBytes(bytes, entryFileName, isDirectory);
    }

    @Override
    public String toString() {
        return "AddBytes{" +
                "zipEntryName='" + entryFileName + '\'' +
                ", isDirectory=" + isDirectory +
                '}';
    }
}
