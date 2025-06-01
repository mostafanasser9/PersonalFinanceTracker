import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HistoryLogger {
    private static final List<String> logEntries = new ArrayList<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Private constructor to prevent instantiation
    private HistoryLogger() {
    }

    public static void addLog(String action) {
        String timestamp = sdf.format(new Date());
        logEntries.add(timestamp + " - " + action);
    }

    public static List<String> getLogs() {
        return Collections.unmodifiableList(logEntries);
    }

    public static void clearLogs() {
        logEntries.clear();
    }
}
