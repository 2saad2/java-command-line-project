package fr.uvsq.cprog;

import java.io.IOException;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandeTest {

    @TempDir
    Path tempDir;

    Path file;
    Path directory;
    Path copiedFile;

    private FileManager fileManager;
    private Commande commande;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() throws IOException {
        fileManager = new FileManager();
        commande = new Commande(fileManager);

        // Redirection de la sortie standard
        System.setOut(new PrintStream(outContent));

        // Setup: Créer un dossier temporaire dans tempDir et vérifier son existence.
        directory = tempDir.resolve("testDirectory");
        Files.createDirectory(directory);
        assertTrue(Files.isDirectory(directory), "Le répertoire n'est pas créé avec succès");

        // Setup: Créer un fichier temporaire dans tempDir et vérifier son existence.
        file = tempDir.resolve("testFile.txt");
        Files.writeString(file, "Contenu de test");
        assertTrue(Files.exists(file));

        // Simuler le changement du répertoire courant à tempDir
        fileManager.updateCurrentDir(tempDir.toString());
    }

    @Test
    public void testCopyPast() throws IOException {
        // Action: Copier le fichier.
        commande.copyFile(2, false);
        // Simuler le changement du répertoire courant à tempDir/testDirectory
        fileManager.updateCurrentDir(directory.toString());
        // Action: Coller le fichier.
        commande.pasteFile("");
        copiedFile = directory.resolve("testFile-copy.txt");
        // Vérifier l'existence du fichier testFile-copy.txt
        assertTrue(Files.exists(copiedFile));
    }

    @Test
    public void testCutPast() throws IOException {
        // Action: Copier le fichier.
        commande.copyFile(2, false);
        // Simuler le changement du répertoire courant à tempDir/testDirectory
        fileManager.updateCurrentDir(directory.toString());
        // Action: Coller le fichier.
        commande.pasteFile("cut");
        copiedFile = directory.resolve("testFile.txt");
        // Vérifier l'existence du fichier tempDir/testDirectory/testFile.txt
        assertTrue(Files.exists(copiedFile));
        // Vérifier l'inexistence du fichier tempDir/testFile.txt
        assertFalse(Files.exists(file));
    }

    @Test
    public void testCutCopy() throws IOException {
        // Action: Copier le fichier.
        commande.copyFile(2, false);
        // Simuler le changement du répertoire courant à tempDir/testDirectory
        commande.copyFile(1, true);
        // Vérifier l'inexistence du fichier tempDir/testFile.txt
        assertFalse(Files.exists(file));
    }

    @Test
    public void testMkdir() throws IOException {
        // Action: Création du repertoire avec mkdir().
        commande.mkdir("testDirectory");
        Path madeDirectory = tempDir.resolve("testDirectory");
        // Vérifier l'inexistence du repertoire tempDir/testDirectory
        assertTrue(Files.exists(madeDirectory));
        Files.deleteIfExists(madeDirectory);
    }

    @Test
    public void testVisu() throws IOException {
        // Action: Visualiser le contenu du fichier
        commande.visu(2);
        // Assertion: Vérifier que le contenu du fichier est affiché
        String expectedOutput = "Contenu de test" + System.lineSeparator();
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    public void testVisuWithDirectory() throws IOException {
        // Action: Tenter de visualiser le contenu du dossier
        commande.visu(1);

        assertFalse(fileManager.isTextFile(fileManager.getPathByNER(1)));
        assertTrue(Files.isDirectory(fileManager.getPathByNER(1)));
    }

    @Test
    public void testVisuWithNonTexteFile() throws IOException {
        // Création d'un fichier non-texte (exemple: fichier binaire)
        Path nonTextFile = tempDir.resolve("nonTextFile.bin");
        byte[] binaryContent = new byte[256];
        for (int i = 0; i < 256; i++) {
            binaryContent[i] = (byte) i;
        }
        Files.write(nonTextFile, binaryContent); // Contenu binaire étendu
        assertTrue(Files.exists(nonTextFile));
        assertFalse(fileManager.isTextFile(fileManager.getPathByNER(1)));
        assertFalse(Files.isDirectory(fileManager.getPathByNER(1)));
    }

    @Test
    public void testFind() throws IOException {
        // Appel de la méthode find
        commande.find("testFile.txt");

        // Convertir le chemin en format relatif pour la comparaison
        String relativePath = FileManager.getRelativePath(file.toString(), tempDir.toString());

        // Vérification si la sortie contient le chemin du fichier
        assertTrue(outContent.toString().contains(relativePath),
                "Le fichier recherché n'a pas été trouvé par la méthode find");

    }
    
    @AfterEach
    public void tearDown() throws IOException {
        // Restaurer la sortie standard
        System.setOut(System.out);
        if (file != null) {
            Files.deleteIfExists(file);
        }
        if (copiedFile != null) {
            Files.deleteIfExists(copiedFile);
        }
        if (directory != null) {
            Files.deleteIfExists(directory);
        }
}


}
