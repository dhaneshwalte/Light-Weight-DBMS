package dal.dmw.w23;

import java.util.Scanner;

public class App {
    public static void main( String[] args ){
        handleMainMenu();
    }

    static void handleMainMenu(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("1. Login\n2. Register");
        int choice = Integer.parseInt(scanner.nextLine());
        if (choice == 1){
            handleLogin();
        }
        else if (choice == 2){
            handleRegistration();
            handleMainMenu();
        }
        scanner.close();
    }

    static void handleLogin(){
        AuthService authService = new AuthService();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username");
        String username = scanner.nextLine();
        System.out.println("Enter Password");
        String password = scanner.nextLine();
        if (!authService.authenticate(username, password)){
            System.out.println("Invalid Credentials");
            scanner.close();
            return;
        }
        System.out.println("Login Successful");
        while(true){
            System.out.print(">> ");
            String inpuString = scanner.nextLine();
            QueryEngine queryEngine = new QueryEngine(username);
            queryEngine.executeQuery(inpuString);
        }
    }

    static void handleRegistration(){
        AuthService authService = new AuthService();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username");
        String username = scanner.nextLine();
        System.out.println("Enter Password");
        String password = scanner.nextLine();
        if(authService.register(username, password)){
            System.out.println("Registration Successful");
        }else{
            System.out.println("User already exists");
        }
        scanner.close();
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
