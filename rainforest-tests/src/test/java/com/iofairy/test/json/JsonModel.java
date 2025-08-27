package com.iofairy.test.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iofairy.range.Range;
import com.iofairy.time.DateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author GG
 * @version 1.0
 */
public class JsonModel {
    @Data
    public static class Model {
        DateTime dt;
        DateTime dt1;
        Range<DateTime> dtRange;
        BigDecimal bigDecimal;
        @JsonFormat(pattern = "yyyyMMddHHmmss '['VV']'", timezone = "GMT-4", shape = JsonFormat.Shape.NUMBER_INT)
        Range<?> rangeWithFormat;
        Range<?> rangeNonFormat;
        Range<Compare1<String>> compare1Range;
        List<Range<DateTime>> dtRanges;
        @JsonFormat(pattern = "yyyyMMdd HHmmss '['VV']'", timezone = "GMT-4")
        List<Range<?>> ranges;
        @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss.SSS '['VV']'", timezone = "GMT+2")
        Map<Integer, Range<DateTime>> dtRangeMap;
        Map<Integer, List<Range<DateTime>>> dtRangesMap;
    }

    @Data
    @NoArgsConstructor
    public static class Compare1<T> implements Comparable<Compare1> {
        Map<String, String> map;
        ModelForCompare1 mfc;
        T t;

        public Compare1(T t, Integer i1, Integer i2) {
            this.t = t;
            this.mfc = new ModelForCompare1();
            mfc.i1 = i1;
            mfc.i2 = i2;
        }

        @Override
        public int compareTo(Compare1 o) {
            return Integer.compare(mfc.i1 + (mfc.i2 == null ? 0 : mfc.i2), o.mfc.i1 + (o.mfc.i2 == null ? 0 : o.mfc.i2));
        }

    }

    @Data
    public static class ModelForCompare1 {
        Integer i1;
        Integer i2;
    }

    @Data
    public static class Compare2 implements Comparable<Compare2> {
        int i1;
        int i2;

        @Override
        public int compareTo(Compare2 o) {
            return 0;
        }
    }

}
