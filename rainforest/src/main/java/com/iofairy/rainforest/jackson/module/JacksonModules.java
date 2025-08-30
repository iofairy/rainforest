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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iofairy.top.S;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * module工具类
 *
 * @since 0.6.0
 */
public class JacksonModules {

    public static void registerModules(ObjectMapper objectMapper, String serPattern, String deserPattern, ZoneId zoneId) {
        DateFormat dateFormat = objectMapper.getDateFormat();
        if (dateFormat instanceof SimpleDateFormat) {
            serPattern = S.isBlank(serPattern) ? ((SimpleDateFormat) dateFormat).toPattern() : serPattern;
            deserPattern = S.isBlank(deserPattern) ? serPattern : deserPattern;
        }

        DateTimeFormatter serFormatter = DateTimeFormatter.ofPattern(S.isBlank(serPattern) ? "yyyy-MM-dd HH:mm:ss" : serPattern);
        if (zoneId != null) {
            serFormatter = serFormatter.withZone(zoneId);
        }
        DateTimeFormatter deserFormatter = DateTimeFormatter.ofPattern(S.isBlank(deserPattern) ? "y-M-d H:m:s" : deserPattern);
        if (zoneId != null) {
            deserFormatter = deserFormatter.withZone(zoneId);
        }

        registerModules(objectMapper, serFormatter, deserFormatter);
    }

    public static void registerModules(ObjectMapper objectMapper) {
        registerModules(objectMapper, null, null, null);
    }

    public static void registerModules(ObjectMapper objectMapper, ZoneId zoneId) {
        registerModules(objectMapper, null, null, zoneId);
    }

    public static void registerModules(ObjectMapper objectMapper, DateTimeFormatter serFormatter, DateTimeFormatter deserFormatter) {
        objectMapper.registerModules(
                new DateTimeModule(serFormatter, deserFormatter),
                new RangeModule(serFormatter, deserFormatter),
                new JSR310TimeModule(serFormatter, deserFormatter)
        );
    }

}
