package org.chomookun.fintics.core.ohlcv.indicator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.chomookun.arch4j.core.common.test.CoreTestUtil;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractCalculatorTest {

    /**
     * Read test resource as TSV
     * @param pkg resource package
     * @param fileName file name
     * @param columnNames column names
     * @return TSV map list
     */
    protected final List<Map<String,String>> readTestResourceAsTsv(Package pkg, String fileName, String[] columnNames) {
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter("\t")
                .setHeader(columnNames)
                .setSkipHeaderRecord(true)
                .build();
        List<Map<String,String>> list = new ArrayList<>();
        try (InputStream inputStream = CoreTestUtil.readTestResourceAsStream(pkg, fileName)) {
            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .forEach(record -> {
                        Map<String,String> map = new LinkedHashMap<>();
                        for(String columnName : columnNames) {
                            String columnValue = record.get(columnName);
                            map.put(columnName, columnValue);
                        }
                        list.add(map);
                    });
            return list;
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert ohlcvs
     * @param rows rows
     * @param datetimeNameAndPattern data time name and pattern
     * @param openName open name
     * @param highName high name
     * @param lowName low name
     * @param closeName close name
     * @param volumeName volume name
     * @return list of ohlcv
     */
    protected final List<Ohlcv> convertOhlcvs(List<Map<String,String>> rows, String datetimeNameAndPattern, String openName, String highName, String lowName, String closeName, String volumeName) {
        String datetimeName = null;
        String datetimePattern = null;
        if(datetimeNameAndPattern != null && datetimeNameAndPattern.contains("^")) {
            String[] array = datetimeNameAndPattern.split("\\^");
            datetimeName = array[0];
            datetimePattern = array[1];
        }
        String finalDateTimeName = datetimeName;
        String finalDateTimePattern = datetimePattern;
        return rows.stream()
                .map(row -> {
                    LocalDateTime dateTime = null;
                    if(finalDateTimeName != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat(finalDateTimePattern);
                            Date date = sdf.parse(row.get(finalDateTimeName));
                            dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                        } catch (ParseException ignored) {}
                    }
                    return Ohlcv.builder()
                            .dateTime(dateTime)
                            .open(new BigDecimal(row.get(openName).replaceAll(",","")))
                            .high(new BigDecimal(row.get(highName).replaceAll(",","")))
                            .low(new BigDecimal(row.get(lowName).replaceAll(",","")))
                            .close(new BigDecimal(row.get(closeName).replaceAll(",","")))
                            .volume(Optional.ofNullable(row.get(volumeName))
                                    .map(value -> new BigDecimal(value.replaceAll(",","")))
                                    .orElse(BigDecimal.ZERO))
                            .build();
                })
                .collect(Collectors.toList());
    }

}
