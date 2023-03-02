package dal.dmw.w23.services;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Service class that handles user inputs for queries via Console
 */
public class ConsoleService {
    /**
     * scanner to read user input
     */
    Scanner scanner;
    public ConsoleService(InputStream inputStream){
        scanner = new Scanner(inputStream);
    }

    /**
     * Main method that prints the console and redirects call to respective functions
     */
    public void run(){
        userInput("1. Login\n2. Register\n3. Exit\nEnter Choice: ");
        int choice = Integer.parseInt(scanner.nextLine());
        if (choice == 1){
            handleLogin(scanner);
            run();
        }
        else if (choice == 2){
            handleRegistration(scanner);
            run();
        }
        else if (choice == 3){
            System.exit(0);
        }
        scanner.close();
    }

    /**
     * Handles user input for login
     * @param scanner - scanner through which input is to be handled
     */
    void handleLogin(Scanner scanner){
        AuthService authService = new AuthService();
        userInput("Enter username");
        String username = scanner.nextLine();
        userInput("Enter Password");
        String password = scanner.nextLine();
        if (!authService.authenticate(username, password)){
            System.out.println("Invalid Credentials, Please try again");
            return;
        }
        userInput("Security Question: " + authService.getSecurityQuestion(username));
        String answer = scanner.nextLine();
        if (!authService.verifySecurityQA(username, answer)){
            System.out.println("Incorrect Answer To The Security Question, Please try again");
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

    /**
     * Handles input for user registration
     * @param scanner - scanner through which input is to be handled
     */
    void handleRegistration(Scanner scanner){
        AuthService authService = new AuthService();
        userInput("Enter username");
        String username = scanner.nextLine();
        if (authService.exists(username)){
            System.out.println("User Already Exists");
            return;
        }
        userInput("Enter Password");
        String password = scanner.nextLine();
        userInput("Enter Security Question");
        String securityQuestion = scanner.nextLine();
        userInput("Enter Security Answer");
        String securityAnswer = scanner.nextLine();
        if(authService.register(username, password, securityQuestion, securityAnswer)){
            System.out.println("Registration Successful");
        }
    }

    /**
     * Method to add input angle bracket '>'
     * whenever input is expected from the user
     * @param s - string to be printed
     */
    void userInput(String s){
        System.out.print(s + "\n" + "> ");
    }
}
