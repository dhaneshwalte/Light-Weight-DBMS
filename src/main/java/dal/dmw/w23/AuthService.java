package dal.dmw.w23;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.mindrot.jbcrypt.BCrypt;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import dal.dmw.w23.models.UserInfo;

public class AuthService {
    private String authFilePath = "meta/auth.csv";
    private static UserInfo principle;
    public boolean authenticate(String username, String password) {
        UserInfo userInfo = getUserInfoFromCsv(username);
        if (userInfo == null){
            //TODO: Make custom exception
            System.out.println("User not found");
            return false;
        }

        String hashedPassword = userInfo.getHashedPassword();
        boolean passwordMatches = BCrypt.checkpw(password, hashedPassword);

        if (passwordMatches) {
            principle = new UserInfo(username, hashedPassword);
            return true;
        } else {
            return false;
        }
    }

    public static UserInfo getPrinciple(){
        if (principle == null){
            System.out.println("Unauthenticated");
        }
        return principle;
    }

    private UserInfo getUserInfoFromCsv(String username){
        UserInfo userInfo = null;
        try {
            FileReader reader = new FileReader(this.authFilePath);
            CSVReader csvReader = new CSVReader(reader);
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                if (nextLine[0].equals(username)) {
                    userInfo = new UserInfo(nextLine[0], nextLine[1]);
                    break;
                }
            }
            csvReader.close();
        } catch (IOException e) {
            //TODO: Handle file not found exception
            e.printStackTrace();
        }
        return userInfo;
    }

    public boolean register(String username, String password){
        UserInfo userinfo = getUserInfoFromCsv(username);
        if (userinfo != null){
            //TODO: throw exception
            return false;
        }
        String userSalt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(password, userSalt);
        String filename = "meta/auth.csv";
        String[] row1 = { username, hashedPassword };
        FileWriter writer = null;
        CSVWriter csvWriter = null;
        try {
            File file = new File(filename);
            if (file.exists()) {
                //File exists, append data
                writer = new FileWriter(filename, true);
            } else {
                //File dne, add headers
                writer = new FileWriter(filename);
            }
            csvWriter = new CSVWriter(writer);
            if (!file.exists()){ //write headers if file dne
                String[] headers = { "username", "hashedPassword" };
                csvWriter.writeNext(headers);
            }
            csvWriter.writeNext(row1);
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}