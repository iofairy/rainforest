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
package com.iofairy.rainforest.json.deser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.iofairy.time.DateTime;
import com.iofairy.tcf.Try;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

/**
 * {@link DateTime} Json反序列基类
 *
 * @since 0.6.0
 */
public abstract class DateTimeBaseDeserializer<T> extends StdScalarDeserializer<T> implements ContextualDeserializer {
    private static final long serialVersionUID = 1L;

    protected static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("y-M-d H:m:s");

    protected final DateTimeFormatter _formatter;
    protected final JavaType _currentType;
    protected final Boolean _useTimestamp;
    protected final Boolean _writeZoneId;
    protected final JsonFormat.Shape _shape;
    protected final Boolean _leniency;

    protected DateTimeBaseDeserializer(Class<T> clazz, DateTimeFormatter formatter) {
        super(clazz);
        _formatter = formatter;
        _currentType = null;
        _useTimestamp = null;
        _writeZoneId = null;
        _shape = null;
        _leniency = null;
    }

    protected DateTimeBaseDeserializer(Class<T> clazz, String pattern) {
        super(clazz);
        _formatter = DateTimeFormatter.ofPattern(pattern);
        _currentType = null;
        _useTimestamp = null;
        _writeZoneId = null;
        _shape = null;
        _leniency = null;
    }

    protected DateTimeBaseDeserializer(Class<T> clazz) {
        super(clazz);
        _formatter = DTF;
        _currentType = null;
        _useTimestamp = null;
        _writeZoneId = null;
        _shape = null;
        _leniency = null;
    }


    protected DateTimeBaseDeserializer(DateTimeBaseDeserializer<T> base,
                                       DateTimeFormatter formatter,
                                       JavaType currentType,
                                       Boolean useTimestamp,
                                       Boolean writeZoneId,
                                       JsonFormat.Shape shape,
                                       Boolean leniency) {
        super(base);
        _formatter = formatter;
        _currentType = currentType;
        _useTimestamp = useTimestamp != null && useTimestamp;
        _writeZoneId = writeZoneId != null && writeZoneId;
        _shape = shape;
        _leniency = leniency != null && leniency;
    }


    protected abstract DateTimeBaseDeserializer<?> withFormat(DateTimeFormatter formatter, Boolean useTimestamp);

    protected abstract DateTimeBaseDeserializer<?> withJavaType(JavaType javaType);

    protected abstract DateTimeBaseDeserializer<?> withShape(JsonFormat.Shape shape);

    protected abstract DateTimeBaseDeserializer<?> withLeniency(Boolean leniency);


    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        DateTimeBaseDeserializer<?> deser = this;

        if (ctxt != null) {
            JavaType currentType = ctxt.getContextualType();
            deser = deser.withJavaType(currentType);
        }

        JsonFormat.Value format = findFormatOverrides(ctxt, property, handledType());
        if (format != null) {
            if (format.hasLenient()) {
                Boolean leniency = format.getLenient();
                if (leniency != null) deser = deser.withLeniency(leniency);
            }

            Boolean useTimestamp = null;

            JsonFormat.Shape shape = format.getShape();
            if (shape == JsonFormat.Shape.ARRAY || shape.isNumeric()) {
                useTimestamp = Boolean.TRUE;
            } else {
                useTimestamp = (shape == JsonFormat.Shape.STRING) ? Boolean.FALSE : null;
            }

            if (format.hasPattern()) {
                final String pattern = format.getPattern();
                final Locale locale = format.hasLocale() ? format.getLocale() : ctxt.getLocale();
                DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
                if (acceptCaseInsensitiveValues(ctxt, format)) {
                    builder.parseCaseInsensitive();
                }
                builder.appendPattern(pattern);
                DateTimeFormatter df;
                if (locale == null) {
                    df = builder.toFormatter();
                } else {
                    df = builder.toFormatter(locale);
                }

                if (format.hasTimeZone()) {
                    df = df.withZone(format.getTimeZone().toZoneId());
                }

                deser = deser.withFormat(df, useTimestamp);
            }

            if (shape != _shape) {
                deser = deser.withShape(shape);
            }
        }

        return deser;
    }


    private boolean acceptCaseInsensitiveValues(DeserializationContext ctxt, JsonFormat.Value format) {
        Boolean enabled = format.getFeature(JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_VALUES);
        if (enabled == null) {
            enabled = ctxt.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_VALUES);
        }
        return enabled;
    }

    protected void _throwNoNumericTimestampNeedTimeZone(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        ctxt.reportInputMismatch(handledType(),
                "raw timestamp (%d) not allowed for `%s`: need additional information such as an offset or time-zone (see class Javadocs)",
                p.getNumberValue(), handledType().getName());
    }

    @SuppressWarnings("unchecked")
    protected <R> R _handleUnexpectedToken(DeserializationContext context, JsonParser parser, String message, Object... args) throws JsonMappingException {
        try {
            return (R) context.handleUnexpectedToken(handledType(), parser.getCurrentToken(), parser, message, args);

        } catch (JsonMappingException e) {
            throw e;
        } catch (IOException e) {
            throw JsonMappingException.fromUnexpectedIOE(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected <R> R _handleDateTimeException(DeserializationContext context, DateTimeException e0, String value) throws JsonMappingException {
        try {
            return (R) context.handleWeirdStringValue(handledType(), value,
                    "Failed to deserialize %s: (%s) %s",
                    handledType().getName(), e0.getClass().getName(), e0.getMessage());

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


    protected DateTime _fromString(JsonParser p, DeserializationContext ctxt, String string0) throws IOException {
        String string = string0.trim();
        if (string.isEmpty()) {
            return _fromEmptyString(p, ctxt, string);
        }

        try {
            if (_leniency != null && _leniency) {
                DateTime dt = Try.tcf(() -> DateTime.parse(string, _formatter), false);
                if (dt == null) {
                    dt = DateTime.parse(string);
                }
                return dt;
            } else {
                return DateTime.parse(string, _formatter);
            }
        } catch (DateTimeException e) {
            return _handleDateTimeException(ctxt, e, string);
        }
    }

    @SuppressWarnings("unchecked")
    protected DateTime _fromEmptyString(JsonParser p, DeserializationContext ctxt, String str) throws IOException {
        final CoercionAction act = _checkFromStringCoercion(ctxt, str);
        switch (act) { // note: Fail handled above
            case AsEmpty:
                return (DateTime) getEmptyValue(ctxt);
            case TryConvert:
            case AsNull:
            default:
        }

        return null;
    }

}
