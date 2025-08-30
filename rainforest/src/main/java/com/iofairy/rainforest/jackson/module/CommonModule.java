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
package com.iofairy.rainforest.jackson.module;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.format.DateTimeFormatter;

/**
 * 常用模块
 *
 * @since 0.6.0
 */
public class CommonModule extends SimpleModule {
    protected static final String VERSION = "0.6.0";
    protected static final String GROUP_ID = "com.iofairy";

    protected static final DateTimeFormatter SER_DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    protected static final DateTimeFormatter DESER_DTF = DateTimeFormatter.ofPattern("y-M-d H:m:s");
    protected final DateTimeFormatter _serFormatter;
    protected final DateTimeFormatter _deserFformatter;

    public CommonModule(Version version) {
        super(version);
        _serFormatter = SER_DTF;
        _deserFformatter = DESER_DTF;
    }

    public CommonModule(Version version, DateTimeFormatter serFormatter, DateTimeFormatter deserFormatter) {
        super(version);
        _serFormatter = serFormatter == null ? SER_DTF : serFormatter;
        _deserFformatter = deserFormatter == null ? SER_DTF : deserFormatter;
    }

    public static Version getVersion(String moduleName) {
        return VersionUtil.parseVersion(VERSION, GROUP_ID, moduleName);
    }

}
