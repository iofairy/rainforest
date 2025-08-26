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
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.iofairy.range.Ranges;
import com.iofairy.time.DateTime;
import com.iofairy.time.DateTimePattern;
import com.iofairy.time.DateTimes;
import com.iofairy.time.TZ;
import com.iofairy.range.Range;
import com.iofairy.top.O;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;

/**
 * {@link Range} 序列化器
 *
 * @since 0.6.0
 */
public class RangeSerializer extends DatetimeBaseSerializer<Range> implements ContextualSerializer {
    private static final long serialVersionUID = 1L;

    public static final RangeSerializer INSTANCE = new RangeSerializer();

    protected RangeSerializer() {
        this(DateTimes.STD_DTF);
    }

    public RangeSerializer(DateTimeFormatter formatter) {
        super(Range.class);
    }

    public RangeSerializer(String pattern) {
        super(Range.class);
    }

    protected RangeSerializer(Class<Range> clazz) {
        super(clazz);
    }

    protected RangeSerializer(RangeSerializer base, Boolean writeZoneId) {
        this(base, base._formatter, base._currentType, base._useTimestamp, writeZoneId, base._shape);
    }

    protected RangeSerializer(RangeSerializer base, JavaType currentType) {
        this(base, base._formatter, currentType, base._useTimestamp, base._writeZoneId, base._shape);
    }

    protected RangeSerializer(RangeSerializer base, DateTimeFormatter formatter, Boolean useTimestamp, JsonFormat.Shape shape) {
        this(base, formatter, base._currentType, useTimestamp, base._writeZoneId, shape);
    }

    protected RangeSerializer(RangeSerializer base,
                              DateTimeFormatter formatter,
                              JavaType currentType,
                              Boolean useTimestamp,
                              Boolean writeZoneId,
                              JsonFormat.Shape shape) {
        super(base, formatter, currentType, useTimestamp, writeZoneId, shape);
    }

    @Override
    protected DatetimeBaseSerializer<?> withFormat(DateTimeFormatter formatter, Boolean useTimestamp, JsonFormat.Shape shape) {
        return new RangeSerializer(this, formatter, useTimestamp, shape);
    }

    @Override
    protected DatetimeBaseSerializer<?> withJavaType(JavaType javaType) {
        return new RangeSerializer(this, javaType);
    }

    @Override
    protected DatetimeBaseSerializer<?> withFeatures(Boolean writeZoneId) {
        return new RangeSerializer(this, writeZoneId);
    }

    @Override
    public void serialize(Range value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        if (value.lowerBound == null && value.upperBound == null) {
            gen.writeString(value.toString());
        } else if (value.lowerBound == null || value.upperBound == null) {
            Comparable<?> comparable = O.firstNonNull(value.lowerBound, value.upperBound);
            if (Ranges.isSupportedParseRangeString(comparable.getClass())) {
                gen.writeString(value.toString(_formatter, _useTimestamp));
                return;
            }
        } else {
            if ((value.lowerBound instanceof Number && value.upperBound instanceof Number)
                    || (DateTimes.isDTSupported(value.lowerBound.getClass()) && DateTimes.isDTSupported(value.upperBound.getClass()))
                    || (value.lowerBound instanceof Character && value.upperBound instanceof Character)) {
                gen.writeString(value.toString(_formatter, _useTimestamp));
                return;
            }
        }

        gen.writeStartObject();
        gen.writeObjectField("lowerBound", value.lowerBound);
        gen.writeObjectField("upperBound", value.upperBound);
        gen.writeObjectField("intervalType", value.intervalType.name());
        gen.writeEndObject();

    }

}
