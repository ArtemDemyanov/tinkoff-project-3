package backend.academy;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

// Класс для управления данными отчёта.
public class LogReport {
    private static final int PERCENTILE_95 = 95;
    private static final int TOP_RESOURCES_LIMIT = 10;

    @Getter
    private int totalRequests;
    @Getter
    private double averageResponseSize;
    @Getter
    private double responseSize95Percentile;
    @Getter
    private int maxResponseSize;
    @Getter
    private int minResponseSize;
    @Getter
    private List<String> fileNames;
    @Getter
    private LocalDateTime startDate;
    @Getter
    private LocalDateTime endDate;
    @Getter
    private List<Map.Entry<String, Long>> mostRequestedResources;
    @Getter
    private Map<Integer, Long> statusCodes;

    // Создаёт объект LogReport, заполняя данные на основе списка записей логов и имен файлов.
    public static LogReport generate(List<LogRecord> records, List<String> fileNames) {
        LogReport reportData = new LogReport();
        reportData.fileNames = (fileNames != null) ? fileNames : Collections.emptyList();
        reportData.prepareReportData(records);
        return reportData;
    }

    // Подготавливает данные отчёта, включая инициализацию статистики, если список записей пустой.
    private void prepareReportData(List<LogRecord> records) {
        if (records.isEmpty()) {
            totalRequests = 0;
            averageResponseSize = 0;
            responseSize95Percentile = 0;
            maxResponseSize = 0;
            minResponseSize = 0;
            mostRequestedResources = Collections.emptyList();
            statusCodes = Collections.emptyMap();
            return;
        }
        calculateStatistics(records);
    }

    // Рассчитывает основные статистические данные:
    // общее количество запросов, средний размер ответа,
    // максимальный и минимальный размер ответа,
    // 95-й перцентиль размера ответа, а также временные рамки.
    private void calculateStatistics(List<LogRecord> records) {
        IntSummaryStatistics stats = records.stream()
            .mapToInt(LogRecord::size)
            .summaryStatistics();

        totalRequests = (int) stats.getCount();
        averageResponseSize = stats.getAverage();
        maxResponseSize = stats.getMax();
        minResponseSize = stats.getMin();
        responseSize95Percentile = LogReportFormatter.calculatePercentile(records, PERCENTILE_95);
        startDate = records.stream().map(LogRecord::time).min(LocalDateTime::compareTo).orElse(null);
        endDate = records.stream().map(LogRecord::time).max(LocalDateTime::compareTo).orElse(null);

        populateResourceAndStatusData(records);
    }

    // Заполняет данные о распределении статус-кодов и определяет наиболее запрашиваемые ресурсы.
    private void populateResourceAndStatusData(List<LogRecord> records) {
        statusCodes = records.stream()
            .collect(Collectors.groupingBy(LogRecord::status, Collectors.counting()));
        Map<String, Long> resourcesCount = records.stream()
            .collect(Collectors.groupingBy(LogRecord::resource, Collectors.counting()));
        mostRequestedResources = resourcesCount.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(TOP_RESOURCES_LIMIT)
            .collect(Collectors.toList());
    }
}
