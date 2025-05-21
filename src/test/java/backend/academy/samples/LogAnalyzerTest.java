package backend.academy.samples;

import backend.academy.LogAnalyzer;
import backend.academy.LogRecord;
import backend.academy.LogReport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogAnalyzerTest {

    // Тесты для чтения файлов
    @Test
    void shouldReadLocalFilesSuccessfully() throws IOException {
        String[] args = {"--path", "logs/2023-01-01.txt"};
        LogAnalyzer analyzer = new LogAnalyzer(args);

        List<String> files = analyzer.resolveLogFiles("logs/2023-01-01.txt");

        assertNotNull(files);
        assertFalse(files.isEmpty());
        assertTrue(files.stream().allMatch(f -> f.endsWith("2023-01-01.txt")));
    }

    @Test
    void shouldReadFilesFromURLSuccessfully() throws IOException {
        String[] args = {"--path", "http://example.com/logs/access.log"};
        LogAnalyzer analyzer = new LogAnalyzer(args);

        List<String> files = analyzer.resolveLogFiles("http://example.com/logs/access.log");

        assertNotNull(files);
        assertEquals(1, files.size());
        assertTrue(files.get(0).startsWith("http://example.com"));
    }

    // Тесты для парсинга логов
    @Test
    void shouldParseValidLogRecordSuccessfully() {
        String logLine = "127.0.0.1 - - [12/Nov/2024:10:20:30 +0000] \"GET /api/resource HTTP/1.1\" 200 1234 \"-\" \"Mozilla/5.0\"";

        LogRecord record = LogRecord.parse(logLine);

        assertNotNull(record);
        assertEquals("GET", record.method());
        assertEquals("/api/resource", record.resource());
        assertEquals(200, record.status());
        assertEquals(1234, record.size());
        assertEquals("Mozilla/5.0", record.agent());
    }

    @Test
    void shouldReturnNullForInvalidLogRecord() {
        String invalidLogLine = "Invalid log line";

        LogRecord record = LogRecord.parse(invalidLogLine);

        assertNull(record);
    }

    // Тесты фильтрации по временному диапазону
    @Test
    void shouldFilterRecordsWithinTimeRange() throws IOException {
        String[] args = {"--from", "2024-11-12T10:00:00", "--to", "2024-11-12T12:00:00"};
        LogAnalyzer analyzer = new LogAnalyzer(args);

        LocalDateTime from = analyzer.parseDateTime("2024-11-12T10:00:00", true);
        LocalDateTime to = analyzer.parseDateTime("2024-11-12T12:00:00", false);

        LogRecord record = new LogRecord(
            LocalDateTime.of(2024, 11, 12, 11, 0, 0),
            "GET",
            "/resource",
            200,
            123,
            "-",
            "Agent"
        );

        assertTrue(analyzer.isRecordInTimeRange(record, from, to));
    }

    @Test
    void shouldExcludeRecordsOutsideTimeRange() throws IOException {
        String[] args = {"--from", "2024-11-12T10:00:00", "--to", "2024-11-12T12:00:00"};
        LogAnalyzer analyzer = new LogAnalyzer(args);

        LocalDateTime from = analyzer.parseDateTime("2024-11-12T10:00:00", true);
        LocalDateTime to = analyzer.parseDateTime("2024-11-12T12:00:00", false);

        LogRecord record = new LogRecord(
            LocalDateTime.of(2024, 11, 12, 13, 0, 0),
            "GET",
            "/resource",
            200,
            123,
            "-",
            "Agent"
        );

        assertFalse(analyzer.isRecordInTimeRange(record, from, to));
    }

    // Тесты для подсчета статистики
    @Test
    void shouldCalculateStatisticsCorrectly() {
        List<LogRecord> records = List.of(
            new LogRecord(LocalDateTime.now(), "GET", "/api/resource1", 200, 500, "-", "Agent1"),
            new LogRecord(LocalDateTime.now(), "GET", "/api/resource2", 404, 1000, "-", "Agent2"),
            new LogRecord(LocalDateTime.now(), "POST", "/api/resource3", 200, 1500, "-", "Agent3")
        );

        LogReport report = LogReport.generate(records, List.of("test.log"));

        assertEquals(3, report.totalRequests());
        assertEquals(1000, report.averageResponseSize(), 0.01);
        assertEquals(1500, report.responseSize95Percentile(), 0.01);
        assertEquals(1500, report.maxResponseSize());
        assertEquals(500, report.minResponseSize());
        assertEquals(2, report.statusCodes().get(200));
        assertEquals(1, report.statusCodes().get(404));
    }

}
