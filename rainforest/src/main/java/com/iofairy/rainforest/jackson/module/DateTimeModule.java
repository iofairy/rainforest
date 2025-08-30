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
import com.iofairy.time.DateTime;
import com.iofairy.rainforest.jackson.deser.DateTimeDeserializer;
import com.iofairy.rainforest.jackson.deser.DateTimeKeyDeserializer;
import com.iofairy.rainforest.jackson.ser.DateTimeKeySerializer;
import com.iofairy.rainforest.jackson.ser.DatetimeSerializer;

import java.time.format.DateTimeFormatter;

/**
 * 注册 {@code DateTime} Json序列化/反序列化模块
 *
 * @since 0.6.0
 */
public class DateTimeModule extends CommonModule {

    protected static final String MODULE_NAME = DateTimeModule.class.getName();

    public DateTimeModule() {
        super(getVersion(MODULE_NAME));
    }

    public DateTimeModule(DateTimeFormatter serFormatter, DateTimeFormatter deserFormatter) {
        super(getVersion(MODULE_NAME), serFormatter, deserFormatter);
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);

        SimpleDeserializers desers = new SimpleDeserializers();
        desers.addDeserializer(DateTime.class, new DateTimeDeserializer(_deserFformatter));
        context.addDeserializers(desers);
        if (_deserializers != null) {
            context.addDeserializers(_deserializers);
        }

        SimpleSerializers sers = new SimpleSerializers();
        sers.addSerializer(DateTime.class, new DatetimeSerializer(_serFormatter));
        context.addSerializers(sers);
        if (_serializers != null) {
            context.addSerializers(_serializers);
        }

        SimpleSerializers keySers = new SimpleSerializers();
        keySers.addSerializer(DateTime.class, DateTimeKeySerializer.INSTANCE);
        context.addKeySerializers(keySers);
        if (_keySerializers != null) {
            context.addKeySerializers(_keySerializers);
        }

        SimpleKeyDeserializers keyDesers = new SimpleKeyDeserializers();
        keyDesers.addDeserializer(DateTime.class, DateTimeKeyDeserializer.INSTANCE);
        context.addKeyDeserializers(keyDesers);
        if (_keyDeserializers != null) {
            context.addKeyDeserializers(_keyDeserializers);
        }

    }


}
