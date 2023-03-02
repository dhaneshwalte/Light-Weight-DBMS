package dal.dmw.w23.models;

/**
 * Class that represents a user
 */
public class UserInfo {
    /**
     * name of the user
     */
    private String username;
    /**
     * hashedPassword of the user
     */
    private String hashedPassword;
    public UserInfo(String username, String hashedPassword){
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getUsername(){
        return this.username;
    }

    public String getHashedPassword(){
        return this.hashedPassword;
    }
}