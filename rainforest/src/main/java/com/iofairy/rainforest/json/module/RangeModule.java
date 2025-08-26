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
package com.iofairy.rainforest.json.module;

import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.iofairy.rainforest.json.deser.RangeDeserializer;
import com.iofairy.rainforest.json.ser.RangeSerializer;
import com.iofairy.range.Range;

import java.time.format.DateTimeFormatter;

/**
 * 注册 {@code Range} Json序列化/反序列化模块
 *
 * @since 0.6.0
 */
public class RangeModule extends CommonModule {

    protected static final String MODULE_NAME = "range-module";

    public RangeModule() {
        super(getVersion(MODULE_NAME));
    }

    public RangeModule(DateTimeFormatter serFormatter, DateTimeFormatter deserFormatter) {
        super(getVersion(MODULE_NAME), serFormatter, deserFormatter);
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);

        SimpleDeserializers desers = new SimpleDeserializers();
        desers.addDeserializer(Range.class, new RangeDeserializer(_deserFformatter));
        context.addDeserializers(desers);
        if (_deserializers != null) {
            context.addDeserializers(_deserializers);
        }

        SimpleSerializers sers = new SimpleSerializers();
        sers.addSerializer(Range.class, new RangeSerializer(_serFormatter));
        context.addSerializers(sers);
        if (_serializers != null) {
            context.addSerializers(_serializers);
        }
    }

}
