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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.iofairy.time.DateTime;
import com.iofairy.time.DateTimes;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE;


/**
 * {@link DateTime} Json序列化基类
 *
 * @since 0.6.0
 */
public abstract class DatetimeBaseSerializer<T> extends StdSerializer<T> implements ContextualSerializer {
    private static final long serialVersionUID = 1L;

    protected final DateTimeFormatter _formatter;
    protected final JavaType _currentType;
    protected final Boolean _useTimestamp;
    protected final Boolean _writeZoneId;
    protected final JsonFormat.Shape _shape;

    protected DatetimeBaseSerializer(Class<T> clazz, DateTimeFormatter formatter) {
        super(clazz);
        _formatter = formatter;
        _currentType = null;
        _useTimestamp = null;
        _writeZoneId = null;
        _shape = null;
    }

    protected DatetimeBaseSerializer(Class<T> clazz, String pattern) {
        super(clazz);
        _formatter = DateTimeFormatter.ofPattern(pattern);
        _currentType = null;
        _useTimestamp = null;
        _writeZoneId = null;
        _shape = null;
    }

    protected DatetimeBaseSerializer(Class<T> clazz) {
        super(clazz);
        _formatter = DateTimes.DTF_STD;
        _currentType = null;
        _useTimestamp = null;
        _writeZoneId = null;
        _shape = null;
    }


    protected DatetimeBaseSerializer(DatetimeBaseSerializer<T> base,
                                     DateTimeFormatter formatter,
                                     JavaType currentType,
                                     Boolean useTimestamp,
                                     Boolean writeZoneId,
                                     JsonFormat.Shape shape) {
        super(base.handledType());
        _formatter = formatter;
        _currentType = currentType;
        _useTimestamp = useTimestamp != null && useTimestamp;
        _writeZoneId = writeZoneId != null && writeZoneId;
        _shape = shape;
    }


    protected abstract DatetimeBaseSerializer<?> withFormat(DateTimeFormatter formatter,
                                                            Boolean useTimestamp,
                                                            JsonFormat.Shape shape);

    protected abstract DatetimeBaseSerializer<?> withJavaType(JavaType javaType);

    protected DatetimeBaseSerializer<?> withFeatures(Boolean writeZoneId) {
        return this;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        DatetimeBaseSerializer<?> ser = this;

        JavaType currentType = null;
        if (property != null) {
            currentType = property.getType();
            ser = ser.withJavaType(currentType);
        }

        JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
        if (format != null) {
            Boolean useTimestamp;

            JsonFormat.Shape shape = format.getShape();
            if (shape == JsonFormat.Shape.ARRAY || shape.isNumeric()) {
                useTimestamp = Boolean.TRUE;
            } else {
                useTimestamp = (shape == JsonFormat.Shape.STRING) ? Boolean.FALSE : null;
            }

            DateTimeFormatter dtf = _formatter;

            if (format.hasPattern()) {
                dtf = _useDateTimeFormatter(prov, format);
            }

            if ((shape != _shape) || (useTimestamp != _useTimestamp) || (dtf != _formatter)) {
                ser = ser.withFormat(dtf, useTimestamp, shape);
            }

            Boolean writeZoneId = format.getFeature(JsonFormat.Feature.WRITE_DATES_WITH_ZONE_ID);
            if (writeZoneId != null) {
                ser = ser.withFeatures(writeZoneId);
            }
        }

        return ser;
    }

    protected DateTimeFormatter _useDateTimeFormatter(SerializerProvider prov, JsonFormat.Value format) {
        DateTimeFormatter dtf;
        final String pattern = format.getPattern();
        final Locale locale = format.hasLocale() ? format.getLocale() : prov.getLocale();
        if (locale == null) {
            dtf = DateTimeFormatter.ofPattern(pattern);
        } else {
            dtf = DateTimeFormatter.ofPattern(pattern, locale);
        }

        if (format.hasTimeZone()) {
            dtf = dtf.withZone(format.getTimeZone().toZoneId());
        }
        return dtf;
    }

    protected String formatValue(DateTime value, SerializerProvider provider) {
        DateTimeFormatter formatter = getFormatter(_formatter, provider);

        if (_shape == JsonFormat.Shape.STRING) {
            if (Boolean.TRUE.equals(_writeZoneId)) {
                formatter = DateTimes.withTimeZone(formatter);
            }
        }
        return value.format(formatter);
    }

    protected DateTimeFormatter getFormatter(DateTimeFormatter formatter, SerializerProvider provider) {
        formatter = formatter == null ? DateTimes.DTF_STD : formatter;
        if (formatter.getZone() == null) {
            if (provider.getConfig().hasExplicitTimeZone() && provider.isEnabled(WRITE_DATES_WITH_CONTEXT_TIME_ZONE)) {
                formatter = formatter.withZone(provider.getTimeZone().toZoneId());
            }
        }

        return formatter;
    }

    protected boolean useTimestamp(SerializerProvider ctxt) {
        if (_useTimestamp != null) return _useTimestamp;

        if (_shape != null) {
            if (_shape == JsonFormat.Shape.STRING) return false;
            if (_shape == JsonFormat.Shape.NUMBER_INT) return true;
        }

        return (_formatter == null) && useTimestampFromGlobalDefaults(ctxt);
    }

    protected boolean useTimestampFromGlobalDefaults(SerializerProvider ctxt) {
        return (ctxt != null) && ctxt.isEnabled(getTimestampsFeature());
    }

    protected SerializationFeature getTimestampsFeature() {
        return SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
    }

    public boolean shouldWriteWithZoneId(SerializerProvider ctxt) {
        return (_writeZoneId != null) ? _writeZoneId : ctxt.isEnabled(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
    }

}
