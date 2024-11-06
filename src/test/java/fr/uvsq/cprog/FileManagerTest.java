package fr.uvsq.cprog;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileManagerTest {
    public static final String FILE_CONTENT = """
            Bonjour et bienvenue dans notre mini projet Gestionnaire de fichiers.
            Dans ce code on travaillera en commande en ligne.
            Le gestionnaire de fichier s'appuie sur le système de fichier de votre système d'exploitation.
            Ce projet a été réalisé par Farouk Delloumi et Saad Dahmani.
            """;

    @TempDir
    Path tempDir;

    Path file;
    Path file_1;
    Path directory;

    @BeforeEach
    public void setUp() throws IOException {
        directory = tempDir.resolve("testDirectory");
        Files.createDirectory(directory);
        assertTrue(Files.isDirectory(directory), "Le répertoire n'est pas créé avec succès");

        file = tempDir.resolve("testfile.txt");
        Files.writeString(file, FILE_CONTENT);

        file_1 = directory.resolve("testfile_1.txt");
        Files.writeString(file_1, "Contenu de test pour testfile_1.txt");
    }

    @Test
    void testIsTextFileWithTextFile() throws IOException {
        FileManager fileManager = new FileManager();
        assertTrue(fileManager.isTextFile(file));

    }

    @Test
    void testgetDirectoryContents() {
        FileManager fileManager = new FileManager();
        var expected = List.of("testfile_1.txt");
        assertEquals(expected, fileManager.getDirectoryContents(directory));

    }

    @Test
    void testGetRelativePath() {
        String expected = "testfile_1.txt";
        assertEquals(expected, FileManager.getRelativePath(file_1.toString(), directory.toString()));
    }

    @Test
    void testDeleteFileorDirectory() {
        FileManager fileManager = new FileManager();
        var expected = List.of();
        fileManager.deleteFileOrDirectory(file_1);
        assertEquals(expected, fileManager.getDirectoryContents(directory));
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(file);
        Files.deleteIfExists(file_1);
        Files.deleteIfExists(directory);
    }
}
