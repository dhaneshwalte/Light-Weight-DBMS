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
    /**
     * security question of the user
     */
    private String securityQuestion;
    /**
     * security answer of the user
     */
    private String securityAnswer;
    
    public UserInfo(String username, String hashedPassword, String securityQuestion, String securityAnswer) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    public String getUsername(){
        return this.username;
    }

    public String getHashedPassword(){
        return this.hashedPassword;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    
}