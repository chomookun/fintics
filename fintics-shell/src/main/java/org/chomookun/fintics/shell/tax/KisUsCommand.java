package org.chomookun.fintics.shell.tax;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import technology.tabula.*;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@ShellComponent
@ShellCommandGroup("Tax Commands")
@RequiredArgsConstructor
@Slf4j
public class KisUsCommand {

    /**
     * 한국투자증권 미국 주식 세금 보고서 PDF 파일을 엑셀로 변환하는 명령어
     * @param inputPdfFilePath 한국투자증권 양도소득세 PDF 파일 경로
     * @param outputExcelFilePath 홈텍스 양도소득세 제출 엑셀 파일 경우
     */
    @ShellMethod(key = "tax kis-us", value = "Decrypts the given text.")
    public void kisUs(
            @ShellOption(help = "Input PDF file path") String inputPdfFilePath,
            @ShellOption(help = "Output Excel file path") String outputExcelFilePath
    ) throws IOException {
        File inputPdfFile = new File(inputPdfFilePath);
        PDDocument document = Loader.loadPDF(inputPdfFile);

        ObjectExtractor extractor = new ObjectExtractor(document);
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();

        List<InputPdfRow> inputPdfRows = new ArrayList<>();
        PageIterator pageIterator = extractor.extract();
        while (pageIterator.hasNext()) {
            Page page = pageIterator.next();
            List<Table> tables = sea.extract(page);
            for (Table table : tables) {
                int colCount = table.getColCount();
                // 컬럼 11개가 매매내역임
                if (colCount == 11) {
                    List<InputPdfRow> inputPdfRowsInTable = convertTableToPdfInputRows(table);
                    inputPdfRows.addAll(inputPdfRowsInTable);
                }
            }
        }

        // sum of feeAmount
        BigDecimal totalFeeAmount = inputPdfRows.stream()
                .map(InputPdfRow::getFeeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Total fee amount: {}", totalFeeAmount);

        // sum of profitAmount
        BigDecimal totalProfitAmount = inputPdfRows.stream()
                .map(InputPdfRow::getProfitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Total profit amount: {}", totalProfitAmount);

        // creates excel output file
        List<OutputExcelRow> outputExcelRows = convertInputPdfRowsToOutputExcelRows(inputPdfRows);

        // writes to excel file
        int chunkSize = 500;
        int totalSize = outputExcelRows.size();
        File excelOutFile = new File(outputExcelFilePath);
        File dir = excelOutFile.getParentFile();
        String[] parts = excelOutFile.getName().split("\\.");
        String prefixFilename = parts[0];
        String extension = parts[1];

        for (int from = 0; from < totalSize; from += chunkSize) {
            int to = Math.min(from + chunkSize, totalSize);
            List<OutputExcelRow> chunk = outputExcelRows.subList(from, to);

            // 여기서 각 500개 단위 처리
            File finalExcelOutputFile = new File(dir, prefixFilename + "_" + (from / chunkSize + 1) + "." + extension);
            exportOutputExcelRowsToFile(chunk, finalExcelOutputFile.getAbsolutePath());
        }

        document.close();
    }

    List<InputPdfRow> convertTableToPdfInputRows(Table table) {
        List<InputPdfRow> pdfInputRows = new ArrayList<>();
        List<List<RectangularTextContainer>> rows = table.getRows();
        int rowCount = rows.size();
        for (int i = 0; i < rowCount; i++) {
            List<RectangularTextContainer> row = rows.get(i);
            String firstColValue = row.get(0).getText();
            if (firstColValue.contains("주식")) {
                continue;
            }

            InputPdfRow pdfInputRow = convertRowToPdfInputRow(row);
            pdfInputRows.add(pdfInputRow);
        }
        return pdfInputRows;
    }

    InputPdfRow convertRowToPdfInputRow(List<RectangularTextContainer> row) {
        InputPdfRow pdfInputRow = null;
        try {
            String assetName = normalizeValue(row.get(0).getText());
            String assetIsin = normalizeValue(row.get(1).getText());
            String kindCode = normalizeValue(row.get(2).getText());
            String sellQuantity = normalizeValue(row.get(3).getText());
            String sellDate = normalizeValue(row.get(4).getText());
            String sellPrice = normalizeValue(row.get(5).getText());
            String sellAmount = normalizeValue(row.get(6).getText());
            String buyPrice = normalizeValue(row.get(7).getText());
            String buyAmount = normalizeValue(row.get(8).getText());
            String feeAmount = normalizeValue(row.get(9).getText());
            String profitAmount = normalizeValue(row.get(10).getText());
            pdfInputRow = InputPdfRow.builder()
                    .assetName(assetName)
                    .assetIsin(assetIsin)
                    .kindCode(kindCode)
                    .sellQuantity(new BigDecimal(sellQuantity))
                    .sellDate(LocalDate.parse(sellDate, DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                    .sellPrice(new BigDecimal(sellPrice))
                    .sellAmount(new BigDecimal(sellAmount))
                    .buyPrice(new BigDecimal(buyPrice))
                    .buyAmount(new BigDecimal(buyAmount))
                    .feeAmount(new BigDecimal(feeAmount))
                    .profitAmount(new BigDecimal(profitAmount))
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
        return pdfInputRow;
    }

    String normalizeValue(String value) {
        value = value.replaceAll("[\\r\\n]", " ").trim();
        value = value.replaceAll("\\s+", "");
        value = value.replaceAll(",", "");
        return value;
    }

    List<OutputExcelRow> convertInputPdfRowsToOutputExcelRows(List<InputPdfRow> inputPdfRows) {
        List<OutputExcelRow> outputExcelRows = new ArrayList<>();
        for (InputPdfRow inputPdfRow : inputPdfRows) {
            OutputExcelRow outputExcelRow = OutputExcelRow.builder()
                    .assetName(inputPdfRow.getAssetName())
                    .companyNo("")
                    .type("2")
                    .sellQuantity(inputPdfRow.getSellQuantity())
                    .stockType("10")
                    .stockKind("61")
                    .taxCode("61")
                    .buyType("01")
                    .sellDate(inputPdfRow.getSellDate())
                    .sellPrice(inputPdfRow.getSellPrice())
                    .sellAmount(inputPdfRow.getSellAmount())
                    // 한국투자증권은 이동평균 산출 방식이라 매수일자가 없는듯 그 해 첫날짜로 고정
                    .buyDate(inputPdfRow.getSellDate().withDayOfYear(1))
                    .buyPrice(inputPdfRow.getBuyPrice())
                    .buyAmount(inputPdfRow.getBuyAmount())
                    .feeAmount(inputPdfRow.getFeeAmount())
                    .nonTaxAmount(BigDecimal.ZERO)
                    .taxReduceKind("")
                    .taxReduceRate("")
                    .taxReduceAmount("")
                    .taxDeferralYn("N")
                    .assetIsin(inputPdfRow.getAssetIsin())
                    .assetCountryCode("US")
                    .assetDescription("")
                    .build();
            outputExcelRows.add(outputExcelRow);
        }
        return outputExcelRows;
    }

    void exportOutputExcelRowsToFile(List<OutputExcelRow> outputExcelRows, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Overseas Capital Gains");
        // header
        String[] headers = {
                "주식 종목명",
                "사업자등록번호",
                "국내/국외 구분",
                "취득유형별 양도주식 수",
                "세율구분",
                "주식등 종류",
                "양도물건 종류",
                "취득유형",
                "양도일자",
                "주당양도가액",
                "양도가액",
                "취득일자",
                "주당취득가액",
                "취득가액",
                "필요경비",
                "비과세 양도소득금액",
                "감면종류",
                "감면율",
                "감면소득금액",
                "과세이연여부",
                "국제증권식별번호(ISIN코드,종목코드)",
                "국외자산국가코드",
                "국외자산내용"
        };
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        // data
        DecimalFormat decimalFormat = new DecimalFormat("#,##0");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int rowNum = 1;
        for (OutputExcelRow outputExcelRow : outputExcelRows) {
            Row excelRow = sheet.createRow(rowNum++);
            String[] cellValues = new String[23];
            cellValues[0] = outputExcelRow.getAssetName();
            cellValues[1] = outputExcelRow.getCompanyNo();
            cellValues[2] = outputExcelRow.getType();
            cellValues[3] = decimalFormat.format(outputExcelRow.getSellQuantity());
            cellValues[4] = outputExcelRow.getStockType();
            cellValues[5] = outputExcelRow.getStockKind();
            cellValues[6] = outputExcelRow.getTaxCode();
            cellValues[7] = outputExcelRow.getBuyType();
            cellValues[8] = dateTimeFormatter.format(outputExcelRow.getSellDate());
            cellValues[9] = decimalFormat.format(outputExcelRow.getSellPrice());
            cellValues[10] = decimalFormat.format(outputExcelRow.getSellAmount());
            cellValues[11] = dateTimeFormatter.format(outputExcelRow.getBuyDate());
            cellValues[12] = decimalFormat.format(outputExcelRow.getBuyPrice());
            cellValues[13] = decimalFormat.format(outputExcelRow.getBuyAmount());
            cellValues[14] = decimalFormat.format(outputExcelRow.getFeeAmount());
            cellValues[15] = decimalFormat.format(outputExcelRow.getNonTaxAmount());
            cellValues[16] = outputExcelRow.getTaxReduceKind();
            cellValues[17] = outputExcelRow.getTaxReduceRate();
            cellValues[18] = outputExcelRow.getTaxReduceAmount();
            cellValues[19] = outputExcelRow.getTaxDeferralYn();
            cellValues[20] = outputExcelRow.getAssetIsin();
            cellValues[21] = outputExcelRow.getAssetCountryCode();
            cellValues[22] = outputExcelRow.getAssetDescription();
            // cell
            for (int i = 0; i < cellValues.length; i++) {
                excelRow.createCell(i).setCellValue(cellValues[i]);
            }
        }
        File file = new File(filePath);
        try (FileOutputStream out = new FileOutputStream(file)) {
            workbook.write(out);
        }
        workbook.close();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class InputPdfRow {
        private String assetName;
        private String assetIsin;
        private String kindCode;
        // sell info
        private BigDecimal sellQuantity;
        private LocalDate sellDate;
        private BigDecimal sellPrice;
        private BigDecimal sellAmount;
        // buy info
        private BigDecimal buyPrice;
        private BigDecimal buyAmount;
        // final info
        private BigDecimal feeAmount;
        private BigDecimal profitAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class OutputExcelRow {
        private String assetName;               // 주식종목명
        private String companyNo;               // 사업자등록번호
        private String type = "2";              // 국내/국외 구분 (1: 국내주식, 2: 해외주식)
        private BigDecimal sellQuantity;        // 취득유형별 양도주식 수
        private String stockType = "10";        // 양도물건종류
        private String stockKind = "61";        // 주식등종류 (61: 국외주식등-중소기업외)
        private String taxCode = "61";          // 세율구분 (61: 중소기업외 소액주주, 중소기업외 국외주식)
        private String buyType = "01";          // 취득유형 (01: 매매)
        private LocalDate sellDate;             // 양도일자 (형식: yyyy-MM-dd)
        private BigDecimal sellPrice;           // 주당양도가액
        private BigDecimal sellAmount;          // 양도가액
        private LocalDate buyDate;              // 취득일자 (형식: yyyy-MM-dd)
        private BigDecimal buyPrice;            // 주당취득가액
        private BigDecimal buyAmount;           // 취득가액
        private BigDecimal feeAmount;           // 필요경비
        private BigDecimal nonTaxAmount;        // 비과세 양도소득금액
        private String taxReduceKind;           // 감면 종류
        private String taxReduceRate;           // 감면율
        private String taxReduceAmount;         // 감면소득금액
        private String taxDeferralYn;           // 과세이연 여부
        private String assetIsin;               // 종목코드(ISIN)
        private String assetCountryCode = "US"; // 국외자산국가코드
        private String assetDescription;        // 국외자산내용
    }

}
