package backend.academy;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

// Главный класс для запуска программы анализа логов.
public final class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName()); // Логгер

    // Приватный конструктор, чтобы предотвратить создание экземпляра класса.
    private Main() {
        throw new UnsupportedOperationException("Утилитный класс не должен иметь публичного конструктора");
    }

    // Точка входа в программу.
    public static void main(String[] args) {
        PrintStream out = System.out; // Используем PrintStream для вывода
        PrintStream err = System.err; // Используем PrintStream для ошибок

        try {
            // Создаём анализатор и запускаем анализ логов
            LogAnalyzer analyzer = new LogAnalyzer(args);
            LogReport report = analyzer.analyze();

            // Получаем формат отчёта (по умолчанию "markdown")
            String format = analyzer.params().getOrDefault("format", "markdown");

            LogReportFormatter formatter = new LogReportFormatter();
            String formattedReport = formatter.format(report, format);
            // Выводим отчёт
            out.println(formattedReport);
        } catch (Exception e) {
            // Логируем ошибку
            LOGGER.log(Level.SEVERE, "Ошибка выполнения программы: " + e.getMessage(), e);
        }
    }
}
