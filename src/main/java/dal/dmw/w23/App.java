package dal.dmw.w23;

public class App {
    public static void main( String[] args ){
        AuthService authService = new AuthService();
        String username = "root";
        String password = "password";
        authService.register(username, password);
        if (!authService.authenticate(username, password)){
            System.out.println("Invalid Credentials");
            return;
        }
        String selectString = "Select * from users";
        String updateString = "Update users set user_id = 5, username=ricky where username=dan";
        String deleteString = "DELETE FROM users WHERE username=dan and email=dan@dan.dan";
        String insertQueryString1 = "INSERT INTO users VALUES (1,dan,1234,dan@gmail.com)";
        String insertQueryString2 = "INSERT INTO users VALUES (2,mavy,1234,mavy@gmail.com)";
        String insertColumnQueryString = "INSERT INTO users(user_id, username) VALUES (3,dan3)";
        String createQueryString = """
            CREATE TABLE users3(
            user_id INT PRIMARY KEY,
            username VARCHAR(40) UNIQUE,
            password VARCHAR(255),
            email VARCHAR(255) NOT NULL
         );
        """;
        QueryEngine queryEngine = new QueryEngine();
        queryEngine.executeQuery(updateString);
        //queryEngine.executeQuery(insertQueryString2);
        //queryEngine.executeQuery(deleteString);
    }
}
