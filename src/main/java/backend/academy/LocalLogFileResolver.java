package backend.academy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Класс для разрешения файлов логов в локальной файловой системе на основе заданного пути и шаблона.
public class LocalLogFileResolver implements LogFileResolver {

    // Находит файлы в указанной директории, соответствующие заданному шаблону, и возвращает список путей к ним.
    @Override
    public List<String> resolve(String path) throws IOException {
        Path dirPath = Paths.get(path.substring(0, path.lastIndexOf('/')));
        String filePattern = path.substring(path.lastIndexOf('/') + 1)
            .replace("*", ".*").replace("?", ".?");

        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            throw new IOException("Указанная директория не существует: " + dirPath);
        }

        try (Stream<Path> stream = Files.list(dirPath)) {
            return stream.filter(Files::isRegularFile)
                .filter(file -> file.getFileName().toString().matches(filePattern))
                .map(Path::toString)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new IOException("Ошибка при обработке файлов в директории: "
                + dirPath + ", шаблон: " + filePattern, e);
        }
    }
}
