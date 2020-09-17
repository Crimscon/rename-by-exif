package service;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

public class RenameService {

    private void renameFile(Path file) throws IOException {

        if (ImageIO.read(file.toFile()) == null || file.toAbsolutePath().toString().contains("renamed")) {
            return;
        }

        File renamedDir = new File(file.getParent().toAbsolutePath() + "/renamed");

        createDirectory(renamedDir);

        BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

        LocalDateTime firstDate = LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault())
                .isBefore(LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.systemDefault())) ?
                LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault()) :
                LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.systemDefault());

        String[] fileArray = file.toString().split("\\.");
        String extension = fileArray[fileArray.length - 1];

        File dirName = new File(
                renamedDir.getAbsolutePath()
                        + "/"
                        + firstDate.format(DateTimeFormatter.ofPattern("yyyy MMMM")));

        File filename = new File(
                dirName.getAbsolutePath()
                        + "/"
                        + firstDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm"))
                        + "."
                        + extension);

        createDirectory(dirName);

        if (!file.toFile().equals(filename))
            Files.copy(file, filename.toPath(), StandardCopyOption.REPLACE_EXISTING);

        System.out.println(file.getFileName() + " >>> " + filename.getName());
    }

    private void createDirectory(File directory) {
        if (Files.notExists(directory.toPath())) {
            try {
                Files.createDirectory(directory.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void renameAllFiles(String path) {
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            renameFile(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
