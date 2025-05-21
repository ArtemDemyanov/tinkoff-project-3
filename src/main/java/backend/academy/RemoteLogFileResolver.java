package backend.academy;

import java.util.Collections;
import java.util.List;

// Класс для разрешения удалённых лог-файлов, принимает URL и возвращает его как единственный элемент списка.
public class RemoteLogFileResolver implements LogFileResolver {

    // Проверяет, начинается ли путь с "http", и возвращает URL как список,
    // либо выбрасывает исключение для некорректного пути.
    @Override
    public List<String> resolve(String path) {
        if (!path.startsWith("http")) {
            throw new IllegalArgumentException("Некорректный URL: " + path);
        }
        return Collections.singletonList(path);
    }
}
