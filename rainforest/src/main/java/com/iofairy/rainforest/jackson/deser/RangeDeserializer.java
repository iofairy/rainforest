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
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.iofairy.falcon.map.ComparableMap;
import com.iofairy.range.*;
import com.iofairy.tcf.Try;
import com.iofairy.time.DateTime;
import com.iofairy.time.DateTimes;
import com.iofairy.top.O;
import com.iofairy.tuple.EasyTuple;
import com.iofairy.tuple.EasyTuple2;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static com.iofairy.validator.Preconditions.checkArgument;

/**
 * {@link Range} 反序列化器
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
public class RangeDeserializer extends DateTimeBaseDeserializer<Range> implements ContextualDeserializer {
    private static final long serialVersionUID = 1L;

    public static final RangeDeserializer INSTANCE = new RangeDeserializer();

    protected RangeDeserializer() {
        this(DTF);
    }

    public RangeDeserializer(DateTimeFormatter formatter) {
        super(Range.class, formatter);
    }

    public RangeDeserializer(String pattern) {
        super(Range.class, pattern);
    }

    protected RangeDeserializer(Class<Range> clazz) {
        super(clazz);
    }

    protected RangeDeserializer(RangeDeserializer base, JavaType currentType) {
        this(base, base._formatter, currentType, base._useTimestamp, base._writeZoneId, base._shape, base._leniency);
    }

    protected RangeDeserializer(RangeDeserializer base, JsonFormat.Shape shape) {
        this(base, base._formatter, base._currentType, base._useTimestamp, base._writeZoneId, shape, base._leniency);
    }

    protected RangeDeserializer(RangeDeserializer base, Boolean leniency) {
        this(base, base._formatter, base._currentType, base._useTimestamp, base._writeZoneId, base._shape, leniency);
    }

    protected RangeDeserializer(RangeDeserializer base, DateTimeFormatter formatter, Boolean useTimestamp) {
        this(base, formatter, base._currentType, useTimestamp, base._writeZoneId, base._shape, base._leniency);
    }

    protected RangeDeserializer(RangeDeserializer base,
                                DateTimeFormatter formatter,
                                JavaType currentType,
                                Boolean useTimestamp,
                                Boolean writeZoneId,
                                JsonFormat.Shape shape,
                                Boolean leniency) {
        super(base, formatter, currentType, useTimestamp, writeZoneId, shape, leniency);
    }

    protected DateTimeBaseDeserializer<?> withFormat(DateTimeFormatter formatter, Boolean useTimestamp) {
        return new RangeDeserializer(this, formatter, useTimestamp);
    }

    protected DateTimeBaseDeserializer<?> withJavaType(JavaType javaType) {
        return new RangeDeserializer(this, javaType);
    }

    protected DateTimeBaseDeserializer<?> withShape(JsonFormat.Shape shape) {
        return new RangeDeserializer(this, shape);
    }

    protected DateTimeBaseDeserializer<?> withLeniency(Boolean leniency) {
        return new RangeDeserializer(this, leniency);
    }


    @Override
    public Range<?> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JacksonException {
        JavaType currentType = _currentType;
        boolean canGetActualType = Try.tcfs(() -> _currentType.getBindings().getBoundType(0)) != null
                && Try.tcfs(() -> _currentType.getBindings().getBoundType(0).getRawClass()) != Object.class;    // 是否能获取真实的 range泛型类型
        if (!canGetActualType) {
            currentType = ctxt.getTypeFactory().constructType(new TypeReference<Range<ComparableMap<String, Object>>>() {
            });
        }

        // 获取 range 真实类型
        JavaType actualTypeArgument = currentType.getBindings().getBoundType(0);
        @SuppressWarnings("unchecked")
        Class<? extends Comparable<?>> comparableClass = (Class<? extends Comparable<?>>) actualTypeArgument.getRawClass();


        /*================================================
         *************************************************
         ==========      当前TOKEN是字符串类型     ==========
         *************************************************
         ================================================*/
        if (parser.hasTokenId(JsonTokenId.ID_STRING)) {
            String jsonText = parser.getText().trim();
            boolean isValidRangeString = (jsonText.startsWith("[") || jsonText.startsWith("(")) && (jsonText.endsWith("]") || jsonText.endsWith(")")) && jsonText.contains(", ");
            if (isValidRangeString) {
                String centerSection = jsonText.substring(1, jsonText.length() - 1).trim();
                if (Ranges.INFINITY_RANGE.matcher(centerSection).matches()) {
                    return Range.open(null, null);
                }
            }

            try {
                if (canGetActualType) {  /* 可以获取真实类型 */
                    Class<? extends Comparable> comparableRawClass = comparableClass;
                    if (Objects.equals(jsonText, Range.EMPTY_SET)) {
                        try {
                            @SuppressWarnings("unchecked")
                            Range<?> range = Ranges.parseRange(jsonText, comparableRawClass);
                            return range;
                        } catch (Exception e) {
                            if (comparableRawClass == ComparableMap.class) {
                                return Range.open(new ComparableMap<>(), new ComparableMap<>());
                            }
                            throw e;
                        }
                    } else if (isValidRangeString) {
                        @SuppressWarnings("unchecked")
                        Range<?> range = Ranges.parseRange(jsonText, comparableRawClass, _formatter);
                        return range;
                    }
                } else {  /* 无法获取真实类型 */
                    if (Objects.equals(jsonText, Range.EMPTY_SET)) {
                        return Range.open(0, 0);
                    } else if (isValidRangeString) {
                        Class<?> clazz = Ranges.inferPossibleRangeType(jsonText, Boolean.TRUE.equals(_useTimestamp));
                        if (clazz != null) {
                            @SuppressWarnings("unchecked")
                            Range<?> range = Ranges.parseRange(jsonText, (Class<? extends Comparable>) clazz, _formatter);
                            return range;
                        }
                    }
                }

                throw new IllegalArgumentException();
            } catch (IllegalArgumentException e) {
                throw new JsonParseException(parser, "The string '" + parser.getText() + "' cannot be parsed to a `Range` instance for type " + getJavaTypeString(currentType) + ". ");
            } catch (Exception e) {
                throw new JsonParseException(parser, "The string '" + parser.getText() + "' cannot be parsed to a `Range` instance for type " + getJavaTypeString(currentType) + ". ", e);
            }
        }


        /*================================================
         *************************************************
         ==========  当前TOKEN是JSON或Object类型  ==========
         *************************************************
         ================================================*/
        ObjectCodec codec = parser.getCodec();
        if (codec instanceof ObjectMapper) {
            ObjectMapper mapper = (ObjectMapper) codec;
            JsonNode rangeJsonNode = mapper.readTree(parser);
            try {
                /*
                 注：BigDecimal 反序列化会失真，会当作Double来处理。可以开启此项配置 mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
                 */
                JsonNode lowerJsonNode = rangeJsonNode.get("lowerBound");
                JsonNode upperJsonNode = rangeJsonNode.get("upperBound");
                JsonNode intervalTypeJsonNode = rangeJsonNode.get("intervalType");
                checkArgument(intervalTypeJsonNode == null || intervalTypeJsonNode.isNull(), "The `intervalType` field cannot be null in '${?}'", rangeJsonNode);
                String intervalTypeStr = intervalTypeJsonNode.textValue();
                IntervalType intervalType = IntervalType.of(intervalTypeStr);

                JsonNode boundJsonNode = O.firstNonNull(lowerJsonNode, upperJsonNode);
                if (boundJsonNode == null || ((lowerJsonNode == null || lowerJsonNode.isNull()) && (upperJsonNode == null || upperJsonNode.isNull()))) {
                    return Range.open(null, null);
                } else {

                    if (canGetActualType) {
                        Comparable<?> lowerBound = readBoundValue(lowerJsonNode, comparableClass, mapper, actualTypeArgument, _formatter, Boolean.TRUE.equals(_useTimestamp));
                        Comparable<?> upperBound = readBoundValue(upperJsonNode, comparableClass, mapper, actualTypeArgument, _formatter, Boolean.TRUE.equals(_useTimestamp));
                        return Range.of(lowerBound, upperBound, intervalType);
                    } else {
                        Class<? extends Comparable<?>> possibleType = inferPossibleType(lowerJsonNode, upperJsonNode, Boolean.TRUE.equals(_useTimestamp), _formatter);
                        Comparable<?> lowerBound = readBoundValue(lowerJsonNode, possibleType, mapper, actualTypeArgument, _formatter, Boolean.TRUE.equals(_useTimestamp));
                        Comparable<?> upperBound = readBoundValue(upperJsonNode, possibleType, mapper, actualTypeArgument, _formatter, Boolean.TRUE.equals(_useTimestamp));
                        return Range.of(lowerBound, upperBound, intervalType);
                    }
                }

            } catch (Exception e) {
                throw new JsonParseException(parser, "The JSON string '" + rangeJsonNode.toString() + "' cannot be parsed to a `Range` instance for type " + getJavaTypeString(currentType) + ". ", e);
            }
        }

        throw new JsonParseException(parser, "`JsonParser.getCodec()` is not an instance of ObjectMapper! ");
    }

    /**
     * 根据所提供的类型，再通过JsonNode对象获取指定类型的值
     *
     * @param boundJsonNode      JsonNode对象
     * @param comparableClass    指定类型
     * @param mapper             ObjectMapper
     * @param actualTypeArgument 实际类型
     * @param formatter          日期格式化器
     * @param useTimestamp       是否使用时间戳
     * @return 指定类型的值
     */
    private static Comparable<?> readBoundValue(JsonNode boundJsonNode,
                                                Class<? extends Comparable<?>> comparableClass,
                                                ObjectMapper mapper,
                                                JavaType actualTypeArgument,
                                                DateTimeFormatter formatter,
                                                boolean useTimestamp) {
        Comparable<?> bound;
        if (boundJsonNode == null || boundJsonNode.isNull()) {
            bound = null;
        } else if (Byte.class == comparableClass || byte.class == comparableClass) {
            bound = (byte) boundJsonNode.intValue();
        } else if (Character.class == comparableClass || char.class == comparableClass) {
            bound = boundJsonNode.textValue().charAt(0);
        } else if (Short.class == comparableClass || short.class == comparableClass) {
            bound = boundJsonNode.shortValue();
        } else if (Integer.class == comparableClass || int.class == comparableClass) {
            bound = boundJsonNode.intValue();
        } else if (Long.class == comparableClass || long.class == comparableClass) {
            bound = boundJsonNode.longValue();
        } else if (Float.class == comparableClass || float.class == comparableClass) {
            bound = boundJsonNode.floatValue();
        } else if (Double.class == comparableClass || double.class == comparableClass) {
            bound = boundJsonNode.doubleValue();
        } else if (BigInteger.class == comparableClass) {
            bound = boundJsonNode.bigIntegerValue();
        } else if (BigDecimal.class == comparableClass) {
            bound = boundJsonNode.decimalValue();
        } else if (DateTimes.isDTSupported(comparableClass)) {
            if (useTimestamp) {
                bound = DateTime.of(boundJsonNode.longValue()).toDT(comparableClass);
            } else {
                bound = DateTime.parse(boundJsonNode.textValue(), formatter).toDT(comparableClass);
            }
        } else if (String.class == comparableClass) {
            bound = boundJsonNode.textValue();
        } else if (Boolean.class == comparableClass || boolean.class == comparableClass) {
            bound = boundJsonNode.booleanValue();
        } else {
            bound = mapper.convertValue(boundJsonNode, actualTypeArgument);
        }
        return bound;
    }


    /**
     * Json反序列化时未获取到Range实际参数类型，所以推断Range可能的实际参数类型
     *
     * @param lower          range的lowerBound JsonNode对象
     * @param upper          range的upperBound JsonNode对象
     * @param maybeTimestamp 是否可能为时间戳
     * @param formatter      时间格式化器
     * @return Range可能的实际参数类型
     */
    private static Class<? extends Comparable<?>> inferPossibleType(JsonNode lower, JsonNode upper, boolean maybeTimestamp, DateTimeFormatter formatter) {
        boolean isLowerNull = lower == null || lower.isNull();
        boolean isUpperNull = upper == null || upper.isNull();
        if (isLowerNull && isUpperNull) return Integer.class;

        /*
         存在一个为 null
         */
        if (isLowerNull || isUpperNull) {
            return getBoundClass(isUpperNull ? lower : upper, maybeTimestamp, formatter);
        } else {
            /*
             都不为 null
             */
            Class<? extends Comparable<?>> lowerClass = getBoundClass(lower, maybeTimestamp, formatter);
            Class<? extends Comparable<?>> upperClass = getBoundClass(upper, maybeTimestamp, formatter);
            if (lowerClass == upperClass) return lowerClass;
            if ((lowerClass == String.class && upperClass == DateTime.class) || (lowerClass == DateTime.class && upperClass == String.class)) {
                return String.class;
            }
            if (Number.class.isAssignableFrom(lowerClass) && Number.class.isAssignableFrom(upperClass)) {
                EasyTuple2<Integer> typeIndex1 = getNumberIndexByClass(lowerClass);
                EasyTuple2<Integer> typeIndex2 = getNumberIndexByClass(upperClass);
                return getNumberClassByIndex(EasyTuple.of(Math.max(typeIndex1._1, typeIndex2._1), Math.max(typeIndex1._2, typeIndex2._2)));
            }

            @SuppressWarnings("unchecked")
            Class<? extends Comparable<?>> mapClass = (Class<? extends Comparable<?>>) (Class<? extends Comparable>) ComparableMap.class;
            return mapClass;
        }
    }

    private static Class<? extends Comparable<?>> getBoundClass(JsonNode boundJsonNode, boolean maybeTimestamp, DateTimeFormatter formatter) {
        if (boundJsonNode.isLong() && isMaybeTimestamp(boundJsonNode.asText(), maybeTimestamp)) return DateTime.class;
        if (boundJsonNode.isShort() || boundJsonNode.isInt()) return Integer.class;
        if (boundJsonNode.isLong()) return Long.class;
        if (boundJsonNode.isBigInteger()) return BigInteger.class;
        if (boundJsonNode.isFloat()) return Float.class;
        if (boundJsonNode.isDouble()) return Double.class;
        if (boundJsonNode.isBigDecimal() || boundJsonNode.isNumber()) return BigDecimal.class;
        if (boundJsonNode.isBoolean()) return Boolean.class;
        if (boundJsonNode.isTextual()) {
            DateTime dateTime = Try.tcfs(() -> DateTime.parse(boundJsonNode.textValue(), formatter));
            return dateTime != null ? DateTime.class : String.class;
        }
        @SuppressWarnings("unchecked")
        Class<? extends Comparable<?>> mapClass = (Class<? extends Comparable<?>>) (Class<? extends Comparable>) ComparableMap.class;
        return mapClass;
    }

    private static boolean isMaybeTimestamp(String str, boolean maybeTimestamp) {
        return maybeTimestamp && str.length() == 13 && !str.contains("-") && !str.contains("+");
    }

    private static Class<? extends Comparable<?>> getNumberClassByIndex(EasyTuple2<Integer> index) {
        switch (index._1 + "" + index._2) {
            case "11":
                return Integer.class;
            case "12":
                return Long.class;
            case "13":
                return BigInteger.class;
            case "20":
                return Float.class;
            case "21":
                return Double.class;
            case "22":
            case "23":
                return BigDecimal.class;
            default:
                return null;
        }
    }


    private static EasyTuple2<Integer> getNumberIndexByClass(Class<? extends Comparable<?>> clazz) {
        if (Integer.class == clazz) return EasyTuple.of(1, 1);
        if (Long.class == clazz) return EasyTuple.of(1, 2);
        if (BigInteger.class == clazz) return EasyTuple.of(1, 3);
        if (Float.class == clazz) return EasyTuple.of(2, 0);
        if (Double.class == clazz) return EasyTuple.of(2, 1);
        if (BigDecimal.class == clazz) return EasyTuple.of(2, 2);

        return EasyTuple.of(-1, -1);
    }

    private static String getJavaTypeString(JavaType javaType) {
        if (javaType == null) return null;
        return "[" + javaType.toString().replaceAll("\\[simple type, class ", "").replaceAll("\\]", "") + "]";
    }

}
