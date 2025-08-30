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
package com.iofairy.rainforest.jackson.deser;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.iofairy.time.DateTime;

import java.io.IOException;
import java.time.DateTimeException;

/**
 * {@link DateTime} 作为Json键时的<b>反序列化</b>操作
 * <br>
 * <b>让此反序列化器快捷生效的方法:</b>
 * <blockquote><pre>{@code
 * import com.iofairy.rainforest.json.module.JacksonModules;
 *
 * ObjectMapper mapper = new ObjectMapper();
 * JacksonModules.registerModules(mapper);
 * }</pre></blockquote>
 *
 * @since 0.6.0
 */
public class DateTimeKeyDeserializer extends KeyDeserializer {

    public static final DateTimeKeyDeserializer INSTANCE = new DateTimeKeyDeserializer();

    private DateTimeKeyDeserializer() {
        // singleton
    }

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        if ("".equals(key)) return null;

        return deserialize(key, ctxt);
    }

    protected DateTime deserialize(String key, DeserializationContext ctxt) throws IOException {
        try {
            return DateTime.parse(key);
        } catch (DateTimeException e) {
            return _handleDateTimeException(ctxt, DateTime.class, e, key);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T _handleDateTimeException(DeserializationContext ctxt, Class<?> type, DateTimeException e0, String value) throws IOException {
        try {
            return (T) ctxt.handleWeirdKey(type, value, "Failed to deserialize %s: (%s) %s",
                    type.getName(), e0.getClass().getName(), e0.getMessage());

        } catch (JsonMappingException e) {
            e.initCause(e0);
            throw e;
        } catch (IOException e) {
            if (null == e.getCause()) {
                e.initCause(e0);
            }
            throw JsonMappingException.fromUnexpectedIOE(e);
        }
    }

}
