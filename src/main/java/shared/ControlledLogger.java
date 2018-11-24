package shared;

public class ControlledLogger {
    private static boolean logsAllowed = true;

    public synchronized static void allowLogs() {
        logsAllowed = true;
    }

    public synchronized static void disableLogs() {
        logsAllowed = false;
    }

    public synchronized static boolean areLogsAllowed(){
        return logsAllowed;
    }

    public synchronized static void log(String data) {
        if(data != null && logsAllowed) {
            System.out.println(data);
        }
    }
}
