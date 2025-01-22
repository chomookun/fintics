package org.chomoo.fintics.indicator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.chomoo.fintics.model.Ohlcv;

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

    protected final List<Map<String,String>> readTsv(String filePath, String[] columnNames) {
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter("\t")
                .setHeader(columnNames)
                .setSkipHeaderRecord(true)
                .build();
        List<Map<String,String>> list = new ArrayList<>();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
            CSVParser.parse(inputStream, StandardCharsets.UTF_8, format).stream()
                    .forEach(record -> {
                        Map<String,String> map = new LinkedHashMap<>();
                        for(String columnName : columnNames) {
                            String columnValue = record.get(columnName);
                            map.put(columnName, columnValue);
                        }
                        list.add(map);
                    });
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
        return list;
    }

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
