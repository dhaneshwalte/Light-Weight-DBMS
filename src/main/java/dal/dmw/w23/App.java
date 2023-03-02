package dal.dmw.w23;

import dal.dmw.w23.services.ConsoleService;
import dal.dmw.w23.services.QueryEngine;

public class App {
    public static void main( String[] args ){
        ConsoleService consoleService = new ConsoleService(System.in);
        consoleService.run();
    }
    public void testSql(){
        String selectString = "Select * from users";
        String updateString = "Update users set email = newjack@gmail.com where username=jack;";
        String deleteString = "DELETE FROM users WHERE username=dan;";
        String insertQueryString1 = "INSERT INTO users VALUES (1,dan,1234,dan@gmail.com);";
        String insertQueryString2 = "INSERT INTO users VALUES (2,mavy,1234,mavy@gmail.com);";
        String insertQueryString3 = "INSERT INTO users VALUES (3,ricky,1234,ricky@gmail.com);";
        String insertQueryString4 = "INSERT INTO users VALUES (4,jack,1234,jack@gmail.com);";
        String insertColumnQueryString = "INSERT INTO users(user_id, username) VALUES (3,dan3)";
        String createQueryString = """
            CREATE TABLE users(user_id INT PRIMARY KEY, username VARCHAR(40) UNIQUE, password VARCHAR(255), email VARCHAR(255) NOT NULL);
        """;
        String createQueryString2 = """
            CREATE TABLE inventory(item_id INT PRIMARY KEY, username VARCHAR(40) UNIQUE, password VARCHAR(255));
        """;
        QueryEngine queryEngine = new QueryEngine("dummy");
        // queryEngine.executeQuery(insertQueryString1);
        // queryEngine.executeQuery(insertQueryString2);
        // queryEngine.executeQuery(insertQueryString3);
        // queryEngine.executeQuery(insertQueryString4);
        queryEngine.executeQuery(selectString);
        //queryEngine.executeQuery(insertQueryString2);
        //queryEngine.executeQuery(deleteString);
    }
}
