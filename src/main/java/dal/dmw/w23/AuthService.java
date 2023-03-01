package dal.dmw.w23;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dal.dmw.w23.models.UserInfo;

//Reference - https://howtodoinjava.com/java/java-security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
/**
 * This is a service class that handles 
 * login and registration of the user
 */
public class AuthService {
    /**
     * Path for auth file where user credentials are stored
     */
    private String authFilePath = "meta/auth.txt";
    /**
     * messageDigest for calculating hash value of user password
     */
    MessageDigest messageDigest = null;
    AuthService(){
        try{
            messageDigest = MessageDigest.getInstance("MD5");
        }
        catch(NoSuchAlgorithmException e){
            System.out.println("Invalid Hashing Algorithm");
        }
    }
    /**
     * This method checks if the user inputted username and password matches.
     * @param username - input username
     * @param password - input password
     * @return - returns true if credentials are valid, false if not.
     */
    public boolean authenticate(String username, String password) {
        UserInfo userInfo = getUserInfoFromFile(username);

        if (userInfo == null){
            System.out.println("User not found");
            return false;
        }

        String userPasswordHashed = userInfo.getHashedPassword();
        String inputPasswordHashed = getHashedPassword(password);

        if (userPasswordHashed.equals(inputPasswordHashed)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method registers a new user in the database
     * @param username - input username
     * @param password - input password
     * @return - returns true if user registration is successful.
     */

    public boolean register(String username, String password){
        UserInfo userinfo = getUserInfoFromFile(username);
        if (userinfo != null){
            return false;
        }
        String hashedPassword = getHashedPassword(password);
        String[] headers = { "username", "hashedPassword" };
        String[] values = { username, hashedPassword };
        FileWriter fileWriter = null;
        PrintWriter printWriter = null;
        boolean writeHeaders = false;
        File authFile = new File(authFilePath);
        try{
            if (authFile.exists()){
                fileWriter = new FileWriter(authFilePath, true);
            } else {
                authFile.createNewFile();
                fileWriter = new FileWriter(authFilePath);
                writeHeaders = true;
            }
            printWriter = new PrintWriter(fileWriter);
            if (writeHeaders){
                printWriter.println(String.join(",", headers));
            }
            String line = String.join(",", values);
            printWriter.println(line);
            printWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * This is a utility method fetches the user object using the specified username
     * @param username - username of the user
     * @return - return the object of UserInfo class if the mentioned user exists.
     */
    private UserInfo getUserInfoFromFile(String username){
        File authFile = new File(authFilePath);
        if (!authFile.exists()){
            return null;
        }
        UserInfo userInfo = null;
        FileReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new FileReader(this.authFilePath);
            bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",");
                if (values[0].equals(username)) {
                    userInfo = new UserInfo(values[0], values[1]);
                    break;
                }
            }
            bufferedReader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userInfo;
    }

    /**
     * This is a utility method that calculates md5 hash for the given password
     * @param password - input password
     * @return - returns the hash of the input password.
     */
    public String getHashedPassword(String password){
        messageDigest.update(password.getBytes());
        byte[] bytes = messageDigest.digest();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            stringBuilder.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuilder.toString();
    }
}