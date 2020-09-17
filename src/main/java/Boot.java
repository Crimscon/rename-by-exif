import service.RenameService;

import java.util.Scanner;

public class Boot {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the path to the folder: ");
        String path = scanner.nextLine();
        System.out.println("Start");

        RenameService renameService = new RenameService();

        renameService.renameAllFiles(path);

        System.out.println("End");

    }
}
