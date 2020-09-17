package service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

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

    private void renameFile(Path file, Boolean deleteFiles) throws IOException {

        if (ImageIO.read(file.toFile()) == null || file.toAbsolutePath().toString().contains("renamed")) {
            return;
        }

        File renamedDir = new File(file.getParent().toAbsolutePath() + "/renamed");

        createDirectory(renamedDir);

        BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
        LocalDateTime originalDate = LocalDateTime.now();

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file.toFile());

            ExifSubIFDDirectory directory
                    = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            originalDate = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            e.printStackTrace();
        }

        LocalDateTime firstDate = LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault())
                .isBefore(LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.systemDefault())) ?
                LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault()) :
                LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.systemDefault());
        LocalDateTime lastDate = firstDate.isBefore(originalDate) ? firstDate : originalDate;

        String[] fileArray = file.toString().split("\\.");
        String extension = fileArray[fileArray.length - 1];

        File dirName = new File(
                renamedDir.getAbsolutePath()
                        + "/"
                        + lastDate.format(DateTimeFormatter.ofPattern("yyyy MMMM")));

        File filename = new File(
                dirName.getAbsolutePath()
                        + "/"
                        + lastDate.format(DateTimeFormatter.ofPattern("yyyy_MM_dd HH-mm-ss"))
                        + "."
                        + extension);

        createDirectory(dirName);

        if (!file.toFile().equals(filename)) {
            Files.copy(file, filename.toPath(), StandardCopyOption.REPLACE_EXISTING);
            if (deleteFiles) Files.delete(file);
        }

        System.out.println(file.getFileName()
                + (deleteFiles ? "(deleted)" : "")
                + " >>> " + filename.getName());
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

    public void renameAllFiles(String path, Boolean deleteFiles) {
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            renameFile(file, deleteFiles);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
