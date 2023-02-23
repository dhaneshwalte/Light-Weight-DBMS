public class UserInfo {
    private String username;
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