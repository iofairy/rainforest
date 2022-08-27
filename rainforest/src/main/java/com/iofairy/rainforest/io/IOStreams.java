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
package com.iofairy.rainforest.io;

import com.iofairy.falcon.io.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * IO流工具类
 *
 * @since 0.0.1
 */
public class IOStreams {
    /**
     * 将 {@code InputStream} 转为 {@code MultiByteArrayInputStream}
     *
     * @param inputStream 输入流
     * @return MultiByteArrayInputStream
     * @throws IOException io异常
     */
    public static MultiByteArrayInputStream transferTo(InputStream inputStream) throws IOException {
        MultiByteArrayOutputStream multiBaos = new MultiByteArrayOutputStream();
        IOUtils.copy(inputStream, multiBaos);
        return new MultiByteArrayInputStream(multiBaos.toByteArrays());
    }

}
