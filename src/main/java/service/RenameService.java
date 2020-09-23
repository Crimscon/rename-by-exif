package service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class RenameService {

    public void renameAllFiles(String path, Boolean deleteFiles) {
        try {
            Files.walkFileTree(Paths.get(path), new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (attrs.isDirectory()) {
                        if ("renamed".equals(dir.getFileName().toString())) {
                            return FileVisitResult.SKIP_SUBTREE;
                        } else return FileVisitResult.CONTINUE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (isImage(file)) {
                        renameAndCopyFile(file);
                        if (deleteFiles) {
                            try {
                                Files.delete(file);
                            } catch (IOException e) {
                                System.err.println("Can't delete file (" + file.getFileName() + ")");
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void renameAndCopyFile(Path file) {
        File renameDir = new File(file.getParent().toAbsolutePath() + "/renamed");

        createDirectory(renameDir);
        LocalDateTime creationDate = getCreationDate(file);

        if (!creationDate.equals(LocalDateTime.MIN)) {
            String extension = getExtension(file);

            File dirName = new File(
                    renameDir.getAbsolutePath()
                            + "/"
                            + creationDate.format(DateTimeFormatter.ofPattern("yyyy MMMM")));

            File filename = new File(
                    dirName.getAbsolutePath()
                            + "/"
                            + creationDate.format(DateTimeFormatter.ofPattern("yyyy_MM_dd HH-mm-ss"))
                            + (extension.isEmpty()
                            ? "" : ("." + extension)));

            createDirectory(dirName);

            try {
                Files.copy(file, filename.toPath(), StandardCopyOption.REPLACE_EXISTING);

                System.out.println(file.getFileName()
                        + " >>> " + filename.getName());
            } catch (IOException e) {
                System.err.println("Can't copy file (" + file.getFileName() + ")");
            }
        }
    }

    private String getExtension(Path file) {
        String extension = "";
        int i = file.getFileName().toString().lastIndexOf(".");

        if (i > 0) {
            extension = file.getFileName().toString().substring(i + 1);
        }

        return extension;
    }

    private LocalDateTime getCreationDate(Path file) {
        try {
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

            LocalDateTime creationDateFromBasicAttr = LocalDateTime
                    .ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault());
            LocalDateTime lastModifiedDateFromBasicAttr = LocalDateTime
                    .ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.systemDefault());

            LocalDateTime firstDateFromBasicAttr = creationDateFromBasicAttr.isBefore(lastModifiedDateFromBasicAttr) ?
                    creationDateFromBasicAttr : lastModifiedDateFromBasicAttr;

            LocalDateTime creationDateFromExif = getCreationDateFromExif(file);

            return firstDateFromBasicAttr.isBefore(creationDateFromExif) ?
                    firstDateFromBasicAttr : creationDateFromExif;
        } catch (IOException e) {
            System.err.println("Can't read file (" + file.getFileName() + ") attributes");
            return LocalDateTime.MIN;
        }
    }

    private LocalDateTime getCreationDateFromExif(Path file) {
        LocalDateTime creationDateFromExif = LocalDateTime.now();

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file.toFile());

            ExifSubIFDDirectory directory
                    = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory != null && directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL) != null) {
                creationDateFromExif = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
        } catch (Exception e) {
            return creationDateFromExif;
        }

        return creationDateFromExif;

    }

    private boolean isImage(Path file) {
        try {
            return ImageIO.read(file.toFile()) != null;
        } catch (IOException e) {
            return false;
        }
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

}
