import service.RenameService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Boot {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Path path;

        while (true) {
            try {
                System.out.print("Enter the path to the folder: ");
                Thread.sleep(100);
                path = new File(scanner.nextLine().replaceAll("\"", "")).toPath();
                if (!Files.exists(path)) {
                    System.err.println("There is no such path");
                    Thread.sleep(100);
                } else if (!Files.isDirectory(path)) {
                    System.err.println("This is not a directory");
                    Thread.sleep(100);
                } else break;
            } catch (Exception e) {
                System.err.println("This is not a directory");
            }
        }

        String tempString;
        do {
            System.out.print("Do you want delete files? (y/n): ");
            tempString = scanner.nextLine();
        } while (!(tempString.equals("y") || tempString.equals("n")));

        Boolean deleteFiles = tempString.equals("y");
        System.out.println("Start");

        RenameService renameService = new RenameService();

        renameService.renameAllFiles(path.toString(), deleteFiles);

        System.out.println("End");
    }
}
