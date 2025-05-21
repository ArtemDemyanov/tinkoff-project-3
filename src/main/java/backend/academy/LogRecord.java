package backend.academy;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Класс для представления записи лога.
public record LogRecord(
    LocalDateTime time, // Время записи
    String method,      // HTTP-метод
    String resource,    // Ресурс (URL)
    int status,         // Статус ответа
    int size,           // Размер ответа
    String referer,     // Referer (откуда пришёл запрос)
    String agent        // User-Agent (информация о клиенте)
) {
    // Формат даты/времени в логе
    private static final DateTimeFormatter LOG_DATE_FORMATTER = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("dd/MMM/yyyy:HH:mm:ss Z")
        .toFormatter(Locale.ENGLISH);

    // Регулярное выражение для разбора строки лога
    private static final Pattern LOG_PATTERN = Pattern.compile(
        "^(\\S+) \\S+ \\S+ \\[(.+?)] \"(\\S+) (\\S+) \\S+\" (\\d{3}) (\\d+) \"(.*?)\" \"(.*?)\""
    );

    // Группы в регулярном выражении
    private static final int GROUP_TIME = 2;
    private static final int GROUP_METHOD = 3;
    private static final int GROUP_RESOURCE = 4;
    private static final int GROUP_STATUS = 5;
    private static final int GROUP_SIZE = 6;
    private static final int GROUP_REFERER = 7;
    private static final int GROUP_AGENT = 8;

    // Сообщение об ошибке
    private static final String ERROR_PARSING_LOG = "Ошибка разбора строки лога: ";

    // Парсит строку лога и создаёт объект LogRecord.
    public static LogRecord parse(String line) {
        try {
            Matcher matcher = LOG_PATTERN.matcher(line);
            if (!matcher.matches()) {
                System.err.println(ERROR_PARSING_LOG + line);
                return null;
            }

            // Извлекаем время
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(matcher.group(GROUP_TIME), LOG_DATE_FORMATTER);
            LocalDateTime time = zonedDateTime.toLocalDateTime();

            // Извлекаем метод, ресурс, статус, размер, реферер и агент
            String method = matcher.group(GROUP_METHOD);
            String resource = matcher.group(GROUP_RESOURCE);
            int status = Integer.parseInt(matcher.group(GROUP_STATUS));
            int size = Integer.parseInt(matcher.group(GROUP_SIZE));
            String referer = matcher.group(GROUP_REFERER);
            String agent = matcher.group(GROUP_AGENT);

            return new LogRecord(time, method, resource, status, size, referer, agent);
        } catch (Exception e) {
            System.err.println(ERROR_PARSING_LOG + line + " — " + e.getMessage());
            return null;
        }
    }
}
