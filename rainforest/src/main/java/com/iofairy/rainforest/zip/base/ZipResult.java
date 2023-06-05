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

import java.util.List;

/**
 * 解压并重压缩后返回的结果
 *
 * @since 0.2.0
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ZipResult<T> {
    byte[][] bytes;
    List<T> results;

    public static <R> ZipResult<R> of(final byte[][] bytes, final List<R> results) {
        return new ZipResult<>(bytes, results);
    }

    @Override
    public String toString() {
        return "ReZipResult{" +
                "results=" + results +
                '}';
    }
}
