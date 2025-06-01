import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HistoryLogger {
    private static HistoryLogger instance;
    private final List<String> logEntries = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Private constructor to prevent instantiation
    private HistoryLogger() {
    }

    public static synchronized HistoryLogger getInstance() {
        if (instance == null) {
            instance = new HistoryLogger();
        }
        return instance;
    }

    public void addLog(String action) {
        String timestamp = sdf.format(new Date());
        logEntries.add(timestamp + " - " + action);
    }

    public List<String> getLogs() {
        return Collections.unmodifiableList(logEntries);
    }

    public void clearLogs() {
        logEntries.clear();
    }
}
