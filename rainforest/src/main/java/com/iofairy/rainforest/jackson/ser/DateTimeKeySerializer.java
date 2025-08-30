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
package com.iofairy.rainforest.jackson.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.iofairy.time.DateTime;

import java.io.IOException;

/**
 * {@link DateTime} 作为Json键时的<b>序列化</b>操作
 * <br>
 * <b>让此序列化器快捷生效的方法:</b>
 * <blockquote><pre>{@code
 * import com.iofairy.rainforest.json.module.JacksonModules;
 *
 * ObjectMapper mapper = new ObjectMapper();
 * JacksonModules.registerModules(mapper);
 * }</pre></blockquote>
 *
 * @since 0.6.0
 */
public class DateTimeKeySerializer extends JsonSerializer<DateTime> {

    public static final DateTimeKeySerializer INSTANCE = new DateTimeKeySerializer();

    private DateTimeKeySerializer() {
        // singleton
    }

    @Override
    public void serialize(DateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        if (serializers.isEnabled(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)) {
            gen.writeFieldName(value.dtDetail());
        } else if (useTimestamps(serializers)) {
            gen.writeFieldName(String.valueOf(value.toEpochMilli()));
        } else {
            gen.writeFieldName(value.formatYMDHMS());
        }
    }

    private static boolean useTimestamps(SerializerProvider serializers) {
        return serializers.isEnabled(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
    }

}
