package dal.dmw.w23.services;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dal.dmw.w23.Constants;
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
    public AuthService(){
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
        String inputPasswordHashed = getHashedValue(password);

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
    public boolean register(String username, String password, String securityQuestion, String securityAnswer){
        UserInfo userinfo = getUserInfoFromFile(username);
        if (userinfo != null){
            System.out.println("User already exists");
            return false;
        }
        String hashedPassword = getHashedValue(password);
        String hashedSecurityAnswer = getHashedValue(securityAnswer);
        String[] values = { username, hashedPassword, securityQuestion, hashedSecurityAnswer };
        FileWriter fileWriter = null;
        PrintWriter printWriter = null;
        File authFile = new File(authFilePath);
        try{
            if (authFile.exists()){
                fileWriter = new FileWriter(authFilePath, true);
            } else {
                authFile.createNewFile();
                fileWriter = new FileWriter(authFilePath);
            }
            printWriter = new PrintWriter(fileWriter);
            String line = String.join(Constants.fileSeparator, values);
            printWriter.println(line);
            printWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        File userDirectory = new File("database/"+username);
        userDirectory.mkdir();
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
                String[] values = line.split(Constants.fileSeparator);
                if (values[0].equals(username)) {
                    userInfo = new UserInfo(values[0], values[1], values[2], values[3]);
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
    public String getHashedValue(String password){
        messageDigest.update(password.getBytes());
        byte[] bytes = messageDigest.digest();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            stringBuilder.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuilder.toString();
    }
    
    /**
     * Utility method to fetch security question of the user
     * @param username - name of the user
     * @return returns the security question if user exists.
     */
    public String getSecurityQuestion(String username) {
        UserInfo userInfo = getUserInfoFromFile(username);

        if (userInfo == null){
            System.out.println("User not found");
            return null;
        }

        return userInfo.getSecurityQuestion();
    }

    /**
     * Utility method to verify security answer of the user
     * @param username - name of the user
     * @param securityAnswer - answer provided by the user
     * @return returns true if the security answer matches
     */
    public boolean verifySecurityQA(String username, String securityAnswer) {
        UserInfo userInfo = getUserInfoFromFile(username);

        if (userInfo == null){
            System.out.println("User not found");
            return false;
        }

        String userSecurityAnswer = userInfo.getSecurityAnswer();
        String inputHashedSecurityAnswer = getHashedValue(securityAnswer);

        if (userSecurityAnswer.equals(inputHashedSecurityAnswer)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Utility method that checks if the username already exists
     * @param username username that needs to be checked
     * @return returns true if username exists
     */
    public boolean exists(String username) {
        UserInfo userinfo = getUserInfoFromFile(username);
        if (userinfo != null){
            return true;
        }
        return false;
    }
}