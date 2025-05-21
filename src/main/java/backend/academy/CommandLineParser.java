package backend.academy;

import java.util.HashMap;
import java.util.Map;

// Утилитарный класс для парсинга аргументов командной строки.
public class CommandLineParser {
    private CommandLineParser() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Парсит переданные аргументы командной строки в виде массива строк и возвращает их в виде карты ключ-значение.
    public static Map<String, String> parseArgs(String[] args) {
        Map<String, String> parsedParams = new HashMap<>();
        for (int index = 0; index < args.length; index++) {
            if (args[index].startsWith("--")) {
                String key = args[index].substring(2);
                if (index + 1 < args.length && !args[index + 1].startsWith("--")) {
                    parsedParams.put(key, args[index + 1]);
                } else {
                    parsedParams.put(key, "");
                }
            }
        }
        return parsedParams;
    }
}
