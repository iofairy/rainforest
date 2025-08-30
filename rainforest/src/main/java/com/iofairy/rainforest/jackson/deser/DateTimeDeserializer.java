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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.iofairy.time.DateTime;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * {@link DateTime} 反序列化器
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
public class DateTimeDeserializer extends DateTimeBaseDeserializer<DateTime> implements ContextualDeserializer {
    private static final long serialVersionUID = 1L;

    public static final DateTimeDeserializer INSTANCE = new DateTimeDeserializer();


    protected DateTimeDeserializer() {
        this(DTF);
    }

    public DateTimeDeserializer(DateTimeFormatter formatter) {
        super(DateTime.class, formatter);
    }

    public DateTimeDeserializer(String pattern) {
        super(DateTime.class, pattern);
    }

    protected DateTimeDeserializer(Class<DateTime> clazz) {
        super(clazz);
    }

    protected DateTimeDeserializer(DateTimeDeserializer base, JavaType currentType) {
        this(base, base._formatter, currentType, base._useTimestamp, base._writeZoneId, base._shape, base._leniency);
    }

    protected DateTimeDeserializer(DateTimeDeserializer base, JsonFormat.Shape shape) {
        this(base, base._formatter, base._currentType, base._useTimestamp, base._writeZoneId, shape, base._leniency);
    }

    protected DateTimeDeserializer(DateTimeDeserializer base, Boolean leniency) {
        this(base, base._formatter, base._currentType, base._useTimestamp, base._writeZoneId, base._shape, leniency);
    }

    protected DateTimeDeserializer(DateTimeDeserializer base, DateTimeFormatter formatter, Boolean useTimestamp) {
        this(base, formatter, base._currentType, useTimestamp, base._writeZoneId, base._shape, base._leniency);
    }

    protected DateTimeDeserializer(DateTimeDeserializer base,
                                   DateTimeFormatter formatter,
                                   JavaType currentType,
                                   Boolean useTimestamp,
                                   Boolean writeZoneId,
                                   JsonFormat.Shape shape,
                                   Boolean leniency) {
        super(base, formatter, currentType, useTimestamp, writeZoneId, shape, leniency);
    }

    protected DateTimeBaseDeserializer<?> withFormat(DateTimeFormatter formatter, Boolean useTimestamp) {
        return new DateTimeDeserializer(this, formatter, useTimestamp);
    }

    protected DateTimeBaseDeserializer<?> withJavaType(JavaType javaType) {
        return new DateTimeDeserializer(this, javaType);
    }

    protected DateTimeBaseDeserializer<?> withShape(JsonFormat.Shape shape) {
        return new DateTimeDeserializer(this, shape);
    }

    protected DateTimeBaseDeserializer<?> withLeniency(Boolean leniency) {
        return new DateTimeDeserializer(this, leniency);
    }


    @Override
    public DateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {

        if (parser.hasTokenId(JsonTokenId.ID_STRING)) {
            return _fromString(parser, context, parser.getText());
        }

        if (parser.isExpectedStartObjectToken()) {
            return _fromString(parser, context, context.extractScalarFromObject(parser, this, handledType()));
        }

        if (parser.hasToken(JsonToken.VALUE_EMBEDDED_OBJECT)) {
            return (DateTime) parser.getEmbeddedObject();
        }

        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return DateTime.of(parser.getLongValue()).withZoneSameInstant(_formatter.getZone());
        }

        return _handleUnexpectedToken(context, parser, "Expected string.");
    }

}
