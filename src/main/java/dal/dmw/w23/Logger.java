package dal.dmw.w23;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Class used for logging
 */
public class Logger {

    /**
     * Logs the message in the log file specified in the constants
     * @param logMessage - logMessage to be printed
     */
    public void log(String logMessage){
        System.out.println(logMessage);
        FileWriter fileWriter;
        File file = new File(Constants.logFilePath);
        try{
            if (!file.exists()){
                file.createNewFile();
                fileWriter = new FileWriter(file);
            }else{
                fileWriter = new FileWriter(file, true);
            }
            PrintWriter writer = new PrintWriter(fileWriter);
            writer.println(logMessage);
            writer.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
