package com.iofairy.test.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iofairy.falcon.map.ComparableMap;
import com.iofairy.rainforest.json.module.JacksonModules;
import com.iofairy.range.Range;
import com.iofairy.time.DateTime;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.iofairy.test.json.JsonModel.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author GG
 * @version 1.0
 * @date 2025/8/20 16:32
 */
public class Jackson0Test {

    static ObjectMapper mapper = null;

    @BeforeAll
    static void beforeAll() {
        mapper = new ObjectMapper();
        JacksonModules.registerModules(mapper);
    }

    @SneakyThrows
    @Test
    void testSerializerAndDeserialize() {
        Model model = new Model();
        DateTime start1 = DateTime.parse("2025-08-19 09:01:50.365");
        DateTime end1 = DateTime.parse("2025-08-20 16:30:00.853");
        DateTime start2 = DateTime.parse("2022-07-20 09:01:50.020");
        DateTime end2 = DateTime.parse("2020-03-15 06:30:08");
        Date start3 = DateTime.parse("2025-01-10 09:01:50.246").toDate();
        Date end3 = DateTime.parse("2024-09-28 15:30:00.783").toDate();
        model.dt = start1;
        model.dtRange = Range.open(start1, null);
        model.bigDecimal = new BigDecimal("5687994463556887999945956.658987456985699935500000");
        model.rangeWithFormat = Range.closedOpen(start3, end3);
        model.rangeNonFormat = Range.closedOpen(start3, end3);
        model.dtRanges = Arrays.asList(Range.open(start1, end1), Range.open(start2, end2));
        model.ranges = Arrays.asList(
                Range.open("bbb", null),
                Range.openClosed(new Compare1<>("a", 1, 2), new Compare1<>("b", 2, 3)),
                Range.openClosed(null, new Compare1<>("c", 5, 6)),
                Range.open(start1, end1),
                Range.closed(new BigDecimal(1), new BigDecimal("5687994463556887999945956.658987456985699935500000")),
                Range.open(true, false)
        );
        model.compare1Range = Range.open(new Compare1<>("a", 1, 2), new Compare1<>("b", 2, 3));

        model.dtRangeMap = new HashMap<>();
        model.dtRangeMap.put(1, Range.closed(start1, end1));
        model.dtRangeMap.put(2, Range.open(start2, end2));

        model.dtRangesMap = new HashMap<>();
        model.dtRangesMap.put(1, Arrays.asList(Range.open(start1, end1), Range.open(start2, end2)));

        String json = mapper.writeValueAsString(model);
        System.out.println(json);
        System.out.println("============================================================");

        Model newModel = mapper.readValue(json, Model.class);
        System.out.println(newModel);
        assertTrue(model.rangeWithFormat.isEmpty);
        assertEquals(6, model.ranges.size());
        assertSame(Compare1.class, model.compare1Range.start.getClass());
        assertEquals("['2025-01-10 09:01:50', '2024-09-28 15:30:00')", model.rangeWithFormat.toString());
        assertEquals("{1=['2025-08-19 09:01:50', '2025-08-20 16:30:00'], 2=('2022-07-20 09:01:50', '2020-03-15 06:30:08')}", model.dtRangeMap.toString());
        assertEquals("{1=[('2025-08-19 09:01:50', '2025-08-20 16:30:00'), ('2022-07-20 09:01:50', '2020-03-15 06:30:08')]}", model.dtRangesMap.toString());

    }

    @SneakyThrows
    @Test
    void testDeserializer() {
        String json = "[{\"lowerBound\":\"bbb\",\"upperBound\":null,\"intervalType\":\"OPEN\"}," +
                "{\"intervalType\":\"CLOSE\"}," +
                "{\"lowerBound\":{\"map\":null,\"mfc\":{\"i1\":1,\"i2\":2},\"t\":\"a\"},\"upperBound\":{\"map\":null,\"mfc\":{\"i1\":2,\"i2\":3},\"t\":\"b\"},\"intervalType\":\"OPEN_CLOSED\"}," +
                "{\"lowerBound\":null,\"upperBound\":{\"map\":null,\"mfc\":{\"i1\":5,\"i2\":6},\"t\":\"c\"},\"intervalType\":\"OPEN_CLOSED\"}," +
                "\"('2025-08-18 21:01:50', +∞]\"," +
                "{\"lowerBound\":null,\"upperBound\":null,\"intervalType\":\"[]\"}]";

        List<Range> ranges = mapper.readValue(json, new TypeReference<List<Range>>() {
        });
        System.out.println(ranges);
        assertEquals(ranges.toString(), "[(\"bbb\", +∞), (-∞, +∞), " +
                "({\"t\"=\"a\", \"mfc\"={\"i1\"=1, \"i2\"=2}, \"map\"=null}, {\"t\"=\"b\", \"mfc\"={\"i1\"=2, \"i2\"=3}, \"map\"=null}], " +
                "(-∞, {\"t\"=\"c\", \"mfc\"={\"i1\"=5, \"i2\"=6}, \"map\"=null}], ('2025-08-18 21:01:50', +∞), (-∞, +∞)]");

        try {
            String json1 = "{\"lowerBound\":\"bbb\",\"upperBound\":null}";
            Range range1 = mapper.readValue(json1, Range.class);
            throwException();
        } catch (Exception e) {
            assertSame(e.getClass(), JsonParseException.class);
            assertEquals(e.getMessage(), "The JSON string '{\"lowerBound\":\"bbb\",\"upperBound\":null}' cannot be parsed to a `Range` instance for type [com.iofairy.range.Range<com.iofairy.falcon.map.ComparableMap<java.lang.String,java.lang.Object>>]. \n" +
                    " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 39]");
            assertEquals(e.getCause().getMessage(), "The `intervalType` field cannot be null in '{\"lowerBound\":\"bbb\",\"upperBound\":null}'");
        }

    }


