import java.util.Scanner;

public class index{
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AuthService authService = new AuthService();
        System.out.println("1. Login\n2. Register");
        int choice = Integer.parseInt(scanner.nextLine());
        if (choice == 1){
            System.out.println("Enter username");
            String username = scanner.nextLine();
            System.out.println("Enter Password");
            String password = scanner.nextLine();
            if (!authService.authenticate(username, password)){
                System.out.println("Invalid Credentials");
                scanner.close();
                return;
            }
        }
        else if (choice == 2){
            System.out.println("Enter username");
            String username = scanner.nextLine();
            System.out.println("Enter Password");
            String password = scanner.nextLine();
            if(authService.register(username, password)){
                System.out.println("Registration Successful");
            }else{
                System.out.println("User already exists");
            }
        }
        scanner.close();
    }
}