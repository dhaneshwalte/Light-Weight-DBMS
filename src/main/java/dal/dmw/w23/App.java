package dal.dmw.w23;

import dal.dmw.w23.services.ConsoleService;

public class App {
    public static void main( String[] args ){
        ConsoleService consoleService = new ConsoleService(System.in);
        consoleService.run();
    }
}