    @SneakyThrows
    @Test
    void testDeserializer1() {
        /// ////////////////////////////////////
        String s = "\"∅\"";
        Range<Integer> emptyRange01 = mapper.readValue(s, Range.class);
        Range<Integer> emptyRange02 = mapper.readValue(s, new TypeReference<Range<Integer>>() {
        });
        Range<DateTime> emptyRange03 = mapper.readValue(s, new TypeReference<Range<DateTime>>() {
        });
        Range<Date> emptyRange04 = mapper.readValue(s, new TypeReference<Range<Date>>() {
        });
        Range<Double> emptyRange05 = mapper.readValue(s, new TypeReference<Range<Double>>() {
        });
        Range<ComparableMap<String, Object>> emptyRange06 = mapper.readValue(s, new TypeReference<Range<ComparableMap<String, Object>>>() {
        });
        Range<?> emptyRange07 = mapper.readValue(s, Range.class);
        System.out.println(emptyRange01);
        System.out.println(emptyRange02);
        System.out.println(emptyRange03);
        System.out.println(emptyRange04);
        System.out.println(emptyRange05);
        System.out.println(emptyRange06);
        System.out.println(emptyRange07);

        System.out.println("============================================================");
        String s1 = "\" (-∞ ,  +∞ ) \"";
        Range<Integer> infinityRange01 = mapper.readValue(s1, Range.class);
        Range<Integer> infinityRange02 = mapper.readValue(s1, new TypeReference<Range<Integer>>() {
        });
        Range<DateTime> infinityRange03 = mapper.readValue(s1, new TypeReference<Range<DateTime>>() {
        });
        Range<Date> infinityRange04 = mapper.readValue(s1, new TypeReference<Range<Date>>() {
        });
        Range<Double> infinityRange05 = mapper.readValue(s1, new TypeReference<Range<Double>>() {
        });
        Range<ComparableMap<String, Object>> infinityRange06 = mapper.readValue(s1, new TypeReference<Range<ComparableMap<String, Object>>>() {
        });
        Range<?> infinityRange07 = mapper.readValue(s1, Range.class);

        System.out.println(infinityRange01);
        System.out.println(infinityRange02);
        System.out.println(infinityRange03);
        System.out.println(infinityRange04);
        System.out.println(infinityRange05);
        System.out.println(infinityRange06);
        System.out.println(infinityRange07);
        System.out.println("============================================================");
        /// ////////////////////////////////////
        String s2 = "\" [-∞ ,  '2025-01-15 20:11:05' ] \"";
        Range<DateTime> dtRange01 = mapper.readValue(s2, Range.class);
        Range<DateTime> dtRange02 = mapper.readValue(s2, new TypeReference<Range<DateTime>>() {
        });
        Range<Date> dtRange03 = mapper.readValue(s2, new TypeReference<Range<Date>>() {
        });
        try {
            Range<ComparableMap<String, Object>> dtRange04 = mapper.readValue(s2, new TypeReference<Range<ComparableMap<String, Object>>>() {
            });
            throwException();
        } catch (JsonProcessingException e) {
            assertSame(e.getClass(), JsonParseException.class);
            assertEquals(e.getMessage(), "The string ' [-∞ ,  '2025-01-15 20:11:05' ] ' cannot be parsed to a `Range` instance for type [com.iofairy.range.Range<com.iofairy.falcon.map.ComparableMap<java.lang.String,java.lang.Object>>]. \n" +
                    " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 35]");
        }
        try {
            mapper.readValue(s2, new TypeReference<Range<Integer>>() {
            });
            throwException();
        } catch (JsonProcessingException e) {
            assertSame(e.getClass(), JsonParseException.class);
            assertEquals(e.getMessage(), "The string ' [-∞ ,  '2025-01-15 20:11:05' ] ' cannot be parsed to a `Range` instance for type [com.iofairy.range.Range<java.lang.Integer>]. \n" +
                    " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 35]");
        }
        Range<?> dtRange05 = mapper.readValue(s2, Range.class);
        Range<Calendar> dtRange06 = mapper.readValue(s2, new TypeReference<Range<Calendar>>() {
        });

        System.out.println(dtRange01);
        System.out.println(dtRange02);
        System.out.println(dtRange03);
        System.out.println(dtRange05);
        System.out.println(dtRange06);
        System.out.println("============================================================");

        assertEquals("('1970-01-01 08:00:00', '1970-01-01 08:00:00')", emptyRange03.toString());
        assertEquals("({}, {})", emptyRange06.toString());
        assertEquals("(-∞, '2025-01-15 20:11:05']", dtRange01.toString());

    }


    private void throwException() {
        throw new RuntimeException();
    }

}
