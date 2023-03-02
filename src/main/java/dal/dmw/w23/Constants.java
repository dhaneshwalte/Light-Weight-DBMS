package dal.dmw.w23;

import java.util.Calendar;

public class Constants {
    public static final String fileSeparator = ";";
    public static final String databaseDirectory = "database";
    public static final String databasePath = databaseDirectory + "/";
    public static final String metaFileExtension = ".meta";
    public static final String tableFileExtension = ".table";
    public static final String logsDirectory = "Logs";
    public static final String logFilePath = logsDirectory + 
                                             Calendar.getInstance().get(Calendar.YEAR) + "-" +
                                             Calendar.getInstance().get(Calendar.MONTH+1) + "-" +
                                             Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + 
                                             ".log";
}
