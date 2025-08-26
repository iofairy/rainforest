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
package com.iofairy.rainforest.json.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.iofairy.time.DateTime;
import com.iofairy.time.DateTimes;

import java.io.IOException;
import java.time.format.DateTimeFormatter;


/**
 * {@link DateTime} 序列化器
 *
 * @since 0.6.0
 */
public class DatetimeSerializer extends DatetimeBaseSerializer<DateTime> implements ContextualSerializer {
    private static final long serialVersionUID = 1L;

    public static final DatetimeSerializer INSTANCE = new DatetimeSerializer();

    protected DatetimeSerializer() {
        this(DateTimes.STD_DTF);
    }

    public DatetimeSerializer(DateTimeFormatter formatter) {
        super(DateTime.class, formatter);
    }

    public DatetimeSerializer(String pattern) {
        super(DateTime.class, pattern);
    }

    protected DatetimeSerializer(Class<DateTime> clazz) {
        super(clazz);
    }

    protected DatetimeSerializer(DatetimeSerializer base, Boolean writeZoneId) {
        this(base, base._formatter, base._currentType, base._useTimestamp, writeZoneId, base._shape);
    }

    protected DatetimeSerializer(DatetimeSerializer base, JavaType currentType) {
        this(base, base._formatter, currentType, base._useTimestamp, base._writeZoneId, base._shape);
    }

    protected DatetimeSerializer(DatetimeSerializer base, DateTimeFormatter formatter, Boolean useTimestamp, JsonFormat.Shape shape) {
        this(base, formatter, base._currentType, useTimestamp, base._writeZoneId, shape);
    }

    protected DatetimeSerializer(DatetimeSerializer base,
                                 DateTimeFormatter formatter,
                                 JavaType currentType,
                                 Boolean useTimestamp,
                                 Boolean writeZoneId,
                                 JsonFormat.Shape shape) {
        super(base, formatter, currentType, useTimestamp, writeZoneId, shape);
    }

    @Override
    protected DatetimeBaseSerializer<?> withFormat(DateTimeFormatter formatter, Boolean useTimestamp, JsonFormat.Shape shape) {
        return new DatetimeSerializer(this, formatter, useTimestamp, shape);
    }

    @Override
    protected DatetimeBaseSerializer<?> withJavaType(JavaType javaType) {
        return new DatetimeSerializer(this, javaType);
    }

    @Override
    protected DatetimeBaseSerializer<?> withFeatures(Boolean writeZoneId) {
        return new DatetimeSerializer(this, writeZoneId);
    }

    @Override
    public void serialize(DateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        if (!useTimestamp(provider)) {
            if ((_formatter != null) && (_shape == JsonFormat.Shape.STRING)) {

            } else if (shouldWriteWithZoneId(provider)) {
                DateTimeFormatter formatter = _formatter == null ? DateTimes.STD_DTF : _formatter;
                gen.writeString(DateTimes.withTimeZone(formatter).format(value));
                return;
            }
        }

        if (useTimestamp(provider)) {
            gen.writeNumber(value.toEpochMilli());
            return;
        }

        gen.writeString(formatValue(value, provider));
    }

    @Override
    public void serializeWithType(DateTime value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, serializationShape(provider)));
        serialize(value, g, provider);
        typeSer.writeTypeSuffix(g, typeIdDef);
    }

    protected JsonToken serializationShape(SerializerProvider provider) {
        if (useTimestamp(provider)) {
            return JsonToken.VALUE_NUMBER_INT;
        }
        return JsonToken.VALUE_STRING;
    }

}
