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

import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.LocalDateTimeKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.OffsetDateTimeKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.ZonedDateTimeKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.key.ZonedDateTimeKeySerializer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 注册 Java8时间类 Json序列化/反序列化模块
 *
 * @since 0.6.0
 */
public class JSR310TimeModule extends CommonModule {

    protected static final String MODULE_NAME = JSR310TimeModule.class.getName();

    public JSR310TimeModule() {
        super(getVersion(MODULE_NAME));
    }

    public JSR310TimeModule(DateTimeFormatter serFormatter, DateTimeFormatter deserFormatter) {
        super(getVersion(MODULE_NAME), serFormatter, deserFormatter);
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);

        SimpleDeserializers desers = new SimpleDeserializers();
        desers.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(_deserFformatter));
        context.addDeserializers(desers);
        if (_deserializers != null) {
            context.addDeserializers(_deserializers);
        }

        SimpleSerializers sers = new SimpleSerializers();
        sers.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(_serFormatter));
        sers.addSerializer(OffsetDateTime.class, OffsetDateTimeSerializer.INSTANCE);
        sers.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(_serFormatter));
        context.addSerializers(sers);
        if (_serializers != null) {
            context.addSerializers(_serializers);
        }

        SimpleSerializers keySers = new SimpleSerializers();
        keySers.addSerializer(ZonedDateTime.class, ZonedDateTimeKeySerializer.INSTANCE);
        context.addKeySerializers(keySers);
        if (_keySerializers != null) {
            context.addKeySerializers(_keySerializers);
        }

        SimpleKeyDeserializers keyDesers = new SimpleKeyDeserializers();
        keyDesers.addDeserializer(LocalDateTime.class, LocalDateTimeKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(OffsetDateTime.class, OffsetDateTimeKeyDeserializer.INSTANCE);
        keyDesers.addDeserializer(ZonedDateTime.class, ZonedDateTimeKeyDeserializer.INSTANCE);
        context.addKeyDeserializers(keyDesers);
        if (_keyDeserializers != null) {
            context.addKeyDeserializers(_keyDeserializers);
        }

    }


}
