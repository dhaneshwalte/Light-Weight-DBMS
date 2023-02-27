package dal.dmw.w23;

public class App {
    public static void main( String[] args ){
        String selectString = "Select * from users";
        String updateString = "Update users set user_id = 5, username=Mavy2 where user_id = 3";
        String deleteString = "DELETE FROM users WHERE username=dan and email=dan@dan.dan";
        String insertQueryString = "INSERT INTO users VALUES (dan,dan,1234,dan@dan.dan)";
        String insertQueryString2 = "INSERT INTO users VALUES (2,dan2,1234,dan@dan.dan)";
        String insertColumnQueryString = "INSERT INTO users(user_id, username) VALUES (3,dan3)";
        String createQueryString = """
            CREATE TABLE users(
            user_id INT PRIMARY KEY,
            username VARCHAR(40) UNIQUE,
            password VARCHAR(255),
            email VARCHAR(255) NOT NULL
         );
        """;
        QueryEngine queryEngine = new QueryEngine();
        queryEngine.executeQuery(insertQueryString);
        //queryEngine.executeQuery(insertQueryString2);
        //queryEngine.executeQuery(deleteString);
    }
}
