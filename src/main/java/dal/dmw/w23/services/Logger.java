package dal.dmw.w23.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import dal.dmw.w23.Constants;

/**
 * Class used for logging
 */
public class Logger {

    String username;
    public Logger(String username){
        this.username = username;
    }

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
            writer.println(username + "|" + new Date().toString() + "|" + logMessage);
            writer.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
