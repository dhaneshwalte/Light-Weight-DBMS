package dal.dmw.w23;

import java.util.Calendar;

public class Constants {
    public static final String fileSeparator = ";";
    public static final String databasePath = "database/";
    public static final String metaFileExtension = ".meta";
    public static final String tableFileExtension = ".table";
    public static final String logFilePath = "Logs/" + 
                                             Calendar.getInstance().get(Calendar.YEAR) + "-" +
                                             Calendar.getInstance().get(Calendar.MONTH) + "-" +
                                             Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + 
                                             ".log";
}
