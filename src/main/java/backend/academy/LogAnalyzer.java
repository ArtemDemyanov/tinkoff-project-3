package backend.academy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

// Класс для анализа логов, предоставляет функционал фильтрации, парсинга и формирования отчётов.
public class LogAnalyzer {

    private static final String HTTP_PREFIX = "http";
    private static final String INVALID_DATE_MESSAGE = "Неверный формат даты/времени: ";
    private static final String NO_PATH_PROVIDED = "Путь к лог-файлу не указан. Используйте аргумент --path.";
    private static final int DATE_TIME_LENGTH = 10;

    @Getter
    private final Map<String, String> params;
    private final List<LogRecord> records;
    private final PrintStream out;

    public LogAnalyzer(String[] args) {
        this.params = CommandLineParser.parseArgs(args);
        this.records = new ArrayList<>();
        this.out = System.out;
    }

    // Выполняет основной процесс анализа логов: разрешение файлов, фильтрация записей и создание отчёта.
    public LogReport analyze() throws IOException {
        String path = params.get("path");
        if (path == null) {
            throw new IllegalArgumentException(NO_PATH_PROVIDED);
        }

        List<String> logFiles = resolveLogFiles(path);

        LocalDateTime from = parseDateTime(params.get("from"), true);
        LocalDateTime to = parseDateTime(params.get("to"), false);

        List<LogRecord> filteredRecords = processLogFiles(logFiles, from, to);

        if (filteredRecords.isEmpty()) {
            out.println("Не найдено записей для анализа.");
        } else {
            out.println("Обработано записей: " + filteredRecords.size());
        }

        return LogReport.generate(filteredRecords, logFiles);
    }

    // Обрабатывает список лог-файлов, применяя фильтры и парсинг записей, возвращает список отфильтрованных записей.
    private List<LogRecord> processLogFiles(List<String> logFiles,
        LocalDateTime from, LocalDateTime to) throws IOException {
        List<LogRecord> filteredRecords = new ArrayList<>();
        for (String file : logFiles) {
            try (Stream<String> lines = createStreamReader(file)) {
                List<LogRecord> fileRecords = lines.map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(LogRecord::parse)
                    .filter(logRecord -> logRecord != null)
                    .filter(logRecord -> isRecordInTimeRange(logRecord, from, to))
                    .filter(this::matchesFilter)
                    .collect(Collectors.toList());
                filteredRecords.addAll(fileRecords);
            }
        }
        return filteredRecords;
    }

    // Создаёт поток для чтения строк из локального файла или удалённого ресурса по URL.
    private Stream<String> createStreamReader(String file) throws IOException {
        if (file.startsWith(HTTP_PREFIX)) {
            return new BufferedReader(new InputStreamReader(new URL(file).openStream())).lines();
        }
        return Files.lines(Paths.get(file));
    }

    // Проверяет, соответствует ли запись заданным фильтрам, таким как метод, агент, ресурс или статус.
    private boolean matchesFilter(LogRecord logRecord) {
        String filterField = params.get("filter-field");
        String filterValue = params.get("filter-value");
        if (filterField == null || filterValue == null) {
            return true;
        }

        return switch (filterField.toLowerCase()) {
            case "agent" -> logRecord.agent().toLowerCase()
                .contains(filterValue.toLowerCase().replace("*", ""));
            case "method" -> logRecord.method().equalsIgnoreCase(filterValue);
            case "resource" -> logRecord.resource()
                .contains(filterValue.replace("*", ""));
            case "status" -> String.valueOf(logRecord.status()).equals(filterValue);
            default -> true;
        };
    }

    // Парсит строку с датой и временем, поддерживает опциональное указание начала или конца дня.
    public LocalDateTime parseDateTime(String dateTime, boolean startOfDay) {
        if (dateTime == null) {
            return null;
        }

        try {
            if (dateTime.length() == DATE_TIME_LENGTH) {
                return startOfDay
                    ? LocalDateTime.parse(dateTime + "T00:00:00")
                    : LocalDateTime.parse(dateTime + "T23:59:59");
            }
            return LocalDateTime.parse(dateTime);
        } catch (Exception e) {
            throw new IllegalArgumentException(INVALID_DATE_MESSAGE + dateTime);
        }
    }

    // Проверяет, входит ли запись в заданный временной диапазон.
    public boolean isRecordInTimeRange(LogRecord logRecord, LocalDateTime from, LocalDateTime to) {
        return (from == null || !logRecord.time().isBefore(from))
            && (to == null || !logRecord.time().isAfter(to));
    }

    // Определяет список файлов логов для анализа, используя подходящий резолвер (локальный или удалённый).
    public List<String> resolveLogFiles(String path) throws IOException {
        LogFileResolver resolver = createResolver(path);
        return resolver.resolve(path);
    }

    // Создаёт экземпляр резолвера для обработки локальных или удалённых лог-файлов.
    private LogFileResolver createResolver(String path) {
        if (path.startsWith(HTTP_PREFIX)) {
            return new RemoteLogFileResolver();
        } else {
            return new LocalLogFileResolver();
        }
    }

}
