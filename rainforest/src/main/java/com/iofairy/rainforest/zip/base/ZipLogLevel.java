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

/**
 * 用于SuperCompressor的日志等级
 *
 * @since 0.2.0
 */
public enum ZipLogLevel {
    NONE(0),
    BRIEF(1),
    DETAIL(2),
    ALL(3);

    public final int level;

    ZipLogLevel(int level) {
        this.level = level;
    }

}
