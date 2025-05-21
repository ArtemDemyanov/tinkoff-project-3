package backend.academy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Класс для форматирования отчёта.
public class LogReportFormatter {
    private static final double PERCENTAGE_DIVISOR = 100.0;
    private static final int COLUMN_WIDTH = 18;

    private static final String NEW_LINE = " |\n";
    private static final String DOUBLE_NEW_LINE = "\n\n";
    private static final String BYTE_FORMAT = "%,.2f b";
    private static final String HEADER_SEPARATOR_MD = "|-----------------------------|--------------------|";
    private static final String HEADER_SEPARATOR_ADOC = "|===";

    private static final Map<Integer, String> HTTP_STATUS_NAMES = Map.ofEntries(
        Map.entry(200, "OK"),
        Map.entry(404, "Not Found"),
        Map.entry(403, "Forbidden"),
        Map.entry(500, "Internal Server Error"),
        Map.entry(503, "Service Unavailable"),
        Map.entry(206, "Partial Content"),
        Map.entry(304, "Not Modified"),
        Map.entry(416, "Range Not Satisfiable"),
        Map.entry(400, "Bad Request"),
        Map.entry(401, "Unauthorized"),
        Map.entry(201, "Created"),
        Map.entry(202, "Accepted"),
        Map.entry(204, "No Content")
    );

    public static double calculatePercentile(List<LogRecord> records, int percentile) {
        List<Integer> sizes = records.stream().map(LogRecord::size).sorted().collect(Collectors.toList());
        if (sizes.isEmpty()) {
            return 0;
        }
        int index = (int) Math.ceil((percentile / PERCENTAGE_DIVISOR) * sizes.size()) - 1;
        return sizes.get(index);
    }

    public String format(LogReport data, String format) {
        boolean isAdoc = format.equalsIgnoreCase("adoc");
        StringBuilder sb = new StringBuilder();

        String sectionHeader = isAdoc ? "==" : "####";
        String headerSeparator = isAdoc ? HEADER_SEPARATOR_ADOC : HEADER_SEPARATOR_MD;

        // Общая информация
        sb.append(sectionHeader).append(" Общая информация").append(DOUBLE_NEW_LINE);
        sb.append(headerSeparator).append("\n");
        sb.append("| Метрика                    | Значение           ").append(NEW_LINE)
            .append(headerSeparator).append("\n")
            .append("| Файл(-ы)                   | `").append(String.join(", ", data.fileNames())).append("`")
            .append(NEW_LINE)
            .append("| Начальная дата             | ")
            .append((data.startDate() != null ? data.startDate().toLocalDate() : "-")).append(NEW_LINE)
            .append("| Конечная дата              | ")
            .append((data.endDate() != null ? data.endDate().toLocalDate() : "-")).append(NEW_LINE)
            .append("| Количество запросов        | ").append(data.totalRequests()).append(NEW_LINE)
            .append("| Средний размер ответа      | ").append(String.format(BYTE_FORMAT, data.averageResponseSize()))
            .append(NEW_LINE)
            .append("| 95p размера ответа         | ")
            .append(String.format(BYTE_FORMAT, data.responseSize95Percentile())).append(NEW_LINE)
            .append("| Максимальный размер ответа | ").append(data.maxResponseSize()).append(" b").append(NEW_LINE)
            .append("| Минимальный размер ответа  | ").append(data.minResponseSize()).append(" b").append(NEW_LINE)
            .append(headerSeparator).append(DOUBLE_NEW_LINE);

        // Запрашиваемые ресурсы
        sb.append(sectionHeader).append(" Запрашиваемые ресурсы").append(DOUBLE_NEW_LINE);
        if (data.mostRequestedResources().isEmpty()) {
            sb.append("Нет данных о запрашиваемых ресурсах.").append(DOUBLE_NEW_LINE);
        } else {
            sb.append(headerSeparator).append("\n");
            sb.append("| Ресурс              | Количество         ").append(NEW_LINE);
            sb.append(headerSeparator).append("\n");
            for (var entry : data.mostRequestedResources()) {
                sb.append("| ").append(entry.getKey())
                    .append(" ".repeat(Math.max(0, COLUMN_WIDTH - entry.getKey().length())))
                    .append("| ").append(entry.getValue()).append(NEW_LINE);
            }
            sb.append(headerSeparator).append(DOUBLE_NEW_LINE);
        }

        // Коды ответа
        sb.append(sectionHeader).append(" Коды ответа").append(DOUBLE_NEW_LINE);
        if (data.statusCodes().isEmpty()) {
            sb.append("Нет данных о кодах ответа.").append(DOUBLE_NEW_LINE);
        } else {
            sb.append(headerSeparator).append("\n");
            sb.append("| Код               | Имя                  | Количество        ").append(NEW_LINE);
            sb.append(headerSeparator).append("\n");
            for (var entry : data.statusCodes().entrySet()) {
                String statusName = HTTP_STATUS_NAMES.getOrDefault(entry.getKey(), "Unknown");
                sb.append("| ").append(entry.getKey())
                    .append(" ".repeat(Math.max(0, COLUMN_WIDTH - entry.getKey().toString().length())))
                    .append("| ").append(statusName)
                    .append(" ".repeat(Math.max(0, COLUMN_WIDTH - statusName.length())))
                    .append("| ").append(entry.getValue()).append(NEW_LINE);
            }
            sb.append(headerSeparator).append(DOUBLE_NEW_LINE);
        }

        return sb.toString();
    }
}
