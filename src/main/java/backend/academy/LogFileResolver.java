package backend.academy;

import java.io.IOException;
import java.util.List;

// Интерфейс для разрешения пути к лог-файлам и получения списка файлов для анализа.
public interface LogFileResolver {
    List<String> resolve(String path) throws IOException;
}
